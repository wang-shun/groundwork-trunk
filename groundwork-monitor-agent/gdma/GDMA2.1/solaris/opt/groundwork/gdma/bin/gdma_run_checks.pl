#!/opt/groundwork/perl/bin/perl -w --
################################################################################
#
#    gdma_run_checks.pl
#
#    This program is a subsidiary of GDMA Poller program.  It executes the
#    plugins for a hostname passed as an argument to it.  The checks to be
#    executed are read from the configuration file for that host.  This
#    program write the checks results to the spool file.  The write to the
#    spool file is synchronized with a lock.
#
#    Copyright 2003-2018 GroundWork Open Source, Inc.
#    http://www.groundworkopensource.com
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.
#
################################################################################

use strict;
use warnings;

use Getopt::Std;
use Fcntl qw(:DEFAULT :flock);
use Sys::Hostname;
use Config;
use IO::Socket;
use IO::Handle;

# The sigtrap die handler will not reliably bring down the process in the
# presence of eval{}-block evaluation, which can occur both at startup time
# (during compilation of various packages) and later at run time:
#     https://groups.google.com/forum/#!topic/comp.lang.perl.misc/tIrHsavy6Xw
# Thus we no longer use it here.  Instead, we allow the process to just die
# a natural death upon receipt of a signal before we establish our own
# signal handlers.  If we did want a background signal handler in place,
# the way to do that would probably be to use this instead, for each signal
# of interest:
#     use Carp;
#     $SIG{PIPE} = sub { carp("Caught a SIG$_[0]"); exit(1); };
# except that having carp() write to STDERR is probably not useful for this
# daemon, anyway.  Better would be some formulation that figures out if we
# have got far enough to know what our logfile is supposed to be, and if so,
# writes the message there before exiting.
#
## use sigtrap qw(die normal-signals);

use POSIX qw(:sys_wait_h :signal_h :errno_h _exit);

use GDMA::Logging;
use GDMA::Utils;

our $VERSION = '2.6.1';

if ( $^O eq 'MSWin32' ) {
    ## Check if we support the windows flavor.
    ## We are going to use Win32::Job to create and manipulate sub-processes.
    ## Win32:Job is supported only on Windows 2000 and later versions.
    require Win32;
    my $osname = Win32::GetOSName();
    if (   ( $osname =~ /^WinME/ )
	or ( $osname =~ /^WinNT/ )
	or ( $osname =~ /^Win95/ )
	or ( $osname =~ /^Win98/ ) )
    {
	die "$osname is not a supported operating system";
    }
    ## Load Win32:Job on windows platform only.
    ## We need this to create and manipulate processes to execute plugins.
    require Win32::Job;
    import Win32::Job;
}

sub exec_plugincmd_unix;
sub exec_plugincmd_windows;
sub insert_plugin_error_message;
sub insert_plugin_timeout_message;
sub read_and_process_plugin_output;
sub main;
sub get_version;
sub set_environment;
sub run_check;
sub if_run_check_now;
sub get_last_check_details;
sub update_check_status;
sub install_config_filepaths;
sub insert_into_result_buf;
sub get_status_filepath;

# Command line options
my %g_opt = ();

# A global hash that stores all the configuration file parameters.
my %g_config = ();

my $logging = undef;
my $logger  = undef;

my $log_prefix     = ( $^O eq 'MSWin32' ) ? "PID $$: " : '';
my $file_separator = ( $^O eq 'MSWin32' ) ? "\\"       : '/';

# Handle Command Line Options.
handle_cmd_line();

# We need to prohibit executing as root (say, for a manual debugging run), so we
# don't create files that won't be modifiable later on when this script is run in
# its daemon mode as an ordinary user.  But we run this check after handling the
# command-line arguments, so we can always at least run the -h (help) and -v
# (version) options to just spill out useful information that can't be damaging.
# To make that reasonable, handle_cmd_line() can't do anything that touches any
# outside resources.
#
# FIX LATER:  Modify this test to check affirmatively against the particular user the
# GDMA software was installed as, rather than negatively against just the root user.
#
# We don't apply this test to Windows primarily because GDMA will ordinarily be run
# as a system service in that environment, and the standard service account will have
# super-user privileges anyway.
if ( ( $^O eq 'linux' ) or ( $^O eq 'solaris' ) or ( $^O eq 'aix' ) or ( $^O eq 'hpux' ) ) {
    if ($> == 0) {
	(my $program = $0) =~ s<.*/><>;
	die "ERROR:  You cannot run $program as root.\n";
    }
}

# Set up the environment so that we will access correct libraries.
# Pass the installation base directory we received as an argument.
set_environment( $g_opt{p} );

# Call the main processing function, turning is function status into a process exit status.
exit( main() ? 0 : 1 );

################################################################################
#
#   main()
#
#   The main processing function.  To begin with, it reads the host
#   configuration file.  It then executes the checks defined in the config file
#   and writes the results to a spool file.  It writes the number of checks
#   spooled to a diskfile.  Returns 1 on success, 0 otherwise.
#
################################################################################
sub main {
    ## Path for the configuration file.
    my $HostconfigfilePath;
    ## Holds error cause.
    my $errstr;
    ## Buffer to hold the results to be spooled.
    my @result_buf;
    my $spool_filename;
    my $blocking = 1;
    my $osname = $^O;
    ## The number of results successfully spooled.
    my $num_results = 0;
    my $num_checks_file = undef;
    my $num_checks_fhw;
    my $rc = 1;

    # Make sure end-of-life messages are output before we call POSIX::_exit to quit.
    STDOUT->autoflush(1) if $g_opt{i};

    # We presently use bundled logging in case there might be some parallelism between multiple
    # copies of this script running at the same time, logging into the same file.  An uncaught
    # signal will cause END blocks to be skipped, which means that a side effect of our bundled
    # logging is that buffered log messages will never make it out into the logfile if such a
    # signal appears.  If that turns out to be an issue, and if we don't in fact have more than one
    # concurrent copy of this script logging into the same file (but see the Max_Concurrent_Hosts
    # option), we could switch the logging over to "grouping => 'individual'".
    #
    my $logging_hostname = $g_opt{l} ? $g_opt{H} : undef;
    my $logging_logfile = $g_opt{l} ? $g_opt{D} . $file_separator . 'gwmon_' . $logging_hostname . '_run_checks.log' : undef;
    my %logging_options = ( logfile => $logging_logfile, grouping => 'bundled' );
    $logging_options{stdout}    = 1       if $g_opt{i};
    $logging_options{log_level} = 'debug' if $g_opt{d};
    $logging_options{log_level} = 'trace' if $g_opt{d} && $g_opt{d} =~ /^(\d+)$/ && $g_opt{d} > 1;

    ## FIX MINOR:  Perhaps provide appropriate external values for these settings, presumably from command-line arguments.
    ## IF we don't supply these options, they'll be defaulted to sane values internal to the GDMA::Logging package.
    # $logging_options{max_logfile_size}       = 10_000_000;
    # $logging_options{max_logfiles_to_retain} = 5;

    $logging = GDMA::Logging->new( \%logging_options, 'started', \*STDERR );
    if ( not defined $logging ) {
	print "FATAL:  Cannot create a GDMA::Logging object" . ( defined($logging_logfile) ? " for file \"$logging_logfile\"" : '' ) . ".\n";
	return 0;
    }
    $logger = $logging->logger();

    if ( $g_opt{l} ) {
	my $log_spin_status = $logging->rotate_logfile();
	if ( $log_spin_status == 0 ) {
	    ## Log rotation failed; this process should not asssume that logging is still functional.
	    $logger->warn("WARNING:  Logfile rotation failed in process $$.  Logging might no longer be functional.");
	}
    }

    # Set-up SIGTERM and SIGINT handlers to make sure that we are
    # sensitive to these signals, for linux, solaris, aix, and hpux.
    if ( ( $^O eq 'linux' ) or ( $^O eq 'solaris' ) or ( $^O eq 'aix' ) or ( $^O eq 'hpux' ) ) {
	## FIX TODAY:  this is the wrong way to handle these signals; instead, we
	## must forward these signals to our child process(es), if any, before we exit.
	## $SIG{TERM} = sub { $SIG{TERM} = 'IGNORE'; kill "TERM", -$$; exit 1 };
	## $SIG{INT}  = sub { $SIG{INT}  = 'IGNORE'; kill "INT",  -$$; exit 1 };
    }

    # Set up the path for a file where the number of check results spooled will be written.
    if (($osname eq 'linux') or ($osname eq 'solaris') or ($osname eq 'aix') or ($osname eq 'hpux')) {
	$num_checks_file = "$g_opt{p}/tmp/$g_opt{H}" . "_checks.txt";
    }
    elsif ($osname eq 'MSWin32') {
	$num_checks_file = "$g_opt{p}\\tmp\\$g_opt{H}" . "_checks.txt";
    }
    else {
	$logger->error("ERROR:  $osname is not a supported operating system.");
	$rc = 0;
	goto end;
    }
    ## Set the value for host config filepath based on platform.
    $HostconfigfilePath = install_config_filepaths($g_opt{p}, $g_opt{H});

    # Read the host config file.
    # FIX MAJOR:  Ought we to lock the config file for reading, as we do in
    # other scripting?  Or is that presumed not to be needed here because this
    # script will be called only from the poller, which is responsible for
    # synchronously modifying the config file with respect to calling this script?
    if (!GDMA::Utils::read_config($HostconfigfilePath, \%g_config, \$errstr, 0)) {
	## Log error and bail out.  There is nothing else we can do.
	$logger->error("ERROR:  Failed to read $HostconfigfilePath:  $errstr");
	$rc = 0;
	goto end;
    }

    # On a single-host GDMA client, Monitor_Host_Type can be any of:
    #
    # undefined                => default is 'hostname_command'
    # 'hostname_command'       => run `hostname`    on this machine to find Monitor_Host
    # 'long_hostname_command'  => run `hostname -f` (`hostname` on Windows) on this machine to find Monitor_Host
    # 'short_hostname_command' => run `hostname -s` (`hostname` on Windows) on this machine to find Monitor_Host
    # 'config_file_hostname'   => legacy behavior:  use the hostname embedded in the config-file name (or equivalently, $g_opt{H})
    # 'Monitor_Host'           => use the Monitor_Host value literally if available, or default back to legacy behavior
    #
    # On a multi-host GDMA client, Monitor_Host_Type for the GDMA client itself is
    # interpreted exactly the same as for a single-host GDMA client.
    #
    # On a multi-host GDMA client, Monitor_Host_Type for the other monitored machines (that is, where $g_opt{f} is true) is:
    #
    # undefined                => default is 'config_file_hostname'
    # 'hostname_command'       => option value is silently ignored; defaulted to 'config_file_hostname'
    # 'long_hostname_command'  => option value is silently ignored; defaulted to 'config_file_hostname'
    # 'short_hostname_command' => option value is silently ignored; defaulted to 'config_file_hostname'
    # 'config_file_hostname'   => legacy behavior:  use the hostname embedded in the config-file name (or equivalently, $g_opt{H})
    # 'Monitor_Host'           => use the Monitor_Host value literally if available, or default back to legacy behavior

    if ( $g_opt{f} ) {
	$g_config{Monitor_Host} = $g_opt{H}
	  if not defined( $g_config{Monitor_Host_Type} )
	      or $g_config{Monitor_Host_Type} ne 'Monitor_Host'
	      or not $g_config{Monitor_Host};
    }
    else {
	if ( not $g_config{Monitor_Host_Type} or $g_config{Monitor_Host_Type} eq 'hostname_command' ) {
	    $g_config{Monitor_Host} = find_my_hostname('hostname');
	}
	elsif ( $g_config{Monitor_Host_Type} eq 'long_hostname_command' ) {
	    $g_config{Monitor_Host} = find_my_hostname( $^O eq 'MSWin32' ? 'hostname' : 'hostname -f' );
	}
	elsif ( $g_config{Monitor_Host_Type} eq 'short_hostname_command' ) {
	    $g_config{Monitor_Host} = find_my_hostname( $^O eq 'MSWin32' ? 'hostname' : 'hostname -s' );
	}
	elsif ( $g_config{Monitor_Host_Type} eq 'config_file_hostname' ) {
	    ## Legacy behavior.
	    $g_config{Monitor_Host} = $g_opt{H};
	}
	elsif ( not $g_config{Monitor_Host} ) {
	    $g_config{Monitor_Host} = $g_opt{H};
	}
    }

    # Now execute the checks.
    foreach my $checkname (keys %g_config) {
	## If in config file
	if (defined($g_config{$checkname})) {
	    ## Only process if name contains "Check_"
	    if ( $checkname !~ /Check_/i ) { next; }
	    ## run_check() updates the result_buffer
	    ## Even if a check fails, we still got to execute the rest.
	    run_check($checkname, \@result_buf);
	}
    }

    # Get the spool filename.
    $spool_filename = GDMA::Utils::get_spool_filename($g_opt{p});

    # Flush results out to the spool file immediately.
    # Make a blocking write call.  If we don't record results here, we lose them.
    if (!GDMA::Utils::spool_results($spool_filename, \@result_buf, $blocking, \$num_results, \$errstr)) {
	## This is loss of data.
	$logger->error("ERROR:  Failed to write results to spool file:  $errstr");
	$rc = 0;
	$num_results = 0;
    }

    # We will write the number of checks spooled to a diskfile for the poller to pick up.
    # There will be a different file for each monitored host.
    # Open the num checks file for writing.
end:
    my $old_umask = umask 0133;
    if (open $num_checks_fhw, '>', $num_checks_file) {
	print $num_checks_fhw "$num_results";
	close $num_checks_fhw;
    }
    else {
	my $os_error = "$!";
	$os_error .= " ($^E)" if "$^E" ne "$!";
	$logger->error("ERROR:  Failed to open file to write the number of checks processed ($num_checks_file):  $os_error.");
	$rc = 0;
    }
    umask $old_umask;

    return $rc;
}

sub find_my_hostname {
    my $command = shift;

    my $hostname = qx($command);
    $hostname = '' if not defined $hostname;
    if ($?) {
	chomp $hostname;
	$logger->error( "ERROR:  Command to find hostname (\"$command\") failed with " . wait_status_message($?) . "; output was:\n$hostname" );
	$hostname = '';
    }
    else {
	if ( $hostname =~ /\n./s ) {
	    $logger->error("ERROR:  Command to find hostname (\"$command\") produced unexpected multi-line output:\n$hostname");
	}
	$hostname =~ s/[\r\n].*//s;

	# Sanitize to look something like a hostname, in case the command produced bad output.
	$hostname =~ tr/-a-zA-Z0-9._//cd;
    }
    if ( $hostname eq '' ) {
	$hostname = $g_opt{H};
	$logger->error("ERROR:  Command to find hostname (\"$command\") produced no useful output.");
	$logger->error("        Using \"$hostname\" instead.");
    }

    return $hostname;
}

# See the Config(3pm) man page for details of this magic formulation.
sub system_signal_name {
    my $signal_number = shift;
    my %sig_num;
    my @sig_name;

    unless ( $Config{sig_name} && $Config{sig_num} ) {
	return undef;
    }

    my @names = split ' ', $Config{sig_name};
    @sig_num{@names} = split ' ', $Config{sig_num};
    foreach (@names) {
	$sig_name[ $sig_num{$_} ] ||= $_;
    }

    return $sig_name[$signal_number] || undef;
}

# Note:  The decomposed wait status reported here may be a bit surprising under
# certain circumstances having to do with the way the child process handles signals.
# In particular, for instance, if you are running a script using "#!/bin/bash -e"
# (which is strongly recommended, so your script doesn't just try to continue running
# when component commands have aborted), and the shell also traps the SIGTERM signal,
# then when the parent process sends SIGTERM to the script's process group,
# a subsidiary process will be generally killed by the SIGTERM, and the shell will
# exit "normally" (not at the hand of the signal) and report the exit status of that
# subsidiary process as its own exit status, so the shell which is our immediate
# child will not be reported as having been itself killed by the signal.

sub wait_status_message {
    my $wait_status   = shift;
    my $exit_status   = $wait_status >> 8;
    my $signal_number = $wait_status & 0x7F;
    my $dumped_core   = $wait_status & 0x80;
    my $signal_name   = system_signal_name($signal_number) || "$signal_number is unknown";
    my $message = "exit status $exit_status" . ( $signal_number ? " (signal $signal_name)" : '' ) . ( $dumped_core ? ' (with core dump)' : '' );
    return $message;
}

################################################################################
#
#   handle_cmd_line()
#
#   Handles the command line options to the program.
#   Sets global flags as per the options specified.
#
################################################################################
sub handle_cmd_line
{
    my $version = get_version(1);
    my $helpstring = "
An assistant to the GDMA poller agent, version $version.
Executes plugins for a hostname passed.  Dumps the results to the spool file.

Options:
-H <HOSTNAME>       Host for which to execute the checks.
-f                  Host is a foreign host (not the same machine as this one).
-T <Timeout>        Plugin timeout in seconds.
-p <Install path>   Installation base directory for the software.
-s <Service Name>   Poller servicename string.  The plugin timeout message will be spooled under this service.
-l                  Enable local logging.
-D <Logging Dir>    Directory where log files will be created.
-d <DEBUGLEVEL>     Debug mode.  Will log additional messages to the log file;
		    <DEBUGLEVEL> should be 1 for moderate logging, or 2 for additional detail.
-L <plugin dir>     Directory where plugins are installed.
-h                  Display help message.
-i                  Run interactively - shows output to the Command Line Interface
		    (CLI; used in non service mode) as well as to the log file.
-v                  Show version.

Copyright 2003-2018 GroundWork Open Source, Inc.
http://www.groundworkopensource.com
Unless required by applicable law or agreed to in writing, software distributed under the License
is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
or implied.  See the License for the specific language governing permissions and limitations under
the License.
";

    getopts("H:fT:p:s:lD:d:L:hiv",\%g_opt);
    if ($g_opt{h}) { print $helpstring;               exit; }
    if ($g_opt{v}) { print "GDMA version $version\n"; exit; }
    if (not $g_opt{H}) {die "Hostname not specified\n";}
    if (not $g_opt{T}) {die "Plugin timeout not specified\n";}
    if (not $g_opt{p}) {die "Installation base directory not specified\n";}
    if (not $g_opt{s}) {die "Poller service name not specified\n";}
    if (not $g_opt{L}) {die "Plugin Directory not specified\n";}

    if ($g_opt{l}) {
	# If logging is enabled, we need to know the logging dir.
	if (not $g_opt{D}) {
	    die "Logging directory not specified.\n";
	}
    }
    if ( $g_opt{d} ) {
	if ( $g_opt{d} !~ /^(\d+)$/ || ( $g_opt{d} != 1 && $g_opt{d} != 2 ) ) {
	    die "$g_opt{d} is not a supported debug level.  Choose 1 or 2.\n";
	}
    }
}

################################################################################
#
#   get_version()
#
#   Returns the version number of the run checks program.
#
#   Arguments:
#   $full - If true, include detail.
#
################################################################################
sub get_version {
    my $full    = shift;
    my $version = $VERSION;
    if ( $full && defined $PerlApp::VERSION ) {
	my $compile_time = PerlApp::get_bound_file('compile_time');
	$version .= " ($compile_time)" if $compile_time;
    }
    return $version;
}

################################################################################
#
#   set_environment()
#
#   Set up environment variables based on platform information passed.
#
#   Arguments:
#   $my_headpath - Head path for the agent.
#
################################################################################
sub set_environment {
    my $my_headpath = shift;
    my $my_osname   = $^O;
}

################################################################################
#
#   run_check()
#
#   Performs the check name passed, by executing the plugin.
#   Pushes the result/execution error, if any, into the result buffer.
#   Inserts an information message if the plugin times out.
#
#   Arguments:
#   $check_name - Check to be executed.
#   $result_buf - A reference to a buffer containing results.
#
################################################################################
sub run_check
{
    my ($check_name, $result_buf) = @_;
    ## Whether or not to spool the result.
    my $nospool = 0;
    ## "0" implies that the result is to be sent to all the targets.
    my $default_target = 0;
    my $ret;
    my $errstr;
    ## A flag that indicates if the plugin timed out.
    my $timedout;
    my $plugin_timeout;
    ## Plugin exit code mapping.
    my $hostname = $g_opt{H};
    my $osname = $^O;
    my $statusfile;
    my $iterations_till_next_execution;

    # Get the path for check status file.
    # The status file contains 2 fields for each check -
    # $check_name:$iterations_till_next_execution:$check_interval
    # The check will be executed only if iterations till next execution are
    # found zero.
    $statusfile = get_status_filepath();

    # Check if special check.
    if (($check_name =~ /Check_Request_Servlet/) or ($check_name =~ /Check_SAR/))
    {
	## These plugins send the result themselves, so don't spool.
	$logger->debug("DEBUG:  $check_name sends result on its own.  Don't spool.");
	$nospool = 1;
    }

    # Execute all the checks that match.
    foreach (my $i=1; $i<=$#{$g_config{$check_name}}; $i++)
    {
	my $exit = undef;
	my $text = "";
	my $check_interval;
	if ($g_config{$check_name}->[$i]->{Enable} ne "OFF")
	{
	    ## The check interval is in terms of number of steps of
	    ## Poller_Proc_Interval.  If a service specific step size
	    ## is defined, use it.  Otherwise, use value "1" i.e., run
	    ## the check once for each Poller_Proc_Interval.
	    $check_interval = defined $g_config{$check_name}->[$i]->{Check_Interval} ?
				      $g_config{$check_name}->[$i]->{Check_Interval} :
				      1;

	    # Should we execute this check now?
	    if (if_run_check_now($check_name, $statusfile, $check_interval, \$iterations_till_next_execution)) {
		## Yes we should.  Update the status file to indicate that we
		## are about to execute this check.  The status line for each
		## check contains 2 fields -
		## iterations till next check:configured check interval
		##
		## Initialize the "iterations till next execution" field to
		## (check_interval - 1).  Decrement the field for each iteration
		## that we do not execute the check for.  The check will be
		## executed when the "iterations till next execution" value is 0.
		## For example, say Check_Interval is 5.  "iterations till next
		## execution" will be initialized to 4.  In the subsequent
		## iterations the value will be decremented -- 4, 3, 2, 1, 0.
		if ( not update_check_status( $check_name, $statusfile, ${ \( $check_interval - 1 ) }, $check_interval ) ) {
		    $logger->error("ERROR:  Failed to update status file.  This may result in checks not being");
		    $logger->error("        executed, or executed without regard to the configured check interval.");
		}

		$logger->debug("DEBUG:  Executing '$check_name' - iteration $i");
		my $execute_string = $g_config{$check_name}->[$i]->{Command};

		# The config file command definitions may use macros like
		# "Plugin_Directory" and "Monitor_Host".  For example:
		#     c:\..\check_cpu_load_percentage.vbs -h $Monitor_Host$ -inst _Total -t 97,99
		# Replace the macros with their configured or derived values before trying to execute.
		$execute_string =~ s/\$Plugin_Directory\$/$g_opt{L}/g;
		$execute_string =~ s/\$Monitor_Host\$/$g_config{Monitor_Host}/g;

		foreach my $option (keys %{$g_config{$check_name}->[$i]}) {
		    if (defined($g_config{$check_name}->[$i]->{$option})) {
			$logger->trace( $check_name . "[$i]_$option = " . $g_config{$check_name}->[$i]->{$option} );
			if ($option =~ /^Parm_(--.*)/) {
			    $execute_string .= " $1=";
			    $execute_string .= $g_config{$check_name}->[$i]->{$option};
			}
			elsif ($option =~ /^Parm_(-.*)/) {
			    $execute_string .= " $1 ";
			    $execute_string .= $g_config{$check_name}->[$i]->{$option};
			}
		    }
		    else {
			if ($option =~ /^Parm_(--.*)/) {
			    $execute_string .= " $1";
			}
			elsif ($option =~ /^Parm_(-.*)/) {
			    $execute_string .= " $1";
			}
		    }
		}

		# Execute the command and read the result.
		$logging->log_message("TRACE:  PLUGIN COMMAND STRING:\n$execute_string");
		# If the timeout is defined for this check use it.
		# Otherwise, use the default one.
		$plugin_timeout = (defined($g_config{$check_name}->[$i]->{Timeout})) ?
				  $g_config{$check_name}->[$i]->{Timeout} :
				  $g_opt{T};

		# Execute the command by spawning a new process.
		# This will be handled differently on different platforms.
		if (($osname eq 'linux') or ($osname eq 'solaris') or ($osname eq 'aix') or ($osname eq 'hpux'))
		{
		    $ret = exec_plugincmd_unix($execute_string, $plugin_timeout, \$exit, \$text, \$errstr, \$timedout);
		}
		elsif (($osname eq 'MSWin32'))
		{
		    $ret = exec_plugincmd_windows($execute_string, $plugin_timeout, \$exit, \$text, \$errstr, \$timedout);
		}
		if (!$ret)
		{
		    ## We could not execute the plugin; spool an error message.
		    ## We will just insert the message into the result buffer --
		    ## we know that it will be spooled along with other results.
		    ## We want a different error message for plugin timeout.
		    if ($timedout)
		    {
			$logging->log_message("ERROR:  Timed-out plugin:\n$execute_string");
			insert_plugin_timeout_message($execute_string, $plugin_timeout, $result_buf);
		    }
		    else
		    {
			$logging->log_message("ERROR:  Failed plugin:\n$execute_string");
			insert_plugin_error_message($g_config{$check_name}->[$i]->{Service}, $execute_string, $errstr, $result_buf);
		    }
		}
		else
		{
		    ## Plugin execution successful.
		    $logger->debug("PLUGIN RESULTS:  exit=$exit; text=$text");
		    if (!$nospool) {
			insert_into_result_buf($default_target, $hostname, $g_config{$check_name}->[$i]->{Service}, $exit, $text, $result_buf);
		    }
		}
	    }
	    else
	    {
		## We are not supposed to run the check now.
		## Decrement the iterations_till_next_execution field.
		$iterations_till_next_execution--;
		if ( not update_check_status( $check_name, $statusfile, $iterations_till_next_execution, $check_interval ) ) {
		    $logger->error("ERROR:  Failed to update status file.  This may result in checks not being");
		    $logger->error("        executed or executed without regard to the configured check interval.");
		}
	    }
	}
    }
}

################################################################################
#
#   get_status_filepath()
#
#   Returns the status file path based on the operating system on which this
#   program is running.  The path is returned by value.
#
################################################################################
sub get_status_filepath
{
    my $statusfile = "Null";
    my $osname = $^O;
    my $hostname = $g_opt{H};

    if (($osname eq 'linux') or ($osname eq 'solaris') or ($osname eq 'aix') or ($osname eq 'hpux'))
    {
	$statusfile = "$g_opt{p}/status/gdma_$hostname.status";
    }
    elsif (($osname eq 'MSWin32'))
    {
	$statusfile = "$g_opt{p}\\status\\gdma_$hostname.status";
    }

    return $statusfile;

}

################################################################################
#
#   if_run_check_now()
#
#   Checks if its time to run a check by looking in the gdma check status file
#   for the no. of iterations till next execution of this check.
#   Returns 1 for yes go run it now, 0 for don't run it now.
#
#   Arguments:
#   $checkname - The check name for which we will return the status.
#   $gdmastatusfile - A file containing iterations till next execution for all
#                     the checks.
#   $current_check_interval - Presently configured check interval for
#                             $checkname.  The check will be executed once per
#                             $current_check_interval poller iterations.
#   $iterations_till_next_execution - A reference to a variable where
#                                     iterations till next execution for this
#                                     check is to be recorded.
#
################################################################################
sub if_run_check_now
{
    my ( $checkname, $gdmastatfile, $current_check_interval, $iterations_till_next_execution ) = @_;

    # Configured interval in the status file.
    my $last_configured_interval;

    if ( !-e $gdmastatfile ) {
	## The stat file doesn't exist yet.  So run check assuming that no check
	## run yet.  Also accounts for the case when statfile is accidentally removed.
	$logger->debug("DEBUG:  GDMA status file $gdmastatfile does not exist - assuming first run.");
	return 1;
    }

    if ( !-r $gdmastatfile ) {
	## The stat file isn't readable; don't run the check.
	$logger->error("ERROR:  GDMA status file $gdmastatfile is not readable -- the check will not be run.");
	return 0;
    }

    # Get the status details from stats file.
    # In the case of the check not having run before, the iterations till next
    # execution and last configured interval will be set to "hasnotrun"
    # by get_last_check_details(), in which case run it now.
    if ( not get_last_check_details( $checkname, $gdmastatfile, $iterations_till_next_execution, \$last_configured_interval ) ) {
	$logger->error("ERROR:  Failed to get GDMA status for $checkname");
	return 0;
    }

    if ( $$iterations_till_next_execution eq "hasnotrun" ) {
	$logger->notice("NOTICE:  Check $checkname has not been run before -- will run it now.");
	return 1;
    }

    # If the last configured interval for this check is smaller than the
    # interval in the cfg, run it now.
    if ( $current_check_interval < $last_configured_interval ) {
	$logger->debug("DEBUG:  Last run check interval ($last_configured_interval) is greater than the incoming");
	$logger->debug("        configured check interval ($current_check_interval) -- the check will run now.");
	return 1;
    }

    # If number of iterations till next execution is "0", it's time to run it.
    if ( $$iterations_till_next_execution == 0 ) {
	$logger->debug( "DEBUG:  Check $checkname ran $last_configured_interval "
	      . ( $last_configured_interval == 1 ? 'iteration' : 'iterations' )
	      . " ago; the check will run now." );
	return 1;
    }
    else {
	## Don't run the check just yet.
	$logger->debug( "DEBUG:  $$iterations_till_next_execution "
	      . ( $$iterations_till_next_execution == 1 ? 'iteration' : 'iterations' )
	      . " until we run $checkname again." );
	return 0;
    }
}

################################################################################
#
#  get_last_check_details()
#
#  Reads the status file and gets the details for a particular check.
#  iterations_till_next_execution and last_configured_interval fields are
#  returned by reference.  Returns 1 on success and 0 on error.
#  Arguments:
#  $checkname - The check name for which we will return the status.
#  $gdmastatusfile - A file containing iterations till next execution for all
#                    the checks.
#  $iterations_till_next_execution - A reference to a variable where
#                                    iterations till next execution for this
#                                    check is to be recorded.
#  $last_configured_interval - A reference.  The check interval in the status
#                              file will be recorded here.
#
################################################################################
sub get_last_check_details
{
    my ($checkname, $gdmastatfile, $iterations_till_next_execution, $last_configured_interval) = @_;

    # Status file handle.
    my $statfh;
    # Buffer to store all the lines in status file.
    my @statfile;
    my $statline;
    # Fields read from status file.
    my ($statcheckname, $statiterations_till_next_exe, $stat_config_interval);

    $statfh = new IO::Handle;
    if (not open($statfh, '<', $gdmastatfile))
    {
	$logger->error("ERROR:  Failed to open the status file ($gdmastatfile) for reading:  $!");
	return 0;
    }
    # Suck in all the lines.
    @statfile = <$statfh>;
    # Done with status file handle.
    if (not close $statfh)
    {
	$logger->error("ERROR:  Failed to close the status file ($gdmastatfile):  $!");
	return 0;
    }

    # Assume that the check has not yet run, to begin with.
    $$iterations_till_next_execution = $$last_configured_interval= "hasnotrun";

    foreach $statline (@statfile)
    {
	## Format is check name:Iterations till next execution:last configured check interval.
	chomp $statline;
	( $statcheckname, $statiterations_till_next_exe, $stat_config_interval ) = ( split( /:/, $statline ) );

	if ( $statcheckname eq $checkname ) {
	    $$iterations_till_next_execution = $statiterations_till_next_exe;
	    $$last_configured_interval       = $stat_config_interval;
	    last;
	}
    }

    return 1;
}

################################################################################
#
#   update_check_status()
#
#   Updates the iterations_till_next_execution, check_interval for a check in
#   the status file.  Returns 1 on success and 0 on error.
#
#   Arguments:
#   $checkname - The check name for which to update the status.
#   $gdmastatusfile - A file containing iterations till next execution for all
#                     the checks.
#   $iterations_till_next_execution - New value of iterations till next
#                                     execution for this check.
#   $check_interval - New value of check interval for this check.
#
################################################################################
sub update_check_status {
    my ( $checkname, $gdmastatfile, $iterations_till_next_execution, $check_interval ) = @_;

    # Status file handle.
    my $statfh;

    # Buffer to hold the status file lines.
    my @statusfile;

    # Status file fields.
    my ( $statcheckname, $statiterations_till_next_exe, $stat_config_interval );
    my $checkfound;
    my $updatedstat;
    my $rc = 1;

    # Open the status file for update
    $statfh = new IO::Handle;

    # Create a new row to be used for updating
    $updatedstat = "$checkname:$iterations_till_next_execution:$check_interval\n";

    # If the status file does not exist, no check is run yet.
    # Simply add the status line to the file.
    if ( !-e $gdmastatfile ) {
	## Create the file.
	my $old_umask = umask 0133;
	if ( not open( $statfh, '>', $gdmastatfile ) ) {
	    $rc = 0;
	    $logger->error("ERROR:  Failed to open the status file ($gdmastatfile) for update:  $!");
	    umask $old_umask;
	    goto end;
	}
	print $statfh "$updatedstat";
	close($statfh);
	umask $old_umask;
	## We are done.
	goto end;
    }

    # First read the entire file into an array.  Update the status in the array.
    # Then write the array back to the status file.
    if ( not open( $statfh, '+<', $gdmastatfile ) ) {
	$rc = 0;
	$logger->error("ERROR:  Failed to open the status file ($gdmastatfile) for update:  $!");
	goto end;
    }

    # Suck in the status file.
    @statusfile = <$statfh>;

    # Whether check was found in the status file.
    $checkfound = 0;
    foreach ( my $i = 0 ; $i <= $#statusfile ; $i++ ) {
	( $statcheckname, $statiterations_till_next_exe, $stat_config_interval ) = ( split( /:/, $statusfile[$i] ) );
	if ( $checkname eq $statcheckname ) {
	    $checkfound = 1;
	    $statusfile[$i] = "$updatedstat";
	}
    }

    # If there was no stat info for this check, add it now
    if ( $checkfound == 0 ) {
	push( @statusfile, "$updatedstat" );
    }

    # Write the updated status back to the status file.
    # We will do this by writing the updated status in
    # @statusfile array to the status file.
    if ( not seek( $statfh, 0, 0 ) ) {
	$rc = 0;
	$logger->error("ERROR:  Failed to seek status file handle:  $!");
	goto end;
    }
    if ( not print $statfh @statusfile ) {
	$rc = 0;
	$logger->error("ERROR:  Failed to write to the status file:  $!");
	goto end;
    }

    # There is a possibility that the contents of the file
    # were bigger before we wrote to it.  So clear everything
    # in the file, after the location where we finished
    # writing the @statusfile.  It is stale data.
    if ( not truncate( $statfh, tell($statfh) ) ) {
	$rc = 0;
	$logger->error("ERROR:  Failed to update the status file:  $!");
	goto end;
    }
    close($statfh);

  end:
    return $rc;
}

################################################################################
#
#   exec_plugincmd_unix()
#
#   Spawns a new process to invoke the plugin command passed, on linux
#   or solaris.  Makes sure that plugin terminates within the configured
#   timeout, by killing the plugin process, if it takes too long.
#   Records the plugin output and exit code.  Returns 1 if the plugin is
#   executed successfully; 0 otherwise.  A 0 return value indicates that
#   the $exit and $out output variable values should be ignored.
#
#   Arguments:
#   $exec_string - Plugin command to be executed.
#   $plugin_timeout - Timeout for plugin command.
#   $exit - A reference.  The plugin exit value will be recorded here.
#   $out - A reference to the buffer that should hold plugin output.
#   $errstr - A reference.  The plugin execution error (0 return), if any,
#             will be recorded here.
#   $timedout - A reference.  Set to 1, if the plugin times out.
#
################################################################################

my $signal_name = undef;

sub catch_signal {
    my $signame = shift;
    $signal_name = 'SIG' . $signame;
    die "Caught a $signal_name signal!\n";
}

sub exec_plugincmd_unix {
    my ($exec_string, $plugin_timeout, $exit, $out, $errstr, $timedout) = @_;
    my $kidpid;
    my $ret = 1;
    ## Get the root path for installation.
    my $head_path = $g_opt{p};
    my $hostname = $g_opt{H};
    ## Note:  If we ever want to generalize this process to run checks for a single host in parallel,
    ## we had better generalize this pathname to include some uniqueness entropy for each check.
    my $outfile = "$head_path/tmp/${hostname}_out.txt";

    # Flush the output buffer.
    $$out = "";
    $$timedout = 0;

    $logger->debug("DEBUG:  Executing plugin command on $^O");

    my $oldblockset = POSIX::SigSet->new;
    my $newblockset = POSIX::SigSet->new(SIGCHLD);
    # FIX MINOR:  die() won't log; address that
    sigprocmask(SIG_BLOCK, $newblockset, $oldblockset) or die "FATAL:  Could not block SIGCHLD ($!),";

    # In pursuit of run-time efficiency, we avoid spawning extra intermediate child processes here.  That
    # would essentially be a waste of resources, unless doing so somehow makes it easier to impose a timeout
    # on the plugin command, or makes it easier to clean up any resources in the parent or plugin process when
    # a timeout occurs.  But our present construction is already adequate in those regards.  Also, having an
    # extra intermediate child process would mean we would have to write the plugin output to a file so we can
    # communicate it back to the parent.  Using an external file for this simple communication is pointlessly
    # expensive.

    # FIX TODAY:  We force the child process to be its own process group leader, which we must do to
    # guarantee that we can reach all of its descendants (and not any other sibling daemon processes) should
    # we need to kill them all if the plugin execution times out.  But be aware of how this affects the
    # assumptions made by this script's own parent process, that all its own descendants would remain in a
    # single controlled process group that would be reachable by a SIGTERM or SIGINT sent to that process
    # group.  So we might also need to intercept a possible SIGTERM or SIGINT from our own parent process,
    # and translate that into a SIGTERM or SIGINT sent to our own child process group.

    # FIX MINOR:  Perhaps this gdma_run_checks.pl script, which runs all the checks for a single host, ought
    # to be its own process group leader, so it can just kill its own process group (while blocking that
    # signal from itself) if it receives a termination signal (or if a particular plugin takes too long to
    # run).  The parent process (the poller) would then be responsible for forwarding any SIGTERM or SIGINT it
    # receives to all the gdma_run_checks.pl process groups which are currently outstanding when the poller
    # itself receives a SIGTERM or SIGINT.  That would mean we wouldn't need to have the child process here be
    # its own process group leader for a process group that can contain any additional processes the plugin
    # itself might spawn.  (We want whatever process group contains the plugin to not be the same as the
    # process group that contains the parent poller, so we can safely signal the plugin process group if the
    # plugin takes too long to run, without endangering the poller.)

    # Spawn a new process to execute the plugin command.
    if ( !defined( $kidpid = open(FROM_CHILD, '-|') ) ) {
	# Restore the old signal mask so we can reap zombies once again.
	# FIX MINOR:  die() won't log; address that
	sigprocmask(SIG_SETMASK, $oldblockset) or die "FATAL:  Could not restore SIGCHLD signal ($!),";

	## Report plugin execution error.
	$$errstr = "Could not fork a new process for plugin command:  $!";
	$logger->error("ERROR:  $$errstr");
	return 0;
    }
    elsif ($kidpid == 0) {
	## Child.

	# Make the child its own process group leader, so we can easily kill a whole tree of descendants.
	# Note that this call may fail because the parent has already set this (see below), but that's okay.
	# FIX MINOR:  The fact that we're making the child its own process group leader means it won't
	# receive any SIGTERM or SIGINT that is sent to the process group of the parent spooler process,
	# when GDMA as a whole is requested to terminate.  That means we should have in place SIGTERM and
	# SIGINT handlers in the parent process (not here in the child process) that will forward the
	# incoming signal to this child's process group, taking care of any race condition regarding
	# setting the child process to be a process group leader (i.e., blocking SIGTERM and SIGINT
	# in the parent process surrounding the creation of the child, recording of its PID, and making
	# it a process group leader).
	if ( ! POSIX::setpgid(0, 0) ) {
	    $logger->debug( "NOTICE:  setpgid() in child process failed ($!)") if not $g_opt{i};
	}

	# Restore the old signal mask so we can reap zombies once again.
	# FIX MINOR:  die() won't log; address that
	sigprocmask(SIG_SETMASK, $oldblockset) or die "FATAL:  Could not restore SIGCHLD signal ($!),";

	## With interactive-mode execution of this script directing log messages to the standard output
	## stream, this message is now seen in those conditions to interfere with our interpretation of
	## the plugin results in the logged data, so we suppress logging this info.  (Besides, it's not
	## at all clear that we want to be using the logger concurrently from within the child process
	## except in extreme circumstances.)  If you really want this info, run under a high debug level
	## and look for the "PLUGIN COMMAND STRING" line in the logged output from the parent process.
	## Or run manually, but not in interactive mode, and look at the logfile instead of output in the
	## terminal window.
	$logger->debug("DEBUG:  Executing command ($exec_string)") if not $g_opt{i};

	## FIX LATER:  This plugin invocation may or may not invoke an intermediate shell.
	## Possibly, we ought to try to re-jigger this to invoke the plugin directly.
	## FIX LATER:  Perhaps we ought to ensure that the STDERR stream from this command
	## is somehow captured by the parent, so it can be logged upon failure, in case the
	## interesting failure data appears on STDERR instead of STDOUT.
	do { exec($exec_string); };

	# Capture the status so this particular value doesn't disappear before we use it for the last time.
	my $os_error = "$!";

	# exec() should never return.  But just in case ...
	$logger->error("ERROR:  Failed to exec plugin program:  $os_error");

	# Print this failure message so it ends up in the plugin output file.
	print STDOUT "Failed to invoke command:  $os_error\n";

	# Terminate.  Use POSIX::_exit rather than exit().
	# It is safer on unices when exiting from child processes.
	POSIX::_exit 1;
    }
    else {
	## Parent.

	# Avoid potential race conditions in the parent process (this branch) by setting the process group
	# of the child here to make it a process group leader, even though it will also be set in the child.
	# One of these two calls (the setpgid() in the child, above, or this setpgid() in the parent) will
	# fail, but that won't matter.  See APITUE2/e, page 270 for the rationale.
	#
	# A POSIX::EACCES failure is okay (the child did the dirty work first).
	if (! POSIX::setpgid($kidpid, 0) && $! != POSIX::EACCES) {
	    # ESRCH (pid is not the current process and not a child of the current process) is the bad error
	    # code we dread here, but that's why we call sigprocmask() around this call (to prevent the child
	    # process from possibly being reaped and its PID being re-used before we can reference it here).
	    # We usually see EACCES (an attempt was made to change the process group ID of one of the children
	    # of the calling process and the child had already performed an execve()), but that should not
	    # be considered an error in the present context.  (The child's own setpgid() call should have
	    # occurred before such an execve() call, so it ought to be safe.)
	    $logger->error("ERROR:  setpgid() in parent of child failed ($!)");
	}

	## Check if the kid finishes within the timeout.  Kill it if it does not.

	my @child_stdout = ();

	$signal_name = undef;

	# We need to make sure that the plugin execution does not take more than
	# $plugin_timeout seconds.  Set up an alarm that will break us out of our
	# torpor if that much time elapses before we see the child terminate.
	do {
	    local $SIG{ALRM} = \&catch_signal;
	    eval {
		alarm($plugin_timeout);
		eval {
		    @child_stdout = <FROM_CHILD>;
		};
		alarm(0);
		if ($@) {
		    chomp $@;
		    die "$@\n";
		}
	    };
	    if ($@) {
		# Is it an error that we threw?  If not, complain.
		if ($@ !~ /Caught a SIGALRM signal!/) {
		    chomp $@;
		    $logger->error("ERROR:  Unexpected error while waiting for plugin to complete:  $@");
		}
	    }
	};
	$logger->debug("DEBUG:  Wait for child process completed at " . (scalar localtime) . ".");

	# Check if the kid terminated on its own before the timeout expired.
	if ( defined($signal_name) && $signal_name eq 'SIGALRM' ) {
	    ## We timed out.
	    ##
	    ## We must kill our kid and grandchildren, if any, so they don't continue to hang around after
	    ## the timeout has expired.  The child process that we fork()ed should have in turn spawned at
	    ## least one new descendant process, namely the plugin.  We need to make sure that all such
	    ## descendants are cleaned up.  So we will send SIGTERM to the entire child process group.
	    ## This has to be done before we close the FROM_CHILD filehandle, because the close() will
	    ## wait for the child process to be gone before it returns.
	    kill TERM => -$kidpid;
	    ## Report a plugin timeout error.
	    $logger->error("ERROR:  Plugin process took too long.  Throttled.");
	    $$timedout = 1;
	    $ret       = 0;
	}

	# Restore the old signal mask so we can reap zombies once again.
	# FIX MINOR:  die() won't log; address that
	sigprocmask(SIG_SETMASK, $oldblockset) or die "FATAL:  Could not restore SIGCHLD signal ($!),";

	# This close() call will internally perform the required waitpid() call to clean up the <defunct>
	# process, whether it ran to completion on its own or was killed by our action above, so calling
	# the waitpid() routine ourselves above would have been counterproductive.
	# FIX MINOR:  Theoretically, the child process could close its STDOUT stream, getting us out of
	# the alarm() region above, then take a long time to finally exit().  That additional wait would
	# be borne here, but without benefit of a timeout and the possibility of sending a SIGTERM if the
	# wait takes too long.  The code here ought to be rejiggered to account for that possibility
	# (which is unlikely in the case of most plugins, but could be an issue in the general case).
	if ( not close FROM_CHILD ) {
	    if ($!) {
		## Something went wrong on our side of the connection (the pipe).
		$logger->error("ERROR:  Close on child process failed:  $!");
		## Return an error.
		$ret = 0;
	    }
	    elsif ($logger) {
		## Something went wrong on the child process side of the connection (likely, the plugin
		## itself exited with a non-zero exit status, or the plugin could not be exec()ed and our
		## child process then directly exited with a non-zero exit status).  Either way, if our
		## child process terminated with a non-zero exit status, $! will have been forced to 0
		## (leading us into this branch), and $? will now be the child process wait status.  We
		## can at least capture and record that here.
		## FIX LATER:  If we see strange stuff here that cannot be easily explained, we might want
		## to capture and log the child's STDERR stream as well, if we can do so independently
		## of the child's STDOUT stream so it doesn't interfere with normal operation of the
		## plugins we run from this script.
		$logger->error( "ERROR:  Child process failed with " . wait_status_message($?) . "; command was:" );
		$logging->log_message("    $exec_string");
		$logging->log_message( 'which produced' . ( @child_stdout ? ':' : ' no output.' ) );
		if (@child_stdout) {
		    my $stdout_string = '    ' . join( '    ', @child_stdout );
		    chomp $stdout_string;
		    $logging->log_message($stdout_string);
		}
		$logging->log_message("This can happen if the command exits with non-zero status (e.g., to signal a warning or critical condition).");
		## The plugin (and thus our own child process) should exit with "0" if everything goes
		## well, and with a non-zero exit status otherwise.  But all we care about here is that
		## the plugin probably executed, so this looks like success from the perspective of the
		## calling function, and we never reset $ret here.  We'll look later at the plugin or
		## child process output to see if it should be reset on that basis.
	    }
	}
	else {
	    ## A successful run.
	}

	if (not $$timedout) {
	    ## The kid terminated on its own.  Record the exit code.
	    ## The upper byte of the status contains the exit status of the process.
	    $$exit = $? >> 8;
	    $logger->trace("TRACE:  Plugin process terminated with code $$exit.");

	    # FIX MINOR:  The use of an external file here is due to the continued use of historical
	    # code which has not yet been improved.  Re-jigger the read_and_process_plugin_output()
	    # routine to accept the @child_stdout data directly, instead of writing it to a file
	    # and then immediately reading it back and deleting the file.  That's just ridiculously
	    # inefficient.  Bear in mind that the Windows version of running a plugin still needs
	    # the external file, at least in its present incarnation.

	    # If we do eliminate the call to read_and_process_plugin_output() here, then we want to
	    # retain its secondary behavior of logging the plugin output under debug mode, as that
	    # can be very helpful in diagnosing problems in the field.

	    # Write the output/execution error to the output file.
	    my $outhandle;
	    my $old_umask = umask 0133;
	    if (not open $outhandle, '>', $outfile) {
		$logger->error("ERROR:  Could not open outfile \"$outfile\" ($!).");
	    }
	    else {
		print $outhandle @child_stdout if @child_stdout;
		close $outhandle or $logger->error("ERROR:  close of outhandle failed:  $!");
	    }
	    umask $old_umask;

	    my $plugin_error = 0;
	    ## Read plugin output from outfile.
	    read_and_process_plugin_output($outfile, $out, $errstr, \$plugin_error);
	    ## Return a plugin execution error, if necessary.  The corresponding
	    ## error string will have been set by read_and_process_plugin_output().
	    $ret = 0 if $plugin_error;

	    if (!unlink($outfile)) {
		## Report the error.  Not catastrophic though.
		$logger->error("ERROR:  exec_plugincmd_unix:  failed to clean-up the temporary plugin outfile ($outfile):  $!");
	    }
	}
    }

    return $ret;
}

################################################################################
#
#   read_and_process_plugin_output()
#
#   Reads plugin output from the disk file passed and conditions it.  Stores
#   the conditioned plugin output into a variable passed.  Also, records the
#   plugin execution errors.
#
#   $filename - A filename containing the plugin output.
#   $out - An output variable where the plugin output will be stored.
#   $errstr - A reference.  Execution error, if any, will be written to this
#             variable.
#   $plugin_error - A reference.  This will be set to 1 if there was plugin
#                   execution error.
#
################################################################################
sub read_and_process_plugin_output
{
    my ($filename, $out, $errstr, $plugin_error) = @_;
    # Plugin output file handle.
    my $pluginh;

    # Open the plugin output file for reading.
    if (!open($pluginh, '<', $filename))
    {
	## Report plugin execution error.
	$$errstr = "Failed to open temporary plugin outfile ($filename):  $!";
	$$errstr .= " ($^E)" if "$^E" ne "$!";
	$logger->error("ERROR:  read_and_process_plugin_output:  $$errstr");
	$$plugin_error = 1;
    }
    else
    {
	## Read the output from the plugin command.
	my @lines = <$pluginh>;
	chomp @lines;
	## Join multi-line output with the "/" character.
	$$out = join('/', @lines);

	# Parse plugin output to find execution error.
	# The plugin exitcode can be other than zero in cases of warning or
	# critical and cannot be used to detect plugin execution error.
	# For such error conditions linux/solaris/aix/hpux plugin output will
	# contain "Failed to invoke command" and for windows "Input Error".
	# On windows cscript.exe invokes plugins, even if plugin script does
	# not exist, cscript.exe is invoked anyways and so we can't detect
	# plugin execution error when invoking the plugin.
	if (!defined($$out) || length($$out) == 0)
	{
	    ## Could not read the plugin output.
	    $$errstr = "Failed to read the plugin output";
	    $logger->error("ERROR:  read_and_process_plugin_output:  $$errstr");
	    $$plugin_error = 1;
	}
	elsif (($$out =~ /Failed to invoke command:/i) or ($$out =~ /Input Error:/i))
	{
	    ## Report the plugin execution error.
	    $$errstr = $$out;
	    $logger->error("ERROR:  read_and_process_plugin_output:  $$errstr");
	    $$plugin_error = 1;
	}
	else
	{
	    if ( $g_opt{d} && $g_opt{d} == 2 ) {
		my $output = $$out;
		chomp $output;
		$logging->log_message("DEBUG:  Raw plugin output was:\n$output");
	    }
	    ## Remove the newlines and tabs.  Suppressing newlines in this fashion effectively
	    ## allows multi-line plugin output to be passed back to the server, although the
	    ## server will then treat it as ordinary single-line output, which is not exactly
	    ## what would happen with multi-line output from a Nagios active plugin execution.
	    ## Also note that this will mightily confuse downstream processing if the plugin
	    ## output contains performance data, followed by additional lines of output.
	    $$out =~ s/\r/\//og;
	    $$out =~ s/\n/\//og;
	    $$out =~ s/\t/\//og;
	    # Replace " with ' (why are we doing this???)
	    $$out =~ s/\"/'/og;
	}
	close $pluginh or $logger->error("ERROR:  Failed to close temporary plugin outfile ($filename):  $!");
    }
}

################################################################################
#
#   exec_plugincmd_windows()
#
#   Spawns a new process to invoke the plugin command passed, on windows.
#   Makes sure that plugin terminates within the configured timeout,
#   by killing the plugin process if it takes too long.
#   Records the plugin output and exit code.  Returns 1 if the plugin is
#   executed successfully; 0 otherwise.  A 0 return value indicates that
#   the $exit and $out output variable values should be ignored.
#
#   Arguments:
#   $exec_string - Plugin command to be executed.
#   $plugin_timeout - Timeout for plugin command.
#   $exit - A reference.  The plugin exit code will be recorded here.
#   $out - A reference to the buffer that should hold plugin output.
#   $errstr - A reference.  The plugin execution error (0 return), if any,
#             will be recorded here.
#   $timedout - A reference.  Set to 1, if the plugin times out.
#
################################################################################
sub exec_plugincmd_windows {
    my ($exec_string, $plugin_timeout, $exit, $out, $errstr, $timedout) = @_;
    my $ret = 0;  # failure is the norm unless proven otherwise
    # Get the root path for installation.
    my $head_path = $g_opt{p};
    my $hostname = $g_opt{H};
    my $outfile = "$head_path\\tmp\\${hostname}_out.txt";
    # Array to hold the processed command fields
    my @cmd_string;
    # loop variables
    my ($i, $j);

    # Initialize the loop variables.
    $i = $j = 0;

    $logger->debug("DEBUG:  Executing plugin command on $^O");
    ## Flush the output buffer.
    $$out = "";
    $$timedout = 0;

    # Create a job handle for executing plugin process.
    my $job = Win32::Job->new;
    if ( not defined $job ) {
	$$errstr = "Cannot create a new Win32::Job object ($^E).";
	$logger->error("ERROR:  Cannot create a new Win32::Job object for \"$exec_string\" ($^E).");
	return 0;
    }

    # The config file contains a command for each service check, perhaps
    # something like:
    #
    # "cscript.exe //nologo //T:60 'c:\Program Files\groundwork\gdma\libexec\v2\check_counter_counter.vbs'
    #     -h $Monitor_Host$ -class Win32_PerfRawData_PerfOS_Memory -inst *"
    #
    # or like:
    #
    # "CMD /C ECHO C:\Progra~2\groundwork\gdma\libexec\v3\getCounterParams.ps1
    #     -object 'Terminal Services' -counter 'Active Sessions' -label terminal_svcs
    #     -warning 2 -critical 5 ; exit $LASTEXITCODE | powershell.exe -command -"
    #
    # Some arguments in the execution string may contain spaces.  Such
    # arguments are enclosed within single quotes in the config file,
    # for example 'C:/Program Files/...'.
    #
    # The execution string needs to be processed to make sure that:
    # 1. The single-quote-enclosed strings are treated as one argument.
    # 2. The single quotes are replaced with duoble quotes, so that
    #    we can execute the command using Win32::Job.

    # First, split the execution string on a space.  The arguments with
    # spaces in them, will be split into different tokens.  But we will
    # have the starting and ending single quotes to identify them by.
    my @tokens = split(/ /, $exec_string);

    # Now, run through the tokens and look for the one that starts with
    # a single quote.  When it is found, go on joining the tokens until
    # the ending single quote is found.  Do this for all the tokens.
    # This will make sure that the arguments which had spaces in them
    # earlier, are treated as single tokens.
    while ( defined( $tokens[$i] ) ) {
	if ( $tokens[$i] =~ /^'/ ) {
	    while ( not( $tokens[$i] =~ /'$/ ) ) {
		## While joining, make sure that the spaces that split() consumed earlier, are restored.
		$cmd_string[$j] .= "$tokens[$i] ";
		## While at it, replace the single quotes with double quotes.
		$cmd_string[$j] =~ s/'/"/;
		$i++;
	    }
	    ## Join the last token with ending single quote in it.
	    $cmd_string[$j] .= $tokens[$i];
	    ## Replace the single quotes with double quotes.
	    ## If an argument without space is enclosed with single quotes
	    ## in the config file, cmd_string[$j] will have two single quotes.
	    ## Hence, substitute globally.
	    $cmd_string[$j] =~ s/'/"/g;
	}
	else {
	    $cmd_string[$j] = $tokens[$i];
	}
	$i++;
	$j++;
    }

    # Join the processed tokens back to get the command to be executed.
    # Also present in the logfile the output file path, in case an "Access is denied"
    # error is due to problems in that regard as opposed to the exec string itself.
    my $processed_exec_string = join (' ', @cmd_string);
    $logger->trace("TRACE:  The processed cmd string is:  $processed_exec_string");
    $logger->trace("TRACE:  with output directed to:  $outfile");

    # We need to pass the program name as a separate initial argument to Win32::Job.
    # To ensure that we use the full program name, including any embedded spaces, we
    # must use the results of the quote-processing above to determine which part of
    # the command line is the program name.  But we must then remove any enclosing
    # quotes, as they would interfere with use of the program name in this context.
    my $program = $cmd_string[0];
    if ($program =~ /^"/) {
	$program =~ s/"//;  # drop the initial quote
	$program =~ s/"//;  # drop the matching quote, presumably at the end of the token
    }

    # We want to read the output and errors from the plugin.
    # Create the plugin process with redirected STDOUT and STDERR.
    my %opts = ( no_window => 1, stdout => $outfile, stderr => $outfile );

    # Spawn the plugin process.
    my $child_pid = $job->spawn( $program, "$processed_exec_string", \%opts );
    if ( not defined $child_pid ) {
	## This is a plugin execution error.
	$$errstr = "Failed to spawn the plugin process for \"$program\" ($^E)";
	$logger->error("ERROR:  exec_plugincmd_windows:  $$errstr");
	return 0;
    }

    # Wait for the plugin process to complete for at most $plugin_timeout seconds.
    # Note that we explicitly wait for ALL processes in the job to finish, not just
    # the arbitrary first one to complete.
    $ret = $job->run( $plugin_timeout, 1 );
    if ($ret) {
	## All processes exited on their own (without timing out and being killed
	## by the Win32::Job object).
	## Record the exit code of the plugin.
	## status() returns a hash with the job PIDs as the keys.
	## Each value in this hash is a subhash with keys "exitcode" and "time".
	## NOTE:  The Win32::Job doc says that status() returns a hash, not a hashref.
	## Let's see how well this assumption of a hashref actually works.
	my $status = $job->status();
	$$exit = $$status{$child_pid}{exitcode};
	$logger->notice("NOTICE:  Child process $child_pid completed with exit code $$exit.");
	my @exit_status = ();
	foreach my $pid ( keys %$status ) {
	    push @exit_status, "$pid => $$status{$pid}{exitcode}";
	}
	$logger->notice( "         All PID => exit status values:  " . join( ', ', @exit_status ) );

	my $plugin_error = 0;
	## Read plugin output from outfile
	read_and_process_plugin_output( $outfile, $out, $errstr, \$plugin_error );

	# Return a plugin execution error, if necessary.  The corresponding
	# error string will have been set by read_and_process_plugin_output().
	$ret = 0 if $plugin_error;
    }
    else {
	## Timeout.  Wipe out all the processes.
	## (Killing the processes should have already been done by the Win32::Job
	## object.  Doing so here should just be a redundant formality.)
	##
	## FIX MINOR:  Hopefully, trying to kill non-existent processes won't
	## cause any hiccups in the system by accidentally shooting some other
	## processes which have already re-used the same PIDs, or somesuch.
	## We should check to make sure this is safe.
	##
	## All the processes in the job should be totally gone by the time we
	## unlink($outfile) below, in case there is any danger that a residual
	## process having the $outfile still open might prevent the unlink from
	## succeeding (which wouldn't be an issue under UNIX, but might be under
	## Windows).  Here we're not waiting for the kill to fully take effect,
	## which we probably ought to if in fact the $job->kill() is really a
	## valid thing to do here.
	##
	$job->kill();
	$$timedout = 1;
	$logger->error("ERROR:  Plugin process took too long.  Throttled.");
    }
    if ( !unlink($outfile) ) {
	my $os_error = "$!";
	$os_error .= " ($^E)" if "$^E" ne "$!";
	$logger->error("ERROR:  exec_plugincmd_windows:  failed to clean up the temporary plugin outfile ($outfile):  $os_error");
    }
    return $ret;
}

################################################################################
#
#   insert_plugin_error_message()
#
#   Inserts the plugin execution error, passed as an argument, into the result
#   buffer.It will be send to all the primary targets for Poller_Service.
#   The return code for this message is "critical".
#
#   Arguments:
#   $servicename - The service name for which there was an error.
#   $command - Plugin command that caused the error.
#   $errstr - Error string.
#   $result_buf - Reference to the buffer containing results.
#
################################################################################
sub insert_plugin_error_message
{
    my ($servicename, $command, $errstr, $result_buf) = @_;
    my $msg_payload;
    my $osname = $^O;
    # "0" implies that the result is to be sent to all the primary targets.
    my $default_target = 0;
    # The return code for plugin error message  is "CRITICAL".
    my $return_code_crit = 2;
    my $hostname = $g_opt{H};
    # Record the IP address for the host, as seen on this host.
    my $packed_ip = gethostbyname($hostname);
    my $host_ip = defined($packed_ip) ? inet_ntoa($packed_ip) : 'unknown IP addr';

    # Compose the command.
    $logger->debug("DEBUG:  Inserting plugin error message to result buffer.");
    $msg_payload = "Plugin execution error:  $errstr";
    $msg_payload .= " for $command";
    $msg_payload .= " on $hostname [$host_ip]";
    $msg_payload .= " running Perl compiled under $osname $Config{osvers}";
    $logger->trace("TRACE:  insert_plugin_error_message:  $msg_payload");

    # Insert the message into the result buffer.
    insert_into_result_buf($default_target, $hostname, $servicename, $return_code_crit, $msg_payload, $result_buf);
}

################################################################################
#
#   insert_plugin_timeout_message()
#
#   Inserts the plugin timeout error into the result buffer.  This message is
#   for Poller_Service on monitored host.  The return code for this message is
#   "warning".
#
#   Arguments:
#   $command - Plugin command that caused the error.
#   $timeout - Plugin timeout.
#   $result_buf - Reference to the buffer containing results.
#
################################################################################
sub insert_plugin_timeout_message
{
    my ($command, $timeout, $result_buf) = @_;
    my $msg_payload;
    # "0" implies that the result is to be sent to all the primary targets.
    my $default_target = 0;
    # The return code for plugin timeout message is "WARNING".
    my $return_code_warn = 1;
    my $hostname = $g_opt{H};

    $logger->debug("DEBUG:  Inserting plugin timeout message to result buffer.");
    # Compose the message body.
    $msg_payload = "Plugin '$command' timed out after $timeout seconds";
    $logger->trace("TRACE:  insert_plugin_timeout_message:  $msg_payload");

    # Insert the message into the buffer.
    insert_into_result_buf($default_target, $hostname, $g_opt{s}, $return_code_warn, $msg_payload, $result_buf);
}

################################################################################
#
#   install_config_filepaths()
#
#   Sets up the autoconfig and hostconfig file paths.
#   Arguments:
#   $my_headpath - Head path for the agent.
#   $hostname - Name of the host for which to run the checks
#
################################################################################
sub install_config_filepaths
{
    my $my_headpath    = shift;
    my $hostname       = shift;
    my $hostconfigfile = 'NULL';
    my $my_osname      = $^O;

    # Set the filenames based on the operating system.
    if ( ( $my_osname eq 'linux' ) or ( $my_osname eq 'solaris' ) or ( $my_osname eq 'aix' ) or ( $my_osname eq 'hpux' ) ) {
	$hostconfigfile = "$my_headpath/config/gwmon_$hostname" . ".cfg";
    }
    elsif ( $my_osname eq 'MSWin32' ) {
	$hostconfigfile = "$my_headpath\\config\\gwmon_$hostname" . ".cfg";
    }

    return $hostconfigfile;
}

################################################################################
#
#   insert_into_result_buf()
#
#   Inserts the message passed as a result into the global buffer.
#   Assumes the default value for retries field.
#
#   Arguments:
#   $target - The target host for the result.
#   $host - The hostname for which this is a result, as known by the central server.
#   $service - The service name for which this is a result.
#   $ret_code - Return code for the results.
#   $msg_body - The message text.
#   $result_buf - A reference to a buffer containing results.
#
################################################################################
sub insert_into_result_buf
{
    my ($target, $host, $service, $ret_code, $msg_body, $result_buf) = @_;

    # The retries field should be 0, when the result is first spooled.
    my $default_retries = 0;

    # Use default value for retries.
    my $result_str = join ('',
			   $default_retries, "\t",
			   $target,          "\t",
			   time(),           "\t",
			   $host,            "\t",
			   $service,         "\t",
			   $ret_code,        "\t",
			   $msg_body,        "\n");

    # Push it into the result buffer.
    push(@$result_buf, $result_str);
    chomp $result_str;
    $logging->log_message("DEBUG:  Pushed result into the buffer:\n$result_str") if $g_opt{d};
}


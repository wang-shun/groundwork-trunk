################################################################################
#
#    gdma_spool_processor.pl
#
#    This is the spool processor part of GroundWork Distributed Monitoring
#    agent.  The spool processor reads the check results from the spool file
#    and attempts to submit them to the target hosts which are found alive.
#    It invokes send_nsca program to submit the result to the target server.
#    The read and write to the spool file are synchronized with the poller
#    program using a file lock.  The program picks up the spool file for
#    processing in a forever loop and at specific time intervals.
#    This should run as a system service.
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

use Fcntl qw(:DEFAULT :flock);
use IO::Socket;
use Getopt::Std;
use Config;

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

use POSIX qw(:sys_wait_h :signal_h :errno_h);
use Storable qw(dclone);
use Net::Telnet;

use GDMA::Logging;
use GDMA::Utils;

our $VERSION = '2.6.1';

# Load Win32::Job on windows platform only.
# We need this to create and manipulate processes to execute plugins.

if ( $^O eq 'MSWin32' ) {
    require Win32::Job;
    import Win32::Job;
}

# Is it an OS that we support?
# The special variable $^O contains the OS name.
if ( ( $^O ne 'linux' ) and ( $^O ne 'solaris' ) and ( $^O ne 'aix' ) and ( $^O ne 'hpux' ) and ( $^O ne 'MSWin32' ) ) {
    die "ERROR:  $^O is not a supported operating system\n";
}

# Function declarations
sub main;
sub install_config_filepaths;
sub process_live_targets;
sub process_dead_targets;
sub legacy_run_autoconfig_mode;
sub run_normal_mode;
sub build_live_targets;
sub read_and_purge_spoolfile;
sub filter_results;
sub batch_process;
sub send_nsca;
sub send_nsca_windows;
sub send_nsca_unix;
sub detect_conf_file_change;
sub reload_config;
sub spool_config_corrupt_message;
sub send_config_corrupt_message;
sub spool_startup_info;
sub spool_heart_beat_message;
sub spool_retries_rejection_message;
sub spool_age_rejection_message;
sub spool_transmission_failure_message;
sub dump_config;

# Command line options.
my %g_opt = ();

# A global hash that stores all the configuration file parameters.
my %g_config;

my $logging = undef;
my $logger  = undef;

my $trace = undef;

my $separator = '------------------------------------------------------------------------------------------';

my $file_separator = ( $^O eq 'MSWin32' ) ? "\\" : '/';

# Handle Command Line Options
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
#
# FIX LATER:  Running under Windows pretty much requires that the GDMA_BASE_DIR
# environment variable is set, but it might not be if you're not running as the same
# user as is used to run the GDMA system service.  So under Windows, we ought to at
# least check to see if that environment variable exists, and if not, warn that you're
# probably not running under the proper account.
#
if ( ( $^O eq 'linux' ) or ( $^O eq 'solaris' ) or ( $^O eq 'aix' ) or ( $^O eq 'hpux' ) ) {
    if ($> == 0) {
	(my $program = $0) =~ s<.*/><>;
	die "ERROR:  You cannot run $program as root.\n";
    }
}

# Set up the environment so that we will access correct libraries.
set_environment( GDMA::Utils::get_headpath() );

# Perform the spool processing in a forever main loop.
exit( main() ? 0 : 1 );

################################################################################
#
#   main()
#
#   This is the main spool processing function.
#   In a forever loop:
#   1. Checks if config file modified.  If yes, reads it.
#   2. Decides the mode of operation (normal mode/autoconfig mode)
#   3. For normal mode, it invokes run_normal_mode() to process the
#      spool file.
#   4. For autoconfig mode, it transmits any autoconfig messages from the poller,
#      by processing the high-priority queue instead of the normal queue of
#      result messages to be sent to the server.
#   5. Sleeps until it's time for the next iteration, as determined by
#      the configured Spooler_Proc_Interval.
#
################################################################################
sub main {
    my $failed = 0;

    ## We only set up to run as a daemon if we are not running interactively from the command line.
    ## See the Bookshelf for instructions on running the GDMA daemons interactively.
    if ( not $g_opt{i} ) {
	## setpgrp() is unimplemented under Windows.  Whatever we need to do to run as a daemon
	## on that platform, we'll have to figure out separately.
	if ( $^O ne 'MSWin32' ) {
	    ## FIX MAJOR:  HP-UX specifically requires running as a separate process group, to run under the
	    ## normal service startup setup.  However, more generally, there are a number of other steps we
	    ## should take to run as a daemon, on all platforms.  Just calling setpgrp() is a poor man's
	    ## daemon setup.  We should generalize this to call our GW::Daemon (or GroundWork::Daemon)
	    ## module to handle all the other aspects of life as a daemon, and where we fork and call the
	    ## POSIX::setsid() routine as a more general alternative to calling setpgrp().
	    setpgrp(0, 0);
	}
    }

    my $loop_start_time;

    # A loop counter
    my $loopcount = 0;

    # Filepaths that contain the autoconfig/default settings
    my $AutoconfigfilePath;
    my $AutoconfigOverridefilePath;

    # Path for pulled configuration file
    my $HostconfigfilePath;
    my $errstr;

    # A flag that marks autoconfig mode
    my $auto_config = 0;

    # Modification time of the autoconfig files.
    my $autoconf_last_modified     = 0;
    my $overrideconf_last_modified = 0;

    # Flag to denote autoconfig file has been modified
    my $autoconf_modified     = 0;
    my $overrideconf_modified = 0;

    # Modification time of the hostconfig file
    my $hostconf_last_modified = 0;

    # Flag to denote hostconfig file has been modified
    my $hostconf_modified = 0;

    # Flag to denote whether we are running for the first time
    my $first_reload = 1;

    # Flag to denote this is spool_processor's first iteration
    my $startup = 1;

    # Get the head path for the installation.
    my $head_path = GDMA::Utils::get_headpath();

    # Lock filename for host config file
    my $hostconf_lock_file = GDMA::Utils::get_config_lock_filename($head_path);

    # Make sure end-of-life messages are output before we call POSIX::_exit to quit.
    STDOUT->autoflush(1) if $g_opt{i};

    $trace = $g_opt{d} && $g_opt{d} =~ /^(\d+)$/ && $g_opt{d} > 1;

    ## We need certain critical %g_config options here, so we fake them up for the time being.
    my %t_config = ( Logdir => "$head_path${file_separator}log", Enable_Local_Logging => 'on' );
    my $logging_hostname = GDMA::Utils::my_hostname( $t_config{Use_Long_Hostname}, $t_config{Forced_Hostname}, $t_config{Use_Lowercase_Hostname} );
    my $logging_logfile = $t_config{Enable_Local_Logging} =~ /^on$/i ? $t_config{Logdir} . $file_separator . 'gwmon_' . $logging_hostname . '_spool_processor.log' : undef;
    my %logging_options = ( logfile => $logging_logfile, grouping => 'individual' );
    $logging_options{stdout}    = 1       if $g_opt{i};
    $logging_options{log_level} = 'debug' if $g_opt{d};
    $logging_options{log_level} = 'trace' if $trace;

    ## FIX MINOR:  Perhaps provide appropriate external values for these settings, presumably from the config file.
    ## (On the other hand, we haven't yet read the config file to figure out what these option values should be.)
    ## IF we don't supply these options, they'll be defaulted to sane values internal to the GDMA::Logging package.
    # $logging_options{max_logfile_size}       = 10_000_000;
    # $logging_options{max_logfiles_to_retain} = 5;

    $logging = GDMA::Logging->new( \%logging_options, 'started', \*STDERR );
    if ( not defined $logging ) {
	print "FATAL:  Cannot create a GDMA::Logging object" . ( defined($logging_logfile) ? " for file \"$logging_logfile\"" : '' ) . ".\n";
	return 0;
    }
    $logger = $logging->logger();
    $logging->log_separator($separator);

    # Set up SIGTERM and SIGINT handlers to make sure that we are
    # sensitive to these signals, only on linux/solaris/aix/hpux platforms.
    # On Solaris these signals are not delivered to the processes
    # running in background.  (If that sounds confused, it probably is.)
    if ( ( $^O eq 'linux' ) or ( $^O eq 'solaris' ) or ( $^O eq 'aix' ) or ( $^O eq 'hpux' ) ) {
	## FIX MAJOR:  We haven't taken any trouble to make ourself a process group leader,
	## so it makes no sense to kill our process group here.
	## FIX MAJOR:  Our typical child process (used to call and then write to send_nsca)
	## is now its own process group leader, so sending a signal here to just our own
	## process group wouldn't do the whole job even if we were our own process group leader.
	## FIX MINOR:  We should handle SIGHUP and SIGQUIT as well, in appropriate ways.
	# $SIG{TERM} = sub { kill "TERM", -$$; exit 1 };
	# $SIG{INT}  = sub { kill "TERM", -$$; exit 1 };
    }

    # Get the spoolfile paths based on the platform we are running on.
    my $normal_spool_filename = GDMA::Utils::get_spool_filename($head_path);
    my $priority_spool_filename = GDMA::Utils::get_spool_filename( $head_path, 1 );

    # Load high resolution timer Time::HiRes, if available.
    # Otherwise use normal resolution timer.
    my ( $hires_time, $hires_time_format ) = GDMA::Utils::load_hires_timer();

    # Set the values for server config filepaths based on platform.
    ($AutoconfigfilePath, $AutoconfigOverridefilePath) = install_autoconfig_filepaths($head_path);

    # Read the default parameters from the autoconfig and override files.
    if ( !GDMA::Utils::read_config( $AutoconfigfilePath, \%g_config, \$errstr, 0 ) ) {
	## We can't log anything here -- we don't know the log file name yet.
	## There is nothing to do.
	$logger->fatal("FATAL:  Failed to read the main autoconfig file:  $errstr");
	die "Failed to read the main autoconfig file:  $errstr\n";
    }
    if ( !GDMA::Utils::read_config( $AutoconfigOverridefilePath, \%g_config, \$errstr, 1 ) ) {
	## We can't log anything here -- we don't know the log file name yet.
	## There is nothing to do.
	$logger->fatal("FATAL:  Failed to read the autoconfig override file:  $errstr");
	die "Failed to read the autoconfig override file:  $errstr\n";
    }

    my $new_logging_hostname = GDMA::Utils::my_hostname( $g_config{Use_Long_Hostname}, $g_config{Forced_Hostname}, $g_config{Use_Lowercase_Hostname} );
    my $new_logging_logfile =
      $g_config{Enable_Local_Logging} =~ /^on$/i ? $g_config{Logdir} . $file_separator . 'gwmon_' . $new_logging_hostname . '_spool_processor.log' : undef;
    ## If the smartmatch operator were not still considered experimental/deprecated, this comparison would be simpler as:
    ## (not ($new_logging_logfile ~~ $logging_logfile))
    if (   ( defined($new_logging_logfile) xor defined($logging_logfile) )
	|| ( defined($new_logging_logfile) && defined($logging_logfile) && $new_logging_logfile ne $logging_logfile ) )
    {
	if ( not defined $new_logging_logfile ) {
	    $logger->notice("NOTICE:  Turning off logging, to reflect the Enable_Local_Logging option.");
	    ## We potentially rotate the currently open logfile here before reinitializing the logging,
	    ## since this will be our only chance to do so.  It doesn't matter whether this fails, since
	    ## we are just about to re-initialize the logging anyway; so we don't check the return value.
	    $logging->rotate_logfile();
	}

	$logging_options{logfile} = $new_logging_logfile;

	## FIX MINOR:  Perhaps provide appropriate external values for these settings, presumably from the config file.
	## (On the other hand, we haven't yet read the config file to figure out what these option values should be.)
	## IF we don't supply these options, they'll be defaulted to sane values internal to the GDMA::Logging package.
	# $logging_options{max_logfile_size}       = 10_000_000;
	# $logging_options{max_logfiles_to_retain} = 5;

	$logging = GDMA::Logging->new( \%logging_options, 'started', \*STDERR );
	if ( not defined $logging ) {
	    print "FATAL:  Cannot create a GDMA::Logging object"
	      . ( defined($logging_logfile) ? " for file \"$logging_logfile\"" : '' ) . ".\n";
	    return 0;
	}
	$logger = $logging->logger();
    }

    # Set the values for host config filepath based on platform and on the auto-config setup.
    # If the auto-config file doesn't specify whether to use a long or short hostname, we first
    # try the long form (the most specific construction), and then fall back to the short form
    # (the most general construction) if a config file using the long form is not available.
    my $Use_Long_Hostname = defined( $g_config{Use_Long_Hostname} ) ? $g_config{Use_Long_Hostname} : 'on';
    my $hostname = GDMA::Utils::my_hostname( $Use_Long_Hostname, $g_config{Forced_Hostname}, $g_config{Use_Lowercase_Hostname} );
    $HostconfigfilePath =
      install_hostconfig_filepaths( $head_path, $Use_Long_Hostname, $g_config{Forced_Hostname}, $g_config{Use_Lowercase_Hostname} );
    if ( -f $HostconfigfilePath ) {
	$g_config{Use_Long_Hostname} = $Use_Long_Hostname if not defined $g_config{Use_Long_Hostname};
    }
    elsif ( not defined $g_config{Use_Long_Hostname} ) {
	$HostconfigfilePath = install_hostconfig_filepaths(
	    $head_path,
	    $g_config{Use_Long_Hostname},
	    $g_config{Forced_Hostname},
	    $g_config{Use_Lowercase_Hostname}
	);
    }
    $logger->debug("DEBUG:  Spooler is using hostname \"$hostname\".");

    # Execute forever, unless we've been told to execute just once, or until we receive a shutdown signal.
    while (1) {
	$loop_start_time = &$hires_time();

	if ( $g_config{Enable_Local_Logging} =~ /^on$/i ) {
	    my $restart_program = 0;
	    my $log_spin_status = $logging->rotate_logfile();
	    if ( $log_spin_status == 0 ) {
		## Log rotation failed; this process should not asssume that logging is still functional.
		## We make an attempt to log a message as to why we are exiting, even though it might not
		## work, to make it less mysterious why the daemon has gone down.
		$logger->fatal("FATAL:  Logfile rotation failed in process $$.  Logging might no longer be functional.");
		$restart_program = 1;
	    }
	    elsif ( $log_spin_status == 1 ) {
		## Log rotation "succeeded", but took no actual action with respect to switching the logfile.
		## To continue usefully logging (mostly, to create a new logfile and use it if the logfile was
		## recently externally renamed or deleted), we manually reopen the logfile in this case.
		if ( not $logging->reopen_logfile() ) {
		    $logger->fatal("FATAL:  Logfile re-open failed in process $$.  Logging might no longer be functional.");
		    $restart_program = 1;
		}
	    }
	    if ($restart_program) {
		## Let's not take drastic action in a tight loop.
		sleep 10;

		## FIX MAJOR:  Given that we don't have an external watchdog on the GDMA daemons, we might
		## attempt to have them restart themselves in this situation.  However, so as not to cause
		## disruption with external system-service-tracking facilities, we would need to do so in
		## a manner that would allow re-use of exactly the same process ID.  Thus a direct exec()
		## would be in order, not spawning a child process and then ourselves exiting.  On UNIX-like
		## operating systems, that should be quite easy.  On Windows, we need to think carefully and
		## test to see if exec() of that sort is even possible.
		##
		## If we do exec(), we need to think about whatever external-resource cleanup that normally
		## happens on process termination that we implicitly depend on, such as automatic release of
		## file locks.  Do they get released on an exec() as well?  We want the rejuvenated process
		## to start with a clean slate, not hindered by attached strings.  Also, we would need to
		## force the use of a form of exec() that is guaranteed not to use a shell as an intermediate
		## process, since we need to re-use the same process ID to get any external daemon-handling
		## code to believe that the same process has stayed running.
		##
		$logger->fatal("FATAL:  Process $$ is exiting due to errors shown above.");
		$failed = 1;
		last;
	    }
	}

	# Check if any of the config files have changed.  If last_modified == 0 then
	# we are reading for the first time; the function will return 1 in this case.
	# Reload config if any of the files have changed since we last checked.
	# We call detect_conf_file_change() on all of them, not trying to short-circuit this checking, to make
	# sure the "XXX_last_modified" timestamp is updated in this cycle for all files that have changed.
	detect_conf_file_change( $AutoconfigfilePath,         \$autoconf_last_modified,     \$autoconf_modified,     0 );
	detect_conf_file_change( $AutoconfigOverridefilePath, \$overrideconf_last_modified, \$overrideconf_modified, 1 );
	detect_conf_file_change( $HostconfigfilePath,         \$hostconf_last_modified,     \$hostconf_modified,     0 );
	if ( $autoconf_modified || $overrideconf_modified || $hostconf_modified ) {
	    ## Reload the config files if this is the first iteration or some config file is modified.
	    if (
		!reload_config(
		    $AutoconfigfilePath,    $AutoconfigOverridefilePath, \$HostconfigfilePath, \$auto_config,
		    $normal_spool_filename, $head_path,                  $first_reload
		)
	      )
	    {
		$logger->error("ERROR:  Failed to reload config files.");
	    }
	    else {
		$first_reload = 0;
	    }
	    $hostname = GDMA::Utils::my_hostname( $g_config{Use_Long_Hostname}, $g_config{Forced_Hostname}, $g_config{Use_Lowercase_Hostname} );
	    $logger->debug("DEBUG:  Spooler is using hostname \"$hostname\".");
	    if ( !defined( $g_config{Target_Server} ) ) {
		## If we don't have a target server defined, the spool processor cannot process the spool file.
		## This can, in fact, be a commonly desired setup.  For instance, the GDMA spooler which is
		## provided on the GroundWork Monitor system (as opposed to its GDMA clients) can be disabled
		## by failing to define the Target_Server.
		##
		## We need to leave around some evidence of this failure, so the cause of the spooler not running
		## for long periods is not completely mysterious.  So in this situation, we force logging on if
		## we are operating in server context.  Given the sleep we impose, this will create only a slow
		## accumulation of log messages.
		##
		## FIX MINOR:  Implement automatic log-file rolling from within this script, based on file size,
		## to limit the total space occupied by the log file(s).
		##
		my $in_server_context = -f '/usr/local/groundwork/Info.txt';
		$g_config{Enable_Local_Logging} = 'on' if $in_server_context;
		$logger->notice("NOTICE:  No Target server is defined in any of the config files.");
		$logger->notice("         (That may make sense on a GW server; it probably doesn't on a GDMA client.)");

		## We sleep rather than die immediately, partly to limit the growth of the log file if retries
		## were to happen frequently, and partly because we want the script to be normally up and running
		## in a server context if this is the expected configuration.  (It might still occasionally be
		## seen to be briefly down in a server context because of an intentional sleep in the supervise
		## run script before script startup, to prevent tight start/stop loops should some other type of
		## failure occur.)
		if ($in_server_context) {
		    ## Server context.  We assume this configuration is intentional; staying up is the right
		    ## choice so "service groundwork status gwservices" doesn't think this component is broken.
		    $logger->notice("NOTICE:  Sleeping forever ...");
		    sleep 100_000_000;
		}
		else {
		    ## GDMA client context.  Sleep only briefly, so the spooler can automatically resume
		    ## operation quickly if the poller manages to bring new configuration data into view
		    ## and if some supervisory agent (perhaps a human) restarts the spooler.
		    $logger->notice("NOTICE:  Sleeping for ten minutes before exiting.");
		    sleep 600;
		}
		## FIX LATER:  Should we just start the next iteration of the main loop we are in, instead
		## of exiting at this point, so no external agent is needed to restart the spooler?
		$logger->notice("NOTICE:  Exiting due to the situation described above.");
		exit 1;
	    }
	    $autoconf_modified     = 0;
	    $overrideconf_modified = 0;
	    $hostconf_modified     = 0;
	}

	if ($startup) {
	    ## Send the spooler startup information to the monitor server.
	    spool_startup_info($normal_spool_filename);

	    ## Send it only at start-up.
	    $startup = 0;
	}

	# Check if we have to run in auto-config mode.
	if ( $auto_config == 1 ) {
	    ## We no longer send a special packet from this process.  Instead, we let the poller spool
	    ## whatever packets it needs in a special high-priority queue, and we process that queue here
	    ## when the spooler finds itself in a similar situation.  It's possible that these mechanisms
	    ## might get slightly out-of-sync, but the danger is very low.
	    ## legacy_run_autoconfig_mode($head_path);
	    ##
	    ## Note that the other GDMA daemons will likely be spooling many service-check results to the
	    ## $normal_spool_filename, but none of them will be sent to the server in this operational mode.
	    ## Hence the reload_config() routine now issues a WARNING message if $auto_config mode is in
	    ## operation after all the config files have been successfully read and processed, to direct
	    ## the administrator's attention to what likely needs to be changed.
	    run_normal_mode( 'auto-config', $priority_spool_filename, $loop_start_time, $hires_time, $hires_time_format );
	}
	else {
	    run_normal_mode( 'normal', $normal_spool_filename, $loop_start_time, $hires_time, $hires_time_format );
	}

	++$loopcount;

	# Compute the time to wait before the next execution.
	my $exec_time = &$hires_time() - $loop_start_time;
	$logger->stats("STATS:  Loop count = $loopcount.  Last loop exec time = " . sprintf( $hires_time_format, $exec_time ) . " seconds.");

	# We are to run only once, if "-x".
	last if ( $g_opt{x} );

	# If it's time to sleep, do so.  Note that if the system time jumps for some reason (e.g.,
	# from an asynchronous NTP time-slew adjustment, or from a manual system-time correction), the
	# execution time may come out negative.  So we need to protect ourselves from that possibility.
	# Spooler_Proc_Interval is in seconds.
	if ( ( $exec_time >= 0 ) and ( $exec_time < $g_config{Spooler_Proc_Interval} ) ) {
	    ## FIX MINOR:  Be precise from the original start time, to avoid a caravan effect?
	    my $wait_time = int( $g_config{Spooler_Proc_Interval} - $exec_time );
	    $logger->debug("DEBUG:  Waiting $wait_time seconds ...");
	    sleep $wait_time;
	}
    }
    return !$failed;
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
#   run_normal_mode()
#
#   Excutes the normal mode of operation for spool processor -
#   1. Builds a list of live targets.
#   2. Reads in the spool file and truncates it to 0.
#   3. In memory, filters out the results which are too old or tried
#      too many times.
#   4. Respools all the results marked for dead targets.
#   5. Attempts transmission of results for all the live targets.
#
#   The results that are marked for target "0", are transmitted to all primary
#   targets.
#   The processing of secondary target server is a little tricky.  If a
#   secondary target is defined in the config file, all the results that fail
#   for any of the primary targets will be respooled for secondary target.
#   That is to say that each result will be attempted for a designated primary
#   once before it is tried for the secodary.  The total number of retries,
#   for primary and secodary, will be "max_retries", after which the result
#   will be discarded.
#   If secondary target is not defined, each result tried for the designated
#   primary target at the most "max_retries" times.
#
#   Arguments:
#   $mode              - mode the process is running in.
#   $spool_filename    - spool file name.
#   $hires_time        - Reference to available time subroutine.
#   $hires_time_format - Available resolution time format.
#   $loop_start_time   - to compute normal mode execution time.
#
################################################################################
sub run_normal_mode {
    my ( $mode, $spool_filename, $loop_start_time, $hires_time, $hires_time_format ) = @_;

    # A buffer that stores results to be transmitted.
    my @result_buf = ();

    # A list of target servers found alive.
    my @live_targets = ();

    # A list of target servers found dead.
    my @dead_targets = ();

    # Holds the count of results rejected for too many attempts.
    my $retries_rejection_count = 0;

    # Holds the count of results rejected because of age.
    my $age_rejection_count = 0;
    my $secondary_target;
    my $ret_val = 1;

    # A flag that indicates that something other than heartbeat message
    # was transmitted in the current iteration.
    # Set it to 0 to begin with.
    my $non_heartbeat_msg_transmitted = 0;

    # Variable to hold total no. of results transmitted successfully by spool processor.
    my $total_transmission_count = 0;

    $logger->debug("DEBUG:  Spool processor is running in $mode mode.");

    # Check if we have a spool file to process.
    if ( -e $spool_filename ) {
	## Build a list of targets to which we will send the results.
	$ret_val = build_live_targets( \@live_targets, \@dead_targets );
	if ( !$ret_val ) {
	    $logger->debug("DEBUG:  No live targets found.");
	    ## Though no live targets we should respool for dead targets.
	    $ret_val = 1;
	}

	if ($ret_val) {
	    ## Read the entire spoolfile contents into a result buffer and then
	    ## purge the file to 0.  This is so as to minimize the amount of time
	    ## we hold the lock on spool file.  We don't want to starve the poller.
	    ## FIX MINOR:  Rename as active, instead:  read active file first, and
	    ## only if none around, then read spool file (for reliability -- don't
	    ## destroy on-disk data before it has been sent, and thereby risk
	    ## getting to-send data out of order).
	    $ret_val = read_and_purge_spoolfile( $spool_filename, \@result_buf );
	    if ( !$ret_val ) {
		$logger->error("ERROR:  Could not read or purge the spoolfile.");
	    }

	    if ( !scalar(@result_buf) ) {
		$logger->debug("DEBUG:  No data read from the spoolfile.");
		## This is not a catastrophic condition, but we have nothing to process.  Try again later.
		$ret_val = 0;
	    }
	}

	if ($ret_val) {
	    ## Remove results older than retention_time or tried more than max_retries times.
	    filter_results( \@result_buf, \$retries_rejection_count, \$age_rejection_count );

	    if ($retries_rejection_count) {
		spool_retries_rejection_message( $spool_filename, $retries_rejection_count );
	    }
	    if ($age_rejection_count) {
		spool_age_rejection_message( $spool_filename, $age_rejection_count );
	    }
	    if ( !scalar(@result_buf) ) {
		$logger->debug("DEBUG:  No result is left after discarding the old ones.");
	    }

	    # Set $secondary_target to "Target_Server_Secondary", if one is
	    # defined in the config.  Otherwise set it to a broken value.
	    $secondary_target = ( $mode eq 'normal' and defined( $g_config{Target_Server_Secondary} ) ) ? $g_config{Target_Server_Secondary} : "NULL";

	    # Parse the secondary target server and extract the address part,
	    # if it is specified as a URL - https://abc.def
	    # Otherwise, just use whatever is configured.
	    if ($secondary_target =~ m{^\S+://([^/]+)}) {
		$secondary_target = $1;
	    }

	    # We are not going to attempt a transmission for the targets found dead earlier.
	    # Hence, respool all the results marked for dead targets.  We are going to check
	    # for target availability in each iteration.  This means that if the target is
	    # found dead for "max_retries" iterations, the result will be purged.
	    if ( scalar(@dead_targets) ) {
		process_dead_targets( $spool_filename, \@dead_targets, \@result_buf, $secondary_target );
	    }

	    # Now process the results for live targets.
	    # Attempt a transmission to each of the live targets that is applicable.
	    if ( scalar(@live_targets) ) {
		process_live_targets( $spool_filename, \@live_targets, \@result_buf, $secondary_target, \$total_transmission_count,
		    \$non_heartbeat_msg_transmitted );
	    }

	    if ( $mode eq 'normal' ) {
		## Compose and spool the heartbeat message, only if ("Spooler_Status" is "on") or
		## if ("Spooler_Status" is "updates" and a non heartbeat message was transmitted
		## in this iteration).  This is to avoid unnecessary noise on GW Monitor.
		if (   ( $g_config{Spooler_Status} =~ /on/i )
		    or ( $g_config{Spooler_Status} =~ /updates/i and $non_heartbeat_msg_transmitted == 1 ) )
		{
		    spool_heart_beat_message( $spool_filename, $total_transmission_count, $loop_start_time, $hires_time_format, $hires_time );
		}
	    }
	}
    }
    else {
	## We can't do anything for now.
	$logger->info("INFO:  In $mode mode; the spool file ($spool_filename) does not exist.");
    }
}

################################################################################
#
#   process_dead_targets()
#
#   Processes the results for targets found dead earlier; i.e., it
#   respools the results which are marked for dead targets.
#
#   Arguments:
#   $normal_spool_filename - spool file name
#   $dead_targets          - A reference to an array of dead targets.
#   $result_buf            - A reference to an array of targets to be processed.
#   $secondary_target      - The secondary target address.  The results will be
#                            marked for this target while respooling, if it is
#                            not "NULL".
#
################################################################################
sub process_dead_targets() {
    my ( $normal_spool_filename, $dead_targets, $result_buf, $secondary_target ) = @_;
    my $target;
    my @respool_results = ();
    my $blocking        = 1;
    my $num_results;
    my $errstr;
    my $respool_target;

    $logger->debug("DEBUG:  Processing dead targets.");
    foreach $target (@$dead_targets) {
	## For each dead target ...
	my $result;

	# ... run through the list of results.
	foreach $result (@$result_buf) {
	    ## Read in the designated target for the result.
	    my ( $retries, $res_target, $rest ) = split( /\t/, $result, 3 );

	    # "0" means for all primary targets.
	    if ( ( ( $res_target eq "0" ) and ( $target ne $secondary_target ) ) or ( $res_target eq $target ) ) {
		## Respool for secondary target if it is not NULL.
		## Otherwise, persist with same old target.
		$respool_target = ( $secondary_target ne "NULL" ) ? $secondary_target : $target;

		# Increment the retry count even if we did
		# not try the actual transmission.
		my $new_result_string = join( "\t", ++$retries, $respool_target, $rest );

		# Push the result into respool array.
		push( @respool_results, $new_result_string );
	    }
	}
    }

    # Respool the results marked for dead targets.  Make a blocking call to spool_results().
    # If we can't spool the results here, we lose them.
    if ( scalar(@respool_results) ) {
	if ( !GDMA::Utils::spool_results( $normal_spool_filename, \@respool_results, $blocking, \$num_results, \$errstr ) ) {
	    ## This is essentially loss of data.
	    $logger->warn("WARNING:  $errstr");
	    $logger->warn( 'WARNING:  Failed to respool ' . @respool_results . ' result' . ( @respool_results == 1 ? '; it' : 's; they' ) . ' will be lost.' );
	}
    }
}

################################################################################
#
#   process_live_targets()
#
#   Processes the results for live targets -
#   1. Sorts the results for targets
#   2. Passes the results for each live target to send_nsca in chunks
#      of configured batch size.
#   3. Respools the failed results, if any, after incrementing the
#      retry count.
#
#   Arguments:
#   $normal_spool_filename - spool file name
#   $live_targets          - A reference to an array of live targets.
#   $result_buf            - A reference to an array of targets to be processed.
#   $secondary_target      - The secondary target address.  The results will be
#                            marked for this target while respooling, if it is
#                            not "NULL".
#   $total_transmission_count - A reference, we populate it with no. of results
#                               transmitted.
#   $non_heartbeat_msg_transmitted - A reference.  We set this to 1, if a
#                                    result other than heartbeat was
#                                    transmitted successfully.
#
################################################################################
sub process_live_targets {
    my ( $normal_spool_filename, $live_targets, $result_buf, $secondary_target, $total_transmission_count, $non_heartbeat_msg_transmitted ) =
      @_;
    my $target;
    my $blocking = 1;
    my $num_results;
    my $errstr;
    my $respool_target;

    $$total_transmission_count = 0;

    # We set this variable to 0 at the start of each iteration.
    # Its value may be updated and read right throughout the iteration.
    $$non_heartbeat_msg_transmitted = 0;
    $logger->debug("DEBUG:  Processing live targets.");
    foreach $target (@$live_targets) {
	$logger->trace("TRACE:  Processing for target $target");

	# For each live target ...
	my @results_per_target = ();
	my $result;

	# Variable to hold results transmitted by batch_process at a time.
	my $batch_transmitted_count = 0;

	# Run through the list of results.
	foreach $result (@$result_buf) {
	    ## Read in the designated target for the result.
	    my ( $retries, $res_target, $rest ) = split( /\t/, $result, 3 );

	    # "0" means send to all primary live targets, but
	    # not the secondary.  The live targets array could contain
	    # the secondary target as well.
	    if ( ( $res_target eq "0" ) and ( $target ne $secondary_target ) ) {
		## Insert the actual target in the buffer.
		my $new_result_string = join( "\t", $retries, $target, $rest );

		# Push the result into the array for current target.
		push( @results_per_target, $new_result_string );
	    }

	    # If result target is non zero, check if it matches
	    # the target we are looking for.
	    elsif ( $res_target eq $target ) {
		push( @results_per_target, $result );
	    }
	}

	# Process the results in batches.
	batch_process( \@results_per_target, $target, \$batch_transmitted_count, $non_heartbeat_msg_transmitted );
	$$total_transmission_count += $batch_transmitted_count;

	# Whatever is left in the array are failed results.  Transmission of results may fail even if we
	# attempt it only for targets found live earlier.  We need to write them back to the spool file.
	my $failed_results_count = scalar(@results_per_target);
	if ( $failed_results_count > 0 ) {
	    ## Spool transmission failure message
	    spool_transmission_failure_message( $normal_spool_filename, $failed_results_count, $target );

	    # We need to bump up the retries field before writting
	    # back to the spool file.  Modify the array in place.
	    foreach $result (@results_per_target) {
		## Split the result into 3 parts:  retries, target, and the rest of the string.
		my ( $retries, $res_target, $rest ) = split( /\t/, $result, 3 );

		# Increment the retries count.
		$retries++;

		# Respool for secondary target if it is not NULL.
		# Otherwise, persist with same old target.
		$respool_target = ( $secondary_target ne "NULL" ) ? $secondary_target : $res_target;

		# Join "retries" and "target" with the rest, to create a
		# new result string.
		$result = join( "\t", $retries, $respool_target, $rest );
	    }

	    # Make a blocking call to spool_results().  If we can't spool the results here, we lose them.
	    if ( !GDMA::Utils::spool_results( $normal_spool_filename, \@results_per_target, $blocking, \$num_results, \$errstr ) ) {
		## This is essentially loss of data.
		$logger->warn("WARNING:  $errstr");
		$logger->warn( 'WARNING:  Failed to respool ' . @results_per_target . ' result' . ( @results_per_target == 1 ? '; it' : 's; they' ) . ' will be lost.' );
	    }
	}
    }
}

################################################################################
#
#   build_live_targets()
#
#   Builds a list of live targets by telnet'ing to NSCA port on each
#   of the configured primary and secondary, if any, target servers.
#   The dead servers are rejected with a silent warning.
#   Returns the number of live targets found.
#
#   Arguments:
#   $live_targets - A reference to an array where live targets are to be stored.
#   $dead_targets - A reference to an array where dead targets are to be stored.
#
################################################################################
sub build_live_targets {
    my $live_targets   = shift;
    my $dead_targets   = shift;
    my @config_targets = ();
    my $target;
    my $mode;
    my $length;

    # Timout in seconds for NSCA ports
    my $nsca_port_timeout = 10;

    # Finding dead servers is not a catastrophic condition, so we silently
    # ignore them.  Hence the error mode "return" rather than "die".
    $mode = "return";

    # Get the comma-separated primary server names into an array.
    @config_targets = split( /[,\s]+/, $g_config{Target_Server} );

    # If a secondary target server is defined, check its health too.
    if ( defined( $g_config{Target_Server_Secondary} ) ) {
	push @config_targets, $g_config{Target_Server_Secondary};
    }

    foreach $target (@config_targets) {
	## The target server in the configuration file will be
	## https://<address>/somedir.  We need to extract the address.
	## We're going to force the requirement that the <address>
	## part is the place to send the results, ie the web server from
	## which we pull the cfgs is the same place we send nsca results.

	# E.g., https://abc.def/gdma-linux -> abc.def
	if ( $target =~ /^\S+:\/\/(.*)?\/.*$/i ) {
	    $target =~ s|^\S+://(.*)?/.*$|$1|ig;
	}

	# E.g., https://abc.def -> abc.def
	if ( $target =~ /^\S+:\/\/(.*)?.*$/i ) {
	    $target =~ s|^\S+://(.*)?.*$|$1|ig;
	}

	# log error if we couldn't extract
	if ( $target =~ /^\S+:\/\// ) {
	    $logger->error("ERROR:  Could not extract server from '$target'.");
	    next;
	}

	# Telnet momentarily to the nsca port to check if somebody is listening.
	my $obj = new Net::Telnet(
	    Host    => $target,
	    Port    => $g_config{Spooler_NSCA_Port},
	    Timeout => $nsca_port_timeout,
	    Errmode => $mode
	);

	# Check if the connection was successful.
	if ( defined($obj) ) {
	    ## Live target found.  Stuff it into live_targets array.
	    push( @$live_targets, $target );

	    # Close the connection.
	    if ( !$obj->close() ) {
		$logger->error("ERROR:  Could not close the connection on NSCA port for $target");
	    }
	}
	else {
	    ## Stuff the dead target into dead_targets array.
	    push( @$dead_targets, $target );

	    # Report dead target in the log.
	    $logger->debug("DEBUG:  $target found dead.");
	}
    }

    # Return the number of live targets found.
    $length = @$live_targets;
    return $length;
}

################################################################################
#
#   read_and_purge_spoolfile()
#
#   Reads the spool file into a buffer and purges the spool file to 0.
#   The reads and writes to the spool file are wrapped around with a
#   blocking file lock.  Returns 1 on sucess and 0 on failure.
#
#   Arguments:
#   $spool_filename - spool file name.
#   $results        - A Reference to the results buffer where the results are read into.
#
################################################################################
sub read_and_purge_spoolfile {
    my $spool_filename = shift;
    my $results        = shift;
    my $spoolh;
    my $ret_val = 1;

    if ( !open( $spoolh, '+<', $spool_filename ) ) {
	$logger->error("ERROR:  Could not open spoolfile ($spool_filename) for read and write:  $!");
	$ret_val = 0;
    }
    else {
	## Get a blocking lock on the spool file.
	if ( GDMA::Utils::get_lock($spoolh) ) {
	    ## We got the lock.  Read the spool file.
	    @$results = <$spoolh>;

	    # Purge the spool file.
	    $ret_val = truncate( $spoolh, 0 );
	    if ( !$ret_val ) {
		$logger->error("ERROR:  Could not truncate spoolfile ($spool_filename):  $!");
	    }

	    # Relinquish the lock.
	    if ( not GDMA::Utils::release_lock($spoolh) ) {
		## We think we are never going to get the lock again.  May as well quit.
		$logger->fatal("FATAL:  Could not relinquish the spool file lock:  $!.  Exiting ...");
		close $spoolh;
		exit 1;
	    }
	}
	else {
	    ## No lock.  Report error.
	    $ret_val = 0;
	    $logger->error("ERROR:  Could not get spool lock.");
	}

	# We are done with the spool file.
	close $spoolh;
    }
    return $ret_val;
}

################################################################################
#
#   filter_results()
#
#   Filters out the results which are older than retention_time or have
#   been tried for transmission more than max_retries times.  Directly
#   removes the entries from results buffer passed.
#
#   Arguments:
#   $results                     - A reference to an array which holds the results.
#   $ref_retries_rejection_count - A reference to retry rejection count variable.
#   $ref_age_rejection_count     - A reference to age rejection count variable.
#
################################################################################
sub filter_results {
    my ( $results, $ref_retries_rejection_count, $ref_age_rejection_count ) = @_;
    my $i = 0;
    my $retries;
    my $target;
    my $timestamp;
    my $now;

    # Initialize the rejection counts.
    $$ref_retries_rejection_count = 0;
    $$ref_age_rejection_count     = 0;

    # Record the current time.
    $now = time;

    # Run through the results array.
    while ( defined( $results->[$i] ) ) {
	## For each result line -
	## 1. Suck in the first 3 fields
	( $retries, $target, $timestamp ) = split( /\t/, $results->[$i], 4 );

	## 2. Check if we have tried it too many times
	if ( $retries >= $g_config{Spooler_Max_Retries} ) {
	    ## Remove the entry from the array
	    splice( @$results, $i, 1 );
	    $$ref_retries_rejection_count++;
	}

	## 3. Check if it is too old
	elsif ( ( $now - $timestamp ) >= ( $g_config{Spooler_Retention_Time} ) ) {
	    ## Remove the entry from the array
	    splice( @$results, $i, 1 );
	    $$ref_age_rejection_count++;
	}
	else {
	    ## When we delete an entry from the array, we effectively
	    ## move one place ahead anyway.  So we need to increment
	    ## the array counter only when we don't remove anything.
	    $i++;
	}
    }
}

################################################################################
#
#   batch_process()
#
#   Processes the results passed, in chunks of size "Spooler_Batch_Size".
#   Invokes send_nsca() for each chunk.  Eats out the first two fields
#   (retries and target) from each result line before sending.  Updates
#   the results array passed with the results that failed.
#
#   Arguments:
#   $results                  - A reference to array of results to be processed.
#   $target                   - The target to send the results to.
#   $result_transmitted_count - a reference, we populate with no. of results
#                               transmitted for this target.
#   $non_heartbeat_msg_transmitted - A reference.  We set this to 1, if a
#                                    result other than heartbeat was
#                                    transmitted successfully.
#
################################################################################
sub batch_process {
    my ( $results, $target, $result_transmitted_count, $non_heartbeat_msg_transmitted ) = @_;
    my @chunk                   = ();
    my @failed_results          = ();
    my $non_heartbeat_msg_found = 0;

    if ( scalar(@$results) == 0 ) {
	## There are no results.  We are done here.
	return 0;
    }

    # We have an array of results for the passed target.
    # We need to pass them to send_nsca in chunks of "Spooler_Batch_Size".
    # So go ahead and create those chunks.
    @chunk = splice( @$results, 0, $g_config{Spooler_Batch_Size} );
    while ( scalar(@chunk) > 0 ) {
	## Each result line in the spool file contains 2 fields, retries
	## and target, that only poller and spool processor understand.
	## So build a new array after removing those 2 fields.
	my @chunk_to_send = ();
	@chunk_to_send = map( ( split( /\t/, $_, 3 ) )[2], @chunk );

	# We need to decide if we transmitted a non heartbeat message.
	# First we will go through the chunk to check if it has a
	# non heartbeat message.  Then we will check if the transmission
	# of the chunk was successful.  We need to do this only if
	# "Spooler_Status" parameter is set to "updates".  So also,
	# if $non_heartbeat_msg_transmitted is set, then we know
	# that we transmitted a non heartbeat message in this iteration,
	# in an earlier batch or an earlier target.
	if ( ( $g_config{Spooler_Status} =~ /updates/i ) and $$non_heartbeat_msg_transmitted == 0 ) {
	    foreach (@chunk_to_send) {
		unless (/Spooler transmitted \d+ results in \S+ secs.*/) {
		    $non_heartbeat_msg_found = 1;
		    ## We got what we wanted; get out of the loop.
		    last;
		}
	    }
	}

	# Send the chunk for processing.
	if ( !send_nsca( \@chunk_to_send, $target ) ) {
	    ## Transmission of results failed.  Push the original chunk into failed results array.
	    push( @failed_results, @chunk );
	}
	else {
	    ## If send_nsca succeeded we transmitted no. of results = length of array @chunk
	    $$result_transmitted_count += scalar(@chunk);

	    # If the chunk had a non heartbeat message, we will spool the heartbeat in this iteration.
	    $$non_heartbeat_msg_transmitted = 1 if ($non_heartbeat_msg_found);
	}

	# Splice eats up the portion from the array.
	# Hence, offset, the second argument, will always be 0.
	@chunk = splice( @$results, 0, $g_config{Spooler_Batch_Size} );
    }

    # Stuff the failed results into the results buffer.
    @$results = @failed_results;

    # If there are no failed results, the transmission is successful.
    return ( scalar(@$results) == 0 );
}

################################################################################
#
#   send_nsca()
#
#   Invokes send_nsca program to submit a chunk of results to the
#   specified target.  Calls two different functions to handle send_nsca
#   on (linux/solaris) and windows.  Returns 1 if send_nsca was
#   successful, 0 otherwise.
#
#   Arguments:
#   $chunk  - A reference to chunk of results to be processed.
#   $target - The target to send the results to.
#
################################################################################
sub send_nsca {
    my $chunk  = shift;
    my $target = shift;
    my $status = 0;
    my $osname = $^O;

    # Invoke send_nsca as a new process and make sure that it terminates within
    # the configured timeout.  This needs to be handled differently on different plaforms.
    if ( ( $osname eq 'linux' ) or ( $osname eq 'solaris' ) or ( $osname eq 'aix' ) or ( $osname eq 'hpux' ) ) {
	## Invocation will be same on linux and solaris.
	$status = send_nsca_unix( $chunk, $target );
    }
    elsif ( $osname eq 'MSWin32' ) {
	$status = send_nsca_windows( $chunk, $target );
    }
    return $status;
}

################################################################################
#
#   send_nsca_unix()
#
#   Invokes send_nsca as a new process, on linux or solaris, to transmit
#   the chunk of results passed on.  Makes sure that the invoked send_nsca
#   terminates within the configured timeout.
#   Returns 1 if transmission was successful, 0 otherwise.
#
#   Arguments:
#   $chunk  - A reference to chunk of results to be processed.
#   $target - The target to send the results to.
#
################################################################################

my $signal_name = undef;

sub catch_signal {
    my $signame = shift;
    $signal_name = 'SIG' . $signame;
    die "Caught a $signal_name signal!\n";
}

sub send_nsca_unix {
    my $chunk  = shift;
    my $target = shift;
    my $kidpid;
    my @send_nsca;
    my $status               = 0;
    my $Spooler_NSCA_Timeout = defined( $g_config{Spooler_NSCA_Timeout} ) ? $g_config{Spooler_NSCA_Timeout} : 10;

    # Build the options to invoke send_nsca, without invoking an intermediate shell to do so.
    # Note that we would generally like to set the -wp option as well, but we defer that setting
    # to the send_nsca.cfg configuration file (wide_plugin_output) to better accommodate varying
    # local conditions.
    # FIX MINOR:  Also supply a "-to" option?
    @send_nsca = (
	$g_config{Spooler_NSCA_Program}, '-H', $target, '-p', $g_config{Spooler_NSCA_Port},
	'-od', '-c', $g_config{Spooler_NSCA_Config}
    );
    if ( $g_config{Spooler_NSCA_Timeout} ) {
	push @send_nsca, '-to', $g_config{Spooler_NSCA_Timeout};
    }

    my $oldblockset = POSIX::SigSet->new;
    my $newblockset = POSIX::SigSet->new(SIGCHLD);
    ## FIX MINOR:  die() won't log; address that
    sigprocmask( SIG_BLOCK, $newblockset, $oldblockset ) or die "FATAL:  Could not block SIGCHLD ($!),";

    # Fork a new process to send data to send_nsca.  Meanwhile, we can read the output of the child,
    # which is inherited by send_nsca, to capture the send_nsca output.  This is a rather unusual
    # construction -- being able to both send to a target child process and read its output.  It works
    # only because we have prepared all the input we want to send to the target process before we fork the
    # child, so no further feedback is needed between the parent and target child as a result of reading
    # the target's output.
    if ( !defined( $kidpid = open( FROM_CHILD, '-|' ) ) ) {

	# Restore the old signal mask so we can reap zombies once again.
	# FIX MINOR:  die() won't log; address that
	sigprocmask( SIG_SETMASK, $oldblockset ) or die "FATAL:  Could not restore SIGCHLD signal ($!),";

	## Return error.
	$logger->error("ERROR:  Could not fork a new process for send_nsca:  $!");
	return 0;
    }
    elsif ( $kidpid == 0 ) {
	## Child.

	# Make the child its own process group leader, so we can easily kill a whole tree of descendants.
	# Note that this call may fail because the parent has already set this (see below), but that's okay.
	# FIX MINOR:  The fact that we're making the child its own process group leader means it won't
	# receive any SIGTERM or SIGINT that is sent to the process group of the parent spooler process,
	# when GDMA as a whole is requested to terminate.  That means we should have in place SIGTERM and
	# SIGINT handlers in the parent process (not here in the child process) that will forward the
	# incoming signal to this child's process group, taking care of any race condition regarding
	# setting the child process to be a process group leader.
	if ( !POSIX::setpgid( 0, 0 ) ) {
	    $logger->debug("DEBUG:  setpgid() in child process failed ($!)");
	}

	# Restore the old signal mask so we can reap zombies once again.
	# FIX MINOR:  die() won't log; address that
	sigprocmask( SIG_SETMASK, $oldblockset ) or die "FATAL:  Could not restore SIGCHLD signal ($!),";

	## Handle to write onto pipe to send_nsca process.
	my $nsca_handle;
	my $ret_val;
	my $exit_code;

	# Reflect the command to the log file, for easy manual testing.
	$logger->debug( "DEBUG:  " . join( ' ', @send_nsca ) );

	# Invoke send_nsca using open, get its handle, and write our results to be sent using print.
	$ret_val = open( $nsca_handle, '|-', @send_nsca );
	if ( not defined $ret_val ) {
	    ## This is a command execution error.
	    $logger->error("ERROR:  Could not invoke send_nsca successfully:  $!");

	    # Terminate.  Use POSIX::_exit rather than exit().
	    # It is safer on unices when exiting from child processes.
	    # Reference:  "perldoc perlfork" - "CAVEATS AND LIMITATIONS" (although that refers to Perl's
	    # fork() emulation when the real OS-level fork() call is not available, not here where it is)
	    POSIX::_exit 1;
	}
	else {
	    $logger->trace("TRACE:  Invoked send_nsca successfully ...");

	    # Write results to be processed to the pipe, which acts as input to send_nsca.
	    # This print (or the subsequent close, which might really be where the final i/o happens)
	    # could fail with a SIGPIPE.  We put in place a signal handler so we don't die immediately
	    # if we do receive such a signal, so we can print a log message explaining our demise.
	    local $SIG{PIPE} = sub { die "Caught a SIGPIPE signal!\n"; };
	    eval {
		$ret_val = print $nsca_handle @$chunk;
		if ( $ret_val == 0 ) {
		    $logger->error("ERROR:  Could not write to send_nsca handle:  $!");
		    POSIX::_exit 1;
		}

		if ( not close $nsca_handle ) {
		    if ($!) {
			## Something went wrong on our side of the connection (the pipe).
			$logger->error("ERROR:  Close on send_nsca failed:  $!");
		    }
		    else {
			## Something went wrong on the send_nsca side of the connection.
			## We cannot easily capture STDOUT from the send_nsca invocation here in the child
			## process, but the parent may do so and mirror those messages to the log file as well.
			## If send_nsca terminated with a non-zero exit status, $! will have been forced to 0
			## (leading us into this branch), and $? will now be the send_nsca wait status.
			## We can at least capture and record that here.
			$logger->error("ERROR:  send_nsca failed with " . wait_status_message($?) . ".");
		    }
		    ## We don't need to exit with 1; for simplicity, we use the same exit code
		    ## that send_nsca failed with.
		}
	    };
	    if ($@) {
		chomp $@;
		$logger->error("ERROR:  Could not write to send_nsca handle:  $@");
		POSIX::_exit 1;
	    }

	    # The upper byte of the wait status contains the exit status of the process.
	    $exit_code = $? >> 8;
	    POSIX::_exit $exit_code;
	}
    }
    else {
	## Parent.

	# Avoid potential race conditions in the parent process (this branch) by setting the process group
	# of the child here to make it a process group leader, even though it will also be set in the child.
	# One of these two calls (the setpgid() in the child, above, or this setpgid() in the parent) will
	# fail, but that won't matter.  See APITUE2/e, page 270 for the rationale.
	#
	# A POSIX::EACCES failure is okay (the child did the dirty work first).
	if ( !POSIX::setpgid( $kidpid, 0 ) && $! != POSIX::EACCES ) {
	    ## ESRCH (pid is not the current process and not a child of the current process) is the bad error
	    ## code we dread here, but that's why we call sigprocmask() around this call (to prevent the child
	    ## process from possibly being reaped and its PID being re-used before we can reference it here).
	    ## We usually see EACCES (an attempt was made to change the process group ID of one of the children
	    ## of the calling process and the child had already performed an execve()), but that should not
	    ## be considered an error in the present context.  (The child's own setpgid() call should have
	    ## occurred before such an execve() call, so it ought to be safe.)
	    $logger->debug("DEBUG:  setpgid() in parent of child failed ($!)");
	}

	## Check if the kid finishes within Spooler_NSCA_Timeout.
	## Kill it if it does not.
	$Spooler_NSCA_Timeout += 2;    # Force this to be non-zero, and allow for send_nsca start/stop time as well.

	my @child_stdout = ();

	$signal_name = undef;

	do {
	    local $SIG{ALRM} = \&catch_signal;
	    eval {
		alarm($Spooler_NSCA_Timeout);
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
		chomp $@;
		$logger->error("ERROR:  $@");
	    }
	};
	$logger->debug("DEBUG:  Wait for child process completed at " . time . ".");

	# Check if the kid terminated on its own before timeout.
	if ( defined($signal_name) && $signal_name eq 'SIGALRM' ) {
	    ## We timed out.
	    ##
	    ## We must kill our kid and grandchildren, if any, so they don't continue to hang around after
	    ## the timeout has expired.  The child process that we fork()ed should have in turn spawned at
	    ## least one new descendant process, namely send_nsca.  We need to make sure that all such
	    ## descendants are cleaned up.  So we will send SIGTERM to the entire child process group.
	    ## This has to be done before we close the FROM_CHILD filehandle, because the close() will
	    ## wait for the child process to be gone before it returns.
	    kill TERM => -$kidpid;
	    $logger->error("ERROR:  send_nsca process took too long.  Throttled.");
	}

	# Restore the old signal mask so we can reap zombies once again.
	# FIX MINOR:  die() won't log; address that
	sigprocmask( SIG_SETMASK, $oldblockset ) or die "FATAL:  Could not restore SIGCHLD signal ($!),";

	# This close() call will internally perform the required waitpid() call to clean up the <defunct>
	# process, whether it ran to completion on its own or was killed by our action above, so calling
	# the waitpid() routine ourselves above would have been counterproductive.
	# FIX MINOR:  Theoretically, the child process could close its STDOUT stream, getting us out of
	# the alarm() region above, then take a long time to finally exit().  That additional wait would
	# be borne here, but without benefit of a timeout and the possibility of sending a SIGTERM if the
	# wait takes too long.  The code here ought to be rejiggered to account for that possibility
	# (which is very unlikely in the case of send_nsca, but could be an issue in the general case).
	if ( not close FROM_CHILD ) {
	    if ($!) {
		## Something went wrong on our side of the connection (the pipe).
		$logger->error("ERROR:  Close on child process failed:  $!");

		# Return an error.
		$status = 0;
	    }
	    else {
		## Something went wrong on the child process side of the connection (likely, send_nsca
		## itself exited with a non-zero exit status, and our child process then turned around
		## and exited with the same exit status).  If our child process terminated with a
		## non-zero exit status, $! will have been forced to 0 (leading us into this branch),
		## and $? will now be the child process wait status.  We can at least capture and
		## record that here.
		$logger->error("ERROR:  Child process failed with " . wait_status_message($?) . ".");
		## send_nsca (and thus our own child process, in turn) exits with "0" if everything goes well.
		$status = ( $? == 0 ) ? 1 : 0;
	    }
	}
	else {
	    ## A successful run.
	    $status = 1;
	}

	if (@child_stdout) {
	    my $stdout_string = join( '', @child_stdout );
	    chomp $stdout_string;
	    $logging->log_message("================================================================");
	    $logging->log_message("send_nsca output:");
	    $logging->log_message("----------------------------------------------------------------");
	    $logging->log_message($stdout_string);
	    $logging->log_message("================================================================");
	}
    }
    return $status;
}

################################################################################
#
#   send_nsca_windows()
#
#   Invokes send_nsca as a new process, on windows, to transmit
#   the chunk of results passed on.  Makes sure that the invoked send_nsca
#   terminates within the configured timeout.
#   Returns 1 if transmission was successful, 0 otherwise.
#
#   Arguments:
#   $chunk  - A reference to chunk of results to be processed.
#   $target - The target to send the results to.
#
################################################################################
sub send_nsca_windows {
    my $chunk          = shift;
    my $target         = shift;
    my $send_nsca_args = "";
    my $exec_string    = "";
    my $ret            = 0;
    my $timedout       = 0;

    my $head_path            = GDMA::Utils::get_headpath();
    my $tmp_batch_file       = "$head_path\\tmp\\batch.txt";
    my $tmp_output_file      = "$head_path\\tmp\\send_nsca_output.txt";
    my $job                  = Win32::Job->new;
    my %opts                 = ( no_window => "1", stdin => $tmp_batch_file, stdout => $tmp_output_file, stderr => $tmp_output_file );
    my $nsca_prog            = "$g_config{Spooler_NSCA_Program}";
    my $Spooler_NSCA_Timeout = defined( $g_config{Spooler_NSCA_Timeout} ) ? $g_config{Spooler_NSCA_Timeout} : 10;

    # Write the chunk to an intermediate text file.
    # We will make send_nsca pick the results from this file.
    my $old_umask = umask 0133;
    if ( not open( NSCABATCH, '>', $tmp_batch_file ) ) {
	$logger->error("ERROR:  Failed to open intermediate batch file \"$tmp_batch_file\":  $!.");
	umask $old_umask;
	return 0;
    }
    umask $old_umask;
    if ( not print( NSCABATCH @$chunk ) ) {
	$logger->error("ERROR:  Failed to write to intermediate batch file \"$tmp_batch_file\":  $!.");
	return 0;
    }

    # We are done with the text file for now.
    close NSCABATCH;

    $send_nsca_args = "-H $target -p $g_config{Spooler_NSCA_Port}";
    $send_nsca_args .= " -od -c \"$g_config{Spooler_NSCA_Config}\"";
    if ( $g_config{Spooler_NSCA_Timeout} ) {
	$send_nsca_args .= " -to $g_config{Spooler_NSCA_Timeout}";
    }
    $exec_string = "\"$nsca_prog\" $send_nsca_args";
    ## $logger->trace("TRACE:  The send_nsca command string is:  $exec_string");

    # Spawn the send_nsca process.
    my $child_pid = $job->spawn( $nsca_prog, $exec_string, \%opts );
    if ( not defined $child_pid ) {
	## This is a send_nsca execution error.
	my $os_error = "$!";
	$os_error .= " ($^E)" if "$^E" ne "$!";
	$logger->error("ERROR:  Failed to spawn the send_nsca process:  $os_error");
	$logger->error("        Failed command was:  $exec_string");
	unlink($tmp_output_file);
	return 0;
    }

    # Wait for the send_nsca process to complete for at most Spooler_NSCA_Timeout seconds.
    # Note that we explicitly wait for ALL processes in the job to finish, not just the
    # arbitrary first one to complete (even though we only expect there to be just one).
    $Spooler_NSCA_Timeout += 2;    # Force this to be non-zero, and allow for send_nsca start/stop time as well.
    $ret = $job->run( $Spooler_NSCA_Timeout, 1 );
    if ($ret) {
	## Record the exit code of the process.
	## $job->status() returns a hash with the job PIDs as the keys.
	## Each value in this hash is a subhash with keys "exitcode" and "time".
	## NOTE:  The Win32::Job doc says that status() returns a hash, not a hashref.
	## Let's see how well this assumption of a hashref actually works.
	my $status = $job->status();
	my $pid    = ( keys(%$status) )[0];
	if ( $pid ne $child_pid ) {
	    $logger->error("ERROR:  send_nsca child_pid=$child_pid while calculated pid=$pid");
	}
	$ret = ( $$status{$pid}{exitcode} == 0 ) ? 1 : 0;
	if ( $ret == 0 ) {
	    my $os_error = "$!";
	    $os_error .= " ($^E)" if "$^E" ne "$!";
	    $logger->error("ERROR:  send_nsca execution failed:  $os_error");
	}
    }
    else {
	## Timeout.  Wipe out all the processes.
	$job->kill();
	$timedout = 1;
	$logger->error("ERROR:  send_nsca process took too long.  Throttled.");
    }

    my @child_stdout = ();
    if ( !open( SENDOUTPUT, '<', $tmp_output_file ) ) {
	if ($ret) {
	    my $os_error = "$!";
	    $os_error .= " ($^E)" if "$^E" ne "$!";
	    $logger->error("ERROR:  Failed to open temporary send_nsca output file ($tmp_output_file):  $os_error");
	}
    }
    else {
	@child_stdout = <SENDOUTPUT>;
	if (not close SENDOUTPUT) {
	    my $os_error = "$!";
	    $os_error .= " ($^E)" if "$^E" ne "$!";
	    $logger->error("ERROR:  Failed to close temporary send_nsca output file ($tmp_output_file):  $os_error");
	}
    }
    if (@child_stdout) {
	my $stdout_string = join( '', @child_stdout );
	chomp $stdout_string;
	$logging->log_message("================================================================");
	$logging->log_message("send_nsca output:");
	$logging->log_message("----------------------------------------------------------------");
	$logging->log_message($stdout_string);
	$logging->log_message("================================================================");
    }

    # Clean up the temporary files we created.
    if ( not unlink($tmp_batch_file) ) {
	my $os_error = "$!";
	$os_error .= " ($^E)" if "$^E" ne "$!";
	$logger->error("ERROR:  Could not remove the intermediate batch file \"$tmp_batch_file\":  $os_error");
    }
    if ( not unlink($tmp_output_file) ) {
	my $os_error = "$!";
	$os_error .= " ($^E)" if "$^E" ne "$!";
	$logger->error("ERROR:  Could not remove the send_nsca output file \"$tmp_output_file\":  $os_error");
    }
    return $ret;
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
#   install_autoconfig_filepaths()
#
#   Sets up the server config file path.
#
#   Arguments:
#   $my_headpath - Head path for the agent.
#
################################################################################
sub install_autoconfig_filepaths {
    ## The command line arguments override.
    return $g_opt{a} if ( $g_opt{a} );

    my $my_headpath            = shift;
    my $autoconfigfile         = "NULL";
    my $autoconfigoverridefile = "NULL";
    my $my_osname              = $^O;

    # Set the filenames based on the operating system.
    if ( ( $my_osname eq 'linux' ) or ( $my_osname eq 'solaris' ) or ( $my_osname eq 'aix' ) or ( $my_osname eq 'hpux' ) ) {
	$autoconfigfile         = "$my_headpath/config/gdma_auto.conf";
	$autoconfigoverridefile = "$my_headpath/config/gdma_override.conf";
    }
    elsif ( $my_osname eq 'MSWin32' ) {
	$autoconfigfile         = "$my_headpath\\config\\gdma_auto.conf";
	$autoconfigoverridefile = "$my_headpath\\config\\gdma_override.conf";
    }

    return ( $autoconfigfile, $autoconfigoverridefile );
}

################################################################################
#
#   install_hostconfig_filepaths()
#
#   Sets up the host config file path.
#
#   Arguments:
#   $my_headpath            - Head path for the agent.
#   $Use_Long_Hostname      - The configured Use_Long_Hostname value, if any.
#   $Forced_Hostname        - The configured Forced_Hostname value, if any.
#   $Use_Lowercase_Hostname - The configured Use_Lowercase_Hostname value, if any.
#
################################################################################
sub install_hostconfig_filepaths {
    ## The command line arguments override.
    return $g_opt{c} if ( $g_opt{c} );

    my $my_headpath            = shift;
    my $Use_Long_Hostname      = shift;
    my $Forced_Hostname        = shift;
    my $Use_Lowercase_Hostname = shift;
    my $my_osname              = $^O;
    my $hostconfigfile         = "NULL";
    my $hostname               = GDMA::Utils::my_hostname( $Use_Long_Hostname, $Forced_Hostname, $Use_Lowercase_Hostname );

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
#   detect_conf_file_change()
#
#   Checks if the config file has changed since last iteration, by doing
#   a stat on it and recording the modification time.
#
#   Arguments:
#   $filePath      - Full path of the config file to be detected for changes.
#   $last_modified - A reference.  The last modified time for the config file.
#                    This will be updated if the config file is found updated.
#   $modified      - A reference.  This will be set to 1 if the config file is
#                    found updated.
#   $optional      - A flag to tell whether this config file might not exist.
#
################################################################################
sub detect_conf_file_change {
    my ( $filePath, $last_modified, $modified, $optional ) = @_;

    $logger->debug("DEBUG:  Checking config file \"$filePath\" for a change.");
    $$modified = 0;

    # Stat the disk file to get the modification time.
    # stat returns an array of file attributes.  10th element is
    # the file modification time.
    my $mtime = ( stat("$filePath") )[9];

    if ( defined($mtime) ) {
	if ( $$last_modified != 0 ) {
	    if ( $mtime > $$last_modified ) {
		## The config file has changed since we last checked.
		$logger->debug("DEBUG:  $filePath:  config file change detected.");

		# Update the file modification time.
		$$last_modified = $mtime;
		$$modified      = 1;
	    }
	}
	else {
	    ## If last_modified is 0, this is the first time we've seen the file.
	    ## Set the file modification time.  Also, this is grounds for an
	    ## autoconfig action to ensure that we employ its contents.
	    $$last_modified = $mtime;
	    $$modified      = 1;
	}
    }
    else {
	## If we cannot stat the file, that might be okay if it's optional -- it might
	## simply not exist at all.  We could check the error code to be sure, but that
	## might be platform-dependent, so until we test for the values across platforms,
	## we won't do that.
	if ( $$last_modified or not $optional ) {
	    ## No config file detected, but either it used to exist or we always need it;
	    ## so we want to trigger autoconfig mode.
	    $logger->warn("WARNING:  detect_conf_file_change:  failed to stat the \"$filePath\" config file ($!).");
	    $$last_modified = 0;
	    $$modified      = 1;
	}
    }
}

################################################################################
#
#   reload_config()
#
#   Reads the config file, checks syntax and validity of the values set, by
#   calling validate_config(), a GDMA::Utils subroutine.  It updates the global
#   %g_config hash only if the new configfile passes the syntax and validation
#   check.  It inserts an error message into internal buffer if the configfile
#   fails the sanity check.
#   Returns 1 on successful reading and validation, 0 otherwise.
#
#   Arguments:
#   $AutoconfigfilePath         - Full path of the main autoconfig file.
#   $AutoconfigOverridefilePath - Full path of the autoconfig override file.
#   $HostconfigfilePath         - Reference to the full path of the host config file.
#   $auto_config - This is an output parameter, that triggers auto config mode.
#                  Set to 1, if the host config file does not exist, or if
#                  autoconfig flag is set in the hostconfig.
#   $normal_spool_filename - required to spool the corrupt config file message.
#   $first_reload          - A flag that indicates whether we are reloading for
#                            the first time.
#
################################################################################
sub reload_config {
    my ( $AutoconfigfilePath, $AutoconfigOverridefilePath, $HostconfigfilePath, $auto_config, $normal_spool_filename, $head_path, $first_reload )
      = @_;
    my %tmp_config = ();
    my $ret_val    = 0;
    my $errstr;
    my $config_lock_file;
    my $read_config;
    my $config_lockh;

    # Record the debug level.
    my $debug = defined( $g_opt{d} ) ? $g_opt{d} : 0;

    $$auto_config = 0;
    $logger->debug("DEBUG:  Reloading config files.");

    # First, read the main and override autoconfig files into a temp hash.
    if ( !GDMA::Utils::read_config( $AutoconfigfilePath, \%tmp_config, \$errstr, 0 ) ) {
	## Log error and bail.
	$logger->fatal("FATAL:  Failed to read the main autoconfig file:  $errstr");
	die "Failed to read the main autoconfig file:  $errstr\n";
    }
    if ( !GDMA::Utils::read_config( $AutoconfigOverridefilePath, \%tmp_config, \$errstr, 1 ) ) {
	## Log error and bail.
	$logger->fatal("FATAL:  Failed to read the autoconfig override file:  $errstr");
	die "Failed to read the autoconfig override file:  $errstr\n";
    }

    ## Before we reload, we must recalculate whether to use a long-form or short-form hostname in
    ## the path, because the situation might have changed since this script was first started.
    my $Use_Long_Hostname = defined( $tmp_config{Use_Long_Hostname} ) ? $tmp_config{Use_Long_Hostname} : 'on';
    $$HostconfigfilePath =
      install_hostconfig_filepaths( $head_path, $Use_Long_Hostname, $tmp_config{Forced_Hostname}, $tmp_config{Use_Lowercase_Hostname} );
    if ( -f $$HostconfigfilePath ) {
	$tmp_config{Use_Long_Hostname} = $Use_Long_Hostname if not defined $tmp_config{Use_Long_Hostname};
    }
    elsif ( not defined $tmp_config{Use_Long_Hostname} ) {
	$$HostconfigfilePath = install_hostconfig_filepaths(
	    $head_path,
	    $tmp_config{Use_Long_Hostname},
	    $tmp_config{Forced_Hostname},
	    $tmp_config{Use_Lowercase_Hostname}
	);
    }

    # Read the host config file; let it overwrite the temp hash values.
    # Get blocking lock over the config file, poller might be pulling it.
    $config_lock_file = GDMA::Utils::get_config_lock_filename( GDMA::Utils::get_headpath() );
    my $old_umask = umask 0133;
    if ( !open( $config_lockh, '>', $config_lock_file ) ) {
	$logger->error("ERROR:  reload_config:  Failed to open the config lockfile \"$config_lock_file\":  $!.");
	umask $old_umask;
	return $ret_val;
    }
    umask $old_umask;
    if ( !GDMA::Utils::get_lock($config_lockh) ) {
	$logger->error("ERROR:  reload_config:  Failed to acquire config file lock.");
	close($config_lockh);
	return $ret_val;
    }

    # got the lock, now read the host config file.
    $read_config = GDMA::Utils::read_config( $$HostconfigfilePath, \%tmp_config, \$errstr, 0 );

    # We are done with the config lock for now.  Surrender it.
    if ( not GDMA::Utils::release_lock($config_lockh) ) {
	$logger->error("ERROR:  reload_config:  Could not relinquish the config file lock:  $!");
	## We think we are never going to get the lock again.  May as well quit.
	## But first, we close the file (which should in itself release the lock).
	## We need to perform this close() in case the die() is caught by an
	## enclosing eval{}; block, to prevent a file descriptor leak.
	close($config_lockh);
	die "Could not relinquish the config file lock:  $!\n";
    }
    if ( !close($config_lockh) ) {
	$logger->error("ERROR:  reload_config:  Could not close the lock file.");
    }

    if ($read_config) {
	$logger->debug("DEBUG:  Successfully read host config file.");

	# Check the syntax and validate the values.
	if ( GDMA::Utils::validate_config( \%tmp_config, \$errstr ) ) {
	    $logger->debug("DEBUG:  Host config file syntax is ok.");

	    # The config file passed the sanity check.  Deep copy the temporary
	    # hash into the global config hash.  We want the actual values to be
	    # copied over and not the references.
	    %g_config = %{ dclone( \%tmp_config ) };
	    dump_config() if $debug == 2;

	    # Set the auto_config flag to whatever is configured.
	    $$auto_config = ( $g_config{Enable_Auto} =~ /^on$/i );

	    if ($$auto_config) {
		$logger->warn( "WARNING:  Auto-config mode (controlled by the Enable_Auto option) remains in effect"
		      . " after host externals have been read.  In this mode, service check results may be queued in"
		      . " $normal_spool_filename but will never be sent to the server.  Most likely, you must change"
		      . " your host externals to set the Enable_Auto option to \"off\"." );
	    }

	    $ret_val = 1;
	}
	else {
	    chomp $errstr;
	    $logger->error("ERROR:  The host config file is corrupt:  $errstr");
	    $ret_val = 0;

	    # Record the corruption error for logging later.
	    my $corruption_err = $errstr;

	    # The first reload failed.  We can't continue processing without a valid config,
	    # so we switch back to auto-config mode.
	    if ($first_reload) {
		$$auto_config = 1;

		# We need to read the auto conf again and copy its contents to
		# %g_config.  We cannot directly copy tmp_config, as it now
		# contains the corrupt host config.  We could have previously
		# copied the contents of auto conf, however in case of a corrupt
		# config we do not always switch to autoconfigure mode.
		# For first reload we have previously read the auto conf file,
		# however if we do not read here again, modifications made in
		# auto conf will not be read.
		%tmp_config = ();
		if ( !GDMA::Utils::read_config( $AutoconfigfilePath, \%tmp_config, \$errstr, 0 ) ) {
		    ## Log error and bail.
		    $logger->fatal("FATAL:  Failed to read the main autoconfig file:  $errstr");
		    die "Failed to read the main autoconfig file:  $errstr\n";
		}
		if ( !GDMA::Utils::read_config( $AutoconfigOverridefilePath, \%tmp_config, \$errstr, 1 ) ) {
		    ## Log error and bail.
		    $logger->fatal("FATAL:  Failed to read the autoconfig override file:  $errstr");
		    die "Failed to read the autoconfig override file:  $errstr\n";
		}
		%g_config = %{ dclone( \%tmp_config ) };
	    }
	    if ( $$auto_config == 1 ) {
		## We don't process the spool file in autoconfig mode.
		## Send the config corrupt message immediately.
		send_config_corrupt_message($corruption_err);
	    }
	    else {
		## Spool the config corrupt message.
		spool_config_corrupt_message( $normal_spool_filename, $corruption_err );
	    }
	}
    }
    else {
	## read_config() failed.  Report error.
	$logger->error("ERROR:  reload_config:  $errstr");
	$ret_val = 0;

	# No hostconfig file on disk; trigger autoconfig mode.
	$logger->debug("DEBUG:  reload_config:  setting autoconfig mode.");
	$$auto_config = 1;

	# FIX MAJOR:  Do we need to re-read the auto-conf file here, or otherwise grab
	# the copy we read before, because %tmp_config might now be modified from what
	# was previously in the auto-conf file?

	# We could not read host config file so we are switching to
	# autoconfigure mode.  This function is called when one of the config
	# files or both have been modified.  If we were already in autoconfigure
	# mode and we do not copy here, we miss out on modifications made in
	# auto conf.  If we are switching from normal mode and we do not copy,
	# the g_config would contain previous host config parameter values.
	%g_config = %{ dclone( \%tmp_config ) };
    }
    return $ret_val;
}

################################################################################
#
#  dump_config()
#
#  Prints the contents of config structure
#
################################################################################
sub dump_config {
    $logger->debug("DEBUG:  The configuration file contains:");

    # Sort for readability during debug
    foreach my $param ( sort keys %g_config ) {
	if ( ref( $g_config{$param} ) eq "ARRAY" ) {
	    foreach ( my $i = 0 ; $i <= $#{ $g_config{$param} } ; $i++ ) {
		foreach my $option ( keys %{ $g_config{$param}->[$i] } ) {
		    $logging->log_message( $param . "[$i]_$option = " . $g_config{$param}->[$i]->{$option} );
		}
	    }
	}
	else {
	    $logging->log_message( $param . " = " . $g_config{$param} );
	}
    }
}

################################################################################
#
#   legacy_run_autoconfig_mode()
#
#   This function writes the autoconfig message into an internal buffer and
#   transmits it using send_nsca.  The autoconfig result is for the configured
#   "GDMA_Auto_Service" on GDMA host and is sent to "Target_Server".
#
################################################################################
sub legacy_run_autoconfig_mode {
    my $head_path = shift;
    my $ipaddress = undef;
    my $msg_payload;
    my $errstr;
    my $packed_ip   = undef;
    my @to_sendnsca = ();

    # The return code for autoconfig message is "UNKNOWN".
    my $return_code_unknown = 3;
    my $hostname = GDMA::Utils::my_hostname( $g_config{Use_Long_Hostname}, $g_config{Forced_Hostname}, $g_config{Use_Lowercase_Hostname} );
    my $osname   = $^O;

    $logger->debug("DEBUG:  Spool processor is running in auto config mode.");

    # Record the IP address for the host.
    $packed_ip = gethostbyname($hostname);

    # Check if we could resolve the hostname to IP address.
    if ( defined($packed_ip) ) {
	$ipaddress = inet_ntoa($packed_ip);
    }
    else {
	$logger->error("ERROR:  Failed to resolve hostname $hostname");
    }

    # Build the autoconfig message body.
    $msg_payload = "No configuration file in spooler:  $hostname";

    # Append the IP address if we could resolve it.
    if ( defined($ipaddress) ) {
	$msg_payload .= " [$ipaddress]";
    }
    $msg_payload .= " running Perl compiled under $osname $Config{osvers}";

    # Add a bit more data if it's windows
    if ( $osname eq 'MSWin32' ) {
	my $osres = `cscript -nologo "$head_path\\libexec\\v2\\get_system_uptime.vbs" -h $hostname -w 2 -c 1`;
	$msg_payload .= " $osres";
    }
    $logger->trace("TRACE:  legacy_run_autoconfig_mode:  $msg_payload");

    # Compose the auto config message and transmit.
    my $result_str = join( '',
	time(), "\t", $g_config{GDMA_Auto_Host},
	"\t", $g_config{GDMA_Auto_Service},
	"\t", $return_code_unknown, "\t", $msg_payload, "\n" );

    push( @to_sendnsca, $result_str );

    # When running in autoconfig mode we do not process the spool file
    # and we need to send the auto config message immediately.
    # If we spool it, it will be delivered only when spool file is processed
    # (i.e., in normal mode).
    # Though we are sending only one result, we use an array
    # as the function send_nsca expects an array reference.
    # Extract the target address from the "Target_Server" directive.
    # Send the autoconfig message to the first target server.
    my $target = ( split( /[,\s]+/, $g_config{Target_Server} ) )[0];

    # E.g., https://abc.def/gdma-linux -> abc.def
    # E.g., https://abc.def            -> abc.def
    if ($target =~ m{^\S+://([^/]+)}) {
	$target = $1;
    }

    if ( !send_nsca( \@to_sendnsca, $target ) ) {
	## Transmission of auto configure message failed.
	## We can't do anything; just log the message.
	$logger->warn("WARNING:  Failed to transmit auto configure message; it will be lost.");
    }
}

################################################################################
#
#   spool_config_corrupt_message()
#
#   Spools the corrupt config message.
#   One copy of this message is destined to be sent to GDMA_Auto_Host
#   for GDMA_Auto_Service, another to all the primary targets for Spooler_Service.
#   Administrator will configure the DNS so that one host will be "GDMA_Auto_Host".
#   The return code for this message will be "critical".
#
#   Arguments:
#   $normal_spool_filename - required to spool the config corrupt message.
#   $corruption_error      - A string explaining the type of corruption found.
#
################################################################################
sub spool_config_corrupt_message {
    my ( $normal_spool_filename, $corruption_error ) = @_;
    my @result_buf = ();
    my $msg_payload;

    # "0" implies that the result is to be sent to all the primary targets.
    my $default_target = 0;
    my $errstr;
    my $num_results;

    # The return code for corrupt config message  is "CRITICAL".
    my $return_code_crit = 2;
    my $blocking         = 1;
    my $osname           = $^O;
    my $ipaddress        = undef;
    my $packed_ip        = undef;
    my $hostname = GDMA::Utils::my_hostname( $g_config{Use_Long_Hostname}, $g_config{Forced_Hostname}, $g_config{Use_Lowercase_Hostname} );

    $logger->debug("DEBUG:  Spooling configuration corrupt message.");

    # Record the IP address for the host.
    $packed_ip = gethostbyname($hostname);

    # Check if we could resolve the hostname to IP address.
    if ( defined($packed_ip) ) {
	$ipaddress = inet_ntoa($packed_ip);
    }
    else {
	$logger->error("ERROR:  Failed to resolve hostname $hostname");
    }

    # Compose the corrupt config message.
    $msg_payload = "Error processing configuration file:  $corruption_error.";
    $msg_payload .= " On $hostname";
    if ( defined($ipaddress) ) {
	$msg_payload .= " [$ipaddress]";
    }
    $msg_payload .= " running Perl compiled under $osname $Config{osvers}";
    $logger->trace("TRACE:  spool_config_corrupt_message:  $msg_payload");

    # Insert both the messages into the result buffer.
    insert_into_result_buf( $g_config{GDMA_Auto_Host}, $hostname, $g_config{GDMA_Auto_Service}, $return_code_crit, $msg_payload, \@result_buf );

    insert_into_result_buf( $default_target, $hostname, $g_config{Spooler_Service}, $return_code_crit, $msg_payload, \@result_buf );
    if ( !GDMA::Utils::spool_results( $normal_spool_filename, \@result_buf, $blocking, \$num_results, \$errstr ) ) {
	## This is essentially loss of data.
	$logger->warn("WARNING:  $errstr");
	$logger->warn( 'WARNING:  Failed to respool ' . @result_buf . ' result' . ( @result_buf == 1 ? '; it' : 's; they' ) . ' will be lost.' );
    }
}

################################################################################
#
#   send_config_corrupt_message()
#
#   Sends the corrupt config message immediately.
#   The message is sent for "gdma_auto" service and to "GDMA_Auto_Host".
#   This function is called when spool processor needs to enter autoconfig mode
#   because of config file corruption.
#   The return code for this message will be "critical".
#
#   Arguments:
#   $corruption_error - A string explaining the type of corruption found.
#
################################################################################
sub send_config_corrupt_message {
    my $corruption_error = shift;
    my @result           = ();
    my $msg_payload;
    my $result_str;

    # The return code for corrupt config message  is "CRITICAL".
    my $return_code_crit = 2;
    my $osname           = $^O;
    my $ipaddress        = undef;
    my $packed_ip        = undef;
    my $hostname         = GDMA::Utils::my_hostname( $g_config{Use_Long_Hostname}, $g_config{Forced_Hostname}, $g_config{Use_Lowercase_Hostname} );

    $logger->debug("DEBUG:  Sending configuration corrupt message.");

    # Record the IP address for the host.
    $packed_ip = gethostbyname($hostname);

    # Check if we could resolve the hostname to IP address.
    if ( defined($packed_ip) ) {
	$ipaddress = inet_ntoa($packed_ip);
    }
    else {
	$logger->error("ERROR:  Failed to resolve hostname $hostname");
    }

    # Compose the corrupt config message.
    $msg_payload = "Error processing configuration file:  $corruption_error.";
    $msg_payload .= " On $hostname";
    if ( defined($ipaddress) ) {
	$msg_payload .= " [$ipaddress]";
    }
    $msg_payload .= " running Perl compiled under $osname $Config{osvers}";
    $logger->trace("TRACE:  send_config_corrupt_message:  $msg_payload");

    # Build the result string for which we can directly invoke sned_nsca.
    $result_str = join( '', time(), "\t", $hostname, "\t", $g_config{GDMA_Auto_Service}, "\t", $return_code_crit, "\t", $msg_payload, "\n" );

    # send_nsca() expects a reference to an array.  Hence copy the result into an array.
    push @result, $result_str;

    if ( !send_nsca( \@result, $g_config{GDMA_Auto_Host} ) ) {
	## Transmission of corrupt configure message failed.
	## We can't do anything; just log.
	$logger->warn("WARNING:  Failed to transmit corrupt configure message; it will be lost.");
    }
}

################################################################################
#
#   spool_startup_info()
#
#   Spools a startup message, containing information about spool processor
#   version and startup time.
#   This message is meant for the GW monitor server.
#   The return code for this message is "Ok".
#
#   Arguments:
#   $normal_spool_filename - required to spool the startup message.
#
################################################################################
sub spool_startup_info {
    my $normal_spool_filename = shift;
    my @result_buf            = ();

    # Start-up message body.
    my $msg_payload;

    # "0" implies that the result is to be sent to all the targets.
    my $default_target = 0;
    my $blocking       = 1;
    my $errstr;

    # The return code for start-up message  is "OK".
    my $return_code_ok = 0;
    my $num_results;
    my $version = get_version();

    # Record the debug level.
    my $hostname = GDMA::Utils::my_hostname( $g_config{Use_Long_Hostname}, $g_config{Forced_Hostname}, $g_config{Use_Lowercase_Hostname} );

    $logger->debug("DEBUG:  Spooling spool processor startup message.");

    # Compose the startup message.
    $msg_payload = "Spool processor $version started at " . GDMA::Utils::get_current_time_str();
    $logger->trace("TRACE:  spool_startup_info:  $msg_payload");

    # Use the default target.
    # The result will be for the configured "Spooler_Service".
    # The return code for startup message will be "0" i.e., "OK".
    insert_into_result_buf( $default_target, $hostname, $g_config{Spooler_Service}, $return_code_ok, $msg_payload, \@result_buf );

    # Spool the start up message.
    if ( !GDMA::Utils::spool_results( $normal_spool_filename, \@result_buf, $blocking, \$num_results, \$errstr ) ) {
	## This is essentially loss of data.
	$logger->warn("WARNING:  $errstr");
	$logger->warn( 'WARNING:  Failed to respool ' . @result_buf . ' result' . ( @result_buf == 1 ? '; it' : 's; they' ) . ' will be lost.' );
    }
}

################################################################################
#
#   spool_heart_beat_message()
#
#   Compose a heart beat message and spool it.
#   This will be delivered to target host in next iteration as we have already
#   read and processed the spool file.
#   The return code for this message is "Ok".
#
#   Arguments:
#   $normal_spool_filename    - spool file name to respool failed transmissions.
#   $result_transmitted_count - no. of results transmitted to all targets.
#   $loop_start_time          - to calculate the loop execution time.
#   $hires_time_format        - Available high resolution time format.
#   $hires_time               - Reference to available high resolution time subroutine
#
#################################################################################
sub spool_heart_beat_message {
    my ( $normal_spool_filename, $results_transmitted_count, $loop_start_time, $hires_time_format, $hires_time ) = @_;
    my $blocking = 1;
    my $num_results;
    my $errstr;
    my $msg_payload;
    my $default_target = 0;

    # The return code for heartbeat message  is "OK".
    my $return_code_ok = 0;
    my @result_buf     = ();
    my $hostname       = GDMA::Utils::my_hostname( $g_config{Use_Long_Hostname}, $g_config{Forced_Hostname}, $g_config{Use_Lowercase_Hostname} );
    my $service        = $g_config{Spooler_Service};
    my $loop_execution_time = &$hires_time() - $loop_start_time;

    $logger->debug("DEBUG:  Spooling heart beat message.");

    # Compose the heartbeat message.
    # A heartbeat message, sent per iteration, includes the
    # number of results transmitted in the iteration and the time
    # it took to transmit them.
    $msg_payload = "Spooler transmitted $results_transmitted_count results in ";
    $msg_payload .= sprintf( $hires_time_format, $loop_execution_time ) . " secs ";
    $msg_payload .= "| RESULTS=$results_transmitted_count;;;; TIME=";
    $msg_payload .= sprintf( $hires_time_format, $loop_execution_time ) . "s;;;;";
    $logger->trace("TRACE:  Spooler heart beat message:  $msg_payload");

    insert_into_result_buf( $default_target, $hostname, $service, $return_code_ok, $msg_payload, \@result_buf );

    if ( !GDMA::Utils::spool_results( $normal_spool_filename, \@result_buf, $blocking, \$num_results, \$errstr ) ) {
	## This is essentially loss of data.
	$logger->warn("WARNING:  $errstr");
	$logger->warn( 'WARNING:  Failed to respool ' . @result_buf . ' result' . ( @result_buf == 1 ? '; it' : 's; they' ) . ' will be lost.' );
    }
}

################################################################################
#
#   spool_retries_rejection_message()
#
#   Compose a message retries rejection message and spool it.
#   This message is meant for GWMonitor.
#   The return code for this message is "Warning".
#   This will be delivered to target host in next iteration as we have already
#   read and processed the spool file.
#
#   Arguments:
#   $normal_spool_filename   - spool file name to respool failed transmissions.
#   $retries_rejection_count - no. of results rejected as they exceeded
#                              the max_retries mentioned in config file.
#
#################################################################################
sub spool_retries_rejection_message {
    my ( $normal_spool_filename, $retries_rejection_count ) = @_;
    my $blocking = 1;
    my $num_results;
    my $errstr;
    my $msg_payload;
    my $default_target = 0;

    # The return code for rejection message is "WARNING".
    my $return_code_warn = 1;
    my @result_buf       = ();
    my $hostname = GDMA::Utils::my_hostname( $g_config{Use_Long_Hostname}, $g_config{Forced_Hostname}, $g_config{Use_Lowercase_Hostname} );
    my $service  = $g_config{Spooler_Service};

    $logger->debug("DEBUG:  $retries_rejection_count results rejected because they were tried too many times.");

    # Compose the rejection message.
    $msg_payload = "Max retries $g_config{Spooler_Max_Retries} reached ";
    $msg_payload .= "for $retries_rejection_count messages, messages purged.";
    $logger->trace("TRACE:  Spooler retry rejection message:  $msg_payload");
    insert_into_result_buf( $default_target, $hostname, $service, $return_code_warn, $msg_payload, \@result_buf );

    if ( !GDMA::Utils::spool_results( $normal_spool_filename, \@result_buf, $blocking, \$num_results, \$errstr ) ) {
	## This is essentially loss of data.
	$logger->warn("WARNING:  $errstr");
	$logger->warn( 'WARNING:  Failed to respool ' . @result_buf . ' result' . ( @result_buf == 1 ? '; it' : 's; they' ) . ' will be lost.' );
    }
}

################################################################################
#
#   spool_age_rejection_message()
#
#   Compose a message max age rejection message and spool it.
#   The return code for this message is "Warning".
#   This will be delivered to target host in next iteration as we have already
#   read and processed the spool file.
#
#   Arguments:
#   $normal_spool_filename - spool file name to respool failed transmissions.
#   $age_rejection_count   - no. of results rejected as they exceeded
#                            the max retention time  mentioned in config file.
#
#################################################################################
sub spool_age_rejection_message {
    my ( $normal_spool_filename, $age_rejection_count ) = @_;
    my $blocking = 1;
    my $num_results;
    my $errstr;
    my $msg_payload;
    my $default_target = 0;

    # The return code for rejection message  is "WARNING".
    my $return_code_warn = 1;
    my @result_buf       = ();
    my $hostname = GDMA::Utils::my_hostname( $g_config{Use_Long_Hostname}, $g_config{Forced_Hostname}, $g_config{Use_Lowercase_Hostname} );
    my $service  = $g_config{Spooler_Service};

    $logger->debug("DEBUG:  $age_rejection_count results rejected because they were too old.");

    # Compose the rejection message.
    $msg_payload = "Retention timer $g_config{Spooler_Retention_Time} reached ";
    $msg_payload .= "for $age_rejection_count messages, messages purged ";
    $logger->trace("TRACE:  Spooler age rejection message:  $msg_payload");
    insert_into_result_buf( $default_target, $hostname, $service, $return_code_warn, $msg_payload, \@result_buf );

    if ( !GDMA::Utils::spool_results( $normal_spool_filename, \@result_buf, $blocking, \$num_results, \$errstr ) ) {
	## This is essentially loss of data.
	$logger->warn("WARNING:  $errstr");
	$logger->warn( 'WARNING:  Failed to respool ' . @result_buf . ' result' . ( @result_buf == 1 ? '; it' : 's; they' ) . ' will be lost.' );
    }
}

################################################################################
#
#   spool_transmission_failure_message()
#
#   Compose a transmission failure message and spool it.
#   It is meant for GWMonitor and gdma_auto_host.
#   The return code for this message is "Critical".
#   This will be delivered to target host in next iteration as we have already
#   read and processed the spool file.
#
#   Arguments:
#   $normal_spool_filename      - spool file name to respool failed transmissions.
#   $transmission_failure_count - no. of results whose transmission failed
#                                 for below target.
#   $target                     - host for which trasmission of checks has failed.
#
#################################################################################
sub spool_transmission_failure_message {
    my ( $normal_spool_filename, $failed_results_count, $target ) = @_;
    my $blocking = 1;
    my $num_results;
    my $errstr;
    my $msg_payload;
    my $default_target = 0;

    # The return code for transmission failure message is "CRITICAL".
    my $return_code_crit = 2;
    my @result_buf       = ();
    my $hostname  = GDMA::Utils::my_hostname( $g_config{Use_Long_Hostname}, $g_config{Forced_Hostname}, $g_config{Use_Lowercase_Hostname} );
    my $service   = $g_config{Spooler_Service};
    my $ipaddress = undef;
    my $packed_ip = undef;

    # Get the ip address of the host, for which transmission failed.
    $packed_ip = gethostbyname($target);

    # Check if we could resolve the hostname to IP address.
    if ( defined($packed_ip) ) {
	$ipaddress = inet_ntoa($packed_ip);
    }
    else {
	$logger->error("ERROR:  Failed to resolve hostname $target");
    }

    # Compose the transimission failure message.
    # We need to spool this for GDMA_Auto_Host and GWMonitor.
    $msg_payload = "Failed to transmit $failed_results_count results ";
    if ( defined($ipaddress) ) {
	$msg_payload .= "to $ipaddress ";
    }
    else {
	$msg_payload .= "to $target ";
    }

    $logger->trace("TRACE:  Spooler transmission failure message:  $msg_payload");
    insert_into_result_buf( $g_config{GDMA_Auto_Host}, $hostname, $g_config{GDMA_Auto_Service}, $return_code_crit, $msg_payload, \@result_buf );
    insert_into_result_buf( $default_target, $hostname, $service, $return_code_crit, $msg_payload, \@result_buf );

    if ( !GDMA::Utils::spool_results( $normal_spool_filename, \@result_buf, $blocking, \$num_results, \$errstr ) ) {
	## This is essentially loss of data.
	$logger->warn("WARNING:  $errstr");
	$logger->warn( 'WARNING:  Failed to respool ' . @result_buf . ' result' . ( @result_buf == 1 ? '; it' : 's; they' ) . ' will be lost.' );
    }
}

################################################################################
#
#   insert_into_result_buf()
#
#   Inserts the message passed as a result into a buffer passed.
#   Assumes the default value for retries field.
#
#   Arguments:
#   $target     - The target host for the result.
#   $host       - The hostname for which this is a result.
#   $service    - The service name for which this is a result.
#   $ret_code   - Return code for the results.
#   $msg_body   - The message text.
#   $result_buf - A reference to a buffer containing results.
#
################################################################################
sub insert_into_result_buf {
    my ( $target, $host, $service, $ret_code, $msg_body, $result_buf ) = @_;

    # The retries field should be 0, when the result is first spooled.
    my $default_retries = 0;

    # Use default value for retries.
    my $result_str =
      join( '', $default_retries, "\t", $target, "\t", time(), "\t", $host, "\t", $service, "\t", $ret_code, "\t", $msg_body, "\n" );

    # Push it into result buffer.
    push( @$result_buf, $result_str );
    chomp $result_str;
    $logging->log_message("TRACE:  Pushed result into the buffer:\n$result_str") if $trace;
}

################################################################################
#
#   handle_cmd_line()
#
#   Handles the command line options to the program.
#   Sets global flags as per the options specified.
#
################################################################################
sub handle_cmd_line {
    my $version    = get_version(1);
    my $helpstring = "
GDMA Version $version

The GDMA Spool Processor picks up results from the spool file
and sends them back to the GroundWork server(s) using NSCA.

Options:
-c <CONFIG FILE>    Config file containing monitoring parameters.
-a <AUTOCONF FILE>  File with default settings.  Must contain a target server address.
-l <LOG DIR>        Log directory for this script, relative to GDMA HOME.
-d <DEBUGLEVEL>     Debug mode.  Will log additional messages to the log file;
		    <DEBUGLEVEL> should be 1 for moderate logging, or 2 for additional detail.
-h                  Displays help message.
-x                  Run once.  If this option is not selected, run continually with sleep.
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

    getopts( "d:hivxc:a:l:", \%g_opt );
    if ( $g_opt{h} ) { print $helpstring;               exit; }
    if ( $g_opt{v} ) { print "GDMA version $version\n"; exit; }
    if ( $g_opt{l} ) {
	## Override the default value set earlier.
	$g_config{Logdir} = $g_opt{l};
    }
    if ( $g_opt{d} ) {
	if ( ( $g_opt{d} != 1 ) and ( $g_opt{d} != 2 ) ) {
	    die "$g_opt{d} is not a supported debug level.  Choose 1 or 2.\n";
	}
    }
}

################################################################################
#
#   get_version()
#
#   Returns the version number of the Spool processor program.
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

__END__


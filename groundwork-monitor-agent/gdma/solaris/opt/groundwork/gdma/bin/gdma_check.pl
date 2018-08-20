#!/usr/bin/perl -w --
#
#	Copyright 2003-2007 Groundwork Open Source, Inc.
#	http://www.groundworkopensource.com
#
#	Unless required by applicable law or agreed to in writing, software
#	distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
#	WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
#	License for the specific language governing permissions and limitations under
#	the License.

##
##	Dependencies
##

#use strict;
use Fcntl;
use IO::Socket;
use Data::Dumper;
use POSIX qw(setsid);
use Errno qw(EAGAIN);

# We'd like to use Time::HiRes, but it's not dependably installed
# on all platforms we might want to monitor.  So we build a portable
# mechanism for finding time to whatever resolution is available.

my $hires_time;
my $hires_time_format;
eval {require Time::HiRes; import Time::HiRes;};
if ($@) {
    # 'require' died; Time::HiRes is not available.
    $hires_time = sub { return time; };
    $hires_time_format = "%0.0f";
} else {
    # 'require' succeeded; Time::HiRes was loaded.
    $hires_time = sub { return Time::HiRes::time(); };
    $hires_time_format = "%0.3f";
}

use Getopt::Std;
use Sys::Hostname;
use sigtrap qw(die normal-signals);

##
##	Local Variables
##

my $host;
my $Logfile;
my $ServerfilePath;
my $ConfigfilePath;
my $Configfile;
my %opt				= ();
my $cycle_time			= 300;	# Default time interval for each poll - 5 minutes
my $num_cycles_until_get_config	= 10;
my $debug			= 0;
my $daemon			= 1;
my $start_program_time		= time;

my $os = `uname -s`;
chomp $os;
my $solaris = ($os eq 'SunOS');
my $prefix;
if ($solaris) {
    $prefix = "/opt";
} else {
    # For Linux, and for all other platforms until we extend this code.
    $prefix = "/usr/local";
}
my $head_path			= "$prefix/groundwork/gdma";
my $local_spool_filename	= "$prefix/groundwork/gdma/spool/gdma.spool";

$ENV{'PATH'}.=":$head_path/libexec";
if ($solaris) {
    $ENV{'LD_LIBRARY_PATH'} = "/usr/sfw/lib:/usr/local/ssl/lib:$prefix/groundwork/lib";
}

my %return_codes	= ("OK" => "0","WARNING" => "1","CRITICAL" => "2","UNKNOWN" => "3" );
my %exit_codes 		= ("0" => "0","256" => "1","512" => "2","768" => "3" , "-1"=>"512");
my $helpstring		= "
This script will monitor system statistics on this server.
Options:
-c <CONFIG FILE>  Config file containing monitoring parameters.
-l <LOG FILE>     Log file for this script.
-d <1,2>          Debug mode. Will log additional messages to the log file, 1 less 2 most.
-h or -help       Displays help message.
-i                Run as an interactive process instead of as a daemon.
-x                Run once.  If this option not selected, run continually with sleep.

Copyright 2003-2007 Groundwork Open Source, Inc.
http://www.groundworkopensource.com
Unless required by applicable law or agreed to in writing, software distributed under the License
is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
or implied. See the License for the specific language governing permissions and limitations under
the License.
";

my $fqhn		= hostname();
@host_parseout		= split(/\./, $fqhn);
my $hostname		= $host_parseout[0];
my $wait_for_cfg_sleep	= 60;

##
##	Handle Command Line Options
##

getopts("id:hxc:",\%opt);
if ($opt{h} or $opt{help}) {
    print $helpstring;
    exit;
}
if ($opt{i}) {
    $daemon = 0;
}
if ($opt{d}) {
    $debug = $opt{d};
}
if ($opt{c}) {
    # FIX LATER:  What do we use for the ServerfilePath in this case?  Do we need another command-line option?
    $ServerfilePath = undef;
    $ConfigfilePath = $opt{c};
} else {
    if ($solaris) {
	$ServerfilePath = "$prefix/groundwork/home/gdma/config/gdma_server.conf";
	$ConfigfilePath = "$prefix/groundwork/home/gdma/config/gwmon_$hostname.cfg";
    } else {
	$ServerfilePath = "$head_path/config/gdma_server.conf";
	$ConfigfilePath = "$head_path/config/gwmon_$hostname.cfg";
    }
}
($Configfile = $ConfigfilePath) =~ s#.*/##;	# strip leading pathname components, leaving just the filename

########################################################################
##	Handle Shutdown Cleanly
##
##	We need to arrange for clean shutdown -- not just ourself, but
##	also any descendant processes.  To do that, we arrange to be a
##	process group leader, and when we are told to shut down, we pass
##	along the message to all of our descendants.
########################################################################

sub do_fork {
    my $retries = 5;
    while (--$retries >= 0) {
	my $pid;
	if ($pid = fork) {
	    # successful fork; we're in the parent
	    return $pid;
	} elsif (defined $pid) {
	    # successful fork; we're in the child
	    return 0;
	} elsif ($! == EAGAIN) {
	    # unsuccessful but supposedly recoverable fork error; wait, then loop around and try again
	    sleep 5;
	} else {
	    # weird fork error
	    die "Cannot fork: $!\n";
	}
    }
}

sub make_daemon {
    # Make ourself immune to background job control write checks.
    $SIG{TTOU} = 'IGNORE';
    $SIG{TTIN} = 'IGNORE';
    $SIG{TSTP} = 'IGNORE';

    # We ought to close all open file descriptors, especially stdin, stdout, stderr. 
    # FIX LATER:  figure out of we can reliably figure out what other channels might be open, and close them all.
    close STDIN;
    close STDOUT;
    close STDERR;

    # Disassociate from our process group and controlling terminal.
    my $retries = 5;
    if (do_fork()) {
	# successful fork; we're in the parent
	exit 0;
    }
    # parent has exited, child remains; make it a session leader (not just a process group leader);
    # the preceding fork was necessary to guarantee that this call succeeds
    POSIX::setsid();

    ## # Do not reacquire a controlling terminal.  To ensure that, become immune from process group leader death ...
    ## $SIG{HUP} = 'IGNORE';
    ## # ... then become non-process-group leader. 
    ## if (do_fork()) {
    ##     # successful fork; we're in the parent
    ##     exit 0;
    ## }
    # But in fact we don't want to do that, because the whole point of our exercise here is to become
    # our own process group leader, so all descendants will be killed along with us when our process
    # group is killed.  So we'll just have to be careful not to reacquire a controlling terminal,
    # either by watching what actions we take (don't open any terminal devices), or by forking and
    # having the parent just sleep forever waiting for the shutdown signal to come in.

    # child has exited; grandchild remains 

    # Change current directory to '/'. 
    chdir '/';

    # Reset the file mode creation mask to an appropriate value,
    # to override whatever got inherited from the parent process.
    umask 022;
}

sub catch_shutdown_signal {
    # Shut down everything in our process group.  We intentionally use our own PID as the process group number
    # instead of asking explicitly for our process group number, because this ensures that we're not killing
    # arbitrary processes in case we're somehow not a process group leader when this function gets called.
    kill "TERM", -$$;
    # Killing our own process group won't necessarily kill us, so we need to deliberately exit.
    exit 1;
}

if ($daemon) {
    # First, go daemon; then arrange to have all descendant processes also killed when we go down.
    make_daemon();
    $SIG{TERM} = \&catch_shutdown_signal;
    $SIG{INT}  = \&catch_shutdown_signal;
}

########################################################################
##	Wait for Config File
##
##	When we first are executed, it is possible that we are executing
##	without a configuration file.  This is the circumstance where
##	we have just loaded the software on a new host.  So, we sit here
##	and wait for our configuration file before we start executing.
########################################################################

while (1) {
    if (open(CFG_TEST, '<', "$ConfigfilePath")) {
	close(CFG_TEST);
	last;
    }
    sleep($wait_for_cfg_sleep);

    # We cannot depend on any external agent grabbing the config file for us,
    # so we need to take that responsibility ourselves.  That raises the question,
    # "without a config file to tell us, how do we know what machine to contact
    # to read the config file from?".  The answer is, we look in the $ServerfilePath
    # configuration file to find the IP address of that server.
    fetch_config_file (server_address ($ServerfilePath));
}

########################################################################
##	Open LogFile
##
##	Note, it is closed after the main loop is executed and before the sleep
##	Also note that it is only the very first config file read after startup,
##	or the default value, that is ever used as the logfile name and to
##	determine from where to find fetch configuration files; no later
##	config-file settings will have any effect.
########################################################################

my $logconfig = read_config($ConfigfilePath) || {};
$logconfig->{Output_Logfile} ||= "$head_path/log/gwmon_$hostname.log";

########################################################################
#	Loop for running as a daemon, depending on the startup flag option "-x".
#	Note that the LOG file is named above so as to be available for opening
#	and writing time stamp
########################################################################

my $loop_start_time;
my $loopcount = 0;
my $num_cycles = 0;

while (1) {
    open(LOG, '>', "$logconfig->{Output_Logfile}");

    $loop_start_time = &$hires_time();
    &main;
    my $exec_time = &$hires_time() - $loop_start_time;	# to help compute the time to wait before the next execution
    print LOG "Loop count=$loopcount. Last loop exec time = " . sprintf($hires_time_format, $exec_time) . " seconds.\n";
    if($opt{x}) {last;}

    ##
    ##	If it's time to sleep, do so.
    ##

    ++$loopcount;
    if ($exec_time < $cycle_time) {
	my $wait_time = int($cycle_time - $exec_time);
	print LOG "Waiting $wait_time seconds...\n" if $debug;
	close LOG;
	sleep $wait_time;
    }

    ++$num_cycles;
    if ($num_cycles >= $num_cycles_until_get_config) {

	# We'd prefer to use a Config_Server option instead of Target_Server here, so on boxes without SSH support,
	# if our initial config file doesn't contain a Config_Server, we would never attempt to update our
	# configuration file.  That's okay on a box where we don't want to install or use SSH for such purposes.
	# In that situation, the config file would need to be manually managed by the local administrator.
	# With the current setup, we will instead go through a series of ultimately fruitless machinations
	# resulting in a failed "scp" down below, so the configuration file should remain unchanged.

	# Target_Server may be a list, not just a single host, so by convention we need to extract and use just the first host.
	fetch_config_file((split(/[,\s]+/, $logconfig->{Target_Server}))[0]) if (defined($logconfig->{Target_Server}));

	$num_cycles = 0;
    }
}
exit 0;

########################################################################
# go grab a copy of the config-server's IP address
########################################################################

sub server_address {
    my $serverfile = shift;
    my $ip_address = undef;

    if (defined($serverfile) && open(CONFIGSERVER, '<', "$serverfile")) {
	# We only expect a single line, containing an IP address with no adornment.
	while (<CONFIGSERVER>) {
	    chomp;
	    # Let's validate the address before we assume it's correct, to avoid script-injection attacks.
	    # Currently, we only allow literal IPv4 addresses, not IPv6 addresses or hostnames.
	    if (/^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$/) {
		$ip_address = $_;
		last;
	    }
	}
	close(CONFIGSERVER);
    }
    return $ip_address;
}

########################################################################
# go grab a copy of the current config file from our config server
########################################################################

sub fetch_config_file {
    my $configserver = shift;
    if (defined($configserver)) {
	my $cmd_string = join ('', $head_path, "/bin/gdma_getconfig.pl -H ", $configserver, " -c ", $Configfile);
	`$cmd_string 2>&1`;
    }
}

########################################################################
# the main executed from the loop.  Note that the config file is opened
# every time so a configuration change will happen immediately.
########################################################################

sub main {

    ##
    ##	Configuration file and create reference for $config
    ##

    my $config = read_config($ConfigfilePath);
    if (! defined($config)) {
	# The read_config() routine itself complains but doesn't know if LOG is open,
	# so it cannot record the problem there.  In our present context, however,
	# we do have that luxury.
	print LOG "Can't open configuration file $ConfigfilePath\n";
	return;
    }

    ##
    ##	print config values
    ##

    if ($debug)
    {
	foreach my $param (keys %{$config})
	{
	    if (ref($config->{$param}) eq "ARRAY")
	    {
		foreach (my $i=0; $i<=$#{$config->{$param}}; $i++)
		{
		    # FIX MINOR:  this looks really suspicious to me.  we just found
		    # above that $config->{$param} was an array ref, not a hash ref;
		    # so what are we doing dereferencing it like a hash reference?
		    foreach my $option (keys %{$config->{$param}->[$i]})
		    {
			print LOG $param."[$i]_$option = ".$config->{$param}->[$i]->{$option}."\n" if $debug;
		    }
		}
	    }
	    else
	    {
		print LOG $param." = ".$config->{$param}."\n" if $debug;
	    }
	}
    }

    ########################################################################
    ## Set defaults if not set in configuration file.
    ########################################################################

    $config->{Output_Logfile} ||= "$head_path/log/gwmon_$hostname.log";
    $config->{Monitor_Host}   ||= $hostname;
    # FIX MINOR:  using a fixed name here doesn't make sense; shouldn't we just return, instead?
    $config->{Target_Server}  ||= "groundwork1";
    $config->{Loop_Count}     ||= $cycle_time;

    ########################################################################
    ## adopt new values from the config file
    ########################################################################

    $cycle_time = $config->{Loop_Count};

    ########################################################################
    #	Start executing system checks if defined in the
    #	configuration file.
    ########################################################################

    my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
    $year += 1900;
    my $month = qw(January February March April May June July August September October November December)[$mon];
    my $timestring = sprintf "%02d:%02d:%02d",$hour,$min,$sec;
    my $thisday = qw(Sunday Monday Tuesday Wednesday Thursday Friday Saturday)[$wday];
    print LOG "GroundWork Monitoring Script starting on $thisday, $month $mday, $year at $timestring.\n" if $debug;
    print LOG "Debug set to $debug\n" if $debug;
    print LOG "Using configuration file $ConfigfilePath\n" if $debug;

    foreach my $checkname (keys %{$config})
    {
	if (defined($config->{$checkname})) {			# if in config file
	    if ( $checkname !~ /^Check_/i ) { next; }	# Only process if name starts with "Check_"
	    if (($checkname =~ /Check_Request_Servlet/) 	# See if special check
	    or  ($checkname =~ /Check_SAR/)) {
		Check_GENERIC_NOSEND($checkname);	# Execute but don't send_nsca. Handled in script
	    } else {
		Check_GENERIC($checkname);		# Generic, get check result and send to Nagios
	    }
	}
    }

    ########################################################################
    #	Finished executing system checks.
    ########################################################################

    # FIX MINOR:  is this useful, given that we're already logging the execution time in the caller?
    my $program_end_time = time;
    $program_end_time -= $start_program_time;
    print LOG "Total elapsed time since startup:  $program_end_time seconds.\n" if $debug;
}

########################################################################
#	Read configuration file
########################################################################

sub read_config {
    my $configfile = shift;
    if (!open(CONFIG, '<', "$configfile") ) {
	print "Can't open configuration file $configfile\n";
	return undef;
    }
    while (my $line = <CONFIG>) {
	# Discard comment and invalid lines
	if ($line=~/^\s*#/) {next }						# Comment if line starts with #
	if ($line=~/^\s*(\S+)\s*=\s*"(.*?)"/) {
	    my $parameter = $1; my $value = $2;
	    # Sample line:
	    #		Check_Disk[1]_Parm_--warning = "10%"
	    if ($parameter =~ /^(.*?)\[(\d+)\]_(.*)/) {
		$config->{$1}->[$2]->{$3} = $value;
	    } elsif ($parameter =~ /^(.*?)\[(\d+)\]$/) {
		$config->{$1}->[$2] = $value;
	    } else {
		$config->{$parameter} = $value;
	    }
	} elsif ($line=~/^\s*(\S+)\s*=\s*(\S+)/) {
	    #
	    #  Set to another parameter that has already been defined. No quotes after =
	    #	Monitor_Server[1]= "groundwork.company.com"
	    # 	Check_Response_Servlet[1]_Parm_-n = Monitor_Server[1]
	    #	Also support multiples, ie:
	    # 	Check_Response_Servlet[1]_Parm_-n = Monitor_Server[1],Monitor_Server[2]
	    #
	    my $parameter = $1; my $target = $2;
	    my @targetparameters = split /,/,$target;
	    print "target = $2\n" if $debug;
	    foreach my $targetparameter (@targetparameters) {
		my $value = "";
		print "Checking targetparameter=$targetparameter\n" if $debug;
		if ($targetparameter =~ /^(.*?)\[(\d+)\]_(.*)/) {
		    $value = $config->{$1}->[$2]->{$3};
		} elsif ($targetparameter =~ /^(.*?)\[(\d+)\]$/) {
		    $value = $config->{$1}->[$2];
		} else {
		    $value = $config->{$targetparameter};
		}
		print "value = $value\n" if $debug;
		if ($value) {
		    if ($parameter =~ /^(.*?)\[(\d+)\]_(.*)/) {
			$config->{$1}->[$2]->{$3} .= $value.",";
		    } elsif ($parameter =~ /^(.*?)\[(\d+)\]$/) {
			$config->{$1}->[$2] .= $value.",";
		    } else {
			$config->{$parameter} .= $value.",";
		    }
		    print "setting parameter $parameter \n" if $debug;
		}
	    }
	    if ($parameter =~ /^(.*?)\[(\d+)\]_(.*)/) {	# get rid of trailing comma
		$config->{$1}->[$2]->{$3} =~ s/,$//;
	    } elsif ($parameter =~ /^(.*?)\[(\d+)\]$/) {
		$config->{$1}->[$2] =~ s/,$//;
	    } else {
		$config->{$parameter} =~ s/,$//;
	    }
	} elsif ($line=~/^\s*(\S+)\s*#?/) {
	    my $parameter = $1;
	    # Sample line:
	    #		Check_Disk[1]_Parm_--errors-only
	    if ($parameter =~ /^(.*?)\[(\d+)\]_(.*)/) {
		$config->{$1}->[$2]->{$3} = "";
	    } elsif ($parameter =~ /^(.*?)\[(\d+)\]$/) {
		$config->{$1}->[$2] = "";
	    } else {
		$config->{$parameter} = "";
	    }
	}
    }
    close CONFIG;
    return \%{$config};
}

########################################################################
#	Generic plugin processing
########################################################################

sub Check_GENERIC
{
    my $check_name = shift;
    print "check_name=$check_name\n" if $debug;
    foreach (my $i=1; $i<=$#{$config->{$check_name}}; $i++) {
	my $exit = undef;
	my $text = "";
	my $execute_string = "";
	if ($config->{$check_name}->[$i]->{Enable} ne "OFF") {
	    print LOG "Executing $check_name - iteration $i\n" if $debug;
	    my $execute_string = $config->{$check_name}->[$i]->{Command};
	    foreach my $option (keys %{$config->{$check_name}->[$i]}) {
		#if ($config->{$check_name}->[$i]->{$option}) {
		if (defined($config->{$check_name}->[$i]->{$option})) {
		    print LOG $check_name."[$i]_$option = ".$config->{$check_name}->[$i]->{$option}."\n"  if $debug;
		    if ($option =~ /^Parm_(--.*)/) {
			$execute_string .= " $1=".$config->{$check_name}->[$i]->{$option};
		    } elsif ($option =~ /^Parm_(-.*)/) {
			$execute_string .= " $1 ".$config->{$check_name}->[$i]->{$option};
		    }
		} else {
		    if ($option =~ /^Parm_(--.*)/) {
			$execute_string .= " $1";
		    } elsif ($option =~ /^Parm_(-.*)/) {
			$execute_string .= " $1";
		    }
		}
	    }
	    print LOG "PLUGIN COMMAND STRING: $execute_string \n" if $debug;
	    my @lines = `$execute_string 2>&1`;
	    $exit=$?;	# Exit code
	    $text = "";
	    if ($exit < 0) {
		$text = "Plugin execution error";
	    }
	    else {
		$num_lines=scalar @lines;
		$line_num=0;

		foreach $line (@lines)
		{
		    $line_num=$line_num+1;
		    if ($debug > 1) { print LOG "PLUGIN OUTPUT: $line"; }
		    if ($line =~ /ERROR/) {
			$exit = "512";		# If ERROR found, force status to CRITICAL
		    }
		    if ($line =~ /(\S.*)/) {
			$text .= $1;
			if ($line_num < $num_lines) {
			    $text .= "/";
			}
		    }
		}
	    }
	    if ($debug > 1) {
		print LOG "PLUGIN RESULTS: exit=$exit; text=$text \n";
	    }

	    # <gwhost name>[tab]<service description>[tab]<return code> [tab]<plugin output> | <performance metrics>
	    # echo "<send command text>" | send_nsca -H <monitor server> -c send_nsca.cfg`;

	    my @target_servers = split(/[,\s]+/, $config->{Target_Server});
	    my $num_target_servers = scalar(@target_servers);
	    my $target_server;
	    my $ts;

	    $text =~ s/\r/\//og;
	    $text =~ s/\n/\//og;
	    $text =~ s/\t/\//og;
	    $text =~ s/\"/'/og;
	    #$text =~ s/\|/\//og;

	    for ($ts=0; $ts < $num_target_servers; $ts++)
	    {
		$target_server = $target_servers[$ts];

		my $send_string = join ('',
		    "echo \"",
		    $config->{Monitor_Host},
		    "\t",
		    $config->{$check_name}->[$i]->{Service},
		    "\t",
		    $exit_codes{$exit},
		    "\t",
		    $text,
		    " \" | ",
		    $head_path,
		    "/bin/send_nsca.pl -t 10 -p 5667 -H ",
		    $target_server);

		##
		## Now send it to Nagios.
		##

		print LOG "SEND STRING: $send_string\n" if $debug;
		send_to_nagios($send_string);
	    }
	}
    }
}

########################################################################
#	Generic plugin processing
#		Executes plugin but doesn't send output.
#		The plugin will execute the send_nsca
########################################################################

sub Check_GENERIC_NOSEND {
    my $check_name = shift;
    foreach (my $i=1; $i<=$#{$config->{$check_name}}; $i++) {
	my $exit = undef; my $text = "";
	my $execute_string = "";
	if ($config->{$check_name}->[$i]->{Enable} ne "OFF") {
	    print LOG "Executing $check_name - iteration $i\n" if $debug;
	    my $execute_string = $config->{Plugin_Directory};
	    $execute_string .= "/".$config->{$check_name}->[$i]->{Command};
	    foreach my $option (keys %{$config->{$check_name}->[$i]}) {
		#if ($config->{$check_name}->[$i]->{$option}) {
		if (defined($config->{$check_name}->[$i]->{$option})) {
		    print LOG $check_name."[$i]_$option = ".$config->{$check_name}->[$i]->{$option}."\n" if $debug;
		    if ($option =~ /^Parm_(--.*)/) {
			$execute_string .= " $1=".$config->{$check_name}->[$i]->{$option};
		    } elsif ($option =~ /^Parm_(-.*)/) {
			$execute_string .= " $1 ".$config->{$check_name}->[$i]->{$option};
		    }
		} else {
		    if ($option =~ /^Parm_(--.*)/) {
			$execute_string .= " $1";
		    } elsif ($option =~ /^Parm_(-.*)/) {
			$execute_string .= " $1";
		    }
		}
	    }
	    print LOG "PLUGIN COMMAND STRING: $execute_string \n" if $debug;
	    my @lines = `$execute_string 2>&1`;
	    $text = "";
	    foreach $line (@lines) {
		if ($debug > 1) { print LOG "PLUGIN OUTPUT: $line"; }
		if ($line =~ /(\S.*)/) {
		    $text .= $1."\n";
		}
	    }
	    print LOG "PLUGIN RESULTS: text=$text \n" if $debug;
	}
    }
}

########################################################################
#	send_to_nagios
#
#	Send plug-in output to nagios through send_nsca. If send_nsca
#	fails, the plug-in output is spooled to a local file for retry
#	later.
########################################################################

sub send_to_nagios
{
    my $send_string = shift;
    my $send_result;

    #
    # First, empty our spool file if it exists.
    #

    # FIX MINOR:  I object to the current handling of the spool file on five counts:
    # (1) we let our spool file build up forever, never concluding that we should
    #     no longer accumulate into it or send its content to the central server,
    #     in case the central server is down or unreachable for a long time
    # (2) there are far too many individual calls to send_nsca (one per spooled line,
    #     instead of bunching up some part of the spooled results, up to some maximum
    #     message-size limit of perhaps 100 KB, for each call); this is inefficient
    #     from the standpoint of overhead in both forking and opening an excessive
    #     number of network connections, and it also leaves that many more sockets
    #     on the client machine in a TIME_WAIT state when send_nsca closes them
    # (3) if the loop fails after sending a few previously spooled results,
    #     there is no recognition that some of the results were successfully sent;
    #     and thus those results will be re-sent at a future time, which may result
    #     in some out-of-sequence results being reported
    # (4) there is no aging of results, such that sufficiently-old results are
    #     considered to be irrelevant and will be dropped so they don't just clog up
    #     the downstream processing
    # (5) since we don't save a timestamp with each spooled line, we cannot tell how
    #     old that data is when we do finally send it, which means the central server
    #     may be inundated with a lot of completely obsolete data sent as if it were
    #     the current state, and immediately overwritten by later state
    # These problems are obvious targets for improvement in a future release.
    if (-e $local_spool_filename)
    {
	open(EMPTYSPOOL, '<', "$local_spool_filename");
	print LOG "Emptying spool file\n";

	$send_result = 1;	# delete an empty spool file, if the loop has no iterations
	while (my $line = <EMPTYSPOOL>)
	{
	    chomp $line;
	    $send_result = send_nsca($line);
	    if (!$send_result) {
		last;
	    }
	}
	close(EMPTYSPOOL);
	if ($send_result) {
	    unlink($local_spool_filename);
	}
    }

    #
    #	Next, write our string to nsca.
    #

    $send_result = send_nsca($send_string);
    if (!$send_result) {
	local_spool($send_string);
    }
}

########################################################################
#	send_nsca
#
#	Perform the actual send to nsca.
########################################################################

sub send_nsca
{
    my $debug = 1;
    my $send_string = shift;
    my $OK = 0;
    my $try = 0;
    my $maxtry = 2;
    my $minwait = 2;
    my $maxwait = 5;	# will wait between 2 and 7 seconds

    while (!$OK and ($try < $maxtry))
    {
	my @lines = `$send_string 2>&1`;
	foreach $line (@lines)
	{
	    if ($line =~ /Sent \d+ packets to /)	# OK response for send_nsca perl script
	    {
		$OK = 1;
	    }
	}
	if ($debug)
	{
	    foreach $line (@lines)
	    {
		# $line should already contain a newline at the end
		print LOG "return = $line";
	    }
	}

	$try++;
	if (!$OK) {
	    print LOG "Failed attempt $try to send to nsca; sleeping.\n" if $debug;
	    sleep int(rand($maxwait)) + $minwait;
	}
    }
    return($OK);
}

sub local_spool
{
    my $line = shift(@_);

    if (open(SPOOL_FILE, '>>', "$local_spool_filename")) {
	if (print (SPOOL_FILE $line."\n")) {
	    print LOG "Spooled line: $line\n";
	} else {
	    print LOG "Unable to write to spool file\n";
	}
	close (SPOOL_FILE);
    } else {
	print LOG "Unable to open spool file\n";
    }
}

__END__

sub gettime {
    my $logtime = shift;
    ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst)=localtime($logtime);
    $year=$year+1900;
    $mon=$mon+1;
    $timet = sprintf "%04d-%02d-%02d %02d:%02d:%02d",$year,$mon,$mday,$hour,$min,$sec;
    $datet = sprintf "%04d-%02d-%02d",$year,$mon,$mday;
    return ($timet,$datet);
}

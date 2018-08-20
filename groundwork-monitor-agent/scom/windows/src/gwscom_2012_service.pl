#!perl 
#windows does not understand shebang
#
# This program reads the output of the SCOM 2012 universal runbook and transmits results to the groundwork spooler table
#  ie the scom_events table in the scom database
#
# Copyright 2012-13 GroundWork OpenSource
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# Revision History
#       Kevin Stone 07-Feb-2013 -  Initial Revision
#       Dominic Nicholas March 2013 -various revisions, including PerlSvc-ized
# two cases : reaper fails to start, service should not get stuck trying to start
#             reaper starts, service should not report failing starting
#
# Rev history
# 2.0.2 DN 6/24/15 - reviewed code - added logging, added detection of reaper process and restarting it if goes away, other cleanup

package PerlSvc;

use strict;
use warnings;
use File::Basename;
use Win32::Process;
use Win32::Process::Info;

# Default values
my $service = 'GWSCOM';
my $logging = 1;
my $auto_restarts_enabled = 1; # controls if detection/restarting of missing reaper process is done - TBD put into a config later

(my $progname = $0) =~ s/.*?([^\\]+?)(\.\w+)$/$1/;
my @options  = ( );
my $exe = PerlApp::exe(); 
my $scomdir = dirname($exe) ;
my $logfile = "$scomdir\\gwscom-service.log";

our(%Config,$Verbose);
my $VERSION = "2.0.2";

	

###############################################################################
#   get_options()
#   gets and validates the command line options for SCOM reaper service
#   Exits on error.
#
###############################################################################
sub get_options 
{
    require Getopt::Long;
    my @options = @_;
    my $usage = pop @options;
    $SIG{__WARN__} = sub { print "$usage\n$_[0]"; exit 1 };
    Getopt::Long::GetOptions(@options);
    $SIG{__WARN__} = 'DEFAULT';

    my $opterror=0;

    if (($logging != 0) and ($logging != 1)) {
       print ("Error - Logging option not supported. Use '0' or '1'");
    }

    exit 2 if $opterror == 1;
}


###############################################################################
#   unsupported ()
#   The --install and --remove options are implemented by PerlSvc and
#   cannot be simulated when running via `perl gwscom_2012_service.pl`
#   FIX MAJOR:  Actually, the --remove option, for instance, is also not
#   available when the compiled gwscom_2012_service.exe program is run from the
#   command line, at least under some circumstances.  We still get the
#   message specified here.  What gives?
#
###############################################################################
sub unsupported {
    my $option = shift;
    die "The '--$option' option is only supported in the compiled script.\n";
}

###############################################################################
#   configure ()
#   Sets-up SCOM reaper service configuration.
#
###############################################################################
sub configure {
    %Config = (ServiceName => $service,
	           DisplayName => "GWSCOM",
	           Description => "GroundWork SCOM 2012 Reaper");
}

###############################################################################
#   Interactive()
#   The Interactive() function is called whenever the service is run
#   from the command line, and none of the --install, --remove, or --help
#   options were used.
#
###############################################################################
sub Interactive {
    push(@options,
	 'help'    => \&Help,
	 'version' => \&Version,
	 'install' => \&unsupported,
	 'remove'  => \&unsupported);

    # Setup the %Config hash based on our configuration parameter
    configure();
    Startup();

}

###############################################################################
#   Startup()
#   This function is called automatically when the service starts.
#   It processes events.
###############################################################################
sub Startup {

    get_options(@options, <<__USAGE__);
Try `$progname --help` to get a list of valid options.
__USAGE__

    my ( $reaper_pid, $pi, $cycle, @pid, @pids, $exitcode, $try, $max_tries, $wait ) ;
    my $reaper_prog = "$scomdir\\gwscom_2012_reaper.exe";
    my $reaper_args = "";

    Log("$service service version $VERSION starting");

    # Launch the reaper
    if ( not launch_reaper_process( $reaper_prog, $reaper_args, \$reaper_pid ) ) {
        Log("Failed to start the reaper $exe process");
        return 0; # If the initial launch fails, then the service stops
    }

    # Keep on checking if the GWSCOM service is stopped.
    # While checking that, also check if the reaper is still running and restart it if not
    $cycle = 0;
    my $display_gone_msg = 1;
    while (1)
    {
        $cycle++;
	    sleep 1;
	    last if ( not ContinueRun(0) ); # if the service was stopped, then break this loop

        # check to see if the reaper process is still running 
        if ( not $cycle % 10 ) { # do this check every 10 cycles (10 seconds if sleep 1 above)

            $pi = Win32::Process::Info->new();
            @pid = ( $reaper_pid ) ;
            @pids = $pi->ListPids ( @pid ); # will return the  intersection of all pids with this one pid

            if ( not @pids ) {

                Log ("Reaper $exe process $reaper_pid has gone away") if $display_gone_msg ; #don't fill up the log file - just display this once when necessary
                $display_gone_msg = 0;

                if ( $auto_restarts_enabled ) {  # try and restart reaper if auto restarts is enabled
                    $try = 1; $max_tries = 5; $wait = 10; # TBD put in a config in future perhaps 
                    Log ("Attempting to re-launch $reaper_prog $reaper_args ...");
                    while ( (not launch_reaper_process( $reaper_prog, $reaper_args, \$reaper_pid ) ) and $try <= $max_tries )  { 
                        Log ("Failed to re-launch - try $try/$max_tries - waiting for $wait seconds before retrying");
                        sleep $wait;
                        $try++;
                    }
                    if ( $try > $max_tries ) {
                        Log ("Failed to re-launch after $max_tries tries - giving up - the $service service will now stop"); # monitoring processes externally a good thing
                        return 0; 
                    }
                    $display_gone_msg = 1; # its running again so allow gone away messages again
                }

            }
        }
    }

    # The service was stopped. Cleaup.
    Log("Reaper service stopped");

    # kill the reaper itself.
    Log("Killing reaper $exe process pid $reaper_pid");
    Win32::Process::KillProcess($reaper_pid, $exitcode);# second arg is the exit code - ie 0
    # the exit code will be the exit code of the reaper process

    #$reaper_obj->GetExitCode($exitcode);
    # do something with the exitcode here ??
    # There's not much documation on killprocess other than it kills the process and returns that proc's exit code


}

###############################################################################
sub launch_reaper_process
{
    # takes a reaper prog and args, tries to create a process for it, and returns the pid
    # returns 0 if fails, 1 otherwise
    my ( $reaper_prog, $reaper_args, $pid_ref, $fail ) = @_;
    my ( $reaper_obj );

    # For testing purposes you can pass in a return value - handy for testing auto restarting mechanism
    if ( defined $fail ) { return 0 ; }

    # Launch the reaper
    Log ("Creating Win32::Process for : $reaper_prog $reaper_args");
    if ( not Win32::Process::Create( $reaper_obj,                            # container object
				                    "$reaper_prog",                          # fully qualified exe name
				                    "$reaper_prog $reaper_args",             # exe name and args
				                     0,                                      # flag: inherit calling processes handles or not
				                     NORMAL_PRIORITY_CLASS|CREATE_NO_WINDOW, # creation flags
				                     $scomdir                                # working dir for process
                                    )  )  {
	    Log ("Failed to launch reaper process '$reaper_prog $reaper_args'");
	    return 0;
    }
    else { 
        ${$pid_ref} = $reaper_obj->GetProcessID();
        Log ("Reaper process $exe started ok with pid ${$pid_ref}")
    }


    return 1;
}

###############################################################################
#   Install()
#   This function is called when the service is installed.
#   Set-up the service configuration.
#
###############################################################################
sub Install {
    get_options('name=s' => \$service, @options, <<__USAGE__);
Valid --install suboptions are:

  auto       automatically start service / install service as Automatic type also
  --name     service name     [$service]

For example:

  $progname --install auto

__USAGE__

    configure();
}

###############################################################################
#   Remove()
#   This function is called when the service is dropped.
#
###############################################################################
sub Remove {
    get_options('name=s' => \$service, <<__USAGE__);
Valid --remove suboptions are:

  --name     service name                     [$service]

For example:

  $progname --remove
__USAGE__

    # Let's be generous and support `gwscom_2012_service --remove PingFoo` too:
    $service = shift @ARGV if @ARGV;

    $Config{ServiceName} = $service;
}

################################################################################
#
#   get_version()
#
#   Returns the version number of the SCOM reaper program.
#
#   Arguments:
#   $full - If true, include detail.
#
################################################################################
sub get_version {
    my $full    = shift;
    my $version = $VERSION;
    if ($full) {{
        last unless defined $PerlApp::VERSION;
	my $compile_time = PerlApp::get_bound_file("compile_time") or last;
	$version .= " ($compile_time)";
    }}
    return $version;
}

###############################################################################
#
#   Version()
#   Displays SCOM reaper version.
#
###############################################################################
sub Version {
    my $version = get_version(1);
    print "SCOM reaper service version $version\n";
    exit;
}
###############################################################################
#
#   Help()
#   Show help.
#
###############################################################################
sub Help {
    my $version = get_version(1);
    print <<__HELP__;
SCOM reaper version $version
This program will install and run the SCOM 2012 reaper as a system service.

    $progname
    --help - show this help (non service mode only)
    --version - show the version of this program (non service mode only)

To install as a service that is of service type "Automatic" ie automatically starts with the OS:

    $progname --install auto
    (note that this will automatically start the service).

To install as a service that is of service type "Manual" ie does not start automatically with the OS:

    $progname --install
    net start $service

You can pause and resume the service with:

    net pause $service
    net continue $service

To remove the service from your system, stop und uninstall it:

    net stop $service
    $progname --remove

Quicker still is just

    $progname --remove

which will auto stop the service and remove it.

Copyright 2003-2013 GroundWork Open Source, Inc.
http://www.groundworkopensource.com
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License.

__HELP__

    exit 0;

    # Don't display standard PerlSvc help text
    $Verbose = 0;
}


###############################################################################
#   Log()
#   Logging function.
#
###############################################################################
sub Log {
    my $msg = shift;
    if (! $logging) { 
        return 0; 
    }
    unless (RunningAsService()) {
	    print localtime . " : $msg\n";
	    return;
    }

    # we should always check the return code to see if the open
    # failed.  die() might be a little harsh here, as it will
    # kill the service if there is a problem opening the log
    # file, but if the service can't log, then it isn't of much use.

    open(my $logfh, ">>$logfile") or die "Could not open log file '$logfile' for appending : $!";
    print $logfh localtime() . " : $msg\n";
    close $logfh;
}

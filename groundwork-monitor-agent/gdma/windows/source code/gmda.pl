#!perl -w

# GDMA service script, based on PingSvc from the pdk toolkit
# Build with PDK 7 : perlsvc --norunlib --nocompress --exe gdma.exe gdma.pl
# Dominic 9/2008
# Dom : 12/2008 Updated to fork dispatcher process using win32 process handling

package PerlSvc;

use strict;
use warnings;
use Win32::Process;  
#use Win32::Process::Info qw{NT};
use Win32::Process::Info ;
use File::Basename;

# Default values 
my $default_drive = "c";
my $start_program_time = time;;
my $service       = 'GDMA';
my $dispatcher    = "${default_drive}:/groundwork/winagent/gw_win_dispatcher.exe";
my $config        = "${default_drive}:/groundwork/winagent/gw_dispatcher.cfg";
my $frequency     = 600 ; # seconds - be nice to make sure the rrd's on the GW end have the same frequency :)
my $dispatcher_timeout =  300 ; # half of the $frequency by default is a good place to start
my $logfile       = "${default_drive}:/groundwork/winagent/gdma.log";
my $version       = "1.1." . localtime();
my $debug = 0;
my $debugflag = "";

my @options = ('dispatcher=s'  => \$dispatcher,
	       'log=s'         => \$logfile,
	       'frequency=i'   => \$frequency,
	       'config=s'      => \$config,
	       'debug'         => \$debug,
	       'timeout=i'     => \$dispatcher_timeout,
              );

# turn on autoflush
$|=1;

(my $progname = $0) =~ s/.*?([^\\]+?)(\.\w+)$/$1/;
our(%Config,$Verbose);

# These assignments will allow us to run the script with `perl gdma.pl`
unless (defined &ContinueRun) {
    # Don't delay the very first time ContinueRun() is called
    my $sleep;
    *ContinueRun = sub {
	Win32::Sleep(1000*shift) if $sleep && @_;
	$sleep = 1;
	return 1
    };
    *RunningAsService = sub {return 0};

    # This is an original pingsvc comment and is a bit confusing :
    # Interactive() would be called automatically if we were running
    # the compiled gdma.exe
    Interactive();
}

# ------------------------------------------------------------------------------
sub get_options {
    require Getopt::Long;
    my @options = @_;
    my $usage = pop @options;
    $SIG{__WARN__} = sub { print "$usage\n$_[0]"; exit 1 };
    Getopt::Long::GetOptions(@options);
    $SIG{__WARN__} = 'DEFAULT';
    
    my $opterror=0;

    # check the dispatcher exists
    if ( ! -e $dispatcher ) {
       print ("Error - dispatcher '$dispatcher' does not exist" ) ;
       $opterror=1;
    }

    # check the config file exists
    if ( ! -e $config ) {
       print ("Error - config file '$config' does not exist" ) ;
       $opterror=1;
    }

    # check the frequency is positive and non zero and at least 60 seconds
    if ( $frequency < 60 ) {
       print ("Error - frequency should be a positive non zero number greater than 60 seconds") ;
       $opterror=1;
    }

    if ( $frequency <= $dispatcher_timeout ) 
    {
       Log("GDMA polling frequency should be greater than the dispatcher timeout");
    }

    $debugflag = " --debug" if $debug;

    exit 2 if $opterror == 1;

}

# ------------------------------------------------------------------------------
# The --install and --remove options are implemented by PerlSvc and
# cannot be simulated when running via `perl gdma.pl`
sub unsupported {
    my $option = shift;
    die "The '--$option' option is only supported in the compiled script.\n";
}

# ------------------------------------------------------------------------------
sub configure {
    %Config = (ServiceName => $service,
	       DisplayName => "GDMA",
	       Parameters  => "--dispatcher $dispatcher --config $config --frequency $frequency --log $logfile --timeout $dispatcher_timeout $debugflag",
	       Description => "GroundWork Distributed Monitoring Agent");
}

# ------------------------------------------------------------------------------
# The Interactive() function is called whenever the gdma.exe is run from the
# commandline, and none of the --install, --remove or --help options were used.
sub Interactive {
    # These entries are only used when the program is run with
    # `perl gdma.pl` and is not compiled into a service yet.
    push(@options,
	 'help'    => \&Help,
	 'version'    => \&Version,
	 'install' => \&unsupported,
	 'remove'  => \&unsupported);

    # Setup the %Config hash based on our configuration parameter
    configure();
    Startup();
}

# ------------------------------------------------------------------------------
# The Startup() function is called automatically when the service starts
sub Startup {
    get_options(@options, <<__USAGE__);
Try `$progname --help` to get a list of valid options.
__USAGE__

    Log("GDMA dispatcher $dispatcher -c $config , every $frequency seconds\n");
    Log("$Config{DisplayName} starting at: ".localtime);

    # Run the gdma dispatcher.
    # ContinueRun() will return early
    # if the service receives a STOP, PAUSE or SHUTDOWN command.
    reset_gdma_pull_counter_file($config);

    while (ContinueRun($frequency))
    {

       my $cmd = "$dispatcher "; my $cmdargs = "-c $config";
       my $exename = fileparse( $cmd ); chomp $exename; $exename =~ s/\s*$//g; # make sure to strip trailing spaces

       Log(localtime()." : Running '$cmd $cmdargs' at frequency $frequency, dispatcher timeout of $dispatcher_timeout");
       Log("Log file is $logfile (only used in service mode)");

       my $ProcessObj = undef;
       my $ProcessID = undef;


       # create a win32 process object that runs the dispatcher - note that if there are more than one arg , like "-c somefile" (yes thats 2 args), then
       # you need to pass in two args to the args arg (!), namely the exename and the cmd args themselves, else this doesn't work.
       if (Win32::Process::Create($ProcessObj, "$cmd", "$exename $cmdargs", 0, NORMAL_PRIORITY_CLASS, ".")) # service working dir is $ENV{SystemRoot} eg c:\windows 
       {
	    $ProcessID = $ProcessObj->GetProcessID();
	    my $TimeStarted = time;
	    Log(">>> [ " . localtime() . "] Dispatcher process started, process ID $ProcessID");
       } 
       else 
       {
            Log("Win32 Process Create error: " . Win32::FormatMessage( Win32::GetLastError() )  );
       }


        # wait for an amount of time that the dispatcher process should complete in
        Log( "Waiting for dispatcher process $ProcessID to finish , time is :" . localtime()  ) if $debug;
        $ProcessObj->Wait($dispatcher_timeout*1000);
        Log( "End Wait at : " . localtime() ) if $debug;

        # Check if the dispatcher is still running - if it is , kill its subprocesses and it, and log error
	my $dispatcherpid = Win32::Process::Info->new();
        my @info = $dispatcherpid->GetProcInfo ( $ProcessID ); 

        if ( @info ) 
        {

            my $procname = $info[0]->{Name};  
            chomp $procname;  $procname =~ s/\s*$//g; # make sure to strip trailing spaces

            Log("Exename = '$exename', infoname = '$procname'") if $debug;

            if ( $procname eq $exename )
            {
                my $duration = time - $info[0]->{CreationDate};

                # get the main dispatchers subprocs list
                my %subs = $dispatcherpid->Subprocesses($ProcessID);

                # kill the main dispatcher
                Log("Killing process $ProcessID - it ran too long ($duration seconds)" );
                Win32::Process::KillProcess($ProcessID, 0);

                # kill the subprocs
                my @subprocs = sort keys %subs;
                Log("Subprocesses (including self) are : @subprocs") if $debug;
		foreach my $sub (keys %subs) 
                {
                    next if $sub == $ProcessID; # kill main process later
                    Log("   Killing dispatcher subprocess  $sub.") ;
                    Win32::Process::KillProcess($sub, 0);
                }
            }
            else
            {
               # this is unlikely but should be checked
               Log("The process id $ProcessID has been re-used (it now belongs to process name '" . $info[0]->{Name} . "') and will not be killed");
            }
        }
        else
        {
           Log("Process $ProcessID completed in time") if $debug;
        }


    } # continue with the continuerun dispatcher polling loop

    Log("$Config{DisplayName} stopped at: ".localtime);
}

# ------------------------------------------------------------------------------
sub Log {
    my $msg = shift;
    unless (RunningAsService()) {
	print "$msg\n";
	return;
    }

    # we should always check the return code to see if the open
    # failed.  die() might be a little harsh here, as it will
    # kill the service if there is a problem opening the log
    # file, but if the service can't log, then it isn't of much use.
    open(my $f, ">>$logfile") or die $!;
    print $f "$msg\n";
    close $f;
}

# ------------------------------------------------------------------------------
sub Install {
    get_options('name=s' => \$service, @options, <<__USAGE__);
Valid --install suboptions are:

  auto       automatically start service / install service as Automatic type also
  --name     service name                     [$service]
  --dispatcher     fully qualified dispatcher command [$dispatcher]
  --config     fully qualified dispatcher config [$dispatcher]
  --log      log file name                    [$logfile]
  --frequency    frequency of dispatcher runs in seconds   [$frequency]
  --timeout    max time the dispatcher can run in seconds   [$dispatcher_timeout]
  --debug    produce more output

For example:

  $progname --install auto --dispatcher ${default_drive}:\\groundwork\\dispatcher.exe --frequency 600

__USAGE__

    configure();
    #print("debugging - config=$config, dispatcher=$dispatcher\n");
}

# ------------------------------------------------------------------------------
sub Remove {
    get_options('name=s' => \$service, <<__USAGE__);
Valid --remove suboptions are:

  --name     service name                     [$service]

For example:

  $progname --remove 
__USAGE__

    # Let's be generous and support `gdma --remove PingFoo` too:
    $service = shift @ARGV if @ARGV;

    $Config{ServiceName} = $service;
}

# ------------------------------------------------------------------------------
sub Version {
   print "GDMA version $version\n";
   exit;
}
# ------------------------------------------------------------------------------
sub Help {
    print <<__HELP__;
GDMA version $version -- Will run $dispatcher every $frequency seconds and logs to $logfile (log used in service mode only)

GDMA can be run in interactive mode from the cli, or as a service.

    $progname [--dispatcher dispatcher.exe --log logfile.log --frequency seconds --config config.cfg]

    --dispatcher <fully qualified dispatcher executable> , default is $dispatcher
    --config <fully qualified gdma dispatcher config file> , default is $config
    --frequency <frequency in seconds at which to execute the dispatcher> , default is $frequency
    --timeout <dispatcher timeout in seconds >, default is $dispatcher_timeout
         Using this timeout option, the maximum run time of the dispatcher can be set. After this 
         time is exceeded, the dispatcher and its process tree subprocesses are killed automatically.
    --debug - gives more output 
    --log <fully qualified log file> , default is $logfile
    --help - show this help (non service mode only)
    --version - show the version of this program (non service mode only)

To install as a service that is of service type "Automatic" ie automatically starts with the OS :

    $progname --install auto  

    (note that this will automatically start the service).

To install as a service that is of service type "Manual" ie does not start automatically with the OS :

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

By default, the GDMA agent is designed to run on ${default_drive}:/groundwork/winagent.
If you'd like to run it from a different drive for example e:, then you can 
start the service as follows :

    $progname --install auto 
              --dispatcher e:\\groundwork\\winagent\\gw_win_dispatcher.exe   
              --config e:\\groundwork\\winagent\\gw_dispatcher.cfg 
              --log e:\\groundwork\\winagent\\gdma.log 
            
Be sure to update your GroundWork host externals so they also use e: instead of ${default_drive}: now.

Using Other Options

You can combine the --timeout and --debug options with the --install option, for example

   $progname --install auto --timeout 250 --debug

This would set the dispatcher process timeout to 250 seconds, killing it and its subprocesses
if it runs for longer than that. The --debug will create more logging to the log file.

Copyright 2003-2008 Groundwork Open Source, Inc. 
http://www.groundworkopensource.com
Unless required by applicable law or agreed to in writing, software distributed under the License 
is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express 
or implied. See the License for the specific language governing permissions and limitations under
the License.


__HELP__
     
    exit 0;

    # Don't display standard PerlSvc help text
    $Verbose = 0;
}

# ------------------------------------------------------------------------------
sub Pause {
    Log("$Config{ServiceName} is about to pause at ".localtime);
}

# ------------------------------------------------------------------------------
sub Continue {
    Log("$Config{ServiceName} is continuing at ".localtime);
}

# ------------------------------------------------------------------------------
sub reset_gdma_pull_counter_file
{

   # each time the gdma service starts, reset the pull counter file back to 1 - this will cause
   # the host cfg to re-pull if using https

   my ( $config ) = @_;
   my ( @cfg, $cfgline, $using_https_for_pull, $counterfile );
   open(CFG, "$config") or die "Cannot open config $config for reading : $!\n";
   @cfg = <CFG>;
   close CFG;


   # search for ConfigFile_Use_HTTPS being set
   $using_https_for_pull = 0;
   foreach $cfgline ( @cfg )
   {
       chomp $cfgline;
       if ( $cfgline =~ /^\s*ConfigFile_Use_HTTPS\s*=.*$/ )
       {
           $using_https_for_pull = 1;
           last ;
       }     
   }

   return if not $using_https_for_pull;

   # if we are using https for pulling cfgs, then get the name of the counter file from the cfg
   # if it doesn't exist yet thats fine, just create it with a zero in
   
   foreach $cfgline ( @cfg )
   {
       chomp $cfgline;
       if ( $cfgline =~ /^\s*ConfigFile_Pull_CounterFile\s*=(.*)$/ )
       {
           $counterfile = $1;
           $counterfile =~ s/#.*$//g;
           $counterfile =~ s/"//g;
           last ;
       }     
   }


   Log("Reset counter file $counterfile");
   if ( not open(CF, ">$counterfile" ) )
   {
       Log("Could not open  counter file '$counterfile' for writing : $!");
       exit 2;
   }

   if ( not (print CF "0") )
   {
       Log("Could not write 0 to counter file $counterfile : $!");
       exit 2;
   }

   if ( not close(CF) )
   {
       Log("Could not close counter file $counterfile : $!");
       exit 2;
   }
   
   

}

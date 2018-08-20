#!perl -w

# GDMA service script, based on PingSvc from the ActiveState PDK toolkit.
# Start the GDMA poller and spool processor as a system service.
#
# Operation of this code is highly mysterious if you don't know where to look in the ActiveState
# documentation.  See this page on our Windows GDMA build machine for how this program is constructed:
# file:///C:/Program%20Files/ActiveState%20Perl%20Dev%20Kit%209.2.1/html/PerlSvc_overview.html
#
# We build this program with PDK 9.2.1, using some variant of the following
# command, captured in our make_gdma.bat script:
#
#     perlsvc --norunlib --nocompress --exe gdma_service.exe gdma_service.pl
#
# Then: "gdma_service --install" to install the service, possibly with other options as well.
#
# See the file:///C:/Program%20Files/ActiveState%20Perl%20Dev%20Kit%209.2.1/html/PerlSvc.html page
# on our Windows GDMA build machine for info on the "perlsvc" program we run in our make_gdma.bat
# script to create the gdma_service.exe binary from the gdma_service.pl source code.

package PerlSvc;

use strict;
use warnings;

# Pull in packages that will be require'd by the packages that we "use".
# Doing this now sidesteps some warning messages in the compiled-code context.
use Cwd;
use Scalar::Util;
use File::Spec::Unix;
use File::Spec::Win32;

# Pull in packages that we directly reference in this script.
use Getopt::Long;
use Win32::Process;
use Win32::Process::Info;

our $VERSION = '2.6.1';

# Default values
my $service = 'GDMA';
my $dir     = '';
if (defined($ENV{GDMA_BASE_DIR})) {
    $dir = "$ENV{GDMA_BASE_DIR}";
}
my $logfile              = "$dir\\gdma_install.log";
my $poller_prog          = "$dir\\bin\\gdma_poll.exe";
my $poller_args          = "-d2";
my $spool_processor_prog = "$dir\\bin\\gdma_spool_processor.exe";
my $spool_processor_args = "-d2";
my $logging              = 1;
my $GDMA_path            = $dir;
my $username             = undef;
my $password             = undef;
my @options              = ( 'logging=i' => \$logging, 'path=s' => \$GDMA_path, 'user=s' => \$username, 'pass=s' => \$password );
my $parsing_remove       = 0;

( my $progname = $0 ) =~ s/.*?([^\\]+?)(\.\w+)$/$1/;
our ( %Config, $Verbose );

###############################################################################
#
#   get_options()
#   gets and validates the command line options for gdma_service.exe
#   Exits on error.
#
###############################################################################
sub get_options {
    my @options = @_;
    my $usage = pop @options;
    $SIG{__WARN__} = sub { print "$usage\n$_[0]\n"; exit 1; };
    Getopt::Long::GetOptions(@options);
    $SIG{__WARN__} = 'DEFAULT';

    $dir                  = $GDMA_path;
    $logfile              = "$dir\\gdma_install.log";
    $poller_prog          = "$dir\\bin\\gdma_poll.exe";
    $poller_args          = "-d2";
    $spool_processor_prog = "$dir\\bin\\gdma_spool_processor.exe";

    my $opterror = 0;

    # For most invocations, check that the dispatcher exists.  If we're removing
    # the service, there is no need for info about the install directory, since
    # all we really need is the name of the service.
    if ( not $parsing_remove ) {
	if ( !-e $dir ) {
	    if ( $dir eq '' ) {
		print("Error:  The installation directory is not defined.\n");
		print("You may use the GDMA_BASE_DIR environment variable,\n");
		print("whose value typically ends in ...\\groundwork\\gdma,\n");
		print("to define the installation directory.\n");
		print("The --path=directory option may also be used.\n");
	    }
	    else {
		print("Error:  The installation directory $dir does not exist.\n");
	    }
	    $opterror = 1;
	}
    }
    if ( ( $logging != 0 ) and ( $logging != 1 ) ) {
	print("Error:  Logging option not supported.  Use '0' or '1'\n");
    }

    # We might consider doing some qualification of the $username if it is defined,
    # to (say) check for the presence of a domain name when it is needed, or the
    # specific required domainnames for particular well-known accounts.  But we
    # will leave that to possible future development, since we don't yet know if
    # specifying a managed service account needs a domain name, nor how to figure
    # out whether a given user name is a managed service account name.  (For the
    # latter, we would probably want to look at the Windows NetIsServiceAccount()
    # function call, however that might ultimately be packaged for use by Perl.)

    exit 2 if $opterror == 1;
}

###############################################################################
#
#   unsupported ()
#   The --install and --remove options are implemented by PerlSvc and cannot be
#   simulated when running this script directly via `perl gdma_service.pl`.
#
#   This routine may also be called when running the compiled script, when the
#   command-line arguments are not constructed as expected.  So we define the
#   error message to cover both cases.
#
###############################################################################
sub unsupported {
    my $option = shift;
    die "The '--$option' option is only supported in the compiled script.\n"
      . "If you are running the compiled script, you probably typed bad options.\n"
      . "Once again, try \"gdma_service --help\".\n";
}

###############################################################################
#
#   configure ()
#   Sets up GDMA service configuration, effectively specifying many of the
#   values passed to the Windows CreateService() call.
#
###############################################################################
sub configure {
    ##
    ## We set the StartNow option to a false value, to ignore any hidden value of the
    ## StartType option that is provided by the PerlSvc package (derived from the
    ## --install option on the command line) when the service is installed.  That
    ## way, the service must be started in a separate step after installation (via
    ## "net start GDMA"), and we can support the ability for the administrator to
    ## make adjustments to the gdma_auto.conf file before the first time the service
    ## is started.
    ##
    ## The Description and DisplayName values are perhaps a little confusing.  The
    ## historical values are shown commented out.  The new values are used because
    ## the DisplayName is the field that shows up as the Description in the Task
    ## Manager's main list of Services.  The DisplayName also shows up as the Name
    ## field in the Task Manager's secondary list of services, while the Description
    ## shows up as the Description in the Task Manager's secondary list of services,
    ## where each service is supposed provide a sentence explaining exactly what the
    ## service actually does.
    ##
    ## THe DisplayName field can be used case-insensitively as the service name in
    ## "net start" and "net stop" commands, as an alternative to the --name value we
    ## use to define the service name.  When we used "GDMA" as the DisplayName, that
    ## could be convenient if we used some alternate --name value for testing.  But
    ## such use is rare in production, so we use more descriptive contents for these
    ## fields to be clear in the places where these values are most likely to be seen.
    ## You can still use:
    ##
    ##    net start "groundwork distributed monitoring agent"
    ##    net stop  "groundwork distributed monitoring agent"
    ##
    %Config = (
	ServiceName => $service,
	## DisplayName => "GDMA",
	## Description => "GroundWork Distributed Monitoring Agent",
	DisplayName => "GroundWork Distributed Monitoring Agent",
	Description => "Runs service checks and sends results to a GroundWork Monitor server.",
	StartNow    => 0
    );

    ## If we want the GDMA service to run as a non-administrative user, we must add
    ## a UserName key and possibly a Password key to this hash, with the following
    ## conventions as established by Windows itself.  See MSDN doc for in-depth
    ## details beyond the short summary here.
    ##
    ## If these credentials are not specified, the service runs using the LocalSystem
    ## account (having no explicitly specified domain).  (The Task Manager shows the
    ## associated daemon processes running as the "SYSTEM" user and the "N/A" group.)  If
    ## provided, the UserName should be generally in the form 'MyDomainName\MyUserName'.
    ## If the account belongs to the local built-in domain, the UserName should be
    ## specified in the form '.\MyUserName'.  (In the single-quoted example strings
    ## here, I'm showing the form that would ultimately need to be passed to the Windows
    ## CreateService() call, not the doubled backslashes that Perl uses in a literal
    ## string to get single backslashes into the actual string.)  For a managed service
    ## account, the UserName should be the name of the managed service account.  For
    ## a virtual account, the Username should be 'NT SERVICE\ServiceName' (with the
    ## "ServiceName" string replaced by the actual name of the service), meaning for our
    ## purposes, 'NT SERVICE\GDMA' (more generally, "NT SERVICE\\$service" in Perl form,
    ## if you use some alternate name for the GDMA service) for our GDMA daemons.
    ##
    ## For the password, part of the MSDN doc says an empty string should be supplied
    ## if a UserName is supplied and the account has no password (which would be a bad
    ## thing, and not something we should support, for an ordinary user account other
    ## than the particular special accounts noted here).
    ##
    ## If the service is to run as the LocalSystem account (in Perl string form, having
    ## UserName ".\\LocalSystem", 'LocalSystem', or not specified), as the LocalService
    ## account (UserName "NT AUTHORITY\\LocalService"), or as the NetworkService account
    ## (UserName "NT AUTHORITY\\NetworkService"), this account has no password, the
    ## password field is ignored in the CreateService() call, and testing shows that
    ## it doesn't matter whether we pass in undef, '', or some arbitrary string as the
    ## Password, or whether we just don't supply this field at all.
    ##
    ## On the other hand, if the UserName is the name of a managed service account or
    ## virtual account name, the password parameter in the Windows CreateService()
    ## call must be NULL, which apparently means we must pass an undef here, either
    ## explicitly as the value of the Password element (perhaps) or (as we do now) by
    ## just not including that element in the %Config hash.
    ##
    ## The upshot is:
    ##
    ## * If you want to use LocalSystem, don't specify either the --gdma_username or
    ##   the --gdma_password options to the BitRock GDMA installer, so neither the
    ##   --user nor the --pass option will be passed to this script.
    ##
    ## * If you want to use LocalService or NetworkService, specify just the
    ##   --gdma_username option to the BitRock GDMA installer, and only the --user
    ##   option will be passed to this script.  It won't matter whether or not the
    ##   --gdma_password option is specified, or equivalently, whether or not this
    ##   script is passed the --pass option.
    ##
    ## * If you want to use a managed service account or virtual account, specify
    ##   just the --gdma_username option to the BitRock GDMA installer, and only
    ##   the --user option will be passed to this script.
    ##
    ## * If you want to use some other account, specify both the -gdma_username
    ##   and the --gdma_password options to the BitRock GDMA installer, so both
    ##   the --user option and the --pass option will be passed to this script.
    ##
    ## Note:  When using a managed service account, it may be necessary to assign
    ## the SeServiceLogonRight logon right to the account to allow it to be used
    ## to run the service.  If that is so, getting that right assigned is beyond
    ## the scope of this program, and doing so must be handled by standard tools
    ## provided by Microsoft.  However, testing shows that a virtual account will
    ## already have this logon right, as soon as that account is automatically
    ## created just by it having been mentioned in the CreateService() call.
    ##
    if ( defined $username ) {
	## Space characters are allowed embedded in the middle of a domain\user,
	## such as "NT AUTHORITY\LocalService", but should not appear at either
	## the beginning or end of the username string.
	$username =~ s/^\s+|\s+$//g;
	if ( $username ne '' ) {
	    $Config{UserName} = $username;
	    if ( defined $password ) {
		$Config{Password} = $password;
	    }
	    elsif ( $username =~ /^(LocalSystem|NT AUTHORITY\\(LocalService|NetWorkService))$/i ) {
		## The CreateService() doc says we must supply an empty string for the
		## password for these three particular accounts, although testing on
		## Win2008, Win2012R2, and Win2016 shows that the password field can
		## be entirely omitted, be passed as an empty string, or be passed as
		## any arbitrary string in this case.
		$Config{Password} = '';
	    }
	    else {
		## In this case, we don't supply any password at all to the underlying
		## CreateService() call, effectively meaning we pass a NULL value to
		## the call instead of a pointer to some string.  The CreateService()
		## doc says that this is the right action to take for either a managed
		## service account or a virtual account.  We have verified on both
		## Win2012R2 and Win2016 that this is definitely REQUIRED for a virtual
		## account ("NT SERVICE\\$service", in Perl terms).
	    }
	}
    }
}

###############################################################################
#
#   Interactive()
#   The Interactive() function is called whenever the gdma_service.exe program
#   is run from the command line, and none of the --install, --remove, or
#   --help options were used.
#
###############################################################################
sub Interactive {
    push(
	@options,
	'help'    => \&Help,
	'version' => \&Version,
	'install' => \&unsupported,
	'remove'  => \&unsupported
    );

    # Setup the %Config hash based on our configuration parameter
    configure();
    Startup();
}

###############################################################################
#
#   Startup()
#   This function is called automatically when the service starts.
#   It spawns the poller and then the spool processor. It also cleans up
#   the poller and spool processor when the service is stopped.
#
###############################################################################

my $caught_signal = 0;

sub catch_abort_signal {
    $caught_signal = 1;
}

sub Startup {
    get_options(@options, <<__USAGE__);
Try `$progname --help` to get a list of valid options.
__USAGE__

    my ($poller_obj, $spool_processor_obj);
    my ($poller_pid, $spool_processor_pid);
    my $pi;
    my @subprocs = ();
    my $exitcode;

    # Launch the poller
    if (
	not Win32::Process::Create(
	    $poller_obj,
	    "$poller_prog",
	    "$poller_prog $poller_args",
	    0,
	    NORMAL_PRIORITY_CLASS | CREATE_NO_WINDOW,
	    "."
	)
      )
    {
	Log("Failed to launch poller\n");
	return 0;
    }

    # Launch the spool processor
    if (
	not Win32::Process::Create(
	    $spool_processor_obj,
	    "$spool_processor_prog",
	    "$spool_processor_prog $spool_processor_args",
	    0,
	    NORMAL_PRIORITY_CLASS | CREATE_NO_WINDOW,
	    "."
	)
      )
    {
	Log("Failed to launch spool processor\n");
	return 0;
    }

    # Windows supposedly doesn't support signals.  But if we run this script interactively and then
    # interrupt it with Ctrl-C, it says "Terminating on signal SIGINT(2)".  That being the case,
    # we could attempt to install a signal handler to catch at least that signal and set a flag to
    # be tested in the following loop, to allow us to exit the loop upon receipt of the signal and
    # clean up the GDMA daemon processes we just launched.  Otherwise, they just become orphaned.
    #
    # We have tried that, and the result is that apparently sometimes the SIGINT is received,
    # and apparently sometimes it is just ignored.  When it is received, it will stop the child
    # processes, but then this process itself might not exit.  So the upshot is, this potential
    # mechanism is unreliable.  Therefore, for the time being, the best advice is just to not run
    # gdma_service manually, and to only depend on running it via a "net start gdma" command.
    #
    # local $SIG{INT} = \&catch_abort_signal;

    # Keep on checking whether the GDMA service is stopped.
    while ( not $caught_signal ) {
	sleep 1;
	last if ( not ContinueRun(0) );
    }

    # The service was stopped.  Clean up.
    $poller_pid          = $poller_obj->GetProcessID();
    $spool_processor_pid = $spool_processor_obj->GetProcessID();

    $pi       = Win32::Process::Info->new();
    @subprocs = $pi->Subprocesses($poller_pid);

    # Kill all the processes that poller started.
    foreach my $sub (@subprocs) {
	next if $sub == $poller_pid;
	Win32::Process::KillProcess( $sub, 0 );
    }

    # Now, kill the poller itself.
    Win32::Process::KillProcess( $poller_pid, 0 );
    $poller_obj->GetExitCode($exitcode);

    @subprocs = $pi->Subprocesses($spool_processor_pid);

    # First, kill the processes that spool processor started.
    # Then, the spool processor itself.
    foreach my $sub (@subprocs) {
	next if $sub == $spool_processor_pid;
	Win32::Process::KillProcess( $sub, 0 );
    }
    Win32::Process::KillProcess( $spool_processor_pid, 0 );
    $spool_processor_obj->GetExitCode($exitcode);
}

###############################################################################
#
#   Install()
#   This function is called when the service is installed.
#   Set-up the service configuration.
#
###############################################################################
sub Install {
    get_options('name=s' => \$service, @options, <<__USAGE__);
Valid --install suboptions are:

  auto       install this service as Automatic type, so it starts
             automatically at system startup
  --name <altname>  optional, service name                    [$service]
  --logging <0,1>   optional, local logging for this program  [$logging]
  --path <path>     GDMA base directory          [$GDMA_path]
  --user <domain\\user>   optional, domainname\\username of the account under
                         which the GDMA service should run  [LocalSystem]
  --pass <password>      optional, password of the account under which the GDMA
                         service should run

For example:

  $progname --install auto

__USAGE__

    configure();
}

###############################################################################
#
#   Remove()
#   This function is called when the service is dropped.
#
#   If for some reason the installed software gets destroyed without first
#   removing the service, the gdma_service.exe program won't be around to be
#   called upon to do so.  But the service can still be removed manually, via
#   the "sc delete gdma" command.  (Or more generally, use "sc delete $service"
#   for whatever value of $service you previously used as the --name option
#   when you installed the service.)
#
###############################################################################
sub Remove {
    ## Pass a clue to get_options() as to our present context,
    ## so error checking can be adjusted accordingly.
    $parsing_remove = 1;

    get_options('name=s' => \$service, <<__USAGE__);
Valid --remove suboptions are:

  --name <altname>  service name    [$service]

For example:

  $progname --remove
__USAGE__

    # Let's be generous and support `gdma_service --remove PingFoo` too:
    $service = shift @ARGV if @ARGV;

    $Config{ServiceName} = $service;
}

################################################################################
#
#   get_version()
#
#   Returns the version number of the poller program.
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

###############################################################################
#
#   Version()
#   Displays GDMA version.
#
###############################################################################
sub Version {
    my $version = get_version(1);
    print "GDMA version $version\n";
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
GDMA version $version
This program will install GDMA as a system service, run that system service, or remove it.

    $progname
    --help            - show this help (non service mode only)
    --install [auto]  - install the service; "auto" means the service should be started
                        automatically at system startup
    --remove          - remove the service
    --name <altname>  - optional, alternate service name
    --logging <0,1>   - default is $logging
    --path <path>     - path should be "C:\\INSTALLDIR\\gdma" with appropriate substitution
    --user <domain\\user>  - optional, domainname\\username of the account under which
			    the GDMA service should run; default is LocalSystem
    --pass <password>     - optional, password of the account under which the
                            GDMA service should run
    --version         - show the version of this program (non service mode only)

To install as a service that is of service type "Automatic",
i.e., automatically starts with the OS:

    $progname --install auto
    net start $service
    (Installation will not automatically start the service at the
    same time it is created, to allow the user to make adjustments
    to the config files before the first startup.)

To install as a service that is of service type "Manual",
i.e., does not start automatically with the OS:

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

Copyright 2003-2018 GroundWork Open Source, Inc.
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
#
#   Log()
#   Logging function.
#
###############################################################################
sub Log {
    my $msg = shift;
    if ( !$logging ) {
	return 0;
    }
    unless ( RunningAsService() ) {
	print "$msg\n";
	return;
    }

    # We should always check the return code to see if the open
    # failed.  die() might be a little harsh here, as it will
    # kill the service if there is a problem opening the log file,
    # but if the service can't log, then it isn't of much use.
    open( my $logfh, '>>', $logfile ) or die "Cannot open the logfile ($logfile):  $!\n";
    print $logfh "$msg\n";
    close $logfh;
}


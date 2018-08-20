################################################################################
#
#    gdma_poll.pl
#
#    This is the poller part of GroundWork Distributed Monitoring Agent.
#    This program performs active checks at regular intervals, as defined
#    in the config file.  The config file is pulled via https/http from
#    the GroundWork server at regular intervals.  The check results are
#    recorded in a spool file.  The read/write to the spool file is
#    synchronized with the spool processor program.  In each iteration
#    the program sleeps until it is time for the next check.
#    This should be executed as a system service.
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
#
# v2.4.1 2015-05-29 DN	Modified logic in handle_config_pull() and fetch_config_file()
#			so if a do_timed_mirror() Windows call to $useragent->mirror()
#			dies (i.e., fails), that failure is used to send an error to the
#			gdma_poller service, and control the logic in handle_config_pull to
#			NOT try to run an auto register.  Also in get_plugin_file_list(), if
#			a failure is detected, a message is sent to the gdma_poller service
#			too.  This was in response to the customer case EGAIN-49.
#
# v2.4.2 2016-07-29 GH	Modified SSL_version to switch from TLSv1 (only) to TLSv1_2 (only).
#			This will require corresponding support on the server side, to accept
#			such connections.  Also stopped setting SSL_cipher_list here, so
#			we instead use the default (and rather complex) setting from the
#			IO::Socket::SSL package (which we now insist must be used instead
#			of Net::SSL as the underlying SSL implementation, and be of fairly
#			recent vintage, just to get this default SSL_cipher_list).  Our
#			previous hardcoded SSL_cipher_list setting was reasonable at the time
#			we implemented it (for instance, it would repel the BEAST attack,
#			as noted in the documentation for Net::SSLeay 1.74), but using the
#			IO::Socket::SSL default value is now the recommended practice, because
#			that default setting has been real-world battle tested, especially as
#			of IO::Socket::SSL version 1.956 (and updated again in later releases
#			at least through 2.026).  To make this change complete, we must ensure
#			that the LWP::UserAgent package uses IO::Socket::SSL for its back-end
#			SSL Socket implementation, not Net::SSL, for which the situation is
#			less certain.  The old default (as of LWP::UserAgent 5.807) seems to
#			have been to use Net::SSL if both packages were loaded; but as of 6.06,
#			at least, IO::Socket::SSL now appears to be the default.  (As of 6.00,
#			IO::Socket::SSL seems to be the default for https:// connections, and
#			Mozilla::CA must be installed as well.)  In any case, just to be safe,
#			we take explicit action here to force the use of an appropriate version
#			of IO::Socket::SSL, on all platforms.
#
# v2.5.0 2016-09-13 GH	Fixed an incomplete implementation of the v2.4.1 changes in error
#			handling while fetching a config file, that would have caused the GDMA
#			client to crash.  Also bumped up the version number to 2.5.0 to reflect
#			the switch to only supporting TLSv1.2, across all GDMA client platforms.
#
# v2.5.1 2017-04-26 GH	Drop use of the sigtrap die handler, as it is effectively broken.
#
# v2.4.3 2017-07-31 GH	For Windows GDMA only, revert to using TLSv1_1 and our old explicitly
#			specified cipher list, for a temporary build of Windows GDMA 2.4.0
#			overlay files that force the use of TLS 1.1 instead of TLS 1.0 (our
#			old GDMA releases) or TLS 1.2 (our present direction); see GDMA-412.
#			Except for this note documenting the situation with this numbering,
#			these changes will then be immediately backed out.  The Windows GDMA
#			2.4.3 overlay files will take advantage of other changes previously
#			made for the GDMA 2.5.0 and 2.5.1 releases.
#
# v2.5.2 2017-12-04 GH	Adjust the PATH on a 64-bit Windows machine to invoke a 64-bit context
#			when gdma_run_checks spawns child processes.
#
# v2.6.0 2018-04-18 GH	Support Auto-Setup.
#			Restrict permissions on the gwmon_$hostname.cfg and gdma_override.conf files.
#			Convert all the logging to use Log::Log4perl (beneath an abstraction layer of
#			our own), and in so doing also provide for automatic log rotation.
#
# v2.6.1 2018-05-08 GH	Refactor to support Auto-Setup using HTTPS to the server.

# TO DO:
# (*) Under Windows, use a hash instead of an array to keep track of spawned run-checks processes,
#     and clean up each instance as soon as you know the child has completed, instead of waiting for
#     all of them to complete before cleaning up any of them.
# (*) Both under Windows and under UNIX, take care to read and log the exit status of each spawned
#     run-checks process.  In the case of UNIX, you might need to propagate the grand-child exit status
#     up through the child exit status for this to get all the way back to the original poller script.
# (*) Use_Long_Hostname needs an explicitly-named third option, to default on (check the long name first)
#     but fall bak to the short name, as we do now if Use_Long_Hostname is not defined.
# (*) Messages about request for long-hostname config files that end up in the server error log
#     should mention the Use_Long_Hostname option.

use strict;
use warnings;

# This is supposed to be the default, but we force it anyway, because we want to ensure that
# we have the default SSL_cipher_list from IO::Socket::SSL in play (and not whatever Net::SSL
# provides, if anything), even if somebody has set the PERL_NET_HTTPS_SSL_SOCKET_CLASS
# environment variable to something else.  See the Net::HTTPS documentation for information
# on this variable.
BEGIN {
    ## LWP::UserAgent "use"s LWP::Protocol and calls LWP::Protocol::create() to dynamically reference
    ## LWP::Protocol::https if we have an HTTPS connection configured, and that in turn "require"s
    ## Net::HTTPS at run time.  While this chain works just fine, the Perl compilation phase can't
    ## tell whether Net::HTTPS will be loaded, so it complains about "used only once: possible typo"
    ## for the following assignment, which will be the only reference to this variable at compilation
    ## time.  We disable that noisy warning about a known singleton use of the variable.
    no warnings 'once';
    $Net::HTTPS::SSL_SOCKET_CLASS = 'IO::Socket::SSL';
}

# We would prefer to insist on IO::Socket::SSL 2.033, but IO::Socket::SSL 2.027
# is all that is portably available to us across all platforms as of this writing.
# Still, this gets us the cipher-list updates we crave.
use Fcntl qw(:DEFAULT :flock);
use IO::Socket;
use IO::Socket::SSL 2.027;  # Make sure IO::Socket::SSL is used in preference to Net::SSL, and use a recent cipher list.
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

use POSIX qw(:sys_wait_h _exit strftime :signal_h);
use Storable qw(dclone);
use HTML::LinkExtor;
use Digest::MD5;
use File::Copy;
use XML::Simple;
use LWP::UserAgent;
use HTTP::Request::Common;
use HTTP::Status;
use URI::Escape;
use Socket;
use Net::Address::Ethernet (qw(get_address get_addresses));

use GDMA::LockFile;
use GDMA::Logging;
use GDMA::Discovery;
use GDMA::Utils;

our $VERSION = '2.6.1';

if ( $^O eq 'MSWin32' ) {
    ## Check if we support the windows flavor.
    ##
    ## We are going to use Win32::GetShortPathName() (in the Win32 package)
    ## to convert certain configured pathnames to their 8.3 formats so they
    ## hopefully can be used in PowerShell -command invocations without
    ## danger of running afoul of the fact that the powershell -command
    ## option does not parse spaces in pathnames correctly.
    ##
    ## We are going to use Win32::Job to create and manipulate
    ## sub-processes.  Win32:Job is supported only on Windows 2000
    ## and later versions.
    require Win32;
    import  Win32;
    require Win32::Process;
    import  Win32::Process;
    import  Win32::Process qw(STILL_ACTIVE);
    require Win32::Process::Info;
    my $osname = Win32::GetOSName();
    if ( ( $osname =~ /^WinME/ ) or ( $osname =~ /^WinNT/ ) or ( $osname =~ /^Win95/ ) or ( $osname =~ /^Win98/ ) ) {
	die "$osname is not a supported operating system";
    }

    # Load Win32:Job on the Windows platform only.
    # We need this to create and manipulate processes to execute plugins.
    require Win32::Job;
    import  Win32::Job;

    # If we are running in a 32-bit context on a 64-bit Windows machine, set up to prefer
    # spawning 64-bit powershell.exe (for PowerShell scripts) and cscript.exe (for VBS
    # scripts) child processes, if 64-bit PowerShell and cscript interpreters are available.
    #
    # Typical Windows %PATH% environment variables seem to have possibly several copies of
    # path components that include the offending ...\System32\... directory that causes us
    # trouble.  Typical path components are:
    #
    #     C:\Windows\system32
    #     C:\Windows\System32\Wbem
    #     C:\Windows\System32\WindowsPowerShell\v1.0\
    #
    # Which means partly that we should expect to see more than one component we might
    # need to adjust.  Essentially, we should split the value of %PATH% on ';', adjust the
    # set of path components to stuff an equivalent "Sysnative" path component in front of
    # each existing "System32" path component, and then join up all the components.  We
    # retain the System32 path components to provide fallback in case a 64-bit version
    # of some program is not available.  That should cover all bases, not just for
    # powershell.exe but also for cscript.exe and perhaps other programs.
    #
    if ( $ENV{'PROCESSOR_ARCHITECTURE'} eq 'x86' && defined $ENV{'PROCESSOR_ARCHITEW6432'} ) {
	## We're operating under WOW64, meaning in a 32-bit context on a 64-bit OS.
	##
	## To access 64-bit versions of standard programs, we must transform
	## %PATH% components like these:
	##
	##     C:\WINDOWS\system32
	##     C:\Windows\System32\Wbem
	##
	## which the system in this operating context, because of path-interception
	## actions of the Windows File System Redirector, will interpret instead as:
	##
	##     C:\WINDOWS\SysWOW64
	##     C:\Windows\SysWOW64\Wbem
	##
	## to the equivalent of the following:
	##
	##     C:\WINDOWS\Sysnative;C:\WINDOWS\SysWOW64
	##     C:\Windows\Sysnative\Wbem;C:\Windows\SysWOW64\Wbem
	##
	## which will invoke 64-bit programs (such as powershell.exe and cscript.exe)
	## if they exist, instead of the equivalent 32-bit programs.
	##
	my @path = split ';', $ENV{'PATH'};
	local $_;
	for (@path) {
	    if (/\\System32(?:\\|$)/i) {
		( my $native = $_ ) =~ s/\\System32(?=\\|$)/\\Sysnative/ig;
		$_ = "$native;$_";
	    }
	}
	$ENV{'PATH'} = join( ';', @path );
    }
}
else {
    ## The POSIX::RT::Timer package is only available on UNIX platforms.
    ## We need it to impose timeouts on file mirroring via HTTPS, given that
    ## the library we use for mirroring is broken in that mode (GDMA-295).
    ##
    ## We run these statements inside string evals in order to sidestep the
    ## Windows Perl compiler's behavior of acting on these statements at
    ## compile time (if only to check that the package exists, and include
    ## it into the compiled program).  That won't work under Windows, because
    ## the POSIX::RT::Timer package, being UNIX-specific, won't compile there.
    eval 'require POSIX::RT::Timer';
    if ($@) {
	chomp $@;
	die "$@\n";
    }
    eval 'import POSIX::RT::Timer';
    if ($@) {
	chomp $@;
	die "$@\n";
    }
}

# Is it an OS that we support?
# The special variable $^O contains the OS name.
if ( ( $^O ne 'linux' ) and ( $^O ne 'solaris' ) and ( $^O ne 'aix' ) and ( $^O ne 'hpux' ) and ( $^O ne 'MSWin32' ) ) {
    die "ERROR:  $^O is not a supported operating system\n";
}

# Function declarations
sub main;
sub upgrade_plugins;
sub download_plugins;
sub get_plugin_file_list;
sub run_normal_mode;
sub spool_autoconfig_message;
sub reload_config;
sub insert_into_result_buf;
sub do_checks;
sub get_gdma_cfg_file_using_http;
sub fetch_config_file;
sub install_autoconfig_filepaths;
sub install_hostconfig_filepaths;
sub spool_startup_info;
sub handle_config_pull;
sub detect_conf_file_change;
sub spool_config_change_message;
sub get_config_file_list;
sub exec_plugins_windows;
sub exec_plugins_unix;
sub spool_a_health_message;

# Command line options
my %g_opt = ();

# A global hash that stores all the configuration file parameters.
my %g_config;

my $logging = undef;
my $logger  = undef;

my $separator = '------------------------------------------------------------------------------------------';

my $file_separator = ( $^O eq 'MSWin32' ) ? "\\" : '/';

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
# FIX LATER:  Modify this test to check affirmatively against the particular user
# the GDMA software was installed as (though that might possibly vary), rather than
# negatively against just the root user.
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

# Let's just do the probing for the execution environment exactly once,
# since it won't change over the lifetime of this process.

my $platform  = '';
my $os        = '';
my $processor = '';
my $os_bits   = 0;

# The tests here are customized to yield the best possible determination for each respective platform.
if ( $^O eq 'linux' ) {
    $os = $^O;
    my $machine = `/bin/uname -m`;
    chomp $machine;
    ## This $processor assignment is presumptive, since this is currently the only variant we support.
    $processor = 'intel';
    ## We could also potentiall use `getconf LONG_BIT` for this.
    $os_bits = $machine eq 'x86_64' ? '64' : '32';
    $platform = "$os-$processor";
}
elsif ( $^O eq 'solaris' ) {
    $os = $^O;
    ## The defaulting of intel if not sparc is presumptive, since those are the only variants we support.
    my $isa_type = `/usr/bin/uname -p`;
    $processor = $isa_type =~ /sparc/ ? 'sparc' : 'intel';
    ## isainfo was introduced with Solaris 7, at the same time that 64-bit kernels became available.
    ## So if isainfo is not found, you're running in 32-bit mode.
    my $kernel_isa = -f '/usr/bin/isainfo' ? `/usr/bin/isainfo -kv` : '';
    $os_bits = defined($kernel_isa) ? ($kernel_isa =~ /64-bit/ ? '64' : '32') : '32';
    $platform = "$os-$processor";
}
elsif ( $^O eq 'aix' ) {
    $os = $^O;
    $processor = `/usr/bin/uname -p`;
    chomp $processor;
    $os_bits = `/usr/bin/getconf KERNEL_BITMODE`;
    chomp $os_bits;
    if ($os_bits !~ /^\d+$/) {
	my $kernel_file_type = `/usr/bin/file /usr/lib/boot/unix`;
	$os_bits = $kernel_file_type =~ /64-bit/ ? '64' : '32';
    }
    $platform = "$os-$processor";
}
elsif ( $^O eq 'hpux' ) {
    $os = $^O;
    ## We expect $processor to be "ia64" on all Itanium systems.
    ## HP9000 machines are presumed to be some flavor of older, PA-RISC machines.
    ## Possibly, we might want to distinguish PA-RISC 1.0, 1.1, and 2.0 machines
    ## (via `getconf CPU_VERSION`), though this code does not yet do so.
    my $machine = `/usr/bin/uname -m`;
    chomp $machine;
    $processor = $machine eq 'ia64' ? 'ia64' : $machine =~ m{^9000/} ? 'parisc' : $machine;
    $os_bits = `/usr/bin/getconf KERNEL_BITS`;
    chomp $os_bits;
    $platform = "$os-$processor";
}
elsif ( $^O eq 'MSWin32' ) {
    $os = 'windows';
    ## Current Windows variants run on Intel, and in the future on ARM.
    ## Very old versions of Windows support Alpha, MIPS, PowerPC, and Itanium.
    ## This $processor assignment is presumptive, since this is currently the only variant we support.
    $processor = 'intel';
    $os_bits =
	( ( defined $ENV{PROCESSOR_ARCHITEW6432} and $ENV{PROCESSOR_ARCHITEW6432} =~ /64/ ) or $ENV{PROCESSOR_ARCHITECTURE} =~ /64/ )
	? '64' : '32';
    $platform = "$os-$processor";
}

# Set up the environment so that we will access correct libraries.
set_environment( GDMA::Utils::get_headpath() );

my $Default_Max_Server_Redirects = 5;

# Hardcoded timeout for downloading individual plugins and plugin dependencies from the server.
# In some future release, we might document that this is an externally configurable parameter.
my $Default_PluginPull_Timeout = 40;
my $PluginPull_Timeout         = $Default_PluginPull_Timeout;

my $Default_Poller_Proc_Interval = 600;

# Valid values for $Default_Auto_Register_Attempts are:
#  "never", "once", "arithmetic", "exponential", "fibonacci", "periodic"
my $Default_Auto_Register_Attempts     = "fibonacci";
my $Default_Auto_Register_Max_Interval = 86400;
my $Default_Auto_Register_Cycle_Limit  = $Default_Auto_Register_Max_Interval / $Default_Poller_Proc_Interval;

my $Auto_Register_Attempts              = $Default_Auto_Register_Attempts;
my $Auto_Register_Cycle_Limit           = $Default_Auto_Register_Cycle_Limit;
my $Auto_Register_Cycle_Count           = 0;
my $Auto_Register_Cycle_Period          = 1;
my $Auto_Register_Cycle_Previous_Period = 0;

# In some future version, we might make this option available externally,
# settable in the gdma_auto.conf file, and still defaulted to 0 if not set.
my $Force_CRL_Check = 0;

# See the comments for %standard_ssl_opts in GDMA::Utils for notes on these settings.
my %application_ssl_opts = (
    verify_hostname => 1,
    SSL_ca_path     => GDMA::Utils::get_ca_path(),
    SSL_version     => 'TLSv1_2',
    SSL_check_crl   => 1,
);

## For use later on in our signal handler.
our @caught_abort_signals = ();
##
## Pre-extend the array a bit, so we probably shouldn't ever have to do so within the
## signal handler that pushes elements into this array.  This is a precaution that will
## hopefully sidestep any potential allocation issues within a signal-handling context.
##
$#caught_abort_signals = 5;
@caught_abort_signals  = ();

# Call the main processing loop.
exit( main() ? 0 : 1 );

################################################################################
#
#   main()
#
#   The main poller functionality is implemented here.  To begin with it
#   spools the script start-up information.  Then in a forever loop -
#   1. Checks if config file should be pulled.  If yes, pulls it and
#      reads.  Spools a config change message, if applicable.
#   2. Decides the mode of operation (normal mode/autoconfig mode)
#   3. In normal mode, it performs the configured system checks,
#      spools them, and then spools the heartbeat message.
#   4. In autoconfig mode, it simply sleeps till the next iteration.
#   5. Sleeps until its time for the next iteration, determined by
#      the configured Poller_Proc_Interval.
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

    my ( $loop_start_time, $errstr, $conf_modified, $first_reload );
    my $loopcount = 0;

    # A counter to keep a track of pull cycles.
    my $num_cycles = 0;

    # A flag that indicates if the config file has been fetched from the GroundWork server.
    my $fetched_config;

    # A flag that indicates if the config file needs to be re-read.
    my $reload_config;

    # A Variable that marks autoconfig/normal mode.
    # autoconfig mode if auto_config = 1
    # normal mode if auto_config = 2
    # only read autoconfig and host config file if auto_config = 0
    my $auto_config = 0;

    my $startup = 1;

    # Filepaths that contain the autoconfig/default settings.
    my $AutoconfigfilePath;
    my $AutoconfigOverridefilePath;

    # Path for pulled configuration file.
    my $HostconfigfilePath;

    # A buffer that stores results to be spooled.
    my @result_buf = ();

    # Time when the config was last pulled.
    my $time_last_pulled = 0;

    # Modification time of the autoconfig files.
    my $autoconf_last_modified     = 0;
    my $overrideconf_last_modified = 0;

    # A list of all the config files.
    my @conf_files = ();

    # A list of modified conf files.
    my @modified_conf_filenames = ();

    # Debug level set.
    my $debug = defined( $g_opt{d} ) ? $g_opt{d} : 0;

    # Get the head path for installation.
    my $head_path = GDMA::Utils::get_headpath();

    # Make sure end-of-life messages are output before we call POSIX::_exit to quit.
    STDOUT->autoflush(1) if $g_opt{i};

    # In some future version, we might move this call later, after we have had a chance to
    # initialize $Force_CRL_Check from some new optional directive in the gdma_auto.conf file.
    GDMA::Utils::initialize_ssl_opts( \%application_ssl_opts, $Force_CRL_Check );

    ## We need certain critical %g_config options here, so we fake them up for the time being.
    my %t_config = ( Logdir => "$head_path${file_separator}log", Enable_Local_Logging => 'on' );
    my $logging_hostname = GDMA::Utils::my_hostname( $t_config{Use_Long_Hostname}, $t_config{Forced_Hostname}, $t_config{Use_Lowercase_Hostname} );
    my $logging_logfile = $t_config{Enable_Local_Logging} =~ /^on$/i ? $t_config{Logdir} . $file_separator . 'gwmon_' . $logging_hostname . '_poller.log' : undef;
    my %logging_options = ( logfile => $logging_logfile, grouping => 'individual' );
    $logging_options{stdout}    = 1       if $g_opt{i};
    $logging_options{log_level} = 'debug' if $g_opt{d};
    $logging_options{log_level} = 'trace' if $g_opt{d} && $g_opt{d} =~ /^(\d+)$/ && $g_opt{d} > 1;

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

    # Set-up SIGTERM and SIGINT handlers to make sure that we are sensitive
    # to these signals, but only in case of linux, solaris, aix, and hpux.
    #
    # FIX MAJOR:  Safe::reval() (in Safe versions at least 2.35 through 2.39) ends up resetting the entire
    # %SIG hash to DEFAULT actions.  See https://rt.cpan.org/Public/Bug/Display.html?id=112092 for details.
    # And that can happen buried in some package we call indirectly, such as Log::Log4perl::Config which
    # we call indirectly from within our GDMA::Logging->new() code, as well as TypedConfig which we call
    # within our GDMA::Discovery code.  So until we get the Safe package either fixed directly or have
    # the Safe::reval() routine globally overridden to save and restore all previously established signal
    # handlers, the signal-handler setup we do here will sooner or later be undone.  The danger here is
    # that shutting down the GDMA service as a whole will not end up shutting down any long-running service
    # checks which are out of control.
    #
    if ( ( $^O eq 'linux' ) or ( $^O eq 'solaris' ) or ( $^O eq 'aix' ) or ( $^O eq 'hpux' ) ) {
	##
	## In each signal handler, we ignore future reception of that signal only
	## because we're about to signal the entire process group, which will include
	## ourself, and we want not to have the signal handler invoked recursively and
	## continually.  We do want this process itself to terminate, but we take care
	## of that on our own as a result of the first signal being recognized.
	##
	## FIX MAJOR:  Signaling the entire process group makes very little sense
	## if we haven't previously taken care to make our own process the process
	## group leader.  Without that, we're signaling some unknown controlling
	## process group, and lots of unexpected processes may be told to shut down.
	## (Fortunately, we probably won't have permissions to send a signal to some
	## other process group, so this isn't quite as dangerous as it might seem.)
	##
	## FIX MINOR:  We should handle SIGHUP and SIGQUIT as well, in appropriate ways.
	##
	$SIG{TERM} = sub { $SIG{TERM} = 'IGNORE'; kill "TERM", -$$; exit 1 };
	$SIG{INT}  = sub { $SIG{INT}  = 'IGNORE'; kill "INT",  -$$; exit 1 };
    }

    # Load high resolution timer Time::HiRes if available.
    # Otherwise, use normal one.
    my ( $hires_time, $hires_time_format ) = GDMA::Utils::load_hires_timer();

    # Set up spoolfile paths based on the platform we are running on.
    my $normal_spool_filename = GDMA::Utils::get_spool_filename($head_path);
    my $priority_spool_filename = GDMA::Utils::get_spool_filename( $head_path, 1 );

    # Set the values for server config filepaths based on platform.
    ($AutoconfigfilePath, $AutoconfigOverridefilePath) = install_autoconfig_filepaths($head_path);

    # Read the default parameters from the autoconfig and override files.
    if ( !GDMA::Utils::read_config( $AutoconfigfilePath, \%g_config, \$errstr, 0 ) ) {
	## We can't officially log anything here -- we don't know the logfile name yet.
	## But that's why we faked up some temporary config data earlier, so we could
	## have a logger open now in spite of that.
	## There is nothing to do.
	$logger->fatal("FATAL:  Failed to read the main autoconfig file:  $errstr");
	die "Failed to read the main autoconfig file:  $errstr\n";
    }
    if ( !GDMA::Utils::read_config( $AutoconfigOverridefilePath, \%g_config, \$errstr, 1 ) ) {
	## We can't officially log anything here -- we don't know the logfile name yet.
	## But that's why we faked up some temporary config data earlier, so we could
	## have a logger open now in spite of that.
	## There is nothing to do.
	$logger->fatal("FATAL:  Failed to read the override autoconfig file:  $errstr");
	die "Failed to read the override autoconfig file:  $errstr\n";
    }

    my $new_logging_hostname = GDMA::Utils::my_hostname( $g_config{Use_Long_Hostname}, $g_config{Forced_Hostname}, $g_config{Use_Lowercase_Hostname} );
    my $new_logging_logfile =
      $g_config{Enable_Local_Logging} =~ /^on$/i ? $g_config{Logdir} . $file_separator . 'gwmon_' . $new_logging_hostname . '_poller.log' : undef;
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
	    print "FATAL:  Cannot create a GDMA::Logging object" . ( defined($logging_logfile) ? " for file \"$logging_logfile\"" : '' ) . ".\n";
	    return 0;
	}
	$logger = $logging->logger();
    }

    if ( !defined( $g_config{Target_Server} ) ) {
	## We have to know the target server to pull the host config file from.
	$logger->fatal("FATAL:  Target server is not defined in the autoconfig file.");
	die "Target server is not defined in the autoconfig file.\n";
    }

    set_config_defaults ();
    initialize_auto_register_cycle();
    reset_auto_register_cycle();

    # Run in autoconfig mode if told.
    $auto_config = 1 if ( $g_config{Enable_Auto} =~ /^[Oo]n$/ );

    ## Run a first pass of auto-discovery upon startup, if it is warranted.  This will
    ## allow the initial set of server-side externals to be generated before the first
    ## time we attempt to pick them up, so we can begin monitoring with them right away.
    if ( defined( $g_config{Enable_Auto_Setup} ) && $g_config{Enable_Auto_Setup} =~ /^on$/i ) {
	## We don't really care about the returned status, as there is not much we can do
	## about any sort of failure at this point.
	run_auto_setup($AutoconfigOverridefilePath);
    }

    # When calling internal routines to fetch a config file, pass back through the stack
    # a bit more detail on failure than just the return code, so this calling code can
    # see whether we had an unexpected situation (like the mirror failing with a die),
    # and handle that condition properly in the logic at this level.
    my $first_try_failure_message  = undef;
    my $second_try_failure_message = undef;

    # FIX MAJOR:  Shouldn't we lock around this fetching, the same way we do inside handle_config_pull()?
    # Yes, probably.  But then we'll need to figure out what to do if we cannot acquire the lock.  That
    # would probably be to emit a log message, sleep a short time, and retry -- an infinite number of
    # times, if need be.

    # To begin with, try to pull the host config file via http/s.
    # We don't have to wait until the pull is successful --
    # we will run in autoconfig mode if the pull fails.
    # Parse the target server in the config file, to get the
    # first one in the list, which will be comma separated.
    my $target_addr = ( split( /[,\s]+/, $g_config{Target_Server} ) )[0];

    # Set the values for host config filepath based on platform and on the auto-config setup.
    # If the auto-config file doesn't specify whether to use a long or short hostname, we first
    # try the long form (the most specific construction), and then fall back to the short form
    # (the most general construction) if a config file using the long form is not available.
    my $Use_Long_Hostname = defined( $g_config{Use_Long_Hostname} ) ? $g_config{Use_Long_Hostname} : 'on';
    my $hostname = GDMA::Utils::my_hostname( $Use_Long_Hostname, $g_config{Forced_Hostname}, $g_config{Use_Lowercase_Hostname} );
    $HostconfigfilePath =
      install_hostconfig_filepaths( $head_path, $Use_Long_Hostname, $g_config{Forced_Hostname}, $g_config{Use_Lowercase_Hostname} );
    $fetched_config =
      fetch_config_file( $target_addr, $hostname, $HostconfigfilePath, \$conf_modified,
	\@modified_conf_filenames, \@conf_files, \$first_try_failure_message );
    if ($fetched_config) {
	$time_last_pulled = time;
	## For later consistency, cement a provisional success into place for future
	## determination of the hostname form.  This can be overridden when we later
	## reload the configuration from a fetched configuration file.
	$g_config{Use_Long_Hostname} = $Use_Long_Hostname if not defined $g_config{Use_Long_Hostname};
    }
    elsif ( not defined $g_config{Use_Long_Hostname} ) {
	## We used a long-form hostname before.  Fall back to trying the short-form hostname.
	$hostname = GDMA::Utils::my_hostname( $g_config{Use_Long_Hostname}, $g_config{Forced_Hostname}, $g_config{Use_Lowercase_Hostname} );
	$HostconfigfilePath = install_hostconfig_filepaths(
	    $head_path,
	    $g_config{Use_Long_Hostname},
	    $g_config{Forced_Hostname},
	    $g_config{Use_Lowercase_Hostname}
	);
	$fetched_config =
	  fetch_config_file( $target_addr, $hostname, $HostconfigfilePath, \$conf_modified, \@modified_conf_filenames, \@conf_files,
	    \$second_try_failure_message );
	if ($fetched_config) {
	    $time_last_pulled = time;
	}
    }
    $logger->debug("DEBUG:  Poller is using hostname \"$hostname\".");

    my $auto_registered = 0;
    if ( !$fetched_config ) {
	$logger->error("ERROR:  Failed to fetch the host config file.");

	# We prefer to report a second failure message in preference to a first failure message,
	# mainly because it is more current and perhaps now better reflects the state of the world.
	if ( $second_try_failure_message || $first_try_failure_message ) {
	    $logger->notice("NOTICE:  Skipping auto-registration because config-file fetching failed for a strange reason listed above.");
	    ## If the mirroring failed way down in get_gdma_cfg_file_using_http calling do_timed_mirror,
	    ## then $first_try_failure_message and/or $second_try_failure_message will not be undef.
	    ## Example case:  way down in Net::HTTP::read_entity_body called via LWP Useragent, the
	    ## mirror() died mysteriously.  In this case, report on the error via the poller service,
	    ## but DON'T try to auto register.
	    ##
	    ## Push something useful into the spooler file for the poller service :).
	    ##
	    spool_a_health_message( "Configuration file fetch failure:  " . ( $second_try_failure_message || $first_try_failure_message ), 2 );
	}
	else {
	    ## Call the auto-registration API on the target server if it makes sense to do so now.
	    ## If we don't have credentials for that, we'll end up emitting an auto-configure message instead.
	    ## (Or so we used to; that protocol is now considered to be obsolete, so we don't call it any more.)
	    $auto_registered = run_auto_register_cycle( $AutoconfigOverridefilePath, $head_path, $priority_spool_filename );
	}
    }
    else {
	$logger->debug("DEBUG:  Successfully fetched config file.");
    }

    # This is the first pull, reload config no matter what.
    $reload_config = 1;

    # We are going to re-load it for the first time.
    $first_reload = 1;

    # Execute forever.
    my $config_error = 0;
    while (1) {
	$loop_start_time = &$hires_time();

	# FIX MINOR:  Logically, we would only want to rotate the logfile if we have logging formally enabled.
	# However, we presently override the disabling of logging in the config file, so as to get the word out
	# about early-phase problems in GDMA operations.  And for some reason, logging is not yet disabled once
	# we get going.  (Ideal would be a third configuration value, fully supported:  log only the most serious
	# of messages, so we get the word out if the process fails but are otherwise quiet.
	#
	# In the meantime, since we're logging even when configured not to, we must force file-size checking and
	# possible logfile rotation under all circumstances.  Once we are more sophisticated about logging, we
	# could perhaps skip this step if logging were truly disabled, not rotating the logfile during every loop,
	# but only once right before we stop our initial forced logging.
	#
	if ( 1 || $g_config{Enable_Local_Logging} =~ /^on$/i ) {
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
	## This marker visually separates output from successive polling cycles.
	$logging->log_separator($separator);

	# We don't pull the configuration on the first iteration of this loop unless we just auto-registered,
	# because we just tried to fetch the config data before we entered this loop.  If we did auto-register
	# before entering this loop, we would have done so only because the fetching failed.  So if the
	# auto-registration worked, we may as well go ahead and attempt a config-file fetch right away on the
	# first cycle.
	#
	# FIX MINOR:  handle_config_pull() is very much like the code just above this loop that fetches the
	# initial copy of the config file.  We ought to merge that code into handle_config_pull under control
	# of some special function arguments, to avoid duplication of code fragments here and there.
	++$num_cycles;
	if ( ( $startup && $auto_registered ) || ( !$startup && $num_cycles >= $g_config{ConfigFile_Pull_Cycle} ) ) {
	    ## Try to pull the host config file from the server.  $time_last_pulled and $reload_config will
	    ## be updated if the file is found modified.  It may also spool some informational messages.
	    ## Note that this call might result in an internal deletion of the host configuration file,
	    ## and then reversion to auto-config mode on the next iteration of the enclosing loop.
	    handle_config_pull(
		$AutoconfigOverridefilePath, \$HostconfigfilePath,     \$time_last_pulled, \$reload_config,
		$normal_spool_filename,      $priority_spool_filename, \@result_buf,       $head_path,
		\@conf_files, \$auto_config, not( $startup && $auto_registered )
	    );
	    $num_cycles = 0;
	}

	# Check if the autoconfig files have changed since last iteration.
	my $autoconf_modified     = 0;
	my $overrideconf_modified = 0;
	detect_conf_file_change( $AutoconfigfilePath,         \$autoconf_last_modified,     \$autoconf_modified,     0 );
	detect_conf_file_change( $AutoconfigOverridefilePath, \$overrideconf_last_modified, \$overrideconf_modified, 1 );
	if ( $autoconf_modified || $overrideconf_modified ) {
	    if ( not $first_reload ) {
		## Notify GW monitor that autoconfig files have changed.  We don't notify if the
		## config changed while we were down, because we have no good way to detect that.

		# Strip leading pathname components, leaving just the filename.
		my @files = ();
		if ($autoconf_modified) {
		    ( my $file = $AutoconfigfilePath ) =~ s{.*[/\\]}{};
		    push @files, $file;
		}
		if ($overrideconf_modified) {
		    ( my $file = $AutoconfigOverridefilePath ) =~ s{.*[/\\]}{};
		    push @files, $file;
		}
		spool_config_change_message( $normal_spool_filename, \@result_buf, \@files );
	    }

	    # Force a reload of config.
	    $reload_config = 1;
	}

	# If a new config file was pulled ...
	# Note that a reload of the configuration may result in a reversion to the auto-config setup,
	# which will implicitly destroy the validity of any derived or overwritten values we try to
	# save across this call, such as $g_config{Use_Long_Hostname} or $HostconfigfilePath.
	if ($reload_config) {
	    if (
		!reload_config(
		    $AutoconfigfilePath,    $AutoconfigOverridefilePath, \$HostconfigfilePath, \$auto_config,
		    $normal_spool_filename, \@result_buf,                $head_path,           $first_reload
		)
	      )
	    {
		$logger->error("ERROR:  Failed to reload config.");
		$config_error = 1;
	    }
	    else {
		$first_reload = 0;
		$config_error = 0;
	    }
	    $reload_config = 0;
	}

	if ( $auto_config == 0 ) {
	    $logger->debug("DEBUG:  Poller only reads gdma auto configuration and host configuration file.");
	}
	elsif ( $auto_config == 1 ) {
	    $logger->debug("DEBUG:  Executing poller auto mode.");
	}

	if ($startup) {
	    ## Send the poller startup information to the monitor server.
	    spool_startup_info( $normal_spool_filename, $HostconfigfilePath, \@result_buf );

	    ## Send it only at start-up.
	    $startup = 0;
	}

	if ( defined( $g_config{Enable_Poller_Plugins_Upgrade} ) and
	    $g_config{Enable_Poller_Plugins_Upgrade} =~ /^[O|o]n$/ and not $config_error ) {
	    ## Host config file is updated and Enable_Poller_Plugins_Upgrade is On, so check for plugins upgrade.
	    upgrade_plugins( $head_path, $g_config{Poller_Plugin_Directory}, $g_config{Poller_Plugins_Upgrade_URL} );
	}

	# In the autoconfig mode we simply sleep till the next iteration.
	# Run the checks only if auto_config mode is not set.
	# This does not also depend on "not $config_error" as we do for upgrading plugins just above,
	# because it effectively implicitly does so by the manner in which $auto_config is computed
	# inside the call to reload_config() above.  Otherwise, we would risk running checks using
	# inappropriate plugins.
	if ( $auto_config == 2 ) {
	    ## Execute the normal mode for poller.
	    ## Perform configured system checks and send heartbeat messages.
	    run_normal_mode( $loop_start_time, $normal_spool_filename, $hires_time, $hires_time_format, \@result_buf, \@conf_files );
	}

	++$loopcount;

	# Compute the time to wait before the next execution.
	my $exec_time = &$hires_time() - $loop_start_time;
	if ( $debug == 2 ) {
	    $logger->stats("STATS:  Loop count=$loopcount.  Last loop exec time = " . sprintf( $hires_time_format, $exec_time ) . " seconds.");
	}

	# We are to run only once, if "-x".
	last if ( $g_opt{x} );

	# If it's time to sleep, do so.  Note that if the system time jumps for some reason (e.g.,
	# from an asynchronous NTP time-slew adjustment, or from a manual system-time correction), the
	# execution time may come out negative.  So we need to protect ourselves from that possibility.
	# Poller_Proc_Interval is in seconds.
	if ( ( $exec_time >= 0 ) and ( $exec_time < $g_config{Poller_Proc_Interval} ) ) {
	    my $wait_time = int( $g_config{Poller_Proc_Interval} - $exec_time );
	    $logger->debug("DEBUG:  Waiting $wait_time seconds ...");
	    sleep $wait_time;
	}
    }
    return !$failed;
}

sub set_config_defaults {
    $PluginPull_Timeout = $Default_PluginPull_Timeout;
    if ( defined $g_config{PluginPull_Timeout} ) {
	if ( $g_config{PluginPull_Timeout} =~ /^\d+$/ ) {
	    $PluginPull_Timeout = $g_config{PluginPull_Timeout};
	}
	else {
	    $logger->warn("WARNING:  PluginPull_Timeout is improperly defined; defaulting to $PluginPull_Timeout.");
	}
    }
}

sub initialize_auto_register_cycle {
    my $Auto_Register_Max_Interval =
      defined( $g_config{Auto_Register_Max_Interval} ) ? $g_config{Auto_Register_Max_Interval} : $Default_Auto_Register_Max_Interval;
    $Auto_Register_Attempts =
      defined( $g_config{Auto_Register_Attempts} ) ? $g_config{Auto_Register_Attempts} : $Default_Auto_Register_Attempts;
    $Auto_Register_Cycle_Limit =
      $Auto_Register_Max_Interval / ( $g_config{Poller_Proc_Interval} <= 0 ? $Default_Poller_Proc_Interval : $g_config{Poller_Proc_Interval} );
    $Auto_Register_Cycle_Count           = 0;
    $Auto_Register_Cycle_Period          = 1;
    $Auto_Register_Cycle_Previous_Period = 0;
}

sub reset_auto_register_cycle {
    if ( $Auto_Register_Attempts ne 'never' and $Auto_Register_Attempts ne 'once' ) {
	$Auto_Register_Cycle_Count = 0;
	if ( $Auto_Register_Attempts eq 'arithmetic' or $Auto_Register_Attempts eq 'exponential' or $Auto_Register_Attempts eq 'fibonacci' ) {
	    $Auto_Register_Cycle_Period          = 1;
	    $Auto_Register_Cycle_Previous_Period = 0;
	}
	elsif ( $Auto_Register_Attempts eq 'periodic' ) {
	    $Auto_Register_Cycle_Period = $Auto_Register_Cycle_Limit;
	}
    }
}

sub run_auto_register_cycle {
    my $OverridefilePath        = shift;
    my $head_path               = shift;
    my $priority_spool_filename = shift;
    my $auto_registered         = 0;

    # Auto-Setup, if enabled, takes precedence over Auto-Registration.  However, it is invoked elsewhere, not here.
    if ( defined( $g_config{Enable_Auto_Setup} ) && $g_config{Enable_Auto_Setup} =~ /^on$/i ) {
	return 0;
    }

    if ($Auto_Register_Attempts ne 'never') {
	if ($Auto_Register_Cycle_Count == 0) {
	    if (auto_register($OverridefilePath, $head_path, $priority_spool_filename)) {
		$auto_registered = 1;
		reset_auto_register_cycle();
	    }
	}
	++$Auto_Register_Cycle_Count;
	if ($Auto_Register_Cycle_Count >= $Auto_Register_Cycle_Period && $Auto_Register_Attempts ne 'once') {
	    $Auto_Register_Cycle_Count = 0;
	    if ($Auto_Register_Attempts eq 'arithmetic') {
		++$Auto_Register_Cycle_Period;
	    }
	    elsif ($Auto_Register_Attempts eq 'exponential') {
		$Auto_Register_Cycle_Period *= 2;
	    }
	    elsif ($Auto_Register_Attempts eq 'fibonacci') {
		my $Cycle_Period = $Auto_Register_Cycle_Period;
		$Auto_Register_Cycle_Period += $Auto_Register_Cycle_Previous_Period;
		$Auto_Register_Cycle_Previous_Period = $Cycle_Period;
	    }
	    if ($Auto_Register_Cycle_Period > $Auto_Register_Cycle_Limit) {
		$Auto_Register_Cycle_Period = $Auto_Register_Cycle_Limit;
	    }
	}
    }
    return $auto_registered;
}

################################################################################
#
#   reload_config()
#
#   Reads the config file, checks syntax and validity of the values set, by
#   calling validate_config(), a GDMA::Utils subroutine.  It updates the global
#   %g_config hash only if the new configfile passes the syntax and validation
#   check.  It spools an error message if the configfile fails the sanity check.
#   Returns 1 on successful reading and validation, 0 otherwise.
#
#   Arguments:
#   $AutoconfigfilePath - Full path of the main autoconfig file.
#   $AutoconfigOverridefilePath - Full path of the autoconfig override file.
#   $HostconfigfilePath - Reference to the full path of the host config file.
#   $auto_config - This is an output parameter, that triggers auto config mode.
#                  Set to 1, if the host config file does not exist, or if
#                  autoconfig flag is set in the hostconfig.
#   $normal_spool_filename - Spool file name.  If the config is found corrupt,
#                            an error message will be spooled.
#   $result_buf - A reference to a buffer containing results.
#   $head_path - Root path for the installation.
#   $first_reload - A flag that indicates whether we are reloading for the
#                   first time.
#
################################################################################
sub reload_config {
    my ( $AutoconfigfilePath, $AutoconfigOverridefilePath, $HostconfigfilePath, $auto_config, $normal_spool_filename, $result_buf, $head_path,
	$first_reload ) = @_;
    my %tmp_config = ();
    my $ret_val    = 1;
    my $ret;
    my $errstr;

    $logger->debug("DEBUG:  Reloading config file.");

    # Don't set normal or auto mode.  In this case, poller will only
    # check for valid updated auto, override, and host config files.
    $$auto_config = 0;

    # First, read the autoconfig file into a temp hash.
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

    # Set auto config mode if enabled.
    $$auto_config = 1 if ( $tmp_config{Enable_Auto} =~ /^[Oo]n$/ );

    # FIX MINOR:  Our error checking is very poor here, because we haven't done an
    # incremental check at this point on the validity of either the main autoconfig
    # file or the override config file, so we can't really tell clearly when errors
    # occur in those components.

    # We need to re-evaluate $$HostconfigfilePath because either the auto-config
    # file or the override file may have changed its Use_Long_Hostname value or
    # its Forced_Hostname value, especially the latter in the override file.
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
    # If GDMA_Multihost = on, then function as a child server and
    # read multiple configurations (generally used for Windows).
    # FIX MINOR:  do we need case-insensitive matching here?
    if ( defined( $tmp_config{GDMA_Multihost} ) && $tmp_config{GDMA_Multihost} eq "on" ) {
	## Read multiple configuration files from a folder, instead of
	## reading just the host config corresponding to the local host.
	## FIX MINOR:  change internals of GDMA::Utils::read_multiple_config() to validate after read_config() call
	$ret = GDMA::Utils::read_multiple_config( $$HostconfigfilePath, \%tmp_config, \$errstr );
    }
    else {
	$ret = GDMA::Utils::read_config( $$HostconfigfilePath, \%tmp_config, \$errstr, 0 );
    }
    if ($ret) {
	$logger->trace("TRACE:  Successfully read host config file.");

	if (   defined( $tmp_config{GDMA_Multihost} )    && $tmp_config{GDMA_Multihost} eq "on"
	    && defined( $tmp_config{Enable_Auto_Setup} ) && $tmp_config{Enable_Auto_Setup} =~ /^on$/i )
	{
	    ## We won't disable anything as a result of this condition, but on the other hand we
	    ## want to make it clear that we don't support Auto-Setup when operating in multi-host
	    ## mode, since we have no means for remote discovery of assets on the secondary hosts.
	    $logger->error("ERROR:  Having GDMA_Multihost and Enable_Auto_Setup both enabled is not a supported configuration.");
	}

	# Force 8.3 pathname components on the Windows platform, so when we substitute the
	# $Plugin_Directory$ macro within gdma_run_checks.pl, we don't end up with spaces
	# in the path that will confuse PowerShell in the "powershell -command -" input.
	# (This is an 8.3 conversion, not a space conversion, so it's not a guarantee.)
	if ( $^O eq 'MSWin32' && defined( $tmp_config{Poller_Plugin_Directory} ) ) {
	    ## The documentation for Win32::GetShortPathName() says:
	    ##     For path components where the file system has not generated the short form the
	    ##     returned path will use the long form, so this function might still for instance
	    ##     return a path containing spaces.  Returns undef when the PATHNAME does not exist.
	    my $Short_Poller_Plugin_Directory = Win32::GetShortPathName( $tmp_config{Poller_Plugin_Directory} );
	    if ( defined $Short_Poller_Plugin_Directory ) {
		if ($Short_Poller_Plugin_Directory =~ /\s/) {
		    $logger->warn("WARNING:  The path for Poller_Plugin_Directory contains whitespace, and won't work with PowerShell.");
		}
		$tmp_config{Poller_Plugin_Directory} = $Short_Poller_Plugin_Directory;
	    }
	    else {
		## FIX MINOR:  In this case, we should make the configuration validation below fail.
		$logger->error("ERROR:  Configuration contains non-existent path for Poller_Plugin_Directory.");
	    }
	}

	# Check the syntax and validate the values.
	if ( GDMA::Utils::validate_config( \%tmp_config, \$errstr ) ) {
	    $logger->debug("DEBUG:  Host config file syntax is ok.");

	    # The config file passed the sanity check.  Deep copy the temporary
	    # hash into the global config hash.  We want the actual values to be
	    # copied over and not the references.
	    %g_config = %{ dclone( \%tmp_config ) };

	    # Use Enable_Auto from the gdma_auto.conf file.
	    $g_config{Enable_Auto} = ( $$auto_config == 1 ) ? "On" : "off";

	    # Set normal mode independent of Enable_Auto since host file is present.
	    $$auto_config = 2;
	}
	else {
	    $logger->error("ERROR:  The host config file is corrupt:  $errstr");
	    $ret_val = 0;

	    # The first reload failed.  We can't process checks without a valid host config.
	    if ($first_reload) {
		## We need to read the auto conf again and copy its contents to
		## %g_config.  We cannot directly copy %tmp_config, as it now
		## contains the corrupt host config.  We could have previously
		## copied the contents of auto conf, however in case of a corrupt
		## config we do not always switch to autoconfigure mode.
		## For first reload we have previously read the auto conf file,
		## however if we do not read here again, modifications made in
		## auto conf will not be seen.
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

	    # Use Enable_Auto from the gdma_auto.conf file.
	    $g_config{Enable_Auto} = ( $$auto_config == 1 ) ? "On" : "off";
	}
    }
    else {
	## read_multiple_config() or read_config() failed.  Report error.
	$logger->error("ERROR:  reload_config:  $errstr");
	$ret_val = 0;

	# FIX MAJOR:  We need to re-read the auto-conf file here, or otherwise grab the
	# copy we read before, because %tmp_config might now be modified from what was
	# previously in the auto-conf file.

	# This function is called when at least one of the config files has been modified.
	# If we were already in autoconfigure mode and we do not copy here, we miss out on
	# modifications made in auto conf.  If we are switching from normal mode and we do
	# not copy, then %g_config would contain previous host config parameter values.
	%g_config = %{ dclone( \%tmp_config ) };
    }
    set_config_defaults();
    return $ret_val;
}

################################################################################
#
#   handle_config_pull()
#
#   Tries to pull the config file from the target server.  Spools autoconfig
#   message and triggers autoconfig mode by deleting on-disk host config file,
#   if the pull has been failing for too long.  If the config file is
#   updated, spools a config change message to the GW monitor.
#
#   Arguments:
#   $OverridefilePath - The path to a possible extra config file which can
#                       override values from the main auto-config file.
#   $HostconfigfilePath - A reference to the full path of the host config file.
#                         This path may be changed by the call.
#   $time_last_pulled - A reference.  Will be updated with the new pull time.
#   $reload_config - A reference.  Will be set to 1, if the config file is
#                    found updated on the server.
#   $normal_spool_filename - Spool file name.
#   $priority_spool_filename - Spool file name.
#   $result_buf - A reference to a buffer containing results.
#   $head_path - Root path for the installation.
#   $conf_files - A reference to an array where a list of config files will be
#                 recorded.
#   $auto_config - A reference to autoconfig/normal mode
#   $recurse - Boolean, whether to call handle_config_pull() again if
#              auto-registration is run and succeeds.
#
################################################################################
sub handle_config_pull {
    my (
	$OverridefilePath,      $HostconfigfilePath,      $time_last_pulled, $reload_config,
	$normal_spool_filename, $priority_spool_filename, $result_buf,       $head_path,
	$conf_files,            $auto_config,             $recurse
    ) = @_;
    my $conf_modified = 0;

    # We don't yet have a complicated model for how often Auto-Setup should be attempted.  For the time
    # being, we ask the server on every polling cycle whether it has new instructions and/or trigger
    # files for us.  A later version of this daemon might check for such files less often, under control
    # of a config-file option, to reduce the burden on the server of having lots of GDMA clients all
    # asking all the time "are we there yet?" in terms of whether new discovery files are available.
    #
    # FIX MINOR:  Consider adding a config-file option to tell how frequently to request discovery files
    # from the server.  Compare to how ConfigFile_Pull_Cycle might already be operating in that regard,
    # and consider how complex we want our request scheduling to be.
    #
    if ( defined( $g_config{Enable_Auto_Setup} ) && $g_config{Enable_Auto_Setup} =~ /^on$/i ) {
	## The run_auto_setup() routine returns complex status values, but in this calling context,
	## there is really nothing we can or should do to act differently depending on what happened.
	run_auto_setup($OverridefilePath);
    }

    # The lock file for config change.
    my $config_lock_file;

    # Lock file handle.
    my $config_lockh;
    my $fetched_config;

    # Array to hold the list of modified conf files.
    # In multi-host mode, there can be more than one of them.
    my @modified_conf_filenames = ();

    # When calling internal routines to fetch a config file, pass back through the stack
    # a bit more detail on failure than just the return code, so this calling code can
    # see whether we had an unexpected situation (like the mirror failing with a die),
    # and handle that condition properly in the logic at this level.
    my $first_try_failure_message  = undef;
    my $second_try_failure_message = undef;

    # Get the config lock filename based on platform.
    $config_lock_file = GDMA::Utils::get_config_lock_filename($head_path);

    $logger->debug("DEBUG:  Attempting config file pull.");
    $$reload_config = 0;

    # Check if we know where to pull from ...
    if ( !defined( $g_config{Target_Server} ) ) {
	$logger->error("ERROR:  handle_config_pull:  target server is not defined");
	return;
    }

    # First we need to acquire a non-blocking lock for the config file.
    # We will try a non-blocking lock; we don't want to be stalled for too long.
    # Open the lock file.
    my $old_umask = umask 0133;
    if ( !open( $config_lockh, '>', $config_lock_file ) ) {
	$logger->error("ERROR:  handle_config_pull:  Failed to open the config lockfile $config_lock_file ($!).");
	umask $old_umask;
	return;
    }
    umask $old_umask;

    # Try the lock.
    # FIX LATER:  Compare to Monarch file locking, but also consider cross-platform (Windows) usage.
    if ( not GDMA::Utils::try_lock($config_lockh) ) {
	$logger->error("ERROR:  handle_config_pull:  Failed to acquire config lock");
	close($config_lockh);
	return;
    }

    # Now, try fetching.
    $logger->debug("DEBUG:  Got the lock; fetching config file.");

    # To begin with, try to pull the host config file via http/s.
    # We don't have to wait until the pull is successful --
    # we will run in autoconfig mode if the pull fails.
    # Parse the target server in the config file, to get the
    # first one in the list, which will be comma separated.
    my $target_addr = ( split( /[,\s]+/, $g_config{Target_Server} ) )[0];

    # Set the values for host config filepath based on platform and on the auto-config setup.
    # If the auto-config file doesn't specify whether to use a long or short hostname, we first
    # try the long form (the most specific construction), and then fall back to the short form
    # (the most general construction) if a config file using the long form is not available.
    my $Use_Long_Hostname = defined( $g_config{Use_Long_Hostname} ) ? $g_config{Use_Long_Hostname} : 'on';
    my $hostname = GDMA::Utils::my_hostname( $Use_Long_Hostname, $g_config{Forced_Hostname}, $g_config{Use_Lowercase_Hostname} );
    $$HostconfigfilePath =
      install_hostconfig_filepaths( $head_path, $Use_Long_Hostname, $g_config{Forced_Hostname}, $g_config{Use_Lowercase_Hostname} );
    $logger->trace("TRACE:  Target address: $target_addr; Filename: $$HostconfigfilePath");
    $fetched_config =
      fetch_config_file( $target_addr, $hostname, $$HostconfigfilePath, \$conf_modified,
	\@modified_conf_filenames, $conf_files, \$first_try_failure_message );
    if ($fetched_config) {
	$$time_last_pulled = time;
	## For later consistency, cement a provisional success into place for future
	## determination of the hostname form.  This can be overridden when we later
	## reload the configuration from a fetched configuration file.
	$g_config{Use_Long_Hostname} = $Use_Long_Hostname if not defined $g_config{Use_Long_Hostname};
    }
    elsif ( not defined $g_config{Use_Long_Hostname} ) {
	## We used a long-form hostname before.  Fall back to trying the short-form hostname.
	$hostname = GDMA::Utils::my_hostname( $g_config{Use_Long_Hostname}, $g_config{Forced_Hostname}, $g_config{Use_Lowercase_Hostname} );
	$$HostconfigfilePath = install_hostconfig_filepaths(
	    $head_path,
	    $g_config{Use_Long_Hostname},
	    $g_config{Forced_Hostname},
	    $g_config{Use_Lowercase_Hostname}
	);
	$logger->trace("TRACE:  Target address - $target_addr, Filename - $$HostconfigfilePath");
	$fetched_config =
	  fetch_config_file( $target_addr, $hostname, $$HostconfigfilePath, \$conf_modified, \@modified_conf_filenames, $conf_files,
	    \$second_try_failure_message );
	if ($fetched_config) {
	    $$time_last_pulled = time;
	}
    }
    $logger->debug("DEBUG:  Poller is using hostname \"$hostname\".");

    # We are done with the config lock for now.  Surrender it.
    if ( not GDMA::Utils::release_lock($config_lockh) ) {
	$logger->error("ERROR:  handle_config_pull:  Could not relinquish the lock:  $!");
	## We think we are never going to get the lock again.  May as well quit.
	## But first, we close the file (which should in itself release the lock).
	## We need to perform this close() in case the die() is caught by an
	## enclosing eval{}; block, to prevent a file descriptor leak.
	close($config_lockh);
	die "Could not relinquish the config lock:  $!";
    }
    if ( !close($config_lockh) ) {
	$logger->error("ERROR:  handle_config_pull:  Could not close the lock file");
    }
    if ( !$fetched_config ) {
	$logger->error("ERROR:  Failed to fetch the host config file");

	# We prefer to report a second failure message in preference to a first failure message,
	# mainly because it is more current and perhaps now better reflects the state of the world.
	if ( $second_try_failure_message || $first_try_failure_message ) {
	    $logger->error("ERROR:  Skipping auto-registration because config-file fetching failed for a strange reason listed above.");
	    ## If the mirroring failed way down in get_gdma_cfg_file_using_http calling do_timed_mirror,
	    ## then $first_try_failure_message and/or $second_try_failure_message will not be undef.
	    ## Example case:  way down in Net::HTTP::read_entity_body called via LWP Useragent, the
	    ## mirror() died mysteriously.  In this case, report on the error via the poller service,
	    ## but DON'T try to auto register.
	    ##
	    ## Push something useful into the spooler file for the poller service :).
	    ##
	    spool_a_health_message( "Configuration file fetch failure:  " . ( $second_try_failure_message || $first_try_failure_message ), 2 );
	    return;    # RETURN here -- don't try to auto register (below)
	}

	if ( $g_config{Poller_Pull_Failure_Interval} != 0 ) {
	    $logger->debug("DEBUG:  Ignoring fetch failures for $g_config{Poller_Pull_Failure_Interval} seconds.");
	}

	# Force a re-load of the local config file.  Make sure that
	# we re-read the local config file.  We want to fall back to
	# autoconfig mode, if the local file is gone too.
	$$reload_config = 1;

	# Check if the pull's been failing for too long.
	# "Poller_Pull_Failure_Interval" is in seconds.
	# If Poller_Pull_Failure_Interval = 0, never switch to autoconfig mode.
	if ( ( $g_config{Poller_Pull_Failure_Interval} != 0 ) and ( ( time - $$time_last_pulled ) > $g_config{Poller_Pull_Failure_Interval} ) )
	{
	    $logger->debug("DEBUG:  Fetch failed for $g_config{Poller_Pull_Failure_Interval}" . " seconds.");

	    # If Enable_Auto is On, then force autoconfig mode by deleting the on-disk Host configfile.
	    if ( $g_config{Enable_Auto} =~ /^[Oo]n$/ ) {
		$$auto_config = 1;
		$logger->debug("DEBUG:  handle_config_pull:  setting autoconfig mode.");
		## We don't need to delete both the long-hostname and short-hostname forms of the file
		## (to make sure we don't confuse ourselves by deleting one form and then reading the
		## other form), because the subsequent reload_config() call will use this same path.
		unlink($$HostconfigfilePath) or $logger->error("ERROR:  Failed to unlink hostconfig file:  $!");
	    }
	}
	else {
	    ## Pull failed and we are not forcing autoconfig mode just yet.
	    ##
	    ## For now, we will just run normal mode with the config that we have, though we will
	    ## attempt (just below) to run auto-registration (or failing that, auto-configuration)
	    ## to address this issue in the longer run.
	}

	# Call the auto-registration API on the target server if it makes sense to do so now.
	# If we don't have credentials for that, we'll end up emitting an auto-configure message instead.
	# (Or so we used to; that protocol is now considered to be obsolete, so we don't call it any more.)
	my $auto_registered = run_auto_register_cycle( $OverridefilePath, $head_path, $priority_spool_filename );
	if ( $auto_registered && $recurse ) {
	    ## The server should now have an externals file waiting for us.  Let's try to load it
	    ## right away, instead of waiting a full polling cycle to do so.
	    $recurse = 0;
	    handle_config_pull(
		$OverridefilePath,      $HostconfigfilePath,      $time_last_pulled, $reload_config,
		$normal_spool_filename, $priority_spool_filename, $result_buf,       $head_path,
		$conf_files,            $auto_config,             $recurse
	    );
	}
    }
    else {
	$logger->debug("DEBUG:  Successfully fetched config file.");

	# We need to reload only if the conf file was modified.
	$$reload_config = $conf_modified;

	# If the conf was modified, notify the GW monitor.
	if ( $conf_modified and ( scalar(@modified_conf_filenames) > 0 ) ) {
	    spool_config_change_message( $normal_spool_filename, $result_buf, \@modified_conf_filenames );
	}
    }
}

################################################################################
#
#   auto_register()
#
################################################################################
sub auto_register {
    my $OverridefilePath        = shift;
    my $head_path               = shift;
    my $priority_spool_filename = shift;

    my $Auto_Register_User            = $g_config{Auto_Register_User};
    my $Auto_Register_Pass            = $g_config{Auto_Register_Pass};
    my $Auto_Register_Host_Profile    = $g_config{Auto_Register_Host_Profile}    || '';
    my $Auto_Register_Service_Profile = $g_config{Auto_Register_Service_Profile} || '';

    # Don't call auto-registration without credentials in hand.  Previously,
    # we would fall back to the older auto-configure protocol.  That protocol
    # is now considered to be obsolete, so we don't call it any more.
    if ( !$Auto_Register_User or !$Auto_Register_Pass ) {
	## return auto_configure($head_path, $priority_spool_filename);
	$logger->notice( "NOTICE:  Cannot run auto-registration without credentials in hand"
	      . " (both the Auto_Register_User and Auto_Register_Pass configuration options would need to be set)."
	      . "  If you really don't want auto-registration to ever run (and not see this message again),"
	      . " another way to disable it is to set Auto_Register_Attempts to \"never\""
	      . " instead of its usual value of \"fibonacci\"." );
	return 0;
    }

    my $target_addr = ( split( /[,\s]+/, $g_config{Target_Server} ) )[0];

    my $register_by_profile_url = $target_addr . '/foundation-webapp/restwebservices/autoRegister/registerAgentByProfile';

    # Agent type can be 'GDMA', 'JDMA', 'CEMA' (not sure what that is).
    # For our purposes, 'GDMA' is the right choice for now.  In the future, this might be
    # generalized to also indicate the platform on which this host is running, as best it
    # can be determined (bare metal, VMware, EC2-Classic, EC2-VPC, KVM, Solaris global or
    # non-global zone, Linux Container, Linux Docker, etc.).  That would be done to provide
    # the server with "believability" information as to the submitted data values.
    my $agent_type = 'GDMA';

    # We must force the use of the fully-qualified hostname, as gethostbyname() is likely to return 127.0.0.1 as its result for
    # a shortname.  FIX MAJOR:  That said, we need to test this on a machine where the canonical machine name is a shortname.
    my $long_host_name = GDMA::Utils::my_hostname( 'on', $g_config{Forced_Hostname}, $g_config{Use_Lowercase_Hostname} );

    # Note that gethostbyname() can return undef if passed an argument which is not actually found in DNS.  This
    # can happen, for instance, if the host is renamed on the GroundWork Monitor server to something that is not
    # an actual hostname on the network.  You can thereby end up with a Forced_Hostname in the gdma_override.conf
    # file, which hostname is not known to anyone, and thus does not map to any IP address.  So we need to check
    # the gethostbyname() return value to ensure it is defined.
    #
    # Also note that gethostbyname() is capable of returning all IP addresses on the machine, but we don't have any
    # way to figure out which is the "best" (except perhaps to reject any that look like localhost, unless that's
    # the only choice), so we just go with whatever standard algorithm is already embedded within gethostbyname()
    # when it is called in scalar context.  Presumably, any errors made here will be sorted out on the server side.
    my $packed_ip = gethostbyname($long_host_name);
    my $host_ip = '';
    # Check if we could resolve the hostname to an IP address.
    if ( defined($packed_ip) ) {
	$host_ip = inet_ntoa($packed_ip);
    }
    else {
	$logger->error("ERROR:  Failed to resolve hostname \"$long_host_name\".");
    }
    my $host_mac = '';

    # FIX LATER:  Should we use the full "platform" ("$os-$processor") that we establish in GDMA, instead?
    my $os = ( $^O eq 'MSWin32' ) ? 'windows' : $^O;

    foreach my $addr ( get_addresses() ) {
	if ( $addr->{iActive} ) {
	    foreach my $rasIP ( @{ $addr->{rasIP} } ) {
		if ( $rasIP eq $host_ip ) {
		    $host_mac = $addr->{sEthernet};
		    last;
		}
	    }
	    last if $host_mac;
	}
    }
    $host_mac = get_address() if not $host_mac;

    my @register_params = ();
    push @register_params, "username="             . uri_escape($Auto_Register_User);
    push @register_params, "password="             . uri_escape($Auto_Register_Pass);
    push @register_params, "agent-type="           . uri_escape($agent_type);
    push @register_params, "host-name="            . uri_escape($long_host_name);
    push @register_params, "host-ip="              . uri_escape($host_ip);
    push @register_params, "host-mac="             . uri_escape($host_mac);
    push @register_params, "operating-system="     . uri_escape($os);
    push @register_params, "host-profile-name="    . uri_escape($Auto_Register_Host_Profile);
    push @register_params, "service-profile-name=" . uri_escape($Auto_Register_Service_Profile);

    my $params = join( '&', @register_params );

    my $successful = 1;
    my $response   = undef;
    my $errormsg   = undef;

    my $ssl_opts = GDMA::Utils::gdma_client_ssl_opts($logger);
    ## Auto-Register/2.0 is just like Auto-Register/1.0 except for
    ## its treatment of lettercase in the submitted hostname.
    my $ua = LWP::UserAgent->new(
	agent    => 'GDMA Client ' . get_version() . ' Auto-Register/2.0',
	ssl_opts => $ssl_opts,
    );
    my $req = HTTP::Request->new( POST => $register_by_profile_url );
    # FIX MAJOR:  The content type should instead be specified as:
    #     $req->content_type('application/x-www-form-urlencoded');
    # since we are applying uri_escape() above to these parameters.
    # However, we need to test against a GWMEE 6.7.0 system to see
    # whether it receives the parameters in the same manner under
    # this proper content type (or for that matter, under the bad
    # content type as well).  In particular, look at the host MAC
    # address, which is likely to have had ":" characters encoded
    # to be represented as "%3A" on the wire, to see if the wire
    # encoding is visible on the receiving end in either case.
    # This issue is tracked as GDMA-377.
    $req->content_type('text/plain');
    $req->content($params);

    # The timeout is currently hardcoded here.  Possibly in some future version,
    # we might want to make this configurable, via an Auto_Register_Timeout setting.
    my $Auto_Register_Timeout = 30;

    # Default is both GET and HEAD redirect, if we don't make this call.  POST calls are already not
    # redirectable by default, but you can't be too careful.  We are now (as of GDMA 2.3.2) disabling
    # automatic redirects, as we must intervene and prevent a possible HTTPS-to-HTTP downgrade.
    # So all redirects are now handled manually here.
    $ua->requests_redirectable( [] );

    my $remaining_fetches = $g_config{Max_Server_Redirects};
    $remaining_fetches = $Default_Max_Server_Redirects if not defined $remaining_fetches;

    for ( my $fetch = 1, ++$remaining_fetches ; $fetch && $remaining_fetches > 0 ; --$remaining_fetches ) {
	$fetch = 0;    # Only fetch upon redirect if explicitly commanded below.

	if ( not GDMA::Utils::is_valid_ca_path( \$errormsg, $register_by_profile_url ) ) {
	    $logger->error("ERROR:  $errormsg");
	    $successful = 0;
	}
	elsif ( not do_timed_request( 'Auto-Registration', $ua, $req, $Auto_Register_Timeout, \$response, \$errormsg ) ) {
	    chomp $errormsg;
	    $logger->error("ERROR:  Auto-Registration failed:  $errormsg");
	    $successful = 0;
	}
	elsif ( not $response->is_success ) {
	    if ( is_redirect( $response->code ) ) {
		## We allow the full response content, if any, to be dumped to the log just below, so we don't immediately
		## declare an unsuccessful Auto-registration ("$successful = 0;").  But we do want to log the formal
		## redirect Location, in case that might provide simpler diagnostic information.
		$logger->notice( "NOTICE:  Auto-registration request was not processed -- " . $response->status_line );
		my $redirect_location = $response->header('Location');
		## Unless the protocol is downgraded from HTTPS to HTTP, we'll allow the redirect,
		## subject to the configured (or defaulted) Max_Server_Redirects value.
		if ( not defined $redirect_location ) {
		    $logger->notice(" (got redirected, but with no new location supplied)");
		}
		elsif ($redirect_location =~ m{^https?://}i
		    && ( $register_by_profile_url =~ m{^http://}i || $redirect_location =~ m{^https://}i )
		    && $remaining_fetches >= 2 )
		{
		    $logger->notice(" (redirecting to $redirect_location)");
		    $register_by_profile_url = $redirect_location;
		    $req->uri($register_by_profile_url);
		    $fetch = 1;
		}
		else {
		    $logger->notice(" (ignoring redirection to $redirect_location)");
		}
	    }
	}
    }

    my $xmlresponse;
    if ($successful) {
	my $http_status = $response->code;
	$xmlresponse = $response->decoded_content( ref => 1 );
	if ( $response->is_success && $http_status == 200 ) {
	    $logger->notice("NOTICE:  Auto-registration request was processed.");
	}
	else {
	    ## We suppress the first message if we already emitted similar content just above.
	    $logger->error("ERROR:  Auto-registration request was not processed; HTTP/S response code = $http_status.")
	      unless !$response->is_success and is_redirect( $response->code );
	    $logging->log_message("XML response was:\n$$xmlresponse") if defined $$xmlresponse;
	    $successful = 0;
	}
    }

    my $xml;
    if ($successful) {
	eval { $xml = XMLin( $$xmlresponse, KeyAttr => [], ForceArray => 0, KeepRoot => 0, SuppressEmpty => '' ); };
	if ($@) {
	    chomp $@;
	    $logger->error("ERROR:  XMLin() on auto-registration response failed ($@).");
	    $successful = 0;
	}
    }

    my $code;
    my $message;
    if ($successful) {
	$code    = $xml->{code};
	$message = $xml->{message};
	if ( not defined $code ) {
	    $logger->error("ERROR:  Failed to auto-register; no error code is available.");
	    $successful = 0;
	}
	elsif ( $code ne 'SUCCESS' && $code ne '0' ) {
	    chomp $message;
	    $logger->error("ERROR:  Failed to auto-register; code is $code; message is:\n$message");
	    $successful = 0;
	}
    }

    if ($successful) {
	## Finally, a supposedly successful auto-registration.
	##
	## Do whatever is appropriate with the hostname by which this machine will be known to the server.
	## We used to only match lowercase here, because that was our standard for hostnames elsewhere
	## within GDMA.  But now we allow mixed-case.  This is just a very basic validation check; we're not
	## concerned with exactness of formulation, since we assume the server must have got it right.
	if ( $message =~ /^hostname=([-.a-zA-Z0-9]+?)$/m ) {
	    my $registered_hostname = $1;
	    chomp $message;
	    $logger->info("INFO:  Auto-registration call succeeded, with these server-side messages:\n$message");
	    if ( write_override_file( $OverridefilePath, $registered_hostname ) ) {
		$logger->notice("NOTICE:  Forced_Hostname is being dynamically changed to \"$registered_hostname\".");
		$g_config{Forced_Hostname} = $registered_hostname;
	    }
	    else {
		$logger->error("ERROR:  Forced_Hostname has not been changed due to file-handling problems.");
		$successful = 0;
	    }
	}
	else {
	    $logger->error("ERROR:  Auto-registration supposedly succeeded on the server side, but returned no valid hostname.");
	    if ($message) {
		chomp $message;
		$logger->notice("NOTICE:  Auto-registration server-side messages:\n$message");
	    }
	    $successful = 0;
	}
    }

    return $successful;
}

# FIX MINOR:  This routine should be moved to GDMA::Utils if it will be used by other code, such as the standalone "discover" tool.
sub write_override_file {
    my $OverridefilePath  = shift;
    my $hostname_override = shift;
    my $outcome           = 1;

    my $tempfile = "$OverridefilePath-new";

    my $old_umask = umask 0133;
    if ( not open( OVERRIDE, '>', $tempfile ) ) {
	$logger->error("ERROR:  Cannot open $tempfile ($!).");
	umask $old_umask;
	return 0;
    }
    umask $old_umask;

    print( OVERRIDE "# DO NOT EDIT:  The contents of this file are subject to being overwritten,\n" ) or $outcome = 0;
    print( OVERRIDE "# so you should not consider this data to be permanently persistent.\n" )        or $outcome = 0;
    print( OVERRIDE "# Also, the contents of this file may end up being world-readable.\n" )          or $outcome = 0;
    print( OVERRIDE "Forced_Hostname = \"$hostname_override\"\n" )                                    or $outcome = 0;
    close(OVERRIDE)                                                                                   or $outcome = 0;
    if ( not $outcome ) {
	$logger->error("ERROR:  Cannot write to $tempfile ($!).");
    }
    elsif ( not rename( $tempfile, $OverridefilePath ) ) {
	my $os_error = "$!";
	$os_error .= " ($^E)" if "$^E" ne "$!";
	$logger->error("ERROR:  Cannot rename $tempfile ($os_error).");
	$outcome = 0;
    }
    if ( not $outcome ) {
	unlink $tempfile;
    }

    return $outcome;
}

################################################################################
#
#   auto_configure()
#
#   Spool a message to the server to tell it to invoke its auto-configure
#   processing to recognize this host and generate externals for it.  The
#   auto-configure processing is an older, obsolete protocol that should
#   be completely stripped from this program now that auto-registration has
#   been fully available for some time and we even have auto-setup available.
#
################################################################################
sub auto_configure {
    my $head_path               = shift;
    my $priority_spool_filename = shift;
    my $host_ip                 = undef;
    my $packed_ip               = undef;
    my $msg_payload;
    my $errstr;
    my $blocking = 0;
    my $num_results;
    my $successful = 1;

    # The return code for autoconfig message is "UNKNOWN".
    my $return_code_unknown = 3;
    my $hostname = GDMA::Utils::my_hostname( $g_config{Use_Long_Hostname}, $g_config{Forced_Hostname}, $g_config{Use_Lowercase_Hostname} );
    my $osname   = $^O;

    $logger->debug("DEBUG:  Poller sending auto-configure message.");

    # Record the IP address for the host.
    $packed_ip = gethostbyname($hostname);

    # Check if we could resolve the hostname to an IP address.
    if ( defined($packed_ip) ) {
	$host_ip = inet_ntoa($packed_ip);
    }
    else {
	$logger->error("ERROR:  Failed to resolve hostname \"$hostname\".");
    }

    # Build the autoconfig message body.
    $msg_payload = "No configuration file in poller:  $hostname";

    # Append the IP address if we could resolve it.
    if ( defined($host_ip) ) {
	$msg_payload .= " [$host_ip]";
    }
    $msg_payload .= " running Perl compiled under $osname $Config{osvers}";

    # Add a bit more data if it's Windows.
    if ( $osname eq 'MSWin32' ) {
	my $osres = `cscript -nologo "$head_path\\libexec\\v2\\get_system_uptime.vbs" -h $hostname -w 2 -c 1`;
	$msg_payload .= " $osres";
    }
    $logger->trace("TRACE:  auto-configure message:  $msg_payload");

    # Extract the target address from the "Target_Server" directive.
    # Send the autoconfig message to the first target server.
    my $target = ( split( /[,\s]+/, $g_config{Target_Server} ) )[0];

    # E.g., https://abc.def/gdma-linux -> abc.def
    # E.g., https://abc.def            -> abc.def
    if ( $target =~ m{^\S+://([^/]+)} ) {
	$target = $1;
    }

    # The retries field should be 0, when the result is first spooled.
    my $default_retries = 0;

    my $result_str = join( '',
	$default_retries, "\t",
	$target, "\t",
	time(), "\t",
	$g_config{GDMA_Auto_Host}, "\t",
	$g_config{GDMA_Auto_Service}, "\t",
	$return_code_unknown, "\t",
	$msg_payload, "\n"
    );

    # Flush it out to the spool file immediately.
    # Make a non-blocking call, we don't want to block for too long.
    if ( !GDMA::Utils::spool_results( $priority_spool_filename, [$result_str], $blocking, \$num_results, \$errstr ) ) {
	$logger->warn("WARNING:  Failed to spool auto configure message ($errstr); it will be lost.");
	$successful = 0;
    }

    return $successful;
}

################################################################################
#
#   detect_conf_file_change()
#
#   Checks if the config file has changed since last iteration, by doing
#   a stat on it and recording the modification time.
#
#   Arguments:
#   $filePath - Full path of the config file to be detected for changes.
#   $last_modified - A reference.  The last modified time for the config file.
#                    This will be updated if the config file is found updated.
#   $modified - A reference.  This will be set to 1 if the config file is
#               found updated.
#   $optional - A flag to tell whether this config file might not exist.
#
################################################################################

# FIX MINOR:  We should use a more-sophisticated algorithm to determine whether or not
# the content of the configuration file has changed, by comparing the actual active
# option settings and see whether any of them have changed, or whether there are any new
# or now-missing options in the new copy.  We want that extra level of checking so we
# don't emit warning messages back to the server for file-level changes that are in fact
# meaningless.

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
#   spool_config_change_message()
#
#   Spools a configuration file change message, to be sent to the GW monitor.
#   This result is for "Poller_Service" on GDMA host.
#
#   Arguments:
#   $normal_spool_filename - Spool file name.
#   $result_buf - A reference to a buffer containing results.
#   $modified_conf_filenames - A reference to an array containing names of the
#                              conf files changed.
#
################################################################################
sub spool_config_change_message {
    my ( $normal_spool_filename, $result_buf, $modified_conf_filenames ) = @_;
    my $msg_payload;

    # "0" implies that the result is to be sent to all the primary targets.
    my $default_target = 0;
    my $errstr;
    my $num_results;

    # The return code for config change message is "WARNING".
    my $return_code_warn = 1;
    my $blocking         = 0;
    my $hostname = GDMA::Utils::my_hostname( $g_config{Use_Long_Hostname}, $g_config{Forced_Hostname}, $g_config{Use_Lowercase_Hostname} );

    # FIX MINOR:  What is this business messing with $" about?  The default is already a space, and
    # it's not used anywhere else that we call insert_into_result_buf() or GDMA::Utils::spool_results().
    # We should probably just use 'local $" = " ";' inside a block around where it is truly needed,
    # instead of saving and restoring the value, if we even need this at all.  Also remember, this
    # is a global variable that affects subroutines we call here, and their descendant calls, not
    # just array substitutions at this level of the code.

    # Record the old array elements separator value.
    my $old_separator = $";

    # Make sure that the array elements separator is space.
    $" = " ";

    $logger->debug("DEBUG:  Spooling configuration change message.");

    # Compose the config change message.
    $msg_payload = "Configuration change detected: ";
    $msg_payload .= "@$modified_conf_filenames at " . GDMA::Utils::get_current_time_str();
    $logger->trace("TRACE:  $msg_payload");

    # Insert the message into the result buffer.
    insert_into_result_buf( $default_target, $hostname, $g_config{Poller_Service}, $return_code_warn, $msg_payload, $result_buf );

    # Flush it out to the spool file immediately.
    # Make a non-blocking call, we don't want to block for too long.
    if ( !GDMA::Utils::spool_results( $normal_spool_filename, $result_buf, $blocking, \$num_results, \$errstr ) ) {
	## Spooling failed, but the result is still there in the buffer.
	## Hopefully, it will be spooled at a later time.
	$logger->warn("WARNING:  spool_config_change_message:  $errstr");
    }

    # Restore the old array elements separator value.
    $" = $old_separator;
}

################################################################################
#
#   spool_startup_info()
#
#   Inserts a startup message, containing information about poller
#   version and startup time into the result buffer and then spools it.
#   This message is meant for the GW monitor server.
#
#   Arguments:
#   $normal_spool_filename - spool file name.
#   $configfile - Full name of the config file (presently unused here).
#   $result_buf - A reference to a buffer containing results.
#
################################################################################
sub spool_startup_info {
    my ( $normal_spool_filename, $configfile, $result_buf ) = @_;

    # Start-up message body.
    my $msg_payload;

    # "0" implies that the result is to be sent to all the targets.
    my $default_target = 0;
    my $errstr;

    # The return code for start-up message  is "OK".
    my $return_code_ok = 0;
    my $blocking       = 0;
    my $num_results;
    my $version = get_version();

    my $hostname = GDMA::Utils::my_hostname( $g_config{Use_Long_Hostname}, $g_config{Forced_Hostname}, $g_config{Use_Lowercase_Hostname} );

    $logger->debug("DEBUG:  Spooling poller startup message.");

    # Compose the startup message.
    $msg_payload = "Poller $version started at " . GDMA::Utils::get_current_time_str();
    $logger->trace("TRACE:  spool_startup_info:  $msg_payload");

    # Use the default target.
    # The result will be for the configured "Poller_Service".
    # The return code for startup message will be "0" i.e., "OK".
    insert_into_result_buf( $default_target, $hostname, $g_config{Poller_Service}, $return_code_ok, $msg_payload, $result_buf );

    # Flush it out to spool file immediately.
    # Make a non-blocking call, we don't want to block for too long.
    if ( !GDMA::Utils::spool_results( $normal_spool_filename, $result_buf, $blocking, \$num_results, \$errstr ) ) {
	## Spooling failed, but the result is still there in the buffer.
	## Hopefully, it will be spooled at a later time.
	$logger->warn("WARNING:  spool_startup_info:  $errstr");
    }
}

################################################################################
#
#   spool_a_health_message()
#
#   Inserts a message into result buffer and then spools it.
#   This message is meant for the GW monitor server.
#
#   Arguments:
#
#   $message - the message
#   $status - status, 0,1,2,3 for ok, warning, critical, and unknown, respecively
#
################################################################################
sub spool_a_health_message {
    my ( $message, $status ) = @_;

    my $default_target = 0;    # "0" implies that the result is to be sent to all the targets.
    my $errstr;
    my $blocking = 0;
    my $num_results;
    my $result_buf = [];

    $status = 0 if not defined $status;

    my $head_path             = GDMA::Utils::get_headpath();                    # Get the root path for installation.
    my $normal_spool_filename = GDMA::Utils::get_spool_filename($head_path);    # Build the spool filename.
    my $hostname = GDMA::Utils::my_hostname( $g_config{Use_Long_Hostname}, $g_config{Forced_Hostname}, $g_config{Use_Lowercase_Hostname} );

    # Compose the startup message.
    $logger->debug("DEBUG:  spool_a_health_message:  $message");

    # The result will be for the configured "Poller_Service".
    insert_into_result_buf( $default_target, $hostname, $g_config{Poller_Service}, $status, $message, $result_buf );

    # Flush it out to the spool file immediately.
    # Make a non-blocking call, as we don't want to block for too long.
    if ( !GDMA::Utils::spool_results( $normal_spool_filename, $result_buf, $blocking, \$num_results, \$errstr ) ) {
	## Spooling failed, but the result is still there in the buffer.
	## Hopefully, it will be spooled at a later time.
	$logger->warn("WARNING:  spool_a_health_message:  $errstr");
    }
}

################################################################################
#
#   upgrade_plugins()
#
#   The poller upgrades the plugins from the GW Monitor server.  The poller
#   sends an HTTP request to the GW Monitor server along with platform,
#   architecture, and last update timestamp parameters.  The GW Monitor server
#   returns the plugins_update data in an XML format.  GW Monitor's response
#   does not contain plugin information if nothing has changed since the last
#   update timestamp for the particular platform and architecture (including the
#   implied multiplatform platform); otherwise, GW Monitor returns the relevant
#   updates for the request.  Following are the valid parameter values:
#
#       1. platform:  "aix-powerpc", "hpux-ia64", "linux-intel", "solaris-intel",
#                     "solaris-sparc", or "windows-intel" ("multiplatform" will
#                     be applied automatically by the server as well, if any
#                     such plugins have previously been uploaded to the server)
#       2. arch:  32, 64
#       3. lastUpdateTimestamp:  numeric timestamp expressed as seconds since
#                                the standard UNIX Epoch (00:00:00 UTC,
#                                January 1, 1970)
#       4. lastUpdateDate:  time in the format of "2010-10-01 12:14:05"; legacy,
#                           for human consumption only; ignored by the server
#
################################################################################
sub upgrade_plugins {
    my ( $headpath, $plugins_dir, $plugins_upgrade_url ) = @_;
    my ( %download_plugin_files, $dependency_dir, $url_params, $plugin_request_timestamp,
	$plugin_request_date, $local_plugin_status_filepath, $polled_plugin_status_filepath );
    my $update_status_filename = "plugins_update.xml";

    my $tempdirpath = '';

    # Per the platform, determine the parameter values for a plugin update HTTP request.
    if ( $^O eq 'linux' or $^O eq 'solaris' or $^O eq 'aix' or $^O eq 'hpux' ) {
	$url_params                    = 'platform=' . $platform;
	$tempdirpath                   = "$headpath/tmp";
	$local_plugin_status_filepath  = "$plugins_dir/$update_status_filename";
	$polled_plugin_status_filepath = "$tempdirpath/$update_status_filename";

	# Hard-coded path in which to store plugin dependencies.
	$dependency_dir = $headpath . '/../common/lib';
    }
    elsif ( $^O eq 'MSWin32' ) {
	$url_params                    = 'platform=' . $platform;
	$tempdirpath                   = "$headpath\\tmp";
	$local_plugin_status_filepath  = "$plugins_dir\\$update_status_filename";
	$polled_plugin_status_filepath = "$tempdirpath\\$update_status_filename";

	# Hard-coded path in which to store plugin dependencies.
	$dependency_dir = $plugins_dir;
    }

    $url_params .= '&arch=' . $os_bits;

    # Determine the plugins which need to be downloaded.
    if (
	get_plugin_file_list(
	    $plugins_upgrade_url,          $url_params,                    $plugins_dir,            $tempdirpath,
	    $local_plugin_status_filepath, $polled_plugin_status_filepath, $update_status_filename, \%download_plugin_files,
	    \$plugin_request_timestamp,    \$plugin_request_date
	)
      )
    {
	download_plugins( \%download_plugin_files, $plugin_request_timestamp, $plugin_request_date, $plugins_dir, $dependency_dir, $tempdirpath,
	    $update_status_filename );
    }
}

##############################################################################
#
#   get_plugin_file_list()
#
#   Retrieves plugin_update.xml status file from the server and the creates
#   list of plugin files which needs to be downloaded from the server.
#   Returns 0 if error happens, in other case returns 1.
#
#   Arguments:
#   $server_plugin_status_url - URL to get "plugins_update" status file
#                               from the server
#   $url_params     - contains parameters required by the plugin
#                     update HTTP request
#   $plugins_dir    - path of plugin directory
#   $tempdirpath    - path of temp directory
#   $local_plugin_status_filepath   - Path of "plugin_update" status
#                                   file on the client
#   $polled_plugin_status_filepath  - Temporary location to store downloaded
#                                        "plugin_update" status file
#   $update_status_filename     - file name of plugins update status file
#   $plugin_files               - Reference to hashes which contains
#                                 plugin files' name which need to be
#                                 downloaded from the server
#   $plugin_request_timestamp   - Reference to lastUpdateTimestamp of plugin's request
#   $plugin_request_date        - Reference to lastUpdateDate of plugin's request
#
##############################################################################
sub get_plugin_file_list {
    my (
	$server_plugin_status_url,     $url_params,                    $plugins_dir,            $tempdirpath,
	$local_plugin_status_filepath, $polled_plugin_status_filepath, $update_status_filename, $plugin_files,
	$plugin_request_timestamp,     $plugin_request_date
    ) = @_;
    my ( $plugin_useragent, $response, $errormsg, %local_plugin_files );
    my ( $polled_status_filehandler, $local_status_filehandler );
    my $debug = defined( $g_opt{d} ) ? $g_opt{d} : 0;

    my $server_status_file_url = $server_plugin_status_url . "?" . $url_params;
    my ( $polled_xml_data, $local_xml_data, $updated_xml_data );

    #my $xml_parser = new XML::Parser(Style => 'Tree', ErrorContext => 2);
    my $xml_parser = new XML::Simple( KeyAttr => [], ForceArray => 1, KeepRoot => 1 );

    # Hard-coded default value of lastUpdateTimestamp.
    my $last_update_timestamp = '0';    # 0 => 1970-01-01 00:00:00 UTC
    if ( -e $local_plugin_status_filepath ) {
	## Read the local plugins_update.xml status file.
	$local_xml_data = eval { $xml_parser->XMLin($local_plugin_status_filepath); };
	## check for the error, log errors if any or log successful xml parsing
	if ($@) {
	    ## remove module line number
	    if ( $^O eq 'MSWin32' ) {
		$@ =~ s{at [a-zA-Z]:/.*?$}{}s;
	    }
	    else {
		$@ =~ s{at /.*?$}{}s;
	    }
	    $logging->log_message('');
	    $logger->error("ERROR:  Parsing '$local_plugin_status_filepath' failed:  $@");
	    return 0;
	}
	else {
	    $logger->debug("DEBUG:  Successfully parsed '$local_plugin_status_filepath' file.");
	}

	my $lastUpdateTimestamp = $local_xml_data->{pluginUpdate}[0]->{lastUpdateTimestamp};
	$last_update_timestamp = $lastUpdateTimestamp if defined($lastUpdateTimestamp) && $lastUpdateTimestamp =~ /^\d+$/;
    }

    $server_status_file_url .= "&lastUpdateTimestamp=$last_update_timestamp";
    $$plugin_request_timestamp = $last_update_timestamp;

    my $last_update_date = strftime( '%Y-%m-%d %H:%M:%S', localtime($last_update_timestamp) );
    $server_status_file_url .= "&lastUpdateDate=$last_update_date";
    $$plugin_request_date = $last_update_date;

    # Hard-coded timeout for plugin_update request.
    my $urltimeout = 10;
    my $ssl_opts   = GDMA::Utils::gdma_client_ssl_opts($logger);
    $plugin_useragent = LWP::UserAgent->new(
	agent    => 'GDMA Client/' . get_version(),
	timeout  => $urltimeout,
	ssl_opts => $ssl_opts,
    );

    # Default is both GET and HEAD redirect, if we don't make this call.  We are now (as of GDMA 2.3.2)
    # disabling automatic redirects, as we must intervene and prevent a possible HTTPS-to-HTTP downgrade.
    # So all redirects are now handled manually here.
    $plugin_useragent->requests_redirectable( [] );

    my $remaining_fetches = $g_config{Max_Server_Redirects};
    $remaining_fetches = $Default_Max_Server_Redirects if not defined $remaining_fetches;

    for ( my $fetch = 1, ++$remaining_fetches ; $fetch && $remaining_fetches > 0 ; --$remaining_fetches ) {
	$fetch = 0;    # Only fetch upon redirect if explicitly commanded below.

	if ( not GDMA::Utils::is_valid_ca_path( \$errormsg, $server_status_file_url ) ) {
	    $logger->error("ERROR:  $errormsg");
	    return 0;
	}

	# Download "plugins_update" status file from server.
	# If download fails, then log the debug message and return error code.
	$logger->debug( "DEBUG:  Sending request to get server plugin status XML file ($server_status_file_url)"
	      . " to $polled_plugin_status_filepath (timeout is set to "
	      . $plugin_useragent->timeout()
	      . " seconds)." );

	if (
	    not do_timed_mirror(
		'Server status file fetch', $plugin_useragent, $server_status_file_url, "$polled_plugin_status_filepath",
		$urltimeout,                \$response,        \$errormsg
	    )
	  )
	{
	    $logger->error("ERROR:  Failed to get server status file $server_status_file_url -- $errormsg");
	    spool_a_health_message( "Failed to get server status file $server_status_file_url -- $errormsg", 2 );
	    return 0;
	}
	elsif ( not $response->is_success ) {
	    $logger->notice( "NOTICE:  Failed to get server status file $server_status_file_url -- " . $response->status_line );
	    if ( is_redirect( $response->code ) ) {
		my $redirect_location = $response->header('Location');
		## Unless the protocol is downgraded from HTTPS to HTTP, we'll allow the redirect,
		## subject to the configured (or defaulted) Max_Server_Redirects value.
		if ( not defined $redirect_location ) {
		    $logger->notice(" (got redirected, but with no new location supplied)");
		}
		elsif ($redirect_location =~ m{^https?://}i
		    && ( $server_status_file_url =~ m{^http://}i || $redirect_location =~ m{^https://}i )
		    && $remaining_fetches >= 2 )
		{
		    $logger->notice(" (redirecting to $redirect_location)");
		    $server_status_file_url = $redirect_location;
		    $fetch                  = 1;
		    next;
		}
		else {
		    $logger->notice(" (ignoring redirection to $redirect_location)");
		}
	    }
	    return 0;
	}
    }

    $logger->trace("TRACE:  Successfully polled plugins update status \"$server_status_file_url\"");

    # Check the GW monitor server's response.
    # If response is empty then plugins are not updated.
    if ( -z $polled_plugin_status_filepath ) {
	$logger->trace("TRACE:  Response is empty, so plugins are not updated on the server.");
	return 1;
    }

    # read polled XML file
    $polled_xml_data = eval { $xml_parser->XMLin($polled_plugin_status_filepath); };

    # check for the error, log errors if any or log successful xml parsing
    if ($@) {
	## remove module line number
	if ( $^O eq 'MSWin32' ) {
	    $@ =~ s{at [a-zA-Z]:/.*?$}{}s;
	}
	else {
	    $@ =~ s{at /.*?$}{}s;
	}
	## $logging->log_message('');
	$logger->error("ERROR:  Parsing '$polled_plugin_status_filepath' failed:  $@");
	return 0;
    }
    else {
	$logger->debug("DEBUG:  Successfully parsed '$update_status_filename' file.");
    }

    unless ( -e $local_plugin_status_filepath ) {
	## $logging->log_message('') if $debug == 2;
	$logger->trace("TRACE:  Copying all plugin filenames, since the local plugins_update.xml file is not present.");
	%$plugin_files = %$polled_xml_data;
	return 1;
    }

    %$plugin_files = %$polled_xml_data;
    return 1;
}

##############################################################################
#
#   sub download_plugins()
#
#   Downloads plugins and corresponding plugins and logs error if unable to
#   download plugins or dependency.  Also, it verifies checksum of downloaded
#   plugins'.  If checksum is correct then moves plugins to plugin directory
#   and dependencies to dependency directory.  Then, it updates local update
#   status XML file with downloaded plugins information.
#
#   Arguments:
#   $polled_xml_data - Reference to hash which contains plugins information
#                      which needs to be downloaded.
#   $plugin_request_timestamp - Reference to lastUpdateTimestamp of plugin request
#   $plugin_request_date - Reference to lastUpdateDate of plugin request
#   $plugins_dir    - path of plugin directory
#   $dependency_dir - path of plugin's dependency directory
#   $tempdirpath    - path of temp directory
#   $update_status_filename - file name of plugins update status file
#
##############################################################################
sub download_plugins {
    my ( $polled_xml_data, $plugin_request_timestamp, $plugin_request_date, $plugins_dir, $dependency_dir, $tempdirpath,
	$update_status_filename )
      = @_;
    my %successful_downloads = ();
    my $response;
    my $errormsg;

    my $md5 = Digest::MD5->new;

    # Check if plugin's information is present or not
    $logger->notice("NOTICE:  Poller will try to download plugins.");
    if ( !defined( $polled_xml_data->{'pluginUpdate'}[0]->{'plugin'} ) ) {
	$logger->notice("NOTICE:  Nothing to download.  Plugins are not updated on the GW Monitor.");
	return 0;
    }

    my $count = 0;

    # Store plugins infomation in the successful_downloads which will be written to local plugins_update.xml file.
    %successful_downloads = (
	'pluginUpdate' => [
	    {
		'platform'            => $polled_xml_data->{'pluginUpdate'}[0]->{platform},
		'lastUpdateTimestamp' => $plugin_request_timestamp,
		'lastUpdateDate'      => $plugin_request_date,
		'plugin'              => []
	    }
	]
    );

    my $ssl_opts = GDMA::Utils::gdma_client_ssl_opts($logger);
    my $plugin_useragent = LWP::UserAgent->new(
	agent    => 'GDMA Client/' . get_version(),
	ssl_opts => $ssl_opts,
    );

    # Default is both GET and HEAD redirect, if we don't make this call.  We are now (as of GDMA 2.3.2)
    # disabling automatic redirects, as we must intervene and prevent a possible HTTPS-to-HTTP downgrade.
    # So all redirects are now handled manually here.
    $plugin_useragent->requests_redirectable( [] );

  PLUGIN: foreach my $new_plugin ( @{ $polled_xml_data->{pluginUpdate}[0]->{plugin} } ) {
	my $plugin_url = $new_plugin->{url};
	if ( defined $plugin_url ) {
	    my $pluginpath = "$tempdirpath" . $file_separator . $new_plugin->{name};

	    my $remaining_plugin_fetches = $g_config{Max_Server_Redirects};
	    $remaining_plugin_fetches = $Default_Max_Server_Redirects if not defined $remaining_plugin_fetches;

	  PLUGIN_FETCH:
	    for (
		my $fetch_plugin = 1, ++$remaining_plugin_fetches ;
		$fetch_plugin && $remaining_plugin_fetches > 0 ;
		--$remaining_plugin_fetches
	      )
	    {
		$fetch_plugin = 0;    # Only fetch upon redirect if explicitly commanded below.

		if ( not GDMA::Utils::is_valid_ca_path( \$errormsg, $plugin_url ) ) {
		    $logger->error("ERROR:  $errormsg");
		    next PLUGIN;
		}

		if (
		    not do_timed_mirror(
			'GDMA plugin file fetch',
			$plugin_useragent, $plugin_url, $pluginpath, $PluginPull_Timeout, \$response, \$errormsg
		    )
		  )
		{
		    $logger->error("ERROR:  Failed to get plugin $plugin_url -- $errormsg");
		}
		## We treat errors ($errormsg) in the initial attempt to mirror as being likely due to timeouts (without any
		## explicit confirmation of that assumption), and thus possibly justifying a retry using the alternate hostname.
		if ( $errormsg or not $response->is_success ) {
		    $logger->notice( "NOTICE:  Failed to get plugin $plugin_url -- " . $response->status_line ) if !$errormsg;
		    if ( !$errormsg and $response->status_line =~ /304 not modified/i ) {
			$logger->notice(" (304's are generally safe to ignore).");
		    }
		    elsif ( !$errormsg and is_redirect( $response->code ) ) {
			my $redirect_location = $response->header('Location');
			## Unless the protocol is downgraded from HTTPS to HTTP, we'll allow the redirect,
			## subject to the configured (or defaulted) Max_Server_Redirects value.
			if ( not defined $redirect_location ) {
			    $logger->notice(" (got redirected, but with no new location supplied)");
			}
			elsif ($redirect_location =~ m{^https?://}i
			    && ( $plugin_url =~ m{^http://}i || $redirect_location =~ m{^https://}i )
			    && $remaining_plugin_fetches >= 2 )
			{
			    $logger->notice(" (redirecting to $redirect_location)");
			    $plugin_url   = $redirect_location;
			    $fetch_plugin = 1;
			    next PLUGIN_FETCH;
			}
			else {
			    $logger->notice(" (ignoring redirection to $redirect_location)");
			}
			next PLUGIN;
		    }
		    elsif ( $errormsg or $response->status_line =~ /500 Can't connect/i ) {
			my $saved_plugin_url = $plugin_url;

			# Initial plugin download failed; but GW server name might be FQDN.
			# Poller will try to download plugin using short host name of GW Monitor server.
			if ( $g_config{Poller_Plugins_Upgrade_URL} =~ m{^(https|http)://([a-zA-Z0-9_-]+)}i ) {

			    # Protocol name contains HTTP or HTTPS whichever is present in the Poller_Plugins_Upgrade_URL
			    my $protocol_name = $1;

			    ## Short Host Name for the GW server will be used if FQDN fails.
			    my $short_server_name = $2;

			    # create new plugin download URL
			    $plugin_url =~ s{^(https|http)://.*?/}{$protocol_name://$short_server_name/}i;
			}

			# If the newly constructed plugin URL and the previous plugin URL are the same,
			# then the poller should not try again to download the plugin.
			if ( $plugin_url eq $saved_plugin_url ) {
			    $logging->log_message('');
			    next PLUGIN;
			}

			my $remaining_short_plugin_fetches = $g_config{Max_Server_Redirects};
			$remaining_short_plugin_fetches = $Default_Max_Server_Redirects if not defined $remaining_short_plugin_fetches;

		      PLUGIN_SHORT_HOSTNAME_FETCH:
			for (
			    my $fetch_short_plugin = 1, ++$remaining_short_plugin_fetches ;
			    $fetch_short_plugin && $remaining_short_plugin_fetches > 0 ;
			    --$remaining_short_plugin_fetches
			  )
			{
			    $fetch_short_plugin = 0;    # Only fetch upon redirect if explicitly commanded below.

			    if ( not GDMA::Utils::is_valid_ca_path( \$errormsg, $plugin_url ) ) {
				$logger->error("ERROR:  $errormsg");
				next PLUGIN;
			    }

			    $logging->log_message('');
			    $logger->info("INFO:  Trying to get plugin $plugin_url");
			    $response = "";
			    if (
				not do_timed_mirror(
				    'GDMA plugin file fetch', $plugin_useragent, $plugin_url, $pluginpath,
				    $PluginPull_Timeout,      \$response,        \$errormsg
				)
			      )
			    {
				$logger->error("ERROR:  Failed to get plugin $plugin_url -- $errormsg");
				next PLUGIN;
			    }
			    elsif ( not $response->is_success ) {
				$logger->notice( "NOTICE:  Failed to get plugin $plugin_url -- " . $response->status_line );
				if ( $response->status_line =~ /304 not modified/i ) {
				    $logger->notice(" (304's are generally safe to ignore).");
				}
				elsif ( is_redirect( $response->code ) ) {
				    my $redirect_location = $response->header('Location');
				    ## Unless the protocol is downgraded from HTTPS to HTTP, we'll allow the redirect,
				    ## subject to the configured (or defaulted) Max_Server_Redirects value.
				    if ( not defined $redirect_location ) {
					$logger->notice(" (got redirected, but with no new location supplied)");
				    }
				    elsif ($redirect_location =~ m{^https?://}i
					&& ( $plugin_url =~ m{^http://}i || $redirect_location =~ m{^https://}i )
					&& $remaining_short_plugin_fetches >= 2 )
				    {
					$logger->notice(" (redirecting to $redirect_location)");
					$plugin_url         = $redirect_location;
					$fetch_short_plugin = 1;
					next PLUGIN_SHORT_HOSTNAME_FETCH;
				    }
				    else {
					$logger->notice(" (ignoring redirection to $redirect_location)");
				    }
				    next PLUGIN;
				}
				else {
				    $logging->log_message('');
				    next PLUGIN;
				}
			    }
			}
		    }
		    else {
			$logging->log_message('');
			next PLUGIN;
		    }
		}

		$logger->debug("DEBUG:  Successfully polled plugin:  $pluginpath");

		# Check the GW monitor server's response.
		# Verify plugin file size and checksum.
		my $file = "$tempdirpath" . $file_separator . $new_plugin->{name};
		if ( not open( FILE, '<', $file ) ) {
		    $logger->error("ERROR:  Cannot open $file ($!).");
		    next PLUGIN;
		}
		binmode(FILE);
		$md5->addfile(*FILE);
		close(FILE);

		# if checksum verification fails then skip downloading of dependencies
		my $hexdigest = $md5->hexdigest();
		if ( $hexdigest ne $new_plugin->{checksum} ) {
		    $logger->error( "ERROR:  Checksum verification failed for plugin:  " . $new_plugin->{name} );
		    $logger->error( "        Supposed checksum " . $new_plugin->{checksum} . " was computed instead as $hexdigest." );
		    next PLUGIN;
		}

		if ( -z "$tempdirpath" . $file_separator . $new_plugin->{name} ) {
		    $logger->trace("TRACE:  $tempdirpath" . $file_separator . $new_plugin->{name} . " is empty.");
		    next PLUGIN;
		}

		# Check that all dependencies are downloaded.
		# $dependency_state == 0  plugin doesn't have dependency
		# $dependency_state == 1  error during downloading of any one dependency
		# $dependency_state == 2  all dependencies are downloaded
		my $dependency_state = 0;
		if ( defined( $new_plugin->{dependency} ) and ( ref( $new_plugin->{dependency} ) eq "ARRAY" ) ) {
		  DEPENDENCY: foreach my $dependency ( @{ $new_plugin->{dependency} } ) {
			my $dependency_url = $dependency->{url};
			if ( defined $dependency_url ) {
			    my $temp_dependency_path = "$tempdirpath" . $file_separator . $dependency->{name};

			    my $remaining_dependency_fetches = $g_config{Max_Server_Redirects};
			    $remaining_dependency_fetches = $Default_Max_Server_Redirects if not defined $remaining_dependency_fetches;

			  DEPENDENCY_FETCH:
			    for (
				my $fetch_dependency = 1, ++$remaining_dependency_fetches ;
				$fetch_dependency && $remaining_dependency_fetches > 0 ;
				--$remaining_dependency_fetches
			      )
			    {
				$fetch_dependency = 0;    # Only fetch upon redirect if explicitly commanded below.

				if ( not GDMA::Utils::is_valid_ca_path( \$errormsg, $dependency_url ) ) {
				    $logger->error("ERROR:  $errormsg");
				    $dependency_state = 1;
				    last DEPENDENCY;
				}

				if (
				    not do_timed_mirror(
					'GDMA dependency file fetch', $plugin_useragent,
					$dependency_url,              $temp_dependency_path,
					$PluginPull_Timeout,          \$response,
					\$errormsg
				    )
				  )
				{
				    $logger->error("ERROR:  Failed to get dependency $dependency_url -- $errormsg");
				}
				## We treat errors ($errormsg) in the initial attempt to mirror as being likely due to timeouts (without any
				## explicit confirmation of that assumption), and thus possibly justifying a retry using the alternate hostname.
				if ( $errormsg or not $response->is_success ) {
				    $logger->notice( "NOTICE:  Failed to get dependency $dependency_url -- " . $response->status_line )
				      if !$errormsg;
				    if ( !$errormsg and $response->status_line =~ /304 not modified/i ) {
					$logger->notice(" (304's are generally safe to ignore).");
				    }
				    elsif ( !$errormsg and is_redirect( $response->code ) ) {
					my $redirect_location = $response->header('Location');
					## Unless the protocol is downgraded from HTTPS to HTTP, we'll allow the redirect,
					## subject to the configured (or defaulted) Max_Server_Redirects value.
					if ( not defined $redirect_location ) {
					    $logger->notice(" (got redirected, but with no new location supplied)");
					}
					elsif ($redirect_location =~ m{^https?://}i
					    && ( $dependency_url =~ m{^http://}i || $redirect_location =~ m{^https://}i )
					    && $remaining_dependency_fetches >= 2 )
					{
					    $logger->notice(" (redirecting to $redirect_location)");
					    $dependency_url   = $redirect_location;
					    $fetch_dependency = 1;
					    next DEPENDENCY_FETCH;
					}
					else {
					    $logger->notice(" (ignoring redirection to $redirect_location)");
					}
					$dependency_state = 1;
					last DEPENDENCY;
				    }
				    elsif ( $errormsg or $response->status_line =~ /500 Can't connect/i ) {
					my $saved_dependency_url = $dependency_url;

					# Initial dependency download failed; but GW server name might be FQDN.
					# Poller will try to download dependency using short host name of GW Monitor server.
					if ( $g_config{Poller_Plugins_Upgrade_URL} =~ m{^(https|http)://([a-zA-Z0-9_-]+)}i ) {

					    # Protocol name contains HTTP or HTTPS whichever is present in the Poller_Plugins_Upgrade_URL
					    my $protocol_name = $1;

					    ## Short Host Name for the GW server will be used if FQDN fails.
					    my $short_server_name = $2;

					    # create new dependency download URL
					    $dependency_url =~ s{^(https|http)://.*?/}{$protocol_name://$short_server_name/}i;
					}

					# If the newly constructed dependency URL and the previous dependency URL are the same,
					# then the poller should not try again to download the dependency.
					if ( $dependency_url eq $saved_dependency_url ) {
					    $logging->log_message('');
					    $dependency_state = 1;
					    last DEPENDENCY;
					}

					my $remaining_short_dependency_fetches = $g_config{Max_Server_Redirects};
					$remaining_short_dependency_fetches = $Default_Max_Server_Redirects
					  if not defined $remaining_short_dependency_fetches;

				      DEPENDENCY_SHORT_HOSTNAME_FETCH:
					for (
					    my $fetch_short_dependency = 1, ++$remaining_short_dependency_fetches ;
					    $fetch_short_dependency && $remaining_short_dependency_fetches > 0 ;
					    --$remaining_short_dependency_fetches
					  )
					{
					    $fetch_short_dependency = 0;    # Only fetch upon redirect if explicitly commanded below.

					    if ( not GDMA::Utils::is_valid_ca_path( \$errormsg, $dependency_url ) ) {
						$logger->error("ERROR:  $errormsg");
						$dependency_state = 1;
						last DEPENDENCY;
					    }

					    $logging->log_message('');
					    $logger->info("INFO:  Trying to get dependency $dependency_url");
					    $response = "";
					    if (
						not do_timed_mirror(
						    'GDMA dependency file fetch', $plugin_useragent,
						    $dependency_url,              $temp_dependency_path,
						    $PluginPull_Timeout,          \$response,
						    \$errormsg
						)
					      )
					    {
						$logger->error("ERROR:  Failed to get dependency $dependency_url -- $errormsg");
						$dependency_state = 1;
						last DEPENDENCY;
					    }
					    elsif ( not $response->is_success ) {
						$logger->notice(
						    "NOTICE:  Failed to get dependency $dependency_url -- " . $response->status_line );
						if ( $response->status_line =~ /304 not modified/i ) {
						    $logger->notice(" (304's are generally safe to ignore).");
						}
						elsif ( is_redirect( $response->code ) ) {
						    my $redirect_location = $response->header('Location');
						    ## Unless the protocol is downgraded from HTTPS to HTTP, we'll allow the redirect,
						    ## subject to the configured (or defaulted) Max_Server_Redirects value.
						    if ( not defined $redirect_location ) {
							$logger->notice(" (got redirected, but with no new location supplied)");
						    }
						    elsif ($redirect_location =~ m{^https?://}i
							&& ( $dependency_url =~ m{^http://}i || $redirect_location =~ m{^https://}i )
							&& $remaining_short_dependency_fetches >= 2 )
						    {
							$logger->notice(" (redirecting to $redirect_location)");
							$dependency_url         = $redirect_location;
							$fetch_short_dependency = 1;
							next DEPENDENCY_SHORT_HOSTNAME_FETCH;
						    }
						    else {
							$logger->notice(" (ignoring redirection to $redirect_location)");
						    }
						    $dependency_state = 1;
						    last DEPENDENCY;
						}
						else {
						    $logging->log_message('');
						    $dependency_state = 1;
						    last DEPENDENCY;
						}
					    }
					}
				    }
				    else {
					$logging->log_message('');
					$dependency_state = 1;
					last DEPENDENCY;
				    }
				}
				$dependency_state = 2;
				$logger->debug( "DEBUG:  Successfully polled dependency:  " . $temp_dependency_path );
			    }
			}
			else {
			    $logger->error( "ERROR:  URL of plugin dependency is missing; dependency name: " . $dependency->{name} );
			    $dependency_state = 1;
			    last DEPENDENCY;
			}
		    }
		    next PLUGIN if ( $dependency_state == 1 );
		}

		# copy plugin and dependencies to plugin directory
		if (
		    not copy( "$tempdirpath" . $file_separator . $new_plugin->{name}, "$plugins_dir" . $file_separator . $new_plugin->{name} ) )
		{
		    my $os_error = "$!";
		    $os_error .= " ($^E)" if "$^E" ne "$!";
		    $logger->error("ERROR:  Plugin copying to \"$plugins_dir$file_separator$new_plugin->{name}\" failed:  $os_error");
		    next PLUGIN;
		}

		if ( $^O ne 'MSWin32' ) {
		    ## Change file permission of the plugin, to make it executable.
		    chmod( 0755, "$plugins_dir" . $file_separator . $new_plugin->{name} ) or do {
			$logger->trace( "TRACE:  Could not change file permission: $plugins_dir$file_separator" . $new_plugin->{name} . " ($!)" );
		    };

		    # FIX MAJOR:  this code is messed up on several counts
		    # * For safety's sake, GDMA is supposed to run as a non-root user, which means it won't be
		    #   able to either create files as some user other than itself in the first place, or to change
		    #   the ownership afterward.  That makes the chown pointless on most systems unless we test
		    #   for running as root and only do the chown then.  (Some old flavors of UNIX [but not Linux,
		    #   Solaris, or AIX] do allow ordinary users to give away files; none allow them to grab someone
		    #   else's files.)  But we should instead be prohibiting this script from being run as root.
		    # * There might conceivably be some purpose in a chown here to set the group to a canonical
		    #   distinguished member of the set of groups to which the current user belongs, rather than the
		    #   group we currently happen to be executing as.  But we have no documented setup for alternate
		    #   group assignment to the "gdma" or equivalent user in the first place, making it doubtful
		    #   that this serves any real purpose.  Instead, it's likely that this code is here simply
		    #   because of developer confusion as to what makes sense.
		    # * If we could in fact change file ownership here, we might want to change the ownership to
		    #   some user other than a hardcoded "gdma", if GDMA was installed under some other user name.
		    if (0) {
			my ( $uid, $gid );
			if ( ( $uid, $gid ) = ( getpwnam("gdma") )[ 2, 3 ] ) {
			    chown( $uid, $gid, "$plugins_dir" . $file_separator . $new_plugin->{name} ) or do {
				$logger->trace( "TRACE:  Could not change file ownership: $plugins_dir$file_separator" . $new_plugin->{name} . " ($!)" );
			    };
			}
			else {
			    $logger->trace("TRACE:  User 'gdma' is not present in the password file.");
			}
		    }
		}

		if ( $dependency_state == 2 ) {
		  DEPENDENCY_COPY: foreach my $dependency ( @{ $new_plugin->{dependency} } ) {
			if (
			    not(
				copy(
				    "$tempdirpath" . $file_separator . $dependency->{name},
				    "$dependency_dir" . $file_separator . $dependency->{name}
				)
			    )
			  )
			{
			    $logger->error( "ERROR:  Plugin dependency copying to plugin directory failed; dependency name: "
				  . $dependency->{name}
				  . ":  $!" );
			    $dependency_state = 1;
			    last DEPENDENCY_COPY;
			}
		    }
		}
		next PLUGIN if ( $dependency_state == 1 );

		# update successful_downloads with plugins and dependencies information
		$successful_downloads{pluginUpdate}[0]{plugin}[ $count++ ] = {%$new_plugin};

		# Find the highest lastUpdateTimestamp value among all the successfully downloaded
		# plugins, and update successful_download's lastUpdateTimestamp by adding 1 second to
		# that lastUpdateTimestamp.  This globally stored slightly-greater-than-maximum value
		# will provide the basis for looking for further downloads in the future, presuming that
		# the server is not keeping file-upload time at any higher resolution than one second.
		if ( $successful_downloads{pluginUpdate}[0]{lastUpdateTimestamp} <= $new_plugin->{lastUpdateTimestamp} ) {
		    ## Bump up the lastUpdateTimestamp timestamp by one second.  Since we are dealing with
		    ## a simple numeric count of seconds since the epoch, we don't need to worry about any
		    ## transitions between larger time units or any counting discontinuities within this
		    ## representation, such as:
		    #
		    #   * transition to the next minute
		    #   * transition to the next hour
		    #   * transition to the next day, including particularly:
		    #       * transition from the last day of the month to the first day of the next month
		    #       * transition from February 28 to February 29, in a leap year
		    #       * transition from December 31 of one year to January 1 of the following year
		    #   * transition from the last second of Standard Time to the first second of Daylight
		    #     Savings Time, according to the vagaries of the local timezone's definition of
		    #     Daylight Savings Time
		    #   * transition from the last second of Daylight Savings Time to the first second of
		    #     Daylight Savings Time, according to the vagaries of the local timezone's
		    #     definition of Daylight Savings Time
		    #
		    # However, there will be a certain amount of ambiguity in the string representation
		    # of this timestamp, given that we neither record the local GDMA client timezone in
		    # that string nor indicate whether Daylight Savings Time is in play at that moment.
		    # Thus the string representation is not authoritative; it serves only as a general
		    # indicator for human consumption.
		    #
		    # The last transition is perhaps the most troublesome, inasmuch as the overlap of a
		    # long sequence of timestamp values at the end of DST and the beginning of ST means
		    # there is ambiguity as to what point in time a timestamp in this interval really
		    # represents, and how each of the two possible interpretations would affect the
		    # validity of the derived calculations.  The situation with Daylight Savings Time is
		    # even more complicated by the silly penchant of politicians to mess with the timing
		    # of DST transitions; it is quite possible that individual GDMA client machines
		    # might not have up-to-date timezone files installed that would contain accurate DST
		    # definitions that will agree with the DST definitions in operation on the central
		    # GroundWork Monitor server (which is assumed without justification to have its
		    # timezone files kept up-to-date).
		    #
		    # In the conversion to a string, we ignore leap seconds; see these discussions:
		    #
		    #     http://en.wikipedia.org/wiki/Leap_second
		    #     http://download.oracle.com/javase/1.5.0/docs/api/java/util/Date.html
		    #     http://tycho.usno.navy.mil/leapsec.html
		    #     http://tycho.usno.navy.mil/systime.html

		    if ( $new_plugin->{lastUpdateTimestamp} =~ /^\d+/ ) {
			my $modified_timestamp = $new_plugin->{lastUpdateTimestamp} + 1;
			$successful_downloads{pluginUpdate}[0]{lastUpdateTimestamp} = "$modified_timestamp";

			## Convert the timestamp to a string of the indicated format, taking into account the
			## local timezone and possible Daylight Savings Time adjustments.  This conversion
			## unjustifiably assumes the timezone conversion files are up-to-date on this machine,
			## which is why this string is only for human consumption, and not authoritative.

			## All the platforms we support (Linux, Solaris, AIX, and Windows) use the same epoch
			## for their time base (1970-01-01 00:00:00 UTC), so we don't bother with any temporal
			## offsets for portability between platforms.
			my $modified_date = strftime( '%Y-%m-%d %H:%M:%S', localtime($modified_timestamp) );
			$successful_downloads{pluginUpdate}[0]{lastUpdateDate} = "$modified_date";
		    }
		    else {
			$logger->trace("TRACE:  \"$new_plugin->{name}\" plugin lastUpdateTimestamp has an invalid format.");
			## We remove the lastUpdateDate in this case, so we are not misled by its value.
			delete $successful_downloads{pluginUpdate}[0]{lastUpdateDate};
		    }
		}
	    }
	}
	else {
	    $logger->error("ERROR:  URL to download plugin is missing; plugin name:  " . $new_plugin->{name});
	}
    }

    # Create an XML object.  Using "KeepRoot => 1" here forces XMLout() to not wrap the generated XML
    # in an extra level of elements, so the root element we already have in the hash (<pluginUpdate>)
    # will be used.  Setting "RootName => 'pluginUpdate'" when we call XMLout() just sets the added
    # root element name when "KeepRoot => 0" is in effect, and is ignored when "KeepRoot => 1" is in
    # effect unless the hash has no root element to begin with, in which case the specified RootName
    # is used.  So we do specify the RootName below as a fallback measure that should never really be
    # exercised.
    my $xml = new XML::Simple( KeyAttr => [], ForceArray => 1, KeepRoot => 1 );

    # Write XML file.
    my $xml_filepath = "$plugins_dir" . $file_separator . $update_status_filename;

    # update local plugins_update.xml file, log error if any occurs
    eval {
	$xml->XMLout(
	    \%successful_downloads,
	    OutputFile => $xml_filepath,
	    RootName   => 'pluginUpdate',
	    NoSort     => 0,
	    XMLDecl    => '<?xml version="1.0" encoding="ISO-8859-1"?>'
	);
    };
    if ($@) {
	chomp $@;
	$logger->error("ERROR:  Cannot construct updated $xml_filepath file ($@).");
    }

    return 1;
}

#################################################################################
#
#   run_normal_mode()
#
#   This is the normal mode of operation (as against autoconfig mode)
#   for poller.  Performs the system checks specified in the
#   config file and spools them.  It then spools the heartbeat message,
#   containing statistical data.
#
#   Arguments:
#   $start_time - The start time for iteration.  Used in computing the
#                 executed time for the checks.
#   $normal_spool_filename - Spool file name.
#   $hires_time - Reference to available high resolution time subroutine.
#   $hires_time_format - Available high resolution time format.
#   $result_buf - A reference to a buffer containing results.
#   $conf_files - A reference to the list of conf files to be processed.
#
################################################################################
sub run_normal_mode {
    my ( $start_time, $normal_spool_filename, $hires_time, $hires_time_format, $result_buf, $conf_files ) = @_;
    my $elapsed_time;
    my $elapsed_pct;
    my $sleep_time = 0;
    my $exec_time;
    my $nchecks;
    my $result_str;
    my $errstr;
    my $blocking    = 0;
    my $return_code = 0;
    my $return_text = "OK";

    # "0" implies that the result is to be sent to all the targets.
    my $default_target = 0;

    # Use default hard coded values for Warning And Critical Threshold if they are not configured.
    my $warning_threshold  = 60;
    my $critical_threshold = 80;
    $warning_threshold  = $g_config{Warning_Threshold}  if ( defined $g_config{Warning_Threshold} );
    $critical_threshold = $g_config{Critical_Threshold} if ( defined $g_config{Critical_Threshold} );

    $logger->debug("DEBUG:  Executing poller normal mode.");

    # Execute system checks.
    # do_checks() takes care of executing the checks as well as spooling them.
    my $hostname = GDMA::Utils::my_hostname( $g_config{Use_Long_Hostname}, $g_config{Forced_Hostname}, $g_config{Use_Lowercase_Hostname} );
    if ( not do_checks( $hostname, $conf_files, \$nchecks, \$sleep_time ) ) {
	$logger->error("ERROR:  Problem occurred while executing checks.");
    }

    # Note the time it took to execute the checks.
    $elapsed_time = &$hires_time() - $start_time;

    # Calculate actual execution time by excluding sleep time.
    $exec_time   = $elapsed_time - $sleep_time;
    $elapsed_pct = $elapsed_time / $g_config{Poller_Proc_Interval} * 100;
    $elapsed_pct = sprintf "%.2f", $elapsed_pct;

    # We could choose to parameterize these but for now having them hardcoded will suffice KDS
    if ( $elapsed_pct > $warning_threshold )  { $return_code = 1; $return_text = "Warning" }
    if ( $elapsed_pct > $critical_threshold ) { $return_code = 2; $return_text = "Critical" }

    # Compose and spool the heartbeat message, only if "Poller_Status" is "on|On"
    if ( $g_config{Poller_Status} =~ /on/i ) {
	## Insert the heartbeat message into the result buffer.
	## A heartbeat message, sent per iteration, includes the
	## number of checks processed in the iteration and the time
	## it took to execute them.
	my @msg_payload = ();
	push @msg_payload, "$return_text ";
	push @msg_payload, "Poller processed $nchecks checks in ";
	push @msg_payload, sprintf( $hires_time_format, $elapsed_time ) . " secs. ";
	push @msg_payload, "Using $elapsed_pct% of ";
	push @msg_payload, sprintf( $g_config{Poller_Proc_Interval} ) . " sec Polling Interval.";
	push @msg_payload, "|NumChecks=$nchecks;;;; TimeSecs=";
	push @msg_payload, sprintf( $hires_time_format, $elapsed_time ) . ";;;0;";
	push @msg_payload, " PctTime=$elapsed_pct;$warning_threshold;$critical_threshold;0;";
	push @msg_payload, " ExecTime=$exec_time;;;0;";
	my $msg_payload = join( '', @msg_payload );
	$logger->trace("TRACE:  run_normal_mode:  $msg_payload");

	# Use the default target.
	# The result will be for the configured "Poller_Service".
	# The return code for the heartbeat message defaults to "0" (i.e., "OK"), but will be overridden if the
	# length of the polling activity within the polling cycle exceeds the warning or critical thresholds.
	insert_into_result_buf( $default_target, $hostname, $g_config{Poller_Service}, $return_code, $msg_payload, $result_buf );

	# Spool the heartbeat result immediately, as we don't want to wait till the next iteration.
	# Make a non-blocking call, as we don't want to block for too long.
	if ( !GDMA::Utils::spool_results( $normal_spool_filename, $result_buf, $blocking, \$nchecks, \$errstr ) ) {
	    ## Spooling failed, but the result is still there in the buffer.
	    ## Hopefully, it will be spooled at a later time.
	    $logger->error("ERROR:  run_normal_mode:  $errstr");
	}
    }
}

################################################################################
#
#   spool_autoconfig_message()
#
#   This function spools the autoconfig message.  The autoconfig result is for
#   the configured "GDMA_Auto_Service" on the configured "GDMA_Auto_Host" and
#   is sent to the configured "Target_Server".
#
#   Arguments:
#   $normal_spool_filename - Spool file name.
#   $result_buf - A reference to a buffer containing results.
#
################################################################################
sub spool_autoconfig_message {
    my $normal_spool_filename = shift;
    my $result_buf            = shift;
    my $head_path             = shift;
    my $host_ip               = undef;
    my $packed_ip             = undef;
    my $msg_payload;
    my $errstr;
    my $blocking = 0;
    my $num_results;
    my $default_target = 0;

    # The return code for autoconfig message is "UNKNOWN".
    my $return_code_unknown = 3;
    my $hostname = GDMA::Utils::my_hostname( $g_config{Use_Long_Hostname}, $g_config{Forced_Hostname}, $g_config{Use_Lowercase_Hostname} );
    my $osname   = $^O;

    $logger->debug("DEBUG:  Spooling autoconfig message.");

    # Record the IP address for the host.
    $packed_ip = gethostbyname($hostname);

    # Check if we could resolve the hostname to an IP address.
    if ( defined($packed_ip) ) {
	$host_ip = inet_ntoa($packed_ip);
    }
    else {
	$logger->error("ERROR:  Failed to resolve hostname \"$hostname\".");
    }

    # Build the autoconfig message body.
    $msg_payload = "No configuration file in poller:  $hostname";

    # Append the IP address if we could resolve it.
    if ( defined($host_ip) ) {
	$msg_payload .= " [$host_ip]";
    }
    $msg_payload .= " running Perl compiled under $osname $Config{osvers}";

    # Add a bit more data if it's Windows.
    if ( $osname eq 'MSWin32' ) {
	my $osres = `cscript -nologo "$head_path\\libexec\\v2\\get_system_uptime.vbs" -h $hostname -w 2 -c 1`;
	$msg_payload .= " $osres";
    }
    $logger->trace("TRACE:  spool_autoconfig_message:  $msg_payload");

    # Use the default retries.
    insert_into_result_buf(
	$default_target,
	$g_config{GDMA_Auto_Host},
	$g_config{GDMA_Auto_Service},
	$return_code_unknown, $msg_payload, $result_buf
    );

    # Flush it out to the spool file immediately.
    # Make a non-blocking call, we don't want to block for too long.
    if ( !GDMA::Utils::spool_results( $normal_spool_filename, $result_buf, $blocking, \$num_results, \$errstr ) ) {
	## Spooling failed, but the result is still there in the buffer.
	## Hopefully, it will be spooled at a later time.
	$logger->warn("WARNING:  spool_autoconfig_message:  $errstr");
    }
}

################################################################################
#
#   run_auto_setup()
#
# Return values:
# Status 0 means we encountered failure.
# Status 1 means we ran a successful pass of discovery, and the configuration is new or has changed.
# Status 2 means we ran a pass of discovery, but found no changes in the results since the previous pass.
# Status 3 means there was no active trigger, so no discovery pass was run.
#
################################################################################
sub run_auto_setup {
    my $OverridefilePath = shift;

    return 0 if !defined( $g_config{Enable_Auto_Setup} ) || $g_config{Enable_Auto_Setup} !~ /^on$/i;

    ## FIX MINOR:  move this part elsewhere, when we first read or reload the configuration data,
    ## as part of validating the configuration, making sure this stuff is taken only from gdma_auto.conf
    if ( defined( $g_config{Enable_Auto_Setup} ) && $g_config{Enable_Auto_Setup} =~ /^on$/i ) {
	if ( not $g_config{Auto_Setup_Instructions_Dir} ) {
	    $logger->error("ERROR:  Enable_Auto_Setup is enabled but Auto_Setup_Instructions_Dir is not supplied.");
	    return 0;
	}
	if ( not $g_config{Auto_Setup_Trigger_Dir} ) {
	    $logger->error("ERROR:  Enable_Auto_Setup is enabled but Auto_Setup_Trigger_Dir is not supplied.");
	    return 0;
	}
    }

    my $target_addr = ( split( /[,\s]+/, $g_config{Target_Server} ) )[0];
    my $trigger_path;
    my $instructions_path;
    my $last_discovery_path;
    my $failure_message = undef;

    # Set the values for host discovery filepaths based on platform and on the auto-config setup.
    # If the auto-config file doesn't specify whether to use a long or short hostname, we first
    # try the long form (the most specific construction), and then fall back to the short form
    # (the most general construction) if discovery files using the long form are not available.
    my $Use_Long_Hostname = defined( $g_config{Use_Long_Hostname} ) ? $g_config{Use_Long_Hostname} : 'on';
    my $hostname = GDMA::Utils::my_hostname( $Use_Long_Hostname, $g_config{Forced_Hostname}, $g_config{Use_Lowercase_Hostname} );
    my $status = fetch_auto_setup_files( $target_addr, $hostname, \$trigger_path, \$instructions_path, \$last_discovery_path, \$failure_message );
    if ( $status == 0 ) {
	## configuration or pull failure (check $failure_message; it will be defined on error,
	## undef on simple failure to find a file on the remote side)
	if ( defined $failure_message ) {
	    $logger->error("ERROR:  Cannot pull discovery files from the server ($failure_message).");
	    return 0;
	}
	## We used a long-form hostname before.  Fall back to trying the short-form hostname.
	$hostname = GDMA::Utils::my_hostname( $g_config{Use_Long_Hostname}, $g_config{Forced_Hostname}, $g_config{Use_Lowercase_Hostname} );
	$status = fetch_auto_setup_files( $target_addr, $hostname, \$trigger_path, \$instructions_path, \$last_discovery_path, \$failure_message );
	if ( $status == 0 ) {
	    if ( defined $failure_message ) {
		$logger->error("ERROR:  Cannot pull discovery files from the server ($failure_message).");
	    }
	    else {
		$logger->notice("NOTICE:  There are no discovery files for this host on the server; therefore cannot run Auto-Setup.");
	    }
	    return 0;
	}
    }

    if ( $status == 1 ) {
	## We have copies of server-side instructions and trigger files, but file-timestamp analysis shows
	## that no pass of discovery should be run at this time.  We should not treat this case as an error.
	$logger->notice("NOTICE:  File timestamp analysis shows that no pass of discovery is warranted at this time.");
	return 3;
    }
    elsif ( $status == 2 ) {
	my $message;

	## We have a matched pair of trigger and instructions files for this host that should trigger a new pass of discovery.
	$logger->notice("NOTICE:  File timestamp analysis shows that a new pass of discovery should be run.");

	my $headpath = GDMA::Utils::get_headpath();

	## FIX MINOR:  (This is probably already done now.)  parse the trigger file, and if this is a live-action run,
	## modify the trigger (create a "gdma/tmp/local_trigger" file or somesuch) to prevent sending results to the
	## server if they exactly match discovery results from the last successful live-action pass of discovery on the
	## client; clean up this modified trigger file once the pass of discovery is done, regardless of whether or not
	## it succeeded

	## Run the discovery, by calling the external "discover" process with appropriate
	## arguments and processing the returned JSON to set the hostname accordingly.
	## We may as well capture and log its output.
	my $results_path             = "${headpath}${file_separator}tmp${file_separator}${hostname}_results";
	my $dry_results_path         = "${headpath}${file_separator}tmp${file_separator}${hostname}_last_dry_results";
	my $live_results_path        = "${headpath}${file_separator}tmp${file_separator}${hostname}_last_live_results";
	my $discovery_outcome        = 0;
	my $discovery_results        = ();
	my $send_outcome             = 0;
	my $server_status            = 0;
	my $do_discovery             = 0;
	my $send_results             = 0;
	my $is_live_action_discovery = 0;
	my $results_type             = undef;
	my $previous_results_path    = undef;
	my $results_are_identical    = 1;

	do {{
	    my $outcome = 1;

	    my $is_unix    = ( $^O eq 'linux' || $^O eq 'solaris' || $^O eq 'aix' || $^O eq 'hpux' );
	    my $is_windows = ( $^O eq 'MSWin32' );
	    my $discovery_lockfile =
		$is_unix    ? "$headpath/tmp/auto_discovery_lock"
	      : $is_windows ? "$headpath\\tmp\\auto_discovery_lock"
	      :               'unknown_filepath';
	    my $discovery_lock;

	    my %discovery_options = ( logger => $logger );

	    ## FIX MAJOR:  Make this a debug-level option or somesuch, and have some value support logging instead of printing
	    $discovery_options{show_resources} = 0;

	    my $discovery = GDMA::Discovery->new( \%discovery_options );
	    if ( not defined $discovery ) {
		$logger->error("ERROR:  Cannot initialize the GDMA::Discovery package.");
		last;
	    }

	    my $config_debug     = 0;
	    my $return_all_items = 0;
	    my $trigger          = $discovery->read_trigger_file( $trigger_path, $config_debug, $return_all_items );
	    my $last_step        = $trigger->{last_step};
	    my $if_duplicate     = $trigger->{if_duplicate};
	    if ( defined $last_step ) {
		$do_discovery = 1 if $last_step =~ m{^( do_discovery | send_results | do_analysis | test_configuration | do_configuration )$}x;
		$send_results = 1 if $last_step =~ m{^( send_results | do_analysis | test_configuration | do_configuration )$}x;
		$is_live_action_discovery = 1 if $last_step eq 'do_configuration';
	    }
	    $results_type = $is_live_action_discovery ? 'live-action' : 'dry-run';
	    $previous_results_path = $is_live_action_discovery ? $live_results_path : $dry_results_path;

	    if ( not $do_discovery ) {
		$logger->notice( "NOTICE:  No pass of discovery will be run, because the trigger last_step option was "
		      . ( defined($last_step) ? "set to '$last_step'." : 'not defined.' ) );
		return 3;
	    }

	    my $errors = GDMA::LockFile::get_file_lock( \*discovery_lock, $discovery_lockfile, $GDMA::LockFile::EXCLUSIVE, $GDMA::LockFile::NON_BLOCKING );
	    if (@$errors) {
		$logger->error("ERROR:  @$errors");
		last;
	    }

	    ( $discovery_outcome, $discovery_results ) = $discovery->run_discovery( \%g_config, $trigger_path, $instructions_path );
	    if ( not $discovery_outcome ) {
		$logger->error("ERROR:  Discovery failed; see earlier messages.");
		## This situation should not block us from releasing the lock, so we don't directly return from here.  Also,
		## we don't let this failure block us from sending the discovery results to the server if the trigger file
		## says that should be done.  That way, we should get a log message visible in the server-side logfile to
		## indicate what happened out here on the client, without needing to log in to the client.
	    }
	    if (%$discovery_results) {
		## FIX MAJOR:  if we're going to save the discovery results, consider that we should
		## probably suppress all the printed output while the pass of discovery is running
		if ($results_path) {
		    if ( $discovery->save_discovery_results( $discovery_results, $results_path ) ) {
			$logger->notice("NOTICE:  A $results_type pass of discovery has been run, and the results have been saved.");
			##
			## Herein, we want to emit an informative log message telling of our choice of whether to send
			## the results to the server, and justifying that decision.  This helps the user to understand
			## what is going on when discovery is run, despite not having the full content of the trigger
			## logged and interpreted.  Logically, we only need to check whether the results of this pass
			## of discovery are identical to those of the last pass if we might possibly send the results,
			## since that's the only condition where duplicate results have an influence on the decision to
			## send.  So we could bypass this test for duplicating the last pass of same-type discovery if
			## we already know that we should not be sending the results because of the value of $last_step.
			## However, the informative log messages will be a lot more interesting and useful if they also
			## include info about duplicate results even when we have no intention of sending them to the
			## server.  So we do this test first, before diving into individual case-by-case analysis.
			##
			$results_are_identical = $discovery->discovery_results_are_identical( $previous_results_path, $results_path );
			$logger->notice( "NOTicE:  These discovery results are "
			      . ( $results_are_identical ? 'identical to' : 'different from' )
			      . " those of the last $results_type pass of discovery." );

			if ( not $send_results ) {
			    ## Given the value of last_step in the trigger, we will not send in the results regardless of the
			    ## value of the if_duplicate option.  By design, there will therefore be no evidence on the server
			    ## side of what happened when the discovery was run.  For the sake of transparency, we log on the
			    ## client side why we're not sending in the results, so the rationale for ignoring different results
			    ## is not mysterious to a reader of the logfile who cannot see the trigger file we just used.
			    $logger->notice( "NOTICE:  The new discovery results are not being sent to the server,"
				  . " because the trigger last_step option was "
				  . ( defined($last_step) ? "set to '$last_step'." : 'not defined.' ) );
			}
			elsif ($results_are_identical) {
			    ## Buried deep, this is the one place where the trigger's if_duplicate option comes into play.
			    if ( $if_duplicate eq 'ignore' ) {
				$logger->notice( "NOTICE:  The duplicate discovery results are not being sent to the server,"
				      . " because the trigger if_duplicate option was set to 'ignore'." );
				$send_results = 0;
			    }
			    elsif ( $if_duplicate eq 'optimize' ) {
				if ($is_live_action_discovery) {
				    ##
				    ## Send the results only if the GDMA client has no externals in hand that correspond to these exact
				    ## discovery results (i.e., that are timestamped later than the earlier run that produced those results).
				    ## So, we need to compare the last-modified timestamp on the current externals file with the last-modified
				    ## timestamp on XXX.
				    ##
				    ## A key part of this timestamp comparison is that there might be some time skew between the server time
				    ## and the client time.  So we must compare timestamps derived from the same machine.  Since the server
				    ## determines the last-modified timestamp of the externals file and that is mirrored to the client, we
				    ## must ensure that the timestamp of the discovery results also reflects server time.
				    ##
				    ## Bear in mind that when externals are built on the server, if there is no effective change in the
				    ## content of the externals, the last-modified timestamp on the externals file will not be changed.
				    ##
				    my $externals_last_modified_time  = 0;
				    my $discovery_last_different_time = 0;
				    $send_results = $externals_last_modified_time < $discovery_last_different_time ? 1 : 0;
				    ## FIX MINOR:  We haven't worked that out yet, so we make this work like 'force' for the time being;
				    ## this needs to be revisited and corrected.
				    $logger->notice(
					    "NOTICE:  The duplicate discovery results are being sent to the server,"
					  . " because the trigger if_duplicate option was set to 'optimize', this is a live-action discovery,"
					  . " and we have not yet implemented logic to figure out when the results should and should not be ignored in this situation."
				    );
				    $send_results = 1;
				}
				else {
				    $logger->notice( "NOTICE:  The duplicate discovery results are not being sent to the server,"
					  . " because the trigger if_duplicate option was set to 'optimize' and this is only a dry-run discovery."
				    );
				    $send_results = 0;
				}
			    }
			    elsif ( $if_duplicate eq 'force' ) {
				$logger->notice( "NOTICE:  The duplicate discovery results are being sent to the server,"
				      . " because the trigger if_duplicate option was set to 'force'." );
				$send_results = 1;
			    }
			}
			else {
			    $logger->notice( "NOTICE:  The new discovery results are being sent to the server,"
				  . " because they are not completely the same as what has gone before.  The difference might be"
				  . " as trivial as some change in the trigger options, not the results of the sensor processing." );
			}
			if ($send_results) {
			    ## We impose a one-second delay before sending in the discovery results, to guarantee
			    ## that any externals newly built and published based on these discovery results will
			    ## have a last-modified timestamp later than that of the discovery results.
			    select undef, undef, undef, 1.25;
			    my $interactive = 0;
			    ( $send_outcome, $server_status, $message ) = $discovery->send_discovery_results( \%g_config, $results_path, $interactive );
			}
		    }
		}
		else {
		    ## This call will never be exercised in this context.  We leave it here only as an indication
		    ## of something we might do as an approach to somehow get the discovery results logged.
		    $outcome = $discovery->print_discovery_results($discovery_results);
		    $discovery_outcome = 0;
		}
	    }
	    else {
		$logger->notice("NOTICE:  Discovery ran but produced no results.");
		$discovery_outcome = 0;
	    }

	    $outcome = GDMA::LockFile::release_file_lock( \*discovery_lock, $discovery_lockfile );
	    if ( not $outcome ) {
		$logger->error("ERROR:  Cannot release lock on $discovery_lockfile lockfile.");
		$discovery_outcome = 0;
		last;
	    }
	}};

	if ( !$discovery_outcome || ( $send_results && ( !$send_outcome || $server_status ne 'success' ) ) ) {
	    $logger->error("ERROR:  Auto-Setup processing failed; see earlier messages.");
	}

	## We have a run of discovery, successful or not.  Note that if we return early here at the end, we
	## will not update the ${hostname}_last_discovery_time file and thus the client will likely continue
	## to repeat the same discovery processing over and over, perhaps repeatedly peppering the server with
	## the same results, until the problem is addressed.
	##
	## FIX MAJOR:  To address that, we should have max-retries logic in here that will cut off repeated
	## attempts to run discovery with exactly the same trigger-file timestamp after a few (perhaps 3)
	## failed attempts, so we don't keep pounding the server with attempts that for whatever reason will
	## never work.  The administrator could still trigger new passes of discovery by updating the trigger
	## file on the server, so there would be no general loss of functionality.  If we do stop repeating
	## because of a max-retries limit, we should definitely log that fact locally on the client.

	if ($send_results) {
	    if ( not $send_outcome ) {
		## If we were supposed to send results to the server but we couldn't get through or there was some serious
		## server-side operational failure, we don't finish up by updating our local timestamp file to indicate that
		## we're done with this trigger file, nor do we update the $previous_results_path to capture the results
		## of this pass and possibly affect how the trigger if_duplicate option will be interpreted the next time
		## around.  Instead, we will try again with whatever instructions and trigger file we see on the next polling
		## cycle, even if neither of them have changed in the meantime.  This will allow the client to keep sending
		## results until it finally gets through and there is no serious server-side failure.
		## FIX MAJOR:  Implement some max-retries counting here.
		return 0;
	    }
	    if ( $server_status eq 'success' ) {
		## Finally, we have a supposedly successful auto-setup.
		##
		## Do whatever is appropriate with the hostname by which this machine will be known to the server.
		## We used to only match lowercase here, because that was our standard for hostnames elsewhere
		## within GDMA.  But now we allow mixed-case.  This is just a very basic validation check; we're
		## not concerned with exactness of formulation, since we assume the server must have got it right.
		##
		if ( $message =~ /^Configured hostname is '([-.a-zA-Z0-9]+?)'\./m ) {
		    my $configured_hostname = $1;
		    chomp $message;
		    $logger->notice("NOTICE:  Auto-Setup call succeeded, with these server-side messages:\n$message");
		    if ( write_override_file( $OverridefilePath, $configured_hostname ) ) {
			$logger->notice("NOTICE:  Forced_Hostname is being dynamically changed to \"$configured_hostname\".");
			$g_config{Forced_Hostname} = $configured_hostname;
		    }
		    else {
			$logger->error("ERROR:  Forced_Hostname has not been changed due to file-handling problems.");
			## Returning 0 here will cause the next polling cycle to conclude that a pass of discovery is
			## still warranted.  And since we haven't yet saved the current round of discovery results to
			## the $previous_results_path, we won't have any interference from the if_duplicate option in
			## the trigger file on future passes using the same instructions and trigger.
			## FIX MAJOR:  Implement some max-retries counting here.
			return 0;
		    }
		}
		elsif ( $message =~ /^Determined hostname is '([-.a-zA-Z0-9]+?)'\./m ) {
		    ## my $determined_hostname = $1;
		    chomp $message;
		    $logger->notice("NOTICE:  Auto-Setup call succeeded, with these server-side messages:\n$message");
		}
		else {
		    ## This can't happen.  If it does, we have an implementation problem in the server-side regisration script.
		    $logger->error("ERROR:  Auto-Setup supposedly succeeded on the server side, but returned no valid hostname.");
		    if ($message) {
			chomp $message;
			$logger->notice("NOTICE:  Auto-Setup server-side messages were:\n$message");
		    }
		    ## FIX MINOR:  By returning here and not updating the $last_discovery_path file and possibly the
		    ## $previous_results_path as well, we are pretty much guaranteeing that another pass of discovery
		    ## will be run again soon, and that will probably happen over and over.  Given that each pass of
		    ## auto-setup may affect the configuration database, we might experience a certain amount of churn
		    ## on the server side, and in particular in temporarily allocated sequence IDs.  So those ID values
		    ## could quickly run up to large numbers.  There's nothing especially wrong in the sense of being
		    ## non-functional with that, but it does make looking at the database a lot more opaque to a human.
		    ## FIX MAJOR:  Implement some max-retries counting here.
		    return 0;
		}
	    }
	    else {
		## In this case, we managed to send the data to the server, and the server accepted it and was able to
		## interpret it, but the server didn't like it.  Maybe it recognized invalid data, maybe it recognized
		## a client-side failure, or maybe it found a logical inconsistency when trying to apply changes to the
		## database.  In all such cases, the server wants us not to send in exactly the same data again, since
		## it won't do any good.  If something changes on the server that might affect its ability to accept
		## the same discovery results, the administrator can install a new trigger and initiate a separate run
		## of discovery.  So our action here is just to fall through and update our local files to indicate we
		## are done with this pass of discovery.
	    }
	}

	# Save the discovery results aside for future comparison.  If the results are identical to those of
	# a previous pass, we don't bother to overwrite the previous results, so the timestamp of the first
	# time these result were generated is preserved.  Possibly, that might be important later on.
	if ( not $results_are_identical ) {
	    if ( not rename $results_path, $previous_results_path ) {
		my $os_error = "$!";
		$os_error .= " ($^E)" if "$^E" ne "$!";
		$logger->error("ERROR:  Cannot save the $results_type discovery results to file \"$previous_results_path\" ($os_error).");
		## FIX MAJOR:  Implement some max-retries counting here.
		return 0;
	    }
	}

	return update_last_discovery_file( $trigger_path, $last_discovery_path );
    }

    $logger->error("ERROR:  Got unexpected status \"$status\" back from call to fetch_auto_setup_files().");
    return 0;
}

################################################################################
#
#   update_last_discovery_file()
#
################################################################################
sub update_last_discovery_file {
    my ( $trigger_path, $last_discovery_path ) = @_;

    my $trigger_mtime = ( stat $trigger_path )[9];
    if ( not defined $trigger_mtime ) {
	$logger->error("ERROR:  Cannot find the last-modification time for trigger file \"$trigger_path\" ($!).");
	return 0;
    }

    my $last_discovery_mtime = ( stat $last_discovery_path )[9];
    if ( not defined $last_discovery_mtime ) {
	my $temp_last_discovery_path = "$last_discovery_path.tmp";
	my $old_umask                = umask 0177;
	if ( not open( DISCOVERY, '>', $temp_last_discovery_path ) ) {
	    $logger->error("ERROR:  Cannot open the temporary last-discovery file \"$temp_last_discovery_path\" ($!).");
	    umask $old_umask;
	    return 0;
	}
	umask $old_umask;
	if ( not close DISCOVERY ) {
	    $logger->error("ERROR:  Cannot close the temporary last-discovery file \"$temp_last_discovery_path\" ($!).");
	    return 0;
	}
	if ( not utime( $trigger_mtime, $trigger_mtime, $temp_last_discovery_path ) ) {
	    $logger->error("ERROR:  Cannot set the last-modification time on temporary last-discovery file \"$temp_last_discovery_path\" ($!).");
	    unlink $temp_last_discovery_path;
	    return 0;
	}
	if ( not rename $temp_last_discovery_path, $last_discovery_path ) {
	    my $os_error = "$!";
	    $os_error .= " ($^E)" if "$^E" ne "$!";
	    $logger->error("ERROR:  Cannot rename the temporary last-discovery file \"$temp_last_discovery_path\" ($os_error).");
	    unlink $temp_last_discovery_path;
	    return 0;
	}
    }
    elsif ( not utime( $trigger_mtime, $trigger_mtime, $last_discovery_path ) ) {
	$logger->error("ERROR:  Cannot set the last-modification time on last-discovery file \"$last_discovery_path\" ($!).");
	return 0;
    }

    return 1;
}

################################################################################
#
#   fetch_auto_setup_files()
#
#   Grabs a copy of the current trigger and instructions files from our config
#   server.  Because we don't have any means worked out yet for remote discovery
#   of GDMA client assets, this routine will only fetch files for the current
#   GDMA client, ignoring possible trigger and instructions files for associated
#   GDMA clients if the GDMA_Multihost option is set.
#
#   Returns:
#   0 on configuration or pull failure (check $failed_msg_ref; it will be defined on error,
#     undef on simple failure to find a file on the remote side)
#   1 if no error or pull failure occurred, but we do not have a matched pair of trigger
#     and instructions files for this $hostname that should trigger a new pass of discovery
#   2 if no error or pull failure occurred, and we now have a matched pair of trigger
#     and instructions files for this $hostname that should trigger a new pass of discovery
#   See also the description of the $failed_msg_ref argument.
#
#   Arguments:
#   $configserver        - Web protocol and server where to pull the trigger and
#                          instructions files from.
#   $hostname            - Long-form or short-form of the current GDMA hostname.
#   $trigger_path        - This is an output parameter.  It must be a reference
#                          to a scalar variable in which will be placed the full
#                          local path to the trigger file if a new pass of
#                          discovery should be run.
#   $instructions_path   - This is an output parameter.  It must be a reference
#                          to a scalar variable in which will be placed the full
#                          local path to the instructions file if a new pass of
#                          discovery should be run.
#   $last_discovery_path - This is an output parameter.  It must be a reference
#                          to a scalar variable in which will be placed the full
#                          local path to the ${hostname}_last_discovery_time
#                          file if a new pass of discovery should be run.
#                          The last-modified timestamp of this file must be
#                          set outside of this routine to the last-modified
#                          timestamp of the trigger file used for a successful
#                          pass of discovery.  It will be used here to decide
#                          whether a new pass of discovery is warranted.
#   $failed_msg_ref - This is an output parameter.  It must be a reference to
#                     a scalar variable, which in the event of certain types
#                     of failure to fetch the trigger and/or instruction
#                     file will be populated with a low-level error message
#                     explaining the failure.  Not all callers will care
#                     about this level of detail, but all callers must supply
#                     a valid scalar reference for this parameter.  See the
#                     description of the $ref_errormsg parameter in the
#                     get_gdma_cfg_file_using_http() routine for more detail.
#
################################################################################
sub fetch_auto_setup_files {
    my ( $configserver, $hostname, $trigger_path, $instructions_path, $last_discovery_path, $failed_msg_ref ) = @_;
    my $ret_val       = undef;
    my $headpath      = GDMA::Utils::get_headpath();
    my $file_modified = 0;
    my $error_detail  = undef;

    if ( not defined $configserver ) {
	$logger->error("ERROR:  fetch_auto_setup_files:  no configuration server defined");
	## We use a bit of inside information here not directly supplied by the caller
	## (that $configserver is derived from Target_Server in the calling code) to
	## make this error message more meaningful to the human who will need to debug
	## this failure.
	$$failed_msg_ref = 'no Target_Server configuration server is defined';
	return 0;
    }

    # Using the supplied $hostname, we generate the filenames of the corresponding trigger and
    # instructions files.  Then we look for those files on the server.  If we find them, we pull
    # them back to the client.  If we don't find a trigger file, there is no point in looking for
    # an instructions file, and we tell the client that it should not run a pass of discovery,
    # even if it already has in hand a set of files that would otherwise cause it to do so --
    # clearly, the server is no longer interested in having the client do discovery.  If we do find
    # a trigger file, we look for an instructions file.  If we end up with a pair of files wherein
    # the last-modified timestamp of the trigger file is younger than that of the instructions file,
    # and either the last_discovery file does not exist or its own last_discovery file is older than
    # that of the trigger file, we tell the caller that a pass of discovery is warranted.  This will
    # be true even if we actually pulled no files because the trigger and/or instructions file(s) we
    # already had in hand match those on the server.

    $$trigger_path        = "${headpath}${file_separator}tmp${file_separator}${hostname}_trigger";
    $$instructions_path   = "${headpath}${file_separator}tmp${file_separator}${hostname}_instructions";
    $$last_discovery_path = "${headpath}${file_separator}tmp${file_separator}${hostname}_last_discovery_time";

    my $trigger_url      = "${configserver}/$g_config{Auto_Setup_Trigger_Dir}/${hostname}_trigger";
    my $instructions_url = "${configserver}/$g_config{Auto_Setup_Instructions_Dir}/${hostname}_instructions";

    ## In the following code, we're a bit paranoid about modifying the setting of the
    ## $$failed_msg_ref value.  We only want to change the upstream value if we get
    ## a final failure to fetch a file, not if the get_gdma_cfg_file_using_http()
    ## routine happens to modify our referenced variable on some call where it
    ## succeeds.  So we use a local variable to intercede in our calculations.

    $logger->debug("DEBUG:  Getting $trigger_url to $$trigger_path (timeout $g_config{ConfigPull_Timeout}).");
    $error_detail = undef;
    $ret_val = get_gdma_cfg_file_using_http( $trigger_url, $$trigger_path, 'trigger', $g_config{ConfigPull_Timeout},
	\$file_modified, \$error_detail );
    if ( $ret_val == 0 ) {
	## This is a failure, but might not be an error, since we might be trying to pull based on the wrong form of the hostname.
	$logger->notice("NOTICE:  fetch_auto_setup_files:  Pull failure for $trigger_url");
	$$failed_msg_ref = $error_detail if defined $error_detail;
	return 0;
    }

    # FIX MINOR:  Logically, according to our model of the trigger file, we ought to parse it here and check
    # the last_step value.  If it is "ignore_instructions", we ought to skip attempting to pull back the
    # instructions file, and return status 1 to indicate that we should not attempt a new pass of discovery.

    $logger->debug("DEBUG:  Getting $instructions_url to $$instructions_path (timeout $g_config{ConfigPull_Timeout}).");
    $error_detail = undef;
    $ret_val = get_gdma_cfg_file_using_http( $instructions_url, $$instructions_path, 'instructions', $g_config{ConfigPull_Timeout},
	\$file_modified, \$error_detail );
    if ( $ret_val == 0 ) {
	## This is a failure, but might not be an error, since we might be trying to pull based on the wrong form of the hostname.
	$logger->notice("NOTICE:  fetch_auto_setup_files:  Pull failure for $instructions_url");
	$$failed_msg_ref = $error_detail if defined $error_detail;
	return 0;
    }

    my $trigger_mtime      = ( stat $$trigger_path )[9];
    my $instructions_mtime = ( stat $$instructions_path )[9];
    return 1 if $instructions_mtime >= $trigger_mtime;
    my $last_discovery_mtime = ( stat $$last_discovery_path )[9];
    return 1 if defined($last_discovery_mtime) and $last_discovery_mtime >= $trigger_mtime;
    return 2;
}

################################################################################
#
#   fetch_config_file()
#
#   Grabs a copy of the current config file from our config server.
#   If the GDMA_Multihost option is set, will pull multiple config files
#   and will behave similar to a Windows child server.
#
#   Returns 0 on pull error, 1 otherwise.  See also the description of the
#   $failed_msg_ref argument.
#
#   Arguments:
#   $configserver  - Server where to pull the config file from.
#   $hostname      - Long-form or short-form of the current GDMA hostname.
#   $configfile    - Full name of the config file.
#   $conf_modified - This is an output parameter.  Will be set to 1, if the
#                    config file is found updated on the server.
#   $modified_conf_filenames - A reference to an array.  This will be set to
#                              names of the conf files modified, if any.
#   $conf_files     - A reference to an array where a list of config files
#                     will be recorded.
#   $failed_msg_ref - This is an output parameter.  It must be a reference to
#                     a scalar variable, which in the event of certain types
#                     of failure to fetch the config file will be populated
#                     with a low-level error message explaining the failure.
#                     Not all callers will care about this level of detail, but
#                     all callers must supply a valid scalar reference for this
#                     parameter in case it is dereferenced by the lower-level code.
#                     See the description of the $ref_errormsg parameter in the
#                     get_gdma_cfg_file_using_http() routine for more detail.
#
################################################################################
sub fetch_config_file {
    my ( $configserver, $hostname, $configfile, $conf_modified, $modified_conf_filenames, $conf_files, $failed_msg_ref ) = @_;
    my $ret_val               = 1;
    my $headpath              = GDMA::Utils::get_headpath();
    my @config_file_list      = ();
    my $current_config_file   = ();
    my $filename              = ();
    my $filepath              = ();
    my $current_conf_modified = 0;

    # Flush.
    $$conf_modified           = 0;
    @$modified_conf_filenames = ();
    @$conf_files              = ();

    # If the GDMA_Multihost option is set, GDMA will download multiple configuration files rather than a single one.

    if ( not defined $configserver ) {
	$logger->error("ERROR:  fetch_config_file:  no configuration server defined");
	## We use a bit of inside information here not directly supplied by the caller
	## (that $configserver is derived from Target_Server in the calling code) to
	## make this error message more meaningful to the human who will need to debug
	## this failure.
	$$failed_msg_ref = 'no Target_Server configuration server is defined';
	$ret_val         = 0;
    }
    ## FIX MINOR:  do we need case-insensitive matching here?
    elsif ( defined( $g_config{GDMA_Multihost} ) && $g_config{GDMA_Multihost} eq 'on' ) {
	## Get the available list of configuration files in "GDMAConfigDir"
	my $url = "$configserver/$g_config{GDMAConfigDir}";
	$url .= '/' if ( $g_config{GDMAConfigDir} !~ m{/$} );

	@config_file_list = get_config_file_list($url);
	push @$conf_files, @config_file_list;

	if ( scalar(@$conf_files) eq 0 ) {
	    $logger->error("ERROR:  fetch_config_file:  Could not retrieve a list of config files.");
	    $$failed_msg_ref = 'could not retrieve a list of config files';
	    $ret_val         = 0;
	}

	# In the following loop, we're a bit paranoid about modifying the setting of the
	# $$failed_msg_ref value.  We only want to change the upstream value if we get
	# a final failure to fetch a file, not if the get_gdma_cfg_file_using_http()
	# routine happens to modify our referenced variable on some call where it
	# succeeds.  So we use a local variable to intercede in our calculations.
	#
	# Note that it's a bit questionable what the caller ought to do in GDMA_Multihost
	# mode if we experience a retrieval failure but $$failed_msg_ref is not set.
	# If it was trying to retrieve its own config file and it got a 404 Not Found,
	# then it should attempt to auto-register, just as though it were operating in
	# non-multihost mode.  But if it was trying to retrieve the config file for some
	# other host and got a 404 not found, that should not be occasion to run an
	# auto-register, because the retrieval failure is not for this GDMA host itself.
	# This situation is not yet well studied nor necessarily handled properly in the
	# calling code.  FIX MINOR:  Those developments will await some future version.

	# Pull each of these configuration files.
	foreach $current_config_file (@$conf_files) {
	    my @tmp = split( /\//, $current_config_file );
	    $filename = pop(@tmp);
	    $filepath = $headpath . $file_separator . 'config' . $file_separator . $filename;
	    $logger->debug("DEBUG:  Getting $current_config_file to $filepath (timeout $g_config{ConfigPull_Timeout}).");
	    my $error_detail = undef;
	    $ret_val = get_gdma_cfg_file_using_http( $current_config_file, $filepath, 'config', $g_config{ConfigPull_Timeout},
		\$current_conf_modified, \$error_detail );
	    if ( $ret_val == 0 ) {
		## Return error if pull fails for any config file.
		$logger->notice("NOTICE:  fetch_config_file:  Pull failure for $current_config_file");
		$$failed_msg_ref = $error_detail if defined $error_detail;
		return $ret_val;
	    }

	    # Set $conf_modified = 1, only when current config file is modified.
	    # We don't want to overwrite $conf_modified with zero, if any previous
	    # configuration file was modified.
	    if ($current_conf_modified) {
		$$conf_modified = $current_conf_modified;
		push( @$modified_conf_filenames, $filename );
	    }
	}
    }
    else {
	## Try do the pull of the externals cfg with http/s
	my $hostnamecfg = "$g_config{GDMAConfigDir}/gwmon_$hostname.cfg";

	# Add the hostconfig file url to the list.
	push @$conf_files, "$configserver/$hostnamecfg";

	$logger->debug("DEBUG:  Getting $configserver/$hostnamecfg to $configfile (timeout $g_config{ConfigPull_Timeout}).");
	$ret_val = get_gdma_cfg_file_using_http( "$configserver/$hostnamecfg", "$configfile", 'config', $g_config{ConfigPull_Timeout},
	    $conf_modified, $failed_msg_ref );
	if ($$conf_modified) {
	    push( @$modified_conf_filenames, "gwmon_$hostname.cfg" );
	}
    }
    return $ret_val;
}

################################################################################
#
#   get_config_file_list()
#
#   Returns a list of configuration files found in GDMAConfigDir
#   Returns empty array on error.
#
#   Arguments:
#   $url - url of GDMAConfigDir to get the list of configuration files.
#
################################################################################
sub get_config_file_list {
    my $url = shift;
    my $agent;
    my $request;
    my $response;
    my $errormsg;
    my $page;
    my $page_parser;
    my @links;
    my @urls = ();

    my $ssl_opts = GDMA::Utils::gdma_client_ssl_opts($logger);
    $agent = LWP::UserAgent->new(
	agent    => 'GDMA Client/' . get_version(),
	ssl_opts => $ssl_opts,
    );
    $request = HTTP::Request->new( GET => $url );

    # Default is both GET and HEAD redirect, if we don't make this call.  We are now (as of GDMA 2.3.2)
    # disabling automatic redirects, as we must intervene and prevent a possible HTTPS-to-HTTP downgrade.
    # So all redirects are now handled manually here.
    $agent->requests_redirectable([]);

    my $remaining_fetches = $g_config{Max_Server_Redirects};
    $remaining_fetches = $Default_Max_Server_Redirects if not defined $remaining_fetches;

    for ( my $fetch = 1, ++$remaining_fetches ; $fetch && $remaining_fetches > 0 ; --$remaining_fetches ) {
	$fetch = 0;    # Only fetch upon redirect if explicitly commanded below.

	if ( not GDMA::Utils::is_valid_ca_path( \$errormsg, $url ) ) {
	    $logger->error("ERROR:  $errormsg");
	}
	elsif ( not do_timed_request( 'GDMA config file list fetch', $agent, $request, $g_config{ConfigPull_Timeout}, \$response, \$errormsg ) ) {
	    $logger->error("ERROR:  Failed to get config file list $url -- $errormsg");
	}
	elsif ( not $response->is_success ) {
	    $logger->notice( "NOTICE:  Failed to get config file list $url -- " . $response->status_line );
	    if ( is_redirect( $response->code ) ) {
		my $redirect_location = $response->header('Location');
		## Unless the protocol is downgraded from HTTPS to HTTP, we'll allow the redirect,
		## subject to the configured (or defaulted) Max_Server_Redirects value.
		if ( not defined $redirect_location ) {
		    $logger->notice(" (got redirected, but with no new location supplied)");
		}
		elsif ($redirect_location =~ m{^https?://}i
		    && ( $url =~ m{^http://}i || $redirect_location =~ m{^https://}i )
		    && $remaining_fetches >= 2 )
		{
		    $logger->notice(" (redirecting to $redirect_location)");
		    $url = $redirect_location;
		    $request->uri($url);
		    $fetch = 1;
		}
		else {
		    $logger->notice(" (ignoring redirection to $redirect_location)");
		}
	    }
	}
	else {
	    $page = $response->content;

	    # Parse and extract the links from the above url
	    $page_parser = HTML::LinkExtor->new( undef, $url );
	    $page_parser->parse($page)->eof;
	    @links = $page_parser->links;

	    # Create an array of required config file links to be returned.
	    foreach my $link (@links) {
		## Append only those links which contain ".cfg" (i.e., links to configuration files).
		if ( $$link[2] =~ /\.cfg$/ ) {
		    push @urls, $$link[2];
		}
	    }
	}
    }

    return @urls;
}

################################################################################

# With the use of Log::Log4perl for logging, we can no long assure ourselves that we won't run into
# reentrancy problems if we try to log directly out of of signal handler (GDMA-404).  So instead we
# use a global array and just record the signals as they come in, for inspection and logging later
# on once we're out of the critical region and before we do any other sort of logging.  There might
# be some race conditions involved if Perl allows signal B to be received while we are still in the
# signal handler for signal A, that might cause our array of caught-signal names to be slightly out
# of order.  But that shouldn't matter much for our simple purposes.

sub catch_abort_signal {
    my $signame = shift;
    push @caught_abort_signals, $signame;
    die "timed out\n";
}

# The LWP::Useragent::mirror() code only pays attention to a $ua->timeout($timeout) setting if it is
# fetching via HTTP.  If it is fetching via HTTPS, this timeout is ignored.  The do_timed_mirror()
# routine encapsulates the extra processing needed to impose an external timeout on the fetch.
sub do_timed_mirror {
    my $action    = $_[0];    # required argument
    my $useragent = $_[1];    # required argument
    my $url       = $_[2];    # required argument
    my $file      = $_[3];    # required argument
    my $timeout   = $_[4];    # required argument
    my $response  = $_[5];    # required argument
    my $errormsg  = $_[6];    # required argument

    $$errormsg = '';
    my $successful = 1;

    if ( $^O eq 'MSWin32' ) {
	## Windows has no proper signal support, and no POSIX real-time timers, so on this platform
	## we cannot use the code below for UNIX platforms to time out the mirror request.  We have
	## not yet figured out a way to do so using native Windows APIs, because of the complexity
	## of their possible interaction with the Perl run-time model.  Likely, we would need to
	## code a C-level Perl package, and dig into Perl internals on this platform, to make it all
	## work reliably without any race conditions.  Instead, for now we depend only on whatever
	## socket-level timeouts are already implemented on this platform.  Experiments with an
	## attempted mirror via HTTPS from an inaccessible host to Linux shows that this results in
	## about a 190-second timeout.  The exact behavior on Windows is as yet untested.
	my $old_umask = umask 0177;
	eval {
	    ## mirror() can die(), so we need to encapsulate it and check for those types of errors.
	    $$response = $useragent->mirror( $url, $file );
	};
	if ($@) {
	    chomp $@;
	    $$errormsg  = "$action failure ($@).";
	    $successful = 0;
	}
	umask $old_umask;
    }
    else {
	@caught_abort_signals = ();
	do {
	    ## Usually in a routine like this, we would wrap the code to which a timeout should apply
	    ## in an alarm($timeout) ... alarm(0) sequence (with lots of extra protection against race
	    ## conditions).  However, in the present case, the code we want to wrap already internally
	    ## absconds with control over SIGALRM.  So we need to impose an independent timer at this
	    ## level.  For that purpose, we have chosen to use the SIGABRT signal.
	    local $SIG{ABRT} = \&catch_abort_signal;

	    # If our timer expires, it may kill the wrapped code before it has a chance to cancel a
	    # future alarm.  Hopefully it will have a local SIGALRM handler, so that setting should
	    # be unwound automatically when we die out of our timer's signal handler and abort our
	    # eval{};, but if we got such an uncanceled alarm and we either didn't have our own signal
	    # handler in place or we hadn't ignored the signal at this level, we would exit.  It seems
	    # safest to just use the same signal handler we're using for the SIGABRT signal.
	    local $SIG{ALRM} = \&catch_abort_signal;

	    ## The nested eval{}; blocks protect against race conditions, as described in the comments.
	    my $old_umask = umask 0177;
	    eval {
		## Create our abort timer in a disabled state.
		my $timer = POSIX::RT::Timer->new( signal => SIGABRT );
		eval {
		    ## Start our abort timer.
		    $timer->set_timeout($timeout);

		    # We might die here either explicitly or because of a timeout and the signal
		    # handler action.  If we get the abort signal and die because of it, we need
		    # not worry about resetting the abort before exiting the eval, because it has
		    # already expired (we use a one-shot timer).
		    eval {
			## The user-agent request() logic internally calls alarm() somewhere,
			## perhaps within some sleep() or equivalent indirect call.  We presume
			## that the mirror() logic probably does the same thing.  That's why we use
			## an independent timer and an independent signal (and signal handler).  We
			## haven't actually identified the line of code that does so, but we have
			## shown by experiment that this is the case for a request(), and it would
			## kill our own carefully-set SIGALRM timeout so it becomes inoperative.
			## FIX LATER:  Track down where the alarm stuff happens, and submit a bug
			## report that this should be described in the package documentation.
			$$response = $useragent->mirror( $url, $file );    # Send mirror request, get response
		    };
		    ## We got here because one of the following happened:
		    ##
		    ## * the wrapped code die()d on its own (not that we have knowledge of any
		    ##   specific circumstances in which that might predictably happen), in which
		    ##   case we probably have our timer interrupt still armed, and possibly we
		    ##   might also have an alarm interrupt from the wrapped code still armed
		    ## * the wrapped code exited normally (either it ran to completion or it ran up
		    ##   against its own internal timeout), in which case we probably have our timer
		    ##   interrupt still armed
		    ## * our timer expired, in which case we might have an alarm interrupt from the
		    ##   wrapped code still armed
		    ##
		    ## If interrupts from both signals are still armed, there is no way to know the
		    ## relative sequence in which they will fire.  Consequently, we have two signals
		    ## we need to manage here, and we need to resolve all possible orders of signal
		    ## generation and the associated race conditions.  That accounts for the triple
		    ## nesting of eval{}; blocks here and the repeated signal cancellations.

		    ## Save the death rattle in case our subsequent processing inadvertenty changes it
		    ## before we get to use it.
		    my $exception = $@;

		    # In case the wrapped code's alarm was still armed when either it died on its
		    # own or we aborted the code via our timer, disarm the alarm here.
		    alarm(0);

		    # Stop our abort timer.
		    $timer->set_timeout(0);

		    # Percolate failure to the next level of nesting.
		    if ($exception) {
			chomp $exception;
			die "$exception\n";
		    }
		};
		## Save the death rattle in case our subsequent processing inadvertenty changes it
		## before we get to use it.
		my $exception = $@;

		# In case the wrapped code died while its alarm was still armed, and our timer
		# expired before we could disarm the alarm just above, disarm it here.
		alarm(0);

		# In case the wrapped code died while its alarm was still armed, and then the
		# alarm fired just above before we could disarm it (and subsequently disarm our
		# own timer), disarm our timer here.
		$timer->set_timeout(0);

		# Percolate failure to the next level of nesting.
		if ($exception) {
		    chomp $exception;
		    die "$exception\n";
		}
	    };
	    ## Check for either any residual cases where we failed to disable an interrupt before
	    ## it got triggered, or the percolation of whatever interrupt or other failure might
	    ## have occurred within the nested eval{}; blocks.
	    if ($@) {
		chomp $@;
		$$errormsg  = "$action failure ($@).";
		$successful = 0;
	    }
	    umask $old_umask;
	};
	## We log the list of caught signals once we're safely out of the region where the signal
	## handler is operative and might still be extending the @caught_abort_signals array.
	## This logging is not fully synchronous with receipt of the signal(s), but it should be
	## close enough for our usage.
	foreach my $signame (@caught_abort_signals) {
	    $logger->notice("NOTICE:  Caught a SIG$signame signal!");
	}
    }

    return $successful;
}

sub do_timed_request {
    my $action    = $_[0];    # required argument
    my $useragent = $_[1];    # required argument
    my $request   = $_[2];    # required argument
    my $timeout   = $_[3];    # required argument
    my $response  = $_[4];    # required argument
    my $errormsg  = $_[5];    # required argument

    $$errormsg = '';
    my $successful = 1;

    if ( $^O eq 'MSWin32' ) {
	## See do_timed_mirror() for why we run differently on this platform.
	eval {
	    ## In case request() might ever die, we encapsulate it so we can keep our daemon running.
	    $$response = $useragent->request($request);
	};
	if ($@) {
	    $@ =~ s/\s+$//;  # chomp depends on $/, which might not contain what we expect
	    $$errormsg  = "$action failure ($@).";
	    $successful = 0;
	}
    }
    else {
	@caught_abort_signals = ();
	do {
	    ## Usually in a routine like this, we would wrap the code to which a timeout should apply
	    ## in an alarm($timeout) ... alarm(0) sequence (with lots of extra protection against race
	    ## conditions).  However, in the present case, the code we want to wrap already internally
	    ## absconds with control over SIGALRM.  So we need to impose an independent timer at this
	    ## level.  For that purpose, we have chosen to use the SIGABRT signal.
	    local $SIG{ABRT} = \&catch_abort_signal;

	    # If our timer expires, it may kill the wrapped code before it has a chance to cancel a
	    # future alarm.  Hopefully it will have a local SIGALRM handler, so that setting should
	    # be unwound automatically when we die out of our timer's signal handler and abort our
	    # eval{};, but if we got such an uncanceled alarm and we either didn't have our own signal
	    # handler in place or we hadn't ignored the signal at this level, we would exit.  It seems
	    # safest to just use the same signal handler we're using for the SIGABRT signal.
	    local $SIG{ALRM} = \&catch_abort_signal;

	    ## The nested eval{}; blocks protect against race conditions, as described in the comments.
	    eval {
		## Create our abort timer in a disabled state.
		my $timer = POSIX::RT::Timer->new( signal => SIGABRT );
		eval {
		    ## Start our abort timer.
		    $timer->set_timeout($timeout);

		    # We might die here either explicitly or because of a timeout and the signal
		    # handler action.  If we get the abort signal and die because of it, we need
		    # not worry about resetting the abort before exiting the eval, because it has
		    # already expired (we use a one-shot timer).
		    eval {
			## The user-agent request() logic internally calls alarm() somewhere, perhaps
			## within some sleep() or equivalent indirect call.  That's why we switched
			## to using an independent timer and an independent signal (and signal
			## handler).  We haven't actually identified the line of code that does so,
			## but we have shown by experiment that this is the case, and it would kill
			## our own carefully-set SIGALRM timeout so it becomes inoperative.
			## FIX LATER:  Track down where the alarm stuff happens, and submit a bug
			## report that this should be described in the package documentation.
			$$response = $useragent->request($request);    # Send request, get response
		    };
		    ## We got here because one of the following happened:
		    ##
		    ## * the wrapped code die()d on its own (not that we have knowledge of any
		    ##   circumstances in which that might predictably happen), in which case we
		    ##   probably have our timer interrupt still armed, and possibly we might
		    ##   also have an alarm interrupt from the wrapped code still armed
		    ## * the wrapped code exited normally (either it ran to completion or it ran up
		    ##   against its own internal timeout), in which case we probably have our timer
		    ##   interrupt still armed
		    ## * our timer expired, in which case we might have an alarm interrupt from the
		    ##   wrapped code still armed
		    ##
		    ## If interrupts from both signals are still armed, there is no way to know the
		    ## relative sequence in which they will fire.  Consequently, we have two signals
		    ## we need to manage here, and we need to resolve all possible orders of signal
		    ## generation and the associated race conditions.  That accounts for the triple
		    ## nesting of eval{}; blocks here and the repeated signal cancellations.

		    ## Save the death rattle in case our subsequent processing inadvertenty changes it
		    ## before we get to use it.
		    my $exception = $@;

		    # In case the wrapped code's alarm was still armed when either it died on its
		    # own or we aborted the code via our timer, disarm the alarm here.
		    alarm(0);

		    # Stop our abort timer.
		    $timer->set_timeout(0);

		    # Percolate failure to the next level of nesting.
		    if ($exception) {
			$exception =~ s/\s+$//;  # chomp depends on $/, which might not contain what we expect
			die "$exception\n";
		    }
		};
		## Save the death rattle in case our subsequent processing inadvertenty changes it
		## before we get to use it.
		my $exception = $@;

		# In case the wrapped code died while its alarm was still armed, and our timer
		# expired before we could disarm the alarm just above, disarm it here.
		alarm(0);

		# In case the wrapped code died while its alarm was still armed, and then the
		# alarm fired just above before we could disarm it (and subsequently disarm our
		# own timer), disarm our timer here.
		$timer->set_timeout(0);

		# Percolate failure to the next level of nesting.
		if ($exception) {
		    $exception =~ s/\s+$//;  # chomp depends on $/, which might not contain what we expect
		    die "$exception\n";
		}
	    };
	    ## Check for either any residual cases where we failed to disable an interrupt before
	    ## it got triggered, or the percolation of whatever interrupt or other failure might
	    ## have occurred within the nested eval{}; blocks.
	    if ($@) {
		$@ =~ s/\s+$//;  # chomp depends on $/, which might not contain what we expect
		$$errormsg  = "$action failure ($@).";
		$successful = 0;
	    }
	};
	## We log the list of caught signals once we're safely out of the region where the signal
	## handler is operative and might still be extending the @caught_abort_signals array.
	## This logging is not fully synchronous with receipt of the signal(s), but it should be
	## close enough for our usage.
	foreach my $signame (@caught_abort_signals) {
	    $logger->notice("NOTICE:  Caught a SIG$signame signal!");
	}
    }

    return $successful;
}

################################################################################
#
#   get_gdma_cfg_file_using_http()
#
#   Pulls the config file via http/s.
#
#   The URL that we try to access may be mirrored.  That means the actual
#   data may be received from a different location than suggested by the URL.
#   A mirror is an exact copy of the server, and is usually implemented to
#   speed up the downloads and to have a back up for the data.
#
#   Returns 0 on pull error, 1 otherwise.  See also the description of
#   the $ref_errormsg argument.
#
#   Arguments:
#   $cfgurl - URL to the cfg file location.
#   $outfile - The fully qualified name of where to save the cfg file.
#   $urltimeout - Timeout in seconds for the URL.
#   $conf_modified - This is an output parameter.  Will be set to 1, if the
#                    config file is found updated on the server.
#   $ref_errormsg - This is an output parameter.  It must be a reference to
#                   a scalar variable, which in the event of certain types
#                   of failure to fetch the config file will be populated
#                   with a low-level error message explaining the failure.
#                   Not all callers will care about this level of detail, but
#                   all callers must supply a valid scalar reference for this
#                   parameter in case it is dereferenced here.  See below.
#
#   This routine can either succeed or fail.  If it fails, the nature of that
#   failure can be important to the calling code, affecting its decisions on
#   what next steps to take.  So we need to return more information than just
#   the fact that a failure occurred.  For that purpose, we use $ref_errormsg.
#
#   The $$ref_errormsg value is intended to reflect situations in which a
#   failure to retrieve the file is either known to be due to some client-side
#   problem, or is due to some heretofore uncategorized networking/mirroring
#   problem which is not well understood.  In both of those cases, the calling
#   code should not try to auto-register simply because of this failure to
#   fetch the file, since it might well not be because the file is missing on
#   the server.  Instead, the calling code should at most spool a message back
#   to the server telling of the difficulty the client is having in accessing
#   the config file.  It is for that reason that the error detail is being
#   provided.  So the calling protocol for this routine is that the incoming
#   $$ref_errormsg value is left undisturbed unless this routine sees either a
#   recognizable client-side problem or an unrecognizable networking/mirroring
#   problem.  In both of those situations, the $$ref_errormsg value will be
#   assigned a text message which is the best information we have as to the
#   nature of the failure.  Before the call, the caller can set $$ref_errormsg
#   to undef, an empty string, or any special sentinal value of interest to the
#   caller that is expected to be different from any possible error message that
#   this routine might produce.  Then upon recognition (from its return value)
#   that this call has failed, the caller can compare $$ref_errormsg against its
#   value before calling this routine, and if it has changed, $$ref_errormsg
#   can be used to report problems to data sinks that might usefully notify
#   people of the problem, and avoid taking drastic steps like running an
#   auto-registration cycle.
#
################################################################################
sub get_gdma_cfg_file_using_http {
    my ( $cfgurl, $outfile, $filetype, $urltimeout, $conf_modified, $ref_errormsg ) = @_;
    my ( $gdma_useragent, $response, $errormsg );
    my $ret_val      = 0;
    my $error_detail = undef;

    $$conf_modified = 0;

    ## We set the ssl_opts whether or not this is an HTTPS connection, because they will be ignored if not.
    my $ssl_opts = GDMA::Utils::gdma_client_ssl_opts($logger);
    $gdma_useragent = LWP::UserAgent->new(
	agent    => 'GDMA Client/' . get_version(),
	timeout  => $urltimeout,
	ssl_opts => $ssl_opts,
    );

    $logger->debug("DEBUG:  Attempting to get $cfgurl to $outfile (timeout is set to " . $gdma_useragent->timeout() . " seconds).");

    # Default is both GET and HEAD redirect, if we don't make this call.  We are now (as of GDMA 2.3.2)
    # disabling automatic redirects, as we must intervene and prevent a possible HTTPS-to-HTTP downgrade.
    # So all redirects are now handled manually here.
    $gdma_useragent->requests_redirectable([]);

    my $remaining_fetches = $g_config{Max_Server_Redirects};
    $remaining_fetches = $Default_Max_Server_Redirects if not defined $remaining_fetches;

    for ( my $fetch = 1, ++$remaining_fetches ; $fetch && $remaining_fetches > 0 ; --$remaining_fetches ) {
	$fetch = 0;    # Don't loop to fetch upon failure, except upon redirect if explicitly commanded below.

	if ( not GDMA::Utils::is_valid_ca_path( \$errormsg, $cfgurl ) ) {
	    $logger->error("ERROR:  $errormsg");
	    ## This is a recognizable client-side problem; report that to the caller.
	    $error_detail = $errormsg;
	    $ret_val = 0;
	}
	## NOTE:  The $gdma_useragent->mirror() internals fetch into a new file, and then rename it to the
	## old file name.  So if some other process tries to read the file while the fetching is underway,
	## it will see either a complete copy of the old file, or a complete copy of the new file, but never
	## an incomplete new file (still being transferred).  However, the mirror() routine doesn't do an
	## atomic rename().  Instead, it first unlinks the old file, and then renames the new file to the old
	## filename, to accommodate poorly designed filesystems without checking what type of filesystem is
	## actually being used.  This leaves a small window of time during which another process can reach for
	## the file and find neither old nor new file in place.  The possibility of this situation has to be
	## accounted for on the reading side.
	elsif ( not do_timed_mirror( "GDMA $filetype file fetch", $gdma_useragent, $cfgurl, $outfile, $urltimeout, \$response, \$errormsg ) ) {
	    $logger->error("ERROR:  Failed to get $filetype file $cfgurl -- $errormsg");
	    ## mirror() might have died because of a file-transfer error, or because of a rename error.
	    ## We could probably safely ignore a file-transfer error, considering that the same error is
	    ## not likely to occur on the next cycle.  But a rename error is more serious.  For now, we
	    ## don't try to distinguish the type of error; we simply return a notice of failure to the
	    ## caller, even if we are left with the previous clean copy of the file still intact.
	    ##
	    ## This is an unrecognizable networking/mirroring problem; report that to the caller.
	    $error_detail = $errormsg;
	    $ret_val = 0;
	}
	elsif ( not $response->is_success ) {
	    $logger->notice( "NOTICE:  Failed to get $filetype file $cfgurl -- " . $response->status_line );
	    if ( $response->status_line =~ /304 not modified/i ) {
		$logger->notice(" (304's are generally safe to ignore)");
		## Return success.
		$ret_val = 1;
	    }
	    elsif ( is_redirect( $response->code ) ) {
		my $redirect_location = $response->header('Location');
		## Unless the protocol is downgraded from HTTPS to HTTP, we'll allow the redirect,
		## subject to the configured (or defaulted) Max_Server_Redirects value.
		if ( not defined $redirect_location ) {
		    $logger->notice(" (got redirected, but with no new location supplied)");
		    $error_detail = 'Got redirected, but with no new location supplied.';
		}
		elsif ($redirect_location =~ m{^https?://}i
		    && ( $cfgurl =~ m{^http://}i || $redirect_location =~ m{^https://}i )
		    && $remaining_fetches >= 2 )
		{
		    $logger->notice(" (redirecting to $redirect_location)");
		    ## There is no reason to set $error_detail in this case, both because this is a
		    ## recognizable networking/mirroring situation and (more importantly) because
		    ## given the conditions we just tested and the $fetch flag we are about to set,
		    ## we know we will loop around and end up either back here (though not forever)
		    ## or in some other case.
		    $cfgurl = $redirect_location;
		    $fetch  = 1;
		}
		else {
		    $logger->notice(" (ignoring redirection to $redirect_location)");
		    $error_detail = "Ignoring redirection to $redirect_location";
		}
		## Return error, unless overridden in a subsequent loop iteration.
		$ret_val = 0;
	    }
	    elsif ( $response->status_line =~ /404 Not Found/i ) {
		if ( -e $outfile ) {
		    ## Host configuration file is not present on the GW server, so remove host config file,
		    ## since that is a signal that the server doesn't want us monitoring or reporting results.
		    ## Now, Poller will just check auto file, since there will be no host config file around.
		    $logger->notice("NOTICE:  Removing the host $filetype file, because there is no corresponding file on the server.");
		    unlink($outfile);
		}
		## Return error, but with no $error_detail since this is a well-understood situation.
		$error_detail = undef;
		$ret_val = 0;
	    }
	    else {
		## Return error.  This is an unexpected case that we don't already have broken out as
		## a recognizable networking/mirroring problem, so we report details to the caller
		## as best we can.  Over time, if we see that customers are experiencing particular
		## $response->code values on a regular basis that we should ignore to the extent of
		## allowing upstream code to proceed as though it got a /404 Not Found/ status_line,
		## we can elaborate this else{} clause to break out particular cases of interest and
		## treat them differently.
		$error_detail = $response->status_line;
		$ret_val = 0;
	    }
	}
	else {
	    ## We just pulled a new conf file.
	    ##
	    ## We could potentially use a more-sophisticated algorithm to determine whether or not
	    ## the content of the configuration file has changed, by comparing the actual active
	    ## option settings and see whether any of them have changed, or whether there are any
	    ## new or now-missing options in the new copy.  Specifically, we should compare the
	    ## actual content of the old file we already had in hand, if any, with the content of
	    ## the new file.  Only if the sets of active options specified in the two files are
	    ## different should we conclude that there has in fact been some kind of change.
	    ##
	    ## However, we now use this routine to fetch multiple types of files from the server,
	    ## so any such comparison would need to understand the exact type of file being fetched
	    ## and all the ways in which it might be permuted and still be the "same" content.
	    ## It's not at all clear that any such complicated comparisons would truly yield any
	    ## significant benefit in trying to avoid further work.
	    ##
	    $$conf_modified = 1;
	    $logger->debug("DEBUG:  get_gdma_cfg_file_using_http:  $filetype file modified");

	    # This isn't that useful here due to mirroring (the $response->content we see here is
	    # generally just an empty string; the actual content got dumped to a local file and
	    # does not also get saved in the response).  But at least it tells us something happened.
	    my $content = $response->content;
	    if (not defined $content) {
		$content = '(content here is undefined; but see the file)';
	    }
	    elsif ($content eq '') {
	        $content = '(content here is empty, but see the file)';
	    }
	    $logger->debug( "DEBUG:  Retrieved content for $cfgurl as shown:\n" . $content );

	    # Return success.
	    $ret_val = 1;
	}
    }

    # We only set $$ref_errormsg at the end, in order to not get confused by some
    # error happening in an early cycle that would have caused us to report detail,
    # followed by a later known error that causes us not to want to report detail.
    $$ref_errormsg = $error_detail if !$ret_val && defined($error_detail);

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
#   do_checks()
#
#   Invokes one instance of gdma_run_checks.pl for each host in the hostlist
#   passed.  In Multihost mode there will be more than one hosts in the hostlist,
#   and the checks will be executed on each host in parallel with the others.
#   Returns 1 on success and 0 on failure.
#
#   Arguments:
#   $conf_files - A reference to the list of conf files to be processed.
#   $nchecks - Reference to a variable where the number of checks performed on
#              all hosts together are to be recorded.
#   $sleep_time - Reference to the total time for which poller waits for an
#              instance to complete the check execution
#
################################################################################
sub do_checks {
    my $hostname   = shift;
    my $conf_files = shift;
    my $nchecks    = shift;
    my $sleep_time = shift;
    my @hostlist   = ();
    my $osname     = $^O;
    my $outcome    = 1;
    my $debug      = defined( $g_opt{d} ) ? $g_opt{d} : 0;

    $$nchecks = 0;
    dump_config() if $debug == 2;

    # We received a list of configuration files found.  What we want is a
    # list of hosts to be monitored.  Extract hostnames from conf filenames.
    # When Multihost option is "On", there will be multiple hosts that
    # this program will execute the checks for.
    foreach my $file (@$conf_files) {
	if ( $file =~ /.+gwmon_(.+)\.cfg$/ ) {
	    push @hostlist, $1;
	}
	else {
	    $logger->error("ERROR:  Failed to parse configuration filename:  $file");
	    return 0;
	}
    }

    # For each host that we need to run checks on, we need to create a
    # new process and make that process run checks on the host.
    # Creation of processes has to be handled differently on different
    # platforms.
    if ( ( $osname eq 'linux' ) or ( $osname eq 'solaris' ) or ( $osname eq 'aix' ) or ( $osname eq 'hpux' ) ) {
	$outcome = exec_plugins_unix( $hostname, \@hostlist, $nchecks );
    }
    elsif ( $osname eq 'MSWin32' ) {
	$outcome = exec_plugins_windows( $hostname, \@hostlist, $nchecks, $sleep_time );
    }

    return $outcome;
}

################################################################################
#
#   exec_plugins_windows()
#
#   Executes plugins on all the hosts in parallel, by invoking gdma_run_checks
#   program for each host, on windows.  Makes sure that at the most
#   "Max_Concurrent_Hosts" instances of gdma_run_checks.pl are running at any
#   given time.  Returns 1 on success and 0 on failure.
#
#   Arguments:
#   $hostlist - A reference to an array of hosts to be processed.
#   $num_checks - Reference to a variable where the number of checks performed on
#                 all hosts together are to be recorded.
#   $total_sleep_time - Reference to the total time for which poller waits for an
#              instance to complete the check execution
#
################################################################################
sub exec_plugins_windows {
    my $hostname         = shift;
    my $hostlist         = shift;
    my $num_checks       = shift;
    my $total_sleep_time = shift;

    # default hard-coded sleep interval is 1 second
    my $sleep_interval = defined( $g_config{Sleep_Interval} ) ? $g_config{Sleep_Interval} : 1;

    # Holds process objects created.
    my @Processobj;
    my $obj;
    my $i = 0;

    # Indicates whether a new instance of gdma_run_check can be invoked.
    my $slot_available = 1;
    my $headpath       = GDMA::Utils::get_headpath();

    # Arguments to gdma_run_checks.pl program.
    my $proc_args;
    my $exitcode;
    my $plugin_dir = $g_config{Poller_Plugin_Directory};
    my $num_checks_file;
    my $num_checks_fh;

    $$num_checks = 0;

    # If the configured plugin directory path has a trailing backslash, remove it.
    # It generates an error when passed to gdma_run_checks.pl
    #
    # FIX LATER:  I suspect the problem is that we are quoting the arguments and passing
    # them as part of a complete command-line string.  And in that context, the trailing
    # backslash bumps up against the trailing quote of that argument, presumably
    # causing that quote to be interpreted as part of the argument and throwing off the
    # interpretation of the rest of the command string.  Possibly better would be to just
    # double all the backslashes in the value, and see if they are then collapsed back to
    # single backslashes when the command-line arguments are processed.  If that works,
    # there would be no confusion about the trailing quote.  Testing is needed.
    $plugin_dir =~ s/\\$//;

    # For each host create an instance of gdma_run_checks.pl with the hostname
    # as an argument, as long as total number instances is less than
    # Max_Concurrent_Hosts.  When the number of instances running become
    # equal to Max_Concurrent_Hosts, wait for an instance to complete.
    $$total_sleep_time = 0;
    while ( defined( $$hostlist[$i] ) ) {
	if ( scalar(@Processobj) < $g_config{Max_Concurrent_Hosts} ) {
	    ## We are yet to invoke Max_Concurrent_Hosts instances.
	    $slot_available = 1;
	}
	else {
	    my $count = 0;

	    # FIX MINOR:  Isn't there some way to just block until the first child process dies,
	    # rather than sleeping a fixed time period here to avoid having the parent spin?

	    # update sleep time
	    $$total_sleep_time += $sleep_interval;
	    sleep $sleep_interval;

	    # Get a count of all the instances still running.
	    for my $obj (@Processobj) {
		$obj->GetExitCode($exitcode);
		$count++ if ( $exitcode == STILL_ACTIVE() );
	    }

	    # We have a slot for one more instance.
	    $slot_available = 1 if ( $count < $g_config{Max_Concurrent_Hosts} );
	}

	if ( $slot_available == 1 ) {
	    ## Slot available.  Create a new instance for the next host to be processed.
	    $proc_args = "-H \"$$hostlist[$i]\"";
	    $proc_args .= " -f" if $$hostlist[$i] ne $hostname;
	    $proc_args .= " -T $g_config{Poller_Plugin_Timeout} -p \"$headpath\"";
	    $proc_args .= " -s \"$g_config{Poller_Service}\"";
	    $proc_args .= " -L \"$plugin_dir\"";
	    if ( defined $g_config{Enable_Local_Logging} and ( $g_config{Enable_Local_Logging} =~ /^on$/i ) ) {
		$proc_args .= " -l -D \"$headpath\\log\"";
	    }

	    if ( $g_opt{d} ) {
		$proc_args .= " -d $g_opt{d}";
	    }
	    if ( $g_opt{i} ) {
		$proc_args .= " -i ";
	    }

	    $logger->info("INFO:  Executing gdma_run_checks; arguments are:  $proc_args") if $g_opt{d};

	    if (
		not Win32::Process::Create(
		    $obj,
		    "$headpath\\bin\\gdma_run_checks.exe",
		    "gdma_run_checks $proc_args",
		    0, NORMAL_PRIORITY_CLASS() | CREATE_NO_WINDOW(), "."
		)
	      )
	    {
		$logger->error( "ERROR:  exec_plugins_windows:" . Win32::FormatMessage( Win32::GetLastError() ) );
		return 0;
	    }
	    push @Processobj, $obj;
	    $i++;
	}
	$slot_available = 0;
    }

    # Wait for all the processes to terminate.
    foreach $obj (@Processobj) {
	$obj->Wait( INFINITE() );
    }

    # All the checks have been run.  Calculate the total number of checks
    # executed successfully.  This number will be used for heartbeat message.
    # gdma_run_check program writes the number of check results spooled, into
    # a file.  There will be a num checks file for each hosts.  Add the numbers
    # for all the hosts to get the total number of checks in this iteration.
    foreach my $host (@$hostlist) {
	$num_checks_file = "$headpath\\tmp\\$host" . "_checks.txt";
	if ( open $num_checks_fh, '<', $num_checks_file ) {
	    my $nchecks = <$num_checks_fh>;
	    close($num_checks_fh);
	    chomp $nchecks;
	    $$num_checks += $nchecks;
	}
	else {
	    $logger->error("ERROR:  Failed to read the number of checks executed for host $host");

	    # Report 0 rather than a wrong value.
	    $$num_checks = 0;
	    return 0;
	}
    }

    return 1;
}

################################################################################
#
#   exec_plugins_unix()
#
#   Executes plugins on all the hosts in parallel, by invoking gdma_run_checks
#   program for each host, on linux/solaris/aix/hpux.  Makes sure that at the
#   most "Max_Concurrent_Hosts" instances of gdma_run_checks.pl are running at
#   any given time.  Returns 1 on success and 0 on failure.
#
#   Arguments:
#   $hostlist - A reference to an array of hosts to be processed.
#   $num_checks - Reference to a variable where the number of checks performed on
#                 all hosts together are to be recorded.
#
################################################################################
sub exec_plugins_unix {
    my $hostname   = shift;
    my $hostlist   = shift;
    my $num_checks = shift;
    my $i          = 0;

    my $headpath = GDMA::Utils::get_headpath();

    # Arguments to gdma_run_checks.pl program.
    my @proc_args;
    my %childprocs;
    my $plugin_dir = $g_config{Poller_Plugin_Directory};
    my $num_checks_file;
    my $num_checks_fh;
    my $kidpid;

    $$num_checks = 0;

    # If the configured plugin directory path has a trailing backslash, remove it.
    # It generates an error when passed to gdma_run_checks.pl
    #
    # FIX LATER:  Just drop this after testing.  This used to be true because we were
    # quoting the arguments and passing them as part of a complete command-line string.
    # In that context, the trailing backslash bumped up against the trailing quote of that
    # argument, causing that quote to be interpreted as part of the argument and throwing
    # off the interpretation of the rest of the command string.  Better would have been to
    # just double all the backslashes in the value, so the intermediate shell would then
    # have collapsed them back to single backslashes when it processed the command-line
    # arguments, and there would be no confusion about the trailing quote.  But now that
    # we are completely sidestepping an intermediate shell and forking the subsidiary
    # process directly, we're no longer using quoted arguments, and no such adjustment is
    # needed.
    # $plugin_dir =~ s/\\$//;

    # For each host create an instance of gdma_run_checks.pl with the hostname
    # as an argument, as long as total number instances is less than
    # Max_Concurrent_Hosts.  When the number of instances running becomes
    # equal to Max_Concurrent_Hosts, wait for an instance to complete.
    while ( defined( $$hostlist[$i] ) ) {
	my $waitpid_flags = WNOHANG;
	if ( scalar( keys %childprocs ) < $g_config{Max_Concurrent_Hosts} ) {
	    ## The number of live instances of gdma_run_checks is less than
	    ## "Max_Concurrent_Hosts".  We are good to spawn more children.
	    ## Create a new instance for the next host to be processed.
	    @proc_args = ( "-H", $$hostlist[$i] );
	    push @proc_args, "-f" if $$hostlist[$i] ne $hostname;
	    push @proc_args, "-T", $g_config{Poller_Plugin_Timeout}, "-p", $headpath;
	    push @proc_args, "-s", $g_config{Poller_Service};
	    push @proc_args, "-L", $plugin_dir;
	    if ( defined( $g_config{Enable_Local_Logging} ) and ( $g_config{Enable_Local_Logging} =~ /^on$/i ) ) {
		push @proc_args, "-l", "-D", "$headpath/log";
	    }
	    if ( $g_opt{d} ) {
		push @proc_args, "-d", $g_opt{d};
	    }
	    if ( $g_opt{i} ) {
		push @proc_args, "-i";
	    }

	    # Spawn a new process.
	    if ( !defined( $kidpid = fork() ) ) {
		## Catastrophic.  Bail out.
		$logger->error("ERROR:  Failed to spawn a process to execute plugins:  $!");
		## Possibly, the failure might be due to an excessive number of processes (and their
		## respective system resources) in the system.  Let's do our bit to clean up any of
		## our own zombies before we abandon any of our still-running child processes.  Any
		## still-active children will be waited for and cleaned up in some future cycle.
		do {
		    $kidpid = waitpid( -1, WNOHANG );
		} while $kidpid > 0;
		return 0;
	    }
	    elsif ( $kidpid == 0 ) {
		## Child.  Exec gdma_run_checks.
		$logger->debug("DEBUG:  Executing gdma_run_checks; arguments are:  " . join( ' ', @proc_args ) );
		do { exec( "$headpath/bin/gdma_run_checks.pl", @proc_args ); };

		# exec() should never return.  But just in case ...
		$logger->error("ERROR:  Failed to exec gdma_run_checks program:  $!");
		POSIX::_exit 1;
	    }
	    else {
		## Parent.  We spawned a child.  Update the record.
		$childprocs{$kidpid} = 1;
		$i++;
	    }
	}
	else {
	    ## There is no slot available, so we may as well do a blocking wait for the first
	    ## child process to die, so we don't spin wasting resources in the parent process.
	    $waitpid_flags = 0;
	}

	# Check if there are any child processes waiting to be reaped.
	# We want to reap all such processes as soon as they are available
	# to be reaped, to minimize outstanding system resources.
	while (1) {
	    ## Make a blocking or non-blocking call, as needed.
	    $kidpid = waitpid( -1, $waitpid_flags );
	    last if $kidpid <= 0;
	    ## A child terminated.  Remove its record.
	    if ( exists $childprocs{$kidpid} ) {
		delete $childprocs{$kidpid};
	    }
	    else {
		## Unlikely, but worth a log.  Could be from a previous call to this routine,
		## where we returned early without waiting for all our children to exit.
		$logger->warn("WARNING:  Found a child process we did not spawn.");
	    }
	    ## Further waiting in this cycle should be non-blocking.
	    $waitpid_flags = WNOHANG;
	}
    }

    # Wait for all the processes to terminate.  We do a blocking wait here,
    # as we have nothing better to do.
    do {
	$kidpid = wait();
    } while $kidpid > 0;

    # All the checks have been run.  Calculate the total number of checks
    # executed successfully.  This number will be used for heartbeat message.
    # gdma_run_check program writes the number of check results spooled, into
    # a file.  There will be a num checks file for each hosts.  Add the numbers
    # for all the hosts to get the total number of checks in this iteration.
    foreach my $host (@$hostlist) {
	$num_checks_file = "$headpath/tmp/$host" . "_checks.txt";
	if ( open $num_checks_fh, '<', $num_checks_file ) {
	    my $nchecks = <$num_checks_fh>;
	    close($num_checks_fh);
	    chomp $nchecks;
	    $$num_checks += $nchecks;
	}
	else {
	    $logger->error("ERROR:  Failed to read the number of checks executed for host $host");

	    # Report 0 rather than a wrong value.
	    $$num_checks = 0;
	    return 0;
	}
    }

    return 1;
}

################################################################################
#
#   set_environment()
#
#   Set up perhaps PATH and LD_LIBRARY_PATH, and other environment variables
#   needed by this and descendant processes, based on platform information
#   passed in to this routine.
#
#   NAGIOS_PLUGIN_LOCALE_DIRECTORY and NAGIOS_PLUGIN_STATE_DIRECTORY are
#   needed to support the Nagios plugins.
#
#   Arguments:
#   $my_headpath - Head path for the agent.
#
################################################################################
sub set_environment {
    my $my_headpath = shift;
    my $my_osname   = $^O;

    if ( ( $my_osname eq 'linux' ) or ( $my_osname eq 'aix' ) ) {
	$ENV{NAGIOS_PLUGIN_LOCALE_DIRECTORY} = '/usr/local/groundwork/nagios/share/locale';
	$ENV{NAGIOS_PLUGIN_STATE_DIRECTORY}  = '/usr/local/groundwork/nagios/var';
    }
    elsif ( ( $my_osname eq 'solaris' ) or ( $my_osname eq 'hpux' ) ) {
	$ENV{NAGIOS_PLUGIN_LOCALE_DIRECTORY} = '/opt/groundwork/nagios/share/locale';
	$ENV{NAGIOS_PLUGIN_STATE_DIRECTORY}  = '/opt/groundwork/nagios/var';
    }
    elsif ( $my_osname eq 'MSWin32' ) {
	## Typical values, though these can be relocated at install time:
	## GDMA_BASE_DIR = "C:\Program Files\groundwork\gdma"		(on 32-bit Windows)
	## GDMA_BASE_DIR = "C:\Program Files (x86)\groundwork\gdma"	(on 64-bit Windows)

	if ( defined $ENV{GDMA_BASE_DIR} ) {
	    $ENV{NAGIOS_PLUGIN_LOCALE_DIRECTORY} = $ENV{GDMA_BASE_DIR} . '\\libexec\\nagios\\share\\locale';
	    $ENV{NAGIOS_PLUGIN_STATE_DIRECTORY}  = $ENV{GDMA_BASE_DIR} . '\\libexec\\nagios\\var';
	}
	elsif ( defined $PerlApp::RUNLIB ) {
	    $ENV{NAGIOS_PLUGIN_LOCALE_DIRECTORY} = $PerlApp::RUNLIB . '\\libexec\\nagios\\share\\locale';
	    $ENV{NAGIOS_PLUGIN_STATE_DIRECTORY}  = $PerlApp::RUNLIB . '\\libexec\\nagios\\var';
	}

	# The following setting of the CYGWIN environment variable is here to
	# suppress the following message, e.g., from the check_disk.exe plugin:
	#
	# cygwin warning:
	#   MS-DOS style path detected: c:\
	#   Preferred POSIX equivalent is: /cygdrive/c
	#   CYGWIN environment variable option "nodosfilewarning" turns off this warning.
	#   Consult the user's guide for more details about POSIX paths:
	#     http://cygwin.com/cygwin-ug-net/using.html#using-pathnames
	#
	# See also:
	#     http://cygwin.com/cygwin-ug-net/ov-new1.7.html
	# for the setting of this variable.
	$ENV{CYGWIN} = 'nodosfilewarning';
    }
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

The GDMA poller agent monitors system statistics on this server,
and dumps the results to a spool file.

Options:
-c <CONFIG FILE>    Config file containing monitoring parameters.
-a <AUTOCONF FILE>  File with default settings.  Must contain a target server address.
-l <LOG DIR>        Full path for log directory for this script.
-d <DEBUGLEVEL>     Debug mode.  Will log additional messages to the log file;
		    <DEBUGLEVEL> should be 1 for moderate logging, or 2 for additional detail.
-h                  Displays help message.
-x                  Run once.  If this option is not selected, run continually with sleep.
-i                  Run interactively -- shows output to the Command Line Interface
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

################################################################################
#
#   insert_into_result_buf()
#
#   Inserts the message passed as a result into the global buffer.
#   Assumes the default value for retries field.
#
#   Arguments:
#   $target - The target host for the result.
#   $host - The hostname for which this is a result.
#   $service - The service name for which this is a result.
#   $ret_code - Return code for the results.
#   $msg_body - The message text.
#   $result_buf - A reference to a buffer containing results.
#
################################################################################
sub insert_into_result_buf {
    my ( $target, $host, $service, $ret_code, $msg_body, $result_buf ) = @_;

    my $debug = defined( $g_opt{d} ) ? $g_opt{d} : 0;

    # The retries field should be 0, when the result is first spooled.
    my $default_retries = 0;

    # Use default value for retries.
    my $result_str =
      join( '', $default_retries, "\t", $target, "\t", time(), "\t", $host, "\t", $service, "\t", $ret_code, "\t", $msg_body, "\n" );

    # Push it into result buffer.
    push( @$result_buf, $result_str );
    chomp $result_str;
    $logging->log_message("DEBUG:  Pushed result into the buffer:\n$result_str") if $debug;
}

################################################################################
#
#   install_autoconfig_filepaths()
#
#   Sets up the autoconfig file paths.
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
    if ( ( $my_osname eq 'solaris' ) or ( $my_osname eq 'linux' ) or ( $my_osname eq 'aix' ) or ( $my_osname eq 'hpux' ) ) {
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
#   Sets up the hostconfig file paths.
#
#   Arguments:
#   $my_headpath - Head path for the agent.
#   $Use_Long_Hostname - The configured Use_Long_Hostname value, if any.
#   $Forced_Hostname  - The configured Forced_Hostname value, if any.
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
    my $hostconfigfile         = "NULL";
    my $hostname               = GDMA::Utils::my_hostname( $Use_Long_Hostname, $Forced_Hostname, $Use_Lowercase_Hostname );
    my $my_osname              = $^O;

    # Set the filenames based on the operating system.
    if ( ( $my_osname eq 'solaris' ) or ( $my_osname eq 'linux' ) or ( $my_osname eq 'aix' ) or ( $my_osname eq 'hpux' ) ) {
	$hostconfigfile = "$my_headpath/config/gwmon_$hostname" . ".cfg";
    }
    elsif ( $my_osname eq 'MSWin32' ) {
	$hostconfigfile = "$my_headpath\\config\\gwmon_$hostname" . ".cfg";
    }

    return $hostconfigfile;
}

__END__

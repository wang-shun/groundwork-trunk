#!/usr/local/groundwork/perl/bin/perl -w --

=pod

=head1 NeDi Feeder

Cleanup with:
/usr/local/groundwork/postgresql/bin/psql -c "SELECT * FROM servicestatus WHERE servicedescription ~ 'NeDi'" gwcollagedb;
/usr/local/groundwork/postgresql/bin/psql -c "DELETE FROM servicestatus WHERE servicedescription = 'NeDi'" gwcollagedb;

=head2 AUTHORS

Remo Rickli

=cut

use strict;
use warnings;

use Getopt::Std;
use POSIX qw(strftime);
use File::Basename;
use Log::Log4perl;    # For logging from the GW::RAPID package.
use TypedConfig;
use GW::Logger;
use GW::RAPID;

use Data::Dumper;
$Data::Dumper::Indent   = 1;
$Data::Dumper::Sortkeys = 1;

use lib '/usr/local/groundwork/core/foundation/api/perl/lib';
use lib '/usr/local/groundwork/nedi/inc';

# ================================
# Package Parameters
# ================================

my $PROGNAME       = "nedi_feeder.pl";
my $VERSION        = "0.1.3";
my $COPYRIGHT_YEAR = "2016";

use vars qw($p $now $lchk $nchk $rest_api %opt);

# We don't use a colon in the prefix, because such a character is bound to cause
# problems down the road once we start parsing IPv6 addresses.  Yes, this prefix
# is for service names, not hostnames, but there would be confusion nonetheless.
# The same goes for any shell metacharacters, since this prefix will be used to
# construct service names, and those names may be used in shell context.
our $prefix = 'NeDi_';

my $default_config_file = '/usr/local/groundwork/config/nedi_feeder.conf';

# ================================
# Command-Line Parameters
# ================================

# In theory, these parameter settings could be overridden by command-line arguments.
# In practice, we don't currently support any such arguments; this script uses only
# a fixed set of arguments.

my $config_file  = $default_config_file;
my $debug_config = 0;                      # if set, spill out certain data about config-file processing to STDOUT

my $run_interactively     = 0;
my $reflect_log_to_stdout = 0;

# ================================
# Configuration Parameters
# ================================

# Parameters in the config file.

my $enable_processing     = undef;

my $logfile                = undef;
my $max_logfile_size       = undef;    # log rotate is handled externally, not here
my $max_logfiles_to_retain = undef;    # log rotate is handled externally, not here

my $rest_api_requestor    = undef;
my $ws_client_config_file = undef;
my $GW_RAPID_log_level    = undef;
my $log4perl_config       = undef;

# ================================
# Working Variables
# ================================

my $logger             = undef;
my $shutdown_requested = 0;

use constant ERROR_STATUS    => 0;
use constant STOP_STATUS     => 1;
use constant RESTART_STATUS  => 2;
use constant CONTINUE_STATUS => 3;

# ================================================================
# Program.
# ================================================================

exit ((main() == ERROR_STATUS) ? 1 : 0);

# To be kind to the server and always disconnect our session, we attempt to force a shutdown
# of the REST API before global destruction sets in and makes it impossible to log out.
END {
    terminate_rest_api();
}

# ================================================================
# Supporting subroutines.
# ================================================================

sub main {
    my @SAVED_ARGV = @ARGV;

    my $process_outcome = 1;

    if (open (STDERR, '>>&STDOUT')) {
	## Apparently, appending STDERR to the STDOUT stream isn't by itself enough
	## to get the line disciplines of STDOUT and STDERR synchronized and their
	## respective messages appearing in order as produced.  The combination is
	## apparently happening at the file-descriptor level, not at the level of
	## Perl's i/o buffering.  So it's still possible to have their respective
	## output streams inappropriately interleaved, brought on by buffering of
	## STDOUT messages.  To prevent that, we need to have STDOUT use the same
	## buffering as STDERR, namely to flush every line as soon as it is produced.
	## This is certainly a less-efficient use of system resources, but we don't
	## expect this program to write much to the STDOUT stream anyway.
	STDOUT->autoflush(1);
    }
    else {
	print "ERROR:  STDERR cannot be redirected to STDOUT!\n";
	return ERROR_STATUS;
    }

    getopts( 'Dd:vp:U:', \%opt ) || &HELP_MESSAGE;
    if ( !defined $opt{'d'} ) { $opt{'d'} = '' }    # Avoid warnings if unused

    if (not read_config_file($config_file, $debug_config)) {
	spill_message "FATAL:  $PROGNAME cannot load configuration from $config_file";
	return ERROR_STATUS;
    }

    # Stop if this is just a debugging run.
    return STOP_STATUS if $debug_config;

    # We need to prohibit executing as root (say, for a manual debugging run), so we
    # don't create files and directories that won't be modifiable later on when this
    # script is run in its usual mode as an ordinary user ("nagios").  We purposely
    # delay this test until after simple actions of the script, so we can at least
    # show the version and command-usage messages without difficulty.
    if ($> == 0) {
	(my $program = $0) =~ s<.*/><>;
	print "ERROR:  You cannot run $program as root.\n";
	return ERROR_STATUS;
    }

    # We use a message prefix because multiple concurrent copies of this script may be writing to
    # the log file (not in normal operation via daily cron job, but potentially if manual executions
    # are also run occasionally), and we need a means to disambiguate where each message comes from.
    GW::Logger->new( $logfile, $run_interactively, $reflect_log_to_stdout, $max_logfile_size, $max_logfiles_to_retain, '' );

    if ( !open_logfile() ) {
	## The routine will print an error message if it fails, so we don't do so ourselves.
	return ERROR_STATUS;
    }

    log_message '';
    log_timed_message "=== NeDi Feeder script (version $VERSION) starting up (process $$). ===";
    log_timed_message "INFO:  Running " . ( @SAVED_ARGV ? "with options:  " . join( ' ', @SAVED_ARGV ) : 'without any command-line options.' );

    if ( not $enable_processing ) {
	## Nothing to do here.  We assume this configuration is intentional; staying up is the right
	## choice so "service groundwork status gwservices" doesn't think this component is broken.
	log_timed_message "Sleeping forever ...";
	eval {
	    ## local $SIG{INT}  = \&die_on_exit_signal;
	    ## local $SIG{QUIT} = \&die_on_exit_signal;
	    ## local $SIG{TERM} = \&die_on_exit_signal;
	    die "Shutdown requested.\n" if $shutdown_requested;    # handle race condition
	    sleep 100_000_000;
	};
	if ($@) {
	    chomp $@;
	    log_timed_message $@;
	}
	log_timed_message "Exiting.";
	exit 1;
    }

    select(STDOUT);
    $| = 1;                                         # Disable buffering

    # $p = $0;					# Guess nedi path
    # $p =~ s/(.*)\/(.*)/$1/;
    # if($0 eq $p){$p = "."};
    $p = '/usr/local/groundwork/nedi';

    $misc::dbname = $misc::dbhost = $misc::dbuser = $misc::dbpass = '';

    require "libmisc.pm";                           # Use the miscellaneous nedi library
    require "libmon.pm";                            # Use the Monitoring lib for notifications
    require "libdb.pm";                             # Use the DB library

    if ( $opt{'D'} ) {                              # Put in background
	misc::Daemonize();
    }

    if (not initialize_rest_api()) {
	## The routine will print an error message if it fails, so we don't do so ourselves.
	return ERROR_STATUS;
    }

    my $i    = 0;
    my $dvup = 0;
    while (1) {                                     # Loop forever
	$now = time;                                # Use defined timestamp for each run
	misc::ReadConf( $main::opt{'U'} );
	$lchk = strftime( '%Y-%m-%dT%H:%M:%S%z', localtime(time) );                     # last check
	$nchk = strftime( '%Y-%m-%dT%H:%M:%S%z', localtime( time + $misc::pause ) );    # next check

	# initialize NeDi DB connection
	db::Connect( $misc::dbname, $misc::dbhost, $misc::dbuser, $misc::dbpass, 1 );

	$logger->info("  Updating GWOS $lchk ($i)");
	misc::Prt("\nUpdating GWOS $lchk ($i)\n");
	misc::Prt("===============================================================================\n");

	# The "NEDI" application type is now a permanent, predefined part of our GWMEE 7.1.1 database,
	# so there's no longer any reason to attempt to add it here.
	# UPapptype( 'NEDI', 'NeDi Application Feed' ) unless $i;

	UPsrv( 'localhost', '127.0.0.1', 'OK', $prefix . 'Feeder', 'Updated by nedi_feeder iteration ' . $i );

	$logger->info("  Processing traffic policies:");
	misc::Prt("Processing traffic policies:\n");

	# Get NeDi services from GWOS
	my $gwsv = GETsrv("appType = 'NEDI'");

	# Feed NeDi traffic policies and resulting events as services on localhost
	my %cursv;
	my $nfp = db::Select( 'policies', '', '*', "class='byt' or class='pkt' or class='fl'" );
	if ($nfp) {
	    foreach my $p (@$nfp) {
		## We don't intentionally use parentheses or other shell metacharacters in service names because
		## they're bound to cause problems down the road when such names get used in shell contexts.
		my $svde = 'Traffic_' . $p->[13] . "_$p->[0]";
		$cursv{ $prefix . $svde } = 1;
		my $pev = db::Select( 'events', '', 'id', "time>$now-300 and class='sptr' and source='$p->[0]'" );
		UPsrv( 'localhost', '127.0.0.1', $pev ? 'CRITICAL' : 'OK', $prefix . $svde, $p->[6] );
		ADDevent( 'localhost', 'CRITICAL', 'SERIOUS', $prefix . 'Traffic' . $p->[0], $svde ) if $pev;
	    }
	}

	# Delete obsolete services, if no policy exists
	foreach my $g ( keys %$gwsv ) {
	    my $svm = $prefix . 'Traffic';
	    if ( $gwsv->{$g}{'description'} =~ /^$svm/ and !exists $cursv{ $gwsv->{$g}{'description'} } ) {
		DELsrv( 'localhost', $gwsv->{$g}{'description'} );
	    }
	}

	if ( $now - $misc::rrdstep > $dvup ) {
	    $logger->info("  Processing devices:");
	    misc::Prt("Processing devices:\n");

	    # Get NeDi services from GWOS
	    my $gwho = GEThost("appType = 'NEDI'");

	    # Feed NeDi devices as hosts in their group
	    my %curdv;
	    my $dvs = db::Select( 'devices', 'device' );

	    # TODO combine with monitoring? my $dvs = db::Select('devices','device','devices.*,latency','','monitoring','device');
	    foreach my $d ( keys %$dvs ) {
		my $ip = misc::Dec2Ip( $dvs->{$d}{'devip'} );
		my $dstat = $dvs->{$d}{'lastdis'} > $now - $misc::rrdstep ? 'UP' : 'UNREACHABLE';
		UPhost( $d, $ip, $dstat, $dvs->{$d}{'description'}, 'Updated by nedi_feeder iteration ' . $i, 'nedigroup' );
		## We don't intentionally use spaces or other shell metacharacters in service names because
		## they're bound to cause problems down the road when such names get used in shell contexts.
		UPsrv(
		    $d, $ip,
		    $dvs->{$d}{'cpu'} > $misc::cpua ? 'CRITICAL' : 'OK',
		    $prefix . 'System_Load',
		    "$dvs->{$d}{'cpu'}%, threshold $misc::cpua"
		) if substr( $dvs->{$d}{'devopts'}, 1, 1 ) ne '-';
		ADDevent( $d, 'CRITICAL', 'SERIOUS', $prefix . 'System_Load', "$dvs->{$d}{'cpu'}%, threshold $misc::cpua" )
		  if $dvs->{$d}{'cpu'} > $misc::cpua;

		# TODO for NeDi 1.7, where proper thresholds are available
		# UPsrv( $d, $ip, $dvs->{$d}{'memcpu'} > $misc::mema ? 'CRITICAL' : 'OK', $prefix . 'Memory_Free',
		#     "$dvs->{$d}{'memcpu'}%, threshold $misc::mema" );
		UPsrv(
		    $d, $ip,
		    $dvs->{$d}{'temp'} > $misc::tmpa ? 'CRITICAL' : 'OK',
		    $prefix . 'Temperature',
		    "$dvs->{$d}{'temp'}C, threshold $misc::tmpa"
		) if $dvs->{$d}{'temp'};
		ADDevent( $d, 'CRITICAL', 'SERIOUS', $prefix . 'Temperature', "$dvs->{$d}{'temp'}C, threshold $misc::tmpa" )
		  if $dvs->{$d}{'temp'} > $misc::tmpa;
	    }

	    # Delete obsolete hosts, if no device exists
	    foreach my $d ( keys %$gwho ) {
		if ( !exists $dvs->{ $gwho->{$d}{'hostName'} } ) {
		    DELhost($d);
		}
	    }
	    $dvup = $now;
	}

	misc::Prt("===============================================================================\n");
	my $took = time - $now;
	if ( $misc::pause > $took ) {
	    my $sl = $misc::pause - $took;
	    misc::Prt("Took ${took}s, sleeping ${sl}s\n\n");
	    db::Disconnect();    # Disconnect DB before sleep
	    $logger->info("Took ${took}s, sleeping ${sl}s");
	    my $slept = sleep($sl);
	}
	else {
	    db::Insert(
		'events',
		'level,time,source,class,device,info',
		"150,$now,'NeDi','bugn','','Feeder§ took ${took}s, increase pause!'"
	    );
	    misc::Prt("Took ${took}s, no time to pause!\n\n");
	    db::Disconnect();
	}
	$i++;
    }

    terminate_rest_api();

    close_logfile();

    return $process_outcome ? STOP_STATUS : ERROR_STATUS;
}

sub read_config_file {
    my $config_file  = shift;
    my $config_debug = shift;

    # All the config-file processing is wrapped in an eval{}; because TypedConfig
    # throws exceptions when it cannot open the config file or finds bad config data.
    eval {
	my $config = TypedConfig->new( $config_file, $config_debug );

	$enable_processing     = $config->get_boolean('enable_processing');
	$logfile               = $config->get_scalar('logfile');
	$rest_api_requestor    = $config->get_scalar('rest_api_requestor');
	$ws_client_config_file = $config->get_scalar('ws_client_config_file');
	$GW_RAPID_log_level    = $config->get_scalar('GW_RAPID_log_level');
	$log4perl_config       = $config->get_scalar('log4perl_config');

	# Security constraint.  There doesn't seem to be a way to outlaw this through the
	# Log::Log4perl package itself, so we must do so at the application level.
	if ($log4perl_config =~ /^(ldap|https?|ftp|wais|gopher|file):/i) {
	    die "Reading Log::Log4perl configuration from a URL is not supported.\n";
	}
    };
    if ($@) {
	chomp $@;
	$@ =~ s/^ERROR:\s+//i;
	print "ERROR:  Cannot read config file $config_file\n  ($@).\n";
	return 0;
    }

    return 1;
}

sub initialize_rest_api {

    # Basic security:  disallow code in the logging config data.
    Log::Log4perl::Config->allow_code(0);

    # Here we add custom logging levels to form our full standard complement.  There are six
    # predefined log levels:  FATAL, ERROR, WARN, INFO, DEBUG, and TRACE (in descending priority).
    # We add NOTICE and STATS levels to the default set of logging levels supplied by Log4perl,
    # to form the full useful set:  FATAL, ERROR, WARN, NOTICE, STATS, INFO, DEBUG, and TRACE
    # (excepting NONE, I suppose, though there is some hint in the code that OFF is also supported).
    # This *must* be done before the call to Log::Log4perl::init().
    Log::Log4perl::Logger::create_custom_level("NOTICE", "WARN");
    Log::Log4perl::Logger::create_custom_level("STATS", "NOTICE");

    # If we wanted to support logging either through a syslog appender (I'm not sure how this would
    # be done; presumably via something other than Log::Dispatch::Syslog, since that is still
    # Log::Dispatch) or through Log::Dispatch, the following code extensions would come in handy.
    # (Frankly, I'm not really sure that Log4perl even supports syslog logging other than through
    # Log::Log4perl::JavaMap::SyslogAppender, which just wraps Log::Dispatch::Syslog.)
    #
    # use Sys::Syslog qw(:macros);
    # use Log::Dispatch;
    # my $log_null = Log::Dispatch->new( outputs => [ [ 'Null', min_level => 'debug' ] ] );
    # Log::Log4perl::Logger::create_custom_level("NOTICE", "WARN", LOG_NOTICE, $log_null->_level_as_number('notice'));
    # Log::Log4perl::Logger::create_custom_level("STATS", "NOTICE", LOG_INFO, $log_null->_level_as_number('info'));

    # This logging setup is an application-global initialization for the Log::Log4perl package, so
    # it only makes sense to initialize it at the application level, not in some lower-level package.
    #
    # It's not documented, but apparently Log::Log4perl::init() always returns 1, even if
    # it is handed a garbage configuration as a literal string.  That makes it hard to tell
    # if you really have it configured correctly.  On the other hand, if it's handed the
    # path to a missing config file, it throws an exception (also undocumented).
    eval {
	## If the value starts with a leading slash, we interpret it as an absolute path to a file that
	## contains the logging configuration data.  Otherwise, we interpret it as the data itself.
	Log::Log4perl::init( $log4perl_config =~ m{^/} ? $log4perl_config : \$log4perl_config );
    };
    if ($@) {
	chomp $@;
	log_timed_message "ERROR:  Could not initialize Log::Log4perl logging:\n$@";
	return 0;
    }

    # Initialize the REST API object.
    $logger = Log::Log4perl::get_logger("NeDi.Feeder.GW.RAPID");
    if ( not defined $logger ) {
	log_timed_message "ERROR:  Could not initialize logger";
	return 0;
    }

    my %rest_api_options = (
	logger => $logger,
	access => $ws_client_config_file
    );
    $rest_api = GW::RAPID->new( undef, undef, undef, undef, $rest_api_requestor, \%rest_api_options );
    if ( not defined $rest_api ) {
	## The GW::RAPID constructor doesn't directly return any information to the caller on the reason for
	## a failure.  But it will already have used the logger handle to write such detail into the logfile.
	log_timed_message "ERROR:  Could not create a GW::RAPID object.";
	return 0;
    }

    return 1;
}

sub terminate_rest_api {
    ## Release our handle to the REST API (if we used it), to force the REST API to call its destructor.
    ## This will attempt to log out before Perl's global destruction pass wipes out resources needed for
    ## logout to work properly.
    $rest_api = undef;
}

=head2 FUNCTION UPapptype()

Update host

B<Options>

B<Globals> -

B<Returns> -

=cut
sub UPapptype {
    my ( $na, $de ) = @_;

    my ( %outcome, @results ) = ();
    my @application_types = (
	{
	    'name'                    => $na,
	    'description'             => $de,
	    'stateTransitionCriteria' => 'Device;Host',
	}
    );
    my $res = $rest_api->upsert_application_types( \@application_types, {}, \%outcome, \@results );

    misc::Prt("UAPP:$na $de = $res\n");

    return $res;
}

=head2 FUNCTION UPhost()

Update host

B<Options>

B<Globals> -

B<Returns> -

=cut
sub UPhost {
    my ( $dv, $ip, $st, $de, $cmt, $gr ) = @_;

    my ( %outcome, @results ) = ();

    my %properties = ();

    # FIX MAJOR:  Upon first host creation, we need to send in a full panoply of data
    # to describe the new host.  But after that, we don't need to be sending in fields
    # whose values have not changed, aside from those that identify the particular host
    # or fields being updated.  That is just a pure waste of system resources.
    if (1) {
	$properties{Latency}   = 125;
	$properties{UpdatedBy} = 'admin';
	$properties{Comments}  = $cmt;
    }

    # FIX MAJOR:  The LastStateChange value MUST be set to a reasonable timestamp
    # value when the host is first created.  But we should ONLY be sending in and
    # updating this value if this is the first time this host is created or if the
    # state actually changed, not continually setting it to the current time.
    $properties{LastStateChange} = $lchk if 1;

    # FIX MAJOR:  Upon first host creation, we need to send in a full panoply of data
    # to describe the new host.  But after that, we don't need to be sending in fields
    # whose values have not changed, aside from those that identify the particular host
    # or fields being updated.  That is just a pure waste of system resources.
    my %host = (
	"hostName"             => $dv,
	"description"          => $de,
	"deviceIdentification" => $ip,
	"deviceDisplayName"    => $dv,
	"lastCheckTime"        => $lchk,
	"nextCheckTime"        => $nchk,
	"monitorStatus"        => $st,
	'stateType'            => 'HARD',
	"appType"              => "NEDI",
	"monitorServer"        => "localhost",
	"checkType"            => 'ACTIVE'
    );
    $host{properties} = \%properties if %properties;
    my @hosts = ( \%host );
    my $res = $rest_api->upsert_hosts( \@hosts, {}, \%outcome, \@results );
    $logger->debug( "\\\@results:\n", Dumper \@results );
    misc::Prt("UHST:$dv,$ip $st $de $res\n");

    if ($res) {
	## FIX MAJOR:  It makes no sense to be updating the "description" field with a
	## string that says "Created at" long afte the hostgroup has been created, simply
	## because some new host got added to the hostgroup.  And in general, we should
	## not be updating the hostgroup at all if this host already belongs to the
	## hostgroup.  (That refinement might wait until we have an efficient means to
	## collect hostgroup membership from Foundation without dragging in hordes of
	## detailed status fields for every host in the hostgroup.)
	my @hostgroups = (
	    {
		"name"        => $gr,
		"description" => "Created at " . localtime,
		"alias"       => "NeDi group $gr",
		"hosts"       => [ { hostName => $dv } ]
	    }
	);
	$res = $rest_api->upsert_hostgroups( \@hostgroups, {}, \%outcome, \@results );
	misc::Prt("UHST:$dv added to $gr $res\n");
    }

    return $res;
}

=head2 FUNCTION UPsrv()

Update service
B<Options>

B<Globals> -

B<Returns> -

=cut
sub UPsrv {
    my ( $dv, $ip, $st, $de, $cmt ) = @_;

    my ( %outcome, @results ) = ();

    # FIX MAJOR:  Upon first service creation, we need to send in a full panoply of data
    # to describe the new service.  But after that, we don't need to be sending in fields
    # whose values have not changed, aside from those that identify the particular service
    # or fields being updated.  That is just a pure waste of system resources.
    my %service = (
	'hostName'             => $dv,
	'description'          => $de,
	'deviceIdentification' => $ip,
	'lastCheckTime'        => $lchk,
	'nextCheckTime'        => $nchk,
	'monitorStatus'        => $st,
	'stateType'            => 'HARD',
	'appType'              => 'NEDI',
	'monitorServer'        => 'localhost',
	'checkType'            => 'ACTIVE',
	'properties'           => {
	    'Latency'          => '10',
	    'ExecutionTime'    => '7',
	    'MaxAttempts'      => '1',
	    'LastPluginOutput' => $cmt
	}
    );

    # FIX MAJOR:  The lastHardState should perhaps be set to 'PENDING' (or perhaps the
    # actual first known state) when the service is first created.  But we should ONLY be
    # sending in and updating this value if this is the first time this service is created
    # or if the state actually changed, not continually setting it to this fixed value.
    #
    # FIX MAJOR:  Look up what this field is actually supposed to represent (the
    # last_hard_state field within Nagios), for comparison here.
    #
    $service{lastHardState} = $st if 1;

    # FIX MAJOR:  The lastStateChange value MUST be set to a reasonable timestamp
    # value when the service is first created.  But we should ONLY be sending in and
    # updating this value if this is the first time this service is created or if the
    # state actually changed, not continually setting it to the current time.
    $service{lastStateChange} = $lchk if 1;

    my @services = ( \%service );
    my $res = $rest_api->upsert_services( \@services, {}, \%outcome, \@results );
    $logger->debug( "\\\@results:\n", Dumper \@results );

    misc::Prt("USRV:$dv,$ip $st $de = $res\n");

    return $res;
}

=head2 FUNCTION ADDevent()

Add event
B<Options>

B<Globals> -

B<Returns> -

=cut
sub ADDevent {
    my ( $dv, $st, $se, $sv, $cmt ) = @_;

    my ( %outcome, @results ) = ();
    my @events = (
	{
	    'consolidationName' => 'NEDIEVENT',
	    'device'            => $dv,
	    'host'              => $dv,
	    'monitorStatus'     => $st,
	    'service'           => $sv,
	    'properties'        => { 'Latency' => '125.0', 'Comments' => 'Additional comments' },
	    'appType'           => 'NEDI',
	    'textMessage'       => $cmt,
	    'monitorServer'     => 'localhost',
	    'severity'          => $se,
	    'reportDate'        => $lchk
	}
    );

    my $res = $rest_api->create_events( \@events, {}, \%outcome, \@results );
    $logger->debug( "\\\@results:\n", Dumper \@results );

    misc::Prt("AEVT:$dv $sv $st-$se = $res\n");

    return $res;
}

=head2 FUNCTION DELsrv()

Delete service

B<Options>

B<Globals> -

B<Returns> -

=cut
sub DELsrv {
    my ( $dv, $sv ) = @_;

    my ( %outcome, @results ) = ();

    my @servicenames = ();
    my @hostnames    = ();
    push @servicenames, $sv;
    push @hostnames,    $dv;
    my $res = $rest_api->delete_services( \@servicenames, { hostname => \@hostnames }, \%outcome, \@results );
    $logger->debug( "\\\@results:\n", Dumper \@results );

    misc::Prt("DSRV:$dv $sv = $res\n");

    return $res;
}

=head2 FUNCTION DELhost()

Delete host

B<Options>

B<Globals> -

B<Returns> -

=cut
sub DELhost {
    my ($dv) = @_;

    my ( %outcome, @results ) = ();

    my @hostnames = ();
    push @hostnames, $dv;
    my $res = $rest_api->delete_hosts( \@hostnames, {}, \%outcome, \@results );
    $logger->debug( "\\\@results:\n", Dumper \@results );

    misc::Prt("DHST:$dv = $res\n");

    return $res;
}

=head2 FUNCTION GEThost()

Get hosts from GWOS

B<Options> query

B<Globals> -

B<Returns> -

=cut
sub GEThost {
    my ($query) = @_;

    my ( %outcome, %results ) = ();

    my $res = $rest_api->get_hosts( [], { query => $query }, \%outcome, \%results );
    $logger->debug( "\\\%results:\n", Dumper \%results );

    misc::Prt("GHST:$query = $res\n");

    return \%results;
}

=head2 FUNCTION GETsrv()

Get services from GWOS

B<Options> query

B<Globals> -

B<Returns> -

=cut
sub GETsrv {
    my ($query) = @_;

    my ( %outcome, %results ) = ();

    my $res = $rest_api->get_services( [], { query => $query }, \%outcome, \%results );
    $logger->debug( "\\\%results:\n", Dumper \%results );

    misc::Prt("GSRV:$query = $res\n");

    return \%results;
}


=head2 FUNCTION HELP_MESSAGE()

Display some help

B<Options> -

B<Globals> -

B<Returns> -

=cut
sub HELP_MESSAGE {
    print "\n";
    print "usage: nedi_feeder.pl <Option(s)>\n\n";
    print "---------------------------------------------------------------------------\n";
    print "Options:\n";
    print "-U file	use config\n";
    print "-d	debug output\n";
    print "-v	verbose output\n";
    print "-D	Run as daemon\n\n";
    print "NeDi feeder version $VERSION (c) $COPYRIGHT_YEAR NeDi Consulting Rickli\n\n";
    die;
}

package GW::AlertSite;

# Copyright (c) 2011 GroundWork Open Source (www.groundworkopensource.com).
# All rights reserved.  Use is subject to GroundWork commercial license terms.

# This module handles all the details of contacting AlertSite, requesting device
# data, and maintaining an external database of the trailing endpoint of the last
# time interval for which data has already been retrieved for each device.  (The
# database is needed to persist that dynamically-updated endpoint across bounces
# of the scripting that calls this module.)

# Note:  Some of the operations undertaken by this package are sensitive, in that
# they involve updating external resources such as RRD files or database tables.
# However, no action is taken within this package to protect critical code sections
# against asynchronous interrupts.  It is therefore up to the calling application to
# provide appropriate signal-handling protection to deal with possible termination
# signals and to prevent them from aborting code in this package that might corrupt
# external resources if it is interrupted partway through.  A future release may
# provide formal documentation of which package routines require this kind of
# protection, and which may be safely interrupted.
#
# Furthermore, we currently take no action within the routines provided in this
# package to probe periodically for background receipt of termination signals and
# to abort loops or long-running sequences early if evidence of such receipt is
# found.  It is up to the calling application to provide adequate granularity of such
# checking, and to respond if such flags are found to be set.  As of this writing,
# we believe that the routines in this package provide reasonable structuring of
# the necessary processing into pieces that can be managed by a calling application
# without allowing unreasonably long uninterruptible code execution paths.

# FIX LATER:  Generalize the supported $self->{debug} levels to support our
# standard enumeration, to allow all messages to be conditionally output.
# (Extend GW::Logger to support the numeric enumeration values we need, so
# this package and its caller can agree on what values to set.)

use strict;
use warnings;

# Be sure to update the VERSION as changes are made to this module,
# as well as the copy in the documentation at the end of the file!

our (@ISA, @EXPORT, @EXPORT_OK, %EXPORT_TAGS, $VERSION);
BEGIN {
    use Exporter ();
    @ISA         = qw(Exporter);
    @EXPORT      = qw();
    @EXPORT_OK   = qw();
    %EXPORT_TAGS = ( DEBUG => [ @EXPORT, @EXPORT_OK ] );
    $VERSION     = "0.2.1";
}

use LWP::UserAgent;
use HTTP::Request::Common;
use URI::URL;  # to define url()
use XML::Simple;
use DateTime;
use DBI;
use RRDs;
use Data::Dumper;
$Data::Dumper::Indent   = 1;
$Data::Dumper::Sortkeys = 1;
use POSIX qw(strftime ceil :signal_h);
use POSIX::RT::Timer;

use GW::Logger;
use GW::Nagios qw(
    nagios_plugin_numeric_host_status
    nagios_plugin_numeric_service_severity
    nagios_plugin_symbolic_service_severity
);

# Used to enforce orderly object destruction.
my %alertsite_objects = ();

## We use $utc_timezone to cache this setting and avoid the extra disk i/o of
## looking up this timezone every time we need it.
my $utc_timezone = DateTime::TimeZone::UTC->new();

#######################################################
#
#   AlertSite Access
#
#######################################################

# The new() constructor must be invoked as:
#     my $alertsite = GW::AlertSite->new ($dbhost, $dbname, $dbuser, $dbpass,
#       $latency, $server, $timeout, $timezone, $hostmap, $servicemap, $rollup, $rrdbasedir, $debug);
# because if it is invoked instead as:
#     my $alertsite = GW::AlertSite::new ($dbhost, $dbname, $dbuser, $dbpass,
#       $latency, $server, $timeout, $timezone, $hostmap, $servicemap, $rollup, $rrdbasedir, $debug);
# no invocant is supplied as the implicit first argument.

sub new {
    ## $hostmap is a source => target regexp hash for customer resource -> valid host name mapping.
    ## $servicemap is a source => target regexp hash for AlertSite location -> valid service name mapping.
    my $invocant   = $_[0];                          # implicit argument
    my $dbhost     = $_[1];                          # required argument
    my $dbname     = $_[2];                          # required argument
    my $dbuser     = $_[3];                          # required argument
    my $dbpass     = $_[4];                          # required argument
    my $latency    = $_[5];                          # required argument
    my $server     = $_[6];                          # required argument ("www.alertsite.com")
    my $timeout    = $_[7];                          # required argument (applies to $server access)
    my $timezone   = $_[8];                          # required argument (e.g., "America/Los_Angeles")
    my $hostmap    = $_[9];                          # required argument
    my $servicemap = $_[10];                         # required argument
    my $rollup     = $_[11];                         # required argument ("worst-case" or "most-recent")
    my $rrdbasedir = $_[12];                         # required argument ("/usr/local/groundwork/rrd")
    my $debug      = $_[13];                         # optional argument
    my $class      = ref($invocant) || $invocant;    # object or class name

    # We could do more to validate the constructor parameters, but for now we just do the minimum.
    # For the rest, we depend on the calling application's own configuration validation.

    my $primary_timezone = undef;
    eval {
	$primary_timezone = DateTime::TimeZone->new( name => $timezone );
    };
    if ($@) {
	chomp $@;
	log_timed_message "ERROR:  Invalid timezone \"$timezone\": $@";
	die "\n";
    }

    if (not defined $rrdbasedir) {
	log_timed_message "ERROR:  RRD base directory is not defined.";
	die "\n";
    }
    if ($rrdbasedir !~ m{^/}) {
	log_timed_message "ERROR:  RRD base directory \"$rrdbasedir\" is not an absolute pathname.";
	die "\n";
    }
    if (!-d $rrdbasedir) {
	log_timed_message "ERROR:  RRD base directory \"$rrdbasedir\" cannot be accessed or does not exist.";
	die "\n";
    }
    if (!-r _ or !-w _ or !-x _) {
	log_timed_message "ERROR:  RRD base directory \"$rrdbasedir\" has restricted permissions for user $>.";
	die "\n";
    }

    my %self = (
	dbhost     => $dbhost,
	dbname     => $dbname,
	dbuser     => $dbuser,
	dbpass     => $dbpass,
	dbhandle   => undef,
	latency    => $latency,
	server     => $server,
	timeout    => $timeout,
	timezone   => $primary_timezone,
	hostmap    => $hostmap,
	servicemap => $servicemap,
	rollup     => $rollup,
	rrdbasedir => $rrdbasedir,
	debug      => $debug,
	devices    => {},
	statuses   => {},
	endtime    => {},
	lasttime   => {},
	username   => undef,
	password   => undef,
	useragent  => undef,
	customer   => undef,
	objcust    => undef,
	session    => undef,
	cookie     => undef,
	warning    => undef,
	error      => undef,
    );

    # Save a reference to this object so we can always destroy it safely,
    # before Global Destruction sets in and the objects it references may
    # already be gone by the time we try to reference them.
    $alertsite_objects{ \%self } = \%self;

    bless \%self, $class;
}

# NOTE:  If this gets called on an object within Global Destruction,
# there is no guarantee that any %$self elements which refer to other
# objects haven't already had those objects destroyed.  Thus we might
# not unwind external connections as gracefully as we would like.
# We address that by supplying the END block just below.
sub DESTROY {
    my $self = $_[0];	# implicit argument
    if ($self->{useragent}) {
	$self->{DESTROY} = 1;
	logout($self);
    }
    eval {
	dbdisconnect($self) if $self->{dbhandle};
    };
    delete $alertsite_objects{$self};
}

# This END block fixes the Global Destruction problem noted above by
# using a record of all objects created via GW::AlertSite->new() and not
# previously destroyed, and destroying them gracefully before Global
# Destruction sets in, so we know that all the elements of each object
# have not yet been randomly destroyed themselves before the parent
# object is destroyed.
END {
    foreach my $object (keys %alertsite_objects) {
	# This call will clean up the innards of the object (while those
	# innards are known not to yet be already destroyed themselves),
	# together with our reference to it here, while still leaving
	# the object itself to be cleaned up during Global Destruction.
	# The call to DESTROY for this object during Global Destruction
	# will then have no work to do, but that's fine.
	GW::AlertSite::DESTROY $alertsite_objects{$object};
    }
}

sub daemon_status {
    my $self = $_[0];	# implicit argument
    return $self->{warning}, $self->{error};
}

sub clear_daemon_status {
    my $self = $_[0];	# implicit argument
    $self->{warning} = undef;
    $self->{error}   = undef;
}

sub dbconnect {
    my $self = $_[0];	# implicit argument

    eval {
	dbdisconnect($self) if $self->{dbhandle};
    };
    $self->{dbhandle} = undef;

    my $dsn = "DBI:mysql:$self->{dbname}:$self->{dbhost}";
    eval {
	# We specify RaiseError here, so all database access using this handle
	# must thereafter be wrapped in an eval{}; block to sense errors.
	$self->{dbhandle} = DBI->connect( $dsn, $self->{dbuser}, $self->{dbpass}, {
	    'AutoCommit' => 1,
	    'RaiseError' => 1,
	    # 'PrintError' => 0  # Should we use this too?  Review all error handling.
	} );
    };
    if ($@) {
	chomp $@;
	log_timed_message "ERROR:  \"$self->{dbname}\" database connection failed ($@)";
	$self->{error} = "\"$self->{dbname}\" database connection failed.";
	return 0;
    }
    return 1;
}

sub dbdisconnect {
    my $self = $_[0];	# implicit argument
    my $error = undef;

    # Wrapped in a test to ensure that the handle is still present,
    # because it might not be (e.g., during global destruction).
    if ($self->{dbhandle}) {
	eval {
	    $self->{dbhandle}->disconnect();
	};
	if ($@) {
	    chomp $@;
	    $error = "WARNING:  \"$self->{dbname}\" database disconnect failed ($@)";
	    $self->{warning} = "\"$self->{dbname}\" database disconnect failed.";
	}
    }
    $self->{dbhandle} = undef;
    if ($error) {
	log_timed_message $error;
	return 0;
    }
    return 1;
}

sub dbconnected {
    my $self = $_[0];	# implicit argument
    return $self->{dbhandle} ? 1 : 0;
}

sub catch_abort_signal {
    my $signame = shift;
    log_timed_message "NOTICE:  Caught SIG$signame signal!";
    die "timed out\n";
}

# Internal routine.
sub do_timed_request {
    my $self = $_[0];    # implicit argument
    my $act  = $_[1];    # required argument
    my $ua   = $_[2];    # required argument
    my $req  = $_[3];    # required argument
    my $resp = $_[4];    # required argument

    my $successful = 1;

    # Usually in a routine like this, we would wrap the code to which a timeout should apply in an
    # alarm($timeout) .. alarm(0) sequence (with lots of extra protection against race conditions).
    # However, in the present case, the code we want to wrap already internally absconds with
    # control over SIGALRM.  So we need to impose an independent timer at this level.  For that
    # purpose, we have chosen to use the SIGABRT signal.
    local $SIG{ABRT} = \&catch_abort_signal;

    # If our timer expires, it may kill the wrapped code before it has a chance to cancel a
    # future alarm.  Hopefully it will have a local SIGALRM handler, so that setting should
    # be unwound automatically when we die out of our timer's signal handler and abort our
    # eval{};, but if we get such an uncanceled alarm and we either don't have our own signal
    # handler in place or we haven't ignored the signal at this level, we will exit.  It seems
    # safest to just use the same signal handler we're using for the SIGABRT signal.
    local $SIG{ALRM} = \&catch_abort_signal;

    ## The nested eval{}; blocks protect against race conditions, as described in the comments.
    eval {
	## Create our abort timer in a disabled state.
	my $timer = POSIX::RT::Timer->new( signal => SIGABRT );
	eval {
	    ## Start our abort timer.
	    $timer->set_timeout( $self->{timeout} );

	    # We might die here either explicitly or because of a timeout and the signal
	    # handler action.  If we get the abort signal and die because of it, we need
	    # not worry about resetting the abort before exiting the eval, because it has
	    # already expired (we use a one-shot timer).
	    eval {
		## That's why we switched to using an independent timer and an independent
		## signal (and signal handler).  We haven't actually identified the line
		## of code that does so, but we have shown by experiment that this is the
		## case.  and would kill our own carefully-set SIGALRM timeout so it becomes
		## inoperative.  The user-agent request logic internally calls alarm()
		## somewhere, perhaps within some sleep() or equivalent indirect call,
		## FIX LATER:  Track down where the alarm stuff happens, and submit a bug
		## report that this should be described in the package documentation.
		$$resp = $ua->request($req);    # Send request, get response
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
	$self->{error} = "$act failure ($@).";
	$successful = 0;
    }

    return $successful;
}

sub login {
    my $self     = $_[0];    # implicit argument
    my $username = $_[1];    # required argument
    my $password = $_[2];    # required argument

    # Save the username/password credentials for possible future use in auto-relogin during
    # auto-retry of data requests, should we discover that the session has expired.  (We've been
    # told that a session will typically time out after two hours, whether it's active or not.)
    $self->{username} = $username;
    $self->{password} = $password;

    # Let's be optimistic and assume success until we find out otherwise.
    my $successful = 1;

    my $ua = LWP::UserAgent->new;
    $ua->agent('AlertSite REST Client/1.0');

    my $LOGIN_POST_XML = "<Login><Login>$username</Login><Password>$password</Password></Login>";

    # Set up an HTTP request to login.
    # Use text/xml and raw POST data to conform to the existing REST API.

    my $req = HTTP::Request->new( POST => "https://$self->{server}/restapi/user/login" );
    $req->content_type('text/xml');
    $req->content($LOGIN_POST_XML);

    my $resp = undef;
    if (not do_timed_request($self, 'AlertSite login', $ua, $req, \$resp)) {
	log_timed_message "ERROR:  $self->{error}";
	$successful = 0;
    }

    my $xmlinput;
    if ($successful) {
	## check the success of the request, at the HTTP::Request level
	my $http_status = $resp->code;
	$xmlinput = $resp->decoded_content( ref => 1 );

	if ( $resp->is_success && $http_status == 200 ) {
	    log_timed_message 'DEBUG:  successful AlertSite login request/response' if $self->{debug};
	}
	else {
	    log_timed_message "ERROR:  unsuccessful AlertSite login request/response; HTTP status code = $http_status";
	    chomp $$xmlinput;
	    log_timed_message "ERROR:  content is:\n", $$xmlinput;
	    $self->{error} = 'AlertSite login failure (bad HTTP status).';
	    $successful = 0;
	}
    }

    my $xml;
    if ($successful) {
	## We set KeepRoot => 0 here to drop the top-level hash index of 'Response',
	## to make subsequent access to these hash values easier.  Unlike with certain
	## other AlertSite APIs, testing with various forms of malformed input to
	## the login procedure has not shown anything other than 'Response' at that
	## position in this response hierarchy, if we get back any kind of XML.
	eval { $xml = XMLin( $$xmlinput, KeyAttr => [], ForceArray => 0, KeepRoot => 0 ); };
	if ($@) {
	    chomp $@;
	    log_timed_message "ERROR:  Failed to log in to AlertSite:  could not parse the returned content as XML ($@).";
	    $self->{error} = 'AlertSite login failure (bad XML).';
	    $successful = 0;
	}
	elsif ($self->{debug}) {
	    log_timed_message "DEBUG:  AlertSite login response:";
	    log_message Dumper($xml);
	}
    }

    if ($successful) {
	## Check the success of the request, according to the returned XML.
	if ( not defined $xml->{Status} ) {
	    log_timed_message "ERROR:  Failed to log in to AlertSite; no Status is available.";
	    $self->{error} = 'AlertSite login failure (no Status).';
	    $successful = 0;
	}
	elsif ( $xml->{Status} ne 0 ) {
	    log_timed_message "ERROR:  Failed to log in to AlertSite; Status $xml->{Status}:  $xml->{Message}";
	    $self->{error} = "AlertSite login failure (bad Status; $xml->{Message}).";
	    $successful = 0;
	}
    }

    if ($successful) {
	## Finally, a fully-successful login.

	my $session  = $xml->{SessionID};
	my $customer = ref( $xml->{Custid}  ) eq 'HASH' ? '' : $xml->{Custid};
	my $objcust  = ref( $xml->{ObjCust} ) eq 'HASH' ? '' : $xml->{ObjCust};
	my $cookie   = $resp->header('Set-Cookie');

	# Save the stuff we'll need for later use in fetching object data from the server.
	$self->{useragent} = $ua;
	$self->{session}   = $session;
	$self->{customer}  = $customer;
	$self->{objcust}   = $objcust;
	$self->{cookie}    = $cookie;
    }
    else {
	## Kill the stuff we need for later use, to indicate that we have no session.
	$self->{useragent} = undef;
	$self->{customer}  = undef;
	$self->{objcust}   = undef;
	$self->{session}   = undef;
	$self->{cookie}    = undef;
    }

    return $self->{useragent} ? 1 : 0;
}

sub logout {
    my $self = $_[0];    # implicit argument

    # Wrapped in a test to ensure that the handle is still present,
    # because it might not be (e.g., during global destruction).
    if ( $self->{useragent} ) {
	my $req = HTTP::Request->new( POST => "https://$self->{server}/restapi/user/logout" );
	$req->header( Cookie => $self->{cookie} );
	$req->content_type('text/plain');
	$req->content("\n");

	## We don't bother to log if we are being called from the destructor, because
	## it's likely that the log file has already been closed under that condition.
	my $resp = undef;
	if (not do_timed_request($self, 'Logout', $self->{useragent}, $req, \$resp)) {
	    log_timed_message "ERROR:  $self->{error}" if not $self->{DESTROY};
	}
	elsif ( $resp->is_success ) {
	    log_timed_message "DEBUG:  successful AlertSite logout request/response" if $self->{debug} and not $self->{DESTROY};
	}
	else {
	    log_timed_message "ERROR:  unsuccessful AlertSite logout request/response" if not $self->{DESTROY};
	}
    }

    $self->{useragent} = undef;
    $self->{cookie}    = undef;
}

# Potentially transient state.  Sometimes desired to check if we need to log in,
# possibly because the session expired some time after we already logged in.  But
# this just tells if we think we have an active session; it doesn't actually
# reach over to AlertSite and tell us if the session is really not still valid.
# (Furthermore, since even active sessions expire after a couple of hours, any such
# determination made here would be subject to race conditions.)  So all this tells us
# is that we definitely need to log in, if this call fails.  We still need to cope
# with unexpected session expiration in the middle of any of our other actions.
sub connected {
    my $self = $_[0];	# implicit argument
    return $self->{useragent} ? 1 : 0;
}

# This routine is nearly useless for our purposes.  We originally thought we needed
# it for the purpose of fetching all the device (object) IDs.  But in practice, we
# can get almost all the parts of this data that we actually need in a single call
# along with the basic last-state monitoring results from each of the AlertSite
# monitoring stations, by wildcarding the device in the get_device_status() routine.
#
# The two pieces of data not present in the devices/status response that can be
# usefully retrieved using this call are:
# (*) The AlertSite polling interval for each device.  That is useful when we
#     create an RRD file, and it is returned as the TxnList.Txn.TxnDetail.Interval
#     field (specified in minutes) in a device listing,
# (*) Whether monitoring is enabled at the device level.  That is useful to tell
#     whether we need to add a host for this resource in the "monarch" database,
#     if it is not already present there.
sub get_device_list {
    my $self      = $_[0];    # implicit argument
    my $recursive = $_[1];    # optional argument, for internal use
    my %devices;

    # Let's be optimistic and assume success until we find out otherwise.
    my $successful = 1;

    # If our last go-around left us logged out, we may as well try a login immediately
    # instead of stumbling ahead into certain failure.
    if (not connected($self) and not login($self, $self->{username}, $self->{password})) {
	$successful = 0;
    }

    my $resp;
    if ($successful) {
	# Set Login from config-file parameter, SessionID from login request response.
	my $LIST_POST_XML = << "LIST_POST_XML";    # Request body
<List>
    <APIVersion>1.1</APIVersion>
    <TxnHeader>
	<Request>
	    <Login>$self->{username}</Login>
	    <SessionID>$self->{session}</SessionID>
	</Request>
    </TxnHeader>
    <Source>REST_Client</Source>
</List>
LIST_POST_XML

	# Set up an HTTP request to list devices and include the cookie from login.
	# Use text/xml and raw POST data to conform to the existing REST API.

	my $req = HTTP::Request->new( POST => "https://$self->{server}/restapi/devices/list" );
	$req->header( Cookie => $self->{cookie} );
	$req->content_type('text/xml');
	$req->content($LIST_POST_XML);

	if (not do_timed_request($self, 'Device list', $self->{useragent}, $req, \$resp)) {
	    log_timed_message "ERROR:  $self->{error}";
	    $successful = 0;
	}
    }

    my $xmlinput;
    if ($successful) {
	## check the success of the request, at the HTTP::Request level
	my $http_status = $resp->code;
	$xmlinput = $resp->decoded_content( ref => 1 );

	if ( $resp->is_success && $http_status == 200 ) {
	    log_timed_message "DEBUG:  successful device list retrieval request/response" if $self->{debug};
	}
	else {
	    log_timed_message "ERROR:  unsuccessful device list retrieval request/response; HTTP status code = $http_status";
	    chomp $$xmlinput;
	    log_timed_message "ERROR:  content is:\n", $$xmlinput;
	    $self->{error} = 'Device list retrieval failure (bad HTTP status).';
	    $successful = 0;
	}
    }

    my $xml;
    if ($successful) {
	## Just for development debugging.
	if ($self->{debug} && 0) {
	    log_timed_message "DEBUG:  raw device list response xml:";
	    log_message $$xmlinput;
	}

	## We set KeepRoot => 0 here to drop the top-level hash index of 'Response',
	## to make subsequent access to these hash values easier.  We might get back
	## some non-XML content if the retrieval request is not successful, but in
	## that case we won't enter this branch of the code.  And so far in testing
	## we haven't seen anything like <error>...</error> instead of the expected
	## <Response>...</Response> packet.
	eval { $xml = XMLin( $$xmlinput, KeyAttr => [], ForceArray => ['Txn'], KeepRoot => 0 ); };
	if ($@) {
	    chomp $@;
	    log_timed_message "ERROR:  Failed to retrieve device list:  could not parse the returned content as XML ($@).";
	    $self->{error} = 'Device list retrieval failure (bad XML).';
	    $successful = 0;
	}
	elsif ($self->{debug}) {
	    log_timed_message "DEBUG:  Device list retrieval response:";
	    log_message Dumper($xml);
	}
    }

    if ($successful) {
	## Check the success of the request, according to the returned XML.
	if ( not defined $xml->{Status} ) {
	    log_timed_message "ERROR:  Failed to retrieve device list; no Status is available.";
	    $self->{error} = 'Device list retrieval failure (no Status).';
	    $successful = 0;
	}
	elsif ( $xml->{Status} ne 0 ) {
	    log_timed_message "ERROR:  Failed to retrieve device list; Status $xml->{Status}:  $xml->{Message}";
	    ## Here we handle the case where we might have an expired session.
	    ## See the AlertSite documentation for the meanings of these codes:
	    ##     20 => REST API session is invalid
	    ##     30 => REST API session failure
	    if (($xml->{Status} == 20 || $xml->{Status} == 30) and not $recursive) {
		log_timed_message "NOTICE:  Failure was due to a bad session; will attempt re-login and retry.";
		if (login($self, $self->{username}, $self->{password})) {
		    ## At most, a single-level recursive call to ourself.
		    return get_device_list($self, 1);
		}
		## login() already provided all the logging and error recording we need here
	    }
	    else {
		$self->{error} = "Device list retrieval failure (bad Status; $xml->{Message}).";
	    }
	    $successful = 0;
	}
    }

    if ($successful) {
	## What we need back is a list of object device IDs that are subject
	## to monitoring, plus perhaps some amount of detail on them.
	my $Txn = $xml->{'TxnList'}{'Txn'};
	## This type coercion should no longer be needed, given our setting of ForceArray above.
	## But it doesn't hurt to test, just in case ...
	$Txn = [ $Txn ] if ref $Txn eq 'HASH';

	for ( my $dev = 0; my $Txn_dev = $Txn->[$dev]; ++$dev ) {
	    my $name       = $Txn_dev->{'TxnName'};
	    my $TxnDetail  = $Txn_dev->{'TxnDetail'};
	    my $interval   = $TxnDetail->{'Interval'};
	    my $monitor    = $TxnDetail->{'Monitor'};
	    my $notify     = $TxnDetail->{'Notify'};
	    my $obj_device = $TxnDetail->{'ObjDevice'};
	    ## FIX LATER:  Only output those devices whose Notify flag is 'y'?
	    ## Ask AlertSite the intended meaning of this flag, or make this a config option.
	    ## For now, we are interpreting this flag to indicate what action AlertSite should
	    ## take if a device error is found, not what action GroundWork Monitor should take.
	    if ($monitor eq 'y') {
		$devices{$obj_device}{'TxnName'}  = $name;
		$devices{$obj_device}{'Interval'} = $interval;       # in minutes
		$devices{$obj_device}{'StepSize'} = $interval * 60;  # corresponding RRD step size, in seconds
		$devices{$obj_device}{'Monitor'}  = $monitor;
		$devices{$obj_device}{'Notify'}   = $notify;
	    }
	    else {
		$name =~ s/%20/ /g;
		log_timed_message "INFO:  Skipping device $obj_device (\"$name\"), as it is not configured for monitoring.";
	    }
	}
    }

    return $successful ? \%devices : undef;
}

# A routine for the caller to pass back the result of a call to get_device_list()
# and have it saved (or cleared) for use in this package.  This allows the caller
# to control how often the list is refreshed.
sub set_device_list {
    my $self    = $_[0];    # implicit argument
    my $devices = $_[1];    # optional argument; leave undefined to clear the list

    $self->{devices} = $devices || {};

    # Return the input value, so we can chain get_device_list() and set_device_list()
    # calls and still get back a testable indication of whether the setup worked.
    return $devices;
}

# The caller can obtain status for all devices in just one call, by omitting the $obj_device parameter.
# In fact, that is the usual way to call this routine.
sub get_device_status {
    my $self       = $_[0];	# implicit argument
    my $obj_device = $_[1];	# optional argument
    my $recursive  = $_[2];     # optional argument, for internal use
    my %status     = ();

    # If $obj_device is not provided, we will grab status for all devices.
    $obj_device = '' if not defined($obj_device);

    # Let's be optimistic and assume success until we find out otherwise.
    my $successful = 1;

    # If our last go-around left us logged out, we may as well try a login immediately
    # instead of stumbling ahead into certain failure.
    if (not connected($self) and not login($self, $self->{username}, $self->{password})) {
	$successful = 0;
    }

    my $resp;
    if ($successful) {
	my $STATUS_POST_XML = << "STATUS_POST_XML";    # Request body
<Status>
    <TxnHeader>
	<Request>
	    <Login>$self->{username}</Login>
	    <SessionID>$self->{session}</SessionID>
	    <ObjCust>$self->{objcust}</ObjCust>
	    <ObjDevice>$obj_device</ObjDevice>
	</Request>
    </TxnHeader>
    <Source></Source>
</Status>
STATUS_POST_XML

	# Set up an HTTP request to get device statuses and include the cookie from login.
	# Use text/xml and raw POST data to conform to the existing REST API.

	my $req = HTTP::Request->new( POST => "https://$self->{server}/restapi/devices/status" );
	$req->header( Cookie => $self->{cookie} );
	$req->content_type('text/xml');
	$req->content($STATUS_POST_XML);

	if (not do_timed_request($self, 'Device status', $self->{useragent}, $req, \$resp)) {
	    log_timed_message "ERROR:  $self->{error}";
	    $successful = 0;
	}
    }

    my $xmlinput;
    if ($successful) {
	## check the success of the request, at the HTTP::Request level
	my $http_status = $resp->code;
	$xmlinput = $resp->decoded_content( ref => 1 );

	if ( $resp->is_success && $http_status == 200 ) {
	    log_timed_message "DEBUG:  successful device status retrieval request/response" if $self->{debug};
	}
	else {
	    log_timed_message "ERROR:  unsuccessful device status retrieval request/response; HTTP status code = $http_status";
	    chomp $$xmlinput;
	    log_timed_message "ERROR:  device status retrieval content is:\n", $$xmlinput;
	    $self->{error} = 'Device status retrieval failure (bad HTTP status).';
	    $successful = 0;
	}
    }

    my $xml;
    if ($successful) {
	## We set KeepRoot => 0 here to drop the top-level hash index of 'Response',
	## to make subsequent access to these hash values easier.  We might get back
	## some non-XML content if the retrieval request is not successful, but in
	## that case we won't enter this branch of the code.  And so far in testing
	## we haven't seen anything like <error>...</error> instead of the expected
	## <Response>...</Response> packet.
	eval { $xml = XMLin( $$xmlinput, KeyAttr => [], ForceArray => ['Device'], KeepRoot => 0 ); };
	if ($@) {
	    chomp $@;
	    log_timed_message "ERROR:  Device status retrieval failure:  could not parse the returned content as XML ($@).";
	    $self->{error} = 'Device status retrieval failure (bad XML).';
	    $successful = 0;
	}
	elsif ($self->{debug}) {
	    log_timed_message "DEBUG:  Device status retrieval response for "
	      . ( $obj_device ? "obj_device '$obj_device'" : 'all devices' )
	      . " is:\n"
	      . $$xmlinput;
	    log_message Dumper($xml);
	}
    }

    if ($successful) {
	## Check the success of the request, according to the returned XML.
	if ( not defined $xml->{Status} ) {
	    log_timed_message "ERROR:  Failed to retrieve device status; no Status is available.";
	    $self->{error} = 'Device status retrieval failure (no Status).';
	    $successful = 0;
	}
	elsif ( $xml->{Status} ne 0 ) {
	    log_timed_message "ERROR:  Failed to retrieve device status; Status $xml->{Status}:  $xml->{Message}";
	    ## Here we handle the case where we might have an expired session.
	    ## See the AlertSite documentation for the meanings of these codes:
	    ##     20 => REST API session is invalid
	    ##     30 => REST API session failure
	    if (($xml->{Status} == 20 || $xml->{Status} == 30) and not $recursive) {
		log_timed_message "NOTICE:  Failure was due to a bad session; will attempt re-login and retry.";
		if (login($self, $self->{username}, $self->{password})) {
		    ## At most, a single-level recursive call to ourself.
		    return get_device_status($self, $obj_device, 1);
		}
		## login() already provided all the logging and error recording we need here
	    }
	    else {
		$self->{error} = "Device status retrieval failure (bad Status; $xml->{Message}).";
	    }
	    $successful = 0;
	}
    }

    if ($successful) {
	my $CustomerResource = '';

	my $Devices = $xml->{'DeviceStatuses'}{'Device'};
	## This type coercion should no longer be needed, given our setting of ForceArray above.
	## But it doesn't hurt to test, just in case ...
	$Devices = [ $Devices ] if ref $Devices eq 'HASH';

	my $hostmap = $self->{hostmap};
	$hostmap = {} if not defined $hostmap;
	$hostmap = {} if ref $self->{hostmap} ne 'HASH';
	my $servicemap = $self->{servicemap};
	$servicemap = {} if not defined $servicemap;
	$servicemap = {} if ref $self->{servicemap} ne 'HASH';
	for ( my $dev = 0; my $device = $Devices->[$dev]; ++$dev ) {
	    ## The following fields are available:
	    ##
	    ## Descrip is the URL-encoded string name of the customer resource that AlertSite is probing.
	    ## ObjDevice is the AlertSite integer identifier corresponding to Descrip.
	    ## Location is the string location of the AlertSite server from which this probe was performed.
	    ## ObjLocation is the AlertSite integer identifier corresponding to Location.
	    ## Monitor is 'y' if this Customer Site is to be monitored from this Location, 'n' otherwise.
	    ## LastStatusCode is an integer status code reflecting the result of the last location->resource probe.
	    ## LastStatus is a string interpretation of LastStatusCode (may not exactly match the documented explanation)
	    ## DtLastStatus is the "YYYY-MM-DD hh:mm:ss" timestamp of the last location->resource probe, or an empty hash.
	    ## DtLastError is the "YYYY-MM-DD hh:mm:ss" of the last error from this probing, or an empty hash.
	    ## DtLastErrorCleared is always an empty hash, in our testing
	    ##
	    ## DtLastError and DtLastErrorCleared are not interesting for our purposes, so we ignore them here.
	    ##
	    my $Descrip        = $device->{'Descrip'};
	    my $ObjDevice      = $device->{'ObjDevice'};
	    my $Location       = $device->{'Location'};
	    my $ObjLocation    = $device->{'ObjLocation'};
	    my $Monitor        = $device->{'Monitor'};
	    my $LastStatusCode = $device->{'LastStatusCode'};
	    my $LastStatus     = $device->{'LastStatus'};
	    my $DtLastStatus   = $device->{'DtLastStatus'};

	    # Cleanup and normalization.
	    $Descrip =~ s/%20/ /g;

	    if ($Monitor ne 'y') {
		log_timed_message "INFO:  AlertSite Monitor is disabled for device $ObjDevice (\"$Descrip\")";
		log_timed_message "       as seen from location $ObjLocation (\"$Location\"); ignoring status.";
		next;
	    }

	    # Print the raw data for debugging, or to track down some problem with the
	    # host map, or to identify exactly what value to use for that map.
	    log_timed_message "DEBUG:  Raw resource field:  \"$Descrip\"" if $self->{debug};
	    my $host_name = $hostmap->{$Descrip};
	    $Descrip = (defined $host_name) ? $host_name : host_name($Descrip);

	    # Print the raw data for debugging, or to track down some problem with the
	    # service map, or to identify exactly what value to use for that map.
	    log_timed_message "DEBUG:  Raw location field:  \"$Location\"" if $self->{debug};
	    my $service_name = $servicemap->{$Location};
	    $Location = (defined $service_name) ? $service_name : service_name($Location);

	    if ( $Descrip ne $CustomerResource ) {
		$CustomerResource = $Descrip;
		log_timed_message "DEBUG:  CustomerResource (host):  $CustomerResource ($ObjDevice)" if $self->{debug};
	    }

	    # An empty DtLastStatus value in the response XML appears here as an empty hash,
	    # so we recode it to something we can use below.
	    #
	    # FIX LATER:  See if the XML::Simple SuppressEmpty option could usefully be
	    # invoked so the parser automatically gives us back something other than an
	    # empty hash in this situation.  We would need to ensure that setting this
	    # option wouldn't have any undesired effects on other fields, though.
	    #
	    # Likely, such an empty value would be a CustomerResource/Location combination
	    # that has never been exercised, either because it was recently added and
	    # Monitor is not yet 'y', or it was very recently enabled and the first probe
	    # has not yet happened.  However, we do not have confirmation from AlertSite
	    # as to what gives rise to this condition.
	    $DtLastStatus = '' if ref $DtLastStatus eq 'HASH';

	    # Normalize the $DtLastStatus value to a GMT UNIX epoch-based timestamp.
	    my $LastStatusUTC      = undef;
	    my $LastStatusUTCEpoch = undef;
	    my $readable_utc_time  = '(no DtLastStatus)';
	    if ($DtLastStatus =~ /^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})$/) {
		$LastStatusUTC = DateTime->new(
		    year       => $1,
		    month      => $2,
		    day        => $3,
		    hour       => $4,
		    minute     => $5,
		    second     => $6,
		    nanosecond => 0,
		    time_zone  => $self->{timezone}
		);
		$LastStatusUTC->set_time_zone($utc_timezone);
		$LastStatusUTCEpoch = $LastStatusUTC->epoch();
		## Stringification for the pattern matching coerces the DateTime into an ISO-8601 string
		## ("YYYY-MM-DDThh:mm:ss") expressed in the specified timezone, which we then modify.
		($readable_utc_time = $LastStatusUTC) =~ s/T/ /;
		$readable_utc_time .= " UTC" if $readable_utc_time;
	    }
	    elsif ($self->{debug}) {
		## This device has never been probed from this location.
		log_timed_message "DEBUG:  device status content for $Descrip (ObjDevice $ObjDevice)";
		log_timed_message "        as seen from $Location (ObjLocation $ObjLocation) is:";
		log_message Dumper($device);
		$readable_utc_time = "DtLastStatus='$DtLastStatus'";
	    }

	    log_timed_message
	      "  ObjLocation $ObjLocation (service $Location):  Monitor? $Monitor; Status $LastStatus ($LastStatusCode); $readable_utc_time"
		if $self->{debug};

	    # We re-cast the incoming data into a form that the calling script can easily scan.
	    # This code uses auto-vivification to create leaf hashes as needed.
	    my $device_status = \%{ $status{$ObjDevice} };
	    $device_status->{CustomerResource}       = $CustomerResource;
	    $device_status->{Monitor}                = defined( $self->{devices}{$ObjDevice} ) ? $self->{devices}{$ObjDevice}{'Monitor'} : 'n';
	    $device_status->{ObjLocation}{$Location} = $ObjLocation;
	    my $status_location = \%{ $device_status->{Probes}{$ObjLocation} };
	    $status_location->{Location}           = $Location;
	    $status_location->{Monitor}            = $Monitor;
	    $status_location->{LastStatusCode}     = $LastStatusCode;
	    $status_location->{LastStatus}         = $LastStatus;
	    $status_location->{LastStatusUTC}      = $LastStatusUTC;
	    $status_location->{LastStatusUTCEpoch} = $LastStatusUTCEpoch;
	}
    }

    return $successful ? \%status : undef;
}

# A routine for the caller to pass back the result of a call to get_device_status()
# and have it saved (or cleared) for use in this package.  This allows the caller
# to control how often the status is refreshed.
sub set_device_status {
    my $self     = $_[0];    # implicit argument
    my $statuses = $_[1];    # optional argument; leave undefined to clear the statuses

    $self->{statuses} = $statuses || {};

    # Return the input value, so we can chain get_device_status() and set_device_status()
    # calls and still get back a testable indication of whether the setup worked.
    return $statuses;
}

# Package-internal routine to convert a resource string returned from AlertSite into a reasonable valid host name.
sub host_name {
    my $resource = shift;

    if (defined $resource) {
	## Normalize to the usual hostname character restrictions, as best we can.
	## This name won't really be used as an actual hostname on a network, but the
	## transform will keep it legal within Monarch's future hostname constraints.
	$resource =~ s/\s+-/-/g;
	$resource =~ s/-\s+/-/g;
	$resource =~ s/ /-/g;
	$resource =~ s/_/-/g;
	$resource =~ s/[^-.a-zA-Z0-9]//g;
	$resource =~ s/^-+|-+$//g;
    }

    return $resource;
}

# Package-internal routine to convert a location string returned from AlertSite into a reasonable valid service name.
sub service_name {
    my $location = shift;

    # Checking to see if we've been passed a reference is a precaution that we've
    # not seen the need for in testing, but we're being extra cautious here.
    if (defined($location) and not ref $location) {
	## Normalize to reasonable servicename character restrictions, as best we can.
	## This transform should keep the name legal within Monarch's future servicename
	## constraints.
	$location =~ s/,//g;
	$location =~ s/\.//g;
	$location =~ s/\s+-/-/g;
	$location =~ s/-\s+/-/g;
	$location =~ s/ /-/g;
	$location =~ s/^-+|-+$//g;
    }

    return $location;
}

sub get_device_metrics {
    my $self          = $_[0];	# implicit argument
    my $obj_device    = $_[1];	# required argument
    my $obj_locations = $_[2];	# required argument; arrayref
    my @devices       = ();     # AlertSite devices for which we have collected metric data
    my %metrics       = ();     # ${host}_${service} => arrayref to RRD timestamp+data update strings
    my @messages      = ();     # Nagios host+service check messages

    # Let's be optimistic and assume success until we find out otherwise.
    my $successful = 1;

    # If the database connection has been broken through the actions of an outside agency,
    # (say, if the database server daemon has been bounced), that will not be reflected
    # in the state of our internal handle.  But we'll find out soon enough, via a failure
    # during access below.  So mainly, this test just protects against trying to use the
    # $self->{dbhandle} object later on if it's currently undefined.  To get around the
    # possibility of an external agent breaking the connection, the caller should attempt
    # to reconnect near the beginning of every major cycle, before this routine is ever
    # called, to recover from possible earlier failures.
    if (not dbconnected($self)) {
	log_timed_message "ERROR:  There is no \"$self->{dbname}\" database connection at this time.";
	$self->{error} = "No \"$self->{dbname}\" database connection.";
	return \@devices, \%metrics, \@messages;
    }

    my $customer = $self->{customer};

    # $locations is a comma-separated list of IDs of the AlertSite locations
    # from which the customer web resources are probed.
    my $locations = join(',', @$obj_locations);

    # The value of $from that we calculate here must be the end of the last interval we probed for,
    # in the right format ("YYYY-MM-DD hh:mm:ss", currently in the $self->{timezone} [that is,
    # until AlertSite makes available an option in their API to allow probing via UTC timestamps]).
    # Most commonly, we can just use the DateTime object we saved for this $obj_device at the end
    # of the last processing cycle, when we updated the database timestamp for this device.  If
    # that value is not already available in memory (say, because this process was bounced since
    # the last cycle completed), we try to retrieve it from the database, and if that fails, we
    # manufacture a reasonable value.

    # Alias (shallow copy), not clone (deep copy) of a DateTime object.
    my $from_then = $self->{endtime}{$obj_device};

    if (not defined $from_then) {
	## For the sake of consistency and to avoid any issues with timezones, we always store
	## timestamps in the last_access table in UTC.  When UNIX_TIMESTAMP() is used on a
	## TIMESTAMP column, the function returns the internal timestamp value directly (i.e.,
	## still effectively in UTC), with no implicit string-to-Unix-timestamp conversion.
	my $sqlstmt = "select UNIX_TIMESTAMP(last_time) from last_access where device_id=?";
	eval {
	    # If the row doesn't exist yet, $from_timestamp ends up as undef
	    # without throwing an exception, so that's easy to test afterward.
	    (my $from_timestamp) = $self->{dbhandle}->selectrow_array($sqlstmt, {}, $obj_device);
	    if (defined $from_timestamp) {
		$from_then = DateTime->from_epoch( epoch => $from_timestamp, time_zone => $utc_timezone );
	    }
	};
	if ($@) {
	    chomp $@;
	    log_timed_message "ERROR:  Cannot read the \"$self->{dbname}\" database for object \"$obj_device\" ($@).";
	    $self->{error} = "Cannot read the \"$self->{dbname}\" database.";
	    return \@devices, \%metrics, \@messages;
	}
    }
    if (not defined $from_then) {
	# initialization for this $obj_device:  just look at metrics from the last hour
	# FIX LATER:  Perhaps make this initialization interval configurable, so a bunch of
	# historical data could be pulled in (especially for new customer deployments), once we
	# have code in place to deal gently with possible duplication of RRD file updates.
	my $from_timestamp = time() - (60 * 60);
	$from_then = DateTime->from_epoch( epoch => $from_timestamp, time_zone => $utc_timezone );
    }

    # $to must be "now" in the right format ("YYYY-MM-DD hh:mm:ss", currently in the $self->{timezone}).
    #
    # FIX LATER:  If we didn't have to worry about timezones (i.e., if $to and $from could be
    # expressed in UTC), we might start with this (with latency adjustments and such), and drop
    # most or all of the DateTime stuff:
    # my $to = strftime "%Y-%m-%d %H:%M:%S", gmtime;

    # We cannot reasonably ask for data from AlertSite right up to the current second,
    # because it may take some time for them to complete the probing, record the results
    # in their database, and make it available to external queries.  So we must back off
    # a bit and account for some typical amount of unavoidable latency on their side.

    my $to_timestamp = time() - $self->{latency};
    my $to_now = DateTime->from_epoch( epoch => $to_timestamp, time_zone => $utc_timezone );

    # If $from_then >= $to_now, we need to wait until a later cycle to ask for metrics for this device.
    if ( DateTime->compare($from_then, $to_now) >= 0 ) {
	log_timed_message "WARNING:  Skipped fetching device metrics for object \"$obj_device\" to allow for AlertSite latency.";
	if ($self->{debug}) {
	    (my $readable_from = $from_then) =~ s/T/ /;
	    (my $readable_to = $to_now) =~ s/T/ /;
	    log_timed_message "DEBUG:  from = $readable_from; to = $readable_to";
	}
	$self->{warning} = "Skipped fetching certain device metrics to allow for AlertSite latency.";
	return \@devices, \%metrics, \@messages;
    }

    # We intentionally save the UTC version as an unambiguous timestamp in a separate
    # (deep-copy) instance of the DateTime object, before changing the timezone.
    $self->{endtime}{$obj_device} = $from_then->clone();
    $from_then->set_time_zone( $self->{timezone} );

    ## Stringification for the pattern matching coerces the DateTime into an ISO-8601 string
    ## ("YYYY-MM-DDThh:mm:ss") expressed in the specified timezone, which we then modify.
    (my $from = $from_then) =~ s/T/ /;

    ## Make a deep copy, like we do above.  We will delete this copy later on if we have trouble and
    ## don't actually get to process the data for this device, so we don't go updating the database
    ## with this value and thereby create a dead period in the monitored and accumulated data.
    $self->{lasttime}{$obj_device} = $to_now->clone();
    $to_now->set_time_zone( $self->{timezone} );

    ## Stringification for the pattern matching coerces the DateTime into an ISO-8601 string
    ## ("YYYY-MM-DDThh:mm:ss") expressed in the specified timezone, which we then modify.
    (my $to = $to_now) =~ s/T/ /;

    # Currently, the $from and $to timestamps we submit in the URL to AlertSite must be expressed
    # in the $self->{timezone}, not in UTC.  That explains the set_time_zone() calls just above.
    #
    # FIX LATER:  AlertSite has a fix for the ought-to-be-in-UTC feature request
    # that is already in the works and will be available in the Dashboard Phase
    # III release coming around Q3 - 2011.  When that does become available, this
    # code ought to be changed to use the new facility, to avoid any issues with
    # timestamp ambiguities around Daylight Savings Time transitions.

    # Testing shows that we need exactly one obj_device value here; neither omitting the
    # obj_device parameter, nor leaving its value empty, nor setting it to a comma-delimited
    # list, will yield the obvious and desired effective-wildcard results.
    my $urlstring = "https://$self->{server}/report-api/detail/"
      . "$customer?obj_device=$obj_device&location=$locations&from=$from&to=$to&showrecs=&sort_order=asc";

    # This will also transform each space in $from and $to to "%20".  We haven't tested
    # to see what it would do with "%20" if $urlstring already contained that string.
    my $url = url($urlstring);
    log_timed_message "DEBUG:  device metrics retrieval request URL:\n$url" if $self->{debug};

    # Because each request through the AlertSite Report API is independently
    # authenticated, we don't need to worry about session expiration here.
    my $req = new HTTP::Request 'GET', $url;
    $req->authorization_basic( $self->{username}, $self->{password} );

    my $resp = undef;
    if (not do_timed_request($self, 'Device metrics', $self->{useragent}, $req, \$resp)) {
	log_timed_message "ERROR:  $self->{error}";
	delete $self->{lasttime}{$obj_device};
	$successful = 0;
    }

    my $xmlinput;
    if ($successful) {
	## check the success of the request, at the HTTP::Request level
	my $http_status = $resp->code;
	$xmlinput = $resp->decoded_content( ref => 1 );

	if ( $resp->is_success && $http_status == 200 ) {
	    log_timed_message "DEBUG:  successful device metrics retrieval request/response for object \"$obj_device\"" if $self->{debug};
	}
	else {
	    my $name = defined( $self->{devices}{$obj_device} ) ? $self->{devices}{$obj_device}{'TxnName'} : undef;
	    my $device_name = defined($name) ? " ($name)" : '';
	    log_timed_message "ERROR:  unsuccessful device metrics retrieval request/response for object \"$obj_device\"$device_name; HTTP status code = $http_status";
	    chomp $$xmlinput;
	    log_timed_message "ERROR:  content is:\n", $$xmlinput;
	    $self->{error} = 'Device metrics retrieval failure (bad HTTP status).';
	    delete $self->{lasttime}{$obj_device};
	    $successful = 0;
	}
    }

    my $xml;
    if ($successful) {
	## Just for development debugging.
	if ($self->{debug} && 0) {
	    log_timed_message "DEBUG:  raw device metrics response xml:";
	    log_message $$xmlinput;
	}

	# We set KeepRoot => 1 primarily so we can easily distinguish
	# a normal response (top-level hash index of 'Report') from an
	# error response (top-level hash index of 'error'), without needing
	# to test whether $xml is a hash.
	eval { $xml = XMLin( $$xmlinput, KeyAttr => [], ForceArray => ['Location', 'Measurement'], KeepRoot => 1 ); };
	if ($@) {
	    chomp $@;
	    my $name = defined( $self->{devices}{$obj_device} ) ? $self->{devices}{$obj_device}{'TxnName'} : undef;
	    my $device_name = defined($name) ? " ($name)" : '';
	    log_timed_message "ERROR:  Failed to retrieve device metrics for object \"$obj_device\"$device_name:  could not parse the returned content as XML ($@).";
	    $self->{error} = 'Device metrics retrieval failure (bad XML).';
	    delete $self->{lasttime}{$obj_device};
	    $successful = 0;
	}
	elsif ($self->{debug}) {
	    log_timed_message "DEBUG:  Device metrics retrieval response:";
	    log_message Dumper($xml);
	}
    }

    # Before we use the response data in computations, we run through a series of checks
    # to verify the success of the request, according to the returned XML.
    #
    # With regard to $xml->{Report}, we're careful to arrange the validation tests in a sequence
    # that avoids confusing ourselves with auto-vivification as we try to access its members.

    my $Report;
    if ($successful) {
	$Report = $xml->{Report};
	## The $xml->{error} element might be returned instead of $xml->{Report}.
	if ( defined $xml->{error} ) {
	    my $name = defined( $self->{devices}{$obj_device} ) ? $self->{devices}{$obj_device}{'TxnName'} : undef;
	    my $device_name = defined($name) ? " ($name)" : '';
	    log_timed_message "ERROR:  Failed to retrieve device metrics for obj_device '$obj_device'$device_name;";
	    log_timed_message "        error is \"$xml->{error}\"";
	    log_message Dumper($xml);
	    $self->{error} = "Failed to retrieve device metrics for obj_device $obj_device.";
	    delete $self->{lasttime}{$obj_device};
	    $successful = 0;
	}
	elsif ( !defined($Report) or ref($Report) ne 'HASH' ) {
	    my $name = defined( $self->{devices}{$obj_device} ) ? $self->{devices}{$obj_device}{'TxnName'} : undef;
	    my $device_name = defined($name) ? " ($name)" : '';
	    log_timed_message "ERROR:  Failed to retrieve device metrics for obj_device '$obj_device'$device_name;";
	    log_timed_message "        no <Report> returned:";
	    log_message Dumper($xml);
	    $self->{error} = "Failed to retrieve device metrics for obj_device $obj_device.";
	    delete $self->{lasttime}{$obj_device};
	    $successful = 0;
	}
    }

    my $SiteName;
    if ($successful) {
	$SiteName = $Report->{SiteName};
	if ( !defined($SiteName) or ref($SiteName) or $SiteName eq '' ) {
	    my $name = defined( $self->{devices}{$obj_device} ) ? $self->{devices}{$obj_device}{'TxnName'} : undef;
	    my $device_name = defined($name) ? " ($name)" : '';
	    log_timed_message "ERROR:  Failed to retrieve device metrics for obj_device '$obj_device'$device_name;";
	    log_timed_message "        no <Report><SiteName> returned:";
	    log_message Dumper($xml);
	    $self->{error} = "Failed to retrieve device metrics for obj_device $obj_device.";
	    delete $self->{lasttime}{$obj_device};
	    $successful = 0;
	}
    }

    my $Location;
    if ($successful) {
	$Location = $Report->{Location};

	# In the current AlertSite API, the From and Thru timestamps we get back
	# will be expressed in the $self->{timezone}, not in UTC.
	my $From = $Report->{From} || 'undefined';
	my $Thru = $Report->{Thru} || 'undefined';

	## The <Report><Location> element may be missing entirely.
	if ( not defined $Location ) {
	    ## We have seen this case when the device was configured to be monitored from a certain
	    ## location, but monitoring from that location was disabled, and no probes of this device
	    ## occurred during the $from..$to interval.  So since this is not necessarily a serious
	    ## problem, we just classify it as a warning.
	    ##
	    ## We have also seen this case when monitoring was enabled, but the $from..$to interval
	    ## happened to be too short to contain any probes of this device.  So again, this is not
	    ## generally a serious problem.
	    ##
	    ## In general, we want not to even get here if monitoring is off for this device, by means
	    ## of looking at the <Device><Monitor> value in the device status when we fetch that data
	    ## earlier, and just skipping this device if that flag shows the device to be disabled.
	    ## And we do have logic in place to do so.  But that still leaves us open to a possible
	    ## race condition, if the device was just disabled between the time we found the device
	    ## status and now, perhaps many minutes later, when we're looking for device metrics.
	    ##
	    ## For all the reasons above, we need to maintain support for this case, and do something
	    ## reasonable when we find ourselves here.
	    ##
	    ## In a future release, we might downgrade this to a NOTICE, but only after we resolve
	    ## the fact that we're seeing a lot of time periods longer than the configured AlertSite
	    ## monitoring interval for a particular device, where in fact no metric data is being
	    ## retrieved by our request.  So for the time being, these are in fact serious problems.

	    my $interval   = defined( $self->{devices}{$obj_device} ) ? $self->{devices}{$obj_device}{'Interval'} : undef;
	    my $configured = defined($interval) ? "; configured monitoring interval is $interval minutes" : '';

	    log_timed_message "WARNING:  Failed to retrieve device metrics for obj_device '$obj_device' ($SiteName);";
	    log_timed_message "          no <Report><Location> returned$configured.";
	    ## This is such a common occurrence (asking AlertSite for device metric data for a time
	    ## interval during which the device was not probed by AlertSite) that you would think we
	    ## wouldn't want the extra burden of logging details unless we really ask for them, and
	    ## that we should qualify this next statement with "if $self->{debug}".  But in fact, we
	    ## find that the content of this message includes the spelled-out From and Thru timestamps,
	    ## which can be instrumental in diagnosing problems of data retrieval and of latency between
	    ## when results are gathered by AlertSite probing actions and when they are made available
	    ## via external queries such as we are making.  And in fact, since the standard cycle time
	    ## for our metric gathering will generally be much longer than the typical configured probe
	    ## interval that AlertSite should be following, we do in fact expact to receive metric data
	    ## during every request here, for every device.
	    my $step_size = defined( $self->{devices}{$obj_device} ) ? $self->{devices}{$obj_device}{'StepSize'} : undef;
	    if (defined $step_size) {
		## DateTime of the From point:  $self->{endtime}{$obj_device}
		## DateTime of the To point:    $self->{lasttime}{$obj_device}
		my $timespan = $self->{lasttime}{$obj_device}->subtract_datetime_absolute( $self->{endtime}{$obj_device} );
		my $seconds  = $timespan->in_units('seconds');
		if ( $seconds > $step_size * 1.4 ) {
		    log_timed_message "NOTICE:  No metric data is available for significantly longer ($seconds seconds)";
		    log_timed_message "         than the AlertSite-configured monitoring interval of $step_size seconds.";
		}
	    }
	    log_message Dumper($xml);
	    $self->{warning} = "Failed to retrieve device metrics for $SiteName.";
	    delete $self->{lasttime}{$obj_device};
	    $successful = 0;
	}
	## We don't really expect these values to ever be invalid; we're just being extra cautious.
	## The rest of the code is not actually dependent on From or Thru, as it pays attention instead
	## to the Timestamp values for individual measurements.  But this might help us detect the
	## conversion of the AlertSite API to UTC timestamps, when that eventually occurs.
	elsif ($From ne $from or $Thru ne $to) {
	    log_timed_message "ERROR:  Failed to retrieve device metrics for obj_device '$obj_device' ($SiteName);";
	    log_timed_message "        found mismatched request/response interval endpoints:";
	    log_message "ERROR:   request period from: $from";
	    log_message "ERROR:  response period From: $From";
	    log_message "ERROR:   request period   to: $to";
	    log_message "ERROR:  response period Thru: $Thru";
	    $self->{error} = "Failed to retrieve device metrics for $SiteName.";
	    delete $self->{lasttime}{$obj_device};
	    $successful = 0;
	}
    }

    if ($successful) {
	if ($self->{debug}) {
	    log_timed_message "DEBUG:  parsed device metrics response xml:";
	    log_message Dumper($xml);
	}

	my $hostmap = $self->{hostmap};
	$hostmap = {} if not defined $hostmap;
	$hostmap = {} if ref $self->{hostmap} ne 'HASH';

	my $host_name = $hostmap->{$SiteName};
	my $host = (defined $host_name) ? $host_name : host_name($SiteName);
	my $Locations = $Location;
	## This type coercion should no longer be needed, given our setting of ForceArray above.
	## But it doesn't hurt to test, just in case ...
	$Locations = [ $Locations ] if ref $Locations eq 'HASH';

	my $step_size = defined( $self->{devices}{$obj_device} ) ? $self->{devices}{$obj_device}{'StepSize'} : undef;

	my $servicemap = $self->{servicemap};
	$servicemap = {} if not defined $servicemap;
	$servicemap = {} if ref $self->{servicemap} ne 'HASH';

	my $rollup_host_status = nagios_plugin_numeric_host_status('UP');
	my $rollup_message     = 'No problem seen.';
	my $rollup_timestamp   = 0;
	foreach my $location ( @{ $Locations } ) {
	    ## Skip an empty Location entry, should it ever occur.
	    if (ref $location ne 'HASH') {
		log_timed_message "ERROR:  Found non-hash <Location> for <Report><SiteName> (host) \"$host\".";
		$self->{error} = "Found bad Location for SiteName \"$host\".";
		next;
	    }
	    next if not %$location;

	    my $service_name = $servicemap->{ $location->{City} };
	    my $service = (defined $service_name) ? $service_name : service_name( $location->{City} );

	    # Should never happen, and we've never seen it either, but just in case ...
	    if (not defined $service) {
		log_timed_message "ERROR:  Found undefined <Location><City> (service) for <Report><SiteName> (host) \"$host\".";
		$self->{error} = "Found undefined City for SiteName \"$host\".";
		next;
	    }

	    # These messages have to be higher priority (lower debug level) than the STATS messages
	    # which follow, to ensure that those statistical messages appear in reasonable context.
	    log_timed_message "NOTICE:  Resource (host):  $host";
	    log_timed_message "NOTICE:  City (service):  $service";

	    my $Measurements = $location->{Measurement};
	    ## This type coercion should no longer be needed, given our setting of ForceArray above.
	    ## But it doesn't hurt to test, just in case ...
	    $Measurements = [ $Measurements ] if ref $Measurements eq 'HASH';

	    # Lucky for us, with regard to dumping measurements into RRD files, and with respect to finding the
	    # most current status for constructing Nagios service-check messages, all the returned measurements
	    # are provided in chronological order.  So we don't need to take any action to sort them.
	    my @RRD_slots = ();
	    my $Info_msg       = undef;
	    my $Status         = undef;
	    my $Status_verify  = undef;
	    my $Status_warning = undef;
	    my $Timestamp      = undef;
	    my $timestamp      = undef;
	    my $prev_timestamp = undef;
	    my $logged_notice  = 0;
	    foreach my $measurement ( @{ $Measurements } ) {
		## Skip an empty Measurement entry, should it ever occur.
		if ( ref $measurement ne 'HASH' ) {
		    log_timed_message
		      "ERROR:  Found non-hash <Measurement> for <Report><SiteName> (host) \"$host\", <Location><City> (service) \"$service\".";
		    $self->{error} = "Found strange Measurement for SiteName \"$host\", City \"$service\".";
		    next;
		}
		next if not %$measurement;

		# We log this metric as a simple proxy for the entire measurement, even when not debugging,
		# just to demonstrate what the script is seeing.
		log_timed_message "STATS:  ResponseLength:  $measurement->{Timestamp} => $measurement->{ResponseLength}";

		## There is a variety of measurement results returned.  In the present version of this code, the
		## only ones we're going to pay attention to are those that we can use to determine whether the
		## customer resource or the probing is in trouble, or those which represent timing or numeric
		## probe-result information that we should be storing in an RRD file for possible later graphing.
		## In some cases, we simply don't know what certain returned results mean, because the AlertSite
		## documentation provides no clues.
		#
		# Unfortunately, $measurement->{Info_msg} can be empty (an empty hash, given that we haven't
		# modified the default setting of the SuppressEmpty option in our call to XMLin() above) even when
		# an error occurs.  So we cannot depend on it to generate a readable error message reflecting
		# either $measurement->{Status}, $measurement->{Status_verify}, or $measurement->{Status_warning}
		# to be sent to GroundWork Monitor.
		#
		# $measurement->{HTTPStatus}		# (empty hash or ne 'HTTP/1.1 200 OK') => trouble
		# $measurement->{Notified}		# we've seen an empty hash, 'ERROR', and 'CLEAR'
		$Info_msg       = $measurement->{Info_msg};		# probably an empty hashref, but hope never dies
		$Status         = $measurement->{Status};		# the usual enumeration, I suppose (!0 => trouble)
		$Status_verify  = $measurement->{Status_verify};	# non-zero numeric value; !0 => trouble detail
		$Status_warning = $measurement->{Status_warning};	# need to ask AlertSite what this value means
		$Timestamp      = $measurement->{Timestamp};		# AlertSite probe timestamp ("2011-04-13 14:36:04")

		if (defined($Timestamp) && $Timestamp =~ /^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})$/) {
		    # convert $Timestamp from $self->{timezone} to a UTC $timestamp for populating the RRD slot
		    my $ProbeTime = DateTime->new(
			year       => $1,
			month      => $2,
			day        => $3,
			hour       => $4,
			minute     => $5,
			second     => $6,
			nanosecond => 0,
			time_zone  => $self->{timezone}
		    );
		    ## I'm not sure whether it's necessary to convert the timezone before calling epoch(),
		    ## but it won't hurt.  Note that whether we perform this timezone conversion here, via
		    ## DateTime, or via Time::Local inside the epoch() call, we cannot distinguish between
		    ## the two possible epoch-time values represented by a string timestamp in the last hour
		    ## of a Daylight Savings Time period or in the first hour of Standard Time.  If we do
		    ## this conversion via DateTime, we will get the later of the two possible UTC times (in
		    ## effect, interpreting the time string in Standard Time during the last hour of Daylight
		    ## Savings Time).  If we do this conversion inside epoch(), we will get the earlier of the
		    ## two possible UTC times (in effect, interpreting the time string in Daylight Savings
		    ## Time during the first hour of Standard Time).  Since we cannot know for sure which
		    ## period the timestamp represents without further clues from AlertTime, we just punt and
		    ## take one arbitrary choice.  With either choice, we are likely to run into failed RRD
		    ## updates in the first hour of Standard Time, as the UTC clock will appear to have turned
		    ## back and RRD will refuse to process updates with a timestamp before that of the last
		    ## previously-updated slot.  A future evolution of the AlertSite Report API will provide
		    ## relief by making returned Timestamp data directly specified in UTC format.
		    $ProbeTime->set_time_zone($utc_timezone);
		    ## This conversion to the UNIX epoch time does not account for leap seconds.
		    $timestamp = $ProbeTime->epoch();

		    # We have seen that sometimes an AlertSite device is configured for, say, 10 minutes,
		    # but the actual metric monitoring interval instead appears to be, say, 20 minutes.
		    # Let's make that kind of situation very visible, so we don't get confused about what
		    # happened when RRD graphs don't look like we expect them to.
		    if (defined($prev_timestamp) and defined($step_size) and not $logged_notice) {
			my $interval = $timestamp - $prev_timestamp;
			if ($interval > $step_size * 1.4) {
			    ## The Resource and City messages above give this context.
			    log_timed_message "NOTICE:  Metric data interval ($interval seconds) is significantly longer than";
			    log_timed_message "         the AlertSite-configured monitoring interval of $step_size seconds.";
			    ## We don't want to flood the log with lots of similar messages for the same host::service
			    ## if we get a long run of the same interval beteen monitoring samples.
			    $logged_notice = 1;
			}
		    }
		    $prev_timestamp = $timestamp;

		    # See the AlertSite API documentation for the meanings of the specific status codes we
		    # test for explicitly here to set $Warning and $Critical.  Presumably, these calculations
		    # could need modification when we run against some future version of the AlertSite API.
		    my $Warning           = (($Status != 0) && ($Status_verify == 70 || $Status_verify == 79)) ? 1 : 0;
		    my $Critical          = (($Status != 0) && ($Status_verify != 70 && $Status_verify != 79)) ? 1 : 0;
		    my $ResponseLength    = $measurement->{ResponseLength};
		    my $Timings           = $measurement->{Timings};
		    my $Timings_Connect   = $Timings->{Connect};
		    my $Timings_Content   = $Timings->{Content};
		    my $Timings_Dns       = $Timings->{Dns};
		    my $Timings_Firstbyte = $Timings->{Firstbyte};
		    my $Timings_Redirect  = $Timings->{Redirect};
		    my $Timings_Total     = $Timings->{Total};

		    $Warning           = 'U' if not defined $Warning;
		    $Critical          = 'U' if not defined $Critical;
		    $ResponseLength    = 'U' if not defined $ResponseLength;
		    $Timings_Connect   = 'U' if not defined $Timings_Connect;
		    $Timings_Content   = 'U' if not defined $Timings_Content;
		    $Timings_Dns       = 'U' if not defined $Timings_Dns;
		    $Timings_Firstbyte = 'U' if not defined $Timings_Firstbyte;
		    $Timings_Redirect  = 'U' if not defined $Timings_Redirect;
		    $Timings_Total     = 'U' if not defined $Timings_Total;

		    # We will capture the following statistics in the RRD file, in this order:
		    #     Warning Critical ResponseLength
		    #     Timings_Connect Timings_Content Timings_Dns Timings_Firstbyte Timings_Redirect Timings_Total
		    # For convenience in referencing the timing values, the ordering of timing statistics is simply
		    # alphabetic rather than reflecting the time sequence in which these measurements actually apply.
		    # Graphing of the content of the RRD file should rearrange the data in a manner that makes sense.

		    # Formulate and queue an RRD update.  We accumulate all of the measurements for this host+service
		    # so we can shove them all at the single ${host}_${service}.rrd file in one RRD update operation.
		    push @RRD_slots,
			"$timestamp:$Warning:$Critical:$ResponseLength:$Timings_Connect:$Timings_Content:$Timings_Dns:$Timings_Firstbyte:$Timings_Redirect:$Timings_Total";
		}
	    }
	    $metrics{ "${host}_${service}" } = \@RRD_slots if @RRD_slots;

	    foreach my $update (@RRD_slots) {
		log_timed_message "STATS:  RRD update:  $update";
	    }

	    # The generation of service checks here is basically an empty exercise, now that we have
	    # status and metric data collection on separate schedules, and service checks sensibly
	    # being generated from the more-frequent status polling.  This code is left in place
	    # mostly so it's still easily available should it ever need to be revived.
	    #
	    # Every cycle of retrieving AlertSite device-metric probe results for a given
	    # customer-device/alertsite-location will result in a corresponding passive
	    # {host::service}-check result.  Whether or not these check results are actually used is
	    # up to the calling application.
	    #
	    # Alarm message content should include:
	    #     ResponseLength
	    #     uninterpreted numeric Status code
	    #     uninterpreted numeric Status_verify code
	    #     uninterpreted numeric Status_warning code
	    # We'd like to interpret the numbers as strings returned in the service-check result, but
	    # any mapping we might try to maintain as part of this package would almost certainly get
	    # out of date either with respect to whatever codes AlertSite might be generating or how
	    # they interpret such codes.  And their current documentation probably isn't accurate or
	    # complete to begin with.  It would be better to have them change their response to include
	    # the specific intended interpretations under error conditions.

	    # You would think that service-check results ought to be derived elsewhere from status data,
	    # not here from metric data.  But in fact we get more status detail when we ask AlertSite
	    # for metric data than we do when we ask for status data, so we take advantage of that to
	    # provide a more nuanced calculation of the device status to report to Nagios.  On the other
	    # hand, the status data contains a better readable description of the status, as <LastStatus>.
	    # Here we struggle a bit to come up with a useful readable status string.  $Info_msg almost
	    # certainly contains no useful data.  So if we end up not liking the messages we create here,
	    # just comment out the push, and let the calling code construct its own messages.  Or just
	    # configure the caller to ignore these messages (disable generate_service_checks_from_metrics,
	    # in our standard calling script).  Alas, the reported severity will no longer be nuanced,
	    # but perhaps that's a reasonable tradeoff.  We don't try to combine the detailed severity
	    # derived here with the readable description from the status data, because there would always
	    # be a race condition between probing for status and metrics, and no guarantee that the two
	    # representations would reflect the same state.
	    #
	    # FIX LATER:  Discuss with AlertSite to see if they can optionally provide the <LastStatus>
	    # string from the report-api output in an equivalent field of the device metric data, either
	    # by configuration at the customer level when the account is set up, or by parameter in an
	    # individual report-api request.  (This is not critical, because we generally will ignore the
	    # service-check and host-check results generated here, in favor of more-timely results which
	    # are generated from device status rather than device metrics.)

	    # The data fields we're drawing from here are for each measurement, not for the service as a
	    # whole.  By dint of the fact that the measurements are presented and processed in chronological
	    # order, we are using the data fields from the most current measurement.  The service check
	    # construction is carefully designed to handle the boundary case of having no measurements, and
	    # returning an UNKNOWN state in that situation.  The rollup to a host check result is similarly
	    # constructed to handle the boundary case of one or more missing service checks.

	    # We skip generating a service check from this location if it is not being monitored (as seen
	    # in the device status, not in the metric data, which does not include those flags).  In spite
	    # of that, we still keep (in the code above) and store (later on) any metric data we have
	    # collected from this location, because there seems to be no good reason not to.

	    my $monitor_resource = undef;
	    my $monitor_location = undef;
	    my $device_status = $self->{statuses}{$obj_device};
	    if (defined $device_status) {
		$monitor_resource = $device_status->{Monitor};
		my $ObjLocation = $device_status->{ObjLocation}{$service};
		if (defined $ObjLocation) {
		    $monitor_location = $device_status->{Probes}{$ObjLocation}{Monitor};
		}
	    }

	    next
	      if !defined($monitor_resource) or $monitor_resource ne 'y'
	      or !defined($monitor_location) or $monitor_location ne 'y';

	    my ($host_status, $service_severity) = nagios_interpretation_of_alertsite_statuses( $self, $Status, $Status_verify, $Status_warning );
	    my $severity_string = nagios_plugin_symbolic_service_severity($service_severity);
	    my $message =
	      ( defined($Info_msg) and not ref($Info_msg) ) ? $Info_msg : ($severity_string || 'ILLEGAL STATUS');
	    push @messages, service_check_result( $self, $timestamp, $host, $service, $service_severity, $message );

	    # We only roll up service-check results to host-check results if there is actually some data to consolidate.
	    # Otherwise, the failure of a single monitoring location to report any measurements in this cycle would
	    # automatically kick the host status to UNREACHABLE, which would be rather draconian.  If we get no locations
	    # reporting metric measurements, then we will not report any host-check results for this cycle, and it will
	    # be up to Nagios to run its freshness checking to handle this condition.
	    if (defined $Status) {
		if ( $self->{rollup} eq 'worst-case' ) {
		    ## We ignore all location probe timestamps, and just take the worst of the most-recent probes from all the active locations.
		    ## We only use the most-recent result when we find results of the worst-case probes are tied.
		    if ($rollup_host_status < $host_status) {
			$rollup_host_status = $host_status;
			$rollup_timestamp   = $timestamp;
			$rollup_message     = $message;
		    }
		    elsif ($rollup_host_status == $host_status and $rollup_timestamp < $timestamp) {
			$rollup_timestamp = $timestamp;
			$rollup_message   = $message;
		    }
		}
		else {
		    ## Instead of looking for the worst-case result, we look for the most-recent result, and ignore any possibly worse results.
		    ## Using this rollup algorithm is not really recommended, as it can mask trouble accessing the resource from some locations.
		    ## We only use the worst-case result when we find timestamps of the most-recent probes are tied.
		    if ($rollup_timestamp < $timestamp) {
			$rollup_timestamp = $timestamp;
			## Capture the message and host status, too.
			$rollup_message     = $message;
			$rollup_host_status = $host_status;
		    }
		    elsif ($rollup_timestamp == $timestamp and $rollup_host_status < $host_status) {
			$rollup_host_status = $host_status;
			$rollup_message     = $message;
		    }
		}
	    }
	}
	if ($rollup_timestamp) {
	    push @messages, host_check_result( $self, $rollup_timestamp, $host, $rollup_host_status, $rollup_message );
	}

	# Save a device reference so we can update our database timestamp cache outside of this routine,
	# after termination signal protection is restored, with $to as the new $from for the next cycle.
	# If we had trouble earlier in this routine, this update will be skipped, so we can potentially
	# gather old data again in some future cycle.  If we had trouble only in this last part of the
	# logic, we will do this update anyway, so we don't risk later regenerating RRD update lines
	# that would cause errors if they got re-applied to the RRD files in a future cycle.  I suppose
	# we could qualify this update by looking at whether we have indeed changed the size of %metrics
	# in this cycle, but that refinement doesn't seem to buy us much, and might simply cause the
	# same errors to re-appear in the next cycle.  So it's time to move on.  If some small amount of
	# AlertSite probe metric data gets lost this way because we didn't reprocess an interval where we
	# had trouble late in the game, it can't be too big a deal.
	push @devices, $obj_device;
    }

    return \@devices, \%metrics, \@messages;
}

# We return both host and service evaluations of the state data in a single call,
# because there is not a one-to-one mapping between host status and service severity.
sub nagios_interpretation_of_alertsite_statuses {
    my $self           = $_[0];    # implicit argument
    my $Status         = $_[1];
    my $Status_verify  = $_[2];
    my $Status_warning = $_[3];

    # We warn if AlertSite says we should.
    return nagios_plugin_numeric_host_status('DOWN'), nagios_plugin_numeric_service_severity('WARNING') if $Status_warning;

    # No status provided.
    return nagios_plugin_numeric_host_status('UNREACHABLE'), nagios_plugin_numeric_service_severity('UNKNOWN') if not defined $Status;

    # No error seen from this probe location.
    return nagios_plugin_numeric_host_status('UP'), nagios_plugin_numeric_service_severity('OK') if $Status == 0;

    # No further status detail available:  we cannot distinguish any warning states.
    return nagios_plugin_numeric_host_status('DOWN'), nagios_plugin_numeric_service_severity('CRITICAL') if not defined $Status_verify;

    # Error seen from this probe location, but not from other probe location(s) at this time.
    return nagios_plugin_numeric_host_status('UP'), nagios_plugin_numeric_service_severity('WARNING') if $Status_verify == 70;

    # Error seen from this probe location, but verification from other probe location(s) was not available at this time.
    # The AlertSite documentation suggests that this generally indicates a problem with Internet traffic and should not
    # be worried about, so they don't generate an alert.  However, from the standpoint of automated monitoring, we must
    # treat this as DOWN/WARNING, not as UP/WARNING, because if we cannot get verification from other locations, then
    # this is the best information we have at this time, so we should take it seriously.
    return nagios_plugin_numeric_host_status('DOWN'), nagios_plugin_numeric_service_severity('WARNING') if $Status_verify == 79;

    # Any other exceptional condition.
    return nagios_plugin_numeric_host_status('DOWN'), nagios_plugin_numeric_service_severity('CRITICAL');
}

sub host_check_result {
    my $self      = $_[0];    # implicit argument
    my $timestamp = $_[1];    # expressed in seconds UTC since the UNIX epoch
    my $host      = $_[2];
    my $status    = $_[3];    # 0 => UP, 1 => DOWN, 2 => UNREACHABLE
    my $message   = $_[4];

    $timestamp = time() if not defined $timestamp;

    return "[$timestamp] PROCESS_HOST_CHECK_RESULT;$host;$status;$message\n";
}

sub service_check_result {
    my $self      = $_[0];    # implicit argument
    my $timestamp = $_[1];    # expressed in seconds UTC since the UNIX epoch
    my $host      = $_[2];
    my $service   = $_[3];
    my $severity  = $_[4];    # 0 => OK, 1 => WARNING, 2 => CRITICAL, 3 => UNKNOWN
    my $message   = $_[5];

    $timestamp = time() if not defined $timestamp;

    return "[$timestamp] PROCESS_SERVICE_CHECK_RESULT;$host;$service;$severity;$message\n";
}

sub create_rrd_file {
    my $self         = $_[0];    # implicit argument
    my $obj_device   = $_[1];    # required argument
    my $rrd_filepath = $_[2];    # required argument

    # The step size is set to match the AlertSite polling interval for this host+service.
    my $step_size = defined( $self->{devices}{$obj_device} ) ? $self->{devices}{$obj_device}{'StepSize'} : undef;
    return 0 if not $step_size;

    # We will define sufficient RRAs so we can produce high-quality graphs covering up to
    # a bit over a year's worth of data, in basic increments of Daily, Weekly, Monthly,
    # Quarterly, Yearly.  That will make up to 5 RRAs in the RRD file, depending somewhat
    # on the $step_size for this $obj_device.  The yearly graph should cover perhaps 14
    # months, so the site can see this last full month against that same full month a year
    # ago.  These RRAs can only be approximately the precise data-point consolidations for
    # the graphs that might be produced, because in the Status Viewer application, a lot of
    # flexibility is allowed in the selected time periods represented by these graphs.  The
    # standard predefined periods are:  today (midnight until now), last 24 hours, last 48
    # hours, last 5 days, last 7 days, last 30 days, last 90 days, and custom interval.

    # The values we compute will be used to define RRAs:
    #
    # RRA:AVERAGE:xff:steps:rows
    #
    # xff The xfiles factor defines what part of a consolidation interval may be made up from
    # *UNKNOWN* data while the consolidated value is still regarded as known. It is given as
    # the ratio of allowed *UNKNOWN* PDPs to the number of PDPs in the interval. Thus, it
    # ranges from 0 to 1 (exclusive).
    #
    # steps defines how many of these primary data points are used to build a consolidated
    # data point which then goes into the archive.
    #
    # rows defines how many generations of data values are kept in an RRA. Obviously, this has
    # to be greater than zero.

    # We make this parameter relatively simple to modify by pulling it out here into a variable.
    # Note that this only applies during RRD file creation, not during updates or graph creation.
    # The nominal value is usually taken to be "0.5", but we are seeing lots of empty areas
    # within the AlertSite graphs due to missing AlertSite metric data, so we are bumping this
    # up as an experiment.
    my $xfiles_factor = "0.67";

    # A simplified model of nested time intervals, good enough for our purposes here.
    my $seconds_per_minute = 60;
    my $minutes_per_hour   = 60;
    my $hours_per_day      = 24;
    my $days_per_week      = 7;
    my $days_per_month     = 31;
    my $months_per_quarter = 3;
    my $months_per_year    = 12;

    # Derived values.
    my $seconds_per_hour    = $seconds_per_minute  * $minutes_per_hour;
    my $seconds_per_day     = $seconds_per_hour    * $hours_per_day;
    my $seconds_per_week    = $seconds_per_day     * $days_per_week;
    my $seconds_per_month   = $seconds_per_day     * $days_per_month;
    my $seconds_per_quarter = $seconds_per_month   * $months_per_quarter;
    my $seconds_per_year    = $seconds_per_month   * $months_per_year;

    # Here are the basic calculations, to be repeated at each desired graph time scale
    # for which we will attempt to mirror graph pixels with corresponding single cdps.
    # The goal is to calculate pdps_per_cdp given a working assumption that the graphing
    # will use approximately one CDP per pixel.
    #
    # Assumed values:
    #
    #     time_per_step    = step_size				[basic definition]
    #     pdps_per_step    = 1					[basic definition]
    #     cdps_per_pixel   = 1					[a presumptive goal of this calculation]
    #     pixels_per_graph = 520				[set by external constraint]
    #     time_per_graph   = ?????				[set differently for each time scale]
    #
    # Relationships:
    #
    #     time_per_pixel   = time_per_step * steps_per_pixel	[simple algebraic relationship]
    #     time_per_graph   = time_per_pixel * pixels_per_graph	[simple algebraic relationship]
    #
    #     time_per_pixel   = time_per_graph / pixels_per_graph	[rearrangement, still algebraic]
    #     steps_per_pixel  = time_per_pixel / time_per_step	[rearrangement, still algebraic]
    #     steps_per_cdp    = steps_per_pixel / cdps_per_pixel	[simple algebraic relationship]
    #     pdps_per_cdp     = pdps_per_step * steps_per_cdp	[simple algebraic relationship]
    #
    # Let's successively substitute definitions until all the quantities used are assumed values:
    #
    #     pdps_per_cdp     = pdps_per_step * steps_per_cdp			[as above]
    #     pdps_per_cdp     = 1 * (steps_per_pixel / cdps_per_pixel)		[each term substituted]
    #     pdps_per_cdp     = 1 * ((time_per_pixel / time_per_step) / 1)		[each term substituted]
    #     pdps_per_cdp     = (time_per_pixel / time_per_step)			[drop unit factors]
    #     pdps_per_cdp     = ((time_per_graph / pixels_per_graph) / step_size)	[each term substituted]
    #

    # This graph width is set by external constraint:  namely, it must be large enough
    # to accommodate the longest title (resource+location string) seen in testing.
    my $pixels_per_graph = 520;

    # We keep extra data beyond just the minimal amount needed for the graph, as described below.
    # This value is set by a judgment call.
    my $extra_rows_factor = 1.2;

    my @RRAs = ();
    my $pdps_per_step = 1;
    my $max_pdps_per_cdp = 1;
    my @graph_periods = ($seconds_per_day, $seconds_per_week, $seconds_per_month, $seconds_per_quarter, $seconds_per_year);
    foreach my $time_per_graph (@graph_periods) {
	my $pdps_per_cdp = ceil( ( $time_per_graph / $pixels_per_graph ) / $step_size );

	# Rounding effects in the calculation of $pdps_per_cdp will affect the actual number
	# of rows we need to keep to cover the specified $time_per_graph.  So we recalculate.
	#
	# We make our calculations of the RRA CDP time resolutions based on our expected
	# graph width ($pixels_per_graph), as described above.  But in fact, we choose to
	# store somewhat (20%) more data in each RRA.  That will allow some flexibility in
	# case we need to extend the graph width in the future, to accommodate even longer
	# titles.  It also provides for a time window in the longest-period graph of a full
	# 14 months of data, for reasons noted above.
	my $rows_per_rra = int( ( $time_per_graph / ( $pdps_per_cdp * $step_size ) ) * $extra_rows_factor );

	if ( $pdps_per_cdp > 1 ) {
	    push @RRAs, "RRA:AVERAGE:$xfiles_factor:$pdps_per_cdp:$rows_per_rra";
	    if ($max_pdps_per_cdp < $pdps_per_cdp) {
		$max_pdps_per_cdp = $pdps_per_cdp;
	    }
	}
    }

    # This value must be an integer, at least as great as the larger of
    # $max_pdps_per_cdp and $pixels_per_graph (for separate reasons).
    my $max_pdps_to_keep = $max_pdps_per_cdp;
    if ($max_pdps_to_keep < $pixels_per_graph) {
	$max_pdps_to_keep = $pixels_per_graph;
    }
    ## Same extended-period adjustment as above, but for the base RRA.
    $max_pdps_to_keep = int($max_pdps_to_keep * $extra_rows_factor);

    # We make this parameter relatively simple to modify by pulling it out here into a variable.
    # Note that this only applies during RRD file creation, not during updates or graph creation.
    # The nominal value is usually taken to be 1.5, but we are seeing lots of empty areas
    # within the AlertSite graphs due to missing AlertSite metric data, so we are bumping this
    # up as an experiment.
    my $heartbeat_factor = 2.1;

    # heartbeat defines the maximum number of seconds that may pass between two updates of
    # this data source before the value of the data source is assumed to be *UNKNOWN*.
    my $heartbeat = ceil( $step_size * $heartbeat_factor );
    my @command_args = (
	$rrd_filepath,
	'--step', "$step_size",
	'--start', 'n-1yr',
	'--no-overwrite',
	"DS:Warning:GAUGE:$heartbeat:0:1",
	"DS:Critical:GAUGE:$heartbeat:0:1",
	"DS:ResponseLength:GAUGE:$heartbeat:0:U",
	"DS:Timings_Connect:GAUGE:$heartbeat:0:U",
	"DS:Timings_Content:GAUGE:$heartbeat:0:U",
	"DS:Timings_Dns:GAUGE:$heartbeat:0:U",
	"DS:Timings_Firstbyte:GAUGE:$heartbeat:0:U",
	"DS:Timings_Redirect:GAUGE:$heartbeat:0:U",
	"DS:Timings_Total:GAUGE:$heartbeat:0:U",
    );
    push @command_args, "RRA:AVERAGE:$xfiles_factor:$pdps_per_step:$max_pdps_to_keep";
    push @command_args, @RRAs;

    RRDs::create(@command_args);
    my $ERR = RRDs::error;
    if ($ERR) {
	## We don't explicitly include $rrd_filepath in this message,
	## because it is generally already included in $ERR.
	log_timed_message 'ERROR:  RRD create command failed (', $ERR, ')';
	$self->{error} = 'RRD create command failed.';
	return 0;
    }
    return 1;
}

# FIX LATER:  This present code is already reasonably efficient in a certain sense, namely
# that it stacks up multiple RRD-slot updates for a single host+service into a single update
# command here.  However, in normal operation we are still likely to have a lot of host+service
# files to open during a full cycle of the calling daemon.  For that reason, it might be better
# to queue updates to rrdcached instead of directly taking care of all of them here.  Only
# implementing and testing this alternative approach will prove the case one way or the other.
# If we do provide a means to invoke rrdcached, its use should be externally configurable.

# If for some reason a call to update_last_access_time() fails, and the last_access time
# for a given device does not get updated, then the time interval we just processed will be
# reprocessed (and extended) during a later cycle.  This will likely cause a set of updates
# for RRD slots which have already been stuffed into the RRD to be regenerated.  And that will
# cause the RRD update to fail.  To avoid losing any additional data at the tail end which
# could have still been stuffed into the RRD, we analyze the error message and make one attempt
# to stuff that additional data into the RRD.  This policy of "assume success, and react to
# failure" provides the robust efficiency we seek.
#
# In the unlikely event that this second update fails, the situation will correct itself (RRD
# updates will once again succeed) on the cycle after update_last_access_time() succeeds, when
# the processed time interval will no longer overlap with a previously processed interval.  But
# whatever data was collected during the time that the database could not be updated will still
# be gone, even though there was nothing wrong with the RRD files during that period.
#
# We could avoid this exception handling if there were an RRD update flag that said to ignore
# updates for time slots at or before the current-last update already in the file.  The
# alternative would be to go looking ourselves with an "info" command, but it seems wasteful
# to do so before every time we want to update an RRD file.  Possibly there is some option to
# be used when sending data to rrdcached that might have the same effect, and in any case this
# issue will need to be dealt with if we ever switch from performing direct RRD file updates to
# sending updates to rrdcached.

sub create_and_update_rrd_files {
    my $self       = $_[0];    # implicit argument
    my $obj_device = $_[1];    # required argument
    my $metrics    = $_[2];    # required argument

    foreach my $host_service ( keys %$metrics ) {
	my $rrd_filepath = "$self->{rrdbasedir}/$host_service.rrd";
	next if !-f $rrd_filepath and not create_rrd_file( $self, $obj_device, $rrd_filepath );

	# This debug message is here so we can enable it to find out where this process is dying,
	# in case we ever run across a corrupted and poisonous RRD file in the field that causes
	# this process to go down without any fanfare.
	log_timed_message "DEBUG:  attempting RRD update for device $obj_device:  $rrd_filepath" if $self->{debug};

	RRDs::update( $rrd_filepath, @{ $metrics->{$host_service} } );
	my $ERR = RRDs::error;
	if ($ERR) {
	    ## If we find we have just tried to insert some data that is already present in the RRD,
	    ## ignore all such data and attempt to insert any remaining data in hand.  This strategy
	    ## preserves as much data as possible, which could be important because there is no
	    ## guarantee that any updates dropped after the offending update will come around again.
	    if ($ERR =~ /illegal attempt to update using time \d+ when last update time is (\d+) /) {
		my $last_update_time = $1;
		my @RRD_slots = ();
		foreach my $slot ( @{ $metrics->{$host_service} } ) {
		    if ($slot =~ /^(\d+):/ and $1 > $last_update_time) {
			push @RRD_slots, $slot;
		    }
		}
		if (@RRD_slots) {
		    ## This is so common and un-noteworthy that at this stage, we don't
		    ## even bother to record it as a warning, let alone as an error.
		    log_timed_message "NOTICE:  RRD update command failed (old data); will attempt recovery of remaining data:\n$ERR";
		    RRDs::update( $rrd_filepath, @RRD_slots );
		    $ERR = RRDs::error;
		    if ($ERR) {
			## We don't explicitly include $rrd_filepath in this message,
			## because it is generally already included in $ERR.
			log_timed_message "ERROR:  RRD update command failed ($ERR)";
			$self->{error} = 'RRD update command failed.';
		    }
		}
		else {
		    ## This is so common and un-noteworthy that we don't even
		    ## bother to record it as a warning, let alone as an error.
		    log_timed_message "NOTICE:  RRD update command failed (old data); there is no further data to recover:\n$ERR";
		}
	    }
	    else {
		## We don't explicitly include $rrd_filepath in this message,
		## because it is generally already included in $ERR.
		log_timed_message "ERROR:  RRD update command failed ($ERR)";
		$self->{error} = 'RRD update command failed.';
	    }
	}
    }
}

# This routine has to be called under carefully controlled conditions
# (after a call to get_device_metrics()), since it depends on and maintains
# certain subsidiary $self data, and only the package itself knows when
# that data has been updated and ought to be copied out and saved.
#
# To be called if we successfully sent data to GroundWork, to update the
# alertsite database with the last $to value to become the new $from value,
# and to update our internal memory of that shift so we don't have to go
# reading the database to retrieve the same value on the next cycle.
sub update_last_access_time {
    my $self       = $_[0];    # implicit argument
    my $obj_device = $_[1];    # optional argument
    my $sth        = undef;

    # For the sake of consistency and to avoid any issues with timezones, we always store
    # timestamps in the last_access table effectively in UTC, by using epoch-based values.  We
    # believe that FROM_UNIXTIME(epoch_time_value) in this formulation of the insert statement
    # will avoid any kind of conversion, thus sidestepping any timezone/DST issues at this
    # level.  (We may still have timezone issues at other levels of this application code.)
    my $lasttime = ( not defined $obj_device ) ? $self->{lasttime} : { $obj_device => $self->{lasttime}{$obj_device} };
    my $endtime  = $self->{endtime};
    my $sqlstmt  = "insert into last_access values (?, FROM_UNIXTIME(?)) on duplicate key update last_time = values(last_time)";
    eval {
	$sth = $self->{dbhandle}->prepare($sqlstmt);
    };
    if ($@) {
	chomp $@;
	log_timed_message "ERROR:  Cannot prepare last_access updates in the \"$self->{dbname}\" database ($@).";
	$self->{error} = "Cannot prepare \"$self->{dbname}\" database updates.";
	return;
    }
    foreach my $device ( keys %$lasttime ) {
	## $lasttime->{$device} and $endtime->{$device} are both DateTime objects, or possibly undefined.
	if  (
	    defined( $lasttime->{$device} )
	    &&
	    ( !defined( $endtime->{$device} ) || DateTime->compare($lasttime->{$device}, $endtime->{$device}) )
	    ) {
	    eval {
		## $timestamp must be a standard UNIX UTC timestamp number here, to plug into the SQL statement.
		my $timestamp = $lasttime->{$device}->epoch();
		log_timed_message "DEBUG:  updating device $obj_device:  last_time = '$timestamp'" if $self->{debug};
		$sth->execute( $device, $timestamp );
		$sth->finish();
		## Also update our in-memory copy of the last-time-interval endpoint, so we can easily use it as
		## the "from" time in the next processing cycle without looking it up in the database.  If we were
		## to keep the lasttime object around after updating the database, we would make a deep copy:
		##     $self->{endtime}{$device} = $self->{lasttime}{$device}->clone();
		## to avoid any possible aliasing effects of having two references to the same object hanging around
		## (not that we have any particular danger in mind, but we would just want to be extra cautious).  But
		## given that we're going to delete the lasttime object here to keep it from being re-used, we just
		## make a shallow copy (alias) while deleting the first reference.  That ought to be more efficient.
		$self->{endtime}{$device} = delete $self->{lasttime}{$device};
	    };
	    if ($@) {
		chomp $@;
		log_timed_message "ERROR:  Cannot update the \"$self->{dbname}\" database for object \"$obj_device\" ($@).";
		$self->{error} = "Cannot update the \"$self->{dbname}\" database.";
	    }
	}
    }
}

1;

__END__

=head1 NAME

AlertSite - Access AlertSite service to fetch web-sensing data

=head1 SYNOPSIS

    use GW::AlertSite;

    # $hostmap is a source => target hash to map a
    # customer resource name into a valid host name.
    # $servicemap is a source => target hash to map an
    # AlertSite location name into a valid service name.
    # Default transformations will be used for both the
    # hostmap and servicemap if the source is not found
    # in the respective hash.
    my $dbhost      = 'localhost';
    my $dbname      = 'alertsite';
    my $dbuser      = 'my_user';
    my $dbpass      = 'my_pass';
    my $latency     = 90;
    my $server      = 'www.alertsite.com';
    my $timeout     = 60;  # applies to $server access
    my $timezone    = "America/Los_Angeles";  # AlertSite master timezone
    my $hostmap     = {};
    my $servicemap  = {};
    my $rollup      = 'worst-case';
    my $rrdbasedir  = "/usr/local/groundwork/rrd";
    my $debug_level = 0;  # 1 => print extra debug messages, 0 => don't

    my $alertsite = GW::AlertSite->new (
	$dbhost, $dbname, $dbuser, $dbpass,
	$latency, $server, $timeout, $timezone,
	$hostmap, $servicemap, $rollup, $rrdbasedir,
	$debug_level
    );
    $alertsite->dbconnect();
    $alertsite->login($username, $password);

    $alertsite->set_device_list( $alertsite->get_device_list() );

    my $device_statuses = $alertsite->set_device_status( $alertsite->get_device_status() );

    my $obj_device = 12345;
    my $obj_locations = [ 60, 35, 42 ];
    my ($devices, $metrics, $messages) = $alertsite->get_device_metrics($obj_device, $obj_locations);
    $alertsite->create_and_update_rrd_files($obj_device, $metrics);

    $alertsite->logout();
    my ($warning_msg, $error_msg) = $alertsite->daemon_status();
    $alertsite->clear_daemon_status();
    $alertsite->dbdisconnect();

=head1 DESCRIPTION

This module encapsulates the details of accessing the AlertSite servers
to fetch web-site monitoring data.  A limited set of the full data
available from AlertSite is fetched, but it should be sufficient to
process to generate GroundWork alerts and performance-data graphs.

=head1 SUBROUTINES/METHODS

=over

=item new()

The B<new()> constructor returns an object reference which can be used
to access the methods for initializing other aspects of the object and
for pulling data from AlertSite.  If validation of the arguments fails,
the constructor will B<die>.

The B<new()> constructor must be invoked as:

    my $alertsite = GW::AlertSite->new ($dbhost, $dbname, $dbuser, $dbpass,
      $latency, $server, $timeout, $timezone, $hostmap, $servicemap, $rollup,
      $rrdbasedir, $debug_level);

because if it is invoked instead as:

    my $alertsite = GW::AlertSite::new ($dbhost, $dbname, $dbuser, $dbpass,
      $latency, $server, $timeout, $timezone, $hostmap, $servicemap, $rollup,
      $rrdbasedir, $debug_level);

no invocant is supplied as the implicit first argument.

=item $alertsite->dbconnect()

This method must be called to connect to the database used to store
information about time intervals which have already been covered
in previous probing of AlertSite for events for particular devices.
A database connection is needed before the get_device_metrics() call
can proceed.

=item $alertsite->dbdisconnect()

This method must be called to disconnect from the database used to store
information about time intervals which have already been covered in
previous probing of AlertSite for events for particular devices.

=item $alertsite->dbconnected()

This method returns true if the $alertsite object currently has an open
connection to the database, false otherwise.  Calling this routine
just returns internal state, and does not actually probe the database
connection to verify that it is still operational.

=item $alertsite->login($username, $password)

This method must be called to log in to the AlertSite servers and obtain
server credentials used for the get_...() calls.

=item $alertsite->logout()

This method should be called to log out from the AlertSite servers when
no further probing will occur, to clean up session information on the
AlertSite servers.

=item $alertsite->connected()

This method returns true if the $alertsite object is currently logged
in to AlertSite, false otherwise.  Calling this routine just returns
internal state, and does not actually probe AlertSite to verify that
the connection is still valid.

=item $alertsite->get_device_list()

This method is nearly useless for our purposes.  We originally thought
we needed it for the purpose of fetching all the device (object) IDs,
which would then be processed sequentially based on that list.  But in
practice, we can get almost all the parts of this data that we actually
need in a single call along with the basic last-state monitoring results
from each of the AlertSite monitoring stations, by wildcarding the device
in the get_device_status() routine.

The one piece of data not present in the devices/status response that can
be usefully retrieved using this call is the AlertSite polling interval
for each device.  That is useful when we create an RRD file, to set the
proper RRD file step size.  For that reason, it is still necessary to call
get_device_list() and set_device_list() periodically (say, once at the
beginning of each major processing cycle), in case new devices show up.

=item $alertsite->set_device_list( $alertsite->get_device_list() )

This method allows the caller to pass back the result of a call to
get_device_list() and have it saved (or cleared) for use in this package.
This allows the caller to control how often the list is refreshed.

=item $alertsite->get_device_status($obj_device)

This method probes AlertSite to pull back current status data for the
indicated device, or for all devices if $obj_device is not supplied.
In practice, the $obj_device is usually not supplied.  Calling the routine
this way returns complete data on all the devices, including both their
object device IDs and their respective status data, so there is no real
need for a preceding call to another routine to essentially just get the
list of device IDs.  Calling this routine separately for each device to
obtain the status data for just that device would be far less efficient.

=item $alertsite->get_device_metrics($obj_device, $obj_locations);

This method probes AlertSite to pull back recent metric data for the
indicated device.  Due to limitations in the current AlertSite API,
only one device can be probed for at a time.  The interval for which
metric data is retrieved starts at the end of the last interval for
which data was successfully retrieved.  That endpoint is maintained
for each device within this module, as long as the module application
is alive.  To persist the endpoint data across application bounces, the
time-interval data must be flushed to the database after corresponding RRD
files have been updated and GroundWork Monitor alerts have been generated.
The update_last_access_time() routine is used for such flushing.

=item $alertsite->create_and_update_rrd_files($obj_device, $metrics)

This method persists metric data returned by get_device_metrics() into
corresponding RRD files, creating those files if necessary.

=item $alertsite->update_last_access_time($obj_device)

This method is used to flush new-interval data back to the database,
to persist it across application bounces.  The critical data stored
in the database is the end of the last time interval for which data
has already been fetched and processed (sent to GroundWork Monitor).
The $obj_device argument is optional.  If specified, only the data for
that one object will be flushed.  If not specifed, the data for all
objects not previously flushed will now be flushed to the database.
Generally speaking, this routine is only called internally from within
the B<GW::AlertSite> package, since it depends on certain instance
data, and only the package itself knows when that data has been
updated and ought to be flushed.

=item $alertsite->daemon_status()

This method returns the last warning and error messages, if any,
that occurred within the GW::AlertSite package since either the start
of the program or the last call to the clear_daemon_status() routine.
If no warnings or errors have occurred, the respective returned messages
are undefined.

=item $alertsite->clear_daemon_status()

This method clears the internal storage of the last warning and error
messages, in preparation for another cycle of operation.

=back

=head1 CONFIGURATION AND ENVIRONMENT

Configuration is handled mainly via the B<new> class constructor,
with access credentials also being supplied via the B<login> method.
No environment variables are used.

=head1 SEE ALSO

B<GW::Logger>, B<RRDs>

=head1 COPYRIGHT AND LICENSE

Copyright (c) 2011 GroundWork Open Source, Inc.

Use of this module is subject to commercial license terms.

=head1 BUGS AND LIMITATIONS

The AlertSite API operates using timestamps expressed in a local timezone,
which creates ambiguous timestamps and probably invalid time intervals
around Daylight Savings Time transitions.  AlertSite is supposedly working
on an adjusted API that will use some form of UTC; it is expected to be
available in their Dashboard Phase III release, targeted for Q3 - 2011.
Without that capability, we cannot guarantee that alarms or metrics will
be processed correctly around DST transitions.

AlertSite allows a device ID to be effectively wildcarded while probing
for device status, and we take advantage of that for efficiency and speed.
But as of this writing, there is no similar facility for wildcarding
the device ID in a single call while probing for device monitoring
metrics.  Each device metric probe takes typically 10 to 15 seconds,
and this will add up significantly when a customer has many tens of
resources being monitored by AlertSite.  The B<GW::AlertSite> package
is only single-threaded; it does not attempt to overlap requests for
metrics from multiple devices.  This results in a rather long cycle
time for the calling application to process all the monitored devices.
AlertSite needs to be encouraged to provide an efficient mechanism for
bulk retrieval of metric data for multiple devices.

=head1 INCOMPATIBILITIES

None known.

=head1 DIAGNOSTICS

This package depends on the B<GW::Logger> package having been set up by
the calling application.  A variety of high-level progress messages will
be logged in that log file.

To emit a variety of detailed debug messages into the log file, set the
last parameter on the B<new()> constructor to 1.

A typical cycle of operation using this package might involve probing for
data regarding many individual devices, and as such, potentially lots
of anomalies might show up during the cycle as a whole.  If failures
do occur, log messages will record the details, but there is a separate
need for summary strings noting whether anything untoward has occurred
during the entire cycle.  That is the purpose of the daemon_status()
and clear_daemon_status() routines.  At the end of the cycle, call the
daemon_status() routine to retrieve short versions of the last warning
and error conditions that arose during the cycle, followed immediately by
a call to clear_daemon_status() to reset the state for the next cycle.
The short messages can then be selectively forwarded to an external
system to notify people that problems have arisen and that the log file
contains the details.

=head1 DEPENDENCIES

B<GW::AlertSite> depends on a variety of CPAN packages outside of those
provided in a base GWMEE 6.4 release.  It also uses B<GW::Logger>, which
is considered to be a beta version of what will eventually become the
B<GroundWork::Logger> package.  The calling application is assumed to
have initialized the B<GW::Logger> package.

=head1 AUTHOR

GroundWork Open Source, Inc. <www.groundworkopensource.com>

=head1 VERSION

0.2.1

=cut


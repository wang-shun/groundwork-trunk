package GW::Webmetrics;

# Copyright (c) 2011 GroundWork Open Source (www.groundworkopensource.com).
# All rights reserved.  Use is subject to GroundWork commercial license terms.

# NOTE:  Some of the text in this module reflects possible future capabilities.
# This includes any description of using an external database, and of handling
# RRD files and data directly in this package.  Also, the monitoring model used
# by Webmetrics does not necessarily match that of other web-monitoring providers,
# in the manner in which specific provider servers are selected for monitoring
# customer resources in a given monitoring cycle.

# This module handles all the details of contacting Webmetrics, requesting device
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

# ================================================================
# Documentation
# ================================================================

# FIX MINOR:  Try to help the server filter status data during realtime.getdata queries,
# by setting the lastsampletime parameter.  Take the latest timestamp returned for each
# of the active services from the last query, and use the earliest timestamp of that
# set as the lastsampletime parameter value.  That still might result in some duplicate
# samples seen at the client end over successive queries, but at least there wouldn't be
# any dropped data.  This should accommodate the fact that there is likely no guarantee
# that all the probing machines always manage to insert their data into the database in
# globally time-ordered sequence, across all services.

# FIX LATER:  Generalize the supported $self->{debug} levels to support our
# standard enumeration, to allow all messages to be conditionally output.
# (Extend GW::Logger to support the numeric enumeration values we need, so
# this package and its caller can agree on what values to set.)

# ================================================================
# Perl code
# ================================================================

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
    $VERSION     = "0.1.0";
}

use Digest::SHA1 qw(sha1_base64);
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
my %webmetrics_objects = ();

## We use $utc_timezone to cache this setting and avoid the extra disk i/o of
## looking up this timezone every time we need it.
my $utc_timezone = DateTime::TimeZone::UTC->new();

#######################################################
#
#   Webmetrics Access
#
#######################################################

# The new() constructor must be invoked as:
#     my $webmetrics = GW::Webmetrics->new ($dbhost, $dbname, $dbuser, $dbpass,
#       $latency, $server, $timeout, $timezone, $hostmap, $servicemap, $rollup, $rrdbasedir, $debug);
# because if it is invoked instead as:
#     my $webmetrics = GW::Webmetrics::new ($dbhost, $dbname, $dbuser, $dbpass,
#       $latency, $server, $timeout, $timezone, $hostmap, $servicemap, $rollup, $rrdbasedir, $debug);
# no invocant is supplied as the implicit first argument.

sub new {
    ## $hostmap is a source => target regexp hash for customer resource -> valid host name mapping.
    ## $servicemap is a source => target regexp hash for Webmetrics location -> valid service name mapping.
    my $invocant   = $_[0];                          # implicit argument
    my $dbhost     = $_[1];                          # required argument
    my $dbname     = $_[2];                          # required argument
    my $dbuser     = $_[3];                          # required argument
    my $dbpass     = $_[4];                          # required argument
    my $latency    = $_[5];                          # required argument
    my $server     = $_[6];                          # required argument ("www.webmetrics.com")
    my $timeout    = $_[7];                          # required argument (applies to $server access)
    my $timezone   = $_[8];                          # required argument (e.g., "America/Los_Angeles")
    my $hostmap    = $_[9];                          # required argument
    my $servicemap = $_[10];                         # required argument
    my $rollup     = $_[11];                         # required argument ("worst-case", "most-recent", or "none")
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
    $webmetrics_objects{ \%self } = \%self;

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
    delete $webmetrics_objects{$self};
}

# This END block fixes the Global Destruction problem noted above by
# using a record of all objects created via GW::Webmetrics->new() and not
# previously destroyed, and destroying them gracefully before Global
# Destruction sets in, so we know that all the elements of each object
# have not yet been randomly destroyed themselves before the parent
# object is destroyed.
END {
    foreach my $object (keys %webmetrics_objects) {
	# This call will clean up the innards of the object (while those
	# innards are known not to yet be already destroyed themselves),
	# together with our reference to it here, while still leaving
	# the object itself to be cleaned up during Global Destruction.
	# The call to DESTROY for this object during Global Destruction
	# will then have no work to do, but that's fine.
	GW::Webmetrics::DESTROY $webmetrics_objects{$object};
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

# This routine is currently just scaffolding to be possibly enabled in some future release.
if (0) {
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

sub urlEncode {
    my ($str) = @_; 
    $str =~ s/([^=%&a-zA-Z0-9_\-.])/uc sprintf("%%%02x",ord($1))/eg;
    return $str;
}

sub login {
    my $self      = $_[0];    # implicit argument
    my $username  = $_[1];    # required argument
    my $password  = $_[2];    # required argument
    my $recursive = $_[3];    # optional argument, for internal use

    # Save the username/password credentials for possible future use in auto-relogin during
    # auto-retry of data requests, should we discover that the session has expired.  (For some
    # web-monitoring providers, we've been told that a session will typically time out after
    # two hours, whether it's active or not.)
    $self->{username} = $username;
    $self->{password} = $password;

    # Let's be optimistic and assume success until we find out otherwise.
    my $successful = 1;

    my $ua = LWP::UserAgent->new;
    $ua->agent('Webmetrics REST Client/1.0');

    # Set up an HTTP request to emulate a login.  An actual login to Webmetrics is not required,
    # as each individual request constitutes an independent transaction, and there is no persistent
    # session involved.  So all we're really doing here is validating the basic credentials.  The
    # actual full credentials must be regenerated on every call, because of the embedded timestamp.

    my $timestamp = time;    
    my $signature = urlEncode( sha1_base64( $username . $password . $timestamp ) );

    my $req = HTTP::Request->new( GET => "https://$self->{server}/v2/?method=maintenance.getNotepad&sig=$signature&username=$username" );

    my $resp = undef;
    if (not do_timed_request($self, 'Webmetrics login', $ua, $req, \$resp)) {
	log_timed_message "ERROR:  $self->{error}";
	$successful = 0;
    }

    my $xmlinput;
    if ($successful) {
	## check the success of the request, at the HTTP::Request level
	my $http_status = $resp->code;
	$xmlinput = $resp->decoded_content( ref => 1 );

	if ( $resp->is_success && $http_status == 200 ) {
	    log_timed_message 'DEBUG:  successful Webmetrics login request/response' if $self->{debug};
	}
	else {
	    log_timed_message "ERROR:  unsuccessful Webmetrics login request/response; HTTP status code = $http_status";
	    chomp $$xmlinput;
	    log_timed_message "ERROR:  content is:\n", $$xmlinput;
	    $self->{error} = 'Webmetrics login failure (bad HTTP status).';
	    $successful = 0;
	}
    }

    my $xml;
    if ($successful) {
	## We set KeepRoot => 0 here to drop the top-level hash index of 'rsp',
	## to make subsequent access to these hash values easier.
	eval { $xml = XMLin( $$xmlinput, KeyAttr => [], ForceArray => 0, KeepRoot => 0 ); };
	if ($@) {
	    chomp $@;
	    log_timed_message "ERROR:  Failed to log in to Webmetrics:  could not parse the returned content as XML ($@).";
	    $self->{error} = 'Webmetrics login failure (bad XML).';
	    $successful = 0;
	}
	elsif ($self->{debug}) {
	    log_timed_message "DEBUG:  Webmetrics login response:";
	    log_message Dumper($xml);
	}
    }

    if ($successful) {
	## Check the success of the request, according to the returned XML.
	if ( not defined $xml->{stat} ) {
	    log_timed_message "ERROR:  Failed to log in to Webmetrics; no 'stat' field is available.";
	    $self->{error} = 'Webmetrics login failure (no Status).';
	    $successful = 0;
	}
	elsif ( $xml->{stat} ne 'ok' ) {
	    my $message = (ref($xml->{error}) eq 'HASH') ? $xml->{error}{msg} : 'unknown error';
	    log_timed_message "ERROR:  Failed to log in to Webmetrics; status $xml->{stat}:  $message";
	    ## Here we handle the case where we might have run into throttling from the provider.
	    ## See the Webmetrics documentation for the meanings of these codes:
	    ##     18 => Account has exceeded the request limit; you must wait to try this API again.
	    if ((ref($xml->{error}) eq 'HASH' and $xml->{error}{code} == 18) and not $recursive) {
		log_timed_message "NOTICE:  Failure was due to provider throttling; will attempt a retry.";
		sleep 2;
		## At most, a single-level recursive call to ourself.
		return login($self, $self->{username}, $self->{password}, 1);
	    }
	    else {
		$self->{error} = "Webmetrics login failure (bad Status; $message).";
	    }
	    $successful = 0;
	}
    }

    if ($successful) {
	## Finally, a fully-successful login.

	my $session  = 1;      # Emulate a server-side session for this web-monitoring provider.
	my $customer = undef;  # No customer ID is returned.
	my $objcust  = undef;  # No customer name is returned.
	my $cookie   = undef;  # No cookies from this monster.

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
	## There is no persistent session to log out of, for this web-monitoring provider.
    }

    $self->{useragent} = undef;
    $self->{cookie}    = undef;
}

# Potentially transient state.  Sometimes desired to check if we need to log in,
# possibly because the session expired some time after we already logged in.  But
# this just tells if we think we have an active session; it doesn't actually
# reach over to Webmetrics and tell us if the session is really not still valid.
# (Furthermore, since even active sessions expire after a couple of hours, any such
# determination made here would be subject to race conditions.)  So all this tells us
# is that we definitely need to log in, if this call fails.  We still need to cope
# with unexpected session expiration in the middle of any of our other actions.
sub connected {
    my $self = $_[0];	# implicit argument
    return $self->{useragent} ? 1 : 0;
}

# This routine is nearly useless for our purposes.  Perhaps in some future version,
# it will yield up information on which devices are being actively monitored, and at
# what intervals.  In the meantime, we can get almost all the parts of this data that
# we actually need in a single call along with the basic last-state monitoring results
# from each of the Webmetrics monitoring stations, by wildcarding the device in the
# get_device_status() routine.
#
# The two pieces of data that may in some future version be usefully retrieved using
# this call are:
# (*) The Webmetrics polling interval for each device.  That is useful when we
#     create an RRD file, to set the proper RRD file step size.
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
	## Set up an HTTP request to list devices.

	my $timestamp = time;    
	my $signature = urlEncode( sha1_base64( $self->{username} . $self->{password} . $timestamp ) );

	my $req = HTTP::Request->new(
	    GET => "https://$self->{server}/v2/?method=maintenance.getServices&sig=$signature&username=$self->{username}&expanded=0"
	);

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

	## We set KeepRoot => 0 here to drop the top-level hash index of 'rsp',
	## to make subsequent access to these hash values easier.  We might get back
	## some non-XML content if the retrieval request is not successful, but in
	## that case we won't enter this branch of the code.
	eval { $xml = XMLin( $$xmlinput, KeyAttr => [], ForceArray => ['service'], KeepRoot => 0 ); };
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
	if ( not defined $xml->{stat} ) {
	    log_timed_message "ERROR:  Failed to retrieve device list; no 'stat' field is available.";
	    $self->{error} = 'Device list retrieval failure (no Status).';
	    $successful = 0;
	}
	elsif ( $xml->{stat} ne 'ok' ) {
	    my $message = (ref($xml->{error}) eq 'HASH') ? $xml->{error}{msg} : 'unknown error';
	    log_timed_message "ERROR:  Failed to retrieve device list; status $xml->{stat}:  $message";
	    ## Here we handle the case where we might have run into throttling from the provider.
	    ## See the Webmetrics documentation for the meanings of these codes:
	    ##     18 => Account has exceeded the request limit; you must wait to try this API again.
	    if ((ref($xml->{error}) eq 'HASH' and $xml->{error}{code} == 18) and not $recursive) {
		log_timed_message "NOTICE:  Failure was due to provider throttling; will attempt a retry.";
		sleep 2;
		## At most, a single-level recursive call to ourself.
		return get_device_list($self, 1);
	    }
	    else {
		$self->{error} = "Device list retrieval failure (bad Status; $message).";
	    }
	    $successful = 0;
	}
    }

    if ($successful) {
	## What we need back is a list of object device IDs that are subject
	## to monitoring, plus perhaps some amount of detail on them.
        ## FIX LATER:  In fact, what we have from Webmetrics at this point is just a list
        ## of configured devices, with no detail as to whether they are being actively
        ## monitored, nor at what monitoring interval.  We ought to press Webmetrics for
        ## efficient means to pull back that data in bulk (e.g., generalizations of the
        ## maintenance.getServiceStatus and maintenance.getMonitoringInterval calls), and
        ## make such additional calls here.  In the meantime, we just pretend that all
	## known devices are being monitored, and impose additional filtering downstream.

	my $services = $xml->{'service'};
	## This type coercion should no longer be needed, given our setting of ForceArray above.
	## But it doesn't hurt to test, just in case ...
	$services = [ $services ] if ref $services eq 'HASH';
	if (defined $services) {
	    foreach my $service (@$services) {
		my $name       = $service->{'name'};
		my $obj_device = $service->{'id'};
		my $monitor    = 'y';  # presumptive, at this point, but not necessarily true
		# my $interval   = $service->{'Interval'};
		if ($monitor eq 'y') {
		    $devices{$obj_device}{'Name'}     = $name;
		    # $devices{$obj_device}{'Interval'} = $interval;       # in minutes
		    # $devices{$obj_device}{'StepSize'} = $interval * 60;  # corresponding RRD step size, in seconds
		    $devices{$obj_device}{'Monitor'}  = $monitor;
		}
		else {
		    log_timed_message "INFO:  Skipping device $obj_device (\"$name\"), as it is not configured for monitoring.";
		}
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

    # Webmetrics does not define an integer identifier for each probed-from location,
    # so we manufacture one in this package while it operates.  We maintain consistency
    # of this created value during a given call to this routine, but not across calls.
    # That seems to be sufficient for our purposes here, since these values only serve
    # as localized hash keys within the data structures we produce here, and have no
    # other real relevance to the calling application.
    my %ObjLocations      = ();
    my $last_obj_location = 0;

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
	## Set up an HTTP request to get device statuses.

	my $timestamp = time;
	my $signature = urlEncode( sha1_base64( $self->{username} . $self->{password} . $timestamp ) );

	# About the possible API parameters in this call:
	#     lastsampletime	will not function as described in the API documentation;
	#			because of unpredictable latency between actual probes and
	#			when they are stored in the database, using lastsampletime
	#			will inevitably lead to some valid most-recent-probe data
	#			never being returned to our client.
	#     samplenum		we allow to default to 1, since all we care about is the
	#			most recent probe for each customer resource.  This sample
	#			will come from any of some number of web-monitoring provider
	#			locations, apparently sequenced in some type of random order.
	#			So we cannot expect a given resource+location pair to have a
	#			full set of associated monitoring data at the monitoring
	#			frequency specified for the resource.
	#     usebaselines	we are disabling this because it sounds like it may limit
	#			the returned data in some way we don't yet understand.
	#     items		for now, we don't want this detailed data.  In the future,
	#			if and when we want to capture fine-granularity timing,
	#			we will enable this.  For now, we allow it to default to 0.
	#     servicestate	we allow to default to "all", so we get data on services
	#			which are disabled as well as those that are enabled.
	#			This allows us to record in our log file what happened
	#			with services that are disabled.
	#     status		we allow to default to "all" so we can affirmatively
	#			process services whose monitoring status is okay, and
	#			to capture their respective access-time statistics.
	#     shared		we allow to default to 1, so as not to limit the set of
	#			services we see results for.  This could be critical if
	#			we use a dedicated user account just for our continual
	#			monitoring, and a separate account for casual inspection
	#			which might access the same APIs (to sidestep per-account
	#			throttling imposed by the web-monitoring provider).
	my $req = HTTP::Request->new(
	    GET => "https://$self->{server}/v2/?method=realtime.getdata&sig=$signature&username=$self->{username}&usebaselines=0"
	);

	if ( not do_timed_request( $self, 'Device status', $self->{useragent}, $req, \$resp ) ) {
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
	## We set KeepRoot => 0 here to drop the top-level hash index of 'rsp',
	## to make subsequent access to these hash values easier.  We might get back
	## some non-XML content if the retrieval request is not successful, but in
	## that case we won't enter this branch of the code.
	eval { $xml = XMLin( $$xmlinput, KeyAttr => [], ForceArray => ['service', 'sample', 'page', 'item'], KeepRoot => 0 ); };
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
	if ( not defined $xml->{stat} ) {
	    log_timed_message "ERROR:  Failed to retrieve device status; no 'stat' field is available.";
	    $self->{error} = 'Device status retrieval failure (no Status).';
	    $successful = 0;
	}
	elsif ( $xml->{stat} ne 'ok' ) {
	    my $message = (ref($xml->{error}) eq 'HASH') ? $xml->{error}{msg} : 'unknown error';
	    log_timed_message "ERROR:  Failed to retrieve device status; status $xml->{stat}:  $message";
	    ## Here we handle the case where we might have run into throttling from the provider.
	    ## See the Webmetrics documentation for the meanings of these codes:
	    ##     18 => Account has exceeded the request limit; you must wait to try this API again.
	    if ((ref($xml->{error}) eq 'HASH' and $xml->{error}{code} == 18) and not $recursive) {
		log_timed_message "NOTICE:  Failure was due to provider throttling; will attempt a retry.";
		sleep 62;
		## At most, a single-level recursive call to ourself.
		return get_device_status($self, $obj_device, 1);
	    }
	    else {
		$self->{error} = "Device status retrieval failure (bad Status; $message).";
	    }
	    $successful = 0;
	}
    }

    if ($successful) {
	my $CustomerResource = '';

	## The following fields are available at the top level:
	## error	only appears at the top level if there is a failure.  We handle it above.
	## login	is the Webmetrics username, reflected back to us.  We have no need of this data,
	##		so we just ignore this field.
	## service	is an array of hashes, one per service, containing service detail.  This data is
	##		critical to our processing here.
	## stat		is the overall status of the request:  'ok' => success, 'fail' => failure.
	##		We handle this field above, along with the error field.
	## time		is a UNIX epoch timestamp indicating when we asked Webmetrics for these results.
	##		This is not of great interest, and turns out to be dangerous to use to constrain
	##		the next query of this type (as suggested in the lastsampletime documentation),
	##		at least if we don't subtract $self->{latency} to allow for latency in the
	##		web-monitoring provider's probe servers to get their results into the database.
	##		Note that in worst-case overload situations at the provider, we have seen such
	##		latency go over half an hour.  Since we cannot predict its duration, we currently
	##		decline to use this capability.  A future version of this package might allow such
	##		filtering (with some preconfigured allowance for latency), and allow our downstream
	##		data processing to make its own determination about the unavailability of any data
	##		from the web-monitoring provider for a given customer resource.
	## timezone	is the Olson timezone name for the timezone (e.g., 'US/Pacific') in which
	## 		service.sample.date (presumably) and service.sample.time fields are expressed.
	## wmversion	is the version number of this Webmetrics API, currently '2.0' corresponding to /v2/
	##		above.  In some future release, we might use this to validate the results, but for
	##		now, we just ignore it.

	my $services = $xml->{'service'};
	## This type coercion should no longer be needed, given our setting of ForceArray above.
	## But it doesn't hurt to test, just in case ...
	$services = [ $services ] if ref $services eq 'HASH';

	my $timezone = $xml->{'timezone'};

	my $this_moment = DateTime->now( time_zone  => $timezone );
	my $this_second = $this_moment->second();
	my $this_minute = $this_moment->minute();
	my $this_hour   = $this_moment->hour();
	my $this_day    = $this_moment->day();
	my $this_month  = $this_moment->month();
	my $this_year   = $this_moment->year();
	my $last_year   = $this_year - 1;

	my $hostmap = $self->{hostmap};
	$hostmap = {} if not defined $hostmap;
	$hostmap = {} if ref $self->{hostmap} ne 'HASH';
	my $servicemap = $self->{servicemap};
	$servicemap = {} if not defined $servicemap;
	$servicemap = {} if ref $self->{servicemap} ne 'HASH';

	if (defined $services) {
	    foreach my $service (@$services) {
		## The following fields are available:
		##
		## active indicates the status of the services:  0 => off, 1 => on, 2 = starting (has no data).
		## inmaintwindow indicates whether the service is in a maintenance window:  0 => no, 1 => yes.
		## name is the Webmetrics name of the service.
		## sample is an array of hashes represending sample points for this service, generally at most
		##     one (the latest) per service [this field may be missing from any given service].
		## serviceid is the Webmetrics integer identifier corresponding to name.
		## type is a string describing the Webmetrics monitoring performed for this service.
		##
		## sample fields are:
		##
		## STRIKE is an optional human-readable field, which if present tells why the monitoring probe
		##     sample represents a STRIKE status.
		## ERROR is an optional human-readable field, which if present tells why the monitoring probe
		##     sample represents an ERROR status.
		## date is the calendar date of the probe, in a very crude 'm/d' (month/day) format,
		##     presumably expressed in the global response timezone.
		## status is the result of this sample probe:  'OK' => okay, 'STRIKE' => a soft error has occurred,
		##     'ERROR' => a hard error has occurred.
		## time is the clock time of the probe, in 'HH:MM:SS.ss' format (not sure about leading
		##     zero-padding in all fields), expressed in the global response timezone.
		## transaction is a hashref container for probe details.
		##
		## transaction fields are:
		##
		## loadtime is a measure of how long the entire transaction took to run (essentially, the sum of
		##     all the page.loadtime fields within the transaction), expressed in time units specified by
		##     the transaction.units field.  This field may be empty, particularly if the sample represents
		##     a STRIKE or ERROR status.
		## page is an array of hashes representing individual web fetches that make up an entire monitoring
		##     probe transaction.
		## server is a human-readable string (city, plus state or country) that tells where the probe
		##     originated from, within the Webmetrics monitoring infrastructure.
		## units is the name of the timescale in which loadtime is expressed, typically 'Seconds'.
		##
		## For this version, we will not delve deeper, into the page fields and subfields, which are
		## where even more detailed metrics are buried.
		##
		my $name       = $service->{'name'};
		my $serviceid  = $service->{'serviceid'};
		my $monitor    = $service->{'active'} == 1 ? 'y' : 'n';
		my $controlled = $service->{'active'} == 2 ? 'pending' : $service->{'active'} == 1 ? 'enabled' : 'disabled';

		## The API says that the first sample returned is always the most recent.
		my $sample = $service->{sample}[0];
		if (not defined $sample) {
		    log_timed_message "INFO:  Skipping service $serviceid (\"$name\"), as no sample is available.";
		    next;
		}

		my $sample_strike = $sample->{STRIKE};
		my $sample_error  = $sample->{ERROR};
		my $sample_date   = $sample->{date};
		my $sample_status = $sample->{status};
		my $sample_time   = $sample->{time};
		my $transaction   = $sample->{transaction};
		if (not defined $transaction) {
		    ## Every sample is supposed to contain exactly one transaction, but hey, just in case ...
		    log_timed_message "INFO:  Skipping service $serviceid (\"$name\"), as no sample transaction is available.";
		    next;
		}
		my $loadtime      = $transaction->{loadtime};
		my $server        = $transaction->{server};
		my $units         = $transaction->{units};

		## We have confirmation from Neustar that "Seconds" is currently the only supported value,
		## but let's be sure things haven't changed since then, since our logic below makes no
		## adjustment for any other potential value.
		if ( !defined($units) or $units ne 'Seconds' ) {
		    log_timed_message
		      "ERROR:  Skipping service $serviceid (\"$name\"), as we have \"$units\" instead of \"Seconds\" for timing units.";
		    next;
		}

		## Descrip is the string name of the customer resource that Webmetrics is probing.
		## ObjDevice is the Webmetrics integer identifier corresponding to Descrip.
		## Location is the string location of the Webmetrics server from which this probe was performed.
		## ObjLocation is the Webmetrics integer identifier corresponding to Location (we manufacture this number).
		## Monitor is 'y' if this Customer Site has been and is to be monitored from this Location, 'n' otherwise.
		## LastStatusCode is a status code reflecting the result of the last location->resource probe.
		## LastStatus is a string interpretation of LastStatusCode (presumably providing a fuller explanation).

		my $Descrip        = $name;
		my $ObjDevice      = $serviceid;
		my $Location       = $server;
		my $ObjLocation    = undef;
		if (defined $Location) {
		    $ObjLocation = $ObjLocations{$Location};
		    if (not defined $ObjLocation) {
			$ObjLocation = $ObjLocations{$Location} = ++$last_obj_location;
		    }
		}
		else {
		    $Location    = 'unknown server';
		    $ObjLocation = 0;
		}
		my $Monitor        = $monitor;
		my $LastStatusCode = $sample_status;
		my $LastStatus     = defined($sample_error) ? $sample_error : defined($sample_strike) ? $sample_strike : 'Okay';
		my $Timings_Total  = $loadtime;

		if ($Monitor ne 'y') {
		    log_timed_message "INFO:  Webmetrics Monitor is $controlled for device $ObjDevice (\"$Descrip\")";
		    log_timed_message "       as seen from location $ObjLocation (\"$Location\"); ignoring status." if defined $ObjLocation;
		    next;
		}

                # Webmetrics restricts service names to alphanumerics plus hyphens and underscores.  Our host_name() routine,
		# called just below, will translate underscores to dashes, as they will not be valid characters in a future
                # version of Monarch where hostnames are constrained to only contain valid Internet hostname characters.

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

		# Normalize the sample date/time values to a GMT UNIX epoch-based timestamp.
		my $LastStatusUTC      = undef;
		my $LastStatusUTCEpoch = undef;
		my $readable_utc_time  = '(no sample-point date/time)';
		$sample_time =~ s/\..*//;  # drop sub-seconds
		if ($sample_time =~ /^(\d{2}):(\d{2}):(\d{2})$/) {
		    my $sample_second = $3;
		    my $sample_minute = $2;
		    my $sample_hour   = $1;
		    if ($sample_date =~ m{(\d+)/(\d+)}) {
			my $sample_day   = $2;
			my $sample_month = $1;
			my $sample_year =
			    $sample_month  > $this_month  ? $last_year :
			    $sample_month  < $this_month  ? $this_year :
			    $sample_day    > $this_day    ? $last_year :
			    $sample_day    < $this_day    ? $this_year :
			    $sample_hour   > $this_hour   ? $last_year :
			    $sample_hour   < $this_hour   ? $this_year :
			    $sample_minute > $this_minute ? $last_year :
			    $sample_minute < $this_minute ? $this_year :
			    $sample_second > $this_second ? $last_year :
			    $sample_second < $this_second ? $this_year :
			    $this_year;  # exact timestamp match; presumptive year
			$LastStatusUTC = DateTime->new(
			    year       => $sample_year,
			    month      => $sample_month,
			    day        => $sample_day,
			    hour       => $sample_hour,
			    minute     => $sample_minute,
			    second     => $sample_second,
			    nanosecond => 0,
			    time_zone  => $timezone
			);
			$LastStatusUTC->set_time_zone($utc_timezone);
			$LastStatusUTCEpoch = $LastStatusUTC->epoch();
			## Stringification for the pattern matching coerces the DateTime into an ISO-8601 string
			## ("YYYY-MM-DDThh:mm:ss") expressed in the specified timezone, which we then modify.
			($readable_utc_time = $LastStatusUTC) =~ s/T/ /;
			$readable_utc_time .= " UTC" if $readable_utc_time;
		    }
		}
		if (!defined($LastStatusUTC) and $self->{debug}) {
		    ## We should never get here.
		    log_timed_message "ERROR:  device status content for $Descrip (ObjDevice $ObjDevice)";
		    log_timed_message "        as seen from $Location (ObjLocation $ObjLocation) is:";
		    log_message Dumper($service);
		    $readable_utc_time = "bad sample date/time";
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
		$status_location->{Timings_Total}      = $Timings_Total;
	    }
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

# Package-internal routine to convert a resource string returned from Webmetrics into a reasonable valid host name.
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

# Package-internal routine to convert a location string returned from Webmetrics into a reasonable valid service name.
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
    my @devices       = ();     # Webmetrics devices for which we have collected metric data
    my %metrics       = ();     # ${host}_${service} => arrayref to RRD timestamp+data update strings
    my @messages      = ();     # Nagios host+service check messages

    # This routine is currently just scaffolding for extension in some future release.

    return \@devices, \%metrics, \@messages;
}

# We return both host and service evaluations of the state data in a single call,
# because there is not a one-to-one mapping between host status and service severity.
sub nagios_interpretation_of_webmetrics_statuses {
    my $self           = $_[0];    # implicit argument
    my $Status         = $_[1];
    my $Status_verify  = $_[2];
    my $Status_warning = $_[3];

    # Currently for this web-monitoring provider, we don't have $Status_verify or $Status_warning values to worry about.

    # No status provided.
    return nagios_plugin_numeric_host_status('UNREACHABLE'), nagios_plugin_numeric_service_severity('UNKNOWN') if not defined $Status;

    # No error seen from this probe location.
    return nagios_plugin_numeric_host_status('UP'), nagios_plugin_numeric_service_severity('OK') if $Status eq 'OK';

    # Initial error condition seen from this probe location.  (There may be a couple of STRIKEs before an ERROR is declared.)
    # We don't really know if the host is up or down at this point (the STRIKE cound be a failure to connect, for instance),
    # but that's why we put the service in a warning state.  We leave Nagios to sort out a sequence of soft and hard failures.
    return nagios_plugin_numeric_host_status('DOWN'), nagios_plugin_numeric_service_severity('WARNING') if $Status eq 'STRIKE';

    # Confirmed error condition seen from this probe location.
    return nagios_plugin_numeric_host_status('DOWN'), nagios_plugin_numeric_service_severity('CRITICAL') if $Status eq 'ERROR';

    # Any other exceptional condition (we should never get here).
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

    # This routine is currently just scaffolding for extension in some future release.
    return 0;
}

sub create_and_update_rrd_files {
    my $self       = $_[0];    # implicit argument
    my $obj_device = $_[1];    # required argument
    my $metrics    = $_[2];    # required argument

    # This routine is currently just scaffolding for extension in some future release.
}

sub update_last_access_time {
    my $self       = $_[0];    # implicit argument
    my $obj_device = $_[1];    # optional argument

    # This routine is currently just scaffolding for extension in some future release.
}

1;

__END__

=head1 NAME

Webmetrics - Access Webmetrics service to fetch web-sensing data

=head1 SYNOPSIS

    use GW::Webmetrics;

    # $hostmap is a source => target hash to map a
    # customer resource name into a valid host name.
    # $servicemap is a source => target hash to map an
    # Webmetrics location name into a valid service name.
    # Default transformations will be used for both the
    # hostmap and servicemap if the source is not found
    # in the respective hash.
    my $dbhost      = 'localhost';
    my $dbname      = 'webmetrics';
    my $dbuser      = 'my_user';
    my $dbpass      = 'my_pass';
    my $latency     = 90;
    my $server      = 'api.webmetrics.com';
    my $timeout     = 60;  # applies to $server access
    my $timezone    = "US/Pacific";  # Webmetrics master timezone
    my $hostmap     = {};
    my $servicemap  = {};
    my $rollup      = 'none';
    my $rrdbasedir  = "/usr/local/groundwork/rrd";

    my $debug_level = 0;  # 1 => print extra debug messages, 0 => don't

    my $webmetrics = GW::Webmetrics->new (
	$dbhost, $dbname, $dbuser, $dbpass,
	$latency, $server, $timeout, $timezone,
	$hostmap, $servicemap, $rollup, $rrdbasedir,
	$debug_level
    );
    $webmetrics->dbconnect();
    $webmetrics->login($username, $password);

    $webmetrics->set_device_list( $webmetrics->get_device_list() );

    my $device_statuses = $webmetrics->set_device_status( $webmetrics->get_device_status() );

    my $obj_device = 12345;
    my $obj_locations = [ 60, 35, 42 ];
    my ($metrics, $messages) = $webmetrics->get_device_metrics($obj_device, $obj_locations);
    $webmetrics->create_and_update_rrd_files($obj_device, $metrics);

    $webmetrics->logout();
    my ($warning_msg, $error_msg) = $webmetrics->daemon_status();
    $webmetrics->clear_daemon_status();
    $webmetrics->dbdisconnect();

=head1 DESCRIPTION

This module encapsulates the details of accessing the Webmetrics servers
to fetch web-site monitoring data.  A limited set of the full data
available from Webmetrics is fetched, but it should be sufficient to
process to generate GroundWork alerts and performance-data graphs.

Some of the code, terminology, and documentation in this package is
inherited from a more-general framework on which this package is based.
Those conventions are kept intact here even though the actual usage
for Webmetrics is a bit different, just in case we later extend this
package.

Currently, all RRD processing is handled outside of this package, by
having the calling application append performance data to service-check
results it sends to Nagios, and having the ordinary downstream processing
of that data be in play.

=head1 SUBROUTINES/METHODS

=over

=item new()

The B<new()> constructor returns an object reference which can be used
to access the methods for initializing other aspects of the object and
for pulling data from Webmetrics.  If validation of the arguments fails,
the constructor will B<die>.

The B<new()> constructor must be invoked as:

    my $webmetrics = GW::Webmetrics->new ($dbhost, $dbname, $dbuser, $dbpass,
      $latency, $server, $timeout, $timezone, $hostmap, $servicemap, $rollup,
      $rrdbasedir, $debug_level);

because if it is invoked instead as:

    my $webmetrics = GW::Webmetrics::new ($dbhost, $dbname, $dbuser, $dbpass,
      $latency, $server, $timeout, $timezone, $hostmap, $servicemap, $rollup,
      $rrdbasedir, $debug_level);

no invocant is supplied as the implicit first argument.

=item $webmetrics->dbconnect()

This method must be called to connect to the database used to store
information about time intervals which have already been covered
in previous probing of Webmetrics for events for particular devices.
A database connection is needed before the get_device_metrics() call
can proceed.

=item $webmetrics->dbdisconnect()

This method must be called to disconnect from the database used to store
information about time intervals which have already been covered in
previous probing of Webmetrics for events for particular devices.

=item $webmetrics->dbconnected()

This method returns true if the $webmetrics object currently has an open
connection to the database, false otherwise.  Calling this routine
just returns internal state, and does not actually probe the database
connection to verify that it is still operational.

=item $webmetrics->login($username, $password)

This method must be called to log in to the Webmetrics servers and obtain
server credentials used for the get_...() calls.

=item $webmetrics->logout()

This method should be called to log out from the Webmetrics servers when
no further probing will occur, to clean up session information on the
Webmetrics servers.

=item $webmetrics->connected()

This method returns true if the $webmetrics object is currently logged
in to Webmetrics, false otherwise.  Calling this routine just returns
internal state, and does not actually probe Webmetrics to verify that
the connection is still valid.

=item $webmetrics->get_device_list()

This routine is nearly useless for our purposes.  Perhaps in some future
version, it will yield up information on which devices are being actively
monitored, and at what intervals.  In the meantime, we can get almost all
the parts of this data that we actually need in a single call along with the
basic last-state monitoring results from each of the Webmetrics monitoring
stations, by wildcarding the device in the get_device_status() routine.

The two pieces of data that may in some future version be usefully retrieved
using this call are:
(*) The Webmetrics polling interval for each device.  That is useful when we
    create an RRD file, to set the proper RRD file step size.
(*) Whether monitoring is enabled at the device level.  That is useful to tell
    whether we need to add a host for this resource in the "monarch" database,
    if it is not already present there.
For those reasons, once we extend this code, it will still be necessary
to call get_device_list() and set_device_list() periodically (say, once at
the beginning of each major processing cycle), in case new devices show up.

=item $webmetrics->set_device_list( $webmetrics->get_device_list() )

This method allows the caller to pass back the result of a call to
get_device_list() and have it saved (or cleared) for use in this package.
This allows the caller to control how often the list is refreshed.

=item $webmetrics->get_device_status($obj_device)

This method probes Webmetrics to pull back current status data for the
indicated device, or for all devices if $obj_device is not supplied.
In practice, the $obj_device is usually not supplied.  Calling the routine
this way returns complete data on all the devices, including both their
object device IDs and their respective status data, so there is no real
need for a preceding call to another routine to essentially just get the
list of device IDs.  Calling this routine separately for each device to
obtain the status data for just that device would be far less efficient.

=item $webmetrics->get_device_metrics($obj_device, $obj_locations);

This method probes Webmetrics to pull back recent metric data for the
indicated device.  The current design for the calling application assumes
that only one device can be probed for at a time, though that assumption will
be revisited when we implement this routine.  The interval for which metric
data is retrieved starts at the end of the last interval for which data was
successfully retrieved.  That endpoint is maintained for each device within
this module, as long as the module application is alive.  To persist the
endpoint data across application bounces, the time-interval data must be
flushed to the database after corresponding RRD files have been updated and
GroundWork Monitor alerts have been generated.  The update_last_access_time()
routine is used for such flushing.

=item $webmetrics->create_and_update_rrd_files($obj_device, $metrics)

This method persists metric data returned by get_device_metrics() into
corresponding RRD files, creating those files if necessary.

=item $webmetrics->update_last_access_time($obj_device)

This method is used to flush new-interval data back to the database,
to persist it across application bounces.  The critical data stored
in the database is the end of the last time interval for which data
has already been fetched and processed (sent to GroundWork Monitor).
The $obj_device argument is optional.  If specified, only the data for
that one object will be flushed.  If not specifed, the data for all
objects not previously flushed will now be flushed to the database.
Generally speaking, this routine is only called internally from within
the B<GW::Webmetrics> package, since it depends on certain instance
data, and only the package itself knows when that data has been
updated and ought to be flushed.

=item $webmetrics->daemon_status()

This method returns the last warning and error messages, if any,
that occurred within the GW::Webmetrics package since either the start
of the program or the last call to the clear_daemon_status() routine.
If no warnings or errors have occurred, the respective returned messages
are undefined.

=item $webmetrics->clear_daemon_status()

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

The Webmetrics API operates using timestamps expressed in a local timezone,
which creates ambiguous timestamps and probably invalid time intervals
around Daylight Savings Time transitions.  Webmetrics understands this
issue and may address it by using some form of UTC in a future release.
Without that capability, we cannot guarantee that alarms or metrics will
be processed correctly around DST transitions.

Webmetrics allows a device ID to be effectively wildcarded while probing
for device status, and we take advantage of that for efficiency and speed.
We have not yet investigated whatever mechanisms might be available for
retrieving more detailed metrics for the purposes of RRD data capture.

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

B<GW::Webmetrics> depends on a variety of CPAN packages outside of those
provided in a base GWMEE 6.4 release.  It also uses B<GW::Logger>, which
is considered to be a beta version of what will eventually become the
B<GroundWork::Logger> package.  The calling application is assumed to
have initialized the B<GW::Logger> package.

=head1 AUTHOR

GroundWork Open Source, Inc. <www.groundworkopensource.com>

=head1 VERSION

0.1.0

=cut


#!/usr/local/groundwork/perl/bin/perl -w --

# Copyright (c) 2013-2017 GroundWork Open Source (www.groundworkopensource.com).
# All rights reserved.  Use is subject to GroundWork commercial license terms.

use strict;
use warnings;

use lib '/usr/local/groundwork/ticketing/perl/lib';

use Config::General;
use IO::Socket;
use Data::Dumper;

use GW::DBObject;
use GW::HelpDeskUtils;
use GW::LogFile;
use GW::Nagios;

# The primary configuration file
my $config_file = "/usr/local/groundwork/ticketing/config/twoway_helpdesk.conf";

# A global hash representing the contents of the config_file
my %params = ();

# A container for holding database objects
my %dBObjects = ();

# Parameter hash created by scanning conf file for help desk module
my %moduleParams = ();

# Handle to LogFile object
my $logObj = undef;

# Array to hold console events retrieved from HelpDeskBridgeDB
my @itemsOfInterest = ();

# Array to hold the set of strings representing resolved ticket states
# This is populated from the ResolvedStates parameter in the conf file.
my @resolvedStates = ();

# Number of events that were moved to the TerminalOperationStatus state
# (typically either ACCEPTED or CLOSED) on the console.
my $numEventsToTerminate = 0;

sub main {
    readConfFile();

    if ( isProcessAlreadyRunning() ) {
	print "\n";
	print "Identical process is already executing.  Exiting without performing any actions.\n";
	print "\n";
	exit;
    }

    initialize();

    my $startTime = time();

    scanBridgeDBForItemsOfInterest();
    queryHelpDeskForUpdates();
    markEventsInResolvedState();
    markEventsToTerminate();
    acceptEventsInFoundation();
    addEventCommentsToNagios();
    updateHelpDeskBridgeDB();

    my $stopTime  = time();
    my $deltaTime = $stopTime - $startTime;

    $logObj->log("Number of Events Moved to $params{'TerminalOperationStatus'} state: $numEventsToTerminate");
    $logObj->log("Total Script Execution Time: $deltaTime second(s)");
    $logObj->log("");
    $logObj->rotateLogFile();
}

sub readConfFile {
    ## Read the conf files
    my $config = new Config::General(
	-ConfigFile      => $config_file,
	-InterPolateVars => 1,
	-AutoTrue        => 1,
	-SplitPolicy     => 'equalsign'
    );
    %params = $config->getall();

    # Read in the configuration file for the external help desk module
    my $moduleConfig = new Config::General(
	-ConfigFile      => $params{'HelpDeskModuleConfFile'},
	-InterPolateVars => 1,
	-AutoTrue        => 1,
	-SplitPolicy     => 'equalsign'
    );
    %moduleParams = $moduleConfig->getall();
}

sub initialize {
    ## Instantiate the LogFile object
    $logObj = new GW::LogFile();
    $logObj->setLogFile( $params{'HelpDeskLogFile'} );
    $logObj->setLogRotation( $params{'MaxLogFileSize'}, $params{'MaxLogFilesToRetain'} );

    # Replace the escaped quotes: \' with just a single quote: '
    # FIX LATER:  I don't think this does anything useful, as constructed.
    # What was the intent here?  We might need s/\\'/'/g instead.
    $params{'ResolvedStates'} =~ s/\'/'/g;

    # Since $params{'ResolvedStates'} will be stuck directly into an SQL statement
    # later on, we need to validate that it is constructed exactly as expected,
    # to avoid any SQL injection attacks through this vector.  For convenience,
    # we allow spaces around the commas that separate successive values, though
    # this is not documented in the configuration file.
    $params{'ResolvedStates'} = trim( $params{'ResolvedStates'} );
    if ($params{'ResolvedStates'} !~ /^'[^']+'(?:\s*,\s*'[^']+')*$/) {
	$logObj->log("FATAL:  ResolvedStates is not constructed properly.");
	$logObj->log("        See the configuration file for the required format");
	$logObj->log("        ($config_file).");
	exit 1;
    }

    # Read the resolved states string and put each state string
    # into an array.
    my @fields = split( /,/, $params{'ResolvedStates'} );

    foreach my $resolvedState (@fields) {
	$resolvedState = trim($resolvedState);
	my $resolvedStateLen = length($resolvedState);

	if ( $resolvedStateLen > 2 ) {
	    ## Remove the single quotes around the state name
	    $resolvedState = substr( $resolvedState, 1, $resolvedStateLen - 2 );
	}

	# Store resolved state strings in an array
	push( @resolvedStates, $resolvedState );
    }

    # We disallow any construction that could cause an XML injection failure.
    if (!defined( $params{'TerminalOperationStatus'} )
	or $params{'TerminalOperationStatus'} eq ''
	or $params{'TerminalOperationStatus'} =~ /[<>'"&\[\]]/) {
	$logObj->log("FATAL:  TerminalOperationStatus is not constructed properly.");
	$logObj->log("        See the configuration file for the required format");
	$logObj->log("        ($config_file).");
	exit 1;
    }

    # We disallow any construction that could cause an SQL injection failure.
    if (!defined( $params{'TicketRetentionTime'} ) or $params{'TicketRetentionTime'} !~ /^\d+$/) {
	$logObj->log("FATAL:  TicketRetentionTime is not constructed properly.");
	$logObj->log("        See the configuration file for the required format");
	$logObj->log("        ($config_file).");
	exit 1;
    }

    initBridgeDBObject();
    initCollageDBObject();
}

sub initBridgeDBObject {
    my $bridgeDBConfig = new Config::General( $params{'BridgeDBCredentials'} );
    my %bridgeDBParams = $bridgeDBConfig->getall();

    my %dBParams = ( 'AutoCommit' => 1 );

    # Create and initialize DBObject
    $dBObjects{'bridgeDB'} = new GW::DBObject();

    $dBObjects{'bridgeDB'}->setDBHost( $bridgeDBParams{'bridgeDB.dbhost'} );
    $dBObjects{'bridgeDB'}->setDBName( $bridgeDBParams{'bridgeDB.database'} );
    $dBObjects{'bridgeDB'}->setDBUser( $bridgeDBParams{'bridgeDB.username'} );
    $dBObjects{'bridgeDB'}->setDBPass( $bridgeDBParams{'bridgeDB.password'} );
    $dBObjects{'bridgeDB'}->setDBParams( \%dBParams );

    # Create a cached dB connection
    $dBObjects{'bridgeDB'}->connect();
}

sub initCollageDBObject {
    my $gwCollageDBConfig = new Config::General( $params{'GWCollageDBCredentials'} );
    my %gwCollageDBParams = $gwCollageDBConfig->getall();

    my %dBParams = ( 'AutoCommit' => 1 );

    # Create and initialize DBObject
    $dBObjects{'collageDB'} = new GW::DBObject();

    $dBObjects{'collageDB'}->setDBHost( $gwCollageDBParams{'collage.dbhost'} );
    $dBObjects{'collageDB'}->setDBName( $gwCollageDBParams{'collage.database'} );
    $dBObjects{'collageDB'}->setDBUser( $gwCollageDBParams{'collage.username'} );
    $dBObjects{'collageDB'}->setDBPass( $gwCollageDBParams{'collage.password'} );
    $dBObjects{'collageDB'}->setDBParams( \%dBParams );

    # Create a cached dB connection
    $dBObjects{'collageDB'}->connect();
}

sub scanBridgeDBForItemsOfInterest {
    my $resolvedStates = $params{'ResolvedStates'};

    # lastchangetime is included here in service of a means to tell which items
    # are very old and can be removed automatically.
    my $query = qq{
	SELECT
	    logmessageid         as "LogMessageID",
	    deviceidentification as "DeviceIdentification",
	    operator             as "Operator",
	    ticketno             as "TicketNo",
	    ticketstatus         as "TicketStatus",
	    clientdata           as "ClientData",
	    lastchangetime       as "LastChangeTime"
	FROM   HelpDeskLookupTable
	WHERE  TicketStatus NOT IN ($resolvedStates)
    };

    my $dBObj = $dBObjects{'bridgeDB'};

    # Connect to the cached dB connection
    $dBObj->connect();

    my $dBHandle = $dBObj->getHandle();

    my $sth = undef;
    $sth = $dBHandle->prepare($query);
    $sth->execute();
    while ( my $row = $sth->fetchrow_hashref() ) {
	$row->{'hasChanged'}   = 0;
	$row->{'isResolved'}   = 0;
	$row->{'canTerminate'} = 0;

	push( @itemsOfInterest, $row );
    }
    $sth->finish();
}

sub queryHelpDeskForUpdates {
    ## Instantiate the client specific HelpDesk object
    my $helpDeskModule = $params{'HelpDeskModule'};

    eval "use $helpDeskModule";
    if ($@) {
	chomp $@;
	my $exception = $@;
	$logObj->log("ERROR:  Could not load module: $helpDeskModule");
	$logObj->log($exception);
    }
    else {
	my $numEvents = scalar @itemsOfInterest;

	# Instantiate HelpDesk module and delegate to it only if
	# valid unresolved events were found in the bridge database
	if ($numEvents) {
	    my $helpDeskObj = $helpDeskModule->new();
	    $helpDeskObj->setLogObj($logObj);
	    $helpDeskObj->setDBObj( $dBObjects{'bridgeDB'} );

	    eval { $helpDeskObj->checkStatusOfTickets( \%moduleParams, \@itemsOfInterest ); };
	    if ($@) {
		chomp $@;
		my $exception = $@;
		$logObj->log("ERROR:  An error was encountered while invoking checkStatusOfTickets:");
		$logObj->log($exception);
	    }

	    # This optional logging is here just for development debugging.
	    # Possibly, this data might only be available for certain ticketing providers
	    # (e.g., ServiceNow).
	    if (0) {
		foreach my $event (@itemsOfInterest) {
		    if ( $event->{'close_notes'} ) {
			$logObj->log("Close notes for ticket $event->{TicketNo}:  '$event->{close_notes}'");
		    }
		    if ( $event->{'resolved_at'} ) {
			$logObj->log("Resolved at for ticket $event->{TicketNo}:  '$event->{resolved_at}'");
		    }
		}
	    }
	}
    }
}

sub markEventsInResolvedState {
    foreach my $event (@itemsOfInterest) {
	if ( $event->{'hasChanged'} ) {
	    if ( isContainedIn( $event->{'TicketStatus'}, \@resolvedStates ) ) {
		$event->{'isResolved'} = 1;
	    }
	}
    }
}

sub markEventsToTerminate {
    ## An array to hold pointers to all resolved events
    my %resolvedEvents;

    foreach my $event (@itemsOfInterest) {
	if ( $event->{'isResolved'} ) {
	    $resolvedEvents{ $event->{'LogMessageID'} } = $event;
	}
    }

    # Continue only if any events moved into the resolved state.
    if (%resolvedEvents) {
	## Query to make sure LogMessage exists in Foundation.  This is done to make sure
	## that non-existent Foundation events are not accepted.  (It's possible the event
	## got purged from Foundation between the time the ticket was originally created
	## and when it finally got resolved.)
	my $logMessageIDStr = join( ',', map { "'" . $_ . "'" } keys %resolvedEvents );

	# We probe for host/service names as well here so we can use them for sending acknowledgements to
	# Nagios.  Bear in mind that for any given event in the database, we might have null values in
	# the database for both hostname and servicedescription.  Also, inasmuch as this is the only good
	# way to retrieve host/service names for the specified events, it is possible that an aged-out
	# event won't get acknowledged in Nagios this way, but there's not much we can do about that, as
	# we don't have another source of association between events and hosts or host-services.
	my $query = qq{
	    SELECT
		lm.logmessageid       AS "LogMessageID",
		h.hostname            AS "HostName",
		ss.servicedescription AS "ServiceDescription"
	    FROM
		logmessage lm
		left join host h on h.hostid = lm.hoststatusid
		left join servicestatus ss on ss.servicestatusid = lm.servicestatusid
	    WHERE LogMessageID IN ($logMessageIDStr)
	};

	my $dBObj = $dBObjects{'collageDB'};

	# Connect to the cached dB connection
	$dBObj->connect();

	my $dBHandle = $dBObj->getHandle();

	my $sth = undef;
	$sth = $dBHandle->prepare($query);
	$sth->execute();

	# If LogMessage exists in Foundation, go ahead and mark the event
	# as acceptable.  Otherwise don't send the accept XML for this event.
	while ( my $row = $sth->fetchrow_hashref() ) {
	    my $logMessageID       = $row->{'LogMessageID'};
	    my $HostName           = $row->{'HostName'};
	    my $ServiceDescription = $row->{'ServiceDescription'};
	    if ( defined( $resolvedEvents{$logMessageID} ) ) {
		my $event = $resolvedEvents{$logMessageID};
		$event->{'canTerminate'}       = 1;
		$event->{'hostname'}           = $HostName if defined $HostName;
		$event->{'servicedescription'} = $ServiceDescription if defined $ServiceDescription;
	    }
	}
	$sth->finish();
    }
}

sub updateHelpDeskBridgeDB {
    my $update = qq{UPDATE HelpDeskLookupTable SET TicketStatus=? WHERE LogMessageID=?};

    my $dBObj = $dBObjects{'bridgeDB'};

    # Connect to the cached dB connection
    $dBObj->connect();

    my $dBHandle = $dBObj->getHandle();

    my $sth = undef;
    $sth = $dBHandle->prepare($update);

    # Update any items that may have changed in the BridgeDB
    foreach my $event (@itemsOfInterest) {
	if ( $event->{'hasChanged'} ) {
	    $sth->execute( $event->{'TicketStatus'}, $event->{'LogMessageID'} );
	}
    }
    $sth->finish();

    # FIX LATER:  Insisting that the LastChangeTime be sufficiently old (past a configurable age
    # limit) allows us to retain these rows for some time, during which they might be re-opened.
    # During that time, while the resolved tickets are still in the HelpDeskLookupTable, we
    # ought to check during our earlier processing to see if they have been re-opened within
    # the ticketing system, and take appropriate steps to change the event Operation Status in
    # GroundWork Monitor accordingly.
    #
    # FIX LATER:  Should the HelpDeskLookupTable.LastChangeTime field be updated to current time
    # when a ticket is resolved, so our deletion test here is based on when the ticket processing
    # is seen to be done instead of when the ticket was initially opened?  Or do we already have
    # an update_lastchangetime trigger installed for that purpose?
    #
    # The last clause in the DELETE statement reflects what in MySQL would have been:
    # AND (UNIX_TIMESTAMP() - UNIX_TIMESTAMP(LastChangeTime)) > $params{'TicketRetentionTime'}
    #
    my $resolvedStates = $params{'ResolvedStates'};
    my $delete = qq{
	DELETE FROM HelpDeskLookupTable WHERE TicketStatus IN ($resolvedStates)
	AND (extract(epoch from localtimestamp(0)) - extract(epoch from LastChangeTime)) > $params{'TicketRetentionTime'}
    };
    $dBHandle->do( $delete );

    # FIX MINOR:  For cleanliness, we ought to disconnect from the database here,
    # so we don't build up lots of connections.  We're saved only by the fact that
    # this script exits soon after this routine is called.
}

sub acceptEventsInFoundation {
    my @eventsToTerminate = ();

    # Create a list of events to be accepted
    foreach my $event (@itemsOfInterest) {
	if ( $event->{'canTerminate'} ) {
	    push( @eventsToTerminate, $event );
	}
    }

    $numEventsToTerminate = scalar @eventsToTerminate;

    # If there is 1 or more events to accept, then inform Foundation.
    # Please note that markEventsToTerminate has already made sure that
    # the LogMessage currently exists in Foundation.
    if ($numEventsToTerminate) {
	my $logMessageIDStr = getLogMessageString( \@eventsToTerminate );

	addApplicationTypeToEvents( \@eventsToTerminate, $logMessageIDStr );

	my $acceptXML = constructTerminateXML( \@eventsToTerminate );

	sendToFoundation($acceptXML);
    }
}

sub constructTerminateXML {
    my $eventsToTerminate = shift(@_);
    my %appTypeBins    = ();

    # Put the consoleEvents into bins defined by Application Type.
    # Autovivification dramatically simplifies this loop.
    foreach my $event (@$eventsToTerminate) {
	push @{ $appTypeBins{ $event->{'ApplicationType'} } }, $event;
    }

    my $adapterEnvelope = "<Adapter Session=\"1002\" AdapterType=\"SystemAdmin\">";
    my $commandEnvelope = "";

    # For each bin, construct the appropriate Command XML.
    foreach my $appType ( keys %appTypeBins ) {
	my $bin = $appTypeBins{$appType};

	$commandEnvelope .= "<Command Action=\"MODIFY\" ApplicationType=\"$appType\"> ";

	my $logMessageXML = "";
	foreach my $event (@$bin) {
	    my $id = $event->{'LogMessageID'};
	    $logMessageXML .= "<LOGMESSAGE LogMessageId=\'$id\' OperationStatus=\'$params{'TerminalOperationStatus'}\' />";
	}

	$commandEnvelope .= "$logMessageXML</Command>";
    }

    $adapterEnvelope .= "$commandEnvelope</Adapter>";

    return $adapterEnvelope;
}

sub getLogMessageString {
    my $eventsToTerminate = shift(@_);

    my $idString = "";

    foreach my $event (@$eventsToTerminate) {
	$idString .= "'" . $event->{'LogMessageID'} . "',";
    }

    if ( length($idString) ) { chop($idString); }

    return $idString;
}

sub addApplicationTypeToEvents {
    my $eventsToTerminate = shift(@_);
    my $logMessageIDStr   = shift(@_);

    my $query = qq|
	SELECT LogMessageID AS "LogMessageID", ApplicationType.Name AS "ApplicationType"
	FROM LogMessage, ApplicationType
	WHERE LogMessage.ApplicationTypeID = ApplicationType.ApplicationTypeID
	AND LogMessageID IN ($logMessageIDStr)
    |;

    my $dBObj = $dBObjects{'collageDB'};

    # Connect to the cached dB connection
    $dBObj->connect();

    my $dBHandle = $dBObj->getHandle();

    my $sth = undef;
    $sth = $dBHandle->prepare($query);
    $sth->execute();

    my %appTypeHash = ();

    while ( my $row = $sth->fetchrow_hashref() ) {
	$appTypeHash{ $row->{'LogMessageID'} } = $row->{'ApplicationType'};
    }
    $sth->finish();

    # Add the application type to each event
    foreach my $event (@$eventsToTerminate) {
	$event->{'ApplicationType'} = $appTypeHash{ $event->{'LogMessageID'} };
    }
}

sub sendToFoundation {
    my $xml    = shift(@_);
    my $socket = undef;

    my $foundationHost = $params{'FoundationHost'};
    my $foundationPort = $params{'FoundationPort'};

    # Socket send timeout for writing XML to Foundation, to address GWMON-7407.
    # Specified in seconds.  The usual value is 30; set to 0 to disable.
    my $socket_send_timeout = $params{'socket_send_timeout'};

    $socket = IO::Socket::INET->new( PeerAddr => $foundationHost, PeerPort => $foundationPort, Type => SOCK_STREAM );

    if ($socket) {
	if ( $params{'Debug'} ) {
	    $logObj->log("-----------------------------------");
	    $logObj->log("Sending XML to Foundation:");
	    $logObj->log($xml);
	    $logObj->log("-----------------------------------");
	}

	# Apply a socket send timeout on direct writes to Foundation (GWMON-7407).
	if ( not $socket->sockopt( SO_SNDTIMEO, pack( 'L!L!', $socket_send_timeout, 0 ) ) ) {
	    $logObj->log("ERROR:  Could not set send timeout on socket to $foundationHost Foundation.");
	}

	print $socket $xml;
	close($socket);
    }
    else {
	$logObj->log("ERROR:  Could not open connection to [$foundationHost] on port [$foundationPort]");
    }
}

sub addEventCommentsToNagios {
    my %hostsToComment        = ();
    my %hostServicesToComment = ();
    my %haveHostComments;
    my %haveHostServiceComments;

    if ( $params{'SendNagiosClosingComments'} ) {
	## Create a list of events to be commented upon.  We will send comment messages
	## to Nagios for every host and host-service that we can, regardless of whether Nagios
	## actually owns the host or host-service.  (Logically, we could have restricted the
	## host-service list to only those host-services owned by Nagios, but we can't do that
	## for hosts because a host could possibly be owned by CloudHub but have a service
	## owned by Nagios.)  Nagios will just drop any extra messages on the floor, so there
	## is little harm done save a bit of extra noise in the nagios/var/nagios.log file.  We
	## can get more precise in a later release of this integration code.
	##
	## Because we might have the same host or host-service mentioned in several resolved
	## tickets in this same pass, with separate close_notes strings, we must send separate
	## comment messagess to Nagios in such a case.  We will sort them by the time at
	## which each ticket got resolved; the $event->{'resolved_at'} field is of the form
	## "YYYY-MM-DD hh:mm:ss", making it easy to sort in chronological order.
	##
	## A given host or host-service can be represented multiple times within the same
	## incident, and thus would have the same closing notes multiple times if we treated
	## each such instance separately.  To prevent that, we collapse such duplication here,
	## using $event->{TicketNo} to suppress extra notes from the same incident.
	##
	## If two incidents referencing the same host or host service get resolved at exactly
	## the same second by two different people, their respective resolved_at fields will
	## collide.  We don't want that to prevent us from capturing the close notes from both
	## incidents.  So we use an final $event->{TicketNo} level of hash to disambiguate
	## this condition, even though in the common case there will only be one TicketNo per
	## resolved_at value.
	##
	## Note that these comment messages will be ignored by Nagios if the host or
	## service is already back to an OK state when the message is received by Nagios, which
	## is rather likely once the underlying problem has been resolved by dealing with the
	## ServiceNow incident.  So in that case, the message won't be seen in Nagios CGI
	## screens and won't be forwarded to Foundation.
	##
	foreach my $event (@itemsOfInterest) {
	    if ( $event->{'canTerminate'} && defined( $event->{'resolved_at'} ) && defined( $event->{'hostname'} ) ) {
		if ( defined $event->{'servicedescription'} ) {
		    if ( not $haveHostServiceComments{ $event->{TicketNo} }{ $event->{'hostname'} }{ $event->{'servicedescription'} } ) {
			$hostServicesToComment{ $event->{'hostname'} }{ $event->{'servicedescription'} }{ $event->{'resolved_at'} }
			  { $event->{TicketNo} } = $event->{'close_notes'};
			$haveHostServiceComments{ $event->{TicketNo} }{ $event->{'hostname'} }{ $event->{'servicedescription'} } = 1;
		    }
		}
		else {
		    if ( not $haveHostComments{ $event->{TicketNo} }{ $event->{'hostname'} } ) {
			$hostsToComment{ $event->{'hostname'} }{ $event->{'resolved_at'} }{ $event->{TicketNo} } = $event->{'close_notes'};
			$haveHostComments{ $event->{TicketNo} }{ $event->{'hostname'} } = 1;
		    }
		}
	    }
	}

	if ( %hostsToComment || %hostServicesToComment ) {
	    my $nagios = GW::Nagios->new(
		$moduleParams{'nagios_command_pipe'},
		$moduleParams{'max_command_pipe_write_size'},
		$moduleParams{'max_command_pipe_wait_time'}
	    );
	    if ( not defined $nagios ) {
		$logObj->log('ERROR:  Creating a GW::Nagios object has failed; thus host and service');
		$logObj->log("        comments for incident resolution will not be sent to Nagios.");
	    }
	    else {
		sendHostCommentsToNagios   ( $nagios, \%hostsToComment        ) if %hostsToComment;
		sendServiceCommentsToNagios( $nagios, \%hostServicesToComment ) if %hostServicesToComment;
	    }
	}
    }
}

sub sendHostCommentsToNagios {
    my $nagios         = shift;
    my $hostsToComment = shift;

    # 1 => allow the comment to persist across a Nagios restart
    my $persistent = $params{'PersistNagiosClosingComments'} ? '1' : '0';

    # Who shall we say sent the comments?
    my $author = $params{'NagiosClosingCommentsAuthor'};

    # It would actually make some sense to use the resolved_at timestamp in the comment message to
    # Nagios, but then we might risk sending in a comment for a ticket that was resolved quite some time
    # ago, well before Nagios is willing to accept old timestamps in such messages.  (Bronx has such a
    # limit on the NSCA port; I'm not sure whether Nagios imposes such a limit on the command pipe.)
    # We are effectively collapsing the timestamps this way, as Nagios sees them, so a question arises
    # as to whether we lose an important ordering downstream this way, when messages get sorted during
    # insertion into the Comments field in Foundation.  The answer is, the entry_time that Nagios sees
    # will probably be identical for all messages we send in using the same value of $now.  However, the
    # comment_id field in Nagios should preserve the ordering in which the comments were sent to and
    # received by Nagios.  So as long as we use the resolved_at timestamp to sort the comments we send to
    # Nagios, the final result in Foundation should be sorted the same way.
    my $now = time();

    my @comments = ();
    foreach my $hostname ( keys %$hostsToComment ) {
	my $tickets_for_resolved_at = $hostsToComment->{$hostname};
	foreach my $resolved_at ( sort keys %$tickets_for_resolved_at ) {
	    my $close_notes_for_ticket = $tickets_for_resolved_at->{$resolved_at};
	    foreach my $ticket ( sort keys %$close_notes_for_ticket ) {
		push @comments, "[$now] ADD_HOST_COMMENT;$hostname;$persistent;$author;$close_notes_for_ticket->{$ticket}\n";
	    }
	}
    }
    my $errors = $nagios->send_messages_to_nagios( \@comments );
    $logObj->log($_) for @$errors;
}

sub sendServiceCommentsToNagios {
    my $nagios                = shift;
    my $hostServicesToComment = shift;

    # 1 => allow the comment to persist across a Nagios restart
    my $persistent = $params{'PersistNagiosClosingComments'} ? '1' : '0';

    # Who shall we say sent the comments?
    my $author = $params{'NagiosClosingCommentsAuthor'};

    # It would actually make some sense to use the resolved_at timestamp in the comment message to
    # Nagios, but then we might risk sending in a comment for a ticket that was resolved quite some time
    # ago, well before Nagios is willing to accept old timestamps in such messages.  (Bronx has such a
    # limit on the NSCA port; I'm not sure whether Nagios imposes such a limit on the command pipe.)
    # We are effectively collapsing the timestamps this way, as Nagios sees them, so a question arises
    # as to whether we lose an important ordering downstream this way, when messages get sorted during
    # insertion into the Comments field in Foundation.  The answer is, the entry_time that Nagios sees
    # will probably be identical for all messages we send in using the same value of $now.  However, the
    # comment_id field in Nagios should preserve the ordering in which the comments were sent to and
    # received by Nagios.  So as long as we use the resolved_at timestamp to sort the comments we send to
    # Nagios, the final result in Foundation should be sorted the same way.
    my $now = time();

    my @comments = ();
    foreach my $hostname ( keys %$hostServicesToComment ) {
	my $resolved_at_for_service = $hostServicesToComment->{$hostname};
	foreach my $servicedescription ( keys %$resolved_at_for_service ) {
	    my $tickets_for_resolved_at = $resolved_at_for_service->{$servicedescription};
	    foreach my $resolved_at ( sort keys %$tickets_for_resolved_at ) {
		my $close_notes_for_ticket = $tickets_for_resolved_at->{$resolved_at};
		foreach my $ticket ( sort keys %$close_notes_for_ticket ) {
		    push @comments,
		      "[$now] ADD_SVC_COMMENT;$hostname;$servicedescription;$persistent;$author;$close_notes_for_ticket->{$ticket}\n";
		}
	    }
	}
    }
    my $errors = $nagios->send_messages_to_nagios( \@comments );
    $logObj->log($_) for @$errors;
}

sub isContainedIn() {
    my $testMember = shift(@_);
    my $set        = shift(@_);

    # Cycle through each element of the set and check if testMember
    # matches any element in the set.
    foreach ( @{$set} ) {
	if ( $_ eq $testMember ) { return 1; }
    }

    return 0;
}

sub trim {
    my $aString = shift(@_);

    if ( defined($aString) ) {
	$aString =~ s/^\s+//;    # remove leading  whitespace
	$aString =~ s/\s+$//;    # remove trailing whitespace
    }

    return $aString;
}

sub isProcessAlreadyRunning {
    my $processMatch = $params{'ProcessMatch'};
    my $psApp        = $params{'PSApp'};
    my $psArgs       = $params{'PSArgs'};
    my $grepApp      = $params{'GrepApp'};

    # The "| $grepApp -v $grepApp" at the end should not be necessary, if you have
    # defined PSArgs and ProcessMatch properly.  We're just being extra cautious here.
    my $results = `$psApp $psArgs | $grepApp '$processMatch' | $grepApp -v $grepApp`;
    chomp($results);

    my @lineItems = split( /\n/, $results );
    foreach (@lineItems) {
	my @fields = split;
	## Return true if the PID (demanded here to be the first field in the line)
	## does not match the PID of the current process: $$
	return 1 if defined( $fields[0] ) and $fields[0] ne $$;
    }

    # No conflicting processes
    return 0;
}

main();


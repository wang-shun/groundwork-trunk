#!/usr/local/groundwork/perl/bin/perl -w --

# Copyright (c) 2013 GroundWork Open Source (www.groundworkopensource.com).
# All rights reserved.  Use is subject to GroundWork commercial license terms.

use strict;
use warnings;

use lib '/usr/local/groundwork/jira/perl/lib';

use Config::General;
use IO::Socket;
use Data::Dumper;

use GW::DBObject;
use GW::HelpDeskUtils;
use GW::LogFile;

# The primary configuration file
my $confFile = "/usr/local/groundwork/jira/config/twoway_helpdesk.conf";

# A global hash representing the contents of the confFile
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
    my $config = new Config::General($confFile);
    %params = $config->getall();

    # Read in the configuration file for the external help desk module
    my $moduleConfig = new Config::General( $params{'HelpDeskModuleConfFile'} );
    %moduleParams = $moduleConfig->getall();
}

sub initialize {
    ## Instantiate the LogFile object
    $logObj = new GW::LogFile();
    $logObj->setLogFile( $params{'HelpDeskLogFile'} );
    $logObj->setLogRotation($params{'MaxLogFileSize'}, $params{'MaxLogFilesToRetain'});

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
	$logObj->log("        ($confFile).");
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
	$logObj->log("        ($confFile).");
	exit 1;
    }

    # We disallow any construction that could cause an SQL injection failure.
    if (!defined( $params{'TicketRetentionTime'} ) or $params{'TicketRetentionTime'} !~ /^\d+$/) {
	$logObj->log("FATAL:  TicketRetentionTime is not constructed properly.");
	$logObj->log("        See the configuration file for the required format");
	$logObj->log("        ($confFile).");
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

    my $query = qq{SELECT
			 logmessageid         as "LogMessageID",
			 deviceidentification as "DeviceIdentification",
			 operator             as "Operator",
			 ticketno             as "TicketNo",
			 ticketstatus         as "TicketStatus",
			 clientdata           as "ClientData"
	               FROM   HelpDeskLookupTable
	               WHERE  TicketStatus NOT IN ($resolvedStates)};

    # FIX LATER:  A future version will include the following column as well.
    # We're not including this now only to ease the transition from a MySQL
    # based integration to a PostgreSQL based integration.  The future code
    # that supports this field will do so in service of a means to tell which
    # items are very old and can be removed automatically.
    # ,
    # lastchangetime       as "LastChangeTime"

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
	}
    }
}

sub markEventsInResolvedState {
    for my $event (@itemsOfInterest) {
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

    for my $event (@itemsOfInterest) {
	if ( $event->{'isResolved'} ) {
	    $resolvedEvents{ $event->{'LogMessageID'} } = $event;
	}
    }

    my $numResolvedEvents = scalar keys %resolvedEvents;

    # Continue only if any events moved into the resolved state
    if ($numResolvedEvents) {
	## Query to make sure LogMessage exists in Foundation.
	## This is done to make sure that non-existent Foudation events
	## are not accepted.
	my $logMessageIDStr = join( ',', map { "'" . $_ . "'" } keys %resolvedEvents );

	my $query = qq{SELECT LogMessageID AS "LogMessageID" FROM LogMessage WHERE LogMessageID IN ($logMessageIDStr)};

	my $dBObj = $dBObjects{'collageDB'};

	# Connect to the cached dB connection
	$dBObj->connect();

	my $dBHandle = $dBObj->getHandle();

	my $sth = undef;
	$sth = $dBHandle->prepare($query);
	$sth->execute();

	# If LogMessage exists in Foundation, go ahead and mark the event
	# as acceptable. Otherwise don't send the accept XML for this event.
	while ( my $row = $sth->fetchrow_hashref() ) {
	    my $logMessageID = $row->{'LogMessageID'};

	    if ( defined( $resolvedEvents{$logMessageID} ) ) {
		my $event = $resolvedEvents{$logMessageID};
		$event->{'canTerminate'} = 1;
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
    for my $event (@itemsOfInterest) {
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
    # FIX LATER:  The LastChangeTime/TicketRetentionTime clause should be reinstated once we
    # have added back the LastChangeTime column to the HelpDeskLookupTable table.
    # FIX LATER:  When we enable that clause, we need a replacement for the UNIX_TIMESTAMP()
    # and UNIX_TIMESTAMP(column_name) functions, since these are not available in PostgreSQL.
    my $resolvedStates = $params{'ResolvedStates'};
    my $delete = qq{
	DELETE FROM HelpDeskLookupTable WHERE TicketStatus IN ($resolvedStates)
	-- AND (UNIX_TIMESTAMP() - UNIX_TIMESTAMP(LastChangeTime)) > $params{'TicketRetentionTime'}
    };
    $dBHandle->do( $delete );

    # FIX LATER:  For cleanliness, we ought to disconnect from the database here,
    # so we don't build up lots of connections.  We're saved only by the fact that
    # this script exits soon after this routine is called.
}

sub acceptEventsInFoundation {
    my @eventsToTerminate = ();

    # Create a list of events to be accepted
    for my $event (@itemsOfInterest) {
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
    my $eventsToTerminate  = shift(@_);
    my $logMessageIDStr = shift(@_);

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


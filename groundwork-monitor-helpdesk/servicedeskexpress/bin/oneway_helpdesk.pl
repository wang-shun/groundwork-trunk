#!/usr/local/groundwork/perl/bin/perl -w --

# Copyright (c) 2013 GroundWork Open Source (www.groundworkopensource.com).
# All rights reserved.  Use is subject to GroundWork commercial license terms.

use strict;
use warnings;

use lib '/usr/local/groundwork/servicedeskexpress/perl/lib';

use Config::General;
use Sort::Naturally;
use IO::Socket;
use Data::Dumper;

use GW::DBObject;
use GW::HelpDeskUtils;
use GW::LogFile;

# The primary configuration file
my $confFile = "/usr/local/groundwork/servicedeskexpress/config/oneway_helpdesk.conf";

# A global hash representing the contents of the confFile
my %params = ();

# A container for holding database objects
my %dBObjects = ();

# The arguments hash that gets passed to the client help desk object
my %args = ();

# Parameter hash created by scanning conf file for module
my %moduleParams = ();

# Handle to LogFile object
my $logObj = undef;

# Params that are returned from the createTicket method
# Store globally for easy access in other methods
my $ticketNo     = "";
my $ticketStatus = "";
my $clientData   = "";
my $filingError  = "";

sub main {
    my $startTime = time();

    init();

    processInputArguments();

    # We're careful here not to trust the construction of the input arguments,
    # when we print log messages.
    if ( isUserAllowedToFileTicket() ) {
	my $numEvents = scalar @{ $args{'consoleEvents'} };

	# Only try to file ticket if 1 or more valid events exist
	if ($numEvents) {
	    $logObj->log("About to create an incident for $numEvents events.");
	    createTicket();
	}
	else {
	    $logObj->log( sprintf( "NOTICE:  Log messages '%s' yielded no corresponding events.", $args{'logMessageIDStr'} ) );
	}
    }
    else {
	$logObj->log( sprintf( "ERROR:  User '%s' is not allowed to file tickets.", $args{'operator'} ) );
    }

    my $stopTime  = time();
    my $deltaTime = $stopTime - $startTime;

    $logObj->log("Total Script Execution Time: $deltaTime second(s)");
    $logObj->log("");
}

sub init {
    ## Read the conf files
    eval {
	my $config = new Config::General($confFile);
	%params = $config->getall();
    };
    if ($@) {
	chomp $@;
	my $exception = $@;
	## We cannot use $logObj here, because it hasn't been defined yet,
	## because the log file is named in the file we failed to read!
	print 'FATAL:  Failed to read primary configuration file:';
	print $exception;
	exit 1;
    }

    # Instantiate the LogFile object
    $logObj = new GW::LogFile();
    $logObj->setLogFile( $params{'HelpDeskLogFile'} );
    $logObj->log("Started execution:  oneway_helpdesk.pl @ARGV");

    # Read in the configuration file for the external help desk module
    eval {
	my $moduleConfig = new Config::General( $params{'HelpDeskModuleConfFile'} );
	%moduleParams = $moduleConfig->getall();
    };
    if ($@) {
	chomp $@;
	my $exception = $@;
	$logObj->log('FATAL:  Failed to read HelpDeskModuleConfFile config file:');
	$logObj->log($exception);
	exit 1;
    }

    initBridgeDBObject();
    initCollageDBObject();
}

sub initBridgeDBObject {
    my %bridgeDBParams = ();
    eval {
	my $bridgeDBConfig = new Config::General( $params{'BridgeDBCredentials'} );
	%bridgeDBParams = $bridgeDBConfig->getall();
    };
    if ($@) {
	chomp $@;
	my $exception = $@;
	$logObj->log('FATAL:  Failed to read BridgeDBCredentials config file:');
	$logObj->log($exception);
	exit 1;
    }

    # Setup any special database connection parameters
    # In this case, turn off autocommit (necessary for race condition handling logic)
    my %dBParams = ( 'AutoCommit' => 0 );

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
    my %gwCollageDBParams = ();
    eval {
	my $gwCollageDBConfig = new Config::General( $params{'GWCollageDBCredentials'} );
	%gwCollageDBParams = $gwCollageDBConfig->getall();
    };
    if ($@) {
	chomp $@;
	my $exception = $@;
	$logObj->log('FATAL:  Failed to read GWCollageDBCredentials config file:');
	$logObj->log($exception);
	exit 1;
    }
    my %dBParams = ( 'AutoCommit' => 0 );

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

sub processInputArguments {
    my $argc = scalar @ARGV;

    if ( $argc != 2 ) {
	printUsage();
	exit;
    }

    # The 1st argument is the user name
    $args{'operator'} = trim( $ARGV[0] );

    # The second argument is a comma-separated string of LogMessageIDs
    # provided by the ShellScriptAction class instance that invokes this
    # script.
    #
    # This is stored in the args hash as 'logMessageIDStr'
    # for later reference after it is stripped of any duplicate
    # entries and sorted in natural ascending order.
    $args{'logMessageIDStr'} = removeDuplicates( $ARGV[1] );

    # Create an array of console event hashes corresponding to
    # each logMessage identified in the command-line parameters.
    $args{'consoleEvents'} = getConsoleEventData();
}

sub createTicket {
    ##------------------------------------------------------------------------------
    ## Guard against race condition where multiple people might simultaneously
    ## file a ticket on one or more overlapping console events.
    ##
    ## It is an error condition to file multiple tickets for a single event.
    ## But it is perfectly ok to log multiple events on a single ticket.
    ##------------------------------------------------------------------------------
    if ( addToConcurrencyTable() ) {
	if ( isTicketAlreadyFiled() ) {
	    removeFromConcurrencyTable();
	    $logObj->log("A ticket has already been filed for one or more selected events.");
	}
	else {
	    ## Instantiate the client specific HelpDesk object
	    my $helpDeskModule = $params{'HelpDeskModule'};

	    eval "use $helpDeskModule";

	    if ($@) {
		chomp $@;
		my $exception = $@;
		removeFromConcurrencyTable();
		$logObj->log("ERROR:  Could not load module: $helpDeskModule");
		$logObj->log($exception);
	    }
	    else {
		my $helpDeskObj = $helpDeskModule->new();

		$helpDeskObj->setLogObj($logObj);
		$helpDeskObj->setDBObj( $dBObjects{'bridgeDB'} );

		my $ticketDetails = undef;

		eval { $ticketDetails = $helpDeskObj->createTicket( \%moduleParams, $args{'operator'}, $args{'consoleEvents'} ); };
		if ($@) {
		    ## First, save the error message so it isn't overwritten by any subsequent
		    ## actions before we try to emit a log message containing it.
		    chomp $@;
		    my $exception = $@;
		    removeFromConcurrencyTable();
		    $logObj->log("ERROR:  An error was encountered while invoking createTicket:");
		    $logObj->log($exception);
		}
		else {
		    updateTicketDetails($ticketDetails);
		    removeFromConcurrencyTable();
		}
	    }
	}
    }
    else {
	$logObj->log("Another operator is filing a ticket on this set or subset of selected events.");
    }
}

sub updateTicketDetails {
    my $ticketDetails = shift(@_);

    # Capture the ticket details and store into global vars
    if ( defined( $ticketDetails->{'FilingError'} ) )  { $filingError  = $ticketDetails->{'FilingError'}; }
    if ( defined( $ticketDetails->{'TicketNo'} ) )     { $ticketNo     = $ticketDetails->{'TicketNo'}; }
    if ( defined( $ticketDetails->{'TicketStatus'} ) ) { $ticketStatus = $ticketDetails->{'TicketStatus'}; }
    if ( defined( $ticketDetails->{'ClientData'} ) )   { $clientData   = $ticketDetails->{'ClientData'}; }

    if ( $filingError eq "" ) {
	if ( ( $ticketNo ne "" ) && ( $ticketStatus ne "" ) ) {
	    ## No errors or blank fields, so let's continue with processing
	    insertTicketDataIntoLookupTable();
	    insertDataIntoFoundation();
	}
	else {
	    $logObj->log("TicketNo or TicketStatus were blank.");
	}
    }
    else {
	## A filing error was encountered.
	$logObj->log("A ticket filing error was encountered.");
    }
}

sub insertDataIntoFoundation {
    my $xml = constructFoundationXML();
    sendToFoundation($xml);
}

sub constructFoundationXML {
    my $consoleEvents = $args{'consoleEvents'};
    my %appTypeBins   = ();

    # Put the consoleEvents into bins defined by Application Type.
    foreach my $event (@$consoleEvents) {
	my $appType = $event->{'ApplicationType'};

	if ( not defined( $appTypeBins{$appType} ) ) {
	    my @emptyArray = ();
	    $appTypeBins{$appType} = \@emptyArray;
	}

	my $bin = $appTypeBins{$appType};
	push( @$bin, $event );
    }

    my $adapterEnvelope = "<Adapter Session=\"1001\" AdapterType=\"SystemAdmin\">";
    my $commandEnvelope = "";

    # For each bin, construct the appropriate Command XML.
    foreach my $appType ( keys %appTypeBins ) {
	my $bin = $appTypeBins{$appType};

	$commandEnvelope .= "<Command Action=\"MODIFY\" ApplicationType=\"$appType\"> ";

	my $logMessageXML = "";
	foreach my $event (@$bin) {
	    my $id       = $event->{'LogMessageID'};
	    my $operator = $args{'operator'};

	    $logMessageXML .= "<LOGMESSAGE LogMessageId='$id' TicketNo='$ticketNo' Operator='$operator' />";
	}

	$commandEnvelope .= "$logMessageXML</Command>";
    }

    $adapterEnvelope .= "$commandEnvelope</Adapter>";

    return $adapterEnvelope;
}

sub sendToFoundation {
    my $xml    = shift(@_);
    my $socket = undef;

    my $foundationHost = $params{'FoundationHost'};
    my $foundationPort = $params{'FoundationPort'};

    # Socket send timeout for writing XML to Foundation, to address GWMON-7407.
    # Specified in seconds.  The usual value is 30; set to 0 to disable.
    my $socket_send_timeout = 30;

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

sub addToConcurrencyTable {
    ## User helper functions to define a query and insert set of LogMessageIDs
    my $logMessageIDQuerySet  = GW::HelpDeskUtils::generateQuerySet( $args{'logMessageIDStr'} );
    my $logMessageIDInsertSet = GW::HelpDeskUtils::generateInsertSet( $args{'logMessageIDStr'} );

    # MySQL-equivalent commands:
    # my $lockTable   = "LOCK TABLES HelpDeskConcurrencyTable Write;";
    # my $unlockTable = "UNLOCK TABLES;";
    my $lockTable   = "LOCK TABLE HelpDeskConcurrencyTable IN ACCESS EXCLUSIVE MODE;";
    my $unlockTable = "UNLOCK TABLE;";

    my $query  = "SELECT LogMessageID AS \"LogMessageID\" FROM HelpDeskConcurrencyTable WHERE LogMessageID IN $logMessageIDQuerySet";
    my $insert = "INSERT INTO HelpDeskConcurrencyTable VALUES $logMessageIDInsertSet;";

    my $dBObj = $dBObjects{'bridgeDB'};

    # Connect to the cached dB connection
    $dBObj->connect();

    my $dBHandle = $dBObj->getHandle();

    my $sth = undef;

    # Lock the HelpDeskConcurrencyTable
    $sth = $dBHandle->prepare($lockTable);
    $sth->execute();

    # Check to see if another operator is filing a ticket on any
    # overlapping events
    $sth = $dBHandle->prepare($query);

    my $result = $sth->execute();

    if ($result) {
	## Perl returns "0E0" which equates to 0 but evals to true
	## if the execute method above did not affect any rows.
	## ( http://search.cpan.org/~timb/DBI/DBI.pm#execute )

	if ( $result == 0 ) {
	    ## There are no conflicts with other processes. So it is ok
	    ## to insert this LogMessageID into the concurrency table.

	    $sth = $dBHandle->prepare($insert);
	    $sth->execute();

	    $sth = $dBHandle->prepare($unlockTable);
	    $sth->execute();

	    $sth->finish();

	    $dBHandle->commit();

	    return 1;
	}
    }

    # PostgreSQL has no UNLOCK TABLE command; locks are always held until
    # transaction end, and released automatically at that time.
    # $sth = $dBHandle->prepare($unlockTable);
    # $sth->execute();

    $sth->finish();

    $dBHandle->commit();

    return 0;
}

sub removeFromConcurrencyTable {
    ## Use helper class to construct query set of LogMessageIDs
    my $logMessageIDQuerySet = GW::HelpDeskUtils::generateQuerySet( $args{'logMessageIDStr'} );

    # MySQL-equivalent commands:
    # my $lockTable   = "LOCK TABLES HelpDeskConcurrencyTable Write;";
    # my $unlockTable = "UNLOCK TABLES;";
    my $lockTable   = "LOCK TABLE HelpDeskConcurrencyTable IN ACCESS EXCLUSIVE MODE;";
    my $unlockTable = "UNLOCK TABLE;";

    my $delete = "DELETE FROM HelpDeskConcurrencyTable WHERE LogMessageID IN $logMessageIDQuerySet;";

    my $dBObj = $dBObjects{'bridgeDB'};

    # Connect to the cached dB connection
    $dBObj->connect();

    my $dBHandle = $dBObj->getHandle();

    my $sth = undef;

    # Lock the HelpDeskConcurrencyTable
    $sth = $dBHandle->prepare($lockTable);
    $sth->execute();

    # Delete the LogMessageIDs
    $sth = $dBHandle->prepare($delete);
    $sth->execute();

    # PostgreSQL has no UNLOCK TABLE command; locks are always held until
    # transaction end, and released automatically at that time.
    # # Unlock the HelpDeskConcurrencyTable
    # $sth = $dBHandle->prepare($unlockTable);
    # $sth->execute();

    $sth->finish();

    $dBHandle->commit();
}

sub isTicketAlreadyFiled {
    ## Use helper class to construct query set of LogMessageIDs
    my $logMessageIDQuerySet = GW::HelpDeskUtils::generateQuerySet( $args{'logMessageIDStr'} );

    my $query = "SELECT count(*) FROM HelpDeskLookupTable WHERE LogMessageID IN $logMessageIDQuerySet;";

    my $dBObj = $dBObjects{'bridgeDB'};

    # Connect to the cached dB connection
    $dBObj->connect();

    my $dBHandle = $dBObj->getHandle();

    my $sth = undef;

    $sth = $dBHandle->prepare($query);
    $sth->execute();

    my $numTicketsFiled = $sth->fetchrow_array;

    $dBHandle->commit();

    # At least 1 ticket was filed for one or more of these events
    return 1 if $numTicketsFiled;

    # There were no tickets filed for any of these events
    return 0;
}

sub isUserAllowedToFileTicket {
    my $isAllowed  = 0;
    my $filterFile = $params{'AuthUsersFilterFile'};

    # Make sure the filter file is available
    if ( -e $filterFile ) {
	## Read the users filter file
	my %usersHash = ();
	eval {
	    my $configObj = new Config::General($filterFile);
	    %usersHash = $configObj->getall();
	};
	if ($@) {
	    chomp $@;
	    my $exception = $@;
	    $logObj->log('FATAL:  Failed to read AuthUsersFilterFile config file:');
	    $logObj->log($exception);
	    exit 1;
	}

	my @usersArray = keys %usersHash;

	foreach my $user (@usersArray) {
	    ## Found wildcard character
	    $isAllowed = 1 if $user eq '*';

	    ## Found an exact match
	    $isAllowed = 1 if $user eq $args{'operator'};
	}
    }

    return $isAllowed;
}

sub getConsoleEventData {
    ## Array to store event information
    my @eventData = ();

    # Get the array reference to LogMessageID Strings
    my $logMessageIDStr = $args{'logMessageIDStr'};

    # Get the string of valid MonitorStatus
    my $validMonitorStatus = $params{'MonitorStatus'};

    my $query = qq|
	SELECT
	LogMessage.LogMessageID			AS "LogMessageID",
	MonitorStatus.Name			AS "MonitorStatus",
	ApplicationType.Name			AS "ApplicationType",
	LogMessage.ReportDate			AS "ReportDate",
	LogMessage.FirstInsertDate		AS "FirstInsertDate",
	LogMessage.LastInsertDate		AS "LastInsertDate",
	LogMessage.MsgCount			AS "MsgCount",
	LogMessage.DeviceID			AS "DeviceID",
	Device.DisplayName			AS "DeviceDisplayName",
	Device.Identification			AS "DeviceIdentification",
	TextMessage				AS "TextMessage"
	FROM LogMessage, ApplicationType, Device, MonitorStatus
	WHERE
	    LogMessage.LogMessageID IN ($logMessageIDStr)
	AND
	    ApplicationType.ApplicationTypeID = LogMessage.ApplicationTypeID
	AND
	    MonitorStatus.MonitorStatusID = LogMessage.MonitorStatusID
	AND
	    MonitorStatus.Name IN ($validMonitorStatus)
	AND
	    Device.DeviceID = LogMessage.DeviceID
    |;

    my $dBObj = $dBObjects{'collageDB'};

    # Connect to the cached dB connection
    $dBObj->connect();

    my $dBHandle = $dBObj->getHandle();

    my $sth = undef;

    $sth = $dBHandle->prepare($query);

    $sth->execute() or do { $logObj->log( $sth->errstr ); };

    $logObj->log("getting rows of event data");
    while ( my $row = $sth->fetchrow_hashref() ) {
	$logObj->log("got row of event data");
	push( @eventData, $row );
    }

    $dBHandle->commit();

    return \@eventData;
}

#------------------------------------------------------------------------------
# Method to populate the LookupTable in the bridge database.
#------------------------------------------------------------------------------
sub insertTicketDataIntoLookupTable {
    my $insert = qq{
	INSERT INTO HelpDeskLookupTable
	(LogMessageID, DeviceIdentification, Operator, TicketNo, TicketStatus, ClientData)
	VALUES (?, ?, ?, ?, ?, ?)
    };

    my $dBObj = $dBObjects{'bridgeDB'};

    # Connect to the cached dB connection
    $dBObj->connect();

    my $dBHandle = $dBObj->getHandle();

    my $sth = undef;

    $sth = $dBHandle->prepare($insert);

    my $operator      = $args{'operator'};
    my $consoleEvents = $args{'consoleEvents'};

    $logObj->log("about to create tickets");
    foreach my $consoleEvent (@$consoleEvents) {
	$logObj->log("creating ticket");
	$sth->execute(
	    $consoleEvent->{'LogMessageID'},
	    $consoleEvent->{'DeviceIdentification'},
	    $operator, $ticketNo, $ticketStatus, $clientData
	) or do { $logObj->log( $sth->errstr ); };
    }

    $dBHandle->commit();
}

sub removeDuplicates {
    my $idString = shift(@_);

    # Remove any whitespace
    $idString = trim($idString);

    # Create an array of LogMessageIDs
    my @idList = split( /,/, $idString );

    # Weed out any duplicates
    my %idHash = ();
    foreach my $id (@idList) { $idHash{$id} = 1; }

    # Create a naturally sorted list
    my @processedIDList = nsort( keys %idHash );

    # Build a stored string representation
    my $processedIDString = join( ',', @processedIDList );

    return $processedIDString;
}

#------------------------------------------------------------------------------
# Trim whitespace from front and end of strings
#------------------------------------------------------------------------------
sub trim() {
    my $aString = shift(@_);

    if ( defined($aString) ) {
	$aString =~ s/^\s+//;    # remove leading  whitespace
	$aString =~ s/\s+$//;    # remove trailing whitespace
    }

    return $aString;
}

sub printUsage {
    print "\n";
    print "Usage: $0 <User> <LogMessageIDString>\n";
    print "\n";
    print "              User:  a GroundWork user\n";
    print "LogMessageIDString:  a comma-separated list of LogMessageIDs\n";
    print "                     such as 3,35,94,380,230\n";
    print "\n";
    print "Remember that the maximum length of command line args should\n";
    print "not exceed 127,968 bytes.\n";
    print "\n\n";
}

main();


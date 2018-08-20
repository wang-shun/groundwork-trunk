#!/usr/local/groundwork/perl/bin/perl -w --

# Copyright (c) 2013 GroundWork Open Source (www.groundworkopensource.com).
# All rights reserved.  Use is subject to GroundWork commercial license terms.

# TO DO:
# (*) Redirect STDERR to STDOUT, so it is captured and sent to the calling
#     context if this script fails for any reason (say, some unexpected Perl
#     run-time error).
# (*) Improve the error detection and handling for potential database-access
#     exceptions.
# (*) Whenever this script encounters an error that the operator ought to
#     know about, arrange that the script continue on to do whatever cleanup
#     it needs to do, then to print a set of error messages to STDOUT and
#     exit with a non-zero exit status.  This will cause the Event Console
#     to present the messages to the user.

# GENERAL RULE:
#
# If we ever need to exit early (exit(1)), then we must first print an error message on
# STDOUT for the benefit of the calling context, which may choose to display it to the
# user.  Then we should log an error message into our own log file, for later forensic
# work (using $logObj->log()), if the log file is available.  Only then can we actually
# exit with a clear conscience.

use strict;
use warnings;

use lib '/usr/local/groundwork/jira/perl/lib';

use Config::General;

#use Sort::Naturally;
use IO::Socket;
use Data::Dumper;

use GW::DBObject;
use GW::HelpDeskUtils;
use GW::LogFile;

# The primary configuration file
my $confFile = "/usr/local/groundwork/jira/config/oneway_helpdesk.conf";

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
my $ticketNo     = '';
my $ticketStatus = '';
my $clientData   = '';
my $filingError  = '';

sub main {
    my $startTime = time();
    my $outcome = 1;

    init();

    processInputArguments();

    # We're careful here not to trust the construction of the input arguments,
    # when we print log messages.
    if ( isUserAllowedToFileTicket() ) {
	my $numEvents = scalar @{ $args{'consoleEvents'} };

	# Only try to file ticket if 1 or more valid events exist
	if ($numEvents) {
	    $logObj->log("About to create a ticket for $numEvents events.");
	    $outcome &&= createTicket();
	}
	else {
	    my $message = sprintf( "NOTICE:  Log messages '%s' yielded no corresponding events.", $args{'logMessageIDStr'} );
	    print        "$message\n";
	    $logObj->log( $message );
	    $outcome = 0;
	}
    }
    else {
	my $message = sprintf( "ERROR:  User '%s' is not allowed to file tickets.", $args{'operator'} );
	print        "$message\n";
	$logObj->log( $message );
	$outcome = 0;
    }

    my $stopTime  = time();
    my $deltaTime = $stopTime - $startTime;

    $logObj->log("Total Script Execution Time: $deltaTime second(s)");
    $logObj->log("");
    $logObj->rotateLogFile();

    return $outcome;
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
	print "FATAL:  $0 failed to read primary configuration file:\n";
	print "$exception\n";
	exit (1);
    }

    # Instantiate the LogFile object
    $logObj = new GW::LogFile();
    $logObj->setLogFile( $params{'HelpDeskLogFile'} );
    $logObj->setLogRotation( $params{'MaxLogFileSize'}, $params{'MaxLogFilesToRetain'} );

    # We want to know when the script started, so we have a line in the sand in case
    # it is interrupted by Foundation, to tell how long it ran in that situation by
    # comparing the timestamp generated for this log message with the timestamp for
    # a java.lang.InterruptedException message in framework.log.  This also allows us
    # to verify that the command arguments are being provided in the expected order.
    $logObj->log("Started execution:  oneway_helpdesk.pl" . (@ARGV ? ' "' : '') . join('" "', @ARGV) . (@ARGV ? '"' : ''));

    # Read in the configuration file for the external help desk module
    eval {
	my $moduleConfig = new Config::General( $params{'HelpDeskModuleConfFile'} );
	%moduleParams = $moduleConfig->getall();
    };
    if ($@) {
	chomp $@;
	my $exception = $@;
	print "FATAL:  $0 failed to read HelpDeskModuleConfFile config file:\n";
	print "$exception\n";
	$logObj->log('FATAL:  Failed to read HelpDeskModuleConfFile config file:');
	$logObj->log($exception);
	exit (1);
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
	print "FATAL:  Failed to read BridgeDBCredentials config file:\n";
	print "$exception\n";
	$logObj->log('FATAL:  Failed to read BridgeDBCredentials config file:');
	$logObj->log($exception);
	exit (1);
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
	print "FATAL:  Failed to read GWCollageDBCredentials config file:\n";
	print "$exception\n";
	$logObj->log('FATAL:  Failed to read GWCollageDBCredentials config file:');
	$logObj->log($exception);
	exit (1);
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

# Our parsing of command-line arguments here demands that this script be used ONLY with
# GWMEE 6.5 or later.  That's because we had two separate bugs in the way that the
# Event Console in the GW6.4 and earlier releases passed arguments to an Action script.
# These arguments might appear in any order (GWMON-9931), and if an argument contained
# whitespace, it would appear instead as multiple arguments (GWMON-9912).
#
# Because we now allow and expect a user comment to be passed as the third argument to
# this script, and that argument could contain an arbitrary number of space characters,
# our previous strategy of trying to unwind all the mistakes of the Event Console in
# passing arguments will no longer work.  Thus we have removed the complex parsing, and
# this script is only operable under GWMEE 6.5 or later.
sub processInputArguments {
    my $argc = scalar @ARGV;

    # We always expect 3 arguments:  OperatorName, LogMessageIDs, and Comment (the last field
    # being whatever is entered by the user when the log messages are submitted for ticket
    # creation, or defaulted by the Event Console if the user doesn't enter anything).
    if ( $argc != 3 ) {
	printUsage();
	$logObj->log("FATAL:  $0 was called with $argc arguments.");
	exit (1);
    }

    # For debug purposes only.  To be disabled in production.
    if (0) {
	do {
	    foreach my $arg (@ARGV) {
		$logObj->log("DEBUG:  command-line argument:  '$arg'");
	    }
	};
    }

    unless ($ARGV[1] =~ /^[0-9,]+$/ && $ARGV[1] =~ /^[0-9]/ && $ARGV[1] =~ /[0-9]$/ && $ARGV[1] !~ /,,/) {
	print        "FATAL:  $0 second argument (\"$ARGV[1]\") does not look like log message IDs.\n";
	$logObj->log("FATAL:  $0 second argument (\"$ARGV[1]\") does not look like log message IDs.");
	exit (1);
    }

    # The first argument is the user name.  Note that the integration may
    # require it to be a multi-word form such as "Firstname Lastname", so
    # the calling program must quote the user name or otherwise manage the
    # command-line arguments to ensure that all the words in the name form
    # just one argument.
    $args{'operator'} = trim( $ARGV[0] );

    # The second argument is a comma-separated string of LogMessageIDs
    # provided by the ShellScriptAction class instance that invokes this
    # script.
    #
    # This is stored in the args hash as 'logMessageIDStr'
    # for later reference after it is stripped of any duplicate
    # entries and sorted in natural ascending order.
    $args{'logMessageIDStr'} = removeDuplicates( $ARGV[1] );

    # The third argument is an operator-specified commentary for this ticket creation.
    #
    # This is stored in the args hash as 'summary', trimmed but otherwise intact.
    $args{'summary'} = trim( $ARGV[2] );

    # Strip the trailing timestamp that is invariably appended by the Event Console
    # calling context.  (Since we are going to use this field as the JIRA Summary, that
    # timestamp is of little use to us.  The JIRA itself will automatically contain a
    # "Created:" field, which represents equivalent information.)  Then suppress the
    # particular remaining text that is used as a default value by the Event Console if
    # the user does not enter any text in the pop-up dialog box (or if such a dialog box
    # never appears).  We do the latter because the default text has no real value.  An
    # empty $args{'summary'} will be replaced later on when a ticket is created by a
    # standard constructed value that will include a list of all the Log Message ID
    # numbers.  That string is not terribly informative either, but at least it should
    # have the benefit of a certain degree of uniqueness.
    $args{'summary'} =~ s{\s+\d+/\d+/\d+\s+\d+:\d+:\d+\s*(?:AM|PM)?$}{};
    $args{'summary'} = '' if $args{'summary'} =~ /(?:Updated|Acknowledged) from console at/;

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
    my $outcome = 1;
    if ( addToConcurrencyTable() ) {
	if ( isTicketAlreadyFiled() ) {
	    removeFromConcurrencyTable();
	    print        "A ticket has already been filed for one or more of the selected events.\n";
	    $logObj->log("A ticket has already been filed for one or more of the selected events.");
	    $outcome = 0;
	}
	else {
	    ## Instantiate the client specific HelpDesk object
	    my $helpDeskModule = $params{'HelpDeskModule'};

	    eval "use $helpDeskModule";
	    if ($@) {
		chomp $@;
		my $exception = $@;
		removeFromConcurrencyTable();
		print "ERROR:  Could not load module: $helpDeskModule\n";
		print "$exception\n";
		$logObj->log("ERROR:  Could not load module: $helpDeskModule");
		$logObj->log($exception);
		$outcome = 0;
	    }
	    else {
		my $helpDeskObj = $helpDeskModule->new();

		$helpDeskObj->setLogObj($logObj);
		$helpDeskObj->setDBObj( $dBObjects{'bridgeDB'} );

		my $ticketDetails = undef;

		eval {
		    $ticketDetails =
		      $helpDeskObj->createTicket( \%params, \%moduleParams, $args{'operator'}, $args{'consoleEvents'}, $args{'summary'} );
		};
		if ($@) {
		    ## First, save the error message so it isn't overwritten by any subsequent
		    ## actions before we try to emit a log message containing it.
		    chomp $@;
		    my $exception = $@;
		    removeFromConcurrencyTable();
		    print "ERROR:  An error was encountered while invoking createTicket:\n";
		    print "$exception\n";
		    $logObj->log("ERROR:  An error was encountered while invoking createTicket:");
		    $logObj->log($exception);
		    $outcome = 0;
		}
		else {
		    updateTicketDetails($ticketDetails);
		    removeFromConcurrencyTable();
		}
	    }
	}
    }
    else {
	print        "Another operator is filing a ticket on this set or subset of selected events.\n";
	$logObj->log("Another operator is filing a ticket on this set or subset of selected events.");
	$outcome = 0;
    }
    return $outcome;
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
	    print        "TicketNo or TicketStatus were blank.\n";
	    $logObj->log("TicketNo or TicketStatus were blank.");
	}
    }
    else {
	## A filing error was encountered.
	print        "A ticket filing error was encountered.\n";
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
    # Auto-vivification dramatically simplifies this loop.
    foreach my $event (@$consoleEvents) {
	push @{ $appTypeBins{ $event->{'ApplicationType'} } }, $event;
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

	    ## Optionally modify the GWCollageDB.LogMessage.OperationStatusID
	    ## field from its existing value (generally OPEN) to NOTIFIED.
	    my $operation_status = ( $params{'UpdateStatusOfNewlyTicketedEvents'} eq 'yes' ) ?
		"OperationStatus='$params{StatusOfNewlyTicketedEvents}'" : '';

	    $logMessageXML .= "<LOGMESSAGE LogMessageId='$id' $operation_status TicketNo='$ticketNo' Operator='$operator' />";
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
	print        "ERROR:  Could not open connection to [$foundationHost] on port [$foundationPort]\n";
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
	    print "FATAL:  Failed to read AuthUsersFilterFile config file:\n";
	    print "$exception\n";
	    $logObj->log('FATAL:  Failed to read AuthUsersFilterFile config file:');
	    $logObj->log($exception);
	    exit (1);
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
	Host.HostName                           AS "HostName",
	ServiceStatus.ServiceDescription        AS "ServiceDescription",
	HostStatusProperty.ValueString		AS "HostNotes",
	ServiceStatusProperty.ValueString	AS "ServiceNotes",
	ApplicationType.Name			AS "ApplicationType",
	HostMonitorStatus.Name			AS "HostMonitorStatus",
	MonitorStatus.Name			AS "MonitorStatus",
	Severity.Name				AS "Severity",
	LogMessage.ReportDate                   AS "ReportDate",
	LogMessage.FirstInsertDate              AS "FirstInsertDate",
	LogMessage.LastInsertDate               AS "LastInsertDate",
	LogMessage.MsgCount                     AS "MsgCount",
	LogMessage.TextMessage                  AS "TextMessage",
	LogMessage.DeviceID                     AS "DeviceID",
	Device.DisplayName			AS "DeviceDisplayName",
	Device.Identification			AS "DeviceIdentification"
	FROM
	    LogMessage
		LEFT JOIN Host ON (Host.HostID = LogMessage.HostStatusID)
		LEFT JOIN ServiceStatus ON (ServiceStatus.ServiceStatusID = LogMessage.ServiceStatusID)
		LEFT JOIN (PropertyType CROSS JOIN HostStatusProperty)
		    ON (PropertyType.Name = 'Notes'
		    and HostStatusProperty.HostStatusID = LogMessage.HostStatusID
		    and HostStatusProperty.PropertyTypeID = PropertyType.PropertyTypeID)
		LEFT JOIN ServiceStatusProperty
		    ON (PropertyType.Name = 'Notes' and
		    ServiceStatusProperty.ServiceStatusID = LogMessage.ServiceStatusID and
		    ServiceStatusProperty.PropertyTypeID = PropertyType.PropertyTypeID)
		LEFT JOIN (HostStatus CROSS JOIN MonitorStatus HostMonitorStatus)
		    ON (HostStatus.HostStatusID = LogMessage.HostStatusID
		    and HostMonitorStatus.MonitorStatusID = HostStatus.MonitorStatusID)
		LEFT JOIN Severity ON Severity.SeverityID = LogMessage.SeverityID,
	    ApplicationType, Device, MonitorStatus
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

    if ( not $sth->execute() ) {
	print         $sth->errstr . "\n";
	$logObj->log( $sth->errstr );
    }
    else {
	$logObj->log("getting rows of event data");
	while ( my $row = $sth->fetchrow_hashref() ) {
	    $logObj->log("got row of event data");
	    push( @eventData, $row );
	}
    }
    $sth->finish();

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

    $logObj->log("about to record tickets");
    foreach my $consoleEvent (@$consoleEvents) {
	$logObj->log("recording ticket $ticketNo for Log Message $consoleEvent->{'LogMessageID'}");
	$sth->execute(
	    $consoleEvent->{'LogMessageID'},
	    $consoleEvent->{'DeviceIdentification'},
	    $operator, $ticketNo, $ticketStatus, $clientData
	) or do {
	    print         $sth->errstr . "\n";
	    $logObj->log( $sth->errstr );
	};
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
    #	my @processedIDList = nsort(keys %idHash);   # removed by DAB for sake of ZenDesk
    my @processedIDList = keys %idHash;

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
    print "Usage: $0 {User} {LogMessageIDString} {Comment}\n";
    print "\n";
    print "              User:  a GroundWork user\n";
    print "LogMessageIDString:  a comma-separated list of LogMessageIDs\n";
    print "                     such as 3,35,94,380,230\n";
    print "           Comment:  a text string (all in one command-line argument,\n";
    print "                     even if it contains space characters) summarizing\n";
    print "                     the messages selected to create a ticket\n";
    print "\n";
    print "Remember that the maximum length of command line args should\n";
    print "not exceed 127,968 bytes.\n";
    print "\n\n";
}

# Here is the entire substance of this script, in a one-liner:
exit (main() ? 0 : 1);


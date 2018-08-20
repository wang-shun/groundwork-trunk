#!/usr/local/groundwork/perl/bin/perl -w

use lib '/usr/local/groundwork/helpdesk/lib';

use strict;
use warnings;
use Config::General;
use IO::Socket;
use Data::Dumper;

use GW::DBObject;
use GW::HelpDeskUtils;
use GW::LogFile;


# The primary configuration file
my $confFile = "/usr/local/groundwork/helpdesk/conf/twoway_helpdesk.conf";

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

# Number of events that were moved to the 'ACCEPT' state on the console
my $numEventsToAccept = 0;


sub main {
	readConfFile();

	if (isProcessAlreadyRunning()) {
		print "\n";
		print "Identical process is already executing.  Exiting without performing any actions. \n";
		print "\n";
		exit; 
	}

	initialize();

	my $startTime = time();

	scanBridgeDBForItemsOfInterest();

	queryHelpDeskForUpdates();

	markEventsInResolvedState();

	markEventsToAccept();

	acceptEventsInFoundation();

	updateHelpDeskBridgeDB();

	my $stopTime = time();
	my $deltaTime = $stopTime - $startTime;

	$logObj->log("Number of Events Moved to CLOSED state: $numEventsToAccept");
	$logObj->log("Total Script Execution Time: $deltaTime second(s)");
	$logObj->log("");
}

sub readConfFile {
	# Read the conf files
	my $config = new Config::General($confFile);
	%params    = $config->getall();

	# Read in the configuration file for the external help desk module
	my $moduleConfig = new Config::General($params{'HelpDeskModuleConfFile'});
	%moduleParams    = $moduleConfig->getall();
}

sub initialize {
	# Instantiate the LogFile object
	my $logFileName = $params{'HelpDeskLogFile'};
	$logObj         = new GW::LogFile();
	$logObj->setLogFile($logFileName);

	# Replace the escaped quotes: \' with just a single quote: '
	$params{'ResolvedStates'} =~ s/\'/'/g;

	# Read the resolved states string and put each state string
	# into an array.
	my @fields = split(/,/, $params{'ResolvedStates'});

	foreach my $resolvedState (@fields) {
		$resolvedState       = trim($resolvedState);
		my $resolvedStateLen = length($resolvedState);

		if ($resolvedStateLen > 2) {
			# Remove the single quotes around the state name
			$resolvedState = substr($resolvedState, 1, $resolvedStateLen-2);
		}

		# Store resolved state strings in an array
		push(@resolvedStates, $resolvedState);
	}

	initBridgeDBObject();
	initCollageDBObject();
}

sub initBridgeDBObject {
	my $bridgeDBConfig = new Config::General($params{'BridgeDBCredentials'});
	my %bridgeDBParams = $bridgeDBConfig->getall();

	# Create and initialize DBObject
	$dBObjects{'bridgeDB'} = new GW::DBObject();

	$dBObjects{'bridgeDB'}->setDBHost($bridgeDBParams{'bridgeDB.host'});
	$dBObjects{'bridgeDB'}->setDBName($bridgeDBParams{'bridgeDB.name'});
	$dBObjects{'bridgeDB'}->setDBUser($bridgeDBParams{'bridgeDB.user'});
	$dBObjects{'bridgeDB'}->setDBPass($bridgeDBParams{'bridgeDB.pass'});

	# Create a cached dB connection
	$dBObjects{'bridgeDB'}->connect();
}

sub initCollageDBObject {
	my $gwCollageDBConfig = new Config::General($params{'GWCollageDBCredentials'});
	my %gwCollageDBParams = $gwCollageDBConfig->getall();

	# Create and initialize DBObject
	$dBObjects{'collageDB'} = new GW::DBObject();

	$dBObjects{'collageDB'}->setDBHost($gwCollageDBParams{'collage.dbhost'});
	$dBObjects{'collageDB'}->setDBName($gwCollageDBParams{'collage.database'});
	$dBObjects{'collageDB'}->setDBUser($gwCollageDBParams{'collage.username'});
	$dBObjects{'collageDB'}->setDBPass($gwCollageDBParams{'collage.password'});

	# Create a cached dB connection
	$dBObjects{'collageDB'}->connect();
}

sub scanBridgeDBForItemsOfInterest {
	my $resolvedStates   = $params{'ResolvedStates'};

	my $query = qq{SELECT *
	               FROM   HelpDeskLookupTable
	               WHERE  TicketStatus NOT IN ($resolvedStates)};

	my $dBObj = $dBObjects{'bridgeDB'};

	# Connect to the cached dB connection
	$dBObj->connect();

	my $dBHandle = $dBObj->getHandle();

	my $sth = undef;

	$sth = $dBHandle->prepare($query);

	$sth->execute();

	while (my $row = $sth->fetchrow_hashref()) {
		$row->{'hasChanged'} = 0;
		$row->{'isResolved'} = 0;	
		$row->{'canAccept'}  = 0;

		push(@itemsOfInterest, $row)
	}	
}

sub queryHelpDeskForUpdates {
	# Instantiate the client specific HelpDesk object
	my $helpDeskModule = $params{'HelpDeskModule'};

	eval "use $helpDeskModule";

	if ($@) { 
		$logObj->log("Could not load module: $helpDeskModule");
		$logObj->log($@);
	}
	else {
		my $numEvents = scalar @itemsOfInterest;

		# Instantiate HelpDesk module and delegate to it only if
		# valid unresolved events were found in the bridge database
		if ($numEvents) {
			my $helpDeskObj = $helpDeskModule->new();
			$helpDeskObj->setLogObj($logObj);
			$helpDeskObj->setDBObj($dBObjects{'bridgeDB'});

			eval {
				$helpDeskObj->updateStatusForTickets(\%moduleParams, \@itemsOfInterest);
			};

			if ($@) {
				$logObj->log("An error was encountered while invoking updateStatusForTickets");
				$logObj->log($@);
			}
		}
	}
}

sub markEventsInResolvedState {
	for my $event (@itemsOfInterest) {
		if ($event->{'hasChanged'}) {
			if (isContainedIn($event->{'TicketStatus'}, \@resolvedStates)) {
				$event->{'isResolved'} = 1;
			}
		}
	}
}

sub markEventsToAccept {
	# An array to hold pointers to all resolved events
	my %resolvedEvents;

	for my $event (@itemsOfInterest) {
		if ($event->{'isResolved'}) {
			$resolvedEvents{$event->{'LogMessageID'}} = $event;	
		}
	}

	my $numResolvedEvents = scalar keys %resolvedEvents;

	# Continue only if any events moved into the resolved state
	if ($numResolvedEvents) {
	
		# Query to make sure LogMessage exists in Foundation.
		# This is done to make sure that non-existent Foudation events 
		# are not accepted.
		my $logMessageIDStr = join(',', map { "'" . $_ . "'" } keys %resolvedEvents);

		my $query = qq{SELECT LogMessageID FROM LogMessage
		               WHERE LogMessageID IN ($logMessageIDStr) };

		my $dBObj = $dBObjects{'collageDB'};

		# Connect to the cached dB connection
		$dBObj->connect();

		my $dBHandle = $dBObj->getHandle();

		my $sth = undef;

		$sth = $dBHandle->prepare($query);

		$sth->execute();

		# If LogMessage exists in Foundation, go ahead and mark the event 
		# as acceptable. Otherwise don't send the accept XML for this event.
		while (my $row = $sth->fetchrow_hashref()) {
			my $logMessageID = $row->{'LogMessageID'};

			if (defined($resolvedEvents{$logMessageID})) {
				my $event             = $resolvedEvents{$logMessageID};
				$event->{'canAccept'} = 1;
			}
		}
	}
}

sub updateHelpDeskBridgeDB {
	my $update = qq{UPDATE HelpDeskLookupTable
	                SET TicketStatus=?
	                WHERE LogMessageID=?};

	my $dBObj = $dBObjects{'bridgeDB'};

	# Connect to the cached dB connection
	$dBObj->connect();

	my $dBHandle = $dBObj->getHandle();

	my $sth = undef;

	$sth = $dBHandle->prepare($update);

	# Update any items that may have changed in the BridgeDB
	for my $event (@itemsOfInterest) {
		if ($event->{'hasChanged'}) {	
			$sth->execute($event->{'TicketStatus'}, $event->{'LogMessageID'});
		}
	}
}

sub acceptEventsInFoundation {
	my @eventsToAccept = ();
	
	# Create a list of events to be accepted
	for my $event (@itemsOfInterest) {
		if ($event->{'canAccept'}) {
			push(@eventsToAccept, $event);
		}
	}

	$numEventsToAccept = scalar @eventsToAccept;

	# If there is 1 or more events to accept, then inform Foundation.
	# Please note that markEventsToAccept has already made sure that
	# the LogMessage currently exists in Foundation.
	if ($numEventsToAccept) {
		my $logMessageIDStr = getLogMessageString(\@eventsToAccept);

		addApplicationTypeToEvents(\@eventsToAccept, $logMessageIDStr);

		my $acceptXML = constructAcceptXML(\@eventsToAccept);

		sendToFoundation($acceptXML);
	}
}

sub constructAcceptXML {
	my $eventsToAccept = shift(@_);
	my %appTypeBins   = ();

	# Put the consoleEvents into bins defined by Application Type
	foreach my $event (@$eventsToAccept) {
		my $appType = $event->{'ApplicationType'};

		if (not defined($appTypeBins{$appType})) {
			my @emptyArray         = ();
			$appTypeBins{$appType} = \@emptyArray;
		}

		my $bin = $appTypeBins{$appType};
		push(@$bin, $event);
	}

	my $adapterEnvelope = "<Adapter Session=\"1002\" AdapterType=\"SystemAdmin\">";
	my $commandEnvelope = "";

	# For each bin, construct the appropriate Command XML
	foreach my $appType (keys %appTypeBins) {
		my $bin = $appTypeBins{$appType};

		$commandEnvelope .= "<Command Action=\"MODIFY\"" .
		                    " ApplicationType=\"$appType\"> ";

		my $logMessageXML = "";

		foreach my $event (@$bin) {
			my $id = $event->{'LogMessageID'};

			$logMessageXML .= "<LOGMESSAGE LogMessageId=\'$id\'"    .
			                  " OperationStatus=\'CLOSED\' />";
		}

		$commandEnvelope .= "$logMessageXML</Command>";
	}

	$adapterEnvelope .= "$commandEnvelope</Adapter>";

	return $adapterEnvelope;
}

sub getLogMessageString {
	my $eventsToAccept = shift(@_);

	my $idString = "";	

	foreach my $event (@$eventsToAccept) {
		$idString .= "'" . $event->{'LogMessageID'} . "',";	
	}

	if (length($idString)) { chop($idString); }

	return $idString;
}

sub addApplicationTypeToEvents {
	my $eventsToAccept  = shift(@_);
	my $logMessageIDStr = shift(@_);

	my $query = qq|SELECT LogMessageID,
	               ApplicationType.Name  AS ApplicationType
	               FROM LogMessage, ApplicationType
	               WHERE
	               LogMessage.ApplicationTypeID = ApplicationType.ApplicationTypeID
	               AND
	               LogMessageID IN ($logMessageIDStr)|;

	my $dBObj = $dBObjects{'collageDB'};

	# Connect to the cached dB connection
	$dBObj->connect();

	my $dBHandle = $dBObj->getHandle();

	my $sth = undef;

	$sth = $dBHandle->prepare($query);

	$sth->execute();

	my %appTypeHash = ();

	while (my $row = $sth->fetchrow_hashref()) {
		$appTypeHash{$row->{'LogMessageID'}} = $row->{'ApplicationType'};
	}

	# Add the application type to each event
	foreach my $event (@$eventsToAccept) {
		$event->{'ApplicationType'} = $appTypeHash{$event->{'LogMessageID'}};
	}
}

sub sendToFoundation {
	my $xml    = shift(@_);
	my $socket = undef;

	my $foundationHost = $params{'FoundationHost'};
	my $foundationPort = $params{'FoundationPort'};

	$socket = IO::Socket::INET->new(PeerAddr => $foundationHost,
	                                PeerPort => $foundationPort,
	                                Type     => SOCK_STREAM);

	if ($socket) {
		if ($params{'Debug'}) {
			$logObj->log("-----------------------------------");
			$logObj->log("Sending XML to Foundation:");
			$logObj->log($xml);
			$logObj->log("-----------------------------------");
		}

		print $socket $xml;
		close($socket);
	}
	else {
		$logObj->log("Could not open connection to [$foundationHost] on port [$foundationPort]");
	}
}

sub isContainedIn()
{
	my $testMember = shift(@_);
	my $set        = shift(@_);

	# Cycle through each element of the set and check if testMember 
	# matches any element in the set.
	foreach (@{$set}) {
		if ($_ eq $testMember) { return 1; }
	}

	return 0;
}

sub trim {
	my $aString = shift(@_);

	if (defined($aString)) {
		$aString =~ s/^\s+//;  # remove leading  whitespace
		$aString =~ s/\s+$//;  # remove trailing whitespace
	}

	return $aString;
}

sub isProcessAlreadyRunning {
	my $processMatch = $params{'ProcessMatch'};
	my $psApp        = $params{'PSApp'};
	my $psArgs       = $params{'PSArgs'};
	my $grepApp      = $params{'GrepApp'};

	my $results      = `$psApp $psArgs | $grepApp \"$processMatch\" | $grepApp -v $grepApp`;

	chomp($results);

	my @lineItems = split(/\n/, $results);

	if ($#lineItems+1 > 0) {

		foreach (@lineItems) {
			my $lineItem = $_;

			my @fields = split(/\s+/, $lineItem);
			my $pid    = $fields[1];

			# Return true if pid does not match pid of current process: $$
			if ($pid ne $$) { return 1; }
		}
	}

	# No conflicting processes
	return 0;
}

main();


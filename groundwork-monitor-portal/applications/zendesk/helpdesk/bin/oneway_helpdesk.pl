#!/usr/local/groundwork/perl/bin/perl -w

use lib '/usr/local/groundwork/helpdesk/lib';

use strict;
use warnings;

use Config::General;
#use Sort::Naturally;
use IO::Socket;
use Data::Dumper;

use GW::DBObject;
use GW::HelpDeskUtils;
use GW::LogFile;


# The primary configuration file
my $confFile = "/usr/local/groundwork/helpdesk/conf/oneway_helpdesk.conf";

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

	if (isUserAllowedToFileTicket()) {
		my $numEvents = scalar @{$args{'consoleEvents'}};

		# Only try to file ticket if 1 or more valid events exist
		if ($numEvents) {
			createTicket();
		}
	}

	my $stopTime  = time();
	my $deltaTime = $stopTime - $startTime;

	$logObj->log("Total Script Execution Time: $deltaTime second(s)");
	$logObj->log("");
}

sub init {
	# Read the conf files
	my $config = new Config::General($confFile);
	%params    = $config->getall();

	# Instantiate the LogFile object
	my $logFileName = $params{'HelpDeskLogFile'};
	$logObj         = new GW::LogFile();
	$logObj->setLogFile($logFileName);

	# Read in the configuration file for the external help desk module
	my $moduleConfig = new Config::General($params{'HelpDeskModuleConfFile'});
	%moduleParams    = $moduleConfig->getall();

	initBridgeDBObject();
	initCollageDBObject();
}

sub initBridgeDBObject {
	my $bridgeDBConfig = new Config::General($params{'BridgeDBCredentials'});
	my %bridgeDBParams = $bridgeDBConfig->getall();

	# Setup any special database connection parameters
	# In this case, turn off autocommit (necessary for race condition handling logic)
	my %dBParams = ( 'AutoCommit' => 0 );
	
	# Create and initialize DBObject
	$dBObjects{'bridgeDB'} = new GW::DBObject();

	$dBObjects{'bridgeDB'}->setDBHost($bridgeDBParams{'bridgeDB.host'});
	$dBObjects{'bridgeDB'}->setDBName($bridgeDBParams{'bridgeDB.name'});
	$dBObjects{'bridgeDB'}->setDBUser($bridgeDBParams{'bridgeDB.user'});
	$dBObjects{'bridgeDB'}->setDBPass($bridgeDBParams{'bridgeDB.pass'});
	$dBObjects{'bridgeDB'}->setDBParams(\%dBParams);

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

sub processInputArguments {
	my $argc = scalar @ARGV;

	if ($argc != 2) {
		printUsage();
		exit;
	}

	# The 1st argument is the user name
	$args{'operator'} = trim($ARGV[0]);

	# The 2nd argument is a string of LogMessageIDs provided
	# by the ShellScriptAction class instance that invokes this
	# script.
	#
	# This is stored in the args hash as 'logMessageIDStr'
	# for later reference after it is stripped of any duplicate
	# entries and sorted in natural ascending order.
	$args{'logMessageIDStr'} = removeDuplicates($ARGV[1]);

	# Create a an array of console event hashes corresponding to
	# each logMessage identified in the 2nd parameter.
	$args{'consoleEvents'} = getConsoleEventData();
}

sub createTicket {
	#------------------------------------------------------------------------------
	# Guard against race condition where multiple people might simultaneously
	# file a ticket on one or more overlapping console events.
	#
	# It is an error condition to file multiple tickets for a single event.
	# But it is perfectly ok to log multiple events on a single ticket.
	#------------------------------------------------------------------------------
	if (addToConcurrencyTable()) {
		if (isTicketAlreadyFiled()) {
			removeFromConcurrencyTable();
			$logObj->log("A ticket has already been filed for one or more selected events.");
		}
		else {
			# Instantiate the client specific HelpDesk object
			my $helpDeskModule = $params{'HelpDeskModule'};

			eval "use $helpDeskModule";

			if ($@) { 
				removeFromConcurrencyTable();
				$logObj->log("Could not load module: $helpDeskModule"); 
				$logObj->log($@);
			}
			else {
				my $helpDeskObj = $helpDeskModule->new();

				$helpDeskObj->setLogObj($logObj);
				$helpDeskObj->setDBObj($dBObjects{'bridgeDB'});

				my $ticketDetails = undef;

				eval {
					$ticketDetails = $helpDeskObj->createTicket(\%moduleParams,
					                                            $args{'operator'},
				                                                    $args{'consoleEvents'});
				};

				if ($@) {
					removeFromConcurrencyTable();
					$logObj->log("An error was encountered while invoking createTicket");
					$logObj->log($@);	
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
	if (defined($ticketDetails->{'FilingError'}))  { $filingError  = $ticketDetails->{'FilingError'};  }
	if (defined($ticketDetails->{'TicketNo'}))     { $ticketNo     = $ticketDetails->{'TicketNo'};	   }
	if (defined($ticketDetails->{'TicketStatus'})) { $ticketStatus = $ticketDetails->{'TicketStatus'}; }
	if (defined($ticketDetails->{'TicketStatus'})) { $clientData   = $ticketDetails->{'ClientData'};   }

	if ($filingError eq "") {
		if (($ticketNo ne "") && ($ticketStatus ne "")) {
			# No errors or blank fields, so let's continue with processing
			insertTicketDataIntoLookupTable();
			insertDataIntoFoundation();
		}
		else {
			$logObj->log("TicketNo or TicketStatus were blank.");
		}
	}
	else {
		# A filing error was encountered.
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

	# Put the consoleEvents into bins defined by Application Type
	foreach my $event (@$consoleEvents) {
		my $appType = $event->{'ApplicationType'};

		if (not defined($appTypeBins{$appType})) {
			my @emptyArray         = ();
			$appTypeBins{$appType} = \@emptyArray;
		}

		my $bin = $appTypeBins{$appType};
		push(@$bin, $event);
	}

	my $adapterEnvelope = "<Adapter Session=\"1001\" AdapterType=\"SystemAdmin\">";
	my $commandEnvelope = "";
	
	# For each bin, construct the appropriate Command XML
	foreach my $appType (keys %appTypeBins) {
		my $bin = $appTypeBins{$appType};

		$commandEnvelope .= "<Command Action=\"MODIFY\"" .
		                    " ApplicationType=\"$appType\"> ";

		my $logMessageXML = "";

		foreach my $event (@$bin) {
			my $id       = $event->{'LogMessageID'};
			my $operator = $args{'operator'};

			$logMessageXML .= "<LOGMESSAGE LogMessageId=\'$id\'" .
			                  " TicketNo=\'$ticketNo\'"  .
			                  " Operator=\'$operator\' />";		
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

sub addToConcurrencyTable {
	# User helper functions to define a query and insert set of LogMessageIDs
	my $logMessageIDQuerySet  = GW::HelpDeskUtils::generateQuerySet($args{'logMessageIDStr'});
	my $logMessageIDInsertSet = GW::HelpDeskUtils::generateInsertSet($args{'logMessageIDStr'});

	my $lockTable   = "LOCK TABLES HelpDeskConcurrencyTable Write;";
	my $unlockTable = "UNLOCK TABLES;";

	my $query = "SELECT LogMessageID "           .
	            "FROM   HelpDeskConcurrencyTable " .
	            "WHERE  LogMessageID IN $logMessageIDQuerySet";

	my $insert = "INSERT INTO HelpDeskConcurrencyTable VALUES " .
	             "$logMessageIDInsertSet;";

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
		#  Perl returns "0E0" which equates to 0 but evals to true
		# if the execute method above did not affect any rows.
		# ( http://search.cpan.org/~timb/DBI/DBI.pm#execute )

		if ($result == 0)  {
			# There are no conflicts with other processes. So it 
			# is ok to insert this LogMessageID into the 
			# concurrency table.

			$sth = $dBHandle->prepare($insert);
			$sth->execute();

			$sth = $dBHandle->prepare($unlockTable);
			$sth->execute();
			$dBHandle->commit();

			$sth->finish();

			return 1;
		}
	}

	$sth = $dBHandle->prepare($unlockTable);
	$sth->execute();
	$dBHandle->commit();

	$sth->finish();

	return 0;
}

sub removeFromConcurrencyTable {
	# Use helper class to construct query set of LogMessageIDs
	my $logMessageIDQuerySet  = GW::HelpDeskUtils::generateQuerySet($args{'logMessageIDStr'});

	my $lockTable   = "LOCK TABLES HelpDeskConcurrencyTable Write;";
	my $unlockTable = "UNLOCK TABLES;";

	my $delete = "DELETE "                        .
		     "FROM   HelpDeskConcurrencyTable " .
		     "WHERE  LogMessageID IN $logMessageIDQuerySet;";

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
	
	# Unlock the HelpDeskConcurrencyTable
	$sth = $dBHandle->prepare($unlockTable);
	$sth->execute();

	$dBHandle->commit();
	$sth->finish();
}

sub isTicketAlreadyFiled {
	# Use helper class to construct query set of LogMessageIDs
	my $logMessageIDQuerySet  = GW::HelpDeskUtils::generateQuerySet($args{'logMessageIDStr'});

	my $query = "SELECT count(*) "          .
	            "FROM   HelpDeskLookupTable " .
	            "WHERE  LogMessageID IN $logMessageIDQuerySet;";

	my $dBObj = $dBObjects{'bridgeDB'};

	# Connect to the cached dB connection
	$dBObj->connect();

	my $dBHandle = $dBObj->getHandle();

	my $sth = undef;

	$sth = $dBHandle->prepare($query);
	$sth->execute();

	my $numTicketsFiled = $sth->fetchrow_array;

	# At least 1 ticket was filed for one or more of these events
	if ($numTicketsFiled) { return 1; }

	# There were no tickets filed for any of these events
	return 0;
}

sub isUserAllowedToFileTicket {
	my $isAllowed  = 0;
	my $filterFile = $params{'AuthUsersFilterFile'};

	# Make sure the filter file is available
	if (-e $filterFile) {
		# Read the users filter file
		my $configObj = new Config::General($filterFile);
		my %usersHash = $configObj->getall();

		my @usersArray = keys %usersHash;

		foreach my $user (@usersArray) {
			# Found wildcard character
			if ($user eq '*')               { $isAllowed = 1; }

			# Found an exact match
			if ($user eq $args{'operator'}) { $isAllowed = 1; }
		}
	}

	return $isAllowed;
}


sub getConsoleEventData {
	# Array to store event information
	my @eventData = ();

	# Get the array reference to LogMessageID Strings
	my $logMessageIDStr = $args{'logMessageIDStr'};

	# Get the string of valid MonitorStatus
	my $validMonitorStatus = $params{'MonitorStatus'};

	my $query = qq|
	    SELECT LogMessageID, 
	    MonitorStatus.Name    AS MonitorStatus,
	    ApplicationType.Name  AS ApplicationType,
	    LogMessage.ReportDate,
	    LogMessage.FirstInsertDate,
	    LogMessage.LastInsertDate,
	    LogMessage.MsgCount,
	    LogMessage.DeviceID,
	    Device.DisplayName    AS DeviceDisplayName,
	    Device.Identification AS DeviceIdentification,
	    TextMessage
	    FROM LogMessage, ApplicationType, Device, MonitorStatus
	    WHERE 
	    LogMessage.ApplicationTypeID = ApplicationType.ApplicationTypeID
	    AND
	    LogMessage.DeviceID = Device.DeviceID
	    AND
	    LogMessage.MonitorStatusID = MonitorStatus.MonitorStatusID
	    AND
	    LogMessageID IN ($logMessageIDStr) 
	    AND
	    MonitorStatus.Name IN ($validMonitorStatus)
	|;

	my $dBObj = $dBObjects{'collageDB'};

	# Connect to the cached dB connection 
	$dBObj->connect();

	my $dBHandle = $dBObj->getHandle();

	my $sth = undef;

	$sth = $dBHandle->prepare($query);

	$sth->execute();

	while (my $row = $sth->fetchrow_hashref()) {
		push(@eventData, $row)
	}

	return \@eventData;
}

#------------------------------------------------------------------------------
# Method to populate the LookupTable in the bridge database.
#------------------------------------------------------------------------------
sub insertTicketDataIntoLookupTable {
	my $insert = qq{INSERT INTO HelpDeskLookupTable 
                         ( LogMessageID, DeviceIdentification, Operator, 
                           TicketNo,     TicketStatus,         ClientData)
                        VALUES (?, ?, ?, ?, ?, ?)};

	my $dBObj = $dBObjects{'bridgeDB'};

	# Connect to the cached dB connection
	$dBObj->connect();

	my $dBHandle = $dBObj->getHandle();

	my $sth = undef;

	$sth = $dBHandle->prepare($insert);

	my $operator      = $args{'operator'};
	my $consoleEvents = $args{'consoleEvents'};

	foreach my $consoleEvent (@$consoleEvents) {
		$sth->execute($consoleEvent->{'LogMessageID'},
			      $consoleEvent->{'DeviceIdentification'},
			      $operator,
		              $ticketNo,
		              $ticketStatus,
		              $clientData);
	}
}

sub removeDuplicates {
	my $idString          = shift(@_);
	my $processedIDString = "";
	
	# Remove any whitespace
	$idString = trim($idString);

	# Create an array of LogMessageIDs
	my @idList = split(/,/, $idString);

	# Weed out any duplicates	
	my %idHash = ();	
	foreach my $id (@idList) { $idHash{$id} = 1; };

	# Create a naturally sorted list	
#	my @processedIDList = nsort(keys %idHash);   # removed by DAB for sake of ZenDesk
	my @processedIDList = keys %idHash;

	# Build a stored string representation
	foreach my $id (@processedIDList) {
		$processedIDString .= "$id,";
	}

	# Remove the trailing ','
	chop($processedIDString);

	return $processedIDString;
}

#------------------------------------------------------------------------------
# Trim whitespace from front and end of strings
#------------------------------------------------------------------------------
sub trim() {
	my $aString = shift(@_);

	if (defined($aString)) {
		$aString =~ s/^\s+//;  # remove leading  whitespace
		$aString =~ s/\s+$//;  # remove trailing whitespace
	}

	return $aString;
}

sub printUsage {
	print "\n";
	print "Usage: $0 <User> <LogMessageIDString>\n";
	print "\n";
	print "              User:  a GroundWork user\n";
	print "LogMessageIDString: a comma separated list of LogMessageIDs such as ";
	print "3,35,94,380,230 \n";
	print "\n";
	print "Remember that the maximum length of command line args should ";
	print "not exceed 127,968 bytes. \n";
	print "\n\n";
}

main();


package OTRS::HelpDesk;

# Manage OTRS tickets in a GroundWork Monitor deployment.
# Copyright (c) 2013-2017 GroundWork Open Source (www.groundworkopensource.com).
# All rights reserved.  Use is subject to GroundWork commercial license terms.

#-----------------------------------------------
# Perl setup.
#-----------------------------------------------

use vars qw($VERSION);
$VERSION = '2.2';

use strict;
use warnings;

use Data::Dumper;
$Data::Dumper::Indent   = 1;
$Data::Dumper::Sortkeys = 1;

#-----------------------------------------------
# Globals and Constants
#-----------------------------------------------

my $helpDeskObj = undef;

#-----------------------------------------------

sub new {
    my $packageName = shift;

    my $self = {
	_dBObj  => undef,    # dBObject for HelpDeskBridgeDB
	_logObj => undef
    };

    # Bless the Hash
    bless $self, $packageName;

    # Pass the Reference
    return $self;
}

#--------------------------------------------------------------------------------
# Get / Set Methods
#--------------------------------------------------------------------------------

sub getLogObj { my $self = shift(@_); return $self->{_logObj}; }
sub getDBObj  { my $self = shift(@_); return $self->{_dBObj}; }

sub setLogObj {
    my ( $self, $aLogObj ) = @_;
    if ( defined($aLogObj) ) { $self->{_logObj} = $aLogObj; }
}

sub setDBObj {
    my ( $self, $aDBObj ) = @_;
    if ( defined($aDBObj) ) { $self->{_dBObj} = $aDBObj; }
}

#--------------------------------------------------------------------------------
# Method: createTicket
#
# Inputs:
#
#   - $params contains any framework-specific key/value pairs sourced from the
#     framework configuration file.  This contains pointers to related configuration
#     files, as well as certain general configuration items.
#
#   - $moduleParams contains any module-specific key/value pairs sourced from the
#     'HelpDeskModuleConfFile'.  Usually this contains items like login credentials
#     for the HelpDesk system.
#
#   - $operator is the username of the GroundWork operator who initiated the ticket
#     creation.
#
#   - $selectedEvents is an array of the user selected console events.
#
#     Each item in the array is a hash containing the following key/value pairs:
#
#          'LogMessageID'    => A unique GroundWork console message identifier
#          'MonitorStatus'   => OK, DOWN, UNREACHABLE, WARNING, CRITICAL, UNKNOWN,
#                               UP, PENDING, MAINTENANCE
#          'ApplicationType' => SYSTEM, NAGIOS, SNMPTRAP, SYSLOG
#          'ReportDate'      => date/timestamp
#          'FirstInserDate'  => date/timestamp
#          'LastInsertDate'  => date/timestamp
#          'MsgCount'        => number of times message has been reported
#          'DeviceID'        => GroundWork ID of the device
#          'DisplayName'     => Onscreen name of device
#          'Identification'  => canonical identification of the device
#                               (usually hostname or IP address)
#          'TextMessage'     => alarm message text
#
# Output:
#   A Hash reference containing the following key/value pairs must be supplied on exit:
#          'TicketNo'     => [string]  an alphanumeric ticket number
#          'TicketStatus' => [string]  status of ticket in helpdesk workflow
#          'ClientData'   => [optional string] a representation of any additional
#                            data that should be stored in the HelpDeskLookupTable.
#          'FilingError'  => [string]  if non-empty, it indicates that a ticket
#                             filing error was encountered.
#
#  If for some reason an unrecoverable error is generated, use the die construct to
#  exit this module.  The encompassing parent module will sense this condition and
#  log it in the HelpDeskLogFile.
#
#--------------------------------------------------------------------------------

sub createTicket {
    my $self           = shift(@_);
    my $params         = shift(@_);
    my $moduleParams   = shift(@_);
    my $operator       = shift(@_);
    my $selectedEvents = shift(@_);
    my $summary        = shift(@_);

    #--------------------------------------
    # Perform Ticket Creation Here
    #--------------------------------------

    require SOAP::Lite;

    $helpDeskObj = $self;    # Global references to $self
    my $logObj      = $self->getLogObj();    # Shorthand reference for log
    my %resultsHash = ();

    my %ticketProps = ();

    $ticketProps{'ApplicationType'}      = $selectedEvents->[0]->{'ApplicationType'};
    $ticketProps{'MonitorStatus'}        = $selectedEvents->[0]->{'MonitorStatus'};
    $ticketProps{'DeviceDisplayName'}    = $selectedEvents->[0]->{'DeviceDisplayName'};
    $ticketProps{'DeviceID'}             = $selectedEvents->[0]->{'DeviceID'};
    $ticketProps{'DeviceIdentification'} = $selectedEvents->[0]->{'DeviceIdentification'};
    $ticketProps{'LogMessageID'}         = $selectedEvents->[0]->{'LogMessageID'};
    $ticketProps{'ReportDate'}           = $selectedEvents->[0]->{'ReportDate'};
    $ticketProps{'MsgCount'}             = $selectedEvents->[0]->{'MsgCount'};
    $ticketProps{'TextMessage'}          = $selectedEvents->[0]->{'TextMessage'};
    $ticketProps{'Operator'}             = $operator;

    $ticketProps{'HostName'}    = '';
    $ticketProps{'ServiceName'} = '';

    # Config options from helpdesk_module.conf
    my $OTRSServer   = $moduleParams->{'OTRSServer'};
    my $OTRSUser     = $moduleParams->{'OTRSUser'};
    my $OTRSPass     = $moduleParams->{'OTRSPass'};
    my $OTRSCustomer = $moduleParams->{'OTRSCustomer'};
    my $OTRSQueue    = $moduleParams->{'OTRSQueue'};
    my $GWRKServer   = $moduleParams->{'GWRKServer'};

    # START OF HOST AND SERVICE NAME RETRIEVAL
    # get the Host name and possible service name using direct SQL since the WSEvent web service
    # in Foundation does not appear to be supported via SOAP::Lite, or SOAP::WDSL

    my %params   = ();
    my $confFile = "/usr/local/groundwork/otrs/config/oneway_helpdesk.conf";
    my $config   = new Config::General($confFile);

    %params = $config->getall();

    my %dBObjects = ();

    $dBObjects{'collageDB'} = new GW::DBObject();

    my $gwCollageDBConfig = new Config::General( $params{'GWCollageDBCredentials'} );
    my %gwCollageDBParams = $gwCollageDBConfig->getall();

    $dBObjects{'collageDB'}->setDBHost( $gwCollageDBParams{'collage.dbhost'} );
    $dBObjects{'collageDB'}->setDBName( $gwCollageDBParams{'collage.database'} );
    $dBObjects{'collageDB'}->setDBUser( $gwCollageDBParams{'collage.username'} );
    $dBObjects{'collageDB'}->setDBPass( $gwCollageDBParams{'collage.password'} );

    my $dBObj = $dBObjects{'collageDB'};

    # Create a cached dB connection
    $dBObjects{'collageDB'}->connect();

    # Connect to the cached dB connection
    $dBObj->connect();
    my $dBHandle = $dBObj->getHandle();
    my $sth      = undef;

    # First see if the Device has a corresponding Host entry
    my $query = qq|
		SELECT
			HostName
		FROM
			Host
		WHERE
			Host.DeviceID = $ticketProps{'DeviceID'}
		LIMIT 	1
		|;
    $sth = $dBHandle->prepare($query);
    $sth->execute();

    while ( my $row = $sth->fetchrow_hashref() ) {
	$ticketProps{'HostName'} = $row->{'HostName'};
    }

    # If Device DOES have a corresponding Host entry, and the Event corresponds to a NAGIOS service event then
    # retrieve the service description
    if ( $ticketProps{'HostName'} && $ticketProps{'ApplicationType'} =~ /NAGIOS/ ) {
	$query = qq|
		SELECT
			LogMessageProperty.ValueString	AS ValueString
		FROM
			LogMessageProperty,
			PropertyType
		WHERE
			LogMessageProperty.LogMessageID = $ticketProps{'LogMessageID'}
			AND
			LogMessageProperty.PropertyTypeID = PropertyType.PropertyTypeID
			AND
			PropertyType.Name = 'SubComponent'
		LIMIT	1
	|;

	$sth = $dBHandle->prepare($query);
	$sth->execute();

	my $SubComponent = '';

	while ( my $row = $sth->fetchrow_hashref() ) {
	    $SubComponent = $row->{'ValueString'};
	}

	( $ticketProps{'ServiceName'} ) = $SubComponent =~ /.*:(.*)/;
    }

    # Clean up dB connection
    $sth->finish();

    # END OF HOST AND SERVICE NAME RETRIEVAL

    import SOAP::Lite( 'autodispatch', proxy => "$OTRSServer/otrs/rpc.pl" );
    my $RPC = Core->new();

    eval {
	my $description = "GroundWork event for ";
	if ( $ticketProps{'HostName'} ) {
	    $description .= "host " . $ticketProps{'HostName'};
	    if ( $ticketProps{'ServiceName'} ) {
		$description .= ", service " . $ticketProps{'ServiceName'};
	    }
	}
	else {
	    $description .= "device " . $ticketProps{'DeviceIdentification'};
	}
	my $OTRSBody = "ApplicationType: " . $ticketProps{'ApplicationType'} . "\n";

	if ( $ticketProps{'HostName'} ) {
	    $OTRSBody .= "HostName:        " . $ticketProps{'HostName'} . "\n";
	}
	if ( $ticketProps{'ServiceName'} ) {
	    $OTRSBody .= "Service:         " . $ticketProps{'ServiceName'} . "\n";
	}

	$OTRSBody .=
	    "MonitorStatus:   "
	  . $ticketProps{'MonitorStatus'} . "\n"
	  . "ReportDate:      "
	  . $ticketProps{'ReportDate'} . "\n"
	  . "MsgCount:        "
	  . $ticketProps{'MsgCount'} . "\n"
	  . "TextMessage:     "
	  . $ticketProps{'TextMessage'} . "\n\n";

	my $jbossURL = "$GWRKServer";

	if ( $ticketProps{'HostName'} && $ticketProps{'ServiceName'} ) {
	    $jbossURL .= "/portal-statusviewer/urlmap?host=" . $ticketProps{'HostName'} . "&service=" . $ticketProps{'ServiceName'};
	}
	elsif ( $ticketProps{'HostName'} ) {
	    $jbossURL .= "/portal-statusviewer/urlmap?host=" . $ticketProps{'HostName'};
	}

	$OTRSBody .= "For more information, please go to GroundWork Monitor at " . $jbossURL . "\n";

	# create a new ticket number
	my $TicketNumber = $RPC->Dispatch( $OTRSUser, $OTRSPass, 'TicketObject', 'TicketCreateNumber' );

	# create a new ticket
	my %TicketData = (
	    TN           => $TicketNumber,
	    Title        => $description,
	    Queue        => $OTRSQueue,
	    Lock         => 'unlock',
	    Priority     => '3 normal',
	    State        => 'new',
	    CustomerID   => $OTRSCustomer,
	    CustomerUser => $OTRSCustomer,
	    OwnerID      => 3,
	    UserID       => 3,
	);
	my $TicketID = $RPC->Dispatch( $OTRSUser, $OTRSPass, 'TicketObject', 'TicketCreate', %TicketData => 1 );

	my $ArticleID = $RPC->Dispatch(
	    $OTRSUser, $OTRSPass, 'TicketObject', 'ArticleCreate',
	    TicketID                  => $TicketID,
	    ArticleType               => 'phone',                 # email-external|email-internal|phone|fax|...
	    SenderType                => 'customer',              # agent|system|customer
	    From                      => $OTRSCustomer,           # not required but useful
	    To                        => $OTRSQueue,              # not required but useful
	    ReplyTo                   => $OTRSCustomer,           # not required
	    Subject                   => $description,            # required
	    Body                      => $OTRSBody,               # required
	    Charset                   => 'utf-8',
	    HistoryType               => 'PhoneCallCustomer',     # EmailCustomer|Move|AddNote|PriorityUpdate|WebRequestCustomer|...
	    HistoryComment            => 'Customer called us.',
	    UserID                    => 3,
	    NoAgentNotify             => 0,                       # if you don't want to send agent notifications
	    MimeType                  => 'text/plain',
	    Loop                      => 0,                       # auto reject|auto follow up|auto follow up|auto remove
	    AutoResponseType          => 'auto reply',
	    ForceNotificationToUserID => '',
	    OrigHeader => { 'From' => $OTRSCustomer, 'To' => $OTRSQueue, 'Subject' => $description, 'Body' => $OTRSBody, },
	);

	if ( $ticketProps{'HostName'} ) {
	    my $returnCode = $RPC->Dispatch(
		$OTRSUser, $OTRSPass, 'TicketObject', 'TicketFreeTextSet',
		Counter  => 1,
		Key      => 'Host',
		Value    => $ticketProps{'HostName'},
		TicketID => $TicketID,
		UserID   => 3,
	    );
	}
	if ( $ticketProps{'ServiceName'} ) {
	    my $returnCode = $RPC->Dispatch(
		$OTRSUser, $OTRSPass, 'TicketObject', 'TicketFreeTextSet',
		Counter  => 2,
		Key      => 'Service',
		Value    => $ticketProps{'ServiceName'},
		TicketID => $TicketID,
		UserID   => 3,
	    );
	}

	# Fill in the results data
	$resultsHash{'TicketNo'}     = $TicketNumber;
	$resultsHash{'TicketStatus'} = "new";           # technically we should check OTRS to see the State value
	$resultsHash{'ClientData'}   = $TicketID;
	$resultsHash{'FilingError'}  = "";

    };

    if ( my $exception = $@ ) {
	$logObj->log("[Error]: $exception");
	die $@;
    }

    return \%resultsHash;
}

#--------------------------------------------------------------------------------
# Method: checkStatusOfTickets
#
# Inputs:
#   - $moduleParams contains any module specific key/value pairs sourced from the
#     'HelpDeskModuleConfFile'.  Usually this contains items like login credentials
#     for the HelpDesk system.
#
#   - $ticketedEvents is an array of hashes.
#     Each entry contains the following key/value pairs:
#       'TicketNo'     => [string] an alphanumeric ticket number
#       'TicketStatus' => [string] a status string
#                         This should be changed to reflect the status in the
#                         ticketing system if it is different.
#       'ClientData'   => [string] a value that may have been set when the ticket
#                         was created.  See the 'outputs' section of createTicket
#                         for details.
#       'hasChanged'   => [boolean] a change flag
#                         Set this to 1 if the ticket status has changed
#                         in the ticketing system.
# Outputs:
#   - Since the inputs were passed by reference, there are no explicit return
#     values.  The expectation of the calling method is that the 'hasChanged'
#     and 'TicketStatus' parameters will be altered as needed by the business
#     logic contained in this module before exiting.
#
#  If for some reason an unrecoverable error is generated, use the die construct to
#  exit this module.  The encompassing parent module will sense this condition and
#  log it in the HelpDeskLogFile.
#--------------------------------------------------------------------------------

sub checkStatusOfTickets {
    my $self           = shift(@_);
    my $moduleParams   = shift(@_);
    my $ticketedEvents = shift(@_);

    #--------------------------------------------------------------
    # Cycle through each Ticketed Event and check to see if its
    # ticket status has changed in the HelpDesk system.
    #
    # If it has, change the status of the ticketed event and
    # set the hasChanged property to 1 for that ticket.
    #--------------------------------------------------------------

    $helpDeskObj = $self;    # Global references to $self
    my $logObj = $self->getLogObj();    # Shorthand reference for log

    # Config options from helpdesk_module.conf
    my $OTRSServer = $moduleParams->{'OTRSServer'};
    my $OTRSUser   = $moduleParams->{'OTRSUser'};
    my $OTRSPass   = $moduleParams->{'OTRSPass'};

    require SOAP::Lite;
    import SOAP::Lite( 'autodispatch', proxy => "$OTRSServer/otrs/rpc.pl" );
    my $RPC = Core->new();

    eval {
	foreach my $event (@$ticketedEvents) {
	    my %TicketData =
	      $RPC->Dispatch( $OTRSUser, $OTRSPass, 'TicketObject', 'TicketGet', TicketID => $event->{'ClientData'}, UserID => 3, );

	    # perform RPC call to get ticket status

	    my $ticketStatus = $TicketData{State};

	    if ( $ticketStatus ne $event->{'TicketStatus'} ) {
		$event->{'TicketStatus'} = $ticketStatus;
		$event->{'hasChanged'}   = 1;
	    }
	}
    };

    # We save the exception message to make sure it is still unchanged when we die().
    if ($@) {
	my $exception = $@;
	chomp $exception;
	$logObj->log("ERROR:  $exception");
	die "$exception\n";
    }
}

1;

__END__


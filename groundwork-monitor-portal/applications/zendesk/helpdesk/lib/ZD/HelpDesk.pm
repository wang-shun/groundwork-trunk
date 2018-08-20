package ZD::HelpDesk;

use vars qw($VERSION); $VERSION = '1.0';

use strict;
use warnings;

use lib '/usr/local/groundwork/core/foundation/api/perl';

use Data::Dumper;

#-----------------------------------------------
# Globals
#-----------------------------------------------
my $helpDeskObj = undef;
#-----------------------------------------------

sub new {
	my $packageName = shift;

	my $self = {
		_dBObj  => undef, # dBObject for HelpDeskBridgeDB
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
sub getDBObj  { my $self = shift(@_); return $self->{_dBObj};  }

sub setLogObj {
	my ($self, $aLogObj) = @_;
	if (defined($aLogObj)) { $self->{_logObj} = $aLogObj; }
}

sub setDBObj {
	my ($self, $aDBObj) = @_;
	if (defined($aDBObj)) { $self->{_dBObj} = $aDBObj; }
}

#--------------------------------------------------------------------------------
# Method: createTicket
#
# Inputs:
#
#   - $moduleParams contains any module specific key/value pairs sourced from the
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
	my $moduleParams   = shift(@_);
	my $operator	   = shift(@_);
	my $selectedEvents = shift(@_);

	#--------------------------------------
	# Perform Ticket Creation Here
	#--------------------------------------

	$helpDeskObj    = $self;	        # Global references to $self
	my $logObj      = $self->getLogObj();   # Shorthand reference for log
	my %resultsHash = (); 
	
	# Config options from helpdesk_module.conf
        my $ZendeskURLUnescaped = $moduleParams->{'ZenDeskServer'};
        (my $ZenDeskServer = $ZendeskURLUnescaped) =~ s/\\:/:/g;
        my $ZenDeskUser   = $moduleParams->{'ZenDeskUser'};
        my $ZenDeskPass   = $moduleParams->{'ZenDeskPass'};
	my $GroundworkServerUnescaped = $moduleParams->{'GroundworkServer'};
        (my $GroundworkServer = $GroundworkServerUnescaped) =~ s/\\:/:/g;

	my %ticketProps = ();

	$ticketProps{'MonitorStatus'}        = $selectedEvents->[0]->{'MonitorStatus'};
	$ticketProps{'DeviceDisplayName'}    = $selectedEvents->[0]->{'DeviceDisplayName'};
	$ticketProps{'DeviceID'}    	     = $selectedEvents->[0]->{'DeviceID'};
	$ticketProps{'DeviceIdentification'} = $selectedEvents->[0]->{'DeviceIdentification'};
	$ticketProps{'LogMessageID'}         = $selectedEvents->[0]->{'LogMessageID'};
	$ticketProps{'ReportDate'}           = $selectedEvents->[0]->{'ReportDate'};
	$ticketProps{'MsgCount'}             = $selectedEvents->[0]->{'MsgCount'};
	$ticketProps{'TextMessage'}          = $selectedEvents->[0]->{'TextMessage'};
	$ticketProps{'Operator'}             = $operator;


	# for ZenDesk it would be ideal to send in the correct email address for the Operator.  In this first pass
	# we will fake an email address.

	eval {
		my $description = $ticketProps{'DeviceDisplayName'} . " - " . $ticketProps{'MonitorStatus'} . ": " . $ticketProps{'TextMessage'};

		use HTTP::Request::Common qw(POST PUT);
		use LWP::UserAgent;

		my $ua = LWP::UserAgent->new;

		my $ZenDeskServerShort = $ZenDeskServer;
		$ZenDeskServerShort =~ s/http\:\/\///;
		$ZenDeskServerShort .= ":80";

		$ua->credentials($ZenDeskServerShort, 'Web Password', $ZenDeskUser, $ZenDeskPass);

		my $url = $ZenDeskServer . "/tickets.xml";

		my $req = POST $url,
			'Content-Type'  => 'application/xml',
			'Content'	=>
			'<ticket><description>' . $description . '</description><requester-name>' . $ticketProps{'Operator'} . '</requester-name>
			<requester-email>' . $ZenDeskUser .'</requester-email></ticket>';

		my $res = $ua->request($req);

		if ($res->is_success) {

			my $ticketNo = $res->as_string;
			$ticketNo =~ s/\n//g;
			$ticketNo =~ s/.*Location: .*?tickets\/(\d+)\.xml.*/$1/;

			# Fill in the results data
			$resultsHash{'TicketNo'}     = $ticketNo;
			$resultsHash{'TicketStatus'} = 0;     # technically we should check ZenDesk to see the status-id value
			$resultsHash{'ClientData'}   = "";
			$resultsHash{'FilingError'}  = "";
		}
		else {
			# An error was returned from the HTTP POST call.
			my $postErrorMsg = $res->status_line;
			my $error = "HTTP POST to $url returned an error.  $postErrorMsg.";

			die $error;
		}

		# since we now have a created ticket, it would be nice to put the direct JBoss URL to the host/service issue in a comment
		# PUT /tickets/#{id}.xml
		# <comment>
		#   <is-public>false</is-public>
		#   <value>URL</value>
		# </comment>
		#  response code is a 200.

		$url = $ZenDeskServer . "/tickets/" . $resultsHash{'TicketNo'} . ".xml";

my $jbossURL= $GroundworkServer . "/portal-statusviewer/urlmap?host=" . $ticketProps{'DeviceDisplayName'};

#		my $jbossURL = $GroundworkServer . "/portal/status/HostView+" . $ticketProps{'DeviceID'} . "?name=" . $ticketProps{'DeviceDisplayName'} .
#			"&svcmd=create&path=" . $ticketProps{'DeviceDisplayName'} ;

		$req = PUT $url,
			'Content-Type'  => 'application/xml',
			'Content'	=>
			'<comment><is-public>true</is-public>
			<value>' . $jbossURL . '</value></comment>';

		$res = $ua->request($req);

		if (! $res->is_success) {
			# An error was returned from the HTTP PUT call.
			my $putErrorMsg = $res->status_line;
			my $error = "HTTP PUT to $url returned an error.  $putErrorMsg.";

			die $error;
		}


	};

	if (my $exception = $@) {
		$logObj->log("[Error]: $exception");
		die $@;
	}

	return \%resultsHash;
}

#--------------------------------------------------------------------------------
# Method: updateStatusForTickets
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
sub updateStatusForTickets {
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

	$helpDeskObj = $self;	            # Global references to $self
	my $logObj   = $self->getLogObj();   # Shorthand reference for log

	# Config options from helpdesk_module.conf
        my $ZendeskURLUnescaped = $moduleParams->{'ZenDeskServer'};
        (my $ZenDeskServer = $ZendeskURLUnescaped) =~ s/\\:/:/g;
	my $ZenDeskUser   = $moduleParams->{'ZenDeskUser'};
	my $ZenDeskPass   = $moduleParams->{'ZenDeskPass'};

	eval {
		foreach my $event (@$ticketedEvents) {
			# perform the HTTP GET to get ticket status
			use LWP::UserAgent;

			my $ua = LWP::UserAgent->new;
			my $ZenDeskServerShort = $ZenDeskServer;
			$ZenDeskServerShort =~ s/http\:\/\///;
			$ZenDeskServerShort .= ":80";

			$ua->credentials($ZenDeskServerShort, 'Web Password', $ZenDeskUser, $ZenDeskPass);

			my $url = $ZenDeskServer . "/tickets/" . $event->{"TicketNo"} . ".xml";
			my $res = $ua->get( $url );

			if ($res->is_success) {
				my $ticketStatus = $res->as_string;
				$ticketStatus =~ s/\n//g;
				$ticketStatus =~ s/.*status-id type=\"integer\">(\d+).*/$1/;

                                if ($ticketStatus ne $event->{'TicketStatus'}) {
                                        $event->{'TicketStatus'} = $ticketStatus;
                                        $event->{'hasChanged'}   = 1;
				}
			}
			else {
			# An error was returned from the HTTP GET call.
				my $getErrorMsg = $res->status_line;
				my $error = "HTTP GET to $url returned an error.  $getErrorMsg.";

				die $error;
			}

		}
	};

	if (my $exception = $@) {
		$logObj->log("[Error]: $exception");
		die $@;
	}
}

1;
__END__


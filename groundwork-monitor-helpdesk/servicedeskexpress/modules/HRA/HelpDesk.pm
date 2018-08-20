package HRA::HelpDesk;

use vars qw($VERSION); $VERSION = '2.1';

#Bugfix to handle problem when collagequery returns empty hash. KDS 6/28/11
#2.1 RE added HRA Code from 1.1 version

use strict;
use warnings;

use lib '/usr/local/groundwork/core/foundation/api/perl';

use CollageQuery;
use SOAP::Lite;
use Data::Dumper;

#-----------------------------------------------
# Globals
#-----------------------------------------------
my $helpDeskObj = undef;
my $uri         = 'http://bmc.com/webservices';
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
	
	# Config options from servicedeskexpress_module.conf
	my $GWInsertUpdateProxy = $moduleParams->{'GWInsertUpdateProxy'};

	# For HRA we will add all of the logic to first identify what Site Code Host Groups the host belongs
	# to, perform any conversion of GroundWork fields to HRA SDE fields that cannot be accomplished on the
	# SDE Integration Console side, and then perform the web services request to create a ticket.

	my %ticketProps = ();

#	$ticketProps{'MonitorStatus'}        = $selectedEvents->[0]->{'MonitorStatus'};
	$ticketProps{'MonitorStatus'}        = '1-VIP';
	$ticketProps{'DeviceDisplayName'}    = $selectedEvents->[0]->{'DeviceDisplayName'};
	$ticketProps{'DeviceIdentification'} = $selectedEvents->[0]->{'DeviceIdentification'};
	$ticketProps{'LogMessageID'}         = $selectedEvents->[0]->{'LogMessageID'};
	$ticketProps{'ReportDate'}           = $selectedEvents->[0]->{'ReportDate'};
	$ticketProps{'MsgCount'}             = $selectedEvents->[0]->{'MsgCount'};
	$ticketProps{'TextMessage'}          = $selectedEvents->[0]->{'TextMessage'};
	$ticketProps{'Operator'}             = $operator;
	$ticketProps{'HostGroups'}           = '';

#KDS strips seconds from ReportDate
	$logObj->log("Report Date =$ticketProps{'ReportDate'}");
	my @words = split( /\./, $ticketProps{'ReportDate'} );
	$ticketProps{'ReportDate'} = $words[0];
	$logObj->log("Report Date =$ticketProps{'ReportDate'}");
#KDS End strip seconds

	eval {
		my $collageObj = CollageQuery->new();
		my $hostGroups = $collageObj->getHostGroups();

		foreach my $hostGroup (keys %{$hostGroups}) {
		  if ( $hostGroup !~ /^sc\-/) { next; }

		  my $hosts = $collageObj->getHostsForHostGroup($hostGroup);

	          if ( ref($hosts) eq 'HASH' and $hosts->{$ticketProps{'DeviceDisplayName'}} ) {
			$ticketProps{'HostGroups'} .= "$hostGroup,"; 
		  }
		}
	};

	if (my $exception = $@) {
		$logObj->log("[Error]: CollageQuery.pm exception: $exception");
		die $@;
	}

	# Assign default host group if none found, or remove trailing comma from 
	# hostgroup list if it is not empty
	if ( ! $ticketProps{'HostGroups'} ) { $ticketProps{'HostGroups'} = 'NONE';  }
	else                                { chop($ticketProps{'HostGroups'});     }

#KDS Add find host alias code
        # Obtain the Monarch alias field for the DeviceDisplayName.  Because of the Perl modules required
        # to communicate with Monarch using the Monarch API, this is easier to handle as a call to a
        # separate executable.

        my $get_host_alias_cmd = '/usr/local/groundwork/servicedeskexpress/bin/get_host_alias.pl';
        my $host_alias = qx/$get_host_alias_cmd "$selectedEvents->[0]->{'DeviceDisplayName'}"/;

        if ( $host_alias =~ /^\[Error\]\: (.*)/ ) {
                $logObj->log("[Error]: get_host_alias.pl exception: $1");
                die $1;
        }

        # Assign default alias if none found
        if ( ! $host_alias )    { $ticketProps{'HostAlias'} = 'NONE';           }
        else  { $ticketProps{'HostAlias'} = $host_alias;      }

#KDS End find host alias code

	eval { 
		my $soapObj = SOAP::Lite
       	                        -> proxy( $GWInsertUpdateProxy )
       	                        -> on_action( \&constructSoapActionForDotNET )
       	                        -> on_fault( \&createTicketSoapFault ); 

		my $GWInsertUpdateMethod = SOAP::Data->name('GroundWorkInsertUpdate')->attr({'xmlns' => "$uri/" });

	        my @GWInsertUpdateParams = (
	       	  SOAP::Data->name('GroundWorkInsertUpdate' =>
 	             SOAP::Data->value(
	              SOAP::Data->name('MonitorStatus' => $ticketProps{'MonitorStatus'})
	            )
	          ),
	          SOAP::Data->name('GroundWorkInsertUpdate' =>
	            SOAP::Data->value(
	             SOAP::Data->name('DeviceDisplayName' => $ticketProps{'DeviceDisplayName'})
	           )
	          ),
	          SOAP::Data->name('GroundWorkInsertUpdate' =>
	            SOAP::Data->value(
	              SOAP::Data->name('DeviceIdentification' => $ticketProps{'DeviceIdentification'})
	            )
	          ),
	          SOAP::Data->name('GroundWorkInsertUpdate' =>
	            SOAP::Data->value(
	              SOAP::Data->name('LogMessageID' => $ticketProps{'LogMessageID'})
	            )
	          ),
	          SOAP::Data->name('GroundWorkInsertUpdate' =>
	            SOAP::Data->value(
	              SOAP::Data->name('ReportDate' => $ticketProps{'ReportDate'})
	            )
	          ),
	          SOAP::Data->name('GroundWorkInsertUpdate' =>
	            SOAP::Data->value(
	              SOAP::Data->name('MsgCount' => $ticketProps{'MsgCount'})
	            )
	          ),
	          SOAP::Data->name('GroundWorkInsertUpdate' =>
	            SOAP::Data->value(
	              SOAP::Data->name('TextMessage' => $ticketProps{'TextMessage'})
	            )
	          ),
	          SOAP::Data->name('GroundWorkInsertUpdate' =>
	            SOAP::Data->value(
	              SOAP::Data->name('HostGroups' => $ticketProps{'HostGroups'})
	            )
	          ),
	          SOAP::Data->name('GroundWorkInsertUpdate' =>
	            SOAP::Data->value(
	              SOAP::Data->name('Operator' => $ticketProps{'Operator'})
	           )
	          ),
#KDS Add host Alias to ticket hash
	          SOAP::Data->name('GroundWorkInsertUpdate' =>
                    SOAP::Data->value(
                      SOAP::Data->name('HostAlias' => $ticketProps{'HostAlias'})
                   )
                  )
#KDS End add alias
	        );

		my $response = $soapObj->call($GWInsertUpdateMethod => @GWInsertUpdateParams);
		my $result   = $response->result();

		if ($result->{'HasError'} eq 'false') {
			# SOAP call was successful.

			my $incidentNumber = $result->{'IncidentList'}->{'Incident'}->{'IncidentNumber'};
			my $incidentState  = $result->{'IncidentList'}->{'Incident'}->{'State'};

			# Fill in the results data
			$resultsHash{'TicketNo'}     = $incidentNumber;
			$resultsHash{'TicketStatus'} = $incidentState;
			$resultsHash{'ClientData'}   = "";
			$resultsHash{'FilingError'}  = "";
		}
		else {
			# An error was returned from the SOAP call.
			my $soapErrorMsg = $result->{'ErrorMessage'};

			my $error = "SOAP call to $GWInsertUpdateMethod returned an error.  $soapErrorMsg";

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

	# Config options from servicedeskexpress_module.conf
	my $GWSelectProxy = $moduleParams->{'GWSelectProxy'};

	eval {
		foreach my $event (@$ticketedEvents) {
			# perform the web service query to get the content
			my $soapObj = SOAP::Lite
				        -> proxy ( $GWSelectProxy )
				        -> on_action( \&constructSoapActionForDotNET )
				        -> on_fault( \&updateStatusForTicketsSoapFault ); 

			my $GWSelectMethod = SOAP::Data->name('GroundWorkSelect')->attr({'xmlns' => "$uri/" });
			my $GWSelectParams = SOAP::Data->name('GroundWorkSelect' =>
					SOAP::Data->value(
						SOAP::Data->name('Incident' => $event->{'TicketNo'})
					)
				);

			my $response = $soapObj->call($GWSelectMethod => $GWSelectParams);
			my $result   = $response->result();

			if ($result->{'HasError'} eq 'false') {

				# test code by Dave Blunt on 2009-11-16 to look for tickets with no state
                                eval {
                                        if (! $result->{'IncidentList'}->{'Incident'}->{'State'} ) {
                                        }
                                };
                                if (my $exception = $@) {
                                        $logObj->log("[Error]: Failure to process TicketNo " . $event->{'TicketNo'});
                                        die $@;
                                }

				my $ticketStatus = $result->{'IncidentList'}->{'Incident'}->{'State'};

				if ($ticketStatus ne $event->{'TicketStatus'}) {
					$event->{'TicketStatus'} = $ticketStatus;
					$event->{'hasChanged'}   = 1;
				}
			}
			else {
				# An error was returned from the SOAP call.
				my $soapErrorMsg = $result->{'ErrorMessage'};

				my $error = "SOAP call to $GWSelectMethod returned an error.  $soapErrorMsg";

				die $error;
			}
		}
	};

	if (my $exception = $@) {
		$logObj->log("[Error]: $exception");
		die $@;
	}
}

#--------------------------------------------------------------------------------
# Method: constructSoapActionForDotNET
#
# The SOAP spec does not specify the format for the soapAction
# HTTP Header.
#
# Both SOAP::Lite and SOAP::WSDL construct a soapAction header that
# looks like:
#              http://bmc.com/webservices#GroundWorkInsertUpdate
#
# But .NET expects the following format:
#              http://bmc.com/webservices/GroundWorkInsertUpdate
#
# This support method provides the necessary translation.
#--------------------------------------------------------------------------------
sub constructSoapActionForDotNET {
        my $service = $_[1];
        return join '/', $uri, $service;
}

#--------------------------------------------------------------------------------
# Method: createTicketSoapFault
#--------------------------------------------------------------------------------
sub createTicketSoapFault {
	my ($soapObj, $response) = @_;

	my $error = "[Error]: createTicket SOAP Fault.  "              .
	            "Transport Status: " . $soapObj->transport->status . ".  ";

	if (ref $response) {
		$error .= "Fault Code: "   . $response->faultcode   . ".  " .
		          "Fault String: " . $response->faultstring . ".  ";
	}

	my $logObj = $helpDeskObj->getLogObj();

	$logObj->log($error);

	die "Method createTicketSoapFault invoked.  Exception generated";
}

#--------------------------------------------------------------------------------
# Method: updateStatusForTicketsSoapFault
#--------------------------------------------------------------------------------
sub updateStatusForTicketSoapFault {
        my ($soapObj, $response) = @_;

	my $error = "[Error]: updateStatusForTickets  SOAP Fault.  "   .
	            "Transport Status: " . $soapObj->transport->status . ".  ";

	if (ref $response) {
		$error .= "Fault Code: "   . $response->faultcode   . ".  " .
		          "Fault String: " . $response->faultstring . ".  ";
	}

	my $logObj = $helpDeskObj->getLogObj();

	$logObj->log($error);

	die "Method updateStatusForTicketsSoapFault invoked.  Exception generated";
}

1;
__END__


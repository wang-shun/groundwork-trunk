package GW::HelpDesk;

use vars qw($VERSION); $VERSION = '1.0';

use strict;
use warnings;
use Data::Dumper;

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
	my $self         = shift(@_);
	my $argsHash     = shift(@_);
	my $moduleParams = shift(@_);

	#--------------------------------------
	# Perform Ticket Creation Here
	#--------------------------------------

	#--------------------------------------
	# Now return the results.
	# Be sure to override the default values listed below
	# with any site specific entries.
	#--------------------------------------
	my %resultsHash              = ();
	$resultsHash{'TicketNo'}     = "DefaultTicketNo";
	$resultsHash{'TicketStatus'} = "DefaultTicketStatus";
	$resultsHash{'ClientData'}   = "DefaultClientData";
	$resultsHash{'FilingError'}  = "";

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

	# Sample Pseudo code:

	foreach my $event (@$ticketedEvents) {
		# Get TicketStatus of $event from Help Desk

		# Set hasChanged property if TicketStatus is
	        # different: $event->{'hasChanged'} = 1;
	}
}

1;
__END__


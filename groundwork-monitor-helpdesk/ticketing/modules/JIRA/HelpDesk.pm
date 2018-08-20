package JIRA::HelpDesk;

# Manage JIRA tickets in a GroundWork Monitor deployment.
# Copyright (c) 2013 GroundWork Open Source (www.groundworkopensource.com).
# All rights reserved.  Use is subject to GroundWork commercial license terms.

#-----------------------------------------------
# Perl setup.
#-----------------------------------------------

use vars qw($VERSION);
$VERSION = '1.0';

use strict;
use warnings;

use Data::Dumper;
$Data::Dumper::Indent   = 1;
$Data::Dumper::Sortkeys = 1;

use POSIX qw(strftime ceil :signal_h);
use POSIX::RT::Timer;

use JSON;
use LWP::UserAgent;
use HTTP::Request::Common;
use URI::URL;  # to define url()

# use GW::Logger;
use GW::Nagios;

#-----------------------------------------------
# Globals and Constants
#-----------------------------------------------

my $helpDeskObj = undef;

my $UnknownHostName           = 'Unknown Host';
my $UnknownServiceDescription = 'Unknown Service';
my $UnknownApplicationType    = 'Unknown Application';
my $UnknownSeverity           = 'Unknown Severity';
my $UnknownMonitorStatus      = 'Unknown Status';

#-----------------------------------------------

sub new {
    my $packageName = shift;

    my $self = {
	timeout => 10,
	debug   => 0,
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

# FIX LATER:  provide routines to get/set the timeout and debug parameters,
# and perhaps call them as appropriate from the oneway and twoway scripts

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
# Method: readMappingFile
#
# This internal routine is used to swallow external mapping files, turning
# them into hashes that point to arrays of corresponding values.
#--------------------------------------------------------------------------------

sub readMappingFile {
    my $self   = shift(@_);
    my $file   = shift(@_);
    my $keys   = shift(@_);
    my $values = shift(@_);

    my $logObj = $self->getLogObj();    # Shorthand reference for log

    my %mapping = ();

    my $fields = $keys + $values;

    if ( $keys == 0 || $values == 0 ) {
	die "ERROR:  #keys or #values for file \"$file\" is zero\n";
    }

    open MAPPING, '<', $file or die "ERROR:  Cannot open file \"$file\" for reading ($!).\n";

    while (<MAPPING>) {
	s/^\s+//;    # kill all leading whitespace
	next if /^#/;    # skip comment lines
	next if /^$/;    # skip blank lines
	my @split_line = split /\t+/;
	if ( @split_line == $fields ) {

	    # Clean up whitespace around all individual fields.
	    for (@split_line) {
		s/^\s+//;    # trim  leading whitespace
		s/\s+$//;    # trim trailing whitespace
	    }
	    my $ref = \%mapping;
	    for ( my $key = 1 ; $key <= $keys ; ++$key ) {
		my $field = shift @split_line;
		if ( $key == $keys ) {
		    $ref->{$field} = \@split_line;
		}
		else {
		    $ref = \%{ $ref->{$field} };
		}
	    }
	}
	elsif ( @split_line < $fields ) {
	    $logObj->log("ERROR:  in file $file, first field \"$split_line[0]\" has too few following fields\n");
	}
	else {
	    $logObj->log("ERROR:  in file $file, first field \"$split_line[0]\" has too many following fields\n");
	}
    }

    close MAPPING;

    # Verification during development; not fully general.  Might be somewhat useful in the field.
    if (0) {
	foreach my $key ( keys %mapping ) {
	    if ( ref $mapping{$key} eq 'ARRAY' ) {
		$logObj->log("$key => $mapping{$key}\n");
	    }
	    else {
		foreach my $subkey ( keys %{ $mapping{$key} } ) {
		    $logObj->log("$key, $subkey => $mapping{$key}{$subkey}\n");
		}
	    }
	}
    }

    return \%mapping;
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
#          'HostName'        => provided for convenience so a secondary lookup in the
#                               LogMessage and Host tables need not be done to retrieve
#                               this value; value may be undefined (e.g., for a
#                               non-NAGIOS message)
#          'ServiceDescription' => service name, provided for convenience so a secondary
#                                  lookup in the LogMessage and ServiceStatus tables
#                                  need not be done to retrieve this value; value may be
#                                  undefined (e.g., for a non-NAGIOS message)
#          'HostNotes'       => Monarch-configured host notes
#          'ServiceNotes'    => Monarch-configured service notes
#          'ApplicationType' => SYSTEM, NAGIOS, SNMPTRAP, SYSLOG
#          'HostMonitorStatus'=> UP, DOWN, UNREACHABLE, PENDING, MAINTENANCE
#                                (related-host status for a service message)
#          'MonitorStatus'   => OK, DOWN, UNREACHABLE, WARNING, CRITICAL, UNKNOWN,
#                               UP, PENDING, MAINTENANCE
#          'Severity'        => short event severity description
#          'ReportDate'      => timestamp, not just a date ("YYYY-MM-DD hh:mm:ss")
#          'FirstInsertDate' => timestamp, not just a date ("YYYY-MM-DD hh:mm:ss")
#          'LastInsertDate'  => timestamp, not just a date ("YYYY-MM-DD hh:mm:ss")
#          'MsgCount'        => number of times message has been reported
#          'TextMessage'     => alarm message text
#          'DeviceID'        => GroundWork ID of the device
#          'DeviceDisplayName'     => Onscreen name of device
#          'DeviceIdentification'  => canonical identification of the device
#                                     (usually hostname or IP address)
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

    $self->{error} = '';

    #--------------------------------------
    # Perform Ticket Creation Here
    #--------------------------------------

    $helpDeskObj    = $self;                 # Global references to $self
    my $logObj      = $self->getLogObj();    # Shorthand reference for log
    my %resultsHash = ();

    my %dBObjects = ();

    $dBObjects{'monarchDB'} = new GW::DBObject();

    my $MonarchDBConfig = new Config::General( $params->{'MonarchDBCredentials'} );
    my %MonarchDBParams = $MonarchDBConfig->getall();

    $dBObjects{'monarchDB'}->setDBHost( $MonarchDBParams{'monarch.dbhost'} );
    $dBObjects{'monarchDB'}->setDBName( $MonarchDBParams{'monarch.database'} );
    $dBObjects{'monarchDB'}->setDBUser( $MonarchDBParams{'monarch.username'} );
    $dBObjects{'monarchDB'}->setDBPass( $MonarchDBParams{'monarch.password'} );

    my $dBObj = $dBObjects{'monarchDB'};

    # FIX LATER:  Are we really making a cached connection here?  What about using connect_cached() instead?
    # Look at the DBI documentation on this in detail.  Given that this module is only invoked intermittently
    # and does not persist, it's not clear that creating an extra connection will have any lasting deleterious
    # effect on the system.  But it's also not clear whether it's having any beneficial effect, either.
    #
    # Create a cached dB connection
    $dBObjects{'monarchDB'}->connect();

    # Connect to the cached dB connection
    $dBObj->connect();
    my $dBHandle = $dBObj->getHandle();
    my $sth      = undef;
    my $query    = undef;

    my $JIRA_REST_Anchor_Point     = $moduleParams->{'JIRA_REST_Anchor_Point'};
    my $JIRA_REST_User             = $moduleParams->{'JIRA_REST_User'};
    my $JIRA_REST_Pass             = $moduleParams->{'JIRA_REST_Pass'};
    my $JIRA_New_Ticket_Project    = $moduleParams->{'JIRA_New_Ticket_Project'};
    my $JIRA_New_Ticket_Issue_Type = $moduleParams->{'JIRA_New_Ticket_Issue_Type'};

    # Normalize the REST anchor-point parameter, so we can depend on its form later on.
    chop $JIRA_REST_Anchor_Point if $JIRA_REST_Anchor_Point =~ m{/$};

    my @LogMessageIDs       = ();
    my %ServiceDescriptions = ();
    my %ApplicationTypes    = ();
    my %Severities          = ();
    my %MonitorStatuses     = ();
    my @acknowledgements    = ();

    my @description = ();
    push @description, '||Host||Service||MonitorStatus||Severity||Message||ReportDate||';

    foreach my $event (@$selectedEvents) {
	my $LogMessageID         = $event->{'LogMessageID'};
	my $DeviceID             = $event->{'DeviceID'};
	my $HostName             = $event->{'HostName'};
	my $ServiceDescription   = $event->{'ServiceDescription'};
	my $HostNotes            = $event->{'HostNotes'};
	my $ServiceNotes         = $event->{'ServiceNotes'};
	my $ApplicationType      = $event->{'ApplicationType'};
	my $HostMonitorStatus    = $event->{'HostMonitorStatus'};
	my $MonitorStatus        = $event->{'MonitorStatus'};
	my $Severity             = $event->{'Severity'};
	my $ReportDate           = $event->{'ReportDate'};
	my $TextMessage          = $event->{'TextMessage'};
	my $DeviceIdentification = $event->{'DeviceIdentification'};

	$HostName           = $UnknownHostName           if not defined $HostName;
	$ServiceDescription = $UnknownServiceDescription if not defined $ServiceDescription;
	$MonitorStatus      = $UnknownMonitorStatus      if not defined $MonitorStatus;

	push @description, "|$HostName|$ServiceDescription|$MonitorStatus|$Severity|$TextMessage|$ReportDate|";

	push @LogMessageIDs, $LogMessageID;
	next;

	$ServiceDescriptions{ defined($ServiceDescription) ? $ServiceDescription : $UnknownServiceDescription } = 1;
	$ApplicationTypes{    defined($ApplicationType)    ? $ApplicationType    : $UnknownApplicationType }    = 1;
	$Severities{          defined($Severity)           ? $Severity           : $UnknownSeverity }           = 1;
	$MonitorStatuses{     defined($MonitorStatus)      ? $MonitorStatus      : $UnknownMonitorStatus }      = 1;

	$HostNotes    =~ s/<br>/\n/g if defined $HostNotes;
	$ServiceNotes =~ s/<br>/\n/g if defined $ServiceNotes;

	my @HostGroups    = ();

	# FIX MINOR:  Since Monarch really represents a possible future configuration and not necessarily
	# the running configuration, these mappings probably should have been done by equivalent lookups
	# within the GWCollageDB database, not the monarch database.
	#
	# Find all hostgroups that this host belongs to.
	$query = qq{
	    select distinct hg.name from hostgroups hg, hostgroup_host hgh, hosts h
	    where h.name = ? and hgh.host_id = h.host_id and hg.hostgroup_id = hgh.hostgroup_id
	};
	$sth = $dBHandle->prepare($query);
	$sth->execute($HostName);
	while ( my @values = $sth->fetchrow_array() ) {
	    push @HostGroups, $values[0];
	}
	$sth->finish();

	if ( $ApplicationType eq 'NAGIOS' ) {
	    ## Create and queue a persistent and notifying acknowledgement to Nagios, based on the $HostName
	    ## and $ServiceDescription.  "Persistent and notifying" means that certain command-variant flags
	    ## must be set in the acknowledgement.
	    my $now = time();
	    if ( defined $ServiceDescription ) {
		## Command Format:
		## ACKNOWLEDGE_SVC_PROBLEM;<host_name>;<service_description>;<sticky>;<notify>;<persistent>;<author>;<comment>
		## Description: Allows you to acknowledge the current problem for the specified service. By
		## acknowledging the current problem, future notifications (for the same service state) are
		## disabled. If the "sticky" option is set to one (1), the acknowledgement will remain until the
		## service returns to an OK state. Otherwise the acknowledgement will automatically be removed
		## when the service changes state. If the "notify" option is set to one (1), a notification will be
		## sent out to contacts indicating that the current service problem has been acknowledged. If the
		## "persistent" option is set to one (1), the comment associated with the acknowledgement will
		## survive across restarts of the Nagios process. If not, the comment will be deleted the next
		## time Nagios restarts.
		push @acknowledgements,
"[$now] ACKNOWLEDGE_SVC_PROBLEM;$HostName;$ServiceDescription;1;1;1;$operator;Service problem tracked in JIRA issue {ISSUE_KEY}.\n";
	    }
	    else {
		## Command Format:
		## ACKNOWLEDGE_HOST_PROBLEM;<host_name>;<sticky>;<notify>;<persistent>;<author>;<comment>
		## Description: Allows you to acknowledge the current problem for the specified host. By acknowledging
		## the current problem, future notifications (for the same host state) are disabled. If the
		## "sticky" option is set to one (1), the acknowledgement will remain until the host returns to
		## an UP state. Otherwise the acknowledgement will automatically be removed when the host changes
		## state. If the "notify" option is set to one (1), a notification will be sent out to contacts
		## indicating that the current host problem has been acknowledged. If the "persistent" option is
		## set to one (1), the comment associated with the acknowledgement will survive across restarts
		## of the Nagios process. If not, the comment will be deleted the next time Nagios restarts.
		push @acknowledgements,
		  "[$now] ACKNOWLEDGE_HOST_PROBLEM;$HostName;1;1;1;$operator;Host problem tracked in JIRA issue {ISSUE_KEY}\n";
	    }
	}
    }

    # Create and submit a JIRA issue.
    my $LogMessageIDs = join( ',', sort { $a <=> $b } @LogMessageIDs );
    my $description = join( "\r\n", @description );

    # Provide a default Summary line for the JIRA, if the user has not typed in anything interesting.
    $summary = 'GWMEE Log ID' . ( @LogMessageIDs == 1 ? '' : 's' ) . ": $LogMessageIDs" if $summary =~ /^\s*$/;

    my %ticket = ();
    $ticket{fields}{project}{key}    = $JIRA_New_Ticket_Project;
    $ticket{fields}{summary}         = $summary;
    $ticket{fields}{description}     = $description;
    $ticket{fields}{issuetype}{name} = $JIRA_New_Ticket_Issue_Type;
    $ticket{fields}{reporter}{name}  = $operator;

    # The new JIRA issue will have fields:
    #	{
    #	"fields":
    #	    {
    #	    "project":
    #		{
    #		"key": "$JIRA_New_Ticket_Project"
    #		},
    #	    "summary": "$summary",
    #	    "description": "$description",
    #	    "issuetype":
    #		{
    #		"name": "Task"
    #		},
    #	    "reporter":
    #		{
    #		"name": "$operator"
    #		}
    #	    }
    #	}

    my $issue_key = undef;
    eval {
	my $json_text = encode_json \%ticket;

	my $ua = LWP::UserAgent->new;
	$ua->agent('JIRA REST Client/1.0');

	my $req = HTTP::Request->new( POST => "$JIRA_REST_Anchor_Point/api/2/issue/" );
	$req->content_type('application/json');
	$req->authorization_basic($JIRA_REST_User, $JIRA_REST_Pass);
	$req->content($json_text);

	my $resp = undef;
	if (not do_timed_request($self, 'Create JIRA issue', $ua, $req, \$resp)) {
	    $logObj->log( "ERROR:  $self->{error}" );
	    die $self->{error} . "\n";
	}

	## check the success of the request, at the HTTP::Request level
	my $http_status = $resp->code;
	my $json_response = $resp->decoded_content( ref => 1 ); 
	my $perl_response;

	## As of this writing, the JIRA 5.2.10 REST API documentation:
	## https://developer.atlassian.com/static/rest/jira/5.2.10.html#id168761
	## is wrong about the response code returned on a successful issue creation.
	## The doc says 200, whereas we actually get a 201.
	if ( $resp->is_success && $http_status == 201 ) { 
	    $logObj->log( 'DEBUG:  successful JIRA issue creation' ) if $self->{debug};
	}
	else {
	    $logObj->log( "ERROR:  unsuccessful JIRA issue creation; HTTP status code = $http_status" );
	    chomp $$json_response;
	    $logObj->log( "ERROR:  content is:\n$$json_response" );
	    if ( $http_status == 500 && $$json_response =~ /Can't connect to.*Connection refused/s ) {
		$self->{error} = "The JIRA system appears to be down.";
	    }
	    else {
		$self->{error} = 'JIRA issue creation failure (bad HTTP status).';
		eval { $perl_response = decode_json( $$json_response ); };
		if ($@) {
		    chomp $@;
		    $logObj->log( "ERROR:  Could not parse the returned error content as JSON ($@)." );
		}
		else {
		    ## Try to decode the error response.  It might look something like:
		    ##
		    ##     {"errorMessages":[],"errors":{"reporter":"The reporter specified is not a user."}}
		    ## or like:
		    ##     {"errorMessages":[
		    ##	"You will not be able to create new issues because your JIRA evaluation period has expired, please contact your JIRA administrators."
		    ##     ],"errors":{}}
		    ##
		    ## Over time, as we experience various types of issue-creation failures, we should
		    ## improve this code to extract more detail about specific types of failures.
		    if ( @{ $perl_response->{errorMessages} } == 0) {
			if ( $perl_response->{errors}{reporter} eq 'The reporter specified is not a user.' ) {
			    ## In this case, we can override the generic message with a specific message,
			    ## somewhat more user-friendly than what is directly reported back by JIRA.
			    $self->{error} = "\"$ticket{fields}{reporter}{name}\" is not a registered user in JIRA.";
			}
		    }
		    else {
			$self->{error} .= ' ' . join( ' ', @{ $perl_response->{errorMessages} } );
		    }
		}
	    }
	    die $self->{error} . "\n";
	}

	eval { $perl_response = decode_json( $$json_response ); };
	if ($@) {
	    chomp $@;
	    $logObj->log( "ERROR:  Failed to create JIRA issue:  could not parse the returned content as JSON ($@)." );
	    $self->{error} = 'JIRA issue creation failure (bad JSON).';
	    die $self->{error} . "\n";
	}
	elsif ($self->{debug}) {
	    $logObj->log( "DEBUG:  JIRA issue creation response:" );
	    $logObj->log( Dumper($perl_response) );
	}

	$issue_key = $perl_response->{key};
	if ( defined $issue_key ) {
	    $logObj->log("JIRA issue key:  $issue_key");
	    ## Fill in the results data.
	    $resultsHash{'TicketNo'}     = $issue_key;
	    $resultsHash{'TicketStatus'} = "Open";              # technically we should check JIRA to see the Status value
	    $resultsHash{'ClientData'}   = $perl_response->{self};
	    $resultsHash{'FilingError'}  = "";
	}
	else {
	    $logObj->log("ERROR:  Failed to create a JIRA issue.");
	    $logObj->log("Error details follow:");
	    $logObj->log( Dumper( $perl_response ) );
	    $self->{error} = 'JIRA issue creation failure (no issue key returned).';
	    die $self->{error} . "\n";
	}
    };

    # We save the exception message to make sure it is still unchanged when we die().
    if ($@) {
	my $exception = $@;
	chomp $exception;
	$logObj->log("ERROR:  $exception");
	die "$exception\n";
    }

    if ( @acknowledgements and defined $issue_key ) {
	if ( $moduleParams->{'send_to_nagios'} ne 'yes' ) {
	    $logObj->log('NOTICE:  JIRA::HelpDesk is configured to not send acknowledgements to Nagios.');
	}
	else {
	    ## Now that we know what the issue key is, insert it into the acknowledgement
	    ## messages for easy tracking purposes.  That's one reason why we waited until now to
	    ## send the acknowledgements (the other reason being that we don't want to turn off
	    ## Nagios alerts if in fact we didn't manage to create a JIRA issue).
	    s/{ISSUE_KEY}/$issue_key/g for @acknowledgements;

	    # We want to reliably send the acknowledgements to Nagios.  In the current release, we
	    # just write to the Nagios command pipe (though very carefully, taking into account
	    # the usually-unrecognized trickiness of writing to a pipe).  In a future release, for
	    # simplicity, we will allow writing via the Bronx socket, as an option.  Regardless of
	    # the chosen transport, at the moment we attempt this writing, Nagios might be down
	    # (say, because it's in the middle of a Commit operation), so we might be tempted to
	    # queue the acknowledgements using the GDMA spooler instead.  But currently, the GDMA
	    # spooler can only be used to send host and service check results, not commands.

	    # The later implementation will allow writes to the Bronx socket mostly so we can
	    # potentially move this integration onto a child server if that turns out to be useful
	    # at some customer site.  But when we add this capability, we will make the selection
	    # of pipe vs. socket a configuration option, thus retaining flexibility as to how the
	    # data transfer will occur.  Writing directly to the Nagios command pipe avoids the
	    # overhead of forking a send_nsca process and the complex NSCA protocol needed for the
	    # Bronx socket.

	    if ( $moduleParams->{'use_nsca'} eq 'yes' ) {
		# FIX LATER:  Allow optionally writing all the acknowledgements efficiently to the Bronx
		# socket.  I suppose that means we should run send_nsca to handle the connection protocol.
		# But all the heavy lifting should be done inside a separate GW::Bronx module.
		# FIX LATER:  Make sure the messages we constructed above are the right format for writing
		# to the Bronx socket.
	    }
	    else {
		my $nagios = GW::Nagios->new (
		    $moduleParams->{'nagios_command_pipe'},
		    $moduleParams->{'max_command_pipe_write_size'},
		    $moduleParams->{'max_command_pipe_wait_time'}
		);
		if (not defined $nagios) {
		    ## We have already created the issue, so there is no sense in dying
		    ## once we have logged this error.  The best we can do is to carry on.
		    ## FIX MAJOR:  There should still be some way to reflect this failure
		    ## back to the operator, perhaps by generating a new log message.
		    my $count = scalar @acknowledgements;
		    $logObj->log('ERROR:  Creating a GW::Nagios object has failed; thus');
		    $logObj->log("        $count acknowledgements will not be sent to Nagios.");
		}
		else {
		    my $errors = $nagios->send_messages_to_nagios(\@acknowledgements);
		    $logObj->log($_) for @$errors;
		    ## FIX MAJOR:  There should be some way to reflect any failure
		    ## back to the operator, perhaps by generating a new log message.
		}
	    }
	}
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

    $self->{error} = '';

    #--------------------------------------------------------------
    # Cycle through each Ticketed Event and check to see if its
    # ticket status has changed in the HelpDesk system.
    #
    # If it has, change the status of the ticketed event and
    # set the hasChanged property to 1 for that ticket.
    #--------------------------------------------------------------

    $helpDeskObj = $self;    # Global references to $self
    my $logObj = $self->getLogObj();    # Shorthand reference for log

    my $JIRA_REST_Anchor_Point  = $moduleParams->{'JIRA_REST_Anchor_Point'};
    my $JIRA_REST_User          = $moduleParams->{'JIRA_REST_User'};
    my $JIRA_REST_Pass          = $moduleParams->{'JIRA_REST_Pass'};

    # Normalize the REST anchor-point parameter, so we can depend on its form later on.
    chop $JIRA_REST_Anchor_Point if $JIRA_REST_Anchor_Point =~ m{/$};
    eval {
	## Note that if multiple log message IDs were selected when a given JIRA issue was initially created,
	## we will have multiple helpdesklookuptable rows all with the same ticketno field (but with different
	## logmessageid fields).  And so we will then probe JIRA multiple times for the same JIRA issue, in
	## the following loop.  No matter, though, this will get all of those rows cleaned up as intended.
	foreach my $event (@$ticketedEvents) {
	    # Let's be optimistic and assume success until we find out otherwise.
	    my $successful = 1;

	    my $ua = LWP::UserAgent->new;
	    $ua->agent('JIRA REST Client/1.0');

	    my $req = HTTP::Request->new( GET => "$JIRA_REST_Anchor_Point/api/2/issue/$event->{TicketNo}?fields=status&expand=status" );
	    $req->content_type('application/json');
	    $req->authorization_basic($JIRA_REST_User, $JIRA_REST_Pass);

	    my $resp = undef;
	    if (not do_timed_request($self, 'Query JIRA issue status', $ua, $req, \$resp)) {
		$logObj->log( "ERROR:  $self->{error}" );
		$successful = 0;
	    }

	    my $json_response;
	    if ($successful) {
		## check the success of the request, at the HTTP::Request level
		my $http_status = $resp->code;
		$json_response = $resp->decoded_content( ref => 1 ); 
		if ( $resp->is_success && $http_status == 200 ) { 
		    $logObj->log( "DEBUG:  successful JIRA issue query for $event->{TicketNo}" ) if $self->{debug};
		}
		else {
		    $logObj->log( "ERROR:  unsuccessful JIRA issue query for $event->{TicketNo}; HTTP status code = $http_status" );
		    chomp $$json_response;
		    $logObj->log( "ERROR:  content is:\n$$json_response" );
		    $self->{error} = 'JIRA issue query failure (bad HTTP status).';
		    $successful = 0;
		}
	    }

	    # What we get back:
	    #	[2013-05-15 17:57:18] [pid:21720] $VAR1 = {
	    #	  'expand' => 'renderedFields,names,schema,transitions,operations,editmeta,changelog',
	    #	  'fields' => {
	    #	    'status' => {
	    #	      'description' => 'The issue is open and ready for the assignee to start work on it.',
	    #	      'iconUrl' => 'http://172.28.111.222:8080/images/icons/status_open.gif',
	    #	      'id' => '1',
	    #	      'name' => 'Open',
	    #	      'self' => 'http://172.28.111.222:8080/rest/api/2/status/1'
	    #	    }
	    #	  },
	    #	  'id' => '10019',
	    #	  'key' => 'DRA-20',
	    #	  'self' => 'http://172.28.111.222:8080/rest/api/2/issue/10019'
	    #	};

	    my $perl_response;
	    if ($successful) {
		eval { $perl_response = decode_json( $$json_response ); };
		if ($@) {
		    chomp $@;
		    $logObj->log( "ERROR:  Failed to query JIRA issue $event->{TicketNo}:  could not parse the returned content as JSON ($@)." );
		    $self->{error} = 'JIRA issue creation failure (bad JSON).';
		    $successful = 0;
		}
		elsif ($self->{debug}) {
		    $logObj->log( "DEBUG:  JIRA issue query response for $event->{TicketNo}:" );
		    $logObj->log( Dumper($perl_response) );
		}
	    }

	    if ($successful) {
		my $ticketStatus = $perl_response->{fields}{status}{name};
		if ( defined $ticketStatus ) {
		    $logObj->log("JIRA issue status for $event->{TicketNo}:  $ticketStatus");
		    if ( $ticketStatus ne $event->{'TicketStatus'} ) {
			$logObj->log("JIRA issue status for $event->{TicketNo} has changed (had been $event->{'TicketStatus'}).");
			$event->{'TicketStatus'} = $ticketStatus;
			$event->{'hasChanged'}   = 1;
		    }
		}
		else {
		    $logObj->log("ERROR:  Failed to find status for JIRA issue $event->{TicketNo}.");
		    $logObj->log("Issue details follow:");
		    $logObj->log( Dumper( $perl_response ) );
		}
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

sub catch_abort_signal {
    my $signame = shift;
    # FIX MAJOR:  log this through the appropriate, available channel
    # log_timed_message "NOTICE:  Caught SIG$signame signal!";
    die "timed out\n";
}

# Internal routine.
sub do_timed_request {
    my $self = $_[0];    # implicit argument
    my $act  = $_[1];    # required argument
    my $ua   = $_[2];    # required argument
    my $req  = $_[3];    # required argument
    my $resp = $_[4];    # required argument

    my $successful = 1;

    # Usually in a routine like this, we would wrap the code to which a timeout should apply in an
    # alarm($timeout) .. alarm(0) sequence (with lots of extra protection against race conditions).
    # However, in the present case, the code we want to wrap already internally absconds with
    # control over SIGALRM.  So we need to impose an independent timer at this level.  For that
    # purpose, we have chosen to use the SIGABRT signal.
    local $SIG{ABRT} = \&catch_abort_signal;

    # If our timer expires, it may kill the wrapped code before it has a chance to cancel a
    # future alarm.  Hopefully it will have a local SIGALRM handler, so that setting should
    # be unwound automatically when we die out of our timer's signal handler and abort our
    # eval{};, but if we get such an uncanceled alarm and we either don't have our own signal
    # handler in place or we haven't ignored the signal at this level, we will exit.  It seems
    # safest to just use the same signal handler we're using for the SIGABRT signal.
    local $SIG{ALRM} = \&catch_abort_signal;

    ## The nested eval{}; blocks protect against race conditions, as described in the comments.
    eval {
	## Create our abort timer in a disabled state.
	my $timer = POSIX::RT::Timer->new( signal => SIGABRT );
	eval {
	    ## Start our abort timer.
	    $timer->set_timeout( $self->{timeout} );

	    # We might die here either explicitly or because of a timeout and the signal
	    # handler action.  If we get the abort signal and die because of it, we need
	    # not worry about resetting the abort before exiting the eval, because it has
	    # already expired (we use a one-shot timer).
	    eval {
		## The user-agent request() logic internally calls alarm() somewhere, perhaps
		## within some sleep() or equivalent indirect call.  That's why we switched
		## to using an independent timer and an independent signal (and signal
		## handler).  We haven't actually identified the line of code that does so,
		## but we have shown by experiment that this is the case, and it would kill
		## our own carefully-set SIGALRM timeout so it becomes inoperative.
		## FIX LATER:  Track down where the alarm stuff happens, and submit a bug
		## report that this should be described in the package documentation.
		$$resp = $ua->request($req);    # Send request, get response
	    };
	    ## We got here because one of the following happened:
	    ##
	    ## * the wrapped code die()d on its own (not that we have knowledge of any
	    ##   circumstances in which that might predictably happen), in which case we
	    ##   probably have our timer interrupt still armed, and possibly we might
	    ##   also have an alarm interrupt from the wrapped code still armed
	    ## * the wrapped code exited normally (either it ran to completion or it ran up
	    ##   against its own internal timeout), in which case we probably have our timer
	    ##   interrupt still armed
	    ## * our timer expired, in which case we might have an alarm interrupt from the
	    ##   wrapped code still armed
	    ##
	    ## If interrupts from both signals are still armed, there is no way to know the
	    ## relative sequence in which they will fire.  Consequently, we have two signals
	    ## we need to manage here, and we need to resolve all possible orders of signal
	    ## generation and the associated race conditions.  That accounts for the triple
	    ## nesting of eval{}; blocks here and the repeated signal cancellations.

	    ## Save the death rattle in case our subsequent processing inadvertenty changes it
	    ## before we get to use it.
	    my $exception = $@;

	    # In case the wrapped code's alarm was still armed when either it died on its
	    # own or we aborted the code via our timer, disarm the alarm here.
	    alarm(0);

	    # Stop our abort timer.
	    $timer->set_timeout(0);

	    # Percolate failure to the next level of nesting.
	    if ($exception) {
		chomp $exception;
		die "$exception\n";
	    }
	};
	## Save the death rattle in case our subsequent processing inadvertenty changes it
	## before we get to use it.
	my $exception = $@;

	# In case the wrapped code died while its alarm was still armed, and our timer
	# expired before we could disarm the alarm just above, disarm it here.
	alarm(0);

	# In case the wrapped code died while its alarm was still armed, and then the
	# alarm fired just above before we could disarm it (and subsequently disarm our
	# own timer), disarm our timer here.
	$timer->set_timeout(0);

	# Percolate failure to the next level of nesting.
	if ($exception) {
	    chomp $exception;
	    die "$exception\n";
	}
    };
    ## Check for either any residual cases where we failed to disable an interrupt before
    ## it got triggered, or the percolation of whatever interrupt or other failure might
    ## have occurred within the nested eval{}; blocks.
    if ($@) {
	chomp $@;
	$self->{error} = "$act failure ($@).";
	$successful = 0;
    }

    return $successful;
}

1;

__END__


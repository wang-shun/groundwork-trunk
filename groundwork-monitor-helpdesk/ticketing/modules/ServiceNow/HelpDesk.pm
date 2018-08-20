package ServiceNow::HelpDesk;

# Manage ServiceNow Incident tickets in a GroundWork Monitor deployment.
# Copyright (c) 2015 GroundWork Open Source (www.groundworkopensource.com).
# All rights reserved.  Use is subject to GroundWork commercial license terms.

#-----------------------------------------------
# Perl setup.
#-----------------------------------------------

use vars qw($VERSION);
$VERSION = '2.0';

use strict;
use warnings;

use Data::Dumper;
$Data::Dumper::Indent   = 1;
$Data::Dumper::Sortkeys = 1;

# IO::Socket:SSL is not directly referenced in this package, but we want to 
# force use of it instead of Net::SSL when the ServiceNow connection is made.
# Loading it beforehand helps do this.  This will allow the Common Name in the
# site certificate to be compared to the hostname we're connecting to, to help 
# prevent a man-in-the-middle attack. 
use IO::Socket::SSL;

# Useful only in development, for checking Perl compilability of this package,
# so the ServiceNow packages can be reached in a standalone-package context.
# use lib '..';

use ServiceNow;
use ServiceNow::Configuration;
use ServiceNow::ITIL::Incident;

use GW::DBObject;
use GW::Nagios;

# IMPORTANT NOTE ON TERMINOLOGY:  The overall intent here is to create and manage "trouble
# tickets" in a general sense.  However, we are using the ServiceNow::ITIL::Incident object
# for this purpose, not the ServiceNow::ITIL::Ticket object.  As of this writing, the
# ServiceNow Wiki documentation is really awful about describing the intended purposes of
# these objects, so we have chosen what seems to make most sense as far as we can determine.
# Even ServiceNow's own developer doesn't seem to know what a ServiceNow::ITIL::Ticket is for,
# so it might be a vestige of old abandoned development.

#-----------------------------------------------
# Globals and Constants
#-----------------------------------------------

my $helpDeskObj = undef;

my $UnknownHostName           = 'Unknown Host';
my $UnknownServiceDescription = 'Unknown Service';
my $UnknownServiceGroup       = 'Unknown ServiceGroup';
my $UnknownApplicationType    = 'Unknown Application';
my $UnknownSeverity           = 'Unknown Severity';
my $UnknownMonitorStatus      = 'Unknown Status';
my $UnknownLocation           = 'Unknown Location';

#-----------------------------------------------

sub new {
    my $packageName = shift;

    my $self = {
	_dBObj  => undef,  # dBObject for HelpDeskBridgeDB
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
	    ## Clean up whitespace around all individual fields.
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
#   - $summary is an operator-specified commentary for this ticket creation.
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

    $helpDeskObj    = $self;                 # Global references to $self
    my $logObj      = $self->getLogObj();    # Shorthand reference for log
    my %resultsHash = ();

    my %dBObjects = ();

    $dBObjects{'monarchDB'} = new GW::DBObject();
    $dBObjects{'collageDB'} = new GW::DBObject();

    my $MonarchDBConfig = new Config::General( $params->{'MonarchDBCredentials'} );
    my %MonarchDBParams = $MonarchDBConfig->getall();

    my $CollageDBConfig = new Config::General( $params->{'GWCollageDBCredentials'} );
    my %CollageDBParams = $CollageDBConfig->getall();

    $dBObjects{'monarchDB'}->setDBHost( $MonarchDBParams{'monarch.dbhost'} );
    $dBObjects{'monarchDB'}->setDBName( $MonarchDBParams{'monarch.database'} );
    $dBObjects{'monarchDB'}->setDBUser( $MonarchDBParams{'monarch.username'} );
    $dBObjects{'monarchDB'}->setDBPass( $MonarchDBParams{'monarch.password'} );

    $dBObjects{'collageDB'}->setDBHost( $CollageDBParams{'collage.dbhost'} );
    $dBObjects{'collageDB'}->setDBName( $CollageDBParams{'collage.database'} );
    $dBObjects{'collageDB'}->setDBUser( $CollageDBParams{'collage.username'} );
    $dBObjects{'collageDB'}->setDBPass( $CollageDBParams{'collage.password'} );

    my $monarch_dBObj = $dBObjects{'monarchDB'};
    my $collage_dBObj = $dBObjects{'collageDB'};

    # Create cached dB connections
    $dBObjects{'monarchDB'}->connect();
    $dBObjects{'collageDB'}->connect();

    # Connect to the cached dB connections
    $monarch_dBObj->connect();
    $collage_dBObj->connect();
    my $monarch_dbh = $monarch_dBObj->getHandle();
    my $collage_dbh = $collage_dBObj->getHandle();
    my $sth         = undef;
    my $query       = undef;

    my $ServiceNowSOAPEndpoint     = $moduleParams->{'ServiceNowSOAPEndpoint'};
    my $ServiceNowSOAPUser         = $moduleParams->{'ServiceNowSOAPUser'};
    my $ServiceNowSOAPPass         = $moduleParams->{'ServiceNowSOAPPass'};
    my $ServiceNow_SSL_CA_FILE     = $moduleParams->{'ServiceNow_SSL_CA_FILE'};
    my $ServiceNow_SSL_CA_PATH     = $moduleParams->{'ServiceNow_SSL_CA_PATH'};
    my $NewIncidentCaller          = $moduleParams->{'NewIncidentCaller'};
    my $NewIncidentCompany         = $moduleParams->{'NewIncidentCompany'};
    my $NewIncidentSeverity        = $moduleParams->{'NewIncidentSeverity'};
    my $NewIncidentImpact          = $moduleParams->{'NewIncidentImpact'};
    my $NewIncidentUrgency         = $moduleParams->{'NewIncidentUrgency'};
    my $NewIncidentContactType     = $moduleParams->{'NewIncidentContactType'};
    my $NewIncidentState           = $moduleParams->{'NewIncidentState'};
    my $UnknownCategory            = $moduleParams->{'UnknownCategory'};
    my $UnknownSubcategory         = $moduleParams->{'UnknownSubcategory'};
    my $UnknownAssignmentGroup     = $moduleParams->{'UnknownAssignmentGroup'};
    my $DefaultGroundWorkServer    = $moduleParams->{'DefaultGroundWorkServer'};
    my $AssignmentGroupMappingType = $moduleParams->{'AssignmentGroupMappingType'};

    my $Calculate_urgency     = $moduleParams->{'Calculate_urgency'};
    my $monitorstatus_urgency = $moduleParams->{'monitorstatus_urgency'};
    if ( $params->{'Debug'} ) {
	$logObj->log("DEBUG:  Calculate_urgency = $Calculate_urgency");
	foreach my $key ( sort keys %$monitorstatus_urgency ) {
	    $logObj->log("DEBUG:  monitorstatus_urgency{$key} = $monitorstatus_urgency->{$key}");
	}
    }

    my $Send_category     = $moduleParams->{'Send_category'};
    my $Send_cmdb_ci      = $moduleParams->{'Send_cmdb_ci'};
    my $Send_company      = $moduleParams->{'Send_company'};
    my $Send_contact_type = $moduleParams->{'Send_contact_type'};
    my $Send_impact       = $moduleParams->{'Send_impact'};
    my $Send_opened_by    = $moduleParams->{'Send_opened_by'};
    my $Send_severity     = $moduleParams->{'Send_severity'};
    my $Send_subcategory  = $moduleParams->{'Send_subcategory'};
    my $Send_urgency      = $moduleParams->{'Send_urgency'};

    # IMPORTANT:  The ServiceNowSOAPEndpoint value must end with a "/" character for it
    # to be handled correctly within the ServiceNow package, so we guarantee that here
    # rather than depending on the configuration file value to be so specified.
    $ServiceNowSOAPEndpoint .= '/' if $ServiceNowSOAPEndpoint !~ m{/$};

    my $use_hostgroup_service_map = $AssignmentGroupMappingType eq 'HostgroupService';
    my $use_servicegroup_map      = $AssignmentGroupMappingType eq 'ServiceGroup';

    # If we don't have one of the supported values of AssignmentGroupMappingType in hand,
    # we'll end up using the configured UnknownAssignmentGroup for every created incident.
    # So while this condition is not fatal, we ought to warn about it.
    $logObj->log("WARNING:  Unsupported value for AssignmentGroupMappingType:  $AssignmentGroupMappingType")
      if !$use_hostgroup_service_map && !$use_servicegroup_map;

    my $monarch_group_location_map             = readMappingFile( $self, $moduleParams->{'MonarchGroupLocationMapFile'},            1, 1 );
    my $hostgroup_category_subcategory_map     = readMappingFile( $self, $moduleParams->{'HostgroupCategorySubcategoryMapFile'},    1, 2 );
    my $hostgroup_service_assignment_group_map = readMappingFile( $self, $moduleParams->{'HostgroupServiceAssignmentGroupMapFile'}, 2, 1 ) if $use_hostgroup_service_map;
    my $servicegroup_assignment_group_map      = readMappingFile( $self, $moduleParams->{'ServiceGroupAssignmentGroupMapFile'},     1, 1 ) if $use_servicegroup_map;

    my @LogMessageIDs       = ();
    my %HostNames           = ();
    my %ServiceDescriptions = ();
    my %ApplicationTypes    = ();
    my %Severities          = ();
    my %MonitorStatuses     = ();
    my %locations           = ();
    my %categories          = ();
    my %subcategories       = ();
    my %assignment_groups   = ();
    my @AdditionalComments  = ();
    my $comment_separator   = "-----------------------------------------------------------------------------------------------\n";
    my @acknowledgements    = ();

    # We either use a fixed urgency value from the config file, or we calculate starting with
    # an insignificant urgency and take the worst-case value we find in the selected events.
    my $CalculatedUrgency = $Calculate_urgency ? 10 : $NewIncidentUrgency;

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

	push @LogMessageIDs, $LogMessageID;
	$HostNames{           defined($HostName)           ? $HostName           : $UnknownHostName }           = 1;
	$ServiceDescriptions{ defined($ServiceDescription) ? $ServiceDescription : $UnknownServiceDescription } = 1;
	$ApplicationTypes{    defined($ApplicationType)    ? $ApplicationType    : $UnknownApplicationType }    = 1;
	$Severities{          defined($Severity)           ? $Severity           : $UnknownSeverity }           = 1;
	$MonitorStatuses{     defined($MonitorStatus)      ? $MonitorStatus      : $UnknownMonitorStatus }      = 1;

	if ($Calculate_urgency) {
	    $logObj->log("DEBUG:  event MonitorStatus = $MonitorStatus") if $params->{'Debug'};
	    ( my $monitor_status_key = $MonitorStatus ) =~ s/ /_/g;
	    $monitor_status_key =~ tr/A-Za-z0-9_//cd;
	    my $event_urgency = $monitorstatus_urgency->{$monitor_status_key} || 1;
	    $CalculatedUrgency = $event_urgency if $event_urgency < $CalculatedUrgency;
	}

	$HostNotes    =~ s/<br>/\n/g if defined $HostNotes;
	$ServiceNotes =~ s/<br>/\n/g if defined $ServiceNotes;

	my @HostGroups    = ();
	my @MonarchGroups = ();

	# FIX MINOR:  Since Monarch really represents a possible future configuration and not necessarily
	# the running configuration, these mappings probably should have been done by equivalent lookups
	# within the GWCollageDB database, not the monarch database.
	#
	# Find all hostgroups that this host belongs to.
	$query = qq{
	    select distinct hg.name from hostgroups hg, hostgroup_host hgh, hosts h
	    where h.name = ? and hgh.host_id = h.host_id and hg.hostgroup_id = hgh.hostgroup_id
	};
	## FIX MINOR:  check for errors in prepare() and execute()
	$sth = $monarch_dbh->prepare($query);
	$sth->execute($HostName);
	while ( my @values = $sth->fetchrow_array() ) {
	    push @HostGroups, $values[0];
	}
	$sth->finish();

	# FIX MINOR:  Since Monarch really represents a possible future configuration and not necessarily
	# the running configuration, these mappings probably should have been done by equivalent lookups
	# within the GWCollageDB database, not the monarch database.  (But we don't store Monarch Group
	# associations within Foundation, so that's not practical at this time.)
	#
	# Find all Monarch Configuration Groups that this host belongs
	# to, either directly (by assignment to the group) or indirectly
	# (by assignment to a hostgroup that is assigned to the group).
	$query = qq{
	    select distinct mg.name from monarch_groups mg, monarch_group_host mgh, hosts h
	    where h.name = ? and mgh.host_id = h.host_id and mg.group_id = mgh.group_id
	    UNION DISTINCT
	    select distinct mg.name from monarch_groups mg, monarch_group_hostgroup mghg, hostgroup_host hgh, hosts h
	    where h.name = ? and hgh.host_id = h.host_id and mghg.hostgroup_id = hgh.hostgroup_id and mg.group_id = mghg.group_id
	};
	## FIX MINOR:  check for errors in prepare() and execute()
	$sth = $monarch_dbh->prepare($query);
	$sth->execute( $HostName, $HostName );
	my $got_server = 0;
	while ( my @values = $sth->fetchrow_array() ) {
	    push @MonarchGroups, $values[0];
	    $got_server = 1;
	}
	$sth->finish();
	push @MonarchGroups, $DefaultGroundWorkServer if not $got_server and $DefaultGroundWorkServer ne '';

	# It's possible that $ServiceDescription is undefined here.
	my $service = defined($ServiceDescription) ? $ServiceDescription : $UnknownServiceDescription;
	foreach my $HostGroup (@HostGroups) {
	    my $category_array_ref = $hostgroup_category_subcategory_map->{$HostGroup};
	    $categories{    $category_array_ref->[0] } = 1 if defined $category_array_ref->[0];
	    $subcategories{ $category_array_ref->[1] } = 1 if defined $category_array_ref->[1];

	    if ($use_hostgroup_service_map) {
		my $assignment_group_array_ref = $hostgroup_service_assignment_group_map->{$HostGroup}{$service};
		$assignment_groups{ $assignment_group_array_ref->[0] } = 1
		  if defined($assignment_group_array_ref) && defined( $assignment_group_array_ref->[0] );
	    }
	}
	if ( $use_servicegroup_map && defined $ServiceDescription ) {
	    ## Find the name of "the" servicegroup for $HostName and $ServiceDescription, if any, within Foundation.
	    ## There might be more than one, though, so we must take that into account.
	    my $query = "
		select	distinct c.Name
		from	Host h, ServiceStatus ss, CategoryEntity ce, Category c, EntityType et
		where	et.Name = 'SERVICE_GROUP'
		and	c.EntityTypeID = et.EntityTypeID
		and	ce.CategoryID = c.CategoryID
		and	ss.ServiceStatusID = ce.ObjectID
		and	ss.ServiceDescription = ?
		and	h.HostID = ss.HostID
		and	h.HostName = ?
	    ";
	    ## FIX MINOR:  check for errors in prepare() and execute()
	    my $sth = $collage_dbh->prepare($query);
	    $sth->execute( $ServiceDescription, $HostName );
	    my @servicegroups = ();
	    my @values        = ();
	    while ( @values = $sth->fetchrow_array() ) {
		push @servicegroups, $values[0];
	    }
	    $sth->finish;

	    my $ServiceGroup = @servicegroups == 1 ? $servicegroups[0] : @servicegroups ? $UnknownServiceGroup : undef;
	    my $assignment_group_array_ref =
	      defined($ServiceGroup)
	      ? ( $ServiceGroup eq $UnknownServiceGroup ? [$UnknownAssignmentGroup] : $servicegroup_assignment_group_map->{$ServiceGroup} )
	      : undef;
	    $assignment_groups{ $assignment_group_array_ref->[0] } = 1
	      if defined($assignment_group_array_ref) && defined( $assignment_group_array_ref->[0] );
	}

	foreach my $MonarchGroup (@MonarchGroups) {
	    my $location_array_ref = $monarch_group_location_map->{$MonarchGroup};
	    $locations{ $location_array_ref->[0] } = 1 if defined $location_array_ref->[0];
	}

	# If we could use HTML in the comment we submit to ServiceNow, we would create a small table for each block,
	# so the labels and values would line up cleanly.  But experimentation shows we can only submit plain text,
	# with each line ending in a newline character.  This also means that we cannot embed active hyperlinks in
	# the submitted comments.
	#
	# Secondly, experimentation shows that certain ISO-8859-1 characters, such as centered dot, confuse the heck
	# out of ServiceNow if they are included in a comment, and cause the entire comment to be completely garbled.
	# Heaven forbid that we should want to go even further and include some kind of Unicode.  So we need to keep
	# the comment content very simple.  Since Monarch has been extended recently to properly support ISO-8859-1
	# characters in service names, there is no protection against such characters showing up in these comments if
	# the customer chooses to use such characters in their service names.  ServiceNow will need to improve their
	# API or internal software appropriately, if support for non-ASCII characters is required by the customer.
	my $comment = '';
	if ( defined $HostName ) {
	    if ( defined $ServiceDescription ) {
		## We don't have the equivalent of a Nagios $NOTIFICATIONTYPE$
		## field to display here, so we display $Severity instead.  It's
		## not the same set of values, but it is at least vaguely related.
		$comment .= "GroundWork Service $Severity Event (Log Message $LogMessageID)\n";
		$comment .= "Host:  $HostName ($DeviceIdentification)\n";
		$comment .= "Host State:  $HostMonitorStatus\n";
		$comment .= "Service:  $ServiceDescription\n";
		$comment .= "Service State:  $MonitorStatus\n";
		$comment .= "Service Info:  $TextMessage\n";
		$comment .= "Time:  $ReportDate\n";
		$comment .= "Service Notes:  $ServiceNotes\n" if defined $ServiceNotes;
	    }
	    else {
		## We don't have the equivalent of a Nagios $NOTIFICATIONTYPE$
		## field to display here, so we display $Severity instead.  It's
		## not the same set of values, but it is at least vaguely related.
		$comment .= "GroundWork Host $Severity Event (Log Message $LogMessageID)\n";
		$comment .= "Host:  $HostName ($DeviceIdentification)\n";
		$comment .= "Host State:  $MonitorStatus\n";
		$comment .= "Host Info:  $TextMessage\n";
		$comment .= "Time:  $ReportDate\n";
		$comment .= "Host Notes:  $HostNotes\n" if defined $HostNotes;
	    }
	}
	else {
	    ## FIX LATER:  Should we revise what we present as the comment here (i.e., for non-NAGIOS events)?
	    ## For a SYSTEM event:  ...
	    ## For an SNMPTRAP event:  We might want to use certain dynamic fields from LogMessageProperty,
	    ##     such as (for $ApplicationType eq 'SNMPTRAP') category, event_Name, event_OID_symbolic
	    ##     (if it differs from event_Name), ipaddress, and variable_Bindings.
	    ## For a SYSLOG event:  TextMessage, MonitorStatus, HostName, MsgCount, ApplicationType,
	    ##     ipaddress, subComponent, with the last two being dynamic properties
	    $comment .= "GroundWork $ApplicationType $Severity Event (Log Message $LogMessageID)\n";
	    $comment .= "Address:  $DeviceIdentification\n";
	    $comment .= "State:  $MonitorStatus\n";
	    $comment .= "Info:  $TextMessage\n";
	    $comment .= "Time:  $ReportDate\n";
	}
	push @AdditionalComments, $comment;

	if ( $ApplicationType eq 'NAGIOS' ) {
	    ## Create and queue a persistent and notifying acknowledgement to Nagios, based on the $HostName
	    ## and $ServiceDescription.  "Persistent and notifying" means that certain command-variant flags
	    ## must be set in the acknowledgement.
	    ##
	    ## Note that these acknowledgement messages will be ignored by Nagios if the host or service
	    ## is already back to an OK state when the message is received by Nagios.  So in that case,
	    ## the message won't be seen in Nagios CGI screens and won't be forwarded to Foundation.
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
		  "[$now] ACKNOWLEDGE_SVC_PROBLEM;$HostName;$ServiceDescription;1;1;1;$operator;Service problem tracked in ServiceNow incident {INCIDENT_NUMBER}.\n";
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
		  "[$now] ACKNOWLEDGE_HOST_PROBLEM;$HostName;1;1;1;$operator;Host problem tracked in ServiceNow incident {INCIDENT_NUMBER}\n";
	    }
	}
    }

    # Create and submit a ServiceNow incident (not a ServiceNow ticket).
    my $LogMessageIDs = join( ',', sort { $a <=> $b } @LogMessageIDs );

    # Provide a default Summary line to fill in the Work Notes for a ServiceNow incident,
    # if the user has not typed in anything interesting.
    $summary = 'GWMEE Log ID' . ( @LogMessageIDs == 1 ? '' : 's' ) . ": $LogMessageIDs" if $summary =~ /^\s*$/;

    # If there are multiple locations processed above but they are all the same location,
    # use that.  Otherwise, we have either a missing or ambiguous location, and in either
    # case we tag it as Unknown.  Same with category, subcategory, and assignment group.
    my $Caller             = $NewIncidentCaller || $operator;
    my $Location           = keys %locations         == 1 ? each %locations         : $UnknownLocation;
    my $Category           = keys %categories        == 1 ? each %categories        : $UnknownCategory;
    my $Subcategory        = keys %subcategories     == 1 ? each %subcategories     : $UnknownSubcategory;
    my $AssignmentGroup    = keys %assignment_groups == 1 ? each %assignment_groups : $UnknownAssignmentGroup;
    my $AdditionalComments = join( $comment_separator, @AdditionalComments );
    my $ShortDescription   = '';
    my $ConfigurationItem  = '';

    # Incident data will be sent to ServiceNow within XML packaging, and apparently the
    # existing ServiceNow package already handles escaping of XML-critical characters internal
    # to its own processing.  So we need not do so here.  However, the XMLification transforms
    # are not being reversed on the other side, so for instance any single-quote characters
    # we send in the comment field are showing up as &apos; when displayed in the Service-Now
    # incident GUI.  Yuck.  I've sent an email to Service-Now asking if they have a public bug
    # tracker we can use to submit issues like this back to them.  The transforms that the
    # XMLification presumably carries out that we would want reversed are:
    # $AdditionalComments =~ s/&/&amp;/g;
    # $AdditionalComments =~ s/"/&quot;/g;
    # $AdditionalComments =~ s/'/&apos;/g;
    # $AdditionalComments =~ s/</&lt;/g;
    # $AdditionalComments =~ s/>/&gt;/g;

    # define $ShortDescription based on a lot of different possible conditions
    my $HostName           = keys %HostNames           == 1 ? each %HostNames           : $UnknownHostName;
    my $ServiceDescription = keys %ServiceDescriptions == 1 ? each %ServiceDescriptions : $UnknownServiceDescription;
    my $ApplicationType    = keys %ApplicationTypes    == 1 ? each %ApplicationTypes    : $UnknownApplicationType;
    my $Severity           = keys %Severities          == 1 ? each %Severities          : $UnknownSeverity;
    my $MonitorStatus      = keys %MonitorStatuses     == 1 ? each %MonitorStatuses     : $UnknownMonitorStatus;
    if ( @$selectedEvents == 1 ) {
	if ( defined $HostName ) {
	    if ( defined $ServiceDescription ) {
		$ShortDescription = "Service $MonitorStatus alert for $HostName $ServiceDescription";
	    }
	    else {
		$ShortDescription = "Host $MonitorStatus alert for $HostName";
	    }
	}
	else {
	    $ShortDescription = "$ApplicationType $Severity alert";
	}
    }
    else {
	if ( $HostName ne $UnknownHostName ) {
	    if ( $ServiceDescription ne $UnknownServiceDescription ) {
		$ShortDescription = "Service $MonitorStatus alert for $HostName $ServiceDescription";
	    }
	    else {
		$ShortDescription = "Host $MonitorStatus alert for $HostName";
	    }
	}
	else {
	    $ShortDescription = "$ApplicationType $Severity alert";
	}
    }
    ## We used to include "(Log Message $LogMessageIDs)" at the end of each possible $ShortDescription.
    ## But that turns out to not be terribly useful, as the Log Message IDs are not presented anywhere
    ## in our Event Console for reference.  We are including the Log Message IDs in the content of the
    ## journaled comment field, with the ID for each event in the incident recorded along with the other
    ## details, so this information is still available.  So we have taken this information out of the
    ## $ShortDescription for now.  (I can see one possible reason to have these tags in the message,
    ## as a quick and easy way of distinguishing near-identical incidents just from these tags, if you
    ## have a bunch of incidents displayed in a list with just this message displayed.  If that becomes
    ## an issue in practice, we can put back the Log Message IDs easily enough.)
    $ShortDescription .= " (Log Message $LogMessageIDs)" if 0;

    # If we don't have an unambiguous determination of the service name (due either to differences in
    # multiple events or to all events lacking any service name), then we currently suppress the service
    # name from the configuration item string rather than passing the $UnknownServiceDescription value.
    # This doesn't prevent us passing the $UnknownHostName value if we have conflicting host names for
    # the selected events.
    $ConfigurationItem = $ServiceDescription eq $UnknownServiceDescription ? $HostName : "$HostName:$ServiceDescription";

    $logObj->log( "About to create a ServiceNow incident for Log Message " . ( @LogMessageIDs == 1 ? 'ID' : 'IDs' ) . " $LogMessageIDs." );

    my $CONFIG = ServiceNow::Configuration->new();

    $CONFIG->setSoapEndPoint($ServiceNowSOAPEndpoint);
    $CONFIG->setUserName($ServiceNowSOAPUser);
    $CONFIG->setUserPassword($ServiceNowSOAPPass);

    my $incident_number = undef;
    eval {
	## For the Caller field in a ServiceNow incident to be filled in properly,
	## the ServiceNow Integration must pass as the 'caller_id' field a username
	## which matches an existing name in the ServiceNow database, when creating
	## the incident.  That full name must look either like "Firstname Lastname",
	## matching a corresponding name in ServiceNow, or it must be a single word
	## that matches a single-word name in ServiceNow.  Since we will be deriving
	## the caller name from the GroundWork logged-in operator name, the customer
	## must create all GroundWork user IDs that might create ServiceNow incidents
	## in one of these same formats, and ensure that the ServiceNow database
	## contains those same operator names.  If this convention is not followed,
	## whatever we send to ServiceNow as the caller in the incident definition
	## will just be ignored.
	##
	## That said, we have a customer that has their ServiceNow instance set up to
	## ignore whatever we send as the caller_id value, and to just use a specific
	## fixed value ("GroundWork", possibly including a trailing space character)
	## which is configured on their side to be used for all incidents created by
	## the $ServiceNowSOAPUser that we use above.  In that situation, there is
	## little to worry about here with respect to the caller_id value we send.

	# GWMON-9912:  We need to fix the Event Console to ensure that the two-word
	# operator name is passed to the oneway_helpdesk.pl script as just one
	# command-line argument, by proper quoting or equivalent management of the
	# command-line arguments.  See
	# os/trunk/foundation/collage/impl/common/src/java/org/groundwork/foundation/bs/actions/ShellScriptAction.java
	# for how this script is called.

	# I'm just guessing about how to enter the comments, but it seems to work
	# just fine.  See the complex structure of <sys_journal_field> in the
	# Incident XML of the ServiceNow database schema for a more complex notion
	# of comment data.

	# The Incident GUI on a ServiceNow test system used a single-level Location.
	# Conversely, we saw a multi-level selection of the Location on a ServiceNow
	# demo system.  This somehow affects what values we might send as the
	# 'location' on a production system.  A little testing with selection in
	# the multi-level setup shows that only the leaf value is saved, but when
	# we try to set only the leaf value in 'location' to the same value we saw
	# when we set that value via the ServiceNow Incident GUI, the attempt fails.
	#
	# We've been told that the test system reflects what will be used for the
	# production system, so we're not making any attempt to handle a multi-level
	# location.  FIX LATER:  That situation might be different for some future
	# customer, at which time we would need to generalize the code here.

	# FIX LATER:  Ditto for the assignment group.

	# The ServiceNow test system uses 'state', while the ServiceNow demo system
	# seems to use 'incident_state' (with 'state' relegated to a secondary duty).
	# We've been told that the production system will mirror the test system,
	# so that's what we will go forward with here.  FIX LATER:  That situation
	# might be different for some future customer, at which time we would need to
	# generalize the code here to allow setting 'incident_state' instead of 'state'.

	if ( $params->{'Debug'} ) {
	    $logObj->log("DEBUG:  Parameters for a new incident:");
	    $logObj->log("DEBUG:          caller_id = '$Caller'.");
	    $logObj->log("DEBUG:           location = '$Location'.");
	    $logObj->log("DEBUG:           category = '$Category'.")               if $Send_category;
	    $logObj->log("DEBUG:        subcategory = '$Subcategory'.")            if $Send_subcategory;
	    $logObj->log("DEBUG:            cmdb_ci = '$ConfigurationItem'.")      if $Send_cmdb_ci;
	    $logObj->log("DEBUG:            company = '$NewIncidentCompany'.")     if $Send_company;
	    $logObj->log("DEBUG:           severity = '$NewIncidentSeverity'.")    if $Send_severity;
	    $logObj->log("DEBUG:             impact = '$NewIncidentImpact'.")      if $Send_impact;
	    $logObj->log("DEBUG:            urgency = '$CalculatedUrgency'.")      if $Send_urgency;
	    $logObj->log("DEBUG:          opened_by = '$Caller'.")                 if $Send_opened_by;
	    $logObj->log("DEBUG:       contact_type = '$NewIncidentContactType'.") if $Send_contact_type;
	    $logObj->log("DEBUG:              state = '$NewIncidentState'.");
	    $logObj->log("DEBUG:   assignment_group = '$AssignmentGroup'.");
	    $logObj->log("DEBUG:  short_description = '$ShortDescription'.");
	    $logObj->log("DEBUG:           comments = '$AdditionalComments'.");
	    $logObj->log("DEBUG:         work_notes = '$summary'.");
	}

	# Incident fields which are presently required in our integration.
	my %incident_fields = (
	    'caller_id'         => $Caller,
	    'location'          => $Location,
	    'state'             => $NewIncidentState,
	    'assignment_group'  => $AssignmentGroup,
	    'short_description' => $ShortDescription,
	    'comments'          => $AdditionalComments,
	    'work_notes'        => $summary,
	);

	# Incident fields which are optional in our integration.
	$incident_fields{category}     = $Category               if $Send_category;
	$incident_fields{cmdb_ci}      = $ConfigurationItem      if $Send_cmdb_ci;
	$incident_fields{company}      = $NewIncidentCompany     if $Send_company;
	$incident_fields{contact_type} = $NewIncidentContactType if $Send_contact_type;
	$incident_fields{impact}       = $NewIncidentImpact      if $Send_impact;
	$incident_fields{opened_by}    = $Caller                 if $Send_opened_by;
	$incident_fields{severity}     = $NewIncidentSeverity    if $Send_severity;
	$incident_fields{subcategory}  = $Subcategory            if $Send_subcategory;
	$incident_fields{urgency}      = $CalculatedUrgency      if $Send_urgency;

	# In the code below, I'd like to use the createIncident() convenience method:
	#     my $ServiceNow = ServiceNow->new($CONFIG);
	#     my $incident_number = $ServiceNow->createIncident({...});
	# but at least in the ServiceNow-1.01 release, its error handling is terrible.
	# If an error occurs, it returns undef, but there is then no way to retrieve
	# the reason for the error.  So we call the lower-level routine ourselves,
	# allowing us access to the error details.

	# The following comment applies to the ServiceNow-1.00 package.  Possibly this situation
	# might be altered in future releases.
	#
	# $incident->insert() returns $incident->{'RESULT'}->getValue("sys_id"), which may well
	# not be the incident number!  In fact, it's not at all clear what $sys_id represents.
	# If we were able to call $ServiceNow->createIncident() instead, we would find that it
	# returns the return value of $incident->create(), which itself calls $incident->insert()
	# and then returns $incident->getValue("number") instead.  So we mirror that behavior
	# here, with the added benefit that we have $incident directly in hand so we can look at
	# $incident->{RESULT} if need be to diagnose problems.
	#
	# ServiceNow-1.01, in conjunction with a more-recent ServiceNow release, returns the same
	# type of data.  So this code remains stable, except that we have extended it for testing
	# purposes to allow alternatives to be easily tried against future ServiceNow packages.
	# That has helped with debugging a customer deployment, demonstrating that ServiceNow
	# needed to make some tweaks on their server side to get their Perl API to work once again
	# as documented.

	# Block all the sneaky ways we know of to alter the means by which an SSL certificate is
	# or is not validated, to ensure that we have complete control via our own config settings.
	local %ENV = %ENV;
	delete $ENV{HTTPS_CA_DIR};
	delete $ENV{HTTPS_CA_FILE};
	delete $ENV{PERL_LWP_ENV_PROXY};
	delete $ENV{PERL_LWP_SSL_CA_FILE};
	delete $ENV{PERL_LWP_SSL_CA_PATH};
	delete $ENV{PERL_LWP_SSL_VERIFY_HOSTNAME};

	# Set up Perl environment variables to control certificate validation for an SSL call to
	# ServiceNow, if corresponding parameters were provided in the config file.  If neither of
	# these is defined, the LWP::Protocol::https code will fall back to the trusted certificates
	# provided by the installed Mozilla::CA package.  If that is also not available, hostname
	# verification will fail, and the SSL connection to ServiceNow will in turn fail with a
	# "Can't verify SSL peers without knowing which Certificate Authorities to trust" message.
	# Other, similar, forms of failure include a "Can't connect ... (certificate verify failed)"
	# message.
	#
	# We have to use Perl environment variables for this purpose, simply overriding defaults,
	# because the ServiceNow API provides no direct means to accept equivalent parameters and
	# pass them through to the SOAP::Lite package and thence to the LWP::Protocol::https package.
	# Fortunately, the ServiceNow layer does not itself override the defaults, so setting the
	# defaults here will achieve the desired effect.
	$ENV{PERL_LWP_SSL_CA_FILE} = $ServiceNow_SSL_CA_FILE if defined($ServiceNow_SSL_CA_FILE) && $ServiceNow_SSL_CA_FILE ne '';
	$ENV{PERL_LWP_SSL_CA_PATH} = $ServiceNow_SSL_CA_PATH if defined($ServiceNow_SSL_CA_PATH) && $ServiceNow_SSL_CA_PATH ne '';

	my $ServiceNow = undef;
	my $incident   = undef;
	my $sys_id     = undef;
	if (0) {
	    $ServiceNow      = ServiceNow->new($CONFIG);
	    $incident_number = $ServiceNow->createIncident( \%incident_fields );
	}
	else {
	    $incident = ServiceNow::ITIL::Incident->new($CONFIG);
	    $sys_id   = $incident->insert( \%incident_fields );

	    # FIX LATER:  Should we check the value of $sys_id in any way?  In our testing, it
	    # is sometimes undefined when we get an incident submission error (though I have also
	    # seen WSRESULT.insertResponse.sys_id be a value such as '2ad5f7cdd50b000000e6084603083a0f'
	    # when incident creation failed).  I'm not sure if there is any extra value in checking it,
	    # beyond what we do for the $incident_number.
	    $logObj->log("ERROR:  Incident creation returned an undefined sys_id.") if not defined $sys_id;

	    if ( 0 && defined $sys_id ) {
		## This branch is just an experiment in the face of not getting an incident number
		## back through the expected data structures when the ServiceNow server was not
		## working properly.  This code is not at all intended for production use.
		$incident->addQuery( 'sys_id', $sys_id );
		$incident->query();
		if ( $incident->next() ) {
		    if (0) {
			$incident_number = $incident->getValue("number");
		    }
		    else {
			my %record = $incident->getRecord();
			$incident_number = $record{'number'};
		    }
		}
	    }
	    else {
		$incident_number = $incident->getValue("number");
	    }
	}

	if ( defined $incident_number ) {
	    $logObj->log("ServiceNow Incident identifier:  $incident_number");
	    ## Fill in the results data.
	    $resultsHash{'TicketNo'}     = $incident_number;
	    $resultsHash{'TicketStatus'} = "New";              # technically we should check ServiceNow to see the State value
	    $resultsHash{'ClientData'}   = $incident_number;
	    $resultsHash{'FilingError'}  = "";

	    # These can sometimes help in development debugging.
	    # $logObj->log("Incident details follow:");
	    # $logObj->log( Dumper( $incident ) );
	}
	else {
	    $logObj->log("ERROR:  Failed to create a ServiceNow Incident.");
	    $logObj->log("Error details follow:");
	    $logObj->log( Dumper( $incident->{RESULT} ) );
	    ## $logObj->log( Dumper( $ServiceNow ) );
	    if ( $params->{'Debug'} ) {
		$logObj->log("Full details follow:");
		$logObj->log( Dumper( $incident ) );
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

    if ( @acknowledgements and defined $incident_number ) {
	if ( not $moduleParams->{'send_to_nagios'} ) {
	    $logObj->log('NOTICE:  ServiceNow::HelpDesk is configured to not send acknowledgements to Nagios.');
	}
	else {
	    ## Now that we know what the incident number is, insert it into the acknowledgement
	    ## messages for easy tracking purposes.  That's one reason why we waited until now to
	    ## send the acknowledgements (the other reason being that we don't want to turn off
	    ## Nagios alerts if in fact we didn't manage to create a ServiceNow incident).
	    s/{INCIDENT_NUMBER}/$incident_number/g for @acknowledgements;

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

	    if ( $moduleParams->{'use_nsca'} ) {
		# FIX LATER:  Allow optionally writing all the acknowledgements efficiently to the Bronx
		# socket.  I suppose that means we should run send_nsca to handle the connection protocol.
		# But all the heavy lifting should be done inside a separate GW::Bronx module.
		# FIX LATER:  Make sure the messages we constructed above are the right format for writing
		# to the Bronx socket.
		$logObj->log('ERROR:  use_nsca is not yet supported');
	    }
	    else {
		my $nagios = GW::Nagios->new (
		    $moduleParams->{'nagios_command_pipe'},
		    $moduleParams->{'max_command_pipe_write_size'},
		    $moduleParams->{'max_command_pipe_wait_time'}
		);
		if (not defined $nagios) {
		    ## We have already created the incident, so there is no sense in dying
		    ## once we have logged this error.  The best we can do is to carry on,
		    ## while at the same time notifying the caller about the problem so
		    ## some evidence of failure can be reflected back to the operator.
		    my $count = scalar @acknowledgements;
		    $logObj->log('ERROR:  Creating a GW::Nagios object has failed; thus');
		    $logObj->log("        $count acknowledgements will not be sent to Nagios.");
		    $resultsHash{'FilingError'} = "ERROR:  could not create a GW::Nagios object to send $count acknowledgements to Nagios";
		}
		else {
		    my $errors = $nagios->send_messages_to_nagios(\@acknowledgements);
		    $logObj->log($_) for @$errors;
		    ## Tell the caller about any problem in sending to Nagios, so some
		    ## evidence of failure can be reflected back to the operator.
		    $resultsHash{'FilingError'} = join('; ', @$errors) if @$errors;
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

    #--------------------------------------------------------------
    # Cycle through each Ticketed Event and check to see if its
    # ticket status has changed in the HelpDesk system.
    #
    # If it has, change the status of the ticketed event and
    # set the hasChanged property to 1 for that ticket.
    #--------------------------------------------------------------

    $helpDeskObj = $self;    # Global references to $self
    my $logObj = $self->getLogObj();    # Shorthand reference for log

    my $ServiceNowSOAPEndpoint = $moduleParams->{'ServiceNowSOAPEndpoint'};
    my $ServiceNowSOAPUser     = $moduleParams->{'ServiceNowSOAPUser'};
    my $ServiceNowSOAPPass     = $moduleParams->{'ServiceNowSOAPPass'};
    my $ServiceNow_SSL_CA_FILE = $moduleParams->{'ServiceNow_SSL_CA_FILE'};
    my $ServiceNow_SSL_CA_PATH = $moduleParams->{'ServiceNow_SSL_CA_PATH'};

    # IMPORTANT:  The ServiceNowSOAPEndpoint value must end with a "/" character for it
    # to be handled correctly within the ServiceNow package, so we guarantee that here
    # rather than depending on the configuration file value to be so specified.
    $ServiceNowSOAPEndpoint .= '/' if $ServiceNowSOAPEndpoint !~ m{/$};

    eval {
	my $CONFIG = ServiceNow::Configuration->new();

	$CONFIG->setSoapEndPoint($ServiceNowSOAPEndpoint);
	$CONFIG->setUserName($ServiceNowSOAPUser);
	$CONFIG->setUserPassword($ServiceNowSOAPPass);

	# Block all the sneaky ways we know of to alter the means by which an SSL certificate is
	# or is not validated, to ensure that we have complete control via our own config settings.
	local %ENV = %ENV;
	delete $ENV{HTTPS_CA_DIR};
	delete $ENV{HTTPS_CA_FILE};
	delete $ENV{PERL_LWP_ENV_PROXY};
	delete $ENV{PERL_LWP_SSL_CA_FILE};
	delete $ENV{PERL_LWP_SSL_CA_PATH};
	delete $ENV{PERL_LWP_SSL_VERIFY_HOSTNAME};

	# Set up Perl environment variables to control certificate validation for an SSL call to
	# ServiceNow, if corresponding parameters were provided in the config file.  If neither of
	# these is defined, the LWP::Protocol::https code will fall back to the trusted certificates
	# provided by the installed Mozilla::CA package.  If that is also not available, hostname
	# verification will fail, and the SSL connection to ServiceNow will in turn fail with a
	# "Can't verify SSL peers without knowing which Certificate Authorities to trust" message.
	# Other, similar, forms of failure include a "Can't connect ... (certificate verify failed)"
	# message.
	#
	# We have to use Perl environment variables for this purpose, simply overriding defaults,
	# because the ServiceNow API provides no direct means to accept equivalent parameters and
	# pass them through to the SOAP::Lite package and thence to the LWP::Protocol::https package.
	# Fortunately, the ServiceNow layer does not itself override the defaults, so setting the
	# defaults here will achieve the desired effect.
	$ENV{PERL_LWP_SSL_CA_FILE} = $ServiceNow_SSL_CA_FILE if defined($ServiceNow_SSL_CA_FILE) && $ServiceNow_SSL_CA_FILE ne '';
	$ENV{PERL_LWP_SSL_CA_PATH} = $ServiceNow_SSL_CA_PATH if defined($ServiceNow_SSL_CA_PATH) && $ServiceNow_SSL_CA_PATH ne '';

	foreach my $event (@$ticketedEvents) {
	    my $incident = ServiceNow::ITIL::Incident->new($CONFIG);
	    ## The fact that the incident number is the 'number' field is not documented
	    ## anywhere that I can see in the ServiceNow Wiki.  We only know this from
	    ## dumping out the entire retrieved incident and looking to see all the fields
	    ## provided, then identifying the one that looks like the "INC2009498" incident
	    ## numbers we see in the ServiceNow Incident GUI.
	    $incident->addQuery( 'number', $event->{'TicketNo'} );
	    if ( $incident->query() ) {
		## The ServiceNow documentation shows running $incident->next() in a loop,
		## iterating over all incidents that satisfy the query.  But we're using
		## what ought to be a unique identifier for the incident, so I don't see
		## any purpose in iterating.  Also, I've seen huge amounts of data returned
		## in testing when the query parameters are mangled, and I want to avoid
		## any similar failure in the field.  So we limit our view to just the
		## first incident found by the query.
		if ( $incident->next() ) {
		    ## Incident states are stored within ServiceNow as a numeric enumeration, but
		    ## their corresponding display values are readable strings.  By experiment on
		    ## a ServiceNow test system, we see the following possible display values:
		    ##
		    ##     'state'  'dv_state'
		    ##     State    State Display Value
		    ##     =======  ===================
		    ##     1        New
		    ##     2        Active
		    ##     5        Awaiting User Info
		    ##     6        Resolved
		    ##     7        Closed
		    ##
		    ## On the other hand, on a ServiceNow demo system, we have seen these
		    ## combinations of state information:
		    ##
		    ##     'state'  'dv_state'           'incident_state'  'dv_incident_state'
		    ##     State    State Display Value  Incident State    Incident State Display Value
		    ##     =======  ===================  ================  ============================
		    ##     1        Open                 1                 New
		    ##     1        Open                 2                 Active
		    ##     1        Open                 3                 Awaiting Problem
		    ##     1        Open                 4                 Awaiting User Info
		    ##     1        Open                 5                 Awaiting Evidence
		    ##     1        Open                 6                 Resolved
		    ##     1        Open                 100               Help
		    ##     3        Closed Complete      7                 Closed
		    ##
		    ## For now, we've been told that the test-system setup mirrors what we will
		    ## see on the production system, so that's what we will roll forward with.
		    ## FIX LATER:  That situation might be different for some future customer,
		    ## at which time we would need to generalize the code here.
		    ##
		    ## We are storing the display value in the HelpDeskLookupTable.TicketStatus
		    ## column, so that's the form we want to get back here to compare with to see
		    ## if the incident status has changed.
		    my $ticketStatus = $incident->getValue('dv_state');
		    if ( $ticketStatus ne $event->{'TicketStatus'} ) {
			$event->{'TicketStatus'} = $ticketStatus;
			$event->{'hasChanged'}   = 1;
		    }

		    ## We also capture and return the "Close notes" and "Resolved at" fields from the
		    ## incident, if any.  The caller can use these to, for instance, add them to the
		    ## GroundWork acknowledge comments, and to sort them properly before such adding.
		    my $close_notes = $incident->getValue('close_notes');
		    if ( defined($close_notes) ) {
			## ServiceNow provides \r\n line termination.  Start by cleaning that up for our use.
			$close_notes =~ s/\r//g;
			## We must transform newlines to simple whitespace because the downstream
			## processing (sending as as comment string to Nagios) will treat a newline
			## as the end of the command, thereby truncating at that point.
			$close_notes =~ s/\n/ /g;
			$close_notes =~ s/^\s+//;
			$close_notes =~ s/\s+$//;
			$event->{'close_notes'} = $close_notes if $close_notes ne '';
		    }
		    my $resolved_at = $incident->getValue('resolved_at');
		    if (defined($resolved_at) && $resolved_at !~ /^\s*$/) {
			($event->{'resolved_at'} = $resolved_at) =~ s/\r//g;
		    }

		    # We considered passing along the "resolved_by" field to be used as the <author>
		    # field in a Nagios acknowledgment, but that field ends up being a gobbledegook
		    # string like "62dc591b74b03100f1f5d2633bc939c8", which is of little use to us.
		}
	    }
	    else {
		$logObj->log("ERROR:  Query for ServiceNow Incident $event->{'TicketNo'} has failed.");
		## In our testing, this doesn't really yield much of diagnostic interest -- just a
		## very large amount of gobbledegook.  If you're faced with a serious problem, though,
		## it's at least a possible source of information.
		# $logObj->log("Error details follow:");
		# $logObj->log( Dumper($incident) );
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


#!/usr/local/groundwork/perl/bin/perl -w --

# Notes Aug 31 2015
# KB: https://kb.groundworkopensource.com/display/GWENG/NoMa+Auto+Ticketing+Requirements+and+Functional+Description
# This script was a work-in-progress for UCB footprints integration with GW NoMa.
# UCB no longer required this integration so the final footprints integration pieces were not completed.
# A host or service notification would be sent from NoMa and this script was designed to be
# the NoMa notifier handler script that processed the notification.
# This script does this :
# - parses an incoming notification from NoMa
# - for service CRITICAL and host DOWN notifications, it :
#   - upserts a dynamic property on the service/host with a ticket value 
#   - that value would be the case/ticket # returned from an API call to the footprints systems (just time() for now)
#   - auto creation of the dynamic property if it doesn't exist
#   - and then the auto application to the service or host
#   - creates GW AUDIT events for case creation
#   - appends ticket # to GW service/host status
# - for service OK and host UP notifications, it :
#   - gets the footprints ticket # from the dynamic property
#   - updates the footprints ticket with some OK update (currently stubbed and does nothing)
#   - creates GW event
#   - removes ticket # from GW service/host status
# - RAPID.pm was also updated for this to support some property types methods

use warnings;
use strict;

use version;
my  $VERSION = qv('0.0.1'); 
use GW::RAPID;
use Data::Dumper; $Data::Dumper::Indent= 2; $Data::Dumper::Sortkeys = 1;
use YAML::Syck;
use POSIX qw(strftime);  
use Log::Log4perl qw(get_logger);
Log::Log4perl::init('/usr/local/groundwork/config/tix.log4perl.conf'); # logger config
my $logger          = get_logger("GW.tix");  # logger details
my $tix_system_name = 'footprints'; # this is key for various things, include noma yaml method etc
my $noma_yaml       = '/usr/local/groundwork/noma/etc/NoMa.yaml'; # noma yaml file
my $ticket_property = 'footprintsTicketId'; # name of the dynamic property type that will be used for storing ticket info in the GW data model
#my $fp_conf = $conf->{methods}->{$tix_system_name};

my ( %outcome , @results , %results, $error, $rest_api );

# algorithm
# - noma notification comes in, and fires off this handler
# - parse out the notification details into a usable data structure
# - for this host/service see if there's a TicketId (or whatever the field will be for storing the footprints ticket id)
# - if there is a TicketId 
# 	- update the footprints ticket with this TicketId
# - if there isn't a TicketId:
# 	- get a ticket id from footprints
# 	- upsert the host/service with a TicketId property with the footprints ticket id

# TBD
#  - Comments on OK/UP states  - waiting for GWMON-12228 response first
#  - add a health service updater routine for error handling, but one that doesn't notify :) report_problem() started but want to rethink
#  - add a help page and general options like -v 

# Notes config
# ------------
# ### NOMA ###
# 1. Insert a row into the notification_methods table in the NoMa.db sqlite3 db :
#     sqlite3 /usr/local/groundwork/noma/var/NoMa.db
#     sqlite> insert into notification_methods values (7, "footprints",    "footprints","email", "", 0,0);
# 2. Modify /usr/local/groundwork/noma/etc/NoMa.yaml to include the footprints method
# 3. service groundwork restart noma
# 4. create a noma rule that uses the footprints method
# 5. install this script into /usr/local/groundwork/noma/notifier/tix.pl
#
## ### GW ###
# 1. create a resource macro with value /usr/local/groundwork/noma/notifier for the location of the notifier for step 2
# 2. create new commands : 
#         host-notify-by-noma
#         $macro$/alert_via_noma.pl -c h -s "$HOSTSTATE$" -H "$HOSTNAME$"  -G "$HOSTGROUPNAMES$" -n "$NOTIFICATIONTYPE$" -i "$HOSTADDRESS$" -o "$HOSTOUTPUT$" -t "$TIMET$" -u "$HOSTNOTIFICATIONID$" -A "$NOTIFICATIONAUTHORALIAS$" -C "$NOTIFICATIONCOMMENT$" -R "$NOTIFICATIONRECIPIENTS$"
#
#         service-notify-by-noma
#         $macro$/alert_via_noma.pl -c s -s "$SERVICESTATE$" -H "$HOSTNAME$" -G "$HOSTGROUPNAMES$" -E "$SERVICEGROUPNAMES$" -S "$SERVICEDESC$" -o "$SERVICEOUTPUT$" -n "$NOTIFICATIONTYPE$" -a "$HOSTALIAS$" -i "$HOSTADDRESS$" -t "$TIMET$" -u "$SERVICENOTIFICATIONID$" -A "$NOTIFICATIONAUTHORALIAS$" -C "$NOTIFICATIONCOMMENT$" -R "$NOTIFICATIONRECIPIENTS$"
# 3. update nagiosadmin or whichever contact will be used, to use these new commands
# 4. make sure notifications are enabled globally and that hosts/services are using the contact in 3 and notifications are enabled at the object level etc



END {
    # To be kind to the server and always disconnect our session, we attempt to force a shutdown
    # of the REST API before global destruction sets in and makes it impossible to log out,
    # regardless of how we got to the end of the program.
    $rest_api = undef;
}
    

$logger->debug( "Starting $0 at " . localtime() );
main();
$logger->debug( "$0 Finished at " . localtime() );

# ---------------------------------------------------------------------------------
sub main
{
    my ( %hprops, %sprops ) ;
    my ( %notification_details, $noma_yaml_ref );

    # Parse the incoming noma CLI and break it out into a more usable data structure
    if ( not get_notification_details( \%notification_details ) ) {
        $logger->error("Could not determine notification details successfully - quitting");
        exit;
    }

    # Load the NoMa YAML configuration
    if ( not load_noma_yaml( \$noma_yaml_ref, $noma_yaml ) ) {
        $logger->error("Could not load noma yaml $noma_yaml - quitting");
        exit;
    }

    # initialize GW REST api - TBD in the future there will be a tix object, like Feeder which will hide all of this stuff
    if ( not initialize_GW_REST_api() ) { 
        $logger->logdie("Could not initialize GW REST API - quitting");
    }

    # handle the incoming notification
    handle_notification( \%notification_details );

}

# ---------------------------------------------------------------------------------
sub handle_notification
{
    # If the incoming notification is a service :
    #   - if the status is UNSCHEDULED CRITICAL, create a ticket, get the ticket #, put that into the service property
    #   - if the status is OK, if there's a ticket on this service, update the ticket to indicate issue is now OK

    my ( $notification_ref ) = @_;
    
    # service notification
    if (  $notification_ref->{nomanotificationtype} eq 's' ) {
        $logger->info("Handling notification : host '$notification_ref->{hostname}', service '$notification_ref->{servicename}', state '$notification_ref->{state}'");
        if ( $notification_ref->{state} eq 'CRITICAL' ) {
            $logger->debug("Handling CRITICAL service notification");
            handle_critical_service( $notification_ref );
            
        }
        elsif ( $notification_ref->{state} eq 'OK' ) {
            $logger->debug("Handling OK service notification");
            handle_ok_service( $notification_ref );
        }
        else {
            $logger->error("No service handler for incoming state : '$notification_ref->{state}'");
        }
    }

    # host notification
    elsif (  $notification_ref->{nomanotificationtype} eq 'h' ) {
        $logger->info("Handling notification : host '$notification_ref->{hostname}', state '$notification_ref->{state}'");
        if ( $notification_ref->{state} eq 'DOWN' ) {
            $logger->debug("Handling DOWN host notification");
            handle_down_host( $notification_ref );
        }
        elsif ( $notification_ref->{state} eq 'UP' ) {
            handle_up_host( $notification_ref );
            $logger->debug("Handling UP host notification");
        }
        else {
            $logger->error("No host handler for incoming state : '$notification_ref->{state}'");
        }
    }

    else {
        $logger->logdie("Unrecognized NoMa notification type '$notification_ref->{nomanotificationtype}' - quitting");
    }

}

# ---------------------------------------------------------------------------------
sub handle_critical_service
{
    my ( $notification_ref ) = @_;

    my ( $ticket_number, $error, %sprops , $service_status ) = undef;

    # First, create a new footprints issue and retrieve the ticket # returned from that process
    if ( not create_footprints_issue( \$ticket_number ) ) {
        $logger->error("Failed to create footprints ticket number");
        return 0;
    }
    
    # Error if no ticket # returned
    if ( not $ticket_number or not defined $ticket_number or $ticket_number =~ /^\s*$/ ) {
        $logger->error("Failed to retrieve a footprints ticket number");
        return 0;
    }

    # Generate a GW AUDIT event for the creation of the ticket
    if (  not generate_audit_event( $notification_ref, "Created footprints ticket '$ticket_number'" ) ) {
        $logger->error("Failed to generate GroundWork AUDIT event for ticket creation event");
    }

    # update the service with the ticket #
    %sprops = ( 
                    $notification_ref->{'hostname'} => {  
                        $notification_ref->{'servicename'} => { 
                            $ticket_property => $ticket_number 
                        }
                    }
                );

    if ( not set_service_dynamic_properties_values (\%sprops, \$error ) ) { 
        $logger->error("Failed to store property '$ticket_property' with value '$ticket_number' on service '$notification_ref->{'servicename'}', on host '$notification_ref->{'hostname'}'");
        return 0;
    }
    else {
        $logger->debug("Stored property '$ticket_property' with value '$ticket_number' on host/service $notification_ref->{'hostname'}/$notification_ref->{'servicename'}");
    }

    # Now set the service status by appending it with the ticket #
    %sprops = ( 
                $notification_ref->{'hostname'} => {  
                    $notification_ref->{'servicename'} => { 
                        props => [ 'LastPluginOutput' ] 
                    } 
                } 
    );
    if ( not get_service_dynamic_properties_values (\%sprops, \$error ) ) { 
        $logger->error("Failed to get property 'LastPluginOutput' from host '$notification_ref->{hostname}', service '$notification_ref->{servicename}'");
        return 0;
    }
    # update the service status by appending the ticket id - assumes something is set else will get a perl warning here no biggee
    $logger->debug("Updating service status by appending ticket number to end of it");
    # TBD reconsider safest way to include this info - right now appends it to the end as '(<ticket property id>:<ticket id>)' 
    # TBD if change this here, then change service_ok_host() too. 
    # TBD better to have a generalized pattern that is global. Next version.
    $service_status = $sprops{ $notification_ref->{'hostname'}} { $notification_ref->{'servicename'}} { results } { 'LastPluginOutput' } . " ($ticket_property:$ticket_number)";
    %sprops = ( 
                    $notification_ref->{'hostname'} => {  
                        $notification_ref->{'servicename'} => {  
                            'LastPluginOutput' => $service_status
                        }
                    }
    );
    if ( not set_service_dynamic_properties_values (\%sprops, \$error ) ) { 
        $logger->error("Failed to update service status with ticket number for host/service '$notification_ref->{'hostname'}'/'$notification_ref->{'servicename'}");
        return 0;
    }

    return 1;
}

# ---------------------------------------------------------------------------------
sub handle_ok_service
{
    #   if the status is OK, if there's a ticket on this service, update the ticket to indicate issue is now OK
    my ( $notification_ref ) = @_;

    my ( $ticket_number , $error , $service_status ) = undef;

    # First, see if there's a footprints ticket associated with this service
    my %sprops = ( 
                    $notification_ref->{'hostname'} => {
                        $notification_ref->{'servicename'} => {
                            props => [ $ticket_property ]
                        }
                    }
    );
    get_service_dynamic_properties_values( \%sprops, \$error ); 
    if ( defined $error ) {
        $logger->error($error) ;
        return 0;
    }
    if ( exists $sprops{ $notification_ref->{'hostname'} }{ $notification_ref->{'servicename'} }{ results } { $ticket_property } ) { 
        $ticket_number = $sprops{ $notification_ref->{'hostname'} }{ $notification_ref->{'servicename'} }{ results } { $ticket_property };
    }

    # update the ticket if there was a ticket number that is set to something
    if ( defined $ticket_number and $ticket_number ) {
        if ( not update_footprints_issue( $ticket_number ) ) {
            $logger->error("Failed to update footprints ticket number '$ticket_number'");
            return 0;
        }
        # Generate a GW AUDIT event for the update of the ticket
        if (  not generate_audit_event( $notification_ref, "Updated footprints ticket '$ticket_number' - service OK" ) ) {
            $logger->error("Failed to generate GroundWork AUDIT event for ticket creation event");
        }

        # Now update the service status by removing the ticket # info
        %sprops = ( 
                $notification_ref->{'hostname'} => {  
                    $notification_ref->{'servicename'} => { 
                        props => [ 'LastPluginOutput' ] 
                    } 
                } 
        );
        if ( not get_service_dynamic_properties_values (\%sprops, \$error ) ) { 
            $logger->error("Failed to get property 'LastPluginOutput' from host '$notification_ref->{hostname}', service '$notification_ref->{servicename}'");
            return 0;
        }
        # update the service status by removing the ticket id - assumes something is set else will get a perl warning here no biggee
        $logger->debug("Updating service status by removing ticket number from end of it");
        $service_status = $sprops{ $notification_ref->{'hostname'}} { $notification_ref->{'servicename'}} { results } { 'LastPluginOutput' } ;
        if ( $service_status =~ /\s\($ticket_property:.*\)/ ) {
            $service_status =~ s/\s\($ticket_property:.*\)//g;
            %sprops = ( 
                        $notification_ref->{'hostname'} => {  
                            $notification_ref->{'servicename'} => {  
                                'LastPluginOutput' => $service_status
                            }
                        }
            );
            if ( not set_service_dynamic_properties_values (\%sprops, \$error ) ) { 
                $logger->error("Failed to update service status with ticket number for host/service '$notification_ref->{'hostname'}'/'$notification_ref->{'servicename'}");
                return 0;
            }
        }
    }
    else {
        $logger->debug("No ticket attached - nothing to do");
    }

    return 1;

}


# ---------------------------------------------------------------------------------
sub handle_down_host
{
    my ( $notification_ref ) = @_;

    my ( $ticket_number, $error, %hprops, $host_status ) = undef;

    # First, create a new footprints issue and retrieve the ticket # returned from that process
    if ( not create_footprints_issue( \$ticket_number ) ) {
        $logger->error("Failed to create footprints ticket number");
        return 0;
    }
    
    # error if no ticket # returned
    if ( not $ticket_number or not defined $ticket_number or $ticket_number =~ /^\s*$/ ) {
        $logger->error("Failed to retrieve a footprints ticket number");
        return 0;
    }

    # Generate a GW AUDIT event for the creation of the ticket
    if (  not generate_audit_event( $notification_ref, "Created footprints ticket '$ticket_number'" ) ) {
        $logger->error("Failed to generate GroundWork AUDIT event for ticket creation event");
    }

    # Update the host status with the new ticket #
    %hprops = ( 
                    $notification_ref->{'hostname'} => {  
                            $ticket_property => $ticket_number 
                    }
    );
    if ( not set_host_dynamic_properties_values (\%hprops, \$error ) ) { 
        $logger->error("Failed to store property '$ticket_property' with value '$ticket_number' on host '$notification_ref->{'hostname'}'");
        return 0;
    }
    else {
        $logger->debug("Stored property '$ticket_property' with value '$ticket_number' on host $notification_ref->{'hostname'}");
    }
    # Now get the host status by appending it with the ticket #
    %hprops = ( 
                $notification_ref->{'hostname'} => {  
                    props => [ 'LastPluginOutput' ] 
                } 
    );
    if ( not get_host_dynamic_properties_values (\%hprops, \$error ) ) { 
        $logger->error("Failed to get property 'LastPluginOutput' from host '$notification_ref->{'hostname'}'");
        return 0;
    }
    # update the host status by including the ticket id - assumes something is set else will get a perl warning here no biggee
    $logger->debug("Updating host status by appending ticket number to end of it");
    # TBD reconsider safest way to include this info - right now appends it to the end as '(<ticket property id>:<ticket id>)' 
    # TBD if change this here, then change handle_up_host() too. 
    # TBD better to have a generalized pattern that is global. Next version.
    $host_status = $hprops{ $notification_ref->{'hostname'}} { results } { 'LastPluginOutput' } . " ($ticket_property:$ticket_number)";
    %hprops = ( 
                    $notification_ref->{'hostname'} => {  
                            'LastPluginOutput' => $host_status
                    }
    );
    if ( not set_host_dynamic_properties_values (\%hprops, \$error ) ) { 
        $logger->error("Failed to update host status with ticket number for host '$notification_ref->{'hostname'}'");
        return 0;
    }

    return 1;

}

# ---------------------------------------------------------------------------------
sub handle_up_host
{
    # if the status is OK, if there's a ticket on this service, update the ticket to indicate issue is now OK
    my ( $notification_ref ) = @_;

    my ( $ticket_number , $error, $host_status ) = undef;

    # First, see if there's a footprints ticket associated with this service
    my %hprops = ( 
                    $notification_ref->{'hostname'} => {
                        props => [ $ticket_property ]
                    }
    );
    get_host_dynamic_properties_values( \%hprops, \$error ); 
    if ( defined $error ) {
        $logger->error($error) ;
        return 0;
    }
    if ( exists $hprops{ $notification_ref->{'hostname'} }{ results } { $ticket_property } ) { 
        $ticket_number = $hprops{ $notification_ref->{'hostname'} }{ results } { $ticket_property };
    }

    # update the ticket if there was a ticket number that is set to something
    if ( defined $ticket_number and $ticket_number ) {
        if ( not update_footprints_issue( $ticket_number ) ) {
            $logger->error("Failed to update footprints ticket number '$ticket_number'");
            return 0;
        }
        # Generate a GW AUDIT event for the update of the ticket
        if (  not generate_audit_event( $notification_ref, "Updated footprints ticket '$ticket_number' - host UP" ) ) {
            $logger->error("Failed to generate GroundWork AUDIT event for ticket creation event");
        }

        # Now update the host status by removing any ticket # from it
        $logger->debug("Updating host status by removing ticket number from end of it");
        %hprops = ( 
                    $notification_ref->{'hostname'} => {  
                        props => [ 'LastPluginOutput' ] 
                    } 
        );
        if ( not get_host_dynamic_properties_values (\%hprops, \$error ) ) { 
            $logger->error("Failed to get property 'LastPluginOutput' from host '$notification_ref->{'hostname'}'");
            return 0;
        }
        # update the host status by including the ticket id - assumes something is set else will get a perl warning here no biggee
        $host_status = $hprops{ $notification_ref->{'hostname'}} { results } { 'LastPluginOutput' } ;
        # Only update it if appears to have the info that needs removing
        if ( $host_status =~ /\s\($ticket_property:.*\)/ ) {
            $host_status =~ s/\s\($ticket_property:.*\)//g;
            %hprops = ( 
                        $notification_ref->{'hostname'} => {  
                                'LastPluginOutput' => $host_status
                        }
            );
            if ( not set_host_dynamic_properties_values (\%hprops, \$error ) ) { 
                $logger->error("Failed to update host status by removing ticket info from host '$notification_ref->{'hostname'}'");
                return 0;
            }
        }
    }
    else {
        $logger->debug("No ticket attached - nothing to do");
    }

    return 1;

}


# ---------------------------------------------------------------------------------
sub create_footprints_issue
{
    # TBD this needs writing
    # for now just returns 1 stat and populates ticket number with the time

    my ( $ticket_ref ) = @_;

    $logger->debug(" !!! TBD !!! - create footprints issue");
    ${$ticket_ref} = time;

    return 1;
}

# ---------------------------------------------------------------------------------
sub update_footprints_issue
{
    # TBD this needs writing
    # for now just returns 1 stat 

    my ( $ticket_ref ) = @_;

    $logger->debug(" !!! TBD !!! - update footprints issue '$ticket_ref' with OK");

    return 1;
}

# ---------------------------------------------------------------------------------
sub tests
{

    my (%hprops, %sprops, $error );
    %hprops = (
                    abc => { 'hdp1' => 'some value',   'hdp2' => 123,  'LastPluginOutput' => 12345 }
    );
    set_host_dynamic_properties_values (\%hprops, \$error ) ;
    %sprops = ( 
                    localhost => {  
                                    linux_uptime => { 'sdp1' => 'val1', 'sdp2' => 'val2' },
                                    svc2 => { 'p3' => '3', 'p4' => 'v4' },
                                    tcp_nsca => { 'Aprop' => '', 'Bprop' => '' }
                                },
                    scom_feeder_host => {  
                                    s3 => { 's31' => 'v31' },
                                    s4 => { 's41' => 'v41', 's42' => 'v42' }
                    }
                );

    set_service_dynamic_properties_values (\%sprops, \$error ) ;

    %hprops = (
                    localhost => { 
                            props => [ 'hdp1', 'LastPluginOutput' ] } ,
                            #results => { 'LastPluginOutput' = "sdsdsa"  } ,
    );

    %sprops = ( 
                    localhost => {  
                                    linux_uptime => { 
                                                props => [ 'sdp1', 'LastPluginOutput', 'Latency' ] 
                                                # results => { 'sdp1' => 123,  'LastPluginOutput' => "xyz" }
                                            } ,
                                    svc2 => { 
                                                props => [ 'sp2' ] 
                                                # results => { } 
                                            } ,
                                },
                    scom_feeder_host => {  
                                    scom_feeder_health => { 
                                                props => [ 'LastPluginOutput' ] 
                                                # results => { 'LastPluginOutput' => "hello" }
                                            }
                          },
                    x2 => {  
                                    svc3 => { 
                                                props => [ 'sdp5', 'LastPluginOutput' ] 
                                                # results => { 'LastPluginOutput' => "hello" }
                                            }
                          },
            );

    get_host_dynamic_properties_values( \%hprops, \$error ); print Dumper \%hprops;
    $logger->fatal($error) if defined $error;

    get_service_dynamic_properties_values( \%sprops, \$error ); print Dumper \%sprops;
    $logger->fatal($error) if defined $error;

    die "Done.\n";

}

# ----------------------------------------------------------------------------------------------------------------------
sub set_service_dynamic_properties_values
{
    # Tries to upsert service dynamic properties 
    # Args :
    #   - ref to a hash like this :
    #       (
    #               h1 => { 
    #                          service1 => { 'hdp1' => 'hdp1_value', ... },
    #                          service2 => { 'LastPluginOutput' => 'some output', ... }
    #                     } , 
    #                     ...
    #       )
    #   - ref to error string
    # Returns :
    #   - error ref undef if all went ok
    #   - error ref with message if any problems were found

    my ( $props_request_hash_ref, $error_ref ) = @_;
    my ( $host, $service, $prop ) ;
    
    ${$error_ref} = undef; # reset the error 

    $logger->trace( "Setting service dynamic properties and values");
    HOST: foreach $host ( sort keys %{ $props_request_hash_ref } ) { 
        # Host needs to exist in order to upsert a property on it...
        $logger->trace( "Searching for host '$host'" ); 
        $rest_api->get_hosts( [ $host ] , { }, \%outcome, \%results ) ;
        if ( defined $outcome{response_code} and $outcome{response_code} ne '404' ) { 
            $logger->error( "Something went wrong getting hosts: " . Dumper \%outcome, \%results );
            next HOST;
        }

        if ( exists $results{ $host } ) { 
            $logger->trace( "Host '$host' exists" );
    
            SERVICE: foreach $service ( sort keys %{$props_request_hash_ref->{$host}} ) { 

                # service needs to exist in order to upsert a property on it ...
                $logger->trace( "Searching for service '$service' on host '$host'" ); 
                $rest_api->get_services( [ $host ] , { }, \%outcome, \%results ) ;
                $rest_api->get_services( [], { hostname => [ $host ],  format => 'service,host'}, \%outcome, \%results );
                if ( defined $outcome{response_code} and $outcome{response_code} ne '404' ) { 
                    $logger->error( "Something went wrong getting services for host '$host' : " . Dumper \%outcome, \%results );
                    next HOST;
                }

                if ( not exists $results{ $service } ) { 
                    $logger->error("Service '$service' on host '$host' does not exist - skipping");
                    next SERVICE;
                }

                # for each property to set and attach ...
                PROP: foreach $prop ( sort keys %{$props_request_hash_ref->{$host}->{$service}} ) {

                    # create the property type if it doesn't exist 
                    if ( not property_type_exists( $prop ) ) {
                        if ( not upsert_property_type( $prop ) ) {
                            $logger->error("Could not upsert property type '$prop' - skipping this property"); # TBD raise this error to sv ?
                            next PROP;
                        }
                    }

                    # attach the property to the service with it's value
                    # do an upsert of the service with just this property and it's value
                    my @service_property_update = {  
                                                    "hostName"   => $host,
                                                    "description"   => $service , # this is the service name
                                                    "properties" => { $prop => $props_request_hash_ref->{$host}->{$service}->{$prop} } 
                    };
                    if ( not $rest_api->upsert_services(  \@service_property_update, {}, \%outcome, \@results ) ) {
                        $logger->error("Failed to upsert host '$host', service '$service' with property '$prop' and property value $props_request_hash_ref->{$host}->{$prop} : " . Dumper \%outcome, \%results );
                    }
                    else {
                        return 1; # success - all other places are failure
                    }
                }

            }

        }
        else { 
            # TBD is there a valid case for this ? The notification came from a host to noma should should exist in foundation
            # TBD raise this error to sv ?
            $logger->error( "HOST '$host' does not exist which is odd because the notification came from this host") ;
        }
    }

    return 0; # failure.

}

# ----------------------------------------------------------------------------------------------------------------------
sub set_host_dynamic_properties_values
{
    # Tries to upsert host dynamic properties 
    # Args :
    #   - ref to a hash like this :
    #       (
    #               h1 => { 
    #                          'hdp1' => 'hdp1_value', 
    #                          'LastPluginOutput' => 'some output', ...
    #                     } ,
    #               h2 => { 
    #                          'hdp2' => 'abc', ...
    #                     } , ...
    #       )
    #   - ref to error string
    # Returns :
    #   - error ref undef if all went ok
    #   - error ref with message if any problems were found

    my ( $props_request_hash_ref, $error_ref ) = @_;
    my ( $host, $prop ) ;
    
    ${$error_ref} = undef; # reset the error 

    $logger->trace( "Setting host dynamic properties and values");
    HOST: foreach $host ( sort keys %{ $props_request_hash_ref } ) { 
        # Host needs to exist in order to upsert a property on it...
        $logger->trace( "Searching for host '$host'" ); 
        $rest_api->get_hosts( [ $host ] , { }, \%outcome, \%results ) ;
        if ( defined $outcome{response_code} and $outcome{response_code} ne '404' ) { 
            $logger->error( "Something went wrong getting hosts: " . Dumper \%outcome, \%results );
            next HOST;
        }

        if ( exists $results{ $host } ) { 
            $logger->trace( "Host '$host' exists" );

            # for each property to set and attach ...
            PROP: foreach $prop ( sort keys %{$props_request_hash_ref->{$host}} ) {

                # create the property type if it doesn't exist 
                if ( not property_type_exists( $prop ) ) {
                    if ( not upsert_property_type( $prop ) ) {
                        $logger->error("Could not upsert property type '$prop' - skipping this property"); # TBD raise this error to sv ?
                        next PROP;
                    }
                }

                # attach the property to the host with it's value
                # do an upsert of the host with just this property and it's value
                my @host_property_update = {  
                                                "hostName"   => $host,
                                                "properties" => { $prop => $props_request_hash_ref->{$host}->{$prop} } 
                };
                if ( not $rest_api->upsert_hosts(  \@host_property_update, {}, \%outcome, \@results ) ) {
                    $logger->error("Failed to upsert host '$host' with property '$prop' and property value $props_request_hash_ref->{$host}->{$prop} : " . Dumper \%outcome, \%results );
                }
                else {
                    return 1; # only place success occurs
                }
            }

        }
        else { 
            # TBD is there a valid case for this ? The notification came from a host to noma should should exist in foundation
            # TBD raise this error to sv ?
            $logger->error( "HOST '$host' does not exist which is odd because the notification came from this host") ;
        }
    }

    return 0; # failure.

}

# ----------------------------------------------------------------------------------------------------------------------
sub get_host_dynamic_properties_values
{
    # Tries to get values of host dynamic properties 
    # Args :
    #   - ref to a hash like this :
    #       (
    #               h1 => { 
    #                          props => [ 'hdp1', 'LastPluginOutput' ] 
    #                     } ,
    #               h2 => { 
    #                          props => [ 'hdp2' ] 
    #                     } , ...
    #       )
    #   - ref to error string
    # 
    # Returns :
    #   - updates hash by ref like this :
    #       (
    #               h1 => { 
    #                          props => [ 'hdp1', 'LastPluginOutput' ] 
    #                          results => { 'LastPluginOutput' => "sdsdsa"  } , # ie a hash or found props and their vals
    #                     } ,
    #               h2 => { 
    #                          props => [ 'hdp2' ] 
    #                          results => { 'hdp2' = "123"  } ,
    #                     } , ...
    #       )
    #   - error ref undef if all went ok
    #   - error ref with message if any problems were found

    my ( $props_request_hash_ref, $error_ref ) = @_;
    my ( $host, $prop ) ;
    
    ${$error_ref} = undef; # reset the error 

    $logger->trace( "Getting host dynamic properties and values");
    HOST: foreach $host ( sort keys %{ $props_request_hash_ref } ) { 
        $logger->trace( "Searching for host '$host'" );
        $rest_api->get_hosts( [ $host ] , { }, \%outcome, \%results ) ;
        if ( defined $outcome{response_code} and $outcome{response_code} ne '404' ) { 
            $logger->error( "Something went wrong getting hosts: " . Dumper \%outcome, \%results );
            next HOST;
        }
        if ( exists $results{ $host } ) { 
            $logger->trace( "Host '$host' exists" );
            # look for each property requested
            foreach $prop ( @{$props_request_hash_ref->{$host}->{props} } ) {
                $logger->trace( "\tChecking for property '$prop'") ;
                if ( exists $results{$host}{properties}{$prop} ) { 
                    $logger->trace( "\t\tFound." );
                    $props_request_hash_ref->{$host}->{results}->{$prop} = $results{$host}{properties}{$prop};
                }
                else { 
                    $logger->trace( "\t\tNot found.") ;
                }
            }
        }
        else { 
            $logger->trace( "HOST '$host' does not exist") ;
        }
    }

    return 1;  # TBD

}

# ----------------------------------------------------------------------------------------------------------------------
sub get_service_dynamic_properties_values
{
    # Tries to get values of a hosts' services dynamic properties
    # Args :
    #   - ref to a hash like this :
    #               localhost => {  
    #                               linux_uptime => { 
    #                                           props => [ 'sdp1', 'LastPluginOutput', 'Latency' ] 
    #                                       } ,
    #                               svc2 => { 
    #                                           props => [ 'sp2' ] 
    #                                       } ,
    #                           }, ...
    #   - ref to error string
    # 
    # Returns :
    #   - updates hash by ref like this :
    #               localhost => {  
    #                               linux_uptime => { 
    #                                           props => [ 'sdp1', 'LastPluginOutput', 'Latency' ] 
    #                                           results => { 'sdp1' => 123,  'LastPluginOutput' => "xyz" }
    #                                       } ,
    #                               svc2 => { 
    #                                           props => [ 'sp2' ] 
    #                                           results => { } 
    #                                       } ,
    #                           }, ...
    #   - error ref undef if all went ok
    #   - error ref with message if any problems were found

    my ( $props_request_hash_ref, $error_ref ) = @_;
    my ( $host, $service, $prop ) ;
    
    ${$error_ref} = undef; # reset the error 

    $logger->trace( "Getting service dynamic properties and values");
    HOST: foreach $host ( sort keys %{ $props_request_hash_ref } ) { 
        $logger->trace( "Searching for services for host '$host'" );
        #$rest_api->get_hosts( [ $host ] , { }, \%outcome, \%results ) ;
        $rest_api->get_services( [], { hostname => $host, format => 'host,service'}, \%outcome, \%results );
        if ( defined $outcome{response_code} and $outcome{response_code} ne '404' ) { 
            $logger->error( "Something went wrong getting hosts: " . Dumper \%outcome, \%results );
            next HOST;
            # return ? Or update $$error_ref TBD
        }
        if ( exists $results{ $host } ) { 
            $logger->trace( "Host '$host' exists" );
            foreach $service ( sort keys %{ $props_request_hash_ref->{$host} } ) { 
                foreach $prop ( @{$props_request_hash_ref->{$host}->{$service}->{props} } ) {
                    #print( "\tChecking for property '$prop' on service '$service'\n") ;
                    $logger->trace( "\tChecking for property '$prop' on service '$service'") ;
                    if ( exists $results{$host}{$service}{properties}{$prop} ) { 
                        $logger->trace( "\t\tFound." );
                        $props_request_hash_ref->{$host}->{$service}->{results}->{$prop} = $results{$host}{$service}{properties}{$prop};
                    }
                    else { 
                        $logger->trace( "\t\tNot found.") ;
                    }
                }
            }

        }
        else { 
            $logger->trace( "HOST '$host' does not exist") ;
        }
    }

    return 1;  # TBD

}
# ----------------------------------------------------------------------------------------------
END {

    # To be kind to the server and always disconnect our session, we attempt to force a shutdown
    # of the REST API before global destruction sets in and makes it impossible to log out,
    # regardless of how we got to the end of the program.
    terminate_rest_api();

    # We generally run this daemon under control of supervise, which will immediately attempt to
    # restart the process when it dies.  In order to prevent a tight loop of failure and restart,
    # we delay process exit a short while no matter how we're going down.
    sleep 0; # TBD change to 5 later
}


# -------------------------------------------------------------
sub terminate_rest_api 
{
    ## Release our handle to the REST API (if we used it), to force the REST API to call its destructor.
    ## This will attempt to log out before Perl's global destruction pass wipes out resources needed for
    ## logout to work properly.
    $rest_api = undef;
}


# ----------------------------------------------------------------------------------------------------------------------
sub load_noma_yaml
{
    # Tries to load the noma yaml
    # Args
    #   - ref to noma yaml data structure
    #   - noma yaml file
    # Returns
    #   - populated (or not) noma yaml data structure
    #   - 1 on success, 0 otherwise 

    my ( $noma_yaml_ref, $noma_yaml, $tix_system_name ) = @_;

    eval { 
        $noma_yaml_ref = YAML::Syck::LoadFile( $noma_yaml );
    };

    if ( $@ ) { 
        chomp $@;
        $logger->error("Failed to load YAML file $noma_yaml : $@");
        return 0;
    }

    return 1;
}

# ----------------------------------------------------------------------------------------------------------------------
sub get_notification_details
{
    # Tries to determine whether the incoming notification is a host or service type
    # Args
    #   - ref to the to-be populated details data structure
    # Returns
    #   - populated (or not) notification type
    #   - 1 on success, 0 otherwise 
    # TBD 
    #   - make this work with real noma input ie :
    #       - read in @ARGV and parse it out into the props below

    my ( $ref_notification_details ) = @_;

    # initialize it to empty
    %{$ref_notification_details} = ( );

    # HOST
    # Expected Nagios notification command : 
    #     alert_via_noma.pl -c h -s "$HOSTSTATE$" -H "$HOSTNAME$"  -G "$HOSTGROUPNAMES$" -n "$NOTIFICATIONTYPE$" -i "$HOSTADDRESS$" -o "$HOSTOUTPUT$" -t "$TIMET$" -u "$HOSTNOTIFICATIONID$" -A "$NOTIFICATIONAUTHORALIAS$" -C "$NOTIFICATIONCOMMENT$" -R "$NOTIFICATIONRECIPIENTS$"
    # Processes like this through NoMa :
    #     '', from
    #     'nagios@localhost', to
    #     'h', type
    #     '1440238857', time
    #     'DOWN', state
    #     'CUSTOM', notification type
    #     'HOSTNAME', hostname
    #     '', host alias
    #     'HOSTADDRESS', address
    #     '1440238857907', incident id
    #     'NOTIFICATIONAUTHORALIAS',
    #     'NOTIFICATIONCOMMENT',
    #     'HOSTOUTPUT '

    # SERVICE
    # Expected Nagios notification command : 
    #     alert_via_noma.pl -c s -s "$SERVICESTATE$" -H "$HOSTNAME$" -G "$HOSTGROUPNAMES$" -E "$SERVICEGROUPNAMES$" -S "$SERVICEDESC$" -o "$SERVICEOUTPUT$" -n "$NOTIFICATIONTYPE$" -a "$HOSTALIAS$" -i "$HOSTADDRESS$" -t "$TIMET$" -u "$SERVICENOTIFICATIONID$" -A "$NOTIFICATIONAUTHORALIAS$" -C "$NOTIFICATIONCOMMENT$" -R "$NOTIFICATIONRECIPIENTS$"
    # Processes like this through NoMa :
    #     '', from
    #     'nagios@localhost', to
    #     's', type
    #     '1440239586', time
    #     'OK', state
    #     'PROBLEM', tye
    #     'HOSTNAME', hostname
    #     'HOSTALIAS', hosta alias
    #     'HOSTADDRESS', address
    #     '144023958520286', indicent id
    #     'NOTIFICATIONAUTHORALIAS',
    #     'NOTIFICATIONCOMMENT',
    #     'SERVICEOUTPUT',
    #     'SERVICEDESC'

    # expect 13 or 14 entries in ARGV - error otherwise 
    my $num_args = scalar @ARGV;
    # Not sure under which circumstances this happens - NoMa won't pass along anything if it has problems processing it's input
    # so I think NoMa would have to be broken. Worth checking here tho.
    if ( $ARGV[0] ne '-dev' ) { 
        if ( $num_args != 13 and $num_args != 14 ) {
            $logger->error("Expected 13 or 14 arguments from NoMa - received $num_args"); 
            return 0;
        }
    }

    #$logger->info("ARGS in order : \n" . Dumper \@ARGV);
    
    $ref_notification_details->{ from }                    = $ARGV[0];   # notification from ( not sure what sets this yet )
    $ref_notification_details->{ to   }                    = $ARGV[1];   # notification to
    $ref_notification_details->{ nomanotificationtype }    = $ARGV[2];   # s for service, h for host
    $ref_notification_details->{ datetime }                = $ARGV[3];   # timestamp when processed by noma I think
    $ref_notification_details->{ state }                   = $ARGV[4];   # host or service state
    $ref_notification_details->{ notificationtype }        = $ARGV[5];   # eg nagios notification type, eg PROBLEM, RECOVERY, etc
    $ref_notification_details->{ hostname }                = $ARGV[6];   # host name
    $ref_notification_details->{ hostalias }               = $ARGV[7];   # host alias
    $ref_notification_details->{ hostaddress }             = $ARGV[8];   # host address
    $ref_notification_details->{ notificationid }          = $ARGV[9];   # host or service notification id from nagios
    $ref_notification_details->{ notificationauthoralias } = $ARGV[10];  # from nagios macro
    $ref_notification_details->{ notificationcomment }     = $ARGV[11];  # from nagios macro
    $ref_notification_details->{ statusmessage }           = $ARGV[12];  # this is either host or service output
    chomp $ref_notification_details->{ statusmessage } if $ref_notification_details->{ statusmessage }; # strip trailing newlines from this here
    if ( defined $ref_notification_details->{ nomanotificationtype } and $ref_notification_details->{ nomanotificationtype } eq 's' ) {  # if service notification, then grab the service name too
        $ref_notification_details->{ servicename } = $ARGV[13];  
    }

    return 1;
}

# ----------------------------------------------------------------------------------------------------------------------
sub initialize_GW_REST_api
{
    # initializes a GW REST api object 

    my ( %RAPID_options, %config ); 

    # TBD decide where %config gets its input from - noma config (fast) or TypedConfig - for now hard coded
    $config{api_timeout} = 30;
    $config{ws_client_config_file} = '/usr/local/groundwork/config/ws_client.properties';
    $config{RAPID_710_plus} = 1;   # this will enable force_crl_check to true in RAPID
    $config{RAPID_debug} = 0;

	# Prepare GW::RAPID options.
    $RAPID_options{logger}  = $logger if $config{RAPID_debug};
	$RAPID_options{timeout} = $config{api_timeout};
	$RAPID_options{access}  = $config{ws_client_config_file};

    # v 0.4.0 : If the version of RAPID is >= 0.7.6, then it's ok to add the force_crl_check option
    if ( $config{RAPID_710_plus} ) {
	    $RAPID_options{force_crl_check} = $config{force_crl_check};
    }

	# Establish a REST API connection and object.
	$rest_api = GW::RAPID->new( undef, undef, undef, undef, $tix_system_name, \%RAPID_options );
	if ( not $rest_api ) {  
        $logger->error("ERROR - Failed to initialize Groundwork REST API:\n    $@"); 
        return 0;
    }

    return 1;
}

# ----------------------------------------------------------------------------------------------------------------------
sub property_type_exists
{
    # Takes a property name and checks if one of that type exists
    # Args
    #   - prop name/type to check existence for
    # Returns
    #   - 1 if found , 0 otherwise

    my ( $prop ) = @_;

    $logger->debug("Getting property type '$prop'");
    $rest_api->get_propertytypes( [ $prop ], {}, \%outcome, \%results ) ;

	if ( defined $outcome{response_code} ) { 
        if ( $outcome{response_code} == '404' ) { 
            $logger->debug("Property type '$prop' doesn't exist");
            return 0;
        }
        else {
	        $logger->error( "Something went wrong getting property types: " . Dumper \%outcome, \%results );
	        return 0;
	    }
    }

    # double check it exists, and if some logic was missed, we'll catch that here
    if ( exists $results{$prop} ) { 
        $logger->debug("Property type '$prop' exists");
        return 1;
    }
    else {
        $logger->debug("Property type '$prop' does not exist");
        return 0;
    }

}

# ----------------------------------------------------------------------------------------------------------------------
sub upsert_property_type
{
    # Takes a property name and tries to create that property type 
    # Args
    #   - prop name/type to upsert
    # Returns
    #   - 1 if found , 0 otherwise

    my ( $prop ) = @_;

    # For now, all prop types will be created like this below. Get fancy later if necessary.
    my @pt = (
                {
                    'name' => $prop,
                    'dataType' => 'STRING',
                    'description' => "A property added by $tix_system_name",
                },
    );

    $logger->debug("Upserting property type '$prop'");
    if ( not $rest_api->upsert_propertytypes( \@pt, {}, \%outcome, \@results ) ) { 
        $logger->error("Failed to upsert property type '$prop' : " . Dumper \%outcome, \@results);
        return 0;
    }

    return 1;

}
    
# ----------------------------------------------------------------------------------------------------------------------
sub report_problem
{
    # WIP - lot of this is Feeder stuff - initialization etc

    # Used for reporting problems in a visible way
    # Takes a host, service, message, state 
    # Updates the service message and state on that host
    # and creates an event on that host and service
    # Args
    #   - host, service, state, message
    # Returns
    #   - nothing. If this fails, hopefully it will be at least logged.

    my ( $host, $service, $message, $state ) = @_;

    my ( @service_update ) ;

	my $now = strftime( '%Y-%m-%dT%H:%M:%S%z', localtime );
	@service_update = (
            	        {
                            # TBD rethink all of this routine - use Feeder instead ? could then initialize, etc
			                hostName      => $host,
			                description   => $service,
			                monitorStatus => $state,
			                lastCheckTime => $now,
			                properties    => { 'LastStateChange' => $now, 'LastPluginOutput' => $message },
	                        #'stateType'       => 'HARD',
	                        'monitorServer'   => 'localhost',
	                        'lastStateChange' => '2013-05-22T09:36:47-07:00',
	                        'appType'         => 'NAGIOS',
		                }
    );
    

}

# ----------------------------------------------------------------------------------------------------------------------
sub generate_audit_event
{
    # Generates a GW AUDIT event for a notification and ticket
    my ( $notification_ref, $event_message ) = @_;

    $logger->debug("Generating GroundWork AUDIT event for ticket event");

    my $now = strftime( '%Y-%m-%dT%H:%M:%S%z', localtime );
    
    # create default audit - this works for host events already
    my @audit_event = ( 
                        {
		                    'appType'           => 'AUDIT',
		                    'host'              => $notification_ref->{hostname},
		                    'device'            => $notification_ref->{hostname},
		                    'monitorStatus'     => 'UP', # might want to change this later - will see
		                    'severity'          => 'OK', # might want to change this later - will see
		                    'textMessage'       => $event_message, # "Created footprints ticket '$ticket_number'",
                            'reportDate'        => $now,
		                    'firstInsertDate'   => $now,
                        }
    );

    # If the event is for a service, add in the servicename
    if ( $notification_ref->{nomanotificationtype} eq 's' ) {
        $audit_event[0]{ service } = $notification_ref->{servicename};   
    }

    # internal check just in case
    elsif ( $notification_ref->{nomanotificationtype} !~ /^(s|h)$/ ) {
        $logger->error("Cannot generate AUDIT event - unrecognized NoMa notification type - expected 's', or 'h', got '$notification_ref->{nomanotificationtype}'");
        return 0;
    }

    # create the event
	if ( not $rest_api->create_events( \@audit_event, {}, \%outcome, \@results ) ) {
        $logger->error( "Something went wrong AUDIT event: " . Dumper \%outcome, \@results );
        return 0;
    }

    return 1;



}



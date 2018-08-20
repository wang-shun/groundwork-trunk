package GW::Feeder;
# GW::Feeder - GroundWork Feeder module 
#
# Copyright 2013-2014 GroundWork Open Source, Inc. (GroundWork).
# All rights reserved. This program is free software; you can redistribute
# it and/or modify it under the terms of the GNU General Public License
# version 2 as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
#
# Original author: Dominic Nicholas
#
# Revision history:
#
# 2014-01-31 Dominic Nicholas    0.1.0    Original version.
# 2014-05-14 Dominic Nicholas    0.2.2    GWMON-11628 : Send notifications on creation/deletion of hosts and services
# 2014-05-14 Dominic Nicholas    0.2.3    GWMON-11617 : Provide an option to not have the feeder update the host status
# 2014-05-14 Dominic Nicholas    0.2.4    GWMON-11605 : Create pending and initial host/service states on host/service creation
# 2014-05-14 Dominic Nicholas    0.2.5    GWMON-11605 and then some : added firstInsertDate to all events when they are created, 
#                                         and made pending state events preceed initial state events by 1 sec to make status graphs render correctly
# 2014-05-14 Dominic Nicholas    0.2.6    Minor fix to auditing routine - only send notifications if post_notifications = yes
# 2014-05-14 Dominic Nicholas    0.2.7    Minor fix to flush_audit so it doesn't run audit flush if auditing = false
# 2014-08-14 Dominic Nicholas    0.2.8    Minor fix to set monitorServer always to be 'localhost' to avoid status viewer crashing
# 2014-08-21 GH                  0.3.0    Use ws_client.properties to find GW::RAPID endpoint and credentials;
#                                         make config-file updating a clean atomic operation.
# 2014-08-22 GH                  0.3.1    Except within terminate_feeder(), this level of code should never directly exit().
# 2015-03    DN                  0.3.1.1  Mods for scom feeder under the 702 dev branch. These mods are for lastStateChange for host/service 'since' stuff
# 2015-03    DN                  0.3.1.2  Fix to DESTROY to prevent confusing log4perl messages and termination
# 2015-04    DN                  0.3.1.3  Updates for handling lastStateChange properly in various places

# NOTES
# - this is a first version of an attempt to create a feeder utility library based on the GW::RAPID library
# - it is developed out of the requirements from the cacti feeder and is a starting point for development
#   for other feeders in the future.

# KNOWN PROBLEMS
# - if a host or service in Foundation has a : in it, audit trail will probably fail - see 'TBD API FIX' 

# TBD
# - As with any software, there is always a lot TBD. That complete list is not documented here yet.
# - when REST API supports it, add call to see if a license is installed, and set a license_installed property = 1/0
# - Doc up what each sub expects in pod format etc
# - Document and comment and review logging and report_feeder_error's and comments
# - A lot more validation of parameters etc

use warnings;
use strict;
use attributes;

our ( @ISA, @EXPORT, @EXPORT_OK, %EXPORT_TAGS, $VERSION );

BEGIN {
    use Exporter ();
    @ISA         = qw(Exporter);
    @EXPORT      = qw( get_current_time perl_script_process_count );
    @EXPORT_OK   = qw();
    %EXPORT_TAGS = ( DEBUG => [ @EXPORT, @EXPORT_OK ] );
    $VERSION     = "0.3.1.3";
}


# ================================ Modules ================================
use Data::Dumper;    
$Data::Dumper::Indent   = 1;
$Data::Dumper::Sortkeys = 1;
use POSIX qw(strftime);  # For time formatting 
use DateTime::Format::Strptime; # For time manipulation
use TypedConfig qw();    # For reading in the feeder properties file
use Log::Log4perl;       # For logging
use List::MoreUtils qw(any uniq notall);
use Sys::Hostname;
my  $sql_time_format = '%Y-%m-%dT%H:%M:%S%z';

# ---------------------------------------------------------------------------- 
sub new 
{
    # Creates a new feeder object.
    # E.g., my $feeder = GW::Feeder->new( $feeder_name, $configuration_file, \%feeder_options ) ;

    my $invocant           = $_[0];
    my $feeder_name        = $_[1];
    my $configuration_file = $_[2];
    my $options_ref        = $_[3];
    my $class              = ref( $invocant ) || $invocant;    # object or class name
    my $self               = undef;
    my ( %config, $feeder_TypedConfig_object, %RAPID_options );

    eval {

        my $logger = ( defined($options_ref) ? $options_ref->{logger} : undef) || Log::Log4perl::get_logger("GW.RAPID.module");
        $config{logger} = $logger; 
        $config{feeder_name} = $feeder_name;
        $config{feeder_host} = ( split( m{\.}xms, hostname() ) )[0]; # use simple hostname, rather than possible fqdn
            
        my %valid_options = ( logger => 'logger handle', feeder_specific_properties => 'hash', api_options => 'hash' );

        if ( defined $options_ref ) {

            if ( attributes::reftype($options_ref) ne 'HASH' ) {
                $logger->logcroak("ERROR Invalid feeder options hash.");
            }

            foreach my $key ( keys %$options_ref ) {
                if ( not exists $valid_options{$key} ) {
                    $logger->logcroak("ERROR Invalid feeder option '$key'.");
                }
                if ( $valid_options{$key} eq 'integer' ) {
                    if ( $options_ref->{$key} !~ /^\d+$/ ) {
                        $logger->logcroak("ERROR Invalid feeder option '$key': not an integer.");
                    }
                }
                elsif ( $valid_options{$key} eq 'logger handle' ) {
                    ## FIX MINOR:  Allow more flexibility in what is accepted as a logger handle.
                    ## Perhaps a simple open file handle would do the job, if we modify the rest of
                    ## this package to deal with such an object appropriately.
                    if ( ref $options_ref->{$key} ne 'Log::Log4perl::Logger' ) {
                        # $logger->logcroak("ERROR Invalid feeder option '$key': not a handle.");
                        # If invalid logger handle, don't try to write to it :) Instead print an error and croak and let the eval catch that.
                        $logger->logcroak( "ERROR Invalid feeder option '$key': Expecting type to be Log::Log4perl::Logger" );
                    }
                }
                elsif ( $valid_options{$key} eq 'hash' ) {
                    if ( ref $options_ref->{$key} ne 'HASH' ) {
                        $logger->logcroak("ERROR Invalid feeder option '$key': Expected type to be hash.");
                    }
               }
            }
        }

        
        # Read the properties from the config file into the config associated with this feeder object.
        eval {
            $feeder_TypedConfig_object = TypedConfig->new( $configuration_file );

            # get properties common to all feeders - these are all required regardless of which feeder
            $config{RAPID_debug}                   = $feeder_TypedConfig_object->get_boolean ('RAPID_debug');
            $config{api_timeout}                   = $feeder_TypedConfig_object->get_number  ('api_timeout');
            $config{ws_client_config_file}         = $feeder_TypedConfig_object->get_scalar  ('ws_client_config_file');
            $config{app_type}                      = $feeder_TypedConfig_object->get_scalar  ('app_type');
            $config{auditing}                      = $feeder_TypedConfig_object->get_boolean ('auditing');
            #$config{constrain_to_service_states}   = $feeder_TypedConfig_object->get_hash    ('constrain_to_service_states') ; # This functionality is removed for now.
            $config{cycle_timings}                 = $feeder_TypedConfig_object->get_number  ('cycle_timings');
            $config{enable_processing}             = $feeder_TypedConfig_object->get_boolean ('enable_processing');
            $config{events_bundle_size}            = $feeder_TypedConfig_object->get_number  ('events_bundle_size');
            $config{feeder_services}               = $feeder_TypedConfig_object->get_hash    ('feeder_services') ;
            $config{guid}                          = $feeder_TypedConfig_object->get_scalar  ('guid');
            $config{health_hostgroup}              = $feeder_TypedConfig_object->get_scalar  ('health_hostgroup');
            $config{health_hostname}               = $feeder_TypedConfig_object->get_scalar  ('health_hostname');
            $config{license_check}                 = $feeder_TypedConfig_object->get_scalar  ('license_check');
            $config{license_check_user}            = $feeder_TypedConfig_object->get_scalar  ('license_check_user');
            $config{monitoring_server}             = $feeder_TypedConfig_object->get_scalar  ('monitoring_server');
            $config{notifications_bundle_size}     = $feeder_TypedConfig_object->get_number  ('notifications_bundle_size');
            $config{post_events}                   = $feeder_TypedConfig_object->get_boolean ('post_events');
            $config{post_notifications}            = $feeder_TypedConfig_object->get_boolean ('post_notifications');
            $config{update_hosts_statuses}         = $feeder_TypedConfig_object->get_boolean ('update_hosts_statuses');
            # Do some sanity checking in here of various required config. 
            # TBD improve this area.
                
            # Need to handle this cleanly. perhaps there's an even better way to avoid the Perl hash errors if the hash is not present.
            # This functionality is removed for now.
           #if ( not defined $config{constrain_to_service_states} ) { 
           #    $logger->logcroak("FATAL: Cannot read config file - property constrain_to_service_states is no defined");
           #}
           #else { 
           #    $config{constrain_to_service_states}   =  { $feeder_TypedConfig_object->get_hash('constrain_to_service_states') };
           #}
           ## Validation of states : OK, UNSCHEDULED CRITICAL are only permissable ones for now
           ## This is designed for possible future expansion of allowed states
           #if ( any { $_ !~ /^(OK|UNSCHEDULED CRITICAL)$/i } keys %{$config{constrain_to_service_states} } ) { 
           #    $logger->logcroak("FATAL: Cannot read config file - illegal property name in constrain_to_service_states");
           #}
           ## Convert the keys to lowercase for use later
           #%{ $config{constrain_to_service_states} } = map { lc $_ => $config{constrain_to_service_states}{$_} } keys %{ $config{constrain_to_service_states} };
           %{ $config{constrain_to_service_states} } = (); # just leave it set but empty for now

            # If feeder_services given, then need to get it as a hash 
            if ( defined $config{feeder_services} ) {
                $config{feeder_services} = { $feeder_TypedConfig_object->get_hash('feeder_services')  };
            }

            # Get optional properties are feeder-specific. 
            if ( defined $options_ref->{feeder_specific_properties} ) {
                foreach my $feeder_specific_property ( keys %{ $options_ref->{feeder_specific_properties} } ) {
                    if ( $options_ref->{feeder_specific_properties}->{ $feeder_specific_property } eq 'scalar' ) {
                            $config{ $feeder_specific_property } = $feeder_TypedConfig_object->get_scalar($feeder_specific_property);
                    }
                    elsif ( $options_ref->{feeder_specific_properties}->{ $feeder_specific_property } eq 'number' ) {
                            $config{ $feeder_specific_property } = $feeder_TypedConfig_object->get_number($feeder_specific_property);
                    }
                    elsif ( $options_ref->{feeder_specific_properties}->{ $feeder_specific_property } eq 'boolean' ) {
                            $config{ $feeder_specific_property } = $feeder_TypedConfig_object->get_boolean($feeder_specific_property);
                    }
                    elsif ( $options_ref->{feeder_specific_properties}->{ $feeder_specific_property } eq 'hash' ) {
                            if ( defined $feeder_TypedConfig_object->get_hash($feeder_specific_property) ) { 
                                $config{ $feeder_specific_property } = { $feeder_TypedConfig_object->get_hash($feeder_specific_property) };
                            }
                    }
                    elsif ( $options_ref->{feeder_specific_properties}->{ $feeder_specific_property } eq 'array' ) {
                            $config{ $feeder_specific_property } = [ $feeder_TypedConfig_object->get_array($feeder_specific_property) ];
                    }
                    else {
                        $logger->logcroak("FATAL: unsupported option type '$options_ref->{feeder_specific_properties}->{ $feeder_specific_property }' for option '$feeder_specific_property'");
                    }
                }
            }
            
        };
        if ($@) {
            chomp $@;
            $@ =~ s/^ERROR:\s+//i;
            $logger->logcroak("FATAL Cannot read config file $configuration_file: $@");
        }

        # Make all of the properties available to the feeder object
        $config{properties} = $feeder_TypedConfig_object;

        # Prepare GW::RAPID options.
        %RAPID_options = ();
        $RAPID_options{timeout} = $config{api_timeout};
        $RAPID_options{logger}  = $logger if $config{RAPID_debug};
        $RAPID_options{access}  = $config{ws_client_config_file};

        # Establish a REST API connection and object.
        $config{rest_api} = GW::RAPID->new( undef, undef, undef, undef, $feeder_name, \%RAPID_options );
        if ( not $config{rest_api} ) { $logger->logdie("Terminating ERROR - Failed to initialize Groundwork REST API: $@"); }

        # If auditing is on, create an auditing hash.
        # See sub flush_audit for details on the audit_trail data structure
        if ( $config{auditing} ) {
            $config{audit_trail} = { } ;
        }

        # Associate all of the data with the feeder class object
        $self = bless \%config, $class;

        # Set the feeder's guid property if it's still 'undefined'.
        if ( $config{guid} eq 'undefined' ) {
            if ( not $self->set_guid_property($configuration_file) ) {
                $config{rest_api} = undef;
                $logger->logdie("Terminating ERROR - Could not set the guid property.");
            }
        }
        else {
            ## This is critical info, so it doesn't hurt to save an extra copy
            ## in the log file in case it ever gets destroyed in the config file.
            $logger->debug("DEBUG  guid property is '$config{guid}'");
        }

        # Check that the application type in app_type, build it if not.
        # FIX MINOR:  Why are there two successive calls here?
        $self->create_app_type();
        if ( not $self->create_app_type() )  { 
            $config{rest_api} = undef;
            $logger->logdie("Terminating ERROR - Could not create/update application type."); 
        }

        $logger->debug("DEBUG $feeder_name feeder object initialized.");

    };

    # Check for errors during the mega eval above
    if ($@) {
        return undef;
    }

    # All ok so return the blessed feeder object. Amen.
    return $self;
}

# ---------------------------------------------------------------------------- 
sub DESTROY 
{
    # TBD anything else required on feeder object destroy ? 
    my $self = $_[0];

    # Using logger in DESTROY leads to confusing error messages - see http://log4perl.sourceforge.net/releases/Log-Log4perl/docs/html/Log/Log4perl/FAQ.html#eb5cc
  # $self->{logger}->debug("DEBUG  Feeder object destroyed..") if $self->{logger};

    # Release our handle to the REST API, if it hasn't already been released via some other means.
    $self->{rest_api} = undef if exists $self->{rest_api};
    return 1;
}

# ------------------------------------------------------------------------------------------------------------------------
sub create_app_type
{
    # Upserts the app type. Returns 1 on success, 0 otherwise
    # About as expensive to check existence as it is to create it so just do one upsert call
    my ( $this ) = @_;
    my ( @application_types, %outcome, @results ) ;

    @application_types = (
                            {
	                            'name'                    => $this->{app_type},
	                            'description'             => "Feeder $this->{feeder_name} application type",
	                            'stateTransitionCriteria' => 'Device;Host',
                            }
    );

    # Create the feeder application type
    if ( not $this->{rest_api}->upsert_application_types( \@application_types, {}, \%outcome, \@results ) ) {
        $this->{logger}->error( "ERROR Something went wrong upserting application type $this->{app_type} : " . Dumper \%outcome, \@results );
        return 0;
    }

    return 1;
}

# ------------------------------------------------------------------------------------------------------------------------
sub set_guid_property
{
    # Sets the guid property in the feeder config file.

    my ( $this, $configuration_file ) = @_;
    my ( @config, $line, $ug, $uuid ) ;

    # Read in the config file
    open (CONF, $configuration_file) or do { 
        $this->{logger}->error("ERROR set_guid_property() could not open the config file '$configuration_file' for reading: $!");
        return 0;
    };
    @config = <CONF>;    
    close CONF;  

    # Open a temporary config file for writing.  We do this so as not to utterly destroy the
    # old copy if we get interrupted before we're done constructing the updated copy.
    my $temp_config_file = "$configuration_file.new";
    open (CONFOUT, ">", $temp_config_file) or do {
        $this->{logger}->error("ERROR set_guid_property() could not open the config file '$temp_config_file' for writing: $!");
        return 0;
    };

    my ($dev, $ino, $mode, $nlink, $uid, $gid) = stat $configuration_file;
    if (not defined $mode) {
        $this->{logger}->error("ERROR set_guid_property() could not get the mode of the config file '$configuration_file': $!");
        return 0;
    }

    # Kill any filetype info, and further restrict the permissions to disallow any pointless
    # set-id/sticky or execute permissions and any group-write or other-write permissions.
    $mode &= 0644;

    # Set the mode of the new file to the mode of the old file, perhaps sensibly restricted.
    unless ( chmod( $mode, $temp_config_file ) ) {
        $this->{logger}->error("ERROR set_guid_property() could not set the mode of the config file '$temp_config_file': $!");
        return 0;
    }

    # Set the ownership of the new file to that of the old file.  This should effectively be a
    # no-op, because we should be running as the owner of the old file.  But if this fails, then
    # we know we cannot put the config file back as it was without change, so we abort.
    unless (chown $uid, $gid, $temp_config_file) {
        $this->{logger}->error("ERROR set_guid_property() could not set the ownership of the config file '$temp_config_file': $!");
        return 0;
    }

    # Find the guid line and update its value.
    foreach $line (@config)  
    {    
        if ( $line =~ m{ ^\s*guid\s*=.*$  }xms ) {
            use APR::UUID (); 
            $uuid = APR::UUID->new->format; # APR::UUID is already part of stock GW 7.0.2, so use instead of Data::UUID
            $line = "guid = $uuid\n";
            $this->{logger}->info("INFO  Setting guid property to '$uuid'");
        }
        # TBD perhaps use IO::File instead
        unless ( print CONFOUT $line ) {
            $this->{logger}->error("ERROR set_guid_property() could not write to the config file '$temp_config_file': $!");
            return 0;
        }
    }    

    # A close() may flush Perl's I/O buffers, so writing can occur here, too,
    # and the success of this operation must be checked as well.
    unless ( close CONFOUT ) {
        $this->{logger}->error("ERROR set_guid_property() could not write to the config file '$temp_config_file': $!");
        return 0;
    }

    # Perform an atomic rename of the new config file.  By standard UNIX rename semantics,
    # the end result is that you either get the entire new file or the entire old file at
    # the name of the old file, depending on whether or not the rename succeeded.  But you
    # never can get any partial file as a result.  This provide essential safety.
    unless ( rename( $temp_config_file, $configuration_file ) ) {
        $this->{logger}->error("ERROR set_guid_property() could not rename the updated config file '$temp_config_file': $!");
        return 0;
    }

    # Update feeder object guid property now that it's been updated here.
    $this->{properties}->{guid} = $uuid;
    $this->{guid} = $uuid;

    return 1;
}

# ----------------------------------------------------------------------------------------------------------------
sub feeder_get_hostgroups
{
    # Takes a ref to a hash of hostnames : { hostname=>undef, hostname2=>undef ... }
    # and for each hostname, populates it with a hash of hostgroups to which the host belongs 
    # in Foundation : { hostname => {hg1, hg2}, hostname2 => {hg3, hg4} ... }
    # returns 1 success 0 otherwise

    my ( $this, $hashref_hostnames ) = @_;

    my ( %outcome, %results, @hostgroups, $hostgroup_ref, $arrayref_hosts, $hostgroup_name ) ;

    # Get all hostgroups in detail all in one go - TBD chunk this up into smaller bits
    if ( not $this->{rest_api}->get_hostgroups( [ ] , { depth=>'shallow'}, \%outcome, \%results ) ) {
        if ( defined $outcome{response_code} and $outcome{response_code} ne '404' ) { # zero hostgroups - expect a 404 
            $this->{logger}->error( "ERROR Something went wrong getting hostgroups : " . Dumper \%outcome, \%results );
            return 0;
        }
    }
        
    foreach $hostgroup_ref ( keys %results ) {
        $hostgroup_name = $results{$hostgroup_ref}->{name};
        if ( defined $results{$hostgroup_ref}->{hosts}  ) { # possible that a hostgroup is empty
            foreach $arrayref_hosts ( @{ $results{$hostgroup_ref}->{hosts}} ) {
                 if ( exists $hashref_hostnames->{  $arrayref_hosts->{hostName} } ) { 
                    $hashref_hostnames->{  $arrayref_hosts->{hostName} } { $hostgroup_name } = 1;
                 }  
            }
       }
    }

    return 1;

}

# ----------------------------------------------------------------------------------------------------------------
sub feeder_post_notifications
{
    # Takes a reference to an array of REST API consumable notification data property hashes suitable for either
    # host or service notifications, and a type (either host or service)
    # and does a bundled sending of them ie this wrapped provides bundling 
    # TBD smarter way to do this if time - in the %types hash, store the function names - can then dry up the if $type eq logic

    my ( $this, $type, $arrayref_notifications ) = @_;
    my ( @notifications_bundle, %outcome, @results, @built_notifications, $not, %built_notification, $now, $tz );
    my  %types = ( host => 1, service => 1 );

    if ( not exists $types{$type} ) {
        $this->{logger}->error( "ERROR feeder_post_notifications() bailing due to invalid type '$type', expected " . join ",", keys %types );
        return 0;
    }

    while ( @notifications_bundle = splice @{$arrayref_notifications}, 0, $this->{notifications_bundle_size}) { 
        $this->{logger}->debug( "DEBUG feeder_post_notifications() starting to process bundle of " . ($#notifications_bundle + 1 ) . " notification(s)" );
        # Validate and build notifications structure for API
        @built_notifications = ();
        foreach $not ( @notifications_bundle ) {
            if ( $type eq 'host' ) {
                if ( any { not defined $_ } $not->{hostName}, $not->{notificationType}, $not->{hostState}, $not->{hostOutput} ) { 
                    $this->{logger}->error("ERROR missing expected host notification field - skipping this notification object : " . Dumper $not ) ;
                    next;
                }
                # Build required fields - straight copy 
                %built_notification = %$not;
            }
            if ( $type eq 'service' ) {
                if ( any { not defined $_ } $not->{hostName}, $not->{notificationType}, $not->{serviceDescription}, $not->{serviceOutput}, $not->{serviceState} ) {
                    $this->{logger}->error("ERROR missing expected service notification field - skipping this notification object : " . Dumper $not ) ;
                    next;
                }
                # Build required fields - straight copy 
                %built_notification = %$not;
            }

            push @built_notifications, { %built_notification };
        }

        # Create the host notifications
        if ( $type eq 'host' ) {
            if ( not $this->{rest_api}->create_noma_host_notifications(\@built_notifications, {}, \%outcome, \@results) ) {
                $this->{logger}->error( "ERROR something went wrong creating host notifications : " . Dumper \%outcome, \@results );
                return 0;
            }
        }

        # Create the service notifications
        if ( $type eq 'service' ) {
            if ( not $this->{rest_api}->create_noma_service_notifications(\@built_notifications, {}, \%outcome, \@results) ) {
                $this->{logger}->error( "ERROR something went wrong creating service notifications : " . Dumper \%outcome, \@results );
                return 0;
            }
        }

    }

    return 1;
    
}

# ----------------------------------------------------------------------------------------------------------------
sub feeder_post_events
{
    # Takes a reference to an array of REST API consumable event data property hashes suitable for either
    # posting host or service events, and a type (either host or service)
    # and does a bundled posting of them ie this wrapper provides bundling 
    # TBD smarter way to do this if time - in the %types hash, store the function names - can then DRY up the if $type eq logic

    my ( $this, $type, $arrayref_events ) = @_;
    my ( @events_bundle, %outcome, @results, @built_events, $event, %built_event, $now, $tz );
    my  %types = ( host => 1, service => 1 );

    if ( not exists $types{$type} ) {
        $this->{logger}->error( "ERROR feeder_post_events() bailing due to invalid type '$type', expected " . join ",", keys %types );
        return 0;
    }

    while ( @events_bundle = splice @{$arrayref_events}, 0, $this->{events_bundle_size}) { 
        $this->{logger}->debug( "DEBUG feeder_post_events() starting to process bundle of " . ($#events_bundle + 1 ) . " event(s)" );

        @built_events = ( );

        foreach $event ( @events_bundle ) {
            if ( $type eq 'host' ) {
                if ( any { not defined $_ } $event->{device}, $event->{host}, $event->{monitorStatus}, $event->{severity}, $event->{textMessage} ) { 
                    $this->{logger}->error("ERROR missing expected host event field - skipping this event object : " . Dumper $event ) ;
                    next;
                }
                # Build required fields - straight copy 
                %built_event = %$event;
            }
            if ( $type eq 'service' ) {
                if ( any { not defined $_ } $event->{device}, $event->{host}, $event->{monitorStatus}, $event->{severity}, $event->{textMessage}, $event->{service} ) { 
                    $this->{logger}->error("ERROR missing expected host service field - skipping this event object : " . Dumper $event ) ;
                    next;
                }
                # Build required fields - straight copy 
                %built_event = %$event;
            }

            # reportDate : if not supplied, create it
            if ( not defined $event->{reportDate} ) {
                $now = strftime( '%Y-%m-%dT%H:%M:%S', localtime );
                $tz  = strftime("%z", localtime );
                $built_event{reportDate} = "$now$tz";
            }
            # firstInsertDate is also required (contrary to REST API doc)
            if ( not defined $event->{firstInsertDate} ) {
                $built_event{firstInsertDate} = $built_event{reportDate};
            }
            # appType: add it if not supplied
            if ( not defined $event->{appType} ) {
               $built_event{appType} =  $this->{properties}->{app_type};
            }
            push @built_events, { %built_event } ;
        }

        # Create the host events
        if ( $type eq 'host' ) {
            if ( not $this->{rest_api}->create_events( \@built_events, {}, \%outcome, \@results ) ) {
                $this->{logger}->error( "ERROR something went wrong creating host events : " . Dumper \%outcome, \@results );
                return 0;
            }
        }

        # Create the service events
        if ( $type eq 'service' ) {
            if ( not $this->{rest_api}->create_events( \@built_events, {}, \%outcome, \@results ) ) {
                $this->{logger}->error( "ERROR something went wrong creating service events : " . Dumper \%outcome, \@results );
                return 0;
            }
        }
    }

    return 1;
    
}

# ----------------------------------------------------------------------------------------------------------------
sub feeder_upsert_hosts
{
    # Create and/or update a set of hosts. 
    # Supports audit trail.
    # Takes a ref to an array of host hashes, and a ref to an options hash.
    # Returns 1 on success, 0 otherwise.

    my ( $this, $hosts_ref, $options_ref ) = @_;
    my ( $host_ref, $now, $tz, %built_host, @built_hosts, $host, @hosts_to_check );
    my ( @hosts_bundle, %outcome, @results, %these_hosts_exist, %these_hosts_dont_exist );
    my ( %upserted_successfully, $result_hashref );
    my ( $event_severity, @host_events, $host_event, @created_host_events, @post_these_host_events );


    # First make note of whether each host exists or not and therefore whether it will need adding.
    # This information will determine which properties to use in the upsert_hosts() call.
    # 
    # Build a list of hostnames to check existence for,
    # whilst checking for required properties that are used here and later.
    foreach $host_ref ( @{$hosts_ref} ) {
        # TBD also need to add a check for things that should NOT be passed in here too. 
        # However, its nice to allow overrides for health services internally too, so leave as-is for now.
        if ( not defined $host_ref->{hostName} and not defined $host_ref->{monitorStatus} ) { 
            $this->report_feeder_error( "ERROR expected hostName and monitorStatus in host object" . Dumper $host_ref );
            return 0;
        }
        else {
            push @hosts_to_check, $host_ref->{hostName} ;
        }
    }

    # Check existence of these hosts. This is needed in order to construct different api data structures depending upon whether a host exists or not.
    #$this->{logger}->debug("DEBUG starting feeder_upsert_hosts() hosts existence checking ....");
    if ( not $this->check_foundation_objects_existence( 'hosts', \@hosts_to_check, \%these_hosts_exist, \%these_hosts_dont_exist ) ) {
        $this->report_feeder_error( "ERROR checking Foundation hosts existence" );
        return 0;
    }
    #$this->{logger}->debug("DEBUG ending feeder_upsert_hosts() hosts existence checking ....");

    # Process each host object by 'building' it ie preparing its fields for REST API call
    foreach $host_ref ( @{$hosts_ref} ) {

        # Initialize the built host with all of the fields/values passed in, and then update them.
        # This way you keep the door open to whatever fields/values the feeder code cares to pass in.
        # Useful for future new supported REST API host fields.
        %built_host = %{$host_ref};

        # When upserting hosts, there are two cases: the host already exists, the host doesn't already exist.
        # Depending on the case, a different set of properties need to be sent to GW::RAPID/REST API upsert_hosts().
        # In the case of a host existing and having been created by Nagios (or other app) earlier, 
        # dont want to upsert the agentId because that can cause the host to be deleted entirely later based on that agentId,
        # of the description of the host to change from its original, or the agentId to be overwritten.
        # The only properties needed if upserting an existing host, are : lastCheckTime, hostName, monitorStatus
        # and monitorStatus is questionable as it will override any other method such as fping.

        # Regardless of whether host exists already or not...
        $built_host{hostName}      = $host_ref->{hostName};
        $built_host{monitorStatus} = $host_ref->{monitorStatus};
        if ( not defined $host_ref->{lastCheckTime} ) { # lastCheckTime : if not supplied, create it
            $now = strftime( '%Y-%m-%dT%H:%M:%S', localtime );
            $tz  = strftime("%z", localtime );
            $built_host{lastCheckTime} = "$now$tz";
        }
        else {
            $built_host{lastCheckTime} = $host_ref->{lastCheckTime} ;
        }

        # So, only upsert with these additional things if the host is going to be created by this upsert
        if ( not exists $these_hosts_exist{ $host_ref->{hostName} } ) {
            # Use values if they were specified, defaults if not
            $built_host{deviceDisplayName}    = ( defined $host_ref->{deviceDisplayName} )    ? $host_ref->{deviceDisplayName}    : $host_ref->{hostName};
            $built_host{deviceIdentification} = ( defined $host_ref->{deviceIdentification} ) ? $host_ref->{deviceIdentification} : $host_ref->{hostName};

            # GWMON-11737
            $built_host{monitorServer}        = 'localhost';
            $built_host{appType}              = ( defined $host_ref->{appType} )              ? $host_ref->{appType}              : $this->{properties}->{app_type};
            $built_host{agentId}              = ( defined $host_ref->{agentId} )              ? $host_ref->{agentId}              : $this->{properties}->{guid};
            $built_host{description}          = "Added by feeder $this->{feeder_name}";                # this doesn't show up in SV ..
	        $built_host{properties}           = {  # ... but this will make it show up
                                                    'Alias' => "Added by feeder $this->{feeder_name}", 
                                                    'LastStateChange' => $built_host{lastCheckTime}  # For the Up since in sv to work properly
                                                }; 

            # GWMON-11605 create a pending event and an initial state event for the to-be created host : 
            #   Event : host->pending,  Event : host->$host_ref->{monitorStatus} 
            if ( $built_host{monitorStatus} ne 'UP' ) { $event_severity = 'SERIOUS' ;  } else { $event_severity = 'OK' ; }
            push @host_events , { # Initial state event (pending will be added later)
                                       'appType'          => $built_host{appType},
                                       'device'           => $built_host{deviceIdentification},
                                       'host'             => $built_host{hostName},
                                       'monitorStatus'    => $built_host{monitorStatus},
                                       'reportDate'       => $built_host{lastCheckTime},
                                       'firstInsertDate'  => $built_host{lastCheckTime},
                                       'severity'         => $event_severity,
                                       'textMessage'      => 'Host creation initial state event'
                                };
            
            # Add the prepared host data structure to the build_hosts array that will be used in the REST API upsert_hosts call
            # GWMON-11617 : always set an initial status if the host is being created, regardless of update_hosts_statuses
            push @built_hosts, { %built_host };
        }
        else { # Host exists already
            # GWMON-11617 : if host exists already, then only update it's status if update_hosts_statuses is true
            if ( $this->{update_hosts_statuses} ) {
                $this->{logger}->debug( "DEBUG host $host_ref->{hostName} status will be updated" );


                # If the status of the host has changed, then need to add a lastStateChange prop to sthat the status viewer <state> Since data updates correctly
                my %outcome; my %results;
                if ( not $this->{rest_api}->get_hosts( [ $built_host{hostName} ] , { }, \%outcome, \%results ) ) { # get the host's state
                    $this->{logger}->error( "ERROR something went wrong getting hosts: " . Dumper \%outcome, \@results );
                    return 0;
                }
                #$this->{logger}->debug( " ---- $built_host{hostName} : --- incoming= $built_host{monitorStatus}, currently $results{$built_host{hostName}}{monitorStatus} \n"); # TBD remove
                if ( $built_host{monitorStatus} ne $results{$built_host{hostName}}{monitorStatus} ) {  # if changed, add lastStateChange prop
                    $built_host{properties}{LastStateChange} = $built_host{lastCheckTime};
                }


                push @built_hosts, { %built_host };
            }
            else {
                $this->{logger}->debug( "DEBUG update_hosts_statuses is false, host $host_ref->{hostName} status will not be updated" );
            }
        }

    };


    # Do complete batch license check here ?
    # Or move the license checking into the auditing section below and allow bundle-wise license filling ?
    # Preferable Answer : in each batch - thats closer to what the api will eventually do perhaps ? 
    # Correct answer : what the requirements spell out - ie check that adding all hosts won't exceed license
    # NOTE: check_license() figures out how many hosts would actually need adding ie total - existing = need adding
    # and return failure if exceeds license
    # Only check license if there are built_hosts
    # Note that this will change once the REST API supports license checking methods
    # Also, not very efficient currently as check_license will do an existence check all over again for these hosts.
    if ( ($#built_hosts >= 0) and (not $this->check_license( \@built_hosts ))  ) {
        $this->report_feeder_error("LICENSE ERROR - Adding " . ( $#built_hosts + 1) . " host(s) would exceed your GroundWork license limits - none of these hosts will be added");
        return 0; 
    }

    # To be efficient, will update/insert hosts in bundles, including auditing hosts existence testing
    while ( @hosts_bundle = splice @built_hosts, 0, $this->{host_bundle_size}) { 

        $this->{logger}->debug( "DEBUG feeder_upsert_hosts() starting to process bundle of " . ($#hosts_bundle + 1 ) . " host(s)" );

        @created_host_events = ();

        # If auditing, then need to check whether the hosts in this bundle exists or not prior to upserting it, 
        # so that later can then test if the host was added to Foundation anew.
        # For those hosts that do NOT exist, make a note that they will be added.
        # After the REST API call to actually try and add hosts, then check the results to see which actually were added,
        # and then update the audit trail finally. JIRA GWMON-11572 to simplify auditing.

        # Run the upsert on the hosts bundle
        if ( not $this->{rest_api}->upsert_hosts( \@hosts_bundle, $options_ref, \%outcome, \@results ) ) {
            $this->{logger}->error( "ERROR something went wrong upserting hosts: " . Dumper \%outcome, \@results );
            return 0;
        }

        # Update audit trail
        # ... or if posting events (GWMON-11605)
        if ( $this->{auditing} or $this->{post_events} ) {

            # Check that each host in the %these_hosts_dont_exist hash, was successfully upserted, and
            # if that host was one of those that didn't exist earlier, add it to the audit trail

            if ( @results ) { # ... First, @results has to be defined, else assuming that upserting completely failed

                # Add successfully upsert'd hosts to a hash ie if the result entity was tagged as success
                foreach $result_hashref ( @results ) {
                    $upserted_successfully{ $result_hashref->{entity} } = 1 if $result_hashref->{status} eq 'success';
                }
            
                # Check if host was upserted ok
                foreach $host ( keys %these_hosts_dont_exist ) {
                    if ( defined $upserted_successfully{ $host } ) { 
                        $this->{audit_trail}->{hosts}{$host}{created}{host}{added} = 1;
                        # GWMON-11605 create a set of pending and initial state events
                        # create the PENDING state event. 
                        # Note that the pending event needs to come before the initial state event
                        # so 1 second is subtracted. Could do this a different way obviously.
                        foreach $host_event ( @host_events ) {
                            if ( $host_event->{host} eq $host )  {
                                push @created_host_events, {
                                                        'appType'            => $host_event->{appType},
                                                        'device'             => $host_event->{device},
                                                        'host'               => $host_event->{host},
                                                        'monitorStatus'      => 'PENDING',
                                                        'reportDate'         => subtract_a_second( $host_event->{reportDate}, $sql_time_format ) , 
                                                        'firstInsertDate'    => subtract_a_second( $host_event->{reportDate}, $sql_time_format ) ,
                                                        'severity'           => 'OK',
                                                        'textMessage'        => 'Host creation PENDING event'
                                                   };
                                # create the initial host state event 
                                push @created_host_events, $host_event;
                            }
                        }
                    }
                }
            }
            else {
                $this->{logger}->warn( "WARN  No results from upsert_hosts API call. Outcome was : " . Dumper \%outcome );
            }

            # GWMON-11605 post any pending and initial state events
            if ( @created_host_events ) {
                push @post_these_host_events, @created_host_events; # just make one jumbo list of host events to post later
            }
        }
    }

    # GWMON-11605 post any pending and initial state events
    if ( @post_these_host_events ) { 
        #print Dumper \@post_these_host_events;
        if ( not $this->feeder_post_events( 'host', \@post_these_host_events ) ) {
            $this->report_feeder_error( "ERROR Failed to create events during upserting of hosts");
            return 0;
        }
    }

    return 1;
}

# ----------------------------------------------------------------------------------------------------------------
sub feeder_upsert_hostgroups
{
    # Create or update a set of hostgroups. The only way to update a hostgroup's members is via upsert_hostgroups.
    # 
    # Whilst there's a lot of similarities with upsert_hosts() above, this function is broken out because
    # its expected that different auditing logic might be added in future to capture hostgroup membership changes too.
    # Making one super DRY'd up upsert_objects function would make for a complicated function that might be hard to maintain.
    #
    # Takes a ref to an array of hostgroup hashes, and a ref to an options hash.
    # Returns 1 on success, 0 otherwise.

    my ( $this, $hostgroups_ref, $options_ref ) = @_;
    my ( $hostgroup_ref, $now, $tz, %built_hostgroup, @built_hostgroups, $hostgroup );
    my ( @hostgroups_bundle, %outcome, @results, @hostgroups_to_check, %these_hostgroups_exist, %these_hostgroups_dont_exist );
    my ( %upserted_successfully, $result_hashref );

    # First make note of whether each hostgroup exists or not and therefore whether it will need adding.
    # This information will determine which properties to use in the upsert_hostgroups() call.
    #
    # Build a list of hostgroup names to check existence for,
    # whilst checking for required fields used here and later.
    # TBD also need to add a check for things that should NOT be passed in here too. 
    # However, its nice to allow overrides for health services internally too, so leave as-is for now.
    foreach $hostgroup_ref ( @{$hostgroups_ref} ) {
        if ( not defined $hostgroup_ref->{name} ) { 
            $this->report_feeder_error( "ERROR expected name in hostgroup object" . Dumper $hostgroup_ref );
            return 0; 
        }
        else {
            push @hostgroups_to_check, $hostgroup_ref->{name} ;
        }
    }

    # Check existence of these hostgroups. This information will be used later when building the api data structures, which content varies depending
    # on whether hostgroup exists or not.
    #$this->{logger}->debug("DEBUG starting hostgroups existence checking ....");
    if ( not $this->check_foundation_objects_existence( 'hostgroups', \@hostgroups_to_check, \%these_hostgroups_exist, \%these_hostgroups_dont_exist ) ) {
        $this->report_feeder_error("ERROR checking Foundation hostgroups existence"); 
        return 0; 
    }
    #$this->{logger}->debug("DEBUG ending hostgroups existence checking ....");

    # Process each hostgroup object by 'building' it ie preparing its fields for REST API call
    foreach $hostgroup_ref ( @{$hostgroups_ref} ) {

        # Initialize the built hostgroup with all of the fields/values passed in, and then update them.
        # This way you keep the door open to whatever fields/values the feeder code cares to pass in.
        # Useful for future new supported REST API host fields.
        %built_hostgroup = %{$hostgroup_ref};
        
        # When upserting hostgroups, there are two cases: the hostgroups already exists, the hostgroups doesn't already exist.
        # Depending on the case, a different set of properties need to be sent to GW::RAPID/REST API upsert_hostgroups().
        # In the case of a hostgroup existing and having been created by Nagios (or other app) earlier, 
        # dont want to upsert existing properties.
        # The only properties needed if upserting an existing hostgroup, are : name ( yes you can upsert an existing hostgroup with nothing )
        # Required fields first
        $built_hostgroup{name} = $hostgroup_ref->{name};

        if ( not exists $these_hostgroups_exist{ $hostgroup_ref->{name} } ) {
            # Use values if they were specified, defaults if not
            $built_hostgroup{alias}       = ( defined $hostgroup_ref->{alias} )        ? $hostgroup_ref->{alias}         : $hostgroup_ref->{name};
            $built_hostgroup{description} = ( defined $hostgroup_ref->{description} )  ? $hostgroup_ref->{description}   : $hostgroup_ref->{name};
            $built_hostgroup{appType}     = ( defined $hostgroup_ref->{appType} )      ? $hostgroup_ref->{appType}       : $this->{properties}->{app_type};
            $built_hostgroup{agentId}     = ( defined $hostgroup_ref->{agentId} )      ? $hostgroup_ref->{agentId}       : $this->{properties}->{guid};
        }

        # Add the prepared host datastructure to the build_hosts array that will be used in the REST API upsert_hosts call
        push @built_hostgroups, { %built_hostgroup };

    };


    # To be efficient, will add hostgroups in bundles, including auditing hostgroups existence testing
    while ( @hostgroups_bundle = splice @built_hostgroups, 0, $this->{hostgroup_bundle_size}) { 

        $this->{logger}->debug( "DEBUG feeder_upsert_hostgroups() starting to process bundle of " . ($#hostgroups_bundle + 1 ) . " hostgroup(s)" );

        # If auditing, then need to check whether the hostgroups in this bundle exists or not prior to upserting it, 
        # so that later can then test if the hostgroups was added to Foundation anew.
        # For those hostgroups that do NOT exist, make a note that they will be added.
        # After the REST API call to actually try and add hostgroups, then check the results to see which actually were added,
        # and then update the audit trail finally. Phew.
        # This is now already done above, so carefully remove this later and retest auditing system

        # Run the upsert on the hostgroups bundle
        if ( not $this->{rest_api}->upsert_hostgroups( \@hostgroups_bundle, $options_ref, \%outcome, \@results ) ) {
            $this->{logger}->error( "ERROR something went wrong upserting hostgroups: " . Dumper \%outcome );
            return 0;
        }

        # Update audit trail
        # TBD in future version : add logic to cover hostgroup membership changes too perhaps
        if ( $this->{auditing} ) {

            # Check that each hostgroup in the %these_hostgroups_dont_exist hash, was successfully upserted, and
            # if that host was one of those that didn't exist earlier, add it to the audit trail

            if ( @results ) { # ... First, @results has to be defined, else assuming that upserting completely failed

                # Add successfully upsert'd hostgroups to a hash ie if the result entity was tagged as success
                foreach $result_hashref ( @results ) {
                    $upserted_successfully{ $result_hashref->{entity} } = 1 if $result_hashref->{status} eq 'success';
                }
            
                # Check if host was upserted ok
                foreach  $hostgroup ( keys %these_hostgroups_dont_exist ) {
                    if ( defined $upserted_successfully{ $hostgroup } ) { 
                        $this->{audit_trail}->{hostgroups}{$hostgroup}{created} = 1;
                    }
                }
            }
            else {
                # Already covered the failure case above hopefully - revisit this TBD case 
                # print "No results. Here's the outcome : " . Dumper \%outcome; 
            }
        }

    }

    return 1;
}

# ----------------------------------------------------------------------------------------------------------------
sub feeder_upsert_services
{
    # Create or update a set of hosts' services. 
    # So the wrinkle with upserting services is that devices are auto created if they don't exist. 
    #
    # Whilst there's a lot of similarities with upsert_hosts() etc above, this function is broken out because
    # of different auditing logic for device auto creation that can happen when creating services.
    # Making one super DRY'd up upsert_objects function would make for a complicated function that might be hard to maintain.
    #
    # Takes a ref to an array of service object hashes, and a ref to an options hash.
    # Returns 1 on success, 0 otherwise.

    my ( $this, $services_ref, $options_ref ) = @_;
    my ( $service_ref, $now, $tz, %built_service, @built_services, $service );
    my ( @services_bundle, %outcome, @results );
    my ( %upserted_successfully, $result_hashref );
    my ( @hosts_to_check, %these_hosts_exist, %these_hosts_dont_exist );
    my ( %services_to_check, %these_services_exist, %these_services_dont_exist );
    my ( $result_entity_host, $result_entity_service, %upserted_hosts_services, $host ) ;
    my ( @service_states_filtered_bundle ) ;
    my ( @service_events, $service_event, @created_service_events, @post_these_service_events, $event_severity );
    my ( @created_host_events, @post_these_host_events );

    # First make note of whether each host:service exists or not and therefore whether it will need adding.
    # This information will determine which properties to use in the upsert_hosts() call.
    # 
    # Build a list of hostnames and services for existence checking,
    # whilst checking for required fields used now and later
    # TBD also need to add a check for things that should NOT be passed in here too. 
    # However, its nice to allow overrides for health services internally too, so leave as-is for now.
    foreach $service_ref ( @{$services_ref} ) {
        if ( any { not defined $_ } $service_ref->{description}, $service_ref->{hostName}, $service_ref->{monitorStatus} ) {   
            $this->{logger}->error("ERROR missing expected service field : " . Dumper $service_ref ) ;
            return 0;
        }
        push @hosts_to_check, $service_ref->{hostName} ;  # hosts 
        $services_to_check{ $service_ref->{hostName} }{$service_ref->{description}} = 1; # services
    }

    # Check existence of things. This information will be used later when building the api data structures, which content varies depending
    # on whether services  exists or not.

    # Getting hosts existence is not always required - only for auditing or service state restraining
    # # Get existence of hosts
    # if ( not $this->check_foundation_objects_existence( 'hosts', \@hosts_to_check, \%these_hosts_exist, \%these_hosts_dont_exist ) ) {
    #     $this->report_feeder_error("ERROR occurred while checking Foundation hosts existence");
    #     return 0;
    # }

    # Get existence of services 
    if ( not $this->check_foundation_objects_existence( 'services', \%services_to_check, \%these_services_exist, \%these_services_dont_exist ) ) {
        $this->report_feeder_error("ERROR occurred while checking Foundation services existence");
        return 0;
    }

    # Process each service object by 'building' it ie preparing its fields for REST API call
    foreach $service_ref ( @{$services_ref} ) {

        # Initialize the built service with all of the fields/values passed in, and then update them.
        # This way you keep the door open to whatever fields/values the feeder code cares to pass in.
        # Useful for future new supported REST API host fields.
        %built_service = %{$service_ref};
        
        # When upserting services, there are two cases: the host:service already exists, the host:service doesn't already exist.
        # Depending on the case, a different set of properties need to be sent to GW::RAPID/REST API upsert_services().
        # In the case of a host:service existing and having been created by Nagios (or other app) earlier, 
        # dont want to upsert the agentId because that can cause the service to be deleted entirely later based on that agentId,
        # of the description of the service to change from its original, or the agentId to be overwritten etc.
        # The only properties needed if upserting an existing host:service, are : lastCheckTime, hostName, monitorStatus
        # and monitorStatus is questionable as it will override any other method such as fping.

        # TBD discuss whether the feeder should update the host status

        # Required fields first
        $built_service{description}   = $service_ref->{description}; 
        $built_service{hostName}      = $service_ref->{hostName}; 
        $built_service{monitorStatus} = $service_ref->{monitorStatus}; 

        # lastCheckTime : if not supplied, create it
        if ( not defined $service_ref->{lastCheckTime} ) {
            $now = strftime( '%Y-%m-%dT%H:%M:%S', localtime );
            $tz  = strftime("%z", localtime );
            $built_service{lastCheckTime} = "$now$tz";
        }
        else {
            $built_service{lastCheckTime} = $service_ref->{lastCheckTime} ;
        }

        # Not sure if it makes sense to create a default value here since don't know what the feeder frequency is
        # Default to +10 mins ???
        # nextCheckTime : if not supplied, create it 
        #if ( not defined $service_ref->{nextCheckTime} ) {
        #    $now = strftime( '%Y-%m-%dT%H:%M:%S', localtime );
        #    $tz  = strftime("%z", localtime );
        #    $built_service{nextCheckTime} = "$now$tz";
        #}
        #else {
        #    $built_service{nextCheckTime} = $service_ref->{nextCheckTime} ;
        #}

        # Only add these properties if creating the service rather than updating an existing one
        if ( not exists $these_services_exist{$service_ref->{hostName}}{$service_ref->{description}} ) { 

            # required for Since time to work in status viewer properly
	        $built_service{properties} {LastStateChange}  = $built_service{lastCheckTime} ;  # add to the properties hash, don't reset it !

            $built_service{deviceIdentification} = ( defined $service_ref->{deviceIdentification} )   ? $service_ref->{deviceIdentification} : $service_ref->{hostName};

            # GWMON-11737
            $built_service{monitorServer}        = 'localhost';

            $built_service{stateType}            = ( defined $service_ref->{stateType} )              ? $service_ref->{stateType}            : 'HARD';
            $built_service{checkType}            = ( defined $service_ref->{checkType} )              ? $service_ref->{checkType}            : 'PASSIVE';
            $built_service{lastHardState}        = ( defined $service_ref->{lastHardState} )          ? $service_ref->{lastHardState}        : $service_ref->{monitorStatus}; # NAGIOS vestige. Its actually required by the API
            $built_service{agentId}              = ( defined $service_ref->{agentId} )                ? $service_ref->{agentId}              : $this->{properties}->{guid}; 
            $built_service{appType}              = ( defined $service_ref->{appType} )                ? $service_ref->{appType}              : $this->{properties}->{app_type};

            # GWMON-11605 create a pending event and an initial state event for the to-be created service : 
            #   Event : service->pending
            #   Event : service->$service_ref->{monitorStatus} (whatever that is in event speak)
            # This only happens if creating the service 
            if ( $built_service{monitorStatus} ne 'OK' ) { $event_severity = 'SERIOUS' ;  } else { $event_severity = 'OK' ; }
            push @service_events , { # Initial state event (pending will be added later)
                                        'service'            => $built_service{description},
                                        'host'               => $built_service{hostName},
                                        'monitorStatus'      => $built_service{monitorStatus}, 
                                        'severity'           => $event_severity,
                                        'appType'            => $built_service{appType},
                                        'device'             => $built_service{deviceIdentification},
                                        'reportDate'         => $built_service{lastCheckTime},
                                        'firstInsertDate'    => $built_service{lastCheckTime},
                                        'textMessage'        => 'Service creation initial state event'
                                   };
        }

        # If the status of the service has changed, then need to add a lastStateChange prop to sthat the status viewer <state> Since data updates correctly
        else { # host/service exists - get it's status currently in GW and compare against incoming - ie detect state change and add lastStateChange prop
            my %outcome; my %results;
            if ( not $this->{rest_api}->get_services( [ $built_service{description} ], { hostname => $built_service{hostName} , format => 'host,service' }, \%outcome, \%results ) ) { # get service state
                $this->{logger}->error( "ERROR something went wrong getting services: " . Dumper \%outcome, \@results );
                return 0;
            }
            if ( $built_service{monitorStatus} ne $results{$built_service{hostName}}{$built_service{description}}{monitorStatus} ) { # if change, add lastStateChange prop
                $built_service{lastStateChange} = $built_service{lastCheckTime};
            }
        }

        # Add the prepared host datastructure to the build_hosts array that will be used in the REST API upsert_hosts call
        push @built_services, { %built_service };

    };

    # print "=" x 50 . Dumper \@built_services;

    # To be efficient, will add services in bundles, including auditing hostgroups existence testing
    while ( @services_bundle = splice @built_services, 0, $this->{service_bundle_size}) { 
 
        $this->{logger}->debug( "DEBUG feeder_upsert_services() starting to process bundle of " . ($#services_bundle + 1 ) . " service(s)" );

        # If auditing, or constraining to service states, then need to check whether the hosts+services in this bundle exists or not prior to upserting it, 
        # so that later can then test if the hos+services  was added to Foundation anew.
        # Constraining to service states only applies to adding services ie creating them anew, so need to whether host+service exists/doesn't first.
        # For those host+services  that do NOT exist, make a note that they will be added.
        # After the REST API call to actually try and add host+services , then check the results to see which actually were added,
        # and then update the audit trail finally. Phew. JIRA required for more details back from the API whether thing was created at upsert time.
        # Also need to see which hosts+services exist in a similar fashion

        @hosts_to_check = (); %these_hosts_exist = (); %these_hosts_dont_exist = ();
        %services_to_check = (); 
        %upserted_hosts_services = ();
        @service_states_filtered_bundle = ();

        # If auditing, or constraining to service states ...
        if ( $this->{auditing} or scalar keys %{$this->{constrain_to_service_states}} or $this->{post_events} ) {
            # Build structures to pass in to object existence checking
            foreach $service_ref ( @services_bundle ) {
                push @hosts_to_check, $service_ref->{hostName} ; # ( hostname1, hostname2, ... )  # TBD doc that a certain data structure is required
                $services_to_check{ $service_ref->{hostName} }{$service_ref->{description}} = 1;
            }

            @hosts_to_check =  uniq @hosts_to_check;

            # Passes a list of hosts to check, gets hosts that exist/don't exist - needed since upsert_service can add hosts automatically
            if ( not $this->check_foundation_objects_existence( 'hosts', \@hosts_to_check, \%these_hosts_exist, \%these_hosts_dont_exist ) ) {
                $this->report_feeder_error("ERROR occurred while building auditing - audit trail will be incorrect"); # TBD consider adding a quit-on-audit-errors option
            }

            # If filtering down based on service states, then build a service state filtered bundle with services with states that match constraint filter
            if ( scalar keys %{$this->{constrain_to_service_states}} ) {
                # Create a new filtered bundle based on comparing each event's state in the bundle with that in the contrain_to_service_states set
                foreach $service_ref ( @services_bundle ) { 
                    if ( exists $this->{constrain_to_service_states}->{ lc( $service_ref->{monitorStatus} ) } ) { 
                        #print "KEEP this one : $service_ref->{monitorStatus}\n";
                        push @service_states_filtered_bundle, $service_ref; 
                    }
                    else {
                        #print "DON'T KEEP this one : $service_ref->{monitorStatus}\n";
                        # Some debug log message about this service being filtered out  TBD
                    }
                        
                }
                # Replace @services_bundle with the new bundle 
                @services_bundle = @service_states_filtered_bundle; 
            }
        }
    
        # Run the upsert on the services bundle
        if ( not $this->{rest_api}->upsert_services( \@services_bundle, $options_ref, \%outcome, \@results ) ) {
            $this->{logger}->error( "ERROR something went wrong upserting services: " . Dumper \%outcome, \@results );
            return 0;
        }

        # Update audit trail
        # ... or if posting events (GWMON-11605)
        if ( $this->{auditing} or $this->{post_events} ) {

             # Check that each host and service in the dont_exist hashes were successfully upserted, and
             # if they were one of those that didn't exist earlier, add to the audit trail
 
             if ( @results ) { # @results has to be defined, else assuming that upserting completely failed

                 # this is all repeated for each of the service bundles so need to reset these each time 
                 @created_service_events = (); @created_host_events = (); 

                 # Add successfully upsert'd hosts:services entities to a hash ie if the result entity was tagged as success.
                 # Note that a hostname is not allowed to have a : in it. 

                 foreach $result_hashref ( @results ) {
                    # The result entity is going to look like this :  host:service and host should not have :'s in it.
                    # In the version of the API developed against, :s are possible. JIRA GWMON-11574 filed.
                    ($result_entity_host, $result_entity_service) = split( /:/, $result_hashref->{entity} ); # TBD API FIX 
                    $upserted_hosts_services{$result_entity_host }{$result_entity_service} = 1;
                 }
             
                 # Check if host was upserted ok 
                 foreach $host ( keys %these_hosts_dont_exist ) {
                     if ( defined $upserted_hosts_services{ $host } ) { 
                        $this->{audit_trail}->{hosts}{$host}{created}{host}{added} = 1;
                        # GWMON-11605 create a set of pending and initial state host events
                        # Care is taken to only create pending and initial state events for hosts that were only successfully created.
                        foreach $service_event ( @service_events ) {
                            if ( $service_event->{host} eq $host )  {
                                # create the PENDING state event
                                # Note that the pending event needs to come before the initial state event
                                # so 1 second is subtracted. Could do this a different way obviously.
                                push @created_host_events, {
                                                                'appType'         => $service_event->{appType},
                                                                'device'          => $service_event->{device},
                                                                'host'            => $service_event->{host},
                                                                'monitorStatus'   => 'PENDING',
                                                                'reportDate'      => subtract_a_second( $service_event->{reportDate}, $sql_time_format  ),
                                                                'firstInsertDate' => subtract_a_second( $service_event->{reportDate}, $sql_time_format  ),
                                                                'severity'        => 'OK',
                                                                'textMessage'     => 'Host creation PENDING event'
                                                           };
                                # create the initial host state event 
                                push @created_host_events, {
                                                                'appType'         => $service_event->{appType},
                                                                'device'          => $service_event->{device},
                                                                'host'            => $service_event->{host},
                                                                'monitorStatus'   => 'UP', # don't actually know what that is so set it to UP for now
                                                                'reportDate'      => $service_event->{reportDate},
                                                                'firstInsertDate' => $service_event->{reportDate},
                                                                'severity'        => 'OK',
                                                                'textMessage'     => 'Host creation initial state event'
                                                           };
                            }
                        }
                    }
                 }

                 # Check if service was upserted ok 
                 foreach $host ( keys %these_services_dont_exist ) {
                   foreach $service ( keys %{$these_services_dont_exist{$host}} ) {
                     if ( defined $upserted_hosts_services{ $host }{ $service}  ) { 
                        $this->{audit_trail}->{hosts}{$host}{created}{services}{$service} = 1;
                        # GWMON-11605 create a set of pending and initial state service events.
                        # Care is taken to only create pending and initial state events for services that were only successfully created.
                        # Note that the pending event needs to come before the initial state event
                        # so 1 second is subtracted. Could do this a different way obviously.
                        foreach $service_event ( @service_events ) {
                            if ( $service_event->{host} eq $host and $service_event->{service} eq $service ) {
                                # create the PENDING state event
                                push @created_service_events, { 
                                                                'service'         => $service_event->{service},
                                                                'host'            => $service_event->{host},
                                                                'monitorStatus'   => 'PENDING',
                                                                'severity'        => 'OK', 
                                                                'appType'         => $service_event->{appType},
                                                                'device'          => $service_event->{device},
                                                                'reportDate'      => subtract_a_second( $service_event->{reportDate} , $sql_time_format ) ,
                                                                'firstInsertDate' => subtract_a_second( $service_event->{reportDate} , $sql_time_format ) ,
                                                                'textMessage'     => 'Service creation PENDING event'
                                                              };
                                # create the initial state event
                                push @created_service_events, $service_event;
                            }
                        }
                      }
                    }
                 }

            }
            else {
                 # Already covered the failure case above hopefully - revisit this TBD case 
                 # print "No results. Here's the outcome : " . Dumper \%outcome; 
            }

            # GWMON-11605 post any state initial and pending state events
            if ( @created_service_events ) {
                push @post_these_service_events, @created_service_events; # just make one jumbo list of service events to post later
            }
            if ( @created_host_events ) {
                push @post_these_host_events, @created_host_events; # just make one jumbo list of host events to post later
            }

        } # end of @results populated test

    } # end auditing / posting events check

    # GWMON-11605 post any state initial and pending state events
    if ( @post_these_service_events ) { 
        #print "++++++++++++ Feeder upsert services: " . Dumper \@post_these_service_events;
        if ( not $this->feeder_post_events( 'service', \@post_these_service_events ) ) {
            $this->report_feeder_error( "ERROR Failed to create events during upserting of services");
            return 0;
        }
    }
    if ( @post_these_host_events ) { 
        #print Dumper \@post_these_host_events;
        if ( not $this->feeder_post_events( 'host', \@post_these_host_events ) ) {
            $this->report_feeder_error( "ERROR Failed to create events during upserting of services");
            return 0;
        }
    }
        
    return 1;
}

# ----------------------------------------------------------------------------------------------------------------
sub feeder_delete_hosts
{
    # Takes a hash : { host1=>1, host2=>1 ... }, and a ref to an options hash for delete_hosts();
    # Runs an bundled delete on the keys of that hash.
    # Supports audit trail.
    # returns 1 on success, 0 on failure

    my ( $this,  $hashref_hosts, $hashref_options ) = @_;
    my ( %these_hosts_exist, %these_hosts_dont_exist, @all_hosts, @hosts_bundle );
    my ( %outcome, @results );
    my ( $result_hashref, %deleted_successfully, $host );

    @all_hosts = sort keys %{$hashref_hosts} ;
    
    # Bundled hosts deletion and hosts existence testing for auditing
    while ( @hosts_bundle = splice @all_hosts, 0, $this->{host_bundle_size}) { 

        $this->{logger}->debug( "DEBUG feeder_delete_hosts() starting to process bundle of " . ($#hosts_bundle + 1 ) . " host(s)" );

        %these_hosts_exist = (); %these_hosts_dont_exist = ();

        if ( $this->{auditing} ) {
            if ( not $this->check_foundation_objects_existence( 'hosts', \@hosts_bundle, \%these_hosts_exist, \%these_hosts_dont_exist ) ) {
                $this->{logger}->error( "ERROR Failed to check existence of hosts in Foundation" );
                return 0;
            }

            # If that worked, then now have the list hosts that do/dont exist
        }

        # Run the delete on the hosts bundle
        $this->{logger}->debug( "DEBUG Deleting Foundation hosts @hosts_bundle" );
        if ( not $this->{rest_api}->delete_hosts( \@hosts_bundle, $hashref_options, \%outcome, \@results ) ) {
            $this->{logger}->error( "ERROR Something went wrong deleting hosts : " . Dumper \%outcome, \@results );
            return 0;
        }
    
        # Update audit trail
        if ( $this->{auditing} ) {

            # Check that each host in the %these_hosts_exist hash, were successfully deleted, and
            # if that host was one of those that existed earlier, add it to the audit trail for deleted hosts

            if ( @results ) { # ... First, @results has to be defined, else assuming that upserting completely failed

                # Add successfully deleted hosts to a hash ie if the result entity was tagged as success
                foreach $result_hashref ( @results ) {
                    $deleted_successfully{ $result_hashref->{entity} } = 1 if $result_hashref->{status} eq 'success';
                }
            
                # Check if host was deleted ok
                foreach $host ( keys %these_hosts_exist ) {
                    if ( defined $deleted_successfully{ $host } ) { 
                        $this->{audit_trail}->{hosts}{$host}{deleted}{host}{deleted} = 1;
                    }
                }
            }
            else {
                # Already covered the failure case above hopefully - revisit this TBD case 
                # print "No results. Here's the outcome : " . Dumper \%outcome; 
            }
        }
    }

    return 1;

}

# ----------------------------------------------------------------------------------------------------------------
sub feeder_delete_services
{
    # Takes a hash : { host1=> { svc1=>1, svc2=>1,... },  host2=> {svc10=>1, svc12=>1} , ... } , and a ref to an options hash for delete_services();
    # Runs delete services on a per host basis (ie not so efficient in terms of bundling), mainly cos I don't believe there's a way to
    # delete host1:svc1, host2:svc2,... in a bundled fashion with the API.
    # Supports audit trail.
    # returns 1 on success, 0 on failure

    my ( $this, $hashref_services, $hashref_options ) = @_;
    my ( %these_services_exist, %these_services_dont_exist, $host, $service, @services );
    my ( $result_hashref, %deleted_successfully, $result_entity_host, $result_entity_service, %deleted_hosts_services );
    my ( %outcome, @results );

    foreach $host ( keys %{$hashref_services} ) {
        $this->{logger}->debug( "DEBUG feeder_delete_services() processing host $host" );

        %these_services_exist = (); %these_services_dont_exist = ();

        if ( $this->{auditing} ) {
            if ( not $this->check_foundation_objects_existence( 'services', { $host => $hashref_services->{$host} } , \%these_services_exist, \%these_services_dont_exist ) ) {
                $this->{logger}->error( "ERROR Failed to check existence of services in Foundation" );
                return 0;
            }
            # If that worked, then now have the lists of services that do/don't exist for this host
        }

        @services = keys %{ $hashref_services->{$host} } ;

        # Run the services delete for this host.
        # to delete services for a single host : delete_services( \@servicenames, { hostname => 'hostname' }, \%outcome, \@results );
        $this->{logger}->debug( "DEBUG Deleting Foundation services : host $host, services @services " );
        if ( not $this->{rest_api}->delete_services( \@services, { hostname => $host} , \%outcome, \@results ) ) {
            $this->{logger}->error( "ERROR Something went wrong deleting hosts : " . Dumper \%outcome );
            return 0;
        }

        # Check the results to see which ones were deleted and update audit trail accordingly
        if ( $this->{auditing} ) {

            # Check that each host:service in the %these_services_exist hash, were successfully deleted, and
            # if that host:service was one of those that existed earlier, add it to the audit trail for deleted hosts

            if ( @results ) { # ... First, @results has to be defined, else assuming that upserting completely failed

                 foreach $result_hashref ( @results ) {
                    # The result entity is going to look like this :  host:service and host should not have :'s in it.
                    # in them. In the version of the API developed against, :s are possible. JIRA task filed.
                    ($result_entity_host, $result_entity_service) = split( /:/, $result_hashref->{entity} ); # TBD API FIX 
                    $deleted_hosts_services{$result_entity_host }{$result_entity_service} = 1;
                 }

                 # Check if service was deleted ok 
                 foreach $host ( keys %these_services_exist ) {
                    foreach $service ( keys %{$these_services_exist{$host}} ) {
                        if ( defined $deleted_hosts_services{ $host }{ $service } ) { 
                            #print "DELETED ------ SERVICE $host -> $service\n"; # Maybe log this in the field if necessary. For now tmi
                            $this->{audit_trail}->{hosts}{$host}{deleted}{services}{$service} = 1;
                        }
                    }
                }
            }
            else {
                # Already covered the failure case above hopefully - revisit this TBD case 
                # print "No results. Here's the outcome : " . Dumper \%outcome; 
            }
        }
    }

    return 1;

}

# ----------------------------------------------------------------------------------------------------------------
sub check_foundation_objects_existence
{
    # Takes :
    # 1. a ref to an array of things to check for existence of in Foundation:
    #    Expected argument structures :
    #    Type         Structure
    #    hosts        an array of hostnames
    #    hostsgroups  an array of hostgroups names
    #    services     hash like this : { host1 => { svc1=>1, svc2=>2,...} ,  host2 = { svc10=>1, svc20=>1,... } , ... }
    #                 afaik the current REST API cannot be passed a list of host/service, host/service, ... pairs to check for at this time.
    #                 so for now, get all services for the list of hosts, then figure it out.
    # 2. two results hashes refs, one for storing a list of the hosts which do exist, and one for those that don't
    # returns 1 on successfully being able to get objects, 0 otherwise
    # TBD improve error handling of this routine, including rest_api calls
    # NOTE/TBD : perhaps need an easier way via the REST API to do existence checking , like using HEAD

    my ( $this, $object_type, $ref_objects_to_check, $hashref_objects_exist, $hashref_objects_dont_exist ) = @_;
    my ( %outcome, %results, $object, $get_status, @hostnames, %hosts, $hostname, $service );
    my ( @uniq_objects_list, @objects_bundle, @object_list, $bundle_size );

    if    ( $object_type eq 'hosts' )      { $bundle_size = $this->{host_bundle_size};      @uniq_objects_list = uniq @{$ref_objects_to_check};      }
    elsif ( $object_type eq 'hostgroups' ) { $bundle_size = $this->{hostgroup_bundle_size}; @uniq_objects_list = uniq @{$ref_objects_to_check};      }
    elsif ( $object_type eq 'services' )   { $bundle_size = $this->{service_bundle_size};   @uniq_objects_list = uniq keys %{$ref_objects_to_check}; }
    else {
        $this->{logger}->error( "ERROR Bailing due to object type '$object_type' not yet supported by check_foundation_objects_existence()" ) ;
        return 0;
    }

    while ( @objects_bundle = splice @uniq_objects_list, 0, $bundle_size ) {  # bundling is important to avoid REST API uri too long errors
        %results = ();
        if ( $object_type eq 'hosts' ) { 
            $this->{logger}->trace( "TRACE existence checking - getting status of " . ( $#objects_bundle + 1 ) . " host(s)");
            $get_status = $this->{rest_api}->get_hosts( \@objects_bundle, {}, \%outcome, \%results ) ; 
        }
        if ( $object_type eq 'hostgroups' ) { 
            $this->{logger}->trace( "TRACE existence checking - getting status of " . ( $#objects_bundle + 1 ) . " hostgroup(s)");
            $get_status = $this->{rest_api}->get_hostgroups( \@objects_bundle, {}, \%outcome, \%results ) ; 
        }
        if ( $object_type eq 'services' ) { 
            $this->{logger}->trace( "TRACE existence checking - getting status of services for " . ( $#objects_bundle + 1 ) . " host(s)");
            $get_status = $this->{rest_api}->get_services( [], { hostname => \@objects_bundle, format => 'host,service' }, \%outcome, \%results );
        }
        # If status was ok, then some set of the objects was found
        if ( $get_status ) {
            if ( $object_type eq 'hosts' or $object_type eq 'hostgroups' ) {
                foreach $object ( @objects_bundle ) {
                    if ( defined $results{$object} ) { $hashref_objects_exist->{$object} = 1; } # found
                    else { $hashref_objects_dont_exist->{$object} = 1; }  # not found
                } 
            } 
            elsif ( $object_type eq 'services' ) {
                foreach $object ( @objects_bundle ) {
                    # Loop over the original hosts/services to check, and see if they showed up in the results
                    foreach $service ( keys %{ $ref_objects_to_check->{$object} }  ) {
                        #print "---- Checking $object - $service\n";
                        if ( defined $results{ $object } { $service } ) { $hashref_objects_exist->{ $object }{ $service } = 1; } # found
                        else { $hashref_objects_dont_exist->{ $object }{ $service } = 1; } # not found
                    }
                }
            }
        } 
        else { # In the case of a response code of 404, NONE of the objects were found
            if ( defined $outcome{response_code} and $outcome{response_code} == 404 ) { # 404 response code
                if ( $object_type eq 'hosts' or $object_type eq 'hostgroups' ) {
                    foreach $object ( @objects_bundle ) { $hashref_objects_dont_exist->{$object} = 1; }
                }
                elsif ( $object_type eq 'services' ) {
                    foreach $object ( @objects_bundle ) { 
                        foreach $service ( keys %{ $ref_objects_to_check->{$object} }  ) { $hashref_objects_dont_exist->{ $object }{ $service } = 1; }
                    }
                }
            }
            else { # Some other unhandled outcome eg uri length limit exceeded
                $this->{logger}->error( "ERROR Something went wrong checking existence of ${object_type}(s) : " . Dumper \%outcome );
                return 0;
            }
        }
    }

    return 1;

}



# ----------------------------------------------------------------------------------------------------------------
sub flush_audit
{
    # If auditing is on, parses the audit_trail hash and writes things to Foundation events.
    # Also, if things were created/deleted, then send notifications too.
    # If flushes as it goes along.
    # Returns 1 on success, 0 otherwise

    # Structure of audit trail hash for Foundation change reporting:
    #
    #  HOSTS and SERVICES
    #    $audit_trail{hosts}{$hostname}{created}{host}{added}   => 1            : presence means host was added
    #    $audit_trail{hosts}{$hostname}{created}{hostgroups}    => @hostgroups  : hostgroups to which the host was added
    #    $audit_trail{hosts}{$hostname}{created}{services}{"someservice"}  => 1 : services which were added to the host
    #    $audit_trail{hosts}{$hostname}{deleted}{host}{deleted} => 1            : presence means host was deleted
    #    $audit_trail{hosts}{$hostname}{deleted}{hostgroups}    => @hostgroups  : hostgroups from which the host was removed
    #    $audit_trail{hosts}{$hostname}{deleted}{services}{"someservice"}  => 1 : services which were deleted from the host
    #
    #  HOSTGROUPS
    #    $audit_trail{hostgroups}{$hostgroupname}{created} => 1                 : presence means hostgroup was created
    #    $audit_trail{hostgroups}{$hostgroupname}{deleted} => 1                 : presence means hostgroup was deleted

    my ( $this ) = @_;
    my ( $hostgroup, $host, $service, $action );
    my ( @notifications, @events );
    my ( %outcome, %results, $msg );

    # Write the audit trail to Foundation events.
    # Auditing may best be done some underlying API level such as the REST API or Collage API. 
    # Until that functionality exists, this library aims at providing at least some functionality here.
    # Currently there is no AUDIT type of event, meaning have to (ab)use the exist host/host-service event properties.
    # GWMON-11585 created.
    # For now, create events against the cacti feeder health hostname for now (including the hostname of where feeder was running)

    # Do nothing if auditing is off ie audit_trail is not defined or false
    if ( not $this->{auditing} )  {
        $this->{logger}->debug( "DEBUG Auditing is disabled" );
        return 1;
    }

    # If the audit trail is empty, return here too
    if ( not scalar keys %{ $this->{audit_trail} } ) {
        $this->{logger}->debug( "DEBUG Audit trail empty - no flushing required");
        return 1 ;
    };

    $this->{logger}->debug( "DEBUG Flushing audit trail");

    # Check AUDIT app type exists and bail if not
    if ( not $this->{rest_api}->get_application_types( ['AUDIT'], {}, \%outcome, \%results ) ) {
        $this->report_feeder_error( "ERROR Cannot flush audit to Foundation events - no AUDIT application type was found" );
        return 0;
    }
    
    # Hostgroups added (nothing currently deletes hostgroups)
    if ( exists $this->{audit_trail}->{hostgroups} ) {
        foreach $hostgroup ( keys %{$this->{audit_trail}->{hostgroups}} ) { 
            foreach $action ( keys %{$this->{audit_trail}->{hostgroups}{$hostgroup}} ) {
                $msg = "Hostgroup $hostgroup $action by feeder $this->{feeder_name} running on $this->{feeder_host}";
                push @events, {
                                    'host'              => $this->{properties}->{health_hostname},
                                    'device'            => $this->{properties}->{health_hostname},
                                    'monitorStatus'     => 'UP', 
                                    'appType'           => 'AUDIT', 
                                    'severity'          => 'OK',
                                    'textMessage'       => $msg
                               };
                push @notifications, {
	                                    'hostName'            => $this->{properties}->{health_hostname},
	                                    'hostState'           => "UP",
	                                    'notificationType'    => "RECOVERY",
	                                    'hostOutput'          => $msg
                                      };
            }
        }
    }

    # Hosts and their services
    if ( exists $this->{audit_trail}->{hosts} ) {

        foreach $host ( keys %{$this->{audit_trail}->{hosts}} ) { 

            # Host added
            if ( exists $this->{audit_trail}->{hosts}{$host}{created}{host}{added} ) {
                $msg = "Host '$host' created by feeder $this->{feeder_name} running on $this->{feeder_host}";
                push @events, {
                                        'host'              => $this->{properties}->{health_hostname},
                                        'device'            => $this->{properties}->{health_hostname},
                                        'monitorStatus'     => 'UP', 
                                        'appType'           => 'AUDIT', 
                                        'severity'          => 'OK',
                                        'textMessage'       => $msg
                               };
                push @notifications, {
	                                    'hostName'            => $this->{properties}->{health_hostname},
	                                    'hostState'           => "UP",
	                                    'notificationType'    => "RECOVERY",
	                                    'hostOutput'          => $msg
                                      };
            }

            # Host deleted
            if ( exists $this->{audit_trail}->{hosts}{$host}{deleted}{host}{deleted} ) {
                $msg = "Host '$host' deleted by feeder $this->{feeder_name} running on $this->{feeder_host}";
                push @events, {
                                        'host'              => $this->{properties}->{health_hostname},
                                        'device'            => $this->{properties}->{health_hostname},
                                        'monitorStatus'     => 'UP', 
                                        'appType'           => 'AUDIT',
                                        'severity'          => 'OK',
                                        'textMessage'       => $msg
                               };
                push @notifications, {
	                                    'hostName'            => $this->{properties}->{health_hostname},
	                                    'hostState'           => "UP",
	                                    'notificationType'    => "RECOVERY",
	                                    'hostOutput'          => $msg
                                      };
            }

            # Services added
            if ( exists $this->{audit_trail}->{hosts}{$host}{created}{services} ) {
                foreach $service ( keys %{ $this->{audit_trail}->{hosts}{$host}{created}{services} } ) { 
                    $msg = "Service '$service' created by feeder $this->{feeder_name} running on $this->{feeder_host}";
                    push @events, {
                                        'host'              => $this->{properties}->{health_hostname},
                                        'device'            => $this->{properties}->{health_hostname},
                                        'monitorStatus'     => 'UP', 
                                        'appType'           => 'AUDIT', 
                                        'severity'          => 'OK',
                                        'textMessage'       => $msg
                                  };
                    push @notifications, {
	                                    'hostName'            => $this->{properties}->{health_hostname},
	                                    'hostState'           => "UP",
	                                    'notificationType'    => "RECOVERY",
	                                    'hostOutput'          => $msg
                                      };
                }
            }

            # Services deleted
            if ( exists $this->{audit_trail}->{hosts}{$host}{deleted}{services} ) {
                foreach $service ( keys %{ $this->{audit_trail}->{hosts}{$host}{deleted}{services} } ) { 
                    $msg = "Service '$service' deleted by feeder $this->{feeder_name} running on $this->{feeder_host}";
                    push @events, {
                                        'host'              => $this->{properties}->{health_hostname},
                                        'device'            => $this->{properties}->{health_hostname},
                                        'monitorStatus'     => 'UP', 
                                        'appType'           => 'AUDIT', 
                                        'severity'          => 'OK',
                                        'textMessage'       => $msg
                                  };
                    push @notifications, {
	                                    'hostName'            => $this->{properties}->{health_hostname},
	                                    'hostState'           => "UP",
	                                    'notificationType'    => "RECOVERY",
	                                    'hostOutput'          => $msg
                                      };
                }
            }
        }
    }

    if ( @events ) {
        if ( not $this->feeder_post_events( 'host', \@events ) ) {
            $this->report_feeder_error("AUDITING ERROR occurred posting auditing events - audit log will not be flushed");
            return 0;
        }
    }

    if ( @notifications and $this->{post_notifications} ) {
        if ( not $this->feeder_post_notifications( 'host', \@notifications ) ) {
                $this->report_feeder_error("AUDITING NOTIFICATIONS ERROR creating host notifications");
        }
    }

    # Empty the audit trail now the audit events have been created
    $this->{audit_trail} = ();
    $this->{logger}->debug( "DEBUG Audit trail flushed");

    return 1;

}

# ----------------------------------------------------------------------------------------------------------------
sub initialize_health_objects
{
    # This subroutine creates these objects for stats and health of the app in Foundation :
    #
    #   <feeder stats hostgroup> 
    #     <feeder health host>
    #         <feeder_name>_health service (automatically by this routine)
    #         other services defined in <feeder_services> hash in conf
    #
    # and then sets the feeder health service status to OK and message = $started_at
    # returns 1 ok , 0 otherwise.

    my ( $this, $started_at ) = @_;
    my ( %instructions, %results, $last_processed_event_id, $last_event_id );
    my ( @hosts,  %host_options, @hostgroups, %hostgroup_options , @services, %service_options, $feeder_specific_service );

    $this->{logger}->debug("DEBUG Initializing $this->{feeder_name} statistical and health objects in Foundation");


    my $now = strftime( '%Y-%m-%dT%H:%M:%S%z', localtime );

    # Update or create the health host and set it to be in an UP state
    %host_options = ( );
    @hosts = (
        { 
            hostName       => $this->{properties}->{health_hostname},
            monitorStatus  => "UP",
            description    => "$this->{feeder_name} virtual host",  
            lastCheckTime => $now,
           #properties    => { 'LastStateChange' => $now }, # No - let feeder_upsert_hosts handle this properly now 5/4/15
        },
    );


    if ( not $this->feeder_upsert_hosts( \@hosts, \%host_options ) ) { 
        $this->{logger}->error("ERROR Could not upsert hosts during feeder health objects initialization");
        return 0;
    }

    # Update or create the health hostgroup and put the health vhost in it
    %hostgroup_options = ();
    @hostgroups = (
        { 
            "name"        => $this->{properties}->{health_hostgroup},
            "alias"       => $this->{properties}->{health_hostgroup},
            "description" => "$this->{feeder_name} virtual hostgroup",
            "hosts"       => [ { "hostName" => $this->{properties}->{health_hostname} } ] ,
            "agentId"     => $this->{guid},
        },
    );

    if ( not $this->feeder_upsert_hostgroups( \@hostgroups, \%hostgroup_options ) ) {
        return 0;
    }

    # Upsert some services on that host to create them
    %service_options = ( );
    @services = ( 
       {
           'description'          => $this->{feeder_name}."_health", # automatically create <feeder_name>_health
           'hostName'             => $this->{properties}->{health_hostname}, 
           'monitorStatus'        => 'OK', 
           'properties'           => { "LastPluginOutput" => "OK - started at $started_at" }, # This is the message
           'lastCheckTime'        => $now,
           #'lastStateChange'      => $now,
       },
    );

    # Add any feeder specific services defined in feeder_services hash prop
    if ( defined $this->{feeder_services} ) {
        foreach my $feeder_specific_service ( keys %{ $this->{feeder_services} } ) {
            push @services, {
                                'description'          => $feeder_specific_service, 
                                'hostName'             => $this->{properties}->{health_hostname}, 
                                'monitorStatus'        => 'OK', 
                                'properties'           => { "LastPluginOutput" => "$this->{feeder_services}{$feeder_specific_service}" },  # seed the value with its description :)
                                'lastCheckTime'        => $now,
                               #'lastStateChange'      => $now,
                             };
        }
    }

    if ( not $this->feeder_upsert_services( \@services, \%service_options ) ) {
        return 0;
    }

    return 1; 
}


# ----------------------------------------------------------------------------------------------------------------
sub initialize_interrupt_handlers
{
    # Sets up interrupt handlers
    # TBD figure out correct best way to handle these when more complete. For now this is just super simple.
    my ( $this ) = @_;

    $SIG{INT}  = sub { $this->terminate_feeder("$this->{feeder_name} feeder was terminated with an interrupt (SIGINT) signal !!!");  };
    $SIG{TERM} = sub { $this->terminate_feeder("$this->{feeder_name} feeder was terminated with a terminate (SIGTERM) signal !!!") } ;
    $SIG{HUP}  = sub { $this->terminate_feeder("$this->{feeder_name} feeder was terminated with a hangup (SIGHUP) signal !!!") } ; # HUP from logrotate 
}

# ----------------------------------------------------------------------------------------------------------------
sub terminate_feeder
{
    # Terminates the feeder in a hopefully transparent way.
    # Takes: 
    #   a message 
    #   optional sleep - sometimes used (in the case of supervise running disabled feeder for example)
    #   optional service status : if this is not set, then assumes bad. If its set to 'OK', 
    #                             then assumes clean termination, as in case of run-once mode
    #   optional no_feeder_ops flag - if its defined, then dont do feeder operations, just log - case : no license installed
    # There's a chance that the feeder cannot talk to the REST API etc, but its worth trying anyway
    # (as long as subs called here don't call terminate_feeder and get stuck in a loop:) )
    #
    # TBD this could be combined with report_feeder_error somehow.
    # TBD improve checking and logic around incoming status

    my ( $this, $message, $sleep, $health_service_status, $no_feeder_ops ) = @_;
    my ( $event_severity ) ;

    $message = "(no termination reason given)" if not defined $message;
    $event_severity = 'OK'; # assume ok event
    if ( not defined $health_service_status ) {
        $health_service_status = 'UNSCHEDULED CRITICAL';
        $event_severity = 'SERIOUS';
        $this->{logger}->error( $message ); # log the message
    } 
    else {
        $this->{logger}->info( $message ); # log the message 
    }

    if ( not defined $no_feeder_ops ) {
        # Put the health host into a down state 
        $this->feeder_upsert_hosts( [
                                        { 
                                            hostName       => $this->{properties}->{health_hostname},
                                            description    => "$this->{feeder_name} virtual host", 
                                            monitorStatus  => 'UNSCHEDULED DOWN',
                                        }
                                    ],
                                    {}  # options
        );

        # Post a message to the health service
        $this->feeder_upsert_services(    [ 
                                            {
                                                'description'          => $this->{feeder_name}."_health",
                                                'hostName'             => $this->{properties}->{health_hostname}, 
                                                'monitorStatus'        => $health_service_status,
                                                'properties'           => { "LastPluginOutput" => localtime() . " $message" }
                                            }
                                          ],
                                          {} # options
        );                                   

        # Create a service event for this termination event too
        $this->feeder_post_events( 'service', [ 
                                                {
                                                    'host'              => $this->{properties}->{health_hostname},
                                                    'device'            => $this->{properties}->{health_hostname},
                                                    'service'           => $this->{feeder_name}."_health",
                                                    'monitorStatus'     => $health_service_status,
                                                    'appType'           => $this->{app_type},
                                                    'severity'          => $event_severity,
                                                    'textMessage'       => $message,
                                                } 
                                            ]
        );
    }

    # Sometimes used (in the case of supervise running disabled feeder for example)
    if (defined $sleep) {
        if ( $sleep < 0 ) { 
            $this->{logger}->info("Sleeping forever");
        }
        else {
            $this->{logger}->info("Sleeping $sleep seconds before quitting...");
        }
        sleep $sleep ;
    }

    # From the perldoc GW::RAPID ...
    # "IMPORTANT:  Before process exit, be sure to release your handle to the
    # REST API.  This will force GW::RAPID to call its destructor.  And that
    # will attempt to log out before Perl's global destruction pass wipes
    # out resources needed for logout to work properly.  We want to log out
    # in order to be polite and release server-side resources right away."
    $this->{rest_api} = undef;
    
    exit;
}

# ----------------------------------------------------------------------------------------------------------------
sub report_feeder_error
{
    # Handles errors detected by feeder by making them transparent through a 
    # feeder health service and logging them too.

    my ( $this, $message ) = @_;
    my ( %outcome, @results );

    $message .= " (reported from " .  ( caller(1) )[3] . "() )" if defined ( ( caller(1) )[3] );
    
    # Update the health service
    $this->{rest_api}->upsert_services(    [ 
                                             {
                                                'description'          => $this->{feeder_name}."_health",
                                                'hostName'             => $this->{properties}->{health_hostname}, 
                                                'monitorStatus'        => 'UNSCHEDULED CRITICAL', 
                                                'properties'           => { "LastPluginOutput" => $message }
                                             }
                                           ], 
                                           {}, \%outcome, \@results 
    );

    # Create an event for this error
    $this->{rest_api}->create_events( [
                                            {
                                                'host'              => $this->{properties}->{health_hostname},
                                                'device'            => $this->{properties}->{health_hostname},
                                                'service'           => $this->{feeder_name}."_health",
                                                'monitorStatus'     => 'UNSCHEDULED CRITICAL',
                                                'appType'           => $this->{app_type},
                                                'severity'          => 'SERIOUS',
                                                'textMessage'       => $message,
                                                'reportDate'        => strftime( '%Y-%m-%dT%H:%M:%S', localtime ) .  strftime("%z", localtime )
                                            } 
                                       ],
                                       {}, \%outcome, \@results 
    );

    # Log the error too
    $this->{logger}->error( $message );
}

# ------------------------------------------------------------------------------------------------------------------------
sub get_current_time
{
    # Returns the current time in SQL date format
    my ($sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $isdst) = localtime(time);
    return sprintf("%04d-%02d-%02d %02d:%02d:%02d", $year+1900, $mon+1, $mday, $hour, $min, $sec);
}

# ----------------------------------------------------------------------------------------------------------------
sub run_system_cmd
{
    # Runs a system command
    # This is considered temporary and only in here to do license checking until the API supports that
    # expects 0 as a success stat from the cmd
    # returns 0 on success, the cmd stat if not
    # TBD Pretty sure this routine can be improved/revamped/done in better way
    #     Eg remove the feeder object dependency at least

    my ( $logger, $cmd ) = @_;
    my $tries = 1;
    my $maxtries = 1; # this used to be 3 but set to 1 for cacti feeder
    my $sleep = 1;
    my ( $cmdstat, $shiftedstat );

    $logger->debug("DEBUG running system command '$cmd'");

    while ( $tries <= $maxtries ) {
        $cmdstat = system($cmd) ;  
        $shiftedstat = $cmdstat >> 8;
        if ( $cmdstat == 0 ) { 
            $logger->debug("DEBUG Successfully executed '$cmd'");
            return 0;
        }
        else { 
            $tries++;
            #$logger->error( "ERROR Command '$cmd' failed - status=$shiftedstat (or $cmdstat) $! - retrying in $sleep second" );
            $logger->error( "ERROR Command '$cmd' failed - status=$shiftedstat (or $cmdstat) $!"); # for maxtries=1 - this is a temp change
            sleep $sleep;
            $sleep += $sleep ;
        }
    }

    $logger->error( "ERROR Command '$cmd' failed to execute successfully" );
    #return $cmdstat;
    return $shiftedstat;
}

# ----------------------------------------------------------------------------------------------------------------
sub perl_script_process_count
{
    # Checks if a perl script is running - returns the number of script processes running
    my ( $perlscript ) = @_;
    my $perl_script_process_count = `ps -w -w -o pid,args --no-headers -C .perl.bin | fgrep $perlscript | wc -l`;
    chomp $perl_script_process_count;
    return $perl_script_process_count;
}

# ----------------------------------------------------------------------------------------------------------------
sub license_installed
{
    # On a fresh install of GW, checks license has been installed. 
    # This is a temp workaround for lacking REST API support for a method to see if a license is installed yet.
    # Returns 1 if installed, 0 otherwise.
    # Takes a logger object.

    my ( $this, $logger ) = @_;

    my ( $check_command, $stat ) ;

    $this->{logger}->debug( "DEBUG checking if license is installed" );
    $check_command = "/usr/local/groundwork/core/monarch/bin/add_check 1";
    if ( $this->{license_check} eq "remote" )
    {
        $check_command = "ssh -oConnectTimeout=10 $this->{license_check_user}\@$this->{monitoring_server} $check_command";
    }
   
    $this->{logger}->debug("DEBUG License installation check command set to $check_command");
    $stat =  run_system_cmd( $this->{logger}, $check_command )  ;
    # add_check seems to return 3 if no license is installed.
    if ( $stat == 3 ) { 
        $this->{logger}->debug( "DEBUG license not installed");
        return 0 ; 
    } 
    else { 
        $this->{logger}->debug( "DEBUG license installed");
        return 1; 
    }
}

# ----------------------------------------------------------------------------------------------------------------
sub check_license
{
    # Takes a ref to an array of built hosts ie data structures that can be consumed by upsert_hosts().
    # first checks to see how many hosts actually would need to be added
    # then checks if that number would exceed a license limit
    # returns 1 if would NOT exceed limit, 0 if WOULD
    # NOTE : this is considered temporary and will change once the REST API supports license checking methods

    my ( $this, $arrayref_built_hosts ) = @_;
    my ( $host_ref, @hosts_to_check, $count_hosts_that_need_adding, %these_hosts_exist, %these_hosts_dont_exist, $check_command );

    # Build a list of hostnames to check existence for
    foreach $host_ref ( @{$arrayref_built_hosts} ) {
        if ( not defined $host_ref->{hostName} ) { 
            $this->{logger}->error( "ERROR : check_license() : host data structure missing expected hostName property : " . Dumper $host_ref );
            $this->{logger}->error( "ERROR : check_license() : license checking cannot be performed");
            return 0;
        }
        else {
            push @hosts_to_check, $host_ref->{hostName} ;
        }
    }
    
    # Check existence of these hosts
    if ( not $this->check_foundation_objects_existence( 'hosts', \@hosts_to_check, \%these_hosts_exist, \%these_hosts_dont_exist ) ) {
        $this->{logger}->error( "ERROR : check_license() : Failed to check for host object existence - license checking cannot be performed");
        return 0;
    }
        
    $count_hosts_that_need_adding = scalar keys %these_hosts_dont_exist;
    if ( $count_hosts_that_need_adding > 0 ) {
        $this->{logger}->debug( "DEBUG checking $count_hosts_that_need_adding host(s) could be added without exceeding license limits");
        $check_command = "/usr/local/groundwork/core/monarch/bin/add_check $count_hosts_that_need_adding";
        if ( $this->{license_check} eq "remote" )
        {
            $check_command = "ssh -oConnectTimeout=10 $this->{license_check_user}\@$this->{monitoring_server} $check_command";
        }
    
        $this->{logger}->debug("DEBUG License check command set to $check_command");
        if ( run_system_cmd( $this->{logger}, $check_command )  )
        {
            $this->{logger}->error("ERROR license checking failure - either the license would be exceeded, or there was a problem running the license check command.");
            return 0;
        }
    
        $this->{logger}->debug("DEBUG license checking success - adding $count_hosts_that_need_adding hosts will not exceed a license limit");
    }
    else {
        $this->{logger}->debug("DEBUG license checking not necessary - zero hosts needed adding");
    }
    return 1;
}

# ----------------------------------------------------------------------------------------------------------------
sub subtract_a_second
{
    # Takes a time string, eg 2014-05-13T16:19:44-0400
    # and a format string, eg %Y-%m-%dT%H:%M:%S%z
    # and figures out what that time minus 1 second is and returns that
    # NOTE this routine has only been tested with %Y-%m-%dT%H:%M:%S%z formatted time strings
    # NOTE this routine needs to handle errors better
    # See http://search.cpan.org/~drolsky/DateTime-Format-Strptime-1.55/lib/DateTime/Format/Strptime.pm
    # and http://stackoverflow.com/questions/1274800/how-can-i-parse-a-strftime-formatted-string-in-perl
    my ( $time_string, $format ) = @_;
    my $strp = DateTime::Format::Strptime->new( pattern => $format ) ;
    my $dt = $strp->parse_datetime($time_string);
    return strftime( $format, localtime( $dt->epoch - 1) );
}

1;


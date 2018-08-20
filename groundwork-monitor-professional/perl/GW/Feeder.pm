package GW::Feeder;
# GW::Feeder - GroundWork Feeder module
#
# Copyright 2013-2015 GroundWork Open Source, Inc. (GroundWork).
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
# Revision history:
#
# 2014-01-31 Dominic Nicholas    0.1.0    Original version.
# 2014-05-14 Dominic Nicholas    0.2.2    GWMON-11628: Send notifications on creation/deletion of hosts and services
# 2014-05-14 Dominic Nicholas    0.2.3    GWMON-11617: Provide an option to not have the feeder update the host status
# 2014-05-14 Dominic Nicholas    0.2.4    GWMON-11605: Create pending and initial host/service states on host/service creation
# 2014-05-14 Dominic Nicholas    0.2.5    GWMON-11605 and then some: added firstInsertDate to all events when they are created,
#                                         and made pending state events preceed initial state events by 1 sec to make status graphs render correctly
# 2014-05-14 Dominic Nicholas    0.2.6    Minor fix to auditing routine - only send notifications if post_notifications = yes
# 2014-05-14 Dominic Nicholas    0.2.7    Minor fix to flush_audit so it doesn't run audit flush if auditing = false
# 2014-08-14 Dominic Nicholas    0.2.8    Minor fix to set monitorServer always to be 'localhost' to avoid status viewer crashing
# 2014-08-21 GH                  0.3.0    Use ws_client.properties to find GW::RAPID endpoint and credentials;
#                                         make config-file updating a clean atomic operation.
# 2014-08-22 GH                  0.3.1    Except within terminate_feeder(), this level of code should never directly exit().
# 2014-10-13 GH                  0.3.2    terminate_feeder() should never create objects that don't already exist.
#                                         Also protect a bit against asynchronous service deletion by outside actors.
# 2014-10-14 GH                  0.3.3    The update_hosts_statuses option should not apply to the health_hostname.
#                                         Also update timestamps on feeder health objects.
# 2014-12-17 GH                  0.3.4    Add support for a force_crl_check option.
# 2015-02-04 Dominic Nicholas    0.3.6    Endpoint retry caching, license checking with REST, and various other changes.
# 2015-02-04 Dominic Nicholas    0.3.7    Some code formatting - no changes other than indentations.
# 2015-05-20 Dominic Nicholas    0.3.8    Updates for handling lastStateChange properly in various places (7.0.2 branch 0.3.1.3 updates merge)
# 2015-05-20 Dominic Nicholas    0.3.9    Update to get_services call in feeder_upsert_services to work properly with service names with non-uri-escapable chars like [, / etc
# 2015-06-08 Dominic Nicholas    0.4.0    Backward compat mods to make this version work a) 710 feeders placed in a 702 system, b) 702 feeders in a 702 system
# 2015-06-11 Dominic Nicholas    0.4.1    Udpate to feeder_upsert_hosts() to allow host descriptions to be sent in on create and update
# 2015-06-12 Dominic Nicholas    0.4.2    cleanup $yes option added
# 2015-06-13 Dominic Nicholas    0.4.3    Fix to DESTROY to prevent confusing log4perl messages and termination
# 2015-06-19 Dominic Nicholas    0.4.4    Updates to cleanup including showing what it will do before asking for confirmation
# 2015-06-29 Dominic Nicholas    0.4.5    Updates to cleanup : added do_not_exit opt for multi endpoint feeders cleanup
# 2015-06-30 Dominic Nicholas    0.4.6    Health virtual host now has Status set to tell which host feeder is running on - useful for multi endpoint feeder scenarios
# 2015-07-27 Dominic Nicholas    0.4.7    Added endpoints_enabled() for scom feeder initially, but useful for others too
# 2015-07-28 Dominic Nicholas    0.4.8    Exported terminate_feeder() - useful for feeders like SCOM with multi endpoints in play to use espy when using -once option
# 2015-07-29 Dominic Nicholas    0.4.9    Added a_retry_cache_needs_flushing which is useful when a feeder doesn't want to get roadblocked waiting for new things to do in order to flush a cache to a reachable endpoint
# 2016-01-26 Dominic Nicholas    0.5.0    Added send_metrics() for GWMON-12363, also updated writing of retry cache to not fill it up with entries which contain rows = [ ] .
# 2016-01-25 Dominic Nicholas    0.5.1    Refactored flush_audit() to use RAPID::create_auditlogs (ie /api/auditlogs), including changing cleanup() to use feeder_delete_* so
#                                         that audit trail is created. This required adding feeder_delete_hostgroups() too. Various other mods for metrics work for gwmon-12363 etc etc.
# 2016-02-19 DN 0.5.2 Logging bought into Feeder.pm (removes RAPID_debug and <feeder>.log4perl.conf
# 2016-02-22 DN 0.5.3 <feeder_services> brought out of endpoint config into here and services common across feeders now prepende with $feeder_name
# 2016-03-08 DN 0.5.4 added remove_host_from_hostgroup()
# 2016-08-30 DN 0.5.5 gwmon-12689 : update to create_app_type() to only upsert app type if it isn't present
# 2016-09-23 DN 0.5.6 updated very minor error message in line 1818 - should say there was an issue deleting services, not hosts
# 2016-10-25 DN 0.5.7 GWMON-12763 - minor update to better handle api timeout when getting all services on large systems
# 2017-08-09 DN 0.5.8 added perf data for cacti services
# 
# VIM : set tabstop=4  set expandtab - please use just these settings and no others

# NOTES
# - this is an early version of an attempt to create a feeder utility library based on the GW::RAPID library
# - it is developed out of the requirements from the cacti feeder and cacti feeder version 2, 
#   and is a starting point for development for other feeders in the future.

# KNOWN PROBLEMS
# - if a host or service in Foundation has a : in it, audit trail will probably fail - see 'TBD API FIX'

# TBD
# - As with any software, there is always a lot TBD. That complete list is not documented here yet.
# - Doc up what each sub expects in pod format etc
# - Document and comment and review logging and report_feeder_error's and comments
# - A lot more validation of parameters etc

use warnings;
use strict;
use attributes;

# ================================ Modules ================================
use Data::Dumper; $Data::Dumper::Indent   = 1; $Data::Dumper::Sortkeys = 1;
use POSIX qw(strftime);  # For time formatting
use DateTime::Format::Strptime; # For time manipulation
use TypedConfig qw();    # For reading in the feeder properties file
use Log::Log4perl;       # For logging
use List::MoreUtils qw(any uniq notall);
use Sys::Hostname;
use File::Path qw(make_path);
use File::Basename;
use IO::File;
use JSON; # this automatically uses JSON::XS since JSON ver > 2.0
my  $sql_time_format = '%Y-%m-%dT%H:%M:%S%z';
use version;
use GW::RAPID; # v 0.4.0 - note that there is no version restriction - let Feeder.pm logic determine how to do things based on version of RAPID
use Carp qw(longmess);

# Module export etc
our ( @ISA, @EXPORT, @EXPORT_OK, %EXPORT_TAGS, $VERSION, $feeder_host, $metric_service_meta_tag );
BEGIN {
    use Exporter ();
    @ISA         = qw(Exporter);
    @EXPORT      = qw(  get_current_time 
                        perl_script_process_count 
                        endpoint_retry_cache_name
                        endpoint_retry_cache_prep_dir 
                        endpoint_retry_cache_write 
                        endpoint_retry_cache_import 
                        endpoint_retry_cache_size 
                        a_retry_cache_needs_flushing
                        initialize_interrupt_handlers
                        endpoints_enabled
                        terminate_feeder
                        send_metrics
                        stage_error_for_publishing_via_metrics
                        truncate_file_from_front
                        get_one_prop_from_conf
                        feeder_host
                        metric_service_meta_tag
                        initialize_logger
                        feeder_send_perf_data
                     );
    @EXPORT_OK   = qw();
    %EXPORT_TAGS = ( DEBUG => [ @EXPORT, @EXPORT_OK ] );
    $VERSION     = "0.5.8";
    $feeder_host = Sys::Hostname::hostname();
    $metric_service_meta_tag = "Feeder metric";
}



# ----------------------------------------------------------------------------
sub new
{
    # Creates a new Feeder object.
    # E.g., my $feeder = GW::Feeder->new( $feeder_name, $configuration_file, \%feeder_options, $endpoint_name ) ;
    # Does $logger->logdie( "message \n" ); because die with a \n suppresses filename and line number - RAPID should use this instead of logcroak

    my $invocant           = $_[0];
    my $feeder_name        = $_[1];
    my $configuration_file = $_[2];
    my $options_ref        = $_[3];
    my $endpoint_name      = $_[4];
    my $class              = ref( $invocant ) || $invocant;    # object or class name
    my $self               = undef;
    my ( %config, $feeder_TypedConfig_object, %RAPID_options );

    # V0.4.0. 
    # Various features come with RAPID that comes with 710+ :
    #    - force_crl_check
    #    - a license_check() method
    # If a 710+ version of RAPID is detected, use that information to do or don't use these newer features.
    # This allows backwards 702 compatibility - ie a 702 feeder to use this version of Feeder.pm.
    if ( version->new( $GW::RAPID::VERSION )->numify() >= version->new( '0.7.6' )->numify() ) { 
            $config{RAPID_710_plus} = 1;
    }
    else  {
            $config{RAPID_710_plus} = 0;
    }

    eval {

	    my $logger = ( defined($options_ref) ? $options_ref->{logger} : undef) || Log::Log4perl::get_logger("GW.RAPID.module");
	    $config{logger} = $logger;
	    $config{feeder_name} = $feeder_name;
	    $config{endpoint_name} = $endpoint_name;
	    $config{feeder_host} = ( split( m{\.}xms, hostname() ) )[0]; # use simple hostname, rather than possible fqdn
    
	    my %valid_options = ( logger => 'logger handle', feeder_specific_properties => 'hash', api_options => 'hash' );
    
	    if ( defined $options_ref ) {
    
	        if ( attributes::reftype($options_ref) ne 'HASH' ) {
		        $logger->logdie("ERROR Invalid feeder options hash.\n");
	        }

	        foreach my $key ( keys %$options_ref ) {
		        if ( not exists $valid_options{$key} ) {
		            $logger->logdie("ERROR Invalid feeder option '$key'.\n");
		        }
		        if ( $valid_options{$key} eq 'integer' ) {
		            if ( $options_ref->{$key} !~ /^\d+$/ ) {
			            $logger->logdie("ERROR Invalid feeder option '$key': not an integer.\n");
		            }
		        }
		        elsif ( $valid_options{$key} eq 'logger handle' ) {
		            ## FIX MINOR:  Allow more flexibility in what is accepted as a logger handle.
		            ## Perhaps a simple open file handle would do the job, if we modify the rest of
		            ## this package to deal with such an object appropriately.
		            if ( ref $options_ref->{$key} ne 'Log::Log4perl::Logger' ) {
			            # $logger->logcroak("ERROR Invalid feeder option '$key': not a handle.");
			            # If invalid logger handle, don't try to write to it :) Instead print an error and croak and let the eval catch that.
			            $logger->logdie( "ERROR Invalid feeder option '$key': Expecting type to be Log::Log4perl::Logger\n" );
		            }
		        }
		        elsif ( $valid_options{$key} eq 'hash' ) {
		            if ( ref $options_ref->{$key} ne 'HASH' ) {
			            $logger->logdie("ERROR Invalid feeder option '$key': Expected type to be hash.\n");
		            }
	            }
	        }
	    }


	    # Read the properties from the config file into the config associated with this feeder object.
	    eval {
	        $feeder_TypedConfig_object = TypedConfig->new( $configuration_file );
    
	        # get properties common to all feeders - these are all required regardless of which feeder
	        #$config{RAPID_debug}                   = $feeder_TypedConfig_object->get_boolean ('RAPID_debug');
	        $config{api_timeout}                   = $feeder_TypedConfig_object->get_number  ('api_timeout');
	        $config{ws_client_config_file}         = $feeder_TypedConfig_object->get_scalar  ('ws_client_config_file');
	        $config{app_type}                      = $feeder_TypedConfig_object->get_scalar  ('app_type');
	        $config{auditing}                      = $feeder_TypedConfig_object->get_boolean ('auditing');
	       #$config{constrain_to_service_states}   = $feeder_TypedConfig_object->get_hash    ('constrain_to_service_states') ; # This functionality is removed for now.
	        $config{cycle_timings}                 = $feeder_TypedConfig_object->get_number  ('cycle_timings');
	        $config{enable_processing}             = $feeder_TypedConfig_object->get_boolean ('enable_processing');
	        $config{events_bundle_size}            = $feeder_TypedConfig_object->get_number  ('events_bundle_size');
	       #$config{feeder_services}               = $feeder_TypedConfig_object->get_hash    ('feeder_services') ;
	        $config{guid}                          = $feeder_TypedConfig_object->get_scalar  ('guid');
	       #$config{health_hostname}               = $feeder_TypedConfig_object->get_scalar  ('health_hostname');
	        $config{health_hostgroup}              = $feeder_TypedConfig_object->get_scalar  ('health_hostgroup');
	       #$config{license_check}                 = $feeder_TypedConfig_object->get_scalar  ('license_check');
	       #$config{license_check_user}            = $feeder_TypedConfig_object->get_scalar  ('license_check_user');
	       #$config{monitoring_server}             = $feeder_TypedConfig_object->get_scalar  ('monitoring_server');
	        $config{notifications_bundle_size}     = $feeder_TypedConfig_object->get_number  ('notifications_bundle_size');
	        $config{post_events}                   = $feeder_TypedConfig_object->get_boolean ('post_events');
	        $config{post_notifications}            = $feeder_TypedConfig_object->get_boolean ('post_notifications');
	        $config{update_hosts_statuses}         = $feeder_TypedConfig_object->get_boolean ('update_hosts_statuses');
	        $config{send_feeder_perf_data}         = $feeder_TypedConfig_object->get_boolean ('send_feeder_perf_data');

            # v 0.4.0  - only expect these for 710+ versions 
            if ( $config{RAPID_710_plus} ) {  
	            $config{force_crl_check}               = $feeder_TypedConfig_object->get_boolean ('force_crl_check');
            }

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
	        ## Validation of states: OK, UNSCHEDULED CRITICAL are only permissable ones for now
	        ## This is designed for possible future expansion of allowed states
	        #if ( any { $_ !~ /^(OK|UNSCHEDULED CRITICAL)$/i } keys %{$config{constrain_to_service_states} } ) {
	        #    $logger->logcroak("FATAL: Cannot read config file - illegal property name in constrain_to_service_states");
	        #}
	        ## Convert the keys to lowercase for use later
	        #%{ $config{constrain_to_service_states} } = map { lc $_ => $config{constrain_to_service_states}{$_} } keys %{ $config{constrain_to_service_states} };
	        %{ $config{constrain_to_service_states} } = (); # just leave it set but empty for now

	        # If feeder_services given, then need to get it as a hash
	       #if ( defined $config{feeder_services} ) {
		   #    $config{feeder_services} = { $feeder_TypedConfig_object->get_hash('feeder_services')  };
	       #}

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
			            $logger->logdie("FATAL: unsupported option type '$options_ref->{feeder_specific_properties}->{ $feeder_specific_property }' for option '$feeder_specific_property'\n");
		            }
		        }
	        }
	    }; # end eval on reading the config with TypedConfig
	    if ($@) {
	        chomp $@;
	        $@ =~ s/^ERROR:\s+//i;
	        #$logger->logcroak("FATAL Cannot read config file $configuration_file: $@");
	        $logger->logdie("FATAL Cannot read config file $configuration_file: $@\n");
	    }

        # This is required for things to make sense when sending metrics to all endpoints and backward compat after removing health_hostname from configs (part of v 0.5.0)
        $feeder_TypedConfig_object->{health_hostname} = $endpoint_name;

	    # Make all of the properties available to the feeder object
	    $config{properties} = $feeder_TypedConfig_object;
    
	    # Prepare GW::RAPID options.
	    %RAPID_options = ();
	    #$RAPID_options{logger}          = $logger if $config{RAPID_debug};
	    $RAPID_options{timeout}         = $config{api_timeout};
	    $RAPID_options{access}          = $config{ws_client_config_file};

        # v 0.4.0 : If the version of RAPID is >= 0.7.6, then it's ok to add the force_crl_check option
        if ( $config{RAPID_710_plus} ) {
	        $RAPID_options{force_crl_check} = $config{force_crl_check};
        }

	    # Establish a REST API connection and object.
	    $config{rest_api} = GW::RAPID->new( undef, undef, undef, undef, $feeder_name, \%RAPID_options );
	    if ( not $config{rest_api} ) { 
            $@ =~ s/\n//g if defined $@;
            $logger->logdie("ERROR - Failed to initialize Groundwork REST API. $@\n");
        }

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
		        $logger->logdie("ERROR - Could not set the guid property.\n");
	        }
	    }
	    else {
	        ## This is critical info, so it doesn't hurt to save an extra copy
	        ## in the log file in case it ever gets destroyed in the config file.
	        $logger->trace("TRACE  guid property is '$config{guid}'");
	    }
    
	    # Check that the application type in app_type, build it if not.
	    # FIX MINOR:  Why are there two successive calls here?
	    #$self->create_app_type(); # FIXED :)
	    if ( not $self->create_app_type() )  {
	        $config{rest_api} = undef;
	        $logger->logdie("ERROR - Could not create/update application type.\n");
	    }
    
	    $logger->trace("TRACE $feeder_name feeder object initialized.");
    
    }; # end eval on Feeder object creation

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

    # Release our handle to the REST API, if it hasn't already been released via some other means.
    $self->{rest_api} = undef if exists $self->{rest_api};

    # Can give confusing messages.
    #$self->{logger}->trace("TRACE Feeder object destroyed.") if $self->{logger};
    return 1;
}

# ----------------------------------------------------------------------------
sub create_app_type
{
    # Upserts the app type. Returns 1 on success, 0 otherwise
   
    my ( $this ) = @_;
    my ( @application_types, %outcome, @results, %results ) ;

    # GWMON-12689 update: only upsert the app type if it doesn't already exist
    if ( $this->{rest_api}->get_application_types( [ $this->{app_type} ], {}, \%outcome, \%results ) ) {
        $this->{logger}->trace( "Application type $this->{app_type} was found to exist and will not be updated" );
        return 1;
    }

    @application_types = (
			    {
				    'name'                    => $this->{app_type},
				    'displayName'             => $this->{app_type},
				    'description'             => "Feeder $this->{feeder_name} application type",
				    'stateTransitionCriteria' => 'Device;Host',
			    }
    );

    # Create the feeder application type
    if ( not $this->{rest_api}->upsert_application_types( \@application_types, {}, \%outcome, \@results ) ) {
	    $this->{logger}->error( "Something went wrong upserting application type $this->{app_type}: " . Dumper \%outcome, \@results );
	    return 0;
    }

    return 1;
}

# ----------------------------------------------------------------------------
sub set_guid_property
{
    # Sets the guid property in the feeder config file.
    # Returns 1 on success, 0 otherwise

    my ( $this, $configuration_file ) = @_;
    my ( @config, $line, $ug, $uuid ) ;

    # Read in the config file
    open (CONF, $configuration_file) or do {
	    $this->{logger}->error("set_guid_property() could not open the config file '$configuration_file' for reading: $!");
	    return 0;
    };
    @config = <CONF>;
    close CONF;

    # Open a temporary config file for writing.  We do this so as not to utterly destroy the
    # old copy if we get interrupted before we're done constructing the updated copy.
    my $temp_config_file = "$configuration_file.new";
    open (CONFOUT, ">", $temp_config_file) or do {
	    $this->{logger}->error("set_guid_property() could not open the config file '$temp_config_file' for writing: $!");
	    return 0;
    };

    my ($dev, $ino, $mode, $nlink, $uid, $gid) = stat $configuration_file;
    if (not defined $mode) {
	    $this->{logger}->error("set_guid_property() could not get the mode of the config file '$configuration_file': $!");
	    return 0;
    }

    # Kill any filetype info, and further restrict the permissions to disallow any pointless
    # set-id/sticky or execute permissions and any group-write or other-write permissions.
    $mode &= 0644;

    # Set the mode of the new file to the mode of the old file, perhaps sensibly restricted.
    unless ( chmod( $mode, $temp_config_file ) ) {
	    $this->{logger}->error("set_guid_property() could not set the mode of the config file '$temp_config_file': $!");
	    return 0;
    }

    # Set the ownership of the new file to that of the old file.  This should effectively be a
    # no-op, because we should be running as the owner of the old file.  But if this fails, then
    # we know we cannot put the config file back as it was without change, so we abort.
    unless (chown $uid, $gid, $temp_config_file) {
	    $this->{logger}->error("set_guid_property() could not set the ownership of the config file '$temp_config_file': $!");
	    return 0;
    }

    # Find the guid line and update its value.
    foreach $line (@config) {
	    if ( $line =~ m{ ^\s*guid\s*=.*$  }xms ) {
	        use APR::UUID ();
	        $uuid = APR::UUID->new->format; # APR::UUID is already part of stock GW 7.0.2, so use instead of Data::UUID
	        $line = "guid = $uuid\n";
	        $this->{logger}->info("INFO  Setting guid property to '$uuid'");
	    }
	    # TBD perhaps use IO::File instead
	    unless ( print CONFOUT $line ) {
	        $this->{logger}->error("set_guid_property() could not write to the config file '$temp_config_file': $!");
	        return 0;
	    }
    }

    # A close() may flush Perl's I/O buffers, so writing can occur here, too,
    # and the success of this operation must be checked as well.
    unless ( close CONFOUT ) {
	    $this->{logger}->error("set_guid_property() could not write to the config file '$temp_config_file': $!");
	    return 0;
    }

    # Perform an atomic rename of the new config file.  By standard UNIX rename semantics,
    # the end result is that you either get the entire new file or the entire old file at
    # the name of the old file, depending on whether or not the rename succeeded.  But you
    # never can get any partial file as a result.  This provide essential safety.
    unless ( rename( $temp_config_file, $configuration_file ) ) {
	    $this->{logger}->error("set_guid_property() could not rename the updated config file '$temp_config_file': $!");
	    return 0;
    }

    # Update feeder object guid property now that it's been updated here.
    $this->{properties}->{guid} = $uuid;
    $this->{guid} = $uuid;

    return 1;
}

# ----------------------------------------------------------------------------
sub feeder_get_hostgroups
{
    # Takes a ref to a hash of hostnames: { hostname=>undef, hostname2=>undef ... }
    # and for each hostname, populates it with a hash of hostgroups to which the host belongs
    # in Foundation: { hostname => {hg1, hg2}, hostname2 => {hg3, hg4} ... }
    # returns 1 success 0 otherwise

    my ( $this, $hashref_hostnames ) = @_;

    my ( %outcome, %results, @hostgroups, $hostgroup_ref, $arrayref_hosts, $hostgroup_name ) ;

    # Get all hostgroups in detail all in one go - TBD chunk this up into smaller bits
    if ( not $this->{rest_api}->get_hostgroups( [], { depth=>'shallow'}, \%outcome, \%results ) ) {
	    if ( defined $outcome{response_code} and $outcome{response_code} ne '404' ) { # zero hostgroups - expect a 404
	        $this->{logger}->error( "Something went wrong getting hostgroups: " . Dumper \%outcome, \%results );
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

# ----------------------------------------------------------------------------
sub feeder_get_hostgroup_members
{
    # Takes a hash of hostgroups to get members for : { "hg1" => undef, "hg2"=>undef ,... }
    # Does a RAPID::get_hostgroups and returns a hash : { "hg1"=>{ "members"=> { 'h1'=>undef,...} } , ... }
    my ( $this, $hostgroups_hash_ref ) = @_;
    my ( %outcome, %results , @hostgroups ) ;
    if ( not $this->{rest_api}->get_hostgroups( [ keys %{$hostgroups_hash_ref} ] , { }, \%outcome, \%results ) ) {
	    if ( defined $outcome{response_code} and $outcome{response_code} ne '404' ) { # zero hostgroups - expect a 404
	        $this->{logger}->error( "Something went wrong getting hostgroups members: " . Dumper \%outcome, \%results );
	        return 0;
	    }
    }
    
    foreach my $hostgroup ( keys %results ) {
        foreach my $hostname_hash ( @{$results{$hostgroup}{hosts}} ) { 
            $hostgroups_hash_ref->{$hostgroup}->{members}{ $hostname_hash->{hostName} } = undef;
        }
    }

    return 1;

}

# ----------------------------------------------------------------------------
sub feeder_post_notifications
{
    # Takes a reference to an array of REST API consumable notification data property hashes suitable for either
    # host or service notifications, and a type (either host or service)
    # and does a bundled sending of them ie this wrapper provides bundling.
    # TBD smarter way to do this if time - in the %types hash, store the function names - can then dry up the if $type eq logic

    my ( $this, $type, $arrayref_notifications ) = @_;
    my ( @notifications_bundle, %outcome, @results, @built_notifications, $not, %built_notification );
    my  %types = ( host => 1, service => 1 );

    if ( not exists $types{$type} ) {
	    $this->{logger}->error( "feeder_post_notifications() bailing due to invalid type '$type', expected " . join ",", keys %types );
	    return 0;
    }

    while ( @notifications_bundle = splice @{$arrayref_notifications}, 0, $this->{notifications_bundle_size}) {

	    $this->{logger}->debug( "feeder_post_notifications() starting to process bundle of " . ($#notifications_bundle + 1 ) . " notification(s)" );

	    # Validate and build notifications structure for API
	    @built_notifications = ();
	    foreach $not ( @notifications_bundle ) {
	        if ( $type eq 'host' ) {
		        if ( any { not defined $_ } $not->{hostName}, $not->{notificationType}, $not->{hostState}, $not->{hostOutput} ) {
		            $this->{logger}->error("Missing expected host notification field - skipping this notification object: " . Dumper $not ) ;
		            next; # TBD return instead ?
		        }
		        # Build required fields - straight copy
		        %built_notification = %$not;
	        }
	        if ( $type eq 'service' ) {
		        if ( any { not defined $_ } $not->{hostName}, $not->{notificationType}, $not->{serviceDescription}, $not->{serviceOutput}, $not->{serviceState} ) {
		            $this->{logger}->error("Missing expected service notification field - skipping this notification object: " . Dumper $not ) ;
		            next; # TBD return instead ?
		        }
		        # Build required fields - straight copy
		        %built_notification = %$not;
	        }
    
	        push @built_notifications, { %built_notification };
	    }
    
	    # Create the host notifications
	    if ( $type eq 'host' ) {
	        if ( not $this->{rest_api}->create_noma_host_notifications(\@built_notifications, {}, \%outcome, \@results) ) {
		        $this->{logger}->error( "Something went wrong creating host notifications: " . Dumper \%outcome, \@results );
		        return 0;
	        }
            else { 
                $this->{logger}->trace( "create_noma_host_notifications results outcome and results : " . Dumper \%outcome, \@results );
            }
	    }
    
	    # Create the service notifications
	    if ( $type eq 'service' ) {
	        if ( not $this->{rest_api}->create_noma_service_notifications(\@built_notifications, {}, \%outcome, \@results) ) {
		        $this->{logger}->error( "Something went wrong creating service notifications: " . Dumper \%outcome, \@results );
		        return 0;
	        }
            else { 
                $this->{logger}->trace( "create_noma_service_notifications results outcome and results : " . Dumper \%outcome, \@results );
            }
	    }
    
    }

    return 1;
}

# ----------------------------------------------------------------------------
sub feeder_post_events
{
    # Takes a reference to an array of REST API consumable event data property hashes suitable for either
    # posting host or service events, and a type (either host or service)
    # and does a bundled posting of them ie this wrapper provides bundling
    # TBD smarter way to do this if time - in the %types hash, store the function names - can then DRY up the if $type eq logic

    my ( $this, $type, $arrayref_events ) = @_;
    my ( @events_bundle, %outcome, @results, @built_events, $event, %built_event, $now );
    my  %types = ( host => 1, service => 1 );

    if ( not exists $types{$type} ) {
	    $this->{logger}->error( "feeder_post_events() bailing due to invalid type '$type', expected " . join ",", keys %types );
	    return 0;
    }

    $now = strftime( '%Y-%m-%dT%H:%M:%S%z', localtime );
    while ( @events_bundle = splice @{$arrayref_events}, 0, $this->{events_bundle_size}) {
	    $this->{logger}->debug( "feeder_post_events() starting to process bundle of " . ($#events_bundle + 1 ) . " event(s)" );

	    @built_events = ( );

	    foreach $event ( @events_bundle ) {
	        if ( $type eq 'host' ) {
		        if ( any { not defined $_ } $event->{device}, $event->{host}, $event->{monitorStatus}, $event->{severity}, $event->{textMessage} ) {
		            $this->{logger}->error("Missing expected host event field - skipping this event object: " . Dumper $event ) ;
		            next;
		        }
		        # Build required fields - straight copy - make sure to pass in correct values eg don't use PENDING but UNREACHABLE
		        %built_event = %$event;
	        }
	        if ( $type eq 'service' ) {
		        if ( any { not defined $_ } $event->{device}, $event->{host}, $event->{monitorStatus}, $event->{severity}, $event->{textMessage}, $event->{service} ) {
		            $this->{logger}->error("Missing expected host service field - skipping this event object: " . Dumper $event ) ;
		            next;
		        }
		        # Build required fields - straight copy
		        %built_event = %$event;
	        }

	        # reportDate: if not supplied, create it
	        if ( not defined $event->{reportDate} ) {
		        $built_event{reportDate} = $now;
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
		        $this->{logger}->error( "Something went wrong creating host events: " . Dumper \%outcome, \@results );
		        return 0;
	        }
	    }

	    # Create the service events
	    if ( $type eq 'service' ) {
	        if ( not $this->{rest_api}->create_events( \@built_events, {}, \%outcome, \@results ) ) {
		        $this->{logger}->error( "Something went wrong creating service events: " . Dumper \%outcome, \@results );
		        return 0;
	        }
	    }
    }

    return 1;
}

# ----------------------------------------------------------------------------
sub fmee()
{
    # This little routine returns 0 if there's an entry for the caller routine name
    # in the <fmee> block of the endpoint's config.
    # To use, just add this line to the routine to emulate failure from :
    #     return 0 if not $this->fmee()
    # and add the routine name into the <fmee> block.
    my ( $this ) = @_;
    my $caller = (caller(1))[3]; # get the caller's sub name
    $caller =~ s/^.*:://g; # don't really care about the GW::Feeder:: stuff 
   #if ( exists $this->{properties}->{fmee}->{timestamp} ) { 
   #    print "++++++ Compare $main::fmee_timestamp with " . Dumper $this->{properties}->{fmee}->{timestamp}; 
   #}
    # If there's a sub in the fmee config block with the same name ...
    if ( exists $this->{properties}->{fmee}->{$caller} ) { 

        # If the timestamp of the dataset being processed matches the fmee timestamp to emulate failure at, then fail 
        if ( exists $this->{properties}->{fmee}->{timestamp} ) { 
            #return 0 if $main::fmee_timestamp == $this->{properties}->{fmee}->{timestamp};
            if ( $main::fmee_timestamp == $this->{properties}->{fmee}->{timestamp} ) {
                #print "Emulating failure at timestamp $this->{properties}->{fmee}->{timestamp}\n";
                return 0;
            }
        }

        # else no specific timestamp defined, so just fake the failure immediately
        else { 
            return 0; 
        }
    }
    return 1;
}

# ----------------------------------------------------------------------------
sub feeder_upsert_hosts
{
    # Create and/or update a set of hosts.
    # Supports audit trail.
    # Args
    #  - a ref to an array of host hashes
    #  - a ref to an options hash
    #  - an error ref that will be set here if necessary
    # Returns 
    #  - 1 on success
    #  - 0 otherwise and error_ref

    my ( $this, $hosts_ref, $options_ref, $error_ref ) = @_;
    my ( $host_ref, $now, %built_host, @built_hosts, $host, @hosts_to_check );
    my ( @hosts_bundle, %outcome, @results, %these_hosts_exist, %these_hosts_dont_exist );
    my ( %upserted_successfully, $result_hashref );
    my ( $event_severity, @host_events, $host_event, @created_host_events, @post_these_host_events, $licensing_error );

    return 0 if not $this->fmee() ; # for testing and QA purposes

    # First make note of whether each host exists or not and therefore whether it will need adding.
    # This information will determine which properties to use in the upsert_hosts() call.
    #
    # Build a list of hostnames to check existence for,
    # whilst checking for required properties that are used here and later.
    foreach $host_ref ( @{$hosts_ref} ) {
	    # TBD also need to add a check for things that should NOT be passed in here too.
	    # However, its nice to allow overrides for health services internally too, so leave as-is for now.
	    if ( not defined $host_ref->{hostName} or not defined $host_ref->{monitorStatus} ) {
	        $this->report_feeder_error( "ERROR Expected hostName and monitorStatus in host object" . Dumper $host_ref );
	        ${$error_ref} = "ERROR Expected hostName and monitorStatus in host object" . Dumper $host_ref ;
	        return 0;
	    }
	    else {
	        push @hosts_to_check, $host_ref->{hostName} ;
	    }
    }

    # Check existence of these hosts. This is needed in order to construct different api data structures depending upon whether a host exists or not.
    #$this->{logger}->debug("starting feeder_upsert_hosts() hosts existence checking ....");
    if ( not $this->check_foundation_objects_existence( 'hosts', \@hosts_to_check, \%these_hosts_exist, \%these_hosts_dont_exist ) ) {
	    $this->report_feeder_error( "ERROR Checking Foundation hosts existence." );
	    ${$error_ref} = "ERROR Checking Foundation hosts existence.";
	    return 0;
    }
    #$this->{logger}->debug("ending feeder_upsert_hosts() hosts existence checking ....");

    # Process each host object by 'building' it ie preparing its fields for REST API call
    $now = strftime( '%Y-%m-%dT%H:%M:%S%z', localtime );
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
	    # The only properties needed if upserting an existing host, are: lastCheckTime, hostName, monitorStatus
	    # and monitorStatus is questionable as it will override any other method such as fping.

    
	    # Regardless of whether host exists already or not ...
	    $built_host{hostName}      = $host_ref->{hostName};
	    $built_host{monitorStatus} = $host_ref->{monitorStatus};
	    if ( not defined $host_ref->{lastCheckTime} ) { # lastCheckTime: if not supplied, create it
	        $built_host{lastCheckTime} = $now;
	    }
	    else {
	        $built_host{lastCheckTime} = $host_ref->{lastCheckTime} ;
	    }
        # This is always required now regardless of whether or not the host exists,
        # otherwise without it whilst the api will return success, it won't update (and the docs say it's required but it's optional on existing updates it seems)
	    $built_host{appType} = ( defined $host_ref->{appType} ) ? $host_ref->{appType} : $this->{properties}->{app_type}; 


	    # So, only upsert with these additional things if the host is going to be created by this upsert
	    if ( not exists $these_hosts_exist{ $host_ref->{hostName} } ) {
	        # Use values if they were specified, defaults if not
	        $built_host{deviceDisplayName}    = ( defined $host_ref->{deviceDisplayName} )    ? $host_ref->{deviceDisplayName}    : $host_ref->{hostName};
	        $built_host{deviceIdentification} = ( defined $host_ref->{deviceIdentification} ) ? $host_ref->{deviceIdentification} : $host_ref->{hostName};
    
	        # GWMON-11737
	        $built_host{monitorServer}        = 'localhost';
    
	        $built_host{appType} = ( defined $host_ref->{appType} ) ? $host_ref->{appType} : $this->{properties}->{app_type}; # TBD remove this since its now added above in all cases
	        $built_host{agentId} = ( defined $host_ref->{agentId} ) ? $host_ref->{agentId} : $this->{properties}->{guid};
	        $built_host{properties}{LastStateChange} = $built_host{lastCheckTime}; # this too ... for the Up since in sv to work properly # Version 0.3.8

            # v 0.4.1
	        #$built_host{description} = "Added by $this->{feeder_name} - shows up";            # this doesn't show up in SV ..
	        #$built_host{properties}{Alias} = "Added by $this->{feeder_name}";      # ... but this will make it show up but also need ....
	        $built_host{description} = ( defined $host_ref->{description} ) ? $host_ref->{description} : "Added by $this->{feeder_name} running on $feeder_host";           
	        $built_host{properties}{Alias} = $built_host{description}; # this doesn't seem to work anymore 2/10/16
            # 2/10/16 Hm that no longer works so using this too ...
	        $built_host{properties}{LastPluginOutput} = $built_host{description};
    
	        # GWMON-11605 create a pending event and an initial state event for the to-be created host:
	        #   Event : host->pending
	        #   Event : host->$host_ref->{monitorStatus}
	        if ( $built_host{monitorStatus} ne 'UP' ) { $event_severity = 'SERIOUS' ;  } else { $event_severity = 'OK' ; }
	        push @host_events, {    # Initial state event (pending will be added later)
		        'appType'         => $built_host{appType}, 
		        'device'          => $built_host{deviceIdentification},
		        'host'            => $built_host{hostName},
		        'monitorStatus'   => $built_host{monitorStatus},
		        'reportDate'      => $built_host{lastCheckTime},
		        'firstInsertDate' => $built_host{lastCheckTime},
		        'severity'        => $event_severity,
		        'textMessage'     => 'Host creation initial state event (FUH)'
	        };

	        # Add the prepared host data structure to the build_hosts array that will be used in the REST API upsert_hosts call
	        # GWMON-11617: always set an initial status if the host is being created, regardless of update_hosts_statuses
	        push @built_hosts, { %built_host };
	    }
	    else {
	        # GWMON-11617: if host exists already, then only update its status if update_hosts_statuses is true
	        if ( $this->{update_hosts_statuses} or $host_ref->{hostName} eq $this->{properties}->{health_hostname} ) {
		        $this->{logger}->trace( "Host $host_ref->{hostName} status will be updated" );

                # If the status of the host has changed, then need to add a lastStateChange prop to sthat the status viewer <state> Since data updates correctly . Version 0.3.8
                my %outcome; my %results;
                if ( not $this->{rest_api}->get_hosts( [ $built_host{hostName} ] , { }, \%outcome, \%results ) ) { # get the host's state
                	$this->{logger}->error( "ERROR something went wrong getting hosts: " . Dumper \%outcome, \@results );
                    ${$error_ref} = "ERROR something went wrong getting hosts: " . Dumper \%outcome, \@results;
                	return 0;
                }
                if ( $built_host{monitorStatus} ne $results{$built_host{hostName}}{monitorStatus} ) {  # if changed, add lastStateChange prop
                	$built_host{properties}{LastStateChange} = $built_host{lastCheckTime};
                }

                # v 0.4.1
                # Sometimes useful to be able to update the host description too, eg logbridge feeder
                if ( defined $built_host{description} ) { 
                    $built_host{description} = $built_host{description};
	                $built_host{properties}{Alias} = $built_host{description}; # to make it show in status viewer
                }
              	 
		        push @built_hosts, { %built_host };
	        }
	        else {
		        $this->{logger}->trace( "update_hosts_statuses is false, host $host_ref->{hostName} status will not be updated" );
	        }
	    }

    };

    # Do complete batch license check here ?
    # Or move the license checking into the auditing section below and allow bundle-wise license filling ?
    # Preferable Answer: in each batch - thats closer to what the api will eventually do perhaps ?
    # Correct answer: what the requirements spell out - ie check that adding all hosts won't exceed license limits
    # NOTE: check_license() figures out how many hosts would actually need adding ie total - existing = need adding
    # and return failure if exceeds license limits. 
    # Note that in this feeder, hosts = devices. In the GW model, these are different, and there's a 
    # many-to-one relationship of devices-to-hosts (check that!). 

    # Only check license if there are built_hosts...
    if ( ($#built_hosts >= 0) and (not $this->check_license( \@built_hosts, \$licensing_error ))  ) {
	    $this->report_feeder_error("LICENSE ERROR - Adding " . ( $#built_hosts + 1) . " host(s) - none of these hosts will be added. $licensing_error");
	    ${$error_ref} = "LICENSE ERROR - Adding " . ( $#built_hosts + 1) . " host(s) - none of these hosts will be added. $licensing_error";
	    return 0;
    }

    # To be efficient, will update/insert hosts in bundles, including auditing hosts existence testing
    while ( @hosts_bundle = splice @built_hosts, 0, $this->{host_bundle_size}) {

	    $this->{logger}->debug( "feeder_upsert_hosts() starting to process bundle of " . ($#hosts_bundle + 1 ) . " host(s)" );

	    @created_host_events = ();

	    # If auditing, then need to check whether the hosts in this bundle exists or not prior to upserting it,
	    # so that later can then test if the host was added to Foundation anew.
	    # For those hosts that do NOT exist, make a note that they will be added.
	    # After the REST API call to actually try and add hosts, then check the results to see which actually were added,
	    # and then update the audit trail finally. JIRA GWMON-11572 to simplify auditing.
    
        #print Dumper \@hosts_bundle;
	    # Run the upsert on the hosts bundle
	    if ( not $this->{rest_api}->upsert_hosts( \@hosts_bundle, $options_ref, \%outcome, \@results ) ) {
	        $this->{logger}->error( "Something went wrong upserting hosts: " . Dumper \%outcome, \@results );
	        ${$error_ref} = "Something went wrong upserting hosts: " . Dumper \%outcome, \@results ;
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
		            if ( defined $upserted_successfully{$host} ) {
			            $this->{audit_trail}->{hosts}{$host}{created}{host}{added} = 1;
			            ## GWMON-11605 create a set of pending and initial state events
			            ## create the PENDING state event.
			            ## Note that the pending event needs to come before the initial state event
			            ## so 1 second is subtracted. Could do this a different way obviously.
			            foreach $host_event (@host_events) {
			                if ( $host_event->{host} eq $host ) {
				                push @created_host_events,
				                {
				                    'appType'         => $host_event->{appType},
				                    'device'          => $host_event->{device},
				                    'host'            => $host_event->{host},
				                    'monitorStatus'   => 'PENDING',
				                    'reportDate'      => subtract_a_second( $host_event->{reportDate}, $sql_time_format ),
				                    'firstInsertDate' => subtract_a_second( $host_event->{reportDate}, $sql_time_format ),
				                    'severity'        => 'OK',
				                    'textMessage'     => 'Host creation PENDING event (FUH)'
				                };
             
				                # create the initial host state event
				                push @created_host_events, $host_event;

			                }
			            }
		            }
		        }
	        }
	        else {
		        $this->{logger}->warn( "WARN  No results from upsert_hosts API call. Outcome was: " . Dumper \%outcome );
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
	        ${$error_ref}  = "ERROR Failed to create events during upserting of hosts";
	        return 0;
	    }
    }

    return 1;
}

# ----------------------------------------------------------------------------
sub feeder_upsert_bizservices
{
    # Create and/or update a set of hosts and their services using the biz/services api.
    #
    # Args : 
    # - a ref to an array of host hashes that contain services, host status and other props etc , eg : 
    #   [
    #     'host1' => {
    #                    'services' => [
    #                                   {
    #                                     'service' => 'errors',
    #                                     'message' => 'Couldn\'t create feeder object for endpoint \'another\' - updating its retry cache and ending processing attempt. ',
    #                                     'status' => 'UNSCHEDULED CRITICAL'
    #                                     < possibly other key values here >
    #                                   } ,  
    #                                   ...
    #                                 ],
    #                    'hostgroup'  => "hg"  - the hostgroup for host1 # optional
    #                },
    #     'host2' => {
    #                    'services'  => [ ... ]
    #                    'hostgroup'  => "hg"  - the hostgroup for host2 # optional
    #                }, 
    #     ...
    #   ]
    #   Note that it's an array to preserve order.
    #
    # Returns 1 on success, 0 otherwise.
    #
    # NOTES:
    # - The host's hostgroup doesn't need to be specified. If it's not, and the host is being created, it WILL NOT be visible in status viewer
    #   and you'll get an error if you try to access the host via status viewer, cos it'll have no hg :)
    # - Updates the internal feeder audit trail.
    
    my ( $this, $bizservices_ref, $options_ref ) = @_;
    my ( %outcome, @results ) ;

    return 0 if not $this->fmee() ; # for testing and QA purposes

    # Build a data structure that can be consumed by RAPID::upsert_bizservices()
    my ( @bizservices, %bizservice, @services, @hosts, @built_services, %built_bizsvc, %built_svcperf );

    @built_services = ();  
    foreach my $host_hash ( @{$bizservices_ref} ) {

        # grab the host name - expected structure has only one key in each hash here, the key being the hostname
        my $host = (keys %{$host_hash})[0];

        # Construct the datastructure to be passed on through to biz/services api (via RAPID::upsert_bizservices).
        %built_bizsvc  = ();
        @services = @{$host_hash->{$host}->{services}};
        foreach my $shash ( @services ) {
            
            # For upserting hosts and services ...
            $built_bizsvc{host}      = $host;
            $built_bizsvc{service}   = $shash->{service};
            $built_bizsvc{message}   = $shash->{message};
            $built_bizsvc{status}    = $shash->{status};
            $built_bizsvc{hostGroup} = $host_hash->{$host}->{hostgroup} if defined $host_hash->{$host}->{hostgroup};
            $built_bizsvc{device}    = $host;
            $built_bizsvc{appType}   = $this->{app_type};
            $built_bizsvc{agentId}   = $this->{guid};
            $built_bizsvc{setStatusOnCreate}  = 1; # this is required to make this pop into the desired state when they are created by biz/services api
            $built_bizsvc{'properties'} = $shash->{properties} if exists $shash->{properties};

            push @built_services, { %built_bizsvc };

        }
    }

    # Now have a payload suitable for sending to RAPID::upsert_bizservices 
    $this->{logger}->trace( "Commands being sent into upsert_bizservices : " . Dumper \@built_services ) ;
    if ( not $this->{rest_api}->upsert_bizservices( \@built_services, {}, \%outcome, \@results ) ) {
     	$this->{logger}->error( "Something went wrong upserting through RAPID::upsert_bizservices : " . Dumper \%outcome, \@results);
        return 0;
    }
    $this->{logger}->trace( "Results from upsert_bizservices : " . Dumper \%outcome, \@results ) ;

    # Build audit trail for created hosts and/or services by parsing the results of the api call
    if ( $this->{auditing} ) {
   
        if ( @results ) { # ... First, @results has to be defined, else assuming that /api/bizservices completely failed

            foreach my $result_hashref ( @results ) { 

                # Good for cases during dev when api is updating and this field isn't there yet on an endpoint
                if ( not exists $result_hashref->{message} ) {
                    $this->{logger}->error( "Expected to see a 'message' field for bizservices result - skipping result block " . Dumper $result_hashref );
                    next;
                }
                next if $result_hashref->{message} !~ /^insert$/i;  # only care about creation of new things for audit
                next if $result_hashref->{status} !~ /^success$/i;  # only care about things that were successful
    
                # If have a defined non empty entity field  ...
                if ( exists $result_hashref->{entity} and $result_hashref->{entity} ) {
                    # doesn't contain a : then it's referring to a host ( a service has the format 'host:service' vs just a host 'host' )
                    if ( $result_hashref->{entity} !~ /:/ ) { 
			            $this->{audit_trail}->{hosts}{ $result_hashref->{entity}  }{created}{host}{added} = 1;
                    }
                    else { # otherwise it's a host service
                        my ( $host, $service ) = split( /:/, $result_hashref->{entity} );
			            $this->{audit_trail}->{hosts}{ $host }{created}{services}{ $service } = 1;
                    }
                }
            }
        }
        else {
            # Already covered the failure case above hopefully - revisit this TBD case
            # print "No results. Here's the outcome: " . Dumper \%outcome;
        }
    }


    return 1;
}

# ----------------------------------------------------------------------------
sub feeder_send_metrics_perfdata 
{
    # Tries to make graphs for feeder metrics services.
    # Takes the same data structure used by feeder_upsert_bizservices,
    # builds a set of perf data objects to be sent into RAPID::create_performance_data 
    # Returns 1 on success, 0 otherwise.

    my ( $this, $bizservices_ref, $options_ref ) = @_;
    my ( %outcome, @results, $host_hash, $host, $shash, %built_svcperf, @built_perfdata, @services, $perf_item ) ;

    if ( not $this->{send_feeder_perf_data} ) {
        $this->{logger}->debug( "Sending of metrics perf data is disabled - no metrics perf data will be sent." );
        return 1;
    }
        
    $this->{logger}->debug( "Sending metrics perf data" );

    return 0 if not $this->fmee() ; # for testing and QA purposes

    foreach $host_hash ( @{$bizservices_ref} ) {

        # grab the host name - expected structure has only one key in each hash here, the key being the hostname
        $host = (keys %{$host_hash})[0];

        # Construct the datastructure to be passed on through to perfdata routine
        @services = @{$host_hash->{$host}->{services}};
        foreach $shash ( @services ) {
            if ( exists $shash->{perfval} ) {
                %built_svcperf = ();
                foreach $perf_item ( keys %{$shash->{perfval}} ) {
                    $built_svcperf{appType}     = $this->{app_type},
                    $built_svcperf{label}       = $perf_item , # "A text string label describing this service name that is attached to the plotted line graph" 
                    $built_svcperf{serverName}  = $host, 
                    $built_svcperf{serverTime}  = time, 
                    $built_svcperf{serviceName} = $shash->{service},
                    $built_svcperf{value}       = $shash->{perfval}->{$perf_item};
                    push @built_perfdata, { %built_svcperf } ;
                }
            }
        }
    }

    #print "Built bizservices  = " . Dumper $bizservices_ref;
    #print "Built perfdata = " . Dumper \@built_perfdata;

    # If have anything in the perf buffer, send that too
    if ( @built_perfdata ) { 
        if ( not $this->{rest_api}->create_performance_data( \@built_perfdata, {}, \%outcome, \@results ) ) {
     	    $this->{logger}->error( "Something went wrong sending perfdata through RAPID::create_performance_data : " . Dumper \@built_perfdata, \%outcome, \@results);
            return 0;
        }
    }

    return 1;

}

# ----------------------------------------------------------------------------
sub feeder_send_perf_data
{
    # Sends performance data. Initially designed to send perf data from cacti feeder threshold services.
    # Returns 1 on success, 0 otherwise.

    my ( $this, $perfdata_ref ) = @_;
    my ( %outcome, @results, $service_hash, $service, $shash, %built_svcperf, @built_perfdata, @services, $perf_item ) ;
    my ( @perf_bundle, $bundle_size );

    # TBD eventually make this dependent on this setting.
    #if ( not $this->{send_feeder_perf_data} ) {
    #   $this->{logger}->debug( "Sending of metrics perf data is disabled - no metrics perf data will be sent." );
    #   return 1;
    #}
        
    $this->{logger}->debug( "Sending performance data" );

    return 0 if not $this->fmee() ; # for testing and QA purposes

    foreach $service ( keys %{$perfdata_ref} ) {

        $service_hash = $perfdata_ref->{ $service} ;

        %built_svcperf = ();
        $built_svcperf{serviceName} = $service;
        $built_svcperf{appType} = $this->{app_type};
        $built_svcperf{serverName}  = $service_hash->{serverName};
        $built_svcperf{serverTime}  = time;
        $built_svcperf{value}  = $service_hash->{value};
        $built_svcperf{label}  = $service_hash->{label};
        push @built_perfdata, { %built_svcperf } ;
    }

    #$this->{logger}->error( "Perf data: " .  Dumper \@built_perfdata );
    
    $bundle_size = 100; # TODO parameterize this via perfdata_bundle_size prop
    while ( @perf_bundle = splice @built_perfdata, 0, $bundle_size ) {
	$this->{logger}->debug( "feeder_send_perf_data() starting to process bundle of " . ($#perf_bundle + 1 ) . " perfdata" );
        if ( not $this->{rest_api}->create_performance_data( \@perf_bundle, {}, \%outcome, \@results ) ) {
     	    $this->{logger}->error( "Something went wrong sending perfdata through RAPID::create_performance_data : " . Dumper \%outcome, \@results);
            return 0;
        }
    }

    return 1;

}

# ----------------------------------------------------------------------------
sub feeder_delete_hostgroups
{
    # Takes an array of hostgroups to delete and tries to delete them with audit trail
    
    my ( $this, $hostgroups_ref, $options_ref ) = @_;
    my ( %outcome, @results, @hostgroups_to_check, %these_hostgroups_exist, %these_hostgroups_dont_exist, %deleted_successfully );

    if ( not $this->check_foundation_objects_existence( 'hostgroups', $hostgroups_ref, \%these_hostgroups_exist, \%these_hostgroups_dont_exist ) ) {
	    $this->report_feeder_error("ERROR Checking Foundation hostgroups existence");
	    return 0;
    }

    foreach my $missing_hg ( keys %these_hostgroups_dont_exist ) {
        $this->{logger}->warn("Hostgroup $missing_hg didn't exist and won't be deleted");
    }
    
	if ( not $this->{rest_api}->delete_hostgroups( [ keys %these_hostgroups_exist] , $options_ref, \%outcome, \@results ) ) {
	    $this->{logger}->error( "Something went wrong deleting hostgroups: " . Dumper \%outcome );
	    return 0;
	}


    if ( $this->{auditing} ) {
   
        if ( @results ) { # ... First, @results has to be defined, else assuming that deletion completely failed

            # Add successfully deleted hostgroups to a hash ie if the result entity was tagged as success
            foreach my $result_hashref ( @results ) {
                if ( $result_hashref->{status} eq 'success' ) {
                    $this->{audit_trail}->{hostgroups}{ $result_hashref->{entity} }{deleted} = 1;
                }
            }
    
        }
        else {
            # Already covered the failure case above hopefully - revisit this TBD case
            # print "No results. Here's the outcome: " . Dumper \%outcome;
        }
    }

    return 1;

}

# ----------------------------------------------------------------------------
sub feeder_clear_hostgroups
{
    # Clears all members out of a hostgroup
    # Args  
    #   - ref to a list of hostgroups
    # Returns 1 if cleared ok, 0 otherwise
    my ( $this, $hostgroup_array_ref ) = @_;
    my ( %outcome, @results );
    
    if ( not $this->{rest_api}->clear_hostgroups( $hostgroup_array_ref, {}, \%outcome, \@results ) ) {
	    $this->{logger}->error( "Something went wrong clearing hostgroups: " . Dumper \%outcome );
	    return 0;
    }
    
    return 1;
    
}

# ----------------------------------------------------------------------------
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
    my ( $hostgroup_ref, %built_hostgroup, @built_hostgroups, $hostgroup );
    my ( @hostgroups_bundle, %outcome, @results, @hostgroups_to_check, %these_hostgroups_exist, %these_hostgroups_dont_exist );
    my ( %upserted_successfully, $result_hashref );

    return 0 if not $this->fmee() ; # for testing and QA purposes

    # First make note of whether each hostgroup exists or not and therefore whether it will need adding.
    # This information will determine which properties to use in the upsert_hostgroups() call.
    #
    # Build a list of hostgroup names to check existence for,
    # whilst checking for required fields used here and later.
    # TBD also need to add a check for things that should NOT be passed in here too.
    # However, its nice to allow overrides for health services internally too, so leave as-is for now.
    foreach $hostgroup_ref ( @{$hostgroups_ref} ) {
	    if ( not defined $hostgroup_ref->{name} ) {
	        $this->report_feeder_error( "ERROR Expected name in hostgroup object" . Dumper $hostgroup_ref );
	        return 0;
	    }
	    else {
	        push @hostgroups_to_check, $hostgroup_ref->{name} ;
	    }
    }


    # Check existence of these hostgroups. This information will be used later when building the api data structures, which content varies depending
    # on whether hostgroup exists or not.
    #$this->{logger}->debug("starting hostgroups existence checking ....");
    if ( not $this->check_foundation_objects_existence( 'hostgroups', \@hostgroups_to_check, \%these_hostgroups_exist, \%these_hostgroups_dont_exist ) ) {
	    $this->report_feeder_error("ERROR Checking Foundation hostgroups existence");
	    return 0;
    }
    #$this->{logger}->debug("ending hostgroups existence checking ....");

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
	    # The only properties needed if upserting an existing hostgroup, are: name ( yes you can upsert an existing hostgroup with nothing )
	    # Required fields first
	    $built_hostgroup{name} = $hostgroup_ref->{name};
    
	    if ( not exists $these_hostgroups_exist{ $hostgroup_ref->{name} } ) {
	        ## Use values if they were specified, defaults if not
	        $built_hostgroup{alias}       = ( defined $hostgroup_ref->{alias} )       ? $hostgroup_ref->{alias}       : $hostgroup_ref->{name};
	        $built_hostgroup{description} = ( defined $hostgroup_ref->{description} ) ? $hostgroup_ref->{description} : $hostgroup_ref->{name};
	        $built_hostgroup{appType}     = ( defined $hostgroup_ref->{appType} )     ? $hostgroup_ref->{appType}     : $this->{properties}->{app_type};
	        $built_hostgroup{agentId}     = ( defined $hostgroup_ref->{agentId} )     ? $hostgroup_ref->{agentId}     : $this->{properties}->{guid};
	    }
    
	    # Add the prepared host datastructure to the build_hosts array that will be used in the REST API upsert_hosts call
	    push @built_hostgroups, { %built_hostgroup };

    };

    # To be efficient, will add hostgroups in bundles, including auditing hostgroups existence testing
    while ( @hostgroups_bundle = splice @built_hostgroups, 0, $this->{hostgroup_bundle_size}) {

	    $this->{logger}->debug( "feeder_upsert_hostgroups() starting to process bundle of " . ($#hostgroups_bundle + 1 ) . " hostgroup(s)" );

	    # If auditing, then need to check whether the hostgroups in this bundle exists or not prior to upserting it,
	    # so that later can then test if the hostgroups was added to Foundation anew.
	    # For those hostgroups that do NOT exist, make a note that they will be added.
	    # After the REST API call to actually try and add hostgroups, then check the results to see which actually were added,
	    # and then update the audit trail finally. Phew.
	    # This is now already done above, so carefully remove this later and retest auditing system
    
	    # Run the upsert on the hostgroups bundle
	    if ( not $this->{rest_api}->upsert_hostgroups( \@hostgroups_bundle, $options_ref, \%outcome, \@results ) ) {
	        $this->{logger}->error( "Something went wrong upserting hostgroups: " . Dumper \%outcome );
	        return 0;
	    }
    
	    # Update audit trail
	    # TBD in future version: add logic to cover hostgroup membership changes too perhaps
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
		        # print "No results. Here's the outcome: " . Dumper \%outcome;
	        }
	    }

    }

    return 1;
}

# ----------------------------------------------------------------------------
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
    my ( $service_ref, $now, %built_service, @built_services, $service );
    my ( @services_bundle, %outcome, @results );
    my ( %upserted_successfully, $result_hashref );
    my ( @hosts_to_check, %these_hosts_exist, %these_hosts_dont_exist );
    my ( %services_to_check, %these_services_exist, %these_services_dont_exist );
    my ( $result_entity_host, $result_entity_service, %upserted_hosts_services, $host ) ;
    my ( @service_states_filtered_bundle ) ;
    my ( @service_events, $service_event, @created_service_events, @post_these_service_events, $event_severity );
    my ( @created_host_events, @post_these_host_events );

    return 0 if not $this->fmee() ; # for testing and QA purposes

    # First make note of whether each host:service exists or not and therefore whether it will need adding.
    # This information will determine which properties to use in the upsert_hosts() call.
    #
    # Build a list of hostnames and services for existence checking,
    # whilst checking for required fields used now and later
    # TBD also need to add a check for things that should NOT be passed in here too.
    # However, its nice to allow overrides for health services internally too, so leave as-is for now.
    foreach $service_ref ( @{$services_ref} ) {
	    if ( any { not defined $_ } $service_ref->{description}, $service_ref->{hostName}, $service_ref->{monitorStatus} ) {
	        $this->{logger}->error("Missing expected service field: " . Dumper $service_ref ) ;
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
	    $this->report_feeder_error("ERROR Occurred while checking Foundation services existence");
	    return 0;
    }

    # Process each service object by 'building' it ie preparing its fields for REST API call
    $now = strftime( '%Y-%m-%dT%H:%M:%S%z', localtime );


    # Get all services so have all monitorStatus values which are then used in adding lastStateChange for existing and state changed services.
    # This is a major optimization. 2/15/16
    my %all_services;

    # 0.5.7 : For GWMON-12763 
    # On systems with large number of services such as on the eGain testbed where this issue was discovered, 
    # this call to get all services can take longer than the default api timeout so need to check and handle that scenario.
    # With this logic in place, an error is caught and propogated back to the health service.
    # TBD There are definitely other places that this timeout checking should be done, and this functionality should be made into more of a general solution
    # for other rest api calls. For now, the assumption is that this is one of the most expensive calls in Feeder and the longest, and it's also one of the
    # earliest perl cycle per endpoint, so this can act as a half decent trap for an api_timeout scenario. This needs improving later after 711. Load testing
    # in QA would also flush this sort of issue out. 
    # TBD One other note here- not sure why I'm using { hostname => $built_service{hostName} - its empty so go back and review this. For now its working in the tests so minor.
    $this->{logger}->debug( "Getting a list of all services from foundation ...");
    if ( not  $this->{rest_api}->get_services( [ ], { hostname => $built_service{hostName} , format => 'host,service' }, \%outcome, \%all_services ) ) { 
	if ( $outcome{response_code} == 500 and $outcome{response_error} =~ /read timeout/i ) {
		$this->report_feeder_error("ERROR Occurred while getting services - it looks like the REST API timed out - try increasing api_timeout");
	}
	else {
		$this->report_feeder_error("ERROR Occurred while getting services : " . Dumper \%outcome);
	}
	return 0; 
    }
    $this->{logger}->debug( "Done getting a list of all services from foundation");

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
	    # The only properties needed if upserting an existing host:service, are: lastCheckTime, hostName, monitorStatus
	    # and monitorStatus is questionable as it will override any other method such as fping.
    
	    # TBD discuss whether the feeder should update the host status
    
	    # Required fields first
	    $built_service{description}   = $service_ref->{description};
	    $built_service{hostName}      = $service_ref->{hostName};
	    $built_service{monitorStatus} = $service_ref->{monitorStatus};
    
	    # lastCheckTime: if not supplied, create it
	    if ( not defined $service_ref->{lastCheckTime} ) {
	        $built_service{lastCheckTime} = $now;
	    }
	    else {
	        $built_service{lastCheckTime} = $service_ref->{lastCheckTime} ;
	    }
    
	    # Not sure if it makes sense to create a default value here since don't know what the feeder frequency is
	    # Default to +10 mins ???
	    # nextCheckTime: if not supplied, create it
	    #if ( not defined $service_ref->{nextCheckTime} ) {
	    #    $built_service{nextCheckTime} = $now;
	    #}
	    #else {
	    #    $built_service{nextCheckTime} = $service_ref->{nextCheckTime} ;
	    #}

	    # Only add these properties if creating the service rather than updating an existing one
	    if ( not exists $these_services_exist{$service_ref->{hostName}}{$service_ref->{description}} ) {

            # required for Since time to work in status viewer properly
            $built_service{properties}{LastStateChange}  = $built_service{lastCheckTime} ;  # add to the properties hash, don't reset it ! # Version 0.3.8

	        $built_service{deviceIdentification} = ( defined $service_ref->{deviceIdentification} ) ? $service_ref->{deviceIdentification} : $service_ref->{hostName};
    
	        # GWMON-11737:  monitorServer must be fixed as 'localhost'.
	        # lastHardState is a NAGIOS vestige. It's actually required by the API.
	        $built_service{monitorServer} = 'localhost';
	        $built_service{stateType}     = ( defined $service_ref->{stateType} )     ? $service_ref->{stateType}     : 'HARD';
	        $built_service{checkType}     = ( defined $service_ref->{checkType} )     ? $service_ref->{checkType}     : 'PASSIVE';
	        $built_service{lastHardState} = ( defined $service_ref->{lastHardState} ) ? $service_ref->{lastHardState} : $service_ref->{monitorStatus};
	        $built_service{agentId}       = ( defined $service_ref->{agentId} )       ? $service_ref->{agentId}       : $this->{properties}->{guid};
	        $built_service{appType}       = ( defined $service_ref->{appType} )       ? $service_ref->{appType}       : $this->{properties}->{app_type};
    
	        # GWMON-11605 create a pending event and an initial state event for the to-be created service:
	        #   Event : service->pending
	        #   Event : service->$service_ref->{monitorStatus} (whatever that is in event speak)
	        # This only happens if creating the service
	        if ( $built_service{monitorStatus} ne 'OK' ) { $event_severity = 'SERIOUS' ;  } else { $event_severity = 'OK' ; }
	            push @service_events, {    # Initial state event (pending will be added later)
		            'service'         => $built_service{description},
		            'host'            => $built_service{hostName},
		            'monitorStatus'   => $built_service{monitorStatus},
		            'severity'        => $event_severity,
		            'appType'         => $built_service{appType},
		            'device'          => $built_service{deviceIdentification},
		            'reportDate'      => $built_service{lastCheckTime},
		            'firstInsertDate' => $built_service{lastCheckTime},
		            'textMessage'     => 'Service creation initial state event (FUS)'
	            };
	    }

        # If the status of the service has changed, then need to add a lastStateChange prop so that the status viewer <state> Since data updates correctly # Version 0.3.8
        else { # host/service exists - get it's status currently in GW and compare against incoming - ie detect state change and add lastStateChange prop
            #my %outcome; my %results;
            #if ( not $this->{rest_api}->get_services( [ $built_service{description} ], { hostname => $built_service{hostName} , format => 'host,service' }, \%outcome, \%results ) ) { # get service state
            # get service state - need to use query=> because [ service ] with special chars don't all uriescape and cause HTTP and then REST problems v 0.3.9
           #if ( not $this->{rest_api}->get_services( [ ], { query => "hostName='$built_service{hostName}' and description='$built_service{description}'" , format => 'host,service' }, \%outcome, \%results ) ) { 
            # 2/15/16 - this seems ok again to use special chars - no noticeable difference to performance though
            #           Instead the fix is to get all services above. Major improvement to feeder performance overall.
     #      if ( not $this->{rest_api}->get_services( [ $built_service{description} ], { hostname => $built_service{hostName} , format => 'host,service' }, \%outcome, \%results ) ) { # get service state
     #          $this->{logger}->error( "ERROR something went wrong getting services: " . Dumper \%outcome, \@results );
     #          return 0;
     #      }
            if ( $built_service{monitorStatus} ne $all_services{$built_service{hostName}}{$built_service{description}}{monitorStatus} ) { # if change, add lastStateChange prop
                $built_service{lastStateChange} = $built_service{lastCheckTime};
            }
        }
    
	    # Add the prepared host datastructure to the build_hosts array that will be used in the REST API upsert_hosts call
	    push @built_services, { %built_service };

    };

    # To be efficient, will add services in bundles, including auditing hostgroups existence testing
    while ( @services_bundle = splice @built_services, 0, $this->{service_bundle_size}) {

	    $this->{logger}->debug( "feeder_upsert_services() starting to process bundle of " . ($#services_bundle + 1 ) . " service(s)" );

	    # If auditing, or constraining to service states, then need to check whether the hosts+services in this bundle exists or not prior to upserting it,
	    # so that later can then test if the hos+services  was added to Foundation anew.
	    # Constraining to service states only applies to adding services ie creating them anew, so need to whether host+service exists/doesn't first.
	    # For those host+services  that do NOT exist, make a note that they will be added.
	    # After the REST API call to actually try to add host+service, then check the results to see which actually were added,
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
			            #print "KEEP this one: $service_ref->{monitorStatus}\n";
			            push @service_states_filtered_bundle, $service_ref;
		            }
		            else {
			            #print "DON'T KEEP this one: $service_ref->{monitorStatus}\n";
			            # Some debug log message about this service being filtered out  TBD
		            }
		        }
		        # Replace @services_bundle with the new bundle
		        @services_bundle = @service_states_filtered_bundle;
	        }
	    } 
	    # Run the upsert on the services bundle
	    if ( not $this->{rest_api}->upsert_services( \@services_bundle, $options_ref, \%outcome, \@results ) ) {
	        $this->{logger}->error( "Something went wrong upserting services: " . Dumper \%outcome, \@results );
	        return 0;
	    }

	    # Update audit trail
	    # ... or if posting events (GWMON-11605)
	    if ( $this->{auditing} or $this->{post_events} ) {

	        # Check that each host and service in the dont_exist hashes were successfully upserted, and
	        # if they were one of those that didn't exist earlier, add to the audit trail

	        if (@results) {    # @results has to be defined, else assuming that upserting completely failed

		        # this is all repeated for each of the service bundles so need to reset these each time
		        @created_service_events = ();
		        @created_host_events    = ();

		        # Add successfully upsert'd hosts:services entities to a hash ie if the result entity was tagged as success.
		        # Note that a hostname is not allowed to have a : in it.

		        foreach $result_hashref (@results) {
		            ## The result entity is going to look like this:  host:service and host should not have :'s in it.
		            ## In the version of the API developed against, :s are possible. JIRA GWMON-11574 filed.
		            ( $result_entity_host, $result_entity_service ) = split( /:/, $result_hashref->{entity} );    # TBD API FIX
		            $upserted_hosts_services{$result_entity_host}{$result_entity_service} = 1;
		        }

		        # Check if host was upserted ok
		        foreach $host ( keys %these_hosts_dont_exist ) {
		            if ( defined $upserted_hosts_services{$host} ) {
			            $this->{audit_trail}->{hosts}{$host}{created}{host}{added} = 1;
			            ## GWMON-11605 create a set of pending and initial state host events
			            ## Care is taken to only create pending and initial state events for hosts that were only successfully created.
			            foreach $service_event (@service_events) {
			                if ( $service_event->{host} eq $host ) {

				                # create the PENDING state event
				                # Note that the pending event needs to come before the initial state event
				                # so 1 second is subtracted. Could do this a different way obviously.
				                push @created_host_events,
				                {
				                    'appType'         => $service_event->{appType},
				                    'device'          => $service_event->{device},
				                    'host'            => $service_event->{host},
				                    'monitorStatus'   => 'PENDING',
				                    'reportDate'      => subtract_a_second( $service_event->{reportDate}, $sql_time_format ),
				                    'firstInsertDate' => subtract_a_second( $service_event->{reportDate}, $sql_time_format ),
				                    'severity'        => 'OK',
				                    'textMessage'     => 'Host creation PENDING event (FUS)'
				                };
                
				                # create the initial host state event
				                push @created_host_events, {
				                    'appType'         => $service_event->{appType},
				                    'device'          => $service_event->{device},
				                    'host'            => $service_event->{host},
				                    'monitorStatus'   => 'UP',                        # don't actually know what that is so set it to UP for now
				                    'reportDate'      => $service_event->{reportDate},
				                    'firstInsertDate' => $service_event->{reportDate},
				                    'severity'        => 'OK',
				                    'textMessage' => 'Host creation initial state event (FUS)'
				                };
			                }
			            }
		            }
		        }

		        # Check if service was upserted ok
		        foreach $host ( keys %these_services_dont_exist ) {
		            foreach $service ( keys %{ $these_services_dont_exist{$host} } ) {
			            if ( defined $upserted_hosts_services{$host}{$service} ) {
			                $this->{audit_trail}->{hosts}{$host}{created}{services}{$service} = 1;

			                # GWMON-11605 create a set of pending and initial state service events.
			                # Care is taken to only create pending and initial state events for services that were only successfully created.
			                # Note that the pending event needs to come before the initial state event
			                # so 1 second is subtracted. Could do this a different way obviously.
			                foreach $service_event (@service_events) {
				                if ( $service_event->{host} eq $host and $service_event->{service} eq $service ) {

				                    # create the PENDING state event
				                    push @created_service_events,
				                    {
					                    'service'         => $service_event->{service},
					                    'host'            => $service_event->{host},
					                    'monitorStatus'   => 'PENDING',
					                    'severity'        => 'OK',
					                    'appType'         => $service_event->{appType},
					                    'device'          => $service_event->{device},
					                    'reportDate'      => subtract_a_second( $service_event->{reportDate}, $sql_time_format ),
					                    'firstInsertDate' => subtract_a_second( $service_event->{reportDate}, $sql_time_format ),
					                    'textMessage'     => 'Service creation PENDING event (FUS)'
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
		        # print "No results. Here's the outcome: " . Dumper \%outcome;
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

# ----------------------------------------------------------------------------
sub feeder_delete_hosts
{
    # Takes a hash: { host1=>1, host2=>1 ... }, and a ref to an options hash for delete_hosts();
    # Runs an bundled delete on the keys of that hash.
    # Supports audit trail.
    # returns 1 on success, 0 on failure

    my ( $this,  $hashref_hosts, $hashref_options ) = @_;
    my ( %these_hosts_exist, %these_hosts_dont_exist, @all_hosts, @hosts_bundle );
    my ( %outcome, @results );
    my ( $result_hashref, %deleted_successfully, $host );

    return 0 if not $this->fmee() ; # for testing and QA purposes

    @all_hosts = sort keys %{$hashref_hosts};

    # Bundled hosts deletion and hosts existence testing for auditing
    while ( @hosts_bundle = splice @all_hosts, 0, $this->{host_bundle_size}) {

	    $this->{logger}->debug( "feeder_delete_hosts() starting to process bundle of " . ($#hosts_bundle + 1 ) . " host(s)" );

	    %these_hosts_exist      = ();
	    %these_hosts_dont_exist = ();

	    if ( $this->{auditing} ) {
	        if ( not $this->check_foundation_objects_existence( 'hosts', \@hosts_bundle, \%these_hosts_exist, \%these_hosts_dont_exist ) ) {
		        $this->{logger}->error( "Failed to check existence of hosts in Foundation" );
		        return 0;
	        }
    
	        # If that worked, we now have lists of hosts that do/don't exist.
	    }

	    # Run the delete on the hosts bundle
	    $this->{logger}->debug( 'Deleting Foundation hosts: "' . join( '", "', @hosts_bundle ) . '"' );
	    if ( not $this->{rest_api}->delete_hosts( \@hosts_bundle, $hashref_options, \%outcome, \@results ) ) {
	        $this->{logger}->error( "Something went wrong deleting hosts: " . Dumper \%outcome, \@results );
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
		        # print "No results. Here's the outcome: " . Dumper \%outcome;
	        }
	    }
    }

    return 1;
}

# ----------------------------------------------------------------------------
sub feeder_delete_services
{
    # Takes a hash: { host1=> { svc1=>1, svc2=>1,... },  host2=> {svc10=>1, svc12=>1}, ... }, and a ref to an options hash for delete_services();
    # Runs delete services on a per host basis (ie not so efficient in terms of bundling), mainly cos I don't believe there's a way to
    # delete host1:svc1, host2:svc2,... in a bundled fashion with the API.
    # Supports audit trail.
    # returns 1 on success, 0 on failure

    my ( $this, $hashref_services, $hashref_options ) = @_;
    my ( %these_services_exist, %these_services_dont_exist, $host, $service, @services );
    my ( $result_hashref, %deleted_successfully, $result_entity_host, $result_entity_service, %deleted_hosts_services );
    my ( %outcome, @results );

    return 0 if not $this->fmee() ; # for testing and QA purposes

    foreach $host ( keys %{$hashref_services} ) {
	    $this->{logger}->debug( "feeder_delete_services() processing host $host" );

	    %these_services_exist = (); %these_services_dont_exist = ();

	    if ( $this->{auditing} ) {
	        if ( not $this->check_foundation_objects_existence( 'services', { $host => $hashref_services->{$host} }, \%these_services_exist, \%these_services_dont_exist ) ) {
		        $this->{logger}->error("Failed to check existence of services in Foundation");
		        return 0;
	        }
	        ## If that worked, then now have the lists of services that do/don't exist for this host
	    }

	    @services = keys %{ $hashref_services->{$host} } ;

	    # Run the services delete for this host.
	    # to delete services for a single host: delete_services( \@servicenames, { hostname => 'hostname' }, \%outcome, \@results );
	    $this->{logger}->debug( "Deleting Foundation services: host $host, services @services " );
	    if ( not $this->{rest_api}->delete_services( \@services, { hostname => $host }, \%outcome, \@results ) ) {
	        $this->{logger}->error( "Something went wrong deleting host(s) service(s): " . Dumper \%outcome );
	        return 0;
	    }

	    # Check the results to see which ones were deleted and update audit trail accordingly
	    if ( $this->{auditing} ) {

	        # Check that each host:service in the %these_services_exist hash, were successfully deleted, and
	        # if that host:service was one of those that existed earlier, add it to the audit trail for deleted hosts

	        if ( @results ) { # ... First, @results has to be defined, else assuming that upserting completely failed

		        foreach $result_hashref ( @results ) {
		            # The result entity is going to look like this:  host:service and host should not have :'s in it.
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
		        # print "No results. Here's the outcome: " . Dumper \%outcome;
	        }
	    }
    }

    return 1;
}

# ----------------------------------------------------------------------------
sub check_foundation_objects_existence
{
    # Takes:
    # 1. a ref to an array of things to check for existence of in Foundation:
    #    Expected argument structures:
    #    Type         Structure
    #    hosts        an array of hostnames
    #    hostsgroups  an array of hostgroups names
    #    services     hash like this: { host1 => { svc1=>1, svc2=>2,...},  host2 = { svc10=>1, svc20=>1,... }, ... }
    #                 afaik the current REST API cannot be passed a list of host/service, host/service, ... pairs to check for at this time.
    #                 so for now, get all services for the list of hosts, then figure it out.
    # 2. two results hashes refs, one for storing a list of the hosts which do exist, and one for those that don't
    # returns 1 on successfully being able to get objects, 0 otherwise
    # TBD improve error handling of this routine, including rest_api calls
    # NOTE/TBD: perhaps need an easier way via the REST API to do existence checking, like using HEAD

    my ( $this, $object_type, $ref_objects_to_check, $hashref_objects_exist, $hashref_objects_dont_exist ) = @_;
    my ( %outcome, %results, $object, $get_status, @hostnames, %hosts, $hostname, $service );
    my ( @uniq_objects_list, @objects_bundle, @object_list, $bundle_size );

    return 0 if not $this->fmee() ; # for testing and QA purposes


    if    ( $object_type eq 'hosts' )      { $bundle_size = $this->{host_bundle_size};      @uniq_objects_list = uniq @{$ref_objects_to_check};      }
    elsif ( $object_type eq 'hostgroups' ) { $bundle_size = $this->{hostgroup_bundle_size}; @uniq_objects_list = uniq @{$ref_objects_to_check};      }
    elsif ( $object_type eq 'services' )   { $bundle_size = $this->{service_bundle_size};   @uniq_objects_list = uniq keys %{$ref_objects_to_check}; }
    else {
	    $this->{logger}->error( "Bailing due to object type '$object_type' not yet supported by check_foundation_objects_existence()" ) ;
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
		        $this->{logger}->error( "Something went wrong checking existence of ${object_type}(s): " . Dumper \%outcome );
		        return 0;
	        }
	    }
    }

    return 1;
}

# ----------------------------------------------------------------------------
sub flush_audit
{
    # If auditing is on, parses the audit_trail hash and uses /api/auditlogs to create audit trail.
    # Notifications for auditing are the responsibility of /api/auditlogs now.
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
    my ( %outcome, @results, $msg, @audit_data, $username );
    my $feeder_name = ( defined $main::feeder_name ? $main::feeder_name : 'unknown' ) ;

    return 0 if not $this->fmee() ; # for testing and QA purposes

    # Do nothing if auditing is off ie audit_trail is not defined or false
    if ( not $this->{auditing} )  {
	    $this->{logger}->debug( "Auditing is disabled" );
	    return 1;
    }

    # If the audit trail is empty, return here too
    if ( not scalar keys %{ $this->{audit_trail} } ) {
	    $this->{logger}->debug( "Audit trail empty - no flushing required");
	    return 1 ;
    };

    $this->{logger}->debug( "Flushing audit trail");

    # When scropt is running as root in non interactive shell mode, I think LOGNAME and USER don't get set and getpwuid returns something like id output or some 
    # other multi field string which ends up injecting spurious fields into the built audit data which then makes the /api/auditlogs call puke.
    #$username = $ENV{LOGNAME} || $ENV{USER} || getpwuid($<);
    $username = $ENV{LOGNAME} || $ENV{USER} || "cacti_feeder_user"; # just keep gwtpwuid out of the way for now - want a single non spaced string
    $username =~ s/\s//g; # Just in case - remove any whitespace

    # Hostgroups 
    if ( exists $this->{audit_trail}->{hostgroups} ) {

	    foreach $hostgroup ( keys %{ $this->{audit_trail}->{hostgroups} } ) {

            if ( exists $this->{audit_trail}->{hostgroups}{$hostgroup}{created} ) { 
                push @audit_data, 
                {
	               subsystem          => $feeder_name,
	               hostGroupName      => $hostgroup,
	               action             => 'ADD',
	               description        => "Hostgroup $hostgroup added by feeder $feeder_name running on $feeder_host",
	               username           => $username
                };
            }

            elsif ( exists $this->{audit_trail}->{hostgroups}{$hostgroup}{deleted} ) { 
                push @audit_data, 
                {
	               subsystem          => $feeder_name,
	               hostGroupName      => $hostgroup,
	               action             => 'DELETE',
	               description        => "Hostgroup $hostgroup deleted by feeder $feeder_name running on $feeder_host",
	               username           => $username
                };
            }
	    }
    }

    # Hosts and their services
    if ( exists $this->{audit_trail}->{hosts} ) {

	    foreach $host ( keys %{$this->{audit_trail}->{hosts}} ) {

	        # Host added
	        if ( exists $this->{audit_trail}->{hosts}{$host}{created}{host}{added} ) {
		        #$msg = "Host '$host' created by feeder $this->{feeder_name} running on $this->{feeder_host}";
		        $msg = "Host '$host' created by feeder $feeder_name running on $feeder_host";
                push @audit_data, 
                {
	                subsystem          => $feeder_name,
	                hostName           => $host,
	                action             => 'ADD',
	                description        => $msg,
	                username           => $username
                }
	        }

	        # Host deleted
	        if ( exists $this->{audit_trail}->{hosts}{$host}{deleted}{host}{deleted} ) {
		        #$msg = "Host '$host' deleted by feeder $this->{feeder_name} running on $this->{feeder_host}";
		        $msg = "Host '$host' deleted by feeder $feeder_name running on $feeder_host";

                push @audit_data, 
                {
	                subsystem          => $feeder_name,
	                hostName           => $host,
	                action             => 'DELETE',
	                description        => $msg,
	                username           => $username
                }

	        }
    
	        # Services added
	        if ( exists $this->{audit_trail}->{hosts}{$host}{created}{services} ) {
		        foreach $service ( keys %{ $this->{audit_trail}->{hosts}{$host}{created}{services} } ) {
		            #$msg = "Service '$service' created by feeder $this->{feeder_name} running on $this->{feeder_host}";
		            $msg = "Service '$service' created by feeder $feeder_name running on $feeder_host";
                    push @audit_data, 
                    {
	                    subsystem          => $feeder_name,
	                    hostName           => $host,
	                    serviceDescription => $service,
	                    action             => 'ADD',
	                    description        => $msg,
	                    username           => $username
                    }
		        }
	        }

	        # Services deleted
	        if ( exists $this->{audit_trail}->{hosts}{$host}{deleted}{services} ) {
		        foreach $service ( keys %{ $this->{audit_trail}->{hosts}{$host}{deleted}{services} } ) {
		            #$msg = "Service '$service' deleted by feeder $this->{feeder_name} running on $this->{feeder_host}";
		            $msg = "Service '$service' deleted by feeder $feeder_name running on $feeder_host";
                    push @audit_data, 
                    {
	                    subsystem          => $feeder_name,
	                    hostName           => $host,
	                    serviceDescription => $service,
	                    action             => 'DELETE',
	                    description        => $msg,
	                    username           => $username
                    }
		        }
	        }
	    }
    }

    if ( not $this->{rest_api}->create_auditlogs( \@audit_data, { async => 'false' }, \%outcome, \@results ) ) {
	    $this->report_feeder_error("Auditing error occurred posting auditing - audit log will not be flushed");
        return 0;
    }

    # Empty the audit trail now the audit trail has been created
    $this->{audit_trail} = ();
    $this->{logger}->debug( "Audit trail flushed");

    return 1;
}

# ----------------------------------------------------------------------------
sub initialize_health_objects
{
    # Initializes feeder health services.
    # This subroutine creates these objects for stats and health of the app in Foundation:
    #
    #   <feeder stats hostgroup>
    #     <feeder health host>
    #         <feeder_name>_health service (automatically by this routine)
    #         other services defined in $feeder_services hash ref 
    #
    # Then sets the feeder health service status to a message about things being started
    # 
    # Args
    #  - startup message
    #  - ref to hash of services
    #
    # Returns 1 ok, 0 otherwise.

    my ( $this, $started_message, $feeder_services_ref ) = @_;
    my ( %instructions, %results, $last_processed_event_id, $last_event_id );
    my ( @hosts,  %host_options, @hostgroups, %hostgroup_options, @services, %service_options, $feeder_specific_service );

    $this->{logger}->debug("Initializing $this->{feeder_name} statistical and health objects in Foundation");

    return 0 if not $this->fmee() ; # for testing and QA purposes

    my $now = strftime( '%Y-%m-%dT%H:%M:%S%z', localtime );

    # Update or create the health host and set it to be in an UP state
    %host_options = ( );
    @hosts = (
	    {
	        hostName      => $this->{properties}->{health_hostname},
	        monitorStatus => "UP",
	        description   => "$this->{feeder_name} virtual host",
	        lastCheckTime => $now,
	        #properties    => { 'LastStateChange' => $now }  # No - let feeder_upsert_hosts handle this properly now # Version 0.3.8
	        properties    => { 'LastPluginOutput' => "Feeder host : $this->{feeder_host}" } # includes host where feeder is running, in the Status in SV
	    },
    );

    if ( not $this->feeder_upsert_hosts( \@hosts, \%host_options ) ) {
	    $this->{logger}->error("Could not upsert hosts during feeder health objects initialization");
	    return 0;
    }

    # Update or create the health hostgroup and put the health vhost in it
    %hostgroup_options = ();
    @hostgroups = (
	    {
	        "name"        => $this->{properties}->{health_hostgroup},
	        "alias"       => $this->{properties}->{health_hostgroup},
	        "description" => "$this->{feeder_name} virtual hostgroup",
	        "hosts"       => [ { "hostName" => $this->{properties}->{health_hostname} } ],
	        "agentId"     => $this->{guid},
	    },
    );

    if ( not $this->feeder_upsert_hostgroups( \@hostgroups, \%hostgroup_options ) ) {
	    return 0;
    }

    # Upsert some services on that host to create them
    # First add the feeder health service
    %service_options = ( );
        @services = (
	    {
	        description     => $this->{feeder_name} . ".health",                           # automatically create <feeder_name>_health
	        hostName        => $this->{properties}->{health_hostname},
	        monitorStatus   => 'OK',
	        properties      => { 
                                    "LastPluginOutput" => "OK - $started_message" ,    # This is the message
                                    "Notes" => $metric_service_meta_tag # Need to tag the type of service for proper handling in  remove_cacti_objects_from_foundation() at least
                               },
	        lastCheckTime   => $now,
	        # lastStateChange => $now # V 0.3.8
	    },
    );

    # Then add any feeder specific services defined in feeder_services hash prop
    if ( defined $feeder_services_ref  ) {
	    foreach my $feeder_specific_service ( keys %{ $feeder_services_ref } ) {
	        push @services, {
		        description     => $feeder_specific_service,
		        hostName        => $this->{properties}->{health_hostname},
		        monitorStatus   => 'OK',
		        properties      => { 
                                        "LastPluginOutput" => "$feeder_services_ref->{$feeder_specific_service}" ,    # seed the value with its description :)
                                        "Notes" => $metric_service_meta_tag # Need to tag the type of service for proper handling in  remove_cacti_objects_from_foundation() at least
                                    },
		        lastCheckTime   => $now,
		        # lastStateChange => $now # V 0.3.8
	        };
	    }
    }


    if ( not $this->feeder_upsert_services( \@services, \%service_options ) ) {
	    return 0;
    }

    return 1;
}

# ----------------------------------------------------------------------------
sub initialize_interrupt_handlers
{
    # TBD figure out correct best way to handle these when more complete. For now this is just super simple.
    # TBD this still doesn't work quite right but better than nothing right now
    # TBD it still gives this after running terminate_feeder and then notify_feeder_endpoint_health_service
    #   POST error during REST API logout:  Status code 424: 'Failed Dependency'. Full response content : 'Cannot make a REST call without a REST client object in play.'.
    #   ERROR:  Did you forget to undefine your GW::RAPID handle before exiting your application?
    # Will get to the bottom of it when more time.
    my ( $endpoint_array_ref, $feeder_options_ref ) = @_;
    my $feeder_name = ( defined $main::feeder_name ? $main::feeder_name : 'unknown' ) ;
    
    # SIGINT doesn't work too well when the feeder is sleeping in a cycle (and possibly when daemonized). But, its useful when running interactively.
    $SIG{INT}   = sub { terminate_feeder("Feeder '$feeder_name' was terminated with an interrupt (SIGINT) signal at " . localtime() , $feeder_name, $endpoint_array_ref, $feeder_options_ref ); };

    # SIGKILL is not trappable so don't try
    # $SIG{KILL}  = sub { terminate_feeder("Feeder '$feeder_name' was terminated with an interrupt (SIGKILL) signal at " . localtime() , $feeder_name, $endpoint_array_ref, $feeder_options_ref ); };

    # Really not sure what's going to be sending a SIGTERM - leaving in for now tho
    $SIG{TERM} = sub { terminate_feeder("Feeder '$feeder_name' was terminated with a terminate (SIGTERM) signal at " . localtime() , $feeder_name, $endpoint_array_ref, $feeder_options_ref ); };

    # SIGHUP is sent by logrorate
    $SIG{HUP}  = sub { terminate_feeder("Feeder '$feeder_name' was terminated with a hangup (SIGHUP) signal at "     . localtime() , $feeder_name, $endpoint_array_ref, $feeder_options_ref ); }; 

}

# ----------------------------------------------------------------------------
sub terminate_feeder
{
    # Attempt to send a notification to each endpoint. This might use a complete refactoring.
    my ( $message, $feeder_name, $endpoint_array_ref, $feeder_options_ref ) = @_;
    my ( $endpoint , $endpoint_name, $endpoint_config, $feeder_object , $error );


   #ENDPOINT: foreach $endpoint ( @{$endpoint_array_ref}  ) {
   #    ( $endpoint_name, $endpoint_config ) = split /:/, $endpoint;
   #    # Try to create a new feeder object - this might fail but worth trying
   #    if ( $feeder_object = GW::Feeder->new( $feeder_name, $endpoint_config, $feeder_options_ref, $endpoint_name ) ) {
   #        # If processing is endabled in the endpoint's config, then send message to health service
   #        if ( $feeder_object->{properties}->{enable_processing} ) {
   #            $feeder_object->notify_feeder_endpoint_health_service( $message );
   #        }
   #    }
   #    else {
   #        $feeder_options_ref->{logger}->error($message);
   #    }
   #}

    # Need to inform all endpoint services at all endpoints that the feeder was interrupted - this is in line with the new metrics approach
    # of sending all metrics to all endpoints. The send_metrics routine will be used for interrupt notification.

    $feeder_options_ref->{logger}->info("======= Terminating feeder ======" );
    my %metrics ;
    foreach $endpoint ( @{$endpoint_array_ref}  ) {
        ( $endpoint_name, $endpoint_config ) = split /:/, $endpoint;
        # Try to create a new feeder object - this might fail but need to try it
        if ( $feeder_object = GW::Feeder->new( $feeder_name, $endpoint_config, $feeder_options_ref, $endpoint_name ) ) {

            # If the feeder is not enabled, don't try to build stuff to send to it
            # otherwise ends up with hosts without hostgroups in foundation.
            next if not $feeder_object->{enable_processing};

            # build the metrics data structure but just for the health service
            $metrics{endpoints}{$endpoint_name}{feeder_object} = $feeder_object;
            $metrics{endpoints}{$endpoint_name}{handling_interrupt} = 1;
            $metrics{endpoints}{$endpoint_name}{services} = [ 
                {
                    service => $feeder_name . ".health",
                    message => $message,
                    status  => "UNSCHEDULED CRITICAL"
                }
            ];

        }
        else {
            $feeder_options_ref->{logger}->error($message);
        }
    }

    send_metrics( \%metrics, \$error, $feeder_options_ref->{logger} ); 

    $feeder_options_ref->{logger}->info("======= Feeder terminated ======" );
    exit;

}

# ----------------------------------------------------------------------------
sub endpoints_enabled
{
    # Calculates how many endpoints, defined in the main feeder's config, are enabled
    # Args :
    #   ref to endpoint array ref
    #   ref to error that gets populated
    # Returns :
    #   Count of endpoints enabled

    my ( $endpoint_array_ref, $error_ref ) = @_;
    my ( $endpoint, $endpoint_name, $endpoint_config, $enabled_count, $config );

    $enabled_count = 0;
    ${$error_ref} = undef;

    foreach $endpoint ( @{$endpoint_array_ref}  ) {
        ( $endpoint_name, $endpoint_config ) = split /:/, $endpoint;
	    eval { 
            $config = TypedConfig->new( $endpoint_config ); 
        } ; 
        if ( $@ ) { 
            chomp $@;
            ${$error_ref} = $@;
            return 0; # would need to fix why cannot get endpoint config first 
        }
        if ( exists $config->{enable_processing} and defined $config->{enable_processing} ) { 
            $enabled_count += $config->{enable_processing};
        }
        $config = undef;
    }

    return $enabled_count;

}

# ----------------------------------------------------------------------------
sub get_one_prop_from_conf
{
    # Tries to get one prop's val from a config
    # Args
    #  - prop name
    #  - fully qualified config file name
    #  - ref to prop value if found
    #  - error ref if error occurred reading the confi
    # Returns : 
    #  - 1 on success read of conf and prop found
    #  - 0 if unsuccessful read of conf or prop not found and error_ref populated if error happened

    my ( $prop, $conf, $prop_val_ref, $error_ref ) = @_;
    my ( $config );
    eval { 
        $config = TypedConfig->new( $conf ) ;
    };
    if ( $@ ) { 
        chomp $@;
        ${$error_ref} = $@;
        return 0; 
    }
    if ( exists $config->{$prop} and defined $config->{$prop} ) { 
        ${$prop_val_ref} = $config->{$prop};
        return 1;
    }
    else { 
        ${$prop_val_ref} = undef;
        return 0;
    }
}


# ----------------------------------------------------------------------------
# V 0.4.0 - used for backward compat, where a 702 feeder is using this latest 710 Feeder lib
sub initialize_interrupt_handlers_702
{
    # Sets up interrupt handlers
    # TBD figure out correct best way to handle these when more complete. For now this is just super simple.
    my ( $this ) = @_;

    $SIG{INT}  = sub { $this->terminate_feeder_702("$this->{feeder_name} feeder was terminated with an interrupt (SIGINT) signal !!!");  };
    $SIG{TERM} = sub { $this->terminate_feeder_702("$this->{feeder_name} feeder was terminated with a terminate (SIGTERM) signal !!!") } ;
    $SIG{HUP}  = sub { $this->terminate_feeder_702("$this->{feeder_name} feeder was terminated with a hangup (SIGHUP) signal !!!") } ; # HUP from logrotate
}

# ----------------------------------------------------------------------------------------------------------------
# V 0.4.0 - used for backward compat, where a 702 feeder is using this latest 710 Feeder lib
sub terminate_feeder_702
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
                                                'description'          => $this->{feeder_name}.".health",
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
                                                    'service'           => $this->{feeder_name}.".health",
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

# ----------------------------------------------------------------------------
sub notify_feeder_endpoint_health_service 
{
    # This used to be called terminate_feeder.
    # Terminates the feeder in a hopefully transparent way.
    # Takes:
    #   a message
    #   optional sleep - sometimes used (in the case of supervise running disabled feeder for example)
    #   optional service status: if this is not set, then assumes bad. If its set to 'OK',
    #                            then assumes clean termination, as in case of run-once mode
    #   optional no_feeder_ops flag - if its defined, then dont do feeder operations, just log - case: no license installed
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
	    ## Only update health objects if they already exist, so we don't create any such objects
	    ## if we're terminating because the feeder has never been enabled in its config file.
    
	    my @hosts_to_check         = ();
	    my %these_hosts_exist      = ();
	    my %these_hosts_dont_exist = ();
    
	    push @hosts_to_check, $this->{properties}->{health_hostname};
	    if ( not $this->check_foundation_objects_existence( 'hosts', \@hosts_to_check, \%these_hosts_exist, \%these_hosts_dont_exist ) ) {
	        $this->report_feeder_error("ERROR checking Foundation health host existence");
	    }
	    ## Alas, %these_hosts_exist isn't (yet) populated by check_foundation_objects_existence(), so we need to check the other hash.
	    elsif ( not $these_hosts_dont_exist{ $this->{properties}->{health_hostname} } ) {
	        my $now = strftime( '%Y-%m-%dT%H:%M:%S%z', localtime );
    
	        ## Put the health host into a down state
	        $this->feeder_upsert_hosts(
		    [
		        {
			        hostName      => $this->{properties}->{health_hostname},
			        description   => "$this->{feeder_name} virtual host",
			        monitorStatus => 'UNSCHEDULED DOWN',
			        lastCheckTime => $now,
			       #properties    => { 'LastStateChange' => $now }
			        properties    => { 'LastStateChange' => $now, 'LastPluginOutput' => "Feeder host : $this->{feeder_host}" }
		        }
		    ],
		    {}    # options
	        );
    
	        # Post a message to the health service
	        $this->feeder_upsert_services(
		    [
		        {
			        hostName        => $this->{properties}->{health_hostname},
			        description     => $this->{feeder_name} . ".health",
			        monitorStatus   => $health_service_status,
			        properties      => { 'LastPluginOutput' => localtime() . " $message" },
			        lastCheckTime   => $now,
			        lastStateChange => $now
		        }
		    ],
		    {}    # options
	        );
    
	        # Create a service event for this termination event too
	        $this->feeder_post_events(
		        'service',
		        [
		            {
			            device        => $this->{properties}->{health_hostname},
			            host          => $this->{properties}->{health_hostname},
			            service       => $this->{feeder_name} . ".health",
			            monitorStatus => $health_service_status,
			            appType       => $this->{app_type},
			            severity      => $event_severity,
			            textMessage   => $message,
			            reportDate    => $now
		            }
		        ]
	        );
	    }
    }

    # From the perldoc GW::RAPID ...
    # "IMPORTANT:  Before process exit, be sure to release your handle to the
    # REST API.  This will force GW::RAPID to call its destructor.  And that
    # will attempt to log out before Perl's global destruction pass wipes
    # out resources needed for logout to work properly.  We want to log out
    # in order to be polite and release server-side resources right away."
    # We do this before sleeping, in order to ensure that the session does
    # get actively destroyed on the other side even if our sleeping gets
    # interrupted.
    $this->{rest_api} = undef;

    # Sometimes used (in the case of supervise running disabled feeder for example)
    if (defined $sleep) {
	    if ( $sleep < 0 ) {
	        $this->{logger}->info("Sleeping forever ...");
	    }
	    else {
	        $this->{logger}->info("Sleeping $sleep seconds before quitting ...");
	    }
	    sleep $sleep;
    }

    #exit;
}

# ----------------------------------------------------------------------------
sub report_feeder_error 
{

    # Handles errors detected by feeder by making them transparent through a
    # feeder health service and logging them too.
    # If the REST endpoint has failed, or the REST API calls fail, it still at the end writes
    # out to the $logger too.

    my ( $this, $message ) = @_;
    my ( %outcome, @results );

    $message .= " (reported from " . ( caller(1) )[3] . "() )" if defined( ( caller(1) )[3] );

    # Update the health service.  appType is not required here if the service already exists, but if someone
    # deletes the service in Foundation without bouncing the feeder, this will be treated as a create instead
    # of an update, and that will throw an error without an appType being present.
    $this->{rest_api}->upsert_services(
	    [
	        {
		        'description'   => $this->{feeder_name} . ".health",
		        'hostName'      => $this->{properties}->{health_hostname},
		        'appType'       => $this->{properties}->{app_type},
		        'monitorStatus' => 'UNSCHEDULED CRITICAL',
		        'properties'    => { "LastPluginOutput" => $message }
	        }
	    ],
	    {},
	    \%outcome,
	    \@results
    );

    # Create an event for this error
    $this->{rest_api}->create_events(
	    [
	        {
		        'host'          => $this->{properties}->{health_hostname},
		        'device'        => $this->{properties}->{health_hostname},
		        'service'       => $this->{feeder_name} . ".health",
		        'monitorStatus' => 'UNSCHEDULED CRITICAL',
		        'appType'       => $this->{app_type},
		        'severity'      => 'SERIOUS',
		        'textMessage'   => $message,
		        'reportDate'    => strftime( '%Y-%m-%dT%H:%M:%S%z', localtime )
	        }
	    ],
	    {},
	    \%outcome,
	    \@results
    );

    # Log the error too
    $this->{logger}->error($message);
}

# ----------------------------------------------------------------------------
sub get_current_time
{
    # Returns the current time in SQL date format
    my ($sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $isdst) = localtime(time);
    return sprintf("%04d-%02d-%02d %02d:%02d:%02d", $year+1900, $mon+1, $mday, $hour, $min, $sec);
}

# ----------------------------------------------------------------------------
# Note : no longer in use - was used by license checking routines which now use new 
# RAPID::check_license() / REST API license/check method. Leaving in here for possible 
# future use.
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

    $logger->trace("TRACE running system command '$cmd'");

    while ( $tries <= $maxtries ) {
	    $cmdstat = system($cmd) ;
	    $shiftedstat = $cmdstat >> 8;
	    if ( $cmdstat == 0 ) {
	        $logger->trace("TRACE Successfully executed '$cmd'");
	        return 0;
	    }
	    else {
	        $tries++;
	        #$logger->error( "Command '$cmd' failed - status=$shiftedstat (or $cmdstat) $! - retrying in $sleep second" );
	        $logger->error( "Command '$cmd' failed - status=$shiftedstat (or $cmdstat) $!"); # for maxtries=1 - this is a temp change
	        sleep $sleep;
	        $sleep += $sleep ;
	    }
    }

    $logger->error( "Command '$cmd' failed to execute successfully" );
    #return $cmdstat;
    return $shiftedstat;
}

# ----------------------------------------------------------------------------
sub perl_script_process_count
{
    # Checks if a perl script is running - returns the number of script processes running
    my ( $perlscript ) = @_;
    my $perl_script_process_count = `ps -w -w -o pid,args --no-headers -C .perl.bin | fgrep $perlscript | wc -l`;
    chomp $perl_script_process_count;
    return $perl_script_process_count;
}

# ----------------------------------------------------------------------------
sub license_installed_old # No longer required as a license is always installed
{
    # On a fresh install of GW, checks license has been installed.
    # This is a temp workaround for lacking REST API support for a method to see if a license is installed yet.
    # Returns 1 if installed, 0 otherwise.
    # Takes a logger object.

    my ( $this, $logger ) = @_;
    my ( $check_command, $stat ) ;

    return 0 if not $this->fmee() ; # for testing and QA purposes

    $this->{logger}->trace( "TRACE checking if license is installed" );
    $check_command = "/usr/local/groundwork/core/monarch/bin/add_check 1";
    if ( $this->{license_check} eq "remote" )
    {
	    $check_command = "ssh -oConnectTimeout=10 $this->{license_check_user}\@$this->{monitoring_server} $check_command";
    }

    $this->{logger}->trace("TRACE License installation check command set to $check_command");
    $stat =  run_system_cmd( $this->{logger}, $check_command )  ;
    # add_check seems to return 3 if no license is installed.
    if ( $stat == 3 ) {
	    $this->{logger}->trace( "TRACE license not installed");
	    return 0 ;
    }
    else {
	    $this->{logger}->trace( "TRACE license installed");
	    return 1;
    }
}

# ----------------------------------------------------------------------------
sub check_license
{
    # Takes a ref to an array of built hosts ie data structures that can be consumed by upsert_hosts().
    # First checks to see how many hosts actually would need to be added,
    # then checks if that number would exceed a license limit.
    # Returns 1 if would NOT exceed limit, 0 if WOULD

    my ( $this, $arrayref_built_hosts, $error_ref ) = @_;
    my ( $host_ref, @hosts_to_check, $count_hosts_that_need_adding, %these_hosts_exist, %these_hosts_dont_exist, %outcome, %results );

    return 0 if not $this->fmee() ; # for testing and QA purposes

    # Build a list of hostnames to check existence for
    foreach $host_ref ( @{$arrayref_built_hosts} ) {
	    if ( not defined $host_ref->{hostName} ) {
	        $this->{logger}->error( "License checking cannot be performed - host data structure missing expected hostName property: " . Dumper $host_ref );
	        ${$error_ref} = "License checking cannot be performed - host data structure missing expected hostName property: " . Dumper $host_ref ;
	        return 0;
	    }
	    else {
	        push @hosts_to_check, $host_ref->{hostName} ;
	    }
    }

    # Check existence of these hosts
    if ( not $this->check_foundation_objects_existence( 'hosts', \@hosts_to_check, \%these_hosts_exist, \%these_hosts_dont_exist ) ) {
	    $this->{logger}->error( "check_license(): Failed to check for host object existence - license checking cannot be performed");
	    ${$error_ref} = "check_license(): Failed to check for host object existence - license checking cannot be performed";
	    return 0;
    }

    $count_hosts_that_need_adding = scalar keys %these_hosts_dont_exist;

    if ( $count_hosts_that_need_adding > 0 ) {
	    $this->{logger}->trace( "TRACE checking $count_hosts_that_need_adding host(s) could be added without exceeding license limits");

        if ( $this->{RAPID_710_plus} ) {  # v0.4.0 - this property gets set if RAPID version supports the check_license method

            # See if there is room for a few new devices.
            if ( not $this->{rest_api}->check_license( [], { allocate => $count_hosts_that_need_adding }, \%outcome, \%results ) ) {
                ## Either the call itself failed, or there was not room for that many.
                if ( defined $outcome{success} ) {
                    # "success" will be defined but false in this case; adding N devices would run us over the limit.
	                $this->{logger}->error("License checking failure - The license would be exceeded by adding $count_hosts_that_need_adding more devices.");
	                ${$error_ref} = "License checking failure - The license would be exceeded by adding $count_hosts_that_need_adding more devices.";
                    return 0;
                }
                else {
                    # The call itself failed; perhaps the other side went down, or there was no license installed
	                $this->{logger}->error("License checking failure - There was a problem running the license check.");
                    ${$error_ref} = "License checking failure - There was a problem running the license check.";
                    # Worth providing a little more detail perhaps ...
                    foreach my $outcome_key ( sort keys %outcome ) {
	                    $this->{logger}->error("License checking failure -     $outcome_key => $outcome{$outcome_key}");
	                    ${$error_ref} .= "License checking failure -     $outcome_key => $outcome{$outcome_key}";
                    }
                    return 0;
                }
            }
            else {
                ## There was (momentarily, at least) still room for 5 new devices.
	            $this->{logger}->trace("TRACE license checking success - adding $count_hosts_that_need_adding hosts will not exceed a license limit");
                return 1;
            }

        }
        else { # v 0.4.0
            $this->{logger}->debug("License checking has been automatically disabled.") ; 
            # because the only reliable way of checking license limits is with the RAPID->check_license() which uses the REST API, not the old add_check method
            return 1;
        }

    }
    else {
	    $this->{logger}->trace("TRACE license checking not necessary - zero hosts needed adding");
        return 1;
    }

    # should not get here - internal error if it did
	$this->{logger}->error("INTERNAL ERROR in license check routine.");
	${$error_ref} = "INTERNAL ERROR in license check routine check_license().";
    return 0; 

}

# ----------------------------------------------------------------------------
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

# ----------------------------------------------------------------------------
# The retry cache stuff is a prime candidate for making into it's own OO library
# ----------------------------------------------------------------------------
sub endpoint_retry_cache_prep_dir
{
    # Checks things about the retry cache directory
    # returns 1 if all ok, 0 otherwise.

    my ( $logger, $retry_cache_directory ) = @_;

    # Creates the retry cache directory if necessary
    if ( ! -e $retry_cache_directory ) {
        $logger->debug("Creating missing retry cache directory '$retry_cache_directory'");
        eval { make_path( $retry_cache_directory, { } ) ; } ; 
        if ( $@ ) { 
            chomp $@;
            $logger->error("Failed to create retry cache directory '$retry_cache_directory' : $@");
            return 0;
        }
    }
    
    # Checks read write permissions on the cache directory
    # TBD switch this over to use stat or File::State instead. 
    if (  ( not -r $retry_cache_directory ) or ( not -w $retry_cache_directory ) or ( not -x $retry_cache_directory )  ) {
        $logger->error("Insufficient permissions on retry cache directory '$retry_cache_directory'");
        return 0;
    }

    return 1;

}

# ----------------------------------------------------------------------------
sub endpoint_retry_cache_name
{
    # Constructs what is hopefully a unique enough retry cache filename for a given endpoint and feeder.
    # It would be awesome to use the endpoint's guid, but if the feeder fails to initialize ever, then 
    # the guid might not be set. Also the use of hostname could be useful should the cache directory be 
    # shared across multiple hosts running the same feeder - although thats pretty unlikely.
    my ( $hostname, $feeder_name, $endpoint_name, $retry_cache_directory ) = @_;

    # Format is : <cache dir>/<hostname>_<feedername>_<endpointname>
    # EG  /usr/local/groundwork/foundation/feeder/retry_caches/gwmon_cacti_feeder_localhost
    return "$retry_cache_directory/${hostname}_${feeder_name}_${endpoint_name}";
}

# ----------------------------------------------------------------------------
sub endpoint_retry_cache_size
{
    # Figures out the size of a cache. It returns the size in various UOMs such as Mb, timestamp_rows, etc.
    # Args:
    #   - name of a retry cache file
    #   - ref to a hash of size data that this will populate
    #   - ref to an error string that will be populated if necessary in this sub
    # Returns 
    #   - 1 if ok + populated size hashref
    #   - 0 otherwise + populated ref to error string
    
    my ( $cache_name, $size_hashref, $error_ref ) = @_;

    $size_hashref->{summary} = ""; # set this to empty to avoid undefined errors in caller when there's an error in this routine

    # Check the file exists and is readable
    if ( (! -e $cache_name) or (! -r $cache_name) ) { 
        ${$error_ref} = "Cache file doesn't exist or is not readable";
        $size_hashref->{error} = ${$error_ref};
        return 0;
    }

    # Add the name of the cache
    $size_hashref->{"Cache file name"} = $cache_name;

    # Get the size of the file in Mb using stat 
    $size_hashref->{Mb} = sprintf "%0.6f", (stat $cache_name )[7] / 1048576;

    # Count the number of lines in the cache - this is a count of the number of querytime entries.
    # There's plenty of ways to do this, ranging from `wc -l` (v fast but spawns another shell,depends on wc etc), 
    # to loading file into memory and counting  each line (bad and slow). Somewhere in the middle, is just reading the file and
    # counting the lines with Perl. In tests of a 300mb file read compared to `wc -l` its about 7 times slower, but still sub-second.
    my $handle; 
    #print "opening cache $cache_name - emulating error - hit enter after deleting cache file\n"; <STDIN>;
    if ( open $handle, $cache_name ) { 
        my $numlines = 0;
        while (<$handle>) { $numlines ++ ; };  # get a count of the number of lines 
        $size_hashref->{querytime_rows} = $numlines;
        close $handle;
    }
    else { 
        ${$error_ref} = "Could not open $cache_name to count rows";
        return 0;
    }

    # Compose a directly consumable message string here too
    if ( $size_hashref->{querytime_rows} > 0 or $size_hashref->{Mb} > 0 ) { 
        $size_hashref->{summary} = "Retry cache file " . basename($cache_name) . " contains $size_hashref->{querytime_rows} timestamped rows, and is $size_hashref->{Mb} Mb in size.";
    }
    else {
        $size_hashref->{summary} = "Ok - retry cache file " . basename($cache_name) . " is empty";
    }

    return 1;
    
}

# ----------------------------------------------------------------------------
sub endpoint_retry_cache_write
{
    # Writes new or appends to a retry cache.
    # Args:
    # - endpoint name - name of endpoint being processed
    # - cache file name: as produced by endpoint_retry_cache_name()
    # - ref to endpoint data : a reference to an array of hashes : [  { querytime => time, rows = [ { ... } ]  } ,  ... ] 
    # - logger : a log4perl logger object ref
    # - op : 'a' for append, 'w' for write (ie replace)
    # - ref to rows written to the cache - this is for surfacing this info into the metrics GWMON-12363, and will be populated here
    # - ref to a hash that will be populated here with size of cache in Mb and rows etc : { "Mb" => 123, "rows" => 43, ... }
    # The data array is appended to the cache file, in JSON format.
    # Returns 1 if ok, 0 otherwise
    # Truncates from the top the retry cache if it is exceeding any defined critical limits.
    # NOTES
    # 1. 0.5.0 mod to not write out empty rows data to cache

    my ( $endpoint_name, $cache_name, $ref_endpoint_data, $logger, $op, $ref_rows_written_to_cache, $size_hashref, $cache_file_truncation_message_ref ) = @_;
    my ( $fh, $JSONdata, $operation, $sizing_error ) ;

    # Check given op is append or write
    if ( $op eq 'a' ) {
        $operation = "create and/or append";
    }
    elsif ( $op eq 'w' ) {
        $operation = "replacement write";
    }
    else {
        $logger->error("INTERNAL ERROR Unrecognized cache write operation '$op' - expecting 'a' or 'w'");
        return 0;
    }

    $logger->debug("Beginning $operation on retry cache $cache_name");

    # Check will be able to write to the cache if it already exists
    if ( -e $cache_name and ! -w $cache_name ) {
        ${$cache_file_truncation_message_ref} = "Insufficient write permission on '$cache_name' - append not possible";
        $logger->error( ${$cache_file_truncation_message_ref} );
        return 0;
    }

    # Open the cache file for append
    $logger->trace("TRACE Opening retry cache '$cache_name' for append");
    $fh = IO::File->new( $cache_name, $op);
    if ( not defined $fh ) {
        $logger->error("Could not open retry cache file '$cache_name' for $operation- $!");
        return 0;
    }

    ${$ref_rows_written_to_cache} = 0 ;
    foreach my $entry ( @$ref_endpoint_data ) {

        # 0.5.0 - if rows contains nothing, don't bother filling the cache up with the entry
        if ( not scalar @{$entry->{rows}} ) {
            $logger->debug("Empty rows data - skipping putting it into the retry cache");
            next;
        }

        # Convert the data into JSON. TBD need some test data to exercise failure condition.
        eval {
            $JSONdata = encode_json( $entry ); # TBD is there a risk that encode_json might disrupt the golden data (by ref) here?
        };
        if ( $@ ) { 
            chomp $@;
            $logger->error("Could not encode data into JSON - $@");
            return 0;
        }
    
        # Write the JSON to the cache
        if ( not print $fh "$JSONdata\n"  ) {
            $logger->error("Could not write encoded JSON data to retry cache file '$cache_name' - $!");
            undef $fh; # close the file
            return 0;
        }

        ${$ref_rows_written_to_cache} ++;
    }
    
    # Close the retry cache file. TBD check close went ok.
    $fh->close();

    # Get cache size stats for reporting via metrics
    if ( not endpoint_retry_cache_size( $cache_name, $size_hashref, \$sizing_error ) ) { 
        $size_hashref->{error} = "Could not get size info for retry cache file '$cache_name'. $sizing_error";
        $logger->error( $size_hashref->{error} );
    }

    # If there's is a critical size limit defined for this endpoint's retry cache, see if the cache has grown so large that it needs truncating, and truncate it if it has
    if ( defined $main::master_config->{retry_cache_limits}->{$endpoint_name}->{critical} ) { 
        if  ( not truncate_file_from_front( $logger, $cache_name, $main::master_config->{retry_cache_limits}->{$endpoint_name}->{critical} * 1024 * 1024, $cache_file_truncation_message_ref ) ) { 
            $logger->error("An error occurred in the routine that determines if a retry cache needs to be truncated, truncating it if necessary");
            # TBD carry on or bail ?? The above message will propogate back into cache_errors service via send_metrics so no need to do anything else here ?
        }
    }

    $logger->debug("Finished $operation");
    
    return 1;

}

# ----------------------------------------------------------------------------
sub endpoint_retry_cache_import
{
    # If there's a cache file, prepend it's contents to the front of the endpoint's data structure.
    # This routine also checks the entries for being too old (see retry_cache_max_age in endpoint conf)
    # If an entry is found to be too old, it will not be added to the endpoint data, and an error will be logged
    # to the log file, and propogated back to the metrics services for visibility. 
    # Returns 1 on success, 0 if failed somehow

    my ( $endpoint_name, $cache_name, $ref_endpoint_data, $logger, $max_age, $aged_out_count_ref, $rows_read_count_ref ) = @_;
    my ( $fh, $JSONdata, $decoded_data_ref, $cached_row, @cached_data ) ;

    # If there's no cache file, thats ok
    if ( ! -e $cache_name ) {
        $logger->debug("No retry cache file " . basename($cache_name) . " found - that's ok.");
        return 1; # It's ok - the endpoint has never failed 
    }
    
    # Check will be able to read the cache
    if ( ! -r $cache_name ) {
        $logger->error("Insufficient read permission on '$cache_name' - import not possible");
        return 0;
    }

   ## If there's is a critical size limit defined for this endpoint's retry cache, see if the cache has grown so large that it needs truncating, and truncate it if it has
   #if ( defined $main::master_config->{retry_cache_limits}->{$endpoint_name}->{critical} ) { 
   #    if  ( not truncate_file_from_front( $logger, $cache_name, $main::master_config->{retry_cache_limits}->{$endpoint_name}->{critical} * 1024 * 1024 , $cache_file_truncation_message_ref ) ) { 
   #        $logger->error("An error occurred in the routine that determines if a retry cache needs to be truncated, truncating it if necessary");
   #        # TBD carry on or bail ?? The above message will propogate back into cache_errors service via send_metrics so no need to do anything else here ?
   #    }
   #}

    # Open the cache file for reading
    $fh = IO::File->new();
    ${$rows_read_count_ref} = 0 ;
    $logger->trace("TRACE Opening retry cache '$cache_name' for import i.e. read");
    $fh->open("< $cache_name"); 
    if ( not defined $fh ) {
        $logger->error("Could not open retry cache file '$cache_name' for read - $!");
        return 0;
    }

    # Read the contents of the cache
    while ( $cached_row = <$fh> ) { 
        chomp $cached_row;
        next if not $cached_row ; # skip any blank lines that might have been introduced by debugging in the field
        next if $cached_row =~ /\s*#/; # skip any comment lines that might have been introduced by debugging in the field

        # Convert JSON back into Perl
        eval { 
                $decoded_data_ref = decode_json( $cached_row ) ;
        };
        if ( $@ ) { 
            chomp $@;
            $logger->error("Could not decode JSON data from $cache_name - $@");
            return 0;
        }

        # Check if the cached entry is too old and don't import it if it is. The act of 'discarding' is really an act of not importing. 
        # Since it won't be imported, it won't be processed and it won't get back into the retry cache if a processing error occurs.
        # The culling of old aged cache entries is done at import time which is only done when a feeder object successfully has been
        # created, rather than culling on every cycle of the feeder regardless. This means the cache might end up with entries in it that
        # will need culling and that's by design. The age limit is read from the config file on each cycle. For example, an admin might 
        # decide that they want to increase the age limit before their failed standby system comes back up.
        if ( ( time() - $decoded_data_ref->{querytime} ) > $max_age ) {
            $logger->warn("Retry cache entry with timestamp $decoded_data_ref->{querytime} is older than the $max_age seconds (retry_cache_max_age) and won't be imported.");
            ${$aged_out_count_ref} ++;
        }
        else {
            push @cached_data, $decoded_data_ref; 
        }

    }

    ${$rows_read_count_ref} = scalar @cached_data;

    # prepend the data set with the cached data
    @$ref_endpoint_data = ( @cached_data, @$ref_endpoint_data );

    # Close the retry cache file. TBD check close went ok.
    $fh->close();
    
    return 1;

}
# ----------------------------------------------------------------------------
sub a_retry_cache_needs_flushing
{
    # Figures out if any endpoints retry caches require flushing (simply by seeing if content is empty or not)
    # Args :
    #   feeder name 
    #   cache dir (from master config)
    #   ref to endpoint array ref
    #   ref to retry cache info hash that gets populated by this routine
    #   ref to error that gets populated that gets populated by this routine
    # Returns :
    #   1 if :
    #      - determined that at least one retry cache requires flushing ie is non empty
    #      - a populated info hash of non empty hashes
    #      - undef error
    #   0 if :
    #       - determined that no caches need flushing, 
    #       - or an error occcured figuring it out
    #       - possibly an error if failed to figure stuff out

    my ( $feeder_name, $cache_dir, $endpoint_array_ref, $info_hash_ref, $error_ref ) = @_;
    my ( $endpoint, $endpoint_name, $endpoint_config, $cache_filename, @cache_stat, $retry_cache_size ) ;

    ${$error_ref} = undef;

    ENDPOINT: foreach $endpoint ( @{$endpoint_array_ref} ) {
        ( $endpoint_name, $endpoint_config ) = split /:/, $endpoint;
        $cache_filename = endpoint_retry_cache_name( Sys::Hostname::hostname(), $feeder_name, $endpoint_name, $cache_dir );
        @cache_stat = stat $cache_filename ;
        if ( not scalar @cache_stat ) { 
            ${$error_ref} .= "Could not stat cache file $cache_filename. ";
            next ENDPOINT;
        }
        $retry_cache_size = $cache_stat[7]; # size
        if ( $retry_cache_size != 0 ) { 
            $info_hash_ref->{$cache_filename} = $retry_cache_size ;
        }
    }

    if ( defined ${$error_ref} ) { 
        return 0 ;
    }
    else { 
        return 1 ;
    }

}

# ----------------------------------------------------------------------------
sub cleanup
{
    # cleanup attempts to remove any objects that this feeder created :
    # - hosts (will also do services and events due to db relational structure, and devices apparently too)
    # - hostgroups
    # - app types (currently excluded because status viewer can get very confused apparently)
    # Args
    #   - this - a feeder object
    #   - yes - just do it if this is defined
    #   - do_not_exit - if this is defined, just return rather than exit- for multi endpoint feeders cleanup
    # Returns
    #   if do_not_exit is defined, it returns 1
    #   otherwise exits

    my ( $this, $yes, $do_not_exit ) = @_;
    my ( $guid, %outcome_hosts, %results_hosts, %outcome_hostgroups, %results_hostgroups, %outcome, @results, 
         $query, $host_count, $hostgroup_count, %hosts_to_delete, %delete_hosts_options, $host, $hostgroup,
         %outcome_services, %results_services, $service_count, $service, @servicenames,
       ) ;

    # Get the guid / agentid for this feeder 
    $guid = $this->{properties}->{guid};

    # SERVICES (to ensure coverage of services that are attached to hosts not created by this feeder ) : get a list of servics
    $this->{logger}->info( "Getting services with agentid = '$guid'");
    $this->{rest_api}->get_services( [], { query => "agentId ='$guid'" , format => "host,service" }, \%outcome_services, \%results_services );
    $service_count = scalar keys %results_services;

    # HOSTS (and therefore services and events too) : get a list of hosts 
    $this->{logger}->info( "Getting hosts with agentid = '$guid'");
    $this->{rest_api}->get_hosts( [], { query => "agentId = '$guid'" }, \%outcome_hosts, \%results_hosts );
    $host_count = scalar keys %results_hosts;

    # HOSTGROUPS : get a list of hostgroups 
    $this->{logger}->info( "Getting hostgroups with agentid = '$guid'");
    $this->{rest_api}->get_hostgroups( [], { query => "agentId ='$guid'" }, \%outcome_hostgroups, \%results_hostgroups );
    $hostgroup_count = scalar keys %results_hostgroups;

    # Show what will be deleted 
    $this->{logger}->info( "The agentid set to $guid" );

    # services 
    if ( $service_count ) {
        $this->{logger}->info( "The following services will be removed:" );
        foreach $host ( sort keys %results_services ) {
            @servicenames = (  keys ( %{$results_services{$host}} ) ) ;
            foreach $service ( sort @servicenames ) {
                $this->{logger}->info( "\t\tHost: $host,  Service: $service" );
            }
        }
    }
    else {
        $this->{logger}->info( "No services were found - none will be removed" );
    }

    # hosts
    if ( $host_count ) {
        $this->{logger}->info( "The following hosts will be removed:" );
        foreach $host ( sort keys %results_hosts ) { 
            $this->{logger}->info( "\t\t$host" );
        }
    }
    else {
        $this->{logger}->info( "No hosts were found - none will be removed" );
    }

    # hostgroups
    if ( $hostgroup_count ) {
        $this->{logger}->info( "The following hostgroups will be removed:" );
        foreach $hostgroup ( sort keys %results_hostgroups ) { 
            $this->{logger}->info( "\t\t$hostgroup" );
        }
    }
    else {
        $this->{logger}->info( "No hostgroups were found - none will be removed" );
    }

    # Just stop if nothing to do
    if ( $host_count == 0 and $hostgroup_count == 0 and $service_count == 0 ) {
        $this->{logger}->info("Nothing to remove.");
        if ( not defined $do_not_exit ) {
            $this->{rest_api} = undef if exists $this->{rest_api};
            $this->{logger}->info( "Done - exitting."); 
            exit;
        }
        else {
            return 1;
        }
    }

    # Ask for confirmation to delete things 
    if ( not defined $yes ) {
        $this->{logger}->info ("Are you sure you want to continue ? Type in 'yes' and hit enter to remove these things ... ") ; 
        my $go = <STDIN>; chomp $go;
        if ( $go ne 'yes' ) { 
	        $this->{logger}->error("Cleanup aborted!");
            if ( not defined $do_not_exit ) {
                $this->{rest_api} = undef if exists $this->{rest_api};
                $this->{logger}->info( "Done - exitting."); 
                exit;
            }
            else { 
                return 1;
            }
        }
    }


    # Services attached to hosts that were not created by this feeder
    # Takes a hash: { host1=> { svc1=>1, svc2=>1,... },  host2=> {svc10=>1, svc12=>1}, ... }, and a ref to an options hash for delete_services();
    if ( $service_count ) {
        my $deleted_count = 0;
        foreach $host ( sort keys %results_services ) {
            @servicenames = (  sort keys ( %{$results_services{$host}} ) ) ;
            foreach $service ( @servicenames ) { # one at a time since all of them at once could make for a very long uri!
                my @servicename = ( $service ) ;
                $this->{logger}->info("Deleting service $service from host $host");
                if ( not $this->feeder_delete_services( {  $host => { $service => 1 }  }  ) ) {
                    $this->{logger}->info( "Failed to delete service $service from host $host");
                }
                else { 
                    $deleted_count++;
                }
            }
        }
        $this->{logger}->info("$deleted_count services were deleted");
    }
    else {
        $this->{logger}->info( "No services deleted") ;
    }

    # Delete hosts - do this after deleting services
    if ( $host_count ) {
	    %delete_hosts_options = ();
    	foreach ( keys %results_hosts ) { $hosts_to_delete{$_} = 1; }  
         $this->{logger}->info("Deleting hosts");
	    if  ( not $this->feeder_delete_hosts( \%hosts_to_delete, \%delete_hosts_options ) ) { 
		    $this->{logger}->info( "Failed to delete hosts" );
	    }
	    else { 
		    $this->{logger}->info( "$host_count hosts deleted" );
	    }
    }
    else {
        $this->{logger}->info( "No hosts deleted" );
    }
    
    # Delete hostgroups
    if ( $hostgroup_count ) {
         $this->{logger}->info("Deleting hostgroups");
         #if ( not $this->{rest_api}->delete_hostgroups( [ keys %results_hostgroups ] , {} , \%outcome, \@results ) ) {
         if ( not $this->feeder_delete_hostgroups( [ keys %results_hostgroups ] , {}  ) ) {
             $this->{logger}->info( "No hostgroups deleted (delete_hostgroups failed?)");
         }
         else {
             $this->{logger}->info( "$hostgroup_count hostgroups deleted");
         }
    }
    else {
        $this->{logger}->info( "No hostgroups deleted") ;
    }

    # APP TYPES : get a list of events and then try to delete them
    # Removed for now - removing the app type and then re-adding it seems to cause stack traces in status viewer.
    # Its minor to leave the app type alone this point.
   #if ( not $feeder->{rest_api}->delete_application_types( [ $feeder->{app_type} ] , {}, \%outcome_apptypes, \@results_apptypes ) ) {
   #    print "No application type '$feeder->{app_type}' deleted\n";
   #}
   #else { 
   #    print "Application type '$feeder->{app_type}' deleted\n";
   #}
 
    # flush auditing for removed stuff
    $this->flush_audit( ) ;  # this will produce its own errors if necessary
    if ( not defined $do_not_exit ) {
        $this->{rest_api} = undef if exists $this->{rest_api};
        $this->{logger}->info("Done - exitting."); 
        exit;
    }
    else {
        return 1;
    }
}

# ----------------------------------------------------------------------------
sub send_metrics
{
    # Takes a data structure containing metrics stuff and publishes these values to all endpoints defined therein (that are up).
    #
    # Args 
    #   - ref to data structure
    #   - ref to error string that will be populated here
    #   - logger object ref
    #   - a flag to indicate if retry caching is in play or not (gwevents_to_es for example doesn't use it)
    # Returns 
    #   - 1 if all ok
    #   - 0 on error, and populated error string
    #
    # NOTES: 
    # - This routine is now also called when handling interrupts to inform the <feeder>_health service. In this case, a 
    #   special $metrics{endpoints}{<endpoint>}{handling_interrupt} prop will exist.        

    # Data structure coming in should look like this :
    #
    # %metrics = (  
    #    'endpoints' =>  {
    #       <endpoint1 name> => {  # end point name is used instead of host name because might not be able to calculate that if that host is down 
    #               'services' => [  # - an array of metrics and their messages and states, for this endpoint; returned from update_feeder_stats(); possibly empty if problems earlier occurred
    #                               {
    #                                   service => "name of metric service",
    #                                   message => "service message for this metric service",
    #                                   status => "GW service status for this metric service",
    #                               }, 
    #                               {
    #                                   service => "name of metric service",
    #                                   message => "service message for this metric service",
    #                                   status => "GW service status for this metric service",
    #                               }, 
    #                               ...
    #               ],
    #               'general_errors' => "general errors" # might be not present and thats ok
    #               'caching_errors' => "errors specific to retry caching stuff", # might be unset/not present which is ok
    #               'feeder_object'  => a Feeder object handle - might be not present if that none was created due to unreachable etc
    #               'health_hostgroup'  => hostgroup for the health host
    #           },
    #           <endpoint2 name> => {
    #           }, 
    #           ...
    #   }
    # );
    #


    my ( $metrics_hash_ref, $error_ref, $logger, $not_using_caching ) = @_;
    my ( $endpoint, $something_to_do ) ;

    # see if there's anything to do - in case when all endpoints are disabled, there won't be any services to process
    $something_to_do = 0;
    foreach $endpoint ( keys %{ $metrics_hash_ref->{endpoints} } ) {
        if ( exists $metrics_hash_ref->{endpoints}->{$endpoint}->{services} ) {
            $something_to_do = 1;
            last;
        }
    }

    if ( not $something_to_do ) { 
        $logger->debug( "No feeder metrics and health service need updating"); 
        return 1;
    }
            
    $logger->info( "======== Compiling and sending feeder metrics and health service updates ========" ) ;

    # This foreach builds a datastructure of built services for ALL endpoints metrics, regardless of whether
    # they are reachable or not. This datastructure is then sent to endpoints that are up to be processed by feeder_upsert_bizservices.
    #
    # If there were errors, coerce them into a service object that can be sent to available endpoints.
    # This object is built directly into the main metrics hash by ref.
    # Assumes there are no metrics available and errors takes precedence if there were.
    my @bizservices;
    foreach $endpoint ( keys %{ $metrics_hash_ref->{endpoints} } ) {

        # Handle general feeder errors
        if ( exists $metrics_hash_ref->{endpoints}->{$endpoint}->{general_errors} ) { 
            push @{$metrics_hash_ref->{endpoints}->{$endpoint}->{services}} , {
                                             'service' => "$main::feeder_name.health", # ie the service for general errors is the main feeder health service
                                             'message' => $metrics_hash_ref->{endpoints}{$endpoint}{general_errors}, 
                                             'status'  => 'UNSCHEDULED CRITICAL' # Always crit
                                          } ;
            $logger->error( $metrics_hash_ref->{endpoints}{$endpoint}{general_errors} );
        }

        # Handle retry cache errors
        if ( exists $metrics_hash_ref->{endpoints}->{$endpoint}->{caching_errors} ) { 
            push @{$metrics_hash_ref->{endpoints}->{$endpoint}->{services}} , {
                                             'service' => "$main::feeder_name.retry.caching", 
                                             'message' => $metrics_hash_ref->{endpoints}{$endpoint}{caching_errors}, 
                                             'status'  => 'UNSCHEDULED CRITICAL' # TBD - depends  ?
                                          } ;
            $logger->error( $metrics_hash_ref->{endpoints}{$endpoint}{caching_errors} );
        }
        else { # set the service to ok - otherwise if can get stuck in a crit state, but only if not handling an interrupt, and only if using caching
            if (  ( not exists $metrics_hash_ref->{endpoints}->{$endpoint}->{handling_interrupt} ) and ( not defined $not_using_caching )  ) { 
                push @{$metrics_hash_ref->{endpoints}->{$endpoint}->{services}} , {
                                             'service' => "$main::feeder_name.retry.caching",
                                             'message' => "Ok",
                                             'status'  => 'OK'
                                          } ;
            }
        }

        # Handle cache size info and any warnings for it
        if ( exists $metrics_hash_ref->{endpoints}->{$endpoint}->{retry_cache_size} ) { 

            # If an error was triggered during getting the size of the cache, that will take precendence ...
            if ( exists $metrics_hash_ref->{endpoints}->{$endpoint}->{retry_cache_size_error} ) { 
                push @{$metrics_hash_ref->{endpoints}->{$endpoint}->{services}} , {
                                             'service' => "$main::feeder_name.retry.cache.size", 
                                             'message' => $metrics_hash_ref->{endpoints}{$endpoint}{retry_cache_size_error}, 
                                             'status'  => "UNSCHEDULED CRITICAL",
                                             'perfval' => {  retry_cache_size => 0 } # really should be NaN not 0
                                          } ;
                $logger->error( $metrics_hash_ref->{endpoints}{$endpoint}{retry_cache_size_error} );
            }

            # Otherwise, just do the usual size limit checking
            else { 
                my $retry_cache_size_status = "OK"; # default state is OK
                # If there's a cache size calculated for this endpoint's retry cache (which normally there should be unless an error occured calc'g it) ...
                if ( defined $metrics_hash_ref->{endpoints}{$endpoint}{retry_cache_size_mb} ) {

                    # If there's a critical limit set and the size exceeds it, set status to critical
                    if ( defined $main::master_config->{retry_cache_limits}->{$endpoint}->{critical} and $metrics_hash_ref->{endpoints}{$endpoint}{retry_cache_size_mb} >= $main::master_config->{retry_cache_limits}->{$endpoint}->{critical} ) {
                        $retry_cache_size_status = "UNSCHEDULED CRITICAL"; 
                        $metrics_hash_ref->{endpoints}{$endpoint}{retry_cache_size} .= "Size critical threshold is $main::master_config->{retry_cache_limits}->{$endpoint}->{critical} Mb. ";
                    }

                    # Otherwise if there's a warning limit set and the size exceeds it, set status to warning
                    elsif ( defined $main::master_config->{retry_cache_limits}->{$endpoint}->{warning} and $metrics_hash_ref->{endpoints}{$endpoint}{retry_cache_size_mb} >= $main::master_config->{retry_cache_limits}->{$endpoint}->{warning} ) {
                        $retry_cache_size_status = "WARNING"; 
                        $metrics_hash_ref->{endpoints}{$endpoint}{retry_cache_size} .= "Size warning threshold is $main::master_config->{retry_cache_limits}->{$endpoint}->{warning} Mb. ";
                    }

                    
                }

                push @{$metrics_hash_ref->{endpoints}->{$endpoint}->{services}} , {
                                         'service' => "$main::feeder_name.retry.cache.size", 
                                         'message' => $metrics_hash_ref->{endpoints}{$endpoint}{retry_cache_size}, 
                                         'status'  => $retry_cache_size_status,
                                         'perfval' => {  retry_cache_size => defined $metrics_hash_ref->{endpoints}{$endpoint}{retry_cache_size_mb} ? $metrics_hash_ref->{endpoints}{$endpoint}{retry_cache_size_mb} : 0  }
                                      } ;

                # Log message too
                if ( $retry_cache_size_status eq "UNSCHEDULED CRITICAL" ) { 
                    $logger->error( $metrics_hash_ref->{endpoints}{$endpoint}{retry_cache_size}  ); 
                }
                elsif ( $retry_cache_size_status eq "WARNING" ) { 
                    $logger->warn( $metrics_hash_ref->{endpoints}{$endpoint}{retry_cache_size}  ); 
                }
                else { # assume OK
                    $logger->debug( $metrics_hash_ref->{endpoints}{$endpoint}{retry_cache_size}  ); 
                }
            }

        }
        # Else : this always gets the size and is OK so no change currently for it to get stuck in a crit state so no need to clear it
	
        my %details;
        # Put the built services into the @bizservices array which will be used for passing to feeder_upsert_bizservices
        $details{$endpoint}{'services'} = $metrics_hash_ref->{endpoints}->{$endpoint}->{services};
        # Make sure have hostgroup being passed in - esp'y important for hosts that don't exist yet because it's endpoint was down say.
        $details{$endpoint}{'hostgroup'} = $metrics_hash_ref->{endpoints}->{$endpoint}->{health_hostgroup};
        push @bizservices, {  %details };

        # Now tag all metrics services with a dynamic property to distinguish them from other types of services.
        # This is important for when cacti feeder is deleting services.
        foreach my $svcs ( @{$metrics_hash_ref->{endpoints}->{$endpoint}->{services}} ) { 
            $svcs->{properties}->{Notes} = $metric_service_meta_tag;
        }

    };


    # Publish the metrics which now includes various info/errors about caching and general errors
    # The @bizservices data structure contains details for ALL endpoints, regardless of up or down.
    # This loop sends this data to only endpoints that are up...
    # die Dumper \@bizservices;
    ${$error_ref} = undef;
    # Get a list of all of the endpoint names from the master config for health hostgroup clean below
    my %endpoint_hosts = map { (split ':',$_)[0] => 1 }  @{ $main::master_config->{endpoint} } ;
    my ( @new_health_hostgroup, %hostgroup_options );
    foreach $endpoint ( keys %{ $metrics_hash_ref->{endpoints} } ) {
        if ( exists $metrics_hash_ref->{endpoints}->{$endpoint}->{feeder_object} ) { 

            # Send metrics over to the available endpoint
            if ( not $metrics_hash_ref->{endpoints}->{$endpoint}->{feeder_object}->feeder_upsert_bizservices( \@bizservices ) ) { 
                ${$error_ref} .= "An error occurred updating metrics for endpoint $endpoint using Feeder::feeder_upsert_bizservices(). ";
            }

            # flush endpoint's audit log since that send might have added/deleted things
            $metrics_hash_ref->{endpoints}->{$endpoint}->{feeder_object}->flush_audit( ) ; 

            # Send perf data in for metrics graphs
            if ( not $metrics_hash_ref->{endpoints}->{$endpoint}->{feeder_object}->feeder_send_metrics_perfdata( \@bizservices ) ) { 
                ${$error_ref} .= "An error occurred sending metrics performance data for endpoint $endpoint using Feeder::feeder_send_metrics_perfdata(). ";
            }

            # Clean up host group membership of health hostgroup - this is applicable to when the endpoint names have been changed 
            # and don't want the old endpoint names in the health hostgroup
            # Get host membership of health hostgroup for this endpoint ...
            my $feeder_health_groupname = $metrics_hash_ref->{endpoints}->{$endpoint}->{feeder_object}->{health_hostgroup};
            my %health_hostgroup_members = (  $feeder_health_groupname => undef );
            $metrics_hash_ref->{endpoints}->{$endpoint}->{feeder_object}->feeder_get_hostgroup_members( \%health_hostgroup_members ) ;
            # If hosts are found in the health host group, that are NOT in the endpoint set, make a note that the health 
            # host group needs resetting/refreshing ie clearing and reassigning.
            my $health_hostgroup_contents_needs_refreshing = 0;
            foreach my $healthgroup_host ( keys %{ $health_hostgroup_members{ $feeder_health_groupname }{members} } ) { 
                if ( not exists $endpoint_hosts{ $healthgroup_host } ) { 
                    $health_hostgroup_contents_needs_refreshing = 1;
                    last;
                }
            }

            if ( $health_hostgroup_contents_needs_refreshing ) {
                # remove the old members 
                $metrics_hash_ref->{endpoints}->{$endpoint}->{feeder_object}->feeder_clear_hostgroups( [ $feeder_health_groupname ] ); 
                # add the new members which should all exist already
                push @new_health_hostgroup, 
	                {
	                    "name"        => $feeder_health_groupname,
	                    "hosts"       => [ keys %endpoint_hosts ],
                       # Only need name and hosts props since everything should exist already.
	                   #"alias"       => $feeder_health_groupname,
	                   #"description" => "$metrics_hash_ref->{endpoints}->{$endpoint}->{feeder_object}->{feeder_name} virtual hostgroup", 
	                   #"agentId"     => $cs_hash_ref->{endpoints}->{$endpoint}->{feeder_object}->{guid}, 
	                };

                $metrics_hash_ref->{endpoints}->{$endpoint}->{feeder_object}->feeder_upsert_hostgroups( \@new_health_hostgroup, \%hostgroup_options );

            } # refresh required test

        } # feeder object existence test

    } # endpoint loop
    
    if ( defined ${$error_ref} ) { 
        return 0; 
    } 
    else {
        return 1;
    }

}

# -------------------------------------------------------------
sub stage_error_for_publishing_via_metrics
{
    # Logs an error, and APPENDS that error to a metrics staging key for reporting to the rest of the cluster if applicable.
    # Args :
    #   - logger handle
    #   - ref to metrics data structure
    #   - name of endpoint being processed
    #   - the error message
    #   - error key - this is general_errors, caching_errors etc (could be called the staging key)
    #   - do_not_log flag - if set, don't log message
    # The name of the service that the errors will be sent to is determined in Feeder::send_metrics
    # Returns: nothing.
    my ( $logger, $metrics_ref, $endpoint_name, $message, $error_key, $do_not_log ) = @_;
    $metrics_ref->{endpoints}->{$endpoint_name}->{$error_key} .= "$message "; # for appending info to data structure
    $logger->error( $message ) if not defined $do_not_log; # for logging
}

# -------------------------------------------------------------
sub truncate_file_from_front
{
    # Truncates file *from the beginning*. 
    # This routine is intended to be used for shrinking retry caches from the beginning ie oldest entries removed first.
    # File format is expected to be flat, ie not binary, with new-lines for end of lines.
    # The file should not be growing while this operation is happening.
    # The bit that will remain after front-truncation will be read into memory so the system memory resources need to accomodate.
    # Args : 
    #   - logger - a handle to a logger object
    #   - filename - the file to be truncated
    #   - size_limit - the size in bytes to which the file should be shrunk to
    #   - error_ref - ref to an error string that will be populated by this routine if necessary
    # Returns :
    #   - 1 if all ok and a truncated file if necessary
    #   - 0 if error
    # NOTES
    # - no backup is made of the file first since the intent of the truncation is to avoid a disk getting full up

    my ( $logger, $filename, $size_limit, $error_ref ) = @_;

    my ( $file_size, $handle, $nextchar, $new_file_content, $position, $giveup, $msg, $new_size );

    ${$error_ref} = undef;
    $giveup = "Giving up - no truncation will be done";

    # If there's no file ... bail
    if ( ! -e $filename ) {
        ${$error_ref} = "File '$filename' was not found. $giveup.";
        $logger->error( ${$error_ref} ) ;
        return 0; 
    }
    
    # If its not readable ... bail
    if ( ! -r $filename ) {
        ${$error_ref} = "File $filename cannot be read - no truncation will be done.";
        $logger->error( ${$error_ref} ) ;
        return 0;
    }

    # If the size of the file is already smaller than the size limit, just return
    $file_size = -s $filename ;

    if ( $file_size <= $size_limit ) {
        #${$error_ref} = "File doesn't need truncating.";
        #$logger->debug( ${$error_ref} ) ;
        $logger->debug( "Retry cache $filename doesn't need truncating.") ; # just log this info if debug
        # Not an error but caller should check return status first :)
        return 1;
    };

    # Open the file for reading
    open ( $handle, $filename ) or do { 
        ${$error_ref} = "Failed to open $filename. $giveup. $!";
        $logger->error( ${$error_ref} ) ;
        return 0;
    };

    # Position the read pointer at the ideal position from the end of the file
    seek ( $handle, $file_size - $size_limit, 0 ) or do {
        ${$error_ref} = "Seek failed in file $filename. $giveup. $!";
        close $handle or ${$error_ref} .= "Failed to close file $filename. $!"; # try to close the file too
        $logger->error( ${$error_ref} ) ;
        return 0;
    };

    # Search for a newline char from here, and stop when found, or when the position of read pointer is at the end of the file.
    # This works best if the file is not growing :) If this routine needs to work with a growing file, then need to keep getting
    # the file size. For now this isn't necessary.

    $position = tell $handle; # get the current position of the read pointer in the file
    # If tell fails it will return -1
    if ( $position == -1 ) {
        ${$error_ref} = "Initial tell() failed for file $filename - unable to determine current position in file. $giveup. $!";
        close $handle or ${$error_ref} .= "Failed to close file $filename. $!"; # try to close the file too
        $logger->error( ${$error_ref} ) ;
        return 0;
    }

    $nextchar = "";
    while ( $nextchar !~ /\n/ and $position < $file_size ) {
        read ( $handle, $nextchar, 1 ) or do {
            ${$error_ref} =  "read() failed for file $filename - unable to get next character. $giveup. $!";
            close $handle or ${$error_ref} .= "Failed to close file $filename. $!"; # try to close the file too
            $logger->error( ${$error_ref} ) ;
            return 0;
        };
        $position = tell $handle; 
        if ( $position == -1 ) {
            ${$error_ref} =  "tell() failed for file $filename - unable to determine current position in file. $giveup. $!";
            close $handle or ${$error_ref} .= "Failed to close file $filename. $!"; # try to close the file too
            $logger->error( ${$error_ref} ) ;
            return 0;
        }
    }

    # Reads the truncated remnants into memory prior to writing it out to the file.
    # If the reading above to find the next new line pushed the read pointer out to the end of the file (for example in unlikely
    # case of the size limit being less than the size of a line of cache content), then doing a read will result in an error and 
    # no truncation would happen. So first set the new file content to be empty, and then only do a read if there's left to read.
    $new_file_content = "";
    if ( tell $handle < $file_size ) {
        read ( $handle, $new_file_content, $size_limit ) or do { 
            ${$error_ref} =  "read() failed for file $filename - unable to read content into memory. $giveup. $!";
            close $handle or ${$error_ref} .= "Failed to close file $filename. $!"; # try to close the file too
            $logger->error( ${$error_ref} ) ;
            return 0;
        };
    }

    # close the file after having read the selected chunk from it
    close $handle or do {
        ${$error_ref} .= "Failed to close file $filename prior to writing new content. $giveup. $!"; 
        $logger->error( ${$error_ref} ) ;
        return 0;
    };

    # open the file to write the selected chunk out to it
    open ($handle, ">$filename" ) or do {
        ${$error_ref} = "Failed to open $filename for write. $giveup. $!";
        $logger->error( ${$error_ref} ) ;
        return 0;
    };
    
    # writes the relevant bit out to the file - now it's truncated
    if ( not print $handle $new_file_content ) { 
        ${$error_ref} = "Failed to write new content to $filename. $!";
        $logger->error( ${$error_ref} ) ;
        return 0;
    }

    # close the file
    close $handle or do {
        ${$error_ref} .= "Failed to close file $filename after writing new content. $!"; 
        $logger->error( ${$error_ref} ) ;
        return 0;
    };

    # Got this far so all ok ie the cache file needed truncating and was truncated successfully
    $new_size = -s $filename ;
    #${$error_ref} = sprintf "Retry cache file " . basename($filename) . " was truncated from to $new_size bytes (%0.3f Mb)", $file_size/(1024*1024), $new_size/(1024*1024);
    #${$error_ref} = sprintf "Retry cache file " . basename($filename) . " was truncated to %0.3f Mb (critical threshold is %f)", $new_size/(1024*1024), $size_limit/(1024*1024);
    ${$error_ref} = sprintf "Retry cache file " . basename($filename) . " was truncated to %0.3f Mb", $new_size/(1024*1024);
    $logger->error( ${$error_ref} ) ;
    return 1;

}

# -------------------------------------------------------------
sub initialize_logger 
{
    my ( $phase, $master_config ) = @_;

    my ( $config );

    eval {
        $config          = TypedConfig->new($master_config);
        $main::log4perl_config = $config->get_scalar('log4perl_config');
        $main::logfile         = $config->get_scalar('logfile');
    };
    if ($@) {
        chomp $@;
        $@ =~ s/^ERROR:\s+//i;
        print("ERROR Cannot read config file $master_config\n    ($@)\n") ;
        return 0;
    }

    ## Basic security:  disallow code in the logging config data.
    Log::Log4perl::Config->allow_code(0);

    # Here we add custom logging levels to form our full standard complement.  There are six
    # predefined log levels:  FATAL, ERROR, WARN, INFO, DEBUG, and TRACE (in descending priority).
    # We add NOTICE and STATS levels to the default set of logging levels supplied by Log4perl,
    # to form the full useful set:  FATAL, ERROR, WARN, NOTICE, STATS, INFO, DEBUG, and TRACE
    # (excepting NONE, I suppose, though there is some hint in the code that OFF is also supported).
    # This *must* be done before the call to Log::Log4perl::init().
    if ( $phase eq 'started' ) {
     Log::Log4perl::Logger::create_custom_level( "NOTICE", "WARN" );
     Log::Log4perl::Logger::create_custom_level( "STATS",  "NOTICE" );
    }

    # If we wanted to support logging either through a syslog appender (I'm not sure how this would
    # be done; presumably via something other than Log::Dispatch::Syslog, since that is still
    # Log::Dispatch) or through Log::Dispatch, the following code extensions would come in handy.
    # (Frankly, I'm not really sure that Log4perl even supports syslog logging other than through
    # Log::Log4perl::JavaMap::SyslogAppender, which just wraps Log::Dispatch::Syslog.)
    #
    # use Sys::Syslog qw(:macros);
    # use Log::Dispatch;
    # my $log_null = Log::Dispatch->new( outputs => [ [ 'Null', min_level => 'debug' ] ] );
    # Log::Log4perl::Logger::create_custom_level("NOTICE", "WARN", LOG_NOTICE, $log_null->_level_as_number('notice'));
    # Log::Log4perl::Logger::create_custom_level("STATS", "NOTICE", LOG_INFO, $log_null->_level_as_number('info'));

    # This logging setup is an application-global initialization for the Log::Log4perl package, so
    # it only makes sense to initialize it at the application level, not in some lower-level package.
    #
    # It's not documented, but apparently Log::Log4perl::init() always returns 1, even if
    # it is handed a garbage configuration as a literal string.  That makes it hard to tell
    # if you really have it configured correctly.  On the other hand, if it's handed the
    # path to a missing config file, it throws an exception (also undocumented).
    eval {
	    ## If the value starts with a leading slash, we interpret it as an absolute path to a file that
	    ## contains the logging configuration data.  Otherwise, we interpret it as the data itself.
	    Log::Log4perl::init( $main::log4perl_config =~ m{^/} ? $main::log4perl_config : \$main::log4perl_config );
    };
    if ($@) {
	    chomp $@;
	    print "ERROR:  Could not initialize Log::Log4perl logging:\n$@\n";
	    return 0;
    }

    # Note that there seems to be no kind of error return for get_logger().
    # So if the configuration is wrong, we don't get warned about that here.
    $main::logger = Log::Log4perl::get_logger($main::feeder_name);

    $SIG{__WARN__} = sub { $main::logger->warn("WARN @_"); }; 

    # The __DIE__ handler catches Perl run-time errors and could be used to get them logged.  But
    # it also captures lots of internal detail that is caught by eval{}; statements, that has no
    # business being logged.  So we use a better mechanism, provided below, for capturing Perl
    # errors, by explicitly redirecting STDERR to the logfile.
    # $SIG{__DIE__} = sub { $logger->fatal("DIE @_"); };

    if ( not open STDERR, '>>', $main::logfile ) {
        $main::logger->logdie("FATAL:  Cannot redirect STDERR to the log file \"$main::logfile\": $!");
    }

    return 1;
}

# -------------------------------------------------------------
sub remove_host_from_hostgroup
{
    # Takes a host and a hostgroup and removes host from hostgroup.
    # To do this, due to the way the api works, you have to :
    # 1. get a list of hostgroup members and modify that list by removing the host (if it's there)
    # 2. clear the hostgroup
    # 3. update the hostgroup with the modified list from step 1
    # ie there's no api method to just remove a host from a hostgroup - it's clear and reset
    #
    # Args
    #   - this
    #   - hostname
    #   - hostgroup
    #   - error ref
    # 
    # Returns
    #   - 1 ok and host removed from hostgroup
    #   - 0 not ok, and error set by ref

    my ( $this, $host, $hostgroup, $error_ref ) = @_;

    my ( %hostgroup_members, %outcome, %results, $description );

    $this->{logger}->debug("Removing host '$host' from hostgroup '$hostgroup'");

    # Get a list of hosts in this group
    %hostgroup_members = ( $hostgroup => undef );
    $this->feeder_get_hostgroup_members( \%hostgroup_members ) ;

    # update the list or just bail if host not in group
    if ( exists $hostgroup_members{$hostgroup}{members}{$host} ) { 
        delete $hostgroup_members{$hostgroup}{members}{$host}; # remove the host from the new list of hosts 
    }
    else { 
        $this->{logger}->debug("Host '$host' is not a member of hostgroup '$hostgroup' - nothing to do.");
        return 1;
    }

    # Get the existing description of the hostgroup
    if ( not $this->{rest_api}->get_hostgroups( [ $hostgroup ], {}, \%outcome, \%results ) ) {
        ${$error_ref} .= "Failed to get details for hostgroup '$hostgroup'";
        return 0;
    }

    # clear the hostgroup
    $this->{logger}->debug("Clearing hostgroup '$hostgroup'");
    if ( not $this->feeder_clear_hostgroups( [ $hostgroup ] ) ) { 
        ${$error_ref} .= "remove_host_from_hostgroup Failed to clear hostgroup '$hostgroup'";
        return 0;
    }

    # Build the data structure required bu feeder_upsert_hostgroups for building the hostgroup
    my %hostgroup_options = ();
    my @hostgroups = (
	    {
	        "name"        => $hostgroup,
	        "alias"       => $hostgroup,
	        "description" => $description,
	        "hosts"       => [ keys %{$hostgroup_members{$hostgroup}{members}} ],
	        "agentId"     => $this->{guid},
	    }
    );

    # rebuild the hostgroup
    $this->{logger}->debug("Building hostgroup '$hostgroup'");
    if ( not $this->feeder_upsert_hostgroups( \@hostgroups, \%hostgroup_options ) ) {
        ${$error_ref} = "remove_host_from_hostgroup Failed to upsert hostgroup.";
        return 0;
    }

    return 1;


}

__END__




1;


#!/usr/local/groundwork/perl/bin/perl

# SCOM feeder - integrates SCOM with GroundWork 
#
# Copyright 2015 GroundWork OpenSource
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# Revision History
#        Dominic Nicholas 3/15 - 2.0.0 - Initial revised version for GW 7.0.2
#        Dominic Nicholas 3/15 - 2.0.1 - Some code clean-up; help improved
#        Dominic Nicholas 4/15 - 2.0.2 - cleanup() : removed app type removal
#        Dominic Nicholas 5/15 - 2.0.3 - updates to correctly update Since date/times and use associated Feeder.pm v0313
#                                      - timezone of event data now correctly set to utc (-0000) instead of %z in build_2012_event()
#        Dominic Nicholas 5/15 - 2.0.4 - refactoring of main loop and some error handling for resiliency to GW services being down; 
#        			       - fixed up cleanup() for long host lists
#        			       - added beginnings of perf data hooks  - at least will provide record 
#        Dominic Nicholas 5/15 - 2.0.5 - added missing close() to perf data file write block of metrics push
#        Dominic Nicholas 5/21 - 2.0.6 - build_2012_event() : if host_down_criteria, but resolutionstate=255, the host is marked as UP
#        Dominic Nicholas 5/21 - 2.0.6 - post_events_and_notifications() : host/services new to collage now create events and notifications on first add
#        Dominic Nicholas 7/10 - 2.0.7 - added -use_save_table option, and hostname_constraint_filters and servicename_constraint_filters added
#        Dominic Nicholas 7/17 - 2.0.8 - temporary change until fully migrated to 710 Feeder.pm and so not to interfere with unpatched 702 cacti feeder,
#                                        the location of Feeder.pm needs to be put in its own /u/l/g/SCOM/GW/ directory
#        Dominic Nicholas 7/2015 - 3.0.0 - refactored to use same pattern as other latest feeders : multi endpoints, and retry caching , latest Feeder.pm etc
#                                        and numerous other minor updates, improvements and fixes
#        Dominic Nicholas 10/2015 - 3.0.1 - refactored way new feeder objects get created to aviod issue with GW not releasing auth tokens in a timely fashion
#        Dominic Nicholas 10/2015 - 3.0.2 - lc of hostname
#        Dominic Nicholas 3/2016  - 3.1.0 - cluster metrics, logging config, error propogation into metrics, etc
#        Dominic Nicholas 9/2016  - 3.1.1 - Only re-read master config file if its mod time changed
#        Dominic Nicholas 11/22/2016  - 3.1.2 - line 2203 minor update to make it work with Perl 5.24 in 7.1.1
#
# TO-DO
# - feeder should wait for new scom events if there's something in the cache to work on
# - Full updates system and test tweaks systems - if required in the field then add it
# - Add option to report on save table by category or move them back into events table etc 
#   - need field input on what versions are of interest before doing this.
# - Generalize build event routine and associated logic to support different versions 
#   - need field input on what versions are of interest before doing this.
#   - create $feeder->update_feeder_stat() that does everything this one does that can be used by all other feeders.
# - add index to scom db tables ?!  http://www.postgresql.org/docs/9.1/static/indexes.html
# - figure out why scom_feeder_host is left in gwcollagedb::device table after -clean runs.

use 5.0;
use warnings;
use strict;
use version; 
my $VERSION = qv('3.1.2'); 
use GW::Feeder qv('0.5.4'); 
use JSON;
use Data::Dumper; $Data::Dumper::Indent = 2; $Data::Dumper::Sortkeys = 1;
use Log::Log4perl qw(get_logger);
use Getopt::Long;
use Time::HiRes;
use POSIX qw(strftime);
use Time::Local;
use Sys::Hostname;
use TypedConfig qw(); # leave qw() on to address minor bug in TypedConfig.pm
use DBI;
use File::Basename;
use HTTP::Date qw(str2time); # for converting ISO 8601 timestamps from the scom events to epoch time
use Array::Compare; 

our $feeder_name = "scom_feeder"; # Feeder name var - will key various things off this ('our' cos Feeder.pm also uses this)
our ( $logger, $log4perl_config, $logfile, $master_config );
our $fmee_timestamp=-1; # This is used in Feeder.pm for testing with fmee
my $master_config_file = '/usr/local/groundwork/config/scom_feeder.conf';
my ( $database_handle, $feeder, $tests_config, %feeder_objects ) = undef; # various globals
my ( $help, $show_version, $once, $clean, $show, $yes, $limit, $use_save_table, $every ) = undef; # CLI option vars
# SCOM specifics
my %supported_scom_versions = ( "SCOM_2012_v0" => undef ); # supported version checking
my $scom_events_table = 'scom_events'; # db table for to-be-processed events
my $scom_save_table   = 'scom_save'; # db table for saved events
my ( $purge_marker ) = '__purge__'; # built event field for purge reason
my $printed_hdr = 0; # for show mode
my @query_data = undef; # Various globals
my %showstats = ();
my %feeder_services = (
     "$feeder_name.endpoint.processing.time" => "Time taken to process the endpoint",
     "$feeder_name.events.processed"         => "Metrics on events processed",
     "$feeder_name.cycle.end.queue.size"     => "The number of events waiting in the scom_events table at the end of this cycle"
);

# =================================================================================
main();
# =================================================================================

# ------------------------------------------------------------------------------------------------------------
END {

    exit if $show; # don't wait around if show mode - no rest api was in play

    # To be kind to the server and always disconnect our session, we attempt to force a shutdown
    # of the REST API before global destruction sets in and makes it impossible to log out,
    # regardless of how we got to the end of the program.
    terminate_rest_api();

    # We generally run this daemon under control of supervise, which will immediately attempt to
    # restart the process when it dies.  In order to prevent a tight loop of failure and restart,
    # we delay process exit a short while no matter how we're going down.
    sleep(5) if not ( $once or $clean or $help or $show_version );
}

# ---------------------------------------------------------------------------------
sub main
{
    my ( $cycle_start_time, $cycle_count, $total_cycle_time_taken, $try, $max_retries, $disabled_notice_given, $error, 
         $started_message, $sync_status, $total_events, $total_events_processed, $aged_out_events_count, $resolved_events_count, 
         $maintenance_events_count, $events_other_count , %feeder_options, %master_config, $endpoint, $endpoint_name, $endpoint_config,
         @endpoint_data, $retry_cache_filename, $timestamped_dataset, @built_timestamped_data, $successfully_processed_built_rows,
         $endpoint_start_time, $sum_of_total_events , $sum_of_total_events_processed , $sum_of_aged_out_events_count , $purge_error_count,
         $sum_of_resolved_events_count , $sum_of_maintenance_events_count , $sum_of_events_other_count , $data_failed_to_processed_ok, $sum_of_purge_error_count,
         $sum_of_events_filtered_count, $events_filtered_count, $scom_events_count_error, $get_data_error, %metric, $init_db_connection_error ,
         $endpoint_health_hostgroup, %metrics, $error_message, $cache_file_truncated_message, $count_of_data_rows_cached, %retry_cache_size,
         $aged_out_count, $cache_imported_rows_count, $build_error, $sync_error, $endpoint_enabled
    );

    $data_failed_to_processed_ok = 0; # needs to defined at least.

    # v3.1.1 : This will be used to control re-reads of master config file in the main CYCLE loop. Seeded with 0 to force the first read.
    my $master_config_last_mod_time = 0;

    # read and process cli opts
    initialize_options(); 

    # get logger details
    if ( not initialize_logger('started', $master_config_file ) ) {
        print "Cannnot initialize logger - quitting!\n";
        exit;
    }

    # check for other feeders running - there can only be one
    if ( not $show ) {  # allow multiple to run if -show mode is in use tho
        $logger->logexit("Another $feeder_name is running - quitting") if ( perl_script_process_count( basename($0) ) > 1 ) ; 
    }

    # log app start
    if ( $once ) { 
        $started_message = "$feeder_name running once on " . Sys::Hostname::hostname() . " started at " . localtime() ;
    } 
    else {
        $started_message = "$feeder_name started " . Sys::Hostname::hostname() . " at " . localtime() ;
    }
    $logger->info( $started_message ) ;

    # Get SCOM events data, build it and sync the endpoint(s) in a never ending cycle
    $cycle_count = 1; $disabled_notice_given = 0;
    CYCLE: while ( 1 ) {
        
        $cycle_start_time = Time::HiRes::time(); # will be used to calc entire cycle time for all all operations and endpoints

        if ( not $disabled_notice_given ) { 
            if ( $show ) { 
                $logger->info( ">>>>>>>> Starting showmode cycle <<<<<<<<" ) ;
            }
            else { 
                $logger->info( ">>>>>>>> Starting cycle $cycle_count <<<<<<<<" ) ;
            }
        }

	# v3.1.1 Only (re)read the master config if the mast mod time changed
        if ( $master_config_last_mod_time != ( stat( $master_config_file ) )[9] ) { 
	    $master_config_last_mod_time = ( stat( $master_config_file ) )[9] ;
            # Moved to inside the main cycle loop so the master config so can change settings without restarting the main feeder
            # Read the master configuration - this will determine the GW server set aka the REST endpoint set, and other global settings
            if ( not read_master_config( $master_config_file ) ) { 
                $logger->error("Failed to read feeder's master configuration file '$master_config_file' - waiting for a minute before restarting processing cycle...") ;
                sleep 60; 
                next CYCLE;
            }
        }

        # Go to sleep if feeder is disabled, but wait quietly checked for being enabled without filling up a log file about it.
        if ( not $master_config->{feeder_enabled} ) {
            $logger->info("Feeder is disabled. Set feeder_enabled = yes in $master_config_file to enable it. The feeder will quietly wait for 1 minute and recheck to see if it's enabled.") if not $disabled_notice_given;
            $disabled_notice_given = 1; # make a note that this noticed has been logged
            sleep 60; 
            next CYCLE;
        }
        else {
            $disabled_notice_given = 0; # feeder is enabled so reset any disabled notice memory
        }

        # Set options up for feeder objects
        %feeder_options = (
            # The log4perl logger
            logger => $logger, 
            # Feeder specific options to retrieve and type-check.
            # Access standard or specific properties with $feeder->{properties}->{propertname}, eg $feeder->{properties}->{cycle_time}
            feeder_specific_properties => { 
                                            #always_send_full_updates => 'boolean', # unimplemented for now
                                            constrain_to_hostgroups => 'hash',
                                            custom_hostname_mapping_rules => 'hash',
                                            default_hostgroups => 'hash',
                                            #full_update_frequency => 'number', # unimplemented for now
                                            host_bundle_size => 'number',
                                            host_down_criteria => 'hash',
                                            hostgroup_bundle_size => 'number',
                                            hostname_exclusion_filters => 'hash',
                                            hostname_constraint_filters => 'hash',
                                            hostname_hostgroup_mappings => 'hash',
                                            process_resolved_events => 'boolean',
                                            process_in_maintenance => 'boolean',
                                            retry_cache_max_age => 'number',
                                            save_to_db => 'hash',
                                            service_bundle_size => 'number',
                                            servicename_constraint_filters => 'hash',
                                            servicename_exclusion_filters => 'hash',
                                            servicename_hostgroup_mappings => 'hash',
                                            #system_indicator_file => 'scalar',
                                            #test_tweaks_file => 'scalar',
                                        }
        );

        # Set up interrupt handlers for various signals. 
        # Notification of the interrupt will attempted to be sent to each endpoint.
        # This could use some more work since it doesn't work that well if the feeder is waiting for something to do.
        initialize_interrupt_handlers( $master_config->{endpoint}, \%feeder_options);

        # check/prepare retry cache directory
        if ( not endpoint_retry_cache_prep_dir( $logger, $master_config->{retry_cache_directory} ) ) {
            $logger->error("A problem was found with the retry cache directory - waiting for a minute before restarting processing cycle...");
            sleep 60; 
            next CYCLE;
        }

        # Establish a scom db connection at the start of the cycle, and close it at the end,
        # because many of the operations require it at different times like counting events, getting data, and then purging

        if ( not initialize_database_connection( \$init_db_connection_error ) ) { 
           #$logger->error("Error initializing connection to GW scom database to count rows from $scom_events_table - sleeping 30 and restarting cycle");
           $logger->error("Error initializing connection to GW scom database to count rows from $scom_events_table.");
           #sleep 30;
           #next CYCLE;
        }

        if ( not $clean and not defined $init_db_connection_error ) {  # don't do any of this if db init error, or running clean
            # Wait until there's an indication to proceed. 
            # In the case of the SCOM feeder, that means wait for some rows in the scom_events table to show up from the reaper insertion.
            # Note/TBD
            #  - Stuff in retry caches should not have to wait for new scom events in order to be imported and processed with that new event set 
            #  - At one point thought it would be nice to address this, but :
            #       - this requires a bunch of regression testing 
            #       - with this commented line in place, empty entries like '{"querytime":"1438192710","rows":[],"rows_built_data":[]}' would be added 
            #         to the retry cache that had real data in it. That was another pile to test and get working. 
            #       - needs more thought on best way to address this - possibly in Feeder::endpoint_retry_cache_write() etc.
            #  - Revisit this later if required
            # while ( not scom_events_count( \$error ) and not a_cache_needs_flushing() ) {  

            while ( not scom_events_count( \$scom_events_count_error ) ) {
                if ( defined $scom_events_count_error ) {
                    # Make this error propogate into the metrics services - ie do nothing other than set the scom events count error
                    #$logger->error( $scom_events_count_error . " - Sleeping 10 before continuing to next cycle" ) ;
                    #sleep 10;  
                    #next CYCLE; 
                }
                else {
                    $logger->info("Waiting for $master_config->{system_indicator_check_frequency} seconds before checking for something to do...");
                    sleep $master_config->{system_indicator_check_frequency};
                }
            }
    
            # Populates the @query_data global data structure.
            if ( not get_data( \$get_data_error ) ) { 
                # Make this error propogate into the metrics services ie do nothing other than set the get_data_error
                #$logger->error("A problem occurred getting the data for the feeder! Trying again in 30 seconds.");
                #sleep 30;
                #next CYCLE;
            }
        }

        # If no endpoints are enabled, don't go into a tight processing loop, but wait for a while and next CYCLE instead
        if ( endpoints_enabled( $master_config->{endpoint}, \$error ) == 0 ) {
            if ( defined $error ) {
                $logger->error("Problem calculating number of enabled feeders : $error");
            }
            $logger->info("No endpoints are enabled - sleeping 60 before processing next cycle"); 
            sleep 60;
            next CYCLE;
        }

        # Need to wait for a while if the data didn't process ok on last cycle to avoid a crazy tight loop, before attempting processing again normally
                # case : sync_endpoint failed on last cycle for example (eg in purge_event() say)
                if ( $data_failed_to_processed_ok ) { 
                    $logger->error("Data not processed on last cycle - sleeping 30 seconds before continuing");
                    sleep 30;
                }

                # Initialize the metrics data structure
                %metrics = ( );

                # Synchronize each endpoint, in the order they are specified in the master config file
                ENDPOINT: foreach $endpoint ( @{$master_config->{endpoint}}  ) {

                    $endpoint_start_time = Time::HiRes::time(); # start timer for endpoint processing

                    # initialize the endpoint overall metrics used across all datasets (ie case of retry caching)
                    $sum_of_total_events = $sum_of_total_events_processed = $sum_of_aged_out_events_count = $sum_of_resolved_events_count = 0 ; 
                    $sum_of_maintenance_events_count = $sum_of_events_other_count = $sum_of_purge_error_count = $sum_of_events_filtered_count = 0;

                    # Make a copy of the raw data for processing just this endpoint. 
                    # Later unprocessed data will be put in this endpoints retry cache at the end of this ENDPOINT iteration.
                    @endpoint_data = @query_data; 

                    # Extract the endpoint name and the endpoint configuration file from the master feeder endpoint config list
                    # read_master_config() has validated the content of $endpoint 
                    ( $endpoint_name, $endpoint_config ) = split /:/, $endpoint;

                    # For show mode, need to do a couple of things ...
                    if ( $show ) {
                        $printed_hdr = 0 ; # reset required here so each endpoint dump gets shown with headers
                        remove_show_file( $endpoint_name ); # remove the target show file
                        %showstats = ();
                    }

                    # If the endpoint's config says that it's disabled, dont add the health hostgroup (otherwise Feeder::send_metrics will create a retry.caching service
                    # an it'd be cleaner if there weren't any services for disabled endpoint).
                    $endpoint_enabled = 0;
                    get_one_prop_from_conf ( 'enable_processing', $endpoint_config, \$endpoint_enabled, \$error ) ;

                    if ( $endpoint_enabled ) { 
                        # Figure out which hostgroup the feeder health host belongs to - this is used later in metrics and needs to be pulled out explicitly
                        # from the endpoint conf now, since if it's down then there'll be no Feeder object to reference it via.
                        # Need this in the case of endpoint down, and need to grab stuff from it for metrics reporting.
                        if ( not get_one_prop_from_conf ( 'health_hostgroup', $endpoint_config, \$endpoint_health_hostgroup, \$error ) ) {
                            $error_message = "Couldn't extract health_hostgroup prop value from config file '$endpoint_config'" ;
                            $error_message .=  " : $error" if defined $error;
                            stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $error_message, 'general_errors');
                            # TBD decide what else to do if this fails ?
                        }
                        else {
                            $metrics{endpoints}{$endpoint_name}{health_hostgroup} = $endpoint_health_hostgroup;
                        }
                    }
                    else { 
                        # This is helpful since if the endpoint was at some point enabled, and then disabled, this will clue the user in.
                        $error_message = "Feeder endpoint $endpoint_name is currently disabled. To enable it, set enable_processing = yes in $endpoint_config. To remove this virtual host and it's services altogether, remove the endpoint:$endpoint_name line from $master_config_file";
                        stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $error_message, 'general_errors');
                    }
                    
                    $logger->info( "======== Processing endpoint ::: '$endpoint_name' ========" ); 

                    # 3.0.1 - Removed this to make it persistent and let RAPID do re-auth's instead
                    # Destroy feeder if defined - expecting this to invoke the $feeder->DESTROY() automagically and that this is the last reference in use.
                    #undef $feeder if defined $feeder; 

                    # Create a retry cache filename for this hostname, feeder and endpoint combo
                    $retry_cache_filename = endpoint_retry_cache_name( Sys::Hostname::hostname(), $feeder_name, $endpoint_name, $master_config->{retry_cache_directory} ) ;

            # As part of 3.0.1 auth token cache update ...
            # If a GW::Feeder feeder object has not yet been created for this endpoint, go try create one
            if ( not exists $feeder_objects{$endpoint_name} ) {
                $try = 1;
                defined $master_config->{endpoint_max_retries} ? $max_retries = $master_config->{endpoint_max_retries} : 1;

                while ( not ( $feeder = GW::Feeder->new( $feeder_name, $endpoint_config, \%feeder_options, $endpoint_name ) ) and $try <= $max_retries ) {
                    $logger->error("Couldn't create feeder object for endpoint '$endpoint_name' - try $try/$max_retries - waiting to try again");
                    defined $master_config->{endpoint_retry_wait} ? sleep $master_config->{endpoint_retry_wait} : sleep 5; 
                    $try++;
                }
                # If failed to create a new feeder endpoint after retrying, skip the endpoint for now.
                # Before moving on to the next endpoint, append the queried data onto this endpoint's retry cache.
                if ( $try > $max_retries ) {
                    #$logger->error("Couldn't create feeder object for endpoint '$endpoint_name' - updating its retry cache and ending processing attempt."); 
                    $error_message = "Feeder host $GW::Feeder::feeder_host : Couldn't create feeder object for endpoint '$endpoint_name' - updating its retry cache and ending processing attempt.";

                    # Add the error coming back from GW::Feeder (possibly passed along from RAPID) too here. This is useful in the case of say the ws_client.props not being readable. This 
                    # way it'll actually make it back into the status viewer - easier than tracking it down from the log file.
                    if ( defined $@ ) { 
                        $@ =~ s/\n//g ;
                        $error_message .= $@ ;
                    }
                    stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $error_message, 'general_errors');
                    $cache_file_truncated_message = undef;

                    # Append the data to the retry cache.
                    if ( not endpoint_retry_cache_write( $endpoint_name, $retry_cache_filename, \@endpoint_data, $logger, "a", \$count_of_data_rows_cached, \%retry_cache_size, \$cache_file_truncated_message ) ) {
                        # TBD In the event of not being able to create a feeder, and not write data to it's cache, do what? exit and depend on feeder process monitoring
                        # TBD to highlight this problem ? Monitor the feeder log for various strings ? Probably the latter although a bit clunky. Can't send error messages
                        # TBD to the feeder health services since it's feeder object isn't available
                        # For now the error is made visible via the health service for the feederd
                        # $metrics{endpoints}{$endpoint_name}{errors} .= "Failed to update the retry cache! $cache_file_truncated_message";
                        # $logger->error( $metrics{endpoints}{$endpoint_name}{errors} );
                        #stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, "Failed to update the retry cache! $cache_file_truncated_message", 'general_errors');
                        stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, "Failed to update the retry cache! $cache_file_truncated_message", 'caching_errors');
                    }

                    # GWMON-12363 : make service 'retry_caching' go critical, status message  "Endpoint GW_Server can't be reached. Last message send: YYYY-MM-DD:HH:MM:SS, cached ### data rows"
                    else {
                        # This will update a service that contains data about the cache size
                        stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, "Feeder host $GW::Feeder::feeder_host: $retry_cache_size{summary}", 'retry_cache_size');
                        stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $retry_cache_size{Mb}, 'retry_cache_size_mb', "do not log");

                        # This will update a service that contains error information about the caching occurring with a count of how many rows written
                        #stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, "$error_message at " . localtime() . " - $count_of_data_rows_cached rows written. " , 'caching_errors');
                        stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, "$count_of_data_rows_cached rows written. $error_message at " . localtime(), 'caching_errors');

                        # If the cache grew so large that it needed to be truncated, add that message to the metrics too. Also, if something went wrong in the truncation procedure, that info should
                        # have ended up in $cache_file_truncated_message. Ie just pass along anything that is set in $cache_file_truncated_message. 
                        if ( defined $cache_file_truncated_message ) {
                            stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, "$cache_file_truncated_message.", 'caching_errors' ) ;
                            stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, "$cache_file_truncated_message.", 'retry_cache_size', "do not log" ) ;
                        }
                    }

                    next ENDPOINT;
                }

                $feeder_objects{$endpoint_name} = $feeder;
            }

            # Do some final value sanity checking of feeder-specific configuration
            my $validation_error;
            if ( not validate_feeder_specific_options( \$validation_error ) ) {
                #$logger->error("A problem was found in endpoint '$endpoint_name' configuration that needs fixing - updating its retry cache and ending processing attempt.");
                $error_message = "Feeder host $GW::Feeder::feeder_host : A problem was found in endpoint '$endpoint_name' configuration that needs fixing - updating its retry cache and ending processing attempt. $validation_error";
                stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $error_message, 'general_errors' );
                if ( not endpoint_retry_cache_write( $endpoint_name, $retry_cache_filename, \@endpoint_data, $logger, "a", \$count_of_data_rows_cached, \%retry_cache_size, \$cache_file_truncated_message ) ) {
                    #$logger->error("Failed to update the retry cache!");
                    #stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, "Failed to update the retry cache!", 'general_errors' );
                    stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, "Failed to update the retry cache!", 'caching_errors' );
                }
                # GWMON-12363 : make service 'retry_caching' go critical, status message  "Endpoint GW_Server can't be reached. Last message send: YYYY-MM-DD:HH:MM:SS, cached ### data rows"
                else {
                    # This will update a service that contains error information about the caching occurring with a count of how many rows written
                    stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, "$error_message at " . localtime() . " - $count_of_data_rows_cached rows written. " , 'caching_errors');
                    # This will update a service that contains data about the cache size
                    stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, "Feeder host $GW::Feeder::feeder_host: $retry_cache_size{summary}", 'retry_cache_size');
                    if ( defined $cache_file_truncated_message ) {
                        stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, "$cache_file_truncated_message.", 'caching_errors' ) ;
                        stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, "$cache_file_truncated_message.", 'retry_cache_size', "do not log" ) ;
                    }
                }
                next ENDPOINT;
            }

            # If there were general problems, still want to propogate that info up into metrics services, but nothing else
            if ( defined $init_db_connection_error or defined $scom_events_count_error or defined $get_data_error) {

                if ( defined $init_db_connection_error ) {
                    $error_message = "Feeder host $GW::Feeder::feeder_host : An error occurred initializing the database connection. $init_db_connection_error";
                }
                elsif ( defined $scom_events_count_error ) {
                    $error_message = "Feeder host $GW::Feeder::feeder_host : An error occurred getting a count of scom events. $scom_events_count_error";
                }
                elsif ( defined $get_data_error ) {
                    $error_message = "Feeder host $GW::Feeder::feeder_host : An error occurred getting data. $get_data_error";
                }

                stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $error_message, 'general_errors' ) ;
                # Need to call update_feeder_stats to populate the services array for the endpoint, else send_metrics will figure there's nothing to do.
                #update_feeder_stats( $cycle_count, scalar localtime, 0, 0, 0, 0, 0, 0, [ ], \@{ $metrics{endpoints}{$endpoint_name}{services} } );
                update_feeder_stats( $cycle_count, sprintf ( "%0.2f", Time::HiRes::time() - $endpoint_start_time ) ,
                                        0,# count of all possible events that were retrieved for processing
                                        0,# count of successfully processed events
                                        0,# count of events unprocessed due to aging out
                                        0,# count of events unprocessed due to being resolved
                                        0,# count of events unprocessed due to being in maintenance mode
                                        0,# count of events filtered out
                                        0,# count of events unprocessed due to other issues
                                        0,# count of events that failed to purge
                                        \@{ $metrics{endpoints}{$endpoint_name}{services} }
                                ) ;
                $metrics{endpoints}{$endpoint_name}{feeder_object} = $feeder;
                next ENDPOINT;
            }



            # Cleanup is per-endpoint
            if ( $clean ) {
                $feeder->cleanup( $yes , 1 ) ;
                next ENDPOINT;
            }
    
            # Import ALL data in this endpoint's retry cache by prepending this endpoint's data structure with it.
            # Procesing will then start with these things that failed before and were cached.
            # If fail to do this, don't proceed with this endpoint because the story will become jumbled possibly.

            $aged_out_count = 0;
            $cache_file_truncated_message = undef;
            if ( not endpoint_retry_cache_import( $endpoint_name, $retry_cache_filename, \@endpoint_data, $logger, $feeder->{properties}->{retry_cache_max_age}, \$aged_out_count, \$cache_imported_rows_count ) ) {
                $error_message = "Feeder host $GW::Feeder::feeder_host : Failed to import retry cache '$retry_cache_filename' - ending processing attempt for this endpoint.";
                stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $error_message, 'general_errors' ) ;
                next ENDPOINT;
            }

            # Communicate some info that processing retry cache can take some time - else it can look like the feeder is frozen 
            if ( defined $cache_imported_rows_count and $cache_imported_rows_count > 0 ) {
                $logger->info( "$cache_imported_rows_count rows from retry cache were imported and may take some time to process" ) ;
            }
            
            # If any data was aged out, raise that up to the metrics services level
            if ( $aged_out_count > 0 ) {
                stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, "Retry cache data was aged out : $aged_out_count retry cache rows were too old to be processed.", 'caching_errors' ) ;
            }

            # Don't perform processing of this endpoint if processing is disabled in it's config.
            # No retry caching for this endpoint in this case either.
            # Also, don't try to update health objects - if the feeder endpoint is disabled, don't clutter it up with info about it being ... disabled.
            if ( not $feeder->{enable_processing} ) {
                #$feeder->terminate_feeder("NOTICE The feeder is currently disabled. To enable it, set enable_processing to yes. Sleeping forever.", -1);
                #$feeder->report_feeder_error("Processing for endpoint '$endpoint_name' is currently disabled. To enable it, set enable_processing = yes in $endpoint_config.");
                $logger->info("Processing for endpoint '$endpoint_name' is currently disabled. To enable it, set enable_processing = yes in $endpoint_config.");
                next ENDPOINT;
            }


            # Initialize a feeder hostgroup, virtual feeder host, feeder health service, and any other feeder specific services as defined in %feeder_services
            # NOTE: Initializing health objects will trigger a sequence of events that will safely re-auth the auth token via RAPID if it has expired.
            if ( not $feeder->initialize_health_objects( $started_message, \%feeder_services ) ) {
                # Don't continue for now since failing to do this simple step indicates a bigger issue probably. 
                # For example, if there was a license error (eg not installed) or a general REST breakdown.
                $error_message = "Feeder host $GW::Feeder::feeder_host : An error occurred initializing feeder health objects - ending processing attempt for this endpoint.";
                stage_error_for_publishing_via_metrics($logger, \%metrics, $endpoint_name, $error_message, 'general_errors');
                if ( not endpoint_retry_cache_write( $endpoint_name, $retry_cache_filename, \@endpoint_data, $logger, "a", \$count_of_data_rows_cached, \%retry_cache_size, \$cache_file_truncated_message ) ) {
                    stage_error_for_publishing_via_metrics($logger, \%metrics, $endpoint_name, "Failed to update the retry cache!", 'caching_errors');
                }
                else {
                    # This will update a service that contains error information about the caching occurring with a count of how many rows written
                    stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, "$error_message at " . localtime() . " - $count_of_data_rows_cached rows written. " , 'caching_errors');
                    # This will update a service that contains data about the cache size
                    stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, "Feeder host $GW::Feeder::feeder_host: $retry_cache_size{summary}", 'retry_cache_size');
                    if ( defined $cache_file_truncated_message ) {
                        stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, "$cache_file_truncated_message.", 'caching_errors' ) ;
                        stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, "$cache_file_truncated_message.", 'retry_cache_size', "do not log" ) ;
                    }
                }
                next ENDPOINT;
            }


            # Process each time-stamped set of raw data for this endpoint (just 1 set if empty retry cache, >1 otherwise)
            $sync_status = 1; # start out optimistically ;)
            ENDPOINTDATASET: while ( $sync_status == 1 and $timestamped_dataset = shift @endpoint_data ) {

                $fmee_timestamp = $timestamped_dataset->{querytime}; # for fmee testing logic

                # reset metrics for this endpoint
                #$total_took_time = $total_elapsed_time = $successfully_execd_es_searches_count = $unsuccessfully_execd_es_searches_count = 0;
                
                # Prepare the data for processing by building it for this timestamped dataset
                if ( not build_data( $endpoint_name, $timestamped_dataset, \$build_error ) ) { 
                    #$feeder->report_feeder_error( "ERROR Data build error was encountered - processing of this endpoint will stop here." ); 
                    $error_message = "Feeder host $GW::Feeder::feeder_host : A data build error was encountered - endpoint will not be processed. $build_error";
                    stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $error_message,  'general_errors');
                    $sync_status = 0;
                    last ENDPOINTDATASET;
                }

                # build_data can return a set build error, but still return ok ...
                if ( defined $build_error and $build_error ne '' ) {
                    $error_message = "Feeder host $GW::Feeder::feeder_host : A data build error was encountered - endpoint will not be processed. $build_error";
                    stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $error_message,  'general_errors');
                    # Carry on tho and allow sync status to be ok 
                }

                # At this point, a time-stamped entry of the @endpoint_data array has been updated with a rows_built_data key and data.
                # Now synchronize the endpoint using this time-stamped built data set.
                # The sync process will remove items from the dataset that were successfully processed, leaving things that were not and that need putting back into the retry cache. 
                # Question : Does retry-caching at this level of granularity will work generally, or need to fail as a complete timestamped data set?
                # Answer : it doesn't really work at that level of granularity - will cache all data on sync failure.

                # Sync the endpoint using the build data. In -show mode, sync_endpoint will do the right thing.
                $total_events = $total_events_processed = $aged_out_events_count = $resolved_events_count = $maintenance_events_count = $events_other_count = $purge_error_count = $events_filtered_count = undef;
                $sync_error = undef;
                $sync_status = sync_endpoint(   $cycle_count, 
                                                $endpoint_name, 
                                                $timestamped_dataset, 
                                              # \$successfully_processed_built_rows, # not currently used
                                                \$total_events,             # metric : count of all possible events that were retrieved for processing
                                                \$total_events_processed,   # metric : count of successfully processed events
                                                \$aged_out_events_count,    # metric : count of events unprocessed due to aging out
                                                \$resolved_events_count,    # metric : count of events unprocessed due to being resolved
                                                \$maintenance_events_count, # metric : count of events unprocessed due to being in maintenance mode
                                                \$events_filtered_count,    # metric : count of events filtered out
                                                \$events_other_count,       # metric : count of events unprocessed due to other issues
                                                \$purge_error_count,        # will use this to avoid super tight CYCLE loop condition
                                                \$sync_error,               # error that might get returned
                                            ) ;

                # Converts audit into events in foundation, and empties the $feeder->{audit_trail} data structure
                # Flush the audit trail even if there was a problem with sync'ing since it only gets populated if something was actually done.
                $feeder->flush_audit();  # this will produce its own errors if necessary

                # If there was a sync error, then put all of the timestamped data back into the retry cache.
                # Actually if there was NO error, remove this dataset from the endpoint data set, and,
                # if there is an error with the sync, just stop trying to process any more endpoint data
                # Then put back into the cache whatever is in the endpoint data.
                # This approach regards the entire set of endpoint data (ie all timestamped sets) as one long
                # set of time-ordered things to process. If there's a problem midway through processing this long list,
                # then stop, flag an error, and put stuff back into the cache to retry later.

                if ( $sync_status != 1 ) {  
                    #$feeder->report_feeder_error( "ERROR in syncing data set for endpoint '$endpoint_name'- no more processing will be done for this endpoint data set." ); 
                    $error_message = "Feeder host $GW::Feeder::feeder_host : An error in syncing data set for endpoint '$endpoint_name' occurred. $sync_error" ;
                    stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $error_message,  'general_errors');

                    # If sync of this endpoint data set failed, eg license exhausted, don't go into a tight failure loop
                    sleep 15;

                    # Let this then drop out in to the remaining code here 
                }
                elsif ( defined $sync_error ) {  
                    # sync endpoint "ok", but sync error can still be set in certain conditions, eg one event out of many being of an unsupported version
                    $error_message = "Feeder host $GW::Feeder::feeder_host : An issue in syncing data set for endpoint '$endpoint_name' occurred : $sync_error" ;
                    stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $error_message,  'general_errors');
                }

                # Remove the rows_built_data : a) if don't, it will end up in a retry cache which should only be { timestamp=>time, rows=> [ {}, ... ]  }, and, b) good practice.
                # Only do this if $timestamped_data is still non empty.
                delete $timestamped_dataset->{rows_built_data} if exists $timestamped_dataset->{rows_built_data}; 

                # totalling up for across all endpoint datasets ...
                $sum_of_total_events             += $total_events;
                $sum_of_total_events_processed   += $total_events_processed;
                $sum_of_aged_out_events_count    += $aged_out_events_count;
                $sum_of_resolved_events_count    += $resolved_events_count;
                $sum_of_maintenance_events_count += $maintenance_events_count;
                $sum_of_events_other_count       += $events_other_count;
                $sum_of_events_filtered_count    += $events_filtered_count;
                $sum_of_purge_error_count        += $purge_error_count;

            } # end ENDPOINTDATASET loop

            # If sync of a timestamped dataset failed, the above while loop will have exited and now 
            # need to put the timestamped dataset that failed to process back into the endpoint data 
            # prior to writing it out to the retry cache
            if ( $sync_status != 1 ) {
                @endpoint_data = ( $timestamped_dataset, @endpoint_data ) ;
            }

            # If got this far, then assume things were processed ok, but lastly check that the purging worked ok too.
            if ( $sum_of_purge_error_count > 0 )  {
                $data_failed_to_processed_ok = 1;
            }
    
            # Write the @endpoint_data data back to the retry cache - even if it's empty
            if ( not endpoint_retry_cache_write( $endpoint_name, $retry_cache_filename, \@endpoint_data, $logger, "w", \$count_of_data_rows_cached, \%retry_cache_size, \$cache_file_truncated_message ) ) { 
                $error_message = "An error occurred in writing retry cache for endpoint '$endpoint_name'. $cache_file_truncated_message";
                stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $error_message, 'caching_errors');
            }
            else {
                # This will update a service that contains data about the cache size
                stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, "Feeder host $GW::Feeder::feeder_host: $retry_cache_size{summary}", 'retry_cache_size', "do not log");
                # If some error occurred sizing the cache file, raise that into the metrics too here
                if ( defined $retry_cache_size{error} ) { 
                    stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $retry_cache_size{error}, 'retry_cache_size_error');
                }
                if ( defined $cache_file_truncated_message ) {
                    stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, "$cache_file_truncated_message.", 'caching_errors' ) ;
                    stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, "$cache_file_truncated_message.", 'retry_cache_size', "do not log" ) ;
                }
            }

            # This is where removal of metrics services would need to happen to cover the case of a renamed endpoint in the master config
            my $remove_error = undef;
            remove_feeder_objects_from_foundation( \$remove_error ) ; # this routine can set remove_error regardless of whether it is successful or not
            if ( defined $remove_error ) { 
                $error_message .= "Feeder host $GW::Feeder::feeder_host : Feeder had a problem removing objects from foundation. ";
                $error_message .= $remove_error if defined $remove_error;
                stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $error_message,  'general_errors');
            }

            # Update feeder stats services 
            # - per feeder endpoint because rules in each endpoint config could be different.
            # - also currently the metrics are across all datasets processed for this endpoint, so if there are cached ones, this will impact the metrics.
            #   (Maybe change that later so that it's per endpoint dataset. Low priority.)
            # - Only do this if not in show mode.
            if ( not $show ) {
                update_feeder_stats(    $cycle_count, 
                                        sprintf ( "%0.2f", Time::HiRes::time() - $endpoint_start_time ) ,
                                        $sum_of_total_events,             # count of all possible events that were retrieved for processing
                                        $sum_of_total_events_processed,   # count of successfully processed events
                                        $sum_of_aged_out_events_count,    # count of events unprocessed due to aging out
                                        $sum_of_resolved_events_count,    # count of events unprocessed due to being resolved
                                        $sum_of_maintenance_events_count, # count of events unprocessed due to being in maintenance mode
                                        $sum_of_events_filtered_count,    # count of events filtered out
                                        $sum_of_events_other_count,       # count of events unprocessed due to other issues
                                        $sum_of_purge_error_count,        # count of events that failed to purge
                                        \@{ $metrics{endpoints}{$endpoint_name}{services} }
                                ) ;
            }

           # If there's a Feeder api object successfully created then it meant that the REST API endpoint was up.
           # Put a reference to this Feeder object into %metrics for use later
           $metrics{endpoints}{$endpoint_name}{feeder_object} = $feeder;

           if ( $show ) { 
              $logger->info("Show mode : look in ${endpoint_name}_${show} for results"); 
           }
    
        } # end ENDPOINT loop


        # close it for now - reopen it on next cycle
        if ( not close_database_connection( \$error ) ) {
            $error_message = "Feeder host $GW::Feeder::feeder_host : An error occurred closing the database connection. $error" ;
            stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $error_message, 'general_errors');
        }

        if ( $clean ) {
            $logger->info( "Cleaning option supplied - feeder finished" );
            exit; # exit will call END which will call terminate_rest_api which will end the GW API sessions
        };

        # log the total cycle elapsed time
        $logger->info("Cycle $cycle_count elapsed processing time : " . sprintf ( "%0.2f", Time::HiRes::time() - $cycle_start_time ) .  " seconds");

        # Send metrics out to all endpoints - this also send perf data to the metrics services if defined.
        if ( not send_metrics( \%metrics, \$error, $logger ) ) {
            $logger->error("A problem was encountered processing metrics");
            $logger->error($error) if defined $error;
        }

        if ( $once ) {
            $logger->info( "Run-once option supplied - feeder finished" ); 
            exit; # exit will call END which will call terminate_rest_api which will end the GW API sessions
        };

        if ( $show ) {
            $logger->info( "Show mode option supplied - feeder finished" );
            exit; # exit will call END which will call terminate_rest_api which will end the GW API sessions
        };

        # Increment the cycle number
        $cycle_count++;

    } # end CYCLE loop

}

# ----------------------------------------------------------------------------------------------------------------
sub sync_endpoint
{
    # Gets all scom events from scom events table, in order of the TimeOfLastEvent.
    # Each event is then : built, processed, purged.

    my ( $cycle_iteration,              # cycle iteration number
         $endpoint_name,                # name of endpoint
         $ref_timestamped_dataset,      # ref to timestamped built set of scom events
        #$ref_successfully_processed_built_rows, # success/failure flag - not currently used here but could be later
         $ref_total_events,             # count of all possible events that were retrieved for processing
         $ref_total_events_processed,   # count of successfully processed events
         $ref_aged_out_events_count,    # count of events unprocessed due to aging out
         $ref_resolved_events_count,    # count of events unprocessed due to being resolved
         $ref_maintenance_events_count, # count of events unprocessed due to being in maintenance mode
         $ref_events_filtered_count,    # count of events filtered out
         $ref_events_other_count,       # count of events unprocessed due to other issues
         $ref_purge_event_errors,       # count of events that failed to purge
         $error_ref,                    # error by ref
       ) = @_;

    my ( $built_scom_event, @built_scom_events, $alertid, $scom_table_id );
    my ( @constrained, $status_code, $event_array_index, $process_built_event ) ;
    my ( $process_built_event_error, $purge_error ) ;

    # Initialize the metrics
    ${$ref_total_events_processed} = ${$ref_aged_out_events_count} = ${$ref_resolved_events_count} = ${$ref_maintenance_events_count} = 0;
    ${$ref_events_other_count} = ${$ref_purge_event_errors} = ${$ref_events_filtered_count} = 0;

    # get the build scom events
    @built_scom_events = @{$ref_timestamped_dataset->{rows_built_data}};

    # Total # of events that could be processed - for metrics
    ${$ref_total_events} = scalar @built_scom_events;
    
    my $formatted_query_time = strftime( '%Y-%m-%dT%H:%M:%S%z', localtime( $ref_timestamped_dataset->{querytime} ) );
    $logger->debug("Syncing endpoint '$endpoint_name', query time $formatted_query_time ($ref_timestamped_dataset->{querytime})");

    # Process the built events
     
    # Index into raw events array of query data structure (aka rows)
    # Note there is expected to be an equal number of entries in the @rows and @rows_built_data in the data structure
    $event_array_index = -1; 
	EVENT : foreach $built_scom_event ( @built_scom_events ) {

        $event_array_index++; # increment it here to avoid missing a 'next' not incrementing it first 
    
        $alertid       = $built_scom_event->{AlertId}; # the uuid of the event- seems to be possibly uniq across all SCOM events
        $scom_table_id = $built_scom_event->{RowId};  # the scom_save table row index

	    $logger->debug("Processing scom table id $scom_table_id, SCOM event id $alertid") if not $show;

        # Skip the event if it failed to build during build_data phase
		if ( exists $built_scom_event->{failed_to_build} ) {
			$logger->warn("Processing of failed-to-build event $alertid (scom table id $scom_table_id)  will be skipped"); 
            $built_scom_event->{$purge_marker} = 'other:failed to build'; 
            ${$ref_events_other_count}++;
		}
	
        # Skip event if it's too old.
        # Note the "GMT" in the str2time call.... from http://search.cpan.org/~rse/lcwa-1.0.0/lib/lwp/lib/HTTP/Date.pm :
        # "The str2time() function takes an optional second argument that specifies the default time zone 
        #  to use when converting the date. This zone specification should be numerical (like "-0800" or "+0100") or "GMT". 
        #  This parameter is ignored if the zone is specified in the date string itself. If this parameter is missing, 
        #  and the date string format does not contain any zone specification then the local time zone is assumed."
        # ie without the "GMT" zone arg, its converting the GMT timeoflastevent back into local tz, and thats wrong - it needs to stay
        # in it's original GMT tz and then the calc using the epoch time (UTC) will work.
        elsif ( time() - HTTP::Date::str2time(  $ref_timestamped_dataset->{rows}[ $event_array_index ]->{timeoflastevent}, "GMT") >= $master_config->{event_max_age} ) { 
            my $diff = time() - HTTP::Date::str2time(  $ref_timestamped_dataset->{rows}[ $event_array_index ]->{timeoflastevent}, "GMT");
			$logger->warn("Event $alertid (scom table id $scom_table_id) was too old to be processed - aging it out (diff = $diff, max age = $master_config->{event_max_age})"); 
            $built_scom_event->{$purge_marker} = 'aged_out'; 
            ${$ref_aged_out_events_count}++;
        }
            
        # Skip event if closed apparently
		elsif ( $ref_timestamped_dataset->{rows}[ $event_array_index ]->{resolutionstate} == 255 and not $feeder->{process_resolved_events} ) {  
			$logger->warn("Processing of closed event $alertid (scom table id $scom_table_id)  will be skipped"); 
            $built_scom_event->{$purge_marker} = 'resolved'; 
            ${$ref_resolved_events_count}++;
		}

        # skip event if the presumably monitored object is in maint. mode
     	elsif ( $ref_timestamped_dataset->{rows}[ $event_array_index ]->{monitoringobjectinmaintenancemode} eq "True" and not $feeder->{process_in_maintenance} ) {
            $logger->warn("Processing of maintenance mode event $alertid (scom table id $scom_table_id) will be skipped");  
			$built_scom_event->{$purge_marker} = 'maintenance';
            ${$ref_maintenance_events_count}++;
        }
             
        # check event is from a version of SCOM that is supported. Note that if connectorversion is missing, a default will be created in the build event routine
        elsif ( not exists $supported_scom_versions{ $ref_timestamped_dataset->{rows}[ $event_array_index ]->{connectorversion} } ) { 
            #$logger->error("Unsupported SCOM connector version '$ref_timestamped_dataset->{rows}[ $event_array_index ]->{connectorversion}', $alertid (scom table id $scom_table_id) ");
            ${$error_ref} .= "Unsupported or missing SCOM connector version for alert id $alertid (scom table id $scom_table_id). ";
			$built_scom_event->{$purge_marker} = 'other:unsupported SCOM connector version "' . $ref_timestamped_dataset->{rows}[ $event_array_index ]->{connectorversion} . '"' ; 
            ${$ref_events_other_count}++;
        }

        # sync the scom event into foundation if its of a recognized and handled version
        # TBD generalize this some TBD requires input from field as to what versions even exist that need supporting
        elsif ( $ref_timestamped_dataset->{rows}[ $event_array_index ]->{connectorversion} eq 'SCOM_2012_v0' ) {

            $process_built_event = 1 ; # assume will be building this event

            # If constraining hostsgroups are defined in the conf, then only process this event if it's in one of the Foundation hostgroups specified
            if ( defined $feeder->{constrain_to_hostgroups} and scalar keys %{$feeder->{constrain_to_hostgroups}}  > 0 ) {

                @constrained = ( $built_scom_event ); # the array should contain just this one event in the case of the SCOM feeder
                if ( not constrain_to_hostgroups( \@constrained )  ) {
                   # an error occurred at the API level in getting hostgroup info - so don't try to process the event 
                   #$logger->error("Failed to perform constraint checks for built scom event $alertid (scom table id $scom_table_id)"); 
                   ${$error_ref} = "Failed to perform constraint checks for built scom event $alertid (scom table id $scom_table_id). " ;
                   @constrained = ();
                }
                # If event hostgroup matched one hostgroup in the constrained hostgroup set, then process it, otherwise the event was not in a contraint hostgroup
                if ( not scalar @constrained ) { 
                    my $constrained_hostgroups = join ",", keys %{$feeder->{properties}->{constrain_to_hostgroups}} ; # make a list of the hostgroups
                    $built_scom_event->{$purge_marker} = "filtered:unconstrained - event not in hostgroup(s) ($constrained_hostgroups)";
                    $logger->debug( "Event $alertid with hostname '$built_scom_event->{Host}' is not in any constrained hostgroups ($constrained_hostgroups)"); 
                    $process_built_event = 0 ; # make a note to not process it later in this routine
                    ${$ref_events_filtered_count}++; # required for metrics - note it under 'filtered' 
                }
            }

            # If host/service constraint filters are defined, and the host or service does not match any of them, then don't process it
            if ( filtered_by_constraint_regex( $built_scom_event ) ) { 
                $logger->debug("Filtered by constraint regex scom event $alertid (scom table id $scom_table_id)"); 
                $process_built_event = 0 ; # make a note to not process it later in this routine
                $built_scom_event->{$purge_marker} = 'filtered:filtered by constraint regex'; # required for purging
                ${$ref_events_filtered_count}++; # required for metrics
            }

            # If host or service matches an exclusion filter regex, don't process it
            if ( filtered_by_exclusion_regex( $built_scom_event ) ) { 
                $logger->debug("Filtered by exclusion regex scom event $alertid (scom table id $scom_table_id)"); 
                $process_built_event = 0 ; # make a note to not process it later in this routine
                $built_scom_event->{$purge_marker} = 'filtered:filtered by exclusion regex'; # required for purging
                ${$ref_events_filtered_count}++; # required for metrics
            }
                
            # process the built event (if constraining was on and there was no match,or, the event was filtered by regex, the built event will be empty)
            if ( $process_built_event == 1 ) {

                if ( $show ) { 
                    print_event( $endpoint_name, $built_scom_event ) ;
                    next EVENT;     
                }

                # Finally - sync the built event !
                # As of v 3.0.0, a 0 return value on this is taken to mean a REST API error

                if ( not process_built_event( $built_scom_event, \$status_code, $endpoint_name, \$process_built_event_error ) ) { 
                    #$logger->error("Failed to process built scom event $alertid (scom table id $scom_table_id)"); 
                    ${$error_ref} .= "Failed to process built scom event $alertid (scom table id $scom_table_id). $process_built_event_error. "; 
                    # See if there was a feeder connection error, return to caller, otherwise store the event in save table.
                    # Don't want to move it if gwservices is simply down.
                    if ( $status_code eq '500' ) { 
                        #$logger->error("REST endpoint connection error detected (status 500) detected - ending processing of events for now");
                        ${$error_ref} .= "REST endpoint connection error detected (status 500) detected - ending processing of events for now. ";
                        return 0;
                    }
                    $built_scom_event->{$purge_marker} = 'other:failed to process'; 
                    ${$ref_events_other_count}++;
                    return 0; # New in v3.0.0 because this effects how processing of retry caching works
                }
                else {
                    $built_scom_event->{$purge_marker} = 'processed'; # Mark event as processed - can purge ok (either delete or save)
                    ${$ref_total_events_processed}++;
                }
            }
            else { 
                # built event was marked as not to be processed so nothing to do
            }
        }
        else {
            # flag an unsupported version of event - shouldn't happen
            #$logger->error("Invalid scom event version for event $alertid (scom table id $scom_table_id) - skipping processing of it");
            ${$error_ref} .= "Invalid scom event version for event $alertid (scom table id $scom_table_id) - skipping processing of it. ";
			$built_scom_event->{$purge_marker} = 'other:invalid scom version';
            ${$ref_events_other_count}++;
        }

        # Got this far, so purge the processed event
        if ( not $show ) {
            if ( not purge_event( $built_scom_event, \$purge_error ) ) { 
                #$logger->error("ERROR purging event"); 
                ${$error_ref} .= "An error purging the event occurred. $purge_error"; 
                # count up any purge errors so that up at the main CYCLE level sleep for a bit to avoid endless tight loop
                ${$ref_purge_event_errors}++;
                next EVENT;
            }
            else {
                ${$ref_purge_event_errors} = 0;
            }
                
        }

    } # End loop on processing built scom events

    # Alternatively, purge all events at once - do it all at once rather than open/closing db connection.
    # Can do this if the other per-event approach is a problem.
    # eg purge_all_events( \@built_scom_events ) if not $show ;

    # print processing stats to show file
    print_event( $endpoint_name, undef, 1 ) if $show;

    return 1;
}

# ----------------------------------------------------------------------------------------------------------------
sub process_built_event
{
    # Process a built scom event.
    # SCOM events differ to cacti events at least in that each event could have a different host state for the same host
    # so a per-event approach is required for SCOM events processing, vs the more efficient approach used for cacti
    # where the device state is fixed for a given processing cycle.
    # This routine does this with the built event :
    #    
    #    get host state from foundation for this event
    #    upsert host with new state
    #    upsert host into hostgroup(s)
    #    get service state from foundation for this event
    #    upsert service with new state and message
    #    if the host and/or service state has changed, post events and notifications
    # 
    # Returns 
    #   1 if ok, including if an empty event was passed in (as of v 3)
    #       Version 3 Note : changed empty event to return ok instead of failure. That way 0 means 
    #       something bad happened in the Feeder module and for now assume that is API related.
    #       This is important cos this later effects what happens to retry caches if one is
    #       being processed and a failure occurs. Don't want to lose retry cache data.
    #   0 otherwise, and if returning 0, will return status code from api call errors if appropriate, or undef if not set

    my ( $built_scom_event, $ref_status_code, $endpoint_name, $error_ref ) = @_;
    my ( %hosts_states, $status_code, $error_message );

    $status_code = '';

    # If show mode is enabled, just print out how event built event looks - useful for testing
    print_event( $endpoint_name, $built_scom_event ) if defined $show ;
                    
    # if the incoming scom event is empty - just flag that and return .
    if ( not scalar keys %{$built_scom_event} or not $built_scom_event ) { 
        $logger->warn( "Empty event - nothing to process"); 
        #return 0; # V 3.0.0
        return 1;
    }
        
    # Host - get Foundation state of host - adds FoundationHostState to event
    if ( not get_foundation_host_state( $built_scom_event , \$status_code ) ) { 
	    ${$ref_status_code} = $status_code;
        #$logger->error( "Error getting foundation host state" );
        ${$error_ref} .= "Error getting foundation host state during processing built event" ; 
        return 0;
    }


    # Upserts the host into Foundation, including the host's state
    if ( not upsert_foundation_host( $built_scom_event, \$status_code, \$error_message ) ) {
	    # TBD Feeder modules need to return http status from api call on error  - $status_code will just be '' for now
	    ${$ref_status_code} = $status_code;
        #$logger->error( "Error upserting host $built_scom_event->{Host}" );
        ${$error_ref} .= "Error upserting host $built_scom_event->{Host} during processing built event. $error_message";
        return 0;
    }

    # Add the host to a hostgroup        
    if ( not upsert_foundation_hostgroups_with_scom_host( $built_scom_event, \$status_code ) ) {  
	    # TBD Feeder modules need to return http status from api call on error  - $status_code will just be '' for now
	    ${$ref_status_code} = $status_code;
        #$logger->error( "Error upserting hostgroups" ) ;
        ${$error_ref} .= "Error upserting hostgroups during processing built event" ;
        return 0; 
    }

    # Get the service state as it is now in foundation - adds FoundationServiceState to the event
    if ( not get_foundation_service_state( $built_scom_event, \$status_code ) ) { 
	    ${$ref_status_code} = $status_code;
        #$logger->error( "Error getting foundation service state" ) ;
        ${$error_ref} .= "Error getting foundation service state during processing built event";
        return 0;
    }

    # Upsert the service into foundation
    if ( not upsert_foundation_service( $built_scom_event, \$status_code ) ) {
	    # TBD Feeder modules need to return http status from api call on error  - $status_code will just be '' for now
	    ${$ref_status_code} = $status_code;
        #$logger->error( "Error upserting service" ) ;
        ${$error_ref} .= "Error upserting service during processing built event" ;
        return 0;
    }

    # Post events and notifications
    # Re-using the existing post_events_and_notifications() routine but for just one event and one host ...
    # TBD skipping for now because for feeder we always send full updates anyway - ie this function is of limited use and not worth adding in here
    # If necessary, reduce the set of SCOM host and service events to only those which are having a state change.
    # Only do this filtering if a) always_send_full_updates = false, and b) we're on a full_update_frequency cycle, and c) its not the very first cycle
    #if ( not $feeder->{always_send_full_updates} and ( $cycle_iteration % $feeder->{full_update_frequency} != 0 ) and ( $cycle_iteration != 1 )  ) {
    #    filter_out_non_state_changed_scom_hosts_and_services( \@scom_events, \%hosts_states );
    #    # Again, record the new event set size as the size that was processed (even if there are errors, the were 'processed')
    #    ${$ref_total_events_processed} = $#scom_events + 1; 
    #}

    %hosts_states = ( 
        $built_scom_event->{Host} => { 
            'HostState' => $built_scom_event->{HostState}, 
            'FoundationHostState' => $built_scom_event->{FoundationHostState} 
        } 
    ) ;

    if ( not post_events_and_notifications( [ $built_scom_event ] , \%hosts_states, $built_scom_event, \$status_code ) ) {
	    # TBD Feeder modules need to return http status from api call on error  - $status_code will just be '' for now
	    ${$ref_status_code} = $status_code;
        #$logger->error( "Error posting events and notifications during processing built event");
        ${$error_ref} .= "Error posting events and notifications during processing built event";
        return 0; 
    }

    return 1;

}


# ----------------------------------------------------------------------------------------------------------------
sub post_events_and_notifications
{
    # Looks for state changes for hosts and services.
    # Posts notifications if state change detected and post_notifications is true.
    # Posts events if state change detected and options permit
    #
    # Takes as arguments :
    # - a ref to an array of event hashes, 
    # - a ref to a hash of host states (for a list of hosts)
    # - a ref to a single scom event (for time props etc), from which the other args were constructed
    #       (could construct them in here, but trying to use this function without changing it)
    #
    # returns 1 if ok, 0 otherwise
    # NOTES
    # - the ReportDate and FirstInsertDate from the built event are used for both host and service reportDate and firstInsertDate props
    #   until have example of host-down type's of events.

    my ( $ref_array_of_scom_events , $ref_hash_of_host_states, $scom_event_ref ) = @_;
    my ( $scom_event, $scom_host, @host_notifications, @service_notifications, $notificationType, $noma_status ) ;
    my ( @host_events, @service_events, $event_severity, $status );

    if ( not $feeder->{post_notifications} and not $feeder->{post_events} ) {
        $logger->debug("post_notifications and post_events are both disabled - no posting of events or notifications will be done");
        return 1;
    }

    # Search for HOST state changes.
    # Construct arrays for both host notification and event objects.
    # HostState can be one of these values : UNREACHABLE, UNSCHEDULED DOWN, UP
    foreach $scom_host ( keys %{$ref_hash_of_host_states} ) {

        ## hosts not yet in foundation means FoundationHostState not set, and means don't send events or notifications
        #if ( ( defined $ref_hash_of_host_states->{$scom_host}{FoundationHostState}) and ( $ref_hash_of_host_states->{$scom_host}{HostState} ne $ref_hash_of_host_states->{$scom_host}{FoundationHostState} )  ) {
        # V 2.0.6 - actually do send events and notifications if it's not in foundation yet.  Set FoundationHostState to '' ie not undef, so it gets processed in the next block
        $ref_hash_of_host_states->{$scom_host}{FoundationHostState} = '' if not defined $ref_hash_of_host_states->{$scom_host}{FoundationHostState} ;

        if ( ( defined $ref_hash_of_host_states->{$scom_host}{FoundationHostState}) and ( $ref_hash_of_host_states->{$scom_host}{HostState} ne $ref_hash_of_host_states->{$scom_host}{FoundationHostState} )  ) {

            # For events and notifications ....
            if ( $ref_hash_of_host_states->{$scom_host}{HostState} ne 'UP' ) { # ie UNREACHABLE, UNSCHEDULED DOWN, UP
                $notificationType = "PROBLEM";
                $event_severity = "SERIOUS";
            }
            else 
            {
                $notificationType = "RECOVERY";
                $event_severity = "OK";
            };

            # For notifications ...
            $noma_status = $ref_hash_of_host_states->{$scom_host}{HostState};
            $noma_status =~ s/UNSCHEDULED//g; # NoMa will quietly ignore UNSCHEDULED DOWN - only accepts UP, DOWN, UNREACHABLE
            $noma_status =~ s/\s+//g; # NoMa will quietly ignore ' UP' - pretty dumb about whitespace 
            push @host_notifications, {
	                                    'hostName'            => $scom_host,
	                                    'hostState'           => $noma_status,
	                                    'notificationType'    => $notificationType,
	                                    'hostOutput'          => "$scom_host is $ref_hash_of_host_states->{$scom_host}{HostState}", 
                                      };

            # For host events .... 
            push @host_events, {
                                   'host'              => $scom_host,
                                   'device'            => $scom_host,
                                   'monitorStatus'     => $ref_hash_of_host_states->{$scom_host}{HostState},
                                   'appType'           => $feeder->{app_type},
                                   'severity'          => $event_severity,
                                   'textMessage'       => "$scom_host is $ref_hash_of_host_states->{$scom_host}{HostState}",
                                   'reportDate'        => $scom_event_ref->{HostReportDate},
                                   'firstInsertDate'   => $scom_event_ref->{HostFirstInsertDate},
                               }
        }
    }

    # Search for SERVICE state changes and post events for them
    foreach $scom_event ( @{$ref_array_of_scom_events} ) { 
        # events for hosts not yet in foundation means FoundationHostState not set, and means don't send events or notifications
        # V 2.0.6 - actually do send events and notifications if it's not in foundation yet.  Set FoundationServiceState to '' ie not undef, so it gets processed in the next block
        $scom_event->{FoundationServiceState} = '' if not defined $scom_event->{FoundationServiceState} ;
        
        if ( ( defined $scom_event->{FoundationServiceState} ) and ( $scom_event->{ServiceState} ne $scom_event->{FoundationServiceState} ) ) {

            # For notifications and events ...
            if ( $scom_event->{ServiceState} ne 'OK' ) { # ie OK, UNSCHEDULED CRITICAL (or WARNING, UNKNOWN from test tweaking)
                $notificationType = "PROBLEM";
                $event_severity = "SERIOUS";
            }
            else 
            {
                $notificationType = "RECOVERY";
                $event_severity = "OK";
            };

            # For service notifications ...
            $noma_status = $scom_event->{ServiceState} ;
            $noma_status =~ s/UNSCHEDULED//g; # NoMa will quietly ignore UNSCHEDULED CRITICAL - only accepts OK, WARNING, CRITICAL and UNKNOWN 
            $noma_status =~ s/\s+//g; # NoMa will quietly ignore ' CRITICAL' - pretty dumb about whitespace 
            push @service_notifications,  {
                                            'hostName'            => $scom_event->{Host},
                                            'serviceDescription'  => $scom_event->{ServiceDescription},
                                            'serviceState'        => $noma_status,
                                            'notificationType'    => $notificationType,
                                            'serviceOutput'       => $scom_event->{LastPluginOutput},
                                          };

            # For service events ...
            push @service_events, {
                                   'host'              => $scom_event->{Host},
                                   'device'            => $scom_event->{Host},
                                   'service'           => $scom_event->{ServiceDescription},
                                   'monitorStatus'     => $scom_event->{ServiceState},
                                   'appType'           => $feeder->{app_type},
                                   'severity'          => $event_severity,
                                   'textMessage'       => $scom_event->{LastPluginOutput},
                                   'reportDate'        => $scom_event_ref->{ServiceReportDate},
                                   'firstInsertDate'   => $scom_event_ref->{ServiceFirstInsertDate},
                               };

            # Add a handy link from the event's service name to the SCOM system.
            # Currently this fails the api - need correct property name if one.
            # Also not even sure it makes sense to include the urls at all since they are from a different context altogether
            #if ( defined $scom_event_ref->{WebConsoleUrl} ) { 
            #    push @service_events, {
            #                        'properties'        => { 'WebConsoleURL' => $scom_event_ref->{WebConsoleUrl} }, 
            #    };
            #}

        }

    }

    $status = 1; # assume all operations will be ok and disprove ... rename this var :)

    # Send notifications ...
    if ( $feeder->{post_notifications} ) {
        # Send any host notifications
        if ( @host_notifications ) {
            $logger->debug( "Posting host notifications" );
            if ( not $feeder->feeder_post_notifications( 'host', \@host_notifications ) ) {
                $logger->error("Error creating host notifications.");
                $status = 0; # don't bail just yet - try and do as much as possible
            }
        }
        # Send any service notifications
        if ( @service_notifications ) {
            $logger->debug( "Posting service notifications" );
            if ( not $feeder->feeder_post_notifications( 'service', \@service_notifications ) ) {
                $logger->error("Error creating service notifications.");
                $status = 0; # don't bail just yet - try and do as much as possible
            }
        }
    }

    # Post events ...
    if ( $feeder->{post_events} and $feeder->{update_hosts_statuses} ) {
        # Post any host events.
        # Only post host events if update_hosts_statuses is set. Otherwise, the sv host status graphs will reflect up/down states, 
        # but the actual host status will not change when update_hosts_statuses = false
        if ( @host_events ) {
            $logger->debug( "Posting host events" );
            if ( not $feeder->feeder_post_events( 'host', \@host_events ) ) {
                $logger->error("Error posting host events.");
                $status = 0; # don't bail just yet - try and do as much as possible
            }
        }
    }
    # Post any service events
    if ( $feeder->{post_events} and @service_events ) {
        $logger->debug( "Posting service events" );
        if ( not $feeder->feeder_post_events( 'service', \@service_events ) ) {
            $logger->error("Error posting service events.");
            $status = 0; # don't bail just yet - try and do as much as possible
        }
    }
    
    return $status;

}

# ----------------------------------------------------------------------------------------------------------------
sub constrain_to_hostgroups # stock from cacti feeder hence the cacti vars
{
    # Takes a ref to an array of scom event hashes, 
    # figures out unique set of hosts,
    # figures out which Foundation hostgroups these hosts are in,
    # then removes those events which have hostgroups matching the constrain_to_hostgroup array members
    # returns 1 on success, 0 on failure 

    my ( $ref_array_of_events ) = @_;

    my ( $event, %hosts, $event_hostgroup, $cacti_event );

    # Figure out unique set of hostnames across all cacti events
    # assumes a cacti event always has a Host element
    foreach $cacti_event ( @{ $ref_array_of_events } ) {
        if ( not defined $hosts{ $cacti_event->{Host} } ) { 
            $hosts{ $cacti_event->{Host} } = undef;
        }
    }

    # If no hostgroups are defined for constraining, or an error occurred getting hostgroups, just return
    # If feeder_get_hostgroups fails to get hostgroups via the REST API, then it returns 0
    if ( not $feeder->feeder_get_hostgroups( \%hosts ) ) {
        $logger->trace("No constraining to hostgroups will be done.");
        return 0;
    }
    else {
        $logger->trace("Constraining to hostgroups.");
    }

    # Now have a %hosts hash that looks like this :
    # {
    #    host1 => { hg1 =>1, hg2 => 1, hg3 => 1 }, # in hg1, hg2 and hg3
    #    host2 => { hg1 =>1, hg4 => 1 }, 
    #    host3 => undef ; # in no hostgroups
    #    ...
    # }

    # If the event Host is NOT a member of a hostgroup being constrained to, delete it.
    # TBD might be better to label it as constrained, and then later can decide whether to delete it or not ?
    my $index=0; my $constrained_events = 0; my @constrained_events = ();
    foreach $event ( @{ $ref_array_of_events } ) {
        # cycle through the list of hostgroups that this event's Host belongs to
        foreach $event_hostgroup ( keys %{ $hosts{ $event->{Host} } } ) {
            # If the event hostgroup matches one its constrained to, add it to a result list
            # ( Why not splice the cacti events array ? Splicing out elements of the array whilst its being referenced in this event loop - bad :] )
            if ( defined $feeder->{constrain_to_hostgroups}{$event_hostgroup} ) { 
                $constrained_events++;
                $logger->debug("Hostgroup-constrained event : hostgroup '$event_hostgroup', host '$event->{Host}', service '$event->{ServiceDescription}'"); 
                push @constrained_events, $event;
                last; # only add this event once to the results
            }
        }
        $index ++;
    }

    #$logger->info( "Constraining to host groups : events constrained down to $constrained_events events out of a possible total $index events");

    @{$ref_array_of_events} = @constrained_events;

    return 1;

}

# ----------------------------------------------------------------------------------------------------------------
sub get_foundation_host_state
{
    # takes a built scom event
    # if the event's host exists in foundation, then it adds a FoundationHostState property to the built event
    # otherwise it doesn't.
    # returns 1 on ok, 0 failure

    my ( $built_scom_event, $status_code_ref ) = @_;
    my ( %outcome, %results ) ;

    $logger->debug("Getting host state for $built_scom_event->{Host}");
    #if ( not $feeder->{rest_api}->get_hosts( \@hosts_bundle, {}, \%outcome, \%results ) ) {
    if ( not $feeder->{rest_api}->get_hosts( [ $built_scom_event->{Host} ] , {}, \%outcome, \%results ) ) {
        # report an error but continue on rather than returning - ie try to do as much as possible
	    # If an error occurs because GW services went down, then \%outcome response code = 500 
	    # If an error occurs because the host being looked for doesn't exist, then \%outcome response code = 404 - this is ok
        if ( defined $outcome{response_code} and $outcome{response_code} ne '404' ) {
            #$logger->error("Error getting host states : " . Dumper \%outcome, \%results); 
            $logger->error("Error getting host states : API HTTP status code = $outcome{response_code}");
	        ${$status_code_ref} = $outcome{response_code};
            return 0;
        }

    }
    if ( defined $results{ $built_scom_event->{Host} }{monitorStatus} ) {
        $built_scom_event->{FoundationHostState} = $results{ $built_scom_event->{Host} }{monitorStatus} ;
    }
    # else don't add FoundationHostState

    return 1;

}

# ----------------------------------------------------------------------------------------------------------------
sub get_foundation_service_state
{
    # Takes a built event and and figures out Foundation state for it's service
    # Result is stored back into the event object 

    my ( $event, $status_code_ref ) = @_;
    my ( @hosts_bundle, %outcome, %results, %hosts_and_services );

    $logger->debug( "Getting and setting Foundation service states");

    push @hosts_bundle, $event->{Host};
    if ( not $feeder->{rest_api}->get_services( [], { hostname => \@hosts_bundle, format => 'host,service' }, \%outcome, \%results ) ) {
        if ( defined $outcome{response_code} and $outcome{response_code} ne '404' ) {
            $logger->error( "Error getting Foundation service states - no Foundation service states will be set for this bundle of hosts : @hosts_bundle" );
	    ${$status_code_ref} = $outcome{response_code};
            return 0;
        }
        else {
            # else just carry on - its ok to get a 404 in the case service not existing yet
        }
    }

    if ( defined $results{$event->{Host}}{$event->{ServiceDescription}}{monitorStatus} ) { 
        $event->{FoundationServiceState} =  $results{$event->{Host}}{$event->{ServiceDescription}}{monitorStatus} ;
    }

    return 1;

}


# ----------------------------------------------------------------------------------------------------------------
sub upsert_foundation_host
{
    # Takes one built scom event with HostState prop and 
    # upserts the host in Foundation with that state
    my ( $built_event, $status_code_ref, $error_ref ) = @_; # status_code_ref is for future dev
    my ( %host_options, @hosts, $error ) ;

    ${$error_ref} = undef;
    %host_options = ();

    # print "UFH : built event = " . Dumper $built_event;

    $logger->debug("Upserting hosts");

    # Build an array of options that the feeder rest api can consume
    # However, don't pass in description, properties, agentId, appType or anything else that 
    # will overwrite things should the host already exist. Instead, let feeder_upsert_hosts add those if necessary.
    push @hosts,  {
                      # This should be the smallest set of properties required for updating an existing host
                     'hostName'       => $built_event->{Host},
                     'monitorStatus'  => $built_event->{HostState},
                     'lastCheckTime'  => $built_event->{HostReportDate}, # this is needed to ensure events have correct time stamps => correct host state histograms in sv
                     #'properties'     => {  'LastStateChange' => $built_event->{HostReportDate} },  # For the Up since ... 
    };

    # feeder_upsert_hosts does bundling 
    if ( not $feeder->feeder_upsert_hosts( \@hosts, \%host_options, \$error ) ) { 
        #$logger->error("FOUNDATION HOSTS UPSERT ERROR could not upsert hosts" );
        ${$error_ref} .= "Foundation hosts upsert error - could not upsert hosts. ";
        ${$error_ref} .= $error if defined $error;
        return 0; 
    }

    return 1;

}

# ----------------------------------------------------------------------------------------------------------------
sub upsert_foundation_hostgroups_with_scom_host
{
    # takes a built scom event
    # assigns it's host to a hostgroup
    # which host group it goes in might possibly depend later on some logic based on the service name
    # returns 1 on success, 0 otherwise

    my ( $built_event ) = @_;

    my ( @hostgroups, @hosts, %hostgroup_options ) ;

    # Need to create this in @hostgroups for feeder_upsert_hostgroups()
    #   [
    #     {
    #       'hosts' => [
    #                    {
    #                      'hostName' => 'host1'
    #                    }
    #                  ],
    #       'name' => 'hg1'
    #     },
    #     {
    #       'hosts' => [
    #                    {
    #                      'hostName' => 'host1'
    #                    }
    #                  ],
    #       'name' => 'hg2'
    #     }
    #   ];

    push @hosts, { "hostName" => $built_event->{Host} };

    foreach my $hg ( keys %{$built_event->{HostGroup}} ) {
        push @hostgroups, {
                        # Just enough properties to update hostgroup membership
                        "name"        => $hg,
                        #"hosts"       => [ @hosts ] , 
                        "hosts"       => [ { "hostName" => $built_event->{Host} } ] , 
        } ;
    }

    if ( not $feeder->feeder_upsert_hostgroups( \@hostgroups, \%hostgroup_options ) ) {
        $logger->error("FOUNDATION HOSTGROUPS UPSERT ERROR could not upsert hostgroups" );
        return 0;
    }

    return 1;


}

# ----------------------------------------------------------------------------------------------------------------
sub upsert_foundation_service
{
    # Takes a built event and upserts its services in Foundation
    # Returns 1 on success, 0 otherwise 

    my ( $event ) = @_;
    my ( @services, %service_options );

    %service_options = ( );

    # Build the required api fields.
    # However, don't pass in anything that will overwrite things should the host:service already exist. 
    # Instead, let feeder_upsert_services add those if necessary.
    push @services, { 
                        # This is the minimum set of properties to achive an update of the service
                        'description'          => $event->{ServiceDescription},   # the name of the service
                        'hostName'             => $event->{Host},                 # the host name
                        'monitorStatus'        => $event->{ServiceState},    # the service status
                        'properties'           => { "LastPluginOutput" => $event->{LastPluginOutput} }, # the service status message
                        'lastCheckTime'        => $event->{ServiceReportDate},
                       #'lastStateChange'      => $event->{ServiceReportDate}, # for the <state> since ... sv message
    };

    if ( not $feeder->feeder_upsert_services( \@services, \%service_options ) ) {
        $logger->error("FOUNDATION SERVICES UPSERT ERROR could not upsert SCOM services in Foundation" );
        return 0;
    }

    return 1;
}

# ----------------------------------------------------------------------------------------------------------------
sub initialize_database_connection
{
    # Set up scom database handle
    # returns 1 on success, 0 otherwise

    my ( $error_ref ) = @_;
    my ( $dsn ) ;

    $database_handle = undef if defined $database_handle; # drop connection first

    $logger->debug("Initializing connection to database");
    if ( $master_config->{dbtype} eq 'postgresql' ) {
        $dsn = "DBI:Pg:dbname=$master_config->{dbname};host=$master_config->{dbhost};port=$master_config->{dbport}";
    }
    elsif ( $master_config->{dbtype} eq 'mysql' ) {
        $dsn = "DBI:mysql:database=$master_config->{dbname};host=$master_config->{dbhost};port=$master_config->{dbport}";
    }
    else {
        # unrecognized db type error
        #$logger->error("DATABASE ERROR Invalid database type - should be postgresql or mysql.");
        ${$error_ref} = "DATABASE ERROR Invalid database type - should be postgresql or mysql.";
        return 0;  
    }

    $database_handle = DBI->connect( $dsn, $master_config->{dbuser}, $master_config->{dbpass}, { 'AutoCommit' => 1 } ); 
    if ( ! $database_handle ) {
        #$logger->error ("DATABASE ERROR Cannot connect to database '$master_config->{dbname}'. Error: '$DBI::errstr'");
        ${$error_ref} = "DATABASE ERROR Cannot connect to database '$master_config->{dbname}'. Error: '$DBI::errstr'";
        return 0;
    }

    return 1;

}

# ----------------------------------------------------------------------------------------------------------------
sub db_connection_ok
{
    # Checks to see if a db connection is up using the ping() method.

    return 0 if not defined $database_handle;
    return $database_handle->ping();
}

# ----------------------------------------------------------------------------------------------------------------
sub update_feeder_stats
{
    # Logs feeder stats and updates services with metrics too
    my ( $cycle_number,
         $endpoint_processing_time,
         $total_events,             # count of all possible events that were retrieved for processing
         $total_events_processed,   # count of successfully processed events
         $aged_out_events_count,    # count of events unprocessed due to aging out
         $resolved_events_count,    # count of events unprocessed due to being resolved
         $maintenance_events_count, # count of events unprocessed due to being in maintenance mode
         $events_filtered_count,    # count of events filtered out
         $events_other_count,       # count of events unprocessed due to other issues
         $purge_error_count,        # count of events that failed to purge
         $built_metrics_ref,        # reference to an array of built metrics services that will be populated by this routine.
      ) = @_;


    my ( $endpoint_processing_time_msg, $events_processed_msg, $cycle_end_queue_size_msg, $purge_error_msg, $events_filtered_count_msg, 
         $cycle_end_queue_size_status, $events_processed_service_status, $purge_error_status, $events_processed_status, $events_filtered_count_status,
         $cycle_end_queue_size, @built_hosts, @built_services, $formatted_query_time, %hosts_states, $count_error 
    );

    $endpoint_processing_time_msg = "Endpoint processing time : $endpoint_processing_time seconds";

    $total_events              = 0 if not defined $total_events;
    $total_events_processed    = 0 if not defined $total_events_processed;
    $aged_out_events_count     = 0 if not defined $aged_out_events_count;
    $resolved_events_count     = 0 if not defined $resolved_events_count;
    $maintenance_events_count  = 0 if not defined $maintenance_events_count;
    $events_filtered_count     = 0 if not defined $events_filtered_count;
    $events_other_count        = 0 if not defined $events_other_count;
    $purge_error_count         = 0 if not defined $purge_error_count;

    $events_processed_service_status = 'OK';
    $events_processed_msg  = "$total_events_processed events processed out of a total possible $total_events events. ";
    $events_processed_msg .= "(Detail : $events_filtered_count filtered out; $aged_out_events_count aged out; $resolved_events_count resolved; $maintenance_events_count maintenance; $events_other_count other causes.) ";

    if ( $events_other_count > 0 ) {
        $events_processed_service_status = 'UNSCHEDULED CRITICAL'; # no safe 'other' reason - raise this to the operator clearly
    }

    if ( $purge_error_count > 0 ) { 
        $events_processed_msg .= "Failed to purge $purge_error_count events. ";
        $events_processed_service_status = 'UNSCHEDULED CRITICAL';
    }

    $events_processed_msg .= sprintf "Processed events / second : %0.4f", $total_events_processed / $endpoint_processing_time;   

    $cycle_end_queue_size = scom_events_count( \$count_error, 1 ); # TBD - what's the second unused arg here for ??
    if ( defined $count_error ) {
        $cycle_end_queue_size_status = 'UNKNOWN' ; 
        $cycle_end_queue_size_msg = "Count of records in $scom_events_table table : could not retrieve : $count_error";
    }
    else {
        $cycle_end_queue_size_status = 'OK' ; # TBD threshold this later - this needs graphing too
        $cycle_end_queue_size_msg = "Count of records in $scom_events_table table : $cycle_end_queue_size";
    }

    $logger->debug("Updating feeder statistics");

    # Log metrics
    $logger->info( "$endpoint_processing_time_msg") if defined $feeder->{cycle_timings};
    if ( $events_processed_service_status ne 'OK' ) {
        $logger->error( "$events_processed_msg");
    }
    else {
        $logger->info( "$events_processed_msg");
    }
    if ( $cycle_end_queue_size_status ne 'OK' ) { 
        $logger->error( "$cycle_end_queue_size_msg");
    }
    else {
        $logger->info( "$cycle_end_queue_size_msg");
    }

    @{ $built_metrics_ref } = (
        {   # Required.
            service => $feeder_name . ".health",
            message => "Feeder host $GW::Feeder::feeder_host: ok", # ok descriptive enough lol !?
            status  => "OK"
        },
        {
            service => "$feeder_name.endpoint.processing.time",
            message => "Feeder host $GW::Feeder::feeder_host: $endpoint_processing_time_msg",
            status  => "OK",
            # perfval  => { } # TBD later
        },
        {
            service => "$feeder_name.events.processed",
            message => "Feeder host $GW::Feeder::feeder_host: $events_processed_msg",
            status  => $events_processed_service_status
            # perfval  => { } # TBD later
        },
        {
            service => "$feeder_name.cycle.end.queue.size",
            message => "Feeder host $GW::Feeder::feeder_host: $cycle_end_queue_size_msg",
            status  => $cycle_end_queue_size_status
            # perfval  => { } # TBD later
        },
    );

    return 1;

}

# ----------------------------------------------------------------------------------------------------------------
sub initialize_options
{
    # Command line options processing and help.
    my $helpstring = "
Groundwork SCOM feeder - version $VERSION
GroundWork Feeder module version $GW::Feeder::VERSION
GroundWork RAPID module version $GW::RAPID::VERSION

Overview

    Description
        The SCOM feeder provides a bridge to Microsoft's System Center Operations Manager (SCOM).
        A GroundWork runbook installed in SCOM creates xml files for the SCOM event.
        A GroundWork Winows service - the SCOM reaper - feeds these xml event into a SCOM database
        on the GroundWork server.
        This SCOM feeder program processes events in the GroundWork SCOM database, by mapping
        GroundWork objects such as hosts and services and their states, to data with SCOM events.
        
        By default, the SCOM feeder is disabled. See configuration section for details.
        The SCOM feeder runs daemonized and is controlled via the GroundWork supervise subsystem.
        The log file is defined in /usr/local/groundwork/config/scom_feeder.log4perl.conf and
        is by default /usr/local/groundwork/foundation/container/logs/scom_feeder.log.

        The SCOM feeder is not a true feeder like the Cacti feeder, because it does not do a
        full synchronization of SCOM to GroundWork ie it only adds hosts, services, etc, or
        only updates states on the objects it creates, but doesn't remove GroundWork objects.

    Algorithm Description
        
        The feeder follows this algorithm :

            - Get all events from the GroundWork SCOM events database, ordered by TimeOfLastEvent
            - 'Build' each event - the building events refers to the mapping of SCOM event data 
               to GroundWork objects in the GroundWork data model
            - Process each built event where hosts and services and their states are sent via
              the GroundWork REST API to GroundWork, where events and notifications are generated if necessary
            - Purge the processed event from the GroundWork SCOM database, including saving it if configured

    General Configuration

        The master configuration file is done via $master_config_file.
        This configuration file defines configuration common to processing all endpoints,
        including the definition of endpoints themselves. To enable the feeder,
        set feeder_enabled = yes.

        Endpoint configuration files, e.g. /usr/local/groundwork/config/scom_feeder_localhost.conf
        specify configuration specific to each endpoint. Each endpoint configuration file includes an
        ws_client_config_file option which points to a Groundwork web services configuration file,
        inside which is the actual REST endpoint's details, held in the foundation_rest_url properties.
        The default web services properties file is /usr/local/groundwork/ws_client.properties.

        Configuration files are read every processing cycle i.e. it is not necessary to restart the
        feeder after changing a setting in any configuration file.  This includes enabling/disabling
        the feeder.

    Configuring for SSL

        Before configuring the SCOM feeder to use SSL, follow instructions from GroundWork on
        configuring all GroundWork servers to use SSL.

        If the GWME scom database resides on the same host as where the SCOM feeder is pointing,
        ie all local, you do not need to change the cacti feeder's configuration, i.e.,
        foundation_rest_url=http://localhost:8080/foundation-webapp/api.  
        If there is an second non local SSL endpoint - host endpoint2 say - which the feeder is 
        updating, then set foundation_rest_url=https://endpoint2/foundation-webapp/api .

    Show mode

        For a description of using the -show option, see the 'Configuring the SCOM Feeder' article
        in http://kb.groundworkopensource.com.

Options
        -clean           - removes foundation objects that this feeder (uuid) created across all endpoints, then exits
        -help            - show this help
        -limit N         - only process the first N scom events found in scom database scom_events table, then exits
        -once            - don't run deamonized, just run one cycle and exit
        -show <file>     - dump built objects to a tsv file - no processing will be done of events other than printing them
        -use_save_table  - use with -show - shows how saved events would be processed
        -version         - show version info
        -yes             - assumes yes to the remove question presented by the -clean option

For more detailed information on feeder operation and configuration, see http://kb.groundworkopensource.com

Author
    GroundWork 2015

";

    $SIG{__WARN__} = undef; # disable warnings to log4perl temporarily
    GetOptions(
                'clean'            => \$clean,
                'help'             => \$help,
                'limit=i'          => \$limit,
                'once'             => \$once,
                'show=s'           => \$show,
                'use_save_table'   => \$use_save_table,
                'version'          => \$show_version,
                'yes'              => \$yes,
              ) or die "$helpstring\n";

    if ( defined $help ) { print $helpstring; exit; }
    if ( defined $show_version ) { print "$0 version $VERSION, Feeder module version $GW::Feeder::VERSION\n"; exit; }

    # if -show is on and -used_saved_table given too, then switch the scom_events table to be scom_save 
    if ( $show and $use_save_table ) { 
        $scom_events_table = $scom_save_table;
    }

    $SIG{__WARN__} = sub { $logger->warn( "WARN  @_" );  }; # revert- warnings to log4perl
}

# ---------------------------------------------------------------------------------
sub read_test_tweaks_file # not currently used - left in for future re-use
{
    # Reads the test_tweaks_file config into a hash 

    if ( ( not -e $feeder->{test_tweaks_file} ) or ( not -r $feeder->{test_tweaks_file} ) ) {
        $logger->debug("System test tweaks config file '$feeder->{test_tweaks_file}' doesn't exist or isn't readable. No testing will be done.");
        return;
    }
         
    eval { $tests_config = TypedConfig->new ( $feeder->{test_tweaks_file} ); };
    if ($@) {
        chomp $@;
        $@ =~ s/^ERROR:\s+//i;
        $logger->error("Cannot read tests config file $feeder->{test_tweaks_file} ($@) - continuing without tests");
    };
}

# ---------------------------------------------------------------------------------
sub validate_feeder_specific_options
{
    # Logic for validation of feeder-specific options
    # TBD improve this

    my ( $error_ref ) = @_;

    if ( not defined $feeder->{default_hostgroups} or not scalar keys %{ $feeder->{default_hostgroups} } ) {
        #$feeder->error("No default hostgroups given - check the default_hostgroups hash property is present and non empty.");
        ${$error_ref} = "No default hostgroups given - check the default_hostgroups hash property is present and non empty.";
	    return 0;
    }
    return 1;
}

# ----------------------------------------------------------------------------------------------------------------
sub scom_events_count
{
    # Tries to initialize a db connection and count the number of rows in the scom_events table
    # Args:
    #   ref to an error string which gets populated if there was an error
    # Returns :
    #   undef and populated error string by ref if failed to initialize, prep or exec
    #   Count of rows otherwise, including possibly 0 hence the error string 
    
    my ( $error_ref ) = @_;
    my ( $query, $sqlQuery, $rv, @row ) ;

    $query = "SELECT COUNT(*) FROM $scom_events_table;";
    eval { $sqlQuery  = $database_handle->prepare($query); } ; 
    if ( $@ ) { 
        ${$error_ref} = "Cannot prepare $query: $@" ; 
        return undef; 
    };
    $rv = $sqlQuery->execute() or do { ${$error_ref} = "Cannot execute the query: " . $sqlQuery->errstr; return undef; };
    @row= $sqlQuery->fetchrow_array();
    ${$error_ref} = undef; # indicates no error
    return $row[0]; # return number of rows counted
}

# ----------------------------------------------------------------------------------------------------------------
sub purge_event
{
    # Deletes an event from the scom events table, optionally saving it first in the save table
    # Whether the event is saved is determined by the config option for the purge marker..
    # These valid purge markers that associate with the <save_to_db> options are :
    #  - processed 
    #  - aged_out 
    #  - resolved 
    #  - maintenance
    #  - filtered[:description] - as by a config exclusion and/or constraint rule
    #  - other[:description]
    # Args:
    #   a ref to a built scom event that contains a purge marker
    #   an error ref
    # returns 1 success, 0 otherwise + error ref

    #return 0; # emulate failure

    my ( $built_scom_event, $error_ref ) = @_;
    my ( $save, $query, $sqlQuery, $rv ) ;
    my $purge_state = $built_scom_event->{$purge_marker};

    # In the case of the purge state being other:reason or filtered:reason - strip off :reason so the lookup works later
    $purge_state =~ s/:.*$//g if $purge_state =~ /^(other|filtered)/; 

    # Take the event's purge marker value
    if ( not defined $built_scom_event->{$purge_marker} ) {
        #$logger->error( "Event $built_scom_event->{AlertId} (scom table id $built_scom_event->{RowId}) has no purge marker set - no purge or save will be done");
        ${$error_ref} .= "Event $built_scom_event->{AlertId} (scom table id $built_scom_event->{RowId}) has no purge marker set - no purge or save will be done. " ;
        return 0;
    }

    # If the option to save this type of purge state is 'yes' in the config, then save the event
    elsif ( $master_config->{save_to_db}->{ $purge_state } )  {
        $save = 1;
    }
    # otherwise it'll just be deleted from scom_events table
    else { 
        $save = 0;
    }
    
    # If not saving, just delete the event from the scom_events table
    if ( not $save ) {
        $query = "DELETE FROM $scom_events_table WHERE id = $built_scom_event->{RowId};";
        $sqlQuery = $database_handle->prepare($query) or do { 
            #$logger->error( "Can't prepare $query: " . $database_handle->errstr); 
            ${$error_ref} = "Can't prepare $query: " . $database_handle->errstr; 
            return 0; 
        } ;
        $sqlQuery->execute() or do { 
            #$logger->error( "Can't execute the query: " . $sqlQuery->errstr ) ; 
            ${$error_ref} = "Can't execute the query: " . $sqlQuery->errstr ;
            return 0; 
        };
    }

    # Otherwise, saving so : save, add the save reason, delete 
    else { 
        # Copy the event from the scom_events table to the scom_save table
        $query = "INSERT INTO $scom_save_table SELECT * FROM $scom_events_table WHERE id = $built_scom_event->{RowId};";
        $sqlQuery = $database_handle->prepare($query) or do { 
            #$logger->error( "Can't prepare $query: " . $database_handle->errstr); 
            ${$error_ref} = "Can't prepare $query: " . $database_handle->errstr ;
            return 0; 
        } ;
        $sqlQuery->execute() or do {
            #$logger->error( "PURGE : Can't execute the query: $@") ; 
            ${$error_ref} = "Can't execute the query: " . $sqlQuery->errstr ; 
            return 0; 
        };

        # Add the reason for saving to the saved event
        $query = "UPDATE $scom_save_table SET savereason = '$built_scom_event->{$purge_marker}' WHERE id = $built_scom_event->{RowId};";
        $sqlQuery = $database_handle->prepare($query) or do { 
            #$logger->error( "Can't prepare $query: " . $database_handle->errstr); 
            ${$error_ref} = "Can't prepare $query: " . $database_handle->errstr; 
            return 0;
        };
        $sqlQuery->execute() or do { 
            #$logger->error( "PURGE : Can't execute the query: $@" );
            ${$error_ref} = "Can't execute the query: " . $sqlQuery->errstr ;
            return 0; 
        };

        # Remove the event from the scom_events table
        $query = "DELETE FROM $scom_events_table WHERE id = $built_scom_event->{RowId};";
        $sqlQuery = $database_handle->prepare($query) or do { 
            #$logger->error( "Can't prepare $query: " . $database_handle->errstr); 
            ${$error_ref} = "Can't prepare $query: " . $database_handle->errstr; 
            return 0;
        } ;
        $sqlQuery->execute() or do {
            #$logger->error( "PURGE : Can't execute the query: $@" );
            ${$error_ref} = "Can't execute the query: " . $sqlQuery->errstr ;
            return 0; 
        }
    }

    return 1;
}

# ----------------------------------------------------------------------------------------------------------------
sub build_2012_event
{
    # Builds one event ie 'builds' it into something Feeder api and ultimately REST api can use.
    # Args 
    #   a raw xml event data structure
    #   a ref to var which will contain the built event
    #   an error ref
    # 
    # Returns 
    #   1 on success with a populated built scom event data structure
    #   0 otherwise and a default populated structure of less than useful data that shouldn't be used, and error by ref

    my ( $scom_event, $built_scom_event, $error_ref ) = @_;
    my ( $criteria, $host_down, $down_field, $hostname_field_value, $hostname_custom_mapped ) ;

    # The structure of a built scom event ...
    %{$built_scom_event} = (
        'AlertId'=>'alertid_undefined', # useful to have this for debugging and logging
	    'Host' => 'host_undefined', # the host name
	    'HostGroup' => 'hostgroup_undefined', # the hostgroup(s)
	    'ServiceDescription' => 'servicedescription_undefined', # the service name
	    'ServiceState' => 'UNKNOWN', # this is a translated-to-GW SCOM event state 
	    'HostState' => 'UP', # state of host - may decide to do something with this but for now they all get set to UP 
	    'HostMessage' => 'SCOM host objects are assumed to be UP (default)',  # host message
	    'LastPluginOutput' => 'lastpluginoutput_undefined', # the service message
	    'HostFirstInsertDate' => '2000-01-01 00:00:00', # various time fields for events
	    'HostReportDate' => '2000-01-01 00:00:00',# various time fields for events
	    'ServiceFirstInsertDate' => '2000-01-01 00:00:00',# various time fields for events
	    'ServiceReportDate' => '2000-01-01 00:00:00',# various time fields for events
	    'Severity' => 'UNKNOWN', # service state
       #'WebConsoleUrl' => undef,  # leaving out for now
       # Unused but left in for reference
	   #'Category' => 'category_undefined', 
	   #'httplink' => 'httplink_undefined',
       #'id'=>'id_undefined', 
	   #'LastCheckTime' => '2000-01-01 00:00:00',
	   #'LastInsertDate' => '2000-01-01 00:00:00',
	   #'MonitorServerName' => 'localhost', 
	   #'Priority' => 'priority_undefined', 
	   #'ScheduledDowntimeDepth' => '0', 
	   #'StateType' => 'HARD', 
	   #'SubComponent' => 'subcomponent_undefined', # only required for 4913 xml api 
	   #'TextMessage' => 'textmessage_undefined', # use LastPluginOutput instead
	);

    # Note
    # The reaper only reads in the event xml, checks that each property name has a column in scom_events, and shoves it in.
    # It doesn't check for expected or missing fields - that is this data feeder's responsibility.
    # That checking is built in below where necessary

    # useful in logging later
    $built_scom_event->{RowId} = $scom_event->{id} ;

    # This is used in logging info during processing and purging. It seems all scom events have these.
    if ( $scom_event->{alertid} ) { 
        $built_scom_event->{AlertId} = $scom_event->{alertid}
    }
    else {
        #$logger->error("Event was missing an AlertId - (scom table id $scom_event->{id})");
        ${$error_ref} .= "Event was missing an AlertId - (scom table id $scom_event->{id})";
        return 0;
    } 

    # first append uuid to some fields - this could be useful for tracking down issues later if processing fails
    $built_scom_event->{Host} .= "_$scom_event->{alertid}";
    $built_scom_event->{ServiceDescription} .= "_$scom_event->{alertid}";

    # Try to derive GW hostname name from event
    # -----------------------------------------

    # If remap rules are provided, try to use one of them
    if ( defined $feeder->{properties}->{custom_hostname_mapping_rules} and scalar keys %{ $feeder->{properties}->{custom_hostname_mapping_rules} } ) {

        REMAPRULE: foreach my $remap_rule ( sort keys %{$feeder->{properties}->{custom_hostname_mapping_rules}} ) {

            $hostname_custom_mapped = 1; # assume success will occur in remapping
            # go through all of the mapping fields - if they all match, then note that $hostname_custom_mapped=1
            MAP: foreach my $map ( sort keys %{$feeder->{properties}->{custom_hostname_mapping_rules}->{$remap_rule}} ) {

                # skip the special fields that say how to extract the hostname itself
                next if $map =~ /^_/ ; 

                # Found in 3.1.0 QA : It's possible that the map doesn't exist as a key here - eg I found 3 out of 7500 events that had no MonitoringObjectPath
                # This causes the  !~ to fail with a Perl WARN which ends up hiding in the log file
                next if not defined $scom_event->{ lc $map } ;  

                if ( $scom_event->{ lc $map } !~ m/$feeder->{properties}->{custom_hostname_mapping_rules}->{$remap_rule}->{$map}/ ) {
                    $hostname_custom_mapped = 0;
                    last MAP;
                }
            }

            if ( $hostname_custom_mapped ) {

                # Try to extract the host name now from the event
                $hostname_field_value = $scom_event->{  lc $feeder->{properties}->{custom_hostname_mapping_rules}->{$remap_rule}->{_hostname_field} } ;
                if ( $hostname_field_value ) { 
                    $hostname_field_value =~ s/$feeder->{properties}->{custom_hostname_mapping_rules}->{$remap_rule}->{_hostname_location}/$1/g;
                    if ( $hostname_field_value ) { 
                        # Adjust this event's hostname to be the name of the down host
                        $logger->debug("Hostname remapping : event alert id $built_scom_event->{AlertId} - name '$built_scom_event->{Host}' remapped to '$hostname_field_value'");
                        $built_scom_event->{Host} = $hostname_field_value;
                    }
                    else {
                        #$logger->error("Could not remap hostname from $scom_event->{alertid} (scom table id $scom_event->{id})");
                        ${$error_ref} .= "Could not remap hostname from $scom_event->{alertid} (scom table id $scom_event->{id})";
                        return 0;
                    }
                }
                else {
                    #$logger->error("Could not derive remap from $scom_event->{alertid} (scom table id $scom_event->{id})");
                    ${$error_ref} .= "Could not derive remap from $scom_event->{alertid} (scom table id $scom_event->{id})";
                    return 0;
                }

                # one of the remap rules was applied so don't process any more of them
                last REMAPRULE; 
            }
        }
    }
    
    # If the custom hostname mapping rules didn't set the hostname, drop back to the 'usual' method
    if ( not $hostname_custom_mapped ) {

        if ( $scom_event->{monitoringobjectpath} ) {
	        $built_scom_event->{Host} = $scom_event->{monitoringobjectpath};
	    }
	    elsif ( $scom_event->{monitoringobjectdisplayname} ) {
		    $built_scom_event->{Host} = $scom_event->{monitoringobjectdisplayname} ; 
	    }
        else {
            #$logger->error("Could not derive a host name from event $scom_event->{alertid} (scom table id $scom_event->{id})");
            ${$error_ref} .= "Could not derive a host name from event $scom_event->{alertid} (scom table id $scom_event->{id})";
            return 0;
        }
    }


    # Try to derive GW hostname state from event
    # ------------------------------------------
    # The criteria for determining if a host is regarded as down is defined in the <host_down_criteria> config sub-hashes.
    # Each sub hash is a criteria that contains raw event xml fields and associated regex's. 
    # Each critera is checked against for a match. Within that criteria, 
    # all things need to match in order for the host to be regarded as down.
    if ( defined $feeder->{properties}->{host_down_criteria} and scalar keys %{ $feeder->{properties}->{host_down_criteria} } ) {

        DOWNCRITERIA: foreach $criteria ( sort keys %{$feeder->{properties}->{host_down_criteria}} ) {

            # assume the host is down, and disprove - ie if any of the regex's don't match, then host is not down
            $host_down = 1;
    
            DOWNFIELD: foreach $down_field ( keys %{ $feeder->{properties}->{host_down_criteria}->{$criteria} }  ) {
                next if $down_field =~ /^_/ ; # skip the special fields that say how to extract the hostname itself
                # If the value in the event's field doesn't match the regex in the down criteria, then host is not down
                if ( $scom_event->{ lc $down_field } !~ m/$feeder->{properties}->{host_down_criteria}->{$criteria}->{$down_field}/ ) {
                    $host_down = 0; # not down
                    last DOWNFIELD; # stop checking fields
                }
            }

            # Try to get the hostname out of the event, and mark the host as down.
            # NOTE This is experimental because haven't tried it with many down-type events yet. Mar 19 2015
            if ( $host_down ) {
    
                # Mark the host state as down if host was determined to be down
                $built_scom_event->{HostState} = 'UNSCHEDULED DOWN' ;
    
                # Try to extract the host name now from the event
                $hostname_field_value = $scom_event->{  lc $feeder->{properties}->{host_down_criteria}->{$criteria}->{_hostname_field} } ;
                if ( $hostname_field_value ) { 
                    $hostname_field_value =~ s/$feeder->{properties}->{host_down_criteria}->{$criteria}->{_hostname_location}/$1/g;
                    if ( $hostname_field_value ) { 
                        # Adjust this event's hostname to be the name of the down host
                        $built_scom_event->{Host} = $hostname_field_value;
                    }
                    else {
                        #$logger->error("Could not derive hostname (in DOWN state) from $scom_event->{alertid} (scom table id $scom_event->{id})");
                        ${$error_ref} .= "Could not derive hostname (in DOWN state) from $scom_event->{alertid} (scom table id $scom_event->{id})";
                        return 0;
                    }
                }
                else {
                    #$logger->error("Could not derive hostname (in DOWN state) from $scom_event->{alertid} (scom table id $scom_event->{id})");
                    ${$error_ref} .= "Could not derive hostname (in DOWN state) from $scom_event->{alertid} (scom table id $scom_event->{id})";
                    return 0;
                }

                $logger->debug( "HOST $built_scom_event->{Host} determined to be $built_scom_event->{HostState} - host-down criteria:'$criteria'");
                $built_scom_event->{HostMessage} = "HOST $built_scom_event->{Host} determined to be $built_scom_event->{HostState} - host-down criteria:'$criteria'";

                # V 2.0.6
                # SCOM works by generating an event for a problem, but then updates it's resolution state for when it is ok again. So in this case, 
                # if the host down criteria was met, but the resolution state = 255, then that indicates that the issue was cleared ie the host is up again.
                if ( $scom_event->{resolutionstate} == 255) { 
                    $built_scom_event->{HostState} = 'UP' ;
                    $logger->debug( "HOST $built_scom_event->{Host} determined to be $built_scom_event->{HostState} - host-down criteria:'$criteria', resolution state = 255");
                    $built_scom_event->{HostMessage} = "HOST $built_scom_event->{Host} determined to be $built_scom_event->{HostState} - host-down criteria:'$criteria', resolution state = 255";
                }

                # one of the criteria matched and so don't check any others
                last DOWNCRITERIA; 
            }
        }
    }

    # 3.0.2 - moved this cleanup bit to here since the Host can be set in the hoststate code block above too. cleanup also lc's the name now too.
    # Sometimes a hostname is a url with /'s - that will fail the API. Possibly other things later on too will need cleaning up.
    $built_scom_event->{Host} = cleanup_hostname( $built_scom_event->{Host} );


    # Try to derive GW service name
    # -----------------------------
    if ( $scom_event->{monitoringclassname} ) {
	    $built_scom_event->{ServiceDescription} = $scom_event->{monitoringclassname} ; # Might need groundworkify'ing
	}
	elsif ( $scom_event->{name} ) {
		$built_scom_event->{ServiceDescription} = $scom_event->{name} ; # Might need groundworkify'ing
    }
    else {
        #$logger->error("Could not derive a service name from event $scom_event->{alertid} (scom table id $scom_event->{id})");
        ${$error_ref} .= "Could not derive a service name from event $scom_event->{alertid} (scom table id $scom_event->{id})";
        return 0;
	}

    # Try to derive the service state
    # -------------------------------
    # NOTE that setting 'ServiceState' - this is the GW service state - so later don't try to derive it again
    # Map of lowercase SCOM event service states, and their mapped GW service states
    my %gw_service_statuses = (
        'critical'    => 'UNSCHEDULED CRITICAL',
        'warning'     => 'WARNING',
        'information' => 'OK',
    );
    # If the scom even has 'severity' defined, and, there's a mapping for it in the gw state map, use it.
    if ( defined $scom_event->{severity} and exists $gw_service_statuses{ lc $scom_event->{severity} } ) {
        $built_scom_event->{ServiceState} =  $gw_service_statuses{ lc $scom_event->{severity} };
    }
    # Otherwise leave it as unknown
    else {
        $built_scom_event->{ServiceState} = 'UNKNOWN';
    };
    # However, set closed events to OK
    if ( $scom_event->{resolutionstate} == 255) { # scom 'closed'
	    $built_scom_event->{ServiceState} = $built_scom_event->{Severity} = "OK"
	}

    # Try to derive the service message
    # ---------------------------------
    if ( $scom_event->{description} ) { 
        $built_scom_event->{LastPluginOutput} = $scom_event->{description} ;
        $built_scom_event->{LastPluginOutput} =~ s/(\n|\r)/ /g; # convert newlines/cr-lf's to spaces
    };

    # Try to derive the hostgroup(s) that this host will go into
    # -------------------------------------------------------
    my ( $hgs, $k, %newhg, $newk, $hgk, $macro ) ;
    # If have some servicenames->hostgroup mappings defined, process them
    if ( defined $feeder->{properties}->{servicename_hostgroup_mappings} and scalar keys %{ $feeder->{properties}->{servicename_hostgroup_mappings} } ) {
        foreach $k ( keys %{$feeder->{properties}->{servicename_hostgroup_mappings}} ) { 
            if ( $built_scom_event->{ServiceDescription} =~ m/$k/ ) { 
                $hgs->{ $feeder->{properties}->{servicename_hostgroup_mappings}->{$k}  } = undef;
            }      
        }
    }
    # If have some hostnames->hostgroup mappings defined, process them
    if ( defined $feeder->{properties}->{hostname_hostgroup_mappings} and scalar keys %{ $feeder->{properties}->{hostname_hostgroup_mappings} } ) {
        foreach $k ( keys %{$feeder->{properties}->{hostname_hostgroup_mappings}} ) { 
            if ( $built_scom_event->{Host} =~ m/$k/ ) { 
                $hgs->{ $feeder->{properties}->{hostname_hostgroup_mappings}->{$k}  } = undef;
            }      
        }
    }
    # If host groups were resolved via regexes, use them
    if ( $hgs ) { 
        $built_scom_event->{HostGroup} = $hgs;
    }
    # Otherwise use the default hostgroups
    else {
        $built_scom_event->{HostGroup} = $feeder->{properties}->{default_hostgroups};
    }
    # Do any macro subs of entries in the HostGroup hash
    foreach $hgk ( keys %{$built_scom_event->{HostGroup}} ) { 
        $newk = $hgk;
        foreach $macro ( "category", "computerdomain", "managementgroupname", "managementserver" ) { # 3.1.2 fix
            $newk =~ s/{$macro}/$scom_event->{$macro}/g;   
        }
        $newhg{$newk} = undef;
    }
    $built_scom_event->{HostGroup} = { %newhg };

    # Event related times
    # TBD Need to figure out how best to use these in rest of feeder processing
    #
    # The event has these times :
    #   - TimeAdded        		   the time this event was created in SCOM for the first time
    #   - TimeOfLastEvent  		   the time this event was last updated in SCOM
    #   - TimeResolutionStateLastModified  the time the 'resolution state' was modified
    #
    # GW events have these time properties
    #   - firstInsertDate   First time this event was updated    
    #   - lastInsertDate    Last time this event was updated     
    #   - reportDate        The date this event was reported
    #
    # Mapping:
    #   - firstInsertDate => TimeOfLastEvent (ie don't try to make GW insert date match scom insert date 
    #                                         because no idea yet if that will make a whole lot of sense)
    #   - lastInsertDate  => unset for now unless start trying to make GW match dates to SCOM
    #   - reportDate      => TimeOfLastEvent
    # 
    # Build event:
    #   - HostFirstInsertDate for now is same as ServiceFirstInsertDate (likewise for ReportDate)
    #     until have some examples of host-state-specific scom events (if there is such a thing)
    #   - No timezone info seems to be available in the raw event, but GW event's require it - so at least adding localtime's tz 

    # If timeoflastevent was set, use that, otherwise if timeadded, use that (that logic is from original version)
    # Verison 2.0.3 notes
    # - since times from SCOM are UTC, enter them into GW using UTC -0000 tz, not local tz
    # - Status Viewer and GW app will render in local time 
    # - TBD verify format of time is ok, else status viewer can get confused
    # This could change more in the future - eg if this host/service doesn't exist, use the dateadded, else use the timeoflastevent? Feedback required.
    if ( $scom_event->{timeoflastevent} ) {
        $built_scom_event->{HostFirstInsertDate} = $built_scom_event->{ServiceFirstInsertDate} = $scom_event->{timeoflastevent} . '-0000' ; # strftime("%z", localtime );
        $built_scom_event->{HostReportDate}      = $built_scom_event->{ServiceReportDate}      = $scom_event->{timeoflastevent} . '-0000' ; # strftime("%z", localtime );
    }
    elsif ( $scom_event->{timeadded} ) { 
        $built_scom_event->{HostFirstInsertDate} = $built_scom_event->{ServiceFirstInsertDate} = $scom_event->{timeadded} . '-0000' ; # strftime("%z", localtime );
        $built_scom_event->{HostReportDate}      = $built_scom_event->{ServiceReportDate}      = $scom_event->{timeadded} . '-0000' ; # strftime("%z", localtime );
    }
    else {
        #$logger->error("Could not derive time related fields from event $scom_event->{alertid} (scom table id $scom_event->{id})");
        ${$error_ref} .= "Could not derive time related fields from event $scom_event->{alertid} (scom table id $scom_event->{id})";
        return 0;
    }

    # This might make more sense for host first insert date... ??? But until know what TimeAdded means in the event, leaving it to timeoflastevent if that was set
    #if ( $scom_event->{timeadded} ) {
    #    $built_scom_event->{HostFirstInsertDate} = $scom_event->{timeadded} . strftime("%z", localtime) ;
    #}
 
    # Leaving out for now but ref. for possible future REST API use.
    # This would be great to use in status viewer's Related Links section
    #if ( $scom_event->{webconsoleurl} ) {
    #    $built_scom_event->{WebConsoleUrl} = $scom_event->{webconsoleurl};
    #}

    return 1; 

}

# ----------------------------------------------------------------------------------------------------------------
sub cleanup_hostname
{
    # tries to clean up a hostname so that it will get through the GW REST API successfully without too much
    # loss of information or translation.
    my ( $hostname ) = @_;
    if ( $hostname =~ m{\/} ) { 
        $hostname =~ s#/#%2F#g; # meh - apache might not like it but it gets through for now
    }
    $hostname = lc $hostname; # V 3.0.2 - lower case the hostname
    return $hostname;
}

# ----------------------------------------------------------------------------------------------------------------
sub groundworkify 
{
    # Takes a scom object string (eg a service name, a service message etc)
    # and makes it safe for GW consumption

    my ( $string ) = @_;

    # TBD for now this just returns identity - lets see if run into issues with new REST API
    return $string;

    if ( ! $string) {
        $string="unknown_scom_object";
    }
    else {
        $string =~ s/\s/-/g; # space -> -
        $string =~ s/\.//g;  # remove periods
        $string =~ s/\t/ /g; # tab -> space
    };

    return $string;
}

# ----------------------------------------------------------------------------------------------------------------
sub filtered_by_constraint_regex
{
    # Looks at hostname_constraint_filters and servicename_constraint_filters filter regexs to see 
    # if the event should be excluded from processing. Host takes priority. 
    # This logic allows for either of the host or services filters to be undefined completely. 
    # Here is the logic table for all of the combos :
    #
    # case  host regexes defined    service regexes defined host match  service match   filter event ?  return logic
    #
    # 1     no                      no                      -           -                no             0
    # 2     no                      yes                     -           no               yes            ! service match
    # 3     no                      yes                     -           yes              no             ! service match
    # 4     yes                     no                      no          -                yes            ! host match
    # 5     yes                     no                      yes         -                no             ! host match
    # 6     yes                     yes                     no          no               yes            ! ( host match and service match )
    # 7     yes                     yes                     no          yes              yes            ! ( host match and service match )
    # 8     yes                     yes                     yes         no               yes            ! ( host match and service match )
    # 9     yes                     yes                     yes         yes              no             ! ( host match and service match )
    # 
    #
    # Args
    #   ref to built scom event
    # Returns
    #   1 - the built scom event did not match any of the constraint filters
    #   0 - the built scom event matched at least one of the constraint filters, or, no constraint filters were given
    # if it matches a regex and the event needs filtering ie not processing, 0 otherwise

    my ( $event ) = @_;
    my ( $regex, $hostname_constraint_filters_defined, $servicename_constraint_filters_defined, $host_matched, $service_matched ) ;

    $hostname_constraint_filters_defined = $servicename_constraint_filters_defined = $host_matched = $service_matched = 0;

    # see if the event should be excluded if hostname did not match any regex's
    if ( defined $feeder->{properties}->{hostname_constraint_filters} and scalar keys %{ $feeder->{properties}->{hostname_constraint_filters} } ) {
        $hostname_constraint_filters_defined = 1;
        HNCREGEX: foreach $regex ( keys %{$feeder->{properties}->{hostname_constraint_filters}} ) { 
            if ( $event->{Host} =~ m/$regex/ ) { 
                $host_matched = 1;
                last HNCREGEX;
            }      
        }
    }

    # see if the event should be excluded if servicename did not match any regex's
    if ( defined $feeder->{properties}->{servicename_constraint_filters} and scalar keys %{ $feeder->{properties}->{servicename_constraint_filters} } ) {
        $servicename_constraint_filters_defined = 1;
        SNCREGEX: foreach $regex ( keys %{$feeder->{properties}->{servicename_constraint_filters}} ) { 
            if ( $event->{ServiceDescription} =~ m/$regex/ ) { 
                $service_matched = 1;
                last SNCREGEX;
            }      
        }
    }

    # return logic
    # case 1: if neither of filters were defined, just return 0 - ie don't filter the event
    if    ( not $hostname_constraint_filters_defined and not $servicename_constraint_filters_defined  ) {
        return 0;
    }

    # cases 2 and 3
    elsif ( not $hostname_constraint_filters_defined and     $servicename_constraint_filters_defined  ) {
        return (not $service_matched );
    }

    # cases 4 and 5
    elsif (     $hostname_constraint_filters_defined and not $servicename_constraint_filters_defined  ) {
        return not $host_matched ;
    }

    # cases 6,7,8 and 9
    else { 
        return not ( $host_matched and $service_matched ) ;
    }
    
}

# ----------------------------------------------------------------------------------------------------------------
sub filtered_by_exclusion_regex
{
    # looks at hostname_exclusion_filters and servicename_exclusion_filters filter regexs to see 
    # if the event should be excluded from processing. 
    # Returns 1 if it matches a regex and the event needs filtering ie not processing, 0 otherwise
    my ( $event ) = @_;

    my ( $regex );

    #$logger->trace( "----------------- $event->{ServiceDescription}" );

    # see if the event should be excluded based on a hostname regex match 
    if ( defined $feeder->{properties}->{hostname_exclusion_filters} and scalar keys %{ $feeder->{properties}->{hostname_exclusion_filters} } ) {
        foreach $regex ( keys %{$feeder->{properties}->{hostname_exclusion_filters}} ) { 
            #print "Compare $event->{Host} against /$regex/\n";
            if ( $event->{Host} =~ m/$regex/ ) { 
                return 1;
            }      
        }
    }

    # see if the event should be excluded based on a servicename regex match 
    if ( defined $feeder->{properties}->{servicename_exclusion_filters} and scalar keys %{ $feeder->{properties}->{servicename_exclusion_filters} } ) {
        foreach $regex ( keys %{$feeder->{properties}->{servicename_exclusion_filters}} ) { 
            #print "Compare $event->{ServiceDescription} against /$regex/\n";
            if ( $event->{ServiceDescription} =~ m/$regex/ ) { 
                return 1;
            }      
        }
    }

    return 0;
}

# ----------------------------------------------------------------------------------------------------------------
sub print_event
{
    # Prints out a built event
    my ( $endpoint_name, $event, $dumpstats ) = @_;

    # which fields to be dumped out
    my @fields = (
	                'Host',
	                'ServiceDescription' ,
	                'LastPluginOutput' ,
	                'ServiceState' ,
	                'HostState' ,
	                'HostGroup' ,
	                'HostMessage' ,
	                'HostFirstInsertDate' ,
	                'HostReportDate' ,
	                'ServiceFirstInsertDate' ,
	                'ServiceReportDate' ,
	                'Severity' ,
                    'AlertId',
                   #'WebConsoleUrl' ,
	            );

    my $filename = "${endpoint_name}_${show}";
    open(EVENTS, ">> $filename") or die "Error opening $filename : $!\n";

    if ( not defined $dumpstats ) {
        if ( not $printed_hdr ) { 
            print EVENTS join ("\t", @fields), "\n"; # print the headers
            $printed_hdr = 1; # make a note that the headers have been printed 
        }
        foreach my $field ( @fields ) {
            if ( $field eq 'HostGroup' ) {
                print EVENTS join ';;', ( sort keys %{$event->{$field}} ) ;
                print EVENTS "\t";
            }
            else {
                print EVENTS "$event->{$field}\t";
            }
        }
        print EVENTS "\n";

        # stats stats
        if ( exists $showstats{services}{ $event->{ServiceDescription} } ) { 
            $showstats{services}{ $event->{ServiceDescription} } += 1 ; } 
        else { 
             $showstats{services}{ $event->{ServiceDescription} } += 1;
        }

        # host stats
        if ( exists $showstats{hosts}{ $event->{Host} } ) {
             $showstats{hosts}{ $event->{Host} } += 1 ; 
        }
        else {
            $showstats{hosts}{ $event->{Host} } = 1;
        }
    
    }
    else {
        if ( scalar keys %showstats ) { # if there were some hosts/services produced ...
            print EVENTS "\n\n";
            print EVENTS "Services stats\n";
            print EVENTS "Service name\tOccurences\n";
            foreach my $name (sort { $showstats{services}{$b} <=> $showstats{services}{$a} } keys %{$showstats{services}} ) {
            print EVENTS "$name\t$showstats{services}{$name}\n";
            }
            print EVENTS "\n\n";
            print EVENTS "Host stats\n";
            print EVENTS "Host name\tOccurences\n";
            foreach my $name (sort { $showstats{hosts}{$b} <=> $showstats{hosts}{$a} } keys %{$showstats{hosts}} ) {
            print EVENTS "$name\t$showstats{hosts}{$name}\n";
            }
        }
        else { # otherwise no events so say so
            print EVENTS "No events.\n";
        }
    }

    close EVENTS;

}

# -------------------------------------------------------------
sub remove_show_file
{
    # tries to remove a show mode file
    # Args : 
    #   - an endpoint file name ($show contains the base filename for show output)
    # Returns 
    #   - 1 on success
    #   - 0 otherwise
    my ( $endpoint_name ) = @_;
    my $filename = "${endpoint_name}_${show}";

    $logger->debug("Removing $filename");

    # remove the old results tsv
    unlink $filename ;
    if (  -e $filename ) { 
        $logger->error( "Could not delete -show file : $filename") ;
        return 0;
    }
    return 1;

}

# -------------------------------------------------------------
sub terminate_rest_api 
{
    # Release our handle to the REST API (if we used it), to force the REST API to call its destructor.
    # This will attempt to log out before Perl's global destruction pass wipes out resources needed for
    # logout to work properly.
    #$feeder->{rest_api} = undef;
    foreach my $feeder_object ( keys %feeder_objects ) {
        $feeder_objects{$feeder_object}->{rest_api} = undef;
    }
}

# ----------------------------------------------------------------------------
sub read_master_config
{
    # Takes a master feeder configuration file and returns by ref a hash of its parsed contents.
    # This routine is not put in Feeder.pm since it's logic might need to change depending on the feeder.
    # returns 1 on success, 0 otherwise

    my ( $master_config_file ) = @_;

    eval {
        $master_config = TypedConfig->new( $master_config_file );
        # Force the reading of the expected contents to make TypedConfig do most of the validation for us
        # These variables are not used other than right here.
        my $feeder_enabled                   = $master_config->get_boolean( 'feeder_enabled' );
        my $system_indicator_check_frequency = $master_config->get_number(  'system_indicator_check_frequency' );
        my $endpoint_max_retries             = $master_config->get_number(  'endpoint_max_retries' );
        my $endpoint_retry_wait              = $master_config->get_number(  'endpoint_retry_wait' );
        my $retry_cache_directory            = $master_config->get_scalar(  'retry_cache_directory' );
        my $dbtype                           = $master_config->get_scalar(  'dbtype' );
        my $dbhost                           = $master_config->get_scalar(  'dbhost' );
        my $dbport                           = $master_config->get_number(  'dbport' );
        my $dbname                           = $master_config->get_scalar(  'dbname' );
        my $dbuser                           = $master_config->get_scalar(  'dbuser' );
        my $dbpass                           = $master_config->get_scalar(  'dbpass' );
        my $save_to_db                       = $master_config->get_hash(    'save_to_db' );
        my $event_max_age                    = $master_config->get_number(  'event_max_age' );
    };
    if ($@) {
        chomp $@;
        $@ =~ s/^ERROR:\s+//i;
        $logger->error("Cannot read master config file $master_config_file: $@");
        $master_config = undef;  # just in case, undefine the TypedConfig object to release file handles
        return 0;
    }

    # validate for expected configuration - need at least endpoint(s) defined
    if ( not exists $master_config->{endpoint} ) {
        $logger->error("Master config file $master_config_file doesn't contain any endpoints");
        $master_config = undef;  # just in case, undefine the TypedConfig object to release file handles
        return 0;
    }

    # The order of the endpoints is important and preserved by using array rather than hash (ie Config <block>'s).
    # If only one endpoint is configured, coerce it from a scalar into an array
    # (If >1 endpoint entries are given, TypedConfig gives an array)
    if ( ref $master_config->{endpoint} ne 'ARRAY' ) { 
        $master_config->{endpoint} = [ $master_config->get_scalar('endpoint') ];
    }

    # Check endpoint config looks reasonable
    foreach my $endpoint ( @{$master_config->{endpoint}} ) {
        if ( $endpoint !~ /:/ ) { 
            $logger->error("Master config file endpoint incorrect syntax - expecting format : <endpoint name>:<endpoint config file>");
            $master_config = undef; 
            return 0;
        }
        my $endpoint_config = ( split /:/, $endpoint )[1];
        $endpoint_config =~ s/\s//; # get the config file associated with this endpoint
        # check config file is fully qualified
        if ( $endpoint_config !~ /^\// ) { 
            $logger->error("Master config file endpoint incorrect syntax - expecting fully qualified filename - got '$endpoint_config'");
            $master_config = undef; 
            return 0;
        }
        # check config file exists and is readable
        if ( ! -r $endpoint_config or ! -e $endpoint_config ) {
            $logger->error("Master config file endpoint configuration file '$endpoint_config' doesn't exist or is not readable");
            $master_config = undef; 
            return 0;
        }
    }

    # Further validation of values
    if ( $master_config->{system_indicator_check_frequency} <= 0 ) { 
        $logger->error("Master config file system_indicator_check_frequency should be positive non zero number");
        $master_config = undef; 
        return 0;
    }
    if ( $master_config->{endpoint_max_retries} < 0 ) { 
        $logger->error("Master config file endpoint_max_retries should be non-negative");
        $master_config = undef; 
        return 0;
    }
    if ( $master_config->{endpoint_retry_wait} < 0 ) { 
        $logger->error("Master config file endpoint_retry_wait should be non-negative");
        $master_config = undef; 
        return 0;
    }

    # validate save_to_db
    my $save_to_db_valid = 1;
    my $save_key;
    my %valid_save_keys = ( 
        'processed'   => undef,
        'aged_out'    => undef,
        'resolved'    => undef,
        'maintenance' => undef,
        'filtered'    => undef,
        'other'       => undef,
    );
    # 1. check all required keys are given
    foreach $save_key ( keys %valid_save_keys ) {
        if ( not exists $master_config->{save_to_db}->{$save_key} ) {
            $logger->error("Feeder main configuration is missing a required save_to_db key '$save_key'");
            $save_to_db_valid *= 0;
        }
    }
    # 2. check keys given are all valid
    foreach $save_key ( keys %{$master_config->{save_to_db}} ) {
        if ( not exists $valid_save_keys{$save_key} ) {
            $logger->error("Unrecognized main configuration save_to_db key '$save_key'");
            $save_to_db_valid *= 0;
        }
    }
    if ( not $save_to_db_valid ) { 
        $logger->error("Master config file save_to_db error was found");
        $master_config = undef; 
        return 0;
    }

    # validate event_max_age - should be >0
    if ( $master_config->{event_max_age} <= 0 ) { 
        $logger->error("Master config file event_max_age should be positive non zero number");
        $master_config = undef; 
        return 0;
    }
    
    

    return 1;
}

# ---------------------------------------------------------------------------------
sub get_data
{
    # Runs a query against the GWME scom database, and puts the rows of data into a global data structure @query_data.
    # Args:
    #   error ref
    # Returns 
    #   1 on success, 0 and no data otherwise
    #   Also, populates this global data structure :
    #
    #    @query_data = (  {   
    #                        "querytime" => time(), # This is the time reading of all of the data completed
    #                        "rows"      => @data   # @data is an array of hashes, each entry a row from the data query
    #                     }
    #                  );
    #
    # The @query_data structure is first emptied before attempting to populate it.
    # All data required for subsequent processing steps (build_data, sync_endpoint etc)
    # should all be gathered in this one routine.

    my ( $error_ref ) = @_;
    my ( $sql, $sth, $row, $host_name, $thold_alarm, $bl_alarm, @data, $total_rows, $processed_rows, $skipped_count );

    $logger->debug("Getting data from $master_config->{dbname} database, table $scom_events_table");

    # Build a query for generating the dataset
    if ( not generate_query( \$sql ) ) {
        #$logger->fatal("Could not generate the main query - no processing will be done.");
        ${$error_ref} = "Could not generate the main get_data query - no processing will be done.";
        return 0;
    }

    # Prepare
    $logger->trace("get_data() : SQL = '$sql'" );
    $sth = $database_handle->prepare($sql) or do { 
        #$logger->fatal( "SQL ERROR failed to prepare sql '$sql' : $@"); 
        #${$error_ref} = "SQL ERROR failed to prepare sql '$sql' : $@";
        ${$error_ref} = "SQL ERROR failed to prepare sql '$sql' : " . $database_handle->errstr;
        return 0; 
    };
  
    # Execute
    $logger->trace("Running sql query against database ...");
    $sth->execute() or do { 
        #$logger->fatal( "SQL ERROR failed to execute sql '$sql' : $@" ); 
        ${$error_ref} = "SQL ERROR failed to execute sql '$sql' : " . $sth->errstr;
        return 0; 
    };

    # Process : this loop builds a data structure out of the raw query data.
    @data = (); 
    $total_rows = 0;
    QUERYROW: while ( $row = $sth->fetchrow_hashref() )  {
        $total_rows++; # keep count of all rows the query returns
        # Build up the central data array (array preserves query row order).
        # Each row ref is a hash, and that will be stored in the array.
        if ( defined $limit and $total_rows > $limit ) { 
            $logger->info("!!! Limiting intake of events to $limit !!!");
            last QUERYROW;
        }
        push @data, $row;
    }

    # Build the global data structure - a timestamped-once set of rows from this query
    @query_data = (   {   
                           "querytime" => time(),  
                           "rows"      => [ @data ] 
                      }
    );

    # log some info about # of queried rows 
    $logger->debug("Queried data contained $total_rows rows.");

    return 1;

}

# ---------------------------------------------------------------------------------
sub generate_query
{
    # Generates a query upon to use in get_data() - this data drives the entire feeder.
    # Args:
    #   just a ref to the sql var that will be populatd by this routine
    # Returns 
    #   1 on success, 0 otherwise
    my ( $query_ref ) = @_;

    ${$query_ref} = "SELECT * FROM $scom_events_table ORDER BY timeoflastevent;"; # note the ordering
    return 1;
}

# ---------------------------------------------------------------------------------
sub close_database_connection
{
    # Closes connection to the database 
    # Returns 1 on success (including if there database handle is undefined), 0 otherwise. 
    # Not quite sure what to do in failure case here tho.
    my ( $error_ref ) = @_;

    return 1 if not defined $database_handle;
    eval { $database_handle->disconnect(); } ; # a failed disconnect will kill the script
    if ( $@ ) {
        chomp $@;
        #$logger->error ("DATABASE ERROR Cannot disconnect from database '$master_config->{dbname}'. Error: '$@'");
        ${$error_ref} = "Database error -  Cannot disconnect from database '$master_config->{dbname}'. Error: '$@'";
        return 0;
    }
    return 1;
}

# ---------------------------------------------------------------------------------
sub build_data
{
    # Description:
    # Takes one timestamped series of data in a structure as follows :
    #       {   
    #           'querytime' => 1423491849,
    #           'rows' => [   
    #                        { <query row 1 key-values> },
    #                        { <query row 2 key-values> },
    #                        ...
    #                     ]
    #       }
    # 
    # To this it adds :
    #           'rows_built_data' => [ 
    #                               { built data 1 key-values },
    #                               { built data 2 key-values },
    #                               ...
    #                           ]
    #            
    # Args : 
    #    1. name of endpoint
    #    2. reference to one timestamped data structure as described above
    #    3. an error by ref
    #
    # Returns :
    #   - Update-by-ref the timestamped data structure by adding the 'rows_built_data' key=>[ { }, { } ... ] structure
    #   - 1 on success, 0 otherwise, with error set by ref
    # Notes:
    #   In the event of an event failing to build, because build_2012_event failed, the event will need to be marked for purging
    #   otherwise it will be repeatedly built and fail to build. If a stack of these types of events come in, the feeder will fall
    #   behind.
    #

    my ( $endpoint_name, $ref_data_to_build, $error_ref ) = @_;
    my ( $row, $alertid, $scom_table_id, %built_scom_event ) ;

    $logger->debug("Building data for endpoint $endpoint_name, query time $ref_data_to_build->{querytime}");

    # Iterate over the query data and "build" it.
    @{$ref_data_to_build->{rows_built_data}} = (); # reset the global built data structure
    DATAROW: foreach $row ( @{ $ref_data_to_build->{rows} } ) {

        $alertid       = $row->{alertid}; # the uuid of the event- seems to be possibly uniq across all SCOM events - not 100% sure yet
        $scom_table_id = $row->{id};      # the scom_save table row index

        # create built event data
        my $build_event_error ;
        if ( not build_2012_event( $row, \%built_scom_event, \$build_event_error) ) {
            #$logger->error("Failed to build scom event (scom table id $scom_table_id)");
            ${$error_ref} = "Failed to build scom event (scom table id $scom_table_id. $build_event_error";
            # If there was a problem building the event, note that info in the built event itself.  This will be picked up by sync_endpoint later.
            $built_scom_event{failed_to_build} = undef;
        }

        # Add the built stuff to the rows_built_data key in the time-stamped data structure
        push @{$ref_data_to_build->{rows_built_data}}, { %built_scom_event };
    }

    return 1;

}

# ---------------------------------------------------------------------------------
sub a_cache_needs_flushing
{
    # checks if a retry cache needs flushing. returns 1 if yes, 0 otherwise (or if error)
   
    my ( %retry_caches_that_need_flushing, $error );

    a_retry_cache_needs_flushing( $feeder_name, $master_config->{retry_cache_directory}, $master_config->{endpoint}, \%retry_caches_that_need_flushing, \$error ) ;
 
    if ( defined $error ) { 
        $logger->error( "Couldn't reliably check retry caches" );
        return 0;
    }
    else {
        if ( scalar keys %retry_caches_that_need_flushing ) {
            $logger->debug("A retry cache needs flushing");
            return 1;
        }
        else { 
            $logger->debug("No retry caches need flushing");
            return 0;
        }   
    }
}


# ---------------------------------------------------------------------------------
sub remove_feeder_objects_from_foundation
{
    # Summary 
    # Removes feeder-created host/services from foundation - specifically in this feeder's case
    # this is for removing hosts/services when an endpoint has changed name
    #
    # Arguments :
    #   - error ref
    #
    # Returns 1 on ok, 0 otherwise and error populated

    my ( $error_ref ) = @_;

    my ( %outcome, %results, @hosts_bundle, $foundation_host, %foundation_hosts_to_delete, %foundation_services_to_delete ,
         $foundation_service, %delete_hosts_options, %delete_services_options );


    my ( %health_hostgroup_members, @health_hostgroup_members, %endpoint_hosts, @endpoint_hosts, $compare, $host, %hosts_results, %services_results, $error );

    $logger->debug("Checking/syncing health hostgroup members/services");

    # Get a list of health hostgroup members
    %health_hostgroup_members = (  $feeder->{properties}->{health_hostgroup} => undef );
    if ( not $feeder->feeder_get_hostgroup_members( \%health_hostgroup_members ) ) {
        ${$error_ref} = "Couldn't get members of health hostgroup for feeder";
        return 0;
    }
    @health_hostgroup_members = sort keys %{$health_hostgroup_members{$feeder->{properties}->{health_hostgroup}}{members}};

    # Get a list of all of the endpoint names 
    %endpoint_hosts = map { (split ':',$_)[0] => 1 }  @{ $master_config->{endpoint} } ;
    @endpoint_hosts = sort keys %endpoint_hosts;

    # Compare the endpoint hostnames with the existing healthgroup hostnames - if equal nothing to do
    $compare = Array::Compare->new();
    if ( $compare->compare( \@health_hostgroup_members, \@endpoint_hosts) ) {
        $logger->debug( "No change detected between endpoint hostname set (@endpoint_hosts), and feeder health group set (@health_hostgroup_members) - nothing to remove." );
        return 1;
    }

    # Get a list of all hosts from foundation that were created by this feeder. These results will be used in later logic.
    # There should always be some hosts at this point that were created by this feeder, because of the initialization of health services that should have been called before here.
    $logger->debug( "Getting all Foundation hosts that were created by this feeder");
    if ( not $feeder->{rest_api}->get_hosts( [ ] , { query => "agentId = '$feeder->{guid}'", depth => 'simple' }, \%outcome, \%hosts_results ) ) {
        $logger->debug("No hosts were found that were created by this feeder." );
    }

    # Main removal logic
    # foreach host :
    #   if the host is a member of endpoint set, do nothing - no change
    #   delete the metrics services
    #   remove the host from the feeder health hostgroup
    #   if the host was created by the feeder, and there are no more services on it, delete the host

    foreach $host ( @health_hostgroup_members ) {

        # if the existing health group host is a member of endpoint set, do nothing - no change
        next if exists $endpoint_hosts{$host} ;

        # get the metrics services created by this feeder, for this host 
        $feeder->{rest_api}->get_services( [ ], { 
                hostname => $host, 
                #query => "property.Notes = '$GW::Feeder::metric_service_meta_tag' and s.agentId = '$feeder->{guid}' ", # s.agentId means service agentId, s.host.agentId means host's agentId
                query => "property.Notes = '$GW::Feeder::metric_service_meta_tag' and agentId = '$feeder->{guid}' ", # agentId now means service agentId, and use hostAgentId to get hosts with /api/services.
                format=>'host,service' }, 
                \%outcome, \%services_results);

        my %services_to_delete;
        foreach my $service ( keys %{$services_results{$host}} ) {
            $services_to_delete{ $host } { $service } = 1;
        }
        $logger->debug( "Delete these services from host '$host' : " . Dumper \$services_to_delete{$host} ); # delete_services will dump out debug
        if ( not $feeder->feeder_delete_services( \%services_to_delete , {} ) ) { 
            ${$error_ref} .= "An error occurred removing the services";
            # keep going - do as much as poss 
        }

        # Remove the host from the health hostgroup
        if ( not $feeder->remove_host_from_hostgroup( $host, $feeder->{properties}->{health_hostgroup}, \$error ) ) {
            ${$error_ref} .= "Couldn't remove host $host from hostgroup $feeder->{properties}->{health_hostgroup}}. $error";
            # Keep going tho - caller will have to check for error being set, with ok status on this sub
        }

        # If the host was created by the feeder, and there are no more services on it, delete the host
        #print "Press enter to get service sfor host $host...\n"; <STDIN>;
        my ( %new_service_results, %new_outcome ) ;
        if ( exists $hosts_results{$host} ) { # if the host was created by the feeder 

            if ( not $feeder->{rest_api}->get_services( [ ] , { hostname => $host } , \%new_outcome, \%new_service_results)  ) {
                if ( $new_outcome{response_code} eq '404' ) { 
                    # no services were found for this host so delete it
                    if ( not $feeder->feeder_delete_hosts( { $host => 1 } , { } )  )  { 
                        ${$error_ref} .= "Could not delete host '$host'.";
                    }
                }
                else { 
                    ${$error_ref} .= "Cannot get services for $host : " . Dumper \%new_outcome, \%new_service_results;
                }
            }
            else  {
                # Do nothing.
                # services were found so don't delete the host. These services were added by other feeders. Eg localhost NAGIOS.
            }

        }

    }

    return 1;
}

__END__

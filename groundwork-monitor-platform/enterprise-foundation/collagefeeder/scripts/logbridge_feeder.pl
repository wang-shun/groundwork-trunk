#!/usr/local/groundwork/perl/bin/perl
# LogBridge feeder - integrates elasticsearch with GroundWork
#
# Copyright 2013-2016 GroundWork OpenSource
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
#
# 2015-06-15 DN - v1.0.0 - initial version 
# 2015-06-18 DN - v1.0.1 - Documentation added 
# 2015-07-20 DN - v1.0.2 - Minor logging update
# 2015-08-17 DN - v1.0.3 - Minor fix : undefined took times around line 332 after calc of metrics;
#                          Major fix : added size param to get_saved_kibana_searches and es_search_simple (was defaulting to 10)
# 2016-02-23 DN - v2.0.0 - refactoring for metrics updates, and improved movement of errors up into health service etc
# 2016-05-16 DN - v2.0.1 - Changed size from 999999 to 9999 for ES 2.2.1 to work - look for 2.0.1 tag
# 2016-08-25 GH - v2.0.2 - Die automatically after running for an hour, as a temporary fix to work around memory leaks (GWMON-12626), to be restarted by the parent supervise.
# 2016-09-02 DN - v2.0.3 - pulled out 2.0.2 work - it risks exhausting the server-side auth token cache due to known issues with /api/logout garbage cleanup. Instead,
# 			   the last mod time is monitored and the master config only read when the mtime has changed.
# 2016-10-25 DN - v2.0.4 - GWMON-12769 very minor change for 5.24 testing if array is empty or not. This is for 7.1.1 and introduced by the new version of Perl 5.24
# 2017-01-12 DN - v2.0.5 - GWMON-12864 Updated query generator construct_es_query() to work with new Elasticsearch 5.x DSL; 
#                          squashed bug in construct_services_and_esqueries() where if prefix = kibana search name, avoid empty service name;
#                          system_requirements_met() now returns the version of elasticsearch which is then used in logic in construct_es_query()
# 2017-01-20 DN - v2.0.6 - Configuration refactored to better support x-pack (user/password authenticated elasticsearch sessions and SSL)
#
# NOTE - Update $VERSION below when changing the version # here.
#      - VIM : set tabstop=4  set expandtab - please use just these settings and no others
#
# TO DO 
#   fix this : WARN - WARN  Warning: XML::LibXML compiled against libxml2 20708, but runtime libxml2 is older 20706 
#   Flesh out es_search_result_ok() ?
#   Facilitate desc= prop for hostgroups in lb conf xml ?

use 5.0;
use warnings;
use strict;
use version;
my  $VERSION = qv('2.0.6'); # keep this up to date
use GW::Feeder qv('0.5.4'); 
use TypedConfig qw(); # leave qw() on to address minor bug in TypedConfig.pm
use Data::Dumper; $Data::Dumper::Indent = 1; $Data::Dumper::Sortkeys = 1; $Data::Dumper::Terse = 1;
use Log::Log4perl qw(get_logger :levels);
use File::Basename;
use JSON;
use DBI;
use Getopt::Long;
use Time::HiRes;
use Time::Local;
use POSIX qw(strftime);
use Sys::Hostname; 
use Search::Elasticsearch;
use XML::Simple;
use Scalar::Util qw(looks_like_number);

our $feeder_name = "logbridge_feeder"; # Feeder name - various things key off this such as logging and retry caching
our $master_config;
my $master_config_file = '/usr/local/groundwork/config/logbridge_feeder.conf'; # Config file for this feeder
our ( $logger, $log4perl_config, $logfile );
my ( $clean, $every, $help, $show_version, $once, $yes) = undef; # CLI option vars
my ( $database_handle, $feeder, $tests_config, @query_data, %feeder_objects ) = undef; # Various globals
my $elastic_search_object; # global elasticsearch api object
our $fmee_timestamp=-1; # this is used in Feeder.pm for testing with fmee
my $xml_root_hg = 'root-hg'; # the xml config root hostgroup element name
my %supported = ( 'elasticsearch' => '1.5.0' ,  'lucene' => '4.10.4' ); # supported versions of relevant feeder source stuff
my %feeder_services = (
     "$feeder_name.cycle.elapsed.time"   => "Time taken to process last cycle",
     "$feeder_name.esearches.run"        => "Counts of successful or unsuccessful elastic searches performed",
     "$feeder_name.esearches.durations"  => "Total elapsed (including network etc) / Total 'took' time reported by elasticsearch",
);

# =================================================================================
main();
# =================================================================================

END {
    # To be kind to the server and always disconnect our session, we attempt to force a shutdown
    # of the REST API before global destruction sets in and makes it impossible to log out,
    # regardless of how we got to the end of the program.
    terminate_rest_api();
    
    # We generally run this daemon under control of supervise, which will immediately attempt to
    # restart the process when it dies.  In order to prevent a tight loop of failure and restart,
    # we delay process exit a short while no matter how we're going down.
    sleep(5); 
}

# ---------------------------------------------------------------------------------
sub main
{
    # main sub that does initialiation, processing loop etc

    my $fresh_start_time = time();

    my ( $start_time, $cycle_count, $total_cycle_time_taken, $events_retrieved, $events_sent, $retrieve_time, $send_time ,
         $total_events, $total_events_processed, %feeder_options, %master_config, $endpoint, $endpoint_name, $endpoint_config ,
         @endpoint_data, $retry_cache_filename, $timestamped_dataset, @built_timestamped_data  ,
         $max_retries, $try, $disabled_notice_given, $started_message ,
         $sync_status, $total_built_thresholds_count, $successfully_processed_built_rows, $total_successfully_processed_built_rows, $total_queried_threshold_rows ,
         $total_took_time , $total_elapsed_time , $successfully_execd_es_searches_count , $unsuccessfully_execd_es_searches_count ,
         $sum_total_took_time , $sum_total_elapsed_time , $sum_successfully_execd_es_searches_count , $sum_unsuccessfully_execd_es_searches_count, $get_data_elapsed_time ,
         @unsuccessful_service_names, %metrics, $endpoint_health_hostgroup, $error, $error_message, $cache_file_truncated_message,
         $count_of_data_rows_cached, %retry_cache_size, $aged_out_count, $cache_imported_rows_count, $build_error, $sync_error, $get_data_error, $endpoint_enabled
    );
    my $started_at = localtime;

    # v2.0.3 : This will be used to control re-reads in the main CYCLE loop. Seeded with 0 to force the first read.
    my $master_config_last_mod_time = 0;

    # read and process cli opts
    initialize_options();

    # get logger details
    if ( not initialize_logger('started', $master_config_file ) ) {
        print "Cannnot initialize logger - quitting!\n";
        exit;
    }

    # Check for other feeders running - there can only be one ... but allow -help option to run concurrently
    $logger->logexit("Another $feeder_name is running - quitting") if ( perl_script_process_count( basename($0) ) > 1 );

    # Log app starting
    if ( $once ) {
        $started_message = "Feeder $feeder_name running once at $started_at";
    }
    else {
        $started_message = "Feeder $feeder_name started at $started_at";
    }
    $logger->info($started_message);

    # Get data, build it and sync the endpoint(s) in a never ending cycle
    $cycle_count = 1; $disabled_notice_given = 0;
    CYCLE: while ( 1  )
    {
        $logger->info( ">>>>>>>> Starting cycle $cycle_count <<<<<<<<" ) if not $disabled_notice_given;

	# v2.0.3 Only (re)read the master config if the mast mod time changed
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


        # Set of options for new feeder objects
        %feeder_options = (
            ## The log4perl logger
            logger => $logger,
            # Feeder specific options to retrieve and type-check.
            # Access standard or specific properties with $feeder->{properties}->{propertname}, e.g., $feeder->{properties}->{cycle_time}
            feeder_specific_properties => {
               #always_send_full_updates               => 'boolean',
               #full_update_frequency                  => 'number',
                foundation_follows_conf                => 'boolean',
                host_bundle_size                       => 'number',
                hostgroup_bundle_size                  => 'number',
                retry_cache_max_age                    => 'number',
                service_bundle_size                    => 'number',
            }
        );

        # Set up interrupt handlers for various signals.  Notification of the interrupt will attempted to be sent to each endpoint.
        # This could use some more work since it doesn't work that well if the feeder is waiting for something to do.
        initialize_interrupt_handlers( $master_config->{endpoint}, \%feeder_options);

        # check/prepare retry cache directory
        if ( not endpoint_retry_cache_prep_dir( $logger, $master_config->{retry_cache_directory} ) ) {
            $logger->error("A problem was found with the retry cache directory - waiting for a minute before restarting processing cycle...");
            sleep 60; 
            next CYCLE;
        }
    
        # Wait until there's an indication to proceed. In the case of the feeder,
        # that just means waiting for a period of time.
        # # If -every option was supplied, use that as the trigger to start the main cycle instead - useful for testing
        #my $wait;
        #if ( $cycle_count != 1 ) { # start right away
        #    $wait = ( defined $every ? $every : $master_config->{system_indicator_check_frequency} ) ;
        #    $logger->info("Waiting for $wait seconds before running next cycle ...\n");
        #    sleep $wait;
        #}
        # Wait is moved to end of cycle loop now

        # Populates the @query_data global data structure.
        # This step also cleanses the queried data if necessary.
        
        
        if ( not $clean ) { # don't bother getting data if doing a cleanup
            if ( not get_data( \$get_data_elapsed_time, \$get_data_error ) ) {
                #$logger->error("A problem occurred getting the data for the feeder! Trying again in 30 seconds.");
                $logger->error("A problem occurred getting the data for the feeder!");
                # instead of sleeping, and next cycle, want this error to get into the metrics services stream
                #sleep 30;
                #next CYCLE;
            }
        }

        # Initialize the metrics data structure
        %metrics = ( );

        # Synchronize each endpoint, in the order they are specified in the master config file
        ENDPOINT: foreach $endpoint ( @{$master_config->{endpoint}}  ) {
        
            # Reset the summation of metrics related sums
            $sum_total_took_time = $sum_total_elapsed_time = $sum_successfully_execd_es_searches_count = $sum_unsuccessfully_execd_es_searches_count = 0;
            @unsuccessful_service_names = ();

            # Make a copy of the raw data for processing just this endpoint. 
            # Later unprocessed data will be put in this endpoints retry cache at the end of this ENDPOINT iteration.
            @endpoint_data = @query_data; 

            # Extract the endpoint name and the endpoint configuration file from the master feeder endpoint config list
            # read_master_config() has validated the content of $endpoint 
            ( $endpoint_name, $endpoint_config ) = split /:/, $endpoint;


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


            $start_time = Time::HiRes::time();
            $logger->info( "======== Processing endpoint ::: '$endpoint_name' ========" ); 

            # Destroy feeder if defined - expecting this to invoke the $feeder->DESTROY() automagically and that this is the last reference in use.
            # Don't do this
            # undef $feeder if defined $feeder;

            # Create a retry cache filename for this hostname, feeder and endpoint combo
            $retry_cache_filename = endpoint_retry_cache_name( $GW::Feeder::feeder_host, $feeder_name, $endpoint_name, $master_config->{retry_cache_directory} ) ;

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
            else  {
                # A feeder object already exists so use it.
                # If the auth token expires, RAPID will re-auth it and during the initialize_health_objects step later
                $feeder = $feeder_objects{$endpoint_name} ;
            }

            # Do some final value sanity checking of feeder-specific configuration
            my $validation_error;
            if ( not validate_feeder_specific_options( \$validation_error ) ) {
                $error_message = "Feeder host $GW::Feeder::feeder_host : A problem was found in endpoint '$endpoint_name' configuration that needs fixing - updating its retry cache and ending processing attempt. $validation_error";
                stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $error_message, 'general_errors' );
                if ( not endpoint_retry_cache_write( $endpoint_name, $retry_cache_filename, \@endpoint_data, $logger, "a", \$count_of_data_rows_cached, \%retry_cache_size, \$cache_file_truncated_message ) ) {
                    stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, "Failed to update the retry cache!", 'caching_errors' );
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

            if ( $clean ) {
                $feeder->cleanup( $yes, 1 ) ;
                next ENDPOINT;
            }

            # If there was a problem with getting the data, still want to propogate that info up into metrics services, but nothing else
            if ( defined $get_data_error ) { 
                $error_message = "Feeder host $GW::Feeder::feeder_host : An error occurred getting the data. $get_data_error";
                stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $error_message, 'general_errors' ) ;
                # Need to call update_feeder_stats to populate the services array for the endpoint, else send_metrics will figure there's nothing to do.
                update_feeder_stats( $cycle_count, scalar localtime, 0, 0, 0, 0, 0, 0, [ ], \@{ $metrics{endpoints}{$endpoint_name}{services} } );
                $metrics{endpoints}{$endpoint_name}{feeder_object} = $feeder;
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
            if ( not $feeder->initialize_health_objects( $started_message, \%feeder_services ) ) {
                # Don't continue for now since failing to do this simple step indicates a bigger issue probably. 
                # For example, if there was a license error (eg not installed) or a general REST breakdown.
                $error_message = "Feeder host $GW::Feeder::feeder_host : An error occurred initializing feeder health objects - ending processing attempt for this endpoint.";
                stage_error_for_publishing_via_metrics($logger, \%metrics, $endpoint_name, $error_message, 'general_errors');
                if ( not endpoint_retry_cache_write( $endpoint_name, $retry_cache_filename, \@endpoint_data, $logger, "a", \$count_of_data_rows_cached, \%retry_cache_size, \$cache_file_truncated_message ) ) {
                    stage_error_for_publishing_via_metrics($logger, \%metrics, $endpoint_name, "Failed to update the retry cache!", 'caching_errors');
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


            # Process each time-stamped set of raw data for this endpoint (just 1 set if empty retry cache, >1 otherwise)
            $sync_status = 1; # start out optimistically ;)
            ENDPOINTDATASET: while ( $sync_status == 1 and $timestamped_dataset = shift @endpoint_data ) {

                $fmee_timestamp = $timestamped_dataset->{querytime}; # for fmee testing logic

                # reset metrics for this endpoint
                $total_took_time = $total_elapsed_time = $successfully_execd_es_searches_count = $unsuccessfully_execd_es_searches_count = 0;

                # Prepare the data for processing by building it for this timestamped dataset
                if ( not build_data( $endpoint_name, $timestamped_dataset, \$build_error ) ) { # NOTE build_data always returns success- code here left in for future use perhaps
                    $error_message = "Feeder host $GW::Feeder::feeder_host : A data build error was encountered - endpoint will not be processed. $build_error";
                    stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $error_message,  'general_errors');
                    $sync_status = 0;
                    last ENDPOINTDATASET;
                }


                # At this point, a time-stamped entry of the @endpoint_data array has been updated with a rows_built_data key and data.
                # Now synchronize the endpoint using this time-stamped built data set.
                # The sync process will remove items from the dataset that were successfully processed, leaving things that were not and that need putting back into the retry cache. 
                # Question : Does retry-caching at this level of granularity will work generally, or need to fail as a complete timestamped data set?
                # Answer : it doesn't really work at that level of granularity - will cache all data on sync failure.

                # sync_endpoint returns 1 on ok
                $successfully_processed_built_rows = 0;
                $sync_status = sync_endpoint( $cycle_count, $endpoint_name, $timestamped_dataset, \$successfully_processed_built_rows, \$sync_error);  

                # Converts audit into events in foundation, and empties the $feeder->{audit_trail} data structure
                # Flush the audit trail even if there was a problem with sync'ing since it only gets populated if something was actually done.
                $feeder->flush_audit();  # this will produce its own errors if necessary

                # If there was a sync error, the put all of the timestamped data back into the retry cache.
                # Actually if there was NO error, remove this dataset from the endpoint data set, and,
                # if there is an error with the sync, just stop trying to process any more endpoint data
                # Then put back into the cache whatever is in the endpoint data.
                # This approach regards the entire set of endpoint data (ie all timestamped sets) as one long
                # set of time-ordered things to process. If there's a problem midway through processing this long list,
                # then stop, flag an error, and put stuff back into the cache to retry later.

                if ( $sync_status != 1 ) {  
                    $error_message = "Feeder host $GW::Feeder::feeder_host : An error in syncing data set for endpoint '$endpoint_name' occurred - no more processing will be done for this endpoint. $sync_error" ;
                    stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $error_message,  'general_errors');
                    # Let this then drop out in to the remaining code here 
                }

                # metrics - for this cycle : 
                #    total took for all searches and    total elapsed for all searches - helps to understand effect of network latency too
                #    all es searches : successfully run / unsuccessfully run
                calculate_cycle_metrics( $timestamped_dataset->{'rows'}[0] , \$total_took_time, \$total_elapsed_time, \$successfully_execd_es_searches_count, \$unsuccessfully_execd_es_searches_count, \@unsuccessful_service_names );

                # Sum up things 
                $sum_total_took_time += $total_took_time if defined $total_took_time;
                $sum_total_elapsed_time += $total_elapsed_time if defined $total_elapsed_time;
                $sum_successfully_execd_es_searches_count += $successfully_execd_es_searches_count;
                $sum_unsuccessfully_execd_es_searches_count += $unsuccessfully_execd_es_searches_count;

                # Remove the rows_built_data : a) if don't, it will end up in a retry cache which should only be { timestamp=>time, rows=> [ {}, ... ]  }, and, b) good practice
                # Only do this if $timestamped_data is still non empty.
                delete $timestamped_dataset->{rows_built_data_hosts}    if exists $timestamped_dataset->{rows_built_data_hosts}; 
                delete $timestamped_dataset->{rows_built_data_services} if exists $timestamped_dataset->{rows_built_data_services}; 

            } # end ENDPOINTDATASET loop

            # If sync of a timestamped dataset failed, the above while loop will have exited and now 
            # need to put the timestamped dataset that failed to process back into the endpoint data 
            # prior to writing it out to the retry cache
            if ( $sync_status != 1 ) {
                @endpoint_data = ( $timestamped_dataset, @endpoint_data ) ;
            }
    
            # Write the @endpoint_data data back to the retry cache - even if it's empty
            if ( not endpoint_retry_cache_write( $endpoint_name, $retry_cache_filename, \@endpoint_data, $logger, "w", \$count_of_data_rows_cached, \%retry_cache_size, \$cache_file_truncated_message ) ) { 
                $error_message = "An error occurred in writing retry cache for endpoint '$endpoint_name'. $cache_file_truncated_message";
                stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $error_message, 'caching_errors');
            }
            # GWMON-12363 : make service 'retry_caching' go critical, status message  "Endpoint GW_Server can't be reached. Last message send: YYYY-MM-DD:HH:MM:SS, cached ### data rows"
            else {
                # This will update a service that contains error information about the caching occurring with a count of how many rows written
         #      stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, "Retry cache file written at " . localtime() . " - $count_of_data_rows_cached rows written" , 'caching_errors');
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

            # Update feeder stats services 
            $total_cycle_time_taken = sprintf "%0.0f", ( Time::HiRes::time() - $start_time ) * 1000; # ms so all time metrics are same uom
            my $finished_time = localtime();
            update_feeder_stats( $cycle_count, 
                                 $finished_time, 
                                 $total_cycle_time_taken, 
                                 $get_data_elapsed_time, 
                                 $sum_successfully_execd_es_searches_count, 
                                 $sum_unsuccessfully_execd_es_searches_count, 
                                 $sum_total_took_time, 
                                 $sum_total_elapsed_time,
                                 \@unsuccessful_service_names,
                                 \@{ $metrics{endpoints}{$endpoint_name}{services} } 
            );

            # If there's a Feeder api object successfully created then it meant that the REST API endpoint was up. 
            # Put a reference to this Feeder object into %metrics for use later
            $metrics{endpoints}{$endpoint_name}{feeder_object} = $feeder;

        } # end ENDPOINT loop

        # If -clean opt was supplied, just quit
        exit if $clean;

        # Send metrics out to all endpoints - this also send perf data to the metrics services if defined.
        if ( not send_metrics( \%metrics, \$error, $logger ) ) {
            $logger->error("A problem was encountered processing metrics");
            $logger->error($error) if defined $error;
        }

        # The -once option is intended for testing purposes and doesn't currently send updates to feeder health services
        if ( $once ) {
            $logger->info( "Run-once option supplied - feeder shut down - exiting" );
            exit; 
        }

        if ( $every ) { 
            $logger->info("Sleeping $every seconds");
            sleep $every;
        }
        else {
            $logger->info("Sleeping $master_config->{system_indicator_check_frequency} seconds");
            sleep $master_config->{system_indicator_check_frequency};
        }
    
        # Increment the cycle number
        $cycle_count++;

    }
    
}

# ---------------------------------------------------------------------------------
sub get_data
{
    # Algorithm
    # 
    # get groups, hosts and prefixes 
    # get es searches from kibana
    # 
    # foreach config hostgroup {
    #     foreach config host {
    #        if ( there are es search matches for prefix ) {
    #             foreach es search match {
    #                 @constructed_services = construct service names ( search matches, config row ) - returns  [ { name=<name>, timerange=<range>, query=<es query to run>, [other props later] }, { ... } ,... ]
    #                 foreach @constructed_service_names {
    #                    execute es search and update service message and status
    #                    update hostgroup->host->services->service data structure
    #                 }
    #             }
    #         }
    #         else { # no es search match for prefix
    #             # do nothing - leave it to staleness checker later to update possibly orphaned services on this host
    #         }
    #     }
    #     update host status props in data structure (eg Up (N searches executed) , Down (0 searches executed)
    # }
    # 
    # This structure is an array containing one hash as follows:
    #
    #    @query_data = (  {   
    #                        "querytime" => time(), # This is the time reading of all of the data completed
    #                        "rows"      => @data   # @data is an array of hashes, each entry a row from the data query
    #                     }
    #                  );
    #
    # An entry in @data looks like this :
    #  hostgroup =>
    #       GWhost1 => { 
    #               hostStatus => <a GW host state>,
    #               hostStatusMessage => <a GW host state message>,
    #               services => {
    #                               <svc1> => {  serviceState => <state>,  serviceMessage => <message>, etc },
    #                               <svc2> => {  serviceState => <state>,  serviceMessage => <message>, etc },
    #                               ...
    #                           }
    #       }
    #  }
    #
    # All data required for subsequent processing steps (build_data, sync_endpoint etc) should all be gathered in this one routine.
    #
    # args
    #   timing ref
    #   error ref
    #
    # returns 
    #   success : 1 and populated @query_data
    #   failure : 0 and unpopulated @query_data
    #   How long it all took, in ms

    my ( $how_long_this_all_took_ref, $error_ref ) = @_;
    my ( $sql, $sth, $row, $host_name, $thold_alarm, $bl_alarm, @data, $total_rows, $processed_rows, $skipped_count,
         $es_ver, %kibana_searches, $logbridge_config, $search_prefix, $desc, @tholds, @matched_searches, 
         @constructed_services, $constructed_service, $hostgroup, $host, $es_result, $es_error, $service_message, 
         $service_status, $matched_count, %data, $elapsed_time, $config_error, $sysreqs_error, $ksearches_error
    );

    ${$how_long_this_all_took_ref} = 0; # jic failure later

    ${$error_ref} = undef;

    my $start_time  = Time::HiRes::time();

    if ( not initialize_elasticsearch() ) { 
        #$logger->error("Failed to initialize elasticsearch!");
        ${$error_ref} = "Failed to initialize elasticsearch!";
        return 0;
    }

    $logger->debug("Getting data from elasticsearch");

    # Check system requirements
    if ( not system_requirements_met( \$es_ver, $error_ref ) ) {
        return 0;
    }

    # get elasticsearch searches saved via Kibana
    if ( not get_saved_kibana_searches( \%kibana_searches, $error_ref ) ) {
        return 0;
    }

    # Get the logbridge groups config 
    if ( not parse_logbridge_groups_configuration( \$logbridge_config, $error_ref ) ) {
        return 0;
    }

    HOSTGROUP: foreach $hostgroup ( sort keys %{ $logbridge_config->{$xml_root_hg} } ) {  # foreach hostgroup

        HOST: foreach $host ( keys %{ $logbridge_config->{$xml_root_hg}->{$hostgroup}->{'hosts'}[0]->{'host'}  } ) { # foreach host in hostgroup

            $logger->debug( "get_data() : hostgroup:$hostgroup, host:$host" );

            # Grab the search prefix and then use it to find matched searches
            $search_prefix = $logbridge_config->{$xml_root_hg}->{$hostgroup}->{'hosts'}[0]->{'host'}->{$host}->{'prefix'}; # get the search prefix
            @matched_searches = grep /^$search_prefix/, keys %kibana_searches; # get keys from searches that match config here

            # process matched searches ie build GW services with the corresponding queries
            if ( @matched_searches ) {

                $logger->debug( "\tKibana searches matched for hostgroup $hostgroup, host $host, prefix $search_prefix :  @matched_searches") ;

                # construct service names
                @constructed_services = ();
                if ( not construct_services_and_esqueries( $hostgroup, $host, \@matched_searches, $logbridge_config, \%kibana_searches, \@constructed_services, $es_ver ) ) {
                    #$logger->error( "Error constructing GroundWork service names - skipping $hostgroup -> $host") ;
                    ${$error_ref} .= "Error constructing GroundWork service names - skipping processing $hostgroup -> $host. ";
                    next HOST;
                }

                # just for debug output ...
                my @snlist;
                foreach $constructed_service ( @constructed_services ) { 
                    foreach my $service ( sort keys %{$constructed_service} )  {  
                        push @snlist, $service;
                    }
                }

                $logger->debug( "\tConstructed services names : " , join ', ', @snlist );

                $logger->trace( "\tConstructed services details : " . Dumper \@constructed_services ) ;
               
                foreach $constructed_service ( @constructed_services ) {
                    SERVICE: foreach my $service ( sort keys %{$constructed_service} ) { 

                        $service_status = "UNKNOWN"; $service_message = "Unknown"; # Just in case, seed some results

                        # Execute es search and update service message and status
                        $logger->trace("\tExecuting matched elasticsearch query : " . Dumper $constructed_service->{$service}->{'esquery'} );
                        ( $es_result, $es_error, $elapsed_time ) = undef;
                        if ( not es_search_preconstructed( $constructed_service->{$service}->{'esquery'},  \$es_result, \$es_error, \$elapsed_time ) ) {
                            $logger->error( "Error executing elasticsearch search for service $hostgroup -> $host -> $service");
                            $service_status  = 'UNSCHEDULED CRITICAL';
                            $service_message = "Error executing elasticsearch search: $es_error"; 
                        }

                        # The search exec'd, but need to check the result   
                        # TBD flesh out es_search_result_ok() - currently it just returns true always - will see how this works out
                        elsif ( not es_search_result_ok( \$es_result, \$es_error ) ) {
                            $logger->error( "Error detected in elasticsearch search result for service $hostgroup -> $host -> $service");
                            $service_status  = 'UNSCHEDULED CRITICAL';
                            $service_message = "Error executing elasticsearch search: $es_error"; 
                        }

                        # otherwise if there's a hits count, use that as the service message
                        else {
                            if ( defined $es_result->{'hits'}->{'total'} ) { 
                                $service_message = $es_result->{'hits'}->{'total'}; 
                                $service_status  = 'OK'; # assume ok and adjust
                                if ( defined $constructed_service->{$service}->{'critical_threshold'} ) {
                                    $service_message .= " (critical threshold is $constructed_service->{$service}->{'critical_threshold'})";
                                    if ( $es_result->{'hits'}->{'total'} >= $constructed_service->{$service}->{'critical_threshold'} ) { 
                                        $service_status  = 'UNSCHEDULED CRITICAL';
                                    }
                                }
                            }
                        }

                        # update hostgroup->host->services->service data structure 
                        $data{ $hostgroup }{ $host }{ 'services' }{ $service }{ 'description'   } = $constructed_service->{$service}->{'desc'};
                        $data{ $hostgroup }{ $host }{ 'services' }{ $service }{ 'monitorStatus' } = $service_status;
                        $data{ $hostgroup }{ $host }{ 'services' }{ $service }{ 'statusMessage' } = $service_message;

                        # add into this structure how long es reported the search took to run
                        $data{ $hostgroup }{ $host }{ 'services' }{ $service }{ 'took' } = $es_result->{'took'} if defined $es_result->{'took'};

                        # add into this structure how long it took to run it including network etc
                        $data{ $hostgroup }{ $host }{ 'services' }{ $service }{ 'elapsed' } = $elapsed_time if defined $elapsed_time;
                    }
                }

            }
            else {
                $logger->debug("\tNo Kibana saved searches matched");
                # do nothing - leave it to staleness checker later to update possibly orphaned services on this host  
            }

            # Update host status props in data structure (eg Up (N searches executed) , Down (0 searches executed)
            my $matched_searches_count = scalar @matched_searches;
            if ( not $matched_searches_count ) {
                # might not always want to add hosts with no services - here's an option for that
                if ( $master_config->{add_hosts_with_no_search_matches} ) {
                    #$data{ $hostgroup }{ $host }{ 'hostStatus' }  = 'UNSCHEDULED DOWN';
                    $data{ $hostgroup }{ $host }{ 'hostStatus' }  = 'UNREACHABLE'; # kind of like it more - maybe config opt later on. 
                    $data{ $hostgroup }{ $host }{ 'hostMessage' } = "No Kibana searches matched prefix $search_prefix.";
                }
            }
            else {
                $data{ $hostgroup }{ $host }{ 'hostStatus' }  = 'UP';
                $data{ $hostgroup }{ $host }{ 'hostMessage' } = $matched_searches_count . " Kibana search" . ($matched_searches_count > 1 ? "es" : "") ." matched prefix $search_prefix";
            }

        }

    }

    # Build the global timestamped data structure.
    # This structure contains one an array that has just one entry.
    # That entry is the set of results calculated above , arranged into one giant data structure.
    @query_data = (   {   
                           "querytime" => time(),  
                           "rows"      => [ { %data }  ] 
                      }
    );

    ${$how_long_this_all_took_ref} = sprintf "%0.0f", (Time::HiRes::time() - $start_time) * 1000; # convert to ms - same as es

    if ( defined ${$error_ref} ) { 
        return 0;
    }
    else { 
        return 1; 
    }

}

# ---------------------------------------------------------------------------------
sub build_data
{
    # Description:
    # Takes one timestamped series from the query_data structure 
    #
    #       {   
    #           'querytime' => 1423491849,
    #           'rows' => [   
    #                        { the big structure from get_data() }
    #                     ]
    #       }
    # 
    # For services, it adds this :
    #       'rows_built_data_services' => [ 
    #                               { built service 1 key-values },
    #                               { built service 2 key-values },
    #                               ...
    #       ]
    #
    #       where each entry looks like this :
    #       {
    #                              'Host' => 'localhost', # hostname
    #                              'ServiceState' => 'OK', # service state 
    #                              'ServiceName' => 'service name' # the service name
    #                              'LastPluginOutput' => 'service message', # service message
    #       }
    #
    # For hosts, it adds this :
    #
    #       'rows_built_data_hosts' => [
    #                               { built host 1 key-values },
    #                               { built host 2 key-values },
    #                               ...
    #       ]
    #
    #       where each entry looks like this :
    #       {
    #                              'HostGroup' => { 'somegroup' => '' },  # hostgroups
    #                              'Host' => 'localhost', # hostname
    #                              'HostStatusGW' => 'UP', # GW state of host
    #                              'HostMessage' => 'some message on the host' # gw host message
    #       }
    #            
    # Args : 
    #    1. name of endpoint
    #    2. reference to one timestamped data structure as described above
    #

    # Returns :
    #   - Update-by-ref the timestamped data structure by adding the 'rows_built_data' key=>[ { }, { } ... ] structure
    #   - Always returns 1 for success.

    my ( $endpoint_name, $ref_data_to_build ) = @_;
    my ( $hostgroup, $host, $service, %built_host, %built_service );

    $logger->debug("Building data for endpoint $endpoint_name, query time $ref_data_to_build->{querytime}");
    # Iterate over the query data and "build" it ie prepare it for use with the Feeder.pm module and the REST API
    @{$ref_data_to_build->{rows_built_data_hosts}} = @{$ref_data_to_build->{rows_built_data_services}} = (); # reset the global built data structures

    # building the gotten data is just a matter of transposing stuff around
    # Build the built hosts data
    foreach $hostgroup ( sort keys %{ $ref_data_to_build->{rows}[0] } ) {
        foreach $host ( sort keys %{ $ref_data_to_build->{rows}[0]->{$hostgroup} } ) {
            %built_host = ();
            $built_host{'Host'} = $host;
            $built_host{'HostGroup'} = $hostgroup;
            $built_host{'HostStatusGW'} = $ref_data_to_build->{rows}[0]->{$hostgroup}->{$host}->{hostStatus};
            $built_host{'HostMessage'} = $ref_data_to_build->{rows}[0]->{$hostgroup}->{$host}->{hostMessage};
            push @{ $ref_data_to_build->{'rows_built_data_hosts'} }, { %built_host };
        } 
    }

    # Build the built services data
    foreach $hostgroup ( sort keys %{ $ref_data_to_build->{rows}[0] } ) {
        foreach $host ( sort keys %{ $ref_data_to_build->{rows}[0]->{$hostgroup} } ) {
            # some 'hosts' might not have services if no es searches matched the prefix for the host
            if ( exists $ref_data_to_build->{rows}[0]->{$hostgroup}->{$host}->{'services'} ) { 
                foreach $service ( sort keys %{ $ref_data_to_build->{rows}[0]->{$hostgroup}->{$host}->{'services'} } ) {
                    %built_service = ();
                    $built_service{'Host'} = $host;
                    $built_service{'ServiceName'} = $service; # just the name 
                    $built_service{'ServiceState'} = $ref_data_to_build->{rows}[0]->{$hostgroup}->{$host}->{'services'}->{$service}->{'monitorStatus'};
                    $built_service{'LastPluginOutput'} = $ref_data_to_build->{rows}[0]->{$hostgroup}->{$host}->{'services'}->{$service}->{'statusMessage'};
                    # Note on Service_Description :
                    #   - Underscored to differentiate it from earlier versions of code that used ServiceDescription which held the service name
                    #   - This is added here, but there doesn't seem to be a way to use this in the GW model yet - want this description to show up under
                    #     the service in status viewer ultimately.
                    $built_service{'Service_Description'} = $ref_data_to_build->{rows}[0]->{$hostgroup}->{$host}->{'services'}->{$service}->{'description'};
                    push @{ $ref_data_to_build->{'rows_built_data_services'} }, { %built_service };
                }
            }
        }
    }
    
    return 1;

}

# ---------------------------------------------------------------------------------
sub sync_endpoint
{
    # This routine processes a timestamped and built data set ie it sycn's the endpoint for this timestamped dataset.
    # This timestamped built dataset could be either from a retry cache entry, or be a fresh new set.
    #
    # The sync steps are :
    #   get GW states of built data hosts 
    #   sync GW hosts/services set with built hosts/services (if foundation_follows_conf is true)
    #   upsert foundation hosts
    #   upsert foundation hostgroups
    #   get GW service states, and add these states to the built services data structs
    #   upsert foundation services with built data services
    #   post events and notifications for state changes
    # 
    # Arguments:
    # - (Global) feeder object for this endpoint currently being sync'd
    # - cycle iteration from main loop in main() and used in update frequency mechanism
    # - endpoint name
    # - ref to a timestamped dataset data structure
    # - ref to a count of successfully built rows which only gets updated when the end of this sync op is reached (ie its not totally accurate)
    # - error ref
    #
    # Returns 1 on success, 0 otherwise, with error ref set

    my ( $cycle_iteration, $endpoint_name, $ref_timestamped_dataset, $ref_successfully_processed_built_rows, $error_ref ) = @_;
    my ( %hosts_states, $formatted_query_time, $error_message );

    # A time consumable by the GW REST API is required especially for processing retry cache entries and posting events
    $formatted_query_time = strftime( '%Y-%m-%dT%H:%M:%S%z', localtime( $ref_timestamped_dataset->{querytime} ) );

    $logger->debug("Syncing endpoint '$endpoint_name', query time $formatted_query_time ($ref_timestamped_dataset->{querytime})");

    my @built_hosts = @{$ref_timestamped_dataset->{rows_built_data_hosts}};
    my @built_services = @{$ref_timestamped_dataset->{rows_built_data_services}};

    # Get states for hosts as they are seen in the built data set, and as they are seen in Foundation.
    # This will be used later for notification and event logic.
    # This sets FoundationHostState's in %hosts_states for hosts that exist in Foundation.
    # If this procedure fails at the API level, or the built data  hosts don't exist in Foundation,
    # then FoundationHostStates won't get set, and it will seem as though these hosts don't exist in Foundation.
    if ( not get_hosts_states( \@built_hosts, \%hosts_states, \$error_message ) ) {
        ${$error_ref} = "Error getting host states - ending any further processing of LogBridge built data. $error_message" ;
        return 0;
    }


    # Check for hosts and/or services that have been removed from built hosts and services set and remove them from Foundation.
    # NOTE: This only applies to hosts/services that this feeder created (or whatever agentId ie guid is set to).
    # This is just the first part of sychronization of Foundation. The adding of things is done later.
    # If this fails, a partial or non removal will result.
    if ( $feeder->{foundation_follows_conf} ) { 
        if ( not remove_feeder_objects_from_foundation( $ref_timestamped_dataset->{'rows'}[0] , \$error_message)  ) { 
            ${$error_ref} = "Error in removing LogBridge objects from foundation - ending any further processing of LogBridge data. $error_message" ;
            return 0;
        }
    }

    # Create and/or update foundation hosts with their as-built states
    if ( not upsert_foundation_hosts( \%hosts_states, $formatted_query_time, \$error_message ) ) {
        ${$error_ref} = "An error occurred upserting hosts - ending any further processing of LogBridge built data. $error_message" ;
        return 0;
    }

    # Upsert foundation hostgroups with hosts memberships
    if ( not upsert_foundation_hostgroups( \@built_hosts, $ref_timestamped_dataset->{'rows'}[0], \$error_message ) ) {
        ${$error_ref} = "An error occurred upserting hostgroups - ending any further processing of LogBridge built data. $error_message" ;
        return 0;
    }

    # Get all of the current Foundation hosts' services states and set them directly back into each built service
    # Here %hosts_states is passed in because it provides a convenient unique list of hosts to work with.
    # If this fails at the API level:
    #   - some if not all built services will get FoundationServiceState property set to the service state
    #   - don't carry on but flag an error
    if ( not get_and_set_foundation_service_states( \@built_services, \%hosts_states, \$error_message ) ) {
        ${$error_ref} = "An error getting and setting Foundation service states - ending any further processing of LogBridge built data. $error_message";
        return 0;
    }

    # Upsert foundation services with the built services states.
    # If this fails, should still carry on because even if Foundation services failed to be upserted,
    # state information is still available for use in notification logic.
    if ( not upsert_foundation_services( \@built_services, $formatted_query_time, \$error_message ) ) {
        ${$error_ref} = "An error occurred upserting services - ending any further processing of LogBridge built data. $error_message";
        return 0;
    }

    # Check for state changes, posting events and notifications if necessary
    # If this fails, at least an error will have been logged.
    if ( not post_events_and_notifications( \@built_services, \%hosts_states, $formatted_query_time, \$error_message ) ) {
        ${$error_ref} = "An error occurred posting events and/or notifications - ending any further processing of LogBridge built data. $error_message";
        return 0;
    }

    return 1;
}


# ---------------------------------------------------------------------------------
sub post_events_and_notifications
{
    # Description:
    # Looks for state changes for hosts and services.
    # Posts notifications if state change detected and post_notifications is true.
    # Posts events if state change detected and options permit.
    #
    # Arguments :
    # - a ref to an array of built services
    # - a ref to a hash of host states 
    # - the timestamp of the dataset which the data is associated with
    # - error ref
    #
    # Returns 1 if ok, 0 otherwise + error

    my ( $built_services_arrayref, $ref_hash_of_host_states, $formatted_querytime, $error_ref ) = @_;

    my ( $built_service, $built_host, @host_notifications, @service_notifications, $notificationType, $noma_status );
    my ( @host_events, @service_events, $event_severity, $status );

    if ( not $feeder->{post_notifications} and not $feeder->{post_events} ) {
        $logger->debug("post_notifications and post_events are both disabled - no posting of events or notifications will be done");
        return 1;
    }

    # Build data for posting HOST state change events and noma notifications
    foreach $built_host ( keys %{$ref_hash_of_host_states} ) {
        # hosts not yet in foundation means FoundationHostState not set, and means don't send events or notifications
        # Update : actually do send events and notifications if it's not in foundation yet.  Set FoundationHostState to '' ie not undef, so it gets processed in the next block
        $ref_hash_of_host_states->{$built_host}{FoundationHostState} = '' if not defined $ref_hash_of_host_states->{$built_host}{FoundationHostState} ;

        if ( ( defined $ref_hash_of_host_states->{$built_host}{FoundationHostState}) and
            ( $ref_hash_of_host_states->{$built_host}{HostState} ne $ref_hash_of_host_states->{$built_host}{FoundationHostState} )  ) {
    
            # For events and notifications ....
            if ( $ref_hash_of_host_states->{$built_host}{HostState} ne 'UP' ) { # ie UNREACHABLE, or UNSCHEDULED DOWN
                $notificationType = "PROBLEM";
                $event_severity   = "SERIOUS";
            }
            else {
                $notificationType = "RECOVERY";
                $event_severity   = "OK";
            };
    
            # For notifications ...
            $noma_status = $ref_hash_of_host_states->{$built_host}{HostState};
            $noma_status =~ s/UNSCHEDULED//g; # NoMa will quietly ignore UNSCHEDULED DOWN - only accepts UP, DOWN, UNREACHABLE
            $noma_status =~ s/\s+//g; # NoMa will quietly ignore ' UP' - pretty dumb about whitespace
            push @host_notifications, {
                'hostName'         => $built_host,
                'hostState'        => $noma_status,
                'notificationType' => $notificationType,
                'hostOutput'       => "$built_host is $ref_hash_of_host_states->{$built_host}{HostState}",
            };
    
            # For host events ....
            push @host_events, {
                'host'            => $built_host,
                'device'          => $built_host,
                'monitorStatus'   => $ref_hash_of_host_states->{$built_host}{HostState},
                'appType'         => $feeder->{app_type},
                'severity'        => $event_severity,
                'textMessage'     => "$built_host is $ref_hash_of_host_states->{$built_host}{HostState}",
                'reportDate'      => $formatted_querytime,
                'firstInsertDate' => $formatted_querytime,
            }
        }
    }

    # Build data for posting SERVICE state change events and noma notifications
    foreach $built_service ( @{$built_services_arrayref} ) {
        # events for hosts not yet in foundation means FoundationServiceState not set, and means don't send events or notifications
        # Update :  actually do send events and notifications if it's not in foundation yet.  Set FoundationServiceState to '' ie not undef, so it gets processed in the next block
        $built_service->{FoundationServiceState} = '' if not defined $built_service->{FoundationServiceState} ;
        if ( ( defined $built_service->{FoundationServiceState} ) and ( $built_service->{ServiceState} ne $built_service->{FoundationServiceState} ) ) {

            # For notifications and events ...
            if ( $built_service->{ServiceState} ne 'OK' ) { # ie UNSCHEDULED CRITICAL, WARNING, UNKNOWN 
                $notificationType = "PROBLEM";
                $event_severity   = "SERIOUS";
            }
            else {
                $notificationType = "RECOVERY";
                $event_severity   = "OK";
            };

            # For service notifications ...
            $noma_status = $built_service->{ServiceState};
            $noma_status =~ s/UNSCHEDULED//g; # NoMa will quietly ignore UNSCHEDULED CRITICAL - only accepts OK, WARNING, CRITICAL and UNKNOWN
            $noma_status =~ s/\s+//g; # NoMa will quietly ignore ' CRITICAL' - pretty dumb about whitespace
            push @service_notifications, {
                'hostName'           => $built_service->{Host},
                'serviceDescription' => $built_service->{ServiceName},
                'serviceState'       => $noma_status,
                'notificationType'   => $notificationType,
                'serviceOutput'      => $built_service->{LastPluginOutput},
            };

            # For service events ...
            push @service_events, {
                'host'            => $built_service->{Host},
                'device'          => $built_service->{Host},
                'service'         => $built_service->{ServiceName},
                'monitorStatus'   => $built_service->{ServiceState},
                'appType'         => $feeder->{app_type},
                'severity'        => $event_severity,
                'textMessage'     => $built_service->{LastPluginOutput},
                'reportDate'      => $formatted_querytime,
                'firstInsertDate' => $formatted_querytime,
            }
        }
    }

    $status = 1; # assume all operations will be ok and disprove ... rename this var :)

    # Send notifications ...
    if ( $feeder->{post_notifications} ) {
        # HOST notifications
        if ( @host_notifications ) {
            $logger->debug( "Posting host notifications" );
            if ( not $feeder->feeder_post_notifications( 'host', \@host_notifications ) ) {
                #$feeder->report_feeder_error("NOTIFICATIONS ERROR creating host notifications.");
                ${$error_ref} .= " An error occurred creating host notifications.";
                $status = 0; # don't bail just yet - try and do as much as possible
            }
        }
        # SERVICE notifications
        if ( @service_notifications ) {
            $logger->debug( "Posting service notifications" );
            if ( not $feeder->feeder_post_notifications( 'service', \@service_notifications ) ) {
                #$feeder->report_feeder_error("NOTIFICATIONS ERROR creating service notifications.");
                ${$error_ref} .= " An error occurred creating service notifications.";
                $status = 0; # don't bail just yet - try and do as much as possible
            }
        }
    }

    # HOST events
    if ( $feeder->{post_events} and $feeder->{update_hosts_statuses} ) {
        # Only post host events if update_hosts_statuses is set. Otherwise, the sv host status graphs will reflect up/down states,
        # but the actual host status will not change when update_hosts_statuses = false
        if ( @host_events ) {
            $logger->debug( "Posting host events" );
            if ( not $feeder->feeder_post_events( 'host', \@host_events ) ) {
                #$feeder->report_feeder_error("EVENTS ERROR posting host events.");
                ${$error_ref} .= " An error occurred posting host events.";
                $status = 0; # don't bail just yet - try and do as much as possible
            }
        }
     }
    
    # SERVICE events
    if ( $feeder->{post_events} ) {
        if ( @service_events ) {
            $logger->debug( "Posting service events" );
            if ( not $feeder->feeder_post_events( 'service', \@service_events ) ) {
                #$feeder->report_feeder_error("EVENTS ERROR posting service events.");
                ${$error_ref} .= " An error occurred posting service events.";
                $status = 0; # don't bail just yet - try and do as much as possible
            }
        }
    }

    return $status;

}

# ---------------------------------------------------------------------------------
sub get_hosts_states
{
    # Takes a ref to an array of built hosts hashes, and a reference to a hash for host states.
    # %host_states is populated with two things : state of host in Foundation, state of host as built via build_data
    # Tries to be as fast and efficient as possible with the API calls.
    # If the API call fails, then the FoundationHostState's don't get set, which will make these appear to not
    # exist in Foundation later on in the code.
    # returns 1 if ok, 0 otherwise

    $logger->debug( "Getting host states");

    my ( $built_hosts_arrayref, $ref_hash_of_host_states, $error_ref ) = @_;
    my ( $built_host, %hosts, %outcome, %results, $host, @all_hosts, @hosts_bundle, $host_build_state );


    # Extract a unique hash of the host names out of the built hosts data and set their values to their built host states
    foreach $built_host ( @{ $built_hosts_arrayref } ) {

        # host can only be in one state. If subsequent built hosts set it again, we'll have the last one set
        if ( not defined $ref_hash_of_host_states->{ $built_host->{Host} }{HostState} ) {
            $ref_hash_of_host_states->{ $built_host->{Host} }{HostState} = $built_host->{HostStatusGW};
            $ref_hash_of_host_states->{ $built_host->{Host} }{HostMessage} = $built_host->{HostMessage};
        }
    
        # build hash of unique hostnames across all build hosts
        if ( not defined $hosts{ $built_host->{Host} }  ) {
            $hosts{ $built_host->{Host} } = 1;
        }
    }

    # Efficiently get batches of Foundation host states
    @all_hosts = keys %hosts;
    while ( @hosts_bundle = splice @all_hosts, 0, $feeder->{host_bundle_size} ) {
        $logger->debug("Getting host states for " . ($#hosts_bundle + 1) . " hosts");
        if ( not $feeder->{rest_api}->get_hosts( \@hosts_bundle, {}, \%outcome, \%results ) ) {
            if ( defined $outcome{response_code} and $outcome{response_code} ne '404' ) {
                #$feeder->report_feeder_error("ERROR getting host states : " . Dumper \%outcome, \%results); 
                ${$error_ref} .= "An error occurred getting host states : " . Dumper \%outcome, \%results ;
                return 0;
            }
        }
        foreach $host ( keys %hosts ) {
            if ( defined $results{$host}{monitorStatus} ) {
                $ref_hash_of_host_states->{$host}{FoundationHostState} = $results{$host}{monitorStatus};
            }
            else {
                # do nothing - the host does not yet exist in Foundation - this is ok
            }
        }
    }

    # If things failed, or the buit hosts don't exist in Foundation, then the FoundationHostState's won't be in the hash.
    # At this point, if things worked, then the hash referred to by $ref_hash_of_host_states will look like this, for eg:
    #
    # {
    #     'host1' => {
    #                        'HostState' => 'UNSCHEDULED DOWN', # ie host1's incoming state (translated to GW) is this
    #                         # NOTE  no FoundationHostState set if the host was not yet in Foundation!!!
    #                },
    #     'host2' => {
    #                        'HostState' => 'UNSCHEDULED DOWN', # ie host2's incoming state (translated to GW states) is this
    #                        'FoundationHostState' => 'UP' # ie host2's host state in Foundation is this
    #                },
    #      ...
    # }

    return 1;
}



# ---------------------------------------------------------------------------------
sub remove_feeder_objects_from_foundation
{
    # Summary 
    # - Removes feeder-created host/services from foundation that don't exist lb conf xml any more
    #
    # Detail :
    # - Gets a list of hosts and their services that are in Foundation that were created by this feeder.
    # - Compares that Foundation list with the incoming lb conf xml
    # - Removes anything in Foundation that was created by this feeder and is not in the lb conf xml
    #
    # Arguments :
    #   - reference to the build lb conf ie with services expanded out for tholds
    #   - error ref
    #
    # Returns 1 on ok, 0 otherwise+ error

    # Cases:
    #   no hosts created by this feeder found - just return immediately
    #   no hosts or services changed - nothing done
    #   host with services removed from conf - host removed
    #   host without services removed from conf - host removed
    #   services removed from conf - services removed

    my ( $lbconf, $error_ref ) = @_;

    my ( %outcome, %results );
    my ( @hosts_bundle, $foundation_host, %foundation_hosts_to_delete, %foundation_services_to_delete ,
         $foundation_service, %delete_hosts_options, %delete_services_options );

    # Get a list of all hosts from foundation that were created by this feeder.
    # There are cases where it's ok if there are no hosts that were created by this feeder:
    #   - fresh GW install, only default hosts
    #   - all hosts were created by some other feeder

    $logger->debug( "Getting all Foundation hosts that were created by this feeder");
    if ( not $feeder->{rest_api}->get_hosts( [ ] , { query => "agentId = '$feeder->{guid}'", depth => 'simple' }, \%outcome, \%results ) ) {
        # If no hosts created by this feeder are in foundation - there's nothing to do here
        $logger->debug("No hosts were found that were created by this feeder." );
       #return 1; # Don't return - need to cover case of NAGIOS host, with CACTI services added to it
    }
        
    # If the only host added by this feeder is the feeder health host, just return
    if ( scalar keys %results == 1 and exists $results{$feeder->{properties}->{health_hostname}} ) {
        $logger->debug("No hosts other than the feeder health host $feeder->{properties}->{health_hostname} were found that were created by this feeder. No removal necessary." );
        return 1;
    }

    # construct list of hosts and hosts services that are in the lb conf
    my ( %lbconf_hosts, %lbconf_services, $lbconf_hg, $lbconf_host );
    foreach $lbconf_hg ( keys %{ $lbconf } ) {
        foreach $lbconf_host ( keys %{ $lbconf->{$lbconf_hg} } ) { 
            $lbconf_hosts{$lbconf_host} = 1;
            if ( defined $lbconf->{$lbconf_hg}->{$lbconf_host}->{'services'} ) {
                foreach my $lbconf_service ( keys %{ $lbconf->{$lbconf_hg}->{$lbconf_host}->{'services'}  } ) {
                    $lbconf_services{$lbconf_host}{$lbconf_service} = 1;
                }
            }
        }
    } 


    # Make a list of hosts created by this agentid that exist in Foundation but not in the lb conf 
    foreach $foundation_host ( sort keys %results ) {
        if ( not exists $lbconf_hosts{ $foundation_host } ) {
           #next if ( $foundation_host eq $feeder->{properties}->{health_hostname} ); # skip deleting the feeder health host
            $logger->debug( "Marking Foundation host for delete : $foundation_host");
            $foundation_hosts_to_delete{$foundation_host} = 1;
        }
    }

    #die Dumper \%lbconf_hosts, \%results, \%foundation_hosts_to_delete;

    # Get a list of all services from foundation that were created by this feeder.
    # Format the results using the host->service format.
    # Allow for case of host existing but without any services attached.
    $logger->debug( "Getting all Foundation services that were created by this feeder");
    if ( not $feeder->{rest_api}->get_services( [ ], { query => "agentId = '$feeder->{guid}'", format=>'host,service' }, \%outcome, \%results ) ) {
        $logger->debug("No services were found that were created by this feeder.");
        #return 1; # allow for case of feeder host without any services on it
        %results = ();
    }


    # Get a list of all of the endpoint names from the master config. Used for special logic for feeder health services etc
    my %endpoint_hosts = map { (split ':',$_)[0] => 1 }  @{ $master_config->{endpoint} } ;

    # Make a list of services that exist in Foundation but not in lbconf
    FOUNDATIONHOST: foreach $foundation_host ( sort keys %results ) {
       #next if ( $foundation_host eq $feeder->{properties}->{health_hostname} ); # skip deleting the feeder health host services
        next if ( defined $foundation_hosts_to_delete{$foundation_host} ); # skip deleting services for a host if it host marked for delete as del host takes dels its svcs
        SERVICE: foreach $foundation_service ( keys %{ $results{$foundation_host} } ) {
            if ( exists $results{$foundation_host}{$foundation_service}{properties}{Notes} and $results{$foundation_host}{$foundation_service}{properties}{Notes} eq $GW::Feeder::metric_service_meta_tag ) {
                # Skip removal of this feeder metric but only if it's attached to a host that is in the endpoint host set
                if ( exists $endpoint_hosts{ $foundation_host } ) {
                    $logger->debug( "Skipping service $foundation_host : $foundation_service - its a metric service");
                    next SERVICE;
                }
            }
            if ( not exists $lbconf_services{ $foundation_host } { $foundation_service } ) {
                $logger->debug( "Marking Foundation service $foundation_host : $foundation_service for delete");
                $foundation_services_to_delete{$foundation_host}{$foundation_service} = 1;
            }
        }
    }

    # Special logic for cacti feeder health group contents...
    # Don't want to remove hosts that are in the endpoint list...
    foreach my $host_to_delete ( keys %foundation_hosts_to_delete ) {
        if ( exists $endpoint_hosts{ $host_to_delete } ) {
            $logger->debug("Removing host $host_to_delete from hosts removal list - its an endpoint host");
            delete $foundation_hosts_to_delete{ $host_to_delete } ;
        }
    }

    # Bundle-wise delete any hosts from Foundation that don't exist in lb conf set
    #print "HOSTS To DEL : " . Dumper \%foundation_hosts_to_delete; 
    %delete_hosts_options = ();
    if ( keys %foundation_hosts_to_delete ) {
        if ( not $feeder->feeder_delete_hosts( \%foundation_hosts_to_delete, \%delete_hosts_options ) ) {
            #$feeder->report_feeder_error("ERROR Couldn't delete hosts from Foundation.");
            ${$error_ref} = "Couldn't delete hosts from Foundation.";
            return 0;
        }
    }

    # Bundle-wise delete any services from foundation that don't exist in lb conf set
    # print "SVCS To DEL : " . Dumper \%foundation_services_to_delete; 
    %delete_services_options = ();
    if ( keys %foundation_services_to_delete ) {
        if ( not $feeder->feeder_delete_services( \%foundation_services_to_delete, \%delete_services_options ) ) {
            #$feeder->report_feeder_error("ERROR Couldn't delete services from Foundation.");
            ${$error_ref} = "Couldn't delete services from Foundation.";
            return 0;
        }
    }

    return 1;
}

# ---------------------------------------------------------------------------------
sub get_and_set_foundation_service_states
{
    # Takes a ref to an array of built services hashes, and a ref to a hash of host states (for a list of hosts)
    # and figures out Foundation states for the built services.
    # Results are stored back into each built service object in the array ref.
    # Returns 1 on success, 0 otherwise

    my ( $built_services_arrayref, $ref_hash_of_host_states, $error_ref ) = @_;
    my ( $built_service, @all_built_hosts, @hosts_bundle, $hbsize, %outcome, %results, $built_host, %built_hosts_and_services );

    # Efficiently get all services for a subset of hosts (ie with least # of api calls)
    # Note : there's currently no way to get services like this : {hostA, svc1}, {hostB, svc2} etc which would be ideal here.

    $logger->debug( "Getting and setting Foundation service states");

    # Create a simple hash of built hostnames and their sets of built services 
    # This is aimed at helping unpack potentially large nested loops.
    foreach $built_service ( @{ $built_services_arrayref } ) {
        $built_hosts_and_services{  $built_service->{Host} } { $built_service->{ ServiceName } } = undef;
    }

    @all_built_hosts = sort keys %{$ref_hash_of_host_states};
    while ( @hosts_bundle = splice @all_built_hosts, 0, $feeder->{host_bundle_size} ) {
        $logger->debug( "get_and_set_foundation_service_states() process bundle of " . ($#hosts_bundle + 1 ) . " host(s)" );
        if ( not $feeder->{rest_api}->get_services( [], { hostname => \@hosts_bundle, format => 'host,service' }, \%outcome, \%results ) ) {
            if ( defined $outcome{response_code} and $outcome{response_code} ne '404' ) {
                #$feeder->report_feeder_error( "ERROR Getting Foundation service states - no Foundation service states will be set for this bundle of hosts : @hosts_bundle" );
                ${$error_ref} = "Error getting Foundation service states - no Foundation service states will be set for this bundle of hosts : @hosts_bundle" ;
                return 0;
            }
            else {
                # else just carry on - its ok to get a 404 in the case of ALL services not existing yet
            }
        }
        foreach $built_host ( @hosts_bundle ) {
            # See if the built services for this host have a service status in the Foundation services list for this host
            foreach $built_service ( keys %{ $built_hosts_and_services{ $built_host } } ) {
                # If the built service for this built host showed up with a status in Foundation service results for this host, then
                # update the built hosts and services hash for this host/service with the Foundation service status
                if ( defined $results{$built_host}{$built_service}{monitorStatus} ) {
                    $built_hosts_and_services{ $built_host } { $built_service } = $results{$built_host}{$built_service}{monitorStatus};
                }
                    # Else the built service didn't show up in Foundation so it hasn't been added yet - and thats ok NOTE so far this has been ok 
            }
        }
    }

    
    # Go back through the original array of built events, inserting FoundationServiceState values
    foreach $built_service ( @{ $built_services_arrayref } ) {
        # If this built event has a defined value in the built hosts and events hash built above, then record that back into the built service hash itself
        if ( defined $built_hosts_and_services{ $built_service->{Host} } { $built_service->{ServiceName} } ) {
            $built_service->{FoundationServiceState} = $built_hosts_and_services{ $built_service->{Host} } { $built_service->{ServiceName} };
        }
        else {
            # service has not been added to Foundation yet and thats ok NOTE so far this has been ok
        }
    }

    return 1;
}

# ---------------------------------------------------------------------------------
sub upsert_foundation_hosts
{
    # Takes a ref to a hash of host names with expected ostState values and
    # creates the hosts or updates them (ie upserts them) in Foundation
    my ( $hosts_hashref, $formatted_querytime, $error_ref ) = @_;
    my ( @hosts_bundle, @built_hosts, $host, %host_options, @hosts, $error );

    %host_options = ();
    @built_hosts = keys %{$hosts_hashref};


    $logger->debug("Upserting hosts");

    if ( not @built_hosts ) {
        $logger->debug("No hosts needed processing.");
        return 1;
    }

    # Build an array of options that the Feeder rest api can consume
    # However, don't pass in description, properties, agentId, appType or anything else that
    # will overwrite things should the host already exist. Instead, let feeder_upsert_hosts add those if necessary.
    foreach $host ( @built_hosts ) {

        push @hosts,  {
                          # This should be the smallest set of properties required for updating an existing host
                          'hostName'       => $host,
                         #'description'    => $hosts_hashref->{$host}{HostMessage},
                          'properties' => {'LastPluginOutput'  => $hosts_hashref->{$host}{HostMessage} } , # Updates the Status text area in sv
                          'monitorStatus'  => $hosts_hashref->{$host}{HostState},
                          'lastCheckTime'  => $formatted_querytime, # this is needed to ensure events have correct time stamps => correct host state histograms in sv
                      };
    }


    # feeder_upsert_hosts does bundling
    if ( not $feeder->feeder_upsert_hosts( \@hosts, \%host_options, \$error ) ) {
        #$feeder->report_feeder_error("FOUNDATION HOSTS UPSERT ERROR could not upsert hosts" );
        ${$error_ref} = "Foundation hosts upsert error - could not upsert hosts" ;
        ${$error_ref} .= $error if defined $error;
        return 0;
    }

    return 1;
}

# ---------------------------------------------------------------------------------
sub upsert_foundation_hostgroups
{
    # Takes a ref to an array of built hosts hashes, and updates foundation hostgroup membership with these hosts.
    # Returns 1 on success, 0 otherwise

    my ( $built_hosts_arrayref, $lbconf_ref, $error_ref ) = @_;
    my ( %hostgroup_options, @hostgroups, $built_host, %hosts_and_groups, $hostgroup_member, @hosts, $hostgroup );

    %hostgroup_options = ();

    # Figure out from the built hosts which hostgroups have which hosts as members
    foreach $built_host ( @{$built_hosts_arrayref} ) {
        $hosts_and_groups{ $built_host->{'HostGroup'} }{$built_host->{'Host'}} = 1;
    }

    # Construct data for api call
    foreach $hostgroup ( keys %hosts_and_groups ) {
        # Get the members of each hostgroup
        @hosts = ();
        foreach $hostgroup_member ( keys %{$hosts_and_groups{$hostgroup}} ) {
            push @hosts, { "hostName" => $hostgroup_member };
        }
        # Build the required api fields, referencing the hosts array built beforehand
        # However, don't pass in anything that will overwrite things should the hostgroup already exist.
        # Instead, let feeder_upsert_hostgroups add those if necessary.
        push @hostgroups, {
            ## Just enough properties to update hostgroup membership
            "name"  => $hostgroup,
            "hosts" => [@hosts],           # use [ @hosts ] rather than \@hosts here
        };
    
        # Upsert the hostgroups, remember that feeder_upsert_hostgroup handles bundling
        # at the hostgroup level, but not at the hosts level ie you could have one hostgroup with 100000 hosts
        # and all 100000 will attempted to be added in one api call. TBD improve this later perhaps.
    }

    if ( not $feeder->feeder_upsert_hostgroups( \@hostgroups, \%hostgroup_options ) ) {
        #$feeder->report_feeder_error("FOUNDATION HOSTGROUPS UPSERT ERROR could not upsert hostgroups" );
        ${$error_ref} = "Foundation hostgroups upsert error -  could not upsert hostgroups" ;
        return 0;
    }

    return 1;
}

# ---------------------------------------------------------------------------------
sub upsert_foundation_services
{
    # Takes an ref to an array of built services and upserts them in Foundation
    # Returns 1 on success, 0 otherwise

    my ( $built_services_arrayref, $formatted_querytime, $error_ref ) = @_;
    my ( $built_service, @services, %service_options, $api_service );

    if ( not @{$built_services_arrayref} ) {
        $logger->debug("No built services needed processing.");
        return 1;
    }

    %service_options = ( );
    foreach $built_service ( @{$built_services_arrayref} ) {

        # Build the required api fields.
        # However, don't pass in anything that will overwrite things should the host:service already exist.
        # Instead, let feeder_upsert_services add those if necessary.

        # This is the minimum set of properties to achieve an update of the service
        $api_service = {
                    'description'          => $built_service->{ServiceName},     # the name of the service - confusingly called. 
                    'hostName'             => $built_service->{Host},            # the host name
                    'lastCheckTime'        => $formatted_querytime,              # this is needed to ensure events have correct time stamps => correct host state histograms in sv
                    'monitorStatus'        => $built_service->{ServiceState},    # the service status
                    'properties'           => { 'LastPluginOutput' => $built_service->{LastPluginOutput} }, # the service status message
        };

        # If there's a description for the service, shove that in too. This is done via the 'Notes' dyn prop
        if ( defined $built_service->{'Service_Description'} ) { 
            $api_service->{'properties'}->{ 'Notes' } = $built_service->{'Service_Description'}  ;
        }

        push @services, $api_service;

    }
    
    if ( not $feeder->feeder_upsert_services( \@services, \%service_options ) ) {
        #$feeder->report_feeder_error("Could not upsert services in Foundation" );
        ${$error_ref} = "Could not upsert services in Foundation" ;
        return 0;
    }

    return 1;
}

# ---------------------------------------------------------------------------------
sub update_feeder_stats
{
    # Logs feeder stats and updates services with metrics too
    #
    # Metrics 
    # Service name                  Message
    # cycle_elapsed_time            Cycle # total elapsed processing time : # seconds
    # esearches_run                 # / # successfully/unsuccessfully run elastic searches peformed
    # esearches_durations           Time taken to run esearches (reported by esearch) : #
    # removed :  esearches_run_elapsed         Time taken to run esearches (measured by us) : # 

    my ( $cycle_number,                      # the cycle iteration #
         $finished_time,                     # the time this call was called - useful to see, rather than just a cycle time
         $total_cycle_time_taken,            # total feeder cycle time
         $get_data_elapsed_time,             # get_data etime
         $successful_search_count,           # count of all successfully executed kibana esearches
         $unsuccessful_search_count,         # count of all unsuccessfully executed kibana esearches
         $total_took,                        # total esearch time across all processed searches
         $total_etime,                       # our own measurement of es searching across all processed searches
         $failed_services_arrayref,          # list of failed services - can be empty.
         $built_metrics_ref                  # reference to an array of built metrics services that will be populated by this routine.
      ) = @_;

    my ( $cycle_elapsed_time_msg, $esearches_run_msg, $esearches_durations_msg );
    my ( $cycle_elapsed_time_stat, $esearches_run_stat, $esearches_durations_stat );
    my ( @built_hosts, @built_services, %hosts_states, $formatted_query_time );

    $total_cycle_time_taken = '?' if not defined $total_cycle_time_taken;
    $successful_search_count = '?' if not defined $successful_search_count;
    $unsuccessful_search_count = '?' if not defined $unsuccessful_search_count;
    $total_took = '?' if not defined $total_took;
    $total_etime = '?' if not defined $total_etime;

    # Assume all ok - then disprove
    $cycle_elapsed_time_stat = $esearches_run_stat = $esearches_durations_stat = "OK";

    # Metric service : cycle_elapsed_time
    $cycle_elapsed_time_stat = "UNSCHEDULED CRITICAL" if ( $total_cycle_time_taken eq '?' );
    $cycle_elapsed_time_msg = "$cycle_elapsed_time_stat - Cycle $cycle_number finished at $finished_time, total cycle processing time : $total_cycle_time_taken ms, and get_data took $get_data_elapsed_time ms";
    # Metric service : esearches_run
    $esearches_run_stat = "WARNING" if ( $unsuccessful_search_count ne '?' and $unsuccessful_search_count > 0 ) ;
    $esearches_run_stat = "UNSCHEDULED CRITICAL" if ( $successful_search_count eq '?' or $unsuccessful_search_count eq '?' );
    $esearches_run_msg = "$esearches_run_stat - $successful_search_count / $unsuccessful_search_count successfully/unsuccessfully run elastic searches peformed. ";
    # v2.0.4 - in Perl 5.24, "defined() is not useful on arrays because it checks for an undefined scalar value. 
    # If you want to see if the array is empty, just use if (@array) { # not empty } for example." (http://perldoc.perl.org/perldiag.html)
    #if ( defined @{ $failed_services_arrayref } and scalar @{ $failed_services_arrayref } ) { 
    if ( @{ $failed_services_arrayref } and scalar @{ $failed_services_arrayref } ) {   
        $esearches_run_msg .= "Failed services : " . join ',', @{ $failed_services_arrayref };
    }

    # Metric service : esearches_durations
    $esearches_durations_stat = "UNSCHEDULED CRITICAL" if ( $total_took eq '?' or $total_etime eq '?' );
    $esearches_durations_msg  = "$esearches_durations_stat - Times taken for elasticsearch to execute all searches - total elapsed / total took : $total_etime / $total_took ms";

    $logger->debug("Updating feeder statistics");

    # Log metrics
    # TBD do warning or error if something here is not ok - low priority
    $logger->info( "$cycle_elapsed_time_msg") if defined $feeder->{cycle_timings};
    $logger->info( "$esearches_run_msg");
    $logger->info( "$esearches_durations_msg");

    @{ $built_metrics_ref } = (  
        {   # Required.
            service => $feeder_name . ".health",
            message => "Feeder host $GW::Feeder::feeder_host: ok", # ok descriptive enough lol !?
            status  => "OK" 
        },
        {
            service => "$feeder_name.cycle.elapsed.time",
            message => "Feeder host $GW::Feeder::feeder_host: $cycle_elapsed_time_msg",
            status  => $cycle_elapsed_time_stat,
            perfval  => { cycle_elapsed_time => $total_cycle_time_taken }
        },
        {
            service => "$feeder_name.esearches.run",
            message => "Feeder host $GW::Feeder::feeder_host: $esearches_run_msg",
            status  => $esearches_run_stat,
            # perfval  => {  }, # TBD when opentsdb working
        },
        {
            service => "$feeder_name.esearches.durations",
            message => "Feeder host $GW::Feeder::feeder_host: $esearches_durations_msg",
            status  => $esearches_durations_stat,
            # perfval  => {  }, # TBD when opentsdb working
        },
        {
            service => "$feeder_name.retry.caching",
            message => "Ok",
            status  => "OK" 
        },

    );

    return 1;
}
# ---------------------------------------------------------------------------------
sub initialize_options
{
    # Command line options processing and help.
    # TBD finish help string

    my $helpstring = "
Groundwork Log Bridge feeder - version $VERSION
GroundWork Feeder module version $GW::Feeder::VERSION

Description

    Overview

        The Log Bridge feeder integrates elasticsearch with GroundWork.
        Through a configuration file, searches saved in Kibana can be automatically
        associated with GroundWork hosts and services. The elasticsearch searches are
        automatically executed and document match counts are used to update the GroundWork
        service statuses.  See the Groups Configuration section for more details.

        Multiple GroundWork instances can be fed by one feeder instance.
        The term 'endpoint' here refers to a GroundWork REST API endpoint, ie GroundWork server.
        By default, the feeder is disabled. See configuration section for details.
        The feeder runs daemonized and is controlled via the GroundWork supervise subsystem.
        The log file is defined in /usr/local/groundwork/config/logbridge_feeder.log4perl.conf and
        is by default /usr/local/groundwork/foundation/container/logs/logbridge_feeder.log

    Algorithm description

        The feeder follows the usual pattern of : get data, build data, sync endpoint with data
        The building of the data is the preparation of it for consumption by API's that are use to
        update the GroundWork endpoints.

        The feeder follows this algorithm :

            - Getting the data set:
                - parse the groups configuration
                - calculate GroundWork hostnames and servicenames based on what is in the groups config
                  and which Kibana saved elasticsearch searches match prefixes 
                - execute the  elasticsearch queries and count results, and populate services with results
            - for all configured endpoints:
                - prepend retry cache contents to the data set, purging entries that are too old
                - build a dataset consumable by the endpoint, based on endpoint's configuration 
                - update the endpoint with the build data set
                - upon failures, rebuild the endpoint's retry cache

    Resiliency
        
        If an endpoint is unreachable, or fails during an operation, the dataset is placed into
        a retry cache for the endpoint. This cache is imported at the beginning of each processing
        cycle, prepending it to the latest thresholds data for processing.
        Retry caches are plain text files contain timestamped JSON objects that represent query data.
        These cache files directory is defined by the logbridge_feeder.conf retry_cache_directory setting.

    General Configuration

        The master configuration file is $master_config_file.
        This configuration file defines configuration common to processing all endpoints, 
        including the definition of endpoints themselves. To enable the feeder, 
        set feeder_enabled = yes.

        Endpoint configuration files, e.g. /usr/local/groundwork/config/logbridge_feeder_localhost.conf
        specify configuration specific to each endpoint. Each endpoint configuration file includes an
        ws_client_config_file option which points to a Groundwork web services configuration file, 
        inside which is the actual REST endpoint's details, held in the foundation_rest_url properties. 
        The default web services properties file is /usr/local/groundwork/ws_client.properties.

        The master configuration file is read every processing cycle i.e. it is not necessary to restart the 
        feeder after changing a setting in it.  This includes enabling/disabling the feeder. Endpoint 
        configuration file changes require a restart to the feeder though. 

    Groups Configuration

        The Groups Configuration file provides central control to the feeder.
        It maps collections of elasticsearch searches to GroundWork services arranged under GroundWork hosts.
        Its currently an xml file, but eventually the feeder will be configured via the portal.
        The Groups Configuration file pointed to by the groups_configuration setting in the 
        master configuration file. Upon installation, the groups config file is 
        /usr/local/groundwork/config/logbridge-groups.xml. 

        It follows this general structure :
        
            <log-bridge>
                <root-hg name='Name_Of_Hostgroup'>
                    <hosts>
                        <host name   = 'Host_Name'
                              prefix = 'Saved_Kibana_search_match_prefix'
                              desc   = 'Description'
                              thold_TimeRangeSpecifier = 'value' 
                              thod_...
                              thod_...
                         />
                        ...
                    </hosts>
                </root-hg>

                <root-hg name='Group 2'>
                    ...
                </root-hg>

                ...

            </log-bridge>

        For example :

            <log-bridge>
                <root-hg name='Compliance'>
                    <host name='HIPAA'       prefix='hipaa_'        desc='Searches related to HIPAA compliance searches'   thold_now-1h='10'  thold_now-1d='100' />
                    <host name='PCI'         prefix='pci_'          desc='Searches related to PCI compliance searches'                        thold_now-1d='200' />
                    <host name='Forensic'    prefix='forensic_'     desc='Searches related forensic searches'              thold_now-1h='30'  thold_now-1d='300' />
                    <host name='INFOSEC'     prefix='infosec_'      desc='Searches related to SECURITY information'                           thold_now-1d='100' />
                    <host name='Correlation' prefix='correlation_'  desc='Searches related to correlation searches'        thold_now-1h='50'  thold_now-1d='500' />
                    <host name='Others'      prefix='custom_'       desc='Searches not matching any pre-defined rule sets'                                       />
                </root-hg>
            </log-bridge>


        XML Elements Syntax Description
        
            Element : log-bridge
                Description : root tag.
                Required : yes          
                How many : exactly one
                Attributes : none.

            Element : root-hg
                Description : this will create a host group in GroundWork, into which hosts defined within will be attached
                Required : yes
                How many : at least one, and more than one is ok too
                Attributes : 
                    name  :
                        Required : yes
                        Description : the name of the GroundWork hostgroup 

            Element : host
                Description : 
                Required : yes
                How many : at least one, and more than one is ok too
                Attributes : 
                    name:
                        Required : yes
                        Description : the name of the GroundWork service 
                    prefix :
                        Required : yes
                        Description : the feeder uses saved Kibana searches beginning with this prefix value. The 
                                      prefix is stripped automatically from the calculated GroundWork service name.
                    desc :
                        Required : yes
                        Description : a meaningful description of this collection of searches, which will be used
                                      in the GroundWork service description.
                    thold_XXXXX :
                        Required : no
                        Description : Kibana doesn't save elasticsearch time filter ranges in it's query for obvious reasons.
                                      Each thold_ attribute defines a time range filter for the search, and results in a 
                                      GroundWork service being created. 
                                      The XXXXX piece is a valid elasticsearch time filter, such as now-5m, now-1h etc.
                                      Any number of thold_ attributes may be specified - each resulting in a service.
                                      The value of the attribute (ie the '10' in thold_now-1h = '10') defines a threshold.
                                      If the count of the documents from the search exceeds this threshold, the associated
                                      GroundWork service will be put into an UNSCHEDULED CRITICAL state. There are no
                                      warning states.
                                      If no thold_'s are defined, the search is done without time filtering.

    Full Example

        Scenario : 
          - groups config is as above
          - elasticsearch searches defined and saved in Kibana :
                hipaa_SecureRecordChanged
                pci_s1, pci_s2, pci_s3
                forensic_f1
                infosec_i1
                ( no correlation searches defined )
                custom_search1
                custom_search2

        GroundWork objects rendered in Status Viewer :

            LogBridge  (host group)
                HIPAA 
                    SecureRecordChanged_now-1h  : 5 (critical threshold is 10)
                    SecureRecordChanged_now-1d : 24 (critical threshold is 100)
                PCI
                    s1_now-1d : 10 (critical threshold is 200)
                    s2_now-1d : 9 (critical threshold is 200)
                    s3_now-1d : 15 (critical threshold is 200)
                Forensic
                    forensic_now-1h : 0 (critical threshold is 30)
                    forensic_now-1d : 2 (critical threshold is 300)
                INFOSEC
                    infosec_i1_now-1d : 245 (critical threshold is 100)
                Correlation (host status will say 'No Kibana searches matched prefix correlation_.)
                    ( no services since no searches defined )
                Others
                    search1 : 2443
                    search2 : 23

    Configuring for SSL

        Before configuring the feeder to use SSL, follow instructions from GroundWork on 
        configuring all GroundWork servers to use SSL.

        If you're using the feeder that comes with GroundWork, all locally on one system, 
        you do not need to change the feeder's configuration, i.e., 
        foundation_rest_url=http://localhost:8080/foundation-webapp/api.  If there is an second non 
        local SSL endpoint - host endpoint2 say - which the feeder is synchronizing, then set 
        foundation_rest_url=https://endpoint2/foundation-webapp/api .

Options
    -clean        - Remove objects created by this feeder
    -every <N>    - Run main cycle every N seconds, instead of system_indicator_check_frequency (used for testing)
    -help         - Show this help
    -once         - Run one main cycle and exit
    -version      - Show version and exit
    -yes          - Assume yes to -clean question

Author
    GroundWork 2016

";

    $SIG{__WARN__} = undef; # disable warnings to log4perl temporarily
    GetOptions(
        'clean'   => \$clean,
        'help'    => \$help,
        'every=i' => \$every,
        'once'    => \$once,
        'version' => \$show_version,
        'yes'     => \$yes,
    ) or die "$helpstring\n";

    if ( defined $help ) { print $helpstring; exit; }
    if ( defined $show_version ) { print "$0 version $VERSION, Feeder module version $GW::Feeder::VERSION\n"; exit; }
    if ( defined $every and $every <= 0 ) { print "Error - -every <N> : N should be positive non zero integer\n"; exit; }

    $SIG{__WARN__} = sub { $logger->warn( "WARN  @_" );  }; # revert- warnings to log4perl
}

# ---------------------------------------------------------------------------------
sub validate_feeder_specific_options
{
    # Logic for validation of feeder-specific options
    # returns 1 if ok, 0 otherwise

    # Required configuration properties are defined in feeder_specific_properties above
    # and are automatically checked for type when a new feeder object is created. All
    # that remains is any final value sanity checking.

    my ( $error_ref ) = @_;

    # Check host_bundle_size is sane
    if ( $feeder->{host_bundle_size} < 0 ) { 
        ${$error_ref} = "Error host_bundle_size should be a positive number - check the feeder configuration.";
        return 0;
    }

    # Check hostgroup_bundle_size is sane
    if ( $feeder->{hostgroup_bundle_size} < 0 ) { 
        ${$error_ref} = "Error hostgroup_bundle_size should be a positive number - check the feeder configuration.";
        return 0;
    }

    # Check service_bundle_size is sane
    if ( $feeder->{service_bundle_size} < 0 ) { 
        ${$error_ref} = "Error service_bundle_size should be a positive number - check the feeder configuration.";
        return 0;
    }

    # Check retry_cache_max_age is sane
    if ( $feeder->{retry_cache_max_age} < 0 ) { 
        ${$error_ref} = "Error retry_cache_max_age should be a positive number - check the feeder configuration.";
        return 0;
    }

    # Good enough for now.
    return 1;

}

# ---------------------------------------------------------------------------------
sub system_requirements_met
{
    # Checks for the following requirements :
    #  - elasticsearch version sufficient
    #  - lucense version sufficient
    # Takes ref to an es version var which it will populate, and an error ref
    # Returns 0 if any of these requirements are not met or couldn't perform the info check, 1 if all requirements are met
    # v 2.0.5 : version_ref is now populated with es version

    my ( $version_ref, $sys_error_ref ) = @_;
    my ( $info, $result_ref, $error_ref, $reqs_met ) = @_;

    $reqs_met =1 ; # assume and refute

    # try to get basic es info
    # Also, if there's an issue with es nodes, or misconfigured enodes/port, raise it here
    eval {
        $result_ref = $elastic_search_object->info();
    };
    if ( $@ ) { 
        chomp $@;
        #$logger->error("Error checking requirements : $@") ;
        ${$sys_error_ref} = "Error checking requirements : $@" ;
        return  es_parse_result( $@, $error_ref, { }  );
    }

    # If successful, $result_ref should look something close to this :
    #
    # {
    #   'cluster_name' => 'gw-elasticsearch',
    #   'name' => 'gw-logstash-02',
    #   'status' => 200,
    #   'tagline' => 'You Know, for Search',
    #   'version' => {
    #                  'build_hash' => '544816042d40151d3ce4ba4f95399d7860dc2e92',
    #                  'build_snapshot' => bless( do{\(my $o = 0)}, 'JSON::XS::Boolean' ),
    #                  'build_timestamp' => '2015-03-23T14:30:58Z',
    #                  'lucene_version' => '4.10.4',
    #                  'number' => '1.5.0' ### This is the version of elasticsearch
    #                }
    # }

    # check the version of elasticsearch meets at least the supported version
    if ( version->parse( $result_ref->{'version'}->{'number'}  ) < version->parse( $supported{'elasticsearch'}  ) ) {
        #$logger->error("Elasticsearch version $result_ref->{'version'}->{'number'} is not supported by this feeder - expecting version $supported{'elasticsearch'} or higher");
        ${$sys_error_ref} .= "Elasticsearch version $result_ref->{'version'}->{'number'} is not supported by this feeder - expecting version $supported{'elasticsearch'} or higher";
        $reqs_met = 0;
    }

    # check the version of lucene meets at least the supported version
    if ( version->parse( $result_ref->{'version'}->{'lucene_version'}  ) < version->parse( $supported{'lucene'}  ) ) {
        #$logger->error("Lucense version $result_ref->{'version'}->{'lucene_version'} is not supported by this feeder - expecting version $supported{'lucene'} or higher");
        ${$sys_error_ref} .= "Lucense version $result_ref->{'version'}->{'lucene_version'} is not supported by this feeder - expecting version $supported{'lucene'} or higher";
        $reqs_met = 0;
    }

    # 2.0.5 - return the es version for later use
    $$version_ref = $result_ref->{'version'}->{'number'};
    
    return $reqs_met ;
}

# ----------------------------------------------------------------------------
sub read_master_config
{
    # Takes a master feeder configuration file and returns by ref a hash of its parsed contents.
    # This routine is not put in Feeder.pm since it's logic might need to change depending on the feeder.
    # returns 1 on success, 0 otherwise

    my ( $master_config_file ) = @_;
   
    # v 2.0.6
    my %allowed_cxn_directives = ( 'nodes' => undef, 'port' => undef, 'use_https' => undef, 'userinfo' => undef ) ;

    eval {
        $master_config = TypedConfig->new( $master_config_file );
        # Force the reading of the expected contents to make TypedConfig do most of the validation for us
        # These variables are not used other than right here.
        my $feeder_enabled                   = $master_config->get_boolean( 'feeder_enabled' );
        my $system_indicator_check_frequency = $master_config->get_number(  'system_indicator_check_frequency' );
        my $endpoint_max_retries             = $master_config->get_number(  'endpoint_max_retries' );
        my $endpoint_retry_wait              = $master_config->get_number(  'endpoint_retry_wait' );
        my $retry_cache_directory            = $master_config->get_scalar(  'retry_cache_directory' );
        my $groups_configuration             = $master_config->get_scalar(  'groups_configuration' );
        my $elasticsearch_nodes              = $master_config->get_array(   'elasticsearch_nodes' );
        my $add_hosts_with_no_search_matches = $master_config->get_boolean( 'add_hosts_with_no_search_matches' );
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
    
    # Check that at least one elasticnode was specified and throw error if not. 
    # Otherwise, the new elasticsearch object will use a default of localhost:9200 which
    # could get confusing in the field potentially.
    
    #  v2.0.6 - refactored to pass though configuration of port, nodes etc via cxn block
    if ( not defined $master_config->{cxn} ) { 
       $logger->error( "A required <cxn> configuration block was not found. No elasticsearch nodes were defined." );
       $master_config = undef; 
       return 0;
    }
    # check at least one cxn nodes defined
    if ( not defined $master_config->{cxn}->{nodes} ) { 
        $logger->error( "No elasticsearch nodes were defined. At least one 'nodes' directive is required in the <cxn> block.");
        $master_config = undef; 
        return 0;
    }
    # check cxn port defined
    if ( not defined $master_config->{cxn}->{port} ) { 
        $logger->error( "No elasticsearch nodes port was defined. A 'port' directive is required in the <cxn> block.");
        $master_config = undef; 
        return 0;
    }

    # Also to at least restrict things a little bit, and not just have complete options pass through, limit allowed cxn options directives
    foreach my $cxn_directive ( keys %{$master_config->{cxn}} ) {
	if ( not exists $allowed_cxn_directives{$cxn_directive} ) { 
            $logger->error( "Disallowed or invalid cxn option '$cxn_directive'. Allowed directives are " . join ",", keys %allowed_cxn_directives );
            $master_config = undef; 
            return 0;
	}
    }

    return 1;
}


# -------------------------------------------------------------
sub initialize_elasticsearch
{
    # Wrap this in an eval and trap the error and report on it!
    undef $elastic_search_object if defined $elastic_search_object;  # want to force a new connection each time

    # v2.0.6 - refactored configuration to more flexibly support X-pack : ssl and authentication for example
    # https://metacpan.org/pod/Search::Elasticsearch::Cxn::HTTPTiny#CONFIGURATION 
    # https://metacpan.org/pod/Search::Elasticsearch::Role::Cxn#node
    
    my %es_opts;

    # pass through connection options if given
    if ( defined $master_config->{cxn} ) {
        foreach my $cxn_opt ( keys %{$master_config->{cxn}} ) {
	    $es_opts{$cxn_opt} = $master_config->{cxn}->{$cxn_opt};
	}
    }

    # pass through ssl_options if any defined
    if ( defined $master_config->{ssl_options} ) { 
        $es_opts{ssl_options} = $master_config->{ssl_options}
    }

    eval { 
        $elastic_search_object = Search::Elasticsearch->new( %es_opts );
    };

    if ( $@ ) { 
        chomp $@;
        $logger->error("Error initializing Search::Elasticsearch : $@");
        return 0;
    }

    $logger->trace( "Elasticsearch connection object: " . Dumper $elastic_search_object );

    return 1 ;
}
 
# ----------------------------------------------------------------------------
sub get_saved_kibana_searches
{
    # Does an equivalent of a GET 'http://localhost:9200/.kibana/_search?q=_type:search'.
    # Results stored into hash ref : searchname => searchSourceJSON data structure.
    #
    # args :
    #   ref to searches hash 
    #   error ref
    # returns
    #   1 if at least one kibana search found 
    #   0 if no searches found, or search failed for some reason
    #   (un)populated searches hash that has this structure 
    #   
    #      {
    #          <search name 1> => { 
    #                   'index' => <some index>,
    #                   'query' => { extracted saved query }
    #          },
    #          <search name 2> => { 
    #                   'index' => <some index>,
    #                   'query' => { extracted saved query }
    #          },
    #          ...
    #      }
    

    my ( $kibana_searches_hashref, $error_ref ) = @_ ; 
    my ( $es_result, $es_index, $es_error, $query, $decoded_json, $found_search, $size );

    $es_index = '.kibana';
    #$size = 999999; # for this search, it's unlikely that there will be so many saved kibana searches that this could become a big es perf issue ??
    $size = 9999; # For ES 2.2.1 to work properly. Version 2.0.1
    $query = {  query => {
                    match => {  _type => 'search', }
                }
             };
                        
    if ( not es_search_simple( $es_index, $query, $size, \$es_result, \$es_error ) ) { 
        #$logger->error( "Failed to get kibana searches!" ); # TBD do something with es_result and es_error ?
        ${$error_ref} = "No saved kibana searches were found or a failure retrieving them occurred." ; # the es result/ error could be giant strings, so just log them
        return 0;
    }

    # Example of search result :
    #
    #   {
    #     "hits": {
    #       "hits": [
    #         {
    #           "_source": {
    #             "kibanaSavedObjectMeta": {
    #               "searchSourceJSON": "{\"index\":\"groundwork-*\",\"highlight\":{\"pre_tags\":[\"@kibana-highlighted-field@\"],\"post_tags\":[\"@/kibana-highlighted-field@\"],\"fields\":{\"*\":{}}},\"filter\":[],\"query\":{\"query_string\":{\"query\":\"*\",\"analyze_wildcard\":true}}}"
    #             },
    #             "version": 1,
    #             "sort": [
    #               "@timestamp",
    #               "desc"
    #             ],
    #             "columns": [
    #               "_source"
    #             ],
    #             "hits": 0,
    #             "description": "",
    #             "title": "GroundworkDiscovery"
    #           },
    #           "_score": 1,
    #           "_id": "GroundworkDiscovery",
    #           "_type": "search",
    #           "_index": ".kibana"
    #         },
    #         ...
    #         etc etc more _sources in here 
    #         ...
    #       ],
    #       "max_score": 1,
    #       "total": 8
    #     },
    #     "_shards": {
    #       "failed": 0,
    #       "successful": 1,
    #       "total": 1
    #     },
    #     "timed_out": false,
    #     "took": 1
    #   }
    # 
    # The query piece that'll be used by this feeder is in _source -> kibanaSavedObjectMeta -> searchSourceJSON , in the query piece.
    # Expanding this so can see the query piece :
    #   {
    #     "filter": [],
    #     "highlight": {
    #       "fields": {
    #         "*": {}
    #       },
    #       "post_tags": [
    #         "@/kibana-highlighted-field@"
    #       ],
    #       "pre_tags": [
    #         "@kibana-highlighted-field@"
    #       ]
    #     },
    #     "query": {
    #       "query_string": {
    #         "query": "*",
    #         "analyze_wildcard": true
    #       }
    #     },
    #     "index": "groundwork-*"
    #   }

    foreach $found_search ( @{ $es_result->{hits}->{hits} } ) {

        # decode the JSON from the stored kibana search back into a perl structure that can be used later in api calls
        $decoded_json =  decode_json( $found_search->{_source}->{kibanaSavedObjectMeta}->{searchSourceJSON} );
        
        # build searchname->{index} and searchname->{query} elements
        $kibana_searches_hashref->{ $found_search->{_source}->{title} }{'index'} = $decoded_json->{'index'}; 
        $kibana_searches_hashref->{ $found_search->{_source}->{title} }{'query'} = $decoded_json->{'query'};
    }


    # finally validation - something should have been found. If there wasn't - error.
    if ( not scalar keys %{ $kibana_searches_hashref } ) { 
        #$logger->error( "Error - no elasticsearch saved searches were found" );
        ${$error_ref} = "No elasticsearch saved searches were found." ;
        return 0;
    }

    # TBD other validation ?
   
    return 1;
}

# ----------------------------------------------------------------------------
sub es_search_simple
{
    # A wrapper around a very simple es search structure that just has an index and a body.
    # Used for getting kibana saved searches at least.
    # args :
    #   index - index to search on
    #   body  - the search itself
    #   size  - the max # of results to return - not necessarily efficient but good for now
    #   result_ref - the results, if any,  will be returned by ref in here
    #   error_ref - the error, if one, will be returned by ref in here 
    # returns :
    #   the return value of es_parse_result
    #   a possibly populated error string by reference

    my ( $index, $body, $size, $result_ref, $error_ref ) = @_;
    
    $size = 10 if not defined $size; # force default size if none given

    # try to perform the search
    eval {
        ${$result_ref} = $elastic_search_object->search(
            size => $size,
            index => $index,
            body  => $body,
        );
    };

    # Parse the result, updating the error by reference if one happened.
    # The { index=> ...} anon hash is passed in just so es_parse_result can 
    # provide some useful basic information about the args that were in use.
    return es_parse_result( $@, $error_ref, { index=>$index, body=>$body }  );

}

# ----------------------------------------------------------------------------
sub es_search_preconstructed
{
    # A wrapper around a search structure that has been completed built already
    # args :
    #   search - search structure that is directly usable by Search::Elasticsearch->search()
    #   result_ref - the results, if any,  will be returned by ref in here
    #   error_ref - the error, if one, will be returned by ref in here 
    #   elapsed time ref - how long executing the search took including network etc (compare to es took)
    # returns :
    #   the return value of es_parse_result
    #   a possibly populated error string by reference

    my ( $search, $result_ref, $error_ref, $elapsed_time_ref ) = @_;
    my $start_time  = Time::HiRes::time();
    eval {
        ${$result_ref} = $elastic_search_object->search( $search ) ;
    };
    ${$elapsed_time_ref} = sprintf "%0.0f", (Time::HiRes::time() - $start_time) * 1000; # convert to ms - same as es

    # Parse the result, updating the error by reference if one happened.
    return es_parse_result( $@, $error_ref, { }  );

}

# ----------------------------------------------------------------------------
sub es_parse_result
{
    # A general es eval result parser
    # args : 
    #   error - the $@ from an elasticsearch object operation such as search, or get etc
    #   error_ref - the error string - simplified possibly by this routine
    #   terms_ref - an anonymous hash that contains stuff that was used in the search 
    #               and will be used in creating a simplified error message
    # returns :
    #   1 if error was not defined (ie es obj op was performed w/o error)
    #   0 if error was defined
    #   A parsed possibly simplified error message based on the $@ from the original eval
    # 
    # TBD 
    #   Add more exception cases.
   
    my ( $error, $error_ref, $terms_ref ) = @_;

    my $terms_message = "";
    my $handled = 1; # assume will be handled

    if ( $error ) { 
        chomp $error;
        # initialize the error message with caller info
        ${$error_ref} = whowasi(); 
        if ( defined $terms_ref and scalar keys %{$terms_ref} ) {  # may get terms_ref = {}, need to check it's size too
            $terms_message .= "\nTerms : ";
            foreach my $term ( keys %{$terms_ref} ) {
                $terms_message .= "$term : " . Dumper $terms_ref->{$term} ;
            }
        }
        
        if ( $error =~ /IndexMissingException/ ) { 
            ${$error_ref} .= "Index not found (IndexMissingException). "; 
        }
        elsif ( $error =~ /SearchPhaseExecutionException/ ) { 
            ${$error_ref} .= "Malformed search phrase (SearchPhaseExecutionException). ";
        }
        else {
            # Just return the unedited message if its not yet handled here
            ${$error_ref} .= "Unhandled exception. ";
            $handled = 0; # note not handled message
        }

        ${$error_ref} .= $terms_message;  
        ${$error_ref} .= "For more details, set debug level to TRACE for details, and look in logfile.";

        # If need more info, and exception message was not handled above, add on the detail
        # Maybe in future add a trunc'd version of the full error string ?
        #if ( $handled ) {
            # In tests, some of these errors can be gigantic so only dump them if TRACEing
            # Bad idea to include in service output - it risks putting giant crap piles into gwcollagedb servicestatus table
            #if ( Log::Log4perl::Level::isGreaterOrEqual( $logger->level(), $Log::Log4perl::TRACE ) ) {
            #    ${$error_ref} .= " Full message : '$error'" ;
            #}
        # }
    
        # trace out the full message to the log file
        $logger->trace("Full error message from search : '$error'");

        $logger->error( ${$error_ref} );
        return 0;
    }
    else {
        return 1;
    }
}

# ----------------------------------------------------------------------------
sub es_search_result_ok
{
    # This routine is intended to look at the es search result and error and see if 
    # there is something in either that indicates that the result, whilst exec'd ok, 
    # failed somehow.
    #
    # TBD  flesh this out more when have more info

    my ( $es_result_ref, $es_error_ref ) = @_;

    return 1;
}

# ----------------------------------------------------------------------------
# Couple of stack tracers for exception handling
sub whoami  { return (caller(1))[3] . "() : "; }
sub whowasi { return (caller(2))[3] . "() : "; }

# ----------------------------------------------------------------------------
sub parse_logbridge_groups_configuration
{
    # Parses the xml config file that defines hostgroups, hosts etc 
    # Since user can easily mess up the contents of this config, lots of checking will be done.
    # Args
    #   - reference to a scalar that will contain a reference to this parsed xml struct from XLMin
    # returns
    #   - populated nested hash data structure that contains the parsed xml data 
    #   - 1 on failure - malformed xml, invalid content, or file not found
    #   - 0 if ok

    my ( $logbridge_config_ref, $error_ref ) = @_;
    my ( $xml_object, $config, %required_props, %optional_props_regexes, @error_msg ) ;

    %required_props = ( 
                        'desc' => undef, 
                        'prefix' => undef,
                      );

    %optional_props_regexes = ( 
                                '^thold_.*$' => undef,
                              ) ;

    # need to use ForceArray to keep everything in a consistent datastructure - case is 
    # if you have only one host entry in a <hosts> block
    $xml_object = new XML::Simple ( ForceArray => 1 ); 

    # If the content doesn't adhere to XML standard, file read errors, etc, it will throw an error here
    eval {
        $config = $xml_object->XMLin( $master_config->{groups_configuration} ) ;
    };
    if ( $@ ) {
        chomp $@;
        ${$error_ref} .= "Error reading XML from $master_config->{groups_configuration} : Error: '$@'";
        $logger->error ( ${$error_ref} );
        return 0;
    }

    # ----------------------------------------
    # Validate the content

    @error_msg = ( ); # just for clarity

    # - check there's at least one root-hg (could be more)
    if ( not exists $config->{$xml_root_hg} ) { 
        $logger->error( "No $xml_root_hg element was found in $master_config->{groups_configuration}" ) ; 
        return 0;
    }

    # - check that there are only root-hg's at the top level
    foreach my $root_level_element ( keys %{ $config } ) { 
        if ( $root_level_element ne $xml_root_hg ) { 
            push @error_msg, "Expecting only $xml_root_hg element(s) - found '$root_level_element'";
        }
    }

    ROOT_HG : foreach my $root_hg ( keys %{ $config->{$xml_root_hg} } ) { 
        # - under each root hg, check there's a 'hosts' element
        if ( not exists $config->{$xml_root_hg}->{$root_hg}->{'hosts'} ) { 
            push @error_msg, "Missing <hosts> element in $xml_root_hg = $root_hg";
            next ROOT_HG;
        }
        
        # - under each hosts element, check there's at least one host
        if ( not exists $config->{$xml_root_hg}->{$root_hg}->{'hosts'}[0]->{'host'} ) { 
            push @error_msg, "Missing <host> element under <hosts> in $xml_root_hg '$root_hg'";
        }

        # - under each host, check properties
        HOST: foreach my $host ( keys %{ $config->{$xml_root_hg}->{$root_hg}->{'hosts'}[0]->{'host'}  } ) {

            # check existence of required properties
            foreach my $req_prop ( keys %required_props ) {
                if ( not exists $config->{$xml_root_hg}->{$root_hg}->{'hosts'}[0]->{'host'}->{$host}->{$req_prop} ) {
                    push @error_msg, "Host '$host' in group '$root_hg' has missing required '$req_prop' property";
                }
            }

            # look at the props on the host that are not in the required list
            foreach my $host_prop ( keys %{ $config->{$xml_root_hg}->{$root_hg}->{'hosts'}[0]->{'host'}->{$host}  } ) {
                next if exists $required_props{$host_prop}; # skip required ones
                # check the prop matches regex in optional list
                foreach my $opt_prop ( keys %optional_props_regexes ) { 
                    if ( $host_prop !~ /$opt_prop/ ) {
                        push @error_msg, "Host '$host' in group '$root_hg' has unrecognized property '$host_prop'";
                    }

                    my $prop_val = $config->{$xml_root_hg}->{$root_hg}->{'hosts'}[0]->{'host'}->{$host}->{$host_prop};
                    if ( $host_prop =~ /^thold_/ ) {
                        
                        # Validate value is of expected type
                        if ( not looks_like_number( $prop_val ) ) {
                            push @error_msg, "Threshold '$host_prop' for host '$host' in group '$root_hg' has non numeric threshold value '$prop_val'";
                        }
                    }
                }
            }

        }
    }

    if ( @error_msg ) {
        $logger->error( "Errors were found in $master_config->{groups_configuration} : " , join " ; ", @error_msg ) ;
        return 0;
    }

    ${$logbridge_config_ref} = $config;
    return 1;
}

# ----------------------------------------------------------------------------
sub construct_services_and_esqueries
{
    # Constructs a set of GW service objects with their associated es queries
    # The query construction is currently just taking kibana query found earlier, and 
    # adding a time range to it since that's not kept in kibana.
    #
    # args
    #   hostgroup - a single hostgroup for index into logbridge conf hash
    #   host - a single host name for index into logbridge conf hash
    #   matched searches array ref - a list of kibana es searches that matched this hostgroup/host prefix 
    #   logbridge_config_hashref - self explanatory
    #   kibana searches hashref - self explanatory
    #   constructed services array ref
    #   elasticsearch version (v 2.0.5 for use by construct_es_query() )
    # returns
    #   1 on success, 0 on failuire
    #   a populated constructed services array by ref. Objects in that array look like this :
    #
    #    [
    #       { <servicename> => { esquery = <es query>, desc = <desc>, ... }   },
    #       { <servicename> => { esquery = <es query>, desc = <desc>, ... }   },
    #       ...
    #    ]
    #               

    my ( $hostgroup, $host, $matched_searches_arrayref, $logbridge_config_hashref, $kibana_searches_hashref, $constructed_services_arrayref, $es_ver )  = @_;
    my ( %services, $matched_search, $thold, $service_name, $prefix, $critical_threshold_value ) ;

    # Note the prefix for this lb conf host
    $prefix = $logbridge_config_hashref->{$xml_root_hg}->{$hostgroup}->{'hosts'}[0]->{'host'}->{$host}->{'prefix'};

    # Services and their queries are constructed in differeny ways depending on how the lb conf is defined..

    # See if there are any thold_ params for this hostgroup->host in the lb conf and construct services accordingly
    my @matched_tholds = grep /^thold_/, keys %{  $logbridge_config_hashref->{$xml_root_hg}->{$hostgroup}->{'hosts'}[0]->{'host'}->{$host}  };


    # CASE : thold's are defined.
    # Service name : the kibana search name, with the lb conf prefix remove, and the time range from the thold suffixed
    # Elasticsearch query : a time range will be added based on the thold_<bit here>
    if ( @matched_tholds ) {
        foreach $thold ( @matched_tholds ) { 
            $critical_threshold_value = $logbridge_config_hashref->{$xml_root_hg}->{$hostgroup}->{'hosts'}[0]->{'host'}->{$host}->{$thold};
            $thold =~ s/^thold_//g;
            foreach $matched_search ( @{$matched_searches_arrayref} ) {
                $service_name = "${matched_search}_$thold";
                $service_name =~ s/^$prefix//; # strip off the prefix from the service name
                $services{$service_name}{'desc'} = $logbridge_config_hashref->{$xml_root_hg}->{$hostgroup}->{'hosts'}[0]->{'host'}->{$host}->{'desc'} . " - time range : $thold";
                $services{$service_name}{'critical_threshold'} = $critical_threshold_value;
                $services{$service_name}{'esquery'} = construct_es_query( $kibana_searches_hashref->{ $matched_search }, $thold, $es_ver );

            }
        }
        # add the built service to the results array
        push @{ $constructed_services_arrayref }, { %services };
    }

    # CASE : no tholds are defined
    if ( ! @matched_tholds ) {
        foreach $matched_search ( @{$matched_searches_arrayref} ) {
            $service_name = "${matched_search}";
            
            # v 2.0.5 If the prefix = the kibana search name, don't sub it out because that will result in an empty service name and REST API errors
            # just leave it alone if they are the same
            if ( $service_name ne $prefix ) {
                $service_name =~ s/^$prefix//; # strip off the prefix from the service name
            }
            
            $services{$service_name}{'desc'} = $logbridge_config_hashref->{$xml_root_hg}->{$hostgroup}->{'hosts'}[0]->{'host'}->{$host}->{'desc'};
            $services{$service_name}{'esquery'} = construct_es_query( $kibana_searches_hashref->{ $matched_search }, undef, $es_ver );
        }
        # add the built service to the results array
        push @{ $constructed_services_arrayref }, { %services };
    }

    # (more cases here as the lb conf evolves)

    return 1;
}

# ----------------------------------------------------------------------------
sub construct_es_query
{
    my ( $matched_search, $thold, $es_ver ) = @_;

    # V 2.0.5 changes made to facilitate elastic 5.x DSL changes
    # Takes a search found from Kibana, and an optional threshold from the lb conf, and an es version (as of v 2.0.5)
    # and tries to construct a datastructure that can be directly used by Search::Elasticsearch->search()
    # 
    # For now, all search types are of type count.
    # The matched search looks like this (so far, only seen index and query keys from kibana saved searches).
    #
    # {
    #   'index' => 'logstash-*',
    #   'query' => {
    #        'query_string' => { <the query string> } 
    #    }
    # }
    #
    # In Elastic 2.x : the query needs to look like this :
    # {
    #     index => "$es_index",
    #     search_type => 'count',
    #     body  => {
    #         query => { 
    #               query_string => { <the query string> }
    #         },
    #         filter => { 
    #                range => { '@timestamp' => { 'gte' => ... }   # <<< added here if a thold was defined
    #         }
    #     }
    # }
    #
    # In Elastic 5.x : it changes and needs to look like this :
    # {
    #     index => "$es_index",
    #     size => 0, # <<< search_type => 'count' goes away in 5.x, replaced with size:0
    #     body => {
    #         query => { 
    #            bool => {
    #                must => {
    #                   query_string => { <the query string> }
    #                },
    #                filter => {    
    #                      range => { '@timestamp' => { 'gte' => ... }   # <<< added here if a thold was defined
    #                }
    #            }
    #        }
    #     }
    #
    # args
    #   a reference to a kibana search 
    #   a thold (possibly undef)
    # returns
    #   a ref to a built query
    #
    # Notes
    #   this routine doesn't return a ref via an incoming ref var, but just directly
    #   it differes to other routines in that it doesnt return 0 or 1 because :
    #       - it makes much clearer calling code
    #       - if the constructed query fails later, that will be caught and raised
    #       - building here doesn't really go 'wrong', even if the content is wrong 

    my %query = ();

    # build the search index - this comes from the matched search 
    $query{'index'} = $matched_search->{'index'};

    # This is for ES 2.x ...
    if ( $es_ver =~ /^2/ ) {
       # build the search type - for now it's always just count
       $query{'search_type'} = 'count'; # deprecated in 2.0 - now use size:0

       # build the body
       # First need the query
       $query{'body'}{'query'} = $matched_search->{'query'};
       
       #If there's a thold key, use that for the time filter range
       if ( defined $thold ) {
           $query{'body'}{'filter'} = { 'range' => { '@timestamp' => { 'gte' => $thold }  } } ;
       }
    }

    # For ES 5.x ...
    if ( $es_ver =~ /^5/ ) { 
        $query{'size'} = 0; # deprecated in 2.0 - now use size:0
        $query{'body'}{'query'}{'bool'}{'must'} = $matched_search->{'query'};
        if ( defined $thold ) {
            $query{'body'}{'query'}{'bool'}{'filter'}{'range'} = { '@timestamp' => { 'gte' => $thold }  };
        }
    }
  
    return \%query; 

}


# ----------------------------------------------------------------------------
sub calculate_cycle_metrics
{
    my ( $dataset_ref , $total_took_time_ref, $total_elapsed_time_ref, $successfully_execd_es_searches_count_ref, $unsuccessfully_execd_es_searches_count_ref, $failed_servicenames_arrayref ) = @_;
    # dataset ref - ref to built and executed set of esearches in GW format

    my ( $hostgroup, $host, $service ) ;

    ${$total_took_time_ref} = undef; # not zero - but undef. If no searches exec'd => no took's => pass back undef for reporting later
    ${$successfully_execd_es_searches_count_ref} = ${$unsuccessfully_execd_es_searches_count_ref} = 0; # assume all zero for now

    HOSTGROUP: foreach $hostgroup ( keys %{ $dataset_ref } ) {  
        HOST: foreach $host ( keys %{ $dataset_ref->{$hostgroup}  } ) { 

            if ( exists  $dataset_ref->{$hostgroup}->{$host}->{'services'} ) { # if the host has services, add up some stuff

                # Sum up total took and elapsed times
                SERVICE: foreach $service ( keys %{ $dataset_ref->{$hostgroup}->{$host}->{'services'}  } ) { 
                    # sum up elapsed's - should always have this
                    ${$total_elapsed_time_ref} += $dataset_ref->{$hostgroup}->{$host}->{'services'}->{$service}->{'elapsed'};
                    # sum up took's - won't have this if es search failed to exec
                    if ( exists $dataset_ref->{$hostgroup}->{$host}->{'services'}->{$service}->{'took'} ) {
                        ${$total_took_time_ref} += $dataset_ref->{$hostgroup}->{$host}->{'services'}->{$service}->{'took'};
                        # Presence of a took key => successfully executed e-search
                        ${$successfully_execd_es_searches_count_ref} ++;
                    }
                    else {
                        # No took key => UNsuccessfully executed e-search
                        push @{ $failed_servicenames_arrayref }, "$hostgroup:$host:$service";
                        ${$unsuccessfully_execd_es_searches_count_ref} ++;
                    }
                }
            }
        }
    }


}

# -------------------------------------------------------------
sub terminate_rest_api
{
    ## Release our handle to the REST API (if we used it), to force the REST API to call its destructor.
    ## This will attempt to log out before Perl's global destruction pass wipes out resources needed for
    ## logout to work properly.
    #$feeder->{rest_api} = undef;
    foreach my $feeder_object ( keys %feeder_objects ) {
        $feeder_objects{$feeder_object}->{rest_api} = undef;
    }
}

__END__


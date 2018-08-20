#!/usr/local/groundwork/perl/bin/perl
# Cacti feeder - integrates Cacti with GroundWork
#
# Copyright 2013-2015 GroundWork OpenSource
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
# 2014-04-14 DN - v0.2.0 - Initial Version
# 2014-04-14 DN - v0.2.1 - Various JIRA's - see check-in comments for this version
# 2014-05-12 DN - v0.2.3 - Various JIRA's - see check-in comments for this version
# 2014-05-12 DN - v0.2.4 - Fixed issue with filter_out_non_state_changed_cacti_hosts_and_services() where
#                            on state-change-only cycles, events weren't geting filtered correctly.
# 2014-05-14 DN - v0.2.5 - Minor cleanup and Feeder.pm version increase
# 2014-05-14 DN - v0.2.6 - version bump for fix to Feeder.pm
# 2014-05-15 DN - v0.2.7 - some comment cleanup, and version bump for Feeder.pm minor fix
# 2014-05-23 DN - v0.2.8 - support and minor refactoring for standalone mysql cacti instance put in place
# 2014-05-23 DN - v0.2.9 - Minor fix to post_events_and_notifications so that status graphs stay in sync with host status
# 2014-08-21 GH - v0.3.0 - Don't loop too quickly upon configuration failure.
# 2014-10-13 GH - v0.3.1 - Make enable_processing completely control what it's supposed to if disabled.
# 2015-02-04 DN - v2.0.1 - Large refactoring to provide multi-endpoint and retry caching functionality, and many other 
#                          improvements and changes.
# 2015-03-05 DN - v2.0.2 - Allow -help option to work if another feeder is running; -help output worked on
# 2015-03-06 DN - v2.0.3 - Don't logexit on master config read or retry cache prep errors
# 2015-05-20 DN - v2.0.4 - Added cleanup ( including moving cleanup() to Feeder.pm )
# 2015-06-08 DN - v2.0.5 - Making this version work with GWME 702 - most of the changes are in Feeder.pm
# 2015-06-29 DN - v2.0.6 - Updated update_feeder_stats() and added -yes option and call to cleanup() to support multi endpoints, and added END { }
# 2015-06-30 DN - v2.0.7 - health virtual service now has info about which host feeder its running on - useful for multi endpoint feeder scenarios
# 2015-07-20 DN - v2.0.8 - minor logging update; fixed typo in error logging in close_database_connection()
# 2015-09-29 DN - v2.0.9 - minor change to generate_query() : latest version of cacti updated from 088c to 088f - this is for standalone cacti systems
# 2015-10-22 DN - v2.1.0 - lc of hostname
# 2015-10-22 DN - v2.1.1 - refactored way new feeder objects get created to aviod issue with GW not releasing auth tokens in a timely fashion
# 2015-11-19 DN - v2.1.2 - GWMON-12363 : sending metrics to all endpoints; health_hostname in endpoint config is replaced 
#                                        by endpoint name from main config; cache file is truncated based on threshold defined in main config
#                                        and other mods that bring error reporting up into the metrics stream to make them visible in status viewer.
# 2016-02-19 DN - v2.1.3 - logging config brought into cacti_feeder.conf
# 2016-02-22 DN - v2.1.4 - <feeder_services> brought out of endpoint config into here and services common across feeders now prepende with $feeder_name
# 2016-03-04 DN - v2.1.5 - added sync_hostgroups  ( but not fully working - slated for another release date )
# 2016-04-13 DN - v2.1.6 - removed lc of cacti device name ( see 2.1.0 above for when that was added ). 7.1.0 : mixed case is ok now.
# 2016-05-17 DN - v2.1.7 - fixed discrepancy between %supported_cacti_versions 0.8.8c (changed to 0.8.8b) and generate_query() supported logic, and updated
#                          to work with 0.8.8h standalone.
# 2016-09-06 DN - v2.1.8 - Only read the master config file if the last modified time has changed.
# 2017-02-06 DN - v2.1.9 - GWMON-12879 Cacti feeder does not respect threshold qualification options (check_thold_fail_count and check_thold_fail_count)
# 2017-08-08 DN - v2.2.0 - added perf data for cacti feeder threshold services
#
# NOTE - Update $VERSION below when changing the version # here.
#
# VIM : set tabstop=4  set expandtab - please use just these settings and no others
#
# KNOWN ISSUES/QUESTIONS
# - if feeder adds a hostgroup, then eventually that hostgroup has no services created by this feeder, should that hg be removed ?
#     - eg: default_hostgroups has cactigroup and nagiosgroup entries, where nagiosgroup is an hg created by NAGIOS
#           Feeder detects a threshold on host localhost and adds it to localhost host in GW, and creates cactigroup
#           Later, feeder detects threshold was removed. Then deletes it from localhost in GW. cactigroup remains.
# - Feeder.pm : If a host or service in Foundation has a ':' (colon) in it, audit trail will probably fail - see 'TBD API FIX'
#
# TBD
# - Test against 0.8.8h standalone 
# - As with any software, there is always a lot TBD. That complete list is not documented here yet.
# - fig out how to send -v output to screen not log
# - documentation of this feeder and Feeder.pm, including tests file format and examples
# - more validation of expected data structure/properties throughout
# - emulate find cacti graphs
        
use 5.0;
use warnings;
use strict;

use version;
my  $VERSION = qv('2.2.0'); # keep this up to date
#use GW::RAPID; qv('0.7.2'); # v2.0.5 Just remove this line - let Feeder do the import
use GW::Feeder qv('0.5.8');
use TypedConfig qw(); # leave qw() on to address minor bug in TypedConfig.pm
use Data::Dumper; $Data::Dumper::Indent = 2; $Data::Dumper::Sortkeys = 1;
use Log::Log4perl qw(get_logger);
use File::Basename;
use JSON;
use DBI;
use Getopt::Long;
use Time::HiRes;
use Time::Local;
use POSIX qw(strftime);
use Sys::Hostname; 
use Array::Compare;

our $feeder_name = "cacti_feeder"; # Feeder name - various things key off this such as logging and retry caching
our $master_config; # want to be able to access some of the endpoint-independent config from the Feeder module too such as retry_cache_limits
my $master_config_file = '/usr/local/groundwork/config/cacti_feeder.conf'; # Config file for this feeder
our ( $logger, $log4perl_config, $logfile );
my ( $clean, $every, $help, $show_version, $once, $yes) = undef; # CLI option vars
my ( $database_handle, $feeder, $tests_config, @query_data, $cacti_version ) = undef; # Various globals
my %feeder_objects ;

# Supported versions of cacti
my %supported_cacti_versions = ( '0.8.8h' => undef, '0.8.8f' => undef, '0.8.7g' => undef, '0.8.8b' => undef );

# feeder services
my %feeder_services = ( 
    "$feeder_name.cycle.elapsed.time"                => "Time taken to process last cycle",
    "$feeder_name.retry.caching"                     => "Retry cache info and errors etc",
    "$feeder_name.cycle.processed.built.thresholds"  => "Total processed built thresholds"
);

# This is used in Feeder.pm for testing with fmee
our $fmee_timestamp=-1; 

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
    sleep(5) if not defined $once;
}

# ---------------------------------------------------------------------------------
sub main
{
    # main sub that does initialiation, processing loop etc

    my ( $start_time, $cycle_count, $total_cycle_time_taken, $events_retrieved, $events_sent, $retrieve_time, $send_time );
    my ( $total_events, $total_events_processed, %feeder_options, %master_config, $endpoint, $endpoint_name, $endpoint_config );
    my ( @endpoint_data, $retry_cache_filename, $timestamped_dataset, @built_timestamped_data, %perfdata  );
    my ( $max_retries, $try, $disabled_notice_given, $started_message );
    my ( $sync_status, $total_built_thresholds_count, $successfully_processed_built_rows, $total_successfully_processed_built_rows, $total_queried_threshold_rows ) ; 
    my ( %metrics, $error_message, $aged_out_count, $count_of_data_rows_cached, %retry_cache_size, $retry_cache_size_message, $cache_file_truncated_message );
    my ( $endpoint_health_hostgroup, $error, $build_error, $sync_error, $cache_imported_rows_count , $endpoint_enabled );
    my $started_at = localtime;

    # v2.1.8 : This will be used to control re-reads of master config file in the main CYCLE loop. Seeded with 0 to force the first read.
    my $master_config_last_mod_time = 0;

    # read and process cli opts
    initialize_options();

    # get logger details
    if ( not initialize_logger('started', $master_config_file ) ) {
        print "Cannnot initialize logger - quitting!\n";
        exit;
     }

    # Check for other cacti feeders running - there can only be one ... but allow -help option to run concurrently
    $logger->logexit("Another $feeder_name is running - quitting") if ( perl_script_process_count( basename($0) ) > 1 );

    # Log app starting
    if ( $once ) {
        $started_message = "Feeder $feeder_name running once on $GW::Feeder::feeder_host started at $started_at";
    }
    else {
        $started_message = "Feeder $feeder_name started on $GW::Feeder::feeder_host at $started_at";
    }
    $logger->info($started_message);

    # Get data, build it and sync the endpoint(s) in a never ending cycle
    $cycle_count = 1; $disabled_notice_given = 0;
    CYCLE: while ( 1  )
    {
        #$logger->info( ">>>>>>>> Starting cycle $cycle_count <<<<<<<<" ) ; #if $master_config->{feeder_enabled}; # remove else missing cycle 1 log entry
        $logger->info( ">>>>>>>> Starting cycle $cycle_count <<<<<<<<" ) if not $disabled_notice_given;

	# v2.1.8 Only (re)read the master config if the mast mod time changed
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

        # Initialization of interrupt handling needs to happen before the sleep/wait loop 
        # Set of options for new cacti feeder objects
        %feeder_options = (
            ## The log4perl logger
            logger => $logger,
            # Feeder specific options to retrieve and type-check.
            # Access standard or specific properties with $feeder->{properties}->{propertname}, e.g., $feeder->{properties}->{cycle_time}
            feeder_specific_properties => {
                always_send_full_updates               => 'boolean',
                cacti_system_test_tweaks_file          => 'scalar',
                constrain_to_hostgroups                => 'hash',
                default_hostgroups                     => 'hash',
                full_update_frequency                  => 'number',
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
    
        # Wait until there's an indication to proceed. In the case of the cacti feeder,
        # that just means wait for a file to show up in the filesystem.
        # If -every option was supplied, use that as the trigger to start the main cycle instead - useful for testing
        if ( not defined $every ) {
            while ( not check_system_indicator() ) {
                $logger->info("Waiting for $master_config->{system_indicator_check_frequency} seconds before checking for something to do...");
                sleep $master_config->{system_indicator_check_frequency};
            }
        }
        else {
            if ( $cycle_count != 1 ) { # start right away
                $logger->info("Waiting for $every seconds before running next cycle ...\n");
                sleep $every;
            }
        }

        # Populates the @query_data global data structure.
        # This step also cleanses the queried data if necessary.
        if ( not get_data() ) {
            $logger->error("A problem occurred getting the data for the feeder! Trying again in 30 seconds.");
            sleep 30;
            next CYCLE;
        }

        # Original location of %feeder_options and initialize_interrupt_handlers() call


        # Initialize the metrics data structure
        %metrics = ( );

        # Synchronize each endpoint, in the order they are specified in the master config file
        ENDPOINT: foreach $endpoint ( @{$master_config->{endpoint}}  ) {
        
            # Reset the summation of metrics related sums
            $total_built_thresholds_count = $total_successfully_processed_built_rows = $total_queried_threshold_rows = 0;

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

            # 2.1.1 - Removed this to make it persistent and let RAPID do re-auth's instead
            # Destroy feeder if defined - expecting this to invoke the $feeder->DESTROY() automagically and that this is the last reference in use.
            # undef $feeder if defined $feeder;

            # Create a retry cache filename for this hostname, feeder and endpoint combo
            $retry_cache_filename = endpoint_retry_cache_name( $GW::Feeder::feeder_host, $feeder_name, $endpoint_name, $master_config->{retry_cache_directory} ) ;

            # As part of 2.1.1 auth token cache update ...
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

            if ( $clean ) {
                $feeder->cleanup( $yes, 1 ) ;
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
                #$logger->error("An error occurred initializing feeder health objects - ending processing attempt for this endpoint.");
                $error_message = "Feeder host $GW::Feeder::feeder_host : An error occurred initializing feeder health objects - ending processing attempt for this endpoint.";
                stage_error_for_publishing_via_metrics($logger, \%metrics, $endpoint_name, $error_message, 'general_errors');
                if ( not endpoint_retry_cache_write( $endpoint_name, $retry_cache_filename, \@endpoint_data, $logger, "a", \$count_of_data_rows_cached, \%retry_cache_size, \$cache_file_truncated_message ) ) {
                    #$logger->error("Failed to update the retry cache!");
                    #stage_error_for_publishing_via_metrics($logger, \%metrics, $endpoint_name, "Failed to update the retry cache!", 'general_errors');
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

            # Try to read tests file each cycle so can test dynamically
            # This sets $tests_config. If it has a problem, it reports it and no tweaking will be done.
            read_cacti_system_test_tweaks_file();  

            # Process each time-stamped set of raw data for this endpoint (just 1 set if empty retry cache, >1 otherwise)
            $sync_status = 1; # start out optimistically ;)
            ENDPOINTDATASET: while ( $sync_status == 1 and $timestamped_dataset = shift @endpoint_data ) {

                $fmee_timestamp = $timestamped_dataset->{querytime}; # for fmee testing logic

                # Sum up how many rows were successfully queried for this dataset, for metrics reporting later
                $total_queried_threshold_rows += scalar @{$timestamped_dataset->{rows}};
                
                # Prepare the data for processing by building it for this timestamped dataset
                if ( not build_data( $endpoint_name, $timestamped_dataset, \%perfdata, \$build_error ) ) {
                    $error_message = "Feeder host $GW::Feeder::feeder_host : A data build error was encountered - endpoint will not be processed. $build_error";
                    #$feeder->report_feeder_error( $error_message );
                    #$metrics{endpoints}{$endpoint_name}{errors} .= "$error_message "; # note trailing space for clear concat/reading 
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
                $feeder->flush_audit ( );  # this will produce its own errors if necessary

                # Figure out what data should to be put back onto the retry cache
                #
                # First approach of putting failed built objects back into the cache 
                # This doesn't really work :
                # Putting only failed bits back into the cache meant that next time this routine was called, objects that were found in foundation
                # but not in the build cacti dataset would get removed - bad for various reasons.
                # So, for now, dropping back to putting everything back into the cache if one piece of the sync failed. The reasoning being
                # that if half way through sync'ing, one blip failed the entire set, at least everything up til that point was sync'd in foundation,
                # and so doing that sync up til that point again won't generate any new foundation events or make any changes.

                # Code from first approach
                # ---------------------------------------------------------------
                #my @indices_for_retry_cache = ();
                #foreach my $built_object ( @{$timestamped_dataset->{rows_built_data}} ) {
                #    if ( exists $built_object->{ProcessingErrorType} ) { 
                #        push @indices_for_retry_cache, $built_object->{RowId};
                #    }
                #}
                #
                ## Create a set of query data based on the retry indices calculated above
                #my @retries = ();
                #foreach my $row_index ( @indices_for_retry_cache ) {
                #    push @retries, $timestamped_dataset->{rows}[ $row_index ];
                #}
                #my @new = {  "querytime" => $timestamped_dataset->{querytime},
                #             "rows"      => [ @retries ]  };
                # Then endpoint_retry_cache_write( $endpoint_name, $retry_cache_filename, \@new, $logger, "w", \$count_of_data_rows_cached, \%retry_cache_size )
                # ---------------------------------------------------------------


                # Second approach 
                # ---------------------------------------------------------------
                # If there was a sync error, the put all of the timestamped data back into the retry cache.
                # Actually if there was NO error, remove this dataset from the endpoint data set, and,
                # if there is an error with the sync, just stop trying to process any more endpoint data
                # Then put back into the cache whatever is in the endpoint data.
                # This approach regards the entire set of endpoint data (ie all timestamped sets) as one long
                # set of time-ordered things to process. If there's a problem midway through processing this long list,
                # then we stop, flag an error, and put stuff back into the cache to retry later.

                if ( $sync_status != 1 ) {  
                    $error_message =   "Feeder host $GW::Feeder::feeder_host : An error in syncing data set for endpoint '$endpoint_name' occurred - no more processing will be done for this endpoint. $sync_error" ;
                    #$feeder->report_feeder_error( $error_message ) ;
                    #$metrics{endpoints}{$endpoint_name}{errors} .= "$error_message "; # Note trailing space
                    stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $error_message,  'general_errors');
                    # Let this then drop out in to the remaining code here 
                }
                # ---------------------------------------------------------------

                # Sum up the total # of built threshold rows that this endpoint successfully processed
                $total_built_thresholds_count += scalar @{$timestamped_dataset->{rows_built_data}};

                # Sum up the total # of successfully processed built threshold rows 
                $total_successfully_processed_built_rows += $successfully_processed_built_rows;

                # Remove the rows_built_data : a) if don't, it will end up in a retry cache which should only be { timestamp=>time, rows=> [ {}, ... ]  }, and, b) good practice
                # Only do this if $timestamped_data is still non empty.
                delete $timestamped_dataset->{rows_built_data} if exists $timestamped_dataset->{rows_built_data}; 

                # post perf data if any
                if ( not $feeder->feeder_send_perf_data( \%perfdata ) ) {
                     $error_message = "An error occurred in writing performance data for endpoint '$endpoint_name'";
                     stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $error_message, 'general_errors');
                }
                

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
                #$feeder->report_feeder_error( "$error_message" ); 
                #$metrics{endpoints}{$endpoint_name}{errors} .= "$error_message ";
                #stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $error_message, 'general_errors');
                stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $error_message, 'caching_errors');
            }
            # GWMON-12363 : make service 'retry_caching' go critical, status message  "Endpoint GW_Server can't be reached. Last message send: YYYY-MM-DD:HH:MM:SS, cached ### data rows"
            else {
         #      # This will update a service that contains error information about the caching occurring with a count of how many rows written
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

            # 2.1.2 : this moves - want to have all endpoint REST api objects created first
            # See Feeder.pm::send_metrics() for details of this data structure.
            # Update feeder stats services with :
            # - Total count of all built query data rows 
            # - Total count of successfully processed built query rows 
            $total_cycle_time_taken = sprintf "%0.2f", Time::HiRes::time() - $start_time;
            # NOTE For now, just build a data structure which will be used to send to all GW REST endpoints with.
            # This routine will now just build and create a set of metrics services for sending later.
            # TBD rename update_feeder_stats to build_feeder_metrics.
            update_feeder_stats( $cycle_count, 
                                 $total_cycle_time_taken,
                                 $total_built_thresholds_count, 
                                 $total_successfully_processed_built_rows, 
                                 $total_queried_threshold_rows, 
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
            reset_system_indicator(); # clear out the system ready indicator flag file
            exit; 
        }

        # Reset the system indicator that this feeder has done processing stuff.
        # If the reset fails, then the feeder will be stuck in a processing loop which means noogies
        reset_system_indicator();

        # Increment the cycle number
        $cycle_count++;


    }
    
}

# ---------------------------------------------------------------------------------
sub get_data
{
    # Description:
    # Runs a query against the cacti database, and puts the rows of data into a global data structure @query_data.
    # This structure is an array containing one hash as follows:
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
    #
    # Arguments:
    # - None.
    #
    # Returns : 1 on success, 0 and no data otherwise

    my ( $sql, $sth, $row, $host_name, $thold_alarm, $bl_alarm, @data, $total_rows, $processed_rows, $skipped_count );

    $logger->debug("Getting data from cacti database");

    # Establish a connection to the Cacti database
    if ( not initialize_database_connection() ) { 
        $logger->fatal("Failed to initialize database connection!");
        return 0;
    }

    # Check that the cacti system meets requirements
    if ( not system_requirements_met( \$cacti_version ) ) {
        $logger->fatal("Some system requirements were not met - no processing will be done.");
        return 0;
    }

    # Build a query for generating the dataset
    if ( not generate_query( \$sql, $cacti_version ) ) {
        $logger->fatal("Could not generate the main query - no processing will be done.");
        return 0;
    }

    # Prepare
    $logger->trace("get_data() : SQL = '$sql'" );
    $sth = $database_handle->prepare($sql);

    # Execute
    $logger->trace("Running sql query against database ...");
    $sth->execute() or do { $logger->fatal( "SQL ERROR failed to execute sql '$sql' : $@" ); return 0; };

    # Process : this loop builds an data structure out of the raw query data.
    @data = (); 
    $total_rows = $processed_rows = $skipped_count = 0; 
    QUERYROW: while ( $row = $sth->fetchrow_hashref() )  {

        $total_rows++; # keep count of all rows the query returns

        # Get the hostname for a given host id
        #$host_name = get_cacti_hostname( $row->{host_id} ); # The host name was already provided via the query so just use that
        $host_name = $row->{description}; 

        # If you delete hosts from within the cacti interface, you can end up with a row here that has no host name
        # It's a data consistency problem inherent in cacti: you can delete a host but the thold data is not cleared, 
        # leading to this condition, where the cacti data is inconsistant.
        # GWMON-11719 and GWMON-11861 - fixed here to only update the feeder log, rather than generate GW events
        if ( not $host_name ) { 
            $logger->trace("Cacti threshold '$row->{name} without a device name was selected in get_data() - skipping processing of this threshold." ); 
            $skipped_count++;
            next QUERYROW; 
        }
    
        # Build up the central data array (array preserves query row order).
        # Each row ref is a hash, and that will be stored in the array.
        push @data, $row;

        # count how many rows were actually processed
        $processed_rows++;
    }

    # Log a message about skipped query rows count
    $logger->warn("$skipped_count cacti threshold(s) without a host name were skipped in get_data().") if $skipped_count > 0;

    # Build the global data structure - a timestamped-once set of rows from this query
    @query_data = (   {   
                           "querytime" => time(),  
                           "rows"      => [ @data ] 
                      }
    );

    # Close connection to the database 
    close_database_connection(); # This sub will log an error if it cannot close cleanly

    # log some info about # of queried rows processed used
    if ( $processed_rows != $total_rows ) { 
        $logger->warn("Queried data contained $total_rows rows, only $processed_rows rows were usable.");
    }
    else {
        $logger->debug("Queried data contained $total_rows rows, all were usable.");
    }

    # got this far so probably successful
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
    #    3. error ref that'll be filled in here if necessary
    #
    # Returns :
    #   - Update-by-ref the timestamped data structure by adding the 'rows_built_data' key=>[ { }, { } ... ] structure
    #   - 1 on success, 0 and error ref set otherwise

    my ( $endpoint_name, $ref_data_to_build, $perfdata_ref, $error_ref ) = @_;
    my ( $host_name, $thold_alarm, $bl_alarm, $service_state, $message, $lastread_value,
         %cacti_event, $row, $host_status_GW, $rowid ) ;

    ${$error_ref} = undef;

    $logger->debug("Building data for endpoint $endpoint_name, query time $ref_data_to_build->{querytime}");

    # Iterate over the query data and "build" it.
    @{$ref_data_to_build->{rows_built_data}} = (); # reset the global built data structure
    $rowid = 0; # See $cacti_event{RowId} comments below
    DATAROW: foreach $row ( @{ $ref_data_to_build->{rows} } ) {

        # Create built data structure - even if fail later to populate it, still want one for each row for this time stamp
        %cacti_event = (); 

        # A unique id to tie together the built data with the queried data. Its useful to use this to keep track of which 
        # row of query data this built data object is for. Originally this was put in here for fine res retry caching approach 1.
        # Leaving it in here for future possible re-use.
        $cacti_event{RowId}              = $rowid;                           

        $cacti_event{CactiServiceState}  = undef;                            # cacti service state, translated into GW state
        $cacti_event{Device}             = undef;                            # device will be set to hostname
        $cacti_event{Host}               = undef;                            # set earlier, and is the cacti system hostname
        $cacti_event{HostStatusCacti}    = undef;                            # The raw cacti host state 
        $cacti_event{HostStatusGW}       = undef;                            # The GW mapped cacti host state 

        # 2.1.9 note TBD $feeder->{default_hostgroups} no longer applicable here - minor fix.
        $cacti_event{HostGroup}          = $feeder->{default_hostgroups};    # this is a hash with keys = hostgroups as defined in this endpoint's config <default_hostgroups> block

        $cacti_event{HostId}             = undef;                            # the cacti system host id
        $cacti_event{LastPluginOutput}   = undef;                            # service status message
        $cacti_event{ServiceDescription} = undef;                            # service name will be the interface name

   
        # Get the hostname for a given host id
        #$host_name = $row->{description}; 
        #$host_name = lc $row->{description};  # 2.1.0 - lowercase added
        $host_name = $row->{description};  # 2.1.6 - lowercase requirement removed now
  
        # Need to describe what is going on here or refactor it to make it obvious. 
        # This is first-version feeder code that was never cleaned up but is critical.
        #$thold_alarm = ( $row->{thold_enabled} eq 'on' ) && $row->{thold_alert} && ((! $feeder->{check_thold_fail_count} ) || ( $row->{thold_fail_count} >= $row->{thold_fail_trigger}) );
        $thold_alarm = ( $row->{thold_enabled} eq 'on' ) && $row->{thold_alert} && ((! $master_config->{check_thold_fail_count} ) || ( $row->{thold_fail_count} >= $row->{thold_fail_trigger}) ); # v2.1.9 
 
        # Depending on which version of cacti is in use, bl_enabled column may or may not exist
        if ( $cacti_version eq '0.8.7g' ) {
            #$bl_alarm = ( $row->{bl_enabled} eq 'on' ) && $row->{bl_alert} && ((! $feeder->{check_bl_fail_count} ) || ( $row->{bl_fail_count} >= $row->{bl_fail_trigger}) );
            $bl_alarm = ( $row->{bl_enabled} eq 'on' ) && $row->{bl_alert} && ((! $master_config->{check_bl_fail_count} ) || ( $row->{bl_fail_count} >= $row->{bl_fail_trigger}) ); # v2.1.9
        }
        elsif ( $cacti_version eq '0.8.8b' ) { # This is left in here for reference, but only latest is allowed
            $bl_alarm = 0; # baseline alarm mechanism different in 0.8.8b
        }
        elsif ( $cacti_version eq '0.8.8c' ) {
            $bl_alarm = 0; 
        }
        elsif ( $cacti_version eq '0.8.8f' or $cacti_version eq '0.8.8h' ) { 
            # In 088f, thold_data.thold_type = 1 means Baseline Derivation type.
            # Just not clear yet on what bl_alert values are for different states of this type of thold.
            # TBD this needs more field testing!
            $bl_alarm = ( $row->{thold_type} == 1 && $row->{bl_alert} > 0 ); 
        }
        else {
            # Should never get in here since get_data() should prevent this.
            $logger->error("Unrecognized version of cacti '$cacti_version' - skipping cacti data row: " . Dumper $row);
            next DATAROW;
        }

        # NOTE if change the possible service_state values here, change them in Feeder.pm->new() too
        if ( $thold_alarm || $bl_alarm ) {
            # Note from original cacti feeder - not sure if note about ScheduledDowntimeDepth is still applicable.
            # if ScheduledDowntimeDepth = 0, then Foundation translates CRITICAL to UNSCHEDULED CRITICAL.
            # In cacti there's currently no notion of scheduled downtime so use UNSCHEDULED CRITICAL, and ScheduledDowntimeDepth = 0
            $service_state = 'UNSCHEDULED CRITICAL';
        }
        else {
            $service_state = 'OK';
        }
     
        $message = 'Thresholds currently disabled'; # this message will become the service status message
     
        # Get the last value of the interface name
        if ( $row->{lastread} ) {
            $lastread_value = $row->{lastread}; 

            # Build the perf data for this threshold 
            $perfdata_ref->{ "$row->{name}" }{serverName} = $host_name;
            $perfdata_ref->{ "$row->{name}" }{serviceName} = "$row->{name}";
            $perfdata_ref->{ "$row->{name}" }{value} = $row->{lastread};
            # the label, ie the metric name, for cacti threshold services is in []s at the end of the row name
            $perfdata_ref->{ "$row->{name}"}{label} = $row->{name};
            $perfdata_ref->{ "$row->{name}"}{label} =~ s/^.*\[(.*)\]$/$1/g;

        }
        else {
            $lastread_value='n/a';
        }
     
        if ( $row->{thold_enabled} eq 'on' ) {
            $message = "Threshold status is $service_state, value=$lastread_value THOLD_HI=$row->{thold_hi} THOLD_LOW=$row->{thold_low}";
        }
 
        # This was commented out from very early versions of this code and left in for future reference / clues
        # if ( $row->{bl_enabled} eq 'on' ) {
        #     $message = "Baseline status is $service_state, value=$lastread_value BASELINE_UP=$row->{bl_pct_up} BASELINE_DOWN=$row->{bl_pct_down}";
        # }

        # Get the status of the cacti host
        if ( not get_cacti_host_state( $row->{status}, \$host_status_GW ) ) {
            #$feeder->report_feeder_error( "ERROR building GroundWork host status from cacti host status '$row->{status}'" ); 
            ${$error_ref} = "ERROR building GroundWork host status from cacti host status '$row->{status}'" ; 
            return 0;
        } 

        # Create a data structure that reduces the raw data into a GW feeder set
        $cacti_event{CactiServiceState}  = $service_state;                   # cacti service state, translated into GW state
        $cacti_event{Device}             = $host_name;                       # device will be set to hostname
        $cacti_event{Host}               = $host_name;                       # set earlier, and is the cacti system hostname
        $cacti_event{HostId}             = $row->{host_id};                  # the cacti system host id
        $cacti_event{HostStatusCacti}    = $row->{status};                   # The raw cacti host state 
        $cacti_event{HostStatusGW}       = $host_status_GW;                  # The GW mapped cacti host state 
        $cacti_event{LastPluginOutput}   = $message;                         # service status message
        $cacti_event{ServiceDescription} = $row->{name};                     # service name will be the cacti threshold name

        # Add the built stuff to the rows_built_data key in the time-stamped data structure
        push @{$ref_data_to_build->{rows_built_data}}, { %cacti_event };

        $rowid++;
    }
    
    return 1;

}

# ---------------------------------------------------------------------------------
sub sync_endpoint
{
    # Description:
    # This routine processes a timestamped and built data set ie it sycn's the endpoint for this timestamped dataset.
    # This timestamped built dataset could be either from a retry cache entry, or be a fresh new set.
    # The build data in this data set is referred to here as a 'cacti event'.
    #
    # A built cacti event starts off as a bunch of properties, such as HostId, Host, Device, ServiceDescription, CactiServiceState etc
    # ie whatever build_data() created.
    # 
    # The built data set is then used for :
    #   - removing cacti hosts/services from foundation (ie if a cacti device or threshold removed, then remove it from foundation)
    #   - adding cacti hosts to foundation
    #   - adding cacti hosts to hostgroups as defined in the endpoints config
    #   - adding cacti thresholded interfaces as a foundation services
    # Finally events and notifications are sent out for state changes.
    #
    # As the cacti event gets processed further in this routine, other properties might be addded to it eg FoundationServiceState.
    # Also the original complete set of cacti events might get filtered down possibly more than once depending on the endpoint conf options.
    # 
    # Arguments:
    # - (Global) feeder object for this endpoint currently being sync'd
    # - cycle iteration from main loop in main() and used in update frequency mechanism
    # - endpoint name
    # - ref to a timestamped dataset data structure
    # - ref to a count of successfully built rows which only gets updated when the end of this sync op is reached (ie its not totally accurate)
    # - ref to an error which will get set if error occurs here
    #
    # Returns 1 on success, 0 with populated error ref otherwise

    my ( $cycle_iteration, $endpoint_name, $ref_timestamped_dataset, $ref_successfully_processed_built_rows, $error_ref ) = @_;
    my ( @cacti_events, %hosts_states, $formatted_query_time, $error_message );

    ${$error_ref} = undef;

    # A time consumable by the GW REST API is required especially for processing retry cache entries and posting events
    $formatted_query_time = strftime( '%Y-%m-%dT%H:%M:%S%z', localtime( $ref_timestamped_dataset->{querytime} ) );

    $logger->debug("Syncing endpoint '$endpoint_name', query time $formatted_query_time ($ref_timestamped_dataset->{querytime})");

    # Note - careful not to do anything with the $ref_timestamped_dataset->{rows} data - thats the golden query data.  
    # Just work with the built data.
    @cacti_events = @{$ref_timestamped_dataset->{rows_built_data}};

    # If constraining hostsgroups are defined in the conf, then restrict the set of cacti events
    # to those whose hosts are members of the hostgroups in Foundation.
    # NOTE: Constraining at this point means any proceeding operations will only apply to this set of constrained events.
    if ( defined $feeder->{constrain_to_hostgroups} and scalar keys %{$feeder->{constrain_to_hostgroups}}  > 0 ) {
        if ( not constrain_to_hostgroups( \@cacti_events )  ) {
            #$feeder->report_feeder_error( "CONSTRAINING TO HOSTGROUPS ERROR occurred getting hostgroups for hosts - skipping all processing of cacti events" );
            ${$error_ref} = "Constraining to hostgroups error getting hostgroups for hosts - skipping all processing of cacti events" ;
            return 0;
        }
    }

    # Get states for hosts as they are seen in the Cacti events set, and as they are seen in Foundation.
    # This will be used later for notification and event logic.
    # This sets FoundationHostState's in %hosts_states for hosts that exist in Foundation.
    # If this procedure fails at the API level, or the cacti hosts don't exist in Foundation,
    # then FoundationHostStates won't get set, and it will seem as though these hosts don't exist in Foundation.
    if ( not get_hosts_states( \@cacti_events, \%hosts_states, \$error_message ) ) {
        #$feeder->report_feeder_error( "ERROR getting host states - ending any further processing of cacti event data" );
        ${$error_ref} = "Error getting host states - ending any further processing of cacti event data. $error_message" ; # this might be tmi for the status viewer tho
        return 0;
    }

    # If an existent and non empty cacti_system_test_tweaks_file has been read, go process it.
    # If processing the test file fails, testing will be skipped.
    if ( defined $tests_config and scalar keys %{$tests_config} ) {
        if ( not cacti_system_test_tweaks( \@cacti_events, \%hosts_states, \$error_message ) ) { 
            #$feeder->report_feeder_error( "ERROR in testing tweaks processing - ending any further processing of cacti event data" );
            ${$error_ref} = "Error in testing tweaks processing - ending any further processing of cacti event data. $error_message" ;
            return 0;
        }
    }

    # Check for cacti hosts and/or services that have been removed from Cacti and remove them from Foundation.
    # NOTE: This only applies to hosts/services that this feeder created (or whatever agentId ie guid is set to).
    # This is just the first part of sychronization of Foundation. The adding of things is done later.
    # If this fails, a partial or non removal will result.
    if ( not remove_cacti_objects_from_foundation( \@cacti_events, \%hosts_states, \$error_message )  ) { 
        #$feeder->report_feeder_error( "ERROR in removing cacti objects from foundation - ending any further processing of cacti event data" );
        ${$error_ref} = "Error in removing cacti objects from foundation - ending any further processing of cacti event data. $error_message" ;
        return 0;
    }

    # TBD for a future release other than 710 - see notes in routine
    if ( 0 ) { 
        if ( not sync_hostgroups( \$error_message ) ) {
            ${$error_ref} = "Error in updating cacti hostgroups - ending any further processing of cacti event data. $error_message" ;
            return 0;
        }
    }

    # Get all of the current Foundation hosts' services states and set them directly back into each cacti event object.
    # Here %hosts_states is passed in because it provides a convenient unique list of hosts to work with.
    # If this fails at the API level:
    #   - some if not all cacti events will get FoundationServiceState property set to the service state
    #   - don't carry on but flag an error
    if ( not get_and_set_foundation_service_states( \@cacti_events, \%hosts_states, \$error_message ) ) {
        #$feeder->report_feeder_error( "ERROR getting and setting Foundation service states on Cacti event data - ending any further processing of cacti event data" );
        ${$error_ref} = "Error getting and setting Foundation service states on Cacti event data - ending any further processing of cacti event data. $error_message" ;
        return 0;
    }

    # If necessary, reduce the set of cacti host and service events to only those which are having a state change.
    # Only do this filtering if a) always_send_full_updates = false, and b) we're on a full_update_frequency cycle, and c) its not the very first cycle
    if ( not $feeder->{always_send_full_updates} and ( $cycle_iteration % $feeder->{full_update_frequency} != 0 ) and ( $cycle_iteration != 1 )  ) {
        filter_out_non_state_changed_cacti_hosts_and_services( \@cacti_events, \%hosts_states );
    }

    # Create and/or update foundation hosts with their cacti hosts states
    if ( not upsert_foundation_hosts_with_cacti_host_states( \%hosts_states, $formatted_query_time, \$error_message ) ) {
        #$feeder->report_feeder_error( "ERROR occurred upserting hosts - ending any further processing of cacti events" );
        ${$error_ref} = "Error occurred upserting hosts - ending any further processing of cacti events. $error_message" ;
        return 0;
    }

    # Upsert foundation hostgroups with cacti hosts memberships
    if ( not upsert_foundation_hostgroups_with_cacti_hosts( \@cacti_events, \$error_message ) ) {
        #$feeder->report_feeder_error( "ERROR occurred upserting hostgroups - ending any further processing of cacti events" );
        ${$error_ref} = "Error occurred upserting hostgroups - ending any further processing of cacti events. $error_message" ;
        return 0;
    }

    # Upsert foundation hosts services with their cacti hosts services states
    # If this fails, should still carry on because even if Foundation services failed to be upserted,
    # state information is still available for use in notification logic.
    if ( not upsert_foundation_cacti_services( \@cacti_events, $formatted_query_time, \$error_message ) ) {
        #$feeder->report_feeder_error( "ERROR occurred upserting services - ending any further processing of cacti events" );
        ${$error_ref} =  "Error occurred upserting services - ending any further processing of cacti events. $error_message" ;
        return 0;
    }


    # Check for state changes, posting events and notifications if necessary
    # If this fails, at least an error will have been logged.
    if ( not post_events_and_notifications( \@cacti_events, \%hosts_states, $formatted_query_time, \$error_message ) ) {
        #$feeder->report_feeder_error( "ERROR occurred posting events and/or notifications - ending any further processing of cacti events" );
        ${$error_ref} = "Error occurred posting events and/or notifications - ending any further processing of cacti events. $error_message" ;
        return 0;
    }

    # Original thinking :
    # Update prior processing so that failures to process a cacti event results in it's built structure getting
    # tagged with 'ProcessingErrorType' => <some #>, 'ProcessingErrorMessage' => <some message>
    # Then, back in the calling logic, compile a list of RowId's for any built event that has a ProcessingErrorType key, 
    # and remove any dataset rows that don't have one of these RowIds - that will be the set that needs putting back into the retry cache.
    # Built objects that were filtered out via config options will mean they are not in @cacti_events any more, and didn't need 
    # sync'ing, and so don't need putting on the retry cache either.
    # 
    # Problem with this approach :
    # Putting only failed bits back into the cache meant that next time this routine was called, objects that were found in foundation
    # but not in the build cacti dataset would get removed - bad for various reasons.
    # So, for now, dropping back to putting everything back into the cache if one piece of the sync failed. The reasoning being
    # that if half way through sync'ing, one blip failed the entire set, at least everything up til that point was sync'd in foundation,
    # and so doing that sync up til that point again won't generate any new foundation events or make any changes.

    # If successfully reached this point, probably safe to say that the number of actually processed built cacti threshold rows 
    # is whatever is in the @cacti_events array, which would have been possibly constrained down
    $$ref_successfully_processed_built_rows = scalar @cacti_events;

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
    # - a ref to an array of cacti event hashes
    # - and a ref to a hash of host states (for a list of hosts)
    # - the timestamp of the dataset which the data is associated with
    # - ref to an error string that will be set here if necessary
    #
    # Returns 1 if ok, 0 with set error ref otherwise

    my ( $ref_array_of_cacti_events, $ref_hash_of_host_states, $formatted_querytime, $error_ref ) = @_;
    my ( $cacti_event, $cacti_host, @host_notifications, @service_notifications, $notificationType, $noma_status );
    my ( @host_events, @service_events, $event_severity, $status );

    ${$error_ref} = ""; # need to .= this from empty

    if ( not $feeder->{post_notifications} and not $feeder->{post_events} ) {
        $logger->debug("post_notifications and post_events are both disabled - no posting of events or notifications will be done");
        return 1;
    }

    $logger->debug("Posting events and/or notifications");

    # Search for HOST state changes.
    # Construct arrays for both host notification and event objects.
    # CactiHostState can be one of these values ( as determined by get_cacti_host_state() ): UNREACHABLE, UNSCHEDULED DOWN, UP
    foreach $cacti_host ( keys %{$ref_hash_of_host_states} ) {
        # hosts not yet in foundation means FoundationHostState not set, and means don't send events or notifications
        if ( ( defined $ref_hash_of_host_states->{$cacti_host}{FoundationHostState}) and
            ( $ref_hash_of_host_states->{$cacti_host}{CactiHostState} ne $ref_hash_of_host_states->{$cacti_host}{FoundationHostState} )  ) {
    
            # For events and notifications ....
            if ( $ref_hash_of_host_states->{$cacti_host}{CactiHostState} ne 'UP' ) { # ie UNREACHABLE, UNSCHEDULED DOWN, UP
                $notificationType = "PROBLEM";
                $event_severity = "SERIOUS";
            }
            else
            {
                $notificationType = "RECOVERY";
                $event_severity = "OK";
            };
    
            # For notifications ...
            $noma_status = $ref_hash_of_host_states->{$cacti_host}{CactiHostState};
            $noma_status =~ s/UNSCHEDULED//g; # NoMa will quietly ignore UNSCHEDULED DOWN - only accepts UP, DOWN, UNREACHABLE
            $noma_status =~ s/\s+//g; # NoMa will quietly ignore ' UP' - pretty dumb about whitespace
            push @host_notifications, {
                'hostName'         => $cacti_host,
                'hostState'        => $noma_status,
                'notificationType' => $notificationType,
                'hostOutput'       => "$cacti_host is $ref_hash_of_host_states->{$cacti_host}{CactiHostState}",
            };
    
            # For host events ....
            push @host_events, {
                ##'consolidationName' => 'CACTIEVENT', # for now don't consolidate events as they seem to mask each other
                'host'            => $cacti_host,
                'device'          => $cacti_host,
                'monitorStatus'   => $ref_hash_of_host_states->{$cacti_host}{CactiHostState},
                'appType'         => $feeder->{app_type},
                'severity'        => $event_severity,
                'textMessage'     => "$cacti_host is $ref_hash_of_host_states->{$cacti_host}{CactiHostState}",
                'reportDate'      => $formatted_querytime,
                'firstInsertDate' => $formatted_querytime,
            }
        }
    }

    # Search for SERVICE state changes and post events for them
    foreach $cacti_event ( @{$ref_array_of_cacti_events} ) {
        # events for hosts not yet in foundation means FoundationHostState not set, and means don't send events or notifications
        if ( ( defined $cacti_event->{FoundationServiceState} ) and ( $cacti_event->{CactiServiceState} ne $cacti_event->{FoundationServiceState} ) ) {

            # For notifications and events ...
            if ( $cacti_event->{CactiServiceState} ne 'OK' ) { # ie OK, UNSCHEDULED CRITICAL (or WARNING, UNKNOWN from test tweaking)
                $notificationType = "PROBLEM";
                $event_severity = "SERIOUS";
            }
            else
            {
                $notificationType = "RECOVERY";
                $event_severity = "OK";
            };

            # For service notifications ...
            $noma_status = $cacti_event->{CactiServiceState};
            $noma_status =~ s/UNSCHEDULED//g; # NoMa will quietly ignore UNSCHEDULED CRITICAL - only accepts OK, WARNING, CRITICAL and UNKNOWN
            $noma_status =~ s/\s+//g; # NoMa will quietly ignore ' CRITICAL' - pretty dumb about whitespace
            push @service_notifications, {
                'hostName'           => $cacti_event->{Host},
                'serviceDescription' => $cacti_event->{ServiceDescription},
                'serviceState'       => $noma_status,
                'notificationType'   => $notificationType,
                'serviceOutput'      => $cacti_event->{LastPluginOutput},
            };

            # For service events ...
            push @service_events, {
                ##'consolidationName' => 'CACTIEVENT', # for now don't consolidate events as they seem to mask each other
                'host'            => $cacti_event->{Host},
                'device'          => $cacti_event->{Host},
                'service'         => $cacti_event->{ServiceDescription},
                'monitorStatus'   => $cacti_event->{CactiServiceState},
                'appType'         => $feeder->{app_type},
                'severity'        => $event_severity,
                'textMessage'     => $cacti_event->{LastPluginOutput},
                'reportDate'      => $formatted_querytime,
                'firstInsertDate' => $formatted_querytime,
            }
        }
    }

    $status = 1; # assume all operations will be ok and disprove ... rename this var :)

    # Send notifications ...
    if ( $feeder->{post_notifications} ) {
        # Send any host notifications
        if ( @host_notifications ) {
            $logger->debug( "Posting host notifications" );
            if ( not $feeder->feeder_post_notifications( 'host', \@host_notifications ) ) {
                #$feeder->report_feeder_error("NOTIFICATIONS ERROR creating host notifications.");
                ${$error_ref} .= "Notifications error creating host notifications. ";
                $status = 0; # don't bail just yet - try and do as much as possible
            }
        }
        # Send any service notifications
        if ( @service_notifications ) {
            $logger->debug( "Posting service notifications" );
            if ( not $feeder->feeder_post_notifications( 'service', \@service_notifications ) ) {
                #$feeder->report_feeder_error("NOTIFICATIONS ERROR creating service notifications.");
                ${$error_ref} .= "Notifications error creating service notifications. ";
                $status = 0; # don't bail just yet - try and do as much as possible
            }
        }
    }

    # Post host events ...
    if ( $feeder->{post_events} and $feeder->{update_hosts_statuses} ) {
        # Post any host events.
        # Only post host events if update_hosts_statuses is set. Otherwise, the sv host status graphs will reflect up/down states,
        # but the actual host status will not change when update_hosts_statuses = false
        if ( @host_events ) {
            $logger->debug( "Posting host events" );
            if ( not $feeder->feeder_post_events( 'host', \@host_events ) ) {
                #$feeder->report_feeder_error("EVENTS ERROR posting host events.");
                ${$error_ref} .= "Events error posting host events. ";
                $status = 0; # don't bail just yet - try and do as much as possible
            }
        }
     }
    
    # Post any service events - this should be independent of update_hosts_statuses option
    if ( $feeder->{post_events} ) {
        if ( @service_events ) {
            $logger->debug( "Posting service events" );
            if ( not $feeder->feeder_post_events( 'service', \@service_events ) ) {
                #$feeder->report_feeder_error("EVENTS ERROR posting service events.");
                ${$error_ref} .= "Events error posting service events. ";
                $status = 0; # don't bail just yet - try and do as much as possible
            }
        }
    }

    return $status;

}

# ---------------------------------------------------------------------------------
sub validate_cacti_events  # incomplete and used for testing/debugging occasionally
{
    my ( $ref_array_of_cacti_events ) = @_;
    foreach my $event ( @{$ref_array_of_cacti_events} ) {
        print " $event->{monitorStatus} \n";
    }
    # Add some code to validate structure of each event to ensure it has all required fields 
}

# ---------------------------------------------------------------------------------
sub remove_cacti_objects_from_foundation
{
    # Summary 
    # - Removes feeder-created host/services from foundation that don't exist in the cacti data set:
    #     - If a threshold was removed in cacti, remove the correspondig service in foundation
    #     - If a device    was removed in cacti, remove the correspondig hsot    in foundation
    #
    # Detail :
    # - Gets a list of hosts and their services that are in Foundation that were created by this feeder.
    # - Compares that Foundation list with the incoming cacti events lists.
    # - Removes anything in Foundation that was created by this feeder and is not in the cacti list.
    #
    # Arguments :
    # - a ref to an array of cacti event hashes
    # - a ref to a hash of host states (this provides a list of hosts)
    # - ref to error, set on error
    #
    # Returns 1 on ok, 0 and error by ref set otherwise

    my ( $ref_array_of_cacti_events, $ref_hash_of_host_states, $error_ref ) = @_;

    my ( %outcome, %results );
    my ( @hosts_bundle, @all_cacti_hosts, $foundation_host, $cacti_host );
    my ( %foundation_hosts_to_delete, %foundation_services_to_delete );
    my ( %cacti_hosts_and_services, $cacti_event, $foundation_service );
    my ( %delete_hosts_options, %delete_services_options );

    # Get a list of all hosts from foundation that were created by this feeder.
    # There are cases where it's ok if there are no hosts that were created by this feeder:
    #   - fresh GW install, only default hosts
    #   - all hosts were created by some other feeder

    ${$error_ref} = "";


    $logger->debug( "Getting all Foundation hosts that were created by this feeder (guid=$feeder->{guid})");
    if ( not $feeder->{rest_api}->get_hosts( [ ] , { query => "agentId = '$feeder->{guid}'", depth => 'simple' }, \%outcome, \%results ) ) {
        $logger->debug("No hosts were found that were created by this feeder - skipping any required host removal." );
        #return 1; # Don't return - need to cover case of NAGIOS host, with CACTI services added to it
    }

    # Make a list of hosts that exist in Foundation but not in cacti (for the agentId as described above)
    # If not hosts were found in the results, nothing gets added to %foundation_hosts_to_delete here
    foreach $foundation_host ( sort keys %results ) {
        if ( not exists $ref_hash_of_host_states->{ $foundation_host } ) {
            $logger->debug( "Marking Foundation host for delete : $foundation_host");
            $foundation_hosts_to_delete{$foundation_host} = 1;
        }
    }

    # Get a list of all services from foundation that were created by this feeder.
    # Format the results using the host->service format.
    # If got this far, then there is at least one host that this feeder created and deducing that cacti services are expected too.
    # If there aren't any services found that are created by this feeder, then we're done.
    $logger->debug( "Getting all Foundation services that were created by this feeder (guid=$feeder->{guid})");
    if ( not $feeder->{rest_api}->get_services( [ ], { query => "agentId = '$feeder->{guid}'", format=>'host,service' }, \%outcome, \%results ) ) {
        $logger->debug("No services were found that were created by this feeder - skipping any required service removal.");
        return 1;
    }

    # Get a list of all of the endpoint names from the master config. Used for special logic for feeder health services etc
    my %endpoint_hosts = map { (split ':',$_)[0] => 1 }  @{ $master_config->{endpoint} } ;

    # Construct a hash of cacti hosts->services from all of the cacti events
    summarize_cacti_hosts_and_services( $ref_array_of_cacti_events, \%cacti_hosts_and_services, $error_ref );

    # Make a list of services that exist in Foundation but not in cacti (for the agentId as described above)
    FOUNDATIONHOST: foreach $foundation_host ( sort keys %results ) {
        next if ( defined $foundation_hosts_to_delete{$foundation_host} ); # skip deleting services for a host if it host marked for delete - deleting host=>all of its services nuked too
        SERVICE: foreach $foundation_service ( keys %{ $results{$foundation_host} } ) {
            if ( exists $results{$foundation_host}{$foundation_service}{properties}{Notes} and $results{$foundation_host}{$foundation_service}{properties}{Notes} eq $GW::Feeder::metric_service_meta_tag ) { 
                # Skip removal of this feeder metric but only if it's attached to a host that is in the endpoint host set
                if ( exists $endpoint_hosts{ $foundation_host } ) { 
                    $logger->debug( "Skipping service $foundation_host : $foundation_service - its a metric service");
                    next SERVICE;
                }
            }
            if ( not exists $cacti_hosts_and_services{ $foundation_host } { $foundation_service } ) {
                $logger->debug( "Marking Foundation service $foundation_host : $foundation_service for delete");
                $foundation_services_to_delete{$foundation_host}{$foundation_service} = 1;
            }
        }
    }

    # Special logic for cacti feeder health group contents...
    # Don't want to remove hosts that are in the endpoint list...
    foreach my $host_to_delete ( keys %foundation_hosts_to_delete ) {
        if ( exists $endpoint_hosts{ $host_to_delete } ) { 
            $logger->debug("Removing host $host_to_delete from hosts removal list - its an enpoint host");
            delete $foundation_hosts_to_delete{ $host_to_delete } ;
        }
    }

    # Services are already excluded by Notes = Feeder metric check above
    # Don't want to remove hosts (and their services) that are in the endpoint list ...
    #foreach my $host_to_delete ( keys %foundation_services_to_delete ) {
    #    if ( exists $endpoint_hosts{ $host_to_delete } ) { 
    #        $logger->debug("Removing host $host_to_delete from services removal list - its an enpoint host");
    #        delete $foundation_services_to_delete{ $host_to_delete } ;
    #    }
    #}

    # Bundle-wise delete any hosts from Foundation that don't exist in cacti event set
    %delete_hosts_options = ();
    if ( keys %foundation_hosts_to_delete ) {
        if ( not $feeder->feeder_delete_hosts( \%foundation_hosts_to_delete, \%delete_hosts_options ) ) {
            #$feeder->report_feeder_error("ERROR Couldn't delete hosts from Foundation.");
            ${$error_ref} .= "Error deleting hosts from Foundation.";
            return 0;
        }
    }

    # Bundle-wise delete any services from foundation that don't exist in cacti event set
    %delete_services_options = ();
    if ( keys %foundation_services_to_delete ) {
        if ( not $feeder->feeder_delete_services( \%foundation_services_to_delete, \%delete_services_options ) ) {
            #$feeder->report_feeder_error("ERROR Couldn't delete services from Foundation.");
            ${$error_ref} .= "Error deleting services from Foundation.";
            return 0;
        }
    }

    return 1;
}

# ---------------------------------------------------------------------------------
sub sync_hostgroups
{

    # NOTE v.2.1.5
    #  - this is still not working quite right for the case of a host being in the group that was not created by this feeder
    #    - ie hostgroups no longer in the <default_hostgroups> list still keep stacking up in this case
    #  - it mostly works, but for 710 release crunch, leaving this out for the time being.
    #
    # Call this routine before sync-adding things in.
    # It's main purpose to clean up feeder hosts from hosts in groups defined in <default_hostgroups> when that list changes.
    # eg by default <default_hostgroups> has cactigroup. Change that to abc, then this routine will clear the cactigroup group of
    # any hosts that were created by this feeder's agentid. 

    # NOTES:
    # - <default_hostgroups> is not allowed to be empty
    # - SV will not display empty hostgroups. 
    # - There should always be at least the health hostgroup.
    # - Looked into using biz/hosts to modify hostgroup on a per host basis , but need to get status (and proby
    #   other required props) first for each host to use that api. Instead, will use the hostgroup api.

    my ( $error_ref ) = @_; 

    my ( %outcome, %results, $hostgroup, $hosthash, @hosts_to_add_back, %hostgroup_options, @keep_hosts ) ;

    $logger->debug("Sync'ing hostgroups");

    # check $feeder defined - should add this to more routines
    if ( not defined $feeder ) { 
        ${$error_ref} = "No feeder object defined in sync_hostgroup()";
        return 0;
    }
    
    # get a list of hostgroups created by this feeder
    if ( not $feeder->{rest_api}->get_hostgroups( [ ] , { query => "agentId = '$feeder->{guid}'"}, \%outcome, \%results ) ) {  
        ${$error_ref} .= "Didn't get any hostgroups created by this feeder with agentId = $feeder->{guid}";
        return 0;
    }

    # Don't do anything if the list of hostgroups created matches the list of defined <default_hostgroups> hostgroups.
    my $comp = Array::Compare->new();

    my @default_hostgroups = sort keys %{$feeder->{properties}->{default_hostgroups}}   ;
    my @feeder_hostgroups;
    foreach $hostgroup (  keys %results ) {
        next if $hostgroup eq $feeder->{properties}->{health_hostgroup}; # skip the health hostgroup
        push  @feeder_hostgroups, $hostgroup;
    }

    if ( $comp->compare( \@default_hostgroups, \@feeder_hostgroups) ) { 
        $logger->debug( "Feeder-created hostgroups (excluding metrics group) matches default hostgroups in config - nothing to do." );
        return 1;
    }

    # In the case of the first time this feeder is running enabled, there will be only the health group which will be skipped over 
    # and nothing will be done here which is fine.

    # Go through each hostgroup that was created by this feeder...
    #   - if the group is the health group, skip that
    #   - look at the list of hosts in the hostgroup
    #      - if the host was NOT created by this feeder, add it to a list of 'keepers' hostgroup members
    #   - clear the hostgroup
    #   - add 'keepers' back to hostgroup
    
    foreach $hostgroup (  keys %results ) {
        @hosts_to_add_back = @keep_hosts = ();
        next if $hostgroup eq $feeder->{properties}->{health_hostgroup}; # skip the health hostgroup
        foreach $hosthash  ( @{ $results{$hostgroup}{hosts} } )  { 
            # If this host was NOT created by this feeder, then add it to a list of hosts that will be added back to the hostgroup later on
            # after the group is cleared.
            # TBD dry this up
            if ( defined $hosthash->{agentId} ) { 
                if ( $hosthash->{agentId} ne $feeder->{properties}->{guid} ) {
                    push @hosts_to_add_back,  { "name" => $hostgroup, "hosts" => [ $hosthash->{hostName} ] };
                    push @keep_hosts, $hosthash->{hostName};  # this is for debug logging
                }
            }
            else { # host has no agentId so prob'y Nagios type host - just add it to the keep list
                push @hosts_to_add_back,  { "name" => $hostgroup, "hosts" => [ $hosthash->{hostName} ] };
                push @keep_hosts, $hosthash->{hostName};  # this is for debug logging
            }
        }

        # clear the hostgroup, and then add back the hosts to add back
        $logger->debug("Clearing hostgroup $hostgroup");
        if ( not $feeder->feeder_clear_hostgroups( [ $hostgroup ] ) ) { 
            ${$error_ref} .= "Failed to clear hostgroup $hostgroup";
        }

        # If any hosts to add back, add them back
        if ( @keep_hosts ) { 
            $logger->debug("Adding these hosts back to hostgroup $hostgroup : @keep_hosts" );
            %hostgroup_options = ();
            if ( not $feeder->feeder_upsert_hostgroups( \@hosts_to_add_back, \%hostgroup_options ) ) { 
                ${$error_ref} .= "Failed to add these hosts to hostgroup $hostgroup : @keep_hosts";
            }
        }

        # remove the hostgroup completely if it's now empty
        if ( not $feeder->{rest_api}->get_hostgroups( [ $hostgroup ] , { query => "agentId = '$feeder->{guid}'"}, \%outcome, \%results ) ) {  
            ${$error_ref} .= "Couldn't get hostgroup details for hostgroup $hostgroup: " . Dumper \%outcome;
        }

        #print Dumper \%results;
        if ( not exists $results{$hostgroup}{hosts} ) { 
            $logger->debug ("Removing hostgroup $hostgroup - its empty");
            if ( not $feeder->feeder_delete_hostgroups( [ $hostgroup ] , { } ) ) { 
                ${$error_ref} .= "Couldn't remove empty hostgroup $hostgroup";
            }
        }
        
    } # end looping over hostgroups created by this feeder

    # If an error was built during the above logic ... ( error comes into this routine as '', not necessarily undef )
    if ( defined ${$error_ref} and ${$error_ref} ne '' ) { 
        return 0;
    }
    
    return 1;
}

# ---------------------------------------------------------------------------------
sub constrain_to_hostgroups
{
    # Takes a ref to an array of cacti event hashes,
    # Figures out unique set of hosts, figures out which Foundation hostgroups these hosts are in,
    # then removes those events which have hostgroups matching the constrain_to_hostgroup array members
    my ( $ref_array_of_cacti_events ) = @_;

    my ( $cacti_event, %hosts, $cacti_event_hostgroup );

    # Figure out unique set of hostnames across all cacti events
    # assumes a cacti event always has a Host element
    foreach $cacti_event ( @{ $ref_array_of_cacti_events } ) {
        if ( not defined $hosts{ $cacti_event->{Host} } ) {
            $hosts{ $cacti_event->{Host} } = undef;
        }
    }

    # If no hostgroups are defined for constraining, or an error occurred getting hostgroups, just return
    if ( not $feeder->feeder_get_hostgroups( \%hosts ) ) {
        $logger->debug("No constraining to hostgroups will be done.");
        return 0;
    }
    else {
        $logger->debug("Constraining to hostgroups.");
    }

    # Now have a %hosts hash that looks like this :
    # {
    #    host1 => { hg1 =>1, hg2 => 1, hg3 => 1 }, # in hg1, hg2 and hg3
    #    host2 => { hg1 =>1, hg4 => 1 },
    #    host3 => undef; # in no hostgroups
    #    ...
    # }

    # If the cacti event Host is NOT a member of a hostgroup being constrained to, delete it.
    # NOTE it might be better to label it (or its cacti events) as constrained, and then later can decide whether to delete it or not ?
    my $index=0; my $constrained_events = 0; my @constrained_events = ();
    foreach $cacti_event ( @{ $ref_array_of_cacti_events } ) {
        # cycle through the list of hostgroups that this event's Host belongs to
        foreach $cacti_event_hostgroup ( keys %{ $hosts{ $cacti_event->{Host} } } ) {
            # If the event hostgroup matches one its constrained to, add it to a result list
            # ( Why not splice the cacti events array ? Splicing out elements of the array whilst its being referenced in this cacti_event loop - bad :] )
            if ( defined $feeder->{constrain_to_hostgroups}{$cacti_event_hostgroup} ) {
                $constrained_events++;
                $logger->debug("Hostgroup-constrained event : hostgroup $cacti_event_hostgroup, host $cacti_event->{Host}, service $cacti_event->{ServiceDescription}");
                push @constrained_events, $cacti_event;
                last; # only add this event once to the results
            }
        }
        $index ++;
    }

    $logger->info( "Constraining to host groups : events constrained down to $constrained_events events out of a possible total $index events");

    @{$ref_array_of_cacti_events} = @constrained_events;

    return 1;

}

# ---------------------------------------------------------------------------------
sub filter_out_non_state_changed_cacti_hosts_and_services
{
    # Takes a ref to an array of cacti event hashes, and a reference to a hash for host states.
    # Reduces both to just those that are experiencing a state change.
    # No return other than updating the data structures by ref.

    my ( $ref_array_of_cacti_events, $ref_hash_of_host_states ) = @_;
    my ( @new_events, %new_hosts_states, $host, $cacti_event );

    # Filter out hosts which didn't have state change
    foreach $host ( sort keys %{$ref_hash_of_host_states}  ) {
        # In the case of a host not yet existing in Foundation, FoundationHostState will not be defined here.
        # ie only those hosts which are in Foundation and which have had a state change get into the filtered hosts list
        if ( ( defined $ref_hash_of_host_states->{$host}{FoundationHostState} ) and ( "$ref_hash_of_host_states->{$host}{CactiHostState}" ne "$ref_hash_of_host_states->{$host}{FoundationHostState}" ) )  {
            $new_hosts_states{$host} =  $ref_hash_of_host_states->{$host};
        }
    }

    # Update the hosts states results
    %{$ref_hash_of_host_states} = %new_hosts_states;

    # Filter out events which didn't have state change
    foreach $cacti_event ( @{ $ref_array_of_cacti_events } ) {
        # Cacti events should always have CactiServiceState set.
        # If the corresponding service is not in Foundation, then FoundationServiceState won't be set. In that case, do allow it through so it gets
        # added asap rather than waiting for the next full update cycle.
    
        # If both defined and not equal - thats a state change
        if ( ( defined $cacti_event->{FoundationServiceState} ) and ( "$cacti_event->{CactiServiceState}" ne "$cacti_event->{FoundationServiceState}" ) ) {
            push @new_events, $cacti_event;
        }
    
        # New service needs adding so let it through
        if ( not defined $cacti_event->{FoundationServiceState} ) {
            push @new_events, $cacti_event;
        }

    }

    @{$ref_array_of_cacti_events} = @new_events;

}

# ---------------------------------------------------------------------------------
sub cacti_system_test_tweaks
{
    # Takes a ref to an array of cacti event hashes, and a reference to a hash for host states.
    # Tweaks those events as per instructions in the testdefs config.
    # This results in a reduced or altered set of cacti events and/or host states.
    # TBD add counters for more info at the end of tweaking?
    # TBD add <*> ... </*> blocks for applying tweaks to all hosts
    # TBD add * = state  for applying tweaks to all services
    # TBD document the tweak file format - its very useful for QA
    # returns 1 on success with modified events set, 0 otherwise (and no mods to the cacti event set)

    my ( $ref_array_of_cacti_events, $ref_hash_of_host_states, $error_ref ) = @_;
    my ( $cacti_event, $host );
    my ( %testdefs, @new_events, %new_hosts_states, %hosts_to_delete, $new_event );
    my %allowed_host_states = ( "UP"=>1, "UNSCHEDULED DOWN"=>1, "UNREACHABLE"=>1);
    my %allowed_service_states = ( "OK"=>1, "WARNING"=>1, "UNKNOWN"=>1, "UNSCHEDULED CRITICAL"=>1, "FLIP"=>1 ); # FLIP means toggle OK<->CRIT

    # This section tweaks cacti hosts' CactiHostState's.
    %testdefs = %$tests_config; # dereference it else checking it will actually populate it unclear why tho

    $logger->debug("Processing cacti system test tweaks configuration '$feeder->{cacti_system_test_tweaks_file}'");

    # Identify which hosts need deleting
    foreach $host ( sort keys %{$ref_hash_of_host_states}  ) {
        # Repetition of host blocks will be disallowed for clarity
        if ( ref ( $tests_config->{$host} ) eq 'ARRAY' ) {
            #$feeder->report_feeder_error( "TWEAKING ERROR in $feeder->{cacti_system_test_tweaks_file} - host $host had a block repeated - please consolidate. No test tweaking will be done.");
            ${$error_ref} = "Tweaking error in $feeder->{cacti_system_test_tweaks_file} - host $host had a block repeated - please consolidate. No test tweaking will be done.";
            return 0;
        }
        if ( exists $testdefs{ $host } { 'delete' } ) {
            $hosts_to_delete{$host} = 1;
            $logger->info( "Marking host '$host' for delete");
        }
        else {
            # this hash stores hosts that are marked for delete
            $new_hosts_states{$host} =  $ref_hash_of_host_states->{$host};
    
            # tweak cacti hosts' CactiHostState's
            if ( $testdefs{ $host }{ hoststate } ) {
                $logger->info( "Tweaking host '$host' state from '$ref_hash_of_host_states->{$host}->{CactiHostState}' to '$testdefs{ $host }{ hoststate }'");
                #$ref_hash_of_host_states->{$host}->{CactiHostState} = $testdefs{ $host }{ hoststate };
                if ( not exists $allowed_host_states{ $testdefs{ $host }{ hoststate }} ) {
                    #$feeder->report_feeder_error( "TWEAKING ERROR in $feeder->{cacti_system_test_tweaks_file} - host state '$testdefs{ $host }{ hoststate }' invalid for host $host - should be one of :" .  join( ",", keys %allowed_host_states )  .  ". No test tweaking will be done." );
                    ${$error_ref} = "Tweaking error in $feeder->{cacti_system_test_tweaks_file} - host state '$testdefs{ $host }{ hoststate }' invalid for host $host - should be one of :" .  join( ",", keys %allowed_host_states )  .  ". No test tweaking will be done." ;
                    return 0;
                }
                $new_hosts_states{$host}{CactiHostState} = $testdefs{ $host }{ hoststate };
            }
        }
    }

    # update host states for rest of this sub and the rest of the feeder to use
    %{$ref_hash_of_host_states} = %new_hosts_states;

    # This section tweaks cacti event's CactiServiceState's
    my $skipcount = 0;
    foreach $cacti_event ( @{ $ref_array_of_cacti_events } ) {

        # skip event if host marked for delete ...
        if ( exists $hosts_to_delete{ $cacti_event->{Host} } ) {  # if host marked for delete, next
            $logger->debug("Skipping event since host $cacti_event->{Host} was marked for delete");
            $skipcount++;
            next;
        }
        # tweak services if applicable
        if ( exists $testdefs{ $cacti_event->{Host} }{ $cacti_event->{ServiceDescription}}  ) {
            # skip event if service marked for delete ...
            if ( $testdefs{ $cacti_event->{Host} }{ $cacti_event->{ServiceDescription}} eq 'delete') {
                $logger->debug("Skipping event since service $cacti_event->{ServiceDescription} was marked for delete");
                $skipcount++;
                next;
            }
            # just set the service state
            else {
                # Test validity of incoming tweaked state
                if ( not exists $allowed_service_states{ $testdefs{ $cacti_event->{Host} }{ $cacti_event->{ServiceDescription} } } ) {
                    # $feeder->report_feeder_error( "TWEAKING ERROR in $feeder->{cacti_system_test_tweaks_file} - service state '$testdefs{ $cacti_event->{Host} }{ $cacti_event->{ServiceDescription} }' invalid - should be one of : " .  join( ",", keys %allowed_service_states )  .  ". No test tweaking will be done." );
                    ${$error_ref} = "Tweaking error in $feeder->{cacti_system_test_tweaks_file} - service state '$testdefs{ $cacti_event->{Host} }{ $cacti_event->{ServiceDescription} }' invalid - should be one of : " .  join( ",", keys %allowed_service_states )  .  ". No test tweaking will be done.";
                    return 0;
                }
                my $newservicestate = $testdefs{ $cacti_event->{Host} }{ $cacti_event->{ServiceDescription} };
                # FLIP ideally would work on foundation state rather than cacti state. Ltd use for now.
                if ( $testdefs{ $cacti_event->{Host} }{ $cacti_event->{ServiceDescription} } eq 'FLIP' ) {  
                    if ( $cacti_event->{CactiServiceState} eq 'OK' )                    { $newservicestate = 'UNSCHEDULED CRITICAL'; }
                    if ( $cacti_event->{CactiServiceState} eq 'UNSCHEDULED CRITICAL' )  { $newservicestate = 'OK'; }
                }
                $logger->debug("Tweaking service '$cacti_event->{ServiceDescription}' state from '$cacti_event->{CactiServiceState}' to '$newservicestate'");
                $new_event = $cacti_event;
                $new_event->{CactiServiceState} = $newservicestate;
            }
        }
        else {
            #print "Host / Service combo $cacti_event->{Host} / $cacti_event->{ServiceDescription} doesn't exist in incoming cacti events - skipping\n";
            # TMI ?
        }
    
        # build the new events array
        push @new_events, $cacti_event;

    }
    $logger->info("Tweaking : $skipcount events have been removed essentially") if $skipcount > 0;

    @{$ref_array_of_cacti_events} = @new_events; # pass the newly build events back

    return 1;

}

# ---------------------------------------------------------------------------------
sub get_hosts_states
{
    # Takes a ref to an array of cacti event hashes, and a reference to a hash for host states.
    # %host_states is populated with two things : state of cacti host in Foundation, state of cacti host in Cacti.
    # Tries to be as fast and efficient as possible with the API calls.
    # If the API call fails, then the FoundationHostState's don't get set, which will make these appear to not
    # exist in Foundation later on in the code.
    # returns 1 if ok, 0 otherwise

    $logger->debug( "Getting host states");

    my ( $ref_array_of_cacti_events, $ref_hash_of_host_states, $error_ref ) = @_;
    my ( $gwevent, $host, %cacti_hosts, %outcome, %results, $cacti_host );
    my ( @all_cacti_hosts, $hbsize, @hosts_bundle );
    my ( $host_cacti_state );

    ${$error_ref} = undef;

    # Extract a unique hash of the cacti host names out of the cacti events list
    # and set their values to their current cacti host states

    foreach $gwevent ( @{ $ref_array_of_cacti_events } ) {

        # Get the state of the cacti host as it is OR WAS in the Cacti system
        #$host_cacti_state = get_cacti_host_state( $gwevent->{HostId} );
        $host_cacti_state = $gwevent->{HostStatusGW};
    
        # Stash that host state - this code assumes that the host state is equal across of cacti services
        # as per that big select up in sync_endpoint.
        if ( not defined $ref_hash_of_host_states->{ $gwevent->{Host} }{CactiHostState} ) {
            $ref_hash_of_host_states->{ $gwevent->{Host} }{CactiHostState}   = $host_cacti_state;
        }
    
        # Continue to build the hash of unique hostnames across all events
        if ( not defined $cacti_hosts{ $gwevent->{Host} }  ) {
            $cacti_hosts{ $gwevent->{Host} } = 1;
        }
    }

    # Efficiently get batches of Foundation host states
    @all_cacti_hosts = keys %cacti_hosts;
    while ( @hosts_bundle = splice @all_cacti_hosts, 0, $feeder->{host_bundle_size} ) {
        $logger->debug("Getting host states for " . ($#hosts_bundle + 1) . " hosts");
        if ( not $feeder->{rest_api}->get_hosts( \@hosts_bundle, {}, \%outcome, \%results ) ) {
            if ( defined $outcome{response_code} and $outcome{response_code} ne '404' ) {
                #$feeder->report_feeder_error("ERROR getting host states : " . Dumper \%outcome, \%results); 
                ${$error_ref} = "Error getting host states : " . Dumper \%outcome, \%results; 
                return 0;
            }
        }
        foreach $cacti_host ( keys %cacti_hosts ) {
            if ( defined $results{$cacti_host}{monitorStatus} ) {
                $ref_hash_of_host_states->{$cacti_host}{FoundationHostState} = $results{$cacti_host}{monitorStatus};
            }
            else {
                # the host might not yet exist in Foundation
                #print "Host $cacti_host doesn't have monitorStatus set yet\n"; #  TBD anything to do here ?
            }
        }
    }

    # At this point, if things worked, then the hash referred to by $ref_hash_of_host_states will look like this, for eg:
    #
    # {
    #     'host1' => {
    #                        'CactiHostState' => 'UNSCHEDULED DOWN', # ie host1's state in Cacti (translated to GW) is this
    #                         # NOTE  no FoundationHostState set if the host was not yet in Foundation!!!
    #                },
    #     'host2' => {
    #                        'CactiHostState' => 'UNSCHEDULED DOWN', # ie host2's state inc Cacti (translated to GW states) is this
    #                        'FoundationHostState' => 'UP' # ie host2's host state in Foundation is this
    #                },
    #      ...
    # }

    # If things failed, or the hosts in cacti don't exist in Foundation, then the FoundationHostState's won't be in the hash.

    return 1;

}

# ---------------------------------------------------------------------------------
sub get_and_set_foundation_service_states
{
    # Takes a ref to an array of cacti event hashes, and a ref to a hash of host states (for a list of hosts)
    # and figures out Foundation states for the cacti services.
    # Results are stored back into each cacti event object in the array ref.

    my ( $ref_array_of_cacti_events, $ref_hash_of_host_states, $error_ref ) = @_;
    my ( $cacti_event, @all_cacti_hosts, @hosts_bundle, $hbsize, %outcome, %results, $cacti_host, $cacti_service);

    ${$error_ref} = undef;

    # Efficiently get all services for a subset of hosts (ie with least # of api calls)
    # Note : there's currently no way to get services like this : {hostA, svc1}, {hostB, svc2} etc which would be ideal here.

    # Definitely other ways this could be approached.  For example, could figure out the services list for each cacti host
    # and get all of the service states for that host from foundation. The possible issue with that approach is that the service
    # names for cacti services can be quite long and quite numerous so might run into exceeding url length limitations via RAPID
    # which is just doing a GET <url>.
    # Could also get all hosts services (a bit too heavy an operation for thousands of hosts with many services on them).
    # Could also chunk it up by getting all services for subsets of hosts like in get_hosts_states().
    # For now will get all services for each host via one api call PER HOST and see how that performs.
    # Getting all services for each host is less efficient.
    # Returns 1 on success, 0 otherwise

    # Create a simple hash of cacti hostnames and their sets of cacti services from the events.
    # This is aimed at helping unpack potentially large nested loops.
    my %cacti_hosts_and_services;

    $logger->debug( "Getting and setting Foundation service states");

    foreach $cacti_event ( @{ $ref_array_of_cacti_events } ) {
        $cacti_hosts_and_services{  $cacti_event->{Host} } { $cacti_event->{ ServiceDescription } } = undef;
    }

    @all_cacti_hosts = sort keys %{$ref_hash_of_host_states};
    while ( @hosts_bundle = splice @all_cacti_hosts, 0, $feeder->{host_bundle_size} ) {
        #$logger->debug( "get_and_set_foundation_service_states() process bundle of " . ($#hosts_bundle + 1 ) . " host(s) : @hosts_bundle" );
        $logger->debug( "get_and_set_foundation_service_states() process bundle of " . ($#hosts_bundle + 1 ) . " host(s)" );
        if ( not $feeder->{rest_api}->get_services( [], { hostname => \@hosts_bundle, format => 'host,service' }, \%outcome, \%results ) ) {
            if ( defined $outcome{response_code} and $outcome{response_code} ne '404' ) {
                #$feeder->report_feeder_error( "ERROR Getting Foundation service states - no Foundation service states will be set for this bundle of hosts : @hosts_bundle" );
                ${$error_ref} =  "Error getting Foundation service states - no Foundation service states will be set for this bundle of hosts : @hosts_bundle" ;
                return 0;
            }
            else {
                # else just carry on - its ok to get a 404 in the case of ALL services not existing yet
            }
        }
        foreach $cacti_host ( @hosts_bundle ) {
            # See if the cacti services for this host have a service status in the Foundation services list for this host
            foreach $cacti_service ( keys %{ $cacti_hosts_and_services{ $cacti_host } } ) {
                # If the cacti service for this cacti host showed up with a status in Foundation service results for this host, then
                # update the cacti hosts and services hash for this host/service with the Foundation service status
                if ( defined $results{$cacti_host}{$cacti_service}{monitorStatus} ) {
                    #print "Host '$cacti_host' -> Service '$cacti_service' === defined : status=$results{$cacti_host}{$cacti_service}{monitorStatus}\n";
                    $cacti_hosts_and_services{ $cacti_host } { $cacti_service } = $results{$cacti_host}{$cacti_service}{monitorStatus};
                }
                    # Else the cacti service didn't show up in Foundation so it hasn't been added yet - and thats ok NOTE so far this has been ok 
            }
        }
    }
    
    # Go back through the original array of cacti events, inserting FoundationServiceState values
    foreach $cacti_event ( @{ $ref_array_of_cacti_events } ) {
        # If this cacti event has a defined value in the cacti hosts and events hash built above, then record that back into the event itself
        if ( defined $cacti_hosts_and_services{ $cacti_event->{Host} } { $cacti_event->{ServiceDescription} } ) {
            $cacti_event->{FoundationServiceState} = $cacti_hosts_and_services{ $cacti_event->{Host} } { $cacti_event->{ServiceDescription} };
        }
        else {
            # service has not been added to Foundation yet and thats ok NOTE so far this has been ok
        }
    }

    return 1;
}

# ---------------------------------------------------------------------------------
sub upsert_foundation_hosts_with_cacti_host_states
{
    # Takes a ref to a hash of cacti host names with expected CactiHostState values and
    # creates the hosts or updates them (ie upserts them) in Foundation
    my ( $ref_hash_of_cacti_hosts, $formatted_querytime, $error_ref ) = @_;
    my ( @cacti_hosts_bundle, @cacti_hosts, $cacti_host, %host_options, @hosts, $error );

    ${$error_ref} = undef;
    %host_options = ();
    @cacti_hosts = keys %{$ref_hash_of_cacti_hosts};

    $logger->debug("Upserting hosts");

    if ( not @cacti_hosts ) {
        $logger->debug("No hosts needed processing.");
        return 1;
    }

    # Build an array of options that the feeder rest api can consume
    # However, don't pass in description, properties, agentId, appType or anything else that
    # will overwrite things should the host already exist. Instead, let feeder_upsert_hosts add those if necessary.
    foreach $cacti_host ( @cacti_hosts ) {

        push @hosts,  {
                          # This should be the smallest set of properties required for updating an existing host
                          'hostName'       => $cacti_host,
                          'monitorStatus'  => $ref_hash_of_cacti_hosts->{$cacti_host}{CactiHostState},
                          'lastCheckTime'  => $formatted_querytime, # this is needed to ensure events have correct time stamps => correct host state histograms in sv
                      };
    }

    # feeder_upsert_hosts does bundling
    if ( not $feeder->feeder_upsert_hosts( \@hosts, \%host_options, \$error ) ) {
        #$feeder->report_feeder_error("FOUNDATION HOSTS UPSERT ERROR could not upsert hosts" );
        ${$error_ref} = "Foundation hosts upsert error - could not upsert hosts. " ;
        ${$error_ref} .= $error if defined $error;
        return 0;
    }

    return 1;
}

# ---------------------------------------------------------------------------------
sub upsert_foundation_hostgroups_with_cacti_hosts
{
    # Takes a ref to an array of cacti event hashes, and updates foundation hostgroup membership with
    # the hosts from these events. The hostgroups these hosts are assigned to are defined in the default_hostgroups hash prop in the conf.
    # The idea behind the decoupling of assigning hostgroups to hosts from events, and the construction of hostgroup REST data
    # is to allow the former to change without the latter needing to necessarily.
    # This always gets executed regardless. Be nice to make it smarter, however it's not necessarily efficient to check hostgroup membership
    # and then update accordingly, versus just upserting every time.
    # Returns 1 on success, 0 otherwise

    my ( $ref_array_of_cacti_events, $error_ref ) = @_;
    my ( %hostgroup_options, @hostgroups, $cacti_event, %cacti_hosts_and_groups, $cacti_hostgroup, $cacti_hostgroup_member, @hosts, $hostgroup );

    ${$error_ref} = undef;
    %hostgroup_options = ();

    $logger->debug("Upserting hostgroups");

    # Figure out from the cacti events which hostgroups have which hosts as members
    # that info goes into an hash <hostgroupname> => { host, host, host ... }
    foreach $cacti_event ( @{$ref_array_of_cacti_events} ) {
        foreach $hostgroup ( keys %{ $feeder->{default_hostgroups} } ) {
            $cacti_hosts_and_groups{ $hostgroup} { $cacti_event->{Host}} = 1;
        }
    }

    # Construct data for api call
    foreach $cacti_hostgroup ( keys %cacti_hosts_and_groups ) {
        # Get the members of each hostgroup
        @hosts = ();
        foreach $cacti_hostgroup_member ( keys %{$cacti_hosts_and_groups{$cacti_hostgroup}} ) {
            push @hosts, { "hostName" => $cacti_hostgroup_member };
        }
        # Build the required api fields, referencing the hosts array built beforehand
        # However, don't pass in anything that will overwrite things should the hostgroup already exist.
        # Instead, let feeder_upsert_hostgroups add those if necessary.
        push @hostgroups, {
            ## Just enough properties to update hostgroup membership
            "name"  => $cacti_hostgroup,
            "hosts" => [@hosts],           # use [ @hosts ] rather than \@hosts here
        };
    
        # Upsert the hostgroups, remember that feeder_upsert_hostgroup handles bundling
        # at the hostgroup level, but not at the hosts level ie you could have one hostgroup with 100000 hosts
        # and all 100000 will attempted to be added in one api call. TBD improve this later perhaps.
    }

    if ( not $feeder->feeder_upsert_hostgroups( \@hostgroups, \%hostgroup_options ) ) {
        #$feeder->report_feeder_error("FOUNDATION HOSTGROUPS UPSERT ERROR could not upsert hostgroups" );
        ${$error_ref} = "Foundation hostgroups upsert error - could not upsert hostgroups" ;
        return 0;
    }

    return 1;

}

# ---------------------------------------------------------------------------------
sub upsert_foundation_cacti_services
{
    # Takes an ref to an array of cacti events and upserts their services in Foundation
    # Returns 1 on success, 0 otherwise

    my ( $ref_array_of_cacti_events, $formatted_querytime, $error_ref ) = @_;
    my ( $cacti_event, @services, %service_options );

    ${$error_ref} = undef;

    if ( not @{$ref_array_of_cacti_events} ) {
        $logger->debug("No cacti events needed processing.");
        return 1;
    }

    $logger->debug("Upserting foundation cacti services");

    %service_options = ( );
    foreach $cacti_event ( @{$ref_array_of_cacti_events} ) {

        # Build the required api fields.
        # However, don't pass in anything that will overwrite things should the host:service already exist.
        # Instead, let feeder_upsert_services add those if necessary.
        push @services, {
                    # This is the minimum set of properties to achieve an update of the service
                    'description'          => $cacti_event->{ServiceDescription},   # the name of the service
                    'hostName'             => $cacti_event->{Host},                 # the host name
                    'monitorStatus'        => $cacti_event->{CactiServiceState},    # the service status
                    'properties'           => { "LastPluginOutput" => $cacti_event->{LastPluginOutput} }, # the service status message
                    'lastCheckTime'        => $formatted_querytime, # this is needed to ensure events have correct time stamps => correct host state histograms in sv
                };
        }
    
        if ( not $feeder->feeder_upsert_services( \@services, \%service_options ) ) {
            #$feeder->report_feeder_error("FOUNDATION SERVICES UPSERT ERROR could not upsert Cacti services in Foundation" );
            ${$error_ref} = "Foundation services upsert error - could not upsert Cacti services in Foundation";
            return 0;
        }

    return 1;
}

# ---------------------------------------------------------------------------------
sub get_cacti_host_state
{
    # Maps a cacti host state to a GW host state.
    #
    # Cacti host state values and their desired GW mappings are:
    #   3 Up         => UP                  it is pingable, snmp accessible, or both depending on how configured  AND it has graphs assigned
    #   2 Recovering => PENDING             it was disabled and is now enabled but not yet pinged/snmp polled
    #   1 Down       => UNSCHEDULED DOWN    the configured number of pings/polls by snmp with a failure has been reached
    #   0 Unknown    => UNKNOWN             not reporting in, probably because it is "disabled" see below
    #
    # These negative cacti host states are relating to the UI - host states should only be 0-3 above
    #  -1 Any        => UNKNOWN             just allows the UI to show you hosts in whatever state they are in. You will never ever get back status of a host that shows -1
    #  -2 Disabled   => UNKNOWN             means the check box in the configuration is not checked for "enabled" so Cacti will not poll or ping it. State reported for these will likely be "unknown" as above. Needs testing.
    #  -3 Enabled    => UNKNOWN             means that the check box is checked so Cacti is going to process these. State reported will not be "enabled" but rather, up, recovering, down or unknown
    #  -4 Not Up     => UNKNOWN             means show me the devices which are not in an up state; the actual status reported will be up, recovering, down or unknown
    #
    # Arguments
    # - a cacti host state 
    # - a reference to a mapped gw state which this routine populates
    #
    # Returns 
    #   - populated GW host state by ref (undef if not able to map)
    #   - 1 if mapped ok, 0 otherwise
    #
    # Notes
    # GW host states here have been chosen so they render properly in status viewer:
    #  - DOWN needs to be UNSCHEDULED DOWN for the REST API to work
    #  - UNKNOWN whilst currently accepted by upsert_hosts() shows up as unrecognized ie empty host state in sv, so using UNREACHABLE instead
    #  - PENDING cannot be used because this data is used in events too - and PENDING indicates the adding of a host in the host context, so UNREACHABLE is used instead.

    my ( $cacti_host_status, $ref_mapped_gw_status ) = @_;

    my %gw_host_statuses = (
         '3' => 'UP',
         '2' => 'UNREACHABLE',               
         '1' => 'UNSCHEDULED DOWN',
         '0' => 'UNREACHABLE',
         # Negative states are only for UI
         # '-1' => 'UNREACHABLE',      
         #'-2' => 'UNREACHABLE',
         #'-3' => 'UNREACHABLE',
         #'-4' => 'UNSCHEDULED DOWN',
    );

    # By default, anything unrecognized is going to fall into the UNREACHABLE bucket. See Notes above about UNKNOWN.
    $$ref_mapped_gw_status = 'UNREACHABLE'; 

    #if ( not exists $gw_host_statuses{ $cacti_host_status } ) {
    #    $feeder->report_feeder_error("ERROR Unrecognized cacti host status '$cacti_host_status' - unable to map it to a GroundWork host state.");
    #    return 0;
    #}
    
    # If there's a cacti->gw host state mapping, use it.
    $$ref_mapped_gw_status = $gw_host_statuses{ $cacti_host_status } if exists $gw_host_statuses{ $cacti_host_status };

    return 1;
}

# ---------------------------------------------------------------------------------
sub initialize_database_connection
{
    # Set up cacti database handle.
    # Returns 1 on success, 0 otherwise

    my ( $dsn_cacti );

    $logger->trace("Initializing connection to cacti database");
    if ( $master_config->{cactidbtype} eq 'postgresql' ) {
        $dsn_cacti = "DBI:Pg:dbname=$master_config->{cactidbname};host=$master_config->{cactidbhost};port=$master_config->{cactidbport}";
    }
    elsif ( $master_config->{cactidbtype} eq 'mysql' ) {
        $dsn_cacti = "DBI:mysql:database=$master_config->{cactidbname};host=$master_config->{cactidbhost};port=$master_config->{cactidbport}";
    }
    else {
        # unrecognized db type error
        $logger->error("DATABASE ERROR Invalid database type - should be postgresql or mysql.");
        return 0;
    }

    $database_handle = DBI->connect( $dsn_cacti, $master_config->{cactidbuser}, $master_config->{cactidbpass}, { 'AutoCommit' => 1 } );
    if ( ! $database_handle ) {
        $logger->error ("DATABASE ERROR Cannot connect to database '$master_config->{cactidbname}'. Error: '$DBI::errstr'");
        return 0;
    }

    return 1;
}

# ---------------------------------------------------------------------------------
sub close_database_connection
{
    # Closes connection to the database (cacti db in this case)
    # Returns 1 on success (including if there database handle is undefined), 0 otherwise. 
    # Not quite sure what to do in failure case here tho.
    return 1 if not defined $database_handle;
    eval { $database_handle->disconnect(); } ; # a failed disconnect will kill the script
    if ( $@ ) {
        chomp $@;
        $logger->error ("DATABASE ERROR Cannot disconnect from database '$master_config->{cactidbname}'. Error: '$@'");
        return 0;
    }
    return 1;
}

# ---------------------------------------------------------------------------------
# No longer in use - left in for possible re-use later
sub db_connection_ok
{
    # Checks to see if a db connection is up using the ping() method.
    return 0 if not defined $database_handle;
    return $database_handle->ping();
}

# ---------------------------------------------------------------------------------
sub update_feeder_stats
{
    # Updates feeder metrics services with
    # - elapsed time for the cycle
    # - total count of all built query data rows 
    # - total count of successfully processed built query rows 
    # - total count of raw queried rows 
    # - reference to an array of built metrics services that will be populated by this routine.

    my ( $cycle_number, $total_cycle_time_taken, $total_built_thresholds_count, $total_successfully_processed_built_rows, $total_queried_threshold_rows, $built_metrics_ref ) = @_;

    my ( $cycle_elapsed_time_msg, $events_processed_msg, $cycle_elapsed_time_stat, $events_processed_stat, $formatted_query_time, %hosts_states ) ;

    $total_built_thresholds_count            = 0 if not defined $total_built_thresholds_count;
    $total_successfully_processed_built_rows = 0 if not defined $total_successfully_processed_built_rows;

    $cycle_elapsed_time_msg  = "Cycle $cycle_number : Elapsed processing time was $total_cycle_time_taken seconds";
    $cycle_elapsed_time_stat = 'OK';

    $events_processed_msg   = "Cycle $cycle_number : $total_queried_threshold_rows raw data rows, $total_built_thresholds_count built thresholds, $total_successfully_processed_built_rows built rows processed.";
    $events_processed_stat  = 'OK';

    $logger->debug("Updating feeder statistics");

    # Log metrics
    $logger->info( "$cycle_elapsed_time_msg") if defined $feeder->{cycle_timings};
    $logger->info( "$events_processed_msg");

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
            service => "$feeder_name.cycle.processed.built.thresholds",
            message => "Feeder host $GW::Feeder::feeder_host: $events_processed_msg",
            status  => $events_processed_stat,
            # TBD at some point in the future when we're switched over to opentsdb and no longer use the perf data file processor, put these multiple metrics back in.
            #     Until then, we can only send in one to the rrd via the perfdata api
            #perfval  => { queried_rows => $total_queried_threshold_rows, built_thresholds => $total_built_thresholds_count, built_thresholds_processed => $total_successfully_processed_built_rows }
            perfval  => {  cycle_processed_built_thresholds => $total_successfully_processed_built_rows },
            #properties  => {  Notes => "abc"  }
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
Groundwork Cacti feeder - version $VERSION
GroundWork Feeder module version $GW::Feeder::VERSION

Description

    Overview

        The cacti feeder synchronizes cacti thresholds with GroundWork services.
        Multiple GroundWork instances can be fed by one feeder instance.
        The term 'endpoint' here refers to a GroundWork REST API endpoint, ie GroundWork server.
        By default, the cacti feeder is disabled. See configuration section for details.
        The cacti feeder runs daemonized and is controlled via the GroundWork supervise subsystem.
        The log file is defined in /usr/local/groundwork/config/cacti_feeder.log4perl.conf and
        is by default /usr/local/groundwork/foundation/container/logs/cacti_feeder.log
    
    Algorithm description

        The feeder follows this algorithm :

            - get cacti thresholds data from cacti
            - for all configured endpoints:
                - prepend retry cache contents to the data set, purging entries that are too old
                - build a dataset consumable by the endpoint, based on endpoint's configuration 
                - synchronize the endpoint with the build data set
                - upon failures, rebuild the endpoint's retry cache

    Resiliency
        
        If an endpoint is unreachable, or fails during an operation, the dataset is placed into
        a retry cache for the endpoint. This cache is imported at the beginning of each processing
        cycle, prepending it to the latest thresholds data for processing.
        Retry caches are plain text files contain timestamped JSON objects that represent query data.
        These cache files directory is defined by the cacti_feeder.conf retry_cache_directory setting.

    General Configuration

        The master configuration file is $master_config_file.
        This configuration file defines configuration common to processing all endpoints, 
        including the definition of endpoints themselves. To enable the feeder, 
        set feeder_enabled = yes.

        Endpoint configuration files, e.g. /usr/local/groundwork/config/cacti_feeder_localhost.conf
        specify configuration specific to each endpoint. Each endpoint configuration file includes an
        ws_client_config_file option which points to a Groundwork web services configuration file, 
        inside which is the actual REST endpoint's details, held in the foundation_rest_url properties. 
        The default web services properties file is /usr/local/groundwork/ws_client.properties.

        Configuration files are read every processing cycle i.e. it is not necessary to restart the 
        feeder after changing a setting in any configuration file.  This includes enabling/disabling 
        the feeder.

    Configuring for SSL

        Before configuring the cacti feeder to use SSL, follow instructions from GroundWork on 
        configuring all GroundWork servers to use SSL.

        If you're using the cacti feeder that comes with GroundWork, all locally on one system, 
        you do not need to change the cacti feeder's configuration, i.e., 
        foundation_rest_url=http://localhost:8080/foundation-webapp/api.  If there is an second non 
        local SSL endpoint - host endpoint2 say - which the feeder is synchronizing, then set 
        foundation_rest_url=https://endpoint2/foundation-webapp/api .

    Testing Options

        Some additional testing options are built in to the feeder. These are not normally
        required to be used in the field.
    
        1. Emulating cacti host and threshold states

        To emulate different cacti device and threshold states etc, you can use a 'tweaks' file.
        Point the endpoint's configuration cacti_system_test_tweaks_file option to the tweaks file.
        The tweaks file format is as follows :

            <hostname1>

                # set host state to something
                [ hoststate = { UP, UNSCHEDULED DOWN, UNREACHABLE } ]
         
                # set service state(s) to something
                [ <servicename1> = { OK, WARNING, UNKNOWN, UNSCHEDULED CRITICAL }
                [ <servicename1> = { OK, WARNING, UNKNOWN, UNSCHEDULED CRITICAL }
                [ <servicename1> = { OK, WARNING, UNKNOWN, UNSCHEDULED CRITICAL }
         
                # delete the host - this emulates the device being removed in cacti
                [ delete ]
         
            </hostname1>
        
            <host2>
                ...
            </host2>
            ...

        2. Feeder module error emulation (fmee)

        To emulate failures in Feeder.pm routines, use the <fmee> ... </fmee> block
        in that endpoint's configuration file. It has the following format :

        <fmee>
            #timestamp = 1425331212
            #feeder_upsert_hosts
            #feeder_upsert_hostgroups
            #feeder_upsert_services
            #feeder_delete_hosts
            #feeder_delete_services
            #check_foundation_objects_existence
            #flush_audit
            #initialize_health_objects
            #license_installed
            #check_license
        </fmee>

        To use this, uncomment out one or more of the entries which, with the exception of timestamp, 
        are Feeder.pm module routine names. Uncommenting them out will emulate failure in these routines.
        To emulate failure processing data associated with a given timestamp (useful for retry cache 
        testing), set timestamp to an epoch time that matches the timestamp of interest.

Options
    -clean        - removes foundation objects that this feeder (uuid) created across all endpoints, then exits
    -help         - Show this help
    -every <N>    - Run main cycle every N seconds, and don't wait for ready indicator file.
                    This is useful for QA/testing purposes.
    -once         - Run one main cycle and exit
    -version      - Show version and exit
    -yes          - Assumes yes to the remove question presented by the -clean option

Author
    GroundWork 2015

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
sub read_cacti_system_test_tweaks_file
{
    # Reads the cacti_system_test_tweaks_file config into a hash

    $tests_config = undef; # reset from any previous endpoint's test config just in case

    if ( ( not -e $feeder->{cacti_system_test_tweaks_file} ) or ( not -r $feeder->{cacti_system_test_tweaks_file} ) ) {
        $logger->debug("Cacti system test tweaks config file '$feeder->{cacti_system_test_tweaks_file}' doesn't exist or isn't readable. No testing will be done.");
        return;
    }

    eval { $tests_config = TypedConfig->new ( $feeder->{cacti_system_test_tweaks_file} ); };
    if ($@) {
        chomp $@;
        $@ =~ s/^ERROR:\s+//i;
        $logger->error("Cannot read tests config file $feeder->{cacti_system_test_tweaks_file} ($@) - continuing without tests");
    };
}

# ---------------------------------------------------------------------------------
sub summarize_cacti_hosts_and_services
{
    # Takes an array of cacti event hashes and
    # boils it down to a hash : { host1 => { service1=>1, service2=>2, ... },   host2 => { service5=>1, service12=>2, ... }   }

    my ( $ref_hash_of_cacti_hosts, $hashref_results, $error_ref ) = @_;
    my ( %results, $cacti_event );

    ${$error_ref} = "" if not defined ${$error_ref};

    foreach $cacti_event ( @{$ref_hash_of_cacti_hosts} ) {
        if ( defined $cacti_event->{Host} and defined $cacti_event->{ServiceDescription} ) {
            $results{ $cacti_event->{Host} } { $cacti_event->{ServiceDescription} } = 1;
        }
        else {
            #$feeder->report_feeder_error("ERROR Skipping malformed cacti event - expecting both Host and ServiceDescription keys to be defined. Got " . Dumper $cacti_event );
            ${$error_ref} .= "Error - Skipping malformed cacti event - expecting both Host and ServiceDescription keys to be defined. Got " . Dumper $cacti_event;
        }
    }

    %{$hashref_results} = %results;

}

# ---------------------------------------------------------------------------------
sub validate_feeder_specific_options
{
    # Logic for validation of feeder-specific options
    # Returns 1 if ok, 0 otherwise

    my ( $error_ref ) = @_;
    my ( $message ) ;

    # Required configuration properties are defined in feeder_specific_properties above
    # and are automatically checked for type when a new feeder object is created. All
    # that remains is any final value sanity checking.
    # 
    #           always_send_full_updates               => 'boolean', # enough checking in Feeder.pm new()
    #           cacti_system_test_tweaks_file          => 'scalar',  # this is checked elsewhere
    #           constrain_to_hostgroups                => 'hash',    # check here
    #           default_hostgroups                     => 'hash',    # check here
    #           full_update_frequency                  => 'number',  # check here
    #           host_bundle_size                       => 'number',  # check here
    #           hostgroup_bundle_size                  => 'number',  # check here
    #           retry_cache_max_age                    => 'number',  # check here
    #           service_bundle_size                    => 'number',  # check here

    # Default hostgroups hash should have at least an entry
    if ( not defined $feeder->{default_hostgroups} or not scalar keys %{ $feeder->{default_hostgroups} } ) {
        $message = "Config error - No default hostgroups given - check the default_hostgroups hash property is present and non empty.";
        #$feeder->report_feeder_error($message);
        ${$error_ref} = $message;
        return 0;
    }
    
    # TBD check that each default_hostgroups entry is a valid GW string
    # TBD Waiting for dev input on what defines a valid hg name

    # TBD check that each contstain_to_hostgroups entry is a valid GW string
    # TBD Waiting for dev input on what defines a valid hg name

    # Check full_update_frequency is sane
    if ( $feeder->{full_update_frequency} < 0 ) { 
        $message = "Config error - full_update_frequency should be a positive number - check the feeder configuration.";
        #$feeder->report_feeder_error($message);
        ${$error_ref} = $message;
        return 0;
    }

    # Check host_bundle_size is sane
    if ( $feeder->{host_bundle_size} < 0 ) { 
        $message = "Config error - host_bundle_size should be a positive number - check the feeder configuration.";
        #$feeder->report_feeder_error($message);
        ${$error_ref} = $message;
        return 0;
    }

    # Check hostgroup_bundle_size is sane
    if ( $feeder->{hostgroup_bundle_size} < 0 ) { 
        $message = "Config error - hostgroup_bundle_size should be a positive number - check the feeder configuration.";
        #$feeder->report_feeder_error( $message );
        ${$error_ref} = $message;
        return 0;
    }

    # Check service_bundle_size is sane
    if ( $feeder->{service_bundle_size} < 0 ) { 
        $message = "Config error - service_bundle_size should be a positive number - check the feeder configuration.";
        #$feeder->report_feeder_error($message);
        ${$error_ref} = $message;
        return 0;
    }

    # Check retry_cache_max_age is sane
    if ( $feeder->{retry_cache_max_age} < 0 ) { 
        $message = "Config error - retry_cache_max_age should be a positive number - check the feeder configuration.";
        #$feeder->report_feeder_error( $message );
        ${$error_ref} = $message;
        return 0;
    }

    # Good enough for now.
    return 1;

}

# ---------------------------------------------------------------------------------
sub check_system_indicator
{
    # Looks for an indicator from the system telling the feeder to go ahead and start processing/sync'ing.
    # For now, this is simply existence of a flag file which is created via the cacti_cron.sh cron script.
    # Returns 1 if the indicator is positive, 0 otherwise

    if ( -e $master_config->{system_indicator_file} ) {
        return 1;
    }
    return 0;
}

# ---------------------------------------------------------------------------------
sub reset_system_indicator
{
    # Resets the system indicator.
    # For now this just means delete the flag file.
    # If running in -every mode, indicator file prob'y won't exist most of the time.
    # Returns 1 if the reset worked, 0 otherwise.

    if ( -e $master_config->{system_indicator_file} and not unlink $master_config->{system_indicator_file} ) {
        #$feeder->report_feeder_error("CACTI SYSTEM INTEGRATION ERROR Could not remove cacti system indicator file $feeder->{system_indicator_file} : $!");
        $logger->error("Could not remove system indicator file $master_config->{system_indicator_file} : $!");
        return 0;
    }
    else {
        $logger->debug("Resetting system indicator by removing file $master_config->{system_indicator_file}");
    }
    return 1;

}

# ---------------------------------------------------------------------------------
sub system_requirements_met
{
    # Checks for the following requirements :
    #  - specific version of cacti
    #  - existence of thold plugin
    #  - specific version of thold plugin
    # Takes ref to a cacti version var which it will populate
    # Returns 0 if any of these requirements are not met, 1 if all are met

    my ( $cacti_version_ref ) = @_;
    my ( $sth );

    # get the version of cacti
    $$cacti_version_ref = get_cacti_version();

    # check the version of cacti
    if ( not exists $supported_cacti_versions{$$cacti_version_ref} ) {
        # There's no Feeder object in play at this point since this routine is called from get_data(), so don't waste time calling report_feeder_error()
        #$feeder->report_feeder_error("ERROR Cacti version $$cacti_version_ref is not yet supported by this feeder");
        $logger->error("Cacti version '$$cacti_version_ref' is not yet supported by this feeder");
        return 0;
    }

    # Check that the thold plugin is installed by checking for existence of thold tables
    $sth = $database_handle->table_info('', '', 'thold_%', 'TABLE');
    if ( not $sth->fetch) {
        # There's no Feeder object in play at this point since this routine is called from get_data(), so don't waste time calling report_feeder_error()
        #$feeder->report_feeder_error("ERROR Cacti doesn't appear to have the thold plugin installed - no thold tables were found in the cacti database");
        $logger->error("Cacti doesn't appear to have the thold plugin installed - no thold tables were found in the cacti database");
        return 0;
    }

    # TBD check for the right version of the thold plugin via the database, not the cli.
    # Since there's no thold_version table yet, marking this as to-do

    return 1;
}

# ---------------------------------------------------------------------------------
sub generate_query
{
    # Generates a query upon to use in get_data() - this data drives the entire feeder.
    # Takes a cacti version string which is used to determine which query to return.
    # Returns 1 on success, 0 otherwise
    my ( $query_ref, $cacti_version ) = @_;

    # Create the query based on the version
    if ( $cacti_version eq '0.8.7g' ) {
        # This is the version shipped with GW 7.0.2.
        # The original queries returned a lot of columns which are never used.
        # Cleaned them up to only return the minimum data that will be required to get the job done. 
        # This becomes more important when using the retry caches which store all raw data from these queries. 
        # The thold_data.* was the issue.

        # Original query :
        # $$query_ref= "SELECT    thold_data.*, host.description, host.status
        #         FROM      thold_data  LEFT JOIN  host  ON  thold_data.host_id=host.id
        #         WHERE     thold_enabled='on'  OR  bl_enabled='on'
        #         ORDER BY  thold_alert DESC, bl_alert DESC, host.description, rra_id ASC;";
    
        # cleaned up query :
        $$query_ref= "SELECT 
                                  thold_data.bl_alert,
                                  thold_data.bl_enabled,
                                  thold_data.bl_fail_count,
                                  thold_data.bl_fail_trigger,
                                  thold_data.bl_pct_down,
                                  thold_data.bl_pct_up,
                                  thold_data.host_id,
                                  thold_data.lastread,
                                  thold_data.name,
                                  thold_data.thold_alert,
                                  thold_data.thold_enabled,
                                  thold_data.thold_fail_count,
                                  thold_data.thold_fail_trigger,
                                  thold_data.thold_hi,
                                  thold_data.thold_low,
                                  host.description,
                                  host.status
                      FROM        thold_data  
                      LEFT JOIN   host  
                      ON          thold_data.host_id=host.id
                      WHERE       thold_enabled='on'  OR  bl_enabled='on'
                      ORDER BY    thold_alert DESC, bl_alert DESC, host.description, rra_id ASC;";
        return 1;
    }
    elsif ( $cacti_version eq '0.8.8b' ) { # leaving this in here for reference, but only supporting latest 
        # TBD this query needs cleaning up in the same way as the 087g version above.
        # In this version 0.8.8b :
        # - thresholds have a type : 0 for hi/low, 1 for baseline deviation, 2 for time based
        # - thresholds can be enabled or not, regardless of type
        $$query_ref = "SELECT    thold_data.*, host.description, host.status
                FROM      thold_data  LEFT JOIN  host  ON  thold_data.host_id=host.id
                WHERE     thold_enabled='on'
                ORDER BY  thold_alert DESC, bl_alert DESC, host.description, rra_id ASC;";
        return 1;
    }
    elsif ( $cacti_version eq '0.8.8f' or $cacti_version eq '0.8.8h' ) { # v 2.0.9 : this version should be the latest Cacti version available, and the query should be checked that it works with that version!
        # This is the latest version at the time this comment is being written. 
        $$query_ref = "SELECT    
				thold_data.bl_alert,
                                thold_data.bl_fail_count,
                                thold_data.bl_fail_trigger,
                                thold_data.bl_pct_down,
                                thold_data.bl_pct_up,
                                thold_data.host_id,
                                thold_data.lastread,
                                thold_data.name,
                                thold_data.thold_alert,
                                thold_data.thold_enabled,
                                thold_data.thold_fail_count,
                                thold_data.thold_fail_trigger,
                                thold_data.thold_hi,
                                thold_data.thold_low,
                                thold_data.thold_type,
				host.description, 
				host.status
			FROM	thold_data
		   LEFT JOIN    host
			  ON    thold_data.host_id=host.id
                       WHERE    thold_enabled='on'
                    ORDER BY    thold_alert DESC, bl_alert DESC, host.description, rra_id ASC;";
	
        return 1;
    }
    else {
        # A query needs writing for this the version of cacti
        # There's no Feeder object in play at this point since this routine is called from get_data(), so don't waste time calling report_feeder_error()
        #$feeder->report_feeder_error("ERROR No suitable cacti query has been defined yet for Cacti version $cacti_version");
        $logger->error("No suitable cacti query has been defined yet for Cacti version $cacti_version");
        $$query_ref = undef;
        return 0;
    }
}

# ---------------------------------------------------------------------------------
sub get_cacti_version
{
    # Looks in the cacti database version table for the cacti version.
    # Returns that version, or undef if couldn't find the version.
    # There's no Feeder object in play at this point since this routine is called from get_data(), so don't waste time calling report_feeder_error()
    

    my $query = "select * from version;";

    $logger->trace("get_cacti_version() SQL = '$query'");

    my $sqlQuery  = $database_handle->prepare($query) or do { 
        #$feeder->report_feeder_error( "SQL ERROR Can't prepare $query: " . $database_handle->errstr); 
        $logger->error( "Can't prepare $query: " . $database_handle->errstr); 
        return "Unknown"; 
    };

    $sqlQuery->execute or do { 
        #$feeder->report_feeder_error("SQL ERROR Can't execute the query $query : " . $sqlQuery->errstr); 
        $logger->error("Can't execute the query $query : " . $sqlQuery->errstr); 
        return "Unknown"; 
    };

    my @rows = $sqlQuery->fetchrow_array();

    if ( not @rows ) {
        #$feeder->report_feeder_error("ERROR No cacti version was defined in the cacti version table.");
        $logger->error("No cacti version was defined in the cacti version table.");
        return "Unknown";
    }
    $logger->trace("Cacti version : '$rows[0]'");
    return $rows[0];
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
        my $system_indicator_file            = $master_config->get_scalar(  'system_indicator_file' );
        my $endpoint_max_retries             = $master_config->get_number(  'endpoint_max_retries' );
        my $endpoint_retry_wait              = $master_config->get_number(  'endpoint_retry_wait' );
        my $retry_cache_directory            = $master_config->get_scalar(  'retry_cache_directory' );
        my $cactidbtype                      = $master_config->get_scalar(  'cactidbtype' );
        my $cactidbhost                      = $master_config->get_scalar(  'cactidbhost' );
        my $cactidbport                      = $master_config->get_number(  'cactidbport' );
        my $cactidbname                      = $master_config->get_scalar(  'cactidbname' );
        my $cactidbuser                      = $master_config->get_scalar(  'cactidbuser' );
        my $cactidbpass                      = $master_config->get_scalar(  'cactidbpass' );
        my $check_thold_fail_count           = $master_config->get_boolean( 'check_thold_fail_count' );
        my $check_bl_fail_count              = $master_config->get_boolean( 'check_bl_fail_count' );
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
    

    return 1;
}

# -------------------------------------------------------------
sub terminate_rest_api
{
    ## Release our handle to the REST API (if we used it), to force the REST API to call its destructor.
    ## This will attempt to log out before Perl's global destruction pass wipes out resources needed for
    ## logout to work properly.
    foreach my $feeder_object ( keys %feeder_objects ) { 
        $feeder_objects{$feeder_object}->{rest_api} = undef;
    }
}

__END__


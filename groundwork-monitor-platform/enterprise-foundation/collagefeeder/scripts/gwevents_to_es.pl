#!/usr/local/groundwork/perl/bin/perl

# Gets events (i.e., logmessages) from Foundation, and feeds them into Elasticsearch.
#
# Copyright (c) 2014 GroundWork, Inc. (www.gwos.com).  All rights reserved.
# Use of this software is subject to commercial license terms.
#
# Revision History
# v 0.9.0  - Dominic Nicholas 2/14 - Initial Revision
# v 0.9.1  - Dominic Nicholas 2/14 - Minor fixes / tweaks
# v 0.9.2  - Dominic Nicholas 2/14 - added health service and report_daemon_error()
# v 0.9.3  - Kevin Stone      3/14 - @timestamp, feeder_version
# v 0.9.42 - Dominic Nicholas 3/14 - mappings, hostgroups, rest_api_user/password, removed id from event
# v 0.9.43 - Dominic Nicholas 3/14 - fixed bug: missing service in generated test events
# v 0.9.44 - Dominic Nicholas 3/14 - fixed bug: health host/services need to be appType other than NAGIOS so commits don't remove it
# v 0.9.45 - Dominic Nicholas 3/14 - once added to aid in debugging mappings issue and mapping stuff fixed (create index with mapping, then bulk index)
# v 1.0.0  - GH 2014-09-04 - upgraded to current GW::RAPID package; general cleanup
# v 2.0.0  - DN 2015-05-05 - refactored to use Feeder.pm and robustness 
# v 2.0.1  - DN 2015-06-15 - fixed initialize_elasticsearch() to use array of nodes properly
# v 2.0.2  - DN 2015-06-19 - various minor mods such as don't init health objs if feeder disabled; moved -test invocation 
#                            to after feeder object instantiation; update_feeder_stats update; rename from elastic_scanner.pl; etc
# v 2.0.3  - DN 2015-06-20 - updates to upsert_daemon_service() to create events and post notifications etc
# v 2.1.0  - DN 2016-02-17 - refactoring for multi endpoints, and improved movement of errors up into health service etc
# v 2.1.1  - DN 2016-06-13 - initialize_elasticsearch() called before elasticsearch_nodes is defined - fixed, plus more debug added to that routine
# v 2.1.2  - GH 2016-08-25 - Die automatically after running for an hour, as a temporary fix to work around memory leaks (GWMON-12626), to be restarted by the parent supervise.
# v 2.1.3  - DN	2016-09-02 - pulled out 2.0.2 work - it risks exhausting the server-side auth token cache due to known issues with /api/logout garbage cleanup. Instead,
# 			     the last mod time is monitored and the master config only read when the mtime has changed.
# v 2.1.4  - DN 2016-11-1  GWMON-12786 better handle bulk send errors to ES
# v 2.1.5 -  DN 2017-01-23 GWMON-12867 Configuration refactored to better support x-pack (user/password authenticated elasticsearch sessions and SSL)
# v 2.1.6 -  DN 2017-08-07 send_events_to_elasticsearch() updated to check for index existence before trying to create index
#
# TODO:
# - revisit signal handling
# - consider Storable qw(dclone); for use in send_events_to_elasticsearch()
# - move some of the routines out of here into Feeder.pm - see TODO tags
# - fix TBD in process_log_messages()
# - fix other outstanding TODO's, FIX MINOR and FIX MAJOR's

use 5.0;
use strict;
use warnings;
use version;
my $VERSION = qv('2.1.6');
use GW::Feeder qv('0.5.4');
use JSON;
use Getopt::Long;
use POSIX qw(strftime);
use Sys::Hostname;
use Time::HiRes qw(usleep);
use Time::Local;
use Log::Log4perl qw(get_logger);
use TypedConfig;
use List::Util qw(max);
use Search::Elasticsearch;
use File::Basename;
use Data::Dumper; $Data::Dumper::Indent = 2; $Data::Dumper::Sortkeys = 1;

our $feeder_name = "gwevents_to_es";
our $master_config; # want to be able to access some of the endpoint-independent config from the Feeder module too such as retry_cache_limits
my $master_config_file = '/usr/local/groundwork/config/gwevents_to_es.conf'; # Main config file for this feeder
our ( $logger, $log4perl_config, $logfile, %feeder_objects );
my $config_file = '/usr/local/groundwork/config/gwevents_to_es.conf';
my ( $feeder, $disabled_notice_given  ) = undef;
my ( $clean, $every, $help, $show_version, $testbatch, $once, $yes ) = undef; # CLI option vars
my $elastic_search_object;    # global elasticsearch api object

# health/stats service names
my $service_last_event_id_processed = "$feeder_name.last.event.id.processed";
my $service_test_events_service     = "$feeder_name.test.events";

my %feeder_services = (
     "$feeder_name.cycle.elapsed.time"              => "Time taken to process last cycle",
     "$feeder_name.events.retrieved.on.last.cycle"  => "Count of events retrieved on last cycle",
     "$feeder_name.events.sent.on.last.cycle"       => "Count of events sent on last cycle",
     "$feeder_name.events.retrieved.per.minute"     => "Events retrieved per minute",
     "$feeder_name.events.sent.per.minute"          => "Events sent per minute",
     "$feeder_name.test.events.service"             => "For event generation events"
);

# ============================================================= 
main();
# ============================================================= 

# ------------------------------------------------------------------------------------------------------------
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

# ============================================================= 
sub main 
{
    ## main sub that does initialization, processing loop, etc.

    my $fresh_start_time = time();

    my ( $started_message, $started_at, $cycle_count, $disabled_notice_given, %feeder_options, $start_time,
         %metrics, $endpoint, $endpoint_name, $endpoint_config, $error, $error_message, $endpoint_health_hostgroup,
         $validation_error, $try, $max_retries, $processing_error, $ipe_error,
         $total_cycle_time_taken, $events_retrieved, $events_sent, $retrieve_time, $send_time, $finished_time, $endpoint_enabled
    );

    $started_at = localtime;
    
    # v2.1.3 : This will be used to control re-reads in the main CYCLE loop. Seeded with 0 to force the first read.
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

    # process log messages in a never-ending cycle
    $cycle_count = 1; $disabled_notice_given = 0;
    CYCLE: while ( 1  )
    {
        $logger->info( ">>>>>>>> Starting cycle $cycle_count <<<<<<<<" ) if not $disabled_notice_given;

	# v2.1.3 Only (re)read the master config if the mast mod time changed
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
                cxn                                    => 'hash',
               #elasticsearch_nodes                    => 'array',  # v2.1.5 this is deprecated by cxn
                origin                                 => 'scalar',
                number_of_test_events                  => 'number',
                batch_size                             => 'number',
                initially_process_everything           => 'boolean',
                host_bundle_size                       => 'number',
                hostgroup_bundle_size                  => 'number',
                retry_cache_max_age                    => 'number',
                service_bundle_size                    => 'number',
            }
        );
        # Set up interrupt handlers for various signals.  Notification of the interrupt will attempted to be sent to each endpoint.
        # This could use some more work since it doesn't work that well if the feeder is waiting for something to do.
        initialize_interrupt_handlers( $master_config->{endpoint}, \%feeder_options);

        # NOTE about retry caching for this feeder ... there isn't any. This is because the logmessages are in gwcollagedb.logmessage
        # on the server on which this feeder is running. It's both a bad idea to store possibly 10's of thousands or more logmessages
        # into the file cache and redundant.

        # v 2.1.1 - elasticsearch_nodes (in 2.1.5 onwards, this is deprecated by cxn) are not defined yet. This has to move to after $feeder is defined.
        # initialize esearch
        #if ( not initialize_elasticsearch() ) {
        #    # Can't really send this into GW for viewing in status viewer because nothing gets processed including possibly creating feeder objects (ie next CYCLE)
        #    $logger->error( "Failed to initialize elasticsearch - waiting for a bit then restarting cycle" ); 
        #    sleep 10;
        #    next CYCLE;
        #}
    
        # Initialize the metrics data structure
        %metrics = ( );

        # Synchronize each endpoint, in the order they are specified in the master config file
        ENDPOINT: foreach $endpoint ( @{$master_config->{endpoint}}  ) {

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
                if ( $try > $max_retries ) {
                    $error_message = "Feeder host $GW::Feeder::feeder_host : Couldn't create feeder object for endpoint '$endpoint_name'.";

                    # Add the error coming back from GW::Feeder (possibly passed along from RAPID) too here. This is useful in the case of say the ws_client.props not being readable. This 
                    # way it'll actually make it back into the status viewer - easier than tracking it down from the log file.
                    if ( defined $@ ) { 
                        $@ =~ s/\n//g ;
                        $error_message .= $@ ;
                    }
                    stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $error_message, 'general_errors');
                    next ENDPOINT;
                }

                $feeder_objects{$endpoint_name} = $feeder;
            }
            else  {
                # A feeder object already exists so use it.
                # If the auth token expires, RAPID will re-auth it and during the initialize_health_objects step later
                $feeder = $feeder_objects{$endpoint_name} ;
            }
        
            # v 2.1.1 - elasticsearch_nodes (in 2.1.5 onwards, this is deprecated by cxn) are now defined 
            # initialize esearch
            if ( not initialize_elasticsearch() ) {
                $error_message = "Failed to initialize elasticsearch - waiting for a bit then restarting cycle"; 
                stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $error_message, 'general_errors');
        	next ENDPOINT;
            }

            # Do some final value sanity checking of feeder-specific configuration
            if ( not validate_feeder_specific_options( \$validation_error ) ) {
                $error_message = "Feeder host $GW::Feeder::feeder_host : A problem was found in endpoint '$endpoint_name' configuration that needs fixing. $validation_error";
                stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $error_message, 'general_errors' );
                next ENDPOINT;
            }

            if ( $clean ) {
                $feeder->cleanup( $yes, 1 ) ;
                next ENDPOINT;
            }

            # Initialize a feeder hostgroup, virtual feeder host, feeder health service, and any other feeder specific services as defined in <feeder_services> conf hash
            if ( not $feeder->initialize_health_objects( $started_message ) ) {
                # Don't continue for now since failing to do this simple step indicates a bigger issue probably. 
                # For example, if there was a license error (eg not installed) or a general REST breakdown.
                $error_message = "Feeder host $GW::Feeder::feeder_host : An error occurred initializing feeder health objects - ending processing attempt for this endpoint.";
                stage_error_for_publishing_via_metrics($logger, \%metrics, $endpoint_name, $error_message, 'general_errors');
                next ENDPOINT;
            }

            # Create a batch if test events if ncessary. This is designed to be run in interactive mode.
            if ($testbatch) {
	            $logger->info("---- Generating $testbatch test events ----"); 
	            if ( not generate_test_events($testbatch) ) { 
	                $logger->error("Failed to create batch of test events.");
                }
	            $logger->info("---- Done - quitting ----");
	            exit;
            }

            # This also seeds the last_event_id_processed service - important!
            # See also the initially_process_everything config option.
            if ( not initialize_process_everything( \$ipe_error ) ) {
                $error_message = "Feeder host $GW::Feeder::feeder_host : An error occurred initializing $service_last_event_id_processed service.";
                $error_message .= $ipe_error if defined $ipe_error;
                stage_error_for_publishing_via_metrics($logger, \%metrics, $endpoint_name, $error_message, 'general_errors');
                next ENDPOINT;
            }

            # Create a batch of test events if necessary
	        if ( $feeder->{properties}->{number_of_test_events} > 0 ) { 
                if ( not generate_test_events($feeder->{properties}->{number_of_test_events} ) ) { 
                    $error_message = "Failed to create a batch of test events." ;
                    stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $error_message,  'general_errors');
                }
            }

            # Init feeder statistics 
            $events_retrieved = $events_sent = $retrieve_time = $send_time = 0;

            # process the log messages into es 
            if ( not process_log_messages( \$events_retrieved, \$events_sent, \$retrieve_time, \$send_time, \$processing_error ) ) {
                $error_message = "Feeder host $GW::Feeder::feeder_host : Feeder failed to successfully process log messages.";
                $error_message .= $processing_error if defined $processing_error;
                stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $error_message,  'general_errors');
            }
            # Also, some errors might be noteworthy but weren't enough to stop processing. These might be in processing_error, even tho
            # process_log_messages returned ok
            else { 
                if ( defined $processing_error and $processing_error ne "" ) { 
                    $error_message = "Feeder host $GW::Feeder::feeder_host : Feeder encountered some problems processing log messages : $processing_error";
                    stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $error_message,  'general_errors');
                }
            }


            # This is where removal of metrics services would need to happen to cover the case of a renamed endpoint in the master config
            my $remove_error;
            if ( not remove_feeder_objects_from_foundation( \$remove_error ) ) {
                $error_message .= "Feeder host $GW::Feeder::feeder_host : Feeder had a problem removing objects from foundation. ";
                $error_message .= $remove_error if defined $remove_error;
                stage_error_for_publishing_via_metrics( $logger, \%metrics, $endpoint_name, $error_message,  'general_errors');
            }

            # converts audit into events in foundation, and empties it
            $feeder->flush_audit() ;  
    
            # Build feeder metrics - these can be changed based on errors that occurred earlier, in send_metrics
            $total_cycle_time_taken = sprintf "%0.2f", Time::HiRes::time() - $start_time;
            $finished_time = localtime();
	        update_feeder_stats( $cycle_count,
                                 $finished_time,
                                 $total_cycle_time_taken,
                                 $events_retrieved,  
                                 $events_sent,   
                                 $retrieve_time,     
                                 $send_time,
                                 \@{ $metrics{endpoints}{$endpoint_name}{services} } 
                             );



            # If there's a Feeder api object successfully created then it meant that the REST API endpoint was up. 
            # Put a reference to this Feeder object into %metrics for use later
            $metrics{endpoints}{$endpoint_name}{feeder_object} = $feeder;

        } # ENDPOINT

        # If -clean opt was supplied, just quit
        exit if $clean;

        # Send metrics out to all endpoints - this also send perf data to the metrics services if defined.
        if ( not send_metrics( \%metrics, \$error, $logger, "nocaching" ) ) {
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

    } # end CYCLE
}

# ------------------------------------------------------------- 
sub terminate_rest_api {
    ## Release our handle to the REST API (if we used it), to force the REST API to call its destructor.
    ## This will attempt to log out before Perl's global destruction pass wipes out resources needed for
    ## logout to work properly.
    #$feeder->{rest_api} = undef;
    foreach my $feeder_object ( keys %feeder_objects ) {
        $feeder_objects{$feeder_object}->{rest_api} = undef;
    }
}

# ------------------------------------------------------------- 
sub process_log_messages 
{
    ## gets recent log messages (events) and sends them into elasticsearch
    ## Gets a bundle of them from foundation, and sends that bundle bulk-fashion
    ## into elasticsearch. The last logmessage processed is stored in a service result in foundation.
    ## returns 1 on success, 0 otherwise

    my ( $ref_events_retrieved, $ref_events_sent, $ref_retrieve_time, $ref_send_time, $error_ref ) = @_;
    my ( $last_event_id, $last_processed_event_id, $start_id, $end_id, %outcome, %results, $gappy );

    my $events_retrieved                = 0;
    my $events_sent                     = 0;
    my $total_count_of_events_retrieved = 0;
    my $total_count_of_events_sent      = 0;
    my $send_time                       = 0;
    my $retrieve_time                   = 0;
    my $send_status;

    $total_count_of_events_retrieved = $total_count_of_events_sent = 0;
    $$ref_events_retrieved = $$ref_events_sent = $$ref_retrieve_time = $$ref_send_time = 0;
    $$error_ref = "";

    $logger->debug("Processing Foundation log messages aka events");

    # get the last logmessageid available
    if ( not get_last_available_logmessageid( \$last_event_id, $error_ref ) ) {
	    return 0;
    }
    $logger->debug("The id of last available event in the logmessage table is $last_event_id");

    # get the id of the last event processed by this app
    if ( not get_last_processed_event_id( \$last_processed_event_id, $last_event_id, $error_ref ) ) {
	    return 0;
    }
    $logger->debug("The id of the event last processed by this app was '$last_processed_event_id'");

    # If there are no new events, then bail nicely.
    if ( $last_processed_event_id == $last_event_id ) {
	    $logger->debug("All up to date.");
	    return 1;
    }

    # process the events in the required range, doing it a chunk at a time as defined by batch_size
    $logger->debug( "Process events with ids in range " . ( $last_processed_event_id + 1 ) . " .. $last_event_id" );
    while ( $last_processed_event_id <= $last_event_id ) {
	    ## get next batch of events from foundation
	    $start_id = $last_processed_event_id + 1;    # makes for more readable code, +1 because we don't want to reprocess last event
	    $end_id   = $start_id + $feeder->{properties}->{batch_size} - 1;     # makes for more readable code, -1 because we're working with indices

	    # don't try to process more events than there actually are
	    $logger->debug("Next range: $start_id .. $end_id");
	    if ( $end_id > $last_event_id ) {
	        $end_id = $last_event_id;
	        $logger->debug("Range end id reset to $end_id");
	    }

	    # Get the events for the range and check for errors
	    $logger->debug("Getting foundation log messages with ids in range $start_id .. $end_id");
	    $retrieve_time = Time::HiRes::time();

	    if ( not $feeder->{rest_api}->get_events( [], { query => "id >= $start_id and id <= $end_id" }, \%outcome, \%results ) ) {
	        # There could be gaps in the event id range from purges for example, so it's possible that this batch is empty.
	        # Also, get_events will throw it's own WARNING if there are no events found for the query;
	        # these can be quietened down by changing the RAPID log level using GW_RAPID_log_level in the main conf.
	        #${$error_ref} = "WARNING Could not get events $start_id .. $end_id from Foundation: " ;    # Don't report on this - could be an empty batch due to gaps
	        if ( defined $outcome{response_error} and $outcome{response_error} =~ /Events not found for given event query/ ) {
		        $logger->debug("An empty batch - gappy logmessageid sequence");
	        }
	        else {
		        ## some other error occurred - log it and stop processing events (FIX MINOR: refine this later if necessary)
		        ## $logger->error( "ERROR getting events - quitting processing of events: " . to_json( \%results, {  utf8 => 1 , pretty => 1} ) );
		        #report_daemon_error( "Error getting events - quitting processing of events: " . to_json( \%results, { utf8 => 1, pretty => 1 } ) );
                        ${$error_ref} .= "Error getting events - quitting processing of events: " . to_json( \%results, { utf8 => 1, pretty => 1 } );
		        return 0;
	        }
	        ## FIX MAJOR:  Here we're making a bald assumption that in spite of the failure of the last retrieval,
	        ## we ought to move on and try to retrieve the next batch.  But what if the problem was due to an
	        ## inability to contact the server, for instance, so we shouldn't really be skipping ahead now?
	        ## This is a critical decision point in the logic of this routine.
                # TBD agree - needs fixing
	        $last_processed_event_id += $feeder->{properties}->{batch_size};
	        next;
	    }
	    else {
	        # find how many events were actually retrieved
	        # need this for purged logmessage table, e.g., first logmessageid = 2000, but last processed ptr not yet set at all
	        $events_retrieved = scalar keys %results;
	    }
	    $retrieve_time = Time::HiRes::time() - $retrieve_time;
    
	    # highlight if didn't get a full batch - case of gappy logmessage table
	    #if ( $events_retrieved < ( $end_id - $start_id ) ) {
	    #    $logger->debug("Only retrieved $events_retrieved events.");
	    #}
	    #else {
	    #    $logger->debug("Retrieved $events_retrieved events");
	    #}
            # Gaps are ok. Just report on how many events were retrieved and avoid confusing warnings
	    $logger->debug("Retrieved $events_retrieved events");
        
    
	    # send that batch to elasticsearch if non-empty
	    $send_time = Time::HiRes::time();
            # If some events were retrieved, try and send them on to es. $error_ref could pick up some errors here.
	    $send_status = $events_retrieved ? send_events_to_elasticsearch( \%results, \$events_sent, $error_ref ) : 0;
	    $send_time = Time::HiRes::time() - $send_time;

	    # update stats for health services
	    $total_count_of_events_retrieved += $events_retrieved;
	    $total_count_of_events_sent      += $events_sent;
    
	    # increment $last_processed_event_id by batch size
	    # Note that 'last processed' is a bit misleading as there's a chance it wasn't actually
	    # processed properly during the send to elasticsearch.
	    # Only update the range if things sent without a bad error with the bulk send.
	    # Will have to see how this works out in the field and refine if necessary if get stuck in loops.

	    if ( $send_status == 1 ) {
	        $last_processed_event_id = highest_event_id( \%results );
	        $logger->debug("Set last_processed_event_id = $last_processed_event_id");
	        # update a health service with the last processed event id - important!
	        update_last_event_id_service( $service_last_event_id_processed, $last_processed_event_id, $error_ref );
	    }
	    else { 
                # v 2.1.4. Changed this so that 
                # - the elasticsearch error isn't sent into foundation because it can be huge and full of special chars which might break foundation
                # - put the error out to the log file
	        $logger->error( ${$error_ref} ); # log the full error to the log file
	        ${$error_ref} = "An error occurred sending GroundWork events to Elasticsarch. The error was logged to the feeder logfile."; # reset the error to be a shorter potentially safer version for Foundation
	    }
    
	    # v2.1.4 only do this if successully sent the events to es - so this was moved into the if-then clause above
	    # update a health service with the last processed event id - important!
	    #update_last_event_id_service( $service_last_event_id_processed, $last_processed_event_id, $error_ref );
    
	    # if the calculated event batch end range is bigger than the actual available events (because of the batch size), then stop trying to process events
	    last if ( $end_id >= $last_event_id );

            # v2.1.4 : bail the loop if weren't able to bulk sent to es. 
            # At this point $error_ref will be set, and is now available for propogation back into the health service.
            last if not $send_status;

    } # chunked get-send processing loop

    # metrics
    ( $$ref_events_retrieved, $$ref_events_sent, $$ref_retrieve_time, $$ref_send_time ) = ( $total_count_of_events_retrieved, $total_count_of_events_sent, $retrieve_time, $send_time );

    return 1;
}

# -------------------------------------------------------------
sub get_last_processed_event_id 
{
    # get the id of the last event processed by this app
    # takes:
    #   - a reference to a scalar in which to put the result
    #   - the id of the last event in logmessage table
    #   - an error ref, populated here
    # result is the id, or null if not set 
    # returns 1 on success, 0 on failure with $error_ref set
    my ( $ref_last_processed_event_id, $last_event_id, $error_ref ) = @_;
    my ( %outcome, %results);

    # get the details for the service which has the last processed event id

    # FIX LATER:  standardize the error reporting from %outcome and %results
    if ( not $feeder->{rest_api}->get_services( [$service_last_event_id_processed], { hostname => $feeder->{properties}->{health_hostname} }, \%outcome, \%results ) ) {
	    ## $logger->error( "ERROR Couldn't get service '$service_last_event_id_processed' for host '$health_hostname': "
	    ##     . to_json( \%outcome, { utf8 => 1, pretty => 1 } ) );
        ${$error_ref} = "Couldn't get service '$service_last_event_id_processed' for host '$feeder->{properties}->{health_hostname}': " . to_json( \%outcome, { utf8 => 1, pretty => 1 } ) ;
	    #report_daemon_error( "Couldn't get service '$service_last_event_id_processed' for host '$feeder->{properties}->{health_hostname}': " . to_json( \%outcome, { utf8 => 1, pretty => 1 } ) );
	    ${$ref_last_processed_event_id} = undef;
	    return 0;
    }

    if (%results) {
	    ## The service property 'LastPluginOutput' contains where the last processed event id is stored.
	    ## If it's '[pending]' then this is the first time the app has run.
	    my $lastpluginoutput = ( values %results )[0]->{properties}{LastPluginOutput};
	    if ( $lastpluginoutput eq '[pending]' ) {
	        ${$ref_last_processed_event_id} = 0;    # set to 0 will add one to it later when calculating range START
	    }
	    else {
	        ${$ref_last_processed_event_id} = $lastpluginoutput;
	    }
    }
    else {
	    #report_daemon_error( "Couldn't get service '$service_last_event_id_processed' for host '$feeder->{properties}->{health_hostname}': no result returned from query.");
	    ${$error_ref} =  "Couldn't get service '$service_last_event_id_processed' for host '$feeder->{properties}->{health_hostname}': no result returned from query.";
	    ${$ref_last_processed_event_id} = undef;
	    return 0;
    }

    return 1;
}

# -------------------------------------------------------------
sub send_events_to_elasticsearch 
{
    # Takes a batch of events and sends them in bulk to elasticsearch
    # args
    #   a ref to a hash of logmessages batch which is already populated, with this structure :
    #       <logmessageid> => {  key=>val, ... }
    #   where the keys are logmessage table columns, and the values are the values for that id.
    #   a ref to a counter that will be updated
    #   an error ref
    # returns
    #   1 ok
    #   0 on failure

    my ( $hashref_events_batch, $ref_count_of_events_sent, $error_ref ) = @_;
    my ( $event_ref, $event_property, %esearch_bulk_instructions, @events, %event_properties );
    my ( $es_result, $e_index, $e_type, $sent_count, $count_result );
    my ( @hostgroup_names, @category_names, %mappings, $doc_count );
    my ( %outcome, %results );

    $logger->debug("Sending events to elasticsearch");

    # create the esearch index and type
    $e_index = 'groundwork-' . strftime( "%Y.%m.%d", localtime );    # eg groundwork-2014.02.19
    $e_type = 'foundation_logmessage';
    $logger->debug("Sending events to elasticsearch (index=$e_index, type=$e_type)");

    # Take apart the bulk of retrieved events and copy them into an array of hashes to be used by esearch
    # perhaps use storage::dclone to do deep copying of array of hashes in future.
    foreach $event_ref ( values %{ $hashref_events_batch } ) {
	    @hostgroup_names = @category_names = (); # reset these per event ie per host
	    $logger->debug("Preparing event id $event_ref->{id}");
	    foreach $event_property ( keys %{$event_ref} ) {
	        next if $event_property eq 'id';
	        $event_properties{$event_property} = $event_ref->{$event_property};
	    }

	    # figure out which hostgroups (and categories later - currently disabled ?) the host is in and add that list to the doc
	    if ( $event_properties{host} ) {
	        if ( not $feeder->{rest_api}->get_hostgroups( [], { query => "hosts.hostName = '$event_properties{host}'", depth => 'simple' }, \%outcome, \%results)) {
		        #report_daemon_error("Could not get hostgroups for host '$event_properties{host}'");
                ${$error_ref} .= "Could not get hostgroups for host '$event_properties{host}'";
	        }
	        else {
		        push @hostgroup_names, keys %results;
		        $event_properties{'hostgroups'} = \@hostgroup_names;
	        }

	        # get categories for host - disabled for now - not sure what the intention was any more. Leaving in for future ref.
	        if (0) {
		        if ( not $feeder->{rest_api}->get_categories( [], { query => "hosts.hostName = '$event_properties{host}'", depth => 'shallow' }, \%outcome, \%results)) {
		            #report_daemon_error("Could not get categories for host '$event_properties{ host }'");
		            ${$error_ref} .= "Could not get categories for host '$event_properties{ host }'"; 
		        }
		        else {
		            foreach my $cat_ref ( @{ $results{categories} } ) {
			            print "$cat_ref->{name}\n";
			            push @category_names, $cat_ref->{name};
		            }
		            $event_properties{'categories'} = \@category_names;
		        }
	        }

	    }

	    # Add custom property to show server origin
	    $event_properties{'origin'} = $feeder->{properties}->{origin} ? $feeder->{properties}->{origin} : hostname();

	    # Add a timestamp using last insert time (KDS)
	    if ( defined $event_properties{lastInsertDate} and $event_properties{lastInsertDate} ) {
	        $event_properties{'@timestamp'} = $event_properties{'lastInsertDate'};
	    }

	    # Add a feeder version tag (KDS)
	    $event_properties{'feeder_version'} = "$feeder_name version $VERSION";
    
	    # add metadata
	    push @events, { 'index' => { _index => $e_index, _type => $e_type, _id => $event_ref->{id}, } };
	    ## push @events, { 'create' => { _index => $e_index, _type => $e_type, _id => 2  }} ; # for testing error detection
    
	    # add document
	    push @events, {%event_properties};
    }

    # Moved this to a one-time deal before main loop. Uncomment this to do error testing
    # eg to get No Nodes available or timeouts and trap them with the eval below.
    # $elastic_search_object = Elasticsearch->new( nodes => [ @elasticsearch_nodes ]); # 2.1.5 cxn block is used instead

    # build the elastic search bulk instruction set
    $esearch_bulk_instructions{'index'} = $e_index;
    $esearch_bulk_instructions{'type'}  = $e_type;
    $esearch_bulk_instructions{'body'}  = \@events;

    # FOR TESTING - this will delete the index for sure
    # if ( $elastic_search_object->indices->exists( index => $e_index) ) {
    #     print "Index $e_index exists... deleting it\n";
    #     $elastic_search_object->indices->delete( index => $e_index) ;
    # }
    # exit;

    # The process now is:  create an index with the mappings, then, bulk index to get the docs created...
    # See if the index has been created yet, if it hasn't then create it with mappings, readying it for bulk indexing
    $doc_count = 0;
    eval { $count_result = $elastic_search_object->count( { 'index' => $e_index, 'type' => $e_type } ) };

    # yes the index might not exist yet in which case $@ contains an error about index missing exception
    # for now assuming that getting the count either works or gives an index missing exception just to get this working
    if ( defined $count_result->{count} ) { $doc_count = $count_result->{count}; }

    # if none yet, then create an index
    #if ( $doc_count == 0 ) { 
    if ( $doc_count == 0 and not $elastic_search_object->indices->exists( index => $e_index) ) {  ### v2.1.6 - check for existence before creating
	    ## create mappings - turn off tokenization/analyzer for the following fields of an event doc
	    ## FIX MINOR: need to do this for properties.* and test hostgroups with -'s
	    %mappings = (
	        'index' => $e_index,
	        'body'  => {
		    'mappings' => {
		        $e_type => {
			    'properties' => {
			        'applicationSeverity' => { 'type' => 'string', 'index' => 'not_analyzed' },
			        'component'           => { 'type' => 'string', 'index' => 'not_analyzed' },
			        'device'              => { 'type' => 'string', 'index' => 'not_analyzed' },
			        'host'                => { 'type' => 'string', 'index' => 'not_analyzed' },
			        'monitorServer'       => { 'type' => 'string', 'index' => 'not_analyzed' },
			        'monitorStatus'       => { 'type' => 'string', 'index' => 'not_analyzed' },
			        'operationStatus'     => { 'type' => 'string', 'index' => 'not_analyzed' },
			        'origin'              => { 'type' => 'string', 'index' => 'not_analyzed' },
			        'priority'            => { 'type' => 'string', 'index' => 'not_analyzed' },
			        'service'             => { 'type' => 'string', 'index' => 'not_analyzed' },
			        'severity'            => { 'type' => 'string', 'index' => 'not_analyzed' },
			       #'severity'            => { 'type' => 'string', 'index' => 'not_analyzed' }, # 2.0.2 noticed this is in here twice
			        'typeRule'            => { 'type' => 'string', 'index' => 'not_analyzed' },
			    }
		        }
		    }
	        }
	    );

	    $logger->debug("Creating index with mappings for index $e_index, type $e_type");
	    $logger->trace( "TRACE mappings for $e_index, $e_type are: " . to_json( \%mappings, { utf8 => 1, pretty => 1 } ) );

	    # NOTE https://metacpan.org/pod/Search::Elasticsearch::Client::Direct::Indices#create
	    # Also, note that indices->put_mapping() requires the index to exist first, so better to create the index first .. with the mappings

	    eval { $es_result = $elastic_search_object->indices->create( \%mappings ); };
	    if ($@) {
	        ## if this happens, the main loop will get wedged trying to send the same set of events in over and over
	        #report_daemon_error("Could not create index $e_index, type $e_type, with mappings: $@");
	        ${$error_ref} .= "Could not create index $e_index, type $e_type, with mappings: $@";
	        ${$ref_count_of_events_sent} = 0;
	        ## FIX MINOR:  Well, it does happen, at least in development testing.  So at a minimum we need some sort
	        ## of sleep to slow down the repeated errors, so as not to fill up the log file too fast.  If we otherwise
	        ## address the problem in the large, we may be able to eliminate this sleep.
	        #sleep $cycle_sleep_time;
	        # sleep 5; # See no point in waiting around here
	        return 0;
	    }
	    else {
	        $logger->trace("TRACE Created index $e_index, type $e_type, with mappings");
	    }
    }

    # Perform the bulk index of the events into esearch docs
    # NOTE the bulk indexing can be done a few different ways with the perl api.
    # I am using this: https://metacpan.org/pod/Search::Elasticsearch::Client::Direct#bulk
    # But you could also use this which is probably nicer: https://metacpan.org/pod/Search::Elasticsearch::Bulk
    $logger->debug("Performing bulk index of event docs - index $e_index, type $e_type");
    $logger->trace( "TRACE elasticsearch instructions: " . to_json( \%esearch_bulk_instructions, { utf8 => 1, pretty => 1 } ) );    # tmi ?

    eval { $es_result = $elastic_search_object->bulk( \%esearch_bulk_instructions ); };
    if ($@) {
	    #report_daemon_error("Could not bulk index to elasticsearch: $@");
	    ${$error_ref} .= "Could not bulk index to elasticsearch: $@";
	    ${$ref_count_of_events_sent} = 0;
	    return 0;
    }

    $logger->trace( "TRACE elasticsearch result: " . to_json( $es_result, { utf8 => 1, pretty => 1 } ) );

    # check response data structure for errors and tally up how many were sent
    $sent_count = 0;
    foreach my $itemhash ( @{ $es_result->{items} } ) {
	    ## if ( not defined $itemhash->{create}{error} ) { # for testing error detection ... }
	    if ( not defined $itemhash->{index}{error} ) {    # FIX MINOR: might need to adjust this logic as we learn about possible result structures
	        $sent_count++;
	    }
	    else {
	        ## $logger->error("WARNING possibly failed to send item to elasticsearch: " . to_json( $itemhash, {  utf8 => 1 , pretty => 1}  ) );
	        #report_daemon_error( "WARNING possibly failed to send item to elasticsearch: " . to_json( $itemhash, { utf8 => 1, pretty => 1 } ) );
	        ${$error_ref} .=  "Possibly failed to send item to elasticsearch: " . to_json( $itemhash, { utf8 => 1, pretty => 1 } ) ;
	    }
    }

    # if didn't send the same amount received, log an error
    my $received_count = scalar keys %{$hashref_events_batch};

    if ( $sent_count != $received_count ) {
	    ## $logger->error( "ERROR Received $received_count events, sent $sent_count events to elasticsearch" );
	    #report_daemon_error("Received $received_count events, sent $sent_count events to elasticsearch");
        if ( defined ${$error_ref} ) { 
	        ${$error_ref} .= "Unequal receive/send count : Received $received_count events, sent $sent_count events to elasticsearch";
        }
        else { 
	        ${$error_ref} = "Unequal receive/send count : Received $received_count events, sent $sent_count events to elasticsearch";
        }
    }
    else {
	    $logger->debug("Received $received_count events, sent $sent_count events to elasticsearch");
    }

    # returns the sent count for stats/health
    ${$ref_count_of_events_sent} = $sent_count;

    return 1;
}

# ------------------------------------------------------------- #

sub highest_event_id {
    ## takes a reference to a result hash of events and figures out the highest id (i.e., logmessageid)
    my ($hashref_results) = @_;

    # Since the "id" values are used for keys in the hash, we need not dig further
    # to find the actual "id" key in each event.
    my @eventids = keys %$hashref_results;
    return max(@eventids);
}

# ------------------------------------------------------------- #
sub update_last_event_id_service 
{
    # Create or update a daemon service that is attached to the health vhost
    # args
    #   service name
    #   message
    #   error ref
    # returns 1 if ok, 0 and error ref if not

    my ( $service, $message, $error_ref ) = @_;
    my ( @bizservices, %update, $status );

    $logger->debug("Upserting $feeder_name service '$service' with message \"$message\"");

    $status = 'OK';

    $update{ $feeder->{properties}->{health_hostname} } { hostgroup } = $feeder->{properties}->{health_hostgroup};
    push @{ $update{ $feeder->{properties}->{health_hostname} } {'services'}} ,  
            { 
                service => $service,
                message => $message,
                status  => $status,
	            properties    => { "Notes" => $GW::Feeder::metric_service_meta_tag } # so it doesn't get removed via metrics sending
            };

    push @bizservices, { %update };

    if ( not $feeder->feeder_upsert_bizservices( \@bizservices ) ) {
        ${$error_ref} .= "An error occurred updating service '$service' with message '$message' using Feeder::feeder_upsert_bizservices()."; 
        return 0;
    }

    return 1;
}

# -------------------------------------------------------------
sub generate_test_events 
{
    ## generates a bunch of test events - useful for testing and development purposes
    my ($count) = @_;

    $logger->debug("Generating $count test events");

    my $now = strftime( '%Y-%m-%dT%H:%M:%S', localtime );
    my $tz = strftime( "%z", localtime() );

    my @events = ();
    for ( my $event = 1 ; $event <= $count ; $event++ ) {
	    $logger->debug("Generating test event number $event / $count");

	    push @events, {
	        ## 'consolidationName'   => 'NAGIOSEVENT',  # Disable consolidation to create separate events (logmessage rows).
	        ## Default preprocessing rules in foundation.properties will
	        ## cause these events to show up with a message count of 1
	        ## but at least the message updates.
	        'appType'       => $feeder->{properties}->{app_type},
	        'device'        => $feeder->{properties}->{health_hostname},
	        'host'          => $feeder->{properties}->{health_hostname},
	        'service'       => $service_test_events_service,
	        'monitorStatus' => 'OK', # warning state perhaps uncessary - changing to OK
	        'textMessage'   => "$feeder_name - TEST EVENT - batch $now$tz",
	        'monitorServer' => 'localhost',    # this needs to be localhost
	        'severity'      => 'MINOR',
	        'reportDate'    => "$now$tz",
	        'properties'    => { 'Latency' => '125.0', 'Comments' => "$0 testing" }
	    };
    }

    my %outcome; my @results;

    # FIX LATER:  standardize the error reporting from %outcome and @results
    if ( not $feeder->{rest_api}->create_events( \@events, {}, \%outcome, \@results ) ) {
	    $logger->error( "Could not create $count test events:" . to_json( \%outcome, { utf8 => 1, pretty => 1 } ) );
        return 0;
    }

    return 1;
}

# -------------------------------------------------------------
sub update_feeder_stats
{
    # Logs feeder stats and updates services with metrics too
    # Metrics 
    # Service name                      Message
    # cycle_elapsed_time                Time taken to process last cycle
    # events_retrieved_on_last_cycle    Count of events retrieved on last cycle
    # events_retrieved_per_minute       Events retrieved per minute
    # events_sent_on_last_cycle         Count of events sent on last cycle
    # events_sent_per_minute            Events sent per minute

    my ( $cycle_number,         # the cycle iteration #
         $finished_time,         # the time this call was called - useful to see, rather than just a cycle time
         $total_cycle_time_taken,    # total feeder cycle time
         $events_retrieved,      # num events retrieved on this cycle
         $events_sent,           # num events sent into es on this cycle
         $retrieve_time,         # how long it took to get the events from GW
         $send_time,             # how long it took to send the events into es
         $built_metrics_ref      # reference to an array of built metrics services that will be populated by this routine.
        ) = @_;

    my ( $events_retrieved_per_min, $events_sent_per_min, @built_services, @built_hosts, $formatted_query_time, %hosts_states  );
    $events_retrieved_per_min = $events_sent_per_min = 0;
    my ( $cycle_elapsed_time_msg, $events_retrieved_on_last_cycle_msg, $events_retrieved_per_minute_msg, $events_sent_on_last_cycle_msg, $events_sent_per_minute_msg ,
         $cycle_elapsed_time_stat, $events_retrieved_on_last_cycle_stat, $events_retrieved_per_minute_stat, $events_sent_on_last_cycle_stat, $events_sent_per_minute_stat );

    $send_time = sprintf "%0.4f", $send_time;
    $retrieve_time = sprintf "%0.4f", $retrieve_time;
    
    # Metric service : cycle.elapsed.time
    $cycle_elapsed_time_msg = "Feeder host $GW::Feeder::feeder_host: Cycle $cycle_number finished at $finished_time, total elapsed processing time : $total_cycle_time_taken seconds";
    $cycle_elapsed_time_stat = "OK";

    # Metric service : events_retrieved_on_last_cycle
    $events_retrieved_on_last_cycle_msg = "Feeder host $GW::Feeder::feeder_host: $events_retrieved GroundWork events retrieved in cycle $cycle_number";
    $events_retrieved_on_last_cycle_stat = "OK";

    # Metric service : events_retrieved_per_minute
    $events_retrieved_per_minute_msg = "Feeder host $GW::Feeder::feeder_host: $events_retrieved_per_min/min ($events_retrieved GroundWork events retrieved in $retrieve_time sec)";
    if ( $retrieve_time > 0 ) {
        $events_retrieved_per_min = sprintf( "%d", $events_retrieved * 60 / $retrieve_time );
	    $events_retrieved_per_minute_msg = "Feeder host $GW::Feeder::feeder_host: $events_retrieved_per_min/min ($events_retrieved GroundWork events retrieved in $retrieve_time sec)";
    }
    $events_retrieved_per_minute_stat = "OK";

    # Metric service : events_sent_on_last_cycle
    $events_sent_on_last_cycle_msg = "Feeder host $GW::Feeder::feeder_host: $events_sent GroundWork events sent in cycle $cycle_number";
    $events_sent_on_last_cycle_stat = "OK";

    # Metric service : events_sent_per_minute
    $events_sent_per_minute_msg = "Feeder host $GW::Feeder::feeder_host: 0/min ($events_sent GroundWork events retrieved in $send_time sec)";
    if ( $send_time > 0 ) {
	    $events_sent_per_min = sprintf( "%d", $events_sent * 60 / $send_time ) ;
	    $events_sent_per_minute_msg = "Feeder host $GW::Feeder::feeder_host: $events_sent_per_min/min ($events_sent sent in $send_time sec)";
    }
    $events_sent_per_minute_stat = "OK";

    $logger->debug("Updating feeder statistics");

    # Log metrics
    $logger->info( "$cycle_elapsed_time_msg") if defined $feeder->{cycle_timings};
    $logger->info( "$events_retrieved_on_last_cycle_msg");
    $logger->info( "$events_retrieved_per_minute_msg");
    $logger->info( "$events_sent_on_last_cycle_msg");
    $logger->info( "$events_sent_per_minute_msg");

    @{ $built_metrics_ref } = (  
        {   # Required.
            service => $feeder_name . ".health",
            message => "Feeder host $GW::Feeder::feeder_host: ok", # ok descriptive enough lol !?
            status  => "OK" 
        },
        {
            service => "$feeder_name.cycle.elapsed.time",
            message => "$cycle_elapsed_time_msg",
            status  => $cycle_elapsed_time_stat,
            perfval  => { cycle_elapsed_time => $total_cycle_time_taken }
        },
        { 
            service => "$feeder_name.events.retrieved.on.last.cycle",
            message => $events_retrieved_on_last_cycle_msg,
            status  => $events_retrieved_on_last_cycle_stat,
            perfval => { events_retrieved_on_last_cycle => $events_retrieved }
        },
        { 
            service => "$feeder_name.events.retrieved.per.minute",
            message => $events_retrieved_per_minute_msg,
            status  => $events_retrieved_per_minute_stat,
            perfval => { events_retrieved_per_minute => $events_retrieved_per_min }
        },
        { 
            service => "$feeder_name.events.sent.on.last.cycle",
            message => $events_sent_on_last_cycle_msg,
            status  => $events_sent_on_last_cycle_stat,
            perfval => { events_sent_on_last_cycle => $events_sent_per_min }
        },
        { 
            service => "$feeder_name.events.sent.per.minute",
            message => $events_sent_per_minute_msg,
            status  => $events_sent_per_minute_stat, 
            perfval => { events_sent_per_minute => $events_retrieved }
        },
       #{
       #    service => 'retry.caching',
       #    message => "Ok",
       #    status  => "OK" 
       #}

    );

}

# ------------------------------------------------------------- 
sub read_daemon_config_file 
{
    ## reads the feeder config file
    my ($config_file) = @_;
    my $config;

    $logger->debug("Reading and processing config file: $config_file") if $logger;

    eval {
	    $config                       = TypedConfig->new($config_file);
	    $log4perl_config              = $config->get_scalar('log4perl_config');
	    $logfile                      = $config->get_scalar('logfile');
    };
    if ($@) {
	    chomp $@;
	    $@ =~ s/^ERROR:\s+//i;
	    $logger->error("Cannot read config file $config_file\n    ($@)\n") if $logger;
	    return 0;
    }
    return 1;
}

# ------------------------------------------------------------- #
sub initialize_options 
{
    
    my $helpstring = "
Groundwork Events to Elasticsearch feeder ($feeder_name) - version $VERSION
GroundWork Feeder module version $GW::Feeder::VERSION

Description
    
    $feeder_name periodically scans for new GroundWork events and sends them into elasticserach.
    Events in GroundWork are stored as rows in the gwcollagedb database's logmessage table.
    The elasticsearch index used is groundwork-YYYY.MM.DD, eg groundwork-2014.02.19.
    It keeps track of the last logmessageid by storing it in a special service, $service_last_event_id_processed.
    This service is automatically attached to the feeder health host defined in $config_file.
    One instance of this application runs per GroundWork system that is to feed into elasticsearch.
    This feeder app was originally called 'events_to_elasticsearch.pl'. 
    It was then renamed to elastic_scanner.pl which was confusing since it was not scanning elasticsearch.
    It has now been renamed to ${feeder_name}.pl which describes that it's taking GroundWork events
    and feeding Elasticsearch.

General Configuration

    Configuration of this feeder is done through the $config_file, which is self documenting.

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
    -every <N>    - Run main cycle every N seconds
    -help         - Show this help
    -once         - Run one main cycle and exit
    -testbatch N  - Generate a batch of test events and quit - useful for testing
    -version      - Show version and exit
    -yes          - Assume yes to -clean question

Author
    GroundWork 2016
";

    GetOptions( 
                'clean'         => \$clean, 
                'every=i'       => \$every, 
                'help'          => \$help, 
                'once'          => \$once, 
                'testbatch=i'   => \$testbatch, 
                'version'       => \$show_version, 
                'yes'           => \$yes, 
              ) or die "$helpstring\n";

    if ( defined $help )         { print $helpstring;             exit(0); }
    if ( defined $show_version ) { print "$0 version $VERSION\n"; exit(0); }
}

# -------------------------------------------------------------
sub initialize_elasticsearch
{
    # Wrap this in an eval and trap the error and report on it
    if ( defined $elastic_search_object ) {  # want to force a new connection each time
    	$logger->debug("Undefining elasticsearch object - ending previous es connection");
    	undef $elastic_search_object;
    }

    # v2.1.5 - refactored configuration to more flexibly support X-pack : ssl and authentication for example
    # https://metacpan.org/pod/Search::Elasticsearch::Cxn::HTTPTiny#CONFIGURATION 
    # https://metacpan.org/pod/Search::Elasticsearch::Role::Cxn#node
    
    my %es_opts;

    # pass through connection options if given
    if ( defined $feeder->{properties}->{cxn} ) {
        foreach my $cxn_opt ( keys %{$feeder->{properties}->{cxn}} ) {
	    $es_opts{$cxn_opt} = $feeder->{properties}->{cxn}->{$cxn_opt};
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

    return 1 ;

}

# -------------------------------------------------------------
sub get_last_available_logmessageid 
{
    my ( $ref_last, $error_ref ) = @_;
    my ( $last, %outcome, %results );

    # FIX MINOR:  This is potentially a MASSIVELY inefficient query, if the "id" field
    # (which presumably translates to logmessage.logmessageid) is not indexed.  Test
    # performance against a huge logmessage table.
    # TBD - agree - fix with more efficient query.
    # However, in practice the count = 1 limits things and it hasn't been a problem.
    if ( not $feeder->{rest_api}->get_events( [], { count => 1, query => 'order by id desc' }, \%outcome, \%results ) ) {
	    ## $logger->error( "ERROR Could not get event: query was \"count=1&query='order by id desc'\", outcome: " .
	    #report_daemon_error( "Could not get event: query was \"count=1&query='order by id desc'\", outcome: " . to_json( \%outcome, { utf8 => 1, pretty => 1 } ) );

        # It's possible the logmessage table is empty, especially during testing :)
        if ( not ( $outcome{response_code} eq '404' and $outcome{response_error} =~ /Events not found for given event query/ )  ) { # then problem no events in table
	        ${$error_ref} = "Could not get event: query was \"count=1&query='order by id desc'\", outcome: " . to_json( \%outcome, { utf8 => 1, pretty => 1 }  );
	        return 0;
        }
    }

    # Since get_events() %results is keyed by id, we need look no deeper than the key itself.
    if (%results) {
	    ${$ref_last} = (keys %results)[0];
    }
    else { 
        # In here in the case of no logmessage entries, but don't return 0 since that indicates error which it's not.
        # return 0; 
        ${$ref_last} = 0; # just set this to 0
    }
    return 1;
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
    #           elasticsearch_nodes                    => 'array',   # check here that non empty (if non defined this array will exist, but just empty)
    #           host_bundle_size                       => 'number',  # check here
    #           origin                                 => 'scalar',
    #           number_of_test_events                  => 'number',
    #           batch_size                             => 'number',
    #           initially_process_everything           => 'boolean',
    #           hostgroup_bundle_size                  => 'number',
    #           retry_cache_max_age                    => 'number',
    #           service_bundle_size                    => 'number',

    # v 2.1.5
    # elasticsearch_nodes is replaced with cxn
    #if ( scalar @{ $feeder->{elasticsearch_nodes} } == 0 ) { 
    #    $message = "No elasticsearch nodes were defined!";
    #    ${$error_ref} = $message;
    #    return 0;
    #}

    # check <cxn> block present
    if ( not defined $feeder->{cxn} ) { 
        $message = "A required <cxn> configuration block was not found. No elasticsearch nodes were defined.";
        ${$error_ref} = $message;
        return 0;
    }
    # check at least one cxn nodes defined
    if ( not defined $feeder->{cxn}->{nodes} ) { 
        $message = "No elasticsearch nodes were defined. At least one 'nodes' directive is required in the <cxn> block.";
        ${$error_ref} = $message;
        return 0;
    }
    # check cxn port defined
    if ( not defined $feeder->{cxn}->{port} ) { 
        $message = "No elasticsearch nodes port was defined. A 'port' directive is required in the <cxn> block.";
        ${$error_ref} = $message;
        return 0;
    }
    # Also to at least restrict things a little bit, and not just have complete options pass through, limit allowed cxn options directives
    my %allowed_cxn_directives = ( 'nodes' => undef, 'port' => undef, 'use_https' => undef, 'userinfo' => undef ) ;
    foreach my $cxn_directive ( keys %{$feeder->{cxn}} ) {
	if ( not exists $allowed_cxn_directives{$cxn_directive} ) { 
            $message =  "Disallowed or invalid cxn option '$cxn_directive'. Allowed directives are " . join ",", keys %allowed_cxn_directives ;
            ${$error_ref} = $message;
            return 0;
	}
    }


    # TBD origin check - not sure what to check for tho yet

    # Check host_bundle_size is sane
    if ( $feeder->{host_bundle_size} < 0 ) { 
        $message = "Config error - host_bundle_size should be a positive number - check the feeder configuration.";
        ${$error_ref} = $message;
        return 0;
    }

    # Check hostgroup_bundle_size is sane
    if ( $feeder->{hostgroup_bundle_size} < 0 ) { 
        $message = "Config error - hostgroup_bundle_size should be a positive number - check the feeder configuration.";
        ${$error_ref} = $message;
        return 0;
    }

    # Check service_bundle_size is sane
    if ( $feeder->{service_bundle_size} < 0 ) { 
        $message = "Config error - service_bundle_size should be a positive number - check the feeder configuration.";
        ${$error_ref} = $message;
        return 0;
    }

    # Check retry_cache_max_age is sane
    if ( $feeder->{retry_cache_max_age} < 0 ) { 
        $message = "Config error - retry_cache_max_age should be a positive number - check the feeder configuration.";
        ${$error_ref} = $message;
        return 0;
    }

    # Check batch_size is sane
    if ( $feeder->{batch_size} < 0 ) { 
        $message = "Config error - batch_size should be a positive number - check the feeder configuration.";
        ${$error_ref} = $message;
        return 0;
    }

    # Check number_of_test_events is sane
    if ( $feeder->{number_of_test_events} < 0 ) { 
        $message = "Config error - number_of_test_events should be a positive number - check the feeder configuration.";
        ${$error_ref} = $message;
        return 0;
    }


    # Good enough for now.
    return 1;

}

# ---------------------------------------------------------------------------------
sub initialize_process_everything # This routine needs renaming
{
    my ( $error_ref ) = @_;
    my ( $last_processed_event_id, $last_event_id, %outcome , %results, $error ) ;

    # if first time running, and service will be created to track last event processed, then
    # if initially process everything is off, then need to seed the service with the id of the
    # last available event.
    # When the feeder starts, just process incoming new events if == 0
    if ( $feeder->{properties}->{initially_process_everything} == 0 ) {
	    if ( not get_last_available_logmessageid( \$last_event_id, \$error ) ) {
            ${$error_ref} = "Failed to initialize health and stats objects including $service_last_event_id_processed service. ";
            ${$error_ref} .= $error if defined $error;
	        $logger->error( ${$error_ref} ) ; 
            return 0;
	    }
    }
    else {
	    $last_event_id = '[pending]';
    }

    # if there is NOT a last_event_id_processed service, upsert that service with the last_event_id (that'll be [pending]) most probably
    if ( not $feeder->{rest_api}->get_services( [$service_last_event_id_processed], { hostname => $feeder->{properties}->{health_hostname} }, \%outcome, \%results ) ) {
        if ( %outcome and $outcome{response_code} == 404  )  {
            update_last_event_id_service( $service_last_event_id_processed, $last_event_id, \$error );
        }
        else {
            ${$error_ref} = "Failed to get service details for $feeder->{properties}->{health_hostname}/$service_last_event_id_processed";
	        $logger->error( ${$error_ref} ) ; 
            return 0;
        }
    }

    # if the service exists and its value is not a number, set it to pending, else leave it alone
    else {
	    get_last_processed_event_id( \$last_processed_event_id );
	    if ( $last_processed_event_id !~ /^\d+$/ ) {
	        update_last_event_id_service( $service_last_event_id_processed, $last_event_id, \$error );
	    }
    }

    return 1;
}

# ---------------------------------------------------------------------------------
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
       #my $retry_cache_directory            = $master_config->get_scalar(  'retry_cache_directory' );
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

    # Get a list of all hosts from foundation that were created by this feeder.
    $logger->debug( "Getting all Foundation hosts that were created by this feeder");
    if ( not $feeder->{rest_api}->get_hosts( [ ] , { query => "agentId = '$feeder->{guid}'", depth => 'simple' }, \%outcome, \%results ) ) {
        # If no hosts created by this feeder are in foundation - there's nothing to do here
        $logger->debug("No hosts were found that were created by this feeder." );
       #return 1; # Don't return - need to cover case of NAGIOS host, with CACTI services added to it
    }

    # Make a list of hosts created by this agentid that exist in Foundation 
    foreach $foundation_host ( sort keys %results ) {
        $logger->debug( "Marking Foundation host for delete : $foundation_host");
        $foundation_hosts_to_delete{$foundation_host} = 1;
    }

    # Get a list of all services from foundation that were created by this feeder.
    # Format the results using the host->service format.
    $logger->debug( "Getting all Foundation services that were created by this feeder");
    if ( not $feeder->{rest_api}->get_services( [ ], { query => "agentId = '$feeder->{guid}'", format=>'host,service' }, \%outcome, \%results ) ) {
        $logger->debug("No services were found that were created by this feeder.");
        return 1; # allow for case of feeder host without any services on it
    }

    # Get a list of all of the endpoint names from the master config. Used for special logic for feeder health services etc
    my %endpoint_hosts = map { (split ':',$_)[0] => 1 }  @{ $master_config->{endpoint} } ;

    # Make a list of services that exist in Foundation 
    FOUNDATIONHOST: foreach $foundation_host ( sort keys %results ) {
        next if ( defined $foundation_hosts_to_delete{$foundation_host} ); # skip deleting services for a host if it host marked for delete as del host takes dels its svcs
        SERVICE: foreach $foundation_service ( keys %{ $results{$foundation_host} } ) {
            if ( exists $results{$foundation_host}{$foundation_service}{properties}{Notes} and $results{$foundation_host}{$foundation_service}{properties}{Notes} eq $GW::Feeder::metric_service_meta_tag ) {
                # Skip removal of this feeder metric but only if it's attached to a host that is in the endpoint host set
                if ( exists $endpoint_hosts{ $foundation_host } ) {
                    $logger->debug( "Skipping service $foundation_host : $foundation_service - its a metric service");
                    next SERVICE;
                }
            }
            $logger->debug( "Marking Foundation service $foundation_host : $foundation_service for delete");
            $foundation_services_to_delete{$foundation_host}{$foundation_service} = 1;
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

    $logger->trace( "HOSTS to delete : " . Dumper \%foundation_hosts_to_delete );
    $logger->trace( "SERVICES to delete : " . Dumper \%foundation_services_to_delete ); 

    # Bundle-wise delete any hosts from Foundation that don't exist in lb conf set
    %delete_hosts_options = ();
    if ( keys %foundation_hosts_to_delete ) {
        if ( not $feeder->feeder_delete_hosts( \%foundation_hosts_to_delete, \%delete_hosts_options ) ) {
            ${$error_ref} = "ERROR Couldn't delete hosts from Foundation.";
            return 0;
        }
    }

    # Bundle-wise delete any services from foundation that don't exist in lb conf set
    %delete_services_options = ();
    if ( keys %foundation_services_to_delete ) {
        if ( not $feeder->feeder_delete_services( \%foundation_services_to_delete, \%delete_services_options ) ) {
            ${$error_ref} = "ERROR Couldn't delete services from Foundation.";
            return 0;
        }
    }

    return 1;
}

__END__
€ý5:q!€ý5:q!

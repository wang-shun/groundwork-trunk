#!/usr/local/groundwork/perl/bin/perl

# Gets events (i.e., logmessages) from Foundation, and feeds them into Elasticsearch.
#
# Copyright (c) 2014-2016 GroundWork, Inc. (www.gwos.com).  All rights reserved.
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
# v 0.9.45 - Dominic Nicholas 3/14 - once added to aid in debugging mappings issue and mapping stuff fixed
#                                    (create index with mapping, then bulk index)
# v 1.0.0  - GH               2014-09-04 - upgraded to current GW::RAPID package; general cleanup
# v 2.0.0  - DN               2015-05-05 - refactored to use Feeder.pm and robustness 
# v 2.0.1  - GH               2016-08-25 - Die automatically after running for an hour, as a temporary fix to work
#                                          around memory leaks (GWMON-12626), to be restarted by the parent supervise.
#
#
# FIX MINOR:
# - write help
# - revisit signal handling
# - log rotation (non script related)
# use Storable qw(dclone); # perhaps in future see send_events_to_elasticsearch()

use strict;
use warnings;

use version;
use GW::RAPID;
use GW::Feeder qv('0.3.1.3');
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

use Data::Dumper; $Data::Dumper::Indent   = 1; $Data::Dumper::Sortkeys = 1;
my $VERSION = qv('2.0.1');
my $config_file = '/usr/local/groundwork/config/elastic_scanner.conf';
# Configuration option vars
my (
    $enable_processing,     $logfile,                      $log4perl_config,       $rest_api_requestor,
    $ws_client_config_file, $monitoring_server,            $cycle_timings,         $cycle_sleep_time,
    $health_hostname,       $health_hostgroup,             $number_of_test_events, $batch_size,
    @elastic_search_nodes,  $initially_process_everything, $origin,                $health_service,
    $app_type,
) = undef;

my $feeder_name = "elastic_scanner";

my ( $logger, $feeder ) = undef;

# CLI option vars
my ( $clean, $help, $show_version, $testbatch, $once ) = undef;

# health/stats service names
my $service_last_event_id_processed = "last_event_id_processed";
my $service_test_events_service     = "test_events_service";

# elasticsearch
my $elastic_search_object;    # global elasticsearch api object

# FIX MINOR:  This value supports a temporary workaround for a memory leak
# which occurs partly because of a leak in an underlying third-party package,
# and partly because we're reconfiguring ourselves WAY too often.  If we
# really want to use this setting long-term, it (or some name like it) ought
# to be moved to the config file.
my $max_seconds_to_run_without_restart = 3600;

# ============================================================= 
main();

# ------------------------------------------------------------------------------------------------------------
END {
    # To be kind to the server and always disconnect our session, we attempt to force a shutdown
    # of the REST API before global destruction sets in and makes it impossible to log out,
    # regardless of how we got to the end of the program.
    terminate_rest_api();

    # We generally run this daemon under control of supervise, which will immediately attempt to
    # restart the process when it dies.  In order to prevent a tight loop of failure and restart,
    # we delay process exit a short while no matter how we're going down.
    sleep(0);
}

# ============================================================= 
sub main 
{
    ## main sub that does initialization, processing loop, etc.

    my $fresh_start_time = time();

    my ( $start_time, $cycle_count, $total_cycle_time_taken, $events_retrieved, $events_sent, $retrieve_time, $send_time );
    my ( $try, $max_retries );

    my $started_at = localtime;

    # read and process cli opts
    initialize_options();

    # Check for other feeders running - there can only be one
    if ( perl_script_process_count( basename($0) ) > 1 ) {
        print("Another $feeder_name is running - quitting\n") ;
        exit;
    }

    # set up interrupt handlers
    #initialize_interrupt_handlers();

    # read option values - need this to get the logger stuff - bit inefficient reading the conf twice
    read_daemon_config_file($config_file);

    if ( not initialize_logger('started') ) {
	    print "Cannot initialize the logger; quitting in 5 seconds.\n";
	    sleep 5; 
	    exit (1);
    }

    # Set options up for new feeder
    my %feeder_options = (

        # The log4perl logger
        logger => $logger,

        # Feeder specific options to retrieve and type-check.
        # Access standard or specific properties with $feeder->{properties}->{propertname}, eg $feeder->{properties}->{cycle_time}
        feeder_specific_properties => {
	                                    batch_size => 'number',
	                                    elastic_search_nodes => 'array',
                                        host_bundle_size => 'number',
                                        hostgroup_bundle_size => 'number',
	                                    initially_process_everything => 'boolean',
                                        log4perl_config => 'array',
	                                    number_of_test_events => 'number',
	                                    origin => 'scalar',
                                        service_bundle_size => 'number',
                                        system_indicator_check_frequency => 'number',
                                      }
    );

    # log app start
    $logger->info("INFO ======== $feeder_name starting ========");

    # TBD where to move this ?
    if ($testbatch) {
	    $logger->info("INFO ---- Generating $testbatch test events ----"); 
	    generate_test_events($testbatch); # TBD catch errors
	    $logger->info("INFO ---- Done - quitting ----");
	    exit;
    }

    # process log messages in a never-ending cycle
    $cycle_count = 1;
    CYCLE: while (1) {

        # FIX MAJOR:  The code here ought to be restructured to examine the
        # last-modified timestamp of the config file, and only re-read it if
        # it has changed since the last time we read the config file.
        #
        # read in the config again - useful if you want to turn on/off test events while feeder running
        # don't quit if there's an error though
        if ( not read_daemon_config_file( $config_file ) ) {
            sleep 5;
            next CYCLE;
        }

        # Create a new feeder object per cycle to avoid any potential REST expiration/disconnection issues with long cycle times
        undef $feeder if defined $feeder; # Destroy feeder if defined 

        # FIX MAJOR:  This automated restart is a workaround for the fact that we
        # have a memory leak (GWMON-12626).  It ought to go away when the code is
        # generally restructured not to re-read the config file on every cycle, not
        # only here just above in the top-level daemon, and subsequently again within
        # the GW::Feeder->new() call.  When we intentionally die here, the parent
        # supervise process will restart this process so there should be no loss of
        # ongoing function.
        #
        # The better fix long-term is to stop re-reading the config file directly
        # from within this script on every cycle unless its last-modificaton time
        # has changed, and to stop creating a new Feeder on every cycle so that
        # action does not also re-read the config file.  Also, the current release of
        # GW::RAPID automatically attempts to log in again if some call receives an
        # authorizaton failure, which will happen if the client's token times out on
        # the server.  So there ought to be little reason to create a new Feeder here
        # at the top application level simply because this daemon has been running
        # a long time.  What should happen instead is that the daemon should depend
        # on GW::RAPID to automatically recover from ordinary server-side token
        # expiration, and if that fails and the REST call fails, or if a REST call
        # fails for any other reason, then this daemon should sleep for perhaps 10 or
        # 15 seconds (to avoid an instant-restart loop) and then die, to be restarted
        # by the parent "supervise" process.
        #
        if (time() - $fresh_start_time > $max_seconds_to_run_without_restart) {
            $logger->info( "max_seconds_to_run_without_restart limit reached - feeder shut down - exiting" );
            exit 0;
        }

        $try = 1; $max_retries = 3;
        while ( not ( $feeder = GW::Feeder->new( $feeder_name, $config_file, \%feeder_options ) ) and $try <= $max_retries ) {
            $logger->error("Couldn't create feeder object - try $try/$max_retries - waiting to try again...");
            sleep 5;
            $try++;
        }
        # If failed to create a new feeder endpoint after retrying, skip the endpoint for now.
        # Before moving on to the next endpoint, append the queried data onto this endpoint's retry cache.
        if ( $try > $max_retries ) {
            $logger->error("Couldn't create feeder object.");
            next CYCLE;
        }

        # validate feeder-specific options
        if ( not validate_feeder_specific_options() ) {
            $logger->error("Invalid feeder options detected - no processing will be done."); 
            sleep 5;
            next CYCLE;
        }
            
        # set up interrupt handlers
        $feeder->initialize_interrupt_handlers();

        # If -clean, do cleanup and exit
        cleanup() if defined $clean;


        # Initialize a feeder hostgroup, etc
        if ( not $feeder->initialize_health_objects( $started_at ) ) {
            $logger->error("Initializing feeder health objects");
            next CYCLE;
        }

        # see initially_process_everything option
        # This also seeds the last_event_id_processed service - important!

        initialize_process_everything();

        # is feeder disabled ?
        if ( not $feeder->{enable_processing} ) {
            ## FIX MAJOR:  If disabled, sleep for a much longer time period, like nearly forever, so as not to keep cycling.
            $logger->info("Processing is currently disabled. To enable it, set enable_processing = yes in $config_file.");
            #sleep $feeder->{properties}->{system_indicator_check_frequency};    
            sleep 5;
            next CYCLE;
        }

        # initialize esearch
        if ( not initialize_elasticsearch() ) {
            $logger->error( "Failed to initialize elasticsearch - waiting for a bit then restarting cycle" );
            sleep 10;
            next CYCLE;
        }

	    $start_time = Time::HiRes::time();
	    $logger->info("INFO ---- Starting cycle $cycle_count ----");

	    if ( $feeder->{properties}->{number_of_test_events} > 0 ) { 
            generate_test_events($feeder->{properties}->{number_of_test_events} );  # TBD catch
        }

        $events_retrieved = $events_sent = $retrieve_time = $send_time = 0;
        if ( not process_log_messages( \$events_retrieved, \$events_sent, \$retrieve_time, \$send_time ) ) {
            upsert_daemon_service( "${feeder_name}_health", "Feeder failed to successfully process log messages", "UNSCHEDULED CRITICAL");
        }

        # converts audit into events in foundation, and empties it
        $feeder->flush_audit() ;  # this will produce its own errors if necessary

        # update metrics
	    $total_cycle_time_taken = sprintf "%0.3f", Time::HiRes::time() - $start_time;
        my $finished_time = localtime();
	    update_elastic_scanner_stats(   $cycle_count,
                                        $finished_time,
                                        $total_cycle_time_taken,
                                        $events_retrieved,  
                                        $events_sent,   
                                        $retrieve_time,     
                                        $send_time );

	    if ( defined $cycle_timings ) { $logger->info("INFO Cycle took $total_cycle_time_taken seconds"); }

	    $cycle_count++;

        # done if -once option given
        if ( $once ) {
            upsert_daemon_service( "${feeder_name}_health", "Run-once option supplied - feeder shut down - exiting");
            $logger->info( "Run-once option supplied - feeder shut down - exiting" );
            undef $feeder ;
            exit;
        };
    
        $logger->info("INFO sleeping $feeder->{properties}->{system_indicator_check_frequency} seconds");
        sleep $feeder->{properties}->{system_indicator_check_frequency};
	    #$logger->info("INFO sleeping $cycle_sleep_time seconds");
	    #sleep $cycle_sleep_time;
    
    }
}

# -------------------------------------------------------------
sub initialize_logger {
    my $phase = $_[0];

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
	    Log::Log4perl::init( $log4perl_config =~ m{^/} ? $log4perl_config : \$log4perl_config );
    };
    if ($@) {
	    chomp $@;
	    print "ERROR:  Could not initialize Log::Log4perl logging:\n$@\n";
	    return 0;
    }

    # Note that there seems to be no kind of error return for get_logger().
    # So if the configuration is wrong, we don't get warned about that here.
    $logger = Log::Log4perl::get_logger("Elastic.Scanner");

    $SIG{__WARN__} = sub { $logger->warn("WARN @_"); };

    # The __DIE__ handler catches Perl run-time errors and could be used to get them logged.  But
    # it also captures lots of internal detail that is caught by eval{}; statements, that has no
    # business being logged.  So we use a better mechanism, provided below, for capturing Perl
    # errors, by explicitly redirecting STDERR to the logfile.
    # $SIG{__DIE__} = sub { $logger->fatal("DIE @_"); };

    if ( not open STDERR, '>>', $logfile ) {
	    $logger->logdie("FATAL:  Cannot redirect STDERR to the log file \"$logfile\": $!");
    }

    return 1;
}

# ------------------------------------------------------------- 
sub terminate_rest_api {
    ## Release our handle to the REST API (if we used it), to force the REST API to call its destructor.
    ## This will attempt to log out before Perl's global destruction pass wipes out resources needed for
    ## logout to work properly.
    $feeder->{rest_api} = undef;
}

# ------------------------------------------------------------- 
sub process_log_messages 
{
    ## gets recent log messages (events) and sends them into elasticsearch
    ## Gets a bundle of them from foundation, and sends that bundle bulk-fashion
    ## into elasticsearch. The last logmessage processed is stored in a service result in foundation.
    ## returns 1 on success, 0 otherwise

    my ( $ref_events_retrieved, $ref_events_sent, $ref_retrieve_time, $ref_send_time ) = @_;
    my ( $last_event_id, $last_processed_event_id, $start_id, $end_id );
    my $events_retrieved                = 0;
    my $events_sent                     = 0;
    my $total_count_of_events_retrieved = 0;
    my $total_count_of_events_sent      = 0;
    my $send_time                       = 0;
    my $retrieve_time                   = 0;
    my $send_status;

    $total_count_of_events_retrieved = $total_count_of_events_sent = 0;
    $$ref_events_retrieved = $$ref_events_sent = $$ref_retrieve_time = $$ref_send_time = 0;

    $logger->debug("DEBUG Processing Foundation log messages aka events");

    # get the last logmessageid available
    if ( not get_last_available_logmessageid( \$last_event_id ) ) {
	    return 0;
    }
    $logger->debug("DEBUG The id of last available event in the logmessage table is $last_event_id");

    # get the id of the last event processed by this app
    if ( not get_last_processed_event_id( \$last_processed_event_id, $last_event_id ) ) {
	    return 0;
    }
    $logger->debug("DEBUG >>> the id of the event last processed by this app was '$last_processed_event_id'");

    # hey it could happen ;->
    if ( $last_processed_event_id == $last_event_id ) {
	    $logger->debug("DEBUG >>> All up to date");
	    return 1;
    }

    my %outcome;
    my %results;

    # process the events in the required range, doing it a chunk at a time as defined by batch_size
    $logger->debug( "DEBUG process events with ids in range " . ( $last_processed_event_id + 1 ) . " .. $last_event_id" );
    while ( $last_processed_event_id <= $last_event_id ) {
	    ## get next batch of events from foundation
	    $start_id = $last_processed_event_id + 1;    # makes for more readable code, +1 because we don't want to reprocess last event
	    $end_id   = $start_id + $feeder->{properties}->{batch_size} - 1;     # makes for more readable code, -1 because we're working with indices

	    # don't try to process more events than there actually are
	    $logger->debug("DEBUG next range: $start_id .. $end_id");
	    if ( $end_id > $last_event_id ) {
	        $end_id = $last_event_id;
	        $logger->debug("DEBUG range end id reset to $end_id");
	    }

	    # Get the events for the range and check for errors
	    $logger->debug("DEBUG Getting foundation log messages with ids in range $start_id .. $end_id");
	    $retrieve_time = Time::HiRes::time();

	    # FIX LATER:  standardize the error reporting from %outcome and %results
	    if ( not $feeder->{rest_api}->get_events( [], { query => "id >= $start_id and id <= $end_id" }, \%outcome, \%results ) ) {
	        ## $logger->error( "WARNING Could not get events $start_id .. $end_id from Foundation: " ) ; ##     . to_json( \%outcome, {  utf8 => 1 , pretty => 1} ) );
	        report_daemon_error("WARNING Could not get events $start_id .. $end_id from Foundation: ") ;   # . to_json( \%outcome, {  utf8 => 1 , pretty => 1} ) );
	        # There could be gaps in the event id range from purges for example, so it's possible that this batch is empty.
	        if ( defined $outcome{response_error} and $outcome{response_error} =~ /Events not found for given event query/ ) {
		        $logger->debug("DEBUG an empty batch - gappy logmessageid sequence");
	        }
	        else {
		        ## some other error occurred - log it and stop processing events (FIX MINOR: refine this later if necessary)
		        ## $logger->error( "ERROR getting events - quitting processing of events: " . to_json( \%results, {  utf8 => 1 , pretty => 1} ) );
		        report_daemon_error( "ERROR getting events - quitting processing of events: " . to_json( \%results, { utf8 => 1, pretty => 1 } ) );
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
	    if ( $events_retrieved < ( $end_id - $start_id ) ) {
	        $logger->debug("WARNING only retrieved $events_retrieved events");
	    }
	    else {
	        $logger->debug("DEBUG Retrieved $events_retrieved events");
	    }
    
	    # send that batch to elasticsearch if non-empty
	    $send_time = Time::HiRes::time();
	    $send_status = $events_retrieved ? send_events_to_elasticsearch( \%results, \$events_sent ) : 0;
	    $send_time = Time::HiRes::time() - $send_time;

	    # update stats for health services
	    $total_count_of_events_retrieved += $events_retrieved;
	    $total_count_of_events_sent      += $events_sent;
    
	    # increment $last_processed_event_id by batch size
	    # Note that 'last processed' is a bit misleading as there's a chance it wasn't actually
	    # processed properly during the send to elasticsearch.
	    # Only update the range if things sent with a bad error with the bulk send.
	    # Will have to see how this works out in the field and refine if necessary if get stuck in loops.
	    if ( $send_status == 1 ) {
	        $last_processed_event_id = highest_event_id( \%results );
	        $logger->debug("DEBUG set last_processed_event_id = $last_processed_event_id");
	    }
	    else {
	        $logger->debug("DEBUG last_processed_event_id NOT updated due to a big bulk send error");
	    }
    
	    # update a health service with the last processed event id - important!
	    upsert_daemon_service( $service_last_event_id_processed, $last_processed_event_id );
    
	    # if the calculated event batch end range is bigger than the actual available events (because of the batch size),
	    # then stop trying to process events
	    last if ( $end_id >= $last_event_id );
    }

    # Do these updates in the update_feeder_stats sub instead
    # update a health service with total # of events pulled out of foundation
    # upsert_daemon_service( 'events_retrieved_on_last_cycle', "$total_count_of_events_retrieved" );
    # update a health service with total # of events pushed into elasticsearch
    # upsert_daemon_service( 'events_sent_on_last_cycle', "$total_count_of_events_sent" );

    # for stats in main loop
    ( $$ref_events_retrieved, $$ref_events_sent, $$ref_retrieve_time, $$ref_send_time ) = ( $total_count_of_events_retrieved, $total_count_of_events_sent, $retrieve_time, $send_time );

    return 1;
}

# -------------------------------------------------------------
sub get_last_processed_event_id 
{
    ## get the id of the last event processed by this app
    ## takes:
    ##   - a reference to a scalar in which to put the result
    ##   - the id of the last event in logmessage table
    ## result is the id, or null if not set
    ## returns 1 on success, 0 on failure
    my ( $ref_last_processed_event_id, $last_event_id ) = @_;

    # get the details for the service which has the last processed event id

    my %outcome;
    my %results;

    # FIX LATER:  standardize the error reporting from %outcome and %results
    if ( not $feeder->{rest_api}->get_services( [$service_last_event_id_processed], { hostname => $health_hostname }, \%outcome, \%results ) ) {
	    ## $logger->error( "ERROR Couldn't get service '$service_last_event_id_processed' for host '$health_hostname': "
	    ##     . to_json( \%outcome, { utf8 => 1, pretty => 1 } ) );
	    report_daemon_error( "ERROR Couldn't get service '$service_last_event_id_processed' for host '$health_hostname': " . to_json( \%outcome, { utf8 => 1, pretty => 1 } ) );
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
	    report_daemon_error( "ERROR Couldn't get service '$service_last_event_id_processed' for host '$health_hostname': no result returned from query.");
	    ${$ref_last_processed_event_id} = undef;
	    return 0;
    }

    return 1;
}

# ------------------------------------------------------------- #
sub send_events_to_elasticsearch {
    ## takes a batch of events and sends them in bulk to elasticsearch

    my ( $hashref_events_batch, $ref_count_of_events_sent ) = @_;
    my ( $event_ref, $event_property, %esearch_bulk_instructions, @events, %event_properties );
    my ( $es_result, $e_index, $e_type, $sent_count, $count_result );
    my ( @hostgroup_names, @category_names, %mappings, $doc_count );

    $logger->debug("DEBUG Sending events to elasticsearch");

    # create the esearch index and type
    $e_index = 'groundwork-' . strftime( "%Y.%m.%d", localtime );    # eg groundwork-2014.02.19
    $e_type = 'foundation_logmessage';
    $logger->debug("DEBUG Sending events to elasticsearch (index=$e_index, type=$e_type)");

    my %outcome;
    my %results;

    # take apart the bulk of retrieved events and copy them into an array of hashes to be used by esearch
    # perhaps use storage::dclone to do deep copying of array of hashes in future?
    foreach $event_ref ( values %{ $hashref_events_batch } ) {
	@hostgroup_names = @category_names = ();                     # reset these per event ie per host

	$logger->debug("DEBUG Preparing event id $event_ref->{id}");
	foreach $event_property ( keys %{$event_ref} ) {
	    next if $event_property eq 'id';
	    ## print "\t $event_property --> $event_ref->{$event_property} \n";
	    $event_properties{$event_property} = $event_ref->{$event_property};
	}

	# figure out which hostgroups and categories the host is in and add that list to the doc
	if ( $event_properties{host} ) {
	    ## FIX LATER:  standardize the error reporting from %outcome and %results
	    if ( not $feeder->{rest_api}->get_hostgroups( [], { query => "hosts.hostName = '$event_properties{host}'", depth => 'simple' }, \%outcome, \%results)) {
		report_daemon_error("ERROR could not get hostgroups for host '$event_properties{host}'");
	    }
	    else {
		## The get_hostgroups() %results are keyed by the hostgroup names, so we need dig no deeper.
		## That's why we specified depth => 'simple' in the query.
		push @hostgroup_names, keys %results;
		$event_properties{'hostgroups'} = \@hostgroup_names;
	    }

	    # get categories for host (BUT WHAT DOES THAT EVEN MEAN???  SERVICEGROUPS?)
	    # WAS NOT ORIGINALLY WORKING - waiting for "next" rev of GW REST API (BUT WHAT *EXACT* FEATURE ARE WE AWAITING?)
	    if (0) {
		## FIX LATER:  standardize the error reporting from %outcome and %results
		if ( not $feeder->{rest_api}->get_categories( [], { query => "hosts.hostName = '$event_properties{host}'", depth => 'shallow' }, \%outcome, \%results)) {
		    report_daemon_error("ERROR could not get categories for host '$event_properties{ host }'");
		}
		else {
		    ## FIX MINOR:  We still need to convert this to the new GW::RAPID call results,
		    ## once we figure out what we're trying to extract from the database (WHAT DO
		    ## CATEGORIES HAVE TO DO WITH HOSTS???  ARE WE PERHAPS TRYING TO EXTRACT ALL
		    ## SERVICEGROUP NAMES FOR WHICH THIS HOST HAS A SERVICE IN THE SERVICEGROUP?
		    ## THE GWMEE 7.1.0 RELEASE HAS EXPLICIT GW::RAPID ROUTINE SUPPORT FOR DIRECTLY
		    ## PROBING SERVICEGROUPS WITHOUT CONFUSING THE ISSUE WITH REFERENCES TO THE
		    ## CATEGORIES THAT IMPLEMENT THEM.  IF WE MEAN SERVICEGROUPS, THEN WE SHOULD
		    ## PROBABLY BE POPULATING $event_properties{'servicegroups'} HERE INSTEAD OF
		    ## $event_properties{'categories'}, FOR CLARITY.)
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
    # $elastic_search_object = Elasticsearch->new( nodes => [ @elastic_search_nodes ]);

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
    if ( $doc_count == 0 ) {
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
			    'severity'            => { 'type' => 'string', 'index' => 'not_analyzed' },
			    'typeRule'            => { 'type' => 'string', 'index' => 'not_analyzed' },
			}
		    }
		}
	    }
	);

	# print( "DEBUG creating index with mappings for index $e_index, type $e_type\n");
	$logger->debug("DEBUG creating index with mappings for index $e_index, type $e_type");
	$logger->trace( "TRACE mappings for $e_index, $e_type are: " . to_json( \%mappings, { utf8 => 1, pretty => 1 } ) );

	# NOTE https://metacpan.org/pod/Search::Elasticsearch::Client::Direct::Indices#create
	# Also, note that indices->put_mapping() requires the index to exist first, so better to create the index first .. with the mappings

	eval { $es_result = $elastic_search_object->indices->create( \%mappings ); };
	if ($@) {
	    ## if this happens, the main loop will get wedged trying to send the same set of events in over and over
	    report_daemon_error("ERROR !!! Could not create index $e_index, type $e_type, with mappings: $@");
	    ${$ref_count_of_events_sent} = 0;
	    ## FIX MINOR:  Well, it does happen, at least in development testing.  So at a minimum we need some sort
	    ## of sleep to slow down the repeated errors, so as not to fill up the log file too fast.  If we otherwise
	    ## address the problem in the large, we may be able to eliminate this sleep.
	    sleep $cycle_sleep_time;
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
    $logger->debug("DEBUG performing bulk index of event docs - index $e_index, type $e_type");
    $logger->trace( "TRACE elasticsearch instructions: " . to_json( \%esearch_bulk_instructions, { utf8 => 1, pretty => 1 } ) );    # tmi ?

    eval { $es_result = $elastic_search_object->bulk( \%esearch_bulk_instructions ); };
    if ($@) {
	report_daemon_error("ERROR !!! Could not bulk index to elasticsearch: $@");
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
	    report_daemon_error(
		"WARNING possibly failed to send item to elasticsearch: " . to_json( $itemhash, { utf8 => 1, pretty => 1 } ) );
	}
    }

    # if didn't send the same amount received, log an error
    my $received_count = scalar keys %{$hashref_events_batch};
    if ( $sent_count != $received_count ) {
	## $logger->error( "ERROR Received $received_count events, sent $sent_count events to elasticsearch" );
	report_daemon_error("ERROR Received $received_count events, sent $sent_count events to elasticsearch");
    }
    else {
	$logger->debug("DEBUG Received $received_count events, sent $sent_count events to elasticsearch");
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
# TBD this should be using $feeder->upsert_feeder_service()
sub upsert_daemon_service 
{
    ## Create or update a daemon service that is attached to the health vhost
    ## The only things this subroutine currently updates are LastPluginOutput

    my ( $service, $message, $status ) = @_;

    my %statuses = ( "0" => "OK", "1" => "WARNING", "2" => "UNSCHEDULED CRITICAL", "3" => "UNKNOWN" );

    $logger->debug("DEBUG Upserting $feeder_name service '$service' with message \"$message\"");

    my $now = strftime( '%Y-%m-%dT%H:%M:%S', localtime );
    my $tz = strftime( "%z", localtime() );

    # figure out status to pass in to API
    if ( defined $status ) {
	    if ( $status =~ /^\d+$/ ) {
	        if ( defined $statuses{$status} ) {
	    	    $status = $statuses{$status};
	        }
	        else {
		        $logger->error("ERROR unrecognized status value '$status' - setting to UNKNOWN");
		        $status = 'UNKNOWN';
	        }
	    }
    }
    else {
	    $status = 'OK';
    }

    my @services = (
	                {
	                    'agentId'              => $feeder->{properties}->{guid},
	                    'description'          => $service,
	                    'hostName'             => $feeder->{properties}->{health_hostname},
	                    'deviceIdentification' => $feeder->{properties}->{health_hostname},
	                    'appType'              => $feeder->{properties}->{app_type},
	                    'monitorStatus'        => $status,
	                    'lastHardState'        => 'PENDING',                            # FIX MINOR: not sure if this is appropriate here
	                    'lastCheckTime'        => "$now$tz",
	                    'checkType'            => 'PASSIVE',
	                    'properties'           => { 'LastPluginOutput' => $message, }
	                }
    );

    my %outcome;
    my @results;

    # FIX LATER:  standardize the error reporting from %outcome and @results
    if ( not $feeder->{rest_api}->upsert_services( \@services, { async => 'false' }, \%outcome, \@results ) ) {
	    $logger->error( "Could not upsert service '$service' on host '$health_hostname' with message \"$message\": " . to_json( \%outcome, { utf8 => 1, pretty => 1 } ) );
    }
}

# -------------------------------------------------------------
sub generate_test_events 
{
    ## generates a bunch of test events - useful for testing and development purposes
    my ($count) = @_;

    $logger->debug("DEBUG Generating $count test events");

    my $now = strftime( '%Y-%m-%dT%H:%M:%S', localtime );
    my $tz = strftime( "%z", localtime() );

    my @events = ();
    for ( my $event = 1 ; $event <= $count ; $event++ ) {
	    $logger->debug("DEBUG Generating test event number $event / $count");

	    push @events, {
	        ## 'consolidationName'   => 'NAGIOSEVENT',  # Disable consolidation to create separate events (logmessage rows).
	        ## Default preprocessing rules in foundation.properties will
	        ## cause these events to show up with a message count of 1
	        ## but at least the message updates.
	        'appType'       => $feeder->{properties}->{app_type},
	        'device'        => $feeder->{properties}->{health_hostname},
	        'host'          => $feeder->{properties}->{health_hostname},
	        'service'       => $service_test_events_service,
	        'monitorStatus' => 'WARNING', # warning state more useful
	        'textMessage'   => "$feeder_name test event batch $now$tz",
	        #'monitorServer' => $monitoring_server,    # FIX MAJOR:  Verify:  should this be a fixed 'localhost' value instead?
	        'monitorServer' => 'localhost',    # FIX MAJOR:  Verify:  should this be a fixed 'localhost' value instead?
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
sub update_elastic_scanner_stats_old
{
    ## updates some stats for elastic_scanner
    my ( $cycle_count, $cycle_elapsed_time, $events_retrieved, $events_sent, $retrieve_time, $send_time ) = @_;
    my ( $events_retrieved_per_sec, $events_sent_per_sec );
    $events_retrieved_per_sec = $events_sent_per_sec = 0;

    $logger->debug("DEBUG Updating elastic_scanner statistics");

    if ( $retrieve_time > 0 ) {
	    $events_retrieved_per_sec = sprintf( "%d", $events_retrieved * 60 / $retrieve_time ) . "/sec ($events_retrieved retrieved in $retrieve_time sec)";
    }

    if ( $send_time > 0 ) {
	    $events_sent_per_sec = sprintf( "%d", $events_sent * 60 / $send_time ) . "/sec ($events_sent sent in $send_time sec)";
    }

    upsert_daemon_service( 'events_retrieved_per_minute', "$events_retrieved_per_sec" );
    upsert_daemon_service( 'events_sent_per_minute',      "$events_sent_per_sec" );

}

# -------------------------------------------------------------
sub update_elastic_scanner_stats
{
    # Logs feeder stats and updates services with metrics too
    my ( $cycle_number, 
        $finished_time,
        $cycle_elapsed_time, 
        $events_retrieved, 
        $events_sent, 
        $retrieve_time, 
        $send_time ) = @_;

    my ( $events_retrieved_per_sec, $events_sent_per_sec );
    $events_retrieved_per_sec = $events_sent_per_sec = 0;

    # Metrics 
    # Service name                      Message
    # cycle_elapsed_time                Time taken to process last cycle
    # events_retrieved_on_last_cycle    Count of events retrieved on last cycle
    # events_retrieved_per_minute       Events retrieved per minute
    # events_sent_on_last_cycle         Count of events sent on last cycle
    # events_sent_per_minute            Events sent per minute

    
    # Metric service : cycle_elapsed_time
    my $cycle_elapsed_time_msg = "Cycle $cycle_number finished at $finished_time, total elapsed processing time : $cycle_elapsed_time seconds";

    # Metric service : events_retrieved_on_last_cycle
    my $events_retrieved_on_last_cycle_msg = "$events_retrieved GroundWork events retrieved in cycle $cycle_number";

    # Metric service : events_retrieved_per_minute
    my $events_retrieved_per_minute_msg = "0/sec ($events_retrieved GroundWork events retrieved in $retrieve_time sec)";
    if ( $retrieve_time > 0 ) {
	    $events_retrieved_per_minute_msg = sprintf( "%d", $events_retrieved * 60 / $retrieve_time ) . "/sec ($events_retrieved GroundWork events retrieved in $retrieve_time sec)";
    }

    # Metric service : events_sent_on_last_cycle
    my $events_sent_on_last_cycle_msg = "$events_sent GroundWork events sent in cycle $cycle_number";

    # Metric service : events_sent_per_minute
    my $events_sent_per_minute_msg = "0/sec ($events_sent GroundWork events retrieved in $send_time sec)";
    if ( $send_time > 0 ) {
	    $events_sent_per_minute_msg = sprintf( "%d", $events_sent * 60 / $send_time ) . "/sec ($events_sent sent in $send_time sec)";
    }


    $logger->debug("Updating feeder statistics");

    # Log metrics
    $logger->info( "$cycle_elapsed_time_msg") if defined $feeder->{cycle_timings};
    $logger->info( "$events_retrieved_on_last_cycle_msg");
    $logger->info( "$events_retrieved_per_minute_msg");
    $logger->info( "$events_sent_on_last_cycle_msg");
    $logger->info( "$events_sent_per_minute_msg");

    #my $now = strftime( '%Y-%m-%dT%H:%M:%S', localtime ); my $tz  = strftime("%z", localtime );

    # Update services with metrics
    if ( not $feeder->feeder_upsert_services(    [ 
                                                    {
                                                        'description'          => 'cycle_elapsed_time',
                                                        'hostName'             => $feeder->{properties}->{health_hostname}, 
                                                        'monitorStatus'        => "OK",
                                                        'properties'           => { "LastPluginOutput" => $cycle_elapsed_time_msg },
                                                    },
                                                    {
                                                        'description'          => 'events_retrieved_on_last_cycle',
                                                        'hostName'             => $feeder->{properties}->{health_hostname}, 
                                                        'monitorStatus'        => "OK",
                                                        'properties'           => { "LastPluginOutput" => $events_retrieved_on_last_cycle_msg },
                                                    },
                                                    {
                                                        'description'          => 'events_retrieved_per_minute',
                                                        'hostName'             => $feeder->{properties}->{health_hostname}, 
                                                        'monitorStatus'        => "OK",
                                                        'properties'           => { "LastPluginOutput" => $events_retrieved_per_minute_msg },
                                                    },
                                                    {
                                                        'description'          => 'events_sent_on_last_cycle',
                                                        'hostName'             => $feeder->{properties}->{health_hostname}, 
                                                        'monitorStatus'        => "OK",
                                                        'properties'           => { "LastPluginOutput" => $events_sent_on_last_cycle_msg },
                                                    },
                                                    {
                                                        'description'          => 'events_sent_per_minute',
                                                        'hostName'             => $feeder->{properties}->{health_hostname}, 
                                                        'monitorStatus'        => "OK",
                                                        'properties'           => { "LastPluginOutput" => $events_sent_per_minute_msg },
                                                    },
                                                ], {}  )  ) { 
        $logger->error("Error updating feeder statistical services");
    }

}

# ------------------------------------------------------------- 
sub read_daemon_config_file 
{
    ## reads the elastic_scanner config file
    my ($config_file) = @_;
    my $config;

    $logger->debug("DEBUG Reading and processing config file: $config_file") if $logger;

    eval {
	    $config                       = TypedConfig->new($config_file);
	   #$app_type                     = $config->get_scalar('app_type');
	   #$batch_size                   = $config->get_number('batch_size');
	   #$initially_process_everything = $config->get_boolean('initially_process_everything');
	   #$number_of_test_events        = $config->get_number('number_of_test_events');
	   #$origin                       = $config->get_scalar('origin');
	   #@elastic_search_nodes         = $config->get_array('elasticsearch_node');
	   #$cycle_sleep_time             = $config->get_number('cycle_sleep_time');
	   #$cycle_timings                = $config->get_number('cycle_timings');
	   #$enable_processing            = $config->get_boolean('enable_processing');
	   #$health_hostgroup             = $config->get_scalar('health_hostgroup');
	   #$health_hostname              = $config->get_scalar('health_hostname');
	   #$health_service               = $config->get_scalar('health_service');
	    $log4perl_config              = $config->get_scalar('log4perl_config');
	    $logfile                      = $config->get_scalar('logfile');
	   #$monitoring_server            = $config->get_scalar('monitoring_server');
	   #$rest_api_requestor           = $config->get_scalar('rest_api_requestor');
	   #$ws_client_config_file        = $config->get_scalar('ws_client_config_file');
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

sub initialize_options {
    ## command line options processing
    my $helpstring = "\nGroundwork Foundation events to Elasticsearch feeder version $VERSION;\n";
    GetOptions( 
                'help' => \$help, 
                'clean' => \$clean, 
                'version' => \$show_version, 
                'testbatch=i' => \$testbatch, 
                'once' => \$once, ) or die "$helpstring\n";

    if ( defined $help )         { print $helpstring;             exit(0); }
    if ( defined $show_version ) { print "$0 version $VERSION\n"; exit(0); }
}

# -------------------------------------------------------------
sub initialize_elasticsearch
{
    ## FIX LATER: improve validation of the new $elastic_search_object
    undef $elastic_search_object if defined $elastic_search_object;  # want to force a new connection each time
    #$elastic_search_object = Search::Elasticsearch->new( nodes => ["$feeder->{esearchhost}:$feeder->{esearchport}"] );
    $elastic_search_object = Search::Elasticsearch->new( nodes => [ $feeder->{properties}->{elastic_search_nodes} ] );
    defined $elastic_search_object ? return 1 : return 0;
}

# -------------------------------------------------------------
sub get_last_available_logmessageid 
{
    my ($ref_last) = @_;
    my $last;
    my %outcome;
    my %results;

    # FIX MINOR:  This is potentially a MASSIVELY inefficient query, if the "id" field
    # (which presumably translates to logmessage.logmessageid) is not indexed.  Test
    # performance against a huge logmessage table.
    # TBD - agree - fix with more efficient query
    if ( not $feeder->{rest_api}->get_events( [], { count => 1, query => 'order by id desc' }, \%outcome, \%results ) ) {
	    ## $logger->error( "ERROR Could not get event: query was \"count=1&query='order by id desc'\", outcome: " .
	    report_daemon_error( "ERROR Could not get event: query was \"count=1&query='order by id desc'\", outcome: " . to_json( \%outcome, { utf8 => 1, pretty => 1 } ) );
	    return 0;
    }

    # Since get_events() %results is keyed by id, we need look no deeper than the key itself.
    if (%results) {
	    ${$ref_last} = (keys %results)[0];
    }
    else {
        return 0;
    }
    return 1;
}

# -------------------------------------------------------------
sub report_daemon_error 
{
    ## handles errors detected by daemon by making them transparent through a
    ## daemon health service and logging them too.

    my ($message) = @_;

    # don't try to handle error on upsert service as that might be failing, too;
    # put the daemon service into a state to reflect this condition, if we got
    # far enough along that we have an active connection to Foundation
    upsert_daemon_service( $health_service, $message, "UNSCHEDULED CRITICAL" ) if $feeder->{rest_api};

    $logger->error($message) if $logger;
}

# ---------------------------------------------------------------------------------
sub validate_feeder_specific_options
{
    # Logic for validation of feeder-specific options
    # TBD
    return 1;

}

# ---------------------------------------------------------------------------------
sub cleanup
{
    # cleanup attempts to remove any objects that this feeder created :
    # - hosts (will also do services and events due to db relational structure, and devices apparently too)
    # - hostgroups
    # - app types

    my ( $guid, %outcome, %results, @results, $query, $count ) ;

    print "\nAre you sure you want to continue ? Enter 'yes' to clean up... " ; my $go = <STDIN>; chomp $go;
    if ( $go ne 'yes' ) { 
        $logger->error( "Cleanup aborted!");
        $feeder = undef; print "Done.\n"; exit;
    }

    # Get the guid / agentid for this feeder 
    $guid = $feeder->{guid};

    # HOSTS (and therefore services and events too) : get a list of hosts and then try to delete them
    $feeder->{rest_api}->get_hosts( [], { query => "agentId = '$guid'" }, \%outcome, \%results );
    $count = scalar keys %results;
    if ( $count ) {
        if ( not $feeder->{rest_api}->delete_hosts( [ keys %results ] , {} , \%outcome, \@results ) ) {
            print "No hosts deleted (delete_hosts failed?)\n";
        }
        else {
            print "$count hosts deleted\n";
        }
    }
    else {
        print "No hosts deleted\n";
    }

    # HOSTGROUPS : get a list of hostgroups and then try to delete them
    $feeder->{rest_api}->get_hostgroups( [], { query => "agentId ='$guid'" }, \%outcome, \%results );
    $count = scalar keys %results;
    if ( $count ) {
        if ( not $feeder->{rest_api}->delete_hostgroups( [ keys %results ] , {} , \%outcome, \@results ) ) {
            print "No hostgroups deleted (delete_hostgroups failed?)\n";
        }
        else {
            print "$count hostgroups deleted\n";
        }
    }
    else {
        print "No hostgroups deleted\n";
    }
    
    # APP TYPES : get a list of events and then try to delete them
    # Removed for now - removing the app type and then re-adding it seems to cause stack traces in status viewer.
    # Its minor to leave the app type alone this point.
   #if ( not $feeder->{rest_api}->delete_application_types( [ $feeder->{app_type} ] , {}, \%outcome, \@results ) ) {
   #    print "No application type '$feeder->{app_type}' deleted\n";
   #}
   #else { 
   #    print "Application type '$feeder->{app_type}' deleted\n";
   #}
    
    $feeder = undef; print "Done.\n"; exit;
}

# ---------------------------------------------------------------------------------
sub initialize_process_everything
{
    my ( $last_processed_event_id, $last_event_id );

    # if first time running, and service will be created to track last event processed, then
    # if initially process everything is off, then need to seed the service with the id of the
    # last available event.
    # When the feeder starts, just process incoming new events if == 0
    if ( $feeder->{properties}->{initially_process_everything} == 0 ) {
	    if ( not get_last_available_logmessageid( \$last_event_id ) ) {
	        $logger->error("ERROR Failed to initialize health and stats objects including $service_last_event_id_processed service");
            return 0;
	    }
    }
    else {
	    $last_event_id = '[pending]';
    }

    # upsert some services on that host to create them
    # if the service doesn't exist, create it

    my %outcome;
    my %results;

    # FIX LATER:  standardize the error reporting from %outcome and %results
    if ( not $feeder->{rest_api}->get_services( [$service_last_event_id_processed], { hostname => $health_hostname }, \%outcome, \%results ) ) {
	    ## FIX MAJOR: check that the outcome is 'service not found' - otherwise croak
	    upsert_daemon_service( $service_last_event_id_processed, $last_event_id );
    }

    # if the service exists and its value is not a number, set it to pending, else leave it alone
    else {
	    get_last_processed_event_id( \$last_processed_event_id );
	    if ( $last_processed_event_id !~ /^\d+$/ ) {
	        upsert_daemon_service( $service_last_event_id_processed, $last_event_id );
	    }
    }


    return 1;
}

__END__

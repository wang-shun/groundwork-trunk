#!/usr/local/groundwork/perl/bin/perl

# Performs elasticsearch searches and feeds results into Foundation.
#
# Copyright (c) 2014-2016 GroundWork, Inc. (www.gwos.com).  All rights reserved.
# Use of this software is subject to commercial license terms.
#
# Revision History
# v 0.1.0 - Kevin Stone      2/14 Initial Revision based on first version of elastic_scanner.pl
# v 0.2.0 - Dominic Nicholas 2/14 - added dynamically calculated processing subroutine names
# v 0.2.0 - Dominic Nicholas 2/14 - major code cleanup
# v 0.2.0 - Dominic Nicholas 2/14 - increased resilience to failures and improved logging
# v 0.2.0 - Dominic Nicholas 2/14 - added $todaysdate$ macro subs for search terms
# v 0.2.0 - Dominic Nicholas 2/14 - brought searches into more readable maintainable format in the config file
# v 0.2.0 - Dominic Nicholas 2/14 - feeder health service added and wrapper around errors added
# v 0.2.0 - Dominic Nicholas 2/14 - stats services added
# v 0.2.0 - Dominic Nicholas 2/14 - replaced pod doc with usual helpstring stuff
# v 0.2.1 - Dominic Nicholas 3/14 - rest_api_user/rest_api_password in logstash props instead of ws_client props now
# v 0.2.2 - Dominic Nicholas 3/14 - noma integration added - notification if state change detected
# v 0.2.3 - Dominic Nicholas 3/14 - creation/updating of categories - currently commented out pending REST API fix
# v 0.2.4 - Kevin Stone      3/14 - added process_esearch_8()
# v 0.2.5 - Kevin Stone      3/14 - added process_esearch_9()
# v 0.2.6 - Kevin Stone      3/14 - added process_esearch_10()
# v 0.2.7 - Dominic Nicholas 3/14 - updated Elasticsearch to Search::Elasticsearch to pick up latest version
# v 0.2.8 - Dominic Nicholas 3/14 - minor bug fix to generate_hostname()
# v 0.2.9 - Dominic Nicholas 3/14 - remote execution on monitoring_server of noma notifier added
# v 1.0.0 - GH               2014-09-04    upgraded to current GW::RAPID package; general cleanup
# v 1.0.1 - Dominic Nicholas 4/14 - minor fix to reading config where only one search defined
# v 1.0.2 - Dominic Nicholas 4/29 - added 'disabled' prop to search block to quickly disable it vs #'ing block
# v 2.0.0 - Dominic Nicholas 4/30 - refactored to use Feeder.pm; made more robust ie no exits; added -clean and more.
# v 2.0.1 - Dominic Nicholas 5/30 - adjusted search subs to use .raw properties in various places,
#                                   add only_do_enabled_searches and search_verbosity options
# v 2.0.2 - GH               2016-08-25    Die automatically after running for an hour, as a temporary fix to work
#                                          around memory leaks (GWMON-12626), to be restarted by the parent supervise.
#
#
# VIM : set tabstop=4  set expandtab 
#
# TODO : 
# - write help page (note that -clean can only be used when no other feeder is running)
# - clean up formatting of search queries subs
# - revisit $todaysdate$ when time range of query crosses date boundary
# - revisit signal handling
# - log rotation

use strict;
use warnings;
use version;
use GW::RAPID;
use GW::Feeder qv('0.3.1.3');
use JSON;
use Getopt::Long;
use POSIX qw(strftime);
use Time::HiRes;
use Time::Local;
use Log::Log4perl;
use TypedConfig qw(); # leave qw() on to address minor bug in TypedConfig.pm
use Search::Elasticsearch;
use IO::Socket;
use Sys::Hostname;
use File::Basename;
use Data::Dumper; $Data::Dumper::Indent = 1; $Data::Dumper::Sortkeys = 1;
my $VERSION = qv('2.0.2'); # Be sure to keep this up-to-date.
my $config_file = '/usr/local/groundwork/config/logstash_feeder.conf';

# Configuration option vars
my (
    $searchfile,        $send_notifications, $esearchport,      $esearchhost,        $enable_processing,
    $logfile,           $app_type,           $log4perl_config,  $rest_api_requestor, $ws_client_config_file,
    $monitoring_server, $cycle_timings,      $cycle_sleep_time, $health_hostname,    $health_service,
    $health_hostgroup,  $nomaprog,           $remotenoma,       $nomauser,
    ) = undef;

my $feeder_name = "logstash_feeder";
my ( $logger, $feeder ) = undef;

# CLI option vars
my ( $clean, $help, $once, $show_version ) = undef;

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
    #sleep(0);
}

# ------------------------------------------------------------------------------------------------------------
sub main 
{
    ## main sub that does initialization, processing loop, etc.

    my $fresh_start_time = time();

    my (
        $cycle_start_time, $cycle_count,  $search_entry, @searches,               $search,     @search_terms,
        $state,            $search_terms, @pair,         $key,                    $value,      $hashref_search,
        $proc_name,        $total_etime,  $total_count,  $total_cycle_time_taken, $total_took, 
        $successful_esearch_count, $unsuccessful_esearch_count, 
        $successfully_processed_into_gw, $unsuccessfully_processed_into_gw,
        $try, $max_retries, $cycle_end_time , $cycle_elapsed,
    );

    my $started_at = localtime;

    # read and process cli opts
    initialize_options();

    # Check for other feeders running - there can only be one
    if ( perl_script_process_count( basename($0) ) > 1 ) {
        print("Another $feeder_name is running - quitting\n") ;
        exit;
    }

    # read option values - need this to get the logger stuff - bit inefficient reading the conf twice
    read_daemon_config_file( $config_file, \@searches );

    # initialize the logger
    if ( not initialize_logger('started') ) {
        print "Terminating error: cannot initialize the logger; quitting in 5 seconds.\n";
        sleep 5;    # helpful for when running under supervise
        exit (1);
    }

    # Set options up for new feeder
    my %feeder_options = (

        # The log4perl logger
        logger => $logger,

        # Feeder specific options to retrieve and type-check.
        # Access standard or specific properties with $feeder->{properties}->{propertname}, eg $feeder->{properties}->{cycle_time}
        feeder_specific_properties => {
                                        esearchhost => 'scalar',
                                        esearchport => 'number',
                                        host_bundle_size => 'number',
                                        hostgroup_bundle_size => 'number',
                                        log4perl_config => 'array',
                                        only_do_enabled_searches => 'boolean',
                                        service_bundle_size => 'number',
                                        system_indicator_check_frequency => 'number',
                                        search => 'hash',
                                        search_verbosity => 'boolean',
                                      }
    );

    # log app start
    $logger->info("======== $feeder_name starting ========");

    # perform elasticsearch searches in a never ending loop
    $cycle_count = 1;
    CYCLE: while (1) {

        # FIX MAJOR:  The code here ought to be restructured to examine the
        # last-modified timestamp of the config file, and only re-read it if
        # it has changed since the last time we read the config file.
        #
        # reread the config file again to pick up any new search rules
        # don't quit if there's an error though
        if ( not read_daemon_config_file( $config_file, \@searches ) ) {
            sleep 5;
            next CYCLE;
        }

        # Create a new feeder object per cycle to avoid any potential REST expiration/disconnection issues with long cycle times
        undef $feeder if defined $feeder; # First destroy feeder if defined 

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

        # reset metrics stats per cycle...
        $successful_esearch_count = $unsuccessful_esearch_count = $total_count = $total_etime = $total_took = 0;
        $successfully_processed_into_gw = $unsuccessfully_processed_into_gw = 0;

        # start cycle time measurement
        $cycle_start_time = Time::HiRes::time();

        $logger->info("---- Starting cycle $cycle_count ----");

        # loop over the searches as defined in the config file
        SEARCH: foreach $hashref_search (@searches) {

            # if all searches are disabled globally ... 
            if ( $feeder->{properties}->{only_do_enabled_searches} ) {
                if ( not defined $hashref_search->{enabled} ) { # if this search doesnt have 'enbled' set, skip it 
                    $logger->debug("Searches globally disabled, and search not explicitly enabled - skipping search block");
                    next SEARCH;
                }
            }

            # skip disabled searches
            if ( defined $hashref_search->{disabled} ) { 
                $logger->debug("Skipping disabled search block");
                next SEARCH;
            }

            # figure out what we are going to use for hostname
            generate_hostname($hashref_search);
    
            # figure out what we are going to use for hostgroupname
            generate_hostgroup($hashref_search);
    
            # figure out what we are going to use for servicename
            generate_servicename($hashref_search);
    
            # expand any special strings in es_index
            generate_search_index($hashref_search);
    
            # Build the subroutine name dynamically and then run it
            $proc_name = "process_esearch_" . $hashref_search->{stype};
            if ( defined( &{$proc_name} ) ) {
                if ( not &{ \&{$proc_name} }($hashref_search) ) { # malarky with &{\& etc is to avoid turning off strict temporarily (balance:})
                    report_daemon_error("Procedure '$proc_name()' failed - search results will not be posted into Foundation");
                    $unsuccessful_esearch_count++;  
                    next SEARCH;
                }
                else {
                    $successful_esearch_count++; 
                    $hashref_search->{LastCheckTime} = get_current_time();
                    # log the search message if verbosity set globally, or for the search specifically
                    if ( defined $hashref_search->{verbose} or $feeder->{properties}->{search_verbosity} ) { 
                        $logger->info($hashref_search->{message});
                    }
                }
            }
            else {
                report_daemon_error( "Procedure '$proc_name()' is not defined - search type was $hashref_search->{stype} - search will be skipped");
                $unsuccessful_esearch_count++;  
                next SEARCH;
            }
    
            # metrics
            $total_took  += $hashref_search->{took}; # took is how long it took to execute the search request in milliseconds
            $total_etime += $hashref_search->{etime}; # our own measurement of how long es search() takes
            $total_count += $hashref_search->{count}; # total hits

            # determine check state from search data and add it to the search hash
            get_state($hashref_search);

            # process above was essentially building the data we're interested in now processing into GW
            if ( not process_built_event( $hashref_search ) ) {
                $logger->error("Failed to process built logstash feeder event");
                $unsuccessfully_processed_into_gw++;
            }
            else {
                $successfully_processed_into_gw++;
            }

        }

        # converts audit into events in foundation, and empties it
        $feeder->flush_audit() ;  # this will produce its own errors if necessary

        $cycle_end_time = Time::HiRes::time();
        $cycle_elapsed  = $cycle_end_time - $cycle_start_time;
        my $finished_time = localtime();
        update_feeder_stats( $cycle_count, 
                             $finished_time,
                             $cycle_elapsed,
                             $successful_esearch_count,           # count of all enabled and successfully run esearches
                             $unsuccessful_esearch_count,         # count of all enabled but unsuccessfully esearches
                             $successfully_processed_into_gw,    # count of searches that were successfully processed into GW
                             $unsuccessfully_processed_into_gw,  # count of searches that were unsuccessfully processed into GW
                             $total_took,                        # total esearch time across all processed searches
                             $total_etime,                       # our own measurement of es searching across all processed searches
                            #$total_count,                       # total hists across all searches run - not useful
                           ); 

        if ( defined $cycle_timings ) { $logger->info( "Cycle took " . sprintf( "%.3f", $cycle_elapsed ) . " seconds" ); }

        $cycle_count++;

        # update_logstash_feeder_stats( $search_count, $cycle_elapsed, $total_took ); 

        # done if -once option given
        if ( $once ) {
            upsert_daemon_service( "${feeder_name}_health", "Run-once option supplied - feeder shut down - exiting");
            $logger->info( "Run-once option supplied - feeder shut down - exiting" );
            undef $feeder ;
            exit;
        };

        $logger->info("Sleeping $feeder->{properties}->{system_indicator_check_frequency} seconds");
        sleep $feeder->{properties}->{system_indicator_check_frequency};

    }
}

# -------------------------------------------------------------
sub process_built_event
{
    # Process a built logstash search event.
    # This routine does this with the built event :
    #    
    #    get host state from foundation for this event
    #    upsert host with new state
    #    upsert host into hostgroup(s)
    #    get service state from foundation for this event
    #    upsert service with new state and message
    #    if the host and/or service state has changed, post events and notifications
    # 
    # Returns 1 if ok, 0 otherwise

    my ( $built_event ) = @_;
    my ( %hosts_states );

    # if the incoming event is empty - just flag that and return failure
    if ( not scalar keys %{$built_event} or not $built_event ) { 
        $logger->warn( "Empty event - nothing to process"); 
        return 0;
    }
        
    # Host - get Foundation state of host - adds FoundationHostState to event
    if ( not get_foundation_host_state( $built_event ) ) { 
        $logger->error( "Error getting foundation host state" );
        return 0;
    }

    # Upserts the host into Foundation, including the host's state
    # For now, assume the host is just UP
    #$built_event->{HostState} = 'UNSCHEDULED DOWN'; # for testing
    $built_event->{HostState} = 'UP';

    # Also, for now, just set the time for the host update to be now
    # A time consumable by the GW REST API is required especially for processing retry cache entries and posting events
    $built_event->{HostReportDate} = strftime( '%Y-%m-%dT%H:%M:%S%z', localtime() );


    if ( not upsert_foundation_host( $built_event ) ) {
        $logger->error( "Error upserting host $built_event->{Host}" );
        return 0;
    }

    # Add the host to a hostgroup        
    if ( not upsert_foundation_hostgroups_with_host( $built_event ) ) {  
        $logger->error( "Error upserting hostgroups" ) ;
        return 0; 
    }

    # Get the service state as it is now in foundation - adds FoundationServiceState to the event
    if ( not get_foundation_service_state( $built_event ) ) { 
        $logger->error( "Error getting foundation service state" ) ;
        return 0;
    }


    # Upsert the service into foundation
    # for now, the service report date can be set to now
    $built_event->{ServiceReportDate} = strftime( '%Y-%m-%dT%H:%M:%S%z', localtime() );
    if ( not upsert_foundation_service( $built_event) ) {
        $logger->error( "Error upserting service" ) ;
        return 0;
    }

    # Post events and notifications
    # Re-using the existing post_events_and_notifications() routine but for just one event and one host ...
    # TBD skipping for now because for cacti feeder we always send full updates anyway - ie this function is of limited use and not worth adding in here
    # If necessary, reduce the set of cacti host and service events to only those which are having a state change.
    # Only do this filtering if a) always_send_full_updates = false, and b) we're on a full_update_frequency cycle, and c) its not the very first cycle
    #if ( not $feeder->{always_send_full_updates} and ( $cycle_iteration % $feeder->{full_update_frequency} != 0 ) and ( $cycle_iteration != 1 )  ) {
    #    filter_out_non_state_changed_cacti_hosts_and_services( \@cacti_events, \%hosts_states );
    #    # Again, record the new event set size as the size that was processed (even if there are errors, the were 'processed')
    #    ${$ref_total_events_processed} = $#cacti_events + 1; 
    #}

    %hosts_states = ( 
        $built_event->{Host} => { 
            'HostState' => $built_event->{HostState}, 
            'FoundationHostState' => $built_event->{FoundationHostState} 
        } 
    ) ;

    if ( not post_events_and_notifications( [ $built_event ] , \%hosts_states, $built_event ) ) {
        $logger->error( "Error posting events and notifications");
        return 0; 
    }

    return 1;

}

# ------------------------------------------------------------- 
sub initialize_logger 
{
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
    $logger = Log::Log4perl::get_logger("Logstash.Feeder");

    $SIG{__WARN__} = sub { $logger->warn("WARN @_"); };

    # The __DIE__ handler catches Perl run-time errors and could be used to get them logged.  But
    # it also captures lots of internal detail that is caught by eval{}; statements, that has no
    # business being logged.  So we use a better mechanism, provided below, for capturing Perl
    # errors, by explicitly redirecting STDERR to the logfile.
    # $SIG{__DIE__} = sub { $logger->fatal("DIE @_"); };

    if ( not open STDERR, '>>', $logfile ) {
        $logger->logdie("Cannot redirect STDERR to the log file \"$logfile\": $!");
    }

    return 1;
}

# -------------------------------------------------------------
sub terminate_rest_api 
{
    ## Release our handle to the REST API (if we used it), to force the REST API to call its destructor.
    ## This will attempt to log out before Perl's global destruction pass wipes out resources needed for
    ## logout to work properly.
    $feeder->{rest_api} = undef;
}

# ------------------------------------------------------------- #
sub upsert_daemon_service {
    ## Create or update a daemon service that is attached to the health vhost
    ## The only things this subroutine currently updates are LastPluginOutput

    my ( $service, $message, $status ) = @_;
    my %statuses = ( "0" => "OK", "1" => "WARNING", "2" => "UNSCHEDULED CRITICAL", "3" => "UNKNOWN" );

    $logger->debug("Upserting $feeder_name service '$service' with message \"$message\"");

    my $now = strftime( '%Y-%m-%dT%H:%M:%S', localtime );
    my $tz = strftime( "%z", localtime() );

    # figure out status to pass in to API
    if ( defined $status ) {
        if ( $status =~ /^\d+$/ ) {
            if ( defined $statuses{$status} ) {
                $status = $statuses{$status};
            }
            else {
                $logger->error("Unrecognized status value '$status' - setting to UNKNOWN");
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
        $logger->error( "Could not upsert service '$service' on host '$feeder->{properties}->{health_hostname}' with message \"$message\": " . to_json( \%outcome, { utf8 => 1, pretty => 1 } ) );
    }
}

# ------------------------------------------------------------- 
# Not currently used, but left in as opentsdb future reference example
sub update_logstash_feeder_stats 
{
    ## updates some stats for logstash_feeder
    ## Note on 'took':
    ## "The time reported by elasticsearch in the "took" field is the time that it took
    ## elasticsearch to process the query on its side. It doesn't include
    ## - serializing the request into JSON on the client
    ## - sending the request over the network
    ## - deserializing the request from JSON on the server
    ## - serializing the response into JSON on the server
    ## - sending the response over the network
    ## - deserializing the response from JSON on the client"

    my ( $search_count, $cycle_elapsed, $cycle_took ) = @_;
    my ( $message, $searchespersec );
    $cycle_took = $cycle_took / 1000;

    # To avoid potential divide-by-zero errors, let's impose a minimum $cycle_elapsed time.
    # FIX MINOR:  Really, this ought to be one hi-res clock tick on the current platform, but I'm not sure how to fetch such a value.
    my $min_cycle_elapsed_time = 0.000_000_001;
    $cycle_elapsed = $min_cycle_elapsed_time if $cycle_elapsed < $min_cycle_elapsed_time;

    $searchespersec = $search_count / $cycle_elapsed;
    $message        = $searchespersec . " searches per second," . $search_count . " Searches executed in " . $cycle_elapsed . " seconds";
    $logger->debug("$message");
    upsert_daemon_service( "${feeder_name}_health", $message, "OK" );

    my ( $pct_took, $pct_feeder, $avg_elapsed, $avg_took ) = ();
    $pct_took    = $cycle_took / $cycle_elapsed;
    $pct_feeder  = 1 - ( $cycle_took / $cycle_elapsed );

    $avg_elapsed = $search_count ? $cycle_elapsed / $search_count : 0;
    $avg_took    = $search_count ? $cycle_took / $search_count    : 0;

    # KDS quick and dirty to get some performance data on the feeder
    # FIX LATER: fix so it uses the perfdata API, and non-fixed parameters

    my $tsdb_host    = "gwmon-03";
    my $tsdb_port    = "4242";
    my $source       = "ps-70-esearch-connector-dev_LOGSTASH_FEEDER";
    my $timestamp    = time;
    my $host         = $feeder->{properties}->{health_hostname};
    my $service      = "${feeder_name}_health";
    my $post_to_tsdb = 0;

    if ($post_to_tsdb) {
        tsdbwrite( $tsdb_host, $tsdb_port, $source, $timestamp, $host, $service, "took_avg_s",       $avg_took );
        tsdbwrite( $tsdb_host, $tsdb_port, $source, $timestamp, $host, $service, "took_total_s",     $cycle_took );
        tsdbwrite( $tsdb_host, $tsdb_port, $source, $timestamp, $host, $service, "elapsed_avg_s",    $avg_elapsed );
        tsdbwrite( $tsdb_host, $tsdb_port, $source, $timestamp, $host, $service, "elapsed_total_s",  $cycle_elapsed );
        tsdbwrite( $tsdb_host, $tsdb_port, $source, $timestamp, $host, $service, "pct_time_es",      $pct_took );
        tsdbwrite( $tsdb_host, $tsdb_port, $source, $timestamp, $host, $service, "pct_time_feeder",  $pct_feeder );
        tsdbwrite( $tsdb_host, $tsdb_port, $source, $timestamp, $host, $service, "search_count",     $search_count );
        tsdbwrite( $tsdb_host, $tsdb_port, $source, $timestamp, $host, $service, "searches_per_sec", $searchespersec );
    }
}

# ------------------------------------------------------------- #
sub read_daemon_config_file 
{
    ## reads the logstash_feeder config file
    my ( $config_file, $arrayref_searches ) = @_;
    my $config;

    $logger->debug("Reading and processing config file: $config_file") if $logger;

    eval {
        $config                = TypedConfig->new($config_file);
        #$app_type              = $config->get_scalar('app_type');
        ##$cycle_sleep_time      = $config->get_number('cycle_sleep_time');
        #$cycle_timings         = $config->get_number('cycle_timings');
        #$enable_processing     = $config->get_boolean('enable_processing');
        #$esearchhost           = $config->get_scalar('esearchhost');
        #$esearchport           = $config->get_scalar('esearchport');
        #$health_hostgroup      = $config->get_scalar('health_hostgroup');
        #$health_hostname       = $config->get_scalar('health_hostname');
        #$health_service        = $config->get_scalar('health_service');
        $log4perl_config       = $config->get_scalar('log4perl_config');
        $logfile               = $config->get_scalar('logfile');
        #$monitoring_server     = $config->get_scalar('monitoring_server');
        #$nomaprog              = $config->get_scalar('nomaprog');
        #$nomauser              = $config->get_scalar('nomauser');
        #$remotenoma            = $config->get_boolean('remotenoma');
        #$rest_api_requestor    = $config->get_scalar('rest_api_requestor');
        #$send_notifications    = $config->get_boolean('send_notifications');
        #$ws_client_config_file = $config->get_scalar('ws_client_config_file');
    };
    if ($@) {
        chomp $@;
        $@ =~ s/^ERROR:\s+//i;
        $logger->error("Cannot read config file $config_file\n    ($@)\n") if $logger;
        return 0;
    }

    # populate the searches array with an array of hashes, one hash per search
    if ( ref $config->{search} ne 'ARRAY' ) {
        @{$arrayref_searches} = ( $config->{search} );
    }
    else {
        @{$arrayref_searches} = @{ $config->{search} };
    }

    return 1;
}

# -------------------------------------------------------------
sub initialize_options 
{
    ## command line options processing
    my $helpstring = "\nGroundwork Elasticsearch to Foundation feeder version $VERSION\n\n"; # TBD
    GetOptions( 'clean' => \$clean,
                'help' => \$help, 
                'once' => \$once, 
                'version' => \$show_version, 
              ) or die "$helpstring\n";

    if ( defined $help )         { print $helpstring;             exit(0); }
    if ( defined $show_version ) { print "$0 version $VERSION\n"; exit(0); }
}

# ------------------------------------------------------------- #
sub report_daemon_error {
    ## handles errors detected by daemon by making them transparent through a
    ## daemon health service and logging them too.

    my ($message) = @_;

    # don't try to handle error on upsert service as that might be failing, too;
    # put the daemon service into a state to reflect this condition, if we got
    # far enough along that we have an active connection to Foundation
    upsert_daemon_service( "${feeder_name}_health", $message, "UNSCHEDULED CRITICAL" ) if $feeder;

    $logger->error($message) if $logger;
}

# ------------------------------------------------------------- 
# determine check state from search data
sub get_state 
{
    my ($hashref_search) = @_;
    my $state            = 3; # default to unknown
    my $count            = $hashref_search->{count};
    my $warn             = $hashref_search->{warn};
    my $crit             = $hashref_search->{crit};

    if ( $count < $warn )  { $state = "OK"; }
    if ( $count >= $warn ) { $state = "WARNING"; }
    if ( $count >= $crit ) { $state = "UNSCHEDULED CRITICAL"; }

    $hashref_search->{ServiceState} = $state;
}

# ------------------------------------------------------------- 
# process type 1 queries "host and words"
sub process_esearch_1 {
    my ($hashref_search) = @_;
    my ( $es_result, $count, $took, $etime, $message ) = ();

    my $start_time    = Time::HiRes::time();
    my $es_index      = $hashref_search->{es_index};
    my $es_message    = $hashref_search->{es_message};
    my $es_host       = $hashref_search->{es_host};
    my $es_timeperiod = $hashref_search->{es_timeperiod};

    eval {
    $es_result = $elastic_search_object->search(
        index       => "$es_index",    # or undef, (all)
        search_type => 'count',
        body        => {
        'filter' => { 'range' => { '@timestamp' => { 'gte' => $es_timeperiod } } },
        'query'  => {
            'bool' => {
            'must' => [
                #{ 'match_phrase' => { 'host' => $es_host } },
                { 'match_phrase' => { 'host.raw' => $es_host } }, # Changed to host.raw in V2.0 to avoid analysis and tokenization for this one - as-is
                { 'match' => { 'message' => $es_message } }
                ##   {
                ##       'match' => {
                ##           '_type' => $es_type
                ##       }
                ##   },
                ##   {
                ##       'match' => {
                ##           'path' => $es_path
                ##       }
                ##   },
            ]
            }
        }
        }
    );
    };    # end eval

    if ($@) {
        my $host    = $hashref_search->{hostname};
        my $service = $hashref_search->{servicename};
        report_daemon_error("!!! Could not execute search type 1 for host $host service $service: $!");
        return 0;
    }

    $count = ${$es_result}{hits}{total};
    $took  = ${$es_result}{took};

    $etime   = Time::HiRes::time() - $start_time;
    $message = "$count Messages found from Host $es_host and Word(s) \"$es_message\" since $es_timeperiod";

    $hashref_search->{count}   = $count;
    $hashref_search->{took}    = $took;
    $hashref_search->{etime}   = $etime;
    $hashref_search->{message} = $message;

    return 1;    # success
}

# ------------------------------------------------------------- #

# process type 2 queries match "host phrase"
sub process_esearch_2 {
    my ($hashref_search) = @_;
    my ( $es_result, $count, $took, $etime, $message ) = ();

    my $start_time = Time::HiRes::time();
    my $es_index   = $hashref_search->{es_index};

    #my $es_type       = $hashref_search->{es_type};
    my $es_message    = $hashref_search->{es_message};
    my $es_host       = $hashref_search->{es_host};
    my $es_timeperiod = $hashref_search->{es_timeperiod};

    eval {
    $es_result = $elastic_search_object->search(
        index       => "$es_index",    # or undef, (all)
        search_type => 'count',
        body        => {
        'filter' => { 'range' => { '@timestamp' => { 'gte' => $es_timeperiod } } },
        'query'  => {
            'bool' => {
            'must' => [
                #{ 'match_phrase' => { 'host' => $es_host } },
                { 'match_phrase' => { 'host.raw' => $es_host } }, # Changed to host.raw in V2.0 to avoid analysis and tokenization for this one - as-is
                { 'match_phrase' => { 'message' => $es_message } }
                ##  { 'match' => { '_type' => $es_type } }, 
                ##  { 'match' => { 'path' => $es_path } },
            ]
            }
        }
        }
    );
    };

    if ($@) {
    my $host    = $hashref_search->{hostname};
    my $service = $hashref_search->{servicename};
    report_daemon_error("!!! Could not execute search type 2 for host $host service $service: $!");
    return 0;
    }

    $count   = ${$es_result}{hits}{total};
    $took    = ${$es_result}{took};
    $etime   = Time::HiRes::time() - $start_time;
    $message = "$count Messages found from Host $es_host and with phrase \"$es_message\" since $es_timeperiod";

    $hashref_search->{count}   = $count;
    $hashref_search->{took}    = $took;
    $hashref_search->{etime}   = $etime;
    $hashref_search->{message} = $message;

    return 1;    # success
}

# -------------------------------------------------------------
# process type 3 queries match "host path words"
sub process_esearch_3 {
    my ($hashref_search) = @_;
    my ( $es_result, $count, $took, $etime, $message ) = ();

    my $start_time    = Time::HiRes::time();
    my $es_index      = $hashref_search->{es_index};
    my $es_path       = $hashref_search->{es_path};
    my $es_message    = $hashref_search->{es_message};
    my $es_host       = $hashref_search->{es_host};
    my $es_timeperiod = $hashref_search->{es_timeperiod};

    eval {
    $es_result = $elastic_search_object->search(
        index => $es_index,    # or undef, (all)
        search_type => 'count', # Uncommented this out in v 2.0.1
        body => {
        'filter' => { 'range' => { '@timestamp' => { 'gte' => $es_timeperiod } } },
        'query'  => {
            'bool' => {
            'must' => [
                #{ 'match_phrase' => { 'host' => $es_host } }, 
                #{ 'match_phrase' => { 'path' => $es_path } },
                { 'match_phrase' => { 'host.raw' => $es_host } }, # Changed to host.raw in V2.0 to avoid analysis and tokenization for this one - as-is
                { 'match' => { 'path.raw' => $es_path } }, # Changed to path.raw in V2.0 to avoid analysis and tokenization for this one - as-is
                { 'match' => { 'message' => $es_message } }
                ##   {
                ##       'match' => {
                ##           '_type' => $es_type
                ##       }
                ##   },
            ]
            }
        }
        }
    );
    };

    if ($@) {
    my $host    = $hashref_search->{hostname};
    my $service = $hashref_search->{servicename};
    report_daemon_error("!!! Could not execute search type 3 for host $host service $service: $!");
    return 0;
    }

    $count = ${$es_result}{hits}{total};
    $took  = ${$es_result}{took};

    $etime   = Time::HiRes::time() - $start_time;
    $message = "$count Messages found from Host $es_host and Path $es_path with words(s) \"$es_message\" since $es_timeperiod";

    $hashref_search->{count}   = $count;
    $hashref_search->{took}    = $took;
    $hashref_search->{etime}   = $etime;
    $hashref_search->{message} = $message;

    return 1;    # success
}

# ------------------------------------------------------------- 
# process type 4 queries match host path phrase
sub process_esearch_4 {
    my ($hashref_search) = @_;
    my ( $es_result, $count, $took, $etime, $message ) = ();

    my $start_time    = Time::HiRes::time();
    my $es_index      = $hashref_search->{es_index};
    my $es_path       = $hashref_search->{es_path};
    my $es_message    = $hashref_search->{es_message};
    my $es_host       = $hashref_search->{es_host};
    my $es_timeperiod = $hashref_search->{es_timeperiod};

    eval {
        $es_result = $elastic_search_object->search(
            index => $es_index,    # or undef, (all)
            ## search_type => 'count',
            body => {
                'filter' => { 'range' => { '@timestamp' => { 'gte' => $es_timeperiod } } },
                'query'  => {
                    'bool' => {
                        'must' => [
                            { 'match_phrase' => { 'host.raw' => $es_host } },
                            #{ 'match_phrase' => { 'path' => $es_path } },
                            { 'match_phrase' => { 'path.raw' => $es_path } }, # Changed to .raw in V2.0 to avoid analysis and tokenization for this one - as-is
                            { 'match_phrase' => { 'message' => $es_message } }
                       ##   {
                       ##       'match' => {
                       ##           '_type' => $es_type
                       ##       }
                       ##   },
                        ]
                    }
                }
            }
        );
    };
    if ($@) {
        my $host    = $hashref_search->{hostname};
        my $service = $hashref_search->{servicename};
        report_daemon_error("!!! Could not execute search type 4 for host $host service $service: $!");
        return 0;
    }

    $count = ${$es_result}{hits}{total};
    $took  = ${$es_result}{took};

     print "---- Type 4 $count\n"; print Dumper($es_result);

    $etime   = Time::HiRes::time() - $start_time;
    $message = "$count Messages found from Host $es_host and Path $es_path with phrase \"$es_message\" since $es_timeperiod";

    $hashref_search->{count}   = $count;
    $hashref_search->{took}    = $took;
    $hashref_search->{etime}   = $etime;
    $hashref_search->{message} = $message;

    return 1;    # success
}

# ------------------------------------------------------------- #

# process type 5 queries Windows Event Logs match "ComputerName"  "Logfile" "Words"
sub process_esearch_5 {
    my ($hashref_search) = @_;
    my ( $es_result, $count, $took, $etime, $message ) = ();

    my $start_time      = Time::HiRes::time();
    my $es_index        = $hashref_search->{es_index};
    my $es_type         = $hashref_search->{es_type};
    my $es_message      = $hashref_search->{es_message};
    my $es_ComputerName = $hashref_search->{es_ComputerName};
    my $es_timeperiod   = $hashref_search->{es_timeperiod};

    eval {
    $es_result = $elastic_search_object->search(
        index       => $es_index,    # or undef, (all)
        search_type => 'count',
        body        => {
        'filter' => { 'range' => { '@timestamp' => { 'gte' => $es_timeperiod } } },
        'query'  => {
            'bool' => {
            'must' => [
                { 'match_phrase' => { 'ComputerName.raw' => $es_ComputerName } }, # Changed to .raw in V2.0 to avoid analysis and tokenization for this one - as-is
                { 'match'        => { '_type'        => $es_type } },
                { 'match'        => { 'message'      => $es_message } },
            ]
            }
        }
        }
    );
    };

    if ($@) {
    my $host    = $hashref_search->{hostname};
    my $service = $hashref_search->{servicename};
    report_daemon_error("!!! Could not execute search type 5 for host $host service $service: $!");
    return 0;
    }

    $count = ${$es_result}{hits}{total};
    $took  = ${$es_result}{took};

    $etime   = Time::HiRes::time() - $start_time;
    $message = "$count Messages found from ComputerName $es_ComputerName and Type $es_type with words \"$es_message\" since $es_timeperiod";

    $hashref_search->{count}   = $count;
    $hashref_search->{took}    = $took;
    $hashref_search->{etime}   = $etime;
    $hashref_search->{message} = $message;

    return 1;    # success
}

# -------------------------------------------------------------
# process type 6 queries Windows Event Logs match "ComputerName" "_type" "Phrase" (from logstash agent)
sub process_esearch_6
{
    my ($hashref_search) = @_;
    my ( $es_result, $count, $took, $etime, $message ) = ();

    my $start_time      = Time::HiRes::time();
    my $es_index        = $hashref_search->{es_index};
    my $es_type         = $hashref_search->{es_type};
    my $es_message      = $hashref_search->{es_message};
    my $es_ComputerName = $hashref_search->{es_ComputerName};
    my $es_timeperiod   = $hashref_search->{es_timeperiod};

    eval {
        $es_result = $elastic_search_object->search(
            index       => $es_index,    # or undef, (all)
            search_type => 'count',
            body        => {
                'filter' => { 'range' => { '@timestamp' => { 'gte' => $es_timeperiod } } },
                'query'  => {
                    'bool' => {
                        'must' => [
                            #{ 'match' => { 'ComputerName' => $es_ComputerName } },
                            { 'match' => { 'ComputerName.raw' => $es_ComputerName } }, # Changed to .raw in V2.0 to avoid analysis and tokenization for this one - as-is
                            { 'match'        => { '_type'        => $es_type } },
                            { 'match_phrase' => { 'message'      => $es_message } },
                        ]
                    }
                }
           }
       );
    };

    if ($@) {
        my $host    = $hashref_search->{hostname};
        my $service = $hashref_search->{servicename};
        report_daemon_error("!!! Could not execute search type 6 for host $host service $service: $!");
        return 0;
    }

    $count = ${$es_result}{hits}{total};
    $took  = ${$es_result}{took};

    ## print "Type 6 $count\n"; print Dumper($es_result);

    $etime   = Time::HiRes::time() - $start_time;
    $message = "$count Messages found from ComputerName $es_ComputerName and Type $es_type with phrase \"$es_message\" since $es_timeperiod";

    $hashref_search->{count}   = $count;
    $hashref_search->{took}    = $took;
    $hashref_search->{etime}   = $etime;
    $hashref_search->{message} = $message;

    return 1;    # success
}

# -------------------------------------------------------------
# process type 6 queries Windows Event Logs match "Hostname" "_type" "Phrase" (from nxlog)
sub process_esearch_6_nxlog
{
    my ($hashref_search) = @_;
    my ( $es_result, $count, $took, $etime, $message ) = ();

    my $start_time      = Time::HiRes::time();
    my $es_index        = $hashref_search->{es_index};
    my $es_type         = $hashref_search->{es_type};
    my $es_message      = $hashref_search->{es_message};
    my $es_ComputerName = $hashref_search->{es_ComputerName};
    my $es_timeperiod   = $hashref_search->{es_timeperiod};

    eval {
        $es_result = $elastic_search_object->search(
            index       => $es_index,    # or undef, (all)
            search_type => 'count',
            body        => {
                'filter' => { 'range' => { '@timestamp' => { 'gte' => $es_timeperiod } } },
                'query'  => {
                    'bool' => {
                        'must' => [
                            #{ 'match_phrase' => { 'Hostname'     => $es_ComputerName } }, # The only bit that is different :) Hostname - this is from nxlog ...
                            { 'match_phrase' => { 'Hostname.raw'     => $es_ComputerName } }, # ... and changed to .raw in V2.0 to avoid analysis and tokenization for this one - as-is
                            { 'match'        => { '_type'        => $es_type } },
                            { 'match_phrase' => { 'message'      => $es_message } },
                        ]
                    }
                }
           }
       );
    };

    if ($@) {
        my $host    = $hashref_search->{hostname};
        my $service = $hashref_search->{servicename};
        report_daemon_error("!!! Could not execute search type 6 for host $host service $service: $!");
        return 0;
    }

    $count = ${$es_result}{hits}{total};
    $took  = ${$es_result}{took};

    ## print "Type 6 $count\n"; print Dumper($es_result);

    $etime   = Time::HiRes::time() - $start_time;
    $message = "$count Messages found from Hostname $es_ComputerName and Type $es_type with phrase \"$es_message\" since $es_timeperiod";

    $hashref_search->{count}   = $count;
    $hashref_search->{took}    = $took;
    $hashref_search->{etime}   = $etime;
    $hashref_search->{message} = $message;

    return 1;    # success
}# ------------------------------------------------------------- #

# process type 7 queries Windows Event Logs match "ComputerName"  "Logfile" "EventiCode"
sub process_esearch_7 {
    my ($hashref_search) = @_;
    my ( $es_result, $count, $took, $etime, $message ) = ();

    my $start_time      = Time::HiRes::time();
    my $es_index        = $hashref_search->{es_index};
    my $es_type         = $hashref_search->{es_type};
    my $es_ComputerName = $hashref_search->{es_ComputerName};
    my $es_EventCode    = $hashref_search->{es_EventCode};
    my $es_timeperiod   = $hashref_search->{es_timeperiod};
    eval {
    $es_result = $elastic_search_object->search(
        index       => $es_index,    # or undef, (all)
        search_type => 'count',
        body        => {
        'filter' => { 'range' => { '@timestamp' => { 'gte' => $es_timeperiod } } },
        'query'  => {
            'bool' => {
            'must' => [
                #{ 'match_phrase' => { 'ComputerName' => $es_ComputerName } },
                { 'match_phrase' => { 'ComputerName.raw' => $es_ComputerName } }, # Changed to .raw in V2.0 to avoid analysis and tokenization for this one - as-is
                { 'match'        => { '_type'        => $es_type } },
                { 'match'        => { 'EventCode'    => $es_EventCode } },
            ]
            }
        }
        }
    );
    };

    if ($@) {
    my $host    = $hashref_search->{hostname};
    my $service = $hashref_search->{servicename};
    report_daemon_error("!!! Could not execute search type 7 for host $host service $service: $!");
    return 0;
    }

    $count = ${$es_result}{hits}{total};
    $took  = ${$es_result}{took};

    $etime = Time::HiRes::time() - $start_time;
    $message =
      "$count Messages found from ComputerName $es_ComputerName and Type $es_type with Event Code \"$es_EventCode\" since $es_timeperiod";

    $hashref_search->{count}   = $count;
    $hashref_search->{took}    = $took;
    $hashref_search->{etime}   = $etime;
    $hashref_search->{message} = $message;

    return 1;    # success
}

# ------------------------------------------------------------- #

# process type 8 queries foundation events by Hostgroup
sub process_esearch_8 {
    my ($hashref_search) = @_;
    my ( $es_result, $count, $took, $etime, $message ) = ();
    my $start_time       = Time::HiRes::time();
    my $es_index         = $hashref_search->{es_index};
    my $es_type          = $hashref_search->{es_type};
    my $es_monitorStatus = $hashref_search->{es_monitorStatus};
    my $es_hostgroups    = $hashref_search->{es_hostgroups};
    my $es_timeperiod    = $hashref_search->{es_timeperiod};
    eval {
    $es_result = $elastic_search_object->search(
        index       => $es_index,    # or undef, (all)
        search_type => 'count',
        body        => {
        'filter' => { 'range' => { '@timestamp' => { 'gte' => $es_timeperiod } } },
        'query'  => {
            'bool' => {
            'must' => [
                { 'match_phrase' => { 'hostgroups'    => $es_hostgroups } },
                { 'match'        => { '_type'         => $es_type } },
                { 'match'        => { 'monitorStatus' => $es_monitorStatus } },
            ]
            }
        }
        }
    );
    };

    if ($@) {
        my $host    = $hashref_search->{hostname};
        my $service = $hashref_search->{servicename};
        report_daemon_error("!!! Could not execute search type 8 for host $host service $service: $!");
    return 0;
    }

    $count   = ${$es_result}{hits}{total};
    $took    = ${$es_result}{took};
    $etime   = Time::HiRes::time() - $start_time;

    $message = "$count Foundation Events from Hostgroup(s) $es_hostgroups and monitorStatus $es_monitorStatus  since $es_timeperiod";
    $hashref_search->{count}   = $count;
    $hashref_search->{took}    = $took;
    $hashref_search->{etime}   = $etime;
    $hashref_search->{message} = $message;
    return 1;    # success
}

# ------------------------------------------------------------- 
sub initialize_elasticsearch 
{
    ## FIX LATER: improve validation of the new $elastic_search_object
    undef $elastic_search_object if defined $elastic_search_object;  # want to force a new connection each time
    $elastic_search_object = Search::Elasticsearch->new( nodes => ["$feeder->{esearchhost}:$feeder->{esearchport}"] );
    defined $elastic_search_object ? return 1 : return 0;
}

# ------------------------------------------------------------- 
# adds hostname key to the search hash - returns 1 on success. Only success here.
sub generate_hostname 
{
    my ($hashref_search) = @_;

    my $hostprefix     = "";                    # FIX MINOR: get this from the configuration prepend to all feeder generated hosts
    my $hostdefault    = "unknown_log_host";    # FIX MINOR: get this from the configuration
    my $allhostdefault = "all_log_hosts";       # FIX MINOR: get this from the configuration
    my $hostname       = undef;
    my $candidate      = undef;

    #see if anything from the elasticsearch search terms can be used
    if ( $hashref_search->{es_host} )         { $candidate = $hashref_search->{es_host}; }
    if ( $hashref_search->{es_ComputerName} ) { $candidate = $hashref_search->{es_ComputerName}; }

    # if the esearch term contain the _all keyword for elasticsearch then change the displayed hostname to default "all_hosts" name
    if ( defined $candidate and $candidate eq "_all" ) { $candidate = $allhostdefault; }

    # if the configuration contains an explicit hostname override then use it
    if ( $hashref_search->{hostname} ) { $candidate = $hashref_search->{hostname}; }

    if ( !$candidate ) {
        $hostname = $hostdefault;
    }
    else {
        $hostname = $candidate;
    }

    # add the prefix
    $hostname = $hostprefix . $hostname;

    # FIX MINOR: add code to validate hostname per Foundation rules
    $hashref_search->{Host} = $hostname;

    return 1;    # success
}

# ------------------------------------------------------------- 
# adds hostgroup key to the search hash - returns 1 on success. Only success here.
sub generate_hostgroup 
{
    my ($hashref_search) = @_;
    my $hostgroupdefault = "unknown_logstash";    # FIX MINOR: get this from the configuration
    my $candidate        = $hostgroupdefault;
    my ($hostgroup)      = ();
    if ( $hashref_search->{hostgroup} ) { $hostgroup = $hashref_search->{hostgroup} }

    # FIX MINOR: add code to validate hostname per Foundation rules
    if ($hostgroup) { $candidate = $hostgroup; }

    $hashref_search->{HostGroup} = { $candidate => undef } ;

    return 1; # success
}

# ------------------------------------------------------------- 
# adds servicename key to the search hash - returns 1 on success. Only success here.
sub generate_servicename 
{
    my ($hashref_search)   = @_;
    my $servicenamedefault = "unknown_logstash_service";    # FIX MINOR: get this from the configuration
    my $candidate          = $servicenamedefault;
    my ($servicename)      = ();
    if ( $hashref_search->{servicename} ) { $servicename = $hashref_search->{servicename} }

    # FIX MINOR: add code to validate servicename per Foundation rules
    if ($servicename) { $candidate = $servicename; }

    $hashref_search->{ServiceDescription} = $candidate;

    return 1; # success
}

# ------------------------------------------------------------- 
# substitutes special stuff in the search terms es_index param
sub generate_search_index {
    my ($hashref_search) = @_;
    my ($todays_date);

    # es_index
    if ( $hashref_search->{es_index} ) {

        # substitute #todaysdate# with YYYY.mm.dd
        if ( $hashref_search->{es_index} =~ /\$TODAYSDATE\$/i ) {
            $todays_date = strftime( "%Y.%m.%d", localtime );    # eg 2014.02.19
            $hashref_search->{es_index} =~ s/\$TODAYSDATE\$/$todays_date/ig;
        }

        # more es_index subs here in future
    }

    # other param subs logic here 

    return 1;
}

# ------------------------------------------------------------- #
# used by legacy/unused sub - left in for ref
sub tsdbwrite 
{
    ## FIX MINOR:
    ## Review Metric/Tag schema in accordance with http://opentsdb.net/docs/build/html/user_guide/writing.html
    ## E.G. Vema service names tags are much more regular and lend themselves to tagification beyond what can be done in nagios.
    my ( $tsdb_host, $tsdb_port, $source_app, $timestamp, $host, $service, $label, $value, $warn, $crit, $min, $max ) = @_;

    my $post_tholds = 0;
    my $post_minmax = 0;

    my %tsdb_metric = (
        'Target'    => $tsdb_host,
        'Port'      => $tsdb_port,
        'App'       => $source_app,
        'Type'      => "value",
        'Label'     => $label,
        'Value'     => $value,
        'Host'      => $host,
        'Timestamp' => $timestamp,
        'Service'   => $service
    );
    post_metric( \%tsdb_metric );

    if ( $warn && $post_tholds ) {
        my %tsdb_metric = (
            'Target'    => $tsdb_host,
            'Port'      => $tsdb_port,
            'App'       => $source_app,
            'Type'      => "thold-w",
            'Label'     => $label,
            'Value'     => $warn,
            'Host'      => $host,
            'Timestamp' => $timestamp,
            'Service'   => $service
        );
        post_metric( \%tsdb_metric );
    }

    if ( $crit && $post_tholds ) {
        my %tsdb_metric = (
            'Target'    => $tsdb_host,
            'Port'      => $tsdb_port,
            'App'       => $source_app,
            'Type'      => "thold-c",
            'Label'     => $label,
            'Value'     => $crit,
            'Host'      => $host,
            'Timestamp' => $timestamp,
            'Service'   => $service
        );
        post_metric( \%tsdb_metric );
    }

    if ( $min && $post_minmax ) {
        my %tsdb_metric = (
            'Target'    => $tsdb_host,
            'Port'      => $tsdb_port,
            'App'       => $source_app,
            'Type'      => "minimum",
            'Label'     => $label,
            'Value'     => $min,
            'Host'      => $host,
            'Timestamp' => $timestamp,
            'Service'   => $service
        );
        post_metric( \%tsdb_metric );
    }

    if ( $max && $post_minmax ) {
        my %tsdb_metric = (
            'Target'    => $tsdb_host,
            'Port'      => $tsdb_port,
            'App'       => $source_app,
            'Type'      => "maximmum",
            'Label'     => $label,
            'Value'     => $max,
            'Host'      => $host,
            'Timestamp' => $timestamp,
            'Service'   => $service
        );
        post_metric( \%tsdb_metric );
    }
}

# -------------------------------------------------------------
# left in for future ref
sub post_metric 
{
    ## FIX MINOR:
    ## Use REST http://opentsdb.net/docs/build/html/api_http/put.html
    ## do something usefull on communication failures
    my ($metric)    = @_;
    my $metric_name = $metric->{Service};
    my $source      = $metric->{App};
    my $host        = $metric->{Host};
    my $timestamp   = $metric->{Timestamp};
    my $value       = $metric->{Value};
    my $label       = $metric->{Label};
    my $type        = $metric->{Type};
    my $target      = $metric->{Target};
    my $port        = $metric->{Port};

    my $payload = "put $metric_name $timestamp $value source=$source host=$host label=$label valuetype=$type\n";
    my $message = "put $metric_name $timestamp $value source=$source host=$host label=$label valuetype=$type";

    $logger->info("posting Perfdata $message to $target:$port ");

    my $tsdb_socket = IO::Socket::INET->new( PeerAddr => $target, PeerPort => $port, Proto => 'tcp', Type => SOCK_STREAM );

    $tsdb_socket->autoflush(1);

    print $tsdb_socket $payload;

    close($tsdb_socket);
    return;
}

# ------------------------------------------------------------- 
# process type 9 queries match path phrase
sub process_esearch_9 {
    my ($hashref_search) = @_;
    my ( $es_result, $count, $took, $etime, $message ) = ();

    my $start_time    = Time::HiRes::time();
    my $es_index      = $hashref_search->{es_index};
    my $es_path       = $hashref_search->{es_path};
    my $es_message    = $hashref_search->{es_message};
    my $es_timeperiod = $hashref_search->{es_timeperiod};
    my $es_type       = $hashref_search->{es_type}; # Added v2.0.0

    eval {
        $es_result = $elastic_search_object->search(
            index       => $es_index,    # or undef, (all)
            search_type => 'count',
            body        => {
                'filter' => { 'range' => { '@timestamp' => { 'gte' => $es_timeperiod } } },
                'query'  => {
                    'bool' => {
                        'must' => [
                            #{ 'match_phrase' => { 'path' => $es_path } }, 
                            { 'match_phrase' => { 'path.raw' => $es_path } },  # Changed to .raw in V2.0 to avoid analysis and tokenization for this one - as-is
                            { 'match_phrase' => { 'message'  => $es_message } },
                            { 'match_phrase' => { 'type'     => $es_type } } # Added in v2.0
                        ]
                    }
                }
            }
        );
    };
    if ($@) {
        my $host    = $hashref_search->{hostname};
        my $service = $hashref_search->{servicename};
        report_daemon_error("!!! Could not execute search type 9 for host $host service $service: $!");
        return 0;
    }


    $count = ${$es_result}{hits}{total};
    $took  = ${$es_result}{took};

    $etime   = Time::HiRes::time() - $start_time;
    $message = "$count Messages found from with : path $es_path , phrase \"$es_message\" , type $es_type, since $es_timeperiod";

    $hashref_search->{count}   = $count;
    $hashref_search->{took}    = $took;
    $hashref_search->{etime}   = $etime;
    $hashref_search->{message} = $message;

    return 1;    # success
}

# ------------------------------------------------------------- #

# process type 10 queries match path phrase
sub process_esearch_10 {
    my ($hashref_search) = @_;
    my ( $es_result, $count, $took, $etime, $message ) = ();

    my $start_time    = Time::HiRes::time();
    my $es_index      = $hashref_search->{es_index};
    my $es_type       = $hashref_search->{es_type};
    my $es_message    = $hashref_search->{es_message};
    my $es_timeperiod = $hashref_search->{es_timeperiod};

    eval {
        $es_result = $elastic_search_object->search(
            index       => $es_index,    # or undef, (all)
            search_type => 'count',
            body        => {
                'filter' => { 'range' => { '@timestamp' => { 'gte' => $es_timeperiod } }   },
                'query'  => {
                    'bool' => {
                        'must' => [
                                    { 'match_phrase' => { 'type' => $es_type } },
                                    { 'match_phrase' => { 'message' => $es_message } }
                                  ]
                    }
                }
            }
        );
    };
    if ($@) {
        my $host    = $hashref_search->{hostname};
        my $service = $hashref_search->{servicename};
        report_daemon_error("!!! Could not execute search type 10 for host $host service $service: $!");
        return 0;
    }

    $count = ${$es_result}{hits}{total};
    $took  = ${$es_result}{took};

    ## print "Type 10 $count\n"; print Dumper($es_result);

    $etime   = Time::HiRes::time() - $start_time;
    $message = "$count Messages found of Type $es_type with phrase \"$es_message\" since $es_timeperiod";

    $hashref_search->{count}   = $count;
    $hashref_search->{took}    = $took;
    $hashref_search->{etime}   = $etime;
    $hashref_search->{message} = $message;

    return 1;    # success
}

# ----------------------------------------------------------------------------------------------------------------
sub get_foundation_host_state
{
    # takes a built event
    # if the event's host exists in foundation, then it adds a FoundationHostState property to the built event
    # otherwise it doesn't.
    # returns 1 on ok, 0 failure

    my ( $built_event ) = @_;
    my ( %outcome, %results ) ;


    $logger->debug("Getting host state for $built_event->{Host}");
    #if ( not $feeder->{rest_api}->get_hosts( \@hosts_bundle, {}, \%outcome, \%results ) ) {
    if ( not $feeder->{rest_api}->get_hosts( [ $built_event->{Host} ] , {}, \%outcome, \%results ) ) {
        # report an error but continue on rather than returning - ie try to do as much as possible
        if ( defined $outcome{response_code} and $outcome{response_code} ne '404' ) {
            $logger->error("Error getting host states : " . Dumper \%outcome, \%results); 
            return 0;
        }
    }
    if ( defined $results{ $built_event->{Host} }{monitorStatus} ) {
        $built_event->{FoundationHostState} = $results{ $built_event->{Host} }{monitorStatus} ;
    }
    # else don't add FoundationHostState

    return 1;
}

# ----------------------------------------------------------------------------------------------------------------
sub upsert_foundation_host
{
    # Takes one built event with HostState prop and upserts the host in Foundation with that state
    my ( $built_event ) = @_;
    my ( %host_options, @hosts ) ;

    %host_options = ();

    $logger->debug("Upserting hosts");

    # Build an array of options that the feeder rest api can consume
    # However, don't pass in description, properties, agentId, appType or anything else that 
    # will overwrite things should the host already exist. Instead, let feeder_upsert_hosts add those if necessary.
    push @hosts,  {
                      # This should be the smallest set of properties required for updating an existing host
                     'hostName'       => $built_event->{Host},
                     'monitorStatus'  => $built_event->{HostState},
                     'lastCheckTime'  => $built_event->{HostReportDate}, # this is needed to ensure events have correct time stamps => correct host state histograms in sv
                    # TBD do I need to detect state change here first or does foundation do this ? TBD
                     #'properties'     => {  'LastStateChange' => $built_event->{HostReportDate} },  # For the Up since ... 
    };

    # feeder_upsert_hosts does bundling 
    if ( not $feeder->feeder_upsert_hosts( \@hosts, \%host_options ) ) { 
        $logger->error("!!! Could not upsert hosts" );
        return 0; 
    }

    return 1;

}

# ----------------------------------------------------------------------------------------------------------------
sub upsert_foundation_hostgroups_with_host
{
    # takes a built event
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
        $logger->error("!!! Could not upsert hostgroups" );
        return 0;
    }

    return 1;

}

# ----------------------------------------------------------------------------------------------------------------
sub get_foundation_service_state
{
    # Takes a built event and and figures out Foundation state for it's service
    # Result is stored back into the event object 

    my ( $event ) = @_;
    my ( @hosts_bundle, %outcome, %results, %hosts_and_services );

    $logger->debug( "Getting and setting Foundation service states");

    push @hosts_bundle, $event->{Host};
    if ( not $feeder->{rest_api}->get_services( [], { hostname => \@hosts_bundle, format => 'host,service' }, \%outcome, \%results ) ) {
        if ( defined $outcome{response_code} and $outcome{response_code} ne '404' ) {
            $logger->error( "Error getting Foundation service states - no Foundation service states will be set for this bundle of hosts : @hosts_bundle" );
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
                        'properties'           => { "LastPluginOutput" => $event->{message} }, # the service status message
                        'lastCheckTime'        => $event->{ServiceReportDate}, 
                       # TBD do I need to detect state change here first or does foundation do this ? TBD
                       #'lastStateChange'      => $event->{ServiceReportDate}, # for the <state> since ... sv message # TBD
    };


    if ( not $feeder->feeder_upsert_services( \@services, \%service_options ) ) {
        $logger->error("!!! Could not upsert services in Foundation" );
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
    # - a ref to a single event (for time props etc), from which the other args were constructed
    #       (could construct them in here, but trying to use this function without changing it)
    #
    # returns 1 if ok, 0 otherwise

    my ( $ref_array_of_events , $ref_hash_of_host_states, $event_ref ) = @_;
    my ( $event, $host, @host_notifications, @service_notifications, $notificationType, $noma_status ) ;
    my ( @host_events, @service_events, $event_severity, $status, $now );

    if ( not $feeder->{post_notifications} and not $feeder->{post_events} ) {
        $logger->debug("post_notifications and post_events are both disabled - no posting of events or notifications will be done");
        return 1;
    }

    $now = strftime( '%Y-%m-%dT%H:%M:%S%z', localtime() );

    # Search for HOST state changes.
    # Construct arrays for both host notification and event objects.
    # HostState can be one of these values : UNREACHABLE, UNSCHEDULED DOWN, UP
    foreach $host ( keys %{$ref_hash_of_host_states} ) {
        # hosts not yet in foundation means FoundationHostState not set, and means don't send events or notifications
        if ( ( defined $ref_hash_of_host_states->{$host}{FoundationHostState}) and 
             ( $ref_hash_of_host_states->{$host}{HostState} ne $ref_hash_of_host_states->{$host}{FoundationHostState} )  ) {

            # For events and notifications ....
            if ( $ref_hash_of_host_states->{$host}{HostState} ne 'UP' ) { # ie UNREACHABLE, UNSCHEDULED DOWN, UP
                $notificationType = "PROBLEM";
                $event_severity = "SERIOUS";
            }
            else 
            {
                $notificationType = "RECOVERY";
                $event_severity = "OK";
            };

            # For notifications ...
            $noma_status = $ref_hash_of_host_states->{$host}{HostState};
            $noma_status =~ s/UNSCHEDULED//g; # NoMa will quietly ignore UNSCHEDULED DOWN - only accepts UP, DOWN, UNREACHABLE
            $noma_status =~ s/\s+//g; # NoMa will quietly ignore ' UP' - pretty dumb about whitespace 
            push @host_notifications, {
	                                    'hostName'            => $host,
	                                    'hostState'           => $noma_status,
	                                    'notificationType'    => $notificationType,
	                                    'hostOutput'          => "$host is $ref_hash_of_host_states->{$host}{HostState}", 
                                      };

            # For host events .... 
            push @host_events, {
                                   'host'              => $host,
                                   'device'            => $host,
                                   'monitorStatus'     => $ref_hash_of_host_states->{$host}{HostState},
                                   'appType'           => $feeder->{app_type},
                                   'severity'          => $event_severity,
                                   'textMessage'       => "$host is $ref_hash_of_host_states->{$host}{HostState}",
                                   #'reportDate'        => $event_ref->{HostReportDate},
                                   #'firstInsertDate'   => $event_ref->{HostFirstInsertDate},
                                   'reportDate'        => $now,
                                   'firstInsertDate'   => $now,
                               }
        }
    }


    # Search for SERVICE state changes and post events for them
    foreach $event ( @{$ref_array_of_events} ) { 
        # events for hosts not yet in foundation means FoundationHostState not set, and means don't send events or notifications
        if ( ( defined $event->{FoundationServiceState} ) and ( $event->{ServiceState} ne $event->{FoundationServiceState} ) ) {

            # For notifications and events ...
            if ( $event->{ServiceState} ne 'OK' ) { # ie OK, UNSCHEDULED CRITICAL (or WARNING, UNKNOWN from test tweaking)
                $notificationType = "PROBLEM";
                $event_severity = "SERIOUS";
            }
            else 
            {
                $notificationType = "RECOVERY";
                $event_severity = "OK";
            };

            # For service notifications ...
            $noma_status = $event->{ServiceState} ;
            $noma_status =~ s/UNSCHEDULED//g; # NoMa will quietly ignore UNSCHEDULED CRITICAL - only accepts OK, WARNING, CRITICAL and UNKNOWN 
            $noma_status =~ s/\s+//g; # NoMa will quietly ignore ' CRITICAL' - pretty dumb about whitespace 
            push @service_notifications,  {
                                            'hostName'            => $event->{Host},
                                            'serviceDescription'  => $event->{ServiceDescription},
                                            'serviceState'        => $noma_status,
                                            'notificationType'    => $notificationType,
                                            'serviceOutput'       => $event->{LastPluginOutput},
                                          };

            # For service events ...
            push @service_events, {
                                   'host'              => $event->{Host},
                                   'device'            => $event->{Host},
                                   'service'           => $event->{ServiceDescription},
                                   'monitorStatus'     => $event->{ServiceState},
                                   'appType'           => $feeder->{app_type},
                                   'severity'          => $event_severity,
                                   #'textMessage'       => $event->{LastPluginOutput},
                                   'textMessage'       => $event->{message},
                                   #'reportDate'        => $event_ref->{ServiceReportDate},
                                   #'firstInsertDate'   => $event_ref->{ServiceFirstInsertDate},
                                   'reportDate'        => $now,
                                   'firstInsertDate'   => $now,
                               };


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
sub validate_feeder_specific_options
{
    # Logic for validation of feeder-specific options
    # TBD
    return 1;

}

# ----------------------------------------------------------------------------------------------------------------
sub update_feeder_stats
{
    # Logs feeder stats and updates services with metrics too
    # Metrics 
    # Service name                  Message
    # cycle_elapsed_time            Cycle # total elapsed processing time : # seconds
    # esearches_run                 # / # successfully/unsuccessfully run elastic searches peformed
    # results_processed_into_gw     # / # successfully/unsuccessfully run elastic search results processed into GroundWork
    # esearches_run_took            Time taken to run esearches (reported by esearch) : #
    # esearches_run_elapsed         Time taken to run esearches (measured by us) : # 
    
    my ( $cycle_number,
         $finished_time, 
         $total_cycle_time_taken,
         $successful_search_count,           # count of all enabled and successfully run esearches
         $unsuccessful_search_count,         # count of all enabled but unsuccessfully esearches
         $successfully_processed_into_gw,    # count of searches that were successfully processed into GW
         $unsuccessfully_processed_into_gw,  # count of searches that were unsuccessfully processed into GW
         $total_took,                        # total esearch time across all processed searches
         $total_etime,                       # our own measurement of es searching across all processed searches
      ) = @_;

    my ( $cycle_elapsed_time_msg, $esearches_run_msg, $results_processed_into_gw_msg, $esearches_run_took_msg, $esearches_run_elapsed_msg) ;
    my ( $cycle_elapsed_time_stat, $esearches_run_stat, $results_processed_into_gw_stat, $esearches_run_took_stat, $esearches_run_elapsed_stat) ;

    $total_cycle_time_taken = '?' if not defined $total_cycle_time_taken;
    $successful_search_count = '?' if not defined $successful_search_count;
    $unsuccessful_search_count = '?' if not defined $unsuccessful_search_count;
    $successfully_processed_into_gw = '?' if not defined $successfully_processed_into_gw;
    $unsuccessfully_processed_into_gw = '?' if not defined $unsuccessfully_processed_into_gw;

    # Assume all ok - then disprove
    $cycle_elapsed_time_stat = $esearches_run_stat = $results_processed_into_gw_stat = $esearches_run_took_stat = $esearches_run_elapsed_stat = "OK";

    # Metric service : cycle_elapsed_time
    $cycle_elapsed_time_stat = "UNSCHEDULED CRITICAL" if ( $total_cycle_time_taken eq '?' );
    $cycle_elapsed_time_msg = "$cycle_elapsed_time_stat - Cycle $cycle_number finished at $finished_time, total elapsed processing time : $total_cycle_time_taken seconds";

    # Metric service : esearches_run
    $esearches_run_stat = "WARNING" if ( $unsuccessful_search_count ne '?' and $unsuccessful_search_count > 0 ) ;
    $esearches_run_stat = "UNSCHEDULED CRITICAL" if ( $successful_search_count eq '?' or $unsuccessful_search_count eq '?' );
    $esearches_run_msg = "$esearches_run_stat - $successful_search_count / $unsuccessful_search_count successfully/unsuccessfully run elastic searches peformed";

    # Metric service : results_processed_into_gw 
    $results_processed_into_gw_stat = "WARNING" if ( $unsuccessfully_processed_into_gw ne '?' and $unsuccessfully_processed_into_gw > 0 );
    $results_processed_into_gw_stat = "UNSCHEDULED CRITICAL" if ( $successfully_processed_into_gw eq '?' or $unsuccessfully_processed_into_gw eq '?' );
    $results_processed_into_gw_msg = "$results_processed_into_gw_stat - $successfully_processed_into_gw / $unsuccessfully_processed_into_gw successfully/unsuccessfully run elastic search results processed into GroundWork";

    # Metric service : esearches_run_took
    # TBD add some configurable thresholding at some point in the future
    $esearches_run_took_stat = "UNSCHEDULED CRITICAL" if ( $total_took eq '?' );
    $esearches_run_took_msg = "$esearches_run_took_stat - Time taken for elasticsearch to execute all searches run : $total_took ms";

    # Metric service : esearches_run_elapsed
    # TBD add some configurable thresholding at some point in the future
    $esearches_run_elapsed_stat = "UNSCHEDULED CRITICAL" if ( $total_etime eq '?' );
    $esearches_run_elapsed_msg = "$esearches_run_elapsed_stat - Time taken to run elastic searches included network etc : $total_etime";

    $logger->debug("Updating feeder statistics");

    # Log metrics
    $logger->info( "$cycle_elapsed_time_msg") if defined $feeder->{cycle_timings};
    $logger->info( "$esearches_run_msg");
    $logger->info( "$results_processed_into_gw_msg");
    $logger->info( "$esearches_run_took_msg");
    $logger->info( "$esearches_run_elapsed_msg");

    #my $now = strftime( '%Y-%m-%dT%H:%M:%S', localtime ); my $tz  = strftime("%z", localtime );

    # Update services with metrics
    if ( not $feeder->feeder_upsert_services(    [ 
                                                    {
                                                        'description'          => 'cycle_elapsed_time',
                                                        'hostName'             => $feeder->{properties}->{health_hostname}, 
                                                        'monitorStatus'        => $cycle_elapsed_time_stat,
                                                        'properties'           => { "LastPluginOutput" => $cycle_elapsed_time_msg },
                                                        #'lastCheckTime'        => "$now$tz", 
                                                    },
                                                    {
                                                        'description'          => 'esearches_run',
                                                        'hostName'             => $feeder->{properties}->{health_hostname}, 
                                                        'monitorStatus'        => $esearches_run_stat,
                                                        'properties'           => { "LastPluginOutput" => $esearches_run_msg },
                                                        #'lastCheckTime'        => "$now$tz", 
                                                    },
                                                    {
                                                        'description'          => 'results_processed_into_gw',
                                                        'hostName'             => $feeder->{properties}->{health_hostname}, 
                                                        'monitorStatus'        => $results_processed_into_gw_stat,
                                                        'properties'           => { "LastPluginOutput" => $results_processed_into_gw_msg },
                                                        #'lastCheckTime'        => "$now$tz",
                                                    },
                                                    {
                                                        'description'          => 'esearches_run_took',
                                                        'hostName'             => $feeder->{properties}->{health_hostname}, 
                                                        'monitorStatus'        => $esearches_run_took_stat,
                                                        'properties'           => { "LastPluginOutput" => $esearches_run_took_msg },
                                                        #'lastCheckTime'        => "$now$tz",
                                                    },
                                                    {
                                                        'description'          => 'esearches_run_elapsed',
                                                        'hostName'             => $feeder->{properties}->{health_hostname}, 
                                                        'monitorStatus'        => $esearches_run_elapsed_stat,
                                                        'properties'           => { "LastPluginOutput" => $esearches_run_elapsed_msg },
                                                        #'lastCheckTime'        => "$now$tz",
                                                    },
                                                ], {}  )  ) { 
        $logger->error("Error updating feeder statistical services");
    }

}
__END__



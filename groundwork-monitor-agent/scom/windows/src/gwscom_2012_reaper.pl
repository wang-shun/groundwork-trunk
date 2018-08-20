#!perl 
#
# This program reads the output of the SCOM 2012 universal runbook and transmits results to the groundwork spooler table
#  ie the scom_events table in the scom database
#
# Copyright 2012-13 GroundWork OpenSource
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
# Dominic Nicholas March 2013 - converted Kevin's original code into this PerlApp
# Dominic Nicholas Apr 2013 - mods to cleanup and the write_event_to_db sub
# Dominic Nicholas May 2013 
#      - cleaned up incorrect error messages in write_event_to_db()
#      - don't mark as processed or remove the event xml if it wasn't written to the db successfully
#      - If failure on writing event to db (prepare or execute), for good measure, make a new db connection since that might have failed
# 1.0.3 DN Sep 2013 - updated routine that parses xml into sql - now far more maintainable, 
#                     automatic and will flag error in event of xml tag not present in scom_events table, 
#                     which is very useful to ensure we are handling all MS products' xml structures
# 2.0.0 DN 6/22/15 - Noticed missing column TimeResolved from scom_events and scom_save tables - added
#		           - corrected minor column name existence test and logic in write_event_to_db() - now will handle unrecognized and size-unlimited columns better
#		           - process_scom_events() rewritten for resilience
#		           - reading of xml into internal datastructure rewritten for resilience 
#		           - debug level 5 added for logging skipped events etc
#		           - write_event_to_db resiliency improved and it also checks validity of fields in event 
#		           - main loop logic around db connection and getting db col info made more resilient, including checking if db is reachable
#		           - general code review
#		           - reviewed signal handling - according to GDMA notes, nothing to be done here
# 2.0.1 DN 6/24/15 - Updated process_scom_events to check for db connection and break the processing loop if not up; added -i option
# 2.0.2 DN 6/24/15 - cycle time is just left as-is and not auto adjusted in some wierd sadistic fashion
#
# TODO:
#   - Have errors sent directly to a gw service , like we do with gdma
#   - more config file settings validation
#   - any other TODO's or TBD's

package PerlApp;

use strict;
use DBI;
use Getopt::Std;
use TypedConfig;
use Time::Local;
use Time::HiRes;
use XML::Simple;
use Data::Dumper;
use File::Basename;

# expect to see props file in same dir as the reaper exe... keeps it simple for now
my $VERSION = "2.0.2 " . get_version(1);
my $props = "gwscom.properties"; # simple name of props file
my $exe = PerlApp::exe();  # fully qualified exe name
my $config_file = dirname($exe) . "\\$props"; # fully qualified props file
(my $progname = $0) =~ s/.*?([^\\]+?)(\.\w+)$/$1/; # simple name of exe, eg gwscom_2012_reaper
my $runonce = 0; # assume deamonized by service exe
my $debug_config  = 0;  # if set, spill out certain data about config-file processing to STDOUT
my %opt;
my $logging=1; # leave this set to 1 for now until make logging a cli opt
my $terminate =0;
my $cgrow=10; # grow cycle time by this much if we are too slow.
my ( $event_directory, $scomeventtable, $etime, $sleeptime, $cycle_time, $unixtime, $debug_level, $debug_log, 
     $scomdbname, $dsn_scom, $scomdbpass, $scomdbuser, $scomdbport, $scomdbhost, $dbtype, $dbh_scom, 
     $remove_processed_events, $db_connection_retries, %scom_events_schema_info, $interactive ) ;

# TBD this won't work on mswin32 - needs cleaning up
#$SIG{TERM} = \&record_terminate_signal;

main();

# ------------------------------------------------------------------------------------------
END {
    # not sure how useful this is yet
    #log_message("Shutting down at " . localtime );
}

# ------------------------------------------------------------------------------------------
sub main 
{
    my ( $etime, $file_count, $events_written_to_db_count, $cycle_count ) ;

    if ( not get_options() ) { 
        print("An error occurred with command line options - the app will now exit\n"); # print cos no logging defined yet
        exit;
    }

    if ( not read_config_file($config_file) ) {  # On failure this will cause this app to fail. It also is only read once at startup.
        # it's worth trying to log_message at this point
        log_message("An error occurred reading the config file - the app will now exit");
        exit;
    }

	log_message ("Application $exe version $VERSION started at " . localtime ) ;

    $cycle_count = 0;

    CYCLE: while (1) {

        $cycle_count++;

	    log_message ("Cycle $cycle_count started at " . localtime ) ;

        # if its the first cycle or the db handle is not defined or the db failed to ping ( this tested ok with postgres ), then try to create db connection
        if (  ( $cycle_count == 1 )  or  ( not defined $dbh_scom )  or  ( not db_alive() )  ) { 
            if ( not create_db_connection() ) { 
                log_message("ERROR creating database connection - sleeping for 30 seconds before trying on next cycle");
                sleep 30;
                next CYCLE;
            }
            if ( not get_column_info() ) {
                log_message("ERROR getting  database column information - sleeping for 30 seconds before trying on next cycle");
                sleep 30;
                next CYCLE;
            }
        }
    
        # check that the events dir exists and only continue if it does
        if ( not check_event_directory() ) { 
            log_message( "An error occurred with the event directory - no events will be processed this cycle - sleeping for 30 seconds before trying again on next cycle");
            sleep 30;
            next CYCLE;
        }
 
        # Look for and process scom event xml files put in the events dir by the SCOM GW runbook
	    if ( not process_scom_events( \$etime, \$file_count, \$events_written_to_db_count ) ) {
		    log_message("ERROR was detected during processing of events");
        }

        # See if need to increment the cycle time
	    $sleeptime = $cycle_time ; # 2.0.2 - don't slow things down later because of a large batch now
	   #$sleeptime = $cycle_time - $etime;
	   #if ($sleeptime <=0) 
	   #{
		   #log_message("Warning Elapsed time $etime greater than cycle time $cycle_time - Incrementing Cycle Time $cgrow seconds"); 
		   #$cycle_time += $cgrow;
		   #$sleeptime = $cgrow;
	   #}
    
        # Metrics update - eventually have this update a service on the GW side via nsca etc like gdma
        log_message("$events_written_to_db_count event files were processed and written to the database in $etime seconds");
    
        if ( $runonce ) { 
		    log_message("Run-once option supplied - quitting now");
            exit;
        }
         
        # TBD make this more informative! Do some real signal handling if poss
	    if ( $terminate ) {
		    log_message("Termination event detected - terminating");
 	        exit;
        }
    
	    log_message("Sleeping for $sleeptime");
	    sleep $sleeptime;
    
   }
   exit;

}


# ------------------------------------------------------------------------------------------
sub create_db_connection
{
    # instantiates a global db handle to the scom db, $dbh_scom
    # args : none
    # returns 1 on success, 0 on encountering a problem

    my ( $tries, $wait_duration, $wait, $rc ) ;

    $tries = 1; # will count up to config->db_connection_retries 
    $wait_duration = 5; # seconds - will use a simple arithmetic progression on each try - ie add 5 each time
    $wait = $wait_duration ;

    # if the db handle just disconnect and undef it 
    if ( $dbh_scom ) {
        log_message("Database handle is set - disconnect from it") if $debug_level >= 1;
        # at least log errors if they occur
        # 1.0.4 - AutoCommit => 1 so no need to do this - autocommits are after all successful queries
        # if ( ! $dbh_scom->commit ) { log_message("ERROR during commit : $DBI::errstr"); } 
        if ( not $dbh_scom->disconnect ) { log_message("ERROR disconnecting : $DBI::errstr"); }
        undef $dbh_scom;
    }

    if ( defined ($dbtype) && $dbtype eq 'postgresql' ) {
        $dsn_scom = "DBI:Pg:dbname=$scomdbname;host=$scomdbhost;port=$scomdbport";
    }
    else {
        $dsn_scom = "DBI:mysql:database=$scomdbname;host=$scomdbhost;port=$scomdbport";
    }
    
    log_message( "Creating database connection : dsn = $dsn_scom" ) if $debug_level >= 1;
    log_message( "Attempting to connect to database $scomdbname") if $debug_level >= 1;
    eval {
        $dbh_scom = DBI->connect( $dsn_scom, $scomdbuser, $scomdbpass, { 'AutoCommit' => 1 } );
    };
    if ( $@ ) { 
        chomp $@;
        log_message( "An error occurred connecting to the database : $@");
    }

    
    while ( not $dbh_scom and $tries < $db_connection_retries ) {
        log_message ("ERROR Cannot connect to database $scomdbname. Error: $DBI::errstr - waiting $wait seconds to retry");
        sleep $wait;
        log_message( "INFO: trying again to create database connection : dsn = $dsn_scom" );
        eval { 
            $dbh_scom = DBI->connect( $dsn_scom, $scomdbuser, $scomdbpass, { 'AutoCommit' => 1 } ); 
        };
        if ( $@ ) { 
            chomp $@;
            log_message( "An error occurred connecting to the database : $@");
        }

        $wait += $wait_duration ;
        $tries++;
    }

    # if still can't connect, then bail, rather than trying forever processing nothing
    if ( not $dbh_scom) { 
        log_message ("ERROR Giving up trying to connect to database $scomdbname. Check the configuration and connectivity."); 
        return 0;
    }

    log_message("Connection established to database $scomdbname") if $debug_level >= 1;
    return 1;

}

# ------------------------------------------------------------------------------------------
sub process_scom_events
{
    # 1.0.4 various changes to this routine including preventing it from
    # entering into a crazy infinite loop if it cannot process an xml file!
    # 
    # Tries to process all xml files in a directory
    # Those it can process, it either removes or moves to a .processed version.
    # Those it cannot process, it leaves alone.
    # Args
    #   none - see returns
    # Returns
    #   1 on successfully processing all events
    #   0 on failing to processing all events, or cannot open the events directory
    #   by ref : 
    #       elapsed time to process all event files
    #       count of # of files looked at in the event dir
    #       count of # of actual event files that were processed and written to the scom db

    my ( $etime_ref, $file_count_ref, $events_written_to_db_count_ref ) = @_;
    my ( $processed_name, $start_time, $all_xml_files_processed_ok, $event_file, $event_xml ) ;

    $start_time = Time::HiRes::time(); # start a timer
    ${$file_count_ref} = 0; # count how many files were looked at, not necessary processed
    ${$events_written_to_db_count_ref} = 0; # count of successfully processed to db event files

    # This will get set to 0 if an event fails to process.  Assumes all will be processed ok and disprove.
    $all_xml_files_processed_ok = 1; 

    # Open the events directory - it has already been tested for existence earlier, but check read works
    if ( not opendir (DIR, $event_directory) ) { 
        log_message("Could not open the event directory $event_directory : $!");
	    return 0;   
    } 

    # get a list of event files that need processing
    GETEVENT: while ( $event_file = readdir(DIR) ) {

        # Quietly skip . and ..
        next GETEVENT if ( $event_file =~ /^(\.|\.\.)$/ ) ; 

        $event_file = "$event_directory/$event_file"; # Fq the filename
        ${$file_count_ref} ++; # inc file counter for metrics later

        # skip files that have been processed already
        if ( $event_file =~ /^.*\.xml\.processed\.\d+$/ ) { 
            log_message( "Skipping already processed event file $event_file") if ( $debug_level >= 5 );
            next GETEVENT;
        }

        # skip files that don't end exactly in '.xml'.
        if ( $event_file !~ /^.*\.xml$/ ) { 
            log_message( "Skipping file $event_file because it doesn't have a '.xml' suffix") if ( $debug_level >= 5 );
            next GETEVENT;
        }

        # Create a datastructure from the xml. The $event_xml will contain a ref to a hash from XMLin()
        log_message("Processing event file $event_file") if $debug_level >= 1;
        if ( not file_to_xml( $event_file, \$event_xml ) ) { 
            log_message( "Skipping further processing of $event_file because it failed to be converted into an XML object" );
            $all_xml_files_processed_ok = 0; # Make a note that not all xml files were processed properly
            next GETEVENT;
        }


        # Now have an XML event object based on the content of the XML file, so send it over to the GWME scom database
    	if ( write_event_to_db( $event_xml ) != -2 ) {  # -2 => error occured during prepare or execute of query or an unrecognized property was found

            ${$events_written_to_db_count_ref} ++; # inc counter of how many event files were written to the database

            # delete or rename the processed event xml file
            if ( $remove_processed_events == 1 )  {  # delete
                unlink ($event_file); 
                if ( -e $event_file ) { 
                    log_message("ERROR Could not remove $event_file"); 
                }
            }
            else { # rename 
	            $processed_name = "$event_file.processed." . time();
	            if ( not rename($event_file, $processed_name) ) { 
                    log_message("Could not rename $event_file to $processed_name $!"); 
                    $all_xml_files_processed_ok = 0; # Make a note that not all xml files were processed properly
                }
            }
        }
        else {
            # Leave the event xml there for future processing.
            $all_xml_files_processed_ok = 0; # Make a note that not all xml files were processed properly
            
            # The db connection might have broken during the write to it. In this case
            # stop processing xml event files until it's back up.
            if ( not db_alive() ) {
                log_message("ERROR Database connection lost? Ending further processing of events for now."); 
                last GETEVENT;
            }
        }
    }

    ${$etime_ref} = Time::HiRes::time() - $start_time;
    return $all_xml_files_processed_ok;
   
}


# ------------------------------------------------------------------------------------------
sub read_config_file 
{
    # takes a config file to read, and tries to read it.
    # returns 1 if ok, plus a populated bunch of global vars - yuk but heh; zero otherwise
    my ( $scom_proc_config_file ) = @_;

    # test config file exists and is readable - print error to stdout if it doesn't
    if ( ( ! -e $scom_proc_config_file ) or ( ! -r $scom_proc_config_file ) ) { 
	    print "Configuration file '$scom_proc_config_file' doesnt exist or isnt readable - quitting\n";
	    exit;
    };
    
    eval {
        my $config = TypedConfig->new ($scom_proc_config_file);
       	$debug_level		= $config->get_number ('debug_level');
       	$debug_log		    = $config->get_scalar ('debug_log');
       	$event_directory	= $config->get_scalar ('event_directory');
	    $dbtype		        = $config->get_scalar ('dbtype');
	    $scomdbname	        = $config->get_scalar ('scomdbname');
	    $scomdbuser	        = $config->get_scalar ('scomdbuser');
	    $scomdbpass	        = $config->get_scalar ('scomdbpass');
	    $scomdbport	        = $config->get_scalar ('scomdbport');
	    $scomdbhost	        = $config->get_scalar ('scomdbhost');
	    $scomeventtable	    = $config->get_scalar ('scomeventtable');
	    $cycle_time         = $config->get_number ('cycle_time');
        $remove_processed_events = $config->get_number ('remove_processed_events');
        $db_connection_retries = $config->get_number ('db_connection_retries');
    };
    if ($@) {
        chomp $@;
        $@ =~ s/^ERROR:\s+//i;
        log_message("ERROR reading config file $scom_proc_config_file ($@)"); 
	    return 0;
    }
    
    # TBD more validation
    # eg check that dbtype = postgres or mysql 



    return 1;

}

# ------------------------------------------------------------------------------------------
sub check_event_directory
{
    # checks that the events directory exists etc
    # returns 1 if ok, 0 otherwise
    if ( ( ! -e $event_directory ) or ( ! -d $event_directory ) or ( ! -r $event_directory ) ) {
	    log_message("ERROR Event directory $event_directory doesn't exist or is not a directory or is not readable");
	    return 0;
    }
    return 1;
}


# ------------------------------------------------------------------------------------------
sub get_options 
{

   my $helpstring = "
Groundwork SCOM 2012 Event Reaper [Version $VERSION]
Options:

        -h print this message
        -i Send messaging to terminal/stdout as well as log file
        -v print version
        -x run once and exit

All other behavior is controlled by the configuration file $config_file.

Copyright (C)2013-16 GroundWork Opensource.
This program comes with absolutely NO WARRANTY either implied or explicit.
";

    getopts("xvhi",\%opt);
    if ($opt{h}) { print $helpstring; exit; }
    if ($opt{i}) { $interactive = 1 ; }
    if ($opt{x}) { $runonce = 1 ; }
    if ($opt{v}) { print "$progname version $VERSION\n"; exit; }

    # validation - nothing to do cos the args don't have params, but are just flags
    return 1;

}

# ------------------------------------------------------------------------------------------
# The normal signal handler for process termination.  We just record the fact that
# the signal came in, and process it at the next safe and convenient opportunity.
# This won't work on Windows 
sub record_terminate_signal 
{
    log_message("Termination signal intercepted");
    $terminate = 1;
}


# ------------------------------------------------------------------------------------------
sub write_event_to_db
{
    # takes a ref to an xml data structure for the scom event,
    # checks it for validity, sanitizes it, and writes it over in the GWME scom db
    # args :
    #   event ref
    # returns
    #   -2 : - invalid xml property found in event ie the property when lc'd doesn't exist
    #          as a column in the scom events database; 
    #        - the query could not be prepared 
    #        - the query could not be executed 
    #   The dbi execute return stat otherwise
    #   

    my ( $event ) = @_;
    my ( $xml_property, $sql, @sqlcols, @sqlvals, $rv, $sqlQuery );

    # For SCOM 2007, the ConnectorVersion field presumably can be missing so add it here
    if ( not defined $event->{ConnectorVersion} ) { $event->{ConnectorVersion} = 'SCOM_2007'; }
  
    # Build a query is dynamically based on the incoming event's properties.
    # Each lc version of the property must be available as a column in the gw scom database else the query->prep will fail.
    foreach my $xml_property ( keys %{$event} ) {

        # sanitize the incoming data so it doesn't blow up foundation later TBD this could use a review
        $event->{$xml_property} = cleanup( $event->{$xml_property}, "$xml_property" ) ;

        # If there's a corresponding scom_events table column for the xml key, use it

        #if ( defined lc($scom_events_schema_info{$xml_property}) )  # this is insufficient with new schema where the column char lengths are undefined,
        if ( exists $scom_events_schema_info{ lc $xml_property} ) { # 1.0.4
            push( @sqlcols, $xml_property );
            push( @sqlvals, "'$event->{$xml_property}'" );
        }
        else  { # flag error - need to dev code some more to handle this xml structure
	        # If an xml property is not recognized, running the query will fail so trap it here
            log_message("ERROR: xml property $xml_property is not a column in scom_events table so this event cannot be processed");
	        return -2;
        }
    }

    # put together the query string
    $sql = "INSERT INTO $scomeventtable (" . join(",", @sqlcols) . ") VALUES (" . join(",", @sqlvals) . ")" ;
    log_message("SQL = $sql") if $debug_level >= 4 ;

    # return values : see http://search.cpan.org/dist/DBI/DBI.pm#execute for descriptions of what return values can be from these DBI ops.
    # -2 is returned on error , which should not conflict with real and valid return values from prepare or execute

    if ( not $sqlQuery  = $dbh_scom->prepare($sql) ) { 
       #log_message("Can't prepare $sql: $dbh_scom->errstr - Quitting"); 
       log_message("Can't prepare $sql: $DBI::errstr"); 
       return -2;
    }
    if ( not $rv = $sqlQuery->execute )  {
       log_message( "ERROR executing query '$sql' .\nError was : '$DBI::errstr'"); 
       return -2;
    }

    # $sqlQuery->finish(); # see DBI doc - usually not necessary

    return $rv; 

}

# ------------------------------------------------------------------------------------------
sub get_column_info
{
    # Builds a hash containing { column name => character max length , ... } info from the scom event table.
    # This info is used for sanitizing data, and for validating xml properties when writing to the db.
    # Args : none
    # Returns : 1 ok, 0 otherwise

    my $sql ="select column_name,character_maximum_length from INFORMATION_SCHEMA.COLUMNS where table_name = '$scomeventtable';";
    my ( $sqlQuery, $rv );

    log_message("get_column_info() : SQL = $sql") if $debug_level >= 4;

    if ( not $sqlQuery  = $dbh_scom->prepare($sql) ) { 
        log_message("Can't prepare $sql: $DBI::errstr"); 
        return 0;
    }

    if ( not $rv = $sqlQuery->execute ) {
        log_message( "ERROR executing query '$sql' .\nError was : '$DBI::errstr'"); 
        return 0;
    } 

    # build the hash
    while ( my $row = $sqlQuery->fetchrow_hashref() ) {
        $scom_events_schema_info{ lc( $$row{column_name}) } = $$row{character_maximum_length} ;
    }

    # $sqlQuery->finish(); # see DBI doc - usually not necessary
    return 1;

}

# ------------------------------------------------------------------------------------------
sub cleanup
{
    # replaces cleanup_old() to truncate column values to avoid database insertion errors
    # column names and max sizes are pulled during initialization

    my ($value, $column) = @_;

    if ( ! $value ) { $value=0; }
    if ( ! $column ) { $column = ""; } # this might need rethinking - its really an internal error since it should always get a field passed to it
    $column = lc($column); # schema query returns lc, make sure we match

    my $truncation_length;
    my $truncation_suffix = "****** Truncated"; # string that will overlay the end a truncated column value

    # if a max length was found for the column then use it in truncation testing/action
    if ( $scom_events_schema_info{$column} ) {

       # if the length of the column value exceeds what the database is allowed to take, then truncate it down
       if ( length($value) > $scom_events_schema_info{$column} ) {

           $truncation_length = $scom_events_schema_info{$column} - length($truncation_suffix) ;

           # a non negative truncation lenght means we have space in the column to put some of the value AND the truncation message
           if ( $truncation_length >= 0 ) {
              # truncate the value and overlay the truncation message on the end
              $value = substr($value, 0, $truncation_length) . $truncation_suffix;
           }

           # otherise this is the wierd case of the column width not being wide enough to fit the value and the truncation message,
           # so will just have to truncate the truncation message itself! its an oddball case that prob'y won't happen
           else {
              $value = substr($truncation_suffix, 0 , $truncation_length );
           }

        } # end test on length of value being > than schema limit

    }
    # otherwise, no max length was found so don't truncate ie no else

    # do some actual value cleanup/translations for db safety
    $value =~ tr/\cM//d;
    $value =~ tr/'/"/;
    $value =~ tr/<//d;
    $value =~ tr/>//d;
    $value =~ tr/&//d;
    $value =~ tr/\n//d;
    $value =~ tr/\r//d;

    return $value;
}

# ------------------------------------------------------------------------------------------
sub file_to_xml 
{
    # Converts an xml file content into a hash representing the XML
    # Args
    #   Filename of the event xml
    # Returns
    #   1 - success
    #   0 - failure
    #   ref to an event xml datastructure created by this routine
   
    my ( $event_filename, $event_xml_ref ) = @_;
    my ( $data, $xml_object ) = undef;

    $xml_object = new XML::Simple(suppressempty => 1); # TBD check what suppressempty =1 actually does

    # Need to eval this and catch. For example, if XMLin fails, it will quit the entire app
    eval {
        ${$event_xml_ref} = $xml_object->XMLin( $event_filename ) ;
    };
    if ( $@ ) {
        chomp $@;
        $@ =~ s/\n//g; 
        log_message("ERROR reading XML from $event_filename : Error: '$@'");
        return 0;
    }

    return 1;
}

# ------------------------------------------------------------------------------------------el
sub get_version 
{
    my $full    = shift;
    my $version = $VERSION;
    if ($full) {
        last unless defined $PerlApp::VERSION;
	    my $compile_time = PerlApp::get_bound_file("compile_time") or last;
	    $version .= " ($compile_time)";
    }
    return $version;
}

# ------------------------------------------------------------------------------------------
sub log_message 
{
    my ( $msg  ) = @_;

    print "$msg\n" if $interactive;

    if (! $logging) { return 0; }

    # just in case trying to call log_message before read_config_file ...
    if ( not defined $debug_log ) { 
         print "Debug log not yet defined - cannot print to it yet! Msg sent in was '$msg' - quitting\n"; 
         exit;
    }

    # problem is what happens if fail to write to log file ?
    open( my $logfh, ">>$debug_log") or die "Could not open log file '$debug_log' for appending : $!";
    print $logfh localtime() . " $msg\n";
    print localtime() . " $msg\n" if $runonce;
    close $logfh;
}

# ------------------------------------------------------------------------------------------
sub db_alive 
{
    # tries to determine if the scom db is up/reachable/alive
    # args:
    #   the global db handle
    # returns 1 if db ok, 0 otherwise

    # if the db handle isn't defined or set, then no connection
    return 0 if ( not $dbh_scom or not defined $dbh_scom ) ;

    # on Postgres , the ping method appears to work ok
    if ( not $dbh_scom->ping() ) { 
        return 0;
    }

    # all ok if reached this far
    return 1;

}



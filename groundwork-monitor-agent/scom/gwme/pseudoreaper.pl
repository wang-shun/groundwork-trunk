#!/usr/local/groundwork/perl/bin/perl

# dev helper script to emulate the windows reaper piece from unix
# the windows reaper watches the events xml directory which is populated
# by the SCORCH runbook, and then imports those xml events into he scom events table of the scom db
# for dev purposes, this script emulates this windows functionality based on the windows\src\gwscom_2012_reaper.pl perlapp code
# It's the .../gwscom_1.0.3/windows/src/gwscom_2012_reaper.pl with some mods 

# 0.0.2 - -use_save_table opt added 
# 0.0.3 - updated write_event_to_db to be same as latest reaper service code

# NOTE Jun 25 2015 DN : this hasn't been kept up to date compared with gwscom_2012_reaper.pl which has more resiliency. However it does the job.

use strict;
use DBI;
use Getopt::Std;
use TypedConfig;
use Time::Local;
use Time::HiRes;
use XML::Simple;
use Data::Dumper;
use File::Basename;

# history
# 0.0.4 - added -c (constant feeder) option

# expect to see props file in same dir as the reaper exe... keeps it simple for now
my $config_file = "/usr/local/groundwork/config/scom_feeder.conf";
my $VERSION = "0.0.4";
my %opt;
my $terminate =0;
my ($event_directory,$scomeventtable,$etime,$sleeptime,$unixtime,$debug_level,$scomdbname,$dsn_scom,$scomdbpass,$scomdbuser,$scomdbport,$scomdbhost,$dbtype,$dbh_scom, $remove_processed_events)=();
my %scom_events_schema_info; 
my ( $feed_interval, $import, $purge , $use_save_table ) = undef;

$scomeventtable	='scom_events';
#scomsavetable = scom_save
$debug_level = 4;
$remove_processed_events = 0 ;
# remove xml event files after processing. Setting to 1 removes them completely.  # Setting to 0 renames them with a .processed_<epochtime> suffix

main();

# ------------------------------------------------------------------------------------------
sub main 
{
    get_options();
    read_config_file ($config_file);
    create_db_connection();
    get_column_info();
    
    clear_db() if defined $purge;

    if ( defined $import ) {
	if ( defined ( $feed_interval ) ) { 
	    log_message ("Constant feed mode - events will be imported every $feed_interval seconds constantly until interrupted");
	    while ( 1 ) {
    		process_scom_events() ;
		sleep $feed_interval;
   	    }
	}
	else { # just do it once
    		process_scom_events() ;
	}
		
    }

}


# ------------------------------------------------------------------------------------------
sub create_db_connection
{
   $dsn_scom = "DBI:Pg:dbname=$scomdbname;host=$scomdbhost;port=$scomdbport";
   #log_message( "Creating database connection : dsn = $dsn_scom" );
   $dbh_scom = DBI->connect( $dsn_scom, $scomdbuser, $scomdbpass, { 'AutoCommit' => 1 } );

   if (!$dbh_scom) {
        log_message ("ERROR:  Cannot connect to database $scomdbname. Error: $DBI::errstr");
	    exit 2;
   }
}

	
# ------------------------------------------------------------------------------------------
sub process_scom_events
{
   my $start_time = Time::HiRes::time();
   my $count=0;
   my $processed_name;

   print "Importing xml events from directory $event_directory into SCOM event table $scomeventtable\n";
   #while ( geteventfile( $event_directory ) ) {
   if ( not opendir (DIR, $event_directory) ) {
        log_message("Could not open event directory $event_directory : $! - quitting");
        exit 2;
    }
    while (my $file = readdir(DIR))
    {
      next if ( $file !~ /\.xml($|.process.*$)/ ) ;
    
      $count++;
      print "$count events read\n" if not $count % 500;
      #my $eventfile = geteventfile($event_directory);
      #log_message("EventFile = $eventfile\n") if $debug_level >= 1;

      my $event = readevent("$event_directory/$file");

      if ( $event ) { 
	     write_event_to_db($event);
      }

      # delete or rename the processed event xml file
  #   if ( $remove_processed_events == 1 ) {
  #      unlink ($eventfile); 
  #   }
  #   else {
#        $processed_name = "$eventfile.processed." . time();
#        if ( not rename($eventfile, $processed_name) ) {
#              log_message("Could not rename $eventfile to $processed_name $!"); 
#           }
#     }
   }

   my $etime = Time::HiRes::time()-$start_time;

   log_message("$count Scom Events inserted into database in $etime");

   #if  ( $runonce == 1 ) { $terminate = 1 ; } 
   return ($etime);
}

# ------------------------------------------------------------------------------------------
sub geteventfile
{
    my ( $directory ) = @_;

    if ( not opendir (DIR, $directory) ) { 
	    log_message("Could not open event directory $directory : $! - quitting"); 
	    exit 2; 
    } 

    while (my $file = readdir(DIR))
    {
        if ($file =~ /\.xml($|.process.*$)/)
        {
	        $file="$directory/$file";
	        return $file;
        }
        next;
    }
    return;
}


# ------------------------------------------------------------------------------------------
sub read_config_file 
{
    my $scom_proc_config_file = shift;
    # test config file exists and is readable - print error to stdout if it doesn't
    if ( ( ! -e $scom_proc_config_file ) or ( ! -r $scom_proc_config_file ) ) { 
	    print "Configuration file '$scom_proc_config_file' doesnt exist or isnt readable - quitting\n";
	    exit 3;
    };
    
    eval {
        my $config = TypedConfig->new ($scom_proc_config_file);
	    $dbtype		        = $config->get_scalar ('dbtype');
	    $scomdbname	        = $config->get_scalar ('dbname');
	    $scomdbuser	        = $config->get_scalar ('dbuser');
	    $scomdbpass	        = $config->get_scalar ('dbpass');
	    $scomdbport	        = $config->get_scalar ('dbport');
	    $scomdbhost	        = $config->get_scalar ('dbhost');
    };
    if ($@) {
        chomp $@;
        $@ =~ s/^ERROR:\s+//i;
        log_message("Error:  Cannot read config file $scom_proc_config_file ($@) - Quitting"); 
	    exit 2;
    }



}


# ------------------------------------------------------------------------------------------
sub get_options 
{

   my $helpstring = "
Groundwork SCOM 2012 Event Reaper ***Unix Emulator*** [Version $VERSION]
Options:

        -c <interval> - constant feeder mode, waiting <interval> seconds between each cycle
        -d <dir> - sample events directory
        -h - print this message
        -i - import xml events into db
        -p - remove all data from $scomeventtable table
        -s - load events into save table instead
        -v - print version

All other behavior is controlled by the configuration file $config_file.

Copyright (C)2013 GroundWork Opensource
This program comes with absolutely NO WARRANTY either implied or explicit
";

   getopts("vhd:sipc:i",\%opt);
   if ( $opt{c} ) { $feed_interval = $opt{c}; } 
   if ( $opt{d} ) { $event_directory = $opt{d}; } 
   else { 
        if ( not $opt{p} ) { print "Need an events directory!\n"; die $helpstring; }
    }

   if ( $opt{h} ) { print $helpstring; exit 3; }
   if ( $opt{i} ) { $import = 1; }
   if ( $opt{p} ) { $purge = 1; }
   if ( $opt{s} ) { $use_save_table = 1; }
   if ( $opt{v} ) { print "$0 version $VERSION\n"; exit 3; }

    # do some sanity checks
    if ( $opt{d} ) {
        if ( ( ! -e $event_directory ) or ( ! -d $event_directory ) or ( ! -r $event_directory ) ) {
	        log_message("Event directory $event_directory doesn't exist or is not a directory or is not readable - quitting");
	        exit 2;
        }
    }

    if ( $use_save_table ) { $scomeventtable = "scom_save" ; } 

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
    foreach my $xml_property ( sort keys %{$event} ) {

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
    #log_message("SQL = $sql") if $debug_level >= 4 ;

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
sub write_event_to_db_old
{
    my $event=shift;
    my $ConnectorVersion='SCOM_2012_v0';
    my $AlertId=cleanup($event->{AlertId}, "alertid"); 
    if ($event->{ConnectorVersion}){$ConnectorVersion=cleanup($event->{ConnectorVersion}, "ConnectorVersion")}; 
    my $Category=cleanup($event->{Category}, "category"); 
    my $ComputerDomain=cleanup($event->{ComputerDomain}, "computerdomain"); 
    my $ComputerName=cleanup($event->{ComputerName}, "computername"); 
    my $Context=cleanup( $event->{Context}, "Context"); 
    my $Description=cleanup($event->{Description}, "description"); 
    my $EventType=cleanup( $event->{EventType}, "EventType" ); 
    my $LastModifiedByNonConnector=cleanup($event->{LastModifiedByNonConnector}, "LastModifiedByNonConnector"); 
    my $MaintenanceModeLastModified=cleanup($event->{MaintenanceModeLastModified}, "MaintenanceModeLastModified" ); 
    my $ManagementGroupName=cleanup($event->{ManagementGroupName}, "ManagementGroupName" ); 
    my $ManagementPack=cleanup($event->{ManagementPack}, "ManagementPack"); 
    my $ManagementServer=cleanup($event->{ManagementServer}, "ManagementServer"); 
    my $ModifiedBy=cleanup($event->{ModifiedBy}, "ModifiedBy"); 
    my $MonitoringClassId=cleanup($event->{MonitoringClassId}, "MonitoringClassId"); 
    my $MonitoringClassName=cleanup($event->{MonitoringClassName}, "MonitoringClassName" ); 
    my $MonitoringObjectDisplayName=cleanup($event->{MonitoringObjectDisplayName}, "MonitoringObjectDisplayName"); 
    my $MonitoringObjectFullName=cleanup($event->{MonitoringObjectFullName}, "MonitoringObjectFullName"); 
    my $MonitoringObjectHealthState=cleanup($event->{MonitoringObjectHealthState}, "MonitoringObjectHealthState"); 
    my $MonitoringObjectId=cleanup($event->{MonitoringObjectId}, "MonitoringObjectId"); 
    my $MonitoringObjectInMaintenanceMode=cleanup($event->{MonitoringObjectInMaintenanceMode}, "MonitoringObjectInMaintenanceMode"); 
    my $MonitoringObjectName=cleanup($event->{MonitoringObjectName}, "MonitoringObjectName"); 
    my $MonitoringObjectPath=cleanup($event->{MonitoringObjectPath}, "MonitoringObjectPath"); 
    my $MonitoringRuleId=cleanup($event->{MonitoringRuleId}, "MonitoringRuleId"); 
    my $Name=cleanup($event->{Name}, "name"); 
    my $PrincipalName=cleanup($event->{PrincipalName}, "PrincipalName" ); 
    my $Priority=cleanup($event->{Priority}, "Priority"); 
    my $ProblemId=cleanup($event->{ProblemId}, "ProblemId"); 
    my $ProductKnowledge=cleanup( $event->{ProductKnowledge}, "ProductKnowledge" ); 
    my $RepeatCount=cleanup($event->{RepeatCount},"repeatcount"); 
    my $ResolutionState=cleanup($event->{ResolutionState}, "ResolutionState"); 
    my $RuleName=cleanup($event->{RuleName}, "rulename"); 
    my $RuleTarget=cleanup($event->{RuleTarget}, "ruletarget"); 
    my $Severity=cleanup($event->{Severity}, "severity"); 
    my $StateLastModified=cleanup($event->{StateLastModified}, "StateLastModified"); 
    my $TimeAdded=cleanup($event->{TimeAdded}, "timeadded"); 
    my $TimeOfLastEvent=cleanup($event->{TimeOfLastEvent}, "timeoflastevent"); 
    my $TimeResolutionStateLastModified=cleanup($event->{TimeResolutionStateLastModified}, "TimeResolutionStateLastModified"); 
    my $WebConsoleUrl=cleanup($event->{WebConsoleUrl}, "WebConsoleUrl"); 

    my $sql = "INSERT INTO $scomeventtable" ;
    my ( $sqlQuery, $rv );

    $sql=$sql . "(AlertId,ConnectorVersion,Category,ComputerDomain,ComputerName,Context,Description,EventType,LastModifiedByNonConnector,MaintenanceModeLastModified,ManagementGroupName,ManagementPack,ManagementServer,ModifiedBy,MonitoringClassId,MonitoringClassName,MonitoringObjectDisplayName,MonitoringObjectFullName,MonitoringObjectHealthState,MonitoringObjectId,MonitoringObjectInMaintenanceMode,MonitoringObjectName,MonitoringObjectPath,MonitoringRuleId,Name,PrincipalName,Priority,ProblemId,ProductKnowledge,RepeatCount,ResolutionState,RuleName,RuleTarget,Severity,StateLastModified,TimeAdded,TimeOfLastEvent,TimeResolutionStateLastModified,WebConsoleUrl) ";

    $sql=$sql . "values ('$AlertId','$ConnectorVersion','$Category','$ComputerDomain','$ComputerName','$Context','$Description','$EventType','$LastModifiedByNonConnector','$MaintenanceModeLastModified','$ManagementGroupName','$ManagementPack','$ManagementServer','$ModifiedBy','$MonitoringClassId','$MonitoringClassName','$MonitoringObjectDisplayName','$MonitoringObjectFullName','$MonitoringObjectHealthState','$MonitoringObjectId','$MonitoringObjectInMaintenanceMode','$MonitoringObjectName','$MonitoringObjectPath','$MonitoringRuleId','$Name','$PrincipalName','$Priority','$ProblemId','$ProductKnowledge','$RepeatCount','$ResolutionState','$RuleName','$RuleTarget','$Severity','$StateLastModified','$TimeAdded','$TimeOfLastEvent','$TimeResolutionStateLastModified','$WebConsoleUrl')";

    #$sql=$sql . "values ('$event->{AlertId}','$event->{Category}','$event->{ComputerDomain}','$event->{ComputerName}','$event->{Context}','$event->{Description}','$event->{EventType}','$event->{LastModifiedByNonConnector}','$event->{MaintenanceModeLastModified}','$event->{ManagementGroupName}','$event->{ManagementPack}','$event->{ManagementServer}','$event->{ModifiedBy}','$event->{MonitoringClassId}','$event->{MonitoringClassName}','$event->{MonitoringObjectDisplayName}','$event->{MonitoringObjectFullName}','$event->{MonitoringObjectHealthState}','$event->{MonitoringObjectId}','$event->{MonitoringObjectInMaintenanceMode}','$event->{MonitoringObjectName}','$event->{MonitoringObjectPath}','$event->{MonitoringRuleId}','$event->{Name}','$event->{PrincipalName}','$event->{Priority}','$event->{ProblemId}','$event->{ProductKnowledge}','$event->{RepeatCount}','$event->{ResolutionState}','$event->{RuleName}','$event->{RuleTarget}','$event->{Severity}','$event->{StateLastModified}','$event->{TimeAdded}','$event->{TimeOfLastEvent}','$event->{TimeResolutionStateLastModified}','$event->{WebConsoleUrl}')"; 

    #if($debug_level >= 4) {log_message("SQL=$sql\n")} ;

    if ( not $sqlQuery  = $dbh_scom->prepare($sql) ) { 
       #log_message("Can't prepare $sql: $dbh_scom->errstr - Quitting"); 
       log_message("Can't prepare $sql: $DBI::errstr - Quitting"); 
       #exit 2;  # exiting kills the service - lets return after logging the error
       return 99;
    }
    if ( not $rv = $sqlQuery->execute )  {
        #log_message( "Can't execute the query: $sqlQuery->errstr - Quitting"); 
        log_message( "ERROR executing query '$sql' .\nError was : '$DBI::errstr' - Quitting"); 
        #exit 2;  # exiting kills the service - lets return after logging the error
        return 99;
    } 
    $sqlQuery->finish();
    return $rv;
}

# ------------------------------------------------------------------------------------------
sub get_column_info
{
    my $sql ="select column_name,character_maximum_length from INFORMATION_SCHEMA.COLUMNS where table_name = '$scomeventtable';";
    my ( $sqlQuery, $rv );

    #if($debug_level >= 4) {log_message("SQL=$sql\n")} ;

    if ( not $sqlQuery  = $dbh_scom->prepare($sql) ) { 
       log_message("Can't prepare $sql: $DBI::errstr - Quitting"); 
       exit 2;
    }
    if ( not $rv = $sqlQuery->execute )  {
        log_message( "ERROR executing query '$sql' .\nError was : '$DBI::errstr' - Quitting"); 
        exit 2 ;
    } 

    while ( my $row = $sqlQuery->fetchrow_hashref() ) {
       $scom_events_schema_info{ lc($$row{column_name}) } = $$row{character_maximum_length} ;
    }

    #foreach ( sort keys %scom_events_schema_info ) { print "$_ : $scom_events_schema_info{$_}\n"; }
    $sqlQuery->finish();

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

    return $value;
}


# ------------------------------------------------------------------------------------------
sub cleanup_old
{
    my $value=shift;

    if (!$value){$value=0;}
    $value =~ tr/\cM//d;
    $value =~ tr/'/"/;
    $value =~ tr/<//d;
    $value =~ tr/>//d;
    $value =~ tr/&//d;


    return $value;
}

# ------------------------------------------------------------------------------------------
sub readevent
{
    my ( $file ) = @_;
    my $data = undef;
    my $xml = new XML::Simple(suppressempty => 1);
    $data = $xml->XMLin($file);
    #print Dumper $data;
    return $data;
}

# ------------------------------------------------------------------------------------------
sub log_message {
    print "@_\n";
}

# ------------------------------------------------------------------------------------------
sub clear_db {
    my ( $sql, $rv, $sqlQuery ) ;

    print "Clearing all data from $scomeventtable table ...\n";
    my $sql = "delete from $scomeventtable;";
    if ( not $sqlQuery  = $dbh_scom->prepare($sql) ) { 
       log_message("Can't prepare $sql: $DBI::errstr - Quitting"); 
       return 99;
    }
    if ( not $rv = $sqlQuery->execute )  {
        log_message( "ERROR executing query '$sql' .\nError was : '$DBI::errstr' - Quitting"); 
        return 99;
    } 
    $sqlQuery->finish();

}



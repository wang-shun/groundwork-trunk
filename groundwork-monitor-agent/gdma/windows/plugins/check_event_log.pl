#  This script checks the Win32 Event Log for various errors.
#  Based on a script by Dave Roth called CheckEventLog.pl 
#  GroundWork 11/2008

#use strict;
use Getopt::Long;
use Time::Local;
use Win32::EventLog;

use vars qw ( $VERSION $SEC $MIN $HOUR $DAY %EVENT_TYPE %Config 
	      $TIME_LIMIT $EVENT_MASK %Event $Count $Result 
	      @EVENTS %ERRORS $DEBUG
            );

$VERSION = 20081110;
$SEC = 1;
$MIN = 60 * $SEC;
$HOUR = 60 * $MIN;
$DAY = 24 * $HOUR;

%EVENT_TYPE = (
    eval EVENTLOG_AUDIT_FAILURE     =>  'AUDIT_FAILURE',
    eval EVENTLOG_AUDIT_SUCCESS     =>  'AUDIT_SUCCESS',
    eval EVENTLOG_ERROR_TYPE        =>  'ERROR',
    eval EVENTLOG_WARNING_TYPE      =>  'WARNING',
    eval EVENTLOG_INFORMATION_TYPE  =>  'INFORMATION',
);

%ERRORS=('OK'=>0,'WARNING'=>1,'CRITICAL'=>2,'UNKNOWN'=>3);

%Config = (
    log     =>  'System',
);
Configure( \%Config );
$DEBUG = $Config{debug};

if( $Config{help} )
{
    Syntax();
    exit $ERRORS{"UNKNOWN"};
}
if( defined $Config{date} )
{
    my( $Year, $Month, $Day ) = ( $Config{date} =~ /^(\d{4}).(\d{2}).(\d{2})/ );
    $TIME_LIMIT = timelocal( 0, 0, 0, $Day, $Month - 1, $Year - 1900 );
}
elsif( $Config{hour} || $Config{day} || $Config{min})
{
    $TIME_LIMIT = time() - ( $DAY * $Config{day} ) - ( $HOUR * $Config{hour} )  - ( $MIN * $Config{min} );
}

if( ! scalar @{$Config{machine}} )
{
    push( @{$Config{machine}}, Win32::NodeName );
}

if( defined( $Config{type} ) )
{
    foreach my $Mask ( @{$Config{type}} )
    {
        # Try referencing the EVENTLOG_xxxx_TYPE and EVENTLOG_xxxxx
        # constants. One of them is bound to work.
        $EVENT_MASK |= eval( "EVENTLOG_" . uc( $Mask ) . "_TYPE" );
        $EVENT_MASK |= eval( "EVENTLOG_" . uc( $Mask ) );
    }
}
else
{
    map
    {
        $EVENT_MASK |= 0 + $_;
    }( keys( %EVENT_TYPE ) );
}

# Tell the extension to always attempt to fetch the
# event log message table text
$Win32::EventLog::GetMessageText = 1;
$~ = EventLogFormat;
foreach my $Machine ( @{$Config{machine}} )
{
    my $EventLog; 
    my $realEventId;
    my %foundEvents;
    if( $EventLog = Win32::EventLog->new( $Config{log}, $Machine ) )
    {
        my %Records;
        local %Event;
        local $Count = 0;
        
        while( (   $EventLog->Read( EVENTLOG_BACKWARDS_READ | EVENTLOG_SEQUENTIAL_READ, 0, \%Event ) )
                   && ( $Event{TimeGenerated} > $TIME_LIMIT ) 
	        )
        {
            write if $DEBUG;
            $Count++;
	    $realEventId = $Event{EventID} & 0xffff; 
	    if ( array_contains( $realEventId , @EVENTS )  )
	    {
		    $foundEvents{$realEventId}++; 
	    }
        }
    }
    else
    {
        print "Can not connect to the $Config{log} Event Log on $Machine.\n";
    }


    if ( %foundEvents ) 
    {
         print "CRITICAL - Events detected : ";
	 print join(", ", map { "$_\[$foundEvents{$_}]" } keys %foundEvents);
	 exit $ERRORS{"CRITICAL"};
    }
    else
    { 
	 print "OK - No events detected\n";
	 exit $ERRORS{"OK"};
    }


}

sub array_contains
{
   # checks for an element existing in an array
   # Takes args <item to check for>, <array>
   # returns 1 if item found, 0 otherwise

   my ($item,@array) = @_;

   my $array_elem; my $found = 0;
   foreach $array_elem ( @array )
   {
       if ( $array_elem eq $item ) { $found = 1 ; last; }
   }
   return $found;
}


sub Configure
{
    my( $Config ) = @_;

    Getopt::Long::Configure( "prefix_pattern=(-|\/)" );
    $Result = GetOptions( $Config, 
                            qw(
                                machine|m=s@
                                log|l=s
                                type|t=s@
                                min=i
                                hour|h=i
                                day|d=i
                                date=s
                                help|?
                                source=s
                                cfg=s
				events=s
				debug
                            )
                        );
    $Config->{help} = 1 if( ! $Result );
    push( @{$Config->{machine}}, Win32::NodeName() ) unless( scalar @{$Config->{machine}} );
    @EVENTS = split(",", $Config->{events});
}

sub Syntax
{
    my( $Script ) = ( $0 =~ /([^\\]*?)$/ );
    my $Whitespace = " " x length( $Script );
    print<< "EOT";

This plugin checks the Win32 Event Log for various errors.

Syntax:
    $Script [-m Machine] [-t EventType] [-l Log]
    $Whitespace [-h Hours] [-d Days] [-date Date]
    $Whitespace [-help]
        -m Machine......Name of machine whose Event Log is to be examined.
                        This switch can be specified multiple times. 
        -t EventType....Type of event to display:
                            ERROR
                            WARNING
                            INFORMATION
                            AUDIT_SUCCESS
                            AUDIT_FAILURE
                        This switch can be specified multiple times.    
        -l Log..........Name of Event Log to examine. Common examples:
                            Application
                            Security
                            System
                        This switch can be specified multiple times.    
        -h Hours........Will consider events between now and the specified
                        number of hours previous.
        -mins mins........Will consider events between now and the specified
                        number of minutes previous.
        -d Days.........Will consider events between now and the specified
                        number of days previous.                        
        -date Date......Will consider events between now and the specified
                        date.  Date is in international time format
                        (eg. 2000.07.18)                        
	-events <event,event,event>...A comma seperated list of events ID's 
	                to search for. Finding any of them returns a critical.
        -debug...display the events in the given time range
EOT
}

format EventLogFormat =
--------------------------------
@>>>>> @<<<<<<<<<<<<<<<<<<<<<<<<<<<<  ^<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
$Event{EventID}&0xffff,  $Event{RecordNumber}, "\\\\" . $Event{Computer},     $Event{Message}
       @<<<<<<<<<<<<<<<<<<<<<<<<<<<<  ^<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
scalar localtime( $Event{TimeGenerated} ), $Event{Message}
       Type: @<<<<<<<<<<<<<<<<<<<<<<  ^<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
$EVENT_TYPE{$Event{EventType}}, $Event{Message}
       Source: @<<<<<<<<<<<<<<<<<<<<  ^<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
$Event{Source},                       $Event{Message}
~                                     ^<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
$Event{Category},                     $Event{Message}
~                                     ^<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                                      $Event{Message}
~                                     ^<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                                      $Event{Message}
~                                     ^<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                                      $Event{Message}
~                                     ^<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                                      $Event{Message}
.

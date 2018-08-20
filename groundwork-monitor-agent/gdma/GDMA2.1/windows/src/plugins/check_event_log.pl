#  This script checks the Win32 Event Log for various errors.
#  Based on a script by Dave Roth called CheckEventLog.pl
#  Last revised by GroundWork 2011-09-20

# use strict;
use Getopt::Long;
use Time::Local;
use Win32::EventLog;

use vars qw ( $VERSION $SEC $MIN $HOUR $DAY %EVENT_TYPE %Config
  $TIME_LIMIT $EVENT_MASK %Event $Count $Result %EVENTS %ERRORS $DEBUG
);

$VERSION = 20110920;
$SEC     = 1;
$MIN     = 60 * $SEC;
$HOUR    = 60 * $MIN;
$DAY     = 24 * $HOUR;

%EVENT_TYPE = (
    eval EVENTLOG_AUDIT_FAILURE    => 'AUDIT_FAILURE',
    eval EVENTLOG_AUDIT_SUCCESS    => 'AUDIT_SUCCESS',
    eval EVENTLOG_ERROR_TYPE       => 'ERROR',
    eval EVENTLOG_WARNING_TYPE     => 'WARNING',
    eval EVENTLOG_INFORMATION_TYPE => 'INFORMATION',
);

%ERRORS = ( 'OK' => 0, 'WARNING' => 1, 'CRITICAL' => 2, 'UNKNOWN' => 3 );

%Config = ( log => 'System' );
Configure( \%Config );
$DEBUG = $Config{debug};

if ( $Config{help} ) {
    Syntax();
    exit $ERRORS{'UNKNOWN'};
}
if ( defined $Config{date} ) {
    my $Year  = 0;
    my $Month = 0;
    my $Day   = 0;
    ( $Year, $Month, $Day ) = ( $Config{date} =~ /^(\d{4}).(\d{2}).(\d{2})/ );
    if ($Year eq 0 or $Month eq 0 or $Day eq 0) {
	print "ERROR:  bad -date specified; see -help output\n";
	exit $ERRORS{'UNKNOWN'};
    }
    $TIME_LIMIT = timelocal( 0, 0, 0, $Day, $Month - 1, $Year - 1900 );
}
elsif ( $Config{day} || $Config{hour} || $Config{min} ) {
    if ($Config{day} && $Config{day} !~ /^\d+$/) {
	print "ERROR:  bad -d specified; see -help output\n";
	exit $ERRORS{'UNKNOWN'};
    }
    if ($Config{hour} && $Config{hour} !~ /^\d+$/) {
	print "ERROR:  bad -h specified; see -help output\n";
	exit $ERRORS{'UNKNOWN'};
    }
    if ($Config{min} && $Config{min} !~ /^\d+$/) {
	print "ERROR:  bad -min specified; see -help output\n";
	exit $ERRORS{'UNKNOWN'};
    }
    $TIME_LIMIT = time() - ( $DAY * ( $Config{day} || 0 ) ) - ( $HOUR * ( $Config{hour} || 0 ) ) - ( $MIN * ( $Config{min} || 0 ) );
}
else {
    print "ERROR:  no time span specified; see -date, -d, -h, -min in -help output\n";
    exit $ERRORS{'UNKNOWN'};
}
if (not %EVENTS) {
    print "ERROR:  no events specified; you must use -events (see -help output)\n";
    exit $ERRORS{'UNKNOWN'};
}

if ( defined( $Config{type} ) ) {
    foreach my $Mask ( @{ $Config{type} } ) {
	## Try referencing the EVENTLOG_xxxx_TYPE and EVENTLOG_xxxxx
	## constants.  One of them is bound to work.  If not, complain.
	my $mask = 0;
	$mask |= eval( "EVENTLOG_" . uc($Mask) . "_TYPE" );
	$mask |= eval( "EVENTLOG_" . uc($Mask) );
	if ( $mask == 0 ) {
	    print "ERROR:  event type $Mask is unsupported (see -help output)\n";
	    exit $ERRORS{'UNKNOWN'};
	}
	$EVENT_MASK |= $mask;
    }
}
else {
    map { $EVENT_MASK |= 0 + $_; } ( keys(%EVENT_TYPE) );
}

# Tell the extension to always attempt to fetch the event log message table text.
$Win32::EventLog::GetMessageText = 1;
$~                               = EventLogFormat;
foreach my $Machine ( @{ $Config{machine} } ) {
    my $EventLog;
    my $realEventId;
    my %foundEvents;
    if ( $EventLog = Win32::EventLog->new( $Config{log}, $Machine ) ) {
	my %Records;
	local %Event;
	local $Count = 0;

	while (( $EventLog->Read( EVENTLOG_BACKWARDS_READ | EVENTLOG_SEQUENTIAL_READ, 0, \%Event ) )
	    && ( $Event{TimeGenerated} > $TIME_LIMIT ) )
	{
	    write if $DEBUG;
	    if ($Event{EventType} & $EVENT_MASK) {
		$realEventId = $Event{EventID} & 0xffff;
		if ( $EVENTS{$realEventId} ) {
		    $Count++;
		    $foundEvents{$realEventId}++;
		}
	    }
	}
    }
    else {
	print "Cannot connect to the $Config{log} Event Log on $Machine.\n";
    }

    if (%foundEvents) {
	print "CRITICAL - $Count events detected on $Machine: ";
	print join( ", ", map { "$_\[$foundEvents{$_}]" } sort { $a <=> $b } keys %foundEvents );
	print "\n";
	exit $ERRORS{"CRITICAL"};
    }
    else {
	print "OK - No events detected\n";
	exit $ERRORS{"OK"};
    }
}

sub Configure {
    my ($Config) = @_;

    Getopt::Long::Configure("prefix_pattern=(-|\/)");
    $Result = GetOptions(
	$Config, qw(
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
    $Config->{help} = 1 if ( !$Result );
    push( @{ $Config->{machine} }, Win32::NodeName() ) unless ( scalar @{ $Config->{machine} } );
    %EVENTS = map { $_ => 1 } ( split( ",", $Config->{events} ) );
}

sub Syntax {
    my ($Script) = ( $0 =~ /([^\\]*?)$/ );
    my $Whitespace = " " x length($Script);
    print << "EOT";

This plugin checks the Win32 Event Log for various errors.

Syntax:
    $Script -events <event,event,event> [-t EventType] [-m Machine] [-l Log]
    $Whitespace { -date Date | [-d Days] [-h Hours] [-min Minutes] } [-debug]
    $Script [-help]
        -events <event,event,event>
                        Mandatory parameter (this script looks for just
                        these specific events).  A comma-separated list of
                        numeric event IDs to search for.  Finding any of them
                        returns a critical state.
        -t EventType    Type of events to recognize:
                            ERROR
                            WARNING
                            INFORMATION
                            AUDIT_SUCCESS
                            AUDIT_FAILURE
                        This switch can be specified multiple times.
                        Default is all of those listed types of events.
        -m Machine      Name of machine whose Event Log is to be examined.
                        This switch can be specified multiple times.  Default
                        is the machine on which this plugin is running.  The
                        plugin reports results only for the log on the first
                        machine on which it finds some matching log events,
                        without checking any additional machines.
        -l Log          Name of Event Log to examine. Common examples:
                            Application
                            Security
                            System
                        This switch can only be specified once; a later
                        mention will override earlier mentions.  Default
                        is "System".
        -date Date      Will consider events between now and the specified
                        prior date.  Date is in international time format
                        (e.g., 2000.07.18).
        -d Days         Will consider events between now and the specified
                        number of days previous.
        -h Hours        Will consider events between now and the specified
                        number of hours previous.
        -min Minutes    Will consider events between now and the specified
                        number of minutes previous.
        -debug          Display all the events encountered in the given time
                        range, even if they do not match -events or -t as
                        specified on the command line.  This can be helpful
                        when the plugin is run from the command line, to
                        identify what events to look for.
        Only events which match both of the -events and -t filters are
        recognized and reported.
        Either -date or some combination of -d, -h, and -min must be
        specified, to constrain the search period.  The -d, -h, and -min
        values will be combined together to determine the log start point,
        if more than one of these is specified.
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

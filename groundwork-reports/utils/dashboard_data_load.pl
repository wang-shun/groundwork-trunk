#!/usr/local/groundwork/perl/bin/perl --
#
#	GroundWork Monitor - The ultimate data integration framework.
#	Copyright (C) 2004-2016 GroundWork Open Source, Inc.
#	www.groundworkopensource.com
#
#	This program is free software; you can redistribute it and/or modify
#	it under the terms of version 2 of the GNU General Public License
#	as published by the Free Software Foundation.
#
#	This program is distributed in the hope that it will be useful,
#	but WITHOUT ANY WARRANTY; without even the implied warranty of
#	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
#	GNU General Public License for more details.
#
#	You should have received a copy of the GNU General Public License
#	along with this program; if not, write to the Free Software
#	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
#

use strict;

use DBI;
use Time::Local;
use Getopt::Std;

# This script will load the SQL dashboard database to enable the Groundwork
# Insight Reports.  If the set of files being processed in a single run contains
# any data for a specific date, then measurements for that date will be inserted
# into the database if they do not already exist, and replaced in the database
# if they do exist.  This means that we depend on Nagios rotating its logfile
# exactly at midnight, as well as possibly other times during the day, so we
# never have the situation where we process a set of files that includes only
# the trailing part of a day's events.  (If we did have that situation, data on
# all earlier events for that day would be lost.)

my $helpstring = "
This script will load the SQL dashboard database used by the
Groundwork Insight Reports.  Options are:

-a <DIRECTORY>
	Read all files in the named log directory, which must be specified as an
	absolute path.  Typically used to read all log files in an archive, i.e.,
	/usr/local/groundwork/nagios/var/archives
	It is not required to specify this option; any files read via this option
	are in addition to the one particular file specified by the -f option.
-c <INSIGHT REPORTS CONFIG FILE>
	Config file for Insight Reports scripts.  A standard default is provided
	(/usr/local/groundwork/core/reports/etc/gwir.cfg).
-C <NAGIOS MAIN CONFIG FILE>
	Main Nagios config file.  This is used to get contact and host group data.
        This program will run if this is not set, however the notification contact
	reports and host group reports will not work properly.  A standard default
	is provided by the nagios_cfg_file option within the standard Insight
	Reports config file.
-f <NAGIOS LOG FILE>
	Read this Nagios log file.  The default is \"<NAGIOS_VAR>/nagios.log\",
	as provided by the nagios_event_log option within the standard Insight
	Reports config file.
-L <OUTPUT LOG>
	Log file containing status messages from this program.  The default is
	\"<GROUNDWORK>/core/reports/utils/log/dashboard_data.log\", as provided by
	the dashboard_data_log option in the standard Insight Reports config file.
-d      Debug mode.  Will log additional messages to the log file.  The default
	setting is provided via the dashboard_data_debug option in the standard
	Insight Reports config file.
-h	Displays help message.

GroundWork Monitor - The ultimate data integration framework.
Copyright (c) 2008-2016 GroundWork Open Source Solutions
www.groundworkopensource.com

This program is free software; you can redistribute it and/or modify
it under the terms of version 2 of the GNU General Public License
as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
";

my $Logfile;
my @NagiosLogs = ();
my $debug      = 0;
my %opt        = ();
if ( not getopts( "a:c:C:f:L:dh", \%opt ) ) {
    print $helpstring;
    exit;
}
if ( $opt{h} ) {
    print $helpstring;
    exit;
}
my $configfile;
if ( $opt{c} ) {
    $configfile = $opt{c};
}
else {
    $configfile = "/usr/local/groundwork/core/reports/etc/gwir.cfg";
}
my $config_ref = readNagiosReportsConfig($configfile);
my $logfile    = $config_ref->{dashboard_data_log};
if ( open( LOG, '>', "$logfile" ) ) {
    print "Writing to log file $logfile\n";
    print LOG "Executing Nagios host group feeder at " . time_text(time) . "\n" or print "Error writing to log file.";
    print LOG "Using configuration file $configfile\n";
    foreach my $parm ( sort keys %{$config_ref} ) {
	## We suppress logging of credentials, as that would constitute a security hole.
	if ( $parm !~ /user|password/ ) {
	    print     "\t$parm=$config_ref->{$parm}\n";
	    print LOG "\t$parm=$config_ref->{$parm}\n";
	}
    }
}
else {
    print "Can't open log file $logfile\n";
    print "Executing without a log file\n";
}

if ( $opt{d} ) {
    $debug = 1;
}
else {
    $debug = $config_ref->{dashboard_data_debug};
}
if ( $opt{L} ) {
    $Logfile = $opt{L};
}
else {
    $Logfile = $config_ref->{dashboard_data_log};
}
print LOG "Using $Logfile as a log file\n";

my $NagiosConfig = undef;
if ( $opt{C} ) {
    $NagiosConfig = $opt{C};
}
else {
    $NagiosConfig = $config_ref->{nagios_cfg_file};
}
print LOG "Using Nagios config file $NagiosConfig\n";

if ( $opt{a} ) {
    print LOG "Reading archive directory $opt{a}\n";
    my @lines = `ls -l $opt{a}`;
    $opt{a} =~ s{(?<=.)/$}{};    # get rid of trailing "/", but only if it's not the only character
    foreach my $line (@lines) {
	if ( $line =~ /\s(\S+)\s*$/ ) {
	    push @NagiosLogs, $opt{a} . '/' . $1;
	}
    }
}

if ( $opt{f} ) {
    push @NagiosLogs, $opt{f};
}
else {
    push @NagiosLogs, $config_ref->{nagios_event_log};
}
foreach my $tmp (@NagiosLogs) {
    print LOG "Adding log file $tmp.\n";
}

#open(LOG,'>>', "$Logfile");	# Append to logfile
open( LOG, '>', "$Logfile" );   # Overwrite logfile
my ( $sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $isdst ) = localtime(time);
$year += 1900;
my $month = (qw(January February March April May June July August September October November December))[$mon];
my $timestring = sprintf "%02d:%02d:%02d", $hour, $min, $sec;
my $thisday = (qw( Sunday Monday Tuesday Wednesday Thursday Friday Saturday ))[$wday];
print LOG "Dashboard database load starting at $timestring on $thisday, $month $mday, $year.\n";
print LOG "Debug set to $debug\n";

#	Read Host Group configuration file and create
#	references for Host->HostGroup and HostGroup->Host
my ( $hostgroup_ref,    $host_ref )    = read_hostgroupconfig($NagiosConfig);
my ( $contactgroup_ref, $contact_ref ) = read_contactgroupconfig($NagiosConfig);
my @tmp = ();
if ($debug) {
    foreach my $key ( keys %$hostgroup_ref ) {
	@tmp = keys %{ $hostgroup_ref->{$key}->{"HOSTS"} };
	print LOG "Host Group: $key:@tmp\n\n";
    }
    foreach my $key ( keys %$host_ref ) {
	@tmp = keys %{ $host_ref->{$key}->{"HOST GROUPS"} };
	print LOG "Host: $key:@tmp\n\n";
    }
    if (defined $contactgroup_ref) {
	foreach my $key ( keys %$contactgroup_ref ) {
	    @tmp = keys %{ $contactgroup_ref->{$key}->{"CONTACTS"} };
	    print LOG "Contact Group: $key:@tmp\n\n";
	}
    }
    if (defined $contact_ref) {
	foreach my $key ( keys %$contact_ref ) {
	    @tmp = keys %{ $contact_ref->{$key}->{"CONTACT GROUPS"} };
	    print LOG "Contact: $key:@tmp\n\n";
	}
    }
}

my $logrecordcount  = 0;
my $logprocesscount = 0;

my $day = {};

my $logtime;
my $alerttype;
my $contact;
my $hostname;
my $servicename;
my $status;
my $statustype;
my $currentattempt;
my $output;
my $notifyscript;
my $timet;
my $datet;
my @cgs = ();
my @hgs = ();

foreach my $NagiosLog (@NagiosLogs) {
    ## Now check log file for any state changes since last cycle
    if ( !open( NAGIOSLOG, $NagiosLog ) ) {
	print LOG "Can't open Nagios Log $NagiosLog\n";
	next;
    }
    print LOG "Processing nagios log file $NagiosLog\n";
    print "Processing nagios log file $NagiosLog\n";
    while ( my $line = <NAGIOSLOG> ) {
	$logrecordcount++;
	chomp $line;
	if ( $line =~ /\[(\d+)\]\s(HOST ALERT.*?):\s*(.*?);(.*?);(.*?);(.*?);(.*)/ ) {
	    $logtime        = $1;
	    $alerttype      = $2;
	    $hostname       = $3;
	    $servicename    = "";
	    $status         = $4;
	    $statustype     = $5;
	    $currentattempt = $6;
	    $output         = $7;
	    ( $timet, $datet ) = gettime($logtime);

	    if ( ( $status eq "DOWN" ) and ( $statustype eq "HARD" ) ) {
		$day->{$datet}->{"ALERTS"}++;
		$day->{$datet}->{"HOST"}->{$hostname}->{"DOWN COUNT"}++;
		@hgs = keys %{ $host_ref->{$hostname}->{"HOST GROUPS"} };
		foreach my $hg (@hgs) {
		    $day->{$datet}->{"HOST GROUP"}->{$hg}->{"DOWN COUNT"}++;
		}
		$logprocesscount++;
	    }
	    else {
		if ( $day->{$datet}->{"HOST"}->{$hostname}->{"DOWN COUNT"} eq undef ) {    # instantiate this object. Use for $managed_hosts
		    $day->{$datet}->{"HOST"}->{$hostname}->{"DOWN COUNT"} = 0;
		}
	    }
	}
	elsif ( $line =~ /\[(\d+)\]\s(SERVICE ALERT.*?):\s*(.*?);\s*(.*?);(.*?);(.*?);(.*)/ ) {
	    $logtime        = $1;
	    $alerttype      = $2;
	    $hostname       = $3;
	    $servicename    = $4;
	    $status         = $5;
	    $statustype     = $6;
	    $currentattempt = $7;
	    $output         = $8;
	    ( $timet, $datet ) = gettime($logtime);

	    if ( ( $status eq "CRITICAL" ) and ( $statustype eq "HARD" ) ) {
		$day->{$datet}->{"ALERTS"}++;
		$day->{$datet}->{"HOST"}->{$hostname}->{"CRITICAL COUNT"}++;
		$day->{$datet}->{"HOST"}->{$hostname}->{"SERVICES"}->{$servicename}->{"CRITICAL COUNT"}++;
		$day->{$datet}->{"SERVICE"}->{$servicename}->{"CRITICAL COUNT"}++;
		$logprocesscount++;
		@hgs = keys %{ $host_ref->{$hostname}->{"HOST GROUPS"} };
		foreach my $hg (@hgs) {
		    $day->{$datet}->{"HOST GROUP"}->{$hg}->{"CRITICAL COUNT"}++;
		}
	    }
	    elsif ( $status eq "WARNING" ) {
		$day->{$datet}->{"WARNINGS"}++;
		$day->{$datet}->{"HOST"}->{$hostname}->{"WARNING COUNT"}++;
		$day->{$datet}->{"HOST"}->{$hostname}->{"SERVICES"}->{$servicename}->{"WARNING COUNT"}++;
		$day->{$datet}->{"SERVICE"}->{$servicename}->{"WARNING COUNT"}++;
		@hgs = keys %{ $host_ref->{$hostname}->{"HOST GROUPS"} };
		foreach my $hg (@hgs) {
		    $day->{$datet}->{"HOST GROUP"}->{$hg}->{"WARNING COUNT"}++;
		}
		$logprocesscount++;
	    }
	    else {
		if ( $day->{$datet}->{"HOST"}->{$hostname}->{"SERVICES"}->{$servicename}->{"DOWN COUNT"} eq undef )
		{    # instantiate this object. Use for $managed_hosts
		    $day->{$datet}->{"HOST"}->{$hostname}->{"SERVICES"}->{$servicename}->{"DOWN COUNT"} = 0;
		}
	    }
	}
	elsif ( $line =~ /\[(\d+)\]\s(HOST NOTIFICATION.*?):\s*(.*?);(.*?);(.*?);(.*?);(.*)/ ) {
	    $logtime      = $1;
	    $alerttype    = $2;
	    $contact      = $3;
	    $hostname     = $4;
	    $servicename  = "";
	    $status       = $5;    # DOWN, UNREACHABLE, UP
	    $notifyscript = $6;
	    ( $timet, $datet ) = gettime($logtime);
	    $day->{$datet}->{"NOTIFICATIONS"}++;
	    $day->{$datet}->{"NOTIFY COMMAND"}->{$notifyscript}->{"NOTIFICATION COUNT"}++;
	    $day->{$datet}->{"CONTACT"}->{$contact}->{"NOTIFICATION COUNT"}->{$status}++;
	    $day->{$datet}->{"CONTACT"}->{$contact}->{"NOTIFICATION COUNT"}->{"ALL"}++;
	    $day->{$datet}->{"HOST"}->{$hostname}->{"NOTIFICATION COUNT"}++;        # Only Host Notifications
	    $day->{$datet}->{"HOST"}->{$hostname}->{"NOTIFICATION COUNT ALL"}++;    # Host and Service Notifications
	    @hgs = keys %{ $host_ref->{$hostname}->{"HOST GROUPS"} };
	    foreach my $hg (@hgs) {
		$day->{$datet}->{"HOST GROUP"}->{$hg}->{"NOTIFICATION COUNT"}++;
	    }
	    @cgs = defined($contact_ref) ? keys %{ $contact_ref->{$contact}->{"CONTACT GROUPS"} } : ();
	    foreach my $cg (@cgs) {
		$day->{$datet}->{"CONTACT GROUP"}->{$cg}->{"NOTIFICATION COUNT"}->{$status}++;
		$day->{$datet}->{"CONTACT GROUP"}->{$cg}->{"NOTIFICATION COUNT"}->{"ALL"}++;
	    }
	    $logprocesscount++;
	}
	elsif ( $line =~ /\[(\d+)\]\s(SERVICE NOTIFICATION.*?):\s*(.*?);(.*?);(.*?);(.*?);(.*?);(.*)/ ) {
	    $logtime      = $1;
	    $alerttype    = $2;
	    $contact      = $3;
	    $hostname     = $4;
	    $servicename  = $5;
	    $status       = $6;    # CRITICAL, WARNING or OK
	    $notifyscript = $7;
	    ( $timet, $datet ) = gettime($logtime);
	    $day->{$datet}->{"NOTIFICATIONS"}++;
	    $day->{$datet}->{"NOTIFY COMMAND"}->{$notifyscript}->{"NOTIFICATION COUNT"}++;
	    $day->{$datet}->{"CONTACT"}->{$contact}->{"NOTIFICATION COUNT"}->{$status}++;
	    $day->{$datet}->{"CONTACT"}->{$contact}->{"NOTIFICATION COUNT"}->{"ALL"}++;
	    $day->{$datet}->{"HOST"}->{$hostname}->{"NOTIFICATION COUNT ALL"}++;    # Host and Service Notifications
	    $day->{$datet}->{"HOST"}->{$hostname}->{"SERVICES"}->{$servicename}->{"NOTIFICATION COUNT"}++;
	    $day->{$datet}->{"SERVICE"}->{$servicename}->{"NOTIFICATION COUNT"}++;
	    @hgs = keys %{ $host_ref->{$hostname}->{"HOST GROUPS"} };
	    foreach my $hg (@hgs) {
		$day->{$datet}->{"HOST GROUP"}->{$hg}->{"NOTIFICATION COUNT"}++;
	    }
	    @cgs = defined($contact_ref) ? keys %{ $contact_ref->{$contact}->{"CONTACT GROUPS"} } : ();
	    foreach my $cg (@cgs) {
		$day->{$datet}->{"CONTACT GROUP"}->{$cg}->{"NOTIFICATION COUNT"}->{$status}++;
		$day->{$datet}->{"CONTACT GROUP"}->{$cg}->{"NOTIFICATION COUNT"}->{"ALL"}++;
	    }
	    $logprocesscount++;
	}
	else {
	    next;
	}
    }
    close NAGIOSLOG;
}

if ($debug) {
    print "\nHost Group Info:\n";
    foreach my $key ( sort keys %$day ) {
	print "\nDay $key. Total # alerts=" . $day->{$key}->{"ALERTS"} . "\n";
	foreach my $key2 ( keys %{ $day->{$key}->{"HOST GROUP"} } ) {
	    print "Host Group $key2:\n";
	    print "\tDown:" . $day->{$key}->{"HOST GROUP"}->{$key2}->{"DOWN COUNT"} . "\n";
	    print "\tCritical:" . $day->{$key}->{"HOST GROUP"}->{$key2}->{"CRITICAL COUNT"} . "\n";
	    print "\tWarning:" . $day->{$key}->{"HOST GROUP"}->{$key2}->{"WARNING COUNT"} . "\n";
	    print "\tNotifications:" . $day->{$key}->{"HOST GROUP"}->{$key2}->{"NOTIFICATION COUNT"} . "\n";
	}
    }
    print "\nHost Info:\n";
    foreach my $key ( sort keys %$day ) {
	print "\nDay $key. Total # alerts=" . $day->{$key}->{"ALERTS"} . "\n";
	foreach my $key2 ( keys %{ $day->{$key}->{"HOST"} } ) {
	    print "Host $key2:\n";
	    print "\tDown:" . $day->{$key}->{"HOST"}->{$key2}->{"DOWN COUNT"} . "\n";
	    print "\tCritical:" . $day->{$key}->{"HOST"}->{$key2}->{"CRITICAL COUNT"} . "\n";
	    print "\tWarning:" . $day->{$key}->{"HOST"}->{$key2}->{"WARNING COUNT"} . "\n";
	    print "\tNotifications:" . $day->{$key}->{"HOST"}->{$key2}->{"NOTIFICATION COUNT"} . "\n";
	}
    }
    foreach my $key ( sort keys %$day ) {
	print "\nDay $key. Total # alerts=" . $day->{$key}->{"ALL ALERTS"} . "\n";
	foreach my $key2 ( keys %{ $day->{$key}->{"HOST"} } ) {
	    print "Host $key2\n";
	    foreach my $key3 ( keys %{ $day->{$key}->{"HOST"}->{$key2}->{"SERVICES"} } ) {
		if ( $key3 =~ /evalid/i ) {
		    print "\tService $key3:  # Criticals="
		      . $day->{$key}->{"HOST"}->{$key2}->{"SERVICES"}->{$key3}->{"CRITICAL COUNT"}
		      . " # Warnings="
		      . $day->{$key}->{"HOST"}->{$key2}->{"SERVICES"}->{$key3}->{"CRITICAL COUNT"}
		      . " # Notifications="
		      . $day->{$key}->{"HOST"}->{$key2}->{"SERVICES"}->{$key3}->{"NOTIFICATION COUNT"} . "\n";
		}
	    }
	}
    }
}

#my ($dbname,$dbhost,$dbuser,$dbpass,$dbtype) = CollageQuery::readGroundworkDBConfig("insightreports");
my $dbname = $config_ref->{dbname};
my $dbhost = $config_ref->{dbhost};
my $dbuser = $config_ref->{dbusername};
my $dbpass = $config_ref->{dbpassword};
my $dbtype = $config_ref->{dbtype};

my $dsn = '';
if ( defined($dbtype) && $dbtype eq 'postgresql' ) {
    $dsn = "DBI:Pg:dbname=$dbname;host=$dbhost";
}
else {
    $dsn = "DBI:mysql:database=$dbname;host=$dbhost";
}

my $dbh;
if ( !( $dbh = DBI->connect( $dsn, $dbuser, $dbpass, { 'AutoCommit' => 1 } ) ) ) {
    my $errstr = $DBI::errstr;
    chomp $errstr;
    print LOG "Can't connect to database $dbname. Error: $errstr\n";
    exit 1;
}
## We suppress logging of credentials, as that would constitute a security hole.
# print LOG "Connected OK to database $dbname with user $dbuser.\n";
print LOG "Connected OK to database $dbname.\n";

foreach my $key ( sort keys %$day ) {
    print LOG "Day $key. ";
    print LOG "\tTotal # alerts=" . $day->{$key}->{"ALERTS"};
    print LOG "\tTotal # warnings=" . $day->{$key}->{"WARNINGS"};
    print LOG "\tTotal # notifications=" . $day->{$key}->{"NOTIFICATIONS"} . "\n";
    my $managed_hostgroups   = 0;
    my $managed_hosts        = 0;
    my $managed_hostservices = 0;

    # Insert Hostgroup entry, Host entry, Insert Service entry
    # Construct the query line
    update_db( $key, "nagios alerts",        "all", "daily", $day->{$key}->{"ALERTS"} );
    update_db( $key, "nagios warnings",      "all", "daily", $day->{$key}->{"WARNINGS"} );
    update_db( $key, "nagios notifications", "all", "daily", $day->{$key}->{"NOTIFICATIONS"} );
    foreach my $hg ( keys %{ $day->{$key}->{"HOST GROUP"} } ) {
	update_db( $key, "nagios alerts",        "hostgroup:$hg", "daily", $day->{$key}->{"HOST GROUP"}->{$hg}->{"CRITICAL COUNT"} );
	update_db( $key, "nagios warnings",      "hostgroup:$hg", "daily", $day->{$key}->{"HOST GROUP"}->{$hg}->{"WARNING COUNT"} );
	update_db( $key, "nagios notifications", "hostgroup:$hg", "daily", $day->{$key}->{"HOST GROUP"}->{$hg}->{"NOTIFICATION COUNT"} );
	$managed_hostgroups++;
    }
    foreach my $host ( keys %{ $day->{$key}->{"HOST"} } ) {
	update_db( $key, "nagios alerts",            "host:$host", "daily", $day->{$key}->{"HOST"}->{$host}->{"CRITICAL COUNT"} );
	update_db( $key, "nagios warnings",          "host:$host", "daily", $day->{$key}->{"HOST"}->{$host}->{"WARNING COUNT"} );
	update_db( $key, "nagios notifications",     "host:$host", "daily", $day->{$key}->{"HOST"}->{$host}->{"NOTIFICATION COUNT"} );
	update_db( $key, "nagios notifications all", "host:$host", "daily", $day->{$key}->{"HOST"}->{$host}->{"NOTIFICATION COUNT ALL"} );
	foreach my $service ( keys %{ $day->{$key}->{"HOST"}->{$host}->{"SERVICES"} } ) {
	    update_db(
		$key,
		"nagios alerts",
		"hostservice:$host / $service",
		"daily", $day->{$key}->{"HOST"}->{$host}->{"SERVICES"}->{$service}->{"CRITICAL COUNT"}
	    );
	    update_db(
		$key,
		"nagios warnings",
		"hostservice:$host / $service",
		"daily", $day->{$key}->{"HOST"}->{$host}->{"SERVICES"}->{$service}->{"WARNING COUNT"}
	    );
	    update_db(
		$key,
		"nagios notifications",
		"hostservice:$host / $service",
		"daily", $day->{$key}->{"HOST"}->{$host}->{"SERVICES"}->{$service}->{"NOTIFICATION COUNT"}
	    );
	    $managed_hostservices++;
	}
	$managed_hosts++;
    }
    foreach my $service ( keys %{ $day->{$key}->{"SERVICE"} } ) {
	update_db( $key, "nagios alerts",        "service:$service", "daily", $day->{$key}->{"SERVICE"}->{$service}->{"CRITICAL COUNT"} );
	update_db( $key, "nagios warnings",      "service:$service", "daily", $day->{$key}->{"SERVICE"}->{$service}->{"WARNING COUNT"} );
	update_db( $key, "nagios notifications", "service:$service", "daily", $day->{$key}->{"SERVICE"}->{$service}->{"NOTIFICATION COUNT"} );
    }

    # $day->{$datet}->{"CONTACT"}->{$contact}->{"NOTIFICATION COUNT"}->{$severity}++;
    foreach my $contact ( keys %{ $day->{$key}->{"CONTACT"} } ) {
	foreach my $severity ( keys %{ $day->{$key}->{"CONTACT"}->{$contact}->{"NOTIFICATION COUNT"} } ) {
	    update_db( $key, "nagios notifications $severity",
		"contact:$contact", "daily", $day->{$key}->{"CONTACT"}->{$contact}->{"NOTIFICATION COUNT"}->{$severity} );
	}
    }
    foreach my $cg ( keys %{ $day->{$key}->{"CONTACT GROUP"} } ) {
	foreach my $severity ( keys %{ $day->{$key}->{"CONTACT GROUP"}->{$cg}->{"NOTIFICATION COUNT"} } ) {
	    update_db( $key, "nagios notifications $severity",
		"contactgroup:$cg", "daily", $day->{$key}->{"CONTACT GROUP"}->{$cg}->{"NOTIFICATION COUNT"}->{$severity} );
	}
    }

    # $day->{$datet}->{"NOTIFY COMMAND"}->{$notifyscript}->{"NOTIFICATION COUNT"}++;
    foreach my $notifyscript ( keys %{ $day->{$key}->{"NOTIFY COMMAND"} } ) {
	update_db(
	    $key,
	    "nagios notifications",
	    "notify_command:$notifyscript",
	    "daily", $day->{$key}->{"NOTIFY COMMAND"}->{$notifyscript}->{"NOTIFICATION COUNT"}
	);
    }

    update_db( $key, "nagios managed hostgroups",   "all", "daily", $managed_hostgroups );
    update_db( $key, "nagios managed hosts",        "all", "daily", $managed_hosts );
    update_db( $key, "nagios managed hostservices", "all", "daily", $managed_hostservices );
}

$dbh->disconnect;
print LOG "Log records read:$logrecordcount.  Processed: $logprocesscount\n";

( $sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $isdst ) = localtime(time);
$year += 1900;
$month = (qw(January February March April May June July August September October November December))[$mon];
$timestring = sprintf "%02d:%02d:%02d", $hour, $min, $sec;
$thisday = (qw( Sunday Monday Tuesday Wednesday Thursday Friday Saturday ))[$wday];
print LOG "Dashboard database load finished at $thisday, $month $mday, $year. $timestring.\n";
print LOG "*******************************************************************************************************\n";
exit;

sub check_notnull {
    my $tmp = shift;
    if ($tmp) {
	return $tmp;
    }
    else {
	return 0;
    }
}

sub update_db {
    my $key           = shift;
    my $name          = shift;
    my $component     = shift;
    my $time_interval = shift;
    my $object        = shift;
    my $tmp = check_notnull($object);

    return if $tmp == 0;

    my $sql_string = "";
    my $qstring =
      "SELECT * FROM measurements WHERE timestamp='$key' and name='$name' and component='$component' and time_interval='$time_interval' ";
    my $sth = $dbh->prepare($qstring);
    $sth->execute();
    while ( my $ref = $sth->fetchrow_hashref() ) {
	$sql_string =
	    "UPDATE measurements SET "
	  . "measurement=$tmp"
	  . " WHERE (timestamp='$key' and name='$name' and component='$component' and time_interval='$time_interval')";
    }
    if ( !$sql_string ) {
	$sql_string = "INSERT INTO measurements (timestamp,name,component,time_interval,measurement) "
	  . "VALUES('$key','$name','$component','$time_interval',$tmp)";
    }
    $sth->finish();

    $sth = $dbh->prepare($sql_string);
    if ($sth->execute()) {
	print LOG "SQL command OK: $sql_string\n";
    }
    else {
	my $errstr = $DBI::errstr;
	chomp $errstr;
	print LOG "ERROR: Can't execute $sql_string.\nError: $errstr\n";
    }
    $sth->finish();

    return;
}

sub gettime {
    my $logtime = shift;
    ( $sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $isdst ) = localtime($logtime);
    $year  = $year + 1900;
    $mon   = $mon + 1;
    my $timet = sprintf "%04d-%02d-%02d %02d:%02d:%02d", $year, $mon, $mday, $hour, $min, $sec;
    my $datet = sprintf "%04d-%02d-%02d", $year, $mon, $mday;
    return ( $timet, $datet );
}

sub read_hostgroupconfig {
    my $nagiosconfigfile = shift;
    my $hostgroup_ref    = undef;
    my $host_ref         = undef;

    my $hg = '';
    my @members = ();
    open( NAGIOSCFG, $nagiosconfigfile ) or die "Can't open file $nagiosconfigfile: $!";
    my %configfiles = ();
    while ( my $line = <NAGIOSCFG> ) {
	chomp $line;
	if ( $line =~ /cfg_file=(\S+)/ ) {
	    $configfiles{$1} = $1;
	}
    }
    close NAGIOSCFG;
    foreach my $file ( keys %configfiles ) {
	## do something with "$dirname/$file"
	if ( !open( CONFIG, "$file" ) ) {
	    print LOG "Can't open host group configuration file $file\n";
	    next;
	}
	print LOG "Opening file $file\n";
	my $define_open = 0;
	while ( my $line = <CONFIG> ) {
	    ## print LOG "Processing line: $line";
	    chomp $line;
	    if ( ( $line =~ /define\s+hostgroup\s*\{/i ) and ( $define_open == 0 ) ) {
		$define_open = 1;
	    }
	    elsif ( ( $line =~ /define\s+hostgroup\s*\{/i ) and ( $define_open == 1 ) ) {
		print LOG "Invalid host group configuration file $file\n";
		return "ERROR";
	    }
	    if ( ( $line =~ /hostgroup_name\s+(\S+)/i ) and ( $define_open == 1 ) ) {
		$hg = $1;
	    }
	    if ( ( $line =~ /members\s+(.*)/i ) and ( $define_open == 1 ) ) {
		## @members = split /[\s\,]+/,$1;
		@members = splitmembers($1);
	    }
	    if ( ( $line =~ /\}/ ) and ( $define_open == 1 ) ) {
		$define_open = 0;
		foreach my $host (@members) {
		    $hostgroup_ref->{$hg}->{"HOSTS"}->{$host}  = 1;
		    $host_ref->{$host}->{"HOST GROUPS"}->{$hg} = 1;
		}
	    }
	}
	close CONFIG;
    }

    return ( $hostgroup_ref, $host_ref );
}

sub read_contactgroupconfig {
    my $nagiosconfigfile = shift;
    my $contactgroup_ref = undef;
    my $contact_ref      = undef;
    my $cg               = '';
    my @members          = ();
    open( NAGIOSCFG, $nagiosconfigfile ) or die "Can't open file $nagiosconfigfile: $!";
    my %configfiles = ();
    while ( my $line = <NAGIOSCFG> ) {
	chomp $line;
	if ( $line =~ /cfg_file=(\S+)/ ) {
	    $configfiles{$1} = $1;
	}
    }
    close NAGIOSCFG;
    foreach my $file ( keys %configfiles ) {
	if ( !open( CONFIG, $file ) ) {
	    print "Can't open contact group configuration file $file\n";
	    print LOG "Can't open contact group configuration file $file\n";
	    return "ERROR";
	}
	my $define_open = 0;
	while ( my $line = <CONFIG> ) {
	    if ( ( $line =~ /define\s+contactgroup\s*\{/i ) and ( $define_open == 0 ) ) {
		$define_open = 1;
	    }
	    elsif ( ( $line =~ /define\s+contactgroup\s*\{/i ) and ( $define_open == 1 ) ) {
		print "Invalid contact group configuration file $file\n";
		print LOG "Invalid contact group configuration file $file\n";
		return "ERROR";
	    }
	    if ( ( $line =~ /contactgroup_name\s+(\S+)/i ) and ( $define_open == 1 ) ) {
		$cg = $1;
	    }
	    if ( ( $line =~ /members\s+(.*)/i ) and ( $define_open == 1 ) ) {
		## @members = split /[ ,]+/,$1;  # Doesn't work due to Perl/RH split loop bug. Replace with the following.
		@members = splitmembers($1);
	    }
	    if ( ( $line =~ /\}/ ) and ( $define_open == 1 ) ) {
		$define_open = 0;
		foreach my $contact (@members) {
		    $contactgroup_ref->{$cg}->{"CONTACTS"}->{$contact}  = 1;
		    $contact_ref->{$contact}->{"CONTACT GROUPS"}->{$cg} = 1;
		}
	    }
	}
    }
    return ( $contactgroup_ref, $contact_ref );
}

sub splitmembers {
    my @members   = ();
    my $tmpstring = shift;
    while ( $tmpstring =~ /\s*(\S+?)[\s,]+(.*)?/ ) {
	push @members, $1;
	## print LOG "Adding $1 to members\n";
	$tmpstring = $2;
    }
    if ( $tmpstring =~ /(\S+)\s*?/ ) {
	push @members, $1;
	## print LOG "Adding $1 to members\n";
    }
    return @members;
}

sub readNagiosReportsConfig {
    my $configfile   = shift;
    my $config_ref   = undef;
    my @config_parms = qw(dbusername dbpassword dbname dbhost dbtype
      graphdirectory graphhtmlref
      nagios_cfg_file nagios_event_log dashboard_data_log dashboard_data_debug
      dashboard_lwp_debug nagios_server_address nagios_realm nagios_user nagios_password
      dashboard_lwp_log
    );
    open( CONFIG, "$configfile" ) or die "ERROR: Unable to find configuration file $configfile";

    while ( my $line = <CONFIG> ) {
	chomp $line;
	if ( $line =~ /^\s*(\S+)\s*=\s*(.*?)\s*$/ ) {
	    my $var   = $1;
	    my $value = $2;
	    chomp $value;
	    foreach my $parm (@config_parms) {
		if ( $var eq $parm ) {
		    $config_ref->{$parm} = $value;
		}
	    }
	}
    }
    close CONFIG;
    return $config_ref;
}

sub time_text {
    my $timestamp = shift;
    if ( $timestamp <= 0 ) {
	return "0";
    }
    else {
	my ( $seconds, $minutes, $hours, $day_of_month, $month, $year, $wday, $yday, $isdst ) = localtime($timestamp);
	return sprintf "%02d-%02d-%02d %02d:%02d:%02d", $year + 1900, $month + 1, $day_of_month, $hours, $minutes, $seconds;
    }
}

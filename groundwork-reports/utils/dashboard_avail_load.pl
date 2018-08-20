#!/usr/local/groundwork/perl/bin/perl --
#
# dashboard_avail_load.pl
#
# v1.23  2015-03-02  Fix for JIRA GWMON-7849 -- host availability reports display wrong values March 18, 2010
# v1.24  2016-09-07  Fix for JIRA GWMON-12707 -- use a full day, to match avail.cgi expectations; support multiple days in one run
# v1.25  2016-10-09  Suppress logging of credentials.
# v1.26  2016-10-27  Improve logging of database errors.
#
# Copyright 2010-2016 GroundWork Open Source, Inc. ("GroundWork")
# All rights reserved. This program is free software; you can redistribute it
# and/or modify it under the terms of the GNU General Public License version 2
# as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with
# this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
# Street, Fifth Floor, Boston, MA 02110-1301, USA.
#

use strict;

use DBI;
use Time::Local;
use Getopt::Std;
use URI;

# use LWP::UserAgent;
# use HTML::TreeBuilder 3;  # make sure our version isn't ancient

my $helpstring = "
This script will load the SQL dashboard database to enable the Groundwork
Insight Reports.  The Nagios availability reports cgi report will be executed.
This script will read the response and load the results in the database.
The settings program are defined in the configuration file gwir.cfg.
The detault location is /usr/local/groundwork/core/reports/etc/gwir.cfg.
You can specify an alternate config file with the -c flag.  The following
options will override the configuration file settings.

Options:
    -s <YYYYMMDD>	Start day (Default to yesterday, 00 hours).
    -e <YYYYMMDD>	End day (Default to yesterday, 24 hours).
    -c <filename>	Configuration file location.
    -L <OUTPUT LOG>	Log file containing status messages from this program.
    -d			Debug mode. Will log additional messages to the log file.
    -h			Displays help message.

Copyright 2003-2016 Groundwork Open Source, Inc.
http://www.groundworkopensource.com
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
";

my $Logfile;    # Log file for this program. Use for debug
my $debug = 0;  # Set to 1 for debug mode.
my $nagios_ipaddr;
my %opt = ();    # Program options hash
if ( not getopts( "s:e:c:L:n:dh", \%opt ) ) {
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
my $logfile    = $config_ref->{dashboard_avail_log};
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
if   ( $opt{d} ) { $debug = 1; }
else             { $debug = $config_ref->{dashboard_avail_debug} }
if   ( $opt{L} ) { $Logfile = $opt{L}; }
else             { $Logfile = $config_ref->{dashboard_avail_log}; }

my $nagioscgi_dir;    # Directory containing Nagios CGI programs
if   ( $opt{n} ) { $nagioscgi_dir = $opt{n}; }
else             { $nagioscgi_dir = $config_ref->{nagios_cgi_dir}; }

my $startdate = '';
if ( $opt{s} ) {
    if ( $opt{s} =~ /(\d{4})(\d{2})(\d{2})/ ) {
	$startdate = $opt{s};
    }
    else {
	die "Invalid start date $opt{s}\n";
    }
}
else {
    my ( $sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $isdst ) = localtime( time - ( 24 * 60 * 60 ) );    # Compute yesterday's date stamp
    $startdate = sprintf "%04d%02d%02d", $year + 1900, $mon + 1, $mday;
}
my $enddate = '';
if ( $opt{e} ) {
    if ( $opt{e} =~ /(\d{4})(\d{2})(\d{2})/ ) {
	$enddate = $opt{e};
    }
    else {
	die "Invalid end date $opt{e}\n";
    }
    if ( $enddate < $startdate ) {
	die "Invalid dates; end date $enddate earlier than start date $startdate.\n";
    }
}
else {
    $enddate = $startdate;
}

my $dbname      = $config_ref->{dbname};
my $dbhost      = $config_ref->{dbhost};
my $dbuser      = $config_ref->{dbusername};
my $dbpass      = $config_ref->{dbpassword};
my $dbtype      = $config_ref->{dbtype};
my $nagios_user = $config_ref->{nagios_user};

#open(LOG,'>>', "$Logfile");	# Append to logfile
open( LOG, '>', "$Logfile" );   # Overwrite logfile
my ( $sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $isdst ) = localtime(time);
$year += 1900;
my $month = (qw(January February March April May June July August September October November December))[$mon];
my $timestring = sprintf "%02d:%02d:%02d", $hour, $min, $sec;
my $thisday = (qw( Sunday Monday Tuesday Wednesday Thursday Friday Saturday ))[$wday];
print LOG "Dashboard database load starting at $thisday, $month $mday, $year. $timestring.\n";

print LOG "Output log: $Logfile\n";
print LOG "Nagios server IP address: \"$nagios_ipaddr\".\n";

## We suppress logging of credentials, as that would constitute a security hole.
# print LOG "Nagios security parameters: realm=$nagios_realm, user=$nagios_user, password=$nagios_password\n";
# print LOG "Nagios security parameters: user=$nagios_user\n";
print LOG "Report days: start=$startdate, end=$enddate\n";

my $statusgrid_cgi  = $nagioscgi_dir . "/status.cgi";
my $availreport_cgi = $nagioscgi_dir . "/avail.cgi";
my $cmd = "export REQUEST_METHOD=GET;export QUERY_STRING=\"hostgroup=all&style=grid\";export REMOTE_USER=$nagios_user;$statusgrid_cgi";
print LOG "Getting Hostgroup/Host/Service info from status grid page using command: $cmd\n";
my @lines      = `$cmd`;
my $hg_ref     = {};
my $current_hg = undef;
foreach my $line (@lines) {
    print LOG "LINE: $line" if $debug;
    while ( $line =~ s/a href=["']status.cgi\?hostgroup=(.*?)\&//i ) {
	print LOG "Host Group=$1\n" if ($debug);
	if ( $1 ne 'all' ) {
	    $current_hg = unencode($1);
	    $hg_ref->{$current_hg}->{NAME} = $current_hg;
	}
    }
    while ( $line =~ s/a href=["']status.cgi\?host=(.*?)["']//i ) {
	print LOG "Host=$1\n" if ($debug);
	my $host = unencode($1);
	if ($current_hg) {
	    ## FIX MINOR:  $host was already unencoded when it was assigned; does it make sense to unencode a second time?
	    $hg_ref->{$current_hg}->{HOST}->{$host}->{NAME} = unencode($host);
	}
    }
    while ( $line =~ s/href=["']extinfo.cgi\?type=\d+\&host=(.*?)\&service=(.*?)["']//i ) {
	print LOG "Host=$1, Service=$2\n" if ($debug);
	my $host    = unencode($1);
	my $service = unencode($2);
	if ($current_hg) {
	    $hg_ref->{$current_hg}->{HOST}->{$host}->{NAME} = $host;
	    $hg_ref->{$current_hg}->{HOST}->{$host}->{SERVICE}->{$service}->{NAME} = $service;
	}
    }
}

if ($debug) {
    foreach my $hg ( sort keys %$hg_ref ) {
	print LOG "Host Group=$hg\n";
	foreach my $host ( sort keys %{ $hg_ref->{$current_hg}->{HOST} } ) {
	    print LOG "\tHost=$host\n";
	    foreach my $service ( sort keys %{ $hg_ref->{$current_hg}->{HOST}->{$host}->{SERVICE} } ) {
		print LOG "\t\tService=$service\n";
	    }
	}
    }
}

# Get host avail by looking at the host availability report
# http://192.168.19.128/nagios/cgi-bin/avail.cgi?t1=1097161620&t2=1097766420&show_log_entries=&host=all&assumeinitialstates=yes&assumestateretention=yes&initialassumedstate=3&backtrack=4&timeperiod=custom
my @getdates = ();
$startdate =~ /(\d{4})(\d{2})(\d{2})/;
my $tmputc = timelocal( "00", "00", "00", $3, $2 - 1, $1 - 1900 );    #	$TIME = timelocal($sec, $min, $hours, $mday, $mon, $year);
$enddate =~ /(\d{4})(\d{2})(\d{2})/;
my $end_interval_utc = timelocal( "59", "59", "23", $3, $2 - 1, $1 - 1900 );
while ( $tmputc < $end_interval_utc ) {
    ( $sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $isdst ) = localtime($tmputc);
    $timestring = sprintf "%04d%02d%02d", $year + 1900, $mon + 1, $mday;
    push @getdates, $timestring;
    print LOG "Pushing $timestring\n";
    $tmputc += 24 * 60 * 60;                                       #	Calculate next day's start time
}

my $date_ref = {};
foreach my $date ( sort @getdates ) {
    $date =~ /(\d{4})(\d{2})(\d{2})/;
    my $startutc = timelocal( "00", "00", "00", $3, $2 - 1, $1 - 1900 );
    my $endutc = $startutc + ( 24 * 60 * 60 );
    print LOG "Processing date $date. start_utc=$startutc, end_utc=$endutc\n";
    print "Processing date $date. start_utc=$startutc, end_utc=$endutc\n";
    my $query_string =
        'show_log_entries=&'
      . 'host=all&'
      . "t1=$startutc&"
      . "t2=$endutc&"
      . 'assumeinitialstates=yes&'
      . 'assumestateretention=yes&'
      . 'initialassumedstate=3&'
      . 'backtrack=4&'
      . 'timeperiod=custom&'
      . 'csvoutput=';

    my $cmd = "export REQUEST_METHOD=GET;export QUERY_STRING=\"$query_string\";export REMOTE_USER=$nagios_user;$availreport_cgi";
    print LOG "Getting Hostgroup/Host/Service info from availability report page using command: $cmd\n";
    @lines = `$cmd`;
    my $i = 0;
    while ( $i <= $#lines ) {
	if ( $lines[$i] =~ /HOST_NAME/ ) {
	    last;
	}
	$i++;
    }
    chomp $lines[$i];
    print LOG "keys=" . $lines[$i] . "\n" if ($debug);
    my @keys = split /\s*,\s*/, $lines[$i];
    my @values = ();
    my $j;
    for ( $i++ ; $i <= $#lines ; $i++ ) {
	chomp $lines[$i];
	print LOG "values=" . $lines[$i] . "\n" if ($debug);
	@values = split /\s*,\s*/, $lines[$i];
	$values[0] =~ s/"//g;    #	strip "s from host name field
	for ( $j = 1 ; $j <= $#keys ; $j++ ) {
	    $values[$j] =~ s/%//g;    #	strip %s from percent values
	    $date_ref->{$date}->{HOST}->{ $values[0] }->{PARAMETER}->{ $keys[$j] } = $values[$j];
	}
    }

    # Get service avail by looking at the services availability report
    # http://192.168.19.128/nagios/cgi-bin/avail.cgi?show_log_entries=&host=localhost&service=all&timeperiod=custom&smon=10&sday=1&syear=2004&shour=0&smin=0&ssec=0&emon=10&eday=14&eyear=2004&ehour=24&emin=0&esec=0&assumeinitialstates=yes&assumestateretention=yes&initialassumedstate=6&backtrack=4&csvoutput=
    $query_string =
        'show_log_entries=&'
      . 'host=all&'
      . 'service=all&'
      . "t1=$startutc&"
      . "t2=$endutc&"
      . 'assumeinitialstates=yes&'
      . 'assumestateretention=yes&'
      . 'initialassumedstate=6&'
      . 'backtrack=4&'
      . 'timeperiod=custom&'
      . 'csvoutput=';
    $cmd = "export REQUEST_METHOD=GET;export QUERY_STRING=\"$query_string\";export REMOTE_USER=$nagios_user;$availreport_cgi";
    print LOG "Getting Service availability from avail report page using command: $cmd\n";
    @lines = `$cmd`;
    $i     = 0;
    while ( $i <= $#lines ) {
	if ( $lines[$i] =~ /HOST_NAME/ ) {
	    last;
	}
	$i++;
    }
    chomp $lines[$i];
    print LOG "keys=" . $lines[$i] . "\n" if ($debug);
    @keys = split /\s*,\s*/, $lines[$i];
    @keys = grep( !/TRUE/, @keys );
    for ( $i++ ; $i <= $#lines ; $i++ ) {
	chomp $lines[$i];
	print LOG "values=" . $lines[$i] . "\n" if ($debug);
	@values = split /\s*,\s*/, $lines[$i];
	for ( $j = 2 ; $j <= $#keys ; $j++ ) {
	    $values[0]  =~ s/"//g;    #	strip "s
	    $values[1]  =~ s/"//g;    #	strip "s
	    $values[$j] =~ s/%//g;    #	strip %s from percent values
	    $date_ref->{$date}->{HOST}->{ $values[0] }->{SERVICE}->{ $values[1] }->{PARAMETER}->{ $keys[$j] } = $values[$j]
	      unless ( $keys[$j] =~ /TRUE/ );
	}
    }
}    # End date processing loop

# $debug = 1;
my $day;
if ($debug) {
    my $param;
    foreach $day ( sort keys %{$date_ref} ) {
	print LOG "date=$day\n";
	foreach my $host ( sort keys %{ $date_ref->{$day}->{HOST} } ) {
	    print LOG "host=$host\n";
	    foreach $param ( sort keys %{ $date_ref->{$day}->{HOST}->{$host}->{PARAMETER} } ) {
		print LOG "$day, $host, Host parameter: $param=" . $date_ref->{$day}->{HOST}->{$host}->{PARAMETER}->{$param} . "\n";
	    }
	    foreach my $service ( sort keys %{ $date_ref->{$day}->{HOST}->{$host}->{SERVICE} } ) {
		print LOG "service=$service\n";
		foreach $param ( sort keys %{ $date_ref->{$day}->{HOST}->{$host}->{SERVICE}->{$service}->{PARAMETER} } ) {
		    print LOG "$day, $host, $service Service parameter: $param="
		      . $date_ref->{$day}->{HOST}->{$host}->{SERVICE}->{$service}->{PARAMETER}->{$param} . "\n";
		}
	    }
	}
    }
}

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
    exit;
}
## We suppress logging of credentials, as that would constitute a security hole.
# print LOG "Connected OK to database $dbname with user $dbuser.\n";
print LOG "Connected OK to database $dbname.\n";

# Daily processing
my $processcount = 0;
foreach $day ( sort keys %{$date_ref} ) {
    print "Processing day=$day\n";

    #	Insert host and service data into the database
    foreach my $host ( sort keys %{ $date_ref->{$day}->{HOST} } ) {
	print LOG "day=$day, host=$host\n" if ($debug);
	update_db_avail( $day, "host_availability", "$host", "", "daily", \%{ $date_ref->{$day}->{HOST}->{$host}->{PARAMETER} } );
	$processcount++;
	foreach my $service ( sort keys %{ $date_ref->{$day}->{HOST}->{$host}->{SERVICE} } ) {
	    update_db_avail( $day, "service_availability", "$host", "$service", "daily",
		\%{ $date_ref->{$day}->{HOST}->{$host}->{SERVICE}->{$service}->{PARAMETER} } );
	    $processcount++;
	}
	if ( $processcount % 100 == 0 ) {
	    print "Updates processed=$processcount\n";
	}
    }

    #	Compute host group availability into the database
    #		first sum all values
    my $param;
    foreach my $hg ( sort keys %$hg_ref ) {
	foreach my $host ( sort keys %{ $hg_ref->{$hg}->{HOST} } ) {
	    foreach $param ( sort keys %{ $date_ref->{$day}->{HOST}->{$host}->{PARAMETER} } ) {
		## Sum all parameter values to compute avg
		$date_ref->{$day}->{"HOST GROUP"}->{$hg}->{"HOST PARAMETER"}->{SUM}->{$param} +=
		  $date_ref->{$day}->{HOST}->{$host}->{PARAMETER}->{$param};
		$date_ref->{$day}->{"HOST GROUP"}->{$hg}->{"HOST PARAMETER"}->{COUNT}->{$param}++;
	    }
	    foreach my $service ( sort keys %{ $hg_ref->{$hg}->{HOST}->{$host}->{SERVICE} } ) {
		foreach $param ( sort keys %{ $date_ref->{$day}->{HOST}->{$host}->{SERVICE}->{$service}->{PARAMETER} } ) {
		    ## Sum all parameter values to compute avg
		    $date_ref->{$day}->{"HOST GROUP"}->{$hg}->{"SERVICE PARAMETER"}->{SUM}->{$param} +=
		      $date_ref->{$day}->{HOST}->{$host}->{SERVICE}->{$service}->{PARAMETER}->{$param};
		    $date_ref->{$day}->{"HOST GROUP"}->{$hg}->{"SERVICE PARAMETER"}->{COUNT}->{$param}++;
		}
	    }
	}
    }

    # now divide to compute avg
    foreach my $hg ( sort keys %$hg_ref ) {
	foreach $param ( sort keys %{ $date_ref->{$day}->{"HOST GROUP"}->{$hg}->{"HOST PARAMETER"}->{SUM} } ) {
	    $date_ref->{$day}->{"HOST GROUP"}->{$hg}->{"HOST PARAMETER"}->{AVERAGE}->{$param} =
	      $date_ref->{$day}->{"HOST GROUP"}->{$hg}->{"HOST PARAMETER"}->{SUM}->{$param} /
	      $date_ref->{$day}->{"HOST GROUP"}->{$hg}->{"HOST PARAMETER"}->{COUNT}->{$param};
	}
	update_db_avail( $day, "hostgroup_host_availability", "$hg", "", "daily",
	    \%{ $date_ref->{$day}->{"HOST GROUP"}->{$hg}->{"HOST PARAMETER"}->{AVERAGE} } );
	foreach $param ( sort keys %{ $date_ref->{$day}->{"HOST GROUP"}->{$hg}->{"SERVICE PARAMETER"}->{SUM} } ) {
	    $date_ref->{$day}->{"HOST GROUP"}->{$hg}->{"SERVICE PARAMETER"}->{AVERAGE}->{$param} =
	      $date_ref->{$day}->{"HOST GROUP"}->{$hg}->{"SERVICE PARAMETER"}->{SUM}->{$param} /
	      $date_ref->{$day}->{"HOST GROUP"}->{$hg}->{"SERVICE PARAMETER"}->{COUNT}->{$param};
	}
	update_db_avail( $day, "hostgroup_service_availability",
	    "$hg", "", "daily", \%{ $date_ref->{$day}->{"HOST GROUP"}->{$hg}->{"SERVICE PARAMETER"}->{AVERAGE} } );
    }
}
$dbh->disconnect;
print LOG "Database updates: $processcount\n";

( $sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $isdst ) = localtime(time);
$year += 1900;
$month = (qw(January February March April May June July August September October November December))[$mon];
$timestring = sprintf "%02d:%02d:%02d", $hour, $min, $sec;
$thisday = (qw( Sunday Monday Tuesday Wednesday Thursday Friday Saturday ))[$wday];
print LOG "Dashboard database load finished at $thisday, $month $mday, $year. $timestring.\n";
print LOG "*******************************************************************************************************\n";
exit;

sub unencode {
    my $coded_string = shift;
    $coded_string =~ s/%([0-9a-f]{2})/pack("c",hex($1))/gie;
    return $coded_string;
}

sub update_db_avail {
    my $day           = shift;
    my $table         = shift;
    my $host          = shift;
    my $service       = shift;
    my $time_interval = shift;
    my $parameter_ref = shift;
    my $where_string;
    my @set_string     = ();
    my $insert_string1 = undef;
    my $insert_string2 = undef;

    if ( $table eq "host_availability" ) {    #   process Host parms
	$where_string   = " DATESTAMP='$day' and HOST_NAME='$host' and TIME_INTERVAL='$time_interval' ";
	$insert_string1 = "DATESTAMP,HOST_NAME,TIME_INTERVAL,";
	$insert_string2 = "'$day','$host','$time_interval',";
    }
    elsif ( $table eq "service_availability" ) {    #   process Service parms
	$where_string   = " DATESTAMP='$day' and HOST_NAME='$host' and SERVICE_NAME='$service' and TIME_INTERVAL='$time_interval' ";
	$insert_string1 = "DATESTAMP,HOST_NAME,SERVICE_NAME,TIME_INTERVAL,";
	$insert_string2 = "'$day','$host','$service','$time_interval',";
    }
    elsif ( $table =~ /^hostgroup_/ ) {             #   process hostgroup parms
	$where_string   = " DATESTAMP='$day' and HOSTGROUP_NAME='$host' and TIME_INTERVAL='$time_interval' ";
	$insert_string1 = "DATESTAMP,HOSTGROUP_NAME,TIME_INTERVAL,";
	$insert_string2 = "'$day','$host','$time_interval',";
    }
    else {
	print LOG "ERROR: Invalid table $table.\n";
	return "ERROR";
    }
    foreach my $param ( keys %{$parameter_ref} ) {
	push @set_string, " $param='" . $parameter_ref->{$param} . "'";
	$insert_string1 .= "$param,";
	$insert_string2 .= "'" . $parameter_ref->{$param} . "',";
    }
    $insert_string1 =~ s/,$//;
    $insert_string2 =~ s/,$//;
    my $qstring = "SELECT * FROM $table WHERE $where_string ";
    my $sth = $dbh->prepare($qstring);
    $sth->execute();
    my $sql_string = "";
    while ( my $ref = $sth->fetchrow_hashref() ) {
	$sql_string = "UPDATE $table SET " . join( ',', @set_string ) .    #  measurement=$tmp
	  " WHERE ($where_string)";
    }
    if ( !$sql_string ) {
	$sql_string = "INSERT INTO $table ($insert_string1) " . "VALUES($insert_string2)";
    }
    $sth->finish();

    $sth = $dbh->prepare($sql_string);
    if ($sth->execute()) {
	print LOG "SQL command: $sql_string\n";
	## print "SQL command OK: $sql_string\n";
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

sub check_notnull {
    my $tmp = shift;
    if   ($tmp) { return $tmp; }
    else        { return 0; }
}

sub readNagiosReportsConfig {
    my $configfile   = shift;
    my $config_ref   = undef;
    my @config_parms = qw(dbusername dbpassword dbname dbhost dbtype
      graphdirectory graphhtmlref
      nagios_cfg_file nagios_event_log dashboard_data_log dashboard_data_debug
      dashboard_lwp_debug nagios_server_address nagios_realm nagios_user nagios_password
      dashboard_avail_log nagios_server_port dashboard_avail_debug nagios_cgi_dir
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

__END__

# Service parameter: PERCENT_KNOWN_TIME_CRITICAL=0.000%
# Service parameter: PERCENT_KNOWN_TIME_CRITICAL_SCHEDULED=0.000%
# Service parameter: PERCENT_KNOWN_TIME_CRITICAL_UNSCHEDULED=0.000%
# Service parameter: PERCENT_KNOWN_TIME_OK=100.000%
# Service parameter: PERCENT_KNOWN_TIME_OK_SCHEDULED=0.000%
# Service parameter: PERCENT_KNOWN_TIME_OK_UNSCHEDULED=100.000%
# Service parameter: PERCENT_KNOWN_TIME_UNKNOWN=0.000%
# Service parameter: PERCENT_KNOWN_TIME_UNKNOWN_SCHEDULED=0.000%
# Service parameter: PERCENT_KNOWN_TIME_UNKNOWN_UNSCHEDULED=0.000%
# Service parameter: PERCENT_KNOWN_TIME_WARNING=0.000%
# Service parameter: PERCENT_KNOWN_TIME_WARNING_SCHEDULED=0.000%
# Service parameter: PERCENT_KNOWN_TIME_WARNING_UNSCHEDULED=0.000%
# Service parameter: PERCENT_TIME_CRITICAL_SCHEDULED=0.000%
# Service parameter: PERCENT_TIME_CRITICAL_UNSCHEDULED=0.000%
# Service parameter: PERCENT_TIME_OK_SCHEDULED=0.000%
# Service parameter: PERCENT_TIME_OK_UNSCHEDULED=48.922%
# Service parameter: PERCENT_TIME_UNDETERMINED_NOT_RUNNING=51.078%
# Service parameter: PERCENT_TIME_UNDETERMINED_NO_DATA=0.000%
# Service parameter: PERCENT_TIME_UNKNOWN_SCHEDULED=0.000%
# Service parameter: PERCENT_TIME_UNKNOWN_UNSCHEDULED=0.000%
# Service parameter: PERCENT_TIME_WARNING_SCHEDULED=0.000%
# Service parameter: PERCENT_TIME_WARNING_UNSCHEDULED=0.000%
# Service parameter: PERCENT_TOTAL_TIME_CRITICAL=0.000%
# Service parameter: PERCENT_TOTAL_TIME_OK=48.922%
# Service parameter: PERCENT_TOTAL_TIME_UNDETERMINED=51.078%
# Service parameter: PERCENT_TOTAL_TIME_UNKNOWN=0.000%
# Service parameter: PERCENT_TOTAL_TIME_WARNING=0.000%
# Service parameter: TIME_CRITICAL_SCHEDULED=0
# Service parameter: TIME_CRITICAL_UNSCHEDULED=0
# Service parameter: TIME_OK_SCHEDULED=0
# Service parameter: TIME_OK_UNSCHEDULED=42269
# Service parameter: TIME_UNDETERMINED_NOT_RUNNING=44131
# Service parameter: TIME_UNDETERMINED_NO_DATA=0
# Service parameter: TIME_UNKNOWN_SCHEDULED=0
# Service parameter: TIME_UNKNOWN_UNSCHEDULED=0
# Service parameter: TIME_WARNING_SCHEDULED=0
# Service parameter: TIME_WARNING_UNSCHEDULED=0
# Service parameter: TOTAL_TIME_CRITICAL=0
# Service parameter: TOTAL_TIME_OK=42269
# Service parameter: TOTAL_TIME_UNDETERMINED=44131
# Service parameter: TOTAL_TIME_UNKNOWN=0
# Service parameter: TOTAL_TIME_WARNING=0

# Host parameter: PERCENT_KNOWN_TIME_DOWN=0.000%
# Host parameter: PERCENT_KNOWN_TIME_DOWN_SCHEDULED=0.000%
# Host parameter: PERCENT_KNOWN_TIME_DOWN_UNSCHEDULED=0.000%
# Host parameter: PERCENT_KNOWN_TIME_UNREACHABLE=0.000%
# Host parameter: PERCENT_KNOWN_TIME_UNREACHABLE_SCHEDULED=0.000%
# Host parameter: PERCENT_KNOWN_TIME_UNREACHABLE_UNSCHEDULED=0.000%
# Host parameter: PERCENT_KNOWN_TIME_UP=100.000%
# Host parameter: PERCENT_KNOWN_TIME_UP_SCHEDULED=0.000%
# Host parameter: PERCENT_KNOWN_TIME_UP_UNSCHEDULED=100.000%
# Host parameter: PERCENT_TIME_DOWN_SCHEDULED=0.000%
# Host parameter: PERCENT_TIME_DOWN_UNSCHEDULED=0.000%
# Host parameter: PERCENT_TIME_UNDETERMINED_NOT_RUNNING=51.078%
# Host parameter: PERCENT_TIME_UNDETERMINED_NO_DATA=0.000%
# Host parameter: PERCENT_TIME_UNREACHABLE_SCHEDULED=0.000%
# Host parameter: PERCENT_TIME_UNREACHABLE_UNSCHEDULED=0.000%
# Host parameter: PERCENT_TIME_UP_SCHEDULED=0.000%
# Host parameter: PERCENT_TIME_UP_UNSCHEDULED=48.922%
# Host parameter: PERCENT_TOTAL_TIME_DOWN=0.000%
# Host parameter: PERCENT_TOTAL_TIME_UNDETERMINED=51.078%
# Host parameter: PERCENT_TOTAL_TIME_UNREACHABLE=0.000%
# Host parameter: PERCENT_TOTAL_TIME_UP=48.922%
# Host parameter: TIME_DOWN_SCHEDULED=0
# Host parameter: TIME_DOWN_UNSCHEDULED=0
# Host parameter: TIME_UNDETERMINED_NOT_RUNNING=44131
# Host parameter: TIME_UNDETERMINED_NO_DATA=0
# Host parameter: TIME_UNREACHABLE_SCHEDULED=0
# Host parameter: TIME_UNREACHABLE_UNSCHEDULED=0
# Host parameter: TIME_UP_SCHEDULED=0
# Host parameter: TIME_UP_UNSCHEDULED=42269
# Host parameter: TOTAL_TIME_DOWN=0
# Host parameter: TOTAL_TIME_UNDETERMINED=44131
# Host parameter: TOTAL_TIME_UNREACHABLE=0
# Host parameter: TOTAL_TIME_UP=42269
#!/usr/local/groundwork/perl/bin/perl --
#
# Copyright 2007-2018 GroundWork Open Source, Inc. ("GroundWork")
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

BEGIN {
    # This is just for debugging warnings during development testing.
    if (0) {
	print "Content-type: text/html\n\n";
	open (STDERR, '>>&STDOUT');
    }
}

use strict;

use DBI;
use Time::Local;
use lib qq(/usr/local/groundwork/core/reports/lib);
use GWIR_outage;
use MonarchStorProc;
use MonarchForms;

# These global variables are shared between this script and the 
# referenced GWIR_outage.pm library.  Ouch.  There has to be a better way.
our %FORM_DATA = ();
our $graphfile = '';
our $NoData    = "&mdash;";    # Set no data string
our $start_year;
our $start_month;
our $start_day;
our $end_year;
our $end_month;
our $end_day;
our $dbh;
our @Colors = qw(#8DD9E0 #64A2B8 #D3DB00 #8BA016 #C0C0C0 #818181 #9BAEFF #6F76C4 #E092E3 #C05599);

my $configfile = "/usr/local/groundwork/core/reports/etc/gwir.cfg";
my $config_ref = readNagiosReportsConfig($configfile);
my $logfile    = $config_ref->{dbusername};
my $dbname     = $config_ref->{dbname};
my $dbhost     = $config_ref->{dbhost};
my $dbuser     = $config_ref->{dbusername};
my $dbpass     = $config_ref->{dbpassword};
my $dbtype     = $config_ref->{dbtype};
our $graphdirectory = $config_ref->{graphdirectory};
our $graphhtmlref   = $config_ref->{graphhtmlref};

print "Content-type: text/html \n\n";
my $request_method = $ENV{'REQUEST_METHOD'};
my $form_info      = '';
if ( $request_method eq "GET" ) {
    $form_info = $ENV{'QUERY_STRING'};
##  $form_info =~ s/%([\dA-Fa-f][\dA-Fa-f])/pack("C",hex($1))/eg;
}
elsif ( $request_method eq "POST" ) {
    my $size_of_form_info = $ENV{'CONTENT_LENGTH'};
    read( STDIN, $form_info, $size_of_form_info );
}
else {
    print "500 Server Error. Server uses unsupported method";
    $ENV{'REQUEST_METHOD'} = "GET";
    $ENV{'QUERY_STRING'}   = $ARGV[0];
    $form_info             = $ARGV[0];
}
my @key_value_pairs = split( /&/, $form_info );
foreach my $key_value (@key_value_pairs) {
    my ( $key, $value ) = split( /=/, $key_value );
    $value =~ tr/+/ /;
    $value =~ s/%([\dA-Fa-f][\dA-Fa-f])/pack("C",hex($1))/eg;
    if ( defined( $FORM_DATA{$key} ) ) {
	$FORM_DATA{$key} = join( "\0", $FORM_DATA{$key}, $value );
    }
    else {
	$FORM_DATA{$key} = $value;
    }
}

my $stylesheethtmlref = '';
print qq(
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<HTML>
<HEAD>
<META HTTP-EQUIV='Expires' CONTENT='0'>
<META HTTP-EQUIV='Pragma' CONTENT='no-cache'>
<TITLE>Groundwork Insight Reports</TITLE>
<link rel='stylesheet' type='text/css' href='$stylesheethtmlref'>
);
printstyles();
print Forms->js_utils();
print "<style type='text/css'>
	select {border: 0px solid #000099; font-family: verdana, helvetica, arial, sans-serif; font-size: 12px; font-weight: normal; color: #000000;}
	</style>";
print "
	</HEAD>
	<BODY>
	<script type='text/javascript' language=JavaScript>
	    function toggle_detail() {
		var detail_checkbox = document.getElementsByName('showtable')[0];
		var detail_label    = document.getElementsByName('table_detail_label')[0];
		var detail_select   = document.getElementsByName('component')[0];
		var show_table = detail_checkbox.checked;
		detail_label.className  = detail_label.className .replace(/(_disabled)?\$/, show_table ? '' : '_disabled');
		detail_select.className = detail_select.className.replace(/(_disabled)?\$/, show_table ? '' : '_disabled');
		detail_select.disabled = show_table ? false : true;
	    }
	</script>
	<DIV id=container>
";

my ( $sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $isdst ) = localtime(time);
$year += 1900;
my $month = qw(January February March April May June July August September October November December) [$mon];
my $timestring = sprintf "%02d:%02d:%02d", $hour, $min, $sec;
my $thisday = qw( Sunday Monday Tuesday Wednesday Thursday Friday Saturday ) [$wday];
print "<FORM action=nagios_outages1.pl method=get>";
print "<table class='data' border='0' cellpadding='5' cellspacing='2' width='100%'>
    <TBODY><tr class='tableHeaderPage'>
";
print "<td>Outages Report &mdash; $thisday, $month $mday, $year at $timestring</td></TR>";

my $dsn = '';
if ( defined($dbtype) && $dbtype eq 'postgresql' ) {
    $dsn = "DBI:Pg:dbname=$dbname;host=$dbhost";
}
else {
    $dsn = "DBI:mysql:database=$dbname;host=$dbhost";
}
$dbh = DBI->connect( $dsn, $dbuser, $dbpass, { 'AutoCommit' => 1 } ) or die "Can't connect to database $dbname. Error: " . $DBI::errstr;

#	Set form defaults
if ( !$form_info ) {
    $FORM_DATA{showtable}           = 1;
    $FORM_DATA{showchart}           = 1;
    $FORM_DATA{showtophostgroups}   = 1;
    $FORM_DATA{showtophosts}        = 1;
    $FORM_DATA{showtophostservices} = 1;
    $FORM_DATA{showtopservices}     = 1;
}
$mon++;
# $thisdate = sprintf "%04d-%02d-%02d", $year, $mon, $mday;
if ( defined( $FORM_DATA{start_date} ) && $FORM_DATA{start_date} =~ /(\d\d\d\d)-(\d\d)-(\d\d)/ ) {
    $FORM_DATA{start_year}  = $1;
    $FORM_DATA{start_month} = $2;
    $FORM_DATA{start_day}   = $3;
}
else {
    if ( !$FORM_DATA{start_month} ) {
	( $sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $isdst ) =
	  localtime( time - ( 30 * 24 * 60 * 60 ) );    # default start is 30 days ago
	$mon++;
	$year += 1900;
    }
    if ( !$FORM_DATA{start_day} ) {
	$FORM_DATA{start_day} = sprintf "%02d", $mday;
    }
    if ( !$FORM_DATA{start_month} ) {
	$FORM_DATA{start_month} = sprintf "%02d", $mon;
    }
    if ( !$FORM_DATA{start_year} ) {
	$FORM_DATA{start_year} = $year;
    }
}
if ( defined( $FORM_DATA{end_date} ) && $FORM_DATA{end_date} =~ /(\d\d\d\d)-(\d\d)-(\d\d)/ ) {
    $FORM_DATA{end_year}  = $1;
    $FORM_DATA{end_month} = $2;
    $FORM_DATA{end_day}   = $3;
}
else {
    my ( $sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $isdst ) = localtime(time);    # end is current time
    $year += 1900;
    $mon++;
    if ( !$FORM_DATA{end_month} ) {
	$FORM_DATA{end_month} = sprintf "%02d", $mon;
    }
    if ( !$FORM_DATA{end_day} ) {
	$FORM_DATA{end_day} = sprintf "%02d", $mday;
    }
    if ( !$FORM_DATA{end_year} ) {
	$FORM_DATA{end_year} = $year;
    }
}
my %checked = ();
$checked{$_} = '' for '01'..'12';
$checked{ $FORM_DATA{start_month} } = "SELECTED";
print "
    <TR>
    <TD class=tableFill03>
	<span class='header'>Start:&nbsp;</span>
	<SELECT class=insight name=start_month>
	<OPTION class=small value='01' $checked{'01'}>Jan
	<OPTION class=small value='02' $checked{'02'}>Feb
	<OPTION class=small value='03' $checked{'03'}>Mar
	<OPTION class=small value='04' $checked{'04'}>Apr
	<OPTION class=small value='05' $checked{'05'}>May
	<OPTION class=small value='06' $checked{'06'}>Jun
	<OPTION class=small value='07' $checked{'07'}>Jul
	<OPTION class=small value='08' $checked{'08'}>Aug
	<OPTION class=small value='09' $checked{'09'}>Sep
	<OPTION class=small value='10' $checked{'10'}>Oct
	<OPTION class=small value='11' $checked{'11'}>Nov
	<OPTION class=small value='12' $checked{'12'}>Dec
	</SELECT>
";
%checked = ();
$checked{$_} = '' for '01'..'31';
$checked{ $FORM_DATA{start_day} } = "SELECTED";
print "
	<SELECT class=insight name=start_day>
	<OPTION class=small value='01' $checked{'01'}>01
	<OPTION class=small value='02' $checked{'02'}>02
	<OPTION class=small value='03' $checked{'03'}>03
	<OPTION class=small value='04' $checked{'04'}>04
	<OPTION class=small value='05' $checked{'05'}>05
	<OPTION class=small value='06' $checked{'06'}>06
	<OPTION class=small value='07' $checked{'07'}>07
	<OPTION class=small value='08' $checked{'08'}>08
	<OPTION class=small value='09' $checked{'09'}>09
	<OPTION class=small value='10' $checked{'10'}>10
	<OPTION class=small value='11' $checked{'11'}>11
	<OPTION class=small value='12' $checked{'12'}>12
	<OPTION class=small value='13' $checked{'13'}>13
	<OPTION class=small value='14' $checked{'14'}>14
	<OPTION class=small value='15' $checked{'15'}>15
	<OPTION class=small value='16' $checked{'16'}>16
	<OPTION class=small value='17' $checked{'17'}>17
	<OPTION class=small value='18' $checked{'18'}>18
	<OPTION class=small value='19' $checked{'19'}>19
	<OPTION class=small value='20' $checked{'20'}>20
	<OPTION class=small value='21' $checked{'21'}>21
	<OPTION class=small value='22' $checked{'22'}>22
	<OPTION class=small value='23' $checked{'23'}>23
	<OPTION class=small value='24' $checked{'24'}>24
	<OPTION class=small value='25' $checked{'25'}>25
	<OPTION class=small value='26' $checked{'26'}>26
	<OPTION class=small value='27' $checked{'27'}>27
	<OPTION class=small value='28' $checked{'28'}>28
	<OPTION class=small value='29' $checked{'29'}>29
	<OPTION class=small value='30' $checked{'30'}>30
	<OPTION class=small value='31' $checked{'31'}>31
	</SELECT>
";
%checked = ();
$checked{$_} = '' for '2000'..'2020';
$checked{ $FORM_DATA{start_year} } = "SELECTED";
print "
	<SELECT class=insight name=start_year>
	<OPTION class=small value='2000' $checked{'2000'}>2000
	<OPTION class=small value='2001' $checked{'2001'}>2001
	<OPTION class=small value='2002' $checked{'2002'}>2002
	<OPTION class=small value='2003' $checked{'2003'}>2003
	<OPTION class=small value='2004' $checked{'2004'}>2004
	<OPTION class=small value='2005' $checked{'2005'}>2005
	<OPTION class=small value='2006' $checked{'2006'}>2006
	<OPTION class=small value='2007' $checked{'2007'}>2007
	<OPTION class=small value='2008' $checked{'2008'}>2008
	<OPTION class=small value='2009' $checked{'2009'}>2009
	<OPTION class=small value='2010' $checked{'2010'}>2010
	<OPTION class=small value='2011' $checked{'2011'}>2011
	<OPTION class=small value='2012' $checked{'2012'}>2012
	<OPTION class=small value='2013' $checked{'2013'}>2013
	<OPTION class=small value='2014' $checked{'2014'}>2014
	<OPTION class=small value='2015' $checked{'2015'}>2015
	<OPTION class=small value='2016' $checked{'2016'}>2016
	<OPTION class=small value='2017' $checked{'2017'}>2017
	<OPTION class=small value='2018' $checked{'2018'}>2018
	<OPTION class=small value='2019' $checked{'2019'}>2019
	<OPTION class=small value='2020' $checked{'2020'}>2020
	<OPTION class=small value='2021' $checked{'2021'}>2021
	<OPTION class=small value='2022' $checked{'2022'}>2022
	<OPTION class=small value='2023' $checked{'2023'}>2023
	<OPTION class=small value='2024' $checked{'2024'}>2024
	<OPTION class=small value='2025' $checked{'2025'}>2025
	</SELECT>
";
%checked = ();
$checked{$_} = '' for '01'..'12';
$checked{ $FORM_DATA{end_month} } = "SELECTED";
print "
	&nbsp;&nbsp;&nbsp;&nbsp;
	<span class='header'>End:&nbsp;</span>
	<SELECT class=insight name=end_month>
	<OPTION class=small value='01' $checked{'01'}>Jan
	<OPTION class=small value='02' $checked{'02'}>Feb
	<OPTION class=small value='03' $checked{'03'}>Mar
	<OPTION class=small value='04' $checked{'04'}>Apr
	<OPTION class=small value='05' $checked{'05'}>May
	<OPTION class=small value='06' $checked{'06'}>Jun
	<OPTION class=small value='07' $checked{'07'}>Jul
	<OPTION class=small value='08' $checked{'08'}>Aug
	<OPTION class=small value='09' $checked{'09'}>Sep
	<OPTION class=small value='10' $checked{'10'}>Oct
	<OPTION class=small value='11' $checked{'11'}>Nov
	<OPTION class=small value='12' $checked{'12'}>Dec
	</SELECT>
";
%checked = ();
$checked{$_} = '' for '01'..'31';
$checked{ $FORM_DATA{end_day} } = "SELECTED";
print "
	<SELECT class=insight name=end_day>
	<OPTION class=small value='01' $checked{'01'}>01
	<OPTION class=small value='02' $checked{'02'}>02
	<OPTION class=small value='03' $checked{'03'}>03
	<OPTION class=small value='04' $checked{'04'}>04
	<OPTION class=small value='05' $checked{'05'}>05
	<OPTION class=small value='06' $checked{'06'}>06
	<OPTION class=small value='07' $checked{'07'}>07
	<OPTION class=small value='08' $checked{'08'}>08
	<OPTION class=small value='09' $checked{'09'}>09
	<OPTION class=small value='10' $checked{'10'}>10
	<OPTION class=small value='11' $checked{'11'}>11
	<OPTION class=small value='12' $checked{'12'}>12
	<OPTION class=small value='13' $checked{'13'}>13
	<OPTION class=small value='14' $checked{'14'}>14
	<OPTION class=small value='15' $checked{'15'}>15
	<OPTION class=small value='16' $checked{'16'}>16
	<OPTION class=small value='17' $checked{'17'}>17
	<OPTION class=small value='18' $checked{'18'}>18
	<OPTION class=small value='19' $checked{'19'}>19
	<OPTION class=small value='20' $checked{'20'}>20
	<OPTION class=small value='21' $checked{'21'}>21
	<OPTION class=small value='22' $checked{'22'}>22
	<OPTION class=small value='23' $checked{'23'}>23
	<OPTION class=small value='24' $checked{'24'}>24
	<OPTION class=small value='25' $checked{'25'}>25
	<OPTION class=small value='26' $checked{'26'}>26
	<OPTION class=small value='27' $checked{'27'}>27
	<OPTION class=small value='28' $checked{'28'}>28
	<OPTION class=small value='29' $checked{'29'}>29
	<OPTION class=small value='30' $checked{'30'}>30
	<OPTION class=small value='31' $checked{'31'}>31
	</SELECT>
";
%checked = ();
$checked{$_} = '' for '2000'..'2020';
$checked{ $FORM_DATA{end_year} } = "SELECTED";
print "
	<SELECT class=insight name=end_year>
	<OPTION class=small value='2000' $checked{'2000'}>2000
	<OPTION class=small value='2001' $checked{'2001'}>2001
	<OPTION class=small value='2002' $checked{'2002'}>2002
	<OPTION class=small value='2003' $checked{'2003'}>2003
	<OPTION class=small value='2004' $checked{'2004'}>2004
	<OPTION class=small value='2005' $checked{'2005'}>2005
	<OPTION class=small value='2006' $checked{'2006'}>2006
	<OPTION class=small value='2007' $checked{'2007'}>2007
	<OPTION class=small value='2008' $checked{'2008'}>2008
	<OPTION class=small value='2009' $checked{'2009'}>2009
	<OPTION class=small value='2010' $checked{'2010'}>2010
	<OPTION class=small value='2011' $checked{'2011'}>2011
	<OPTION class=small value='2012' $checked{'2012'}>2012
	<OPTION class=small value='2013' $checked{'2013'}>2013
	<OPTION class=small value='2014' $checked{'2014'}>2014
	<OPTION class=small value='2015' $checked{'2015'}>2015
	<OPTION class=small value='2016' $checked{'2016'}>2016
	<OPTION class=small value='2017' $checked{'2017'}>2017
	<OPTION class=small value='2018' $checked{'2018'}>2018
	<OPTION class=small value='2019' $checked{'2019'}>2019
	<OPTION class=small value='2020' $checked{'2020'}>2020
	<OPTION class=small value='2021' $checked{'2021'}>2021
	<OPTION class=small value='2022' $checked{'2022'}>2022
	<OPTION class=small value='2023' $checked{'2023'}>2023
	<OPTION class=small value='2024' $checked{'2024'}>2024
	<OPTION class=small value='2025' $checked{'2025'}>2025
	</SELECT>
";
my @hosttypes = qw(
  PERCENT_KNOWN_TIME_DOWN
  PERCENT_KNOWN_TIME_DOWN_SCHEDULED
  PERCENT_KNOWN_TIME_DOWN_UNSCHEDULED
  PERCENT_KNOWN_TIME_UNREACHABLE
  PERCENT_KNOWN_TIME_UNREACHABLE_SCHEDULED
  PERCENT_KNOWN_TIME_UNREACHABLE_UNSCHEDULED
  PERCENT_KNOWN_TIME_UP
  PERCENT_KNOWN_TIME_UP_SCHEDULED
  PERCENT_KNOWN_TIME_UP_UNSCHEDULED
  PERCENT_TIME_DOWN_SCHEDULED
  PERCENT_TIME_DOWN_UNSCHEDULED
  PERCENT_TIME_UNDETERMINED_NOT_RUNNING
  PERCENT_TIME_UNDETERMINED_NO_DATA
  PERCENT_TIME_UNREACHABLE_SCHEDULED
  PERCENT_TIME_UNREACHABLE_UNSCHEDULED
  PERCENT_TIME_UP_SCHEDULED
  PERCENT_TIME_UP_UNSCHEDULED
  PERCENT_TOTAL_TIME_DOWN
  PERCENT_TOTAL_TIME_UNDETERMINED
  PERCENT_TOTAL_TIME_UNREACHABLE
  PERCENT_TOTAL_TIME_UP
  TIME_DOWN_SCHEDULED
  TIME_DOWN_UNSCHEDULED
  TIME_UNDETERMINED_NOT_RUNNING
  TIME_UNDETERMINED_NO_DATA
  TIME_UNREACHABLE_SCHEDULED
  TIME_UNREACHABLE_UNSCHEDULED
  TIME_UP_SCHEDULED
  TIME_UP_UNSCHEDULED
  TOTAL_TIME_DOWN
  TOTAL_TIME_UNDETERMINED
  TOTAL_TIME_UNREACHABLE
  TOTAL_TIME_UP
);
my @servicetypes = qw(
  PERCENT_KNOWN_TIME_CRITICAL
  PERCENT_KNOWN_TIME_CRITICAL_SCHEDULED
  PERCENT_KNOWN_TIME_CRITICAL_UNSCHEDULED
  PERCENT_KNOWN_TIME_OK
  PERCENT_KNOWN_TIME_OK_SCHEDULED
  PERCENT_KNOWN_TIME_OK_UNSCHEDULED
  PERCENT_KNOWN_TIME_UNKNOWN
  PERCENT_KNOWN_TIME_UNKNOWN_SCHEDULED
  PERCENT_KNOWN_TIME_UNKNOWN_UNSCHEDULED
  PERCENT_KNOWN_TIME_WARNING
  PERCENT_KNOWN_TIME_WARNING_SCHEDULED
  PERCENT_KNOWN_TIME_WARNING_UNSCHEDULED
  PERCENT_TIME_CRITICAL_SCHEDULED
  PERCENT_TIME_CRITICAL_UNSCHEDULED
  PERCENT_TIME_OK_SCHEDULED
  PERCENT_TIME_OK_UNSCHEDULED
  PERCENT_TIME_UNDETERMINED_NOT_RUNNING
  PERCENT_TIME_UNDETERMINED_NO_DATA
  PERCENT_TIME_UNKNOWN_SCHEDULED
  PERCENT_TIME_UNKNOWN_UNSCHEDULED
  PERCENT_TIME_WARNING_SCHEDULED
  PERCENT_TIME_WARNING_UNSCHEDULED
  PERCENT_TOTAL_TIME_CRITICAL
  PERCENT_TOTAL_TIME_OK
  PERCENT_TOTAL_TIME_UNDETERMINED
  PERCENT_TOTAL_TIME_UNKNOWN
  PERCENT_TOTAL_TIME_WARNING
  TIME_CRITICAL_SCHEDULED
  TIME_CRITICAL_UNSCHEDULED
  TIME_OK_SCHEDULED
  TIME_OK_UNSCHEDULED
  TIME_UNDETERMINED_NOT_RUNNING
  TIME_UNDETERMINED_NO_DATA
  TIME_UNKNOWN_SCHEDULED
  TIME_UNKNOWN_UNSCHEDULED
  TIME_WARNING_SCHEDULED
  TIME_WARNING_UNSCHEDULED
  TOTAL_TIME_CRITICAL
  TOTAL_TIME_OK
  TOTAL_TIME_UNDETERMINED
  TOTAL_TIME_UNKNOWN
  TOTAL_TIME_WARNING
);
# FIX MAJOR:  Some values are shared between @hosttypes and @servicetypes,
# which makes it inappropriate to use the same %checked hash for both purposes.
@checked{@hosttypes} = ('') x @hosttypes;
@checked{@servicetypes} = ('') x @servicetypes;
$checked{$_} = '' for qw( daily weekly monthly yearly all hostgroup host hostservice service );
if ( !$FORM_DATA{interval} ) { $FORM_DATA{interval} = "weekly"; }
$checked{ $FORM_DATA{interval} } = "SELECTED";
if ( !$FORM_DATA{component} ) { $FORM_DATA{component} = "all"; }
$checked{ $FORM_DATA{component} } = "SELECTED";
if ( !$FORM_DATA{hosttype} ) { $FORM_DATA{hosttype} = "PERCENT_TIME_DOWN_UNSCHEDULED"; }
$checked{ $FORM_DATA{hosttype} } = "SELECTED";
if ( !$FORM_DATA{servicetype} ) { $FORM_DATA{servicetype} = "PERCENT_TIME_CRITICAL_UNSCHEDULED"; }
$checked{ $FORM_DATA{servicetype} } = "SELECTED";
print "
	&nbsp;&nbsp;&nbsp;&nbsp;
    <span class='header'>Reporting Interval:&nbsp;</span>
	<SELECT class=insight name=interval>
	<OPTION class=small value=daily $checked{daily}>Daily
	<OPTION class=small value=weekly $checked{weekly}>Weekly
	<OPTION class=small value=monthly $checked{monthly}>Monthly
	<OPTION class=small value=yearly $checked{yearly}>Yearly</OPTION>
	</SELECT>
	</TD>
	</TR>
	";

$checked{1} = "CHECKED";

my $help_url = StorProc->doc_section_url('Insight+Reports', 'InsightReports-OutageReports');
my $detail_label_class  = $FORM_DATA{showtable} ? 'select_label' : 'select_label_disabled';
my $detail_select_class = $FORM_DATA{showtable} ? 'insight'      : 'insight_disabled';
print "
<TR>
    <TD class=tableFill03><span class='header'>Show:</span>
	&nbsp;&nbsp;&nbsp;&nbsp;<INPUT type=checkbox " . $checked{ $FORM_DATA{showtable} } . " value=1 name=showtable onclick='toggle_detail();'>&thinsp;Table
	&nbsp;&nbsp;&nbsp;&nbsp;<span class=$detail_label_class name='table_detail_label'>Table Detail Level:</span>&nbsp;
	<SELECT class=$detail_select_class name=component>
	<OPTION class=small value=all $checked{all}>All
	<OPTION class=small value=hostgroup $checked{hostgroup}>Host Group
	<OPTION class=small value=host $checked{host}>Host
	<OPTION class=small value=hostservice $checked{hostservice}>Host-Service
	<OPTION class=small value=service $checked{service}>Service
	</OPTION>
	</SELECT>
	&nbsp;&nbsp;&nbsp;&nbsp;<INPUT type=checkbox " . $checked{ $FORM_DATA{showchart} } . " value=1 name=showchart>&thinsp;Totals Chart
";

#	Detail<INPUT type=checkbox ". $checked{$FORM_DATA{showdetail}}." value=1 name=showdetail>
print "</TD></TR>
  <TR>
    <td class=tableFill03><span class='header'>Host Measurement Type:</span>
	&nbsp;&nbsp;&nbsp;&nbsp;
	<SELECT class=insight name=hosttype>
";

foreach my $tmp ( sort @hosttypes ) {
    print "<OPTION class=small value=$tmp $checked{$tmp}>$tmp";
}
print "
	</OPTION>
	</SELECT>
	&nbsp; Used for host group and host measurements.
	</TD>
  </TR>
  <tr>
	<td class=tableFill03><span class='header'>Service Measurement Type:</span>
	&nbsp;&nbsp;&nbsp;&nbsp;
	<SELECT class=insight name=servicetype>
";
foreach my $tmp ( sort @servicetypes ) {
    print "<OPTION class=small value=$tmp $checked{$tmp}>$tmp";
}
print "
	</OPTION>
	</SELECT>
	&nbsp; Used for host/service and service measurements.
	</TD>
  </TR>
  <TR>
    <TD class=tableFill03><span class='header'>Show Top Measurements by:</span>
	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<INPUT class=insight type=checkbox value=1 " . $checked{ $FORM_DATA{showtophostgroups} } . " name=showtophostgroups>&thinsp;Host Group
	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<INPUT class=insight type=checkbox value=1 " . $checked{ $FORM_DATA{showtophosts} } . " name=showtophosts>&thinsp;Host
	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<INPUT class=insight type=checkbox value=1 " . $checked{ $FORM_DATA{showtophostservices} } . " name=showtophostservices>&thinsp;Host-Service
	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<INPUT type=checkbox class=insight value=1 " . $checked{ $FORM_DATA{showtopservices} } . " name=showtopservices>&thinsp;Service
	</TD>
	</TR>
	<TR>
	<TD class=tableFillNone>
	<INPUT type=submit class=button value='Generate Report' name=submit>&nbsp;
	<a STYLE='text-decoration:none' href=nagios_outages1.pl><INPUT type=reset class=button value='Reset'></a>&nbsp;
	<INPUT type=button class=button value='Help' name=help onclick=\"open_window('$help_url')\">
	</TD>
	</TR>
	</TBODY>
	</TABLE>
	</FORM>
";

if ( !$FORM_DATA{submit} ) {
    print "</DIV></BODY></HTML>";
    exit;
}
my $host_outage_parameter    = $FORM_DATA{hosttype};
my $service_outage_parameter = $FORM_DATA{servicetype};
$start_year  = $FORM_DATA{start_year};
$start_month = $FORM_DATA{start_month};
$start_day   = $FORM_DATA{start_day};
$end_year    = $FORM_DATA{end_year};
$end_month   = $FORM_DATA{end_month};
$end_day     = $FORM_DATA{end_day};

if (not valid_dates()) {
    print "</DIV></BODY></HTML>";
    exit;
}
my $table_print_string = get_alarm_data( $host_outage_parameter, $service_outage_parameter );
if ( $FORM_DATA{showtable} ) {
    print $table_print_string;
}
if ( $FORM_DATA{showchart} ) {
    print_outage_trend_chart();
}

if ( $FORM_DATA{showtophostgroups} ) {
    my $component = "hostgroup";
    print "<table class='data' border='0' cellpadding='5' cellspacing='1' width='100%'><colgroup width='45%'></colgroup><colgroup width='*'></colgroup>";
    print "<tbody><tr class='tableHeader'>";
    print "<td colspan=2>Top Outages by Host Group</td>";
    my $printstring = print_top_components( $component, $host_outage_parameter );
    print "<tr><td class=tableFill03 > ";
    print "<table cellpadding='5' cellspacing='1' >";
    if ($printstring) {
	# FIX LATER:  We're using $time to avoid browser caching here.  It would better to disable caching using the proper HTTP headers.
	my $time = time;
	$graphfile = "outagecountstack_" . $component . "_graph.png";
	print_trend_chart_component();
	print "<tbody><tr><td style='border: 0px none ;'><img border=0 src='$graphhtmlref/images/spacer.gif' alt='' height='1' width='1'></td></tr>
	    <tr><td style='border: 0px none ;' valign='top'>
	    <IMG border=0 src='$graphhtmlref/$graphfile?$time' alt='Top Outages by Host Group' hspace='20'></td></tr>";
	print "<tr><td style='border: 0px none ;'><img border=0 src='$graphhtmlref/images/spacer.gif' alt='' height='1' width='1'></td></tr>";
	print "</tbody>";
	print "</table>";
	print "</td>";
	## print "<td class=tableFill03 valign=top  style='border: 0px none ;'>";
	print "<td class=tableFill03>";
	print "<table cellpadding='5' cellspacing='1'>";
	print "<tbody>";
	print "<tr><td style='border: 0px none ;'><img border=0 src='$graphhtmlref/images/spacer.gif' alt='' height='1' width='1'></td></tr>";
	print "<tr>";
	print "<td class=tableFill01  valign='top' width='35' align=center>Rank</td>";
	print "<td class=tableFill01  valign='top'>Host Group</td>";
	print "<td class=tableFill01  valign='top' width='57' align=center>Outage</td></tr>";
	print $printstring;
	print "<tr><td style='border: 0px none ;'><img border=0 src='$graphhtmlref/images/spacer.gif' alt='' height='1' width='1'></td></tr>";
	print "</tbody>";
    }
    else {
	print "<tr><td class=tableFill03  style='border: 0px none ;' colspan=2>No Data Found.</td></tr>";
    }
    print "</table>";
    print "</td></tr>";
    print "</table>";
}

if ( $FORM_DATA{showtophosts} ) {
    my $component = "host";
    print "<table class='data' border='0' cellpadding='5' cellspacing='1' width='100%'><colgroup width='45%'></colgroup><colgroup width='*'></colgroup>";
    print "<tbody><tr class='tableHeader'>";
    print "<td colspan=2>Top Outages by Host</td>";
    my $printstring = print_top_components( $component, $host_outage_parameter );
    print "<tr><td class=tableFill03> ";
    print "<table cellpadding='5' cellspacing='1' >";
    if ($printstring) {
	# FIX LATER:  We're using $time to avoid browser caching here.  It would better to disable caching using the proper HTTP headers.
	my $time = time;
	$graphfile = "outagecountstack_" . $component . "_graph.png";
	print_trend_chart_component();
	print "<tbody><tr><td style='border: 0px none ;'><img border=0 src='$graphhtmlref/images/spacer.gif' alt='' height='1' width='1'></td></tr>
	    <tr><td style='border: 0px none ;' valign='top'>
	    <IMG border=0 src='$graphhtmlref/$graphfile?$time' alt='Top Outages by Host' hspace='20'></td></tr>";
	print "<tr><td style='border: 0px none ;'><img border=0 src='$graphhtmlref/images/spacer.gif' alt='' height='1' width='1'></td></tr>";
	print "</tbody>";
	print "</table>";
	print "</td>";
	print "<td class=tableFill03>";
	print "<table cellpadding='5' cellspacing='1'>";
	print "<tbody>";
	print "<tr><td style='border: 0px none ;'><img border=0 src='$graphhtmlref/images/spacer.gif' alt='' height='1' width='1'></td></tr>";
	print "<tr>";
	print "<td class=tableFill01  valign='top' width='35' align=center>Rank</td>";
	print "<td class=tableFill01  valign='top'>Host</td>";
	print "<td class=tableFill01  valign='top' width='57' align=center>Outage</td></tr>";
	print $printstring;
	print "<tr><td style='border: 0px none ;'><img border=0 src='$graphhtmlref/images/spacer.gif' alt='' height='1' width='1'></td></tr>";
	print "</tbody>";
    }
    else {
	print "<tr><td class=tableFill03  style='border: 0px none ;' colspan=2>No Data Found.</td></tr>";
    }
    print "</table>";
    print "</td></tr>";
    print "</table>";
}

if ( $FORM_DATA{showtophostservices} ) {
    my $component = "hostservice";
    print "<table class='data' border='0' cellpadding='5' cellspacing='1' width='100%'><colgroup width='45%'></colgroup><colgroup width='*'></colgroup>";
    print "<tbody><tr class='tableHeader'>";
    print "<td colspan=2>Top Outages by Host/Service</td>";
    my $printstring = print_top_components( $component, $service_outage_parameter );
    print "<tr><td class=tableFill03> ";
    print "<table cellpadding='5' cellspacing='1' >";
    if ($printstring) {
	# FIX LATER:  We're using $time to avoid browser caching here.  It would better to disable caching using the proper HTTP headers.
	my $time = time;
	$graphfile = "outagecountstack_" . $component . "_graph.png";
	print_trend_chart_component();
	print "<tbody><tr><td style='border: 0px none ;'><img border=0 src='$graphhtmlref/images/spacer.gif' alt='' height='1' width='1'></td></tr>
	    <tr><td style='border: 0px none ;' valign='top'>
	    <IMG border=0 src='$graphhtmlref/$graphfile?$time' alt='Top Outages by Host/Service' hspace='20'></td></tr>";
	print "<tr><td style='border: 0px none ;'><img border=0 src='$graphhtmlref/images/spacer.gif' alt='' height='1' width='1'></td></tr>";
	print "</tbody>";
	print "</table>";
	print "</td>";
	## print "<td class=tableFill03 valign=top  style='border: 0px none ;'>";
	print "<td class=tableFill03>";
	print "<table cellpadding='5' cellspacing='1'>";
	print "<tbody>";
	print "<tr><td style='border: 0px none ;'><img border=0 src='$graphhtmlref/images/spacer.gif' alt='' height='1' width='1'></td></tr>";
	print "<tr>";
	print "<td class=tableFill01  valign='top' width='35' align=center>Rank</td>";
	print "<td class=tableFill01  valign='top'>Host/Service</td>";
	print "<td class=tableFill01  valign='top' width='57' align=center>Outage</td></tr>";
	print $printstring;
	print "<tr><td style='border: 0px none ;'><img border=0 src='$graphhtmlref/images/spacer.gif' alt='' height='1' width='1'></td></tr>";
	print "</tbody>";
    }
    else {
	print "<tr><td class=tableFill03  style='border: 0px none ;' colspan=2>No Data Found.</td></tr>";
    }
    print "</table>";
    print "</td></tr>";
    print "</table>";
}

if ( $FORM_DATA{showtopservices} ) {
    my $component = "service";
    print "<table class='data' border='0' cellpadding='5' cellspacing='1' width='100%'><colgroup width='45%'></colgroup><colgroup width='*'></colgroup>";
    print "<tbody><tr class='tableHeader'>";
    print "<td colspan=2>Top Outages by Service</td>";
    my $printstring = print_top_components( $component, $service_outage_parameter );
    print "<tr><td class=tableFill03> ";
    print "<table cellpadding='5' cellspacing='1' >";
    if ($printstring) {
	# FIX LATER:  We're using $time to avoid browser caching here.  It would better to disable caching using the proper HTTP headers.
	my $time = time;
	$graphfile = "outagecountstack_" . $component . "_graph.png";
	print_trend_chart_component();
	print "<tbody><tr><td style='border: 0px none ;'><img border=0 src='$graphhtmlref/images/spacer.gif' alt='' height='1' width='1'></td></tr>
	    <tr><td style='border: 0px none ;' valign='top'>
	    <IMG border=0 src='$graphhtmlref/$graphfile?$time' alt='Top Outages by Service' hspace='20'></td></tr>";
	print "<tr><td style='border: 0px none ;'><img border=0 src='$graphhtmlref/images/spacer.gif' alt='' height='1' width='1'></td></tr>";
	print "</tbody>";
	print "</table>";
	print "</td>";
	print "<td class=tableFill03>";
	print "<table cellpadding='5' cellspacing='1'>";
	print "<tbody>";
	print "<tr><td style='border: 0px none ;'><img border=0 src='$graphhtmlref/images/spacer.gif' alt='' height='1' width='1'></td></tr>";
	print "<tr>";
	print "<td class=tableFill01  valign='top' width='35' align=center>Rank</td>";
	print "<td class=tableFill01  valign='top'>Service</td>";
	print "<td class=tableFill01  valign='top' width='57' align=center>Outage</td></tr>";
	print $printstring;
	print "<tr><td style='border: 0px none ;'><img border=0 src='$graphhtmlref/images/spacer.gif' alt='' height='1' width='1'></td></tr>";
	print "</tbody>";
    }
    else {
	print "<tr><td class=tableFill03  style='border: 0px none ;' colspan=2>No Data Found.</td></tr>";
    }
    print "</table>";
    print "</td></tr>";
    print "</table>";
}

print "</DIV></BODY></HTML>";
exit;

sub printstyles {
    print qq(
<style type='text/css'>

body {margin:0px; padding:0px; background-color:#ffffff; font-family: verdana, helvetica, arial, sans-serif; font-size: 12px; line-height:1.4em}
option, input {font-family: verdana, helvetica, arial, sans-serif; font-size: 12px; line-height:1.4em}

table td {font-family: verdana, helvetica, arial, sans-serif; font-size: 12px; line-height:1.4em}

A:link {color:#55609A; text-decoration:none; font-weight:bold}
A:visited {color:#999999; text-decoration:none font-weight:bold}
A:active {text-decoration:none; font-weight:bold}
A:hover {color:#FA840F; text-decoration:none; cursor: pointer; font-weight:bold}

h1 {color:#FA840F; font-size: 12px; margin:0px}
h2 {color:#000000; font-size: 12px; margin:25px 0px 3px; font-weight:bold}

ul {margin-top:.5em}
ul, li {margin-left:7px; line-height:2em}
input {margin-left:0px}

input.button {
border: 1px solid #FA840F;
border-radius: 2px;
padding: 0 10px;
font-family: verdana, helvetica, arial, sans-serif;
font-size: 12px;
font-weight: normal;
background-color:#FA840F;
color: #FFFFFF;
margin: 0;
}

/* for position of groundwork logo */
.logo {position:absolute; top:15px; left:182px; z-index:100}

.topHeader {background-color:#666666;}
.topHeaderbg {position:absolute; top:0px; left:0px; background-color:#666666; width:1280px;}
.mainImage {position:absolute; top:100px; left:0px; z-index:20}

/* For top navigational tabs */
.topNav {position:absolute; top:77px; left:12px; height:24px; background-color:#525252; color:#cccccc; z-index:30;}
.topNav table {margin:0px}
.topNav td {font-size: 12px; border:1px solid #000000; padding-left:2em; padding-right:2em; margin:0px}

.topNav A:link {color:#cccccc; text-decoration:none;}
.topNav A:visited {color:#cccccc; text-decoration:none}
.topNav A:active {text-decoration:none}
.topNav A:hover {color:#FA840F; text-decoration:none; cursor: pointer;}


td.tabLit {color:#ffffff; background-color:#9b9b9b; border:1px solid #000000;}
.tabLit A:link {color:#ffffff; text-decoration:none;}
.tabLit A:visited {color:#ffffff; text-decoration:none}
.tabLit A:active {text-decoration:none}
.tabLit A:hover {color:#000000; text-decoration:none; cursor: pointer;}

.tabLitUnder {position:absolute; top:102px; left:0px; width:100%; height;24px; color:#ffffff; background-color:#9b9b9b;}
.tabLitUnder A:link {color:#ffffff; text-decoration:none;}
.tabLitUnder A:visited {color:#ffffff; text-decoration:none}
.tabLitUnder A:active {text-decoration:none}
.tabLitUnder A:hover {color:#000000; text-decoration:none; cursor: pointer;}


/* For collapsible menu */
.menu {position:absolute; top:160px; left:12px; color:#000000; font-weight:bold; font-family: verdana, helvetica, arial, sans-serif; font-size: 12px; line-height:1.3em; border:0px;}

/* for main content of pages */
.contentBody {position:absolute; top:160px; left:182px; border:1px}

/* for tables */
table.data td {border:0px solid #000000; font-size: 12px;}
.tableHeaderPage td {background-color:#444444; color:#ffffff; font-size: 12px; font-weight:bold; width:363px; padding-left:10px}
.tableHeader td {background-color:#777777; color:#ffffff; font-size: 12px; font-weight:bold; width:363px; padding-left:10px}
.tableHeaderRight {font-weight:normal; font-size: 12px; position:relative; left:710px}
.tableHeaderFlexWidth {background-color:#777777; color:#ffffff; font-size: 12px; font-weight:bold; padding-left:10px}

/* for shaded table cells */
.tableFill01 {background-color:#a0a0a0; color:#ffffff; font-weight:bold; padding-left:10px}
.tableFill02 {background-color:#cccccc; color:#000000; padding-left:10px}
.tableFill03 {background-color:#e6e6e6; color:#000000; padding-left:10px}
.tableFill04 {background-color:#ffffff; color:#000000; padding-left:10px}
.tableFillNone {padding: 0;}


/* for highlighted table cells */
.tableLit01 {background-color:#9EC56E; color:#000000; padding-left:10px}
.tableLit02 {background-color:#E0EA44; color:#000000; padding-left:10px}

/* for charts */
.chart01 {background-color:#EB6232;}
.chart02 {background-color:#F3B50F; color:#ffffff; border:0px; font-weight:bold}
.chart03 {background-color:#7E87B7; color:#ffffff; border:0px; font-weight:bold}
.chart04 {background-color:#8DD9E0; color:#ffffff; border:0px; font-weight:bold}
.chart05 {background-color:#64A2B8; color:#ffffff; border:0px; font-weight:bold}
.chart06 {background-color:#D3DB00; color:#ffffff; border:0px; font-weight:bold}
.chart07 {background-color:#8BA016; color:#ffffff; border:0px; font-weight:bold}
.chart08 {background-color:#C0C0C0; color:#ffffff; border:0px; font-weight:bold}
.chart09 {background-color:#818181; color:#ffffff; border:0px; font-weight:bold}
.chart10 {background-color:#9BAEFF; color:#ffffff; border:0px; font-weight:bold}
.chart11 {background-color:#6F76C4; color:#ffffff; border:0px; font-weight:bold}
.chart12 {background-color:#E092E3; color:#ffffff; border:0px; font-weight:bold}
.chart13 {background-color:#C05599; color:#ffffff; border:0px; font-weight:bold}

.leftColumn {position:absolute; top:375px; left:182px; width:500px}
.rightColumn {position:absolute; top:375px; left:728px; width:182px}

.note {font: 7pt/9pt verdana; color:#999999;}
.note A:link {color:#999999; text-decoration:none;}
.note A:visited {color:#999999; text-decoration:none}
.note A:active {text-decoration:none}
.note A:hover {color:#FA840F; text-decoration:none; cursor: pointer;}


/* for smaller information on bottom */
.column01 {position:absolute; top:683px; left:182px;}
.column02 {position:absolute; top:683px; left:364px;}
.column03 {position:absolute; top:683px; left:545px;}
.column04 {position:absolute; top:683px; left:728px;}
.column05 {position:absolute; top:683px; left:1091px; width:70px}
.column06 {position:absolute; top:683px; left:1150px;}


/* for test_tree */
table#dashboard_OK {margin:3px 1px; padding:0px; border: 1px solid #000000; border-collapse:collapse; clear:both; width:220px;}

#dashboard_OK td {vertical-align:top; padding:5px; text-align:left; background-color:#cccccc; border: 1px solid #000000;}

table#dashboard_UNACK {margin:8px 0; padding:5px; border:1px solid #000000; border-collapse:collapse; clear:both; background-color:#EB6232;}

#dashboard_UNACK td {vertical-align:top; padding:5px; background-color:#EB6232;	border: 1px solid #000000;}

table#dashboard_ACK {margin:8px 0; padding:5px; border: 1px solid #000000; border-collapse:collapse; clear:both;}

#dashboard_ACK td {padding:5px; background-color:#F3B50F; border: 1px solid #000000;}
table#dashboard_OUTAGE {margin:8px 0; padding:0; border: 1px solid #000; border-collapse:collapse; clear:both;}
#dashboard_OUTAGE td {padding:5px; background-color:#FF9933; border: 1px solid #000000;}


.dotted01 {position:absolute; top:673px; left:0px; width:100px; z-index:40}
.dotted02 {position:absolute; top:737px; left:0px; width:100px; z-index:40}

.dottedNav {position:absolute; top:125px; left:0px; width:100%; z-index:40}

.bottom {position:absolute; top:734px; left:0px; width:100%; z-index:40}

.header {font-weight:bold}
.insight {background-color:#ffffff}
.insightdim {background-color:#f0f0f0}
.select_label_disabled {
color: #707070;
}
select.insight_disabled {
background-color:#E6E6E6;
color: #707070;
border: 1px solid #CCCCCC;
}
select.insight {
background-color:#ffffff;
border: 1px solid #E6E6E6;
}
.formHead {font-size: 12px; letter-spacing:1px; font-weight:bold; text-transform:uppercase; margin-top:1em}
.noteHead {font-weight:bold}
.submit {margin-top:1em}

</style>
);
}

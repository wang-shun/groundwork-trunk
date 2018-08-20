#!/usr/local/groundwork/perl/bin/perl -w --
#
#	GroundWork Monitor - The ultimate data integration framework.
#	Copyright (C) 2004-2010 GroundWork Open Source, Inc.
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
use Time::Local;
use CollageQuery;
my $stylesheethtmlref = "/groundwork/reports/html/gw_style.css";
my $treescriptsdirref = "/groundwork/reports/html";
my $imagesdirref      = "/groundwork/reports/images/";
my $thisprogram       = "api_sample2.pl";

print "Content-type: text/html \n\n";
my $request_method = $ENV{'REQUEST_METHOD'};
my $form_info;
if ( $request_method eq "GET" ) {
    $form_info = $ENV{'QUERY_STRING'};
    ##	$form_info =~ s/%([\dA-Fa-f][\dA-Fa-f])/pack("C",hex($1))/eg;
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
my %FORM_DATA;
my ( $key, $value );
foreach my $key_value ( split( /&/, $form_info ) ) {
    ( $key, $value ) = split( /=/, $key_value );
    $value =~ tr/+/ /;
    $value =~ s/%([\dA-Fa-f][\dA-Fa-f])/pack("C",hex($1))/eg;
    if ( defined( $FORM_DATA{$key} ) ) {
	$FORM_DATA{$key} = join( "\0", $FORM_DATA{$key}, $value );
    }
    else {
	$FORM_DATA{$key} = $value;
    }
}

print "
	<HTML>
	<HEAD>
	<META HTTP-EQUIV='Expires' CONTENT='0'>
	<META HTTP-EQUIV='Pragma' CONTENT='no-cache'>
	<TITLE>Groundwork Collage</TITLE>
	<link rel='stylesheet' type='text/css' href='$stylesheethtmlref'>
";
print "
	<style>
	   /* styles for the tree */
	   SPAN.TreeviewSpanArea A {
		font-size: 7pt;
		font-family: verdana,helvetica;
		text-decoration: none;
		color: black
	   }
	   table {
		font-size: 7pt;
		font-family: verdana,helvetica;
		text-decoration: none;
		color: black
	   }

	   SPAN.TreeviewSpanArea A:hover {
		color: '#820082';
	   }
	</style>
	<script src=\"$treescriptsdirref/ua.js\"></script>
	<script src=\"$treescriptsdirref/ftiens4.js\"></script>
";

print qq(
	<SCRIPT language="JavaScript">
	function changePage (page) {
		if (page.length) {
			location.href=page
		}
	}
	function updatePage (attrName,attrValue) {
		page="$thisprogram?$form_info&"+attrName+"="+attrValue
		if (page.length) {
			location.href=page
		}
	}
	</SCRIPT>
);

print '
	</HEAD>
	<BODY>
	<DIV id=container>
';
if ( !$FORM_DATA{Portal} ) {    # Don't print header if invoked from the portal
    print '
	<DIV id=logo></DIV>
	<DIV id=pagetitle>
	<H1>GroundWork Perl API Sample</H1>
	</DIV>
	';
}
my ( $sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $isdst ) = localtime(time);
$year += 1900;
my $month = qw(January February March April May June July August September October November December) [$mon];
my $timestring = sprintf "%02d:%02d:%02d", $hour, $min, $sec;
my $thisday = qw(Sunday Monday Tuesday Wednesday Thursday Friday Saturday) [$wday];
print "<FORM name=selectForm class=formspace action=$thisprogram method=get>";
print "<TABLE id=controlpanel><TBODY><TR>";
print "<TH colSpan=4>$thisday, $month $mday, $year. $timestring</TH></TR>";

#	Set form defaults
if ( !$form_info ) {
    ##	$FORM_DATA{cqClass}="CollageHostGroupQuery";
}
my %checked = ();
$checked{ $FORM_DATA{cqClass} } = "SELECTED";
print "
<TR>
    <TD WIDTH=33%><B>Select CollageQuery Class:</B>
    </TD>
    <TD><select name=cqClass class=boxspace onChange=changePage(\"$thisprogram?cqClass=\"+this.options[this.selectedIndex].value)>
	<option class=boxspace $checked{''} value=''>
	<option class=boxspace $checked{CollageHostGroupQuery} value='CollageHostGroupQuery'>CollageHostGroupQuery
	<option class=boxspace $checked{CollageHostQuery} value='CollageHostQuery'>CollageHostQuery
	<option class=boxspace $checked{CollageServiceQuery} value='CollageServiceQuery'>CollageServiceQuery
	<option class=boxspace $checked{CollageMonitorServerQuery} value='CollageMonitorServerQuery'>CollageMonitorServerQuery
	<option class=boxspace $checked{CollageEventQuery} value='CollageEventQuery'>CollageEventQuery
	</select>
    </TD>
";
if ( $FORM_DATA{cqClass} ) {
    my %checked = ();
    $checked{ $FORM_DATA{cqMethod} } = "SELECTED";
    print "<tr>
	    <TD><B>Select Method:</B>
	    <TD><select name=cqMethod class=boxspace onChange=changePage(\"$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=\"+this.options[this.selectedIndex].value)>
	    <option class=boxspace value='$thisprogram'>
    ";
    if ( $FORM_DATA{cqClass} eq "CollageHostGroupQuery" ) {
	print "<option class=boxspace value='getServicesForHostGroup' $checked{getServicesForHostGroup}>getServicesForHostGroup";
	print "<option class=boxspace value='getHostsForHostGroup' $checked{getHostsForHostGroup}>getHostsForHostGroup";
	print "<option class=boxspace value='getHostGroups' $checked{getHostGroups}>getHostGroups";
	print "<option class=boxspace value='getHostGroup' $checked{getHostGroup}>getHostGroup";
    }
    elsif ( $FORM_DATA{cqClass} eq "CollageServiceQuery" ) {
	print "<option class=boxspace value='getService' $checked{getService}>getService";
	print "<option class=boxspace value='getServices' $checked{getServices}>getServices";
    }
    elsif ( $FORM_DATA{cqClass} eq "CollageHostQuery" ) {
	print "<option class=boxspace value='getServicesForHost' $checked{getServicesForHost}>getServicesForHost";
	print "<option class=boxspace value='getHosts' $checked{getHosts}>getHosts";
	print "<option class=boxspace value='getHostStatusForHost' $checked{getHostStatusForHost}>getHostStatusForHost";
	print "<option class=boxspace value='getDeviceForHost' $checked{getDeviceForHost}>getDeviceForHost";
    }
    elsif ( $FORM_DATA{cqClass} eq "CollageMonitorServerQuery" ) {
	print "<option class=boxspace value='getMonitorServers' $checked{getMonitorServers}>getMonitorServers";
	print "<option class=boxspace value='getHostsForMonitorServer' $checked{getHostsForMonitorServer}>getHostsForMonitorServer";
	print
	  "<option class=boxspace value='getHostGroupsForMonitorServer' $checked{getHostGroupsForMonitorServer}>getHostGroupsForMonitorServer";
    }
    elsif ( $FORM_DATA{cqClass} eq "CollageEventQuery" ) {
	print "<option class=boxspace value='getEventsForDevice' $checked{getEventsForDevice}>getEventsForDevice";
	print "<option class=boxspace value='getEventsForService' $checked{getEventsForService}>getEventsForService";
	print "<option class=boxspace value='getEventsForHost' $checked{getEventsForHost}>getEventsForHost";
    }
    print "</select>";
}

my $t;
if ( $t = CollageQuery->new() ) {
    ##	print "New CollageQuery object.\n";
}
else {
    die "Error: connect to CollageQuery failed!\n";
}
if ( $FORM_DATA{cqMethod} =~ /^(getServicesForHostGroup|getHostsForHostGroup|getHostGroup)$/ ) {
    my $ref = $t->getHostGroups();
    print "<TR><TD><B>Select Host Group:</B>";
    print
"<TD><select name=hostgroup class=boxspace onChange=changePage(\"$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=$FORM_DATA{cqMethod}&hostgroup=\"+this.options[this.selectedIndex].value)>";
    print "<option class=boxspace value=''>";
    $ref = $t->getHostGroups();
    foreach my $key ( sort keys %{$ref} ) {
	my $selected = "";
	if ( $FORM_DATA{hostgroup} eq $key ) { $selected = "SELECTED" }
	print "<option class=boxspace value='$key' $selected>$key";
    }
    print "</select>";
}
if ( $FORM_DATA{cqMethod} =~ /^(getServicesForHost|getHostStatusForHost|getDeviceForHost|getService)$/ ) {
    print "<tr><TD><B>Select Host:</B>";
    print
"<TD><select name=host class=boxspace onChange=changePage(\"$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=$FORM_DATA{cqMethod}&host=\"+this.options[this.selectedIndex].value)>";
    print "<option class=boxspace value=''>";
    my $ref = $t->getHosts();
    foreach my $key ( sort keys %{$ref} ) {
	my $selected = "";
	if ( $FORM_DATA{host} eq $key ) { $selected = "SELECTED" }
	print "<option class=boxspace value='$key' $selected>$key";
    }
    print "</select>";
}
if ( $FORM_DATA{cqMethod} =~ /^(getHostsForMonitorServer|getHostGroupsForMonitorServer)$/ ) {
    print "<tr><TD><B>Select Monitor Server:</B>";
    print
"<TD><select name=monitorserver class=boxspace onChange=changePage(\"$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=$FORM_DATA{cqMethod}&monitorserver=\"+this.options[this.selectedIndex].value)>";
    print "<option class=boxspace value=''>";
    my $ref = $t->getMonitorServers();
    foreach my $key ( sort keys %{$ref} ) {
	my $selected = "";
	if ( $FORM_DATA{monitorserver} eq $key ) { $selected = "SELECTED" }
	print "<option class=boxspace value='$key' $selected>$key";
    }
    print "</select>";
}
if ( $FORM_DATA{cqMethod} =~ /^(getEventsForDevice|getEventsForService|getEventsForHost)$/ ) {
    if ( !$FORM_DATA{timefield} ) { $FORM_DATA{timefield} = "LastInsertDate"; }
    my %checked = ();
    $checked{ $FORM_DATA{timefield} } = "checked";
    if ( $FORM_DATA{cqMethod} =~ /^(getEventsForDevice)$/ ) {
	print "<tr><TD><B>Select Device:</B>";
	print "<td><select name=device class=boxspace onChange=changePage("
	  . "\"$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=$FORM_DATA{cqMethod}&timefield=$FORM_DATA{timefield}&device=\""
	  . "+this.options[this.selectedIndex].value+"
	  . "\"&start_month=$FORM_DATA{start_month}&start_day=$FORM_DATA{start_day}&start_year=$FORM_DATA{start_year}"
	  . "&start_hour=$FORM_DATA{start_hour}&start_min=$FORM_DATA{start_min}&start_sec=$FORM_DATA{start_sec}"
	  . "&end_month=$FORM_DATA{end_month}&end_day=$FORM_DATA{end_day}&end_year=$FORM_DATA{end_year}"
	  . "&end_hour=$FORM_DATA{end_hour}&end_min=$FORM_DATA{end_min}&end_sec=$FORM_DATA{end_sec}\"" . ")>";

	print "<option class=boxspace value=''>";
	my $ref = $t->getHosts();
	foreach my $host ( sort keys %{$ref} ) {
	    my %hosthash = $t->getDeviceForHost($host);
	    my $selected = "";
	    if ( $FORM_DATA{device} eq $hosthash{Identification} ) { $selected = "SELECTED" }
	    print "<option class=boxspace value='$hosthash{Identification}' $selected>$hosthash{Identification}";
	}
	print "</select>";
    }
    if ( $FORM_DATA{cqMethod} =~ /^(getEventsForHost)$/ ) {
	print "<tr><TD><B>Select Host:</B>";
	print "<td><select name=host class=boxspace onChange=changePage("
	  . "\"$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=$FORM_DATA{cqMethod}&timefield=$FORM_DATA{timefield}&host=\""
	  . "+this.options[this.selectedIndex].value+"
	  . "\"&start_month=$FORM_DATA{start_month}&start_day=$FORM_DATA{start_day}&start_year=$FORM_DATA{start_year}"
	  . "&start_hour=$FORM_DATA{start_hour}&start_min=$FORM_DATA{start_min}&start_sec=$FORM_DATA{start_sec}"
	  . "&end_month=$FORM_DATA{end_month}&end_day=$FORM_DATA{end_day}&end_year=$FORM_DATA{end_year}"
	  . "&end_hour=$FORM_DATA{end_hour}&end_min=$FORM_DATA{end_min}&end_sec=$FORM_DATA{end_sec}\"" . ")>";
	print "<option class=boxspace value=''>";
	my $ref = $t->getHosts();
	foreach my $key ( sort keys %{$ref} ) {
	    my $selected = "";
	    if ( $FORM_DATA{host} eq $key ) { $selected = "SELECTED" }
	    print "<option class=boxspace value='$key' $selected>$key";
	}
	print "</select>";
    }
    if ( $FORM_DATA{cqMethod} =~ /^(getEventsForService)$/ ) {
	print "<tr><TD><B>Select Host:</B>";
	print "<td><select name=host class=boxspace onChange=changePage("
	  . "\"$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=$FORM_DATA{cqMethod}&timefield=$FORM_DATA{timefield}&host=\""
	  . "+this.options[this.selectedIndex].value+"
	  . "\"&start_month=$FORM_DATA{start_month}&start_day=$FORM_DATA{start_day}&start_year=$FORM_DATA{start_year}"
	  . "&start_hour=$FORM_DATA{start_hour}&start_min=$FORM_DATA{start_min}&start_sec=$FORM_DATA{start_sec}"
	  . "&end_month=$FORM_DATA{end_month}&end_day=$FORM_DATA{end_day}&end_year=$FORM_DATA{end_year}"
	  . "&end_hour=$FORM_DATA{end_hour}&end_min=$FORM_DATA{end_min}&end_sec=$FORM_DATA{end_sec}\"" . ")>";
	print "<option class=boxspace value=''>";
	my $ref = $t->getHosts();
	foreach my $key ( sort keys %{$ref} ) {
	    my $selected = "";
	    if ( $FORM_DATA{host} eq $key ) { $selected = "SELECTED" }
	    print "<option class=boxspace value='$key' $selected>$key";
	}
	print "</select>";
	if ( $FORM_DATA{host} ) {
	    print "<tr><TD><B>Select Service:</B>";
	    ##	print "<td><select name=service class=boxspace>";
	    print "<td><select name=service class=boxspace onChange=changePage("
	      . "\"$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=$FORM_DATA{cqMethod}&timefield=$FORM_DATA{timefield}&host="
	      . "$FORM_DATA{host}&service=\"+this.options[this.selectedIndex].value+"
	      . "\"&start_month=$FORM_DATA{start_month}&start_day=$FORM_DATA{start_day}&start_year=$FORM_DATA{start_year}"
	      . "&start_hour=$FORM_DATA{start_hour}&start_min=$FORM_DATA{start_min}&start_sec=$FORM_DATA{start_sec}"
	      . "&end_month=$FORM_DATA{end_month}&end_day=$FORM_DATA{end_day}&end_year=$FORM_DATA{end_year}"
	      . "&end_hour=$FORM_DATA{end_hour}&end_min=$FORM_DATA{end_min}&end_sec=$FORM_DATA{end_sec}\"" . ")>";

	    print "<option class=boxspace value=''>";
	    my $ref = $t->getServicesForHost( $FORM_DATA{host} );
	    foreach my $key ( sort keys %{$ref} ) {
		my $selected = "";
		if ( $FORM_DATA{service} eq $key ) { $selected = "SELECTED" }
		print "<option class=boxspace value='$key' $selected>$key";
	    }
	    print "</select>";
	}
    }
    if (   ( $FORM_DATA{cqMethod} eq "getEventsForService" and $FORM_DATA{service} and $FORM_DATA{host} )
	or ( $FORM_DATA{cqMethod} eq "getEventsForDevice" and $FORM_DATA{device} )
	or ( $FORM_DATA{cqMethod} eq "getEventsForHost"   and $FORM_DATA{host} ) )
    {
	print "<TR><TD><B>Select Time Field:</B>";
	print "<td><input name=timefield type=radio class=boxspace value='LastInsertDate' $checked{LastInsertDate}>LastInsertDate";
	print "<input name=timefield type=radio class=boxspace value='FirstInsertDate' $checked{FirstInsertDate}>FirstInsertDate";
	if ( !$FORM_DATA{start_month} ) { $FORM_DATA{start_month} = sprintf "%02d", $mon + 1; }
	if ( !$FORM_DATA{start_day} )   { $FORM_DATA{start_day}   = sprintf "%02d", $mday; }
	if ( !$FORM_DATA{start_year} )  { $FORM_DATA{start_year}  = $year; }
	if ( !$FORM_DATA{start_hour} )  { $FORM_DATA{start_hour}  = "00"; }
	if ( !$FORM_DATA{start_min} )   { $FORM_DATA{start_min}   = "00"; }
	if ( !$FORM_DATA{start_sec} )   { $FORM_DATA{start_sec}   = "00"; }

	if ( !$FORM_DATA{end_month} ) { $FORM_DATA{end_month} = sprintf "%02d", $mon + 1; }
	if ( !$FORM_DATA{end_day} )   { $FORM_DATA{end_day}   = sprintf "%02d", $mday; }
	if ( !$FORM_DATA{end_year} )  { $FORM_DATA{end_year}  = $year; }
	if ( !$FORM_DATA{end_hour} )  { $FORM_DATA{end_hour}  = $hour; }
	if ( !$FORM_DATA{end_min} )   { $FORM_DATA{end_min}   = $min; }
	if ( !$FORM_DATA{end_sec} )   { $FORM_DATA{end_sec}   = $sec; }
	%checked = ();
	$checked{ $FORM_DATA{start_month} } = "SELECTED";
	print "<tr><td>
		<B>Start:</B>
		<TD><SELECT name=start_month class=small>
	";

	for ( my $i = 1; $i <= 12; $i++ ) {
	    my $tmp = sprintf "%02d", $i;
	    print "<OPTION class=small value='$tmp' $checked{$tmp}>" . qw(Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec) [ $i - 1 ];
	}
	print "</SELECT>";
	%checked = ();
	$checked{ $FORM_DATA{start_day} } = "SELECTED";
	print "<SELECT name=start_day class=small >";
	for ( my $i = 1; $i < 31; $i++ ) {
	    my $tmp = sprintf "%02d", $i;
	    print "<OPTION class=small value='$tmp' $checked{$tmp}>$tmp";
	}
	print "</SELECT>";
	%checked = ();
	$checked{ $FORM_DATA{start_year} } = "SELECTED";
	print "<SELECT name=start_year class=small>";
	for ( my $i = 2000; $i < 2016; $i++ ) {
	    print "<OPTION class=small value='$i' $checked{$i}>$i";
	}
	print "</SELECT>";
	%checked = ();
	$checked{ $FORM_DATA{start_hour} } = "SELECTED";
	print "<SELECT name=start_hour class=small>";
	for ( my $i = 0; $i < 24; $i++ ) {
	    my $tmp = sprintf "%02d", $i;
	    print "<OPTION class=small value='$tmp' $checked{$tmp}>$tmp";
	}
	print "</SELECT>";
	%checked = ();
	$checked{ $FORM_DATA{start_min} } = "SELECTED";
	print "<SELECT name=start_min class=small>";
	for ( my $i = 0; $i < 60; $i++ ) {
	    my $tmp = sprintf "%02d", $i;
	    print "<OPTION class=small value='$tmp' $checked{$tmp}>$tmp";
	}
	print "</SELECT>";
	%checked = ();
	$checked{ $FORM_DATA{start_sec} } = "SELECTED";
	print "<SELECT name=start_sec class=small>";
	for ( my $i = 0; $i < 60; $i++ ) {
	    my $tmp = sprintf "%02d", $i;
	    print "<OPTION class=small value='$tmp' $checked{$tmp}>$tmp";
	}
	print "</SELECT>";
	$checked{ $FORM_DATA{end_month} } = "SELECTED";
	print "<TR><TD>
		<B>End:</B>
		<td><SELECT name=end_month class=small>
	";
	for ( my $i = 1; $i <= 12; $i++ ) {
	    my $tmp = sprintf "%02d", $i;
	    print "<OPTION class=small value='$tmp' $checked{$tmp}>" . qw(Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec) [ $i - 1 ];
	}
	print "</SELECT>";
	%checked = ();
	$checked{ $FORM_DATA{end_day} } = "SELECTED";
	print "<SELECT name=end_day class=small>";
	for ( my $i = 1; $i < 31; $i++ ) {
	    my $tmp = sprintf "%02d", $i;
	    print "<OPTION class=small value='$tmp' $checked{$tmp}>$tmp";
	}
	print "</SELECT>";
	%checked = ();
	$checked{ $FORM_DATA{end_year} } = "SELECTED";
	print "<SELECT name=end_year class=small>";
	for ( my $i = 2000; $i < 2016; $i++ ) {
	    print "<OPTION class=small value='$i' $checked{$i}>$i";
	}
	print "</SELECT>";
	%checked = ();
	$checked{ $FORM_DATA{end_hour} } = "SELECTED";
	print "<SELECT name=end_hour class=small>";
	for ( my $i = 0; $i < 24; $i++ ) {
	    my $tmp = sprintf "%02d", $i;
	    print "<OPTION class=small value='$tmp' $checked{$tmp}>$tmp";
	}
	print "</SELECT>";
	%checked = ();
	$checked{ $FORM_DATA{end_min} } = "SELECTED";
	print "<SELECT name=end_min class=small>";
	for ( my $i = 0; $i < 60; $i++ ) {
	    my $tmp = sprintf "%02d", $i;
	    print "<OPTION class=small value='$tmp' $checked{$tmp}>$tmp";
	}
	print "</SELECT>";
	%checked = ();
	$checked{ $FORM_DATA{end_sec} } = "SELECTED";
	print "<SELECT name=end_sec class=small>";
	for ( my $i = 0; $i < 60; $i++ ) {
	    my $tmp = sprintf "%02d", $i;
	    print "<OPTION class=small value='$tmp' $checked{$tmp}>$tmp";
	}
	print "</SELECT>";
	print "<input type=hidden name=showevents value=1>";    # Flag to execute get events
    }
}
if ( ( $FORM_DATA{cqMethod} =~ /^(getService)$/ ) and ( $FORM_DATA{host} ) ) {
    print "<tr><TD><B>Select Service:</B>";
    print "<td><select name=service class=boxspace onChange=changePage(this.options[this.selectedIndex].value)>";
    print "<option class=boxspace value=''>";
    my $ref = $t->getServicesForHost( $FORM_DATA{host} );
    foreach my $key ( sort keys %{$ref} ) {
	my $selected = "";
	if ( $FORM_DATA{service} eq $key ) { $selected = "SELECTED" }
	print
"\n<option class=boxspace value='$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=$FORM_DATA{cqMethod}&host=$FORM_DATA{host}&service=$key' $selected>$key";
    }
    print "</select>";
}

print qq(
  <TR>
    <TD style='TEXT-ALIGN: center' colSpan=4>
	<INPUT class=button type=submit value='Submit'>
	<INPUT class=button  type=reset value='Reset' onClick=changePage("$thisprogram")>
	</FORM></TD></TR>
	</TBODY></TABLE>
);

#
#	CollageHostGroupQuery methods
#
my $ref;
if ( ( $FORM_DATA{cqMethod} eq "getServicesForHostGroup" ) and ( $FORM_DATA{hostgroup} ) ) {
    print "<br>Sample getServicesForHostGroup method\n";
    my $getparam = $FORM_DATA{hostgroup};
    print "<br>Getting services for host $getparam\n";
    if ( $ref = $t->getServicesForHostGroup($getparam) ) {
	print "<table border=1 cellspacing=0 padding=0>";
	print "<tr><th>Host</th><th>Service</th><th>Attribute</th><th>Value</th>";
	foreach my $host ( sort keys %{$ref} ) {
	    print "<tr><td>$host</td><td colspan=3>&nbsp;</td>";
	    foreach my $service ( sort keys %{ $ref->{$host} } ) {
		print "<tr><td>&nbsp;</td><td>$service</td><td colspan=2>&nbsp;</td>";
		foreach my $attribute ( sort keys %{ $ref->{$host}->{$service} } ) {
		    print "<tr><td colspan=2>&nbsp;</td><td>$attribute</td><td>" . $ref->{$host}->{$service}->{$attribute} . "</td>";
		}
	    }
	}
	print "</table>";
    }
    else {
	print "<br><br>No records found";
    }
}

if ( ( $FORM_DATA{cqMethod} eq "getHostsForHostGroup" ) and ( $FORM_DATA{hostgroup} ) ) {
    print "<br>Sample getHostsForHostGroup method\n";
    my $getparam = $FORM_DATA{hostgroup};
    print "<br>Getting hosts for host group $getparam\n";
    print "<table border=1 cellspacing=0 padding=0>";
    print "<tr><th>Host</th><th>Attribute</th><th>Value</th>";
    my $ref = $t->getHostsForHostGroup($getparam);
    foreach my $host ( sort keys %{$ref} ) {
	print "<tr><td>$host</td><td colspan=2>&nbsp;</td>";
	foreach my $attribute ( sort keys %{ $ref->{$host} } ) {
	    print "<tr><td>&nbsp;</td><td>$attribute</td><td>" . $ref->{$host}->{$attribute} . "\n";
	}
    }
    print "</table>";
}

if ( $FORM_DATA{cqMethod} eq "getHostGroups" ) {
    print "<br>Sample getHostGroups method\n";
    print "<table border=1 cellspacing=0 padding=0>";
    print "<tr><th>Host Group</th><th>Attribute</th><th>Value</th>";
    my $ref = $t->getHostGroups();
    foreach my $key ( sort keys %{$ref} ) {
	print "<tr><td>$key</td><td colspan=2>&nbsp;</td>";
	foreach my $attribute ( sort keys %{ $ref->{$key} } ) {
	    print "<tr><td>&nbsp;<td>$attribute</td><td>" . $ref->{$key}->{$attribute} . "</td>";
	}
    }
}

if ( ( $FORM_DATA{cqMethod} eq "getHostGroup" ) and ( $FORM_DATA{hostgroup} ) ) {
    print "<br>Sample getHostGroup method\n";
    my $getparam = $FORM_DATA{hostgroup};
    print "<br>Getting attributes for host $getparam\n";
    print "<table border=1 cellspacing=0 padding=0>";
    print "<tr><th>Attribute</th><th>Value</th>";
    my %hash = $t->getHostGroup($getparam);
    foreach my $key ( sort keys %hash ) {
	print "<tr><td>$key</td><td>$hash{$key}</td>";
    }
}

#
# CollageHostQuery class methods
#

if ( ( $FORM_DATA{cqMethod} eq "getServicesForHost" ) and ( $FORM_DATA{host} ) ) {
    print "<br>Sample getServicesForHost method\n";
    my $getparam = $FORM_DATA{host};
    print "<br>Getting services for host $getparam\n";
    my $ref = $t->getServicesForHost($getparam);
    print "<table border=1 cellspacing=0 padding=0>";
    print "<tr><th>Services</th><th>Attribute</th><th>Value</th>";
    foreach my $service ( sort keys %{$ref} ) {
	print "<tr><td>$service</td><td colspan=2>&nbsp;</td>";
	foreach my $attribute ( sort keys %{ $ref->{$service} } ) {
	    print "<tr><td>&nbsp;</td><td>$attribute</td><td>" . $ref->{$service}->{$attribute} . "</td>";
	}
    }
}

if ( $FORM_DATA{cqMethod} eq "getHosts" ) {
    print "<br>Sample getHosts method\n";
    print "<br>Getting all hosts\n";
    print "<table border=1 cellspacing=0 padding=0>";
    print "<tr><th>Host</th><th>Attribute</th><th>Value</th>";
    my $ref = $t->getHosts();
    foreach my $host ( sort keys %{$ref} ) {
	print "<tr><td>$host</td><td colspan=2>&nbsp;</td>";
	foreach my $attribute ( sort keys %{ $ref->{$host} } ) {
	    print "<tr><td>&nbsp;</td><td>$attribute<td>" . $ref->{$host}->{$attribute} . "\n";
	}
    }
}

if ( ( $FORM_DATA{cqMethod} eq "getHostStatusForHost" ) and ( $FORM_DATA{host} ) ) {
    print "<br>Sample getHostStatusForHost method\n";
    my $getparam = $FORM_DATA{host};
    print "<br>Getting Host Status for host $getparam\n";
    my %hash = $t->getHostStatusForHost($getparam);
    print "<table border=1 cellspacing=0 padding=0>";
    print "<tr><th>Attribute</th><th>Value</th>";
    foreach my $key ( sort keys %hash ) {
	print "<tr><td>$key<td>$hash{$key}\n";
    }
}

if ( ( $FORM_DATA{cqMethod} eq "getDeviceForHost" ) and ( $FORM_DATA{host} ) ) {
    print "<br>Sample getDeviceForHost method\n";
    my $getparam = $FORM_DATA{host};
    print "<br>Getting Devices for host $getparam\n";
    my %hash = $t->getDeviceForHost($getparam);
    print "<table border=1 cellspacing=0 padding=0>";
    print "<tr><th>Attribute</th><th>Value</th>";
    foreach my $key ( sort keys %hash ) {
	print "<tr><td>$key<td>$hash{$key}\n";
    }
}

#
# CollageServiceQuery class methods
#
if ( ( $FORM_DATA{cqMethod} eq "getService" ) and ( $FORM_DATA{host} ) and ( $FORM_DATA{service} ) ) {
    print "<br>Sample getService method\n";
    my $gethost    = $FORM_DATA{host};
    my $getservice = $FORM_DATA{service};
    print "<br>Getting Service data for host $gethost, service $getservice.\n";
    my %hash = $t->getService( $gethost, $getservice );
    print "<table border=1 cellspacing=0 padding=0>";
    print "<tr><th>Attribute</th><th>Value</th>";
    foreach my $key ( sort keys %hash ) {
	print "<tr><td>$key<td>$hash{$key}\n";
    }
}

if ( $FORM_DATA{cqMethod} eq "getServices" ) {
    print "<br>Sample getServices method\n";
    print "<br>Getting services for all hosts\n";
    my $ref = $t->getServices();
    print "<table border=1 cellspacing=0 padding=0>";
    print "<tr><th>Host</th><th>Service</th><th>Attribute</th><th>Value</th>";
    foreach my $host ( keys %{$ref} ) {
	print "<tr><td>$host</td><td colspan=3>&nbsp;</td>";
	foreach my $service ( keys %{ $ref->{$host} } ) {
	    print "<tr><td>&nbsp;</td><td>$service<td colspan=2>&nbsp;</td>";
	    foreach my $attribute ( keys %{ $ref->{$host}->{$service} } ) {
		print "<tr><td colspan=2>&nbsp</td><td>$attribute</td><td>" . $ref->{$host}->{$service}->{$attribute} . "</td>";
	    }
	}
    }
}

#
# CollageMonitorQuery class methods
#
#	getMonitorServers() - return a reference to a hash of monitorserver-attributes
#	getHostsForMonitorServer(String MonitorServer) - return a reference to a hash of hosts for a designated monitorserver
#	getHostGroupsForMonitorServer(String MonitorServer) - return a reference to a hash of host groups-attributes

if ( $FORM_DATA{cqMethod} eq "getMonitorServers" ) {
    print "<br>Sample getMonitorServers method\n";
    print "<br>Getting all monitor servers \n";
    my $ref = $t->getMonitorServers();
    print "<table border=1 cellspacing=0 padding=0>";
    print "<tr><th>Monitor</th><th>Attribute</th><th>Value</th>";
    foreach my $monitor ( keys %{$ref} ) {
	print "<tr><td>$monitor</td><td colspan=2>&nbsp;</td>";
	foreach my $attribute ( keys %{ $ref->{$monitor} } ) {
	    print "<tr><td>&nbsp;</td><td>$attribute</td><td>" . $ref->{$monitor}->{$attribute} . "</td>";
	}
    }
}

if ( ( $FORM_DATA{cqMethod} eq "getHostsForMonitorServer" ) and ( $FORM_DATA{monitorserver} ) ) {
    print "<br>Sample getHostsForMonitorServer method\n";
    my $getparam = $FORM_DATA{monitorserver};
    print "<br>Getting hosts for monitor server $getparam\n";
    my $ref = $t->getHostsForMonitorServer($getparam);
    print "<table border=1 cellspacing=0 padding=0>";
    print "<tr><th>Host</th><th>Attribute</th><th>Value</th>";
    foreach my $host ( keys %{$ref} ) {
	print "<tr><td>$host</td><td colspan=2>&nbsp;</td>";
	foreach my $attribute ( keys %{ $ref->{$host} } ) {
	    print "<tr><td>&nbsp;</td><td>$attribute</td><td>" . $ref->{$host}->{$attribute} . "</td>";
	}
    }
}

if ( ( $FORM_DATA{cqMethod} eq "getHostGroupsForMonitorServer" ) and ( $FORM_DATA{monitorserver} ) ) {
    print "<br>Sample getHostGroupsForMonitorServer method\n";
    my $getparam = $FORM_DATA{monitorserver};
    print "<br>Getting hostgroups for monitor server $getparam\n";
    my $ref = $t->getHostGroupsForMonitorServer($getparam);
    print "<table border=1 cellspacing=0 padding=0>";
    print "<tr><th>Host Groups</th><th>Attribute</th><th>Value</th>";
    foreach my $hostgroups ( keys %{$ref} ) {
	print "<tr><td>$hostgroups<td colspan=2>&nbsp;</td>";
	foreach my $attribute ( keys %{ $ref->{$hostgroups} } ) {
	    print "<tr><td>&nbsp;</td><td>$attribute</td><td>" . $ref->{$hostgroups}->{$attribute} . "</td>";
	}
    }
}

#
# CollageEventQuery class methods
#
if (    $FORM_DATA{showevents}
    and ( $FORM_DATA{cqMethod} eq "getEventsForDevice" )
    and $FORM_DATA{timefield}
    and $FORM_DATA{device}
    and $FORM_DATA{start_month}
    and $FORM_DATA{end_month} )
{
    print "<br>Sample getEventsForDevice method\n";
    my $getparam1 = $FORM_DATA{device};
    my $getparam2 = $FORM_DATA{timefield};
    my $getparam3 =
      "$FORM_DATA{start_year}-$FORM_DATA{start_month}-$FORM_DATA{start_day} $FORM_DATA{start_hour}:$FORM_DATA{start_min}:$FORM_DATA{start_sec}";
    my $getparam4 =
      "$FORM_DATA{end_year}-$FORM_DATA{end_month}-$FORM_DATA{end_day} $FORM_DATA{end_hour}:$FORM_DATA{end_min}:$FORM_DATA{end_sec}";
    print "<br>Getting events for device $getparam1, $getparam2 from $getparam3 to $getparam4.\n";
    if ( my $ref = $t->getEventsForDevice( $getparam1, $getparam2, $getparam3, $getparam4 ) ) {
	my $tmpcount = ( keys %{$ref} );
	print "<br>$tmpcount records found";
	print "<table border=1 cellspacing=0 padding=0>";
	print
"<tr><th>Message ID</th><th>Host</th><th>Service</th><th>Status</th><th>First</th><th>Last</th><th>Count</th><th>Type</th><th>Text</th>";
	foreach my $event ( sort keys %{$ref} ) {
	    print "<tr><td>$event</td>" . "<td>"
	      . $ref->{$event}->{HostName} . "</td>" . "<td>"
	      . $ref->{$event}->{ServiceDescription} . "</td>" . "<td>"
	      . $ref->{$event}->{MonitorStatus} . "</td>" . "<td>"
	      . $ref->{$event}->{FirstInsertDate} . "</td>" . "<td>"
	      . $ref->{$event}->{LastInsertDate} . "</td>" . "<td>"
	      . $ref->{$event}->{MsgCount} . "</td>" . "<td>"
	      . $ref->{$event}->{ErrorType} . "</td>" . "<td>"
	      . $ref->{$event}->{TextMessage} . "</td>";

	    ##  foreach my $attribute (keys %{$ref->{$event}}) {
	    ##      print "<br>&nbsp;&nbsp;&nbsp;&nbsp;$attribute=".$ref->{$event}->{$attribute}."\n";
	    ##  }
	}
	print "</table>";
    }
    else {
	print "<br><br>No records found.";
    }
}
if (    $FORM_DATA{showevents}
    and ( $FORM_DATA{cqMethod} eq "getEventsForService" )
    and $FORM_DATA{timefield}
    and $FORM_DATA{host}
    and $FORM_DATA{service}
    and $FORM_DATA{start_month}
    and $FORM_DATA{end_month} )
{
    print "<br>Sample getEventsForService method\n";
    my $getparam2 = $FORM_DATA{timefield};
    my $getparam3 =
      "$FORM_DATA{start_year}-$FORM_DATA{start_month}-$FORM_DATA{start_day} $FORM_DATA{start_hour}:$FORM_DATA{start_min}:$FORM_DATA{start_sec}";
    my $getparam4 =
      "$FORM_DATA{end_year}-$FORM_DATA{end_month}-$FORM_DATA{end_day} $FORM_DATA{end_hour}:$FORM_DATA{end_min}:$FORM_DATA{end_sec}";
    my $getparam5 = $FORM_DATA{host};
    my $getparam6 = $FORM_DATA{service};
    print "<br>Getting events for host $getparam5, service $getparam6, $getparam2 from $getparam3 to $getparam4.\n";
    if ( my $ref = $t->getEventsForService( $getparam5, $getparam6, $getparam2, $getparam3, $getparam4 ) ) {
	my $tmpcount = ( keys %{$ref} );
	print "<br>$tmpcount records found";
	print "<table border=1 cellspacing=0 padding=0>";
	print
"<tr><th>Message ID</th><th>Host</th><th>Service</th><th>Status</th><th>First</th><th>Last</th><th>Count</th><th>Type</th><th>Text</th>";
	foreach my $event ( sort keys %{$ref} ) {
	    print "<tr><td>$event</td>" . "<td>"
	      . $ref->{$event}->{HostName} . "</td>" . "<td>"
	      . $ref->{$event}->{ServiceDescription} . "</td>" . "<td>"
	      . $ref->{$event}->{MonitorStatus} . "</td>" . "<td>"
	      . $ref->{$event}->{FirstInsertDate} . "</td>" . "<td>"
	      . $ref->{$event}->{LastInsertDate} . "</td>" . "<td>"
	      . $ref->{$event}->{MsgCount} . "</td>" . "<td>"
	      . $ref->{$event}->{ErrorType} . "</td>" . "<td>"
	      . $ref->{$event}->{TextMessage} . "</td>";
	}
	print "</table>";
    }
    else {
	print "<br><br>No records found.";
    }
}
if (    $FORM_DATA{showevents}
    and ( $FORM_DATA{cqMethod} eq "getEventsForHost" )
    and $FORM_DATA{timefield}
    and $FORM_DATA{host}
    and $FORM_DATA{start_month}
    and $FORM_DATA{end_month} )
{
    print "<br>Sample getEventsForHost method\n";
    my $getparam2 = $FORM_DATA{timefield};
    my $getparam3 =
      "$FORM_DATA{start_year}-$FORM_DATA{start_month}-$FORM_DATA{start_day} $FORM_DATA{start_hour}:$FORM_DATA{start_min}:$FORM_DATA{start_sec}";
    my $getparam4 =
      "$FORM_DATA{end_year}-$FORM_DATA{end_month}-$FORM_DATA{end_day} $FORM_DATA{end_hour}:$FORM_DATA{end_min}:$FORM_DATA{end_sec}";
    my $getparam5 = $FORM_DATA{host};
    print "<br>Getting events for host $getparam5, $getparam2 from $getparam3 to $getparam4.\n";
    if ( my $ref = $t->getEventsForHost( $getparam5, $getparam2, $getparam3, $getparam4 ) ) {
	my $tmpcount = ( keys %{$ref} );
	print "<br>$tmpcount records found";
	print "<table border=1 cellspacing=0 padding=0>";
	print
"<tr><th>Message ID</th><th>Host</th><th>Service</th><th>Status</th><th>First</th><th>Last</th><th>Count</th><th>Type</th><th>Text</th>";
	foreach my $event ( keys %{$ref} ) {
	    print "<tr><td>$event</td>" . "<td>"
	      . $ref->{$event}->{HostName} . "</td>" . "<td>"
	      . $ref->{$event}->{ServiceDescription} . "</td>" . "<td>"
	      . $ref->{$event}->{MonitorStatus} . "</td>" . "<td>"
	      . $ref->{$event}->{FirstInsertDate} . "</td>" . "<td>"
	      . $ref->{$event}->{LastInsertDate} . "</td>" . "<td>"
	      . $ref->{$event}->{MsgCount} . "</td>" . "<td>"
	      . $ref->{$event}->{ErrorType} . "</td>" . "<td>"
	      . $ref->{$event}->{TextMessage} . "</td>";
	}
	print "</table>";
    }
    else {
	print "<br><br>No records found.";
    }
}
$t->destroy();

__END__

sub printhosttree {
    my $element_ref = shift;
    my $table_print_string2 .= "
	<script>
	// You can find instructions for this file at http://www.treeview.net
	//Environment variables are usually set at the top of this file.
	USETEXTLINKS = 1
	STARTALLOPEN = 0
	USEFRAMES = 0
	USEICONS = 0
	WRAPTEXT = 1
	PRESERVESTATE = 1
	ICONPATH = \"$imagesdirref\"
	HIGHLIGHT = 1
	GLOBALTARGET = \"S\"
	foldersTree = gFld(\"<b>Nagios Managed Elements</b>\",\"$programname\")
	";
    my $hostfldcount      = 0;
    my $tmphostfolderlist = "";
    foreach my $key ( sort keys %{ $element_ref->{Host} } ) {
	my $host_status;
	my $color = "#0000FF";
	if ( $element_ref->{Host}->{$key}->{Status} =~ /^(UP)/ ) {
	    $color = "#00FF00";
	}
	elsif ( $element_ref->{Host}->{$key}->{Status} =~ /^(DOWN|UNREACHABLE)/ ) {
	    $color = "#FF0000";
	}
	$host_status = "$key <font color=$color>" . $element_ref->{Host}->{$key}->{Status} . "</font>";
	$table_print_string2 .= "fld_host_h$hostfldcount = gFld('$host_status','$programname')\n";
	$table_print_string2 .= "fld_host_h$hostfldcount.xID = \"h$hostfldcount\"\n";
	my $servicefldcount      = 0;
	my $tmpservicefolderlist = "";
	my $tmpserviceidlist     = "";
	foreach my $key2 ( keys %{ $element_ref->{Host}->{$key}->{Service} } ) {    # list host parameters
	    my $service_status = "<table id=tree><tr><td valign=top>";
	    my $color          = "#0000FF";
	    if ( $element_ref->{Host}->{$key}->{Service}->{$key2}->{Status} =~ /^(OK)/ ) {
		$color = "#00FF00";
	    }
	    elsif ( $element_ref->{Host}->{$key}->{Service}->{$key2}->{Status} =~ /^(CRITICAL)/ ) {
		$color = "#FF0000";
	    }
	    elsif ( $element_ref->{Host}->{$key}->{Service}->{$key2}->{Status} =~ /^(WARNING)/ ) {
		$color = "#FFFF00";
	    }
	    $service_status .=
	        "$key2</td><td valign=top><font color=$color>"
	      . $element_ref->{Host}->{$key}->{Service}->{$key2}->{Status}
	      . "</font></td> "
	      . "<td valign=top>"
	      . $element_ref->{Host}->{$key}->{Service}->{$key2}->{Plugin_Output};
	    $service_status .= "</td></tr></table>";
	    $table_print_string2 .=
	      "fld_service_h" . $hostfldcount . "_s" . $servicefldcount . " = gFld('" . $service_status . "','" . $programname . "')\n";
	    $tmpservicefolderlist .= "fld_service_h" . $hostfldcount . "_s" . $servicefldcount . ",";
	    $tmpserviceidlist .=
	      "fld_service_h" . $hostfldcount . "_s" . $servicefldcount . ".xID = \"h" . $hostfldcount . "_s" . $servicefldcount . "\"\n";
	    $servicefldcount++;
	}
	$tmpservicefolderlist =~ s/,$//;
	$tmpserviceidlist     =~ s/,$//;
	$table_print_string2 .= "fld_host_h$hostfldcount.addChildren([$tmpservicefolderlist])\n";
	$table_print_string2 .= $tmpserviceidlist;
	$tmphostfolderlist   .= "fld_host_h$hostfldcount,";
	$hostfldcount++;
    }
    $tmphostfolderlist =~ s/,$//;
    $table_print_string2 .= "
	fld_alarms = gFld('Alarms', 'javascript:undefined')
	fld_hostgroups =  gFld('Host Groups','$programname')
	fld_hostgroups.xID='hostgroups'
	fld_hosts = gFld('Hosts','$programname')
	fld_hosts.xID='hosts'
	fld_hosts.addChildren([$tmphostfolderlist])
	fld_alarms.addChildren([fld_hostgroups,fld_hosts])
	fld_alarms.xID='alarms'
	foldersTree.addChildren([fld_alarms])
	foldersTree.treeID = '1'
	foldersTree.xID = 'T1'
	";
    $table_print_string2 .= "</script>";
    $table_print_string2 .= '
	<table border=0><tr><td><font size=-2><a style="font-size:7pt;text-decoration:none;color:silver" href="http://www.treemenu.net/" target=_blank></a></font></td></tr></table>
	<span class=TreeviewSpanArea>
	<script>initializeDocument()</script>
	<noscript>
	A tree for site navigation will open here if you enable JavaScript in your browser.
	</noscript>
	</span>
    ';
    print "
	<table>
	<tr><td valign=top>
	";
    print $table_print_string2;
    print "
	</td>
	<td valign=top>
	";
    print "
	</td></tr>
	</table>
	";
    return;
}

exit;


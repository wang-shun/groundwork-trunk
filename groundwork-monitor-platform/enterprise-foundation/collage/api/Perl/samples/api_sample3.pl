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

my $stylesheethtmlref = "";

#my $imagesdirref="/groundwork/reports/images/";
my $thisprogram = "api_sample3.pl";

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
printstyles();

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
	<BODY class=insight>
	<DIV id=container>
';
if ( !$FORM_DATA{Portal} ) {    # Don't print header if invoked from the portal
    print '
		<DIV id=logo></DIV>
		<DIV id=pagetitle>
		<H1 class=insight>GroundWork Perl API Sample</H1>
		</DIV>
	';
}
my ( $sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $isdst ) = localtime(time);
$year += 1900;
my $month = qw(January February March April May June July August September October November December) [$mon];
my $timestring = sprintf "%02d:%02d:%02d", $hour, $min, $sec;
my $thisday = qw(Sunday Monday Tuesday Wednesday Thursday Friday Saturday) [$wday];
print "<FORM name=selectForm class=formspace action=$thisprogram method=get>";
print "<TABLE class=insightcontrolpanel><TBODY><tr class=insightgray-bg>";
print "<TH class=insight colSpan=4>$thisday, $month $mday, $year. $timestring</TH></TR>";

#	Set form defaults
if ( !$form_info ) {
    ##	$FORM_DATA{cqClass}="CollageHostGroupQuery";
}
my %checked = ();
$checked{ $FORM_DATA{cqClass} } = "SELECTED";

#	<TD class=insight><select name=cqClass class=insight onChange=changePage(\"$thisprogram?cqClass=\"+this.options[this.selectedIndex].value)>
print "
<TR class=insightgray-bg>
    <TD class=insight WIDTH=33%><B>Select CollageQuery Class:</B>
	<TD class=insight><select name=cqClass class=insight onChange=\"changePage('$thisprogram?cqClass='+this.options[this.selectedIndex].value)\">
	<option class=insight $checked{''} value=''>
	<option class=insight $checked{CollageHostGroupQuery} value='CollageHostGroupQuery'>CollageHostGroupQuery
	<option class=insight $checked{CollageHostQuery} value='CollageHostQuery'>CollageHostQuery
	<option class=insight $checked{CollageServiceQuery} value='CollageServiceQuery'>CollageServiceQuery
	<option class=insight $checked{CollageMonitorServerQuery} value='CollageMonitorServerQuery'>CollageMonitorServerQuery
	<option class=insight $checked{CollageEventQuery} value='CollageEventQuery'>CollageEventQuery
	</select>
	</TD>
";
if ( $FORM_DATA{cqClass} ) {
    my %checked = ();
    $checked{ $FORM_DATA{cqMethod} } = "SELECTED";
    print "<tr class=insightgray-bg>
		<TD class=insight><B>Select Method:</B>
		<TD class=insight><select name=cqMethod class=insight onChange=\"changePage('$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod='+this.options[this.selectedIndex].value)\">
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
    print "<TR class=insightgray-bg><TD class=insight><B>Select Host Group:</B>";
    print
"<td class=insight> <select name=hostgroup class=insight onChange=\"changePage('$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=$FORM_DATA{cqMethod}&hostgroup='+this.options[this.selectedIndex].value)\">";
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
    print "<tr class=insightgray-bg><TD class=insight><B>Select Host:</B>";
    print
"<TD class=insight><select name=host class=insight onChange=\"changePage('$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=$FORM_DATA{cqMethod}&host='+this.options[this.selectedIndex].value)\">";
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
    print "<tr class=insightgray-bg><TD class=insight><B>Select Monitor Server:</B>";
    print
"<TD class=insight><select name=monitorserver class=insight onChange=\"changePage('$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=$FORM_DATA{cqMethod}&monitorserver='+this.options[this.selectedIndex].value)\">";
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
	print "<tr class=insightgray-bgr><TD class=insight><B>Select Device:</B>";
	print "<td class=insight> <select name=device class=insight onChange=\"changePage("
	  . "'$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=$FORM_DATA{cqMethod}&timefield=$FORM_DATA{timefield}&device='"
	  . "+this.options[this.selectedIndex].value+"
	  . "'&start_month=$FORM_DATA{start_month}&start_day=$FORM_DATA{start_day}&start_year=$FORM_DATA{start_year}"
	  . "&start_hour=$FORM_DATA{start_hour}&start_min=$FORM_DATA{start_min}&start_sec=$FORM_DATA{start_sec}"
	  . "&end_month=$FORM_DATA{end_month}&end_day=$FORM_DATA{end_day}&end_year=$FORM_DATA{end_year}"
	  . "&end_hour=$FORM_DATA{end_hour}&end_min=$FORM_DATA{end_min}&end_sec=$FORM_DATA{end_sec}'" . ")\">";

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
	print "<tr class=insightgray-bg><TD class=insight><B>Select Host:</B>";
	print "<td class=insight><select name=host class=boxspace onChange=\"changePage("
	  . "'$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=$FORM_DATA{cqMethod}&timefield=$FORM_DATA{timefield}&host='"
	  . "+this.options[this.selectedIndex].value+"
	  . "'&start_month=$FORM_DATA{start_month}&start_day=$FORM_DATA{start_day}&start_year=$FORM_DATA{start_year}"
	  . "&start_hour=$FORM_DATA{start_hour}&start_min=$FORM_DATA{start_min}&start_sec=$FORM_DATA{start_sec}"
	  . "&end_month=$FORM_DATA{end_month}&end_day=$FORM_DATA{end_day}&end_year=$FORM_DATA{end_year}"
	  . "&end_hour=$FORM_DATA{end_hour}&end_min=$FORM_DATA{end_min}&end_sec=$FORM_DATA{end_sec}'" . ")\">";
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
	print "<tr class=insightgray-bg><TD class=insight><B>Select Host:</B>";
	print "<td class=insight><select name=host class=insight onChange=\"changePage("
	  . "'$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=$FORM_DATA{cqMethod}&timefield=$FORM_DATA{timefield}&host='"
	  . "+this.options[this.selectedIndex].value+"
	  . "'&start_month=$FORM_DATA{start_month}&start_day=$FORM_DATA{start_day}&start_year=$FORM_DATA{start_year}"
	  . "&start_hour=$FORM_DATA{start_hour}&start_min=$FORM_DATA{start_min}&start_sec=$FORM_DATA{start_sec}"
	  . "&end_month=$FORM_DATA{end_month}&end_day=$FORM_DATA{end_day}&end_year=$FORM_DATA{end_year}"
	  . "&end_hour=$FORM_DATA{end_hour}&end_min=$FORM_DATA{end_min}&end_sec=$FORM_DATA{end_sec}'" . ")\">";
	print "<option class=boxspace value=''>";
	my $ref = $t->getHosts();
	foreach my $key ( sort keys %{$ref} ) {
	    my $selected = "";
	    if ( $FORM_DATA{host} eq $key ) { $selected = "SELECTED" }
	    print "<option class=boxspace value='$key' $selected>$key";
	}
	print "</select>";
	if ( $FORM_DATA{host} ) {
	    print "<tr class=insightgray-bg><TD class=insight><B>Select Service:</B>";
	    ##	print 	"<td class=insight> <select name=service class=boxspace>";
	    print "<td class=insight> <select name=service class=insight onChange=\"changePage("
	      . "'$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=$FORM_DATA{cqMethod}&timefield=$FORM_DATA{timefield}&host="
	      . "$FORM_DATA{host}&service='+this.options[this.selectedIndex].value+"
	      . "'&start_month=$FORM_DATA{start_month}&start_day=$FORM_DATA{start_day}&start_year=$FORM_DATA{start_year}"
	      . "&start_hour=$FORM_DATA{start_hour}&start_min=$FORM_DATA{start_min}&start_sec=$FORM_DATA{start_sec}"
	      . "&end_month=$FORM_DATA{end_month}&end_day=$FORM_DATA{end_day}&end_year=$FORM_DATA{end_year}"
	      . "&end_hour=$FORM_DATA{end_hour}&end_min=$FORM_DATA{end_min}&end_sec=$FORM_DATA{end_sec}'" . ")\">";

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
	print "<tr class=insightgray-bg><td class=insight> <B>Select Time Field:</B>";
	print
	  "<td class=insight> <input name=timefield type=radio class=boxspace value='LastInsertDate' $checked{LastInsertDate}>LastInsertDate";
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
	if ( !$FORM_DATA{end_hour} )  { $FORM_DATA{end_hour}  = sprintf "%02d", $hour; }
	if ( !$FORM_DATA{end_min} )   { $FORM_DATA{end_min}   = sprintf "%02d", $min; }
	if ( !$FORM_DATA{end_sec} )   { $FORM_DATA{end_sec}   = sprintf "%02d", $sec; }
	%checked = ();
	$checked{ $FORM_DATA{start_month} } = "SELECTED";
	print "<tr class=insightgray-bg><td class=insight>
			<B>Start:</B>
			<td class=insight> <SELECT name=start_month class=small>
		";

	for ( my $i = 1; $i <= 12; $i++ ) {
	    my $tmp = sprintf "%02d", $i;
	    print "<OPTION class=small value='$tmp' $checked{$tmp}>" . qw(Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec) [ $i - 1 ];
	}
	print "</SELECT>	";
	%checked = ();
	$checked{ $FORM_DATA{start_day} } = "SELECTED";
	print "<SELECT name=start_day class=small >";
	for ( my $i = 1; $i < 31; $i++ ) {
	    my $tmp = sprintf "%02d", $i;
	    print "<OPTION class=small value='$tmp' $checked{$tmp}>$tmp";
	}
	print "</SELECT>	";
	%checked = ();
	$checked{ $FORM_DATA{start_year} } = "SELECTED";
	print "<SELECT name=start_year class=small>";
	for ( my $i = 2000; $i < 2016; $i++ ) {
	    print "<OPTION class=small value='$i' $checked{$i}>$i";
	}
	print "</SELECT>	";
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
	print "<tr class=insightgray-bg><td class=insight>
			<B>End:</B>
			<td class=insight> <SELECT name=end_month class=small>
		";
	for ( my $i = 1; $i <= 12; $i++ ) {
	    my $tmp = sprintf "%02d", $i;
	    print "<OPTION class=small value='$tmp' $checked{$tmp}>" . qw(Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec) [ $i - 1 ];
	}
	print "</SELECT>	";
	%checked = ();
	$checked{ $FORM_DATA{end_day} } = "SELECTED";
	print "<SELECT name=end_day class=small>";
	for ( my $i = 1; $i < 31; $i++ ) {
	    my $tmp = sprintf "%02d", $i;
	    print "<OPTION class=small value='$tmp' $checked{$tmp}>$tmp";
	}
	print "</SELECT>	";
	%checked = ();
	$checked{ $FORM_DATA{end_year} } = "SELECTED";
	print "<SELECT name=end_year class=small>";
	for ( my $i = 2000; $i < 2016; $i++ ) {
	    print "<OPTION class=small value='$i' $checked{$i}>$i";
	}
	print "</SELECT>	";
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
    print "<tr class=insightgray-bg><td class=insight> <B>Select Service:</B>";
    print "<td class=insight> <select name=service class=boxspace onChange=\"changePage(this.options[this.selectedIndex].value)\">";
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
  <tr class=insightgray-bg>
    <TD style='TEXT-ALIGN: center' colSpan=4>
	<INPUT class=insightbutton type=submit value='Submit'>
	<INPUT class=insightbutton  type=reset value='Reset' onClick=changePage("$thisprogram")>
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
	print "<table class=insight border=1 cellspacing=0 padding=0>";
	print
"<tr class=insight><th class=insight> Host</th><th class=insight> Service</th><th class=insight> Attribute</th><th class=insight> Value</th>";
	foreach my $host ( sort keys %{$ref} ) {
	    print "<tr class=insight><td class=insight> $host</td><td colspan=3>&nbsp;</td>";
	    foreach my $service ( sort keys %{ $ref->{$host} } ) {
		print "<tr class=insight><td class=insight> &nbsp;</td><td class=insight> $service</td><td colspan=2>&nbsp;</td>";
		foreach my $attribute ( sort keys %{ $ref->{$host}->{$service} } ) {
		    print "<tr class=insight><td colspan=2>&nbsp;</td><td class=insight> $attribute</td><td class=insight> "
		      . $ref->{$host}->{$service}->{$attribute} . "</td>";
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
    print "<table class=insight border=1 cellspacing=0 padding=0>";
    print "<tr class=insight><th class=insight> Host</th><th class=insight> Attribute</th><th class=insight> Value</th>";
    my $ref = $t->getHostsForHostGroup($getparam);
    foreach my $host ( sort keys %{$ref} ) {
	print "<tr class=insight><td class=insight> $host</td><td colspan=2>&nbsp;</td>";
	foreach my $attribute ( sort keys %{ $ref->{$host} } ) {
	    print "<tr class=insight><td class=insight> &nbsp;</td><td class=insight> $attribute</td><td class=insight> "
	      . $ref->{$host}->{$attribute} . "\n";
	}
    }
    print "</table>";
}

if ( $FORM_DATA{cqMethod} eq "getHostGroups" ) {
    print "<br>Sample getHostGroups method\n";
    print "<table class=insight border=1 cellspacing=0 padding=0>";
    print "<tr class=insight><th class=insight> Host Group</th><th class=insight> Attribute</th><th class=insight> Value</th>";
    my $ref = $t->getHostGroups();
    foreach my $key ( sort keys %{$ref} ) {
	print "<tr class=insight><td class=insight> $key</td><td colspan=2>&nbsp;</td>";
	foreach my $attribute ( sort keys %{ $ref->{$key} } ) {
	    print "<tr class=insight><td class=insight> &nbsp;<td class=insight> $attribute</td><td class=insight> "
	      . $ref->{$key}->{$attribute} . "</td>";
	}
    }
}

if ( ( $FORM_DATA{cqMethod} eq "getHostGroup" ) and ( $FORM_DATA{hostgroup} ) ) {
    print "<br>Sample getHostGroup method\n";
    my $getparam = $FORM_DATA{hostgroup};
    print "<br>Getting attributes for host $getparam\n";
    print "<table class=insight border=1 cellspacing=0 padding=0>";
    print "<tr class=insight><th class=insight> Attribute</th><th class=insight> Value</th>";
    my %hash = $t->getHostGroup($getparam);
    foreach my $key ( sort keys %hash ) {
	print "<tr class=insight><td class=insight> $key</td><td class=insight> $hash{$key}</td>";
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
    print "<table class=insight border=1 cellspacing=0 padding=0>";
    print "<tr class=insight><th class=insight> Services</th><th class=insight> Attribute</th><th class=insight> Value</th>";
    foreach my $service ( sort keys %{$ref} ) {
	print "<tr class=insight><td class=insight> $service</td><td colspan=2>&nbsp;</td>";
	foreach my $attribute ( sort keys %{ $ref->{$service} } ) {
	    print "<tr class=insight><td class=insight> &nbsp;</td><td class=insight> $attribute</td><td class=insight> "
	      . $ref->{$service}->{$attribute} . "</td>";
	}
    }
}

if ( $FORM_DATA{cqMethod} eq "getHosts" ) {
    print "<br>Sample getHosts method\n";
    print "<br>Getting all hosts\n";
    print "<table class=insight border=1 cellspacing=0 padding=0>";
    print "<tr class=insight><th class=insight> Host</th><th class=insight> Attribute</th><th class=insight> Value</th>";
    my $ref = $t->getHosts();
    foreach my $host ( sort keys %{$ref} ) {
	print "<tr class=insight><td class=insight> $host</td><td colspan=2>&nbsp;</td>";
	foreach my $attribute ( sort keys %{ $ref->{$host} } ) {
	    print "<tr class=insight><td class=insight> &nbsp;</td><td class=insight> $attribute<td class=insight> "
	      . $ref->{$host}->{$attribute} . "\n";
	}
    }
}

if ( ( $FORM_DATA{cqMethod} eq "getHostStatusForHost" ) and ( $FORM_DATA{host} ) ) {
    print "<br>Sample getHostStatusForHost method\n";
    my $getparam = $FORM_DATA{host};
    print "<br>Getting Host Status for host $getparam\n";
    my %hash = $t->getHostStatusForHost($getparam);
    print "<table class=insight border=1 cellspacing=0 padding=0>";
    print "<tr class=insight><th class=insight> Attribute</th><th class=insight> Value</th>";
    foreach my $key ( sort keys %hash ) {
	print "<tr class=insight><td class=insight> $key<td class=insight> $hash{$key}\n";
    }
}

if ( ( $FORM_DATA{cqMethod} eq "getDeviceForHost" ) and ( $FORM_DATA{host} ) ) {
    print "<br>Sample getDeviceForHost method\n";
    my $getparam = $FORM_DATA{host};
    print "<br>Getting Devices for host $getparam\n";
    my %hash = $t->getDeviceForHost($getparam);
    print "<table class=insight border=1 cellspacing=0 padding=0>";
    print "<tr class=insight><th class=insight> Attribute</th><th class=insight> Value</th>";
    foreach my $key ( sort keys %hash ) {
	print "<tr class=insight><td class=insight> $key<td class=insight> $hash{$key}\n";
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
    print "<table class=insight border=1 cellspacing=0 padding=0>";
    print "<tr class=insight><th class=insight> Attribute</th><th class=insight> Value</th>";
    foreach my $key ( sort keys %hash ) {
	print "<tr class=insight><td class=insight> $key<td class=insight> $hash{$key}\n";
    }
}

if ( $FORM_DATA{cqMethod} eq "getServices" ) {
    print "<br>Sample getServices method\n";
    print "<br>Getting services for all hosts\n";
    my $ref = $t->getServices();
    print "<table class=insight border=1 cellspacing=0 padding=0>";
    print
"<tr class=insight><th class=insight> Host</th><th class=insight> Service</th><th class=insight> Attribute</th><th class=insight> Value</th>";
    foreach my $host ( keys %{$ref} ) {
	print "<tr class=insight><td class=insight> $host</td><td colspan=3>&nbsp;</td>";
	foreach my $service ( keys %{ $ref->{$host} } ) {
	    print "<tr class=insight><td class=insight> &nbsp;</td><td class=insight> $service<td colspan=2>&nbsp;</td>";
	    foreach my $attribute ( keys %{ $ref->{$host}->{$service} } ) {
		print "<tr class=insight><td colspan=2>&nbsp</td><td class=insight> $attribute</td><td class=insight> "
		  . $ref->{$host}->{$service}->{$attribute} . "</td>";
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
    print "<table class=insight border=1 cellspacing=0 padding=0>";
    print "<tr class=insight><th class=insight> Monitor</th><th class=insight> Attribute</th><th class=insight> Value</th>";
    foreach my $monitor ( keys %{$ref} ) {
	print "<tr class=insight><td class=insight> $monitor</td><td colspan=2>&nbsp;</td>";
	foreach my $attribute ( keys %{ $ref->{$monitor} } ) {
	    print "<tr class=insight><td class=insight> &nbsp;</td><td class=insight> $attribute</td><td class=insight> "
	      . $ref->{$monitor}->{$attribute} . "</td>";
	}
    }
}

if ( ( $FORM_DATA{cqMethod} eq "getHostsForMonitorServer" ) and ( $FORM_DATA{monitorserver} ) ) {
    print "<br>Sample getHostsForMonitorServer method\n";
    my $getparam = $FORM_DATA{monitorserver};
    print "<br>Getting hosts for monitor server $getparam\n";
    my $ref = $t->getHostsForMonitorServer($getparam);
    print "<table class=insight border=1 cellspacing=0 padding=0>";
    print "<tr class=insight><th class=insight> Host</th><th class=insight> Attribute</th><th class=insight> Value</th>";
    foreach my $host ( keys %{$ref} ) {
	print "<tr class=insight><td class=insight> $host</td><td colspan=2>&nbsp;</td>";
	foreach my $attribute ( keys %{ $ref->{$host} } ) {
	    print "<tr class=insight><td class=insight> &nbsp;</td><td class=insight> $attribute</td><td class=insight> "
	      . $ref->{$host}->{$attribute} . "</td>";
	}
    }
}

if ( ( $FORM_DATA{cqMethod} eq "getHostGroupsForMonitorServer" ) and ( $FORM_DATA{monitorserver} ) ) {
    print "<br>Sample getHostGroupsForMonitorServer method\n";
    my $getparam = $FORM_DATA{monitorserver};
    print "<br>Getting hostgroups for monitor server $getparam\n";
    my $ref = $t->getHostGroupsForMonitorServer($getparam);
    print "<table class=insight border=1 cellspacing=0 padding=0>";
    print "<tr class=insight><th class=insight> Host Groups</th><th class=insight> Attribute</th><th class=insight> Value</th>";
    foreach my $hostgroups ( keys %{$ref} ) {
	print "<tr class=insight><td class=insight> $hostgroups<td colspan=2>&nbsp;</td>";
	foreach my $attribute ( keys %{ $ref->{$hostgroups} } ) {
	    print "<tr class=insight><td class=insight> &nbsp;</td><td class=insight> $attribute</td><td class=insight> "
	      . $ref->{$hostgroups}->{$attribute} . "</td>";
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
	print "<table class=insight border=1 cellspacing=0 padding=0>";
	print
"<tr class=insight><th class=insight> Message ID</th><th class=insight> Host</th><th class=insight> Service</th><th class=insight> Status</th><th class=insight> First</th><th class=insight> Last</th><th class=insight> Count</th><th class=insight> Type</th><th class=insight> Text</th>";
	foreach my $event ( sort keys %{$ref} ) {
	    print "<tr class=insight><td class=insight> $event</td>"
	      . "<td class=insight> "
	      . $ref->{$event}->{HostName} . "</td>"
	      . "<td class=insight> "
	      . $ref->{$event}->{ServiceDescription} . "</td>"
	      . "<td class=insight> "
	      . $ref->{$event}->{MonitorStatus} . "</td>"
	      . "<td class=insight> "
	      . $ref->{$event}->{FirstInsertDate} . "</td>"
	      . "<td class=insight> "
	      . $ref->{$event}->{LastInsertDate} . "</td>"
	      . "<td class=insight> "
	      . $ref->{$event}->{MsgCount} . "</td>"
	      . "<td class=insight> "
	      . $ref->{$event}->{ErrorType} . "</td>"
	      . "<td class=insight> "
	      . $ref->{$event}->{TextMessage} . "</td>";

	    ##	foreach my $attribute (keys %{$ref->{$event}}) {
	    ##	    print "<br>&nbsp;&nbsp;&nbsp;&nbsp;$attribute=".$ref->{$event}->{$attribute}."\n";
	    ##	}
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
	print "<table class=insight border=1 cellspacing=0 padding=0>";
	print
"<tr class=insight><th class=insight> Message ID</th><th class=insight> Host</th><th class=insight> Service</th><th class=insight> Status</th><th class=insight> First</th><th class=insight> Last</th><th class=insight> Count</th><th class=insight> Type</th><th class=insight> Text</th>";
	foreach my $event ( sort keys %{$ref} ) {
	    print "<tr class=insight><td class=insight> $event</td>"
	      . "<td class=insight> "
	      . $ref->{$event}->{HostName} . "</td>"
	      . "<td class=insight> "
	      . $ref->{$event}->{ServiceDescription} . "</td>"
	      . "<td class=insight> "
	      . $ref->{$event}->{MonitorStatus} . "</td>"
	      . "<td class=insight> "
	      . $ref->{$event}->{FirstInsertDate} . "</td>"
	      . "<td class=insight> "
	      . $ref->{$event}->{LastInsertDate} . "</td>"
	      . "<td class=insight> "
	      . $ref->{$event}->{MsgCount} . "</td>"
	      . "<td class=insight> "
	      . $ref->{$event}->{ErrorType} . "</td>"
	      . "<td class=insight> "
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
	print "<table class=insight border=1 cellspacing=0 padding=0>";
	print
"<tr class=insight><th class=insight> Message ID</th><th class=insight> Host</th><th class=insight> Service</th><th class=insight> Status</th><th class=insight> First</th><th class=insight> Last</th><th class=insight> Count</th><th class=insight> Type</th><th class=insight> Text</th>";
	foreach my $event ( keys %{$ref} ) {
	    print "<tr class=insight><td class=insight> $event</td>"
	      . "<td class=insight> "
	      . $ref->{$event}->{HostName} . "</td>"
	      . "<td class=insight> "
	      . $ref->{$event}->{ServiceDescription} . "</td>"
	      . "<td class=insight> "
	      . $ref->{$event}->{MonitorStatus} . "</td>"
	      . "<td class=insight> "
	      . $ref->{$event}->{FirstInsertDate} . "</td>"
	      . "<td class=insight> "
	      . $ref->{$event}->{LastInsertDate} . "</td>"
	      . "<td class=insight> "
	      . $ref->{$event}->{MsgCount} . "</td>"
	      . "<td class=insight> "
	      . $ref->{$event}->{ErrorType} . "</td>"
	      . "<td class=insight> "
	      . $ref->{$event}->{TextMessage} . "</td>";
	}
	print "</table>";
    }
    else {
	print "<br><br>No records found.";
    }
}
$t->destroy();
exit;

sub printstyles {
    print qq(
<style>
body.insight {
	background-color: #F0F0F0;
	scrollbar-face-color: #dcdcdc;
	scrollbar-shadow-color: #000099;
	scrollbar-highlight-color: #dcdcdc;
	scrollbar-3dlight-color: #000099;
	scrollbar-darkshadow-color: #dcdcdc;
	scrollbar-track-color: #dcdcdc;
	scrollbar-arrow-color: #dcdcdc;
  font-family: Arial, Helvetica, sans-serif;
  font-size: 10pt;
  font-style: normal;
  font-variant: normal;
  font-weight: bold;
  text-decoration: none;
  text-align: left;
}

table.insight {
 width: 100%;
 background-color: #F0F0F0; /* GroundWork Portal Interface: Background */
 border: 1px solid #666666; /* GroundWork Portal Interface: Gray (Table Fill 1px Outlines) */
 text-align: center;
}
table.insightcontrolpanel {
 width: 100%;
 text-align: left;
 background-color: #F0F0F0; /* GroundWork Portal Interface: Background */
 border: 1px solid #666666; /* GroundWork Portal Interface: Gray (Table Fill 1px Outlines) */
}

table.insighttoplist {
 width: 100%;
 background-color: #F0F0F0; /* GroundWork Portal Interface: Background */
 border: 0px solid #666666; /* GroundWork Portal Interface: Gray (Table Fill 1px Outlines) */
}
th.insight {
  font-family: Arial, Helvetica, sans-serif;
  font-size: 8pt;
  font-style: normal;
  font-variant: normal;
  font-weight: bold;
  text-decoration: none;
  text-align: center;
  color: #FFFFFF; /* GroundWork Portal Interface: White */
  padding: 2;
  background-color: #55609A; /* GroundWork Portal Interface: Table Fill #1 */
  border: 1px solid #666666; /* GroundWork Portal Interface: Gray (Table Fill 1px Outlines) */
}
th.insightrow2 {
  font-family: Arial, Helvetica, sans-serif;
  font-size: 8pt;
  font-style: normal;
  font-variant: normal;
  font-weight: bold;
  text-decoration: none;
  text-align: center;
  color: #FFFFFF; /* GroundWork Portal Interface: White */
  padding:0;
  spacing:0;
  background-color: #A0A0A0; /* GroundWork Portal Interface: Table Fill #1 */
  border: 0px solid #666666; /* GroundWork Portal Interface: Gray (Table Fill 1px Outlines) */
}



table.insightform {background-color: #bfbfbf;}
td.insight {color: #000000; font-family:verdana, arial, sans-serif; font-size: 10; vertical-align: top; }
td.insightleft {color: #000000; font-family:verdana, arial, sans-serif; font-size: 10; vertical-align: top; text-align: left;}
td.insightcenter {color: #000000; font-family:verdana, arial, sans-serif; font-size: 10; vertical-align: top;  text-align: center;}
tr.insight {color: #000000; font-family:verdana, arial, sans-serif; font-size: 10;}
tr.insightdkgray-bg td {
	background-color:#999;
	color:#fff;
	font-size:11px;
	}
tr.insightsublist td {
	color:#475181;
	font-size:10px;
	padding-left:12px !important;
	}
tr.insightsublist-graybg td {
	background-color:#efefef;
	color:#475181;
	font-size:10px;
	padding-left:12px !important;
	}

td.insighttitle {color: #000000; font-family:verdana, arial, sans-serif; font-size: 18;font-weight: bold; color: #FA840F;}
td.insighthead {background-color: #55609A; font-family:verdana, arial, sans-serif; font-size: 10; font-weight: bold; color: #ffffff;}
td.insightsubhead {background-color: #8089b9; font-family:verdana, arial, sans-serif; font-size: 10; font-weight: bold; color: #ffffff;}
td.insightselected {background-color: #898787; font-family:verdana, arial, sans-serif; font-size: 10; font-weight: bold; color: #ffffff;}
td.insightrow1 {background-color: #dcdcdc; font-family:verdana, arial, sans-serif; font-size: 10;font-weight: bold;}
td.insightrow2 {background-color: #bfbfbf; font-family:verdana, arial, sans-serif; font-size: 10;font-weight: bold;}
td.insightrow_lt {background-color: #f4f4f4; font-family:verdana, arial, sans-serif; font-size: 10;font-weight: bold;}
td.insightrow_dk {background-color: #e2e2e2; font-family:verdana, arial, sans-serif; font-size: 10;font-weight: bold;}
td.insighterror {background-color: #dcdcdc; font-family:verdana, arial, sans-serif; font-size: 10;font-weight: bold; color:cc0000}
#input, textarea, select {border: 0px solid #000099; font-family: verdana, arial, sans-serif; font-size: 9px; font-weight: bold; background-color: #ffffff; color: #000000;}
input.insight, textarea.insight, select.insight {border: 0px solid #000099; font-family: verdana, arial, sans-serif; font-size: 9px; font-weight: bold; color: #000000;}
input.insightradio {border: 0px; background-color: #dcdcdc;}
input.insightcheckbox {border: 0px; background-color: #dcdcdc;}

#input.button {
#border: 1px solid #000000;
#border-style: solid;
#border-top-width: auto;
#border-right-width: auto;
#border-bottom-width: auto;
#border-left-width: auto:
#font-family: verdana, arial, sans-serif; font-size: 11px; font-weight: bold; background-color: #898787; color: #ffffff;
#}

input.insightbutton {
	font: normal 10px/normal verdana, arial, sans-serif;
	text-transform:uppercase !important;
	border-color: #a0a6c6 #333 #333 #a0a6c6;
	border-width: 2px;
	border-style: solid;
	background:#666;
	color:#fff;
	padding:0;
	}

input.insightbox {border: 0px;}

a.insighttop:link    {
color:#ffffff;
font-size: 12px;
font-family:verdana, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}
a.insighttop:visited {
color:#ffffff;
font-size: 12px;
font-family:verdana, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}
a.insighttop:active  {
color:#ffffff;
font-size: 12px;
font-family:verdana, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}
a.insighttop:hover   {
color:#ffffff;
font-size: 12px;
font-family:verdana, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}

a.insight:link    {
color:#414141;
font-size: 12px;
font-family:verdana, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}
a.insight:visited {
color:#414141;
font-size: 12px;
font-family:verdana, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}
a.insight:active  {
color:#919191;
font-size: 12px;
font-family:verdana, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}
a.insight:hover   {
color:#919191;
font-size: 12px;
font-family:verdana, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}

a.insightorange:link    {
color:#FA840F;
font-size: 12px;
font-family:verdana, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}
a.insightorange:visited {
color:#FA840F;
font-size: 12px;
font-family:verdana, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}
a.insightorange:active  {
color:#FA840F;
font-size: 12px;
font-family:verdana, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}
a.insightorange:hover   {
color:#FA840F;
font-size: 12px;
font-family:verdana, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}

/*Center paragraph*/
p.insightcenter {
color:#000;
font-family:verdana, arial, sans-serif;
font-size: 12px;
font-weight: normal;
}

h1.insight {
color:#FA840F;
font-family:verdana, arial, sans-serif;
font-size: 18px;
font-weight: 600;
}

h2.insight {
color:#55609A;
font-family:verdana, arial, sans-serif;
font-size: 14px;
font-weight: bold;
}

h3.insight {
color:#000;
font-family:verdana, arial, sans-serif;
font-size: 12px;
font-weight: bold;
}

h4.insight {
color:#FFFFFF;
font-family:verdana, arial, sans-serif;
font-size: 12px;
font-weight: bold;
}

h5.insight {
color:#000;
font-family:verdana, arial, sans-serif;
font-size: 16px;
font-style: italic;
font-weight: normal;
}

h6.insight {
color:#000;
font-family:verdana, arial, sans-serif;
font-size: 18px;
font-weight: bold;
}
</style>
);
}


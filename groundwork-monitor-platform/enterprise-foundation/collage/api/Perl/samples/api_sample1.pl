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
my $thisprogram       = "api_sample1.pl";

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
print "	<TABLE id=controlpanel><TBODY><TR>";
print "<TH colSpan=4>$thisday, $month $mday, $year. $timestring</TH></TR>";

# Set form defaults
if ( !$form_info ) {
    ##	$FORM_DATA{cqClass}="CollageHostGroupQuery";
}
my %checked = ();
$checked{ $FORM_DATA{cqClass} } = "SELECTED";

#	<br><select name=cqClass class=boxspace onChange=setMethods(this.options[this.selectedIndex].value)>
#	<option class=boxspace ".$checked{CollageHostGroupQuery}." value='$thisprogram?cqClass=CollageHostGroupQuery'>CollageHostGroupQuery
print "
<TR>
    <TD><B>Select CollageQuery Class:</B>
	<br><select name=cqClass class=boxspace onChange=changePage(this.options[this.selectedIndex].value)>
	<option class=boxspace $checked{''} value='$thisprogram'>
	<option class=boxspace $checked{CollageHostGroupQuery} value='$thisprogram?cqClass=CollageHostGroupQuery'>CollageHostGroupQuery
	<option class=boxspace $checked{CollageHostQuery} value='$thisprogram?cqClass=CollageHostQuery'>CollageHostQuery
	<option class=boxspace $checked{CollageServiceQuery} value='$thisprogram?cqClass=CollageServiceQuery'>CollageServiceQuery
	<option class=boxspace $checked{CollageMonitorServerQuery} value='$thisprogram?cqClass=CollageMonitorServerQuery'>CollageMonitorServerQuery
	<option class=boxspace $checked{CollageEventQuery} value='$thisprogram?cqClass=CollageEventQuery'>CollageEventQuery
	</select>
    </TD>
</TR>
";
if ( $FORM_DATA{cqClass} ) {
    my %checked = ();
    $checked{ $FORM_DATA{cqMethod} } = "SELECTED";
    print "
	<TR>
	<TD><B>Select Method:</B>
	<br><select name=cqMethod class=boxspace onChange=changePage(this.options[this.selectedIndex].value)>
	<option class=boxspace value='$thisprogram'>
	";
    ##	<option value=''><i>Select a method</i>
    if ( $FORM_DATA{cqClass} eq "CollageHostGroupQuery" ) {
	print
"<option class=boxspace value='$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=getServicesForHostGroup' $checked{getServicesForHostGroup}>getServicesForHostGroup";
	print
"<option class=boxspace value='$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=getHostsForHostGroup' $checked{getHostsForHostGroup}>getHostsForHostGroup";
	print
"<option class=boxspace value='$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=getHostGroups' $checked{getHostGroups}>getHostGroups";
	print
	  "<option class=boxspace value='$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=getHostGroup' $checked{getHostGroup}>getHostGroup";
    }
    elsif ( $FORM_DATA{cqClass} eq "CollageServiceQuery" ) {
	print "<option class=boxspace value='$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=getService' $checked{getService}>getService";
	print "<option class=boxspace value='$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=getServices' $checked{getServices}>getServices";
    }
    elsif ( $FORM_DATA{cqClass} eq "CollageHostQuery" ) {
	print
"<option class=boxspace value='$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=getServicesForHost' $checked{getServicesForHost}>getServicesForHost";
	print "<option class=boxspace value='$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=getHosts' $checked{getHosts}>getHosts";
	print
"<option class=boxspace value='$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=getHostStatusForHost' $checked{getHostStatusForHost}>getHostStatusForHost";
	print
"<option class=boxspace value='$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=getDeviceForHost' $checked{getDeviceForHost}>getDeviceForHost";
    }
    elsif ( $FORM_DATA{cqClass} eq "CollageMonitorServerQuery" ) {
	print
"<option class=boxspace value='$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=getMonitorServers' $checked{getMonitorServers}>getMonitorServers";
	print
"<option class=boxspace value='$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=getHostsForMonitorServer' $checked{getHostsForMonitorServer}>getHostsForMonitorServer";
	print
"<option class=boxspace value='$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=getHostGroupsForMonitorServer' $checked{getHostGroupsForMonitorServer}>getHostGroupsForMonitorServer";
    }
    elsif ( $FORM_DATA{cqClass} eq "CollageEventQuery" ) {
	print
"<option class=boxspace value='$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=getEventsForDevice' $checked{getEventsForDevice}>getEventsForDevice";
	print
"<option class=boxspace value='$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=getEventsForService' $checked{getEventsForService}>getEventsForService";
	print
"<option class=boxspace value='$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=getEventsForHost' $checked{getEventsForHost}>getEventsForHost";
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
    print "<br><select name=hostgroup class=boxspace onChange=changePage(this.options[this.selectedIndex].value)>";
    print "<option class=boxspace value=''>";
    $ref = $t->getHostGroups();
    foreach my $key ( sort keys %{$ref} ) {
	my $selected = "";
	if ( $FORM_DATA{hostgroup} eq $key ) { $selected = "SELECTED" }
	print
	  "<option class=boxspace value='$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=$FORM_DATA{cqMethod}&hostgroup=$key' $selected>$key";
    }
    print "</select>";
}
elsif ( $FORM_DATA{cqMethod} =~ /^(getServicesForHost|getHostStatusForHost|getDeviceForHost|getService)$/ ) {
    print "<TR><TD><B>Select Host:</B>";
    print "<br><select name=host class=boxspace onChange=changePage(this.options[this.selectedIndex].value)>";
    print "<option class=boxspace value=''>";
    my $ref = $t->getHosts();
    foreach my $key ( sort keys %{$ref} ) {
	my $selected = "";
	if ( $FORM_DATA{host} eq $key ) { $selected = "SELECTED" }
	print "<option class=boxspace value='$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=$FORM_DATA{cqMethod}&host=$key' $selected>$key";
    }
    print "</select>";
}
elsif ( $FORM_DATA{cqMethod} =~ /^(getHostsForMonitorServer|getHostGroupsForMonitorServer)$/ ) {
    print "<TR><TD><B>Select Monitor Server:</B>";
    print "<br><select name=monitorserver class=boxspace onChange=changePage(this.options[this.selectedIndex].value)>";
    print "<option class=boxspace value=''>";
    my $ref = $t->getMonitorServers();
    foreach my $key ( sort keys %{$ref} ) {
	my $selected = "";
	if ( $FORM_DATA{monitorserver} eq $key ) { $selected = "SELECTED" }
	print
"<option class=boxspace value='$thisprogram?cqClass=$FORM_DATA{cqClass}&cqMethod=$FORM_DATA{cqMethod}&monitorserver=$key' $selected>$key";
    }
    print "</select>";
}
elsif ( $FORM_DATA{cqMethod} =~ /^(getEventsForDevice|getEventsForService|getEventsForHost)$/ ) {
    ## if (!$FORM_DATA{timefield}) { $FORM_DATA{timefield}="LastInsertDate"; }
    my %checked = ();
    $checked{ $FORM_DATA{timefield} } = "SELECTED";
    print "</FORM>";
    print "<FORM name=eventForm class=formspace action=$thisprogram method=get>";
    print "<input type=hidden name=cqClass value=$FORM_DATA{cqClass}>";
    print "<input type=hidden name=cqMethod value=$FORM_DATA{cqMethod}>";
    print "<TR><TD><B>Select Time Field:</B>";
    print "<br><select name=timefield class=boxspace>";
    print "<option class=boxspace value=''>";
    print "<option class=boxspace value='LastInsertDate' $checked{LastInsertDate}>LastInsertDate";
    print "<option class=boxspace value='FirstInsertDate' $checked{FirstInsertDate}>FirstInsertDate";
    print "</select>";
    print "<TR><TD><B>Select Device:</B>";
    print "<br><select name=device class=boxspace>";
    print "<option class=boxspace value=''>";
    my $ref = $t->getHosts();

    foreach my $host ( sort keys %{$ref} ) {
	my %hosthash = $t->getDeviceForHost($host);
	my $selected = "";
	if ( $FORM_DATA{device} eq $hosthash{Identification} ) { $selected = "SELECTED" }
	print "<option class=boxspace value='$hosthash{Identification}' $selected>$hosthash{Identification}";
    }

    $ref = $t->getMonitorServers();
    foreach my $key ( sort keys %{$ref} ) {
	my $selected = "";
	if ( $FORM_DATA{device} eq $key ) { $selected = "SELECTED" }
	print "<option class=boxspace value='$key' $selected>$key";
    }
    print "</select>";
    print "<TR>";
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
    print "<td>
	    <B>Start:</B>
	    <SELECT name=start_month class=small>
	";

    for ( my $i = 1 ; $i <= 12 ; $i++ ) {
	my $tmp = sprintf "%02d", $i;
	print "<OPTION class=small value='$tmp' $checked{$tmp}>" . qw(Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec) [ $i - 1 ];
    }
    print "</SELECT>	";
    %checked = ();
    $checked{ $FORM_DATA{start_day} } = "SELECTED";
    print "<SELECT name=start_day class=small >";
    for ( my $i = 1 ; $i < 31 ; $i++ ) {
	my $tmp = sprintf "%02d", $i;
	print "<OPTION class=small value='$tmp' $checked{$tmp}>$tmp";
    }
    print "</SELECT>	";
    %checked = ();
    $checked{ $FORM_DATA{start_year} } = "SELECTED";
    print "<SELECT name=start_year class=small>";
    for ( my $i = 2000 ; $i < 2016 ; $i++ ) {
	print "<OPTION class=small value='$i' $checked{$i}>$i";
    }
    print "</SELECT>	";
    %checked = ();
    $checked{ $FORM_DATA{start_hour} } = "SELECTED";
    print "<SELECT name=start_hour class=small>";
    for ( my $i = 0 ; $i < 60 ; $i++ ) {
	my $tmp = sprintf "%02d", $i;
	print "<OPTION class=small value='$tmp' $checked{$tmp}>$tmp";
    }
    print "</SELECT>";
    %checked = ();
    $checked{ $FORM_DATA{start_min} } = "SELECTED";
    print "<SELECT name=start_min class=small>";
    for ( my $i = 0 ; $i < 60 ; $i++ ) {
	my $tmp = sprintf "%02d", $i;
	print "<OPTION class=small value='$tmp' $checked{$tmp}>$tmp";
    }
    print "</SELECT>";
    %checked = ();
    $checked{ $FORM_DATA{start_sec} } = "SELECTED";
    print "<SELECT name=start_sec class=small>";
    for ( my $i = 0 ; $i < 60 ; $i++ ) {
	my $tmp = sprintf "%02d", $i;
	print "<OPTION class=small value='$tmp' $checked{$tmp}>$tmp";
    }
    print "</SELECT>";

    $checked{ $FORM_DATA{end_month} } = "SELECTED";
    print "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	    <B>End:</B>
	    <SELECT name=end_month class=small>
	";
    for ( my $i = 1 ; $i <= 12 ; $i++ ) {
	my $tmp = sprintf "%02d", $i;
	print "<OPTION class=small value='$tmp' $checked{$tmp}>" . qw(Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec) [ $i - 1 ];
    }
    print "</SELECT>	";
    %checked = ();
    $checked{ $FORM_DATA{end_day} } = "SELECTED";
    print "<SELECT name=end_day class=small>";
    for ( my $i = 1 ; $i < 31 ; $i++ ) {
	my $tmp = sprintf "%02d", $i;
	print "<OPTION class=small value='$tmp' $checked{$tmp}>$tmp";
    }
    print "</SELECT>	";
    %checked = ();
    $checked{ $FORM_DATA{end_year} } = "SELECTED";
    print "<SELECT name=end_year class=small>";
    for ( my $i = 2000 ; $i < 2016 ; $i++ ) {
	print "<OPTION class=small value='$i' $checked{$i}>$i";
    }
    print "</SELECT>	";
    %checked = ();
    $checked{ $FORM_DATA{end_hour} } = "SELECTED";
    print "<SELECT name=end_hour class=small>";
    for ( my $i = 0 ; $i < 60 ; $i++ ) {
	my $tmp = sprintf "%02d", $i;
	print "<OPTION class=small value='$tmp' $checked{$tmp}>$tmp";
    }
    print "</SELECT>";
    %checked = ();
    $checked{ $FORM_DATA{end_min} } = "SELECTED";
    print "<SELECT name=end_min class=small>";
    for ( my $i = 0 ; $i < 60 ; $i++ ) {
	my $tmp = sprintf "%02d", $i;
	print "<OPTION class=small value='$tmp' $checked{$tmp}>$tmp";
    }
    print "</SELECT>";
    %checked = ();
    $checked{ $FORM_DATA{end_sec} } = "SELECTED";
    print "<SELECT name=end_sec class=small>";
    for ( my $i = 0 ; $i < 60 ; $i++ ) {
	my $tmp = sprintf "%02d", $i;
	print "<OPTION class=small value='$tmp' $checked{$tmp}>$tmp";
    }
    print "</SELECT>";
    print "<br><INPUT class=button type=submit value='Show events'>";
}

if ( ( $FORM_DATA{cqMethod} =~ /^(getService)$/ ) and ( $FORM_DATA{host} ) ) {
    print "<TR><TD><B>Select Service:</B>";
    print "<br><select name=service class=boxspace onChange=changePage(this.options[this.selectedIndex].value)>";
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
	<INPUT class=button type=reset value='Submit' onClick=changePage("$thisprogram?$form_info")>
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
	foreach my $host ( sort keys %{$ref} ) {
	    print "<br>&nbsp;&nbsp;Host=$host\n";
	    foreach my $service ( sort keys %{ $ref->{$host} } ) {
		print "<br>&nbsp;&nbsp;&nbsp;&nbsp;Service=$service\n";
		foreach my $attribute ( sort keys %{ $ref->{$host}->{$service} } ) {
		    print "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;$attribute=" . $ref->{$host}->{$service}->{$attribute} . "\n";
		}
	    }
	}
    }
}

if ( ( $FORM_DATA{cqMethod} eq "getHostsForHostGroup" ) and ( $FORM_DATA{hostgroup} ) ) {
    print "<br>Sample getHostsForHostGroup method\n";
    my $getparam = $FORM_DATA{hostgroup};
    print "<br>Getting hosts for host group $getparam\n";
    my $ref = $t->getHostsForHostGroup($getparam);
    foreach my $host ( sort keys %{$ref} ) {
	print "<br>&nbsp;&nbsp;Host=$host\n";
	foreach my $attribute ( sort keys %{ $ref->{$host} } ) {
	    print "<br>&nbsp;&nbsp;&nbsp;&nbsp;$attribute=" . $ref->{$host}->{$attribute} . "\n";
	}
    }
}

if ( $FORM_DATA{cqMethod} eq "getHostGroups" ) {
    print "<br>Sample getHostGroups method\n";
    my $ref = $t->getHostGroups();
    foreach my $key ( sort keys %{$ref} ) {
	print "<br>&nbsp;&nbsp;HostGroup=$key\n";
	foreach my $attribute ( sort keys %{ $ref->{$key} } ) {
	    print "<br>&nbsp;&nbsp;&nbsp;&nbsp;$attribute=" . $ref->{$key}->{$attribute} . "\n";
	}
    }
}

if ( ( $FORM_DATA{cqMethod} eq "getHostGroup" ) and ( $FORM_DATA{hostgroup} ) ) {
    print "<br>Sample getHostGroup method\n";
    my $getparam = $FORM_DATA{hostgroup};
    print "<br>Getting services for host $getparam\n";
    my %hash = $t->getHostGroup($getparam);
    foreach my $key ( sort keys %hash ) {
	print "<br>&nbsp;&nbsp;$key=$hash{$key}\n";
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
    foreach my $service ( sort keys %{$ref} ) {
	print "<br>&nbsp;&nbsp;Service=$service\n";
	foreach my $attribute ( sort keys %{ $ref->{$service} } ) {
	    print "<br>&nbsp;&nbsp;&nbsp;&nbsp;$attribute=" . $ref->{$service}->{$attribute} . "\n";
	}
    }
}

if ( $FORM_DATA{cqMethod} eq "getHosts" ) {
    print "<br>Sample getHosts method\n";
    print "<br>Getting all hosts\n";
    my $ref = $t->getHosts();
    foreach my $host ( sort keys %{$ref} ) {
	print "<br>&nbsp;&nbsp;Host=$host\n";
	foreach my $attribute ( sort keys %{ $ref->{$host} } ) {
	    print "<br>&nbsp;&nbsp;&nbsp;&nbsp;$attribute=" . $ref->{$host}->{$attribute} . "\n";
	}
    }
}

if ( ( $FORM_DATA{cqMethod} eq "getHostStatusForHost" ) and ( $FORM_DATA{host} ) ) {
    print "<br>Sample getHostStatusForHost method\n";
    my $getparam = $FORM_DATA{host};
    print "<br>Getting Host Status for host $getparam\n";
    my %hash = $t->getHostStatusForHost($getparam);
    foreach my $key ( sort keys %hash ) {
	print "<br>&nbsp;&nbsp;$key=$hash{$key}\n";
    }
}

if ( ( $FORM_DATA{cqMethod} eq "getDeviceForHost" ) and ( $FORM_DATA{host} ) ) {
    print "<br>Sample getDeviceForHost method\n";
    my $getparam = $FORM_DATA{host};
    print "<br>Getting Devices for host $getparam\n";
    my %hash = $t->getDeviceForHost($getparam);
    foreach my $key ( sort keys %hash ) {
	print "<br>&nbsp;&nbsp;$key=$hash{$key}\n";
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
    foreach my $key ( sort keys %hash ) {
	print "<br>&nbsp;&nbsp;$key=$hash{$key}\n";
    }
}

if ( $FORM_DATA{cqMethod} eq "getServices" ) {
    print "<br>Sample getServices method\n";
    print "<br>Getting services for all hosts\n";
    my $ref = $t->getServices();
    foreach my $host ( keys %{$ref} ) {
	print "<br>&nbsp;&nbsp;Host=$host\n";
	foreach my $service ( keys %{ $ref->{$host} } ) {
	    print "<br>&nbsp;&nbsp;&nbsp;&nbsp;Service=$service\n";
	    foreach my $attribute ( keys %{ $ref->{$host}->{$service} } ) {
		print "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;$attribute=" . $ref->{$host}->{$service}->{$attribute} . "\n";
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
    foreach my $monitor ( keys %{$ref} ) {
	print "<br>&nbsp;&nbsp;Monitor=$monitor\n";
	foreach my $attribute ( keys %{ $ref->{$monitor} } ) {
	    print "<br>&nbsp;&nbsp;&nbsp;&nbsp;$attribute=" . $ref->{$monitor}->{$attribute} . "\n";
	}
    }
}

if ( ( $FORM_DATA{cqMethod} eq "getHostsForMonitorServer" ) and ( $FORM_DATA{monitorserver} ) ) {
    print "<br>Sample getHostsForMonitorServer method\n";
    my $getparam = $FORM_DATA{monitorserver};
    print "<br>Getting hosts for monitor server $getparam\n";
    my $ref = $t->getHostsForMonitorServer($getparam);
    foreach my $host ( keys %{$ref} ) {
	print "<br>&nbsp;&nbsp;host=$host\n";
	foreach my $attribute ( keys %{ $ref->{$host} } ) {
	    print "<br>&nbsp;&nbsp;&nbsp;&nbsp;$attribute=" . $ref->{$host}->{$attribute} . "\n";
	}
    }
}

if ( ( $FORM_DATA{cqMethod} eq "getHostGroupsForMonitorServer" ) and ( $FORM_DATA{monitorserver} ) ) {
    print "<br>Sample getHostGroupsForMonitorServer method\n";
    my $getparam = $FORM_DATA{monitorserver};
    print "<br>Getting hostgroups for monitor server $getparam\n";
    my $ref = $t->getHostGroupsForMonitorServer($getparam);
    foreach my $hostgroups ( keys %{$ref} ) {
	print "<br>&nbsp;&nbsp;hostgroups=$hostgroups\n";
	foreach my $attribute ( keys %{ $ref->{$hostgroups} } ) {
	    print "<br>&nbsp;&nbsp;&nbsp;&nbsp;$attribute=" . $ref->{$hostgroups}->{$attribute} . "\n";
	}
    }
}

#
# CollageEventQuery class methods
#
if (    ( $FORM_DATA{cqMethod} eq "getEventsForDevice" )
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
    my $ref = $t->getEventsForDevice( $getparam1, $getparam2, $getparam3, $getparam4 );
    foreach my $event ( keys %{$ref} ) {
	print "<br>&nbsp;&nbsp;Event=$event\n";
	foreach my $attribute ( keys %{ $ref->{$event} } ) {
	    print "<br>&nbsp;&nbsp;&nbsp;&nbsp;$attribute=" . $ref->{$event}->{$attribute} . "\n";
	}
    }
}

if ( $FORM_DATA{cqMethod} eq "getEventsForService" ) {
    print "<br>Sample getEventsForService method\n";
    my $getparam2 = "LastInsertDate";
    my $getparam3 = "2005-05-01 00:00:00";
    my $getparam4 = "2005-05-12 00:00:00";
    my $getparam5 = "nagios";
    my $getparam6 = "localhost";
    print "<br>Getting events for host $getparam5, service $getparam6, $getparam2 from $getparam3 to $getparam4.\n";
    my $ref = $t->getEventsForService( $getparam5, $getparam6, $getparam2, $getparam3, $getparam4 );

    foreach my $event ( keys %{$ref} ) {
	print "<br>&nbsp;&nbsp;Event=$event\n";
	foreach my $attribute ( keys %{ $ref->{$event} } ) {
	    print "<br>&nbsp;&nbsp;&nbsp;&nbsp;$attribute=" . $ref->{$event}->{$attribute} . "\n";
	}
    }
}

if ( $FORM_DATA{cqMethod} eq "getEventsForHost" ) {
    print "<br>Sample getEventsForHost method\n";
    my $getparam2 = "LastInsertDate";
    my $getparam3 = "2005-05-01 00:00:00";
    my $getparam4 = "2005-05-12 00:00:00";
    my $getparam5 = "nagios";
    print "<br>Getting events for host $getparam5, $getparam2 from $getparam3 to $getparam4.\n";
    my $ref = $t->getEventsForHost( $getparam5, $getparam2, $getparam3, $getparam4 );
    foreach my $event ( keys %{$ref} ) {
	print "<br>&nbsp;&nbsp;Event=$event\n";
	foreach my $attribute ( keys %{ $ref->{$event} } ) {
	    print "<br>&nbsp;&nbsp;&nbsp;&nbsp;$attribute=" . $ref->{$event}->{$attribute} . "\n";
	}
    }
}
$t->destroy();

exit;

__END__


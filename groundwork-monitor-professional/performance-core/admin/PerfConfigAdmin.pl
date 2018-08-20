#!/usr/local/groundwork/bin/perl --
#
# Copyright 2007 GroundWork Open Source, Inc. (“GroundWork”)  
# All rights reserved. This program is free software; you can redistribute it and/or 
# modify it under the terms of the GNU General Public License version 2 as published 
# by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY 
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
# PARTICULAR PURPOSE.  See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with this 
# program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, 
# Fifth Floor, Boston, MA 02110-1301, USA.
#
use strict;
use Time::Local;
use CollageQuery ;
use DBI;

my $isPortal = 0;

my $stylesheethtmlref="";
my $thisprogram = undef;
my $nonportalprogram = "PerfConfigAdmin.pl";
my $portalprogram = "/collage/portal/perf-config-admin.psml?file=PerfConfigAdmin.pl";

if ($isPortal) {
	$thisprogram = $portalprogram;
} else {
	$thisprogram = $nonportalprogram;
	print "Content-type: text/html \n\n";
}
my $request_method = $ENV{'REQUEST_METHOD'};
my $form_info;
if ($request_method eq "GET") {
	$form_info = $ENV{'QUERY_STRING'};
#	$form_info =~ s/%([\dA-Fa-f][\dA-Fa-f])/pack("C",hex($1))/eg;
} elsif ($request_method eq "POST") {
	my $size_of_form_info = $ENV{'CONTENT_LENGTH'};
	read (STDIN, $form_info,$size_of_form_info);
} else {
	print "500 Server Error. Server uses unsupported method";
	$ENV{'REQUEST_METHOD'}="GET";
	$ENV{'QUERY_STRING'}=$ARGV[0];
	$form_info=$ARGV[0];
}
my %FORM_DATA;
my ($key,$value);
foreach my $key_value (split(/&/,$form_info)) {
	($key,$value) = split(/=/,$key_value);
	$value=~tr/+/ /;
	$value=~ s/%([\dA-Fa-f][\dA-Fa-f])/pack("C",hex($1))/eg;
	if (defined($FORM_DATA{$key})) {
		$FORM_DATA{$key}=join("\0",$FORM_DATA{$key},$value);
	} else {
		$FORM_DATA{$key}=$value;
	}
}

print "
	<HTML>
	<HEAD>
	<META HTTP-EQUIV='Expires' CONTENT='0'>
	<META HTTP-EQUIV='Pragma' CONTENT='no-cache'>
	<TITLE>Groundwork Performance Configuration Administration</TITLE>
	<link rel='stylesheet' type='text/css' href='$stylesheethtmlref'>
";
printstyles();

print qq(
	<SCRIPT language="JavaScript">
	function changePage (page) {	;
		if (page.length) {			;
			location.href=page		;
		}							;
	}								;
	function updatePage (attrName,attrValue) {		;
		page="$thisprogram?$form_info&"+attrName+"="+attrValue	;
		if (page.length) {			;
			location.href=page		;
		}							;
	}								;
	</SCRIPT>						
);

print '
	</HEAD>
	<BODY class=insight>
	<DIV id=container>
';
print '
	<DIV id=logo></DIV>
	<DIV id=pagetitle>
';
#if (!$isPortal) {		# Don't print header if invoked from the portal
#	print '<H1 class=insight>GroundWork Performance Configuration Administration</H1>';
#}
print '</DIV>';

my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
$year += 1900;
my $month=qw(January February March April May June July August September October November December)[$mon];
my $timestring= sprintf "%02d:%02d:%02d",$hour,$min,$sec;
my $thisday = qw(Sunday Monday Tuesday Wednesday Thursday Friday Saturday)[$wday];
print "<FORM name=selectForm class=formspace action=$nonportalprogram method=get>";
print "<TABLE class=insightcontrolpanel><TBODY><tr class=insightgray-bg>";
#print "<TH class=insight colSpan=2>$thisday, $month $mday, $year. $timestring</TH></TR>";    
print "<TH class=insight colSpan=2>Performance Configuration Administration</TH></TR>";    
print "</TABLE>";
my ($Database_Name,$Database_Host,$Database_User,$Database_Password) = CollageQuery::readGroundworkDBConfig("monarch");
#my $Database_Name = "performanceconfig";
#my $Database_Host = "localhost";
#my $Database_User = "root";
#my $Database_Password = "";
my $dbh = DBI->connect("DBI:mysql:$Database_Name:$Database_Host", $Database_User, $Database_Password) 
	or die "Can't connect to database $Database_Name. Error:".$DBI::errstr;

if ($FORM_DATA{cmd} eq "list" or !$FORM_DATA{cmd}) {
	list();
} elsif ($FORM_DATA{cmd} eq "modify" ) {
	modify();
} elsif ($FORM_DATA{cmd} eq "copy" ) {
	copy();
} elsif ($FORM_DATA{cmd} eq "new" ) {
	new();
} elsif ($FORM_DATA{cmd} eq "delete" ) {
	deleteentry();
} elsif ($FORM_DATA{cmd} eq "update" ) {
	update();
} elsif ($FORM_DATA{cmd} eq "add" ) {
	add();
} elsif ($FORM_DATA{cmd} eq "exportform" ) {
	export_form();
} elsif ($FORM_DATA{cmd} eq "export" ) {
	export();
} elsif ($FORM_DATA{cmd} eq "exportall" ) {
	export_all();
}
print "</FORM>";
$dbh->disconnect();
exit;
#
#	List all entries 
#
sub list {
	print "	<INPUT class=orangebutton type=button value='Create New Entry' onClick='changePage(\"$thisprogram?cmd=new\")'>";
	print "	<INPUT class=orangebutton type=button value='Export All' onClick='changePage(\"$thisprogram?cmd=exportall\")'>";
	my %checked = (); 
	$checked{$FORM_DATA{id}}="SELECTED";
	my $query = "SELECT performanceconfig_id,service,host FROM `performanceconfig` ORDER BY service,host";
	my $sth = $dbh->prepare($query);
	$sth->execute() or die  $@;
	print "<p class=insight>Select Service-Host entry:&nbsp;&nbsp; 
			<select name=id class=insight onChange=changePage('$thisprogram?cmd=list&id='+this.options[this.selectedIndex].value)>
			<option class=insight $checked{''} value=''>Show All
	";
	while (my $row=$sth->fetchrow_hashref()) { 
			my $id = $$row{performanceconfig_id};
			print "<option class=insight $checked{$id} value='$id'>".$$row{service}." - ".$$row{host};
	}
	print "</select>";
	print "<br><br>";
	my $query;
	if ($FORM_DATA{id}) {
		$query = "SELECT * FROM `performanceconfig` where performanceconfig_id =$FORM_DATA{id} ";
	} else {
		$query = "SELECT * FROM `performanceconfig` ORDER BY service,host";
	}
	my $sth = $dbh->prepare($query);
	$sth->execute() or die  $@;
	while (my $row=$sth->fetchrow_hashref()) { 
			my $id = $$row{performanceconfig_id};
			my $host = $$row{host};
			my $service = $$row{service};
			my $service_regx = "OFF";
			if ($$row{service_regx}) {
				 $service_regx = "ON";
			}
			my $label = $$row{label};
			my $rrdname = $$row{rrdname};
			my $rrdcreatestring = $$row{rrdcreatestring};
			my $rrdupdatestring = $$row{rrdupdatestring};
			my $perfidstring = $$row{perfidstring};
			my $graphcgi = $$row{graphcgi};
			my $parseregx = $$row{parseregx};
			my $parseregx_first = "OFF";
			if ($$row{parseregx_first}) {
				 $parseregx_first = "ON";
			}
			my $enable = "OFF";
			if ($$row{enable}) {
				 $enable = "ON";
			}
			print "<table class=insightcontrolpanel>";
			print "<tr><td class=insight><b>Graph Label:</b></td><td class=insight>$label</td></tr>";
			print "<tr><td class=insight><b>Service:</b></td><td class=insight>$service</td></tr>";
			print "<tr><td class=insight><b>Use Service as a Regular Expression</b></td><td class=insight>$service_regx</td></tr>";
			print "<tr><td class=insight><b>Host:</b></td><td class=insight>$host</td></tr>";
			#print "<tr><td class=insight><b>Plugin ID:</b></td><td class=insight>$perfidstring</td></tr>";
			print "<tr><td class=insight><b>Status Text Parsing Regular Expression:</b></td><td class=insight>$parseregx</td></tr>";
			print "<tr><td class=insight><b>Use Status Text Parsing instead of Performance Data</b></td><td class=insight>$parseregx_first</td></tr>";
			print "<tr><td class=insight><b>RRD Name</b></td><td class=insight>$rrdname</td></tr>";
			print "<tr><td class=insight><b>RRD Create Command</b></td><td class=insight>$rrdcreatestring</td></tr>";
			print "<tr><td class=insight><b>RRD Update Command</b></td><td class=insight>$rrdupdatestring</td></tr>";
			print "<tr><td class=insight><b>Custom RRDtool Graph Command</b></td><td class=insight>$graphcgi</td></tr>";
			print "<tr><td class=insight><b>Enable</b></td><td class=insight>$enable</td></tr>";
			print "<tr><td class=insight colspan=2 align=center>
					<INPUT class=orangebutton type=button value='Modify' onClick='changePage(\"$thisprogram?cmd=modify&id=$id\")'>
					<INPUT class=orangebutton type=button value='Copy' onClick='changePage(\"$thisprogram?cmd=copy&id=$id\")'>
					<INPUT class=orangebutton type=button value='Delete' onClick='changePage(\"$thisprogram?cmd=delete&id=$id\")'>
					<INPUT class=orangebutton type=button value='Export' onClick='changePage(\"$thisprogram?cmd=exportform&id=$id\")'>
					</td></tr>";
			print "</tr>";
			print "</table>";
			print "<br>";
	}
}

#
#	Modify the entries 
#
sub modify {
	my $query = "SELECT * FROM `performanceconfig` WHERE performanceconfig_id=$FORM_DATA{id}";
	my $sth = $dbh->prepare($query);
	$sth->execute() or die  $@;
	my %checked;
	$checked{1} = "CHECKED";
	while (my $row=$sth->fetchrow_hashref()) { 
			my $id = $$row{performanceconfig_id};
			my $host = $$row{host};
			my $service = $$row{service};
			my $service_regx = $$row{service_regx};
			my $label = $$row{label};
			my $rrdname = $$row{rrdname};
			my $rrdcreatestring = $$row{rrdcreatestring};
			my $rrdupdatestring = $$row{rrdupdatestring};
			my $perfidstring = $$row{perfidstring};
			my $graphcgi = $$row{graphcgi};
			my $parseregx = $$row{parseregx};
			my $parseregx_first = $$row{parseregx_first};
			my $enable = $$row{enable};
			print "<input type=hidden name=id value=$id>";
			print "<input type=hidden name=cmd value=update>";
			print "<table class=insightcontrolpanel>";
			print "<tr><td class=insight><b>Graph Label:</b></td><td class=insight>
						<INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=label VALUE=\"$label\"></td></tr>";
			print "<tr><td class=insight><b>Service:</b></td><td class=insight>
						<INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=service VALUE=\"$service\"></td></tr>";
			print "<tr><td class=insight><b>Use Service as a Regular Expression:</b></td><td class=insight>
						<INPUT CLASS=insight TYPE=checkbox NAME=service_regx VALUE=1 $checked{$service_regx}></td></tr>";
			print "<tr><td class=insight><b>Host:</b></td><td class=insight>
						<INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=host VALUE=\"$host\"></td></tr>";
			#print "<tr><td class=insight><b>Plugin ID:</b></td><td class=insight>
			#			<INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=perfidstring VALUE=\"$perfidstring\"></td></tr>";
			print "<tr><td class=insight><b>Status Text Parsing Regular Expression:</b></td><td class=insight>
						<INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=parseregx VALUE=\"$parseregx\"></td></tr>";
			print "<tr><td class=insight><b>Use Status Text Parsing instead of Performance Data:</b></td><td class=insight>
						<INPUT CLASS=insight TYPE=checkbox NAME=parseregx_first VALUE=1 $checked{$parseregx_first}></td></tr>";
			print "<tr><td class=insight><b>RRD Name</b></td><td class=insight>
						<INPUT CLASS=insight size=100 maxlength=256 TYPE=TEXT NAME=rrdname VALUE=\"$rrdname\"></td></tr>";
			print "<tr><td class=insight><b>RRD Create Command</b></td><td class=insight>
						<TEXTAREA CLASS=insight cols=100 rows=3 NAME=rrdcreatestring>$rrdcreatestring</TEXTAREA></td></tr>";
			print "<tr><td class=insight><b>RRD Update Command</b></td><td class=insight>
						<TEXTAREA CLASS=insight cols=100 rows=3 NAME=rrdupdatestring>$rrdupdatestring</TEXTAREA></td></tr>";
			print "<tr><td class=insight><b>Custom RRDtool Graph Command</b></td><td class=insight>
						<TEXTAREA CLASS=insight cols=100 rows=12 NAME=graphcgi>$graphcgi</TEXTAREA></td></tr>";
			print "<tr><td class=insight><b>Enable:</b></td><td class=insight>
						<INPUT CLASS=insight TYPE=checkbox NAME=enable VALUE=1 $checked{$enable}></td></tr>";
			print "<tr><td class=insight colspan=2 align=center>
					<INPUT class=orangebutton type=submit value='Update'>
					<INPUT class=orangebutton type=button value='Cancel' onClick='changePage(\"$thisprogram?cmd=list\")'>
					</td></tr>";
			print "</tr>";
			print "</table>";
			print "<br>";
	}
}
#
#	Copy the entries 
#
sub copy {
	my $query = "SELECT * FROM `performanceconfig` WHERE performanceconfig_id=$FORM_DATA{id}";
	my $sth = $dbh->prepare($query);
	$sth->execute() or die  $@;
	my %checked;
	$checked{1} = "CHECKED";
	while (my $row=$sth->fetchrow_hashref()) { 
			my $id = $$row{performanceconfig_id};
			my $host = $$row{host};
			my $service = $$row{service};
			my $service_regx = $$row{service_regx};
			my $label = $$row{label};
			my $rrdname = $$row{rrdname};
			my $rrdcreatestring = $$row{rrdcreatestring};
			my $rrdupdatestring = $$row{rrdupdatestring};
			my $perfidstring = $$row{perfidstring};
			my $graphcgi = $$row{graphcgi};
			my $parseregx = $$row{parseregx};
			my $parseregx_first = $$row{parseregx_first};
			my $enable = $$row{enable};
			print "<input type=hidden name=cmd value=add>";
			print "<table class=insightcontrolpanel>";
			print "<tr><td class=insight><b>Graph Label:</b></td><td class=insight>
						<INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=label VALUE=\"$label\"></td></tr>";
			print "<tr><td class=insight><b>Service:</b></td><td class=insight>
						<INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=service VALUE=\"$service\"></td></tr>";
			print "<tr><td class=insight><b>Use Service as a Regular Expression:</b></td><td class=insight>
						<INPUT CLASS=insight TYPE=checkbox NAME=service_regx VALUE=1 $checked{$service_regx}></td></tr>";
			print "<tr><td class=insight><b>Host:</b></td><td class=insight>
						<INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=host VALUE=\"$host\"></td></tr>";
			#print "<tr><td class=insight><b>Plugin ID:</b></td><td class=insight>
			#			<INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=perfidstring VALUE=\"$perfidstring\"></td></tr>";
			print "<tr><td class=insight><b>Status Text Parsing Regular Expression:</b></td><td class=insight>
						<INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=parseregx VALUE=\"$parseregx\"></td></tr>";
			print "<tr><td class=insight><b>Use Status Text Parsing instead of Performance Data:</b></td><td class=insight>
						<INPUT CLASS=insight TYPE=checkbox NAME=parseregx_first VALUE=1 $checked{$parseregx_first}></td></tr>";
			print "<tr><td class=insight><b>RRD Name</b></td><td class=insight>
						<INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=rrdname VALUE=\"$rrdname\"></td></tr>";
			print "<tr><td class=insight><b>RRD Create Command</b></td><td class=insight>
						<TEXTAREA CLASS=insight cols=100 rows=3 NAME=rrdcreatestring>$rrdcreatestring</TEXTAREA></td></tr>";
			print "<tr><td class=insight><b>RRD Update Command</b></td><td class=insight>
						<TEXTAREA CLASS=insight cols=100 rows=3 NAME=rrdupdatestring>$rrdupdatestring</TEXTAREA></td></tr>";
			print "<tr><td class=insight><b>Custom RRDtool Graph Command</b></td><td class=insight>
						<TEXTAREA CLASS=insight cols=100 rows=12 NAME=graphcgi>$graphcgi</TEXTAREA></td></tr>";
			print "<tr><td class=insight><b>Enable:</b></td><td class=insight>
						<INPUT CLASS=insight TYPE=checkbox NAME=enable VALUE=1 $checked{$enable}></td></tr>";
			print "<tr><td class=insight colspan=2 align=center>
					<INPUT class=orangebutton type=submit value='Create Copy'>
					<INPUT class=orangebutton type=button value='Cancel' onClick='changePage(\"$thisprogram?cmd=list\")'>
					</td></tr>";
			print "</tr>";
			print "</table>";
			print "<br>";
	}
}
#
#	Update an existing entry
#
sub update {
	my $query = "SELECT performanceconfig_id FROM performanceconfig WHERE (host=\"$FORM_DATA{host}\" AND service=\"$FORM_DATA{service}\") ";
	my $sth = $dbh->prepare($query);
	$sth->execute() or die  $@;
	my $id = undef;
	while (my $row=$sth->fetchrow_hashref()) { 
			$id = $$row{performanceconfig_id};
	}
	if (($id != $FORM_DATA{id}) and defined($id)) {
		print "<p class=insight>";
		print "ERROR. Performance configuration already exists for host $FORM_DATA{host} and service $FORM_DATA{service}.";
		print "<br>Duplicate entries are not permitted. Delete the existing entry before adding this entry.";
		print "<br><INPUT class=orangebutton type=button value='Continue' onClick='changePage(\"$thisprogram?cmd=list\")'>";
		return;
	}
#	$FORM_DATA{parseregx} =~ s/\\/\\\\/g;
	$FORM_DATA{parseregx} = $dbh->quote($FORM_DATA{parseregx});
	if ($FORM_DATA{graphcgi} =~ /^'/ && $FORM_DATA{graphcgi} =~ /'$/) {
#		print "stripping quotes and requotng... \n";
		$FORM_DATA{graphcgi} =~ s/^'//;
		$FORM_DATA{graphcgi} =~ s/'$//;
		$FORM_DATA{graphcgi} = $dbh->quote($FORM_DATA{graphcgi});
	} else {
#		 print " quoting... \n";
		$FORM_DATA{graphcgi} = $dbh->quote($FORM_DATA{graphcgi});
	}
#	$FORM_DATA{graphcgi} =~ s/\r//g;
#	$FORM_DATA{graphcgi} =~ s/\\\n//g;
	my $query = "UPDATE performanceconfig SET ".
					"label=\"$FORM_DATA{label}\",". 
					"host=\"$FORM_DATA{host}\",". 
					"service=\"$FORM_DATA{service}\",". 
					"service_regx=\"$FORM_DATA{service_regx}\", ". 
					"perfidstring=\"$FORM_DATA{perfidstring}\",". 
					"parseregx=$FORM_DATA{parseregx},". 
					"parseregx_first=\"$FORM_DATA{parseregx_first}\", ". 
					"rrdname=\"$FORM_DATA{rrdname}\",". 
					"rrdcreatestring=\"$FORM_DATA{rrdcreatestring}\",". 
					"rrdupdatestring=\"$FORM_DATA{rrdupdatestring}\",". 
					"graphcgi=\"$FORM_DATA{graphcgi}\", ". 
					"type=\"nagios\", ". 
					"enable=\"$FORM_DATA{enable}\" ". 
					"WHERE performanceconfig_id=$FORM_DATA{id} "
				;
#	print "<br>SQL=$query<br>";
	my $sth = $dbh->prepare($query);
	$sth->execute() or die  $@;
	print "<p class=insight>";
	print "Performance configuration for host $FORM_DATA{host} and service $FORM_DATA{service} updated.";
	print "<br><INPUT class=orangebutton type=button value='Continue' onClick='changePage(\"$thisprogram?cmd=list\")'>";
}
#
#	Delete an existing entry
#
sub deleteentry {
	my $query = "DELETE FROM performanceconfig WHERE performanceconfig_id=$FORM_DATA{id}";
	my $sth = $dbh->prepare($query);
	$sth->execute() or die  $@;
	print "<p class=insight>";
	print "Entry deleted.";
	print "<br><INPUT class=orangebutton type=button value='Continue' onClick='changePage(\"$thisprogram?cmd=list\")'>";
}

#
#	Add a new entry form
#
sub new {
			print "<input type=hidden name=cmd value=add>";
			print "<table class=insightcontrolpanel>";
			print "<tr><td class=insight><b>Graph Label:</b></td><td class=insight>
						<INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=label VALUE=\"\"></td></tr>";
			print "<tr><td class=insight><b>Service:</b></td><td class=insight>
						<INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=service VALUE=\"\"></td></tr>";
			print "<tr><td class=insight><b>Use Service as a Regular Expression:</b></td><td class=insight>
						<INPUT CLASS=insight TYPE=checkbox NAME=service_regx VALUE=1></td></tr>";
			print "<tr><td class=insight><b>Host:</b></td><td class=insight>
						<INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=host VALUE=\"\"></td></tr>";
			#print "<tr><td class=insight><b>Plugin ID:</b></td><td class=insight>
			#			<INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=perfidstring VALUE=\"\"></td></tr>";
			print "<tr><td class=insight><b>Status Text Parsing Regular Expression:</b></td><td class=insight>
						<INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=parseregx VALUE=\"\"></td></tr>";
			print "<tr><td class=insight><b>Use Status Text Parsing instead of Performance Data:</b></td><td class=insight>
						<INPUT CLASS=insight TYPE=checkbox NAME=parseregx_first VALUE=1></td></tr>";
			print "<tr><td class=insight><b>RRD Name</b></td><td class=insight>
						<INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=rrdname VALUE=\"\"></td></tr>";
			print "<tr><td class=insight><b>RRD Create Command</b></td><td class=insight>
						<TEXTAREA CLASS=insight cols=100 rows=3 NAME=rrdcreatestring></TEXTAREA></td></tr>";
			print "<tr><td class=insight><b>RRD Update Command</b></td><td class=insight>
						<TEXTAREA CLASS=insight cols=100 rows=3 NAME=rrdupdatestring></TEXTAREA></td></tr>";
			print "<tr><td class=insight><b>Custom RRDtool Graph Command</b></td><td class=insight>
						<TEXTAREA CLASS=insight cols=100 rows=12 NAME=graphcgi></TEXTAREA></td></tr>";
			print "<tr><td class=insight><b>Enable:</b></td><td class=insight>
						<INPUT CLASS=insight TYPE=checkbox NAME=enable VALUE=1 CHECKED></td></tr>";
			print "<tr><td class=insight colspan=2 align=center>
					<INPUT class=orangebutton type=submit value='Add'>
					<INPUT class=orangebutton type=button value='Cancel' onClick='changePage(\"$thisprogram?cmd=list\")'>
					</td></tr>";
			print "</tr>";
			print "</table>";
			print "<br>";
}
#
#	Add an newing entry
#
sub add {
	my $query = "SELECT performanceconfig_id FROM performanceconfig WHERE (host=\"$FORM_DATA{host}\" AND service=\"$FORM_DATA{service}\") ";
	my $sth = $dbh->prepare($query);
	$sth->execute() or die  $@;
	my $id = undef;
	while (my $row=$sth->fetchrow_hashref()) { 
			$id = $$row{performanceconfig_id};
	}
	if ($id) {
		print "<p class=insight>";
		print "ERROR. Performance configuration already exists for host $FORM_DATA{host} and service $FORM_DATA{service}.";
		print "<br>Duplicate entries are not permitted. Delete the existing entry before adding this entry.";
		print "<br><INPUT class=orangebutton type=button value='Continue' onClick='changePage(\"$thisprogram?cmd=list\")'>";
		return;
	}
	# $FORM_DATA{parseregx} =~ s/\\/\\\\/g;
	$FORM_DATA{parseregx} = $dbh->quote($FORM_DATA{parseregx});
	my $query = "INSERT INTO performanceconfig (label,host,service,service_regx,perfidstring,parseregx,parseregx_first,rrdname,rrdcreatestring,rrdupdatestring,graphcgi,type,enable) VALUES (".
					"\"$FORM_DATA{label}\",". 
					"\"$FORM_DATA{host}\",". 
					"\"$FORM_DATA{service}\",". 
					"\"$FORM_DATA{service_regx}\",". 
					"\"$FORM_DATA{perfidstring}\",". 
					"$FORM_DATA{parseregx},". 
					"\"$FORM_DATA{parseregx_first}\",". 
					"\"$FORM_DATA{rrdname}\",". 
					"\"$FORM_DATA{rrdcreatestring}\",". 
					"\"$FORM_DATA{rrdupdatestring}\",". 
					"\"$FORM_DATA{graphcgi}\", ".
					"\"nagios\", ".
					"\"$FORM_DATA{enable}\" ".
				");";
	$dbh->do($query) or die  $@;
#	print "<br>Query=$query<br>";
	print "<p class=insight>";
	print "Performance configuration for host $FORM_DATA{host} and service $FORM_DATA{service} added.";
	print "<br><INPUT class=orangebutton type=button value='Continue' onClick='changePage(\"$thisprogram?cmd=list\")'>";
}

sub export_form {
	my $query = "SELECT * FROM `performanceconfig` WHERE performanceconfig_id=$FORM_DATA{id}";
	my $sth = $dbh->prepare($query);
	$sth->execute() or die  $@;
	my %checked;
	$checked{1} = "CHECKED";
	while (my $row=$sth->fetchrow_hashref()) { 
			my $id = $$row{performanceconfig_id};
			my $host = $$row{host};
			my $service = $$row{service};
			my $service_regx = $$row{service_regx};
			my $label = $$row{label};
			my $rrdname = $$row{rrdname};
			my $rrdcreatestring = $$row{rrdcreatestring};
			my $rrdupdatestring = $$row{rrdupdatestring};
			my $perfidstring = $$row{perfidstring};
			my $graphcgi = $$row{graphcgi};
			my $parseregx = $$row{parseregx};
			my $parseregx_first = $$row{parseregx_first};
			my $enable = $$row{enable};
			print "<p>This configuration will be written to directory /tmp.</p>";
			print "<input type=hidden name=id value=$id>";
			print "<input type=hidden name=cmd value=export>";
			print "<table class=insightcontrolpanel>";
			print "<tr><td class=insight><b>Export File Name:</b></td><td class=insight>
						<INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=exportfile VALUE=\"perfconfig-$service.xml\"></td></tr>";
			print "<tr><td class=insight><b>Graph Label:</b></td><td class=insight>$label</td></tr>";
			print "<tr><td class=insight><b>Service:</b></td><td class=insight>$service</td></tr>";
			print "<tr><td class=insight><b>Use Service as a Regular Expression</b></td><td class=insight>$service_regx</td></tr>";
			print "<tr><td class=insight><b>Host:</b></td><td class=insight>$host</td></tr>";
			print "<tr><td class=insight><b>Plugin ID:</b></td><td class=insight>$perfidstring</td></tr>";
			print "<tr><td class=insight><b>Status Text Parsing Regular Expression:</b></td><td class=insight>$parseregx</td></tr>";
			print "<tr><td class=insight><b>Use Status Text Parsing instead of Performance Data</b></td><td class=insight>$parseregx_first</td></tr>";
			print "<tr><td class=insight><b>RRD Name</b></td><td class=insight>$rrdname</td></tr>";
			print "<tr><td class=insight><b>RRD Create Command</b></td><td class=insight>$rrdcreatestring</td></tr>";
			print "<tr><td class=insight><b>RRD Update Command</b></td><td class=insight>$rrdupdatestring</td></tr>";
			print "<tr><td class=insight><b>Custom RRDtool Graph Command</b></td><td class=insight>$graphcgi</td></tr>";
			print "<tr><td class=insight><b>Enable</b></td><td class=insight>$enable</td></tr>";
			print "<tr><td class=insight colspan=2 align=center>
					<INPUT class=orangebutton type=submit value='Export'>
					<INPUT class=orangebutton type=button value='Cancel' onClick='changePage(\"$thisprogram?cmd=list\")'>
					</td></tr>";
			print "</tr>";
			print "</table>";
			print "<br>";
	}
}
sub export {
	my $query = "SELECT * FROM `performanceconfig` WHERE performanceconfig_id=$FORM_DATA{id}";
	my $sth = $dbh->prepare($query);
	$sth->execute() or die  $@;
	my %checked;
	$checked{1} = "CHECKED";
	while (my $row=$sth->fetchrow_hashref()) { 
			my $id = $$row{performanceconfig_id};
			my $host = $$row{host};
			my $service = $$row{service};
			my $service_regx = $$row{service_regx};
			my $rrdname = $$row{rrdname};
			my $rrdcreatestring = $$row{rrdcreatestring};
			my $rrdupdatestring = $$row{rrdupdatestring};
			my $perfidstring = $$row{perfidstring};
			my $graphcgi = $$row{graphcgi};
			my $parseregx = $$row{parseregx};
			my $parseregx_first = $$row{parseregx_first};
			my $enable = $$row{enable};
			my $label = $$row{label};

			my $xmlstring =  
				"<groundwork_performance_configuration>\n".
				"<service_profile name=\"$service profile\">\n".
				"<graph name=\"graph\">\n".
				"<host>$host</host>\n".
				"<service regx=\"$service_regx\"><![CDATA[$service]]></service>\n".
				"<type>nagios</type>\n".
				"<enable>$enable</enable>\n".
				"<label>$label</label>\n".
				"<rrdname><![CDATA[$rrdname]]></rrdname>\n".
				"<rrdcreatestring><![CDATA[$rrdcreatestring]]></rrdcreatestring>\n".
				"<rrdupdatestring><![CDATA[$rrdupdatestring]]></rrdupdatestring>\n".
				"<graphcgi><![CDATA[$graphcgi]]></graphcgi>\n".
				"<parseregx first=\"$parseregx_first\"><![CDATA[$parseregx]]></parseregx>\n".
				"<perfidstring>$perfidstring</perfidstring>\n".
				"</graph>\n".
				"</service_profile>\n".
				"</groundwork_performance_configuration>"  ;
			open(OUT,">/tmp/$FORM_DATA{exportfile}") or die "ERROR Can't open file /tmp/$FORM_DATA{exportfile}";
			print OUT $xmlstring;
			close OUT;
			$xmlstring =~ s/</&lt;/g;
			print "<p>The following XML string was written to directory /tmp in file $FORM_DATA{exportfile}.</p>";
			print "<p><PRE>$xmlstring</PRE>";
			print "<INPUT class=orangebutton type=button value='Return to list' onClick='changePage(\"$thisprogram?cmd=list\")'>";
			print "<br>";
	}
}

sub export_all {
	my $query = "SELECT * FROM `performanceconfig`";
	my $sth = $dbh->prepare($query);
	$sth->execute() or die  $@;
	open(OUT,">/tmp/perfconfig-ALL.xml") or die "ERROR Can't open file /tmp/perfconfig-ALL.xml";
	print OUT "<groundwork_performance_configuration>\n";
	while (my $row=$sth->fetchrow_hashref()) { 
			my $id = $$row{performanceconfig_id};
			my $host = $$row{host};
			my $service = $$row{service};
			my $service_regx = $$row{service_regx};
			my $rrdname = $$row{rrdname};
			my $rrdcreatestring = $$row{rrdcreatestring};
			my $rrdupdatestring = $$row{rrdupdatestring};
			my $perfidstring = $$row{perfidstring};
			my $graphcgi = $$row{graphcgi};
			my $parseregx = $$row{parseregx};
			my $parseregx_first = $$row{parseregx_first};
			my $enable = $$row{enable};
			my $label = $$row{label};
			my $xmlstring =  
				"<service_profile name=\"$service profile\">\n".
				"<graph name=\"graph\">\n".
				"<host>$host</host>\n".
				"<service regx=\"$service_regx\"><![CDATA[$service]]></service>\n".
				"<type>nagios</type>\n".
				"<enable>$enable</enable>\n".
				"<label>$label</label>\n".
				"<rrdname><![CDATA[$rrdname]]></rrdname>\n".
				"<rrdcreatestring><![CDATA[$rrdcreatestring]]></rrdcreatestring>\n".
				"<rrdupdatestring><![CDATA[$rrdupdatestring]]></rrdupdatestring>\n".
				"<graphcgi><![CDATA[$graphcgi]]></graphcgi>\n".
				"<parseregx first=\"$parseregx_first\"><![CDATA[$parseregx]]></parseregx>\n".
				"<perfidstring>$perfidstring</perfidstring>\n".
				"</graph>\n".
				"</service_profile>\n";
			print OUT $xmlstring;
	}
	print OUT "</groundwork_performance_configuration>"  ;
	close OUT;
	print "<p>The exported configuration was written to directory /tmp in file perfconfig-ALL.xml.</p>";
	print "<INPUT class=orangebutton type=button value='Return to list' onClick='changePage(\"$thisprogram?cmd=list\")'>";
	print "<br>";
}






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
 border-spacing: 0px;
 empty-cells: show;
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
  border-spacing: 0;
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
td.insight {color: #000000; font-family:verdana, arial, sans-serif; font-size: 10; vertical-align: top; 
	border: 1px solid #666666;
	background-color: #D9D9D9;
	padding:2; 
	spacing:2; 
	}
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
input.insighttext {border: 0px solid #000099; font-family: verdana, arial, sans-serif; font-size: 9px; font-weight: bold; color: #000000;}
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

/* for orange buttons */
input.orangebutton {background-color:#FA840F; color:#ffffff; font-size:8pt; border:1px solid #000000; margin-top:10px;}

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
p.insight {
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
font-weight: normal;
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











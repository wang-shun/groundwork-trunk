#!/usr/local/groundwork/perl/bin/perl --
#
# Copyright (C) 2008-2013 GroundWork Open Source, Inc. ("GroundWork")
# All rights reserved. Use is subject to GroundWork commercial license terms.
#
use strict;
use Time::Local;
use DBI;
use CollageQuery;

my $stylesheethtmlref="";
my $thisprogram = "showprofiles.pl";

my $skip_macro_translation = 1;	# Don't translate user macros in command line

print "Content-type: text/html \n\n";
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
	<META HTTP-EQUIV='Cache-Control' CONTENT='no-cache'>
	<META HTTP-EQUIV='Pragma' CONTENT='no-cache'>
	<META HTTP-EQUIV='Expires' CONTENT='0'>
	<TITLE>Groundwork Configuration Profile Display</TITLE>
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
if (!$FORM_DATA{Portal}) {		# Don't print header if invoked from the portal
#	print '
#		<DIV id=logo></DIV>
#		<DIV id=pagetitle>
#		<H1 class=insight>Configuration Profile Display Utility</H1>
#		</DIV>
#	';
}
my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
$year += 1900;
my $month=qw(January February March April May June July August September October November December)[$mon];
my $timestring= sprintf "%02d:%02d:%02d",$hour,$min,$sec;
my $thisday = qw(Sunday Monday Tuesday Wednesday Thursday Friday Saturday)[$wday];

print "<FORM name=selectForm class=formspace action=$thisprogram method=get>";
print "<table class=insightcontrolpanel cellspacing=0><TBODY><tr class=insightgray-bg>";
print "<TH class=insight colSpan=2>$thisday, $month $mday, $year. $timestring</TH></TR>";
print "</TABLE>";

my ($dbname,$dbhost,$dbuser,$dbpass,$dbtype) = CollageQuery::readGroundworkDBConfig('monarch');
my $dsn = '';
if ( defined($dbtype) && $dbtype eq 'postgresql' ) {
    $dsn = "DBI:Pg:dbname=$dbname;host=$dbhost";
}
else {
    $dsn = "DBI:mysql:database=$dbname;host=$dbhost";
}
my $dbh = DBI->connect($dsn, $dbuser, $dbpass, { 'AutoCommit' => 1 })
	or die "Can't connect to database $dbname. Error: ".$DBI::errstr;

if ($FORM_DATA{cmd} eq "FormEntry" or !$FORM_DATA{cmd}) {
	FormEntry();
} elsif ($FORM_DATA{cmd} eq "Show Services" ) {
	ShowServices();
}
print "</FORM>";
$dbh->disconnect();
exit;
#
#	Initial Form
#
sub FormEntry {
	my %checked;
	print "<table class=insightcontrolpanel cellspacing=0>";
	my $query = "SELECT * FROM profiles_host";
	print "<tr><td class=insight><b>Select Host Profile:</b></td><td class=insight>
			<select name=hostprofileid class=insight>
			<option class=insight $checked{''} value=''>
	";
	my $sth = $dbh->prepare($query);
	$sth->execute() or die  $@;
	while (my $row=$sth->fetchrow_hashref()) {
			my $option = $$row{hostprofile_id};
			print "<option class=insight $checked{$option} value='$option'>$$row{name}</option>";
	}
	print "</select>";
	print "</td></tr>";


	my $query = "SELECT * FROM profiles_service ORDER BY name";
	print "<tr><td class=insight><b>Select Service Profile:</b></td><td class=insight>
			<select name=serviceprofileid class=insight>
			<option class=insight $checked{''} value=''>
	";
	my $sth = $dbh->prepare($query);
	$sth->execute() or die  $@;
	while (my $row=$sth->fetchrow_hashref()) {
			my $option = $$row{serviceprofile_id};
			print "<option class=insight $checked{$option} value='$option'>$$row{name}</option>";
	}
	print "</select>";
	print "</td></tr>";
	print "</td></tr>";
	print "</table>";
	print "<INPUT class=graybutton type=button value='Reset' onClick='changePage(\"$thisprogram?cmd=FormEntry\")'>
			<INPUT class=orangebutton type=submit value='Show Services' name=cmd>
			";
	print "<br>";
}

sub ShowServices {
	my %checked;
	print "<input type=hidden name=hostprofileid value=$FORM_DATA{hostprofileid}>";
	print "<input type=hidden name=serviceprofileid value=$FORM_DATA{serviceprofileid}>";
	print "<input type=hidden name=file value=$FORM_DATA{file}>";
	my $service = undef;
	my $hostprofilename = undef;
	my @serviceprofile_ids = ();
	my @servicename_ids = ();
	if ($FORM_DATA{hostprofileid}) {
		my $query = "SELECT ph.name,phps.serviceprofile_id FROM profiles_host as ph,profile_host_profile_service as phps ".
					"WHERE ph.hostprofile_id=$FORM_DATA{hostprofileid} and ph.hostprofile_id=phps.hostprofile_id";
		my $sth = $dbh->prepare($query);
		$sth->execute() or die  $@;
		while (my $row=$sth->fetchrow_hashref()) {
			$hostprofilename=$$row{name};
			push @serviceprofile_ids,$$row{serviceprofile_id};
		}

		if ($#serviceprofile_ids < 0) {
			print "	<INPUT class=orangebutton type=button value='Reset' onClick='changePage(\"$thisprogram?cmd=FormEntry\")'> ";
			print "<p>No service profiles are assigned to this host profile.</p>\n";
			return;
		}

		foreach my $serviceprofileid (@serviceprofile_ids) {
			print "<input type=hidden name=serviceprofileid value=$serviceprofileid>";
			my $query = "SELECT * FROM serviceprofile where serviceprofile_id=$serviceprofileid";
			my $sth = $dbh->prepare($query);
			$sth->execute() or die  $@;
			while (my $row=$sth->fetchrow_hashref()) {
				push @servicename_ids,$$row{servicename_id}
			}
			foreach my $id (@servicename_ids) {
				my $query = "SELECT * FROM service_names where servicename_id=$id";
				my $sth = $dbh->prepare($query);
				$sth->execute() or die  $@;
				while (my $row=$sth->fetchrow_hashref()) {
					$service->{$id}->{name} = $$row{name};
					$service->{$id}->{description} = $$row{description};
					$service->{$id}->{command_line} = $$row{command_line};
					$service->{$id}->{check_command} = $$row{check_command};
					$service->{$id}->{extinfo} = $$row{extinfo};
				}
			}
		}
	}


	if ($FORM_DATA{serviceprofileid}) {
		my $query = "SELECT * FROM serviceprofile where serviceprofile_id=$FORM_DATA{serviceprofileid}";
		my $sth = $dbh->prepare($query);
		$sth->execute() or die  $@;
		while (my $row=$sth->fetchrow_hashref()) {
#			print "<br>Adding $$row{servicename_id} to servicename_ids\n";
			push @servicename_ids,$$row{servicename_id};
		}
		foreach my $id (@servicename_ids) {
			my $query = "SELECT * FROM service_names where servicename_id=$id";
			my $sth = $dbh->prepare($query);
			$sth->execute() or die  $@;
			while (my $row=$sth->fetchrow_hashref()) {
				$service->{$id}->{name} = $$row{name};
				$service->{$id}->{description} = $$row{description};
				$service->{$id}->{command_line} = $$row{command_line};
				$service->{$id}->{check_command} = $$row{check_command};
				$service->{$id}->{extinfo} = $$row{extinfo};
			}
		}
	}


	print "<table class=insightcontrolpanel cellspacing=0>";
	print "<tr><td class=insight>Using Host Profile:</td><td class=insight>	";
	if ($FORM_DATA{hostprofileid}) {
		print "$hostprofilename";
	} else {
		print "None";
	}
	print "</td></tr>";
	print "<tr><td class=insight>Using Service Profile:</td><td class=insight>	";
	foreach my $id (keys %{$service}) {
		print $service->{$id}->{name}."<br>"; # Removed invalid descriptions (sparris) ." - ".$service->{$id}->{description}."<br>";
	}
	print "</td></tr>";
	print "</table>";
	print "<br><br>";
	print "<table class=insightcontrolpanel cellspacing=0>";
	print "<tr><th class=insight>Service Name</th>";
# sparris modified 2006-3-10
#	print "<th class=insight>Service Description</th>";
#	print "<th class=insight >Command name</th>";
	print "<th class=insight >Command line</th>";
	print "<th class=insight >Plugin Command Line</th>";
	print "<th class=insight >Graphing Program</th>";

	my $query = "SELECT * FROM setup WHERE name LIKE 'user%'";
	my $sth = $dbh->prepare($query);
	$sth->execute() or die  $@;
	my %user = ();
	while (my $row=$sth->fetchrow_hashref()) {
		if ($$row{name} =~ /^user/i) {
			$user{$$row{name}} = $$row{value};
		}
	}
	my $new_servcmdline = undef;
#	foreach my $id (keys %{$service}) {
	foreach my $id (sort {$service->{$a}->{name} cmp $service->{$b}->{name}} keys %{$service}) {
		print "<tr><td class=insight>".$service->{$id}->{name}."</td>";
# sparris modified 2006-3-10
#		print "<td class=insight>".$service->{$id}->{description}."</td>";
		if ($service->{$id}->{check_command}) {
			my $query = "SELECT * FROM commands where command_id=".$service->{$id}->{check_command};
			my $sth = $dbh->prepare($query);
			$sth->execute() or die  $@;
			while (my $row=$sth->fetchrow_hashref()) {
				$service->{$id}->{chk_command_line} = $$row{data};
				$service->{$id}->{command_name} = $$row{name};
			}
			$new_servcmdline = $service->{$id}->{chk_command_line};
			if ($service->{$id}->{chk_command_line} =~ /\[CDATA\[(.*?)\]]/) {
				$new_servcmdline = $1;
			}

			if (!$skip_macro_translation) {
				foreach my $key (keys %user) {
					$new_servcmdline =~ s/\$$key\$/$user{$key}/gi;
				}
			}
		}
#		print "<td class=insight>".$service->{$id}->{command_name}."</td>";

		if ($service->{$id}->{command_line}) {
			print "<td class=insight>".$service->{$id}->{command_line}."</td>";
		} else {
			print "<td class=insight>".$service->{$id}->{command_name}."</td>";
		}
		print "<td class=insight>".$new_servcmdline."</td>";
		if ($service->{$id}->{extinfo}) {
			my $query = "SELECT * FROM extended_service_info_templates where serviceextinfo_id=".$service->{$id}->{extinfo};
			my $sth = $dbh->prepare($query);
			$sth->execute() or die  $@;
			while (my $row=$sth->fetchrow_hashref()) {
				$service->{$id}->{extinfo_name} = $$row{name};
			}
			print "<td class=insight>".$service->{$id}->{extinfo_name}."</td>";
		} else {
			print "<td class=insight>&nbsp;</td>";
		}
	}
	print "</table>";
	print "<br>";
	print "	<INPUT class=orangebutton type=button value='Reset' onClick='changePage(\"$thisprogram?cmd=FormEntry\")'> ";
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
 border: 1px solid #FFFFFF; /* GroundWork Portal Interface: Gray (Table Fill 1px Outlines) */
 border-spacing: 0px;
 empty-cells: show;
}

table.insighttoplist {
 width: 100%;
 background-color: #F0F0F0; /* GroundWork Portal Interface: Background */
 border: 0px solid #666666; /* GroundWork Portal Interface: Gray (Table Fill 1px Outlines) */
}

th.insight {
  font-family: verdana, helvetica, arial, sans-serif;
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
  font-family: verdana, helvetica, arial, sans-serif;
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
td.insight {color: #000000; font-family: verdana, helvetica, arial, sans-serif; font-size: 10; vertical-align: top;
	border: 1px solid #666666;
	background-color: #D9D9D9;
	padding:2;
	spacing:2;
	}
td.insightleft {color: #000000; font-family: verdana, helvetica, arial, sans-serif; font-size: 10; vertical-align: top; text-align: left;}
td.insightcenter {color: #000000; font-family: verdana, helvetica, arial, sans-serif; font-size: 10; vertical-align: top;  text-align: center;}
tr.insight {color: #000000; font-family: verdana, helvetica, arial, sans-serif; font-size: 10;}
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

td.insighttitle {color: #000000; font-family: verdana, helvetica, arial, sans-serif; font-size: 18;font-weight: bold; color: #FA840F;}
td.insighthead {background-color: #55609A; font-family: verdana, helvetica, arial, sans-serif; font-size: 10; font-weight: bold; color: #ffffff;}
td.insightsubhead {background-color: #8089b9; font-family: verdana, helvetica, arial, sans-serif; font-size: 10; font-weight: bold; color: #ffffff;}
td.insightselected {background-color: #898787; font-family: verdana, helvetica, arial, sans-serif; font-size: 10; font-weight: bold; color: #ffffff;}
td.insightrow1 {background-color: #dcdcdc; font-family: verdana, helvetica, arial, sans-serif; font-size: 10;font-weight: bold;}
td.insightrow2 {background-color: #bfbfbf; font-family: verdana, helvetica, arial, sans-serif; font-size: 10;font-weight: bold;}
td.insightrow_lt {background-color: #f4f4f4; font-family: verdana, helvetica, arial, sans-serif; font-size: 10;font-weight: bold;}
td.insightrow_dk {background-color: #e2e2e2; font-family: verdana, helvetica, arial, sans-serif; font-size: 10;font-weight: bold;}
td.insighterror {background-color: #dcdcdc; font-family: verdana, helvetica, arial, sans-serif; font-size: 10;font-weight: bold; color:cc0000}
#input, textarea, select {border: 0px solid #000099; font-family: verdana, helvetica, arial, sans-serif; font-size: 9px; font-weight: bold; background-color: #ffffff; color: #000000;}
input.insight, textarea.insight, select.insight {border: 0px solid #000099; font-family: verdana, helvetica, arial, sans-serif; font-size: 9px; font-weight: bold; color: #000000;}
input.insighttext {border: 1px solid #000099; font-family: verdana, helvetica, arial, sans-serif; font-size: 9px; font-weight: bold; color: #000000;}
input.insightradio {border: 0px; background-color: #dcdcdc;}
input.insightcheckbox {border: 0px; background-color: #dcdcdc;}

#input.button {
#border: 1px solid #000000;
#border-style: solid;
#border-top-width: auto;
#border-right-width: auto;
#border-bottom-width: auto;
#border-left-width: auto:
#font-family: verdana, helvetica, arial, sans-serif; font-size: 11px; font-weight: bold; background-color: #898787; color: #ffffff;
#}

input.insightbutton {
	font: normal 10px/normal verdana, helvetica, arial, sans-serif;
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

input.graybutton {background-color:gray; color:#ffffff; font-size:8pt; border:1px solid #000000; margin-top:10px;}

input.insightbox {border: 0px;}

a.insighttop:link    {
color:#ffffff;
font-size: 12px;
font-family: verdana, helvetica, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}
a.insighttop:visited {
color:#ffffff;
font-size: 12px;
font-family: verdana, helvetica, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}
a.insighttop:active  {
color:#ffffff;
font-size: 12px;
font-family: verdana, helvetica, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}
a.insighttop:hover   {
color:#ffffff;
font-size: 12px;
font-family: verdana, helvetica, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}

a.insight:link    {
color:#414141;
font-size: 12px;
font-family: verdana, helvetica, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}
a.insight:visited {
color:#414141;
font-size: 12px;
font-family: verdana, helvetica, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}
a.insight:active  {
color:#919191;
font-size: 12px;
font-family: verdana, helvetica, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}
a.insight:hover   {
color:#919191;
font-size: 12px;
font-family: verdana, helvetica, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}

a.insightorange:link    {
color:#FA840F;
font-size: 12px;
font-family: verdana, helvetica, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}
a.insightorange:visited {
color:#FA840F;
font-size: 12px;
font-family: verdana, helvetica, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}
a.insightorange:active  {
color:#FA840F;
font-size: 12px;
font-family: verdana, helvetica, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}
a.insightorange:hover   {
color:#FA840F;
font-size: 12px;
font-family: verdana, helvetica, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}

/*Center paragraph*/
p.insightcenter {
color:#000;
font-family: verdana, helvetica, arial, sans-serif;
font-size: 12px;
font-weight: normal;
}

p {
color:#000;
font-family: verdana, helvetica, arial, sans-serif;
font-size: 12px;
font-weight: normal;
}


h1.insight {
color:#FA840F;
font-family: verdana, helvetica, arial, sans-serif;
font-size: 18px;
font-weight: 600;
}

h2.insight {
color:#55609A;
font-family: verdana, helvetica, arial, sans-serif;
font-size: 14px;
font-weight: bold;
}

h3.insight {
color:#000;
font-family: verdana, helvetica, arial, sans-serif;
font-size: 12px;
font-weight: normal;
}

h4.insight {
color:#FFFFFF;
font-family: verdana, helvetica, arial, sans-serif;
font-size: 12px;
font-weight: bold;
}

h5.insight {
color:#000;
font-family: verdana, helvetica, arial, sans-serif;
font-size: 16px;
font-style: italic;
font-weight: normal;
}

h6.insight {
color:#000;
font-family: verdana, helvetica, arial, sans-serif;
font-size: 18px;
font-weight: bold;
}

</style>
);
}


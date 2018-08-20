#!/usr/local/groundwork/perl/bin/perl --
#
# Copyright 2007-2016 GroundWork Open Source, Inc. ("GroundWork")
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


#use lib "/usr/local/rrdtool-1.0.41/lib/perl";
use lib "/usr/local/groundwork/lib";
use strict;
use RRDs;
use CGI;
use locale;

my $query = new CGI;
my $host = $query->param('host');
my $service = $query->param('service');
my $warning = $query->param('warning');
my $critical = $query->param('criticle');

my ($i, $start, $end, $defstring, $def, $line, $key, $errstr) = undef;
my ($graphfile, $graph0, $graph1, $graph2, $graph3, $graph4, $title) = undef;

#
#	Set Configuration Parameters
#
my $action = '/graphs/cgi-bin/percent_graph.cgi';
my $rrddir = "/usr/local/groundwork/rrd";
#my $rrddir = "/usr/local/nagios/rrd";
my $rrd = "$rrddir/$host\_$service.rrd";
$rrd =~ s/\s/_/g;
my $now = time;
my $graphdir = "/usr/local/groundwork/apache2/htdocs/rrd";
my $graphpath = "/rrd";

my $vlabel = "Percent"; # Vertical Label
my $bcolor = "FFFFFF";  # Background Color

sub body() {
	my %measurement = (
		percent => "#0000FF",
		);
	#
	# Generate the graphs
	#

	foreach $key (keys %measurement) {
	 	$def .= "\"DEF:$key=$rrd:$key:AVERAGE\", ";
		$line .= "\"LINE2:$key$measurement{$key}:$key\", ";
		$line .= "\"GPRINT:$key:MIN:(min=%.0lf\", ";
		$line .= "\"GPRINT:$key:AVERAGE:ave=%.0lf\", ";
		$line .= "\"GPRINT:$key:MAX:max=%.0lf)\", ";

		#$line .= "\"STACK:$key$measurement{$key}:$key\", ";
	}

	$defstring = "$def $line";
	$defstring =~ s/,\s$//;
	$end = time;

	for ($i = 0; $i < 5; $i++) {
		if ($i == 0) {
            # 2 hours
            $title = "$host $service Utilization Last 2 Hours";
            $start = $end - 7200;
            $graph0 = $host."_".$service."_0.png";
			$graph0 =~ s/\s/_/g;
            $graphfile = "$graphdir/$graph0";
		} elsif ($i == 1) {
			# 48 hours
			$title = "$host $service Utilization Last 48 Hours";
			$start = $end - 172800;
			$graph1 = $host."_".$service."_1.png";
			$graph1 =~ s/\s/_/g;
			$graphfile = "$graphdir/$graph1";
		} elsif ($i == 2) {
			# 14 days
			$title = "$host $service Utilization Last 14 Days";
			$start = $end - 1209600;
			$graph2 = $host."_".$service."_2.png";
			$graph2 =~ s/\s/_/g;
			$graphfile = "$graphdir/$graph2";
		} elsif ($i == 3) {
			# 60 days
			$title = "$host $service Utilization Last 60 Days";
			$start = $end - 5184000;
			$graph3 = $host."_".$service."_3.png";
			$graph3 =~ s/\s/_/g;
			$graphfile = "$graphdir/$graph3";
		} elsif ($i == 4) {
			# 360 days
			$title = "$host $service Utilization Last 360 Days";
			$start = $end - 31104000;
			$graph4 = $host."_".$service."_4.png";
			$graph4 =~ s/\s/_/g;
			$graphfile = "$graphdir/$graph4";
		}

		my($averages,$xsize,$ysize);

		# Change to free float - not set max to 100% - to get finer resolution - P. Loh 3-15-2005
		#my $evalstring = '($averages,$xsize,$ysize) = RRDs::graph("$graphfile", "-s", $start, "-e", $end, "--color", "CANVAS#$bcolor", "--vertical-label", $vlabel, "-t", $title, "--lower-limit", 0, "--upper-limit", 100, "-w", 700, "-h", 150, '.$defstring.");";
		my $evalstring = '($averages,$xsize,$ysize) = RRDs::graph("$graphfile", "-s", $start, "-e", $end, "--color", "CANVAS#$bcolor", "--vertical-label", $vlabel, "-t", $title, "-w", 700, "-h", 150, '.$defstring.");";
		eval($evalstring);
		my $err = RRDs::error;
		if ($err) {
			$errstr .= "$err<br>";
		}
	}
	if ($errstr !~ /\w+/) {
		return qq(
<table width=70% bgcolor=#000000 cellspacing=0 cellpadding=0 border=1>
<tr>
<td class=valign=top>
<table width=100% bgcolor=#ffffff cellpadding=5 celspacing=0 border=0>
<tr>
<td class=head align=center>$host $service Utilization</td>
</tr>
<tr>
<td align=center>
<img src=/rrd/$graph0?$now>
</td>
</tr>
<tr>
<td align=center>
<img src=/rrd/$graph1?$now>
</td>
</tr>
<tr>
<td align=center>
<img src=/rrd/$graph2?$now>
</td>
</tr>
<tr>
<td align=center>
<img src=/rrd/$graph3?$now>
</td>
</tr>
<tr>
<td align=center>
<img src=/rrd/$graph4?$now>
</td>
</tr>
<tr>
<td align=center>
<form action=$action method=get>
<input type=hidden name=host value=$host>
<input type=hidden name=service value=$service>
<input type=hidden name=warning value=$warning>
<input type=hidden name=critical value=$critical>
<input type=submit name=refresh value=Refresh>
</form>
);
	} else {
		return qq(
<table width=70% bgcolor=#000000 cellspacing=0 cellpadding=0 border=1>
<tr>
<td class=valign=top>
<table width=100% bgcolor=#ffffff cellpadding=5 celspacing=0 border=0>
<tr>
<td class=head align=center>$host Errors!</td>
</tr>
<td align=center>
<h2>The following occurred while generating the report:</h2>
</td><tr>
<td align=center>
$errstr
</td>
</tr>
);
	}
}


print "Content-type: text/html \n\n";
print qq(
<HTML>
<HEAD>
<META HTTP-EQUIV="CONTENT-TYPE" CONTENT="text/html; charset=windows-1252">
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<META HTTP-EQUIV="Expires" CONTENT="-1">
<TITLE>$host $service Utilization</TITLE>
<!-- <link rel="stylesheet" type="text/css" href="/rrd/style.css"> -->
<style>
.body {
background-color: #ffffff;
scrollbar-face-color: #990000;
scrollbar-shadow-color: #660000;
scrollbar-highlight-color: #990000;
scrollbar-3dlight-color: #660000;
scrollbar-darkshadow-color: #990000;
scrollbar-track-color: #FFFFFF;
scrollbar-arrow-color: #FFFFFF;
}

td { color: #000000; font-family: arial, helvetica, sans-serif; font-size: 12; }
td.head { background-color: #FFCC00; font-family: arial, helvetica, sans-serif; font-size: 12; font-weight: bold; color: #000000; }
td.head2 { background-color: #FFCC66; font-family: arial, helvetica, sans-serif; font-size: 12; font-weight: bold; color: #000000; }
td.row1 { background-color: #ffffcc; font-family: arial, helvetica, sans-serif; font-size: 12; }
td.row2 { background-color: #eeeeee; font-family: arial, helvetica, sans-serif; font-size: 12; }
td.warn { background-color: #fff; font-family: arial, helvetica, sans-serif; font-size: 12; color: #dd0000; }
td.nav { background-color: #fff; font-family: arial, helvetica, sans-serif; font-size: 12; }
input, textarea, select { border: 1px solid #990000; font-family: arial, helvetica, sans-serif;
font-size: 11px; font-weight: bold; background-color: #eeeeee; color: #000000; }


/*Left nav link*/
a.left:link {
color: #CC3300;
font-size: 10px;
font-family: arial, helvetica, sans-serif;
text-decoration: underline;
font-weight: normal;
}
a.left:visited {
color: #CC3300;
font-size: 10px;
font-family: arial, helvetica, sans-serif;
text-decoration: underline;
font-weight: normal;
}
a.left:active {
color: #CC3300;
font-size: 10px;
font-family: arial, helvetica, sans-serif;
text-decoration: underline;
font-weight: normal;
}
a.left:hover {
color: #CC3300;
font-size: 10px;
font-family: arial, helvetica, sans-serif;
text-decoration: underline;
font-weight: normal;
}

/*Standard link*/
a.std:link {
color: #CC3300;
font-size: 12px;
font-family: arial, helvetica, sans-serif;
text-decoration: underline;
font-weight: normal;
}
a.std:visited {
color: #CC3300;
font-size: 12px;
font-family: arial, helvetica, sans-serif;
text-decoration: underline;
font-weight: normal;
}
a.std:active {
color: #CC3300;
font-size: 12px;
font-family: arial, helvetica, sans-serif;
text-decoration: underline;
font-weight: normal;
}
a.std:hover {
color: #CC3300;
font-size: 12px;
font-family: arial, helvetica, sans-serif;
text-decoration: underline;
font-weight: normal;
}

/*Standard link*/
a.head:link {
color: #ffffff;
font-size: 12px;
font-family: arial, helvetica, sans-serif;
text-decoration: underline;
font-weight: bold;
}
a.head:visited {
color: #ffffff;
font-size: 12px;
font-family: arial, helvetica, sans-serif;
text-decoration: underline;
font-weight: bold;
}
a.head:active {
color: #ffffff;
font-size: 12px;
font-family: arial, helvetica, sans-serif;
text-decoration: underline;
font-weight: bold;
}
a.head:hover {
color: #ffffcc;
font-size: 12px;
font-family: arial, helvetica, sans-serif;
text-decoration: underline;
font-weight: bold;
}

/*Center paragraph*/
p.center {
color: #000;
font-family: arial, helvetica, sans-serif;
font-size: 12px;
font-weight: normal;
}

/*Italic paragraph*/
p.italic {
color: #000;
font-family: arial, helvetica, sans-serif;
font-style: italic;
font-size: 12px;
font-weight: normal;
}

/*Center bottom*/
p.bottom {
color: #000;
font-family: arial, helvetica, sans-serif;
font-size: 10px;
font-weight: normal;
}

/*Center bottom*/
p.slide {
color: #FFFFFF;
font-family: arial, helvetica, sans-serif;
font-size: 10px;
font-weight: normal;
}

p.quote {
font-size: 14px;
font-family: arial, helvetica, sans-serif;
color: #000;
line-height: 18pt;
text-align: left;
font-style: italic;
font-weight: bold;
}

h1 {
color: #000;
font-family: arial, helvetica, sans-serif;
font-size: 18px;
font-weight: bold;
}

h2 {
color: #000;
font-family: arial, helvetica, sans-serif;
font-size: 14px;
font-weight: bold;
}

h3 {
color: #000;
font-family: arial, helvetica, sans-serif;
font-size: 12px;
font-weight: bold;
}

h4 {
color: #FFFFFF;
font-family: arial, helvetica, sans-serif;
font-size: 12px;
font-weight: bold;
}

h5 {
color: #000;
font-family: arial, helvetica, sans-serif;
font-size: 10px;
font-weight: normal;
}

</style>
</HEAD>

);

# FIX THIS:  This is a quick hack to work around the currently unimplemented passing of this data
# to the script.  In GW 6.0, access is being controlled instead by web-server referrer chains.
$ENV{'REMOTE_USER'} = 'admin';

if ($ENV{'REMOTE_USER'}) {
	if (-e $rrd) {
		print body();
	} else {
		print qq(
<table width=70% bgcolor=#000000 cellspacing=0 cellpadding=0 border=1>
<tr>
<td class=valign=top>
<table width=100% bgcolor=#ffffff cellpadding=5 celspacing=0 border=0>
<tr>
<td class=head align=center>$host</td>
</tr>
<td align=center>
<h2>Sorry, there is no data for $service.</h2>
</td><tr>
<td align=center>
Either there is a problem with the configuration or the service is not configured to record performance data.
</td>
</tr>
);
	}
} else {
	print  qq(
<table width=70% bgcolor=#000000 cellspacing=0 cellpadding=0 border=1>
<tr>
<td class=valign=top>
<table width=100% bgcolor=#ffffff cellpadding=5 celspacing=0 border=0>
<tr>
<td class=head align=center>Access Denied</td>
</tr>
<td align=center>
<h2>This option is not available from outside the portal.</h2>
</td><tr>
<td align=center>
&nbsp;
</td>
</tr>
);
}
print qq(
</td>
</tr>
</table>
</td>
</tr>
</table>
</body>
</html>
);

__END__

"#E6E6FA", #=> "lavender",
"#FF0000", #=> "red",	# Starting here at index 1
"#00FF00", #=> "green",
"#0000FF", #=> "blue",
"#FFFF00", #=> "yellow",
"#40E0D0", #=> "turquoise",
"#00FFFF", #=> "cyan",
"#006400", #=> "dark green",
"#98FB98", #=> "pale green",
"#FFD700", #=> "gold",
"#A52A2A", #=> "brown",
"#FFA500", #=> "orange",
"#000080", #=> "navy",
"#FFC0CB", #=> "pink",
"#B03060", #=> "maroon",
"#FF00FF", #=> "magenta",
"#A020F0", #=> "purple",
"#000000", #=> "black",
"#BEBEBE"  #=> "gray",



		my $evalstring = '($averages,$xsize,$ysize) = RRDs::graph($graphfile,"--start",$start,"--end",$end,
			"-t","Multi-Parameter Graph",
			"-w",600,"-h","300",'.$defstring.");";


	my($averages,$xsize,$ysize) = RRDs::graph($graphfile, "--start", $start, "--end", $end, "-t", "Multi-Parameter Graph", "-w",600,"-h","300", $defstring);
#print qq($graphfile --start $start --end $end -t "Multi-Parameter Graph" -w 600 -h 300 $defstring);
#	my $graphstring = qw($graphfile --start $start --end $end -t "Multi-Parameter Graph" -w 600 -h 300 $defstring);
	my $graphstring = qq("$graphfile", "-s=$start", "-e=$end","-t=Multi-Parameter Graph", "-w=600", "-h=300", $defstring);

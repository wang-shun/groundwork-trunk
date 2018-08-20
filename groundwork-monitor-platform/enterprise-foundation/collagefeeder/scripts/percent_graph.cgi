#!/usr/bin/perl --

use strict;
use RRDs; 
use CGI;
use locale;

my $query = new CGI;
my $hostgroup = $query->param('hostgroup');
my $warning = $query->param('warning');
my $critical = $query->param('critical');
my $interval = $query->param('interval');
if (!$interval) {
	$interval=30;
}

my ($i, $start, $end, $defstring, $def, $line, $key, $errstr) = undef;
my ($graphfile, $graph0, $graph1, $graph2, $graph3, $graph4, $title) = undef;

#
#	Set Configuration Parameters
#
my $rrddir = "/usr/local/groundwork/feeders/rrd";
#my $rrddir = "/usr/local/nagios/rrd";
my $rrd = "$rrddir/$hostgroup.rrd";
my $graphdir = "/usr/local/groundwork/reports/graphs";
my $graphpath = "/groundwork/reports/graphs";

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
		$line .= "\"GPRINT:$key:MAX:max=%.0lf)\" ";

		#$line .= "\"STACK:$key$measurement{$key}:$key\", ";
	}

	$defstring = "$def $line";
	$defstring =~ s/,\s$//;
	$end = time;
	# Graph Last $interval days
	$title = "$hostgroup Availability Last $interval Days";
	$start = $end - ($interval * 24 * 60 * 60);
	$graph0 = $hostgroup."_0.png";
	$graphfile = "$graphdir/$graph0";

	$title = "\u$title";
	my($averages,$xsize,$ysize);
	# Change to free float - not set max to 100% - to get finer resolution - P. Loh 3-15-2005
	#my $evalstring = '($averages,$xsize,$ysize) = RRDs::graph("$graphfile", "-s", $start, "-e", $end, "--color", "CANVAS#$bcolor", "--vertical-label", $vlabel, "-t", $title, "--lower-limit", 0, "--upper-limit", 100, "-w", 700, "-h", 150, '.$defstring.");";
	my $evalstring = '($averages,$xsize,$ysize) = RRDs::graph("$graphfile", "-s", $start, "-e", $end, "--color", "CANVAS#$bcolor", "--vertical-label", $vlabel, "-t", $title, "-w", 500, "-h", 150, '.$defstring.");";
	eval($evalstring);
	my $err = RRDs::error;
	if ($err) {
		$errstr .= "$err<br>";
	}

	if ($errstr !~ /\w+/) {
		my %checked = ();
		$checked{$interval} = "checked";
		return qq(
<table width=100% bgcolor=#FFFFFF cellspacing=0 cellpadding=0 border=0>
<tr>
<td align=center>
<img src=$graphpath/$hostgroup\_0.png>
</td>
</tr>
<tr>
<td align=center>
<br>
<form action=percent_graph.cgi method=get>
<input type=radio name=interval value=2 $checked{2}>2 days &nbsp;&nbsp;
<input type=radio name=interval value=7 $checked{7}>7 days &nbsp;&nbsp;
<input type=radio name=interval value=14 $checked{14}>14 days &nbsp;&nbsp;
<input type=radio name=interval value=30 $checked{30}>30 days <br>
<input type=hidden name=hostgroup value=$hostgroup>
<input type=hidden name=warning value=$warning>
<input type=hidden name=critical value=$critical>
<input type=hidden name=interval value=$interval>
<input type=submit name=refresh value=Refresh>
</form>
);
	} else {
		return qq(
<table width=100% bgcolor=#FFFFFF cellspacing=0 cellpadding=0 border=1>
<tr>
<td class=head align=center>\u$hostgroup Errors!</td>
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
<TITLE>\u$hostgroup Availability</TITLE>
<link rel="stylesheet" type="text/css" href="/rrd/style.css">
<style>
.body {
background-color: #ffffff;
scrollbar-face-color: #990000;
scrollbar-shadow-color: #660000;
scrollbar-highlight-color: #990000;
scrollbar-3dlight-color: #660000;
scrollbar-darkshadow-color: #990000;
scrollbar-track-color: #FFFFFF;
scrollbar-arrow-color: #FFFFFF
}

td {color: #000000; font-family: Arial, Helvetica, sans-serif; font-size: 12;}
td.head {background-color: #FFCC00; font-family: Arial, Helvetica, sans-serif; font-size: 12; font-weight: bold; color: #000000;}
td.head2 {background-color: #FFCC66; font-family: Arial, Helvetica, sans-serif; font-size: 12; font-weight: bold; color: #000000;}
td.row1 {background-color: #ffffcc; font-family: Arial, Helvetica, sans-serif; font-size: 12;}
td.row2 {background-color: #eeeeee; font-family: Arial, Helvetica, sans-serif; font-size: 12;}
td.warn {background-color: #fff; font-family: Arial, Helvetica, sans-serif; font-size: 12; color: #dd0000}
td.nav {background-color: #fff; font-family: Arial, Helvetica, sans-serif; font-size: 12;}
input, textarea, select {border: 1px solid #990000; font-family: Arial, Helvetica, sans-serif;
font-size: 11px; font-weight: bold; background-color: #eeeeee; color: #000000;}


/*Left nav link*/
a.left:link    {
color:#CC3300; 
font-size: 10px;
font-family:Arial; Helvetica; sans-serif;
text-decoration: underline;
font-weight: normal;
}
a.left:visited {
color:#CC3300; 
font-size: 10px;
font-family:Arial; Helvetica; sans-serif;
text-decoration: underline;
font-weight: normal;
}
a.left:active  {
color:#CC3300; 
font-size: 10px;
font-family:Arial; Helvetica; sans-serif;
text-decoration: underline;
font-weight: normal;
}
a.left:hover   {
color:#CC3300; 
font-size: 10px;
font-family:Arial; Helvetica; sans-serif;
text-decoration: underline;
font-weight: normal;
}

/*Standard link*/
a.std:link    {
color:#CC3300; 
font-size: 12px;
font-family:Arial; Helvetica; sans-serif;
text-decoration: underline;
font-weight: normal;
}
a.std:visited {
color:#CC3300; 
font-size: 12px;
font-family:Arial; Helvetica; sans-serif;
text-decoration: underline;
font-weight: normal;
}
a.std:active  {
color:#CC3300; 
font-size: 12px;
font-family:Arial; Helvetica; sans-serif;
text-decoration: underline;
font-weight: normal;
}
a.std:hover   {
color:#CC3300; 
font-size: 12px;
font-family:Arial; Helvetica; sans-serif;
text-decoration: underline;
font-weight: normal;
}


/*Standard link*/
a.head:link    {
color:#ffffff; 
font-size: 12px;
font-family:Arial; Helvetica; sans-serif;
text-decoration: underline;
font-weight: bold;
}
a.head:visited {
color:#ffffff; 
font-size: 12px;
font-family:Arial; Helvetica; sans-serif;
text-decoration: underline;
font-weight: bold;
}
a.head:active  {
color:#ffffff; 
font-size: 12px;
font-family:Arial; Helvetica; sans-serif;
text-decoration: underline;
font-weight: bold;
}
a.head:hover   {
color:#ffffcc; 
font-size: 12px;
font-family:Arial; Helvetica; sans-serif;
text-decoration: underline;
font-weight: bold;
}

/*Center paragraph*/
p.center {
color:#000; 
font-family:arial; helvetica; sans-serif;
font-size: 12px;
font-weight: normal;
}

/*Itallic paragraph*/
p.italic {
color:#000; 
font-family:arial; Helvetica; sans-serif;
font-style: italic;
font-size: 12px;
font-weight: normal;
}

/*Center bottom*/
p.bottom {
color:#000; 
font-family:Arial; Helvetica; sans-serif;
font-size: 10px;
font-weight: normal;
}

/*Center bottom*/
p.slide {
color:#FFFFFF; 
font-family:Arial; Helvetica; sans-serif;
font-size: 10px;
font-weight: normal;
}

p.quote {
font-size: 14px;
font-family:Arial; Helvetica; sans-serif;
color: #000; 
line-height: 18pt;
text-align: left;
font-style: italic;
font-weight: bold;
}

h1 {
color:#000; 
font-family:Arial; Helvetica; sans-serif;
font-size: 18px;
font-weight: bold;
}

h2 {
color:#000; 
font-family:Arial; Helvetica; sans-serif;
font-size: 14px;
font-weight: bold;
}

h3 {
color:#000; 
font-family:Arial; Helvetica; sans-serif;
font-size: 12px;
font-weight: bold;
}

h4 {
color:#FFFFFF; 
font-family:Arial; Helvetica; sans-serif;
font-size: 12px;
font-weight: bold;
}

h5 {
color:#000; 
font-family:Arial; Helvetica; sans-serif;
font-size: 10px;
font-weight: normal;
}

</style>
</HEAD>

);
print body();
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

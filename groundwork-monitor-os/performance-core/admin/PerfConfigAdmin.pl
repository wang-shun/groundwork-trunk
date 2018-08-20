#!/usr/local/groundwork/perl/bin/perl -w --
#
###############################################################################
# Release 4.6
# October 2017
###############################################################################
#
# Copyright 2007-2017 GroundWork Open Source, Inc. (GroundWork)
# All rights reserved.  This program is free software; you can redistribute it and/or
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
use DBI;
use HTML::Entities ();
use Config;

use CollageQuery;
use MonarchStorProc;
use MonarchForms;

my $isPortal = 0;

# Control whether or not the "Modify > Test" button is available.
my $show_test_button = 1;

# Control whether the Test results display the executed command if graph creation is successful.
my $show_executed_command = 1;

# Where the rrdtool binary lives.
my $rrdtool = '/usr/local/groundwork/common/bin/rrdtool';

# Specify whether to use a shared library to implement RRD file access,
# or to fork an external process for such work (the legacy implementation).
# Set to 1 (recommended) for high performance, to 0 only as an emergency fallback
# or for special purposes.
my $use_shared_rrd_module_for_info = 1;

my $stylesheethtmlref="";
my $thisprogram = undef;
my $nonportalprogram = "PerfConfigAdmin.pl";
my $portalprogram = "/collage/portal/perf-config-admin.psml?file=PerfConfigAdmin.pl";

if ($isPortal) {
    $thisprogram = $portalprogram;
}
else {
    $thisprogram = $nonportalprogram;
    print "Content-type: text/html \n\n";
}
my $request_method = $ENV{'REQUEST_METHOD'};
my $form_info;
if ( $request_method eq "GET" ) {
    $form_info = $ENV{'QUERY_STRING'};
    ## $form_info =~ s/%([\dA-Fa-f][\dA-Fa-f])/pack("C",hex($1))/eg;
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

my $SERVER_SOFTWARE = $ENV{SERVER_SOFTWARE};
my $monarch_js;
if (defined($SERVER_SOFTWARE) && $SERVER_SOFTWARE eq 'TOMCAT') {
    $monarch_js       = '/monarch/js';
}
elsif ( -e '/usr/local/groundwork/config/db.properties' ) {
    $monarch_js       = '/monarch';
}
else {
    # Standalone Monarch (outside of GW Monitor) is no longer supported.
}

my %docs = (
    export_file  => "<p>The Export File Name is the name of a file in the <tt>/tmp</tt> directory into which the exported performance config entry will be written.&nbsp; This is just the name of the file itself, not including any preceding path.</p>",
    graph_label  => "The Graph Label is the heading for this graph's window in Status Viewer.",
    service      => "The Service is a string or expression which must match the name of the service in order for this performance config entry to be applied during performance-data processing.&nbsp; Unless you are also specifying &quot;Use Service as a Regular Expression&quot;, enter the exact service name to which this entry applies.&nbsp; The service name is case-sensitive.&nbsp; If you have entries for both a specific literal service name and a regular expression that matches the service name, the entry for the specific service name will take precedence.",
    service_regx => "If you want this performance config entry to match multiple service names (e.g., snmp_if_interface_1, snmp_if_interface_2, ...), check the &quot;Use Service as a Regular Expression&quot; option.&nbsp; You can then include regular-expression matching syntax in the Service field, and it will be used as a regular expression instead of a simple literal string for matching purposes.&nbsp; Except for service names that match a separate literal-string Service entry, all service names that match this entry's Service field will use this entry to create and update RRDs, and to produce graphs.&nbsp; Be careful with this; if a service name matches the Service field in more than one regular-expression performance-config entry, the system might pick the wrong one to use.",
    host         => "The Host is either a simple literal hostname to match for this entry to be applicable to a service, or a single asterisk (*) character to match all hostnames.&nbsp; If you have an entry for both a specific hostname and a wildcarded (*) hostname for the same service-name matching, the entry for the specific hostname will take precedence.",
    plugin_id    => "plugin_id",
    status_regx  => "<p>The Status Text Parsing Regular Expression field is used when you are working with a plugin that does not return properly formatted performance data.&nbsp; This field is used in conjunction with the next field (&quot;Use Status Text Parsing instead of Performance Data&quot;) to enable Perl regular-expression-based parsing of the plugin-output status text to find performance metrics of interest.</p>
<p class=append>For example, using the regular expression &quot;<tt>(\\d+)&nbsp;(\\d+)</tt>&quot; (without the enclosing double quotes) will parse through the status text looking for the occurrence of two single- or multiple-digit numbers separated by a single space character.&nbsp; These numbers would be captured as \$VALUE1\$ and \$VALUE2\$ and could be passed to the RRD create and/or update commands using those variable names.&nbsp; The end result would be that numbers were extracted from the status text field of the plugin output and inserted into performance graphs despite the fact that the plugin returned no performance data in the standard plugin-output format for such data.</p>
<p class=append>Note: Parentheses in a regular expression are needed to specify that the string or value that matches the enclosed part of the regular expression is to be captured into a variable.&nbsp; In the example shown, those variables would be \$VALUE1\$ and \$VALUE2\$.</p>",
    parse_status => "This field enables or disables the status text parsing function which is defined by the &quot;Status Text Parsing Regular Expression&quot;.",
    rrd_name     => "<p>The RRD Name field defines the absolute pathname of the RRD file that stores accumulated performance data, for each host-service that matches this entry.&nbsp; The following macros may be used as part of the path or filename, to make the RRD file unique to the host-service:</p>
<p class=append>
<table>
<tr>
<td valign=baseline><tt>\$HOST\$</tt></td><td>&nbsp;</td><td>Name of the host whose service output is being handled by the performance-data processor.</td>
</tr>
<tr>
<td valign=baseline><tt>\$SERVICE\$</tt></td><td>&nbsp;</td><td>Name of the service whose output is being handled by the performance-data processor.</td>
</tr>
</table>
</p>
<p class=append>For example, the following string will create an RRD with the Host and Service name in the RRD file:<br><tt>/usr/local/groundwork/rrd/\$HOST\$_\$SERVICE\$.rrd</tt></p>
<p class=append>The performance-data processor will automatically make certain adjustments to substituted values in order to guarantee that a valid filename is produced, so the final result might be slightly different from what you specify here.</p>",
    rrd_create   => "<p>The RRD Create Command is used to create a new RRD file if performance data comes in for a host-service for which an RRD file does not already exist.&nbsp; You can reference the RRDtool documentation for RRD file creation options.&nbsp; The following macros may be used:</p>
<p class=append>
<table>
<tr>
<td valign=baseline><tt>\$RRDTOOL\$</tt></td><td>&nbsp;</td><td>RRDtool program, including file location.</td>
</tr>
<tr>
<td valign=baseline><tt>\$RRDNAME\$</tt></td><td>&nbsp;</td><td>Name of the RRD file, as defined in this configuration tool.</td>
</tr>
</table>
</p>
<p class=append>Here is an example of an RRD Create Command:</p>
<p class=append><tt>\$RRDTOOL\$ create \$RRDNAME\$ --step 300 --start n-1yr DS:number:GAUGE:900:U:U RRA:AVERAGE:0.5:1:2880 RRA:AVERAGE:0.5:5:4032 RRA:AVERAGE:0.5:15:5760 RRA:AVERAGE:0.5:60:8640</tt></p>",
    rrd_update   => "<p>The RRD Update Command is used to insert a new set of performance-data values into the RRD file.&nbsp; The command must include an associated timestamp used to position the new data in the RRD file.</p>
 <p class=append>In each update, the timestamp value must always be larger than the timestamp given in any previous update, or an error will result.&nbsp; Such errors can therefore occur if a performance-data file is reprocessed, but in that case they can be ignored.</p>
<p class=append>See the RRDtool documentation for RRD file update options.&nbsp; In addition to the macros mentioned in the help messages for earlier items, the following macro may be used:</p>
<p class=append>
<table>
<tr>
<td valign=baseline><tt>\$LASTCHECK\$</tt></td><td>&nbsp;</td><td>Service-check time that the plugin executed, in UTC format (whole seconds since the system time epoch).</td>
</tr>
</table>
</p>
<p class=append>Here is an example of an RRD Update Command.&nbsp; This example updates the RRD file with the first value from the performance data string or status text parse:</p>
<p class=append><tt>\$RRDTOOL\$ update \$RRDNAME\$ \$LASTCHECK\$:\$VALUE1\$ 2>&1</tt></p>",
    rrd_graph    => "<p>The Custom RRDtool Graph Command defines how the graph for this service is drawn in the Status Viewer.&nbsp; If no graph command is specified here, the graph command defined for the DEFAULT service will be used instead.</p>
<p class=append>This setting also affects graphing in the Reports &gt; Performance View application.&nbsp; There are three host-view options in that application, namely &quot;Expanded&quot;, &quot;Consolidated by host&quot;, and &quot;Consolidated&quot;.&nbsp; The Custom RRDtool Graph Command you specify here only affects the appearance of the RRD graph when using the Expanded view.</p>
<p class=append>To change the appearance of the graph, see the full documentation available through the Help button on this page.&nbsp; (In that doc, scroll down to &quot;Custom RRDtool Graph Command&quot;.)</p>",
    enable       => "The Enable option, if checked, enables this performance-config entry.&nbsp; If disabled (unchecked), RRD creation and updating will not be executed for this entry.",
    test         => "<p>You can use this button to test the execution of the Custom RRDtool Graph Command.  If you have no custom graph command defined for this entry, the Custom RRDtool Graph Command for the DEFAULT service will be use to graph data for this service.</p>
<p class=append>
To test, select a service-host pair and a time period, then click the Test button.
</p>
<p class=append>
Note that you can only test the graph command against an RRD file that has already been created
(by having this performance-config entry present before a Commit, and then having the service check run a few times).
Also, the selection of service-host pairs is based on the Service and Host strings that were present when this page was first drawn,
along with the &quot;Use Service as a Regular Expression&quot; selection at that time.
This list is not updated if you change those values on-screen.
If you want to change those values and test against some other RRD files,
you must either use a different entry (probably a good idea), or save the changes here
(thereby changing the applicability of this entry) and then come back to this screen.
</p>"
);

print "
	<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN' 'http://www.w3.org/TR/html4/loose.dtd'>
	<HTML>
	<HEAD>
	<META HTTP-EQUIV='Expires' CONTENT='0'>
	<META HTTP-EQUIV='Pragma' CONTENT='no-cache'>
	<TITLE>Groundwork Performance Configuration Administration</TITLE>
	<link rel='stylesheet' type='text/css' href='$stylesheethtmlref'>
";
printstyles();
print Forms->js_utils();

print qq(
	<script type="text/javascript" language=JavaScript src="$monarch_js/nicetitle.js"></script>
	<SCRIPT type="text/javascript" language="JavaScript">
	function lowlight() {
		document.body.style.backgroundColor = '#E6E6E6';
		document.body.style.opacity = 0.6;
	}
	function changePage (page) {
		if (page.length) {
			location.href=page;
		}
	}
	function updatePage (attrName,attrValue) {
		page="$thisprogram?$form_info&"+attrName+"="+attrValue;
		if (page.length) {
			location.href=page;
		}
	}
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
print "<TABLE class=insightcontrolpanel cellspacing=0><TBODY><tr class=insightgray-bg>";
#print "<TH class=insight colSpan=2>$thisday, $month $mday, $year. $timestring</TH></TR>";
print "<TH class=insight colSpan=2>Performance Configuration Administration</TH></TR>";
print "</TABLE>";
my ($dbname,$dbhost,$dbuser,$dbpass,$dbtype) = CollageQuery::readGroundworkDBConfig('monarch');
my $dsn = '';
my $dbh = undef;
eval {
    if ( defined($dbtype) && $dbtype eq 'postgresql' ) {
	$dsn = "DBI:Pg:dbname=$dbname;host=$dbhost";
    }
    else {
	$dsn = "DBI:mysql:database=$dbname;host=$dbhost";
    }
    $dbh = DBI->connect($dsn, $dbuser, $dbpass, { 'AutoCommit' => 1 });
};
if ( not $dbh ) {
    my $title     = "Error Status";
    my $hq_errstr = HTML::Entities::encode( $DBI::errstr || $@ || 'Cause unknown.' );
    my $message   = "ERROR. Database access failed:<br><span class=error>$hq_errstr</span>";
    print "<table class=insightcontrolpanel cellspacing=0><tr><td class=insighthead>$title</td></tr><tr><td class=insightbody>$message</td></tr></table>";
    print "<INPUT class=orangebutton type=button value='Continue' onClick='changePage(\"$thisprogram?cmd=list\")'>";
    exit(0);
}

if ( !$FORM_DATA{cmd} or $FORM_DATA{cmd} eq "list" ) {
    list();
}
elsif ( $FORM_DATA{cmd} eq "modify" ) {
    modify();
}
elsif ( $FORM_DATA{cmd} eq "copy" ) {
    copy();
}
elsif ( $FORM_DATA{cmd} eq "new" ) {
    new();
}
elsif ( $FORM_DATA{cmd} eq "delete" ) {
    deleteentry();
}
elsif ( $FORM_DATA{test} && $FORM_DATA{test} eq "Test" ) {
    test();
}
elsif ( $FORM_DATA{cmd} eq "update" ) {
    update();
}
elsif ( $FORM_DATA{cmd} eq "add" ) {
    add();
}
elsif ( $FORM_DATA{cmd} eq "exportform" ) {
    export_form();
}
elsif ( $FORM_DATA{cmd} eq "export" ) {
    export();
}
elsif ( $FORM_DATA{cmd} eq "exportall" ) {
    export_all();
}
print "
	</FORM>
	</DIV>
	</BODY>
	</HTML>
";
$dbh->disconnect();
exit;

# See the Config(3pm) man page for details of this magic formulation.
sub system_signal_name {
    my $signal_number = shift;
    local $_;

    my %sig_num;
    my @sig_name;

    unless ( $Config{sig_name} && $Config{sig_num} ) {
	return undef;
    }

    my @names = split ' ', $Config{sig_name};
    @sig_num{@names} = split ' ', $Config{sig_num};
    foreach (@names) {
	$sig_name[ $sig_num{$_} ] ||= $_;
    }

    return $sig_name[$signal_number] || undef;
}

sub wait_status_message {
    my $wait_status   = shift;
    my $exit_status   = $wait_status >> 8;
    my $signal_number = $wait_status & 0x7F;
    my $dumped_core   = $wait_status & 0x80;
    my $signal_name   = system_signal_name($signal_number) || "$signal_number is unknown";
    my $message = "exit status $exit_status" . ( $signal_number ? " (signal $signal_name)" : '' ) . ( $dumped_core ? ' (with core dump)' : '' );
    return $message;
}

#
#	List all entries
#
sub list {
    my %is_selected = ();
    $is_selected{ $FORM_DATA{id} || '' } = "SELECTED";
    my $selected = '';
    my $query = "SELECT performanceconfig_id,service,host FROM performanceconfig ORDER BY service,host";
    my $sth   = $dbh->prepare($query);
    if ( not $sth->execute() ) {
	my $title     = "Error Status";
	my $hq_errstr = HTML::Entities::encode( $sth->errstr );
	my $message   = "ERROR. Database access failed:<br><span class=error>$hq_errstr</span>";
	print "<table class=insightcontrolpanel cellspacing=0><tr><td class=insighthead>$title</td></tr><tr><td class=insightbody>$message</td></tr></table>";
	print "<INPUT class=orangebutton type=button value='Continue' onClick='changePage(\"$thisprogram?cmd=list\")'>";
	return;
    }
    my $title = "Performance-Data Collection and Graph Creation Setup";
    my $message =
	"In these screens, you will manage the collection and graphing of performance data from Nagios-related and CloudHub-related data sources. "
      . "Metric data from such sources will not be collected, and will not be available as graphs in the Status Viewer or Performance View applications, "
      . "until you have set up an entry here which matches the service+host combination for the data source of interest. "
      . "Click the Help button for more-detailed information."
      . "<p class=append>Note that the setup here assumes that the set of metrics from a given data source will be stable over time, "
      . "both in the number of metrics provided in each metrics-gathering operation and in the nature and sequence in which those results are reported. "
      . "If those assumptions are violated, the data will not be collected or graphed as you expect. "
      . "See the <a href='http://nagiosplug.sourceforge.net/developer-guidelines.html#AEN201' target='_blank' tabindex='-1'>"
      . "Nagios Plugin Development Guidelines</a> "
      . "at <a href='http://nagiosplug.sourceforge.net/developer-guidelines.html#AEN201' target='_blank' tabindex='-1'>"
      . "http://nagiosplug.sourceforge.net/developer-guidelines.html#AEN201</a> "
      . "for information on the format in which performance data must be reported.</p>"
      . "<p class=append>Information on <a href='http://oss.oetiker.ch/rrdtool/' target='_blank' tabindex='-1'>RRDtool</a> commands may be found in the "
      . "<a href='http://oss.oetiker.ch/rrdtool/doc/index.en.html' target='_blank' tabindex='-1'>RRDtool documentation</a>, in particular pages for "
      . "<a href='http://oss.oetiker.ch/rrdtool/doc/rrdcreate.en.html' target='_blank' tabindex='-1'>rrdcreate</a>, "
      . "<a href='http://oss.oetiker.ch/rrdtool/doc/rrdupdate.en.html' target='_blank' tabindex='-1'>rrdupdate</a>, and the variety of pages related to "
      . "<a href='http://oss.oetiker.ch/rrdtool/doc/rrdgraph.en.html' target='_blank' tabindex='-1'>rrdgraph</a>, as well as the various "
      . "<a href='http://oss.oetiker.ch/rrdtool/tut/index.en.html'  target='_blank' tabindex='-1'>tutorials</a>.</p>";
    print "<table class=insightcontrolpanel cellspacing=0><tr><td class=insighthead>$title</td></tr><tr><td class=insightsubhead>$message</td></tr></table>";
    $selected = $is_selected{''} || '';
    $message = "<select name=id class=insight onChange=\"lowlight();changePage('$thisprogram?cmd=list&amp;id='+this.options[this.selectedIndex].value)\">
	<option class=insight $selected value=''>Show All</option>
    ";
    while ( my $row = $sth->fetchrow_hashref() ) {
	my $id = $$row{performanceconfig_id};
	$selected = $is_selected{$id} || '';
	$message .= "<option class=insight $selected value='$id'>" . $$row{service} . " - " . $$row{host} . "</option>";
    }
    $sth->finish();
    $message .= "</select>";
    print "<table class=insightcontrolpanel cellspacing=0><tr>
    <td class=insighthead width='10%'><span style='white-space: nowrap;'>Select&nbsp;Service-Host&nbsp;entry:</span></td>
    <td class=insighthead>$message</td>
    </tr></table>";
    my $help_url = StorProc->doc_section_url('How+to+configure+performance+graphs');
    print "	<INPUT class=orangebutton type=button value='Create New Entry' onClick='changePage(\"$thisprogram?cmd=new\")'>&nbsp;";
    print "	<INPUT class=orangebutton type=button value='Export All' onClick='changePage(\"$thisprogram?cmd=exportall\")'>&nbsp;";
    print "	<INPUT class=orangebutton type=button value='Help' name=help onclick=\"open_window('$help_url')\">";
    print "<br><br>";

    if ( $FORM_DATA{id} ) {
	$query = "SELECT * FROM performanceconfig where performanceconfig_id = $FORM_DATA{id}";
    }
    else {
	$query = "SELECT * FROM performanceconfig ORDER BY service,host";
    }
    $sth = $dbh->prepare($query);
    if ( not $sth->execute() ) {
	my $title     = "Error Status";
	my $hq_errstr = HTML::Entities::encode( $sth->errstr );
	my $message   = "ERROR. Database access failed:<br><span class=error>$hq_errstr</span>";
	print "<table class=insightcontrolpanel cellspacing=0><tr><td class=insighthead>$title</td></tr><tr><td class=insightbody>$message</td></tr></table>";
	print "<INPUT class=orangebutton type=button value='Continue' onClick='changePage(\"$thisprogram?cmd=list\")'>";
	return;
    }
    while ( my $row = $sth->fetchrow_hashref() ) {
	my $id           = $$row{performanceconfig_id};
	my $host         = $$row{host};
	my $service      = $$row{service};
	my $service_regx = "OFF";
	if ( $$row{service_regx} ) {
	    $service_regx = "ON";
	}
	my $label           = $$row{label};
	my $rrdname         = $$row{rrdname};
	my $rrdcreatestring = $$row{rrdcreatestring};
	my $rrdupdatestring = $$row{rrdupdatestring};
	my $perfidstring    = $$row{perfidstring};
	my $graphcgi        = $$row{graphcgi};
	my $parseregx       = $$row{parseregx};
	my $parseregx_first = "OFF";

	# Some versions of IE don't understand the "white-space: pre-wrap;" CSS we apply to long fields,
	# so we have to punt here and convert long strings of spaces to alternate non-blanking spaces to
	# achieve the same effect.  We don't convert both spaces because we still want to the browser to
	# be able to rearrange long words across lines if the browser width is adjusted.

	$rrdcreatestring = '' if not defined $rrdcreatestring;
	$rrdcreatestring = HTML::Entities::encode($rrdcreatestring);
	$rrdcreatestring =~ s/\r?\n/<br>/g;
	$rrdcreatestring =~ s/  /&nbsp; /g;

	$rrdupdatestring = '' if not defined $rrdupdatestring;
	$rrdupdatestring = HTML::Entities::encode($rrdupdatestring);
	$rrdupdatestring =~ s/\r?\n/<br>/g;
	$rrdupdatestring =~ s/  /&nbsp; /g;

	$graphcgi = '' if not defined $graphcgi;
	$graphcgi = HTML::Entities::encode($graphcgi);
	$graphcgi =~ s/\r?\n/<br>/g;
	$graphcgi =~ s/  /&nbsp; /g;

	if ( $$row{parseregx_first} ) {
	    $parseregx_first = "ON";
	}
	my $enable = "OFF";
	if ( $$row{enable} ) {
	    $enable = "ON";
	}

	print "<table class=insightcontrolpanel cellspacing=0>";
	print "<tr><td class=insightleft><b>Graph Label</b></td><td class=insight>$label</td></tr>";
	print "<tr><td class=insightleft><b>Service</b></td><td class=insight>$service</td></tr>";
	print "<tr><td class=insightleft><b>Use Service as a Regular Expression</b></td><td class=insight>$service_regx</td></tr>";
	print "<tr><td class=insightleft><b>Host</b></td><td class=insight>$host</td></tr>";
	## print "<tr><td class=insightleft><b>Plugin ID</b></td><td class=insight>$perfidstring</td></tr>";
	print "<tr><td class=insightleft><b>Status Text Parsing Regular Expression</b></td><td class=insight>$parseregx</td></tr>";
	print "<tr><td class=insightleft><b>Use Status Text Parsing instead of Performance Data</b></td><td class=insight>$parseregx_first</td></tr>";
	print "<tr><td class=insightleft><b>RRD Name</b></td><td class=insight>$rrdname</td></tr>";
	print "<tr><td class=insightleft><b>RRD Create Command</b></td><td class=insight>$rrdcreatestring</td></tr>";
	print "<tr><td class=insightleft><b>RRD Update Command</b></td><td class=insight>$rrdupdatestring</td></tr>";
	print "<tr><td class=insightleft><b>Custom RRDtool Graph Command</b></td><td class=insight>$graphcgi</td></tr>";
	print "<tr><td class=insightleft><b>Enable</b></td><td class=insight>$enable</td></tr>";
	print "<tr><td class=insightbuttons colspan=2 align=center>
	    <INPUT class=orangebutton type=button value='Modify' onClick='changePage(\"$thisprogram?cmd=modify&amp;id=$id\")'>&nbsp;
	    <INPUT class=orangebutton type=button value='Copy' onClick='changePage(\"$thisprogram?cmd=copy&amp;id=$id\")'>&nbsp;
	    <INPUT class=orangebutton type=button value='Delete' onClick='changePage(\"$thisprogram?cmd=delete&amp;id=$id\")'>&nbsp;
	    <INPUT class=orangebutton type=button value='Export' onClick='changePage(\"$thisprogram?cmd=exportform&amp;id=$id\")'>
	    </td></tr>";
	print "</table>";
	print "<br>";
    }
    $sth->finish();
}

#
#	Modify the entries
#
sub modify {
    my $help_url = StorProc->doc_section_url('How+to+configure+performance+graphs', 'Howtoconfigureperformancegraphs-CreatingaNewEntry');
    my $query = "SELECT * FROM performanceconfig WHERE performanceconfig_id=$FORM_DATA{id}";
    my $sth   = $dbh->prepare($query);
    if ( not $sth->execute() ) {
	my $title     = "Error Status";
	my $hq_errstr = HTML::Entities::encode( $sth->errstr );
	my $message   = "ERROR. Database access failed:<br><span class=error>$hq_errstr</span>";
	print "<table class=insightcontrolpanel cellspacing=0><tr><td class=insighthead>$title</td></tr><tr><td class=insightbody>$message</td></tr></table>";
	print "<INPUT class=orangebutton type=button value='Continue' onClick='changePage(\"$thisprogram?cmd=list\")'>";
	return;
    }
    my %checked = ( 0 => '', 1 => 'CHECKED' );
    while ( my $row = $sth->fetchrow_hashref() ) {
	my $id              = $$row{performanceconfig_id};
	my $host            = $$row{host};
	my $service         = $$row{service};
	my $service_regx    = $$row{service_regx};
	my $label           = $$row{label};
	my $rrdname         = $$row{rrdname};
	my $rrdcreatestring = $$row{rrdcreatestring};
	my $rrdupdatestring = $$row{rrdupdatestring};
	my $perfidstring    = $$row{perfidstring};
	my $graphcgi        = $$row{graphcgi};
	my $parseregx       = $$row{parseregx};
	my $parseregx_first = $$row{parseregx_first};
	my $enable          = $$row{enable};

	$rrdcreatestring = '' if not defined $rrdcreatestring;
	$rrdcreatestring = HTML::Entities::encode($rrdcreatestring);
	$rrdcreatestring =~ tr/\r//d;
	my @rrdcreatestring = split( /\n/, $rrdcreatestring );
	my $rrdcreatestring_rows = @rrdcreatestring;
	foreach my $cgi_row (@rrdcreatestring) {
	    $rrdcreatestring_rows += int(length($cgi_row) / 97);
	}
	$rrdcreatestring_rows = 3  if $rrdcreatestring_rows < 3;
	$rrdcreatestring_rows = 50 if $rrdcreatestring_rows > 50;

	$rrdupdatestring = '' if not defined $rrdupdatestring;
	$rrdupdatestring = HTML::Entities::encode($rrdupdatestring);
	$rrdupdatestring =~ tr/\r//d;
	my @rrdupdatestring = split( /\n/, $rrdupdatestring );
	my $rrdupdatestring_rows = @rrdupdatestring;
	foreach my $cgi_row (@rrdupdatestring) {
	    $rrdupdatestring_rows += int(length($cgi_row) / 97);
	}
	$rrdupdatestring_rows = 3  if $rrdupdatestring_rows < 3;
	$rrdupdatestring_rows = 50 if $rrdupdatestring_rows > 50;

	$graphcgi = '' if not defined $graphcgi;
	$graphcgi = HTML::Entities::encode($graphcgi);
	$graphcgi =~ tr/\r//d;
	my @graphcgi = split( /\n/, $graphcgi );
	my $graphcgi_rows = @graphcgi;
	foreach my $cgi_row (@graphcgi) {
	    $graphcgi_rows += int(length($cgi_row) / 97);
	}
	$graphcgi_rows = 3  if $graphcgi_rows < 3;
	$graphcgi_rows = 50 if $graphcgi_rows > 50;

	print "<input type=hidden name=id value=$id>";
	print "<input type=hidden name=cmd value=update>";
	print "<table class=insightcontrolpanel cellspacing=0>";
	print "<tr><td class=insightleft><b>Graph Label</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{graph_label}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight><INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=label VALUE=\"$label\"></td></tr>";
	print "<tr><td class=insightleft><b>Service</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{service}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight><INPUT CLASS=insight size=100 maxlength=255 TYPE=TEXT NAME=service VALUE=\"$service\"></td></tr>";
	print "<tr><td class=insightleft><b>Use Service as a Regular Expression</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{service_regx}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight><INPUT CLASS=insight TYPE=checkbox NAME=service_regx VALUE=1 $checked{$service_regx||0}></td></tr>";
	print "<tr><td class=insightleft><b>Host</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{host}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight><INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=host VALUE=\"$host\"></td></tr>";
	## print "<tr><td class=insightleft><b>Plugin ID</b></td>
	##     <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{plugin_id}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	##     <td class=insight><INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=perfidstring VALUE=\"$perfidstring\"></td></tr>";
	print "<tr><td class=insightleft><b>Status Text Parsing Regular Expression</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{status_regx}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight><INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=parseregx VALUE=\"$parseregx\"></td></tr>";
	print "<tr><td class=insightleft><b>Use Status Text Parsing instead of Performance Data</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{parse_status}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight><INPUT CLASS=insight TYPE=checkbox NAME=parseregx_first VALUE=1 $checked{$parseregx_first||0}></td></tr>";
	print "<tr><td class=insightleft><b>RRD Name</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{rrd_name}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight><INPUT CLASS=insight size=100 maxlength=256 TYPE=TEXT NAME=rrdname VALUE=\"$rrdname\"></td></tr>";
	print "<tr><td class=insightleft><b>RRD Create Command</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{rrd_create}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight><TEXTAREA CLASS=insight cols=100 rows=$rrdcreatestring_rows NAME=rrdcreatestring>$rrdcreatestring</TEXTAREA></td></tr>";
	print "<tr><td class=insightleft><b>RRD Update Command</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{rrd_update}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight><TEXTAREA CLASS=insight cols=100 rows=$rrdupdatestring_rows NAME=rrdupdatestring>$rrdupdatestring</TEXTAREA></td></tr>";
	print "<tr><td class=insightleft><b>Custom RRDtool Graph Command</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{rrd_graph}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight><TEXTAREA CLASS=insight cols=100 rows=$graphcgi_rows NAME=graphcgi>$graphcgi</TEXTAREA></td></tr>";
	print "<tr><td class=insightleft><b>Enable</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{enable}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight><INPUT CLASS=insight TYPE=checkbox NAME=enable VALUE=1 $checked{$enable||0}></td></tr>";
	show_test_command( $rrdname, $host, $service, $service_regx, $graphcgi, 0 ) if $show_test_button;
	print "<tr><td class=insightbuttons colspan=3 align=center>
		<INPUT class=orangebutton type=submit id=update name=update value='Update'>&nbsp;
		<INPUT class=orangebutton type=button value='Cancel' onClick='changePage(\"$thisprogram?cmd=list\")'>&nbsp;
		<INPUT class=orangebutton type=button value='Help' name=help onclick=\"open_window('$help_url')\">
		</td></tr>";
	print "</table>";
	print "<br><br><br><br><br><br>";

	# FIX MINOR:  This extra space allows the pop-up help for the Test button to have room to appear, even at
	# very large browser font size, if you first scroll the screen all the way down.  Better would be to have
	# the pop-up help automatically adjust its position so it avoids overlapping the edges of the view.
	print "<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>" if $show_test_button;
    }
    $sth->finish();
}

#
#	Copy the entries
#
sub copy {
    my $help_url = StorProc->doc_section_url('How+to+configure+performance+graphs', 'Howtoconfigureperformancegraphs-CreatingaNewEntry');
    my $query = "SELECT * FROM performanceconfig WHERE performanceconfig_id=$FORM_DATA{id}";
    my $sth   = $dbh->prepare($query);
    if ( not $sth->execute() ) {
	my $title     = "Error Status";
	my $hq_errstr = HTML::Entities::encode( $sth->errstr );
	my $message   = "ERROR. Database access failed:<br><span class=error>$hq_errstr</span>";
	print "<table class=insightcontrolpanel cellspacing=0><tr><td class=insighthead>$title</td></tr><tr><td class=insightbody>$message</td></tr></table>";
	print "<INPUT class=orangebutton type=button value='Continue' onClick='changePage(\"$thisprogram?cmd=list\")'>";
	return;
    }
    my %checked = ( 0 => '', 1 => 'CHECKED' );
    while ( my $row = $sth->fetchrow_hashref() ) {
	my $id              = $$row{performanceconfig_id};
	my $host            = $$row{host};
	my $service         = $$row{service};
	my $service_regx    = $$row{service_regx};
	my $label           = $$row{label};
	my $rrdname         = $$row{rrdname};
	my $rrdcreatestring = $$row{rrdcreatestring};
	my $rrdupdatestring = $$row{rrdupdatestring};
	my $perfidstring    = $$row{perfidstring};
	my $graphcgi        = $$row{graphcgi};
	my $parseregx       = $$row{parseregx};
	my $parseregx_first = $$row{parseregx_first};
	my $enable          = $$row{enable};

	$rrdcreatestring = '' if not defined $rrdcreatestring;
	$rrdcreatestring = HTML::Entities::encode($rrdcreatestring);
	$rrdcreatestring =~ tr/\r//d;
	my @rrdcreatestring = split( /\n/, $rrdcreatestring );
	my $rrdcreatestring_rows = @rrdcreatestring;
	foreach my $cgi_row (@rrdcreatestring) {
	    $rrdcreatestring_rows += int(length($cgi_row) / 97);
	}
	$rrdcreatestring_rows = 3  if $rrdcreatestring_rows < 3;
	$rrdcreatestring_rows = 50 if $rrdcreatestring_rows > 50;

	$rrdupdatestring = '' if not defined $rrdupdatestring;
	$rrdupdatestring = HTML::Entities::encode($rrdupdatestring);
	$rrdupdatestring =~ tr/\r//d;
	my @rrdupdatestring = split( /\n/, $rrdupdatestring );
	my $rrdupdatestring_rows = @rrdupdatestring;
	foreach my $cgi_row (@rrdupdatestring) {
	    $rrdupdatestring_rows += int(length($cgi_row) / 97);
	}
	$rrdupdatestring_rows = 3  if $rrdupdatestring_rows < 3;
	$rrdupdatestring_rows = 50 if $rrdupdatestring_rows > 50;

	$graphcgi = '' if not defined $graphcgi;
	$graphcgi = HTML::Entities::encode($graphcgi);
	$graphcgi =~ tr/\r//d;
	my @graphcgi = split( /\n/, $graphcgi );
	my $graphcgi_rows = @graphcgi;
	foreach my $cgi_row (@graphcgi) {
	    $graphcgi_rows += int(length($cgi_row) / 97);
	}
	$graphcgi_rows = 3  if $graphcgi_rows < 3;
	$graphcgi_rows = 50 if $graphcgi_rows > 50;

	print "<input type=hidden name=cmd value=add>";
	print "<table class=insightcontrolpanel cellspacing=0>";
	print "<tr><td class=insightleft><b>Graph Label</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{graph_label}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight><INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=label VALUE=\"$label\"></td></tr>";
	print "<tr><td class=insightleft><b>Service</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{service}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight><INPUT CLASS=insight size=100 maxlength=255 TYPE=TEXT NAME=service VALUE=\"$service\"></td></tr>";
	print "<tr><td class=insightleft><b>Use Service as a Regular Expression</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{service_regx}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight><INPUT CLASS=insight TYPE=checkbox NAME=service_regx VALUE=1 $checked{$service_regx||0}></td></tr>";
	print "<tr><td class=insightleft><b>Host</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{host}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight><INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=host VALUE=\"$host\"></td></tr>";
	## print "<tr><td class=insightleft><b>Plugin ID</b></td>
	##     <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{plugin_id}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	##     <td class=insight><INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=perfidstring VALUE=\"$perfidstring\"></td></tr>";
	print "<tr><td class=insightleft><b>Status Text Parsing Regular Expression</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{status_regx}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight><INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=parseregx VALUE=\"$parseregx\"></td></tr>";
	print "<tr><td class=insightleft><b>Use Status Text Parsing instead of Performance Data</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{parse_status}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight><INPUT CLASS=insight TYPE=checkbox NAME=parseregx_first VALUE=1 $checked{$parseregx_first||0}></td></tr>";
	print "<tr><td class=insightleft><b>RRD Name</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{rrd_name}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight><INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=rrdname VALUE=\"$rrdname\"></td></tr>";
	print "<tr><td class=insightleft><b>RRD Create Command</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{rrd_create}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight><TEXTAREA CLASS=insight cols=100 rows=$rrdcreatestring_rows NAME=rrdcreatestring>$rrdcreatestring</TEXTAREA></td></tr>";
	print "<tr><td class=insightleft><b>RRD Update Command</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{rrd_update}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight><TEXTAREA CLASS=insight cols=100 rows=$rrdupdatestring_rows NAME=rrdupdatestring>$rrdupdatestring</TEXTAREA></td></tr>";
	print "<tr><td class=insightleft><b>Custom RRDtool Graph Command</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{rrd_graph}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight><TEXTAREA CLASS=insight cols=100 rows=$graphcgi_rows NAME=graphcgi>$graphcgi</TEXTAREA></td></tr>";
	print "<tr><td class=insightleft><b>Enable</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{enable}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight><INPUT CLASS=insight TYPE=checkbox NAME=enable VALUE=1 $checked{$enable||0}></td></tr>";
	print "<tr><td class=insightbuttons colspan=3 align=center>
		<INPUT class=orangebutton type=submit value='Create Copy'>&nbsp;
		<INPUT class=orangebutton type=button value='Cancel' onClick='changePage(\"$thisprogram?cmd=list\")'>&nbsp;
		<INPUT class=orangebutton type=button value='Help' name=help onclick=\"open_window('$help_url')\">
		</td></tr>";
	print "</table>";
	print "<br><br><br><br><br><br>";
    }
    $sth->finish();
}

#
#	Update an existing entry
#
sub update {
    # Take into account the various columns we have declared to be NOT NULL.
    $FORM_DATA{host}            = '' if not defined $FORM_DATA{host};
    $FORM_DATA{service}         = '' if not defined $FORM_DATA{service};
    $FORM_DATA{label}           = '' if not defined $FORM_DATA{label};
    $FORM_DATA{perfidstring}    = '' if not defined $FORM_DATA{perfidstring};
    $FORM_DATA{parseregx}       = '' if not defined $FORM_DATA{parseregx};
    $FORM_DATA{rrdname}         = '' if not defined $FORM_DATA{rrdname};
    $FORM_DATA{rrdcreatestring} = '' if not defined $FORM_DATA{rrdcreatestring};
    $FORM_DATA{rrdupdatestring} = '' if not defined $FORM_DATA{rrdupdatestring};

    # Clean up the data.

    $FORM_DATA{host}    =~ s/^\s+//;
    $FORM_DATA{host}    =~ s/\s+$//;
    $FORM_DATA{service} =~ s/^\s+//;
    $FORM_DATA{service} =~ s/\s+$//;
    $FORM_DATA{label}   =~ s/^\s+//;
    $FORM_DATA{label}   =~ s/\s+$//;
    $FORM_DATA{rrdname} =~ s/^\s+//;
    $FORM_DATA{rrdname} =~ s/\s+$//;

    $FORM_DATA{rrdcreatestring} =~ tr/\r//d;
    $FORM_DATA{rrdcreatestring} =~ s/^\s+//;
    $FORM_DATA{rrdcreatestring} =~ s/\s+$//;

    $FORM_DATA{rrdupdatestring} =~ tr/\r//d;
    $FORM_DATA{rrdupdatestring} =~ s/^\s+//;
    $FORM_DATA{rrdupdatestring} =~ s/\s+$//;

    if ( defined $FORM_DATA{graphcgi} ) {
	if ( $FORM_DATA{graphcgi} =~ /^\s*'/ && $FORM_DATA{graphcgi} =~ /'\s*$/ ) {
	    ## print "stripping quotes and surrounding whitespace ... \n";
	    $FORM_DATA{graphcgi} =~ s/^\s*'//;
	    $FORM_DATA{graphcgi} =~ s/'\s*$//;
	}

	$FORM_DATA{graphcgi} =~ tr/\r//d;
	$FORM_DATA{graphcgi} =~ s/^\s+//;
	$FORM_DATA{graphcgi} =~ s/\s+$//;
    }

    my $q_host            = $dbh->quote( $FORM_DATA{host} );
    my $q_service         = $dbh->quote( $FORM_DATA{service} );
    my $q_label           = $dbh->quote( $FORM_DATA{label} );
    my $q_service_regx    = $dbh->quote( $FORM_DATA{service_regx} );
    my $q_perfidstring    = $dbh->quote( $FORM_DATA{perfidstring} );
    my $q_parseregx       = $dbh->quote( $FORM_DATA{parseregx} );
    my $q_parseregx_first = $dbh->quote( $FORM_DATA{parseregx_first} );
    my $q_rrdname         = $dbh->quote( $FORM_DATA{rrdname} );
    my $q_rrdcreatestring = $dbh->quote( $FORM_DATA{rrdcreatestring} );
    my $q_rrdupdatestring = $dbh->quote( $FORM_DATA{rrdupdatestring} );
    my $q_graphcgi        = $dbh->quote( $FORM_DATA{graphcgi} );
    my $q_enable          = $dbh->quote( $FORM_DATA{enable} );
    my $q_type            = $dbh->quote('nagios');

    my $hq_host    = HTML::Entities::encode( $FORM_DATA{host} );
    my $hq_service = HTML::Entities::encode( $FORM_DATA{service} );

    my $host_match    = defined( $FORM_DATA{host} )    ? "host = $q_host"       : "host is NULL";
    my $service_match = defined( $FORM_DATA{service} ) ? "service = $q_service" : "service is NULL";

    my $query = "SELECT performanceconfig_id FROM performanceconfig WHERE $host_match AND $service_match";
    my $sth   = $dbh->prepare($query);
    if ( not $sth->execute() ) {
	my $title     = "Error Status";
	my $hq_errstr = HTML::Entities::encode( $sth->errstr );
	my $message   = "ERROR. Database access failed:<br><span class=error>$hq_errstr</span>";
	print "<table class=insightcontrolpanel cellspacing=0><tr><td class=insighthead>$title</td></tr><tr><td class=insightbody>$message</td></tr></table>";
	print "<INPUT class=orangebutton type=button value='Continue' onClick='changePage(\"$thisprogram?cmd=list\")'>";
	return;
    }
    my $id = undef;
    while ( my $row = $sth->fetchrow_hashref() ) {
	$id = $$row{performanceconfig_id};
    }
    $sth->finish();
    if ( defined($id) and $id != $FORM_DATA{id} ) {
	my $title = "Error Status";
	my $message = "<span class=error>ERROR. Performance configuration already exists for host \"<tt>$hq_host</tt>\" and service \"<tt>$hq_service</tt>\".</span>";
	$message .= "<p class=append>Duplicate entries are not permitted. Delete the existing entry before adding this entry.</p>";
	print "<table class=insightcontrolpanel cellspacing=0><tr><td class=insighthead>$title</td></tr><tr><td class=insightbody>$message</td></tr></table>";
	print "<INPUT class=orangebutton type=button value='Continue' onClick='changePage(\"$thisprogram?cmd=list\")'>";
	return;
    }
    $query =
	"UPDATE performanceconfig SET "
      . "label=$q_label,"
      . "host=$q_host,"
      . "service=$q_service,"
      . "service_regx=$q_service_regx,"
      . "perfidstring=$q_perfidstring,"
      . "parseregx=$q_parseregx,"
      . "parseregx_first=$q_parseregx_first,"
      . "rrdname=$q_rrdname,"
      . "rrdcreatestring=$q_rrdcreatestring,"
      . "rrdupdatestring=$q_rrdupdatestring,"
      . "graphcgi=$q_graphcgi,"
      . "type=$q_type,"
      . "enable=$q_enable"
      . " WHERE performanceconfig_id=$FORM_DATA{id} ";

    ## print "<br>SQL=$query<br>";
    $sth = $dbh->prepare($query);
    if ( not $sth->execute() ) {
	my $title     = "Error Status";
	my $hq_errstr = HTML::Entities::encode( $sth->errstr );
	my $message   = "ERROR. Database access failed:<br><span class=error>$hq_errstr</span>";
	print "<table class=insightcontrolpanel cellspacing=0><tr><td class=insighthead>$title</td></tr><tr><td class=insightbody>$message</td></tr></table>";
	print "<INPUT class=orangebutton type=button value='Continue' onClick='changePage(\"$thisprogram?cmd=list\")'>";
	return;
    }
    my $title = "Status";
    my $message = "Performance configuration for host \"<tt>$hq_host</tt>\" and service \"<tt>$hq_service</tt>\" has been updated.";
    print "<table class=insightcontrolpanel cellspacing=0><tr><td class=insighthead>$title</td></tr><tr><td class=insightbody>$message</td></tr></table>";
    print "<INPUT class=orangebutton type=button value='Continue' onClick='changePage(\"$thisprogram?cmd=list\")'>";
}

#
#	Delete an existing entry
#
sub deleteentry {
    my $query = "select host, service, service_regx FROM performanceconfig WHERE performanceconfig_id=$FORM_DATA{id}";
    my ( $host, $service, $service_regx ) = $dbh->selectrow_array($query);
    if ( !defined($host) || !defined($service) ) {
	my $title     = "Error Status";
	my $hq_errstr = HTML::Entities::encode( $dbh->errstr );
	my $message   = "ERROR.&nbsp; Database access failed:<br><span class=error>$hq_errstr</span>";
	print "<table class=insightcontrolpanel cellspacing=0><tr><td class=insighthead>$title</td></tr><tr><td class=insightbody>$message</td></tr></table>";
	print "<INPUT class=orangebutton type=button value='Continue' onClick='changePage(\"$thisprogram?cmd=list\")'>";
	return;
    }
    if ( $host eq '*' && $service eq 'DEFAULT' ) {
	my $title   = "Error Status";
	my $message = "ERROR.&nbsp; You tried to delete the default performance-config entry, but that is not allowed.";
	print "<table class=insightcontrolpanel cellspacing=0><tr><td class=insighthead>$title</td></tr><tr><td class=insightbody>$message</td></tr></table>";
	print "<INPUT class=orangebutton type=button value='Continue' onClick='changePage(\"$thisprogram?cmd=list\")'>";
	return;
    }

    $query = "DELETE FROM performanceconfig WHERE performanceconfig_id=$FORM_DATA{id}";
    my $sth = $dbh->prepare($query);
    if ( not $sth->execute() ) {
	my $title     = "Error Status";
	my $hq_errstr = HTML::Entities::encode( $sth->errstr );
	my $message   = "ERROR.&nbsp; Database access failed:<br><span class=error>$hq_errstr</span>";
	print "<table class=insightcontrolpanel cellspacing=0><tr><td class=insighthead>$title</td></tr><tr><td class=insightbody>$message</td></tr></table>";
	print "<INPUT class=orangebutton type=button value='Continue' onClick='changePage(\"$thisprogram?cmd=list\")'>";
	return;
    }

    my $title = "Status";
    $host    = HTML::Entities::encode($host);
    $service = HTML::Entities::encode($service);
    my $message = "Entry for host \"<tt>$host</tt>\", " . ( $service_regx ? 'service-name pattern' : 'service' ) . " \"<tt>$service</tt>\" has been deleted.";
    print "<table class=insightcontrolpanel cellspacing=0><tr><td class=insighthead>$title</td></tr><tr><td class=insightbody>$message</td></tr></table>";
    print "<INPUT class=orangebutton type=button value='Continue' onClick='changePage(\"$thisprogram?cmd=list\")'>";
}

#
#	Add a new entry form
#
sub new {
    my $help_url = StorProc->doc_section_url('How+to+configure+performance+graphs', 'Howtoconfigureperformancegraphs-CreatingaNewEntry');
    print "<input type=hidden name=cmd value=add>";
    print "<table class=insightcontrolpanel cellspacing=0>";
    print "<tr><td class=insightleft><b>Graph Label</b></td>
	<td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{graph_label}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	<td class=insight><INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=label VALUE=\"\"></td></tr>";
    print "<tr><td class=insightleft><b>Service</b></td>
	<td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{service}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	<td class=insight><INPUT CLASS=insight size=100 maxlength=255 TYPE=TEXT NAME=service VALUE=\"\"></td></tr>";
    print "<tr><td class=insightleft><b>Use Service as a Regular Expression</b></td>
	<td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{service_regx}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	<td class=insight><INPUT CLASS=insight TYPE=checkbox NAME=service_regx VALUE=1></td></tr>";
    print "<tr><td class=insightleft><b>Host</b></td>
	<td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{host}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	<td class=insight><INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=host VALUE=\"\"></td></tr>";
    ## print "<tr><td class=insightleft><b>Plugin ID</b></td>
    ##     <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{plugin_id}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
    ##     <td class=insight><INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=perfidstring VALUE=\"\"></td></tr>";
    print "<tr><td class=insightleft><b>Status Text Parsing Regular Expression</b></td>
	<td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{status_regx}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	<td class=insight><INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=parseregx VALUE=\"\"></td></tr>";
    print "<tr><td class=insightleft><b>Use Status Text Parsing instead of Performance Data</b></td>
	<td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{parse_status}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	<td class=insight><INPUT CLASS=insight TYPE=checkbox NAME=parseregx_first VALUE=1></td></tr>";
    print "<tr><td class=insightleft><b>RRD Name</b></td>
	<td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{rrd_name}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	<td class=insight><INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=rrdname VALUE=\"\"></td></tr>";
    print "<tr><td class=insightleft><b>RRD Create Command</b></td>
	<td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{rrd_create}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	<td class=insight><TEXTAREA CLASS=insight cols=100 rows=3 NAME=rrdcreatestring></TEXTAREA></td></tr>";
    print "<tr><td class=insightleft><b>RRD Update Command</b></td>
	<td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{rrd_update}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	<td class=insight><TEXTAREA CLASS=insight cols=100 rows=3 NAME=rrdupdatestring></TEXTAREA></td></tr>";
    print "<tr><td class=insightleft><b>Custom RRDtool Graph Command</b></td>
	<td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{rrd_graph}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	<td class=insight><TEXTAREA CLASS=insight cols=100 rows=12 NAME=graphcgi></TEXTAREA></td></tr>";
    print "<tr><td class=insightleft><b>Enable</b></td>
	<td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{enable}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	<td class=insight><INPUT CLASS=insight TYPE=checkbox NAME=enable VALUE=1 CHECKED></td></tr>";
    print "<tr><td class=insightbuttons colspan=3 align=center>
	<INPUT class=orangebutton type=submit value='Add'>&nbsp;
	<INPUT class=orangebutton type=button value='Cancel' onClick='changePage(\"$thisprogram?cmd=list\")'>&nbsp;
	<INPUT class=orangebutton type=button value='Help' name=help onclick=\"open_window('$help_url')\">
	</td></tr>";
    print "</table>";
    print "<br><br><br><br><br><br>";
}

#
#	Add a new entry
#
sub add {
    # Take into account the various columns we have declared to be NOT NULL.
    $FORM_DATA{host}            = '' if not defined $FORM_DATA{host};
    $FORM_DATA{service}         = '' if not defined $FORM_DATA{service};
    $FORM_DATA{label}           = '' if not defined $FORM_DATA{label};
    $FORM_DATA{perfidstring}    = '' if not defined $FORM_DATA{perfidstring};
    $FORM_DATA{parseregx}       = '' if not defined $FORM_DATA{parseregx};
    $FORM_DATA{rrdname}         = '' if not defined $FORM_DATA{rrdname};
    $FORM_DATA{rrdcreatestring} = '' if not defined $FORM_DATA{rrdcreatestring};
    $FORM_DATA{rrdupdatestring} = '' if not defined $FORM_DATA{rrdupdatestring};

    # Clean up the data.

    $FORM_DATA{host}    =~ s/^\s+//;
    $FORM_DATA{host}    =~ s/\s+$//;
    $FORM_DATA{service} =~ s/^\s+//;
    $FORM_DATA{service} =~ s/\s+$//;
    $FORM_DATA{label}   =~ s/^\s+//;
    $FORM_DATA{label}   =~ s/\s+$//;
    $FORM_DATA{rrdname} =~ s/^\s+//;
    $FORM_DATA{rrdname} =~ s/\s+$//;

    $FORM_DATA{rrdcreatestring} =~ tr/\r//d;
    $FORM_DATA{rrdcreatestring} =~ s/^\s+//;
    $FORM_DATA{rrdcreatestring} =~ s/\s+$//;

    $FORM_DATA{rrdupdatestring} =~ tr/\r//d;
    $FORM_DATA{rrdupdatestring} =~ s/^\s+//;
    $FORM_DATA{rrdupdatestring} =~ s/\s+$//;

    if ( defined $FORM_DATA{graphcgi} ) {
	if ( $FORM_DATA{graphcgi} =~ /^\s*'/ && $FORM_DATA{graphcgi} =~ /'\s*$/ ) {
	    ## print "stripping quotes and surrounding whitespace ... \n";
	    $FORM_DATA{graphcgi} =~ s/^\s*'//;
	    $FORM_DATA{graphcgi} =~ s/'\s*$//;
	}

	$FORM_DATA{graphcgi} =~ tr/\r//d;
	$FORM_DATA{graphcgi} =~ s/^\s+//;
	$FORM_DATA{graphcgi} =~ s/\s+$//;
    }

    my $q_host            = $dbh->quote( $FORM_DATA{host} );
    my $q_service         = $dbh->quote( $FORM_DATA{service} );
    my $q_label           = $dbh->quote( $FORM_DATA{label} );
    my $q_service_regx    = $dbh->quote( $FORM_DATA{service_regx} );
    my $q_perfidstring    = $dbh->quote( $FORM_DATA{perfidstring} );
    my $q_parseregx       = $dbh->quote( $FORM_DATA{parseregx} );
    my $q_parseregx_first = $dbh->quote( $FORM_DATA{parseregx_first} );
    my $q_rrdname         = $dbh->quote( $FORM_DATA{rrdname} );
    my $q_rrdcreatestring = $dbh->quote( $FORM_DATA{rrdcreatestring} );
    my $q_rrdupdatestring = $dbh->quote( $FORM_DATA{rrdupdatestring} );
    my $q_graphcgi        = $dbh->quote( $FORM_DATA{graphcgi} );
    my $q_enable          = $dbh->quote( $FORM_DATA{enable} );
    my $q_type            = $dbh->quote('nagios');

    my $hq_host    = HTML::Entities::encode( $FORM_DATA{host} );
    my $hq_service = HTML::Entities::encode( $FORM_DATA{service} );

    my $host_match    = defined( $FORM_DATA{host} )    ? "host = $q_host"       : "host is NULL";
    my $service_match = defined( $FORM_DATA{service} ) ? "service = $q_service" : "service is NULL";

    my $query = "SELECT performanceconfig_id FROM performanceconfig WHERE $host_match AND $service_match";
    my $sth   = $dbh->prepare($query);
    if ( not $sth->execute() ) {
	my $title     = "Error Status";
	my $hq_errstr = HTML::Entities::encode( $sth->errstr );
	my $message   = "ERROR. Database access failed:<br><span class=error>$hq_errstr</span>";
	print "<table class=insightcontrolpanel cellspacing=0><tr><td class=insighthead>$title</td></tr><tr><td class=insightbody>$message</td></tr></table>";
	print "<INPUT class=orangebutton type=button value='Continue' onClick='changePage(\"$thisprogram?cmd=list\")'>";
	return;
    }
    my $id = undef;
    while ( my $row = $sth->fetchrow_hashref() ) {
	$id = $$row{performanceconfig_id};
    }
    $sth->finish();
    if ($id) {
	my $title = "Error Status";
	my $message = "<span class=error>ERROR. Performance configuration already exists for host \"<tt>$hq_host</tt>\" and service \"<tt>$hq_service</tt>\".</span>";
	$message .= "<p class=append>Duplicate entries are not permitted. Delete the existing entry before adding this entry.</p>";
	print "<table class=insightcontrolpanel cellspacing=0><tr><td class=insighthead>$title</td></tr><tr><td class=insightbody>$message</td></tr></table>";
	print "<INPUT class=orangebutton type=button value='Continue' onClick='changePage(\"$thisprogram?cmd=list\")'>";
	return;
    }
    $query =
	"INSERT INTO performanceconfig "
      . "(label,host,service,service_regx,perfidstring,parseregx,parseregx_first,rrdname,rrdcreatestring,rrdupdatestring,graphcgi,type,enable)"
      . " VALUES ("
      . "$q_label,"
      . "$q_host,"
      . "$q_service,"
      . "$q_service_regx,"
      . "$q_perfidstring,"
      . "$q_parseregx,"
      . "$q_parseregx_first,"
      . "$q_rrdname,"
      . "$q_rrdcreatestring,"
      . "$q_rrdupdatestring,"
      . "$q_graphcgi,"
      . "$q_type,"
      . "$q_enable"
      . ");";
    if ( not $dbh->do($query) ) {
	my $title     = "Error Status";
	my $hq_errstr = HTML::Entities::encode( $dbh->errstr );
	my $message   = "ERROR. Database access failed:<br><span class=error>$hq_errstr</span>";
	print "<table class=insightcontrolpanel cellspacing=0><tr><td class=insighthead>$title</td></tr><tr><td class=insightbody>$message</td></tr></table>";
	print "<INPUT class=orangebutton type=button value='Continue' onClick='changePage(\"$thisprogram?cmd=list\")'>";
	return;
    }

    ## print "<br>Query=$query<br>";
    my $title = "Status";
    my $message = "Performance configuration for host \"<tt>$hq_host</tt>\" and service \"<tt>$hq_service</tt>\" has been added.";
    print "<table class=insightcontrolpanel cellspacing=0><tr><td class=insighthead>$title</td></tr><tr><td class=insightbody>$message</td></tr></table>";
    print "<INPUT class=orangebutton type=button value='Continue' onClick='changePage(\"$thisprogram?cmd=list\")'>";
}

sub export_form {
    my $help_url = StorProc->doc_section_url('About+Performance+Graphs', 'AboutPerformanceGraphs-ImportingandExportingPerformanceConfiguration');
    my $query = "SELECT * FROM performanceconfig WHERE performanceconfig_id=$FORM_DATA{id}";
    my $sth   = $dbh->prepare($query);
    if ( not $sth->execute() ) {
	my $title     = "Error Status";
	my $hq_errstr = HTML::Entities::encode( $sth->errstr );
	my $message   = "ERROR. Database access failed:<br><span class=error>$hq_errstr</span>";
	print "<table class=insightcontrolpanel cellspacing=0><tr><td class=insighthead>$title</td></tr><tr><td class=insightbody>$message</td></tr></table>";
	print "<INPUT class=orangebutton type=button value='Continue' onClick='changePage(\"$thisprogram?cmd=list\")'>";
	return;
    }
    while ( my $row = $sth->fetchrow_hashref() ) {
	my $id              = $$row{performanceconfig_id};
	my $host            = $$row{host};
	my $service         = $$row{service};
	my $service_regx    = $$row{service_regx};
	my $label           = $$row{label};
	my $rrdname         = $$row{rrdname};
	my $rrdcreatestring = $$row{rrdcreatestring};
	my $rrdupdatestring = $$row{rrdupdatestring};
	my $perfidstring    = $$row{perfidstring};
	my $graphcgi        = $$row{graphcgi};
	my $parseregx       = $$row{parseregx};
	my $parseregx_first = $$row{parseregx_first};
	my $enable          = $$row{enable};

	# Some versions of IE don't understand the "white-space: pre-wrap;" CSS we apply to long fields,
	# so we have to punt here and convert long strings of spaces to alternate non-blanking spaces to
	# achieve the same effect.  We don't convert both spaces because we still want to the browser to
	# be able to rearrange long words across lines if the browser width is adjusted.

	$rrdcreatestring = '' if not defined $rrdcreatestring;
	$rrdcreatestring = HTML::Entities::encode($rrdcreatestring);
	$rrdcreatestring =~ s/\r?\n/<br>/g;
	$rrdcreatestring =~ s/  /&nbsp; /g;

	$rrdupdatestring = '' if not defined $rrdupdatestring;
	$rrdupdatestring = HTML::Entities::encode($rrdupdatestring);
	$rrdupdatestring =~ s/\r?\n/<br>/g;
	$rrdupdatestring =~ s/  /&nbsp; /g;

	$graphcgi = '' if not defined $graphcgi;
	$graphcgi = HTML::Entities::encode($graphcgi);
	$graphcgi =~ s/\r?\n/<br>/g;
	$graphcgi =~ s/  /&nbsp; /g;

	my $title = "Export a Performance Config Entry";
	my $message = "This configuration will be written to the <tt>/tmp</tt> directory.";
	if ( $service =~ m{[\\/\$\(\)!\@&*?|~`'"\[\]\{\}<>;]} ) {
	    $message .= "<p class=append>NOTICE: The export file name cannot contain special punctuation characters.</p>";
	}

	$service_regx    = '' if not defined $service_regx;
	$parseregx_first = '' if not defined $parseregx_first;
	$enable          = '' if not defined $enable;

	print "<table class=insightcontrolpanel cellspacing=0><tr><td class=insighthead>$title</td></tr><tr><td class=insightsubhead>$message</td></tr></table>";
	print "<input type=hidden name=id value=$id>";
	print "<input type=hidden name=cmd value=export>";
	print "<table class=insightcontrolpanel cellspacing=0>";
	print "<tr><td class=insightleft><b>Export File Name</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{export_file}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight><INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=exportfile VALUE=\"perfconfig-$service.xml\"></td></tr>";
	print "<tr><td class=insightleft><b>Graph Label</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{graph_label}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight>$label</td></tr>";
	print "<tr><td class=insightleft><b>Service</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{service}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight>$service</td></tr>";
	print "<tr><td class=insightleft><b>Use Service as a Regular Expression</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{service_regx}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight>$service_regx</td></tr>";
	print "<tr><td class=insightleft><b>Host</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{host}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight>$host</td></tr>";
	## print "<tr><td class=insightleft><b>Plugin ID</b></td>
	##     <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{plugin_id}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	##     <td class=insight>$perfidstring</td></tr>";
	print "<tr><td class=insightleft><b>Status Text Parsing Regular Expression</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{status_regx}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight>$parseregx</td></tr>";
	print "<tr><td class=insightleft><b>Use Status Text Parsing instead of Performance Data</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{parse_status}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight>$parseregx_first</td></tr>";
	print "<tr><td class=insightleft><b>RRD Name</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{rrd_name}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight>$rrdname</td></tr>";
	print "<tr><td class=insightleft><b>RRD Create Command</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{rrd_create}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight>$rrdcreatestring</td></tr>";
	print "<tr><td class=insightleft><b>RRD Update Command</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{rrd_update}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight>$rrdupdatestring</td></tr>";
	print "<tr><td class=insightleft><b>Custom RRDtool Graph Command</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{rrd_graph}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight>$graphcgi</td></tr>";
	print "<tr><td class=insightleft><b>Enable</b></td>
	    <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{enable}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	    <td class=insight>$enable</td></tr>";
	print "<tr><td class=insightbuttons colspan=3 align=center>
		<INPUT class=orangebutton type=submit value='Export'>&nbsp;
		<INPUT class=orangebutton type=button value='Cancel' onClick='changePage(\"$thisprogram?cmd=list\")'>&nbsp;
		<INPUT class=orangebutton type=button value='Help' name=help onclick=\"open_window('$help_url')\">
		</td></tr>";
	print "</table>";
	print "<br>";
    }
    $sth->finish();
}

sub export {
    my $query = "SELECT * FROM performanceconfig WHERE performanceconfig_id=$FORM_DATA{id}";
    my $sth   = $dbh->prepare($query);
    if ( not $sth->execute() ) {
	my $title     = "Error Status";
	my $hq_errstr = HTML::Entities::encode( $sth->errstr );
	my $message   = "ERROR. Database access failed:<br><span class=error>$hq_errstr</span>";
	print "<table class=insightcontrolpanel cellspacing=0><tr><td class=insighthead>$title</td></tr><tr><td class=insightbody>$message</td></tr></table>";
	print "<INPUT class=orangebutton type=button value='Continue' onClick='changePage(\"$thisprogram?cmd=list\")'>";
	return;
    }
    while ( my $row = $sth->fetchrow_hashref() ) {
	my $id              = $$row{performanceconfig_id};
	my $host            = $$row{host};
	my $service         = $$row{service};
	my $service_regx    = $$row{service_regx};
	my $rrdname         = $$row{rrdname};
	my $rrdcreatestring = $$row{rrdcreatestring};
	my $rrdupdatestring = $$row{rrdupdatestring};
	my $perfidstring    = $$row{perfidstring};
	my $graphcgi        = $$row{graphcgi};
	my $parseregx       = $$row{parseregx};
	my $parseregx_first = $$row{parseregx_first};
	my $enable          = $$row{enable};
	my $label           = $$row{label};

	$service_regx    = '' if not defined $service_regx;
	$parseregx_first = '' if not defined $parseregx_first;
	$enable          = '' if not defined $enable;

	my $xmlstring =
	    "<groundwork_performance_configuration>\n"
	  . "<service_profile name=\"$service profile\">\n"
	  . "<graph name=\"graph\">\n"
	  . "<host>$host</host>\n"
	  . "<service regx=\"$service_regx\"><![CDATA[$service]]></service>\n"
	  . "<type>nagios</type>\n"
	  . "<enable>$enable</enable>\n"
	  . "<label>$label</label>\n"
	  . "<rrdname><![CDATA[$rrdname]]></rrdname>\n"
	  . "<rrdcreatestring><![CDATA[$rrdcreatestring]]></rrdcreatestring>\n"
	  . "<rrdupdatestring><![CDATA[$rrdupdatestring]]></rrdupdatestring>\n"
	  . "<graphcgi><![CDATA[$graphcgi]]></graphcgi>\n"
	  . "<parseregx first=\"$parseregx_first\"><![CDATA[$parseregx]]></parseregx>\n"
	  . "<perfidstring>$perfidstring</perfidstring>\n"
	  . "</graph>\n"
	  . "</service_profile>\n"
	  . "</groundwork_performance_configuration>";
	## We validate the entered filename not just against "/" (to prevent escaping to some other directory),
	## but also against a variety of shell metacharacters, to avoid potential confusion when the filename
	## is interpreted later on.
	if ( $FORM_DATA{exportfile} =~ m{[\\/\$\(\)!\@&*?|~`'"\[\]\{\}<>;]} ) {
	    my $title   = "Error Status";
	    my $message = "<span class=error>ERROR. The export file name cannot contain special punctuation characters.</span>";
	    print "<table class=insightcontrolpanel cellspacing=0><tr><td class=insighthead>$title</td></tr><tr><td class=insightbody>$message</td></tr></table>";
	    print "<INPUT class=orangebutton type=button value='Continue' onClick='changePage(\"$thisprogram?cmd=list\")'>";
	}
	elsif ( not open( OUT, '>', "/tmp/$FORM_DATA{exportfile}" ) ) {
	    my $status        = "$!";
	    my $hq_exportfile = HTML::Entities::encode( $FORM_DATA{exportfile} );
	    my $title         = "Error Status";
	    my $message       = "<span class=error>ERROR. Cannot open file <tt>/tmp/$hq_exportfile</tt> ($status).</span>";
	    print "<table class=insightcontrolpanel cellspacing=0><tr><td class=insighthead>$title</td></tr><tr><td class=insightbody>$message</td></tr></table>";
	    print "<INPUT class=orangebutton type=button value='Continue' onClick='changePage(\"$thisprogram?cmd=list\")'>";
	}
	else {
	    print OUT $xmlstring;
	    close OUT;
	    my $hq_exportfile = HTML::Entities::encode( $FORM_DATA{exportfile} );
	    my $hq_xmlstring  = HTML::Entities::encode( $xmlstring );
	    my $title = "Status";
	    my $message = "The following XML string was written to the file:&nbsp; <tt>/tmp/$hq_exportfile</tt>";
	    print "<table class=insightcontrolpanel cellspacing=0><tr><td class=insighthead>$title</td></tr><tr><td class=insightbody>$message</td></tr></table>";
	    print "<table class=insightcontrolpanel cellspacing=0><tr><td class=insightpreformatted><PRE style='white-space: pre-wrap;'>$hq_xmlstring</PRE></td></tr></table>";
	    print "<INPUT class=orangebutton type=button value='Return to list' onClick='changePage(\"$thisprogram?cmd=list\")'>";
	    print "<br>";
	}
    }
    $sth->finish();
}

sub export_all {
    my $query = "SELECT * FROM performanceconfig";
    my $sth   = $dbh->prepare($query);
    if ( not $sth->execute() ) {
	my $title     = "Error Status";
	my $hq_errstr = HTML::Entities::encode( $sth->errstr );
	my $message   = "ERROR. Database access failed:<br><span class=error>$hq_errstr</span>";
	print "<table class=insightcontrolpanel cellspacing=0><tr><td class=insighthead>$title</td></tr><tr><td class=insightbody>$message</td></tr></table>";
	print "<INPUT class=orangebutton type=button value='Continue' onClick='changePage(\"$thisprogram?cmd=list\")'>";
	return;
    }
    if ( not open( OUT, '>', '/tmp/perfconfig-ALL.xml' ) ) {
	my $status        = "$!";
	my $title         = "Error Status";
	my $message       = "<span class=error>ERROR. Cannot open file <tt>/tmp/perfconfig-ALL.xml</tt> ($status).</span>";
	print "<table class=insightcontrolpanel cellspacing=0><tr><td class=insighthead>$title</td></tr><tr><td class=insightbody>$message</td></tr></table>";
	print "<INPUT class=orangebutton type=button value='Continue' onClick='changePage(\"$thisprogram?cmd=list\")'>";
	return;
    }
    print OUT "<groundwork_performance_configuration>\n";
    while ( my $row = $sth->fetchrow_hashref() ) {
	my $id              = $$row{performanceconfig_id};
	my $host            = $$row{host};
	my $service         = $$row{service};
	my $service_regx    = $$row{service_regx};
	my $rrdname         = $$row{rrdname};
	my $rrdcreatestring = $$row{rrdcreatestring};
	my $rrdupdatestring = $$row{rrdupdatestring};
	my $perfidstring    = $$row{perfidstring};
	my $graphcgi        = $$row{graphcgi};
	my $parseregx       = $$row{parseregx};
	my $parseregx_first = $$row{parseregx_first};
	my $enable          = $$row{enable};
	my $label           = $$row{label};

	$service_regx    = '' if not defined $service_regx;
	$parseregx_first = '' if not defined $parseregx_first;
	$enable          = '' if not defined $enable;

	my $xmlstring =
	    "<service_profile name=\"$service profile\">\n"
	  . "<graph name=\"graph\">\n"
	  . "<host>$host</host>\n"
	  . "<service regx=\"$service_regx\"><![CDATA[$service]]></service>\n"
	  . "<type>nagios</type>\n"
	  . "<enable>$enable</enable>\n"
	  . "<label>$label</label>\n"
	  . "<rrdname><![CDATA[$rrdname]]></rrdname>\n"
	  . "<rrdcreatestring><![CDATA[$rrdcreatestring]]></rrdcreatestring>\n"
	  . "<rrdupdatestring><![CDATA[$rrdupdatestring]]></rrdupdatestring>\n"
	  . "<graphcgi><![CDATA[$graphcgi]]></graphcgi>\n"
	  . "<parseregx first=\"$parseregx_first\"><![CDATA[$parseregx]]></parseregx>\n"
	  . "<perfidstring>$perfidstring</perfidstring>\n"
	  . "</graph>\n"
	  . "</service_profile>\n";
	print OUT $xmlstring;
    }
    $sth->finish();
    print OUT "</groundwork_performance_configuration>";
    close OUT;
    my $title = "Status";
    my $message = "The exported configuration was written to the file:&nbsp; <tt>/tmp/perfconfig-ALL.xml</tt>";
    print "<table class=insightcontrolpanel cellspacing=0><tr><td class=insighthead>$title</td></tr><tr><td class=insightbody>$message</td></tr></table>";
    print "<INPUT class=orangebutton type=button value='Return to list' onClick='changePage(\"$thisprogram?cmd=list\")'>";
    print "<br>";
}

sub show_test_command {
    my $rrdname      = shift;
    my $host         = shift;
    my $service      = shift;
    my $service_regx = shift;
    my $graphcgi     = shift;
    my $user_test    = shift;
    local $_;

    # FIX MAJOR:  react appropriately if $graphcgi is an empty or completely-blank string (after
    # stripping enclosing single-quotes, and substituting backslash+whitespace character sequences)

    # We scan the filesystem for RRD files that match the $rrdname format, with appropriate substitutions.
    # But to simplify matters, we base the search on filenames recorded in Monarch by the performance data
    # handler.  From that list, we choose those that match $host, $service, and $service_regx constraints.

    # The standard form of $rrdname is:
    #     /usr/local/groundwork/rrd/$HOST$_$SERVICE$.rrd
    # but this is not guaranteed, so a hierarchy of directories might need to be scanned.
    # You might even find this kind of pattern, with duplicate substitutions:
    #     /usr/local/groundwork/rrd/$HOST$/$HOST$_$SERVICE$.rrd

    my $query = "SELECT hs.host, hs.service, dt.location FROM host_service hs, datatype dt WHERE dt.datatype_id = hs.datatype_id and dt.type = 'RRD'";
    my $sth   = $dbh->prepare($query);
    if ( not $sth->execute() ) {
	my $title     = "Error Status";
	my $hq_errstr = HTML::Entities::encode( $sth->errstr );
	my $message   = "ERROR. Database access failed:<br><span class=error>$hq_errstr</span>";
	print "<tr><td class=insighthead colspan=3>$title</td></tr><tr><td class=insightbody colspan=3>$message</td></tr>";
	return;
    }
    my $rrd_host    = undef;
    my $rrd_service = undef;
    my $rrd_file    = undef;
    my $file_path   = undef;
    my %rrds        = ();
    while ( my $row = $sth->fetchrow_hashref() ) {
	$rrd_host    = $$row{host};
	$rrd_service = $$row{service};
	$rrd_file    = $$row{location};
	if (   ( $host eq '*' || $rrd_host eq $host )
	    && ( ( !$service_regx && $rrd_service eq $service ) || ( $service_regx && $rrd_service =~ qr{$service} ) ) )
	{
	    ( $file_path = $rrdname ) =~ s/\$HOST\$/$rrd_host/g;
	    $file_path =~ s/\$SERVICE\$/$rrd_service/g;
	    $rrds{"$rrd_service - $rrd_host"} = $rrd_file if $rrd_file eq $file_path && -f $rrd_file;
	}
    }
    $sth->finish();

    # FIX MAJOR:  react appropriately, or show some alternate text, if no existing graphs showed up

    my %services_and_hosts = reverse %rrds;
    my %selected_rrd       = map { $_ => '' } keys %rrds;
    $selected_rrd{ $services_and_hosts{ $FORM_DATA{rrdfile} } } = 'selected' if $FORM_DATA{rrdfile};

    my @times = qw(Hour Day Week Month Quarter Year);
    my $timeperiod = $FORM_DATA{timeperiod};
    $timeperiod = 'Hour' if not $timeperiod;
    my %selected_time = map { $_ => '' } @times;
    $selected_time{$timeperiod} = 'selected';

    my $HOURS_PER_HOUR   = 1;
    my $HOURS_PER_DAY    = 24;
    my $DAYS_PER_WEEK    = 7;
    my $DAYS_PER_MONTH   = 31;
    my $DAYS_PER_QUARTER = 91;
    my $DAYS_PER_YEAR    = 365;

    my $HOURS_PER_WEEK    = $HOURS_PER_DAY * $DAYS_PER_WEEK;
    my $HOURS_PER_MONTH   = $HOURS_PER_DAY * $DAYS_PER_MONTH;
    my $HOURS_PER_QUARTER = $HOURS_PER_DAY * $DAYS_PER_QUARTER;
    my $HOURS_PER_YEAR    = $HOURS_PER_DAY * $DAYS_PER_YEAR;

    my $graph_hours =
	$timeperiod eq 'Hour'    ? $HOURS_PER_HOUR
      : $timeperiod eq 'Day'     ? $HOURS_PER_DAY
      : $timeperiod eq 'Week'    ? $HOURS_PER_WEEK
      : $timeperiod eq 'Month'   ? $HOURS_PER_MONTH
      : $timeperiod eq 'Quarter' ? $HOURS_PER_QUARTER
      : $timeperiod eq 'Year'    ? $HOURS_PER_YEAR
      :                            ( $HOURS_PER_YEAR * 3 );

    my $rrd_select = "<select name=rrdfile class=insight><option class=insight value=''></option>\n";
    $rrd_select .= "<option class=insight value='$rrds{$_}' $selected_rrd{$_}>$_</option>\n" for sort keys %rrds;
    $rrd_select .= "</select>";

    my $time_select = "<select name=timeperiod class=insight>\n";
    $time_select .= "<option class=insight value='$_' $selected_time{$_}>$_</option>\n" for @times;
    $time_select .= "</select>";

    print "<tr>
	<td class=insighthead colspan=3>
	Select&nbsp;Service-Host&nbsp;Pair&nbsp;for&nbsp;Testing&nbsp;the&nbsp;RRDtool&nbsp;Graph&nbsp;Command:&nbsp;&nbsp; $rrd_select
	&nbsp;&nbsp; Time&nbsp;Period:&nbsp;&nbsp; $time_select
	</td>
	</tr>";

    # FIX MAJOR:  Make sure no unexpected changes get propagated from
    # here back upstream to the display of individual fields like $graphcgi
    # (I think that problem has since been solved, but we need to test anyway).

    # FIX MAJOR:  fix the branch logic here for having or not having an RRD filename in hand
    my $result = '';
    if ( $user_test and not $graphcgi ) {
	## If the graph command from the "DEFAULT"-service entry will be used downstream when
	## no graph command has been defined for this entry, substitute that graph command here.
	## If that is the case, only complain here if that cannot be found or is empty or blank.

	# FIX MINOR:  Set a flag if the DEFAULT graph command is in use, and say so in the final
	# display of the resulting graph.

	# First find the default RRD Graph command and assign it.
	$query = "SELECT * FROM performanceconfig where type='nagios' and enable='1' and service = 'DEFAULT'";
	$sth   = $dbh->prepare($query);
	if ( not $sth->execute() ) {
	    my $title     = "Error Status";
	    my $hq_errstr = HTML::Entities::encode( $sth->errstr );
	    my $message   = "ERROR. Database access failed:<br><span class=error>$hq_errstr</span>";
	    print "<tr><td class=insighthead colspan=3>$title</td></tr><tr><td class=insightbody colspan=3>$message</td></tr>";
	    return;
	}
	while ( my $row = $sth->fetchrow_hashref() ) {
	    $graphcgi = $$row{graphcgi};
	    last;
	}
	$sth->finish();

	# Provide a default for the DEFAULT, just in case ...
	if ( !defined($graphcgi) ) {
	    $graphcgi = 'rrdtool graph - $LISTSTART$ DEF:$DEFLABEL#$:AVERAGE CDEF:cdef$CDEFLABEL#$=$CDEFLABEL#$ LINE2:$CDEFLABEL#$$COLORLABEL#$:$DSLABEL#$ GPRINT:$CDEFLABEL#$:MIN:min=%.2lf GPRINT:$CDEFLABEL#$:AVERAGE:avg=%.2lf GPRINT:$CDEFLABEL#$:MAX:max=%.2lf  $LISTEND$  -c BACK#FFFFFF -c CANVAS#FFFFFF -c GRID#C0C0C0 -c MGRID#404040 -c ARROW#FFFFFF -Y --height 120';
	}
    }

    if ( not $result and $user_test and not $graphcgi ) {
	$result = 'You must have a Custom RRDtool Graph Command defined, either for this service or the DEFAULT service, in order to run a test.';
    }
    elsif ( not $result and $user_test and not $FORM_DATA{rrdfile} ) {
	$result =
	    'To test the Custom RRDtool Graph Command, you must first select a service-host pair'
	  . ' that uses this performance-config entry and already has an RRD file created.'
	  . ' The RRD file is created from having the service check run a few times with performance data generated,'
	  . ' after the RRD Create and Update commands in this perf-config entry are in place.'
	  . '  Those commands then get exercised by the software to create and start populating the RRD file.'
	  . '  The currently available service-host candidates are listed in the menu just above.';
    }
    elsif ( !$result && $graphcgi && $FORM_DATA{rrdfile} ) {
	## FIX MAJOR:  If it's necessary to decode $graphcgi in this context, why not
	## in other contexts as well, especially when user-input data is being saved?
	$graphcgi = HTML::Entities::decode($graphcgi);

	# FIX MAJOR:  drop this; but make sure that :ds_source_#: is properly handled, as well as $LISTSTART$ ... $LISTEND$ and other aspects
	if ($graphcgi =~ /\$LISTSTART\$/) {
	    ## FIX MAJOR:  Deal with any $LISTSTART$ ... $LISTEND$ substitutions for this graph command, including these substitutions:
	    ## $DEFLABEL#$
	    ## $CDEFLABEL#$
	    ## $COLORLABEL#$
	    ## $DSLABEL#$
	}

	my %macros = (
	    '\$RRDTOOL\$'     => $rrdtool,
	    '\$RRDNAME\$'     => $FORM_DATA{rrdfile},
	    '\$LASTCHECK\$'   => '{last check time from a service check}',
	    '\$HOST\$'        => $host,
	    '\$SERVICETEXT\$' => '{status text from a service check}',
	    '\$SERVICE\$'     => $service
	);

	my $errors = undef;
	($graphcgi, $errors) = replace_graphing_macros( $graphcgi, $FORM_DATA{rrdfile}, \%macros );

	if (@$errors) {
	    # FIX MAJOR:  Pay attention to $errors; reflect them into the UI, and abort further processing here.
	}

	# FIX MAJOR:  Much of the rest of these substitutions, if not all, just got dealt with
	# in the call to the replace_graphing_macros() routine.  So drop the duplicate code.

	# FIX MAJOR:  This should happen much earlier, even before the call to replace_graphing_macros(),
	# before we start testing $graphcgi to see if it is empty.
	if ($graphcgi =~ /^'/ and $graphcgi =~ /'$/) {
	    $graphcgi =~ s/^'//;
	    $graphcgi =~ s/'$//;
	}

	# replace the string rrd_source with the RRD file, making sure the filename always ends up safely quoted
	(my $sanitized_file = $FORM_DATA{rrdfile}) =~ s/:/\\:/g;
	my $lead;
	my $trail;
	$graphcgi =~ s/(?<=\s)(\S*)rrd_source(\S*)(?=\s)/
	    ($lead = $1, $trail = $2, $lead =~ m{"} && $trail =~ m{"})
	    ? "$lead$sanitized_file$trail" : "$lead\"$sanitized_file\"$trail"
	/eg;

	# FIX MAJOR:  Validate the $graphcgi command to ensure that it invokes rrdtool and nothing else.
	# Among other checks, disallow back-quotes, along with the equivalent bash $(...) syntax.
	# Allow the $RRDTOOL$ macro to be expanded, as well, if other parts of the system do so.
	# Also figure out what is supposed to be done with "/" as the entire $graphcgi command
	# (e.g., as specified for the gdma_21_wmi_memory_pages service).  Also look at invocations
	# of other commands, such as "/graphs/cgi-bin/number_graph.cgi" as the entire $graphcgi
	# command for the local_mysql_engine service.

	require RRDs;

	my $info;
	my $ERR;
	## $info is a hashref here.
	$info = RRDs::info($FORM_DATA{rrdfile});
	$ERR = RRDs::error();
	if ($ERR) {
	    $result =
		'ERROR:  Cannot process RRD file "'
	      . HTML::Entities::encode( $FORM_DATA{rrdfile} )
	      . '".<br>Failed RRD info command: '
	      . HTML::Entities::encode($ERR);
	}

	## Prepare for substitution of all ":ds_source_#:" strings.
	my @ds_list = ();
	unless ($result) {
	    ## We have to trap the DS names in a list ordered by DS sequence number for the custom graph command.
	    foreach my $in (keys %$info) {
		if ( $in =~ /^ds\[(\S+)\]\.index$/ ) {
		    $ds_list[ $$info{$in} ] = $1;
		}
	    }
	    if ( not @ds_list ) {
		$result =
		    'ERROR:  Cannot process RRD file "'
		  . HTML::Entities::encode( $FORM_DATA{rrdfile} )
		  . '".<br>Invalid RRD file (no data sources found);'
		  . ' try to repair the file, or delete and allow the system to re-create it.';
	    }
	}

	## Substitute all ":ds_source_#:" strings.
	unless ($result) {
	    ## Simple case of direct substitution of numbered ds_source_n
	    ## replace the string ds_source_(number) with the DS number we found
	    my $raw_graphcgi = $graphcgi;
	    for ( my $j = 0 ; $j < @ds_list ; $j++ ) {
		## Replace the string ds_source_(number) with the DS number we found.
		## Bounding punctuation is used to avoid confusion of ds_source_1 and
		## ds_source_10 strings.  (Alternatively, we could count down instead of
		## up, and effectively do this without forcing the bounding punctuation.)
		my $ds_name = 'ds_source_' . "$j";
		$graphcgi =~ s/:$ds_name:/:$ds_list[$j]:/g;
	    }
	    if ( $graphcgi =~ /:ds_source_\d+:/ ) {
		my @data_sources = ( $graphcgi =~ /(:ds_source_\d+:)/g );
		my $indent = '<br>&nbsp;&nbsp;&nbsp; ';
		$result = 'Cannot create a graph, because the graph command contains unresolved :ds_source_#: references:';
		$result .= $indent . join( $indent, @data_sources );
		my $substitutions        = '';
		my $actual_substitutions = '';
		for ( my $j = 0 ; $j < @ds_list ; $j++ ) {
		    my $ds_name      = 'ds_source_' . "$j";
		    my $substitution = "$indent:$ds_name: => :$ds_list[$j]:";
		    $substitutions .= $substitution;
		    $actual_substitutions .= $substitution if $raw_graphcgi =~ /:$ds_name:/;
		}
		if ($actual_substitutions) {
		    $result .= '<br>The following data-source references will be substituted from the data sources found in the RRD file:'
		      . $actual_substitutions;
		}
		else {
		    my $plural = @ds_list == 1 ? '' : 's';
		    $result .=
			'<br>No data-source references match the actual data sources found in the RRD file.  The RRD file contains '
		      . @ds_list
		      . " data source$plural, which would be substituted this way:$substitutions";
		}
	    }
	}

	unless ($result) {
	    ## This will shell-escape any single-quote characters within the graph command, to protect against
	    ## the fact that such characters will be embedded within a single-quoted string which is passed to
	    ## the exec_rrdgraph.pl script.  However, note that the exec_rrdgraph.pl script will itself process
	    ## quotes when it tries to execute the command, and it won't understand unbalanced quotes.  So we
	    ## need extra quoting to get around that processing.
	    ##
	    ## FIX MAJOR:  Test against practical use of single-quote characters in a graph command, to ensure
	    ## that we get the intended result.
	    ##
	    ## FIX MAJOR:  Compare to what happens when the RRD graph command is used in the Performance View.
	    $graphcgi =~ s/'/'"\\'"'/g;

	    ## Add the options needed to specify a particular time span for the graph.
	    my $other_options = "\n--end now --start end-${graph_hours}hours";
	    ## We add an image format just like Status Viewer will, but here only if there isn't already one in the graph command.
	    $other_options .= ' --imgformat PNG' if $graphcgi !~ /\s(--imgformat|-a)\s/;
	    ## We add a width just like Status Viewer will, but here only if there isn't already one in the graph command.
	    $other_options .= ' --width 648' if $graphcgi !~ /\s(--width|-w)\s/;

	    my $graph_command = "/usr/local/groundwork/common/bin/exec_rrdgraph.pl '$graphcgi $other_options' 1 1";

	    # We preserve newlines for screen display, but must turn them into spaces for execution.
	    (my $executed_graph_command = $graph_command) =~ s/\n/ /g;

	    $result = qx($executed_graph_command);
	    my $wait_status = $?;
	    if ( $wait_status != 0 ) {
		my $status_message = wait_status_message($wait_status);
		## In our testing, IE8 doesn't seem to understand the "white-space: pre-wrap;" CSS that will be
		## applied to this message, despite some MSDN claims that this CSS value ought to be supported in
		## that browser.  So we need to force "white-space: pre;" instead in such an environment as a poor
		## compromise.  Hence the use of complicated conditional comments that are only understood by IE.
		$result = "<!--[if lte IE 8]><pre><![endif]-->Fully expanded graph command was:\n\n$graph_command\n\n$result\nRRDtool failed with $status_message.<!--[if lte IE 8]></pre><![endif]-->";
	    }
	    else {
		## To see how this <img> works, Google this:  javascript image byte array
		## http://stackoverflow.com/questions/20756042/javascript-how-to-display-image-from-byte-array-using-javascript-or-servlet
		## http://stackoverflow.com/questions/9463981/displaying-byte-array-as-image-using-javascript
		## Here we assume the image type is PNG, inasmuch as none of the other types supported by rrdtool (SVG, EPS, PDF)
		## seem to make sense here.  There is no hint that rrdtool supports GIF, so we don't worry about that.
		require APR::Base64;
		my $imagebytes = APR::Base64::encode($result);
		$result = "<img src='data:image/png;base64,$imagebytes' alt='RRD graph image'>";
		$result .= "<br>Executed graph command:<br>$executed_graph_command" if $show_executed_command;
	    }
	}
    }

    # Force the browser to scroll down here if graph display was attempted from the Test button,
    # so the full graph is automatically visible when the refresh is done.
    my $show_scrolled = $FORM_DATA{test} && $FORM_DATA{test} eq "Test";
    my $scroll_anchor = $show_scrolled ? 'update' : '';

    print qq(<tr>
	<td class=insightleft>
	<script type="text/javascript" language=JavaScript>
	    function ScrollWindow() {
		document.getElementById( '$scroll_anchor' ).scrollIntoView(false);
	    }
	    if (window.attachEvent) {window.attachEvent('onload', ScrollWindow);}
	    else if (window.addEventListener) {window.addEventListener('load', ScrollWindow, false);}
	    else {document.addEventListener('load', ScrollWindow, false);}
	</script>
	<INPUT class=orangebutton type=submit name=test value='Test'></td>
	<td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{test}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	<td class=insight>$result</td>
	</tr>);
}

# Run the RRD graph command and display the results (both text and picture) on-screen, while leaving all the other fields unchanged.
sub test {
    my $help_url = StorProc->doc_section_url('How+to+configure+performance+graphs', 'Howtoconfigureperformancegraphs-CreatingaNewEntry');

    # Take into account the various columns we have declared to be NOT NULL.
    $FORM_DATA{host}            = '' if not defined $FORM_DATA{host};
    $FORM_DATA{service}         = '' if not defined $FORM_DATA{service};
    $FORM_DATA{label}           = '' if not defined $FORM_DATA{label};
    $FORM_DATA{perfidstring}    = '' if not defined $FORM_DATA{perfidstring};
    $FORM_DATA{parseregx}       = '' if not defined $FORM_DATA{parseregx};
    $FORM_DATA{rrdname}         = '' if not defined $FORM_DATA{rrdname};
    $FORM_DATA{rrdcreatestring} = '' if not defined $FORM_DATA{rrdcreatestring};
    $FORM_DATA{rrdupdatestring} = '' if not defined $FORM_DATA{rrdupdatestring};

    # Clean up the data.

    $FORM_DATA{host}    =~ s/^\s+//;
    $FORM_DATA{host}    =~ s/\s+$//;
    $FORM_DATA{service} =~ s/^\s+//;
    $FORM_DATA{service} =~ s/\s+$//;
    $FORM_DATA{label}   =~ s/^\s+//;
    $FORM_DATA{label}   =~ s/\s+$//;
    $FORM_DATA{rrdname} =~ s/^\s+//;
    $FORM_DATA{rrdname} =~ s/\s+$//;

    $FORM_DATA{rrdcreatestring} =~ tr/\r//d;
    $FORM_DATA{rrdcreatestring} =~ s/^\s+//;
    $FORM_DATA{rrdcreatestring} =~ s/\s+$//;

    $FORM_DATA{rrdupdatestring} =~ tr/\r//d;
    $FORM_DATA{rrdupdatestring} =~ s/^\s+//;
    $FORM_DATA{rrdupdatestring} =~ s/\s+$//;

    if ( defined $FORM_DATA{graphcgi} ) {
	if ( $FORM_DATA{graphcgi} =~ /^\s*'/ && $FORM_DATA{graphcgi} =~ /'\s*$/ ) {
	    ## print "stripping quotes and surrounding whitespace ... \n";
	    $FORM_DATA{graphcgi} =~ s/^\s*'//;
	    $FORM_DATA{graphcgi} =~ s/'\s*$//;
	}

	$FORM_DATA{graphcgi} =~ tr/\r//d;
	$FORM_DATA{graphcgi} =~ s/^\s+//;
	$FORM_DATA{graphcgi} =~ s/\s+$//;
    }

    my %checked = ( 0 => '', 1 => 'CHECKED' );

    my $id              = $FORM_DATA{id};
    my $host            = $FORM_DATA{host};
    my $service         = $FORM_DATA{service};
    my $service_regx    = $FORM_DATA{service_regx};
    my $label           = $FORM_DATA{label};
    my $rrdname         = $FORM_DATA{rrdname};
    my $rrdcreatestring = $FORM_DATA{rrdcreatestring};
    my $rrdupdatestring = $FORM_DATA{rrdupdatestring};
    my $perfidstring    = $FORM_DATA{perfidstring};
    my $graphcgi        = $FORM_DATA{graphcgi};
    my $parseregx       = $FORM_DATA{parseregx};
    my $parseregx_first = $FORM_DATA{parseregx_first};
    my $enable          = $FORM_DATA{enable};

    $rrdcreatestring = '' if not defined $rrdcreatestring;
    $rrdcreatestring = HTML::Entities::encode($rrdcreatestring);
    $rrdcreatestring =~ tr/\r//d;
    my @rrdcreatestring = split( /\n/, $rrdcreatestring );
    my $rrdcreatestring_rows = @rrdcreatestring;
    foreach my $cgi_row (@rrdcreatestring) {
	$rrdcreatestring_rows += int(length($cgi_row) / 97);
    }
    $rrdcreatestring_rows = 3  if $rrdcreatestring_rows < 3;
    $rrdcreatestring_rows = 50 if $rrdcreatestring_rows > 50;

    $rrdupdatestring = '' if not defined $rrdupdatestring;
    $rrdupdatestring = HTML::Entities::encode($rrdupdatestring);
    $rrdupdatestring =~ tr/\r//d;
    my @rrdupdatestring = split( /\n/, $rrdupdatestring );
    my $rrdupdatestring_rows = @rrdupdatestring;
    foreach my $cgi_row (@rrdupdatestring) {
	$rrdupdatestring_rows += int(length($cgi_row) / 97);
    }
    $rrdupdatestring_rows = 3  if $rrdupdatestring_rows < 3;
    $rrdupdatestring_rows = 50 if $rrdupdatestring_rows > 50;

    $graphcgi = '' if not defined $graphcgi;
    $graphcgi = HTML::Entities::encode($graphcgi);
    $graphcgi =~ tr/\r//d;
    my @graphcgi = split( /\n/, $graphcgi );
    my $graphcgi_rows = @graphcgi;
    foreach my $cgi_row (@graphcgi) {
	$graphcgi_rows += int(length($cgi_row) / 97);
    }
    $graphcgi_rows = 3  if $graphcgi_rows < 3;
    $graphcgi_rows = 50 if $graphcgi_rows > 50;

    print "<input type=hidden name=id value=$id>";
    print "<input type=hidden name=cmd value=update>";
    print "<table class=insightcontrolpanel cellspacing=0>";
    print "<tr><td class=insightleft><b>Graph Label</b></td>
	<td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{graph_label}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	<td class=insight><INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=label VALUE=\"$label\"></td></tr>";
    print "<tr><td class=insightleft><b>Service</b></td>
	<td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{service}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	<td class=insight><INPUT CLASS=insight size=100 maxlength=255 TYPE=TEXT NAME=service VALUE=\"$service\"></td></tr>";
    print "<tr><td class=insightleft><b>Use Service as a Regular Expression</b></td>
	<td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{service_regx}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	<td class=insight><INPUT CLASS=insight TYPE=checkbox NAME=service_regx VALUE=1 $checked{$service_regx||0}></td></tr>";
    print "<tr><td class=insightleft><b>Host</b></td>
	<td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{host}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	<td class=insight><INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=host VALUE=\"$host\"></td></tr>";
    ## print "<tr><td class=insightleft><b>Plugin ID</b></td>
    ##     <td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{plugin_id}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
    ##     <td class=insight><INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=perfidstring VALUE=\"$perfidstring\"></td></tr>";
    print "<tr><td class=insightleft><b>Status Text Parsing Regular Expression</b></td>
	<td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{status_regx}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	<td class=insight><INPUT CLASS=insight size=100 maxlength=100 TYPE=TEXT NAME=parseregx VALUE=\"$parseregx\"></td></tr>";
    print "<tr><td class=insightleft><b>Use Status Text Parsing instead of Performance Data</b></td>
	<td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{parse_status}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	<td class=insight><INPUT CLASS=insight TYPE=checkbox NAME=parseregx_first VALUE=1 $checked{$parseregx_first||0}></td></tr>";
    print "<tr><td class=insightleft><b>RRD Name</b></td>
	<td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{rrd_name}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	<td class=insight><INPUT CLASS=insight size=100 maxlength=256 TYPE=TEXT NAME=rrdname VALUE=\"$rrdname\"></td></tr>";
    print "<tr><td class=insightleft><b>RRD Create Command</b></td>
	<td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{rrd_create}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	<td class=insight><TEXTAREA CLASS=insight cols=100 rows=$rrdcreatestring_rows NAME=rrdcreatestring>$rrdcreatestring</TEXTAREA></td></tr>";
    print "<tr><td class=insightleft><b>RRD Update Command</b></td>
	<td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{rrd_update}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	<td class=insight><TEXTAREA CLASS=insight cols=100 rows=$rrdupdatestring_rows NAME=rrdupdatestring>$rrdupdatestring</TEXTAREA></td></tr>";
    print "<tr><td class=insightleft><b>Custom RRDtool Graph Command</b></td>
	<td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{rrd_graph}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	<td class=insight><TEXTAREA CLASS=insight cols=100 rows=$graphcgi_rows NAME=graphcgi>$graphcgi</TEXTAREA></td></tr>";
    print "<tr><td class=insightleft><b>Enable</b></td>
	<td class=insightleft width='3%' valign=baseline align=center><a class=orange href='#doc' title=\"$docs{enable}\" tabindex='-1'>&nbsp;?&nbsp;</a></td>
	<td class=insight><INPUT CLASS=insight TYPE=checkbox NAME=enable VALUE=1 $checked{$enable||0}></td></tr>";
    show_test_command( $rrdname, $host, $service, $service_regx, $graphcgi, 1 );
    print "<tr><td class=insightbuttons colspan=3 align=center>
	    <INPUT class=orangebutton type=submit id=update name=update value='Update'>&nbsp;
	    <INPUT class=orangebutton type=button value='Cancel' onClick='changePage(\"$thisprogram?cmd=list\")'>&nbsp;
	    <INPUT class=orangebutton type=button value='Help' name=help onclick=\"open_window('$help_url')\">
	    </td></tr>";
    print "</table>";
    print "<br><br><br><br><br><br>";

    # FIX MINOR:  This extra space allows the pop-up help for the Test button to have room to appear, even at
    # very large browser font size, if you first scroll the screen all the way down.  Better would be to have
    # the pop-up help automatically adjust its position so it avoids overlapping the edges of the view.
    print "<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>" if $show_test_button;
}

# Subroutine to replace macros in the rrdtool graph command.
# It was stolen from process_service_perfdata_file, and slightly simplified;
# it should be kept up with functional changes to that script's routine,
# though the implementation here is now slightly different.
sub replace_graphing_macros {
    my $customgraph_command = shift;
    my $file                = shift;
    my $macros_ref          = shift;
    my @errors              = ();

    ## FIX LATER:  There is an excessive amount of color duplication here.  We could use additional visibly distinct colors.
    my @colors = (
	'#000000','#C05599','#E092E3','#6F76C4','#9BAEFF','#818181','#C0C0C0','#8BA016','#D3DB00','#64A2B8','#8DD9E0','#7E87B7','#F3B50F','#EB6232',
	'#000000','#C05599','#E092E3','#6F76C4','#9BAEFF','#818181','#C0C0C0','#8BA016','#D3DB00','#64A2B8','#8DD9E0','#7E87B7','#F3B50F','#EB6232',
	'#000000','#C05599','#E092E3','#6F76C4','#9BAEFF','#818181','#C0C0C0','#8BA016','#D3DB00','#64A2B8','#8DD9E0','#7E87B7','#F3B50F','#EB6232',
	'#000000','#C05599','#E092E3','#6F76C4','#9BAEFF','#818181','#C0C0C0','#8BA016','#D3DB00','#64A2B8','#8DD9E0','#7E87B7','#F3B50F','#EB6232',
	'#000000','#C05599','#E092E3','#6F76C4','#9BAEFF','#818181','#C0C0C0','#8BA016','#D3DB00','#64A2B8','#8DD9E0','#7E87B7','#F3B50F','#EB6232',
	'#000000','#C05599','#E092E3','#6F76C4','#9BAEFF','#818181','#C0C0C0','#8BA016','#D3DB00','#64A2B8','#8DD9E0','#7E87B7','#F3B50F','#EB6232',
	'#000000','#C05599','#E092E3','#6F76C4','#9BAEFF','#818181','#C0C0C0','#8BA016','#D3DB00','#64A2B8','#8DD9E0','#7E87B7','#F3B50F','#EB6232',
	'#000000','#C05599','#E092E3','#6F76C4','#9BAEFF','#818181','#C0C0C0','#8BA016','#D3DB00','#64A2B8','#8DD9E0','#7E87B7','#F3B50F','#EB6232',
	'#000000','#C05599','#E092E3','#6F76C4','#9BAEFF','#818181','#C0C0C0','#8BA016','#D3DB00','#64A2B8','#8DD9E0','#7E87B7','#F3B50F','#EB6232',
	'#000000','#C05599','#E092E3','#6F76C4','#9BAEFF','#818181','#C0C0C0','#8BA016','#D3DB00','#64A2B8','#8DD9E0','#7E87B7','#F3B50F','#EB6232',
	'#000000','#C05599','#E092E3','#6F76C4','#9BAEFF','#818181','#C0C0C0','#8BA016','#D3DB00','#64A2B8','#8DD9E0','#7E87B7','#F3B50F','#EB6232',
	'#000000','#C05599','#E092E3','#6F76C4','#9BAEFF','#818181','#C0C0C0','#8BA016','#D3DB00','#64A2B8','#8DD9E0','#7E87B7','#F3B50F','#EB6232',
	'#000000','#C05599','#E092E3','#6F76C4','#9BAEFF','#818181','#C0C0C0','#8BA016','#D3DB00','#64A2B8','#8DD9E0','#3366FF','#33CC00','#FF0033'
    );

    # Assemble graphing command for inserting into Foundation database.
    # We have to do an rrdtool info command to get the requisite data.
    # This construction depends on having a recently patched rrdtool
    # ( http://oss.oetiker.ch/rrdtool-trac/ticket/231 ) installed.

    require RRDs;

    my $info;
    my @info;
    my $ERR;
    if ($use_shared_rrd_module_for_info) {
	# $info is a hashref here.
	$info = RRDs::info($file);
	$ERR = RRDs::error();
	if ($ERR) {
	    push @errors, 'ERROR:  Failed RRD info command: ', $ERR;
	    return '', \@errors;
	}
    }
    elsif ( not @info = qx($rrdtool info $file 2>&1) ) {
	push @errors, 'ERROR:  Cannot execute raw rrdtool info command on file: ', $file;
	return '', \@errors;
    }
    ## We have to trap the DS names in a list ordered by DS sequence number for the custom graph command.
    my @ds_list = ();
    if ($use_shared_rrd_module_for_info) {
	foreach my $in (keys %$info) {
	    if ( $in =~ /^ds\[(\S+)\]\.index$/ ) {
		$ds_list[ $$info{$in} ] = $1;
	    }
	}
    }
    else {
	foreach my $in ( @info ) {
	    if ( $in =~ /^ds\[(\S+)\]\.index = (\d+)/ ) {
		$ds_list[$2] = $1;
	    }
	}
    }
    if ( not @ds_list ) {
	push @errors, 'ERROR:  Invalid RRD (no data sources) -- Try to repair or delete and allow system to re-create RRD: ', $file;
	return '', \@errors;
    }

    # replace the string rrd_source with the RRD file, making sure the filename always ends up safely quoted
    (my $sanitized_file = $file) =~ s/:/\\:/g;
    my $lead;
    my $trail;
    $customgraph_command =~ s/(?<=\s)(\S*)rrd_source(\S*)(?=\s)/
	($lead = $1, $trail = $2, $lead =~ m{"} && $trail =~ m{"})
	? "$lead$sanitized_file$trail" : "$lead\"$sanitized_file\"$trail"
    /eg;

    # get rid of those pesky backslashes and newlines, but preserve the whitespace (word separator) aspect of a newline ...
    $customgraph_command =~ s/\\\s//g;
    $customgraph_command =~ s/\n/ /g;
    $customgraph_command =~ s/\r//g;

    # and the single quotes that get in the way ...
    # FIX MAJOR:  This should probably be done here only if both leading and trailing quotes are present,
    # if at all in this routiine (since it should be done even before this routine is called).
    $customgraph_command =~ s/^'//;
    $customgraph_command =~ s/'$//;

    # Handle the List Cases for vname parameters.  This version supports a near-infinite number
    # of data sources, with backward compatibility to the original code that just used single
    # lowercase letters.  Of course, if you actually try to show them all as separate values in
    # the graph, that can create a crowded and confused picture, especially because we currently
    # have lots of color duplication in the @colors array above.  Perhaps a more practical
    # use for a large number of data sources is so they can be combined into fewer but more
    # meaningful metrics displayed in the graph.  The data-source naming sequence is "a", "b",
    # ..., "z", "aa", "ab", ..., "az", "ba", "bb", and so forth.
    #
    # Take care of listed DEFs, CDEFs, LINEs, AREAs, PRINTs, GPRINTs, and STACKs.
    # Note that this current logic only handles one $LISTSTART$ ... $LISTEND$ pair
    # in the command; we might want to generalize that in some future release.
    if ( $customgraph_command =~ /\$LISTSTART\$(.*?)\$LISTEND\$/ ) {
	my $tmpstring1 = $1 . ' ';
	my $tmpstring2 = '';
	for ( my ($j, $vname) = (0, 'a') ; $j < @ds_list ; ++$j, ++$vname) {
	    ## Handle the list case.  We default the color to magenta if we run
	    ## past the end of the @colors array, simply to avoid dying here.
	    my $color      = pop( @colors ) || '#FF00FF';
	    my $tmpstring3 = "$vname=$file:$ds_list[$j]";
	    my $tmpstring4 = $tmpstring1;
	    $tmpstring4 =~ s/\$DEFLABEL\#\$/$tmpstring3/g;
	    $tmpstring4 =~ s/\$CDEFLABEL\#\$/$vname/g;
	    $tmpstring4 =~ s/\$DSLABEL\#\$/$ds_list[$j]/g;
	    $tmpstring4 =~ s/\$COLORLABEL\#\$/$color/g;
	    $tmpstring2 .= $tmpstring4;
	}
	$customgraph_command =~ s/\$LISTSTART\$.*\$LISTEND\$/$tmpstring2/;
    }

    # Simple case of direct substitution of numbered ds_source_n
    # replace the string ds_source_(number) with the DS number we found
    for ( my $j = 0 ; $j < @ds_list ; $j++ ) {
	## Replace the string ds_source_(number) with the DS number we found.
	## Bounding punctuation is used to avoid confusion of ds_source_1 and
	## ds_source_10 strings.  (Alternatively, we could count down instead of
	## up, and effectively do this without forcing the bounding punctuation.)
	my $ds_name = 'ds_source_' . "$j";
	$customgraph_command =~ s/:$ds_name:/:$ds_list[$j]:/g;
    }

    # In case we need to show the service name or host name in a GPRINT,
    # substitute service for SERVICE and host for HOST
    foreach my $macro ( keys %$macros_ref ) {
	if ($macro =~ /SERVICE/) {
	    if (not ($macro =~ /SERVICETEXT/)) {
		$customgraph_command =~ s/SERVICE/$$macros_ref{$macro}/g;
	    }
	} elsif ($macro =~ /HOST/) {
	    $customgraph_command =~ s/HOST/$$macros_ref{$macro}/g;
	}
    }

    return $customgraph_command, \@errors;
}

sub printstyles {
    print qq(
<style type="text/css">

body.insight {
	background-color: #FFFFFF;
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
 background-color: #FFFFFF; /* GroundWork Portal Interface: Background */
 border: 0px solid #FFFFFF; /* GroundWork Portal Interface: Gray (Table Fill 1px Outlines) */
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
  font-size: 12px;
  font-style: normal;
  font-variant: normal;
  font-weight: bold;
  text-decoration: none;
  text-align: center;
  color: #FFFFFF; /* GroundWork Portal Interface: White */
  padding: 2;
  background-color: #444444; /* GroundWork Portal Interface: Table Fill #1 */
  border: 0px solid #444444; /* GroundWork Portal Interface: Gray (Table Fill 1px Outlines) */
  border-spacing: 0;
  height: 25px;
}
th.insightrow2 {
  font-family: verdana, helvetica, arial, sans-serif;
  font-size: 12px;
  font-style: normal;
  font-variant: normal;
  font-weight: bold;
  text-decoration: none;
  text-align: center;
  color: #FFFFFF; /* GroundWork Portal Interface: White */
  padding: 0;
  spacing: 0;
  background-color: #A0A0A0; /* GroundWork Portal Interface: Table Fill #1 */
  border: 0px solid #666666; /* GroundWork Portal Interface: Gray (Table Fill 1px Outlines) */
}

table.insightform {
background-color: #bfbfbf;
}

td.insightbuttons {
color: #000000;
font-family: verdana, helvetica, arial, sans-serif;
font-size: 12px;
vertical-align: top;
border: 0px solid #666666;
background-color: #FFFFFF;
padding: 0px 3px 2px;
}
td.insight {
color: #000000;
font-family: monospace, verdana, helvetica, arial, sans-serif;
font-size: 12px;
vertical-align: top;
border-width: 2px 0 0;
border-style: solid;
border-color: #FFFFFF;
background-color: #E6E6E6;
padding: 2px 0.5em;
white-space: pre-wrap;
width: 80%;
}
td.insightleft {
color: #000000;
font-family: verdana, helvetica, arial, sans-serif;
font-size: 12px;
vertical-align: top;
border-width: 2px 0 0;
border-style: solid;
border-color: #FFFFFF;
background-color: #CCCCCC;
text-align: right;
padding: 2px 0.5em;
white-space: nowrap;
}
td.insightcenter {
color: #000000;
font-family: verdana, helvetica, arial, sans-serif;
font-size: 12px;
vertical-align: top;
text-align: center;
}
tr.insight {
	color: #000000;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
}
tr.insightdkgray-bg td {
	background-color: #999;
	color: #fff;
	font-size: 12px;
}
tr.insightsublist td {
	color: #475181;
	font-size: 12px;
	padding-left: 12px !important;
}

tr.insightsublist-graybg td {
	background-color: #efefef;
	color: #475181;
	font-size: 12px;
	padding-left: 12px !important;
}

td.insighttitle {
color: #000000;
font-family: verdana, helvetica, arial, sans-serif;
font-size: 12px;
font-weight: bold;
}
td.insighthead {
color: #FFFFFF;
background-color: #777777;
font-family: verdana, helvetica, arial, sans-serif;
font-size: 12px;
font-weight: bold;
border-width: 2px 0 0;
border-style: solid;
border-color: #FFFFFF;
text-align: left;
padding: 0.5em 10px;
}
td.insightsubhead {
color: #000000;
background-color: #E6E6E6;
font-family: verdana, helvetica, arial, sans-serif;
font-size: 12px;
font-weight: normal;
/*
border-width: 2px 0 0;
*/
border-width: 0;
border-style: solid;
border-color: #FFFFFF;
text-align: left;
padding: 0.5em 10px;
}
td.insightbody {
color: #000000;
background-color: #E6E6E6;
font-family: verdana, helvetica, arial, sans-serif;
font-size: 12px;
font-weight: normal;
border-width: 2px 0 0;
border-style: solid;
border-color: #FFFFFF;
text-align: left;
padding: 0.5em 10px;
}
td.insightpreformatted {
color: #000000;
background-color: #E6E6E6;
font-family: verdana, helvetica, arial, sans-serif;
font-size: 12px;
font-weight: normal;
border-width: 2px 0 0;
border-style: solid;
border-color: #FFFFFF;
text-align: left;
padding: 0 10px;
}
td.insightselected {background-color: #898787; font-family: verdana, helvetica, arial, sans-serif; font-size: 12px; font-weight: bold; color: #ffffff;}
td.insightrow1 {background-color: #dcdcdc; font-family: verdana, helvetica, arial, sans-serif; font-size: 12px; font-weight: bold;}
td.insightrow2 {background-color: #bfbfbf; font-family: verdana, helvetica, arial, sans-serif; font-size: 12px; font-weight: bold;}
td.insightrow_lt {background-color: #f4f4f4; font-family: verdana, helvetica, arial, sans-serif; font-size: 12px; font-weight: bold;}
td.insightrow_dk {background-color: #e2e2e2; font-family: verdana, helvetica, arial, sans-serif; font-size: 12px; font-weight: bold;}
td.insighterror {background-color: #dcdcdc; font-family: verdana, helvetica, arial, sans-serif; font-size: 12px; font-weight: bold; color: #cc0000;}

span.error {
color: #CC0000;
}

#input, textarea, select {border: 0px solid #000099; font-family: verdana, helvetica, arial, sans-serif; font-size: 12px; font-weight: bold; background-color: #ffffff; color: #000000;}
input.insight, textarea.insight, select.insight {border: 0px solid #000099; font-family: monospace, verdana, helvetica, arial, sans-serif; font-size: 12px; font-weight: bold; color: #000000;}
input.insighttext {border: 0px solid #000099; font-family: verdana, helvetica, arial, sans-serif; font-size: 12px; font-weight: bold; color: #000000;}
input.insightradio {border: 0px; background-color: #dcdcdc;}
input.insightcheckbox {border: 0px; background-color: #dcdcdc;}

#input.button {
#border: 1px solid #000000;
#border-style: solid;
#border-top-width: auto;
#border-right-width: auto;
#border-bottom-width: auto;
#border-left-width: auto:
#font-family: verdana, helvetica, arial, sans-serif; font-size: 12px; font-weight: bold; background-color: #898787; color: #ffffff;
#}

input.insightbutton {
	font: normal 10px/normal verdana, helvetica, arial, sans-serif;
	text-transform: uppercase !important;
	border-color: #a0a6c6 #333 #333 #a0a6c6;
	border-width: 2px;
	border-style: solid;
	background: #666;
	color: #fff;
	padding: 0;
}

/* for orange buttons */
input.orangebutton {
padding: 1px 11px;
border: none;
border-radius: 2px;
font-family: verdana, helvetica, arial, sans-serif;
font-size: 12px;
font-weight: normal;
background-color: #FA840F;
color: #FFFFFF;
margin-top: 2px;
}
input.orangebutton:active {
padding: 1px 11px;
border: none;
border-radius: 2px;
font-family: verdana, helvetica, arial, sans-serif;
font-size: 12px;
font-weight: normal;
background-color: #FA840F;
color: #000000;
margin-top: 2px;
}

input.insightbox {border: 0px;}

a {
color: #0000FF;
text-decoration: none;
}

a.insighttop:link    {
color: #ffffff;
font-size: 12px;
font-family: verdana, helvetica, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}
a.insighttop:visited {
color: #ffffff;
font-size: 12px;
font-family: verdana, helvetica, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}
a.insighttop:hover   {
color: #ffffff;
font-size: 12px;
font-family: verdana, helvetica, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}
a.insighttop:active  {
color: #ffffff;
font-size: 12px;
font-family: verdana, helvetica, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}

a.insight:link    {
color: #414141;
font-size: 12px;
font-family: verdana, helvetica, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}
a.insight:visited {
color: #414141;
font-size: 12px;
font-family: verdana, helvetica, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}
a.insight:hover   {
color: #919191;
font-size: 12px;
font-family: verdana, helvetica, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}
a.insight:active  {
color: #919191;
font-size: 12px;
font-family: verdana, helvetica, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}

a.insightorange:link    {
color: #FA840F;
font-size: 12px;
font-family: verdana, helvetica, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}
a.insightorange:visited {
color: #FA840F;
font-size: 12px;
font-family: verdana, helvetica, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}
a.insightorange:hover   {
color: #FA840F;
font-size: 12px;
font-family: verdana, helvetica, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}
a.insightorange:active  {
color: #FA840F;
font-size: 12px;
font-family: verdana, helvetica, arial, sans-serif;
text-decoration: none;
font-weight: bold;
}

a.orange:link {
font-size: 12px;
font-family: verdana, helvetica, arial, sans-serif;
text-decoration: none;
/*
background-color: #F1B06F;
*/
background-color: #E2A160;
color: #FFFFFF;
font-weight: bold;
}

a.orange:visited {
font-size: 12px;
font-family: verdana, helvetica, arial, sans-serif;
text-decoration: none;
/*
background-color: #F1B06F;
*/
background-color: #E2A160;
color: #FFFFFF;
font-weight: bold;
}

a.orange:hover {
font-size: 12px;
font-family: verdana, helvetica, arial, sans-serif;
text-decoration: none;
/*
background-color: #FA840F;
*/
background-color: #EB7500;
color: #FFFFFF;
border-radius: 2px;
font-weight: bold;
}

a.orange:active {
font-size: 12px;
font-family: verdana, helvetica, arial, sans-serif;
text-decoration: none;
/*
background-color: #FA840F;
*/
background-color: #EB7500;
color: #FFFFFF;
font-weight: bold;
}

p.append {
margin: 0.5em 0px 0px;
}

/*Center paragraph*/
p.insight {
color: #000;
font-family: verdana, helvetica, arial, sans-serif;
font-size: 12px;
font-weight: normal;
}

h1.insight {
color: #FA840F;
font-family: verdana, helvetica, arial, sans-serif;
font-size: 12px;
font-weight: 600;
}

h2.insight {
color: #55609A;
font-family: verdana, helvetica, arial, sans-serif;
font-size: 12px;
font-weight: bold;
}

h3.insight {
color: #000;
font-family: verdana, helvetica, arial, sans-serif;
font-size: 12px;
font-weight: normal;
}

h4.insight {
color: #FFFFFF;
font-family: verdana, helvetica, arial, sans-serif;
font-size: 12px;
font-weight: bold;
}

h5.insight {
color: #000;
font-family: verdana, helvetica, arial, sans-serif;
font-size: 12px;
font-style: italic;
font-weight: normal;
}

h6.insight {
color: #000;
font-family: verdana, helvetica, arial, sans-serif;
font-size: 12px;
font-weight: bold;
}

div.nicetitle {
position: absolute;
padding: 4px;
top: 0px;
left: 0px;
color: white;
font-size: 12px;
font-family: verdana, helvetica, arial, sans-serif;
width: 25em;
font-weight: normal;
background: #808080;
}

div.nicetitle p.append {
margin: 0.5em 0px 0px;
}

div.nicetitle p {
margin: 0;
padding: 0 3px;
}

div.nicetitle p.destination {
font-size: 9px;
text-align: left;
padding-top: 3px;
}

</style>
);
}


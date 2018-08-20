#!/usr/local/groundwork/perl/bin/perl -w --
#
# Copyright 2008-2017 GroundWork Open Source, Inc. ("GroundWork").
# All rights reserved.
# http://www.groundworkopensource.com
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations
# under the License.

# FIX LATER:
# (*) Fix all FIX breakcrumbs.
# (*) Should we drop all the calls to perish() now that we check the error values from $sth->execute() ?
# (*) Make sure that a simple RETURN typed anywhere into the interface won't trigger any
#     dangerous actions (like accidental host deletion).
# (*) Why do we have certain duplicate host/cluster associations?  For instance,
#     the following query digs up 10 or so hosts in the old WGC database:
#
#         use ganglia;
#         select HostID, Name from host where HostID in
#             (select HostID from (select HostID, num from (select HostID, count(*)
#             as num from hostinstance group by HostID) as tt where num > 1) as ee);
#
#     or with the associated clusters displayed:
#
#         use ganglia;
#         select h.HostID, h.Name as HostName, c.ClusterID, c.Name as ClusterName
#         from host h, hostinstance hi, cluster c where h.HostID in
#             (select HostID from (select HostID, num from
#                 (select HostID, count(*) as num from hostinstance group by HostID)
#             as tt where num > 1) as ee)
#         and hi.HostID = h.HostID and c.ClusterID = hi.ClusterID;
#
# (*) Allow warning, critical, or duration thresholds to be left blank (undefined)
#     instead of forcing empty values in the UI to be interpreted as zero upon input.
# (*) Validation should check for hosts with host-level metric thresholds defined,
#     but with the Threshold Cluster either not set, or the Threshold Cluster defined
#     and not equal to the Actual Cluster.  But where are the Threshold Cluster and
#     Actual Cluster uniquely stored in the database?

use strict;
use warnings;

use Time::Local;
use DBI;
use TypedConfig;

# For the time being, this isn't printed anywhere; it's only here as a marker
# for support purposes, to identify what release a customer has installed.
my $VERSION = "7.0.0";

my $config_file = "/usr/local/groundwork/config/GangliaConfigAdmin.conf";
my $debug_config = 0;

# FIX MINOR:  All the reading of config info should be done inside an eval{}; statement,
# because it can throw exceptions.

my $config = TypedConfig->secure_new ($config_file, $debug_config);

# Global Debug Mode Flag;  No debug = 0, Normal debug=1, Detail debug=2 (GMOND XML and metric attribute parsing)
my $debug = $config->get_number ('debug_level');

# The max number of hosts to process in one DELETE statement.  (This just puts a limit on
# a single statement; repeated statements will still be used to process longer lists.)
my $max_delete_hosts = $config->get_number ('max_delete_hosts');

# Ganglia thresholds database connection parameters.
my $ganglia_dbtype = $config->get_scalar ('ganglia_dbtype');
my $ganglia_dbhost = $config->get_scalar ('ganglia_dbhost');
my $ganglia_dbname = $config->get_scalar ('ganglia_dbname');
my $ganglia_dbuser = $config->get_scalar ('ganglia_dbuser');
my $ganglia_dbpass = $config->get_scalar ('ganglia_dbpass');

my $is_postgresql = ( defined($ganglia_dbtype) && $ganglia_dbtype eq 'postgresql' );
# my $is_mysql = !$is_postgresql;

my $stylesheethtmlref="";
my $thisprogram = "GangliaConfigAdmin.cgi";
print "Content-type: text/html\n\n";

my $request_method = $ENV{'REQUEST_METHOD'};
my $form_info = '';
if ( defined($request_method) && $request_method eq "GET" ) {
    $form_info = $ENV{'QUERY_STRING'};
    ## $form_info =~ s/%([\dA-Fa-f][\dA-Fa-f])/pack("C",hex($1))/eg;
}
elsif ( defined($request_method) && $request_method eq "POST" ) {
    my $size_of_form_info = $ENV{'CONTENT_LENGTH'};
    read( STDIN, $form_info, $size_of_form_info );
}
else {
    print "500 Server Error. Server uses unsupported request method.\n";
    $ENV{'REQUEST_METHOD'} = "GET";
    $ENV{'QUERY_STRING'}   = defined( $ARGV[0] ) ? $ARGV[0] : '';
    $form_info             = defined( $ARGV[0] ) ? $ARGV[0] : '';
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

# The number of host columns in the search results in the Find/Delete Multiple Hosts screen.
my $hostname_columns = $FORM_DATA{columns} || 2;

my %doc = doc();
my $thisdoc = undef;
my $tooltip;
use HTML::Tooltip::Javascript;
my $tt = HTML::Tooltip::Javascript->new(
    # Relative url path to where wz_tooltip.js is
    javascript_dir => '/monarch/js',
    options        => {
	bgcolor     => '#FFE5CC',
	bordercolor => '#000000',
	titlecolor  => '#FFFFFF',
	borderwidth => '1',
	fontface    => 'verdana, helvetica, arial, sans-serif',
	fontcolor   => '#000000',
	default_tip => 'Tip not defined',
	delay       => 250,
	title       => 'Tooltip',
    },
);
my %options = (
    fontsize => '12px',
    padding  => '10',
    width    => '500',
    offsetx  => '-250',
    offsety  => '30',
    sticky   => 'true',
    clickclose => 'true'
);

# The wz_tooltip.js code does not provide an option for vertical padding in the title bar,
# so we provide a routine to uniformly insert such padding.  The quoting here is tricky
# because we need to bypass certain levels of quote interpretation inside Javascript to
# get the <style> element correctly defined without prior interference.
sub pad_tooltip_title {
    my $title = shift;
    return "<div style=&quot;padding-top: 3px; padding-bottom: 1px;&quot;>$title</div>";
}

print <<EOF;
    <HTML>
    <HEAD>
    <META HTTP-EQUIV='Expires' CONTENT='0'>
    <META HTTP-EQUIV='Pragma' CONTENT='no-cache'>
    <TITLE>Groundwork Ganglia Configuration Administration</TITLE>
EOF
# If $stylesheethtmlref is empty, then this href will load the current page again
# an extra time (as shown in the Apache access log) but try to interpret it as CSS
# (I suppose).  Whether or not that causes any hiccups in the browser, executing
# the user command twice on the server is not likely to be productive, so we block
# this <link> unless it's going to do something useful.
if ($stylesheethtmlref ne '') {
    print <<EOF;
	<link rel='stylesheet' type='text/css' href='$stylesheethtmlref'>
EOF
}
printstyles();

print <<EOF;
    <SCRIPT language="JavaScript">
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
	function showstatus (message, severity) {
	    var msgpar = document.getElementById('statusmsg');
	    if (severity == 'error') {
		msgpar.className = 'insighterror';
	    } else {
		msgpar.className = 'insight';
	    }
	    msgpar.innerHTML = message;
	}
	function isInteger (str) {
	    str = str.replace(/^\\s+|\\s+\$/g, '');
	    return ((str.length == 1 && str.match(/\\d/)) || (str.length > 1 && str.match(/^[-]?\\d+\$/)));
	}
	function trim (str) {
	    return str.replace(/^\\s+|\\s+\$/g, '');
	}
	function validateElement (type, name) {
	    if (name == '') {
		showstatus ('You must supply a ' + type + ' name before you can add the ' + type + '.', 'error');
		return false;
	    } else {
		return true;
	    }
	}
	function SelectAll (prefix, checkbox_state) {
	    for (var i = 0; i < document.selectForm.elements.length; i++) {
		if ((document.selectForm.elements[i].name.substr(0, prefix.length) == prefix) && (document.selectForm.elements[i].style.visibility != 'hidden')) {
		    document.selectForm.elements[i].checked = checkbox_state;
		}
	    }
	    showstatus ('', '');
	}
	function postselectedhosts (prefix) {
	    var hosts = [];
	    for (var i = 0; i < document.selectForm.elements.length; i++) {
		if ((document.selectForm.elements[i].name.substr(0, prefix.length) == prefix) &&
		    (document.selectForm.elements[i].style.visibility != 'hidden') &&
		    (document.selectForm.elements[i].checked)) {
		    hosts.push (document.selectForm.elements[i].value);
		}
	    }

	    if (hosts.length) {
		document.selectForm.cmd.value = 'deleteselectedhosts';
		document.selectForm.hoststodelete.value = hosts.join('~');
		// We force the use of POST because the list of hosts to delete could be long, and we don't want to exceed possible URL length limits.
		document.selectForm.method = 'post';
		document.selectForm.submit();
	    } else {
		showstatus ('No hosts are selected, so there is nothing to delete.', 'error');
	    }
	}
    </SCRIPT>
EOF

# onClick='SelectAll("check_",this.checked)'
# <td width='1%' align='right' bgcolor='#819bc0' style='padding: 4px; margin: 4px;'>
#     <input type='checkbox' style='margin: 0px;' name='all' title='Select All' onClick='SelectAll("check_",this.checked)'></td>
# <form name='hostlist' method='post' action=$thisprogram>
# <input type='checkbox' style='margin: 0px;' name='check' title="corpnet-router-1">

print <<EOF;
    </HEAD>
    <BODY class=insight>
    <DIV id=container>
    <DIV id=logo></DIV>
    <DIV id=pagetitle></DIV>
EOF

my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
$year += 1900;
my $month=qw(January February March April May June July August September October November December)[$mon];
my $timestring= sprintf "%02d:%02d:%02d",$hour,$min,$sec;
my $thisday = qw(Sunday Monday Tuesday Wednesday Thursday Friday Saturday)[$wday];

$options{'title'} = pad_tooltip_title "&nbsp;Task Selection&nbsp;";
$thisdoc = $doc{'taskselection'};
$tooltip = $tt->tooltip($thisdoc, \%options);

$options{'title'} = pad_tooltip_title "&nbsp;Warning Threshold&nbsp;";
$thisdoc = $doc{'warningthreshold'};
my $warning_tooltip = $tt->tooltip($thisdoc, \%options);

$options{'title'} = pad_tooltip_title "&nbsp;Critical Threshold&nbsp;";
$thisdoc = $doc{'criticalthreshold'};
my $critical_tooltip = $tt->tooltip($thisdoc, \%options);

$options{'title'} = pad_tooltip_title "&nbsp;Duration Threshold&nbsp;";
$thisdoc = $doc{'durationthreshold'};
my $duration_tooltip = $tt->tooltip($thisdoc, \%options);

$options{'title'} = pad_tooltip_title "&nbsp;Metric Description&nbsp;";
$thisdoc = $doc{'metricdescription'};
my $description_tooltip = $tt->tooltip($thisdoc, \%options);

print <<EOF;
<form id=selectForm name=selectForm class=formspace action=$thisprogram method=get>
<table class=insightcontrolpanel>
    <tbody>
	<tr class=insightgray-bg>
	    <!--
	    <th class=insight colspan=2>$thisday, $month $mday, $year. $timestring</th>
	    -->
	    <th class=insight colspan=2><span $tooltip>Ganglia Configuration Task Selection</span></th>
	</tr>
    </tbody>
</table>
EOF

my $dsn = '';
if ( defined($ganglia_dbtype) && $ganglia_dbtype eq 'postgresql' ) {
    $dsn = "DBI:Pg:dbname=$ganglia_dbname;host=$ganglia_dbhost";
}
else {
    $dsn = "DBI:mysql:database=$ganglia_dbname;host=$ganglia_dbhost";
}
my $dbh = DBI->connect( $dsn, $ganglia_dbuser, $ganglia_dbpass, { 'AutoCommit' => 1 } );
if (!$dbh) {
    perish ("ERROR:  Cannot connect to database $ganglia_dbname: ".$DBI::errstr);
}

if ( !$FORM_DATA{cmd} or $FORM_DATA{cmd} eq "home" ) {
    home();
} elsif ($FORM_DATA{cmd} eq "addcluster" ) {
    home();
    addcluster();
} elsif ($FORM_DATA{cmd} eq "insertcluster" ) {
    home(insertcluster());
} elsif ($FORM_DATA{cmd} eq "modcluster" ) {
    home();
    modcluster();
} elsif ($FORM_DATA{cmd} eq "addmetrictocluster" ) {
    home(addmetrictocluster());
    modcluster();
} elsif ($FORM_DATA{cmd} eq "updatecluster" ) {
    home(updatecluster());
    modcluster();
} elsif ($FORM_DATA{cmd} eq "deletecluster" ) {
    home(deletecluster());
} elsif ($FORM_DATA{cmd} eq "addhost" ) {
    home();
    addhost();
} elsif ($FORM_DATA{cmd} eq "inserthost" ) {
    home(inserthost());
} elsif ($FORM_DATA{cmd} eq "modhost" ) {
    home();
    modhost();
} elsif ($FORM_DATA{cmd} eq "addmetrictohost" ) {
    home(addmetrictohost());
    modhost();
} elsif ($FORM_DATA{cmd} eq "updatehost" ) {
    home(updatehost());
    modhost();
} elsif ($FORM_DATA{cmd} eq "deletehost" ) {
    home(deletehost());
} elsif ($FORM_DATA{cmd} eq "filterhosts" ) {
    home();
    filterhosts();
} elsif ($FORM_DATA{cmd} eq "showfilteredhosts" ) {
    home();
    filterhosts();
    showfilteredhosts();
} elsif ($FORM_DATA{cmd} eq "deleteselectedhosts" ) {
    home(deleteselectedhosts());
    filterhosts();
    showfilteredhosts();
} elsif ($FORM_DATA{cmd} eq "validateconfiguration" ) {
    home();
    validateconfiguration();
} elsif ($FORM_DATA{cmd} eq "addmetric" ) {
    home();
    addmetric();
} elsif ($FORM_DATA{cmd} eq "insertmetric" ) {
    home(insertmetric());
} elsif ($FORM_DATA{cmd} eq "modmetric" ) {
    home();
    modmetric();
} elsif ($FORM_DATA{cmd} eq "updatemetric" ) {
    home(updatemetric());
    modmetric();
} elsif ($FORM_DATA{cmd} eq "deletemetric" ) {
    home(deletemetric());
} elsif ($FORM_DATA{cmd} eq "showhelp" ) {
    home();
    showhelp();
}
print "</form>";
$dbh->disconnect();
print $tt->at_end;
print '
    </DIV>
    </BODY>
    </HTML>
';
exit;

# Perish not only dies, but may give the end-user some idea of what happened.
# What shows up on the user screen is partly dependent on the state of the HTML
# being generated, so it might not be complete and actual delivery of the message
# is not guaranteed, but at least there's a chance ...
sub perish {
    my $message = shift;
    chomp $message;
    $message .= "\n";
    print $message;
    die $message;
}

#
#	List all entries
#
sub home {
    my $message = shift;
    my $outbuf = '';

    print <<EOF;
<table class=insightcontrolpanel>
    <tr>
	<th class=insightrow2borderright><b>Element</b></th>
	<th class=insightrow2borderright><b>Add</b></th>
	<th class=insightrow2borderright><b>View / Modify / Delete</b></th>
	<th class=insightrow2borderright><b>Bulk Administration</b></th>
	<th class=insightborderrightbegin>&nbsp;</th>
	<th class=insightrow2><b>General</b></th>
    </tr>
    <tr>
	<td class=insight><b>Cluster</b></td>
	<td class=insight><input class=orangebutton type=button value='Add Cluster' onClick='changePage(\"$thisprogram?cmd=addcluster\")'></td>
EOF

    my %checked = ();
    $checked{ defined( $FORM_DATA{clusterid} ) ? $FORM_DATA{clusterid} : '' }="SELECTED";
    my $query = "SELECT ClusterID as \"ClusterID\", Name as \"Name\" FROM cluster ORDER BY \"Name\"";
    my $sth = $dbh->prepare($query);
    $sth->execute() or perish ($sth->errstr);
    my $SELECTED = $checked{''} || '';
    print <<EOF;
	<td class=insight>
	    <select name=clusterid class=insight onChange=changePage('$thisprogram?cmd=modcluster&clusterid='+this.options[this.selectedIndex].value)>
		<option class=insight $SELECTED value=''>-- Select Cluster --</option>
EOF
    while (my $row=$sth->fetchrow_hashref()) {
	my $id = $$row{ClusterID};
	$SELECTED = $checked{$id} || '';
	print "<option class=insight $SELECTED value='$id'>".$$row{Name}."</option>\n";
    }
    if (defined($sth->err)) {
	$outbuf .= '<p class=insighterror align=center>Database problem while fetching cluster names.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
    }
    $sth->finish();
    print <<EOF;
	    </select>
	</td>
	<td class=insightborderright><input class=orangebutton type=button value='View All Clusters' onClick='changePage(\"$thisprogram?cmd=viewclusters\")' disabled></td>
	<td class=insightheadborderright>&nbsp;</td>
	<td class=insight><input class=orangebutton type=button value='Help' onClick='changePage(\"$thisprogram?cmd=showhelp\")'></td>
    </tr>
    <tr>
	<td class=insight><b>Host</b></td>
	<td class=insight><input class=orangebutton type=button value='Add Host' onClick='changePage(\"$thisprogram?cmd=addhost\")'></td>
EOF

    %checked = ();
    $checked{ defined( $FORM_DATA{hostid} ) ? $FORM_DATA{hostid} : '' }="SELECTED";
    $query = "SELECT HostID as \"HostID\", Name as \"Name\" FROM host ORDER BY \"Name\"";
    $sth = $dbh->prepare($query);
    $sth->execute() or perish ($sth->errstr);
    $SELECTED = $checked{''} || '';
    print <<EOF;
	<td class=insight>
	    <select name=hostid class=insight onChange=changePage('$thisprogram?cmd=modhost&hostid='+this.options[this.selectedIndex].value)>
		<option class=insight $SELECTED value=''>-- Select Host --</option>
EOF
    while (my $row=$sth->fetchrow_hashref()) {
	if ($$row{Name} eq "Default") { next }		# Don't let the user select the Default host.
	my $id = $$row{HostID};
	$SELECTED = $checked{$id} || '';
	print "<option class=insight $SELECTED value='$id'>".$$row{Name}."</option>\n";
    }
    if (defined($sth->err)) {
	$outbuf .= '<p class=insighterror align=center>Database problem while fetching host names.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
    }
    $sth->finish();
    print <<EOF;
	    </select>
	</td>
	<td class=insightborderright><input class=orangebutton type=button value='Find/Delete Multiple Hosts' onClick='changePage(\"$thisprogram?cmd=filterhosts\")'></td>
	<td class=insightheadborderright>&nbsp;</td>
	<td class=insight><input class=orangebutton type=button value='Validate Configuration' onClick='changePage(\"$thisprogram?cmd=validateconfiguration\")'></td>
    </tr>
    <tr>
	<td class=insight><b>Metric</b></td>
	<td class=insight><input class=orangebutton type=button value='Add Metric' onClick='changePage(\"$thisprogram?cmd=addmetric\")'></td>
EOF

    %checked = ();
    $checked{ defined( $FORM_DATA{metricid} ) ? $FORM_DATA{metricid} : '' }="SELECTED";
    $query = "SELECT MetricID as \"MetricID\", Name as \"Name\" FROM metric ORDER BY \"Name\"";
    $sth = $dbh->prepare($query);
    $sth->execute() or perish ($sth->errstr);
    $SELECTED = $checked{''} || '';
    print <<EOF;
	<td class=insight>
	    <select name=metricid class=insight onChange=changePage('$thisprogram?cmd=modmetric&metricid='+this.options[this.selectedIndex].value)>
		<option class=insight $SELECTED value=''>-- Select Metric --</option>
EOF
    while (my $row=$sth->fetchrow_hashref()) {
	my $id = $$row{MetricID};
	$SELECTED = $checked{$id} || '';
	print "<option class=insight $SELECTED value='$id'>".$$row{Name}."</option>\n";
    }
    if (defined($sth->err)) {
	$outbuf .= '<p class=insighterror align=center>Database problem while fetching metric names.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
    }
    $sth->finish();
    print <<EOF;
	    </select>
	</td>
	<td class=insightborderright><input class=orangebutton type=button value='View All Metrics' onClick='changePage(\"$thisprogram?cmd=viewmetrics\")' disabled></td>
	<td class=insightheadborderrightend>&nbsp;</td>
	<td class=insight>
	    <input class=orangebutton type=button value='Back Up DB' onClick='changePage(\"$thisprogram?cmd=backupdatabase\")' disabled>
	    &nbsp;
	    <input class=orangebutton type=button value='Restore DB' onClick='changePage(\"$thisprogram?cmd=restoredatabase\")' disabled>
	</td>
    </tr>
</table>
<br>
EOF

    if (defined($message) && $message ne '') {
        print $message;
    }
    if ($outbuf ne '') {
        print $outbuf;
    }
    return;
}

sub addcluster {
    print <<EOF;
    <br>
    <table class=insightcontrolpanel>
	<tr class=insightgray-bg><th class=insight colspan=2>Add Cluster</th></tr>
    </table>
    <input type=hidden name=cmd value=insertcluster>
    <table class=insightcontrolpanel>
	<tr><td class=insight><b>Cluster Name</b></td><td class=insight><input class=insight size=60 maxlength=255 TYPE=TEXT NAME=Name VALUE=\"\"></td></tr>
	<tr><td class=insight><b>Description</b></td><td class=insight><TEXTAREA class=insight ROWS=5 COLS=80 NAME=Description></TEXTAREA></td></tr>
	<!--
	<tr><td class=insight><b>Regular Expression</b></td><td class=insight><input class=insight TYPE=checkbox NAME=service_regx VALUE=1></td></tr>
	-->
	<tr>
	    <td class=insightbuttons colspan=2 align=center>
		<input class=orangebuttonrow type=submit value='Add'>
		&nbsp;
		<input class=orangebuttonrow type=button value='Cancel' onClick='changePage(\"$thisprogram?cmd=home\")'>
	    </td>
	</tr>
    </table>
    <p class="insight" align="center" id=statusmsg></p>
    <br>
    <script type="text/javascript">
	function validatecluster () {
	    this.Name.value = trim(this.Name.value);
	    return validateElement ('cluster', this.Name.value);
	}
	document.selectForm.onsubmit = validatecluster;
	document.selectForm.Name.focus();
    </script>
EOF
    return;
}

sub insertcluster {
    my $outbuf = '';
    my $query = "SELECT ClusterID as \"ClusterID\" FROM cluster WHERE Name='$FORM_DATA{Name}'";
    my $sth = $dbh->prepare($query);
    $sth->execute() or perish ($sth->errstr);
    my $id = undef;
    while (my $row=$sth->fetchrow_hashref()) {
	$id = $$row{ClusterID};
    }
    if (defined($sth->err)) {
	$outbuf .= '<p class=insighterror align=center>Database problem while fetching cluster ID.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
    }
    $sth->finish();
    if ($id) {
	$outbuf .= "<p class=insighterror align=center>ERROR:&nbsp; A cluster named \"$FORM_DATA{Name}\" already exists.";
	$outbuf .= "<br>Duplicate entries are not permitted.&nbsp; Delete the existing entry before adding this entry.</p>";
    }
    if ($outbuf ne '') {
        return $outbuf;
    }
    $query = "INSERT INTO cluster (Name, Description) VALUES ( '$FORM_DATA{Name}', '$FORM_DATA{Description}' );";
    print "<br>Query=$query<br>" if $debug;
    if (! $dbh->do($query)) {
	$outbuf .= '<p class=insighterror align=center>Database problem while inserting cluster definition.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
    } else {
	$outbuf .= "<p class=insight align=center>Cluster \"$FORM_DATA{Name}\" has been added.</p>";
    }
    return $outbuf;
}

sub modcluster {
    return if ($FORM_DATA{clusterid} eq '');
    my $outbuf = '';
    my $query = "SELECT ClusterID as \"ClusterID\", Name as \"Name\", Description as \"Description\" FROM cluster WHERE ClusterID=$FORM_DATA{clusterid}";
    print "<br>Query=$query<br>" if $debug;
    my $sth = $dbh->prepare($query);
    $sth->execute() or perish ($sth->errstr);
    my $name = '';
    while (my $row=$sth->fetchrow_hashref()) {
	my $id = $$row{ClusterID};
	$name = $$row{Name};
	my $description = $$row{Description};
#	print "<input type=hidden name=clusterid value=$FORM_DATA{clusterid}>";
	print <<EOF;
    <input type=hidden name=cmd value='updatecluster'>
    <br>
    <table class=insightcontrolpanel>
	<tr class=insightgray-bg><th class=insight colspan=2>Settings for Cluster ID $id, Name $name</th></tr>
    </table>
    <table class=insightcontrolpanel>
	<tr>
	    <td class=insight><b>Name</b></td>
	    <td class=insight><input class=insight size=60 maxlength=100 TYPE=TEXT NAME=modClusterName VALUE=\"$name\"></td>
	</tr>
	<tr>
	    <td class=insight><b>Description</b></td>
	    <td class=insight><input class=insight size=60 maxlength=100 TYPE=TEXT NAME=modClusterDescription VALUE=\"$description\"></td>
	</tr>
	<tr>
	    <td class=insightbuttons colspan=2 align=center>
		<input class=orangebuttonrow type=submit value='Update Cluster'>
		&nbsp;
		<input class=orangebuttonrow type=button value='Delete Cluster' onClick='changePage(\"$thisprogram?cmd=deletecluster&clusterid=$FORM_DATA{clusterid}\")'>
		&nbsp;
		<input class=orangebuttonrow type=button value='Cancel' onClick='changePage(\"$thisprogram?cmd=home\")'>
	    </td>
	</tr>
    </table>
EOF
    }
    if (defined($sth->err)) {
	$outbuf .= '<p class=insighterror align=center>Database problem while fetching cluster definition.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
    }
    $sth->finish();
    if ($outbuf ne '') {
        print $outbuf;
        return;
    }

    print <<EOF;
    <br>
    <table class=insightcontrolpanel>
EOF
    if ($name eq 'Default') {
	print "<tr><th class=insight colspan=6>Default Metric Thresholds for All Hosts in All Clusters</th></tr>\n";
    }
    else {
	print "<tr><th class=insight colspan=6>Default Metric Thresholds for Hosts in the \"$name\" Cluster</th></tr>\n";
    }

    my %showmetrics = ();	# hash used in metric dropdown list
    $query = "SELECT
	    mv.MetricValueID as \"MetricValueID\",
	    h.Name           as \"HostName\",
	    m.Name           as \"MetricName\",
	    mv.Description   as \"Description\",
	    mv.Critical      as \"Critical\",
	    mv.Warning       as \"Warning\",
	    mv.Duration      as \"Duration\"
	FROM host as h, metricvalue as mv, metric as m
	WHERE
	    h.Name='Default'
	and mv.HostID=h.HostID
	and mv.ClusterID=$FORM_DATA{clusterid}
	and m.MetricID=mv.MetricID
	ORDER BY \"MetricName\"";
    print "<br>Query=$query<br>" if $debug;
    $sth = $dbh->prepare($query);
    $sth->execute() or perish ($sth->errstr);

    my @cluster_rows = ();
    while (my $row=$sth->fetchrow_hashref()) {
	my $metricname  = $$row{MetricName};
	$showmetrics{$metricname} = 1;
	my $id          = $$row{MetricValueID};
#	my $hostname    = $$row{HostName};
	my $description = $$row{Description};
	my $warning  = format_number($$row{Warning});
	my $critical = format_number($$row{Critical});
	my $duration = format_number($$row{Duration});
	# <td class=insight><input class=insight size=30 maxlength=100 TYPE=TEXT NAME=Name VALUE=\"$hostname\"></td>
	push @cluster_rows, <<EOF;
	<tr>
	    <td class=insight align=center><input class=insight TYPE=checkbox NAME=metricdeleteid VALUE=$id></td>
	    <input type=hidden name=metricvalueid value=$id>
	    <td class=insight>$metricname</td>
	    <td class=insight><input class=insight size=66 maxlength=100 TYPE=TEXT NAME=modMVDescription$id VALUE=\"$description\"></td>
	    <td class=insight><input class=insight size=9 maxlength=50 TYPE=TEXT NAME=modMVWarning$id VALUE=\"$warning\"></td>
	    <td class=insight><input class=insight size=9 maxlength=50 TYPE=TEXT NAME=modMVCritical$id VALUE=\"$critical\"></td>
	    <td class=insight><input class=insight size=9 maxlength=50 TYPE=TEXT NAME=modMVDuration$id VALUE=\"$duration\"></td>
	</tr>
EOF
    }
    if (defined($sth->err)) {
	$outbuf .= '<p class=insighterror align=center>Database problem while fetching associated metric values.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
    }
    $sth->finish();

    if ($name ne 'Default') {
	$query = "SELECT
		c.ClusterID      as \"ClusterID\",
		mv.MetricValueID as \"MetricValueID\",
		h.Name           as \"HostName\",
		m.Name           as \"MetricName\",
		mv.Description   as \"Description\",
		mv.Critical      as \"Critical\",
		mv.Warning       as \"Warning\",
		mv.Duration      as \"Duration\"
	    FROM host as h, cluster as c, metricvalue as mv, metric as m
	    WHERE
		h.Name='Default'
	    and c.Name='Default'
	    and mv.HostID=h.HostID
	    and mv.ClusterID=c.ClusterID
	    and m.MetricID=mv.MetricID
	    ORDER BY \"MetricName\"";
	print "<br>Query=$query<br>" if $debug;
	$sth = $dbh->prepare($query);
	$sth->execute() or perish ($sth->errstr);

	my @default_rows = ();
	while (my $row=$sth->fetchrow_hashref()) {
	    my $metricname = $$row{MetricName};
	    if ( not $showmetrics{$metricname} ) {
		my $cluster     = $$row{ClusterID};
		my $id          = $$row{MetricValueID};
#               my $hostname    = $$row{HostName};
		my $description = $$row{Description};
		my $critical = format_number($$row{Critical});
		my $duration = format_number($$row{Duration});
		my $warning  = format_number($$row{Warning});
		# <td class=insight><input class=insight size=30 maxlength=100 TYPE=TEXT NAME=Name VALUE=\"$hostname\"></td>
		push @default_rows, <<EOF;
		<tr>
		    <td class=insight><a href=\"$thisprogram?cmd=modcluster&clusterid=$cluster\">Default</a></td>
		    <td class=insight>$metricname</td>
		    <td class=insight><input class=insight_disabled size=66 maxlength=100 TYPE=TEXT NAME=modMVDescription$id VALUE=\"$description\" disabled></td>
		    <td class=insight><input class=insight_disabled size=9 maxlength=50 TYPE=TEXT NAME=modMVWarning$id VALUE=\"$warning\" disabled></td>
		    <td class=insight><input class=insight_disabled size=9 maxlength=50 TYPE=TEXT NAME=modMVCritical$id VALUE=\"$critical\" disabled></td>
		    <td class=insight><input class=insight_disabled size=9 maxlength=50 TYPE=TEXT NAME=modMVDuration$id VALUE=\"$duration\" disabled></td>
		</tr>
EOF
	    }
	}

	if (defined($sth->err)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while fetching associated metric values.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
	}
	$sth->finish();

	if (@default_rows) {
	    print <<EOF;
	<tr class=insightgray-bg>
	    <th class=insightrow2borderright>Source</th>
	    <th class=insightrow2borderright>Metric Name</th>
	    <th class=insightrow2borderright><span $description_tooltip>Metric Description</span></th>
	    <th class=insightrow2borderright><span $warning_tooltip>Warning Threshold</span></th>
	    <th class=insightrow2borderright><span $critical_tooltip>Critical Threshold</span></th>
	    <th class=insightrow2><span $duration_tooltip>Duration Threshold</span></th>
	</tr>
EOF
	    print @default_rows;
	}
    }

    print <<EOF;
	<tr class=insightgray-bg>
	    <th class=insightrow2borderright>Delete</th>
	    <th class=insightrow2borderright>Metric Name</th>
	    <th class=insightrow2borderright><span $description_tooltip>Metric Description</span></th>
	    <th class=insightrow2borderright><span $warning_tooltip>Warning Threshold</span></th>
	    <th class=insightrow2borderright><span $critical_tooltip>Critical Threshold</span></th>
	    <th class=insightrow2><span $duration_tooltip>Duration Threshold</span></th>
	</tr>
EOF

    if (@cluster_rows) {
	print @cluster_rows;
    }

    print "<tr><td class=insight colspan=6>Add New Metric Value to this cluster. &nbsp;&nbsp;&nbsp;&nbsp;";
    $query = "SELECT MetricID as \"MetricID\", Name as \"Name\" FROM metric ORDER BY \"Name\"";
    print "<br>Query=$query<br>" if $debug;
    $sth = $dbh->prepare($query);
    $sth->execute() or perish ($sth->errstr);
    print "<select name=addmetricid class=insight onChange=changePage('$thisprogram?cmd=addmetrictocluster&clusterid=$FORM_DATA{clusterid}&addmetricid='+this.options[this.selectedIndex].value)>
	    <option class=insight value=''>-- Select Metric to Add --</option>";
    while (my $row=$sth->fetchrow_hashref()) {
	if (!$showmetrics{$$row{Name}}) {
	    print "<option class=insight value='$$row{MetricID}'>$$row{Name}</option>\n";
	}
    }
    if (defined($sth->err)) {
	$outbuf .= '<p class=insighterror align=center>Database problem while fetching metric names.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
    }
    $sth->finish();
    print <<EOF;
		</select>
	    </td>
	</tr>
	<tr>
	    <td class=insightbuttons colspan=6 align=center>
		<input class=orangebuttonrow type=submit value='Update Cluster Metric Values'>
		&nbsp;
		<input class=orangebuttonrow type=button value='Cancel' onClick='changePage(\"$thisprogram?cmd=home\")'>
	    </td>
	</tr>
    </table>
    <br>
EOF
    if ($outbuf ne '') {
        print $outbuf;
    }
    return;
}

sub addmetrictocluster {
    my $outbuf = '';
    my $query = "SELECT
	    mv.MetricID as \"MetricID\",
	    m.Name      as \"Name\"
	FROM host as h, metricvalue as mv, metric as m
	WHERE
	    h.Name='Default'
	and mv.HostID=h.HostID
	and mv.ClusterID=$FORM_DATA{clusterid}
	and mv.MetricID=$FORM_DATA{addmetricid}
	and m.MetricID=mv.MetricID";
    print "<br>Query=$query<br>" if $debug;
    my $sth = $dbh->prepare($query);
    $sth->execute() or perish ($sth->errstr);
    my $id = undef;
    my $name = undef;
    while (my $row=$sth->fetchrow_hashref()) {
	$id = $$row{MetricID};
	$name = $$row{Name};
    }
    if (defined($sth->err)) {
	$outbuf .= '<p class=insighterror align=center>Database problem while fetching metric name.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
    }
    $sth->finish();
    if ($id) {
	$outbuf .= "<p class=insighterror align=center>ERROR:&nbsp; A metric named \"$name\" already exists for this cluster.<br>Duplicate entries are not permitted.</p>";
    }
    if ($outbuf ne '') {
        return $outbuf;
    }
    $query = "INSERT INTO metricvalue (MetricValueID,ClusterID,HostID,MetricID,Description,Critical,Warning,Duration,LocationID) VALUES (default,".
		    "'$FORM_DATA{clusterid}',".
		    "(SELECT HostID from host where Name='Default'),".
		    "'$FORM_DATA{addmetricid}',".
		    "(SELECT Description from metric where MetricID=$FORM_DATA{addmetricid}),".
		    "(SELECT Critical from metric where MetricID=$FORM_DATA{addmetricid}),".
		    "(SELECT Warning from metric where MetricID=$FORM_DATA{addmetricid}),".
		    "(SELECT Duration from metric where MetricID=$FORM_DATA{addmetricid}),".
		    "(SELECT LocationID from location where Name='Default')".
		    ");";
    print "<br>Query=$query<br>" if $debug;
    if (! $dbh->do($query)) {
	$outbuf .= '<p class=insighterror align=center>Database problem while inserting metric values.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
	return $outbuf;
    }
    $query = "SELECT Name as \"Name\" from metric where MetricID=$FORM_DATA{addmetricid}";
    print "<br>Query=$query<br>" if $debug;
    $sth = $dbh->prepare($query);
    $sth->execute() or perish ($sth->errstr);
    while (my $row=$sth->fetchrow_hashref()) {
	$name = $$row{Name};
    }
    if (defined($sth->err)) {
	$outbuf .= '<p class=insighterror align=center>Database problem while fetching metric name.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
    } else {
	$outbuf .= "<p class=insight align=center>Metric \"$name\" has been added.</p>";
    }
    $sth->finish();
    return $outbuf;
}

#
#	From modify cluster page, user selects update
sub updatecluster {
    my $outbuf = '';
    # See if we need to update the cluster info
    if ($FORM_DATA{clusterid} and $FORM_DATA{modClusterName} and $FORM_DATA{modClusterDescription}) {
	my $query = "UPDATE cluster SET Name='$FORM_DATA{modClusterName}',Description='$FORM_DATA{modClusterDescription}' ".
		    "WHERE ClusterID=$FORM_DATA{clusterid}";
	print "<br>Query=$query<br>" if $debug;
	if (! $dbh->do($query)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while updating cluster definition.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
	    return $outbuf;
	}
	$outbuf .= "<p class=insight align=center>Cluster \"$FORM_DATA{modClusterName}\" has been updated.</p>";
    }
    # See if we need to delete any cluster metric values
    if ($FORM_DATA{metricdeleteid}) {
	my @mvids = split "\0", $FORM_DATA{metricdeleteid};
	my $tmpstring = "";
	foreach my $id (@mvids) {
	    if (defined($id)) {
		$tmpstring .= "$id,";
	    }
	}
	$tmpstring =~ s/,$//;	# delete trailing comma
	my $query = "DELETE from metricvalue WHERE MetricValueID IN ($tmpstring)";
	print "<br>Query=$query<br>" if $debug;
	if (! $dbh->do($query)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while deleting metric values.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
	    return $outbuf;
	}
	$outbuf .= "<p class=insight align=center>Cluster \"$FORM_DATA{modClusterName}\" metric values have been deleted.</p>";
    }
    # See if we need to update the cluster metric values
    if ($FORM_DATA{metricvalueid}) {
	my @mvids = split "\0", $FORM_DATA{metricvalueid};
	foreach my $id (@mvids) {
	    if (defined($id)) {
		my $query = "UPDATE metricvalue SET ".
				"Description='".$FORM_DATA{"modMVDescription$id"}."', ".
				"Warning='".$FORM_DATA{"modMVWarning$id"}."', ".
				"Critical='".$FORM_DATA{"modMVCritical$id"}."', ".
				"Duration='".$FORM_DATA{"modMVDuration$id"}."' ".
			    "WHERE MetricValueID=$id";
		print "<br>Query=$query<br>" if $debug;
		if (! $dbh->do($query)) {
		    $outbuf .= '<p class=insighterror align=center>Database problem while updating metric values.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
		    return $outbuf;
		}
	    }
	}
	$outbuf .= "<p class=insight align=center>Cluster \"$FORM_DATA{modClusterName}\" metric values have been updated.</p>";
    }
    return $outbuf;
}

#
#	From modify cluster page, user selects delete cluster
sub deletecluster {
    my $outbuf = '';

    # See if we need to delete any cluster metric values
    my $clustername=undef;
    if ($FORM_DATA{clusterid}) {

	# Grab the clustername now, before we end up deleting it.
	my $query = "SELECT Name as \"Name\" FROM cluster where ClusterID=$FORM_DATA{clusterid}";
	print "<br>Query=$query<br>" if $debug;
	my $sth = $dbh->prepare($query);
	$sth->execute() or perish ($sth->errstr);
	while (my $row=$sth->fetchrow_hashref()) {
	    $clustername = $$row{Name};
	}
	if (defined($sth->err)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while fetching cluster name.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
	}
	$sth->finish();
	if ($outbuf ne '') {
	    return $outbuf;
	}

	# Delete all metric values for this cluster.
	$query = "DELETE from metricvalue WHERE ClusterID=$FORM_DATA{clusterid}";
	print "<br>Query=$query<br>" if $debug;
	if (! $dbh->do($query)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while deleting metric values.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
	    return $outbuf;
	}

	# Delete all metric instance values that have hostinstances for hosts in this cluster.
	$query = "DELETE from metricinstance WHERE HostInstanceID IN (SELECT HostInstanceID from hostinstance WHERE ClusterID=$FORM_DATA{clusterid})";
	print "<br>Query=$query<br>" if $debug;
	if (! $dbh->do($query)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while deleting metric instances.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
	    return $outbuf;
	}

	# Delete all host instance values for this cluster.
	$query = "DELETE from hostinstance WHERE ClusterID=$FORM_DATA{clusterid}";
	print "<br>Query=$query<br>" if $debug;
	if (! $dbh->do($query)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while deleting host instances.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
	    return $outbuf;
	}

	# Delete all host references to this cluster. Host definitions are not deleted.
	$query = "DELETE from clusterhost WHERE ClusterID=$FORM_DATA{clusterid}";
	print "<br>Query=$query<br>" if $debug;
	if (! $dbh->do($query)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while deleting cluster/host references.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
	    return $outbuf;
	}

	# Delete this cluster.
	$query = "DELETE from cluster WHERE ClusterID=$FORM_DATA{clusterid}";
	print "<br>Query=$query<br>" if $debug;
	if (! $dbh->do($query)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while deleting cluster definition.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
	    return $outbuf;
	}

	$outbuf .= "<p class=insight align=center>Cluster \"$clustername\" has been deleted.</p>";
    } else {
	$outbuf .= "<p class=insighterror align=center>There is no specified cluster to delete.</p>";
    }

    return $outbuf;
}

sub addhost {
    my $outbuf = '';
    my %checked = ();
    my $query = "SELECT ClusterID as \"ClusterID\", Name as \"Name\" FROM cluster";
    my $sth = $dbh->prepare($query);
    $sth->execute() or perish ($sth->errstr);

    $options{'title'} = pad_tooltip_title "&nbsp;Threshold Cluster&nbsp;";
    $thisdoc = $doc{'thresholdcluster'};
    $tooltip = $tt->tooltip($thisdoc, \%options);

    my $SELECTED = $checked{''} || '';
    print <<EOF;
    <br>
    <table class=insightcontrolpanel>
	<tr class=insightgray-bg><th class=insight colspan=2>Add Host</th></tr>
    </table>
    <input type=hidden name=cmd value=inserthost>
    <table class=insightcontrolpanel>
	<tr><td class=insight><b>Host Name</b></td><td class=insight><input class=insight size=60 maxlength=255 TYPE=TEXT NAME=Name VALUE=\"\"></td></tr>
	<tr><td class=insight><b>IP Address</b></td><td class=insight><input class=insight size=16 maxlength=16 TYPE=TEXT NAME=IPAddress VALUE=\"\"></td></tr>
	<tr><td class=insight><b>Description</b></td><td class=insight><TEXTAREA class=insight ROWS=5 COLS=80 NAME=Description></TEXTAREA></td></tr>
	<!--
	<tr><td class=insight><b>Regular Expression</b></td><td class=insight><input class=insight TYPE=checkbox NAME=service_regx VALUE=1></td></tr>
	-->
	<tr>
	    <td class=insight><b><span $tooltip>Threshold Cluster</span></b></td>
	    <td class=insight>
		<select name=id class=insight>
		    <option class=insight $SELECTED value=''>-- none --</option>
EOF
    while (my $row=$sth->fetchrow_hashref()) {
	if ($$row{Name} eq "Default") { next }
	my $id = $$row{ClusterID};
	$SELECTED = $checked{$id} || '';
	print "<option class=insight value='$id' $SELECTED>".$$row{Name}."</option>\n";
    }
    if (defined($sth->err)) {
	$outbuf .= '<p class=insighterror align=center>Database problem while fetching cluster names.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
    }
    $sth->finish();
    print <<EOF;
		</select>
	    </td>
	</tr>
	<tr>
	    <td class=insightbuttons colspan=2 align=center>
		<input class=orangebuttonrow type=submit value='Add'>
		&nbsp;
		<input class=orangebuttonrow type=button value='Cancel' onClick='changePage(\"$thisprogram?cmd=home\")'>
	    </td>
	</tr>
    </table>
    <p class="insight" align="center" id=statusmsg></p>
    <br>
    <script type="text/javascript">
	function validatehost () {
	    this.Name.value = trim(this.Name.value);
	    return validateElement ('host', this.Name.value);
	}
	document.selectForm.onsubmit = validatehost;
	document.selectForm.Name.focus();
    </script>
EOF
    if ($outbuf ne '') {
        print $outbuf;
    }
    return;
}

sub inserthost {
    my $outbuf = '';
    my $query = "SELECT HostID as \"HostID\" FROM host WHERE Name='$FORM_DATA{Name}'";
    print "<br>Query=$query<br>" if $debug;
    my $sth = $dbh->prepare($query);
    $sth->execute() or perish ($sth->errstr);
    my $id = undef;
    while (my $row=$sth->fetchrow_hashref()) {
	$id = $$row{HostID};
    }
    if (defined($sth->err)) {
	$outbuf .= '<p class=insighterror align=center>Database problem while fetching host ID.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
    }
    $sth->finish();
    if ($id) {
	$outbuf .= "<p class=insighterror align=center>ERROR:&nbsp; A host named \"$FORM_DATA{Name}\" already exists.";
	$outbuf .= "<br>Duplicate entries are not permitted.&nbsp; Delete the existing entry before adding this entry.</p>";
    }
    if ($outbuf ne '') {
        return $outbuf;
    }
    $query = "INSERT INTO host (HostID,Name,IPAddress,Description) VALUES (default, '$FORM_DATA{Name}', '$FORM_DATA{IPAddress}', '$FORM_DATA{Description}');";
    $sth = $dbh->prepare($query);
    $sth->execute() or perish ($sth->errstr);
    if (defined($sth->err)) {
	$outbuf .= '<p class=insighterror align=center>Database problem while inserting host definition.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
    }
    $sth->finish();
    if ($outbuf ne '') {
        return $outbuf;
    }
    my $hostid = $is_postgresql ? $dbh->last_insert_id(undef,undef,undef,undef,{sequence=>'host_hostid_seq'}) : $dbh->{'mysql_insertid'};
    print "<br>Query=$query<br>" if $debug;
    print "<br>Hostid=$hostid<br>" if $debug;
    if ($FORM_DATA{id} ne '') {
	my $query = "INSERT INTO clusterhost (ClusterID,HostID) VALUES ( '$FORM_DATA{id}', '$hostid' );";
	print "<br>Query=$query<br>" if $debug;
	if (! $dbh->do($query)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while inserting cluster/host reference.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
	    return $outbuf;
	}
    }

    $outbuf .= "<p class=insight align=center>Host \"$FORM_DATA{Name}\" has been added.</p>";
    return $outbuf;
}

sub modhost {
    my $outbuf = '';
    if (!$FORM_DATA{hostid}) { return }
    my $query = "SELECT HostID as \"HostID\", Name as \"Name\", IPAddress as \"IPAddress\", Description as \"Description\" FROM host WHERE HostID=$FORM_DATA{hostid}";
    print "<br>Query=$query<br>" if $debug;
    my $sth = $dbh->prepare($query);
    $sth->execute() or perish ($sth->errstr);
    my %checked = ();
    $checked{1} = "CHECKED";
    my $name = '';
    while (my $row=$sth->fetchrow_hashref()) {
	my $id          = $$row{HostID};
	$name           = $$row{Name};
	my $ipaddress   = $$row{IPAddress};
	my $description = $$row{Description};
	my $desc_rows = 1 + int(length($description) / 80) + ($description =~ tr/\n/\n/);
#	print "<input type=hidden name=id value=$id>";
	print <<EOF;
    <input type=hidden name=cmd value='updatehost'>
    <br>
    <table class=insightcontrolpanel>
	<TBODY>
	    <tr class=insightgray-bg>
		<th class=insight colspan=2>Settings for Host ID $id, Name $name</th>
	    </tr>
	</TBODY>
    </table>
    <table class=insightcontrolpanel>
	<tr>
	    <td class=insight><b>Host Name</b></td>
	    <td class=insight><input class=insight size=80 maxlength=100 TYPE=TEXT NAME=modHostName VALUE=\"$name\"></td>
	</tr>
	<tr>
	    <td class=insight><b>IP Address</b></td>
	    <td class=insight><input class=insight size=16 maxlength=16 TYPE=TEXT NAME=modHostIPAddress VALUE=\"$ipaddress\"></td>
	</tr>
	<tr>
	    <td class=insight><b>Description</b></td>
	    <td class=insight><TEXTAREA class=insight ROWS=$desc_rows COLS=80 NAME=modHostDescription>$description</TEXTAREA></td>
	</tr>
EOF
#	print "<tr><td class=insight><b>Use Service as a Regular Expression</b></td>";
#	print "<td class=insight><input class=insight TYPE=checkbox NAME=service_regx VALUE=1 $checked{$service_regx}></td></tr>";
    }
    if (defined($sth->err)) {
	$outbuf .= '<p class=insighterror align=center>Database problem while fetching host definition.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
    }
    $sth->finish();
    %checked = ();
    $query = "SELECT ch.ClusterID as \"ClusterID\", c.Name as \"Name\" FROM clusterhost ch, cluster c WHERE ch.HostID=$FORM_DATA{hostid} AND c.ClusterID=ch.ClusterID";
    print "<br>Query=$query<br>" if $debug;
    $sth = $dbh->prepare($query);
    $sth->execute() or perish ($sth->errstr);
    my $thresholdclusterid   = '';
    my $thresholdclustername = '';
    while (my $row=$sth->fetchrow_hashref()) {
	$checked{$$row{ClusterID}}="SELECTED";
	$thresholdclusterid   = $$row{ClusterID};
	$thresholdclustername = $$row{Name};
    }
    if (defined($sth->err)) {
	$outbuf .= '<p class=insighterror align=center>Database problem while fetching cluster/host references.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
    }
    $sth->finish();
    $query = "SELECT Name as \"Name\", ClusterID as \"ClusterID\" FROM cluster";
    print "<br>Query=$query<br>" if $debug;
    $sth = $dbh->prepare($query);
    $sth->execute() or perish ($sth->errstr);
    $options{'title'} = pad_tooltip_title "&nbsp;Threshold Cluster&nbsp;";
    $thisdoc = $doc{'thresholdcluster'};
    $tooltip = $tt->tooltip($thisdoc, \%options);
    my $SELECTED = $checked{''} || '';
    print <<EOF;
	<tr>
	    <td class=insight><b><span $tooltip>Threshold Cluster</span></b></td>
	    <td class=insight>
		<select name=modclusterid class=insight>
		    <option class=insight $SELECTED value=''>-- none --</option>
EOF
    while (my $row=$sth->fetchrow_hashref()) {
	if ($$row{Name} eq "Default") { next }
	my $id = $$row{ClusterID};
	$SELECTED = $checked{$id} || '';
	print "<option class=insight value='$id' $SELECTED>".$$row{Name}."</option>\n";
    }
    if (defined($sth->err)) {
	$outbuf .= '<p class=insighterror align=center>Database problem while fetching cluster names.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
    }
    $sth->finish();
    print <<EOF;
		</select>
		&nbsp;<i>Must be set equal to the Actual Cluster before defining any host-specific thresholds below. See Help.</i>
	    </td>
	</tr>
EOF
    my $actualclusterid = undef;
    my $actualclustername = '';
    $query = "SELECT c.ClusterID as \"ClusterID\", c.Name as \"Name\" FROM cluster c, hostinstance hi where hi.HostID=$FORM_DATA{hostid} and c.ClusterID=hi.ClusterID";
    print "<br>Query=$query<br>" if $debug;
    $sth = $dbh->prepare($query);
    $sth->execute() or perish ($sth->errstr);
    while (my $row=$sth->fetchrow_hashref()) {
	if ($$row{Name} eq "Default") { next }
	$actualclusterid = $$row{ClusterID};
	$actualclustername = $$row{Name};
    }
    if (defined($sth->err)) {
	$outbuf .= '<p class=insighterror align=center>Database problem while fetching cluster name.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
    }
    $sth->finish();
    $options{'title'} = pad_tooltip_title "&nbsp;Actual Cluster&nbsp;";
    $thisdoc = $doc{'actualcluster'};
    $tooltip = $tt->tooltip($thisdoc, \%options);
    print <<EOF;
	<tr>
	    <td class=insight><b><span $tooltip>Actual Cluster</span></b></td>
	    <td class=insight><b>$actualclustername</b></td>
	</tr>
	<tr>
	    <td class=insightbuttons colspan=2 align=center>
		<input class=orangebuttonrow type=submit value='Update Host'>
		&nbsp;
		<input class=orangebuttonrow type=button value='Delete Host' onClick='changePage(\"$thisprogram?cmd=deletehost&hostid=$FORM_DATA{hostid}\")'>
		&nbsp;
		<input class=orangebuttonrow type=button value='Cancel' onClick='changePage(\"$thisprogram?cmd=home\")'>
	    </td>
	</tr>
    </table>
EOF
    if ($outbuf ne '') {
        return $outbuf;
    }
    $query = "SELECT
	    mv.MetricValueID as \"MetricValueID\",
	    m.Name           as \"MetricName\",
	    mv.Description   as \"Description\",
	    mv.Critical      as \"Critical\",
	    mv.Warning       as \"Warning\",
	    mv.Duration      as \"Duration\"
	FROM metricvalue as mv, metric as m
	WHERE
	    mv.HostID='$FORM_DATA{hostid}'
	and m.MetricID=mv.MetricID
	ORDER BY \"MetricName\"";
    print "<br>Query=$query<br>" if $debug;
    $sth = $dbh->prepare($query);
    $sth->execute() or perish ($sth->errstr);
    %checked = ();
    $checked{1} = "CHECKED";

    print <<EOF;
    <br>
    <table class=insightcontrolpanel>
    <tr><th class=insight colspan=6>Metric Thresholds That Apply to the \"$name\" Host</th></tr>
EOF

    my %showmetrics = ();	# hash used in metric dropdown list
    my @host_rows = ();
    while (my $row=$sth->fetchrow_hashref()) {
	my $metricname  = $$row{MetricName};
	$showmetrics{$metricname} = 1;
	my $id          = $$row{MetricValueID};
#	my $hostname    = $$row{HostName};
	my $description = $$row{Description};
	my $critical = format_number($$row{Critical});
	my $warning  = format_number($$row{Warning});
	my $duration = format_number($$row{Duration});
	# <td class=insight><input class=insight size=30 maxlength=100 TYPE=TEXT NAME=Name VALUE=\"$hostname\"></td>
	push @host_rows, <<EOF;
	<input type=hidden name=id value=$id>
	<tr>
	    <td class=insight align=center><input class=insight TYPE=checkbox NAME=metricdeleteid VALUE=$id></td>
	    <input type=hidden name=metricvalueid value=$id>
	    <td class=insight>$metricname</td>
	    <td class=insight><input class=insight size=66 maxlength=100 TYPE=TEXT NAME=modMVDescription$id VALUE=\"$description\"></td>
	    <td class=insight><input class=insight size=9 maxlength=50 TYPE=TEXT NAME=modMVWarning$id VALUE=\"$warning\"></td>
	    <td class=insight><input class=insight size=9 maxlength=50 TYPE=TEXT NAME=modMVCritical$id VALUE=\"$critical\"></td>
	    <td class=insight><input class=insight size=9 maxlength=50 TYPE=TEXT NAME=modMVDuration$id VALUE=\"$duration\"></td>
	</tr>
EOF
    }
    if (defined($sth->err)) {
	$outbuf .= '<p class=insighterror align=center>Database problem while fetching metric values.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
    }
    $sth->finish();

    my %clustermetrics = ();
    my @cluster_rows = ();
    my @default_rows = ();

    # FIX MAJOR NOW:  make sure our treatment of the threshold cluster is correct in all aspects here,
    # including how the check_ganglia.pl script will treat it

    # FIX MINOR NOW:  should we include the actual cluster in the table caption, as another way of reinforcing the connection between
    # actual cluster and threshold cluster and the necessity for setting the threshold cluster if you define per-host metrics?

    # FIX MINOR NOW:  validation checks should include:
    # (*) a list of all hosts that belong to more than one actual cluster, along with the names of those clusters
    # (*) a list of all hosts that belong to more than one threshold cluster, along with the names of those clusters
    # (*) a list of all hosts that have per-host metrics defined for more than one cluster, along with the names of those clusters and metrics
    # (*) a list of all hosts that have a non-empty threshold cluster which differs from the actual cluster for that host, if the actual cluster is defined

    # FIX MAJOR NOW:
    # (*) Later on, we should be marking the per-host metric thresholds as not effective, if the Threshold Cluster is not equal to the Actual Cluster.
    #     This might be done partly by making the background color of the host metric threshold settings our standard light-yellow color, but we also
    #     need an explicit warning message as well.

    # FIX MAJOR NOW:  we should be presenting a warning message to the user if the Threshold Cluster is not defined but some per-host thresholds are defined.
    # FIX MAJOR NOW:  we should be presenting an error message to the user if the Threshold Cluster is defined but does not match the Actual Cluster.

    # FIX MAJOR RIGHT NOW:  verify that check_ganglia uses the hostinstance table to grab cluster-level metric thresholds,
    # and that we also use the hostinstance table here to determine the Actual Cluster

    # FIX MAJOR NOW:  Figure out what happens to existing entries in the metricvalue table for a given host,
    # if the user changes the Threshold Cluster for that host.  Do all of those rows in the metricvalue table
    # have their ClusterID values changed?  Or are they left behind?  How do we reflect whatever effect actually
    # happens in the user interface, so the user is not misled about which host thresholds will and will not be
    # used by check_ganglia?

    # FIX MAJOR NOW:  This error message:
    # "Cluster is not defined.  A valid cluster must be defined for metrics to be valid."
    # is appearing when I delete a per-host metric threshold which was defined when the Threshold Cluster was undefined.
    # But the message did not appear when I tried to define the metric, which is the sensible time to do so.
    # (When you're deleting the metric threshold, it no longer matters.)

    # FIX MAJOR:  verify this statement
    # We search the Actual Cluster for applicable cluster-level thresholds instead of the Threshold Cluster, because it is the
    # Actual Cluster's metric thresholds that will be applied by check_ganglia, not the Threshold Cluster's metric thresholds.
    if (defined $actualclusterid) {
	$query = "SELECT
		mv.MetricValueID as \"MetricValueID\",
		h.Name           as \"HostName\",
		m.Name           as \"MetricName\",
		mv.Description   as \"Description\",
		mv.Critical      as \"Critical\",
		mv.Warning       as \"Warning\",
		mv.Duration      as \"Duration\"
	    FROM host as h, metricvalue as mv, metric as m
	    WHERE
		h.Name='Default'
	    and mv.HostID=h.HostID
	    and mv.ClusterID=$actualclusterid
	    and m.MetricID=mv.MetricID
	    ORDER BY \"MetricName\"";
	print "<br>Query=$query<br>" if $debug;
	$sth = $dbh->prepare($query);
	$sth->execute() or perish ($sth->errstr);

	while (my $row=$sth->fetchrow_hashref()) {
	    my $metricname = $$row{MetricName};
	    if ( not $showmetrics{$metricname} ) {
		my $id          = $$row{MetricValueID};
#               my $hostname    = $$row{HostName};
		$clustermetrics{$metricname} = 1;
		my $description = $$row{Description};
		my $warning  = format_number($$row{Warning});
		my $critical = format_number($$row{Critical});
		my $duration = format_number($$row{Duration});
		# <td class=insight><input class=insight size=30 maxlength=100 TYPE=TEXT NAME=Name VALUE=\"$hostname\"></td>
		push @cluster_rows, <<EOF;
		<tr>
		    <td class=insight><a href=\"$thisprogram?cmd=modcluster&clusterid=$actualclusterid\">$actualclustername</a></td>
		    <td class=insight>$metricname</td>
		    <td class=insight><input class=insight_disabled size=66 maxlength=100 TYPE=TEXT NAME=modMVDescription$id VALUE=\"$description\"></td>
		    <td class=insight><input class=insight_disabled size=9 maxlength=50 TYPE=TEXT NAME=modMVWarning$id VALUE=\"$warning\"></td>
		    <td class=insight><input class=insight_disabled size=9 maxlength=50 TYPE=TEXT NAME=modMVCritical$id VALUE=\"$critical\"></td>
		    <td class=insight><input class=insight_disabled size=9 maxlength=50 TYPE=TEXT NAME=modMVDuration$id VALUE=\"$duration\"></td>
		</tr>
EOF
	    }
	}
	if (defined($sth->err)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while fetching associated metric values.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
	}
	$sth->finish();
    }

    $query = "SELECT
	    c.ClusterID      as \"ClusterID\",
	    mv.MetricValueID as \"MetricValueID\",
	    h.Name           as \"HostName\",
	    m.Name           as \"MetricName\",
	    mv.Description   as \"Description\",
	    mv.Critical      as \"Critical\",
	    mv.Warning       as \"Warning\",
	    mv.Duration      as \"Duration\"
	FROM host as h, cluster as c, metricvalue as mv, metric as m
	WHERE
	    h.Name='Default'
	and c.Name='Default'
	and mv.HostID=h.HostID
	and mv.ClusterID=c.ClusterID
	and m.MetricID=mv.MetricID
	ORDER BY \"MetricName\"";
    print "<br>Query=$query<br>" if $debug;
    $sth = $dbh->prepare($query);
    $sth->execute() or perish ($sth->errstr);

    while (my $row=$sth->fetchrow_hashref()) {
	my $metricname = $$row{MetricName};
	if ( not $showmetrics{$metricname} and not $clustermetrics{$metricname} ) {
	    my $cluster     = $$row{ClusterID};
	    my $id          = $$row{MetricValueID};
#           my $hostname    = $$row{HostName};
	    my $description = $$row{Description};
	    my $warning  = format_number($$row{Warning});
	    my $critical = format_number($$row{Critical});
	    my $duration = format_number($$row{Duration});
	    # <td class=insight><input class=insight size=30 maxlength=100 TYPE=TEXT NAME=Name VALUE=\"$hostname\"></td>
	    push @default_rows, <<EOF;
	    <tr>
		<td class=insight><a href=\"$thisprogram?cmd=modcluster&clusterid=$cluster\">Default</a></td>
		<td class=insight>$metricname</td>
		<td class=insight><input class=insight_disabled size=66 maxlength=100 TYPE=TEXT NAME=modMVDescription$id VALUE=\"$description\" disabled></td>
		<td class=insight><input class=insight_disabled size=9 maxlength=50 TYPE=TEXT NAME=modMVWarning$id VALUE=\"$warning\" disabled></td>
		<td class=insight><input class=insight_disabled size=9 maxlength=50 TYPE=TEXT NAME=modMVCritical$id VALUE=\"$critical\" disabled></td>
		<td class=insight><input class=insight_disabled size=9 maxlength=50 TYPE=TEXT NAME=modMVDuration$id VALUE=\"$duration\" disabled></td>
	    </tr>
EOF
	}
    }

    if (defined($sth->err)) {
	$outbuf .= '<p class=insighterror align=center>Database problem while fetching associated metric values.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
    }
    $sth->finish();

    if (@default_rows || @cluster_rows) {
	print <<EOF;
	<tr class=insightgray-bg>
	    <th class=insightrow2borderright>Source</th>
	    <th class=insightrow2borderright>Metric Name</th>
	    <th class=insightrow2borderright><span $description_tooltip>Metric Description</span></th>
	    <th class=insightrow2borderright><span $warning_tooltip>Warning Threshold</span></th>
	    <th class=insightrow2borderright><span $critical_tooltip>Critical Threshold</span></th>
	    <th class=insightrow2><span $duration_tooltip>Duration Threshold</span></th>
	</tr>
EOF
	print @default_rows if @default_rows;
	print @cluster_rows if @cluster_rows;
    }

    print <<EOF;
	<tr class=insightgray-bg>
	    <th class=insightrow2borderright>Delete</th>
	    <th class=insightrow2borderright>Metric Name</th>
	    <th class=insightrow2borderright><span $description_tooltip>Metric Description</span></th>
	    <th class=insightrow2borderright><span $warning_tooltip>Warning Threshold</span></th>
	    <th class=insightrow2borderright><span $critical_tooltip>Critical Threshold</span></th>
	    <th class=insightrow2><span $duration_tooltip>Duration Threshold</span></th>
	</tr>
EOF

    if (@host_rows) {
	print @host_rows;
    }

    print "<tr><td class=insight colspan=6>Add New Metric Value to this host. &nbsp;&nbsp;&nbsp;&nbsp;";
    $query = "SELECT MetricID as \"MetricID\", Name as \"Name\" FROM metric ORDER BY \"Name\"";
    print "<br>Query=$query<br>" if $debug;
    $sth = $dbh->prepare($query);
    $sth->execute() or perish ($sth->errstr);

    # FIX MAJOR NOW:  understand exactly how $thresholdclusterid will be used by the called page --
    # how does it related to the distinction between Threshold Cluster and Actual Cluster?
    $SELECTED = $checked{''} || '';
    print "<select name=addmetricid class=insight onChange=changePage('$thisprogram?cmd=addmetrictohost&modclusterid=$thresholdclusterid&hostid=$FORM_DATA{hostid}&addmetricid='+this.options[this.selectedIndex].value)>
	    <option class=insight $SELECTED value=''>-- Select Metric to Add --</option>";
    while (my $row=$sth->fetchrow_hashref()) {
	if (!$showmetrics{$$row{Name}}) {
	    print "<option class=insight value='$$row{MetricID}'>$$row{Name}</option>\n";
	}
    }
    if (defined($sth->err)) {
	$outbuf .= '<p class=insighterror align=center>Database problem while fetching metric names.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
    }
    $sth->finish();
    print <<EOF;
		</select>
	    </td>
	</tr>
	<tr>
	    <td class=insightbuttons colspan=6 align=center>
		<input class=orangebuttonrow type=submit value='Update Host Metric Values'>
		&nbsp;
		<input class=orangebuttonrow type=button value='Cancel' onClick='changePage(\"$thisprogram?cmd=home\")'>
	    </td>
	</tr>
    </table>
    <br>
EOF
    if ($outbuf ne '') {
        print $outbuf;
    }
    return;
}

sub addmetrictohost {
    my $outbuf = '';
    my $query = "SELECT
	    mv.MetricID as \"MetricID\",
	    m.Name      as \"Name\"
	FROM metricvalue as mv, metric as m
	WHERE
	    mv.HostID=$FORM_DATA{hostid}
	and mv.MetricID=$FORM_DATA{addmetricid}
	and m.MetricID=mv.MetricID";
    print "<br>Query=$query<br>" if $debug;
    my $sth = $dbh->prepare($query);
    $sth->execute() or perish ($sth->errstr);
    my $id = undef;
    my $name = undef;
    while (my $row=$sth->fetchrow_hashref()) {
	$id   = $$row{MetricID};
	$name = $$row{Name};
    }
    if (defined($sth->err)) {
	$outbuf .= '<p class=insighterror align=center>Database problem while fetching metric name.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
    }
    $sth->finish();
    if ($id) {
	$outbuf .= "<p class=insighterror align=center>ERROR:&nbsp; A metric named \"$name\" already exists for this host.<br>Duplicate entries are not permitted.</p>";
    }
    if ($outbuf ne '') {
        return $outbuf;
    }
    if (!$FORM_DATA{modclusterid}) {	# If no cluster ID has been specified, use the Default cluster.
	my $query = "SELECT ClusterID as \"ClusterID\" from cluster WHERE Name='Default'";
	print "<br>Query=$query<br>" if $debug;
	my $sth = $dbh->prepare($query);
	$sth->execute() or perish ($sth->errstr);
	while (my $row=$sth->fetchrow_hashref()) {
	    $FORM_DATA{modclusterid} = $$row{ClusterID};
	}
	if (defined($sth->err)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while fetching cluster ID.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
	}
	$sth->finish();
	# FIX LATER:  What should we do if we did not find the Default cluster?  Is it okay to just use undef, or should we object and fail this action?
	if ($outbuf ne '') {
	    return $outbuf;
	}
    }
    $query = "INSERT INTO metricvalue (MetricValueID,ClusterID,HostID,MetricID,Description,Critical,Warning,Duration,LocationID) VALUES (default,".
		    "'$FORM_DATA{modclusterid}',".
		    "'$FORM_DATA{hostid}',".
		    "'$FORM_DATA{addmetricid}',".
		    "(SELECT Description from metric where MetricID=$FORM_DATA{addmetricid}),".
		    "(SELECT Critical from metric where MetricID=$FORM_DATA{addmetricid}),".
		    "(SELECT Warning from metric where MetricID=$FORM_DATA{addmetricid}),".
		    "(SELECT Duration from metric where MetricID=$FORM_DATA{addmetricid}),".
		    "(SELECT LocationID from location where Name='Default')".
		    ");";
    print "<br>Query=$query<br>" if $debug;
    if (! $dbh->do($query)) {
	$outbuf .= '<p class=insighterror align=center>Database problem while inserting metric value.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
	return $outbuf;
    }
    $query = "SELECT Name as \"Name\" from metric where MetricID=$FORM_DATA{addmetricid}";
    print "<br>Query=$query<br>" if $debug;
    $sth = $dbh->prepare($query);
    $sth->execute() or perish ($sth->errstr);
    while (my $row=$sth->fetchrow_hashref()) {
	$name = $$row{Name};
    }
    if (defined($sth->err)) {
	$outbuf .= '<p class=insighterror align=center>Database problem while fetching metric name.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
    } else {
	$outbuf .= "<p class=insight align=center>Metric \"$name\" has been added.</p>";
    }
    $sth->finish();
    return $outbuf;
}

sub updatehost {
    my $outbuf = '';
    # See if we need to update the cluster info
    if ($FORM_DATA{hostid} and $FORM_DATA{modHostName} and $FORM_DATA{modHostIPAddress}) {
	my $query = "UPDATE host SET Name='$FORM_DATA{modHostName}',Description='$FORM_DATA{modHostDescription}', ".
			"IPAddress='$FORM_DATA{modHostIPAddress}' ".
			"WHERE HostID=$FORM_DATA{hostid}";
	print "<br>Query=$query<br>" if $debug;
	if (! $dbh->do($query)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while updating host definition.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
	    return $outbuf;
	}
	if ($FORM_DATA{modclusterid}) {
	    $query = "SELECT ClusterHostID as \"ClusterHostID\", ClusterID as \"ClusterID\" FROM clusterhost WHERE HostID=$FORM_DATA{hostid}";
	    print "<br>Query=$query<br>" if $debug;
	    my $sth = $dbh->prepare($query);
	    $sth->execute() or perish ($sth->errstr);
	    my %clusterhostid=();
	    my $alreadysetflag = 0;
	    while (my $row=$sth->fetchrow_hashref()) {
		$clusterhostid{$$row{ClusterHostID}} = $$row{ClusterID};
		$alreadysetflag = 1;
	    }
	    if (defined($sth->err)) {
		$outbuf .= '<p class=insighterror align=center>Database problem while fetching cluster/host references.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
	    }
	    $sth->finish();
	    if ($outbuf ne '') {
		return $outbuf;
	    }
	    if (!$alreadysetflag) {	# if not in clusterhost, then insert
		$query = "INSERT INTO clusterhost (HostID,ClusterID) VALUES($FORM_DATA{hostid},$FORM_DATA{modclusterid})";
		print "<br>Query=$query<br>" if $debug;
		if (! $dbh->do($query)) {
		    $outbuf .= '<p class=insighterror align=center>Database problem while inserting cluster/host reference.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
		    return $outbuf;
		}
	    } else {		# Assumes only one cluster per host
		$query = "UPDATE clusterhost SET ClusterID=$FORM_DATA{modclusterid} WHERE HostID=$FORM_DATA{hostid}";
		print "<br>Query=$query<br>" if $debug;
		if (! $dbh->do($query)) {
		    $outbuf .= '<p class=insighterror align=center>Database problem while updating cluster/host reference.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
		    return $outbuf;
		}
	    }
	} else {
	    $query = "DELETE FROM clusterhost WHERE HostID=$FORM_DATA{hostid}";
	    print "<br>Query=$query<br>" if $debug;
	    if (! $dbh->do($query)) {
		$outbuf .= '<p class=insighterror align=center>Database problem while deleting cluster/host reference.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
		return $outbuf;
	    }
	}
	$outbuf .= "<p class=insight align=center>Host \"$FORM_DATA{modHostName}\" has been updated.</p>";
    }
    # See if we need to delete any cluster metric values
    if ($FORM_DATA{metricdeleteid}) {
	my @mvids = split "\0", $FORM_DATA{metricdeleteid};
	my $tmpstring = "";
	foreach my $id (@mvids) {
	    if (defined($id)) {
		$tmpstring .= "$id,";
	    }
	}
	$tmpstring =~ s/,$//;	# delete trailing comma
	my $query = "DELETE from metricvalue WHERE MetricValueID IN ($tmpstring)";
	print "<br>Query=$query<br>" if $debug;
	if (! $dbh->do($query)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while deleting metric values.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
	    return $outbuf;
	}
	$outbuf .= "<p class=insight align=center>Host \"$FORM_DATA{modHostName}\" metric values have been deleted.</p>";
    }
    # See if we need to update the cluster metric values
    if ($FORM_DATA{metricvalueid}) {
	if (!$FORM_DATA{modclusterid}) {
	    $outbuf .= "<p class=insighterror align=center>Cluster is not defined.&nbsp; A valid cluster must be defined for metrics to be valid.</p>";
	    return $outbuf;
	}
	my @mvids = split "\0", $FORM_DATA{metricvalueid};
	foreach my $id (@mvids) {
	    if (defined($id)) {
		my $query = "UPDATE metricvalue SET ".
		    "Description='".$FORM_DATA{"modMVDescription$id"}."', ".
		    "ClusterID='".$FORM_DATA{"modclusterid"}."', ".
		    "Warning='".$FORM_DATA{"modMVWarning$id"}."', ".
		    "Critical='".$FORM_DATA{"modMVCritical$id"}."', ".
		    "Duration='".$FORM_DATA{"modMVDuration$id"}."' ".
		    "WHERE MetricValueID=$id";
		print "<br>Query=$query<br>" if $debug;
		if (! $dbh->do($query)) {
		    $outbuf .= '<p class=insighterror align=center>Database problem while updating metric values.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
		    return $outbuf;
		}
	    }
	}
	$outbuf .= "<p class=insight align=center>Host \"$FORM_DATA{modHostName}\" metric values have been updated.</p>";
    }
    return $outbuf;
}

sub deletehost {
    my $outbuf = '';

    # See if we need to delete any host metric values.
    my $hostname=undef;
    if ($FORM_DATA{hostid}) {

	# Grab the hostname now, before we end up deleting it.
	my $query = "SELECT Name as \"Name\" FROM host where HostID=$FORM_DATA{hostid}";
	my $sth = $dbh->prepare($query);
	$sth->execute() or perish ($sth->errstr);
	while (my $row=$sth->fetchrow_hashref()) {
	    $hostname = $$row{Name};
	}
	if (defined($sth->err)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while fetching host name.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
	}
	$sth->finish();
	if ($outbuf ne '') {
	    return $outbuf;
	}

	# Delete all metric values for this host.
	$query = "DELETE from metricvalue WHERE HostID=$FORM_DATA{hostid}";
	print "<br>Query=$query<br>" if $debug;
	if (! $dbh->do($query)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while deleting metric values.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
	    return $outbuf;
	}

	# Delete all metric instance values that have hostinstances for this host.
	$query = "DELETE from metricinstance WHERE HostInstanceID IN (SELECT HostInstanceID from hostinstance WHERE HostID=$FORM_DATA{hostid})";
	print "<br>Query=$query<br>" if $debug;
	if (! $dbh->do($query)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while deleting metric instances.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
	    return $outbuf;
	}

	# Delete all host instance values for this host.
	$query = "DELETE from hostinstance WHERE HostID=$FORM_DATA{hostid}";
	print "<br>Query=$query<br>" if $debug;
	if (! $dbh->do($query)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while deleting host instance.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
	    return $outbuf;
	}

	# Delete all cluster references associated with this host. Cluster definitions are not deleted.
	$query = "DELETE from clusterhost WHERE HostID=$FORM_DATA{hostid}";
	print "<br>Query=$query<br>" if $debug;
	if (! $dbh->do($query)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while deleting cluster/host references.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
	    return $outbuf;
	}

	# Delete this host.
	$query = "DELETE from host WHERE HostID=$FORM_DATA{hostid}";
	print "<br>Query=$query<br>" if $debug;
	if (! $dbh->do($query)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while deleting host definition.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
	    return $outbuf;
	}

	$outbuf .= "<p class=insight align=center>Host \"$hostname\" has been deleted.</p>";
    } else {
	$outbuf .= "<p class=insighterror align=center>There is no specified host to delete.</p>";
    }

    return $outbuf;
}

sub filterhosts {
    my $outbuf = '';

    if (defined($FORM_DATA{HostnamePattern})) {
	$FORM_DATA{HostnamePattern} =~ s/^\s+//;
	$FORM_DATA{HostnamePattern} =~ s/\s+$//;
    }
    else {
	$FORM_DATA{HostnamePattern} = '';
    }
    if (defined($FORM_DATA{IPAddressPattern})) {
	$FORM_DATA{IPAddressPattern} =~ s/^\s+//;
	$FORM_DATA{IPAddressPattern} =~ s/\s+$//;
    }
    else {
	$FORM_DATA{IPAddressPattern} = '';
    }
    if (defined($FORM_DATA{MaxHostListSize})) {
	$FORM_DATA{MaxHostListSize} =~ s/^[0\s]+(.)/$1/;	# drop leading zeroes
	$FORM_DATA{MaxHostListSize} =~ s/\s+$//;
    }
    else {
	$FORM_DATA{MaxHostListSize} = '';
    }
    print <<EOF;
    <input type=hidden name=cmd value='showfilteredhosts'>
    <br>
    <table class=insightcontrolpanel>
	<tr class=insightgray-bg>
	    <th class=insight>Host Selection Filter</th>
	</tr>
    </table>
    <table class=insightcontrolpanel>
	<tr class=insightgray-bg>
EOF

    my %checked = ();
    $checked{ defined( $FORM_DATA{id} ) ? $FORM_DATA{id} : '' } = "SELECTED";
    my $query = "SELECT ClusterID as \"ClusterID\", Name as \"Name\" FROM cluster";
    my $sth = $dbh->prepare($query);
    $sth->execute() or perish ($sth->errstr);

    $options{'title'} = pad_tooltip_title "&nbsp;Actual Cluster&nbsp;";
    $thisdoc = $doc{'actualclusterchoice'};
    $tooltip = $tt->tooltip($thisdoc, \%options);
    my $SELECTED = $checked{''} || '';
    print <<EOF;
	    <td class=insightborderright><b><span $tooltip>Actual Cluster</span></b>&nbsp;&nbsp;<select name=id class=insight>
		<option class=insight $SELECTED value=''></option>
EOF
    while (my $row=$sth->fetchrow_hashref()) {
	my $id = $$row{ClusterID};
	$SELECTED = $checked{$id} || '';
	print "<option class=insight value='$id' $SELECTED>".$$row{Name}."</option>\n";
    }
    if (defined($sth->err)) {
	$outbuf .= '<p class=insighterror align=center>Database problem while fetching cluster names.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
    }
    $sth->finish();
    print <<EOF;
		</select>
	    </td>
EOF

    $options{'title'} = pad_tooltip_title "&nbsp;Hostname Pattern&nbsp;";
    $thisdoc = $doc{'hostnamepattern'};
    $tooltip = $tt->tooltip($thisdoc, \%options);
    print <<EOF;
	    <td class=insightborderright><b><span $tooltip>Hostname Pattern</span></b>&nbsp;&nbsp;<input class=insight size=32 maxlength=64 TYPE=TEXT NAME=HostnamePattern VALUE=\"$FORM_DATA{HostnamePattern}\"></td>
EOF

    $options{'title'} = pad_tooltip_title "&nbsp;IP Address Pattern&nbsp;";
    $thisdoc = $doc{'ipaddresspattern'};
    $tooltip = $tt->tooltip($thisdoc, \%options);
    print <<EOF;
	    <td class=insight><b><span $tooltip>IP Address Pattern</span></b>&nbsp;&nbsp;<input class=insight size=25 maxlength=24 TYPE=TEXT NAME=IPAddressPattern VALUE=\"$FORM_DATA{IPAddressPattern}\"></td>
	</tr>
EOF

    $options{'title'} = pad_tooltip_title "&nbsp;Max Number of Hosts to List&nbsp;";
    $thisdoc = $doc{'maxhoststolist'};
    $tooltip = $tt->tooltip($thisdoc, \%options);
    print <<EOF;
	<tr>
	    <td class=insightborderright><b><span $tooltip>Max Number of Hosts to List</span></b>&nbsp;&nbsp;<input class=insight size=6 maxlength=5 TYPE=TEXT NAME=MaxHostListSize VALUE=\"$FORM_DATA{MaxHostListSize}\"></td>
EOF

    my %radiochecked;
    $radiochecked{2} = '';
    $radiochecked{3} = '';
    $radiochecked{4} = '';
    $radiochecked{5} = '';
    $radiochecked{$hostname_columns} = "CHECKED";
    $options{'title'} = pad_tooltip_title "&nbsp;Number of Columns&nbsp;";
    $thisdoc = $doc{'columns'};
    $tooltip = $tt->tooltip($thisdoc, \%options);
    print <<EOF;
	    <td class=insightborderright style='vertical-align: baseline; padding-top: 4px;'>
		<b><span $tooltip>Number of Columns</span></b>&nbsp;&nbsp;
		<input class=insightradio TYPE=radio id='col2' name='columns' VALUE=2 $radiochecked{2}>&nbsp;2&nbsp;&nbsp;
		<input class=insightradio TYPE=radio id='col3' name='columns' VALUE=3 $radiochecked{3}>&nbsp;3&nbsp;&nbsp;
		<input class=insightradio TYPE=radio id='col4' name='columns' VALUE=4 $radiochecked{4}>&nbsp;4&nbsp;&nbsp;
		<input class=insightradio TYPE=radio id='col5' name='columns' VALUE=5 $radiochecked{5}>&nbsp;5&nbsp;&nbsp;
	    </td>
EOF

    print <<EOF;
	    <td class=insight colspan=3 align=center>
		<input class=orangebutton type=submit value='Search'>
		&nbsp;
		<input class=orangebutton type=button value='Cancel' onClick='changePage(\"$thisprogram?cmd=home\")'>
	    </td>
EOF
    print <<EOF;
	</tr>
    </table>
    <br>
    <script type="text/javascript">
	document.selectForm.HostnamePattern.focus();
    </script>
EOF
    if ($outbuf ne '') {
        print $outbuf;
    }
}

sub showfilteredhosts {
    my $outbuf = '';
    my $query;
    my $sth;
    my @id = ();
    my @hostname = ();
    my @ipaddress = ();
    my @clustername = ();
    my $selectedclusterid;
    my $hostnamepattern;
    my $ipaddresspattern;
    my $maxhostlistsize;
    my $matchedhosts;
    my $allmatchedhosts;

    # Validate our SQL matching patterns, partly to ensure we're not a victim of an SQL injection attack.
    if (defined($FORM_DATA{id}) && $FORM_DATA{id} ne '') {
        $selectedclusterid = $FORM_DATA{id};
    }
    if ($FORM_DATA{HostnamePattern} ne '') {
	$hostnamepattern = $FORM_DATA{HostnamePattern};
	if ($hostnamepattern !~ m/^[-.a-zA-Z0-9*?]+$/) {
	    print '<p class="insighterror" align="center">The Hostname Pattern must be empty (no constraint) or follow the guidelines in the help message (wave your mouse over the label).</p>';
	    return;
	}
	# Substitute what PostgreSQL and MySQL need for the more natural wildcard characters the user can type.
	$hostnamepattern =~ tr/*?/%_/;
    }
    if ($FORM_DATA{IPAddressPattern} ne '') {
	$ipaddresspattern = $FORM_DATA{IPAddressPattern};
	if ($ipaddresspattern !~ m/^[.0-9*?]+$/) {
	    print '<p class="insighterror" align="center">The IP Address Pattern must be empty (no constraint) or follow the guidelines in the help message (wave your mouse over the label).</p>';
	    return;
	}
	$ipaddresspattern =~ tr/*?/%_/;
    }
    if ($FORM_DATA{MaxHostListSize} ne '') {
	$maxhostlistsize = $FORM_DATA{MaxHostListSize};
	if ($maxhostlistsize !~ m/^[0-9]+$/ || $maxhostlistsize <= 0) {
	    print '<p class="insighterror" align="center">The Max Number of Hosts to List must be empty (unlimited) or a positive integer.</p>';
	    return;
	}
    }

    my $ClusterName;
    my %clustername;
    my $ClusterID;
    my %clusterid;

    if (defined($selectedclusterid) || defined($hostnamepattern) || defined($ipaddresspattern)) {
	# We cannot perform a join at the database level because many hosts may not have associated clusters.
	# So we must read those tables in bulk and then make those associations here, when possible.

	$query = "SELECT ClusterID as \"ClusterID\", Name as \"ClusterName\" FROM cluster";
	$sth = $dbh->prepare($query);
	$sth->execute() or perish ($sth->errstr);
	while (my $row=$sth->fetchrow_hashref()) {
	    $clustername{$$row{ClusterID}} = $$row{ClusterName};
	}
	if (defined($sth->err)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while fetching cluster names.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
	}
	$sth->finish();
	if ($outbuf ne '') {
	    print $outbuf;
	    return;
	}

	$query = "SELECT HostID as \"HostID\", ClusterID as \"ClusterID\" FROM clusterhost";
	$sth = $dbh->prepare($query);
	$sth->execute() or perish ($sth->errstr);
	while (my $row=$sth->fetchrow_hashref()) {
	    $clusterid{$$row{HostID}} = $$row{ClusterID};
	}
	if (defined($sth->err)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while fetching cluster/host references.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
	}
	$sth->finish();
	if ($outbuf ne '') {
	    print $outbuf;
	    return;
	}

	$query = "SELECT h.HostID as \"HostID\", h.Name as \"HostName\", h.IPAddress as \"IPAddress\"";
	if (defined($selectedclusterid)) {
	    $query .= " FROM host h, hostinstance hi where hi.ClusterID=$selectedclusterid and h.HostID=hi.HostID and h.Name != 'Default'";
	} else {
	    $query .= " FROM host h where h.Name != 'Default'";
	}
	if (defined($hostnamepattern)) {
	    my $like = $is_postgresql ? 'ilike' : 'like';
	    $query .= " and h.Name $like '$hostnamepattern'";
	}
	if (defined($ipaddresspattern)) {
	    $query .= " and h.IPAddress like '$ipaddresspattern'";
	}
	$query .= " ORDER BY \"HostName\"";
	if (defined($maxhostlistsize)) {
	    $query .= " limit " . ($maxhostlistsize + 1);
	}

	print '<p class="insighterror" align="center">' . "Your query was: $query" . '</p>' if $debug;
	$sth = $dbh->prepare($query);
	$sth->execute() or perish ($sth->errstr);
    } else {
	print '<p class="insighterror" align="center">To generate a list of hosts, you must specify at least one Cluster, Hostname Pattern, or IP Address Pattern.</p>';
        return;
    }

    print <<EOF;
    <br>
    <input type=hidden name=hoststodelete value=''>
    <table class=insightcontrolpanel>
	<tr class=insightgray-bg>
EOF

    for (my $col = 0; $col < $hostname_columns; ++$col) {
	print "<th class=insightborderrightbegin>&nbsp;</th>" if ($col);
	my $last_column_class = ( $col + 1 == $hostname_columns ) ? 'insightleft' : 'insightborderright';
	print <<EOF;
	    <th class=insightborderright>Delete</th>
	    <th class=insightborderright>Hostname</th>
	    <th class=insightborderright>IP Address</th>
	    <th class=$last_column_class>Threshold Cluster</th>
EOF
    }

    print <<EOF;
	</tr>
EOF

    $matchedhosts = 0;
    $allmatchedhosts = 0;
    while (my $row=$sth->fetchrow_hashref()) {
	++$allmatchedhosts;
	if (! defined($maxhostlistsize) || $matchedhosts < $maxhostlistsize) {
	    ++$matchedhosts;
	    # FIX MINOR:  This is possible not completely appropriate, since we're only listing one cluster.  We need to ask Peter about this.
	    $ClusterName = '';
	    if (exists($clusterid{$$row{HostID}})) {
		$ClusterID = $clusterid{$$row{HostID}};
		if (exists($clustername{$ClusterID})) {
		    $ClusterName = $clustername{$ClusterID};
		}
	    }
	    push @id,          $$row{HostID};
	    push @hostname,    $$row{HostName};
	    push @ipaddress,   $$row{IPAddress};
	    push @clustername, $ClusterName;
	}
    }
    if (defined($sth->err)) {
	$outbuf .= '<p class=insighterror align=center>Database problem while fetching host names.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
    }
    $sth->finish();

    for (my $col = $hostname_columns; --$col > 0; ) {
	if ((scalar @id) % $hostname_columns) {
	    push @id, -1;
	    push @hostname, "";
	    push @ipaddress, "";
	    push @clustername, "";
	}
    }

    my $outrows = (scalar @id) / $hostname_columns;
    my $cluster_id = '';
    for (my $hostrow = 0; $hostrow < $outrows; ++$hostrow) {
	print "<tr class=insightgray-bg>";

	for (my $col = 0; $col < $hostname_columns; ++$col) {
	    my $separator_class         = ( $hostrow + 1 < $outrows )      ? 'insightheadborderright' : 'insightheadborderrightend';
	    my $threshold_cluster_class = ( $col + 1 < $hostname_columns ) ? 'insightborderright'     : 'insight';
	    print "<td class=$separator_class>&nbsp;</td>" if ($col);
	    if ($id[$hostrow+$outrows*$col] < 0) {
		print "<td class=insight>&nbsp;</td>";
	    } else {
		print "<td class=insight align=center><input class=insight TYPE=checkbox name='check_$id[$hostrow+$outrows*$col]' VALUE=$id[$hostrow+$outrows*$col]></td>";
	    }
	    $cluster_id = $clusterid{$id[$hostrow+$outrows*$col]};
	    $cluster_id = '' if not defined $cluster_id;
	    print <<EOF;
		<td class=insight>
		    <a href=#host$hostrow onClick="changePage('$thisprogram?cmd=modhost&hostid=$id[$hostrow+$outrows*$col]')">$hostname[$hostrow+$outrows*$col]</a>
		</td>
		<td class=insight>$ipaddress[$hostrow+$outrows*$col]</td>
		<td class=$threshold_cluster_class>
		    <a href=#host$hostrow onClick="changePage('$thisprogram?cmd=modcluster&clusterid=$cluster_id')">$clustername[$hostrow+$outrows*$col]</a>
		</td>
EOF
	}

	print "</tr>";
    }
    my $cols = $hostname_columns * 5 - 1;
    print <<EOF;
	<tr>
	    <td class=insightbuttons colspan=$cols align=center>
		<input class=orangebuttonrow type=button value='Select All Listed Hosts' onClick='SelectAll("check_",true);'>
		&nbsp;
		<input class=orangebuttonrow type=button value='Deselect All Listed Hosts' onClick='SelectAll("check_",false);'>
		&nbsp;
		<input class=orangebuttonrow type=button value='Delete Selected Hosts' onClick='postselectedhosts("check_")'>
		&nbsp;
		<input class=orangebuttonrow type=button value='Cancel' onClick='changePage(\"$thisprogram?cmd=home\")'>
	    </td>
	</tr>
    </table>
    <br>
EOF
    if ($outbuf ne '') {
        print $outbuf;
	return;
    }
    if ($outrows == 0) {
	print '<p class="insighterror" align="center" id=statusmsg>No hosts match your filter criteria.</p>';
    } else {
	print '<p class="insight" align="center" id=statusmsg>' . "$matchedhosts " . ($matchedhosts == 1 ? 'host matches' : 'hosts match') . ' your filter criteria.';
	if (defined($maxhostlistsize) && $matchedhosts < $allmatchedhosts) {
	    print '<br>(This number is limited by your Max Number of Hosts to List filter. More hosts would otherwise match.)';
	}
	print '</p>';
    }
    return;
}

sub deleteselectedhosts {
    my $outbuf = '';

    # This message should never be triggered, as the calling code should have checked this condition already.
    # But this serves as insurance nonetheless.
    if ($FORM_DATA{hoststodelete} eq '') {
	$outbuf .= "<p class=insighterror align=center>There are no selected hosts to delete.</p>";
	return $outbuf;
    }

    my @hostids = split /~/, $FORM_DATA{hoststodelete};

    # print "<p class=insight>Host IDs to delete: where HostID in (" . (join ', ', @hostids) . ")</p>";

    my $hostcount = scalar @hostids;

    # FIX MINOR:  go back and revisit this code once I have my Perl book in hand
    while (scalar @hostids) {

        my $hostlist = join ', ', splice(@hostids, 0, $max_delete_hosts);

	# Delete all metric values for this host.
	my $query = "DELETE from metricvalue WHERE HostID in ($hostlist)";
	print "<br>Query=$query<br>" if $debug;
	if (! $dbh->do($query)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while deleting metric values.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
	    return $outbuf;
	}

	# Delete all metric instance values that have hostinstances for this host.
	$query = "DELETE from metricinstance WHERE HostInstanceID IN (SELECT HostInstanceID from hostinstance WHERE HostID in ($hostlist))";
	print "<br>Query=$query<br>" if $debug;
	if (! $dbh->do($query)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while deleting metric instances.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
	    return $outbuf;
	}

	# Delete all host instance values for this host.
	$query = "DELETE from hostinstance WHERE HostID in ($hostlist)";
	print "<br>Query=$query<br>" if $debug;
	if (! $dbh->do($query)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while deleting host instances.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
	    return $outbuf;
	}

	# Delete all cluster references associated with this host. Cluster definitions are not deleted.
	$query = "DELETE from clusterhost WHERE HostID in ($hostlist)";
	print "<br>Query=$query<br>" if $debug;
	if (! $dbh->do($query)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while deleting cluster/host references.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
	    return $outbuf;
	}

	# Delete this host.
	$query = "DELETE from host WHERE HostID in ($hostlist)";
	print "<br>Query=$query<br>" if $debug;
	if (! $dbh->do($query)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while deleting host definitions.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
	    return $outbuf;
	}
    }

    # (The following comments may be obsolete.)
    # Since we didn't actually look at the return codes from the database deletions, we don't actually know whether
    # this many hosts got deleted at thie time.  All we really know is that the database queries did not fail.
    # Nonetheless, for now we'll report the count as such so we have stable output should the user get here by using
    # the browser Back or Refresh buttons.  In some future version, perhaps we'll dig out from the responses exactly
    # how many rows were deleted from which tables, and roll that up into some figure to report out here.
    $outbuf .= '<p class=insight align="center">' . "$hostcount " . ($hostcount == 1 ? "host has" : "hosts have") . " been deleted.</p>";

    return $outbuf;
}

sub validateconfiguration {
    my $outbuf = '';
    my %host_clusters = ();
    my $got_db_error = 0;
    my $query = "
	select h.HostID as \"HostID\", h.Name as \"HostName\", c.ClusterID as \"ClusterID\", c.Name as \"ClusterName\"
	from host h, hostinstance hi, cluster c where h.HostID in
	    (select HostID from (select HostID, num from
		(select HostID, count(*) as num from hostinstance group by HostID)
	    as tt where num > 1) as ee)
	and hi.HostID = h.HostID and c.ClusterID = hi.ClusterID
	";
    my $sth = $dbh->prepare($query);
    $sth->execute() or perish ($sth->errstr);
    while (my $row=$sth->fetchrow_hashref()) {
	my $hostid      = $$row{HostID};
	my $hostname    = $$row{HostName};
	my $clusterid   = $$row{ClusterID};
	my $clustername = $$row{ClusterName};
	$host_clusters{$hostname}{hostid} = $hostid;
	# Alas, cluster names are not constrained to be unique, so we need to key on the unique cluster ID.
	$host_clusters{$hostname}{clusters}{$clusterid} = $clustername;
    }
    if (defined($sth->err)) {
	$got_db_error = 1;
	$outbuf .= '<p class=insighterror align=center>Database problem while validating host/cluster associations.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
    }
    $sth->finish();

    if (%host_clusters) {
	# FIX LATER:  perhaps enclose this table in a scrollable div to limit its vertical size
	$options{'title'} = pad_tooltip_title "&nbsp;Multi-Cluster Hosts&nbsp;";
	$thisdoc = $doc{'multiclusterhosts'};
	my $tooltip = $tt->tooltip($thisdoc, \%options);
	print <<EOF;
	    <table class=insightcontrolpanel>
	    <colgroup>
	    <col width="0*">
	    <col>
	    </colgroup>
	    <tr><th class=insight colspan=2><span $tooltip>Hosts that belong to more than one cluster</span></th></tr>
	    <tr>
		<th class=insightrow2borderright>Host</th>
		<th class=insightrow2>Clusters Containing This Host</th>
	    </tr>
EOF
        foreach my $host (sort keys %host_clusters) {
	    my $hostid = $host_clusters{$host}{hostid};
	    my @clusters = ();
	    foreach my $clusterid ( sort { $a <=> $b } keys %{ $host_clusters{$host}{clusters} } ) {
	        push @clusters, "<a href='$thisprogram?cmd=modcluster&clusterid=$clusterid'>$host_clusters{$host}{clusters}{$clusterid}</a>";
	    }
	    my $clusters = join( ', ', @clusters );
	    print <<EOF;
	    <tr>
		<td class=insight style="white-space: nowrap;"><a href="$thisprogram?cmd=modhost&hostid=$hostid">$host</a></td>
		<td class=insight>$clusters</td>
	    </tr>
EOF
	}
	print <<EOF;
	    </table>
EOF
    }
    elsif (not $got_db_error) {
	$outbuf .= "<p class=insight>No hosts belong to more than one cluster.</p>";
    }

    if ($outbuf ne '') {
        print $outbuf;
    }
    return;
}

sub addmetric {
    print <<EOF;
    <br>
    <table class=insightcontrolpanel>
	<tbody>
	    <tr class=insightgray-bg>
		<th class=insight colspan=2>Add Metric</th>
	    </tr>
	</tbody>
    </table>
    <input type=hidden name=cmd value=insertmetric>
    <table class=insightcontrolpanel>
	<tr>
	    <td class=insight><b>Metric Name</b></td>
	    <td class=insight><input class=insight size=60 maxlength=255 TYPE=TEXT NAME=Name VALUE=\"\"></td>
	</tr>
	<tr>
	    <td class=insight><b><span $description_tooltip>Metric Description</span></b></td>
	    <td class=insight><TEXTAREA class=insight ROWS=5 COLS=80 NAME=Description></TEXTAREA></td>
	</tr>
	<tr>
	    <td class=insight><b><span $warning_tooltip>Default Warning Threshold</span></b></td>
	    <td class=insight><input class=insight size=50 maxlength=50 TYPE=TEXT NAME=Warning VALUE=\"\"></td>
	</tr>
	<tr>
	    <td class=insight><b><span $critical_tooltip>Default Critical Threshold</span></b></td>
	    <td class=insight><input class=insight size=50 maxlength=50 TYPE=TEXT NAME=Critical VALUE=\"\"></td>
	</tr>
	<tr>
	    <td class=insight><b><span $duration_tooltip>Default Duration Threshold</span></b></td>
	    <td class=insight><input class=insight size=50 maxlength=50 TYPE=TEXT NAME=Duration VALUE=\"\"></td>
	</tr>
	<tr>
	    <td class=insightbuttons colspan=2 align=center>
		<input class=orangebuttonrow type=submit value='Add'>
		&nbsp;
		<input class=orangebuttonrow type=button value='Cancel' onClick='changePage(\"$thisprogram?cmd=home\")'>
	    </td>
	</tr>
    </table>
    <p class="insight" align="center" id=statusmsg></p>
    <br>
    <script type="text/javascript">
	function validatemetric () {
	    this.Name.value = trim(this.Name.value);
	    return validateElement ('metric', this.Name.value);
	}
	document.selectForm.onsubmit = validatemetric;
	document.selectForm.Name.focus();
    </script>
EOF
    return;
}

sub insertmetric {
    my $outbuf = '';
    my $query = "SELECT MetricID as \"MetricID\" FROM metric WHERE Name='$FORM_DATA{Name}'";
    print "<br>Query=$query<br>" if $debug;
    my $sth = $dbh->prepare($query);
    $sth->execute() or perish ($sth->errstr);
    my $id = undef;
    while (my $row=$sth->fetchrow_hashref()) {
	$id = $$row{MetricID};
    }
    if (defined($sth->err)) {
	$outbuf .= '<p class=insighterror align=center>Database problem while fetching metric ID.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
    }
    $sth->finish();
    if ($id) {
	$outbuf .= "<p class=insighterror align=center>ERROR:&nbsp; A metric named \"$FORM_DATA{Name}\" already exists.";
	$outbuf .= "<br>Duplicate entries are not permitted.&nbsp; Delete the existing entry before adding this entry.</p>";
    }
    if ($outbuf ne '') {
        return $outbuf;
    }
    $query = "INSERT INTO metric (Name,Description,Critical,Warning,Duration) VALUES (".
		"'$FORM_DATA{Name}',".
		"'$FORM_DATA{Description}',".
		"'$FORM_DATA{Critical}',".
		"'$FORM_DATA{Warning}', ".
		"'$FORM_DATA{Duration}' ".
	    ");";
    print "<br>Query=$query<br>" if $debug;
    if (! $dbh->do($query)) {
	$outbuf .= '<p class=insighterror align=center>Database problem while inserting metric definition.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
    } else {
	$outbuf .= "<p class=insight align=center>Metric \"$FORM_DATA{Name}\" has been added.</p>";
    }
    return $outbuf;
}

sub modmetric {
    my $outbuf = '';
    my $query = "SELECT
	    MetricID    as \"MetricID\",
	    Name        as \"Name\",
	    Description as \"Description\",
	    Critical    as \"Critical\",
	    Warning     as \"Warning\",
	    Duration    as \"Duration\"
	FROM metric
	WHERE MetricID=$FORM_DATA{metricid}";
    print "<br>Query=$query<br>" if $debug;
    my $sth = $dbh->prepare($query);
    $sth->execute() or perish ($sth->errstr);
    while (my $row=$sth->fetchrow_hashref()) {
	my $id          = $$row{MetricID};
	my $name        = $$row{Name};
	my $description = $$row{Description};
	my $warning  = format_number($$row{Warning});
	my $critical = format_number($$row{Critical});
	my $duration = format_number($$row{Duration});
	print <<EOF;
    <input type=hidden name=cmd value='updatemetric'>
    <br>
    <table class=insightcontrolpanel>
	<tbody>
	    <tr class=insightgray-bg>
		<th class=insight colspan=2>Settings for Metric ID $id, Name $name</th>
	    </tr>
	</tbody>
    </table>
    <table class=insightcontrolpanel>
	<tr>
	    <td class=insight><b>Metric Name</b></td>
	    <td class=insight><input class=insight size=60 maxlength=100 TYPE=TEXT NAME=modMetricName VALUE=\"$name\"></td>
	</tr>
	<tr>
	    <td class=insight><b><span $description_tooltip>Metric Description</span></b></td>
	    <td class=insight><input class=insight size=66 maxlength=100 TYPE=TEXT NAME=modMetricDescription VALUE=\"$description\"></td>
	</tr>
	<tr>
	    <td class=insight><b><span $warning_tooltip>Default Warning Threshold</span></b></td>
	    <td class=insight><input class=insight size=50 maxlength=50 TYPE=TEXT NAME=modMetricWarning VALUE=\"$warning\"></td>
	</tr>
	<tr>
	    <td class=insight><b><span $critical_tooltip>Default Critical Threshold</span></b></td>
	    <td class=insight><input class=insight size=50 maxlength=50 TYPE=TEXT NAME=modMetricCritical VALUE=\"$critical\"></td>
	</tr>
	<tr>
	    <td class=insight><b><span $duration_tooltip>Default Duration Threshold</span></b></td>
	    <td class=insight><input class=insight size=50 maxlength=50 TYPE=TEXT NAME=modMetricDuration VALUE=\"$duration\"></td>
	</tr>
EOF
    }
    if (defined($sth->err)) {
	$outbuf .= '<p class=insighterror align=center>Database problem while fetching metric definition.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
    }
    $sth->finish();
    print <<EOF;
	<tr>
	    <td class=insightbuttons colspan=2 align=center>
		<input class=orangebuttonrow type=submit value='Update Metric'>
		&nbsp;
		<input class=orangebuttonrow type=button value='Delete Metric' onClick='changePage(\"$thisprogram?cmd=deletemetric&metricid=$FORM_DATA{metricid}\")'>
		&nbsp;
		<input class=orangebuttonrow type=button value='Cancel' onClick='changePage(\"$thisprogram?cmd=home\")'>
	    </td>
	</tr>
    </table>
    <br>
EOF
    if ($outbuf ne '') {
        print $outbuf;
    }
    return;
}

sub updatemetric {
    my $outbuf = '';
    # See if we need to update the cluster info
    if ($FORM_DATA{metricid} and $FORM_DATA{modMetricName}) {
	my $query = "UPDATE metric SET Name='$FORM_DATA{modMetricName}',Description='$FORM_DATA{modMetricDescription}', ".
			"Critical='$FORM_DATA{modMetricCritical}',Warning='$FORM_DATA{modMetricWarning}',Duration='$FORM_DATA{modMetricDuration}' ".
		    "WHERE MetricID=$FORM_DATA{metricid}";
	print "<br>Query=$query<br>" if $debug;
	if (! $dbh->do($query)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while updating metric definition.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
	} else {
	    $outbuf .= "<p class=insight align=center>Metric \"$FORM_DATA{modMetricName}\" has been updated.</p>";
	}
    }
    return $outbuf;
}

sub deletemetric {
    my $outbuf = '';

    # See if we need to delete any cluster metric values
    my $metricname=undef;
    if ($FORM_DATA{metricid}) {
	my $query = "SELECT Name as \"Name\" FROM metric where MetricID=$FORM_DATA{metricid}";
	print "<br>Query=$query<br>" if $debug;
	my $sth = $dbh->prepare($query);
	$sth->execute() or perish ($sth->errstr);
	while (my $row=$sth->fetchrow_hashref()) {
	    $metricname = $$row{Name};
	}
	if (defined($sth->err)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while fetching metric name.<br>Error=' . $sth->errstr . '; State=' . $sth->state . '</p>';
	}
	$sth->finish();
	if ($outbuf ne '') {
	    return $outbuf;
	}

	# Delete all metric instance values that have hostinstances for this host.
	$query = "DELETE from metricinstance WHERE MetricID=$FORM_DATA{metricid}";
	print "<br>Query=$query<br>" if $debug;
	if (! $dbh->do($query)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while deleting metric instances.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
	    return $outbuf;
	}

	# Delete all metric values for this metric.
	$query = "DELETE from metricvalue WHERE MetricID=$FORM_DATA{metricid}";
	print "<br>Query=$query<br>" if $debug;
	if (! $dbh->do($query)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while deleting metric values.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
	    return $outbuf;
	}

	# Delete this metric.
	$query = "DELETE from metric WHERE MetricID=$FORM_DATA{metricid}";
	print "<br>Query=$query<br>" if $debug;
	if (! $dbh->do($query)) {
	    $outbuf .= '<p class=insighterror align=center>Database problem while deleting metric definition.<br>Error=' . $dbh->errstr . '; State=' . $dbh->state . '</p>';
	    return $outbuf;
	}

	$outbuf .= "<p class=insight align=center>Metric \"$metricname\" has been deleted.</p>";
    } else {
	$outbuf .= "<p class=insighterror align=center>There is no specified metric to delete.</p>";
    }

    return $outbuf;
}

sub showhelp {
    my $outbuf = '';

    print <<EOF;
<table class=insightcontrolpanel>
    <tr>
	<td class=insight colspan=2 style="vertical-align: top; padding: 0.2em 0.5em">
	    <p style="color: #EE0000;">
		<b><i>The help text presented here is preliminary and may be incomplete or simply wrong.</i></b>
	    </p>
	</td>
    </tr>
    <tr>
	<!--
	<td class=insight>$doc{'help'}</td>
	<td class=insight width='50%'>&nbsp;</td>
	-->
	$doc{'help'}
    </tr>
</table>
<br>
EOF

    if ($outbuf ne '') {
        print $outbuf;
    }
    return;
}

sub format_number {
    # Format number for printing
    my $number = shift;
    $number =~ s/(\.\d*?)0+$/$1/;	# Get rid of trailing 0s
    $number =~ s/\.$//;			# Get rid of trailing decimal point
    return $number;
}

sub doc {
    my %doc = ();

    $doc{'taskselection'} = qq{In this section, choose the particular action you wish to take.
	All of these actions are ultimately in service of establishing the metric thresholds that will be checked against the incoming Ganglia data feed.};
    $doc{'thresholdcluster'} = qq{See the description of this value in the Help screen.};
    $doc{'actualcluster'}    = qq{See the description of this value in the Help screen.};
    $doc{'actualclusterchoice'} = qq{You may select a particular Cluster to which the filtered hosts must belong.
	[FIX MAJOR:  Note that this has nothing to do with the Cluster assigned in the Add Host screen;
	rather, it reflects the Cluster to which the host belongs.]
	Choosing the blank selection signifies that no filtering based on the Cluster is to be imposed.};
    $doc{'hostnamepattern'} = qq{Hostname Patterns consist of literal characters (alphanumerics, dashes, and periods) and the following wildcards:
	<br><b>*</b> matches zero or more characters;
	<br><b>?</b> matches exactly one character.
	<br>Leaving this field blank signifies that no filtering based on the Hostname is to be imposed.
	<br>Example: hyb*};
    $doc{'ipaddresspattern'} = qq{IP Address Patterns consist of literal characters (digits and periods) and the following wildcards:
	<br><b>*</b> matches zero or more characters;<br><b>?</b> matches exactly one character.
	<br>Leaving this field blank signifies that no filtering based on the IP Address is to be imposed.
	<br>Example: 192.168.10?.*};
    $doc{'maxhoststolist'} = qq{This is the maximum number of hosts to find and list below.
	You may wish to set this if your Hostname and IP Address patterns might otherwise produce a huge list of matching hosts.
	Leaving this field blank signifies that no limit is to be imposed.
	<br>Example: 200};
    $doc{'columns'} = qq{Specify the number of columns in which the filtered hosts should be displayed.};
    $doc{'metricdescription'} = qq{A human-readable explanation of this metric.};
    $doc{'warningthreshold'} = qq{Define the threshold at which the metric value should be recognized as being in a warning state.
	If no critical threshold is defined, or if the warning threshold is less than or equal to the critical threshold,
	a warning state will be recognized if the metric value is greater than or equal to the warning threshold.
	Conversely, if the warning threshold is greater than the critical threshold,
	a warning state will be recognized if the metric value is less than or equal to the warning threshold.
	<p class=append>
	For each metric value selected and displayed here, at least one of the warning and critical thresholds must be defined.
	If the warning threshold is left undefined, only the critical threshold will be operative.
	</p>};
    $doc{'criticalthreshold'} = qq{Define the threshold at which the metric value should be recognized as being in a critical state.
	If no warning threshold is defined, or if the warning threshold is less than or equal to the critical threshold,
	a critical state will be recognized if the metric value is greater than or equal to the critical threshold.
	Conversely, if the warning threshold is greater than the critical threshold,
	a critical state will be recognized if the metric value is less than or equal to the critical threshold.
	<p class=append>
	For each metric value selected and displayed here, at least one of the warning and critical thresholds must be defined.
	If the critical threshold is left undefined, only the warning threshold will be operative.
	</p>};
    $doc{'durationthreshold'} = qq{Define the duration, in seconds, for which a warning or critical state must persist
	before it is recognized as a significant out-of-bounds condition and it is reported as such to Nagios.};
    $doc{'multiclusterhosts'} = qq{Each of these hosts belongs to more than one cluster, as listed.
	This is not an acceptable configuration,
	because it creates confusion as to which set of cluster-level metric thresholds will be applied to the host.};
    $doc{'help'} = qq{
<td class=insightborderright width="46%" style="vertical-align: top; padding: 0.2em 0.5em">
<p><b>Metric Thresholding Model</b></p>
<p>
Here's how metric values and thresholds are processed:
</p>
<ul>
<li>
Ganglia discovers hosts, and each host resides in a particular Ganglia cluster.
</li>
<li>
Each Ganglia host periodically emits a packet of XML-encoded metric data.
These packets are aggregated from many hosts and made available outside the cluster as a complete Ganglia XML stream for the cluster.
</li>
<li>
The check_ganglia script periodically wakes up, reads the Ganglia XML stream, populates the ganglia configuration database
with newly discovered hosts, and applies metric thresholds defined in this database to the host metric values found in the Ganglia XML stream.
(The ganglia configuration database is the one you view and alter with the tool you are now using.)
</li>
<li>
The check_ganglia script treats the results of metric threshold comparisons as service state data, and reports this state data to Nagios.
It also saves the current metric values in the ganglia database so it can detect changes of state on the next periodic round of comparisons,
and report just this limited data to Nagios.
</li>
</ul>
<p>
The metric thresholds applied to a given host's metrics can come from several sources:
</p>
<ul>
<li>
Host-specific thresholds.
</li>
<li>
A cluster-level set of default thresholds.
</li>
<li>
A global set of default thresholds (defined via the "Default" cluster).
</li>
</ul>
<p>
The source of each threshold can be different from metric to metric, for the same host.
</p>
</td>
<td class=insight style="vertical-align: top; padding: 0.2em 0.5em;">
<p><b>Host/Cluster Associations</b></p>
<p>
When check_ganglia populates the ganglia database with host information, it records which cluster the host resides in.
This is a separate and distinct classification from the threshold-configuration cluster for the host.
Both classifications are important, so they need to be explained.
The terms we will use here and in this tool's screens are:
</p>
<blockquote style="margin: 0.4em 2em;"><i>Actual Cluster</i>: the cluster assignment populated by the check_ganglia script</blockquote>
<blockquote style="margin: 0.4em 2em;"><i>Threshold Cluster</i>: a cluster assignment managed by this tool</blockquote>
<p>
The Threshold Cluster for a host is only important when you are assigning host-specific metric thresholds.
It can take on one of two useful values:  either the same name as the Actual Cluster, or missing ("--&nbsp;none&nbsp;--", as viewed in the user interface).
If you have no host-specific metric thresholds for this host, the setting of the Threshold Cluster does not matter and it should generally be set to missing to prevent confusion.
If you wish to establish host-specific metric thresholds, the Threshold Cluster must first be set to the Actual Cluster name.
This will allow the host-specific metric thresholds to be found and used later on by the check_ganglia script.
If the Threshold Cluster is set to any other value when host-specific metric thresholds are defined, such thresholds will be ignored by the check_ganglia script.
</p>
<p>
The complexity of having separate Threshold Cluster and Actual Cluster values, and requiring the user to set the Threshold Cluster manually,
is primarily due to the historical evolution of this code.
However, it does have a couple of advantages.
A separate value allows the administrator to set up host-specific metric thresholds even before a particular host
is monitored by Ganglia and becomes part of the Ganglia XML stream.
The separate value also helps you to quickly spot those hosts which have host-specific metric thresholds defined,
if you follow the custom of leaving the Threshold Cluster field as missing for hosts without such thresholds.
A filtered list of hosts in the "Find/Delete Multiple Hosts" screen shows the Threshold Cluster in its own column,
and specially configured hosts will stand out in such a listing.
</p>
</td>
};

    return %doc;
}

sub printstyles {
    print <<EOF;
    <style>

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
	background-color: #FFFFFF; /* GroundWork Portal Interface: Background */
	border: 1px solid #666666; /* GroundWork Portal Interface: Gray (Table Fill 1px Outlines) */
	text-align: center;
    }

    table.insightcontrolpanel {
	width: 100%;
	text-align: left;
	background-color: #FFFFFF; /* GroundWork Portal Interface: Background */
	border: 0px solid #FFFFFF; /* GroundWork Portal Interface: Gray (Table Fill 1px Outlines) */
	/*
	border-spacing: 2px 0px;
	*/
	border-spacing: 0px;
	border-width: 0;
	empty-cells: show;
    }

    table.insighttoplist {
	width: 100%;
	background-color: #FFFFFF; /* GroundWork Portal Interface: Background */
	border: 0px solid #666666; /* GroundWork Portal Interface: Gray (Table Fill 1px Outlines) */
    }

    th.insight {
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 13px;
	font-style: normal;
	font-variant: normal;
	font-weight: bold;
	text-decoration: none;
	text-align: center;
	color: #FFFFFF; /* GroundWork Portal Interface: White */
	padding: 4;
	background-color: #4A4A4A; /* GroundWork Portal Interface: Table Fill #1 */
	border: 0px solid #FFFFFF; /* GroundWork Portal Interface: Gray (Table Fill 1px Outlines) */
	border-spacing: 0;
	border-bottom-width: 2px;
    }

    th.insightleft {
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 13px;
	font-style: normal;
	font-variant: normal;
	font-weight: bold;
	text-decoration: none;
	text-align: left;
	color: #FFFFFF; /* GroundWork Portal Interface: White */
	padding: 4;
	background-color: #4A4A4A; /* GroundWork Portal Interface: Table Fill #1 */
	border: 0px solid #FFFFFF; /* GroundWork Portal Interface: Gray (Table Fill 1px Outlines) */
	border-spacing: 0;
	border-bottom-width: 2px;
    }

    th.insighthead {
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 13px;
	font-style: normal;
	font-variant: normal;
	font-weight: bold;
	text-decoration: none;
	text-align: center;
	color: #FFFFFF; /* GroundWork Portal Interface: White */
	padding: 4;
	background-color: #4A4A4A; /* GroundWork Portal Interface: Table Fill #1 */
	border: 0px solid #4A4A4A; /* GroundWork Portal Interface: Gray (Table Fill 1px Outlines) */
	border-spacing: 0;
	border-bottom-width: 2px;
    }

    th.insightborderrightbegin {
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 13px;
	font-style: normal;
	font-variant: normal;
	font-weight: bold;
	text-decoration: none;
	text-align: left;
	color: #FFFFFF; /* GroundWork Portal Interface: White */
	padding: 4;
	background-color: #4A4A4A; /* GroundWork Portal Interface: Table Fill #1 */
	border: 0px solid #FFFFFF; /* GroundWork Portal Interface: Gray (Table Fill 1px Outlines) */
	border-spacing: 0;
	margin-bottom: 2px;
	border-right-width: 2px;
    }

    th.insightborderright {
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 13px;
	font-style: normal;
	font-variant: normal;
	font-weight: bold;
	text-decoration: none;
	text-align: left;
	color: #FFFFFF; /* GroundWork Portal Interface: White */
	padding: 4;
	background-color: #4A4A4A; /* GroundWork Portal Interface: Table Fill #1 */
	border: 0px solid #FFFFFF; /* GroundWork Portal Interface: Gray (Table Fill 1px Outlines) */
	border-spacing: 0;
	border-bottom-width: 2px;
	border-right-width: 2px;
    }

    th.insightrow2 {
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	font-style: normal;
	font-variant: normal;
	font-weight: bold;
	text-decoration: none;
	/*
	text-align: center;
	*/
	text-align: left;
	color: #FFFFFF; /* GroundWork Portal Interface: White */
	spacing: 0;
	padding: 4px 0.5em;
	background-color: #777777; /* GroundWork Portal Interface: Table Fill #1 */
	border: 0px solid #FFFFFF; /* GroundWork Portal Interface: Gray (Table Fill 1px Outlines) */
	border-bottom-width: 2px;
    }

    th.insightrow2borderright {
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	font-style: normal;
	font-variant: normal;
	font-weight: bold;
	text-decoration: none;
	/*
	text-align: center;
	*/
	text-align: left;
	color: #FFFFFF; /* GroundWork Portal Interface: White */
	spacing: 0;
	padding: 4px 0.5em;
	background-color: #777777; /* GroundWork Portal Interface: Table Fill #1 */
	border: 0px solid #FFFFFF; /* GroundWork Portal Interface: Gray (Table Fill 1px Outlines) */
	border-bottom-width: 2px;
	border-right-width: 2px;
    }

    table.insightform {
	background-color: #bfbfbf;
    }

    td.insight {
	color: #000000;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	vertical-align: middle;
	border: 0px solid #FFFFFF;
	border-bottom-width: 2px;
	background-color: #D9D9D9;
	spacing: 2;
	padding: 0 0.5em;
    }

    td.insightborderright {
	color: #000000;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	vertical-align: middle;
	border: 0px solid #FFFFFF;
	border-bottom-width: 2px;
	border-right-width: 2px;
	background-color: #D9D9D9;
	spacing: 2;
	padding: 0 0.5em;
    }

    td.insightbuttons {
	color: #000000;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	vertical-align: middle;
	border: 0px solid #FFFFFF;
	border-bottom-width: 2px;
	background-color: #FFFFFF;
	spacing: 2;
	padding: 0 0.5em;
    }

    td.insightleft {
	color: #000000;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	vertical-align: middle;
	text-align: left;
    }

    td.insightcenter {
	color: #000000;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	vertical-align: middle;
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
	font-size: 18;
	font-weight: bold;
	color: #FA840F;
    }

    td.insighthead {
	background-color: #4A4A4A;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	font-weight: bold;
	color: #ffffff;
	border: 0px solid #4A4A4A;
	border-bottom-width: 2px;
    }

    td.insightheadend {
	background-color: #4A4A4A;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	font-weight: bold;
	color: #ffffff;
	border: 0px solid #FFFFFF;
	border-bottom-width: 2px;
    }

    td.insightheadborderright {
	background-color: #4A4A4A;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	font-weight: bold;
	color: #ffffff;
	border: 0px solid #4A4A4A;
	border-right-color: #FFFFFF;
	border-right-width: 2px;
	margin-bottom: 2px;
    }

    td.insightheadborderrightend {
	background-color: #4A4A4A;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	font-weight: bold;
	color: #ffffff;
	border: 0px solid #FFFFFF;
	border-right-width: 2px;
	border-bottom-width: 2px;
    }

    td.insightsubhead {
	background-color: #8089b9;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	font-weight: bold;
	color: #ffffff;
    }

    td.insightselected {
	background-color: #898787;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	font-weight: bold;
	color: #ffffff;
    }

    td.insightrow1 {
	background-color:
	#dcdcdc;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	font-weight: bold;
    }

    td.insightrow2 {
	background-color: #bfbfbf;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	font-weight: bold;
    }

    td.insightrow_lt {
	background-color: #f4f4f4;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	font-weight: bold;
    }

    td.insightrow_dk {
	background-color: #e2e2e2;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	font-weight: bold;
    }

    td.insighterror {
	background-color: #dcdcdc;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	font-weight: bold;
	color: #cc0000;
    }

    /*
    input, textarea, select {
	border: 0px solid #000099;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	font-weight: bold;
	background-color: #ffffff;
	color: #000000;
    }
    */

    input.insight_disabled {
	border: 0px solid #000099;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	font-weight: bold;
	background-color: #FFFFFF;
	color: #707070;
    }

    input.insight, textarea.insight, select.insight {
	border: 0px solid #000099;
	margin: 2px;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	font-weight: bold;
	background-color: #ffffff;
	color: #000000;
    }

    input.insighttext {
	border: 0px solid #000099;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	font-weight: bold;
	color: #000000;
    }

    input.insightradio {
	border: 0px;
	background-color: #dcdcdc;
	vertical-align: bottom;
    }

    input.insightcheckbox {
	border: 0px;
	background-color: #dcdcdc;
    }

    /*
    input.button {
	border: 1px solid #000000;
	border-style: solid;
	border-top-width: auto;
	border-right-width: auto;
	border-bottom-width: auto;
	border-left-width: auto:
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	font-weight: bold;
	background-color: #898787;
	color: #ffffff;
    }
    */

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

    input.orangebutton {
	background-color: #FA840F;
	color: #ffffff;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	border: 1px solid #FA840F;
	border-radius: 2px;
	margin-top: 5px;
	margin-bottom: 5px;
    }

    input.orangebutton:disabled {
	background-color: #888888;
	border-color: #888888;
    }

    input.orangebuttonrow {
	background-color: #FA840F;
	color: #ffffff;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	border: 1px solid #FA840F;
	border-radius: 2px;
	margin-top: 0px;
	margin-bottom: 5px;
    }

    input.insightbox {
	border: 0px;
    }

    a.insighttop:link {
	color: #ffffff;
	font-size: 12px;
	font-family: verdana, helvetica, arial, sans-serif;
	text-decoration: none;
	font-weight: bold;
    }

    a {
	text-decoration: none;
    }

    a.insighttop:visited {
	color: #ffffff;
	font-size: 12px;
	font-family: verdana, helvetica, arial, sans-serif;
	text-decoration: none;
	font-weight: bold;
    }

    a.insighttop:active {
	color: #ffffff;
	font-size: 12px;
	font-family: verdana, helvetica, arial, sans-serif;
	text-decoration: none;
	font-weight: bold;
    }

    a.insighttop:hover {
	color: #ffffff;
	font-size: 12px;
	font-family: verdana, helvetica, arial, sans-serif;
	text-decoration: none;
	font-weight: bold;
    }

    a.insight:link {
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

    a.insight:active  {
	color: #919191;
	font-size: 12px;
	font-family: verdana, helvetica, arial, sans-serif;
	text-decoration: none;
	font-weight: bold;
    }

    a.insight:hover {
	color: #919191;
	font-size: 12px;
	font-family: verdana, helvetica, arial, sans-serif;
	text-decoration: none;
	font-weight: bold;
    }

    a.insightorange:link {
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

    a.insightorange:active {
	color: #FA840F;
	font-size: 12px;
	font-family: verdana, helvetica, arial, sans-serif;
	text-decoration: none;
	font-weight: bold;
    }

    a.insightorange:hover {
	color: #FA840F;
	font-size: 12px;
	font-family: verdana, helvetica, arial, sans-serif;
	text-decoration: none;
	font-weight: bold;
    }

    /*Center paragraph*/
    p.insight {
	color: #000;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	font-weight: normal;
    }

    p.insighterror {
	color: #FF0000;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	font-weight: bold;
    }

    h1.insight {
	color: #FA840F;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 18px;
	font-weight: 600;
    }

    h2.insight {
	color: #4A4A4A;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 14px;
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
	font-size: 16px;
	font-style: italic;
	font-weight: normal;
    }

    h6.insight {
	color: #000;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 18px;
	font-weight: bold;
    }

    ul {
	margin: 0;
	padding-left: 1.5em;
    }

    p, li {
	color: #000000;
	font-family: verdana, helvetica, arial, sans-serif;
	margin: 0.6em 0;
	line-height: 1.3em;
    }

    p.append {
	color: #000000;
	font-family: verdana, helvetica, arial, sans-serif;
	margin: 0.5em 0 0.1em;
    }

    </style>
EOF
}

#
# perfchart - Groundwork Monitor Architect
# PerfChartForms.pm
#
###############################################################################
# Release 4.6
# June 2018
###############################################################################
#
# Original author: Scott Parris
#
#	Copyright 2005 GroundWork Open Source Solutions, Inc. ("GroundWork")
#	All rights reserved. Use is subject to GroundWork commercial license terms.
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

use strict;

package Forms;

my $is_tomcat          = 1;                        # Set this value to 1 when running in the Portal
my $doc_root_perfchart = '/performance';
my $cgi_dir            = '/performance/cgi-bin';
my $form_class         = 'row1';
my $form_subclass      = $form_class;
my $global_cell_pad    = 3;
my $title_width        = '15%';
if ($is_tomcat) {
    $doc_root_perfchart = '/performance/htdocs/performance';
    $cgi_dir            = '/performance/cgi-bin/performance';
}

my $extend_page = '<br><br><a href="#"></a><a href="#"></a><br><br><br>';

sub members(@) {
    my $title      = $_[1];
    my $name       = $_[2];
    my $members    = $_[3];
    my $nonmembers = $_[4];
    my $size       = 15;
    my @members    = @{$members};
    my @nonmembers = @{$nonmembers};

    my $detail = qq(
<tr>
<td class=data>
<table width=100% cellpadding=$global_cell_pad cellspacing=0 align=left border=0>
<tr>
<td class=$form_class valign=top width=$title_width>$title</td>
<td class=$form_class>
<table cellspacing=0 align=left border=0>
<tr>
<td class=$form_class align=left>
<select name=$name id=members size=$size multiple>);
    @members = sort @members;
    foreach my $mem (@members) {
	$detail .= "\n<option value=\"$mem\">$mem</option>";
    }
    $detail .= qq(
</select>
</td>
<td class=$form_class cellpadding=$global_cell_pad align=left>
<table cellspacing=0 cellpadding=3 align=center border=0>
<tr>
<td class=$form_class align=center>
<input class=submitbutton type=button value="Remove >>" onclick="delIt();">
</td>
<tr>
<td class=$form_class align=center>
<input class=submitbutton type=button value="&nbsp;&nbsp;<< Add&nbsp;&nbsp;&nbsp;&nbsp;" onclick="addIt();">
</td>
</tr>
</table>
</td>
<td class=$form_class align=left>
<select name=nonmembers id=nonmembers size=$size multiple>);
    my $got_mem = undef;
    @nonmembers = sort @nonmembers;
    foreach my $nmem (@nonmembers) {
	foreach my $mem (@members) {
	    if ( $nmem eq $mem ) { $got_mem = 1 }
	}
	if ($got_mem) {
	    $got_mem = undef;
	    next;
	}
	else {
	    $detail .= "\n<option value=\"$nmem\">$nmem</option>";
	}
    }
    $detail .= qq(
</select>
</td>
</td>
</tr>
</table>
</td>
</tr>
</table>
</td>
</tr>);
    return $detail;
}

sub hidden(@) {
    my $hidden = $_[1];
    my %hidden = %{$hidden};
    my $detail = undef;
    foreach my $key ( keys %hidden ) {
	if ( !$hidden{$key} ) { next }
	$detail .= "\n<input type=hidden name=$key value=\"$hidden{$key}\">";
    }
    return $detail;
}

sub checkbox(@) {
    my $title  = $_[1];
    my $name   = $_[2];
    my $value  = $_[3];
    my $detail = qq(
<tr>
<td class=data>
<table width=100% cellpadding=$global_cell_pad cellspacing=0 align=left border=0>
<tr>
<td class=$form_class width=$title_width>$title</td>
<td class=$form_class>);
    if ( $value == 1 ) {
	$detail .= "\n<input class=$form_class type=checkbox name=$name value=1 checked>";
    }
    else {
	$detail .= "\n<input class=$form_class type=checkbox name=$name value=1>";
    }
    $detail .= qq(
</td>
</tr>
</table>
</td>
</tr>);
    return $detail;
}

sub checkbox_left(@) {
    my $title = $_[1];
    my $name  = $_[2];
    my $value = $_[3];
    if ($value) { $value = 'checked' }
    my $detail = qq(
<tr>
<td class=data>
<table width=100% cellpadding=$global_cell_pad cellspacing=0 align=left border=0>
<tr>
<td class=$form_class width=2% align=right>);
    if ($value) {
	$detail .= "\n<input class=$form_class type=checkbox name=$name checked>";
    }
    else {
	$detail .= "\n<input class=$form_class type=checkbox name=$name>";
    }
    $detail .= qq(
</td>
<td class=$form_class>$title</td>
</tr>
</table>
</td>
</tr>);
    return $detail;
}

sub list_box_submit(@) {
    my $title    = $_[1];
    my $name     = $_[2];
    my $list     = $_[3];
    my $selected = $_[4];
    my @list     = @{$list};
    my $display  = $title;
    $display =~ s/://g;
    my $detail = qq(
<tr>
<td class=data>
<table width=100% cellpadding=$global_cell_pad cellspacing=0 align=left border=0>
<tr>
<td class=$form_class width=$title_width>$title</td>
<td class=$form_class align=left>
<select name=$name onChange="lowlight();submit()">);

    if ( !$list[0] ) {
	$detail .= "\n<option selected value=''></option>";
	$detail .= "\n<option value=''>-no \L$display" . "s-</option>";
    }
    else {
	if ($selected) {
	    $detail .= "\n<option value=''></option>";
	}
	else {
	    $detail .= "\n<option selected value=''></option>";
	}
	foreach my $item (@list) {
	    if ( $item eq $selected ) {
		$detail .= "\n<option selected value=\"$item\">$item</option>";
	    }
	    else {
		$detail .= "\n<option value=\"$item\">$item</option>";
	    }
	}
    }
    $detail .= qq(
</select>
</td>
</tr>
</table>
</td>
</tr>);
    return $detail;
}

sub hour_select(@) {
    my $hour     = $_[1];
    my $selected = $_[2] ? 'checked' : '';
    $hour = 0 if not defined $hour;
    my $detail .= qq(
<tr>
<td class=data>
<table width=100% cellpadding=$global_cell_pad cellspacing=0 align=left border=0>
<tr>
<td class=$form_class width=$title_width>Hours:</td>
<td class=$form_class align=left>
<input class=$form_class type=checkbox name=last_x_hours $selected>&nbsp;Last&nbsp;
<select name=hours>);
    for ( my $i = 1 ; $i <= 48 ; $i++ ) {
	if ( $i == $hour ) {
	    $detail .= "\n<option selected value=\"$i\">$i</option>";
	}
	else {
	    $detail .= "\n<option value=\"$i\">$i</option>";
	}
    }
    $detail .= qq(
</select>
&nbsp;hour(s)
</td>
</tr>
</table>
</td>
</tr>);
    return $detail;
}

sub day_select(@) {
    my $day      = $_[1];
    my $selected = $_[2] ? 'checked' : '';
    $day = 0 if not defined $day;
    my $detail .= qq(
<tr>
<td class=data>
<table width=100% cellpadding=$global_cell_pad cellspacing=0 align=left border=0>
<tr>
<td class=$form_class width=$title_width>Days:</td>
<td class=$form_class align=left>
<input class=$form_class type=checkbox name=last_x_days $selected>&nbsp;Last&nbsp;
<select name=days>);
    for ( my $i = 1 ; $i <= 100 ; $i++ ) {
	if ( $i == $day ) {
	    $detail .= "\n<option selected value=\"$i\">$i</option>";
	}
	else {
	    $detail .= "\n<option value=\"$i\">$i</option>";
	}
    }
    $detail .= qq(
</select>
&nbsp;day(s)
</td>
</tr>
</table>
</td>
</tr>);
    return $detail;
}

sub date_select(@) {
    my $start_date = $_[1];
    my $end_date   = $_[2];
    my $selected   = $_[3] ? 'checked' : '';
    my $detail .= qq(
<tr>
<td class=data>
<table width=100% cellpadding=$global_cell_pad cellspacing=0 align=left border=0>
<tr>
<td class=row1 valign=middle width=15%>Date range:</td>
<td class=$form_class width=3%><input class=$form_class type=checkbox name=date_range $selected></td>
<td class=row1 width=15% align=left>
<script>DateInput('start_date', false, 'YYYY-MM-DD', "$start_date")</script>
</td>
<td class=row1 width=3% align=center>&nbsp;through&nbsp;
</td>
<td class=row1><script>DateInput('end_date', false, 'YYYY-MM-DD', "$end_date")</script>
</td>
</tr>
<tr>
<td class=row1 valign=top></td>
<td class=row1 valign=top colspan=4>Note:&nbsp; The time resolution of older data may be coarser than that of recent data.&nbsp;
In the limit case, this could result in a single metric value being displayed for the entire selected time period.
</td>
</tr>
</table>
</td>
</tr>);
    return $detail;
}

sub display_hidden(@) {
    my $title   = $_[1];
    my $name    = $_[2];
    my $value   = $_[3];
    my $display = $value;
    my $detail  = qq(
<tr>
<td class=data>
<table width=100% cellpadding=$global_cell_pad cellspacing=0 align=left border=0>
<tr>
<td class=$form_class width=$title_width>$title</td>
<td class=$form_class>$display
<input type=hidden name=$name value="$value">
</td>
</tr>
</table>
</td>
</tr>);
    return $detail;
}

sub refresh_select(@) {
    my $auto_refresh = $_[1];
    my $rate         = $_[2];
    my $erroneous    = $_[3];

    my $detail = qq(
<tr>
<td class=data>
<table width=100% cellpadding=$global_cell_pad cellspacing=0 align=left border=0>
<tr>
<td class=$form_class align=left>
<input class=submitbutton type=submit name=config value="Configure">
</td>
<td class=$form_class align=right>);
    if ($auto_refresh) {
	$detail .= "\n<input class=$form_class type=checkbox name=auto_refresh checked>";
    }
    else {
	$detail .= "\n<input class=$form_class type=checkbox name=auto_refresh>";
    }
    my $rate_style = $erroneous ? 'style="background-color: #FFFF99;"' : '';
    $detail .= qq(
Auto refresh&nbsp;<input type=text size=6 $rate_style name=refresh_rate value="$rate">&nbsp;secs</td>
<td class=$form_class align=left>
<input class=submitbutton type=submit name=refresh value="Refresh">
</td>
</tr>
</table>
</td>
</tr>);
    return $detail;
}

sub refresh_save(@) {
    my $newauto_refresh = $_[1];
    my $newrate         = $_[2];
    my $erroneous       = $_[3];

    my $detail = qq(
<tr>
<td class=data>
<table width=100% cellpadding=$global_cell_pad cellspacing=0 align=left border=0>
<tr>
<td class=$form_class width=$title_width>Refresh Rate:</td>
<td class=$form_class align=left>);
    if ($newauto_refresh) {
	$detail .= "\n<input class=$form_class type=checkbox name=newauto_refresh checked>";
    }
    else {
	$detail .= "\n<input class=$form_class type=checkbox name=newauto_refresh>";
    }
    my $rate_style = $erroneous ? 'style="background-color: #FFFF99;"' : '';
    $newrate = '' if not defined $newrate;
    $detail .= qq(
&nbsp;<input type=text size=6 $rate_style name=newrefresh_rate value="$newrate">&nbsp;secs</td>
</tr>
</table>
</td>
</tr>);
    return $detail;
}

sub graph(@) {
    my $type        = $_[1];
    my $graph       = $_[2];
    my $graphs      = $_[3];
    my $refresh_url = '';
    my $now         = time;
    foreach my $g ( @{$graphs} ) {
	$g =~ s/ /+/g;
	if ( $type eq 'hosts' ) {
	    $refresh_url .= "&hosts=$g";
	}
	else {
	    $refresh_url .= "&graphs=$g";
	}
    }
    my $detail = qq(
<tr>
<td class=data>
<table width=100% cellpadding=$global_cell_pad cellspacing=0 align=left border=0>
<tr>
<td class=$form_class width=$title_width valign=top>&nbsp;
</td>
<td class=$form_class>
<img src=$doc_root_perfchart/rrd_img/$graph?time=$now border=0 alt=$graph>
</td>
</tr>
</table>
</td>
</tr>);
    return $detail;

}

sub legend(@) {
    my $legend     = $_[1];
    my %legend     = %{$legend};
    my @legend     = ();
    my $num_hosts  = 0;
    my $cell_width = '24%';
    foreach my $host ( keys %legend ) { $num_hosts++ }
    my $num_rrds   = 0;
    my $hosti      = 0;
    my $double_pad = $global_cell_pad * 2;
    foreach my $host ( sort keys %legend ) {
	$hosti++;
	my $hclass = $form_class;
	unless ( $hosti == 1 ) { $hclass = 'data2' }
	push @legend, qq(
<tr>
<td class=$hclass colspan=8 valign=top style="padding: $global_cell_pad $double_pad;">Host: $host</td>);
	my $i = 0;
	foreach my $rrd ( sort keys %{ $legend{$host} } ) {
	    if ( ($i++ % 4) == 0 ) {
		push @legend, "\n</tr>\n<tr>";
	    }
	    push @legend, qq(
<td class=$form_class width="1%" valign=top style="padding-left: $double_pad">
<div style="width:0.6em; height:0.6em; border:1px solid #000099; background-color:$legend{$host}{$rrd}; margin-top: 0.15em;"></div>
</td>
<td class=$form_class width=$cell_width valign=top>
<div style="font-size: 10px; margin-right: $double_pad;">&nbsp;$rrd</div>
</td>);
	}
	while ( ($i++ % 4) != 0 ) {
	    push @legend, qq(
<td class=$form_class width="1%" valign=top style="padding-left: $double_pad">
<div style="width:0.6em; height:0.6em; border:0px; margin-top: 0.15em;"></div>
</td>
<td class=$form_class width=$cell_width valign=top>
<div style="font-size: 10px; margin-right: $double_pad;">&nbsp;</div>
</td>);
	}
	push @legend, "\n</tr>\n<tr><td style='height: $global_cell_pad;'></td></tr>";
    }
    my $full_legend = join( '', @legend );

    my $detail = qq(
<tr>
<td class=data>
<table width=100% cellpadding=0 cellspacing=0 align=left border=0>
$full_legend
</table>
</td>
</tr>);
    return $detail;
}

sub text_box(@) {
    my $title = $_[1];
    my $name  = $_[2];
    my $value = $_[3];
    my $size  = $_[4];
    if ( !$size ) { $size = 50 }
    $value =~ s/</&lt;/g;
    $value =~ s/>/&gt;/g;
    $value =~ s/\"/&quot;/g;
    my $detail = qq(
<tr>
<td class=data>
<table width=100% cellpadding=$global_cell_pad cellspacing=0 align=left border=0>
<tr>
<td class=$form_class width=$title_width>$title</td>
<td class=$form_class>
<input type=text size=$size name=$name value="$value">
</td>
</tr>
</table>
</td>
</tr>);
    return $detail;
}

sub form_top(@) {
    my $caption = $_[1];
    my $action  = $_[2];
    my $width   = $_[3];
    my $align   = $_[4];
    if ( !$action ) { $action = "$cgi_dir/perfchart.cgi" }
    if ( !$width )  { $width  = '75%' }
    if ( !$align )  { $align  = 'left' }
    return qq(
<tr>	
<td valign=top width=80% align=left>
<table class=data width=100% cellpadding=0 cellspacing=2 border=0>
<form name=form action=$action method=post>
<tr>
<td class=data>
<table width=100% cellpadding=7 cellspacing=0 align=left border=0>
<tr>
<td class=head colspan=3>$caption</td>
</tr>
</table>
</td>
</tr>);
}

sub form_errors(@) {
    my $errors = $_[1];
    my @errors = @{$errors};
    my $errstr = undef;
    foreach my $err (@errors) {
	$errstr .= "$err<br>";
    }
    $errstr =~ s/<br>$//;
    return qq(
<tr>
<td class=data>
<table width=100% cellpadding=$global_cell_pad cellspacing=0 align=left border=0>
<tr>
<td class=error valign=top width=$title_width><b>Error(s):</b></td>
<td class=error>
Please correct the following:<br>
$errstr
</td>
</tr>
</table>
</td>
</tr>);
}

sub form_message(@) {
    my $message = $_[1];
    my @message = @{$message};
    my $msgstr  = undef;
    foreach my $msg (@message) {
	$msgstr .= "$msg<br>";
    }
    $msgstr =~ s/<br>$//;
    return qq(
<tr>
<td class=data>
<table width=100% cellpadding=$global_cell_pad cellspacing=0 align=left border=0>
<tr>
<td class=$form_class width=$title_width>Status:</td>
<td class=$form_class>
$msgstr
</td>
</tr>
</table>
</td>
</tr>);
}

# FIX MAJOR:  this is copied verbatim from MonarchAutoConfig.pm; refactor so we have only one common copy
sub js_utils() {
    return qq(
<script type="text/javascript" language=JavaScript>
    // GWMON-9658
    // use browser sniffing to determine if IE or Opera (ugly, but required)
    var isOpera = false;
    var isIE = false;
    if (typeof(window.opera) != 'undefined') {isOpera = true;}
    if (!isOpera && navigator.userAgent.indexOf('MSIE') >= 0) {isIE = true;}
    function open_window(url,name,features) {
	features = features || '';  // GWMON-10363
	if (isIE) {
	    var referLink = document.createElement('a');
	    referLink.href = url;
	    referLink.onclick = function () {
		var safe_url = location.protocol + '//' + location.host + "/portal-core/themes/groundwork/images/favicon.ico";
		window.open(safe_url,name,features);
	    }
	    referLink.target = name;
	    document.body.appendChild(referLink);
	    referLink.click();
	}
	else {
	    window.open(url,name,features);
	}
    }
</script>);
}

sub form_bottom_buttons(@) {
    my $self_discard = shift;
    my @args         = @_;
    my $type;
    my $onclick;
    my $detail = js_utils();
    $detail .= qq(
<tr>
<td>
<table width=100% cellpadding=0 cellspacing=0 border=0>
<tr>
<td style=border:0 align=left>);
    for my $button (@args) {
	if ( $button->{url} ) {
	    $type    = 'button';
	    $onclick = "open_window('$button->{url}')";
	}
	else {
	    $type    = 'submit';
	    $onclick = "this.form.clicked=this.name";
	}
	$detail .= qq(
<input class=submitbutton type=$type name=$button->{name} value="$button->{value}" onclick="$onclick">&nbsp;);
    }
    $detail .= qq(
</td>
</tr>
</form>
</table>
</td>
</tr>
</table>
</td>
</tr>);
    return $detail;
}

sub header(@) {
    my $title        = $_[1];
    my $refresh_url  = $_[2];
    my $refresh_rate = $_[3];
    my $refresh_left = $_[4];
    my $onload       = '';
    my $meta         = qq(<META HTTP-EQUIV="Expires" CONTENT="-1">);
    if ($refresh_left) {
	$onload .= "parent.perfchart_left.location='$cgi_dir/perfchart.cgi?update_left=1&refresh_left=1';";
    }
    if ($refresh_url) {
	# This meta-refresh used to be our standard mechanism for triggering a refresh after
	# a few seconds, but it no longer works because we now insist on a referrer.
	# $meta = qq(<META HTTP-EQUIV="Refresh" CONTENT="$refresh_rate; URL=$refresh_url">);
	$onload .= "setTimeout ('window.location=\\\'$refresh_url\\\'', $refresh_rate * 1000);";
    }
    my $javascript = $onload ? "onload=\"$onload\"" : '';

    my $detail = qq(
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<title>$title</title>
<META HTTP-EQUIV="CONTENT-TYPE" CONTENT="text/html; charset=iso-8859-1">
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
$meta
<link rel="stylesheet" type="text/css" href="$doc_root_perfchart/performance.css" />
<link rel="StyleSheet" href="$doc_root_perfchart/dtree.css" type="text/css" />
<script type="text/javascript" src="$doc_root_perfchart/calendarDateInput.js">

/***********************************************
* Jason's Date Input Calendar- By Jason Moon http://calendar.moonscript.com/dateinput.cfm
* Script featured on and available at http://www.dynamicdrive.com
* Keep this notice intact for use.
***********************************************/

</script>
<script type="text/javascript" src="$doc_root_perfchart/dtree.js">
</script>
<script type="text/javascript" language=JavaScript>
function lowlight() {
    document.body.style.backgroundColor = '#E6E6E6';
    document.body.style.opacity = 0.6;
}   
</script>
<style>
<!--
.tableHeader td {background-color:#55609A; color:#ffffff; font-size:8pt; font-weight:bold; width:728px}
.tableFill03 {font-weight:bold}
.header {font-weight:bold}
-->
</style>
</head>
<body bgcolor=#ffffff $javascript>
<table width=100% cellpadding=0 cellspacing=0 border=0>
<tr>);
}

sub splash() {
    return qq(
<tr>
<td>
</td>
</tr>
</table>
</td>)
      ## <h1>GroundWork Open Source, Inc.</h1>
      ## <h2>Performance</h2>
}

sub frame(@) {
    my $user_acct = $_[1];
    my $top_menu  = $_[2];
    my $url       = $_[3];
    my $now       = time;
    return qq(
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN">
<html>
<head>
<title>Performance Views</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<!-- now $now -->
</head>
<script type="text/javascript" language=JavaScript>
    var isMSIE = /*\@cc_on!@*/false;
    var frameborder = isMSIE ? '0' : '1';
    document.write('<frameset cols="25%,75%" frameborder=1 border=2 framespacing=2 bordercolor="#000000">');
    document.write('<frame name="perfchart_left" frameborder='+frameborder+' style="overflow: auto;" src="$cgi_dir/perfchart.cgi?update_left=1$url">');
    document.write('<frame name="perfchart_main" frameborder='+frameborder+' style="overflow: auto;" src="$cgi_dir/perfchart.cgi?update_main=1$url">');
    document.write('</frameset>');
</script>
</html>);
}

sub left_page(@) {
    my $views      = $_[1];
    my %views      = %{$views};
    my $now        = time;
    my $opento     = 2;
    my $javascript = qq(
	// a.add(0,-1,'&nbsp;Performance&nbsp;','','','','$doc_root_perfchart/images/console.gif','$doc_root_perfchart/images/console.gif');
	a.add(0,-1,'','','','','','');
	a.add(1,0,'&nbsp;New&nbsp;','$cgi_dir/perfchart.cgi?update_main=1&nocache=$now&view=config','','perfchart_main','$doc_root_perfchart/images/graphline.gif','$doc_root_perfchart/images/graphline.gif');
	a.add(2,0,'&nbsp;Views&nbsp;','','','','$doc_root_perfchart/images/graph.gif','$doc_root_perfchart/images/graph.gif'););
    if ( -x '/usr/local/groundwork/foundation/feeder/service_last_check_delays' ) {
	$javascript .= qq(
	    a.add(3,-1,'&nbsp;Latency&nbsp;','$cgi_dir/perfchart.cgi?update_main=1&nocache=$now&view=latency','','perfchart_main','$doc_root_perfchart/images/console.gif','$doc_root_perfchart/images/console.gif'););
    }

    my $id = 3;
    foreach my $view ( sort keys %views ) {
	if ($view) {
	    $id++;
	    my $display = $view;
	    $display =~ s/\'/\\'/g;
	    $display =~ s/\"/\\"/g;
	    $javascript .= qq(
		a.add($id,2,'&nbsp;$display&nbsp;','$cgi_dir/perfchart.cgi?update_main=1&nocache=$now&view=get_view&file=$views{$view}','','perfchart_main','$doc_root_perfchart/images/graphline.gif','$doc_root_perfchart/images/graphline.gif'););
	}
    }
    my $detail = qq(
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<title>Performance Views</title>
<META HTTP-EQUIV="CONTENT-TYPE" CONTENT="text/html; charset=iso-8859-1">
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<link rel="stylesheet" type="text/css" href="$doc_root_perfchart/performance.css" />
<link rel="StyleSheet" href="$doc_root_perfchart/dtree.css" type="text/css" />
<script type="text/javascript" src="$doc_root_perfchart/dtree.js">
</script>
<style>
<!--
.tableHeader td {background-color:#55609A; color:#ffffff; font-size:8pt; font-weight:bold; width:728px}
.tableFill03 {font-weight:bold}
.header {font-weight:bold}
-->
td.menu_head {background-color: #CCCCCC; font-family: verdana, helvetica, arial, sans-serif; font-size: 12px; font-weight: bold; padding: 7px; }
</style>
</head>
<body bgcolor=#ffffff>
<table width=100% cellpadding=0 cellspacing=2 border=0>
<tr>	
<td class=menu_head>Performance&nbsp;Views</td>
</tr>	
<tr>	
<td valign=top width=$title_width align=left>
<div class="item">
<script type="text/javascript">
a = new dTree('a');
a.config.useCookies=true;
a.config.useIcons=false;
a.config.useSelection=false;
$javascript
document.write(a);
a.openTo($opento, true);
</script>
</div>
</td>
</tr>
</table>
</body>
</html>);

    return $detail;
}

sub multiselect(@) {
    my $view             = $_[1];
    my $host_service_rrd = $_[2];
    my $host_data        = $_[3];
    my $host_selected    = $_[4];
    my $service_selected = $_[5];
    my $services         = $_[6];
    my $hidden           = $_[7];
    my %host_service_rrd = %{$host_service_rrd};
    my %host_data        = %{$host_data};
    my %hidden           = %{$hidden};
    my @services         = @{$services};
    my $now              = time;
    use URI::Escape;
    my $detail .= qq(
<tr>
<td class=data>
<table width=100% cellpadding=0 cellspacing=7 align=left border=0>
<tr>
<td width=100% align=left>
<table class=form cellspacing=0 cellpadding=3 width=100% align=left border=0>
<tr>	
<td class=row2_column_head width=30%>Host</td>
<td class=row2_column_head width=65%>Data set</td>
<td class=row2_column_head width=5%>&nbsp;</td>
</tr>);

    if (%host_data) {
	my $row = 1;
	foreach my $host ( sort keys %host_data ) {
	    my $host_val = $host;
	    $host_val = uri_escape($host_val);
	    foreach my $service ( @{ $host_data{$host} } ) {
		my $class = undef;
		my $removebutton = undef;
		if ( $row == 1 ) {
		    $class = 'row_lt';
		                $removebutton = 'removebutton_lt';
		    $row   = 2;
		}
		elsif ( $row == 2 ) {
		    $class = 'row_dk';
		                $removebutton = 'removebutton_dk';
		    $row   = 1;
		}
		delete $host_service_rrd{$host}{$service};
		my $service_val = $service;
		my $host_data   = "$host%%$service";
		$service_val = uri_escape($service_val);
		$host_data   = uri_escape($host_data);
		$detail .= qq(
<tr>	
<td class=$class valign=top>
$host
</td>
<td class=$class valign=top>
$service
</td>
<td class=$class align=center valign=top>
<input type=hidden name=host_data value=$host_data>
<input class=$removebutton type=submit name="remove_$host_data" value=" remove ">
</td>
</tr>);
	    }
	}
    }
    else {
	$detail .= qq(
<tr>	
<td class=row_lt valign=top>
&nbsp;
</td>
<td class=row_lt valign=top>
&nbsp;
</td>
<td class=row_lt align=center valign=top>
&nbsp;
</td>
</tr>);
    }

    $detail .= qq(
</table>

</td>
</tr>
<tr>
<td>

<table width=100% cellpadding=3 cellspacing=0 border=0>
<tr>
<td class=row2 align=left width=10% valign=top>Host:</td>
<td class=row2 align=left width=35% valign=top>
<select name=host onChange="lowlight();submit()">);
    unless (%host_service_rrd) {
	$detail .= "\n<option selected value=''></option>";
	$detail .= "\n<option value=''>-no hosts-</option>";
    }
    else {
	if ($host_selected) {
	    $detail .= "\n<option value=''></option>";
	}
	else {
	    $detail .= "\n<option selected value=''></option>";
	}
	foreach my $host ( sort keys %host_service_rrd ) {
	    my $host_val = $host;
	    $host_val = uri_escape($host_val);
	    if ( defined($host_selected) && $host_selected eq $host ) {
		$detail .= "\n<option selected value=\"$host_val\">$host_selected</option>";
	    }
	    else {
		$detail .= "\n<option value=\"$host_val\">$host</option>";
	    }
	}
    }
    $detail .= qq(
</select>
</td>
<td class=row2 width=35% valign=top align=left>
<select name=rrds size=10 multiple>);
    my $options = undef;
    if (defined $host_selected) {
	foreach my $service ( sort keys %{ $host_service_rrd{$host_selected} } ) {
	    my $service_val = $service;
	    $service_val = uri_escape($service_val);
	    $options .= "\n<option value=\"$service_val\">$service</option>";
	}
    }
    unless ($options) { $options = "\n<option value=''>-- select host first --</option>" }
    $detail .= qq(
$options
</select>
</td>
<td class=row2 align=left valign=middle><input class=submitbutton type=submit name=add_services value="Add Data Set(s)"></td>
</tr>
</table>

</td>
</tr>
<tr>
<td>

<table width=100% cellpadding=3 cellspacing=0 border=0>
<tr>
<td class=row2 align=left width=10% valign=top>Data&nbsp;set:</td>
<td class=row2 align=left width=35% valign=top>
<select name=rrd onChange="lowlight();submit()">);
    unless (@services) {
	$detail .= "\n<option selected value=''></option>";
	$detail .= "\n<option value=''>-no hosts-</option>";
    }
    else {
	if ($service_selected) {
	    $detail .= "\n<option value=''></option>";
	}
	else {
	    $detail .= "\n<option selected value=''></option>";
	}
	foreach my $service (@services) {
	    my $service_val = $service;
	    $service_val = uri_escape($service_val);
	    if ( defined($service_selected) && $service_selected eq $service ) {
		$detail .= "\n<option selected value=\"$service_val\">$service</option>";
	    }
	    else {
		$detail .= "\n<option value=\"$service_val\">$service</option>";
	    }
	}
    }
    $detail .= qq(
</select>
</td>
<td class=row2 width=35% valign=top align=left>
<select name=hosts size=10 multiple>);
    $options = undef;
    if (defined $service_selected) {
	foreach my $host ( sort keys %host_service_rrd ) {
	    if ( $host_service_rrd{$host}{$service_selected} ) {
		my $host_val = $host;
		$host_val = uri_escape($host_val);
		$options .= "\n<option value=\"$host_val\">$host</option>";
	    }
	}
    }
    unless ($options) { $options = "\n<option value=''>-- select data first --</option>" }
    $detail .= qq(
$options
</select>
</td>
<td class=row2 align=left valign=middle><input class=submitbutton type=submit name=add_hosts value="Add Host(s)"></td>
</tr>
</table>

</td>
</tr>
</table>
</td>
</tr>);
    return $detail;
}

sub consolidate_opts(@) {
    my $selected = $_[1];
    unless ($selected) { $selected = 'consolidated_host' }

    my $consolidated      = $selected eq 'consolidated'      ? 'checked' : '';
    my $consolidated_host = $selected eq 'consolidated_host' ? 'checked' : '';
    my $expanded          = $selected eq 'expanded'          ? 'checked' : '';

    my $detail = qq(
<tr>
<td class=data>
<table width=100% cellpadding=$global_cell_pad cellspacing=0 align=left border=0>
<tr>
<td class=$form_class align=right></td>
</tr>
<tr>
<td class=$form_class width="15%" align=left>View&nbsp;Options:</td>
<td class=$form_class width=3% align=right>
<input class=$form_class type=radio name=layout value=expanded $expanded>
</td>
<td class=$form_class>Expanded</td>
</tr>
<tr>
<td class=$form_class colspan=2 align=right>
<input class=$form_class type=radio name=layout value=consolidated_host $consolidated_host>
</td>
<td class=$form_class>Consolidated by host</td>
</tr>
<tr>
<td class=$form_class colspan=2 align=right>
<input class=$form_class type=radio name=layout value=consolidated $consolidated>
</td>
<td class=$form_class>Consolidated</td>
</tr>
<tr>
<td class=$form_class align=right></td>
</tr>
</table>
</td>
</tr>);
    return $detail;
}

sub footer() {
    return qq(
</tr>
</table>
</td>
</tr>
</table>
</td>
<td>
$extend_page
</td>
</tr>
</table>
</body>
</html>);
}

1;


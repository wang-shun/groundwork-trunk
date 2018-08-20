# perfchart - Groundwork Monitor Architect
# PerfChartForms.pm
#
###############################################################################
# Release 1.0
# 21-Sept-2005
###############################################################################
#

use strict;
package Forms;

my $is_portal = 0; # Set this value to 1 when running in the Portal
my $doc_root_perfchart = "/performance";
my $cgi_dir = '/performance/cgi-bin';
my $form_class = 'row1';
my $form_subclass = '$form_class';
my $global_cell_pad = 3;
my $title_width = '15%';
if ($is_portal) {
	$doc_root_perfchart = "/GroundWork3/monarch";
	$cgi_dir = undef;
}

my $image_dir = "$doc_root_perfchart/images";
my $download_dir = "$doc_root_perfchart/download";
sub members(@) {
	my $title = $_[1];
	my $name = $_[2];
	my $members = $_[3];
	my $nonmembers = $_[4];
	my $size = 15;
	my @members = @{$members};
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
<input class=submitbutton type=button value="&nbsp;&nbsp;<< Add&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" onclick="addIt();">
</td>
</tr>
</table>
</td>
<td class=$form_class align=left>
<select name=nonmembers id=nonmembers size=$size multiple>);
	my $got_mem = undef;
	@nonmembers = sort @nonmembers;
	foreach my $nmem (@nonmembers) {
		foreach my $mem(@members) {
			if ($nmem eq $mem) { $got_mem = 1 }
		}
		if ($got_mem) {
			$got_mem = undef;
			next;
		} else {
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
	foreach my $key (keys %hidden) {
		if (!$hidden{$key}) { next }
		$detail .= "\n<input type=hidden name=$key value=\"$hidden{$key}\">";
	}
	return $detail;
}

sub checkbox(@) {
	my $title = $_[1];
	my $name = $_[2];
	my $value = $_[3];
	my $detail = qq(
<tr>
<td class=data>
<table width=100% cellpadding=$global_cell_pad cellspacing=0 align=left border=0>
<tr>
<td class=$form_class width=$title_width>$title</td>
<td class=$form_class>);
	if ($value == 1) {
		$detail .= "\n<input class=$form_class type=checkbox name=$name value=1 checked>";
	} else {
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
	my $name = $_[2];
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
	} else {
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
	my $title = $_[1];
	my $name = $_[2];
	my $list = $_[3];
	my $selected = $_[4];
	my @list = @{$list};
	my $display = $title;
	$display =~ s/://g;
	my $detail = qq(
<tr>
<td class=data>
<table width=100% cellpadding=$global_cell_pad cellspacing=0 align=left border=0>
<tr>
<td class=$form_class width=$title_width>$title</td>
<td class=$form_class align=left>
<select name=$name onChange="submit()">);
	if (!$list[0]) { 
		$detail .= "\n<option selected value=></option>";
		$detail .= "\n<option value=>-no \L$display"."s-</option>";
	} else {
		if ($selected) {
			$detail .= "\n<option value=></option>";
		} else {
			$detail .= "\n<option selected value=></option>";
		}
		foreach my $item (@list) {
			if ($item eq $selected) {
				$detail .= "\n<option selected value=\"$item\">$item</option>";			
			} else {
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
	my $hour = $_[1];
	my $selected = $_[2];
	if ($selected) { $selected = 'checked' }
	my $detail .= qq(
<tr>
<td class=data>
<table width=100% cellpadding=$global_cell_pad cellspacing=0 align=left border=0>
<tr>
<td class=$form_class width=$title_width>Hours:</td>
<td class=$form_class align=left>
<input class=$form_class type=checkbox name=last_x_hours $selected>&nbsp;Last&nbsp;
<select name=hours>);
	for (my $i = 1; $i < 24; $i++) {
		if ($i == $hour) {
			$detail .= "\n<option selected value=\"$i\">$i</option>";	
		} else {
			$detail .= "\n<option value=\"$i\">$i</option>";	
		}
	}


	$detail .= qq(
</select>
&nbsp;hours
</td>
</tr>
</table>
</td>
</tr>);
	return $detail;
}

sub day_select(@) {
	my $day = $_[1];
	my $selected = $_[2];
	if ($selected) { $selected = 'checked' }
	my $detail .= qq(
<tr>
<td class=data>
<table width=100% cellpadding=$global_cell_pad cellspacing=0 align=left border=0>
<tr>
<td class=$form_class width=$title_width>Days:</td>
<td class=$form_class align=left>
<input class=$form_class type=checkbox name=last_x_days $selected>&nbsp;Last&nbsp;
<select name=days>);
	for (my $i = 1; $i < 100; $i++) {
		if ($i == $day) {
			$detail .= "\n<option selected value=\"$i\">$i</option>";	
		} else {
			$detail .= "\n<option value=\"$i\">$i</option>";	
		}
	}


	$detail .= qq(
</select>
&nbsp;days
</td>
</tr>
</table>
</td>
</tr>);
	return $detail;
}

sub date_select(@) {
    my $start_date = $_[1];
    my $end_date = $_[2];
    my $selected = $_[3];
    if ($selected) { $selected = 'checked' }
    my $detail .= qq(
<tr>
<td class=data>
<table width=100% cellpadding=$global_cell_pad cellspacing=0 align=left border=0>
<tr>
<td class=row1 valign=center width=15%>Date range:</td>
<td class=$form_class width=3%><input class=$form_class type=checkbox name=date_range $selected></td>
<td class=row1 width=15% align=left>
<script>DateInput('start_date', false, 'YYYY/MM/DD', "$start_date")</script>
</td>
<td class=row1 width=3% align=center>&nbsp;&nbsp;
</td>
<td class=row1><script>DateInput('end_date', false, 'YYYY/MM/DD', "$end_date")</script>
</td>
</tr>
</table>
</td>
</tr>);
    return $detail;
}



sub display_hidden(@) {
	my $title = $_[1];
	my $name = $_[2];
	my $value = $_[3];
	my $display = $value;
	my $detail = qq(
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
	my $rate = $_[2];

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
	} else {
		$detail .= "\n<input class=$form_class type=checkbox name=auto_refresh>";
	} 		
	$detail .= qq(
Auto refresh&nbsp;<input type=text size=6 name=refresh_rate value="$rate">&nbsp;secs</td>
<td class=$form_class align=left>
<input class=submitbutton type=submit name=refresh value="Set / Refresh">
</td>
</tr>
</table>
</td>
</tr>);
	return $detail;
}


sub graph(@) {
	my $type = $_[1];
	my $graph = $_[2];
	my $graphs = $_[3];
	my $refresh_url = undef;
	my $setting_url = undef;
	my $now = time;
	foreach my $g (@{$graphs}) { 
		$g =~ s/ /+/g;
		if ($type eq 'hosts') {
			$refresh_url .= "&hosts=$g";
		} else {
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
<img src=$doc_root_perfchart/rrd_img/$graph border=0>
</td>
</tr>
</table>
</td>
</tr>);
	return $detail;

}

sub legend(@) {
	my $legend = $_[1];
	my %legend = %{$legend};
	my $num_hosts = 0;
	my $legend = "\n<tr>";
	my $cell_width = '25%';
	foreach my $host (keys %legend) { $num_hosts++ }
	my $num_rrds = 0;
	my $hosti = 0;
	foreach my $host (sort keys %legend) { 
		my %cells = (
			1 => "<td class=$form_class width=$cell_width valign=top>",
			2 => "<td class=$form_class width=$cell_width valign=top>",
			3 => "<td class=$form_class width=$cell_width valign=top>",
			4 => "<td class=$form_class width=$cell_width valign=top>",
			5 => "<td class=$form_class width=$cell_width valign=top>");
		my $i = 0;
		$hosti++;
		my $hclass = $form_class;
		unless ($hosti == 1) { $hclass = 'data2' }
		$legend .= qq(
<tr>
<td class=$hclass colspan=6 valign=top>Host: $host);
		foreach my $rrd (sort keys %{$legend{$host}}) {
			$i++;
			if ($i == 1) {
				$legend .= "\n</td>\n</tr>\n<tr>\n<td class=$form_class valign=top>";
			}
			if ($i == 5) { 
				$i = 1;
			}
			$cells{$i} .= qq(
<div style="width:8px; height:8px; border:1px solid #000099; background-color:$legend{$host}{$rrd}; float: left;"></div>
<div style="float: left; font-size:10px;">&nbsp;$rrd</div><br/>);
		}
		$cells{'1'} .= "</td>";
		$cells{'2'} .= "</td>";
		$cells{'3'} .= "</td>";
		$cells{'4'} .= "</td>";
		$cells{'5'} .= "</td>";
		$legend .= $cells{'1'};
		$legend .= $cells{'2'};
		$legend .= $cells{'3'};
		$legend .= $cells{'4'};
		$legend .= $cells{'5'};	
	}	

	$legend .= "\n</tr>";

	my $detail = qq(
<tr>
<td class=data>
<table width=100% cellpadding=$global_cell_pad cellspacing=0 align=left border=0>
$legend
</table>
</td>
</tr>);
	return $detail;
}

sub text_box(@) {
	my $title = $_[1];
	my $name = $_[2];
	my $value = $_[3];
	my $size = $_[4];
	if (!$size) { $size = 50 }
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
	my $action = $_[2];
	my $width = $_[3];
	my $align = $_[4];
	if (!$action) {	$action = "$cgi_dir/perfchart.cgi" }
	if (!$width) { $width = '75%' }
	if (!$align) { $align = 'left' }
	return qq(
<tr>	
<td valign=top width=80% align=left>
<table class=data width=90% cellpadding=0 cellspacing=1 border=0>
<form name=form action=$action method=post>
<tr>
<td class=data>
<table width=100% cellpadding=5 cellspacing=0 align=left border=0>
<tr>
<td class=subhead colspan=3>$caption</td>
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
	my $msgstr = undef;
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


sub form_bottem_no_button(@) {
	return qq(
</table>
<tr>
<td>
<table width=100% cellpadding=0 cellspacing=0 border=0>
<tr>
<td style=border:0 align=left>
&nbsp;
</td>
</tr>
</form>
</table>
</td>
</table>
</td>
</tr>);
}

sub form_bottem_one_button(@) {
	my $button_one = $_[1];
	my %button_one = %{$button_one};
	return qq(
</table>
<tr>
<td>
<table width=100% cellpadding=0 cellspacing=0 border=0>
<tr>
<td style=border:0 align=left>
<input class=submitbutton type=submit name=$button_one{'name'} value="$button_one{'value'}">&nbsp;
</td>
</tr>
</form>
</table>
</td>
</table>
</td>
</tr>);
}

sub form_bottem_two_button(@) {
	my $button_one = $_[1];
	my $button_two = $_[2];
	my $colspan = $_[3];
	my %button_one = %{$button_one};
	my %button_two = %{$button_two};
	if (!$colspan) { $colspan = 3}
	return qq(
</table>
<tr>
<td>
<table width=100% cellpadding=0 cellspacing=0 border=0>
<tr>
<td style=border:0 align=left>
<input class=submitbutton type=submit name=$button_one{'name'} value="$button_one{'value'}">
&nbsp;
<input class=submitbutton type=submit name=$button_two{'name'} value="$button_two{'value'}">&nbsp;
</td>
</tr>
</form>
</table>
</td>);
}

sub form_bottem_three_button(@) {
	my $button_one = $_[1];
	my $button_two = $_[2];
	my $button_three = $_[3];
	my $colspan = $_[4];
	my %button_one = %{$button_one};
	my %button_two = %{$button_two};
	my %button_three = %{$button_three};
	if (!$colspan) { $colspan = 3}
	return qq(
</table>
<tr>
<td>
<table width=100% cellpadding=0 cellspacing=0 border=0>
<tr>
<td style=border:0 align=left>
<input class=submitbutton type=submit name=$button_one{'name'} value="$button_one{'value'}">
&nbsp;
<input class=submitbutton type=submit name=$button_two{'name'} value="$button_two{'value'}">
&nbsp;
<input class=submitbutton type=submit name=$button_three{'name'} value="$button_three{'value'}">&nbsp;
</td>
</tr>
</form>
</table>
</td>);
}

sub form_bottem_four_button(@) {
	my $button_one = $_[1];
	my $button_two = $_[2];
	my $button_three = $_[3];
	my $button_four = $_[4];
	my $colspan = $_[5];
	my %button_one = %{$button_one};
	my %button_two = %{$button_two};
	my %button_three = %{$button_three};
	my %button_four = %{$button_four};
	if (!$colspan) { $colspan = 3}
	return qq(
</table>
<tr>
<td>
<table width=100% cellpadding=0 cellspacing=0 border=0>
<tr>
<td style=border:0 align=left>
<input class=submitbutton type=submit name=$button_one{'name'} value="$button_one{'value'}">
&nbsp;
<input class=submitbutton type=submit name=$button_two{'name'} value="$button_two{'value'}">
&nbsp;
<input class=submitbutton type=submit name=$button_three{'name'} value="$button_three{'value'}">
&nbsp;
<input class=submitbutton type=submit name=$button_four{'name'} value="$button_four{'value'}">&nbsp;
</td>
</tr>
</form>
</table>
</td>);
}

sub form_bottem_five_button(@) {
	my $button_one = $_[1];
	my $button_two = $_[2];
	my $button_three = $_[3];
	my $button_four = $_[4];
	my $button_five = $_[5];
	my %button_one = %{$button_one};
	my %button_two = %{$button_two};
	my %button_three = %{$button_three};
	my %button_four = %{$button_four};
	my %button_five = %{$button_five};
	return qq(
</table>
<tr>
<td>
<table width=100% cellpadding=0 cellspacing=0 border=0>
<tr>
<td style=border:0 align=left>
<input class=submitbutton type=submit name=$button_one{'name'} value="$button_one{'value'}">
&nbsp;
<input class=submitbutton type=submit name=$button_two{'name'} value="$button_two{'value'}">
&nbsp;
<input class=submitbutton type=submit name=$button_three{'name'} value="$button_three{'value'}">
&nbsp;
<input class=submitbutton type=submit name=$button_four{'name'} value="$button_four{'value'}">
&nbsp;
<input class=submitbutton type=submit name=$button_five{'name'} value="$button_five{'value'}">&nbsp;
</td>
</tr>
</form>
</table>
</td>);
}

sub header(@) {
	my $title = $_[1];
	my $refresh_url = $_[2];
	my $refresh_rate = $_[3];
	my $refresh_left = $_[4];
	my $meta = qq(<META HTTP-EQUIV="Expires" CONTENT="-1">);
	if ($refresh_url) {
		$meta = qq(<META HTTP-EQUIV="Refresh" CONTENT="$refresh_rate; URL=$refresh_url">);
	}
	my $javascript = undef;
	if ($refresh_left) {
		$javascript = "onload=\"parent.perfchart_left.location='$cgi_dir/perfchart.cgi?update_left=1&refresh_left=1';\"";
	}

	my $detail = qq(
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<title>$title</title>
<META HTTP-EQUIV="CONTENT-TYPE" CONTENT="text/html; charset=windows-1252">
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
<style>
<!--
.tableHeader td {background-color:#55609A; color:#ffffff; font-size:8pt; font-weight:bold; width:728px}
.tableFill03 {font-weight:bold}
.header {font-weight:bold}
-->
</style>
</head>
<body bgcolor=#f0f0f0 $javascript>
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
# <h1>GroundWork Open Source Solutions</h1>
# <h2>Performance</h2>
}


sub frame(@) {
	my $user_acct = $_[1];
	my $top_menu = $_[2];
	my $url = $_[3];
	my $now = time;
	return qq(
<html>
<head>
<title>Monarch</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<!-- now $now
</head>
  <frameset cols="25%,75%" frameborder=yes border=1 framespacing=0> 
    <frame name="perfchart_left" scrolling=yes src="$cgi_dir/perfchart.cgi?update_left=1$url">
    <frame name="perfchart_main" scrolling=yes src="$cgi_dir/perfchart.cgi?update_main=1$url">
  </frameset>
</html>);
}

sub left_page(@) {
	my $views = $_[1];
	my %views = %{$views};
	my $now = time;
	my $opento = 2;
	my $javascript = qq(
a.add(0,-1,'&nbsp;Performance','javascript: void(0);','','','$doc_root_perfchart/imgaes/console.gif','$doc_root_perfchart/imgaes/console.gif');
	a.add(1,0,'&nbsp;New','$cgi_dir/perfchart.cgi?update_main=1&nocache=$now&view=config','','perfchart_main','$doc_root_perfchart/imgaes/graph.gif','$doc_root_perfchart/imgaes/graph.gif');
	a.add(2,0,'&nbsp;Views','javascript:void(0);'););	
	my $id = 2;
	foreach my $view (sort keys %views) {
		if ($view) {
			$id++;
			my $display = $view;
			$display =~ s/\'/\\'/g;
			$display =~ s/\"/\\"/g;
			$javascript .= qq(
		a.add($id,2,'&nbsp;$display','$cgi_dir/perfchart.cgi?update_main=1&nocache=$now&view=get_view&file=$views{$view}','','perfchart_main','$doc_root_perfchart/imgaes/graph.gif','$doc_root_perfchart/imgaes/graph.gif'););
		}
	}
	my $detail = qq(
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<title>Performance Views</title>
<META HTTP-EQUIV="CONTENT-TYPE" CONTENT="text/html; charset=windows-1252">
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
</style>
</head>
<body bgcolor=#f0f0f0>
<table width=100% cellpadding=0 cellspacing=0 border=0>
<tr>	
<td valign=top width=$title_width align=left>
<div class="item">
<script type="text/javascript">
a = new dTree('a');
a.config.useCookies=true;
a.config.useIcons=true;
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
	my $view = $_[1];
	my $name = $_[2];
	my $host_data = $_[3];
	my $host = $_[4];
	my $host_nonmembers = $_[5];
	my $hosts = $_[6];
	my $service = $_[7];
	my $service_nonmembers = $_[8];
	my $services = $_[9];
	my $hidden = $_[10];
	
	my %host_data = %{$host_data};
	my %hidden = %{$hidden};
	my $valstr = undef;
	foreach my $n (keys %hidden) { $valstr .= "&$n=$hidden{$n}" };
	my @hosts = @{$hosts};
	my @host_nonmembers = @{$host_nonmembers};
	my @services = @{$services};
	my @service_nonmembers = @{$service_nonmembers};
	my $now = time;
	$name =~ s/\s/+/g;
	my $hostn = $host;
	$hostn =~ s/\s/+/g;
	my $servicen = $service;
	$servicen =~ s/\s/+/g;
	my $detail .= qq(
<tr>
<td class=data>
<table width=100% cellpadding=$global_cell_pad cellspacing=0 align=left border=0>
<tr>
<td class=$form_class width=40% colspan=2 align=left>
<table class=form cellspacing=1 cellpadding=3 width=100% align=left border=0>
<tr>	
<td class=row2 width=30%>Host</td>
<td class=row2 width=65%>Data set</td>
<td class=row2 width=5%>&nbsp;</td>
</tr>);
	if (%host_data) {
		my $host_data_str = undef;
		foreach my $host (sort keys %host_data) { 
			foreach my $data (@{$host_data{$host}}) {
				$host_data_str .= "&host_data=$host+$data" 
			}
		}	
		foreach my $host (sort keys %host_data) {	
			my $hname = $host;
			$hname =~ s/\s/+/g;
			foreach my $data (@{$host_data{$host}}) {
				my $sname = $data;
				$sname =~ s/\s/+/g;
				$detail .= qq(
<tr>	
<td class=row_lt valign=top>
$host
</td>
<td class=row_lt valign=top>
$data
</td>
<td class=row_lt align=center valign=top>
<input type=hidden name=host_data value=$host+$data>
<input class=removebutton type=submit name="remove_$host+$data" value=" x ">
</td>
</tr>);		
			}
		}
	} else {	
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
</table>
</td>
</tr>
<tr>
<td class=data>
<table width=100% cellpadding=$global_cell_pad cellspacing=0 align=left border=0>
<tr>
<td class=$form_class colspan=5>
<table width=100% cellpadding=3 cellspacing=0 border=0>
<tr>
<td class=$form_class align=left width=10% valign=top>Host:
<td class=$form_class align=left width=45% valign=top>
<select name=host onChange="submit()">);
	unless (@hosts) { 
		$detail .= "\n<option selected value=></option>";
		$detail .= "\n<option value=>-no hosts-</option>";
	} else {
		if ($host) {
			$detail .= "\n<option value=></option>";
		} else {
			$detail .= "\n<option selected value=></option>";
		}
		foreach my $h (@hosts) {
			if ($host eq $h) {
				$detail .= "\n<option selected value=\"$host\">$host</option>";			
			} else {
				$detail .= "\n<option value=\"$h\">$h</option>";
			}
		}
	}
	$detail .= qq(
</select>
</td>
<td class=$form_class rowspan=3 width=45% valign=top align=left>
<select name=rrds size=10 multiple>);
	my $options = undef;
	$detail .= "\n<option selected value=></option>";
	foreach my $nmem (@host_nonmembers) {
		my $got_service = 0;
		foreach (@{$host_data{$host}}) { 
			if ($_ eq $nmem) { $got_service = 1 }
		}
		unless ($got_service) {
			$options .= "\n<option value=\"$nmem\">$nmem</option>";
		}
	}
	unless ($options) { $options = "\n<option value=>-select host-</option>" }
	$detail .= qq(
$options
</select>
</td>
</tr>
<tr>
<td class=$form_class align=left width=10% valign=top>&nbsp;
<td class=$form_class align=left width=45% valign=top><input class=submitbutton type=submit name=add_services value="Add Data Set(s)">
</td>
</tr>
<tr>
<td class=$form_class align=left width=10% valign=top>&nbsp;
<td class=$form_class align=left width=45% valign=top>&nbsp;
</td>
</tr>
</table>
</td>
</tr>
</table>
</td>
</tr>
<tr>
<td class=data>
<table width=100% cellpadding=$global_cell_pad cellspacing=0 align=left border=0>
<tr>
<tr>
<td class=$form_class colspan=5>
<table width=100% cellpadding=3 cellspacing=0 border=0>
<tr>
<td class=$form_class align=left width=10% valign=top>Data set:
<td class=$form_class align=left width=45% valign=top>
<select name=rrd onChange="submit()">);
	unless (@services) { 
		$detail .= "\n<option selected value=></option>";
		$detail .= "\n<option value=>-no hosts-</option>";
	} else {
		if ($host) {
			$detail .= "\n<option value=></option>";
		} else {
			$detail .= "\n<option selected value=></option>";
		}
		foreach my $s (@services) {
			if ($service eq $s) {
				$detail .= "\n<option selected value=\"$service\">$service</option>";			
			} else {
				$detail .= "\n<option value=\"$s\">$s</option>";
			}
		}
	}
	$detail .= qq(
</select>
</td>
<td class=$form_class rowspan=3 valign=top width=45%>
<select name=hosts size=10 multiple>);
	my $options = undef;
	$detail .= "\n<option selected value=></option>";
	foreach my $nmem (@service_nonmembers) {
		my $got_host = 0;
		foreach my $host(keys %host_data) { 
			if ($host eq $nmem) { $got_host = 1 }
		}
		unless ($got_host) {
			$options .= "\n<option value=\"$nmem\">$nmem</option>";
		}
	}
	if (!$options) { $options = "\n<option value=>-select data-</option>" }
	$detail .= qq(
$options
</select>
</td>
</tr>
<tr>
<td class=$form_class align=left width=10% valign=top>&nbsp;
<td class=$form_class align=left width=45% valign=top><input class=submitbutton type=submit name=add_hosts value="Add Host(s)">
</td>
</tr>
<tr>
<td class=$form_class align=left width=10% valign=top>&nbsp;
<td class=$form_class align=left width=45% valign=top>&nbsp;
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

sub consolidate_opts(@) {
	my $selected = $_[1];
	unless ($selected) { $selected = 'consolidate_host' }
	my %checked = ($selected => 'checked');
	
	my $detail = qq(
<tr>
<td class=data>
<table width=100% cellpadding=$global_cell_pad cellspacing=0 align=left border=0>
<tr>
<td class=$form_class colspan=2 align=left>View Options:</td>
</tr>
<tr>
<td class=$form_class width=18% align=right>
<input class=$form_class type=radio name=layout value=expand $checked{'expand'}>
</td>
<td class=$form_class>Expanded view</td>
</tr>
<tr>
<td class=$form_class width=18% align=right>
<input class=$form_class type=radio name=layout value=consolidate_host $checked{'consolidate_host'}>
</td>
<td class=$form_class>Consolidate hosts</td>
</tr>
<tr>
<td class=$form_class width=18% align=right>
<input class=$form_class type=radio name=layout value=consolidate $checked{'consolidate'}>
</td>
<td class=$form_class>Consolidated</td>
</tr>
<tr>
<td class=$form_class colspan=2 align=right>&nbsp;</td>
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
</tr>
</table>
</body>
</html>);
}


1;


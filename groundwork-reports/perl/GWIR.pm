#!/usr/local/groundwork/perl/bin/perl --
#
# Copyright 2009-2012 GroundWork Open Source, Inc. ("GroundWork")
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

use Time::Local;
use POSIX qw(ceil);
use GD::Graph::colour qw(:colours :lists :files :convert);

# These global variables are shared between this script and the 
# referenced GWIR.pm library.  Ouch.  There has to be a better way.
our %FORM_DATA;
our @components;
our $graphfile;
our $NoData;
our $start_year;
our $start_month;
our $start_day;
our $end_year;
our $end_month;
our $end_day;
our $dbh;
our @Colors;

# These global variables are shared across routines in this library.
my @sorted_components = ();
my %printdates        = ();
my @data              = ();
my @legend            = ();
my $x_label           = '';
my $y_label           = '';
my %mhgvalues         = ();
my %mhvalues          = ();
my %mhsvalues         = ();
my %values1           = ();
my %values2           = ();
my %values3           = ();
my $y_max             = 0;
my $y_min             = 0;
my $barcount          = 0;
my $stackgraphdata    = {};

sub readNagiosReportsConfig {
    my $configfile = shift;
    my $config_ref = undef;
    my @config_parms =
      qw(dbusername dbpassword dbname dbhost dbtype graphdirectory graphhtmlref nagios_cfg_file nagios_event_log dashboard_data_log dashboard_data_debug dashboard_lwp_debug nagios_server_address nagios_realm nagios_user nagios_password dashboard_lwp_log);
    open( CONFIG, '<', "$configfile" ) or die "ERROR: Unable to find configuration file $configfile";

    while ( my $line = <CONFIG> ) {
	chomp $line;
	if ( $line =~ /^\s*(\S+)\s*=\s*(.*?)\s*$/ ) {
	    my $var   = $1;
	    my $value = $2;
	    chomp $value;
	    foreach my $parm (@config_parms) {
		if ( $var eq $parm ) {
		    $config_ref->{$parm} = $value;
		}
	    }
	}
    }
    close CONFIG;
    return $config_ref;
}

sub print_top_components {
    my $name      = shift;
    my $find_comp = shift;
    my $start     = "$start_year-$start_month-$start_day";
    my $end       = "$end_year-$end_month-$end_day";
    my $query =
      "SELECT * FROM measurements where (name='$name' and timestamp>='$start' and timestamp<='$end' and component like '$find_comp:%') ";
    my $sth = $dbh->prepare($query);
    $sth->execute() or die $sth->errstr;
    my %measurement_ref = ();

    while ( my $row = $sth->fetchrow_hashref() ) {
	## $timestamp = $$row{timestamp};
	$measurement_ref{ $$row{component} } += $$row{measurement};
    }
    $sth->finish();
    @sorted_components = sort { $measurement_ref{$b} <=> $measurement_ref{$a} } keys %measurement_ref;
    my $printstring = '';

    if ( $#sorted_components > 9 ) {    # set top number in list
	$#sorted_components = 9;
    }
    my $colorcount = 1;
    foreach my $comp (@sorted_components) {
	my $tmp = '';
	if ( $comp =~ /$find_comp:\s*(.*)/ ) {
	    $tmp = $1;
	}
	else {
	    $tmp = $comp;
	}

	# $printstring .= "<tr><td>$tmp</td><td align=center>$measurement_ref{$comp}</td></tr>";
	my $styleclass = sprintf "chart%02d", $colorcount + 3;

#	$printstring .= "<tr><td class=insightcenter bgcolor=$Colors[$colorcount-1]><B>$colorcount</B></td><td class=insight>$tmp</td><td class=insightcenter>$measurement_ref{$comp}</td></tr>";
	$printstring .= "<tr><td class=$styleclass style='border: 0px none ;' align=center>$colorcount</td>";
	$printstring .= "<td class=tableFill02>$tmp</td>";
	$printstring .= "<td class=tableFill02 align=center>$measurement_ref{$comp}</td></tr>";
	$colorcount++;
    }
    return $printstring;
}

sub valid_dates {
    my @month_names = qw(January February March April May June July August September October November December);
    my %month_days = ( 1 => 31, 2 => 28, 3 => 31, 4 => 30, 5 => 31, 6 => 30, 7 => 31, 8 => 31, 9 => 30, 10 => 31, 11 => 30, 12 => 31 );
    if ( $start_day > $month_days{ $start_month + 0 } ) {
	my $year = $start_month == 2 ? " $start_year" : '';
	my $month = $month_names[ $start_month - 1 ];
	my $days = $month_days{$start_month + 0};
	## This correction works through the year 2099, which is good enough for our purposes.
	++$days if $start_month == 2 and $start_year % 4 == 0;
	print "<h3 style='color: #CC0000'>ERROR:&nbsp; $month$year has only $days days; please fix the start date.</h3>";
	return 0;
    }
    if ( $end_day > $month_days{ $end_month + 0 } ) {
	my $year = $end_month == 2 ? " $end_year" : '';
	my $month = $month_names[ $end_month - 1 ];
	my $days = $month_days{$end_month + 0};
	## This correction works through the year 2099, which is good enough for our purposes.
	++$days if $end_month == 2 and $end_year % 4 == 0;
	print "<h3 style='color: #CC0000'>ERROR:&nbsp; $month$year has only $days days; please fix the end date.</h3>";
	return 0;
    }
    return 1 if $start_year <  $end_year;
    return 1 if $start_year == $end_year && $start_month <  $end_month;
    return 1 if $start_year == $end_year && $start_month == $end_month && $start_day <= $end_day;
    print "<h3 style='color: #CC0000'>ERROR:&nbsp; The chosen start date cannot be later than the chosen end date.</h3>";
    return 0;
}

sub get_alarms_data {
    my $table_print_string = "<table class='data' border='0' cellpadding='5' cellspacing='1' width='100%'><TBODY>";    # Initialize string
    my $tmp = '';
    if ( $FORM_DATA{interval} eq "daily" ) {
	$tmp = "Day";
    }
    elsif ( $FORM_DATA{interval} eq "weekly" ) {
	$tmp = "Week";
    }
    elsif ( $FORM_DATA{interval} eq "monthly" ) {
	$tmp = "Month";
    }
    elsif ( $FORM_DATA{interval} eq "yearly" ) {
	$tmp = "Year";
    }
    $table_print_string .= "
	<tr>
	<td class=tableHeaderFlexWidth>Measurement</td>
	<td class=tableHeaderFlexWidth>Current $tmp</td>
	<td class=tableHeaderFlexWidth>Last $tmp</td>
	<td class=tableHeaderFlexWidth>Minimum</td>
	<td class=tableHeaderFlexWidth>Maximum</td>
	<td class=tableHeaderFlexWidth>Average</td>
	<td class=tableHeaderFlexWidth># of Samples</td>
	</tr>
    ";
    my ( $sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $isdst ) = localtime(time);
    # These are purposely initialized to values guaranteed to raise an exception later on if we don't assign a sensible value to them.
    my $current_div = 0;
    my $last_div    = 0;
    my $div         = 0;
    if ( $FORM_DATA{interval} eq "weekly" ) {
	$current_div = $wday + 1;    # use day of week ($wday) from current time calculation at top of cgi
	$last_div    = 7;
	$div         = 7;
    }
    elsif ( $FORM_DATA{interval} eq "monthly" ) {
	$current_div = $mday;
	$last_div = ( 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 )[ ( $mon + 1 ) % 12 ];
	$div = 365 / 12;             # use this approximation for the number of days in a month
    }
    elsif ( $FORM_DATA{interval} eq "yearly" ) {
	$current_div = $yday;
	if ( ( $year % 4 ) == 0 ) {
	    $last_div = 366;
	}
	else {
	    $last_div = 365;
	}
	$div = ( ( 365 * 3 ) + 366 ) / 4;    # use this approximation for the number of year including leap years
    }
    else {
	$current_div = 1;
	$last_div    = 1;
	$div         = 1;
    }

    my ( $current, $last, $minimum, $maximum, $average, $samplecount);
    ( $current, $last, $minimum, $maximum, $average, $samplecount, %mhgvalues ) = get_data( "nagios managed hostgroups", "all", $FORM_DATA{interval} );
    if ( $current =~ /\d+/ ) {
	$current = sprintf "%0.2f", $current / $current_div;    # use day of week ($wday) from current time calculation at top of cgi
    }
    if ( $last =~ /\d+/ ) {
	$last = sprintf "%0.2f", $last / $last_div;
    }
    if ( $minimum =~ /\d+/ ) {
	$minimum = sprintf "%0.2f", $minimum / $div;
    }
    if ( $maximum =~ /\d+/ ) {
	$maximum = sprintf "%0.2f", $maximum / $div;
    }
    if ( $average =~ /\d+/ ) {
	$average = sprintf "%0.2f", $average / $div;
    }
    $table_print_string .= "
	<tr>
	<td class=tableFill03>Managed Host Groups (per day)</td>
	<td class=tableFill03 align=center>$current</td>
	<td class=tableFill03 align=center>$last</td>
	<td class=tableFill03 align=center>$minimum</td>
	<td class=tableFill03 align=center>$maximum</td>
	<td class=tableFill03 align=center>$average</td>
	<td class=tableFill03 align=center>$samplecount</td>
	</tr>
    ";

    ( $current, $last, $minimum, $maximum, $average, $samplecount, %mhvalues ) = get_data( "nagios managed hosts", "all", $FORM_DATA{interval} );
    if ( $current =~ /\d+/ ) {
	$current = sprintf "%0.2f", $current / $current_div;    # use day of week ($wday) from current time calculation at top of cgi
    }
    if ( $last =~ /\d+/ ) {
	$last = sprintf "%0.2f", $last / $last_div;
    }
    if ( $minimum =~ /\d+/ ) {
	$minimum = sprintf "%0.2f", $minimum / $div;
    }
    if ( $maximum =~ /\d+/ ) {
	$maximum = sprintf "%0.2f", $maximum / $div;
    }
    if ( $average =~ /\d+/ ) {
	$average = sprintf "%0.2f", $average / $div;
    }
    $table_print_string .= "
	<tr>
	<td class=tableFill02>Managed Hosts (per day)</td>
	<td class=tableFill02 align=center>$current</td>
	<td class=tableFill02 align=center>$last</td>
	<td class=tableFill02 align=center>$minimum</td>
	<td class=tableFill02 align=center>$maximum</td>
	<td class=tableFill02 align=center>$average</td>
	<td class=tableFill02 align=center>$samplecount</td>
	</tr>
    ";

    ( $current, $last, $minimum, $maximum, $average, $samplecount, %mhsvalues ) = get_data( "nagios managed hostservices", "all", $FORM_DATA{interval} );
    if ( $current =~ /\d+/ ) {
	$current = sprintf "%0.2f", $current / $current_div;    # use day of week ($wday) from current time calculation at top of cgi
    }
    if ( $last =~ /\d+/ ) {
	$last = sprintf "%0.2f", $last / $last_div;
    }
    if ( $minimum =~ /\d+/ ) {
	$minimum = sprintf "%0.2f", $minimum / $div;
    }
    if ( $maximum =~ /\d+/ ) {
	$maximum = sprintf "%0.2f", $maximum / $div;
    }
    if ( $average =~ /\d+/ ) {
	$average = sprintf "%0.2f", $average / $div;
    }
    $table_print_string .= "
	<tr>
	<td class=tableFill03>Managed Host-Services (per day)</td>
	<td class=tableFill03 align=center>$current</td>
	<td class=tableFill03 align=center>$last</td>
	<td class=tableFill03 align=center>$minimum</td>
	<td class=tableFill03 align=center>$maximum</td>
	<td class=tableFill03 align=center>$average</td>
	<td class=tableFill03 align=center>$samplecount</td>
	</tr>
    ";

    my $rowclass = '';
    my $rowclass0 = "tableFill01";    # used for alternating colors for sublists
    my $rowclass1 = "tableFill02";    # used for alternating colors for sublists
    my $rowclass2 = "tableFill03";
    ( $current, $last, $minimum, $maximum, $average, $samplecount, %values1 ) = get_data( "nagios alerts", "all", $FORM_DATA{interval} );
    if ( $FORM_DATA{component} ne "all" ) {
	$rowclass = $rowclass0;
    }
    else {
	$rowclass = $rowclass1;
    }
    $table_print_string .= "
	<tr>
	<td class=$rowclass>Total Alarms</td>
	<td class=$rowclass align=center>$current</td>
	<td class=$rowclass align=center>$last</td>
	<td class=$rowclass align=center>$minimum</td>
	<td class=$rowclass align=center>$maximum</td>
	<td class=$rowclass align=center>$average</td>
	<td class=$rowclass align=center>$samplecount</td>
	</tr>
    ";

    if ( $FORM_DATA{component} ne "all" ) {
	$rowclass = $rowclass1;
	foreach my $comp ( sort @components ) {
	    my ( $current, $last, $minimum, $maximum, $average, $samplecount, %values1component ) = get_data( "nagios alerts", "$comp", $FORM_DATA{interval} );
	    %{ $stackgraphdata->{ $FORM_DATA{component} }->{$comp} } = %values1component;
	    if ( $rowclass eq $rowclass1 ) {
		$rowclass = $rowclass2;
	    }
	    else {
		$rowclass = $rowclass1;
	    }
	    $table_print_string .= "
		<tr>
		<td class='$rowclass'>$comp</td>
		<td class='$rowclass' align=center>$current</td>
		<td class='$rowclass' align=center>$last</td>
		<td class='$rowclass' align=center>$minimum</td>
		<td class='$rowclass' align=center>$maximum</td>
		<td class='$rowclass' align=center>$average</td>
		<td class='$rowclass' align=center>$samplecount</td>
		</tr>
	    ";
	}
    }
    else {
	foreach my $key ( keys %values1 ) {
	    $stackgraphdata->{"all"}->{"Total Alarms"}->{$key} = $values1{$key};
	}
    }

    ( $current, $last, $minimum, $maximum, $average, $samplecount, %values2 ) = get_data( "nagios warnings", "all", $FORM_DATA{interval} );
    if ( $FORM_DATA{component} ne "all" ) {
	$rowclass = $rowclass0;
    }
    else {
	$rowclass = $rowclass2;
    }
    $table_print_string .= "
	<tr>
	<td class=$rowclass>Total Warnings</td>
	<td class=$rowclass align=center>$current</td>
	<td class=$rowclass align=center>$last</td>
	<td class=$rowclass align=center>$minimum</td>
	<td class=$rowclass align=center>$maximum</td>
	<td class=$rowclass align=center>$average</td>
	<td class=$rowclass align=center>$samplecount</td>
	</tr>
    ";
    if ( $FORM_DATA{component} ne "all" ) {
	$rowclass = $rowclass1;
	foreach my $comp ( sort @components ) {
	    my ( $current, $last, $minimum, $maximum, $average, $samplecount, %values2component ) =
	      get_data( "nagios warnings", "$comp", $FORM_DATA{interval} );
	    if ( $rowclass eq $rowclass1 ) {
		$rowclass = $rowclass2;
	    }
	    else {
		$rowclass = $rowclass1;
	    }
	    $table_print_string .= "
		<tr>
		<td class=$rowclass>$comp</td>
		<td class=$rowclass align=center>$current</td>
		<td class=$rowclass align=center>$last</td>
		<td class=$rowclass align=center>$minimum</td>
		<td class=$rowclass align=center>$maximum</td>
		<td class=$rowclass align=center>$average</td>
		<td class=$rowclass align=center>$samplecount</td>
		</tr>
	    ";
	}
    }

    ( $current, $last, $minimum, $maximum, $average, $samplecount, %values3 ) = get_data( "nagios notifications", "all", $FORM_DATA{interval} );
    if ( $FORM_DATA{component} ne "all" ) {
	$rowclass = $rowclass0;
    }
    else {
	$rowclass = $rowclass1;
    }
    $table_print_string .= "
	<tr>
	<td class=$rowclass>Total Notifications</td>
	<td class=$rowclass align=center>$current</td>
	<td class=$rowclass align=center>$last</td>
	<td class=$rowclass align=center>$minimum</td>
	<td class=$rowclass align=center>$maximum</td>
	<td class=$rowclass align=center>$average</td>
	<td class=$rowclass align=center>$samplecount</td>
	</tr>
    ";
    if ( $FORM_DATA{component} ne "all" ) {
	$rowclass = $rowclass1;
	foreach my $comp ( sort @components ) {
	    my ( $current, $last, $minimum, $maximum, $average, $samplecount, %values3component ) =
	      get_data( "nagios notifications", "$comp", $FORM_DATA{interval} );
	    if ( $rowclass eq $rowclass1 ) {
		$rowclass = $rowclass2;
	    }
	    else {
		$rowclass = $rowclass1;
	    }
	    $table_print_string .= "
		<tr>
		<td class=$rowclass>$comp</td>
		<td class=$rowclass align=center>$current</td>
		<td class=$rowclass align=center>$last</td>
		<td class=$rowclass align=center>$minimum</td>
		<td class=$rowclass align=center>$maximum</td>
		<td class=$rowclass align=center>$average</td>
		<td class=$rowclass align=center>$samplecount</td>
		</tr>
	    ";
	}
    }
    $table_print_string .= "</tbody></table>";
    return $table_print_string;
}

sub print_alarm_trend_chart {
    ## print "<h4>DEBUG - trying to create alarm chart.</h4>";
    my @array_keys_ref    = sort keys %values1;
    my @array_values1_ref = ();
    my @array_values2_ref = ();
    my @array_values3_ref = ();
    $graphfile = "alarmcount_graph.png";
    foreach my $key (@array_keys_ref) {
	if ( $values1{$key} eq $NoData ) {
	    $values1{$key} = 0;
	}
	if ( $values2{$key} eq $NoData ) {
	    $values2{$key} = 0;
	}
	if ( $values3{$key} eq $NoData ) {
	    $values3{$key} = 0;
	}
	push @array_values1_ref, $values1{$key};
	push @array_values2_ref, $values2{$key};
	push @array_values3_ref, $values3{$key};
    }

    if ( $barcount < 60 ) {
	print_graph_bars( \@array_keys_ref, \@array_values1_ref, \@array_values2_ref, \@array_values3_ref );
    }
    else {
	print_graph_lines( \@array_keys_ref, \@array_values1_ref, \@array_values2_ref, \@array_values3_ref );
    }
    # FIX LATER:  We're using $time to avoid browser caching here.  It would better to disable caching using the proper HTTP headers.
    my $time = time;
    print "
      <table class='data' border='0' cellpadding='5' cellspacing='1' width='100%'>
	<tbody>
	  <tr class='tableHeader'>
	    <td>Trend Chart &mdash; Total Alarms, Warnings and Notifications</td>
	  </tr>
	  <tr>
	    <td class=tableFill03>
	      <table cellpadding='2' cellspacing='1'>
		<tbody>
		  <tr>
		    <td style='border: 0px none ;'><img border=0 src='$main::graphhtmlref/images/spacer.gif' alt='' height='1' width='1'></td>
		  </tr>
		  <tr>
		    <td style='border: 0px none ;' rowspan='3'><IMG border=0 src='$main::graphhtmlref/$graphfile?$time' alt='Trend Chart' hspace='20'></td>
		    <td style='border: 0px none ;' valign='bottom'><img border=0 src='$main::graphhtmlref/images/01_002.gif' alt='' height='16' hspace='5' width='17'></td>
		    <td style='border: 0px none ;' valign='bottom'>Alarms</td>
		  </tr>
		  <tr>
		    <td style='border: 0px none ;' valign='middle'><img border=0 src='$main::graphhtmlref/images/02.gif' alt='' height='16' hspace='5' width='17'></td>
		    <td style='border: 0px none ;' valign='middle'>Warnings</td>
		  </tr>
		  <tr>
		    <td style='border: 0px none ;' valign='top'><img border=0 src='$main::graphhtmlref/images/03.gif' alt='' height='16' hspace='5' width='17'></td>
		    <td style='border: 0px none ;' valign='top'>Notifications</td>
		  </tr>
		  <tr>
		    <td style='border: 0px none ;'><img border=0 src='$main::graphhtmlref/images/spacer.gif' alt='' height='1' width='1'></td>
		  </tr>
		</tbody>
	      </table>
	    </td>
	  </tr>
	</tbody>
      </table>
    ";
    return;
}

sub print_trend_chart_component {
    my $name        = shift;
    my $component   = shift;
    my $current     = undef;
    my $last        = undef;
    my $minimum     = undef;
    my $maximum     = undef;
    my $average     = undef;
    my $samplecount = undef;
    my %values      = ();
    foreach my $comp (@sorted_components) {    # @sorted_components from print_top_components routine
	## print "<br>Component = $comp";
	( $current, $last, $minimum, $maximum, $average, $samplecount, %values ) = get_data( "$name", "$comp", $FORM_DATA{interval} );
	%{ $stackgraphdata->{$component}->{$comp} } = %values;
    }
    my @array_keys_ref       = sort keys %values;
    my @print_array_keys_ref = ();
    @data                 = ();

    # push @data,\@array_keys_ref;  # push time stamps
    foreach my $key (@array_keys_ref) {
	if ( $printdates{$key} ) {
	    push @print_array_keys_ref, $printdates{$key};    # Set the date on the graph to the formatted date
	}
	else {
	    push @print_array_keys_ref, $key;
	}
    }
    push @data, \@print_array_keys_ref;                       # push time stamps

    @legend   = ();
    $barcount = $#array_keys_ref;

    my $tmparray = {};
    # foreach my $key (keys %{$stackgraphdata->{$component}}) { # ... } key=component, ie a host group, host, service
    foreach my $key (@sorted_components) {                    # key=component, ie a host group, host, service
	my $tmp = '';
	if ( $key =~ /(.*?):(.*)/ ) {
	    $tmp = $2;
	}
	else {
	    $tmp = $key;
	}
	push @legend, $tmp;
	@{ $tmparray->{$key} } = ();
	foreach my $key2 (@array_keys_ref) {                  # key2=timestamps, i.e., key of hash values
	    if ( $stackgraphdata->{$component}->{$key}->{$key2} eq $NoData ) {
		$stackgraphdata->{$component}->{$key}->{$key2} = 0;
	    }
	    push @{ $tmparray->{$key} }, $stackgraphdata->{$component}->{$key}->{$key2};
	}
	push @data, \@{ $tmparray->{$key} };
    }

    if ( $barcount < 60 ) {
	print_graph_bars_stacked();
    }
    else {
	print_graph_lines_stacked();
    }
    return;
}

sub print_hlist_notifications {
    my $component   = shift;
    my $start       = "$start_year-$start_month-$start_day";
    my $end         = "$end_year-$end_month-$end_day";
    my $contact_ref = undef;
    foreach my $name (
	( "nagios notifications CRITICAL", "nagios notifications DOWN", "nagios notifications UNREACHABLE", "nagios notifications WARNING" ) )
    {
	my $query = "SELECT * FROM measurements WHERE (name='$name' and component like '$component:%' "
	  . "and timestamp>='$start' and timestamp<='$end') ORDER BY timestamp";
	my $sth = $dbh->prepare($query);
	$sth->execute() or die $sth->errstr;
	while ( my $row = $sth->fetchrow_hashref() ) {
	    if ( $$row{component} =~ /^$component:(.*)/ ) {
		$contact_ref->{$1}->{RED}   += $$row{measurement};
		$contact_ref->{$1}->{TOTAL} += $$row{measurement};
	    }
	}
	$sth->finish();
    }

    # Stacked horizontal graphs don't seem to work, so we will put Warnings in the RED category
    # Count yellow graph items
    #	foreach my $name (("nagios notifications WARNING")) {
    #		my $query = "SELECT * FROM measurements WHERE (name='$name' ".
    #				"and timestamp>='$start' and timestamp<='$end') ORDER BY timestamp";
    #		my $sth = $dbh->prepare($query);
    #		$sth->execute() or die $sth->errstr;
    #		while (my $row=$sth->fetchrow_hashref()) {
    #			if ($$row{component} =~ /^contact:(.*)/) {
    #				$contact_ref->{$1}->{YELLOW} += $$row{measurement};
    #				$contact_ref->{$1}->{TOTAL} += $$row{measurement};
    #			}
    #		}
    #		$sth->finish();
    #	}
    # Count green graph items
    foreach my $name ( ( "nagios notifications UP", "nagios notifications OK" ) ) {
	my $query = "SELECT * FROM measurements WHERE (name='$name' and component like '$component:%' "
	  . "and timestamp>='$start' and timestamp<='$end') ORDER BY timestamp";
	my $sth = $dbh->prepare($query);
	$sth->execute() or die $sth->errstr;
	while ( my $row = $sth->fetchrow_hashref() ) {
	    if ( $$row{component} =~ /^$component:(.*)/ ) {
		$contact_ref->{$1}->{GREEN} += $$row{measurement};
		$contact_ref->{$1}->{TOTAL} += $$row{measurement};
	    }
	}
	$sth->finish();
    }
    @data              = ();
    @legend            = ();
    my @greenarray     = ();
    my @redarray       = ();
    my @yellowarray    = ();
    my @contactarray   = ();
    @sorted_components = sort { $contact_ref->{$b}->{TOTAL} <=> $contact_ref->{$a}->{TOTAL} } keys %{$contact_ref};

    #	if ($#sorted_components == 0) {
    #		return ;
    #	}

    #	push @data,\@sorted_components;  # push contact names
    #	print "<br>Contact list";
    $y_max = 0;
    $y_min = 0;
    foreach my $key (@sorted_components) {    # key=contact name

	# push @greenarray, $contact_ref->{$key}->{GREEN} * -1;  # make negative for horizontal graph
	if ( $y_min < $contact_ref->{$key}->{GREEN} ) {
	    $y_min = $contact_ref->{$key}->{GREEN};
	}
	if ( $y_max < $contact_ref->{$key}->{RED} ) {
	    $y_max = $contact_ref->{$key}->{RED};
	}
	push @greenarray, $contact_ref->{$key}->{GREEN} * -1;
	push @redarray,   $contact_ref->{$key}->{RED};

#	push @yellowarray, $contact_ref->{$key}->{YELLOW} ;
#	push @contactarray, "$key ".sprintf("%01d",$contact_ref->{$key}->{GREEN})."/".sprintf("%01d",$contact_ref->{$key}->{RED})."/".sprintf("%01d",$contact_ref->{$key}->{YELLOW}) ;
	push @contactarray,
	  "$key (" . sprintf( "%01d", $contact_ref->{$key}->{GREEN} ) . "/" . sprintf( "%01d", $contact_ref->{$key}->{RED} ) . ")";
	## print "<br>$key green=".$contact_ref->{$key}->{GREEN}." red=".$contact_ref->{$key}->{RED}." yellow=".$contact_ref->{$key}->{YELLOW};
    }
    $y_min = -1 * $y_min;
    push @data, \@contactarray;    # push contact names
    push @data, \@greenarray;

    # push @data,\@yellowarray;
    push @data, \@redarray;
    @legend  = ( "UP,OK", "DOWN,CRITICAL,UNREACHABLE,WARNING" );
    $x_label = "\u$component (green/red)";
    $y_label = "Number of Notifications";
    print_graph_hbars_stacked();
    return "OK";
}

sub print_pie_chart_component {
    my $name          = shift;
    my $component     = shift;
    my %component_ref = ();
    my $start         = "$start_year-$start_month-$start_day";
    my $end           = "$end_year-$end_month-$end_day";
    my $query =
      "SELECT * FROM measurements WHERE (name='$name' and component like '$component:%' " . "and timestamp>='$start' and timestamp<='$end') ";
    my $sth = $dbh->prepare($query);
    $sth->execute() or die $sth->errstr;

    while ( my $row = $sth->fetchrow_hashref() ) {
	if ( $$row{component} =~ /^$component:(.*)/ ) {
	    $component_ref{$1} += $$row{measurement};
	}
    }
    $sth->finish();

    @data   = ();
    @legend = ();
    my @values = ();
    @sorted_components = sort { $component_ref{$b} <=> $component_ref{$a} } keys %component_ref;
    foreach my $key (@sorted_components) {    # key=component, ie a host group, host, service
	push @legend, $key;
	push @values, $component_ref{$key};
    }
    push @data, \@sorted_components;       # push names
    push @data, \@values;

    print_pie_chart();
    return "OK";
}

sub print_detail_table {
    ## Print sample data
    print "<br>";
    print "<table class=insight>"
      . "<tr><th colspan=7 class=insight>Detail Data Table</th></tr>"
      . "<tr><th class=insight>Period</th>"
      . "<th class=insight>Managed Host Groups</th><th>Managed Hosts</th><th>Managed Host / Service</th>"
      . "<th class=insight>Alarm Count</th><th>Warnings Count</th><th>Notifications Count</th></tr>";
    foreach my $tmp ( sort keys %values1 ) {
	print "<tr class=insight><td>$tmp</td>";
	print "<td align=center class=insight>$mhgvalues{$tmp}</td>";
	print "<td align=center class=insight>$mhvalues{$tmp}</td>";
	print "<td align=center class=insight>$mhsvalues{$tmp}</td>";
	print "<td align=center class=insight>$values1{$tmp}</td>";
	print "<td align=center class=insight>$values2{$tmp}</td>";
	print "<td align=center class=insight>$values3{$tmp}</td></tr>";
    }
    print "</table>";
    return;
}

sub get_components {
    my $find_comp = shift;
    my %match     = ();
    my $query     = "SELECT component FROM measurements where component like '$find_comp:%' ";
    ## print "<br>$query\n";
    my $sth = $dbh->prepare($query);
    $sth->execute() or die $sth->errstr;
    while ( my $row = $sth->fetchrow_hashref() ) {
	$match{ $$row{component} } = 1;
    }
    $sth->finish();
    return keys %match;
}

sub get_data {
    my $name      = shift;
    my $component = shift;
    my $interval  = shift;
    my @period    = ();
    my %sums      = ();
    my $start     = "$start_year-$start_month-$start_day";
    my $end       = "$end_year-$end_month-$end_day";

    my $query = "SELECT * FROM measurements WHERE (name='$name' and component='$component' "
      . "and timestamp>='$start' and timestamp<='$end') ORDER BY timestamp";
    ## print "$query<br>";
    my $sth = $dbh->prepare($query);
    $sth->execute() or die $sth->errstr;
    ## loop and fetch the data in the called routines
    $x_label = "$start ... $end by ";
    if ( $interval eq "weekly" ) {
	%sums = get_weekly($sth);
	$x_label .= "Week";
    }
    elsif ( $interval eq "monthly" ) {
	%sums = get_monthly($sth);
	$x_label .= "Month";
    }
    elsif ( $interval eq "yearly" ) {
	%sums = get_yearly($sth);
	$x_label .= "Year";
    }
    else {    # then daily
	%sums = get_daily($sth);
	$x_label .= "Day";
    }
    $sth->finish();

    my $current     = $NoData;
    my $last        = $NoData;
    my $minimum     = undef;
    my $maximum     = undef;
    my $average     = undef;
    my $total       = 0;
    my $samplecount = 0;
    foreach my $key ( sort keys %sums ) {
	my $x    = $sums{$key};
	$last    = $current;
	$current = $x;
	if ( ( !$x ) or ( $x eq $NoData ) ) { next; }
	if ( not defined $minimum ) { $minimum = $x; }
	if ( not defined $maximum ) { $maximum = $x; }
	$total += $x;
	if ( $x < $minimum ) { $minimum = $x; }
	if ( $x > $maximum ) { $maximum = $x; }
	$samplecount++;
    }
    if ( not defined $minimum ) { $minimum = $NoData; }
    if ( not defined $maximum ) { $maximum = $NoData; }
    if ( $samplecount > 0 ) {
	$average = sprintf "%.2f", $total / $samplecount;
    }
    else {
	$average = $NoData;
    }
    return ( $current, $last, $minimum, $maximum, $average, $samplecount, %sums );
}

sub get_data_array {
    my $name_ref  = shift;
    my $component = shift;
    my $interval  = shift;
    my @period    = ();
    my %sums      = ();
    my $start     = "$start_year-$start_month-$start_day";
    my $end       = "$end_year-$end_month-$end_day";

    my $namestring = '';
    foreach my $name (@$name_ref) {
	$namestring .= "name='$name' or ";
    }
    $namestring =~ s/or $//;

    my $query = "SELECT * FROM measurements WHERE (($namestring) and component='$component' "
      . "and timestamp>='$start' and timestamp<='$end') ORDER BY timestamp";
    ## print "<br>$query\n";
    my $sth = $dbh->prepare($query);
    $sth->execute() or die $sth->errstr;
    ## loop and fetch the data in the called routines
    $x_label = "$start ... $end by ";
    if ( $interval eq "weekly" ) {
	%sums = get_weekly($sth);
	$x_label .= "Week";
    }
    elsif ( $interval eq "monthly" ) {
	%sums = get_monthly($sth);
	$x_label .= "Month";
    }
    elsif ( $interval eq "yearly" ) {
	%sums = get_yearly($sth);
	$x_label .= "Year";
    }
    else {    # then daily
	%sums = get_daily($sth);
	$x_label .= "Day";
    }
    $sth->finish();

    my $current     = $NoData;
    my $last        = $NoData;
    my $minimum     = undef;
    my $maximum     = undef;
    my $average     = undef;
    my $total       = 0;
    my $samplecount = 0;
    foreach my $key ( sort keys %sums ) {
	my $x    = $sums{$key};
	$last    = $current;
	$current = $x;
	if ( ( !$x ) or ( $x eq $NoData ) ) { next; }
	if ( not defined $minimum ) { $minimum = $x; }
	if ( not defined $maximum ) { $maximum = $x; }
	$total += $x;
	if ( $x < $minimum ) { $minimum = $x; }
	if ( $x > $maximum ) { $maximum = $x; }
	$samplecount++;
    }
    if ( not defined $minimum ) { $minimum = $NoData; }
    if ( not defined $maximum ) { $maximum = $NoData; }
    if ( $samplecount > 0 ) {
	$average = sprintf "%.2f", $total / $samplecount;
    }
    else {
	$average = $NoData;
    }
    return ( $current, $last, $minimum, $maximum, $average, $samplecount, %sums );
}

sub get_daily {
    my $sth = shift;
    my %sum_day = ();
    my $cur_year;
    my $cur_month;
    my $cur_day;
    while ( my $row = $sth->fetchrow_hashref() ) {
	my $timestamp = $$row{timestamp};
	if ( $timestamp =~ /(\d\d\d\d)-(\d\d)-(\d\d)/ ) {
	    $cur_year  = $1;
	    $cur_month = $2;
	    $cur_day   = $3;
	}
	else {
	    next;
	}
	$sum_day{$timestamp} += $$row{measurement};
    }
    $barcount = 0;
    my $start_tmp;
    my $end_tmp;
    my $start_month_tmp;
    my $end_month_tmp;
    for ( my $i = $start_year ; $i <= $end_year ; $i++ ) {
	## FIX LATER:  What's this reference to %sum_year ?
	## Did we mean %sum_day instead, with different arguments?
	## print STDERR "startyr=$start_year, endyr=$end_year, sum=$sum_year{2004}\n";
	if ( $i == $start_year ) {
	    $start_month_tmp = $start_month;
	}
	else {
	    $start_month_tmp = 1;
	}
	if ( $i == $end_year ) {
	    $end_month_tmp = $end_month;
	}
	else {
	    $end_month_tmp = 12;
	}
	for ( my $j = $start_month_tmp ; $j <= $end_month_tmp ; $j++ ) {
	    if ( ( $j == $start_month ) and ( $i == $start_year ) ) {
		$start_tmp = $start_day;
	    }
	    else {
		$start_tmp = 1;
	    }
	    if ( ( $j == $end_month ) and ( $i == $end_year ) ) {
		$end_tmp = $end_day;
	    }
	    else {
		if ( ( $i % 4 ) == 0 ) {    # Check if leap year; yes means Feb has 29 days
		    $end_tmp = ( 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 )[ $j - 1 ];
		}
		else {                      # else Feb only has 28 days
		    $end_tmp = ( 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 )[ $j - 1 ];
		}
	    }
	    for ( my $k = $start_tmp ; $k <= $end_tmp ; $k++ ) {
		## FIX LATER:  What's this reference to %sum_month ?
		## Did we mean %sum_day instead, with a different sprint() call?
		## print STDERR "Pushing $i,$j,".$sum_month{sprintf "%02d\/%04d",$j,$i}."\n";
		my $key = sprintf "%04d-%02d-%02d", $i, $j, $k;
		$printdates{$key} = sprintf "%2d/%02d", $j, $k;
		if ( !$sum_day{$key} ) {
		    $sum_day{$key} = $NoData;
		}
		$barcount++;
	    }
	}
    }
    return (%sum_day);
}

sub get_weekly {
    my $sth = shift;
    my %sum_week = ();
    my $cur_year;
    my $cur_month;
    my $cur_day;
    while ( my $row = $sth->fetchrow_hashref() ) {
	my $timestamp = $$row{timestamp};
	if ( $timestamp =~ /(\d\d\d\d)-(\d\d)-(\d\d)/ ) {
	    $cur_year  = $1;
	    $cur_month = $2;
	    $cur_day   = $3;
	    ## compute current week of the year from timestamp
	}
	else {
	    next;
	}
	my $uts = timelocal( "00", "00", "00", $cur_day, ( $cur_month - 1 ), ( $cur_year - 1900 ) );
	my ( $seconds, $minutes, $hours, $day_of_month, $month, $year, $wday, $yday, $isdst ) = localtime($uts);
	my $weekofyear = sprintf "%02d", int( ( $yday - $wday ) / 7 ) + 1;    # First week starts on Sunday
	$sum_week{"$cur_year\-w$weekofyear"} += $$row{measurement};
    }
    $barcount = 0;
    my $start_tmp;
    my $end_tmp;
    for ( my $i = $start_year ; $i <= $end_year ; $i++ ) {
	if ( $i == $start_year ) {
	    my $uts = timelocal( "00", "00", "00", $start_day, ( $start_month - 1 ), ( $start_year - 1900 ) );
	    my ( $seconds, $minutes, $hours, $day_of_month, $month, $year, $wday, $yday, $isdst ) = localtime($uts);
	    $start_tmp = int( ( $yday - $wday ) / 7 ) + 1;
	}
	else {
	    $start_tmp = 1;
	}
	if ( $i == $end_year ) {
	    my $uts = timelocal( "00", "00", "00", $end_day, ( $end_month - 1 ), ( $end_year - 1900 ) );
	    my ( $seconds, $minutes, $hours, $day_of_month, $month, $year, $wday, $yday, $isdst ) = localtime($uts);
	    $end_tmp = int( ( $yday - $wday ) / 7 ) + 1;
	}
	else {
	    $end_tmp = 52;
	}
	for ( my $j = $start_tmp ; $j <= $end_tmp ; $j++ ) {
	    my $weekofyear = sprintf "%02d", $j;
	    if ( !$sum_week{"$i\-w$weekofyear"} ) {
		$sum_week{"$i\-w$weekofyear"} = $NoData;
	    }
	    $printdates{"$i\-w$weekofyear"} = "w$weekofyear";
	    $barcount++;
	}
    }
    return (%sum_week);
}

sub get_monthly {
    my $sth = shift;
    my %sum_month = ();
    my $cur_year;
    my $cur_month;
    my $cur_day;
    while ( my $row = $sth->fetchrow_hashref() ) {
	my $timestamp = $$row{timestamp};
	if ( $timestamp =~ /(\d\d\d\d)-(\d\d)-(\d\d)/ ) {
	    $cur_year  = $1;
	    $cur_month = $2;
	    $cur_day   = $3;
	}
	else {
	    next;
	}
	$sum_month{"$cur_year\-$cur_month"} += $$row{measurement};
    }
    $barcount = 0;
    my $start_tmp;
    my $end_tmp;
    for ( my $i = $start_year ; $i <= $end_year ; $i++ ) {
	## FIX LATER:  What's this reference to %sum_year ?
	## Did we mean %sum_day instead, with different arguments?
	## print STDERR "startyr=$start_year, endyr=$end_year, sum=$sum_year{2004}\n";
	if ( $i == $start_year ) {
	    $start_tmp = $start_month;
	}
	else {
	    $start_tmp = 1;
	}
	if ( $i == $end_year ) {
	    $end_tmp = $end_month;
	}
	else {
	    $end_tmp = 12;
	}
	for ( my $j = $start_tmp ; $j <= $end_tmp ; $j++ ) {
	    ## print STDERR "Pushing $i,$j,".$sum_month{sprintf "%02d\/%04d",$j,$i}."\n";
	    ## FIX MINOR:  I see no reason for this graphtable reference -- I assume it's just obsolete code.
	    ## push @{ $graphtable[0] }, "$j\/$i";
	    my $key = sprintf "%04d\-%02d", $i, $j;
	    if ( !$sum_month{$key} ) {
		$sum_month{$key} = $NoData;
	    }
	    $printdates{$key} = qw(Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec) [ $j - 1 ];
	    $barcount++;
	}
    }
    return (%sum_month);
}

sub get_yearly {
    my $sth = shift;
    ## FIX MINOR:  Bizarre initialization -- what is this for, and why is it expressed this way?
    ## @{@graphtable} = ();
    my %sum_year = ();
    my $cur_year;
    my $cur_month;
    my $cur_day;
    while ( my $row = $sth->fetchrow_hashref() ) {
	my $timestamp = $$row{timestamp};
	if ( $timestamp =~ /(\d\d\d\d)\-(\d\d)\-(\d\d)/ ) {
	    $cur_year  = $1;
	    $cur_month = $2;
	    $cur_day   = $3;
	}
	else {
	    next;
	}
	$sum_year{$cur_year} += $$row{measurement};
    }
    $barcount = 0;
    for ( my $i = $start_year ; $i <= $end_year ; $i++ ) {
	if ( !$sum_year{$i} ) {
	    $sum_year{$i} = $NoData;
	}
	$printdates{$i} = $i;
	$barcount++;
    }
    return (%sum_year);
}

sub print_graph_bars {
    my @graphtable = @_;

    use GD::Graph::bars;

    # FIX MAJOR:  put the width back to 600, and kill the l/r margins,
    # once we have the x-values cut down to mm/dd if appropriate
    my $graph_x_size     = 620;
    my $graph_x_overhead = 130;                                          # estimated, for labels and margins
    my $my_graph         = GD::Graph::bars->new( $graph_x_size, 250 );

    # Font choices are:  Tiny, Small, MediumBold, Large, Giant.
    my $x_axis_font = GD::Font->MediumBold;
    $my_graph->set_x_label_font( GD::Font->Giant );
    $my_graph->set_y_label_font( GD::Font->Giant );
    $my_graph->set_x_axis_font($x_axis_font);
    $my_graph->set_y_axis_font( GD::Font->Giant );

    my $first_label    = $graphtable[0][0];
    my $x_labels_count = scalar @{ $graphtable[0] };
    $graph_x_overhead += 40 if $x_labels_count <= 7;    # with only a few elements, the bars don't occupy the full x-axis
    my $x_label_skip =
      ceil( ( ( length($first_label) + 1 ) * $x_axis_font->width * ( $x_labels_count - 1 ) ) / ( $graph_x_size - $graph_x_overhead ) ) || 1;
    my $x_tick_offset = ( $x_labels_count + ( $x_label_skip - 1 ) ) % $x_label_skip;

    $my_graph->set(
	'dclrs'          => [qw(#EB6232 #F3B50F #7E87B7)],
	x_label          => $x_label,
	y_label          => "Number",
	title            => "",
	y_tick_number    => 8,
	y_label_skip     => 2,
	x_label_skip     => $x_label_skip,
	x_tick_offset    => $x_tick_offset,
	x_label_position => 0.5,
	r_margin         => 20,
	bar_spacing      => 1,
	shadow_depth     => 0,
	bgclr            => "#e6e6e6",
	accent_treshold  => 200,
	transparent      => 0,
    ) or warn $my_graph->error;

    # $my_graph->set_legend('Alarms', 'Warnings','Notifications');
    my $my_image = $my_graph->plot( \@graphtable );
    eval {
	save_chart( $my_image, "$main::graphdirectory/$graphfile" );
    };
    if ($@) {
	print "Could not save chart: $@<br>";
    }
    return;
}

sub print_graph_bars_stacked {
    use GD::Graph::bars;

    my $graph_x_size     = 600;
    my $graph_x_overhead = 130;                                          # estimated, for labels and margins
    my $my_graph         = GD::Graph::bars->new( $graph_x_size, 250 );

    my $x_axis_font = GD::Font->MediumBold;
    $my_graph->set_x_label_font( GD::Font->Giant );
    $my_graph->set_y_label_font( GD::Font->Giant );
    $my_graph->set_x_axis_font($x_axis_font);
    $my_graph->set_y_axis_font( GD::Font->Giant );

    my $first_label    = $data[0][0];
    my $x_labels_count = scalar @{ $data[0] };
    my $x_label_skip =
      ceil( ( ( length($first_label) + 1 ) * $x_axis_font->width * ( $x_labels_count - 1 ) ) / ( $graph_x_size - $graph_x_overhead ) ) || 1;
    my $x_tick_offset = ( $x_labels_count + ( $x_label_skip - 1 ) ) % $x_label_skip;

    # 'dclrs'         => [ qw(lred lyellow lgreen lblue pink green cyan gold purple orange) ],

    $my_graph->set(
	'dclrs'          => [qw(#8DD9E0 #64A2B8 #D3DB00 #8BA016 #C0C0C0 #818181 #9BAEFF #6F76C4 #E092E3 #C05599)],
	x_label          => $x_label,
	y_label          => "Number",
	title            => "",
	y_tick_number    => 8,
	y_label_skip     => 2,
	x_label_skip     => $x_label_skip,
	x_tick_offset    => $x_tick_offset,
	x_label_position => 0.5,
	bar_spacing      => 1,
	shadow_depth     => 0,
	cumulate         => 2,
	accent_treshold  => 200,
	transparent      => 0,
	bgclr            => "#e6e6e6",
	borderclrs       => undef,
    ) or warn $my_graph->error;

    # $my_graph->set_legend(@legend);
    my $my_image = $my_graph->plot( \@data );
    eval {
	save_chart( $my_image, "$main::graphdirectory/$graphfile" );
    };
    if ($@) {
	print "Could not save chart: $@<br>";
    }
    return;
}

sub print_graph_lines {
    my @graphtable = @_;

    use GD::Graph::lines;

    # FIX MAJOR:  put the width back to 600, and kill the l/r margins,
    # once we have the x-values cut down to mm/dd if appropriate
    my $graph_x_size     = 650;
    my $graph_x_overhead = 130;                                           # estimated, for labels and margins
    my $my_graph         = GD::Graph::lines->new( $graph_x_size, 250 );

    my $x_axis_font = GD::Font->MediumBold;
    $my_graph->set_x_label_font( GD::Font->Giant );
    $my_graph->set_y_label_font( GD::Font->Giant );
    $my_graph->set_x_axis_font($x_axis_font);
    $my_graph->set_y_axis_font( GD::Font->Giant );
    $my_graph->set_legend_font(GD::gdMediumBoldFont);

    my $first_label    = $graphtable[0][0];
    my $x_labels_count = scalar @{ $graphtable[0] };
    my $x_label_skip =
      ceil( ( ( length($first_label) + 1 ) * $x_axis_font->width * ( $x_labels_count - 1 ) ) / ( $graph_x_size - $graph_x_overhead ) ) || 1;
    my $x_tick_offset = ( $x_labels_count + ( $x_label_skip - 1 ) ) % $x_label_skip;

    $my_graph->set(
	'dclrs' => [qw(#EB6232 #F3B50F #7E87B7)],
	x_label => $x_label,
	y_label => "Number",
	title   => "",
	## y_max_value      => 40,
	y_tick_number    => 8,
	y_label_skip     => 2,
	x_label_skip     => $x_label_skip,
	x_tick_offset    => $x_tick_offset,
	x_label_position => 0.5,
	l_margin         => 10,
	r_margin         => 40,
	box_axis         => 1,
	line_width       => 2,
	bgclr            => "#e6e6e6",
	transparent      => 0,
    );

    $my_graph->set_legend( 'Alarms', 'Warnings', 'Notifications' );
    my $my_image = $my_graph->plot( \@graphtable );
    eval {
	save_chart( $my_image, "$main::graphdirectory/$graphfile" );
    };
    if ($@) {
	print "Could not save chart: $@<br>";
    }
    return;
}

sub print_graph_lines_stacked {
    use GD::Graph::area;

    # FIX MAJOR:  put the width back to 600, and kill the l/r margins,
    # once we have the x-values cut down to mm/dd if appropriate
    my $graph_x_size     = 650;
    my $graph_x_overhead = 130;                                          # estimated, for labels and margins
    my $my_graph         = GD::Graph::area->new( $graph_x_size, 250 );

    my $x_axis_font = GD::Font->MediumBold;
    $my_graph->set_x_label_font( GD::Font->Giant );
    $my_graph->set_y_label_font( GD::Font->Giant );
    $my_graph->set_x_axis_font($x_axis_font);
    $my_graph->set_y_axis_font( GD::Font->Giant );

    my $first_label    = $data[0][0];
    my $x_labels_count = scalar @{ $data[0] };
    my $x_label_skip =
      ceil( ( ( length($first_label) + 1 ) * $x_axis_font->width * ( $x_labels_count - 1 ) ) / ( $graph_x_size - $graph_x_overhead ) ) || 1;
    my $x_tick_offset = ( $x_labels_count + ( $x_label_skip - 1 ) ) % $x_label_skip;

    $my_graph->set(
	'dclrs'          => [qw(#8DD9E0 #64A2B8 #D3DB00 #8BA016 #C0C0C0 #818181 #9BAEFF #6F76C4 #E092E3 #C05599)],
	x_label          => $x_label,
	y_label          => "Number",
	title            => "",
	y_tick_number    => 8,
	y_label_skip     => 2,
	x_label_skip     => $x_label_skip,
	x_tick_offset    => $x_tick_offset,
	x_label_position => 0.5,
	l_margin         => 10,
	r_margin         => 40,
	bar_spacing      => 1,
	shadow_depth     => 0,
	cumulate         => 2,
	accent_treshold  => 200,
	transparent      => 0,
	bgclr            => "#e6e6e6",
	borderclrs       => undef,
	line_width       => 2,
    ) or warn $my_graph->error;

    # $my_graph->set_legend(@legend);
    my $my_image = $my_graph->plot( \@data );
    eval {
	save_chart( $my_image, "$main::graphdirectory/$graphfile" );
    };
    if ($@) {
	print "Could not save chart: $@<br>";
    }
    return;
}

sub print_graph_hbars_stacked {
    use GD::Graph::bars;
    use GD::Graph::hbars;

    my $vector_count     = scalar @{ $data[0] };
    my $y_interval_count = $vector_count + 1;

    # x-axis and y-axis are swapped internally in this type of graph, so the
    # use of these variables below is actually correct from the outside view.
    my $x_axis_font        = GD::Font->MediumBold;
    my $y_axis_font        = GD::Font->MediumBold;
    my $y_axis_line_height = $y_axis_font->height + 1;    # allow some line leading

    my $graph_x_size     = 620;
    my $graph_x_overhead = 150;                           # estimated, for labels and margins
    my $graph_y_size     = 270;
    my $graph_y_overhead = 65;                            # estimated, for labels and margins

    if ( $graph_y_size < ( $y_interval_count * $y_axis_line_height ) + $graph_y_overhead ) {
	$graph_y_size = ( $y_interval_count * $y_axis_line_height ) + $graph_y_overhead;
    }

    my $my_graph = GD::Graph::hbars->new( $graph_x_size, $graph_y_size );

    $my_graph->set_x_label_font( GD::Font->Giant );
    $my_graph->set_y_label_font( GD::Font->Giant );
    $my_graph->set_x_axis_font($y_axis_font);
    $my_graph->set_y_axis_font($x_axis_font);
    $my_graph->set_legend_font(GD::gdMediumBoldFont);

    my $x_tick_number = 10;

    # In this graph, we have auto-generated x-axis labels, so we don't draw them from @data.
    # Logically, we would want to scan the entire array of data values to find the largest to use as $first_label.
    # But for the time being, we just punt and take a reasonable guess for that.
    # my $first_label = $data[0][0];
    # my $x_labels_count = scalar @{ $data[0] };
    my $first_label    = '100';
    my $x_labels_count = $x_tick_number + 1;
    my $x_label_skip =
      ceil( ( ( length($first_label) + 1 ) * $x_axis_font->width * ( $x_labels_count - 1 ) ) / ( $graph_x_size - $graph_x_overhead ) ) || 1;

    # my $x_tick_offset = ($x_labels_count + ($x_label_skip - 1)) % $x_label_skip;

    # 'dclrs'       => [qw(#8DD9E0 #C05599)],
    # 'dclrs'       => [ qw(lgreen lred) ],

    $my_graph->set(
	'dclrs'       => [qw(#77EE77 #EE7777)],
	x_label       => $x_label,
	y_label       => $y_label,
	title         => "",
	y_tick_number => $x_tick_number,
	y_label_skip  => $x_label_skip,

	# y_tick_offset    => $x_tick_offset,  # there is no y_tick_offset option available, so we don't bother to set it here
	x_label_skip     => 1,
	x_label_position => 0.5,
	overwrite        => 1,
	cumulate         => 0,
	axislabelclr     => 'black',
	legend_placement => 'CT',
	zero_axis_only   => 0,
	bgclr            => "#e6e6e6",
	transparent      => 0,
    ) or warn $my_graph->error;

    #	y_max_value     => $y_max,
    #	y_min_value     => $y_min,
    #	y_number_format => \&y_format,

    #	bar_spacing     => 1,
    #	shadow_depth    => 0,
    #	cumulate        => 2,
    #	accent_treshold => 200,
    #	transparent     => 0,
    #	borderclrs      => undef,

    $my_graph->set_legend(@legend);
    my $my_image = $my_graph->plot( \@data );
    eval {
	save_chart( $my_image, "$main::graphdirectory/$graphfile" );
    };
    if ($@) {
	print "Could not save chart: $@<br>";
    }
    return;
}

sub print_pie_chart {
    use GD::Graph::pie;

    my $my_graph = new GD::Graph::pie( 450, 250 );

    $my_graph->set_value_font(GD::gdSmallFont);
    $my_graph->set(
	'dclrs'      => [qw(#8DD9E0 #64A2B8 #D3DB00 #8BA016 #C0C0C0 #818181 #9BAEFF #6F76C4 #E092E3 #C05599)],
	title        => '',
	label        => '',
	axislabelclr => 'black',
	pie_height   => 30,
	l_margin     => 100,
	r_margin     => 100,
	start_angle  => 195,
	bgclr        => "#e6e6e6",
	transparent  => 0,
    );

    my $my_image = $my_graph->plot( \@data );
    eval {
	save_chart( $my_image, "$main::graphdirectory/$graphfile" );
    };
    if ($@) {
	print "Could not save chart: $@<br>";
    }
    return;
}

sub save_chart {
    my $chart = shift or die "Need a chart!";
    my $name  = shift or die "Need a name!";
    local (*OUT);
    my $ext = "png";
    open( OUT, '>', $name ) or do {
	print "Cannot open $name for write: $!";
	die "Cannot open $name for write: $!";
    };
    binmode OUT;
    print OUT $chart->$ext();
    close OUT;
}

1;

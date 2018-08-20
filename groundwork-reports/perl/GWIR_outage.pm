#!/usr/local/groundwork/perl/bin/perl --
#
# Copyright 2007-2012 GroundWork Open Source, Inc. ("GroundWork")
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

# These global variables are shared between this script and the 
# referenced GWIR.pm library.  Ouch.  There has to be a better way.
our %FORM_DATA;
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
my @data              = ();
my @legend            = ();
my $x_label           = '';
my $barcount          = 0;
my $tmpparm           = '';
my $sum_ref           = undef;    # This is also the name of several local variables; don't get confused!

sub readNagiosReportsConfig {
    my $configfile   = shift;
    my $config_ref   = undef;
    my @config_parms = qw(dbusername dbpassword dbname dbhost dbtype graphdirectory graphhtmlref
      nagios_cfg_file nagios_event_log dashboard_data_log dashboard_data_debug
      dashboard_lwp_debug nagios_server_address nagios_realm nagios_user nagios_password
      dashboard_lwp_log
    );
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

sub get_alarm_data {
    my $host_outage_parameter    = shift;
    my $service_outage_parameter = shift;
    my $outage_parameter         = '';
    my $comp_ref                 = undef;

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
    if ( $FORM_DATA{component} eq "all" ) {
	$outage_parameter = $host_outage_parameter;
    }
    elsif ( $FORM_DATA{component} eq "hostgroup" ) {
	$outage_parameter = $host_outage_parameter;
    }
    elsif ( $FORM_DATA{component} eq "host" ) {
	$outage_parameter = $host_outage_parameter;
    }
    elsif ( $FORM_DATA{component} eq "hostservice" ) {
	$outage_parameter = $service_outage_parameter;
    }
    elsif ( $FORM_DATA{component} eq "service" ) {
	$outage_parameter = $service_outage_parameter;
    }
    $tmpparm = $outage_parameter;
    $tmpparm =~ s/_/ /g;
    $table_print_string .= "
	<tr>
	<td class=tableHeaderFlexWidth> Measurement &mdash; $tmpparm</td>
	<td class=tableHeaderFlexWidth> Current $tmp</td>
	<td class=tableHeaderFlexWidth> Last $tmp</td>
	<td class=tableHeaderFlexWidth> Minimum</td>
	<td class=tableHeaderFlexWidth> Maximum</td>
	<td class=tableHeaderFlexWidth> Average</td>
	<td class=tableHeaderFlexWidth> # of Samples</td>
	</tr>
    ";

    ( $sum_ref, $comp_ref ) = get_outage_data( $outage_parameter, $FORM_DATA{component}, $FORM_DATA{interval} );
    my $rowclass = '';
    my $rowclass0 = "tableFill01";    # used for alternating colors for sublists
    my $rowclass1 = "tableFill02";    # used for alternating colors for sublists
    my $rowclass2 = "tableFill03";
    if ( $FORM_DATA{component} ne "all" ) {
	$rowclass = $rowclass0;
    }
    else {
	$rowclass = $rowclass2;
    }

    # &#8288; is a zero-width no-break space (also known as "word joiner").  It
    # won't show up on-screen, but its use below guarantees that the table-cell
    # box formatting will appear if the cell content is otherwise empty.
    my $zwnbsp = '&#8288;';
    my $current = format_output_number( $outage_parameter, $comp_ref->{ALL}->{CURRENT} );
    my $last    = format_output_number( $outage_parameter, $comp_ref->{ALL}->{LAST} );
    my $min     = format_output_number( $outage_parameter, $comp_ref->{ALL}->{MIN} );
    my $max     = format_output_number( $outage_parameter, $comp_ref->{ALL}->{MAX} );
    my $avg     = format_output_number( $outage_parameter, $comp_ref->{ALL}->{AVERAGE} );
    my $samplecount = format_output_number( "SAMPLECOUNT", $comp_ref->{ALL}->{SAMPLECOUNT} || 0 );
    $table_print_string .= "
	<tr>
	<td class=$rowclass>Total Outages</td>
	<td class=$rowclass align=center>$current$zwnbsp</td>
	<td class=$rowclass align=center>$last$zwnbsp</td>
	<td class=$rowclass align=center>$min$zwnbsp</td>
	<td class=$rowclass align=center>$max$zwnbsp</td>
	<td class=$rowclass align=center>$avg$zwnbsp</td>
	<td class=$rowclass align=center>$samplecount</td>
	</tr>
    ";

    if ( $FORM_DATA{component} ne "all" ) {
	$rowclass = $rowclass1;
	foreach my $comp ( sort keys %{$comp_ref} ) {
	    if ( $comp eq "ALL" ) { next; }    # ALL is already printed
	    if ( $rowclass eq $rowclass1 ) {
		$rowclass = $rowclass2;
	    }
	    else {
		$rowclass = $rowclass1;
	    }
	    my $current = format_output_number( $outage_parameter, $comp_ref->{$comp}->{CURRENT} );
	    my $last    = format_output_number( $outage_parameter, $comp_ref->{$comp}->{LAST} );
	    my $min     = format_output_number( $outage_parameter, $comp_ref->{$comp}->{MIN} );
	    my $max     = format_output_number( $outage_parameter, $comp_ref->{$comp}->{MAX} );
	    my $avg     = format_output_number( $outage_parameter, $comp_ref->{$comp}->{VALUE} );
	    my $samplecount = format_output_number( "SAMPLECOUNT", $comp_ref->{$comp}->{SAMPLECOUNT} || 0 );
	    $table_print_string .= "
		<tr>
		<td class=$rowclass>$comp</td>
		<td class='$rowclass' align=center>$current$zwnbsp</td>
		<td class='$rowclass' align=center>$last$zwnbsp</td>
		<td class='$rowclass' align=center>$min$zwnbsp</td>
		<td class='$rowclass' align=center>$max$zwnbsp</td>
		<td class='$rowclass' align=center>$avg$zwnbsp</td>
		<td class='$rowclass' align=center>$samplecount</td>
		</tr>
	    ";
	}
    }
    $table_print_string .= "</tbody></table>";
    return $table_print_string;
}

sub print_outage_trend_chart {
    ## my @array_keys_ref = sort keys %values1;
    my @array_keys_ref = sort keys %{$sum_ref};

    my @array_values1_ref = ();
    $graphfile         = "outagecount_graph.png";
    print "<table class='data' border='0' cellpadding='5' cellspacing='1' width='100%'>";
    print "<tbody><tr class='tableHeader'>";
    print "<td>Trend Chart &mdash; Total $tmpparm</td></tr>";
    print "<tr><td class=tableFill03 > ";
    print "<table cellpadding='5' cellspacing='1' >";
    my $tmpsum = 0;
    foreach my $key (@array_keys_ref) {
	push @array_values1_ref, $sum_ref->{$key}->{ALL}->{VALUE};
	$tmpsum += $sum_ref->{$key}->{ALL}->{VALUE} if $sum_ref->{$key}->{ALL}->{VALUE} ne $NoData;
    }
    if ($tmpsum) {
	if ( $barcount < 60 ) {
	    print_graph_bars( \@array_keys_ref, \@array_values1_ref );
	}
	else {
	    print_graph_lines( \@array_keys_ref, \@array_values1_ref );
	}
	my $time = time;
	print "
	  <tbody>
	    <tr><td style='border: 0px none ;'><img border=0 src='$main::graphhtmlref/images/spacer.gif' alt='' height='1' width='1'></td></tr>
	    <tr>
	      <td style='border: 0px none ;' ><IMG border=0 src='$main::graphhtmlref/$graphfile?$time' alt='Trend Chart' hspace='20'></td>
	      <td style='border: 0px none ;' valign='middle'><img border=0 src='$main::graphhtmlref/images/01_002.gif' alt='' height='16' hspace='5' width='17'></td>
	      <td style='border: 0px none ;' valign='middle'>Outage Measurement</td>
	    </tr>
	    <tr><td style='border: 0px none ;'><img border=0 src='$main::graphhtmlref/images/spacer.gif' alt='' height='1' width='1'></td></tr>
	  </tbody>
	";
    }
    else {
	print "<tr><td class=tableFill03  style='border: 0px none ;'>No Data Found.</td></tr>";
    }
    print "</table></td></tr></tbody></table>";
    return;
}

sub print_trend_chart_component {
    my @array_keys_ref       = sort keys %{$sum_ref};
    my @print_array_keys_ref = ();
    @data = ();
    foreach my $key (@array_keys_ref) {
	if ( $sum_ref->{$key}->{ALL}->{PRINTDATES} ) {
	    push @print_array_keys_ref, $sum_ref->{$key}->{ALL}->{PRINTDATES};    # Set the date on the graph to the formated date
	}
	else {
	    push @print_array_keys_ref, $key;
	}
    }

    push @data, \@print_array_keys_ref;                                           # push time stamps

    my $tmparray = undef;
    @legend   = ();
    $barcount = $#array_keys_ref;
    foreach my $key (@sorted_components) {                                        # key=component, ie a host group, host, service
	## push @legend, $key;
	@{ $tmparray->{$key} } = ();
	foreach my $key2 (@array_keys_ref) {                                      # key2=timestamps, i.e., key of hash values
	    if ( $sum_ref->{$key2}->{$key}->{VALUE} eq $NoData ) {
		$sum_ref->{$key2}->{$key}->{VALUE} = 0;
	    }
	    push @{ $tmparray->{$key} }, $sum_ref->{$key2}->{$key}->{VALUE};
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

sub get_outage_data {
    my $name      = shift;
    my $component = shift;
    my $interval  = shift;
    my @period    = ();
    my @values    = ();
    my $sum_ref   = undef;
    my $comp_ref  = undef;
    my $start     = "$start_year-$start_month-$start_day";
    my $end       = "$end_year-$end_month-$end_day";
    my $query     = '';

    if ( $component eq "all" ) {
	$query = "SELECT * FROM host_availability WHERE (DATESTAMP>='$start' and DATESTAMP<='$end') ORDER BY DATESTAMP";
    }
    elsif ( $component eq "hostgroup" ) {
	$query = "SELECT * FROM hostgroup_host_availability WHERE (DATESTAMP>='$start' and DATESTAMP<='$end') ORDER BY DATESTAMP";
    }
    elsif ( $component eq "host" ) {
	$query = "SELECT * FROM host_availability WHERE (DATESTAMP>='$start' and DATESTAMP<='$end') ORDER BY DATESTAMP";
    }
    elsif ( $component eq "hostservice" ) {
	$query = "SELECT * FROM service_availability WHERE (DATESTAMP>='$start' and DATESTAMP<='$end') ORDER BY DATESTAMP";
    }
    elsif ( $component eq "service" ) {
	$query = "SELECT * FROM service_availability WHERE (DATESTAMP>='$start' and DATESTAMP<='$end') ORDER BY DATESTAMP";
    }
    ## print "$query<br>";
    my $sth = $dbh->prepare($query);
    $sth->execute() or die $@;
    $x_label = "$start ... $end by ";
    if ( $interval eq "weekly" ) {
	( $sum_ref, $comp_ref ) = get_weekly2( $sth, $component, $name );
	$x_label .= "Week";
    }
    elsif ( $interval eq "monthly" ) {
	( $sum_ref, $comp_ref ) = get_monthly2( $sth, $component, $name );
	$x_label .= "Month";
    }
    elsif ( $interval eq "yearly" ) {
	( $sum_ref, $comp_ref ) = get_yearly2( $sth, $component, $name );
	$x_label .= "Year";
    }
    else {    # then daily
	( $sum_ref, $comp_ref ) = get_daily2( $sth, $component, $name );
	$x_label .= "Day";
    }
    $sth->finish();

    foreach my $comp ( keys %{$comp_ref} ) {
	my @tmp_sorted = sort keys %$sum_ref;    # sort by time
	$comp_ref->{$comp}->{CURRENT} =
	  $sum_ref->{ $tmp_sorted[$#tmp_sorted] }->{$comp}->{VALUE};    # current is the last item in the sorted array
	$comp_ref->{$comp}->{LAST} =
	  $sum_ref->{ $tmp_sorted[ $#tmp_sorted - 1 ] }->{$comp}->{VALUE};    # last is the next to last item in the sorted array
	my @tmpvalues   = ();
	my $total       = 0;
	my $samplecount = 0;
	foreach my $interval ( sort keys %$sum_ref ) {
	    if ( $sum_ref->{$interval}->{$comp}->{VALUE} eq $NoData ) {
		next;
	    }
	    else {
		$total += $sum_ref->{$interval}->{$comp}->{VALUE};
		$samplecount++;
		push @tmpvalues, $sum_ref->{$interval}->{$comp}->{VALUE};     # use to calc min and max. Only include valid data here,
	    }
	}
	@tmp_sorted = sort { $a <=> $b } @tmpvalues;                          # sort by hash value, ascending
	$comp_ref->{$comp}->{MAX} = $tmp_sorted[$#tmp_sorted];
	$comp_ref->{$comp}->{MIN} = $tmp_sorted[0];
	if ( $samplecount > 0 ) {
	    $comp_ref->{$comp}->{AVERAGE} = $total / $samplecount;
	}
	else {
	    $comp_ref->{$comp}->{AVERAGE} = $NoData;
	}
	$comp_ref->{$comp}->{SAMPLECOUNT} = $samplecount;
    }
    return ( \%{$sum_ref}, \%{$comp_ref} );
}

#($sum_ref,$comp_ref) = get_outage_data($outage_parameter,$FORM_DATA{component},$FORM_DATA{interval});
sub print_top_components {
    my $component = shift;
    my $name      = shift;
    my $interval  = shift;
    my $comp_ref  = undef;
    my $start     = "$start_year-$start_month-$start_day";
    my $end       = "$end_year-$end_month-$end_day";
    ( $sum_ref, $comp_ref ) = get_outage_data( $name, $component, $FORM_DATA{interval} );
    foreach my $key ( keys %{$comp_ref} ) {
	if ( $key eq "ALL" ) {
	    delete( $comp_ref->{$key} );
	}
    }
    if ( $name =~ /(_UP|_OK)/ ) {    # is looking at OK or UP, sort ascending. Else sort descending
	@sorted_components = sort { $comp_ref->{$a}->{VALUE} <=> $comp_ref->{$b}->{VALUE} } keys %{$comp_ref};
    }
    else {
	@sorted_components = sort { $comp_ref->{$b}->{VALUE} <=> $comp_ref->{$a}->{VALUE} } keys %{$comp_ref};
    }
    if ( $#sorted_components > 9 ) {    # set top number in list
	$#sorted_components = 9;
    }
    my $colorcount = 1;
    my $output_string = '';
    foreach my $key (@sorted_components) {
	my $styleclass = sprintf "chart%02d", $colorcount + 3;
	$output_string .= "<tr><td class=$styleclass style='border: 0px none ;' align=center>$colorcount</td>";
	$output_string .= "<td class=tableFill02>$key</td>";
	$output_string .= "<td class=tableFill02 align=center>" . format_output_number( $name, $comp_ref->{$key}->{VALUE} ) . "</td></tr>";
	$colorcount++;
    }
    return $output_string;
}

sub print_top_components_save {
    my $component = shift;
    my $name      = shift;
    my $start     = "$start_year-$start_month-$start_day";
    my $end       = "$end_year-$end_month-$end_day";
    my $query     = '';

    if ( $component eq "all" ) {
	$query = "SELECT * FROM host_availability WHERE (DATESTAMP>='$start' and DATESTAMP<='$end') ";
    }
    elsif ( $component eq "hostgroup" ) {
	$query = "SELECT * FROM hostgroup_host_availability WHERE (DATESTAMP>='$start' and DATESTAMP<='$end') ";
    }
    elsif ( $component eq "host" ) {
	$query = "SELECT * FROM host_availability WHERE (DATESTAMP>='$start' and DATESTAMP<='$end') ";
    }
    elsif ( $component eq "hostservice" ) {
	$query = "SELECT * FROM service_availability WHERE (DATESTAMP>='$start' and DATESTAMP<='$end') ";
    }
    elsif ( $component eq "service" ) {
	$query = "SELECT * FROM service_availability WHERE (DATESTAMP>='$start' and DATESTAMP<='$end') ";
    }
    my $sth = $dbh->prepare($query);
    $sth->execute() or die $@;
    my %measurement_ref   = ();
    my %measurement_count = ();
    while ( my $row = $sth->fetchrow_hashref('NAME_uc') ) {
	## print "<br>$component=$$row{HOST_NAME},$name=$$row{$name}";
	if ( $component eq "all" ) {
	    $measurement_ref{ $$row{HOST_NAME} } += $$row{$name};
	    $measurement_count{ $$row{HOST_NAME} }++;
	}
	elsif ( $component eq "hostgroup" ) {
	    $measurement_ref{ $$row{HOSTGROUP_NAME} } += $$row{$name};
	    $measurement_count{ $$row{HOSTGROUP_NAME} }++;
	}
	elsif ( $component eq "host" ) {
	    $measurement_ref{ $$row{HOST_NAME} } += $$row{$name};
	    $measurement_count{ $$row{HOST_NAME} }++;
	}
	elsif ( $component eq "hostservice" ) {
	    $measurement_ref{ $$row{HOST_NAME} . ":" . $$row{SERVICE_NAME} } += $$row{$name};
	    $measurement_count{ $$row{HOST_NAME} . ":" . $$row{SERVICE_NAME} }++;
	}
	elsif ( $component eq "service" ) {
	    $measurement_ref{ $$row{SERVICE_NAME} } += $$row{$name};
	    $measurement_count{ $$row{SERVICE_NAME} }++;
	}
    }
    $sth->finish();
    foreach my $key ( keys %measurement_ref ) {
	if ( $measurement_count{$key} > 0 ) {
	    $measurement_ref{$key} /= $measurement_count{$key};
	}
    }
    if ( $name =~ /(_UP|_OK)/ ) {    # is looking at OK or UP, sort ascending. Else sort descending
	@sorted_components = sort { $measurement_ref{$a} <=> $measurement_ref{$b} } keys %measurement_ref;
    }
    else {
	@sorted_components = sort { $measurement_ref{$b} <=> $measurement_ref{$a} } keys %measurement_ref;
    }
    if ( $#sorted_components > 9 ) {    # set top number in list
	$#sorted_components = 9;
    }
    my $colorcount = 1;
    my $output_string = '';
    foreach my $key (@sorted_components) {
	$output_string .=
	  "<tr class=insight><td class=insight bgcolor=$Colors[$colorcount-1]><B>$colorcount</B></td><td class=insight> $key</td>";
	$output_string .= "<td class=insight> " . format_output_number( "PERCENT", $measurement_ref{$key} ) . "</td></tr>";
	$colorcount++;
    }
    return $output_string;
}

sub get_daily2 {
    my $sth       = shift;
    my $component = shift;
    my $name      = shift;
    my $sum_ref   = undef;
    my $comp_ref  = undef;
    my $cur_year;
    my $cur_month;
    my $cur_day;
    while ( my $row = $sth->fetchrow_hashref('NAME_uc') ) {
	my $timestamp = $$row{DATESTAMP};
	if ( $timestamp =~ /(\d\d\d\d)-(\d\d)-(\d\d)/ ) {
	    $cur_year  = $1;
	    $cur_month = $2;
	    $cur_day   = $3;
	}
	else {
	    next;
	}

	$sum_ref->{$timestamp}->{ALL}->{VALUE} += $$row{$name};
	$sum_ref->{$timestamp}->{ALL}->{COUNT}++;
	$comp_ref->{ALL}->{VALUE} += $$row{$name};
	$comp_ref->{ALL}->{COUNT}++;
	if ( $component eq "hostgroup" ) {
	    $sum_ref->{$timestamp}->{ $$row{HOSTGROUP_NAME} }->{VALUE} += $$row{$name};
	    $sum_ref->{$timestamp}->{ $$row{HOSTGROUP_NAME} }->{COUNT}++;
	    $comp_ref->{ $$row{HOSTGROUP_NAME} }->{VALUE} += $$row{$name};
	    $comp_ref->{ $$row{HOSTGROUP_NAME} }->{COUNT}++;
	}
	elsif ( $component eq "host" ) {
	    $sum_ref->{$timestamp}->{ $$row{HOST_NAME} }->{VALUE} += $$row{$name};
	    $sum_ref->{$timestamp}->{ $$row{HOST_NAME} }->{COUNT}++;
	    $comp_ref->{ $$row{HOST_NAME} }->{VALUE} += $$row{$name};
	    $comp_ref->{ $$row{HOST_NAME} }->{COUNT}++;
	}
	elsif ( $component eq "hostservice" ) {
	    $sum_ref->{$timestamp}->{ $$row{HOST_NAME} . ":" . $$row{SERVICE_NAME} }->{VALUE} += $$row{$name};
	    $sum_ref->{$timestamp}->{ $$row{HOST_NAME} . ":" . $$row{SERVICE_NAME} }->{COUNT}++;
	    $comp_ref->{ $$row{HOST_NAME} . ":" . $$row{SERVICE_NAME} }->{VALUE} += $$row{$name};
	    $comp_ref->{ $$row{HOST_NAME} . ":" . $$row{SERVICE_NAME} }->{COUNT}++;
	}
	elsif ( $component eq "service" ) {
	    $sum_ref->{$timestamp}->{ $$row{SERVICE_NAME} }->{VALUE} += $$row{$name};
	    $sum_ref->{$timestamp}->{ $$row{SERVICE_NAME} }->{COUNT}++;
	    $comp_ref->{ $$row{SERVICE_NAME} }->{VALUE} += $$row{$name};
	    $comp_ref->{ $$row{SERVICE_NAME} }->{COUNT}++;
	}
    }
    foreach my $interval ( keys %{$sum_ref} ) {
	foreach my $comp ( keys %{ $sum_ref->{$interval} } ) {
	    $sum_ref->{$interval}->{$comp}->{VALUE} = $sum_ref->{$interval}->{$comp}->{VALUE} / $sum_ref->{$interval}->{$comp}->{COUNT};
	}
    }
    foreach my $comp ( keys %{$comp_ref} ) {
	$comp_ref->{$comp}->{VALUE} = $comp_ref->{$comp}->{VALUE} / $comp_ref->{$comp}->{COUNT};
    }
    $barcount = 0;
    my $start_tmp;
    my $end_tmp;
    my $start_month_tmp;
    my $end_month_tmp;
    for ( my $i = $start_year ; $i <= $end_year ; $i++ ) {
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
		my $key = sprintf "%04d-%02d-%02d", $i, $j, $k;
		## print "<br>  Checking day $key";
		# foreach my $comp (keys %{$sum_ref->{$key}}) { # ... }
		foreach my $comp ( keys %{$comp_ref} ) {
		    if ( !exists( $sum_ref->{$key}->{$comp} ) ) {
			## print "<br>  NoData: value of $comp, $key now set to $NoData";
			$sum_ref->{$key}->{$comp}->{VALUE} = $NoData;
		    }
		    ## print "<br>  Value of $comp, $key is ".$sum_ref->{$key}->{$comp}->{VALUE};
		    # $sum_ref->{$key}->{$comp}->{PRINTDATES} = sprintf "%2d/%02d",$j,$k;
		}
		$sum_ref->{$key}->{ALL}->{PRINTDATES} = sprintf "%2d/%02d", $j, $k;
		$barcount++;
	    }
	}
    }
    return ( \%{$sum_ref}, \%{$comp_ref} );
}

sub get_weekly2 {
    my $sth       = shift;
    my $component = shift;
    my $name      = shift;
    my $sum_ref   = undef;
    my $comp_ref  = undef;
    my $cur_year;
    my $cur_month;
    my $cur_day;
    while ( my $row = $sth->fetchrow_hashref('NAME_uc') ) {
	my $timestamp = $$row{DATESTAMP};
	if ( $timestamp =~ /(\d\d\d\d)-(\d\d)-(\d\d)/ ) {
	    $cur_year  = $1;
	    $cur_month = $2;
	    $cur_day   = $3;

	    # compute current week of the year from timestamp
	}
	else {
	    next;
	}
	my $uts = timelocal( "00", "00", "00", $cur_day, ( $cur_month - 1 ), ( $cur_year - 1900 ) );
	my ( $seconds, $minutes, $hours, $day_of_month, $month, $year, $wday, $yday, $isdst ) = localtime($uts);
	my $weekofyear = sprintf "%02d", int( ( $yday - $wday ) / 7 ) + 1;    # First week starts on Sunday
	$timestamp = "$cur_year\-w$weekofyear";
	$sum_ref->{$timestamp}->{ALL}->{VALUE} += $$row{$name};
	$sum_ref->{$timestamp}->{ALL}->{COUNT}++;
	$comp_ref->{ALL}->{VALUE} += $$row{$name};
	$comp_ref->{ALL}->{COUNT}++;

	if ( $component eq "hostgroup" ) {
	    $sum_ref->{$timestamp}->{ $$row{HOSTGROUP_NAME} }->{VALUE} += $$row{$name};
	    $sum_ref->{$timestamp}->{ $$row{HOSTGROUP_NAME} }->{COUNT}++;
	    $comp_ref->{ $$row{HOSTGROUP_NAME} }->{VALUE} += $$row{$name};
	    $comp_ref->{ $$row{HOSTGROUP_NAME} }->{COUNT}++;
	}
	elsif ( $component eq "host" ) {
	    $sum_ref->{$timestamp}->{ $$row{HOST_NAME} }->{VALUE} += $$row{$name};
	    $sum_ref->{$timestamp}->{ $$row{HOST_NAME} }->{COUNT}++;
	    $comp_ref->{ $$row{HOST_NAME} }->{VALUE} += $$row{$name};
	    $comp_ref->{ $$row{HOST_NAME} }->{COUNT}++;
	}
	elsif ( $component eq "hostservice" ) {
	    $sum_ref->{$timestamp}->{ $$row{HOST_NAME} . ":" . $$row{SERVICE_NAME} }->{VALUE} += $$row{$name};
	    $sum_ref->{$timestamp}->{ $$row{HOST_NAME} . ":" . $$row{SERVICE_NAME} }->{COUNT}++;
	    $comp_ref->{ $$row{HOST_NAME} . ":" . $$row{SERVICE_NAME} }->{VALUE} += $$row{$name};
	    $comp_ref->{ $$row{HOST_NAME} . ":" . $$row{SERVICE_NAME} }->{COUNT}++;
	}
	elsif ( $component eq "service" ) {
	    $sum_ref->{$timestamp}->{ $$row{SERVICE_NAME} }->{VALUE} += $$row{$name};
	    $sum_ref->{$timestamp}->{ $$row{SERVICE_NAME} }->{COUNT}++;
	    $comp_ref->{ $$row{SERVICE_NAME} }->{VALUE} += $$row{$name};
	    $comp_ref->{ $$row{SERVICE_NAME} }->{COUNT}++;
	}

    }
    foreach my $interval ( keys %{$sum_ref} ) {
	foreach my $comp ( keys %{ $sum_ref->{$interval} } ) {
	    $sum_ref->{$interval}->{$comp}->{VALUE} = $sum_ref->{$interval}->{$comp}->{VALUE} / $sum_ref->{$interval}->{$comp}->{COUNT};
	}
    }
    foreach my $comp ( keys %{$comp_ref} ) {
	$comp_ref->{$comp}->{VALUE} = $comp_ref->{$comp}->{VALUE} / $comp_ref->{$comp}->{COUNT};
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
	    my $key = "$i\-w$weekofyear";
	    foreach my $comp ( keys %{$comp_ref} ) {
		if ( !exists( $sum_ref->{$key}->{$comp}->{VALUE} ) ) {
		    $sum_ref->{$key}->{$comp}->{VALUE} = $NoData;
		}
	    }
	    $sum_ref->{$key}->{ALL}->{PRINTDATES} = "w$weekofyear";
	    $barcount++;
	}
    }
    return ( \%{$sum_ref}, \%{$comp_ref} );
}

sub get_monthly2 {
    my $sth       = shift;
    my $component = shift;
    my $name      = shift;
    my $sum_ref   = undef;
    my $comp_ref  = undef;
    my $cur_year;
    my $cur_month;
    my $cur_day;
    while ( my $row = $sth->fetchrow_hashref('NAME_uc') ) {
	my $timestamp = $$row{DATESTAMP};
	if ( $timestamp =~ /(\d\d\d\d)-(\d\d)-(\d\d)/ ) {
	    $cur_year  = $1;
	    $cur_month = $2;
	    $cur_day   = $3;
	}
	else {
	    next;
	}
	$timestamp = "$cur_year\-$cur_month";
	$sum_ref->{$timestamp}->{ALL}->{VALUE} += $$row{$name};
	$sum_ref->{$timestamp}->{ALL}->{COUNT}++;
	$comp_ref->{ALL}->{VALUE} += $$row{$name};
	$comp_ref->{ALL}->{COUNT}++;
	if ( $component eq "hostgroup" ) {
	    $sum_ref->{$timestamp}->{ $$row{HOSTGROUP_NAME} }->{VALUE} += $$row{$name};
	    $sum_ref->{$timestamp}->{ $$row{HOSTGROUP_NAME} }->{COUNT}++;
	    $comp_ref->{ $$row{HOSTGROUP_NAME} }->{VALUE} += $$row{$name};
	    $comp_ref->{ $$row{HOSTGROUP_NAME} }->{COUNT}++;
	}
	elsif ( $component eq "host" ) {
	    $sum_ref->{$timestamp}->{ $$row{HOST_NAME} }->{VALUE} += $$row{$name};
	    $sum_ref->{$timestamp}->{ $$row{HOST_NAME} }->{COUNT}++;
	    $comp_ref->{ $$row{HOST_NAME} }->{VALUE} += $$row{$name};
	    $comp_ref->{ $$row{HOST_NAME} }->{COUNT}++;
	}
	elsif ( $component eq "hostservice" ) {
	    $sum_ref->{$timestamp}->{ $$row{HOST_NAME} . ":" . $$row{SERVICE_NAME} }->{VALUE} += $$row{$name};
	    $sum_ref->{$timestamp}->{ $$row{HOST_NAME} . ":" . $$row{SERVICE_NAME} }->{COUNT}++;
	    $comp_ref->{ $$row{HOST_NAME} . ":" . $$row{SERVICE_NAME} }->{VALUE} += $$row{$name};
	    $comp_ref->{ $$row{HOST_NAME} . ":" . $$row{SERVICE_NAME} }->{COUNT}++;
	}
	elsif ( $component eq "service" ) {
	    $sum_ref->{$timestamp}->{ $$row{SERVICE_NAME} }->{VALUE} += $$row{$name};
	    $sum_ref->{$timestamp}->{ $$row{SERVICE_NAME} }->{COUNT}++;
	    $comp_ref->{ $$row{SERVICE_NAME} }->{VALUE} += $$row{$name};
	    $comp_ref->{ $$row{SERVICE_NAME} }->{COUNT}++;
	}
    }

    foreach my $interval ( keys %{$sum_ref} ) {
	foreach my $comp ( keys %{ $sum_ref->{$interval} } ) {
	    $sum_ref->{$interval}->{$comp}->{VALUE} = $sum_ref->{$interval}->{$comp}->{VALUE} / $sum_ref->{$interval}->{$comp}->{COUNT};
	}
    }
    foreach my $comp ( keys %{$comp_ref} ) {
	$comp_ref->{$comp}->{VALUE} = $comp_ref->{$comp}->{VALUE} / $comp_ref->{$comp}->{COUNT};
    }

    $barcount = 0;
    my $start_tmp;
    my $end_tmp;
    for ( my $i = $start_year ; $i <= $end_year ; $i++ ) {
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
	    my $key = sprintf "%04d\-%02d", $i, $j;

	    # if (!$sum_month{$key}) {
	    #     $sum_month{$key} = $NoData;
	    # }
	    # $printdates{$key} = qw(Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec)[$j-1];
	    foreach my $comp ( keys %{$comp_ref} ) {
		if ( !exists( $sum_ref->{$key}->{$comp}->{VALUE} ) ) {
		    $sum_ref->{$key}->{$comp}->{VALUE} = $NoData;
		}
	    }
	    $sum_ref->{$key}->{ALL}->{PRINTDATES} = qw(Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec) [ $j - 1 ];
	    $barcount++;
	}
    }
    return ( \%{$sum_ref}, \%{$comp_ref} );
}

sub get_yearly2 {
    my $sth       = shift;
    my $component = shift;
    my $name      = shift;
    my $sum_ref   = undef;
    my $comp_ref  = undef;
    my $cur_year;
    my $cur_month;
    my $cur_day;
    while ( my $row = $sth->fetchrow_hashref('NAME_uc') ) {
	my $timestamp = $$row{DATESTAMP};
	if ( $timestamp =~ /(\d\d\d\d)\-(\d\d)\-(\d\d)/ ) {
	    $cur_year  = $1;
	    $cur_month = $2;
	    $cur_day   = $3;
	}
	else {
	    next;
	}
	$timestamp = $cur_year;
	$sum_ref->{$timestamp}->{ALL}->{VALUE} += $$row{$name};
	$sum_ref->{$timestamp}->{ALL}->{COUNT}++;
	$comp_ref->{ALL}->{VALUE} += $$row{$name};
	$comp_ref->{ALL}->{COUNT}++;
	if ( $component eq "hostgroup" ) {
	    $sum_ref->{$timestamp}->{ $$row{HOSTGROUP_NAME} }->{VALUE} += $$row{$name};
	    $sum_ref->{$timestamp}->{ $$row{HOSTGROUP_NAME} }->{COUNT}++;
	    $comp_ref->{ $$row{HOSTGROUP_NAME} }->{VALUE} += $$row{$name};
	    $comp_ref->{ $$row{HOSTGROUP_NAME} }->{COUNT}++;
	}
	elsif ( $component eq "host" ) {
	    $sum_ref->{$timestamp}->{ $$row{HOST_NAME} }->{VALUE} += $$row{$name};
	    $sum_ref->{$timestamp}->{ $$row{HOST_NAME} }->{COUNT}++;
	    $comp_ref->{ $$row{HOST_NAME} }->{VALUE} += $$row{$name};
	    $comp_ref->{ $$row{HOST_NAME} }->{COUNT}++;
	}
	elsif ( $component eq "hostservice" ) {
	    $sum_ref->{$timestamp}->{ $$row{HOST_NAME} . ":" . $$row{SERVICE_NAME} }->{VALUE} += $$row{$name};
	    $sum_ref->{$timestamp}->{ $$row{HOST_NAME} . ":" . $$row{SERVICE_NAME} }->{COUNT}++;
	    $comp_ref->{ $$row{HOST_NAME} . ":" . $$row{SERVICE_NAME} }->{VALUE} += $$row{$name};
	    $comp_ref->{ $$row{HOST_NAME} . ":" . $$row{SERVICE_NAME} }->{COUNT}++;
	}
	elsif ( $component eq "service" ) {
	    $sum_ref->{$timestamp}->{ $$row{SERVICE_NAME} }->{VALUE} += $$row{$name};
	    $sum_ref->{$timestamp}->{ $$row{SERVICE_NAME} }->{COUNT}++;
	    $comp_ref->{ $$row{SERVICE_NAME} }->{VALUE} += $$row{$name};
	    $comp_ref->{ $$row{SERVICE_NAME} }->{COUNT}++;
	}
    }

    foreach my $interval ( keys %{$sum_ref} ) {
	foreach my $comp ( keys %{ $sum_ref->{$interval} } ) {
	    $sum_ref->{$interval}->{$comp}->{VALUE} = $sum_ref->{$interval}->{$comp}->{VALUE} / $sum_ref->{$interval}->{$comp}->{COUNT};
	}
    }
    foreach my $comp ( keys %{$comp_ref} ) {
	$comp_ref->{$comp}->{VALUE} = $comp_ref->{$comp}->{VALUE} / $comp_ref->{$comp}->{COUNT};
    }

    $barcount = 0;
    for ( my $i = $start_year ; $i <= $end_year ; $i++ ) {
	my $key = $i;

	# if (!$sum_year{$i}) {
	#     $sum_year{$i}=$NoData;
	# }
	# $printdates{$i} = $i;
	foreach my $comp ( keys %{$comp_ref} ) {
	    if ( !exists( $sum_ref->{$key}->{$comp}->{VALUE} ) ) {
		$sum_ref->{$key}->{$comp}->{VALUE} = $NoData;
	    }
	}
	$barcount++;
	$sum_ref->{$key}->{ALL}->{PRINTDATES} = $i;
    }
    return ( \%{$sum_ref}, \%{$comp_ref} );
}

sub print_graph_bars {
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

    my @graphtable = @_;

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

    # $my_graph->set_legend('Outage Measurement');
    my $my_image = $my_graph->plot( \@graphtable );
    save_chart( $my_image, "$main::graphdirectory/$graphfile" );
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
    save_chart( $my_image, "$main::graphdirectory/$graphfile" );
    return;
}

sub print_graph_lines {
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

    my @graphtable = @_;

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

    # $my_graph->set_legend('Outage Measurement');
    my $my_image = $my_graph->plot( \@graphtable );
    save_chart( $my_image, "$main::graphdirectory/$graphfile" );
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
    save_chart( $my_image, "$main::graphdirectory/$graphfile" );
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

sub format_output_number {
    my $type  = shift;
    my $value = shift;
    if ( $value eq $NoData ) {
	return $value;
    }
    else {
	if ( $type =~ /PERCENT/i ) {
	    if ( ( $value - int($value) ) == 0 ) {
		return sprintf "%0d%%", $value;
	    }
	    else {
		return sprintf "%0.4f%%", $value;    # fraction
	    }
	}
	else {
	    if ( ( $value - int($value) ) > 0 ) {
		$value = sprintf "%0.2f", $value;    # fraction
	    }
	    return commify($value);
	}
    }
}

sub commify {
    my $text = reverse sprintf "%0d", $_[0];
    $text =~ s/(\d\d\d)(?=\d)(?!\d*\.)/$1,/g;
    return scalar reverse $text;
}

1;

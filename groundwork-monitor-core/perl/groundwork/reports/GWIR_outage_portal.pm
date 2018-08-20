#!/usr/local/groundwork/bin/perl --
#
#	GroundWork Monitor - The ultimate data integration framework.
#	Copyright (C) 2004-2006 GroundWork Open Source Solutions
#	info@itgroundwork.com
#
#	This program is free software; you can redistribute it and/or modify
#	it under the terms of version 2 of the GNU General Public License
#	as published by the Free Software Foundation.
#
#	This program is distributed in the hope that it will be useful,
#	but WITHOUT ANY WARRANTY; without even the implied warranty of
#	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
#	GNU General Public License for more details.
#
#	You should have received a copy of the GNU General Public License
#	along with this program; if not, write to the Free Software
#	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
#
use DBI;
use Time::Local;

sub get_alarm_data {
	$host_outage_parameter=shift;
	$service_outage_parameter=shift;

	$table_print_string = "<table class='data' border='0' cellpadding='5' cellspacing='1' width='100%'><TBODY><TR>";	# Initialize string 
	if ($FORM_DATA{interval} eq "daily") {
		$tmp = "Day";
	} elsif ($FORM_DATA{interval} eq "weekly") {
		$tmp = "Week";
	} elsif ($FORM_DATA{interval} eq "monthly") {
		$tmp = "Month";
	} elsif ($FORM_DATA{interval} eq "yearly") {
		$tmp = "Year";
	} 
	if ($FORM_DATA{component} eq "all") {
		$outage_parameter=$host_outage_parameter;
	} elsif ($FORM_DATA{component} eq "hostgroup") {
		$outage_parameter=$host_outage_parameter;
	} elsif ($FORM_DATA{component} eq "host") {
		$outage_parameter=$host_outage_parameter;
	} elsif ($FORM_DATA{component} eq "hostservice") {
		$outage_parameter=$service_outage_parameter;
	} elsif ($FORM_DATA{component} eq "service") {
		$outage_parameter=$service_outage_parameter;
	}
	$tmpparm = $outage_parameter;
	$tmpparm =~ s/_/ /g;
	$table_print_string .= "
		<td class=tableHeaderFlexWidth> Measurement - $tmpparm</td>
		<td class=tableHeaderFlexWidth> Current $tmp</td>
		<td class=tableHeaderFlexWidth> Last $tmp</td>
		<td class=tableHeaderFlexWidth> Minimum</td>
		<td class=tableHeaderFlexWidth> Maximum</td>
		<td class=tableHeaderFlexWidth> Average</td>
		<td class=tableHeaderFlexWidth> # of Samples</td>
		</TR>
		";

	($sum_ref,$comp_ref) = get_outage_data($outage_parameter,$FORM_DATA{component},$FORM_DATA{interval});
	$rowclass0="tableFill01";		# used for alternating colors for sublists
	$rowclass1="tableFill02";		# used for alternating colors for sublists
	$rowclass2="tableFill03";
	if ($FORM_DATA{component} ne "all") {
		$rowclass = $rowclass0;
	} else {
		$rowclass = $rowclass2;
	}

	$current = format_output_number($outage_parameter,$comp_ref->{ALL}->{CURRENT});
	$last = format_output_number($outage_parameter,$comp_ref->{ALL}->{LAST});
	$min = format_output_number($outage_parameter,$comp_ref->{ALL}->{MIN});
	$max = format_output_number($outage_parameter,$comp_ref->{ALL}->{MAX});
	$avg = format_output_number($outage_parameter,$comp_ref->{ALL}->{AVERAGE});
	$samplecount = format_output_number("SAMPLECOUNT",$comp_ref->{ALL}->{SAMPLECOUNT});
	$table_print_string .=  "
		<tr>
		<td class=$rowclass>Total Outages</td>
		<td class=$rowclass align=center>$current</td>
		<td class=$rowclass align=center>$last</td>
		<td class=$rowclass align=center>$min </td>
		<td class=$rowclass align=center>$max</td>
		<td class=$rowclass align=center>$avg </td>
		<td class=$rowclass align=center>$samplecount</td>
		";
	if ($FORM_DATA{component} ne "all") {
		$rowclass = $rowclass1;
		foreach $comp (sort keys %{$comp_ref}) {
			if ($comp eq "ALL") { next;	}	# ALL is already printed
			if ($rowclass eq $rowclass1) {
				$rowclass = $rowclass2;
			} else {
				$rowclass = $rowclass1;
			}
			$current = format_output_number($outage_parameter,$comp_ref->{$comp}->{CURRENT});
			$last = format_output_number($outage_parameter,$comp_ref->{$comp}->{LAST});
			$min = format_output_number($outage_parameter,$comp_ref->{$comp}->{MIN});
			$max = format_output_number($outage_parameter,$comp_ref->{$comp}->{MAX});
			$avg = format_output_number($outage_parameter,$comp_ref->{$comp}->{VALUE});
			$samplecount = format_output_number("SAMPLECOUNT",$comp_ref->{$comp}->{SAMPLECOUNT});
			$table_print_string .=  "
				<tr>
				<td class=$rowclass>$comp</td>
				<td class='$rowclass' align=center>$current</td>
				<td class='$rowclass' align=center>$last</td>
				<td class='$rowclass' align=center>$min </td>
				<td class='$rowclass' align=center>$max</td>
				<td class='$rowclass' align=center>$avg </td>
				<td class='$rowclass' align=center>$samplecount</td>
			";
		}
	}
	$table_print_string .=  "</table>";
	return;
}


sub print_outage_trend_chart {
#	@array_keys_ref = sort keys %values1;
	@array_keys_ref = sort keys %{$sum_ref};

	@array_values1_ref = ();
	$graphfile = "outagecount_graph.png";
	print "<table class='data' border='0' cellpadding='5' cellspacing='1' width='100%'>";
	print "<tbody><tr class='tableHeader'>";
	print "<td> Trend Chart - Total $tmpparm</td>";
	print "<tr>";
	$tmpsum = 0;
	foreach $key (@array_keys_ref) {
		push @array_values1_ref, $sum_ref->{$key}->{ALL}->{VALUE};
		$tmpsum += $sum_ref->{$key}->{ALL}->{VALUE};
	}
	if ($tmpsum == 0) {
		print "<td class=tableFill03  style='border: 0px none ;' colspan=2>No data found.</td>";
		print "</table>";
		return;
	}
	if ($barcount <60) {
		if ($barcount < 8) {
			$x_label_skip = 1;
		} elsif ($barcount < 15) {
			$x_label_skip = 3;
		} else {
			$x_label_skip = $barcount;
		}
		print_graph_bars(\@array_keys_ref,\@array_values1_ref);
	} else {
		$x_label_skip = $barcount;
		print_graph_lines(\@array_keys_ref,\@array_values1_ref);
	}
	$time=time;
	print "<td class=tableFill03>
			<table cellpadding='2' cellspacing='1'>
			<tbody>
			<tr><td style='border: 0px none ;'><img src='$graphhtmlref/spacer.gif' border='0' height='20' width='1'></td></tr>
			<tr><td style='border: 0px none ;' rowspan='3'>
			<IMG border=0 src='$graphhtmlref/$graphfile?$time' border='0' hspace='20'></td>
			<td style='border: 0px none ;' valign='top'><img src='$graphhtmlref/01_002.gif' border='0' height='16' hspace='5' width='17'></td>
			<td style='border: 0px none ;' valign='top'>Outage Measurement</td></tr>
			<tr><td style='border: 0px none ;'><img src='$graphhtmlref/spacer.gif' border='0' height='40' width='1'></td></tr>
	</tbody></table></table>				
			";
	return;
}

sub print_trend_chart_component {
	@array_keys_ref = sort keys %{$sum_ref};
	@data=();
	@print_array_keys_ref = ();
	foreach $key (@array_keys_ref ) {
		if ($sum_ref->{$key}->{ALL}->{PRINTDATES}) {
			push @print_array_keys_ref,$sum_ref->{$key}->{ALL}->{PRINTDATES} ;		# Set the date on the graph to the formated date
		} else {
			push @print_array_keys_ref,$key ;
		}
	}

	push @data,\@print_array_keys_ref;	# push time stamps

	$tmparray = undef;
	@legend=();
	$barcount=$#array_keys_ref;
	foreach $key (@sorted_components) {		# key=component, ie a host group, host, service
		$tmp=$key;
#		push @legend,$tmp;
		@{$tmparray->{$key}}=();
		foreach $key2 (@array_keys_ref)	{			# key2=timestamps, i.e., key of hash values
			if ($sum_ref->{$key2}->{$key}->{VALUE} eq $NoData) {
				$sum_ref->{$key2}->{$key}->{VALUE}=0	;
			} 
			push @{$tmparray->{$key}},$sum_ref->{$key2}->{$key}->{VALUE}	;
		}
		push @data,\@{$tmparray->{$key}};
	}
	if ($barcount <60) {
		if ($barcount < 15) {
			$x_label_skip = 1;
		} elsif ($barcount < 25) {
			$x_label_skip = 3;
		} else {
			$x_label_skip = $barcount;
		}
		print_graph_bars_stacked();
	} else {
		$x_label_skip = $barcount;
		print_graph_lines_stacked();
	}
	return;
}


sub get_outage_data {
	my $name=shift;
	my $component=shift;
	my $interval=shift;
	my @period = ();
	my @values = ();
	my $sum_ref = undef;
	my $start= "$start_year-$start_month-$start_day";
	my $end= "$end_year-$end_month-$end_day";
	my $query;
	if ($component eq "all") {
		$query = "SELECT * FROM host_availability WHERE (DATESTAMP>='$start' and DATESTAMP<='$end') ORDER BY DATESTAMP";
	} elsif ($component eq "hostgroup") {
		$query = "SELECT * FROM hostgroup_host_availability WHERE (DATESTAMP>='$start' and DATESTAMP<='$end') ORDER BY DATESTAMP";
	} elsif ($component eq "host") {
		$query = "SELECT * FROM host_availability WHERE (DATESTAMP>='$start' and DATESTAMP<='$end') ORDER BY DATESTAMP";
	} elsif ($component eq "hostservice") {
		$query = "SELECT * FROM service_availability WHERE (DATESTAMP>='$start' and DATESTAMP<='$end') ORDER BY DATESTAMP";
	} elsif ($component eq "service") {
		$query = "SELECT * FROM service_availability WHERE (DATESTAMP>='$start' and DATESTAMP<='$end') ORDER BY DATESTAMP";
	}
#	print "<br>$query\n";
	$sth = $dbh->prepare($query);
	$sth->execute() or die  $@;
	if ($interval eq "weekly") {
		($sum_ref,$comp_ref) = get_weekly2($component,$name);
		$x_label="Week";
	} elsif ($interval eq "monthly") {
		($sum_ref,$comp_ref) = get_monthly2($component,$name);
		$x_label="Month";
	} elsif ($interval eq "yearly"){
		($sum_ref,$comp_ref) = get_yearly2($component,$name);
		$x_label="Year";
	} else {	# then daily
		($sum_ref,$comp_ref) = get_daily2($component,$name);
		$x_label="Day";
	}

	foreach my $comp (keys %{$comp_ref}) {
		@tmp_sorted = sort keys %$sum_ref ; # sort by time
		$comp_ref->{$comp}->{CURRENT} = $sum_ref->{$tmp_sorted[$#tmp_sorted]}->{$comp}->{VALUE};	# current is the last item in the sorted array
		$comp_ref->{$comp}->{LAST} = $sum_ref->{$tmp_sorted[$#tmp_sorted - 1]}->{$comp}->{VALUE};	# last is the next to last item in the sorted array
		@tmpvalues = ();
		$total = 0;
		$samplecount = 0;
		foreach my $interval (sort keys %$sum_ref) {
				if ($sum_ref->{$interval}->{$comp}->{VALUE} eq $NoData) { 
					next;
				} else {
					$total += $sum_ref->{$interval}->{$comp}->{VALUE};
					$samplecount++;
					push @tmpvalues,$sum_ref->{$interval}->{$comp}->{VALUE};	# use to calc min and max. Only include valid data here,
				}
		}
		@tmp_sorted = sort { $a <=> $b } @tmpvalues ; # sort by hash value, ascending
		$comp_ref->{$comp}->{MAX} = $tmp_sorted[$#tmp_sorted];
		$comp_ref->{$comp}->{MIN} = $tmp_sorted[0];
		if ($samplecount > 0) {
			$comp_ref->{$comp}->{AVERAGE} = $total / $samplecount;
		} else {
			$comp_ref->{$comp}->{AVERAGE} = $NoData;
		}
		$comp_ref->{$comp}->{SAMPLECOUNT} = $samplecount;
	}
	return (\%{$sum_ref},\%{$comp_ref});
}



#($sum_ref,$comp_ref) = get_outage_data($outage_parameter,$FORM_DATA{component},$FORM_DATA{interval});
sub print_top_components	{
	my $component = shift;
	my $name = shift;
	my $interval = shift;
	my $start = "$start_year-$start_month-$start_day";
	my $end = "$end_year-$end_month-$end_day";
	($sum_ref,$comp_ref) = get_outage_data($name,$component,$FORM_DATA{interval});
	foreach $key (keys %{$comp_ref}) {
		if ($key eq "ALL") {
			delete($comp_ref->{$key});
		}
	}
	if ($name =~ /(_UP|_OK)/)  {		# is looking at OK or UP, sort ascending. Else sort descending
		@sorted_components = sort { $comp_ref->{$a}->{VALUE} <=> $comp_ref->{$b}->{VALUE} } keys %{$comp_ref};
	} else {
		@sorted_components = sort { $comp_ref->{$b}->{VALUE} <=> $comp_ref->{$a}->{VALUE} } keys %{$comp_ref};
	}
	if ($#sorted_components>9) {	# set top number in list
		$#sorted_components=9;
	}
	$colorcount=1;
	my $output_string = undef;
	foreach $key (@sorted_components) {
		$styleclass= sprintf "chart%02d",$colorcount+3;
		$output_string .= "<tr><td class=$styleclass style='border: 0px none ;' align=center>$colorcount</td>";
		$output_string .= "<td class=tableFill02>$key</td>";
		$output_string .= "<td class=tableFill02 align=center>".format_output_number($name,$comp_ref->{$key}->{VALUE})."</td>";
		$colorcount++;
	}
	return $output_string;
}



sub print_top_components_save	{
	my $component = shift;
	my $name = shift;
	my $start = "$start_year-$start_month-$start_day";
	my $end = "$end_year-$end_month-$end_day";

	if ($component eq "all") {
		$query = "SELECT * FROM host_availability WHERE (DATESTAMP>='$start' and DATESTAMP<='$end') ";
	} elsif ($component eq "hostgroup") {
		$query = "SELECT * FROM hostgroup_host_availability WHERE (DATESTAMP>='$start' and DATESTAMP<='$end') ";
	} elsif ($component eq "host") {
		$query = "SELECT * FROM host_availability WHERE (DATESTAMP>='$start' and DATESTAMP<='$end') ";
	} elsif ($component eq "hostservice") {
		$query = "SELECT * FROM service_availability WHERE (DATESTAMP>='$start' and DATESTAMP<='$end') ";
	} elsif ($component eq "service") {
		$query = "SELECT * FROM service_availability WHERE (DATESTAMP>='$start' and DATESTAMP<='$end') ";
	}
	$sth = $dbh->prepare($query);
	$sth->execute() or die  $@;
	%measurement_ref = ();
	$day_ref = undef;
	while ($row=$sth->fetchrow_hashref()) { 
#		print "<br>$component=$$row{HOST_NAME},$name=$$row{$name}";
		if ($component eq "all") {
			$measurement_ref{$$row{HOST_NAME}} += $$row{$name};
			$measurement_count{$$row{HOST_NAME}}++;
		} elsif ($component eq "hostgroup") {
			$measurement_ref{$$row{HOSTGROUP_NAME}} += $$row{$name};
			$measurement_count{$$row{HOSTGROUP_NAME}}++;
		} elsif ($component eq "host") {
			$measurement_ref{$$row{HOST_NAME}} += $$row{$name};
			$measurement_count{$$row{HOST_NAME}}++;
		} elsif ($component eq "hostservice") {
			$measurement_ref{$$row{HOST_NAME}.":".$$row{SERVICE_NAME}} += $$row{$name};
			$measurement_count{$$row{HOST_NAME}.":".$$row{SERVICE_NAME}}++;
		} elsif ($component eq "service") {
			$measurement_ref{$$row{SERVICE_NAME}} += $$row{$name};
			$measurement_count{$$row{SERVICE_NAME}}++;
		}
	}
	foreach $key (keys %measurement_ref) {
		if ($measurement_count{$key} > 0) {
			$measurement_ref{$key} /= $measurement_count{$key};
		}
	}
	if ($name =~ /(_UP|_OK)/)  {		# is looking at OK or UP, sort ascending. Else sort descending
		@sorted_components = sort { $measurement_ref{$a} <=> $measurement_ref{$b} } keys %measurement_ref;
	} else {
		@sorted_components = sort { $measurement_ref{$b} <=> $measurement_ref{$a} } keys %measurement_ref;
	}
	if ($#sorted_components>9) {	# set top number in list
		$#sorted_components=9;
	}
	$colorcount=1;
	my $output_string = undef;
	foreach $key (@sorted_components) {
		$output_string .= "<tr class=insight><td class=insight bgcolor=$Colors[$colorcount-1]><B>$colorcount</B></td><td class=insight> $key</td>";
		$output_string .= "<td class=insight> ".format_output_number("PERCENT",$measurement_ref{$key})."</td>";
		$colorcount++;
	}
	return $output_string;
}



sub get_daily2 {
	my $component=shift;
	my $name=shift;
	my $sum_ref=undef;
	my $comp_ref=undef;
	while ($row=$sth->fetchrow_hashref()) { 
		$timestamp = $$row{DATESTAMP};
		if ($timestamp =~ /(\d\d\d\d)-(\d\d)-(\d\d)/) {
			$cur_month = $2;
			$cur_day = $3;
			$cur_year = $1;
		} else {
			next;
		}

		$sum_ref->{$timestamp}->{ALL}->{VALUE} += $$row{$name};
		$sum_ref->{$timestamp}->{ALL}->{COUNT}++;
		$comp_ref->{ALL}->{VALUE} += $$row{$name};
		$comp_ref->{ALL}->{COUNT}++;
		if ($component eq "hostgroup") {
			$sum_ref->{$timestamp}->{$$row{HOSTGROUP_NAME}}->{VALUE} += $$row{$name};
			$sum_ref->{$timestamp}->{$$row{HOSTGROUP_NAME}}->{COUNT}++;
			$comp_ref->{$$row{HOSTGROUP_NAME}}->{VALUE} += $$row{$name};
			$comp_ref->{$$row{HOSTGROUP_NAME}}->{COUNT}++;
		} elsif ($component eq "host") {
			$sum_ref->{$timestamp}->{$$row{HOST_NAME}}->{VALUE} += $$row{$name};
			$sum_ref->{$timestamp}->{$$row{HOST_NAME}}->{COUNT}++;
			$comp_ref->{$$row{HOST_NAME}}->{VALUE} += $$row{$name};
			$comp_ref->{$$row{HOST_NAME}}->{COUNT}++;
		} elsif ($component eq "hostservice") {
			$sum_ref->{$timestamp}->{$$row{HOST_NAME}.":".$$row{SERVICE_NAME}}->{VALUE} += $$row{$name};
			$sum_ref->{$timestamp}->{$$row{HOST_NAME}.":".$$row{SERVICE_NAME}}->{COUNT}++;
			$comp_ref->{$$row{HOST_NAME}.":".$$row{SERVICE_NAME}}->{VALUE} += $$row{$name};
			$comp_ref->{$$row{HOST_NAME}.":".$$row{SERVICE_NAME}}->{COUNT}++;
		} elsif ($component eq "service") {
			$sum_ref->{$timestamp}->{$$row{SERVICE_NAME}}->{VALUE} += $$row{$name};
			$sum_ref->{$timestamp}->{$$row{SERVICE_NAME}}->{COUNT}++;
			$comp_ref->{$$row{SERVICE_NAME}}->{VALUE} += $$row{$name};
			$comp_ref->{$$row{SERVICE_NAME}}->{COUNT}++;
		}
	}
	foreach my $interval (keys %{$sum_ref}) {
		foreach my $comp (keys %{$sum_ref->{$interval}}) {
			$sum_ref->{$interval}->{$comp}->{VALUE} = $sum_ref->{$interval}->{$comp}->{VALUE} / $sum_ref->{$interval}->{$comp}->{COUNT};
		}
	}
	foreach my $comp (keys %{$comp_ref}) {
			$comp_ref->{$comp}->{VALUE} =  $comp_ref->{$comp}->{VALUE} / $comp_ref->{$comp}->{COUNT};
	}
	$barcount = 0;
	for ($i = $start_year; $i <= $end_year; $i++) {
		if ($i==$start_year) {
			$start_month_tmp=$start_month;
		} else {
			$start_month_tmp = 1;
		}
		if ($i==$end_year) {
			$end_month_tmp=$end_month;
		} else {
			$end_month_tmp = 12;
		}
		for ($j = $start_month_tmp; $j <= $end_month_tmp; $j++) {
			if (($j==$start_month) and ($i==$start_year)) {
				$start_tmp=$start_day;
			} else {
				$start_tmp = 1;
			}
			if (($j==$end_month) and ($i==$end_year)) {
				$end_tmp=$end_day;
			} else {
				if (($end_year % 4)==0) {	# Check if leap year; yes then Feb has 29 days
					$end_tmp = (31,29,31,30,31,30,31,31,30,31,30,31)[$end_month-1];
				} else {					# else Feb only has 28 days
					$end_tmp = (31,28,31,30,31,30,31,31,30,31,30,31)[$end_month-1];
				}
			}
			for ($k = $start_tmp; $k <= $end_tmp; $k++) {
				$key = sprintf "%04d-%02d-%02d",$i,$j,$k;
#				print "<br>  Checking day $key";
#				foreach my $comp (keys %{$sum_ref->{$key}}) {
				foreach my $comp (keys %{$comp_ref}) {
					if (!exists($sum_ref->{$key}->{$comp})) {
#						print "<br>  NoData: value of $comp, $key now set to $NoData";
						$sum_ref->{$key}->{$comp}->{VALUE} = $NoData;
					}
#					print "<br>  Value of $comp, $key is ".$sum_ref->{$key}->{$comp}->{VALUE};
#					$sum_ref->{$key}->{$comp}->{PRINTDATES} = sprintf "%2d/%02d",$j,$k; 
				}
				$sum_ref->{$key}->{ALL}->{PRINTDATES} = sprintf "%2d/%02d",$j,$k; 
				$barcount++;
			}
		}
	}
	return (\%{$sum_ref},\%{$comp_ref});
}



sub get_weekly2 {
	my $component=shift;
	my $name=shift;
	my $sum_ref=undef;
	my $comp_ref=undef;
	while ($row=$sth->fetchrow_hashref()) { 
		$timestamp = $$row{DATESTAMP};
		if ($timestamp =~ /(\d\d\d\d)-(\d\d)-(\d\d)/) {
			$cur_month = $2;
			$cur_day = $3;
			$cur_year = $1;
			# compute current week of the year from timestamp
		} else {
			next;
		}
		$uts = timelocal("00", "00", "00", $cur_day, ($cur_month-1), ($cur_year-1900) );
		($seconds, $minutes, $hours, $day_of_month, $month, $year,$wday, $yday, $isdst) = localtime($uts);
		$weekofyear = sprintf "%02d",int(($yday-$wday)/7)+1;	# First week starts on Sunday
		$timestamp = "$cur_year\-$weekofyear";
		$sum_ref->{$timestamp}->{ALL}->{VALUE} += $$row{$name};
		$sum_ref->{$timestamp}->{ALL}->{COUNT}++;
		$comp_ref->{ALL}->{VALUE} += $$row{$name};
		$comp_ref->{ALL}->{COUNT}++;
		if ($component eq "hostgroup") {
			$sum_ref->{$timestamp}->{$$row{HOSTGROUP_NAME}}->{VALUE} += $$row{$name};
			$sum_ref->{$timestamp}->{$$row{HOSTGROUP_NAME}}->{COUNT}++;
			$comp_ref->{$$row{HOSTGROUP_NAME}}->{VALUE} += $$row{$name};
			$comp_ref->{$$row{HOSTGROUP_NAME}}->{COUNT}++;
		} elsif ($component eq "host") {
			$sum_ref->{$timestamp}->{$$row{HOST_NAME}}->{VALUE} += $$row{$name};
			$sum_ref->{$timestamp}->{$$row{HOST_NAME}}->{COUNT}++;
			$comp_ref->{$$row{HOST_NAME}}->{VALUE} += $$row{$name};
			$comp_ref->{$$row{HOST_NAME}}->{COUNT}++;
		} elsif ($component eq "hostservice") {
			$sum_ref->{$timestamp}->{$$row{HOST_NAME}.":".$$row{SERVICE_NAME}}->{VALUE} += $$row{$name};
			$sum_ref->{$timestamp}->{$$row{HOST_NAME}.":".$$row{SERVICE_NAME}}->{COUNT}++;
			$comp_ref->{$$row{HOST_NAME}.":".$$row{SERVICE_NAME}}->{VALUE} += $$row{$name};
			$comp_ref->{$$row{HOST_NAME}.":".$$row{SERVICE_NAME}}->{COUNT}++;
		} elsif ($component eq "service") {
			$sum_ref->{$timestamp}->{$$row{SERVICE_NAME}}->{VALUE} += $$row{$name};
			$sum_ref->{$timestamp}->{$$row{SERVICE_NAME}}->{COUNT}++;
			$comp_ref->{$$row{SERVICE_NAME}}->{VALUE} += $$row{$name};
			$comp_ref->{$$row{SERVICE_NAME}}->{COUNT}++;
		}

	}
	foreach my $interval (keys %{$sum_ref}) {
		foreach my $comp (keys %{$sum_ref->{$interval}}) {
			$sum_ref->{$interval}->{$comp}->{VALUE} = $sum_ref->{$interval}->{$comp}->{VALUE} / $sum_ref->{$interval}->{$comp}->{COUNT};
		}
	}
	foreach my $comp (keys %{$comp_ref}) {
			$comp_ref->{$comp}->{VALUE} =  $comp_ref->{$comp}->{VALUE} / $comp_ref->{$comp}->{COUNT};
	}
	$barcount = 0;
	for ($i = $start_year; $i <= $end_year; $i++) {
		if ($i==$start_year) {
			$uts = timelocal("00", "00", "00", $start_day, ($start_month-1), ($start_year-1900) );
			($seconds, $minutes, $hours, $day_of_month, $month, $year,$wday, $yday, $isdst) = localtime($uts);
			$start_tmp = int(($yday-$wday)/7)+1; 
		} else {
			$start_tmp = 1;
		}
		if ($i==$end_year) {
			$uts = timelocal("00", "00", "00", $end_day, ($end_month-1), ($end_year-1900) );
			($seconds, $minutes, $hours, $day_of_month, $month, $year,$wday, $yday, $isdst) = localtime($uts);
			$end_tmp = int(($yday-$wday)/7)+1; 
		} else {
			$end_tmp = 52;
		}

		for ($j = $start_tmp; $j <= $end_tmp; $j++) {
			$weekofyear = sprintf "%02d",$j;
			$key = "$i\-$weekofyear";
			foreach my $comp (keys %{$comp_ref}) {
				if (!exists($sum_ref->{$key}->{$comp}->{VALUE})) {
					$sum_ref->{$key}->{$comp}->{VALUE} = $NoData;
				}
			}
			$sum_ref->{$key}->{ALL}->{PRINTDATES} = "$weekofyear"; 
			$barcount++;
		}
	}
	return (\%{$sum_ref},\%{$comp_ref});
}

sub get_monthly2 {
	my $component=shift;
	my $name=shift;
	my $sum_ref=undef;
	my $comp_ref=undef;
	while ($row=$sth->fetchrow_hashref()) { 
		$timestamp = $$row{DATESTAMP};
		if ($timestamp =~ /(\d\d\d\d)-(\d\d)-(\d\d)/) {
			$cur_month = $2;
			$cur_day = $3;
			$cur_year = $1;
		} else {
			next;
		}
		$timestamp = "$cur_year\-$cur_month";
		$sum_ref->{$timestamp}->{ALL}->{VALUE} += $$row{$name};
		$sum_ref->{$timestamp}->{ALL}->{COUNT}++;
		$comp_ref->{ALL}->{VALUE} += $$row{$name};
		$comp_ref->{ALL}->{COUNT}++;
		if ($component eq "hostgroup") {
			$sum_ref->{$timestamp}->{$$row{HOSTGROUP_NAME}}->{VALUE} += $$row{$name};
			$sum_ref->{$timestamp}->{$$row{HOSTGROUP_NAME}}->{COUNT}++;
			$comp_ref->{$$row{HOSTGROUP_NAME}}->{VALUE} += $$row{$name};
			$comp_ref->{$$row{HOSTGROUP_NAME}}->{COUNT}++;
		} elsif ($component eq "host") {
			$sum_ref->{$timestamp}->{$$row{HOST_NAME}}->{VALUE} += $$row{$name};
			$sum_ref->{$timestamp}->{$$row{HOST_NAME}}->{COUNT}++;
			$comp_ref->{$$row{HOST_NAME}}->{VALUE} += $$row{$name};
			$comp_ref->{$$row{HOST_NAME}}->{COUNT}++;
		} elsif ($component eq "hostservice") {
			$sum_ref->{$timestamp}->{$$row{HOST_NAME}.":".$$row{SERVICE_NAME}}->{VALUE} += $$row{$name};
			$sum_ref->{$timestamp}->{$$row{HOST_NAME}.":".$$row{SERVICE_NAME}}->{COUNT}++;
			$comp_ref->{$$row{HOST_NAME}.":".$$row{SERVICE_NAME}}->{VALUE} += $$row{$name};
			$comp_ref->{$$row{HOST_NAME}.":".$$row{SERVICE_NAME}}->{COUNT}++;
		} elsif ($component eq "service") {
			$sum_ref->{$timestamp}->{$$row{SERVICE_NAME}}->{VALUE} += $$row{$name};
			$sum_ref->{$timestamp}->{$$row{SERVICE_NAME}}->{COUNT}++;
			$comp_ref->{$$row{SERVICE_NAME}}->{VALUE} += $$row{$name};
			$comp_ref->{$$row{SERVICE_NAME}}->{COUNT}++;
		}

	}

	foreach my $interval (keys %{$sum_ref}) {
		foreach my $comp (keys %{$sum_ref->{$interval}}) {
			$sum_ref->{$interval}->{$comp}->{VALUE} = $sum_ref->{$interval}->{$comp}->{VALUE} / $sum_ref->{$interval}->{$comp}->{COUNT};
		}
	}
	foreach my $comp (keys %{$comp_ref}) {
			$comp_ref->{$comp}->{VALUE} =  $comp_ref->{$comp}->{VALUE} / $comp_ref->{$comp}->{COUNT};
	}

	$barcount = 0;
	for ($i = $start_year; $i <= $end_year; $i++) {
		#print STDERR "startyr=$start_year, endyr=$end_year, sum=$sum_year{2004}\n";
		if ($i==$start_year) {
			$start_tmp=$start_month;
		} else {
			$start_tmp = 1;
		}
		if ($i==$end_year) {
			$end_tmp=$end_month;
		} else {
			$end_tmp = 12;
		}
		for ($j = $start_tmp; $j <= $end_tmp; $j++) {
			$key = sprintf "%04d\-%02d",$i,$j;
#			if (!$sum_month{$key}) {
#				$sum_month{$key} = $NoData;
#			}
#			$printdates{$key} = qw(Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec)[$j-1];
			foreach my $comp (keys %{$comp_ref}) {
				if (!exists($sum_ref->{$key}->{$comp}->{VALUE})) {
					$sum_ref->{$key}->{$comp}->{VALUE} = $NoData;
				}
			}
			$sum_ref->{$key}->{ALL}->{PRINTDATES} = qw(Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec)[$j-1]; 
			$barcount++;
		}
	}
	return (\%{$sum_ref},\%{$comp_ref});
}

sub get_yearly2 {
	my $component=shift;
	my $name=shift;
	my $sum_ref=undef;
	my $comp_ref=undef;
	while ($row=$sth->fetchrow_hashref()) { 
		$timestamp = $$row{DATESTAMP};
		if ($timestamp =~ /(\d\d\d\d)\-(\d\d)\-(\d\d)/) {
			$cur_month = $2;
			$cur_day = $3;
			$cur_year = $1;
		} else {
			next;
		}
		$timestamp = $cur_year;
		$sum_ref->{$timestamp}->{ALL}->{VALUE} += $$row{$name};
		$sum_ref->{$timestamp}->{ALL}->{COUNT}++;
		$comp_ref->{ALL}->{VALUE} += $$row{$name};
		$comp_ref->{ALL}->{COUNT}++;
		if ($component eq "hostgroup") {
			$sum_ref->{$timestamp}->{$$row{HOSTGROUP_NAME}}->{VALUE} += $$row{$name};
			$sum_ref->{$timestamp}->{$$row{HOSTGROUP_NAME}}->{COUNT}++;
			$comp_ref->{$$row{HOSTGROUP_NAME}}->{VALUE} += $$row{$name};
			$comp_ref->{$$row{HOSTGROUP_NAME}}->{COUNT}++;
		} elsif ($component eq "host") {
			$sum_ref->{$timestamp}->{$$row{HOST_NAME}}->{VALUE} += $$row{$name};
			$sum_ref->{$timestamp}->{$$row{HOST_NAME}}->{COUNT}++;
			$comp_ref->{$$row{HOST_NAME}}->{VALUE} += $$row{$name};
			$comp_ref->{$$row{HOST_NAME}}->{COUNT}++;
		} elsif ($component eq "hostservice") {
			$sum_ref->{$timestamp}->{$$row{HOST_NAME}.":".$$row{SERVICE_NAME}}->{VALUE} += $$row{$name};
			$sum_ref->{$timestamp}->{$$row{HOST_NAME}.":".$$row{SERVICE_NAME}}->{COUNT}++;
			$comp_ref->{$$row{HOST_NAME}.":".$$row{SERVICE_NAME}}->{VALUE} += $$row{$name};
			$comp_ref->{$$row{HOST_NAME}.":".$$row{SERVICE_NAME}}->{COUNT}++;
		} elsif ($component eq "service") {
			$sum_ref->{$timestamp}->{$$row{SERVICE_NAME}}->{VALUE} += $$row{$name};
			$sum_ref->{$timestamp}->{$$row{SERVICE_NAME}}->{COUNT}++;
			$comp_ref->{$$row{SERVICE_NAME}}->{VALUE} += $$row{$name};
			$comp_ref->{$$row{SERVICE_NAME}}->{COUNT}++;
		}

	
	
	}

	foreach my $interval (keys %{$sum_ref}) {
		foreach my $comp (keys %{$sum_ref->{$interval}}) {
			$sum_ref->{$interval}->{$comp}->{VALUE} = $sum_ref->{$interval}->{$comp}->{VALUE} / $sum_ref->{$interval}->{$comp}->{COUNT};
		}
	}
	foreach my $comp (keys %{$comp_ref}) {
			$comp_ref->{$comp}->{VALUE} =  $comp_ref->{$comp}->{VALUE} / $comp_ref->{$comp}->{COUNT};
	}

	$barcount = 0;
	for ($i = $start_year; $i <= $end_year; $i++) {
		$key = $i;
#		if (!$sum_year{$i}) {
#			$sum_year{$i}=$NoData;
#		} 
#		$printdates{$i} = $i;
		foreach my $comp (keys %{$comp_ref}) {
			if (!exists($sum_ref->{$key}->{$comp}->{VALUE})) {
				$sum_ref->{$key}->{$comp}->{VALUE} = $NoData;
			}
		}
		$barcount++;
		$sum_ref->{$key}->{ALL}->{PRINTDATES} = $i; 
	}
	return (\%{$sum_ref},\%{$comp_ref});
}


sub print_graph_bars{
	use GD::Graph::bars;
	my $my_graph = GD::Graph::bars->new(600,250);
	$i=0;
	foreach $var (@_) {
		$graphtable[$i] = $var;
		$i++;
	}
	$my_graph->set( 
			'dclrs'         => [ qw(#EB6232 #F3B50F #7E87B7) ],
			x_label         => $x_label,
			y_label         => "Number",
			title           => "",
			y_tick_number   => 8,
			y_label_skip    => 2,
			x_label_skip    => $x_label_skip,
			bar_spacing     => 1,
			shadow_depth    => 0,
			bgclr => "#e6e6e6",
			accent_treshold => 200,
			transparent     => 0,
			) or warn $my_graph->error;
#    $my_graph->set_legend('Outage Measurement');
	my $my_image=$my_graph->plot(\@graphtable);
    save_chart($my_image, "$graphdirectory/$graphfile");
	return;
}

sub print_graph_bars_stacked{
	use GD::Graph::bars;
#	my $my_graph = GD::Graph::bars->new(300,150);
	my $my_graph = GD::Graph::bars->new(600,250);

	$my_graph->set( 
			'dclrs'         => [ qw(#8DD9E0 #64A2B8 #D3DB00 #8BA016 #C0C0C0 #818181 #9BAEFF #6F76C4	#E092E3 #C05599) ],
			x_label         => $x_label,
			y_label         => "Number",
			title           => "",
			y_tick_number   => 8,
			y_label_skip    => 2,
			x_label_skip    => $x_label_skip,
			bar_spacing     => 1,
			shadow_depth    => 0,
			cumulate         => 2,
			accent_treshold => 200,
			transparent     => 0,
			borderclrs		=> undef,
			bgclr => "#e6e6e6",
			) or warn $my_graph->error;


#    $my_graph->set_legend(@legend);
	my $my_image=$my_graph->plot(\@data);
	save_chart($my_image, "$graphdirectory/$graphfile");
	return;
}




sub print_graph_lines {
	use GD::Graph::lines;
#	my $my_graph = GD::Graph::lines->new(300,150);
	my $my_graph = GD::Graph::lines->new(600,250);
	$i=0;
	foreach $var (@_) {
		$graphtable[$i] = $var;
		$i++;
	}
#	$graphtable[0] = shift;
#	$graphtable[1] = shift;
	$my_graph->set( 
			'dclrs'         => [ qw(#EB6232 #F3B50F #7E87B7) ],
			x_label         => $x_label,
			y_label         => "Number",
			title           => "",
			#y_max_value => 40,
			y_tick_number => 8,
			y_label_skip => 2,
			x_label_skip    => $x_label_skip,
			box_axis => 0,
			line_width => 2,
			bgclr => "#e6e6e6",
			transparent => 0,
			);
   # $my_graph->set_legend('Outage Measurement');
	my $my_image=$my_graph->plot(\@graphtable);
	save_chart($my_image, "$graphdirectory/$graphfile");
	return;
}

sub print_graph_lines_stacked{
	use GD::Graph::area;
	my $my_graph = GD::Graph::area->new(600,250);
	$my_graph->set( 
			'dclrs'         => [ qw(#8DD9E0 #64A2B8 #D3DB00 #8BA016 #C0C0C0 #818181 #9BAEFF #6F76C4	#E092E3 #C05599) ],
			x_label         => $x_label,
			y_label         => "Number",
			title           => "",
			y_tick_number   => 8,
			y_label_skip    => 2,
			x_label_skip    => $x_label_skip,
			bar_spacing     => 1,
			shadow_depth    => 0,
			cumulate         => 2,
			accent_treshold => 200,
			transparent     => 0,
			borderclrs		=> undef,
			bgclr => "#e6e6e6",
			line_width => 2,
			) or warn $my_graph->error;

#    $my_graph->set_legend(@legend);
	my $my_image=$my_graph->plot(\@data);
	save_chart($my_image, "$graphdirectory/$graphfile");
	return;
}

sub save_chart
{
	my $chart = shift or die "Need a chart!";
	my $name = shift or die "Need a name!";
	local(*OUT);
	my $ext = "png";
	open(OUT, ">$name") or 
		die "Cannot open $name.$ext for write: $!";
	binmode OUT;
	print OUT $chart->$ext();
	close OUT;
}

sub format_output_number {
	my $type=shift;
	my $value=shift;
	if ($value eq $NoData) {
		return $value;
	} else {
		if ($type =~ /PERCENT/i) {
			if (($value - int($value)) == 0)  {
				return sprintf "%0d\%",$value;
			} else {
				return sprintf "%0.4f\%",$value;	# fraction
			}
		} else {
			if (($value-int($value))>0)  {
				$value= sprintf "%0.2f",$value;		# fraction
			} 
			return commify($value);
		}
	}
}
sub commify {
    my $text = reverse sprintf "%0d",$_[0];
    $text =~ s/(\d\d\d)(?=\d)(?!\d*\.)/$1,/g;
    return scalar reverse $text;
}


1;
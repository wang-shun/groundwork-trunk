#!/usr/local/groundwork/bin/perl --
#
#	Copyright 2003-2004 Groundwork Open Source Solution.
#	http://www.itgroundwork.com
#
#	Unless required by applicable law or agreed to in writing, software
#	distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
#	WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
#	License for the specific language governing permissions and limitations under
#	the License.
#
use DBI;
use Time::Local;
use GD::Graph::colour qw(:colours :lists :files :convert);

sub print_top_components	{
	my $name=shift;
	my $find_comp=shift;
	my $start= "$start_year-$start_month-$start_day";
	my $end= "$end_year-$end_month-$end_day";
	my $query = "SELECT * FROM `measurements` where (name='$name' and timestamp>='$start' and timestamp<='$end' and component like '$find_comp:%') ";
	$sth = $dbh->prepare($query);
	$sth->execute() or die  $@;
	%measurement_ref = ();
	while ($row=$sth->fetchrow_hashref()) { 
		$timestamp = $$row{timestamp};
		$measurement_ref{$$row{component}} += $$row{measurement};
	}
	@sorted_components = sort { $measurement_ref{$b} <=> $measurement_ref{$a} } keys %measurement_ref;
	$table_print_string = undef;

	if ($#sorted_components>9) {	# set top number in list
		$#sorted_components=9;
	}
	$colorcount=1;
	foreach $comp (@sorted_components) {
		if ($comp=~ /$find_comp:\s*(.*)/) {
			$tmp = $1;
		} else {
			$tmp=$comp;
		}
#		$table_print_string .= "<tr><td>$tmp</td><td align=center>$measurement_ref{$comp}</td>";
		$styleclass= sprintf "chart%02d",$colorcount+3;
#		$table_print_string .= "<tr><td class=insightcenter bgcolor=$Colors[$colorcount-1]><B>$colorcount</B></td><td class=insight>$tmp</td><td class=insightcenter>$measurement_ref{$comp}</td>";
		$table_print_string .= "<tr><td class=$styleclass style='border: 0px none ;' align=center>$colorcount</td>";
		$table_print_string .= "<td class=tableFill02>$tmp</td>";
		$table_print_string .= "<td class=tableFill02 align=center>$measurement_ref{$comp}</td>";
		$colorcount++;
	}
	return $table_print_string;
}




sub get_alarms_data {
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
	$table_print_string .= "
		<td class=tableHeaderFlexWidth>Measurement</td>
		<td class=tableHeaderFlexWidth>Current $tmp</td>
		<td class=tableHeaderFlexWidth>Last $tmp</td>
		<td class=tableHeaderFlexWidth>Minimum</td>
		<td class=tableHeaderFlexWidth>Maximum</td>
		<td class=tableHeaderFlexWidth>Average</td>
		<td class=tableHeaderFlexWidth># of Samples</td>
		</TR>
		";
	($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
	if ($FORM_DATA{interval} eq "weekly") {
		$current_div=$wday+1; # use day of week ($wday) from current time calculation at top of cgi
		$last_div=7;
		$div=7;
	} elsif ($FORM_DATA{interval} eq "monthly") {
		$current_div=$mday;
		$last_div=(31,28,31,30,31,30,31,31,30,31,30,31)[($mon+1)%12];
		$div=365/12;	# use this approximation for the number of days in a month
	} elsif ($FORM_DATA{interval} eq "yearly") {
		$current_div=$yday;
		if (($year % 4) == 0) {
			$last_div=366;
		} else {
			$last_div=365;
		}
		$div = ((365*3)+366)/4 ; 	# use this approximation for the number of year including leap years
	} else {
		$current_div=1;
		$last_div=1;
		$div=1;
	}

	($current,$last,$min,$max,$avg,$samplecount,%mhgvalues) = get_data("nagios managed hostgroups","all",$FORM_DATA{interval});
	if ($current=~/\d+/) {
		$current=sprintf "%0.2f",$current/$current_div; # use day of week ($wday) from current time calculation at top of cgi
	}
	if ($last=~/\d+/) {
		$last=sprintf "%0.2f",$last/$last_div;
	}
	if ($min=~/\d+/) {
		$min=sprintf "%0.2f",$min/$div;
	}
	if ($max=~/\d+/) {
		$max=sprintf "%0.2f",$max/$div;
	}
	if ($avg=~/\d+/) {
		$avg=sprintf "%0.2f",$avg/$div;
	}
	$table_print_string .=  "
			 <TR>
			<td class=tableFill03>Managed Host Groups (per day)</td>
			<td class=tableFill03 align=center>$current</td>
			<td class=tableFill03 align=center>$last</td>
			<td class=tableFill03 align=center>$min </td>
			<td class=tableFill03 align=center>$max</td>
			<td class=tableFill03 align=center>$avg </td>
			<td class=tableFill03 align=center>$samplecount</td>
			</tr>
	";

	($current,$last,$min,$max,$avg,$samplecount,%mhvalues) = get_data("nagios managed hosts","all",$FORM_DATA{interval});
	if ($current=~/\d+/) {
		$current=sprintf "%0.2f",$current/$current_div; # use day of week ($wday) from current time calculation at top of cgi
	}
	if ($last=~/\d+/) {
		$last=sprintf "%0.2f",$last/$last_div;
	}
	if ($min=~/\d+/) {
		$min=sprintf "%0.2f",$min/$div;
	}
	if ($max=~/\d+/) {
		$max=sprintf "%0.2f",$max/$div;
	}
	if ($avg=~/\d+/) {
		$avg=sprintf "%0.2f",$avg/$div;
	}
	$table_print_string .=  "
			<tr>
			<td class=tableFill02>Managed Hosts (per day)</td>
			<td class=tableFill02 align=center>$current</td>
			<td class=tableFill02 align=center>$last</td>
			<td class=tableFill02 align=center>$min </td>
			<td class=tableFill02 align=center>$max</td>
			<td class=tableFill02 align=center>$avg </td>
			<td class=tableFill02 align=center>$samplecount</td>
	";

	($current,$last,$min,$max,$avg,$samplecount,%mhsvalues) = get_data("nagios managed hostservices","all",$FORM_DATA{interval});
	if ($current=~/\d+/) {
		$current=sprintf "%0.2f",$current/$current_div; # use day of week ($wday) from current time calculation at top of cgi
	}
	if ($last=~/\d+/) {
		$last=sprintf "%0.2f",$last/$last_div;
	}
	if ($min=~/\d+/) {
		$min=sprintf "%0.2f",$min/$div;
	}
	if ($max=~/\d+/) {
		$max=sprintf "%0.2f",$max/$div;
	}
	if ($avg=~/\d+/) {
		$avg=sprintf "%0.2f",$avg/$div;
	}
	$table_print_string .=  "
			<TR>
			<td class=tableFill03>Managed Host-Services (per day)</td>
			<td class=tableFill03 align=center>$current</td>
			<td class=tableFill03 align=center>$last</td>
			<td class=tableFill03 align=center>$min </td>
			<td class=tableFill03 align=center>$max</td>
			<td class=tableFill03 align=center>$avg </td>
			<td class=tableFill03 align=center>$samplecount</td>
	";

	$rowclass0="tableFill01";		# used for alternating colors for sublists
	$rowclass1="tableFill02";		# used for alternating colors for sublists
	$rowclass2="tableFill03";
	($current,$last,$min,$max,$avg,$samplecount,%values1) = get_data("nagios alerts","all",$FORM_DATA{interval});
	if ($FORM_DATA{component} ne "all") {
		$rowclass = $rowclass0;
	} else {
		$rowclass = $rowclass1;
	}
	$table_print_string .=  "
				<tr>
				<td class=$rowclass>Total Alarms</td>
				<td class=$rowclass align=center>$current</td>
				<td class=$rowclass align=center>$last</td>
				<td class=$rowclass align=center>$min </td>
				<td class=$rowclass align=center>$max</td>
				<td class=$rowclass align=center>$avg </td>
				<td class=$rowclass align=center>$samplecount</td>
	";

	if ($FORM_DATA{component} ne "all") {
		$rowclass = $rowclass1;
		foreach $comp (sort @components) {
			($current,$last,$min,$max,$avg,$samplecount,%values1) = get_data("nagios alerts","$comp",$FORM_DATA{interval});
			%{$stackgraphdata->{$FORM_DATA{component}}->{$comp}}=%values1;
			if ($rowclass eq $rowclass1) {
				$rowclass = $rowclass2;
			} else {
				$rowclass = $rowclass1;
			}
			$table_print_string .=  "
				<tr>
				<td class='$rowclass'>$comp</td>
				<td class='$rowclass' align=center>$current</td>
				<td class='$rowclass' align=center>$last</td>
				<td class='$rowclass' align=center>$min </td>
				<td class='$rowclass' align=center>$max</td>
				<td class='$rowclass' align=center>$avg </td>
				<td class='$rowclass' align=center>$samplecount</td>
			";
		}
	} else {
		foreach $key (keys %values1) {
			$stackgraphdata->{"all"}->{"Total Alarms"}->{$key}=$values1{$key};
		}
	}

#	$table_print_string .=  "<tr>";
	($current,$last,$min,$max,$avg,$samplecount,%values2) = get_data("nagios warnings","all",$FORM_DATA{interval});
	if ($FORM_DATA{component} ne "all") {
		$rowclass = $rowclass0;
	} else {
		$rowclass = $rowclass2;
	}
	$table_print_string .=  "
		<tr>
		<td class=$rowclass>Total Warnings</td>
		<td class=$rowclass align=center>$current</td>
		<td class=$rowclass align=center>$last</td>
		<td class=$rowclass align=center>$min </td>
		<td class=$rowclass align=center>$max</td>
		<td class=$rowclass align=center>$avg </td>
		<td class=$rowclass align=center>$samplecount</td>
		";
	if ($FORM_DATA{component} ne "all") {
		$rowclass = $rowclass1;
		foreach $comp (sort @components) {
			($current,$last,$min,$max,$avg,$samplecount,%values2) = get_data("nagios warnings","$comp",$FORM_DATA{interval});
			if ($rowclass eq $rowclass1) {
				$rowclass = $rowclass2;
			} else {
				$rowclass = $rowclass1;
			}
			$table_print_string .=  "
				<tr>
				<td class=$rowclass>$comp</td>
				<td class=$rowclass align=center>$current</td>
				<td class=$rowclass align=center>$last</td>
				<td class=$rowclass align=center>$min </td>
				<td class=$rowclass align=center>$max</td>
				<td class=$rowclass align=center>$avg </td>
				<td class=$rowclass align=center>$samplecount</td>
			";
		}
	}

#	$table_print_string .=  "<tr>";
	($current,$last,$min,$max,$avg,$samplecount,%values3) = get_data("nagios notifications","all",$FORM_DATA{interval});
	if ($FORM_DATA{component} ne "all") {
		$rowclass = $rowclass0;
	} else {
		$rowclass = $rowclass1;
	}
	$table_print_string .=  "
		<tr>
		<td class=$rowclass>Total Notifications</td>
		<td class=$rowclass align=center>$current</td>
		<td class=$rowclass align=center>$last</td>
		<td class=$rowclass align=center>$min </td>
		<td class=$rowclass align=center>$max</td>
		<td class=$rowclass align=center>$avg </td>
		<td class=$rowclass align=center>$samplecount</td>
		";
	if ($FORM_DATA{component} ne "all") {
		$rowclass = $rowclass1;
		foreach $comp (sort @components) {
			($current,$last,$min,$max,$avg,$samplecount,%values3) = get_data("nagios notifications","$comp",$FORM_DATA{interval});
			if ($rowclass eq $rowclass1) {
				$rowclass = $rowclass2;
			} else {
				$rowclass = $rowclass1;
			}
			$table_print_string .=  "
				<tr>
				<td class=$rowclass>$comp</td>
				<td class=$rowclass align=center>$current</td>
				<td class=$rowclass align=center>$last</td>
				<td class=$rowclass align=center>$min </td>
				<td class=$rowclass align=center>$max</td>
				<td class=$rowclass align=center>$avg </td>
				<td class=$rowclass align=center>$samplecount</td>
			";
		}
	}
	$table_print_string .=  "</table>";
	return;
}


sub print_alarm_trend_chart {
	@array_keys_ref = sort keys %values1;
	@array_values1_ref = ();
	@array_values2_ref = ();
	@array_values3_ref = ();
	$graphfile = "alarmcount_graph.png";
	foreach $key (@array_keys_ref) {
		push @array_values1_ref, $values1{$key};
		push @array_values2_ref, $values2{$key};
		push @array_values3_ref, $values3{$key};
	}
	if ($barcount <60) {
		if ($barcount < 8) {
			$x_label_skip = 1;
		} elsif ($barcount < 15) {
			$x_label_skip = 3;
		} else {
			$x_label_skip = $barcount;
		}
		print_graph_bars(\@array_keys_ref, \@array_values1_ref,\@array_values2_ref,\@array_values3_ref);
	} else {
		$x_label_skip = $barcount;
		print_graph_lines(\@array_keys_ref, \@array_values1_ref,\@array_values2_ref,\@array_values3_ref);
	}
	print "<table class='data' border='0' cellpadding='5' cellspacing='1' width='100%'>";
	print "<tbody><tr class='tableHeader'>";
	print "<td>Trend Chart - Total Alarms, Warnings and Notifications</td>";
	$time=time;
	print "<tr>";
	print "<td class=tableFill03>
			<table cellpadding='2' cellspacing='1'>
			<tbody>
			<tr><td style='border: 0px none ;'><img src='$graphhtmlref/spacer.gif' border='0' height='20' width='1'></td></tr>
			<tr><td style='border: 0px none ;' rowspan='3'>
			<IMG border=0 src='$graphhtmlref/$graphfile?$time' border='0' hspace='20'></td>
			<td style='border: 0px none ;' valign='top'><img src='$graphhtmlref/01_002.gif' border='0' height='16' hspace='5' width='17'></td>
			<td style='border: 0px none ;' valign='top'>Alarms</td></tr>
			
		<tr><td style='border: 0px none ;' valign='top'><img src='$graphhtmlref/02.gif' border='0' height='16' hspace='5' width='17'></td>
			<td style='border: 0px none ;' valign='top'>Warnings</td></tr>	
			
		<tr><td style='border: 0px none ;' valign='top'><img src='$graphhtmlref/03.gif' border='0' height='16' hspace='5' width='17'></td>
			<td style='border: 0px none ;' valign='top'>Notifications</td></tr>	
		<tr><td style='border: 0px none ;'><img src='$graphhtmlref/spacer.gif' border='0' height='40' width='1'></td></tr>
	</tbody></table></table>				
			";
	return;
}


sub print_trend_chart_component {
	$name = shift;
	$component=shift;
	foreach $comp (@sorted_components) {	# @sorted_components from print_top_components routine
#		print "<br>Component = $comp";
		($current,$last,$min,$max,$avg,$samplecount,%values) = get_data("$name","$comp",$FORM_DATA{interval});
		%{$stackgraphdata->{$component}->{$comp}}=%values;
	}
	@array_keys_ref = sort keys %values;
	@data=();
	@print_array_keys_ref = ();
#	push @data,\@array_keys_ref;	# push time stamps
	foreach $key (@array_keys_ref ) {
		if ($printdates{$key}) {
			push @print_array_keys_ref,$printdates{$key} ;		# Set the date on the graph to the formated date
		} else {
			push @print_array_keys_ref,$key ;
		}
	}
	push @data,\@print_array_keys_ref;	# push time stamps

	@legend=();
	$barcount=$#array_keys_ref;
#	foreach $key (keys %{$stackgraphdata->{$component}}) {		# key=component, ie a host group, host, service
	foreach $key (@sorted_components) {		# key=component, ie a host group, host, service
		if ($key=~/(.*?):(.*)/) {
			$tmp=$2;
		} else {
			$tmp=$key;
		}
		push @legend,$tmp;
		@{$tmparray->{$key}}=();
		foreach $key2 (@array_keys_ref)	{			# key2=timestamps, i.e., key of hash values
			if ($stackgraphdata->{$component}->{$key}->{$key2} eq $NoData) {
				$stackgraphdata->{$component}->{$key}->{$key2}=0	;
			} 
			push @{$tmparray->{$key}},$stackgraphdata->{$component}->{$key}->{$key2}	;
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



sub print_hlist_notifications {
	my $component = shift;
	my $start= "$start_year-$start_month-$start_day";
	my $end= "$end_year-$end_month-$end_day";
	$contact_ref = undef;
	foreach $name (("nagios notifications CRITICAL","nagios notifications DOWN","nagios notifications UNREACHABLE","nagios notifications WARNING")) {
		my $query = "SELECT * FROM measurements WHERE (name='$name' and component like '$component:%' ".
				"and timestamp>='$start' and timestamp<='$end') ORDER BY timestamp";
		$sth = $dbh->prepare($query);
		$sth->execute() or die  $@;
		while ($row=$sth->fetchrow_hashref()) { 
			if ($$row{component} =~ /^$component:(.*)/) {
				$contact_ref->{$1}->{RED} += $$row{measurement};
				$contact_ref->{$1}->{TOTAL} += $$row{measurement};
			}
		}
	}
	
	# Stacked horizontal graphs don't seem to work, so we will put Warnings in the RED category
	# Count yellow graph items
#	$yellowcount = 0;
#	foreach $name (("nagios notifications WARNING")) {
#		my $query = "SELECT * FROM measurements WHERE (name='$name' ".
#				"and timestamp>='$start' and timestamp<='$end') ORDER BY timestamp";
#		$sth = $dbh->prepare($query);
#		$sth->execute() or die  $@;
#		while ($row=$sth->fetchrow_hashref()) { 
#			if ($$row{component} =~ /^contact:(.*)/) {
#				$contact_ref->{$1}->{YELLOW} += $$row{measurement};
#				$contact_ref->{$1}->{TOTAL} += $$row{measurement};
#			}
#		}
#	}
	# Count green graph items
	$greencount = 0;
	foreach $name (("nagios notifications UP","nagios notifications OK")) {
		my $query = "SELECT * FROM measurements WHERE (name='$name' and component like '$component:%' ".
				"and timestamp>='$start' and timestamp<='$end') ORDER BY timestamp";
		$sth = $dbh->prepare($query);
		$sth->execute() or die  $@;
		while ($row=$sth->fetchrow_hashref()) { 
			if ($$row{component} =~ /^$component:(.*)/) {
				$contact_ref->{$1}->{GREEN} += $$row{measurement};
				$contact_ref->{$1}->{TOTAL} += $$row{measurement};
			}
		}
	}

	@data = ();
	@legend=();
	@greenarray=();
	@redarray=();
	@yellowarray=();
	@contactarray=();
	@sorted_components = sort { $contact_ref->{$b}->{TOTAL} <=> $contact_ref->{$a}->{TOTAL} } keys %{$contact_ref};
#	if ($#sorted_components == 0) {
#		return ;
#	}

#	push @data,\@sorted_components;	# push contact names
#	print "<br>Contact list";
	$y_max = 0; $y_min = 0;
	foreach $key (@sorted_components) {		# key=contact name
#		push @greenarray, $contact_ref->{$key}->{GREEN} * -1;	# make negative for horizontal graph	
		if ($y_min < $contact_ref->{$key}->{GREEN} ) {
			$y_min = $contact_ref->{$key}->{GREEN};
		}
		if ($y_max < $contact_ref->{$key}->{RED} ) {
			$y_max = $contact_ref->{$key}->{RED};
		}
		push @greenarray, $contact_ref->{$key}->{GREEN} * -1;
		push @redarray, $contact_ref->{$key}->{RED} ;			
#		push @yellowarray, $contact_ref->{$key}->{YELLOW} ;			
#		push @contactarray, "$key ".sprintf("%01d",$contact_ref->{$key}->{GREEN})."/".sprintf("%01d",$contact_ref->{$key}->{RED})."/".sprintf("%01d",$contact_ref->{$key}->{YELLOW}) ;
		push @contactarray, "$key (".sprintf("%01d",$contact_ref->{$key}->{GREEN})."/".sprintf("%01d",$contact_ref->{$key}->{RED}).")" ;
#		print "<br>$key green=".$contact_ref->{$key}->{GREEN}." red=".$contact_ref->{$key}->{RED}." yellow=".$contact_ref->{$key}->{YELLOW};
	}
	$y_min = -1 * $y_min;
	push @data,\@contactarray;	# push contact names
	push @data,\@greenarray;
#	push @data,\@yellowarray;
	push @data,\@redarray;
	@legend = ("UP,OK", "DOWN,CRITICAL,UNREACHABLE,WARNING"); 
	$x_label= "Contact (green/red)";
	$y_label = "Number of Notifications";
	print_graph_hbars_stacked();
	return "OK";
}





sub print_pie_chart_component {
	my $name = shift;
	my $component = shift;
	my %component_ref = ();
	my $start= "$start_year-$start_month-$start_day";
	my $end= "$end_year-$end_month-$end_day";
	my $query = "SELECT * FROM measurements WHERE (name='$name' and component like '$component:%' ".
			"and timestamp>='$start' and timestamp<='$end') ";
	$sth = $dbh->prepare($query);
	$sth->execute() or die  $@;
	while ($row=$sth->fetchrow_hashref()) { 
		if ($$row{component} =~ /^$component:(.*)/) {
			$component_ref{$1} += $$row{measurement};
		}
	}

	@data = ();
	@legend=();
	@values = ();
	@sorted_components = sort { $component_ref{$b} <=> $component_ref{$a} } keys %component_ref;
	foreach $key (@sorted_components) {		# key=component, ie a host group, host, service
		push @legend,$key;
		push @values,$component_ref{$key} ;
	}
	push @data,\@sorted_components;		# push names
	push @data,\@values;

	print_pie_chart();
	return "OK";
}



sub print_detail_table {
	# Print sample data
	print "<br>";
	print "<table class=insight>".
		"<tr><th colspan=7 class=insight>Detail Data Table</th>".
		"<tr><th class=insight>Period</th>".
		"<th class=insight>Managed Host Groups</th><th>Managed Hosts</th><th>Managed Host / Service</th>".
		"<th class=insight>Alarm Count</th><th>Warnings Count</th><th>Notifications Count</th>";
	foreach $tmp  (sort keys %values1) {
		print "<tr class=insight><td>$tmp</td>";
		print "<td align=center class=insight>$mhgvalues{$tmp}</td>";
		print "<td align=center class=insight>$mhvalues{$tmp}</td>";
		print "<td align=center class=insight>$mhsvalues{$tmp}</td>";
		print "<td align=center class=insight>$values1{$tmp}</td>";
		print "<td align=center class=insight>$values2{$tmp}</td>";
		print "<td align=center class=insight>$values3{$tmp}</td>";
	}
	print "</table>";
	return;
}


sub get_components {
	my $find_comp=shift;
	my %match = ();
	my $query = "SELECT component FROM `measurements` where component like '$find_comp:%' ";
#	print "<br>$query\n";
	$sth = $dbh->prepare($query);
	$sth->execute() or die  $@;
	while ($row=$sth->fetchrow_hashref()) { 
		$match{$$row{component}} = 1;
	}
	return keys %match;
}

sub get_data {
	my $name=shift;
	my $component=shift;
	my $interval=shift;
	my @period = ();
	my @values = ();
	my $start= "$start_year-$start_month-$start_day";
	my $end= "$end_year-$end_month-$end_day";
	my $query = "SELECT * FROM measurements WHERE (name='$name' and component='$component' ".
				"and timestamp>='$start' and timestamp<='$end') ORDER BY timestamp";
#	print "<br>$query\n";
	$sth = $dbh->prepare($query);
	$sth->execute() or die  $@;
	if ($interval eq "weekly") {
		%sums=get_weekly();
		$x_label="Week";
	} elsif ($interval eq "monthly") {
		%sums=get_monthly();
		$x_label="Month";
	} elsif ($interval eq "yearly"){
		%sums=get_yearly();
		$x_label="Year";
	} else {	# then daily
		%sums=get_daily();
		$x_label="Day";
	}

	$min = undef; ; $max = undef; $total=0; $samplecount=0;
	foreach $key (sort keys %sums) {
			$x = $sums{$key};
			$last = $current;
			$current = $x;
			if ((!$x) or ($x eq $NoData)) { next;}
			if ($min eq undef) {$min = $x;}
			if ($max eq undef) {$max = $x;}
			$total += $x;
			if ($x < $min)  {$min = $x;}
			if ($x > $max ) {$max = $x;}
			$samplecount++;
	}
	if ($min eq undef) {$min=$NoData;	}
	if ($max eq undef) {$max=$NoData;	}
	if ($samplecount>0) {
		$avg = sprintf "%.2f",$total / $samplecount;
	} else {
		$avg = $NoData;
	}
	return ($current,$last,$min,$max,$avg,$samplecount,%sums) ;
}

sub get_data_array {
	my $name_ref=shift;
	my $component=shift;
	my $interval=shift;
	my @period = ();
	my @values = ();
	my $start= "$start_year-$start_month-$start_day";
	my $end= "$end_year-$end_month-$end_day";

	my $namestring = undef;
	foreach $name (@$name_ref) {
		$namestring .= "name='$name' or ";
	}
	$namestring =~ s/or $//;

	my $query = "SELECT * FROM measurements WHERE (($namestring) and component='$component' ".
				"and timestamp>='$start' and timestamp<='$end') ORDER BY timestamp";
#	print "<br>$query\n";
	$sth = $dbh->prepare($query);
	$sth->execute() or die  $@;
	if ($interval eq "weekly") {
		%sums=get_weekly();
		$x_label="Week";
	} elsif ($interval eq "monthly") {
		%sums=get_monthly();
		$x_label="Month";
	} elsif ($interval eq "yearly"){
		%sums=get_yearly();
		$x_label="Year";
	} else {	# then daily
		%sums=get_daily();
		$x_label="Day";
	}

	$min = undef; ; $max = undef; $total=0; $samplecount=0;
	foreach $key (sort keys %sums) {
			$x = $sums{$key};
			$last = $current;
			$current = $x;
			if ((!$x) or ($x eq $NoData)) { next;}
			if ($min eq undef) {$min = $x;}
			if ($max eq undef) {$max = $x;}
			$total += $x;
			if ($x < $min)  {$min = $x;}
			if ($x > $max ) {$max = $x;}
			$samplecount++;
	}
	if ($min eq undef) {$min=$NoData;	}
	if ($max eq undef) {$max=$NoData;	}
	if ($samplecount>0) {
		$avg = sprintf "%.2f",$total / $samplecount;
	} else {
		$avg = $NoData;
	}
	return ($current,$last,$min,$max,$avg,$samplecount,%sums) ;
}

sub get_daily {
	%sum_day = ();
	while ($row=$sth->fetchrow_hashref()) { 
		$timestamp = $$row{timestamp};
		if ($timestamp =~ /(\d\d\d\d)-(\d\d)-(\d\d)/) {
			$cur_month = $2;
			$cur_day = $3;
			$cur_year = $1;
		} else {
			next;
		}
		$sum_day{$timestamp} += $$row{measurement};
	}
	$barcount = 0;
	for ($i = $start_year; $i <= $end_year; $i++) {
		#print STDERR "startyr=$start_year, endyr=$end_year, sum=$sum_year{2004}\n";
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
			#print STDERR "Pushing $i,$j,".$sum_month{sprintf "%02d\/%04d",$j,$i}."\n";
				$key = sprintf "%04d-%02d-%02d",$i,$j,$k;
				$printdates{$key} = sprintf "%2d/%02d",$j,$k;
				if (!$sum_day{$key}) {
					$sum_day{$key} = $NoData;
				}
				$barcount++;
			}
		}
	}
	return (%sum_day);
}



sub get_weekly {
	%sum_week = ();
	while ($row=$sth->fetchrow_hashref()) { 
		$timestamp = $$row{timestamp};
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
		$sum_week{"$cur_year\-$weekofyear"} += $$row{measurement};
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
			if (!$sum_week{"$i\-$weekofyear"}) {
				$sum_week{"$i\-$weekofyear"} = $NoData;
			}
			$printdates{"$i\-$weekofyear"} = "$weekofyear";
			$barcount++;
		}
	}
	return (%sum_week);
}

sub get_monthly {
	%sum_month = ();
	while ($row=$sth->fetchrow_hashref()) { 
		$timestamp = $$row{timestamp};
		if ($timestamp =~ /(\d\d\d\d)-(\d\d)-(\d\d)/) {
			$cur_month = $2;
			$cur_day = $3;
			$cur_year = $1;
		} else {
			next;
		}
		$sum_month{"$cur_year\-$cur_month"} += $$row{measurement};
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
			#print STDERR "Pushing $i,$j,".$sum_month{sprintf "%02d\/%04d",$j,$i}."\n";
			push @{$graphtable[0]}, "$j\/$i";
			$key = sprintf "%04d\-%02d",$i,$j;
			if (!$sum_month{$key}) {
				$sum_month{$key} = $NoData;
			}
			$printdates{$key} = qw(Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec)[$j-1];
			$barcount++;
		}
	}
	return (%sum_month);
}

sub get_yearly {
	@{@graphtable}=();
	%sum_year = ();
	while ($row=$sth->fetchrow_hashref()) { 
		$timestamp = $$row{timestamp};
		if ($timestamp =~ /(\d\d\d\d)\-(\d\d)\-(\d\d)/) {
			$cur_month = $2;
			$cur_day = $3;
			$cur_year = $1;
		} else {
			next;
		}
		$sum_year{$cur_year} += $$row{measurement};
	}
	$barcount = 0;
	for ($i = $start_year; $i <= $end_year; $i++) {
		if (!$sum_year{$i}) {
			$sum_year{$i}=$NoData;
		} 
		$printdates{$i} = $i;
		$barcount++;
	}
	return (%sum_year);
}


sub print_graph_bars{
	use GD::Graph::bars;
#	my $my_graph = GD::Graph::bars->new(300,150);
	my $my_graph = GD::Graph::bars->new(600,250);

	$i=0;
	foreach $var (@_) {
		$graphtable[$i] = $var;
		$i++;
	}
#			'dclrs'         => [ qw(lred lyellow lgray) ],
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
   # $my_graph->set_legend('Alarms', 'Warnings','Notifications');
	my $my_image=$my_graph->plot(\@graphtable);
    save_chart($my_image, "$graphdirectory/$graphfile");
	return;
}

sub print_graph_bars_stacked{
	use GD::Graph::bars;
#	my $my_graph = GD::Graph::bars->new(300,150);
	my $my_graph = GD::Graph::bars->new(600,250);

#			'dclrs'         => [ qw(lred lyellow lgreen lblue pink green cyan gold	
#                                   purple orange) ],

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
			bgclr => "#e6e6e6",
			borderclrs		=> undef,
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
    $my_graph->set_legend('Alarms', 'Warnings','Notifications');
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
			bgclr => "#e6e6e6",
			borderclrs		=> undef,
			line_width => 2,
			) or warn $my_graph->error;

#    $my_graph->set_legend(@legend);
	my $my_image=$my_graph->plot(\@data);
	save_chart($my_image, "$graphdirectory/$graphfile");
	return;
}

sub print_graph_hbars_stacked{
	use GD::Graph::bars;
	use GD::Graph::hbars;
	my $my_graph = GD::Graph::hbars->new(600,250);
#	my $my_graph = GD::Graph::hbars->new(750,300);
#			'dclrs'         => [ qw(lgreen lred) ],
	$my_graph->set( 
			'dclrs'         => [ qw(#8DD9E0 #C05599) ],
			x_label         => $x_label,
			y_label         => $y_label,
			title           => "",
			y_tick_number   => 10,
			y_label_skip    => 1,
			x_label_skip    => 0,
			overwrite       => 1, 
			cumulate         => 0,
			axislabelclr        => 'black',
			legend_placement    => 'CT',
			zero_axis_only      => 0,
			bgclr => "#e6e6e6",
			x_label_position    => 1/2,
			transparent         => 0,
			) or 
		warn $my_graph->error;

#			y_max_value     => $y_max,
#			y_min_value     => $y_min,
#			y_number_format     => \&y_format,

#			bar_spacing     => 1,
#			shadow_depth    => 0,
#			cumulate         => 2,
#			accent_treshold => 200,
#			transparent     => 0,
#			borderclrs		=> undef,

    $my_graph->set_legend(@legend);
	my $my_image=$my_graph->plot(\@data);
	save_chart($my_image, "$graphdirectory/$graphfile");
	return;
}

sub print_pie_chart{
	use GD::Graph::pie;
	$my_graph = new GD::Graph::pie( 250, 250 );
	#$my_graph = new GD::Graph::pie( );

	$my_graph->set( 
		'dclrs'         => [ qw(#8DD9E0 #64A2B8 #D3DB00 #8BA016 #C0C0C0 #818181 #9BAEFF #6F76C4	#E092E3 #C05599) ],
		title => '',
		label => '',
		axislabelclr => 'black',
		pie_height => 36,
		l_margin => 15,
		r_margin => 15,
		start_angle => 15,
		bgclr => "#e6e6e6",
		transparent => 0,
	);

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





1;
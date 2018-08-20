#!/usr/local/groundwork/bin/perl --
#
# $Id: $
#
# PerfChart - Groundwork Performance Charts
# perfchart.cgi
#
###############################################################################
# Release 2.0
# 9-Mar-2006
###############################################################################
# Author: Scott Parris
#
# Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
# All rights reserved. This program is free software; you can redistribute it
# and/or modify it under the terms of the GNU General Public License version 2
# as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE.  See the GNU General Public License for 
# more details.
#
# You should have received a copy of the GNU General Public License along with
# this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
# Street, Fifth Floor, Boston, MA 02110-1301, USA.
#



use strict;
use CGI;
use URI::Escape;
use XML::LibXML;
use RRDs;
use lib qq(/usr/local/groundwork/performance/lib);
use lib qq(/usr/local/groundwork/monarch/lib);
#Added from process_service_perfdata
use Time::HiRes;
use DBI;
use CollageQuery;

use MonarchStorProc;
use PerfChartsForms;
use CollageQuery;
use Time::Local;
$|++;

my $debug = 0;
my $is_portal = 0;
my $query = new CGI;
my %hidden = ();

$hidden{'refresh_rate'} = $query->param('refresh_rate');
$hidden{'file'} = $query->param('file');
my $auto_refresh = $query->param('auto_refresh');
$hidden{'refresh_rate'} = $query->param('refresh_rate');
my $view = $query->param('view');
$view = uri_unescape($view);
$hidden{'view'} = $view;
my $object = $query->param('object');
$object = uri_unescape($object);
$hidden{'object'} = $object;
$hidden{'gen_view'} = $query->param('gen_view');
my $layout = $query->param('layout');
$hidden{'layout'} = $layout;
my $date_range = $query->param('date_range');
$hidden{'date_range'} = $date_range;
my $start_date = $query->param('start_date');
$hidden{'start_date'} = $start_date;
my $end_date = $query->param('end_date');
$hidden{'end_date'} = $end_date;
my $last_x_days = $query->param('last_x_days');
$hidden{'last_x_days'} = $last_x_days;
my $days = $query->param('days');
$hidden{'days'} = $days;
my $last_x_hours = $query->param('last_x_hours');
$hidden{'last_x_hours'} = $last_x_hours;
my $hours = $query->param('hours');
$hidden{'hours'} = $hours;
my $body = undef;

my $rrd_dir = '/usr/local/groundwork/rrd';
my $rrd_bin = '/usr/local/groundwork/bin';
my $graphdir = "/usr/local/groundwork/apache2/htdocs/performance/rrd_img";
my $view_dir = '/usr/local/groundwork/performance/performance_views';
my $cgi_bin = '/performance/cgi-bin';
my $defstring;
my @errors = ();
my @message = ();
my %rrds = ();
my %rrdtype = ();
my %hosts = ();
my %host_rrd = ();
my %host_service_rrd = ();
my %rrd_host = ();
my %host_rrd_select = ();
my %file_view = ();
my %view_file = ();

my @colors = ('#000000','#C05599','#E092E3','#6F76C4','#9BAEFF','#818181','#C0C0C0','#8BA016','#D3DB00','#64A2B8','#8DD9E0','#7E87B7','#F3B50F','#EB6232',
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
'#000000','#C05599','#E092E3','#6F76C4','#9BAEFF','#818181','#C0C0C0','#8BA016','#D3DB00','#64A2B8','#8DD9E0','#7E87B7','#F3B50F','#EB6232');

my @dschars = ('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z');
#('z', 'x', 'y', 'w', 'v', 'u', 't', 's', 'r', 'q', 'p', 'o', 'n', 'm', 'l', 'k', 'j', 'i', 'h', 'g', 'f', 'e', 'd', 'c', 'b', 'a'); 

sub customgraph($$) {
	my ($Database_Name,$Database_Host,$Database_User,$Database_Password) = CollageQuery::readGroundworkDBConfig("monarch");
	my $dbh = DBI->connect("DBI:mysql:$Database_Name:$Database_Host", $Database_User, $Database_Password)
        or die "Can't connect to database $Database_Name. Error:".$DBI::errstr;
	my $graphcgi;
	my $rrdtool = "/usr/local/groundwork/bin/rrdtool";
	my %macros = (
        	'\$RRDTOOL\$' => $rrdtool,
	);

#  pass these values in from calling program
	my $host = shift;
	my $svcdesc = shift;

# here is the selection for exact match
	my $query = "SELECT * FROM `performanceconfig` where (service='$svcdesc' and type='nagios' and enable=1) ORDER BY host";        # Order by host puts * before a hostname
	my $sth = $dbh->prepare($query);
	$sth->execute() or die  $@;
	while (my $row=$sth->fetchrow_hashref()) {
       		if (($$row{host} eq "*") or ($$row{host} eq $host )) {                  # Last row will be a specific host - if * and host are bot set. Specific host has priority
                	$graphcgi = $$row{graphcgi};
        	}
	}
# If no match, then check pattern matches
	if (!$graphcgi) {
        	#print "No exact service name $svcdesc. Query database for service pattern matches. \n" if ($debug >1);
        	$query = "SELECT * FROM `performanceconfig` where (type='nagios' and enable=1 and service_regx=1) ORDER BY service";    # Order by host puts * before a hostname
        	my $sth = $dbh->prepare($query);
	        $sth->execute() or die  $@;
	        while (my $row=$sth->fetchrow_hashref()) {
	                if (($$row{host} eq "*") or ($$row{host} eq $host )) {                  # Last row will be a specific host - if * and host are bot set. Specific host has priority
	                        if ($$row{service}) {
	                                my $serviceregx = qr/$$row{service}/;
	                                if ($svcdesc =~ /$serviceregx/) {
        	                                if ($graphcgi) {                # check if more than one pattern match
                	                                #print "Mulitple service matches. Pattern $$row{service} also matches. Ignored. \n" if ($debug >1);
                        	                        next;
                                	        }
                                        	$graphcgi = $$row{graphcgi};
	                                        #print "$svcdesc matches database service pattern $$row{service}. Using this entry. \n" if ($debug >1);
        	                        }
                	        }
	                }
	        }
	}

	if (!$graphcgi) {
        	#print "No entry in performance database for select: $query \n" if ($debug >1);
	} else {
        	#print "Match on $svcdesc and data is $graphcgi";
	}

	$sth->finish;
	$dbh->disconnect();
	return $graphcgi;
}


sub views($$$) {
	my $name = shift;
	my $file = shift;
	my $delete = shift;
	my $parser = XML::LibXML->new();
	my $tree = undef;
	my %views = ();
	my $views_out = "<?xml version=\"1.0\" ?>\n<views>";
	if (-e "$view_dir/views.xml") {
		eval {$tree = $parser->parse_file("$view_dir/views.xml")};
		push @errors, "Invalid file type: $file is not valid XML: $@" if $@;
		unless (@errors) {
			my $got_file = 0;
			my $root = $tree->getDocumentElement;
			my @nodes = $root->findnodes( "//view" );
			foreach my $node (@nodes) {
				my $fname = $node->getAttribute('name');
				my $vname = $node->textContent;
				if ($fname eq $file) {
					$got_file = 1;
					unless ($delete) {
						if ($name) { $vname = $name }
						$views_out .= "\n <view name=\"$file\"><![CDATA[$vname]]></view>";
						$view_file{$vname} = $file;
						$file_view{$file} = $vname;
					}
				} else {
					$views_out .= "\n <view name=\"$fname\"><![CDATA[$vname]]></view>";
					$view_file{$vname} = $fname;
					$file_view{$fname} = $vname;
				}
			}
			unless ($got_file) { 
				$views_out .= "\n <view name=\"$file\"><![CDATA[$name]]></view>";
				$view_file{$name} = $file;
				$file_view{$file} = $name;
			}
			$views_out .= "\n</views>";
		}
	} else {
		$views_out .= "\n <view name=\"$file\"><![CDATA[$name]]></view>\n</views>";
	}
	if ($file) {
		open(FILE, "> $view_dir/views.xml") || push @errors, "Error: Unable to write $view_dir/views.xml $!";
		print FILE $views_out;
		close (FILE);
	}
	if (@errors) { 
		foreach (@errors) { error_out("$_") }
	}
}



sub error_out($) {
	my $err = shift;
	$body .= "<h2>$err</h2><br>";
}

sub get_hosts_graphs() {
	my $connect = StorProc->dbconnect();
	%host_service_rrd = StorProc->get_host_service_rrd(); 
	my $result = StorProc->dbdisconnect();
	foreach my $host (keys %host_service_rrd) {
		my @hf = ();
		foreach my $service (keys %{$host_service_rrd{$host}}) {
			push @hf, $service;
			$hosts{$host} .= "$service,";
			$rrds{$service} .= "$host,";
		}
		$host_rrd{$host} = [ @hf ];
	}
	foreach my $key (keys %rrds) { chop $rrds{$key} }
	foreach my $host (sort keys %hosts) { chop $hosts{$host}; }
}

sub today() {
	my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
	$year = $year + 1900;
	$mon = $mon + 1;
	return "$year/$mon/$mday";
}

sub convert_date($) {
	my $date = $_[0];
	if ($date =~ /(\d\d\d\d)\/(\d+)\/(\d+)/) {
		my ($year, $mon, $day) = ($1, $2, $3);
		if ($year > 2025) { 
			$end_date =~ s/$year/2025/;
			$year = 2025;
		}
		if ($year < 1968) { 
			$start_date =~ s/$year/1968/;
			$year = 1968;
		}
		$day =~ s/^0//;
		$mon =~ s/^0//;
		if ($day > 31) { $day = 31 }
		if ($mon > 12) { $mon = 12 }
		if ($mon =~ /9|4|6|11/ && $day > 30) { 
			$day = 30;
		} elsif ($mon eq '2' && $day > 30 && $year =~ /2000|2004|2008|2012|2016|2020|2024/) { 
			$day = 29;
		}
		$mon--;
		$date = timelocal('0', '0', '0', $day, $mon, $year);
		return $date;
	} else {
		push @errors, "Invalid date $date (yyyy/mm/dd).";
	}
}

sub data_host(@) {
	my $form = undef;
	my $yeslegend = undef;
	my $type = 'hosts';
	unless ($hidden{'gen_view'}) {
		my $now = time;
		$hidden{'gen_view'} = "view_$now";
	}
	my $view = $hidden{'gen_view'};
	if ($hidden{'file'}) {
		$view = $hidden{'file'};
		$view =~ s/\.xml//;
		$hidden{'gen_view'} = $view;
	}
	if ($layout eq 'consolidated') {
		my $pretitle = "Consolidated";
		my $line = undef;
		my $def = undef;
		my $i = 0;
		my @hosts = ();
		my %legend = ();
		foreach my $host (sort keys %host_rrd_select) {
			my $hname = $host;
			$hname =~ s/\s/+/g;
			push @hosts, $hname;
			foreach my $data (@{$host_rrd_select{$host}}) {
				my $sname = $data;
				print "host $host and data $data selected\n" if $debug;
				my $file = $host_service_rrd{$host}{$data};
				my $info = qx($rrd_bin/rrdtool info $file 2>&1) || push @errors, "Error(s) executing $rrd_bin/rrdtool info $file $!";
				my %lines = ();
				my @info = split(/\n/, $info);
				foreach my $in (@info) {
					if ($in =~ /^ds\[(\S+)\]/) { $lines{$1} = 1 }
				}
				foreach my $key (sort keys %lines) {
					my $color = pop @colors;
					$legend{$host}{"$data\_$key"} = $color;
					$def .= "\"DEF:$key$i=$file:$key:AVERAGE\", ";
					$line .= "\"LINE2:$key$i$color:$key $host\", ";
					$i++;
				}
			}
		}
		if ($last_x_hours) {
			my $end = time;
			my $start = $end - ($hours * 3600);
			my $title .= "$pretitle last $hours hours";
			my $vlabel = "";
			my $defstring = "$def $line";
			$defstring =~ s/,\s$//;
			my $graph4 = "$view\_consolidated\_h_$days\.png";
			$graph4 =~ s/\/|\s/-/g;
			my $graphfile = "$graphdir/$graph4";
			my ($averages, $xsize, $ysize) = undef;
			my $evalstring = '($averages, $xsize, $ysize) = RRDs::graph("$graphfile", "-s", $start, "-e", $end, "--vertical-label", $vlabel, "--no-legend", "-t", $title, "-w", 600, "-h", 200, '.$defstring.");";
			eval($evalstring);
			error_out("$evalstring") if $debug>0;
			my $err = RRDs::error;
			if ($err) { push @errors, "$err $defstring" }
			if (@errors) { $form .= Forms->form_errors(\@errors) }
			@errors = ();
			$form .= Forms->graph($type,$graph4,\@hosts);
			$form .= Forms->legend(\%legend);
		}
		if ($last_x_days) {
			my $end = time;
			my $start = $end - ($days * 86400);
			my $title ="$pretitle last $days days";
			my $vlabel = "";
			my $defstring = "$def $line";
			$defstring =~ s/,\s$//;
			my $graph4 = "$view\_consolidated\_d_$days\.png";
			$graph4 =~ s/\/|\s/-/g;
			my $graphfile = "$graphdir/$graph4";
			my ($averages, $xsize, $ysize) = undef;
			my $evalstring = '($averages, $xsize, $ysize) = RRDs::graph("$graphfile", "-s", $start, "-e", $end, "--vertical-label", $vlabel, "--no-legend", "-t", $title, "-w", 600, "-h", 200, '.$defstring.");";
			eval($evalstring);
			my $err = RRDs::error;
			if ($err) { push @errors,  "$err $defstring" }
			if (@errors) { $form .= Forms->form_errors(\@errors) }
			@errors = ();
			$form .= Forms->graph($type,$graph4,\@hosts);
			unless ($last_x_hours) { $form .= Forms->legend(\%legend) }
		}
		if ($date_range) {
			my $start = convert_date($start_date);
			unless ($start) {
				push @errors, "Start date is invalid: $start_date (yyyy/mm/dd).";
			}
			my $end = convert_date($end_date);
			unless ($end) {
				push @errors, "End date s $start e $end is invalid: $end_date (yyyy/mm/dd).";
			}
			if ($end < $start) {
				push @errors, "End date must be newer than start date.)";
			}
			if (@errors) {
				$form .= Forms->form_errors(\@errors);
			} else {
				my $title = "$pretitle from $start_date to $end_date";
				my $vlabel = "";
				my $defstring = "$def $line";
				$defstring =~ s/,\s$//;
				my $now = time;
				my $graph4 = "$view\_consolidated\_dr_$days\.png";
				$graph4 =~ s/\/|\s/-/g;
				my $graphfile = "$graphdir/$graph4";
				my ($averages, $xsize, $ysize) = undef;
				my $evalstring = '($averages, $xsize, $ysize) = RRDs::graph("$graphfile", "-s", $start, "-e", $end, "--vertical-label", $vlabel, "--no-legend", "-t", $title, "-w", 600, "-h", 200, '.$defstring.");";
				eval($evalstring);
				my $err = RRDs::error;
				if ($err) { push @errors, "$err $defstring" }
				if (@errors) { $form .= Forms->form_errors(\@errors) }
				@errors = ();
				$form .= Forms->graph($type,$graph4,\@hosts);
				unless ($last_x_hours || $last_x_days) { $form .= Forms->legend(\%legend) }
			}
		}
	} elsif ($layout eq 'consolidated_host') {		
		foreach my $host (sort keys %host_rrd_select) {
			my @hosts = ();
			my %legend = ();
			my $pretitle = "Host: $host";
			my $line = undef;
			my $def = undef;
			my $i = 0;
			my $hname = $host;
			$hname =~ s/\s/+/g;
			push @hosts, $hname;
			foreach my $data (@{$host_rrd_select{$host}}) {
				my $sname = $data;
				my $file = $host_service_rrd{$host}{$data};
				my $info = qx($rrd_bin/rrdtool info $file 2>&1) || push @errors, "Error(s) executing $rrd_bin/rrdtool info $file $!";
				my %lines = ();
				my @info = split(/\n/, $info);
				foreach my $in (@info) {
					if ($in =~ /^ds\[(\S+)\]/) { $lines{$1} = 1 }
				}
				foreach my $key (sort keys %lines) {
					my $color = pop @colors;
					$legend{$host}{"$data\_$key"} = $color;
					$def .= "\"DEF:$key$i=$file:$key:AVERAGE\", ";
					$line .= "\"LINE2:$key$i$color:$key $host\", ";
					$i++;
				}
			}
			if ($last_x_hours) {
				my $end = time;
				my $start = $end - ($hours * 3600);
				my $title .= "$pretitle last $hours hours";
				my $vlabel = "";
				my $defstring = "$def $line";
				$defstring =~ s/,\s$//;
				my $graph4 = "$view\_$host\_h_$days\.png";
				$graph4 =~ s/\/|\s/-/g;
				my $graphfile = "$graphdir/$graph4";
				my ($averages, $xsize, $ysize) = undef;
				my $evalstring = '($averages, $xsize, $ysize) = RRDs::graph("$graphfile", "-s", $start, "-e", $end, "--vertical-label", $vlabel, "--no-legend", "-t", $title, "-w", 600, "-h", 200, '.$defstring.");";
				eval($evalstring);
				my $err = RRDs::error;
				if ($err) { push @errors, "$err $defstring" }
				if (@errors) { $form .= Forms->form_errors(\@errors) }
				@errors = ();
				$form .= Forms->graph($type,$graph4,\@hosts);
			}
			if ($last_x_days) {
				my $end = time;
				my $start = $end - ($days * 86400);
				my $title ="$pretitle last $days days";
				my $vlabel = "";
				my $defstring = "$def $line";
				$defstring =~ s/,\s$//;
				my $graph4 = "$view\_$host\_d_$days\.png";
				$graph4 =~ s/\/|\s/-/g;
				my $graphfile = "$graphdir/$graph4";
				my ($averages, $xsize, $ysize) = undef;
				my $evalstring = '($averages, $xsize, $ysize) = RRDs::graph("$graphfile", "-s", $start, "-e", $end, "--vertical-label", $vlabel, "--no-legend", "-t", $title, "-w", 600, "-h", 200, '.$defstring.");";
				eval($evalstring);
				my $err = RRDs::error;
				if ($err) { push @errors,  "$err $defstring" }
				if (@errors) { $form .= Forms->form_errors(\@errors) }
				@errors = ();
				$form .= Forms->graph($type,$graph4,\@hosts);
			}
			if ($date_range) {
				my $start = convert_date($start_date);
				unless ($start) {
					push @errors, "Start date is invalid: $start_date (yyyy/mm/dd).";
				}
				my $end = convert_date($end_date);
				unless ($end) {
					push @errors, "End date s $start e $end is invalid: $end_date (yyyy/mm/dd).";
				}
				if ($end < $start) {
					push @errors, "End date must be newer than start date.)";
				}
				if (@errors) {
					$form .= Forms->form_errors(\@errors);
				} else {
					my $title = "$pretitle from $start_date to $end_date";
					my $vlabel = "";
					my $defstring = "$def $line";
					$defstring =~ s/,\s$//;
					my $now = time;
					my $graph4 = "$view\_$host\_dr_$days$now\.png";
					$graph4 =~ s/\/|\s/-/g;
					my $graphfile = "$graphdir/$graph4";
					my ($averages, $xsize, $ysize) = undef;
					my $evalstring = '($averages, $xsize, $ysize) = RRDs::graph("$graphfile", "-s", $start, "-e", $end, "--vertical-label", $vlabel, "--no-legend", "-t", $title, "-w", 600, "-h", 200, '.$defstring.");";
					eval($evalstring);
					my $err = RRDs::error;
					if ($err) { push @errors, "$err $defstring" }
					if (@errors) { $form .= Forms->form_errors(\@errors) }
					@errors = ();
					$form .= Forms->graph($type,$graph4,\@hosts);
				}
			}
			$form .= Forms->legend(\%legend);
		}
	} else {
		foreach my $host (sort keys %host_rrd_select) {
			my @hosts = ();
			my $hname = $host;
			$hname =~ s/\s/+/g;
			push @hosts, $hname;
			my $pretitle = "Host: $host";
			my $i = 0;
			if ($last_x_hours) {
				foreach my $data (@{$host_rrd_select{$host}}) {
					my %legend = ();
					my $line = undef;
					my $def = undef;
					my $sname = $data;
					my $file = $host_service_rrd{$host}{$data};
					my $info = qx($rrd_bin/rrdtool info $file 2>&1) || push @errors, "Error(s) executing $rrd_bin/rrdtool info $file $!";
					my %lines = ();
					# ---- custom graph commands need these ---
					my $ii = 0;
					my $ds_list = undef;
					# -------------------
					my @info = split(/\n/, $info);
					foreach my $in (@info) {
						if ($in =~ /^ds\[(\S+)\]/) { $lines{$1} = 1 }
					}
					foreach my $key (sort keys %lines) {
						my $color = pop @colors;
						$legend{$host}{"$data\_$key"} = $color;
						$def .= "\"DEF:$key$i=$file:$key:AVERAGE\", ";
						$line .= "\"LINE2:$key$i$color:$key $host\", ";
						# Have to trap the ds names in a list for the custom graph command
						@$ds_list[$ii] = $key;
						$i++;
						$ii++;
					}
					my $end = time;
                                        my $start = $end - ($hours * 3600);
                                        my $title .= "$pretitle last $hours hours";
                                        my $graph4 = "$view\_$host\_$data\_h_$days\.png";
                                        $graph4 =~ s/\/|\s/-/g;
                                        my $graphfile = "$graphdir/$graph4";

					# Look for the custom graph command right here
	                                my $customgraph_command = customgraph($host,$data);

					# Now parse it to see if it should replace the default graphing command
					# That happens if it has an "rrdtool graph" anywhere in the field
					if ($customgraph_command =~ /rrdtool\s+graph/) {
						# replace the string rrd_source with the selected rrd file path
                                                $customgraph_command =~ s/rrd_source/$file/g;
						# reformat the rrdtool graph command to use our file name
						$customgraph_command =~ s/rrdtool\s+graph\s+-/rrdtool graph $graphfile/;
						# get rid of those pesky backslashes and  newlines...
						$customgraph_command =~ s/\\\s//g;
						$customgraph_command =~ s/\n//g;
						$customgraph_command =~ s/\r//g;
						# and the single quotes that get in the way...
						$customgraph_command =~ s/^'//;
				                $customgraph_command=~ s/'$//;
 						# Handle the List Cases for vname parameters
						#Take care of listed DEFS, CDEFS, LINES, AREAS, PRINTS, GPRINTS, and STACKS
                                                if ($customgraph_command =~ /\$LISTSTART\$(.*?)\$LISTEND\$/) {
							my $tmpstring2 = "";
                                                        for (my $j= 0; $j < @$ds_list; $j++) {
                                                                # Handle the list case
                                                                my $tmpstring1 = $1;
                                                                $tmpstring2 .= $tmpstring1." ";
                                                                my $tmpstring3 = "@dschars[$j]=$file:@$ds_list[$j]";
                                                                $tmpstring2 =~ s/\$DEFLABEL\#\$/$tmpstring3/g;
 								$tmpstring2 =~ s/\$CDEFLABEL\#\$/@dschars[$j]/g;
                                                                $tmpstring2 =~ s/\$DSLABEL\#\$/@$ds_list[$j]/g;
                                                                my $color = pop @colors;
                                                                $tmpstring2 =~ s/\$COLORLABEL\#\$/$color/g;
                                                                #error_out("$tmpstring2") if ($debug>0);
							}
                                                        $customgraph_command =~ s/\$LISTSTART\$.*\$LISTEND\$/$tmpstring2/;
						}

						# Simple case of diect substitution of numbered ds_source_n
					 	# replace the string ds_source_(number) with the DS number we found
                                                for (my $j= 0; $j < @$ds_list; $j++) {
                                                	# replace the string ds_source_(number) with the DS number we found
                                                        my $ds_name = "ds_source_"."$j";
                                                        #error_out("$ds_name") if ($debug>0);
                                                        #error_out("@$ds_list[$j]") if ($debug>0);
                                                        $customgraph_command =~ s/$ds_name/@$ds_list[$j]/g;
                                                }

						# Take care of the underspecified cases
						# if you don't see start or end, use the cgi's start and end
						unless ($customgraph_command =~ /\-\-start/) {
							$customgraph_command .= " --start $start ";
						}
						unless ($customgraph_command =~ /\-\-end/) {
                                                        $customgraph_command .= " --end $end ";
                                                }
						# if you don't see height or width, use the cgi's defaults
                                                unless ($customgraph_command =~ /\-\-height/) {
                                                        $customgraph_command .= " --height 200 ";
                                                }
                                                unless ($customgraph_command =~ /\-\-width/) {
                                                        $customgraph_command .= " --width 600 ";
                                                }

						# make the graph
						error_out("$customgraph_command") if ($debug>0);
						my $result = qx($customgraph_command 2>&1) || push @errors, "Error(s) executing $customgraph_command $!";
						 error_out("result is $result") if ($debug>0);
						unless ($result =~ /\d+x\d+$/) {
							push @errors, "Custom Command: $customgraph_command failed with result: $result";
						}
					} else { # the usual (non-custom) case...
						$yeslegend  = 1;
						$defstring = "$def $line";
						my $vlabel = "";
						$defstring =~ s/,\s$//;
                                        	#error_out("effective: $defstring") if ($debug>0);
						my ($averages, $xsize, $ysize) = undef;
                                        	my $evalstring = '($averages, $xsize, $ysize) = RRDs::graph("$graphfile", "-s", $start, "-e", $end, "--vertical-label", $vlabel, "--no-legend", "-t", $title, "-w", 600, "-h", 200, '.$defstring.");";
                                        	eval($evalstring);
						my $err = RRDs::error;
						error_out("$evalstring") if ($debug>0);
						if ($err) { push @errors, "$err $defstring" }
					}
					if (@errors) { $form .= Forms->form_errors(\@errors) }
					@errors = ();
					$form .= Forms->graph($type,$graph4,\@hosts);
					if ($yeslegend == 1) {
						$form .= Forms->legend(\%legend);
						$yeslegend = 0;
					}
				}
			}
			if ($last_x_days) {
				foreach my $data (@{$host_rrd_select{$host}}) {
					my $line = undef;
					my $def = undef;
					my %legend = ();
					my $sname = $data;
					my $file = $host_service_rrd{$host}{$data};
					my $info = qx($rrd_bin/rrdtool info $file 2>&1) || push @errors, "Error(s) executing $rrd_bin/rrdtool info $file $!";
					my %lines = ();
                                        # ---- custom graph commands need these ---
                                        my $ii = 0;
                                        my $ds_list = undef;
                                        # -------------------

					my @info = split(/\n/, $info);
					foreach my $in (@info) {
						if ($in =~ /^ds\[(\S+)\]/) { $lines{$1} = 1 }
					}
					foreach my $key (sort keys %lines) {
						my $color = pop @colors;
						$legend{$host}{"$data\_$key"} = $color;
						$def .= "\"DEF:$key$i=$file:$key:AVERAGE\", ";
						$line .= "\"LINE2:$key$i$color:$key $host\", ";
					 	# Have to trap the ds names in a list for the custom graph command
                                                @$ds_list[$ii] = $key;
                                                $i++;
                                                $ii++;
					}
					my $end = time;
					my $start = $end - ($days * 86400);
					my $title ="$pretitle last $days days";
					my $graph4 = "$view\_$host\_$data\_d_$days\.png";
                                        $graph4 =~ s/\/|\s/-/g;
                                        my $graphfile = "$graphdir/$graph4";
					# Look for the custom graph command right here
                                        my $customgraph_command = customgraph($host,$data);
                                        #error_out("$customgraph_command") if ($debug>0);

                                        # Now parse it to see if it should replace the default graphing command
                                        # That happens if it has an "rrdtool graph" anywhere in the field
                                        if ($customgraph_command =~ /rrdtool\s+graph/) {
                                                # replace the string rrd_source with the selected rrd file path
                                                $customgraph_command =~ s/rrd_source/$file/g;
                                                # reformat the rrdtool graph command to use our file name
                                                $customgraph_command =~ s/rrdtool\s+graph\s+-/rrdtool graph $graphfile/;
                                                # get rid of those pesky backslashes and  newlines...
                                                $customgraph_command =~ s/\\\s//g;
                                                $customgraph_command =~ s/\n//g;
                                                $customgraph_command =~ s/\r//g;
                                                # and the single quotes that get in the way...
                                                $customgraph_command =~ s/^'//;
                                                $customgraph_command=~ s/'$//;
						# Handle the List Cases for vname parameters
                                                #Take care of listed DEFS, CDEFS, LINES, AREAS, PRINTS, GPRINTS, and STACKS
                                                if ($customgraph_command =~ /\$LISTSTART\$(.*?)\$LISTEND\$/) {
                                                        my $tmpstring2 = "";
                                                        for (my $j= 0; $j < @$ds_list; $j++) {
                                                                # Handle the list case
                                                                my $tmpstring1 = $1;
                                                                $tmpstring2 .= $tmpstring1." ";
                                                                my $tmpstring3 = "@dschars[$j]=$file:@$ds_list[$j]";
                                                                $tmpstring2 =~ s/\$DEFLABEL\#\$/$tmpstring3/g;
                                                                $tmpstring2 =~ s/\$CDEFLABEL\#\$/@dschars[$j]/g;
                                                                $tmpstring2 =~ s/\$DSLABEL\#\$/@$ds_list[$j]/g;
                                                                my $color = pop @colors;
                                                                $tmpstring2 =~ s/\$COLORLABEL\#\$/$color/g;
                                                                error_out("$tmpstring2") if ($debug>0);
                                                        }
                                                        $customgraph_command =~ s/\$LISTSTART\$.*\$LISTEND\$/$tmpstring2/;
                                                }
						# replace the string ds_source_(number) with the DS number we found
                                                for (my $j= 0; $j < @$ds_list; $j++) {
                                                        my $ds_name = "ds_source_"."$j";
                                                        error_out("$ds_name") if ($debug>0);
                                                        error_out("@$ds_list[$j]") if ($debug>0);
                                                        $customgraph_command =~ s/$ds_name/@$ds_list[$j]/g;
                                                }
						 # Take care of the underspecified cases
                                                # if you don't see start or end, use the cgi's start and end
                                                unless ($customgraph_command =~ /\-\-start/) {
                                                        $customgraph_command .= " --start $start ";
                                                }
                                                unless ($customgraph_command =~ /\-\-end/) {
                                                        $customgraph_command .= " --end $end ";
                                                }
                                                # if you don't see height or width, use the cgi's defaults
                                                unless ($customgraph_command =~ /\-\-height/) {
                                                        $customgraph_command .= " --height 200 ";
                                                }
                                                unless ($customgraph_command =~ /\-\-width/) {
                                                        $customgraph_command .= " --width 600 ";
                                                }

                                                # make the graph
                                                error_out("$customgraph_command") if ($debug>0);
                                                my $result = qx($customgraph_command 2>&1) || push @errors, "Error(s) executing $customgraph_command $!";
						unless ($result =~ /\d+x\d+$/) {
                                                        push @errors, "Custom Command: $customgraph_command failed with result: $result";
                                                }
                                        } else { # the usual (non-custom) case...
						$yeslegend = 1;
						my $vlabel = "";
						my $defstring = "$def $line";
						$defstring =~ s/,\s$//;
						my ($averages, $xsize, $ysize) = undef;
						my $evalstring = '($averages, $xsize, $ysize) = RRDs::graph("$graphfile", "-s", $start, "-e", $end, "--vertical-label", $vlabel, "--no-legend", "-t", $title, "-w", 600, "-h", 200, '.$defstring.");";
						eval($evalstring);
						my $err = RRDs::error;
						if ($err) { push @errors,  "$err $defstring" }
					}
					if (@errors) { $form .= Forms->form_errors(\@errors) }
					@errors = ();
					$form .= Forms->graph($type,$graph4,\@hosts);
					if ($yeslegend == 1) {
                                                $form .= Forms->legend(\%legend);
                                                $yeslegend = 0;
                                        }
				}
			}
			if ($date_range) {
				my $start = convert_date($start_date);
				my $end = convert_date($end_date);

				foreach my $data (@{$host_rrd_select{$host}}) {
					my $line = undef;
					my $def = undef;
					my %legend = ();
					my $sname = $data;
					my $file = $host_service_rrd{$host}{$data};
					my $info = qx($rrd_bin/rrdtool info $file 2>&1) || push @errors, "Error(s) executing $rrd_bin/rrdtool info $file $!";
					my %lines = ();
					# ---- custom graph commands need these ---
                                        my $ii = 0;
                                        my $ds_list = undef;
                                        # -------------------
					my @info = split(/\n/, $info);
					foreach my $in (@info) {
						if ($in =~ /^ds\[(\S+)\]/) { $lines{$1} = 1 }
					}
					foreach my $key (sort keys %lines) {
						my $color = pop @colors;
						$legend{$host}{"$data\_$key"} = $color;
						$def .= "\"DEF:$key$i=$file:$key:AVERAGE\", ";
						$line .= "\"LINE2:$key$i$color:$key $host\", ";
						# Have to trap the ds names in a list for the custom graph command
                                                @$ds_list[$ii] = $key;
                                                $ii++;
						$i++;
					}
					my $start = convert_date($start_date);
					unless ($start) {
						push @errors, "Start date is invalid: $start_date (yyyy/mm/dd).";
					}
					my $end = convert_date($end_date);
					unless ($end) {
						push @errors, "End date s $start e $end is invalid: $end_date (yyyy/mm/dd).";
					}
					if ($end < $start) {
						push @errors, "End date must be newer than start date.)";
					}
					if (@errors) {
						$form .= Forms->form_errors(\@errors);
					} else {
						my $title = "$pretitle from $start_date to $end_date";
						my $graph4 = "$view\_$host\_$data\_dr_$days\.png";
                                                $graph4 =~ s/\/|\s/-/g;
                                                my $graphfile = "$graphdir/$graph4";
						

						# Look for the custom graph command right here
                                        	my $customgraph_command = customgraph($host,$data);
                                        	#error_out("$customgraph_command") if ($debug>0);

                                        	# Now parse it to see if it should replace the default graphing command
                                        	# That happens if it has an "rrdtool graph" anywhere in the field
                                        	if ($customgraph_command =~ /rrdtool\s+graph/) {
                                                	# replace the string rrd_source with the selected rrd file path
                                                	$customgraph_command =~ s/rrd_source/$file/g;
                                                	# reformat the rrdtool graph command to use our file name
                                                	$customgraph_command =~ s/rrdtool\s+graph\s+-/rrdtool graph $graphfile/;
                                                	# get rid of those pesky backslashes and  newlines...
                                              	  	$customgraph_command =~ s/\\\s//g;
                                                	$customgraph_command =~ s/\n//g;
                                                	$customgraph_command =~ s/\r//g;
                                                	# and the single quotes that get in the way...
                                                	$customgraph_command =~ s/^'//;
                                                	$customgraph_command=~ s/'$//;
							 # Handle the List Cases for vname parameters
                                                	#Take care of listed DEFS, CDEFS, LINES, AREAS, PRINTS, GPRINTS, and STACKS
                                                	if ($customgraph_command =~ /\$LISTSTART\$(.*?)\$LISTEND\$/) {
                                                        	my $tmpstring2 = "";
                                                        	for (my $j= 0; $j < @$ds_list; $j++) {
                                                                	# Handle the list case
                                                                	my $tmpstring1 = $1;
                                                                $tmpstring2 .= $tmpstring1." ";
                                                                my $tmpstring3 = "@dschars[$j]=$file:@$ds_list[$j]";
                                                                $tmpstring2 =~ s/\$DEFLABEL\#\$/$tmpstring3/g;
                                                                $tmpstring2 =~ s/\$CDEFLABEL\#\$/@dschars[$j]/g;
                                                                $tmpstring2 =~ s/\$DSLABEL\#\$/@$ds_list[$j]/g;
                                                                my $color = pop @colors;
                                                                $tmpstring2 =~ s/\$COLORLABEL\#\$/$color/g;
                                                                error_out("$tmpstring2") if ($debug>0);
                                                        }
                                                        $customgraph_command =~ s/\$LISTSTART\$.*\$LISTEND\$/$tmpstring2/;
                                                }
                                                # replace the string ds_source_(number) with the DS number we found
                                                for (my $j= 0; $j < @$ds_list; $j++) {
                                                        my $ds_name = "ds_source_"."$j";
                                                        error_out("$ds_name") if ($debug>0);
                                                        error_out("@$ds_list[$j]") if ($debug>0);
                                                        $customgraph_command =~ s/$ds_name/@$ds_list[$j]/g;
                                                }

							# Take care of the underspecified cases
                                                	# if you don't see start or end, use the cgi's start and end
                                                	unless ($customgraph_command =~ /\-\-start/) {
                                                        	$customgraph_command .= " --start $start ";
                                                	}
                                                	unless ($customgraph_command =~ /\-\-end/) {
                                                        	$customgraph_command .= " --end $end ";
                                                	}
                                                	# if you don't see height or width, use the cgi's defaults
                                                	unless ($customgraph_command =~ /\-\-height/) {
                                                        	$customgraph_command .= " --height 200 ";
                                                	}
                                                	unless ($customgraph_command =~ /\-\-width/) {
                                                        	$customgraph_command .= " --width 600 ";
                                                	}

                                                	# make the graph
                                                	error_out("$customgraph_command") if ($debug>0);
                                                	my $result = qx($customgraph_command 2>&1) || push @errors, "Error(s) executing $customgraph_command $!";
                                                	unless ($result =~ /\d+x\d+$/) {
                                                        	push @errors, "Custom Command: $customgraph_command failed with result: $result";
                                                	}
                                        	} else { # the usual (non-custom) case...
							$yeslegend = 1;
							my $vlabel = "";
							my $defstring = "$def $line";
							$defstring =~ s/,\s$//;
							my $now = time;
							my $graph4 = "$view\_$host\_$data\_dr_$days\.png";
							$graph4 =~ s/\/|\s/-/g;
							my $graphfile = "$graphdir/$graph4";
							my ($averages, $xsize, $ysize) = undef;
							my $evalstring = '($averages, $xsize, $ysize) = RRDs::graph("$graphfile", "-s", $start, "-e", $end, "--vertical-label", $vlabel, "--no-legend", "-t", $title, "-w", 600, "-h", 200, '.$defstring.");";
							eval($evalstring);
							my $err = RRDs::error;
							if ($err) { push @errors, "$err $defstring" }
						}
						if (@errors) { $form .= Forms->form_errors(\@errors) }
						@errors = ();
						$form .= Forms->graph($type,$graph4,\@hosts);
						if ($yeslegend == 1) {
                                                	unless ($last_x_hours || $last_x_days) { $form .= Forms->legend(\%legend) }
                                                	$yeslegend = 0;
                                        	}
					}
				}
			}
		}		
	}
	return $form;
}

sub read_config() {
	my $parser = XML::LibXML->new();
	my $tree = undef;
	eval {$tree = $parser->parse_file("$view_dir/$hidden{'file'}")};
	push @errors, "Invalid file type: $hidden{'file'} is not valid XML: $@" if $@;
	unless (@errors) {
		my $root = $tree->getDocumentElement;
		my @nodes = $root->findnodes( "//view" );
		foreach my $node (@nodes) {
			$hidden{'view'} = $node->getAttribute('name');
			my @siblings = $node->getChildnodes();
			foreach my $sibling (@siblings) {
				if ($sibling->nodeName() eq 'host') {
					$hidden{'host'} = $sibling->getAttribute('name');
					if ($sibling->hasChildNodes()) {
						my @children = $sibling->getChildnodes();
						foreach my $child (@children) {
							if ($child->hasAttributes) {
								my $data = $child->getAttribute('name');
								push @{$host_rrd_select{$hidden{'host'}}}, $data;
							}
						}
					}
				} elsif ($sibling->nodeName() eq 'layout') {
					$hidden{'layout'} = $sibling->textContent;
					$layout = $hidden{'layout'};
				} elsif ($sibling->nodeName() eq 'last_x_days') {
					$hidden{'last_x_days'} = $sibling->textContent;
					$last_x_days = $hidden{'last_x_days'};
				} elsif ($sibling->nodeName() eq 'days') {
					$hidden{'days'} = $sibling->textContent;
					$days = $hidden{'days'};
				} elsif ($sibling->nodeName() eq 'last_x_hours') {
					$hidden{'last_x_hours'} = $sibling->textContent;
					$last_x_hours = $hidden{'last_x_hours'};
				} elsif ($sibling->nodeName() eq 'hours') {
					$hidden{'hours'} = $sibling->textContent;
					$hours = $hidden{'hours'};
				} elsif ($sibling->nodeName() eq 'date_range') {
					$hidden{'date_range'} = $sibling->textContent;
					$date_range = $hidden{'date_range'};
				} elsif ($sibling->nodeName() eq 'start_date') {
					$hidden{'start_date'} = $sibling->textContent;
					$start_date = $hidden{'start_date'};
				} elsif ($sibling->nodeName() eq 'end_date') {
					$hidden{'end_date'} = $sibling->textContent;
					$end_date = $hidden{'end_date'};
				}
			}
		}
	}
}

print "Content-type: text/html \n\n";

my $page_title = 'Performance Graphs';
unless (%view_file) { views('','','') }
if ($query->param('update_left')) {
	print Forms->left_page(\%view_file);
} elsif ($query->param('update_main')) {
	$hidden{'nocache'} = time;
	$hidden{'update_main'} = 1;
	my $refresh_left = 0;

	if ($query->param('host')) {
		my $host = $query->param('host');
		$host = uri_unescape($host);

		my @rrds = $query->param('rrds');
		foreach my $rrd (@rrds) {
			$rrd = uri_unescape($rrd);
			if ($rrd) { push @{$host_rrd_select{$host}}, $rrd }
		}
	}
	if ($query->param('rrd')) {
		my $rrd = $query->param('rrd');
		$rrd = uri_unescape($rrd);
		my @hosts = $query->param('hosts');
		foreach my $host (@hosts) {
			$host = uri_unescape($host);
			if ($host) { push @{$host_rrd_select{$host}}, $rrd }
		}
	}
	my @hosts = $query->param('host_data');
	foreach my $host (@hosts) { 
		unless ($query->param("remove_$host")) {
			$host = uri_unescape($host);
			my @vals = split(/\%\%/, $host);
			push @{$host_rrd_select{$vals[0]}}, $vals[1];
		}
	}

	$hidden{'host_list'} = $query->param('host_list');
	unless ($hidden{'host_list'} ) {
		my @hosts = $query->param('hosts');
		foreach my $host (@hosts) { 
			$host = uri_unescape($host);
			$hidden{'host_list'} .= "$host,";
		}
		chop $hidden{'host_list'};
	}

	$hidden{'graph_list'} = $query->param('graph_list');
	unless ($hidden{'graph_list'} ) {
		my @graphs = $query->param('graphs');
		foreach my $graph (@graphs) { 
			$graph = uri_unescape($graph);
			$hidden{'graph_list'} .= "$graph,";
		}
		chop $hidden{'graph_list'};
	}

	if ($query->param('config')) {
		delete $hidden{'hours'};
		delete $hidden{'days'};
		delete $hidden{'end_date'};
		delete $hidden{'start_date'};
		delete $hidden{'last_x_hours'};
		delete $hidden{'last_x_days'};
		delete $hidden{'date_range'};
		delete $hidden{'layout'};
	}

	my $refresh_url = 0;
	unless ($query->param('config')) {
		if ($auto_refresh && $hidden{'refresh_rate'}) {
			$refresh_url = "$cgi_bin/perfchart.cgi?auto_refresh=1";
			foreach my $name (keys %hidden) {
				$refresh_url .= "&$name=$hidden{$name}";
			}
			my @hd = $query->param('host_data');
			foreach my $hd (@hd) {
				$refresh_url .= "&host_data=$hd";
			}
		}
	}

	if ($debug) {
		foreach my $name ($query->param) {
			my $v = $query->param($name);
			error_out("$name $v");
		}
	}
	my @errors = ();
	my %genreport = ('name' => 'gen_report','value' => 'Graph Performance');
	my %saveview = ('name' => 'save_view','value' => 'Save View');
	my %cancel = ('name' => 'cancel','value' => 'Cancel');
	my %close = ('name' => 'close','value' => 'Close');
	my %rename = ('name' => 'rename','value' => 'Rename');
	my %delete = ('name' => 'delete','value' => 'Delete');
	my $got_form = 0;
	if ($query->param('close')) {
		$got_form = 1;
	} elsif ($query->param('save_view') || $query->param('save') || $query->param('saveas') ||  $query->param('rename') || $query->param('delete') || $query->param('confirm_delete')) {
		get_hosts_graphs();
		unless (%view_file) { views('','','') }
		my $error = undef;
		my $host_data = undef;
		my $save = 0;
		my $view_data = "<?xml version=\"1.0\" ?>\n<view name=\"$hidden{'view'}\">";
		if ($hidden{'view'} eq 'config') {
			foreach my $host (sort keys %host_rrd_select) { 
				$view_data .= "\n <host name=\"$host\">";
				foreach my $data (@{$host_rrd_select{$host}}) {
					my $host_data_str = "$host\%\%$data";
					$host_data_str =~ uri_escape($host_data_str); 
					my %h = ('host_data' => $host_data_str);
					$host_data .= Forms->hidden(\%h);
					$view_data .= "\n  <data name=\"$data\"></data>";
				}
				$view_data .= "\n </host>";
			}	
		}
		$view_data .= "\n <layout>$hidden{'layout'}</layout>";
		$view_data .= "\n <date_range>$hidden{'date_range'}</date_range>";
		$view_data .= "\n <start_date>$hidden{'start_date'}</start_date>";
		$view_data .= "\n <end_date>$hidden{'end_date'}</end_date>";
		$view_data .= "\n <last_x_hours>$hidden{'last_x_hours'}</last_x_hours>";
		$view_data .= "\n <hours>$hidden{'hours'}</hours>";
		$view_data .= "\n <last_x_days>$hidden{'last_x_days'}</last_x_days>";
		$view_data .= "\n <days>$hidden{'days'}</days>";
		$view_data .= "\n</view>";
		if ($query->param('delete') || $query->param('confirm_delete')) {
			if ($query->param('confirm_delete')) {
				unlink("$view_dir/$hidden{'file'}") || push @errors, "Error: Unable to remove $view_dir/$hidden{'file'} $!";
				$refresh_left = 1;
				views('',$hidden{'file'},'1');
				unless (@errors) {
					push @message, "Removed: $file_view{$hidden{'file'}}";
					$body .= Forms->form_top('Performance Selection Criteria','');
					if (@errors) { $body .= Forms->form_errors(\@errors) }
					$body .= Forms->display_hidden('View:','name',$file_view{$hidden{'file'}});
					if (@message) { $body .= Forms->form_message(\@message) }
					$body .= $host_data;
					$body .= Forms->hidden(\%hidden);
					$delete{'name'} = 'confirm_delete';
					$body .= Forms->form_bottem_one_button(\%close);
					$got_form = 1;
				} 
			}
			unless ($got_form) {
				push @message, "Are you sure you want to remove view $file_view{$hidden{'file'}}?";
				$body .= Forms->form_top('Performance Selection Criteria','');
				if (@errors) { $body .= Forms->form_errors(\@errors) }
				$body .= Forms->form_message(\@message);
				$body .= $host_data;
				$body .= Forms->hidden(\%hidden);
				$delete{'name'} = 'confirm_delete';
				$body .= Forms->form_bottem_two_button(\%delete,\%cancel);
				$got_form = 1;
			}
		} elsif ($query->param('save_view') || $query->param('save') || $query->param('saveas') || $query->param('rename')) {
			if ($query->param('save_view') && $query->param('name')) { $save = 1 }
			if ($query->param('rename') && $query->param('new_name')) { $save = 1 }
			if ($query->param('save')) { $save = 1 }
			my $got_name = 0;
			if ($save) { 
				my $tname = undef;
				if ($query->param('rename')) {
					$tname = $query->param('new_name');
				} elsif ($query->param('save_view')) {
					$tname = $query->param('name');
				} else {
					$tname = $file_view{$hidden{'file'}};
				}
				if ($query->param('save_view')) {
					if ($view_file{$tname}) { 
						push @errors, "A view with name $tname already exists.";
					} else {
						unless ($hidden{'file'}) {
							my $now = time;
							$hidden{'file'} = "view_$now.xml";
						}
						views($tname,$hidden{'file'},'');
						$refresh_left = 1;

					}
				}
				if ($query->param('rename')) {
					if ($tname eq $file_view{$hidden{'file'}}) {
						$got_name = 1;
					} else {
						if ($view_file{$tname}) { 
							push @errors, "A view with name $tname already exists.";
						} else {
							views($tname,$hidden{'file'},'');
							$got_name = 1;
							$refresh_left = 1;
						}
					}
				}

				unless (@errors || $query->param('rename')) {
					open(FILE, "> $view_dir/$hidden{'file'}") || push @errors, "Error: Unable to write $view_dir/$hidden{'file'} $!";
					print FILE $view_data;
					close (FILE);
					unless (@errors) { 
						$got_name = 1;
						push @message, "View $file_view{$hidden{'file'}} saved to $view_dir/$hidden{'file'}";
					}
				}
			} 
			unless ($got_name) {
				$body .= Forms->form_top('Performance Selection Criteria','');
				if (@errors) { $body .= Forms->form_errors(\@errors) }
				if ($query->param('rename')) {
					$body .= Forms->display_hidden('View:','name',$file_view{$hidden{'file'}});
					$body .= Forms->text_box('Name:','new_name','');
					%saveview = %rename;
				} else {
					$body .= Forms->text_box('Name:','name','');
				}
				$body .= $host_data;
				$body .= Forms->hidden(\%hidden);
				$body .= Forms->form_bottem_two_button(\%saveview,\%cancel);
				$got_form = 1;
			}
		}
	} else {
		get_hosts_graphs();
	}

	unless ($got_form) {
		unless (%view_file) { views('','','') }
		if ($query->param('config')) {
			$hidden{'view'} = 'config';
		} elsif ($query->param('gen_report')) {
			$hidden{'view'} = 'gen_report';
		} elsif ($hidden{'view'} eq 'get_view') {
			read_config();
			$hidden{'view'} = 'gen_report';
		}
		if ($hidden{'view'} eq 'config') {
			my $host = $query->param('host');
			$host = uri_unescape($host);
			my $rrd = $query->param('rrd');
			$rrd = uri_unescape($rrd);
			$body .= Forms->form_top('Performance View','');
			if (@errors) { $body .= Forms->form_errors(\@errors) }
			if (@message) { $body .= Forms->form_message(\@message) }

			my @host_select = ();
			my @host_rrds = ();
			my @rrd_hosts = ();
			if ($host_rrd{$host}) { push (@host_rrds,(@{$host_rrd{$host}})) }
			if ($rrd_host{$rrd}) { push (@rrd_hosts,(@{$rrd_host{$rrd}})) }
			my @hosts = ();
			foreach my $h (sort keys %hosts) { push @hosts, $h }

			my @rrds = ();
			foreach my $rrd (sort keys %rrds) { push @rrds, $rrd }
			if ($file_view{$hidden{'file'}}) {
				$body .= Forms->display_hidden('View:','name',$file_view{$hidden{'file'}});
			}
			$body .= Forms->multiselect($hidden{'view'},\%host_service_rrd,\%host_rrd_select,$host,$rrd,\@rrds,\%hidden);
			$body .= Forms->consolidate_opts($layout);
			$body .= Forms->hour_select($hours,$last_x_hours);	
			$body .= Forms->day_select($days,$last_x_days);	
			unless ($start_date) { $start_date = today() }
			unless ($end_date) { $end_date = today() }
			$body .= Forms->date_select($start_date,$end_date,$date_range);	
			delete $hidden{'hours'};
			delete $hidden{'days'};
			delete $hidden{'end_date'};
			delete $hidden{'start_date'};
			delete $hidden{'last_x_hours'};
			delete $hidden{'last_x_days'};
			delete $hidden{'date_range'};

			$body .= Forms->hidden(\%hidden);
			if ($file_view{$hidden{'file'}}) {
				my %rename = ('name' => 'rename','value' => 'Rename' );
				$saveview{'name'} = 'save';
				$saveview{'value'} = 'Save';
				$body .= Forms->form_bottem_four_button(\%genreport,\%saveview,\%rename,\%delete);
			} else {
				$body .= Forms->form_bottem_two_button(\%genreport,\%saveview);
			}
		} elsif ($hidden{'view'} eq 'gen_report') {
			$body .= Forms->form_top('Performance View','');
			if ($file_view{$hidden{'file'}}) {
				$body .= Forms->display_hidden('View:','',"$file_view{$hidden{'file'}}");
			}		
			$body .= Forms->refresh_select($auto_refresh,$hidden{'refresh_rate'});
			$body .= data_host();

			foreach my $host (sort keys %host_rrd_select) {
				foreach my $data (@{$host_rrd_select{$host}}) {
					my $host_data_srt = "$host\%\%$data";
					$host_data_srt = uri_escape($host_data_srt);
					my %nam_val = ('host_data' => $host_data_srt);
					$body .= Forms->hidden(\%nam_val);
				}
			}
			$body .= Forms->hidden(\%hidden);
			$body .= Forms->form_bottem_no_button();

		} else {
			$body .= Forms->splash();
		}
	}
	print Forms->header($page_title,$refresh_url,$hidden{'refresh_rate'},$refresh_left);
	print $body;
	print Forms->footer();
} else {
	my $url = undef;
	foreach my $name ($query->param) {
		my $value = $query->param($name);
		$url .= "&$name=$value";
	}
	print Forms->frame($url);
}

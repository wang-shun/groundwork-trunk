#!/usr/local/groundwork/perl/bin/perl -w --
#
# PerfChart - Groundwork Performance Charts
# perfchart.cgi
#
###############################################################################
# Release 4.6
# July 2017
###############################################################################
#
# Original author: Scott Parris
#
# Copyright 2007-2017 GroundWork Open Source, Inc. ("GroundWork")
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

use Config;
use CGI;
use URI::Escape;
use XML::LibXML;
use Time::HiRes;
use Time::Local;
use RRDs;
use DBI;
use POSIX;

use lib qq(/usr/local/groundwork/core/performance/lib);
use lib qq(/usr/local/groundwork/core/monarch/lib);

use MonarchStorProc;
use PerfChartsForms;
use CollageQuery;

$|++;

my $debug         = 0;
my $debug_minimal = ( $debug >= 1 );
my $debug_basic   = ( $debug >= 2 );
my $debug_maximal = ( $debug >= 3 );

# Specify whether to use a shared library to implement RRD file access,
# or to fork an external process for such work (the legacy implementation).
# Set to 1 (recommended) for high performance, to 0 only as an emergency fallback
# or for special purposes.
# NOTE:  Don't enable these options yet, as support for them is as yet incomplete.
my $use_shared_rrd_module_for_info  = 0;
my $use_shared_rrd_module_for_graph = 0;

my $slowest_refresh_rate = 3600 * 24 * 7;  # One week between refreshes, max, for basic validity checking.

my $show_params = undef;
# Uncomment this next line to spill out details of each query at the end of the result screen.
# $show_params = 1;

my $is_portal = 1;         # 1 for GWMEE 7.0.0 or later, 0 for earlier releases
my %hidden    = ();
my $query     = new CGI;

# Adapt to an upgraded CGI package while still maintaining backward compatibility.
my $multi_param = $query->can('multi_param') ? 'multi_param' : 'param';

my $view            = $query->param('view');
my $object          = $query->param('object');
my $layout          = $query->param('layout');
my $date_range      = $query->param('date_range');
my $start_date      = $query->param('start_date');
my $end_date        = $query->param('end_date');
my $last_x_days     = $query->param('last_x_days');
my $last_x_hours    = $query->param('last_x_hours');
my $days            = $query->param('days');
my $hours           = $query->param('hours');
my $auto_refresh    = $query->param('auto_refresh');
my $newauto_refresh = $query->param('newauto_refresh');
my $refresh_rate    = $query->param('refresh_rate');
my $newrefresh_rate = $query->param('newrefresh_rate');
my $file_param      = $query->param('file');
my $gen_view        = $query->param('gen_view');

$view   = uri_unescape($view);
$object = uri_unescape($object);
$refresh_rate    =~ s/^\s*|\s*$//g if defined $refresh_rate;
$newrefresh_rate =~ s/^\s*|\s*$//g if defined $newrefresh_rate;

if (defined $view) {
    $view =~ s{[\\\@%&'<>`[\]"\(\)\$/]}{}g;
    $view =~ s/\pC//g;
    $view =~ s/^\s*|\s*$//g;
}

# We need to allow an empty file param, which appears if we refresh an as-yet-unsaved view.
if (defined($file_param) and $file_param ne '' and $file_param !~ m{^view_\d+\.xml$}) {
    print STDERR "Invalid file parameter found in perfchart.cgi: $file_param\n";
    exit(1);
}
if (defined($layout) and $layout !~ /^(expanded|consolidated_host|consolidated)$/) {
    print STDERR "Invalid layout parameter found in perfchart.cgi: $layout\n";
    exit(1);
}
if (defined($date_range) and $date_range =~ /\W/) {
    print STDERR "Invalid date_range parameter found in perfchart.cgi: $date_range\n";
    exit(1);
}
if (defined($start_date) and $start_date !~ /^\d{4}-\d{2}-\d{2}$/) {
    print STDERR "Invalid start_date parameter found in perfchart.cgi: $start_date\n";
    exit(1);
}
if (defined($end_date) and $end_date !~ /^\d{4}-\d{2}-\d{2}$/) {
    print STDERR "Invalid end_date parameter found in perfchart.cgi: $end_date\n";
    exit(1);
}
if (defined($last_x_days) and $last_x_days =~ /\W/) {
    print STDERR "Invalid last_x_days parameter found in perfchart.cgi: $last_x_days\n";
    exit(1);
}
if (defined($last_x_hours) and $last_x_hours =~ /\W/) {
    print STDERR "Invalid last_x_hours parameter found in perfchart.cgi: $last_x_hours\n";
    exit(1);
}
if (defined($days) and $days !~ /^\d+$/) {
    print STDERR "Invalid days parameter found in perfchart.cgi: $days\n";
    exit(1);
}
if (defined($hours) and $hours !~ /^\d+$/) {
    print STDERR "Invalid hours parameter found in perfchart.cgi: $hours\n";
    exit(1);
}
if (defined($newauto_refresh) and $newauto_refresh =~ /\W/) {
    print STDERR "Invalid newauto_refresh parameter found in perfchart.cgi: $newauto_refresh\n";
    exit(1);
}

# FIX MAJOR:  validate all the other query parameters as well:
# $object, $auto_refresh, $refresh_rate, $gen_view
# plus these:
# $query->param("remove_$host")
# $query->param('graph_list')
# $query->param('graphs')
# $query->param('host')
# $query->param('host_data')
# $query->param('host_list')
# $query->param('hosts')
# $query->param('name')
# $query->param('new_name')
# $query->param('rrd')
# $query->param('rrds')
#
# $newrefresh_rate is validated later on.

# FIX MAJOR:  Once the performance viewer is placed under control of JOSSO,
# we should also check for a defined and non-empty value for $ENV{'REMOTE_USER'},
# to verify that we are running as an authorized user.

$hidden{'file'}         = $file_param;
$hidden{'gen_view'}     = $gen_view;
$hidden{'view'}         = $view;
$hidden{'object'}       = $object;
$hidden{'layout'}       = $layout;
$hidden{'date_range'}   = $date_range;
$hidden{'start_date'}   = $start_date;
$hidden{'end_date'}     = $end_date;
$hidden{'last_x_days'}  = $last_x_days;
$hidden{'last_x_hours'} = $last_x_hours;
$hidden{'days'}         = $days;
$hidden{'hours'}        = $hours;

my $body        = '';
my $refresh_url = '';
my $defstring;

my $rrd_dir  = '/usr/local/groundwork/rrd';
my $rrd_bin  = '/usr/local/groundwork/common/bin';
my $graphdir = '/usr/local/groundwork/core/performance/htdocs/performance/rrd_img';
my $view_dir = '/usr/local/groundwork/core/performance/performance_views';
my $cgi_bin  = $is_portal ? '/performance/cgi-bin/performance' : '/performance/cgi-bin';

my @errors           = ();
my @warnings         = ();
my @message          = ();
my %rrds             = ();
my %rrdtype          = ();
my %hosts            = ();
my %host_rrd         = ();
my %host_service_rrd = ();
my %rrd_host         = ();
my %host_rrd_select  = ();
my %file_view        = ();
my %view_file        = ();

my @colors = (
    '#000000', '#C05599', '#E092E3', '#6F76C4', '#9BAEFF', '#818181', '#C0C0C0', '#8BA016', '#D3DB00', '#64A2B8', '#8DD9E0', '#7E87B7',
    '#F3B50F', '#EB6232', '#000000', '#C05599', '#E092E3', '#6F76C4', '#9BAEFF', '#818181', '#C0C0C0', '#8BA016', '#D3DB00', '#64A2B8',
    '#8DD9E0', '#7E87B7', '#F3B50F', '#EB6232', '#000000', '#C05599', '#E092E3', '#6F76C4', '#9BAEFF', '#818181', '#C0C0C0', '#8BA016',
    '#D3DB00', '#64A2B8', '#8DD9E0', '#7E87B7', '#F3B50F', '#EB6232', '#000000', '#C05599', '#E092E3', '#6F76C4', '#9BAEFF', '#818181',
    '#C0C0C0', '#8BA016', '#D3DB00', '#64A2B8', '#8DD9E0', '#7E87B7', '#F3B50F', '#EB6232', '#000000', '#C05599', '#E092E3', '#6F76C4',
    '#9BAEFF', '#818181', '#C0C0C0', '#8BA016', '#D3DB00', '#64A2B8', '#8DD9E0', '#7E87B7', '#F3B50F', '#EB6232', '#000000', '#C05599',
    '#E092E3', '#6F76C4', '#9BAEFF', '#818181', '#C0C0C0', '#8BA016', '#D3DB00', '#64A2B8', '#8DD9E0', '#7E87B7', '#F3B50F', '#EB6232',
    '#000000', '#C05599', '#E092E3', '#6F76C4', '#9BAEFF', '#818181', '#C0C0C0', '#8BA016', '#D3DB00', '#64A2B8', '#8DD9E0', '#7E87B7',
    '#F3B50F', '#EB6232', '#000000', '#C05599', '#E092E3', '#6F76C4', '#9BAEFF', '#818181', '#C0C0C0', '#8BA016', '#D3DB00', '#64A2B8',
    '#8DD9E0', '#7E87B7', '#F3B50F', '#EB6232', '#000000', '#C05599', '#E092E3', '#6F76C4', '#9BAEFF', '#818181', '#C0C0C0', '#8BA016',
    '#D3DB00', '#64A2B8', '#8DD9E0', '#7E87B7', '#F3B50F', '#EB6232', '#000000', '#C05599', '#E092E3', '#6F76C4', '#9BAEFF', '#818181',
    '#C0C0C0', '#8BA016', '#D3DB00', '#64A2B8', '#8DD9E0', '#7E87B7', '#F3B50F', '#EB6232', '#000000', '#C05599', '#E092E3', '#6F76C4',
    '#9BAEFF', '#818181', '#C0C0C0', '#8BA016', '#D3DB00', '#64A2B8', '#8DD9E0', '#7E87B7', '#F3B50F', '#EB6232', '#000000', '#C05599',
    '#E092E3', '#6F76C4', '#9BAEFF', '#818181', '#C0C0C0', '#8BA016', '#D3DB00', '#64A2B8', '#8DD9E0', '#7E87B7', '#F3B50F', '#EB6232',
    '#000000', '#C05599', '#E092E3', '#6F76C4', '#9BAEFF', '#818181', '#C0C0C0', '#8BA016', '#D3DB00', '#64A2B8', '#8DD9E0', '#7E87B7',
    '#F3B50F', '#EB6232'
);

## $timestamp_format ought to be localized according to the LC_TIME or LC_ALL environment variable, with the
## default being the English-US ("en_US" or "C") version, but such variables are not being passed to this script
my $timestamp_format = "%a %b %d %H\\\\:%M\\\\:%S %Y %Z";
my $center_format = "\\\\c";
## We could make this 'through', but that would be independent of the current locale, unlike the %a and %b
## timestamp format specifications above.  So we choose some well-understood symbols instead.
my $through = '...';

sub is_positive_int($) {
    my $value = shift;
    if ( $value =~ /^\d+$/ && $value > 0 ) {
	return 1;
    }
    return 0;
}

sub is_valid_refresh_rate($) {
    my $rate = shift;
    return ( defined($newrefresh_rate)
	  && $newrefresh_rate ne ''
	  && $newrefresh_rate !~ /\D/
	  && $newrefresh_rate != 0
	  && $newrefresh_rate <= $slowest_refresh_rate );
}

# See the Config(3pm) man page for details of this magic formulation.
sub system_signal_name {
    my $signal_number = shift;
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

sub customgraph($$) {
    my ( $dbname, $dbhost, $dbuser, $dbpass, $dbtype ) = CollageQuery::readGroundworkDBConfig('monarch');
    my $dsn = '';
    if ( defined($dbtype) && $dbtype eq 'postgresql' ) {
	$dsn = "DBI:Pg:dbname=$dbname;host=$dbhost";
    }
    else {
	$dsn = "DBI:mysql:database=$dbname;host=$dbhost";
    }
    my $dbh = DBI->connect( $dsn, $dbuser, $dbpass, { 'AutoCommit' => 1 } )
      or die "Can't connect to database $dbname. Error: " . $DBI::errstr;
    my $graphcgi;

    # pass these values in from calling program
    my $host    = shift;
    my $svcdesc = shift;

    # here is the selection for exact match
    # Order by host puts * before a hostname.
    my $query = "SELECT * FROM performanceconfig where (service='$svcdesc' and type='nagios' and enable=1) ORDER BY host";
    my $sth   = $dbh->prepare($query);
    $sth->execute() or die $sth->errstr;
    while ( my $row = $sth->fetchrow_hashref() ) {
	## Last row will be a specific host -- if * and host are both set. Specific host has priority.
	if ( ( $$row{host} eq "*" ) or ( $$row{host} eq $host ) ) {
	    $graphcgi = $$row{graphcgi};
	}
    }
    $sth->finish;

    # If no match, then check pattern matches.
    if ( !$graphcgi ) {
	## print "No exact service name $svcdesc. Query database for service pattern matches. \n" if $debug_basic;
	## Order by host puts * before a hostname.
	$query = "SELECT * FROM performanceconfig where (type='nagios' and enable=1 and service_regx=1) ORDER BY service";
	my $sth = $dbh->prepare($query);
	$sth->execute() or die $sth->errstr;
	while ( my $row = $sth->fetchrow_hashref() ) {
	    ## Last row will be a specific host - if * and host are both set. Specific host has priority.
	    if ( ( $$row{host} eq "*" ) or ( $$row{host} eq $host ) ) {
		if ( $$row{service} ) {
		    my $serviceregx = qr/$$row{service}/;
		    if ( $svcdesc =~ /$serviceregx/ ) {
			## check if more than one pattern match
			if ($graphcgi) {
			    ## print "Multiple service matches. Pattern $$row{service} also matches. Ignored. \n" if $debug_basic;
			    next;
			}
			$graphcgi = $$row{graphcgi};
			## print "$svcdesc matches database service pattern $$row{service}. Using this entry. \n" if $debug_basic;
		    }
		}
	    }
	}
	$sth->finish;
    }

    if ( !$graphcgi ) {
	## print "No entry in performance database for select: $query \n" if $debug_basic;
    }
    else {
	## print "Match on $svcdesc and data is $graphcgi";
    }

    $dbh->disconnect();
    return $graphcgi;
}

sub views($$$) {
    my $name      = shift;
    my $file      = shift;
    my $delete    = shift;
    my $parser = XML::LibXML->new(
	ext_ent_handler => sub { die "INVALID FILE FORMAT: external entity references are not allowed in the view listing.\n" },
	no_network      => 1
    );
    my $tree      = undef;
    my %views     = ();
    my $views_out = "<?xml version=\"1.0\" encoding=\"iso-8859-1\" ?>\n<views>";
    if ( -e "$view_dir/views.xml" ) {
	eval {
	    $tree = $parser->parse_file("$view_dir/views.xml");
	};
	if ($@) {
	    $@ = HTML::Entities::encode($@);
	    $@ =~ s/\n/<br>/g;
	    if ($@ =~ s/external entity callback died: // || $@ =~ /external entity references are not allowed/) {
		## First undo the effect of the croak() call in XML::LibXML.
		$@ =~ s/ at \S+ line \d+<br>//;
		push @errors, "Invalid file content (views.xml):<br>$@";
	    }
	    elsif ($@ =~ /Attempt to load network entity/) {
		push @errors, "Invalid file content (views.xml):<br>INVALID FILE FORMAT: non-local entity references are not allowed in the view listing.<pre>$@</pre>";
	    }
	    else {
		push @errors, "Invalid file content: views.xml is not valid XML: $@";
	    }
	}
	unless (@errors) {
	    my $got_file = 0;
	    my $root     = $tree->getDocumentElement;
	    my @nodes    = $root->findnodes("//view");
	    foreach my $node (@nodes) {
		my $fname = $node->getAttribute('name');
		my $vname = $node->textContent;
		if ( $fname eq $file ) {
		    $got_file = 1;
		    unless ($delete) {
			if ($name) { $vname = $name }
			$views_out .= "\n <view name=\"$file\"><![CDATA[$vname]]></view>";
			$view_file{$vname} = $file;
			$file_view{$file}  = $vname;
		    }
		}
		else {
		    $views_out .= "\n <view name=\"$fname\"><![CDATA[$vname]]></view>";
		    $view_file{$vname} = $fname;
		    $file_view{$fname} = $vname;
		}
	    }
	    unless ($got_file or not $name or not $file or $delete) {
		$views_out .= "\n <view name=\"$file\"><![CDATA[$name]]></view>";
		$view_file{$name} = $file;
		$file_view{$file} = $name;
	    }
	}
    }
    elsif ($name and $file and not $delete) {
	$views_out .= "\n <view name=\"$file\"><![CDATA[$name]]></view>";
	$view_file{$name} = $file;
	$file_view{$file} = $name;
    }
    $views_out .= "\n</views>\n";
    if ($file) {
	if (
	    not open( FILE, '>', "$view_dir/views.xml" ) or not do {
		my $done = 1;
		print( FILE $views_out ) or $done = 0;
		close(FILE)              or $done = 0;
		$done;
	    }
	  )
	{
	    push @errors, "Error: Unable to write $view_dir/views.xml ($!)";
	}
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
    eval {
	my $connect = StorProc->dbconnect();
	%host_service_rrd = StorProc->get_host_service_rrd();
	my $result = StorProc->dbdisconnect();
	foreach my $host ( keys %host_service_rrd ) {
	    my @hf = ();
	    foreach my $service ( keys %{ $host_service_rrd{$host} } ) {
		push @hf, $service;
		$hosts{$host}   .= "$service,";
		$rrds{$service} .= "$host,";
	    }
	    $host_rrd{$host} = [@hf];
	}
	foreach my $service ( keys %rrds )  { chop $rrds{$service} }
	foreach my $host    ( keys %hosts ) { chop $hosts{$host}; }
    };
    if ($@) {
	chomp $@;
	print "Error in accessing Monarch:<br>$@<br>";
    }
}

sub today() {
    my ( $sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $isdst ) = localtime(time);
    $year += 1900;
    $mon  += 1;
    return "$year-$mon-$mday";
}

sub convert_date($$) {
    my $date     = shift;
    my $endpoint = shift;  # 'start' or 'end'
    if ( $date =~ /^(\d\d\d\d)[-\/](\d+)[-\/](\d+)$/ ) {
	my ( $year, $mon, $day ) = ( $1, $2, $3 );
	if ( $year < 2000 ) {
	    push @warnings, "\u${endpoint}ing year cannot be less than 2000; will be adjusted up.";
	    $start_date =~ s/$year/2000/ if $endpoint eq 'start';
	    $end_date   =~ s/$year/2000/ if $endpoint eq 'end';
	    $year = 2000;
	}
	## Maybe by the time the year 2037 approaches, all computers will be 64-bit
	## and we won't need to run this test.  In the meantime, we protect ourselves.
	if ( $year > 2037 ) {
	    push @warnings, "\u${endpoint}ing year cannot be greater than 2037; will be adjusted down.";
	    $start_date =~ s/$year/2037/ if $endpoint eq 'start';
	    $end_date   =~ s/$year/2037/ if $endpoint eq 'end';
	    $year = 2037;
	}
	$mon =~ s/^0//;
	$day =~ s/^0//;
	# In practice, the calendar widget will restrict the month and day-of-month
	# to correct ranges, but just in case ...
	if ( $mon > 12 ) { $mon = 12 }
	if ( $day > 31 ) { $day = 31 }
	if ( $mon =~ /9|4|6|11/ ) {
	    if ( $day > 30 ) {
		$day = 30;
	    }
	}
	elsif ( $mon eq '2' ) {
	    if ( $day > 29 ) {
		$day = 29;
	    }
	    if ( $day > 28 && $year !~ /2000|2004|2008|2012|2016|2020|2024|2028|2032|2036/ ) {
		$day = 28;
	    }
	}
	$mon--;
	if ( $endpoint eq 'end' ) {
	    $date = timelocal( 59, 59, 23, $day, $mon, $year );
	    $date += 1;
	}
	else {
	    $date = timelocal( 0, 0, 0, $day, $mon, $year );
	}
	return $date;
    }
    else {
	push @errors, "Invalid date $date (yyyy-mm-dd).";
    }
}

sub data_host(@) {
    my $form      = '';
    my $yeslegend = 0;
    my $type      = 'hosts';
    unless ( $hidden{'gen_view'} ) {
	my $now = time;
	$hidden{'gen_view'} = "view_$now";
    }
    my $view = $hidden{'gen_view'};
    if ( $hidden{'file'} ) {
	$view = $hidden{'file'};
	$view =~ s/\.xml//;
	$hidden{'gen_view'} = $view;
    }
    if (!$last_x_hours && !$last_x_days && !$date_range) {
	push @errors, "You have not chosen any time period for graphing (hours, days, and/or date range).  You can correct this situation using Configure.";
	$form .= Forms->form_errors( \@errors );
	@errors = ();
	$refresh_url = '';
    }
    if ( $layout eq 'consolidated' ) {
	my $pretitle = "Consolidated";
	my $line     = '';
	my $def      = '';
	my $i        = 0;
	my @hosts    = ();
	my %legend   = ();
	foreach my $host ( sort keys %host_rrd_select ) {
	    my $hname = $host;
	    $hname =~ s/\s/+/g;
	    push @hosts, $hname;
	    foreach my $data ( @{ $host_rrd_select{$host} } ) {
		my $sname = $data;
		print "host $host and data $data selected\n" if $debug_minimal;
		my $file    = $host_service_rrd{$host}{$data};
		my $ds_list = undef;
		if ($use_shared_rrd_module_for_info) {
		    ## $info is a hashref here.
		    my $info = RRDs::info($file);
		    my $ERR = RRDs::error;
		    if ($ERR) {
			push @errors, "Error executing RRD info command: $ERR";
		    }
		    else {
			foreach my $in (keys %$info) {
			    if ( $in =~ /^ds\[(\S+)\]\.index$/ ) {
				@$ds_list[ $$info{$in} ] = $1;
			    }
			}
		    }
		}
		else {
		    my $info = qx($rrd_bin/rrdtool info $file 2>&1)
		      or push @errors, "Error(s) executing $rrd_bin/rrdtool info $file (" . wait_status_message($?) . ").";
		    if ($info =~ /ERROR:/i) {
			push @errors, $info;
		    }
		    else {
			my @info = split( /\n/, $info );
			foreach my $in ( @info ) {
			    if ( $in =~ /^ds\[(\S+)\]\.index = (\d+)/ ) {
				@$ds_list[$2] = $1;
			    }
			}
		    }
		}
		if ( !defined $ds_list ) {
		    push @errors, "Invalid RRD (no data sources) -- Try to repair or delete and allow system to re-create RRD: $file";
		}
		else {
		    (my $double_sanitized_file = $file) =~ s/:/\\\\:/g;
		    (my $double_sanitized_host = $host) =~ s/:/\\\\:/g;
		    foreach my $ds_name (@$ds_list) {
			my $color = pop @colors;
			$legend{$host}{"$data\_$ds_name"} = $color;
			$def  .= "\"DEF:$ds_name$i=$double_sanitized_file:$ds_name:AVERAGE\", ";
			$line .= "\"LINE2:$ds_name$i$color:$ds_name $double_sanitized_host\", ";
			$i++;
		    }
		}
	    }
	}

	# Drop labels since we will be providing them external to the graph.
	# (Actually, it would be simpler to just not provide them above.)
	# (But, best would be to include them directly in the graph, so it
	# represents a complete, copyable, standalone record of this data.)
	$line =~ s/("LINE2:.*?):[^:"]+"/$1"/g;

	if ($last_x_hours) {
	    my $end              = time;
	    my $start            = $end - ( $hours * 3600 );
	    my $graph_start_time = strftime( $timestamp_format, localtime($start) );
	    my $graph_end_time   = strftime( $timestamp_format, localtime($end) );
	    my $title            = "<b>$pretitle</b>";
	    my $subtitle         = "Last $hours " . ( $hours == 1 ? 'hour' : 'hours' );
	    my $vlabel           = '';
	    my $defstring        = "$def $line";
	    $defstring =~ s/,\s$//;
	    $defstring .= ", \"COMMENT:<b>$subtitle</b>$center_format\"";
	    $defstring .= ", \"COMMENT:$graph_start_time $through $graph_end_time$center_format\"";
	    my $graph4 = "$view\_consolidated\_h_$days\.png";
	    $graph4 =~ s/\/|\s/-/g;
	    my $graphfile = "$graphdir/$graph4";
	    my ( $averages, $xsize, $ysize ) = undef;
	    my $evalstring =
'($averages, $xsize, $ysize) = RRDs::graph("$graphfile", "-P", "-s", $start, "-e", $end, "--vertical-label", $vlabel, "-t", $title, "-w", 600, "-h", 200, '
	      . $defstring . ');';
	    eval($evalstring);
	    error_out("$evalstring") if $debug_minimal;
	    my $err = RRDs::error;
	    if ($err) { push @errors, "$err $defstring" }
	    if (@errors) {
		$form .= Forms->form_errors( \@errors );
		@errors = ();
		$refresh_url = '';
	    }
	    $form .= Forms->graph( $type, $graph4, \@hosts );
	    $form .= Forms->legend( \%legend );
	}
	if ($last_x_days) {
	    my $end              = time;
	    my $start            = $end - ( $days * 86400 );
	    my $graph_start_time = strftime( $timestamp_format, localtime($start) );
	    my $graph_end_time   = strftime( $timestamp_format, localtime($end) );
	    my $title            = "<b>$pretitle</b>";
	    my $subtitle         = "Last $days " . ( $days == 1 ? 'day' : 'days' );
	    my $vlabel           = '';
	    my $defstring        = "$def $line";
	    $defstring =~ s/,\s$//;
	    $defstring .= ", \"COMMENT:<b>$subtitle</b>$center_format\"";
	    $defstring .= ", \"COMMENT:$graph_start_time $through $graph_end_time$center_format\"";
	    my $graph4 = "$view\_consolidated\_d_$days\.png";
	    $graph4 =~ s/\/|\s/-/g;
	    my $graphfile = "$graphdir/$graph4";
	    my ( $averages, $xsize, $ysize ) = undef;
	    my $evalstring =
'($averages, $xsize, $ysize) = RRDs::graph("$graphfile", "-P", "-s", $start, "-e", $end, "--vertical-label", $vlabel, "-t", $title, "-w", 600, "-h", 200, '
	      . $defstring . ');';
	    eval($evalstring);
	    my $err = RRDs::error;
	    if ($err) { push @errors, "$err $defstring" }
	    if (@errors) {
		$form .= Forms->form_errors( \@errors );
		@errors = ();
		$refresh_url = '';
	    }
	    $form .= Forms->graph( $type, $graph4, \@hosts );
	    unless ($last_x_hours) { $form .= Forms->legend( \%legend ) }
	}
	if ($date_range) {
	    my $start = convert_date($start_date, 'start');
	    unless ($start) {
		push @errors, "Start date is invalid: $start_date (yyyy-mm-dd).";
	    }
	    my $end = convert_date($end_date, 'end');
	    unless ($end) {
		push @errors, "End date in start $start end $end is invalid: $end_date (yyyy-mm-dd).";
	    }
	    if ( $end <= $start ) {
		push @errors, "End date cannot be earlier than start date.";
	    }
	    if (@warnings) {
		$form .= Forms->form_errors( \@warnings );
		@warnings = ();
	    }
	    if (@errors) {
		$form .= Forms->form_errors( \@errors );
		@errors = ();
		$refresh_url = '';
	    }
	    else {
		my $end_timestamp = time;
		$end_timestamp = $end if ($end < $end_timestamp);
		my $graph_start_time = strftime( $timestamp_format, localtime($start) );
		my $graph_end_time   = strftime( $timestamp_format, localtime($end_timestamp) );
		my $title            = "<b>$pretitle</b>";
		my $subtitle         = "From $start_date Through $end_date";
		$subtitle = "On $start_date" if ( $start_date eq $end_date );
		my $vlabel    = '';
		my $defstring = "$def $line";
		$defstring =~ s/,\s$//;
		$defstring .= ", \"COMMENT:<b>$subtitle</b>$center_format\"";
		$defstring .= ", \"COMMENT:$graph_start_time $through $graph_end_time$center_format\"";
		my $now    = time;
		my $graph4 = "$view\_consolidated\_dr_$days\.png";
		$graph4 =~ s/\/|\s/-/g;
		my $graphfile = "$graphdir/$graph4";
		my ( $averages, $xsize, $ysize ) = undef;
		my $evalstring =
'($averages, $xsize, $ysize) = RRDs::graph("$graphfile", "-P", "-s", $start, "-e", $end, "--vertical-label", $vlabel, "-t", $title, "-w", 600, "-h", 200, '
		  . $defstring . ');';
		eval($evalstring);
		my $err = RRDs::error;
		if ($err) { push @errors, "$err $defstring" }
		if (@errors) {
		    $form .= Forms->form_errors( \@errors );
		    @errors = ();
		    $refresh_url = '';
		}
		$form .= Forms->graph( $type, $graph4, \@hosts );
		unless ( $last_x_hours || $last_x_days ) { $form .= Forms->legend( \%legend ) }
	    }
	}
    }
    elsif ( $layout eq 'consolidated_host' ) {
	foreach my $host ( sort keys %host_rrd_select ) {
	    my @hosts    = ();
	    my %legend   = ();
	    my $pretitle = "Host: $host";
	    my $line     = '';
	    my $def      = '';
	    my $i        = 0;
	    my $hname    = $host;
	    $hname =~ s/\s/+/g;
	    push @hosts, $hname;

	    foreach my $data ( @{ $host_rrd_select{$host} } ) {
		my $sname   = $data;
		my $file    = $host_service_rrd{$host}{$data};
		my $ds_list = undef;
		if ($use_shared_rrd_module_for_info) {
		    ## $info is a hashref here.
		    my $info = RRDs::info($file);
		    my $ERR = RRDs::error;
		    if ($ERR) {
			push @errors, "Error executing RRD info command: $ERR";
		    }
		    else {
			foreach my $in (keys %$info) {
			    if ( $in =~ /^ds\[(\S+)\]\.index$/ ) {
				@$ds_list[ $$info{$in} ] = $1;
			    }
			}
		    }
		}
		else {
		    my $info = qx($rrd_bin/rrdtool info $file 2>&1)
		      or push @errors, "Error(s) executing $rrd_bin/rrdtool info $file (" . wait_status_message($?) . ").";
		    if ($info =~ /ERROR:/i) {
			push @errors, $info;
		    }
		    else {
			my @info = split( /\n/, $info );
			foreach my $in ( @info ) {
			    if ( $in =~ /^ds\[(\S+)\]\.index = (\d+)/ ) {
				@$ds_list[$2] = $1;
			    }
			}
		    }
		}
		if ( !defined $ds_list ) {
		    push @errors, "Invalid RRD (no data sources) -- Try to repair or delete and allow system to re-create RRD: $file";
		}
		else {
		    (my $double_sanitized_file = $file) =~ s/:/\\\\:/g;
		    (my $double_sanitized_host = $host) =~ s/:/\\\\:/g;
		    foreach my $ds_name (@$ds_list) {
			my $color = pop @colors;
			$legend{$host}{"$data\_$ds_name"} = $color;
			$def  .= "\"DEF:$ds_name$i=$double_sanitized_file:$ds_name:AVERAGE\", ";
			$line .= "\"LINE2:$ds_name$i$color:$ds_name $double_sanitized_host\", ";
			$i++;
		    }
		}
	    }

	    # Drop labels since we will be providing them external to the graph.
	    # (Actually, it would be simpler to just not provide them above.)
	    # (But, best would be to include them directly in the graph, so it
	    # represents a complete, copyable, standalone record of this data.)
	    $line =~ s/("LINE2:.*?):[^:"]+"/$1"/g;

	    if ($last_x_hours) {
		my $end              = time;
		my $start            = $end - ( $hours * 3600 );
		my $graph_start_time = strftime( $timestamp_format, localtime($start) );
		my $graph_end_time   = strftime( $timestamp_format, localtime($end) );
		my $title            = "<b>$pretitle</b>";
		my $subtitle         = "Last $hours " . ( $hours == 1 ? 'hour' : 'hours' );
		my $vlabel           = '';
		my $defstring        = "$def $line";
		$defstring =~ s/,\s$//;
		$defstring .= ", \"COMMENT:<b>$subtitle</b>$center_format\"";
		$defstring .= ", \"COMMENT:$graph_start_time $through $graph_end_time$center_format\"";
		my $graph4 = "$view\_$host\_h_$days\.png";
		$graph4 =~ s/\/|\s/-/g;
		my $graphfile = "$graphdir/$graph4";
		my ( $averages, $xsize, $ysize ) = undef;
		my $evalstring =
'($averages, $xsize, $ysize) = RRDs::graph("$graphfile", "-P", "-s", $start, "-e", $end, "--vertical-label", $vlabel, "-t", $title, "-w", 600, "-h", 200, '
		  . $defstring . ');';
		eval($evalstring);
		my $err = RRDs::error;
		if ($err) { push @errors, "$err $defstring" }
		if (@errors) {
		    $form .= Forms->form_errors( \@errors );
		    @errors = ();
		    $refresh_url = '';
		}
		$form .= Forms->graph( $type, $graph4, \@hosts );
	    }
	    if ($last_x_days) {
		my $end              = time;
		my $start            = $end - ( $days * 86400 );
		my $graph_start_time = strftime( $timestamp_format, localtime($start) );
		my $graph_end_time   = strftime( $timestamp_format, localtime($end) );
		my $title            = "<b>$pretitle</b>";
		my $subtitle         = "Last $days " . ( $days == 1 ? 'day' : 'days' );
		my $vlabel           = '';
		my $defstring        = "$def $line";
		$defstring =~ s/,\s$//;
		$defstring .= ", \"COMMENT:<b>$subtitle</b>$center_format\"";
		$defstring .= ", \"COMMENT:$graph_start_time $through $graph_end_time$center_format\"";
		my $graph4 = "$view\_$host\_d_$days\.png";
		$graph4 =~ s/\/|\s/-/g;
		my $graphfile = "$graphdir/$graph4";
		my ( $averages, $xsize, $ysize ) = undef;
		my $evalstring =
'($averages, $xsize, $ysize) = RRDs::graph("$graphfile", "-P", "-s", $start, "-e", $end, "--vertical-label", $vlabel, "-t", $title, "-w", 600, "-h", 200, '
		  . $defstring . ');';
		eval($evalstring);
		my $err = RRDs::error;
		if ($err) { push @errors, "$err $defstring" }
		if (@errors) {
		    $form .= Forms->form_errors( \@errors );
		    @errors = ();
		    $refresh_url = '';
		}
		$form .= Forms->graph( $type, $graph4, \@hosts );
	    }
	    if ($date_range) {
		my $start = convert_date($start_date, 'start');
		unless ($start) {
		    push @errors, "Start date is invalid: $start_date (yyyy-mm-dd).";
		}
		my $end = convert_date($end_date, 'end');
		unless ($end) {
		    push @errors, "End date in start $start end $end is invalid: $end_date (yyyy-mm-dd).";
		}
		if ( $end <= $start ) {
		    push @errors, "End date cannot be earlier than start date.";
		}
		if (@warnings) {
		    $form .= Forms->form_errors( \@warnings );
		    @warnings = ();
		}
		if (@errors) {
		    $form .= Forms->form_errors( \@errors );
		    @errors = ();
		    $refresh_url = '';
		}
		else {
		    my $end_timestamp = time;
		    $end_timestamp = $end if ($end < $end_timestamp);
		    my $graph_start_time = strftime( $timestamp_format, localtime($start) );
		    my $graph_end_time   = strftime( $timestamp_format, localtime($end_timestamp) );
		    my $title            = "<b>$pretitle</b>";
		    my $subtitle         = "From $start_date Through $end_date";
		    $subtitle = "On $start_date" if ( $start_date eq $end_date );
		    my $vlabel    = '';
		    my $defstring = "$def $line";
		    $defstring =~ s/,\s$//;
		    $defstring .= ", \"COMMENT:<b>$subtitle</b>$center_format\"";
		    $defstring .= ", \"COMMENT:$graph_start_time $through $graph_end_time$center_format\"";
		    my $now    = time;
		    my $graph4 = "$view\_$host\_dr_$days$now\.png";
		    $graph4 =~ s/\/|\s/-/g;
		    my $graphfile = "$graphdir/$graph4";
		    my ( $averages, $xsize, $ysize ) = undef;
		    my $evalstring =
'($averages, $xsize, $ysize) = RRDs::graph("$graphfile", "-P", "-s", $start, "-e", $end, "--vertical-label", $vlabel, "-t", $title, "-w", 600, "-h", 200, '
		      . $defstring . ');';
		    eval($evalstring);
		    my $err = RRDs::error;
		    if ($err) { push @errors, "$err $defstring" }
		    if (@errors) {
			$form .= Forms->form_errors( \@errors );
			@errors = ();
			$refresh_url = '';
		    }
		    $form .= Forms->graph( $type, $graph4, \@hosts );
		}
	    }
	    $form .= Forms->legend( \%legend );
	}
    }
    else {
	foreach my $host ( sort keys %host_rrd_select ) {
	    my @hosts = ();
	    my $hname = $host;
	    $hname =~ s/\s/+/g;
	    push @hosts, $hname;
	    my $pretitle = "Host: $host";
	    my $i        = 0;
	    if ($last_x_hours) {
		foreach my $data ( @{ $host_rrd_select{$host} } ) {
		    my %legend = ();
		    my $line   = '';
		    my $def    = '';
		    my $sname  = $data;
		    my $file   = $host_service_rrd{$host}{$data};
		    my %lines  = ();
		    if ($use_shared_rrd_module_for_info) {
			# FIX THIS:  use RRDs::info for this, now that it is patched to provide ds[].index values
		    }
		    else {
			## FIX MAJOR:  $file should be properly metacharacter-escaped, here and elsewhere
			my $info = qx($rrd_bin/rrdtool info $file 2>&1);
			if (not $info) {
			    push @errors, "Error(s) executing $rrd_bin/rrdtool info $file (" . wait_status_message($?) . ").";
			}
			elsif ( $info =~ /ERROR:/i ) {
			    push @errors, $info;
			}
			else {
			    my @info = split( /\n/, $info );
			    my $ds_counter = 0;
			    foreach my $in (@info) {
				if ( ( $in =~ /^ds\[(\S+)\]/ ) && ( $in =~ /type/ ) ) {
				    $in =~ /(?:ds\[)(\S+)(?:\])/;
				    $lines{$1} = $ds_counter;
				    $ds_counter++;
				}
			    }
			}
		    }
# ========================================================
# FIX THIS:  Clean this stuff up.  It's only here temporarily to help us in getting RRDs stuff implemented in this script.
# Similar code from process_service_perfdata_file:
#   1154     ## We have to trap the DS names in a list ordered by DS sequence number for the custom graph command.
#   1155     my $ds_list = undef;
#   1156     if ($use_shared_rrd_module_for_info) {
#   1157         foreach my $in (keys %$info) {
#   1158             if ( $in =~ /^ds\[(\S+)\]\.index$/ ) {
#   1159                 @$ds_list[ $$info{$in} ] = $1;
#   1160             }
#   1161         }
#   1162     }
#   1163     else {
#   1164         foreach my $in ( @info ) {
#   1165             if ( $in =~ /^ds\[(\S+)\]\.index = (\d+)/ ) {
#   1166                 @$ds_list[$2] = $1;
#   1167             }
#   1168         }
#   1169     }
#   1170     if ( !defined @$ds_list ) {
#   1171         log_message 'Invalid RRD (no data sources) -- Try to repair or delete and allow system to re-create RRD: ', $file;
#   1172         return '';
#   1173     }
# and later:
#   1205     # Simple case of direct substitution of numbered ds_source_n
#   1206     # replace the string ds_source_(number) with the DS number we found
#   1207     for ( my $j = 0 ; $j < @$ds_list ; $j++ ) {
#   1208         ## replace the string ds_source_(number) with the DS number we found
#   1209         my $ds_name = 'ds_source_' . "$j";
#   1210         $customgraph_command =~ s/$ds_name/@$ds_list[$j]/g;
#   1211     }
# what we get back from rrdtool info, with respect to sequencing the data sources:
# ds[load1].index = 0
# ds[load1_wn].index = 1
# ds[load1_cr].index = 2
# ds[load5].index = 3
# ds[load5_wn].index = 4
# ds[load5_cr].index = 5
# ds[load15].index = 6
# ds[load15_wn].index = 7
# ds[load15_cr].index = 8
# ========================================================

		    if (@errors) {
			$form .= Forms->form_errors( \@errors );
			@errors = ();
			$refresh_url = '';
		    }
		    else {
			## ---- custom graph commands need these ---
			my $ii      = 0;  # FIX THIS:  this is probably pointless
			my $ds_list = undef;
			## -------------------

			(my $double_sanitized_file = $file) =~ s/:/\\\\:/g;
			(my $double_sanitized_host = $host) =~ s/:/\\\\:/g;
			foreach my $ds_name ( sort keys %lines ) {
			    my $color = pop @colors;
			    $legend{$host}{"$data\_$ds_name"} = $color;
			    $def  .= "\"DEF:$ds_name$i=$double_sanitized_file:$ds_name:AVERAGE\", ";
			    $line .= "\"LINE2:$ds_name$i$color:$ds_name $double_sanitized_host\", ";

			    # Have to trap the ds names in a list for the custom graph command
			    @$ds_list[ $lines{$ds_name} ] = $ds_name;

			    $i++;
			    $ii++;
			}
			my $end      = time;
			my $start    = $end - ( $hours * 3600 );
			my $title    = "<b>$pretitle</b>";
			my $subtitle = "Last $hours " . ( $hours == 1 ? 'hour' : 'hours' );
			my $graph4   = "$view\_$host\_$data\_h_$days\.png";
			$graph4 =~ s/\/|\s/-/g;
			my $graphfile        = "$graphdir/$graph4";
			my $graph_start_time = strftime( $timestamp_format, localtime($start) );
			my $graph_end_time   = strftime( $timestamp_format, localtime($end) );

			# Look for the custom graph command right here
			my $customgraph_command = customgraph( $host, $data );

			# Now parse it to see if it should replace the default graphing command
			# That happens if it has an "rrdtool graph" anywhere in the field
			if ( defined($customgraph_command) && $customgraph_command =~ /rrdtool\s+graph/ ) {

			    # replace the string rrd_source with the selected rrd file path, making sure the filename always ends up safely quoted
			    (my $sanitized_file = $file) =~ s/:/\\:/g;
			    my $lead;
			    my $trail;
			    $customgraph_command =~ s/(?<=\s)(\S*)rrd_source(\S*)(?=\s)/
				($lead = $1, $trail = $2, $lead =~ m{"} && $trail =~ m{"})
				? "$lead$sanitized_file$trail" : "$lead\"$sanitized_file\"$trail"
			    /eg;

			    # reformat the rrdtool graph command to use our file name
			    $customgraph_command =~ s/rrdtool\s+graph\s+-/rrdtool graph $graphfile/;

			    # get rid of those pesky backslashes and newlines, but preserve the whitespace (word separator) aspect of a newline ...
			    $customgraph_command =~ s/\\\s//g;
			    $customgraph_command =~ s/\n/ /g;
			    $customgraph_command =~ s/\r//g;

			    # ... and the single quotes that get in the way ...
			    $customgraph_command =~ s/^'//;
			    $customgraph_command =~ s/'$//;

			    if ( !defined $ds_list ) {
				push @errors, "Invalid RRD (no data sources) -- Try to repair or delete and allow system to re-create RRD: $file";
			    }
			    else {
				# Handle the List Cases for vname parameters
				# Take care of listed DEFS, CDEFS, LINES, AREAS, PRINTS, GPRINTS, and STACKS
				if ( $customgraph_command =~ /\$LISTSTART\$(.*?)\$LISTEND\$/ ) {
				    my $tmpstring2 = '';
				    for ( my ($j, $vname) = (0, 'a') ; $j < @$ds_list ; ++$j, ++$vname ) {
					## Handle the list case
					my $tmpstring1 = $1;
					$tmpstring2 .= $tmpstring1 . " ";
					my $tmpstring3 = "$vname=\"$sanitized_file\":@$ds_list[$j]";
					$tmpstring2 =~ s/\$DEFLABEL\#\$/$tmpstring3/g;
					$tmpstring2 =~ s/\$CDEFLABEL\#\$/$vname/g;
					$tmpstring2 =~ s/\$DSLABEL\#\$/@$ds_list[$j]/g;
					my $color = pop @colors;
					$tmpstring2 =~ s/\$COLORLABEL\#\$/$color/g;
					## error_out("$tmpstring2") if $debug_minimal;
				    }
				    $customgraph_command =~ s/\$LISTSTART\$.*\$LISTEND\$/$tmpstring2/;
				}

				# Simple case of direct substitution of numbered ds_source_n
				# replace the string ds_source_(number) with the DS number we found
				for ( my $j = 0 ; $j < @$ds_list ; $j++ ) {

				    # replace the string ds_source_(number) with the DS number we found
				    my $ds_name = "ds_source_" . "$j";

				    # error_out("$ds_name") if $debug_minimal;
				    # error_out("@$ds_list[$j]") if $debug_minimal;
				    $customgraph_command =~ s/$ds_name/@$ds_list[$j]/g;
				}
			    }

			    # Take care of the underspecified cases
			    # if you don't see start or end, use the cgi's start and end
			    unless ( $customgraph_command =~ /\-\-start/ ) {
				$customgraph_command .= " --start $start ";
			    }
			    unless ( $customgraph_command =~ /\-\-end/ ) {
				$customgraph_command .= " --end $end ";
			    }

			    # if you don't see height or width, use the cgi's defaults
			    unless ( $customgraph_command =~ /\-\-height/ ) {
				$customgraph_command .= " --height 200 ";
			    }
			    unless ( $customgraph_command =~ /\-\-width/ ) {
				$customgraph_command .= " --width 600 ";
			    }
			    $customgraph_command .= " COMMENT:\"\\n\" \"COMMENT:$subtitle$center_format\"";
			    $customgraph_command .= " \"COMMENT:$graph_start_time $through $graph_end_time$center_format\"";

			    # Someday we might migrate this adjustment into the performanceconfig setup.
			    # But for now, this works here just fine.
			    $customgraph_command =~ s/--title=/--title="$host "/ if $customgraph_command !~ /\$HOST/;

			    # Someday we might migrate this adjustment into the performanceconfig setup.
			    # But for now, this works here just fine.
			    my $hs_title = "Host: $host   Service: $sname  ";
			    $hs_title = "$host / $sname   " if length($hs_title) > 84;
			    $customgraph_command =~ s/--start/--title "$hs_title" --start/
				if ($customgraph_command !~ /--title/ && $customgraph_command !~ / -t/);

			    # make the graph
			    error_out("$customgraph_command") if $debug_minimal;
			    if ($use_shared_rrd_module_for_graph) {
				# FIX THIS:  convert to using RRDs::graph
				# RIGHT HERE
				if (0) {
				    my $graphfile = "$graphdir/$graph4";
				    my ( $averages, $xsize, $ysize ) = undef;
				    my $evalstring =
					'($averages, $xsize, $ysize) = RRDs::graph("$graphfile", "-P", "-s", $start, "-e", $end, "--vertical-label", $vlabel, "-t", $title, "-w", 600, "-h", 200, '
				      . $defstring . ');';
				    eval($evalstring);
				    my $err = RRDs::error;
				    if ($err) { push @errors, "$err $defstring" }
				    if (@errors) {
					$form .= Forms->form_errors( \@errors );
					@errors = ();
					$refresh_url = '';
				    }
				    $form .= Forms->graph( $type, $graph4, \@hosts );
				}
				## Drop possible i/o redirection, which is useless in this context.
				$customgraph_command =~ s/\s2>&1//;
				my @command_args = command_arguments($customgraph_command);
				## Drop the shell command.
				shift @command_args;
				## Drop the RRD command.
				my $action_type = shift @command_args;
				if ( $action_type eq 'graph' ) {
				    RRDs::graph(@command_args);
				    my $ERR = RRDs::error;
				    if ($ERR) {
					print LOG 'ERROR:  Failed RRD graph command: ', $ERR if $debug_minimal;
					# FIX THIS:  abort somehow
				    }
				}
				else {
				    print LOG 'ERROR:  Invalid RRD graph command: ', $customgraph_command if $debug_minimal;
				    # FIX THIS:  abort somehow
				}
			    }
			    else {
				my $result = qx($customgraph_command 2>&1)
				  or push @errors, "Error(s) executing $customgraph_command (" . wait_status_message($?) . ")";
				error_out("result is $result") if $debug_minimal;
				unless ( $result =~ /\d+x\d+$/ ) {
				    push @errors, "Custom Command: $customgraph_command failed with result: $result";
				}
			    }
			}
			else {    # the usual (non-custom) case...
			    $yeslegend = 1;
			    my $vlabel = '';

			    # Drop labels since we will be providing them external to the graph.
			    # (Actually, it would be simpler to just not provide them above.)
			    # (But, best would be to include them directly in the graph, so it
			    # represents a complete, copyable, standalone record of this data.)
			    $line =~ s/("LINE2:.*?):[^:"]+"/$1"/g;

			    my $defstring = "$def $line";
			    $defstring =~ s/,\s$//;
			    $defstring .= ", \"COMMENT:<b>$subtitle</b>$center_format\"";
			    $defstring .= ", \"COMMENT:$graph_start_time $through $graph_end_time$center_format\"";

			    # error_out("effective: $defstring") if $debug_minimal;
			    my ( $averages, $xsize, $ysize ) = undef;
			    my $evalstring =
    '($averages, $xsize, $ysize) = RRDs::graph("$graphfile", "-P", "-s", $start, "-e", $end, "--vertical-label", $vlabel, "-t", $title, "-w", 600, "-h", 200, '
			      . $defstring . ');';
			    eval($evalstring);
			    my $err = RRDs::error;
			    error_out("$evalstring") if $debug_minimal;
			    if ($err) { push @errors, "$err $defstring" }
			}
			if (@errors) {
			    $form .= Forms->form_errors( \@errors );
			    @errors = ();
			    $refresh_url = '';
			}
			$form .= Forms->graph( $type, $graph4, \@hosts );
			if ( $yeslegend == 1 ) {
			    $form .= Forms->legend( \%legend );
			    $yeslegend = 0;
			}
		    }
		}
	    }
	    if ($last_x_days) {
		foreach my $data ( @{ $host_rrd_select{$host} } ) {
		    my $line   = '';
		    my $def    = '';
		    my %legend = ();
		    my $sname  = $data;
		    my $file   = $host_service_rrd{$host}{$data};
		    my %lines  = ();
		    if ($use_shared_rrd_module_for_info) {
			# FIX THIS:  use RRDs::info for this, now that it is patched to provide ds[].index values
		    }
		    else {
			my $info = qx($rrd_bin/rrdtool info $file 2>&1)
			  or push @errors, "Error(s) executing $rrd_bin/rrdtool info $file (" . wait_status_message($?) . ").";
			if ($info =~ /ERROR:/i) {
			    push @errors, $info;
			}
			else {
			    my @info = split( /\n/, $info );
			    my $ds_counter = 0;
			    foreach my $in (@info) {
				if ( ( $in =~ /^ds\[(\S+)\]/ ) && ( $in =~ /type/ ) ) {
				    $in =~ /(?:ds\[)(\S+)(?:\])/;
				    $lines{$1} = $ds_counter;
				    $ds_counter++;
				}
			    }
			}
		    }

		    if (@errors) {
			$form .= Forms->form_errors( \@errors );
			@errors = ();
			$refresh_url = '';
		    }
		    else {
			## ---- custom graph commands need these ---
			my $ii      = 0;  # FIX THIS:  this is probably pointless
			my $ds_list = undef;
			## -------------------

			(my $double_sanitized_file = $file) =~ s/:/\\\\:/g;
			(my $double_sanitized_host = $host) =~ s/:/\\\\:/g;
			foreach my $ds_name ( sort keys %lines ) {
			    my $color = pop @colors;
			    $legend{$host}{"$data\_$ds_name"} = $color;
			    $def  .= "\"DEF:$ds_name$i=$double_sanitized_file:$ds_name:AVERAGE\", ";
			    $line .= "\"LINE2:$ds_name$i$color:$ds_name $double_sanitized_host\", ";

			    # Have to trap the ds names in a list for the custom graph command
			    @$ds_list[ $lines{$ds_name} ] = $ds_name;
			    $i++;
			    $ii++;
			}
			my $end      = time;
			my $start    = $end - ( $days * 86400 );
			my $title    = "<b>$pretitle</b>";
			my $subtitle = "Last $days " . ( $days == 1 ? 'day' : 'days' );
			my $graph4   = "$view\_$host\_$data\_d_$days\.png";
			$graph4 =~ s/\/|\s/-/g;
			my $graphfile        = "$graphdir/$graph4";
			my $graph_start_time = strftime( $timestamp_format, localtime($start) );
			my $graph_end_time   = strftime( $timestamp_format, localtime($end) );

			# Look for the custom graph command right here
			my $customgraph_command = customgraph( $host, $data );

			# error_out("$customgraph_command") if $debug_minimal;

			# Now parse it to see if it should replace the default graphing command
			# That happens if it has an "rrdtool graph" anywhere in the field
			if ( defined($customgraph_command) && $customgraph_command =~ /rrdtool\s+graph/ ) {

			    # replace the string rrd_source with the selected rrd file path, making sure the filename always ends up safely quoted
			    (my $sanitized_file = $file) =~ s/:/\\:/g;
			    my $lead;
			    my $trail;
			    $customgraph_command =~ s/(?<=\s)(\S*)rrd_source(\S*)(?=\s)/
				($lead = $1, $trail = $2, $lead =~ m{"} && $trail =~ m{"})
				? "$lead$sanitized_file$trail" : "$lead\"$sanitized_file\"$trail"
			    /eg;

			    # reformat the rrdtool graph command to use our file name
			    $customgraph_command =~ s/rrdtool\s+graph\s+-/rrdtool graph $graphfile/;

			    # get rid of those pesky backslashes and newlines, but preserve the whitespace (word separator) aspect of a newline ...
			    $customgraph_command =~ s/\\\s//g;
			    $customgraph_command =~ s/\n/ /g;
			    $customgraph_command =~ s/\r//g;

			    # and the single quotes that get in the way...
			    $customgraph_command =~ s/^'//;
			    $customgraph_command =~ s/'$//;

			    if ( !defined $ds_list ) {
				push @errors, "Invalid RRD (no data sources) -- Try to repair or delete and allow system to re-create RRD: $file";
			    }
			    else {
				# Handle the List Cases for vname parameters
				# Take care of listed DEFS, CDEFS, LINES, AREAS, PRINTS, GPRINTS, and STACKS
				if ( $customgraph_command =~ /\$LISTSTART\$(.*?)\$LISTEND\$/ ) {
				    my $tmpstring2 = '';
				    for ( my ($j, $vname) = (0, 'a') ; $j < @$ds_list ; ++$j, ++$vname ) {
					## Handle the list case
					my $tmpstring1 = $1;
					$tmpstring2 .= $tmpstring1 . " ";
					my $tmpstring3 = "$vname=\"$sanitized_file\":@$ds_list[$j]";
					$tmpstring2 =~ s/\$DEFLABEL\#\$/$tmpstring3/g;
					$tmpstring2 =~ s/\$CDEFLABEL\#\$/$vname/g;
					$tmpstring2 =~ s/\$DSLABEL\#\$/@$ds_list[$j]/g;
					my $color = pop @colors;
					$tmpstring2 =~ s/\$COLORLABEL\#\$/$color/g;
					error_out("$tmpstring2") if $debug_minimal;
				    }
				    $customgraph_command =~ s/\$LISTSTART\$.*\$LISTEND\$/$tmpstring2/;
				}

				# replace the string ds_source_(number) with the DS number we found
				for ( my $j = 0 ; $j < @$ds_list ; $j++ ) {
				    my $ds_name = "ds_source_" . "$j";
				    error_out("$ds_name")      if $debug_minimal;
				    error_out("@$ds_list[$j]") if $debug_minimal;
				    $customgraph_command =~ s/$ds_name/@$ds_list[$j]/g;
				}
			    }

			    # Take care of the underspecified cases
			    # if you don't see start or end, use the cgi's start and end
			    unless ( $customgraph_command =~ /\-\-start/ ) {
				$customgraph_command .= " --start $start ";
			    }
			    unless ( $customgraph_command =~ /\-\-end/ ) {
				$customgraph_command .= " --end $end ";
			    }

			    # if you don't see height or width, use the cgi's defaults
			    unless ( $customgraph_command =~ /\-\-height/ ) {
				$customgraph_command .= " --height 200 ";
			    }
			    unless ( $customgraph_command =~ /\-\-width/ ) {
				$customgraph_command .= " --width 600 ";
			    }
			    $customgraph_command .= " COMMENT:\"\\n\" \"COMMENT:$subtitle$center_format\"";
			    $customgraph_command .= " \"COMMENT:$graph_start_time $through $graph_end_time$center_format\"";

			    # Someday we might migrate this adjustment into the performanceconfig setup.
			    # But for now, this works here just fine.
			    $customgraph_command =~ s/--title=/--title="$host "/ if $customgraph_command !~ /\$HOST/;

			    # Someday we might migrate this adjustment into the performanceconfig setup.
			    # But for now, this works here just fine.
			    my $hs_title = "Host: $host   Service: $sname  ";
			    $hs_title = "$host / $sname   " if length($hs_title) > 84;
			    $customgraph_command =~ s/--start/--title "$hs_title" --start/
				if ($customgraph_command !~ /--title/ && $customgraph_command !~ / -t/);

			    # make the graph
			    error_out("$customgraph_command") if $debug_minimal;
			    if ($use_shared_rrd_module_for_graph) {
				# FIX THIS:  convert to using RRDs::graph
				# RIGHT HERE
			    }
			    else {
				my $result = qx($customgraph_command 2>&1)
				  or push @errors, "Error(s) executing $customgraph_command (" . wait_status_message($?) . ")";
				error_out("result is $result") if $debug_minimal;
				unless ( $result =~ /\d+x\d+$/ ) {
				    push @errors, "Custom Command: $customgraph_command failed with result: $result";
				}
			    }
			}
			else {    # the usual (non-custom) case...
			    $yeslegend = 1;
			    my $vlabel = '';

			    # Drop labels since we will be providing them external to the graph.
			    # (Actually, it would be simpler to just not provide them above.)
			    # (But, best would be to include them directly in the graph, so it
			    # represents a complete, copyable, standalone record of this data.)
			    $line =~ s/("LINE2:.*?):[^:"]+"/$1"/g;

			    my $defstring = "$def $line";
			    $defstring =~ s/,\s$//;
			    $defstring .= ", \"COMMENT:<b>$subtitle</b>$center_format\"";
			    $defstring .= ", \"COMMENT:$graph_start_time $through $graph_end_time$center_format\"";

			    # error_out("effective: $defstring") if $debug_minimal;
			    my ( $averages, $xsize, $ysize ) = undef;
			    my $evalstring =
'($averages, $xsize, $ysize) = RRDs::graph("$graphfile", "-P", "-s", $start, "-e", $end, "--vertical-label", $vlabel, "-t", $title, "-w", 600, "-h", 200, '
			      . $defstring . ');';
			    eval($evalstring);
			    my $err = RRDs::error;
			    error_out("$evalstring") if $debug_minimal;
			    if ($err) { push @errors, "$err $defstring" }
			}
			if (@errors) {
			    $form .= Forms->form_errors( \@errors );
			    @errors = ();
			    $refresh_url = '';
			}
			$form .= Forms->graph( $type, $graph4, \@hosts );
			if ( $yeslegend == 1 ) {
			    $form .= Forms->legend( \%legend );
			    $yeslegend = 0;
			}
		    }
		}
	    }
	    if ($date_range) {
		my $start = convert_date($start_date, 'start');
		unless ($start) {
		    push @errors, "Start date is invalid: $start_date (yyyy-mm-dd).";
		}
		my $end = convert_date($end_date, 'end');
		unless ($end) {
		    push @errors, "End date in start $start end $end is invalid: $end_date (yyyy-mm-dd).";
		}
		if ( $end <= $start ) {
		    push @errors, "End date cannot be earlier than start date.";
		}
		if (@warnings) {
		    $form .= Forms->form_errors( \@warnings );
		    @warnings = ();
		}
		if (@errors) {
		    $form .= Forms->form_errors( \@errors );
		    @errors = ();
		    $refresh_url = '';
		}
		else {
		    my $end_timestamp = time;
		    $end_timestamp = $end if ($end < $end_timestamp);
		    my $graph_start_time = strftime( $timestamp_format, localtime($start) );
		    my $graph_end_time   = strftime( $timestamp_format, localtime($end_timestamp) );
		    foreach my $data ( @{ $host_rrd_select{$host} } ) {
			my $line   = '';
			my $def    = '';
			my %legend = ();
			my $sname  = $data;
			my $file   = $host_service_rrd{$host}{$data};
			my %lines  = ();
			if ($use_shared_rrd_module_for_info) {
			    # FIX THIS:  use RRDs::info for this, now that it is patched to provide ds[].index values
			}
			else {
			    my $info = qx($rrd_bin/rrdtool info $file 2>&1)
			      or push @errors, "Error(s) executing $rrd_bin/rrdtool info $file (" . wait_status_message($?) . ").";
			    if ($info =~ /ERROR:/i) {
				push @errors, $info;
			    }
			    else {
				my @info = split( /\n/, $info );
				my $ds_counter = 0;
				foreach my $in (@info) {
				    if ( ( $in =~ /^ds\[(\S+)\]/ ) && ( $in =~ /type/ ) ) {
					$in =~ /(?:ds\[)(\S+)(?:\])/;
					$lines{$1} = $ds_counter;
					$ds_counter++;
				    }
				}
			    }
			}

			if (@errors) {
			    $form .= Forms->form_errors( \@errors );
			    @errors = ();
			    $refresh_url = '';
			}
			else {
			    ## ---- custom graph commands need these ---
			    my $ii      = 0;  # FIX THIS:  this is probably pointless
			    my $ds_list = undef;
			    ## -------------------

			    (my $double_sanitized_file = $file) =~ s/:/\\\\:/g;
			    (my $double_sanitized_host = $host) =~ s/:/\\\\:/g;
			    foreach my $ds_name ( sort keys %lines ) {
				my $color = pop @colors;
				$legend{$host}{"$data\_$ds_name"} = $color;
				$def  .= "\"DEF:$ds_name$i=$double_sanitized_file:$ds_name:AVERAGE\", ";
				$line .= "\"LINE2:$ds_name$i$color:$ds_name $double_sanitized_host\", ";

				# Have to trap the ds names in a list for the custom graph command
				@$ds_list[ $lines{$ds_name} ] = $ds_name;
				$i++;
				$ii++;
			    }

			    my $title    = "<b>$pretitle</b>";
			    my $subtitle = "From $start_date Through $end_date";
			    $subtitle = "On $start_date" if ( $start_date eq $end_date );
			    my $graph4 = "$view\_$host\_$data\_dr_$days\.png";
			    $graph4 =~ s/\/|\s/-/g;
			    my $graphfile = "$graphdir/$graph4";

			    # Look for the custom graph command right here
			    my $customgraph_command = customgraph( $host, $data );

			    # error_out("$customgraph_command") if $debug_minimal;

			    # Now parse it to see if it should replace the default graphing command
			    # That happens if it has an "rrdtool graph" anywhere in the field
			    if ( defined($customgraph_command) && $customgraph_command =~ /rrdtool\s+graph/ ) {

				# replace the string rrd_source with the selected rrd file path, making sure the filename always ends up safely quoted
				(my $sanitized_file = $file) =~ s/:/\\:/g;
				my $lead;
				my $trail;
				$customgraph_command =~ s/(?<=\s)(\S*)rrd_source(\S*)(?=\s)/
				    ($lead = $1, $trail = $2, $lead =~ m{"} && $trail =~ m{"})
				    ? "$lead$sanitized_file$trail" : "$lead\"$sanitized_file\"$trail"
				/eg;

				# reformat the rrdtool graph command to use our file name
				$customgraph_command =~ s/rrdtool\s+graph\s+-/rrdtool graph $graphfile/;

				# get rid of those pesky backslashes and newlines,
				# but preserve the whitespace (word separator) aspect of a newline ...
				$customgraph_command =~ s/\\\s//g;
				$customgraph_command =~ s/\n/ /g;
				$customgraph_command =~ s/\r//g;

				# and the single quotes that get in the way...
				$customgraph_command =~ s/^'//;
				$customgraph_command =~ s/'$//;

				if ( !defined $ds_list ) {
				    push @errors, "Invalid RRD (no data sources) -- Try to repair or delete and allow system to re-create RRD: $file";
				}
				else {
				    # Handle the List Cases for vname parameters
				    # Take care of listed DEFS, CDEFS, LINES, AREAS, PRINTS, GPRINTS, and STACKS
				    if ( $customgraph_command =~ /\$LISTSTART\$(.*?)\$LISTEND\$/ ) {
					my $tmpstring2 = '';
					for ( my ($j, $vname) = (0, 'a') ; $j < @$ds_list ; ++$j, ++$vname ) {
					    ## Handle the list case
					    my $tmpstring1 = $1;
					    $tmpstring2 .= $tmpstring1 . " ";
					    my $tmpstring3 = "$vname=\"$sanitized_file\":@$ds_list[$j]";
					    $tmpstring2 =~ s/\$DEFLABEL\#\$/$tmpstring3/g;
					    $tmpstring2 =~ s/\$CDEFLABEL\#\$/$vname/g;
					    $tmpstring2 =~ s/\$DSLABEL\#\$/@$ds_list[$j]/g;
					    my $color = pop @colors;
					    $tmpstring2 =~ s/\$COLORLABEL\#\$/$color/g;
					    error_out("$tmpstring2") if $debug_minimal;
					}
					$customgraph_command =~ s/\$LISTSTART\$.*\$LISTEND\$/$tmpstring2/;
				    }

				    # replace the string ds_source_(number) with the DS number we found
				    for ( my $j = 0 ; $j < @$ds_list ; $j++ ) {
					my $ds_name = "ds_source_" . "$j";
					error_out("$ds_name")      if $debug_minimal;
					error_out("@$ds_list[$j]") if $debug_minimal;
					$customgraph_command =~ s/$ds_name/@$ds_list[$j]/g;
				    }
				}

				# Take care of the underspecified cases
				# if you don't see start or end, use the cgi's start and end
				unless ( $customgraph_command =~ /\-\-start/ ) {
				    $customgraph_command .= " --start $start ";
				}
				unless ( $customgraph_command =~ /\-\-end/ ) {
				    $customgraph_command .= " --end $end ";
				}

				# if you don't see height or width, use the cgi's defaults
				unless ( $customgraph_command =~ /\-\-height/ ) {
				    $customgraph_command .= " --height 200 ";
				}
				unless ( $customgraph_command =~ /\-\-width/ ) {
				    $customgraph_command .= " --width 600 ";
				}
				$customgraph_command .= " COMMENT:\"\\n\" \"COMMENT:$subtitle$center_format\"";
				$customgraph_command .= " \"COMMENT:$graph_start_time $through $graph_end_time$center_format\"";

				# Someday we might migrate this adjustment into the performanceconfig setup.
				# But for now, this works here just fine.
				$customgraph_command =~ s/--title=/--title="$host "/ if $customgraph_command !~ /\$HOST/;

				# Someday we might migrate this adjustment into the performanceconfig setup.
				# But for now, this works here just fine.
				my $hs_title = "Host: $host   Service: $sname  ";
				$hs_title = "$host / $sname   " if length($hs_title) > 84;
				$customgraph_command =~ s/--start/--title "$hs_title" --start/
				    if ($customgraph_command !~ /--title/ && $customgraph_command !~ / -t/);

				# make the graph
				error_out("$customgraph_command") if $debug_minimal;
				if ($use_shared_rrd_module_for_graph) {
				    # FIX THIS:  convert to using RRDs::graph
				    # RIGHT HERE
				}
				else {
				    my $result = qx($customgraph_command 2>&1)
				      or push @errors, "Error(s) executing $customgraph_command (" . wait_status_message($?) . ")";
				    error_out("result is $result") if $debug_minimal;
				    unless ( $result =~ /\d+x\d+$/ ) {
					push @errors, "Custom Command: $customgraph_command failed with result: $result";
				    }
				}
			    }
			    else {    # the usual (non-custom) case...
				$yeslegend = 1;
				my $vlabel = '';

				# Drop labels since we will be providing them external to the graph.
				# (Actually, it would be simpler to just not provide them above.)
				# (But, best would be to include them directly in the graph, so it
				# represents a complete, copyable, standalone record of this data.)
				$line =~ s/("LINE2:.*?):[^:"]+"/$1"/g;

				my $defstring = "$def $line";
				$defstring =~ s/,\s$//;
				$defstring .= ", \"COMMENT:<b>$subtitle</b>$center_format\"";
				$defstring .= ", \"COMMENT:$graph_start_time $through $graph_end_time$center_format\"";

				my $graph4 = "$view\_$host\_$data\_dr_$days\.png";
				$graph4 =~ s/\/|\s/-/g;
				my $graphfile = "$graphdir/$graph4";
				my ( $averages, $xsize, $ysize ) = undef;
				my $evalstring =
'($averages, $xsize, $ysize) = RRDs::graph("$graphfile", "-P", "-s", $start, "-e", $end, "--vertical-label", $vlabel, "-t", $title, "-w", 600, "-h", 200, '
				  . $defstring . ');';
				eval($evalstring);
				my $err = RRDs::error;
				if ($err) { push @errors, "$err $defstring" }
			    }
			    if (@errors) {
				$form .= Forms->form_errors( \@errors );
				@errors = ();
				$refresh_url = '';
			    }
			    $form .= Forms->graph( $type, $graph4, \@hosts );
			    if ( $yeslegend == 1 ) {
				unless ( $last_x_hours || $last_x_days ) { $form .= Forms->legend( \%legend ) }
				$yeslegend = 0;
			    }
			}
		    }
		}
	    }
	}
    }
    return $form;
}

sub read_config() {
    my $parser = XML::LibXML->new(
	ext_ent_handler => sub { die "INVALID FILE FORMAT: external entity references are not allowed in a view config file.\n" },
	no_network      => 1
    );
    my $tree = undef;
    eval {
	$tree = $parser->parse_file("$view_dir/$hidden{'file'}");
    };
    if ($@) {
	my $file = HTML::Entities::encode( $hidden{'file'} );
	$@ = HTML::Entities::encode($@);
	$@ =~ s/\n/<br>/g;
	if ($@ =~ s/external entity callback died: // || $@ =~ /external entity references are not allowed/) {
	    ## First undo the effect of the croak() call in XML::LibXML.
	    $@ =~ s/ at \S+ line \d+<br>//;
	    push @errors, "Invalid file content ($file):<br>$@";
	}
	elsif ($@ =~ /Attempt to load network entity/) {
	    push @errors, "Invalid file content ($file):<br>INVALID FILE FORMAT: non-local entity references are not allowed in a view config file.<pre>$@</pre>";
	}
	else {
	    push @errors, "Invalid file content: $file is not valid XML: $@";
	}
    }
    unless (@errors) {
	my $root  = $tree->getDocumentElement;
	my @nodes = $root->findnodes("//view");
	foreach my $node (@nodes) {
	    $hidden{'view'} = $node->getAttribute('name');
	    my @siblings = $node->getChildnodes();
	    foreach my $sibling (@siblings) {
		if ( $sibling->nodeName() eq 'host' ) {
		    $hidden{'host'} = $sibling->getAttribute('name');
		    if ( $sibling->hasChildNodes() ) {
			my @children = $sibling->getChildnodes();
			foreach my $child (@children) {
			    if ( $child->hasAttributes ) {
				my $data = $child->getAttribute('name');
				push @{ $host_rrd_select{ $hidden{'host'} } }, $data;
			    }
			}
		    }
		}
		elsif ( $sibling->nodeName() eq 'layout' ) {
		    $hidden{'layout'} = $sibling->textContent;
		    $layout = $hidden{'layout'};
		}
		elsif ( $sibling->nodeName() eq 'last_x_days' ) {
		    $hidden{'last_x_days'} = $sibling->textContent;
		    $last_x_days = $hidden{'last_x_days'};
		}
		elsif ( $sibling->nodeName() eq 'days' ) {
		    $hidden{'days'} = $sibling->textContent;
		    $days = $hidden{'days'};
		}
		elsif ( $sibling->nodeName() eq 'last_x_hours' ) {
		    $hidden{'last_x_hours'} = $sibling->textContent;
		    $last_x_hours = $hidden{'last_x_hours'};
		}
		elsif ( $sibling->nodeName() eq 'hours' ) {
		    $hidden{'hours'} = $sibling->textContent;
		    $hours = $hidden{'hours'};
		}
		elsif ( $sibling->nodeName() eq 'date_range' ) {
		    $hidden{'date_range'} = $sibling->textContent;
		    $date_range = $hidden{'date_range'};
		}
		elsif ( $sibling->nodeName() eq 'start_date' ) {
		    $hidden{'start_date'} = $sibling->textContent;
		    $start_date = $hidden{'start_date'};
		}
		elsif ( $sibling->nodeName() eq 'end_date' ) {
		    $hidden{'end_date'} = $sibling->textContent;
		    $end_date = $hidden{'end_date'};
		}
		elsif ( $sibling->nodeName() eq 'newauto_refresh' ) {
		    $newauto_refresh = $sibling->textContent;
		    $auto_refresh    = $sibling->textContent;
		}
		elsif ( $sibling->nodeName() eq 'newrefresh_rate' ) {
		    ( $newrefresh_rate = $sibling->textContent ) =~ s/^\s*|\s*$//g;
		    ( $refresh_rate    = $sibling->textContent ) =~ s/^\s*|\s*$//g;
		}
	    }
	}
    }
}

# ================

if (0) {
    my $exit_status = 0;

    my $rrdgraph_command = $ARGV[0];

    if ($use_shared_rrd_module_for_graph) {
	## Drop possible i/o redirection, which is useless in this context.
	$rrdgraph_command =~ s/\s2>&1//;
	my @command_args = command_arguments($rrdgraph_command);
	## Drop the shell command.
	shift @command_args;
	## Drop the RRD command.
	my $action_type = shift @command_args;
	if ( $action_type eq 'graph' ) {
	    RRDs::graph(@command_args);
	    my $ERR = RRDs::error;
	    if ($ERR) {
		print LOG 'ERROR:  Failed RRD graph command: ', $ERR if $debug_minimal;
		$exit_status = 1;
	    }
	}
	else {
	    print LOG 'ERROR:  Invalid RRD graph command: ', $rrdgraph_command if $debug_minimal;
	    $exit_status = 1;
	}
    }
    else {
	system ($rrdgraph_command);
    }

    exit $exit_status;
}

# ================

# Chop up a string containing all the command-invocation arguments as it would be seen by a spawning shell,
# into just its individual arguments, in exactly the same way that the shell would have done so.  Actually,
# all we handle here is quoting and escaping such quotes, not filename globbing, subshell invocation, pipes,
# additional commands in a list, shell variable interpolation, etc.)
sub command_arguments {
    my $arg_string = shift;
    my @arguments  = ();

    # Samples of shell handling of quote and escape characters:
    #
    # $ echo 'foo\'
    # foo\
    # $ echo 'foo\''bar'
    # foo\bar
    # $ echo 'foo\"bar'
    # foo\"bar
    # $ echo 'foo\\bar'
    # foo\\bar
    # $ echo "foo\'bar"
    # foo\'bar
    # $ echo "foo\"bar"
    # foo"bar
    # $ echo "foo\\bar"
    # foo\bar
    # $ echo foo\bar
    # foobar
    # $ echo foo\'bar
    # foo'bar
    # $ echo foo\"bar
    # foo"bar
    # $ echo foo\\bar
    # foo\bar

    $arg_string =~ s/^\s+//;

    my $have_arg = 0;
    my $arg      = '';
    my $piece;
    while ( $arg_string =~ /^./ ) {
	if ( $arg_string =~ /^'([^']*)'/gco ) {
	    $arg .= $1;
	    $have_arg = 1;
	}
	elsif ( $arg_string =~ /^"([^"\\]*(?:(?:\\"|\\\\|\\)*[^"\\]*)*)"/gco ) {
	    $piece = $1;
	    ## substitute both \" -> " and \\ -> \ at the same time, left-to-right
	    $piece =~ s:\\(["\\]):$1:g;
	    $arg .= $piece;
	    $have_arg = 1;
	}
	elsif ( $arg_string =~ /^\\(.)/gco ) {
	    $arg .= $1;
	    $have_arg = 1;
	}
	elsif ( $arg_string =~ /^([^'"\\ ]+)/gco ) {
	    $arg .= $1;
	    $have_arg = 1;
	}
	elsif ( $arg_string =~ /^\s+/gco ) {
	    push @arguments, $arg;
	    $have_arg = 0;
	    $arg      = '';
	}
	elsif ( $arg_string =~ /(.+)/gco ) {
	    ## Illegal argument construction (likely, unbalanced quotes).
	    ## Let's just bail and drop the rest of the line.
	    print LOG "RRD command error, starting here: $1\n" if $debug_minimal;
	    last;
	}
	## remove the matched part from $arg_string
	$arg_string = substr( $arg_string, pos($arg_string) );
    }
    if ($have_arg) {
	push @arguments, $arg;
    }

    return @arguments;
}

print "Content-type: text/html\n\n";

my $page_title = 'Performance Graphs';
unless (%view_file) { views( '', '', '' ) }
if ( $query->param('update_left') ) {
    print Forms->left_page( \%view_file );
}
elsif ( $query->param('update_main') ) {
    $hidden{'nocache'}     = time;
    $hidden{'update_main'} = 1;
    my $refresh_left = 0;

    if ( $query->param('host') ) {
	my $host = $query->param('host');
	$host = uri_unescape($host);
	my @rrds = $query->$multi_param('rrds');
	foreach my $rrd (@rrds) {
	    $rrd = uri_unescape($rrd);
	    if ($rrd) { push @{ $host_rrd_select{$host} }, $rrd }
	}
    }

    if ( $query->param('rrd') ) {
	my $rrd = $query->param('rrd');
	$rrd = uri_unescape($rrd);
	my @hosts = $query->$multi_param('hosts');
	foreach my $host (@hosts) {
	    $host = uri_unescape($host);
	    if ($host) { push @{ $host_rrd_select{$host} }, $rrd }
	}
    }

    my @hosts = $query->$multi_param('host_data');
    foreach my $host (@hosts) {
	unless ( $query->param("remove_$host") ) {
	    $host = uri_unescape($host);
	    my @vals = split( /\%\%/, $host );
	    push @{ $host_rrd_select{ $vals[0] } }, $vals[1];
	}
    }

    $hidden{'host_list'} = $query->param('host_list');
    unless ( $hidden{'host_list'} ) {
	my @hosts = $query->$multi_param('hosts');
	foreach my $host (@hosts) {
	    $host = uri_unescape($host);
	    $hidden{'host_list'} .= "$host,";
	}
	chop $hidden{'host_list'} if defined $hidden{'host_list'};
    }

    $hidden{'graph_list'} = $query->param('graph_list');
    unless ( $hidden{'graph_list'} ) {
	my @graphs = $query->$multi_param('graphs');
	foreach my $graph (@graphs) {
	    $graph = uri_unescape($graph);
	    $hidden{'graph_list'} .= "$graph,";
	}
	chop $hidden{'graph_list'} if defined $hidden{'graph_list'};
    }

    if ( $query->param('config') ) {
	delete $hidden{'layout'};
	delete $hidden{'date_range'};
	delete $hidden{'start_date'};
	delete $hidden{'end_date'};
	delete $hidden{'last_x_days'};
	delete $hidden{'last_x_hours'};
	delete $hidden{'days'};
	delete $hidden{'hours'};
    }

    if ($debug_minimal) {
	foreach my $name ( $query->$multi_param ) {
	    my $v = $query->param($name);
	    error_out("$name $v");
	}
    }

    my @errors    = ();
    my %genreport = ( 'name' => 'gen_report', 'value' => 'Graph Performance' );
    my %saveview  = ( 'name' => 'save_view', 'value' => 'Save View' );
    my %cancel    = ( 'name' => 'cancel', 'value' => 'Cancel' );
    my %close     = ( 'name' => 'close', 'value' => 'Close' );
    my %rename    = ( 'name' => 'rename', 'value' => 'Rename' );
    my %delete    = ( 'name' => 'delete', 'value' => 'Delete' );
    my %help      = ( 'name' => 'help', 'value' => 'Help' );
    my $got_form  = 0;
    if ( $query->param('close') ) {
	$got_form = 1;
    }
    elsif ($query->param('save_view')
	|| $query->param('save')
	|| $query->param('saveas')
	|| $query->param('rename')
	|| $query->param('delete')
	|| $query->param('confirm_delete') )
    {
	get_hosts_graphs();
	unless (%view_file) { views( '', '', '' ) }
	my $host_data = '';
	my $save      = 0;
	my $view_data = "<?xml version=\"1.0\" encoding=\"iso-8859-1\" ?>\n<view name=\"$hidden{'view'}\">";
	if ( $hidden{'view'} eq 'config' ) {
	    foreach my $host ( sort keys %host_rrd_select ) {
		$view_data .= "\n <host name=\"$host\">";
		foreach my $data ( @{ $host_rrd_select{$host} } ) {
		    my $host_data_str = "$host\%\%$data";
		    $host_data_str = uri_escape($host_data_str);
		    my %h = ( 'host_data' => $host_data_str );
		    $host_data .= Forms->hidden( \%h );
		    $view_data .= "\n  <data name=\"$data\"></data>";
		}
		$view_data .= "\n </host>";
	    }
	}
	$view_data .= "\n <layout>$hidden{'layout'}</layout>";
	$view_data .= "\n <date_range>" . (defined( $hidden{'date_range'} ) ? $hidden{'date_range'} : '') . "</date_range>";
	$view_data .= "\n <start_date>$hidden{'start_date'}</start_date>";
	$view_data .= "\n <end_date>$hidden{'end_date'}</end_date>";
	$view_data .= "\n <last_x_hours>" . (defined( $hidden{'last_x_hours'} ) ? $hidden{'last_x_hours'} : '') . "</last_x_hours>";
	$view_data .= "\n <hours>$hidden{'hours'}</hours>";
	$view_data .= "\n <last_x_days>" . (defined( $hidden{'last_x_days'} ) ? $hidden{'last_x_days'} : '') . "</last_x_days>";
	$view_data .= "\n <days>$hidden{'days'}</days>";
	$view_data .= "\n <newauto_refresh>" . (defined($newauto_refresh) ? $newauto_refresh : '') . "</newauto_refresh>";
	$view_data .= "\n <newrefresh_rate>" . (defined($newauto_refresh) ? $newrefresh_rate : '') . "</newrefresh_rate>";
	$view_data .= "\n</view>\n";

	if ( $query->param('delete') || $query->param('confirm_delete') ) {
	    ## Carry these values forward in case the user cancels the delete.
	    $hidden{'newauto_refresh'} = $newauto_refresh;
	    $hidden{'newrefresh_rate'} = $newrefresh_rate;

	    if ( $query->param('confirm_delete') ) {
		unlink("$view_dir/$hidden{'file'}") or push @errors, "Error: Unable to remove $view_dir/$hidden{'file'} ($!)";
		$refresh_left = 1;
		views( '', $hidden{'file'}, '1' );
		unless (@errors) {
		    push @message, "Removed: $file_view{$hidden{'file'}}";
		    $body .= Forms->form_top( 'Performance Selection Criteria', '' );
		    if (@errors) {
			$body .= Forms->form_errors( \@errors );
			@errors = ();
		    }
		    $body .= Forms->display_hidden( 'View:', 'name', $file_view{ $hidden{'file'} } );
		    $body .= Forms->form_message( \@message ) if @message;
		    $body .= $host_data;
		    $body .= Forms->hidden( \%hidden );
		    $delete{'name'} = 'confirm_delete';
		    $body .= Forms->form_bottom_buttons( \%close );
		    $got_form = 1;
		}
	    }
	    unless ($got_form) {
		push @message, "Are you sure you want to remove view \"$file_view{$hidden{'file'}}\"?";
		$body .= Forms->form_top( 'Performance Selection Criteria', '' );
		if (@errors) {
		    $body .= Forms->form_errors( \@errors );
		    @errors = ();
		}
		$body .= Forms->form_message( \@message );
		$body .= $host_data;
		$body .= Forms->hidden( \%hidden );
		$delete{'name'} = 'confirm_delete';
		$body .= Forms->form_bottom_buttons( \%delete, \%cancel );
		$got_form = 1;
	    }
	}
	elsif ( $newauto_refresh && not is_valid_refresh_rate($newrefresh_rate) ) {
	    ## There's nothing to do here.  The error will be detected again later on and reported then.
	}
	elsif ( $query->param('save_view') || $query->param('save') || $query->param('saveas') || $query->param('rename') ) {
	    if ( $query->param('save_view') && $query->param('name') )     { $save = 1 }
	    if ( $query->param('rename')    && $query->param('new_name') ) { $save = 1 }
	    if ( $query->param('save') ) { $save = 1 }
	    if (   $query->param('save_view') and defined( $query->param('name') ) and $query->param('name') eq ''
		or $query->param('rename') and defined( $query->param('new_name') ) and $query->param('new_name') eq '' )
	    {
		push @errors, "A view name cannot be empty.";
	    }
	    my $got_name = 0;
	    if ($save) {
		my $tname = undef;
		if ( $query->param('rename') ) {
		    $tname = $query->param('new_name');
		}
		elsif ( $query->param('save_view') ) {
		    $tname = $query->param('name');
		}
		else {
		    $tname = $file_view{ $hidden{'file'} };
		}
		$tname =~ s/^\s*|\s*$//g if defined $tname;
		## We purposely allow &#; characters in a view name, because they can be used to support numeric entity references,
		## which may be found in the query-parameter value.  But we disallow many other characters which might be dangerous.
		if (not defined $tname) {
		    push @errors, "The view name is not defined.";
		}
		elsif ($tname =~ m{[\\\@%'<>`[\]"\(\)\$/]}) {
		    push @errors, "A view name cannot contain any of these characters: \\@%'<>`[]\"()\$/";
		}
		elsif ($tname =~ /\pC/) {
		    push @errors, "A view name cannot contain any control characters.";
		}
		elsif ($tname eq '') {
		    push @errors, "A view name cannot be all blank.";
		}
		else {
		    if ( $query->param('save_view') ) {
			if ( $view_file{$tname} ) {
			    push @errors, "A view with name $tname already exists.";
			}
			else {
			    unless ( $hidden{'file'} ) {
				my $now = time;
				$hidden{'file'} = "view_$now.xml";
			    }
			    views( $tname, $hidden{'file'}, '' );
			    $refresh_left = 1;
			}
		    }
		    if ( $query->param('rename') ) {
			if ( $tname eq $file_view{ $hidden{'file'} } ) {
			    $got_name = 1;
			}
			else {
			    if ( $view_file{$tname} ) {
				push @errors, "A view with name $tname already exists.";
			    }
			    else {
				views( $tname, $hidden{'file'}, '' );
				$got_name     = 1;
				$refresh_left = 1;
			    }
			}
		    }
		}

		unless ( @errors || $query->param('rename') ) {
		    if (
			not open( FILE, '>', "$view_dir/$hidden{'file'}" ) or not do {
			    my $done = 1;
			    print( FILE $view_data ) or $done = 0;
			    close(FILE)              or $done = 0;
			    $done;
			}
		      )
		    {
			push @errors, "Error: Unable to write $view_dir/$hidden{'file'} ($!)";
		    }
		    unless (@errors) {
			$got_name = 1;
			push @message, "View \"$file_view{$hidden{'file'}}\" saved to: $view_dir/$hidden{'file'}";
		    }
		}
	    }
	    unless ($got_name) {
		## Carry these values forward in case the user cancels the rename or name.
		$hidden{'newauto_refresh'} = $newauto_refresh;
		$hidden{'newrefresh_rate'} = $newrefresh_rate;

		$body .= Forms->form_top( 'Performance Selection Criteria', '' );
		if (@errors) {
		    $body .= Forms->form_errors( \@errors );
		    @errors = ();
		}
		if ( $query->param('rename') ) {
		    $body .= Forms->display_hidden( 'View:', 'name', $file_view{ $hidden{'file'} } );
		    $body .= Forms->text_box( 'Name:', 'new_name', '' );
		    %saveview = %rename;
		}
		else {
		    $body .= Forms->text_box( 'Name:', 'name', '' );
		}
		$body .= $host_data;
		$body .= Forms->hidden( \%hidden );
		$help{url} = StorProc->doc_section_url('Performance+View', 'PerformanceView-CreatingaPerformanceView');
		$body .= Forms->form_bottom_buttons( \%saveview, \%cancel, \%help );
		$got_form = 1;
	    }
	}
    }
    else {
	get_hosts_graphs();
    }

    unless ($got_form) {
	unless (%view_file) { views( '', '', '' ) }
	if ( $query->param('config') ) {
	    # We carry transient modified values back from the graphing screen to the configuration screen,
	    # so the user can fiddle with going back and forth between configuration and graphing screens
	    # multiple times before finally saving the desired setup.  The rest of the parameters that
	    # need to be carried back were dealt with above; here we just deal with the refreshing, which
	    # may have been changed in the graphing screen.
	    $newauto_refresh = $auto_refresh;
	    $newrefresh_rate = $refresh_rate;
	    $hidden{'view'}  = 'config';
	}
	elsif ( $query->param('gen_report') ) {
	    $auto_refresh   = $newauto_refresh;
	    $refresh_rate   = $newrefresh_rate;
	    $hidden{'view'} = 'gen_report';
	}
	elsif ( defined( $hidden{'view'} ) && $hidden{'view'} eq 'get_view' ) {
	    read_config();
	    $hidden{'view'} = 'gen_report';
	}
	if ( defined( $hidden{'view'} ) && $hidden{'view'} eq 'config' ) {
	    my $erroneous = 0;
	    if ( $newauto_refresh && ( !defined($newrefresh_rate) || $newrefresh_rate eq '' || $newrefresh_rate =~ /\D/ || $newrefresh_rate == 0 ) ) {
		push @errors, "If the Refresh Rate is checked, the specified rate must be a positive integer.";
		$erroneous = 1;
	    }
	    elsif ($newauto_refresh && $newrefresh_rate > $slowest_refresh_rate) {
		push @errors, "If the Refresh Rate is checked, the specified rate cannot be longer than one week.";
		$erroneous = 1;
	    }
	    elsif ( !$newauto_refresh && defined($newrefresh_rate) && $newrefresh_rate ne '' && ( $newrefresh_rate =~ /\D/ || $newrefresh_rate == 0 ) ) {
		push @errors, "The Refresh Rate must be blank or a positive integer.";
		$erroneous = 1;
	    }
	    my $host = $query->param('host');
	    $host = uri_unescape($host);
	    my $rrd = $query->param('rrd');
	    $rrd = uri_unescape($rrd);
	    $body .= Forms->form_top( 'Performance Selection Criteria', '' );
	    if (@errors)  {
		$body .= Forms->form_errors( \@errors );
		@errors = ();
	    }
	    $body .= Forms->form_message( \@message ) if @message;

	    my @host_rrds = ();
	    my @rrd_hosts = ();
	    if ( defined($host) && $host_rrd{$host} ) { push( @host_rrds, ( @{ $host_rrd{$host} } ) ) }
	    if ( defined($rrd)  && $rrd_host{$rrd}  ) { push( @rrd_hosts, ( @{ $rrd_host{$rrd}  } ) ) }
	    my @hosts = ();
	    foreach my $h ( sort keys %hosts ) { push @hosts, $h }
	    my @rrds = ();
	    foreach my $rrd ( sort keys %rrds ) { push @rrds, $rrd }

	    if ( defined( $hidden{'file'} ) && $file_view{ $hidden{'file'} } ) {
		$body .= Forms->display_hidden( 'View:', 'name', $file_view{ $hidden{'file'} } );
	    }
	    $body .= Forms->multiselect( $hidden{'view'}, \%host_service_rrd, \%host_rrd_select, $host, $rrd, \@rrds, \%hidden );
	    $body .= Forms->consolidate_opts($layout);
	    $body .= Forms->hour_select( $hours, $last_x_hours );
	    $body .= Forms->day_select( $days, $last_x_days );
	    unless ($start_date) { $start_date = today() }
	    unless ($end_date)   { $end_date   = today() }
	    $body .= Forms->date_select( $start_date, $end_date, $date_range );

	    delete $hidden{'layout'};
	    delete $hidden{'date_range'};
	    delete $hidden{'start_date'};
	    delete $hidden{'end_date'};
	    delete $hidden{'last_x_days'};
	    delete $hidden{'last_x_hours'};
	    delete $hidden{'days'};
	    delete $hidden{'hours'};

	    $body .= Forms->refresh_save( $newauto_refresh, $newrefresh_rate, $erroneous );
	    $body .= Forms->hidden( \%hidden );
	    if ( defined( $hidden{'file'} ) && $file_view{ $hidden{'file'} } ) {
		my %rename = ( 'name' => 'rename', 'value' => 'Rename' );
		$saveview{'name'}  = 'save';
		$saveview{'value'} = 'Save';
		$help{url} = StorProc->doc_section_url('Performance+View', 'PerformanceView-CreatingaPerformanceView');
		$body .= Forms->form_bottom_buttons( \%genreport, \%saveview, \%rename, \%delete, \%help );
	    }
	    else {
		$help{url} = StorProc->doc_section_url('Performance+View', 'PerformanceView-AboutPerformanceView');
		$body .= Forms->form_bottom_buttons( \%genreport, \%saveview, \%help );
	    }
	}
	elsif ( defined( $hidden{'view'} ) && $hidden{'view'} eq 'gen_report' ) {
	    my $erroneous = 0;
	    if ($auto_refresh) {
		if ( !defined($refresh_rate) || $refresh_rate eq '' || $refresh_rate =~ /\D/ || $refresh_rate == 0 ) {
		    push @errors, "If Auto refresh is checked, the specified rate must be a positive integer.";
		    $erroneous = 1;
		}
		elsif (defined($refresh_rate) && $refresh_rate > $slowest_refresh_rate) {
		    push @errors, "If Auto refresh is checked, the specified rate cannot be longer than one week.";
		    $erroneous = 1;
		}
		elsif (not @errors) {
		    $refresh_url = "$cgi_bin/perfchart.cgi?auto_refresh=1&refresh_rate=$refresh_rate";
		}
	    }
	    $body .= Forms->form_top( 'Performance View', '' );
	    if (@errors) {
		$body .= Forms->form_errors( \@errors );
		@errors = ();
	    }
	    if ( defined( $hidden{'file'} ) && $file_view{ $hidden{'file'} } ) {
		$body .= Forms->display_hidden( 'View:', '', "$file_view{$hidden{'file'}}" );
	    }
	    $body .= Forms->refresh_select( $auto_refresh, $refresh_rate, $erroneous );
	    $body .= data_host();

	    my @refresh_params = '';
	    foreach my $host ( sort keys %host_rrd_select ) {
		foreach my $data ( @{ $host_rrd_select{$host} } ) {
		    my $host_data_str = "$host\%\%$data";
		    $host_data_str = uri_escape($host_data_str);
		    my %nam_val = ( 'host_data' => $host_data_str );
		    $body .= Forms->hidden( \%nam_val );
		    push @refresh_params, "&host_data=$host_data_str";
		}
	    }
	    if ($refresh_url) {
		foreach my $name ( keys %hidden ) {
		    push @refresh_params, "&$name=" . (defined( $hidden{$name} ) ? $hidden{$name} : '');
		}
		$refresh_url .= join('', @refresh_params);
	    }
	    $body .= Forms->hidden( \%hidden );
	    $help{url} = StorProc->doc_section_url('Performance+View', 'PerformanceView-AboutPerformanceView');
	    $body .= Forms->form_bottom_buttons( \%help );
	}
	elsif ( defined( $hidden{'view'} ) && $hidden{'view'} eq 'latency' ) {
	    my $delays_image  = 'service_last_check_delays.png';
	    my $quanta_image  = 'service_last_check_quanta.png';
	    my $nonzero_image = 'service_nonzero_delays.png';
	    my $longest_image = 'service_longest_delay.png';
	    my $total_image   = 'service_total_delay.png';
	    my $delays_path   = $graphdir . '/' . $delays_image;
	    my $quanta_path   = $graphdir . '/' . $quanta_image;
	    my $nonzero_path  = $graphdir . '/' . $nonzero_image;
	    my $longest_path  = $graphdir . '/' . $longest_image;
	    my $total_path    = $graphdir . '/' . $total_image;
	    push @errors, "$delays_path does not exist."  if ( ! -e $delays_path  );
	    push @errors, "$quanta_path does not exist."  if ( ! -e $quanta_path  );
	    push @errors, "$nonzero_path does not exist." if ( ! -e $nonzero_path );
	    push @errors, "$longest_path does not exist." if ( ! -e $longest_path );
	    push @errors, "$total_path does not exist."   if ( ! -e $total_path   );
	    if (@errors) {
		$body .= Forms->form_errors( \@errors );
		@errors = ();
	    }
	    @errors = ();
	    my @empty = ();
	    $body .= Forms->graph( 'graphs', $delays_image,  \@empty ) if ( -e $delays_path );
	    $body .= Forms->graph( 'graphs', $quanta_image,  \@empty ) if ( -e $quanta_path );
	    $body .= Forms->graph( 'graphs', $nonzero_image, \@empty ) if ( -e $nonzero_path );
	    $body .= Forms->graph( 'graphs', $longest_image, \@empty ) if ( -e $longest_path );
	    $body .= Forms->graph( 'graphs', $total_image,   \@empty ) if ( -e $total_path   );
	}
	else {
	    $body .= Forms->splash();
	}
    }

    print Forms->header( $page_title, $refresh_url, $refresh_rate, $refresh_left );
    # FIX THIS:  A customer has reported a "Wide character in print" error occurring
    # in this next print statement (GWMON-8163), but we don't know what they did to
    # trigger the problem.  I suspect it comes from some data the user typed or copied
    # in, somehow, that is not 7-bit ascii.  We ought to figure out how that could
    # happen, replicate the situation, and deal with it.  In the meantime, perhaps
    # try one of these, to at least keep running in limp-along mode:
    # $body =~ s/[^[:ascii:]]+//g;  # get rid of non-ASCII characters
    # $body =~ s/[^[:ascii:]]+/{???}/g;  # make non-ASCII characters clearly visible
    print $body;
    print Forms->footer();
}
else {
    my $url = '';
    foreach my $name ( $query->$multi_param ) {
	my $value = $query->param($name);
	$url .= "&$name=$value";
    }
    print Forms->frame('dummy_user_acct', 'dummy_top_menu', $url);
}

if ($show_params) {
    my @params = ();
    push @params, '<br>Query parameters:<pre>';
    foreach my $name ( sort $query->$multi_param ) {
	my @values = $query->$multi_param($name);
	push @params, HTML::Entities::encode("$name = '" . join("', '", @values) . "'"), '<br>';
    }
    push @params, '</pre>';
    print join('', @params);
}

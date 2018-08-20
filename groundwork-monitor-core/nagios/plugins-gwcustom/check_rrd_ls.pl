#!/usr/local/groundwork/perl/bin/perl -w
# check_rrd_hw.pl
# Based on check_rrd.pl by Harper Mann (hmann@comcast.net)
# Checks RRDs for linear trends with Nagios
# Uses the least squares algorythm for determining linear trend in data.
#
# Requirements:
# An rrd must exist to check for trends in its data. This is the reference rrd.
# You will specify the reference rrd and data source name on the command line.
# You should know the reference rrd step time to accurately specifiy the reference interval.
# The reference interval is simply the time (in minutes) to use in calulating the trend.
# It must be at least 1x the step value of the reference rrd, and cannot exceed nx the step value,
# where n is the number of steps since data was initially collected.
# If you don't know or care, a small amount of accuracy and efficiency will be sacrificed.
#
# Missing Data:
# Missing data in a trend calculation is handled by extrapolating existing data from all
# measurements up to the point where data is missing. If no data is present
# from the beginning of a reference interval, the interval will be shortened to provide
# a more local trend using data that does exist.
#
# Note:
# As this plugin does a complete trend analysis each time it is run, care should be taken not to
# specify too long a reference interval, or high cpu loads may result from
# multiple services using this check, or checking trends too often.
#
# Example:
# check_rrd_hw -r my_cpu.rrd -R 5min -w 80 -W 120 -c 95 -C 240 -i 60 -p
#
# will check the my_cpu.rrd file for the ds called 5min, and will go to warning if the predicted vaule
# is over 80 within 2 hours, and to critical if the predicted value is over 95 within 4 hours.
# It considers the last 60 minutes in making the prediction. Positive values are the only ones that make sense
# in this context (cpu %s), so the plugin will restrict its predictions to positive real numbers.
#
# usage:
#    check_rrd_hw.pl -r reference_rrdfile -rd ds -w warning -tw trendwarning  -c critical -tc trendcritical  -i interval -p -D
# -r, --reference_rrd
#   The RRD which you want to check for trends
# -R, --reference_data_store
#   Data store in reference rrd to check
# -w, --warning
#  Actual warning threshold <low:high>
# -W, --trend_warning
#   Time threshold for predicted warnings (in minutes). Set this to the amount of time within which a predicted warning will result in a warning state.
# -c, --critical
#    Actual critical threshold <low:high>
# -C, --trend_critical
#   Time threshold for predicted critical errors (in minutes). Set this to the amount of time within which a predicted critical error will result in a critical state.
# -p, --positive
#   Set if you want to restrict data range considered to positive real values.
# -G --graph
#	Set this option if you want an RRD created that contains the data over the iterval considered and a trend line extending out to threshold values in the future
# -D, --debug
#   Turn on debugging.  (Verbose)
# -h, --help
#   Print help
# -V, --Version
#   Print version of plugin
#
#	 Note: critical levels mst be outside warning levels and trendcritical times must be longer
#	 than trendwarning times. Trendcitical conditions supercede trendwarning conditions.
#
# To Do:
# Add -n for negative real restrictions.
# Add facility for writing an rrd and producing a graph of actual and trend predicted values.
#
# Copyright (c) 2000-2006 Thomas Stocking (tstocking@groundworkopensource.com)
# Last Modified 3/15/2006 by Thomas Stocking
# Added error traps for when rrd file not readable wit RRDs Fetch
#
# This plugin is FREE SOFTWARE. No warrenty of any kind is implied or granted.
# You may use this software under the terms of the GNU General Public License only.
# See http://www.gnu.org/copyleft/gpl.html and usage() below
#
#
#
use lib qw( ../perl-shared/blib/lib ../perl-shared/blib/arch );
use lib qw( ../lib/perl /usr/local/groundwork/nagios/libexec );
use RRDs;
use strict;
require 'utils.pm';

# Set up environment

$ENV{'PATH'} = "/usr/local/groundwork/common/bin:/bin:/usr/bin";
$ENV{'ENV'} = "";

# Initialize Variables
my @vals = undef;
my @skips = undef;
my @lines = undef;

my $warn = -1;
my $lo_warn = undef;
my $hi_warn = undef;
my $twarn = undef;
my $crit = -1;
my $lo_crit = undef;
my $hi_crit = undef;
my $tcrit = undef;
my $in_interval = 60;
my $interval = 0;
my $starttime = 0;
my $lasttime = 0;
my $dev = undef;
my $debug = 0;
my $graphopt = 0;
my $positive = 0;
my $res = undef;
my $state = "OK";

# Intermediate values
my $alpha;
my $alpha0;
my $alpha1;
my $beta;
my $beta0;
my $beta1;

# Read the command line options

use Getopt::Long;
use vars qw($opt_r $opt_R $opt_w $opt_W $opt_c $opt_C $opt_i $opt_G $opt_D $opt_V $opt_h $opt_p);
use vars qw($PROGNAME);
#use lib "$utilsdir";
use utils qw($TIMEOUT %ERRORS &print_revision &support &usage);

sub print_help ();
sub print_usage ();

$PROGNAME = "check_rrd_ls";

Getopt::Long::Configure('bundling');
my $status = GetOptions
        ("V"   => \$opt_V, "Version"         => \$opt_V,
         "r=s" => \$opt_r, "reference_rrd=s"  => \$opt_r,
         "R=s" => \$opt_R, "reference_data_store=s"  => \$opt_R,
         "w=s" => \$opt_w, "warning=s"  => \$opt_w,
         "W=s" => \$opt_W, "trend_warning=s"  => \$opt_W,
         "c=s" => \$opt_c, "critical=s"  => \$opt_c,
         "C=s" => \$opt_C, "trend_crittcal=s"  => \$opt_C,
	 	 "i=s" => \$opt_i, "interval=s"  => \$opt_i,
 		 "p"  => \$opt_p, "positive"  => \$opt_p,
         "D"   => \$opt_D, "debug"            => \$opt_D,
         "G"   => \$opt_G, "graph"            => \$opt_G,
         "h"   => \$opt_h, "help"            => \$opt_h);

if ($status == 0)
{
        print_usage() ;
        exit $ERRORS{'OK'};
}

# Debug switch
if ($opt_D) {
	$debug = 1;
}

# Graphing toggle
if ($opt_G) {
	$graphopt = 1;
}

# Positive constraint switch
if ($opt_p) {
	$positive = 1;
}
if ($opt_V) {
        print_revision($PROGNAME,'$Revision: 1.1 $'); #'
        exit $ERRORS{'OK'};
}

if ($opt_h) {print_help(); exit $ERRORS{'OK'};}

# Options checking
# Warning
if ($opt_w) {
	if ($opt_w =~ /:/){
		@vals = split /:/, $opt_w;
		($vals[0]) || usage("Invalid value: low warning: $opt_w\n");
		($vals[1]) || usage("Invalid value: high warning: $opt_w\n");
		$lo_warn = $vals[0] if ($vals[0] =~ /^[0-9]+$/);
		$hi_warn = $vals[1] if ($vals[1] =~ /^[0-9]+$/);
		($lo_warn) || usage("Invalid value: low warning: $opt_w\n");
		($hi_warn) || usage("Invalid value: high warning: $opt_w\n");
	} else {
		$lo_warn = undef;
		$hi_warn = $opt_w if ($opt_w =~ /^[0-9]+$/);
        ($hi_warn) || usage("Invalid value: warning: $opt_w\n");
	}
} else { print "No warning level defined\n" if $debug }

# Critical
if ($opt_c) {
    if ($opt_c =~ /:/){
        @vals = split /:/, $opt_c;
        ($vals[0]) || usage("Invalid value: low critical: $opt_c\n");
        ($vals[1]) || usage("Invalid value: high critical: $opt_c\n");
        $lo_crit = $vals[0] if (($vals[0] =~ /^[0-9]+$/) && ($vals[0] < $lo_warn));
        $hi_crit = $vals[1] if (($vals[1] =~ /^[0-9]+$/) && ($vals[1] > $hi_warn));
        ($lo_crit) || usage("Invalid value: low critical: $opt_c\n");
        ($hi_crit) || usage("Invalid value: high critical: $opt_c\n");
    } else {
        $lo_crit = undef;
        $hi_crit = $opt_c if (($opt_c =~ /^[0-9]+$/)&& ($opt_c > $hi_warn));
        ($hi_crit) || usage("Invalid value: critical: $opt_c\n");
    }
} else { print "No critical level defined\n" if $debug }


# Trend Warning
if ($opt_W) {
        $twarn = $opt_W if ($opt_W =~ /^[0-9]+$/);
        ($twarn) || usage("Invalid value: trend warning: $opt_W\n");
} else { print "No trend warning level defined\n" if $debug }


# Trend Critical
if ($opt_C) {
        $tcrit = $opt_C if (($opt_C =~ /^[0-9]+$/) && ($opt_C > $twarn));
        ($tcrit) || usage("Invalid value: trend critical: $opt_C\n");
} else { print "No trend critical level defined\n" if $debug }


# Default
# Get parameters into variables
if (!$opt_r || !$opt_R) {
	print "Required arguments missing. Please see help.\n";
	exit 3;
}
my $ref_rrdfile = $opt_r if ($opt_r =~ /rrd/);
my $ref_ds = $opt_R;
if ($opt_i) {
 	$in_interval = $opt_i;
}


# (Re)create the graphing RRD if we are doing that...
my $rrddir;
my $dir;
my $graph_rrdfile;


if ($graphopt) {
	my @rrddirs = split(/\//,$ref_rrdfile);
	foreach $dir  (@rrddirs) {
		if ($dir =~ /\.rrd/) {
			#$dir =~ s/\.rrd/_graph.rrd/;
			$dir = $dir."_graph.rrd";
			$graph_rrdfile = $rrddir."\/".$dir;
			$graph_rrdfile =~ s/\/\//\//;
		} else {
			$rrddir .= "\/".$dir;
		}
	}
	print "Graphing RRD: $graph_rrdfile\n" if $debug;
	if (-e $graph_rrdfile) {
		print "deleting $graph_rrdfile\n" if $debug;
		unlink $graph_rrdfile or die "Error deleting last trend rrd $graph_rrdfile\n"; # this gets created each time the plugin is run. We delete it here.

	}
}

# convert to seconds
$interval=$in_interval*60;

# Find out last update time
my ($last) = RRDs::last ($ref_rrdfile);
my $ERR=RRDs::error;
        die "ERROR while reading $ref_rrdfile: $ERR\n" if $ERR;
print "last update to RRD $ref_rrdfile: ","$last\n" if $debug;
print "Interval in seconds: ","$interval\n" if $debug;


# Fetch the reference data
# Fetch all the data over the interval so we can work with arrays
$starttime=$last-$interval;
print "Start of trend interval in seconds: ","$starttime\n" if $debug;
my ($start,$step,$names,$data) = RRDs::fetch ($ref_rrdfile,"AVERAGE","-s",$starttime,"-e",$last);
$ERR=RRDs::error;
        die "ERROR while reading $ref_rrdfile: $ERR\n" if $ERR;
my $i;
my $found_index = undef;
for ($i= 0; $i < @$names; $i++) {
print "Found DS: ","@$names[$i]\n" if $debug;
	if (@$names[$i] eq $ref_ds) {
    	$found_index = $i;
        print "Using DS ","@$names[$i]\n" if $debug;
		last;
    }
}
if (!(defined $found_index)) {
	print "UNKNOWN - DS $ref_ds not found in this RRD\n";
	exit 3
}
print "DS Index number: ","$found_index\n" if $debug;

#saving fetched values in @values array over times stored in @times
my $rows = 0;
my $column = 0;
my $missing = 0;
my $pctundef =0;
my @values;
my @times;
my $time_variable = $start;
foreach my $line (@$data) {
# got to the end, but fetch still has some data so stop here
	if ($time_variable gt $last) {
		last;
	}
	$time_variable = $time_variable + $step;
	foreach my $val (@$line) {
    	if ($column eq $found_index) {
			$values[$rows] = $val;
			$times[$rows] = $time_variable;
			if ($debug) {
				print "  ", scalar localtime($time_variable), " ($time_variable) ";
				print "$column ";
				if (defined $val) {
         			printf "%12.8f ",$val;
				}
				print "\n";
			}
			if (!(defined $val)) {
				$missing++;
			}
		}
		$column++;
	}
   	$rows++;
   	$column = 0;
}
# Find percentage of missing data
$pctundef = 100*$missing/$rows;
printf "%12.2f",$pctundef if $debug;
print "percent undefined or missing data over the interval\n" if $debug;
print "#data points = $rows\n" if $debug;

# Bail if there is no data to analyse...
if ($pctundef ==100) {
    print "UNKNOWN: No data in DS $ref_ds in RRD $ref_rrdfile for the last $in_interval minutes\n";
    exit 3;
}


# Create the graphing RRD if we are doing that...
# RRDs::create ( "$graph_rrdfile", "");


# Loop over the interval to find earliest 2 valid points and assign to reference values
my $row = 0;
my $first = 0;
my $m = 0;
my $sx = 0;
my $sy = 0;
my $sx2 = 0;
my $sxy = 0;
my $value;
while ($row < $rows) {
	print "row = $row\n" if $debug;
	print "first = $first\n" if $debug;
	$value = $values[$row];
	if ($debug) {
    		print "value = $value\n" if (defined $value);
	}
    	if (($row == $first) && (!(defined $value))) { #skip missing data at the start
		$first++;
	} elsif (!$value) { # skip missing data
		$row++;
		next;
	} else { # calculate sums for least squares
		$m++;
		$sx = $sx + $row;
		$sy = $sy + $value;
		$sx2 = $sx2 + $row*$row;
		$sxy = $sxy + $row*$value;
		$row++;
		next;
	}
}
print "final m = $m\n" if $debug;
print "final sx = $sx\n" if $debug;
print "final sx2 = $sx2\n" if $debug;
print "final sy = $sy\n" if $debug;
print "final sxy = $sxy\n" if $debug;

# Find the least squares co-efficients
# constant is alpha:
my $divis = $m*$sx2 - ($sx*$sx);
if ($divis == 0) {
	$alpha = 0;
	$beta = 0;
} else {
	$alpha = ($sx2*$sy - $sx*$sxy)/$divis;
	#slope is beta
	$beta = ($m*$sxy - $sx*$sy)/$divis;
}

# Now fill the graphing rrd if we are doing that...
if ($graphopt) {
# Get the info on this rrd
	if ($debug) {
	print "Data on Reference RRD:\n";
   	my ($rrdinfo) = RRDs::info( $ref_rrdfile );
    	foreach my $prop ( keys %$rrdinfo ) {
       		if (defined $$rrdinfo{$prop}) {
        		print "$prop = $$rrdinfo{$prop}\n";
        	} else {
				print "$prop\n";
    		}
		}
	}

# Create the graphing rrd
#	print "$graph_rrdfile, $starttime, $step, $ref_ds,$rows\n";
    RRDs::create ($graph_rrdfile, "--start", $starttime, "--step", $step, "DS:$ref_ds:GAUGE:$step:U:U", "DS:trend:GAUGE:$step:U:U", "RRA:AVERAGE:0.5:1:$rows");
	my $ERR=RRDs::error;
    die "ERROR while creating $graph_rrdfile: $ERR\n" if $ERR;
	$row = 0;
	$first = 0;
	$value = undef;
	my $linear_value;
	while ($row < $rows) {
		$value = $values[$row];
  		if (($row == $first) && (!(defined$value))) { #skip missing data at start
			$first++;
		} else {
			$linear_value = ($beta*($row)) + $alpha; # y = mx + b
			RRDs::update ( "$graph_rrdfile", "$times[$row]:$value:$linear_value");
			print "value = $value, trend = $linear_value\n" if $debug;
		}
		$row++;
	}
}


# Calculate intercept times with warning and critical thresholds
# First see if we are already there
my $worst =0;

if ($lo_crit && $value < $lo_crit) {
	$worst = $ERRORS{'CRITICAL'};
	$state = "CRITICAL";
}

if ($hi_crit && $value > $hi_crit) {
    $worst = $ERRORS{'CRITICAL'};
    $state = "CRITICAL";
}
if ($lo_warn && $value < $lo_warn) {
    $worst = $ERRORS{'WARNING'} if $worst < $ERRORS{'CRITICAL'};
    $state = "WARNING" if $worst < $ERRORS{'CRITICAL'};
}

if ($hi_warn && $value > $hi_warn) {
    $worst = $ERRORS{'WARNING'} if $worst < $ERRORS{'CRITICAL'};
    $state = "WARNING" if $worst < $ERRORS{'CRITICAL'};
}
#print "Thresholds:\n";
#print "lo_crit = $lo_crit\n" if $lo_crit;
#print "hi_crit = $hi_crit\n";
#print "lo_warn = $lo_warn\n" if $lo_warn;
#print "hi_warn = $hi_warn\n";

# Get out without doing any trending, since we are already over the top
if ($worst != $ERRORS{'OK'}) {
	print "$state: Parameter $ref_ds is already over the $state threshold at";
    printf "%12.2f", $value;
    print " | $ref_ds=$value;\n";
	exit ($worst);
}

# Now handle case where data is steady over the interval (final $beta = 0)
if ($beta == 0) {
    print "$state: Parameter $ref_ds is unchanging at";
    printf "%12.2f", $value;
    print "| $ref_ds=$value;\n";
    exit ($worst);
}

# Otherwise, let's do some trending...


# Find the intercepts
# Are we headed up or down?
my $Crit;
my $Warn;
my $crit_int;
my $warn_int;
my $time_string;
if ($beta >= 0) {
	$Crit = $hi_crit;
	$Warn = $hi_warn;
} else {
	if (!$positive) {
		$Crit = $lo_crit;
		$Warn = $lo_warn;
	} else {
		print "OK: Downward trend. No indication you will exceed the thresholds.| $ref_ds=$value;\n";
		exit (0);
	}
}
$crit_int = $last+(($Crit-$alpha)/$beta)*$step;
$warn_int = $last+(($Warn-$alpha)/$beta)*$step;

# Now test for Trend Critial and warning
if ($crit_int <= ($last+($tcrit*60))) {
    $worst = $ERRORS{'CRITICAL'};
    $state = "CRITICAL";
}
if ($warn_int <= ($last+($twarn*60))) {
    $worst = $ERRORS{'WARNING'} if $worst < $ERRORS{'CRITICAL'};
    $state = "WARNING" if $worst < $ERRORS{'CRITICAL'};
}

# Exit with the right code
$time_variable = scalar localtime ($crit_int);
my $mins_left = ($crit_int-$last)/60;
print "$state: I predict you will exceed the critical value at $time_variable, in ";
printf "%.0f", $mins_left;
print " minutes,";
$time_variable = scalar localtime ($warn_int);
$mins_left = ($warn_int-$last)/60;
print " and the warning value at $time_variable, in ";
printf "%.0f", $mins_left;
print " minutes| $ref_ds=$value;\n";
exit ($worst);




# Usage sub
sub print_usage () {
        print "Usage: $PROGNAME
	[-r ] (Reference RRD name and path)
	[-R ] (Reference data store name)
	[-w <low:high>] (actual warning threshold)
	[-W ] (Trend Warning - minutes)
	[-c <low:high>] (actual critical threshold - must be outside -w range)
	[-C]  (Trend Critical - minutes. Must be higher than -W)
	[-p] (constrain to positive real values)
	[-i] (Trend interval to consider - minutes)
	[-G] (Graphing RRD toggle)
	[-D] (debug) [-h] (help) [-V] (Version)\n";
}


# Help sub
sub print_help () {
        print_revision($PROGNAME,'$Revision: 1.0 $');
        print "Copyright (c) 2004 Thomas Stocking

Perl RRD Trend check plugin for Nagios

";
        print_usage();
        print "
-r, --reference_rrd
   The RRD which you want to check for trends (required)
-R, --reference_data_store
   Data store in reference rrd to check (required)
-i, --interval
   Time in minutes to consider for trending (default 60)
-w, --warning
   Actual warning threshold <low:high>
-W, --trend_warning
   Time threshold for predicted warnings (in minutes). Set this to the amount of time within which a predicted warning will result in a warning state.
-c, --critical
	Actual critical threshold <low:high>
-C, --trend_critical
   Time threshold for predicted critical errors (in minutes). Set this to the amount of time within which a predicted critical error will result in a critical state.
-p, --positive
   Set if you want to restrict data range considered to positive real values.
-D, --debug
   Turn on debugging.  (Verbose)
-h, --help
   Print help
-V, --Version
   Print version of plugin
";
}



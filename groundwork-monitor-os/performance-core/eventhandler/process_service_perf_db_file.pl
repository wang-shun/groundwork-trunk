#!/usr/local/groundwork/perl/bin/perl -w --
#
# ========================================================================
#
#   NOTICE:  THIS VERSION OF THE PERFORMANCE DATA HANDLING SCRIPT IS NOW
#   DEPRECATED.  IT IS INCLUDED IN THE DISTRIBUTION FOR LEGACY SUPPORT
#   ONLY, PRIMARILY FOR BRIEF USE BEFORE UPGRADES ARE COMPLETE.  IN THE
#   GW 6.0 RELEASE, GROUNDWORK RECOMMENDS CONVERTING TO USE OF THE NEW
#   process_service_perfdata_file SCRIPT INSTEAD.  THERE ARE SEVERAL
#   ADJUSTMENTS NEEDED TO PERFORM THAT CONVERSION; CONTACT GROUNDWORK
#   SUPPORT FOR ASSISTANCE IF NEEDED.
#
# ========================================================================
#
# process_service_perfdata_db.pl
#
# Process Service Performance Data
#
# Copyright 2007, 2008, 2009 GroundWork Open Source, Inc. (GroundWork)
# All rights reserved. This program is free software; you can redistribute
# it and/or modify it under the terms of the GNU General Public License
# version 2 as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
#
#
# Revision History
# 16-Aug-2005 Peter Loh
#	Original version.
#
# 23-Dec-2005 Peter Loh
#	* Added capability to use Label macros in RRD create.
#	* Also allowed more flexible perfdata. OK if warn,crit,max,min are missing.
#
# 22-Jan-2006 Peter Loh
#	* Modified to use database for host,service,rrdfile. Required for Status Viewer integration.
#
# 06-Mar-2007 Peter Loh
#	* Added ability to extract a list of labels from perf data and use in DS definition for rrd creation.
#
# 13-Nov-2007 Roger Ruttimann
#	* Added post to Foundation for performance data.
#
# 24-Apr-2008 Anonymous
#	* Patched to strip out redundant and unhandled whitespace in definitions.
#
# 19-Jun-2008 Thomas Stocking
#	* Added substitution of illegal chars for RRD DS names, limited DS name length to 19 chars.
#	* Changed foundation post to include service description in performance label.
#
# 08-Jan-2009 Thomas Stocking
#	* Added code to post the perf data scraped out of status text to foundation.
#	* Added LWP timeout in case foundation fails to accept post.
#
# 03-Feb-2009 Mark Carey
#	* Applied patch from Jurgen Lesney that corrects a greedy regex match
#	  when more than one perfdata value is returned
#	* Fixed perl shebang.
#
# 31-Mar-2009 Glenn Herteg
#	* Script overhauled to consolidate database accesses, thereby significantly
#	  improving the system loading and performance.
#	* Implemented creating and updating of RRD files through a shared libary
#	  instead of forking off a separate process for every such action, to dramatically
#	  cut down the overhead of such external data storage.
#	* Improved error handling, particularly the logging of database errors and of
#	  RRD command errors.
#	* Improved failure detection in the submission of results to Foundation.
#	* Removed `date` invocations from within the main loop, to stop excessive forking.
#	* The foundation_submission_timeout is now a named value, suitable for on-site
#	  configuration if need be; in a future version of this script with a separate
#	  config file, this would be an obvious candidate option for placing in the
#	  external config file.
#	* Fixed the matching of service regex's to guarantee that the expression associated
#	  with a specific host is matched instead of a wildcarded host, if both matches
#	  are present.  In particular, we now adhere to a strict interpretation of the
#	  monarch.performanceconfig.service_regx field, so when it is set to 1, the
#	  monarch.performanceconfig.service field is treated ONLY as a pattern, and
#	  never initially as a literal-match string.  This logic now matches the
#	  documentation, and fixes some obscure situations in which a wildcarded-host
#	  service pattern might be inappropriately matched as a literal string before
#	  any service pattern matches for the specific host are even attempted.
#	* Improved parsing of labels in performance data.
#	* Process multiple $LISTSTART$...$LISTEND$ templates in the same source string
#	  independently, as seems to make sense, rather than forcing the first template
#	  found within these markers to be used for all subsequent instances.
#	* Added an optional --version argument.
#	* Implemented the usage message.
#	* Fixed the signal handling so the script will properly exit upon receiving a
#	  SIGTERM from nagios.
#	* perltidy'd the entire script.

# BE SURE TO KEEP THIS UP-TO-DATE!
my $VERSION = '4.3 (March 31, 2009)';

use strict;
use Config;
use Time::Local;
use Time::HiRes;
use DBI;
use URI;
use LWP;
use CollageQuery;
use RRDs;

my $start_time = Time::HiRes::time();

####################################################################
# Configuration Parameters
####################################################################

# Possible $debug values:
# 0 = no info of any kind printed
# 1 = print just error info and summary statistical data
# 2 = also print basic debug info
# 3 = print detailed debug info
my $debug                         = 1;
my $process_rrd_updates           = 1;    # Create and update RRD files (0 = no, 1 = yes)
my $process_foundation_db_updates = 1;    # Post performance data to the Foundation database (0 = no, 1 = yes)
my $foundation_submission_timeout = 2;    # specified in seconds

# Specify whether to use a shared library to implement RRD file access,
# or to fork an external process for such work (the legacy implementation).
# Set to 1 (recommended) for high performance, to 0 only as an emergency fallback.
my $use_shared_rrd_module = 1;

my $rrdtool               = '/usr/local/groundwork/common/bin/rrdtool';
my $service_perfdata_file = '/usr/local/groundwork/nagios/eventhandlers/service_perfdata.log';
my $debuglog              = '/usr/local/groundwork/nagios/eventhandlers/process_service_perf.log';

####################################################################
# Main-Line Code
####################################################################

my $debug_minimal = ( $debug >= 1 );
my $debug_basic   = ( $debug >= 2 );
my $debug_maximal = ( $debug >= 3 );

my $rrdcreate_count       = 0;
my $rrdcreate_failures    = 0;
my $rrdupdate_count       = 0;
my $rrdupdate_failures    = 0;
my $services_count        = 0;
my $termination_requested = 0;
my %ERRORS                = ( 'UNKNOWN', '-1', 'OK', '0', 'WARNING', '1', 'CRITICAL', '2' );

if ( scalar(@ARGV) == 1 && $ARGV[0] eq '--version' ) {
    print "Version:  $VERSION\n";
    exit 0;
}

if ( scalar(@ARGV) ) {
    print_usage();
    exit 1;
}

my ( $Database_Name, $Database_Host, $Database_User, $Database_Password ) = CollageQuery::readGroundworkDBConfig("monarch");
my $dbh   = undef;
my $sth   = undef;
my $query = undef;

if ($debug) {
    open( FP, '>>', $debuglog ) or die "Cannot open the debug file $debuglog; aborting!\n";
    print FP "=====================================================================\n";
    my $timestamp = scalar localtime();
    print FP "Execution cycle starting at: $timestamp\n";
}

# If this script runs too long, Nagios will send us a SIGTERM followed by SIGKILL a second later.
# Of course, because of stochastic variations in OS process scheduling, we may not see that full
# second here, but we can at least try to log the termination signal and clean up gracefully.
# We also catch SIGINT and SIGQUIT so we can interactively test the signal handling.
$SIG{INT}  = \&handle_exit_signal;
$SIG{QUIT} = \&handle_exit_signal;
$SIG{TERM} = \&handle_exit_signal;

$dbh = DBI->connect( "DBI:mysql:$Database_Name:$Database_Host", $Database_User, $Database_Password )
  or log_and_die( "Can't connect to database $Database_Name. Error: " . $DBI::errstr );

# Prepare for processing individual metrics, by coalescing almost all the
# database activity into one large, efficient query at the start of execution.
my %service_config      = ();
my %service_regx_config = ();

# Subsidiary order by service (within common host) establishes a canonical
# sorting of service regex patterns, to resolve which one to use (the first
# in this sequence, for a given host) if several match a given service.
$query = "SELECT * FROM `performanceconfig` where (type='nagios' and enable=1) ORDER BY host, service";
$sth   = $dbh->prepare($query);
$sth->execute() or log_and_die( $sth->errstr );
while ( my $row = $sth->fetchrow_hashref() ) {
    my $host    = $$row{host};
    my $service = $$row{service};
    if ( defined($service) && $service ) {
	my $service_hashref = {};
	if ( defined( $$row{service_regx} ) && $$row{service_regx} == 1 ) {
	    ## Here we need an array rather than a hash at the service level, to
	    ## preserve the sequencing of the regex's as read from the database.
	    push @{ $service_regx_config{$host} }, $service_hashref;
	    $service_hashref->{service}     = $$row{service};
	    $service_hashref->{serviceregx} = qr/$$row{service}/;
	}
	else {
	    $service_config{$service}{$host} = $service_hashref;
	}
	$service_hashref->{rrdname}         = $$row{rrdname};
	$service_hashref->{rrdcreatestring} = $$row{rrdcreatestring};
	$service_hashref->{rrdupdatestring} = $$row{rrdupdatestring};
	$service_hashref->{perfidstring}    = $$row{perfidstring};
	$service_hashref->{configlabel}     = $$row{label};
	if ( defined( $$row{parseregx} ) && $$row{parseregx} ) {
	    $service_hashref->{parseregx}       = qr/$$row{parseregx}/;
	    $service_hashref->{parseregx_first} = $$row{parseregx_first};
	}
    }
}
$sth->finish();

# Prepare for later checking whether the host_service table is properly populated.
my %host_service_id = ();
$query = "SELECT hs.host_service_id, hs.host, hs.service, dt.location FROM host_service as hs, datatype as dt "
  . "WHERE dt.type='RRD' AND hs.datatype_id=dt.datatype_id";
$sth = $dbh->prepare($query);
$sth->execute() or log_and_die( $sth->errstr );
while ( my $row = $sth->fetchrow_hashref() ) {
    $host_service_id{ $$row{host} }{ $$row{service} }{ $$row{location} } = $$row{host_service_id};
}
$sth->finish();

open( DATA, '<', $service_perfdata_file ) or log_and_die("Can't open service performance data file $service_perfdata_file.\n");
while ( my $line = <DATA> ) {

    my ( $lastcheck, $host, $svcdesc, $statustext, $perfdata ) = ();
    my ( $rrdname, $rrdcreatestring, $rrdupdatestring, $perfidstring, $parseregx, $parseregx_first, $configlabel ) = ();

    chomp $line;
    my @fieldvalues = split /\t/, $line;
    if ( $fieldvalues[0] !~ /^\d+$/ ) {    # First field is timestamp and must be all digits.
	if ($debug_minimal) {
	    print FP "---------------------------------------------------------------------\n";
	    print FP "Skipping invalid line: $line\n";
	}
	next;
    }
    $lastcheck  = $fieldvalues[0];
    $host       = $fieldvalues[1];
    $svcdesc    = $fieldvalues[2];
    $statustext = $fieldvalues[3];
    $perfdata   = $fieldvalues[4] || '';
    $services_count++;
    if ($debug_maximal) {
	print FP "---------------------------------------------------------------------\n";
	my $timestamp = scalar localtime();
	print FP $timestamp . "\n Host: $host\n Svcdesc: $svcdesc\n Lastcheck: $lastcheck\n Statustext: $statustext\n Perfdata:$perfdata\n";
    }

    # Here we try to avoid auto-vivification, which might confuse the calculation.
    my $service_hashref = undef;
    if ( exists $service_config{$svcdesc} and exists $service_config{$svcdesc}{$host} ) {
	$service_hashref = $service_config{$svcdesc}{$host};
    }
    elsif ( exists $service_config{$svcdesc} and exists $service_config{$svcdesc}{'*'} ) {
	$service_hashref = $service_config{$svcdesc}{'*'};
    }
    else {
	print FP "No exact service name '$svcdesc'. Looking for service pattern matches.\n" if ($debug_basic);
	$service_hashref = find_service_hashref( $svcdesc, $host ) || find_service_hashref( $svcdesc, '*' );
    }
    if ( defined $service_hashref ) {
	$rrdname         = $service_hashref->{rrdname};
	$rrdcreatestring = $service_hashref->{rrdcreatestring};
	$rrdupdatestring = $service_hashref->{rrdupdatestring};
	$perfidstring    = $service_hashref->{perfidstring};
	$configlabel     = $service_hashref->{configlabel};
	if ( defined( $service_hashref->{parseregx} ) && $service_hashref->{parseregx} ) {
	    $parseregx       = $service_hashref->{parseregx};
	    $parseregx_first = $service_hashref->{parseregx_first};
	}
    }
    else {
	print FP "No literal or pattern match for host '$host', service '$svcdesc' in performanceconfig table.\n" if ($debug_basic);
	next;
    }

    if ( $process_rrd_updates and ( !defined($rrdupdatestring) or !$rrdupdatestring ) ) {
	print FP "No rrdupdatestring for host '$host', service '$svcdesc' matched row in performanceconfig table.\n" if ($debug_basic);
	## Not only do we not update an RRD file in this case, we also don't forward this performance data to Foundation.
	next;
    }

    if ( !defined($perfdata) and ( !defined($parseregx) or $parseregx eq qr// ) ) {
	if ($debug_basic) {
	    print FP "No performance data or status text regular expression defined in performanceconfig table match "
	      . "for host '$host', service '$svcdesc'.\n";
	}
	next;
    }

    my %macros = (
	'\$RRDTOOL\$'     => $rrdtool,
	'\$RRDNAME\$'     => $rrdname,
	'\$LASTCHECK\$'   => $lastcheck,
	'\$HOST\$'        => $host,
	'\$SERVICETEXT\$' => $statustext,
	'\$SERVICE\$'     => $svcdesc
    );

    # Expected perfdata formats:
    #	  label=value[UnitOfMeasurement];[warn];[crit];[min];[max]
    #	'label'=value[UnitOfMeasurement];[warn];[crit];[min];[max]
    # where label can contain space and "=" characters iff it is quoted,
    # and there may be multiple metric strings like the ones shown above,
    # separated by whitespace.  Note that the Nagios plug-in development guidelines
    # (http://nagiosplug.sourceforge.net/developer-guidelines.html#AEN203) say that
    # a label may contain any characters, including a quote; an internal quote must
    # be doubled to escape it (distinguish it from the trailing quote).
    # warn, crit, min or max may be null (for example, if the threshold is not defined
    # or min and max do not apply). Trailing unfilled semicolons can be dropped.

    my @foundation_labels = ();
    my @rrd_labels        = ();
    my @values            = ();
    my @warns             = ();
    my @crits             = ();
    my @mins              = ();
    my @maxs              = ();

    if ( defined($perfdata) and ( !defined($parseregx_first) or !$parseregx_first ) ) {

	# trim spaces
	$perfdata =~ s/^\s*//;
	$perfdata =~ s/\s$//;

	# Note:  An unaltered Nagios 3.0.6 implementation retains a single-quote character in the
	# configured set of illegal macro output characters, which now (due to a bug fix within Nagios)
	# for the first time causes Nagios to strip single quotes from performance data just before
	# the performance data is written to the performance data file.  Unfortunately, that situation
	# breaks the longstanding conventions for how metric labels containing spaces, quotes, and equal
	# signs are to be presented, namely that such labels must be enclosed in single quotes and
	# embedded single quotes must be doubled.  Trying to patch that situation here would make it
	# quite difficult to reliably parse arbitrarily-formatted input.  So the current code here is
	# set up to handle Nagios 2 standards, with the understanding that the problem must be fixed
	# upstream, either by removing the single-quote character from the configured set of illegal
	# macro output characters, or by modifying the Nagios code either so a single-quote character
	# is automatically removed from the set of illegal characters when processing performance data,
	# or so performance data is not even subject to this kind of filtering.
	#
	my $unquoted_label_pattern = '[^= \']+';
	my $quoted_label_pattern   = '\'(?:[^\']|\'\')+\'';
	my $val_pat                = '-?\d*\.?\d*[%\w]*';
	## This loop relies on the /g modifier to grab successive metrics, and
	## the later testing also depends on the /c modifier to be present here.
	while ( $perfdata =~ /($unquoted_label_pattern|$quoted_label_pattern)=($val_pat);?(\S*)\s*/gco ) {
	    my $label = $1;
	    my $value = $2;
	    my $other = $3;
	    ### print "---- \$1 = $1\n";
	    ### print "---- \$2 = $2\n";
	    ### print "---- \$3 = $3\n";

	    # Note:  warn and crit can be in "range" format, and so are not necessarily
	    # just simple numbers.  See the Nagios plug-in development guidelines
	    # (http://nagiosplug.sourceforge.net/developer-guidelines.html#THRESHOLDFORMAT).
	    my ( $warn, $crit, $min, $max ) = split( /;/o, $other );

	    ## Remove any enclosing quotes.
	    $label =~ s/'(.*)'/$1/;
	    ## Un-double any embedded quotes.
	    $label =~ s/''/'/go;

	    ## This partial clean-up so far is all we need for use with Foundation.
	    push @foundation_labels, $label;

	    ## Handle "/" characters in performance data that are invalid in rrdtool DS names.
	    if ( $label =~ m:^/: ) {
		if ( $label =~ m:/\w+: ) {
		    ## We have a /mount point, most likely. Replace "/" with "_" throughout.
		    $label =~ s:/:_:go;
		}
		else {
		    ## We have just "/". Replace it with the word "root".
		    $label =~ s:/:root:o;
		}
	    }

	    ## An RRD Data Source-name must be 1 to 19 characters long, using only the characters
	    ## [a-zA-Z0-9_].  So we modify the incoming label to conform to that construction.
	    $label =~ tr/ /_/s;
	    $label =~ tr/a-zA-Z0-9_//cd;

	    ## If we need to truncate, we use the leading characters; this is a change (Mar 31 2009)
	    ## from the previous release of this script, which used the trailing characters instead.
	    if ( length($label) > 19 ) {
		$label = substr( $label, 0, 19 );
	    }

	    # Convert Strings to Numbers, so we get decent values to pump into the
	    # database and into RRD graphs.
	    # This also ends up trimming trailing zeroes after the decimal point,
	    # and even the decimal point itself if the number is an exact integer.
	    # Note that this might not create sensible warn and crit values if they
	    # were not supplied in the performance data.
	    foreach my $val ( \$value, \$warn, \$crit, \$min, \$max ) {
		$$val = 0 if ( !defined($$val) or $$val eq '' );
		### print "------ in perfdata '$perfdata':  $$val => ";
		unless ( $$val eq 'U' ) {
		    $$val =~ s/[^-.0-9].*//;
		    $$val = $$val + 0;
		}
		### print "$$val\n";
	    }

	    # Push the values
	    push @rrd_labels, $label;
	    push @values,     $value;
	    push @warns,      $warn;
	    push @crits,      $crit;
	    push @mins,       $min;
	    push @maxs,       $max;
	    print FP "Adding label=$label,value=$value,warn=$warn,crit=$crit,min=$min,max=$max\n" if ($debug_basic);
	    ## print "---- Adding label=$label,value=$value,warn=$warn,crit=$crit,min=$min,max=$max\n" if ($debug_basic);
	}
	if ( $#values < 0 ) {
	    if ( $perfdata eq '' ) {
		print FP "No perfdata is present for host \"$host\" service \"$svcdesc\".\n" if ($debug_basic);
	    }
	    else {
		print FP "Invalid perfdata format (no values) in \"$perfdata\" for host \"$host\" service \"$svcdesc\".\n" if ($debug_minimal);
	    }
	    next;
	}
	if ( $perfdata =~ /(.+)/go ) {
	    ## There was something left after our parsing above, but there shouldn't be.
	    ## Let's tell somebody about it, so it can get fixed upstream.
	    print FP "Invalid perfdata format (excess text: '$1') in \"$perfdata\".\n" if ($debug_minimal);
	}
    }
    elsif ( defined($parseregx) and $parseregx ) {
	$_      = $statustext;
	@values = /$parseregx/;    # @values is same as array of $1,$2,$3,...
	if ( $#values < 0 ) {
	    print FP "No match in status text for regular expression $parseregx.\n" if ($debug_basic);
	    next;
	}
	else {
	    print FP "Match in status text for regular expression $parseregx\n" if ($debug_basic);
	}
    }

    if ($process_rrd_updates) {
	my @lines = ();
	## Check to see if the RRD already exists. If not, create it.
	my $rrdfilename = replace_macros( "$rrdname", \%macros, \@rrd_labels );
	$rrdfilename =~ s/\s/_/go;
	if ( !stat($rrdfilename) ) {
	    my $failure_flag = 0;

	    my $rrdcommand = replace_macros( $rrdcreatestring, \%macros, \@rrd_labels, \@values, \@warns, \@crits, \@mins, \@maxs );
	    print FP "Create RRD command: $rrdcommand\n" if ($debug_maximal);
	    if ($use_shared_rrd_module) {
		## Drop possible i/o redirection, which is useless in this context.
		$rrdcommand =~ s/\s2>&1//;
		my @command_args = command_arguments($rrdcommand);
		## Drop the shell command.
		shift @command_args;
		## Drop the RRD command.
		my $action_type = shift @command_args;
		if ( $action_type eq 'create' ) {
		    RRDs::create(@command_args);
		    my $ERR = RRDs::error;
		    if ($ERR) {
			print FP "ERROR:  Failed RRD create command: $ERR\n" if ($debug_minimal);
			$failure_flag = 1;
		    }
		}
		else {
		    print FP "ERROR:  Invalid RRD create command: $rrdcommand\n" if ($debug_minimal);
		    $failure_flag = 1;
		}
	    }
	    else {
		## Ensure that we can capture all the error output from the command.
		$rrdcommand .= ' 2>&1' if ( $rrdcommand !~ /\s2>&1/ );
		@lines = qx($rrdcommand);
		if ( $? != 0 ) {
		    my $status_message = wait_status_message($?);
		    print FP "ERROR:  RRD create of $rrdfilename failed with $status_message:\n@lines\n" if ($debug_minimal);
		    $failure_flag = 1;
		}
		else {
		    print FP "Return: @lines\n" if ($debug_basic);
		}
	    }

	    # We proceed with the chown and chmod even if the RRD create failed,
	    # in case these commands might clean up old badness.

	    my $cmd = "chown nagios.nagios $rrdfilename";
	    @lines = qx($cmd);
	    if ( $? != 0 ) {
		my $status_message = wait_status_message($?);
		print FP "ERROR:  chown of $rrdfilename failed with $status_message:\n@lines\n" if ($debug_minimal);
		$failure_flag = 1;
	    }

	    $cmd   = "chmod g+w $rrdfilename";
	    @lines = qx($cmd);
	    if ( $? != 0 ) {
		my $status_message = wait_status_message($?);
		print FP "ERROR:  chmod of $rrdfilename failed with $status_message:\n@lines\n" if ($debug_minimal);
		$failure_flag = 1;
	    }

	    if ($failure_flag) {
		$rrdcreate_failures++;
	    }
	    else {
		$rrdcreate_count++;
	    }
	}

	# Insert into Status Viewer graph config database. Check create even if RRD exists.
	createsvdb( $host, $svcdesc, "RRD", $configlabel, $rrdfilename );

	my $rrdcommand = replace_macros( $rrdupdatestring, \%macros, \@rrd_labels, \@values, \@warns, \@crits, \@mins, \@maxs );
	print FP "Update RRD command: $rrdcommand\n" if ($debug_maximal);
	if ($use_shared_rrd_module) {
	    ## Drop possible i/o redirection, which is useless in this context.
	    $rrdcommand =~ s/\s2>&1//;
	    my @command_args = command_arguments($rrdcommand);
	    ## Drop the shell command.
	    shift @command_args;
	    ## Drop the RRD command.
	    my $action_type = shift @command_args;
	    if ( $action_type eq 'update' ) {
		RRDs::update(@command_args);
		my $ERR = RRDs::error;
		if ($ERR) {
		    print FP "ERROR:  Failed RRD update command: $ERR\n" if ($debug_minimal);
		    $rrdupdate_failures++;
		}
		else {
		    $rrdupdate_count++;
		}
	    }
	    else {
		print FP "ERROR:  Invalid RRD update command: $rrdcommand\n" if ($debug_minimal);
		$rrdupdate_failures++;
	    }
	}
	else {
	    ## Ensure that we can capture all the error output from the command.
	    $rrdcommand .= ' 2>&1' if ( $rrdcommand !~ /\s2>&1/ );
	    @lines = qx($rrdcommand);
	    if ( $? != 0 ) {
		my $status_message = wait_status_message($?);
		print FP "ERROR:  RRD update of $rrdfilename failed with $status_message:\n@lines\n" if ($debug_minimal);
		$rrdupdate_failures++;
	    }
	    else {
		print FP "Return: @lines\n" if ($debug_basic);
		$rrdupdate_count++;
	    }
	}
    }

    #
    #	Post data to Foundation
    #
    if ($process_foundation_db_updates) {
	## Look to see if we are using parsed-out status data as perf data (application of /$parseregx/ above
	## to populate @values), in which case we have no labels, so create them from the RRD DS names.
	if ( defined($parseregx) and $parseregx and ( $#foundation_labels < 0 ) ) {
	    ## Looking for DS names in rrdcreate string, e.g., $RRDTOOL$ create $RRDNAME$ DS:pct_used:GAUGE:600:0:100
	    while ( $rrdcreatestring =~ s/\sDS:(\w+)// ) {
		my $synth_label = $1;
		print FP "\nLabel $synth_label found in rrd create string: $rrdcreatestring\n" if ($debug_maximal);
		push @foundation_labels, $synth_label;
	    }
	}
	foundation_post_perf( $lastcheck, $host, $svcdesc, \@foundation_labels, \@values );
    }

    if ($debug_basic) {
	my $time_so_far = sprintf( "%0.3f", ( Time::HiRes::time() - $start_time ) );
	print FP "Elapsed Execution Time = $time_so_far seconds\n";
    }

}    # End read loop for this line

$dbh->disconnect();
$dbh = undef;
close DATA;
if ($debug) {
    my $time_so_far = sprintf( "%0.3f", ( Time::HiRes::time() - $start_time ) );
    print FP "****************************************************************************\n" if ($debug_basic);
    my $tmpstring = scalar localtime();
    $tmpstring .= " Total Services = $services_count;";
    $tmpstring .= " RRD Creates = $rrdcreate_count, Failures = $rrdcreate_failures;";
    $tmpstring .= " RRD Updates = $rrdupdate_count, Failures = $rrdupdate_failures;";
    $tmpstring .= " Execution Time = $time_so_far seconds";
    print FP "$tmpstring\n";
    print FP "****************************************************************************\n" if ($debug_basic);
    close FP;
}
exit 0;

####################################################################
# Subroutines
####################################################################

sub print_usage {
    print "Performance Data Handler script for Nagios V2 and V3\n";
    print "Copyright (c) 2009 Groundwork Open Source Solutions. All Rights Reserved.\n\n";
    print "Usage: process_service_perf_db_file.pl [--version]\n";
    print "(In normal operation, no arguments are specified.)\n";
    exit $ERRORS{"UNKNOWN"};
}

sub log_and_die {
    my $message = shift;
    if ( defined( fileno FP ) ) {
	print FP "$message\n";
	FP->flush;
    }
    ## force output flushing before we quit
    STDOUT->autoflush(1);
    STDERR->autoflush(1);
    if ( defined $dbh ) {
	## This is the important part of the cleanup:  close the DB connection gracefully,
	## so we don't increase the server-side count of aborted connections.
	$dbh->disconnect();
    }

    # Note:  The "die" below won't really die if we're in the middle of sending data to Foundation.
    # Apparently, it invokes some sort of eval{} processing that traps this death and returns the
    # message back to our code, which then redirects its output back to the FP descriptor instead
    # of to STDERR.  The eval{} itself is buried in some of the modules we call.  With further
    # instrumentation not shown here, we have seen interrupts occur in at least these packages:
    #
    # IO::Handle
    # IO::Select
    # IO::Socket::INET
    # LWP::Protocol::http
    # LWP::Protocol::http::SocketMethods
    # main
    # Net::HTTP::Methods
    #
    # Instead of exiting directly here instead:
    # exit 1;
    # we allow the module we've called to clean itself up.  But then our own subroutine that is
    # calling a module that does an eval{} must check this flag on its way out, and immediately
    # exit the entire script, rather than returning to its caller.  The only downside is that
    # we get one copy of the message in the logfile from the "print FP" above, and another copy
    # from the code that was interrupted, after it traps this die().  We'll live with that.
    $termination_requested = 1;

    die "$message\n";
}

sub handle_exit_signal {
    my $signame = shift;
    ## Any processing we do in this asynchronous signal handler is inherently a bit dangerous
    ## due to possible race conditions, but we'll go ahead and clean up as best we can.
    log_and_die "\nERROR:  Received a SIG$signame signal after processing roughly $services_count services; aborting!\n";
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

sub find_service_hashref {
    my $svcdesc          = shift;
    my $host             = shift;
    my $returned_hashref = undef;
    my $service_patterns_arrayref;

    if ( !exists $service_regx_config{$host} ) {
	return $returned_hashref;
    }
    $service_patterns_arrayref = $service_regx_config{$host};

    for my $svc_hashref ( @{$service_patterns_arrayref} ) {
	if ( $svcdesc =~ /$svc_hashref->{serviceregx}/ ) {
	    ## Check to see if more than one pattern matches.
	    if ( defined $returned_hashref ) {
		print FP "Multiple service pattern matches. Pattern /$svc_hashref->{serviceregx}/ for host '$host' also matches. Ignored.\n";
		next;
	    }
	    $returned_hashref = $svc_hashref;
	    if ($debug_maximal) {
		print FP "'$svcdesc' matches database service pattern /$svc_hashref->{serviceregx}/ for host '$host'. Using this entry.\n";
	    }
	    else {
		## Don't bother checking for multiple service pattern matches.
		return $returned_hashref;
	    }
	}
    }
    return $returned_hashref;
}

sub replace_macros {
    my $string     = shift;
    my $macros_ref = shift;
    my $labels_ref = shift;
    my $values_ref = shift;
    my $warns_ref  = shift;
    my $crits_ref  = shift;
    my $mins_ref   = shift;
    my $maxs_ref   = shift;
    my $i;

    $string =~ s/\s+/ /g;

    ## RRDNAME may contain other macros, i.e., $HOST$, $SERVICE$, so substitute this first.
    $string =~ s/\$RRDNAME\$/$$macros_ref{'\$RRDNAME\$'}/g;

    ## Process LIST macros

    #   Substitute LABELLIST with colon-separated labels, i.e., $LABELLIST$ => label1:label2:label3
    if ( $string =~ /\$LABELLIST\$/ ) {
	my $list = join( ':', @$labels_ref );
	$string =~ s/\$LABELLIST\$/$list/g;
    }

    #   Substitute VALUELIST with semicolon separated labels, i.e., $VALUELIST$ => 1:2:3
    if ( $string =~ /\$VALUELIST\$/ ) {
	my $list = join( ':', @$values_ref );
	$string =~ s/\$VALUELIST\$/$list/g;
    }

    #	Substitute LISTSTART-LISTEND macros, i.e.,
    # $LISTSTART$DS:$LABEL#$:GAUGE:900:U:U$LISTEND$ => DS:label1:GAUGE:900:U:U DS:label2:GAUGE:900:U:U DS:label3:GAUGE:900:U:U
    while ( $string =~ /\$LISTSTART\$(.*?)\$LISTEND\$/ ) {
	print FP "Found LISTSTART and LISTEND in $string\n" if ($debug_maximal);
	my $template = $1;
	if ( $template =~ /\$LABEL\#\$/ ) {
	    print FP "Found LABEL# in $template\n" if ($debug_maximal);
	    ## We process the template substitutions one-by-one with respect to the available labels,
	    ## rather than first concatenating a number of copies of the template corresponding to
	    ## the number of available labels, under the assumption that the template might contain
	    ## multiple instances of "$LABEL#$" that all need to be substituted with the same label.
	    my $substituted;
	    my @strings = ();
	    foreach my $label (@$labels_ref) {
		( $substituted = $template ) =~ s/\$LABEL\#\$/$label/g;
		push @strings, $substituted;
	    }
	    my $list = join( ' ', @strings );
	    $string =~ s/\$LISTSTART\$(?:.*?)\$LISTEND\$/$list/;
	    print FP "Resetting string to $string\n" if ($debug_maximal);
	}
    }

    foreach my $macro ( keys %$macros_ref ) {
	$string =~ s/$macro/$$macros_ref{$macro}/g;
    }

    $i = 1;
    foreach my $label (@$labels_ref) {
	$string =~ s/\$LABEL$i\$/$label/g;
	$i++;
    }

    $i = 1;
    foreach my $value (@$values_ref) {
	$string =~ s/\$VALUE$i\$/$value/g;
	$i++;
    }

    $i = 1;
    foreach my $warn (@$warns_ref) {
	$string =~ s/\$WARN$i\$/$warn/g;
	$i++;
    }

    $i = 1;
    foreach my $crit (@$crits_ref) {
	$string =~ s/\$CRIT$i\$/$crit/g;
	$i++;
    }

    $i = 1;
    foreach my $min (@$mins_ref) {
	$string =~ s/\$MIN$i\$/$min/g;
	$i++;
    }

    $i = 1;
    foreach my $max (@$maxs_ref) {
	$string =~ s/\$MAX$i\$/$max/g;
	$i++;
    }

    return $string;
}

#
#	Subroutine to create an entry in the performanceconfig table for each RRD created.
#
sub createsvdb {
    ## ($host,$svcdesc,"RRD",$configlabel,$rrdfilename);	# Call sub to insert into Status Viewer graph config database
    my $host        = shift;
    my $service     = shift;
    my $type        = shift;
    my $configlabel = shift;
    my $rrdfilename = shift;
    my $query       = undef;
    my $sth         = undef;
    my $id          = undef;

    if ( defined $host_service_id{$host}{$service}{$rrdfilename} ) {
	print FP
	  "Table host_service, host=$host, service=$service already has an existing entry for location $rrdfilename. New entry not added.\n"
	  if ($debug_maximal);
	return;
    }

    $query = "INSERT INTO datatype (type,location) VALUES('$type','$rrdfilename')";
    print FP "SQL = $query\n" if ($debug_maximal);
    $sth = $dbh->prepare($query);
    if ( !( $sth->execute() ) ) {
	print FP $sth->errstr if ($debug_minimal);
	return;
    }
    $sth->finish();

    $query = "SELECT datatype_id FROM datatype WHERE type='$type' AND location='$rrdfilename'";
    print FP "SQL = $query\n" if ($debug_maximal);
    $sth = $dbh->prepare($query);
    if ( !( $sth->execute() ) ) {
	print FP $sth->errstr if ($debug_minimal);
	return;
    }
    $id = undef;
    while ( my $row = $sth->fetchrow_hashref() ) {
	$id = $$row{datatype_id};
    }
    $sth->finish();
    if ( !defined $id ) {
	print FP "No datatype_id found for type $type, location $rrdfilename. Possible error on insert into datatype table.\n"
	  if ($debug_minimal);
	return;
    }

    $query = "INSERT INTO host_service (host,service,label,dataname,datatype_id) VALUES('$host','$service','$configlabel','','$id')";
    print FP "SQL = $query\n" if ($debug_maximal);
    $sth = $dbh->prepare($query);
    if ( !( $sth->execute() ) ) {
	print FP $sth->errstr if ($debug_minimal);
	return;
    }
    $sth->finish();

    $host_service_id{$host}{$service}{$rrdfilename} = $id;

    print FP "Successfully inserted entry into datatype and host_service tables.\n" if ($debug_maximal);
    return;
}

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
	    print FP "RRD command error, starting here: $1\n" if ($debug_minimal);
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

sub foundation_post_perf {
    my $lastchecktime   = shift;
    my $host            = shift;
    my $service         = shift;
    my $label_array_ref = shift;                                                           #reference to ds label=>value
    my $value_array_ref = shift;                                                           #reference to ds label=>value
    my $target_url      = 'http://localhost:8080/foundation-webapp/performanceDataPost';

#http://rmsvf18/foundation-webapp/performanceDataPost?hostname=localhost&servicedescription=icmp_ping&performancedatalabel=ResponseTime&performancevalue=5.0&checkdate=2006-09-17%2012:12:13
#	hostname=localhost&
#	servicedescription=icmp_ping&
#	performancedatalabel=ResponseTime&
#	performancevalue=5.0&
#	checkdate=2006-09-17%2012:12:13

    my ( $sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $isdst ) = localtime($lastchecktime);
    my $startdate = sprintf "%04d-%02d-%02d %02d:%02d:%02d", $year + 1900, $mon + 1, $mday, $hour, $min, $sec;
    print FP "Posting data to Foundation\n" if ($debug_basic);
    my %tmphash = ();
    my $i       = 0;
    foreach my $label ( @{$label_array_ref} ) {
	$tmphash{"performancedatalabel"} = $service . "_" . $label;
	$tmphash{"performancevalue"}     = ${$value_array_ref}[$i];
	if ($debug_maximal) {
	    print FP "\tperformancedatalabel=$label\n";
	    print FP "\tperformancevalue=${$value_array_ref}[$i]\n";
	}
	$i++;
	my $url = URI->new($target_url);
	$url->query_form(
	    'hostname'           => $host,
	    'servicedescription' => $service,
	    'checkdate'          => $startdate,
	    %tmphash
	);
	my $browser = LWP::UserAgent->new;
	$browser->timeout($foundation_submission_timeout);
	## This get() call does an internal eval{} which will trap a call to die() which may occur
	## in our signal handler when this script receives a SIGTERM.  We test for that condition
	## below, to ensure we really do exit right away in that situation.
	my $response = $browser->get($url);

	# A good result should contain this output:
	# out.println("<HTML><HEAD>Performance Servlet</HEAD><BODY><H1>Performance Data Added</BODY></HTML>");

	# $response->is_success just indicates whether the HTTP transfer succeeded, not
	# whether or not the application at the other end worked.  So we must test both.
	if ( !$response->is_success ) {
	    print FP "\tFoundation Error: " . $response->status_line . "\n" if ($debug_minimal);
	    exit 1 if ($termination_requested);
	    return;
	}
	elsif ( $response->content !~ /Performance Data Added/ ) {
	    print FP "\tFoundation Error: " . $response->content . "\n" if ($debug_minimal);
	    exit 1 if ($termination_requested);
	    return;
	}
    }
    exit 1 if ($termination_requested);
    return;
}

__END__

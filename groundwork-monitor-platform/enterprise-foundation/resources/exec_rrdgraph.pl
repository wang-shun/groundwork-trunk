#!/usr/local/groundwork/perl/bin/perl --
#
# Copyright (c) 2004-2015 GroundWork Open Source, Inc. (www.groundworkopensource.com)
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of version 2 of the GNU General Public License
# as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
#

# This script simply executes the passed-in, single-quote-enclosed RRD
# graph command, after making certain macro and character substitutions.
# Return value is a .png file written to the standard output stream.

use strict;
use Time::HiRes;
use POSIX qw(strftime);
use RRDs;

# Specify whether to use a shared library to implement RRD file access,
# or to fork an external process for such work (the legacy implementation).
# Set to 1 (recommended) for high performance, to 0 only as an emergency fallback.
my $use_shared_rrd_module = 1;

my $rrdgraph_command = $ARGV[0];
my $debug            = $ARGV[1] || '';
my $debuglog         = $ARGV[2] ? 'STDOUT' : '/tmp/exec_rrdgraph.log';

# This manual override can sometimes come in handy for debugging.
# However, it does not interact well with the execution of this script
# via the Performance Configuration Test button; it is only for temporary
# use in debugging executions initiated directly from the Status Viewer,
# after which this must again be commented out.
# $debug = 3;

my $debug_minimal    = ( $debug =~ /^\d+$/ && $debug >= 1 );
my $debug_basic      = ( $debug =~ /^\d+$/ && $debug >= 2 );
my $debug_maximal    = ( $debug =~ /^\d+$/ && $debug >= 3 );
my $exit_status      = 0;
my $start_time;

if ($debug_minimal) {
    if ($debuglog eq 'STDOUT') {
	open (LOG, '>&STDOUT');
    }
    else {
	open (LOG, '>', $debuglog);
    }
}

if ($debug_basic) {
    $start_time = Time::HiRes::time();
}

if ($debug_maximal) {
    print LOG "===================================\n";
    print LOG "incoming command: $rrdgraph_command\n";
}

# Perform necessary substitutions.
# Timestamp format: "Thu Jun 16 10\\:26\\:54 2011 PDT".
if ($rrdgraph_command =~ /\$GRAPH_START_TIME\$/ && $rrdgraph_command =~ /&space;--start(?:&space;)+(\d+)/) {
    my $graph_start_time = strftime('%a %b %e %H\\\\:%M\\\\:%S %Y %Z', localtime $1);
    $rrdgraph_command =~ s/\$GRAPH_START_TIME\$/$graph_start_time/g;
}
if ($rrdgraph_command =~ /\$GRAPH_END_TIME\$/ && $rrdgraph_command =~ /&space;--end(?:&space;)+(\d+)/) {
    my $graph_end_time = strftime('%a %b %e %H\\\\:%M\\\\:%S %Y %Z', localtime $1);
    $rrdgraph_command =~ s/\$GRAPH_END_TIME\$/$graph_end_time/g;
}
my @rrdgraph_command = split(/&space;/, $rrdgraph_command);
foreach (@rrdgraph_command) {
    utf8::decode($_) if /^DEF:/;
}
$rrdgraph_command = join(' ', @rrdgraph_command);
utf8::downgrade($rrdgraph_command,1);
$rrdgraph_command =~ s/^'//;
$rrdgraph_command =~ s/'$//;
$rrdgraph_command =~ s/&quot;/"/g;
# $rrdgraph_command =~ s/&space;/ /g;  # Now done above.

if ($debug_maximal) {
    print LOG "\n";
    print LOG "outgoing command: $rrdgraph_command\n";
}

if ($use_shared_rrd_module) {
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
	    print LOG "ERROR:  Failed RRD graph command: $ERR\n" if ($debug_minimal);
	    $exit_status = 1;
	}
    }
    else {
	print LOG "ERROR:  Invalid RRD graph command: $rrdgraph_command\n" if ($debug_minimal);
	$exit_status = 1;
    }
}
else {
    system ($rrdgraph_command);
}

if ($debug_basic) {
    my $time_so_far = sprintf( '%0.3f', ( Time::HiRes::time() - $start_time ) );
    print LOG "Execution time:  $time_so_far sec\n";
}

if ($debug_minimal) {
    close LOG;
}

exit $exit_status;

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
	    print LOG "RRD command error, starting here: $1\n" if ($debug_minimal);
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

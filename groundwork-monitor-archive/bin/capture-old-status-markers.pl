#!/usr/local/groundwork/perl/bin/perl -w --

# capture-old-status-markers.pl

# Copyright (c) 2016 GroundWork, Inc. (www.gwos.com).  All rights reserved.
# Use of this software is subject to commercial license terms.

# This script captures logmessage rows from the archive_gwcollagedb database
# that reflect the old status of hosts and services, as they appeared just
# before a certain point in time.  That point in time is supposed to reflect the
# most-recent cutoff when sufficiently-old data was purged from the gwcollagedb
# database.  It is useful for repairing a damaged gwcollagedb database which no
# longer has at least one such row for every host and service.  Some caveats:
#
# (*) This script must be run on the machine where the log-archive-receive.pl
#     script is normally run.
#
# (*) This script must be run interactively, because it prompts for confirmation
#     of the desired cutoff time before proceeding to extract data.
#
# (*) This script is normally only run on a one-time basis, to repair a
#     previously damaged gwcollagedb database.
#
# (*) Running this script is just the first step to take to repair a damaged
#     gwcollagedb database.  To be effective, this run must be followed by
#     running the restore-old-status-markers.pl script to insert the rows it
#     extracted from the archive_gwcollagedb database into the gwcollagedb
#     database.  And then the capture-old-status-markers.pl script, under
#     control of the -m option (to create only "missing" data), will handle
#     the residual cases where no historical data at all is available.  See
#     the full KB documentation for information on all of these scripts.

use strict;
use warnings;

use DBI;
use Fcntl;
use Getopt::Std;
use Time::Local qw(timelocal);
use Time::HiRes;
use Socket;
use Term::ReadLine;
use POSIX qw();

use TypedConfig;

use GW::Logger;
use GW::Foundation;

# ================================================================
# CPAN Packages
# ================================================================

# The following code is taken directly from the Term::ReadPassword
# module (version 0.11) on CPAN, and modified only slightly to
# improve the password prompting.  We fold it in here directly
# because we don't have that module already included in the Perl
# we supply with GroundWork Monitor, and because the GW installer
# won't be applying any new Perl modules before this script is to
# be run.  This way, this master migration script can be run as a
# standalone script.

package Term::ReadPassword;

use strict;
use Term::ReadLine;
use POSIX qw(:termios_h);
my %CC_FIELDS = (
	VEOF => VEOF,
	VEOL => VEOL,
	VERASE => VERASE,
	VINTR => VINTR,
	VKILL => VKILL,
	VQUIT => VQUIT,
	VSUSP => VSUSP,
	VSTART => VSTART,
	VSTOP => VSTOP,
	VMIN => VMIN,
	VTIME => VTIME,
    );

use vars qw(
    $VERSION @ISA @EXPORT @EXPORT_OK
    $ALLOW_STDIN %SPECIAL $SUPPRESS_NEWLINE $INPUT_LIMIT
    $USE_STARS $STAR_STRING $UNSTAR_STRING
);

require Exporter;

@ISA = qw(Exporter);
@EXPORT = qw(
	read_password
);
$VERSION = '0.11';

# The special characters in the input stream
%SPECIAL = (
    "\x03"	=> 'INT',	# Control-C, Interrupt
    "\x15"	=> 'NAK',	# Control-U, NAK (clear buffer)
    "\x08"	=> 'DEL',	# Backspace
    "\x7f"	=> 'DEL',	# Delete
    "\x0d"	=> 'ENT',	# CR, Enter
    "\x0a"	=> 'ENT',	# LF, Enter
);

# The maximum amount of data for the input buffer to hold
$INPUT_LIMIT = 1000;

sub read_password {
    my($prompt, $idle_limit, $interruptable) = @_;
    $prompt = '' unless defined $prompt;
    $idle_limit = 0 unless defined $idle_limit;
    $interruptable = 0 unless defined $interruptable;

    # Let's open the TTY (rather than STDIN) if we can
    local(*TTY, *TTYOUT);
    my($in, $out) = Term::ReadLine->findConsole;
    die "No console available" unless $in;
    if (open TTY, "+<$in") {
	# Cool
    } elsif ($ALLOW_STDIN) {
	open TTY, "<&STDIN"
	    or die "Can't re-open STDIN: $!";
    } else {
	die "Can't open '$in' read/write: $!";
    }

    # And let's send the output to the TTY as well
    if (open TTYOUT, ">>$out") {
	# Cool
    } elsif ($ALLOW_STDIN) {
	# Well, let's allow STDOUT as well
	open TTYOUT, ">>&STDOUT"
	    or die "Can't re-open STDOUT: $!";
    } else {
	die "Can't open '$out' for output: $!";
    }

    # Don't buffer it!
    select( (select(TTYOUT), $|=1)[0] );
    print TTYOUT $prompt;

    # Okay, now remember where everything was, so we can put it back when
    # we're done
    my $fd_tty = fileno(TTY);
    my $term = POSIX::Termios->new();
    $term->getattr($fd_tty);
    my $original_flags = $term->getlflag();
    my %original_cc;
    for my $field_name (keys %CC_FIELDS) {
	$original_cc{$field_name} = $term->getcc($CC_FIELDS{$field_name});
    }

    # What makes this setup different from the ordinary?
    # No keyboard-generated signals, no echoing, no canonical input
    # processing (like backspace handling)
    my $flags = $original_flags & ~(ISIG | ECHO | ICANON);
    $term->setlflag($flags);
    if ($idle_limit) {
	# $idle_limit is in seconds, so multiply by ten
	$term->setcc(VTIME, 10 * $idle_limit);
	# Continue running the program after that time, even if there
	# weren't any characters typed
	$term->setcc(VMIN, 0);
    } else {
	# No time limit, but...
	$term->setcc(VTIME, 0);
	# Continue as soon as one character has been struck
	$term->setcc(VMIN, 1);
    }

    # Optionally echo stars in place of password characters. The
    # $unstar_string uses backspace characters.
    my $star_string = $USE_STARS ? ($STAR_STRING || '*') : '';
    my $unstar_string = $USE_STARS ? ($UNSTAR_STRING || "\b*\b \b") : '';

    # If there's anything already buffered, we should throw it out. This
    # is to discourage users from typing their password before they see
    # the prompt, since their keystrokes may be echoing on the screen.
    #
    # So this statement supposedly makes sure the prompt goes out, the
    # unread input buffer is discarded, and _then_ the changes take
    # effect. Thus, everything they typed ahead is (probably) echoed.
    $term->setattr($fd_tty, TCSAFLUSH);

    my $input = '';
    my $return_value;
KEYSTROKE:
    while (1) {
	my $new_keys = '';
	my $count = sysread(TTY, $new_keys, 99);
	# We're here, so either the idle_limit expired, or the user typed
	# something.
	if ($count) {
	    for my $new_key (split //, $new_keys) {
		if (my $meaning = $SPECIAL{$new_key}) {
		    if ($meaning eq 'ENT') {
			# Enter/return key
			# Return what we have so far
			$return_value = $input;
			last KEYSTROKE;
		    } elsif ($meaning eq 'DEL') {
			# Delete/backspace key
			# Take back one char, if possible
			if (length $input) {
			    $input = substr $input, 0, length($input)-1;
			    print TTYOUT $unstar_string;
			}
		    } elsif ($meaning eq 'NAK') {
			# Control-U (NAK)
			# Clear what we have read so far
			for (1..length $input) {
			    print TTYOUT $unstar_string;
			}
			$input = '';
		    } elsif ($interruptable and $meaning eq 'INT') {
			# Breaking out of the program
			# Return early
			last KEYSTROKE;
		    } else {
			# Just an ordinary keystroke
			$input .= $new_key;
			print TTYOUT $star_string;
		    }
		} else {
		    # Not special
		    $input .= $new_key;
		    print TTYOUT $star_string;
		}
	    }
	    # Just in case someone sends a lot of data
	    $input = substr($input, 0, $INPUT_LIMIT)
		if length($input) > $INPUT_LIMIT;
	} else {
	    # No count, so something went wrong. Assume timeout.
	    # Return early
	    last KEYSTROKE;
	}
    }

    # Done with waiting for input. Let's not leave the cursor sitting
    # there, after the prompt.
    print TTYOUT "\n" unless $SUPPRESS_NEWLINE;

    # Let's put everything back where we found it.
    $term->setlflag($original_flags);
    while (my($field, $value) = each %original_cc) {
	$term->setcc($CC_FIELDS{$field}, $value);
    }
    $term->setattr($fd_tty, TCSAFLUSH);
    close(TTY);
    close(TTYOUT);
    $return_value;
}

# ----------------------------------------------------------------
# Back to the present.
# ----------------------------------------------------------------

package main;

# ================================
# Package Parameters
# ================================

my $PROGNAME       = "capture-old-status-markers.pl";
my $VERSION        = "0.0.8";
my $COPYRIGHT_YEAR = "2016";

my $send_config_file    = '/usr/local/groundwork/config/log-archive-send.conf';
my $receive_config_file = '/usr/local/groundwork/config/log-archive-receive.conf';
my $default_log_file    = '/usr/local/groundwork/foundation/container/logs/status-markers.log';

# If we were not parasitizing another application's config file that does not already
# contain this parameter, we would specify this value in our own config file.  But
# it's unlikely that this value needs adjustment, so this is good enough.
my $dump_copy_block_rows = 10000;

# ================================
# Command-Line Parameters
# ================================

# In theory, these parameter settings could be overridden by command-line arguments.
# In practice, we don't currently support any such arguments; this script uses only
# a fixed set of arguments.

my $debug_config                = 0;                      # if set, spill out certain data about config-file processing to STDOUT
my $show_help                   = 0;
my $show_version                = 0;
my $run_interactively           = 1;                      # Default on in this program to force logging of all useful output.
my $reflect_log_to_stdout       = 1;                      # Default on in this program to force logging of all useful output.
my $output_pathname             = undef;
my $capture_status_for_hosts    = undef;
my $capture_status_for_services = undef;

# ================================
# Configuration Parameters
# ================================

# Parameters in the config file.

my $enable_processing = undef;

# Possible $debug_level values:
# 0 = no info of any kind printed, except for startup/shutdown messages and major errors
# 1 = print just error info and summary statistical data
# 2 = also print basic debug info
# 3 = print detailed debug info
# Initial level, to be overwritten by a value from the config file:
my $debug_level = 1;

# We use the default log file which is hardcoded above, essentially just
# because all other configuration values we can draw from the config file
# which is shared with the log-archive-receive.pl program, but it doesn't
# make sense to dump log output from this script into that script's log file.
my $logfile                = $default_log_file;
my $max_logfile_size       = undef;    # log rotate is handled externally, not here
my $max_logfiles_to_retain = undef;    # log rotate is handled externally, not here

my $archive_dbtype = undef;
my $archive_dbhost = undef;
my $archive_dbport = undef;
my $archive_dbname = undef;
my $archive_dbuser = undef;
my $archive_dbpass = undef;

my $target_script_machine    = undef;
my $target_script_ip_address = undef;

my $runtime_dbtype = undef;
my $runtime_dbhost = undef;
my $runtime_dbport = undef;
my $runtime_dbname = undef;
my $runtime_dbuser = undef;
my $runtime_dbpass = undef;

my $foundation_host = '';
my $foundation_port = 4913;

# Parameters derived from the config-file parameters.

# These values will be replaced once $debug_level is itself replaced by a value from
# the config file.  But we want these values to be operational even before the config
# file is read, in case we need to debug early operation of this script.
my $debug_minimal = ( $debug_level >= 1 );
my $debug_basic   = ( $debug_level >= 2 );
my $debug_maximal = ( $debug_level >= 3 );

my $monitor_server_hostname   = undef;
my $monitor_server_ip_address = undef;

# Locally defined parameters, that might someday move to the config file.

# Socket timeout (in seconds), to address GWMON-7407.  Typical value
# is 60.  Set to 0 to disable.
#
# This timeout is here only for use in emergencies, when Foundation
# has completely frozen up and is no longer reading (will never read)
# a socket we have open.  We don't want to set this value so low that
# it will interfere with normal communication, even given the fact that
# Foundation may wait a rather long time between sips from this straw
# as it processes a large bundle of messages that we sent it, or is
# otherwise busy and just cannot get back around to reading the socket
# in a reasonably short time.
my $socket_send_timeout = 60;

# This is the actual SO_SNDBUF value, as set by setsockopt().  This is
# therefore the actual size of the data buffer available for writing,
# irrespective of additional kernel bookkeeping overhead.  This will
# have no effect without the companion as-yet-undocumented patch to
# IO::Socket::INET.  Set this to 0 to use the system default socket send
# buffer size.  A typical value to set here is 262144.  (Note that the
# value specified here is likely to be limited to something like 131071
# by the sysctl net.core.wmem_max parameter.)
#
# This value is not currently used by the GW::Foundation package we call,
# so we just let it default for future compatibility.
my $send_buffer_size = 0;

# ================================
# Working Variables
# ================================

my $runtime_dbh = undef;
my $archive_dbh = undef;

my $output_file_handle = undef;

# The format of $purge_end_timestamp is 'YYYY-MM-DD hh:mm:ss' as a PostgreSQL
# "timestamp without time zone" value, which effectively means the time is
# assumed to be expressed in the local timezone.
my $purge_end_timestamp = undef;

my $script_start_time                  = undef;
my $script_init_end_time               = undef;
my $script_setup_end_time              = undef;
my $script_host_processing_end_time    = undef;
my $script_service_processing_end_time = undef;

my $host_rows_captured    = 0;
my $service_rows_captured = 0;
my $total_rows_captured   = 0;

my $process_outcome = undef;

my $output_unlinked = undef;

# These variables really ought to just be local to the log_action_statistics() routine,
# except that we want a few of them to be accessible to the send_outcome_to_foundation()
# routine so the message it sends is more informative.
my $init_time         = undef;
my $setup_time        = undef;
my $host_time         = undef;
my $service_time      = undef;
my $capture_data_time = undef;
my $total_time        = undef;
my $init_timestamp    = undef;
my $setup_timestamp   = undef;
my $host_timestamp    = undef;
my $service_timestamp = undef;
my $total_timestamp   = undef;
my $row_capture_speed = undef;

use constant HOURS_PER_DAY      => 24;
use constant MINUTES_PER_HOUR   => 60;
use constant SECONDS_PER_MINUTE => 60;
use constant MINUTES_PER_DAY    => MINUTES_PER_HOUR * HOURS_PER_DAY;
use constant SECONDS_PER_DAY    => SECONDS_PER_MINUTE * MINUTES_PER_DAY;
use constant SECONDS_PER_HOUR   => SECONDS_PER_MINUTE * MINUTES_PER_HOUR;

use constant ERROR_STATUS    => 0;
use constant STOP_STATUS     => 1;
use constant RESTART_STATUS  => 2;
use constant CONTINUE_STATUS => 3;

# ================================================================
# Program.
# ================================================================

exit ((main() == ERROR_STATUS) ? 1 : 0);

# ================================================================
# Supporting subroutines.
# ================================================================

sub main {
    my @SAVED_ARGV = @ARGV;

    capture_timing(\$script_start_time);

    # If this script fails, and we have successfully made it past reading the config file (so we know how to send
    # messages to Foundation), the $status_message will be sent to Foundation, and show up in the Event Console.
    # Thus there is no point in defining $status_message in the code below until we have made it past that point.
    my $status_message = '';
    $process_outcome = 1;

    if (open (STDERR, '>>&STDOUT')) {
	## Apparently, appending STDERR to the STDOUT stream isn't by itself enough
	## to get the line disciplines of STDOUT and STDERR synchronized and their
	## respective messages appearing in order as produced.  The combination is
	## apparently happening at the file-descriptor level, not at the level of
	## Perl's i/o buffering.  So it's still possible to have their respective
	## output streams inappropriately interleaved, brought on by buffering of
	## STDOUT messages.  To prevent that, we need to have STDOUT use the same
	## buffering as STDERR, namely to flush every line as soon as it is produced.
	## This is certainly a less-efficient use of system resources, but we don't
	## expect this program to write much to the STDOUT stream anyway.
	STDOUT->autoflush(1);
    }
    else {
	print "ERROR:  STDERR cannot be redirected to STDOUT!\n";
	$process_outcome = 0;
    }

    if ($process_outcome) {
	my $command_line_status = parse_command_line();
	if ( !$command_line_status ) {
	    spill_message "FATAL:  $PROGNAME either cannot understand its command-line parameters or cannot find its config file";
	    exit 1;
	}

	if ($show_version) {
	    print_version();
	}

	if ($show_help) {
	    print_usage();
	}

	if ($show_version || $show_help) {
	    exit 0;
	}

	if ( $output_pathname && -e $output_pathname ) {
	    spill_message "FATAL:  $PROGNAME cannot proceed, because the specified output file already exists.";
	    exit 1;
	}

	if (not read_send_config_file($send_config_file, $debug_config)) {
	    spill_message "FATAL:  $PROGNAME cannot load configuration from $send_config_file";
	    return ERROR_STATUS;
	}

	if (not read_receive_config_file($receive_config_file, $debug_config)) {
	    spill_message "FATAL:  $PROGNAME cannot load configuration from $receive_config_file";
	    return ERROR_STATUS;
	}

	# Stop if this is just a debugging run.
	return STOP_STATUS if $debug_config;

	# We need to prohibit executing as root (say, for a manual debugging run), so we
	# don't create files and directories that won't be modifiable later on when this
	# script is run in its usual mode as an ordinary user ("nagios").  We purposely
	# delay this test until after simple actions of the script, so we can at least
	# show the version and command-usage messages without difficulty.
	if ($> == 0) {
	    (my $program = $0) =~ s<.*/><>;
	    print "ERROR:  You cannot run $program as root.\n";
	    return ERROR_STATUS;
	}

	# We don't use a message prefix, because this is intended to be an interactive script and
	# the extra text written to the terminal would just be distracting and useless there.  We
	# don't expect multiple concurrent copies of this script to be writing to the log file, so
	# we don't really have a need to disambiguate where each message comes from in that record.
	GW::Logger->new( $logfile, $run_interactively, $reflect_log_to_stdout, $max_logfile_size, $max_logfiles_to_retain, '' );

	if ( !open_logfile() ) {
	    ## The routine will print an error message if it fails, so we don't do so ourselves.
	    $status_message  = 'cannot open log file';
	    $process_outcome = 0;
	}
    }

    if ($process_outcome) {
	## We precede the startup message with a blank line, simply so the startup message is more visible.
	log_message '';
	log_timed_message "=== Status marker capture script (version $VERSION) starting up (process $$). ===";
	log_timed_message "INFO:  Running with options:  " . join (' ', @SAVED_ARGV);
    }

    capture_timing(\$script_init_end_time);

    # Open a connection to the archive database.
    if ($process_outcome) {
	log_timed_message "NOTICE:  Opening a connection to the archive database.";
	$process_outcome = open_database_connection(
	    $archive_dbtype, $archive_dbhost, $archive_dbport, $archive_dbname,
	    $archive_dbuser, $archive_dbpass, \$archive_dbh
	);
	$status_message = 'cannot connect to the archive database' if not $process_outcome;
    }

    # Open a connection to the runtime database.
    if ($process_outcome) {
	log_timed_message "NOTICE:  Opening a connection to the runtime database.";
	$process_outcome = open_database_connection(
	    $runtime_dbtype, $runtime_dbhost, $runtime_dbport, $runtime_dbname,
	    $runtime_dbuser, $runtime_dbpass, \$runtime_dbh
	);
	$status_message = 'cannot connect to the runtime database' if not $process_outcome;
    }

    # Determine the endpoint before which the timestamps to be captured all lie.
    if ($process_outcome) {
	log_timed_message "NOTICE:  Determining the data-purge end timestamp.";
	$process_outcome = determine_purge_end_timestamp( \$purge_end_timestamp );
	$status_message = 'cannot determine the data-purge end timestamp' if not $process_outcome;
    }

    # For convenience at the user level, we use one common output file for both
    # host-related and service-related output.  So rather than opening the file in
    # each individual data-capture routine, we just do so once here at the top level.
    if ( $process_outcome && $output_pathname ) {
	log_timed_message "NOTICE:  Opening the output file.";
	$process_outcome = open_output_file( $output_pathname, \$output_file_handle );
	$status_message = 'cannot open the output file' if not $process_outcome;
    }

    capture_timing(\$script_setup_end_time);

    # Capture old host rows in the archive database.
    if ( $process_outcome && $capture_status_for_hosts ) {
	log_timed_message "NOTICE:  Capturing old host status rows in the archive database.";
	$process_outcome = capture_host_status_markers( $purge_end_timestamp, $output_pathname, $output_file_handle );
	$status_message = 'cannot capture host status rows' if not $process_outcome;
    }

    capture_timing(\$script_host_processing_end_time);

    # Capture old service rows in the archive database.
    if ( $process_outcome && $capture_status_for_services ) {
	log_timed_message "NOTICE:  Capturing old service status rows in the archive database.";
	$process_outcome = capture_service_status_markers( $purge_end_timestamp, $output_pathname, $output_file_handle );
	$status_message = 'cannot capture service status rows' if not $process_outcome;
    }

    # We close the output file, if we attempted to open one, regardless of whether we had difficulty
    # in writing to it.  So there is no "$process_outcome &&" clause in this "if" condition.
    if ( $output_pathname and defined($output_file_handle) and defined(fileno $output_file_handle) ) {
	log_timed_message "NOTICE:  Closing the output file.";
	my $close_outcome = close_output_file( $output_pathname, \$output_file_handle );
	$status_message = 'cannot close the output file' if not $close_outcome;
	$process_outcome &&= $close_outcome;
    }

    capture_timing(\$script_service_processing_end_time);

    # Close the connection to the runtime database.  This is done unconditionally, since we should close
    # the connection even if errors occurred after it was opened.  The routine can internally handle the
    # case where the connection was never opened in the first place because of prior errors.  However,
    # it gets confusing if we log the occurrence of this call under circumstances when it won't actually
    # do anything, so we do qualify the logging here.
    log_timed_message "NOTICE:  Closing the connection to the runtime database." if $runtime_dbh and log_is_open();
    close_database_connection(\$runtime_dbh);

    # Close the connection to the archive database.  This is done unconditionally, since we should close
    # the connection even if errors occurred after it was opened.  The routine can internally handle the
    # case where the connection was never opened in the first place because of prior errors.  However,
    # it gets confusing if we log the occurrence of this call under circumstances when it won't actually
    # do anything, so we do qualify the logging here.
    log_timed_message "NOTICE:  Closing the connection to the archive database." if $archive_dbh and log_is_open();
    close_database_connection(\$archive_dbh);

    log_action_statistics($status_message) if log_is_open();

    send_outcome_to_foundation( $status_message, $process_outcome ) if $output_pathname;

    close_logfile();

    # Now return the overall processing success or failure as the status of this routine.
    # This will be turned into a corresponding script exit code.

    return $process_outcome ? STOP_STATUS : ERROR_STATUS;
}

sub print_version {
    print "$PROGNAME Version:  $VERSION\n";
    print "Copyright $COPYRIGHT_YEAR GroundWork, Inc. (www.gwos.com).\n";
    print "All rights reserved.\n";
}

sub print_usage {
    print <<EOF;

usage:  $PROGNAME -h
        $PROGNAME -v
        $PROGNAME -d
        $PROGNAME [-H] [-S] [-f output_file]

where:  -h:  print this help message
        -v:  print the version number
        -d:  debug config file
        -H:  capture rows for hosts
        -S:  capture rows for services
        -f output_file
             specifies where the found rows should be written, in a format
             acceptable to the restore-old-status-markers.pl script

The usual invocation is:

    $PROGNAME -H -S -f /tmp/logmessage_rows

When running this script to capture markers, at least one of the -H and -S
options (normally both) must be specified.

The -f option is how you specify where the found rows will be written.
Omitting this option is useful for dry-run testing, to run queries but
not to write out found rows to a file.  This dry-run facility can help
both with potential code modifications and with performance testing on
large databases.

EOF

# Usage lines not printed because we hardcode the -i and -o options in this program.
=pod
        $PROGNAME [-H] [-S] [-f output_file] [-i] [-o]
        -i:  run interactively, not as a background process
        -o:  write log messages also to standard output
    $PROGNAME -H -S -i -o -f /tmp/logmessage_rows
The -o option is illegal unless -i is also specified.
=cut

}

# See http://en.wikipedia.org/wiki/Hostname#Restrictions_on_valid_host_names for the tests we run here.
# FIX LATER:  We should probably go further, and run a name-service lookup here, to validate that $hostname
# will actually be useable later on.
sub is_valid_hostname {
    my $hostname = shift;
    my $label    = '(?:[a-zA-Z0-9](?:[-a-zA-Z0-9]{0,61}[a-zA-Z0-9])?)';
    return ( defined($hostname) and $hostname ne '' and length($hostname) <= 255 and $hostname =~ /^$label(?:\.$label)*$/o );
}

sub read_send_config_file {
    my $config_file  = shift;
    my $config_debug = shift;

    # All the config-file processing is wrapped in an eval{}; because TypedConfig
    # throws exceptions when it cannot open the config file or finds bad config data.
    eval {
	my $config = TypedConfig->secure_new( $config_file, $config_debug );

	$runtime_dbtype = $config->get_scalar('runtime_dbtype');
	$runtime_dbhost = $config->get_scalar('runtime_dbhost');
	$runtime_dbport = $config->get_number('runtime_dbport');
	$runtime_dbname = $config->get_scalar('runtime_dbname');
	$runtime_dbuser = $config->get_scalar('runtime_dbuser');
	$runtime_dbpass = $config->get_scalar('runtime_dbpass');
    };
    if ($@) {
	chomp $@;
	$@ =~ s/^ERROR:\s+//i;
	print "ERROR:  Cannot read config file $config_file\n  ($@).\n";
	return 0;
    }

    return 1;
}

sub read_receive_config_file {
    my $config_file  = shift;
    my $config_debug = shift;

    # All the config-file processing is wrapped in an eval{}; because TypedConfig
    # throws exceptions when it cannot open the config file or finds bad config data.
    eval {
	my $config = TypedConfig->secure_new( $config_file, $config_debug );

	# Whether to process anything.  Turn this off if you want to disable
	# this process completely, so log-archiving is prohibited.
	$enable_processing = $config->get_boolean('enable_processing');

	$debug_level = $config->get_number('debug_level');

	$debug_minimal = ( $debug_level >= 1 );
	$debug_basic   = ( $debug_level >= 2 );
	$debug_maximal = ( $debug_level >= 3 );

	# Where to log debug messages.
	#
	# We don't accept the config-file setting for this value, because we are parasitically using
	# the config file for one of the log-archive scripts and we don't want log messages from
	# this script mixed in with log messages from the log archiving.  Instead, we just use the
	# default logfile path which is set as a hardcoded value at the beginning of this script.
	# If there were really some need to allow selection of a logfile distinct from our default
	# logfile for this script, we could implement a command-line -l option to provide the
	# appropriate override path.  For a script which is intended to only be run once (ever) at
	# a customer site, that seems pointlessly extravagant.
	#
	# $logfile = $config->get_scalar ('logfile');

	$archive_dbtype = $config->get_scalar('archive_dbtype');
	$archive_dbhost = $config->get_scalar('archive_dbhost');
	$archive_dbport = $config->get_number('archive_dbport');
	$archive_dbname = $config->get_scalar('archive_dbname');
	$archive_dbuser = $config->get_scalar('archive_dbuser');
	$archive_dbpass = $config->get_scalar('archive_dbpass');

	$target_script_machine    = $config->get_scalar('target_script_machine');
	$target_script_ip_address = $config->get_scalar('target_script_ip_address');

	if ( !is_valid_hostname($target_script_machine) ) {
	    die "ERROR:  configured value for target_script_machine must be a valid hostname\n";
	}

	$monitor_server_hostname = $target_script_machine;
	if ( $monitor_server_hostname eq '' ) {
	    die "ERROR:  configured value for target_script_machine cannot be an empty string\n";
	}
	elsif ( $monitor_server_hostname eq 'localhost' ) {
	    $monitor_server_ip_address = $target_script_ip_address ne '' ? $target_script_ip_address : '127.0.0.1';
	}
	elsif ( $target_script_ip_address ne '' ) {
	    $monitor_server_ip_address = $target_script_ip_address;
	}
	else {
	    ## FIX LATER:  This code is not yet IPv6-compliant.
	    ##
	    ## Note that gethostbyname() can return undef if passed an argument which is not actually found in DNS.
	    ## This can happen, for instance, if the specified host is not an actual hostname on the network.  So we
	    ## need to check the gethostbyname() return value to ensure it is defined.
	    ##
	    ## Also note that gethostbyname() is capable of returning all IP addresses on the machine, but we
	    ## don't have any way to figure out which is the "best" (except perhaps to reject any that look like
	    ## localhost, unless that's the only choice), so we just go with whatever standard algorithm is already
	    ## embedded within gethostbyname() when it is called in scalar context.  The administrator can avoid
	    ## this ambiguity by not defaulting the target_script_ip_address in the config file.
	    my $packed_ip_address = gethostbyname($monitor_server_hostname);
	    ## Check if we could resolve the hostname to an IP address.
	    if ( defined $packed_ip_address ) {
		$monitor_server_ip_address = inet_ntoa($packed_ip_address);
	    }
	    else {
		die "ERROR:  cannot resolve the IP address for target_script_machine \"$monitor_server_hostname\";"
		  . " you can avoid this by specifying a non-empty value for target_script_ip_address\n";
	    }
	}

	$foundation_host = $config->get_scalar('foundation_host');
	$foundation_port = $config->get_number('foundation_port');

	if ( $foundation_host ne '' and !is_valid_hostname($foundation_host) ) {
	    die "ERROR:  configured value for foundation_host must be an empty string or a valid hostname\n";
	}
    };
    if ($@) {
	chomp $@;
	$@ =~ s/^ERROR:\s+//i;
	print "ERROR:  Cannot read config file $config_file\n  ($@).\n";
	return 0;
    }

    return 1;
}

sub parse_command_line {
    ## First, clean up the $default_config_file value in case we print usage.
    ## (This is disabled because of potential working-directory issues with realpath().)
    ## my $real_path = realpath ($default_config_file);
    ## $default_config_file = $real_path if $real_path;

    # The -i and -o options are hardcoded on in this program, so we don't process them here.
    my %opts;
    if ( not getopts( 'hvdHSf:', \%opts ) ) {
	print_usage();
	return 0;
    }

    $show_help             = $opts{h};
    $show_version          = $opts{v};
    $debug_config          = $opts{d};
#   $run_interactively     = $opts{i};
#   $reflect_log_to_stdout = $opts{o};

    $output_pathname             = $opts{f};
    $capture_status_for_hosts    = $opts{H};
    $capture_status_for_services = $opts{S};

    # This test is not a full enforcement of intended exclusivity of the major
    # mode options, but it at least requires that you specify either -d or
    # -H and/or -S, if neither -h nor -v is specified.
    if (   !$show_version
	&& !$show_help
	&& !$debug_config
	&& !( $capture_status_for_hosts || $capture_status_for_services ) )
    {
	print_usage();
	return 0;
    }

    if ( !$run_interactively && $reflect_log_to_stdout ) {
	print_usage();
	return 0;
    }

    return 1;
}

sub log_to_foundation {
    my $severity   = shift;
    my $message    = shift;
    my $foundation = undef;
    local $_;

    # If $foundation_host is defined as an empty string in the config file,
    # we intentionally drop this message on the floor.
    if ( $foundation_host ne '' ) {
	eval {
	    $foundation = GW::Foundation->new( $foundation_host, $foundation_port, $monitor_server_hostname, $monitor_server_ip_address,
		$socket_send_timeout, $send_buffer_size, $debug_basic );
	};
	if ($@) {
	    chomp $@;
	    log_timed_message $@ if log_is_open();
	}

	# We adapt automatically to the absence (GWMEE 6.7.0) or presence (in later releases)
	# of the GW::Foundation::APP_ARCHIVE definition.
	my $app_archive;
	eval { $app_archive = GW::Foundation::APP_ARCHIVE(); };
	if ($@) {
	    if ( $@ =~ /^Undefined subroutine &GW::Foundation::APP_ARCHIVE called/ ) {
		## This possibility is allowed for, as APP_ARCHIVE was not defined in GW::Foundation
		## until the GWMEE 7.0.0 release.  We are assuming that this value was added to the
		## gwcollagedb.applicationtype table when this scripting was installed, though.
		$app_archive = 'ARCHIVE';
	    }
	    else {
		## Something more fundamental is afoot.  We have to punt.
		log_timed_message "ERROR:  Cannot call GW::Foundation::APP_ARCHIVE()." if log_is_open();
		$app_archive = GW::Foundation::APP_SYSTEM();
	    }
	}

	if ( defined $foundation ) {
	    my $errors = $foundation->send_log_message( $severity, $app_archive, $message );
	    map { log_timed_message $_ } @$errors if defined($errors) and log_is_open();
	}
    }
}

sub send_outcome_to_foundation {
    my $status_message = shift;
    my $outcome        = shift;

    # The speed numbers are slightly misleading, in that they also account for time taken for rows in other tables.
    # But this is good enough for reporting purposes.
    my $statistics =
	"captured $host_rows_captured host rows, $service_rows_captured service rows at $row_capture_speed total rows/sec;"
      . " $total_timestamp total run time";

    # We generate a Foundation log message to bring the success or failure of regular archiving
    # to the attention of the system operators.  We make this SEVERITY_WARNING in case of failure,
    # not SEVERITY_CRITICAL, because there should be no data loss involved; the same data will be
    # processed on the next run, generally along with additional new data.
    if ($outcome) {
	## We don't bother cluttering the message with a reference to the log file in the case of
	## success, as it shouldn't ordinarily be necessary to look there if everything went okay.
	log_to_foundation( SEVERITY_OK, $status_message eq '' ? "\u$statistics." : "\u$status_message; $statistics." );
    }
    else {
	## For the short form of this log reference to be valid, we need our standard symlink
	## to be in place.  But partly because this script is likely to be run on older GWMEE
	## releases that we know do not contain it, we must test for such a symlink.
	## FIX LATER:  We could further check that the symlink actually points to where we expect it to,
	## even if that file does not exist (though it certainly ought to, by the time we get here).
	( my $log_file_only = $logfile ) =~ s{.*/}{};
	my $log_details =
	  -l "/usr/local/groundwork/logs/$log_file_only" ? " See logs/$log_file_only for details." : " See $logfile for details.";
	log_to_foundation( SEVERITY_WARNING, $status_message eq '' ? "\u$statistics." : "\u$status_message; $statistics.$log_details" );
    }
}

# There are no changes we make to the databases in this capture-old-status-markers.pl
# script, so there is no interesting transaction behavior we need to control explicitly.
# Therefore, we enable auto-commit on this connection, to keep our application code
# simple.  Note that if a PostgreSQL command fails under auto-commit mode, it will be
# automatically rolled back; the application does not need to take any action to make
# this happen.  (Under PostgreSQL, all changes made so far in the transaction are rolled
# back, any additional commands in the transaction are aborted as soon as the command is
# run, before they have a chance to make any changes, and the COMMIT or END that ends
# the transaction is automatically turned into a ROLLBACK; the application has no choice
# about this.  That behavior is not necessarily the case with other commercial databases,
# so this issue would need to be investigated if we ever wanted to port this code to some
# other database.)
#
sub open_database_connection {
    my $dbtype = shift;
    my $dbhost = shift;
    my $dbport = shift;
    my $dbname = shift;
    my $dbuser = shift;
    my $dbpass = shift;
    my $dbhref = shift;
    my $dbh;

    local %ENV = %ENV;
    delete $ENV{PGCLIENTENCODING};
    delete $ENV{PGDATABASE};
    delete $ENV{PGDATESTYLE};
    delete $ENV{PGGEQO};
    delete $ENV{PGHOSTADDR};
    delete $ENV{PGHOST};
    delete $ENV{PGLOCALEDIR};
    delete $ENV{PGOPTIONS};
    delete $ENV{PGPASSFILE};
    delete $ENV{PGPASSWORD};
    delete $ENV{PGPORT};
    delete $ENV{PGSERVICEFILE};
    delete $ENV{PGSERVICE};
    delete $ENV{PGSYSCONFDIR};
    delete $ENV{PGTZ};
    delete $ENV{PGUSER};
    $ENV{PGCONNECT_TIMEOUT} = 20;
    $ENV{PGREQUIREPEER} = 'postgres';
    $ENV{PATH} = '/bin:/sbin:/usr/bin:/usr/sbin';

    my $dsn = '';
    if ( defined($dbtype) && $dbtype eq 'postgresql' ) {
	$dsn = "DBI:Pg:dbname=$dbname;host=$dbhost";
    }
    else {
	$dsn = "DBI:mysql:database=$dbname;host=$dbhost";
    }
    ## We don't specify RaiseError and/or PrintError here because we
    ## perform all error checking and logging directly in this script.
    $dbh = DBI->connect( $dsn, $dbuser, $dbpass, { 'AutoCommit' => 1, 'PrintError' => 0 } );
    if (!$dbh) {
	log_timed_message "ERROR:  Cannot connect to database $dbname: ", $DBI::errstr;
	return 0;
    }

    # All of the data-capture actions in this script don't need the ability to modify the
    # database, so we globally disable that capability so we are forcibly notified if we
    # violate our own rule.
    if (not defined $dbh->do("set session characteristics as transaction read only")) {
	log_timed_message "ERROR:  Cannot set session transaction access mode to read-only.";
	log_timed_message "        Database error is: ", $dbh->errstr;
	return 0;
    }

    $$dbhref = $dbh;
    return 1;
}

sub close_database_connection {
    my $dbhref = shift;

    my $outcome = 1;
    $$dbhref->disconnect() if $$dbhref;
    $$dbhref = undef;
    return $outcome;
}

sub get_value_from_user {
    my $default     = shift;
    my $description = shift;
    my $is_password = shift;
    my $is_question = shift;
    my $is_yes_no   = shift;

    # Yield the processor very briefly, to allow the Perl i/o layer (perhaps)
    # and the operating system and/or hypervisor a moment to take over and get
    # any buffered output actually sent to the receiving terminal, pseudo-terminal,
    # or socket.  Without this, we can sometimes get the prompt we're about to
    # produce be printed before output to STDOUT that was already queued up before
    # we got to this point.  We have only seen that to be an issue on a VM guest,
    # not on a bare-metal machine, but it was fairly reproducible there.  If we
    # ever see that behaviour again, perhaps the best solution will be to extend
    # this brief suspension to allow more time for all the queued output to appear.
    select undef, undef, undef, 0.01; # sleeps for 0.01 of a second

    my $ALLOW_STDIN   = 0;
    my $entered_value = '';
    my $repeat_value  = '';
    my $prompt_prefix = $is_question ? '' : $is_password ? '   Enter the ' : 'Enter the ';

    # Let's open the TTYIN (rather than STDIN) if we can.
    local(*TTYIN, *TTYOUT);
    my($in, $out) = Term::ReadLine->findConsole;
    die "No console available" unless $in;

    if (open TTYIN, "+<$in") {
	# Cool
    } elsif ($ALLOW_STDIN) {
	open TTYIN, "<&STDIN"
	    or die "Can't re-open STDIN: $!";
    } else {
	die "Can't open '$in' read/write: $!";
    }

    # And let's send the output to the TTY as well
    if (open TTYOUT, ">>$out") {
	# Cool
    } elsif ($ALLOW_STDIN) {
	# Well, let's allow STDOUT as well
	open TTYOUT, ">>&STDOUT"
	    or die "Can't re-open STDOUT: $!";
    } else {
	die "Can't open '$out' for output: $!";
    }

    # Don't buffer it!
    select( (select(TTYOUT), $|=1)[0] );

    while (1) {
	if ($is_password) {
	    # We apply a 60-second timeout between password characters mostly so that,
	    # if the user takes a very long time to type in the password, there is less
	    # chance that our earlier test to see whether the system was running is now
	    # no longer valid.
	    $entered_value = Term::ReadPassword::read_password("$prompt_prefix$description: ", 60, 1);
	    last if not defined $entered_value;
	}
	else {
	    print TTYOUT "$prompt_prefix$description"
	      . (
		$is_yes_no
		? (
		    ' ['
		      . (
			!defined($default)
			? 'y/n'
			: (   ( $default =~ /^[Yy]$/ || $default =~ /^\d+$/ && $default != 0 ) ? 'Y/n'
			    : ( $default =~ /^[Nn]$/ || $default =~ /^\d+$/ && $default == 0 ) ? 'y/N'
			    :                                                                    'y/n' )
		      )
		      . ']: '
		  )
		: defined($default) ? " [$default]: "
		: $is_question      ? ' '
		: ': '
	      );
	}

	if ($is_password) {
	    $repeat_value = Term::ReadPassword::read_password("Re-enter the $description: ", 60, 1);
	    if (not defined $repeat_value) {
		$entered_value = undef;
		last;
	    }
	    last if $repeat_value eq $entered_value;
	    print TTYOUT "ERROR:  Password mismatch.  Please try again.\n";
	}
	else {
	    $entered_value = readline TTYIN;
	    if ( defined $entered_value ) {
		chomp $entered_value;
		$entered_value =~ s/^\s+|\s+$//g;
		$entered_value = $default if $entered_value eq '';
		if ($is_yes_no) {
		    ## Verify the input and normalize the returned value.
		    if    ( $entered_value =~ /^[Yy]$/ ) { $entered_value = 1; last; }
		    elsif ( $entered_value =~ /^[Nn]$/ ) { $entered_value = 0; last; }
		}
		else {
		    last;
		}
	    }
	    print TTYOUT "\nInvalid input.  Please try again.\n";
	}
    }

    close(TTYIN);
    close(TTYOUT);
    return $entered_value;
}

sub determine_purge_end_timestamp {
    my $data_cutoff_timestamp_ref = shift;
    my $outcome                   = 1;

    # The purpose of this routine is to probe the database(s), look at configuration and state
    # files, and do whatever else is needed to determine candidate timestamps for the end of
    # the period for which data has been deleted from the gwcollagedb database.  Once you have
    # some candidate timestamps, present them to the user and ask for confirmation of one of
    # the calculated values.  Set that value to be used for subsequent processing, as the value
    # of the dereferenced $data_cutoff_timestamp_ref variable.

    # Configuration data obtainable from archiving configuration files:
    #
    # log-archive-send.conf:
    # Typical settings are shown here for quick apperception.  Actual settings should
    # be drawn from the config file, though of course those values are not guaranteed
    # to be completely stable, since the site can edit the config file and change them.
    #     operationally_useful_days_for_messages = 92
    #     post_archiving_retention_days_for_messages = 2

    # Initial data obtainable from archiving state files:
    #
    #
    # log-archive-send.state:
    #
    # Each value listed represents one of the most recent successful runs
    # of the archiving scripts.  The value must be of the form:
    #
    #     "run_start_timestamp => data_start_timestamp .. data_end_timestamp"
    #
    # By convention, the rows are sorted by increasing run_start_timestamp,
    # to make it easier to read this file, but that is not strictly necessary
    # and should not be counted on.
    #
    # The specific values listed in those lines are:
    #
    # run_start_timestamp:
    #     A timestamp captured when this cycle of the send script began operation.
    #
    # data_start_timestamp, data_end_timestamp:
    #     The data-capture limits used during this archiving run.  By convention,
    #     the start and end time-of-day is always 00:00:00 (midnight), but the
    #     stored format allows for more-general timestamp specification.  The
    #     selected data archived in this run obeys the mathematical relationship:
    #
    #         data_start_timestamp <= data_timestamp < data_end_timestamp
    #
    # There may be more than one of these rows in the file.
    #
    # successful_archiving_run = "2000-01-02 00:30:00 => 2000-01-01 00:00:00 .. 2000-01-02 00:00:00"
    #
    # To get a sense of how the end limit is used, delete_old_rows_from_table()
    # in the log-archive-send.pl script deletes with the clause:
    #
    #     "where \"$time_column\" < '$delete_end_timestamp'".
    #
    # $delete_end_timestamp is effectively calculated as the following,
    # ignoring format handling in that script:
    #
    #     $delete_end_timestamp = $archive_end_time - $operationally_useful_days_for_messages;
    #     ## Before we go looking to see if we need to keep data around for some
    #     ## post_archiving_retention_days after some previous archiving runs, we may
    #     ## as well recognize that we will need to keep around all the data we are
    #     ## about to archive for at least that long.  So we make the same comparisons
    #     ## we're about to make for prior runs, but applied first to the current run.
    #     if ( $delete_end_timestamp > $archive_start_time ) {
    #         $delete_end_timestamp = $archive_start_time;
    #     }
    #     foreach my $run_timestamp ( keys %data_start_time ) {
    #         my $message_retention_end_time = $run_timestamp + $post_archiving_retention_days_for_messages;
    #         if ($message_retention_end_time >= $archive_end_time) {
    #             ## We're still in the post-archive retention period for messages, for this run.
    #             ## Make sure we extend the delete end time farther back to accommodate (not delete)
    #             ## all data archived during this run, if necessary.
    #             if ($delete_end_timestamp > $data_start_time{$run_timestamp}) {
    #                 $delete_end_timestamp = $data_start_time{$run_timestamp};
    #             }
    #         }
    #     }
    #
    #
    # log-archive-receive.state:
    #
    # The data timestamp of the last successful run of log-archive-receive.pl,
    # expressed in the local timezone of the target script machine.  This value
    # only serves to validate monotonicity of successive injection runs, and not
    # to define or constrain the injected data.  So it is only marginally useful
    # for our purposes here.
    #
    # last_previous_successful_run_timestamp = "2000-01-01 00:00:00"

    my ( $second, $minute, $hour, $month_day, $month, $year, $week_day, $year_day, $is_dst ) = localtime;
    my $current_time = sprintf "%04d-%02d-%02d %02d:%02d:%02d", $year + 1900, $month + 1, $month_day, $hour, $minute, $second;
    my %candidate_cutoffs = ();
    $candidate_cutoffs{$current_time} = 'the current time';

    # ================================================================
    # FIX MAJOR:  Derive the %candidate_cutoffs from information found in databases,
    # configuration files, and state files.  The following entries are just used for
    # script debugging.
    # $candidate_cutoffs{'2016-06-14 00:00:00'} = 'derived from the archiving state files';
    # $candidate_cutoffs{'2016-05-17 00:00:00'} = 'derived from the archiving state files';
    # $candidate_cutoffs{'2016-08-08 00:00:00'} = 'derived from the gwcollagedb database';
    # $candidate_cutoffs{'2016-07-11 00:00:00'} = 'derived from the archive_gwcollagedb database';
    # ================================================================

    # The earliest reportdate value in the runtime logmessage table is a strong indicator
    # of the cutoff time that was used the last time that data was purged from that table,
    # presuming that it was the reportdate field that was used for the purging and not the
    # firstinsertdate or lastinsertdate field.  We are careful to round that timestamp up to
    # the next second, so as not to miss any data if we use this timestamp for data retrieval.
    my ($min_runtime_reportdate) =
      $runtime_dbh->selectrow_array("select date_trunc('second',min(reportdate)) + interval '1 second' as start_cliff from logmessage");
    if ( defined $min_runtime_reportdate ) {
	$candidate_cutoffs{$min_runtime_reportdate} = 'earliest logmessage reportdate in the runtime database';
    }
    elsif ( defined $runtime_dbh->err ) {
	## We only print an error message if there was actually an error.
	## Having an empty runtime logmessage table generates an undefined
	## $min_runtime_reportdate, but that is not an error.
	log_timed_message( ( $runtime_dbh->err ? 'ERROR' : 'WARNING' ) . ":  Cannot read the runtime database:\n" . $runtime_dbh->errstr );
    }

    my @candidate_cutoff_timestamps = sort keys %candidate_cutoffs;

    my $cutoff_selection = undef;
    while ( $outcome and not defined $cutoff_selection ) {
	print "\n";
	print "Your job here is to choose a point in time as of which the state of all\n";
	print "hosts and/or services, as recorded in the archive database, is to be\n";
	print "captured, if available.\n";
	print "\n";
	print "The following candidate cutoffs were found from analyzing existing data.\n";
	print "Select one of them by typing the number printed before the desired selection,\n";
	print "\"q\" to quit now, or just press Enter or Return to refresh this list.\n";
	print "\n";
	my $index = 0;

	printf "%6d:  %s  (%s)\n", $index, '????-??-?? ??:??:??', 'a time of your own choosing';
	foreach my $cutoff_time (@candidate_cutoff_timestamps) {
	    printf "%6d:  %s  (%s)\n", ++$index, $cutoff_time, $candidate_cutoffs{$cutoff_time};
	}
	print "\n";

	# Read one line from the terminal here, and interpret it.
	my $input_line = undef;
	$input_line = get_value_from_user( $input_line, "Your choice? [0.." . ( scalar @candidate_cutoff_timestamps ) . " or q]:", 0, 1 );
	$input_line = '' if not defined $input_line;
	my $answer = -1;
	if ($input_line =~ /^\s*(\d+)\s*$/) {
	    $answer = $1;
	}
	if ( $answer == 0 ) {
	    print "\n";
	    my $arbitrary_cutoff_timestamp = undef;
	    $arbitrary_cutoff_timestamp =
	      get_value_from_user( $arbitrary_cutoff_timestamp, 'desired timestamp in the form "YYYY-MM-DD hh:mm:ss"' );
	    $arbitrary_cutoff_timestamp = '' if not defined $arbitrary_cutoff_timestamp;
	    if ( $arbitrary_cutoff_timestamp =~ /^\s*(\d{4})-(\d{2})-(\d{2})\s+(\d{2}):(\d{2}):(\d{2})\s*$/ ) {
		## Validate the specified time-component values to see whether taken together, they represent
		## a valid point in time (in general, not specific to interpretation of DST and such).
		my ( $Year, $Month, $Day, $Hour, $Minute, $Second ) = ( $1, $2, $3, $4, $5, $6 );
		my ( $s, $m, $h, $D, $M, $current_year ) = localtime;
		$current_year += 1900;
		my $first_allowed_year = 2000;
		print "\n";
		eval {
		    ## Set reasonable limits on acceptable values.

		    # In our usage here, timelocal() mostly checks the non-year time components for sane ranges.
		    # Also note that timelocal() has special interpretation of years in the range of 0 through 999,
		    # so we need to check that component separately.
		    #
		    timelocal( $Second, $Minute, $Hour, $Day, $Month - 1, $Year );

		    # The doc for Time::Local says:
		    #
		    #     Both "timelocal()" and "timegm()" croak if given dates outside the supported range.
		    #
		    # But on a machine with a 64-bit time_t, or with Perl 5.12.0 or later which uses its own
		    # internal timestamp type, "supported range" does not limit you to the range of years from
		    # 1970 through 2038.  So both because of that and because of timelocal()'s special interpretation
		    # of small values for the year component, we impose our own additional checking here.
		    #
		    die "year range\n" if $Year < $first_allowed_year || $Year > $current_year;
		};
		if ($@) {
		    chomp $@;
		    $arbitrary_cutoff_timestamp =~ s/\s+/ /g;
		    if ( $@ eq 'year range' ) {
			print "The year in \"$arbitrary_cutoff_timestamp\" is out of range (it must be\n"
			  . "between $first_allowed_year and $current_year, inclusive).  Check your typing.\n";
		    }
		    else {
			print "\"$arbitrary_cutoff_timestamp\" does not represent an actual time; check your typing.\n";
		    }
		}
		else {
		    $cutoff_selection           = $answer;
		    $$data_cutoff_timestamp_ref = "$1-$2-$3 $4:$5:$6";
		}
	    }
	    else {
		print "\n";
		print "Your answer is not formatted correctly.  Please try again.\n";
	    }
	}
	elsif ( $answer >= 1 && $answer <= @candidate_cutoff_timestamps ) {
	    $cutoff_selection           = $answer;
	    $$data_cutoff_timestamp_ref = $candidate_cutoff_timestamps[ $answer - 1 ];
	}
	elsif ( $input_line =~ /^\s*(exit|quit|q)\s*$/i ) {
	    $outcome = 0;
	}
	elsif ( $input_line !~ /^\s*$/ ) {
	    print "\n";
	    print "Your answer is out of range.  Please try again.\n";
	}
	print "\n";
	if ( defined $cutoff_selection ) {
	    ## confirm [y/N] or repeat
	    my $confirmed = 'N';
	    $confirmed =
	      get_value_from_user( $confirmed,
		"You have chosen \"$$data_cutoff_timestamp_ref\" as the cutoff time;\n  is this correct?",
		0, 1, 1 );
	    if ( not $confirmed ) {
		$cutoff_selection = undef;
		print "\n";
	    }
	}
    }
    if ($outcome) {
	print "\n";
	print "The selected cutoff time is:  $$data_cutoff_timestamp_ref\n";
	print "\n";
	print "\n";

	log_timed_message "NOTICE:  $$data_cutoff_timestamp_ref has been confirmed";
	log_timed_message "         for use as the data-purge end timestamp.";
	log_timed_message "         This means that we will capture status data for";
	log_timed_message "         "
	  . ( ( $capture_status_for_hosts ? 'hosts' : '' )
	    . ( $capture_status_for_hosts && $capture_status_for_services ? ' and ' : '' )
	      . ( $capture_status_for_services ? 'services' : '' ) )
	  . " as of that point in time. ";
    }
    else {
	log_timed_message "NOTICE:  No data-purge end timestamp has been chosen.";
    }

    return $outcome;
}

sub open_output_file {
    my $output_file       = shift;
    my $output_handle_ref = shift;
    my $outcome           = 1;

    # Here we use the client connection as a transport from the database to the file, both because we're not
    # assuming database superuser privileges (needed for the server to write directly to a file), and because
    # the database might be located on a remote server, which would not have access to our local filesystem.
    #
    # Note that we depend on the caller to have already created any ancestor directories needed here.
    #
    if ( not sysopen( $$output_handle_ref, $output_file, O_WRONLY | O_APPEND | O_CREAT | O_EXCL, 0600 ) ) {
	log_timed_message "ERROR:  Cannot open file \"$output_file\" ($!); aborting all data capture!";
	$outcome = 0;
    }

    return $outcome;
}

sub close_output_file {
    my $output_file       = shift;
    my $output_handle_ref = shift;
    my $outcome           = 1;

    if ( not close $$output_handle_ref ) {
	log_timed_message "ERROR:  Cannot close file \"$output_file\" ($!); aborting all data capture!";
	$outcome = 0;
    }
    else {
	$$output_handle_ref = undef;
    }

    return $outcome;
}

sub capture_query_rows {
    my $object_type       = shift;
    my $query             = shift;
    my $output_file       = shift;
    my $output_handle     = shift;
    my $captured_rows_ref = shift;
    my $outcome           = 1;

    my $dump_is_good  = 1;
    my $captured_rows = 0;
    if ( not defined $archive_dbh->do("COPY ($query) TO STDOUT") ) {
	log_timed_message "ERROR:  Cannot put the archive database connection into COPY OUT mode; aborting $object_type data capture!";
	log_timed_message "        Database error is: ", $archive_dbh->errstr;
	$dump_is_good = 0;
    }
    else {
	my @data = ();
	$#data = $dump_copy_block_rows;    # pre-extend the array, for efficiency
	$#data = -1;                       # truncate the array, since we don't have any data yet
	my $x    = 0;
	my $size = 0;
	while (1) {
	    ## NOTE:  The DBD::Pg 2.19.3 documentation (http://search.cpan.org/~turnstep/DBD-Pg/Pg.pm#pg_getcopydata)
	    ## example of how to fill @data is wrong; it violates the condition expressed just above the example, that
	    ## the variable cannot be undefined.  This means that you have to leave the array pre-extended, with defined
	    ## values, and then you cannot use the array size to figure out how much data you have; you must use the
	    ## incremented index to extract an appropriate range of values from the array.  Or you can do what we do
	    ## here, pass a reference to the variable to be populated; testing shows that this works.  This should be
	    ## reported to the upstream maintainers.
	    if ( ( $size = $archive_dbh->pg_getcopydata( \$data[ $x++ ] ) ) >= 0 ) {
		++$captured_rows;
	    }
	    if ( $size < 0 || $x >= $dump_copy_block_rows ) {
		## The DBD::Pg documentation says that, once the server is in COPY TO mode, no other SQL
		## commands are allowed until the final pg_getcopydata() has been called to retrieve the
		## last of the data.  This provides no way to abort the copying over the client/server
		## connection in the middle, in a situation like this where we get a file-writing error.
		## We ought to report this problem upstream, and see if we can get the package fixed to
		## provide some sort of data-transfer interrupt capability.  In the meantime, our error
		## handling here has to allow the data fetching to continue; we just turn off writing to
		## the file, to speed up getting out of this mess.
		if ( @data and $dump_is_good and $output_file and not print $output_handle @data ) {
		    log_timed_message "ERROR:  Cannot write to file \"$output_file\" ($!); aborting $object_type data capture!";
		    $dump_is_good = 0;
		}
		last if $size < 0;
		@data = ();
		$x    = 0;
	    }
	}
    }
    if ( $output_file and not flush $output_handle ) {
	log_timed_message "ERROR:  Cannot flush file \"$output_file\" ($!); aborting $object_type data capture!";
	$dump_is_good = 0;
    }

    if ($dump_is_good) {
	## We only save the statistics if the dump is good.  That means that if this table capture failed,
	## the end-of-run statistics won't reflect the time spent on the table up to the point of failure,
	## which could be a bit misleading with regard to the $row_capture_speed.  We might address that in
	## a future release, when we might perhaps track the dump times on a per-table basis.  Until then,
	## this decision seems acceptable.
	$$captured_rows_ref += $captured_rows;
	$output_unlinked = 0;
    }
    else {
	## We generally delete the dump file if the dump is not good, to prevent anyone
	## confusing it with a complete and useable file.  We only keep a bad dump file
	## if $debug_maximal is true, indicating that we know what we're doing.  This
	## condition will be flagged in the summary data at the end of the program.

	# We were able to open the file, so we're going to presume here that we have
	# permission to clean up by removing it.  A downside is that we're removing
	# the forensic evidence.  If we run into problems like this in the field, the
	# debug_level can be turned up all the way in the config file for as long as
	# you're attempting to run this script.  In that case, it will be up to the user
	# to remove the bad file after problem diagnosis, to avoid later confusion.
	if ( $output_file and not $debug_maximal ) {
	    $output_unlinked = unlink $output_file;
	}
	$outcome = 0;
    }

    return $outcome;
}

sub capture_host_status_markers {
    my $data_cutoff_timestamp = shift;
    my $output_file           = shift;
    my $output_handle         = shift;

    $host_rows_captured = 0;

    # We check the d.endvalidtime and h.endvalidtime fields to filter out status for devices and
    # hosts that we can easily see have already been deleted from the upstream gwcollagedb database.
    # There's no point in resurrecting status for such hosts, as it would cause disruption when such
    # rows are inserted back into the logmessage table with unsatisfied foreign-key references.  The
    # later scripting that inserts the rows is aware that there might be large-scale race conditions
    # in which the archiving is run, then some devices and/or hosts are deleted, and then this
    # script is run, so the filtering we do here does not guarantee that we will not include any
    # inappropriate rows (and in so doing, possibly suppress rows that we should have included)
    # in our output.  And that possibility affects the mechanism that must be used to stuff such
    # rows back into the gwcollagedb database -- it must be prepared to see unsatisfied foreign key
    # references, and ignore such rows.  But still, it's a good idea to minimize possible trouble.
    #
    my $query = "
	SELECT DISTINCT ON (l.hoststatusid)
	    l.*
	FROM
	    logmessage l
	    LEFT JOIN device          d ON (d.deviceid          = l.deviceid)
	    LEFT JOIN host            h ON (h.hostid            = l.hoststatusid)
	    LEFT JOIN monitorstatus   m ON (m.monitorstatusid   = l.monitorstatusid)
	    LEFT JOIN applicationtype a ON (a.applicationtypeid = l.applicationtypeid)
	WHERE
	    l.reportdate <= '$data_cutoff_timestamp'
	AND l.hoststatusid    IS NOT NULL
	AND l.servicestatusid IS     NULL
	AND d.endvalidtime    IS     NULL
	AND h.endvalidtime    IS     NULL
	AND m.endvalidtime    IS     NULL
	AND a.name != 'SYSTEM'
	ORDER BY l.hoststatusid, l.reportdate DESC
    ";

    return capture_query_rows( 'host', $query, $output_file, $output_handle, \$host_rows_captured );
}

sub capture_service_status_markers {
    my $data_cutoff_timestamp = shift;
    my $output_file           = shift;
    my $output_handle         = shift;

    $service_rows_captured = 0;

    # We check the d.endvalidtime, h.endvalidtime, and s.endvalidtime fields to filter out status
    # for devices and services that we can easily see have already been deleted from the upstream
    # gwcollagedb database.  There's no point in resurrecting status for such services, as it would
    # cause disruption when such rows are inserted back into the logmessage table with unsatisfied
    # foreign-key references.  The later scripting that inserts the rows is aware that there might
    # be large-scale race conditions in which the archiving is run, then some devices and/or hosts
    # and/or services are deleted, and then this script is run, so the filtering we do here does not
    # guarantee that we will not include any inappropriate rows (and in so doing, possibly suppress
    # rows that we should have included) in our output.  And that possibility affects the mechanism
    # that must be used to stuff such rows back into the gwcollagedb database -- it must be prepared
    # to see unsatisfied foreign key references, and ignore such rows.  But still, it's a good idea
    # to minimize possible trouble.
    #
    my $query = "
	SELECT DISTINCT ON (l.hoststatusid, l.servicestatusid)
	    l.*
	FROM
	    logmessage l
	    LEFT JOIN device          d ON (d.deviceid          = l.deviceid)
	    LEFT JOIN host            h ON (h.hostid            = l.hoststatusid)
	    LEFT JOIN servicestatus   s ON (s.servicestatusid   = l.servicestatusid)
	    LEFT JOIN monitorstatus   m ON (m.monitorstatusid   = l.monitorstatusid)
	    LEFT JOIN applicationtype a ON (a.applicationtypeid = l.applicationtypeid)
	WHERE
	    l.reportdate <= '$data_cutoff_timestamp'
	AND l.hoststatusid    IS NOT NULL
	AND l.servicestatusid IS NOT NULL
	AND d.endvalidtime    IS     NULL
	AND h.endvalidtime    IS     NULL
	AND s.endvalidtime    IS     NULL
	AND m.endvalidtime    IS     NULL
	AND a.name != 'SYSTEM'
	ORDER BY l.hoststatusid, l.servicestatusid, l.reportdate DESC
    ";

    return capture_query_rows( 'service', $query, $output_file, $output_handle, \$service_rows_captured );
}

sub capture_timing {
    my $timestamp = shift;
    $$timestamp = Time::HiRes::time();
}

sub format_hhmmss_timestamp {
    ## Taking the floor() of the value understates the actual time interval, but it looks
    ## too strange taking the ceil() instead, which overstates the time interval.  In either
    ## case, you are likely to end up with a bunch of values that don't add up to the total
    ## interval, but use of floor() seems more natural.
    my $interval = int(POSIX::floor(shift));
    my $seconds  =     $interval % SECONDS_PER_MINUTE;
    my $minutes  = int($interval / SECONDS_PER_MINUTE) % MINUTES_PER_HOUR;
    my $hours    = int($interval / SECONDS_PER_HOUR);
    return sprintf "%02d:%02d:%02d", $hours, $minutes, $seconds;
}

sub log_action_statistics {
    my $status_message = shift;
    my $suffix         = $status_message eq '' ? '' : " ($status_message)";
    my $process_status = $process_outcome ? 'SUCCEEDED' : 'FAILED';
    my ( $second, $minute, $hour, $month_day, $month, $year, $week_day, $year_day, $is_dst );

    # Sleep for one full second, to move past the $script_end_timestamp computed below by
    # rounding up a high-resolution time via POSIX::ceil().  This will guarantee that any
    # subsequent log_timed_message() output that appears after our "script ended at" message
    # will have a timestamp no earlier than the timestamp specified in that message.
    select undef, undef, undef, 1.0;

    ( $second, $minute, $hour, $month_day, $month, $year, $week_day, $year_day, $is_dst ) = localtime(POSIX::floor($script_start_time));
    my $script_start_timestamp = sprintf "%04d-%02d-%02d %02d:%02d:%02d", $year + 1900, $month + 1, $month_day, $hour, $minute, $second;
    ( $second, $minute, $hour, $month_day, $month, $year, $week_day, $year_day, $is_dst ) = localtime(POSIX::ceil($script_service_processing_end_time));
    my $script_end_timestamp = sprintf "%04d-%02d-%02d %02d:%02d:%02d", $year + 1900, $month + 1, $month_day, $hour, $minute, $second;

    $init_time         = $script_init_end_time - $script_start_time;
    $setup_time        = $script_setup_end_time - $script_init_end_time;
    $host_time         = $script_host_processing_end_time - $script_setup_end_time;
    $service_time      = $script_service_processing_end_time - $script_host_processing_end_time;
    $total_time        = POSIX::ceil($script_service_processing_end_time - $script_start_time);

    $init_timestamp    = format_hhmmss_timestamp($init_time);
    $setup_timestamp   = format_hhmmss_timestamp($setup_time);
    $host_timestamp    = format_hhmmss_timestamp($host_time);
    $service_timestamp = format_hhmmss_timestamp($service_time);
    $total_timestamp   = format_hhmmss_timestamp($total_time);

    $total_rows_captured = $host_rows_captured + $service_rows_captured;
    $capture_data_time   = $host_time + $service_time;

    # All speed measurements are "per second".
    $row_capture_speed = $capture_data_time > 0 ? sprintf( "%12.3f", $total_rows_captured / $capture_data_time ) : 'indeterminate';

    log_timed_message "STATS:  Status capture statistics:";
    log_message "Status-capture script started at:  $script_start_timestamp";
    log_message "Status-capture script   ended at:  $script_end_timestamp";
    log_message         "$init_timestamp taken to initialize the status-capture script";
    log_message        "$setup_timestamp taken to determine the data-purge end timestamp";
    log_message         "$host_timestamp taken to run the host-status phase on the archive database";
    log_message      "$service_timestamp taken to run the service-status phase on the archive database";
    log_message        "$total_timestamp taken to run the entire status-capture operation";

    log_message sprintf( "%8d rows of host    status were captured from the archive database", $host_rows_captured );
    log_message sprintf( "%8d rows of service status were captured from the archive database", $service_rows_captured );
    log_message sprintf( "%8d total rows of   status were captured from the archive database", $total_rows_captured )
      if $host_rows_captured and $service_rows_captured;

    log_message  "$row_capture_speed rows captured per second";

    if ( not $output_pathname ) {
	log_message "This was a dry run; no captured data was written to an output file.";
    }
    elsif ($process_outcome) {
	log_timed_message "NOTICE:  Captured data was written to the output file \"$output_pathname\".";
    }
    elsif ($output_unlinked) {
	log_message "NOTICE:  To avoid confusion, the output file \"$output_pathname\"";
	log_message "         was removed because processing failed, and";
	log_message "         the data in that file was therefore not reliable.";
    }
    elsif ($debug_maximal) {
	log_timed_message "WARNING:  The output file \"$output_pathname\"";
	log_timed_message "          was kept because a high debug level was in play, but";
	log_timed_message "          the data in that file may not be complete or reliable.";
    }
    else {
	log_message "Due to previous errors, no captured data was written to an output file.";
    }

    log_timed_message "STATS:  This run of status capturing $process_status$suffix.";

    # Reformat certain speed measurements for later use in a status message sent to Foundation.
    $row_capture_speed = $capture_data_time > 0 ? sprintf( "%.1f", $total_rows_captured / $capture_data_time ) : 'indeterminate';
}

#!/usr/local/groundwork/perl/bin/perl -w --

# restore-old-status-markers.pl

# Copyright (c) 2016 GroundWork, Inc. (www.gwos.com).  All rights reserved.
# Use of this software is subject to commercial license terms.

# This script takes logmessage rows found by the capture-old-status-markers.pl
# script in the archive_gwcollagedb database, and inserts them into the same
# table in the gwcollagedb database.  Those rows reflect the old status of hosts
# and services, as they appeared just before a certain point in time.  That point
# in time is supposed to reflect the most-recent cutoff when sufficiently-old
# data was purged from the gwcollagedb database.  It is useful for repairing a
# damaged gwcollagedb database which no longer has at least one such row for
# every host and service.  Some caveats:
#
# (*) This script must be run on the machine where the log-archive-send.pl
#     script is normally run.
#
# (*) This script is normally only run on a one-time basis, to repair a
#     previously damaged gwcollagedb database.
#
# (*) This script is consumes the output from the capture-old-status-markers.pl
#     script, which must be run beforehand.  That pair of actions copies back
#     into the gwcollagedb database such markers as might still be available
#     in the archive_gwcollagedb database, reflecting data prior to some
#     data-deletion cutoff.  Afterward, the create-current-status-markers.pl
#     script, under control of the -m option (to create only "missing" data),
#     will just handle the residual cases where no historical data at all is
#     available.  See the full KB documentation for information on all of
#     these scripts.

use strict;
use warnings;

use DBI;
use Fcntl;
use Getopt::Std;
use Time::HiRes;
use Socket;
use POSIX qw();

use Data::Dumper;
$Data::Dumper::Indent   = 1;
$Data::Dumper::Sortkeys = 1;

use TypedConfig;

use GW::Logger;
use GW::Foundation;

# ================================
# Package Parameters
# ================================

my $PROGNAME       = "restore-old-status-markers.pl";
my $VERSION        = "0.0.7";
my $COPYRIGHT_YEAR = "2016";

my $send_config_file    = '/usr/local/groundwork/config/log-archive-send.conf';
my $receive_config_file = '/usr/local/groundwork/config/log-archive-receive.conf';
my $default_log_file    = '/usr/local/groundwork/foundation/container/logs/status-markers.log';

# ================================
# Command-Line Parameters
# ================================

# In theory, these parameter settings could be overridden by command-line arguments.
# In practice, we don't currently support any such arguments; this script uses only
# a fixed set of arguments.

my $debug_config          = 0;       # if set, spill out certain data about config-file processing to STDOUT
my $show_help             = 0;
my $show_version          = 0;
my $run_interactively     = 1;       # Default on in this program to force logging of all useful output.
my $reflect_log_to_stdout = 1;       # Default on in this program to force logging of all useful output.
my $roll_back_all_changes = undef;
my $input_pathname        = undef;

# ================================
# Configuration Parameters
# ================================

# Parameters in the config file.

# Possible $debug_level values:
# 0 = no info of any kind printed, except for startup/shutdown messages and major errors
# 1 = print just error info and summary statistical data
# 2 = also print basic debug info
# 3 = print detailed debug info
# Initial level, to be overwritten by a value from the config file:
my $debug_level = 1;

# We use the default log file which is hardcoded above, essentially just
# because all other configuration values we can draw from the config file
# which is shared with the log-archive-send.pl program, but it doesn't make
# sense to dump log output from this script into that script's log file.
my $logfile                = $default_log_file;
my $max_logfile_size       = undef;    # log rotate is handled externally, not here
my $max_logfiles_to_retain = undef;    # log rotate is handled externally, not here

my $runtime_dbtype = undef;
my $runtime_dbhost = undef;
my $runtime_dbport = undef;
my $runtime_dbname = undef;
my $runtime_dbuser = undef;
my $runtime_dbpass = undef;

my $source_script_machine    = undef;
my $source_script_ip_address = undef;

my @primary_table_attributes   = ();
my @secondary_table_attributes = ();
my @tertiary_table_attributes  = ();
my @primary_tables             = ();
my @secondary_tables           = ();
my @tertiary_tables            = ();
my %all_table_row_type         = ();
my %all_table_key_fields       = ();

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

# Parameters not in the config file, but that would be if we were not parasitizing
# a config file from the log-archive scripting, and instead had our own config file.

# Locally defined parameters, that might someday move to the config file.

# A variant of this value is set in the log-archive-receive.conf config file,
# but inasmuch as that value is to be used in the context of an automatically
# run background process, that setting has little relevance to our work here.
#
# How long to wait (in seconds) for a table lock on one of the runtime tables
# to become available, before aborting the entire restore run.  This provides
# some protection against a near-infinite wait for some other database client
# to release the lock, so the script can finish and formally report its failure.
# This timeout is unlikely to be exercised in practice unless some reporting
# client locks a table for a very long time.  Any data not restored in the
# current run can still be restored in a later run using the same input file,
# so there is no danger of data loss.
my $table_locking_timeout_seconds = 30;

# Not a candidate for direct setting in a config file, but derived in the code
# from the setting for table_locking_timeout_seconds.
my $table_locking_timeout_ms = undef;

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

my $dbh = undef;

my $script_start_time         = undef;
my $script_init_end_time      = undef;
my $script_injection_end_time = undef;

my %matched_file           = ();
my %matched_dump_timestamp = ();

my $total_tables_injected = 0;
my $total_rows_copied     = 0;
my $total_rows_ignored    = 0;
my $total_rows_inserted   = 0;
my $total_rows_injected   = 0;

my $total_copy_time    = 0;
my $total_lock_time    = 0;
my $total_compare_time = 0;
my $total_delete_time  = 0;
my $total_insert_time  = 0;

my %rows_copied   = ();
my %rows_ignored  = ();
my %rows_inserted = ();
my %rows_injected = ();

my %invalid_rows = ();

my $process_outcome = undef;

# These variables really ought to just be local to the log_action_statistics() routine,
# except that we want a few of them to be accessible to the send_outcome_to_foundation()
# routine so the message it sends is more informative.
my $init_time           = undef;
my $injection_time      = undef;
my $total_time          = undef;
my $init_timestamp      = undef;
my $injection_timestamp = undef;
my $copy_timestamp      = undef;
my $lock_timestamp      = undef;
my $compare_timestamp   = undef;
my $delete_timestamp    = undef;
my $insert_timestamp    = undef;
my $total_timestamp     = undef;
my $row_injection_speed = undef;
my $row_copy_speed      = undef;
my $row_insert_speed    = undef;

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
	log_timed_message "=== Status marker insertion script (version $VERSION) starting up (process $$). ===";
	log_timed_message "INFO:  Running with options:  " . join (' ', @SAVED_ARGV);
    }

    capture_timing(\$script_init_end_time);

    # Open a connection to the runtime database.
    if ($process_outcome) {
	log_timed_message "NOTICE:  Opening a connection to the runtime database.";
	$process_outcome = open_database_connection();
	$status_message = 'cannot connect to the runtime database' if not $process_outcome;
    }

    # Insert old rows into the runtime database.
    if ($process_outcome) {
	log_timed_message "NOTICE:  Inserting old status rows into the runtime database.";
	$process_outcome = insert_status_markers( $input_pathname, $roll_back_all_changes );
	$status_message = 'cannot insert status rows' if not $process_outcome;
    }

    capture_timing(\$script_injection_end_time);

    # Close the connection to the runtime database.  This is done unconditionally, since we should close
    # the connection even if errors occurred after it was opened.  The routine can internally handle the
    # case where the connection was never opened in the first place because of prior errors.  However,
    # it gets confusing if we log the occurrence of this call under circumstances when it won't actually
    # do anything, so we do qualify the logging here.
    log_timed_message "NOTICE:  Closing the connection to the runtime database." if $dbh and log_is_open();
    close_database_connection();

    log_action_statistics($status_message) if log_is_open();

    send_outcome_to_foundation( $status_message, $process_outcome ) if not $roll_back_all_changes;

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
        $PROGNAME [-n] -f output_file

where:  -h:  print this help message
        -v:  print the version number
        -d:  debug config file
        -n:  make no permanent changes; roll back instead of committing them
        -f input_file
             specifies where the status rows should be read from, in a format
             created by the capture-old-status-markers.pl script

The usual invocation is:

    $PROGNAME -f /tmp/logmessage_rows

The -f option is how you specify where the status rows will be read from.

The -n option is useful for dry-run testing, to avoid permanently
modifying the database so that repeated runs all start out with the
same setup.  It will run the expected queries and row insertions, then
roll them back out.  They will never be seen outside of this script's own
connection to the database.  This facility can help both with potential
code modifications and with performance testing on large databases.

EOF

# Usage lines not printed because we hardcode the -i and -o options in this program.
=pod
        $PROGNAME [-n] -f output_file [-c config_file] [-i] [-o]
        -i:  run interactively, not as a background process
        -o:  write log messages also to standard output
    $PROGNAME -i -o -f /tmp/logmessage_rows
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

	$debug_level = $config->get_number('debug_level');

	$debug_minimal = ( $debug_level >= 1 );
	$debug_basic   = ( $debug_level >= 2 );
	$debug_maximal = ( $debug_level >= 3 );

	$runtime_dbtype = $config->get_scalar('runtime_dbtype');
	$runtime_dbhost = $config->get_scalar('runtime_dbhost');
	$runtime_dbport = $config->get_number('runtime_dbport');
	$runtime_dbname = $config->get_scalar('runtime_dbname');
	$runtime_dbuser = $config->get_scalar('runtime_dbuser');
	$runtime_dbpass = $config->get_scalar('runtime_dbpass');

	$source_script_machine    = $config->get_scalar('source_script_machine');
	$source_script_ip_address = $config->get_scalar('source_script_ip_address');

	if ( !is_valid_hostname($source_script_machine) ) {
	    die "ERROR:  configured value for source_script_machine must be a valid hostname\n";
	}

	$monitor_server_hostname = $source_script_machine;
	if ( $monitor_server_hostname eq '' ) {
	    die "ERROR:  configured value for source_script_machine cannot be an empty string\n";
	}
	elsif ( $monitor_server_hostname eq 'localhost' ) {
	    $monitor_server_ip_address = $source_script_ip_address ne '' ? $source_script_ip_address : '127.0.0.1';
	}
	elsif ( $source_script_ip_address ne '' ) {
	    $monitor_server_ip_address = $source_script_ip_address;
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
	    ## this ambiguity by not defaulting the source_script_ip_address in the config file.
	    my $packed_ip_address = gethostbyname($monitor_server_hostname);
	    ## Check if we could resolve the hostname to an IP address.
	    if ( defined $packed_ip_address ) {
		$monitor_server_ip_address = inet_ntoa($packed_ip_address);
	    }
	    else {
		die "ERROR:  cannot resolve the IP address for source_script_machine \"$monitor_server_hostname\";"
		  . " you can avoid this by specifying a non-empty value for source_script_ip_address\n";
	    }
	}

	# This value is not set in the parasitized config file we are currently reading;
	# instead, we just use the value which is hardcoded in this script.
	# $table_locking_timeout_seconds = $config->get_number('table_locking_timeout_seconds');

	if ( $table_locking_timeout_seconds < 1 ) {
	    die "ERROR:  configured value for table_locking_timeout_seconds must be at least 1\n";
	}

	# Convert seconds to milliseconds for later use in an SQL statement.
	$table_locking_timeout_ms = $table_locking_timeout_seconds * 1000;

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

sub read_receive_config_file {
    my $config_file  = shift;
    my $config_debug = shift;

    # All the config-file processing is wrapped in an eval{}; because TypedConfig
    # throws exceptions when it cannot open the config file or finds bad config data.
    eval {
	my $config = TypedConfig->secure_new( $config_file, $config_debug );

        @primary_table_attributes = $config->get_array ('primary_table_attributes');
        print Data::Dumper->Dump([\@primary_table_attributes], [qw(\@primary_table_attributes)]) if $config_debug;

        @secondary_table_attributes = $config->get_array ('secondary_table_attributes');
        print Data::Dumper->Dump([\@secondary_table_attributes], [qw(\@secondary_table_attributes)]) if $config_debug;

        @tertiary_table_attributes = $config->get_array ('tertiary_table_attributes');
        print Data::Dumper->Dump([\@tertiary_table_attributes], [qw(\@tertiary_table_attributes)]) if $config_debug;

        foreach my $field_spec (@primary_table_attributes) {
            ## This is really only crude validation.  Full validation will wait until we try to use
            ## these values when accessing the database.
            if ( $field_spec =~ /^\s*([a-z_]+)\s+([a-z_]+)\s+([a-z_]+(?:,[a-z_]+)*)\s*$/ ) {
                my $table         = $1;
                my $row_type      = $2;
                my $key_fields    = $3;
                if ( $table =~ /^schemainfo$/i ) {
                    die "ERROR:  primary_table_attributes cannot specify the \"$table\" table\n";
                }
                push @primary_tables, $table;
                ## timed_association is not used for any primary tables in the present setup, so we may as well
                ## disallow it here to better validate the configuration.  We can put it back in this validation
                ## test in some future release, if it is ever actually needed for a primary table.
                if ( $row_type =~ /^(?:timed_object|untimed_detail)$/ ) {
                    $all_table_row_type{$table} = $row_type;
                }
                else {
                    die "ERROR:  configured value for primary_table_attributes (table \"$table\") has an invalid row_type (\"$row_type\")\n";
                }
                @{ $all_table_key_fields{$table} } = split( ',', $key_fields );
            }
            else {
                die "ERROR:  configured value for primary_table_attributes (\"$field_spec\") is invalid\n";
            }
        }

        foreach my $field_spec (@secondary_table_attributes) {
            ## This is really only crude validation.  Full validation will wait until we try to use
            ## these values when accessing the database.
            if ( $field_spec =~ /^\s*([a-z_]+)\s+([a-z_]+)\s+([a-z_]+(?:,[a-z_]+)*)\s*$/ ) {
                my $table         = $1;
                my $row_type      = $2;
                my $key_fields    = $3;
                if ( $table =~ /^schemainfo$/i ) {
                    die "ERROR:  secondary_table_attributes cannot specify the \"$table\" table\n";
                }
                push @secondary_tables, $table;
                if ( $row_type =~ /^(?:untimed_object)$/ ) {
                    $all_table_row_type{$table} = $row_type;
                }
                else {
                    die "ERROR:  configured value for secondary_table_attributes (table \"$table\") has an invalid row_type (\"$row_type\")\n";
                }
                @{ $all_table_key_fields{$table} } = split( ',', $key_fields );
            }
            else {
                die "ERROR:  configured value for secondary_table_attributes (\"$field_spec\") is invalid\n";
            }
        }

        foreach my $field_spec (@tertiary_table_attributes) {
            ## This is really only crude validation.  Full validation will wait until we try to use
            ## these values when accessing the database.
            if ( $field_spec =~ /^\s*([a-z_]+)\s+([a-z_]+)\s+([a-z_]+(?:,[a-z_]+)*)\s*$/ ) {
                my $table         = $1;
                my $row_type      = $2;
                my $key_fields    = $3;
                if ( $table =~ /^schemainfo$/i ) {
                    die "ERROR:  tertiary_table_attributes cannot specify the \"$table\" table\n";
                }
                push @tertiary_tables, $table;
                if ( $row_type =~ /^(?:timed_object|timed_association|untimed_detail)$/ ) {
                    $all_table_row_type{$table} = $row_type;
                }
                else {
                    die "ERROR:  configured value for tertiary_table_attributes (table \"$table\") has an invalid row_type (\"$row_type\")\n";
                }
                @{ $all_table_key_fields{$table} } = split( ',', $key_fields );
            }
            else {
                die "ERROR:  configured value for tertiary_table_attributes (\"$field_spec\") is invalid\n";
            }
        }

        # Here we reverse the order of the specified tables, because by design we specify these tables
        # in the receiving-script config file in the same order as we do in the sending-script config file
        # (that is, in the required dump order), to make it trivial to compare the two sets of lists.
        # The receiving script, of course, has to populate the tables in the opposite order that they were
        # dumped in, so all the referred-to rows are in place before the references to them are created.
        @primary_tables   = reverse @primary_tables;
        @secondary_tables = reverse @secondary_tables;
        @tertiary_tables  = reverse @tertiary_tables;
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
    if ( not getopts( 'hvdnf:', \%opts ) ) {
	print_usage();
	return 0;
    }

    $show_help             = $opts{h};
    $show_version          = $opts{v};
    $debug_config          = $opts{d};
#   $run_interactively     = $opts{i};
#   $reflect_log_to_stdout = $opts{o};

    $input_pathname        = $opts{f};
    $roll_back_all_changes = $opts{n};

    # This test is not a full enforcement of intended exclusivity of the major
    # mode options, but it at least requires that you specify either -d or
    # -f, if neither -h nor -v is specified.
    if ( !$show_version && !$show_help && !$debug_config && !$input_pathname ) {
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

    # The speed numbers are slightly misleading, in that they also account for time
    # taken for rows in other tables.  And total injection time, which includes copying
    # and comparing, is ignored.  But this is good enough for reporting purposes.
    my $statistics =
	"inserted $total_rows_inserted rows at $row_insert_speed total rows/sec;"
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

# The only changes we make to the database in this restore-old-status-markers.pl script are
# individual row insertions, so there is no interesting transaction behavior we need to control
# explicitly.  Therefore, we enable auto-commit on this connection, to keep our application
# code simple.  Note that if a PostgreSQL command fails under auto-commit mode, it will be
# automatically rolled back; the application does not need to take any action to make
# this happen.  (Under PostgreSQL, all changes made so far in the transaction are rolled
# back, any additional commands in the transaction are aborted as soon as the command is
# run, before they have a chance to make any changes, and the COMMIT or END that ends
# the transaction is automatically turned into a ROLLBACK; the application has no choice
# about this.  That behavior is not necessarily the case with other commercial databases,
# so this issue would need to be investigated if we ever wanted to port this code to some
# other database.)  So any failed insertions in this script will be turned into no-ops, which
# is fine; a future run of this script ought to successfully perform similar insertions,
# if the underlying problem gets resolved.
#
sub open_database_connection {
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
    if ( defined($runtime_dbtype) && $runtime_dbtype eq 'postgresql' ) {
	$dsn = "DBI:Pg:dbname=$runtime_dbname;host=$runtime_dbhost";
    }
    else {
	$dsn = "DBI:mysql:database=$runtime_dbname;host=$runtime_dbhost";
    }
    ## We don't specify RaiseError and/or PrintError here because we
    ## perform all error checking and logging directly in this script.
    $dbh = DBI->connect( $dsn, $runtime_dbuser, $runtime_dbpass, { 'AutoCommit' => 1 } );
    if (!$dbh) {
	log_timed_message "ERROR:  Cannot connect to database $runtime_dbname: ", $DBI::errstr;
	return 0;
    }

    # The purpose of this script is to insert new rows into the database, so we enable that capability.
    # (That should already be the ordinary default; this is somewhat redundant.)
    if (not defined $dbh->do("set session characteristics as transaction read write")) {
	log_timed_message "ERROR:  Cannot set session transaction access mode to read/write; no database insertions are possible.";
	log_timed_message "        Database error is: ", $dbh->errstr;
	return 0;
    }

    return 1;
}

sub close_database_connection {
    my $outcome = 1;
    $dbh->disconnect() if $dbh;
    $dbh = undef;
    return $outcome;
}

# Inject rows from a designated file into a single named database table.
#
# The code in this routine is stolen from the log-archive-receive.pl script and then
# modified (cut down and then slightly extended) to just contain what we need for purposes
# of the present script.  If it seems overly general or still contains some comments that
# might seem out of place here, that's why.  Some of that generality might come in handy in
# the future if it turns out we need to restore rows froom more than one specific table.
#
sub insert_into_database_table {
    my $target_path       = shift;
    my $dump_timestamp    = shift;
    my $table             = shift;
    my $roll_back_changes = shift;
    local $_;

    my $sth     = undef;
    my $query   = undef;
    my $outcome = 1;

    # In this program, we're injecting rows into the runtime database, which has no startvalidtime fields.
    if ( defined $dump_timestamp ) {
	log_timed_message "ERROR:  The insert_into_database_table() dump_timestamp parameter is not supported;";
	log_timed_message "        aborting injection into table $table!";
	$outcome = 0;
	return $outcome;
    }
    # We might consider overriding an undefined value just so we don't run into
    # any Perl errors later on, but there should be no such reference.  So if it
    # happens, we want Perl to tell us about it, so as not to hide the problem.
    # $dump_timestamp = '';

    # This implementation only supports PostgreSQL, because it depends on specific capabilities of the PostgreSQL DBD::Pg driver.
    if ( $runtime_dbtype eq 'postgresql' ) {
	## Here we use the client connection as a transport from the database to the file, both because we're not
	## assuming database superuser privileges (needed for the server to write directly to a file), and because
	## the database might be located on a remote server, which would not have access to our local filesystem.

	# Dump files can potentially be huge, especially for the first archive run when all historical data
	# for a given table is gathered together in a single large file.  Therefore, we have to anticipate
	# the possibility that a dump file might be larger than 2GB.
	#
	# We don't bother specifying O_LARGEFILE here as well, because GroundWork Perl is compiled with
	# "-D_LARGEFILE_SOURCE -D_FILE_OFFSET_BITS=64" (you can check this with "/usr/local/groundwork/perl/bin/perl -V").
	# So we should have no problem accessing very large dump files here.
	if ( sysopen( DUMP, $target_path, O_RDONLY | O_NOFOLLOW ) ) {
	    my $injection_is_good = 1;
	    my $copied_rows       = 0;
	    my $duplicate_rows    = 0;
	    my @deleted_rows      = ();
	    my $ignored_rows      = 0;
	    my $inserted_rows     = 0;
	    my $injected_rows     = 0;

	    # We capture timing statistics for the data-loading activity.  Inasmuch as we are interleaving
	    # file reading and database writing, can cannot reasonably be more granular about the collected
	    # data (i.e., separating reading and writing timing).  So we just lump in everything having to
	    # do with database operations during this activity, including the temporary table creation.
	    my $copy_start_time;
	    my $copy_end_time;
	    capture_timing(\$copy_start_time);

	    ## Create the temporary table first, before copying into it.  PostgreSQL temporary tables
	    ## are automatically unlogged; you cannot specify the UNLOGGED keyword in this command.
	    my $temp_table = "temp_$table";
	    log_timed_message "NOTICE:  Creating temporary table $temp_table ...";
	    ## If we have startvalidtime and endvalidtime fields in $table, it is not acceptable to create
	    ## $temp_table directly in its image.  That's because we don't want those fields in the temporary table,
	    ## which is supposed to mirror the table structure in the runtime database, not in the archive database.
	    ## We want the restricted structure partly because we need to use a COPY command to feed data into the
	    ## temporary table from a dump taken on the runtime database, and partly because we reference "tt.*" in
	    ## one of the SQL statements below, without meaning to have it reference startvalidtime and endvalidtime
	    ## fields.  So we need to either not use those columns in the temporary table definition, or drop those
	    ## two columns from the temporary table definition (for a timed $table) before trying to stuff data into
	    ## the temporary table.  The latter course is by far the simpler, because it means we don't need to have
	    ## detailed knowledge of the column structure when we first create the temporary table.
	    if ( not defined $dbh->do("CREATE TEMPORARY TABLE \"$temp_table\" (LIKE \"$table\")") ) {
		my $errstr = $dbh->errstr;
		chomp $errstr if defined $errstr;
		$errstr = 'unknown condition' if not defined $errstr;
		log_timed_message "ERROR:  Cannot create temporary table \"$temp_table\" ($errstr); aborting injection into table $table!";
		$injection_is_good = 0;
	    }
	    elsif (
		$all_table_row_type{$table} =~ /^timed_/
		and (  !defined( $dbh->do("ALTER TABLE \"$temp_table\" DROP COLUMN startvalidtime") )
		    or !defined( $dbh->do("ALTER TABLE \"$temp_table\" DROP COLUMN endvalidtime") ) )
	      )
	    {
		my $errstr = $dbh->errstr;
		chomp $errstr if defined $errstr;
		$errstr = 'unknown condition' if not defined $errstr;
		log_timed_message "ERROR:  Cannot alter temporary table \"$temp_table\" ($errstr); aborting injection into table $table!";
		$injection_is_good = 0;
	    }

	    if ($injection_is_good) {
		log_timed_message "NOTICE:  Copying data into temporary table $temp_table ...";
		if ( not defined $dbh->do("COPY \"$temp_table\" FROM STDIN") ) {
		    my $errstr = $dbh->errstr;
		    chomp $errstr if defined $errstr;
		    $errstr = 'unknown condition' if not defined $errstr;
		    log_timed_message
		      "ERROR:  Cannot put the database connection into COPY IN mode ($errstr); aborting injection into table $table!";
		    $injection_is_good = 0;
		}
		else {
		    ## A future version of this code might rework this logic to use a separate COPY ... FROM STDIN command
		    ## for each data block of a certain configured number of rows from the file, if we decide that we want
		    ## to commit this data insertion at the block level instead of at the entire-insert level.  It's not
		    ## clear whether that would be either required or advantageous, except perhaps so we could independently
		    ## measure timing for the read-from-file and send-to-database actions.
		    ##
		    ## We don't slurp in the entire file here, because it could be ginormous and chew up all available
		    ## memory.  So we read it as individual rows, and inject those rows directly into the database as we
		    ## go.  Perl doesn't have an operator that would allow us to "read n lines into this array, and let
		    ## me test explicitly for errors" in one operation, so there doesn't seem to be any way to make this
		    ## file-reading process more efficient; we are reliant on whatever input buffer size is supplied by the
		    ## standard Perl I/O layer.  And the PostgreSQL DBI::Pg package doesn't seem to have any block-copy
		    ## support, either, so we are constrained to use per-row calls to pg_putcopydata().  So in that sense,
		    ## our reading the file a row at a time is a good match to the available database facilities.

		    # Oddly, Perl doesn't seem to have any way for me to detect file-reading errors at this stage.
		    # I guess we'll only discover that later, when the file is closed.
		    while (my $data = <DUMP>) {
			## FIX LATER:  The documentation says nothing about the return value of the pg_putcopydata()
			## call, but it should.  Report that upstream.  Here we just assume it must be true on success,
			## false on failure.
			if ( not $dbh->pg_putcopydata($data) ) {
			    my $errstr = $dbh->errstr;
			    chomp $errstr if defined $errstr;
			    $errstr = 'unknown condition' if not defined $errstr;
			    log_timed_message "ERROR:  Cannot copy data into table ($errstr); aborting injection into table $table!";
			    $injection_is_good = 0;
			    ## There's no sense in going further once we see an error.  Better to abort quickly and stop this nonsense.
			    last;
			}
			else {
			    ## Remember, at this point we're only loading the data into a staging area,
			    ## so we don't count inserted rows just yet.  But we do count copied rows,
			    ## to allow us to compute the overall speed of copying.
			    ++$copied_rows;
			    ++$total_rows_copied;
			}
		    }

		    # The documentation is not clear whether we need to call pg_putcopyend() if the copy failed
		    # at an earlier stage, but it can't hurt, so we do it unconditionally here.
		    #
		    # We are assuming that testing the pg_putcopyend() return value here is sufficient to tell us
		    # whether the entire insert succeeded or failed.  Presumably, the fact that we are running the
		    # database connection in auto-commit mode will cause the data to be committed at this time.
		    # Actually, as long as the insertion worked, we don't care whether the data is committed, since
		    # this is just a temporary table that will disappear at the end of this session.  It only needs
		    # to last as long as the upsert/indate actions that will happen later in this session.
		    if ( not $dbh->pg_putcopyend() ) {
			my $errstr = $dbh->errstr;
			chomp $errstr if defined $errstr;
			$errstr = 'unknown condition' if not defined $errstr;
			log_timed_message "ERROR:  Cannot copy data into table ($errstr); aborting injection into table $table!";
			$injection_is_good = 0;
		    }
		}
	    }

	    capture_timing(\$copy_end_time);
	    $total_copy_time += $copy_end_time - $copy_start_time;

	    if ( not close DUMP ) {
		log_timed_message "ERROR:  Cannot close file \"$target_path\" ($!); aborting injection into table $table!";
		$injection_is_good = 0;
	    }

	    if ($injection_is_good) {
		# Originally, we thought we would need to define some PostgreSQL functions here, to accomplish the data
		# merging.  If we did need such functions here, we would create them in the pg_temp schema (pg_temp is
		# an alias for whatever pg_temp_NNN schema is actually in use for your session for creating temporary
		# tables or other objects, once you create such an object).  We would do so both so they are essentially
		# invisible to other database users (although they can go searching through all the pg_temp_NNN schemas,
		# find yours, and use such a function), and because the function definition would automatically disappear
		# once your session ended.  (We should perhaps test to see what happens if PostgreSQL has anything like
		# MySQL's automatic reconnection attempts, if a connection drops -- that might disturb our usage if we
		# cannot then see the function.  But the damage is probably slight -- the affected data would simply be
		# processed in the next archiving run instead.)
		#
		# A big advantage of using the temporary schema is that we never have to worry about replacing any existing
		# function of the same name.  This allows us to fix these function definitions in future versions of this
		# script without needing to deal with cleanup of old function versions.

		# It turns out we really want a MERGE, not an UPSERT.  In its classical incarnation, an UPSERT seems
		# to be oriented around sticking one row at a time into the target table.  But what we want is a bulk
		# UPSERT, and not from externally specified data, but from data already existing in some other table.
		# This is what MERGE is for.  But PostgreSQL 9.X (up through 9.2, at least) has neither MERGE nor
		# UPSERT native support.  So we have to build what we need out of more primitive tools.  Here is the
		# basic approach we need, in dumb-as-rocks fashion.
		#
		#    BEGIN WORK;
		#    SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
		#    LOCK TABLE $table;
		#
		#    -- The UPDATE should skip the $index_field, but capture all other fields in $table.
		#    UPDATE $table SET col1 = t.col1, col2 = t.col2, ... FROM $temp_table t WHERE $temp_table.$index_field = $table.index_field;
		#
		#    -- The INSERT must be expressed in a form which is very efficient to execute; this satisfies that goal.
		#    INSERT INTO "$table" (
		#        SELECT tt.* FROM "$temp_table" AS tt
		#        LEFT JOIN "$table" AS t USING ({their common primary-key columns})
		#        WHERE t."primary-key-column-1" IS NULL [AND t."primary-key-column-2" IS NULL] ...
		#    )
		#
		#    -- A COMMIT releases the lock, too; it also implicitly does a ROLLBACK instead, if some previous
		#    -- command in the transaction failed.  But we are more careful than that; we check the success of
		#    -- each of the commands in the transaction, and if we sense a failure, we don't try to execute any
		#    -- more commands in the transaction, and perform an explicit ROLLBACK here instead of a COMMIT.
		#    COMMIT;
		#    --check the exit code of the commit to see if the whole transaction worked
		#
		# Now, I could easily be wrong about the details here -- what transaction isolation level is required,
		# what locking mode is required (which may depend on the transaction isolation level), exactly what
		# INSERT and UPDATE queries to use, etc.  We shouldn't need to lock the $temp_table, because it is in
		# our own private temp-table space -- though it might not hurt, and we should verify that no other
		# session can see our table.  We should probably always acquire the locks in $temp_table, $table order,
		# for consistency.

		# A given table can have more than one field in its primary key; we take that into account when forming
		# the commands below.
		my @unique_key_fields = @{ $all_table_key_fields{$table} };
		my %is_key_field      = map { $_ => 1 } @unique_key_fields;

		# First, we need a list of all the column names in the table, so we can form the complete UPDATE
		# command below.
		#
		# In MySQL, this would be the following (and then you would need to extract the "Field" column from
		# this more-extensive data):
		#    show columns from `$table`
		# In PostgreSQL, you can get just the field names without any extra data from the query:
		#    select column_name from information_schema.columns
		#    where table_catalog = current_catalog and table_schema = current_schema and table_name='$table'
		#    order by ordinal_position
		# Here we don't necessarily care about the ordering of the column names, but if we do, that last clause
		# puts them in order.
		my @non_key_column_names = ();
		## We need to exclude the startvalidtime and endvalidtime columns from the list of @non_key_column_names
		## we are constructing here, because that list will be used to match columns in $table and $temp_table,
		## and the latter never contains these columns (for good reason).  We could perform this exclusion in
		## any of several ways:  (a) probe the structure of $temp_table instead of $table, (b) ignore such names
		## when we qualify the returned values from this query to decide whether to push them onto the list, or
		## (c) rig the query to recognize and ignore these column names in the first place.  For no particular
		## reason, we choose the last approach.
		$query = "
		    select column_name from information_schema.columns
		    where
			table_catalog = current_catalog  and
			table_schema  = current_schema   and
			table_name    = '$table'         and
			column_name  != 'startvalidtime' and
			column_name  != 'endvalidtime'
		    order by ordinal_position
		";
		log_timed_message "NOTICE:  Finding column names in table $table ...";
		if (not ($sth = $dbh->prepare($query))) {
		    my $errstr = $dbh->errstr;
		    chomp $errstr if defined $errstr;
		    $errstr = 'unknown condition' if not defined $errstr;
		    log_timed_message "ERROR:  Cannot fetch column names for table \"$table\" ($errstr).";
		    $injection_is_good = 0;
		}
		else {
		    if ( not defined $sth->execute ) {
			my $errstr = $sth->errstr;
			chomp $errstr if defined $errstr;
			$errstr = 'unknown condition' if not defined $errstr;
			log_timed_message "ERROR:  Cannot fetch column names for table \"$table\" ($errstr).";
			$injection_is_good = 0;
		    }
		    else {
			while ( my @values = $sth->fetchrow_array() ) {
			    push @non_key_column_names, $values[0] if not exists $is_key_field{ $values[0] };
			}
			## Testing of $sth->err is the approved mechanism for checking for errors here, but the
			## particular values it returns are database-specific, per the DBI specification.  See
			## DBD::Pg for the interpretation of specific values for PostgreSQL.  However, you must
			## also recognize that the current (DBD::Pg v2.19.3) documentation is simply wrong about
			## the values returned by the err() and errstr() routines.  They are generally both undef
			## when no error has occurred; the DBD::Pg module does *not* override this initial value
			## set by the DBI module before each command, when the command has succeeded.
			my $err = $sth->err;
			if ( defined($err) and $err != 2 ) {
			    my $errstr = $sth->errstr;
			    chomp $errstr if defined $errstr;
			    $errstr = 'unknown condition' if not defined $errstr;
			    log_timed_message "ERROR:  Cannot fetch column names for table \"$table\" ($errstr).";
			    $injection_is_good = 0;
			}
		    }
		    if (not $sth->finish) {
			my $errstr = $sth->errstr;
			chomp $errstr if defined $errstr;
			$errstr = 'unknown condition' if not defined $errstr;
			log_timed_message "ERROR:  Cannot fetch column names for table \"$table\" ($errstr).";
			$injection_is_good = 0;
		    }
		}

		my @column_assignments = map { "\"$_\"=\"$temp_table\".\"$_\"" } @non_key_column_names;
		my @null_key_comparisons = map { "\"$table\".\"$_\"=\"null_$table\".\"$_\""} @unique_key_fields;
		my @temp_key_comparisons = map { "\"$table\".\"$_\"=\"$temp_table\".\"$_\""} @unique_key_fields;

		# The LOCK statement here will, by itself, wait forever to obtain a lock if it is not immediately
		# available.  There is apparently no way to specify a timeout directly in the SQL language.  But
		# of course, that's really dumb -- you want that kind of control at the application level.  We
		# have tried to use our usual alarm() and POSIX::RT::Timer constructions around this statement,
		# to no avail.  Fortunately, PostgreSQL provides a transaction-level statement_timeout parameter
		# that we can tweak before the lock and reset immediately afterward, to see the timeout behavior
		# we desire.  See the code below for details.
		#
		my $lock_tables_statement = "LOCK TABLE $temp_table, $table IN EXCLUSIVE MODE";

		# This comparison only counts old rows for which the unique key fields already appear
		# in the runtime database.  It is used only for statistical purposes, to point out that
		# if not all the expected rows got inserted, it's because they were already there.
		#
		# This statement does a sensible join on the two tables even without any indexes on $temp_table
		# because all it has to do is a single full-table walk of the $temp_table, joining each row (or
		# not) with a corresponding row in $table.  We seem to get quite good performance this way.
		#
		my $compare_statement =
		    "SELECT count(*) FROM \"$temp_table\" AS tt LEFT JOIN \"$table\" AS t USING ("
		  . join( ', ', @unique_key_fields )
		  . ") WHERE t.\"" . join( '" IS NOT NULL AND t."', @unique_key_fields ) . "\" IS NOT NULL";

		# We set the startvalidtime field to the earliest time we can definitively say this row was
		# present in the runtime database, and we allow the endvalidtime field to default to NULL.
		# The latter is our flag that says this row is still valid in the runtime database.
		#
		my $startvalidtime = $all_table_row_type{$table} =~ /^timed_/ ? ", '$dump_timestamp'" : '';

		# We special-case the exclusion of certain rows that should not be inserted into $table,
		# because various foreign-key references in such rows will not be satisfied and inserting
		# those rows would just cause a foreign-key violation.  Typically, this happens because
		# certain rows in other tables were deleted between the time that the incoming data was
		# originally captured at the source end (i.e., into the archive database), and the time that
		# this restore is taking place.  (The companion capture-old-status-markers.pl script can't
		# do much about that, without using dblink() to join the archive and runtime databases.  We
		# choose not to invoke that extra complexity there.  It does take into account the information
		# it has available about hosts and services that have been deleted in the runtime database
		# by the time of the last archiving run, but its view of the runtime data doesn't go further
		# than that.)  The simplest way to enforce this exclusion is to just drop the bad rows from
		# the $temp_table, since we won't have any further use for those rows.  We will capture counts
		# for all such deletions, so we can report them in the statistics at the end.  That provides a
		# full accounting of how all the incoming rows were disposed of.
		#
		my @delete_statements = ();
		if ($table eq 'logmessage') {

		    # Indexes into @logmessage_fk_references entries.
		    use constant TABLE_COLUMN   => 0;
		    use constant FOREIGN_TABLE  => 1;
		    use constant FOREIGN_COLUMN => 2;
		    use constant MESSAGE_LABEL  => 3;

		    # Most of this info could have been garnered from PostgreSQL information_schema views,
		    # but we punt here and just hardcode stuff because we want the specific human-readable
		    # labels to be used in messages relating to deletions due to unsatisfied foreign keys.
		    #
		    # We want these delete statements to be executed in the order specified here, to properly
		    # claim the most broad reason appropriate to the exclusion of each row.
		    #
		    #     TABLE_COLUMN             FOREIGN_TABLE      FOREIGN_COLUMN       MESSAGE_LABEL
		    #     -----------------------  -----------------  -------------------  ----------------------
		    my @logmessage_fk_references = (
			[ 'hoststatusid',          'hoststatus',      'hoststatusid',      'host'                 ],
			[ 'servicestatusid',       'servicestatus',   'servicestatusid',   'host service'         ],
			[ 'deviceid',              'device',          'deviceid',          'device'               ],
			[ 'applicationtypeid',     'applicationtype', 'applicationtypeid', 'application type'     ],
			[ 'monitorstatusid',       'monitorstatus',   'monitorstatusid',   'monitor status'       ],
			[ 'severityid',            'severity',        'severityid',        'severity'             ],
			[ 'applicationseverityid', 'severity',        'severityid',        'application severity' ],
			[ 'priorityid',            'priority',        'priorityid',        'priority'             ],
			[ 'typeruleid',            'typerule',        'typeruleid',        'type rule'            ],
			[ 'componentid',           'component',       'componentid',       'component'            ],
			[ 'operationstatusid',     'operationstatus', 'operationstatusid', 'operation status'     ],
		    );

		    # Indexes into @delete_statements entries.
		    use constant DELETE_STATEMENT  => 0;
		    use constant DELETED_ROW_LABEL => 1;

		    foreach my $fkey (@logmessage_fk_references) {
			push @delete_statements, [ "
			    WITH new_rows AS (
				SELECT tt.* $startvalidtime FROM \"$temp_table\" AS tt LEFT JOIN \"$table\" AS t USING ("
			      . join( ', ', @unique_key_fields )
			      . ") WHERE t.\"" . join( '" IS NULL AND t."', @unique_key_fields ) . "\" IS NULL
			    )
			    DELETE FROM \"$temp_table\" AS tt
			    USING new_rows
			    WHERE"
			      . join( ' AND', map { " tt.$_ = new_rows.$_" } @unique_key_fields )
			      . " AND tt.$fkey->[TABLE_COLUMN] NOT IN (
				SELECT ft.$fkey->[FOREIGN_COLUMN] FROM $fkey->[FOREIGN_TABLE] AS ft
			    )
			", $fkey->[MESSAGE_LABEL] ];
		    }
		}

		# This insertion only handles new rows for which the unique key fields do not currently appear
		# in the runtime database.
		#
		# This statement does a sensible join on the two tables even without any indexes on $temp_table
		# because all it has to do is a single full-table walk of the $temp_table, joining each row (or
		# not) with a corresponding row in $table.  We seem to get quite good performance this way.
		#
		my $insert_statement =
		    "INSERT INTO \"$table\" (SELECT tt.* $startvalidtime FROM \"$temp_table\" AS tt LEFT JOIN \"$table\" AS t USING ("
		  . join( ', ', @unique_key_fields )
		  . ") WHERE t.\"" . join( '" IS NULL AND t."', @unique_key_fields ) . "\" IS NULL)";

		my $transaction_started = 0;
		if ($injection_is_good) {
		    ## We count the transaction as having been started even if the begin_work() call fails; this
		    ## flag is mostly a record that we got to this point where we tried to begin the transaction.
		    ## This simply provides the condition for controlling whether we later execute a rollback()
		    ## if some earlier or later database access has failed.  If we get to this point and the
		    ## begin_work() call fails, we won't do any more work until the rollback, which will also
		    ## fail, but that secondary failure is okay.  It's just an insurance policy so we close out
		    ## the transaction in case any funny condition happened during the begin_work() where a
		    ## transaction was really started but it was reported here as a failure.  Stranger things
		    ## have happened.
		    $transaction_started = 1;
		    log_timed_message "NOTICE:  Starting transaction for table $table ...";
		    if ( not $dbh->begin_work() ) {
			my $errstr = $dbh->errstr;
			chomp $errstr if defined $errstr;
			$errstr = 'unknown condition' if not defined $errstr;
			log_timed_message "ERROR:  Cannot start a transaction for table \"$table\" ($errstr).";
			$injection_is_good = 0;
		    }
		}
		if ($injection_is_good) {
		    ## In testing, we have sometimes had trouble getting the locks in short order, because
		    ## we had some old orphaned copy of this script (or more precisely, an orphaned postgres
		    ## process on the database server) still processing the tables we want to edit now, from
		    ## some previously-initiated long-running transaction.  So we capture timing statistics
		    ## here for the locking as well, to see how long we had to spend just waiting.
		    my $lock_start_time;
		    my $lock_end_time;

		    # This detailed level of logging, here and in related code segments below, is inherently
		    # uninteresting.  We only emit these messages because they give an indication in the log
		    # file as to where the code was hanging, if one of these operations takes a long time and
		    # the script gets interrupted while in this process of injecting data.
		    log_timed_message "NOTICE:  Locking tables before processing table $table ...";
		    log_timed_message "DEBUG:  Lock statement is:\n$lock_tables_statement" if $debug_basic;
		    capture_timing(\$lock_start_time);

		    # By coding experiment, we find that neither wrapping the lock statement in an alarm()
		    # construction nor wrapping it in a POSIX::RT::Timer construction suffices to kick
		    # a hung locking attempt out of its slumber.  But the PostgreSQL client-connection
		    # statement_timeout parameter will do that for us.  We make this setting local to the
		    # transaction, then further constrain it to just the lock statement by resetting it
		    # after we have fully processed the lock statement.  (We wait to perform the reset
		    # until after we have captured the error status from the lock statement.)

		    my $timed_out = 0;
		    if (not defined $dbh->do("SET LOCAL statement_timeout = $table_locking_timeout_ms")) {
			my $errstr = $dbh->errstr;
			chomp $errstr if defined $errstr;
			$errstr = 'unknown condition' if not defined $errstr;
			log_timed_message "ERROR:  Cannot set statement_timeout before locking table \"$table\" ($errstr).";
			$injection_is_good = 0;
		    }
		    elsif ( not defined $dbh->do($lock_tables_statement) ) {
			my $errstr = $dbh->errstr;
			chomp $errstr if defined $errstr;
			$errstr = 'unknown condition' if not defined $errstr;
			log_timed_message "ERROR:  Cannot lock tables (\"$temp_table\" or \"$table\") ($errstr).";
			log_timed_message "        Lock statement is:\n$lock_tables_statement" if not $debug_basic;
			$timed_out = ($errstr =~ /canceling statement due to statement timeout/);
			$injection_is_good = 0;
		    }
		    elsif (not defined $dbh->do("SET LOCAL statement_timeout = DEFAULT")) {
			my $errstr = $dbh->errstr;
			chomp $errstr if defined $errstr;
			$errstr = 'unknown condition' if not defined $errstr;
			log_timed_message "ERROR:  Cannot reset statement_timeout before modifying data in table \"$table\" ($errstr).";
			$injection_is_good = 0;
		    }

		    # If we do get a lock timeout, we run an additional query to try to identify what activity is blocking
		    # us by holding a lock.  It will be up to the administrator to use the process ID information we spill
		    # out here to advantage, by further relating it to whatever else is going on in the system, and taking
		    # whatever action is deemed necessary to correct the problem.
		    if ($timed_out) {
			## We perform a rollback on the current transaction so we can once again get access
			## to the database for the following diagnostic queries.  We are assured by our having
			## set the $injection_is_good flag above, and the checking of that flag below, that
			## this won't inadvertently cause any SQL commands below to alter the database outside
			## of a locked transaction.  Note also that the rollback will automatically disable the
			## non-default LOCAL statement_timeout we put in place before the lock attempt, so we
			## don't need to take that action explicitly here.  (Logically, I suppose we might have
			## to if the rollback failed, but if we're in this situation, the entire archiving cycle
			## will be aborted, so it wouldn't matter much then.)
			my $warnstr  = undef;
			my $rollback = undef;
			do {
			    ## We get a "rollback ineffective with AutoCommit enabled" warning along with a failed rollback()
			    ## call, but not an error, when that condition arises.  We have to go to extraordinary measures
			    ## to capture the warning message in this situation so we can log it, because no error condition
			    ## or message is reported via the usual $dbh->err() or $dbh->errstr() routines, and there is no
			    ## corresponding $dbh->warn() routine and no $dbh->warnstr() routine to allow the application to
			    ## directly sense the warning state and retrieve the warning message.
			    local $SIG{__WARN__} = sub { $warnstr = $_[0]; };
			    $rollback = $dbh->rollback();
			};
			if ( not $rollback ) {
			    my $message = $dbh->errstr;
			    $message = $warnstr            if not defined $message;
			    $message = 'unknown condition' if not defined $message;
			    chomp $message;
			    log_timed_message "ERROR:  Cannot roll back transaction on table \"$table\" ($message).";
			}

			# Avoid running another rollback for the original transaction later on.
			$transaction_started = 0;

			# FIX LATER:  Assuming that the database itself runs on the $archive_gwservices_machine (i.e., that
			# is where we will find the "postgres" server processes), we could use that variable along with
			# $archive_gwservices_machine_is_remote to reach out and perform a "ps" listing on each process
			# we find that has a lock on the $table, to reveal some details about the client that process is
			# connected to, and what it is doing.  For instance, "ps -efl" on such a process reveals:
			#
			#   1 R postgres 9500 14233 90 80 0 - 104018 - 12:00 ? 05:05:44 postgres:
			#       collage archive_gwcollagedb 192.168.117.202(43982) INSERT
			#
			# This tells us the client address and process, and what type of query is being run.  It could
			# be valuable to collect this type of information at the time of failure, because typically this
			# scripting will be run in the dead of night and the evidence will have disappeared by the time
			# anyone looks at the problem during the daylight hours.  So such detail should be logged for
			# later inspection.
			$query = "
			    select l.pid, l.mode from pg_locks l, pg_database d, pg_class c
			    where d.datname='$runtime_dbname' and l.database = d.oid
			    and c.relname = '$table' and l.relation = c.oid
			    and l.granted=true
			";
			log_timed_message "NOTICE:  Finding processes holding locks on table $table ...";
			if (not ($sth = $dbh->prepare($query))) {
			    my $errstr = $dbh->errstr;
			    chomp $errstr if defined $errstr;
			    $errstr = 'unknown condition' if not defined $errstr;
			    log_timed_message "ERROR:  Cannot find processes holding locks on table \"$table\" ($errstr).";
			}
			else {
			    if ( not defined $sth->execute ) {
				my $errstr = $sth->errstr;
				chomp $errstr if defined $errstr;
				$errstr = 'unknown condition' if not defined $errstr;
				log_timed_message "ERROR:  Cannot find processes holding locks on table \"$table\" ($errstr).";
			    }
			    else {
				while ( my @values = $sth->fetchrow_array() ) {
				    log_message "INFO:  On the archive database machine, process $values[0] holds "
				      . ($values[1] =~ /^[aeiou]/i ? 'an' : 'a') . " $values[1] on the $table table.";
				}
				## Testing of $sth->err is the approved mechanism for checking for errors here, but the
				## particular values it returns are database-specific, per the DBI specification.  See
				## DBD::Pg for the interpretation of specific values for PostgreSQL.  However, you must
				## also recognize that the current (DBD::Pg v2.19.3) documentation is simply wrong about
				## the values returned by the err() and errstr() routines.  They are generally both undef
				## when no error has occurred; the DBD::Pg module does *not* override this initial value
				## set by the DBI module before each command, when the command has succeeded.
				my $err = $sth->err;
				if ( defined($err) and $err != 2 ) {
				    my $errstr = $sth->errstr;
				    chomp $errstr if defined $errstr;
				    $errstr = 'unknown condition' if not defined $errstr;
				    log_timed_message "ERROR:  Cannot find processes holding locks on table \"$table\" ($errstr).";
				}
			    }
			    if (not $sth->finish) {
				my $errstr = $sth->errstr;
				chomp $errstr if defined $errstr;
				$errstr = 'unknown condition' if not defined $errstr;
				log_timed_message "ERROR:  Cannot find processes holding locks on table \"$table\" ($errstr).";
			    }
			}
		    }

		    # No rows can be affected by a simple table lock, so we don't count any here.

		    capture_timing(\$lock_end_time);
		    ## We don't currently save the lock time on a per-table basis, but we could do that
		    ## easily enough here if it seemed important.
		    $total_lock_time += $lock_end_time - $lock_start_time;
		}

		if ($injection_is_good) {
		    my $compare_start_time;
		    my $compare_end_time;
		    capture_timing(\$compare_start_time);
		    log_timed_message "NOTICE:  Comparing new rows with table $table ...";
		    log_timed_message "DEBUG:  Compare statement is:\n$compare_statement" if $debug_basic;
		    my ($rows_affected) = $dbh->selectrow_array($compare_statement);
		    if ( not defined $rows_affected ) {
			my $errstr = $dbh->errstr;
			chomp $errstr if defined $errstr;
			$errstr = 'unknown condition' if not defined $errstr;
			log_timed_message "ERROR:  Cannot compare rows with table \"$table\" ($errstr).";
			log_timed_message "        Compare statement is:\n$compare_statement" if not $debug_basic;
			$injection_is_good = 0;
		    }
		    elsif ( $rows_affected > 0 ) {
			$duplicate_rows = $rows_affected;
			$ignored_rows  += $rows_affected;
			$injected_rows += $rows_affected;
		    }
		    capture_timing(\$compare_end_time);
		    ## We don't currently save the compare time on a per-table basis,
		    ## but we could do that easily enough here if it seemed important.
		    $total_compare_time += $compare_end_time - $compare_start_time;
		}

		if ($injection_is_good) {
		    my $delete_start_time;
		    my $delete_end_time;
		    capture_timing(\$delete_start_time);
		    if (@delete_statements) {
			log_timed_message "NOTICE:  Ignoring new rows that are incompatible with table $table ...";
			foreach my $delete_spec (@delete_statements) {
			    my $delete_statement  = $delete_spec->[DELETE_STATEMENT];
			    my $deleted_row_label = $delete_spec->[DELETED_ROW_LABEL];
			    log_timed_message "DEBUG:  Delete statement is:\n$delete_statement" if $debug_basic;
			    my $rows_affected = $dbh->do($delete_statement);
			    if ( not defined $rows_affected ) {
				my $errstr = $dbh->errstr;
				chomp $errstr if defined $errstr;
				$errstr = 'unknown condition' if not defined $errstr;
				log_timed_message "ERROR:  Cannot delete rows in table \"$temp_table\" ($errstr).";
				log_timed_message "        Delete statement is:\n$delete_statement" if not $debug_basic;
				$injection_is_good = 0;
			    }
			    elsif ( $rows_affected > 0 ) {
				## Indexes into @deleted_rows entries.
				use constant DELETE_REASON => 0;
				use constant ROWS_DELETED  => 1;
				push @deleted_rows, [ $deleted_row_label, $rows_affected ];
				$ignored_rows += $rows_affected;
			    }
			}
		    }
		    capture_timing( \$delete_end_time );
		    ## We don't currently save the delete time on a per-table basis,
		    ## but we could do that easily enough here if it seemed important.
		    $total_delete_time += $delete_end_time - $delete_start_time;
		}

		if ($injection_is_good) {
		    my $insert_start_time;
		    my $insert_end_time;
		    capture_timing(\$insert_start_time);
		    log_timed_message "NOTICE:  Inserting new rows into table $table ...";
		    log_timed_message "DEBUG:  Insert statement is:\n$insert_statement" if $debug_basic;
		    my $rows_affected = $dbh->do($insert_statement);
		    if ( not defined $rows_affected ) {
			## FIX LATER:  If we get errors like the following, we should attempt to locate and log the
			## respective primary-key values in the two tables, to make it easier to diagnose the problem.
			## Such problems are particularly difficult to diagnose because the temporary table is, well,
			## temporary.  And by the time you go to investigate the root of the problem, it has evaporated
			## along with all of the obvious evidence.  Although such evidence can still be found in the
			## file that was used to populate the temporary table, it's not obvious that you should look
			## there, and not immediately obvious what to look for in either the file or the still-extant
			## permanent table.
			##     DBD::Pg::db do failed: ERROR:  duplicate key value violates unique constraint "severity_name_key"
			##     DETAIL:  Key (name)=(ACKNOWLEDGEMENT (WARNING)) already exists.
			##         at /usr/local/groundwork/core/archive/bin/log-archive-receive.pl line 1257.
			my $errstr = $dbh->errstr;
			chomp $errstr if defined $errstr;
			$errstr = 'unknown condition' if not defined $errstr;
			log_timed_message "ERROR:  Cannot insert rows into table \"$table\" ($errstr).";
			log_timed_message "        Insert statement is:\n$insert_statement" if not $debug_basic;
			$injection_is_good = 0;
		    }
		    elsif ( $rows_affected > 0 ) {
			$inserted_rows = $rows_affected;
			$injected_rows += $rows_affected;
		    }
		    capture_timing(\$insert_end_time);
		    ## We don't currently save the insert time on a per-table basis,
		    ## but we could do that easily enough here if it seemed important.
		    $total_insert_time += $insert_end_time - $insert_start_time;
		}

		if ($injection_is_good and not $roll_back_changes) {
		    log_timed_message "NOTICE:  Committing changes to table $table ...";
		    if ( not $dbh->commit() ) {
			my $errstr = $dbh->errstr;
			chomp $errstr if defined $errstr;
			$errstr = 'unknown condition' if not defined $errstr;
			log_timed_message "ERROR:  Cannot commit changes to table \"$table\" ($errstr).";
			$injection_is_good = 0;
		    }
		}
		elsif ($transaction_started) {
		    log_timed_message "NOTICE:  Rolling back changes to table $table ...";
		    ## Note that in the case of failure, we do not currently back out any increments
		    ## we made above to counts of inserted rows.  In that sense, those statistics
		    ## might be a bit misleading in the case of a failed run.  One can argue the
		    ## point; we did take those actions, even though we subsequently roll them back.
		    ## So does that count, for statistical purposes?  It depends on your point of view
		    ## -- whether you're trying to count for overall work and speed calculations, or
		    ## whether you're trying to count only the successful changes to the database.
		    ##
		    ## Note that this treatment differs from how we are currently collecting equivalent
		    ## data on the sending side.  There, we only count captured rows for a given table
		    ## if a complete dump was successfully generated.  Perhaps in some future release,
		    ## this discrepancy will be resolved.

		    my $warnstr  = undef;
		    my $rollback = undef;
		    do {
			## We get a "rollback ineffective with AutoCommit enabled" warning along with a failed rollback()
			## call, but not an error, when that condition arises.  We have to go to extraordinary measures
			## to capture the warning message in this situation so we can log it, because no error condition
			## or message is reported via the usual $dbh->err() or $dbh->errstr() routines, and there is no
			## corresponding $dbh->warn() routine and no $dbh->warnstr() routine to allow the application to
			## directly sense the warning state and retrieve the warning message.
			local $SIG{__WARN__} = sub { $warnstr = $_[0]; };
			$rollback = $dbh->rollback();
		    };
		    if ( not $rollback ) {
			my $message = $dbh->errstr;
			$message = $warnstr            if not defined $message;
			$message = 'unknown condition' if not defined $message;
			chomp $message;
			log_timed_message "ERROR:  Cannot roll back changes to table \"$table\" ($message).";
			$injection_is_good = 0;
		    }
		}

		# We need to understand the appropriate discipline to follow with regard to the PostgreSQL sequences
		# which are used to implement what MySQL calls auto-increment columns.
		#
		# If we were to update any PostgreSQL sequences associated with $table, we would first need to
		# locate all such sequences (by poking around in information_schema), then find their respective
		# proper new values, then update those values in the database.  Note that it makes no sense to try
		# to capture the current values of the sequences in the runtime database and inject them into the
		# archive database, because each time we run the sending script, we will in general not be capturing
		# the very latest table data in the runtime database, but the current values of the sequences will
		# have already been updated to reflect later rows in the runtime table.  So the best we could do
		# would be to calculate max($column_name) on the runtime table after this set of old data is
		# inserted, and use that to set the sequence value:
		#
		#     select setval('$sequence_name', max("$column_name")) from $table;
		#
		# where $sequence_name is drawn from the modifier for the $table.$column_name (e.g., "not null
		# default nextval('logmessage_logmessageid_seq'::regclass)" yields logmessage_logmessageid_seq as
		# the $sequence_name).  This would, of course, require a full-table walk to find the maximum value,
		# unless a clever PostgreSQL implementation performs this calculation far faster by just looking
		# at the last value in the PRIMARY KEY index which is very likely defined on $column_name.  (One
		# would hope the database would take the latter approach, because a full-table walk on one of our
		# ever-growing tables would be an ever-more-expensive calculation over time.)
		#
		# However, before we get to that stage, we need to step back and examine the rationale for wanting
		# to maintain such sequences relatively up-to-date in the archive database in the first place.  The
		# fact is, such sequences are there in the runtime database to be used when inserting new rows for
		# which a unique ID is not already established.  In such insert statements, the ID column is either
		# not specified at all or is specified as the special keyword DEFAULT.  Either formulation causes
		# the database to run nextval('$sequence_name') to find the appropriate value to stuff into the new
		# row.  This not only provides an identifier for the row, it also prevents duplicate-key collisions.
		# But over here in the archive database, we never want any source other than this receiving script
		# to insert new rows.  If such insertions from other sources do happen, the archive database will
		# contain local data unrelated to the runtime database, and there will eventually be a collision
		# of sorts when new rows from the runtime database are supposed to be inserted into the archive
		# database, but those ID values are already used.  (In practice, we would just update the rest of
		# the data in such rows, so the database would heal itself, but we don't want such alien data there
		# in the first place, as it would contaminate any reporting done on the archive database.)
		#
		# So, what are we to do to prevent such rogue insertions?  At the moment, we don't have any special
		# active blocking in place; we just depend on a gentleman's agreement that shuts down the usual
		# sources of new rows.  Then, since we won't really have a local use for the sequences in the
		# archive database, we don't bother to try to update them here.  This provides a certain level of
		# protection against rogue inserts, in that, if some data originator does try to insert a new row
		# that implicitly uses nextval('$sequence_name'), the sequence will likely still have a low value
		# that will conflict with an existing row already copied over from the runtime database ("ERROR:
		# duplicate key value violates unique constraint"), and the rogue insert will fail.  It's true that
		# if the rogue client repeats this often enough, they may exceed the range of previously-inserted
		# data (since the failed insert will still leave $sequence_name updated with the next value, and
		# it will climb with subsequent insert attempts), so this is not an absolute protection.  But
		# presumably, before then, the failure of the rogue client will have been flagged and noticed by
		# humans, and they will have disabled it.
		#
		# So the upshot is, this receiving script will NOT try to update sequences in the database.
		#
		# We could try to resolve potential conflicts with local insertions in a slightly different, clever
		# way:  by making the sequences in the archive database be decreasing, starting with -1.  Then
		# if any rogue insertions ever did happen, they would occupy a domain never seen in the runtime
		# database, and there would be no direct conflict.  Of course, the data-contamination problem
		# would still remain, so it's still not a good idea to have other clients adding data to this
		# database.  At the present time, we have not taken any steps to change the sequences to have them
		# be decreasing.

		# We don't bother to capture independent timing statistics for the table-drop portion of
		# this phase, because it is ordinarily not expected to take a significant amount of time.
		# It's just bundled in with the total $injection_time accounting.  The only reason to
		# look deeper would be if that statistic appeared to be out of kilter when compared with
		# $total_copy_time + $total_lock_time + $total_compare_time + $total_insert_time timing.
		log_timed_message "NOTICE:  Dropping temporary table $temp_table ...";
		if (not defined $dbh->do("DROP TABLE \"$temp_table\"")) {
		    ## In this case, we already succeeded or failed while injecting the data into the permanent table,
		    ## so the failure to drop the temporary table (which will be dropped automatically anyway, at the
		    ## end of this session) is of little consequence.  So we don't abort the entire injection of this
		    ## table just because of this one issue.  We do log the occurrence, though, for later forensics.
		    my $errstr = $dbh->errstr;
		    chomp $errstr if defined $errstr;
		    $errstr = 'unknown condition' if not defined $errstr;
		    log_timed_message "WARNING:  Cannot drop temporary table \"$temp_table\" ($errstr).";
		}
	    }

	    if ($injection_is_good) {
		## We only save the statistics if the injection is good.  That means that if this table injection
		## failed, the end-of-run statistics won't reflect the time spent on the table up to the point of
		## failure, which could be a bit misleading with regard to the $row_injection_speed.  We might
		## address that in a future release, when we might perhaps track the injection times on a per-table
		## basis.  Until then, this decision seems acceptable.
		$rows_copied{$table}   = $copied_rows;
		$rows_ignored{$table}  = $duplicate_rows;
		$invalid_rows{$table}  = \@deleted_rows if @deleted_rows;
		$rows_inserted{$table} = $inserted_rows;
		$rows_injected{$table} = $injected_rows;
		$total_rows_ignored  += $ignored_rows;
		$total_rows_inserted += $inserted_rows;
		$total_rows_injected += $injected_rows;
	    }
	    else {
		$outcome = 0;
	    }

	    # We were able to open the file, so we presumably have permission to clean up by removing it (after all,
	    # our rule is always that the reader deletes its own incoming data [when it knows it's safely done with
	    # it], never the writer).  However, at this point only one table has been processed, and we might not
	    # want to delete individual files that were processed to completion until we see whether the entire set
	    # of files is also processed without error.  And if we had trouble processing this file, we might want to
	    # leave it around as forensic evidence to help in diagnosing the problem.  In that regard, the downside
	    # of removing the file now is that it would disallow inspecting the file and potentially running the
	    # receiving script by hand on this set of files.  In any case, the file should eventually be purged as
	    # part of ordinary cleanup of old target-machine data files.
	}
	else {
	    log_timed_message "ERROR:  Cannot open file \"$target_path\" ($!); aborting injection into table $table!";
	    $outcome = 0;
	}
    }
    else {
	log_timed_message "ERROR:  runtime_dbtype \"$runtime_dbtype\" is not supported; aborting injection into table $table!";
	$outcome = 0;
    }

    return $outcome;
}

sub insert_into_all_tables {
    my $target_directory  = shift;
    my $roll_back_changes = shift;
    my $outcome           = 1;

    # FIX LATER:  Here we are about to stop gwservices on the archive server.  We might want to
    # protect against any signals coming in and asynchronously aborting this script before we
    # get a chance to start gwservices, even if all the other database manipulations managed
    # by this routine should be aborted.
    ## FIX MINOR:  Do we need some equivalent, for the runtime gwservices?
    ## $outcome = 0 if not stop_archive_gwservices();

    if ($outcome) {
	# We must always inject referred-to rows before we inject corresponding referencing rows, so we know
	# that we will never attempt to inject any dangling references (which ought to fail due to foreign-key
	# constraint violation).  That rule establishes the ordering in which we must process the tables.  So we
	# intentionally inject the tables in this order:  primary, secondary, tertiary.  Because the foreign-key
	# constraints will be immediately enforced, this requirement cannot be relaxed on the receiving side, even
	# if we implement some kind of snapshot-freeze in the database on the sending side before we capture data
	# from any of the tables.  So we depend not just on the array ordering given in the following foreach
	# loop, but also on the ordering given in the config file for each of the individual arrays listed in the
	# loop control, since there are some cross-table references even between tables listed in the same array.
	# If we don't want to force the config file to be in the required ordering, we would need to dive into
	# the database (that is, look at the information_schema in detail) in the compute_table_archive_order()
	# routine, find all the foreign-key references between tables, compute the correct ordering for injecting
	# the full configured set of tables, and pass the resulting all-tables sequence to here.

	foreach my $table ( @primary_tables, @secondary_tables, @tertiary_tables ) {
	    my $target_file = $matched_file{$table};
	    if ($target_file) {
		my $status = insert_into_database_table(
		    $target_directory ? "$target_directory/$target_file" : $target_file,
		    $matched_dump_timestamp{$table},
		    $table, $roll_back_changes
		);
		if ($status) {
		    ++$total_tables_injected;
		}
		else {
		    $outcome = 0;
		    ## Once we see a failure with one table, there's no point in continuing with other tables,
		    ## because any references to the failed table in the later tables might not be correctly
		    ## satisfied.
		    ##
		    ## FIX MINOR:  Should we perhaps have not committed the changes to earlier tables, and roll
		    ## them all back as well when we find a table that fails, along with the one failing table?
		    ## For now, we leave the changes in place, believing that they won't be damaging.  But this
		    ## decision needs to be thoroughly vetted.  If we do make just one large commit or rollback,
		    ## then we also need to lock all tables at once before any of the old data is inserted back
		    ## into the permanent tables, instead of locking them one-by-one as we inject data into them.
		    ## If we do that, we probably want to also separate the copying of data from all files into
		    ## temporary tables as a separate sub-phase, before any copying of data from temporary tables
		    ## into permanent tables.
		    last;
		}
	    }
	}
    }

    # No matter what happened earlier, we attempt to start gwservices on the archive server.
    # FIX LATER:  I suppose this might be considered potentially problematic, if you had
    # gwservices intentionally disabled and you don't want this effectively rogue process
    # coming in asynchronously and starting them back up again.  Perhaps we need to sense
    # earlier on if there was anything to stop, and only start them up again if they were
    # already running when we began this routine.
    ## FIX MINOR:  Do we need some equivalent, for the runtime gwservices?
    ## $outcome = 0 if not start_archive_gwservices();

    return $outcome;
}

# We must take account of the facts that:
#
# (*) We might have collisions if the scripting is run more than once, so we must
#     handle idempotent insertions.
#
# (*) The nature of logmessageid handling needs to be understood, end-to-end from
#     gwcollagedb to archive_gwcollagedb and back to gwcollagedb.  It is assumed
#     that the original value always preserved at all stages.
#
# (*) It might be the case that some hosts, services, or various other objects are
#     deleted from gwcollagedb after the last run of log archiving but before the
#     capture-old-status-markers.pl script is run.  Thus the input file to this
#     restore-old-status-markers.pl script might contain data for certain hosts or
#     services that no longer exist in the gwcollagedb database, in spite of the
#     filtering that the capture script does to exclude status data for such hosts
#     and services.  And those rows could cause foreign-key violations due to missing
#     foreign-key values when we attempt to insert the data back into the gwcollagedb
#     database.  For simplicity of execution of these repair scripts, such rows must
#     simply be ignored (except for counting them to record in run statistics),
#     without impacting the insertion of other rows.  This behavior is different from
#     that expected with an UPSERT or MERGE action, so extreme care must be taken
#     in the implementation.  Alas, we're dealing with PostgreSQL versions before
#     9.5, which means we can't take advantage of the new "INSERT ... ON CONFLICT DO
#     NOTHING" clause (although even then, we would somehow want an accurate count
#     returned of the number of such conflicts [i.e., ignored rows], and better
#     yet, attribution of the particular reasons for rejection, as we currently
#     implement).  So we run queries that will identify and filter such rows out
#     of the temporary table (delete them) before we bulk-insert all the remaining
#     temporary-table rows into the permanent table.
#
# The situation described in that last point is real, and if not handled, results in
# messages similar to these (reformatted here for easier viewing), both on-screen and
# in the log file:
#
#     DBD::Pg::db do failed: ERROR:  insert or update on table "logmessage"
#         violates foreign key constraint "fk_logmessage_hoststatusid"
#     DETAIL:  Key (hoststatusid)=(1) is not present in table "hoststatus".
#         at ./restore-old-status-markers.pl line 1375.
#     [Thu Aug 25 04:32:50 2016] ERROR:  Cannot insert rows into table "logmessage"
#         (ERROR:  insert or update on table "logmessage" violates foreign key constraint "fk_logmessage_hoststatusid"
#         DETAIL:  Key (hoststatusid)=(1) is not present in table "hoststatus".).
#     [Thu Aug 25 04:32:50 2016]         Insert statement is:
#         INSERT INTO "logmessage" (SELECT tt.*  FROM "temp_logmessage" AS tt
#             LEFT JOIN "logmessage" AS t USING (logmessageid) WHERE t."logmessageid" IS NULL)
#
# If we did not handle the foreign-key reference failures, the only workaround would
# be to capture old rows immediately after an archiving run, to ensure that no hosts
# or services get removed from the runtime database before corresponding old data rows
# are captured from the archive database.  But it's unlikely that such instructions
# would be invariably followed, so we do handle such failures in this script, by
# looking for them and avoiding them.
#
# Fixing that requires recognition of exactly what foreign key constraints might fail
# due to deletion of the referenced rows before this script is run.  Looking at the
# logmessage table, we see the following foreign-key constraints:
#
#     FOREIGN KEY (hoststatusid)          REFERENCES hoststatus(hoststatusid)           ON UPDATE RESTRICT ON DELETE SET NULL
#     FOREIGN KEY (servicestatusid)       REFERENCES servicestatus(servicestatusid)     ON UPDATE RESTRICT ON DELETE SET NULL
#     FOREIGN KEY (applicationtypeid)     REFERENCES applicationtype(applicationtypeid) ON UPDATE RESTRICT ON DELETE RESTRICT
#     FOREIGN KEY (deviceid)              REFERENCES device(deviceid)                   ON UPDATE RESTRICT ON DELETE CASCADE
#     FOREIGN KEY (monitorstatusid)       REFERENCES monitorstatus(monitorstatusid)     ON UPDATE RESTRICT ON DELETE CASCADE
#     FOREIGN KEY (severityid)            REFERENCES severity(severityid)               ON UPDATE RESTRICT ON DELETE CASCADE
#     FOREIGN KEY (applicationseverityid) REFERENCES severity(severityid)               ON UPDATE RESTRICT ON DELETE CASCADE
#     FOREIGN KEY (priorityid)            REFERENCES priority(priorityid)               ON UPDATE RESTRICT ON DELETE CASCADE
#     FOREIGN KEY (typeruleid)            REFERENCES typerule(typeruleid)               ON UPDATE RESTRICT ON DELETE CASCADE
#     FOREIGN KEY (componentid)           REFERENCES component(componentid)             ON UPDATE RESTRICT ON DELETE CASCADE
#     FOREIGN KEY (operationstatusid)     REFERENCES operationstatus(operationstatusid) ON UPDATE RESTRICT ON DELETE CASCADE
#
# In some experiments before we handled the failing foreign-key references, it was
# the "fk_logmessage_hoststatusid" foreign key constraint, the first one presented
# in the list above, that was causing trouble after my having deleted a host which
# was represented in the input file.  The FOREIGN KEY reference means you cannot
# insert a new row into the logmessage table with a non-NULL hoststatusid which is
# not currently present in the hoststatus table, but the ON DELETE SET NULL clause
# means you can have an existing row in the logmessage table with that non-NULL
# hoststatusid, then delete the referenced hoststatus-table row, and end up with
# the same row still in the logmessage table but now with a NULL hoststatusid
# value.  In contrast, if a particular logmessage row for a host service has been
# deleted (say by daily purging), and the host still exists but the service on
# that host no longer exists, then attempting to insert the row will violate the
# fk_logmessage_servicestatusid foreign key constraint instead.
#
# We now handle this in between the $compare_statement and the $insert_statement,
# by special-casing the logmessage table (since we currently don't want to develop
# general-purpose code to identify all foreign-key fields) and using a hardcoded
# list of all the foreign-key fields to delete rows from the temporary table that
# would not insert if we tried.  That strategy does the job here on the restore side
# without requiring any special action on the capture side.  This handles the general
# race condition not just involving deletions between archiving and capture actions,
# but also deletions between capture and restore actions.

# There is, of course, always a possible race condition involved between comparing
# and inserting, in case some other agent gets in the middle and deletes some
# rows from the referenced tables while this script is still running.  That can
# only be solved by either (a) locking all of the associated tables as well as
# the particular table we're inserting into; (b) locking the entire database; (c)
# throwing everything into one massive transaction, presuming that it effectively
# locks all tables involved as they are encountered; or (d) just asking the user
# to re-run the restore action, under the presumption that the race condition is
# unlikely to happen again.  With respect to (d), restoration of rows in the input
# file is idempotent, so there's no danger there.
#
# FIX MINOR:  If we cannot fully solve this issue in the restore script (as we
# believe we have now done), extend the capture-old-status-markers.pl script to
# filter out rows that have similar issues with all the foreign-key reference fields
# other than just the hoststatusid and servicestatusid fields that are currently
# handled there.  But, that doesn't solve deletes between the capture run and the
# restore run, so a restore-side solution is better all around.
#
#
# FIX MINOR:  Try these experiments.  Be careful when interpreting results.
# (*) Archive a fair number of hosts and their services.  Capture status markers.
# (*) Delete one of the hosts in the runtime database, so any further attempt to
#     capture status markers should ignore that host.
# (*) Capture status markers again, and see whether the captured rows include any
#     data for the deleted host.  (Such data should have been excluded on the
#     capture side by the query filters applied there.)
# (*) Delete one of the services in the runtime database, so any further attempt
#     to capture status markers should ignore that service.
# (*) Capture status markers again, and see whether the captured rows include any
#     data for the deleted service.  (Such data should have been excluded on the
#     capture side by the query filters applied there.)
#
#
# In combination, these facts mean that a simple single "COPY logmessage FROM STDIN"
# statement copying data directly from the input file to the logmessage table is
# not acceptable, since COPY FROM fails at the first failed row insertion.  But
# with care, we do not need to insert each row separately with an independent COPY
# statement.  That could be rather slow, and we avoid the necessity of finding some
# sort of trigger mechanism to handle the failure of individual-row insertion without
# failing the bulk insertion by filtering the data to prevent such failure in the
# first place.
#
# Overall, we're best off starting by reading the input file into a temporary
# table, which can indeed be done as a single bulk operation.  Then after suitable
# filtering, remaining rows from that table can be copied into the logmessage table.
# That is a more efficient way to handle the overall bulk-insert operation.  We
# don't currently have in place a temporary function written in plpgsql to provide
# exception handling for insertion failures.
#
# See the log-archive-receive.pl script and https://www.depesz.com/2012/06/10/why-is-upsert-so-complicated/
# and other Web resources for general ideas on how to handle all of this.
#
sub insert_status_markers {
    my $input_file        = shift;
    my $roll_back_changes = shift;
    my $outcome           = 1;

    # This is how we restrict the otherwise very general actions of insert_into_all_tables()
    # to just the one table that is the intended target for the data in the $input_file the
    # user specified on the command line.  This is the poor man's equivalent of the complex
    # compute_tables_to_archive() routine in the log-archive-receive.pl script.
    $matched_file{'logmessage'} = $input_file;

    $outcome = insert_into_all_tables( '', $roll_back_changes );

    return $outcome;
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
    ( $second, $minute, $hour, $month_day, $month, $year, $week_day, $year_day, $is_dst ) = localtime(POSIX::ceil($script_injection_end_time));
    my $script_end_timestamp = sprintf "%04d-%02d-%02d %02d:%02d:%02d", $year + 1900, $month + 1, $month_day, $hour, $minute, $second;

    $init_time      = $script_init_end_time - $script_start_time;
    $injection_time = $script_injection_end_time - $script_init_end_time;
    $total_time     = POSIX::ceil( $script_injection_end_time - $script_start_time );

    $init_timestamp      = format_hhmmss_timestamp($init_time);
    $injection_timestamp = format_hhmmss_timestamp($injection_time);
    $copy_timestamp      = format_hhmmss_timestamp($total_copy_time);
    $lock_timestamp      = format_hhmmss_timestamp($total_lock_time);
    $compare_timestamp   = format_hhmmss_timestamp($total_compare_time);
    $delete_timestamp    = format_hhmmss_timestamp($total_delete_time);
    $insert_timestamp    = format_hhmmss_timestamp($total_insert_time);
    $total_timestamp     = format_hhmmss_timestamp($total_time);

    # All speed measurements are "per second".
    $row_copy_speed      = $total_copy_time   > 0 ? sprintf( "%12.3f", $total_rows_copied   / $total_copy_time   ) : 'indeterminate';
    $row_insert_speed    = $total_insert_time > 0 ? sprintf( "%12.3f", $total_rows_inserted / $total_insert_time ) : 'indeterminate';
    $row_injection_speed = $injection_time    > 0 ? sprintf( "%12.3f", $total_rows_injected / $injection_time    ) : 'indeterminate';

    log_message "-------------------------------------------------------------------------------";
    log_timed_message "STATS:  Status save statistics:";
    log_message "In the statistics below, \"injected\" means \"found already present, or inserted\".";
    log_message "Status-save script started at:  $script_start_timestamp";
    log_message "Status-save script   ended at:  $script_end_timestamp";
    log_message         "$init_timestamp taken to initialize the status-save script";
    log_message         "$copy_timestamp taken to copy rows into a temporary table";
    log_message         "$lock_timestamp taken to lock tables in the runtime database";
    log_message      "$compare_timestamp taken to compare input data against the runtime database";
    log_message       "$delete_timestamp taken to discard rows from the temporary table";
    log_message       "$insert_timestamp taken to insert rows into the runtime database";
    log_message    "$injection_timestamp taken to run the injection phase on the runtime database";
    log_message        "$total_timestamp taken to run the entire status-save operation";

    log_message "-------------------------------------------------------------------------------";
    log_message sprintf( "%8d %s into which data was injected",
	$total_tables_injected, $total_tables_injected == 1 ? 'table' : 'tables');
    log_message sprintf( "%8d %s copied   into a temporary table",
	$total_rows_copied,   $total_rows_copied   == 1 ? 'row  of data was ' : 'rows of data were' );
    if ($total_rows_ignored) {
	log_message sprintf( "%8d %s ignored    in a temporary table" . ($total_rows_ignored ? '    (see detail below)' : ''),
	    $total_rows_ignored,  $total_rows_ignored  == 1 ? 'row  of data was ' : 'rows of data were' );
    }
    log_message sprintf( "%8d %s inserted into the runtime database",
	$total_rows_inserted, $total_rows_inserted == 1 ? 'row  of data was ' : 'rows of data were' );

    # This output doesn't add much useful, and it's too confusing (easy to confuse with insertion).
    if (0) {
	log_message sprintf( "%8d %s injected into the runtime database",
	    $total_rows_injected, $total_rows_injected == 1 ? 'row  of data was ' : 'rows of data were' );
    }

    log_message "-------------------------------------------------------------------------------";
    foreach my $table ( sort keys %rows_copied ) {
	log_message sprintf( "%8d %s copied in for the $table table",
	    $rows_copied{$table},  $rows_copied{$table}  == 1 ? 'row  of data was ' : 'rows of data were' );
    }
    foreach my $table ( sort keys %rows_ignored ) {
	log_message sprintf( "%8d %s ignored   for the $table table (ROW IS ALREADY PRESENT)",
	    $rows_ignored{$table},  $rows_ignored{$table}  == 1 ? 'row  of data was ' : 'rows of data were' )
	    if $rows_ignored{$table};
    }
    foreach my $table ( sort keys %invalid_rows ) {
	foreach my $attribution ( @{ $invalid_rows{$table} } ) {
	    log_message sprintf(
		"%8d %s ignored   for the $table table (\U$attribution->[DELETE_REASON]\E NO LONGER EXISTS)",
		$attribution->[ROWS_DELETED],
		$attribution->[ROWS_DELETED] == 1 ? 'row  of data was ' : 'rows of data were'
	    );
	}
    }
    foreach my $table ( sort keys %rows_inserted ) {
	log_message sprintf( "%8d %s inserted into the $table table",
	    $rows_inserted{$table}, $rows_inserted{$table} == 1 ? 'row  of data was ' : 'rows of data were' );
    }

    # This output doesn't add much useful, and it's too confusing (easy to confuse with insertion).
    if (0) {
	foreach my $table ( sort keys %rows_injected ) {
	    log_message sprintf( "%8d %s injected into the $table table",
		$rows_injected{$table}, $rows_injected{$table} == 1 ? 'row  of data was ' : 'rows of data were' );
	}
    }

    log_message "-------------------------------------------------------------------------------";
    log_message      "$row_copy_speed rows copied   per second, over all tables";
    log_message    "$row_insert_speed rows inserted per second, over all tables";
    log_message "$row_injection_speed rows injected per second, over all tables";

    log_message "-------------------------------------------------------------------------------";
    log_message sprintf( "%8d rows of status markers were saved into the runtime database%s",
	$total_rows_inserted, $roll_back_all_changes ? ' (BUT THEN ROLLED BACK)' : '' );

    log_timed_message "STATS:  This run of status saving $process_status$suffix.";

    # Reformat certain speed measurements for later use in a status message sent to Foundation.
    $row_insert_speed = $total_insert_time > 0 ? sprintf( "%.1f", $total_rows_inserted / $total_insert_time ) : 'indeterminate';
}

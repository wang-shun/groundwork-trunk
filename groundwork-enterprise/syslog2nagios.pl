#!/usr/local/groundwork/perl/bin/perl -w --
#
#   Copyright (C) 2009-2013 GroundWork Open Source, Inc. (GroundWork)
#   All rights reserved. Use is subject to GroundWork commercial license terms.
#

# This script can be set up within syslog-ng.conf as a long-running
# persistent destination program to process syslog messages.  In that
# setup, there might be multiple concurrent copies of this script
# configured to run as separate destination programs, so the operation
# of this script must accommodate that possible execution context.

use strict;

use Getopt::Long;

use GDMA::GDMAUtils;
use GW::Nagios;

use TypedConfig;

# --------------------------------
# Global variables.
# --------------------------------

my $PROGNAME = "syslog2nagios.pl";
my $VERSION  = "2.1.0";

my $print_help    = 0;
my $print_version = 0;
my $config_file   = "/usr/local/groundwork/config/syslog2nagios.conf";
my $debug_config  = 0;

my $nagios = undef;

my $debug_minimal = 0;
my $debug_detail  = 0;

# Auto-flush the logging output.  Normally this is set to 1, to autoflush the log
# output on every single write.  This minimizes the chances of interleaving partial
# messages in the log file from multiple concurrent copies of this script.
my $autoflush_log_output = 1;

my $local_spool_filename = undef;

# These variables control an internal mechanism for ensuring that we never have the
# process size baloon up forever because of an excessive number of spooled messages.
my $max_spooled_entries = 10_000;
my $exit_early = 0;

# --------------------------------
# Config-file variables.
# --------------------------------

# This option is turned off in the default configuration file simply so the script can be
# safely installed before it is locally configured.  To get the software to run, it must be
# turned on in the configuration file once the rest of the setup is correct for your site.
my $enable_processing = 0;

# Where to log debug messages.  This path will be used in common for all
# concurrent running instances of the syslog2nagios.pl script; they will
# all append to the same file.
my $logfile = "/usr/local/groundwork/common/var/log/syslog-ng/syslog2nagios.log";

# Global Debug Mode Flag;  No debug = 0, Normal debug=1, Detail debug=2
my $debug_level = 0;

# Send to the local Nagios instance?
my $send_to_nagios = 0;

# Send to the local GDMA Spooler, for forwarding elsewhere?
my $send_to_gdma_spooler = 0;

# Where to spool results, if send_to_gdma_spooler is true.
my $gdma_spool_filename = "/usr/local/groundwork/gdma/spool/gdma.spool";

# Target server for the GDMA spooler, if send_to_gdma_spooler is true.
# This would typically be set to the name of this machine's parent server.
my $gdma_spooler_target_server = "localhost";

# Absolute pathname of the local Nagios command pipe.
my $nagios_cmd_pipe = undef;

# The maximum time in seconds to wait for any single write to the Nagios
# command pipe to complete.
my $max_command_pipe_wait_time = 0;

# The maximum size in bytes for any single write operation to the Nagios
# command pipe.  The value chosen here must be no larger than PIPE_BUF
# (getconf -a | fgrep PIPE_BUF) on your platform, unless you have an absolute
# guarantee that no other process will ever write to the command pipe.
my $max_command_pipe_write_size = 0;

# Base file path for where to spool results intended for the local Nagios, 
# if sending fails.  Note that because multiple copies of the syslog2nagios.pl
# script may be running concurrently, each copy will need to create its own
# variant copy so the various copies don't trample each other.
my $local_spool_file_base  = "/usr/local/groundwork/common/var/log/syslog-ng/syslog2nagios.spool";

# --------------------------------
# Preliminary Code
# --------------------------------

# This script will generally be run by syslog-ng as the root user.  To prevent any files
# it creates from being owned by root, and in general to prevent any other operations from
# executing in a privileged state, we force ourselves back to running as the "nagios" user.
my ( $nagios_uid, $nagios_gid ) = ( getpwnam('nagios') )[ 2, 3 ];
if ( !defined($nagios_uid) or !defined($nagios_gid) ) {
    ## When this script is run by syslog-ng, this message probably won't find its way
    ## to anyplace useful, but we have to try anyway ...
    print "ERROR:  Cannot find the nagios user ID!\n";
    exit(1);
}
eval {
    ## (( compensate for unbalanced parentheses
    $) = $nagios_gid if $) != $nagios_gid;
    ## << compensate for unbalanced angle brackets
    $> = $nagios_uid if $> != $nagios_uid;
};
if ($@) {
    my $exception = $@;
    chomp $exception;
    print "ERROR:  Cannot set the nagios user or group ID!\n";
    die "$exception\n";
}

Getopt::Long::Configure ("no_ignore_case");
if (! GetOptions (
    'help'         => \$print_help,
    'version'      => \$print_version,
    'config=s'     => \$config_file,
    'debug-config' => \$debug_config
    )) {
    print "ERROR:  Cannot parse command-line options!\n";
    print_usage();
    exit(1);
}

if ($print_version) {
    print "$PROGNAME $VERSION\n";
    print "Copyright 2009-2013 GroundWork Open Source, Inc. (\"GroundWork\").  All\n";
    print "rights reserved.  Use is subject to GroundWork commercial license terms.\n";
    exit(0);
}

if ($print_help) {
    print_usage();
    exit(0);
}

exit(0) if $print_help or $print_version;

# Since the remainder of our script does not process any command-line arguments,
# let's detect an apparently confused command line.
if (scalar @ARGV) {
    print "ERROR:  extra command-line arguments \"@ARGV\" are not understood\n";
    print_usage();
    exit(1);
}

# --------------------------------
# Configuration File Handling
# --------------------------------

my $config = undef;
eval {
    $config = TypedConfig->new ($config_file, $debug_config);
};
if ($@) {
    die $@;
}

eval {
    $enable_processing           = $config->get_boolean('enable_processing');
    $logfile                     = $config->get_scalar('logfile');
    $debug_level                 = $config->get_number('debug_level');
    $send_to_nagios              = $config->get_boolean('send_to_nagios');
    $send_to_gdma_spooler        = $config->get_boolean('send_to_gdma_spooler');
    $gdma_spool_filename         = $config->get_scalar('gdma_spool_filename');
    $gdma_spooler_target_server  = $config->get_scalar('gdma_spooler_target_server');
    $nagios_cmd_pipe             = $config->get_scalar('nagios_cmd_pipe');
    $max_command_pipe_wait_time  = $config->get_number('max_command_pipe_wait_time');
    $max_command_pipe_write_size = $config->get_number('max_command_pipe_write_size');
    $local_spool_file_base       = $config->get_scalar('local_spool_file_base');
};
if ($@) {
    my $exception = $@;
    chomp $exception;
    print STDERR "ERROR:  Cannot read all required configuration parameters:\n";
    die "$exception\n";
}

$debug_minimal = ( $debug_level >= 1 );
$debug_detail  = ( $debug_level >= 2 );

# We must manufacture our own individual copy of the local spool file,
# to avoid trampling on the actions of concurrent copies of this script.
$local_spool_filename = "$local_spool_file_base.$$";

# Remove our local spool file on the way out, to avoid a potential buildup of such files.
# We have some degree of protection when doing so in that we have defined the filename
# to include our process ID at the end, which prevents removing arbitrary files.
# In the case of an uncaught catastrophic error, we will still leak such files, but
# that will be an unusual corner case we cannot deal with directly here.
END {
    unlink $local_spool_filename if defined $local_spool_filename;
}

# --------------------------------
# Main Body of Code
# --------------------------------

# Stop if this is just a debugging run.
exit if $debug_config;

if (not open LOG, '>>', $logfile) {
    ## We sleep awhile before exiting, simply so we don't get into a tight loop with
    ## the parent watchdog process.
    ##
    ## This probably won't end up anywhere visible unless you run this script manually,
    ## but we need some way to get the word out at least under unusual circumstances.
    print "Cannot open logfile; sleeping before dying.\n";
    sleep 60;
    exit 1;
}

# Auto-flush LOG (to force local log messages out immediately), or not.
LOG->autoflush($autoflush_log_output || !$enable_processing);

# We re-open the STDERR stream as a duplicate of the logfile stream, to capture any
# output written to STDERR (from, say, any Perl warnings generated by poor coding).
if ( !open( STDERR, '>>&LOG' ) ) {
    local_log("ERROR:  Can't redirect STDERR to '$logfile': $!");
}
else {
    ## Autoflush the error output on every single write, to avoid problems
    ## with block i/o and badly interleaved output lines on LOG and STDERR.
    STDERR->autoflush(1);
}

local_log("=== Starting up (process $$). ===");

if ( !$enable_processing ) {
    local_log("WARNING:  processing is not enabled in the config file; $PROGNAME will ignore all data.\n");

    # Logically, we would like to just sleep forever (instead of just exiting), simply so we
    # don't get continually restarted by syslog-ng and waste resources:
    #
    #     sleep 100000000;
    #
    # However, in the context of having this script be run by syslog-ng, if we do so, this
    # script will never die on its own, even when syslog-ng itself goes away, and there will
    # be a build-up of running copies of this script as syslog-ng is started and stopped.
    # That's because syslog-ng takes no care to send us a signal when it is exiting.  The
    # only way we can tell that our parent process is gone (and thus know that we should then
    # exit) is to detect an EOF on STDIN, the same as would have happened if the config file
    # had been set up to enable processing.  But the only way to do that is to just read STDIN
    # continually, and throw away any data that shows up.  So that's what we do.
    while (<STDIN>) {
	## There's nothing to do here.  The loop will exit when we see EOF.
	## Otherwise, it just blocks waiting for more input.
    }
    local_log("Exiting.");

    ## We use an exit status of 4 to indicate that the script is disabled -- not that anyone
    ## will care, since we will only exit when syslog-ng (that is, <STDIN>) goes away.
    exit 4;
}

# The lines from STDIN are apparently already in shape (as formatted by our syslog-ng setup)
# for direct forwarding to the Nagios command pipe, including the trailing newline.  So this
# is the format we must parse to construct a format to send to the GDMA spooler if need be.
# [1370986321] PROCESS_SERVICE_CHECK_RESULT;host1370986321;syslog_last;3;UNKNOWN my special message goes here

if ($send_to_nagios) {
    $nagios = GW::Nagios->new( $nagios_cmd_pipe, $max_command_pipe_write_size, $max_command_pipe_wait_time );
    if (not defined $nagios) {
	local_log('FATAL:  Cannot create a GW::Nagios object.');
	exit(1);
    }
}

local_log("unlinking local spool file") if $debug_minimal;
unlink($local_spool_filename);
local_log("waiting for input from syslog-ng") if $debug_minimal;
while ( my $line = <STDIN> ) {
    local_log("got line: $line") if $debug_detail;
    $line = normalize_command($line);
    if ($send_to_nagios) {
	send_command_to_nagios($line);
    }
    if ($send_to_gdma_spooler) {
	if ( $line =~ m{^\[(\d+)\] PROCESS_SERVICE_CHECK_RESULT;([-.a-zA-Z0-9]+);([^;]+);(\d+);(.+)\n$} ) {
	    my $timestamp   = $1;
	    my $host        = $2;
	    my $service     = $3;
	    my $return_code = $4;
	    my $message     = $5;

	    # The retries field should be 0, when the result is first spooled.
	    my $default_retries = 0;
	    my $result_str      = join( '',
		$default_retries, "\t", $gdma_spooler_target_server, "\t", $timestamp, "\t", $host, "\t", $service, "\t",
		$return_code, "\t", $message, "\n" );

	    # Because we don't know when the next line will come in from STDIN, we don't know how many times to buffer
	    # incoming data in memory before sending to the GDMA spooler.  So we always flush it out to the spool file
	    # immediately.  We make it a blocking call, since even though we don't want to block for too long, even
	    # worse would be possible interleaving of data (partial writes) into the spool file from multiple writers
	    # (concurrent copies of this script).
	    my $errstr;
	    my $blocking = 1;
	    my $num_results;
	    if ( !GDMAUtils::spool_results( $gdma_spool_filename, [$result_str], $blocking, \$num_results, \$errstr ) ) {
		## We could perhaps spool locally in this case, and then find some time later on to try to resend to GDMA.
		## But if we want that, we would need a more-sophisticated local spooling algorithm.
		local_log("Failed to spool message to GDMA ($errstr) -- this data will be lost.") if $debug_minimal;
	    }
	}
	else {
	    local_log("Incoming line does not match expected pattern; will be dropped:\n$line") if $debug_minimal;
	}
    }
    if ($exit_early) {
	local_log("Exiting due to earlier conditions.");
	exit(0);
    }
    local_log("re-entering loop") if $debug_detail;
}
local_log("Exiting.") if $debug_minimal;

# --------------------------------
# Subroutines
# --------------------------------

sub print_usage {
    print "usage:  syslog2nagios.pl [-c config_file]\n";
    print "where:  syslog-ng is set up to pipe messages to STDIN of this script.\n";
    print "        -c config_file:  specify an alternate config file\n";
    print "             (default is $config_file)\n";
    print "\n";
    print "or:     syslog2nagios.pl [-h] [-v] [-c config_file] [-d]\n";
    print "where:  -h:  print this help message\n";
    print "        -v:  print the version number\n";
    print "        -d:  dump the config file entries (to debug them)\n";
}

sub local_log {
    my $line = shift(@_);
    ## We print everything in one operation (that is, we concatenate all pieces to print
    ## before printing anything, as opposed to using the comma operator between pieces to
    ## print one piece at a time) to minimize the chances of interleaving partial messages
    ## in the log file from multiple concurrent copies of this script.  (That also depends
    ## on our autoflushing all output to LOG, as set earlier.)
    print LOG "($$)\t" . '[' . ( scalar localtime ) . '] ' . $line . ( $line !~ m{\n$} ? "\n" : '' );
}

sub normalize_command {
    my $string = shift;
    $string =~ s/:/./g;
    return $string;
}

##
## send_command_to_nagios
##
## Send a command to the nagios command pipe, or, if it doesn't exist, spool it.
## (Note that this spooling is an imperfect implementation, and in no way guarantees
## that data will not be lost.)
##

sub send_command_to_nagios {
    my $line = shift(@_);
    my @lines = ();
    # This test for the existence of the Nagios command pipe is only approximately
    # true.  It could disappear the instant after we check and find it is there.
    # But in this implementation, we don't try to fully solve that race condition.
    #
    # In practice, since Nagios 2.0, Nagios itself hasn't deleted the command pipe when the process stops.
    # GroundWork might have removed the command pipe when shutting down Nagios for some time after that
    # release.  But we no longer do so, which means that the command pipe survives Nagios going down.
    # Nagios will presumably pick up any data stuffed into the pipe (as much as it can hold, which is
    # just a few kilobytes) when it starts up again.  However, writers can no longer depend on checking
    # for the existence of the pipe alone to tell whether Nagios is down.  Note that any writer which
    # tries to write to a pipe on which there is no reader may be subject to an infinite wait for the
    # write to complete, which is part of why we use the GW::Nagios package instead of trying to manage
    # writing to the command pipe in simple open code here.
    if ( -e $nagios_cmd_pipe ) {
	if ( -e $local_spool_filename ) {
	    if (open( EMPTYSPOOL, '<', $local_spool_filename )) {
		my $count = 0;
		while ( my $l = <EMPTYSPOOL> ) {
		    push @lines, $l;
		    ## We don't want to have the process size grow too large trying to
		    ## swallow a potentially huge accumulated spool file.  So if we sense
		    ## that happening, we only take the first chunk of the spool file,
		    ## ignore the rest, process what we have in hand, and exit this process
		    ## so it can be restarted in a smaller memory footprint by its parent
		    ## watchdog process.  Some data might be lost in this fashion, but this
		    ## is considered to be an acceptable cost of keeping the system running
		    ## in extreme circumstances.
		    if (++$count >= $max_spooled_entries) {
			$exit_early = 1;
			last;
		    }
		}
		close(EMPTYSPOOL);
	    }
	    unlink($local_spool_filename);
	}
	push @lines, $line;
	my $errors = $nagios->send_messages_to_nagios(\@lines);
	if (@$errors) {
	    if ($debug_minimal) {
		local_log($_) for @$errors;
	    }
	    # FIX LATER:  If we had some previously spooled entries, and this attempt to send them
	    # failed, we won't put them all back into the spool file.  That might cause an indefinite
	    # accumulation of old messages.  Instead, we'll drop all of the old entries, and just locally
	    # spool this latest entry.  More entries might accumulate before the script attempts to write
	    # them all to the command pipe, if the command pipe is gone for some period (though that is
	    # unlikely, since Nagios no longer deletes the command pipe when it exits, and GroundWork
	    # does not emulate that old behavior for it).  This means that most subsequent messages will
	    # end up being dropped on the floor, except for the last one before Nagios comes up again,
	    # that one being having been saved in our own spool file.
	    local_log("Failure in writing to the nagios command pipe; spooling entry.") if $debug_minimal;
	    local_spool($line);
	}
    }
    else {
	local_log("sending to local_spool") if $debug_minimal;
	local_spool($line);
    }
}

##
## local_spool
##
## Spool to a local intermediate file until the nagios command pipe appears.
##

sub local_spool {
    my $line = shift(@_);

    local_log("in local_spool: $line") if $debug_detail;
    if ( open( SPOOL_FILE, '>>', $local_spool_filename ) ) {
	my $written = syswrite( SPOOL_FILE, $line, length($line) );
	if ( $written == length($line) ) {
	    local_log("spooled line: $line") if $debug_detail;
	}
	else {
	    local_log("unable to write to spool file") if $debug_minimal;
	}
	close(SPOOL_FILE);
    }
    else {
	local_log("unable to open local spool file") if $debug_minimal;
    }
}

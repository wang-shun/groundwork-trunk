package GW::Logger;

# Handle logging in a GroundWork add-on extension deployment.
# Copyright (c) 2012 GroundWork Inc. (www.gwos.com).  All rights reserved.
# Use is subject to GroundWork commercial license terms.

# ================================================================
# Documentation.
# ================================================================

# To Do:
# * A future version might log to syslog if log writes fail and a
#   similar syslog message has not been emitted in some configured
#   failure-recovery period relative to this process (like 5 minutes,
#   so as not to flood syslog with such messages from a persistent
#   daemon).
# * Note that the present class construction is such that there will/can
#   be only one of these objects in existence, logging to a single log
#   file.  That might change in a future version of this package.
# * Add POD documentation at the end of this file.

# ================================================================
# Perl setup.
# ================================================================

use strict;
use warnings;

require Exporter;
our @ISA = ('Exporter');

our @EXPORT = qw(
    &fill_text
    &system_log
    &spill_message
    &freeze_logtime
    &log_message
    &log_timed_message
    &log_shutdown
    &log_die
    &open_logfile
    &close_logfile
    &log_is_open
    &rotate_logfile
    LOG_LEVEL_FATAL
    LOG_LEVEL_ERROR
    LOG_LEVEL_WARNING
    LOG_LEVEL_NOTICE
    LOG_LEVEL_STATS
    LOG_LEVEL_INFO
    LOG_LEVEL_DEBUG
);

our @EXPORT_OK = qw(
    &log_only_to_file
);

# This is where we'll pick up any Perl packages not in the standard Perl
# distribution, to make this a self-contained package anchored in a single
# directory.
use FindBin qw($Bin);
use lib "$Bin/perl/lib";

use Sys::Hostname;
use Sys::Syslog 0.27;

# Be sure to update this as changes are made to this module!
my $VERSION = '0.5.0';

# ================================================================
# Working variables.
# ================================================================

my $logfile                = undef;
my $run_interactively      = undef;
my $reflect_log_to_stdout  = 0;
my $max_logfile_size       = undef;
my $max_logfiles_to_retain = undef;
my $message_prefix         = '';  # Typically to be '' or "($$)\t".

my $logtime = '';

my $qualified_hostname   = undef;
my $unqualified_hostname = undef;

# ================================================================
# Global constants.
# ================================================================

# Logging levels;
use constant LOG_LEVEL_FATAL   => 'FATAL';
use constant LOG_LEVEL_ERROR   => 'ERROR';
use constant LOG_LEVEL_WARNING => 'WARNING';
use constant LOG_LEVEL_NOTICE  => 'NOTICE';
use constant LOG_LEVEL_STATS   => 'STATS';
use constant LOG_LEVEL_INFO    => 'INFO';
use constant LOG_LEVEL_DEBUG   => 'DEBUG';

# ================================================================
# Package subroutines.
# ================================================================

# The new() constructor must be invoked as:
#     my $logger = GW::Logger->new ($logfile);
# because if it is invoked instead as:
#     my $logger = GW::Logger::new ($logfile);
# no invocant is supplied as the implicit first argument.

# Given the range of argument types here, we might want to change the constructor
# in the future to accept a configuration hash rather than fixed-position arguments.

sub new {
    my $invocant            = $_[0];   # implicit argument
    $logfile                = $_[1];   # required argument
    $run_interactively      = $_[2];   # optional argument
    $reflect_log_to_stdout  = $_[3];   # optional argument
    $max_logfile_size       = $_[4] || 10_000_000;   # optional argument
    $max_logfiles_to_retain = $_[5] || 5;            # optional argument
    $message_prefix         = $_[6] || '';           # optional argument

    $qualified_hostname    = hostname();
    ($unqualified_hostname = $qualified_hostname) =~ s/\..*//;

    my $class = ref($invocant) || $invocant;    # object or class name
    # Options are stored in our object hash to prepare for the day when
    # we allow more than one such object in the program.  These copies
    # are not yet referenced later on, though.
    my $self = {
	logfile                => $logfile,
	run_interactively      => $run_interactively,
	reflect_log_to_stdout  => $reflect_log_to_stdout,
	max_logfile_size       => $max_logfile_size,
	max_logfiles_to_retain => $max_logfiles_to_retain,
	message_prefix         => $message_prefix
    };
    bless $self, $class;
    return $self;
}

sub printstack {
    my $i     = shift;
    my $limit = shift;
    $i     =   0 if !defined $i;
    $limit = 100 if !defined $limit;
    $limit += $i;
    my $level = $limit;
    while (--$level >= $i && (my ($package, $filename, $line, $subroutine) = caller($level))) {
	print STDERR "$message_prefix${package}, ${filename} line $line (${subroutine})\n";
    }
}

# Sometimes we need to output some text in lines of a certain maximum length, but
# it is difficult to format them that way in the input stream due to substituted
# values of unknown length.  We want to make such output look reasonable to the
# reader.  fill_text takes on the reformatting task by filling words in a line
# until the max length is reached, and then starting a new line.  It also takes
# into account that the first line will later be prefixed with a LOG_LEVEL value
# ("WARNING:  "), as it computes the length of the first output line.  It also
# make sure each line ends with a space character, since the output may in some
# cases be concatenated back together into a single line.

sub fill_text {
    my @message = ();
    local $_;
    my $line    = '';
    my $cur_len = length 'WARNING:  ';  # worst-case log-level prefix prepended later
    my $max_len = 75;
    my $adjunct = '';
    foreach (@_) {
	foreach my $word (split) {
	    if ($cur_len && $cur_len + length($word) > $max_len) {
		push @message, $line;
		$line = '';
		$cur_len = 0;
	    }
	    $adjunct = $word . ($word =~ /[.:]$/ ? '  ' : ' ');
	    $line .= $adjunct;
	    $cur_len += length $adjunct;
	}
    }
    push @message, $line if $line;
    return @message;
}

# Sometimes we don't want to risk the process hanging by writing to STDERR, and
# we already have in place code to emit an equivalent message to our log file.
# In such a case, all the extra work we may need is to get the message out to
# the system log.

sub system_log {
    openlog( 'GW', 'nofatal,pid', 'user' );
    syslog( 'crit', join( '', $message_prefix, @_ ) );
    closelog();
}

# Sometimes we cannot log errors from early code, because the routine in question
# will be called when the location of the logfile is not yet known (that location
# is contained in the configuration file, and we haven't read it yet).  So all we
# can do is spill to the error stream and to syslog (actually, to syslog-ng, on a
# GroundWork Monitor machine).  See Unix::Syslog on CPAN for a discussion of some 
# possible security issues regarding open sockets; but Sys::Syslog has apparently
# addressed these issues with the "native" socket type, as far as I can tell.

sub spill_message {
    freeze_logtime();
    print STDERR $logtime, $message_prefix, @_, "\n";
    system_log(@_);
}

sub freeze_logtime {
    $logtime = '[' . ( scalar localtime ) . '] ';
}

# This routine is generally to be used only for voluminous debug info, which is
# why it is not part of the :DEFAULT symbols exported by this package.  For just
# this one routine, the caller is expected to provide the trailing newline,
# because often a single call will be used to write many complete lines.
sub log_only_to_file {
    print LOG $message_prefix, @_;
}

# FIX LATER:  It's possible to pass a single multi-line message to log_message().
# Currently, we only attach $message_prefix to the first of such lines.
sub log_message {
    if (!defined $logfile) {
	printstack(1,2);
	die "ERROR:  GW::Logger log file is not defined\n";
    }
    if (not log_is_open()) {
	printstack(1,2);
	die "ERROR:  GW::Logger log file is not open\n";
    }
    print LOG $message_prefix, @_, "\n";
    if ($reflect_log_to_stdout) {
	print $message_prefix, @_, "\n";
    }
}

sub log_timed_message {
    freeze_logtime();
    log_message $logtime, @_;
}

sub log_shutdown {
    log_timed_message "=== Shutdown requested; terminating (process $$). ===";
}

sub log_die {
    my $message = shift;
    if (caller() eq 'main') {
	chomp $message;
	log_timed_message $message;
	log_shutdown();
	# I'd like to suppress this extra line of output,
	# but there doesn't seem to be any way to do so.
	die "\n";
    }
}

# Note that we might have multiple copies of a script all dumping into the same logfile.
# That's the purpose of $message_prefix, to disambiguate the various sources.
sub open_logfile {
    if (!defined $logfile) {
	printstack(1,1);
	die "ERROR:  GW::Logger log file is not defined\n";
    }

    if (! open (LOG, '>>', $logfile)) {
	print "FATAL:  Can't open log file '$logfile': $!\n";
	return 0;
    }

    # Autoflush the log output on every single write, both to allow debugging mysterious failures and to
    # avoid interleaving problems if multiple concurrent scripts might be writing to the same log file.
    LOG->autoflush(1);

    # In daemon or non-interactive scripted mode (only), we re-open the STDERR stream as
    # a duplicate of the logfile stream, to capture any output written to STDERR (from,
    # say, some third-party packages which might not be logging through our package).
    # In an interactive scripted mode, where you might want to capture STDERR output to
    # reflect it to the end-user, redirecting of the STDERR stream must be handled outside
    # of this module, since any redirects to the log here will go to the log, not to any
    # immediate output stream that the controlling script can easily capture.
    # FIX MAJOR:  Look at how this works or doesn't work in other contexts, especially
    # the autoflushing-related behavior.
    if (!$run_interactively) {
	if (! open (STDERR, '>>&LOG')) {
	    log_timed_message "ERROR:  Can't redirect STDERR to '$logfile': $!\n";
	}
	else {
	    # Autoflush the error output on every single write, to avoid problems
	    # with block i/o and badly interleaved output lines on LOG and STDERR.
	    STDERR->autoflush(1);
	}
    }
    elsif ($reflect_log_to_stdout) {
	## Autoflush all output on every single write, to avoid problems with block i/o
	## and badly interleaved output lines on STDOUT and STDERR, in case those two
	## streams are tied together outside of the Perl context, by the calling program.
	## FIX MAJOR:  Was autoflush done earlier elsewhere?
	STDOUT->autoflush(1);
	STDERR->autoflush(1);
    }

    return 1;
}

sub close_logfile {
    return close(LOG);
}

sub log_is_open {
    return defined fileno LOG;
}

# Our log-rotation model is that short-lived scripts, especially when multiple copies might
# run concurrently, typically rotate the log file by external agency (e.g., logrotate(8)),
# while long-running daemons typically rotate the log file by internal operation (a call to
# the rotate_logfile() routine here), with attempts made on some periodic basis (that may or
# may not rotate, depending on current file size).
#
# FIX MINOR:  Handle possible concurrent logrotate attempts, probably by locking the original
# logfile during rotation, and skipping rotation if you cannot obtain the lock.  That's only a
# partial solution, though; we also need to suppress rotation here if the log file has already
# been rotated, by looking at stat() and the inode numbers for the open file and the current
# file that has the log file name, paying attention to possible race conditions.
#
# FIX LATER:  Log messages emitted here perhaps ought to be under control of a configured debug level.
sub rotate_logfile {
    ## Implement our own locally-controlled log rotation, to be called by a long-running
    ## daemon so it doesn't fill the entire disk partition with a single huge log file.
    ## We don't close and re-open the logfile until we rotate all the files, so we still
    ## have a place to log file rotation failures, should they arise.
    my $outcome = 1;
    if (tell(LOG) > $max_logfile_size) {
	if ($max_logfiles_to_retain > 1) {
	    log_timed_message "=== On $unqualified_hostname, process $$ is rotating logfiles. ===";
	    my $num = $max_logfiles_to_retain - 1;
	    my $newname = "$logfile.$num";
	    while ( --$num >= 0 ) {
		my $oldname = $num ? "$logfile.$num" : $logfile;
		if (-f $oldname && !rename($oldname, $newname)) {
		    log_message "ERROR:  Cannot rename $oldname to $newname";
		}
		$newname = $oldname;
	    }
	}
	else {
	    truncate(LOG, 0) or $outcome = 0;
	}
	if (not close_logfile()) {
	    system_log "ERROR:  logfile \"$logfile\" close() failed: $!";
	    ## Failure doesn't necessarily mean the file isn't closed, so we continue on.
	}
	if (!open_logfile()) {
	    system_log "ERROR:  logfile \"$logfile\" re-open failed: $!";
	    $outcome = 0;
	}
    }
    return $outcome;
}

1;

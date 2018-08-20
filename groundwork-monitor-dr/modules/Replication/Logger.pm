package Replication::Logger;

# Handle logging in a GroundWork Monitor Disaster Recovery deployment.
# Copyright (c) 2010 GroundWork Open Source (www.groundworkopensource.com).
# All rights reserved.  Use is subject to GroundWork commercial license terms.

# ================================================================
# Documentation.
# ================================================================

# Thie purpose of this module is to abstract our logging needs in the
# various Replication modules, partly because we don't yet know exactly
# what logging mechanism we will finally use.  The current implementation
# might end up being swapped out with something else, and even the logging
# API might change before we're done.

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

# This is where we'll pick up any Perl packages not in the standard Perl
# distribution, to make this a self-contained package anchored in a single
# directory.
use FindBin qw($Bin);
use lib "$Bin/perl/lib";

use Sys::Hostname;
use Sys::Syslog 0.27;

# Be sure to update this as changes are made to this module!
my $VERSION = '0.1.1';

# ================================================================
# Working variables.
# ================================================================

my $logfile                = undef;
my $run_interactively      = undef;
my $reflect_log_to_tty     = 0;
my $max_logfile_size       = undef;
my $max_logfiles_to_retain = undef;
my $stdout_is_a_tty        = (-t STDOUT);

my $logtime = '';

my $qualified_hostname   = undef;
my $unqualified_hostname = undef;

# ================================================================
# Global configuration variables.
# ================================================================

# ================================================================
# Configuration variables that perhaps ought to be migrated to
# the config file.
# ================================================================

# ================================================================
# Global working variables.
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
# Supporting subroutines.
# ================================================================

# The new() constructor must be invoked as:
#     my $logger = Replication::Logger->new ($logfile);
# because if it is invoked instead as:
#     my $logger = Replication::Logger::new ($logfile);
# no invocant is supplied as the implicit first argument.

# Note that the present class construction is such that there will/can be
# only one of these objects in existence.  That might change in a future
# version of this package.

# Given the range of argument types here, we might want to change the constructor
# in the future to accept a configuration hash rather than fixed-position arguments.

sub new {
    my $invocant            = $_[0];   # implicit argument
    $logfile                = $_[1];   # required argument
    $run_interactively      = $_[2];   # optional argument
    $reflect_log_to_tty     = $_[3];   # optional argument
    $max_logfile_size       = $_[4] || 10_000_000;   # optional argument
    $max_logfiles_to_retain = $_[5] || 5;            # optional argument

    $qualified_hostname    = hostname();
    ($unqualified_hostname = $qualified_hostname) =~ s/\..*//;

    my $class = ref($invocant) || $invocant;    # object or class name
    # Options are stored in our object hash to prepare for the day when
    # we allow more than one such object in the program.  These copies
    # are not yet referenced later on, though.
    my $self = {
	logfile                => $logfile,
	run_interactively      => $run_interactively,
	reflect_log_to_tty     => $reflect_log_to_tty,
	max_logfile_size       => $max_logfile_size,
	max_logfiles_to_retain => $max_logfiles_to_retain
    };
    bless $self, $class;
    return $self;
}

# For initial debugging only.
sub printstack {
    my $i     = shift;
    my $limit = shift;
    $i     =   0 if !defined $i;
    $limit = 100 if !defined $limit;
    $limit += $i;
    while ($i < $limit && (my ($package, $filename, $line, $subroutine) = caller($i++))) {
	print STDERR "${package}, ${filename} line $line (${subroutine})\n";
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
    my $cur_len = length 'WARNING:  ';  # worst-case prefix prepended later
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
# In such a case, all extra work we may need is to get the message out to the
# system log.

sub system_log {
    openlog('Replication', 'nofatal,pid', 'user');
    syslog('crit', join('',@_));
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
    print STDERR $logtime, @_, "\n";
    system_log(@_);
}

sub freeze_logtime {
    $logtime = '[' . ( scalar localtime ) . '] ';
}

sub log_message {
    if (!defined $logfile) {
	printstack(1,1);
	die "ERROR:  Replication::Logger log file is not defined\n";
    }
    if (not log_is_open()) {
	printstack(1,1);
	die "ERROR:  Replication::Logger log file is not open\n";
    }
    print LOG @_, "\n";
    if ($reflect_log_to_tty && $stdout_is_a_tty) {
	print @_, "\n";
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

sub open_logfile {
    if (!defined $logfile) {
	printstack(1,1);
	die "ERROR:  Replication::Logger log file is not defined\n";
    }

    if (! open (LOG, '>>', $logfile)) {
	print "FATAL:  Can't open log file '$logfile': $!\n";
	sleep 10;  # Don't have supervise restart us immediately, in a tight loop.
	return 0;
    }

    # Autoflush the log output on every single write, to allow debugging mysterious failures.
    LOG->autoflush(1);

    if (!$run_interactively) {
	# In daemon mode (only), we re-open the STDERR stream as a duplicate of the
	# logfile stream, to capture any output written to STDERR (from, say, some
	# third-party packages which might not be logging through our package).
	if (! open (STDERR, '>>&LOG')) {
	    log_timed_message "ERROR:  Can't redirect STDERR to '$logfile': $!\n";
	}
	else {
	    # Autoflush the error output on every single write, to avoid problems
	    # with block i/o and badly interleaved output lines on LOG and STDERR.
	    STDERR->autoflush(1);
	}
    }

    return 1;
}

sub close_logfile {
    close(LOG);
}

sub log_is_open {
    return defined fileno LOG;
}

# FIX THIS:  We ought not to tie the implementation of this Logger package to certain main:: variables.
# FIX THIS:  If we cannot carry out some of the operations here, we ought to log that fact to syslog.
sub rotate_logfile {
    # Implement our own locally-controlled log rotation, so a long-running daemon
    # doesn't fill the entire disk partition with a single huge log file.  We don't
    # close and re-open the logfile until we rotate all the files, so we still have
    # a place to log file rotation failures, should they arise.
    if (tell(LOG) > $max_logfile_size) {
	if ($max_logfiles_to_retain > 1) {
	    log_timed_message "=== On $unqualified_hostname, process $$ is rotating logfiles. ===" if $main::DEBUG_NOTICE;
	    my $num = $max_logfiles_to_retain - 1;
	    my $newname = "$logfile.$num";
	    while ( --$num >= 0 ) {
		my $oldname = $num ? "$logfile.$num" : $logfile;
		if (-f $oldname && !rename($oldname, $newname)) {
		    log_message "ERROR:  Cannot rename $oldname to $newname" if $main::DEBUG_ERROR;
		}
		$newname = $oldname;
	    }
	}
	else {
	    truncate LOG, 0;
	}
	close_logfile();
	if (!open_logfile()) {
	    return 0;
	}
    }
    return 1;
}

1;

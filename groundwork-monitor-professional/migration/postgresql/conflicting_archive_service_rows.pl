#!/usr/local/groundwork/perl/bin/perl -w --

# conflicting_archive_service_rows.pl

# Copyright (c) 2016 GroundWork, Inc.  All rights reserved.

# This script is designed to display rows in the archive_gwcollagedb database
# that must be deleted to effectively bring that database into synchrony with
# some changes that may have been made in the gwcollagedb database during an
# upgrade from 7.0.2 SP02 or earlier to 7.0.2 SP03 or later.  Such cleanup is
# necessary in order for archiving to function without error.
#
# With the optional -m show argument, this script will display any offending rows.
#
# With the optional -m remove argument, this script will delete the offending rows,
# making it possible to run archiving.

use strict;
use warnings;

use DBI;
use Getopt::Std;
use Config; 
use Data::ShowTable;

use TypedConfig;

use GW::Logger;

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
# Redirect formatted-table output to log it, not just print it.
# ----------------------------------------------------------------

# I tried class inheritance to produce a child class for which a call to
# the parent class ShowBoxTable() routine would in turn end up calling
# the child class's out() and put() routines.  But I couldn't make that
# work.  Perhaps my Perl-fu is not yet up to grade.  But in any case,
# applying a bigger hammer, modifying the original class, gets the job done.

package Data::ShowTable;

# This looks like a repeat of what we did for main::, but now it's in the
# context of Data::ShowTable so that package knows about log_message().
use GW::Logger;

BEGIN {
    undef &out;
    undef &put;
}

our $pending_text = '';

# Print text followed by a newline.
sub out {
    my $fmt = shift;
    $fmt =~ s/\n$//;
    log_message( $pending_text . sprintf( $fmt, @_ ) );
    $pending_text = '';
}

# Print text (without a trailing newline).

sub put {
    ## The tricky part here is that you can't just directly "sprint @_". That
    ## construction returns the number of arguments to sprintf() [that is, the
    ## scalar value of @_], not the formatted string you expect to see.  I don't
    ## know where it is documented that somehow @_ gets interpreted as a scalar
    ## in that context.  Perhaps it is in the definition of sprintf() with a
    ## subroutine prototype as accepting a scalar first argument, that might
    ## coerce @_ given as a first argument to be interpreted that way.  The
    ## simple fix is to peel off the format argument and provide it separately.
    ## I don't know yet whether there is some other way to fix this, such as
    ## some sort of "explode" function that accepts an array and returns exactly
    ## the same array (though that would probably still be problematic).
    my $fmt = shift;
    $pending_text .= sprintf $fmt, @_;
}

# ----------------------------------------------------------------
# Back to the present.
# ----------------------------------------------------------------

package main;

# ================================================================
# Global Configuration Variables
# ================================================================

# Enable display of asterisks when entering passwords.
$Term::ReadPassword::USE_STARS = 1;

# ================================================================
# Package Parameters
# ================================================================

my $PROGNAME       = "conflicting_archive_service_rows.pl";
my $VERSION        = "1.0.1";
my $COPYRIGHT_YEAR = 2016;

my $in_development = 0;    # Set to true to block actual row removal, for repeated-testing purposes.

my $send_config_file    = '/usr/local/groundwork/config/log-archive-send.conf';
my $receive_config_file = '/usr/local/groundwork/config/log-archive-receive.conf';

my $default_postgresql_superuser = 'postgres';

# ================================================================
# Command-Line Parameters
# ================================================================

# In theory, these parameter settings could be overridden by command-line arguments.
# In practice, we don't currently support any such arguments; this script uses only
# a fixed set of arguments.

my $debug_config          = 0;            # if set, spill out certain data about config-file processing to STDOUT
my $show_help             = 0;
my $show_version          = 0;
my $run_interactively     = 1;            # Default on in this program to force logging of all useful output.
my $reflect_log_to_stdout = 1;            # Default on in this program to force logging of all useful output.
my $run_in_test_mode      = 0;
my $postgresql_superuser  = $default_postgresql_superuser;
my $action_mode           = undef;

# ================================================================
# Configuration Parameters
# ================================================================

# Parameters in the send config file.

my $runtime_dbtype = undef;
my $runtime_dbhost = undef;
my $runtime_dbport = undef;
my $runtime_dbname = undef;
my $runtime_dbuser = undef;
my $runtime_dbpass = undef;

# Parameters in the receive config file.

my $archive_dbtype = undef;
my $archive_dbhost = undef;
my $archive_dbport = undef;
my $archive_dbname = undef;
my $archive_dbuser = undef;
my $archive_dbpass = undef;

my $logfile                = undef;
my $max_logfile_size       = undef;                 # log rotate is handled externally, not here
my $max_logfiles_to_retain = undef;                 # log rotate is handled externally, not here

# ================================================================
# Working Variables
# ================================================================

my $dbh   = undef;
my $sth   = undef;
my $query = undef;

my $process_outcome = undef;

my $postgresql_superpass = undef;

my $already_had_dblink = undef;

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

    # If this script fails, and we have successfully made it past reading the config file (so we know how to send
    # messages to Foundation), the $status_message will be sent to Foundation, and show up in the Event Console.
    # Thus there is no point in defining $status_message in the code below until we have made it past that point.
    my $status_message = '';
    $process_outcome = 1;

    # Safety first, since we fork some other processes as we execute.
    $ENV{PATH} = '/bin:/sbin:/usr/bin:/usr/sbin';

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
	    spill_message "FATAL:  $PROGNAME cannot understand its command-line parameters";
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
	log_timed_message "=== Archive fixup script (version $VERSION) starting up (process $$). ===";
	log_timed_message "INFO:  Running with options:  " . join (' ', @SAVED_ARGV);
    }

    # We probably need to create the dblink extension, which requires superuser permissions.
    # So we need to open the database connection as the database superuser, and for that we
    # will need credentials.
    if ($process_outcome) {
	log_timed_message "NOTICE:  Obtaining database credentials.";
	$process_outcome = get_superuser_credentials();
	$status_message = 'cannot obtain database credentials' if not $process_outcome;
    }

    # Open a connection to the archive database.
    if ($process_outcome) {
	log_timed_message "NOTICE:  Opening a connection to the archive database.";
	$process_outcome = open_database_connection();
	$status_message = 'cannot connect to the archive database' if not $process_outcome;
    }

    if ($process_outcome) {
	log_timed_message "NOTICE:  Making a temporary table to use in these calculations.";
	$process_outcome = make_temporary_table();
	$status_message = 'cannot make a temporary table' if not $process_outcome;
    }

    if ($process_outcome) {
	log_timed_message "NOTICE:  Creating dblink if it does not exist in the archive database before this run.";
	$process_outcome = create_dblink();
	$status_message = 'cannot create dblink' if not $process_outcome;
    }

    if ($process_outcome) {
	log_timed_message "NOTICE:  Copying the related runtime data.";
	$process_outcome = copy_runtime_data();
	$status_message = 'cannot copy the related runtime data' if not $process_outcome;
    }

    if ($process_outcome) {
	log_timed_message "NOTICE:  Dropping dblink if it did not exist in the archive database before this run..";
	$process_outcome = drop_dblink();
	$status_message = 'cannot drop dblink' if not $process_outcome;
    }

    if ($process_outcome && $action_mode eq 'show' ) {
	log_timed_message "NOTICE:  Showing any offending rows.";
	$process_outcome = show_colliding_rows();
	$status_message = 'cannot show any offending rows' if not $process_outcome;
    }

    if ($process_outcome && $action_mode eq 'remove' ) {
	log_timed_message "NOTICE:  Removing any offending rows.";
	$process_outcome = remove_colliding_rows();
	$status_message = 'cannot remove any offending rows' if not $process_outcome;
    }

    # Close the connection to the archive database.  This is done unconditionally, since we should close
    # the connection even if errors occurred after it was opened.  The routine can internally handle the
    # case where the connection was never opened in the first place because of prior errors.  However,
    # it gets confusing if we log the occurrence of this call under circumstances when it won't actually
    # do anything, so we do qualify the logging here.
    log_timed_message "NOTICE:  Closing the connection to the archive database." if $dbh and log_is_open();
    close_database_connection();

    close_logfile();

    # Now return the overall cycle success or failure as the status of this routine.
    # This will be turned into a corresponding exit code for the receiving script,
    # and that will be the programmatic way that the calling sending script will
    # know whether this receiving cycle worked as intended.  No further detail is
    # either needed or directly provided.  (Note, though, that if the sending script
    # invokes the -o option of the receiving script, then all log messages in the
    # receiving script will be copied to STDOUT of the receiving script, not just
    # placed in the receiving-script's log file.  The sending script can capture
    # all that information and include it in its own log file.  But that level of
    # detail is more for human consumption and ease of access than anything else.
    # The success or failure of the receiving script is still just determined by
    # the exit code of this script.

    return $process_outcome ? STOP_STATUS : ERROR_STATUS;
}

sub print_version {
    print "$PROGNAME Version:  $VERSION\n";
    print "Copyright $COPYRIGHT_YEAR GroundWork, Inc. (www.gwos.com).\n";
    print "All rights reserved.\n";
}

# In similar programs, the -i and -o options are usually optional.
# But in this program, they are hardcoded to be on.  So we suppress
# mention of them here.
#
# The -d option likewise has no useful output in this program, so we suppress
# recognition of it during command-line parsing, and we also suppress notification
# here that it might be used.
sub print_usage {
    print "usage:  $PROGNAME -h\n";
    print "        $PROGNAME -v\n";
#   print "        $PROGNAME -d\n";
#   print "        $PROGNAME -m show   [-i] [-o]\n";
#   print "        $PROGNAME -m remove [-i] [-o]\n";
    print "        $PROGNAME -m show   [-u {postgresql_superuser}]\n";
    print "        $PROGNAME -m remove [-u {postgresql_superuser}]\n";
    print "where:  -h:  print this help message\n";
    print "        -v:  print the version number\n";
#   print "        -d:  debug config file\n";
    print "        -m show\n";
    print "             Display rows which will cause conflicts with archiving.\n";
    print "        -m remove\n";
    print "             Delete rows which will cause conflicts with archiving.\n";
    print "        -u {postgresql_superuser}\n";
    print "             Specify the PostgreSQL administrative superuser\n";
    print "             (defaults to \"postgres\").\n";
#   print "        -i:  run interactively, not as a background process\n";
#   print "        -o:  write log messages also to standard output\n";
#   print "The -o option is illegal unless -i is also specified.\n";
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

	# Where to log debug messages.
	$logfile = $config->get_scalar ('logfile');

	$archive_dbtype = $config->get_scalar('archive_dbtype');
	$archive_dbhost = $config->get_scalar('archive_dbhost');
	$archive_dbport = $config->get_number('archive_dbport');
	$archive_dbname = $config->get_scalar('archive_dbname');
	$archive_dbuser = $config->get_scalar('archive_dbuser');
	$archive_dbpass = $config->get_scalar('archive_dbpass');
    };
    if ($@) {
	chomp $@;
	$@ =~ s/^ERROR:\s+//i;
	print "ERROR:  Cannot read config file $config_file\n  ($@).\n";
	return 0;
    }

    return 1;
}

# FIX LATER:  There seems to be no way to control getopts() to prohibit
# duplicate option settings such as "-m show -m remove".
sub parse_command_line {
    ## First, clean up the $default_config_file value in case we print usage.
    ## (This is disabled because of potential working-directory issues with realpath().)
    ## my $real_path = realpath ($default_config_file);
    ## $default_config_file = $real_path if $real_path;

    # The -d option is not useful in this program, so we don't process it here.
    # The -i and -o options are hardcoded on in this program, so we don't process them here.
    my %opts;
    if (not getopts('hvtu:m:', \%opts)) {
	print_usage();
	return 0;
    }

    $show_help             = $opts{h};
    $show_version          = $opts{v}; 
#   $debug_config          = $opts{d};
#   $run_interactively     = $opts{i};
#   $reflect_log_to_stdout = $opts{o};
    $run_in_test_mode      = $opts{t};
    $postgresql_superuser  = $opts{u} if defined $opts{u};
    $action_mode           = $opts{m};

    # This test is not a full enforcement of intended exclusivity of the major
    # mode options, but it at least requires that you specify either -d or -m,
    # if neither -h nor -v is specified. 
    if (    !$show_help
	and !$show_version
	and !$debug_config
	and ( !defined($action_mode) or $action_mode !~ /^(show|remove)$/ ) )
    {
	print "ERROR:  You have not specified a valid -m option.\n";
	print "\n";
	print_usage();
	return 0;
    }

    if (!$run_interactively && $reflect_log_to_stdout) {
	print_usage();
	return 0;
    }

    return 1;
}

# The only changes we make to the database in this conflicting_archive_service_rows.pl script
# are deletes, so there is no interesting transaction behavior we need to control explicitly.
# Therefore, we enable auto-commit on this connection, to keep our application code
# simple.  Note that if a PostgreSQL command fails under auto-commit mode, it will be
# automatically rolled back; the application does not need to take any action to make
# this happen.  (Under PostgreSQL, all changes made so far in the transaction are rolled
# back, any additional commands in the transaction are aborted as soon as the command is
# run, before they have a chance to make any changes, and the COMMIT or END that ends
# the transaction is automatically turned into a ROLLBACK; the application has no choice
# about this.  That behavior is not necessarily the case with other commercial databases,
# so this issue would need to be investigated if we ever wanted to port this code to some
# other database.)  So any failed deletes in this script will be turned into no-ops, which
# is fine; a future run of this script ought to successfully perform the same deletions,
# if the underlying problem gets resolved.
#
# If we did turn on auto-commit, then we might perhaps generate table snapshots which are
# consistent across all tables, potentially avoiding some of the out-of-sync issues that
# we are handling instead by careful ordering of the dump operations.  But for now, that
# possibility doesn't seem important enough to force us to move in that direction.
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
    if ( defined($archive_dbtype) && $archive_dbtype eq 'postgresql' ) {
	$dsn = "DBI:Pg:dbname=$archive_dbname;host=$archive_dbhost";
    }
    else {
	$dsn = "DBI:mysql:database=$archive_dbname;host=$archive_dbhost";
    }
    $dbh = DBI->connect( $dsn, $postgresql_superuser, $postgresql_superpass, { 'AutoCommit' => 1 } );
    if (!$dbh) {
	log_timed_message "ERROR:  Cannot connect to database $archive_dbname: ", $DBI::errstr;
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

sub get_value {
    my $default     = shift;
    my $description = shift;
    my $is_password = shift;
    my $is_question = shift;

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

    # Let's open the TTYIN (rather than STDIN) if we can
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
	    print TTYOUT "$prompt_prefix$description [$default]: ";
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
	    if (defined $entered_value) {
		chomp $entered_value;
		$entered_value =~ s/^\s+|\s+$//g;
		$entered_value = $default if $entered_value eq '';
		last;
	    }
	    print TTYOUT "\nInvalid input.  Please try again.\n";
	}
    }

    close(TTYIN);
    close(TTYOUT);
    return $entered_value;
}

sub get_superuser_credentials {
    my $outcome = 1;

    $postgresql_superpass = '';
    $postgresql_superpass = get_value( $postgresql_superpass, "password for the PostgreSQL \"$postgresql_superuser\" user", 1 );
    if ( !defined($postgresql_superpass) ) {
	log_timed_message "FATAL:  PostgreSQL access credentials are incomplete; processing aborted!";
	$outcome = 0;
    }

    return $outcome;
}

# For simplicity in processing, create a local temporary table to contain the
# data in the runtime table of interest.  This is not strictly necessary, since
# we could dynamically access the remote table every time we need that data,
# but it makes this script much simpler.
sub make_temporary_table {
    my $outcome = 1;

    my $create_table = "
	CREATE TEMPORARY TABLE \"runtime_servicestatus\" (
	    servicestatusid    integer,
	    servicedescription character varying(254),
	    hostid             integer
	)
    ";
    my $make_primary_key = "
	ALTER TABLE runtime_servicestatus ADD PRIMARY KEY (servicestatusid)
    ";
    my $make_unique_key = "
	ALTER TABLE runtime_servicestatus ADD UNIQUE (hostid, servicedescription)
    ";
    my $make_index = "
	CREATE INDEX ON runtime_servicestatus (servicedescription)
    ";

    if (   !defined( $dbh->do($create_table) )
	or !defined( $dbh->do($make_primary_key) )
	or !defined( $dbh->do($make_unique_key) )
	or !defined( $dbh->do($make_index) ) )
    {
	log_message $dbh->errstr;
	$outcome = 0;
    }

    return $outcome;
}

sub dblink_exists {
    my $exists = undef;

    my $select_statement = "
	SELECT DISTINCT
	    pg_namespace.nspname,
	    pg_proc.proname
	FROM
	    pg_namespace,
	    pg_proc
	WHERE
	    pg_proc.pronamespace = pg_namespace.oid
	AND pg_proc.proname = 'dblink';
    ";

    if ( not( $sth = $dbh->prepare($select_statement) ) ) {
	my $errstr = $dbh->errstr;
	chomp $errstr if defined $errstr;
	$errstr = 'unknown condition' if not defined $errstr;
	log_timed_message "ERROR:  Cannot find whether dblink exists ($errstr).";
    }
    else {
	if ( not defined $sth->execute ) {
	    my $errstr = $sth->errstr;
	    chomp $errstr if defined $errstr;
	    $errstr = 'unknown condition' if not defined $errstr;
	    log_timed_message "ERROR:  Cannot find whether dblink exists ($errstr).";
	}
	else {
	    $exists = 0;
	    while ( my @values = $sth->fetchrow_array() ) {
		$exists = 1 if $values[1] eq 'dblink';
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
		log_timed_message "ERROR:  Cannot find whether dblink exists ($errstr).";
		$exists = undef;
	    }
	}
	if ( not $sth->finish ) {
	    my $errstr = $sth->errstr;
	    chomp $errstr if defined $errstr;
	    $errstr = 'unknown condition' if not defined $errstr;
	    log_timed_message "ERROR:  Cannot find whether dblink exists ($errstr).";
	    $exists = undef;
	}
    }

    return $exists;
}

sub create_dblink {
    my $outcome = 1;

    $already_had_dblink = 0;

    my $exists = dblink_exists();

    if ( not defined $exists ) {
	$already_had_dblink = 1;    # presumption, so we don't delete, since we won't create here
	$outcome            = 0;
    }
    elsif ($exists) {
	$already_had_dblink = 1;
    }
    elsif ( not defined $dbh->do("CREATE EXTENSION IF NOT EXISTS dblink") ) {
	my $errstr = $dbh->errstr;
	chomp $errstr if defined $errstr;
	$errstr = 'unknown condition' if not defined $errstr;
	log_timed_message "ERROR:  Cannot create dblink ($errstr).";
	$outcome = 0;
    }
    else {
	$exists = dblink_exists();
	if ( not defined $exists ) {
	    ## In this case, we rely on error reporting from dblink_exists(), so we don't log a message here.
	    $outcome = 0;
	}
	elsif ( not $exists ) {
	    log_timed_message "ERROR:  dblink cannot be loaded (despite a supposed successful attempt).";
	    $outcome = 0;
	}
    }

    return $outcome;
}

sub drop_dblink {
    my $outcome = 1;

    if (not $already_had_dblink) {
	if (not defined $dbh->do("DROP EXTENSION IF EXISTS dblink")) {
	    my $errstr = $dbh->errstr;
	    chomp $errstr if defined $errstr;
	    $errstr = 'unknown condition' if not defined $errstr;
	    log_timed_message "WARNING:  Cannot drop dblink ($errstr).";
	    $outcome = 0;
	}
    }

    return $outcome;
}

# Copy data from the runtime table to the temporary table.
sub copy_runtime_data {
    my $outcome = 1;

    my $connection_string = "host=''$runtime_dbhost'' port=''$runtime_dbport'' dbname=''$runtime_dbname'' user=''$runtime_dbuser'' password=''$runtime_dbpass''";

    # Populate our local copies of important fields from the remote table,
    # to make it easier to deal with that data.
    my $insert_statement = "
	INSERT INTO runtime_servicestatus (
	    SELECT * FROM dblink('$connection_string','SELECT servicestatusid, servicedescription, hostid FROM servicestatus') as (
		servicestatusid    integer,
		servicedescription character varying(254),
		hostid             integer
	    )
	)
    ";

    my $rows_affected = $dbh->do($insert_statement);

    if ( not defined $rows_affected ) {
	my $errstr = $dbh->errstr;
	chomp $errstr if defined $errstr;
	$errstr = 'unknown condition' if not defined $errstr;
	log_timed_message "ERROR:  Cannot insert rows into table \"runtime_servicestatus\" ($errstr).";
	## log_timed_message "        Insert statement is:\n$insert_statement" if not $debug_basic;
	$outcome = 0;
    }
    elsif ( $rows_affected > 0 ) {
	# $injected_rows += $rows_affected;
	# $inserted_rows = $rows_affected;
    }

    return $outcome;
}

# This is the one disadvantage of using Perl over psql -- not having a built-in
# way to format table data with separator lines in the usual format.  But we do
# include Data::ShowTable in our standard build, so that comes to the rescue.
sub print_table {
    my $headers = shift;
    my $types   = shift;
    my $rows    = shift;

    my @widths     = ();
    my $row_number = 0;
    my $row_sub    = sub {
	my $arg = shift;

	if ($arg) {
	    $row_number = 0;
	    return 1;
	}
	else {
	    return @{ $rows->[ $row_number++ ] || [] };
	}
    };
    my $max_width = 1000;
    ShowBoxTable $headers, $types, \@widths, $row_sub, undef, $max_width;
}

# =============================================================================
# Join the archive tables with the temporary table to discover rows in the
# archive table that need deletion.
# =============================================================================

# Find all the rows in the archive servicestatus table that might cause trouble
# on subsequent archiving if they are not in some way cleaned up.  We must make
# changes to all of these rows which are equivalent to the changes that were
# made to these rows in the runtime database.
#
# The changes made in the runtime database were that certain rows may have been
# deleted from the servicestatus table either in preparation for the upgrade,
# and other rows may have been modified during the upgrade to reassign the host
# on which a service is apparently operating.  The latter change may cause a
# unique-key collision in the archive database if a colliding row, essentially
# the row corresponding to a row that got deleted in the runtime database in
# preparation for the upgrade, is not also deleted from the archive database.
# So we carefully identify such rows here, and delete them.  This will allow
# the daily archiving to proceed without duplicate-key conflicts.
#
# The "certain rows" we just talked about reflected services of exactly the
# same name on two different hosts which have the same name except for having
# different capitalizaton.  In such a case, one of those hosts will end up
# being deleted during the upgrade, with all of its services moved over to
# the retained host.  There is in general no problem with that except for the
# case where both hosts already had exactly the same service name assigned.
# In that scenario, the service cannot simply be moved over to the retained
# host, because that would violate a uniqueness constraint.  So in preparation
# for the upgrade, the duplicate service for one of the hosts is simply dropped
# from the runtime database, so the upgrade can proceed.  That is necessary
# because the 7.1.0 installer-internal upgrade scripting (at least) does not
# itself detect and handle this case of having duplicate services on deleted
# and retained hosts.
#
# As of this writing, a full analysis of how the installer-internal upgrade
# scripting ought to act has not yet been done, so we cannot compare that to
# the actions taken here.  A complication is that there might be more than
# just two hosts with the same name but different capitalization, and the
# analysis is not just whether we have retained_host.service_name duplicating a
# single deleted_host.service_name, but also whether deleted_host_1.service_name
# matches deleted_host_2.service_name even if retained_host does not already have
# that service assigned.

# We are assuming that the servicedescription field has not been modified by any
# external scripting in the same somewhat hacky way that we know the hostid field
# has been modified.  So no special care is taken to handle any cases where the
# hostid field remains stable but the servicedescription field might run into
# conflicts.  If we ever see such a situation in the wild, we'll handle it then.

# We need to validate the use of this script under a variety of situations,
# including:
#
# (*) System was de-duped with the original KB scripts.
#
# (*) System was de-duped with revised KB scripts, which might make different
#     decisions about the hosts and services to retain and delete, but in any
#     case should deal properly with the logmessage and hostgroupcollection
#     tables as well as the servicestatus table.
#
# (*) System was upgraded with the 7.1.0 upgrade scripting, which did not attempt
#     to deal with duplicate services on matching hosts, and therefore produced a
#     failed upgrade if such duplicates actually existed.  (Perhaps for this reason,
#     we won't need to bother with this case.)
#
# (*) System was upgraded with the 7.1.1 upgrade scripting, directly from 7.0.2
#     SP02 or earlier where duplicates were still allowed, using whatever revised
#     runtime-database upgrade scripting we finally include in the 7.1.1 release.

=pod
-- I prefer the following columns and column names for debugging. but the choices
-- used in the actual code are perhaps more useful for user consumption.
--
--    ss_old.servicestatusid  AS ss_old_id,
--    ss_old.hostid           AS ss_old_hid,
--  rtss_old.hostid           AS rtss_old_hid,
--     h_old.hostname         AS h_old_hostname,
--  rtss_old.servicestatusid  AS rtss_old_id,
--
--    ss_new.servicestatusid  AS ss_new_id,
--    ss_new.hostid           AS ss_new_hid,
--  rtss_new.hostid           AS rtss_new_hid,
--     h_new.hostname         AS h_new_hostname,
--  rtss_new.servicestatusid  AS rtss_new_id,
--
--  ss_old.servicedescription AS servicedescription
=cut

sub show_colliding_rows {
    my $select_statement = "
	SELECT
	    ss_old.servicestatusid    AS servicestatusid,
	    ss_old.servicedescription AS servicedescription,
	     h_old.hostname           AS old_hostname,
	     h_new.hostname           AS new_hostname
	FROM
	    servicestatus ss_old
	    LEFT JOIN host                     h_old ON (   h_old.hostid          = ss_old.hostid)
	    LEFT JOIN runtime_servicestatus rtss_old ON (rtss_old.servicestatusid = ss_old.servicestatusid),
	    servicestatus ss_new
	    LEFT JOIN host                     h_new ON (   h_new.hostid          = ss_new.hostid)
	    LEFT JOIN runtime_servicestatus rtss_new ON (rtss_new.servicestatusid = ss_new.servicestatusid)
	WHERE
	    lower(h_new.hostname)      = lower(h_old.hostname)
	AND ss_new.servicedescription  = ss_old.servicedescription
	AND ss_new.hostid             != ss_old.hostid
	AND rtss_new.hostid           IS NOT NULL
	AND rtss_new.hostid            = ss_old.hostid
	AND ss_new.startvalidtime      = ss_old.startvalidtime
	ORDER BY h_old.hostname, ss_old.servicedescription;
    ";

    my @rows = ();
    $sth     = $dbh->prepare($select_statement);
    $sth->execute();
    while ( my @values = $sth->fetchrow_array() ) {
	push @rows, \@values;
    }
    $sth->finish;

    if (@rows) {
	log_message "";
	log_message "The following rows in the servicestatus table";
	log_message "must be deleted for archiving to run without error.";
	log_message "";

	print_table( [qw(servicestatusid servicedescription old_hostname new_hostname)], [qw(int text text text)], \@rows );

	# If we got this far with a non-default PostgreSQL superuser name, then that same name
	# obviously worked for creating dblink, unless dblink was already available, in which
	# case it's perhaps sensible to just use that same superuser name again.
	#
	my $u_option = ( $postgresql_superuser ne $default_postgresql_superuser ) ? " -u $postgresql_superuser" : '';

	log_message "If the table shown above contains any rows, you must run this";
	log_message "script with the \"-m remove\" option to clean up the archive";
	log_message "database before archiving will succeed.";
	log_message "";
	log_message "    $PROGNAME -m remove$u_option";
	log_message "";
    }
    else {
	log_message "";
	log_message "    There are no colliding rows to worry about.";
	log_message "";
    }
}

sub remove_colliding_rows {
    my $outcome = 1;

    # If we're in development, let's not destroy the data, so we can more easily re-run experiments. 
    $dbh->begin_work() if $in_development;

    my $delete_statement = "
	DELETE FROM servicestatus
	WHERE servicestatusid in (
	    SELECT  
		ss_old.servicestatusid AS ss_old_id
	    FROM
		servicestatus ss_old
		LEFT JOIN host           h_old           ON (   h_old.hostid          = ss_old.hostid)
		LEFT JOIN runtime_servicestatus rtss_old ON (rtss_old.servicestatusid = ss_old.servicestatusid),
		servicestatus ss_new
		LEFT JOIN host           h_new           ON (   h_new.hostid          = ss_new.hostid)
		LEFT JOIN runtime_servicestatus rtss_new ON (rtss_new.servicestatusid = ss_new.servicestatusid)
	    WHERE
		lower(h_new.hostname)      = lower(h_old.hostname)
	    AND ss_new.servicedescription  = ss_old.servicedescription
	    AND ss_new.hostid             != ss_old.hostid
	    AND rtss_new.hostid           IS NOT NULL
	    AND rtss_new.hostid            = ss_old.hostid
	    AND ss_new.startvalidtime      = ss_old.startvalidtime
	);
    ";

    my $rows_affected = $dbh->do($delete_statement);

    if ( not defined $rows_affected ) {
	my $errstr = $dbh->errstr;
	chomp $errstr if defined $errstr;
	$errstr = 'unknown condition' if not defined $errstr;
	log_timed_message "ERROR:  Cannot delete rows from table \"servicestatus\" ($errstr).";
	## log_timed_message "        Delete statement is:\n$delete_statement" if not $debug_basic;
	$outcome = 0;
    }
    else {
	# Transform 0E0 to plain 0 for the following calculation.
	$rows_affected += 0;
	my $rows_deleted = $rows_affected == 0 ? 'No rows were' : $rows_affected == 1 ? '1 row was' : "$rows_affected rows were";
	log_message "";
	log_message "    $rows_deleted deleted.";
	log_message "";
    }

    # If we're in development, let's not destroy the data, so we can more easily re-run experiments. 
    $dbh->rollback() if $in_development;

    return $outcome;
}


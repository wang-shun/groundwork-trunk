################################################################################
#
# GDMA::Logging
#
# This library contains routines that support logging from GDMA code, whether
# operating on the GDMA client or in some server-side support tool.  It is meant
# to replace internal logging routines in the GDMA daemon and ancillary scripts,
# and to provide a convenient means to port the existing GDMA code from those
# routines to the use of Log::Log4perl to make it easier to interoperate with
# other packages in the GDMA:: namespace that may also need to perform logging.
#
# This package differs from the original built-in logging mechanism in the GDMA
# clients in that it maintains a persistent open filehandle for the logfile,
# instead of opening and closing the logfile around every single write.
#
# Copyright (c) 2017-2018 GroundWork, Inc. (www.gwos.com).  All rights reserved.
# Use of this software is subject to commercial license terms.
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the specific language governing permissions and limitations
# under the License.
#
################################################################################

# Caveats:
#
# (*) A potential problem for a long-running daemon:
#     https://rt.cpan.org/Public/Bug/Display.html?id=120906
#     Bug #120906 for Log-Log4perl: timezone incorrect after daylight saving time switch
#     This might perhaps only affect a timezone-offset substitution into the timestamp
#     format, rather than the actual time-of-day calculation.
#
#     Perhaps the reopen_logfile() routine in this package could also be used to check
#     whether a Daylight Savings Time transition has recently occurred, and to somehow
#     reset the underlying Log::Log4perl package if so.

# FIX MINOR:  Compare this package to the existing GW::Logger package.

package GDMA::Logging;

use strict;
use warnings;

use Exporter;
use Cwd 'abs_path';
use Log::Log4perl;
use POSIX qw(EWOULDBLOCK ENOENT);
use Fcntl qw(:flock);

# ================================
# Package Parameters
# ================================

our @ISA       = ('Exporter');
our $VERSION = '0.8.0';
our @EXPORT_OK = (
    qw(
      &lprint
      &log_message
      &log_timed_message
      &rotate_logfile
      &reopen_logfile
      )
);

# ================================
# Package Variables
# ================================

# FIX MINOR:  possibly make the log4perl.appender.Logfile.umask value be configurable via the constructor

# There are six predefined log levels within the Log4perl package:  FATAL, ERROR, WARN, INFO,
# DEBUG, and TRACE (in descending priority).  We define two custom levels at the application
# level to form the full useful set:  FATAL, ERROR, WARN, NOTICE, STATS, INFO, DEBUG, and TRACE.
# To see an individual message appear, your configured logging level here has to at least match
# the priority of that logging message in the code.
my $default_GDMA_log_level = 'STATS';

# Application-level logging configuration, for that portion of the logging
# which is currently handled by the Log4perl package.
my $log4perl_config = <<EOF;

# Notes:
#
# Certain fields in this configuration data are defined as brace-enclosed {string} values
# that must be substituted by the code that uses this data, before it is used to initialize
# the Log::Log4perl package.
#
# The utf8 flag for Log::Log4perl::Appender::File does not in fact pay any attention to
# the value you supply, if you specify that flag in this configuration.  If we wish to
# disable that facility (that is, if we are not intending to produce UTF-8 encoding in
# the logged data), we must not even mention this setting in the configuration data, as
# opposed to trying to set it to a false value.
#
# We use GDMA::Logging::Wrapline instead of Log::Log4perl::Layout::PatternLayout because
# we may have very long lines to log, and it's much easier to read the logfile if we can
# get the long lines automatically wrapped in the logfile to some maximum line length.
#
# We pay careful attention to the specified umask, which will apply to both initial logfile
# creation and re-creation during logfile rotation.

# We use this entry to control possibly completely disabing all logging.
log4perl.threshold = {threshold}

# Use this to send everything from FATAL through {log_level} to the target output.
log4perl.category.GDMA = {log_level}, {appender}

# Send all Log4perl lines to the same log file as the rest of this application.
log4perl.appender.Logfile          = Log::Log4perl::Appender::File
log4perl.appender.Logfile.umask    = 0133
log4perl.appender.Logfile.mode     = append
log4perl.appender.Logfile.syswrite = 1
log4perl.appender.Logfile.filename = {logfile}
# log4perl.appender.Logfile.utf8     = 0
# log4perl.appender.Logfile.layout   = Log::Log4perl::Layout::PatternLayout
log4perl.appender.Logfile.layout   = GDMA::Logging::Wrapline
log4perl.appender.Logfile.layout.MaxLineLength     = 130
log4perl.appender.Logfile.layout.PrefixLength      = 27
log4perl.appender.Logfile.layout.ConversionPattern = [%d{EEE MMM dd HH:mm:ss yyyy}] %m%n

# Making the trigger_level be OFF means that no log messages will be of sufficiently
# high priority to trigger flushing the buffer cache.  We'll do that ourselves later
# on by calling the appender's flush() method explicitly.
log4perl.appender.Buffer               = Log::Log4perl::Appender::Buffer
log4perl.appender.Buffer.appender      = Logfile
log4perl.appender.Buffer.trigger_level = OFF

# This is an extra appender that the package caller can invoke in addition to the standard
# appender by setting the "stdout" option in our package constructor, to log to the standard
# output stream in addition to the selected logfile.  This feature provides for interactive
# output to the terminal while still capturing nearly that same output (with the addition
# of a prefix) in the logfile.
log4perl.appender.StdOut        = Log::Log4perl::Appender::Screen
log4perl.appender.StdOut.stderr = 0
log4perl.appender.StdOut.layout = GDMA::Logging::Wrapline
log4perl.appender.StdOut.layout.MaxLineLength     = 103
log4perl.appender.StdOut.layout.ConversionPattern = %m%n

EOF

# ================================
# End-of-Use Flushing
# ================================

## Buffering and flushing only makes sense in the context of a server-side application that needs
## to protect against possible concurrent operations and logging from multiple processes.  That
## situation may not hold in the context of a client-side application.  Make sure that if we use
## this package in client-side programs, buffering and flushing is disabled, so we get continuous
## logging from long-running daemons as we normally expect.

## Flush all buffered messages when the program finishes.  Note that END blocks are not called if
## the process is killed by an uncaught signal.  (In our application, that might be an issue if
## we are not careful to install signal handlers for both SIGTERM and SIGINT to control a graceful
## shutdown, in every application that uses a grouping of 'bundled'.)  So if such a thing becomes
## an issue, we might consider using the sigtrap pragma (but *very* carefully!, since it has known
## problems during program startup) to convert uncaught signals into exceptions.  However, that's
## problematic at the application level, having a library manipulate signals without knowledge of
## the calling code.
##
## We could perhaps make the logging flush happen when the logger is destroyed, which should usually
## be when it automatically goes out of scope, not necessarily in an END block at process end.  But
## the logger might not be destroyed until global destruction at the end of the program, which is a
## bad time to be trying to execute other stuff that might itself have already been destroyed.
##
END {
    ## We lock the logfile via its open filehandle, if there is one, around flushing cached log data
    ## to the logfile.  Compare that with the syswrite setting in our initialization of the Logfile
    ## (Log::Log4perl::Appender::File) appender.  See the following:
    ## http://search.cpan.org/~mschilli/Log-Log4perl-1.49/lib/Log/Log4perl/FAQ.pm#How_can_I_synchronize_access_to_an_appender?
    ##
    ## Note that the implementation here pretty much assumes you will have only one instance of
    ## this package in play, since there is no iteration over all created instances that you might
    ## want to have independently logging into separate logfiles.
    ##
    ## By checking to see if $buffer_appender is defined, we provide a bit of protection against the
    ## Buffer appender not having been defined in our setup due to either development changes or an
    ## execution context which does not invoke buffering, or a too-early process death.
    ##
    my $buffer_appender = Log::Log4perl->appender_by_name('Buffer');
    if ( defined $buffer_appender ) {
	## The trick is to lock the existing open filehandle, which is unfortunately for our purposes
	## not exposed via some getter method.  So we must know about the internals of the particular
	## appender we are using, and reach in directly.
	my $got_lock = 0;
	my $logfile_appender = Log::Log4perl->appender_by_name('Logfile');
	if ( defined( $logfile_appender->{fh} ) && defined( fileno $logfile_appender->{fh} ) && flock( $logfile_appender->{fh}, LOCK_EX ) ) {
	    $got_lock = 1;
	}
	else {
	    ## There's nothing we can do to recover; we will flush anyway.  But at least we possibly
	    ## get some external notice of the failure.  That said, we can't use $self->{logfile}
	    ## to name the logfile in the message, and we can't use $self->{logfh} for that notice,
	    ## because there is no $self object in sight to reference.  So we fall back to using
	    ## STDERR, which is not ideal but at least has some possibility of working.  Note that
	    ## it's actually not a great idea to generating random output on STDERR when this
	    ## package is called from the registerAgentByDiscovery.pl script, as that output will be
	    ## captured by Foundation and sent back to the client along with whatever we wanted to
	    ## send, thereby corrupting the client's ability to parse the complete string it gets
	    ## back as JSON.  But we should NEVER have difficulty eventually grabbing an exclusive
	    ## lock, so the situation we're in now is definitely evidence of server-side trouble.
	    print STDERR "ERROR:  Could not lock the server-side logfile ($!)\n";
	}
	## Flushing alone won't necessarily do the trick, unless we use syswrite in the Logfile
	## appender configuration to force all writes to be done by the time we release the lock.
	$buffer_appender->flush();
	if ( $got_lock && !flock( $logfile_appender->{fh}, LOCK_UN ) ) {
	    ## There's nothing we can do to recover, except to finally exit the process.  But at
	    ## least we possibly get some external notice of the failure.  Ditto the above comments
	    ## on the use of STDERR.
	    print STDERR "ERROR:  Could not unlock the server-side logfile ($!)\n";
	}
    }
}

# ================================
# Package Routines
# ================================

# Package constructor.
sub new {
    my $invocant = $_[0];
    my $options  = $_[1];    # hashref; options like base directory
    my $phase    = $_[2];
    my $logfh    = $_[3];

    # We require an emergency-output filehandle, separate from the logfile itself.  Without that, we have nowhere
    # sensible to report problems.  Typically, the caller specifies \*STDERR for the $logfh argument.
    if ( not defined $logfh ) {
	print STDERR "ERROR:  GDMA::Logging->new() was called without a full set of arguments.\n";
	return undef;
    }

    # Check for typos.
    foreach my $opt ( keys %$options ) {
	if ( $opt !~ m{^( logfile | stdout | grouping | log_level | max_logfile_size | max_logfiles_to_retain )$}x ) {
	    print $logfh "ERROR:  Option \"$opt\" is not supported by the GDMA::Logging package.\n";
	    return undef;
	}
    }

    my $class = ref($invocant) || $invocant;    # object or class name

    # Basic security:  disallow code in the logging config data.
    Log::Log4perl::Config->allow_code(0);

    # Here we add custom logging levels to form our full standard complement.  There are six
    # predefined log levels:  FATAL, ERROR, WARN, INFO, DEBUG, and TRACE (in descending priority).
    # We add NOTICE and STATS levels to the default set of logging levels supplied by Log4perl,
    # to form the full useful set:  FATAL, ERROR, WARN, NOTICE, STATS, INFO, DEBUG, and TRACE
    # (excepting NONE, I suppose, though there is some hint in the code that OFF is also supported).
    # This *must* be done before the call to Log::Log4perl::init(), and we must also accommodate
    # the situation where initialize_rest_api() has previously been called during some earlier sync
    # in the same program.
    if ( $phase eq 'started' ) {
	Log::Log4perl::Logger::create_custom_level( "NOTICE", "WARN" )   if not Log::Log4perl::Logger->can('notice');
	Log::Log4perl::Logger::create_custom_level( "STATS",  "NOTICE" ) if not Log::Log4perl::Logger->can('stats');
    }

    # If we wanted to support logging either through a syslog appender (I'm not sure how this would
    # be done; presumably via something other than Log::Dispatch::Syslog, since that is still
    # Log::Dispatch) or through Log::Dispatch, the following code extensions would come in handy.
    # (Frankly, I'm not really sure that Log4perl even supports syslog logging other than through
    # Log::Log4perl::JavaMap::SyslogAppender, which just wraps Log::Dispatch::Syslog.)
    #
    # use Sys::Syslog qw(:macros);
    # use Log::Dispatch;
    # my $log_null = Log::Dispatch->new( outputs => [ [ 'Null', min_level => 'debug' ] ] );
    # Log::Log4perl::Logger::create_custom_level("NOTICE", "WARN", LOG_NOTICE, $log_null->_level_as_number('notice'));
    # Log::Log4perl::Logger::create_custom_level("STATS", "NOTICE", LOG_INFO, $log_null->_level_as_number('info'));

    my $logfile = $options->{logfile};
    if ( defined $logfile ) {
	my $root_path_pattern = ( $^O eq 'MSWin32' ) ? qr{[A-Za-z]:\\} : qr{^/};
	if ( $logfile !~ m{$root_path_pattern} ) {
	    ##
	    ## Ensure that use of the $logfile path will still be valid during later log rotation even if the caller
	    ## specified a relative path and the process has since chdir'd to some other working directory.
	    ##
	    ## Cwd::abs_path() is something of a mess.  Depending on which package version you're running, on what platform,
	    ## and what conditions it finds, it can sometimes return an empty string instead of an undef on failure, and it
	    ## can carp or croak in addition or instead.  So we must armor ourselves against most of that.
	    ##
	    ## Also note that Cwd::abs_path() won't operate in any sensible manner if the $logfile whose path we're probing
	    ## for does not already exist.  So we need to check for that circumstance before calling this routine.  Note
	    ## that this condition pretty much prohibits ever specifying a relative pathname for the logfile, since there
	    ## will then never be a good way to bootstrap up into operation.
	    ##
	    my $abs_path = undef;
	    eval { $abs_path = abs_path($logfile) if -f $logfile; };
	    if ( $@ || !defined($abs_path) || $abs_path eq '' ) {
		my $error = $@ ? ( chomp($@), $@ ) : $!;
		print $logfh "ERROR:  Cannot determine the absolute path for logfile $logfile ($error).\n" if defined fileno $logfh;
		return undef;
	    }
	    $logfile = $abs_path;
	}
	if ( -l $logfile ) {
	    ## The logic of log rotation prohibits using a symlink as the supposed logfile.
	    print $logfh "ERROR:  Cannot specify a symlink for the logfile.\n" if defined fileno $logfh;
	    return undef;
	}
	if ( -e $logfile && ( -d _ || -p _ || -b _ ) ) {
	    ## The logic of logging in general and log rotation in particular prohibits using a non-file, non-char-device as a logfile.
	    print $logfh "ERROR:  Cannot specify a directory, pipe, or block-special device for the logfile.\n" if defined fileno $logfh;
	    return undef;
	}
    }
    my $adjusted_logfile = $logfile // ( ( $^O eq 'MSWin32' ) ? 'nul' : '/dev/null' );
    my $threshold = ( defined($logfile) || $options->{stdout} ) ? 'TRACE' : 'OFF';
    my $log_level = defined( $options->{log_level} ) ? uc( $options->{log_level} ) : $default_GDMA_log_level;
    if ( $log_level !~ /^(FATAL|ERROR|WARN|NOTICE|STATS|INFO|DEBUG|TRACE)$/ ) {
	print $logfh "ERROR:  Unrecognized log_level option \"$log_level\" specified.\n" if defined fileno $logfh;
	return undef;
    }

    # This logging setup is an application-global initialization for the Log::Log4perl package, so
    # it only makes sense to initialize it at the application level, not in some lower-level package.
    #
    # It's not documented, but apparently Log::Log4perl::init() always returns 1, even if
    # it is handed a garbage configuration as a literal string.  That makes it hard to tell
    # if you really have it configured correctly.  On the other hand, if it's handed the
    # path to a missing config file, it throws an exception (also undocumented).
    my $logger;
    eval {
	## If the init() value starts with a leading slash, we interpret it as an absolute path to a file
	## that contains the logging configuration data.  Otherwise, we interpret it as the data itself.
	## Apparently, init() can be run more than once, so this is working even if initialize_rest_api()
	## has already been called during some earlier sync in the same program.
	##
	## FIX MINOR:  This initialization, along with the previous calls to create_custom_level(),
	## presume that the application as a whole that calls FoundationSync->sync() has never called
	## Log::Log4perl::init() to initialize its own logging.  That might cause problems if an
	## application does want to do so.
	##
	## A grouping of "individual" means that each separate log message should be flushed immediately to
	## the logfile.  This is appropriate for use in a long-running daemon process, and since that is the
	## most common usage, it is the default setting.  A grouping of "bundled" means that all log messages
	## should be cached until explicitly flushed, which will occur automatically at the very end of the
	## execution of the process.  This is appropriate for use in a transient script that may compete
	## for access to the logfile with possible concurrently-running scripts.  In that case, flushing of
	## the cache must be accompanied by synchronized locking so we don't get messages interspersed from
	## multiple scripts all trying to write to the logfile at the same instant.
	##
	my $appender_name = '';
	if ( defined $logfile ) {
	    if ( !defined( $options->{grouping} ) || $options->{grouping} eq 'individual' ) {
		$appender_name = 'Logfile';
	    }
	    elsif ( $options->{grouping} eq 'bundled' ) {
		$appender_name = 'Buffer';
	    }
	    else {
		die "unknown 'grouping' value '$options->{grouping}'.\n";
	    }
	}
	if ( $options->{stdout} or not defined $logfile ) {
	    ## StdOut won't be used if $options->{stdout} is not true; if that is the case,
	    ## $threshold will be 'OFF' and no logging will occur.  We just need a valid
	    ## value to substitute for {appender} even though it won't be used.
	    $appender_name .= ', ' if $appender_name;
	    $appender_name .= 'StdOut';
	}
	## We must modify a local copy of $log4perl_config, not the package copy,
	## so as not to disturb the original copy for later re-use by some future
	## new instance of this GDMA::Logging object that may have different parameters.
	my $logging_config = $log4perl_config;
	$logging_config =~ s/{appender}/$appender_name/g;
	$logging_config =~ s/{logfile}/$adjusted_logfile/g;
	$logging_config =~ s/{threshold}/$threshold/g;
	$logging_config =~ s/{log_level}/$log_level/g;
	Log::Log4perl::init( $logging_config =~ m{^/} ? $logging_config : \$logging_config );
	$logger = Log::Log4perl::get_logger('GDMA');
	die "Cannot find a usable logger for the GDMA category.\n" if not defined $logger or not $logger->has_appenders();
    };
    if ($@) {
	chomp $@;
	print $logfh "ERROR:  Could not initialize Log::Log4perl logging ($@).\n" if defined fileno $logfh;
	return undef;
    }

    my $max_logfile_size       = $options->{max_logfile_size}       || 10_000_000;
    my $max_logfiles_to_retain = $options->{max_logfiles_to_retain} || 2;
    if ( $max_logfile_size !~ m{^\d+$} ) {
	$logger->error("ERROR:  The max_logfile_size option to the GDMA::Logging constructor is not a non-negative integer.");
	print $logfh "ERROR:  The max_logfile_size option to the GDMA::Logging constructor is not a non-negative integer.\n"
	  if defined fileno $logfh;
	return undef;
    }
    if ( $max_logfiles_to_retain !~ m{^\d+$} ) {
	$logger->error("ERROR:  The max_logfiles_to_retain option to the GDMA::Logging constructor is not a non-negative integer.");
	print $logfh "ERROR:  The max_logfiles_to_retain option to the GDMA::Logging constructor is not a non-negative integer.\n"
	  if defined fileno $logfh;
	return undef;
    }

    my %config = (
	logger                 => $logger,
	logfile                => $logfile,
	max_logfile_size       => $max_logfile_size,
	max_logfiles_to_retain => $max_logfiles_to_retain,
	logfh                  => $logfh
    );

    my $self = bless \%config, $class;

    return $self;
}

# Get the logger which was created when this object was constructed.
sub logger {
    my $self = shift;
    return $self->{logger};
}

# FIX MAJOR:  The utility of this routine is suspect.  It was intended to be a
# temporary transition aid for converting certain scripts to use this package.
sub lprint {
    my $self = shift;
    ## FIX MAJOR
}

# Print the message as a separator, but then suppress any immediately following
# separator, so many separators in a row don't all pile up.  Mostly only useful
# for demarcation between output from successive cycles of a long-running daemon.
sub log_separator {
    my $self    = shift;
    my @message = @_;

    my $suppress_separator = 0;
    my $stdout_appender;
    my $logfile_appender;

    if ( defined( $stdout_appender = $Log::Log4perl::Logger::APPENDER_BY_NAME{StdOut} ) ) {
	$suppress_separator ||= $stdout_appender->{layout}->suppress_separator();
    }
    if ( defined( $logfile_appender = $Log::Log4perl::Logger::APPENDER_BY_NAME{Logfile} ) ) {
	$suppress_separator ||= $logfile_appender->{layout}->suppress_separator();
    }

    if ( not $suppress_separator ) {
	$self->log_message(@message);

	if ( defined $logfile_appender ) {
	    $logfile_appender->{layout}->suppress_separator(1);
	}
	if ( defined $stdout_appender ) {
	    $stdout_appender->{layout}->suppress_separator(1);
	}
    }
}

# Print the message without any wrapping or prefix.
sub log_message {
    my $self    = shift;
    my @message = @_;

    my $stdout_appender;
    my $old_stdout_suppress_wrapping;
    my $logfile_appender;
    my $old_logfile_suppress_wrapping;
    my $old_logfile_suppress_prefix;

    if ( defined( $stdout_appender = $Log::Log4perl::Logger::APPENDER_BY_NAME{StdOut} ) ) {
	$old_stdout_suppress_wrapping = $stdout_appender->{layout}->suppress_wrapping(1);
    }
    if ( defined( $logfile_appender = $Log::Log4perl::Logger::APPENDER_BY_NAME{Logfile} ) ) {
	$old_logfile_suppress_wrapping = $logfile_appender->{layout}->suppress_wrapping(1);
	$old_logfile_suppress_prefix   = $logfile_appender->{layout}->suppress_prefix(1);
    }

    my $routine = ( $message[0] =~ /^(FATAL|ERROR|WARN|NOTICE|STATS|INFO|DEBUG|TRACE)/ ) ? lc $1 : 'stats';
    $self->{logger}->$routine(@message);

    if ( defined $logfile_appender ) {
	$logfile_appender->{layout}->suppress_prefix($old_logfile_suppress_prefix);
	$logfile_appender->{layout}->suppress_wrapping($old_logfile_suppress_wrapping);
    }
    if ( defined $stdout_appender ) {
	$stdout_appender->{layout}->suppress_wrapping($old_stdout_suppress_wrapping);
    }
}

# Print the message without any wrapping.
sub log_timed_message {
    my $self    = shift;
    my @message = @_;

    my $stdout_appender;
    my $old_stdout_suppress_wrapping;
    my $logfile_appender;
    my $old_logfile_suppress_wrapping;

    if ( defined( $stdout_appender = $Log::Log4perl::Logger::APPENDER_BY_NAME{StdOut} ) ) {
	$old_stdout_suppress_wrapping = $stdout_appender->{layout}->suppress_wrapping(1);
    }
    if ( defined( $logfile_appender = $Log::Log4perl::Logger::APPENDER_BY_NAME{Logfile} ) ) {
	$old_logfile_suppress_wrapping = $logfile_appender->{layout}->suppress_wrapping(1);
    }

    my $routine = ( $message[0] =~ /^(FATAL|ERROR|WARN|NOTICE|STATS|INFO|DEBUG|TRACE)/ ) ? lc $1 : 'stats';
    $self->{logger}->$routine(@message);

    if ( defined $logfile_appender ) {
	$logfile_appender->{layout}->suppress_wrapping($old_logfile_suppress_wrapping);
    }
    if ( defined $stdout_appender ) {
	$stdout_appender->{layout}->suppress_wrapping($old_stdout_suppress_wrapping);
    }
}

# We don't consider using Log::Dispatch::FileRotate or some other standard means of rotating the
# logfile, because such methods are either very expensive (checking file size on every write) or
# are in general not synchronized with operational cycles of the calling application.
#
# For a long-running daemon, this routine is intended to be called manually once at the beginning
# of each operating cycle, to check whether the logfile has exceeded its configured max size and
# should be rotated, and to perform that file rotation if need be.  If this routine succeeds
# without rotating the logfile, the caller may wish to call reopen_logfile() to protect against
# the logfile having been deleted or simply renamed by some other actor since the start of the
# last operating cycle.
#
# For a short-term transient process, this routine is intended to be called once just after the
# logfile is first opened.  That way, any failure of log rotation will be noticed right away and
# the transient process can treat this situation just like any other failure to have a logfile
# available.  Given that the logfile was just opened, there should be no motivation to call
# reopen_logfile() afterward.
#
# Return value is:
# 0 => failed (the caller should not assume logging is still functional, should probably
#      just exit the process right away)
# 1 => succeeded (no logfile rotation could be done due to concurrent action, not being
#      configured, or not being needed; appender was not affected)
# 2 => succeeded (rotated the logfile and reopened the appender to use the new logfile)
#
sub rotate_logfile {
    my $self    = shift;
    my $outcome = 1;

    if ( defined $self->{logfile} ) {
	my $appender = Log::Log4perl->appender_by_name('Logfile');

	# We must lock the existing logfile here to protect against race conditions between checking the
	# file size and rotating (renaming) the logfile, so we don't rotate some new logfile that was
	# just opened by some concurrent actor.  The trick is to use the existing open filehandle, which
	# is unfortunately for our purposes not exposed via some getter method.  So we must know about
	# the internals of the particular appender we are using, and reach in directly.
	if ( not flock $appender->{fh}, LOCK_EX | LOCK_NB ) {
	    if ( $! != EWOULDBLOCK ) {
		my $os_error = "$!";
		$os_error .= " ($^E)" if "$^E" ne "$!";
		print { $self->{logfh} } "ERROR:  Could not lock $self->{logfile} ($os_error)\n" if defined fileno $self->{logfh};
		$self->{logger}->error("ERROR:  Could not lock $self->{logfile} ($os_error)");
	    }
	    return $outcome;
	}

	if ( !-l $self->{logfile} && ( !-e _ || -f _ ) && -f $appender->{fh} && -s _ > $self->{max_logfile_size} ) {
	    ## We are over the size limit; we must manually rotate all the logfiles, via renaming.
	    $outcome = 2;
	    if ( $self->{max_logfiles_to_retain} > 1 ) {
		my $logfile = $self->{logfile};
		my $num     = $self->{max_logfiles_to_retain} - 1;
		my $newname = "$logfile.$num";
		while ( --$num >= 0 ) {
		    my $oldname = $num ? "$logfile.$num" : $logfile;
		    if ( -f $oldname ) {
			## The rename() call in this loop won't work in the Windows context if our logger is hanging
			## onto an open file handle.  When that is the case, we see $^E after the rename() call as
			## "The process cannot access the file because it is being used by another process".  In fact,
			## the file is just being used by the same process.  So on that platform, we take extra steps
			## to close and re-open the logfile when that particular file is being renamed.
			my $logfile_needs_reopen = 0;
			if ( $oldname eq $self->{logfile} ) {
			    $self->{logger}->notice("NOTICE:  This logfile is being rotated.");
			    if ( $^O eq 'MSWin32' ) {
				eval { $appender->file_close(); };
				if ($@) {
				    chomp $@;
				    print { $self->{logfh} } "ERROR:  Caught exception while trying to close the logfile:  $@\n"
				      if defined fileno $self->{logfh};
				    $self->{logger}->error("ERROR:  Caught exception while trying to close the logfile:  $@");
				    $outcome = 0;
				}
				$logfile_needs_reopen = 1;
			    }
			}
			if ( !rename( $oldname, $newname ) && $! != ENOENT ) {
			    my $os_error = "$!";
			    $os_error .= " ($^E)" if "$^E" ne "$!";
			    print { $self->{logfh} } "ERROR:  Cannot rename $oldname to $newname ($os_error)\n"
			      if defined fileno $self->{logfh};
			    if ($logfile_needs_reopen) {
				eval { $appender->file_open(); };
				if ($@) {
				    chomp $@;
				    print { $self->{logfh} } "ERROR:  Caught exception while trying to open the logfile:  $@\n"
				      if defined fileno $self->{logfh};
				    ## We can't sensibly output to a logfile which is not open.
				    ## $self->{logger}->error("ERROR:  Caught exception while trying to open the logfile:  $@");
				    $outcome = 0;
				}
				$logfile_needs_reopen = 0;
			    }
			    ## Logging will die if we were unable to re-open the logfile, so we protect against that.
			    eval { $self->{logger}->error("ERROR:  Cannot rename $oldname to $newname ($os_error)"); };
			    $outcome = 0 if $num == 0;
			}
			if ($logfile_needs_reopen) {
			    eval { $appender->file_open(); };
			    if ($@) {
				chomp $@;
				print { $self->{logfh} } "ERROR:  Caught exception while trying to open the logfile:  $@\n"
				  if defined fileno $self->{logfh};
				## We can't sensibly output to a logfile which is not open.
				## $self->{logger}->error("ERROR:  Caught exception while trying to open the logfile:  $@");
				$outcome = 0;
			    }
			}
		    }
		    $newname = $oldname;
		}
	    }
	    else {
		## We could flush before truncation if we wanted to, but for now we choose not to.
		# my $buffer_appender = Log::Log4perl->appender_by_name('Buffer');
		# $buffer_appender->flush() if defined $buffer_appender;
		truncate( $appender->{fh}, 0 ) or $outcome = 0;
		## We continue on and switch (close/open) the logfile even if logically we shouldn't need to,
		## to recover from some autonomous agent possibly having deleted the logfile when we were not
		## looking in the same way that we would recover if $self->{max_logfiles_to_retain} were not 1.
	    }
	    ## Switching the logfile will close the old logfile and in so doing, release the lock we acquired
	    ## above.  So we don't need or even want an explicit unlock in this code path.  Concurrent actors
	    ## can create a new logfile as soon as the old one is renamed, but that's not an issue for us.
	    eval { $appender->file_switch( $self->{logfile} ); };
	    if ($@) {
		chomp $@;
		print { $self->{logfh} } "ERROR:  Caught exception while trying to switch the logfile:  $@\n" if defined fileno $self->{logfh};
		## Logging will die if we were unable to re-open the logfile, so we protect against that.
		eval { $self->{logger}->error("ERROR:  Caught exception while trying to switch the logfile:  $@"); };
		$outcome = 0;
	    }
	}
	else {
	    if ( not flock $appender->{fh}, LOCK_UN ) {
		my $os_error = "$!";
		$os_error .= " ($^E)" if "$^E" ne "$!";
		print { $self->{logfh} } "ERROR:  Could not unlock $self->{logfile} ($os_error)\n" if defined fileno $self->{logfh};
		$self->{logger}->error("ERROR:  Could not unlock $self->{logfile} ($os_error)");
		$outcome = 0;
	    }
	}
    }

    return $outcome;
}

# This routine is intended to be called once at the beginning of each operating cycle,
# after checking to see if the logfile should be rotated.  Whether or not the logfile
# did in fact get rotated, we want to reopen the logfile so we have a fresh handle for
# writing during this next cycle.  That will provide some measure of robustness against
# people reaching in and deleting the existing logfile.
#
# On the other hand, the logfile rotation should itself re-open the logfile if the logfile
# did get rotated, at least if given a flag to do so.
#
sub reopen_logfile {
    my $self    = shift;
    my $outcome = 1;

    if ( defined $self->{logfile} ) {
	my $appender = Log::Log4perl->appender_by_name('Logfile');

	if ( !-l $self->{logfile} && ( !-e _ || -f _ ) && -f $appender->{fh} ) {
	    eval { Log::Log4perl->appender_by_name('Logfile')->file_switch( $self->{logfile} ); };
	    if ($@) {
		chomp $@;
		print { $self->{logfh} } "Caught exception trying to reopen the logfile:  $@\n" if defined fileno $self->{logfh};
		$outcome = 0;
	    }
	}
    }

    return $outcome;
}

1;

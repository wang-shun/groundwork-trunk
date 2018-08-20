package GW::Nagios;

# Handle connections to the Nagios command pipe in a GroundWork Monitor environment.

# Copyright (c) 2011-2013 GroundWork Open Source (www.groundworkopensource.com).
# All rights reserved.  Use is subject to GroundWork commercial license terms.

# ================================================================
# Development Notes.
# ================================================================

# ================================================================
# Perl setup.
# ================================================================

use strict;
use warnings;

use Fcntl;

require Exporter;
our @ISA = ('Exporter');

our @EXPORT = qw(
);

our @EXPORT_OK = qw(
    nagios_plugin_numeric_host_status
    nagios_plugin_symbolic_host_status
    nagios_plugin_numeric_service_severity
    nagios_plugin_symbolic_service_severity
);

# Be sure to update this as changes are made to this module,
# as well as the copy in the documentation at the end of the file!
my $VERSION = '0.5.0';

# ================================================================
# Working variables.
# ================================================================

# ================================================================
# Global configuration variables, to be read from the config file.
# ================================================================

# ================================================================
# Configuration variables that perhaps ought to be migrated to
# the config file.
# ================================================================

# ================================================================
# Global working variables.
# ================================================================

my %plugin_numeric_host_status = (
    'UP'          => 0,
    'DOWN'        => 1,
    'UNREACHABLE' => 2
);

my %plugin_symbolic_host_status = (
    0 => 'UP',
    1 => 'DOWN',
    2 => 'UNREACHABLE'
);

my %plugin_numeric_service_severity = (
    'OK'       => 0,
    'WARNING'  => 1,
    'CRITICAL' => 2,
    'UNKNOWN'  => 3
);

my %plugin_symbolic_service_severity = (
    0 => 'OK',
    1 => 'WARNING',
    2 => 'CRITICAL',
    3 => 'UNKNOWN'
);

# ================================================================
# Supporting subroutines.
# ================================================================

# The new() constructor must be invoked as:
#     my $nagios = GW::Nagios->new ($command_pipe, $max_write_size, $max_wait_time);
# because if it is invoked instead as:
#     my $nagios = GW::Nagios::new ($command_pipe, $max_write_size, $max_wait_time);
# no invocant is supplied as the implicit first argument.

sub new {
    my $invocant                    = $_[0];   # implicit argument
    my $nagios_command_pipe         = $_[1];   # required argument
    my $max_command_pipe_write_size = $_[2];   # required argument
    my $max_command_pipe_wait_time  = $_[3];   # required argument

    if (!defined($nagios_command_pipe) || !defined($max_command_pipe_write_size) || !defined($max_command_pipe_wait_time)) {
	return undef;
    }

    my $class = ref($invocant) || $invocant;    # object or class name
    my $self = {
	command_pipe   => $nagios_command_pipe,
	max_write_size => $max_command_pipe_write_size,
	max_wait_time  => $max_command_pipe_wait_time
    };
    bless $self, $class;
    return $self;
}

sub catch_signal {
    my $signame = shift;

    die "Caught a SIG$signame signal!\n";
}

# We assume that setting a local alarm and putting back the previous alarm when we're done
# won't interfere with the calling program's manipulation of alarms.

sub send_messages_to_nagios {
    my $self     = shift;
    my $messages = shift;  # arrayref
    my @errors   = ();

    if ( scalar(@$messages) ) {
	## We will extend whatever encompassing timeout is in play for the duration of our
	## attempts to write to the pipe, but put back whatever remains of that timeout afterward,
	## so it can still rule the general operation of the script.  There is some truncation of
	## time to whole-second values in these computations, so that may introduce some degree of
	## inaccuracy in the overall effect.
	my $time_left  = alarm(0);
	my $start_time = time();

	# We use sysopen() and syswrite(), so we have full direct control of the i/o operations.
	#
	# We don't want to open an actual file if the expected pipe does not exist.  So we don't
	# specify the O_CREAT flag on the sysopen() call.
	#
	# We need to perform unbuffered i/o.  This helps if we ever try to exit gracefully after a
	# timeout, so buffering doesn't kick in again causing the program to re-execute a failed
	# write operation and quite likely hang again.  It should also avoid the extra overhead of
	# copying from our string into an i/o buffer.
	#
	# Note:  dealing with the Nagios command pipe is fraught with possible race conditions.
	# One of them is that we might open a file descriptor and write to it, only to block
	# during that write and have no reader ever come around to read it.  To get around that,
	# we set an alarm and a signal handler so we can break out of an otherwise infinite wait.
	# Our current approach to handling the alarm is distinctly unsophisticated:  we simply
	# report the failure to our caller, without indicating how many messages were left unsent.
	#
	# Here's a weird thing we have to cope with.  If we just return from our signal handler
	# in a non-eval context, Perl would just restart the system call on which we were hung.
	# If instead we die here and use an eval context to catch the error, then eventually finish
	# up by shutting down with a subsequent "die" or "exit", Perl tries to close the FIFO file
	# descriptor, and if we're using buffered i/o, that close will try to flush the buffer and
	# that flush won't complete until the write completes, which means the process will hang
	# again, this time outside the control of an alarm context.  If we try to send SIGTERM to
	# this process, that will only work if we don't have a signal handler in place for that
	# signal that likewise tries to die or exit.
	#
	# To reliably avoid all these problems, we must send data to Nagios only from within an
	# eval context, using unbuffered i/o (a direct syswrite() call), and die from within the
	# signal handler.
	#
	local $SIG{ALRM} = \&catch_signal;

	eval {
	    alarm($self->{max_wait_time});
	    ## We might die here either explicitly or because of a timeout and the signal
	    ## handler action.  If we get the alarm signal and die because of it, we need
	    ## not worry about resetting the alarm before exiting the eval, because it has
	    ## already expired.
	    eval {
		## The pipe might be missing because Nagios is down (or more likely, because it has
		## never been started, since the pipe survives when the nagios process stops).
		sysopen( FIFO, $self->{command_pipe}, O_WRONLY | O_APPEND | O_SYNC )
		  or die "ERROR:  Cannot open the Nagios command pipe: $!\n";
	    };
	    alarm(0);
	    if ($@) {
		chomp $@;
		die "$@\n";
	    }
	};
	if ($@) {
	    chomp $@;
	    push @errors, $@;
	}
	else {
	    # To guarantee atomicity of the pipe writes, we can write no more than PIPE_BUF
	    # bytes in a single write operation.  This avoids having the pipe reader interleave
	    # messages from multiple sources at places other than message boundaries.
	    my $first = 0;
	    my $last  = $first;
	    my $message_size;
	    my $buffer_size    = 0;
	    my $index_past_end = scalar(@$messages);
	    for ( my $index = 0 ; $index <= $index_past_end ; ++$index ) {
		if ( $index < $index_past_end ) {
		    $message_size = length( $messages->[$index] );
		}
		else {
		    $message_size = 0;
		}
		if ( $index < $index_past_end && $buffer_size + $message_size <= $self->{max_write_size} ) {
		    $buffer_size += $message_size;
		}
		else {
		    if ( $buffer_size > 0 ) {
			## The nested eval{}s protect against race conditions.
			eval {
			    alarm($self->{max_wait_time});
			    ## We might die here either explicitly or because of a timeout and the signal
			    ## handler action.  If we get the alarm signal and die because of it, we need
			    ## not worry about resetting the alarm before exiting the eval, because it has
			    ## already expired.
			    eval {
				my $bytes         = join( '', @{$messages}[ $first .. $last ] );
				my $bytes_len     = length $bytes;
				my $bytes_written = syswrite( FIFO, $bytes, $bytes_len );
				die "ERROR:  Cannot write to the Nagios command pipe: $!\n" if not defined $bytes_written;
				die "ERROR:  Short write ($bytes_written of $bytes_len bytes) to the Nagios command pipe.\n"
				  if $bytes_written != $bytes_len;
			    };
			    alarm(0);
			    if ($@) {
				chomp $@;
				die "$@\n";
			    }
			};
			if ($@) {
			    chomp $@;
			    push @errors, $@;
			    last;
			}
		    }
		    $first       = $index;
		    $buffer_size = $message_size;
		}
		$last = $index;
	    }
	    close(FIFO);
	}
	my $end_time = time();
	if ($time_left) {
	    my $time_til_alarm = $time_left - ( $end_time - $start_time );
	    alarm( $time_til_alarm > 0 ? $time_til_alarm : 1 );
	}
    }
    return \@errors;
}

# This routine is defined to return an empty string on success, and an error message on failure.
sub control_notifications {
    my $self    = shift;
    my $command = shift;
    my @errors  = ();
    if (not -p $self->{command_pipe}) {
	## The pipe might be missing because Nagios is down.
	push @errors, "ERROR:  Nagios command pipe $self->{command_pipe} is not a FIFO.";
	push @errors, "ERROR:  Cannot send $command.";
	return \@errors;
    }
    my $datetime = time();
    my $messages = [ "[$datetime] $command\n" ];
    my $errors = send_messages_to_nagios ($self, $messages);
    return $errors;
}

sub enable_notifications {
    my $self = shift;
    return control_notifications($self, 'ENABLE_NOTIFICATIONS');
}

sub disable_notifications {
    my $self = shift;
    return control_notifications($self, 'DISABLE_NOTIFICATIONS');
}

sub nagios_plugin_numeric_host_status {
    my $sev = shift;
    my $num = $plugin_numeric_host_status{$sev};
    return defined($num) ? $num : 2;
}

sub nagios_plugin_symbolic_host_status {
    my $sev = shift;
    my $sym = $plugin_symbolic_host_status{$sev};
    return defined($sym) ? $sym : 'UNREACHABLE';
}

sub nagios_plugin_numeric_service_severity {
    my $sev = shift;
    my $num = $plugin_numeric_service_severity{$sev};
    return defined($num) ? $num : 3;
}

sub nagios_plugin_symbolic_service_severity {
    my $sev = shift;
    my $sym = $plugin_symbolic_service_severity{$sev};
    return defined($sym) ? $sym : 'UNKNOWN';
}

# Internal routine for development debugging; not expected to be for general use.
sub dump_self {
    my $self = shift;
    foreach my $key (sort keys %$self) {
	print "$key => $self->{$key}";
    }
}

1;

__END__

=head1 NAME

GW::Nagios - send commands to a local Nagios command pipe

=head1 SYNOPSIS

    use GW::Nagios qw(
	nagios_plugin_numeric_severity
	nagios_plugin_symbolic_severity
    );

    my $nagios = GW::Nagios->new (
	$config->{'nagios_command_pipe'},
	$config->{'max_command_pipe_write_size'},
	$config->{'max_command_pipe_wait_time'}
    );
    if (not defined $nagios) {
	## Take evasive action, as needed.  For example:
	my $message  = 'FATAL:  This program cannot create a GW::Nagios object.';
	my $response = log_message ($message);
	log_shutdown();
	exit 1;
    }

    # Commonly needed value-translation routines.
    my $symbol = nagios_plugin_symbolic_severity(2);          # yields 'CRITICAL'
    my $number = nagios_plugin_numeric_severity('CRITICAL');  # yields 2

    # Basic method for efficiently sending a bunch of messages to Nagios.
    my $command = "PROCESS_SERVICE_CHECK_RESULT;$host;$service;$severity;$message";
    my $now = time();
    push @messages, "[$now] $command\n";
    my $errors = $nagios->send_messages_to_nagios(\@messages);

    # Convenience methods for particular Nagios commands.
    my $errors = $nagios->enable_notifications();
    my $errors = $nagios->disable_notifications();

=head1 DESCRIPTION

This module encapsulates all the specialized and unexpected tweaks that are
necessary to correctly send data to a local (same-system) Nagios command pipe.
It is designed to make such data transfer easy, while hiding all the details.

=head1 SUBROUTINES/METHODS

=over

=item new()

The C<new()> constructor accepts arguments that control how the Nagios
command pipe is accessed.

=over

=item nagios_command_pipe

Absolute pathname of the Nagios command pipe.

=item max_command_pipe_write_size

The maximum size in bytes for any single write operation to the output
command pipe.  The value chosen here must be no larger than C<PIPE_BUF>
(C<getconf -a | fgrep PIPE_BUF>) on your platform, unless you have an
absolute guarantee that no other process will ever write to the command
pipe (which is highly unlikely).

=item max_command_pipe_wait_time

The maximum time in seconds to wait for any single write to the
nagios command pipe to complete.

=back

The constructor returns an object reference which can be used to access
the methods for sending data to Nagios, as illustrated in the SYNOPSIS.

The C<new()> constructor must be invoked as:

    my $nagios = GW::Nagios->new (...);

because if it is invoked instead as:

    my $nagios = GW::Nagios::new (...);

no invocant is supplied as the implicit first argument.

=item send_messages_to_nagios(\@messages)

This method efficiently sends a bunch of queued messages to Nagios.
The returned value is an arrayref pointing to any error messages that
result; the array will be empty on success.

=item enable_notifications()

=item disable_notifications()

These are convenience methods that send corresponding commands to Nagios
to enable or disable notifications, respectively.  These routines return
an arrayref pointing to any error messages that result; the array will be
empty on success.

=back

=head1 CONFIGURATION AND ENVIRONMENT

The setup values passed to new() are typically drawn from the calling
application's own configuration file, rather than being hardcoded in
the calling application.

No environment variables will be used.

=head1 SEE ALSO

The C<GW::Bronx> module provides similar access to Nagios, but using an
NSCA-based protocol to write to the Bronx socket.  This is helpful when
you need to talk to a remote Nagios system.

=head1 LICENSE AND COPYRIGHT

Copyright (c) 2011-2013 GroundWork Open Source, Inc (http://www.gwos.com/).
All rights reserved.  Use is subject to GroundWork commercial license terms.

=head1 BUGS AND LIMITATIONS

Returning error messages from these routines somewhat violates the usual
Perl conventions wherein a successful call returns a true result and a
failed call returns a false result.  Nonetheless, we feel this design
is more useful for the calling application.

=head1 INCOMPATIBILITIES

GW:: modules are considered to be essentially beta-level design
experiments.  Their implementations are expected to be fully
production-ready, but when we gain more experience with the designs
of these modules, they will eventually be superseded by equivalent
GroundWork:: modules whose APIs might differ in incompatible ways.

=head1 DIAGNOSTICS

Errors are returned to the calling program, which must take responsibility
for noticing and logging them as needed.

=head1 DEPENDENCIES

None.

=head1 AUTHOR

GroundWork Open Source, Inc. <info |AT| groundworkopensource.com>

=head1 VERSION

0.5.0

=cut


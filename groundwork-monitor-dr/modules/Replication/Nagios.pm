package Replication::Nagios;

# Handle connections to Nagios in a GroundWork Monitor Disaster Recovery deployment.
# Copyright (c) 2010 GroundWork Open Source (www.groundworkopensource.com).
# All rights reserved.  Use is subject to GroundWork commercial license terms.

# ================================================================
# Documentation.
# ================================================================

# To do:
# (*) Provide a generic routine to send a message to the Nagios command pipe.
# (*) Provide convenience routines to support the common commands we will need,
#     such as enabling and disabling notifications.

# ================================================================
# Perl setup.
# ================================================================

use strict;
use warnings;

require Exporter;
our @ISA = ('Exporter');

# FIX THIS:  do we want instead to just allow access to these routines
# through the caller's $nagios object?
our @EXPORT = qw(
    &send_messages_to_nagios
    &enable_notifications
    &disable_notifications
);

our @EXPORT_OK = qw(
);

# This is where we'll pick up any Perl packages not in the standard Perl
# distribution, to make this a self-contained package anchored in a single
# directory.
use FindBin qw($Bin);
use lib "$Bin/perl/lib";

use Sys::Hostname;
use Replication::Logger;

# Be sure to update this as changes are made to this module!
my $VERSION = '0.1.0';

# ================================================================
# Working variables.
# ================================================================

# ================================================================
# Global configuration variables, to be read from the config file.
# ================================================================

# Absolute pathname of the Nagios command pipe.
my $nagios_command_pipe = undef;

# The maximum size in bytes for any single write operation to the output command pipe.
# The value chosen here must be no larger than PIPE_BUF (getconf -a | fgrep PIPE_BUF)
# on your platform, unless you have an absolute guarantee that no other process will
# ever write to the command pipe (which is highly unlikely).
my $max_command_pipe_write_size = undef; 

# The maximum time in seconds to wait for any single write to the nagios command pipe
# to complete.
my $max_command_pipe_wait_time = undef;

# ================================================================
# Configuration variables that perhaps ought to be migrated to
# the config file.
# ================================================================

# ================================================================
# Global working variables.
# ================================================================

my $nagios = undef;

# ================================================================
# Supporting subroutines.
# ================================================================

# The new() constructor must be invoked as:
#     my $nagios = Replication::Nagios->new ($command_pipe, $max_write_size, $max_wait_time);
# because if it is invoked instead as:
#     my $nagios = Replication::Nagios::new ($command_pipe, $max_write_size, $max_wait_time);
# no invocant is supplied as the implicit first argument.

sub new {
    my $invocant                 = $_[0];   # implicit argument
    $nagios_command_pipe         = $_[1];   # required argument
    $max_command_pipe_write_size = $_[2];   # required argument
    $max_command_pipe_wait_time  = $_[3];   # required argument

    if (!defined($nagios_command_pipe) || !defined($max_command_pipe_write_size) || !defined($max_command_pipe_wait_time)) {
        return undef;
    }

    my $class = ref($invocant) || $invocant;    # object or class name
    # Options are stored in our object hash to prepare for the day when
    # we allow more than one such object in the program.  These copies
    # are not yet referenced later on, though.
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
# won't interfere with the POE framework's manipulation of alarms.

sub send_messages_to_nagios {
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

	# We don't want to open an actual file if the expected pipe does not exist.  The workaround is
	# this strange '+<" open mode that allows us write access, but won't create a nonexisting file.
	# That mode turns out to also have a secondary beneficial effect:  it causes the pipe to also
	# have a reader on the pipe even though we never read from it.  And that allows the pipe to be
	# opened immediately even if there is currently no other process reading the pipe, instead of
	# having the open() hang indefinitely waiting for a reader to show up, as would happen if the
	# pipe were opened with an O_WRONLY mode or an O_WRONLY|O_APPEND mode.  This allows us to get
	# past this point even if Nagios is currently down, without needing to encapsulate the open()
	# call within a timeout mechanism as we do for the print() call below.  Note that if Nagios
	# does not currently have the pipe open, then when we close the pipe, everything we wrote to
	# it will be flushed and not later read by Nagios when it does open the pipe for reading.  So
	# data in that circumstance is dropped rather than queued.
	#
	# The :unix discipline says we should perform unbuffered i/o.  This helps if we ever try to exit
	# gracefully after a timeout, so buffering doesn't kick in again causing the program to re-execute
	# a failed write operation and quite likely hang again.  It should also avoid the extra overhead
	# of copying from our string into an i/o buffer.
	#
	if ( !open( FIFO, '+<:unix', $nagios_command_pipe ) ) {
	    push @errors, "Could not open the Nagios command pipe: $!";
	}
	else {
	    ## Note:  dealing with the Nagios command pipe is fraught with possible race conditions.
	    ## One of them is that we might open a file descriptor and write to it, only to block
	    ## during that write and have no reader ever come around to read it.  To get around that,
	    ## we set an alarm so we can break out of an otherwise infinite wait.  Our current approach
	    ## to handling the alarm is distinctly unsophisticated:  we simply report the failure to
	    ## our caller, without indicating how many messages were left unsent.
	    ##
	    ## Here's a weird thing we have to cope with.  If we just return from our signal handler
	    ## in a non-eval context, Perl would just restart the system call on which we were hung.
	    ## If instead we die here and use an eval context to catch the error, then eventually finish
	    ## up by shutting down with a subsequent "die" or "exit", Perl tries to close the FIFO file
	    ## descriptor, and if we're using buffered i/o, that close will try to flush the buffer and
	    ## that flush won't complete until the write completes, which means the process will hang
	    ## again, this time outside the control of an alarm context.  If we try to send SIGTERM to
	    ## this process, that will only work if we don't have a signal handler in place for that
	    ## signal that likewise tries to die or exit.
	    ##
	    ## To reliably avoid all these problems, we must send data to Nagios only from within an eval
	    ## context, using unbuffered i/o (the :unix discipline), and die from within the signal handler.
	    ##
	    local $SIG{ALRM} = \&catch_signal;

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
		if ( $index < $index_past_end && $buffer_size + $message_size <= $max_command_pipe_write_size ) {
		    $buffer_size += $message_size;
		}
		else {
		    if ( $buffer_size > 0 ) {
			## The nested eval{}s protect against race conditions.
			eval {
			    alarm($max_command_pipe_wait_time);
			    ## We might die here either explicitly or because of a timeout and the signal
			    ## handler action.  If we get the alarm signal and die because of it, we need
			    ## not worry about resetting the alarm before exiting the eval, because it has
			    ## already expired.
			    eval {
				print FIFO join( '', @{$messages}[ $first .. $last ] )
				  or die "ERROR:  Cannot write to the Nagios command pipe: $!";
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
    my $command = shift;
    if (not -p $nagios_command_pipe) {
	log_timed_message "ERROR:  Nagios command pipe $nagios_command_pipe is not a FIFO.";
	log_timed_message "ERROR:  Cannot send $command.";
	return            "ERROR:  Nagios command pipe $nagios_command_pipe is not a FIFO.";
    }
    my $datetime = time();
    my $messages = [ "[$datetime] $command\n" ];
    my $errors = send_messages_to_nagios ($messages);
    foreach my $error (@$errors) {
	log_timed_message "ERROR:  failed to write to Nagios command pipe:  $error";
    }
    return @$errors ? "ERROR:  failed to write to Nagios command pipe: \n$errors->[0]" : '';
}

sub enable_notifications {
    return control_notifications('ENABLE_NOTIFICATIONS');
}

sub disable_notifications {
    return control_notifications('DISABLE_NOTIFICATIONS');
}

# Internal routine for debugging; not expected to be for general use.
sub log_nagios {
    foreach my $key (sort keys %$nagios) {
        log_message "$key => $nagios->{$key}";
    }
}

1;

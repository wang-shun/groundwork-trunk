package GW::Foundation;

# Handle connections to the Foundation database in a GroundWork Monitor
# application deployment.
#
# Copyright (c) 2012-2013 GroundWork, Inc. (www.gwos.com).  All rights reserved.
# Use is subject to GroundWork commercial license terms.

# ================================================================
# Development Notes.
# ================================================================

# To do:
# (*) Add SIGPIPE handling and automatic retries to socket writes here.
# (*) Add various convenience routines for formatting messages of particular types.
# (*) Generalize and better integrate with the types of environments in which we
#     have called old versions of this package and similar independent routines.
# (*) Support sending to multiple separate Foundation instances instead of just one.
#     Possibly, that might be done simply by having the calling application create
#     a new module instance, and using the proper instance for the intended target.

# ================================================================
# Perl setup.
# ================================================================

use strict;
use warnings;

require Exporter;
our @ISA = ('Exporter');

our @EXPORT = qw(
    SEVERITY_OK
    SEVERITY_WARNING
    SEVERITY_CRITICAL
    SEVERITY_UNKNOWN
    APP_SYSTEM
    APP_NAGIOS
    APP_SNMPTRAP
    APP_SYSLOG
    APP_AUDIT
    APP_ARCHIVE
);

our @EXPORT_OK = qw(
);

use IO::Socket;
use GW::Logger qw( :DEFAULT log_only_to_file );

# Be sure to update this as changes are made to this module!
my $VERSION = '2.0.5';

# ================================================================
# Global configuration variables.
# ================================================================

# Socket timeout (in seconds), to address GWMON-7407.  Typical value is 60.  Set to 0 to disable.
#
# This timeout is here only for use in emergencies, when Foundation has completely frozen up and is no
# longer reading (will never read) a socket we have open.  We don't want to set this value so low that
# it will interfere with normal communication, even given the fact that Foundation may wait a rather
# long time between sips from this straw as it processes a large bundle of messages that we sent it, or
# is otherwise busy and just cannot get back around to reading the socket in a reasonably short time.
#
my $default_socket_send_timeout = 60;

# ================================================================
# Global constants.
# ================================================================

# Severity codes:
use constant SEVERITY_OK       => 'OK';
use constant SEVERITY_WARNING  => 'WARNING';
use constant SEVERITY_CRITICAL => 'CRITICAL';
use constant SEVERITY_UNKNOWN  => 'UNKNOWN';

# Application types:
use constant APP_SYSTEM   => 'SYSTEM';
use constant APP_NAGIOS   => 'NAGIOS';
use constant APP_SNMPTRAP => 'SNMPTRAP';
use constant APP_SYSLOG   => 'SYSLOG';
use constant APP_AUDIT    => 'AUDIT';
use constant APP_ARCHIVE  => 'ARCHIVE';

# ================================================================
# Supporting subroutines.
# ================================================================

# The new() constructor must be invoked as:
#     my $foundation = GW::Foundation->new ();
# because if it is invoked instead as:
#     my $foundation = GW::Foundation::new ();
# no invocant is supplied as the implicit first argument.

# Note: The $monitor_server_ip_address passed to the constructor will be used in generated
# messages.  It is passed as an argument so it can reflect the application context rather
# than being dependent on some locally-derived address (e.g., hostname()'s perhaps
# arbitrary selection of some NIC interface that might not match what the application is
# actually running on for its communication purposes outside of talking to Foundation).

# A calling application which is not picky about selecting a particular NIC for
# reporting purposes, on a possibly multi-hosted machine, might do something
# like the following:
#
#     use Sys::Hostname;
#     my $my_qualified_hostname     = hostname();
#     my $my_dotted_address         = gethostbyname($my_qualified_hostname);
#     my $monitor_server_ip_address = inet_ntoa($my_dotted_address);
#
# On the other hand, you might just want to use 'localhost' for $monitor_server_hostname; it
# is supposed to reflect the server from which the sent messages originate, and need not be
# the same as the $my_qualified_hostname from which the value for $monitor_server_ip_address
# is derived.

sub new {
    my $invocant                  = $_[0];    # implicit argument
    my $foundation_host           = $_[1];    # required argument
    my $foundation_port           = $_[2];    # required argument
    my $monitor_server_hostname   = $_[3];    # required argument
    my $monitor_server_ip_address = $_[4];    # required argument
    my $socket_send_timeout       = $_[5];    # optional argument
    my $send_buffer_size          = $_[6];    # optional argument; for future compatibility
    my $debug                     = $_[7];    # optional argument

    $socket_send_timeout = $default_socket_send_timeout if not defined $socket_send_timeout;
    $send_buffer_size    = 0 if not defined $send_buffer_size;

    my $log_as_utf8 = 0;  # Set to 0 to log Foundation messages as ISO-8859-1, to 1 to log as UTF-8.

    die "ERROR:  Invalid foundation_host passed to GW::Foundation->new().\n"           if !defined($foundation_host);
    die "ERROR:  Invalid foundation_port passed to GW::Foundation->new().\n"           if !defined($foundation_port);
    die "ERROR:  Invalid monitor_server_hostname passed to GW::Foundation->new().\n"   if !defined($monitor_server_hostname);
    die "ERROR:  Invalid monitor_server_ip_address passed to GW::Foundation->new().\n" if !defined($monitor_server_ip_address);

    my $class = ref($invocant) || $invocant;    # object or class name
    ## Options are stored in our object hash to prepare for the day when we allow more than
    ## one such object in the program (say, to talk to both a primary and a standby server,
    ## or both a child and a parent server).
    my $self = {
	foundation_host           => $foundation_host,
	foundation_port           => $foundation_port,
	monitor_server_hostname   => $monitor_server_hostname,
	monitor_server_ip_address => $monitor_server_ip_address,
	socket_send_timeout       => $socket_send_timeout,
	send_buffer_size          => $send_buffer_size,
	packet_count              => 0,
	log_as_utf8               => $log_as_utf8,
	debug                     => $debug
    };
    bless $self, $class;
    return $self;
}

# Internal routine only.
sub time_text {
    my $timestamp = shift;
    if ( $timestamp <= 0 ) {
	return 'none';
    }
    else {
	my ( $seconds, $minutes, $hours, $day_of_month, $month, $year, $wday, $yday, $isdst ) = localtime($timestamp);
	return sprintf "%04d-%02d-%02d %02d:%02d:%02d", $year + 1900, $month + 1, $day_of_month, $hours, $minutes, $seconds;
    }
}

sub die_on_signal {
    my $signame = shift;
    die "Caught a SIG$signame signal.\n";
}

# Internal routine only.
sub xml_encode {
    my $str = shift;
    if ( defined $str ) {
	$str =~ s/&/&amp;/g;
	$str =~ s/"/&quot;/g;
	$str =~ s/'/&apos;/g;
	$str =~ s/</&lt;/g;
	$str =~ s/>/&gt;/g;
    }
    return $str;
}

sub send_xml_messages {
    my $self         = $_[0];    # implicit argument
    my $xml_messages = $_[1];    # required argument

    # We do this final encapsulation and join work outside of the period when the socket
    # is open, to limit the time that Foundation has to wonder what's happening with it
    # and possibly time out and unceremoniously close it from the other end.
    my $full_message = join( "\n", @$xml_messages, '<SERVICE-MAINTENANCE command="close" />' );

    my $status = 1;
    my $socket = undef;

    # The nested eval{};s and the multiple alarm(0); calls protect against race conditions amongst
    # all the possible signals and timings we might encounter.
    eval {
	## We assume no enclosing code has an alarm in play, so we don't bother to save any existing alarm timeout
	## here before we establish our own timeout below, nor to replace any remaining interval after our interlude.
	local $SIG{ALRM} = \&die_on_signal;

	eval {
	    local $SIG{PIPE} = 'IGNORE';

	    # We impose an absolute timeout on the sending of the XML messages, because we don't necessarily
	    # trust that the SO_SNDTIMEO option alone will do the trick.  That option operates at a low level,
	    # and just interrupts the low-level i/o call, telling the calling code that the operation has taken
	    # too long and that a partial (or no) write has taken place so far.  The Perl I/O library is free
	    # to restart the intended i/o at that point, to still try to get all the data transferred, which
	    # would not achieve our intended application-level effect of ensuring that we abort the entire i/o
	    # action if it takes too long.  This absolute timeout also covers the case where we cannot connect
	    # to Foundation at all, but that failure is not immediately recognized by the network layers, which
	    # can happen if the socket is closed on the server side or is otherwise inaccessible.
	    #
	    # An alarm interval which is twice the configured SO_SNDTIMEO socket option timeout seems safe.
	    # It should allow the configured timeout (socket_send_timeout) to take precedence, and only serve
	    # as a fallback if that turns out to be inadequate (or if we hang in the connection attempt, before
	    # we can put the SO_SNDTIMEO timeout in place).
	    alarm($self->{socket_send_timeout} * 2);

	    $socket = IO::Socket::INET->new(
		PeerAddr => $self->{foundation_host},
		PeerPort => $self->{foundation_port},
		Proto    => 'tcp',
		Type     => SOCK_STREAM
	    );
	    if ( !$socket ) {
		alarm(0);
		$status = 0;
		die "cannot open TCP socket $self->{foundation_port}.\n";
	    }
	    if ( !$socket->sockopt( SO_SNDTIMEO, pack( 'L!L!', $self->{socket_send_timeout}, 0 ) ) ) {
		alarm(0);
		close($socket);
		$status = 0;
		die "cannot set send timeout on socket.\n";
	    }
	    $status = 0 if not print $socket $full_message;
	    $status = 0 if not close $socket;
	};
	alarm(0);
	if ($@) {
	    chomp $@;
	    die "$@\n";
	}
    };
    if (my $exception = $@) {
	chomp $exception;
	log_timed_message "ERROR:  While writing message XML to $self->{foundation_host} Foundation:  $exception";
	## FIX MINOR:  This close() might restart a write from the Perl i/o buffering layer,
	## if it sees that not all the buffered data got written out previously, thus perhaps
	## leading to another hang, this one not timed out with an alarm.  We might need to
	## use the :unix discipline on the open() call (unbuffered i/o), or syswrite() and
	## our own interrupted-system-call and partial-write handling, to prevent that.
	## We at least log a message before closing the socket, so there is some evidence
	## in the log as to what is going on if it hangs in the close().
	close $socket if defined $socket;  # avoid a possible resource leak
	## We tag this as an ERROR instead of a NOTICE, even if the cause of the exception was a polite
	## shutdown request, because the intended message probably didn't get out to Foundation.
	## We avoid returning the same error message we already just printed out, as that would
	## be redundant, though we really have nothing more to add in another message.
	return ["ERROR:  Send to Foundation probably failed."];
    }

    # The lack of an error message being produced here does not necessarily mean that the
    # message was actually delivered.  See our earlier comments on SIGPIPE handling.
    return ($status ? undef : ["ERROR:  Cannot send message to Foundation on $self->{foundation_host}:  write failed."]);
}

sub send_log_message {
    my $self     = $_[0];       # implicit argument
    my $severity = $_[1];       # required argument, one of SEVERITY_OK, SEVERITY_WARNING, SEVERITY_CRITICAL, SEVERITY_UNKNOWN
    my $apptype  = $_[2];       # required argument (string; Application Type [e.g., APP_SYSTEM, APP_NAGIOS, APP_SNMPTRAP, APP_SYSLOG])
    my @message  = @_[3..$#_];  # required argument(s)

    my $message = join('', @message);

    $message =~ s/\n/ /g;
    $message =~ s/<br>/ /ig;
    $message = xml_encode($message);

    my @xml_message = ();

    # Our working assumptions, set up elsewhere:
    #
    #   In the GWCollageDB.ConsolidationCriteria table:
    #   +-------------------------+--------+------------------------------------------------------------------+
    #   | ConsolidationCriteriaID | Name   | Criteria                                                         |
    #   +-------------------------+--------+------------------------------------------------------------------+
    #   |                       2 | SYSTEM | OperationStatus;Device;MonitorStatus;ApplicationType;TextMessage |
    #   +-------------------------+--------+------------------------------------------------------------------+

    push @xml_message, '<GENERICLOG consolidation="SYSTEM" ';		# Standard consolidation setup is enabled.
    push @xml_message, "ApplicationType=\"$apptype\" ";			# Classification of the calling application.
    push @xml_message, "MonitorServerName=\"$self->{monitor_server_hostname}\" ";

    # Device should always be IP everywhere (FIX LATER:  not sure what that means)
    push @xml_message, "Device=\"$self->{monitor_server_ip_address}\" ";

    push @xml_message, "Severity=\"$severity\" ";
    push @xml_message, "MonitorStatus=\"$severity\" ";
    push @xml_message, "ReportDate=\"" . time_text(time) . "\" ";	# set ReportDate to current local time
    push @xml_message, "TextMessage=\"$message\" ";
    push @xml_message, "/>";

    my $xml_message = join( '', @xml_message );

    my @xml_messages = ();
    push @xml_messages, $xml_message;

    my $errors = send_xml_messages($self, \@xml_messages);

    return $errors;
}

sub local_service_xml {
    ## We don't xml_encode fields that we don't expect to ever contain any troublesome characters,
    ## to avoid wasting precious CPU resources.
    my $self            = $_[0];                 # implicit argument
    my $host            = $_[1];
    my $svcdesc         = $_[2];
    my $configlabel     = xml_encode( $_[3] );
    my $rrdpath         = $_[4];
    my $rrdgraphcommand = xml_encode( $_[5] );

    return
"<Service Host='$host' ServiceDescription='$svcdesc' RRDLabel='$configlabel' RRDPath='$rrdpath' RRDCommand='$rrdgraphcommand' RemoteRRDCommand=' ' />\n";
}

sub remote_service_xml {
    ## We don't xml_encode fields that we don't expect to ever contain any troublesome characters,
    ## to avoid wasting precious CPU resources.
    my $self        = $_[0];                 # implicit argument
    my $host        = $_[1];
    my $svcdesc     = $_[2];
    my $configlabel = xml_encode( $_[3] );
    my $child_host  = $_[4];

    return
"<Service Host='$host' ServiceDescription='$svcdesc' RRDLabel='$configlabel' RRDPath=' ' RRDCommand=' ' RemoteRRDCommand='$child_host' />\n";
}

sub write_command_xml {
    my $self       = $_[0];    # implicit argument
    my $action     = $_[1];    # required argument; typically 'MODIFY'
    my $apptype    = $_[2];    # required argument (string; Application Type [e.g., APP_SYSTEM, APP_NAGIOS, APP_SNMPTRAP, APP_SYSLOG])
    my $xml_string = $_[3];    # required argument

    return 1 if $xml_string eq '';    # Nothing to do ...

    my $status = 1;
    my $socket = undef;

    # The nested eval{};s and the multiple alarm(0); calls protect against race conditions amongst
    # all the possible signals and timings we might encounter.
    eval {
	## We assume no enclosing code has an alarm in play, so we don't bother to save any existing alarm timeout
	## here before we establish our own timeout below, nor to replace any remaining interval after our interlude.
	local $SIG{ALRM} = \&die_on_signal;

	eval {
	    local $SIG{PIPE} = 'IGNORE';

	    # An alarm interval which is twice the configured SO_SNDTIMEO socket option timeout seems safe.
	    # It should allow the configured timeout to take precedence, and only serve as a fallback if
	    # that turns out to be inadequate.
	    alarm($self->{socket_send_timeout} * 2);

	    # Open connection to Foundation (or not).
	    my $max_connect_attempts = 3;
	    for ( my $i = 0 ; $i <= $max_connect_attempts ; ++$i ) {
		if ( $i == $max_connect_attempts ) {
		    ## No listener socket is available.
		    alarm(0);
		    log_timed_message "ERROR:  Could not connect to $self->{foundation_host}:$self->{foundation_port} ($!).";
		    return 0;
		}
		else {
		    $socket = IO::Socket::INET->new(
			PeerAddr => $self->{foundation_host},
			PeerPort => $self->{foundation_port},
			Proto    => 'tcp',
			Type     => SOCK_STREAM
		    );
		    if ($socket) {
			log_timed_message "DEBUG:  Opened socket to Foundation on $self->{foundation_host}." if $self->{debug};
			$socket->autoflush();
			last if $socket->sockopt(SO_SNDTIMEO, pack('L!L!', $self->{socket_send_timeout}, 0));
			log_timed_message "ERROR:  Cannot send message to Foundation on $self->{foundation_host}:  cannot set send timeout on socket ($!).";
			close($socket);
			$socket = undef;  # subject to race condition during interrupt, but still appropriate
		    }
		    ## We can no longer "sleep 1;" here because this block is wrapped in alarm() processing.
		    ## So we need to invoke a different type of delay that doesn't use SIGALRM.  A system timer
		    ## accessed by POSIX::RT::Timer would be another possibility, though if we were willing to
		    ## use that package, we would probably replace the alarm() processing, not the sleep().
		    select undef, undef, undef, 1;
		}
	    }
	    ++$self->{packet_count};
	    my $xml_out =
qq(<Adapter Session='$self->{packet_count}' AdapterType='SystemAdmin'>
<Command Action='$action' ApplicationType='$apptype'>
$xml_string
</Command>
</Adapter>
);
	    log_only_to_file $xml_out if $self->{debug} && !$self->{log_as_utf8};
	    utf8::encode($xml_out);
	    ## FIX LATER:  We might log details of socket-write failures here.
	    $status = 0 if not print $socket $xml_out;
	    log_only_to_file $xml_out if $self->{debug} && $self->{log_as_utf8};
	    $status = 0 if not print $socket '<SERVICE-MAINTENANCE command="close" />';
	    log_only_to_file "<SERVICE-MAINTENANCE command='close' />\n" if $self->{debug};
	    $status = 0 if not close $socket;
	};
	alarm(0);
	if ($@) {
	    chomp $@;
	    die "$@\n";
	}
    };
    if ($@) {
	chomp $@;
	## We tag this as an ERROR instead of a NOTICE, even if the cause of the exception was a polite
	## shutdown request, because the intended command probably didn't get out to Foundation.
	log_timed_message "ERROR:  While writing command XML to $self->{foundation_host} Foundation:  $@";
	## FIX MINOR:  This close() might restart a write from the Perl i/o buffering layer,
	## if it sees that not all the buffered data got written out previously, thus perhaps
	## leading to another hang, this one not timed out with an alarm.  We might need to
	## use the :unix discipline on the open() call (unbuffered i/o), or syswrite() and
	## our own interrupted-system-call and partial-write handling, to prevent that.
	## We at least log a message before closing the socket, so there is some evidence
	## in the log as to what is going on if it hangs in the close().
	close $socket if defined $socket;  # avoid a possible resource leak
	$status = 0;
    }

    return $status;
}

1;

__END__

# Set up to handle broken pipe errors, so this process does not abruptly die without logging
# anything as to the circumstances that caused its downfall.  (The default SIGPIPE action is to
# terminate the process.)  This has to be done in conjunction with later code that will cleanly
# process an EPIPE return code from a socket write.
#
# Ignoring SIGPIPE turns SIGPIPE signals generated when we write to sockets already closed by the
# server into EPIPE errors returned from the write operations.  Note that because of the manner in
# which sockets work (data transfer is mediated by the kernel, introducing asynchronicity into the
# transport), a closed socket typically will not be seen by the sending application as an error
# code until the *second* write following the closure of the socket by the receiving end.  The
# first write after the socket is closed on the reading side succeeds, because at that point the
# process just writes to the kernel, and that succeeds, and the write returns to the caller.  It
# is only then that the kernel tries to communicate with the other side, the transfer fails, and
# the socket is marked locally as being remotely closed.  A second write at that time then fails,
# because the socket closure is now immediately apparent when the application tries to transfer
# data to the kernel buffer.  This means that late-occurring closures on the reading side might
# not be seen on the writing side as a data-transfer failure, even in the close() return code when
# the socket is closed after the last write operation.  The only way to fully close the loop at
# the application level so the application can be assured that full data transfer did occur is
# for the receiving application to push an application-level acknowledgement back to the original
# sender.  And currently, our Foundation socket API does not do so.
$SIG{PIPE} = 'IGNORE';


package GW::Foundation;

# Handle connections to the Foundation database in a GroundWork Monitor
# application deployment.
#
# Copyright (c) 2011 GroundWork Open Source (www.groundworkopensource.com).
# All rights reserved.  Use is subject to GroundWork commercial license terms.

# ================================================================
# Development Notes.
# ================================================================

# To do:
# (*) Add SIGPIPE handling and automatic retries to socket writes here.
# (*) Extend send_xml_messages() to check the return values when printing
#     to and closing a socket, to affect the routine's own return value.
# (*) Add various convenience routines for formatting messages of particular types.

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
);

our @EXPORT_OK = qw(
);

use IO::Socket;
use GW::Logger qw( :DEFAULT log_only_to_file );

# Be sure to update this as changes are made to this module!
my $VERSION = '2.0.1';

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

sub send_xml_messages {
    my $self         = $_[0];    # implicit argument
    my $xml_messages = $_[1];    # required argument

    # We do this final encapsulation and join work outside of the period when the socket
    # is open, to limit the time that Foundation has to wonder what's happening with it
    # and possibly time out and unceremoniously close it from the other end.
    # FIX MINOR:  append the close in the join, instead of altering the passed-in array
    push @$xml_messages, '<SERVICE-MAINTENANCE command="close" />';
    my $full_message = join( "\n", @$xml_messages );

    my $socket = IO::Socket::INET->new(
	PeerAddr => $self->{foundation_host},
	PeerPort => $self->{foundation_port},
	Proto    => 'tcp',
	Type     => SOCK_STREAM
    );
    if ( !$socket ) {
	return [
	    "ERROR:  Cannot send message to Foundation on $self->{foundation_host}:  cannot open TCP socket $self->{foundation_port}."
	];
    }
    if ( !$socket->sockopt( SO_SNDTIMEO, pack( 'L!L!', $self->{socket_send_timeout}, 0 ) ) ) {
	close($socket);
	return ["ERROR:  Cannot send message to Foundation on $self->{foundation_host}:  cannot set send timeout on socket."];
    }
    print $socket $full_message;
    close($socket);

    return undef;
}

sub send_log_message {
    my $self     = $_[0];       # implicit argument
    my $severity = $_[1];       # required argument, one of SEVERITY_OK, SEVERITY_WARNING, SEVERITY_CRITICAL, SEVERITY_UNKNOWN
    my $apptype  = $_[2];       # required argument (string; Application Type [e.g., APP_SYSTEM, APP_NAGIOS, APP_SNMPTRAP, APP_SYSLOG])
    my @message  = @_[3..$#_];  # required argument(s)

    my $message = join('', @message);

    $message =~ s/\n/ /g;
    $message =~ s/<br>/ /ig;
    $message =~ s/&/&amp;/g;
    $message =~ s/"/&quot;/g;
    $message =~ s/'/&#39;/g;	# &apos; is the XML entity, but historically it's not also an HTML entity and IE won't interpret it
    $message =~ s/</&lt;/g;
    $message =~ s/>/&gt;/g;

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
    my $self            = $_[0];    # implicit argument
    my $host            = $_[1];
    my $svcdesc         = $_[2];
    my $configlabel     = $_[3];
    my $rrdpath         = $_[4];
    my $rrdgraphcommand = $_[5];

    $configlabel =~ s/&/&amp;/g;
    $configlabel =~ s/"/&quot;/g;
    $configlabel =~ s/'/&#39;/g;	# &apos; is the XML entity, but historically it's not also an HTML entity and IE won't interpret it
    $configlabel =~ s/</&lt;/g;
    $configlabel =~ s/>/&gt;/g;

    $rrdgraphcommand =~ s/&/&amp;/g;
    $rrdgraphcommand =~ s/"/&quot;/g;
    $rrdgraphcommand =~ s/'/&apos;/g;	# This won't be treated as HTML, so we use the XML entity.
    $rrdgraphcommand =~ s/</&lt;/g;
    $rrdgraphcommand =~ s/>/&gt;/g;

    return
"<Service Host='$host' ServiceDescription='$svcdesc' RRDLabel='$configlabel' RRDPath='$rrdpath' RRDCommand='$rrdgraphcommand' RemoteRRDCommand=' ' />\n";
}

sub remote_service_xml {
    my $self        = $_[0];    # implicit argument
    my $host        = $_[1];
    my $svcdesc     = $_[2];
    my $configlabel = $_[3];
    my $child_host  = $_[4];

    $configlabel =~ s/&/&amp;/g;
    $configlabel =~ s/"/&quot;/g;
    $configlabel =~ s/'/&#39;/g;	# &apos; is the XML entity, but historically it's not also an HTML entity and IE won't interpret it
    $configlabel =~ s/</&lt;/g;
    $configlabel =~ s/>/&gt;/g;

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

    # Open connection to Foundation (or not).
    my $max_connect_attempts = 3;
    for ( my $i = 0 ; $i <= $max_connect_attempts ; ++$i ) {
	if ( $i == $max_connect_attempts ) {
	    ## No listener socket is available.
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
	    }
	    sleep 2;
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
    $status = 0 if not print $socket "<SERVICE-MAINTENANCE command='close' />";
    log_only_to_file "<SERVICE-MAINTENANCE command='close' />\n" if $self->{debug};
    $status = 0 if not close $socket;
    return $status;
}

1;

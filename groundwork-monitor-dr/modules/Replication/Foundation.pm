package Replication::Foundation;

# Handle connections to the Foundation database in a GroundWork Monitor
# Disaster Recovery deployment.
# Copyright (c) 2010 GroundWork Open Source (www.groundworkopensource.com).
# All rights reserved.  Use is subject to GroundWork commercial license terms.

# ================================================================
# Documentation.
# ================================================================

# To do:
# (*) ...

# ================================================================
# Perl setup.
# ================================================================

use strict;
use warnings;

require Exporter;
our @ISA = ('Exporter');

# FIX THIS:  we mean to allow access to send_message only through the caller's
# $foundation object; should we just delete it from @EXPORT ?
    # &send_message
our @EXPORT = qw(
    SEVERITY_OK
    SEVERITY_WARNING
    SEVERITY_CRITICAL
    SEVERITY_UNKNOWN
    REPLICATION_ENGINE
);

our @EXPORT_OK = qw(
);

# This is where we'll pick up any Perl packages not in the standard Perl
# distribution, to make this a self-contained package anchored in a single
# directory.
use FindBin qw($Bin);
use lib "$Bin/perl/lib";

use Sys::Hostname;
use IO::Socket;
use Replication::Logger;

# Be sure to update this as changes are made to this module!
my $VERSION = '0.1.0';

# ================================================================
# Working variables.
# ================================================================

# FIX THIS:  perhaps some of these configuration values should be
# drawn instead from the calling application's configuration setup
my $remote_host         = 'localhost';
my $remote_port         = 4913;
my $thisnagios          = 'localhost';
my $socket_send_timeout = 30;  # seconds; to address GWMON-7407; set to 0 to disable

my $qualified_hostname   = undef;
my $unqualified_hostname = undef;
my $ipaddress            = undef;

my $service = 'Replication';

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

my $foundation = undef;

# Severity codes;
use constant SEVERITY_OK       => 'OK';
use constant SEVERITY_WARNING  => 'WARNING';
use constant SEVERITY_CRITICAL => 'CRITICAL';
use constant SEVERITY_UNKNOWN  => 'UNKNOWN';

# Names of replication components that might need to log messages,
# specified here as symbols to be used for sending messages, so we
# get consistent spelling throughout the application:
use constant REPLICATION_ENGINE => 'Replication Engine';

# ================================================================
# Supporting subroutines.
# ================================================================

# The new() constructor must be invoked as:
#     my $foundation = Replication::Foundation->new ();
# because if it is invoked instead as:
#     my $foundation = Replication::Foundation::new ();
# no invocant is supplied as the implicit first argument.

sub new {
    my $invocant = $_[0];   # implicit argument
    # $fdn_arg   = $_[1];   # required argument

    # FIX MINOR:  The $ipaddress computed here will be used in generated messages;
    # as such, it should probably be derived from an argument to new() to reflect
    # the application context rather than being dependent on hostname()'s perhaps
    # arbitrary selection of some NIC interface that might not match what the
    # application is actually running on for its communication purposes outside of
    # talking to Foundation.
    # FIX THIS:  this stuff should be moved to a Replication::Host package or somesuch
    $qualified_hostname    = hostname();
    ($unqualified_hostname = $qualified_hostname) =~ s/\..*//;
    my $dotted_address     = gethostbyname($qualified_hostname);
    $ipaddress             = inet_ntoa($dotted_address);

    my $class = ref($invocant) || $invocant;    # object or class name
    # Options are stored in our object hash to prepare for the day when
    # we allow more than one such object in the program.  These copies
    # are not yet referenced later on, though.
    my $self = {
    };
    bless $self, $class;
    return $self;
}

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
    my $xml_messages = shift;

    # We do this final encapsulation and join work outside of the period when the socket
    # is open, to limit the time that Foundation has to wonder what's happening with it
    # and possibly time out and unceremoniously close it from the other end.
    push @$xml_messages, '<SERVICE-MAINTENANCE command="close" />';
    my $full_message = join( "\n", @$xml_messages );

    my $socket = IO::Socket::INET->new( PeerAddr => $remote_host, PeerPort => $remote_port, Proto => 'tcp', Type => SOCK_STREAM );
    if (!$socket) {
	return [ "ERROR:  Cannot send message to Foundation:  cannot open TCP socket $remote_port to host $remote_host." ];
    }
    if (! $socket->sockopt(SO_SNDTIMEO, pack('L!L!', $socket_send_timeout, 0))) {
	close($socket);
	return [ "ERROR:  Cannot send message to Foundation:  cannot set send timeout on socket." ];
    }
    print $socket $full_message;
    close($socket);

    # FIX THIS
    # print $full_message, "\n" if $debug;

    return undef;
}

sub send_message {
    my $invocant = $_[0];       # implicit argument
    my $severity = $_[1];       # required argument, one of SEVERITY_OK, SEVERITY_WARNING, SEVERITY_CRITICAL, SEVERITY_UNKNOWN
    my $msgtype  = $_[2];       # required argument (string; identifies the component which needs to log the message)
    my @message  = @_[3..$#_];  # required argument(s)

    my $message = join('', @message);

    $message =~ s/\n/ /g;
    $message =~ s/<br>/ /ig;
    $message =~ s/["]/&quot;/g;
    $message =~ s/[']/&#39;/g;	# &apos; is the XML entity, but historically it's not also an HTML entity and IE won't interpret it
    $message =~ s/</&lt;/g;
    $message =~ s/>/&gt;/g;

    my $monitorstatus = $severity;
    my $serverip      = $ipaddress;

    my @xml_message = ();

    # Our working assumptions, set up elsewhere:
    #
    #   +-------------------+-------------+--------------------------------------+-------------------------+
    #   | ApplicationTypeID | Name        | Description                          | StateTransitionCriteria |
    #   +-------------------+-------------+--------------------------------------+-------------------------+
    #   |               103 | REPLICATION | Disaster Recovery Replication Engine | Device                  | 
    #   +-------------------+-------------+--------------------------------------+-------------------------+
    #
    #   +-------------------------+--------+------------------------------------------------------------------+
    #   | ConsolidationCriteriaID | Name   | Criteria                                                         |
    #   +-------------------------+--------+------------------------------------------------------------------+
    #   |                       2 | SYSTEM | OperationStatus;Device;MonitorStatus;ApplicationType;TextMessage | 
    #   +-------------------------+--------+------------------------------------------------------------------+

    # FIX LATER:  In the future, we might consider implementing a special ConsolidationCriteria for DR messages.
    # FIX LATER:  In the future, we might consider generalizing and using $msgtype as the ApplicationType value.
    push @xml_message, '<GENERICLOG consolidation="SYSTEM" ';		# Consolidation is ON
    push @xml_message, 'ApplicationType="REPLICATION" ';		# Our application.
    push @xml_message, "MonitorServerName=\"$thisnagios\" ";		# Default Identification

    # Device should always be IP everywhere
    push @xml_message, "Device=\"$serverip\" ";				# Default Identification

    push @xml_message, "Severity=\"$severity\" ";
    push @xml_message, "MonitorStatus=\"$monitorstatus\" ";
    push @xml_message, "ReportDate=\"" . time_text(time) . "\" ";	# set ReportDate to current local time
    push @xml_message, "TextMessage=\"$message\" ";
    push @xml_message, "/>";

    my $xml_message = join( '', @xml_message );

    my @xml_messages = ();
    push @xml_messages, $xml_message;

    my $errors = send_xml_messages(\@xml_messages);

    # FIX THIS:  Should we send a similar message as well to the Nagios command pipe?

    return $errors;
}

# Internal routine for debugging; not expected to be for general use.
sub log_foundation {
    foreach my $key (sort keys %$foundation) {
        log_message "$key => $foundation->{$key}";
    }
}

1;

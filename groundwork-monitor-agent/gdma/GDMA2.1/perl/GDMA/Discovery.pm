################################################################################
#
# GDMA::Discovery
#
# This library contains routines that support execution of GDMA auto-discovery
# sensors on the GDMA client.
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

package GDMA::Discovery;

use strict;
use warnings;

use Exporter;
use Config;
use JSON::PP;
use LWP::UserAgent;
use HTTP::Request;
use HTTP::Status;
use POSIX qw(:signal_h);
use Sys::Hostname;
use Net::Domain;
use NetAddr::IP::Lite;
use Socket;
use File::Glob qw( GLOB_ERROR GLOB_LIMIT GLOB_NOCASE GLOB_NOSORT GLOB_BRACE GLOB_QUOTE GLOB_TILDE );
BEGIN {
    ## Sys::Filesystem uses Module::Pluggable to locate subsidiary packages, and
    ## that doesn't play nice with the ActiveState perlapp's preprocessing to
    ## find the full set of packages that must be loaded up.  So we need to make
    ## it more apparent in this particular case.
    if ( $^O eq 'MSWin32' ) {
	require Sys::Filesystem::Mswin32;
    }
}
use Sys::Filesystem;

## For development use only.
# use Data::Dumper;
# $Data::Dumper::Indent   = 1;
# $Data::Dumper::Sortkeys = 1;

if ( $^O ne 'MSWin32' ) {
    ## The POSIX::RT::Timer package is only available on UNIX platforms.
    ## We need it to impose timeouts on file mirroring via HTTPS, given that
    ## the library we use for mirroring is broken in that mode (GDMA-295).
    ##
    ## We run these statements inside string evals in order to sidestep the
    ## Windows Perl compiler's behavior of acting on these statements at
    ## compile time (if only to check that the package exists, and include
    ## it into the compiled program).  That won't work under Windows, because
    ## the POSIX::RT::Timer package, being UNIX-specific, won't compile there.
    eval 'require POSIX::RT::Timer';
    if ($@) {
	chomp $@;
	die "$@\n";
    }
    eval 'import POSIX::RT::Timer';
    if ($@) {
	chomp $@;
	die "$@\n";
    }
}

use GDMA::AutoSetup;
use GDMA::Utils;

use TypedConfig 1.3.1;

# ================================
# Package Parameters
# ================================

our @ISA = ('Exporter');
our $VERSION = '0.8.0';

my $Default_Max_Server_Redirects = 5;

# ================================
# Package Routines
# ================================

# Package constructor.
#
# The "show_resources" option means that each sensor should list all the qualified resources
# that it finds, exactly once each time it executes using a different set of resources.  (We
# generally assume the resources will be stable during the discovery process, so we limit the
# output to once per instantiation of the Discovery package.  To track whether or not such
# data has been produced for each set of resources for each sensor, the have_shown_resources
# is used.  It is a multi-level hash, with the first level of keys being the sensor type and
# the second level of keys, if needed for the sensor, being the resource specification.  The
# final value will generally just be a true value, though even the existence of the defining
# set of hash keys should be sufficient to tell the sensor that it should not output the data
# another time.  If the caller wishes to reset all of these flags, we can either provide a
# routine to delete the entire have_shown_resources hash in an existing object, or the caller
# can destroy the GDMA::Discovery object in hand and create a new one.
#
sub new {
    my $invocant = $_[0];
    my $options  = $_[1];                          # hashref; options like base directory
    my $class    = ref($invocant) || $invocant;    # object or class name

    # FIX MAJOR:  Verify that all of the required options are in fact supplied, while
    # supporting server-side use of this package that might not need all of these choices.

    if ( not defined $options->{logger} ) {
	print STDERR "ERROR:  GDMA::Discovery->new() called with no logger option specified.\n";
	return undef;
    }

    my %config = (
	hostname             => $options->{hostname},
	show_resources       => $options->{show_resources} ? 1 : 0,
	have_shown_resources => {},
	logger               => $options->{logger}
    );

    my $self = bless \%config, $class;

    return $self;
}

sub read_trigger_file {
    my $self             = shift;
    my $trigger_path     = shift;
    my $config_debug     = shift;
    my $return_all_items = shift;
    my $config           = ();
    my %trigger          = ();

    eval {
	$config = TypedConfig->new( $trigger_path, $config_debug, { interpolate => 0, c_comments => 0 } );
	$trigger{last_step}            = $config->get_scalar('last_step');
	$trigger{if_duplicate}         = $config->get_scalar('if_duplicate');
	$trigger{soft_error_reporting} = $config->get_scalar('soft_error_reporting');
	## change_policy is an optional directive, which might not even be present in the trigger file.
	## Perhaps in some future version of TypedConfig, we will support another argument to get_scalar()
	## to indicate that fact and return undef without throwing an error if the option is missing.
	eval { $trigger{change_policy} = $config->get_scalar('change_policy'); };
	die $@ if $@ && $@ !~ /cannot find a config-file value/;
    };
    if ($@) {
	$@ =~ s/\s+$//;  # chomp depends on $/, which might not contain what we expect
	$@ =~ s/^ERROR:\s+//i;
	$self->{logger}->error("ERROR:  Cannot read trigger file $trigger_path ($@).");
	## We need to return an explicitly empty hashref here, because we might have
	## got a partially-populated has before we aborted, and that would not let us
	## tell the caller that this processing failed.
	return {};
    }

    # $return_all_items is for validation purposes, not production purposes.
    return $return_all_items ? $config : \%trigger;
}

sub validate_trigger {
    my $self         = shift;
    my $trigger_path = shift;
    my $config       = shift;
    my $outcome      = 0;       # start out pessimistic, so we can abort with this outcome at any time

    my %is_valid_last_step = (
	ignore_instructions => 1,
	fetch_instructions  => 1,
	do_discovery        => 1,
	send_results        => 1,
	do_analysis         => 1,
	test_configuration  => 1,
	do_configuration    => 1,
    );
    my %is_valid_if_duplicate         = ( ignore       => 1, optimize      => 1, force => 1 );
    my %is_valid_soft_error_reporting = ( ignore       => 1, post          => 1 );
    my %is_valid_change_policy        = ( from_scratch => 1, ignore_extras => 1, non_destructive => 1 );
    my %is_valid_trigger_option       = (
	last_step            => \%is_valid_last_step,
	if_duplicate         => \%is_valid_if_duplicate,
	soft_error_reporting => \%is_valid_soft_error_reporting,
	change_policy        => \%is_valid_change_policy,
    );

    foreach my $key ( sort keys %$config ) {
	next if $key eq 'block_sigset';
	if ( not $is_valid_trigger_option{$key} ) {
	    $self->{logger}->error("ERROR:  Trigger file $trigger_path contains invalid option \"$key\".");
	    return $outcome;
	}
	if ( not $is_valid_trigger_option{$key}{ $config->{$key} } ) {
	    $self->{logger}->error("ERROR:  Trigger file $trigger_path contains invalid $key value \"$config->{$key}\".");
	    return $outcome;
	}

	## FIX MAJOR:  obsolete code; drop this
	if (0) {
	    if ( $key eq 'last_step' && not $is_valid_last_step{ $config->{$key} } ) {
		$self->{logger}->error("ERROR:  Trigger file $trigger_path contains invalid $key value \"$config->{$key}\".");
		return $outcome;
	    }
	    if ( $key eq 'if_duplicate' && not $is_valid_if_duplicate{ $config->{$key} } ) {
		$self->{logger}->error("ERROR:  Trigger file $trigger_path contains invalid $key value \"$config->{$key}\".");
		return $outcome;
	    }
	    if ( $key eq 'soft_error_reporting' && not $is_valid_soft_error_reporting{ $config->{$key} } ) {
		$self->{logger}->error("ERROR:  Trigger file $trigger_path contains invalid $key value \"$config->{$key}\".");
		return $outcome;
	    }
	}
    }

    $outcome = 1;
    return $outcome;
}

sub my_forced_hostname {
    my $g_config = shift;

    # This value should come from the Forced_Hostname directive in the gdma_override.conf file, if it
    # exists there.  If we don't have such a value, we return undef, which the calling code can handle.

    return $g_config->{Forced_Hostname};
}

sub my_hostnames {
    my $self                = shift;
    my $g_config            = shift;
    my @hostnames           = ();
    my $force_long_hostname = defined( $g_config->{Use_Long_Hostname} ) ? $g_config->{Use_Long_Hostname} =~ /on/i : undef;
    my $force_to_lowercase  = defined( $g_config->{Use_Lowercase_Hostname} ) && $g_config->{Use_Lowercase_Hostname} =~ /on/i;
    my $long_hostname;
    my $simple_hostname;
    my $short_hostname;

    # FIX LATER:  We would like to identify possibly more than one hostname (nodename) on a multi-homed machine,
    # and list them in some canonical order.  That might take some additional platform-specific code here.

    # FIX LATER:  We want to ensure we get FQDNs if at all possible, because they can always be cut down to
    # corresponding shortnames if need be.  Look at these resources:
    #
    # Sys::Hostname::FQDN
    # gethostbyaddr
    # gethostbyname
    # getaddrinfo (if available; perhaps more IPv6-friendly)
    # Socket::gethostinfo
    # Socket::getaddrinfo
    # Net::hostent
    # Net::Domain
    # uname -n
    #
    # bad package; don't use
    # use Sys::Hostname::Long 'hostname_long';
    # $raw_hostname = hostname_long();
    #
    # Recall our old libnet patch for Net::Domain, some form of which we might need to resurrect, and also
    # the fact that we should have (but never did) also patch it to fix up the mechanism by which it tears
    # apart a FQDN and then attempts to sew the pieces back together, sometimes resulting in a shorter FQDN
    # that matches something out on the Internet that is not the actual hostname you're looking for, because
    # some external machine with fewer labels happens to exist somewhere out in the world.
    #
    # See also:
    # http://perldoc.perl.org/perlfaq9.html#How-do-I-find-out-my-hostname%2c-domainname%2c-or-IP-address%3f

    # Net::Domain implements hostdomain(), hostname(), and hostfqdn() calls.
    # What we want here is the fully-qualified hostname.
    $long_hostname = Net::Domain::hostfqdn();

    # This call is reasonably likely to produce just a shortname already, but we force the issue to be sure.
    $simple_hostname = Sys::Hostname::hostname();
    ( $short_hostname = $simple_hostname ) =~ s/\..*//;    # force into shortname form

    # We prefer $simple_hostname over $long_hostname if $simple_hostname is an FQDN, because we don't fully
    # trust the results of Net::Domain::hostfqdn() due to its internal algorithm.
    if ( not defined($force_long_hostname) or $force_long_hostname ) {
	if ( $simple_hostname =~ m{\.} ) {
	    push @hostnames, $force_to_lowercase ? lc($simple_hostname) : $simple_hostname;
	    push @hostnames, $force_to_lowercase ? lc($long_hostname)   : $long_hostname;
	}
	else {
	    push @hostnames, $force_to_lowercase ? lc($long_hostname)   : $long_hostname;
	    push @hostnames, $force_to_lowercase ? lc($simple_hostname) : $simple_hostname;
	}
	push @hostnames, $force_to_lowercase ? lc($short_hostname) : $short_hostname;
    }
    else {
	push @hostnames, $force_to_lowercase ? lc($short_hostname)  : $short_hostname;
	push @hostnames, $force_to_lowercase ? lc($simple_hostname) : $simple_hostname;
	push @hostnames, $force_to_lowercase ? lc($long_hostname)   : $long_hostname;
    }

    # This implementation of de-dup'ing preserves any notion of order determined earlier.
    my %unique = ();
    @unique{@hostnames} = (undef) x @hostnames;
    my @unique_hostnames = ();
    foreach my $hostname (@hostnames) {
	push @unique_hostnames, $hostname if exists $unique{$hostname};
	delete $unique{$hostname};
    }

    return \@unique_hostnames;
}

# FIX LATER:  in the future, if we can, associate at least one hostname with each IP address.
sub my_ip_addresses {
    my $self         = shift;
    my @ip_addresses = ();

    # For how internal and external IP addresses can be programmatically determined from within an AWS instance,
    # see these pages:
    #
    # https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-instance-addressing.html
    # https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-instance-metadata.html
    # https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/elastic-ip-addresses-eip.html
    # https://docs.aws.amazon.com/AmazonVPC/latest/UserGuide/vpc-ip-addressing.html
    #
    # and these commands for the private IPv4 address, the public IPv4 address (which may be an Elastic IP address),
    # and IPv6 addresses, respectively:
    #
    # curl http://169.254.169.254/latest/meta-data/local-ipv4
    # curl http://169.254.169.254/latest/meta-data/public-ipv4
    # curl http://169.254.169.254/latest/meta-data/network/interfaces/macs/{mac-address}/ipv6s
    #
    # with the {mac-address} expreseed like this:
    #
    # curl http://169.254.169.254/latest/meta-data/network/interfaces/macs/02:29:96:8f:6a:2d/ipv6s
    #
    # and ome additional IPv4 data of interest to us also accessible at certal levels:
    #
    # Private IPv4 DNS hostname (on the eth0 device, when multiple network interfaces are present):
    # curl http://169.254.169.254/latest/meta-data/hostname
    #
    # How to get the {mac-address} for the first network interface, for use in more-detailed queries:
    # curl http://169.254.169.254/latest/meta-data/mac
    #
    # curl http://169.254.169.254/latest/meta-data/network/interfaces/macs/{mac-address}/local-hostname
    # curl http://169.254.169.254/latest/meta-data/network/interfaces/macs/{mac-address}/local-ipv4s
    # curl http://169.254.169.254/latest/meta-data/network/interfaces/macs/{mac-address}/public-hostname
    # curl http://169.254.169.254/latest/meta-data/network/interfaces/macs/{mac-address}/public-ipv4s
    #
    # But of course, that begs the question of whether and how we can determine the {mac-address} to insert into that
    # last URL, how we cn even determine that we are running in an AWS instance, what procedures would be used in a
    # non-Linux AWS instance, and what procedures would be used in non-AWS contexts.  We might want to look for some
    # sort of AWS:: Perl packages to handle this, or make our own to separate out this detailed context-dependent
    # code so it can evolve separately from that for other contexts.
    #
    # Note that AWS addresses might not continue to be assigned to a host if it is stopped or terminated.  See the
    # AWS documentation about this.  So it may well be important for later passes of discovery to override the
    # address data for an existing host even when auto-setup is operated with an otherwise non-destructive change
    # policy in effect.
    #
    # Also note that there are similar AWS means to fetch the local-hostname, the public-hostname, and some other
    # data.  Those may come in handy as well.
    #
    # curl http://169.254.169.254/latest/meta-data/local-hostname
    # ip-10-251-50-12.ec2.internal
    #
    # curl http://169.254.169.254/latest/meta-data/public-hostname
    # ec2-203-0-113-25.compute-1.amazonaws.com

    if ( $^O eq 'linux' ) {
	##
	## This seems to be the modern command to run on Linux, as opposed to "ifconfig -a".
	##
	##     ip addr show scope global | fgrep inet | awk '{print $2}' | sed -e 's:/.*::' | sort -u
	##     ip addr show scope link   | fgrep inet | awk '{print $2}' | sed -e 's:/.*::' | sort -u
	##
	## We ignore "scope host" addresses, because they won't be useful off-host.  I'm not sure
	## whether we should be including IPv6 "scope link" IP addresses in our list, but we'll do
	## so for the moment and see if it causes any difficulties.  However, we consider it likely
	## that a "scope global" address will be more useful than a "scope link" address, so we take
	## some pains to push any scope-global addresses on our list before any scope-link addresses.
	## That said, for the time being we take no precautions to attempt to prefer IPv4 addresses
	## over IPv6 addresses, or vice versa.
	##
	my @ip_lines = $self->qx('ip addr show scope global');
	push @ip_lines, $self->qx('ip addr show scope link');
	foreach my $ip_line (@ip_lines) {
	    if ( $ip_line =~ m{\sinet6?\s+(\S+)} ) {
		( my $ip_address = $1 ) =~ s{/.*}{};
		push @ip_addresses, $ip_address;
	    }
	}
    }
    elsif ( $^O eq 'solaris' ) {
	##
	## If available (on Solaris 11), this would be the preferred command:
	##
	##     ipadm show-addr -p -o addr
	##
	## with some filtering of the output to eliminate undesired stuff:  s{-.*}{}; s{/.*}{};
	## delete lines containing a question mark; ignore 127.0.0.1/8 and ::1/128 addresses.
	##
	## This command should be portable across Solaris versions:
	##
	##     ifconfig -a | fgrep inet | awk '{print $2}'
	##
	if ( -x '/usr/sbin/ipadm' ) {
	    my @ipadm_lines = $self->qx('/usr/sbin/ipadm show-addr -p -o addr');
	    foreach my $ipadm_line (@ipadm_lines) {
		$ipadm_line =~ s{-.*}{};    # clean up laddr-->raddr lines
		$ipadm_line =~ s{/.*}{};    # clean up apparent netmask length
		next if $ipadm_line =~ m{\?};
		next if $ipadm_line eq '127.0.0.1';
		next if $ipadm_line eq '::1';
		push @ip_addresses, $ipadm_line;
	    }
	}
	else {
	    my @ifconfig_lines = grep /inet/, $self->qx('ifconfig -a');
	    foreach my $ifconfig_line (@ifconfig_lines) {
		my $ip_address = ( split( ' ', $ifconfig_line ) )[1];
		$ip_address =~ s{/.*}{};    # clean up apparent netmask length
		next if $ip_address eq '127.0.0.1';
		next if $ip_address eq ':;1';
		next if $ip_address eq '::';    # ignore IPv6 multicast address
		push @ip_addresses, $ip_address;
	    }
	}
    }
    elsif ( $^O eq 'aix' ) {
	## We have multiple ways to find the IP addresses on this platform.
	##
	## % ifconfig -a | fgrep inet | awk '{print $2}'
	## 172.28.111.51
	## 127.0.0.1
	## ::1/0
	##
	## % prtconf | fgrep 'IP Address' | awk '{print $NF}'
	## 172.28.111.51
	##
	## To get the IP address(es):
	## % lsrsrc IBM.NetworkInterface | fgrep IPAddress | awk '{print $NF}' | sed -e 's/"//g'
	##
	## Possibly:
	## % host -n `hostname` | awk '{print $NF}'
	## 172.28.111.51
	## However, if the hostname is at all ambiguous and not properly recognized, you may get some
	## address information for other things like the domain itself, so this command is not recommended
	## unless you filter the results before printing the last field, to make sure you're looking at a
	## line that contains exactly the same hostname, and then further qualify the last field to make
	## sure it looks like a valid IP address.
	##
	## Avoid using:
	## % host `hostname` | awk '{print $NF}'
	## as it may produce spurious results (probably, whatever is listed in /etc/hosts, which may not
	## be accurate) if the output of `hostname` is not the fully-qualified hostname.  However, if we
	## had a way to guarantee using the fully-qualified hostname, then we could use that to produce
	## the proper answer, with the same filtering as applied to the "host -n" command.  I don't yet
	## know how to guarantee that you can find the fully-qualified hostname, if what is recorded in
	## /etc/hosts is a shortname.
	##
	## Get a list of all the network interface names, using one of:
	## % ifconfig -l
	## % lsdev -c if | awk '{print $1}'
	## (the latter including an extra mysterious "et0"), and then use them each in turn;
	## % lsattr -El en0 | fgrep netaddr | awk '{print $2}'
	## except that because the second column might be empty, you will then have to filter out stuff
	## that does not look like an IPv4 or IPv6 address.
	##
	my @ifconfig_lines = grep /inet/, $self->qx('ifconfig -a');
	foreach my $ifconfig_line (@ifconfig_lines) {
	    my $ip_address = ( split( ' ', $ifconfig_line ) )[1];
	    $ip_address =~ s{/.*}{};    # clean up apparent netmask length
	    next if $ip_address eq '127.0.0.1';
	    next if $ip_address eq ':;1';
	    push @ip_addresses, $ip_address;
	}
    }
    elsif ( $^O eq 'hpux' ) {
	## FIX MAJOR:  fill this in
    }
    elsif ( $^O eq 'MSWin32' ) {
	##
	## FIX MAJOR:  This works as an administrator.  I'm not sure it will work as an
	## ordinary user; that needs to be tested.
	##
	## Here we present filtering by a regular expression, even though the find command
	## does not actually support that.  We're looking for "IPv4 Address", "IPv6 Address",
	## or "IP Address".
	##
	##     ipconfig /all | find "IP(v[46])? Address"
	##
	## This output may include both IPv4 and IPv6 addresses.  Some of those addresses may
	## be suffixed with "(Preferred)", so we take that as a clue that said addresses should
	## be presented at the beginning of our list instead of at the end of our list.
	##
	## IPv6 addresses may be suffixed with a "%\d+" extension such as %10 which presumably
	## indicates a particular interface.  In our processing here, we will drop that, but
	## we might want to revisit that decision in the future.
	##
	## That needs filtering to select only the last field, to strip off any extra data
	## such as a "%\d+" extension and the string "(Preferred)" -- all of which filtering
	## is probably best done in Perl, not with some Windows command.
	##
	my @ipconfig_lines = $self->qx('ipconfig /all');
	foreach my $ipconfig_line (@ipconfig_lines) {
	    if ( $ipconfig_line =~ m{IP(?:v[46])? Address} ) {
		my @ipconfig_fields = split( ' ', $ipconfig_line );
		( my $ip_field = $ipconfig_fields[$#ipconfig_fields] ) =~ s{[%(].*}{}g;
		if ( $ipconfig_line =~ m{\(Preferred\)} ) {
		    unshift @ip_addresses, $ip_field;
		}
		else {
		    push @ip_addresses, $ip_field;
		}
	    }
	}
    }

    # This implementation of de-dup'ing preserves any notion of order determined earlier.
    my %unique = ();
    @unique{@ip_addresses} = (undef) x @ip_addresses;
    my @unique_ip_addresses = ();
    foreach my $ip_address (@ip_addresses) {
	push @unique_ip_addresses, $ip_address if exists $unique{$ip_address};
	delete $unique{$ip_address};
    }

    return \@unique_ip_addresses;
}

# FIX LATER:  in the future, if we can, associate at least one IP address with each MAC address.
sub my_mac_addresses {
    my $self          = shift;
    my @mac_addresses = ();

    if ( $^O eq 'linux' ) {
	##
	## This seems to be the modern command to run on Linux, as opposed to "ifconfig -a".
	##
	##     ip link | fgrep link/ether | awk '{print $2}' | sort -u
	##
	my @ip_lines = $self->qx('ip link');
	foreach my $ip_line (@ip_lines) {
	    if ( $ip_line =~ m{link/ether\s+(\S+)} ) {
		push @mac_addresses, $1;
	    }
	}
    }
    elsif ( $^O eq 'solaris' ) {
	##
	## "ifconfig -a" output will yield the ethernet address, but only if you're running as a privileged
	## user (which GDMA is not).  So that is not an acceptable way to find this information.  Try this,
	## which should at least show the active interfaces:
	##
	##     /usr/sbin/arp -an | fgrep -v 224.0.0. | awk '{print $NF}' | fgrep : | sort -u
	##
	## That does not show unconfigured cards, but that is probably reasonable.  Perhaps more problematic,
	## "arp -a" only shows the logical interfaces.  If a link aggregation exists, it will show the
	## aggregate, not the underlying multiple physical NIC devices.
	##
	## From this page:
	## https://stackoverflow.com/questions/2679855/on-solaris-is-there-a-command-to-get-the-ethernet-card-mac-address-without-bein
	## Also a possibility, on Solaris 11:
	##
	##     /usr/sbin/dladm show-phys -m | awk '{print $3}' | fgrep : | sort -u
	##
	## which will include MAC addresses not currently in use (they can be filtered out, if desired).
	## Or alternatively, use the documented, definitive means to view all MAC addresses on Solaris 11
	## (see https://docs.oracle.com/cd/E23824_01/html/821-1458/geyqe.html), though this output needs
	## to be normalized to zero-pad the octets:
	##
	##     /usr/sbin/dladm show-linkprop -p mac-address | awk '{print $4}' | fgrep : | sort -u
	##
	if ( -x '/usr/sbin/dladm' ) {
	    require NetAddr::MAC;
	    my @dladm_lines = $self->qx('/usr/sbin/dladm show-linkprop -p mac-address');
	    foreach my $dladm_line (@dladm_lines) {
		my $mac_field = ( split( ' ', $dladm_line ) )[3];
		push @mac_addresses, NetAddr::MAC::mac_as_ieee($mac_field) if defined($mac_field) && $mac_field =~ m{:};
	    }
	}
	else {
	    my @arp_lines = grep !/224\.0\.0\./, $self->qx('arp -an');
	    foreach my $arp_line (@arp_lines) {
		my @arp_fields = split( ' ', $arp_line );
		push @mac_addresses, $arp_fields[$#arp_fields] if @arp_fields && $arp_fields[$#arp_fields] =~ m{:};
	    }
	}
    }
    elsif ( $^O eq 'aix' ) {
	##
	## for interface in `ifconfig -l | tr ' ' '\n' | egrep -v '^lo'`; do entstat -d $interface | fgrep Hardware | awk '{print $NF}' ; done
	##
	## Alternatively:
	##     lscfg -v | fgrep 'Network Address' | awk -F . '{print $NF}'
	## followed by lowercasing and adding in the colon characters.
	##
	## Alternatively:
	## MAC address, in nice colon-separated lowercase form:
	##     lsrsrc IBM.NetworkInterface | fgrep HardwareAddress | awk '{print $NF}' | sed -e 's/"//g'
	##
	## Alternatively:
	## MAC address, in nice colon-separated lowercase form:
	##     netstat -v | fgrep 'Hardware Address' | awk '{print $NF}'
	##
	my @non_local_interfaces = grep !/^lo/, split( ' ', $self->qx('ifconfig -l') );
	foreach my $interface (@non_local_interfaces) {
	    my @hardware_lines = grep /Hardware/, $self->qx("entstat -d $interface");
	    if (@hardware_lines) {
		my @hardware_address_fields = split( ' ', $hardware_lines[0] );
		push @mac_addresses, $hardware_address_fields[$#hardware_address_fields] if @hardware_address_fields;
	    }
	}
    }
    elsif ( $^O eq 'hpux' ) {
	## FIX MAJOR:  fill this in; make sure the values are lowercase and otherwise normalized
    }
    elsif ( $^O eq 'MSWin32' ) {
	##
	## FIX MAJOR:  This works as an administrator.  I'm not sure it will work as an
	## ordinary user; that needs to be tested.
	##
	##     ipconfig /all | find "Physical"
	##
	## Note that the output may include MAC addresses for disconnected-media connections;
	## in some future version, we might wnt to filter those out.
	##
	## That needs filtering to select only the last field, to turn dashes into colons,
	## to ignore 8-octet results, and to lowercase the final result -- all of which
	## filtering is probably best done in Perl, not with some Windows command.
	##
	my @ipconfig_lines = $self->qx('ipconfig /all');
	foreach my $ipconfig_line (@ipconfig_lines) {
	    if ( $ipconfig_line =~ m{Physical} ) {
		my @ipconfig_fields = split( ' ', $ipconfig_line );
		( my $mac_field = $ipconfig_fields[$#ipconfig_fields] ) =~ s/-/:/g;
		push @mac_addresses, lc $mac_field if length($mac_field) == 17;
	    }
	}
    }

    # This implementation of de-dup'ing preserves any notion of order determined earlier.
    my %unique = ();
    @unique{@mac_addresses} = (undef) x @mac_addresses;
    my @unique_mac_addresses = ();
    foreach my $mac_address (@mac_addresses) {
	push @unique_mac_addresses, $mac_address if exists $unique{$mac_address};
	delete $unique{$mac_address};
    }

    return \@unique_mac_addresses;
}

# run a pass of auto-discovery on the GDMA client
sub run_discovery {
    my $self              = shift;
    my $g_config          = shift;
    my $trigger_path      = shift;
    my $instructions_file = shift;
    my $bad_outcome       = 0;
    my $no_sensor_results = {};

    my $trigger = $self->read_trigger_file( $trigger_path, undef, 0 );
    if ( not %$trigger ) {
	$self->{logger}->error("ERROR:  Cannot understand the trigger file \"$trigger_path\".");
	return $bad_outcome, $no_sensor_results;
    }

    my %autosetup_options = ( logger => $self->{logger} );
    my $autosetup = GDMA::AutoSetup->new( \%autosetup_options );
    if ( not defined $autosetup ) {
	$self->{logger}->error("ERROR:  Cannot initialize the GDMA::AutoSetup package; see earlier error messages.");
	return $bad_outcome, $no_sensor_results;
    }
    my $instructions = $autosetup->read_instructions_file($instructions_file);
    if ( not defined($instructions) or not %$instructions ) {
	$self->{logger}->error("ERROR:  Discovery instructions file could not be read; see earlier error messages.");
	return $bad_outcome, $no_sensor_results;
    }
    my $validation_outcome = $autosetup->validate_instructions( $instructions_file, $instructions );
    if ( not $validation_outcome ) {
	$self->{logger}->error("ERROR:  Discovery instructions failed validation; see earlier error messages.");
	return $bad_outcome, $no_sensor_results;
    }

    # Now we know that the instructions are valid.  Go execute all the sensors included in
    # the instructions, and collect all the discovery results into some other structure.
    my ( $full_sensor_outcome, $full_sensor_results ) = $self->execute_instructions($instructions);
    if ( not $full_sensor_outcome ) {
	$self->{logger}->error("ERROR:  Execution of discovery instructions failed; see earlier error messages.");
    }

    my %discovery_results = ();

    # To the basic sensor results, we add some overhead information to identify where those results come from, and
    # some other global attributes.  One thing we might logically like to include would be the date/time at which the
    # discovery occurred.  However, if we do that, every run of discovery results will end up producing a uniquely
    # different set of overall output results.  And that would destroy our ability to trivially compare the results of
    # successive discovery runs by simple string comparison, to see whether or not they are identical.

    # This version identifies the shape of the discovery results, so any software that reads them
    # can adapt to changes over time by switching its processing based on this value.
    $discovery_results{packet_version} = '1.0';

    $discovery_results{succeeded} = $full_sensor_outcome;

    # We make the failure_message be some summary of the problems, if any, with the sensor discovery and matching.
    if ( not $full_sensor_outcome ) {
	my @error_messages;
	foreach my $sensor_results (@$full_sensor_results) {
	    push @error_messages, $sensor_results->{error_message} if defined $sensor_results->{error_message};
	}
	$discovery_results{failure_message} = join( "\n", @error_messages ) if @error_messages;
    }

    $discovery_results{last_step}            = $trigger->{last_step};
    $discovery_results{if_duplicate}         = $trigger->{if_duplicate};
    $discovery_results{soft_error_reporting} = $trigger->{soft_error_reporting};
    $discovery_results{change_policy}        = $trigger->{change_policy} if $trigger->{change_policy};

    $discovery_results{registration_agent} = 'GDMA Auto-Setup';

    # FIX LATER:
    # With regard to IP addresses and MAC addresses, perhaps some future version of this code (with a modified
    # packet_version value to indicate the incompatibility) might send in an { ip_address => [ ... hostnames ... ] }
    # hash for the IP addresses, and a { mac_address => [ ... ip_addresses ... ] } hash for the MAC addresses, instead
    # of just simple arrays.  That might make it easier for the server to choose an IP address which matches the
    # hostname chosen by the server, and to choose a MAC address which matches the IP address chosen by the server.
    # If we can find an obvious way to do these things in the first official release, we should do so.

    my $forced_hostname = my_forced_hostname($g_config);
    $discovery_results{forced_hostname} = $forced_hostname if defined $forced_hostname;

    $discovery_results{hostnames}    = $self->my_hostnames($g_config);
    $discovery_results{ip_addresses} = $self->my_ip_addresses();

    my $mac_addresses = $self->my_mac_addresses();
    $discovery_results{mac_addresses} = $mac_addresses if @$mac_addresses;

    $discovery_results{os_type} = my_os_type();

    # FIX MAJOR:  Draw these values from the Auto_Register_Host_Profile and Auto_Register_Service_Profile values found
    # in the gdma_auto.conf file; try to ignore any overlaid values from host externals uploaded from the server.
    # $discovery_results{configured_host_profile} = 'FIX MAJOR';
    # $discovery_results{configured_service_profile} = 'FIX MAJOR';

    # Scan through all the sensor results and collect lists of host and service profiles which are supposedly to be
    # applied to the machine as a result of this pass of discovery.  These lists are really for the convenience of the
    # people who want to view the discovery results, rather than for machine consumption.  (Later on, not now but during
    # discovery results analysis, we'll deal with any conflicts in having multiple sensors having produced the same
    # profiles but with potentially conflicting customization.)  So in these lists, we eliminate duplicate profile names
    # and alphabetize those that remain.
    my %discovered_host_profiles    = ();
    my %discovered_service_profiles = ();
    my %discovered_services         = ();
    foreach my $sensor_results (@$full_sensor_results) {
	if ( $sensor_results->{matched} ) {
	    $discovered_host_profiles{ $sensor_results->{host_profile_name} }       = 1 if $sensor_results->{host_profile_name};
	    $discovered_service_profiles{ $sensor_results->{service_profile_name} } = 1 if $sensor_results->{service_profile_name};
	    $discovered_services{ $sensor_results->{service_name} }                 = 1 if $sensor_results->{service_name};
	}
    }
    @{ $discovery_results{discovered_host_profiles} }    = sort keys %discovered_host_profiles;
    @{ $discovery_results{discovered_service_profiles} } = sort keys %discovered_service_profiles;
    @{ $discovery_results{discovered_services} }         = sort keys %discovered_services;

    $discovery_results{full_sensor_results} = $full_sensor_results;

    if ( not $self->force_canonical_ordering( $discovery_results{full_sensor_results} ) ) {
	$self->{logger}->error("ERROR:  Could not put discovery results into canonical order; see earlier error messages.");
	return $bad_outcome, \%discovery_results;
    }

    return $discovery_results{succeeded}, \%discovery_results;
}

# See the Config(3pm) man page for details of this magic formulation.
sub system_signal_name {
    my $signal_number = shift;
    local $_;

    # Somehow this routine destroys the value of $!, even though none of this code looks like it should
    # have anything to do with an OS error.  So we block that from affecting its value in the caller.
    local $!;

    my %sig_num;
    my @sig_name;

    unless ( $Config{sig_name} && $Config{sig_num} ) {
	return undef;
    }

    my @names = split ' ', $Config{sig_name};
    @sig_num{@names} = split ' ', $Config{sig_num};
    foreach (@names) {
	$sig_name[ $sig_num{$_} ] ||= $_;
    }

    return $sig_name[$signal_number] || undef;
}

sub full_status_message {
    my $wait_status = shift;
    my $saved_errno = shift;

    my $exit_status   = $wait_status >> 8;
    my $signal_number = $wait_status & 0x7F;
    my $dumped_core   = $wait_status & 0x80;
    my $signal_name   = system_signal_name($signal_number) || "$signal_number is unknown";

    my $message =
      ( $wait_status == -1 )
      ? ( $saved_errno ? "system error \"$saved_errno\"" : 'unknown system error' )
      : "exit status $exit_status" . ( $signal_number ? " (signal $signal_name)" : '' ) . ( $dumped_core ? ' (with core dump)' : '' );

    return $message;
}

# This is an internal routine which effectively mirrors the standard qx() operator except that:
#
# (*) It forces $! to 0 before executing a command.  This ensures that any later analysis of $!
#     immediately after the command is run reflects this command and not some earlier problem.
# (*) If the desired command cannot be run, that status is properly reflected in both $? and $!
#     so it can be reflected back to the user.  (An ordinary qx() call in this situation fails
#     in the child process, but the value of $! in that case is inaccessible to the parent and
#     so cannot be reported out.)
# (*) Failures are logged directly in this routine, so as to provide a bit more detail than will
#     be available once this routine returns.  (All the caller can see for error information is
#     the combination of the $? and $! variables.)
#
# Whether or not to do string interpolation while forming the command to run is effectively
# controlled by the quoting used at the calling level, not here.
#
sub qx {
    my $self    = shift;
    my $command = shift;
    my @output  = ();
    my $output  = undef;
    $! = 0;

    # Note that if the $command cannot be run, we will get a warning message on STDERR written out directly
    # from the child process that tries to exec() the command.  There is no good way to capture that warning
    # message back here in the parent process, unless we directly handle fork()/exec(), SIGINT and SIGTERM
    # signal handling, a $SIG{__WARN__} handler in the child process to capture the generated warning, and
    # some other stuff to create a secondary pipe from child to parent that can be used to report such data.
    # Not wanting to go to such lengths at the moment, we simply allow the child process to write to STDERR.
    #
    if ( not open COMMAND, '-|', $command ) {
	$self->{logger}->error("ERROR:  Cannot open a pipe to read from the \"$command\" command ($!).");
	$? = -1 if $? == 0;    # Turn this into an error that the caller will readily detect.
	return wantarray ? () : undef;
    }
    else {
	@output = <COMMAND>;
	## Closing this filehandle waits for the child process to finish.
	if ( not close COMMAND ) {
	    ## Check for child-process error.
	    if ($!) {
		## Error ($!) closing the pipe (i.e., some problem within Perl or the OS, not the piped program).
		## (It is possible that the piped program independently experienced some failure, and that would still
		## be reflected in $?, but in light of the problem at the Perl level, we're going to ignore that.)
		## Presumably the file descriptor itself did still get closed, but it left behind evidence of some issue.
		$self->{logger}->error("ERROR:  Problem found while closing a pipe from the \"$command\" command ($!).");
	    }
	    else {
		my $status_message = full_status_message( $?, $! );
		$self->{logger}->error("ERROR:  Command \"$command\" failed execution with $status_message.");
	    }
	    return wantarray ? () : undef;
	}
    }

    return wantarray ? @output : join( '', @output );
}

# Internal routine, no $self involved.
#
# Differences between this routine and a simple plain Perl glob:
#
# (1) This routine takes a reference to a list of glob patterns, not a single string of
#     space-separated concatenated glob patterns, some of which might be quoted within the
#     string.
# (2) Each glob pattern can contain space characters, without that glob pattern needing to be
#     quoted at the level of the call to this routine.  (However, upstream parsing of string
#     data in the instructions file will still effectively require such quoting, as the user
#     specifies a list of glob patterns in a single instructions-file resource value string.)
# (3) If a glob does not match any path, the glob pattern itself is never returned.  Not being
#     confused by non-existent paths, when the purpose of the globs is to find actual matches,
#     is obviously critical to later usage of the glob results.
# (4) On Windows, if the glob-result paths contain any forward slashes (which will be true on
#     this platform if you use the recommended forward slashes in sensor resource globs to
#     refer to directory levels), they will be unconditionally turned into backslashes in the
#     results of this routine.  This is critical to convenient downstream usage of the glob
#     results, because we don't know how the resulting pathnames will be interpreted when
#     substituted into Windows command lines.  But that would ordinarily make construction
#     of sensor match patterns that compare against the glob results much more difficult,
#     because you would need quadrupling of backslashes in the sensor match pattern in the
#     instructions file to represent just one backslash character in the resource globbing
#     match results.  To work around that, for the particular sensors that use the sensor
#     pattern to directly match against the glob results (namely, the file_name, symlink_name,
#     directory_name, mounted_filesystem, and open_named_socket sensors), the sensor pattern
#     is automatically altered so each forward slash is replaced by two backslashes before the
#     pattern is interpreted as a Perl regular expression.  That way, you won't need quadrupled
#     backslashes in either the sensor resource globs or the sensor match pattern.  You will use
#     single forward slashes instead, in both places, to match directory levels, and yet still
#     see actual filesystem backslashes in any captured portions of the glob-result paths.
# (5) The overall list of matched paths is de-duplicated.  This is important for computing the
#     correct number of sensor-match instances.
# (6) We impose a limit on the size of the returned results, as a matter of simple sensibility
#     in the context of discovery sensor matching.
# (7) This routine will die() if it encounters an error.  (I'm not sure whether Perl glob will
#     do the same, but I need to call this out because the behavior of Perl glob() in that
#     circumstances isn't documented.)  That means that an eval{}; must be used to wrap each
#     call to this routine, so a sensor can fail softly.
#
# Items (3) and (5) in particular mean that ordinary Perl globbing is simply not good enough
# for our purposes in implementing discovery sensors.
#
sub pathglob {
    my $globs = shift;
    local $_;
    my @paths = ();
    my @globpaths;
    foreach my $glob (@$globs) {

	## We impose the GLOB_LIMIT partly because a discovery sensor has no business matching a huge
	## number of files, and partly because we don't want to blow up memory in a long-running daemon.
	@globpaths =
	  File::Glob::bsd_glob( $glob,
	    GLOB_LIMIT | ( $^O eq 'MSWin32' ? GLOB_NOCASE : 0 ) | GLOB_NOSORT | GLOB_BRACE | GLOB_QUOTE | GLOB_TILDE );

	## This extra printing is only intended for development testing.  On the other hand,
	## it might help people understand exactly what is going on at the glob level, before
	## further filtering happens.  So at some point in the future, we might turn these
	## print statements into log messages or whatever other form of output best integrates
	## with discovery results, and enable this output under some form of deep debug flag.
	if (0) {
	    print "this glob pattern:  '$glob'\n";
	    if (@globpaths) {
		print "     matched path:  '$_'\n" for @globpaths;
	    }
	    else {
		print "     matched no paths\n";
	    }
	}

	if (GLOB_ERROR) {
	    die "glob matching failed ($!)\n";
	}
	if ( $^O eq 'MSWin32' ) {
	    s{/}{\\}g for @globpaths;
	}
	push @paths, @globpaths;
    }

    # De-dup; sort for later convenience.
    use feature 'fc';
    my %unique = ();
    @unique{@paths} = (undef) x @paths;
    @paths = sort { fc($a) cmp fc($b) || $a cmp $b } keys %unique;

    return @paths;
}

# Internal routine, no $self involved.
sub my_os_type {
    ## Given the specific platforms we support, possible values for $^O are:  'linux' 'solaris' 'aix' 'hpux' 'MSWin32'
    my $os_type = $^O;
    ## Recode to make the values more sensible to the outside world.
    $os_type = 'windows' if $os_type eq 'MSWin32';
    return $os_type;
}

sub os_type_sensor {
    my $self           = shift;
    my $kind           = shift;
    my $tag            = shift;
    my $sensor         = shift;
    my $profile        = shift;
    my @instances      = ();
    my $sensor_outcome = 1;
    my $sensor_results = {
	error_message => undef,
    };

    my $os_type = my_os_type();

    # We potentially print the OS type that we determined, since the strings the pattern matching
    # is supposed to go up against might not be obvious.  Because the output will be static on a
    # given platform, we only produce this output once per discovery run, instead of every time
    # this sensor is run during a pass of discovery.  The idea is that each pass of discovery in a
    # long-running process would have the opportunity to reset the "have done this already" flags
    # right before the next pass of discovery is run, if such output is still desired.
    if ( $self->{show_resources} and not $self->{have_shown_resources}{os_type} ) {
	print "---------------------------------------\n";
	print "OS type, as determined during discovery\n";
	print "---------------------------------------\n";
	if ( defined $os_type ) {
	    print "$os_type\n";
	}
	else {
	    print "No OS type was determined.\n";
	}
	print "\n";
	$self->{have_shown_resources}{os_type} = 1;
    }

    local $_ = $os_type;
    if ( my @matched_values = map { $_ // '' } &{ $sensor->{match} } ) {
	## print "match is $matched_values[0]\n";
	## print "sensor matched\n";
	my %instance = ();
	$instance{qualified_resource} = $os_type;
	$instance{raw_match_strings}  = \@matched_values;
	$instance{instance_suffix}    = $sensor->{instance_suffix} // '' if $kind eq 'service';
	$instance{instance_cmd_args}  = $sensor->{instance_cmd_args} if defined $sensor->{instance_cmd_args};
	$instance{instance_ext_args}  = $sensor->{instance_ext_args} if defined $sensor->{instance_ext_args};
	push @instances, \%instance;
    }
    else {
	## print "sensor did not match\n";
    }

    return $sensor_outcome, $sensor_results, \@instances;
}

sub os_version_sensor {
    my $self           = shift;
    my $kind           = shift;
    my $tag            = shift;
    my $sensor         = shift;
    my $profile        = shift;
    my @instances      = ();
    my $sensor_outcome = 1;
    my $sensor_results = {
	error_message => undef,
    };

    my $os_version = '';

    # We use the POSIX package uname() routine instead of calling out to external programs, when possible.
    #
    #   "uname" Get name of current operating system.
    #
    #           ($sysname, $nodename, $release, $version, $machine) = POSIX::uname();
    #
    #           Note that the actual meanings of the various fields are not that
    #           well standardized, do not expect any great portability. The
    #           $sysname might be the name of the operating system, the $nodename
    #           might be the name of the host, the $release might be the (major)
    #           release number of the operating system, the $version might be the
    #           (minor) release number of the operating system, and the $machine
    #           might be a hardware identifier. Maybe.

    # The tests here are customized to yield the best possible determination for each respective platform.
    if ( $^O eq 'linux' ) {
	## The output of lsb_release depends on the Linux OS distribution; for instance, on my current
	## development system, it yields "7.4.1708", which is CentOS-specific.  But it is generally
	## more interesting than the kernel version that we might otherwise garner using qx(uname -r).
	if ( -x '/usr/bin/lsb_release' ) {
	    $os_version = $self->qx("lsb_release -r -s");
	    my $wait_status = $?;
	    if ( $wait_status != 0 ) {
		my $status_message = full_status_message( $wait_status, $! );
		$sensor_results->{error_message} = "Cannot run lsb_release; failed execution with $status_message.";
		$self->{logger}->error("ERROR:  $sensor_results->{error_message}");
		$sensor_outcome = 0;
	    }
	    else {
		chomp $os_version;
	    }
	}
	else {
	    $sensor_results->{error_message} = "The lsb_release program is not available, but it is needed to support the os_version sensor."
	      . "  Hint:  Is the redhat-lsb-core or lsb-release package installed?";
	    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
	    $sensor_outcome = 0;
	}
    }
    elsif ( $^O eq 'solaris' ) {
	## my $release = qx(uname -r);
	## my $version = qx(uname -v);
	## chomp $release;
	## chomp $version;
	require POSIX;
	my ($sysname, $nodename, $release, $version, $machine) = POSIX::uname();
	$os_version = "$release $version";
    }
    elsif ( $^O eq 'aix' ) {
	## my $version = qx(uname -v);
	## my $release = qx(uname -r);
	## chomp $version;
	## chomp $release;
	require POSIX;
	my ($sysname, $nodename, $release, $version, $machine) = POSIX::uname();
	$os_version = "${version}.${release}";
    }
    elsif ( $^O eq 'hpux' ) {
	## $os_version = qx(uname -r);
	## chomp $os_version;
	require POSIX;
	my ($sysname, $nodename, $release, $version, $machine) = POSIX::uname();
	$os_version = $release;
    }
    elsif ( $^O eq 'MSWin32' ) {
	if (0) {
	    ## This implementation will produce a simple string such as "Win2003", "Win2008", or
	    ## "Win2012", plus some additional unspecified detail in case that helps users make
	    ## useful distinctions.  The "Win2012" string will appear also under Windows 2016, so
	    ## it's not a full and clear discrimiation between even the base Windows releases.  See
	    ## https://blog.yaakov.online/finding-operating-system-version/ and GDMA-378 for more
	    ## information, if we decide to attempt to make this more definitive.
	    require Win32;
	    my ( $osname, $osedition ) = Win32::GetOSName();
	    $os_version = "$osname $osedition";
	}
	else {
	    ## Because of the limitations of the other alternative with regard to what shows up for
	    ## Windows 2016, we use a "wmic" command to obtain OS version information -- I would think
	    ## Microsoft must keep this information up-to-date with each OS version, despite what
	    ## goes on with the low-level numbering stuff.  Note that this output will contain some
	    ## ISO-8859-1 "registered" characters (0xAE) or the string "(R)", which will generally
	    ## be difficult to match.  So we strip that out, to make it easier for the consumer of
	    ## this version string.
	    ##
	    my @version_info = $self->qx("wmic path Win32_OperatingSystem get caption,csdversion,version");
	    my $wait_status = $?;
	    if ( $wait_status != 0 ) {
		my $status_message = full_status_message( $wait_status, $! );
		$sensor_results->{error_message} = "Cannot run wmic; failed execution with $status_message.";
		$self->{logger}->error("ERROR:  $sensor_results->{error_message}");
		$sensor_outcome = 0;
	    }
	    else {
		shift @version_info;    # drop heading
		foreach my $line (@version_info) {
		    $line =~ s/\s{2,}/ /g;
		    $line =~ s/\xAE//g;                   # kill the ISO-8859-1 "registered" character
		    $line =~ s/\(R\)//g;                  # kill the simulated "registered" character
		    $line =~ s/Microsoftr/Microsoft/g;    # kill the simulated "registered" character
		    $line =~ s/Serverr/Server/g;          # kill the simulated "registered" character
		    $line =~ s/(\S+)\s*$/Version $1/;
		    $os_version = $line;
		    last;
		}
	    }
	}
    }

    if ($sensor_outcome) {
	## We potentially print the OS version that we determined, since the strings the pattern matching
	## is supposed to go up against might not be obvious.  Because the output will be static on a
	## given platform, we only produce this output once per discovery run, instead of every time
	## this sensor is run during a pass of discovery.  The idea is that each pass of discovery in a
	## long-running process would have the opportunity to reset the "have done this already" flags
	## right before the next pass of discovery is run, if such output is still desired.
	if ( $self->{show_resources} and not $self->{have_shown_resources}{os_version} ) {
	    print "------------------------------------------\n";
	    print "OS version, as determined during discovery\n";
	    print "------------------------------------------\n";
	    if ( defined $os_version ) {
		print "$os_version\n";
	    }
	    else {
		print "No OS version was determined.\n";
	    }
	    print "\n";
	    $self->{have_shown_resources}{os_version} = 1;
	}

	local $_ = $os_version;
	if ( my @matched_values = map { $_ // '' } &{ $sensor->{match} } ) {
	    ## print "match is $matched_values[0]\n";
	    ## print "sensor matched\n";
	    my %instance = ();
	    $instance{qualified_resource} = $os_version;
	    $instance{raw_match_strings}  = \@matched_values;
	    $instance{instance_suffix}    = $sensor->{instance_suffix} // '' if $kind eq 'service';
	    $instance{instance_cmd_args}  = $sensor->{instance_cmd_args} if defined $sensor->{instance_cmd_args};
	    $instance{instance_ext_args}  = $sensor->{instance_ext_args} if defined $sensor->{instance_ext_args};
	    push @instances, \%instance;
	}
	else {
	    ## print "sensor did not match\n";
	}
    }

    return $sensor_outcome, $sensor_results, \@instances;
}

sub os_bitwidth_sensor {
    my $self           = shift;
    my $kind           = shift;
    my $tag            = shift;
    my $sensor         = shift;
    my $profile        = shift;
    my @instances      = ();
    my $sensor_outcome = 1;
    my $sensor_results = {
	error_message => undef,
    };

    my $os_bitwidth = 0;

    # The tests here are customized to yield the best possible determination for each respective platform.
    if ( $^O eq 'linux' ) {
	## my $machine = qx(/bin/uname -m);
	## chomp $machine;
	require POSIX;
	my ($sysname, $nodename, $release, $version, $machine) = POSIX::uname();
	## We could also potentially use qx(getconf LONG_BIT) for this.
	## "ppc64le" is a Little-Endian 64-bit PowerPC machine.
	$os_bitwidth = $machine eq 'x86_64' || $machine eq 'ppc64le' ? '64' : '32';
    }
    elsif ( $^O eq 'solaris' ) {
	## isainfo was introduced with Solaris 7, at the same time that 64-bit kernels became available.
	## So if isainfo is not found, you're running in 32-bit mode.
	my $kernel_isa = undef;
	if ( -f '/usr/bin/isainfo' ) {
	    $kernel_isa = $self->qx("/usr/bin/isainfo -kv");
	    my $wait_status = $?;
	    if ( $wait_status != 0 ) {
		my $status_message = full_status_message( $wait_status, $! );
		$sensor_results->{error_message} = "Cannot run isainfo; failed execution with $status_message.";
		$self->{logger}->error("ERROR:  $sensor_results->{error_message}");
		$sensor_outcome = 0;
	    }
	    else {
		chomp $kernel_isa;
	    }
	}
	else {
	    $kernel_isa = '';
	}
	$os_bitwidth = defined($kernel_isa) ? ( $kernel_isa =~ /64-bit/ ? '64' : '32' ) : '32' if $sensor_outcome;

    }
    elsif ( $^O eq 'aix' ) {
	$os_bitwidth = $self->qx("/usr/bin/getconf KERNEL_BITMODE");
	my $wait_status = $?;
	if ( $wait_status != 0 ) {
	    my $status_message = full_status_message( $wait_status, $! );
	    $sensor_results->{error_message} = "Cannot run getconf; failed execution with $status_message.";
	    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
	    $sensor_outcome = 0;
	}
	else {
	    chomp $os_bitwidth;
	    if ( $os_bitwidth !~ /^\d+$/ ) {
		my $kernel_file_type = $self->qx("/usr/bin/file /usr/lib/boot/unix");
		my $wait_status = $?;
		if ( $wait_status != 0 ) {
		    my $status_message = full_status_message( $wait_status, $! );
		    $sensor_results->{error_message} = "Cannot run the \"file\" command; failed execution with $status_message.";
		    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
		    $sensor_outcome = 0;
		}
		else {
		    $os_bitwidth = $kernel_file_type =~ /64-bit/ ? '64' : '32';
		}
	    }
	}
	## As a third alternative on this platform:
	##     % prtconf | fgrep 'CPU Type'
	##     CPU Type: 64-bit
	##     % prtconf | fgrep 'Kernel Type'
	##     Kernel Type: 64-bit
    }
    elsif ( $^O eq 'hpux' ) {
	$os_bitwidth = $self->qx("/usr/bin/getconf KERNEL_BITS");
	my $wait_status = $?;
	if ( $wait_status != 0 ) {
	    my $status_message = full_status_message( $wait_status, $! );
	    $sensor_results->{error_message} = "Cannot run getconf; failed execution with $status_message.";
	    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
	    $sensor_outcome = 0;
	}
	else {
	    chomp $os_bitwidth;
	}
    }
    elsif ( $^O eq 'MSWin32' ) {
	$os_bitwidth =
	  ( ( defined $ENV{PROCESSOR_ARCHITEW6432} and $ENV{PROCESSOR_ARCHITEW6432} =~ /64/ ) or $ENV{PROCESSOR_ARCHITECTURE} =~ /64/ )
	  ? '64'
	  : '32';
    }

    # We potentially print the OS bitwidth that we determined, since the strings the pattern
    # matching is supposed to go up against might not be obvious.  Because the output will be static
    # on a given platform, we only produce this output once per discovery run, instead of every time
    # this sensor is run during a pass of discovery.  The idea is that each pass of discovery in a
    # long-running process would have the opportunity to reset the "have done this already" flags
    # right before the next pass of discovery is run, if such output is still desired.
    if ( $self->{show_resources} and not $self->{have_shown_resources}{os_bitwidth} ) {
	print "-------------------------------------------\n";
	print "OS bitwidth, as determined during discovery\n";
	print "-------------------------------------------\n";
	if ( defined $os_bitwidth ) {
	    print "$os_bitwidth\n";
	}
	else {
	    print "No OS bitwidth was determined.\n";
	}
	print "\n";
	$self->{have_shown_resources}{os_bitwidth} = 1;
    }

    local $_ = $os_bitwidth;
    if ( my @matched_values = map { $_ // '' } &{ $sensor->{match} } ) {
	## print "match is $matched_values[0]\n";
	## print "sensor matched\n";
	my %instance = ();
	$instance{qualified_resource} = $os_bitwidth;
	$instance{raw_match_strings}  = \@matched_values;
	$instance{instance_suffix}    = $sensor->{instance_suffix} // '' if $kind eq 'service';
	$instance{instance_cmd_args}  = $sensor->{instance_cmd_args} if defined $sensor->{instance_cmd_args};
	$instance{instance_ext_args}  = $sensor->{instance_ext_args} if defined $sensor->{instance_ext_args};
	push @instances, \%instance;
    }
    else {
	## print "sensor did not match\n";
    }

    return $sensor_outcome, $sensor_results, \@instances;
}

sub machine_architecture_sensor {
    my $self           = shift;
    my $kind           = shift;
    my $tag            = shift;
    my $sensor         = shift;
    my $profile        = shift;
    my @instances      = ();
    my $sensor_outcome = 1;
    my $sensor_results = {
	error_message => undef,
    };

    my $processor = '';

    # The tests here are customized to yield the best possible determination for each respective platform.
    # That said, we generate a very generic name for each machine architecture, which ignores details of the
    # specific processor type, be it little-endian or big-endian, 32-bit or 64-bit, or whether it implements
    # various instruction-set extensions.  So for instance, a Big-Endian 64-bit PowerPC machine running AIX
    # will be seen as "powerpc", as will also a Little-Endian 64-bit PowerPC running Linux.

    if ( $^O eq 'linux' ) {
	## my $machine = qx(/bin/uname -m);
	## chomp $machine;
	require POSIX;
	my ( $sysname, $nodename, $release, $version, $machine ) = POSIX::uname();
	## On a 64-bit Intel machine, "uname -m", "uname -p", and "uname -i" all yield "x86_64", which we recode
	## to "intel".  I have only seen "i686" for a 32-bit Intel machine; "i586", "i486", and "i386" are just
	## precautionary.  On a Little-Endian 64-bit PowerPC machine, "uname -m", "uname -p", and "uname -i" all
	## yield "ppc64le", which we recode to "powerpc".
	my %processor_for_machine = (
	    x86_64  => 'intel',
	    i686    => 'intel',
	    i586    => 'intel',
	    i486    => 'intel',
	    i386    => 'intel',
	    ppc64le => 'powerpc',
	);
	$processor = $processor_for_machine{$machine} // 'unknown';
    }
    elsif ( $^O eq 'solaris' ) {
	## The defaulting of intel if not sparc is presumptive, since those are the only variants we support.
	my $isa_type = $self->qx("/usr/bin/uname -p");
	my $wait_status = $?;
	if ( $wait_status != 0 ) {
	    my $status_message = full_status_message( $wait_status, $! );
	    $sensor_results->{error_message} = "Cannot run uname; failed execution with $status_message.";
	    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
	    $sensor_outcome = 0;
	}
	else {
	    chomp $isa_type;
	    $processor = $isa_type =~ /sparc/ ? 'sparc' : 'intel';
	}
    }
    elsif ( $^O eq 'aix' ) {
	$processor = $self->qx("/usr/bin/uname -p");
	my $wait_status = $?;
	if ( $wait_status != 0 ) {
	    my $status_message = full_status_message( $wait_status, $! );
	    $sensor_results->{error_message} = "Cannot run uname; failed execution with $status_message.";
	    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
	    $sensor_outcome = 0;
	}
	else {
	    chomp $processor;
	}
    }
    elsif ( $^O eq 'hpux' ) {
	## We expect $processor to be "ia64" on all Itanium systems.
	## HP9000 machines are presumed to be some flavor of older, PA-RISC machines.
	## Possibly, we might want to distinguish PA-RISC 1.0, 1.1, and 2.0 machines
	## (via qx(getconf CPU_VERSION)), though this code does not yet do so.
	## my $machine = qx(/usr/bin/uname -m);
	## chomp $machine;
	require POSIX;
	my ($sysname, $nodename, $release, $version, $machine) = POSIX::uname();
	$processor = $machine eq 'ia64' ? 'ia64' : $machine =~ m{^9000/} ? 'parisc' : $machine;
    }
    elsif ( $^O eq 'MSWin32' ) {
	## Current Windows variants run on Intel, and in the future on ARM.
	## Very old versions of Windows support Alpha, MIPS, PowerPC, and Itanium.
	## This $processor assignment is presumptive, since this is currently the only variant we support.
	$processor = 'intel';
    }

    # We potentially print the machine architecture that we determined, since the strings the pattern
    # matching is supposed to go up against might not be obvious.  Because the output will be static
    # on a given platform, we only produce this output once per discovery run, instead of every time
    # this sensor is run during a pass of discovery.  The idea is that each pass of discovery in a
    # long-running process would have the opportunity to reset the "have done this already" flags
    # right before the next pass of discovery is run, if such output is still desired.
    if ( $self->{show_resources} and not $self->{have_shown_resources}{machine_architecture} ) {
	print "----------------------------------------------------\n";
	print "Machine architecture, as determined during discovery\n";
	print "----------------------------------------------------\n";
	if ( defined $processor ) {
	    print "$processor\n";
	}
	else {
	    print "No machine architecture was determined.\n";
	}
	print "\n";
	$self->{have_shown_resources}{machine_architecture} = 1;
    }

    local $_ = $processor;
    if ( my @matched_values = map { $_ // '' } &{ $sensor->{match} } ) {
	## print "match is $matched_values[0]\n";
	## print "sensor matched\n";
	my %instance = ();
	$instance{qualified_resource} = $processor;
	$instance{raw_match_strings}  = \@matched_values;
	$instance{instance_suffix}    = $sensor->{instance_suffix} // '' if $kind eq 'service';
	$instance{instance_cmd_args}  = $sensor->{instance_cmd_args} if defined $sensor->{instance_cmd_args};
	$instance{instance_ext_args}  = $sensor->{instance_ext_args} if defined $sensor->{instance_ext_args};
	push @instances, \%instance;
    }
    else {
	## print "sensor did not match\n";
    }

    return $sensor_outcome, $sensor_results, \@instances;
}

sub file_name_sensor {
    my $self    = shift;
    my $kind    = shift;
    my $tag     = shift;
    my $sensor  = shift;
    my $profile = shift;

    return $self->path_name_sensor($kind, $tag, $sensor, $profile, 'file');
}

sub symlink_name_sensor {
    my $self    = shift;
    my $kind    = shift;
    my $tag     = shift;
    my $sensor  = shift;
    my $profile = shift;

    return $self->path_name_sensor($kind, $tag, $sensor, $profile, 'symlink');
}

sub directory_name_sensor {
    my $self    = shift;
    my $kind    = shift;
    my $tag     = shift;
    my $sensor  = shift;
    my $profile = shift;

    return $self->path_name_sensor($kind, $tag, $sensor, $profile, 'directory');
}

# This routine implements the shared guts of file_name_sensor(), symlink_name_sensor(), and directory_name_sensor().
sub path_name_sensor {
    my $self           = shift;
    my $kind           = shift;
    my $tag            = shift;
    my $sensor         = shift;
    my $profile        = shift;
    my $pathtype       = shift;
    my @instances      = ();
    my $sensor_outcome = 1;
    my $sensor_results = {
	error_message => undef,
    };

    my @qualified_paths = ();
    my @resource_paths  = ();
    eval {
	@resource_paths = pathglob $sensor->{resource_globs};
    };
    if ($@) {
	$@ =~ s/\s+$//;  # chomp depends on $/, which might not contain what we expect
	$sensor_results->{error_message} = "Cannot find filepath(s) ($@).";
	$self->{logger}->error("ERROR:  $sensor_results->{error_message}");
	$sensor_outcome = 0;
    }
    foreach my $rpath (@resource_paths) {
	##
	## Note that our use of the "-f" operator here presumes that the user is looking specifically for a
	## regular plain file, not a named socket or pipe, block or character special file, device, or other odd
	## construction.  If we ever need to handle such things, we can implement other sensor types, as we have
	## already done for the open_named_socket sensor, that will allow them to be recognized as such.  More
	## or less for purity of expression, and possibly also to improve security, we also disallow the matched
	## filename actually being a symlink to some other file.
	##
	## Also note that the "-l" operator on Windows is not implemented to understand NTFS symlinks, as of yet
	## in existing releases of Perl.  So this implementation of the symlink_name sensor is not useful on
	## Windows.  In the future, we might consider some alternate implementation for that platform, though
	## standard Perl does not have a means of checking for symlinks on Windows.  There is a Win32::Links
	## package, on GitHub but not on CPAN, that could help with this if we really need it, but it is not
	## yet fully developed.  Also on Windows, we might be better served by having the symlink_name sensor
	## also treat Windows shortcuts, directory junctions, and NTFS reparse points as special kinds of
	## symlink-like filesystem objects.  The point being, we have intentionally limited Windows support in
	## this area for the time being, since it seems like its utility is low and the implementation is hard,
	## but we are aware of the basic issues should we need to extend the development in the future.
	##
	## See https://blogs.windows.com/buildingapps/2016/12/02/symlinks-windows-10/#rXo8c9BPVtaX0QfF.97
	## for information on improvements to symlink support in Windows 10.
	##
	push @qualified_paths, $rpath if $pathtype eq 'file' && !-l $rpath && -f _;
	push @qualified_paths, $rpath if $pathtype eq 'symlink'   && -l $rpath;
	push @qualified_paths, $rpath if $pathtype eq 'directory' && -d $rpath;
    }

    # We potentially print the list of pathnames that we found, since the strings the pattern
    # matching is supposed to go up against might not be obvious.  Because the output is often
    # voluminous, we only produce this output once per discovery run, instead of every time this
    # sensor is run during a pass of discovery.  The idea is that each pass of discovery in a
    # long-running process would have the opportunity to reset the "have done this already" flags
    # right before the next pass of discovery is run, if such output is still desired.
    #
    # Since the list of paths found will likely depend on the value of the sensor resource, we
    # track printing these results down to the level of each unique value of the resources that we
    # encounter.  (We sort the list of globs provided before declaring that we have such a unique
    # value in hand, to avoid excessive output just because the user happened to specify a different
    # ordering of the same globs.  However, we report the unsorted list back to the customer in the
    # heading, because that's what they will see in the their own instructions file.)
    #
    if ( $self->{show_resources} and not $self->{have_shown_resources}{"${pathtype}_name"}{ $sensor->{sorted_globs} } ) {
	my $heading =
	    "\u$pathtype path names for "
	  . ( @{ $sensor->{resource_globs} } == 1 ? 'fileglob' : 'fileglobs' )
	  . " ($sensor->{sorted_globs})"
	  . ', as found during discovery';
	my $demarcation = '-' x length($heading);
	print $demarcation, "\n";
	print $heading,     "\n";
	print $demarcation, "\n";
	if (@qualified_paths) {
	    foreach my $qpath (@qualified_paths) {
		print "$qpath\n";
	    }
	}
	else {
	    print "No $pathtype paths were found.\n";
	}
	print "\n";
	$self->{have_shown_resources}{"${pathtype}_name"}{ $sensor->{sorted_globs} } = 1;
    }

    foreach my $qpath (@qualified_paths) {
	local $_ = $qpath;
	if ( my @matched_values = map { $_ // '' } &{ $sensor->{match} } ) {
	    ## print "match is $matched_values[0]\n";
	    ## print "sensor matched\n";
	    my %instance = ();
	    $instance{qualified_resource} = $qpath;
	    $instance{raw_match_strings}  = \@matched_values;
	    $instance{instance_suffix}    = $sensor->{instance_suffix} // '' if $kind eq 'service';
	    $instance{instance_cmd_args}  = $sensor->{instance_cmd_args} if defined $sensor->{instance_cmd_args};
	    $instance{instance_ext_args}  = $sensor->{instance_ext_args} if defined $sensor->{instance_ext_args};
	    push @instances, \%instance;
	}
	else {
	    ## print "sensor did not match\n";
	}
    }

    return $sensor_outcome, $sensor_results, \@instances;
}

sub mounted_filesystem_sensor {
    my $self           = shift;
    my $kind           = shift;
    my $tag            = shift;
    my $sensor         = shift;
    my $profile        = shift;
    my @instances      = ();
    my $sensor_outcome = 1;
    my $sensor_results = {
	error_message => undef,
    };

    # This sensor accepts an optional resource that specifies the (comma or space separated) type(s)
    # of filesystems that qualify for matching (such as "nfs", "ext3", "ufs", "zfs" and so forth, or
    # "NTFS" or "CDFS" on a Windows platform).  Matching of filesystem types is case-insensitive.
    #
    # FIX LATER:  We might someday also support certain reserved resource strings like "/etc/fstab" or
    # "/etc/vfstab" (or just "fstab" or "vfstab") to specify special filtering based on the content
    # of the named file or other well-known object.

    my @fs_mount_points        = ();
    my %fs_type_of_mount_point = ();

    if (1) {
	if ( $^O eq 'MSWin32' ) {
	    ## On Windows, the formulation using Sys::Filesystem that we use for all the other platforms
	    ## has several difficulties.
	    ##
	    ## (*) It produces pathnames of the form "C:/" and "D:/".  To use that formulation, we would
	    ##     at a minimum need to turn the forward slashes into backslashes, to correspond to our
	    ##     automated adjustment of the sensor pattern to do the same.
	    ## (*) More critically, if "D:/" is a CD drive which is currently unmounted, this package may
	    ##     claim it is mounted.  Looking around, I don't yet see any way to determine whether this
	    ##     sort of mount point is actually mounted.
	    ## (*) There are problems in the implementation of the Win32::DriveInfo package that underlies
	    ##     the Sys::Filesystem::Mswin32 package.  See http://perlmonks.org/?node_id=824099 for
	    ##     details.  The net effect is that machine and path names are not adequately matched.
	    ## (*) On the Windows platform, this stack of packages provides no support for finding anything
	    ##     other than filesystems mounted directly as drive letters.  It thus won't be able to see
	    ##     any junction points elsewhere in the filesystem.
	    ##
	    ## As a replacement, the WMIC command below yields a sensible pathname for each potentially
	    ## mounted filesystem, including junction points (actual paths inside the filesystem) and
	    ## removable media (still just a drive letter).  However, it's not perfect either.
	    ##
	    ## (*) You might not be able to access a mounted volume at each of those paths.  That might
	    ##     be due to some network failure, or possibly because the mount point is listed somewhere
	    ##     in the system configuration but it hasn't been mounted since boot time.
	    ## (*) A path might reference an unmounted CD-ROM disc, or a System Reserved volume which is
	    ##     not actually readable, or a directory junction which is not currently mounted.
	    ## (*) Paths to UNC shares may not be listed.
	    ##
	    ## Even with those defects, this is still better than the alternative.  So we'll adopt it for
	    ## the time being, and perhaps figure out how to evolve it over time to cover the deficiencies.
	    ##
	    ## Note that we might get back mounted-file paths such as these:
	    ##     C:\
	    ##     C:\smalldisk\
	    ##     \\?\Volume{2bac18e8-2b26-11e8-81d8-806e6f6e6963}\

	    ## To see the filesystem type (e.g., "NTFS" or "CDFS") for matching against a sensor resource
	    ## specification, we fetch the "filesystem" field as well, and ignore all returned items that
	    ## have no first field (filesystem type).
	    if (1) {
		my @fs_types_and_mount_paths = $self->qx("wmic path Win32_Volume get filesystem,name");
		my $wait_status              = $?;
		if ( $wait_status != 0 ) {
		    my $status_message = full_status_message( $wait_status, $! );
		    $sensor_results->{error_message} = "Cannot run wmic; failed execution with $status_message.";
		    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
		    $sensor_outcome = 0;
		}
		else {
		    shift @fs_types_and_mount_paths;    # drop heading
		    foreach my $line (@fs_types_and_mount_paths) {
			$line =~ s/\s+$//;              # chomp depends on $/, which might not contain what we expect
			## We assume that we will have a filesystem type if and only if the filesystem is actually mounted.
			if ( $line =~ /^(\S+)\s+(\S+)/ ) {
			    my $fs_type    = $1;
			    my $mount_path = $2;
			    if ( not $sensor->{resource_match} or $sensor->{resource_match}($fs_type) ) {
				push @fs_mount_points, $mount_path;
				$fs_type_of_mount_point{$mount_path} = $fs_type;
			    }
			}
		    }
		}
	    }
	    else {
		my @mount_paths = $self->qx("wmic path Win32_Volume get name");
		my $wait_status = $?;
		if ( $wait_status != 0 ) {
		    my $status_message = full_status_message( $wait_status, $! );
		    $sensor_results->{error_message} = "Cannot run wmic; failed execution with $status_message.";
		    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
		    $sensor_outcome = 0;
		}
		else {
		    shift @mount_paths;    # drop heading
		    foreach my $line (@mount_paths) {
			$line =~ s/\s+$//;    # chomp depends on $/, which might not contain what we expect
			if ( $line ne '' ) {
			    ## FIX LATER:  Here is where we would further filter based on whether the path is actually mounted.
			    push @fs_mount_points, $line;
			}
		    }
		}
	    }
	}
	else {
	    ## As far as we know at the moment, except for difficulties on Windows, this is a nice portable
	    ## way to implement this sensor.  It does require that the Sys::Filesystem package be included in
	    ## GDMA Perl.  One nicety is that the $fs->format() call can retrieve the filesystem type.
	    my $fs                  = Sys::Filesystem->new();
	    my @mounted_filesystems = $fs->mounted_filesystems();
	    if (0) {
		## Old code, without possible filtering based on a sensor resource for the filesystem type.
		@fs_mount_points = map { $fs->mount_point($_) } @mounted_filesystems;
	    }
	    else {
		## New code, with possible filtering based on a sensor resource for the filesystem type.
		for my $filesystem (@mounted_filesystems) {
		    my $fs_type = $fs->format($filesystem) // '';
		    if ( not $sensor->{resource_match} or $sensor->{resource_match}($fs_type) ) {
			my $mount_path = $fs->mount_point($filesystem);
			push @fs_mount_points, $mount_path;
			$fs_type_of_mount_point{$mount_path} = $fs_type;
		    }
		}
	    }

	    ## FIX LATER:  Push a bug report upstream about the Sys::Filesystem documentation similar to this next line:
	    ## printf( "%s is a %s filesystem mounted on %s\n", $fs->device($_), $fs->format($_), $fs->mount_point($_) );

	    ## This is just for development testing.
	    if (0) {
		my @all_filesystems = $fs->filesystems();
		for (@all_filesystems) {
		    my $fs_type = $fs->format($_) // 'missing';
		    printf(
			"%s is %s %s filesystem which %s mounted on %s\n",
			$fs->device($_), $fs_type =~ /^[aeiuo]/i ? 'an' : 'a',
			$fs_type, ( $fs->mounted($_) ? 'is' : 'is not' ),
			$fs->mount_point($_)
		    );
		}
	    }
	}
    }
    else {
	## The tests here are customized to yield the best possible determination for each respective platform.
	## A question is, if we were to go in this direction, could we not only get the basic pathnames, but also
	## the filesystem types, so we could filter on those types once we add support for a resource to this sensor?
	if ( $^O eq 'linux' ) {
	    ## On Linux:  "mount | awk '{print $3}'" gets you a list of mount points; this seems to be the portable way to list mount points
	}
	elsif ( $^O eq 'solaris' ) {
	    ## On Solaris:  mount | awk '{print $1}'
	}
	elsif ( $^O eq 'aix' ) {
	    ## On AIX:  Skip the first two lines:  mount | tail +3 | perl -pe 's{^\S*\s+\S+\s+(\S+).*}{$1}'
	}
	elsif ( $^O eq 'hpux' ) {
	    ## On HP-UX:  This command is believed to be correct, mirroring Solaris, but has not been tested:  mount | awk '{print $1}'
	}
	elsif ( $^O eq 'MSWin32' ) {
	    ## On Windows:  If we didn't have wmic available, then something similar to the following might work,
	    ## but test it on wingdma-dev to check that we only see actual live mounted filesystems.
	    ## powershell "gwmi win32_volume | where-object { $_.filesystem -match 'ntfs' } | foreach-object { echo $_.name }"
	}
    }

    # In case the user might end up creating multiple instances that might benefit somehow from
    # alphabetic sorting, we impose a canonical ordering on the discovered mount points.  This
    # also helps with displaying the discovered mount points in a manner that makes them easier
    # to read.
    @fs_mount_points = sort @fs_mount_points;

    # We potentially print the list of filesystem mount points that we found, since the strings
    # the pattern matching is supposed to go up against might not be obvious.  Because the
    # output will be static on a given machine, we only produce this output once per discovery
    # run, instead of every time this sensor is run during a pass of discovery.  The idea is
    # that each pass of discovery in a long-running process would have the opportunity to reset
    # the "have done this already" flags right before the next pass of discovery is run, if such
    # output is still desired.
    if ( $self->{show_resources} and not $self->{have_shown_resources}{mounted_filesystem} ) {
	print "----------------------------------------------\n";
	print "Mounted filesystems, as found during discovery\n";
	print "----------------------------------------------\n";
	## Because the resource-filtering info is ordinarily somewhat hidden but is critical to the operation of this sensor,
	## we take extra trouble to expose the filesystem-type data here.
	if (@fs_mount_points) {
	    my $max_fs_type_len        = 7;
	    my $max_fs_mount_point_len = 11;
	    my $fs_type_len;
	    my $fs_mount_point_len;
	    foreach my $fs_mount_point (@fs_mount_points) {
		$fs_type_len            = length $fs_type_of_mount_point{$fs_mount_point};
		$fs_mount_point_len     = length $fs_mount_point;
		$max_fs_type_len        = $fs_type_len if $max_fs_type_len < $fs_type_len;
		$max_fs_mount_point_len = $fs_mount_point_len if $max_fs_mount_point_len < $fs_mount_point_len;
	    }
	    printf "%-${max_fs_type_len}s  %s\n", 'FS Type', 'Mount Point';
	    printf "%s  %s\n", '-' x $max_fs_type_len, '-' x $max_fs_mount_point_len;
	    foreach my $fs_mount_point (@fs_mount_points) {
		printf "%-${max_fs_type_len}s  %s\n", $fs_type_of_mount_point{$fs_mount_point}, $fs_mount_point;
	    }
	}
	else {
	    print "No mounted filesystems were found.\n";
	}
	print "\n";
	$self->{have_shown_resources}{mounted_filesystem} = 1;
    }

    foreach my $fs_mount_point (@fs_mount_points) {
	local $_ = $fs_mount_point;
	if ( my @matched_values = map { $_ // '' } &{ $sensor->{match} } ) {
	    ## print "match is $matched_values[0]\n";
	    ## print "sensor matched\n";
	    my %instance = ();
	    $instance{qualified_resource} = $fs_mount_point;
	    $instance{raw_match_strings}  = \@matched_values;
	    $instance{instance_suffix}    = $sensor->{instance_suffix} // '' if $kind eq 'service';
	    $instance{instance_cmd_args}  = $sensor->{instance_cmd_args} if defined $sensor->{instance_cmd_args};
	    $instance{instance_ext_args}  = $sensor->{instance_ext_args} if defined $sensor->{instance_ext_args};
	    push @instances, \%instance;
	}
	else {
	    ## print "sensor did not match\n";
	}
    }

    return $sensor_outcome, $sensor_results, \@instances;
}

sub file_content_sensor {
    my $self           = shift;
    my $kind           = shift;
    my $tag            = shift;
    my $sensor         = shift;
    my $profile        = shift;
    my @instances      = ();
    my $sensor_outcome = 1;
    my $sensor_results = {
	error_message => undef,
    };

    my @qualified_paths = ();
    my @resource_paths  = ();
    eval {
	@resource_paths = pathglob $sensor->{resource_globs};
    };
    if ($@) {
	$@ =~ s/\s+$//;  # chomp depends on $/, which might not contain what we expect
	$sensor_results->{error_message} = "Cannot find file to read ($@).";
	$self->{logger}->error("ERROR:  $sensor_results->{error_message}");
	$sensor_outcome = 0;
    }
    foreach my $rpath (@resource_paths) {
	## We silently skip paths which are not actual files.  Lucky for us, the -f operator excludes
	## named sockets, named pipes, ttys, and block and character special devices, so we don't need
	## to test for any of those explicitly.  That said, symlinks to regular files are treated as
	## regular files.
	next if !-f $rpath;
	push @qualified_paths, $rpath;
    }

    # We potentially print the list of pathnames that we found, since the paths the fileglobs
    # have generated might not be obvious.  Because the output should be static for a particular
    # set of fileglobs, we only produce this output once per discovery run, instead of every time
    # this sensor is run during a pass of discovery.  The idea is that each pass of discovery in a
    # long-running process would have the opportunity to reset the "have done this already" flags
    # right before the next pass of discovery is run, if such output is still desired.
    #
    # Since the list of paths found will likely depend on the value of the sensor resource, we
    # track printing these results down to the level of each unique value of the resources that we
    # encounter.  (We sort the list of globs provided before declaring that we have such a unique
    # value in hand, to avoid excessive output just because the user happened to specify a different
    # ordering of the same globs.  However, we report the unsorted list back to the customer in the
    # heading, because that's what they will see in the their own instructions file.)
    #
    if ( $self->{show_resources} and not $self->{have_shown_resources}{file_content}{ $sensor->{sorted_globs} } ) {
	my $heading =
	    "File-content path names for "
	  . ( @{ $sensor->{resource_globs} } == 1 ? 'fileglob' : 'fileglobs' )
	  . " ($sensor->{sorted_globs})"
	  . ', as found during discovery';
	my $demarcation = '-' x length($heading);
	print $demarcation, "\n";
	print $heading,     "\n";
	print $demarcation, "\n";
	if (@qualified_paths) {
	    foreach my $qpath (@qualified_paths) {
		print "$qpath\n";
	    }
	}
	else {
	    print "No filename paths were found.\n";
	}
	print "\n";
	$self->{have_shown_resources}{file_content}{ $sensor->{sorted_globs} } = 1;
    }

    if ( @qualified_paths == 0 ) {
	## The resource glob(s) matched no files.  This is not considered to be a sensor internal
	## failure; it is just taken to be a matching failure, treated as successful sensor
	## processing that happened to yield no instances of the pattern.
    }
    elsif ( @qualified_paths > 1 ) {
	## We complain that the sensor failed because the resource glob(s) matched more than one file.
	## This is treated as an internal error, in this case effectively an error in specifying the
	## discovery instructions.
	##
	## FIX MINOR:  As noted in the documentation, this restriction might be lifted in a future
	## implementation of this sensor; that would mostly just require putting the code in the next
	## clause in a loop, and updating the documentation.
	##
	$sensor_results->{error_message} = "Sensor resource \"$sensor->{resource}\" matched more than one file.";
	$self->{logger}->error("ERROR:  $sensor_results->{error_message}");
	$self->{logger}->error("NOTICE:  If there is a good use case for doing this, please communicate back to GroundWork.");
	$sensor_outcome = 0;
    }
    else {
	if ( not open CONTENT, '<', $qualified_paths[0] ) {
	    $sensor_results->{error_message} = "Cannot open $qualified_paths[0] ($!).";
	    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
	    $sensor_outcome = 0;
	}
	else {
	    local $_;
	    ## FIX LATER:  How to tell if we got a filesystem error while reading the file?  Would
	    ## that just be by checking whether we encounter an error upon closing the file handle?
	    ## Or is there an IO::Handle::error() routine or somesuch we should call to obtain such
	    ## information?  In practical terms, I have not been able to generate any errors while
	    ## trying to read a file for testing purposes, so this is only a back-burner issue.
	    ## Which is to say, hopefully any such errors will be reported when we try to close the
	    ## file, even if closing the file otherwise succeeds  Given that we haven't been able
	    ## to generate any file-reading errors yet, that remains just speculation.
	    while ( my $line = <CONTENT> ) {
		## FIX LATER:  Consider possibly allowing the pattern to match more than once in a single line of the
		## file.  But if we allow that by default, we would have a hard time disabling it when that behavior
		## is desired.  So perhaps some additional sensor attribute beyond just "resource" would be needed to
		## control its behavior, rather than just forcibly allowing all pattern matches in a single line to
		## be reported.  (We could implement a separate sensor type for this, but that seems like an extreme
		## response.  Extra controlling attributes seems like a better solution.)
		$_ = $line;
		if ( my @matched_values = map { $_ // '' } &{ $sensor->{match} } ) {
		    ## print "match is $matched_values[0]\n";
		    ## print "sensor matched\n";
		    ## We could have used $line instead for the qualified_resource, but then we wouldn't know where it came from.
		    my %instance = ();
		    $instance{qualified_resource} = $qualified_paths[0];
		    $instance{raw_match_strings}  = \@matched_values;
		    $instance{instance_suffix}    = $sensor->{instance_suffix} // '' if $kind eq 'service';
		    $instance{instance_cmd_args}  = $sensor->{instance_cmd_args} if defined $sensor->{instance_cmd_args};
		    $instance{instance_ext_args}  = $sensor->{instance_ext_args} if defined $sensor->{instance_ext_args};
		    push @instances, \%instance;
		}
	    }
	    if (not close CONTENT) {
		$sensor_results->{error_message} = "Problem found while closing the file handle for $qualified_paths[0] ($!).";
		$self->{logger}->error("ERROR:  $sensor_results->{error_message}");
		$sensor_outcome = 0;
	    }
	}
    }

    return $sensor_outcome, $sensor_results, \@instances;
}

sub running_system_service_sensor {
    my $self           = shift;
    my $kind           = shift;
    my $tag            = shift;
    my $sensor         = shift;
    my $profile        = shift;
    my @instances      = ();
    my $sensor_outcome = 1;
    my $sensor_results = {
	error_message => undef,
    };

    my @running_services = ();

    if ( $^O eq 'linux' ) {
	## On Linux:  Implement the equivalent of these commands, and concatenate their results.
	##
	## FIX MAJOR:  The "chkconfig --list" command is looking for services that are enabled in some state,
	## but not necessarily in the current state.  Do we care?  Should we figure out the current runlevel
	## (using the "runlevel" command) and filter by that as well?
	## chkconfig --list 2>/dev/null | fgrep :on | awk '{print $1}'
	## systemctl list-unit-files --full --state static,enabled,indirect --no-pager --no-legend | awk '{{print $1}}'
	##
	my @chkconfig_list = $self->qx("chkconfig --list 2>/dev/null");
	my $wait_status    = $?;
	if ( $wait_status != 0 ) {
	    my $status_message = full_status_message( $wait_status, $! );
	    $sensor_results->{error_message} = "Cannot run chkconfig; failed execution with $status_message.";
	    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
	    $sensor_outcome = 0;
	}
	else {
	    push @running_services, map /(\S+)/, grep /:\s*on/, @chkconfig_list;
	}
	if ( -x '/bin/systemctl' ) {
	    my @systemctl_list = $self->qx("systemctl list-unit-files --full --state static,enabled,indirect --no-pager --no-legend");
	    my $wait_status    = $?;
	    if ( $wait_status != 0 ) {
		my $status_message = full_status_message( $wait_status, $! );
		$sensor_results->{error_message} = "Cannot run systemctl; failed execution with $status_message.";
		$self->{logger}->error("ERROR:  $sensor_results->{error_message}");
		$sensor_outcome = 0;
	    }
	    else {
		push @running_services, map /(\S+)/, @systemctl_list;
	    }
	}
    }
    elsif ( $^O eq 'solaris' ) {
	## On Solaris:  Assume we're running on Solaris 10 or later, and that we can use "svcs -H -o FMRI".
	@running_services = $self->qx("svcs -H -o FMRI");
	my $wait_status = $?;
	if ( $wait_status != 0 ) {
	    my $status_message = full_status_message( $wait_status, $! );
	    $sensor_results->{error_message} = "Cannot run svcs; failed execution with $status_message.";
	    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
	    $sensor_outcome = 0;
	}
	else {
	    chomp @running_services;
	}
    }
    elsif ( $^O eq 'aix' ) {
	## Grab only the active services, and only the subsystem names for those services.
	my @all_services = $self->qx("lssrc -a");
	my $wait_status  = $?;
	if ( $wait_status != 0 ) {
	    my $status_message = full_status_message( $wait_status, $! );
	    $sensor_results->{error_message} = "Cannot run lssrc; failed execution with $status_message.";
	    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
	    $sensor_outcome = 0;
	}
	else {
	    @running_services = map /(\S+)/, grep / active$/, @all_services;
	    chomp @running_services;
	}
    }
    elsif ( $^O eq 'hpux' ) {
	## On HP-UX:  The proper mechanism to use on this platform is as yet unknown.
	## At present, we flag an error if we get here, so we can have a discussion with
	## the customer who needs this and in that manner figure out how to implement it.
	##
	## FIX MAJOR:  implement this case
	$sensor_results->{error_message} = "The running_system_service sensor is not yet implemented on the HP-UX platform.";
	$self->{logger}->error("ERROR:  $sensor_results->{error_message}");
	$sensor_outcome = 0;
    }
    elsif ( $^O eq 'MSWin32' ) {
	## On Windows:  Use "sc query" or equivalent, which is what Win32::Service effectively does at the system-call level.
	## If we have difficulty with this implementation, we could base the sensor data on output from:
	##     wmic path Win32_Service get name,state
	my %services = ();
	require Win32::Service;
	eval {
	    Win32::Service->VERSION('0.07');  # require a minimum package version
	};
	if ($@) {
	    $@ =~ s/\s+$//;  # chomp depends on $/, which might not contain what we expect
	    $sensor_results->{error_message} = "The running_system_service sensor failed ($@).";
	    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
	    $sensor_outcome = 0;
	}
	elsif ( not Win32::Service::GetServices( '', \%services ) ) {
	    $sensor_results->{error_message} = "The running_system_service sensor failed to obtain a list of system services ($^E).";
	    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
	    $sensor_outcome = 0;
	}
	else {
	    ## We have in hand %services, which uses the descriptive service names as keys and the short names as the values,
	    ## enumerates both active and inactive services, and within the active services, lists both running services and
	    ## those in a variety of other states.
	    ##
	    ## We could save the work of looking up service states we won't care about later on, if we pre-filtered the service
	    ## names here using the sensor pattern.  But if we do that, we won't be able to spill out a list of all the running
	    ## services.  That is extremely valuable for development and diagnostic purposes, so the extra effort we incur here
	    ## is definitely worthwhile.
	    ##
	    foreach my $service_name ( values %services ) {
		my %service_status = ();
		if ( not Win32::Service::GetStatus( '', $service_name, \%service_status ) ) {
		    $sensor_results->{error_message} = "The running_system_service sensor failed to obtain the status of service \"$service_name\" ($^E).";
		    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
		    $sensor_outcome = 0;
		}
		else {
		    ## The Win32::Service::SERVICE_RUNNING() function is not documented, but it seems to be available anyway.
		    push @running_services, $service_name if $service_status{CurrentState} == Win32::Service::SERVICE_RUNNING();
		}
	    }
	}
    }

    # We potentially print the list of running system services that we found, since the strings the
    # pattern matching is supposed to go up against might not be obvious.  Because the output is
    # often voluminous, we only produce this output once per discovery run, instead of every time
    # this sensor is run during a pass of discovery.  The idea is that each pass of discovery in a
    # long-running process would have the opportunity to reset the "have done this already" flags
    # right before the next pass of discovery is run, if such output is still desired.
    if ( $self->{show_resources} and not $self->{have_shown_resources}{running_system_service} ) {
	print "--------------------------------------------------\n";
	print "Running system services, as found during discovery\n";
	print "--------------------------------------------------\n";
	if (@running_services) {
	    foreach my $service ( sort @running_services ) {
		print "$service\n";
	    }
	}
	else {
	    print "No running system services were found.\n";
	}
	print "\n";
	$self->{have_shown_resources}{running_system_service} = 1;
    }

    foreach my $service (@running_services) {
	local $_ = $service;
	if ( my @matched_values = map { $_ // '' } &{ $sensor->{match} } ) {
	    ## print "match is $matched_values[0]\n";
	    ## print "sensor matched\n";
	    my %instance = ();
	    $instance{qualified_resource} = $service;
	    $instance{raw_match_strings}  = \@matched_values;
	    $instance{instance_suffix}    = $sensor->{instance_suffix} // '' if $kind eq 'service';
	    $instance{instance_cmd_args}  = $sensor->{instance_cmd_args} if defined $sensor->{instance_cmd_args};
	    $instance{instance_ext_args}  = $sensor->{instance_ext_args} if defined $sensor->{instance_ext_args};
	    push @instances, \%instance;
	}
	else {
	    ## print "sensor did not match\n";
	}
    }

    return $sensor_outcome, $sensor_results, \@instances;
}

sub full_process_command_sensor {
    my $self           = shift;
    my $kind           = shift;
    my $tag            = shift;
    my $sensor         = shift;
    my $profile        = shift;
    my @instances      = ();
    my $sensor_outcome = 1;
    my $sensor_results = {
	error_message => undef,
    };

    my @matched_processes = ();

    # Here are the generic commands that seem to be supported on all UNIX-like platforms:
    #
    #     ps -u 'userlist' -o args=
    #     ps -e -o args=
    #
    # Some UNIX-like platforms can truncate the output width, and there may or may not be
    # anything we can do about that.  So instead of just using one command definition for
    # all such platforms, we split out the cases into the individual platforms so they can
    # be specially tuned if necessary and possible.
    #
    # Solaris does not support such options, and AIX claims it supports the equivalent "ww"
    # but in practice on AIX 5.3 such arguments are rejected.

    my @all_processes = ();

    if ( $^O eq 'linux' || $^O eq 'solaris' || $^O eq 'aix' || $^O eq 'hpux' ) {
	my $ps_command = undef;
	if ( $^O eq 'linux' ) {
	    ## On Linux:  See GWMON-8714 for why we use the "-w -w" options (it used to be an
	    ## issue in a similar context on a SLES-10 system).  This doesn't seem to be a
	    ## problem any more in my testing (e.g., of SLES-11), but using these options on
	    ## this platform doesn't seem to hurt, either.
	    $ps_command = defined( $sensor->{resource} ) ? "ps -w -w -u '$sensor->{resource}' -o args=" : 'ps -w -w -e -o args=';
	}
	elsif ( $^O eq 'solaris' ) {
	    ## On Solaris:  Command-line output is claimed to be truncated to 80 characters (actual
	    ## testing hows 79 characters max), and there is nothing we can do about that (no options
	    ## are provided to extend the width) unless we use the BSD /usr/ucb/ps command, which might
	    ## not even be available.  So for now, we just live with what's guaranteed to be available.
	    $ps_command = defined( $sensor->{resource} ) ? "ps -u '$sensor->{resource}' -o args=" : 'ps -e -o args=';
	}
	elsif ( $^O eq 'aix' ) {
	    ## On AIX:  The "man ps" page claims it would support "ww" to extend the width, but in
	    ## practice on AIX 5.3 such arguments are rejected.  I don't know why there is such a
	    ## mismatch between the doc and the program itself.  On the other hand, actually
	    ## executing the standard "/usr/bin/ps" command doesn't seem to show any truncation,
	    ## so perhaps we won't have a problem on this platform.
	    $ps_command = defined( $sensor->{resource} ) ? "ps -u '$sensor->{resource}' -o args=" : 'ps -e -o args=';
	}
	elsif ( $^O eq 'hpux' ) {
	    ## On HP-UX:  It may be necessary to provide the absolute pathname to the "ps" command
	    ## on this platform, to force the use of the XPG4 version of the command so the "-o format"
	    ## option is valid.  We won't know for sure until we can test on this platform.
	    $ps_command = defined( $sensor->{resource} ) ? "ps -u '$sensor->{resource}' -o args=" : 'ps -e -o args=';
	}

	@all_processes = $self->qx($ps_command);
	my $wait_status = $?;
	if ( $wait_status != 0 ) {
	    my $status_message = full_status_message( $wait_status, $! );
	    $sensor_results->{error_message} = "Cannot run ps; failed execution with $status_message.";
	    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
	    $sensor_outcome = 0;
	}
	else {
	    chomp @all_processes;
	}
    }
    elsif ( $^O eq 'MSWin32' ) {
	## We have tried a LOT of different approaches to retrieving process data on Windows, including
	## quite a few Perl packages, and settled on "tasklist" and "wmic" as the best approach.

	# We use the standard system commands "tasklist" and "wmic" to gather process data, partly for portability
	# and convenience and partly because they are apparently equipped to retrieve some data that we cannot
	# access directly via other means, even when running as an administrator.  That said, when GDMA is run as
	# a non-admin user, the ability to access user-name and command-line data for each process may be severely
	# restricted, largely only to processes that the current user is running.  This will likely severely limit
	# the utility of this sensor in such a context.

	my @tasklist    = $self->qx("tasklist /v /fo csv");
	my $wait_status = $?;
	if ( $wait_status != 0 ) {
	    my $status_message = full_status_message( $wait_status, $! );
	    $sensor_results->{error_message} = "Cannot run tasklist; failed execution with $status_message.";
	    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
	    $sensor_outcome = 0;
	}
	else {
	    my $heading  = shift @tasklist;
	    $heading =~ s/\s+$//;  # chomp depends on $/, which might not contain what we expect
	    my @heading_fields = split /^"|","|"$/, $heading;
	    my $pid_index      = undef;
	    my $username_index = undef;
	    for ( my $i = 0 ; $i <= $#heading_fields ; ++$i ) {
		$pid_index      = $i if $heading_fields[$i] eq 'PID';
		$username_index = $i if $heading_fields[$i] eq 'User Name';
	    }
	    my $pid      = undef;
	    my $username = undef;
	    my %pid_user = ();
	    foreach my $line (@tasklist) {
		$line =~ s/\s+$//;  # chomp depends on $/, which might not contain what we expect
		( $pid, $username ) = ( split( /^"|","|"$/, $line ) )[ $pid_index, $username_index ];
		## Process user names are generally found in MyDomain\MyUserName format.
		## Here we filter by the username(s) listed in the sensor resource, if given.
		if ( not $sensor->{resource_match} or $sensor->{resource_match}($username) ) {
		    $pid_user{$pid} = $username;
		}
	    }
	    ## Note that each process is free to change its own command line as sensed by WMI.  So what
	    ## we find here is not necessarily exactly the same as the arguments that were used when the
	    ## process was started.  We don't use the CSV format from "wmic" because it is not formatted
	    ## correctly (fields are never quoted, even if they contain embedded commas or quotes).
	    ##
	    ## FIX MINOR:  As far as I know, the full path to wmic is "C:\Windows\System32\wbem\wmic".
	    ## Perhaps we should call wmic by its full path, in case its directory is not listed by
	    ## default in the %PATH% of the GDMA run-as user.  However, I don't know how stable that
	    ## path is across the whole range of Windows releases, or how unlikely it is that wmic
	    ## will not be found in %PATH%.  At least the stability of the directory needs testing.
	    ##
	    my $cmdline;
	    my @cmdline_pids = $self->qx("wmic path Win32_Process get commandline,processid");
	    my $wait_status  = $?;
	    if ( $wait_status != 0 ) {
		my $status_message = full_status_message( $wait_status, $! );
		$sensor_results->{error_message} = "Cannot run wmic; failed execution with $status_message.";
		$self->{logger}->error("ERROR:  $sensor_results->{error_message}");
		$sensor_outcome = 0;
	    }
	    else {
		shift @cmdline_pids;    # drop heading
		foreach my $line (@cmdline_pids) {
		    if ( $line =~ m{^(\S.*)\s+(\d+)\s*$} ) {
			$pid = $2;
			( $cmdline = $1 ) =~ s/\s+$//;
			push @all_processes, $cmdline if exists $pid_user{$pid};
		    }
		}
	    }
	}
    }

    # We potentially print the list of full process commands that we found, since the strings the
    # resource specification and the pattern matching are supposed to go up against might not
    # be obvious.  Because the output is often voluminous, we only produce this output once per
    # discovery run, instead of every time this sensor is run during a pass of discovery.  The
    # idea is that each pass of discovery in a long-running process would have the opportunity
    # to reset the "have done this already" flags right before the next pass of discovery is
    # run, if such output is still desired.
    #
    # Since the list of processes found will likely depend on whether the sensor resource is
    # provided and what its value is, we track printing these results down to the level of each
    # unique value of the resources that we encounter.  (We sort the list of usernames provided
    # before declaring that we have such a unique value in hand, to avoid excessive output just
    # because the user happened to specify a different ordering of the same names.  However, we
    # report the unsorted list back to the customer in the heading, because that's what they will
    # see in the their own instructions file.)  Since "*" is an invalid resource for this sensor
    # type's resource, we use it as a safe, non-conflicting hash key for the situation where no
    # resource was provided.
    #
    # Of course, the list of processes might change between sensor runs, so this can only be
    # an approximation of what will be found for later executions of this sensor.  But then,
    # if you are wanting to depend on a dynamic sensor like this, you have to be prepared for
    # the possible consequences.
    #
    my $sorted_users = $sensor->{sorted_fc_users} // '*';
    if ( $self->{show_resources} and not $self->{have_shown_resources}{full_process_command}{$sorted_users} ) {
	my $heading =
	  'Full process commands for '
	  . (
	    defined( $sensor->{fc_users} )
	    ? ( ( ( @{ $sensor->{fc_users} } == 1 ) ? 'user' : 'users' ) . " \"$sensor->{sorted_fc_users}\"" )
	    : 'all users' )
	  . ', as found during discovery';
	my $demarcation = '-' x length($heading);
	print $demarcation, "\n";
	print $heading,     "\n";
	print $demarcation, "\n";
	if (@all_processes) {
	    foreach my $process_command ( sort @all_processes ) {
		print "$process_command\n";
	    }
	}
	else {
	    print "No processes were found.\n";
	}
	print "\n";
	$self->{have_shown_resources}{full_process_command}{$sorted_users} = 1;
    }

    foreach my $process_command (@all_processes) {
	local $_ = $process_command;
	if ( my @matched_values = map { $_ // '' } &{ $sensor->{match} } ) {
	    ## print "match is $matched_values[0]\n";
	    ## print "sensor matched\n";
	    my %instance = ();
	    $instance{qualified_resource} = $process_command;
	    $instance{raw_match_strings}  = \@matched_values;
	    $instance{instance_suffix}    = $sensor->{instance_suffix} // '' if $kind eq 'service';
	    $instance{instance_cmd_args}  = $sensor->{instance_cmd_args} if defined $sensor->{instance_cmd_args};
	    $instance{instance_ext_args}  = $sensor->{instance_ext_args} if defined $sensor->{instance_ext_args};
	    push @instances, \%instance;
	}
	else {
	    ## print "sensor did not match\n";
	}
    }

    return $sensor_outcome, $sensor_results, \@instances;
}

# internal routine
#
# It is up to the caller to ensure that the IP address and the CIDR block arguments are both of the
# same type (either both IPv4 or both IPv6).  It is also up to the caller to rule out a wildcard
# used as the IP address, both because that case is not well handled by this implementation and
# because it would not make sense anyway to match a wildcard address against a negated CIDR block,
# a context which will only be known to the caller, not to to this routine.
#
# This routine does not do any special handling to deal with a possible mapped IPv4-in-IPv6 address.
# But it shouldn't have to, because those address types should not be mixed together by the caller.
#
sub address_is_within_cidr_block {
    my $self       = shift;
    my $ip_address = shift;
    my $cidr_block = shift;

    # We could have possibly used these Perl packages, perhaps among others, as alternatives:
    #     Net::CIDR
    #     Net::CIDR::Compare
    #     Net::CIDR::Set
    #     Net::CIDR::Set::IPv4
    #     Net::CIDR::Set::IPv6

    my $ip   = NetAddr::IP::Lite->new($ip_address);
    my $cidr = NetAddr::IP::Lite->new($cidr_block);

    # This handles standard wildcard CIDR blocks ('0.0.0.0/0' or '::/0') as well as ordinary restricted
    # address ranges.
    my $ip_is_in_cidr = $cidr->contains($ip);

    return $ip_is_in_cidr;
}

sub address_matches_cidr_blocks {
    my $self        = shift;
    my $ip_address  = shift;
    my $cidr_blocks = shift;

    # Here we deal with the intended interpretation of the sensor resource.
    my $address_matches_positive_resource = 0;
    my $address_matches_negative_resource = 0;

    # We must test first for an IPv6 address, because an IPv4-in-IPv6 address could come in the form "::ffff:172.28.113.60"
    # which contains some dot characters.  We don't want to mistake the presence of a dot character to mean we should treat
    # that as an IPv4 address and match it against IPv4 CIDR blocks.
    if ( $ip_address =~ m{:} ) {
	## We have an IPv6 address in hand.  Compare it only to the supplied IPv6 resources, if any.
	##
	## The $ip_address may contain enclosing square brackets, but in our implementation they do not interfere
	## with operation of the $self->address_is_within_cidr_block() routine.  So we don't need to strip them here.
	## However, we do need to account for them when we figure out whether the $ip_address is a wildcard.
	##
	## The $ip_address may contain an %interface suffix, say for a link-local IPv6 address, that won't be
	## handled correctly by our address_is_within_cidr_block() routine.  So we must strip it out here before
	## doing CIDR-block matching.
	$ip_address =~ s/%[^\]]*//;

	foreach my $cidr_block ( @{ $cidr_blocks->{IPv6} } ) {
	    my $cidr_block_is_negated = $cidr_block =~ m{^!};
	    ## We avoid modification of $cidr_block for future use due to aliasing of what's in the underlying array,
	    ## by modifying a separate variable instead.
	    ( my $base_cidr_block = $cidr_block ) =~ s{^!}{};
	    ## A wildcarded IP address is supposed to represent a port that can be opened on any network interface,
	    ## and thus must match any positive CIDR block the user has specified.  Since the routine for comparing
	    ## individual IP addresses with CIDR blocks does not necessarily treat the wildcard IP address in this
	    ## way, we short-circuit the evaluation at this level in that case.  On the other hand, if we also
	    ## short-circuited evaluation of the wildcard IP address against a negative CIDR block, we would either
	    ## always match the CIDR block, meaning we would block matching of such a port even if the user was trying
	    ## to exclude only some particular address range, or we would never match the CIDR block, meaning there
	    ## would be no way to suppress matches of the wildcard IP address when you want to restrict matching to only
	    ## a specific positive CIDR block.  So in the case of a negative CIDR block, the wildcard IP address is
	    ## treated as an ordinary address and not treated as a wildcard, and no short-circuiting of the evaluation
	    ## takes place.  This allows the user to specify "!::/128" as part of the sensor resource to ignore ports
	    ## which are open with an arbitrary IPv6 address.
	    if ( ( !$cidr_block_is_negated && $ip_address =~ m{^\[?::\]?$} )
		|| $self->address_is_within_cidr_block( $ip_address, $base_cidr_block ) )
	    {
		( $cidr_block_is_negated ? $address_matches_negative_resource : $address_matches_positive_resource ) = 1;
	    }
	}
    }
    elsif ( $ip_address =~ m{[.]} ) {
	## We have an IPv4 address in hand.  Compare it only to the supplied IPv4 resources, if any.
	foreach my $cidr_block ( @{ $cidr_blocks->{IPv4} } ) {
	    my $cidr_block_is_negated = $cidr_block =~ m{^!};
	    ## We avoid modification of $cidr_block for future use due to aliasing of what's in the underlying array,
	    ## by modifying a separate variable instead.
	    ( my $base_cidr_block = $cidr_block ) =~ s{^!}{};
	    ## A wildcarded IP address is supposed to represent a port that can be opened on any network interface,
	    ## and thus must match any positive CIDR block the user has specified.  Since the routine for comparing
	    ## individual IP addresses with CIDR blocks does not necessarily treat the wildcard IP address in this
	    ## way, we short-circuit the evaluation at this level in that case.  On the other hand, if we also
	    ## short-circuited evaluation of the wildcard IP address against a negative CIDR block, we would either
	    ## always match the CIDR block, meaning we would block matching of such a port even if the user was trying
	    ## to exclude only some particular address range, or we would never match the CIDR block, meaning there
	    ## would be no way to suppress matches of the wildcard IP address when you want to restrict matching to only
	    ## a specific positive CIDR block.  So in the case of a negative CIDR block, the wildcard IP address is
	    ## treated as an ordinary address and not treated as a wildcard, and no short-circuiting of the evaluation
	    ## takes place.  This allows the user to specify "!0.0.0.0/32" as part of the sensor resource to ignore
	    ## ports which are open with an arbitrary IPv4 address.
	    if ( ( !$cidr_block_is_negated && $ip_address eq '0.0.0.0' )
		|| $self->address_is_within_cidr_block( $ip_address, $base_cidr_block ) )
	    {
		( $cidr_block_is_negated ? $address_matches_negative_resource : $address_matches_positive_resource ) = 1;
	    }
	}
    }

    my $matches = $address_matches_positive_resource && !$address_matches_negative_resource;

    return $matches;
}

# Internal routine, to use in complex sorting.
sub addr_type_and_bits {
    my $addr = shift;
    if ( $addr =~ /:/ ) {
	## We strip possible enclosing brackets and a possible %interface suffix.
	$addr =~ s/\[//;
	$addr =~ s/[%\]].*//;
	return 6, Socket::inet_pton( AF_INET6, $addr );
    }
    else {
	return 4, Socket::inet_pton( AF_INET, $addr );
    }
}

sub open_local_port_sensor {
    my $self           = shift;
    my $kind           = shift;
    my $tag            = shift;
    my $sensor         = shift;
    my $profile        = shift;
    my @instances      = ();
    my $sensor_outcome = 1;
    my $sensor_results = {
	error_message => undef,
    };

    # FIX LATER:  Look to see if there is any portable Perl package we might have used in the implementation
    # of this sensor.  Look at Parse::Netstat to begin with, though it doesn't have explicit AIX or HP-UX support.
    # Support for those platforms could probably be added and sent upstream.  See also the App::ParseNetstat and
    # parse-netstat packages.  If we do find any interesting packages, make sure they have been kept up-to-date
    # with current versions of the operating systems they purport to support.

    my @open_addresses_and_ports = ();

    # Assume we have a CIDR block as a resource, and look for open ports within that CIDR block.
    # We filter the list of open TCP ports to just those in a LISTENING state, ignoring those in
    # an ESTABLISHED connection.  We then match the list of open ports against the sensor pattern.

    if ( $^O eq 'linux' ) {
	## On Linux:
	##
	## netstat -lntu | tail -n +3 | awk '{print $4}'
	##
	## yields ipaddr:port on each line, where ipaddr could be either an IPv4 address or an
	## IPv6 address without containing square brackets.
	## Also, some lines might have an IPv4-in-IPv6 ipaddr, such as:  ::ffff:127.0.0.1:8805
	## And there may be some duplicates, such as:
	##
	##     0.0.0.0:111
	##     0.0.0.0:111
	##     0.0.0.0:68
	##     0.0.0.0:68
	##     :::111
	##     :::111
	##
	## Such duplicates might arise either from the same process listening for both TCP and UDP
	## connections (e.g., one rpcbind process listening on port 111 for both TCP and UDP connections,
	## either IPv4 and IPv6), or from multiple processes having the same port open on the same protocol
	## (e.g., two dhclient processes both having UDP port 68 open).  From a sensor point of view, we
	## might be tempted to just leave it up to the customer as to how to deal with such situations in
	## the discovery instructions, since they will necessarily be application-specific.  However, if
	## both the IP address and the port number match like this, there would be no way to distinguish
	## the multiple instances of a match in the discovery instructions in terms of generating unique
	## instance_suffix values.  So for the time being, we will de-dup the list of address:port values
	## we gather up, before attempting to match any of them against the sensor pattern.
	##
	## Sample lines:
	##     0.0.0.0:123
	##     0.0.0.0:161
	##     0.0.0.0:162
	##     0.0.0.0:22
	##     127.0.0.1:10090
	##     127.0.0.1:25
	##     127.0.0.1:3000
	##     172.28.113.190:123
	##     172.28.113.39:123
	##     2001:550:2:c::26:8011:123
	##     ::1:123
	##     :::111
	##     :::111
	##     :::123
	##     :::161
	##     :::22
	##     ::ffff:127.0.0.1:8805
	##     ::ffff:127.0.0.1:8809
	##     ::ffff:127.0.0.1:8888
	##     fe80::250:56ff:fea0:45eb:123
	##     fe80::250:56ff:feb1:9cd3:123
	##
	## By not filtering explicitly for the word LISTEN, but instead just ignoring the first two lines
	## from the netstat command as representing headers, we include udp and udp6 sockets in the data
	## that we capture.
	##
	## It's possible we might need the netstat -T (older) or -W (newer) flag to avoid trunction of the IP address.
	## I haven't see that be a problem in my initial testing, but it's something to watch for.  Alas, we would need
	## to test which version of netstat we're running in order to figure out which flag to use.
	##
	## We open a pipe to the netstat command instead of using a bulk read from a qx() execution because we believe
	## this gives us much better control over recognizing possible errors during the reading.
	##
	## FIX MINOR:  Convert this to use our standard $self->qx() routine.
	##
	if ( not open NETSTAT, '-|', 'netstat -lntu' ) {
	    $sensor_results->{error_message} = "Cannot open a pipe to read from the netstat command ($!).";
	    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
	    $sensor_outcome = 0;
	}
	else {
	    local $_;
	    while (<NETSTAT>) {
		## We skip two lines of headers before reaching the data we want to see,
		## and choose the fourth field of each line thereafter.
		if ( $. > 2 && /^\S+\s+\S+\s+\S+\s+(\S+)/ ) {
		    push @open_addresses_and_ports, $1;
		}
	    }
	    ## Closing this filehandle waits for the child process to finish.
	    if ( not close NETSTAT ) {
		## Check for child-process error.
		if ($!) {
		    ## Error ($!) closing the pipe (i.e., some problem within Perl or the OS, not the piped program).
		    ## (It is possible that the piped program independently experienced some failure, and that would still
		    ## be reflected in $?, but in light of the problem at the Perl level, we're going to ignore that.)
		    ## Presumably the file descriptor itself did still get closed, but it left behind evidence of some issue.
		    $sensor_results->{error_message} = "Problem found while closing a pipe from the netstat command ($!).";
		    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
		}
		else {
		    my $status_message = full_status_message( $?, $! );
		    $sensor_results->{error_message} = sprintf( "netstat exited with $status_message." );
		    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
		}
		$sensor_outcome = 0;
	    }
	}
    }
    elsif ( $^O eq 'solaris' ) {
	## On Solaris:
	##
	## On this platform, we must probe for TCP and UDP sockets separately, since the filtering is slightly different
	## for those two protocol types, and for inet and inet6 separately, so we can match IPv4-only and IPv6-only
	## resource components properly and handle the wildcarded "*" address often seen in netstat output differently
	## for IPv4 and IPv6.
	##
	## netstat -a -n -P tcp -f inet  | fgrep LISTEN | awk '{print $1}' | sort -u | sed -e 's/^\*\./0.0.0.0./'
	## netstat -a -n -P tcp -f inet6 | fgrep LISTEN | awk '{print $1}' | sort -u | sed -e 's/^\*\./::./'
	## netstat -a -n -P udp -f inet  | fgrep Idle   | awk '{print $1}' | sort -u | sed -e 's/^\*\./0.0.0.0./'
	## netstat -a -n -P udp -f inet6 | fgrep Idle   | awk '{print $1}' | sort -u | sed -e 's/^\*\./::./'
	##
	## Note that in such output, for both TCP and UDP, one process can sometimes be seen as having more than one
	## copy of a listening port on exactly the same address:port combination, for the same network type (either IPv4
	## or IPv6).  We will therefore want to de-duplicate the raw netstat output before further processing.  As far
	## as I can see with the probing done so far (not shown here), the copies differ only in "Mss", which I take to
	## mean "Maximum segment size".  Why this occurs is unknown.
	##
	## Note that the same address:port combination (e.g., "*.111") can appear in both IPv4 and IPv6 contexts,
	## so combining even de-duplicated elements at the level of each netstat command alone won't eliminate all
	## duplication.  What we really need to do is to transform the "*" in each context to a different form
	## (e.g., "0.0.0.0" or "::"), to eliminate this false aliasing and prepare the addresses for downstream
	## processing.  The selection of substituted address has to be done very carefully, as it is supposed to match
	## every possible CIDR range.  Or perhaps all positive CIDR ranges and no negative CIDR ranges.  This may well
	## be tricky, and not handled properly by standard Perl package CIDR-block matching.  We might need to handle
	## wildcard address matching outside of such package calls.
	##
	## Also note that for UDP sockets in particular, we filter out those in the "Unbound" state, inasmuch as they
	## typically have an address:port combination of "*.*", which is useless for our purposes since it has no
	## definitive port number.  (I don't even understand what it represents.)
	##
	## Note that the port numbers in these output lines are separated from the IP address by a dot, not a colon;
	## that will need to be handled to normalize this output against what we see for other platforms.
	##
	## netstat -a -n -P tcp -f inet  | fgrep LISTEN | awk '{print $1}' | sort -u
	## Typical lines:
	##
	## *.111
	## 127.0.0.1.4999
	##
	## netstat -a -n -P tcp -f inet6 | fgrep LISTEN | awk '{print $1}' | sort -u
	## Typical lines:
	##
	## *.6000
	## ::1.631
	##
	## netstat -a -n -P udp -f inet  | fgrep Idle   | awk '{print $1}' | sort -u
	## Typical lines:
	##
	## *.123
	## 10.0.0.220.123
	## 127.0.0.1.123
	## 172.16.0.87.123
	## 192.168.1.64.123
	## 192.168.1.64.68
	##
	## netstat -a -n -P udp -f inet6 | fgrep Idle   | awk '{print $1}' | sort -u
	## Typical lines:
	##
	## *.177
	##

	# The commands we need to run on this platform are all so similar that we can condense them into a single parameterized loop.
	#
	# protocol, type, state, wildcard
	my @netstat_items = (
	    [ 'tcp', 'inet',  'LISTEN', '0.0.0.0' ],
	    [ 'tcp', 'inet6', 'LISTEN', '::' ],
	    [ 'udp', 'inet',  'Idle',   '0.0.0.0' ],
	    [ 'udp', 'inet6', 'Idle',   '::' ],
	);

	foreach my $item (@netstat_items) {
	    my $connection_protocol = $item->[0];
	    my $address_type        = $item->[1];
	    my $port_state          = $item->[2];
	    my $wildcard_address    = $item->[3];
	    if ($sensor_outcome) {
		## We open a pipe to the netstat command instead of using a bulk read from a qx() execution because we
		## believe this gives us much better control over recognizing possible errors during the reading.
		##
		## FIX MINOR:  Convert this to use our standard $self->qx() routine.
		##
		if ( not open NETSTAT, '-|', "netstat -a -n -P $connection_protocol -f $address_type" ) {
		    $sensor_results->{error_message} = "Cannot open a pipe to read from the netstat command ($!).";
		    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
		    $sensor_outcome = 0;
		}
		else {
		    local $_;
		    while (<NETSTAT>) {
			## We convert the special arbitrary-address notation used on this platform to the
			## notation we use elsewhere in this package.
			if ( /$port_state/ && /(\S+)/ ) {
			    ( my $address_and_port = $1 ) =~ s/^\*/$wildcard_address/;
			    push @open_addresses_and_ports, $address_and_port;
			}
		    }
		    ## Closing this filehandle waits for the child process to finish.
		    if ( not close NETSTAT ) {
			## Check for child-process error.
			if ($!) {
			    ## Error ($!) closing the pipe (i.e., some problem within Perl or the OS, not the piped program).
			    ## (It is possible that the piped program independently experienced some failure, and that would still
			    ## be reflected in $?, but in light of the problem at the Perl level, we're going to ignore that.)
			    ## Presumably the file descriptor itself did still get closed, but it left behind evidence of some issue.
			    $sensor_results->{error_message} = "Problem found while closing a pipe from the netstat command ($!).";
			    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
			}
			else {
			    my $status_message = full_status_message( $?, $! );
			    $sensor_results->{error_message} = sprintf( "netstat exited with $status_message." );
			    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
			}
			$sensor_outcome = 0;
		    }
		}
	    }
	}
    }
    elsif ( $^O eq 'aix' ) {
	## On AIX:
	##
	## On this platform, there is apparently no command-line option filtering available based on the protocol.
	## The "-p tcp" and "-p udp" options print overall statistics for these protocols across possibly many
	## connections, not filtered-down per-open-port lines.
	##
	## On this platform, we must probe for TCP and UDP sockets separately, since the filtering is slightly different
	## for those two protocol types, and for inet and inet6 separately, so we can match IPv4-only and IPv6-only
	## resource components properly and handle the wildcarded "*" address often seen in netstat output differently
	## for IPv4 and IPv6.  Note also that with regard to IPv4 vs. IPv6, we sometimes see exactly the same ports
	## listed in probes which are supposedly selective for IPv4 ("-f inet") or selective for IPv6 ("-f inet6"), for
	## instance listed as "tcp" instead of either "tcp4" or tcp6".  I can only guess that somehow these open ports
	## are to be treated as being open on both IPv4 and IPv6 interfaces at the same time.  Also note that on this
	## platform, unlike Solaris, you cannot specify both "-f inet" and "-f inet6" on the same command; only the
	## last "-f" option will have any effect.
	##
	## TCPv4 ports:
	## (This command yields info on both "tcp" and "tcp4" ports;
	## the tall and the fgrep for "tcp" are both redundant given that we grep for "LISTEN".)
	## netstat -a -n -f inet  | tail -n +3 | fgrep tcp | fgrep LISTEN | awk '{print $4}'
	##
	## TCPv6 ports:
	## (This command yields info on both "tcp" and "tcp6" ports;
	## the tall and the fgrep for "tcp" are both redundant given that we grep for "LISTEN".)
	## netstat -a -n -f inet6 | tail -n +3 | fgrep tcp | fgrep LISTEN | awk '{print $4}'
	##
	## UDPv4 ports:
	## FIX MINOR:  We should be checking for listening ports, and probably not include unbound ports.
	##             But there doesn't seem to be a way to do that on this platform.  So we just do as
	##             much as we can to identify the open IPv4 UDP ports, and leave it at that.  Given
	##             the grep for "udp", the tail is redundant, but we record it here as a clear notice
	##             that the heading lines do exist (although this netstat output also includes lines
	##             for tcp ports that we are also ignoring).
	## netstat -a -n -f inet  | tail -n +3 | fgrep udp | awk '{print $4}'
	##
	## UDPv6 ports:
	## FIX MINOR:  We should be checking for listening ports, and probably not include unbound ports.
	##             But there doesn't seem to be a way to do that on this platform.  So we just do as
	##             much as we can to identify the open IPv6 UDP ports, and leave it at that.  Given
	##             the grep for "udp", the tail is redundant, but we record it here as a clear notice
	##             that the heading lines do exist (although this netstat output also includes lines
	##             for tcp ports that we are also ignoring).
	## netstat -a -n -f inet6 | tail -n +3 | fgrep udp | awk '{print $4}'
	##
	## Note that the port numbers in these output lines are separated from the IP address by a dot, not a colon;
	## that will need to be handled to normalize this output against what we see for other platforms.
	##
	## netstat -a -n -f inet  | tail -n +3 | fgrep tcp | fgrep LISTEN | awk '{print $4}'
	## Typical lines:
	##
	## *.37
	## *.111
	## *.514
	## *.2049
	## *.32772
	##
	## netstat -a -n -f inet6 | tail -n +3 | fgrep tcp | fgrep LISTEN | awk '{print $4}'
	## Typical lines:
	##
	## *.21
	## *.512
	## *.657
	## *.2049
	## *.6000
	##
	## netstat -a -n -f inet  | tail -n +3 | fgrep udp | awk '{print $4}'
	## Typical lines:
	##
	## *.69
	## *.111
	## 172.28.111.51.137
	## *.137
	## 172.28.111.51.138
	## *.138
	## *.161
	##
	## netstat -a -n -f inet6 | tail -n +3 | fgrep udp | awk '{print $4}'
	## Typical lines:
	##
	## *.69
	## *.657
	##

	# As best we know them at this time, the commands we need to run on this platform are all so similar that we can
	# condense them into a single parameterized loop.
	#
	# type, filter, wildcard
	my @netstat_items = (
	    [ 'inet',  'LISTEN', '0.0.0.0' ],
	    [ 'inet6', 'LISTEN', '::' ],
	    [ 'inet',  'udp',    '0.0.0.0' ],
	    [ 'inet6', 'udp',    '::' ],
	);

	foreach my $item (@netstat_items) {
	    my $address_type     = $item->[0];
	    my $port_filter      = $item->[1];
	    my $wildcard_address = $item->[2];
	    if ($sensor_outcome) {
		## We open a pipe to the netstat command instead of using a bulk read from a qx() execution because we
		## believe this gives us much better control over recognizing possible errors during the reading.
		##
		## FIX MINOR:  Convert this to use our standard $self->qx() routine.
		##
		if ( not open NETSTAT, '-|', "netstat -a -n -f $address_type" ) {
		    $sensor_results->{error_message} = "Cannot open a pipe to read from the netstat command ($!).";
		    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
		    $sensor_outcome = 0;
		}
		else {
		    local $_;
		    while (<NETSTAT>) {
			## We convert the special arbitrary-address notation used on this platform to the
			## notation we use elsewhere in this package.
			if ( /$port_filter/ && /^\S+\s+\S+\s+\S+\s+(\S+)/ ) {
			    ( my $address_and_port = $1 ) =~ s/^\*/$wildcard_address/;
			    push @open_addresses_and_ports, $address_and_port;
			}
		    }
		    ## Closing this filehandle waits for the child process to finish.
		    if ( not close NETSTAT ) {
			## Check for child-process error.
			if ($!) {
			    ## Error ($!) closing the pipe (i.e., some problem within Perl or the OS, not the piped program).
			    ## (It is possible that the piped program independently experienced some failure, and that would still
			    ## be reflected in $?, but in light of the problem at the Perl level, we're going to ignore that.)
			    ## Presumably the file descriptor itself did still get closed, but it left behind evidence of some issue.
			    $sensor_results->{error_message} = "Problem found while closing a pipe from the netstat command ($!).";
			    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
			}
			else {
			    my $status_message = full_status_message( $?, $! );
			    $sensor_results->{error_message} = sprintf( "netstat exited with $status_message." );
			    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
			}
			$sensor_outcome = 0;
		    }
		}
	    }
	}
    }
    elsif ( $^O eq 'hpux' ) {
	## On HP-UX:
	##
	## The commands to use here are unknown, but start by trying these commands along with subsequent filtering:
	##
	## netstat -a -n -f inet
	## netstat -a -n -f inet6  (might not work on HP-UX 10, so we need to be prepared for that)
	##
	## We cannot fully complete the support for this platform until we see sample output of those commands.
	## However, we expect it be very similar to the AIX filtering.
	## See:  http://lists.xymon.com/archive/2006-April/006372.html
	## Typical output (there might be other leading lines as well, that could probably all be
	## filtered out by looking for LISTEN, at least for a TCP socket):
	##     Active Internet connections (including servers)
	##     Proto Recv-Q Send-Q  Local Address          Foreign Address        (state)
	##     tcp        0      0  127.0.0.1.6010         *.*                    LISTEN
	##     tcp        0      0  127.0.0.1.6011         *.*                    LISTEN
	##     tcp        0      0  *.1103                 *.*                    LISTEN
	##     tcp        0      0  *.2131                 *.*                    LISTEN
	##     tcp        0      0  *.32768                *.*                    LISTEN
	##     tcp        0      0  *.32769                *.*                    LISTEN
	##     tcp        0      0  *.993                  *.*                    LISTEN
	##     tcp        0      0  *.997                  *.*                    LISTEN
	##
	## FIX MAJOR:  implement this case
	$sensor_results->{error_message} = "The open_local_port sensor is not yet implemented on the HP-UX platform.";
	$self->{logger}->error("ERROR:  $sensor_results->{error_message}");
	$sensor_outcome = 0;
    }
    elsif ( $^O eq 'MSWin32' ) {
	## On Windows:
	##
	## On this platform, for TCP connections, I need the equivalent of:
	##     netstat -a -n | fgrep LISTENING | awk '{print $2}'
	## although of course there is no fgrep or awk on this platform.
	## Here is an equivalent command in Windows-speak:
	##     for /f "tokens=2" %a in ('netstat -a -n ^| findstr LISTEN') do @echo %a
	## If using in a batch file, you have to double the % signs, and can assign the
	## values you want (%%a j etc.) to variables.
	##
	## It would also be possible to use a -p argument and run several commands:
	##   netstat -a -n -p TCP
	##   netstat -a -n -p TCPv6
	##   netstat -a -n -p UDP
	##   netstat -a -n -p UDPv6
	## but is apparently not necessary, since the -a output already contains all of that data.
	##
	## For our usage, we just run the netstat command and do all the filtering in Perl, since that's so
	## simple in this language.  We must ignore four lines of heading, but we don't bother trying to drop
	## away header/footer lines explicitly because our checking for either TCP or UDP as the first field
	## in the line effectively subsumes that filtering.
	##
	## Note that running netstat on this platform can be quite slow.  We have seen it take a couple of
	## seconds to process a socket discovered in CLOSE_WAIT state, and if there are a bunch of those, the
	## entire command can take quite awhile to run.  Bear that in mind if we decide to impose any sort of
	## timeouts on sensor processing.
	##
	## Typical output:
	##
	## Active Connections
	##
	##   Proto  Local Address          Foreign Address        State
	##   TCP    0.0.0.0:135            0.0.0.0:0              LISTENING
	##   TCP    0.0.0.0:3389           0.0.0.0:0              LISTENING
	##   TCP    0.0.0.0:47001          0.0.0.0:0              LISTENING
	##   TCP    0.0.0.0:49152          0.0.0.0:0              LISTENING
	##   TCP    0.0.0.0:49153          0.0.0.0:0              LISTENING
	##   TCP    0.0.0.0:49205          0.0.0.0:0              LISTENING
	##   TCP    172.28.113.36:139      0.0.0.0:0              LISTENING
	##   TCP    172.28.113.36:3389     172.28.129.81:54360    ESTABLISHED
	##   TCP    [::]:135               [::]:0                 LISTENING
	##   TCP    [::]:445               [::]:0                 LISTENING
	##   TCP    [::]:3389              [::]:0                 LISTENING
	##   TCP    [::]:47001             [::]:0                 LISTENING
	##   TCP    [::]:49152             [::]:0                 LISTENING
	##   TCP    [::]:49205             [::]:0                 LISTENING
	##   UDP    0.0.0.0:123            *:*
	##   UDP    0.0.0.0:5355           *:*
	##   UDP    127.0.0.1:53426        *:*
	##   UDP    127.0.0.1:63446        *:*
	##   UDP    172.28.113.36:137      *:*
	##   UDP    172.28.113.36:138      *:*
	##   UDP    [::]:123               *:*
	##   UDP    [::]:500               *:*
	##   UDP    [::]:5355              *:*
	##   UDP    [fe80::fc46:46d:d916:72b8%10]:546  *:*
	##
	## Also notice that on this platform, IPv6 addresses are enclosed in square brackets, and might
	## contain a %interface suffix as well (for a link-level address, where it is important to understand
	## on what network interface the address can be used).
	##
	## Note that all the UDP ports are not shown as listening.  We take having the Foreign Address be
	## "*.*" as the best evidence of a UDP or UDPv6 port open for listening, as compared to it being tied
	## to some specific addr:port combination.  Perhaps in the future we may generalize this to 0.0.0.0 or
	## [::] as the IP address, if we can find some example Windows platform where that convention might be
	## used, as long as the Foreign Address port itself is still wildcarded.
	##
	my @netstat_lines = $self->qx("netstat -a -n");
	my $wait_status   = $?;
	if ( $wait_status != 0 ) {
	    my $status_message = full_status_message( $wait_status, $! );
	    $sensor_results->{error_message} = "Cannot run netstat; failed execution with $status_message.";
	    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
	    $sensor_outcome = 0;
	}
	else {
	    ## In collecting the list of open addresses and ports, we don't bother trying to strip enclosing brackets
	    ## for IPv6 addresses, because the later processing will accommodate them.  This makes the display of open
	    ## local ports match the existing platform conventions for such addr:port combinations.
	    my @fields = ();
	    foreach my $line (@netstat_lines) {
		if ( @fields = split ' ', $line ) {
		    if ( $fields[0] eq 'TCP' ) {
			push @open_addresses_and_ports, $fields[1] if $fields[3] eq 'LISTENING';
		    }
		    elsif ( $fields[0] eq 'UDP' ) {
			push @open_addresses_and_ports, $fields[1] if $fields[2] eq '*:*';
		    }
		}
	    }
	}
    }

    if ($sensor_outcome) {
	## We de-duplicate the @open_addresses_and_ports array before using it in the following code.  Sorting the final
	## list is for human consumption.  Logically, we ought to sort by the bit-representation of the IP address, so
	## as to avoid number-as-string sorting problems.  We leave that touch for some future implementation.
	my %unique = ();
	@unique{@open_addresses_and_ports} = (undef) x @open_addresses_and_ports;
	## Here's a complex Schwartzian Transform in action.  We sort first by IP address type and then IP address at
	## the bit level, which fields correspond to the sensor resource definition, and then by port number, which
	## field corresponds to the sensor pattern definition.
	@open_addresses_and_ports =
	  map { $_->[0] }
	  sort { $a->[1] <=> $b->[1] || $a->[2] cmp $b->[2] || $a->[3] <=> $b->[3] }
	  map { [ $_->[0], addr_type_and_bits( $_->[1] ), $_->[2] ] }
	  map { [ $_, split( /[:.](?=\d+$)/, $_ ) ] } keys %unique;

	## We potentially print the list of open local ports that we found, since the strings the
	## resource specification and the pattern matching are supposed to go up against might not
	## be obvious.  Because the output is often voluminous, we only produce this output once per
	## discovery run, instead of every time this sensor is run during a pass of discovery.  The
	## idea is that each pass of discovery in a long-running process would have the opportunity
	## to reset the "have done this already" flags right before the next pass of discovery is
	## run, if such output is still desired.
	if ( $self->{show_resources} and not $self->{have_shown_resources}{open_local_port} ) {
	    print "--------------------------------------------------------\n";
	    print "Open local address:port pairs, as found during discovery\n";
	    print "--------------------------------------------------------\n";
	    if (@open_addresses_and_ports) {
		foreach my $address_and_port ( @open_addresses_and_ports ) {
		    print "$address_and_port\n";
		}
	    }
	    else {
		print "No open local ports were found.\n";
	    }
	    print "\n";
	    $self->{have_shown_resources}{open_local_port} = 1;
	}

	foreach my $address_and_port (@open_addresses_and_ports) {
	    ( my $ip_address )  = $address_and_port =~ m{^(\[?(.+)\]?)[:.]\d+$};
	    ( my $port_number ) = $address_and_port =~ m{[:.](\d+)$};
	    if ( $self->address_matches_cidr_blocks( $ip_address, $sensor->{resource} ) ) {
		local $_ = $port_number + 0;    # Force into the same form as is used by the match routine.
		## A successful pattern match will yield exactly one match value, the port number.  Upon a match,
		## $ip_address should become $MATCHED1$, and the matched port number should become $MATCHED2$.
		if ( my @matched_values = map { $_ // '' } &{ $sensor->{match} } ) {
		    ## print "match is $matched_values[0]\n";
		    ## print "sensor matched\n";
		    ## We could have used $port_number instead for the qualified_resource, but then we wouldn't know the
		    ## associated address when we try to understand the full matching process.
		    my %instance = ();
		    $instance{qualified_resource} = $address_and_port;
		    $instance{raw_match_strings}  = [ $ip_address, $matched_values[0] ];
		    $instance{instance_suffix}    = $sensor->{instance_suffix} // '' if $kind eq 'service';
		    $instance{instance_cmd_args}  = $sensor->{instance_cmd_args} if defined $sensor->{instance_cmd_args};
		    $instance{instance_ext_args}  = $sensor->{instance_ext_args} if defined $sensor->{instance_ext_args};
		    push @instances, \%instance;
		}
		else {
		    ## print "sensor did not match\n";
		}
	    }
	}
    }

    return $sensor_outcome, $sensor_results, \@instances;
}

sub open_named_socket_sensor {
    my $self           = shift;
    my $kind           = shift;
    my $tag            = shift;
    my $sensor         = shift;
    my $profile        = shift;
    my @instances      = ();
    my $sensor_outcome = 1;
    my $sensor_results = {
	error_message => undef,
    };

    # Currently, we draw a list of candidate items to match the sensor pattern regex against from netstat
    # output.  There isn't an obvious need to qualify the items we get back from netstat in a manner similar
    # to how we use CIDR blocks to qualify items (IP addresses) for matching anonymous open ports.  Possibly
    # we could qualify the filepaths for named sockets as to whether they show up on a locally-mounted or
    # remote-mounted filesystem, but that has not yet been implemented.  So for the time being, this sensor
    # type needs no sensor resource defined.

    my @open_named_socket_paths = ();

    if ( $^O eq 'linux' ) {
	## On Linux:
	## Note that the "netstat" program is declared obsolete, with a replacement program "ss", at least
	## on CentOS7 and Ubuntu 16.04.  However, note that its output format has changed over time, which
	## would make it considerably harder to parse.  So we are sticking with netstat for now.
	##
	## On the Linux platform, we implement the equivalent of the following shell pipeline:
	##
	## netstat -l -x | tail -n +3 | awk '{print $NF}'
	##
	## Typical paths are:
	##
	##     @/tmp/.ICE-unix/1896
	##     private/tlsmgr
	##     private/rewrite
	##     /run/systemd/private
	##     @/tmp/.X11-unix/X0
	##     /tmp/.esd-1059/socket
	##     @ISCSID_UIP_ABSTRACT_NAMESPACE
	##     /run/lvm/lvmpolld.socket
	##     public/pickup
	##     /run/lvm/lvmetad.socket
	##     public/cleanup
	##     public/qmgr
	##
	## We don't yet understand all of those notations.  There is no indication that we can find about
	## what port number is associated with each UNIX-domain socket, if there even is one while the
	## named socket is still in a listening state.
	##
	## An open MySQL port from an old GroundWork release was "/usr/local/groundwork/mysql/tmp/mysql.sock",
	## showing the potential utility of this sensor type.
	##
	## FIX MINOR:  Convert this to use our standard $self->qx() routine.
	##
	if ( not open NETSTAT, '-|', 'netstat -lx' ) {
	    $sensor_results->{error_message} = "Cannot open a pipe to read from the netstat command ($!).";
	    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
	    $sensor_outcome = 0;
	}
	else {
	    local $_;
	    while (<NETSTAT>) {
		## We skip two lines of headers before reaching the data we want to see,
		## and choose the last field of each line thereafter.
		if ( $. > 2 && /\s+(\S+)$/ ) {
		    push @open_named_socket_paths, $1;
		}
	    }
	    ## Closing this filehandle waits for the child process to finish.
	    if ( not close NETSTAT ) {
		## Check for child-process error.
		if ($!) {
		    ## Error ($!) closing the pipe (i.e., some problem within Perl or the OS, not the piped program).
		    ## (It is possible that the piped program independently experienced some failure, and that would still
		    ## be reflected in $?, but in light of the problem at the Perl level, we're going to ignore that.)
		    ## Presumably the file descriptor itself did still get closed, but it left behind evidence of some issue.
		    $sensor_results->{error_message} = "Problem found while closing a pipe from the netstat command ($!).";
		    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
		}
		else {
		    my $status_message = full_status_message( $?, $! );
		    $sensor_results->{error_message} = sprintf( "netstat exited with $status_message." );
		    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
		}
		$sensor_outcome = 0;
	    }
	}
    }
    elsif ( $^O eq 'solaris' ) {
	## On Solaris:
	##
	## For now, we don't attempt to distinguish between the socket paths returned as "Local Addr"
	## and "Remote Addr", both because there is no clear documentation on what the difference is
	## between these two fields, and because it is difficult to parse them out individually given
	## the formatting where one field or the other is usually missing.
	##
	## netstat -a -f unix | tail +4 | awk '{print $5}' | egrep '^/' | sort -u
	##
	## Typical lines:
	##
	##     /tmp/.X11-unix/X0
	##     /var/run/atokserver/atokusermanagedaemon
	##     /var/run/jd_sockV6
	##     /var/tmp/orbit-glenn/linc-31e-0-5a512b286b538
	##     /var/tmp/orbit-glenn/linc-367-0-5a5133c2f175
	##     /var/tmp/orbit-glenn/linc-36f-0-5a512533baa3c
	##
	## Without the unique-value sorting at the end, there will be many duplicate lines.  We filter
	## those out later on below, in platform-independent processing.
	##
	## FIX MINOR:  Convert this to use our standard $self->qx() routine.
	##
	if ( not open NETSTAT, '-|', 'netstat -a -f unix' ) {
	    $sensor_results->{error_message} = "Cannot open a pipe to read from the netstat command ($!).";
	    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
	    $sensor_outcome = 0;
	}
	else {
	    local $_;
	    while (<NETSTAT>) {
		## We skip three lines of headers before reaching the data we want to see, and record
		## the fifth field of each line thereafter if it is an actual filesystem path.
		if ( $. > 3 && /\S+\s+\S+\s+\S+\s+\S+\s+(\S+)$/ ) {
		    push @open_named_socket_paths, $1 if $1 =~ m{^/};
		}
	    }
	    ## Closing this filehandle waits for the child process to finish.
	    if ( not close NETSTAT ) {
		## Check for child-process error.
		if ($!) {
		    ## Error ($!) closing the pipe (i.e., some problem within Perl or the OS, not the piped program).
		    ## (It is possible that the piped program independently experienced some failure, and that would still
		    ## be reflected in $?, but in light of the problem at the Perl level, we're going to ignore that.)
		    ## Presumably the file descriptor itself did still get closed, but it left behind evidence of some issue.
		    $sensor_results->{error_message} = "Problem found while closing a pipe from the netstat command ($!).";
		}
		else {
		    my $status_message = full_status_message( $?, $! );
		    $sensor_results->{error_message} = sprintf( "netstat exited with $status_message." );
		}
		$self->{logger}->error("ERROR:  $sensor_results->{error_message}");
		$sensor_outcome = 0;
	    }
	}
    }
    elsif ( $^O eq 'aix' ) {
	## On AIX:
	## netstat -a -f unix | tail -n +3 | egrep 'dgram|stream'| awk '{print $9}' | egrep '^/'
	## dgram (datagram) is for unidirectional communication, stream is for bidirectional communication.
	##
	## Typical lines:
	##
	##     /dev/.SRC-unix/SRCqDQhyh
	##     /dev/.SRC-unix/SRCtHQhyi
	##     /dev/SRC
	##     /dev/log
	##     /tmp/.X11-unix/X0
	##     /var/ct/IW/soc/mc/RMIBM.CSMAgentRM.0
	##     /var/ct/IW/soc/mc/RMIBM.ServiceRM.0
	##
	## There will be some blank lines (with no field 9), that just need to be ignored.  They could
	## also be eliminated without the extra dgram/stream filtering simply by noting that there is no
	## field 9 for those lines as well.  There are also likely to be a few duplicate lines; we filter.
	## those out later on below, in platform-independent processing.
	##
	## FIX MINOR:  Convert this to use our standard $self->qx() routine.
	##
	if ( not open NETSTAT, '-|', 'netstat -a -f unix' ) {
	    $sensor_results->{error_message} = "Cannot open a pipe to read from the netstat command ($!).";
	    $self->{logger}->error("ERROR:  $sensor_results->{error_message}");
	    $sensor_outcome = 0;
	}
	else {
	    local $_;
	    my $path;
	    while (<NETSTAT>) {
		## We skip two lines of headers before reaching the data we want to see, and record
		## the ninth field of each line thereafter if it is an actual filesystem path.
		if ( $. > 2 && defined( $path = (split)[8] ) ) {
		    push @open_named_socket_paths, $path if $path =~ m{^/};
		}
	    }
	    ## Closing this filehandle waits for the child process to finish.
	    if ( not close NETSTAT ) {
		## Check for child-process error.
		if ($!) {
		    ## Error ($!) closing the pipe (i.e., some problem within Perl or the OS, not the piped program).
		    ## (It is possible that the piped program independently experienced some failure, and that would still
		    ## be reflected in $?, but in light of the problem at the Perl level, we're going to ignore that.)
		    ## Presumably the file descriptor itself did still get closed, but it left behind evidence of some issue.
		    $sensor_results->{error_message} = "Problem found while closing a pipe from the netstat command ($!).";
		}
		else {
		    my $status_message = full_status_message( $?, $! );
		    $sensor_results->{error_message} = sprintf( "netstat exited with $status_message." );
		}
		$self->{logger}->error("ERROR:  $sensor_results->{error_message}");
		$sensor_outcome = 0;
	    }
	}
    }
    elsif ( $^O eq 'hpux' ) {
	## On HP-UX:
	## Unknown, but try this command along with subsequent filtering:
	##
	## netstat -a -f unix
	##
	## We cannot fully complete the support for this platform until we see sample output of that command.
	## However, we expect it be very similar to the AIX filtering.
	##
	## FIX MAJOR:  implement this case
	$sensor_results->{error_message} = "The open_named_socket sensor is not yet implemented on the HP-UX platform.";
	$self->{logger}->error("ERROR:  $sensor_results->{error_message}");
	$sensor_outcome = 0;
    }
    elsif ( $^O eq 'MSWin32' ) {
	## On Windows:  Historically, AF_UNIX sockets (named sockets) have not been available under Windows.
	## This is changing quite recently (early 2018), starting with Windows 10; see this blog post:
	## https://blogs.msdn.microsoft.com/commandline/2017/12/19/af_unix-comes-to-windows/
	## On this platform, for the time being until this feature becomes publicly available and we
	## can test it, all we can do is fail the sensor and perhaps generate a configuration error.
	##
	$sensor_results->{error_message} = "The open_named_socket sensor is not yet implemented on the Windows platform.";
	$self->{logger}->error("ERROR:  $sensor_results->{error_message}");
	$sensor_outcome = 0;
    }

    if ($sensor_outcome) {
	## We de-duplicate the @open_named_socket_paths array before using it in the following code.
	## Sorting the final list is for human consumption.
	my %unique = ();
	@unique{@open_named_socket_paths} = (undef) x @open_named_socket_paths;
	@open_named_socket_paths = sort keys %unique;

	## We potentially print the list of open named sockets that we found, since the strings the
	## pattern matching is supposed to go up against might not be obvious.  Because the output
	## may be somewhat voluminous, we only produce this output once per discovery run, instead
	## of every time this sensor is run during a pass of discovery.  The idea is that each pass
	## of discovery in a long-running process would have the opportunity to reset the "have done
	## this already" flags right before the next pass of discovery is run, if such output is
	## still desired.
	if ( $self->{show_resources} and not $self->{have_shown_resources}{open_named_socket} ) {
	    print "---------------------------------------------\n";
	    print "Open named sockets, as found during discovery\n";
	    print "---------------------------------------------\n";
	    if (@open_named_socket_paths) {
		foreach my $socket_path ( sort @open_named_socket_paths ) {
		    print "$socket_path\n";
		}
	    }
	    else {
		print "No open named sockets were found.\n";
	    }
	    print "\n";
	    $self->{have_shown_resources}{open_named_socket} = 1;
	}

	foreach my $socket_path (@open_named_socket_paths) {
	    local $_ = $socket_path;
	    if ( my @matched_values = map { $_ // '' } &{ $sensor->{match} } ) {
		## print "match is $matched_values[0]\n";
		## print "sensor matched\n";
		my %instance = ();
		$instance{qualified_resource} = $socket_path;
		$instance{raw_match_strings}  = \@matched_values;
		$instance{instance_suffix}    = $sensor->{instance_suffix} // '' if $kind eq 'service';
		$instance{instance_cmd_args}  = $sensor->{instance_cmd_args} if defined $sensor->{instance_cmd_args};
		$instance{instance_ext_args}  = $sensor->{instance_ext_args} if defined $sensor->{instance_ext_args};
		push @instances, \%instance;
	    }
	    else {
		## print "sensor did not match\n";
	    }
	}
    }

    return $sensor_outcome, $sensor_results, \@instances;
}

# A disabled sensor always succeeds (so it doesn't interfere with any other processing), but it produces no useful results.
sub disabled_sensor {
    my $self           = shift;
    my $kind           = shift;
    my $tag            = shift;
    my $sensor         = shift;
    my $profile        = shift;
    my @instances      = ();
    my $sensor_outcome = 1;
    my $sensor_results = {
	error_message => undef,
    };

    # Each of our sensor routines returns only an overall outcome, possibly an error message, and
    # possibly a set of discovered instances.  So we need supply no detail for a disabled sensor.
    # All the details will be filled in by the calling execute_sensor() routine.

    return $sensor_outcome, $sensor_results, \@instances;
}

# Internal utility routine.
sub sanitize_string {
    my $raw_string    = shift;
    my $transliterate = shift;
    my $sanitize      = shift;

    # Note that the following pattern matches will not raise any Perl warnings if the incoming
    # raw string is undefined.  But the downstream code will probably end up having hiccups if
    # it tries to use undefined values.  So it is up to earlier code, usually in every sensor
    # as it captures all the match strings, to clean up any match results to ensure that all
    # undefined values are turned into empty strings.

    local $_ = $raw_string;
    $_ = &$transliterate if $transliterate;
    $_ = &$sanitize      if $sanitize;

    return $_;
}

# Internal utility routine.
sub replace_macros {
    my $string           = shift;
    my $matched_macros   = shift;
    my $sanitized_macros = shift;

    # Externally, we call the substitutable macros $MATCHED1$, $MATCHED2$, and so forth,
    # or $SANITIZED1$, $SANITIZED2$, and so forth.  So we must accommodate the fact that
    # our arrays of macro values are indexed starting with zero.
    #
    # FIX LATER:  The present code here allows referencing $MATCHED0$ and $SANITIZED0$ macros
    # without complaint, and we just substitute in an empty string for such references.  We ought
    # to provide some means of instructions validation earlier on to exclude such references.
    #
    # FIX LATER:  Also note that it is possible for macro references to reach beyond the
    # number of captured strings.  Ideally, we would prevent that by marking such references
    # as invalid when the instructions are being validated, by somehow first analyzing the
    # sensor pattern and understanding exactly how many capturing parentheses are present.
    # That is no doubt a difficult job, though.  So for the time being, we simply allow such
    # references and substitute in empty strings for them.
    #
    $string =~ s{   \$MATCHED(\d+)\$ }{ $1 ? (   $matched_macros->[$1 - 1] // '' ) : '' }xeg;
    $string =~ s{ \$SANITIZED(\d+)\$ }{ $1 ? ( $sanitized_macros->[$1 - 1] // '' ) : '' }xeg;

    return $string;
}

# internal routine
sub execute_sensor {
    my $self             = shift;
    my $kind             = shift;
    my $tag              = shift;
    my $sensor           = shift;
    my $sensor_outcome   = 0;
    my $sensor_results   = undef;
    my $sensor_instances = undef;

    my %sensor_type_routine = (
	os_type                => \&os_type_sensor,
	os_version             => \&os_version_sensor,
	os_bitwidth            => \&os_bitwidth_sensor,
	machine_architecture   => \&machine_architecture_sensor,
	file_name              => \&file_name_sensor,
	symlink_name           => \&symlink_name_sensor,
	directory_name         => \&directory_name_sensor,
	mounted_filesystem     => \&mounted_filesystem_sensor,
	file_content           => \&file_content_sensor,
	running_system_service => \&running_system_service_sensor,
	full_process_command   => \&full_process_command_sensor,
	open_local_port        => \&open_local_port_sensor,
	open_named_socket      => \&open_named_socket_sensor,
    );

    # There are two forms of sensor outcome to think about.  One is, did the sensor operate without
    # internal failure (e.g., without generating any error message)?  The other is, did the sensor
    # match anything, or too much?  We need to ensure that we do not confuse these two outcomes.
    # $sensor_outcome is supposed to reflect only whether the sensor operated with or without
    # internal failure.  Issues with matching too little or too much are really the province of
    # analyzed discovery results, not the raw sensor results we are dealing with here.

    # First, execute the designated sensor and get back the raw sensor results.
    my $enabled = !exists $sensor->{enabled} || $sensor->{enabled};
    if ($enabled) {
	$self->{logger}->debug("DEBUG:  Calling $sensor->{type} sensor for $kind tag $tag.");
	( $sensor_outcome, $sensor_results, $sensor_instances ) = &{ $sensor_type_routine{ $sensor->{type} } }( $self, $kind, $tag, $sensor );
    }
    else {
	$self->{logger}->debug("DEBUG:  $sensor->{type} sensor for $kind tag $tag is disabled");
	( $sensor_outcome, $sensor_results, $sensor_instances ) = disabled_sensor( $self, $kind, $tag, $sensor );
    }

    # Sort all the discovered sensor instances.  This canonicalization of the ordering is what will control the
    # selection of which sensor instance is chosen to be representative when the sensor cardinality is "first",
    # and the particular set of macro definitions that will be substituted into sensor-level fields below.
    # Somewhat arbitrarily, we order by:
    # instance_suffix, qualified_resource, comma-joined raw_match_strings
    #
    # Here's a Schwartzian Transform in action.
    @$sensor_instances = map { $_->[0] } sort { $a->[1] cmp $b->[1] || $a->[2] cmp $b->[2] || $a->[3] cmp $b->[3] } map {
	[
	    $_,
	    $_->{instance_suffix}    // '',
	    $_->{qualified_resource} // '',
	    $_->{raw_match_strings} ? join( ',', @{ $_->{raw_match_strings} } ) : ''
	]
    } @$sensor_instances;

    $sensor_results->{sensor_name} = $tag;
    $sensor_results->{type}        = $sensor->{type};
    $sensor_results->{cardinality} = $sensor->{cardinality};
    $sensor_results->{instances}   = $sensor_instances;
    $sensor_results->{enabled}     = $enabled;
    $sensor_results->{matched}     = @$sensor_instances;

    ## FIX MAJOR:  also factor out cardinality and sensor_name
    ##
    if ( $kind eq 'host' ) {
	$sensor_results->{host_profile_name} = $sensor->{host_profile};
    }
    if ( $kind eq 'service' ) {
	$sensor_results->{service_profile_name} = $sensor->{service_profile}     if defined $sensor->{service_profile};
	$sensor_results->{service_name}         = $sensor->{service}             if defined $sensor->{service};
	$sensor_results->{check_command}        = $sensor->{check_command}       if defined $sensor->{check_command};
	$sensor_results->{command_arguments}    = $sensor->{command_arguments}   if defined $sensor->{command_arguments};
	$sensor_results->{externals_arguments}  = $sensor->{externals_arguments} if defined $sensor->{externals_arguments};
    }

    # Next, process the raw sensor results in ways that are common across multiple sensors,
    # so we don't have to maintain the same code in every individual sensor routine.

    if ($sensor_outcome) {
	if ( @{ $sensor_results->{instances} } ) {
	    ## If we have cardinality "first", we report all found sensor instances in the discovery results
	    ## anyway, not just the first.  That makes it easier for humans to see what happened, and leaves
	    ## it up to the downstream code to pick the first instance and ignore all the rest.

	    # Before going further, we compare the declared cardinality against the number of found instances.
	    # If they don't match appropriately, we declare a sensor error.  This constitutes a very small bit
	    # of analysis beyond just providing the raw discovery results, but it is so simple to do at this
	    # point and it seems so useful at this time that we may as well go ahead and do so.  If this test
	    # fails, we don't allow that failure to interfere with any other processing that we would normally
	    # do in the context of the discovery process, but it can be helpful to the user who is trying to
	    # tune the discovery instructions even before the full analysis is run.
	    #
	    # Wnly do this if we don't already have an error_message defined, since we want to report out the
	    # low-level detail first.
	    #
	    if ( not defined $sensor_results->{error_message} ) {
		if ( $sensor->{cardinality} eq 'single' && @{ $sensor_results->{instances} } > 1 ) {
		    $sensor_results->{error_message} =
		      "Sensor $sensor->{type} tag $tag has cardinality '$sensor->{cardinality}', but multiple instances were discovered.";
		}
	    }

	    my $did_sensor_level_subs = 0;
	    foreach my $instance ( @{ $sensor_results->{instances} } ) {
		if ( defined( $instance->{raw_match_strings} ) ) {
		    foreach my $match_string ( @{ $instance->{raw_match_strings} } ) {
			push @{ $instance->{sanitized_match_strings} },
			  sanitize_string( $match_string, $sensor->{transliterate}, $sensor->{sanitize} );
		    }
		}
		if ( defined $instance->{instance_suffix} ) {
		    $instance->{instance_suffix} = replace_macros(
			$instance->{instance_suffix},
			$instance->{raw_match_strings} // [],
			$instance->{sanitized_match_strings} // []
		    );
		}
		if ( defined $instance->{instance_cmd_args} ) {
		    $instance->{instance_cmd_args} = replace_macros(
			$instance->{instance_cmd_args},
			$instance->{raw_match_strings} // [],
			$instance->{sanitized_match_strings} // []
		    );
		}
		if ( defined $instance->{instance_ext_args} ) {
		    $instance->{instance_ext_args} = replace_macros(
			$instance->{instance_ext_args},
			$instance->{raw_match_strings} // [],
			$instance->{sanitized_match_strings} // []
		    );
		}
		if ( not $did_sensor_level_subs ) {
		    ## We make macro substitutions at the sensor level (host or service, not instances) just
		    ## once, for the first and possibly only discovered sensor instance.  We will have validated
		    ## the discovery instructions earlier on, to verify that with cardinality "multiple", these
		    ## configured sensor elements do not contain any macro references, so there will be no
		    ## ambiguity now since we will not have run a pass of discovery with such broken instructions.
		    ## Also, we checked just above to verify that if we have cardinality "single", only one
		    ## sensor instance was found; again, we won't get here if that is problematic.  Finally, our
		    ## $did_sensor_level_subs flag covers the case when we have cardinality "first" and we have
		    ## found multiple sensor instances; our flag will block more than one set of substitutions.
		    ## Of course, that last case depends on our having placed all the instances into canonical
		    ## order before we get here, and maintaining that order throughout all subsequent processing
		    ## so ultimately the server sees the same instance as being first.  The code to implement
		    ## that ordering of instances is present earlier in this subroutine, so we are guaranteed
		    ## that it did get exacuted before we got here.
		    ##
		    my $profile_name_label = "${kind}_profile_name";
		    if ( defined $sensor_results->{$profile_name_label} ) {
			$sensor_results->{$profile_name_label} = replace_macros(
			    $sensor_results->{$profile_name_label},
			    $instance->{raw_match_strings} // [],
			    $instance->{sanitized_match_strings} // []
			);
		    }
		    if ( defined $sensor_results->{service} ) {
			$sensor_results->{service} = replace_macros(
			    $sensor_results->{service},
			    $instance->{raw_match_strings} // [],
			    $instance->{sanitized_match_strings} // []
			);
		    }
		    if ( defined $sensor_results->{check_commmand} ) {
			$sensor_results->{check_commmand} = replace_macros(
			    $sensor_results->{check_commmand},
			    $instance->{raw_match_strings} // [],
			    $instance->{sanitized_match_strings} // []
			);
		    }
		    if ( defined $sensor_results->{command_arguments} ) {
			$sensor_results->{command_arguments} = replace_macros(
			    $sensor_results->{command_arguments},
			    $instance->{raw_match_strings} // [],
			    $instance->{sanitized_match_strings} // []
			);
		    }
		    if ( defined $sensor_results->{externals_arguments} ) {
			$sensor_results->{externals_arguments} = replace_macros(
			    $sensor_results->{externals_arguments},
			    $instance->{raw_match_strings} // [],
			    $instance->{sanitized_match_strings} // []
			);
		    }
		    $did_sensor_level_subs = 1;
		}
	    }
	}
    }

    return $sensor_outcome, $sensor_results;
}

# internal routine
sub execute_instructions {
    my $self                = shift;
    my $instructions        = shift;
    my $full_sensor_outcome = 1;
    my @full_sensor_results = ();
    my $sensor_outcome;
    my $sensor_results;

    my $host_sensors    = $instructions->{host};
    my $service_sensors = $instructions->{service};

    foreach my $tag ( keys %$host_sensors ) {
	( $sensor_outcome, $sensor_results ) = $self->execute_sensor( 'host', $tag, $host_sensors->{$tag} );
	$full_sensor_outcome &= $sensor_outcome;
	push @full_sensor_results, $sensor_results;
    }
    foreach my $tag ( keys %$service_sensors ) {
	( $sensor_outcome, $sensor_results ) = $self->execute_sensor( 'service', $tag, $service_sensors->{$tag} );
	$full_sensor_outcome &= $sensor_outcome;
	push @full_sensor_results, $sensor_results;
    }

    return $full_sensor_outcome, \@full_sensor_results;
}

# When we store the discovery results in a file, we need to have them laid out in some canonical ordering so a repeated
# discovery which logically yields exactly the same results can be trivially recognized as such by simply comparing
# two files that list those results as long strings.  To make that happen, here we decide on a canonical ordering of
# each sensor in the results of all executed sensors.  Within each sensor result, the subsidiary array of instances
# for each discovered sensor will have been sorted when the sensor got executed, so we have a definitive meaning of
# cardinality "first" when macros are substituted into sensor-level (as opposed to instance-level) fields.  All of this
# means that the final ordering of the sensors will not necessarily reflect the ordering in which they were presented in
# the instructions file, and the final ordering of the discovered instances will not necessarily reflect the same order
# in which those instances were found during discovery.
#
# Forcing an ordering here also helps when printing discovery results for human consumption.  Having a stable order makes
# finding what you want within successive passes of trial discovery makes it easier to develop sensor definitions.
#
# All the re-ordering in this routine happens through the hashref which is passed as the full sensor results, so we
# don't need to pass back a modified copy of the discovery results.
#
# The value returned by this routine is nominal.  We don't actually expect that there can ever be a problem when the
# action is simply re-ordering existing items.
#
sub force_canonical_ordering {
    my $self                = shift;
    my $full_sensor_results = shift;
    my $outcome             = 1;

    # We put all the executed sensors into canonical order.  Somewhat arbitrarily, we order by:
    # * sensor kind (all host sensors before all service sensors)
    # * sensor target type (service profiles before services)
    # * sensor_name in (case-sensitive) alphabetic order
    #
    # FIX LATER:  Consider using a case-sensitive sort at the level of the sensor_name values, via the Unicode::Collate
    # package.  See Programming Perl, 4/e, page 304, and condense that construction with a Schwartzian Transform (page 949).
    #
    @$full_sensor_results = sort {
	    ( exists( $a->{service_profile_name} ) || exists( $a->{service_name} ) )
	cmp
	    ( exists( $b->{service_profile_name} ) || exists( $b->{service_name} ) )
	|| exists( $a->{service_name} ) cmp exists( $b->{service_name} )
	|| $a->{sensor_name} cmp $b->{sensor_name}
    } @$full_sensor_results;

    return $outcome;
}

sub print_discovery_results {
    my $self              = shift;
    my $discovery_results = shift;
    my $outcome           = 0;

    ## FIX MAJOR:  drop this
    ## print Data::Dumper->Dump( [$discovery_results], [qw($discovery_results)] );

    print "================================================================\n";
    print "Mostly-unanalyzed discovery results\n";
    print "================================================================\n";
    print "\n";

    if ( not defined($discovery_results) or not %$discovery_results ) {
	print "ERROR:  There are no discovery results to report.\n";
	return $outcome;
    }

    print "packet_version             = $discovery_results->{packet_version}\n";
    print "succeeded                  = " . ( $discovery_results->{succeeded} ? 'true' : 'false' ) . "\n";
    print "failure_message            = $discovery_results->{failure_message}\n" if defined $discovery_results->{failure_message};
    print "last_step                  = $discovery_results->{last_step}\n";
    print "if_duplicate               = $discovery_results->{if_duplicate}\n";
    print "soft_error_reporting       = $discovery_results->{soft_error_reporting}\n";
    print "change_policy              = $discovery_results->{change_policy}\n" if defined $discovery_results->{change_policy};
    print "registration_agent         = $discovery_results->{registration_agent}\n";

    # FIX MAJOR:  what kind of conditionals do these fields need to tell whether to print?
    print "forced_hostname            = $discovery_results->{forced_hostname}\n"  if defined $discovery_results->{forced_hostname};
    print "hostnames                  = @{$discovery_results->{hostnames}}\n"     if defined $discovery_results->{hostnames};
    print "ip_addresses               = @{$discovery_results->{ip_addresses}}\n"  if defined $discovery_results->{ip_addresses};
    print "mac_addresses              = @{$discovery_results->{mac_addresses}}\n" if defined $discovery_results->{mac_addresses};
    print "os_type                    = $discovery_results->{os_type}\n";

    print "configured_host_profile    = $discovery_results->{configured_host_profile}\n"
      if defined $discovery_results->{configured_host_profile};
    print "configured_service_profile = $discovery_results->{configured_service_profile}\n"
      if defined $discovery_results->{configured_service_profile};

    if ( @{ $discovery_results->{discovered_host_profiles} } ) {
	print "discovered_host_profiles:\n";
	foreach my $profile ( @{ $discovery_results->{discovered_host_profiles} } ) {
	    print "    $profile\n";
	}
    }
    if ( @{ $discovery_results->{discovered_service_profiles} } ) {
	print "discovered_service_profiles:\n";
	foreach my $profile ( @{ $discovery_results->{discovered_service_profiles} } ) {
	    print "    $profile\n";
	}
    }
    if ( defined( $discovery_results->{discovered_services} ) && @{ $discovery_results->{discovered_services} } ) {
	print "discovered_services:\n";
	foreach my $service ( @{ $discovery_results->{discovered_services} } ) {
	    print "    $service\n";
	}
    }

    if ( @{ $discovery_results->{full_sensor_results} } ) {
	print "\n";
	print "----------------------------------------------------------------\n";
	print "Sensor results\n";
	print "----------------------------------------------------------------\n";

	foreach my $sensor_results ( @{ $discovery_results->{full_sensor_results} } ) {
	    print "\n";
	    print "Sensor:\n";

	    print "sensor_name          = $sensor_results->{sensor_name}\n";
	    print "type                 = $sensor_results->{type}\n";
	    print "resource             = $sensor_results->{resource}\n" if defined $sensor_results->{resource};
	    print "cardinality          = $sensor_results->{cardinality}\n";
	    print "enabled              = " . ( $sensor_results->{enabled} ? 'true' : 'false' ) . "\n";
	    print "matched              = " . ( $sensor_results->{matched} ? 'true' : 'false' ) . "\n";
	    print "error_message        = $sensor_results->{error_message}\n"        if $sensor_results->{error_message};
	    print "host_profile_name    = $sensor_results->{host_profile_name}\n"    if defined $sensor_results->{host_profile_name};
	    print "service_profile_name = $sensor_results->{service_profile_name}\n" if defined $sensor_results->{service_profile_name};
	    print "service_name         = $sensor_results->{service_name}\n"         if defined $sensor_results->{service_name};
	    print "check_command        = $sensor_results->{check_command}\n"        if defined $sensor_results->{check_command};
	    print "command_arguments    = $sensor_results->{command_arguments}\n"    if defined $sensor_results->{command_arguments};
	    print "externals_arguments  = $sensor_results->{externals_arguments}\n"  if defined $sensor_results->{externals_arguments};

	    if ( defined( $sensor_results->{instances} ) and @{ $sensor_results->{instances} } ) {
		foreach my $instance ( @{ $sensor_results->{instances} } ) {
		    print "\n";
		    print "    Sensor Instance:\n";
		    print "    qualified_resource = $instance->{qualified_resource}\n" if defined $instance->{qualified_resource};
		    if ( defined( $instance->{raw_match_strings} ) ) {
			print "    Raw matched strings, quoted for visibility:\n";
			if ( @{ $instance->{raw_match_strings} } ) {
			    my $i = 0;
			    foreach my $match_string ( @{ $instance->{raw_match_strings} } ) {
				++$i;
				print "        \$MATCHED$i\$     = '$match_string'\n";
			    }
			}
			else {
			    print "        (no raw match strings)\n";
			}
		    }
		    if ( defined( $instance->{sanitized_match_strings} ) ) {
			print "    Sanitized match strings, quoted for visibility:\n";
			if ( @{ $instance->{sanitized_match_strings} } ) {
			    my $i = 0;
			    foreach my $match_string ( @{ $instance->{sanitized_match_strings} } ) {
				++$i;
				print "        \$SANITIZED$i\$   = '$match_string'\n";
			    }
			}
			else {
			    print "        (no sanitized match strings)\n";
			}
		    }
		    ## FIX MAJOR:  Maybe quote the values here?
		    print "    instance_suffix    = $instance->{instance_suffix}\n"   if defined $instance->{instance_suffix};
		    print "    instance_cmd_args  = $instance->{instance_cmd_args}\n" if defined $instance->{instance_cmd_args};
		    print "    instance_ext_args  = $instance->{instance_ext_args}\n" if defined $instance->{instance_ext_args};
		}
	    }
	}
    }

    print "\n";

    $outcome = 1;
    return $outcome;
}

# save discovery results to a local file
# FIX MAJOR:  use our full safety protocol (lock, temporary file, atomic rename, unlock)
#
# FIX MAJOR:  only do locking if we don't think this whole operation is already under the protection of a lock
#
sub save_discovery_results {
    my $self              = shift;
    my $discovery_results = shift;
    my $results_file      = shift;
    my $outcome           = 0;

    ## FIX MAJOR:  drop this
    ## print Data::Dumper->Dump( [$discovery_results], [qw($discovery_results)] );

    if ( not defined($discovery_results) or not %$discovery_results ) {
	$self->{logger}->error("ERROR:  There are no discovery results to save.");
	return $outcome;
    }

    my $json_discovery_results;
    eval {
	## I like four-spaces-per-level indentation in general, for all the code I write and all the data I dump,
	## if I can get that.  In my eyes, it makes everything easier to read.  It's not formally necessary for our
	## purposes here, but it's a nice-to-have.  Unfortunately, the standard JSON package, using the JSON::XS
	## package internally if it can find it (as it will in a distribution of GroundWork Perl), defaults the
	## pretty-printing indentation to three spaces per level, while providing no means to adjust this setting
	## at run time.  So we use JSON::PP instead, and call its indent_length() routine.  This will in general be
	## less efficient, but we're not encoding all that much data and we're not doing it often enough for the
	## difference in efficiency to matter much.
	##
	## The "canonical" flag sorts hashes into some fixed order, presumably alphabetically.  What that order is,
	## we don't care.  All we care about is that the same Perl structure will always yield exactly the same form
	## of the JSON file, so we can trivially compare discovery results by direct string comparison instead of by
	## having to analyze the structures in detail.  If we did care, JSON::PP has a sort_by() routine that would
	## allow us to intervene and establish a sort order we desired.
	##
	## We specify a latin1 encoding because anything we send downstream to Monarch has to be Latin-1 before it
	## can be inserted into the database.
	##
	my $json = JSON::PP->new->latin1->canonical->pretty->indent_length(4);
	$json_discovery_results = $json->encode($discovery_results);
    };
    if ($@) {
	$@ =~ s/\s+$//;  # chomp depends on $/, which might not contain what we expect
	$self->{logger}->error("ERROR:  Problem encountered while converting discovery results to JSON form ($@).");
	return $outcome;
    }

    # FIX MAJOR:  should we control the permissions on the created file?
    # FIX MAJOR:  should we ensure we do not clobber an existing file?
    if ( not open RESULTS, '>', $results_file ) {
	$self->{logger}->error("ERROR:  Cannot open the intended results file '$results_file' ($!).");
	return $outcome;
    }

    # FIX MAJOR:   combine the error checking for the write and the close as we have done elsewhere
    if ( not print RESULTS $json_discovery_results ) {
	$self->{logger}->error("ERROR:  Cannot write to the intended results file '$results_file' ($!).");
    }

    if ( not close RESULTS ) {
	$self->{logger}->error("ERROR:  Problem encountered while closing the results file '$results_file' ($!).");
	return $outcome;
    }

    $outcome = 1;
    return $outcome;
}

# This routine reads and compares the two files as raw undecoded strings.
sub discovery_results_are_identical {
    my $self             = shift;
    my $old_results_file = shift;
    my $new_results_file = shift;
    my $old_results      = undef;
    my $new_results      = undef;
    my $outcome          = 0;

    if ( not open RESULTS, '<', $old_results_file ) {
	$self->{logger}->error("ERROR:  Cannot open the old results file \"$old_results_file\" ($!).");
	return $outcome;
    }
    do {
	local $/;
	$old_results = readline RESULTS;
    };
    if ( not defined $old_results ) {
	$self->{logger}->error("ERROR:  Cannot read the old results file \"$old_results_file\" ($!).");
	close RESULTS;
	return $outcome;
    }
    if ( not close RESULTS ) {
	$self->{logger}->error("ERROR:  Problem encountered while closing the old results file \"$old_results_file\" ($!).");
	return $outcome;
    }

    if ( not open RESULTS, '<', $new_results_file ) {
	$self->{logger}->error("ERROR:  Cannot open the new results file \"$new_results_file\" ($!).");
	return $outcome;
    }
    do {
	local $/;
	$new_results = readline RESULTS;
    };
    if ( not defined $new_results ) {
	$self->{logger}->error("ERROR:  Cannot read the new results file \"$new_results_file\" ($!).");
	close RESULTS;
	return $outcome;
    }
    if ( not close RESULTS ) {
	$self->{logger}->error("ERROR:  Problem encountered while closing the new results file \"$new_results_file\" ($!).");
	return $outcome;
    }

    return $outcome if !defined($old_results) or !defined($new_results);

    return ( $new_results eq $old_results );
}

sub read_discovery_results {
    my $self              = shift;
    my $results_file      = shift;
    my $json_results      = undef;
    my $discovery_results = undef;
    my $outcome           = 0;

    if ( not open RESULTS, '<', $results_file ) {
	$self->{logger}->error("ERROR:  Cannot open the results file \"$results_file\" ($!).");
	return $outcome, $discovery_results;
    }
    do {
	local $/;
	$json_results = readline RESULTS;
    };
    if ( not defined $json_results ) {
	$self->{logger}->error("ERROR:  Cannot read the results file \"$results_file\" ($!).");
	close RESULTS;
	return $outcome, $discovery_results;
    }
    if ( not close RESULTS ) {
	$self->{logger}->error("ERROR:  Problem encountered while closing the results file \"$results_file\" ($!).");
	return $outcome, $discovery_results;
    }

    eval { $discovery_results = decode_json $json_results; };
    if ($@) {
	## We don't reflect the $json_results anywhere, to avoid providing a code path that could read
	## any arbitrary file and send it elsewhere.  If there is a problem decoding the input as JSON,
	## the problem should be diagnosed by independently capturing the discovery-results file stored
	## on the client and trying to decode it manually in a test environment, where you know exactly
	## what you are dealing with.
	$$self->{logger}->error("ERROR:  Found invalid JSON data in results file \"$results_file\".");
	return $outcome, $discovery_results;
    }

    $outcome = 1;
    return $outcome, $discovery_results;
}

=pod

    -- FIX MAJOR:  We may need something like the following inserted into the gwcollagedb database.
    -- Is it already present in a standard GWMEE 7.2.0 release?
    -- Do we have the right parameters defined for this script?
    INSERT INTO Action (ActionTypeID,Name,Description) VALUES (
	(SELECT ActionTypeID FROM ActionType WHERE Name = 'SCRIPT_ACTION'),
	'Register Agent by Discovery',
	'Invoke a script for the selected message'
    );
    INSERT INTO ActionProperty (ActionID, Name, Value) VALUES (
	(SELECT ActionID FROM Action WHERE Name = 'Register Agent by Discovery'),
	'Script',
	'/usr/local/groundwork/foundation/scripts/registerAgentByDiscovery.pl'
    );
    INSERT INTO ApplicationAction (ApplicationTypeID, ActionID) VALUES (
	(SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'GDMA'),
	(SELECT ActionID FROM Action WHERE Name = 'Register Agent by Discovery')
    );
    INSERT INTO ActionParameter (ActionID, Name, Value) VALUES (
	(SELECT ActionID FROM Action WHERE Name = 'Register Agent by Discovery'),
	'agent-type',
	'agent-type'
    );
    INSERT INTO ActionParameter (ActionID, Name, Value) VALUES (
	(SELECT ActionID FROM Action WHERE Name = 'Register Agent by Discovery'),
	'host-name',
	'host-name'
    );

=cut

# FIX MINOR:  To avoid code duplication, merge this version of do_timed_request() with
# the one from the GDMA poller, and move the final copy to the GDMA::Utils package.
sub do_timed_request {
    my $action    = $_[0];    # required argument
    my $useragent = $_[1];    # required argument
    my $request   = $_[2];    # required argument
    my $timeout   = $_[3];    # required argument
    my $response  = $_[4];    # required argument
    my $errormsg  = $_[5];    # required argument

    $$errormsg = '';
    my $successful = 1;

    if ( $^O eq 'MSWin32' ) {
	## See do_timed_mirror() for why we run differently on this platform.
	eval {
	    ## In case request() might ever die, we encapsulate it so we can keep our daemon running.
	    $$response = $useragent->request($request);
	};
	if ($@) {
	    $@ =~ s/\s+$//;  # chomp depends on $/, which might not contain what we expect
	    $$errormsg  = "$action failure ($@).";
	    $successful = 0;
	}
    }
    else {
	## Usually in a routine like this, we would wrap the code to which a timeout should apply
	## in an alarm($timeout) ... alarm(0) sequence (with lots of extra protection against race
	## conditions).  However, in the present case, the code we want to wrap already internally
	## absconds with control over SIGALRM.  So we need to impose an independent timer at this
	## level.  For that purpose, we have chosen to use the SIGABRT signal.
	local $SIG{ABRT} = \&catch_abort_signal;

	# If our timer expires, it may kill the wrapped code before it has a chance to cancel a
	# future alarm.  Hopefully it will have a local SIGALRM handler, so that setting should
	# be unwound automatically when we die out of our timer's signal handler and abort our
	# eval{};, but if we got such an uncanceled alarm and we either didn't have our own signal
	# handler in place or we hadn't ignored the signal at this level, we would exit.  It seems
	# safest to just use the same signal handler we're using for the SIGABRT signal.
	local $SIG{ALRM} = \&catch_abort_signal;

	## The nested eval{}; blocks protect against race conditions, as described in the comments.
	eval {
	    ## Create our abort timer in a disabled state.
	    my $timer = POSIX::RT::Timer->new( signal => SIGABRT );
	    eval {
		## Start our abort timer.
		$timer->set_timeout($timeout);

		# We might die here either explicitly or because of a timeout and the signal
		# handler action.  If we get the abort signal and die because of it, we need
		# not worry about resetting the abort before exiting the eval, because it has
		# already expired (we use a one-shot timer).
		eval {
		    ## The user-agent request() logic internally calls alarm() somewhere, perhaps
		    ## within some sleep() or equivalent indirect call.  That's why we switched
		    ## to using an independent timer and an independent signal (and signal
		    ## handler).  We haven't actually identified the line of code that does so,
		    ## but we have shown by experiment that this is the case, and it would kill
		    ## our own carefully-set SIGALRM timeout so it becomes inoperative.
		    ## FIX LATER:  Track down where the alarm stuff happens, and submit a bug
		    ## report that this should be described in the package documentation.
		    $$response = $useragent->request($request);    # Send request, get response
		};
		## We got here because one of the following happened:
		##
		## * the wrapped code die()d on its own (not that we have knowledge of any
		##   circumstances in which that might predictably happen), in which case we
		##   probably have our timer interrupt still armed, and possibly we might
		##   also have an alarm interrupt from the wrapped code still armed
		## * the wrapped code exited normally (either it ran to completion or it ran up
		##   against its own internal timeout), in which case we probably have our timer
		##   interrupt still armed
		## * our timer expired, in which case we might have an alarm interrupt from the
		##   wrapped code still armed
		##
		## If interrupts from both signals are still armed, there is no way to know the
		## relative sequence in which they will fire.  Consequently, we have two signals
		## we need to manage here, and we need to resolve all possible orders of signal
		## generation and the associated race conditions.  That accounts for the triple
		## nesting of eval{}; blocks here and the repeated signal cancellations.

		## Save the death rattle in case our subsequent processing inadvertenty changes it
		## before we get to use it.
		my $exception = $@;

		# In case the wrapped code's alarm was still armed when either it died on its
		# own or we aborted the code via our timer, disarm the alarm here.
		alarm(0);

		# Stop our abort timer.
		$timer->set_timeout(0);

		# Percolate failure to the next level of nesting.
		if ($exception) {
		    $exception =~ s/\s+$//;  # chomp depends on $/, which might not contain what we expect
		    die "$exception\n";
		}
	    };
	    ## Save the death rattle in case our subsequent processing inadvertenty changes it
	    ## before we get to use it.
	    my $exception = $@;

	    # In case the wrapped code died while its alarm was still armed, and our timer
	    # expired before we could disarm the alarm just above, disarm it here.
	    alarm(0);

	    # In case the wrapped code died while its alarm was still armed, and then the
	    # alarm fired just above before we could disarm it (and subsequently disarm our
	    # own timer), disarm our timer here.
	    $timer->set_timeout(0);

	    # Percolate failure to the next level of nesting.
	    if ($exception) {
		$exception =~ s/\s+$//;  # chomp depends on $/, which might not contain what we expect
		die "$exception\n";
	    }
	};
	## Check for either any residual cases where we failed to disable an interrupt before
	## it got triggered, or the percolation of whatever interrupt or other failure might
	## have occurred within the nested eval{}; blocks.
	if ($@) {
	    $@ =~ s/\s+$//;  # chomp depends on $/, which might not contain what we expect
	    $$errormsg  = "$action failure ($@).";
	    $successful = 0;
	}
    }

    return $successful;
}

sub get_version {
    return $main::VERSION;
}

# send discovery results to the GroundWork server
sub send_discovery_results {
    my $self             = shift;
    my $g_config         = shift;
    my $results_file     = shift;
    my $interactive      = shift;

    if ($interactive) {
	print "=======================================\n";
	print "Sending discovery results to the server\n";
	print "=======================================\n";
    }

    my $Auto_Register_User = $g_config->{Auto_Register_User};
    my $Auto_Register_Pass = $g_config->{Auto_Register_Pass};

    if ( !$Auto_Register_User or !$Auto_Register_Pass ) {
	$self->{logger}->error( "ERROR:  Cannot send discovery results to the server without credentials in hand"
	      . " (you need both the Auto_Register_User and Auto_Register_Pass configuration options set)." );
	print "=== returning early from send_discovery_results\n" if $interactive;
	return 0, undef;
    }

    my $target_addr = ( split( /[,\s]+/, $g_config->{Target_Server} ) )[0];

    my $register_by_discovery_url = $target_addr . '/foundation-webapp/restwebservices/autoRegister/registerAgentByDiscovery';

    my $params = '';
    if (not open RESULTS, '<', $results_file){
	$self->{logger}->error("ERROR:  Cannot open results file \"$results_file\" for reading ($!).");
	print "=== cannot open the discovery results file\n" if $interactive;
	return 0, undef;
    }
    do {
	local $/;
	$params = readline RESULTS;
    };
    if ( not defined $params ) {
	$self->{logger}->error("ERROR:  Cannot read the results file \"$results_file\" ($!).");
	print "=== cannot read discovery results\n" if $interactive;
	close RESULTS;
	return 0, undef;
    }
    if ( not close RESULTS ) {
	$self->{logger}->error("ERROR:  Problem encountered while closing the results file \"$results_file\" ($!).");
	print "=== cannot close the discovery results file\n" if $interactive;
	return 0, undef;
    }

    my $send_outcome = 1;
    my $response     = undef;
    my $errormsg     = undef;

    my $ssl_opts = GDMA::Utils::gdma_client_ssl_opts( $self->{logger} );
    ##
    ## FIX MAJOR:  See
    ## http://search.cpan.org/~mschilli/Log-Log4perl-1.49/lib/Log/Log4perl.pm#Dirty_Tricks
    ## http://log4perl.sourceforge.net/releases/Log-Log4perl/docs/html/Log/Log4perl/FAQ.html#b244f
    ## for a mechanism to capture error messages from LWP::UserAgent and similar packages.
    ##
    my $ua = LWP::UserAgent->new( agent => 'GDMA Client ' . get_version() . ' Auto-Setup/1.0', ssl_opts => $ssl_opts, );
    my $req = HTTP::Request->new( POST => $register_by_discovery_url );

    # Authorization field:
    # "Authorization: Basic " . Base64("gdma:gdma")
    # $req->header(Authorization => "Basic " . Base64("gdma:gdma"));
    #   $h->authorization_basic($Auto_Register_User, $Auto_Register_Pass);
    $req->authorization_basic($Auto_Register_User, $Auto_Register_Pass);

    # FIX MAJOR:  The content type should instead be specified as:
    #     $req->content_type('application/x-www-form-urlencoded');
    # since we are applying uri_escape() above to these parameters.
    # However, we need to test against a GWMEE 6.7.0 system to see
    # whether it receives the parameters in the same manner under
    # this proper content type (or for that matter, under the bad
    # content type as well).  In particular, look at the host MAC
    # address, which is likely to have had ":" characters encoded
    # to be represented as "%3A" on the wire, to see if the wire
    # encoding is visible on the receiving end in either case.
    # This issue is tracked as GDMA-377.
    $req->content_type('text/plain');
    $req->content($params);

    # The timeout is currently hardcoded here.  Possibly in some future version,
    # we might want to make this configurable, via an Auto_Register_Timeout setting.
    my $Auto_Register_Timeout = 30;

    # Default is both GET and HEAD redirect, if we don't make this call.  POST calls are already not
    # redirectable by default, but you can't be too careful.  We are now (as of GDMA 2.3.2) disabling
    # automatic redirects, as we must intervene and prevent a possible HTTPS-to-HTTP downgrade.
    # So all redirects are now handled manually here.
    $ua->requests_redirectable( [] );

    my $remaining_fetches = $g_config->{Max_Server_Redirects};
    $remaining_fetches = $Default_Max_Server_Redirects if not defined $remaining_fetches;

    for ( my $fetch = 1, ++$remaining_fetches ; $fetch && $remaining_fetches > 0 ; --$remaining_fetches ) {
	$fetch = 0;    # Only fetch upon redirect if explicitly commanded below.

	if ( not GDMA::Utils::is_valid_ca_path( \$errormsg, $register_by_discovery_url ) ) {
	    $self->{logger}->error("ERROR:  $errormsg");
	    $send_outcome = 0;
	}
	elsif ( not do_timed_request( 'Auto-Setup', $ua, $req, $Auto_Register_Timeout, \$response, \$errormsg ) ) {
	    $errormsg =~ s/\s+$//;  # chomp depends on $/, which might not contain what we expect
	    $self->{logger}->error("ERROR:  Auto-Setup failed:  $errormsg");
	    $send_outcome = 0;
	}
	elsif ( not $response->is_success ) {
	    if ( is_redirect( $response->code ) ) {
		## We allow the full response content, if any, to be dumped to the log just below, so we don't immediately
		## declare an unsuccessful Auto-Setup ("$send_outcome = 0;").  But we do want to log the formal
		## redirect Location, in case that might provide simpler diagnostic information.
		$self->{logger}->error( "ERROR:  Auto-Setup request was not processed -- " . $response->status_line);
		my $redirect_location = $response->header('Location');
		## Unless the protocol is downgraded from HTTPS to HTTP, we'll allow the redirect,
		## subject to the configured (or defaulted) Max_Server_Redirects value.
		if ( not defined $redirect_location ) {
		    $self->{logger}->error("        (got redirected, but with no new location supplied)");
		}
		elsif ($redirect_location =~ m{^https?://}i
		    && ( $register_by_discovery_url =~ m{^http://}i || $redirect_location =~ m{^https://}i )
		    && $remaining_fetches >= 2 )
		{
		    $self->{logger}->error("        (redirecting to $redirect_location)");
		    $register_by_discovery_url = $redirect_location;
		    $req->uri($register_by_discovery_url);
		    $fetch = 1;
		}
		else {
		    $self->{logger}->error("        (ignoring redirection to $redirect_location)");
		}
	    }
	}
    }

    my $server_response;
    if ($send_outcome) {
	my $http_status = $response->code;
	$server_response = $response->decoded_content( ref => 1 );
	if ( $response->is_success && $http_status == 200 ) {
	    $self->{logger}->error("NOTICE:  Auto-Setup request was processed.");
	}
	else {
	    ## We suppress the first message if we already emitted similar content just above.
	    $self->{logger}->error("ERROR:  Auto-Setup request was not processed; HTTP/S response code = $http_status.")
	      unless !$response->is_success and is_redirect( $response->code );
	    if (defined $$server_response) {
		$$server_response =~ s/\s+$//;  # chomp depends on $/, which might not contain what we expect
		$self->{logger}->error("Server response was:\n$$server_response");
	    }
	    $send_outcome = 0;
	}
    }

    my $result_data;
    if ($send_outcome) {
	##
	## FIX MAJOR:  for this type of request, we actually get back a JSON package, not an XML packet; fix this up
	## { "status": "success", "message": "Your assigned hostname is:  abc.xyz.com"}
	##
	print "server response is:  $$server_response\n" if $interactive;

	## FIX MAJOR:  check response for validity
	eval {
	    my $json = JSON::PP->new->latin1;
	    $result_data = $json->decode($$server_response);
	    local $Data::Dumper::Useqq = 1;
	    my $parsed_result_data = Data::Dumper->Dump( [$result_data], [qw($result_data)] );
	    print $parsed_result_data if $interactive;
	};
	if ($@) {
	    $@ =~ s/\s+$//;  # chomp depends on $/, which might not contain what we expect
	    $self->{logger}->error("ERROR:  JSON decoding of Auto-Setup response failed ($@).");
	    $send_outcome = 0;
	}
    }

    my $status;
    my $message;
    if ($send_outcome) {
	$status  = $result_data->{status};
	$message = $result_data->{message};
	if ( not defined $status ) {
	    $self->{logger}->error("ERROR:  Failed to auto-setup; no status value is available.");
	    $send_outcome = 0;
	}
	elsif ( $status ne 'success' && $status ne 'failure' ) {
	    $message =~ s/\s+$//;  # chomp depends on $/, which might not contain what we expect
	    $self->{logger}->error("ERROR:  Failed to auto-setup; status is $status; message is:\n$message");
	    $send_outcome = 0;
	}
    }

    # $send_outcome tells the caller whether the server received and succeeded in processing the data.
    # $status tells the caller whether the server found a problem with the data, which is distinct from
    # having succeeded in processing the data.  If the sending itself succeeds, a client-side failure
    # will be recognized on the server as there being a problem with the data.  Passing back both forms
    # of outcome is necessary for the client to understand how to interpret tho overall system behavior.
    return $send_outcome, $status, $message;
}

# decode the GroundWork server's respons to discovery results
sub decode_results_response {
    my $self    = shift;
    my $outcome = 0;

    ## FIX MAJOR:  fill this in

    return $outcome;
}

1;

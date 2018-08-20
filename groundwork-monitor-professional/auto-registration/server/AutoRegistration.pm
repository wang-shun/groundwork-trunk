package AutoRegistration;

# This is a simple, working package providing customizable actions
# that may occur during GroundWork Monitor auto-registration processing.

# Copyright (c) 2014 GroundWork, Inc. (www.gwos.com).  All rights reserved.
# Use of this software is subject to commercial license terms.

# This package can be replaced in service by a customer-provided package with
# the same API, to provide whatever host-attribute validation, recoding, and
# ancillary host assignments might be appropriate at the customer site.  This
# could, for instance, take into account any Network Address Translation in
# local use, or any trouble getting DNS to work properly on specific hosts.
#
# If you need to have different behavior than provided for here,
# you are encouraged to clone this routine in a package of your own
# name, and refer to your own package in the register_agent.conf file.

# Note:  This package does not yet handle IPv6 addresses in the way we want it to.
# The proper way to do so is to canonicalize all IPv6 addresses to the format
# described in RFC 5952.  But we have not yet found any standard Perl package
# which consistently normalizes IPv6 addresses to the recommended formats.
# We will need to work with the maintainers (e.g., of NetAddr::IP) to develop
# test cases and a standard routine to accept any-text-format of IP address as
# input, and to output one of the following forms:
#     d.d.d.d
#     x:x:x:x:x:x:x:x      (but with the :: abbreviation properly supported)
#     x:x:x:x:x:x:d.d.d.d  (but with the :: abbreviation properly supported)
# depending on the characteristics of the input IP address.  In all cases,
# leading zeroes in each field must be suppressed (though not the single 0
# in an all-zero field, except under the rules of :: abbreviation in an IPv6
# address), and lowercase hex digits must be used on output.  The standard
# prefix(es) for IPv4-in-IPv6 must be recognized, and if found, the mixed
# x:x:d.d notation must be output.

# TO DO:
# (*) gethostbyaddr() and gethostbyname() calls may internally refer to
#     external web services (e.g., DNS lookups) that could take a very long
#     time to return a value.  We might want to test the extremes of such
#     behavior, and provide appropriate timeouts around such operations.
# (*) If we actually used the MAC address for validation and storage, we
#     ought to also filter it to disallow usage of MAC addresses that represent
#     multicast frames (http://en.wikipedia.org/wiki/Multicast_address#Ethernet).

use strict;
use warnings;

use Socket;
# use Socket6;

# normalize_ip_address() is almost the only routine in this package which doesn't
# depend on a class instance, so it's the only routine we can safely export.
our (@ISA, @EXPORT, @EXPORT_OK, %EXPORT_TAGS, $VERSION);
BEGIN {
    use Exporter ();
    @ISA         = qw(Exporter);
    @EXPORT      = ();
    @EXPORT_OK   = qw(normalize_ip_address);
    %EXPORT_TAGS = ( DEBUG => [ @EXPORT, @EXPORT_OK ] );
    $VERSION     = "1.1.0";
}

# h_errno values are important for gethostbyaddr() and gethostbyname() calls.
# The following h_errno values are from <netdb.h>; these should really be
# looked up via some systematic method rather than being hardcoded here,
# but we want the associated messages, not just a symbol/number mapping.
my %h_errno = (
    -1 => "See errno.",                                                            # NETDB_INTERNAL
    0  => "No problem.",                                                           # NETDB_SUCCESS
    1  => "Authoritative Answer Host not found.",                                  # HOST_NOT_FOUND
    2  => "Non-Authoritative Host not found, or SERVERFAIL.  Try again later.",    # TRY_AGAIN
    3  => "Non recoverable errors, FORMERR, REFUSED, NOTIMP.",                     # NO_RECOVERY
    4  => "Valid name, no data record of requested type.",    # NO_DATA, or NO_ADDRESS (look for MX record)
);

# The new() constructor must be invoked as:
#     my $customer_package = AutoRegistration->new (\%config);
# because if it is invoked instead as:
#     my $customer_package = AutoRegistration::new (\%config);
# no invocant is supplied as the implicit first argument.

sub new {
    my $invocant = $_[0];                          # implicit argument
    my $config   = $_[1];                          # required argument; ref to %config hash
    my $class    = ref($invocant) || $invocant;    # object or class name
    my %self = (
	default_host_profile   => $config->{default_host_profile},
	default_hostgroup      => $config->{default_hostgroup},
	default_monarch_group  => $config->{default_monarch_group},
	hardcoded_hostnames    => $config->{hardcoded_hostnames},
	hostname_qualification => $config->{hostname_qualification},
	force_hostname_case    => $config->{force_hostname_case},
	force_domainname_case  => $config->{force_domainname_case},
	use_hostname_as_key    => $config->{use_hostname_as_key},
	use_mac_as_key         => $config->{use_mac_as_key}
    );
    bless \%self, $class;
}

sub debug {
    my $self        = $_[0];    # implicit argument
    my $debug_level = $_[1];
    $self->{debug_level} = $debug_level;
}

sub is_ipv4_addr {
    my $ipaddr = shift;
    ## FIX MINOR:  This expression is too primitive to properly distinguish valid IPv4
    ## address strings from all invalid strings.  See the CPAN IPv4 module's check_ip()
    ## routine, for example, or Regexp::Common::net, for a more-robust way to do this.
    return $ipaddr =~ /^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$/ ? 1 : 0;
}

sub is_ipv6_addr {
    my $ipaddr = shift;
    ## FIX MINOR:  Validate IPv6 addresses using some standard Perl module, such as
    ## Regexp::IPv6.  See http://forums.intermapper.com/viewtopic.php?t=452 for details.
    ## This expression is far too primitive, as it passes a variety of non-IPv6 strings.
    ## Perhaps try this instead:
    ## return $ipaddr =~ /^(((?=(?>.*?::)(?!.*::)))(::)?(([0-9A-F]{1,4})::?){0,5}|((?5):){6})(\2((?5)(::?|$)){0,2}|((25[0-5]|(2[0-4]|1[0-9]|[1-9])?[0-9])(\.|$)){4}|(?5):(?5))(?<![^:]:|\.)\z/i ? 1 : 0;
    return $ipaddr =~ /^[0-9a-f]{0,4}(:[0-0a-f]{0,4])+(:\d{1,3}(\.\d{1,3})+)?$/i ? 1 : 0;
}

# Normalize both IPv4 and IPv6 addresses to a standard format.
# For IPv4 addresses, perform the following conversions:
#   * express in ordinary dotted-quad notation
# For IPv6 addresses, perform conversions to implement
# the following proposed standard normalized format:
#   http://tools.ietf.org/html/rfc5952
#   http://en.wikipedia.org/wiki/IPv6_address#Recommended_representation_as_text
# We should use a standard Perl module for IPv6 address normalization, but there
# doesn't seem to be one available as of this writing.  We will need to work with
# the authors of appropriate packages to incorporate this kind of canonicalization.
sub normalize_ip_address {
    my $ipaddr = shift;
    if ( is_ipv4_addr($ipaddr) ) {
	return inet_ntoa( inet_aton($ipaddr) );
    }
    ## FIX MINOR:  Normalize IPv6 addresses using RFC 5952 conventions, including
    ## IPv4-in-IPv6 notations.  See the Note at the beginning of this package for
    ## details.  Until we do so, IPv6 is not properly supported here.
    if ( is_ipv6_addr($ipaddr) ) {
	return $ipaddr;
    }
    return undef;
}

sub is_local_name {
    my $self     = $_[0];    # implicit argument
    my $Hostname = $_[1];
    ( my $short_name = $Hostname ) =~ s/\..*//;
    return ( $short_name eq 'localhost' );
}

# Check for loopback addresses.
sub is_local_addr {
    my $self      = $_[0];    # implicit argument
    my $IPaddress = $_[1];
    ## For now, we don't bother trying to match an IPv4-mapped IPv6 address for
    ## localhost if it's written entirely in IPv6 notation, because of the
    ## complexity of trying to match the proper pattern while excluding all others.
    return ( $IPaddress =~ /^127\./ or $IPaddress eq '::1' or $IPaddress =~ /^::ffff:127\.\d{1,3}\.\d{1,3}\.d{1,3}$/ );
}

# Check for special broadcast addresses, IPv4 and IPv6 multicast addresses, and other special ranges.
# If we also knew the subnet mask, we could also check for subnet-specific network and broadcast
# addresses, but we don't have that information.
sub is_special_addr {
    my $self      = $_[0];    # implicit argument
    my $IPaddress = $_[1];

    # FIX MINOR:  We should extend this checking to cover all of the following IPv4 address ranges:
    #   0.0.0.0/8		local broadast		http://tools.ietf.org/html/rfc1700
    #   169.254.0.0/16		DHCP autoconfiguration	http://tools.ietf.org/html/rfc3330
    #   192.0.2.0/24		documentation only	http://tools.ietf.org/html/rfc5737
    #   198.51.100.0/24		documentation only	http://tools.ietf.org/html/rfc5737
    #   203.0.113.0/24		documentation only	http://tools.ietf.org/html/rfc5737
    #   224.0.0.0/4		multicast		http://tools.ietf.org/html/rfc5771
    #   240.0.0.0/4		reserved		http://tools.ietf.org/html/rfc3330
    #   255.255.255.255/32	limited broadcast	http://tools.ietf.org/html/rfc3330
    # as well as equivalent IPv6 address ranges.  Checking inclusion within CIDR ranges like this
    # is probably best done via some standard Perl module, not by hand-rolled code.  See also:
    # http://en.wikipedia.org/wiki/Multicast_address
    # http://en.wikipedia.org/wiki/Reserved_IP_addresses
    # http://www.iana.org/assignments/multicast-addresses/multicast-addresses.xml
    # http://tools.ietf.org/html/rfc1112

    return ( $IPaddress eq '224.0.0.1' or $IPaddress eq 'ff02::1' ) ? 1 : 0;
}

# The form of the hostname can be altered here in a uniform way across all hosts,
# or in a specialized way for certain classes of hosts.  Parameters such as the
# IP address are provided in case your logic might act differently for certain
# subnets, for instance.
sub qualified_hostname {
    my $self       = $_[0];    # implicit argument
    my $AgentType  = $_[1];    # 'GDMA' for GDMA clients; supplied for future flexibility
    my $Hostname   = $_[2];    # supposed to be FQDN, though not guaranteed
    my $IPaddress  = $_[3];    # in dotted-quad decimal form, or some IPv6 variant
    my $MACaddress = $_[4];    # in normalized form:  zero-padded, colon-separated, uppercase hex digits
    my $HostOS     = $_[5];    # usually a generic value, like 'windows', 'linux', 'solaris', 'aix', 'hpux'

    # Historically, GDMA lowercased any hostname it used, for consistency, though that
    # is now configurable on each GDMA client.  If that convention is being enforced on
    # the clients, the configuration for auto-registration should be set to match that
    # here as well as anywhere else we substitute an alternate hostname for this one.
    if ( $self->{force_hostname_case} ne 'as-is' || $self->{force_domainname_case} ne 'as-is' ) {
	$Hostname =~ /^([^.]+)(\..+)?$/;
	my $host = $1;
	my $domain = $2 || '';
	$host   = lc($host)   if $self->{force_hostname_case}   eq 'lower';
	$host   = uc($host)   if $self->{force_hostname_case}   eq 'upper';
	$domain = lc($domain) if $self->{force_domainname_case} eq 'lower';
	$domain = uc($domain) if $self->{force_domainname_case} eq 'upper';
	$Hostname = $host . $domain;
    }

    if ( $self->{hostname_qualification} eq 'full' ) {
	## The client is supposed to have sent in a fully-qualified hostname,
	## so we just leave that alone.  Possibly we might want logic here to
	## verify our assumption and attempt to correct the situation if the
	## client somehow might have sent in a shortname instead.
    }
    elsif ( $self->{hostname_qualification} eq 'short' ) {
	## Force into shortname form.
	$Hostname =~ s/\..*//;
    }
    else {
	## Custom logic is selected for determining the form of the hostname.
	## We don't have any such logic here, but it would go here if we did.
	## Such logic might, for instance, specify that short names should be
	## used for most machines, while fully qualified names should be used
	## for certain classes of machines.  Or it might expand the hostname
	## into a fully qualified form if the client sent in a shorter form
	## (though perhaps arguably, that case ought to be handled by 'full').
    }

    return $Hostname;
}

# The $outcome returned from this routine is supposed to reflect whether an
# initial lookup should be attempted using the returned host attributes.
#
# * A negative result means the attributes are fundamentally bad, and all matching
#   and host addition should be blocked.
#
# * A zero result means that we are unable to determine acceptable attributes
#   here, so an initial lookup should be blocked, but there might be hope for the
#   hard recoding to produce acceptable host attributes.
#
# * A positive result means we have provided reasonable host attributes for an
#   initial lookup in Monarch.
sub soft_recode_host_attributes {
    my $self       = $_[0];    # implicit argument
    my $AgentType  = $_[1];    # 'GDMA' for GDMA clients; supplied for future flexibility
    my $Hostname   = $_[2];    # as supplied by the client; supposed to be FQDN, though not guaranteed
    my $IPaddress  = $_[3];    # in dotted-quad decimal form, or some IPv6 variant
    my $MACaddress = $_[4];    # in normalized form:  zero-padded, colon-separated, uppercase hex digits
    my $HostOS     = $_[5];    # usually a generic value, like 'windows', 'linux', 'solaris', 'aix', 'hpux'
    my $outcome    = undef;    # integer status of this call:  -1 => abandon all hope; 0 => failure; 1 => success
    my @errors     = ();       # any error messages to be passed back to the caller
    my $skip_tests = 0;

    $outcome = 1;              # presume innocent until proven guilty

    $Hostname = $self->qualified_hostname($AgentType, $Hostname, $IPaddress, $MACaddress, $HostOS);

    my $canonical_address = ( $self->{use_hostname_as_key} || $self->{use_mac_as_key} ) ? $IPaddress : normalize_ip_address($IPaddress);
    if (not defined $canonical_address) {
	$outcome = -1;
	push @errors, "\"$IPaddress\" is not a valid IP address";
	$skip_tests = 1;
    }

    # The MAC address is considered to be the most reliable attribute.  The IP address
    # is considered to be next most reliable attribute, though we might question that
    # due to the use of private networks and Network Address Translation.

    if ( $AgentType eq 'VEMA' ) {
	## VEMA host attributes (hostname, ipaddr, macaddr) are considered to be highly
	## accurate, so in theory we should never attempt to remap them to something else.
	## However, I see no reason that the simple tests we are doing below can't be
	## reasonably applied to VEMA hosts as well, so for now, we won't skip that testing.
	# $skip_tests = 1;
    }

    unless ($skip_tests) {{
	my $name_is_localhost = $self->is_local_name($Hostname);
	my $addr_is_localhost = $self->is_local_addr($canonical_address);
	my $addr_is_special   = $self->is_special_addr($canonical_address);
	if ($addr_is_special) {
	    ## If the client has sent in a multicast address, then the data is considered to be
	    ## so messed up that we don't even attempt to perform a lookup of the IP address by
	    ## the hostname to see if the IP address can be corrected.  And we purposely stop
	    ## any further processing of this host.
	    $outcome = -1;
	    push @errors, "IP address $canonical_address is a multicast address";
	    last;
	}
	## How a remote host could see itself as localhost and not as a network
	## host, I don't know, but it's been claimed that this sometimes happens.
	## So we need to protect ourselves against such sillyness.
	if ( $name_is_localhost and !$addr_is_localhost ) {
	    ## FIX LATER:  When we upgrade to full IPv6 support, this section should be implemented with
	    ## Socket::getaddrinfo() and friends, and possibly using Perl >= 5.14 (preferably 5.16 or later).
	    if ( is_ipv4_addr($canonical_address) ) {
		my $packed_addr = inet_aton($canonical_address);
		my ( $name, $aliases, $addrtype, $length, @addrs ) = gethostbyaddr( $packed_addr, AF_INET );
		if ( not defined $name ) {
		    $outcome = 0;
		    push @errors, "gethostbyaddr($canonical_address, AF_INET) failed:  "
		      . ( ( $? == -1 ) ? "$!" : $h_errno{$?} ? $h_errno{$?} : "Unknown problem." );
		    last;
		}
		else {
		    ## The call to gethostbyaddr() succeeded.
		    ## $name is the official name of the host.
		    ## $aliases is a space-separated list of alternate names, but we're going to ignore them
		    ##     both because we want to use the official name, and because if the official name
		    ##     is still a "localhost"-equivalent, then likely any alias which is not also a
		    ##     similar equivalent refers to the server, not the client.
		    ## @addrs is a list of packed binary addresses.
		    ## Since we trusted the IP address to be correct enough to look up the host name,
		    ## we're not going to override it now based on what @addrs contains.  But a second
		    ## pass of analysis (hard recoding) could potentially do so.
		    if ( $self->is_local_name($name) ) {
			## The IP address also turned out to be insufficient to find the desired hostname,
			## which makes the IP address itself suspect.  What we'd like to do here is to
			## run a MACaddress-to-IPaddress lookup, see if that yields a different IP address,
			## then try an IPaddress-to-hostname lookup again.  But we don't yet know how to
			## run the MAC-to-IP lookup reliably.  Possibly, in the future we might do something
			## like look in the "nedi" database, to see if that contains a useful mapping.
			$outcome = 0;
			push @errors, "IP address $canonical_address is a \"localhost\" equivalent";
			last;
		    }
		    else {
			$Hostname = $self->qualified_hostname($AgentType, $name, $canonical_address, $MACaddress, $HostOS);
		    }
		}
	    }
	    elsif ( is_ipv6_addr($canonical_address) ) {
		## FIX LATER:  we don't yet handle IPv6 addresses
		$outcome = 0;
		push @errors, "IP address $canonical_address is a \"localhost\" equivalent";
		last;
	    }
	    else {
		## unknown/illegal address
		$outcome = -1;
		push @errors, "IP address $canonical_address is of unknown type";
		last;
	    }
	}
	elsif ($name_is_localhost and $addr_is_localhost) {
	    ## If the client has sent in identifying data that all looks like localhost, we don't
	    ## have a sufficient handle to figure out what not-this-same-server host is really
	    ## asking to be registered.  So we purposely stop any further processing of this host.
	    $outcome = -1;
	    push @errors, "client host $Hostname and IP address $canonical_address both look like localhost";
	    last;
	}
	elsif ( !$name_is_localhost and $addr_is_localhost ) {
	    ## We believe this situation can arise because the client host found its own IP address using
	    ## gethostbyname($unqualified_short_name), which is likely to return 127.0.0.1 as its result.
	    ## Presumably, the result on the server side will be somewhat different.
	    ##
	    ## gethostbyname() can return multiple addresses, of which the first is presumably considered
	    ## to be canonical.  We will use the first address that doesn't look like localhost, since we
	    ## consider it to be the one most likely to match the hostname as seen by the server on which
	    ## this package is running.
	    my ( $name, $aliases, $addrtype, $length, @addrs ) = gethostbyname($Hostname);
	    if ( not defined $name ) {
		$outcome = -1;
		push @errors, "gethostbyname($Hostname) failed:  "
		  . ( ( $? == -1 ) ? "$!" : $h_errno{$?} ? $h_errno{$?} : "Unknown problem." );
		last;
	    }
	    else {
		my $alternate_address = undef;
		## FIX LATER:  For the moment, we only handle IPv4 addresses returned from gethostbyname().  We might need
		## Perl >= 5.14, preferably 5.16 or later, and Socket::getnameinfo(), and/or the Socket6 package in earlier
		## releases of Perl, to handle IPv6 addresses.  See http://www.perl.org/about/whitepapers/perl-ipv6.html for
		## a brief overview.
		if ( $addrtype == AF_INET ) {
		    foreach my $addr (@addrs) {
			my $printable = inet_ntoa($addr);
			if ( !$self->is_local_addr($printable) and !$self->is_special_addr($printable) ) {
			    $alternate_address = $printable;
			    last;
			}
		    }
		}
		if ( defined $alternate_address ) {
		    $canonical_address =
		      ( $self->{use_hostname_as_key} || $self->{use_mac_as_key} )
		      ? $alternate_address
		      : normalize_ip_address($alternate_address);
		    if (not defined $canonical_address) {
			$outcome = -1;
			push @errors, "derived address \"$alternate_address\" is not a valid IP address";
		    }
		}
		else {
		    ## We can't find any address for this host that doesn't look invalid for our purposes.
		    ## So we just have to abort further processing for this host.  (A future revision of
		    ## this package might relax this slightly [$outcome = 0;], and allow hard recoding to
		    ## attempt to find an appropriate IP address, but we'll leave that for a later release.)
		    $outcome = -1;
		    push @errors, "client host $Hostname has IP address $canonical_address that looks like localhost";
		    last;
		}
	    }
	}
    }}

    return ( $outcome, \@errors, $Hostname, $canonical_address, $MACaddress, $HostOS );
}

# Optional second-pass recoding routine.  If you have no need for this in a custom
# version of this package, just don't define this routine.  Then it won't be called, and
# the second pass of Monarch lookup will be skipped as well.  Or just always return a
# zero $outcome, which will do the same thing.
#
# The $outcome returned from this routine is supposed to reflect whether a second lookup
# should be attempted using the returned host attributes, assuming that the incoming
# attributes either weren't good enough for a first lookup or the first lookup failed.
# So we only return a positive result if we actually change the attributes.  Otherwise,
# there's no point in doing a second lookup.
#
# * A negative result means the attributes are fundamentally bad, and all matching
#   and host addition should be blocked.
#
# * A zero result means that we are unable to determine acceptable recoded attributes
#   here, so a second lookup should be blocked, but that shouldn't stop the host from
#   being added with the host attributes as returned from this routine.
#
# * A positive result means we have provided reasonable modified host attributes for a
#   second lookup in Monarch.
sub hard_recode_host_attributes {
    my $self       = $_[0];    # implicit argument
    my $AgentType  = $_[1];    # 'GDMA' for GDMA clients; supplied for future flexibility
    my $Hostname   = $_[2];    # as supplied by the client, or as soft-recoded
    my $IPaddress  = $_[3];    # in dotted-quad decimal form, for an IPv4 address
    my $MACaddress = $_[4];    # in normalized form:  zero-padded, colon-separated, uppercase hex digits
    my $HostOS     = $_[5];    # usually a generic value, like 'windows', 'linux', 'solaris', 'aix', 'hpux'
    my $outcome    = undef;    # success (true) or failure (false) status of this call
    my @errors     = ();       # any error messages to be passed back to the caller

    $outcome = 0;              # in this pass of recoding, presume failure

    # Pay attention to the associations defined in the config file.  Intended for emergency use only,
    # as this effectively bypasses DNS, which ought to be the system of record for such lookups.
    my $canonical_address = ( $self->{use_hostname_as_key} || $self->{use_mac_as_key} ) ? $IPaddress : normalize_ip_address($IPaddress);
    if (not defined $canonical_address) {
	$outcome = -1;
	push @errors, "\"$IPaddress\" is not a valid IP address";
    }
    elsif ( $self->{hardcoded_hostnames}{$canonical_address} ) {
	$Hostname = $self->{hardcoded_hostnames}{$canonical_address};
	if ( $self->{force_hostname_case} ne 'as-is' || $self->{force_domainname_case} ne 'as-is' ) {
	    $Hostname =~ /^([^.]+)(\..+)?$/;
	    my $host = $1;
	    my $domain = $2 || '';
	    $host   = lc($host)   if $self->{force_hostname_case}   eq 'lower';
	    $host   = uc($host)   if $self->{force_hostname_case}   eq 'upper';
	    $domain = lc($domain) if $self->{force_domainname_case} eq 'lower';
	    $domain = uc($domain) if $self->{force_domainname_case} eq 'upper';
	    $Hostname = $host . $domain;
	}
	$outcome  = 1;         # successful recoding
    }

    if ($outcome == 0 and !@errors) {
	push @errors, "no hard recoding found for client host $Hostname with IP address $canonical_address";
    }

    return ( $outcome, \@errors, $Hostname, $canonical_address, $MACaddress, $HostOS );
}

sub host_profile_to_assign {
    my $self           = $_[0];    # implicit argument
    my $AgentType      = $_[1];    # 'GDMA' for GDMA clients; supplied for future flexibility
    my $Hostname       = $_[2];    # as supplied by the client, then possibly recoded
    my $IPaddress      = $_[3];    # in dotted-quad decimal form, for an IPv4 address
    my $MACaddress     = $_[4];    # in normalized form:  zero-padded, colon-separated, uppercase hex digits
    my $HostOS         = $_[5];    # usually a generic value, like 'windows', 'linux', 'solaris', 'aix', 'hpux'
    my $HostProfile    = $_[6];    # may be an empty string
    my $ServiceProfile = $_[7];    # may be an empty string
    my $outcome        = undef;    # success (true) or failure (false) status of this call
    my @errors         = ();       # any error messages to be passed back to the caller

    # This trivial default logic can be replaced in a customer-modified and renamed
    # version of this package.

    # We perform trivial validation of the client input before using it.
    $HostOS = 'no-os' if $HostOS !~ /^\w+$/;
    (my $default_host_profile = $self->{default_host_profile}) =~ s/{HOST_OS}/$HostOS/;
    my $host_profile = $HostProfile ? $HostProfile : $default_host_profile;
    if ($host_profile) {
	$outcome = 1;
    }
    else {
	$outcome = 0;
	push @errors, "no host profile found for client host $Hostname with IP address $IPaddress";
    }

    return ( $outcome, \@errors, $host_profile );
}

# Determine which hostgroup(s) the host should be assigned to.
sub hostgroups_to_assign {
    my $self           = $_[0];    # implicit argument
    my $AgentType      = $_[1];    # 'GDMA' for GDMA clients; supplied for future flexibility
    my $Hostname       = $_[2];    # as supplied by the client, then possibly recoded
    my $IPaddress      = $_[3];    # in dotted-quad decimal form, for an IPv4 address
    my $MACaddress     = $_[4];    # in normalized form:  zero-padded, colon-separated, uppercase hex digits
    my $HostOS         = $_[5];    # usually a generic value, like 'windows', 'linux', 'solaris', 'aix', 'hpux'
    my $HostProfile    = $_[6];    # may be an empty string
    my $ServiceProfile = $_[7];    # may be an empty string
    my $outcome        = undef;    # success (true) or failure (false) status of this call
    my @errors         = ();       # any error messages to be passed back to the caller
    my @hostgroups     = ();

    # This trivial default logic can be replaced in a customer-modified and renamed
    # version of this package.
    push @hostgroups, $self->{default_hostgroup};
    $outcome = 1;

    return ( $outcome, \@errors, \@hostgroups );
}

# Determine which Monarch group(s) the host should be assigned to.  There must be at least one such
# group, so a location for the generated externals file can be determined for this host.  A given host
# can be a member of multiple Monarch groups, but if so, those Monarch groups are generally related in
# a tree of such groups, all sharing the same configured "build folder" location (see Configuration
# > Groups > Groups > {monarch group} > Detail > Build Instance Properties > Build folder).  Such a
# structure can be used to advantage in how Monarch group macros are expanded and possibly overridden
# when externals are built.
#
# GWMON-8827:  Note that a given host may be a member of more than one Monarch group.  Which group
# takes precedence in that situation [while externals are being built] is currently arbitrary and
# uncontrolled.  And this affects which directory may be used to hold the generated externals file.
# Only one externals file will be generated for each such host, not one per directory if the
# respective Monarch groups refer to different build folders.
sub monarch_groups_to_assign {
    my $self           = $_[0];    # implicit argument
    my $AgentType      = $_[1];    # 'GDMA' for GDMA clients; supplied for future flexibility
    my $Hostname       = $_[2];    # as supplied by the client, then possibly recoded
    my $IPaddress      = $_[3];    # in dotted-quad decimal form, for an IPv4 address
    my $MACaddress     = $_[4];    # in normalized form:  zero-padded, colon-separated, uppercase hex digits
    my $HostOS         = $_[5];    # usually a generic value, like 'windows', 'linux', 'solaris', 'aix', 'hpux'
    my $HostProfile    = $_[6];    # may be an empty string
    my $ServiceProfile = $_[7];    # may be an empty string
    my $outcome        = undef;    # success (true) or failure (false) status of this call
    my @errors         = ();       # any error messages to be passed back to the caller
    my @monarch_groups = ();

    # This trivial default logic can be replaced in a customer-modified and renamed
    # version of this package.
    push @monarch_groups, $self->{default_monarch_group};
    $outcome = 1;

    return ( $outcome, \@errors, \@monarch_groups );
}

1;

__END__

=head1 NAME

AutoRegistration - routines to control the behavior of host auto-registration processing

=head1 SYNOPSIS

    my $customer_network_package = "AutoRegistration";

    # In normal application, this package is require'd, not use'd, because
    # it may be dynamically replaced by some other package with the same API.
    eval { require "$customer_network_package.pm"; };
    if ($@) {
	## 'require' died; $customer_network_package is not available.
	...;
    }
    else {
	## 'require' succeeded; $customer_network_package was loaded.
	...;
    }

    my $customer_network = $customer_network_package->new(
	{
	    default_host_profile   => $default_host_profile,
	    default_hostgroup      => $default_hostgroup,
	    default_monarch_group  => $default_monarch_group,
	    hardcoded_hostnames    => \%hardcoded_hostnames,
	    hostname_qualification => $hostname_qualification,
	    force_hostname_case    => $force_hostname_case,
	    force_domainname_case  => $force_domainname_case,
	    use_hostname_as_key    => $use_hostname_as_key,
	    use_mac_as_key         => $use_mac_as_key
	}
    );

    $customer_network->debug($debug_level);

    $boolean = $customer_network->is_local_name($Hostname);
    $boolean = $customer_network->is_local_addr($IPaddress);
    $boolean = $customer_network->is_special_addr($IPaddress);

    $Hostname = $customer_network->qualified_hostname($AgentType, $Hostname, $IPaddress, $MACaddress, $HostOS);

    ( $outcome, $err_ref, $host_name, $host_ip, $host_mac, $host_os ) =
      $customer_network->soft_recode_host_attributes( $agent_type, $host_name, $host_ip, $host_mac, $host_os );

    ( $outcome, $err_ref, $host_name, $host_ip, $host_mac, $host_os ) =
      $customer_network->hard_recode_host_attributes( $agent_type, $host_name, $host_ip, $host_mac, $host_os );

    ( $outcome, $err_ref, $hostgroups ) = $customer_network->hostgroups_to_assign
      ( $agent_type, $host_name, $host_ip, $host_mac, $host_os, $host_profile, $service_profile );

    ( $outcome, $err_ref, $monarch_groups ) = $customer_network->monarch_groups_to_assign
      ( $agent_type, $host_name, $host_ip, $host_mac, $host_os, $host_profile, $service_profile );

=head1 DESCRIPTION

This module provides basic support for auto-registration host validation
and host-addition actions.  If the customer has special needs not covered
by the standard version of this package, it should be cloned under another
package name, modified to suit the local needs, and the new package
referenced in the register_agent.conf configuration file.  That will
reduce confusion as to what is actually running on the customer system.

A typical special local situation that can be supported by this package
is a reassignment of the hostname to some other name (based on the
IP address, which is assumed to be correct in this instance), if the
calling host cannot correctly determine its own hostname.  This typically
happens on just a few machines for which DNS is misconfigured and cannot
be corrected in a timely fashion.

=head1 SUBROUTINES/METHODS

=over

=item new()

This method returns an object reference which can be used to access the methods for
making adjustments to auto-registration processing, as illustrated in the SYNOPSIS.

The B<new()> constructor must be invoked as:

    my $config = AutoRegistration->new ();

because if it is invoked instead as:

    my $config = AutoRegistration::new ();

no invocant is supplied as the implicit first argument.

=item $customer_network->debug($debug_level);

This method sets the debug level for any log messages we might emit from
this package (i.e., that are returned to the caller via the $err_ref
output parameter).  The standard levels assumed by the caller are:

    0 => suppress all debug output except for major errors
    1 => print error info and summary statistical data
    2 => also print basic debug info
    3 => also print detailed debug info

=item $boolean = $customer_network->is_local_name($Hostname);

This method identifies whether a specified hostname is equivalent
to C<localhost>.  Such a hostname makes no sense in the context of
auto-registration, and if found, the package should attempt to recode
it to something else.

=item $boolean = $customer_network->is_local_addr($IPaddress);

This method identifies whether a specified IP address is equivalent
to C<localhost>.  Such an address makes no sense in the context of
auto-registration, and if found, the package should attempt to recode
it to something else.

=item $boolean = $customer_network->is_special_addr($IPaddress);

This method identifies whether a specified IP address represents a
broadcast, multicast, or other special address which makes no sense
as the address of an individual host.  Such special-purpose addresses
should not be allowed as host IP addresses, for obvious reasons.

=item $Hostname = $customer_network->qualified_hostname($AgentType, $Hostname, $IPaddress, $MACaddress, $HostOS);

This method is used to force the hostname into a standard form,
as configured for this instance of the package.  Typical settings
are to force all fully-qualified hostnames, or to force all
unqualified hostnames, or to call some kind of custom logic to make
this determination.  A customer-modified version of this package,
implementing that custom logic, would be needed for the latter option.

The critical parameter is $Hostname; the other parameters are provided
as part of this API only so that any modified version of this package
can use them in the custom-logic section for its determination of the
desired form of the hostname.

The qualified_hostname() routine is normally expected to be called only
internally to this package.

=item ( $outcome, $err_ref, $host_name, $host_ip, $host_mac, $host_os ) = $customer_network->soft_recode_host_attributes( $agent_type, $host_name, $host_ip, $host_mac, $host_os );

This method allows the package to intervene and supply alternate values
for use in subsequent processing, notably for the first lookup of the
host in the Monarch database.  These alternate values will be used for
subsequent processing in the calling program, even if the outcome from
the call was some type of failure, so you need to be careful in all
cases about what values are returned.  The later processing includes
being passed back to the C<hard_recode_host_attributes()> routine for a
second round of attribute modification if the host is not found in the
database.  If the soft recoding fails, this is taken as an indication
that the first lookup in Monarch should be skipped, in favor of moving
directly to calling the hard recoding.

The return values include an indicator of the success of the call,
which includes not just a determination of whether it could recode
the host attributes, but also whether all future attempts to match the
host should be abandoned because the data is completely untrustworthy,
incoherent, or inappropriate.  In addition, the routine returns an
arrayref reflecting any error messages to be logged by the caller,
and a set of potentially revised host attributes.

=item ( $outcome, $err_ref, $host_name, $host_ip, $host_mac, $host_os ) = $customer_network->hard_recode_host_attributes( $agent_type, $host_name, $host_ip, $host_mac, $host_os );

This method allows the package to intervene and supply alternate values
for use in subsequent processing, notably for a second lookup of the host
in the Monarch database.  This would typically be used for more-aggressive
modification of the values in an attempt to match some existing host in
the database.  If the hard recoding fails, this is taken as an indication
that it doesn't consider the host-attribute data to be trustworthy enough
to either match what is in Monarch or to add the host to Monarch.

The return values include a success/failure indicator (encoded according
to the same conventions used for the soft recoding), an arrayref
reflecting any error messages to be logged by the caller, and a set of
potentially revised host attributes.  In the case of the hard recoding,
the outcome should not be positive unless the routine actively changed
the host attributes.  Otherwise, there is no point in a second lookup.

=item ( $outcome, $err_ref, $hostgroups ) = $customer_network->hostgroups_to_assign( $agent_type, $host_name, $host_ip, $host_mac, $host_os, $host_profile, $service_profile );

This method returns an arrayref reflecting the set of hostgroups the
host should be assigned to, along with the usual status indicator and
arrayref reflecting any error messages.

=item ( $outcome, $err_ref, $monarch_groups ) = $customer_network->monarch_groups_to_assign( $agent_type, $host_name, $host_ip, $host_mac, $host_os, $host_profile, $service_profile );

This method returns an arrayref reflecting the set of Monarch
configuration groups the host should be assigned to, along with the
usual status indicator and arrayref reflecting any error messages.

=back

=head1 CONFIGURATION AND ENVIRONMENT

No environment variables will be used.

=head1 SEE ALSO

The GroundWork Bookshelf should contain more information about auto-registration.

=head1 LICENSE AND COPYRIGHT

Copyright (c) 2014 GroundWork Open Source, Inc.

This library is free software; you can redistribute it and/or
modify it under the same terms as Perl itself.

=head1 BUGS AND LIMITATIONS

This is the first draft of this package.  It may evolve as we gain more experience with
the types of validation and adjustments that customers find useful in practice.  To support
backward compatiblity with packages that customers may have modified and fielded, calls from
the auto-registration script to any new routines must be done only after testing to see whether
the named routine exists in the package (via a C<$customer_network-E<gt>can('new_routine_name')>
call).  If it does not exist, that call must be skipped.

=head1 INCOMPATIBILITIES

None known.

=head1 DIAGNOSTICS

The debug() routine provided in this package is intended to set a debug level to control
the nature and intensity of messages returned to the caller from routines in this package.
See the calling script for the intended enumeration values that define the supported debug levels.

=head1 DEPENDENCIES

Some kinds of adjustments, especially in customer-modified copies of this
package, might require the use of such Perl packages as NetAddr::IP.
Some packages like that may be included in the standard GroundWork
Monitor distribution, and some may need to be added at the customer site.

=head1 AUTHOR

GroundWork Open Source, Inc. (http://www.gwos.com/)

=head1 VERSION

1.1.0

=cut


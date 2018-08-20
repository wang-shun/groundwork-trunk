package WesternGecoHostgroupName;

# This module classifies hostnames into hostgroup names, in service of
# auto-import.  For the WesternGeco implementation, we depend on the
# WesternGeco Omega2 System Naming Standard being followed, and we derive
# the hostgroup names accordingly.  This is equivalent to the output of
# "hosttype -c hostname", except that implementing it as a Perl module is
# far more efficient than forking a separate script for every call.

# Copyright 2007 GroundWork Open Source, Inc. ("GroundWork").  All rights
# reserved.  Use is subject to GroundWork commercial license terms.

use strict;

our $VERSION = '1.0.0';

my $print_function = 1;
my $append_cluster = 0;

my $debugging = 0;

# Simple constructor; takes no user-specified arguments.
sub new {
    my $invocant = $_[0];

    my $class = ref($invocant) || $invocant;	# object or class name
    bless {}, $class;
}

sub debug {
    shift;	# intentionally ignore invocant
    $debugging = shift if @_;
    return $debugging;
}

# Note that while WesternGeco's Omega2 System Naming Standard gives us an idea
# of what the machine does, it fails to provide definitive information about the
# hosts's operating system or other details.  If needed, such information must
# either be presumed or discovered through other means external to this module.

# The hostgroup_name routine encapsulates the rules for decomposing a WesternGeco hostname
# and deriving its associate hosttype.  The list of possible hostname constructions is:
#
#            Machine Type   Hostname Format                                                     Examples
# --------------------------------------------------------------------------------------------------------------------------------
# engineering workstation:  <site(char2)><index(int3)>						kl148    gw014    ho050
# engineering workstation:  <site(char2)><index(alpha,int2)>					sts07
#      clustered computer:  <site(char2)><cluster(char1)><index(int4)>				hoj0047  gwm0436  klc0128
#             site server:  <site(char2)><function(char2)><index(int3)>				gwns003  peos001  hots002  stjs001
#   cluster server/device:  <site(char2)><function(char2)><cluster(char1)><index(int3)>		homgz011 hoswr035
#
# The emitted codes for these machines will be:
#
#            Machine Type   base hosttype	extended hosttype (includes cluster ID)
# -------------------------------------------------------------------------------------
# engineering workstation:  workstation		workstation
#      clustered computer:  compute		compute-{cluster}
#             site server:  server-{function}	server-{function}
#   cluster server/device:  server-{function}	server-{function}-{cluster}
#
# Anything else will be simply labeled as "invalid".

# Possible options:
# -f means suppress the function portion of the server host types,
#    leaving only 'workstation', 'compute', or 'server'
# -c means append the cluster ID, where available, to the hostgroup name
# Specify whatever options you wish to invoke as a single string, such as "-f -c".

sub initialize_hostgroup_options
    {
    # my $self		= $_[0];
    my $options		= $_[1];
    # my $debug_config	= $_[2];	# not needed here, but available

    for (my @opts = split(" ", $options); $#opts >= 0; shift @opts)
	{
	$_ = $opts[0];
	if (/^-f$/) { $print_function = 0;     next; }
	if (/^-c$/) { $append_cluster = 1;     next; }
	die "Unrecognized hostgroup naming option '$_'.";
	}
    }

# Analyze a single hostname and return the result.
# It is okay for a hostname to be suffixed with a -interface and/or
# a .domainname extension; such pieces will be handled correctly.

sub hostgroup_name
    {
    # my $self   = $_[0];
    my $hostname = $_[1];

    # Drop any -interface and .domainname components of the full hostname
    # (host-interface.domain) before matching proceeds.
    $hostname =~ s/[-.].*//;

    # The ordering of tests here is optimized for the most common cases first.
    if ($hostname =~ /^[a-z]{2}([a-z])\d{4}$/io)
        {
	return ($append_cluster ? "compute-$1" : "compute");
	}
    elsif ($hostname =~ /^[a-z]{2}([a-z]{2})\d{3}$/io)
	{
	return ($print_function ? "server-$1" : "server");
	}
    elsif ($hostname =~ /^[a-z]{2}([a-z]{2})([a-z])\d{3}$/io)
        {
	return ($append_cluster ? ($print_function ? "server-$1-$2" : "server-$2") : ($print_function ? "server-$1" : "server"));
	}
    elsif ($hostname =~ /^[a-z]{2}[a-z0-9]\d{2}$/io)
	{
	return ("workstation");
	}
    else
        {
	return ("invalid");
	}
    }

1;

__END__

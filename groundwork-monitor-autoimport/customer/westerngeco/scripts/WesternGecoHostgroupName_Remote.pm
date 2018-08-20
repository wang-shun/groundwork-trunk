package WesternGecoHostgroupName_Remote;

# This module classifies hostnames into hostgroup names, in service of
# auto-import.  For the WesternGeco implementation, we depend on the
# WesternGeco Omega2 System Naming Standard being followed, and we derive
# the hostgroup names accordingly.  This is equivalent to the output of
# "hosttype -c hostname", except that implementing it as a Perl module is
# far more efficient than forking a separate script for every call.

# This version of the module supports optional prepending of a readable site
# location (generally, the Omega Global Site Name) to the hostgroup name,
# if the site can be determined by matching the hostname prefix against
# a preconfigured set of site codes.  This capability is enabled by the
# -l (location) option.  Currently, it has a secondary effect of somewhat
# scrambling the meanings of the -f and -c options when a site location is
# found, in a way that is not well explained and certainly does not preserve
# orthogonality of option meanings.  An actual design specification, and
# some further evolution of the option structure, is needed to clarify the
# intent and straighten out this mess.  Leaving out the -l option retains
# the original meanings of the -f and -c options.
#
# In addition, certain extensions to the WesternGeco Omega2 System Naming
# Standard are optionally supported in this module:
#
# * recognition of a digit as the second character of some hostnames,
#   to support the infrastructure of certain field crews
# * recognition of certain additional hostname patterns for site servers
#   and engineering workstations (to support machines on certain vessels,
#   using an older naming schema)
#
# The digit-as-second-character extension is only enabled by the -d (digit)
# option.  Similarly, the additional hostname patterns are only enabled by
# the -o (old schema) option.  This way, the enabling of these extensions
# can be controlled via the custom_hostgroup_package_options option in the
# autoimport.conf file at each site.

# Copyright 2007, 2011 GroundWork Open Source, Inc. ("GroundWork").  All rights
# reserved.  Use is subject to GroundWork commercial license terms.

use strict;

our $VERSION = '2.0.0';

my $print_function   = 1;
my $append_cluster   = 0;
my $prepend_location = 0;
my $digit_as_char_2  = 0;
my $old_name_schema  = 0;

my $site_file = '/usr/local/groundwork/config/Site_Code_Name.conf';

my %fixed_site_location   = ();
my %pattern_site_location = ();

my $debug_level   = 0;
my $DEBUG_NONE    = 1;  # turn off all debug info
my $DEBUG_FATAL   = 0;  # the application is about to die
my $DEBUG_ERROR   = 0;  # the application has found a serious problem, but will attempt to recover
my $DEBUG_WARNING = 0;  # the application has found an anomaly, but will try to handle it
my $DEBUG_NOTICE  = 0;  # the application wants to inform you of a significant event
my $DEBUG_STATS   = 0;  # the application wants to log statistical data for later analysis
my $DEBUG_INFO    = 0;  # the application wants to log a potentially interesting event
my $DEBUG_DEBUG   = 0;  # the application wants to log detailed debugging data

# Simple constructor; takes no user-specified arguments.
sub new {
    my $invocant = $_[0];

    my $class = ref($invocant) || $invocant;    # object or class name
    bless {}, $class;
}

sub debug {
    shift;    # intentionally ignore invocant
    if (@_) {
	$debug_level   = shift;
	$DEBUG_NONE    = $debug_level == 0;
	$DEBUG_FATAL   = $debug_level >= 1;
	$DEBUG_ERROR   = $debug_level >= 2;
	$DEBUG_WARNING = $debug_level >= 3;
	$DEBUG_NOTICE  = $debug_level >= 4;
	$DEBUG_STATS   = $debug_level >= 5;
	$DEBUG_INFO    = $debug_level >= 6;
	$DEBUG_DEBUG   = $debug_level >= 7;
    }
    return $debug_level;
}

# Note that while WesternGeco's Omega2 System Naming Standard gives us an idea
# of what the machine does, it fails to provide definitive information about the
# hosts's operating system or other details.  If needed, such information must
# either be presumed or discovered through other means external to this module.

# The hostgroup_name routine encapsulates the rules for decomposing a WesternGeco hostname
# and deriving its associate hosttype.  The list of possible hostname constructions is:
#
#            Machine Type   Hostname Format                                             Examples
# ------------------------------------------------------------------------------------------------------------------------
# engineering workstation:  <site(char2)><index(int3)>					kl148    gw014    ho050
# engineering workstation:  <site(char2)><index(alpha1,int2)>				sts07
#      clustered computer:  <site(char2)><cluster(char1)><index(int4)>			hoj0047  gwm0436  klc0128
#             site server:  <site(char2)><function(char2)><index(int3)>			gwns003  peos001  hots002  stjs001
#   cluster server/device:  <site(char2)><function(char2)><cluster(char1)><index(int3)>	homgz011 hoswr035
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
# The standard pattern matching in this module assumes that in the hostname formats listed above,
# <site(char2)> really means <site(alpha2)>, because the only site codes defined at the time of
# the module's original development were entirely alphabetic.  There is nothing in the Omega2
# System Naming Standard that explicitly demands this restriction, though, so the -d option
# extends the site pattern matching to include <site(alpha,int)>.
#
# Additional hostname patterns are optionally recognized under an older naming schema:
#
#            Machine Type   Hostname Format                                             Examples
# -----------------------------------------------------------------------------------------------------------------------------
# engineering workstation:  <site(char2)><type(alpha2)><index(int1)>			clqc2   [QC workstation]
#             site server:  <site(char2)><function(char2)><type(alpha2)><index(int1)>	clsvqc1 [QC wg application server (js)]
#											clsvqc2 [QC oracle server (os)]
#             site server:  <site(char2)><function(char2)><type(alpha3)>	        clbasmu [BlueArc SMU for management]
#
# The true function for these additional site server hostname patterns is actually determined
# by a combination of the <function> code and the rest of the hostname, but the classification
# below does not attempt to apply this finer level of distinction.
#
# Anything else will be simply labeled as "invalid".

# Possible options:
#
# "-f"                    means suppress the function portion of the server host types,
#                         leaving only 'workstation', 'compute', or 'server'
# "-c"                    means append the cluster ID, where available, to the hostgroup name
# "-l"                    means prepend site locations to hostgroups whenever possible
# "-d"                    means allow a digit as the second character of a hostname
# "-o"                    means support certain older host naming schemas (e.g., for vessels)
# "-s /path/to/site_file" specifies an alternate file containing hostname-prefix location mappings
#                         (an absolute pathname will be required); only used if -l is specified
#
# Specify whatever options you wish to invoke as a single string, such as "-f -c".

sub initialize_hostgroup_options {
    ## my $self	= $_[0];
    my $options = $_[1];
    ## my $debug_config	= $_[2];	# not needed here, but available

    for ( my @opts = split( " ", $options ) ; $#opts >= 0 ; shift @opts ) {
	$_ = $opts[0];
	if (/^-f$/) { $print_function   = 0; next; }
	if (/^-c$/) { $append_cluster   = 1; next; }
	if (/^-l$/) { $prepend_location = 1; next; }
	if (/^-d$/) { $digit_as_char_2  = 1; next; }
	if (/^-o$/) { $old_name_schema  = 1; next; }
	if (/^-s$/ && defined( $opts[1] ) && $opts[1] =~ m{^/}) {
	    $site_file = $opts[1];
	    shift @opts;
	    next;
	}
	die "Unrecognized hostgroup naming option '$_'.";
    }

    if ($prepend_location) {
	# Open Site Location lookup table.
	open SITE_LOCATION, '<', $site_file
	  or die "ERROR: CANNOT OPEN \"$site_file\" FILE ($!)\n";

	# Loop through the file and capture its mappings.
	my @split_line = ();
	while (<SITE_LOCATION>) {
	    next if /^\s*#/;  # skip comment lines
	    next if /^\s*$/;  # skip blank lines
	    @split_line = split;
	    if (@split_line >= 2) {
		if ($split_line[0] =~ /^[a-z][a-z0-9]$/) {
		    $fixed_site_location{ $split_line[0] } = $split_line[1];
		    print "$split_line[0] => $split_line[1]\n" if $DEBUG_DEBUG;
		}
		elsif (length $split_line[0] > 1) {
		    # We compile the pattern now, to prevent any reinterpretation later when we substitute
		    # it into an eval string.  But first, we kill any copies of the delimiters we use later,
		    # along with other characters that might also later cause problems in the pattern.
		    $split_line[0] =~ tr/<>@$//d;
		    $split_line[0] = qr{$split_line[0]};
		    # The replacement *does* need to be reinterpreted later, to substitute $1 references.
		    # We drop some illegal characters in the replacement that might cause problems later on.
		    # More characters are illegal from the standpoint of a hostname, but these are the ones
		    # that are most likely to cause problems at the Perl-language level.  We allow backslash
		    # translation escapes to change case in replacement strings, but no other backslashes.
		    # We also suppress "$" characters that aren't followed by either a bare digit or a digit
		    # enclosed in braces, so we only implement substitution of the backreferences we want to
		    # support, not access to named variables from within this script.
		    $split_line[1] =~ tr/@[]<>()//d;
		    $split_line[1] =~ s/\\(?![ULEul])//g;
		    $split_line[1] =~ s/\$(?!([0-9]|{[0-9]}))//g;
		    $pattern_site_location{ $split_line[0] } = $split_line[1];
		    print "$split_line[0] => $split_line[1]\n" if $DEBUG_DEBUG;
		}
		else {
		    print "ERROR: site code \"$split_line[0]\" is too short to match any hostnames.\n" if $DEBUG_ERROR;
		}
	    }
	    else {
		print "ERROR: site code \"$split_line[0]\" has no associated location defined.\n" if $DEBUG_ERROR;
	    }
	}
	close SITE_LOCATION;
    }
}

# Analyze a single hostname and return the result.
# It is okay for a hostname to be suffixed with a -interface and/or
# a .domainname extension; such pieces will be handled correctly.

sub hostgroup_name {
    ## my $self  = $_[0];
    my $hostname = $_[1];

    # Declare variables and assign default values.
    my $site_name = "";
    my $location  = "";

    # Drop any -interface and .domainname components of the full hostname
    # (host-interface.domain) before matching proceeds.
    $hostname =~ s/[-.].*//;

    # Get the first 2 characters in the hostname, country code, and assign to variable.
    my $hostname_prefix = substr( $hostname, 0, 2 );

    if ($prepend_location) {
	if ( defined $fixed_site_location{$hostname_prefix} ) {
	    $location = $fixed_site_location{$hostname_prefix} . '-';
	}
	else {
	    my $loc = $hostname_prefix;
	    # Patterns will be applied in an arbitrary order, so you'd best not have
	    # your patterns constructed such that two patterns might match the same
	    # hostname prefix, or you won't know which will be used.  Trying to sort
	    # the patterns wouldn't work well because they are regular expressions.
	    foreach my $pattern (keys %pattern_site_location) {
		# A string eval may be slow, but there doesn't seem to be any workable alternative.
		if ( eval "\$loc =~ s<^$pattern\$><$pattern_site_location{$pattern}>" ) {
		    $location = $loc . '-';
		    last;
		}
	    }
	}
    }

    # The ordering of tests here is more or less optimized for the most common cases first,
    # though with the profusion of options this becomes less effective.
    if (
	    # Look for clustered computers (compute nodes).
	    ( $hostname =~ /^[a-z]{2}([a-z])\d{4}$/io )
	    ||
	    ( $digit_as_char_2 && $hostname =~ /^[a-z][0-9]([a-z])\d{4}$/io )
	) {
	## If a location was found in the site look-up file, then use the remote site definition.
	## If you comment out a site or a site is missing from the file, then it will instead be
	## defined with a standard GW host group name.  This is used in Denver for the first time
	## to mark their systems as standard GW and other sites as remote.
	return ( $prepend_location && $location )
	    ? ( $append_cluster ? $location . "compute" : "compute" )
	    : ( $append_cluster ? "compute-$1" : "compute" );
    }
    elsif (
	    # Look for site servers.
	    ( $hostname =~ /^[a-z]{2}([a-z]{2})\d{3}$/io )
	    ||
	    ( $old_name_schema && $hostname =~ /^[a-z]{2}([a-z]{2})[a-z]{2}[a-z\d]$/io )
	    ||
	    ( $digit_as_char_2 && $hostname =~ /^[a-z][0-9]([a-z]{2})\d{3}$/io )
	    ||
	    ( $digit_as_char_2 && $old_name_schema && $hostname =~ /^[a-z][0-9]([a-z]{2})[a-z]{2}[a-z\d]$/io )
	) {
	return ( $prepend_location && $location )
	    ? ( $print_function ? $location . "server" : "server" )
	    : ( $print_function ? "server-$1" : "server" );
    }
    elsif (
	    # Look for cluster servers.
	    ( $hostname =~ /^[a-z]{2}([a-z]{2})([a-z])\d{3}$/io )
	    ||
	    ( $digit_as_char_2 && $hostname =~ /^[a-z][0-9]([a-z]{2})([a-z])\d{3}$/io )
	) {
	return ( $prepend_location && $location )
	    ? ( $append_cluster
		? ( $print_function ? $location . "server-$2" : "server-$2" )
		: ( $print_function ? $location . "server" : "server" ) )
	    : ( $append_cluster
		? ( $print_function ? "server-$1-$2" : "server-$2" )
		: ( $print_function ? "server-$1" : "server" ) );
    }
    elsif (
	    # Look for engineering workstations.
	    ( $hostname =~ /^[a-z]{2}[a-z0-9]\d{2}$/io )
	    ||
	    ( $old_name_schema && $hostname =~ /^[a-z]{2}[a-z]{2}\d$/io )
	    ||
	    ( $digit_as_char_2 && $hostname =~ /^[a-z][0-9][a-z0-9]\d{2}$/io )
	    ||
	    ( $digit_as_char_2 && $old_name_schema && $hostname =~ /^[a-z][0-9][a-z]{2}\d$/io )
	) {
	return ( $prepend_location && $location )
	    ? ( $print_function ? $location . "workstation" : "workstation" )
	    : ( "workstation" );
    }
    else {
	return ("invalid");
    }
}

1;

__END__

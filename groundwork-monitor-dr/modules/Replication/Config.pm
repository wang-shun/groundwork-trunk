package Replication::Config;

# Handle configuration in a GroundWork Monitor Disaster Recovery deployment.
# Copyright (c) 2010 GroundWork Open Source (www.groundworkopensource.com).
# All rights reserved.  Use is subject to GroundWork commercial license terms.

# ================================================================
# Documentation.
# ================================================================

# To do:
# (*) Report a YAML::XS bug:  LoadFile and DumpFile are not mentioned in
#     the YAML::XS documentation as available functions.
# (*) Report a YAML::XS bug:  its documentation should refer also to the
#     "boolean" module, so a Perl programmer knows how to get a boolean
#     value properly tagged when dumping for reading by other languages

# ================================================================
# Perl setup.
# ================================================================

use strict;
use warnings;

require Exporter;
our @ISA = ('Exporter');

our @EXPORT = qw(
    &load_configuration
);

# The configuration is supposed to be always only edited by hand,
# and never overwritten by machine (which would lose all the valuable
# comments in the file).  So save_configuration() should never be
# imported into user programs.  It exists only for test purposes,
# and even then it is dangerous!
our @EXPORT_OK = qw(
    &save_configuration
    &log_configuration
);

# This is where we'll pick up any Perl packages not in the standard Perl
# distribution, to make this a self-contained package anchored in a single
# directory.
use FindBin qw($Bin);
use lib "$Bin/perl/lib";

# use IO::Handle;
use POSIX;
use YAML::XS qw(LoadFile);
use boolean;
use Replication::Logger;

# Be sure to update this as changes are made to this module!
my $VERSION = '0.1.0';

# ================================================================
# Working variables.
# ================================================================

my $config_file = undef;

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

my $config = undef;

# ================================================================
# Supporting subroutines.
# ================================================================

# The new() constructor must be invoked as:
#     my $config = Replicatioin::Config->new ($config_file, $debug_config);
# because if it is invoked instead as:
#     my $config = Replicatioin::Config::new ($config_file, $debug_config);
# no invocant is supplied as the implicit first argument.
# The same comments apply to the secure_new() constructor.

sub new {
    my $invocant = $_[0];   # implicit argument
    $config_file = $_[1];   # required argument

    my $class = ref($invocant) || $invocant;    # object or class name
    # Options are stored in our object hash to prepare for the day when
    # we allow more than one such object in the program.  These copies
    # are not yet referenced later on, though.
    my $self = {
	# config_file => $config_file
    };
    bless $self, $class;
    return $self;
}

# This constructor is just like new(), but it insists that the configuration file
# be readable/writable only by the owner.  This form should be used in cases where
# the configuration file is known to contain certain sensitive data, such as user 
# credentials for database access.  In such cases, dying if the file is visible 
# to anyone other than the owner is a simple way to force the local administrator
# to maintain proper security controls on the file.  (But beware, this is not a
# test of complete security; this doesn't check Access Control Lists (ACLs), which
# are a much more sophisticated form of access control.)
sub secure_new {
    # my $invocant     = $_[0]; # implicit argument
    my $config_file  = $_[1];   # required argument

    my ($dev, $ino, $mode) = stat $config_file;
    die "FATAL:  cannot access config file \"$config_file\"\n" if ! defined $mode;
    die "FATAL:  config file \"$config_file\" has permissions beyond r/w to owner\n" if $mode & 0177; 

    return new(@_);
}

sub load_configuration {
    eval {
	$config = YAML::XS::LoadFile($config_file);
    };
    if ($@) {
	if ($! == ENOENT) {
	    spill_message "WARNING:  configuration file $config_file"
	      . " does not exist; loading default configuration instead";
	    return undef;
	}
	else {
	    chomp $@;
	    spill_message 'ERROR:  ', $@;
	    return undef;
	}
    }

    return $config;
}

# FIX THIS:  distinguish between being called as an exported function
# and being called as a class method (the latter may give us an extra
# first argument we will need to recognize and ignore) 
sub save_configuration {
    my $saved_config_file = $_[0];              # required argument
    my $saved_config      = $_[1] || $config;   # optional argument

    my $temporary_config_file = $saved_config_file . '.tmp';
    eval {
	# Note:
	# A false value here serializes as: !!perl/scalar:boolean 0
	# A true  value here serializes as: !!perl/scalar:boolean 1
	# Hopefully the type tags here, while they appear to be language-specific,
	# will be properly understood by other language interpreters upon import.
	# Too bad the values cannot be specially handled by the YAML dumper or
	# emitter and serialized instead as simple and obvious yes and no values.

	# FIX THIS:  is there some other way to get the dump to tag such values
	# with !!bool so they do in fact show up that way?

	YAML::XS::DumpFile( $temporary_config_file, $saved_config );
    };
    if ($@) {
	chomp $@;
        log_timed_message "ERROR:  $@";
	return 0;
    }
    else {
	# Only now that the config file is completely written is it appropriate
	# to slide it into place where other applications can read it.
	if (!rename( $temporary_config_file, $saved_config_file )) {
	    log_timed_message "ERROR:  $!";
	    return 0;
	}
    }
    return 1;
}

# Internal routine for debugging; not expected to be for general use.
sub log_configuration {
    foreach my $key (sort keys %$config) {
        log_message "$key => $config->{$key}";
    }
}

1;

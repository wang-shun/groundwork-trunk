package TypedConfig;

#
# $Id: $
#

use strict;
use warnings;

use Safe;
use Config::General qw(ParseConfig);

my $VERSION = "1.0.1";

#######################################################
#
#   Configuration File Handling
#
#######################################################

# The new() constructor must be invoked as:
#     my $config = TypedConfig->new ($config_file, $debug_config);
# because if it is invoked instead as:
#     my $config = TypedConfig::new ($config_file, $debug_config);
# no invocant is supplied as the implicit first argument.
# The same comments apply to the secure_new() constructor.

sub new {
    my $invocant     = $_[0];	# implicit argument
    my $config_file  = $_[1];	# required argument
    my $debug_config = $_[2];	# optional argument
    my $class = ref($invocant) || $invocant;    # object or class name

    die "ERROR:  cannot access config file \"$config_file\"\n" if ! -f "$config_file" || ! -r "$config_file";

    my %config = ParseConfig
	(
	-ConfigFile => $config_file,
	-InterPolateVars => 1,
	-AutoTrue => 1,
	-SplitPolicy => 'equalsign'
	);

    # A debug_config value given in the new() function call overrides anything read from the config file.
    $config{"debug_config"} = $debug_config if defined $debug_config;

    bless \%config, $class; 
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
    # my $invocant     = $_[0];	# implicit argument
    my $config_file  = $_[1];	# required argument
    # my $debug_config = $_[2];	# optional argument

    my ($dev, $ino, $mode) = stat $config_file;
    die "ERROR:  cannot access config file \"$config_file\"\n" if ! defined $mode;
    die "ERROR:  config file \"$config_file\" has permissions beyond r/w to owner\n" if $mode & 0177;

    return new(@_);
}

# We explicitly disallow undefined config values here.
# This is a conscious choice, so we can perform uniform controlled error checking.
sub get_scalar {
    my $config = $_[0];	# implicit argument
    my $name   = $_[1];	# required argument
    my $value  = $config->{$name};

    die "ERROR:  cannot find a config-file value for '$name'\n" if ! defined $value;
    # We need to take a reference to a scalar value so ref() will return its type.
    die "ERROR:  '$name' is not a scalar value; check for multiple definitions\n" if ref(\$value) ne "SCALAR";

    print "name = $name, value = $value\n" if $config->{"debug_config"};
    return $value;
}

# We explicitly disallow undefined config values here.
# This is a conscious choice, so we can perform uniform controlled error checking.
sub get_boolean {
    my $config = $_[0];	# implicit argument
    my $name   = $_[1];	# required argument
    my $value  = $config->{$name};

    die "ERROR:  cannot find a config-file value for '$name'\n" if ! defined $value;
    # We need to take a reference to a scalar value so ref() will return its type.
    die "ERROR:  '$name' is not a boolean value; check for multiple definitions\n" if ref(\$value) ne "SCALAR";
    die "ERROR:  invalid boolean value for '$name'\n" if $value !~ /^[01]$/o;

    print "name = $name, value = $value\n" if $config->{"debug_config"};
    return $value;
}

# We explicitly disallow undefined config values here.
# This is a conscious choice, so we can perform uniform controlled error checking.
sub get_number {
    my $config     = $_[0];	# implicit argument
    my $name       = $_[1];	# required argument
    my $expression = $config->{$name};
    my $sandbox    = Safe->new();

    die "ERROR:  cannot find a config-file expression for '$name'\n" if ! defined $expression;
    # We need to take a reference to a scalar value so ref() will return its type.
    die "ERROR:  '$name' is not a numeric value; check for multiple definitions\n" if ref(\$expression) ne "SCALAR";
    # We construct our pattern for a valid expression rather carefully, but the fact is that we cannot totally
    # prevent some badly formed expressions from yielding a numeric result that we don't catch as bad.
    die "ERROR:  invalid numeric expression '$expression' for '$name'\n" if $expression !~ m@^[-+()\d][-+*/%()\d\s]*$@o;

    my $value = $sandbox->reval ("scalar $expression", 1);
    die "ERROR:  invalid numeric expression '$expression' for '$name'\n" if ! defined $value;

    print "name = $name, value = $value\n" if $config->{"debug_config"};
    return $value;
}

# In this case, we handle errors by returning an undefined hash.
sub get_hash {
    my $config = $_[0];	# implicit argument
    my $name   = $_[1];	# required argument
    my $value  = $config->{$name};

    # We don't need to take a reference to a hash value in the ref() call,
    # because if it's a hash then what we have in hand is already a reference.
    if (ref($value) eq "HASH") {
	return %{$value};
    } else {
	return undef;
    }
}

# In this case, we may return an empty array, without complaining.
sub get_array {
    my $config = $_[0];	# implicit argument
    my $name   = $_[1];	# required argument
    my $value  = $config->{$name};

    # We don't need to take a reference to an array value in the ref() call,
    # because if it's an array then what we have in hand is already a reference.
    if (ref($value) eq "ARRAY") {
	# Return an incoming array as an array without an extra level of array-ness.
	return @{$value};
    } elsif (defined($value)) {
	# Turn a scalar value into a returned single-element array.
	return ($value);
    } else {
	# Return an empty array.
	return ();
    }
}

1;

__END__

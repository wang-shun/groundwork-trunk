################################################################################
#
# GDMA::AutoSetup
#
# This library contains routines that support GDMA Auto-Setup.  It supports not
# just general client-side actions, but also similar tasks of the autosetup tool
# and the registration script on the server.
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

package GDMA::AutoSetup;

use strict;
use warnings;

use Exporter;
use Safe;
use Log::Log4perl;
use Clone qw(clone);
use Regexp::Common qw(net);
use NetAddr::IP::Lite;
use Text::Wrap;

use Data::Dumper;
$Data::Dumper::Indent   = 1;
$Data::Dumper::Sortkeys = 1;

use Config::General qw(ParseConfig);

# ================================
# Package Parameters
# ================================

our @ISA = ('Exporter');
our $VERSION = '0.8.1';

# ================================
# Package Variables
# ================================

my $config_debug = 1;

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
my %is_required_trigger_option = (
    last_step            => 1,
    if_duplicate         => 1,
    soft_error_reporting => 1,
    change_policy        => 0,
);

my %is_valid_sensor_type = (
    os_type                => 1,
    os_version             => 1,
    os_bitwidth            => 1,
    machine_architecture   => 1,
    file_name              => 1,
    symlink_name           => 1,
    directory_name         => 1,
    mounted_filesystem     => 1,
    file_content           => 1,
    running_system_service => 1,
    full_process_command   => 1,
    open_local_port        => 1,
    open_named_socket      => 1,
);

my %is_valid_sensor_cardinality = ( single => 1, first => 1, multiple => 1 );

# ================================
# Package Routines
# ================================

# Package constructor.
# FIX MAJOR:  deal with the following possible options (perhaps just delete mention of them):
#      trigger_dir (possibly, manufactored by the constructor)
# instructions_dir (possibly, manufactured by the constructor)
sub new {
    my $invocant = $_[0];
    my $options  = $_[1];                          # hashref; options like base directory
    my $class    = ref($invocant) || $invocant;    # object or class name

    # FIX MAJOR:  how do we expect get_logger() to find an appropriate logger?
    my $logger = ( defined($options) ? $options->{logger} : undef ) || Log::Log4perl::get_logger("GDMA");

    # FIX MAJOR:  verify that all of the required options are in fact supplied
    my $max_slurp_size   = 1_000_000;
    my $trigger_dir      = "FIX MAJOR";
    my $instructions_dir = "FIX MAJOR";
    my %config           = (
	hostname         => $options->{hostname},
	enabled          => $options->{enabled} ? 1 : 0,
	logger           => $logger,
	max_slurp_size   => $max_slurp_size,
	trigger_dir      => $trigger_dir,
	instructions_dir => $instructions_dir
    );

    my $self = bless \%config, $class;

    return $self;
}

# use specified trigger options + defaults
# on success, return file content
# on failure, log the problem and return undef
sub make_trigger_content {
    my $self        = shift;
    my $options     = shift;    # hashref
    my $filecontent = '';

    # We list the possible trigger options explicitly instead of using "keys %is_valid_trigger_option",
    # to place them into a canonical order within the generated trigger file.  That forced ordering has
    # no effect on downstream use of the trigger file, but the consistency will make it easier for humans
    # to read and process the file.
    foreach my $directive (qw( last_step if_duplicate soft_error_reporting change_policy )) {
	if ( defined( $options->{$directive} ) ) {
	    if ( $is_valid_trigger_option{$directive}{ $options->{$directive} } ) {
		$filecontent .= "$directive = $options->{$directive}\n";
	    }
	    else {
		$self->{logger}->error("ERROR:  Invalid $directive option found in call to make_trigger_content().");
		$filecontent = undef;
		last;
	    }
	}
	elsif ( $is_required_trigger_option{$directive} ) {
	    $self->{logger}->error("ERROR:  Missing $directive option in call to make_trigger_content().");
	    $filecontent = undef;
	    last;
	}
    }

    return $filecontent;
}

# internal routine, following atomic protocol
# This routine takes an absolute pathname.
sub install_file {
    my $self        = shift;
    my $filecontent = shift;
    my $source_path = shift;
    my $target_path = shift;
    my $outcome     = 1;       # start with a positive attitude

    # We restrict the installed file's permissions as a matter of general security.
    # Unless we have good reason to share a file with other users, we don't allow
    # any other user to even read the file.  This provides blanket protection in
    # case somebody accidentally sticks sensitive info in a file, such an action
    # being omething we cannot predict here in advance.
    #
    # FIX LATER:  It is unclear what effect this might have on a Windows platform;
    # is there an emulation in place?  That will need separate testing.
    #
    my $old_umask = umask 0077;

    if ( not open TEMPFILE, '>', $source_path ) {
	$self->{logger}->error("ERROR:  Cannot open $source_path ($!).");
	$outcome = 0;
    }
    else {
	if ( not print TEMPFILE $filecontent ) {
	    $self->{logger}->error("ERROR:  Cannot print to $source_path ($!).");
	    $outcome = 0;
	}
	if ( not close(TEMPFILE) and $outcome ) {
	    $self->{logger}->error("ERROR:  Cannot close $source_path ($!).");
	    $outcome = 0;
	}
	if ( $outcome and not rename $source_path, $target_path ) {
	    my $os_error = "$!";
	    $os_error .= " ($^E)" if "$^E" ne "$!";
	    $self->{logger}->error("ERROR:  Cannot rename $source_path to $target_path ($os_error).");
	    $outcome = 0;
	}
    }

    umask $old_umask;
    return $outcome;
}

# implement server-side atomic protocol
sub install_trigger_file {
    my $self        = shift;
    my $filecontent = shift;
    my $filename    = shift;
    my $trigger_dir = $self->{trigger_dir};
    return $self->install_file( $filecontent, "$trigger_dir/$filename.tmp", "$trigger_dir/$filename" );
}

# implement server-side atomic protocol
sub install_instructions_file {
    my $self             = shift;
    my $filecontent      = shift;
    my $filename         = shift;
    my $instructions_dir = $self->{instructions_dir};
    return $self->install_file( $filecontent, "$instructions_dir/$filename.tmp", "$instructions_dir/$filename" );
}

# FIX MAJOR:  why do we have this routine?
sub copy_trigger_file {
    my $self            = shift;
    my $source_path     = shift;
    my $target_filename = shift;
    my $outcome         = 0;
    return $outcome;
}

# FIX MAJOR:  Protect against extremely large input files blowing up the memory size of this process.
# Either implement some sort of size limit, checked before reading the file, or mmap the file instead
# of slurping it in and refer to the mapped memory instead of a string allocated in heap memory.
# Look at File::Map for this purpose; we will need to add it to our GDMA Perl distributions, and
# ultimately we should add it to our GWMEE Perl distribution.  But also if we do this, figure out
# how we should pass $filecontent to subsidiary functions; should that be done only by reference?
#
# copy customer instructions to our location
sub copy_instructions_file {
    my $self            = shift;
    my $source_path     = shift;
    my $target_filename = shift;
    my $filecontent     = undef;

    if ( not open CUSTOMERFILE, '<', $source_path ) {
	$self->{logger}->error("ERROR:  Cannot open $source_path ($!).");
	return 0;
    }

    do {
	## Slurp the whole file, up to a limit.
	local $/ = \$self->{max_slurp_size};
	$filecontent = readline CUSTOMERFILE;
    };

    if ( not defined $filecontent ) {
	$self->{logger}->error("ERROR:  Cannot read $source_path ($!).");
	close CUSTOMERFILE;
	return 0;
    }

    if ( not close CUSTOMERFILE ) {
	$self->{logger}->error("ERROR:  Cannot close $source_path ($!).");
	return 0;
    }

    my $len = do { use bytes; length $filecontent; };
    if ( $len >= $self->{max_slurp_size} ) {
	$self->{logger}->error("ERROR:  The instructions file is too large to process.");
	return 0;
    }

    ## FIX MINOR:  possibly validate the customer instructions before installing them,
    ## as a general safety measure
    return $self->install_instructions_file( $filecontent, $target_filename );
}

# fetch trigger or instructions from server
sub fetch_discovery_file {
    my $self     = shift;
    my $filetype = shift;
    my $outcome  = 0;       # start out pessimistic; only report success if we get all the way through
    if ( $filetype eq 'trigger' ) {
	## FIX MAJOR
    }
    elsif ( $filetype eq 'instructions' ) {
	## FIX MAJOR
    }
    else {
	$self->{logger}->error("ERROR:  Unsupported filetype \"$filetype\" passed to fetch_discovery_file().");
	return $outcome;
    }

    # FIX MAJOR:  figure out the proper local directory in which to deposit the file
    # FIX MAJOR:  mirror the remote file to a temporary local file
    # FIX MAJOR:  make sure the timestamp on the temporary local file matches that on the remote file
    # FIX MAJOR:  atomically rename the temporary local file to its final name
    return $outcome;
}

# reflect what was supposedly true when the object was created
sub is_autosetup_enabled {
    my $self = shift;
    return $self->{enabled};
}

# find a trigger, instructions, or results file
sub find_discovery_file {
    my $self     = shift;
    my $filetype = shift;
    my $filepath = '';
    if ( $filetype eq 'trigger' ) {
	## FIX MAJOR
    }
    elsif ( $filetype eq 'instructions' ) {
	## FIX MAJOR
    }
    else {
	$self->{logger}->error("ERROR:  Unsupported filetype \"$filetype\" passed to find_discovery_file().");
	return undef;
    }

    # FIX MAJOR:
    return $filepath;
}

# The instructions file is a complex data structure.  We want to parse it by automated means as
# much as possible, forming a corresponding Perl structure without needing to manually code every
# individual nuance.  To make that possible, we define the overall structure of the file.  This
# should make it possible not only to look for the expected elements, but also to verify that no
# other elements exist due to spelling errors or other mistakes.

# How many of these elements must appear, at least?
use constant OPTIONAL  => 0;
use constant MANDATORY => 1;

# How many of these elements can appear, at most?
use constant SINGLE   => 1;
use constant MULTIPLE => 2;

# What is the datatype of this element?
use constant CONTAINER => 0;
use constant BOOLEAN   => 1;
use constant STRING    => 2;

my $instructions_structure = {
    "format_version" => [ MANDATORY, SINGLE, STRING ],
    "host"           => [
	MANDATORY, MULTIPLE, CONTAINER,
	{
	    "type"            => [ MANDATORY, SINGLE,   STRING ],
	    "resource"        => [ OPTIONAL,  SINGLE,   STRING ],
	    "cardinality"     => [ OPTIONAL,  SINGLE,   STRING ],
	    "pattern"         => [ MANDATORY, SINGLE,   STRING ],
	    "transliteration" => [ OPTIONAL,  MULTIPLE, STRING ],
	    "sanitization"    => [ OPTIONAL,  SINGLE,   STRING ],
	    "host_profile"    => [ MANDATORY, SINGLE,   STRING ],
	    "enabled"         => [ OPTIONAL,  SINGLE,   BOOLEAN ]
	}
    ],
    "service" => [
	OPTIONAL, MULTIPLE, CONTAINER,
	{
	    "type"                => [ MANDATORY, SINGLE,   STRING ],
	    "resource"            => [ OPTIONAL,  SINGLE,   STRING ],
	    "cardinality"         => [ OPTIONAL,  SINGLE,   STRING ],
	    "pattern"             => [ MANDATORY, SINGLE,   STRING ],
	    "transliteration"     => [ OPTIONAL,  MULTIPLE, STRING ],
	    "sanitization"        => [ OPTIONAL,  SINGLE,   STRING ],
	    "service_profile"     => [ OPTIONAL,  SINGLE,   STRING ],
	    "service"             => [ OPTIONAL,  SINGLE,   STRING ],
	    "check_command"       => [ OPTIONAL,  SINGLE,   STRING ],
	    "command_arguments"   => [ OPTIONAL,  SINGLE,   STRING ],
	    "externals_arguments" => [ OPTIONAL,  SINGLE,   STRING ],
	    "instance_suffix"     => [ OPTIONAL,  SINGLE,   STRING ],
	    "instance_cmd_args"   => [ OPTIONAL,  SINGLE,   STRING ],
	    "instance_ext_args"   => [ OPTIONAL,  SINGLE,   STRING ],
	    "enabled"             => [ OPTIONAL,  SINGLE,   BOOLEAN ]
	}
    ]
};

# read file, process the instructions into corresponding Perl structures
sub read_instructions_file {
    my $self                  = shift;
    my $instructions_filepath = shift;
    my $instructions          = undef;

    eval {
	## Attempt to ensure secure use of file permissions.
	##
	## Alas, the Windows file-permissions model is horribly, horribly complicated, and the "icacls"
	## command provided to manipulate those permissions does not even appear to always act as its own
	## help message says it should.  (With a design like this, it's no wonder that it's so easy for
	## an attacker on this platform to invade the filesystem unnoticed -- nobody every looks into or
	## tries to manage this level of complexity.)  Therefore, until such time as we figure out how to
	## conveniently set the file permissions to a sensible restricted form on this platform, we must
	## disable access-permission checking on this one platform.
	##
	## (That's not to say there isn't possibly a similar problem on other platforms.  Even Linux and
	## Solaris, at least, provide support for file-permission ACLs, which are not covered by a check
	## for just the file $mode.  But such ACLs are only rarely used on UNIX-like platforms, so we can
	## reasonably check for basic permissions.)
	##
	my ( $dev, $ino, $mode ) = stat $instructions_filepath;
	die "ERROR:  Cannot access instructions file \"$instructions_filepath\" ($!).\n" if not defined $mode;
	if ( $^O ne 'MSWin32' ) {
	    ## FIX MAJOR:  drop this message; it's only for debugging on Windows
	    # $self->{logger}->error( sprintf( "DEBUG:  Instructions file $instructions_filepath has permissions %04o.", $mode ) );
	    die "ERROR:  Instructions file \"$instructions_filepath\" has permissions beyond r/w to owner.\n" if $mode & 0177;
	}

	# Interpolation is problematic for this config file because macro references like $SANITIZED1$ will
	# look like something that Config::General should handle and attempt to substitute upon reading the
	# instructions file instead of being passed along unchanged.  We might also run into trouble with
	# dollar-sign characters in sensor pattern-directive regex's.  So we disable variable interpolation
	# within discovery instructions files.  We should, however, recommend that single-quotes be generally
	# used to enclose strings that include such macro references or match patterns.  That would make
	# it possible to enable interpolation in the future, because interpolation will only happen within
	# double-quoted strings, not within single-quoted strings.  Also, if we do enable interpolation, we
	# should always recommend the use of the ${foo} syntax instead of naked $foo references, to avoid the
	# problems we have seen with certain trailing punctuation like commas being interpreted as part of the
	# name of the configuration variable to be interpolated.
	my $interpolate = 0;

	# We could possibly have used our TypedConfig package for this parsing, but we had to play a lot
	# with the various Config::General options during development to settle on exactly what we needed
	# for this application.
	#
	# We disable recognizing C comments ( /* ... */ ) in the instructions file because the processing of
	# such comments is broken, at least in Config::General 2.63.  The problem is that some parts of that
	# processing can occur within a quoted option value, which makes no sense.  That interferes with our
	# potential use of those same characters in glob patterns such as this:
	#
	#     resource = "/usr/local/groundwork/*/cacti.log"
	#
	# To get around the broken code, which we have not yet tracked down in detail, we simply disallow
	# recognition of such comments within instructions files.  (The problem seems to be inappropriate
	# recogition of "*/" as ending a comment even when there was no preceding beginning-of-comment in
	# effect, and irrespective of the fact that this character pair was present in the middle of a
	# quoted option value which should not be subject to such comment processing anyway.)
	#
	my %config = ParseConfig(
	    -ConfigFile        => $instructions_filepath,
	    -InterPolateVars   => $interpolate,
	    -CComments         => 0,
	    -AutoTrue          => 1,
	    -SplitPolicy       => 'equalsign'
	);

	$instructions = $self->parse_instructions( $instructions_filepath, \%config, $instructions_structure, '(top-level file content)' );
	if ( not defined $instructions ) {
	    ## We definitely want to provide the user with as much information as possible about what went wrong,
	    ## so we print what the code saw.  But this ordering seems logically a bit backward:  we print the
	    ## config data here AFTER the error messages that would have appeared while parsing the instructions
	    ## file, as opposed to displaying the errors after the offending data.  Alas, since we don't know
	    ## before parsing whether any errors will appear and have no current means to queue error messages
	    ## so we can output them only after we print the config data, this is the best we can do for now.

	    ## FIX MINOR:  dump the config-file data to an appropriate data stream to see the full structure
	    print "\n";
	    print "================================================================\n";
	    print "Discovery instructions, as read but not fully parsed\n";
	    print "================================================================\n";
	    print "\n";
	    my $parsed_config = Data::Dumper->Dump( [ \%config ], [qw(\%config)] );
	    $parsed_config =~ s{\\\\}{\\}g;
	    print $parsed_config, "\n";
	}
	else {
	    $instructions = $self->impose_default_instructions($instructions);

	    # FIX MINOR:  dump the parsed instructions to an appropriate data stream to see the full structure
	    if (%$instructions) {
		if ($config_debug) {
		    print "================================================================\n";
		    print "Discovery instructions, as read and parsed\n";
		    print "================================================================\n";
		    print "\n";
		    my $parsed_instructions = Data::Dumper->Dump( [$instructions], [qw($instructions)] );
		    ##
		    ## It's mighty confusing to see doubled backslashes in this output simply because the
		    ## dump represents strings as they should be presented if the assignment in this string
		    ## was intended to be run through an eval{}; statement.  But we're not going to do that.
		    ## Instead, this output is strictly for human consumption, so we wish to present all
		    ## backslashes exactly as they appear in the text, to minimize confusion.  The only way
		    ## we presently know of that will not be so is when we have a single-quote character (')
		    ## in what will be in the output a single-quoted string.  In that case, the Dump routine
		    ## introduces a preceding backslash which is not in the actual string.  We expect single
		    ## quotes in the strings we deal with here to be rather rare; so for now, we will simply
		    ## live with that circumstance -- life isn't perfect.
		    ##
		    $parsed_instructions =~ s{\\\\}{\\}g;
		    print $parsed_instructions, "\n";
		}
	    }
	}
    };
    if ($@) {
	chomp $@;
	$@ =~ s/^ERROR:\s+//i;

	# FIX MINOR:  Suppress more than one line of diagnostics.  Enable this once we've seen in testing that
	# multi-line diagnostics really are of little use, mostly containing code coordinates which will mean
	# little to the end-user.
	my $trailing_part;
	if (0) {
	    ( $trailing_part = $@ ) =~ s/^[^\n]*(\n\s*)?//;
	    $@ =~ s/\n.*//;
	}

	$self->{logger}->error("ERROR:  Cannot read instructions file $instructions_filepath ($@).");
    }

    return $instructions;
}

# internal routine
sub impose_default_instructions {
    my $self         = shift;
    my $instructions = shift;

    # With the complete set of instructions in hand after all reading and parsing, we can look at
    # the instructions as a whole and make some adjustments that will make later processing easier.

    # We impose a default cardinality on all sensors for which no cardinality was explicitly defined.
    # This way, we don't have to test later on whether some value of the cardinality was supplied.

    my $host_sensors    = $instructions->{host};
    my $service_sensors = $instructions->{service};

    foreach my $tag ( keys %$host_sensors ) {
	my $sensor = $host_sensors->{$tag};
	$sensor->{cardinality} = 'single' if not defined $sensor->{cardinality};
    }

    foreach my $tag ( keys %$service_sensors ) {
	my $sensor = $service_sensors->{$tag};
	$sensor->{cardinality} = 'single' if not defined $sensor->{cardinality};
    }

    return $instructions;
}

# internal routine
sub parse_instructions {
    my $self                  = shift;
    my $instructions_filepath = shift;
    my $config                = shift;
    my $structure_part        = shift;
    my $context               = shift;
    my %instructions          = ();

    foreach my $ckey ( keys %$config ) {
	my $attributes = $structure_part->{$ckey};
	if ( not defined $attributes ) {
	    $self->{logger}->error(
		"ERROR:  In file \"$instructions_filepath\", found invalid construction of discovery instructions;\ncontext is:  $context.$ckey"
	    );
	    return undef;
	}
	my $required     = $attributes->[0];
	my $multiplicity = $attributes->[1];
	my $type         = $attributes->[2];
	my $subhash      = $attributes->[3];
	my $value        = $config->{$ckey};
	if ( $multiplicity == SINGLE ) {
	    if ( ref \$value ne 'SCALAR' ) {
		$self->{logger}
		  ->error("ERROR:  In file \"$instructions_filepath\", \"$ckey\" is not a single scalar value;\ncontext is:  $context.$ckey");
		return undef;
	    }
	    if ( $type == STRING ) {
		$instructions{$ckey} = $value;
	    }
	    elsif ( $type == BOOLEAN ) {
		if ( $value !~ /^[01]$/o ) {
		    $self->{logger}->error(
			"ERROR:  In file \"$instructions_filepath\", \"$ckey\" is not a valid boolean value;\ncontext is:  $context.$ckey");
		    return undef;
		}
		$instructions{$ckey} = $value;
	    }
	    elsif ( $type == CONTAINER ) {
		if (
		    not
		    defined( $instructions{$ckey} = $self->parse_instructions( $instructions_filepath, $value, $subhash, "$context.$ckey" ) ) )
		{
		    return undef;
		}
	    }
	}
	elsif ( $multiplicity == MULTIPLE ) {
	    ## FIX MAJOR:  test this construction and iteration
	    # We don't need to take a reference to an array value in the ref() call,
	    # because if it's an array then what we have in hand is already a reference.
	    if ( ref($value) eq 'ARRAY' ) {
		if ( $type == CONTAINER ) {
		    $self->{logger}->error( "ERROR:  In file \"$instructions_filepath\", found an array of \"$ckey\" objects"
			  . " (are you missing the tag in a <$ckey \"tag\"> object?);\ncontext is:  $context.<$ckey>" );
		    return undef;
		}
		## Return an incoming array as an array without an extra level of array-ness.
		my $i;
		for my $v (@$value) {
		    if ( ref($v) eq 'HASH' ) {
			if ( not defined( $i = $self->parse_instructions( $instructions_filepath, $v, $subhash, "$context.$ckey.$v" ) ) ) {
			    return undef;
			}
			push @{ $instructions{$ckey} }, $i;
		    }
		    else {
			push @{ $instructions{$ckey} }, $v;
		    }
		}
	    }
	    elsif ( ref($value) eq 'HASH' ) {
		my $i;
		foreach my $subkey ( keys %$value ) {
		    if ( ref( $value->{$subkey} ) eq 'ARRAY' ) {
			$self->{logger}->error( "ERROR:  In file \"$instructions_filepath\", <$ckey \"$subkey\"> is specified more than once;\n"
			      . "context is:  $context.<$ckey \"$subkey\">" );
			return undef;
		    }
		    if ( ref( $value->{$subkey} ) ne 'HASH' ) {
			$self->{logger}->error( "ERROR:  In file \"$instructions_filepath\", \"$subkey\" is not a group of other options"
			      . " (are you missing the tag in a <$ckey \"tag\"> object?);\ncontext is:  $context.<$ckey>" );
			return undef;
		    }
		    if (
			not defined(
			    $i = $self->parse_instructions( $instructions_filepath, $value->{$subkey}, $subhash, "$context.<$ckey \"$subkey\">" )
			)
		      )
		    {
			return undef;
		    }
		    $instructions{$ckey}{$subkey} = $i;
		}
	    }
	    elsif ( defined $value ) {
		## Turn a scalar value into a single-element array, to allow consistent downstream processing.
		$instructions{$ckey} = [$value];
	    }
	    else {
		## We have an empty array.
		## FIX MAJOR:  Can this ever be legal in our context?
		## $self->{logger}->error("ERROR:  In file \"$instructions_filepath\", $ckey is an empty array.");
		$instructions{$ckey} = [];
	    }
	    if ( $type == CONTAINER && ref $instructions{$ckey} ne 'HASH' ) {
		$self->{logger}->error( "ERROR:  In file \"$instructions_filepath\", \"$ckey\" is not a group of distinct objects"
		      . " (are you missing the tag in a <$ckey \"tag\"> object?);\ncontext is:  $context.<$ckey>" );
		return undef;
	    }
	}
    }

    # This constitutes a certain amount of syntactic validation beyond just parsing.
    # Semantic validation is run instead in the validate_instructions() routine.
    foreach my $ikey ( keys %$structure_part ) {
	my $attributes   = $structure_part->{$ikey};
	my $required     = $attributes->[0];
	my $multiplicity = $attributes->[1];
	## FIX MAJOR:  do we need any of the rest of these?
	## my $type         = $attributes->[2];
	## my $subhash      = $attributes->[3];
	if ( $required and not defined $instructions{$ikey} ) {
	    my $element = $multiplicity == MULTIPLE ? "<$ikey>" : $ikey;
	    $self->{logger}->error(
		"ERROR:  In file \"$instructions_filepath\", element \"$element\" is required but not supplied;\ncontext is:  $context.$ikey");
	    return undef;
	}
    }
    return \%instructions;
}

# Handle validation steps that are common for host and service sensors, so we don't have to repeat them
# within the validate_instructions() routine.
sub validate_sensor {
    my $self     = shift;
    my $filepath = shift;
    my $kind     = shift;    # 'host' or 'service'
    my $tag      = shift;
    my $sensor   = shift;
    my $outcome  = 0;        # start out pessimistic, so we can abort with this outcome at any time

    my %sensor_type_uses_regex_pattern = (
	os_type                => 1,
	os_version             => 1,
	os_bitwidth            => 1,
	machine_architecture   => 1,
	file_name              => 1,
	symlink_name           => 1,
	directory_name         => 1,
	mounted_filesystem     => 1,
	file_content           => 1,
	running_system_service => 1,
	full_process_command   => 1,
	open_local_port        => 0,
	open_named_socket      => 1,
    );

    # Check for a valid sensor type.  The existence of $sensor->{type} is already
    # guaranteed during the parsing stage, so we need not check that here.
    if ( not exists $is_valid_sensor_type{ $sensor->{type} } ) {
	$self->{logger}->error("ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has an invalid sensor \"type\" field.");
	return $outcome;
    }

    # Every sensor must have some non-empty pattern to match against.
    if ( $sensor->{pattern} eq '' ) {
	$self->{logger}->error("ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has an empty \"pattern\" field.");
	return $outcome;
    }

    # Check for a valid sensor resource for the types that need one, and the absence of a resource for those that don't.
    if ( $sensor->{type} =~ m{^( file_content | file_name | symlink_name | directory_name | open_local_port )$}x ) {
	if ( not defined $sensor->{resource} ) {
	    $self->{logger}->error("ERROR:  In file \"$filepath\", $kind sensor \"$tag\" is missing the \"resource\" field.");
	    return $outcome;
	}
	if ( $sensor->{resource} =~ m{^\s*$} ) {
	    $self->{logger}->error("ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has an empty \"resource\" field.");
	    return $outcome;
	}

	# Here we impose a constraint on the resource value for most of the sensor types that use fileglobs as a
	# candidate-item generator:  the value of each glob must represent an absolute pathname.  That's because
	# we don't want the discovery instructions to depend on a pathname which is relative to whatever happens
	# to be the current working directory of the process that runs the discovery actions.
	#
	if ( $sensor->{type} =~ m{^( file_content | file_name | symlink_name | directory_name )$}x ) {
	    ## We need:  resource = "fileglobs"
	    ##
	    ## In general for sensor types that use fileglobs on a Windows platform, a glob might include
	    ## a drive letter and colon prefix before the actual path.  On this platform, we allow either
	    ## backslash or forward slash as the root-directory delimiter, inasmuch as though we expect a
	    ## backslash to be a common choice, a forward slash is also acceptable to the Windows kernel
	    ## itself and might someday appear in a context we might want to support.
	    ##
	    ## For what it's worth, Windows treats the following characters as reserved and not allowed in
	    ## file or directory names:
	    ##     < > : " / \ | ? *
	    ##     ASCII control characters, except in alternate data streams (probably not something we must deal with)
	    ##
	    ## We start by resetting the match position, to make the multiple passes of pattern matching
	    ## be completely independent of any prior matching on the $sensor->{resource} value.
	    pos( $sensor->{resource} ) = undef;
	    my @globs = grep { defined } $sensor->{resource} =~ m{
	      ## Pick up where we left off at the end of the last pass of this match.
	      \G

	      ## Ignore any leading separators.
	      \s*

	      ## Match either a double-quoted glob or an unquoted glob.  A quoted glob can contain literal
	      ## space characters, which helps support Windows platforms where spaces in filepaths may
	      ## be fairly common.  An unquoted glob cannot contain any space characters.  For complete
	      ## generality, either form can contain an escaped quote character (although that won't do any
	      ## good on Windows, which does not allow double-quote characters in file and directory names),
	      ## an escaped backslash, or an escaped glob metacharacter.  (To avoid confusion, we disallow any
	      ## naked backslashes immediately before any other characters.)  Using backslashes for escaping
	      ## in this way, in combination with the level of backslash interpretation as the instructions
	      ## file is read, means that two backslashes must be specified in the instructions file at
	      ## every place you want to use one backslash to escape some glob metacharacter.  Also, four
	      ## backslashes must be specified in the instructions file at every place where you want to use
	      ## one backslash explicitly in the glob filepath, typically on Windows as a directory level in
	      ## the glob.  (Better is just to use forward slashes for that; see the sensor documentation.)
	      ## For a quoted glob, we capture just the part inside the quotes.
	      (?:
		  "( (?: [^"\\] | \\["\\*?~\{\},\[\]-] )+ )"
	      |
		  ( (?: [^ "\\] | \\["\\*?~\{\},\[\]-] )+ )
	      )

	      ## Ignore any trailing separators.  But insist that there either be a separator following the
	      ## matched glob, or that we be at the end of the string.  That way, we don't allow two globs
	      ## in the resource value to abut without any intervening separators.
	      (?: \s+ | $ )
	      }xgc;
	    ## Pick up the rest of the resource string, beyond all the successful matches.
	    ( my $remainder ) = $sensor->{resource} =~ m{ \G (.*) }x;
	    if ( $remainder ne '' ) {
		$self->{logger}->error( "ERROR:  In file \"$filepath\", $kind sensor \"$tag\""
		      . " has a \"resource\" which contains a bad filepath glob (starting at \"$remainder\")."
		      . "  See the documentation if this is confusing." );
		return $outcome;
	    }
	    ##
	    ## On Windows, ~ and ~/... work, but ~username does not.  So we disallow that non-working form right
	    ## away here, so as not to cause downstream confusion as to why you didn't get an expected glob match.
	    ##
	    my $glob_pattern = ( $^O eq 'MSWin32' ) ? qr{^(([a-zA-Z]:)?[/\\]|~([/\\]|$))} : qr{^[/~]};
	    foreach my $glob (@globs) {
		if ( $glob !~ m{$glob_pattern} ) {
		    $self->{logger}->error( "ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has a \"resource\" component ($glob)"
			  . " which is not an absolute path." );
		    return $outcome;
		}
	    }
	    $sensor->{resource_globs} = \@globs;
	    $sensor->{sorted_globs} = join( ', ', map { '"' . $_ . '"' } sort @globs );
	}
	elsif ( $sensor->{type} eq 'open_local_port' ) {
	    ## We need:  resource = "{IP address block(s)}"

	    ## We validate the resource as though it contains one more more space-separated CIDR blocks.
	    ## A CIDR block may be negated by prefixing with a "!" character, as long as it is not the
	    ## wildcard CIDR block for its address type (that is, "0.0.0.0/0" for IPv4 or "::/0" for IPv6,
	    ## respectively).  An individual IP address will be considered to match as long as it matches
	    ## at least one non-negated CIDR block of the same address type and does not also match match
	    ## any of the negated blocks of the same address type:
	    ##
	    ## ( (addr ~ block1 || addr ~ block2 || addr ~ block3) && addr !~ block4 && addr !~ block5 && addr !~ block6 )
	    ## ( (addr ~ block1 || addr ~ block2 || addr ~ block3) && !(addr ~ block4 || addr ~ block5 || addr ~ block6) )

	    # These are just basic patterns, not completely definitive.  More qualification is
	    # needed to enforce constraints at a level higher than just character matching.
	    # FIX LATER:  We don't currently support the mixed x:x:x:x:x:x:d.d.d.d form of IPv4-in-IPv6 address.
	    # (We'd like to not have to worry about that, although we have seen it in netstat output on some
	    # platforms.)
	    # FIX MINOR:  Look at standard Perl packages for both full validation of CIDR blocks and comparison of
	    # individual IP addresses against CIDR blocks to see if the address is included in the block.  Ideally,
	    # we get both capabilities in the same package.  Look carefully at the bug reports for each package, to
	    # ensure that we are not adopting other people's bad software.  Here are some possible packages:
	    #     Data::Checker::IP            (looks good, though might be a bit heavyweight for our use)
	    #     Data::IPV4::Range::Parse     (looks like it's IPv4 only)
	    #     Data::Validate::IP           (looks good for IP validation, but only IPv4 addr-in-CIDR capability)
	    #     Net::Address::IPv4           ()
	    #     Net::CIDR                    (can do CIDR lookup, but seems clumsy)
	    #     Net::CIDR::Compare           (looks like it's IPv4 only)
	    #     Net::CIDR::Set               ()
	    #     Net::CIDR::Set::IPv4         ()
	    #     Net::CIDR::Set::IPv6         ()
	    #     Net::IP                      ()
	    #     Net::IP::AddrRanges          ()
	    #     Net::IP::Identifier          ()
	    #     Net::IP::Identifier::Net     ()
	    #     Net::IP::Identifier::Regex   ()
	    #     Net::IP::Lite                ()
	    #     Net::IP::Match               ()
	    #     Net::IP::Match::Bin          ()
	    #     Net::IP::Match::Regexp       ()
	    #     Net::IP::Minimal             ()
	    #     Net::IP::RangeCompare        (looks like it's IPv4 only)
	    #     Net::IP::Util                ()
	    #     Net::IPAddress               ()
	    #     Net::IPAddress::Filter       ()
	    #     Net::IPAddress::Util::Range  ()
	    #     Net::IPv4Addr                ()
	    #     Net::IPv6Addr                ()
	    #     Net::IPv6Address             ()
	    #     Net::Inet                    ()
	    #     Net::Interface               ()
	    #     Net::Netmask                 ()
	    #     Net::Subnet                  ()
	    #     NetAddr::IP                  ()
	    #     NetAddr::IP::Lite            ()
	    #     NetAddr::IP::Util            ()
	    #     NetAddr::IP::UtilPP          ()
	    #     NetObj::IPv4Address          ()
	    #     Paranoid::Network            ()
	    #     Paranoid::Network::IPv4      ()
	    #     Paranoid::Network::IPv6      ()
	    #     Regexp::Common               ()
	    #     Regexp::Common::net          ()
	    #     Regexp::Common::net::CIDR    ()
	    #     Regexp::IPv6                 ()
	    my $ipv4_cidr_block = qr{ (\d{1,3}) \. (\d{1,3}) \. (\d{1,3}) \. (\d{1,3}) / (\d{1,2}) }x;
	    my $ipv6_cidr_block = qr{ ( (?: [[:xdigit:]]{1,4})? (?: :{1,2} [[:xdigit:]]{1,4} ){0,7} (?: ::)? ) / (\d{1,3}) }x;

	    my @ipv4_cidr_blocks = ();
	    my @ipv6_cidr_blocks = ();
	    foreach my $cidr_block ( split ' ', $sensor->{resource} ) {
		if ( $cidr_block =~ m{^!?$ipv4_cidr_block$}o ) {
		    ## Validate each address component, along with the network-mask length portion of the IPv4 CIDR block.
		    ## These tests cover all of the individual pieces we can usefully validate, except for possibly
		    ## having some one-bits in the address part beyond the specified number of prefix bits.
		    if ( $1 > 255 || $2 > 255 || $3 > 255 || $4 > 255 || $5 > 32 ) {
			$self->{logger}
			  ->error( "ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has a bad \"resource\" field component ($cidr_block):"
			      . "  it is not a valid IPv4 CIDR block." );
			return $outcome;
		    }
		    ## We don't allow negating a wildcard, since that would just block all addresses of that type.
		    ## If you want that, just don't list any CIDR blocks of that type in the sensor resource.
		    if ($cidr_block eq '!0.0.0.0/0') {
			$self->{logger}
			  ->error( "ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has a bad \"resource\" field component ($cidr_block):"
			      . "  it is the negation of the IPv4 wildcard CIDR block." );
			return $outcome;
		    }
		    ## We insist that if the netmask contains no bits, all the address bits must be zero.
		    if ( $5 == 0 && join( '', $1, $2, $3, $4 ) =~ m{[1-9]} ) {
			$self->{logger}
			  ->error( "ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has a bad \"resource\" field component ($cidr_block):"
			      . "  the netmask is zero but the address part contains some non-zero bits." );
			return $outcome;
		    }
		    push @ipv4_cidr_blocks, $cidr_block;
		}
		elsif ( my @components = $cidr_block =~ m{^!?$ipv6_cidr_block$}o ) {
		    ## FIX MAJOR:  more validation can and should be done, to validate that:
		    ## * at most one of the ":" separators that is a double-colon, and then only if there are fewer than 8 hex-digits components
		    ## * the full set of separators and hex-digits components adds up to an RFC-compliant address
		    ## * (optionally) the IPv6 address is further RFC compliant, in that it is normalized according to
		    ##   RFC-5952, "A Recommendation for IPv6 Address Text Representation"
		    ## When it comes right down to it, though, we're probably better off using some standard package for such validation.
		    ##
		    ## Validate the network-mask length portion of the IPv6 CIDR block.
		    if ( $components[$#components] > 128 ) {
			$self->{logger}
			  ->error( "ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has a bad \"resource\" field component ($cidr_block):"
			      . "  it is not a valid IPv6 CIDR block." );
			return $outcome;
		    }
		    ## We don't allow negating a wildcard, since that would just block all addresses of that type.
		    ## If you want that, just don't list any CIDR blocks of that type in the sensor resource.
		    if ($cidr_block eq '!::/0') {
			$self->{logger}
			  ->error( "ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has a bad \"resource\" field component ($cidr_block):"
			      . "  it is the negation of the IPv6 wildcard CIDR block." );
			return $outcome;
		    }
		    ## We insist that if the netmask contains no bits, all the address bits must be zero.
		    if ( $components[$#components] == 0 && join( '', @components[ 0 .. ( $#components - 1 ) ] ) =~ m{[1-9a-fA-F]} ) {
			$self->{logger}
			  ->error( "ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has a bad \"resource\" field component ($cidr_block):"
			      . "  the netmask is zero but the address part contains some non-zero bits." );
			return $outcome;
		    }
		    push @ipv6_cidr_blocks, $cidr_block;
		}
		else {
		    $self->{logger}
		      ->error( "ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has a bad \"resource\" field component ($cidr_block):"
			  . "  it is not a valid CIDR block." );
		    return $outcome;
		}
	    }
	    ## Here we recode the customer-provided resource to make it easier to process downstream.
	    $sensor->{resource} = { IPv4 => \@ipv4_cidr_blocks, IPv6 => \@ipv6_cidr_blocks };

	    # FIX MINOR:  The first branch of this conditional represents older code that did not yet support port ranges.
	    # It should be removed once we are fully comfortable with the second branch.
	    if (0) {
		# We need pattern = "{space-separated list of port numbers}".
		#
		# FIX LATER:  Perhaps we should also support well-known port names drawn from either the /etc/services or
		# C:\Windows\System32\drivers\etc\services file.  See the Perl getservbyname() routine, and the getservent()
		# and endservent() routines for forming a cache of service names for repeated matching.  If we do this type
		# of validation, we need to be aware that validating this aspect of a sensor on the GroundWork server might
		# yield a different result than what we might compute on a GDMA client machine, since the respective system
		# services files might contain very different lists of services.
		#
		if ( $sensor->{pattern} !~ /^\s*\d+(\s+\d+)*\s*$/ ) {
		    $self->{logger}
		      ->error( "ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has a bad \"pattern\" field ($sensor->{pattern}):"
			  . "  it is not a space-separated list of port numbers." );
		    return $outcome;
		}
		my %sensor_port = ();
		while ( $sensor->{pattern} =~ /(\d+)/g ) {
		    my $port = $1 + 0;    # Force dropping of any leading zeros.
		    ## Port 0 is virtual and should never actually be in use.
		    if ( $port < 1 || $port > 65535 ) {
			$self->{logger}
			  ->error( "ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has a bad \"pattern\" field ($sensor->{pattern}):"
			      . "  port $port is out of range (1..65535)." );
			return $outcome;
		    }
		    $sensor_port{$port} = $port;
		}
		## Here we define a simple one-step match routine that will match against $_ containing a single port number.
		##
		## FIX MINOR;  our match routine should probably set @_ or somesuch to pass back values to be assigned to
		## $MATCHED#$, or return the matched port number in list context (which seems to be working well enough now),
		## or assign directly to an external @MATCHED array, depending on how we handle the equivalent regex matching
		## routine for other sensor types
		##
		## This is my first-ever closure.  (I've just never found them to be otherwise terribly useful.)
		##
		## To fit into the context in which this match routine will be used, it must return the port number if the
		## matching succeeds.
		##
		$sensor->{match} = sub { $sensor_port{$_} // ( wantarray ? () : undef ) };

		## FIX MAJOR:  clean this up; this was just for development testing to make sure our model of
		## creating and running the match routine works as intended
		if (0) {
		    local $_ = 995;
		    my @matched_port = &{ $sensor->{match} };
		    print "matched_port pattern is '$sensor->{pattern}'\n";
		    print "matched_port size is " . ( scalar @matched_port ) . " elements\n";
		    print "matched_port = " . ( @matched_port ? "@matched_port" : '(none)' ) . "\n";
		}
	    }
	    else {
		# We need pattern = "{space-separated list of individual port numbers or port ranges}".
		#
		# FIX LATER:  Perhaps we should also support well-known port names drawn from either the /etc/services or
		# C:\Windows\System32\drivers\etc\services file.  See the Perl getservbyname() routine, and the getservent()
		# and endservent() routines for forming a cache of service names for repeated matching.  If we do this type
		# of validation, we need to be aware that validating this aspect of a sensor on the GroundWork server might
		# yield a different result than what we might compute on a GDMA client machine, since the respective system
		# services files might contain very different lists of services.
		#
		if ( $sensor->{pattern} !~ /^\s*(\d+(-|\.\.)\d+|\d+|\s+)+\s*$/ ) {
		    $self->{logger}
		      ->error( "ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has a bad \"pattern\" field ($sensor->{pattern}):"
			  . "  it is not a space-separated list of individual port numbers or port ranges." );
		    return $outcome;
		}
		my @port_range    = ();
		my %separate_port = ();
		pos( $sensor->{pattern} ) = undef;
		while ( $sensor->{pattern} =~ /(\d+(-|\.\.)\d+|\d+)/g ) {
		    my $port_span = $1;
		    if ( $port_span =~ m{(\d+)(?:-|\.\.)(\d+)} ) {
			## We have an range of consecutive port numbers, with the range endpoints both included in the range.
			my $head_port = $1 + 0;    # Force dropping of any leading zeros.
			my $tail_port = $2 + 0;    # Force dropping of any leading zeros.
			## Port 0 is virtual and should never actually be in use.
			if ( $head_port < 1 || $head_port > 65535 ) {
			    $self->{logger}
			      ->error( "ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has a bad \"pattern\" field ($sensor->{pattern}):"
				  . "  port $head_port is out of range (1..65535)." );
			    return $outcome;
			}
			if ( $tail_port < 1 || $tail_port > 65535 ) {
			    $self->{logger}
			      ->error( "ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has a bad \"pattern\" field ($sensor->{pattern}):"
				  . "  port $tail_port is out of range (1..65535)." );
			    return $outcome;
			}
			if ( $head_port > $tail_port ) {
			    $self->{logger}
			      ->error( "ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has a bad \"pattern\" field ($sensor->{pattern}):"
				  . "  port range $port_span has the start and end ports out of order." );
			    return $outcome;
			}
			## We don't bother to attempt any range-uniqueness testing here; it would be slightly complicated,
			## and the user can handle that on their own if there is any worry about efficiency of matching
			## with range duplication in play.
			push @port_range, [ $head_port, $tail_port ];
		    }
		    else {
			## We have an individual port.
			my $port = $port_span + 0;    # Force dropping of any leading zeros.
			## Port 0 is virtual and should never actually be in use.
			if ( $port < 1 || $port > 65535 ) {
			    $self->{logger}
			      ->error( "ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has a bad \"pattern\" field ($sensor->{pattern}):"
				  . "  port $port is out of range (1..65535)." );
			    return $outcome;
			}
			$separate_port{$port} = $port;
		    }
		}
		## Here we define a match routine that will match both port ranges and individual ports against $_
		## containing a single port number.
		##
		## FIX MINOR;  our match routine should probably set @_ or somesuch to pass back values to be assigned to
		## $MATCHED#$, or return the matched port number in list context (which seems to be working well enough now),
		## or assign directly to an external @MATCHED array, depending on how we handle the equivalent regex matching
		## routine for other sensor types
		##
		## This is my first-ever closure.  (I've just never found them to be otherwise terribly useful.)
		##
		## To fit into the context in which this match routine will be used, it must return the port number if the
		## matching succeeds, whether that port matched an individual port number or a port range in the sensor
		## pattern.  It must supply the result properly in list context when called in list context.  Given that we
		## only ever return one scalar value, we seem to be fine just returning that one value when we find a match.
		## But importantly, if no match occurs, the result in list context must be an empty list, not a one-element
		## list containing perhaps an undefined value.
		##
		$sensor->{match} = sub {
		    $separate_port{$_} // do {
			my $included_port = undef;
			foreach my $range (@port_range) {
			    if ( $_ >= $range->[0] && $_ <= $range->[1] ) {
				$included_port = $_;
				last;
			    }
			}
			$included_port;
		      }
		      // ( wantarray ? () : undef );
		};

		## FIX MAJOR:  clean this up; this was just for development testing to make sure our model of
		## creating and running the match routine works as intended
		if (0) {
		    local $_ = 7100;
		    ## my @matched_port = &{ $sensor->{match} };
		    my @matched_port = map { $_ // '' } &{ $sensor->{match} };
		    print "matched_port pattern is '$sensor->{pattern}'\n";
		    print "matched_port size is " . ( scalar @matched_port ) . " elements\n";
		    print "matched_port = " . ( @matched_port ? "'@matched_port'" : '(none)' ) . "\n";
		    print "matched_port first element is undef\n" if @matched_port && not defined $matched_port[0];
		    print "matched_port first element is an empty string\n" if @matched_port && $matched_port[0] eq '';
		}
	    }
	}
    }
    elsif ( $sensor->{type} eq 'full_process_command' ) {
	## resource = "userlist" is optional, and must be validated here.
	## If provided, it must be non-empty and not consist completely of separators.
	if ( defined $sensor->{resource} ) {
	    if ( $sensor->{resource} =~ m{^[\s,]*$} ) {
		$self->{logger}->error("ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has an empty \"resource\" field.");
		return $outcome;
	    }

	    use feature 'fc';
	    my @users    = ();
	    my @fc_users = ();

	    if ( $^O eq 'MSWin32' ) {
		##
		## We tried using Win32::NetAdmin::GetUsers( '', 0, \%user_full_name ) to fetch a complete list of
		## user names on this machine, to validate the resource components against.  However, that gathering
		## of user account names ended up only including ordinary names, not various system names such as
		## 'SYSTEM', 'LOCAL SERVICE', and 'NETWORK SERVICE'.  So we have abandoned any attempt to enumerate
		## all the available usernames to compare against the usernames specified in the sensor resource.
		## Therefore, this sensor will react silently to typos in the specified usernames, perhaps simply
		## omitting from the sensor results process matches that would have otherwise occurred.
		##
		## If we did try to enumerate user names, we would also need to look beyond standard system names
		## to also find managed account names, group managed account names, and virtual account names.  The
		## last of those might not even exist on the system, which might kill our ability to validate sensor
		## resource components against such user names.  (I suppose that if the user wanted to match this
		## sensor against such a resource, they would need to either not provide a sensor resource, or we
		## would need to accept some sort of marker for the resource component (say, an @ prefix) that would
		## indicate we should not attempt to look for that component in the set of existing user accounts.)
		##
		## The MSDN doc says that user account names are limited to 20 characters, cannot be terminated by a
		## period, cannot include unprintable characters in the range 1..31, and cannot include any of the
		## following printable characters: , " / \ [ ] : | < > + = ; ? *
		## nor any characters in the range \001 through \037, inclusive.  We check those conditions with both
		## a carefully constructed regex and follow-on tests.  The exclusion of commas is lucky for us, so
		## we can still use them as separators on this platform.
		##
		## Note that said constraints do not mention the NUL character (numeric value 0), which I presume
		## is also outlawed, nor DEL (0x7f), nor presumably unprintable characters in the range 0x80..0x9F,
		## nor any issues with Unicode characters at code points 256 and higher.  We assume those are also
		## illegal, but for now we're not going to check for them here.
		##
		## The Microsoft doc says that backslashes are not allowed within usernames, but that belies the fact
		## that usernames may come in the form domain\user which we will allow (but validate) here.
		## (I used a separate test program to develop and validate the complicated regex below.)
		##
		## We first reset the match position so the matching here always starts out independent of all previous
		## matching of the $sensor->{resource} value.
		pos( $sensor->{resource} ) = undef;
		##
		## The following match uses m{}gc to both run multiple passes of matching to pick up successive usernames,
		## and to leave the position as it is when it stops on failure so we can capture the remainder (the failed
		## portion) of the string to construct a clear log message pointing to exactly where the trouble starts.
		##
		## The grep is needed because in each successful pass of matching, one or the other of the alternative
		## username capture groups is going to be undefined while the other is defined.
		##
		@users = grep { defined } $sensor->{resource} =~ m{
		  ## Pick up where we left off at the end of the last pass of this match.
		  \G

		  ## Ignore any leading separators.
		  [\s,]*

		  ## Match either a double-quoted username or an unquoted username.  Either form can contain a single
		  ## backslash to separate domain\user components, both of which must not be empty.  A quoted username
		  ## can contain literal space characters as part of either or both of the domain or user components.
		  ## An unquoted username cannot contain any space characters.  For a quoted username, we capture just
		  ## the part inside the quotes.
		  (?:
		    "( (?: [^,"/\\[\]:|<>+=;?*\001-\037]+ \\ )? [^,"/\\[\]:|<>+=;?*\001-\037]+ )"
		  |
		    ( (?: [^\s,"/\\[\]:|<>+=;?*\001-\037]+ \\ )? [^\s,"/\\[\]:|<>+=;?*\001-\037]+ )
		  )

		  ## Ignore any trailing separators.  But insist that there either be a separator following the
		  ## matched username, or that we be at the end of the string.  That way, we don't allow two
		  ## usernames in the resource value to abut without any intervening separators.
		  (?: [\s,]+ | $ )
		  }xgc;
		## Pick up the rest of the resource string, beyond all the successful matches.
		( my $remainder ) = $sensor->{resource} =~ m{ \G (.*) }x;
		if ($remainder ne '') {
		    $self->{logger}->error( "ERROR:  In file \"$filepath\", $kind sensor \"$tag\""
			  . " has a \"resource\" which contains a bad username (starting at \"$remainder\")." );
		    return $outcome;
		}

		my $hostname = undef;
		eval {
		    require Sys::Hostname;
		    $hostname = Sys::Hostname::hostname();
		    $hostname =~ s/\..*// if defined $hostname;
		};

		foreach my $user (@users) {
		    ##
		    ## In addition to requirements noted earlier, we also check that space characters do not occur at
		    ## the beginning or end of each username, or on either side of a backslash separating the domain
		    ## and user components.  Only the user portion of a domain\user username is limited to 20 characters.
		    ##
		    if ( $user =~ m{^\s|\s\\|\\\s|\s$} || $user =~ /\.$/ || length( $user =~ s/.*\\//r ) > 20 ) {
			$self->{logger}->error( "ERROR:  In file \"$filepath\", $kind sensor \"$tag\""
			      . " has a \"resource\" component ($user) which is not a valid username." );
			return $outcome;
		    }
		    ## We attempt to localize a ".\UserName" reference to the local domain, since we don't expect to see such
		    ## a domain\username on the process side.  The only utility would be to potentially use such a domain in an
		    ## attempt to use "." as a generic stand-in for the computer name used as a domain name, so the resource
		    ## definition can apply across many computers but still reference specifically local user accuonts.
		    ##
		    $user =~ s/^\.(?=\\)/$hostname/ if defined $hostname;
		}

		## Since we have the @users list in hand here, we construct a resource match routine and stuff it into the
		## sensor definition to use during discovery.  That allows the discovery processing to not need to re-parse
		## the sensor resource definition.  The resource match routine must handle usernames both with and without
		## prefixed domain names, and do case-insensitive matching.

		@fc_users = map { fc } @users;
		my %fc_domain_users = map { $_        => 1 } grep /\\/, @fc_users;
		my %fc_only_users   = map { $_        => 1 } grep !/\\/, @fc_users;
		my %fc_all_users    = map { s/.*\\//r => 1 } @fc_users;
		$sensor->{resource_match} = sub {
		    use feature 'fc';
		    my $fc_process_user = fc shift;
		    if ( $fc_process_user =~ /\\/ ) {
			## The process user includes a domain name; first try for a full match
			## with some resource component that also includes a domain name.
			return 1 if $fc_domain_users{$fc_process_user};
			## Then try to match when the resource specifies only the user component.
			return 1 if $fc_only_users{ $fc_process_user =~ s/.*\\//r };
		    }
		    else {
			## The process user does not include a domain name.  I don't actually expect to ever encounter
			## this case, but if we do, match against all the user names specified in the resource.
			return 1 if $fc_all_users{$fc_process_user};
		    }
		    return 0;
		};
	    }
	    else {
		## On Linux, we will use the $sensor->{resource} value in a "ps"-command construction that does
		## not work if the value begins or ends with whitespace.  Leading whitespace would also interfere
		## with the split() below, by creating an illusory empty first username.  We solve both problems
		## by directly editing the sensor resource value to eliminate such whitespace.  This is cleaner
		## than requiring the administrator to avoid using such whitespace. in the instructions file.
		$sensor->{resource} =~ s/^[\s,]+|[\s,]+$//g;
		@users = split /[\s,]+/, $sensor->{resource};
		@fc_users = map { fc } @users;

		# We check for errors in forming our username hash (by checking $!) as a matter of general precaution.
		# getpwent() and endpwent() are not documented at the Perl level as affecting the $! variable.  Yet
		# the underlying library call is so documented (i.e., possibly affecting errno), so I cannot imagine
		# that Perl is not paying attention.
		#
		my %user_uid = ();
		$! = 0;
		while ( my ( $user_name, $password, $uid ) = getpwent() ) {
		    $user_uid{$user_name} = $uid;
		}
		endpwent();
		if ($!) {
		    $self->{logger}->error(
			"ERROR:  In file \"$filepath\", $kind sensor \"$tag\" found a problem while validating the \"resource\" component ($!)."
		    );
		    return $outcome;
		}
		foreach my $user (@users) {
		    ## Here we are assuming a very simple form for a username -- all alphanumeric, dash, or underscore.
		    ## I believe that will suffice on all UNIX-like platforms.
		    if ( $user !~ m{^[-_a-zA-Z0-9]+$} ) {
			$self->{logger}->error( "ERROR:  In file \"$filepath\", $kind sensor \"$tag\""
			      . " has a \"resource\" component ($user) which is not a valid username." );
			return $outcome;
		    }
		    ## Presuming that this sensor is being validated on the machine where it will run, we ought to validate
		    ## every user name individually to ensure that it exists on the machine before it gets used in a "ps"
		    ## command.  "ps" will do that itself anyway, but if we depend only on that, we'll get some ugly error
		    ## messages spilled out by "ps" that won't get properly captured and logged in our discovery results.
		    ##
		    ## FIX MINOR:  We know that some validation of discovery instructions might take place on the GroundWork
		    ## server, where the set of available usernames might be different than it is on a client where the
		    ## discovery will eventually be run.  We need some flag to tell whether we're in that situation, in
		    ## which case a failure to pass this test should probably be turned into a warning instead of an error.
		    ##
		    if ( not exists $user_uid{$user} ) {
			$self->{logger}->error( "ERROR:  In file \"$filepath\", $kind sensor \"$tag\""
			      . " has a \"resource\" which mentions a non-existent user \"$user\"." );
			return $outcome;
		    }
		}
	    }

	    $sensor->{fc_users} = \@fc_users;
	    $sensor->{sorted_fc_users} = join( ', ', sort @fc_users );
	}
    }
    elsif ( $sensor->{type} eq 'mounted_filesystem' ) {
	## resource = "filesystem_types" is optional, and must be validated here.
	## If provided, it must be non-empty and not consist completely of separators.
	if ( defined $sensor->{resource} ) {
	    if ( $sensor->{resource} =~ m{^[\s,]*$} ) {
		$self->{logger}->error("ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has an empty \"resource\" field.");
		return $outcome;
	    }

	    pos( $sensor->{resource} ) = undef;
	    my @fs_types = grep { defined } $sensor->{resource} =~ m{
	      ## Pick up where we left off at the end of the last pass of this match.
	      \G

	      ## Ignore any leading separators.
	      [\s,]*

	      ## Match either a double-quoted filesystem type or an unquoted filesystem type.  A quoted filesystem
	      ## type can contain literal space characters (though it's not clear that this will ever be useful).
	      ## An unquoted filesystem type cannot contain any space characters.  For a quoted filesystem type,
	      ## we capture just the part inside the quotes.
	      (?: "( [^,"]+ )" | ( [^\s,"]+ ) )

	      ## Ignore any trailing separators.  But insist that there either be a separator following the
	      ## matched filesystem type, or that we be at the end of the string.  That way, we don't allow
	      ## two filesystem types in the resource value to abut without any intervening separators.
	      (?: [\s,]+ | $ )
	      }xgc;
	    ## Pick up the rest of the resource string, beyond all the successful matches.
	    ( my $remainder ) = $sensor->{resource} =~ m{ \G (.*) }x;
	    if ( $remainder ne '' ) {
		$self->{logger}->error( "ERROR:  In file \"$filepath\", $kind sensor \"$tag\""
		      . " has a \"resource\" which contains a bad filesystem type (starting at \"$remainder\")." );
		return $outcome;
	    }

	    foreach my $fs_type (@fs_types) {
		##
		## In addition to requirements noted earlier, we also check that space characters do not occur at
		## the beginning or end of each filesystem type.
		##
		if ( $fs_type =~ m{^\s|\s$} ) {
		    $self->{logger}->error( "ERROR:  In file \"$filepath\", $kind sensor \"$tag\""
			  . " has a \"resource\" component ($fs_type) which is not a valid filesystem type." );
		    return $outcome;
		}
	    }

	    ## Since we have the @fs_types list in hand here, we construct a resource match routine and stuff it into the
	    ## sensor definition to use during discovery.  That allows the discovery processing to not need to re-parse
	    ## the sensor resource definition.  The resource match routine must do case-insensitive matching.

	    use feature 'fc';
	    local $_;
	    my %fc_fs_types = map { fc($_) => 1 } @fs_types;
	    $sensor->{resource_match} = sub {
		use feature 'fc';
		my $fc_actual_fs_type = fc shift;
		## Match against all the filesystem types specified in the resource.
		return 1 if $fc_fs_types{$fc_actual_fs_type};
		return 0;
	    };

	    ## $sensor->{fc_fs_types} = \@fc_fs_types;
	    ## $sensor->{sorted_fc_fs_types} = join( ', ', sort @fc_fs_types );
	}
    }
    elsif ( defined $sensor->{resource} ) {
	$self->{logger}
	  ->error("ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has a \"resource\" field, but this sensor type does not need one.");
	return $outcome;
    }

    # Check for a valid form of the cardinality.  The forms that are valid depend on the $kind of sensor ('host' or 'service').

    # Note that cardinality is not allowed to be specified externally for a host sensor, but should be defaulted in the code
    # for both a host sensor (necessarily) and a service sensor (if not already provided) when the instructions are read.
    if ( not defined $sensor->{cardinality} ) {
	$self->{logger}->error("ERROR:  In file \"$filepath\", $kind sensor \"$tag\" is missing a \"cardinality\" value.");
	return $outcome;
    }
    my $valid_cardinality = $kind eq 'host' ? qr{^( single | first )$}x : qr{^( single | first | multiple )$}x;
    if ( $sensor->{cardinality} !~ m{$valid_cardinality} ) {
	$self->{logger}->error("ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has an invalid \"cardinality\" value.");
	return $outcome;
    }

    # Check for for a valid pattern, according to the sensor type.

    if ( $^O eq 'MSWin32' && $sensor->{type} =~ m{^( file_name | symlink_name | directory_name | mounted_filesystem | open_named_socket )$}x ) {
	##
	## For the listed sensors on Windows, we modify the provided pattern to match backslashes in glob-result
	## filepaths by specifying forward slashes in the pattern, so as not to require the user to deal with
	## quadrupling of backslahes in the instructions file.
	##
	## Note that named sockets (the AF_UNIX socket family) is just being added to Windows at the end of 2017.
	## The filepaths for such sockets will be Win32 UTF-8 file system paths.  So we might encounter full Unicode
	## characters in such paths.  Fortunately, that doesn't affect the pattern transform we are making here.
	##
	$sensor->{pattern} =~ s{/}{\\\\}g;
    }

    ## For code development and debugging only.
    ## print "pattern value is <$sensor->{pattern}>\n";

    # FIX LATER:  Apply a timeout around this section, to prevent denial-of-service
    # attacks via bad patterns that tie up the regex compiler.  Also look into the
    # BSD::Resource package for imposing process resource limits such as memory size.

    if ( $sensor_type_uses_regex_pattern{ $sensor->{type} } ) {
	## Validate the user-supplied pattern, in the manner recommended in "Programming Perl".
	if ( not eval { "" =~ /$sensor->{pattern}/; 1 } ) {
	    chomp $@;
	    $self->{logger}
	      ->error("ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has a bad \"pattern\" field ($sensor->{pattern}):  $@");
	    return $outcome;
	}

	# Now we know the user-supplied pattern is at least safe to compile.
	#
	# We don't impose any modifiers such as case-insensitivity on the entire pattern,
	# because we cannot guess the intentions of the user.  Such modifiers can be
	# cloistered inside the pattern if desired.  See the Auto-Setup documentation.
	#
	$sensor->{match} = sub { /$sensor->{pattern}/ };

	## FIX MINOR:  clean this up; this was just for development testing to make sure our model of
	## creating the match routine works as intended
	if (0) {
	    ## local $_ = '/usr/local/bin/httpd.bin -f /etc/httpd.conf';
	    local $_ = '/etc/influxdb/influxdb.conf';
	    my @matched_values = &{ $sensor->{match} };
	    print "matched_values size is " . ( scalar @matched_values ) . " elements\n";
	    print "matched_values = " . ( @matched_values ? "'@matched_values'" : '(none)' ) . "\n";
	}
    }
    else {
	## We don't take any action here for the sensor types that don't use a regex for pattern matching.
	## That validation will occur elsewhere, specific to that sensor type.
    }

    # Check for valid transliteration patterns, if they exist.
    if ( defined $sensor->{transliteration} ) {
	if ( ref $sensor->{transliteration} ne 'ARRAY' ) {
	    $self->{logger}
	      ->error( "ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has a bad \"transliteration\" field ($sensor->{transliteration}):"
		  . "  it is not the expected array." );
	    return $outcome;
	}

	# The following opcodes are needed for our purposes, when this code is run against Perl 5.24.0:
	#
	#     const      'constant item'
	#     rv2gv      'ref-to-glob cast'
	#     lineseq    'line sequence'
	#     padany     'private value'
	#     transr     'transliteration (tr///)'
	#     leaveeval  'eval "string" exit'
	#
	# That appears to be the minimum set necessary to execute a single "tr///r" transform within our
	# sandbox.  That set appears to be small enough that we hope and expect it doesn't allow anything
	# dangerous.
	#
	# The "transr" opcode is specifically the form of transliteration that returns a modified value,
	# not a count.  There is also a "trans" opcode that has the exact same description, but that one
	# refers to transliteration that returns a count instead of the modified value.
	#
	# Unfortunately, it seems that the opcodes required for our purposes change over time, so we will
	# need to tune this as we port to newer versions of Perl.  We use the safe-transliteration.pl
	# tool for that purpose.  Here we list the opcodes in the order that they are encountered during
	# evaluation of the tr operator as we use it; that ordering varies with the version of Perl.
	#
	my $perl_version = sprintf( "%vd", $^V );
	my @required_opcodes = ();
	if ( $perl_version =~ /^\Q5.24.\E/ ) {
	    @required_opcodes = ( 'const', 'rv2gv', 'lineseq', 'padany', 'transr', 'leaveeval' );
	}
	elsif ( $perl_version =~ /^\Q5.16.\E/ && $^O eq 'MSWin32' ) {
	    @required_opcodes = ( 'const', 'rv2gv', 'lineseq', 'padany', 'transr', 'pushmark', 'list', 'leaveeval' );
	}
	if (not @required_opcodes) {
	    $self->{logger}->error( "ERROR:  " . __PACKAGE__ . " is not tuned for your version of Perl ($perl_version) and platform ($^O)." );
	    return $outcome;
	}

	my $sandbox = new Safe('Sandbox::Transliteration');
	$sandbox->permit_only(@required_opcodes);

	foreach my $trans ( @{ $sensor->{transliteration} } ) {
	    if ( $trans eq '' ) {
		$self->{logger}->error("ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has an empty \"transliteration\" field.");
		return $outcome;
	    }
	    ## This check won't necessarily be accurate if you use one of the [cdsr] modifier characters as a
	    ## delimiter, which is why we only say "appears to contain", not "contains".  But those are poor choices
	    ## for delimiters, so don't do that.  Our test for printing the HINT is similarly inadequate, since you
	    ## could for example use braces for the SEARCHLIST delimiter and one of the modifier characters for the
	    ## REPLACEMENTLIST delimiter, and that situation would not trigger printing the hint.  So the coverage and
	    ## messages here are not perfect, but at least this will definitely prevent the situation we need to block.
	    if ( $trans =~ /([cdsr]+)$/ && $1 =~ /r/ ) {
		$self->{logger}->error(
		        "ERROR:  In file \"$filepath\", $kind sensor \"$tag\" appears to contain"
		      . " a /r modifier in the \"transliteration\" field ($trans)."
		      . (
			$trans =~ /^[cdsr]/
			? "  (HINT:  Don't use any of 'c', 'd', 's', or 'r' as your SEARCHLIST or REPLACEMENTLIST delimiter.)"
			: ''
		      )
		);
		return $outcome;
	    }
	    local $_ = '';
	    local $SIG{__WARN__} = sub { die @_ };
	    my $result = $sandbox->reval( "tr ${trans}r", 1 );
	    if ($@) {
		chomp $@;
		$self->{logger}
		  ->error("ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has a bad \"transliteration\" field ($trans):  $@");
		return $outcome;
	    }
	    if ( not defined $result ) {
		$self->{logger}->error(
		    "ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has a bad \"transliteration\" field ($trans):" . "  cause unknown." );
		return $outcome;
	    }
	}

	# Now that we know the externally-specified transliteration is valid and safe to use, we can
	# wrap it in a routine which makes it automatically safe to apply using just a simple subroutine
	# call.  (Then we can save away that routine for later use against some future value of $_.)
	#
	local $_;
	$sensor->{transliterate} = $sandbox->wrap_code_ref(
	    eval "sub { my \$t = \$_; " . join( '; ', map { "\$t =~ tr $_" } @{ $sensor->{transliteration} } ) . "; return \$t; }" );

	## FIX MAJOR:  clean this up; this was just for development testing to make sure our model of
	## creating the transliterate routine works as intended
	if (0) {
	    $_ = 'string needing transliteration';
	    my $transliterated_string = &{ $sensor->{transliterate} };
	    print "transliterated_string = '$transliterated_string'\n";
	}
    }

    # Check for valid sanitization patterns, if they exist.
    if ( defined $sensor->{sanitization} ) {
	## For code development and debugging only.
	## print "sanitization value is <$sensor->{sanitization}>\n";

	# The following opcodes are needed for our purposes, when this code is run against Perl 5.24.0:
	#
	#     const      'constant item'
	#     rv2gv      'ref-to-glob cast'
	#     lineseq    'line sequence'
	#     padany     'private value'
	#     transr     'transliteration (tr///)'
	#     leaveeval  'eval "string" exit'
	#
	# That appears to be the minimum set necessary to execute a single "tr///cdr" transform within our
	# sandbox.  That set appears to be small enough that we hope and expect it doesn't allow anything
	# dangerous.
	#
	# The "transr" opcode is specifically the form of transliteration that returns a modified value,
	# not a count.  There is also a "trans" opcode that has the exact same description, but that one
	# refers to transliteration that returns a count instead of the modified value.
	#
	# Unfortunately, it seems that the opcodes required for our purposes change over time, so we will
	# need to tune this as we port to newer versions of Perl.  We use the safe-sanitization.pl
	# tool for that purpose.  Here we list the opcodes in the order that they are encountered during
	# evaluation of the tr operator as we use it; that ordering varies with the version of Perl.
	#
	my $perl_version = sprintf( "%vd", $^V );
	my @required_opcodes = ();
	if ( $perl_version =~ /^\Q5.24.\E/ ) {
	    @required_opcodes = ( 'const', 'rv2gv', 'lineseq', 'padany', 'transr', 'leaveeval' );
	}
	elsif ( $perl_version =~ /^\Q5.16.\E/ && $^O eq 'MSWin32' ) {
	    @required_opcodes = ( 'const', 'rv2gv', 'lineseq', 'padany', 'transr', 'pushmark', 'list', 'leaveeval' );
	}
	if (not @required_opcodes) {
	    $self->{logger}->error( "ERROR:  " . __PACKAGE__ . " is not tuned for your version of Perl ($perl_version) and platform ($^O)." );
	    return $outcome;
	}

	my $sandbox = new Safe('Sandbox::Sanitization');
	$sandbox->permit_only(@required_opcodes);

	local $_ = '';
	local $SIG{__WARN__} = sub { die @_ };
	my $safe_delimiters = q(/:|,!%&*+.=~);
	my $chosen_delim    = undef;
	foreach my $delim ( split( '', $safe_delimiters ) ) {
	    if ( $sensor->{sanitization} !~ /\Q$delim\E/ ) {
		my $result = $sandbox->reval( "tr ${delim}$sensor->{sanitization}${delim}${delim}cdr", 1 );
		if ($@) {
		    chomp $@;
		    $self->{logger}->error(
			"ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has a bad \"sanitization\" field ($sensor->{sanitization}):  $@");
		    return $outcome;
		}
		if ( not defined $result ) {
		    $self->{logger}->error(
			    "ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has a bad \"sanitization\" field ($sensor->{sanitization}):"
			  . "  cause unknown." );
		    return $outcome;
		}
		$chosen_delim = $delim;
		last;
	    }
	}

	# This problem might not actually be the user's fault; it might simply be that the sanitization string
	# includes all of the characters listed in $safe_delimiters as ones we might try to use.  We expect
	# that is unlikely, so we're willing to abort instead of trying some non-printable control character
	# as a delimiter, which is even more unlikely to be specified in the user's sanitization string.
	if ( not defined $chosen_delim ) {
	    $self->{logger}
	      ->error( "ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has a bad \"sanitization\" field ($sensor->{sanitization}):"
		  . "  cannot find a usable delimiter to wrap it in." );
	    return $outcome;
	}

	# Now that we know the externally-specified sanitization is valid and safe to use, we can
	# wrap it in a routine which makes it automatically safe to apply using just a simple subroutine
	# call.  (Then we can save away that routine for later use against some future value of $_.)
	#
	$sensor->{sanitize} =
	  $sandbox->wrap_code_ref( eval "sub { tr ${chosen_delim}$sensor->{sanitization}${chosen_delim}${chosen_delim}cdr }" );

	## FIX MAJOR:  clean this up; this was just for development testing to make sure our model of
	## creating the sanitize routine works as intended
	if (0) {
	    $_ = 'string needing sanitization';
	    my $sanitized_string = &{ $sensor->{sanitize} };
	    print "sanitized_string = '$sanitized_string'\n";
	}
    }

    # Check for a valid host_profile, service_profile, or service name (the character set of the given
    # value).  It has to fit within the constraints of what Monarch will accept without difficulty, even
    # though we have probably never officially enforced explicit character-set contraints on profile and
    # service names.  We are slightly limited in the validation we can do, in that a profile or service
    # name might include some macro references whose ultimate values are not known at this time.  So all
    # we can do is elide the macro references from the values we have in hand and check what is left.  If
    # the ultimate value including the substituted macro values is bad, that will only be found during
    # the analysis of the discovery results, for those sensors that actually matched.
    my $profile_type = "${kind}_profile";
    if ( defined( $sensor->{$profile_type} ) && $sensor->{$profile_type} eq '' || defined( $sensor->{$kind} ) && $sensor->{$kind} eq '' ) {
	$self->{logger}->error( "ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has an empty "
	      . ( defined( $sensor->{$profile_type} ) ? $profile_type : $kind )
	      . " name." );
	return $outcome;
    }
    ( my $target_object_name = ( defined( $sensor->{$profile_type} ) ? $sensor->{$profile_type} : $sensor->{$kind} ) ) =~
      s{\$(?:MATCHED|SANITIZED)\d+\$}{}g;
    if ( $target_object_name =~ m'[^-_.@a-zA-Z0-9]' ) {
	$self->{logger}->error( "ERROR:  In file \"$filepath\", $kind sensor \"$tag\" has invalid characters (other than -_.\@a-zA-Z0-9)"
	      . " in the specified "
	      . ( defined( $sensor->{$profile_type} ) ? $profile_type : $kind )
	      . " name." );
	return $outcome;
    }

    $outcome = 1;
    return $outcome;
}

# run syntax checks as much as possible
sub validate_instructions {
    my $self         = shift;
    my $filepath     = shift;
    my $instructions = shift;
    my $outcome      = 0;       # start out pessimistic, so we can abort with this outcome at any time

=pod
$instructions = {
  'format_version' => '1.0',
  'host' => {
    'Linux' => {
      'host_profile' => 'gdma-linux-host',
      'pattern' => 'linux',
      'type' => 'os_type'
    }
  },
  'service' => {
    'Apache' => {
      'pattern' => '/(httpd\\.bin)(?:\\s+-f\\s+(\\S+)|$)',
      'service_profile' => 'apache-web-server',
      'type' => 'process_command'
    },
    'InfluxDB Server' => {
      'pattern' => '^/etc/influxdb/influxdb.conf$',
      'service_profile' => 'influxdb-server',
      'type' => 'file_name'
    },
    'sendmail' => {
      'pattern' => '25',
      'resource' => '0.0.0.0/32',
      'service_profile' => 'sendmail',
      'type' => 'open_local_port'
    }
  }
};
=cut

    # All of the following checks are specific to the desired format of our auto-discovery instructions and the
    # particular sensors we will support.  There is no good way to generalize these tests and make them table-driven.

    my $format_version  = $instructions->{format_version};
    my $host_sensors    = $instructions->{host};
    my $service_sensors = $instructions->{service};

    # Check for the proper format version.
    if ( $format_version ne '1.0' ) {
	$self->{logger}->error("ERROR:  In file \"$filepath\", \"format_version\" is \"$format_version\" but must be \"1.0\".");
	return $outcome;
    }

    # FIX MAJOR:  make sure these validations are handled
    # Check for the proper subsidiary elements at every level (that is handled during parsing, not here).
    # Check for enforced tags at the <host> and <service> level.
    # Check for valid tag names (whatever character set we will accept, mostly).
    # Check for valid macro references.

    foreach my $tag ( keys %$host_sensors ) {
	my $sensor = $host_sensors->{$tag};
	return $outcome if not $self->validate_sensor ($filepath, 'host', $tag, $sensor);
    }

    foreach my $tag ( keys %$service_sensors ) {
	my $sensor = $service_sensors->{$tag};

	# We first take care of one test that we used to handle during table-driven parsing, when we only
	# supported the "service_profile" directive and could make it MANDATORY.  But now that we support the
	# "service" directive as an alternative, and each of those two directives by itself is OPTIONAL, we
	# must manually validate that we do have exactly one of them present in the sensor definition.
	if ( not( defined( $sensor->{service_profile} ) xor defined( $sensor->{service} ) ) ) {
	    $self->{logger}->error( "ERROR:  In file \"$filepath\", service sensor \"$tag\" must have exactly one"
		  . " \"service_profile\" or \"service\" directive (and not both)." );
	    return $outcome;
	}

	return $outcome if not $self->validate_sensor( $filepath, 'service', $tag, $sensor );

	# There aren't too many tests we can run against certain sensor directives, with regard to just their own content.
	# (I suppose we might consider restricting the set of characters, for security reasons.)  But we certainly can
	# test for cross-consistency with the values of other sensor directives.
	if ( $sensor->{cardinality} eq 'multiple' ) {
	    ## If we have cardinality "multiple" and there are macro references in the service_profile name,
	    ## that is a configuration error.
	    if ( defined( $sensor->{service_profile} ) && $sensor->{service_profile} =~ m{\$(MATCHED|SANITIZED)\d+\$} ) {
		$self->{logger}->error( "ERROR:  In file \"$filepath\", service sensor \"$tag\" has a \"service_profile\" directive"
		      . " containing macro references, but the cardinality is \"multiple\"." );
		return $outcome;
	    }
	    ## If we have cardinality "multiple" and there are macro references in the service name,
	    ## that is a configuration error.
	    if ( defined( $sensor->{service} ) && $sensor->{service} =~ m{\$(MATCHED|SANITIZED)\d+\$} ) {
		$self->{logger}->error( "ERROR:  In file \"$filepath\", service sensor \"$tag\" has a \"service\" directive"
		      . " containing macro references, but the cardinality is \"multiple\"." );
		return $outcome;
	    }
	    ## If we have cardinality "multiple" and there are macro references in the check_command,
	    ## that is a configuration error.
	    if ( defined( $sensor->{check_command} ) && $sensor->{check_command} =~ m{\$(MATCHED|SANITIZED)\d+\$} ) {
		$self->{logger}->error( "ERROR:  In file \"$filepath\", service sensor \"$tag\" has a \"check_command\" directive"
		      . " containing macro references, but the cardinality is \"multiple\"." );
		return $outcome;
	    }
	    ## If we have cardinality "multiple" and there are macro references in the command_arguments,
	    ## that is a configuration error.
	    if ( defined( $sensor->{command_arguments} ) && $sensor->{command_arguments} =~ m{\$(MATCHED|SANITIZED)\d+\$} ) {
		$self->{logger}->error( "ERROR:  In file \"$filepath\", service sensor \"$tag\" has a \"command_arguments\" directive"
		      . " containing macro references, but the cardinality is \"multiple\"." );
		return $outcome;
	    }
	    ## If we have cardinality "multiple" and there are macro references in the externals_arguments,
	    ## that is a configuration error.
	    if ( defined( $sensor->{externals_arguments} ) && $sensor->{externals_arguments} =~ m{\$(MATCHED|SANITIZED)\d+\$} ) {
		$self->{logger}->error( "ERROR:  In file \"$filepath\", service sensor \"$tag\" has a \"externals_arguments\" directive"
		      . " containing macro references, but the cardinality is \"multiple\"." );
		return $outcome;
	    }
	}

	# Check for a valid instance suffix (character set; preferably one that is not a fixed value, but contains some macro reference).
	# FIX MAJOR:  do this

	# Check for a valid form of the instance_cmd_args, if there is some way to verify this.
	# FIX MINOR:  do this

	# Check for a valid form of the instance_ext_args, if there is some way to verify this.
	# FIX MINOR:  do this

	# Check that an instance_suffix is present if either instance_cmd_args or instance_ext_args is present.
	if ( not defined( $sensor->{instance_suffix} ) ) {
	    foreach my $directive (qw( instance_cmd_args instance_ext_args )) {
		if ( defined $sensor->{$directive} ) {
		    $self->{logger}
		      ->error("ERROR:  In file \"$filepath\", service sensor \"$tag\" uses \"$directive\" but has no \"instance_suffix\".");
		    return $outcome;
		}
	    }
	}
    }

    $outcome = 1;
    return $outcome;
}

# filetype is trigger, instructions, results
sub print_file {
    my $self     = shift;
    my $filetype = shift;
    my $filepath = shift;
    my $outcome  = 0;
    if ( $filetype eq 'trigger' ) {
	## FIX MAJOR
    }
    elsif ( $filetype eq 'instructions' ) {
	## FIX MAJOR
    }
    elsif ( $filetype eq 'results' ) {
	## FIX MAJOR
    }
    else {
	$self->{logger}->error("ERROR:  Unsupported filetype \"$filetype\" passed to print_file().");
	return undef;
    }
    return $outcome;
}

# Upgrade incoming discovery results to the current version, so we can accept old versions without
# having to scatter corrections throughout the code.  This also makes it possible for the server code
# to accommodate a gradual conversion of GDMA clients to newer versions of the client discovery code.
sub upgrade_discovery_results {
    my $self              = shift;
    my $discovery_results = shift;
    my $failed            = 0;
    my $failure_reason    = '';

    unless ($failed) {
	if ( not defined $discovery_results->{packet_version} ) {
	    $failed         = 1;
	    $failure_reason = "missing 'packet_version' field";
	}
    }
    unless ($failed) {
	if ( $discovery_results->{packet_version} eq '1.0' ) {
	    if ( not defined $discovery_results->{discovered_services} ) {
		## We grandfather in some old packet_version="1.0" discovery results from early beta Auto-Setup client
		## releases that did not yet support direct specification of individual services in the sensor results.
		## This is easier than demanding either that the user upgrade the format_version value in all their
		## instructions files, or instantly upgrade all their beta client releases.
		$discovery_results->{discovered_services} = [];
	    }
	}
    }

    return !$failed, $failure_reason;
}

# Validate the incoming discovery results as fitting the general form and content we expect to see.
# This level of checking does not involve any comparison of the data with the content of Monarch,
# and thus cannot know whether a given host profile references certain service profiles, or whether
# a given service profile references certain services.
sub validate_discovery_results {
    my $self              = shift;
    my $discovery_results = shift;
    my $server_side       = shift;    # boolean, to tell if server-side requirements must also be met
    my @analysis_results  = ();
    my $failed            = 0;
    my $failure_reason    = '';

    # FIX MAJOR:  fill in the @analysis_results with whatever we want to have recorded in
    # a file as a result of this validation.

    # These "is_required" hashes may be used in two different ways.  First, by listing all
    # the fields that are allowed at each level of the discovery results, the keys alone
    # can be used to check that the incoming data does not contain any unexpected fields.
    # If we do add some previously ignored field as the software evolves, the fact that it
    # will show up here in testing will trigger our adding a validation check later on.
    # And second, if we set the values appropriately, we can check that every absolutely
    # required field is actually present in the results.
    #
    # In some cases, we have explicit tests below for particular missing fields, that will
    # duplicate testing for required fields using these hashes.  That's largely because
    # those individual tests were written before these hashes were supplied.  We have not
    # taken out the extra tests both because they should do no harm, and because they also
    # provide some measure of protection if the values in these hashes are inadvertently
    # changed to not require certain fields that should be required.
    #
    # "exists $x{field}"      means the field is acceptable
    # "$x{field} > 0"         means the field is required
    # "abs( $x{field} ) > 1"  means if the field is present, its value must be defined
    #
    my %is_required_discovery_field = (
	packet_version              => 1,
	succeeded                   => 1,
	failure_message             => 0,
	last_step                   => 1,
	if_duplicate                => 1,
	soft_error_reporting        => 1,
	change_policy               => 0,
	registration_agent          => 1,
	forced_hostname             => 0,
	hostnames                   => 1,
	ip_addresses                => 1,
	mac_addresses               => 0,
	os_type                     => 1,
	configured_host_profile     => 0,
	configured_service_profile  => 0,
	discovered_host_profiles    => 1,
	discovered_service_profiles => 1,
	discovered_services         => 1,
	full_sensor_results         => 1,
	chosen_hostname             => $server_side ? 2 : -2,    # decided and supplied by the server, not the client
	chosen_alias                => $server_side ? 2 : -2,    # decided and supplied by the server, not the client
	chosen_address              => $server_side ? 2 : -2,    # decided and supplied by the server, not the client
    );
    my %is_required_sensor_field = (
	sensor_name          => 1,
	type                 => 1,
	enabled              => 1,
	matched              => 1,
	error_message        => 0,
	resource             => 0,
	cardinality          => 1,
	host_profile_name    => 0,
	service_profile_name => 0,
	service_name         => 0,
	check_command        => 0,
	command_arguments    => 0,
	externals_arguments  => 0,
	instances            => 1,
    );
    my %is_required_instance_field = (
	qualified_resource      => 1,
	raw_match_strings       => 1,
	sanitized_match_strings => 0,
	instance_suffix         => 0,
	instance_cmd_args       => 0,
	instance_ext_args       => 0,
    );

    unless ($failed) {
	if ( ref $discovery_results ne 'HASH' ) {
	    $failed         = 1;
	    $failure_reason = "internal coding error:  discovery results were not provided as the expected hash";
	}
	else {
	    ## Bring old-version discovery results up to current standards.
	    my $upgrade_outcome;
	    ( $upgrade_outcome, $failure_reason ) = $self->upgrade_discovery_results($discovery_results);
	    $failed = !$upgrade_outcome;
	}
    }

    unless ($failed) {
	foreach my $key ( keys %$discovery_results ) {
	    if ( not exists $is_required_discovery_field{$key} ) {
		$failed         = 1;
		$failure_reason = "unknown discovery results '$key' field";
	    }
	    last if $failed;
	}
    }
    unless ($failed) {
	foreach my $key ( keys %is_required_discovery_field ) {
	    if ( $is_required_discovery_field{$key} > 0 and not exists $discovery_results->{$key} ) {
		$failed         = 1;
		$failure_reason = "missing discovery results '$key' field";
	    }
	    elsif ( abs( $is_required_discovery_field{$key} ) > 1
		and exists $discovery_results->{$key}
		and not defined $discovery_results->{$key} )
	    {
		$failed         = 1;
		$failure_reason = "discovery results '$key' field is undefined";
	    }
	    last if $failed;
	}
    }

    unless ($failed) {
	if ( not defined $discovery_results->{packet_version} ) {
	    $failed         = 1;
	    $failure_reason = "missing 'packet_version' field";
	}
	elsif ( $discovery_results->{packet_version} ne '1.0' ) {
	    $failed         = 1;
	    $failure_reason = "invalid 'packet_version' field";
	}
	elsif ( not defined $discovery_results->{succeeded} ) {
	    $failed         = 1;
	    $failure_reason = "missing 'succeeded' field";
	}
	elsif ( not $discovery_results->{succeeded} ) {
	    $failed = 1;
	    $failure_reason =
	      "discovery failed on the client side (" . ( $discovery_results->{failure_message} // 'no reason was given' ) . ")";
	}
	elsif ( not defined $discovery_results->{last_step} ) {
	    $failed         = 1;
	    $failure_reason = "missing 'last_step' field";
	}
	elsif ( not $is_valid_last_step{ $discovery_results->{last_step} } ) {
	    $failed         = 1;
	    $failure_reason = "invalid 'last_step' field ($discovery_results->{last_step})";
	}
	elsif ( not defined $discovery_results->{if_duplicate} ) {
	    $failed         = 1;
	    $failure_reason = "missing 'if_duplicate' field";
	}
	elsif ( not $is_valid_if_duplicate{ $discovery_results->{if_duplicate} } ) {
	    $failed         = 1;
	    $failure_reason = "invalid 'if_duplicate' field ($discovery_results->{if_duplicate})";
	}
	elsif ( not defined $discovery_results->{soft_error_reporting} ) {
	    $failed         = 1;
	    $failure_reason = "missing 'soft_error_reporting' field";
	}
	elsif ( not $is_valid_soft_error_reporting{ $discovery_results->{soft_error_reporting} } ) {
	    $failed         = 1;
	    $failure_reason = "invalid 'soft_error_reporting' field ($discovery_results->{soft_error_reporting})";
	}
	elsif ( defined( $discovery_results->{change_policy} ) and not $is_valid_change_policy{ $discovery_results->{change_policy} } ) {
	    $failed         = 1;
	    $failure_reason = "invalid 'change_policy' field ($discovery_results->{change_policy})";
	}
	elsif ( not defined $discovery_results->{registration_agent} ) {
	    $failed         = 1;
	    $failure_reason = "missing 'registration_agent' field";
	}
	elsif ( $discovery_results->{registration_agent} !~ m{ ^[-._ a-zA-Z0-9]+$ }x ) {
	    $failed         = 1;
	    $failure_reason = "invalid 'registration_agent' field ($discovery_results->{registration_agent})";
	}
	elsif ( defined( $discovery_results->{forced_hostname} ) and ref $discovery_results->{forced_hostname} ) {
	    $failed         = 1;
	    $failure_reason = "'forced_hostname' field is not a single name";
	}
	elsif ( not defined $discovery_results->{hostnames} ) {
	    $failed         = 1;
	    $failure_reason = "missing 'hostnames' field";
	}
	elsif ( ref $discovery_results->{hostnames} ne 'ARRAY' ) {
	    $failed         = 1;
	    $failure_reason = "'hostnames' field is not a list of names";
	}
	## This next test only checks for validity of whatever elements are in the array, not whether the array is non-empty.
	elsif ( not have_valid_hostnames( $discovery_results->{hostnames} ) ) {
	    $failed         = 1;
	    $failure_reason = "'hostnames' field (@{$discovery_results->{hostnames}}) contains invalid hostname(s)";
	}
	elsif ( not defined $discovery_results->{ip_addresses} ) {
	    $failed         = 1;
	    $failure_reason = "missing 'ip_addresses' field";
	}
	elsif ( ref $discovery_results->{ip_addresses} ne 'ARRAY' ) {
	    $failed         = 1;
	    $failure_reason = "'ip_addresses' field is not a list of IP addresses";
	}
	## This next test only checks for validity of whatever elements are in the array, not whether the array is non-empty.
	elsif ( not have_valid_ip_addresses( $discovery_results->{ip_addresses} ) ) {
	    $failed         = 1;
	    $failure_reason = "'ip_addresses' field (@{$discovery_results->{ip_addresses}}) contains invalid IP address(es)";
	}
	elsif ( defined( $discovery_results->{mac_addresses} ) and ref $discovery_results->{mac_addresses} ne 'ARRAY' ) {
	    $failed         = 1;
	    $failure_reason = "'mac_addresses' field is not a list of MAC addresses";
	}
	## This next test only checks for validity of whatever elements are in the array, not whether the array is non-empty.
	elsif ( defined( $discovery_results->{mac_addresses} ) and not have_valid_mac_addresses( $discovery_results->{mac_addresses} ) ) {
	    $failed         = 1;
	    $failure_reason = "'mac_addresses' field (@{$discovery_results->{mac_addresses}}) contains invalid MAC address(es)";
	}
	elsif ( not defined $discovery_results->{os_type} ) {
	    $failed         = 1;
	    $failure_reason = "missing 'os_type' field";
	}
	elsif ( $discovery_results->{os_type} !~ m{ ^(?: linux | solaris | aix | hpux | windows )$ }x ) {
	    $failed         = 1;
	    $failure_reason = "invalid 'os_type' field ($discovery_results->{os_type})";
	}
	elsif ( not defined $discovery_results->{discovered_host_profiles} ) {
	    $failed         = 1;
	    $failure_reason = "missing 'discovered_host_profiles' field";
	}
	elsif ( ref $discovery_results->{discovered_host_profiles} ne 'ARRAY' ) {
	    $failed         = 1;
	    $failure_reason = "invalid 'discovered_host_profiles' field (not an array of values)";
	}
	elsif ( @{ $discovery_results->{discovered_host_profiles} } == 0 ) {
	    $failed         = 1;
	    $failure_reason = "invalid 'discovered_host_profiles' field (no host profiles were assigned)";
	}
	elsif ( @{ $discovery_results->{discovered_host_profiles} } > 1 ) {
	    ## At this stage, we only check that there is one unique host-profile name.  Later on,
	    ## when we analyze the individual sensor results, we will deal with possible conflicts,
	    ## if any, in associated values that might apply to associated service profiles.
	    my %unique = ();
	    @unique{ @{ $discovery_results->{discovered_host_profiles} } } = (undef) x @{ $discovery_results->{discovered_host_profiles} };
	    if ( scalar keys %unique != 1 ) {
		$failed         = 1;
		$failure_reason = "invalid 'discovered_host_profiles' field (there is more than one distinct host profile assignment)";
	    }
	}
    }

    unless ($failed) {
	if ( not defined $discovery_results->{discovered_service_profiles} ) {
	    $failed         = 1;
	    $failure_reason = "missing 'discovered_service_profiles' field";
	}
	elsif ( ref $discovery_results->{discovered_service_profiles} ne 'ARRAY' ) {
	    $failed         = 1;
	    $failure_reason = "invalid 'discovered_service_profiles' field (not an array of values)";
	}
	if ( not defined $discovery_results->{discovered_services} ) {
	    $failed         = 1;
	    $failure_reason = "missing 'discovered_services' field";
	}
	elsif ( ref $discovery_results->{discovered_services} ne 'ARRAY' ) {
	    $failed         = 1;
	    $failure_reason = "invalid 'discovered_services' field (not an array of values)";
	}
	if ( not defined $discovery_results->{full_sensor_results} ) {
	    $failed         = 1;
	    $failure_reason = "missing 'full_sensor_results' field";
	}
	elsif ( ref $discovery_results->{full_sensor_results} ne 'ARRAY' ) {
	    $failed         = 1;
	    $failure_reason = "invalid 'full_sensor_results' field (not an array of values)";
	}
	elsif ( not @{ $discovery_results->{full_sensor_results} } ) {
	    $failed         = 1;
	    $failure_reason = "invalid 'full_sensor_results' field (discovery results contain no sensor results)";
	}
    }

    ## FIX MINOR:  Fill in more top-level validation tests as we populate more top-level fields in the
    ## discovery results, if we can figure out what might be useful.
    unless ($failed) {
    }

    ## Validate the results of all the individual sensors.
    my %have_host_sensor_name            = ();
    my %have_service_sensor_name         = ();
    my %matching_host_profile_sensors    = ();
    my %matching_service_profile_sensors = ();
    my %matching_service_sensors         = ();
    unless ($failed) {
	foreach my $sensor_results ( @{ $discovery_results->{full_sensor_results} } ) {
	    ##
	    ## Check sensor-level fields for validity.
	    ##

	    ## First, check to make sure the sensor has a name.  If that's not true, we cannot use the sensor name as an
	    ## identifier in error messages.
	    ##
	    ## Next, check if multiple host sensors or multiple service sensors have the same sensor name.  This would
	    ## not matter operationally in that the sensor name is used in the discovery results only for labeling, but
	    ## if we have a duplication, (1) that probably represents an upstream coding problem, and (2) we cannot use
	    ## the sensor names as known unique identifiers in error messages.
	    ##
	    ## For now, we're not so concerned with conflicts between host sensor names and service sensor names, as it
	    ## seems less likely that a user would confuse the two.  If necessary, a future version of the software can
	    ## be more restrictive.
	    ##
	    if ( not defined $sensor_results->{sensor_name} ) {
		$failed         = 1;
		$failure_reason = "a sensor result is missing the 'sensor_name' field";
	    }
	    elsif ( exists $have_host_sensor_name{ $sensor_results->{sensor_name} } ) {
		$failed         = 1;
		$failure_reason = "more than one host sensor has the same sensor name ($sensor_results->{sensor_name})";
	    }
	    elsif ( exists $have_service_sensor_name{ $sensor_results->{sensor_name} } ) {
		$failed         = 1;
		$failure_reason = "more than one service sensor has the same sensor name ($sensor_results->{sensor_name})";
	    }

	    unless ($failed) {
		foreach my $key ( keys %$sensor_results ) {
		    if ( not exists $is_required_sensor_field{$key} ) {
			$failed         = 1;
			$failure_reason = "unknown sensor results '$key' field for sensor '$sensor_results->{sensor_name}'";
		    }
		    last if $failed;
		}
	    }
	    unless ($failed) {
		foreach my $key ( keys %is_required_sensor_field ) {
		    if ( $is_required_sensor_field{$key} and not exists $sensor_results->{$key} ) {
			$failed         = 1;
			$failure_reason = "missing sensor results '$key' field for sensor '$sensor_results->{sensor_name}'";
		    }
		    last if $failed;
		}
	    }
	    unless ($failed) {
		if ( defined $sensor_results->{error_message} ) {
		    $failed         = 1;
		    $failure_reason = "sensor '$sensor_results->{sensor_name}' failed ($sensor_results->{error_message})";
		}
		elsif ( not defined $sensor_results->{type} ) {
		    $failed         = 1;
		    $failure_reason = "missing 'type' field for sensor '$sensor_results->{sensor_name}'";
		}
		elsif ( not $is_valid_sensor_type{ $sensor_results->{type} } ) {
		    $failed         = 1;
		    $failure_reason = "invalid 'type' field ($sensor_results->{cardinality})";
		}
		elsif ( not defined $sensor_results->{cardinality} ) {
		    $failed         = 1;
		    $failure_reason = "missing 'cardinality' field for sensor '$sensor_results->{sensor_name}'";
		}
		elsif ( not $is_valid_sensor_cardinality{ $sensor_results->{cardinality} } ) {
		    $failed         = 1;
		    $failure_reason = "invalid 'cardinality' field ($sensor_results->{cardinality})";
		}
		elsif ( $sensor_results->{cardinality} eq 'multiple' and defined $sensor_results->{host_profile_name} ) {
		    $failed         = 1;
		    $failure_reason = "host sensor has 'cardinality' value of 'multiple' for sensor '$sensor_results->{sensor_name}'";
		}
		elsif ( not defined $sensor_results->{enabled} ) {
		    $failed         = 1;
		    $failure_reason = "missing 'enabled' field for sensor '$sensor_results->{sensor_name}'";
		}
		elsif ( not defined $sensor_results->{matched} ) {
		    $failed         = 1;
		    $failure_reason = "missing 'matched' field for sensor '$sensor_results->{sensor_name}'";
		}
		elsif ( not defined( $sensor_results->{host_profile_name} )
		    and not defined( $sensor_results->{service_profile_name} )
		    and not defined( $sensor_results->{service_name} ) )
		{
		    $failed = 1;
		    $failure_reason =
		      "missing host_profile_name or service_profile_name or service_name field for sensor '$sensor_results->{sensor_name}'";
		}
		elsif ( defined( $sensor_results->{host_profile_name} ) and not is_valid_profile_name( $sensor_results->{host_profile_name} ) )
		{
		    $failed         = 1;
		    $failure_reason = "invalid 'host_profile_name' field for sensor '$sensor_results->{sensor_name}'";
		}
		elsif ( defined( $sensor_results->{service_profile_name} )
		    and not is_valid_profile_name( $sensor_results->{service_profile_name} ) )
		{
		    $failed         = 1;
		    $failure_reason = "invalid 'service_profile_name' field for sensor '$sensor_results->{sensor_name}'";
		}
		elsif ( defined( $sensor_results->{service_name} ) and not is_valid_service_name( $sensor_results->{service_name} ) ) {
		    $failed         = 1;
		    $failure_reason = "invalid 'service_name' field for sensor '$sensor_results->{sensor_name}'";
		}

		## FIX MINOR:  Are there any sensible ways to validate these fields in and of themselves,
		## without comparison to the Monarch database?
		##
		## Well, check for the possibly inappropriate presence of any of these fields in a host sensor, if we
		## don't plan on using those fields to apply them to service profiles referenced by the host profile.
		##
		## $sensor_results->{check_command}
		## $sensor_results->{command_arguments}
		## $sensor_results->{externals_arguments}

		elsif ( not defined $sensor_results->{instances} ) {
		    $failed         = 1;
		    $failure_reason = "missing 'instances' field for sensor '$sensor_results->{sensor_name}'";
		}
		elsif ( ref $sensor_results->{instances} ne 'ARRAY' ) {
		    $failed         = 1;
		    $failure_reason = "invalid 'instances' field for sensor '$sensor_results->{sensor_name}' (not an array of values)";
		}
		elsif ( $sensor_results->{matched} and not @{ $sensor_results->{instances} } ) {
		    $failed         = 1;
		    $failure_reason = "invalid 'matched' field for sensor '$sensor_results->{sensor_name}'"
		      . " (there are no matching instances for a matched sensor)";
		}
		elsif ( @{ $sensor_results->{instances} } and not $sensor_results->{matched} ) {
		    $failed         = 1;
		    $failure_reason = "invalid 'matched' field for sensor '$sensor_results->{sensor_name}'"
		      . " (there are matching instances for a non-matched sensor)";
		}
		elsif ( $sensor_results->{cardinality} eq 'single' and @{ $sensor_results->{instances} } > 1 ) {
		    $failed         = 1;
		    $failure_reason = "invalid 'instances' field for sensor '$sensor_results->{sensor_name}'"
		      . " (cardinality is single, but this sensor has multiple instances)";
		}
		elsif ( @{ $sensor_results->{instances} } ) {
		    my %service_instance_suffix = ();
		    foreach my $instance ( @{ $sensor_results->{instances} } ) {
			##
			## Check instance-level fields for validity.
			##
			foreach my $key ( keys %$instance ) {
			    if ( not exists $is_required_instance_field{$key} ) {
				$failed         = 1;
				$failure_reason = "unknown sensor instance results '$key' field for sensor '$sensor_results->{sensor_name}'";
			    }
			    last if $failed;
			}
			unless ($failed) {
			    foreach my $key ( keys %is_required_instance_field ) {
				if ( $is_required_instance_field{$key} and not exists $instance->{$key} ) {
				    $failed = 1;
				    $failure_reason =
				      "missing sensor instance results '$key' field for sensor '$sensor_results->{sensor_name}'";
				}
				last if $failed;
			    }
			}
			unless ($failed) {
			    if ( not defined( $instance->{instance_suffix} ) ) {
				if ( defined( $sensor_results->{service_profile_name} ) or defined( $sensor_results->{service_name} ) ) {
				    ## Our convention is that every service sensor must yield an instance_suffix in the discovery results,
				    ## whether or not one was declared in the sensor definition.  If a service sensor definition does not
				    ## include an instance_suffix directive, a sensor acting as a service sensor is supposed to supply an
				    ## empty string as the instance_soffix result for the instances of that sensor.
				    $failed         = 1;
				    $failure_reason = "invalid instance for sensor '$sensor_results->{sensor_name}'"
				      . " (internal coding problem; the discovery result for this service sensor has no instance_suffix field)";
				}
			    }
			    else {
				if ( defined $sensor_results->{host_profile_name} ) {
				    $failed = 1;
				    $failure_reason =
				        "invalid instance '$instance->{instance_suffix}' for sensor '$sensor_results->{sensor_name}'"
				      . " (instance_suffix field makes no sense with a host sensor)";
				}
				elsif ( $instance->{instance_suffix} =~ m{^\s+$} ) {
				    ## We can sometimes tolerate an empty instance_suffix field; in the situations where we end up
				    ## only having a single instance to process over all service sensors that name the same service
				    ## profile, that empty field will simply be ignored.  But we can never tolerate a blank field.
				    $failed = 1;
				    $failure_reason =
				      "invalid instance for sensor '$sensor_results->{sensor_name}' (instance_suffix field is blank)";
				}
				elsif ( $sensor_results->{cardinality} eq 'multiple'
				    and @{ $sensor_results->{instances} } > 1
				    and $instance->{instance_suffix} eq '' )
				{
				    $failed         = 1;
				    $failure_reason = "invalid instances for sensor '$sensor_results->{sensor_name}'"
				      . " (have multiple instances, but one has an empty instance_suffix field)";
				}
				elsif ( exists $service_instance_suffix{instance_suffix} ) {
				    ## Here we validate instance suffixes as a whole set, not just for individual instances.
				    ## That is to say, we check for uniqueness of the instance suffixes as they will be used.
				    $failed = 1;
				    $failure_reason =
				        "invalid instance '$instance->{instance_suffix}' for sensor '$sensor_results->{sensor_name}'"
				      . " (have multiple instances with the same instance_suffix field)";
				}
				$service_instance_suffix{ $instance->{instance_suffix} } = 1;
			    }
			}
			unless ($failed) {
			    ## FIX MINOR:  What sensible tests can we run on those fields?
			    ##
			    ## Well, check for the possibly inappropriate presence of the instance_cmd_args or instance_ext_args
			    ## field in a host sensor, if we don't plan on using those fields to apply them to service profiles
			    ## referenced by the host profile.
			    ##
			    ## $instance->{raw_match_strings}
			    ## $instance->{sanitized_match_strings}
			    ## $instance->{instance_cmd_args}
			    ## $instance->{instance_ext_args}
			}
			last if $failed;
		    }
		}
	    }
	    last if $failed;

	    if ( defined $sensor_results->{host_profile_name} ) {
		$have_host_sensor_name{ $sensor_results->{sensor_name} } = 1;
		push @{ $matching_host_profile_sensors{ $sensor_results->{host_profile_name} } }, $sensor_results
		  if $sensor_results->{matched};
	    }
	    if ( defined $sensor_results->{service_profile_name} ) {
		$have_service_sensor_name{ $sensor_results->{sensor_name} } = 1;
		push @{ $matching_service_profile_sensors{ $sensor_results->{service_profile_name} } }, $sensor_results
		  if $sensor_results->{matched};
	    }
	    if ( defined $sensor_results->{service_name} ) {
		$have_service_sensor_name{ $sensor_results->{sensor_name} } = 1;
		push @{ $matching_service_sensors{ $sensor_results->{service_name} } }, $sensor_results
		  if $sensor_results->{matched};
	    }
	}
    }

    #
    # Validate the results of all the sensors, in toto as opposed to individually.  Some
    # of that sort of checking has been integrated into the earlier first pass of looking
    # at all the sensors.  That which is better done separately, we do now.
    #

    # (*) [DONE] Check that there was at least one matching host-sensor result.
    #
    # (*) [DONE] Check if multiple matching host sensors reference different host profiles.
    #     In theory, this just duplicates what should have been apparent when looking at the
    #     discovered_host_profiles field at the top level of the discovery results, so the test
    #     here should in theory, with good incoming data, never be satisfied.  But we're looking
    #     here for unexpected inconsistencies in the data, so it's worth checking at this level.

    unless ($failed) {
	my $host_profile_count = scalar keys %matching_host_profile_sensors;
	if ( $host_profile_count == 0 ) {
	    $failed         = 1;
	    $failure_reason = "no host sensors matched";
	}
	elsif ( $host_profile_count > 1 ) {
	    $failed         = 1;
	    $failure_reason = "matching host sensors represent more than one distinct host profile assignment";
	}
    }

    # (*) [DONE] Check if multiple matching sensors of the same class that reference the same configuration
    #     object all do so using sensor-level fields that are identical, so there will be no conflicts when
    #     the configuration object is applied to the host.
    #
    # (*) [DONE] Check if multiple matching sensors of the same class that reference the same configuration
    #     object all do so using instance-level instance_suffix fields that do not collide across all those
    #     matching sensor results, so there will be no conflicts when the configuration object is applied to
    #     the host.  This is a larger test than we implemented above for an individual sensor, and we could
    #     subsume that test in this one if we choose to do so.
    #
    # (*) FIX LATER:  For the moment, we just don't allow more than one instance with the same instance_suffix,
    #     as checked by the previous test.  Logically, we could allow duplicate instance_suffix fields across
    #     multiple sensors of the same class as long as all of the other instance-level fields are also identical.
    #     We leave that as a future possibility in this code.

    unless ($failed) {
	my @sensor_groups = (
	    { class => 'host profile',    sensors => \%matching_host_profile_sensors },
	    { class => 'service profile', sensors => \%matching_service_profile_sensors },
	    { class => 'service',         sensors => \%matching_service_sensors }
	);

	foreach my $sensor_group (@sensor_groups) {
	    foreach my $object_name ( keys %{ $sensor_group->{sensors} } ) {
		##
		## Look for data conflicts at the sensor level, in these fields:
		##
		##     check_command
		##     command_arguments
		##     externals_arguments
		##
		my $check_command_sensor       = undef;
		my $check_command              = undef;
		my $command_arguments_sensor   = undef;
		my $command_arguments          = undef;
		my $externals_arguments_sensor = undef;
		my $externals_arguments        = undef;
		my %instance_suffix_sensor     = ();

		foreach my $sensor_results ( @{ $sensor_group->{sensors}{$object_name} } ) {
		    unless ($failed) {
			if ( not defined $check_command_sensor ) {
			    $check_command_sensor = $sensor_results->{sensor_name};
			    $check_command        = $sensor_results->{check_command};
			}
			else {
			    if ( defined $sensor_results->{check_command} ) {
				if ( not defined $check_command ) {
				    $failed = 1;
				}
				elsif ( $check_command ne $sensor_results->{check_command} ) {
				    $failed = 1;
				}
			    }
			    elsif ( defined $check_command ) {
				$failed = 1;
			    }
			    if ($failed) {
				$failure_reason = "when checking the discovery results that reference $sensor_group->{class} '$object_name',"
				  . " found conflicting values of check_command for sensor '$sensor_results->{sensor_name}'";
				$failure_reason .= " and sensor '$check_command_sensor'"
				  if $check_command_sensor ne $sensor_results->{sensor_name};
			    }
			}
		    }
		    unless ($failed) {
			if ( not defined $command_arguments_sensor ) {
			    $command_arguments_sensor = $sensor_results->{sensor_name};
			    $command_arguments        = $sensor_results->{command_arguments};
			}
			else {
			    if ( defined $sensor_results->{command_arguments} ) {
				if ( not defined $command_arguments ) {
				    $failed = 1;
				}
				elsif ( $command_arguments ne $sensor_results->{command_arguments} ) {
				    $failed = 1;
				}
			    }
			    elsif ( defined $command_arguments ) {
				$failed = 1;
			    }
			    if ($failed) {
				$failure_reason = "when checking the discovery results that reference $sensor_group->{class} '$object_name',"
				  . " found conflicting values of command_arguments for sensor '$sensor_results->{sensor_name}'";
				$failure_reason .= " and sensor '$command_arguments_sensor'"
				  if $command_arguments_sensor ne $sensor_results->{sensor_name};
			    }
			}
		    }
		    unless ($failed) {
			if ( not defined $externals_arguments_sensor ) {
			    $externals_arguments_sensor = $sensor_results->{sensor_name};
			    $externals_arguments        = $sensor_results->{externals_arguments};
			}
			else {
			    if ( defined $sensor_results->{externals_arguments} ) {
				if ( not defined $externals_arguments ) {
				    $failed = 1;
				}
				elsif ( $externals_arguments ne $sensor_results->{externals_arguments} ) {
				    $failed = 1;
				}
			    }
			    elsif ( defined $externals_arguments ) {
				$failed = 1;
			    }
			    if ($failed) {
				$failure_reason = "when checking the discovery results that reference $sensor_group->{class} '$object_name',"
				  . " found conflicting values of externals_arguments for sensor '$sensor_results->{sensor_name}'";
				$failure_reason .= " and sensor '$externals_arguments_sensor'"
				  if $externals_arguments_sensor ne $sensor_results->{sensor_name};
			    }
			}
		    }
		    unless ($failed) {
			## Presently, this code disallows having more than one sensor match with an identical instance_suffix.
			## That is the most aggressive possible type of validation.
			##
			## FIX MAJOR:  Verify that we are doing the right thing here with respect to a cardinality-'first'
			## sensor result with multiple instances in the discovery results.  (We should be ignoring all the
			## remaining instances for that sensor, in that case.)
			##
			foreach my $instance ( @{ $sensor_results->{instances} } ) {
			    if ( defined $instance->{instance_suffix} ) {
				if ( exists $instance_suffix_sensor{ $instance->{instance_suffix} } ) {
				    unless ( $instance_suffix_sensor{ $instance->{instance_suffix} } eq $sensor_results->{sensor_name}
					&& $sensor_results->{cardinality} eq 'first' )
				    {
					$failed = 1;
					$failure_reason =
					    "when checking the discovery results that reference $sensor_group->{class} '$object_name',"
					  . " found duplicate values of instance_suffix ('$instance->{instance_suffix}')"
					  . " for sensor '$sensor_results->{sensor_name}'";
					$failure_reason .= " and sensor '$instance_suffix_sensor{$instance->{instance_suffix}}'"
					  if $instance_suffix_sensor{ $instance->{instance_suffix} } ne $sensor_results->{sensor_name};
				    }
				}
				else {
				    $instance_suffix_sensor{ $instance->{instance_suffix} } = $sensor_results->{sensor_name};
				}
			    }
			    else {
				my $empty_string = '';
				$instance_suffix_sensor{$empty_string} = $sensor_results->{sensor_name};
			    }

			    ## FIX LATER:  If we relax the previous test and allow duplicate instance_suffix values
			    ## across multiple matched sensors that all name the same $sensor_group->{class} object,
			    ## check that all the important instance-level fields for those instances are identical:
			    ##
			    ##     instance_cmd_args
			    ##     instance_ext_args
			    ##
			    ## (That presumes we don't allow one instance to supply just one of those values and some
			    ## other instance to provide just the other, without directly conflicting values.)
			    ##
			    ## Note that since multiple matching sensors might possibly contribute different parts of these
			    ## items without otherwise conflicting, we will use the "consolidate_discovery_results" routine
			    ## to collapse down the discovery results to a final form before trying to apply them to Monarch.
			    ##
			    ## Also note that if we do relax that test, we will need to elaborate code in the
			    ## consolidate_discovery_results() routine that merges instances of sensors that
			    ## name the same $sensor_group->{class} object, so as to eliminate duplicates.
			    ##
			    last if $failed;
			}
		    }
		    last if $failed;
		}
		unless ($failed) {
		    ## If there is more than one sensor instance over all matching sensors that name the same $sensor_group->{class}
		    ## object, then all instance_suffix values for those sensor instances must be non-empty strings.  Which is to say,
		    ## you can't have the results of one sensor configuring just base instances of services while other sensors are
		    ## simultaneously configuring some non-base instances.
		    my $empty_string = '';
		    if ( scalar keys %instance_suffix_sensor > 1 and exists $instance_suffix_sensor{$empty_string} ) {
			$failed = 1;
			$failure_reason =
			    "when checking the discovery results that reference $sensor_group->{class} '$object_name', found an empty or"
			  . " missing value of instance_suffix for sensor '$instance_suffix_sensor{$empty_string}' when the results for some"
			  . " other matching sensor that specifies this same $sensor_group->{class} contain a non-empty instance_suffix value";
			## Let's not make it too difficult for the user to understand exactly which "other matching sensor" is involved.
			delete $instance_suffix_sensor{$empty_string};
			my $other_matching_sensor_instance_suffix = ( keys %instance_suffix_sensor )[0];
			$failure_reason .= "; compare to the instance_suffix in the results for sensor"
			  . " '$instance_suffix_sensor{$other_matching_sensor_instance_suffix}'";
		    }
		}
		last if $failed;
	    }
	    last if $failed;
	}
    }

    # FIX MAJOR:  implement this section
    #
    # (*) Are there other useful tests, looking for combinations of instances across service sensor results?
    #     Looking for conflicts in those settings?

    do {
	my $message =
	  $failed
	  ? "ERROR:  The discovery results failed validation ($failure_reason).\n"
	  : "NOTICE:  The discovery results passed basic validation (without comparison to the current content of the database).\n";
	local $Text::Wrap::columns  = 90;
	local $Text::Wrap::unexpand = 0;
	local $Text::Wrap::huge     = 'overflow';
	my $its_a_wrap = wrap( '', ' ' x ( $failed ? 8 : 9 ), $message );
	push @analysis_results, $its_a_wrap;
    };

    return !$failed, $failure_reason, '' . join( '', @analysis_results );
}

# Boil down the discovery results to a form that can be directly used during updates to
# Monarch, without needing to tiptoe around alternate possibilities during that phase of
# operation.  That is, discard parts that are irrelevant, and merge parts that are redundant.
# In performing this reduction, we can presume that the discovery results have already passed
# validation testing.
#
# We will use some alternate internal data structure, similar to the incoming discovery results
# but not called discovery results.  For simplicity, we will call these "elemental results".
#
# (*) Do a deep copy of the discovery results to form the initial elemental results, so we
#     don't disturb the original data as we edit it.
# (*) Drop all non-matched sensors.
# (*) For cardinality-"first" sensors, drop all instances beyond the first.
# (*) At the levels of discovery results, sensors, and instances, drop all attributes that
#     don't matter for Monarch updates.
# (*) Merge data from all sensors that specify a particular host profile or service profile
#     into just one consolidated sensor for that profile.  This means:
#     (+) merging sensor-level attributes if they are specified by some sensor and not another
#     (+) merging lists of instances
# (*) Consolidated sensor data should be indexed by the profile category (host or service),
#     and then by the profile name (i.e., no longer by a sensor name).
#
sub consolidate_discovery_results {
    my $self              = shift;
    my $discovery_results = shift;
    my $elemental_results = clone($discovery_results);

    # (*) Drop all non-matched sensors.
    #
    my @full_sensor_results = grep { $_->{matched} } @{ $elemental_results->{full_sensor_results} };
    $elemental_results->{full_sensor_results} = \@full_sensor_results;

    # (*) For cardinality-"first" sensors, drop all instances beyond the first.
    #
    foreach my $sensor_results ( @{ $elemental_results->{full_sensor_results} } ) {
	if ( $sensor_results->{cardinality} eq 'first' && @{ $sensor_results->{instances} } > 1 ) {
	    $sensor_results->{instances} = [ $sensor_results->{instances}[0] ];
	}
    }

    # (*) At the levels of discovery results, sensors, and instances, drop all attributes that
    #     don't matter for Monarch updates.  We show the other attributes we expect to keep in
    #     correponding comments here.
    #
    # FIX LATER:  Perhaps we should grep for the desired remaining elements instead of deleting
    # the elements we wish to discard.  That might be a bit more robust.
    #
    delete $elemental_results->{packet_version};
    delete $elemental_results->{succeeded};
    delete $elemental_results->{failure_message};
    ##     $elemental_results->{last_step};
    ##     $elemental_results->{if_duplicate};
    ##     $elemental_results->{soft_error_reporting};
    ##     $elemental_results->{change_policy};
    ##     $elemental_results->{registration_agent};
    ##     $elemental_results->{forced_hostname};
    ##     $elemental_results->{hostnames};
    ##     $elemental_results->{ip_addresses};
    ##     $elemental_results->{mac_addresses};
    ##     $elemental_results->{os_type};
    delete $elemental_results->{configured_host_profile};
    delete $elemental_results->{configured_service_profile};
    delete $elemental_results->{discovered_host_profiles};
    delete $elemental_results->{discovered_service_profiles};
    delete $elemental_results->{discovered_services};
    ##     $elemental_results->{full_sensor_results};
    ##     $elemental_results->{chosen_hostname};
    ##     $elemental_results->{chosen_alias};
    ##     $elemental_results->{chosen_address};
    foreach my $sensor_results ( @{ $elemental_results->{full_sensor_results} } ) {
	delete $sensor_results->{sensor_name};
	delete $sensor_results->{type};
	delete $sensor_results->{enabled};
	delete $sensor_results->{matched};
	delete $sensor_results->{error_message};
	delete $sensor_results->{resource};
	delete $sensor_results->{cardinality};
	##     $sensor_results->{host_profile_name};
	##     $sensor_results->{service_profile_name};
	##     $sensor_results->{service_name};
	##     $sensor_results->{check_command};
	##     $sensor_results->{command_arguments};
	##     $sensor_results->{externals_arguments};
	##     $sensor_results->{instances};
	foreach my $instance ( @{ $sensor_results->{instances} } ) {
	    delete $instance->{qualified_resource};
	    delete $instance->{raw_match_strings};
	    delete $instance->{sanitized_match_strings};
	    ##     $instance->{instance_suffix};
	    ##     $instance->{instance_cmd_args};
	    ##     $instance->{instance_ext_args};
	}
    }

    # (*) Merge data from all sensors that specify a particular host profile or service profile
    #     into just one consolidated sensor for that profile.  This means:
    #     (+) merging sensor-level attributes if they are specified by some sensor and not another
    #     (+) merging lists of instances
    my %host_sensor_profile_results    = ();
    my %service_sensor_profile_results = ();
    my %service_sensor_service_results = ();
    my %host_profiles                  = ();
    my %service_profiles               = ();
    my %services                       = ();
    foreach my $sensor_results ( @{ $elemental_results->{full_sensor_results} } ) {
	if ( defined $sensor_results->{host_profile_name} ) {
	    push @{ $host_sensor_profile_results{ $sensor_results->{host_profile_name} } }, $sensor_results;
	}
	if ( defined $sensor_results->{service_profile_name} ) {
	    push @{ $service_sensor_profile_results{ $sensor_results->{service_profile_name} } }, $sensor_results;
	}
	if ( defined $sensor_results->{service_name} ) {
	    push @{ $service_sensor_service_results{ $sensor_results->{service_name} } }, $sensor_results;
	}
    }
    foreach my $host_profile ( keys %host_sensor_profile_results ) {
	## We delete the sensor-level results and promote them up to the level of the host profile.
	my $check_command       = undef;
	my $command_arguments   = undef;
	my $externals_arguments = undef;
	my @instances           = ();
	foreach my $sensor_results ( @{ $host_sensor_profile_results{$host_profile} } ) {
	    $check_command       = $sensor_results->{check_command}       if not defined $check_command;
	    $command_arguments   = $sensor_results->{command_arguments}   if not defined $command_arguments;
	    $externals_arguments = $sensor_results->{externals_arguments} if not defined $externals_arguments;
	    ## This processing of sensor instances by simple concatenation is possible because the validation
	    ## stage already ensured that we have no duplicate instance_suffix values.  If we relax that test
	    ## and allow duplicate instance_suffix values as long as all the other relevant values (namely,
	    ## instance_cmd_args and instance_ext_args) are identical, then we would need to elaborate this code.
	    push @instances, @{ $sensor_results->{instances} };
	}
	$host_profiles{$host_profile}{check_command}       = $check_command       if defined $check_command;
	$host_profiles{$host_profile}{command_arguments}   = $command_arguments   if defined $command_arguments;
	$host_profiles{$host_profile}{externals_arguments} = $externals_arguments if defined $externals_arguments;
	$host_profiles{$host_profile}{instances}           = \@instances;
    }
    foreach my $service_profile ( keys %service_sensor_profile_results ) {
	## We delete the sensor-level results and promote them up to the level of the service profile.
	my $check_command       = undef;
	my $command_arguments   = undef;
	my $externals_arguments = undef;
	my @instances           = ();
	foreach my $sensor_results ( @{ $service_sensor_profile_results{$service_profile} } ) {
	    $check_command       = $sensor_results->{check_command}       if not defined $check_command;
	    $command_arguments   = $sensor_results->{command_arguments}   if not defined $command_arguments;
	    $externals_arguments = $sensor_results->{externals_arguments} if not defined $externals_arguments;
	    ## This processing of sensor instances by simple concatenation is possible because the validation
	    ## stage already ensured that we have no duplicate instance_suffix values.  If we relax that test
	    ## and allow duplicate instance_suffix values as long as all the other relevant values (namely,
	    ## instance_cmd_args and instance_ext_args) are identical, then we would need to elaborate this code.
	    push @instances, @{ $sensor_results->{instances} };
	}
	$service_profiles{$service_profile}{check_command}       = $check_command       if defined $check_command;
	$service_profiles{$service_profile}{command_arguments}   = $command_arguments   if defined $command_arguments;
	$service_profiles{$service_profile}{externals_arguments} = $externals_arguments if defined $externals_arguments;
	$service_profiles{$service_profile}{instances}           = \@instances;
    }
    foreach my $service ( keys %service_sensor_service_results ) {
	## We delete the sensor-level results and promote them up to the level of the service profile.
	my $check_command       = undef;
	my $command_arguments   = undef;
	my $externals_arguments = undef;
	my @instances           = ();
	foreach my $sensor_results ( @{ $service_sensor_service_results{$service} } ) {
	    $check_command       = $sensor_results->{check_command}       if not defined $check_command;
	    $command_arguments   = $sensor_results->{command_arguments}   if not defined $command_arguments;
	    $externals_arguments = $sensor_results->{externals_arguments} if not defined $externals_arguments;
	    ## This processing of sensor instances by simple concatenation is possible because the validation
	    ## stage already ensured that we have no duplicate instance_suffix values.  If we relax that test
	    ## and allow duplicate instance_suffix values as long as all the other relevant values (namely,
	    ## instance_cmd_args and instance_ext_args) are identical, then we would need to elaborate this code.
	    push @instances, @{ $sensor_results->{instances} };
	}
	$services{$service}{check_command}       = $check_command       if defined $check_command;
	$services{$service}{command_arguments}   = $command_arguments   if defined $command_arguments;
	$services{$service}{externals_arguments} = $externals_arguments if defined $externals_arguments;
	$services{$service}{instances}           = \@instances;
    }
    $elemental_results->{host_profiles}    = \%host_profiles;
    $elemental_results->{service_profiles} = \%service_profiles;
    $elemental_results->{services}         = \%services;
    delete $elemental_results->{full_sensor_results};

    return $elemental_results;
}

sub is_valid_hostname {
    my $hostname = shift;
    my $outcome  = 0;

    return $outcome if not defined $hostname;

    # To figure out how to validate a hostname, see:
    #
    #     https://en.wikipedia.org/wiki/Hostname#Restrictions_on_valid_hostnames
    #
    # In particular, a bunch of advice on the Internet appears to be wrong, notably about the
    # maximum total length of a hostname.  The total readable length of a hostname cannot exceed
    # 253 octets (bytes); see:  https://blogs.msdn.microsoft.com/oldnewthing/20120412-00/?p=7873/
    #
    my $len = do { use bytes; length $hostname; };
    return $outcome if $len < 1 or $len > 253;

    # A hostname consists of one or more dot-separated labels, wherein the length of each label
    # cannot exceed 63 octets.  There are restrictions on the individual characters in the label.
    #
    my $label = '(?:[a-zA-Z0-9]|[a-zA-Z0-9][-a-zA-Z0-9]{0,61}[a-zA-Z0-9])';
    return $outcome if $hostname !~ m{^$label(?:\.$label)*$}o;

    # There is an additional DNS-name rule that top-level domain names should not be all-numeric.
    # We are assuming here that we have in hand either either an fully-unqualified hostname ("host")
    # or a fully-qualified hostname (e.g., "host.subdomain.topdomain"), but never a partially-qualified
    # hostname ("host.subdomain"), and also not an IPv4 address.
    #
    return $outcome if $hostname =~ m{\.[0-9]+$};

    $outcome = 1;
    return $outcome;
}

sub have_valid_hostnames {
    my $hostnames = shift;
    my $outcome   = 0;

    foreach my $hostname (@$hostnames) {
	return $outcome if not is_valid_hostname($hostname);
    }

    $outcome = 1;
    return $outcome;
}

sub is_valid_ip_address {
    my $ip_address = shift;
    my $outcome    = 0;

    return $outcome if not defined $ip_address;
    return $outcome if $ip_address !~ m{^$RE{net}{IPv4}$} and $ip_address !~ m{^$RE{net}{IPv6}$};

    $outcome = 1;
    return $outcome;
}

sub have_valid_ip_addresses {
    my $ip_addresses = shift;
    my $outcome      = 0;

    foreach my $ip_address (@$ip_addresses) {
	return $outcome if not is_valid_ip_address($ip_address);
    }

    $outcome = 1;
    return $outcome;
}

sub is_valid_mac_address {
    my $mac_address = shift;
    my $outcome     = 0;

    return $outcome if not defined $mac_address;

    # This pattern does not necessarily match all possible forms of MAC address.  But it should
    # be good enough to match the forms that are generated by the client-side code that feeds our
    # discovery results.  If it doesn't, we should probably insist on normalizing the MAC address
    # in the upstream code instead of trying to accept variant forms here.  That could simplify
    # downstream processing.  See NetAddr::MAC and in particular its mac_as_ieee($mac) routine.
    #
    return $outcome if $mac_address !~ m{^\p{AHex}{2}([-:])(?:\p{AHex}{2}\1){4}\p{AHex}{2}$};

    $outcome = 1;
    return $outcome;
}

sub have_valid_mac_addresses {
    my $mac_addresses = shift;
    my $outcome       = 0;

    foreach my $mac_address (@$mac_addresses) {
	return $outcome if not is_valid_mac_address($mac_address);
    }

    $outcome = 1;
    return $outcome;
}

sub is_valid_profile_name {
    my $profile_name = shift;
    my $outcome      = 1;

    $outcome = 0 if $profile_name eq '';

    ## FIX MAJOR:  validate further against a standard Monarch profile-name pattern

    return $outcome;
}

sub is_valid_service_name {
    my $service_name = shift;
    my $outcome      = 1;

    $outcome = 0 if $service_name eq '';

    ## FIX MAJOR:  validate further against a standard Monarch service-name pattern

    return $outcome;
}

# This routine must return a hostname, a coordinated host alias, and a coordinated IP address.
#
# FIX MAJOR:  We might need to accept more info in the discovery results (potentially, both multiple hostnames found
# on the client machine and multiple associated IP addresses).  Also, we might need to take in some server-side
# configuration rules, to specify how to evaluate all of that data if some standard rules can be applied, and whether
# some custom logic must be invoked if such rules do not provide enough flexibility for some customer situation.
#
sub decide_host_identity {
    my $self                   = shift;
    my $discovery_results      = shift;
    my $hostname_qualification = shift;
    my $force_hostname_case    = shift;
    my $force_domainname_case  = shift;
    my $host_address_choices   = shift;
    my $decided_hostname       = undef;
    my $decided_alias          = undef;
    my $decided_address        = undef;

    ## FIX MAJOR:  Fill this in with as much logic as we need to take the hostname specified
    ## in discovery results and turn it into a final hostname to be used throughout the
    ## processing of the discovery results, including modifications to Monarch.  Modify the
    ## signature of this routine as much as necessary to include configuration options such
    ## as these:
    ##
    ##     hostname_qualification
    ##     <hardcoded_hostnames>
    ##     customer_network_package
    ##     compare_to_foundation_hosts
    ##     match_case_insensitive_foundation_hosts
    ##     force_hostname_case
    ##     force_domainname_case
    ##
    ## and perhaps others in the future.  Best is if we can avoid consulting Monarch in this
    ## work, but I'll withhold judgment on that until we have the final logic in place here.
    ##
    ## In the following, an incoming forced hostname overrides config-file options which might
    ## attempt to alter the form of the hostname.  This is intentional, as it both leaves the
    ## exact choice in the hands of the customer and allows a hostname determination which is
    ## fed back to the GDMA client to remain stable over time even if the server-side option
    ## values get changed.  The customer can still change such a hostname by deleting the
    ## client-side gdma_override.conf file and re-running discovery.
    ##
    $decided_hostname = $discovery_results->{forced_hostname};
    if ( !defined($decided_hostname) || $decided_hostname eq '' ) {
	$decided_hostname = $discovery_results->{hostnames}[0];
	$decided_hostname = undef if defined($decided_hostname) && $decided_hostname eq '';
	if ( defined $decided_hostname ) {
	    if ( defined $hostname_qualification ) {
		if ( $hostname_qualification eq 'full' ) {
		    ## There is nothing to do here; just accept a FQDN if we have one in hand.  Or maybe in
		    ## some future version, walk the @{$discovery_results->{hostnames}} array and select the
		    ## first FQDN, in case the first value in the array happens not to be fully qualified.
		    ## Or maybe take the first entry and locally turn it into a FQDN based on server-side
		    ## calculations if it's not already a FQDN.
		}
		elsif ( $hostname_qualification eq 'short' ) {
		    $decided_hostname =~ s{.\.*}{};
		}
		elsif ( $hostname_qualification eq 'custom' ) {
		    ## FIX MAJOR:  This case is not yet handled.  I suppose we ought to return an error
		    ## indication in that case, in the same way that we may need to check the result of
		    ## calling a custom package and return an error condition if it failed.
		}
	    }
	    if ( $decided_hostname =~ m{([^.]+)(?:\.(.+))?} ) {
		my $host   = $1;
		my $domain = $2;
		if ( defined $force_hostname_case ) {
		    if ( $force_hostname_case eq 'lower' ) {
			$host = lc $host;
		    }
		    elsif ( $force_hostname_case eq 'upper' ) {
			$host = uc $host;
		    }
		}
		if ( defined($force_domainname_case) && defined($domain) ) {
		    if ( $force_domainname_case eq 'lower' ) {
			$domain = lc $domain;
		    }
		    elsif ( $force_domainname_case eq 'upper' ) {
			$domain = uc $domain;
		    }
		}
		$decided_hostname = $host;
		$decided_hostname .= ".$domain" if defined $domain;
	    }
	}
    }
    $decided_alias = $decided_hostname;

    my $ipv4_cidr_block = qr{ (\d{1,3}) \. (\d{1,3}) \. (\d{1,3}) \. (\d{1,3}) / (\d{1,2}) }x;
    my $ipv6_cidr_block = qr{ ( (?: [[:xdigit:]]{1,4})? (?: :{1,2} [[:xdigit:]]{1,4} ){0,7} (?: ::)? ) / (\d{1,3}) }x;

    foreach my $choice (@$host_address_choices) {
	if ( $choice =~ m{^$ipv4_cidr_block$}o ) {
	    my $cidr = NetAddr::IP::Lite->new($choice);
	    foreach my $ip_address ( @{ $discovery_results->{ip_addresses} } ) {
		## First check to make sure it's an IPv4 address we're comparing the CIDR block to.
		if ( $ip_address =~ m{\.} ) {
		    my $ip = NetAddr::IP::Lite->new($ip_address);
		    if ( $cidr->contains($ip) ) {
			$decided_address = $ip_address;
			last;
		    }
		}
	    }
	}
	elsif ( $choice =~ m{^$ipv6_cidr_block$}o ) {
	    my $cidr = NetAddr::IP::Lite->new($choice);
	    foreach my $ip_address ( @{ $discovery_results->{ip_addresses} } ) {
		## First check to make sure it's an IPv6 address we're comparing the CIDR block to.
		if ( $ip_address =~ m{:} ) {
		    my $ip = NetAddr::IP::Lite->new($ip_address);
		    if ( $cidr->contains($ip) ) {
			$decided_address = $ip_address;
			last;
		    }
		}
	    }
	}
	elsif ( $choice eq 'mac_address' ) {
	    $decided_address = $discovery_results->{mac_addresses}[0] if defined $discovery_results->{mac_addresses};
	}
	elsif ( $choice eq 'hostname' ) {
	    $decided_address = $decided_hostname;
	}
	elsif ( $choice eq 'custom' ) {
	    $self->{logger}->error("ERROR:  Custom logic for host address selection is not yet supported.");
	    ## For the moment, we will just skip to the next choice if there is one, rather than aborting.
	}
	else {
	    ## The choices should have been validated earlier, so we should never get here.
	    $self->{logger}->error("ERROR:  Internal problem; invalid host address selection choice ($choice).");
	    last;
	}
	last if defined $decided_address;
    }

    return $decided_hostname, $decided_alias, $decided_address;
}

# FIX MAJOR:  compare to the install_file_safely() routine in autosetup
#
# FIX MAJOR:  use our full safety protocol (lock, temporary file, atomic rename)
# The lockfile probably has the name "auto_discovery_lock" and would reside in the
# same directory as the $filepath.
#
# FIX MAJOR:  only do locking if we don't think this whole operation is already
# under the protection of a lock
#
# FIX LATER:  set the file-creation mode, either directly or effectively via umask?
# Probably we want to use sysopen() instead of open() to make that happen.
#
sub save_data_safely {
    my $self           = shift;
    my $filetype       = shift;
    my $string_to_save = shift;
    my $filepath       = shift;
    my $outcome        = 1;

    my $temp_filepath = "$filepath.tmp";

    # FIX MAJOR:  should we control the permissions on the created file?
    # FIX MAJOR:  should we ensure we do not clobber an existing file?
    $outcome = 0 if not open SAVEDFILE, '>', $temp_filepath;
    if ( not $outcome ) {
	$self->{logger}->error("ERROR:  Cannot open the $filetype file '$temp_filepath' ($!).");
	return $outcome;
    }

    $outcome = 0 if not print SAVEDFILE $string_to_save;
    $outcome = 0 if not close SAVEDFILE;
    if ( not $outcome ) {
	$self->{logger}->error("ERROR:  Cannot write to the $filetype file '$temp_filepath' ($!).");
	unlink $temp_filepath;
	return $outcome;
    }

    $outcome = 0 if not rename $temp_filepath, $filepath;
    if ( not $outcome ) {
	my $os_error = "$!";
	$os_error .= " ($^E)" if "$^E" ne "$!";
	$self->{logger}->error("ERROR:  Cannot rename the $filetype file '$temp_filepath' ($os_error).");
	unlink $temp_filepath;
	return $outcome;
    }

    return $outcome;
}

sub read_discovery_analysis {
    my $self               = shift;
    my $analysis_file      = shift;
    my $discovery_analysis = undef;
    my $outcome            = 0;

    if ( not open ANALYSIS, '<', $analysis_file ) {
	$self->{logger}->error("ERROR:  Cannot open the analysis file \"$analysis_file\" ($!).");
	return $outcome, $discovery_analysis;
    }

    do {
	## Slurp the whole file, up to a limit.
	local $/ = \$self->{max_slurp_size};
	$discovery_analysis = readline ANALYSIS;
    };

    if ( not defined $discovery_analysis ) {
	$self->{logger}->error("ERROR:  Cannot read the analysis file \"$analysis_file\" ($!).");
	close ANALYSIS;
	return $outcome, $discovery_analysis;
    }

    if ( not close ANALYSIS ) {
	$self->{logger}->error("ERROR:  Problem encountered while closing the analysis file \"$analysis_file\" ($!).");
	return $outcome, $discovery_analysis;
    }

    my $len = do { use bytes; length $discovery_analysis; };
    if ( $len >= $self->{max_slurp_size} ) {
	$self->{logger}->error("ERROR:  The discovery analysis file is too large to process.");
	return $outcome, $discovery_analysis;
    }

    $outcome = 1;
    return $outcome, $discovery_analysis;
}

1;

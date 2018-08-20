package CommitTests;

# This package is designed to run various tests of Monarch Commit functionality.

# Copyright 2014 GroundWork Open Source, Inc. ("GroundWork")
# All rights reserved.  Use is subject to GroundWork commercial license terms.

# Revision History:
#
# 2014-07-10 GH 0.0.0	Original version.
# 2014-07-14 GH 0.0.1	Extended the sleep time after a Commit before checking the
#			postconditions, because testing showed that 1 second was
#			not always sufficient to get changes flushed to gwcollagedb.
# 2014-07-15 GH 0.0.2	Implement and enable by default the collection of timing
#			data for both audit and commit phases of a full Commit.
# 2014-07-18 GH 0.0.3	Extend the period for a sleep after Commit to operate more
#			reliably in a VM guest context.
# 2014-07-24 GH 0.0.4	Add support for starting PostgreSQL, in case we are running
#			these tests from a dead stop.

# Notes:
# * The coding in this package is not for the faint of heart.  It uses existing
#   Test::Class capabilities in clean and clever ways to provide a selection of
#   possible tests to run.  But it does make a good pattern to follow for future
#   test scripts.
# * One important aspect of the application of Test::Class to these tests is the
#   use of a common pair of setup and teardown methods for all individual logical
#   tests, and just one actual Monarch Commit operation.  This is critical for
#   Commit testing because the process of setup is so slow, involving loading of
#   databases and bouncing gwservices around that.  Hence our test databases are
#   constructed to run a bunch of individual tests all in parallel, not interfering
#   with one another in the sets of objects they reference, so a single set of
#   loaded databases suffices for the full test set.
# * The Commit processing itself must run as the nagios user, in order that it be able
#   to create directories and files as that user as it would in production.  But some
#   parts of testing setup require root access in order to stop or start major system
#   components.  The script is equipped to shift back and forth between effective user
#   IDs as needed, as long as it is started as root.
# * Invoke this script as root using our separate "verify" tool, using commands like:
#       verify CommitTests help                         # list usage and all defined virtual tests
#       verify CommitTests                              # run all defined tests
#       verify CommitTests {dbuser} {dbpass} all        # same thing
#       verify CommitTests {dbuser} {dbpass} addition   # run all tests that involve object addition
#   where {dbuser} is "postgres" and {dbpass} is that user's database password.  The
#   database-access credentials are needed in order to load the gwcollagedb database,
#   because some of the objects in that database are owned by the "postgres" user.
#   (The monarch database has no such problem.)
# * An alternative invocation is:
#       /usr/local/groundwork/perl/bin/prove -v verify :: CommitTests postgres {dbpass}
#   which has the benefit of appending a few summary lines, but the disadvantage that
#   it prepends all STDERR output to the transcript instead of presenting that data
#   in-line with the test results.
# * Success or failure of the tests as a whole can be determined from the exit status
#   of the "verify" tool, which reflects the number of tests that failed.  A zero exit
#   status means that all chosen tests passed; a non-zero status means that some failed.

# ================================ Perl Setup ================================

use strict;
use warnings;

# FIX LATER:  Note that an attempt to use reftype() in development testing within this
# script got me some kind of reference to a Test:: -hierarchy or other variant of that
# routine, not the one I was looking for.  If you need to use that routine, you must
# check to ensure that the code is really doing what you think it should be doing.
use attributes;  # to provide attributes::reftype() [Programming Perl, 4/e, p. 1003]

# ================================ Package Setup ================================

our ( @ISA, @EXPORT, @EXPORT_OK, %EXPORT_TAGS, $VERSION );

BEGIN {
    use Exporter ();
    @ISA         = qw(Exporter);
    @EXPORT      = qw();
    @EXPORT_OK   = qw();
    %EXPORT_TAGS = ( DEBUG => [ @EXPORT, @EXPORT_OK ] );
    $VERSION     = "0.0.4";
}

# ================================ Modules ================================

# This declaration must come after our standard BEGIN block just above.
use parent 'Test::Class';

# We define our own subroutine attributes to track the number of test clauses
# in various routines that are not invoked directly by Test::Class.
use Attribute::Handlers;

use Cwd 'realpath';
use Test::Most;

use Data::Dumper;    # For debugging for when Smart::Comments doesn't hack it.
$Data::Dumper::Indent   = 1;
$Data::Dumper::Sortkeys = 1;

use GW::Test 'log';

use MonarchStorProc;
use MonarchAudit;

# ================================ Variables ================================

# ================================ Routines ================================

# These routines are adapted from Test::Class, to support our Check(#) subroutine attributes.

# FIX MINOR:  Consider migrating this construction into GW::Test, to make it easily accessible
# to future test packages.  Doing so would probably mean that CommitTests would probably need to
# be a subclass of GW::Test, which would probably have an impact on all of the $self->{GW_Test}
# references throughout this package.  But can it be such a subclass if it's already a subclass
# of Test::Class ?

my $Checks = {};

# FIX LATER:  perhaps tailor this to our own needs
sub _parse_attribute_args {
    my $args = shift || '';
    my $num_tests;
    my $type;
    $args =~ s/\s+//sg;
    foreach my $arg (split /=>/, $args) {
	if (Test::Class::MethodInfo->is_num_tests($arg)) {
	    $num_tests = $arg;
	} elsif (Test::Class::MethodInfo->is_method_type($arg)) {
	    $type = $arg;
	} else {
	    die 'bad attribute args';
	};
    };
    return( $type, $num_tests );
};

sub add_checkinfo {
    my ( $class, $name, $type, $num_tests ) = @_;
    if ( $name =~ /^check_(.+)_(preconditions|analysis|postconditions)$/ ) {
	my $check = $1;
	my $phase = $2;
	$num_tests = 0 if not $num_tests;
	$Checks->{$class}->{$phase}->{$check} = $num_tests;
	return 1;
    }
    else {
	return 0;
    }
}

sub Check : ATTR(CODE,RAWDATA) {
    my ( $class, $symbol, $code_ref, $attr, $args ) = @_;
    if ( $symbol eq "ANON" ) {
	warn "cannot check anonymous subs - you probably loaded a "
	  . __PACKAGE__
	  . " package too late (after the CHECK block was run). See 'A NOTE ON LOADING TEST CLASSES' in perldoc Test::Class for more details\n";
    }
    else {
	my $name = *{$symbol}{NAME};
	warn "overriding public method $name with a test method in $class\n" if Test::Class::_is_public_method( $class, $name );
	eval { $class->add_checkinfo( $name, _parse_attribute_args($args) ) } || warn "bad test definition '$args' in $class->$name\n";
    }
}

# ================================================================

# This package has only one "Test" routine advertised to Test::Class.  It runs
# the critical operation common to all the other tests (Commit), doing so in two
# phases so it can examine the output of the first phase before proceeding with
# the second phase.  All validation of preconditions for specific "tests" is
# handled in the setup routine.  All validation of postconditions for specific
# "tests" (verification that the Commit worked as expected in all details) is
# handled in the teardown routine.  So all filtering of what behaviors are
# to be tested by running this class's Test method must be handled outside
# of the Test::Class test filtering.  Instead, the calling application must
# pass some notion of "what to test" to the constructor (and ultimately to the
# initializer) of this class, whether that includes all available logical tests
# (if no specific tests are mentioned) or various subgroups of them.
#
# For purposes of distinguishing between what Test::Class calls a test and what
# we need to refer to here as a test, we use the following terms:
#
# * physical test:  a Test::Class test (here, just "run_commit_operation")
# *  logical test:  a specific type of validation known to the CommitTests class
# *  virtual test:  a useful group of logical tests; "all" includes all of them

my $test_filter = sub {
    my ( $test_class, $test_method ) = @_;
    return 1 if $test_class ne __PACKAGE__;
    return $test_method eq 'run_commit_operation';
};
Test::Class->add_filter($test_filter);

my %virtual_test_types = (
    all          => "The full set of tests.",
    deletion     => "Tests that involve object deletion.",
    direct       => "Tests that involve direct object deletion.",
    cascade      => "Tests that involve cascade object deletion.",
    static       => "Tests that involve static object membership deletion.",
    dynamic      => "Tests that involve dynamic object membership deletion.",
    addition     => "Tests that involve object addition.",
    inaction     => "Tests that involve leaving the configuration unchanged.",
    host         => "Tests that involve specific hosts.",
    service      => "Tests that involve specific host services.",
    hostgroup    => "Tests that involve specific hostgroups.",
    servicegroup => "Tests that involve specific service groups.",
    membership   => "Tests that involve group membership.",
    empty        => "Tests that involve possibly empty group membership (this is not a subset of membership).",
    new          => "Tests that involve newly created objects.",
    existing     => "Tests that involve existing objects.",
);

# This array provides the canonical ordering of virtual test types in a usage message.
my @supported_virtual_test_types =
  qw( all deletion direct cascade static dynamic addition inaction host service hostgroup servicegroup membership empty new existing );

# Let's verify that we do, in fact, mention all the supported virtual test types.
# This should ease maintenance somewhat by automatically checking our code.
do {
    my %supported_types = map { $_ => 1 } @supported_virtual_test_types;
    foreach my $type ( keys %virtual_test_types ) {
	die "FATAL:  Virtual test type \"$type\" is not mentioned as being supported," if not $supported_types{$type};
    }
    foreach my $type ( keys %supported_types ) {
	die "FATAL:  Virtual test type \"$type\" is claimed to be supported but is not," if not $virtual_test_types{$type};
    }
};

# FIX LATER:  Add additional run-time testing to verify that a hash like this exists
# for each of the @supported_virtual_test_types.
my %all_tests          = ();
my %deletion_tests     = ();
my %direct_tests       = ();
my %cascade_tests      = ();
my %static_tests       = ();
my %dynamic_tests      = ();
my %addition_tests     = ();
my %inaction_tests     = ();
my %host_tests         = ();
my %service_tests      = ();
my %hostgroup_tests    = ();
my %servicegroup_tests = ();
my %membership_tests   = ();
my %empty_tests        = ();
my %new_tests          = ();
my %existing_tests     = ();

# For the particular logical test names we happen to have chosen for this package, we could have
# generated these virtual test categories automatically by simply breaking up the logical test
# names into words at underscore characters, except for suppressing "non" as a virtual test
# category.  But that is just a happy accident, which might not hold in the general case for
# other test packages.  So we implement a fairly general mechanism, as a worked-out example
# that we can copy in the future.
#
# "all" need not be mentioned in this listing; every logical test is automatically included in that virtual test.
my %virtual_test_categories = (
    host_deletion                                                                                              => [qw(existing host deletion)],
    host_service_direct_deletion                                                                               => [qw(existing service direct deletion)],
    host_service_cascade_deletion                                                                              => [qw(existing service cascade deletion)],
    hostgroup_deletion                                                                                         => [qw(existing hostgroup deletion)],
    servicegroup_deletion                                                                                      => [qw(existing servicegroup deletion)],
    hostgroup_membership_deletion_with_static_non_empty_result                                                 => [qw(existing hostgroup membership empty deletion)],
    hostgroup_membership_deletion_with_dynamic_non_empty_result                                                => [qw(existing hostgroup membership static empty deletion)],
    hostgroup_membership_deletion_with_empty_result                                                            => [qw(existing hostgroup membership dynamic empty deletion)],
    servicegroup_membership_deletion_with_static_non_empty_result                                              => [qw(existing servicegroup membership empty deletion)],
    servicegroup_membership_deletion_with_dynamic_non_empty_result                                             => [qw(existing servicegroup membership static empty deletion)],
    servicegroup_membership_deletion_with_empty_result                                                         => [qw(existing servicegroup membership dynamic empty deletion)],
    new_host_addition                                                                                          => [qw(new host addition)],
    new_host_service_addition                                                                                  => [qw(new service addition)],
    non_empty_new_hostgroup_addition                                                                           => [qw(new hostgroup empty addition)],
    empty_new_hostgroup_addition                                                                               => [qw(new hostgroup empty addition)],
    non_empty_new_servicegroup_addition                                                                        => [qw(new servicegroup empty addition)],
    empty_new_servicegroup_addition                                                                            => [qw(new servicegroup empty addition)],
    existing_host_existing_hostgroup_hostgroup_membership_addition_with_dynamic_non_empty_result               => [qw(existing host hostgroup membership addition)],
    new_host_existing_hostgroup_hostgroup_membership_addition_with_dynamic_non_empty_result                    => [qw(new host existing hostgroup membership addition)],
    existing_host_new_hostgroup_hostgroup_membership_addition                                                  => [qw(existing host new hostgroup membership addition)],
    new_host_new_hostgroup_hostgroup_membership_addition                                                       => [qw(new host hostgroup membership addition)],
    existing_host_service_existing_servicegroup_servicegroup_membership_addition_with_dynamic_non_empty_result => [qw(existing service servicegroup membership dynamic empty addition)],
    new_host_service_existing_servicegroup_servicegroup_membership_addition_with_dynamic_non_empty_result      => [qw(new service existing servicegroup membership dynamic empty addition)],
    existing_host_service_new_servicegroup_servicegroup_membership_addition                                    => [qw(existing service new servicegroup membership addition)],
    new_host_service_new_servicegroup_servicegroup_membership_addition                                         => [qw(new service servicegroup membership addition)],
    host_inaction                                                                                              => [qw(existing host inaction)],
    host_service_inaction                                                                                      => [qw(existing service inaction)],
    hostgroup_inaction                                                                                         => [qw(existing hostgroup inaction)],
    servicegroup_inaction                                                                                      => [qw(existing servicegroup inaction)],
);

# The specific object names listed here are documented in our "Monarch Commit Operation"
# article ( https://kb.groundworkopensource.com/display/GWENG/Monarch+Commit+Operation ).
#
# Note:  A number of these tests may also involve certain secondary objects whose presence
# and pre-test configuration are currently not being explicitly verified, though they might
# be indirectly verified (such as checking that an object group is not empty, without naming
# the specific object group members).  So these lists of object names may be extended over
# time, as we solidify the full test procedures.
my %logical_test_arguments = (
    host_deletion                                                                                              => [qw(host-001)],
    host_service_direct_deletion                                                                               => [qw(host-002 service-002)],
    host_service_cascade_deletion                                                                              => [qw(host-003 service-003)],
    hostgroup_deletion                                                                                         => [qw(hostgroup-004)],
    servicegroup_deletion                                                                                      => [qw(servicegroup-005)],
    hostgroup_membership_deletion_with_static_non_empty_result                                                 => [qw(host-006-a host-006-b hostgroup-006)],
    hostgroup_membership_deletion_with_dynamic_non_empty_result                                                => [qw(host-007-a host-007-b hostgroup-007)],
    hostgroup_membership_deletion_with_empty_result                                                            => [qw(host-008-a hostgroup-008)],
    servicegroup_membership_deletion_with_static_non_empty_result                                              => [qw(host-009-a service-009-a host-009-b service-009-b servicegroup-009)],
    servicegroup_membership_deletion_with_dynamic_non_empty_result                                             => [qw(host-010-a service-010-a host-010-b service-010-b servicegroup-010)],
    servicegroup_membership_deletion_with_empty_result                                                         => [qw(host-011-a service-011-a servicegroup-011)],
    new_host_addition                                                                                          => [qw(host-031)],
    new_host_service_addition                                                                                  => [qw(host-032 service-032)],
    non_empty_new_hostgroup_addition                                                                           => [qw(host-033 hostgroup-033)],
    empty_new_hostgroup_addition                                                                               => [qw(hostgroup-034)],
    non_empty_new_servicegroup_addition                                                                        => [qw(host-035 service-035 servicegroup-035)],
    empty_new_servicegroup_addition                                                                            => [qw(servicegroup-036)],
    existing_host_existing_hostgroup_hostgroup_membership_addition_with_dynamic_non_empty_result               => [qw(host-037-a host-037-b hostgroup-037)],
    new_host_existing_hostgroup_hostgroup_membership_addition_with_dynamic_non_empty_result                    => [qw(host-038-a host-038-b hostgroup-038)],
    existing_host_new_hostgroup_hostgroup_membership_addition                                                  => [qw(host-039 hostgroup-039)],
    new_host_new_hostgroup_hostgroup_membership_addition                                                       => [qw(host-040 hostgroup-040)],
    existing_host_service_existing_servicegroup_servicegroup_membership_addition_with_dynamic_non_empty_result => [qw(host-041-a service-041-a host-041-b service-041-b servicegroup-041)],
    new_host_service_existing_servicegroup_servicegroup_membership_addition_with_dynamic_non_empty_result      => [qw(host-042-a service-042-a host-042-b service-042-b servicegroup-042)],
    existing_host_service_new_servicegroup_servicegroup_membership_addition                                    => [qw(host-043 service-043 servicegroup-043)],
    new_host_service_new_servicegroup_servicegroup_membership_addition                                         => [qw(host-044 service-044 servicegroup-044)],
    host_inaction                                                                                              => [qw(host-061)],
    host_service_inaction                                                                                      => [qw(host-062 service-062)],
    hostgroup_inaction                                                                                         => [qw(hostgroup-063)],
    servicegroup_inaction                                                                                      => [qw(servicegroup-064)],
);

sub print_usage {
    print "help:      verify CommitTests help                         # list usage and all defined virtual tests\n";
    print "usage:     verify CommitTests {dbuser} {dbpass} [{tests} [{monarch_file} [{gwcollagedb_file} [{monarch_home_path}]]]]\n";
    print "examples:  verify CommitTests {dbuser} {dbpass}            # run all defined tests, using config-file and specified DB credentials\n";
    print "           verify CommitTests {dbuser} {dbpass} all        # same thing\n";
    print "           verify CommitTests {dbuser} {dbpass} addition   # run all tests that involve object addition\n";
}

# ================================================================

sub new {
    my $invocant = shift;
    my $class = ref($invocant) || $invocant;    # object or class name

    # This being a subclass, we first have to ensure that the base class is properly initialized.
    my $self = bless( {}, $class )->SUPER::new();

    # Then we need to ensure that this subclass is properly initialized.
    $self->initialize(@_);

    return $self;
}

# NOTE:  The monarch_home_path is used to ensure that Nagios is not started after a Commit, by ensuring that
# the standard $monarch_home/bin/nagios_reload call to restart Nagios after a Commit is not used to do so.
# This in turn ensures that Nagios does not send data to Foundation which causes it to auto-vivify certain
# objects which might not have been correctly populated in Foundation by the Sync operation (part of Commit)
# that we are testing here.  This subterfuge allows us not to restart Nagios, while not having to damage the
# GroundWork installation in any way that would prevent its subsequent use if we somehow did not undo that
# damage at the end of the test run.
sub initialize {
    my $self = shift;

    my ( $dbuser, $dbpass, $testnames, $monarch_db_dumppath, $gwcollagedb_db_dumppath, $monarch_home_path ) = @_;

    if ( not defined($dbuser) ) {
	print_usage();
	die "\n";
    }
    elsif ( $dbuser eq 'help' ) {
	$dbuser    = undef;
	$dbpass    = undef;
	$testnames = 'help';
    }
    elsif ( not defined($dbpass) ) {
	print_usage();
	die "\n";
    }

    # Lots of gory details are delegated to a sub-package so we can deal more abstractly
    # here in this test script.
    my $GW_Test = GW::Test->new( { verbose => 0 } );

    # Both as a general precaution (so we don't take dangerous actions while running with
    # elevated privileges), and because we know we will be writing out some files during
    # the Commit operation, we intentionally restrict our privilege level here.
    $GW_Test->run_as_effective_nagios();

    $monarch_db_dumppath     ||= 'commit-pre-test-monarch.sql';
    $gwcollagedb_db_dumppath ||= 'commit-pre-test-gwcollagedb.sql';

    # This path is a replacement for /usr/local/groundwork/core/monarch for test purposes.
    my $abs_path = realpath( $monarch_home_path || '/tmp/monarch' );

    # Validate that the absolute path starts with a sensible path, to avoid symlink
    # or parent-directory references sidestepping our security precautions.
    die "FATAL:  The specified Monarch home path ($monarch_home_path) does not exist.\n" if not defined $abs_path;
    ## Since this path will be used to write into the filesystem, validate that it will not overwrite any critical files.
    die "FATAL:  The specified Monarch home path ($monarch_home_path) does not reside under the /tmp/ directory.\n"
      if $abs_path !~ m{^/tmp/.};
    $monarch_home_path = $abs_path;

    # We wish to support names of particular individual tests (here called logical tests),
    # names of useful groups of individual tests (these groupings here called virtual tests),
    # or arbitrary mixtures of logical and virtual tests.
    if (not defined($testnames)) {
	$testnames = [];
    }
    elsif (ref $testnames ne 'ARRAY') {
	$testnames = [ split /[ ,]/, $testnames ];
    }
    $testnames = [ 'all' ] if not @$testnames;

    my %virtual_test_type_hashref = ();
    foreach my $virtual_test_type (keys %virtual_test_types) {
	$virtual_test_type_hashref{$virtual_test_type} = eval "\\%${virtual_test_type}_tests";
	die $@ if $@;
    }
    foreach my $logical_test ( keys %virtual_test_categories ) {
	foreach my $virtual_test_type ( @{ $virtual_test_categories{$logical_test} } ) {
	    ${ $virtual_test_type_hashref{$virtual_test_type} }{$logical_test} = 1;
	}
	${ $virtual_test_type_hashref{all} }{$logical_test} = 1;
    }

    # For diagnostic purposes, to prove that our logic above works to group logical tests into appropriate virtual tests.
    # Perhaps we should emit this output under some sort of debug level of logging.
    if (0) {
	foreach my $virtual_test_type (keys %virtual_test_types) {
	    print "virtual test \"$virtual_test_type\":\n";
	    foreach my $logical_test ( keys %{ $virtual_test_type_hashref{$virtual_test_type} } ) {
		print "    $logical_test\n";
	    }
	}
    }

    my %logicaltests = ();
    foreach my $testname (@$testnames) {
	if ( $testname eq 'help' ) {
	    print_usage();
	    print "Supported virtual test types:\n";
	    my $longest_name_string_len = 0;
	    foreach my $virtual_test_type (@supported_virtual_test_types) {
		my $len = length $virtual_test_type;
		$longest_name_string_len = $len if $len > $longest_name_string_len;
	    }
	    foreach my $virtual_test_type (@supported_virtual_test_types) {
		print "    $virtual_test_type"
		  . ( " " x ( $longest_name_string_len - length($virtual_test_type) ) )
		  . "  $virtual_test_types{$virtual_test_type}\n";
	    }
	    die "\n";
	}
	elsif ( $virtual_test_types{$testname} ) {
	    ## We have a known virtual test name.
	    my $virtual_test_hashref = eval "\\%${testname}_tests";
	    die $@ if $@;
	    @logicaltests{ keys %$virtual_test_hashref } = (1) x scalar keys %$virtual_test_hashref;
	}
	elsif ( $virtual_test_categories{$testname} ) {
	    ## We have a known logical test name.
	    $logicaltests{$testname} = 1;
	}
	else {
	    die "ERROR:  \"$testname\" is not a known virtual or logical test name in the ".__PACKAGE__." package.\n";
	}
    }

    # We presume that none of these hash keys conflict in any way with those
    # that are being maintained in the same hash by the Test::Class superclass.

    # NOTE:  Experience shows that on a bare-metal machine, the sleep_seconds_after_commit setting
    # must be at least 1 second, and sometimes longer, to allow time for Foundation to fully
    # percolate group-membership changes into gwcollagedb, at least when the Commit operation is
    # using the Foundation Socket API to inject changes into the gwcollagedb database.  That's
    # because our Socket-API Commit operation has no full handshake with Foundation, and it is not
    # validating any of the group-membership conditions before it completes.  The situation might
    # be different when the GW::RAPID API is used, but we won't know that until we have converted
    # Commit to support that mode of operation.
    #
    # On a VM guest, even 2 seconds is insufficient for the related group-membership postconditions
    # to be reliably sensed as expected.  So we have set that period to be much longer.  The exact
    # setting for this parameter will always be an ad-hoc adjustment, as long as we are testing the
    # Foundation Socket API.

    $self->{GW_Test}                      = $GW_Test;
    $self->{monarch_existing_db_dumppath} = '/tmp/monarch-commit-test-prior-monarch-db.sql';
    $self->{monarch_db_dumppath}          = $monarch_db_dumppath;
    $self->{gwcollagedb_db_dumppath}      = $gwcollagedb_db_dumppath;
    $self->{monarch_home_path}            = $monarch_home_path;
    $self->{dbuser}                       = $dbuser;
    $self->{dbpass}                       = $dbpass;
    $self->{testnames}                    = $testnames;
    $self->{logicaltests}                 = [ sort keys %logicaltests ];
    $self->{start_postgresql}             = 1;                             # can be disabled for development testing
    $self->{save_and_restore_setup}       = 1;                             # can be disabled for development testing
    $self->{report_detail}                = 1;                             # can be disabled for development testing
    $self->{stop_nagios}                  = 1;                             # can be disabled for development testing
    $self->{start_and_stop_gwservices}    = 1;                             # can be disabled for development testing
    $self->{load_databases}               = 1;                             # can be disabled for development testing
    $self->{check_preconditions}          = 1;                             # can be disabled for development testing
    $self->{show_timing}                  = 1;                             # can be disabled for development testing
    $self->{run_audit_phase}              = 1;                             # can be disabled for development testing
    $self->{check_audit_analysis}         = 1;                             # can be disabled for development testing
    $self->{run_commit_phase}             = 1;                             # can be disabled for development testing
    $self->{sleep_seconds_after_commit}   = 10;                            # can be disabled for development testing
    $self->{check_postconditions}         = 1;                             # can be disabled for development testing

    # We have lots of other i/o redirections elsewhere to try to capture all failure messages
    # in jus one i/o stream.  But they don't necessarily all work in all cases, so we need to
    # cover this Test::Builder setting as well.
    $self->builder->failure_output(\*STDOUT);

    # For development purposes only.
    if (0) {
	print Data::Dumper->Dump( [ $Checks ], [qw($Checks)] );
    }

    my $precondition_detail  = 0;
    my $analysis_detail      = 0;
    my $postcondition_detail = 0;
    my $package = __PACKAGE__;
    foreach my $logicaltest ( @{ $self->{logicaltests} } ) {
	$precondition_detail  += $Checks->{$package}->{preconditions}->{$logicaltest};
	$analysis_detail      += $Checks->{$package}->{analysis}->{$logicaltest};
	$postcondition_detail += $Checks->{$package}->{postconditions}->{$logicaltest};
    }

    # For development purposes only.
    if (0) {
	print "#  precondition detail:  $precondition_detail\n";
	print "#      analysis detail:  $analysis_detail\n";
	print "# postcondition detail:  $postcondition_detail\n";
	exit(1);
    }

    # FIX LATER:  Perhaps adjust the handle_commit_test_preconditions and run_commit_operation
    # actions so we log some skipped tests instead of just skipping them, if certain capabilities
    # are disabled.  (It would be a better implementation if Test::Most implemented the "Cleaner
    # skip()" construction which is documented as a possible future extension.)
    $self->num_method_tests( 'handle_commit_test_startup',
        3 + ( $self->{start_postgresql} ? 1 : 0 ) + ( $self->{save_and_restore_setup} ? 1 : 0 ) );
    $self->num_method_tests( 'handle_commit_test_preconditions',
	( $self->{stop_nagios}                 ? 1 : 0 ) +
	  ( $self->{start_and_stop_gwservices} ? 2 : 0 ) +
	  ( $self->{load_databases}            ? 2 : 0 ) +
	  ( $self->{check_preconditions} ? scalar @{ $self->{logicaltests} } + ( $self->{report_detail} ? $precondition_detail : 0 ) : 0 ) );
    $self->num_method_tests( 'run_commit_operation',
	( $self->{run_audit_phase}    ? 2 : 0 ) +
	  ( $self->{run_commit_phase} ? 6 : 0 ) +
	  scalar @{ $self->{logicaltests} } +
	  ( $self->{report_detail} ? $analysis_detail : 0 ) );
    $self->num_method_tests( 'handle_commit_test_postconditions',
	( $self->{check_postconditions} ? scalar @{ $self->{logicaltests} } + ( $self->{report_detail} ? $postcondition_detail : 0 ) : 0 ) );
    $self->num_method_tests( 'handle_commit_test_shutdown', 2 + ( $self->{save_and_restore_setup} ? 2 : 0 ) );

    return $self;
}

# ================================================================

# The two routines here are responsible for capturing the state of the system as it
# existed before we started running our tests, and putting it back to that same state
# if we manage to get all the way to the end without bailing out along the way.

# The only things we knowingly change in the system during our Commit tests are:
#
# * the "monarch" database
# * the "gwcollagedb" database
# * the nagios/etc/ files
#
# So the sensible thing to do is to capture the "monarch" database content before our
# testing begins, then restore it and run a Commit afterward to re-sync the "gwcollagedb"
# database.  This should also reset nagios/etc/ files to what they were at the start of
# our testing, so long as whatever scripting passed the system to us left it in a state
# where Monarch and Nagios still represented the same consistent setup.
#
# Alternately, we could just capture and restore the "gwcollagedb" database as well,
# especially if we also redirect the $nagios_etc directory during our testing Commit
# to not overwrite anything in the normal production part of the system.  Of course,
# if we try that, we will need to stop gwservices while we restore the "gwcollagedb"
# database, and start it afterward, which processing is terribly slow and certainly
# much slower than simply running a Commit against the restored "monarch" database.

sub save_existing_setup {
    my $self = shift;

    my %outcome = ();
    my @results = ();

    log( "\n", "Dumping monarch (this may take awhile) ..." );
    my $status = $self->{GW_Test}->dump_database( 'monarch', $self->{monarch_existing_db_dumppath}, {}, \%outcome, \@results );
    log(@results) if not $status;

    return $status;
}

sub restore_previous_setup {
    my $self = shift;

    my %outcome = ();
    my @results = ();

    log( "\n", "Loading monarch (this may take awhile) ..." );
    my $status = $self->{GW_Test}->load_database( $self->{monarch_db_dumppath}, 'monarch', {}, \%outcome, \@results );
    log(@results) if not $status;

    # Append to the logfile for this commit.
    $FoundationSync::logging = 2;

    $status = 0 if not $self->run_a_commit('reset');

    return $status;
}

# ================================================================

sub location {
    my $tag = shift;
    my ($package, $filename, $line, $subroutine) = caller(1);

    # This can be uncommented during development testing, as a means to identify exactly what
    # routine you're in or what action you're trying to execute when a particular event happens.
    ## print "$subroutine $tag\n";
}

sub handle_commit_test_startup : Test(startup) {
    location("entry");

    my $self = shift;

    # For some reason, a bail_on_fail() won't actually bail after a failure until the next
    # ok() is run, by which time that ok()'s arguments will have been evaluated, and confusing
    # output from additional calculations may have appeared in the output stream.  To prevent
    # that, we condition the evaluation of ok() arguments on the success of the previous test.
    my $last_status = 1;

    # If our startup fails, that's like a Nagios pre-flight failure, and there's no reason
    # to continue on and attempt a Commit operation.  Test::Class automatically handles this
    # situation in a startup routine and skips all remaining tests for this object, so we don't
    # need to call bail_on_fail(); explicitly here.  However, if we don't, we won't get a clear
    # "Bail out!  Test failed.  BAIL OUT!." message at the end of the output.  Also, without
    # this call, we will get "not ok" messages for tests that (purposely) didn't actually run
    # because of our $last_status processing.
    bail_on_fail();

    # Later on, we will need to be running as root in order to stop and start gwservices.
    # Since that is the case, we may as well just acknowledge that fact up front and stop
    # this run right away if we don't meet that minimum condition.

    # $self->{GW_Test}->log_ids();
    $last_status = ok( $last_status && $self->{GW_Test}->running_as_real_root(), 'check that we are running tests as root' );

    if ( $self->{start_postgresql} ) {
	$last_status && log( "\n", "Starting postgresql ..." );
	$last_status = ok( $last_status && $self->{GW_Test}->start_postgresql(), 'start postgresql' );
    }

    if ( $self->{save_and_restore_setup} ) {
	$last_status = ok( $last_status && $self->save_existing_setup(), 'save the existing setup' );
    }

    $last_status = ok( $last_status && $self->{GW_Test}->open_database('monarch'),     'open connection to the monarch database' );
    $last_status = ok( $last_status && $self->{GW_Test}->open_database('gwcollagedb'), 'open connection to the gwcollagedb database' );

    restore_fail;

    location("exit");
}

# Run all the requested logical-test setup actions.
sub handle_commit_test_preconditions : Tests(setup) {
    location("entry");

    my $self = shift;

    # For some reason, a bail_on_fail() won't actually bail after a failure until the next
    # ok() is run, by which time that ok()'s arguments will have been evaluated, and confusing
    # output from additional calculations may have appeared in the output stream.  To prevent
    # that, we condition the evaluation of ok() arguments on the success of the previous test.
    my $last_status = 1;

    # If our setup fails, that's like a Nagios pre-flight failure, and there's no reason
    # to continue on and attempt a Commit operation.
    bail_on_fail();

    STDOUT->autoflush(1);
    STDERR->autoflush(1);

    my %outcome = ();
    my @results = ();

    # gwservices must be running running for the bulk of the Commit tests, since we need to have
    # Foundation running to receive either Socket API XML data or REST API calls and respond to the
    # desired gwcollagedb transforms.  Note that this requirement means that Commit tests are not
    # really unit tests, but actually must be integration tests.  At best, we could run the auditing
    # phase alone as a set of unit tests, but that would only work if we remain with the original
    # implementation of that phase that reads the gwcollagedb directly (through CollageQuery calls)
    # instead of fetching Foundation data through its REST API.
    #
    # That said, we need to stop Foundation for the duration of loading the gwcollagedb database
    # with test data.

    # We need to stop Nagios in order to prevent it from sending any data to Foundation that
    # Foundation might use to auto-vivify some of our test objects before we intentionally
    # place them in Foundation by means of our Commit operation.  That could happen if the
    # current nagios/etc/... files happen to contain references to such test objects, perhaps
    # left over from some previous test run.
    if ( $self->{stop_nagios} ) {
	$last_status && log( "\n", "Stopping nagios ..." );
	$last_status = ok( $last_status && $self->{GW_Test}->stop_nagios(), 'stop nagios' );
    }

    # Stop gwservices, so we can load the databases without any interference from Hibernate.
    if ( $self->{start_and_stop_gwservices} ) {
	$last_status && log( "\n", "Stopping gwservices (this may take awhile) ..." );
	$last_status = ok( $last_status && $self->{GW_Test}->stop_gwservices(), 'stop gwservices' );
    }

    my %options = ();
    $options{dbuser} = $self->{dbuser} if $self->{dbuser};
    $options{dbpass} = $self->{dbpass} if $self->{dbpass};

    if ( $self->{load_databases} ) {
	$last_status && log( "\n", "Loading monarch (this may take awhile) ..." );
	$last_status = ok( $last_status && $self->{GW_Test}->load_database( $self->{monarch_db_dumppath}, 'monarch', {}, \%outcome, \@results ),
	    'load the monarch database' )
	  or log(@results);
	$last_status && log( "\n", "Loading gwcollagedb (this may take awhile) ..." );
	$last_status = ok(
	    $last_status && $self->{GW_Test}->load_database( $self->{gwcollagedb_db_dumppath}, 'gwcollagedb', \%options, \%outcome, \@results ),
	    'load the gwcollagedb database'
	) or log(@results);
    }

    # Start gwservices, so we can check preconditions that might possibly use Foundation to fetch data.
    if ( $self->{start_and_stop_gwservices} ) {
	$last_status && log( "\n", "Starting gwservices (this will take awhile) ..." );
	$last_status = ok( $last_status && $self->{GW_Test}->start_gwservices(), 'start gwservices' );
    }

    if ( $self->{check_preconditions} ) {
	foreach my $logicaltest ( @{ $self->{logicaltests} } ) {
	    location("test:  $logicaltest");
	    my $test_routine = "check_${logicaltest}_preconditions";
	    $last_status =
	      ok( $last_status && $self->$test_routine( @{ $logical_test_arguments{$logicaltest} } ),
		join( ' ', split( '_', $test_routine ) ) );
	}
    }

    restore_fail;

    location("exit");
}

sub check_commit_test_analyses {
    my $self   = shift;
    my $delta  = shift;
    my $status = 1;

    if ( $self->{check_audit_analysis} ) {
	foreach my $logicaltest ( @{ $self->{logicaltests} } ) {
	    location("test:  $logicaltest");
	    my $test_routine = "check_${logicaltest}_analysis";
	    $status = 0
	      if not ok( $self->$test_routine( $delta, @{ $logical_test_arguments{$logicaltest} } ), join( ' ', split( '_', $test_routine ) ) );

	    # This section is useful only while developing tests, to verify (after NOT having
	    # run an audit) that none of our GW::Test routines are significantly modifying the
	    # delta tree, thus potentially changing the results of later tests.
	    if (0) {
		if ( not ok( !defined($delta) || !%$delta, 'delta is still undefined or an empty hash' ) ) {
		    if ( defined $delta and %$delta ) {
			foreach my $key ( sort keys %$delta ) {
			    print "delta{$key} = $delta->{$key}\n";
			}
			die "delta became a non-empty hash during $test_routine\n";
		    }
		}
	    }
	}
    }

    return $status;
}

sub run_a_commit {
    location("entry");

    my $self        = shift;
    my $commit_type = shift;    # 'test' or 'reset'
    my $status      = 1;

    my $user_acct      = 'test_script';
    my $nagios_version = $self->{GW_Test}->monarch_config_value('nagios_version');
    my $nagios_etc     = $self->{GW_Test}->monarch_config_value('nagios_etc');
    my $nagios_bin     = $self->{GW_Test}->monarch_config_value('nagios_bin');
    my $monarch_home   = ( $commit_type eq 'test' && $self->{monarch_home_path} ) || $self->{GW_Test}->monarch_config_value('monarch_home');

    if ( $commit_type eq 'test' ) {
	## Clean up any previous copy of the entire $monarch_home file tree, so the following
	## attempts to reconstruct that tree will succeed.
	system "/bin/rm -rf $monarch_home" if $monarch_home ne $self->{GW_Test}->monarch_config_value('monarch_home');

	umask 022;
	$status = 0 if not ok( mkdir( $monarch_home,             0755 ), 'make the Monarch home directory' );
	$status = 0 if not ok( mkdir( "$monarch_home/workspace", 0755 ), 'make the Monarch workspace directory' );
	$status = 0 if not ok( mkdir( "$monarch_home/bin",       0755 ), 'make the Monarch bin directory' );

	# We create a fake nagios_reload script that generates appropriate output to make it look
	# as though the reload succeeded, without actually starting Nagios.
	print $self->{GW_Test}->execute_as_effective_user_and_group(qq(/bin/cp fake_nagios_reload $monarch_home/bin/nagios_reload));
	$status = 0 if not ok( $? == 0, 'copy fake nagios_reload script' );
	$status = 0 if not ok( chmod( 0755, "$monarch_home/bin/nagios_reload" ), 'set permissions on fake nagios_reload script' );
    }

    # DB connection.  It doesn't produce a useful return code for checking success.  But if it fails,
    # it is likely to die, which will kill this entire run_commit_operation Test, so we need not be
    # concerned that failure here will be overlooked.
    my $auth = StorProc->dbconnect();

    my ( $errors, $results, $timings ) =
      StorProc->synchronized_commit( $user_acct, $nagios_version, $nagios_etc, $nagios_bin, $monarch_home, '' );

    # There is likely no useful returned $result, but we wouldn't care much even if there were.
    my $result = StorProc->dbdisconnect();

    my @errors  = @{$errors};
    my @results = @{$results};

    # FIX LATER:  Perhaps check that the synchronized-commit timings are reasonable, as well.
    if (ref $timings eq 'ARRAY') {
	foreach my $period (@$timings) {
	    $period =~ s/ \[.+\]//;
	    print "# Commit $period\n";
	}
    }

    print "Commit results:\n" . join( "\n", @results ) . "\n" if @results;
    print "Errors during pre-flight or commit:\n" . join( "\n", @errors ) . "\n" if @errors;

    # FIX LATER:  This is perhaps a poor way to detect success or failure.  Figure out if there is some better way.
    my $nagios_ok_string = 'Good. Changes accepted by Nagios.';
    my $commit_ok_string = 'Synchronization with Foundation completed successfully.';
    ## We have to allow for the "Total Errors:" line in the Nagios output, and not count that as an error.
    my $sync_results_ok =
	 grep ( /\Q$nagios_ok_string\E/, @results )
      && grep ( /\Q$commit_ok_string\E/, @results )
      && grep( /Total Errors:/, @results ) == 1
      && grep( /error/i,        @results ) == 1;

    my $full_commit_status = ok( @errors == 0 && $sync_results_ok, 'run Monarch/Foundation full Commit operation' );
    $status = 0 if not $full_commit_status;

    if ( not $full_commit_status ) {
	$self->BAILOUT( $commit_type eq 'test'
	    ? 'Commit failed, so no postcondition checks are being attempted.'
	    : 'Commit failed, so the systemm is not back as it was before testing.' );
    }

    location("exit");
    return $status;
}

sub run_commit_operation : Tests {
    location("entry");

    my $self = shift;

    my $err_ref  = undef;
    my $time_ref = undef;
    my %delta    = ();
    my $phasetime;

    # If enabled in our initializer, have both audit and commit phases emit
    # timing information, which we will edit and dump into our own log output.
    $StorProc::show_timing = 1 if $self->{show_timing};

    # Note:  For some unknown reason, auditing both opens and closes the StorProc database handle
    # internal to its own operation.  So we don't have to do so here (though we ought to, if the
    # auditing behaved more reasonably).

    # Note:  This paragraph may be usefully disabled while developing tests, to verify
    # (after NOT having run an audit) that none of our GW::Test routines are significantly
    # modifying the delta tree, thus potentially changing the results of later tests.
    if ($self->{run_audit_phase}) {
	## Run an audit of Monarch/Foundation differences, with respect to Nagios-controlled resources.
	( $err_ref, $time_ref, %delta ) = Audit->foundation_sync();
	ok( @$err_ref == 0,              'run Monarch/Foundation audit phase of Commit without generating errors' );
	ok( exists $delta{'statistics'}, 'run Monarch/Foundation audit phase of Commit to completion' );

	# FIX LATER:  Perhaps check that the audit-phase timings are reasonable, as well.
	if (ref $time_ref eq 'ARRAY') {
	    foreach my $period (@$time_ref) {
		$period =~ s/ \[.+\]//;
		print "# Audit $period\n";
	    }
	}
    }

    if ( $self->check_commit_test_analyses( \%delta ) ) {
	if ( $self->{run_commit_phase} ) {
	    ## Start a new logfile for this commit.
	    $FoundationSync::logging = 1;

	    # Here we run the actual full Commit operation and report the overall result as a Test.
	    # It will internally run Nagios file generation, run a Nagios pre-flight, and re-run
	    # the auditing phase, but we don't care about those actions here.  We only care about
	    # the final sync to Foundation, which is what we will be checking in our postcondition
	    # tests.  We do this so we don't try to reach in to run the sync phase separately, both
	    # because that is not how Commit operations are run in practice ("test what you fly,
	    # fly what you test"), and because the earlier auditing delta may have been subtly
	    # altered by some degree of auto-vivification during the previous Tests.  (It shouldn't
	    # have been, and we can test for that to some degree in a development context, but
	    # in any case we're being ultra-safe here.)
	    $self->run_a_commit('test');

	    if ( $self->{sleep_seconds_after_commit} ) {
		log( "\n", "Sleeping for $self->{sleep_seconds_after_commit} seconds after Commit ..." );
		sleep( $self->{sleep_seconds_after_commit} );
	    }
	}
    }
    else {
	## Note that this aborts all testing immediately, which means neither the teardown
	## nor the shutdown routines will be run.  The database connections will be broken
	## abruptly by the script simply exiting rather than by polite disconnects.  If that
	## were a serious issue, we could implement some sort of finally() routine to handle
	## such cleanup, and call it here before bailing out.  But really, we don't care,
	## because PostgreSQL has to deal with possible broken client connections as a matter
	## of ordinary operation.

	# Let's give ourselves some clues for debugging, since by this time all the preconditions
	# have passed, and we can't just look in the database to see what went wrong.
	print Data::Dumper->Dump( [ \%delta ], [qw(\%delta)] );

	$self->BAILOUT('Auditing analysis failed, so no Commit operation is being attempted.');
    }

    location("exit");
}

# Run all the requested logical-test verification actions.
sub handle_commit_test_postconditions : Tests(teardown) {
    location("entry");

    my $self = shift;

    if ( $self->{check_postconditions} ) {
	foreach my $logicaltest ( @{ $self->{logicaltests} } ) {
	    location("test:  $logicaltest");
	    my $test_routine = "check_${logicaltest}_postconditions";
	    ok( $self->$test_routine( @{ $logical_test_arguments{$logicaltest} } ), join( ' ', split( '_', $test_routine ) ) );
	    print "ERROR:  $@" if $@;
	}
    }

    location("exit");
}

sub handle_commit_test_shutdown : Test(shutdown) {
    location("entry");

    my $self = shift;

    if ( $self->{save_and_restore_setup} ) {
	if ( $self->builder->is_passing() ) {
	    ok( $self->restore_previous_setup(), 'restore the previous setup' );
	}
	else {
	    $self->builder->skip('restoring the system to previous state because of earlier test failures');
	}
    }

    ok( $self->{GW_Test}->close_database('monarch'),     'close connection to the monarch database' );
    ok( $self->{GW_Test}->close_database('gwcollagedb'), 'close connection to the gwcollagedb database' );

    my $current_test   = $self->builder->current_test();
    my $expected_tests = $self->expected_tests();
    my $skipped_tests  = grep $_->{type} eq 'skip', $self->builder->details();

    done_testing();

    # If all tests passed, Test::Class doesn't produce any final summary output on its own.
    # So we do so here.
    #
    # The following summary information may be of use to the person who runs the tests.
    # We don't bother trying to compute any percent-passed statistics, because that sort
    # of thing is essentially meaningless in a context when one failure can cascade and
    # cause a huge number of secondary failures.  In the end, if any one test fails, you
    # had best look at the entire test transcript and figure out what happened.

    if ( $current_test == $expected_tests ) {
	print "# Ran all $expected_tests expected tests" . ( $skipped_tests ? " (but skipped $skipped_tests of those)" : '' ) . ".\n";
    }
    else {
        print "# FAILURE:  Ran $current_test tests but expected $expected_tests tests.\n";
    }

    # The actual Test::Builder doc shows there is more complexity here than we are actually
    # tracking, for instance because we don't have any TODO tests.  But this will do for now.
    if ( $self->builder->is_passing() ) {
	if ( $skipped_tests ) {
	    print "# NOTICE:  Some tests were skipped.\n";
	}
	else {
	    print "# SUCCESS:  All tests passed.\n";
	}
    }
    else {
	print "# FAILURE:  Some tests failed.\n";
    }

    location("exit");
}

# ================================================================

my %check_counts = ();

sub reset_count {
    my ($package, $filename, $line, $subroutine) = caller(1);
    if ( $subroutine =~ /(?:^|::)check_([^:]+)_(preconditions|analysis|postconditions)$/ ) {
	my $check = $1;
	my $phase = $2;
	$check_counts{$phase}{$check} = 0;
    }
    else {
	die "ERROR:  reset_count() called from within inappropriate routine $subroutine\n";
    }
}

sub increment_count {
    my ($package, $filename, $line, $subroutine) = caller(2);
    if ( $subroutine =~ /(?:^|::)check_([^:]+)_(preconditions|analysis|postconditions)$/ ) {
	my $check = $1;
	my $phase = $2;
	++$check_counts{$phase}{$check};
    }
    else {
	die "ERROR:  increment_count() called from within inappropriate routine $subroutine\n";
    }
}

sub check_count {
    my ($package, $filename, $line, $subroutine) = caller(1);
    if ( $subroutine =~ /(?:^|::)check_([^:]+)_(preconditions|analysis|postconditions)$/ ) {
	my $check = $1;
	my $phase = $2;
	if ( $check_counts{$phase}{$check} != $Checks->{$package}->{$phase}->{$check} ) {
	    print "# $subroutine:  expected $Checks->{$package}->{$phase}->{$check} checks, executed $check_counts{$phase}{$check} checks\n";
	    ok( 0, "validation:  $check check count" );
	}
    }
    else {
	die "ERROR:  check_count() called from within inappropriate routine $subroutine\n";
    }
}

sub test_condition {
    my $self      = shift;
    my $negate    = '';
    my $condition = shift;
    if ( $condition eq 'not' ) {
	$negate    = 'not ';
	$condition = shift;
    }
    increment_count();
    my $result = ( $negate xor $self->{GW_Test}->$condition(@_) );
    if ( $self->{report_detail} ) {
	ok( $result, "condition:  $negate$condition ( @_ )" );
    }
    return $result;
}

sub test_is_an_empty_set {
    my $self      = shift;
    my $negate    = '';
    my $condition = shift;
    if ( $condition eq 'not' ) {
	$negate    = 'not ';
	$condition = shift;
    }
    increment_count();
    my $result = ( $negate xor scalar( @{ $self->{GW_Test}->$condition(@_) } ) == 0 );
    if ( $self->{report_detail} ) {
	ok( $result, "condition:  $condition ( @_ ) is ${negate}an empty set" );
    }
    return $result;
}

sub local_is_an_empty_set {
    my $self      = shift;
    my $negate    = '';
    my $condition = shift;
    if ( $condition eq 'not' ) {
	$negate    = 'not ';
	$condition = shift;
    }
    increment_count();
    my $result = ( $negate xor scalar( @{ $self->$condition(@_) } ) == 0 );
    if ( $self->{report_detail} ) {
	ok( $result, "condition:  $condition ( @_ ) is ${negate}an empty set" );
    }
    return $result;
}

# Since our testing involves a hostgroup owned by NAGIOS (Monarch), we don't care about any hosts
# in the gwcollagedb hostgroup that are not also present in Monarch -- so we don't count them
# here as being hosts that would remain after all expected deletions in this cycle are complete.
# That means that this test is not actually accurate if the gwcollagedb hostgroup might contain
# any hosts not owned by NAGIOS.  But it will do for our immediate testing purposes.
sub monarch_hosts_to_remain_after_deletions_from_gwcollagedb_hostgroup {
    my $self                           = shift;
    my $hostgroup                      = shift;
    my $hosts_in_monarch_hostgroup     = $self->{GW_Test}->members_of_hostgroup_in_monarch($hostgroup);
    my $hosts_in_gwcollagedb_hostgroup = $self->{GW_Test}->members_of_hostgroup_in_gwcollagedb($hostgroup);
    my @monarch_hosts_to_remain_after_deletions =
      grep { $self->{GW_Test}->host_exists_in_monarch($_) and $self->{GW_Test}->host_is_member_of_hostgroup_in_monarch( $_, $hostgroup ) }
      @$hosts_in_gwcollagedb_hostgroup;
    return \@monarch_hosts_to_remain_after_deletions;
}

sub hosts_to_add_to_gwcollagedb_hostgroup {
    my $self                           = shift;
    my $hostgroup                      = shift;
    my $hosts_in_monarch_hostgroup     = $self->{GW_Test}->members_of_hostgroup_in_monarch($hostgroup);
    my $hosts_in_gwcollagedb_hostgroup = $self->{GW_Test}->members_of_hostgroup_in_gwcollagedb($hostgroup);
    my %hosts_in_gwcollagedb_hostgroup = map { $_ => 1 } @$hosts_in_gwcollagedb_hostgroup;

    if (0) {
	foreach my $host (@$hosts_in_monarch_hostgroup) {
	    print "# DEBUG:    in monarch:  $host is in $hostgroup\n";
	}
	foreach my $host (@$hosts_in_gwcollagedb_hostgroup) {
	    print "# DEBUG:  in gwcollage:  $host is in $hostgroup\n";
	}
    }

    my @hosts_to_add = grep { not $hosts_in_gwcollagedb_hostgroup{$_} } @$hosts_in_monarch_hostgroup;
    return \@hosts_to_add;
}

# Since our testing involves a servicegroup owned by NAGIOS (Monarch), we don't care about any host services
# in the gwcollagedb servicegroup that are not also present in Monarch -- so we don't count them here as
# being host services that would remain after all expected deletions in this cycle are complete.
# That means that this test is not actually accurate if the gwcollagedb servicegroup might contain
# any host services not owned by NAGIOS.  But it will do for our immediate testing purposes.
sub monarch_host_services_to_remain_after_deletions_from_gwcollagedb_servicegroup {
    my $self                                            = shift;
    my $servicegroup                                    = shift;
    my $host_services_in_monarch_servicegroup           = $self->{GW_Test}->members_of_servicegroup_in_monarch($servicegroup);
    my $host_services_in_gwcollagedb_servicegroup       = $self->{GW_Test}->members_of_servicegroup_in_gwcollagedb($servicegroup);
    my @monarch_host_services_to_remain_after_deletions = grep {
	      $self->{GW_Test}->host_service_exists_in_monarch( $_->[0], $_->[1] )
	  and $self->{GW_Test}->host_service_is_member_of_servicegroup_in_monarch( $_->[0], $_->[1], $servicegroup )
    } @$host_services_in_gwcollagedb_servicegroup;
    return \@monarch_host_services_to_remain_after_deletions;
}

sub host_services_to_add_to_gwcollagedb_servicegroup {
    my $self                                      = shift;
    my $servicegroup                              = shift;
    my $host_services_in_monarch_servicegroup     = $self->{GW_Test}->members_of_servicegroup_in_monarch($servicegroup);
    my $host_services_in_gwcollagedb_servicegroup = $self->{GW_Test}->members_of_servicegroup_in_gwcollagedb($servicegroup);
    my %host_services_in_gwcollagedb_servicegroup = ();
    $host_services_in_gwcollagedb_servicegroup{ $_->[0] }{ $_->[1] } = 1 foreach @$host_services_in_gwcollagedb_servicegroup;

    if (0) {
	foreach my $host_service (@$host_services_in_monarch_servicegroup) {
	    print "# DEBUG:    in monarch:  $host_service->[0] $host_service->[1] is in $servicegroup\n";
	}
	foreach my $host_service (@$host_services_in_gwcollagedb_servicegroup) {
	    print "# DEBUG:  in gwcollage:  $host_service->[0] $host_service->[1] is in $servicegroup\n";
	}
    }

    my @host_services_to_add = grep {
	     not exists $host_services_in_gwcollagedb_servicegroup{ $_->[0] }
	  or not exists $host_services_in_gwcollagedb_servicegroup{ $_->[0] }{ $_->[1] }
    } @$host_services_in_monarch_servicegroup;
    return \@host_services_to_add;
}

# ================================================================

sub check_host_deletion_preconditions : Check(3) {
    my $self           = shift;
    my $host_to_delete = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(host_exists_in_gwcollagedb),             $host_to_delete )
      && $self->test_condition( qw(host_in_gwcollagedb_is_owned_by_nagios), $host_to_delete )
      && $self->test_condition( qw(not host_exists_in_monarch),             $host_to_delete );
    $self->check_count();
    return $result;
}

sub check_host_deletion_analysis : Check(1) {
    my $self           = shift;
    my $delta          = shift;
    my $host_to_delete = shift;
    $self->reset_count();
    my $result = $self->test_condition( qw(host_will_be_deleted), $delta, $host_to_delete );
    $self->check_count();
    return $result;
}

sub check_host_deletion_postconditions : Check(1) {
    my $self           = shift;
    my $host_to_delete = shift;
    $self->reset_count();
    my $result = $self->test_condition( qw(not host_exists_in_gwcollagedb), $host_to_delete );
    $self->check_count();
    return $result;
}

# ================================================================

sub check_host_service_direct_deletion_preconditions : Check(5) {
    my $self                           = shift;
    my $host_of_host_service_to_delete = shift;
    my $host_service_to_delete         = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(host_exists_in_gwcollagedb), $host_of_host_service_to_delete )
      && $self->test_condition( qw(host_exists_in_monarch),     $host_of_host_service_to_delete )
      && $self->test_condition( qw(host_service_exists_in_gwcollagedb),             $host_of_host_service_to_delete, $host_service_to_delete )
      && $self->test_condition( qw(host_service_in_gwcollagedb_is_owned_by_nagios), $host_of_host_service_to_delete, $host_service_to_delete )
      && $self->test_condition( qw(not host_service_exists_in_monarch),             $host_of_host_service_to_delete, $host_service_to_delete );
    $self->check_count();
    return $result;
}

sub check_host_service_direct_deletion_analysis : Check(1) {
    my $self                           = shift;
    my $delta                          = shift;
    my $host_of_host_service_to_delete = shift;
    my $host_service_to_delete         = shift;
    $self->reset_count();
    my $result =
      $self->test_condition( qw(host_service_will_be_direct_deleted), $delta, $host_of_host_service_to_delete, $host_service_to_delete );
    $self->check_count();
    return $result;
}

sub check_host_service_direct_deletion_postconditions : Check(1) {
    my $self                           = shift;
    my $host_of_host_service_to_delete = shift;
    my $host_service_to_delete         = shift;
    $self->reset_count();
    my $result = $self->test_condition( qw(not host_service_exists_in_gwcollagedb), $host_of_host_service_to_delete, $host_service_to_delete );
    $self->check_count();
    return $result;
}

# ================================================================

sub check_host_service_cascade_deletion_preconditions : Check(5) {
    my $self                           = shift;
    my $host_of_host_service_to_delete = shift;
    my $host_service_to_delete         = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(host_exists_in_gwcollagedb), $host_of_host_service_to_delete )
      && $self->test_condition( qw(not host_exists_in_monarch), $host_of_host_service_to_delete )
      && $self->test_condition( qw(host_service_exists_in_gwcollagedb),             $host_of_host_service_to_delete, $host_service_to_delete )
      && $self->test_condition( qw(host_service_in_gwcollagedb_is_owned_by_nagios), $host_of_host_service_to_delete, $host_service_to_delete )
      && $self->test_condition( qw(not host_service_exists_in_monarch),             $host_of_host_service_to_delete, $host_service_to_delete );
    $self->check_count();
    return $result;
}

sub check_host_service_cascade_deletion_analysis : Check(2) {
    my $self                           = shift;
    my $delta                          = shift;
    my $host_of_host_service_to_delete = shift;
    my $host_service_to_delete         = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(host_will_be_deleted), $delta, $host_of_host_service_to_delete )
      && $self->test_condition( qw(host_service_will_be_cascade_deleted), $delta, $host_of_host_service_to_delete, $host_service_to_delete );
    $self->check_count();
    return $result;
}

sub check_host_service_cascade_deletion_postconditions : Check(1) {
    my $self                           = shift;
    my $host_of_host_service_to_delete = shift;
    my $host_service_to_delete         = shift;
    $self->reset_count();
    my $result = $self->test_condition( qw(not host_service_exists_in_gwcollagedb), $host_of_host_service_to_delete, $host_service_to_delete );
    $self->check_count();
    return $result;
}

# ================================================================

sub check_hostgroup_deletion_preconditions : Check(3) {
    my $self                = shift;
    my $hostgroup_to_delete = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(hostgroup_exists_in_gwcollagedb),             $hostgroup_to_delete )
      && $self->test_condition( qw(hostgroup_in_gwcollagedb_is_owned_by_nagios), $hostgroup_to_delete )
      && $self->test_condition( qw(not hostgroup_exists_in_monarch),             $hostgroup_to_delete );
    $self->check_count();
    return $result;
}

sub check_hostgroup_deletion_analysis : Check(1) {
    my $self                = shift;
    my $delta               = shift;
    my $hostgroup_to_delete = shift;
    $self->reset_count();
    my $result = $self->test_condition( qw(hostgroup_will_be_deleted), $delta, $hostgroup_to_delete );
    $self->check_count();
    return $result;
}

sub check_hostgroup_deletion_postconditions : Check(1) {
    my $self                = shift;
    my $hostgroup_to_delete = shift;
    $self->reset_count();
    my $result = $self->test_condition( qw(not hostgroup_exists_in_gwcollagedb), $hostgroup_to_delete );
    $self->check_count();
    return $result;
}

# ================================================================

sub check_servicegroup_deletion_preconditions : Check(3) {
    my $self                   = shift;
    my $servicegroup_to_delete = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(servicegroup_exists_in_gwcollagedb),             $servicegroup_to_delete )
      && $self->test_condition( qw(servicegroup_in_gwcollagedb_is_owned_by_nagios), $servicegroup_to_delete )
      && $self->test_condition( qw(not servicegroup_exists_in_monarch),             $servicegroup_to_delete );
    $self->check_count();
    return $result;
}

sub check_servicegroup_deletion_analysis : Check(1) {
    my $self                   = shift;
    my $delta                  = shift;
    my $servicegroup_to_delete = shift;
    $self->reset_count();
    my $result = $self->test_condition( qw(servicegroup_will_be_deleted), $delta, $servicegroup_to_delete );
    $self->check_count();
    return $result;
}

sub check_servicegroup_deletion_postconditions : Check(1) {
    my $self                   = shift;
    my $servicegroup_to_delete = shift;
    $self->reset_count();
    my $result = $self->test_condition( qw(not servicegroup_exists_in_gwcollagedb), $servicegroup_to_delete );
    $self->check_count();
    return $result;
}

# ================================================================

sub check_hostgroup_membership_deletion_with_static_non_empty_result_preconditions : Check(12) {
    my $self                                  = shift;
    my $host_to_delete_as_member_of_hostgroup = shift;
    my $host_stable_member_of_hostgroup       = shift;
    my $hostgroup_containing_host             = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(host_exists_in_monarch),                      $host_to_delete_as_member_of_hostgroup )
      && $self->test_condition( qw(host_exists_in_gwcollagedb),                  $host_to_delete_as_member_of_hostgroup )
      && $self->test_condition( qw(hostgroup_exists_in_monarch),                 $hostgroup_containing_host )
      && $self->test_condition( qw(hostgroup_exists_in_gwcollagedb),             $hostgroup_containing_host )
      && $self->test_condition( qw(hostgroup_in_gwcollagedb_is_owned_by_nagios), $hostgroup_containing_host )
      && $self->test_condition(
	qw(not host_is_member_of_hostgroup_in_monarch),
	$host_to_delete_as_member_of_hostgroup,
	$hostgroup_containing_host
      )
      && $self->test_condition(
	qw(host_is_member_of_hostgroup_in_gwcollagedb),
	$host_to_delete_as_member_of_hostgroup,
	$hostgroup_containing_host
      )
      && $self->test_condition( qw(host_exists_in_monarch),                     $host_stable_member_of_hostgroup )
      && $self->test_condition( qw(host_exists_in_gwcollagedb),                 $host_stable_member_of_hostgroup )
      && $self->test_condition( qw(host_is_member_of_hostgroup_in_monarch),     $host_stable_member_of_hostgroup, $hostgroup_containing_host )
      && $self->test_condition( qw(host_is_member_of_hostgroup_in_gwcollagedb), $host_stable_member_of_hostgroup, $hostgroup_containing_host )
      && $self->local_is_an_empty_set( qw(hosts_to_add_to_gwcollagedb_hostgroup), $hostgroup_containing_host );
    $self->check_count();
    return $result;
}

# FIX MAJOR:  Split into separate tests, effectively one that calls
# $self->{GW_Test}->host_will_be_direct_deleted_from_hostgroup()
# and one that calls
# $self->{GW_Test}->host_will_be_cascade_deleted_from_hostgroup().
# But see the Monarch Commit Operation testing doc to see whether that
# code distinction properly mirrors the test-precondition distinction.
sub check_hostgroup_membership_deletion_with_static_non_empty_result_analysis : Check(1) {
    my $self                                  = shift;
    my $delta                                 = shift;
    my $host_to_delete_as_member_of_hostgroup = shift;
    my $host_stable_member_of_hostgroup       = shift;
    my $hostgroup_containing_host             = shift;
    $self->reset_count();
    my $result = $self->test_condition(
	qw(host_will_be_deleted_from_hostgroup),
	$delta, $host_to_delete_as_member_of_hostgroup,
	$hostgroup_containing_host
    );
    $self->check_count();
    return $result;
}

sub check_hostgroup_membership_deletion_with_static_non_empty_result_postconditions : Check(4) {
    my $self                                  = shift;
    my $host_to_delete_as_member_of_hostgroup = shift;
    my $host_stable_member_of_hostgroup       = shift;
    my $hostgroup_containing_host             = shift;
    $self->reset_count();
    my $result =
      $self->test_condition( qw(hostgroup_exists_in_gwcollagedb), $hostgroup_containing_host ) 
      && $self->test_condition(
	qw(not host_is_member_of_hostgroup_in_gwcollagedb),
	$host_to_delete_as_member_of_hostgroup,
	$hostgroup_containing_host
      )
      && $self->test_condition( qw(host_exists_in_gwcollagedb), $host_stable_member_of_hostgroup )
      && $self->test_condition( qw(host_is_member_of_hostgroup_in_gwcollagedb), $host_stable_member_of_hostgroup, $hostgroup_containing_host );
    $self->check_count();
    return $result;
}

# ================================================================

sub check_hostgroup_membership_deletion_with_dynamic_non_empty_result_preconditions : Check(11) {
    my $self                                  = shift;
    my $host_to_delete_as_member_of_hostgroup = shift;
    my $host_new_member_of_hostgroup          = shift;
    my $hostgroup_containing_host             = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(host_exists_in_monarch),                      $host_to_delete_as_member_of_hostgroup )
      && $self->test_condition( qw(host_exists_in_gwcollagedb),                  $host_to_delete_as_member_of_hostgroup )
      && $self->test_condition( qw(hostgroup_exists_in_monarch),                 $hostgroup_containing_host )
      && $self->test_condition( qw(hostgroup_exists_in_gwcollagedb),             $hostgroup_containing_host )
      && $self->test_condition( qw(hostgroup_in_gwcollagedb_is_owned_by_nagios), $hostgroup_containing_host )
      && $self->test_condition(
	qw(not host_is_member_of_hostgroup_in_monarch),
	$host_to_delete_as_member_of_hostgroup,
	$hostgroup_containing_host
      )
      && $self->test_condition(
	qw(host_is_member_of_hostgroup_in_gwcollagedb),
	$host_to_delete_as_member_of_hostgroup,
	$hostgroup_containing_host
      )
      && $self->local_is_an_empty_set( qw(monarch_hosts_to_remain_after_deletions_from_gwcollagedb_hostgroup), $hostgroup_containing_host )
      && $self->test_condition( qw(host_exists_in_monarch),                         $host_new_member_of_hostgroup )
      && $self->test_condition( qw(host_is_member_of_hostgroup_in_monarch),         $host_new_member_of_hostgroup, $hostgroup_containing_host )
      && $self->test_condition( qw(not host_is_member_of_hostgroup_in_gwcollagedb), $host_new_member_of_hostgroup, $hostgroup_containing_host );
    $self->check_count();
    return $result;
}

# FIX MAJOR:  Split into separate tests, effectively one that calls
# $self->{GW_Test}->host_will_be_direct_deleted_from_hostgroup()
# and one that calls
# $self->{GW_Test}->host_will_be_cascade_deleted_from_hostgroup().
# But see the Monarch Commit Operation testing doc to see whether that
# code distinction properly mirrors the test-precondition distinction.
sub check_hostgroup_membership_deletion_with_dynamic_non_empty_result_analysis : Check(1) {
    my $self                                  = shift;
    my $delta                                 = shift;
    my $host_to_delete_as_member_of_hostgroup = shift;
    my $host_new_member_of_hostgroup          = shift;
    my $hostgroup_containing_host             = shift;
    $self->reset_count();
    my $result = $self->test_condition(
	qw(host_will_be_deleted_from_hostgroup),
	$delta, $host_to_delete_as_member_of_hostgroup,
	$hostgroup_containing_host
    );
    $self->check_count();
    return $result;
}

sub check_hostgroup_membership_deletion_with_dynamic_non_empty_result_postconditions : Check(4) {
    my $self                                  = shift;
    my $host_to_delete_as_member_of_hostgroup = shift;
    my $host_new_member_of_hostgroup          = shift;
    my $hostgroup_containing_host             = shift;
    $self->reset_count();
    my $result =
      $self->test_condition( qw(hostgroup_exists_in_gwcollagedb), $hostgroup_containing_host ) 
      && $self->test_condition(
	qw(not host_is_member_of_hostgroup_in_gwcollagedb),
	$host_to_delete_as_member_of_hostgroup,
	$hostgroup_containing_host
      )
      && $self->test_condition( qw(host_exists_in_gwcollagedb), $host_new_member_of_hostgroup )
      && $self->test_condition( qw(host_is_member_of_hostgroup_in_gwcollagedb), $host_new_member_of_hostgroup, $hostgroup_containing_host );
    $self->check_count();
    return $result;
}

# ================================================================

sub check_hostgroup_membership_deletion_with_empty_result_preconditions : Check(9) {
    my $self                                  = shift;
    my $host_to_delete_as_member_of_hostgroup = shift;
    my $hostgroup_containing_host             = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(host_exists_in_monarch),                      $host_to_delete_as_member_of_hostgroup )
      && $self->test_condition( qw(host_exists_in_gwcollagedb),                  $host_to_delete_as_member_of_hostgroup )
      && $self->test_condition( qw(hostgroup_exists_in_monarch),                 $hostgroup_containing_host )
      && $self->test_condition( qw(hostgroup_exists_in_gwcollagedb),             $hostgroup_containing_host )
      && $self->test_condition( qw(hostgroup_in_gwcollagedb_is_owned_by_nagios), $hostgroup_containing_host )
      && $self->test_condition(
	qw(not host_is_member_of_hostgroup_in_monarch),
	$host_to_delete_as_member_of_hostgroup,
	$hostgroup_containing_host
      )
      && $self->test_condition(
	qw(host_is_member_of_hostgroup_in_gwcollagedb),
	$host_to_delete_as_member_of_hostgroup,
	$hostgroup_containing_host
      )
      && $self->local_is_an_empty_set( qw(monarch_hosts_to_remain_after_deletions_from_gwcollagedb_hostgroup), $hostgroup_containing_host )
      && $self->local_is_an_empty_set( qw(hosts_to_add_to_gwcollagedb_hostgroup),                              $hostgroup_containing_host );
    $self->check_count();
    return $result;
}

sub check_hostgroup_membership_deletion_with_empty_result_analysis : Check(2) {
    my $self                                  = shift;
    my $delta                                 = shift;
    my $host_to_delete_as_member_of_hostgroup = shift;
    my $hostgroup_containing_host             = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(hostgroup_will_be_deleted), $delta, $hostgroup_containing_host )
      && $self->test_is_an_empty_set( qw(hosts_that_will_be_added_to_hostgroup), $delta, $hostgroup_containing_host );
    $self->check_count();
    return $result;
}

sub check_hostgroup_membership_deletion_with_empty_result_postconditions : Check(1) {
    my $self                                  = shift;
    my $host_to_delete_as_member_of_hostgroup = shift;
    my $hostgroup_containing_host             = shift;
    $self->reset_count();
    my $result = $self->test_condition( qw(not hostgroup_exists_in_gwcollagedb), $hostgroup_containing_host );
    $self->check_count();
    return $result;
}

# ================================================================

sub check_servicegroup_membership_deletion_with_static_non_empty_result_preconditions : Check(12) {
    my $self                                                     = shift;
    my $host_of_host_service_to_delete_as_member_of_servicegroup = shift;
    my $host_service_to_delete_as_member_of_servicegroup         = shift;
    my $host_of_host_service_stable_member_of_servicegroup       = shift;
    my $host_service_stable_member_of_servicegroup               = shift;
    my $servicegroup_containing_host_service                     = shift;
    $self->reset_count();

    #<<<  do not let perltidy touch this; it's too complicated for any automatic formatting to make it look readable
    my $result = 
	$self->test_condition( qw(host_service_exists_in_monarch),
	    $host_of_host_service_to_delete_as_member_of_servicegroup,
	    $host_service_to_delete_as_member_of_servicegroup
	)
      &&
	$self->test_condition( qw(host_service_exists_in_gwcollagedb),
	    $host_of_host_service_to_delete_as_member_of_servicegroup,
	    $host_service_to_delete_as_member_of_servicegroup
	)
      &&
	$self->test_condition( qw(servicegroup_exists_in_monarch), $servicegroup_containing_host_service)
      &&
	$self->test_condition( qw(servicegroup_exists_in_gwcollagedb), $servicegroup_containing_host_service)
      &&
	$self->test_condition( qw(servicegroup_in_gwcollagedb_is_owned_by_nagios), $servicegroup_containing_host_service)
      &&
	$self->test_condition( qw(not host_service_is_member_of_servicegroup_in_monarch),
	    $host_of_host_service_to_delete_as_member_of_servicegroup,
	    $host_service_to_delete_as_member_of_servicegroup,
	    $servicegroup_containing_host_service
	)
      &&
	$self->test_condition( qw(host_service_is_member_of_servicegroup_in_gwcollagedb),
	    $host_of_host_service_to_delete_as_member_of_servicegroup,
	    $host_service_to_delete_as_member_of_servicegroup,
	    $servicegroup_containing_host_service
	)
      &&
	$self->test_condition( qw(host_service_exists_in_monarch),
	    $host_of_host_service_stable_member_of_servicegroup,
	    $host_service_stable_member_of_servicegroup
	)
      &&
	$self->test_condition( qw(host_service_exists_in_gwcollagedb),
	    $host_of_host_service_stable_member_of_servicegroup,
	    $host_service_stable_member_of_servicegroup )
      &&
	$self->test_condition( qw(host_service_is_member_of_servicegroup_in_monarch),
	    $host_of_host_service_stable_member_of_servicegroup,
	    $host_service_stable_member_of_servicegroup,
	    $servicegroup_containing_host_service
	)
      &&
	$self->test_condition( qw(host_service_is_member_of_servicegroup_in_gwcollagedb),
	    $host_of_host_service_stable_member_of_servicegroup,
	    $host_service_stable_member_of_servicegroup,
	    $servicegroup_containing_host_service
	)
      &&
	$self->local_is_an_empty_set( qw(host_services_to_add_to_gwcollagedb_servicegroup), $servicegroup_containing_host_service )
   ;
    #>>>
    $self->check_count();
    return $result;
}

# FIX MAJOR:  Split into separate tests, effectively one that calls
# $self->{GW_Test}->host_service_will_be_direct_deleted_from_servicegroup()
# and one that calls
# $self->{GW_Test}->host_service_will_be_cascade_deleted_from_servicegroup().
# But see the Monarch Commit Operation testing doc to see whether that
# code distinction properly mirrors the test-precondition distinction.
sub check_servicegroup_membership_deletion_with_static_non_empty_result_analysis : Check(1) {
    my $self                                                     = shift;
    my $delta                                                    = shift;
    my $host_of_host_service_to_delete_as_member_of_servicegroup = shift;
    my $host_service_to_delete_as_member_of_servicegroup         = shift;
    my $host_of_host_service_stable_member_of_servicegroup       = shift;
    my $host_service_stable_member_of_servicegroup               = shift;
    my $servicegroup_containing_host_service                     = shift;
    $self->reset_count();
    my $result = $self->test_condition(
	qw(host_service_will_be_deleted_from_servicegroup),
	$delta,
	$host_of_host_service_to_delete_as_member_of_servicegroup,
	$host_service_to_delete_as_member_of_servicegroup,
	$servicegroup_containing_host_service
    );
    $self->check_count();
    return $result;
}

sub check_servicegroup_membership_deletion_with_static_non_empty_result_postconditions : Check(4) {
    my $self                                                     = shift;
    my $host_of_host_service_to_delete_as_member_of_servicegroup = shift;
    my $host_service_to_delete_as_member_of_servicegroup         = shift;
    my $host_of_host_service_stable_member_of_servicegroup       = shift;
    my $host_service_stable_member_of_servicegroup               = shift;
    my $servicegroup_containing_host_service                     = shift;
    $self->reset_count();
    my $result =
      $self->test_condition( qw(servicegroup_exists_in_gwcollagedb), $servicegroup_containing_host_service ) 
      && $self->test_condition(
	qw(not host_service_is_member_of_servicegroup_in_gwcollagedb), $host_of_host_service_to_delete_as_member_of_servicegroup,
	$host_service_to_delete_as_member_of_servicegroup,             $servicegroup_containing_host_service
      )
      && $self->test_condition(
	qw(host_service_exists_in_gwcollagedb),
	$host_of_host_service_stable_member_of_servicegroup,
	$host_service_stable_member_of_servicegroup
      )
      && $self->test_condition(
	qw(host_service_is_member_of_servicegroup_in_gwcollagedb), $host_of_host_service_stable_member_of_servicegroup,
	$host_service_stable_member_of_servicegroup,               $servicegroup_containing_host_service
      );
    $self->check_count();
    return $result;
}

# ================================================================

sub check_servicegroup_membership_deletion_with_dynamic_non_empty_result_preconditions : Check(11) {
    my $self                                                     = shift;
    my $host_of_host_service_to_delete_as_member_of_servicegroup = shift;
    my $host_service_to_delete_as_member_of_servicegroup         = shift;
    my $host_of_host_service_new_member_of_servicegroup          = shift;
    my $host_service_new_member_of_servicegroup                  = shift;
    my $servicegroup_containing_host_service                     = shift;
    $self->reset_count();

    #<<<  do not let perltidy touch this; it's too complicated for any automatic formatting to make it look readable
    my $result = 
	$self->test_condition( qw(host_service_exists_in_monarch),
	    $host_of_host_service_to_delete_as_member_of_servicegroup,
	    $host_service_to_delete_as_member_of_servicegroup
	)
      &&
	$self->test_condition( qw(host_service_exists_in_gwcollagedb),
	    $host_of_host_service_to_delete_as_member_of_servicegroup,
	    $host_service_to_delete_as_member_of_servicegroup
	)
      &&
	$self->test_condition( qw(servicegroup_exists_in_monarch), $servicegroup_containing_host_service)
      &&
	$self->test_condition( qw(servicegroup_exists_in_gwcollagedb), $servicegroup_containing_host_service)
      &&
	$self->test_condition( qw(servicegroup_in_gwcollagedb_is_owned_by_nagios), $servicegroup_containing_host_service)
      &&
	$self->test_condition( qw(not host_service_is_member_of_servicegroup_in_monarch),
	    $host_of_host_service_to_delete_as_member_of_servicegroup,
	    $host_service_to_delete_as_member_of_servicegroup,
	    $servicegroup_containing_host_service
	)
      &&
	$self->test_condition( qw(host_service_is_member_of_servicegroup_in_gwcollagedb),
	    $host_of_host_service_to_delete_as_member_of_servicegroup,
	    $host_service_to_delete_as_member_of_servicegroup,
	    $servicegroup_containing_host_service
	)
      &&
	$self->local_is_an_empty_set( qw(monarch_host_services_to_remain_after_deletions_from_gwcollagedb_servicegroup),
	    $servicegroup_containing_host_service
	)
      &&
	$self->test_condition( qw(host_service_exists_in_monarch),
	    $host_of_host_service_new_member_of_servicegroup,
	    $host_service_new_member_of_servicegroup
	)
      &&
	$self->test_condition( qw(host_service_is_member_of_servicegroup_in_monarch),
	    $host_of_host_service_new_member_of_servicegroup,
	    $host_service_new_member_of_servicegroup,
	    $servicegroup_containing_host_service
	)
      &&
	$self->test_condition( qw(not host_service_is_member_of_servicegroup_in_gwcollagedb),
	    $host_of_host_service_new_member_of_servicegroup,
	    $host_service_new_member_of_servicegroup,
	    $servicegroup_containing_host_service
	)
   ;
    #>>>
    $self->check_count();
    return $result;
}

# FIX MAJOR:  Split into separate tests, effectively one that calls
# $self->{GW_Test}->host_service_will_be_direct_deleted_from_servicegroup()
# and one that calls
# $self->{GW_Test}->host_service_will_be_cascade_deleted_from_servicegroup().
# But see the Monarch Commit Operation testing doc to see whether that
# code distinction properly mirrors the test-precondition distinction.
sub check_servicegroup_membership_deletion_with_dynamic_non_empty_result_analysis : Check(1) {
    my $self                                                     = shift;
    my $delta                                                    = shift;
    my $host_of_host_service_to_delete_as_member_of_servicegroup = shift;
    my $host_service_to_delete_as_member_of_servicegroup         = shift;
    my $host_of_host_service_new_member_of_servicegroup          = shift;
    my $host_service_new_member_of_servicegroup                  = shift;
    my $servicegroup_containing_host_service                     = shift;
    $self->reset_count();
    my $result = $self->test_condition(
	qw(host_service_will_be_deleted_from_servicegroup),
	$delta,
	$host_of_host_service_to_delete_as_member_of_servicegroup,
	$host_service_to_delete_as_member_of_servicegroup,
	$servicegroup_containing_host_service
    );
    $self->check_count();
    return $result;
}

sub check_servicegroup_membership_deletion_with_dynamic_non_empty_result_postconditions : Check(4) {
    my $self                                                     = shift;
    my $host_of_host_service_to_delete_as_member_of_servicegroup = shift;
    my $host_service_to_delete_as_member_of_servicegroup         = shift;
    my $host_of_host_service_new_member_of_servicegroup          = shift;
    my $host_service_new_member_of_servicegroup                  = shift;
    my $servicegroup_containing_host_service                     = shift;
    $self->reset_count();
    my $result =
      $self->test_condition( qw(servicegroup_exists_in_gwcollagedb), $servicegroup_containing_host_service ) 
      && $self->test_condition(
	qw(not host_service_is_member_of_servicegroup_in_gwcollagedb), $host_of_host_service_to_delete_as_member_of_servicegroup,
	$host_service_to_delete_as_member_of_servicegroup,             $servicegroup_containing_host_service
      )
      && $self->test_condition(
	qw(host_service_exists_in_gwcollagedb),
	$host_of_host_service_new_member_of_servicegroup,
	$host_service_new_member_of_servicegroup
      )
      && $self->test_condition(
	qw(host_service_is_member_of_servicegroup_in_gwcollagedb), $host_of_host_service_new_member_of_servicegroup,
	$host_service_new_member_of_servicegroup,                  $servicegroup_containing_host_service
      );
    $self->check_count();
    return $result;
}

# ================================================================

sub check_servicegroup_membership_deletion_with_empty_result_preconditions : Check(9) {
    my $self                                        = shift;
    my $host_of_host_service_member_of_servicegroup = shift;
    my $host_service_member_of_servicegroup         = shift;
    my $servicegroup_containing_host_service        = shift;
    $self->reset_count();
    my $result = $self->test_condition(
	qw(host_service_exists_in_monarch),
	$host_of_host_service_member_of_servicegroup,
	$host_service_member_of_servicegroup
      )
      && $self->test_condition(
	qw(host_service_exists_in_gwcollagedb),
	$host_of_host_service_member_of_servicegroup,
	$host_service_member_of_servicegroup
      )
      && $self->test_condition( qw(servicegroup_exists_in_monarch),                 $servicegroup_containing_host_service )
      && $self->test_condition( qw(servicegroup_exists_in_gwcollagedb),             $servicegroup_containing_host_service )
      && $self->test_condition( qw(servicegroup_in_gwcollagedb_is_owned_by_nagios), $servicegroup_containing_host_service )
      && $self->test_condition(
	qw(not host_service_is_member_of_servicegroup_in_monarch), $host_of_host_service_member_of_servicegroup,
	$host_service_member_of_servicegroup,                      $servicegroup_containing_host_service
      )
      && $self->test_condition(
	qw(host_service_is_member_of_servicegroup_in_gwcollagedb), $host_of_host_service_member_of_servicegroup,
	$host_service_member_of_servicegroup,                      $servicegroup_containing_host_service
      )
      && $self->local_is_an_empty_set( qw(monarch_host_services_to_remain_after_deletions_from_gwcollagedb_servicegroup),
	$servicegroup_containing_host_service )
      && $self->local_is_an_empty_set( qw(host_services_to_add_to_gwcollagedb_servicegroup), $servicegroup_containing_host_service );
    $self->check_count();
    return $result;
}

sub check_servicegroup_membership_deletion_with_empty_result_analysis : Check(2) {
    my $self                                        = shift;
    my $delta                                       = shift;
    my $host_of_host_service_member_of_servicegroup = shift;
    my $host_service_member_of_servicegroup         = shift;
    my $servicegroup_containing_host_service        = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(servicegroup_will_be_deleted), $delta, $servicegroup_containing_host_service )
      && $self->test_is_an_empty_set( qw(host_services_that_will_be_added_to_servicegroup), $delta, $servicegroup_containing_host_service );
    $self->check_count();
    return $result;
}

sub check_servicegroup_membership_deletion_with_empty_result_postconditions : Check(1) {
    my $self                                        = shift;
    my $host_of_host_service_member_of_servicegroup = shift;
    my $host_service_member_of_servicegroup         = shift;
    my $servicegroup_containing_host_service        = shift;
    $self->reset_count();
    my $result = $self->test_condition( qw(not servicegroup_exists_in_gwcollagedb), $servicegroup_containing_host_service );
    $self->check_count();
    return $result;
}

# ================================================================

sub check_new_host_addition_preconditions : Check(2) {
    my $self        = shift;
    my $host_to_add = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(host_exists_in_monarch),         $host_to_add )
      && $self->test_condition( qw(not host_exists_in_gwcollagedb), $host_to_add );
    $self->check_count();
    return $result;
}

sub check_new_host_addition_analysis : Check(1) {
    my $self        = shift;
    my $delta       = shift;
    my $host_to_add = shift;
    $self->reset_count();
    my $result = $self->test_condition( qw(host_will_be_added), $delta, $host_to_add );
    $self->check_count();
    return $result;
}

# FIX MINOR:  Check host attributes as well.  Currently, these are:
# * Description     (same as hostname)
# * Notes           (optional, as known to Monarch)
# * Device          (host address)
# * DisplayName     (same as hostname)
# * LastStateChange (a valid timestamp)
# * Parent          (host parents as known to Monarch)
# * Alias           (host alias as known to Monarch)
sub check_new_host_addition_postconditions : Check(2) {
    my $self        = shift;
    my $host_to_add = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(host_exists_in_gwcollagedb),             $host_to_add )
      && $self->test_condition( qw(host_in_gwcollagedb_is_owned_by_nagios), $host_to_add );
    $self->check_count();
    return $result;
}

# ================================================================

sub check_new_host_service_addition_preconditions : Check(2) {
    my $self                        = shift;
    my $host_of_host_service_to_add = shift;
    my $host_service_to_add         = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(host_service_exists_in_monarch),         $host_of_host_service_to_add, $host_service_to_add )
      && $self->test_condition( qw(not host_service_exists_in_gwcollagedb), $host_of_host_service_to_add, $host_service_to_add );
    $self->check_count();
    return $result;
}

sub check_new_host_service_addition_analysis : Check(1) {
    my $self                        = shift;
    my $delta                       = shift;
    my $host_of_host_service_to_add = shift;
    my $host_service_to_add         = shift;
    $self->reset_count();
    my $result = $self->test_condition( qw(host_service_will_be_added), $delta, $host_of_host_service_to_add, $host_service_to_add );
    $self->check_count();
    return $result;
}

# FIX MINOR:  Check host service attributes as well.  Currently, these are:
# * Notes           (optional, as known to Monarch)
# * CheckType       (fixed as 'ACTIVE' at this time, but this could change in the future and as monitoring proceeds)
# * StateType       (fixed as 'SOFT' at this time, but this could change as monitoring proceeds)
# * MonitorStatus   (fixed as 'PENDING' at this time, but this should change as monitoring proceeds)
# * LastHardState   (fixed as 'PENDING' at this time, but this should change as monitoring proceeds)
# * LastStateChange (a valid timestamp)
sub check_new_host_service_addition_postconditions : Check(2) {
    my $self                        = shift;
    my $host_of_host_service_to_add = shift;
    my $host_service_to_add         = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(host_service_exists_in_gwcollagedb),             $host_of_host_service_to_add, $host_service_to_add )
      && $self->test_condition( qw(host_service_in_gwcollagedb_is_owned_by_nagios), $host_of_host_service_to_add, $host_service_to_add );
    $self->check_count();
    return $result;
}

# ================================================================

sub check_non_empty_new_hostgroup_addition_preconditions : Check(4) {
    my $self                     = shift;
    my $host_in_hostgroup_to_add = shift;
    my $hostgroup_to_add         = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(host_exists_in_monarch),              $host_in_hostgroup_to_add )
      && $self->test_condition( qw(hostgroup_exists_in_monarch),         $hostgroup_to_add )
      && $self->test_condition( qw(not hostgroup_exists_in_gwcollagedb), $hostgroup_to_add )
      && $self->test_condition( qw(host_is_member_of_hostgroup_in_monarch), $host_in_hostgroup_to_add, $hostgroup_to_add );
    $self->check_count();
    return $result;
}

sub check_non_empty_new_hostgroup_addition_analysis : Check(1) {
    my $self                     = shift;
    my $delta                    = shift;
    my $host_in_hostgroup_to_add = shift;
    my $hostgroup_to_add         = shift;
    $self->reset_count();
    my $result = $self->test_condition( qw(hostgroup_will_be_added), $delta, $hostgroup_to_add );
    $self->check_count();
    return $result;
}

# FIX MINOR:  Check hostgroup attributes as well.  Currently, these are:
# * Alias       (hostgroup alias as known to Monarch)
# * Description (hostgroup notes as known to Monarch, stored instead in the Description field in Foundation)
sub check_non_empty_new_hostgroup_addition_postconditions : Check(3) {
    my $self                     = shift;
    my $host_in_hostgroup_to_add = shift;
    my $hostgroup_to_add         = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(hostgroup_exists_in_gwcollagedb),             $hostgroup_to_add )
      && $self->test_condition( qw(hostgroup_in_gwcollagedb_is_owned_by_nagios), $hostgroup_to_add )
      && $self->test_condition( qw(host_is_member_of_hostgroup_in_gwcollagedb), $host_in_hostgroup_to_add, $hostgroup_to_add );
    $self->check_count();
    return $result;
}

# ================================================================

sub check_empty_new_hostgroup_addition_preconditions : Check(3) {
    my $self             = shift;
    my $hostgroup_to_add = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(hostgroup_exists_in_monarch),         $hostgroup_to_add )
      && $self->test_condition( qw(not hostgroup_exists_in_gwcollagedb), $hostgroup_to_add )
      && $self->test_condition( qw(hostgroup_in_monarch_is_empty),       $hostgroup_to_add );
    $self->check_count();
    return $result;
}

sub check_empty_new_hostgroup_addition_analysis : Check(1) {
    my $self             = shift;
    my $delta            = shift;
    my $hostgroup_to_add = shift;
    $self->reset_count();
    my $result = $self->test_condition( qw(not hostgroup_will_be_added), $delta, $hostgroup_to_add );
    $self->check_count();
    return $result;
}

sub check_empty_new_hostgroup_addition_postconditions : Check(1) {
    my $self             = shift;
    my $hostgroup_to_add = shift;
    $self->reset_count();
    my $result = $self->test_condition( qw(not hostgroup_exists_in_gwcollagedb), $hostgroup_to_add );
    $self->check_count();
    return $result;
}

# ================================================================

sub check_non_empty_new_servicegroup_addition_preconditions : Check(4) {
    my $self                                   = shift;
    my $host_of_service_in_servicegroup_to_add = shift;
    my $service_in_servicegroup_to_add         = shift;
    my $servicegroup_to_add                    = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(host_service_exists_in_monarch), $host_of_service_in_servicegroup_to_add, $service_in_servicegroup_to_add )
      && $self->test_condition( qw(servicegroup_exists_in_monarch), $servicegroup_to_add )
      && $self->test_condition( qw(not servicegroup_exists_in_gwcollagedb), $servicegroup_to_add )
      && $self->test_condition(
	qw(host_service_is_member_of_servicegroup_in_monarch),
	$host_of_service_in_servicegroup_to_add,
	$service_in_servicegroup_to_add,
	$servicegroup_to_add
      );
    $self->check_count();
    return $result;
}

sub check_non_empty_new_servicegroup_addition_analysis : Check(1) {
    my $self                                   = shift;
    my $delta                                  = shift;
    my $host_of_service_in_servicegroup_to_add = shift;
    my $service_in_servicegroup_to_add         = shift;
    my $servicegroup_to_add                    = shift;
    $self->reset_count();
    my $result = $self->test_condition( qw(servicegroup_will_be_added), $delta, $servicegroup_to_add );
    $self->check_count();
    return $result;
}

# FIX MINOR:  Check servicegroup attributes as well.  Currently, these are:
# * Description (servicegroup notes as known to Monarch, stored instead in the Description field in Foundation)
sub check_non_empty_new_servicegroup_addition_postconditions : Check(3) {
    my $self                                   = shift;
    my $host_of_service_in_servicegroup_to_add = shift;
    my $service_in_servicegroup_to_add         = shift;
    my $servicegroup_to_add                    = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(servicegroup_exists_in_gwcollagedb),             $servicegroup_to_add )
      && $self->test_condition( qw(servicegroup_in_gwcollagedb_is_owned_by_nagios), $servicegroup_to_add )
      && $self->test_condition(
	qw(host_service_is_member_of_servicegroup_in_gwcollagedb),
	$host_of_service_in_servicegroup_to_add,
	$service_in_servicegroup_to_add,
	$servicegroup_to_add
      );
    $self->check_count();
    return $result;
}

# ================================================================

sub check_empty_new_servicegroup_addition_preconditions : Check(3) {
    my $self                = shift;
    my $servicegroup_to_add = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(servicegroup_exists_in_monarch),         $servicegroup_to_add )
      && $self->test_condition( qw(not servicegroup_exists_in_gwcollagedb), $servicegroup_to_add )
      && $self->test_condition( qw(servicegroup_in_monarch_is_empty),       $servicegroup_to_add );
    $self->check_count();
    return $result;
}

sub check_empty_new_servicegroup_addition_analysis : Check(1) {
    my $self                = shift;
    my $delta               = shift;
    my $servicegroup_to_add = shift;
    $self->reset_count();
    my $result = $self->test_condition( qw(not servicegroup_will_be_added), $delta, $servicegroup_to_add );
    $self->check_count();
    return $result;
}

sub check_empty_new_servicegroup_addition_postconditions : Check(1) {
    my $self                = shift;
    my $servicegroup_to_add = shift;
    $self->reset_count();
    my $result = $self->test_condition( qw(not servicegroup_exists_in_gwcollagedb), $servicegroup_to_add );
    $self->check_count();
    return $result;
}

# ================================================================

sub check_existing_host_existing_hostgroup_hostgroup_membership_addition_with_dynamic_non_empty_result_preconditions : Check(10) {
    my $self                       = shift;
    my $host_in_hostgroup          = shift;
    my $unstable_host_in_hostgroup = shift;
    my $hostgroup                  = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(host_exists_in_monarch),                      $host_in_hostgroup )
      && $self->test_condition( qw(host_exists_in_gwcollagedb),                  $host_in_hostgroup )
      && $self->test_condition( qw(hostgroup_exists_in_monarch),                 $hostgroup )
      && $self->test_condition( qw(hostgroup_exists_in_gwcollagedb),             $hostgroup )
      && $self->test_condition( qw(hostgroup_in_gwcollagedb_is_owned_by_nagios), $hostgroup )
      && $self->test_condition( qw(host_is_member_of_hostgroup_in_monarch),         $host_in_hostgroup,          $hostgroup )
      && $self->test_condition( qw(not host_is_member_of_hostgroup_in_gwcollagedb), $host_in_hostgroup,          $hostgroup )
      && $self->test_condition( qw(host_exists_in_gwcollagedb),                     $unstable_host_in_hostgroup )
      && $self->test_condition( qw(host_is_member_of_hostgroup_in_gwcollagedb),     $unstable_host_in_hostgroup, $hostgroup )
      && $self->test_condition( qw(not host_exists_in_monarch),                     $unstable_host_in_hostgroup );
    $self->check_count();
    return $result;
}

sub check_existing_host_existing_hostgroup_hostgroup_membership_addition_with_dynamic_non_empty_result_analysis : Check(1) {
    my $self                       = shift;
    my $delta                      = shift;
    my $host_in_hostgroup          = shift;
    my $unstable_host_in_hostgroup = shift;
    my $hostgroup                  = shift;
    $self->reset_count();
    my $result = $self->test_condition( qw(host_will_be_added_to_existing_hostgroup), $delta, $host_in_hostgroup, $hostgroup );
    $self->check_count();
    return $result;
}

sub check_existing_host_existing_hostgroup_hostgroup_membership_addition_with_dynamic_non_empty_result_postconditions : Check(2) {
    my $self                       = shift;
    my $host_in_hostgroup          = shift;
    my $unstable_host_in_hostgroup = shift;
    my $hostgroup                  = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(host_is_member_of_hostgroup_in_gwcollagedb), $host_in_hostgroup, $hostgroup )
      && $self->test_condition( qw(not host_exists_in_gwcollagedb), $unstable_host_in_hostgroup );
    $self->check_count();
    return $result;
}

# ================================================================

sub check_new_host_existing_hostgroup_hostgroup_membership_addition_with_dynamic_non_empty_result_preconditions : Check(9) {
    my $self                       = shift;
    my $host_in_hostgroup          = shift;
    my $unstable_host_in_hostgroup = shift;
    my $hostgroup                  = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(host_exists_in_monarch),                      $host_in_hostgroup )
      && $self->test_condition( qw(not host_exists_in_gwcollagedb),              $host_in_hostgroup )
      && $self->test_condition( qw(hostgroup_exists_in_monarch),                 $hostgroup )
      && $self->test_condition( qw(hostgroup_exists_in_gwcollagedb),             $hostgroup )
      && $self->test_condition( qw(hostgroup_in_gwcollagedb_is_owned_by_nagios), $hostgroup )
      && $self->test_condition( qw(host_is_member_of_hostgroup_in_monarch),     $host_in_hostgroup,          $hostgroup )
      && $self->test_condition( qw(host_exists_in_gwcollagedb),                 $unstable_host_in_hostgroup )
      && $self->test_condition( qw(host_is_member_of_hostgroup_in_gwcollagedb), $unstable_host_in_hostgroup, $hostgroup )
      && $self->test_condition( qw(not host_exists_in_monarch),                 $unstable_host_in_hostgroup );
    $self->check_count();
    return $result;
}

sub check_new_host_existing_hostgroup_hostgroup_membership_addition_with_dynamic_non_empty_result_analysis : Check(2) {
    my $self                       = shift;
    my $delta                      = shift;
    my $host_in_hostgroup          = shift;
    my $unstable_host_in_hostgroup = shift;
    my $hostgroup                  = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(host_will_be_added), $delta, $host_in_hostgroup )
      && $self->test_condition( qw(host_will_be_added_to_existing_hostgroup), $delta, $host_in_hostgroup, $hostgroup );
    $self->check_count();
    return $result;
}

sub check_new_host_existing_hostgroup_hostgroup_membership_addition_with_dynamic_non_empty_result_postconditions : Check(2) {
    my $self                       = shift;
    my $host_in_hostgroup          = shift;
    my $unstable_host_in_hostgroup = shift;
    my $hostgroup                  = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(host_is_member_of_hostgroup_in_gwcollagedb), $host_in_hostgroup, $hostgroup )
      && $self->test_condition( qw(not host_exists_in_gwcollagedb), $unstable_host_in_hostgroup );
    $self->check_count();
    return $result;
}

# ================================================================

sub check_existing_host_new_hostgroup_hostgroup_membership_addition_preconditions : Check(5) {
    my $self              = shift;
    my $host_in_hostgroup = shift;
    my $hostgroup         = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(host_exists_in_monarch),              $host_in_hostgroup )
      && $self->test_condition( qw(host_exists_in_gwcollagedb),          $host_in_hostgroup )
      && $self->test_condition( qw(hostgroup_exists_in_monarch),         $hostgroup )
      && $self->test_condition( qw(not hostgroup_exists_in_gwcollagedb), $hostgroup )
      && $self->test_condition( qw(host_is_member_of_hostgroup_in_monarch), $host_in_hostgroup, $hostgroup );
    $self->check_count();
    return $result;
}

sub check_existing_host_new_hostgroup_hostgroup_membership_addition_analysis : Check(2) {
    my $self              = shift;
    my $delta             = shift;
    my $host_in_hostgroup = shift;
    my $hostgroup         = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(hostgroup_will_be_added), $delta, $hostgroup )
      && $self->test_condition( qw(host_will_be_added_to_new_hostgroup), $delta, $host_in_hostgroup, $hostgroup );
    $self->check_count();
    return $result;
}

sub check_existing_host_new_hostgroup_hostgroup_membership_addition_postconditions : Check(1) {
    my $self              = shift;
    my $host_in_hostgroup = shift;
    my $hostgroup         = shift;
    $self->reset_count();
    my $result = $self->test_condition( qw(host_is_member_of_hostgroup_in_gwcollagedb), $host_in_hostgroup, $hostgroup );
    $self->check_count();
    return $result;
}

# ================================================================

sub check_new_host_new_hostgroup_hostgroup_membership_addition_preconditions : Check(5) {
    my $self              = shift;
    my $host_in_hostgroup = shift;
    my $hostgroup         = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(host_exists_in_monarch),              $host_in_hostgroup )
      && $self->test_condition( qw(not host_exists_in_gwcollagedb),      $host_in_hostgroup )
      && $self->test_condition( qw(hostgroup_exists_in_monarch),         $hostgroup )
      && $self->test_condition( qw(not hostgroup_exists_in_gwcollagedb), $hostgroup )
      && $self->test_condition( qw(host_is_member_of_hostgroup_in_monarch), $host_in_hostgroup, $hostgroup );
    $self->check_count();
    return $result;
}

sub check_new_host_new_hostgroup_hostgroup_membership_addition_analysis : Check(3) {
    my $self              = shift;
    my $delta             = shift;
    my $host_in_hostgroup = shift;
    my $hostgroup         = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(host_will_be_added),      $delta, $host_in_hostgroup )
      && $self->test_condition( qw(hostgroup_will_be_added), $delta, $hostgroup )
      && $self->test_condition( qw(host_will_be_added_to_new_hostgroup), $delta, $host_in_hostgroup, $hostgroup );
    $self->check_count();
    return $result;
}

sub check_new_host_new_hostgroup_hostgroup_membership_addition_postconditions : Check(1) {
    my $self              = shift;
    my $host_in_hostgroup = shift;
    my $hostgroup         = shift;
    $self->reset_count();
    my $result = $self->test_condition( qw(host_is_member_of_hostgroup_in_gwcollagedb), $host_in_hostgroup, $hostgroup );
    $self->check_count();
    return $result;
}

# ================================================================

sub check_existing_host_service_existing_servicegroup_servicegroup_membership_addition_with_dynamic_non_empty_result_preconditions : Check(10) {
    my $self                                          = shift;
    my $host_of_existing_host_service_in_servicegroup = shift;
    my $existing_host_service_in_servicegroup         = shift;
    my $host_of_unstable_host_service_in_servicegroup = shift;
    my $unstable_host_service_in_servicegroup         = shift;
    my $servicegroup                                  = shift;
    $self->reset_count();
    my $result = $self->test_condition(
	qw(host_service_exists_in_monarch),
	$host_of_existing_host_service_in_servicegroup,
	$existing_host_service_in_servicegroup
      )
      && $self->test_condition(
	qw(host_service_exists_in_gwcollagedb),
	$host_of_existing_host_service_in_servicegroup,
	$existing_host_service_in_servicegroup
      )
      && $self->test_condition( qw(servicegroup_exists_in_monarch),                 $servicegroup )
      && $self->test_condition( qw(servicegroup_exists_in_gwcollagedb),             $servicegroup )
      && $self->test_condition( qw(servicegroup_in_gwcollagedb_is_owned_by_nagios), $servicegroup )
      && $self->test_condition(
	qw(host_service_is_member_of_servicegroup_in_monarch),
	$host_of_existing_host_service_in_servicegroup,
	$existing_host_service_in_servicegroup,
	$servicegroup
      )
      && $self->test_condition(
	qw(not host_service_is_member_of_servicegroup_in_gwcollagedb),
	$host_of_existing_host_service_in_servicegroup,
	$existing_host_service_in_servicegroup,
	$servicegroup
      )
      && $self->test_condition(
	qw(host_service_exists_in_gwcollagedb),
	$host_of_unstable_host_service_in_servicegroup,
	$unstable_host_service_in_servicegroup
      )
      && $self->test_condition(
	qw(host_service_is_member_of_servicegroup_in_gwcollagedb),
	$host_of_unstable_host_service_in_servicegroup,
	$unstable_host_service_in_servicegroup,
	$servicegroup
      )
      && $self->test_condition(
	qw(not host_service_exists_in_monarch),
	$host_of_unstable_host_service_in_servicegroup,
	$unstable_host_service_in_servicegroup
      );
    $self->check_count();
    return $result;
}

sub check_existing_host_service_existing_servicegroup_servicegroup_membership_addition_with_dynamic_non_empty_result_analysis : Check(1) {
    my $self                                          = shift;
    my $delta                                         = shift;
    my $host_of_existing_host_service_in_servicegroup = shift;
    my $existing_host_service_in_servicegroup         = shift;
    my $host_of_unstable_host_service_in_servicegroup = shift;
    my $unstable_host_service_in_servicegroup         = shift;
    my $servicegroup                                  = shift;
    $self->reset_count();
    my $result = $self->test_condition(
	qw(host_service_will_be_added_to_existing_servicegroup),
	$delta,
	$host_of_existing_host_service_in_servicegroup,
	$existing_host_service_in_servicegroup,
	$servicegroup
    );
    $self->check_count();
    return $result;
}

sub check_existing_host_service_existing_servicegroup_servicegroup_membership_addition_with_dynamic_non_empty_result_postconditions : Check(2) {
    my $self                                          = shift;
    my $host_of_existing_host_service_in_servicegroup = shift;
    my $existing_host_service_in_servicegroup         = shift;
    my $host_of_unstable_host_service_in_servicegroup = shift;
    my $unstable_host_service_in_servicegroup         = shift;
    my $servicegroup                                  = shift;
    $self->reset_count();
    my $result = $self->test_condition(
	qw(host_service_is_member_of_servicegroup_in_gwcollagedb),
	$host_of_existing_host_service_in_servicegroup,
	$existing_host_service_in_servicegroup,
	$servicegroup
      )
      && $self->test_condition(
	qw(not host_service_exists_in_gwcollagedb),
	$host_of_unstable_host_service_in_servicegroup,
	$unstable_host_service_in_servicegroup
      );
    $self->check_count();
    return $result;
}

# ================================================================

sub check_new_host_service_existing_servicegroup_servicegroup_membership_addition_with_dynamic_non_empty_result_preconditions : Check(9) {
    my $self                                          = shift;
    my $host_of_new_host_service_in_servicegroup      = shift;
    my $new_host_service_in_servicegroup              = shift;
    my $host_of_unstable_host_service_in_servicegroup = shift;
    my $unstable_host_service_in_servicegroup         = shift;
    my $servicegroup                                  = shift;
    $self->reset_count();
    my $result =
      $self->test_condition( qw(host_service_exists_in_monarch), $host_of_new_host_service_in_servicegroup, $new_host_service_in_servicegroup )
      && $self->test_condition(
	qw(not host_service_exists_in_gwcollagedb),
	$host_of_new_host_service_in_servicegroup,
	$new_host_service_in_servicegroup
      )
      && $self->test_condition( qw(servicegroup_exists_in_monarch),                 $servicegroup )
      && $self->test_condition( qw(servicegroup_exists_in_gwcollagedb),             $servicegroup )
      && $self->test_condition( qw(servicegroup_in_gwcollagedb_is_owned_by_nagios), $servicegroup )
      && $self->test_condition(
	qw(host_service_is_member_of_servicegroup_in_monarch),
	$host_of_new_host_service_in_servicegroup,
	$new_host_service_in_servicegroup,
	$servicegroup
      )
      && $self->test_condition(
	qw(host_service_exists_in_gwcollagedb),
	$host_of_unstable_host_service_in_servicegroup,
	$unstable_host_service_in_servicegroup
      )
      && $self->test_condition(
	qw(host_service_is_member_of_servicegroup_in_gwcollagedb),
	$host_of_unstable_host_service_in_servicegroup,
	$unstable_host_service_in_servicegroup,
	$servicegroup
      )
      && $self->test_condition(
	qw(not host_service_exists_in_monarch),
	$host_of_unstable_host_service_in_servicegroup,
	$unstable_host_service_in_servicegroup
      );
    $self->check_count();
    return $result;
}

sub check_new_host_service_existing_servicegroup_servicegroup_membership_addition_with_dynamic_non_empty_result_analysis : Check(2) {
    my $self                                          = shift;
    my $delta                                         = shift;
    my $host_of_new_host_service_in_servicegroup      = shift;
    my $new_host_service_in_servicegroup              = shift;
    my $host_of_unstable_host_service_in_servicegroup = shift;
    my $unstable_host_service_in_servicegroup         = shift;
    my $servicegroup                                  = shift;
    $self->reset_count();
    my $result = $self->test_condition(
	qw(host_service_will_be_added),
	$delta,
	$host_of_new_host_service_in_servicegroup,
	$new_host_service_in_servicegroup
      )
      && $self->test_condition(
	qw(host_service_will_be_added_to_existing_servicegroup),
	$delta,
	$host_of_new_host_service_in_servicegroup,
	$new_host_service_in_servicegroup,
	$servicegroup
      );
    $self->check_count();
    return $result;
}

sub check_new_host_service_existing_servicegroup_servicegroup_membership_addition_with_dynamic_non_empty_result_postconditions : Check(2) {
    my $self                                          = shift;
    my $host_of_new_host_service_in_servicegroup      = shift;
    my $new_host_service_in_servicegroup              = shift;
    my $host_of_unstable_host_service_in_servicegroup = shift;
    my $unstable_host_service_in_servicegroup         = shift;
    my $servicegroup                                  = shift;
    $self->reset_count();
    my $result = $self->test_condition(
	qw(host_service_is_member_of_servicegroup_in_gwcollagedb),
	$host_of_new_host_service_in_servicegroup,
	$new_host_service_in_servicegroup,
	$servicegroup
      )
      && $self->test_condition(
	qw(not host_service_exists_in_gwcollagedb),
	$host_of_unstable_host_service_in_servicegroup,
	$unstable_host_service_in_servicegroup
      );
    $self->check_count();
    return $result;
}

# ================================================================

sub check_existing_host_service_new_servicegroup_servicegroup_membership_addition_preconditions : Check(5) {
    my $self                                 = shift;
    my $host_of_host_service_in_servicegroup = shift;
    my $host_service_in_servicegroup         = shift;
    my $servicegroup                         = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(host_service_exists_in_monarch),     $host_of_host_service_in_servicegroup, $host_service_in_servicegroup )
      && $self->test_condition( qw(host_service_exists_in_gwcollagedb), $host_of_host_service_in_servicegroup, $host_service_in_servicegroup )
      && $self->test_condition( qw(servicegroup_exists_in_monarch),     $servicegroup )
      && $self->test_condition( qw(not servicegroup_exists_in_gwcollagedb), $servicegroup )
      && $self->test_condition(
	qw(host_service_is_member_of_servicegroup_in_monarch),
	$host_of_host_service_in_servicegroup,
	$host_service_in_servicegroup, $servicegroup
      );
    $self->check_count();
    return $result;
}

sub check_existing_host_service_new_servicegroup_servicegroup_membership_addition_analysis : Check(2) {
    my $self                                 = shift;
    my $delta                                = shift;
    my $host_of_host_service_in_servicegroup = shift;
    my $host_service_in_servicegroup         = shift;
    my $servicegroup                         = shift;
    $self->reset_count();
    my $result =
      $self->test_condition( qw(servicegroup_will_be_added), $delta, $servicegroup )
      && $self->test_condition(
	qw(host_service_will_be_added_to_new_servicegroup),
	$delta, $host_of_host_service_in_servicegroup,
	$host_service_in_servicegroup, $servicegroup
      );
    $self->check_count();
    return $result;
}

sub check_existing_host_service_new_servicegroup_servicegroup_membership_addition_postconditions : Check(1) {
    my $self                                 = shift;
    my $host_of_host_service_in_servicegroup = shift;
    my $host_service_in_servicegroup         = shift;
    my $servicegroup                         = shift;
    $self->reset_count();
    my $result = $self->test_condition(
	qw(host_service_is_member_of_servicegroup_in_gwcollagedb),
	$host_of_host_service_in_servicegroup,
	$host_service_in_servicegroup, $servicegroup
    );
    $self->check_count();
    return $result;
}

# ================================================================

sub check_new_host_service_new_servicegroup_servicegroup_membership_addition_preconditions : Check(5) {
    my $self                                 = shift;
    my $host_of_host_service_in_servicegroup = shift;
    my $host_service_in_servicegroup         = shift;
    my $servicegroup                         = shift;
    $self->reset_count();
    my $result =
      $self->test_condition( qw(host_service_exists_in_monarch), $host_of_host_service_in_servicegroup, $host_service_in_servicegroup )
      && $self->test_condition( qw(not host_service_exists_in_gwcollagedb), $host_of_host_service_in_servicegroup,
	$host_service_in_servicegroup )
      && $self->test_condition( qw(servicegroup_exists_in_monarch),         $servicegroup )
      && $self->test_condition( qw(not servicegroup_exists_in_gwcollagedb), $servicegroup )
      && $self->test_condition(
	qw(host_service_is_member_of_servicegroup_in_monarch),
	$host_of_host_service_in_servicegroup,
	$host_service_in_servicegroup, $servicegroup
      );
    $self->check_count();
    return $result;
}

sub check_new_host_service_new_servicegroup_servicegroup_membership_addition_analysis : Check(3) {
    my $self                                 = shift;
    my $delta                                = shift;
    my $host_of_host_service_in_servicegroup = shift;
    my $host_service_in_servicegroup         = shift;
    my $servicegroup                         = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(host_service_will_be_added), $delta, $host_of_host_service_in_servicegroup, $host_service_in_servicegroup )
      && $self->test_condition( qw(servicegroup_will_be_added), $delta, $servicegroup )
      && $self->test_condition(
	qw(host_service_will_be_added_to_new_servicegroup),
	$delta, $host_of_host_service_in_servicegroup,
	$host_service_in_servicegroup, $servicegroup
      );
    $self->check_count();
    return $result;
}

sub check_new_host_service_new_servicegroup_servicegroup_membership_addition_postconditions : Check(1) {
    my $self                                 = shift;
    my $host_of_host_service_in_servicegroup = shift;
    my $host_service_in_servicegroup         = shift;
    my $servicegroup                         = shift;
    $self->reset_count();
    my $result = $self->test_condition(
	qw(host_service_is_member_of_servicegroup_in_gwcollagedb),
	$host_of_host_service_in_servicegroup,
	$host_service_in_servicegroup, $servicegroup
    );
    $self->check_count();
    return $result;
}

# ================================================================

sub check_host_inaction_preconditions : Check(2) {
    my $self             = shift;
    my $host_to_stay_put = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(host_exists_in_monarch),     $host_to_stay_put )
      && $self->test_condition( qw(host_exists_in_gwcollagedb), $host_to_stay_put );
    $self->check_count();
    return $result;
}

sub check_host_inaction_analysis : Check(2) {
    my $self             = shift;
    my $delta            = shift;
    my $host_to_stay_put = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(not host_will_be_deleted), $delta, $host_to_stay_put )
      && $self->test_condition( qw(not host_will_be_added),   $delta, $host_to_stay_put );
    $self->check_count();
    return $result;
}

sub check_host_inaction_postconditions : Check(1) {
    my $self             = shift;
    my $host_to_stay_put = shift;
    $self->reset_count();
    my $result = $self->test_condition( qw(host_exists_in_gwcollagedb), $host_to_stay_put );
    $self->check_count();
    return $result;
}

# ================================================================

sub check_host_service_inaction_preconditions : Check(2) {
    my $self                             = shift;
    my $host_of_host_service_to_stay_put = shift;
    my $host_service_to_stay_put         = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(host_service_exists_in_monarch),     $host_of_host_service_to_stay_put, $host_service_to_stay_put )
      && $self->test_condition( qw(host_service_exists_in_gwcollagedb), $host_of_host_service_to_stay_put, $host_service_to_stay_put );
    $self->check_count();
    return $result;
}

sub check_host_service_inaction_analysis : Check(2) {
    my $self                             = shift;
    my $delta                            = shift;
    my $host_of_host_service_to_stay_put = shift;
    my $host_service_to_stay_put         = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(not host_service_will_be_deleted), $delta, $host_of_host_service_to_stay_put, $host_service_to_stay_put )
      && $self->test_condition( qw(not host_service_will_be_added),   $delta, $host_of_host_service_to_stay_put, $host_service_to_stay_put );
    $self->check_count();
    return $result;
}

sub check_host_service_inaction_postconditions : Check(1) {
    my $self                             = shift;
    my $host_of_host_service_to_stay_put = shift;
    my $host_service_to_stay_put         = shift;
    $self->reset_count();
    my $result = $self->test_condition( qw(host_service_exists_in_gwcollagedb), $host_of_host_service_to_stay_put, $host_service_to_stay_put );
    $self->check_count();
    return $result;
}

# ================================================================

sub check_hostgroup_inaction_preconditions : Check(3) {
    my $self                  = shift;
    my $hostgroup_to_stay_put = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(hostgroup_exists_in_monarch),       $hostgroup_to_stay_put )
      && $self->test_condition( qw(hostgroup_exists_in_gwcollagedb),   $hostgroup_to_stay_put )
      && $self->test_condition( qw(not hostgroup_in_monarch_is_empty), $hostgroup_to_stay_put );
    $self->check_count();
    return $result;
}

sub check_hostgroup_inaction_analysis : Check(2) {
    my $self                  = shift;
    my $delta                 = shift;
    my $hostgroup_to_stay_put = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(not hostgroup_will_be_deleted), $delta, $hostgroup_to_stay_put )
      && $self->test_condition( qw(not hostgroup_will_be_added),   $delta, $hostgroup_to_stay_put );
    $self->check_count();
    return $result;
}

sub check_hostgroup_inaction_postconditions : Check(1) {
    my $self                  = shift;
    my $hostgroup_to_stay_put = shift;
    $self->reset_count();
    my $result = $self->test_condition( qw(hostgroup_exists_in_gwcollagedb), $hostgroup_to_stay_put );
    $self->check_count();
    return $result;
}

# ================================================================

sub check_servicegroup_inaction_preconditions : Check(3) {
    my $self                     = shift;
    my $servicegroup_to_stay_put = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(servicegroup_exists_in_monarch),       $servicegroup_to_stay_put )
      && $self->test_condition( qw(servicegroup_exists_in_gwcollagedb),   $servicegroup_to_stay_put )
      && $self->test_condition( qw(not servicegroup_in_monarch_is_empty), $servicegroup_to_stay_put );
    $self->check_count();
    return $result;
}

sub check_servicegroup_inaction_analysis : Check(2) {
    my $self                     = shift;
    my $delta                    = shift;
    my $servicegroup_to_stay_put = shift;
    $self->reset_count();
    my $result =
         $self->test_condition( qw(not servicegroup_will_be_deleted), $delta, $servicegroup_to_stay_put )
      && $self->test_condition( qw(not servicegroup_will_be_added),   $delta, $servicegroup_to_stay_put );
    $self->check_count();
    return $result;
}

sub check_servicegroup_inaction_postconditions : Check(1) {
    my $self                     = shift;
    my $servicegroup_to_stay_put = shift;
    $self->reset_count();
    my $result = $self->test_condition( qw(servicegroup_exists_in_gwcollagedb), $servicegroup_to_stay_put );
    $self->check_count();
    return $result;
}

1;

__END__

=head1 NAME

CommitTests - Test Monarch/Foundation Commit Operation

=head1 SYNOPSIS

To be provided.

=head1 VERSION

0.0.4

=head1 DESCRIPTION

This package details all the application-level tests of a Monarch Commit
operation.  There is really only one Test from the standpoint of the
standard Perl Test:: classes, but many different unit tests are run as
part of that one Test.  The structure here provides for defining both
logical and virtual tests, the latter representing convenient groupings
of the individual (logical) unit tests.

=head1 REFERENCES

Details of these tests and maintaining the required setup are documented
in our "Monarch Commit Operation Integration Tests" article:
https://kb.groundworkopensource.com/display/GWENG/Monarch+Commit+Operation+Integration+Tests

The Test::Class package that this script is based on is documented at:
http://search.cpan.org/~ether/Test-Class-0.46/lib/Test/Class.pm
(though the exact version of Test::Class that is in our build should be
examined to see how it might differ from that doc).

"Perl Testing:  A Developer's Notebook", Ian Langworth and chromatic, 2005

"Test Driven Development With Perl:  A Stonehenge Consulting Course"
http://cdn.oreillystatic.com/en/assets/1/event/12/Practical%20Test-driven%20Development%20Presentation.pdf

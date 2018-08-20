package GW::Test;

# This package is designed to support testing a variety of component pieces
# within GroundWork Monitor, starting first with Perl code and perhaps later
# expanding to other application contexts.  It provides actions that can and
# should be factored out of multiple tests.

# Copyright 2014 GroundWork Open Source, Inc. ("GroundWork")
# All rights reserved.  Use is subject to GroundWork commercial license terms.

# Revision History:
#
# 2014-07-10 GH 0.0.0	Original version, still in development.
# 2014-07-23 GH 0.0.1	Port to be runnable under Ubuntu.
#			Improve child-process failure diagnostics.
# 2014-07-24 GH 0.0.2	Add the abilit to start and stop postgresql.

# Notes:
# * This package is still in its infancy.  Its entire structure is subject to change
#   as we adapt it to the real-world needs of testing various GroundWork components.

# STILL TO DO:
# * This package might better be named GW::Test::Utility, depending on what gets
#   included.  We might want to have separate packages for other aspects of testing.
#   Some additional packages might be:
#       GW::MonarchCommit::Test -- defines all Monarch Commit tests
#       GW::Test::MockFoundationSocket -- spawns a process that reads a
#           designated socket and returns all data written to that socket
#           back to the calling process.
# * Fix all exception handling to follow some sane principles.
# * Fix all test routines to use proper database-driven quoting of values substituted
#   into SQL statements, instead of trying to do so manually.  (This is probably done
#   now, but we should check for any residual cases.)

# ================================ Perl Setup ================================

use warnings;
use strict;

use attributes;  # to provide attributes::reftype() [Programming Perl, 4/e, p. 1003]

# ================================ Package Setup ================================

our ( @ISA, @EXPORT, @EXPORT_OK, %EXPORT_TAGS, $VERSION );

BEGIN {
    use Exporter ();
    @ISA         = qw(Exporter);
    @EXPORT      = qw();
    @EXPORT_OK   = qw( log is_valid_dns_hostname is_valid_object_name );
    %EXPORT_TAGS = ( DEBUG => [ @EXPORT, @EXPORT_OK ] );
    $VERSION     = "0.0.2";
}

# ================================ Modules ================================

use Sys::Hostname;  # to provide hostname()

use Config;

use DBI;
use Test::Most;

# use IO::Handle;

use Cwd 'realpath';

use Data::Dumper;    # For debugging for when Smart::Comments doesn't hack it.
$Data::Dumper::Indent   = 1;
$Data::Dumper::Sortkeys = 1;

# ================================ Constants ================================

my $postgresql_base = '/usr/local/groundwork/postgresql';
my $psql            = "$postgresql_base/bin/psql";
my $pg_dump         = "$postgresql_base/bin/pg_dump";

my $db_properties_file = '/usr/local/groundwork/config/db.properties';
my %db_properties_tag = ( monarch => 'monarch', gwcollagedb => 'collage' );

# ================================ Variables ================================

# ================================ Routines ================================

# Test::More::diag() writes to STDERR instead of STDOUT, so we supply a non-drop-in
# replacement here.  Normally, you export this routine to call it simply as log(...).
# An alternative (with a slightly different interpretation of multiple arguments)
# would be to call Test::Builder::failure_output(Test::Builder::output()) to
# redirect diag() output to STDOUT.
sub log {
    for (@_) {
	print '#', length($_) ? ' ' : '', $_, $_ =~ /\n$/ ? '' : "\n";
    }
    return 0;
}

# See the Config(3pm) man page for details of this magic formulation.
sub system_signal_name {
    my $signal_number = shift;
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

sub wait_status_message {
    my $wait_status   = shift;
    my $os_error      = shift;

    # The upstream caller ought to always pass in this parameter, being careful to
    # capture it concurrently with the $wait_status so the two values are synchronized.
    # But we know that historically, this parameter has not been required.  So if the
    # caller has not passed in the value, we punt and attempt to grab the current
    # value, even though it might no longer reflect whatever error occurred when the
    # $wait_status was captured.
    $os_error = $! if not defined $os_error;

    # A $wait_status of -1 is a special case which does not conform to the analysis below.
    # So there's no point in misleading ourselves with that form of output.
    return "could not execute program:  $os_error" if $wait_status == -1;

    my $exit_status   = $wait_status >> 8;
    my $signal_number = $wait_status & 0x7F;
    my $dumped_core   = $wait_status & 0x80;
    my $signal_name   = system_signal_name($signal_number) || "$signal_number is unknown";
    my $message = "exit status $exit_status" . ( $signal_number ? " (signal $signal_name)" : '' ) . ( $dumped_core ? ' (with core dump)' : '' );
    return $message;
}

# ================================================================

sub log_ids {
    print "#      Real UID = $< (" . ( getpwuid($<) || 'unknown user' ) . ")\n";
    print "#      Real GID = $( (" . ( getgrgid($() || 'unknown group' ) . ")\n";
    print "# Effective UID = $> (" . ( getpwuid($>) || 'unknown user' ) . ")\n";
    print "# Effective GID = $) (" . ( getgrgid($)) || 'unknown group' ) . ")\n";
}

sub run_as_effective_root {
    $) = "0 0";
    $> = 0;
    die "ERROR:  Cannot switch to running as root.\n" if $> != 0;
    return 1;
}

sub run_as_effective_nagios {
    my $nagios_uid = getpwnam 'nagios';
    my $nagios_gid = getgrnam 'nagios';
    die "ERROR:  Cannot find nagios-user info to run as nagios.\n" if not defined $nagios_uid or not defined $nagios_gid;
    $) = "$nagios_gid $nagios_gid";
    $> = $nagios_uid;
    die "ERROR:  Cannot switch to running as nagios.\n" if $> != $nagios_uid;
    return 1;
}

sub running_as_real_root {
    return $< == 0;
}

sub running_as_effective_root {
    return $> == 0;
}

sub running_as_real_nagios {
    my $nagios_uid = getpwnam 'nagios';
    die "ERROR:  Cannot find nagios-user info.\n" if not defined $nagios_uid;
    return $< == $nagios_uid;
}

sub running_as_effective_nagios {
    my $nagios_uid = getpwnam 'nagios';
    die "ERROR:  Cannot find nagios-user info.\n" if not defined $nagios_uid;
    return $> == $nagios_uid;
}

# ================================================================

# Internal routine.
sub control_groundwork {
    my $start_or_stop = shift;
    my $component     = shift;
    run_as_effective_root();
    ## CentOS and Ubuntu use different paths to "service", so we need to dynamically adapt here.
    my $service = -x '/sbin/service' ? '/sbin/service' : -x '/usr/sbin/service' ? '/usr/sbin/service' : undef;
    die "ERROR:  Could not $start_or_stop $component (could not find a runnable \"service\" program).\n" if not defined $service;
    my @results     = qx($service groundwork $start_or_stop $component 2>&1);
    my $child_error = $?;
    my $os_error    = $!;
    print @results;
    die "ERROR:  Could not $start_or_stop $component (" . wait_status_message( $child_error, $os_error ) . ").\n" if $child_error;
    run_as_effective_nagios();
}

sub start_postgresql {
    control_groundwork( 'start', 'postgresql' );
}

sub stop_postgresql {
    control_groundwork( 'stop', 'postgresql' );
}

sub start_nagios {
    control_groundwork( 'start', 'nagios' );
}

sub stop_nagios {
    control_groundwork( 'stop', 'nagios' );
}

sub start_gwservices {
    control_groundwork( 'start', 'gwservices' );
}

sub stop_gwservices {
    control_groundwork( 'stop', 'gwservices' );
}

# ================================================================

sub new {
    my ( $invocant, $options ) = @_;
    my $class = ref($invocant) || $invocant;    # object or class name

    my $verbose = 0;

    if (defined $options) {
        if (ref $options eq 'HASH') {
	    $verbose = 1 if $options->{verbose};
	}
	else {
	    die "ERROR:  Invalid options (not a hashref) passed to GW::Test::new()\n";
	}
    }

    my %config  = (
	verbose  => $verbose,
    );
    my $self = bless( \%config, $class );

    return $self;
}

# ================================================================

# We need this routine because a simple qx($commands), if it needs to run a shell to run the $commands,
# will drop the effective IDs entirely and just run with the real IDs instead.  That is exactly counter
# to what we want, and the opposite of what happens if Perl doesn't run a shell to run the $commands.
# So we provide a routine here to allow the user to force the issue.
sub execute_as_effective_user_and_group {
    my ( $self, $commands ) = @_;

    die "ERROR:  Cannot fork child to run command ($!).\n" unless defined( my $pid = open( FROMCHILD, '-|' ) );
    if ($pid) {
	## parent process

	# Individual pipe reads are not error-checked, as there seems to be no
	# documented means to do so in this formulation.  We rely on failure of
	# the close() call to detect any errors in transmission.
	my @results = <FROMCHILD>;

	if ( close FROMCHILD ) {
	    ## close succeeded; all is well, and no adjustments to $? (child exit status) are needed
	}
	elsif ($!) {
	    ## pipe failure, which has nothing to do with $commands, so we just abort
	    die "ERROR:  Encountered pipe failure when running command ($!).\n";
	}
	else {
	    ## We have a $commands failure, which should be reported as such back to our caller.
	    ## $? here represents the best we could do to reflect the actual exit status of
	    ## the executed $commands (as massaged in the child-process branch).  But it is only
	    ## reliable as we see it here in the parent-process branch in the sense that it will
	    ## be non-zero here.  It will represent either the exit status of the $commands, or
	    ## in the alternative, some overhead info that reflects whether $commands were
	    ## interrupted or dumped core.
	}
	return @results;
    }
    else {
	## child process

	# Make sure that this child process doesn't disconnect() any open database handles,
	# as that would interfere with their continued use in the parent process.
	if ( exists $self->{databases} ) {
	    foreach my $dbname ( keys %{ $self->{databases} } ) {
		$self->{$dbname}->{InactiveDestroy} = 1;
	    }
	}

	# Set the UID and GID to the current EUID and GUID, before executing the specified
	# command.  This is necessary because otherwise, when a shell is invoked by qx(),
	# it loses all effective-id info and just carries forward the real-id values into
	# the executing program.  That is exactly the opposite of what I expected.
	$! = 0;
	$< = $>;
	die "ERROR:  Could not change real UID from $< to $> ($!).\n" if $!;
	$( = $) + 0;
	die "ERROR:  Could not change real GID from \"$(\" to \"$)\" ($!).\n" if $!;

	# We force the use of a shell, by always executing multiple commands, in order to
	# have a consistent behavior with regard to whether or not the uid/euid stuff is
	# handled as expected.  Output from the executed $commands are written to STDOUT,
	# which means written to the pipe to the parent process.
	print qx(/bin/true; $commands);

	# Hand back the exit status of the sub-process back to our parent, as
	# best we can, ensuring that we always return a non-zero value if the
	# overall wait status was non-zero.
	exit (($? >> 8) || $?);
    }
}

# ================================================================

# FIX LATER:  Change this to allow drawing the DB type and access credentials from some other source,
# to support connections to databases whose credentials are not stored in the db.properties file.
sub open_database {
    my ( $self, $database ) = @_;
    my ( $dbtype, $dbhost, $dbname, $dbuser, $dbpass ) = undef;

    my $hostname = hostname();

    my $db_tag = $db_properties_tag{$database};
    die "ERROR:  Unknown database '$database'.\n" if not defined $db_tag;

    if (!open( FILE, '<', $db_properties_file )) {
	die "ERROR:  Cannot open $db_properties_file file on $hostname ($!).\n";
    }

    while ( my $line = <FILE> ) {
	if ( $line =~ /^\s*global\.db\.type\s*=\s*(\S+)/ )  { $dbtype = $1 }
	if ( $line =~ /^\s*$db_tag\.dbhost\s*=\s*(\S+)/ )   { $dbhost = $1 }
	if ( $line =~ /^\s*$db_tag\.database\s*=\s*(\S+)/ ) { $dbname = $1 }
	if ( $line =~ /^\s*$db_tag\.username\s*=\s*(\S+)/ ) { $dbuser = $1 }
	if ( $line =~ /^\s*$db_tag\.password\s*=\s*(\S+)/ ) { $dbpass = $1 }
    }
    close(FILE);

    # Historical default.  Will be 'postgresql' as of GWMEE 6.6.
    $dbtype = 'mysql' if not defined $dbtype;

    if ( !defined($dbname) or !defined($dbhost) or !defined($dbuser) or !defined($dbpass) ) {
	die "ERROR:  On $hostname, cannot read the database configuration for database '$database'.\n";
    }

    my $dsn = '';
    if ( defined($dbtype) && $dbtype eq 'postgresql' ) {
	$dsn = "DBI:Pg:dbname=$dbname;host=$dbhost";
    }
    else {
	$dsn = "DBI:mysql:database=$dbname;host=$dbhost";
    }

    my $dbh = DBI->connect( $dsn, $dbuser, $dbpass, {
	AutoCommit => 1,
	RaiseError => 1,
	# PrintError => 0  # Should we use this too?  Review all error handling.
    } );

    # Record the fact that we have an open databse handle.  This is needed later
    # on whenever we fork(), so we can tell the child process not to disconnect
    # database handles that go out of scope.  Having the child process do so will
    # inadvertently close the parent process handle as well (as seen by the server),
    # and thus the parent would be unable to continue using that handle.
    $self->{databases}{$database} = 1;

    # Capture the database handle itself, for future use by this package.
    $self->{$database} = $dbh;
}

sub close_database {
    my ( $self, $dbname ) = @_;
    if ($self->{$dbname}) {
	$self->{$dbname}->disconnect();
    }
    delete $self->{databases}{$dbname};
    delete $self->{$dbname};
}

# Here, $query is expected to be of the form "select count(*) from XXX where ...".
sub query_database {
    my ( $self, $dbname, $query ) = @_;
    my $count;
    eval { $count = $self->{$dbname}->selectrow_array($query); };
    die $@ if $@;
    return $count;
}

# Here, $query is expected to be of the form "select table_name.column_name from XXX where ...".
sub query_database_rows {
    my ( $self, $dbname, $query, $attr ) = @_;
    $attr = {} if not defined $attr;
    my $arrayref;
    eval { $arrayref = $self->{$dbname}->selectcol_arrayref( $query, $attr ); };
    die $@ if $@;
    return $arrayref;
}

# ================================================================

sub load_database {
    my ( $self, $filepath, $dbname, $options, $outcome, $results ) = @_;

    my %valid_options = (
	dbtype => 1,
	dbhost => 1,
	dbname => 1,
	dbuser => 1,
	dbpass => 1
    );

    if ( defined $options ) {
	if ( ref $options ne 'HASH' ) {
	    die "ERROR:  load_database() options argument must be undefined or a hash.\n";
	}
	else {
	    foreach my $key ( keys %$options ) {
		if ( not exists $valid_options{$key} ) {
		    die "ERROR:  Invalid option '$key' for load_database().\n";
		}
	    }
	}
    }
    else {
	$options = {};
    }

    %$outcome = ();
    @$results = ();

    die "ERROR:  Cannot load to an undefined database name.\n" if not defined $dbname;

    # Note that this loading of the database is not interlocked against changes by any other possible concurrent actor.
    if ( $dbname eq 'monarch' or $dbname eq 'gwcollagedb' ) {
	my $errors;
	my $load_results;
	my $exit_status;

	print "# NOTICE:  loading the \"$dbname\" database\n" if $self->{verbose};

	( $errors, $load_results, $exit_status ) = $self->load_database_from_dump_file( $filepath, $dbname, $options );
	my $got_load_errors = 0;
	foreach (@$load_results) {
	    if (/error/i) {
		$got_load_errors = 1;
		last;
	    }
	}
	push @$results, "ERROR:  Error(s) occurred during processing; see below.\n" if $got_load_errors;
	push @$results, @$load_results;
	my $status = not( @$errors || $got_load_errors || $exit_status );
	$outcome->{status}         = $status;
	$outcome->{results}        = $results if @$results;
	$outcome->{errors}         = $errors if @$errors;
	$outcome->{status_message} = wait_status_message($exit_status);

	print "# \"$dbname\" database loading errors:\n", @$errors if @$errors;
	print "# \"$dbname\" database loading results:\n", @$results if @$results;

	return $status;
    }
    else {
	die "ERROR:  Cannot load unknown database '$dbname'.\n";
    }
}

# ================================================================

# The order of the $dbname and $filepath parameters here is deliberately swapped from that used
# by load_database(), to prevent any possible confusion between those two routines if the wrong
# one is accidentally called.  If that happens, validation of $dbname should catch the error.
sub dump_database {
    my ( $self, $dbname, $filepath, $options, $outcome, $results ) = @_;

    my %valid_options = (
	dbtype => 1,
	dbhost => 1,
	dbname => 1,
	dbuser => 1,
	dbpass => 1
    );

    if ( defined $options ) {
	if ( ref $options ne 'HASH' ) {
	    die "ERROR:  dump_database() options argument must be undefined or a hash.\n";
	}
	else {
	    foreach my $key ( keys %$options ) {
		if ( not exists $valid_options{$key} ) {
		    die "ERROR:  Invalid option '$key' for dump_database().\n";
		}
	    }
	}
    }
    else {
	$options = {};
    }

    %$outcome = ();
    @$results = ();

    die "ERROR:  Cannot dump from an undefined database name.\n" if not defined $dbname;

    # Note that this dumping of the database is not interlocked against changes by any other possible concurrent actor.
    if ( $dbname eq 'monarch' or $dbname eq 'gwcollagedb' ) {
	my $errors;
	my $dump_results;
	my $exit_status;

	print "# NOTICE:  dumping the \"$dbname\" database\n" if $self->{verbose};

	( $errors, $dump_results, $exit_status ) = $self->dump_database_to_dump_file( $dbname, $filepath, $options );
	my $got_dump_errors = 0;
	foreach (@$dump_results) {
	    if (/error/i) {
		$got_dump_errors = 1;
		last;
	    }
	}
	push @$results, "ERROR:  Error(s) occurred during processing; see below.\n" if $got_dump_errors;
	push @$results, @$dump_results;
	my $status = not( @$errors || $got_dump_errors || $exit_status );
	$outcome->{status}         = $status;
	$outcome->{results}        = $results if @$results;
	$outcome->{errors}         = $errors if @$errors;
	$outcome->{status_message} = wait_status_message($exit_status);

	print "# \"$dbname\" database dumping errors:\n", @$errors if @$errors;
	print "# \"$dbname\" database dumping results:\n", @$results if @$results;

	return $status;
    }
    else {
	die "ERROR:  Cannot dump unknown database '$dbname'.\n";
    }
}

# ================================================================

sub db_credentials {
    my $hostname      = shift;
    my $database_name = shift;
    my $options       = shift;
    my %access        = ();
    my @errors        = ();

    my $dbtype;
    my $dbhost;
    my $dbname;
    my $dbuser;
    my $dbpass;

    my $db_tag = $db_properties_tag{$database_name};
    die "ERROR:  Unknown database '$database_name'.\n" if not defined $db_tag;

    if ( !open( FILE, '<', $db_properties_file ) ) {
	push @errors, "Error:  Cannot open $db_properties_file on $hostname ($!).";
	return \@errors, \%access;
    }

    # FIX LATER:  Someday, we should support a possible non-default $dbport
    # here as well, once the properties file contains that information.
    while ( my $line = <FILE> ) {
	if    ( $line =~ /^\s*global\.db\.type\s*=\s*(\S+)/  ) { $dbtype = $1 }
	elsif ( $line =~ /^\s*$db_tag\.dbhost\s*=\s*(\S+)/   ) { $dbhost = $1 }
	elsif ( $line =~ /^\s*$db_tag\.database\s*=\s*(\S+)/ ) { $dbname = $1 }
	elsif ( $line =~ /^\s*$db_tag\.username\s*=\s*(\S+)/ ) { $dbuser = $1 }
	elsif ( $line =~ /^\s*$db_tag\.password\s*=\s*(\S+)/ ) { $dbpass = $1 }
    }

    close(FILE);

    # Historical default.  Is 'postgresql' as of GWMEE 6.6.
    $dbtype = 'mysql' if not defined $dbtype;

    die "ERROR:  Options dbname \"$options->{dbname}\" does not match the configured database \"$dbname\" for the $db_tag database.\n"
      if ( defined( $options->{dbname} ) and $dbname ne $options->{dbname} );

    ## Implement user-specified overrides to the configured setup.
    ## For now, we only allow access credentials to be overridden.
    # $dbtype = $options->{dbtype} if $options->{dbtype};
    # $dbhost = $options->{dbhost} if $options->{dbhost};
    $dbuser = $options->{dbuser} if $options->{dbuser};
    $dbpass = $options->{dbpass} if $options->{dbpass};

    if ( !defined($dbname) or !defined($dbhost) or !defined($dbuser) or !defined($dbpass) ) {
	push @errors, "Error:  Cannot read the database configuration on $hostname.";
	return \@errors, \%access;
    }

    # Simple security checks, to prevent command-line injection vulnerabilities below.
    if ( $dbname =~ /['=\\]/ or $dbhost =~ /['\\]/ or $dbuser =~ /['\\]/) {
	push @errors, "Error:  Found invalid database configuration on $hostname.";
	return \@errors, \%access;
    }

    %access = (
	dbtype => $dbtype,
	dbhost => $dbhost,
	dbname => $dbname,
	dbuser => $dbuser,
	dbpass => $dbpass,
    );

    return \@errors, \%access;
}

# This routine is stolen almost wholesale from our existing core/monarch/bin/load_monarch.pl script.
# That accounts for the complexity of the processing here.  This is intended to be an internal routine
# within this package, not something to be called directly by outside code.

# Possible debug levels:
# 0 = print only simple-form error messages
# 1 = print error messages with file and line numbers
# 2 = print error messages with file and line numbers, and all SQL responses
#     (voluminous and generally not of great interest)
my $debug_loading_database = 1;

sub load_database_from_dump_file {
    my ( $self, $db_dump_file, $database_name, $options ) = @_;

    my @errors  = ();
    my @results = ();
    my $exit_status = 1 << 8;  # default exit status is an error

    my $hostname = hostname();

    if ( !-f $db_dump_file || !-r $db_dump_file ) {
	push @errors, "Error:  Cannot access $db_dump_file on $hostname" . ( $! ? " ($!)." : '' );
	return \@errors, \@results, $exit_status;
    }

    my ( $errors, $access ) = db_credentials( $hostname, $database_name, $options );
    return $errors, \@results, $exit_status if @$errors;

    # FIX LATER:  Now that we support remote databases, it would be good if we had
    # a way to identify the parent server and where its database resides, and to
    # prevent overwriting the parent's own database by further validating $dbname.
    # In the meantime, the effective blocking of such a tragedy should still occur
    # because our ~/.pgpass file won't contain access credentials for the parent's
    # database, wherever it resides.

    if ($access->{dbtype} eq 'postgresql') {
	## FIX LATER:  This could be modified to call our (future) tools/restore_db.pl
	## script, so we are guaranteed that we run ANALYZE and/or VACUUM as needed
	## every time the database is loaded this say (although we do have autovacuum
	## turned on, so it's not clear that would be useful).  Another possibility
	## is that our restore script might recognize a MySQL dump and automatically
	## convert it into an equivalent PostgreSQL dump before loading it.

	push @results, "# Loading the \"$access->{dbname}\" database on $hostname ...\n";

	# NOTE:  We would like to use "-v ON_ERROR_STOP=" as well, to get $psql to
	# halt and report a problem in its exit code if an error occurs.  However,
	# if we did that, it might be impossible to recover after an error occurs
	# where certain tables have been dropped and are no longer present.  The
	# dump file starts with commands that try to drop secondary objects
	# (indexes, sequences, etc.) associated with a table, before the table
	# itself is dropped.  If the table is already gone, these initial DROP
	# commands will necessarily fail, which could make it impossible to
	# restore from the dump file if we stopped on the first error.  So we
	# reluctantly don't use ON_ERROR_STOP here.  (We ought to file a bug
	# report and get pg_dump fixed so its "--clean" output uses IF EXISTS
	# clauses and other logic, so it can be restored to an empty database
	# without generating any errors.)

	# We don't prepend "$psql" with "$env PGPASSWORD='$dbpass'" because we don't
	# want to stuff critical passwords on command lines where they may be visible.
	# Instead, we depend on a ~/.pgpass setup for the database-access credentials.
	# We also don't want to allow overriding the ~/.pgpass setup with settings
	# in environment variables, so we kill that possibility here.
	local %ENV = %ENV;
	delete $ENV{PGCLIENTENCODING};
	delete $ENV{PGDATABASE};
	delete $ENV{PGDATESTYLE};
	delete $ENV{PGGEQO};
	delete $ENV{PGHOSTADDR};
	delete $ENV{PGHOST};
	delete $ENV{PGLOCALEDIR};
	delete $ENV{PGOPTIONS};
	delete $ENV{PGPASSFILE};
	delete $ENV{PGPASSWORD};
	delete $ENV{PGPORT};
	delete $ENV{PGSERVICEFILE};
	delete $ENV{PGSERVICE};
	delete $ENV{PGSYSCONFDIR};
	delete $ENV{PGTZ};
	delete $ENV{PGUSER};
	$ENV{PGCONNECT_TIMEOUT} = 20;
	$ENV{PGREQUIREPEER} = 'postgres';
	$ENV{SHELL} = '/bin/false';
	$ENV{PATH} = '/bin:/sbin:/usr/bin:/usr/sbin';

	# If the user needed to override the config file, this password probably
	# isn't in the ~/.pgpass file, so we'll need to specify it explicitly.
	$ENV{PGPASSWORD} = $access->{dbpass} if $options->{dbpass};

	# The statements we want to drop from the dump file are exactly these, because
	# they generate ERROR and WARNING messages of no consequence to our purposes of
	# re-loading an existing database.
	#
	#     DROP EXTENSION plpgsql;
	#     DROP SCHEMA public;
	#     CREATE SCHEMA public;
	#     COMMENT ON SCHEMA public IS 'standard public schema';
	#     COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';
	#     REVOKE ALL ON SCHEMA public FROM PUBLIC;
	#     REVOKE ALL ON SCHEMA public FROM postgres;
	#     GRANT ALL ON SCHEMA public TO postgres;
	#     GRANT ALL ON SCHEMA public TO PUBLIC;
	#
	# We blank the lines rather than delete them, so the line numbers for the rest
	# of the lines in the file remain intact and are still valid with respect to
	# the original file in whatever error messages might be produced in debug mode.
	#
	# Note that it is possible for table data to contain words like SCHEMA that we
	# wish to filter out of the surrounding SQL commands (and this has been seen in
	# actual customer data).  To ensure that there is no corruption of the data, our
	# sed-based filtering must first pass through the blocks of table data ("COPY"
	# through "\.") untouched, before making any changes to the rest of the file.

	# The -f option we use shows "-" as the filename in error/warning messages,
	# so we restore it afterward by filtering the results.
	# Error messages will be printed on STDERR, not STDOUT, and will be emitted
	# regardless of the setting (or not) of the -o option.
	my $in  = $debug_loading_database >= 1 ? '-f -' : '';
	my $out = $debug_loading_database >= 2 ? '' : '-o /dev/null';

	(my $escaped_path = $db_dump_file) =~ s{/}{\\/}g;

	# FIX MINOR:  We should probably use ON_ERROR_STOP in the following command.

	# We need lots of very careful escaping here, for multiple levels of protection.  In
	# order as the string is interpreted and executed, we protect against Perl string
	# interpretation of backslashes and dollar-signs in the middle of the qq()-quoted
	# string (done with backslashes); to protect against the qq()-system()-shell
	# interpretation of quoting and escaping of backslashes and again the dollar-signs
	# (done with backslashes, at the same time that double-quotes are applied [which do
	# not affect that backslash-quoting]), to protect against bash interpretation of
	# backslashes (done via the single-quoting of the entire sed command expression), and
	# then finally to provide sed escaping of the intended backslash and dot characters
	# in the match pattern, so they are both taken literally.  So in reverse construction,
	# the basic ^\.$ match pattern (with backslash and dot to be interpreted literally)
	# becomes ^\\\.$ as sed needs to see the backslash-escape and magic-dot characters
	# escaped, which then becomes '^\\\.$' as it is single-quoted for protection against
	# bash, which becomes "'^\\\\\\.\$'" as sh needs to see the backslash-escape and
	# dollar-sign characters escaped, which then finally becomes "'^\\\\\\\\\\\\.\\\$'"
	# to protect against Perl string unescaping and interpretation of backslash and
	# dollar-sign characters.

	my $load_commands = qq(bash -c "
	    set -e;
	    set -o pipefail;
	    sed -e '/^COPY /,/^\\\\\\\\\\\\.\\\$/{p;d}' -e '/plpgsql/s/.*//' -e '/SCHEMA/s/.*//' $db_dump_file | \
	    $psql --host='$access->{dbhost}' --username='$access->{dbuser}' --no-password --dbname='$access->{dbname}' $in $out 2>&1 | \
	    sed -e 's/^psql.bin:-:/$escaped_path:/'
	" 2>&1);

	# We can print this if needed for diagnostic purposes.  Otherwise, it just clutters up the log.
	push @results, $load_commands . "\n" if $self->{verbose};

	push @results, $self->execute_as_effective_user_and_group($load_commands);
	my $child_error = $?;
	push @results, "# Database-load child status = " . wait_status_message($child_error) . "\n";

	# Report the exit status of the pipeline:  the status of the rightmost command to die or
	# exit with a non-zero status, or zero.  This can be interpreted by wait_status_message().
	# This way, we get some indication of whether the database loading failed.  (Actually,
	# without ON_ERROR_STOP, it's unlikely that $psql will report anything but a zero exit
	# status, since script errors will be ignored.  Still, this will report certain kinds of
	# fatal errors.)
	return \@errors, \@results, $child_error;
    }
    else {
	push @errors, "Error:  On $hostname, bad database type (global.db.type) found in db.properties file.";
	return \@errors, \@results, $exit_status;
    }
}

sub dump_database_to_dump_file {
    my ( $self, $database_name, $db_dump_file, $options ) = @_;

    my @errors  = ();
    my @results = ();
    my $exit_status = 1 << 8;  # default exit status is an error

    my $hostname = hostname();

    # We attempt to validate $db_dump_file path to restrict it to known good locations,
    # so we don't allow overwriting of arbitrary files.  However, since the dump file
    # itself may not exist, what we really need to do is to validate the location of
    # the directory in which it will be placed.  Not allowing a relative pathname to be
    # specified is just a matter of convenience here, so we don't need to go discovering
    # the current working directory.  That restriction could be lifted (with revision
    # not just to that test, but to some of the pattern matching here) in some future
    # version of this code.

    die "FATAL:  The dump file name is undefined.\n" if not defined $db_dump_file;
    die "FATAL:  The dump file name is not an absolute pathname.\n" if $db_dump_file !~ m{^/};
    my ( $base_dir_path, $dump_file_name ) = $db_dump_file =~ m{(.*/)(.*)};
    die "FATAL:  The specified database dump file ($db_dump_file) is a directory, not a file.\n"
      if $dump_file_name eq ''
	  or $dump_file_name eq '.'
	  or $dump_file_name eq '..'
	  or -d $db_dump_file;

    # Trim all trailing slashes, but leave the first if the string is all slashes.
    $base_dir_path =~ s{(.)(.*?)/*$}{$1$2};

    my $abs_dir_path = realpath($base_dir_path);

    # Validate that the absolute directory path starts with a sensible path, to avoid
    # symlink or parent-directory references sidestepping our security precautions.
    die "FATAL:  The specified database dump file directory ($base_dir_path) does not exist.\n" if not defined $abs_dir_path;
    ## Since this path will be used to write into the filesystem, validate that it will not overwrite any critical files.
    die "FATAL:  The specified database dump file ($db_dump_file) does not reside under the /tmp/ directory.\n"
      if $abs_dir_path !~ m{^/tmp(/|$)};
    $db_dump_file = "$abs_dir_path/$dump_file_name";

    my ( $errors, $access ) = db_credentials( $hostname, $database_name, $options );
    return $errors, \@results, $exit_status if @$errors;

    my $postgres_dump = $pg_dump;
    if ( not -x $postgres_dump ) {
	push @errors, "Error:  Cannot find pg_dump!  Unable to dump the $database_name database.";
    }
    else {
	local %ENV = %ENV;
	delete $ENV{PGCLIENTENCODING};
	delete $ENV{PGDATABASE};
	delete $ENV{PGDATESTYLE};
	delete $ENV{PGGEQO};
	delete $ENV{PGHOSTADDR};
	delete $ENV{PGHOST};
	delete $ENV{PGLOCALEDIR};
	delete $ENV{PGOPTIONS};
	delete $ENV{PGPASSFILE};
	delete $ENV{PGPASSWORD};
	delete $ENV{PGPORT};
	delete $ENV{PGSERVICEFILE};
	delete $ENV{PGSERVICE};
	delete $ENV{PGSYSCONFDIR};
	delete $ENV{PGTZ};
	delete $ENV{PGUSER};
	$ENV{PGCONNECT_TIMEOUT} = 20;
	$ENV{PGREQUIREPEER} = 'postgres';
	$ENV{SHELL} = '/bin/false';
	$ENV{PATH} = '/bin:/sbin:/usr/bin:/usr/sbin';

	# If the user needed to override the config file, this password probably
	# isn't in the ~/.pgpass file, so we'll need to specify it explicitly.
	$ENV{PGPASSWORD} = $access->{dbpass} if $options->{dbpass};

	my $dump_command = "$postgres_dump --host='$access->{dbhost}' --username='$access->{dbuser}' --no-password"
	  . " --file='$db_dump_file' --format=plain --clean --encoding=LATIN1 $access->{dbname} 2>&1";

	# We can print this if needed for diagnostic purposes.  Otherwise, it just clutters up the log.
	push @results, $dump_command . "\n" if $self->{verbose};

	push @results, $self->execute_as_effective_user_and_group(qq($dump_command));
	my $child_error = $?;
	push @results, "# Database-dump child status = " . wait_status_message($child_error) . "\n";

	if ($child_error) {
	    unshift @results, 'Error:  Dump command failed (' . wait_status_message($child_error) . ").\n";
	    unshift @results, $dump_command . "\n";
	    push @errors, @results;
	}
	return \@errors, \@results, $child_error;
    }

    return \@errors, \@results, $exit_status;
}

# ================================================================

sub monarch_config_value {
    my ( $self, $config_entry ) = @_;
    my $q_config_entry = $self->{monarch}->quote($config_entry);
    my $config_values = $self->query_database_rows( 'monarch', "select value from setup where name=$q_config_entry and type='config'" );
    die "ERROR:  Could not fetch an unambiguous Monarch configuration value for \"$config_entry\".\n" if @$config_values != 1;
    return $config_values->[0];
}

# ================================================================

sub host_exists_in_monarch {
    my ( $self, $host ) = @_;
    my $q_host = $self->{monarch}->quote($host);
    return $self->query_database( 'monarch', "select count(*) from hosts where name=$q_host" );
}

sub host_service_exists_in_monarch {
    my ( $self, $host, $service ) = @_;
    my $q_host    = $self->{monarch}->quote($host);
    my $q_service = $self->{monarch}->quote($service);
    return $self->query_database(
	'monarch', "
	select count(*) from services s, service_names sn, hosts h
	where sn.servicename_id=s.servicename_id and sn.name=$q_service and h.host_id=s.host_id and h.name=$q_host
	"
    );
}

sub hostgroup_exists_in_monarch {
    my ( $self, $hostgroup ) = @_;
    my $q_hostgroup = $self->{monarch}->quote($hostgroup);
    return $self->query_database( 'monarch', "select count(*) from hostgroups where name=$q_hostgroup" );
}

sub servicegroup_exists_in_monarch {
    my ( $self, $servicegroup ) = @_;
    my $q_servicegroup = $self->{monarch}->quote($servicegroup);
    return $self->query_database( 'monarch', "select count(*) from servicegroups where name=$q_servicegroup" );
}

sub host_exists_in_gwcollagedb {
    my ( $self, $host ) = @_;
    my $q_host = $self->{gwcollagedb}->quote($host);
    return $self->query_database( 'gwcollagedb', "select count(*) from host where hostname=$q_host" );
}

sub host_service_exists_in_gwcollagedb {
    my ( $self, $host, $service ) = @_;
    my $q_host    = $self->{gwcollagedb}->quote($host);
    my $q_service = $self->{gwcollagedb}->quote($service);
    return $self->query_database(
	'gwcollagedb', "
	select count(*) from host h, servicestatus ss
	where h.hostname=$q_host and ss.hostid=h.hostid and ss.servicedescription=$q_service
	"
    );
}

sub hostgroup_exists_in_gwcollagedb {
    my ( $self, $hostgroup ) = @_;
    my $q_hostgroup = $self->{gwcollagedb}->quote($hostgroup);
    return $self->query_database( 'gwcollagedb', "select count(*) from hostgroup where name=$q_hostgroup" );
}

sub servicegroup_exists_in_gwcollagedb {
    my ( $self, $servicegroup ) = @_;
    my $q_servicegroup = $self->{gwcollagedb}->quote($servicegroup);
    return $self->query_database(
	'gwcollagedb', "
	select count(*) from category c, entitytype et
	where et.name = 'SERVICE_GROUP' and c.entitytypeid = et.entitytypeid and c.name = $q_servicegroup
	"
    );
}

sub host_in_gwcollagedb_is_owned_by_nagios {
    my ( $self, $host ) = @_;
    my $q_host = $self->{gwcollagedb}->quote($host);
    return $self->query_database(
	'gwcollagedb', "
	select count(*) from host h, applicationtype at
	where h.hostname=$q_host and at.applicationtypeid=h.applicationtypeid and at.name='NAGIOS'
	"
    );
}

sub host_service_in_gwcollagedb_is_owned_by_nagios {
    my ( $self, $host, $service ) = @_;
    my $q_host    = $self->{gwcollagedb}->quote($host);
    my $q_service = $self->{gwcollagedb}->quote($service);
    return $self->query_database(
	'gwcollagedb', "
	select count(*) from host h, servicestatus ss, applicationtype at
	where h.hostname=$q_host and ss.hostid=h.hostid and ss.servicedescription=$q_service
	and at.applicationtypeid=ss.applicationtypeid and at.name='NAGIOS'
	"
    );
}

sub hostgroup_in_gwcollagedb_is_owned_by_nagios {
    my ( $self, $hostgroup ) = @_;
    my $q_hostgroup = $self->{gwcollagedb}->quote($hostgroup);
    return $self->query_database(
	'gwcollagedb', "
	select count(*) from hostgroup hg, applicationtype at
	where hg.name=$q_hostgroup and at.applicationtypeid=hg.applicationtypeid and at.name='NAGIOS'
	"
    );
}

sub servicegroup_in_gwcollagedb_is_owned_by_nagios {
    my ( $self, $servicegroup ) = @_;
    my $q_servicegroup = $self->{gwcollagedb}->quote($servicegroup);
    ## FIX MAJOR:  It's not clear that this question can even be sensibly asked.
    ## Ask Roger if there is any type of ApplicationType associated with a Service Group in Foundation,
    ## and how the lack of such ownership data might affect how service groups (especially empty service
    ## groups) are to be managed.  (Should the "category" table have an "applicationtypeid" field, that
    ## is not yet present in that table?)  For the time being, we simply assume that Monarch is the only
    ## actor creating service groups; whether or not that is true is as yet unknown.
    return $self->query_database(
	'gwcollagedb', "
	select count(*) from category c, entitytype et
	where et.name = 'SERVICE_GROUP' and c.entitytypeid = et.entitytypeid and c.name = $q_servicegroup
	"
    );
}

# This routine returns an arrayref pointing to a list of the indicated members.
sub members_of_hostgroup_in_monarch {
    my ( $self, $hostgroup ) = @_;
    my $q_hostgroup = $self->{monarch}->quote($hostgroup);
    return $self->query_database_rows(
	'monarch', "
	select h.name from hostgroup_host hgh, hostgroups hg, hosts h
	where hg.name=$q_hostgroup and hgh.hostgroup_id=hg.hostgroup_id
	and h.host_id=hgh.host_id
	"
    );
}

sub host_is_member_of_hostgroup_in_monarch {
    my ( $self, $host, $hostgroup ) = @_;
    my $q_host      = $self->{monarch}->quote($host);
    my $q_hostgroup = $self->{monarch}->quote($hostgroup);
    return $self->query_database(
	'monarch', "
	select count(*) from hostgroup_host hgh, hostgroups hg, hosts h
	where hg.name=$q_hostgroup and hgh.hostgroup_id=hg.hostgroup_id
	and h.host_id=hgh.host_id and h.name=$q_host
	"
    );
}

# This routine returns an arrayref pointing to a list of the indicated members.
sub members_of_hostgroup_in_gwcollagedb {
    my ( $self, $hostgroup ) = @_;
    my $q_hostgroup = $self->{gwcollagedb}->quote($hostgroup);
    return $self->query_database_rows(
	'gwcollagedb', "
	select h.hostname from hostgroupcollection hgc, hostgroup hg, host h
	where hg.name=$q_hostgroup and hgc.hostgroupid=hg.hostgroupid
	and h.hostid=hgc.hostid
	"
    );
}

sub host_is_member_of_hostgroup_in_gwcollagedb {
    my ( $self, $host, $hostgroup ) = @_;
    my $q_host      = $self->{gwcollagedb}->quote($host);
    my $q_hostgroup = $self->{gwcollagedb}->quote($hostgroup);
    return $self->query_database(
	'gwcollagedb', "
	select count(*) from hostgroupcollection hgc, hostgroup hg, host h
	where hg.name=$q_hostgroup and hgc.hostgroupid=hg.hostgroupid
	and h.hostid=hgc.hostid and h.hostname=$q_host
	"
    );
}

# This routine returns an arrayref to a set of arrayrefs, each a 2-element [host, service] array.
sub members_of_servicegroup_in_monarch {
    my ( $self, $servicegroup ) = @_;
    my $q_servicegroup          = $self->{monarch}->quote($servicegroup);
    my $host_service_name_pairs = $self->query_database_rows(
	'monarch', "
	select h.name, sn.name from servicegroup_service sgs, servicegroups sg, hosts h, services s, service_names sn
	where sg.name=$q_servicegroup
	and sgs.servicegroup_id=sg.servicegroup_id and h.host_id=sgs.host_id and s.service_id=sgs.service_id
	and h.host_id=s.host_id and sn.servicename_id=s.servicename_id
	", { Columns => [ 1, 2 ] }
    );
    my @host_services = ();
    while (@$host_service_name_pairs) {
	push @host_services, [ splice( @$host_service_name_pairs, -2, 2 ) ];
    }
    return \@host_services;
}

sub host_service_is_member_of_servicegroup_in_monarch {
    my ( $self, $host, $service, $servicegroup ) = @_;
    my $q_host         = $self->{monarch}->quote($host);
    my $q_service      = $self->{monarch}->quote($service);
    my $q_servicegroup = $self->{monarch}->quote($servicegroup);
    return $self->query_database(
	'monarch', "
	select count(*) from servicegroup_service sgs, servicegroups sg, hosts h, services s, service_names sn
	where sg.name=$q_servicegroup
	and sgs.servicegroup_id=sg.servicegroup_id and h.host_id=sgs.host_id and s.service_id=sgs.service_id
	and h.name=$q_host and h.host_id=s.host_id and sn.servicename_id=s.servicename_id and sn.name=$q_service
	"
    );
}

# This routine returns an arrayref to a set of arrayrefs, each a 2-element [host, service] array.
sub members_of_servicegroup_in_gwcollagedb {
    my ( $self, $servicegroup ) = @_;
    my $q_servicegroup          = $self->{gwcollagedb}->quote($servicegroup);
    my $host_service_name_pairs = $self->query_database_rows(
	'gwcollagedb', "
	select h.hostname, ss.ServiceDescription
	from	host h, servicestatus ss, categoryentity ce, category c, entitytype et
	where	et.name = 'SERVICE_GROUP'
	and	c.entitytypeid = et.entitytypeid
	and	c.name = $q_servicegroup
	and	ce.categoryid = c.categoryid
	and	ss.servicestatusid = ce.objectid
	and	h.hostid = ss.hostid
	", { Columns => [ 1, 2 ] }
    );
    my @host_services = ();
    while (@$host_service_name_pairs) {
	push @host_services, [ splice( @$host_service_name_pairs, -2, 2 ) ];
    }
    return \@host_services;
}

sub host_service_is_member_of_servicegroup_in_gwcollagedb {
    my ( $self, $host, $service, $servicegroup ) = @_;
    my $q_host         = $self->{gwcollagedb}->quote($host);
    my $q_service      = $self->{gwcollagedb}->quote($service);
    my $q_servicegroup = $self->{gwcollagedb}->quote($servicegroup);
    return $self->query_database(
	'gwcollagedb', "
	select count(*)
	from	host h, servicestatus ss, categoryentity ce, category c, entitytype et
	where	et.name = 'SERVICE_GROUP'
	and	c.entitytypeid = et.entitytypeid
	and	c.name = $q_servicegroup
	and	ce.categoryid = c.categoryid
	and	ss.servicestatusid = ce.objectid
	and	ss.ServiceDescription = $q_service
	and	h.hostid = ss.hostid
	and	h.hostname = $q_host
	"
    );
}

sub hostgroup_in_monarch_is_empty {
    my ( $self, $hostgroup ) = @_;
    my $q_hostgroup = $self->{monarch}->quote($hostgroup);
    return not $self->query_database(
	'monarch', "
	select count(*) from hostgroup_host hgh, hostgroups hg
	where hg.name=$q_hostgroup and hgh.hostgroup_id=hg.hostgroup_id
	"
    );
}

sub hostgroup_in_gwcollagedb_is_empty {
    my ( $self, $hostgroup ) = @_;
    my $q_hostgroup = $self->{gwcollagedb}->quote($hostgroup);
    return not $self->query_database(
	'gwcollagedb', "
	select count(*) from hostgroupcollection hgc, hostgroup hg
	where hg.name=$q_hostgroup and hgc.hostgroupid=hg.hostgroupid
	"
    );
}

sub servicegroup_in_monarch_is_empty {
    my ( $self, $servicegroup ) = @_;
    my $q_servicegroup = $self->{monarch}->quote($servicegroup);
    return not $self->query_database(
	'monarch', "
	select count(*) from servicegroup_service sgs, servicegroups sg
	where sg.name=$q_servicegroup and sgs.servicegroup_id=sg.servicegroup_id
	"
    );
}

sub servicegroup_in_gwcollagedb_is_empty {
    my ( $self, $servicegroup ) = @_;
    my $q_servicegroup = $self->{gwcollagedb}->quote($servicegroup);
    ## I'm not sure if we need to join to servicestatus to make this work,
    ## but it shouldn't hurt except possibly for a little performance drag.
    return not $self->query_database(
	'gwcollagedb', "
	select count(*)
	from	servicestatus ss, categoryentity ce, category c, entitytype et
	where	et.name = 'SERVICE_GROUP'
	and	c.entitytypeid = et.entitytypeid
	and	c.name = $q_servicegroup
	and	ce.categoryid = c.categoryid
	and	ss.servicestatusid = ce.objectid
	"
    );
}

# The following set of routines analyzes contents of a %$delta hash produced by auditing (analyzing)
# the differences between the "monarch" database and the "gwcollagedb" database.  In so doing, we
# attempt to minimize any auto-vivification effects, at least insofar as any dynamically-specified
# keys goes (that is, nodes of the hash tree that are attributable to specific named objects like
# hosts or services).  The idea is to leave the hash in sufficiently undamaged shape that it could
# still be used for the remainder of a Foundation Sync operation.

sub host_will_be_deleted {
    my ( $self, $delta, $host ) = @_;
    return (  exists $delta->{'delete'}
	  and exists $delta->{'delete'}{'host'}
	  and exists $delta->{'delete'}{'host'}{$host} );
}

sub host_service_will_be_direct_deleted {
    my ( $self, $delta, $host, $service ) = @_;
    return (  exists $delta->{'delete'}
	  and exists $delta->{'delete'}{'service'}
	  and exists $delta->{'delete'}{'service'}{$host}
	  and exists $delta->{'delete'}{'service'}{$host}{$service} );
}

sub host_service_will_be_cascade_deleted {
    my ( $self, $delta, $host, $service ) = @_;
    return ( $self->host_will_be_deleted( $delta, $host )
	  and not $self->host_service_will_be_direct_deleted( $delta, $host, $service ) );
}

sub host_service_will_be_deleted {
    my ( $self, $delta, $host, $service ) = @_;
    return (
	     $self->host_service_will_be_direct_deleted( $delta, $host, $service )
	  or $self->host_service_will_be_cascade_deleted( $delta, $host, $service )
    );
}

sub hostgroup_will_be_deleted {
    my ( $self, $delta, $hostgroup ) = @_;
    return (  exists $delta->{'delete'}
	  and exists $delta->{'delete'}{'hostgroup'}
	  and exists $delta->{'delete'}{'hostgroup'}{$hostgroup} );
}

sub servicegroup_will_be_deleted {
    my ( $self, $delta, $servicegroup ) = @_;
    return (  exists $delta->{'delete'}
	  and exists $delta->{'delete'}{'servicegroup'}
	  and exists $delta->{'delete'}{'servicegroup'}{$servicegroup} );
}

sub host_will_be_added {
    my ( $self, $delta, $host ) = @_;
    return (  exists $delta->{'add'}
	  and exists $delta->{'add'}{'host'}
	  and exists $delta->{'add'}{'host'}{$host} );
}

sub host_service_will_be_added {
    my ( $self, $delta, $host, $service ) = @_;
    return (  exists $delta->{'add'}
	  and exists $delta->{'add'}{'service'}
	  and exists $delta->{'add'}{'service'}{$host}
	  and exists $delta->{'add'}{'service'}{$host}{$service} );
}

sub hostgroup_will_be_added {
    my ( $self, $delta, $hostgroup ) = @_;
    return (  exists $delta->{'add'}
	  and exists $delta->{'add'}{'hostgroup'}
	  and exists $delta->{'add'}{'hostgroup'}{$hostgroup} );
}

sub servicegroup_will_be_added {
    my ( $self, $delta, $servicegroup ) = @_;
    return (  exists $delta->{'add'}
	  and exists $delta->{'add'}{'servicegroup'}
	  and exists $delta->{'add'}{'servicegroup'}{$servicegroup} );
}

# FIX MAJOR:  compare cascade_deletion of host services when hosts are deleted
# We include the effects of a possible cascade-delete if the hostgroup itself is being deleted,
# although the membership will not be explicitly listed in the delta in that case.
sub host_will_be_direct_deleted_from_hostgroup {
    my ( $self, $delta, $host, $hostgroup ) = @_;
    ## FIX MAJOR:  Why is this testing for membership in monarch?  Shouldn't it be checking in
    ## gwcollagedb instead?  For that matter, why do we need to check at all here?
    ## $self->host_is_member_of_hostgroup_in_monarch( $host, $hostgroup )
    return (  $self->host_is_member_of_hostgroup_in_gwcollagedb( $host, $hostgroup )
	  and exists $delta->{'alter'}
	  and exists $delta->{'alter'}{'hostgroup'}
	  and exists $delta->{'alter'}{'hostgroup'}{$hostgroup}
	  and exists $delta->{'alter'}{'hostgroup'}{$hostgroup}{'members'}
	  and not exists $delta->{'alter'}{'hostgroup'}{$hostgroup}{'members'}{$host} );
}

# FIX MAJOR:  compare cascade_deletion of host services when hosts are deleted
sub host_will_be_cascade_deleted_from_hostgroup {
    my ( $self, $delta, $host, $hostgroup ) = @_;
    ## FIX MAJOR:  Why is this testing for membership in monarch?  Shouldn't it be checking in
    ## gwcollagedb instead?  For that matter, why do we need to check at all here?
    ## $self->host_is_member_of_hostgroup_in_monarch( $host, $hostgroup )
    return (  $self->host_is_member_of_hostgroup_in_gwcollagedb( $host, $hostgroup )
	  and exists $delta->{'delete'}
	  and exists $delta->{'delete'}{'hostgroup'}
	  and exists $delta->{'delete'}{'hostgroup'}{$hostgroup} );
}

sub host_will_be_deleted_from_hostgroup {
    my ( $self, $delta, $host, $hostgroup ) = @_;
    return (
	     $self->host_will_be_direct_deleted_from_hostgroup( $delta, $host, $hostgroup )
	  or $self->host_will_be_cascade_deleted_from_hostgroup( $delta, $host, $hostgroup )
    );
}

# FIX MAJOR:  compare cascade_deletion of host services when hosts are deleted
# We include the effects of a possible cascade-delete if the servicegroup itself is being deleted,
# although the membership will not be explicitly listed in the delta in that case.
sub host_service_will_be_direct_deleted_from_servicegroup {
    my ( $self, $delta, $host, $service, $servicegroup ) = @_;
    ## FIX MAJOR:  Why is this testing for membership in monarch?  Shouldn't it be checking in
    ## gwcollagedb instead?  For that matter, why do we need to check at all here?
    ## $self->host_service_is_member_of_servicegroup_in_monarch( $host, $service, $servicegroup )
    return (
	      $self->host_service_is_member_of_servicegroup_in_gwcollagedb( $host, $service, $servicegroup )
	  and exists $delta->{'alter'}
	  and exists $delta->{'alter'}{'servicegroup'}
	  and exists $delta->{'alter'}{'servicegroup'}{$servicegroup}
	  and exists $delta->{'alter'}{'servicegroup'}{$servicegroup}{'members'}
	  and (not exists $delta->{'alter'}{'servicegroup'}{$servicegroup}{'members'}{$host}
	    or not exists $delta->{'alter'}{'servicegroup'}{$servicegroup}{'members'}{$host}{$service} )
    );
}

# FIX MAJOR:  compare cascade_deletion of host services when hosts are deleted
sub host_service_will_be_cascade_deleted_from_servicegroup {
    my ( $self, $delta, $host, $service, $servicegroup ) = @_;
    ## FIX MAJOR:  Why is this testing for membership in monarch?  Shouldn't it be checking in
    ## gwcollagedb instead?  For that matter, why do we need to check at all here?
    ## $self->host_service_is_member_of_servicegroup_in_monarch( $host, $service, $servicegroup )
    return (  $self->host_service_is_member_of_servicegroup_in_gwcollagedb( $host, $service, $servicegroup )
	  and exists $delta->{'delete'}
	  and exists $delta->{'delete'}{'servicegroup'}
	  and exists $delta->{'delete'}{'servicegroup'}{$servicegroup} );
}

sub host_service_will_be_deleted_from_servicegroup {
    my ( $self, $delta, $host, $service, $servicegroup ) = @_;
    return (
	     $self->host_service_will_be_direct_deleted_from_servicegroup( $delta, $host, $service, $servicegroup )
	  or $self->host_service_will_be_cascade_deleted_from_servicegroup( $delta, $host, $service, $servicegroup )
    );
}

sub hosts_that_will_be_added_to_hostgroup {
    my ( $self, $delta, $hostgroup ) = @_;
    my @hosts = ();

    if (    exists $delta->{'add'}
	and exists $delta->{'add'}{'hostgroup'}
	and exists $delta->{'add'}{'hostgroup'}{$hostgroup}
	and exists $delta->{'add'}{'hostgroup'}{$hostgroup}{'members'} )
    {
	@hosts = keys %{ $delta->{'add'}{'hostgroup'}{$hostgroup}{'members'} };
    }

    return \@hosts;
}

sub host_will_be_added_to_new_hostgroup {
    my ( $self, $delta, $host, $hostgroup ) = @_;
    return (  exists $delta->{'add'}
	  and exists $delta->{'add'}{'hostgroup'}
	  and exists $delta->{'add'}{'hostgroup'}{$hostgroup}
	  and exists $delta->{'add'}{'hostgroup'}{$hostgroup}{'members'}
	  and exists $delta->{'add'}{'hostgroup'}{$hostgroup}{'members'}{$host} );
}

sub host_will_be_added_to_existing_hostgroup {
    my ( $self, $delta, $host, $hostgroup ) = @_;
    return (  exists $delta->{'alter'}
	  and exists $delta->{'alter'}{'hostgroup'}
	  and exists $delta->{'alter'}{'hostgroup'}{$hostgroup}
	  and exists $delta->{'alter'}{'hostgroup'}{$hostgroup}{'members'}
	  and exists $delta->{'alter'}{'hostgroup'}{$hostgroup}{'members'}{$host} );
}

sub host_will_be_added_to_hostgroup {
    my ( $self, $delta, $host, $hostgroup ) = @_;
    return (
	     $self->host_will_be_added_to_new_hostgroup( $delta, $host, $hostgroup )
	  or $self->host_will_be_added_to_existing_hostgroup( $delta, $host, $hostgroup )
    );
}

sub host_services_that_will_be_added_to_servicegroup {
    my ( $self, $delta, $servicegroup ) = @_;
    my @host_services = ();

    if (    exists $delta->{'add'}
	and exists $delta->{'add'}{'servicegroup'}
	and exists $delta->{'add'}{'servicegroup'}{$servicegroup}
	and exists $delta->{'add'}{'servicegroup'}{$servicegroup}{'members'} )
    {
	foreach my $host ( keys %{ $delta->{'add'}{'servicegroup'}{$servicegroup}{'members'} } ) {
	    foreach my $service ( keys %{ $delta->{'add'}{'servicegroup'}{$servicegroup}{'members'}{$host} } ) {
		push @host_services, [ $host, $service ];
	    }
	}
    }

    return \@host_services;
}

sub host_service_will_be_added_to_new_servicegroup {
    my ( $self, $delta, $host, $service, $servicegroup ) = @_;
    return (  exists $delta->{'add'}
	  and exists $delta->{'add'}{'servicegroup'}
	  and exists $delta->{'add'}{'servicegroup'}{$servicegroup}
	  and exists $delta->{'add'}{'servicegroup'}{$servicegroup}{'members'}
	  and exists $delta->{'add'}{'servicegroup'}{$servicegroup}{'members'}{$host}
	  and exists $delta->{'add'}{'servicegroup'}{$servicegroup}{'members'}{$host}{$service} );
}

sub host_service_will_be_added_to_existing_servicegroup {
    my ( $self, $delta, $host, $service, $servicegroup ) = @_;
    return (  exists $delta->{'alter'}
	  and exists $delta->{'alter'}{'servicegroup'}
	  and exists $delta->{'alter'}{'servicegroup'}{$servicegroup}
	  and exists $delta->{'alter'}{'servicegroup'}{$servicegroup}{'members'}
	  and exists $delta->{'alter'}{'servicegroup'}{$servicegroup}{'members'}{$host}
	  and exists $delta->{'alter'}{'servicegroup'}{$servicegroup}{'members'}{$host}{$service} );
}

sub host_service_will_be_added_to_servicegroup {
    my ( $self, $delta, $host, $service, $servicegroup ) = @_;
    return (
	     $self->host_service_will_be_added_to_new_servicegroup( $delta, $host, $service, $servicegroup )
	  or $self->host_service_will_be_added_to_existing_servicegroup( $delta, $host, $service, $servicegroup )
    );
}

1;

__END__

=head1 NAME

GW::Test - Test Management for GroundWork Components

=head1 SYNOPSIS

To be provided.

=head1 VERSION

0.0.2

=head1 DESCRIPTION

Standard CPAN Perl modules such as those in the Test::* families provide
means for collecting and reporting test results, but do nothing for you
in the area of application-specific test support.  Such support may include
various forms of common initialization required before running groups
of tests, normalizing test-result files into a format which suppresses
expected variations, and executing complex data-stream or database-content
comparisons.  Typical examples would be:

=over 4

=item *
Manage database-access credentials.

=item *
Loading the monarch database with a known configuration, as represented
by a database dump file.

=item *
Normalizing nagios/etc config files into a canonical sorted order, so
objects appear in the order of their names and directives within object
definitions appear in alphabetic order.

=item *
Comparing two normalized data streams (e.g., extracted from log files).

=item *
Dumping a result database.

=item *
Comparing two database dump files.

=item *
Supporting database queries, if it would easier to check for specific
test results by that means rather than by some sophisticated dump-file
comparisons.

=item *
Provide timing facilities, to measure performance characteristics of
application actions and phases.

=item *
Record test statistics, so the evolution of test and performance results
can be examined as development proceeds over time.

=item *
Managing the execution of hierarchies of related tests, recognizing
setup dependencies, minimizing duplicate setup, and sequencing the tests.

=item *
Selecting subsets of tests to run, based on functional or logical
classification or based on test names, to allow focusing on particular
areas currently under development.

=item *
Classify test actions as startup, checking preconditions, setup,
validation of setup, execution, checking postconditions, teardown,
and shutdown, for purposes of reporting.

=item *
Possibly, the ability to dynamically create, manipulate, and destroy
test objects in databases.

=back

GW::Test is designed to provide such capabilities, to simplify writing
and executing test scripts.  The idea is that most tests should be
table-driven, by specifying for each test:

=over 4
=item *
a set of preconditions, and how to check them
=item *
steps to set up the test context
=item *
a set of setup validation checks
=item *
steps to execute the test
=item *
a set of postconditions, and how to check them
=item *
test timing requirements
=item *
where and how test results should be permanently recorded
=item *
any test-execution sequencing dependencies
=item *
functional classification of tests as unit tests, integration tests, or system tests
=item *
logical classifications and groupings for tests
=back

=head1 CONSTRUCTOR

=head1 REFERENCES

"Test Driven Development With Perl:  A Stonehenge Consulting Course"
http://cdn.oreillystatic.com/en/assets/1/event/12/Practical%20Test-driven%20Development%20Presentation.pdf

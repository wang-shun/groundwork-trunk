package Replication::State;

# Handle state data in a GroundWork Monitor Disaster Recovery deployment.
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
    &fetch_local_state
    &store_local_state
    &fetch_remote_state
    &store_remote_state
    &validate_replication_state
    &load_default_replication_state
    &load_replication_state
    &save_replication_state
    &print_replication_state
);

our @EXPORT_OK = qw(
    &log_replication_state
);

# This is where we'll pick up any Perl packages not in the standard Perl
# distribution, to make this a self-contained package anchored in a single
# directory.
use FindBin qw($Bin);
use lib "$Bin/perl/lib";

use IO::Handle;
use POSIX;
use YAML::XS qw(LoadFile);
use boolean;
use Replication::Logger;

# Be sure to update this as changes are made to this module!
my $VERSION = '0.1.0';

# ================================================================
# Working variables.
# ================================================================

my $state_file = undef;

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

my $state = undef;

# ================================================================
# Supporting subroutines.
# ================================================================

# The new() constructor must be invoked as:
#     my $state = Replication::State->new ($state_file);
# because if it is invoked instead as:
#     my $state = Replication::State::new ($state_file);
# no invocant is supplied as the implicit first argument.

sub new {
    my $invocant = $_[0];   # implicit argument
    $state_file  = $_[1];   # required argument

    $state_file = "$Bin/$state_file" if $state_file !~ m{^/};

    my $class = ref($invocant) || $invocant;    # object or class name
    # Options are stored in our object hash to prepare for the day when
    # we allow more than one such object in the program.  These copies
    # are not yet referenced later on, though.
    my $self = {
	state_file => $state_file
    };
    bless $self, $class;
    return $self;
}

# Returns a hashref containing the local state.
sub fetch_local_state {
    return $state->{local} || {};
}

# Pass in a hashref containing the local state.
sub store_local_state {
    $state->{local} = shift;
}

# Returns a hashref containing the remote state.
sub fetch_remote_state {
    return $state->{remote} || {};
}

# Pass in a hashref containing the remote state.
sub store_remote_state {
    $state->{remote} = shift;
}

# Run as many validation checks as we can think of to ensure that the given state is
# both complete and consistent.  We cannot catch all errors here; we're mainly interested
# in verifying that certain fields not present in an older version of the software are
# present in the given state.  What we're trying to detect here is picking up the content
# of an old state file and not realizing that it doesn't contain what we now need, either
# because of newer software development or because an on-site upgrade has occurred and
# the old state file is still in play.

# This is generally supposed to be an internal routine, not to be called by external code.
# Perhaps that will change over time.
sub validate_replication_substate {
    my $dubious_state = $_[1] || $state;
    my $substate      = $_[2];	# required; 'local' or 'remote' to indicate that $dubious_state

    # Validation steps:
    # (1) Ensure we have all the keys we expect, and no others.
    # (2) Someday, extend this to ensure that the datatypes and values are as we expect.

    my $default_state = create_default_replication_state (true);
    my $default_substate = $default_state->{$substate};

    my $status = 1;
    foreach my $key (keys %$dubious_state) {
        if (not exists $default_substate->{$key}) {
	    log_timed_message "ERROR:  extra key \"$key\" is present in $substate replication state";
	    $status = 0;
	}
    }
    foreach my $key (keys %$default_substate) {
        if (not exists $dubious_state->{$key}) {
	    log_timed_message "ERROR:  key \"$key\" is missing from $substate replication state";
	    $status = 0;
	}
    }

    return $status;
}

sub validate_replication_state {
    my $dubious_state = $_[1] || $state;
    my $substate      = $_[2];	# optional; 'local' or 'remote' to indicate that $dubious_state
				# is really one of those partial states and not the full $state

    if (defined($substate) && ($substate eq 'local' || $substate eq 'remote')) {
        return validate_replication_substate ('', $dubious_state, $substate);
    }
    foreach my $system (keys %$dubious_state) {
        if ($system ne 'local' && $system ne 'remote') {
	    log_timed_message "FATAL:  replication state includes an unknown \"$system\" system";
	    return 0;
	}
    }
    return
	validate_replication_substate ('', $dubious_state->{'local'},  'local') &&
	validate_replication_substate ('', $dubious_state->{'remote'}, 'remote');
}

# This is an internal routine, not intended to be called from an external program.
sub create_default_replication_state {
    my $i_am_primary = shift;

    # FIX THIS
    log_timed_message "ERROR:  (FIX THIS) no default replication state is being initialized yet in the Replication::State code";

    # what belongs here:
    # has_notification_authority
    # has_master_configuration_authority
    # replication_is_enabled
    # replication_operations_are_active
    # XXX_is_quiesced_for_source_replication
    # XXX_is_quiesced_for_sink_replication
    # XXX_is_out_of_sync
    my %default_state = ();

    # Note:
    # A false value here serializes as:  !!perl/scalar:boolean 0
    # A true  value here serializes as:  !!perl/scalar:boolean 1
    # Hopefully the type tags here, while they appear to be language-specific,
    # will be properly understood by other language interpreters upon import.
    # Too bad the values cannot be specially handled by the YAML dumper or
    # emitter and serialized instead as simple and obvious yes and no values.

    # FIX THIS:  is there some other way to get the dump to tag such values
    # with !!bool so they do in fact show up that way?

    # These elements should be set here, via code.
    $default_state{local}{state_time}                         = time();
    $default_state{local}{monitoring_is_up}                   = false;
    $default_state{local}{notification_authority_control}     = 'dynamic';  # or 'grabbed' or 'released'
    $default_state{local}{has_notification_authority}         = $i_am_primary ? true : false;
    $default_state{local}{has_master_configuration_authority} = $i_am_primary ? true : false;
    $default_state{local}{in_failure_mode}                    = false;
    $default_state{local}{replication_is_enabled}             = false;
    $default_state{local}{replication_operations_are_active}  = false;
    $default_state{local}{last_remote_up_state_times}         = [];  # info about the remote system

    # FIX THIS:  It would be better to pick up the remaining details (or the
    # basis for creating such details) from a read-only default_replication.conf
    # file instead of hardcoding a bunch of applications and databases here.
    # So to the extent that we do so for now, that's mostly for initial testing.
    # But possibly, we might not make that extension until we productize this.

    # These elements should be set from a default configuration file.
    $default_state{local}{app}{'groundwork-monitor'}{blocked} = false;
    $default_state{local}{app}{'snmp-trap-handling'}{blocked} = false;
    $default_state{local}{app}{'syslog-ng'}         {blocked} = false;
    $default_state{local}{app}{'monarch'}           {blocked} = false;
    $default_state{local}{app}{'auto-discovery'}    {blocked} = false;
    $default_state{local}{app}{'nagios'}            {blocked} = false;
    $default_state{local}{app}{'event-console'}     {blocked} = false;
    $default_state{local}{app}{'status-viewer'}     {blocked} = false;
    $default_state{local}{app}{'reports'}           {blocked} = false;
    $default_state{local}{app}{'cacti'}             {blocked} = false;
    $default_state{local}{app}{'nedi'}              {blocked} = false;
    $default_state{local}{app}{'weathermap'}        {blocked} = false;
    $default_state{local}{app}{'ntop'}              {blocked} = false;
    $default_state{local}{db}{'monarch'}            {blocked} = false;
    $default_state{local}{db}{'GWCollageDB'}        {blocked} = false;
    $default_state{local}{db}{'jbossportal'}        {blocked} = false;
    $default_state{local}{db}{'cacti'}              {blocked} = false;
    $default_state{local}{db}{'nedi'}               {blocked} = false;

    return \%default_state;
}

# FIX MAJOR:  if we load this default state, we probably need to synchronize
# with Foundation or somesuch so its notion of Master Configuration Authority
# is properly synchronized with our setting of that state here, and so that
# state can be properly reflected to the GW Monitor UI
sub load_default_replication_state {
    my $i_am_primary = shift;

    $state = create_default_replication_state ($i_am_primary);

    return save_replication_state($state_file, $state);
}

sub load_replication_state {
    my $i_am_primary = shift;
    my $start_time   = shift;
    eval {
	$state = YAML::XS::LoadFile($state_file);
    };
    if ($@) {
	if ($! == ENOENT) {
	    log_timed_message "WARNING:  replication state file $state_file"
	      . " does not exist; loading default replication state instead";
	    return 0 if !load_default_replication_state($i_am_primary);
	}
	else {
	    chomp $@;
	    log_timed_message 'ERROR:  ', $@;
	    return 0;
	}
    }

    $state->{local}{start_time} = $start_time;
    return 1;
}

# FIX THIS:  distinguish between being called as an exported function
# and being called as a class method (the latter may give us an extra
# first argument we will need to recognize and ignore)
# FIX THIS:  Revamp this routine and all call chains to return error
# messages to the caller, so they can percolate back up to the end user
# rather than just being buried in the logfile.  Also validate that the
# call chains are all using proper error detection and reporting.
sub save_replication_state {
    my $saved_state_file = $_[0] || $state_file;
    my $saved_state      = $_[1] || $state;

    my $temporary_state_file = $saved_state_file . '.tmp';
    eval {
	YAML::XS::DumpFile( $temporary_state_file, $saved_state );
    };
    if ($@) {
	chomp $@;
        log_timed_message "ERROR:  $@";
	return 0;
    }
    else {
	# Only now that the state file is completely written is it appropriate
	# to slide it into place where other applications can read it.
	if (!rename( $temporary_state_file, $saved_state_file )) {
	    log_timed_message "ERROR:  $!";
	    return 0;
	}
    }
    return 1;
}

# FIX THIS:  Fill in appropriate stuff to serialize the complete state in a human-readable format,
# outputting an arrayref pointing to a series of lines to be printed on the user terminal.
sub print_replication_state {
    my @lines = ();
    push @lines, 'FIX THIS:  display the entire replication state here';
    return \@lines;
}

# Internal routine for debugging; not expected to be for general use.
sub log_replication_state {
    foreach my $location (sort keys %$state) {
	log_message "$location state:";
	foreach my $key (sort keys %{ $state->{$location} }) {
	    log_message "  $key => $state->{$location}{$key}";
	}
    }
}

1;

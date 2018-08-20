package Replication::Utility;

# Utility functions for a GroundWork Monitor Disaster Recovery deployment.
# Copyright (c) 2010 GroundWork Open Source (www.groundworkopensource.com).
# All rights reserved.  Use is subject to GroundWork commercial license terms.

# ================================================================
# Documentation.
# ================================================================

# This package contains miscellaneous setup and debug routines
# that haven't yet found a home in a capability-targeted package.
# As the software evolves, if some collection of functions here
# grows to the point that we recognize commonality, those functions
# may be split off into their own targeted package (e.g., perhaps a
# Replication::Security package).

# To do:
# * move time_text() from Replication::Foundation to here
# * create a file_timestamp() routine to make timestamp strings
#   suitable for use in filenames (such as backup directories)

# ================================================================
# Perl setup.
# ================================================================

use strict;
use warnings;

require Exporter;
our @ISA = ('Exporter');

our @EXPORT = qw(
    &run_as_nagios
);

our @EXPORT_OK = qw(
    &printstack
);

# This is where we'll pick up any Perl packages not in the standard Perl
# distribution, to make this a self-contained package anchored in a single
# directory.
use FindBin qw($Bin);
use lib "$Bin/perl/lib";

# Be sure to update this as changes are made to this module!
my $VERSION = '0.1.0';

# ================================================================
# Working variables.
# ================================================================

# ================================================================
# Global configuration variables.
# ================================================================

# ================================================================
# Global working variables.
# ================================================================

# ================================================================
# Supporting subroutines.
# ================================================================

# If we cannot find the target user/group, that condition is treated as a hard failure,
# and we just die, because something is seriously wrong with the platform on which we're
# running.  But if we simply fail to change effective uid/gid values, that is treated as
# a soft failure, and we return the status back to the caller.  This allows the calling
# program to determine what to do then.  Since the purpose of this routine is mostly to
# prevent the superuser from executing a program with full system privileges, rather than
# to prevent a non-nagios developer from running a private copy for testing, this seems
# like a reasonable design.

sub run_as_nagios {
    my $nagios_user  = 'nagios';
    my $nagios_group = 'nagios';
    my $nagios_uid = getpwnam $nagios_user;
    my $nagios_gid = getgrnam $nagios_group;

    if (!defined($nagios_uid) || !defined($nagios_gid)) {
	# We don't try to log this failure both because at the time this failure occurs
	# early in a program, the logging facility probably won't be operational yet,
	# and because we don't want to wait until it is operational to call this routine
	# (thus risking creation of the logfile under the wrong user/group).
	die "FATAL:  Cannot determine the \"$nagios_user\" UID or the \"$nagios_group\" GID; aborting!\n";
    }

    # We attempt to set the effective GID first, while we perhaps still have privileges to do so.
    # "<" [balanced angle bracket]
    # "(" [balanced parenthesis]
    $) = "$nagios_gid $nagios_gid";
    $> =  $nagios_uid;

    # Then set the real uid/gid values, to avoid having any child processes inherit the previous
    # real uid/gid as their effective uid/gid (which is what the POE-1.287 POE::Wheel::Run will
    # do, rather unexpectedly).
    $( = $) + 0;  # set real to effective gid
    $< = $>;      # set real to effective uid

    # Return overall success or failure to run as the desired user and group, including both
    # real and effective values.  The calling program is free to interpret this as needed and
    # stop executing if we soft-failed here.
    return ($< == $nagios_uid) && ( ( $( + 0 ) == $nagios_gid)
	&& ($> == $nagios_uid) && ( ( $) + 0 ) == $nagios_gid);
}

# For initial debugging only.
sub printstack {
    my $i = 0; 
    while (my ($package, $filename, $line, $subroutine) = caller($i++)) {
	print STDERR "${package}, ${filename} line $line (${subroutine})\n";
    }
}

1;

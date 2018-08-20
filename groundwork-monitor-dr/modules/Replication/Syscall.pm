package Replication::Syscall;

# Syscall functions for a GroundWork Monitor Disaster Recovery deployment.
# Copyright (c) 2010 GroundWork Open Source (www.groundworkopensource.com).
# All rights reserved.  Use is subject to GroundWork commercial license terms.

# ================================================================
# Documentation.
# ================================================================

# This package contains routines that encapsulate syscalls needed by
# our Replication software.  It's better to keep them in a separate
# package because we may need to replace it with a compiled package
# to provide complete portability to all of our supported platforms.

# To do:
# * ...

# ================================================================
# Perl setup.
# ================================================================

use strict;
use warnings;

require Exporter;
our @ISA = ('Exporter');

our @EXPORT = qw(
    &getsid
    &waitid_pid_nowait
);

our @EXPORT_OK = qw(
);

{
    # See http://lists.debian.org/debian-perl/2002/11/msg00014.html and
    # perldoc perllexwarn(1) for what's going on here.  But some of the
    # advice in perllexwarn (such as about using "BEGIN { $^W = 0 }" and
    # its scope) appears to be wrong.
    local ($^W) = 0;
    # We need these for the syscalls we make below.
    require 'syscall.ph';
    require 'wait.ph';
}

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

# FIX THIS:  /usr/local/groundwork/perl/bin/perl will probably choke on this
# in the GroundWork Monitor 6.1.X releases, because we're not currently shipping
# any *.ph header files in our Perl distribution (GWMON-8508).  So we must use
# /usr/bin/perl for the time being, until we address that either by including
# such headers in the GroundWork Perl distribution, or by reworking these
# routines into a compiled Perl package.

sub getsid {
    # require 'syscall.ph';
    my $pid = 0 + shift;
    # pid_t getsid(pid_t pid);
    return syscall (&SYS_getsid, $pid);
}

# We implement here only a restricted subset of the UNIX-level waitid() function,
# partly because we don't need its full functionality, and partly because it's
# not clear how we would portably pass back the parts of the returned siginfo_t
# that might be interesting to the caller in other circumstances.
#
# We might call this either waitid_pid_nowait() [after its implementation] or
# perhaps waitpid_nowait() [for the essential function it provides].
#
# Our routine will take only one parameter, the PID of the child process whose alive-or-dead status we wish to probe.
# The return value will be:
# (1) -1 upon error (see $! for details; ECHILD means the process doesn't exist as our child)
# (2) 0 if the child process is still alive and kicking
# (3) $pid if the child process is now a zombie

sub waitid_pid_nowait {
    # require 'syscall.ph';
    # require 'wait.ph';
    my $SI_MAX_SIZE = 256;        # see __SI_MAX_SIZE in /usr/include/bits/siginfo.h on Linux, SI_MAXSZ in /usr/include/sys/siginfo.h on Solaris
    my $idtype      = &P_PID;     # Wait for only a specific process.
    my $pid         = 0 + shift;  # This one, the one passed in as an argument.
    my $infop       = "\0" x $SI_MAX_SIZE;  # zero out the entire structure, but most notably the si_pid element (see the Linux waitid(2) man page)
    my $options     = &WEXITED | &WNOHANG | &WNOWAIT;  # see waitid(2) for interpretations

    # int waitid(idtype_t idtype, id_t id, siginfo_t *infop, int options);
    my $result = syscall (&SYS_waitid, $idtype, $pid, $infop, $options);
    if ($result < 0) {
        # waitid() failed; caller will need to look at $! to figure out why
	return -1;
    }

    # FIX LATER:  Make this code construction conditional on Linux or Solaris,
    # and disallow its use on other (untested) platforms.
    #
    # Portably fetching the si_pid field is tricky.  See the header files on various platforms
    # to see how complex a siginfo_t is.  The part of the field structure we care about is:
    #
    #   // Linux:
    #   typedef struct siginfo {
    #       int si_signo;
    #       int si_errno;
    #       int si_code;
    #       union {
    #           int _pad[__SI_PAD_SIZE];
    #           struct {
    #               __pid_t si_pid;
    #               __uid_t si_uid;
    #           } _kill;
    #       } _sifields;
    #   } siginfo_t;
    #   #define si_pid _sifields._kill.si_pid
    #   
    #   // Solaris:
    #   typedef struct siginfo {
    #       int si_signo;
    #       int si_code;
    #       int si_errno;
    #   #ifdef _LP64
    #       int si_pad;         /* _LP64 union starts on an 8-byte boundary */
    #   #endif
    #       union {
    #           int __pad[SI_PAD];
    #           struct {
    #               pid_t   __pid;
    #               union {...} __pdata;
    #               ctid_t  __ctid;
    #               zoneid_t __zoneid;
    #           } __proc;
    #           struct  __fault;
    #           struct  __file;
    #           struct  __prof;
    #           struct  __rctl;
    #       } __data;
    #   } siginfo_t;
    #   #define si_pid __data.__proc.__pid
    #
    # A bit of experimentation with the C compiler shows that on both of these platforms,
    # the offset of si_pid is 12 bytes in a 32-bit program, 16 bytes in a 64-bit program.
    # We let Perl figure this out, by using "L!" in a strategic place during the unpack.
    my ($si_xxx0, $si_xxx1, $si_xxx2_plus_possible_pad, $si_pid) = unpack "iiL!i", $infop;
    if ($si_pid) {
	# Process ID of the waited-for child is non-zero; therefore the child has exited (i.e., is zombified, and has not yet
	# been reaped) and its exit status can be collected.  But note that in our simplified implementation with WNOWAIT forced,
	# we don't set $? for now.  The caller can pick up the child status later when an actual waitpid() is called.
	return $si_pid;
    }
    else {
	# Process ID field is zero; therefore the child still exists (is still running, and is not yet waitable).
	return 0;
    }
}

1;

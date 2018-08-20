package GW::Daemon;

# Daemon functions for a GroundWork add-on extension deployment.
# Copyright (c) 2011 GroundWork Open Source (www.groundworkopensource.com).
# All rights reserved.  Use is subject to GroundWork commercial license terms.

# ================================================================
# Documentation.
# ================================================================

# This package contains routines related to running a program as a
# daemon process.  We are using it in preference to some existing
# CPAN package such as one of the following:
#
#     App::Daemon
#     App::Framework::Daemon
#     Daemon::Daemonize
#     Daemon::Easy
#     Daemon::Generic
#     Daemon::Simple
#     Daemonise
#     MooseX::Daemonize
#     Object::PerlDesignPatterns
#     POE::Component::Daemon
#     POE::Component::GCS::Server::Cfg
#     POE::Component::Server::Inet
#     POEIKC
#     POEIKC::Daemon
#     POEIKC::Daemon::AndClient
#     PTools::Proc::Daemonize
#     Proc::Application::Daemon
#     Proc::Daemon
#     Proc::DaemonLite
#     Proc::Daemontools
#     Proc::Fork
#     Proc::Forking
#     Proc::Launcher
#     Proc::Watchdog
#     RunApp::Control::AppControl
#     Working::Daemon
#
# mostly because I'm already familiar with what my own routine does
# and I trust it, but also because we might need to accommodate
# certain side problems with some of the other Perl packages we are
# including in some of our GroundWork add-on extensions.

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
    &make_daemon
);

our @EXPORT_OK = qw(
);

# use Errno qw(EAGAIN);
use POSIX;  # also supplies a definition for EAGAIN, along with other stuff we need

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

sub do_fork {
    my $retries = 5;
    while (--$retries >= 0) {
	my $pid;
	if ($pid = fork) {
	    # successful fork; we're in the parent
	    return $pid;
	} elsif (defined $pid) {
	    # successful fork; we're in the child
	    return 0;
	} elsif ($! == EAGAIN) {
	    # unsuccessful but supposedly recoverable fork error; wait, then loop around and try again
	    sleep 5;
	} else {
	    # weird fork error; possibly we might want to complain to syslog before dying
	    die "Cannot fork: $!\n";
	}
    }
}

# For details on what is done here to make a daemon process, and why, see:
# * "How to Write a UNIX Daemon":  http://cjh.polyplex.org/software/daemon.pdf
#   (this old USENIX article is the original bible on this topic)
# * Proc::Daemon from CPAN
# We prefer to write our own version so we know exactly what it is doing,
# and can tweak it a bit, but the same principles are being followed.
sub make_daemon {
    # Make ourself immune to background job control write checks.
    $SIG{TTOU} = 'IGNORE';
    $SIG{TTIN} = 'IGNORE';
    $SIG{TSTP} = 'IGNORE';

    # We ought to close all open file descriptors, especially stdin, stdout, stderr,
    # primarily to disconnect from any controlling terminal.
    #
    # However, Perl's i/o layer objects later on if, say, we try to open a file that uses one
    # of these file descriptors (0, 1, or 2) in a manner different from how it is usually
    # used (for example, read-only on file descriptor 1).  Presumably this is to provide a
    # warning against opening these "system" file descriptors (see $SYSTEM_FD_MAX or $^F)
    # in a way that would be inappropriate for actual use as STDIN, STDOUT, or STDERR.  So the
    # simplest approach to dropping any connection to a controlling terminal is to open all of
    # these channels to /dev/null.  But I've seen some advice that due to strange behavior of
    # the Perl i/o layer, we might have to do the open() calls without preceding close() calls,
    # or the open() calls won't connect the STDIN, STDOUT, and STDERR file handles to the file
    # descriptors we expect.  Whether or not that's true, doing it this way is at least safe.
    # close STDERR;
    # close STDOUT;
    # close STDIN;
    # Opening these file descriptors to a safe place instead of leaving them closed also
    # prevents lots of error messages from appearing in our logfile when odd parts of the
    # code (e.g., stuff buried in library modules) tries to access these file handles.
    open STDIN,  '<', '/dev/null';
    open STDOUT, '>', '/dev/null';
    open STDERR, '>', '/dev/null';
    # FIX LATER:  Figure out if we can reliably and efficiently discover what other i/o
    # channels might be open, and close them all.

    # Disassociate from our process group and controlling terminal.
    if (do_fork()) {
	# successful fork; we're in the parent
	exit 0;
    }
    # parent has exited, child remains; make it a session leader (not just a process group leader);
    # the preceding fork was necessary to guarantee that this call succeeds
    POSIX::setsid();

    ## # Do not reacquire a controlling terminal.  To ensure that, become immune from process group leader death ...
    ## $SIG{HUP} = 'IGNORE';
    ## # ... then become non-process-group leader.
    ## if (do_fork()) {
    ##     # successful fork; we're in the parent
    ##     exit 0;
    ## }
    # But in fact we don't want to do that, because the whole point of our exercise here is to become
    # our own process group leader, so all descendants will be killed along with us when our process
    # group is killed.  So we'll just have to be careful not to reacquire a controlling terminal,
    # either by watching what actions we take (don't open any terminal devices), or by forking and
    # having the parent just sleep forever waiting for the shutdown signal to come in.

    # child has exited; grandchild remains

    # Change current directory to '/', to prevent "filesystem busy" problems during unmounts.
    chdir '/';

    # Reset the file mode creation mask to an appropriate value,
    # to override whatever got inherited from the parent process.
    umask 022;
}

1;

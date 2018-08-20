package Replication::Help;

# Handle help information in a GroundWork Monitor Disaster Recovery deployment.
# Copyright (c) 2010 GroundWork Open Source (www.groundworkopensource.com).
# All rights reserved.  Use is subject to GroundWork commercial license terms.

# ================================================================
# Documentation.
# ================================================================

# To do:
# (*) ...

# ================================================================
# Perl setup.
# ================================================================

use strict;
use warnings;

require Exporter;
our @ISA = ('Exporter');

our @EXPORT = qw(
    &help
    &lookup
);

our @EXPORT_OK = qw(
    &print_help
);

# This is where we'll pick up any Perl packages not in the standard Perl
# distribution, to make this a self-contained package anchored in a single
# directory.
use FindBin qw($Bin);
use lib "$Bin/perl/lib";

use Replication::Logger;

# Be sure to update this as changes are made to this module!
my $VERSION = '0.1.1';

# ================================================================
# Working variables.
# ================================================================

# ================================================================
# Help text.
# ================================================================

# To suppress particular lines in the following list of commands (for instance, if
# you want to hide a particular command because you're not yet sure if it will be
# part of the final product, or until it is actually implemented and available for
# use), just put a # character in the first column of those lines.
(my $commands = <<'EOF') =~ s/^#.*\n//mg;

Command syntax:

     #  starts an explanatory comment here; don't type any of it
    <>	encloses the type of parameter you will enter; don't type the <>'s
    []	encloses an optional parameter; don't type the []'s
     |	separates alternative choices; choose one, and don't type the |
   ...	means the previous element can be repeated as often as needed
  word	anything else is to be typed literally as shown

  <application-name> and <database-name>
	You may use predefined aliases for application names and database
	names instead of their full formal names.  This can shorten the
	amount of required typing.  See the replication.conf configuration
	file for the available names and aliases, or use the "alias" command
	(see below) to list them.

Available commands:

  help            # show what you're reading now
  help <command>  # show a detailed explanation of the specified command
  about           # show version and related info
#  login [<username> [<password>]]  # log in to the Replication State Engine
#  engine start    # start the local Replication State Engine
#  engine stop     # stop the local Replication State Engine
  alias all [app|db]            # show aliases for all app's, db's, or both
  alias app <application-name>  # show aliases for this application
  alias db <database-name>      # show aliases for this database
  status all [local|remote] [app|db]  # show specified set of status info
  status [local|remote] app <application-name> ...  # same, for applications
  status [local|remote] db <database-name> ...      # same, for databases
#  status [local|remote] engine  # show Replication State Engine status
  status [local|remote] notify  # show Notification Authority status
  status [local|remote] config  # show Master Configuration Authority status
  status heartbeat      # show status of inter-machine replication heartbeats
  notify grab           # acquire and lock local Notification Authority
  notify dynamic        # have heartbeats control local Notification Authority
  notify release        # revoke and disallow local Notification Authority
  config [forced] grab  # acquire local Master Configuration Authority
  config release        # revoke local Master Configuration Authority
#  pulse                 # run a heartbeat cycle if not already in progress
  block all [app|db]                  # block replication actions
  block app <application-name> ...    # same, for these applications
  block db <database-name> ...        # same, for these databases
  unblock all [app|db]                # unblock replication actions
  unblock app <application-name> ...  # same, for these applications
  unblock db <database-name> ...      # same, for these databases
#  diff all [app|db]                # display local synchronization status
#  diff app <application-name> ...  # same, for these applications
#  diff db <database-name> ...      # same, for these databases
  sync all [app|db]                # initiate local (slave) synchronization
  sync app <application-name> ...  # same, for these applications
  sync db <database-name> ...      # same, for these databases
#  commit app <application-name>  # install a ready application configuration
#  commit db <database-name>      # install a ready database snapshot
  list app <application-name>    # list backups at hand for this application
  list db <database-name>        # list backups at hand for this database
#  rollback app <application-name> [<timestamp>]  # restore from this backup
#  rollback db <database-name> [<timestamp>]      # restore from this backup
  quit  # stop the "recover" client (the Replication Engine stays running)
  exit  # same thing

EOF

my %text = (

    help => <<'EOF',

Usage:	help [<command>]

Without any arguments, "help" displays a list of all the available
commands.

"help <command>" prints a detailed explanation of the specified
command.  What you are reading how is the output of "help help".

EOF

    about => <<'EOF',

Usage:	about

"about" spills out the version of the Replication State Engine
(and possibly of other replication components), and related info.

EOF

    login => <<'EOF',

Usage:	login [<username> [<password>]]

The login command is not yet implemented.  It would provide some level
of authentication and subsequent authorization to control and see status
of the replication system.  In the absence of this command, access will
instead be controlled by restricting access to the UI client program.

EOF

    engine => <<'EOF',

Usage:	engine {start|stop}

"engine start" starts the Replication Engine if it is not already running.
If it is already running, this command has no effect except to state that
fact.

"engine stop" gracefully stops the Replication Engine if it is running.
If it is already down, that fact will be noted.

EOF

    alias => <<'EOF',

Usage:	alias all [app|db]
	alias app <application-name>
	alias db <database-name>

Aliases may be used anywhere an application or database name can be
specified, including on the alias command itself.  They are often
defined as more-common or easier-to-type shorthand for the full
configured name of an object.

"alias all" displays the aliases for all configured applications and
databases.  In addition to providing the aliases for quick reference,
this can serve as a reminder of the full list of application and/or
database objects configured for replication.

"alias all app" displays the aliases for all configured applications.

"alias all db" displays the aliases for all configured databases.

"alias app <application-name>" displays the aliases for just this one
application.

"alias db <database-name>" displays the aliases for just this one
database.

EOF

    status => <<'EOF',

Usage:	status all [local|remote] [app|db]
	status [local|remote] app <application-name> ...
	status [local|remote] db <database-name> ...
#	status [local|remote] engine
	status [local|remote] notify
	status [local|remote] config
	status heartbeat

"status all" prints the status for all the individual components you
might otherwise specify in a "status" command.

"status all [local|remote] app" and
"status [local|remote] app <application-name> ..." print the status of
all or just the named applications, on either the specified site or both
sites.  The most common form of this command is "status all app".

"status all [local|remote] db" and
"status [local|remote] db <database-name> ..." print the status of all
or just the named databases, on either the specified site or both sites.
The most common form of this command is "status all db".

# "status [local|remote] engine" prints the status of either the local or
# remote Replication Engine, as specified.  If neither is specified, the
# status of both engines is printed.
#
Status of a Replication Engine includes:

#   * whether the engine is up or down
  * whether heartbeats to the opposing system are generally working
#   * whether a heartbeat to the opposing system is currently in progress
  * whether any particular applications or databases have replication
    and synchronization actions blocked
  * whether replication and synchronization actions for specific
    applications or databases are currently in progress, stalled, or
    were entirely skipped the last time they were scheduled to run
#   * whether particular applications or databases are currently down

# [FIX THIS:  Perhaps we need to distinguish between replication
# (transferring data between systems) and synchronization (putting
# already-transferred data into actual use {either committing new
# configuration data into production, or rolling back to a previous
# setup}), and report those conditions separately.]
#
# Strictly speaking, the up/down status of replicated applications and
# databases is not actually part of the status of the Replication Engine,
# but it is reported as though it were because the Replication Engine can
# bring those components up and down as part of its dynamic actions.
#
Strictly speaking, Notification Authority and Master Configuration
Authority are not properties of the Replication Engine itself.  Rather,
they are only properties held in custody and managed by the Replication
Engine on behalf of the companion GroundWork Monitor system.
# Thus they are not reported as part of the Replication Engine status.

"status [local|remote] notify" prints the status of either the local
or remote system's current Notification Authority and Notification
Authority Control.  If neither system is specified, the status of both
systems is printed.

"status [local|remote] config" prints the status of either the local or
remote system's current Master Configuration Authority.  If neither system
is specified, the status of both systems is printed.  If both systems
are accessible and seen to be in conflict, that fact will be noted.

For the "status ... remote ..." commands, the status of the remote
system is not directly probed by this command; rather, the timestamped
last state retrieved by a working heartbeat will be shown.

"status heartbeat" prints the status of the heartbeat used to probe the
remote Replication Engine, as seen by the local Replication Engine.

EOF

    notify => <<'EOF',

Usage:	notify grab
	notify dynamic
	notify release

"notify grab" arrogates Notification Authority for the local system,
and locks that choice into place.  This would be useful, for instance,
when you know that the remote system will be undergoing maintenance
and you want the local system to take over.

"notify dynamic" unlocks any previous grab or release choice, and allows
Notification Authority to be automatically and dynamically arrogated and
relinquished by heartbeats on the local system, as sensed conditions vary.

"notify release" relinquishes Notification Authority for the local
system, and locks that choice into place.  This would be useful, for
instance, when you know that the local system will be undergoing
scheduled maintenance and you don't want it to send out spurious
alarms during that period.

EOF

    config => <<'EOF',

Usage:	config [forced] grab
	config release

"config grab" arrogates Master Configuration Authority for the local
system.  If this arrogation is in conflict with the last-known state
of the remote system, that fact will be noted and the request will be
denied.  So if the two systems are still in contact, Master Configuration
Authority must be first released from the remote system before it can
be grabbed by the local system.  This interlock is intended to prevent
serious confusion about where configuration changes are to be made.

"config forced grab" will still check to see if the remote Replication
Engine also has Master Configuration Authority, but the local grab will
take effect even if it does.  This is most often used when the remote
system is currently inaccessible and thus the normal heartbeats cannot
directly change the local notion of the remote state, so the local
notion is out of date; or when the remote system is simply down and its
state cannot be changed.  In normal operation, with the link and both
sides still operating, you would not need to force the grab, as you would
first release it on the remote system.  The next heartbeat would detect
this and change the local notion of the remote state.

"config release" relinquishes Master Configuration Authority for the
local system.

EOF

    pulse => <<'EOF',

Usage:	pulse

"pulse" runs a full heartbeat cycle, initiated from the local system,
if one is not currently in progress.

EOF

    block => <<'EOF',

Usage:	block all [app|db]
	block app <application-name> ...
	block db <database-name> ...

"block all" prevents all replication/synchronization actions from taking
place on the local system.

"block all app" prevents replication/synchronization actions for all
applications on the local system.

"block all db" prevents replication/synchronization actions for all
databases on the local system.

"block app <application-name> ..." prevents replication/synchronization
actions for the specific named applications on the local system.

"block db <database-name> ..." prevents replication/synchronization
actions for the specific named databases on the local system.

These commands affect both the initiation of future full replication or
synchronization actions, and the continuance of ongoing actions to their
next stages.  Any action stages which are currently in flight will not
be affected.

EOF

    unblock => <<'EOF',

Usage:	unblock all [all|db]
	unblock app <application-name> ...
	unblock db <database-name> ...

"unblock all" permits all replication/synchronization actions to take
place on the local system.

"unblock all app" permits replication/synchronization actions for all
applicatioins on the local system.

"unblock all db" permits replication/synchronization actions for all
databases on the local system.

"unblock app <application-name> ..." permits replication/synchronization
actions for the specific named applications on the local system.

"unblock db <database-name> ..." permits replication/synchronization
actions for the specific named databases on the local system.

EOF

    diff => <<'EOF',

Usage:	diff all [app|db]
	diff app <application-name> ...
	diff db <database-name> ...

"diff all" displays the synchronization status for all applications and
databases on the local system.

"diff all app" displays the synchronization status for all applications
on the local system.

"diff all db" displays the synchronization status for all databases on
the local system.

"diff app <application-name> ..." displays the synchronization status
for just the particular named applications on the local system.

"diff db <database-name> ..." displays the synchronization status for
just the particular named databases on the local system.

In each case, the synchronization status reported by "diff" will be more
detailed than just whether the application or database as a whole is
synchronized or not (as reported by the equivalent "status" command).
When possible and appropriate, the state of each file or file tree
configured for replication will be reported, though not the individual
differences in such files.  For a database, [FIX THIS].

EOF

    sync => <<'EOF',

Usage:	sync all [app|db]
	sync app <application-name> ...
	sync db <database-name> ...

"sync all" initiates synchronization (configuration data replication)
operations on all applications and databases on the local system.
(WARNING:  In the current implementation, this is a dangerous command,
to be avoided.)

"sync all app" initiates synchronization operations on all applications
on the local system.

"sync all db" initiates synchronization operations on all databases on
the local system.  (WARNING:  In the current implementation, this is a
dangerous command, to be avoided.)

"sync app <application-name> ..." initiates synchronization operations
on just the specified applications on the local system.

"sync db <database-name> ..." initiates synchronization operations on
just the specified databases on the local system.  Use with caution.

The "sync" command only makes sense to run on the Slave system.  If it
is run on the Master system, the sync operations will refuse to start.

WARNING:  Note that these operations are initiated on the server when
you run these commands, but the server is not directly tethered to your
control session.  Once the command is given, there is no way to interrupt
and stop the invoked actions from your terminal, even partway through
a list of applications or databases.

WARNING:  "sync all" and other commands that start multiple replication
operations currently take no account of when damaging collisions might
occur.  For this reason, you are advised to avoid synchronizing multiple
objects at the same time.  Use separate commands, and wait until each
operation is done before starting the next one.

EOF

    commit => <<'EOF',

Usage:	commit app <application-name>  # only a single application, for now
	commit db <database-name>      # only a single database, for now

At certain times, a new configuration for a system component has been
fully prepared and is ready to transition into production, but that
action has not yet taken place.  The commit command takes that action.

A commit differs from a sync in that a commit only involves operations
on the local system on which it is issued.  Thus, if the new data is
already prepared and ready to go, it can be executed without access to
the opposing system.  In contrast, a sync will generally involve remote
access as well, to pull over and compare data during a replication
process before a final commit takes place.

"commit app <application-name>" takes a ready new configuration, if one
is available for the particular named application, and rolls it into
production.  This will often involve switching new configuration
files into play and bouncing the application so it picks up the new
configuration data.

"commit db <database-name>" takes a ready new database snapshot, if
one is available for the particular named database, and rolls it
into production.  This will generally involve first stopping all
applications which use the database, then loading in the snapshot
data, then restarting those applications.

EOF

    list => <<'EOF',

Usage:	list app <application-name>
	list db <database-name>

The "list" command finds the available complete-configuration backups
for use with the "rollback" command.

"list app <application-name>" lists available backup timestamps for
the particular named application.

"list db <database-name>" lists available backup timestamps for the
particular named database.

EOF

    rollback => <<'EOF',

Usage:	rollback app <application-name> [<timestamp>]
	rollback db <database-name> [<timestamp>]

"rollback app <application-name> [<timestamp>]" rolls back the
configuration of the specified application to the backup recorded under
the specified timestamp.  If no timestamp is given, the most recent
configuration backup snapshot will be used.

"rollback db <database-name> [<timestamp>]" rolls back the content of
the specified database to the backup recorded under the specified
timestamp.  If no timestamp is given, the most recent database backup
snapshot will be used.

Acceptable timestamps have this form:

    YYYY-MM-DD.hh_mm_ss

You will know what timestamp to use based on the output of the "list"
command you use to find a relevant backup to roll back to.

EOF

    quit => <<'EOF',

Usage:	quit

"quit" stops the UI client.  It does not affect the running state of
the Replication Engine.

EOF

    exit => <<'EOF',

Usage:	exit

"exit" stops the UI client.  It does not affect the running state of
the Replication Engine.

EOF

);

foreach my $command (keys %text) {
    $text{$command} =~ s/^#.*\n//mg;
}

# ================================================================
# Supporting subroutines.
# ================================================================

# The new() constructor must be invoked as:
#     my $help = Replication::Help->new ();
# because if it is invoked instead as:
#     my $help = Replication::Help::new ();
# no invocant is supplied as the implicit first argument.

sub new {
    my $invocant = $_[0];   # implicit argument
    # $help_file   = $_[1];   # future possible argument

    # $help_file = "$Bin/../help/$help_file" if $help_file !~ m{^/};

    my $class = ref($invocant) || $invocant;    # object or class name
    # Options are stored in our object hash to prepare for the day when
    # we allow more than one such object in the program.  These copies
    # are not yet referenced later on, though.
    my $self = {
	# help_file => $help_file
    };
    bless $self, $class;
    return $self;
}

# simple "help" command; list all available commands
sub help {
    return [ $commands ];
}

sub lookup {
    return \%text;
}

# FIX THIS:  Fill in appropriate stuff to serialize the complete help
# for all commands in a human-readable format, outputting an arrayref
# pointing to a series of lines to be printed on the user terminal.
sub print_help {
    my @lines = ();
    push @lines, 'FIX THIS:  display a help section here';
    return \@lines;
}

# Internal routine for debugging; not expected to be for general use.
sub log_help {
    foreach my $key (sort keys %text) {
	log_message "$key => $text{$key}";
    }
}

1;

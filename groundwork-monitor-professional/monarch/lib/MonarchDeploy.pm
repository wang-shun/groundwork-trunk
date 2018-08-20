# MonArch - Groundwork Monitor Architect
# MonarchDeploy.pm
#
###############################################################################
# Release 4.6
# March 2018
###############################################################################
#
# Original author: Scott Parris
#
# Copyright 2007-2018 GroundWork Open Source, Inc. ("GroundWork")
# All rights reserved. This program is free software; you can redistribute
# it and/or modify it under the terms of the GNU General Public License
# version 2 as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
#
# Modified 6-Jan 2007 for use with standby server
#
# Calls nagios-foundation-sync.pl on standby to perform a synchronization
# from copied Nagios config files (from build instance for standby) up to
# standby server foundation database, thus ensuring that standby server
# has synchronized Foundation.
#
# Modified 14-Mar 2008 to put a copy of the Monarch db in place on the
# standby server as a backup and to answer the question, what about
# Performance Configs which will be needed over there don't you know.
# If not local primary, do mysqldump monarch to build dir, scp to distant
# etc, do distant mysql load
#
# Changed scp user to nagios. Removed config-current.log file copy.
# Thomas Stocking 9-23-2008
#
# Adjusted the path for 5.3 deploy structure
# Roger
#
# Added use of db.properties for password generalization
# Thomas Stocking 3/29/2009
#
# Script cleanup, fixing simple Perl coding errors and forcing the use of
# nagios at the other end of each scp.
# Glenn Herteg 2011-02-27
#
# Port to PostgreSQL.
# Glenn Herteg November 2011
#
# Fixed bugs in port to PostgreSQL; overhauled general error handling, too.
# Glenn Herteg March 2012
#
# Clean up environment variables to sidestep platform-compatibility issues.
# Glenn Herteg December 2016
#
# Capture a timestamp before Nagios restart, to label events created during sync.
# Glenn Herteg October 2017
#
# Use the "ssh -q" option to suppress noise during a critical child-server connection.
# Glenn Herteg March 2018

use strict;

package Deploy;

# Legacy function signature, now deprecated because it doesn't return a simple failure indicator.
sub deploy(@) {
    my ($outcome, $results) = deploy_group(@_);
    return @$results;
}

# This code assumes you have ssh keys published on the target hosts,
# and can perform scp and ssh without password prompts.
#
sub deploy_group(@) {
    my $group            = $_[1];    # group name must be a DNS-resolved host name
    my $build_folder     = $_[2];    # defined in groups detail
    my $target_etc       = $_[3];    # defined in groups detail
    my $monarch_home     = $_[4];    # set during installation
    my @results          = ();       # used to gather and return messages for display in browser
    my @files            = ();
    my $debug            = 0;
    my $failed           = 0;
    my $pre_restart_time = undef;

    # Prevent any injection vulnerabilities later on.  Underscore
    # is not legal either, but won't cause problems for us here.
    if (!defined($group)) {
	push @results, 'Error:  The group name is not defined.';
	$failed = 1;
    }
    elsif ($group =~ /[^-._a-zA-Z0-9]/ or $group =~ /^-/) {
	push @results, 'Error:  Group name "' . HTML::Entities::encode($group)
	  . '" cannot be used for deploying remote instances.  You must use a valid hostname for that purpose.';
	$failed = 1;
    }
    elsif (!defined($build_folder)) {
	push @results, 'Error:  The build folder is not defined.';
	$failed = 1;
    }
    elsif ($build_folder =~ m{[^-._a-zA-Z0-9/]} or $build_folder =~ /^-/) {
	push @results, 'Error:  Build folder "' . HTML::Entities::encode($build_folder)
	  . '" is not a valid pathname for deploying remote instances.';
	$failed = 1;
    }
    elsif (!defined($target_etc)) {
	push @results, 'Error:  The Nagios etc folder is not defined.';
	$failed = 1;
    }
    elsif ($target_etc =~ m{[^-._a-zA-Z0-9/]} or $target_etc =~ /^-/) {
	push @results, 'Error:  Nagios etc folder "' . HTML::Entities::encode($target_etc)
	  . '" is not a valid pathname for deploying remote instances.';
	$failed = 1;
    }

    # Use standard credentials for Monarch DB manipulation.
    my $dbtype = undef;
    my $dbhost = undef;
    my $dbname = undef;
    my $dbuser = undef;
    my $dbpass = undef;
    unless ($failed) {
	if (not open( FILE, '<', '/usr/local/groundwork/config/db.properties' )) {
	    push @results, "Error:  Cannot open /usr/local/groundwork/config/db.properties to read ($!).";
	    $failed = 1;
	}
	else {
	    while ( my $line = <FILE> ) {
	       if ( $line =~ /^\s*global\.db\.type\s*=\s*(\S+)/  ) { $dbtype = $1 }
	       if ( $line =~ /^\s*monarch\.dbhost\s*=\s*(\S+)/   ) { $dbhost = $1 }
	       if ( $line =~ /^\s*monarch\.database\s*=\s*(\S+)/ ) { $dbname = $1 }
	       if ( $line =~ /^\s*monarch\.username\s*=\s*(\S+)/ ) { $dbuser = $1 }
	       if ( $line =~ /^\s*monarch\.password\s*=\s*(\S+)/ ) { $dbpass = $1 }
	    }
	    close(FILE);
	    if ( !defined($dbname) or !defined($dbhost) or !defined($dbuser) or !defined($dbpass) ) {
		push @results, "Error:  Cannot read the Monarch database configuration.";
		$failed = 1;
	    }
	    elsif ($dbhost =~ /[^-._a-zA-Z0-9]/ or $dbhost =~ /^-/) {
		push @results, 'Error:  Monarch database host "' . HTML::Entities::encode($dbhost) . '" is not a valid hostname.';
		$failed = 1;
	    }
	    # For security purposes.
	    elsif ($dbname =~ /[^a-zA-Z]/) {
		push @results, 'Error:  Monarch database name "' . HTML::Entities::encode($dbname) . '" is not a valid database.';
		$failed = 1;
	    }
	    # Artificial, but for security purposes.
	    elsif ($dbuser =~ /[^a-zA-Z0-9]/) {
		push @results, 'Error:  Monarch database user "' . HTML::Entities::encode($dbuser) . '" is not a valid username.';
		$failed = 1;
	    }
	}
    }

    unless ($failed) {
	# Open nagios.cfg to find files to deploy.
	if (not open( FILE, '<', "$build_folder/nagios.cfg" )) {
	    push @results, "Error:  Cannot open $build_folder/nagios.cfg to read ($!).";
	    $failed = 1;
	}
	else {
	    local %ENV = %ENV;
	    delete $ENV{LD_LIBRARY_PATH};    # GWMON-12809
	    delete $ENV{LD_PRELOAD};         # GWMON-12833
	    while ( my $line = <FILE> ) {
		if ( $line =~ /^\s*resource_file\s*=\s*(\S+)$/ ) {
		    # FIX LATER:  I'm not sure why we don't treat resource_file like any cfg_file.
		    push @results, '', "scp $build_folder/resource.cfg nagios\@$group:$1", '' if $debug;
		    my $res = qx(scp $build_folder/resource.cfg nagios\@$group:$1 2>&1);
		    push @results, split(/\n/, $res);
		    push @results, "Error:  Transferring \"resource.cfg\" to \"$group\": "
		      . ($? == -1 ? 'cannot execute "scp"' : StorProc::wait_status_message($?)) . '.' if $?;
		    $failed = 1 if $?;
		    last if $failed;
		}
		elsif ( $line =~ /^\s*cfg_file\s*=\s*(.*\.cfg)$/ ) {
		    my $file = $1;
		    $file =~ s/$target_etc\///;
		    push @files, $file;
		}
	    }
	    close(FILE);
	}
    }

    unless ($failed) {
	my $sqlfile = "$build_folder/monarch.sql";
	if ( defined($dbtype) && $dbtype eq 'postgresql' ) {
	    my $postgres_dump = '/usr/local/groundwork/postgresql/bin/pg_dump';
	    if ( not -x $postgres_dump ) {
		push @results, "Error:  Cannot find pg_dump!  Unable to back up the $dbname database.";
		$failed = 1;
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
		$ENV{PATH} = '/bin:/sbin:/usr/bin:/usr/sbin';

		my @dump_command = (
		    $postgres_dump,    "--host=$dbhost", "--username=$dbuser", '--no-password',
		    "--file=$sqlfile", '--format=plain', '--clean',            '--encoding=LATIN1',
		    $dbname
		);
		my $dump_command = join(' ', @dump_command);
		my $res = qx($dump_command 2>&1);
		push @results, $dump_command if $?;
		push @results, split(/\n/, $res);
		if ($?) {
		    push @results, 'Error:  Backup command failed (' . StorProc::wait_status_message($?) . ').';
		    $failed = 1;
		}
	    }
	}
	else {
	    ## We ignore $dbhost here because we don't support remote databases in our MySQL-based releases.
	    ##
	    ## FIX MINOR:  This should be modified to use a credentials file instead of
	    ## passing the password on the command line, with a mechanism in place to
	    ## guarantee removal of the credentials file no matter how the script exits.
	    my $dump_command = "/usr/local/groundwork/mysql/bin/mysqldump -u$dbuser -r$sqlfile $dbname";
	    my $res = qx($dump_command -p$dbpass 2>&1);
	    push @results, $dump_command if $?;
	    push @results, split(/\n/, $res);
	    if ($?) {
		push @results, 'Error:  Backup command failed (' . StorProc::wait_status_message($?) . ').';
		$failed = 1;
	    }
	}
    }

    unless ($failed) {
	local %ENV = %ENV;
	delete $ENV{LD_LIBRARY_PATH};    # GWMON-12809
	delete $ENV{LD_PRELOAD};         # GWMON-12833

	push @files, "monarch.sql";
	# push @files, "config-current.log";
	push @files, "nagios.cfg";
	push @files, "cgi.cfg";
	foreach my $file (@files) {
	    push @results, "scp $build_folder/$file nagios\@$group:$target_etc/$file", '' if $debug;
	    my $res = qx(scp $build_folder/$file nagios\@$group:$target_etc/$file 2>&1);
	    push @results, split(/\n/, $res);
	    push @results, "Error:  Transferring \"$file\" to \"$group\": "
	      . ($? == -1 ? 'cannot execute "scp"' : StorProc::wait_status_message($?)) . '.' if $?;
	    $failed = 1 if $?;
	    last if $failed;
	}
    }

    unless ($failed) {
	local %ENV = %ENV;
	delete $ENV{LD_LIBRARY_PATH};    # GWMON-12809
	delete $ENV{LD_PRELOAD};         # GWMON-12833

	push @results, "ssh nagios\@$group /usr/local/groundwork/core/monarch/bin/load_monarch.pl", '' if $debug;
	my $res = qx(ssh nagios\@$group /usr/local/groundwork/core/monarch/bin/load_monarch.pl 2>&1);
	push @results, split(/\n/, $res);
	# $? (i.e., $CHILD_ERROR) is the proper place to look for a success/failure indicator here.
	# In contrast, $! is useless to us in this context, as it is somehow "Bad file descriptor"
	# when the remote script returns an exit status of 0, creating a very misleading impression.
	push @results, "Error:  Remote server (\"$group\") error when loading the \"monarch\" database: "
	  . ($? == -1 ? 'cannot execute "ssh"' : StorProc::wait_status_message($?)) . '.' if $?;
	$failed = 1 if $res =~ /Error:/i or $?;
    }

    unless ($failed) {
	local %ENV = %ENV;
	delete $ENV{LD_LIBRARY_PATH};    # GWMON-12809
	delete $ENV{LD_PRELOAD};         # GWMON-12833

	# GWMON-12865:  We cannot depend on timesync between parent and child, so we need the child's notion of current time.
	# Note that our result processing presumes absolutely no login noise on the ssh call.
	push @results, "ssh -q nagios\@$group date +%s", '' if $debug;
	my $res = qx(ssh -q nagios\@$group date +%s 2>&1);
	push @results, split(/\n/, $res) if $res !~ /^\d+$/;
	push @results, "Error:  Remote server (\"$group\") error when finding the child server time: "
	  . ($? == -1 ? 'cannot execute "ssh"' : StorProc::wait_status_message($?)) . '.' if $?;
	$failed = 1 if $res =~ /Error:/i or $?;

	unless ($failed) {
	    if ($res =~ /^(\d+)$/) {
		$pre_restart_time = $1 - 1;
	    }
	    else {
		push @results, "Error:  Remote server (\"$group\") error when finding the child server time; expecting only a numeric timestamp.";
		$failed = 1;
	    }
	}
    }

    # If the Nagios reload fails, we continue on with the monarch-foundation sync anyway, because all the
    # Nagios and monarch data is now updated, so if Nagios is ever (re)started, it will be operating with
    # the new data.  So we may as well proceed to make sure the rest of the system is set up to match.
    unless ($failed) {
	local %ENV = %ENV;
	delete $ENV{LD_LIBRARY_PATH};    # GWMON-12809
	delete $ENV{LD_PRELOAD};         # GWMON-12833

	# nagios_reload has been deployed to the target machine with the setuid bit (chmod 4750 nagios_reload)
	push @results, "ssh nagios\@$group /usr/local/groundwork/core/monarch/bin/nagios_reload", '' if $debug;
	my $res = qx(ssh nagios\@$group /usr/local/groundwork/core/monarch/bin/nagios_reload 2>&1);
	push @results, split(/\n/, $res);
	push @results, "Error:  Remote server (\"$group\") Nagios reload error: "
	  . ($? == -1 ? 'cannot execute "ssh"' : StorProc::wait_status_message($?)) . '.' if $?;
	$failed = 1 if $res =~ /Error:/i or $?;

	# It says nagios-foundation-sync, but really it carries out a monarch-foundation-sync instead.
	push @results, "ssh nagios\@$group /usr/local/groundwork/core/monarch/bin/nagios-foundation-sync.pl $group $pre_restart_time", '' if $debug;
	$res = qx(ssh nagios\@$group /usr/local/groundwork/core/monarch/bin/nagios-foundation-sync.pl $group $pre_restart_time 2>&1);
	push @results, split(/\n/, $res);
	push @results, "Error:  Remote server (\"$group\") Monarch-Foundation synchronization error: "
	  . ($? == -1 ? 'cannot execute "ssh"' : StorProc::wait_status_message($?)) . '.' if $?;
	$failed = 1 if $res =~ /Error:/i or $?;
    }

    return !$failed, \@results;
}

1;


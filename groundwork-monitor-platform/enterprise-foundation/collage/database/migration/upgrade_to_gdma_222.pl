#!/usr/local/groundwork/perl/bin/perl -w --
# upgrade_to_gdma_222.pl

# This script is used to upgrade database and filesystem content from
# some previous GDMA release (probably the GDMA 2.2.1 release) to the
# GDMA 2.2.2 release.  Files and data for Solaris plugins will be lost
# in this process, given that we are unable to determine whether they
# belong to Solaris/SPARC, Solaris/Intel, or both classifications.
# Such plugins will need to be uploaded again after this script is run.
#
# The actions of this script are idempotent, so there is no danger if
# it is run more than once.
#
# The corresponding updated version of the Foundation database upgrade
# script (migrate-gwcollagedb.sql) must also be run to upgrade to the
# GDMA 2.2.2 release, before this upgrade_to_gdma_222.pl script is run.
# That will ensure that the plugin support tables exist and have the
# correct structure for this script to operate.

############################################################################
# Version 1.0
# March 2011
############################################################################
#
# Author: Glenn Herteg
#
# Copyright 2011 GroundWork Open Source, Inc. (GroundWork)
# All rights reserved.

use strict;

use DBI;

my $all_is_done = 0;

##############################################################################
# Collect credentials from outside sources
##############################################################################

# This must be the user that Foundation runs as.
my $gw_user = 'nagios';
my $gw_uid  = undef;
my $gw_gid  = undef;
unless ( ( $gw_uid, $gw_gid ) = ( getpwnam($gw_user) )[ 2, 3 ] ) {
    die "\n\tERROR:  User '$gw_user' is not present in the password file.\n";
}

my $db_properties = '/usr/local/groundwork/config/db.properties';

my ( $dbhost, $database, $user, $passwd ) = undef;
if ( -e $db_properties ) {
    open( FILE, '<', $db_properties ) or die "\n\tCannot open $db_properties for reading; aborting!\n";
    while ( my $line = <FILE> ) {
	if ( $line =~ /\s*collage\.dbhost\s*=\s*(\S+)/ )   { $dbhost   = $1 }
	if ( $line =~ /\s*collage\.database\s*=\s*(\S+)/ ) { $database = $1 }
	if ( $line =~ /\s*collage\.username\s*=\s*(\S+)/ ) { $user     = $1 }
	if ( $line =~ /\s*collage\.password\s*=\s*(\S+)/ ) { $passwd   = $1 }
    }
    close(FILE);
}
else {
    die "\n\tERROR:  $db_properties does not exist; aborting!\n";
}

##############################################################################
# Connect to DB
##############################################################################

print "\n\tConnecting to $database database with user \"$user\" ...\n";

my $dsn = "DBI:mysql:$database:$dbhost";
my $dbh = undef;

# We turn AutoCommit off because we want to make changes roll back automatically as much as
# possible if we don't get successfully through the entire script.  This is not perfect (i.e.,
# there are companion filesystem changes that will be made outside the control of the database
# transactions, that won't be rolled back as well).  Still, we do the best we can.
#
# We turn PrintError off because RaiseError is on and we don't want duplicate messages printed.
#
# Note that with RaiseError turned on, we will get an exception and the script will immediately
# die (and execute the final END block) if a database access error occurs, unless we encapsulate
# the access in an eval{}; block.  That means that, while we have in place some code below that
# references $dbh->errstr and $sth->errstr, most such code will never actually be exercised.

eval { $dbh = DBI->connect( $dsn, $user, $passwd, { RaiseError => 1, PrintError => 0, AutoCommit => 0 } ) };
if ($@) {
    chomp $@;
    print "\n\tERROR: connect failed ($@)\n";
    exit 1;
}

my $sqlstmt = '';
my $sth     = undef;

##############################################################################
# Run the upgrade conversions
##############################################################################

# First we look for duplicate Plugin.Url values, because they represent plugins
# that supposedly belong to more than one platform category.  In theory, this
# cannot happen because we originally had a unique index on just the Plugin.Name
# field.  Nonetheless, it's good to check for unexpected situations.

my %duplicate_plugin_count = ();
$sqlstmt = "select Url, instances from (select Url, count(*) as instances from Plugin group by Url) url_counts where instances > 1";
$sth     = $dbh->prepare($sqlstmt);
unless ( $sth->execute ) {
    print "\n\tERROR:  ", $sth->errstr, "\n";
    exit 1;
}
while ( my @values = $sth->fetchrow_array() ) {
    $duplicate_plugin_count{ $values[0] } = $values[1];
}
$sth->finish;

if (%duplicate_plugin_count) {
    print "\n\tERROR:  Duplicate plugin URLs were detected in the GWCollageDB.Plugin table.\n";
    foreach my $plugin_url ( sort keys %duplicate_plugin_count ) {
	print "\tERROR:  $plugin_url is listed $duplicate_plugin_count{$plugin_url} times.\n";
    }
    print "\n\tFATAL:  Exiting without any changes due to errors noted above.\n";
    exit 1;
}

# We need the following index changes as part of this upgrade, but we don't make them here
# because we make them instead in the GWCollageDB migration script, which must be run as
# well when a site running GWMEE 6.4 applies the GDMA 2.2.2 patches.  Putting the changes
# there ensures consistency with later complete base-product upgrades for all customers,
# not just fixing those sites which need this interim patch between major releases.  That
# script will also take care of bumping the database-schema version number to reflect the
# changes, and effectively ensure that the changes are performed in an idempotent manner.
#
# ALTER TABLE PluginPlatform ADD UNIQUE INDEX idx_PluginPlatform_Name_Arch USING BTREE (Name, Arch);
# ALTER TABLE Plugin ADD UNIQUE INDEX idx_Plugin_Name_PlatformID USING BTREE (Name, PlatformID);
# ALTER TABLE Plugin DROP INDEX Name;
# ALTER TABLE Plugin DROP INDEX PlatformID;

my @platforms = (
    [ 'AIX-PowerPC',   32, 'AIX PowerPC 32 bit'   ],
    [ 'AIX-PowerPC',   64, 'AIX PowerPC 64 bit'   ],
    [ 'Linux-Intel',   32, 'Linux Intel 32 bit'   ],
    [ 'Linux-Intel',   64, 'Linux Intel 64 bit'   ],
    [ 'Solaris-Intel', 32, 'Solaris Intel 32 bit' ],
    [ 'Solaris-Intel', 64, 'Solaris Intel 64 bit' ],
    [ 'Solaris-SPARC', 32, 'Solaris SPARC 32 bit' ],
    [ 'Solaris-SPARC', 64, 'Solaris SPARC 64 bit' ],
    [ 'Windows-Intel', 32, 'Windows Intel 32 bit' ],
    [ 'Windows-Intel', 64, 'Windows Intel 64 bit' ]
);

foreach my $platform (@platforms) {
    my $name = $platform->[0];
    my $arch = $platform->[1];
    my $desc = $platform->[2];
    my @seen = $dbh->selectrow_array( "select NULL from PluginPlatform where Name=? and Arch=?", {}, $name, $arch );
    unless (@seen) {
	$dbh->do( "insert into PluginPlatform (Name, Arch, Description) values ( ?, ?, ? )", {}, $name, $arch, $desc ) or die $dbh->errstr;
    }
}

my %mappings = (
    'AIX'     => [ 'AIX-PowerPC',   'aix-powerpc'   ],
    'Linux'   => [ 'Linux-Intel',   'linux-intel'   ],
    'Windows' => [ 'Windows-Intel', 'windows-intel' ]
);

foreach my $mapping ( keys %mappings ) {
    foreach my $bitwidth ( '32', '64' ) {
	$sqlstmt = "select Url from Plugin where PlatformID = (select PlatformID from PluginPlatform where Name=? and Arch=?)";
	my $sth = $dbh->prepare($sqlstmt);
	unless ( $sth->execute($mapping, $bitwidth) ) {
	    print "\n\tERROR:  ", $sth->errstr, "\n";
	    exit 1;
	}
	while ( my @values = $sth->fetchrow_array() ) {
	    ( my $old_path = $values[0] ) =~ s<.*/plugin_download/></usr/local/groundwork/apache2/htdocs/agents/plugin_download/>;
	    ( my $new_path = $old_path )  =~ s</plugin_download/></plugin_download/$mappings{$mapping}[1]-$bitwidth/>;
	    ( my $new_dir  = $old_path )  =~ s</plugin_download/.*></plugin_download/$mappings{$mapping}[1]-$bitwidth>;
	    if ( -f $old_path ) {
		if ( -f $new_path ) {
		    ( my $new_file = $new_path ) =~ s<.*/plugin_download/><plugin_download/>;
		    ( my $old_file = $old_path ) =~ s<.*/plugin_download/><plugin_download/>;
		    print "\n\tNOTICE:  $new_file was already established and will not be overwritten\n";
		    print "\tWARNING:  $old_file still exists and will be left in place\n";
		}
		else {
		    if ( !-d $new_dir ) {
			my $old_umask = umask 022;
			mkdir( $new_dir, 0755 ) or die "\n\tERROR:  cannot make directory $new_dir ($!)\n";
			umask $old_umask;
			chown( $gw_uid, $gw_gid, $new_dir ) or die "\n\tERROR:  cannot change ownership of $new_dir ($!)\n";
		    }
		    rename( $old_path, $new_path ) or die "\n\tERROR:  cannot rename $old_path to $new_path ($!)\n";
		}
	    }
	}
	$sth->finish;

	# This will implicitly update the Plugin.LastUpdateTimestamp field as well,
	# according to standard MySQL treatment of the first TIMESTAMP column in a table.
	$dbh->do( "
	    update Plugin
	      set Url = replace(Url,'/plugin_download/','/plugin_download/$mappings{$mapping}[1]-$bitwidth/'),
		  PlatformID = (select PlatformID from PluginPlatform where Name='$mappings{$mapping}[0]' and Arch=$bitwidth)
	    where PlatformID = (select PlatformID from PluginPlatform where Name='$mapping'               and Arch=$bitwidth)
	" );
    }
}

# We cannot necessarily tell whether previous Solaris entries were for Solaris/SPARC or
# Solaris/x86, so we just remove them entirely rather than attempting to classify them.
# We could look to see if a particular plugin is a compiled file, such as running "file"
# on the plugin file, to see if we get back a result that contains something like:
#
#    ELF 32-bit LSB executable, Intel 80386
#    ELF 64-bit MSB executable, SPARC V9
#
# But that wouldn't handle scripts which might be tuned to SPARC or Intel environments,
# so for now we just throw everything into one basket and treat all Solaris plugins the
# same way.
#
$sqlstmt = "select Url from Plugin where PlatformID in (select PlatformID from PluginPlatform where Name='Solaris')";
$sth     = $dbh->prepare($sqlstmt);
unless ( $sth->execute ) {
    print "\n\tERROR:  ", $sth->errstr, "\n";
    exit 1;
}
my @old_paths = ();
while ( my @values = $sth->fetchrow_array() ) {
    ( my $old_path = $values[0] ) =~ s<.*/plugin_download/></usr/local/groundwork/apache2/htdocs/agents/plugin_download/>;
    push @old_paths, $old_path if -f $old_path;
}
$sth->finish;

if (@old_paths) {
    print "\n\tNOTICE:  The following Solaris plugins cannot be attributed to\n";
    print "\teither SPARC or Intel architectures, and so are being deleted.\n";
    print "\tYou must upload these again to put them back in play.\n";
    foreach my $old_path (@old_paths) {
	print "\t    $old_path\n";
	unlink $old_path;
    }
    print "\n";
}
$dbh->do( "delete from Plugin where PlatformID in (select PlatformID from PluginPlatform where Name='Solaris')" );

# Now that we have performed the entire conversion, remove the obsolete PluginPlatform entries.
$dbh->do( "delete from PluginPlatform where Name = 'AIX' or name = 'Linux' or name = 'Solaris' or name = 'Windows'" );

##############################################################################
# Commit Changes
##############################################################################

# Commit all previous changes.  This script is simple enough that we don't have
# any earlier commands that may have performed implicit commit operations, so
# this should be definitive in that sense.  The filesystem changes we made along
# the way should be safe in the sense that if we abort early and re-run this
# script, the actions taken will still restore the database to a sane state
# that will be consistent with any relocated files.
my $rc = $dbh->commit();

##############################################################################
# Done.
##############################################################################

$all_is_done = 1;

END {
    if ($dbh) {
	## Roll back any uncommitted transaction.  If the $dbh->commit() above
	## did not execute, this will leave the GWCollageDB in a state where the
	## upgrade did not complete, but this script is written in such a manner
	## that running it again should safely produce the desired full migration.
	eval { my $rc = $dbh->rollback(); };
	if ($@) {
	    print "\n\tError:  rollback failed: ", $dbh->errstr, "\n";
	}
	$dbh->disconnect();
    }
    if ( !$all_is_done ) {
	print "\n";
	print "\t====================================================================\n";
	print "\t    WARNING:  GDMA plugin upgrade did not fully complete!\n";
	print "\t====================================================================\n";
	print "\n";
	exit 1;
    }
}

print "\n\tUpdate complete.\n\n";


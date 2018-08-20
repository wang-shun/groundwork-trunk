#!/usr/local/groundwork/bin/perl
#
#Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")
#All. rights reserved. This program is free software; you can redistribute it and/or modify it under 
#the terms of the GNU General Public License version 2 as published by the Free Software Foundation.
#
#This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
#even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
#General Public License for more details.
#
#You should have received a copy of the GNU General Public License along with this program; 
#if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
#Boston, MA 02110-1301, USA.  

# repair-upgrade.pl
# Addresses: GWMON-4790
# This script repairs an installation where an upgrade to 5.2 was subsequently performed on a system where:
#
# the 'joe' user was deleted AND/OR
# the 'operator' role was deleted
# User will not be able to login and get and error: "ExternalWidget Does Not Exist."

use DBI;

initDB();

$user = `whoami`;
chomp($user);
unless($user eq "root"){
	print "You must be root to run this script.\n";
    exit(1);
}

if(environmentIsValid()){
	repair();
}
else{
	print "Sorry, this repair can only be performed on a system upgraded to 5.2.0";
}

sub environmentIsValid{
	$validity = 0;
	
	
	$version = `rpm -qi groundwork-monitor-core | grep Version | sed s/' '//g | sed s/Version:// | sed s/Vendor.*//`;
	if($version = "5.2.0"){
		$validity = 1;
	}
	return $validity;
	
}

sub repair{ 
	print "Starting GroundWork Monitor 5.2 repair...\n\n";
	closeSessions();
	stopApache();
	modifyMigrateScripts();
	runMigrateGuava();
	startApache();
	print "\nRepair completed successfully. Thank you for using GroundWork Monitor 5.2\n";
}


sub runMigrateGuava{
	my ($databaseName,$databaseHost,$databaseUser,$databasePassword) = getDBConfig();
	$migrateCommand = "/usr/local/groundwork/bin/php /usr/local/groundwork/migration/migrate-guava-sb.php $databaseHost $databaseName $databaseUser $databasePassword";
	print `$migrateCommand`;

	$migrateCommand = "/usr/local/groundwork/bin/php /usr/local/groundwork/migration/migrate-guava-pro.php $databaseHost $databaseName $databaseUser $databasePassword";
	print `$migrateCommand`;
}


sub modifyMigrateScripts{
	#modify migrate-guava-sb.php
	print "Modifying migrate-guava-sb.php...\n";
	open(SBTMP,">/tmp/migrate-guava-sb.tmp");
        open(SB,"/usr/local/groundwork/migration/migrate-guava-sb.php");
	
	while(<SB>){
		$line = $_;
		if($line =~ "roleassignments"){
			;#skip
		}
		else{
			print SBTMP $line;
		}
		}

	print `/bin/cp /tmp/migrate-guava-sb.tmp /usr/local/groundwork/migration/migrate-guava-sb.php`;
	close(SBTMP);
	close(SB);

	#modify migrate-guava-pro.php
	open(PROTMP,">/tmp/migrate-guava-pro.tmp");
        open(PRO,"/usr/local/groundwork/migration/migrate-guava-pro.php");

        while(<PRO>){
                $line = $_;
                if($line =~ "roleassignments"){print PROTMP "//UPGRADE PATCH- next line commented out\n//" . $line;}
                else{print PROTMP $line;}
                }

        print `/bin/cp /tmp/migrate-guava-pro.tmp /usr/local/groundwork/migration/migrate-guava-pro.php`;
	close(PROTMP);
	close(PRO);

}


sub closeSessions{
	print "Closing any open user sessions...";
	print `rm -rf /tmp/sess*`;
	print `rm -rf /tmp/tpl*`;	
	print "OK\n";
}

sub stopApache{
	print "Stopping Apache web server...";
	print `/etc/init.d/httpd stop`;
	
}


sub startApache{
	print "Starting Apache web server...";
	print `/etc/init.d/httpd start`;
	
	
}

sub removeUser{ 
 my $user = shift;
 print "Removing user '$user'...";
 $query = "delete from guava_users where username='$user'";
  $err = executeQuery($query);
 if($err){
 	print "Error deleting user '$user' $err\n";
 	exit(1);
 }
 else{
 	print "OK\n";
 }

}

sub initDB {
	
	my ($databaseName,$databaseHost,$databaseUser,$databasePassword) = getDBConfig();
 
	# Create database handle
	#
	
 	$dbh = DBI->connect( "DBI:mysql:$databaseName:$databaseHost",
		$databaseUser, $databasePassword )
	  or die "Can't connect to database $databaseName. Error:" . $DBI::errstr;
 
	return 1;
}  


sub getDBConfig{
	my ($dbName,$dbHost,$user,$password);
	open(DBFILE,"/usr/local/groundwork/config/db.properties") || die "Couldn't open db.properties file: $!";
	while(<DBFILE>){
		$line = $_;
		$line = trim($line);
		if($line =~ "guava.database"){
			(undef,$dbName) = split(/=/,$line);	
		}
		elsif($line =~ "guava.dbhost"){
			(undef,$dbHost) = split(/=/,$line);
		}
		elsif($line =~ "guava.username"){
			(undef,$user) = split(/=/,$line);
		}
		elsif($line =~ "guava.password"){
			(undef,$password) = split(/=/,$line);
		}

	}#end while	
	close(DBFILE);
		@retArray = ($dbName,$dbHost,$user,$password);
		return @retArray;
}

sub executeQuery{
	$err = 0;
	# $query = $db->query($sql_query);
	my $query = $_[0];
		#debug("executing $query");	 
	$sth = $dbh->prepare($query)
	  || ($err = "Error preparing query: $query: : ". $dbh->errstr);
 	$sth->{'PrintError'}=0;
	$sth->execute()
	  || ($err = "Error  executing query: $query :  " . $dbh->errstr);	
  	$sth->finish();
  	return $err;
  	
}


sub trim{
	my $string = shift;
	$string =~ s/\s+$//;
	return $string;
}
 

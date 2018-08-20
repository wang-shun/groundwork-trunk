#!/usr/local/groundwork/nms/tools/perl/bin/perl --
#
# Copyright 2007 GroundWork Open Source, Inc.  
# All rights reserved. Use is subject to GroundWork commercial license terms.
#

use strict;
use DBI;

my $nedi_database = "nedi";
my $nedi_host = "florida.groundwork.groundworkopensource.com";
my $nedi_dbuser = "nediuser";
my $nedi_dbpwd = "nediuser";
my $data_file = "/usr/local/groundwork/core/monarch/automation/data/nedi_data.txt";
#my $data_file = "/usr/local/groundwork/core/monarch/automation/data/auto-discovery-nedi.txt";

my $dbh = DBI->connect("DBI:mysql:$nedi_database:$nedi_host", $nedi_dbuser, $nedi_dbpwd) ;
if (!$dbh) {
	print "Can't connect to database $nedi_database. Error:".$DBI::errstr;
	exit 2;
}

my $output = "# name;;ip;;type;;description;;os;;location;;parent;;contact";

my $sqlstmt = "select name, ip, type, description, os, location, contact from devices";
my $sth = $dbh->prepare ($sqlstmt);
$sth->execute;
while(my @values = $sth->fetchrow_array()) {
	if ($values[1]) {
		$output .= "\n$values[0];;$values[1];;$values[3];;$values[4];;$values[5];;$values[2];;$values[6]";
	}
}
$sth->finish;


my %hosts = ();
my $sqlstmt = "select name, ip, oui, device from nodes where ip not in (select ip from devices)";
my $sth = $dbh->prepare ($sqlstmt);
$sth->execute;
while(my @values = $sth->fetchrow_array()) {
	if ($values[1]) {
		$output .= "\n$values[0];;$values[1];;;;$values[2];;;;;;;;$values[3];;";
	}
}
$sth->finish;
$dbh->disconnect();

open(FILE, "> $data_file") || die "Error: Unable to open $data_file $!";
print FILE $output;
close (FILE);

__END__

# nedi table info


# nodes

'name', 'varchar(64)', 'YES', 'MUL', '', ''
'ip', 'int(10) unsigned', 'YES', 'MUL', '', ''
'mac', 'varchar(12)', 'YES', 'MUL', '', ''
'oui', 'varchar(32)', 'YES', '', '', ''
'firstseen', 'int(10) unsigned', 'YES', '', '', ''
'lastseen', 'int(10) unsigned', 'YES', '', '', ''
'device', 'varchar(64)', 'YES', '', '', ''
'ifname', 'varchar(32)', 'YES', '', '', ''
'vlanid', 'smallint(5) unsigned', 'YES', 'MUL', '', ''
'ifmetric', 'tinyint(3) unsigned', 'YES', '', '', ''
'ifupdate', 'int(10) unsigned', 'YES', '', '', ''
'ifchanges', 'int(10) unsigned', 'YES', '', '', ''
'ipupdate', 'int(10) unsigned', 'YES', '', '', ''
'ipchanges', 'int(10) unsigned', 'YES', '', '', ''
'iplost', 'int(10) unsigned', 'YES', '', '', ''


# devices

'name', 'varchar(64)', 'YES', 'MUL', '', ''
'ip', 'int(10) unsigned', 'YES', '', '', ''
'serial', 'varchar(32)', 'YES', '', '', ''
'type', 'varchar(32)', 'YES', '', '', ''
'firstseen', 'int(10) unsigned', 'YES', '', '', ''
'lastseen', 'int(10) unsigned', 'YES', '', '', ''
'services', 'tinyint(3) unsigned', 'YES', '', '', ''
'description', 'varchar(255)', 'YES', '', '', ''
'os', 'varchar(8)', 'YES', '', '', ''
'bootimage', 'varchar(64)', 'YES', '', '', ''
'location', 'varchar(255)', 'YES', '', '', ''
'contact', 'varchar(255)', 'YES', '', '', ''
'vtpdomain', 'varchar(32)', 'YES', '', '', ''
'vtpmode', 'tinyint(3) unsigned', 'YES', '', '', ''
'snmpversion', 'tinyint(3) unsigned', 'YES', '', '', ''
'community', 'varchar(32)', 'YES', '', '', ''
'cliport', 'smallint(5) unsigned', 'YES', '', '', ''
'login', 'varchar(32)', 'YES', '', '', ''
'icon', 'varchar(16)', 'YES', '', '', ''

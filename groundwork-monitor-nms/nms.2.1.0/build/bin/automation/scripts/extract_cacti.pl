#!/usr/local/groundwork/nms/tools/perl/bin/perl --
#
# Copyright 2007 GroundWork Open Source, Inc.  
# All rights reserved. Use is subject to GroundWork commercial license terms.
#

use strict;
use DBI;
#$|=1;

my $cacti_database = "cacti";
my $cacti_host = "localhost";
my $cacti_dbuser = "cactiuser";
my $cacti_dbpwd = "cactiuser";
my $data_file = "/usr/local/groundwork/core/monarch/automation/data/cacti_data.txt";
#my $data_file = "/usr/local/groundwork/monarch/automation/data/auto-discovery-Cacti-Sync.txt";

my $dbh = DBI->connect("DBI:mysql:$cacti_database:$cacti_host", $cacti_dbuser, $cacti_dbpwd) ;
if (!$dbh) {
	print "Can't connect to database $cacti_database. Error:".$DBI::errstr;
	exit 2;
}

my %host_templates = ();
my $sqlstmt = "select id, name from host_template";
my $sth = $dbh->prepare ($sqlstmt);
$sth->execute;
while(my @values = $sth->fetchrow_array()) {
	$host_templates{$values[0]} = $values[1];
}
$sth->finish;

my $output = "# hostname;;description;;template_info;;disabled;;status";

my %hosts = ();
my $sqlstmt = "select hostname, description, host_template_id, disabled, status from host";
my $sth = $dbh->prepare ($sqlstmt);
$sth->execute;
while(my @values = $sth->fetchrow_array()) {
	$output .= "\n$values[0];;$values[1];;$host_templates{$values[2]};;$values[3];;$values[4]";
}
$sth->finish;
$dbh->disconnect();

open(FILE, "> $data_file") || die "Error: Unable to open $data_file $!";
print FILE $output;
#print $output;
print "\n";
close (FILE);


#!/usr/local/groundwork/perl/bin/perl --
#
# read input file to obtain host name
# to work with the companion cron job that sends status
# data format incoming requires that the Host appear in one line (can have Service too)
# subsequent lines without Host are treated as belonging to that Host
# line without Service does not produce a Service sending value
# line with a Host and a Service produces just the Service disable

use strict;
use CollageQuery;
my ( $dbname, $dbhost, $dbuser, $dbpass, $dbtype );
my $dbh   = undef;
my $sth   = undef;
my $query = undef;
my $dsn = '';

my $input= $ARGV[0];
my @services = ();
my @arg = ();
my $host = "";
my $service = "";
my $instance = "";
my $status = "0";
my $output = "OK status is good | value = 50\;60\;70\;\;";
my $hostout = "UP host status is good";
my $line = "";
$ENV{'PGPASSWORD'}='postgres';

( $dbname, $dbhost, $dbuser, $dbpass, $dbtype ) = CollageQuery::readGroundworkDBConfig('monarch');
if ( !defined($dbname) or !defined($dbhost) or !defined($dbuser) or !defined($dbpass) ) {
	print "issue with getting the credentials\n";
	exit;
}
$dsn = "DBI:Pg:dbname=$dbname;host=$dbhost";
$dbh = DBI->connect( $dsn, $dbuser, $dbpass, { 'AutoCommit' => 1 } );
if (!$dbh) {
    print "ERROR:  Cannot connect to database $dbname \n";
    exit 2;
}

if (!open (input,$input. "HOSTS")) {
        print "ERROR: Can't open script hosts list file: $input/HOSTS. ";
        exit 2;
}
if (!open (OUTFILE,'>',$input. "SERVICES")) {
	print "ERROR: Can't open file $input/SERVICES for output services results. ";
	exit 2;
}

while ($line = <input>) {
        chomp $line;
        if ($line=~/^#/) {
                next;
        }
	$line =~ s/ //g;
        @arg = split('\t',$line);
        $host = $arg[0];
	print OUTFILE "$host\t$status\t$hostout\n";
	$query = "select service_names.name, service_instance.name as instance from hosts, service_names, services left join service_instance on (service_instance.service_id = services.service_id) where services.servicename_id = service_names.servicename_id and hosts.name = '$host' and services.host_id = hosts.host_id ";
	$sth   = $dbh->prepare($query);
	if ( !$sth->execute() ) {
		print "failed to execute query\n";
		$sth->finish();
		$dbh->disconnect();
		exit 2;
	}
	while ( my $row = $sth->fetchrow_hashref() ) {
		$service = $$row{name};
		$instance = $$row{instance};
		chomp $instance;
		my $size_of_instance = length ($instance);
		if ($size_of_instance) {
			print OUTFILE "$host\t$service$instance\t$status\t$output\n";
		} else {
			print OUTFILE "$host\t$service\t$status\t$output\n";
		}
	}
	$sth->finish();
}
$dbh->disconnect();
close input;
close OUTFILE;


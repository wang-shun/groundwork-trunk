#!/usr/local/groundwork/perl/bin/perl -w --
#
# Copyright 2007, 2011, 2012, 2014, 2016 GroundWork Open Source, Inc.
# All rights reserved. Use is subject to GroundWork commercial license terms.
#
# This version is compatible with a NeDi 1.5+  database and its associated
# nedi.conf file. Does not operate with NeDi 1.4
#

use strict;
use DBI;
use TypedConfig;
use IO::Socket;

my $nedi_conf = "/usr/local/groundwork/nedi/nedi.conf";

my $dbtype = undef;
my $dbhost = undef;
my $dbname = undef;
my $dbuser = undef;
my $dbpass = undef;

if ( not open( CONF, '<', $nedi_conf ) ) {
    print "ERROR:  Cannot open $nedi_conf ($!).\n";
    exit 2;
}
while (<CONF>) {
    next if /^\s*#/;
    next if /^\s*;/;
    ## FIX LATER:  There's some question as to whether the value should include any
    ## trailing whitespace before the end of the line.  For now, we exclude it, but
    ## we do include any whitespace within the value.
    if (/^\s*(\S+)\t+(\S+(?:\s+\S+)*)\s*$/) {
	my $name  = $1;
	my $value = $2;
	## The last value in the file for each of these settings will prevail.
	if ( $name eq 'backend' and not defined $dbtype ) {
	    $dbtype = $value eq 'mysql' ? 'mysql' : $value eq 'Pg' ? 'postgresql' : 'unknown';
	}
	elsif ( $name eq 'dbtype' ) {
	    $dbtype = $value;
	}
	$dbhost = $value if $name eq 'dbhost';
	$dbname = $value if $name eq 'dbname';
	$dbuser = $value if $name eq 'dbuser';
	$dbpass = $value if $name eq 'dbpass';
    }
}
close CONF;

if ( not defined($dbhost) or not defined($dbname) or not defined($dbuser) or not defined($dbpass) ) {
    print "ERROR:  The full set of database access credentials is not present\n";
    print "        in the $nedi_conf file.\n";
    exit 2;
}

my $data_file = "/usr/local/groundwork/core/monarch/automation/data/nedi_data.txt";

my $dbh     = undef;
my $sqlstmt = '';
my $sth     = undef;

# We turn RaiseError on so the presence of any problem causes the script not to continue as though
# no problem occurred.  But then it will be our responsibility to catch all such exceptions.  We
# turn PrintError off because RaiseError is on and we don't want duplicate messages printed, as we
# will normally be formatting and printing error messages.  But again, it will be our responsibility
# to catch all possible exceptions, or else the script might die without any error output.

my $dsn = '';
if ( defined($dbtype) && $dbtype eq 'postgresql' ) {
    $dsn = "DBI:Pg:dbname=$dbname;host=$dbhost";
}
else {
    $dsn = "DBI:mysql:database=$dbname;host=$dbhost";
}
eval {
    $dbh = DBI->connect( $dsn, $dbuser, $dbpass, { RaiseError => 1, PrintError => 0, 'AutoCommit' => 1 } );
};
if ($@) {
    chomp $@;
    print "ERROR:  Cannot connect to the $dbname database ($@).\n";
    exit 2;
}

# FIX MAJOR:  This heading comment MATCHES the fields being emitted into the output file by
# the code below.  Heading has been adjusted to match the output.
# Auto-Discovery automation schema will be used to import the output file from this script.

my $output = "# name;;ip;;type;;description;;os;;location;;contact;;parent\n";

# FIX MINOR:  We might want to think about adjusting the queries below to try to avoid emitting
# duplicate data, in the various ways it might potentially appear from either the devices table or
# the nodes table.  (Whether that is really a practical problem remains to be proven.)  Note that
# we might get the same nodes.name value attached to multiple nodes.nodip values, if the target
# node has multiple network interfaces.  In that case, the right strategy might be to emit all the
# available data and let the downstream code handle it.
#
# schema and tables used are quite different in 1.5+

$sqlstmt = "select coalesce(device,''), devip, coalesce(type,''), coalesce(description,''), coalesce(devos,''), coalesce(location,''), coalesce(contact,'') from devices";
eval {
    $sth = $dbh->prepare($sqlstmt);
    $sth->execute;
    while ( my @values = $sth->fetchrow_array() ) {
	if ( $values[1] ) {
            my $device_true_ip = long2ip($values[1]);
	    $output .= "$values[0];;$device_true_ip;;$values[2];;$values[3];;$values[4];;$values[5];;$values[6]\n";
	}
    }
    $sth->finish;
};
if ($@) {
    chomp $@;
    print "ERROR:  Cannot access the $dbname database ($@).\n";
    exit 2;
}

my %hosts = ();
$sqlstmt = "select dns.aname, nodarp.nodip, nodes.oui,nodes.device from nodes, nodarp,dns where dns.nodip = nodarp.nodip and nodarp.mac = nodes.mac";
eval {
    $sth = $dbh->prepare($sqlstmt);
    $sth->execute;
    while ( my @values = $sth->fetchrow_array() ) {
	if ( $values[1] ) {
            my $node_true_ip = long2ip($values[1]);
	    $output .= "$values[0];;$node_true_ip;;;;$values[2];;;;;;;;$values[3];;\n";
	}
    }
    $sth->finish;
};
if ($@) {
    chomp $@;
    print "ERROR:  Cannot access the $dbname database ($@).\n";
    exit 2;
}

$dbh->disconnect();

if ( not open( FILE, '>', $data_file ) ) {
    print "ERROR:  Cannot open $data_file ($!).\n";
    exit 2;
}
print FILE $output;
close(FILE);

sub long2ip {
    return inet_ntoa(pack("N*", shift));
}
__END__


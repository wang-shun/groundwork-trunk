#!/usr/local/groundwork/perl/bin/perl
#

# This is a simple script that reads a tabbed file of devices to add to cacti
# the first argument is the file to read
# the second is a file name to hold the list that failed to respond to comm string supplied
# script will  use what is in the file trusting it explcitly
# uses format of multiline sample and discovery script
# we are expecting the Community String "public"....
#

my $convertedip, $result, $execute,@fields, $ipaddress, $convertedip, $templateid;
my @result;
my $tree_name, $sort_method, $tree_name;
open (IN, "< $ARGV[0]");
open (OUT, "> $ARGV[1]");

$execute = "/usr/local/groundwork/php/bin/php /usr/local/groundwork/cacti/htdocs/cli/add_tree.php --list-trees";
@result = `$execute 2>&1`;
foreach $result (@result) {
	print OUT "$result\n";
	($tree_id,$sort_method,$tree_name) = split ('\t', $result);
	if ($tree_name =~ /$ARGV[2]/) { last;}
}

if ( $tree_name !~ /$ARGV[2]/ ) {
	$execute = "/usr/local/groundwork/php/bin/php /usr/local/groundwork/cacti/htdocs/cli/add_tree.php --type=tree --name=$ARGV[2] --sort-method=natural ";
	$result = `$execute 2>&1`;
}

$execute = "/usr/local/groundwork/php/bin/php /usr/local/groundwork/cacti/htdocs/cli/add_tree.php --list-trees";
my @result = `$execute 2>&1`;
foreach $result (@result) {
	($tree_id,$sort_method,$tree_name) = split ('\t', $result);
	if ($tree_name =~ /$ARGV[2]/) { last;}
}

while (<IN>) {
  chomp $_;
  my @fields = split(/\t/, $_);
  $size_of_host = length $fields[0];
  if ( $size_of_host == 0 ) {next;}
  if ( $fields[0] =~ /^#/ ) {next;}
  $convertedip = $fields[1];
  $templateid = '1';

# NOTE  here is the test, if the HOSTS file name has the string in it we say it is to be in Cacti
# make up your own rule as you wish

  if ( $fields[0] =~ /_net-/ ) {
    $result = `snmpget -v 2c -c public -t 2 $convertedip SNMPv2-MIB::sysDescr.0`;
    if ( $result =~ /SNMP/ ) {
      $execute = "/usr/local/groundwork/php/bin/php /usr/local/groundwork/cacti/htdocs/cli/add_device.php --description=$fields[0] --ip=$convertedip --community=public --template=$templateid --avail=snmp --version=2";
      $result = system($execute);
      print OUT "inserted device $fields[0]\n";
    } else { 
      print OUT "ERROR $fields[0] $convertedip  no response\n";
    }

    my $hostid = `env PGPASSWORD='postgres' psql -w cacti --tuples-only -c "select id from host where description = '$fields[0]'"`;
    chomp $hostid;
    $hostid =~ s/\s+//g;
    if ($hostid) {
      $execute = "/usr/local/groundwork/php/bin/php /usr/local/groundwork/cacti/htdocs/cli/add_tree.php --type=node --node-type=host --tree-id=$tree_id --host-id=$hostid --host-group-style=1";
      $result = system($execute);
    }
  }
}
close IN;
close OUT;
exit;
#

# this sub converts a decimal IP to a dotted IP
sub dec2ip ($) {
    join '.', unpack 'C4', pack 'N', shift;
}
# this sub converts a dotted IP to a decimal IP
sub ip2dec ($) {
    unpack N => pack CCCC => split /\./ => shift;
}


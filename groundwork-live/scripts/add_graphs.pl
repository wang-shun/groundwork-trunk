#!/usr/local/groundwork/perl/bin/perl
#

# This is a simple script that reads a tabbed file of devices and interfaces to add graphs to cacti based on interface name
# assume that the device is already in cacti, we look for its id; 
# if it is not there we skip it and the fact goes in to the output file
# format of input file is same as multiline text file for discovery script
# output file contains list of failed attempts
#
# retrieve the host template assigned to this device; subsequent lookups are driven by the graphs associated with that
#
# first selection brings back those graph items associated with the host template which are snmp_qeury based indexed items
# we fill in a graph for each item found, matching on the interface name supplied from spreadsheet
# second selection brings back those graph items which are type cg (not indexed)

my $host_query, $host_template, $host_id, @graph_query, $graph, @cg_graph_query, $result, $graph_template_id, $snmp_query_id, $snmp_query_type_id;
open (IN, "< $ARGV[0]");
open (OUT, ">> $ARGV[1]");
while (<IN>) {
chomp $_;
my @fields = split (/\t/, $_);
$size_of_host = length $fields[0];
if ( $size_of_host == 0 ) {next;}
if ( $fields[0] =~ /^#/ ) {next;}
	if ($fields[0] =~ /_net-/ ) {
		$host_query = `env PGPASSWORD='postgres' psql -w cacti --tuples-only -c "select host_template_id, id from host where description = '$fields[0]' " `; 
		chomp $host_query;
		($host_template,$host_id_string) = split (/\|/,$host_query);
		$host_id = trim($host_id_string);
		if ( ! $host_id ) { print OUT "$fields[0] is not in Cacti database\n"; next; } 
		$execute="/usr/local/groundwork/php/bin/php -q /usr/local/groundwork/cacti/htdocs/cli/add_graphs.php --host-id=$host_id --snmp-field=ifName --list-snmp-values";
		my @result=`$execute 2>&1`;
		foreach $interface_name (@result) {
			if ($interface_name =~ /eth/ ) {	
				$result = `/usr/local/groundwork/php/bin/php -q /usr/local/groundwork/cacti/htdocs/cli/add_graphs.php --host-id=$host_id --graph-type=ds --graph-template-id=22 --snmp-query-id=1 --snmp-query-type-id=2 --snmp-field=ifName --snmp-value=$interface_name`;
				print OUT "/usr/local/groundwork/php/bin/php -q /usr/local/groundwork/cacti/htdocs/cli/add_graphs.php --host-id=$host_id --graph-type=ds --graph-template-id=22 --snmp-query-id=1 --snmp-query-type-id=2 --snmp-field=ifName --snmp-value=$interface_name\n";
				$result = `/usr/local/groundwork/php/bin/php -q /usr/local/groundwork/cacti/htdocs/cli/add_graphs.php --host-id=$host_id --graph-type=ds --graph-template-id=33 --snmp-query-id=1 --snmp-query-type-id=22 --snmp-field=ifName --snmp-value=$interface_name`;
				print OUT "/usr/local/groundwork/php/bin/php -q /usr/local/groundwork/cacti/htdocs/cli/add_graphs.php --host-id=$host_id --graph-type=ds --graph-template-id=33 --snmp-query-id=1 --snmp-query-type-id=22 --snmp-field=ifName --snmp-value=$interface_name\n";
			}
		}
	}
}
close IN;
close OUT;
exit;

sub ltrim($)
{
	my $string = shift;
	$string =~ s/^\s+//;
	return $string;
}
sub trim($)
{
	my $string = shift;
	$string =~ s/^\s+//;
	$string =~ s/\s+$//;
	return $string;
}

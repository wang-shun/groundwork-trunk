#!/usr/local/groundwork/bin/perl --
#
# Copyright 2007 GroundWork Open Source, Inc. (“GroundWork”)  
# All rights reserved. Use is subject to GroundWork commercial license terms.
#

use strict;
use Time::Local;
use CollageQuery ;
use DBI;
use XML::LibXML;

my $xmlfile = $ARGV[0];

open (XMLFILE,"$xmlfile") or die "ERROR: Can't open XML file \"$xmlfile\".\n";
my $data = undef;
my $end_config = undef;
while (my $line=<XMLFILE>) {
	chomp $line;
	$data .= $line;
}
my ($Database_Name,$Database_Host,$Database_User,$Database_Password) = CollageQuery::readGroundworkDBConfig("monarch");
my $dbh = DBI->connect("DBI:mysql:$Database_Name:$Database_Host", $Database_User, $Database_Password) 
	or die "Can't connect to database $Database_Name. Error:".$DBI::errstr;
parse_xml($data);
exit;

sub parse_xml($) {
        my $data = shift;
        my ($host,$service,$service_regx,$type,$enable,$label,$rrdname,$rrdcreatestring,$rrdupdatestring,$graphcgi,$perfidstring,$parseregx,$parseregx_first) = ();
        if ($data) {
                eval {
                        my $parser = XML::LibXML->new();
                        my $doc = $parser->parse_string($data);
                        my @nodes = $doc->findnodes("groundwork_performance_configuration");
                        foreach my $node (@nodes) {
#								print "Processing node $node\n";
								foreach my $servprof ($node->getChildnodes) {
									if ($servprof->hasAttributes()) {
                                        my $servprof_name = $servprof->getAttribute('name');
										print "\tProcessing service profile name=\"$servprof_name\"\n";
									} else {
										print "\tProcessing service profile with no name attribute\n";
									}
#									foreach my $childnode ($servprof->getChildnodes) {
									foreach my $childnode ($servprof->findnodes("graph")) {
										if ($childnode->hasAttributes()) {
											my $graph_name = $childnode->getAttribute('name');
											print "\t\tProcessing graph name=\"$graph_name\"\n";
										} else {
											print "\t\tProcessing graph with no name attribute\n";
										}

										foreach my $key ($childnode->findnodes("host")) {
											$host=$key->textContent;
										}
										foreach my $key ($childnode->findnodes("service")) {
											$service=$key->textContent;
											if ($key->hasAttributes()) {
												$service_regx=$key->getAttribute('regx');
											} 
										}
										foreach my $key ($childnode->findnodes("type")) {
											$type=$key->textContent;
										}
										foreach my $key ($childnode->findnodes("enable")) {
											$enable=$key->textContent;
										}
										foreach my $key ($childnode->findnodes("label")) {
											$label=$key->textContent;
										}
										foreach my $key ($childnode->findnodes("rrdname")) {
											$rrdname=$key->textContent;
										}
										foreach my $key ($childnode->findnodes("rrdcreatestring")) {
											$rrdcreatestring=$key->textContent;
										}
										foreach my $key ($childnode->findnodes("rrdupdatestring")) {
											$rrdupdatestring=$key->textContent;
										}
										foreach my $key ($childnode->findnodes("graphcgi")) {
											$graphcgi=$key->textContent;
										}
										foreach my $key ($childnode->findnodes("perfidstring")) {
											$perfidstring=$key->textContent;
										}
										foreach my $key ($childnode->findnodes("parseregx")) {
											$parseregx=$key->textContent;
											if ($key->hasAttributes()) {
												$parseregx_first=$key->getAttribute('first');
											} 
										}

										print	"\t\t\thost=$host,\n".
											"\t\t\tservice=$service,\n".
											"\t\t\tservice_regx=$service_regx,\n".
											"\t\t\ttype=$type,\n".
											"\t\t\tenable=$enable,\n".
											"\t\t\tlabel=$label,\n".
											"\t\t\trrdname=$rrdname,\n".
											"\t\t\trrdcreatestring=$rrdcreatestring,\n".
											"\t\t\trrdupdatestring=$rrdupdatestring,\n".
											"\t\t\tgraphcgi=$graphcgi,\n".
											"\t\t\tperfidstring=$perfidstring,\n".
											"\t\t\tparseregx=$parseregx,\n".
											"\t\t\tparseregx_first=$parseregx_first\n";
										db_add($host,$service,$service_regx,$parseregx,$parseregx_first,
												$rrdname,$rrdcreatestring,$rrdupdatestring,$graphcgi,
												$type,$enable,$perfidstring,$label);
									}
								}
                        }	# end eval
                };
        }
        if ($@) {
                print "\nError parsing: $@ \n$data\n";  
        } else {
                return ;
        }
}



sub db_add {
	my $host = shift;
	my $service = shift;
	my $service_regx = shift;
	my $parseregx = shift;
	my $parseregx_first = shift;
	my $rrdname = shift;
	my $rrdcreatestring = shift;
	my $rrdupdatestring = shift;
	my $graphcgi = shift;
	my $type = shift;
	my $enable = shift;
	my $perfidstring = shift;
	my $label = shift;

	my $query = "SELECT performanceconfig_id FROM performanceconfig WHERE (host=\"$host\" AND service=\"$service\") ";
	my $sth = $dbh->prepare($query);
	$sth->execute() or die  $@;
	my $id = undef;
	while (my $row=$sth->fetchrow_hashref()) { 
			$id = $$row{performanceconfig_id};
	}
	if ($id) {
		print "ERROR. Performance configuration already exists for host $host and service $service.\n";
		print "Duplicate entries are not permitted. Delete the exisitng entry before adding this entry.\n";
		return;
	}
	$parseregx =~ s/\\/\\\\/g;
	my $query = "INSERT INTO performanceconfig (host,service,perfidstring,parseregx,rrdname,rrdcreatestring,rrdupdatestring,graphcgi,type,enable,parseregx_first,service_regx,label) VALUES (".
					"\"$host\",". 
					"\"$service\",". 
					"\"$perfidstring\",". 
					"\"$parseregx\",". 
					"\"$rrdname\",". 
					"\"$rrdcreatestring\",". 
					"\"$rrdupdatestring\",". 
					"\"$graphcgi\", ".
					"\"$type\", ".
					"\"$enable\", ".
					"\"$parseregx_first\", ".
					"\"$service_regx\", ".
					"\"$label\" ".
					");";
	print "Query=$query\n";
	$dbh->do($query) or die  $@;
	print "Performance configuration for host \"$host\" and service \"$service\" added.";
}


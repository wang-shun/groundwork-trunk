#!/usr/local/groundwork/perl/bin/perl -w --
#
# Copyright 2007-2017 GroundWork Open Source, Inc. ("GroundWork")
# All rights reserved. Use is subject to GroundWork commercial license terms.
#

use strict;
use Time::Local;
use DBI;
use CollageQuery;
use XML::LibXML;

# ================================
# Global variables
# ================================

my $dbh = undef;

# ================================
# Supporting routines
# ================================

sub parse_xml($) {
    my $data = shift;
    my $outcome = 1;
    my (
	$host,            $service,         $service_regx, $type,         $enable,    $label, $rrdname,
	$rrdcreatestring, $rrdupdatestring, $graphcgi,     $perfidstring, $parseregx, $parseregx_first
    ) = ();
    if ($data) {
	eval {
	    my $parser = XML::LibXML->new(
		ext_ent_handler => sub { die "INVALID FORMAT: external entity references are not allowed in XML documents.\n" },
		no_network      => 1
	    );
	    my $doc = undef;
	    eval {
		$doc = $parser->parse_string($data);
	    };
	    if ($@) {
		my ($package, $file, $line) = caller;
		## print STDERR $@, " called from $file line $line.";
		my $error = undef;
		## FIX LATER:  HTMLifying here, along with embedded markup in $error, is something of a hack,
		## as it presumes a context not in evidence.  But it's necessary in the browser context.
		# $@ = HTML::Entities::encode($@);
		# $@ =~ s/\n/<br>/g;
		if ($@ =~ s/external entity callback died: // || $@ =~ /external entity references are not allowed/) {
		    ## First undo the effect of the croak() call in XML::LibXML.
		    $@ =~ s/ at \S+ line \d+\n//;
		    $error = "Bad XML string (parse_xml):\n$@";
		}
		elsif ($@ =~ /Attempt to load network entity/) {
		    $error = "Bad XML string (parse_xml):\nINVALID FORMAT: non-local entity references are not allowed in XML documents.\n$@";
		}
		else {
		    $error = "Bad XML string (parse_xml):\n$@ called from $file line $line.";
		}
		die "$error\n";
	    }
	    else {
		my @nodes  = $doc->findnodes("groundwork_performance_configuration");
		foreach my $node (@nodes) {
		    ## print "Processing node $node\n";
		    foreach my $servprof ( $node->getChildnodes ) {
			if ( $servprof->hasAttributes() ) {
			    my $servprof_name = $servprof->getAttribute('name');
			    print "\tProcessing service profile name=\"$servprof_name\"\n";
			}
			else {
			    print "\tProcessing service profile with no name attribute\n";
			}
			## foreach my $childnode ($servprof->getChildnodes) {  # ... }
			foreach my $childnode ( $servprof->findnodes("graph") ) {
			    if ( $childnode->hasAttributes() ) {
				my $graph_name = $childnode->getAttribute('name');
				print "\t\tProcessing graph name=\"$graph_name\"\n";
			    }
			    else {
				print "\t\tProcessing graph with no name attribute\n";
			    }
			    foreach my $key ( $childnode->findnodes("host") ) {
				$host = $key->textContent;
			    }
			    foreach my $key ( $childnode->findnodes("service") ) {
				$service = $key->textContent;
				if ( $key->hasAttributes() ) {
				    $service_regx = $key->getAttribute('regx');
				}
			    }
			    foreach my $key ( $childnode->findnodes("type") ) {
				$type = $key->textContent;
			    }
			    foreach my $key ( $childnode->findnodes("enable") ) {
				$enable = $key->textContent;
			    }
			    foreach my $key ( $childnode->findnodes("label") ) {
				$label = $key->textContent;
			    }
			    foreach my $key ( $childnode->findnodes("rrdname") ) {
				$rrdname = $key->textContent;
			    }
			    foreach my $key ( $childnode->findnodes("rrdcreatestring") ) {
				$rrdcreatestring = $key->textContent;
			    }
			    foreach my $key ( $childnode->findnodes("rrdupdatestring") ) {
				$rrdupdatestring = $key->textContent;
			    }
			    foreach my $key ( $childnode->findnodes("graphcgi") ) {
				$graphcgi = $key->textContent;
			    }
			    foreach my $key ( $childnode->findnodes("perfidstring") ) {
				$perfidstring = $key->textContent;
			    }
			    foreach my $key ( $childnode->findnodes("parseregx") ) {
				$parseregx = $key->textContent;
				if ( $key->hasAttributes() ) {
				    $parseregx_first = $key->getAttribute('first');
				}
			    }

			    print "\t\t\thost=$host,\n"
			      . "\t\t\tservice=$service,\n"
			      . "\t\t\tservice_regx=$service_regx,\n"
			      . "\t\t\ttype=$type,\n"
			      . "\t\t\tenable=$enable,\n"
			      . "\t\t\tlabel=$label,\n"
			      . "\t\t\trrdname=$rrdname,\n"
			      . "\t\t\trrdcreatestring=$rrdcreatestring,\n"
			      . "\t\t\trrdupdatestring=$rrdupdatestring,\n"
			      . "\t\t\tgraphcgi=$graphcgi,\n"
			      . "\t\t\tperfidstring=$perfidstring,\n"
			      . "\t\t\tparseregx=$parseregx,\n"
			      . "\t\t\tparseregx_first=$parseregx_first\n";

			    # We're careful to execute the routine in any case, and only then combine its result
			    # with previous results, so an error in one row does not prevent other attempts to add
			    # rows to the table.
			    $outcome = db_add(
				$host,    $service,         $service_regx,    $parseregx, $parseregx_first,
				$rrdname, $rrdcreatestring, $rrdupdatestring, $graphcgi,  $type,
				$enable,  $perfidstring,    $label
			    ) && $outcome;
			}
		    }
		}
	    }
	};
    }
    if ($@) {
	chomp $@;
	print "\nError parsing: $@\n$data\n";
	return 0;
    }
    else {
	return $outcome;
    }
}

sub db_add {
    ## The "|| 0" operations on some lines here are done to accommodate the fact that we might have
    ## previously exported NULL values in these particular fields, which would subsequently appear
    ## as undef values here, be quoted into empty strings, and cause INSERT failures when PostgreSQL
    ## doesn't invoke the sloppiness that MySQL does of silently converting an empty string to a 0
    ## value.  (That's what we said here historically, but neither the statement about quoting nor
    ## the statement about INSERT failures is true.  Actually, any incoming undef values would be
    ## changed by $dbh->quote() into unquoted strings representing the NULL value ("NULL"), which
    ## would end up in the database as NULL values as long as the field allows NULL values to be
    ## stored.  [It is true that PostgreSQL does not support the MySQL sloppiness, but that is not
    ## in play here.]  In the present case, these are numeric fields that happen to be allowed to
    ## be NULL in the database.  We simply happen not to want a NULL-value representation of false,
    ## preferring an explicit numeric value instead.)
    ##
    ## Of course, we really shouldn't have NULL values in these fields in the first place, but
    ## that's due to legacy coding sloppiness, which we can't directly do anything about since we
    ## might now be importing profiles exported from setups that still include such NULL values.
    ## But at least we can guarantee that we don't have NULL values in these fields when we insert
    ## new rows during the importing here.
    ##
    ## Substitution of a single space for an empty string in a couple of fields here is not really
    ## desirable.  But the parse_perfconfig_xml() routine in MonarchProfileImport.pm operates that
    ## way because it cannot insert an empty string into the database, owing to interference at the
    ## lower MonarchStorProc level.  So we do the same thing here, in order that the two different
    ## code paths will operate the same way.
    ##
    my $host            = shift;
    my $service         = shift;
    my $service_regx    = shift || 0;
    my $parseregx       = shift || ' ';
    my $parseregx_first = shift || 0;
    my $rrdname         = shift;
    my $rrdcreatestring = shift;
    my $rrdupdatestring = shift;
    my $graphcgi        = shift;
    my $type            = shift;
    my $enable          = shift || 0;
    my $perfidstring    = shift || ' ';
    my $label           = shift;

    ## Doubling of backslashes should now be being handled by the respective call to $dbh->quote(),
    ## instead of by explicit substitution here as this code used to do.
    ##
    ## $parseregx =~ s/\\/\\\\/g;
    $graphcgi  =~ s/^\'//;
    $graphcgi  =~ s/\'$//;
    ## $graphcgi  =~ s/\\/\\\\/g;

    my $q_host            = $dbh->quote($host);
    my $q_service         = $dbh->quote($service);
    my $q_perfidstring    = $dbh->quote($perfidstring);
    my $q_parseregx       = $dbh->quote($parseregx);
    my $q_rrdname         = $dbh->quote($rrdname);
    my $q_rrdcreatestring = $dbh->quote($rrdcreatestring);
    my $q_rrdupdatestring = $dbh->quote($rrdupdatestring);
    my $q_graphcgi        = $dbh->quote($graphcgi);
    my $q_type            = $dbh->quote($type);
    my $q_enable          = $dbh->quote($enable);
    my $q_parseregx_first = $dbh->quote($parseregx_first);
    my $q_service_regx    = $dbh->quote($service_regx);
    my $q_label           = $dbh->quote($label);

    my $host_match    = defined($host)    ? "host = $q_host"       : "host is NULL";
    my $service_match = defined($service) ? "service = $q_service" : "service is NULL";

    my $query = "SELECT performanceconfig_id FROM performanceconfig WHERE $host_match AND $service_match";
    my $sth   = $dbh->prepare($query);
    $sth->execute() or die $@;
    my $id = undef;
    while ( my $row = $sth->fetchrow_hashref() ) {
	$id = $$row{performanceconfig_id};
    }
    if ($id) {
	print "ERROR:  Performance configuration already exists for host $q_host and service $q_service.\n";
	print "Duplicate entries are not permitted. Delete the exisitng entry before adding this entry.\n";
	return 0;
    }
    $query =
	"INSERT INTO performanceconfig "
      . "(host,service,perfidstring,parseregx,rrdname,rrdcreatestring,rrdupdatestring,graphcgi,type,enable,parseregx_first,service_regx,label)"
      . " VALUES ("
      . "$q_host,"
      . "$q_service,"
      . "$q_perfidstring,"
      . "$q_parseregx,"
      . "$q_rrdname,"
      . "$q_rrdcreatestring,"
      . "$q_rrdupdatestring,"
      . "$q_graphcgi,"
      . "$q_type,"
      . "$q_enable,"
      . "$q_parseregx_first,"
      . "$q_service_regx,"
      . "$q_label"
      . ");";
    print "Query = $query\n";
    $dbh->do($query) or die $@;
    print "Performance configuration for host $q_host and service $q_service added.\n";
    return 1;
}

sub print_usage {
    print "usage:  import_perfconfig.pl xml_file\n";
    print "where:  xml_file is the path to an XML file containing entries for the\n";
    print "            performanceconfig table.\n";
}

# ================================
# Main program
# ================================

if (@ARGV != 1) {
    print_usage();
    exit 1;
}

my $xmlfile = $ARGV[0];

open( XMLFILE, "$xmlfile" ) or die "FATAL:  Cannot open XML file \"$xmlfile\".\n";
my $data = '';
while ( my $line = <XMLFILE> ) {
    $line =~ s/\r\n/\n/;
    $data .= $line;
}
close XMLFILE;

my ( $dbname, $dbhost, $dbuser, $dbpass, $dbtype ) = CollageQuery::readGroundworkDBConfig('monarch');
if ( !defined($dbname) or !defined($dbhost) or !defined($dbuser) or !defined($dbpass) ) {
    print "FATAL:  Cannot read credentials for the monarch database.\n";
    exit 1;
}
my $dsn = '';
if ( defined($dbtype) && $dbtype eq 'postgresql' ) {
    $dsn = "DBI:Pg:dbname=$dbname;host=$dbhost";
}
else {
    $dsn = "DBI:mysql:database=$dbname;host=$dbhost";
}
$dbh = DBI->connect( $dsn, $dbuser, $dbpass, { 'AutoCommit' => 1 } )
    or die "Can't connect to database $dbname. Error: " . $DBI::errstr;
unless( parse_xml($data) ) {
    print "\n";
    print "Some errors occurred during processing; see details above.\n";
    exit 1;
}
exit 0;


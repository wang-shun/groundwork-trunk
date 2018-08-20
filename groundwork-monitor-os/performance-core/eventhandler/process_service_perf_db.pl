#!/usr/local/groundwork/bin/perl --
#
# process_service_perfdata_db.pl,v 2.0 02/01/06 
#
# Process Service Performance Data
#
# Copyright 2007 GroundWork Open Source, Inc. (“GroundWork”)  
# All rights reserved. This program is free software; you can redistribute it and/or 
# modify it under the terms of the GNU General Public License version 2 as published 
# by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY 
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
# PARTICULAR PURPOSE.  See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with this 
# program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, 
# Fifth Floor, Boston, MA 02110-1301, USA.
#
# Revision History
# 16-Aug-2005 Peter Loh
#
# 23-Dec-2005 Peter Loh
#		Added capability to use Label macros in RRD create.
#		Also allowed more flexible perfdata. OK if warn,crit,max,min are missing.
#
# 22-Jan-2006 Peter Loh
#		Modified to use database for host,service,rrdfile. Required for Status Viewer integration.
#
#

use strict;
use Time::Local;
use Time::HiRes;
use DBI;
use CollageQuery;

my $start_time = Time::HiRes::time();
my ($rrdname,$rrdcreatestring,$rrdupdatestring,$perfidstring,$parseregx,$parseregx_first,$configlabel);
my $debug = 0;
my $debuglog = ">> /usr/local/groundwork/nagios/eventhandlers/process_service_perf.log";
my $rrdtool = "/usr/local/groundwork/bin/rrdtool";
my %ERRORS = ('UNKNOWN' , '-1',
              'OK' , '0',
              'WARNING', '1',
              'CRITICAL', '2');
my ($lastcheck,$host,$svcdesc,$statustext,$perfdata);
$lastcheck = $ARGV[0];
$host = $ARGV[1];
$svcdesc = $ARGV[2];
$statustext = $ARGV[3];
$perfdata = $ARGV[4];
if ($debug) {
	open(FP, $debuglog) ;
	print FP "---------------------------------------------------------------------\n " ;
	print FP `date`." Host: $host\n Svcdesc: $svcdesc\n Lastcheck: $lastcheck\n Statustext: $statustext\n Perfdata:$perfdata\n " ;
}


my ($Database_Name,$Database_Host,$Database_User,$Database_Password) = CollageQuery::readGroundworkDBConfig("monarch");
#my $Database_Name = "performanceconfig";
#my $Database_Host = "localhost";
#my $Database_User = "root";
#my $Database_Password = "";
my $dbh = DBI->connect("DBI:mysql:$Database_Name:$Database_Host", $Database_User, $Database_Password) 
	or die "Can't connect to database $Database_Name. Error:".$DBI::errstr;
my $query = "SELECT * FROM `performanceconfig` where (service='$svcdesc' and type='nagios' and enable=1) ORDER BY host";	# Order by host puts * before a hostname
my $sth = $dbh->prepare($query);
$sth->execute() or die  $@;
while (my $row=$sth->fetchrow_hashref()) { 
	if (($$row{host} eq "*") or ($$row{host} eq $host )) {			# Last row will be a specific host - if * and host are bot set. Specific host has priority
		$rrdname = $$row{rrdname};
		$rrdcreatestring = $$row{rrdcreatestring};
		$rrdupdatestring = $$row{rrdupdatestring};
		$perfidstring = $$row{perfidstring};
		$configlabel = $$row{label};
		if ($$row{parseregx}) {
			$parseregx = qr/$$row{parseregx}/;
			$parseregx_first = $$row{parseregx_first};
		}

	}
}
# If no match, then check pattern matches
if (!$rrdupdatestring) {
	print FP "No exact service name $svcdesc. Query database for service pattern matches. \n" if $debug;		
	$query = "SELECT * FROM `performanceconfig` where (type='nagios' and enable=1 and service_regx=1) ORDER BY service";	# Order by host puts * before a hostname
	my $sth = $dbh->prepare($query);
	$sth->execute() or die  $@;
	while (my $row=$sth->fetchrow_hashref()) { 
		if (($$row{host} eq "*") or ($$row{host} eq $host )) {			# Last row will be a specific host - if * and host are bot set. Specific host has priority			
			if ($$row{service}) {
				my $serviceregx = qr/$$row{service}/;
				if ($svcdesc =~ /$serviceregx/) {
					if ($rrdupdatestring) {		# check if more than one pattern match
						print FP "Mulitple service matches. Pattern $$row{service} also matches. Ignored. \n" if $debug;		
						next;
					}
					$rrdname = $$row{rrdname};
					$rrdcreatestring = $$row{rrdcreatestring};
					$rrdupdatestring = $$row{rrdupdatestring};
					$perfidstring = $$row{perfidstring};
					$configlabel = $$row{label};
					if ($$row{parseregx}) {
						$parseregx = qr/$$row{parseregx}/;
						$parseregx_first = $$row{parseregx_first};
					}
					print FP "$svcdesc matches database service pattern $$row{service}. Using this entry. \n" if $debug;		
				}
			}
		}
	}
}

if (!defined($perfdata) and ($parseregx eq qr//)) {
	print FP "No performance data or status text regular expression defined.\n" if $debug;
	close FP  if $debug;
	exit;
}

my %macros = (
	'\$RRDTOOL\$' => $rrdtool,
	'\$RRDNAME\$' => $rrdname,
	'\$LASTCHECK\$' => $lastcheck,
	'\$HOST\$' => $host,
	'\$SERVICETEXT\$' => $statustext,
	'\$SERVICE\$' => $svcdesc
);



if (!$rrdupdatestring) {
	print FP "No entry in performance database for select: $query \n" if $debug;
	close FP if $debug;
	exit;		# No perf handling for this host service
}


# Expected perfdata format:
#	'label'=value[UnitOfMeasurement];[warn];[crit];[min];[max]

my @labels=();
my @values=();
my @warns=();
my @crits=();
my @mins=();
my @maxs=();

if (defined($perfdata) and !$parseregx_first) {
		if ($perfdata =~ /\s*\'?(.*?)\'?=(\S+?);(\S*?);(\S*?);(\S*?);(\S*)\s*/)  {		
			while ($perfdata =~ s/\s*\'?(.*?)\'?=(\S+?);(\S*?);(\S*?);(\S*?);(\S*)\s*// ) 
			{
				my $label=$1;
				my $value=$2;
				my $warn=$3;
				my $crit=$4;
				my $min=$5;
				my $max=$6;
				push @labels,$label;
				if ($value=~/([\d\.]+)/) {
					push @values,$1;
				} else {
					push @values,$value;
				}
				push @warns,$warn;
				push @crits,$crit;
				push @mins,$min;
				push @maxs,$max;
				print FP "Adding label=$label,value=$value,warn=$warn,crit=$crit,min=$min,max=$max \n" if $debug;
			} 

		} elsif ($perfdata =~ /\s*\'?(.*?)\'?=(\S+?);(\S*?);(\S*?);(\S*)\s*/)  {		# Allow missing max
			while ($perfdata =~ s/\s*\'?(.*?)\'?=(\S+?);(\S*?);(\S*?);(\S*)\s*// ) 
			{
				my $label=$1;
				my $value=$2;
				my $warn=$3;
				my $crit=$4;
				my $min=$5;
				push @labels,$label;
				if ($value=~/([\d\.]+)/) {
					push @values,$1;
				} else {
					push @values,$value;
				}
				push @warns,$warn;
				push @crits,$crit;
				push @mins,$min;
				print FP "Adding label=$label,value=$value,warn=$warn,crit=$crit,min=$min \n" if $debug;
			} 
		} elsif ($perfdata =~ /\s*\'?(.*?)\'?=(\S+?);(\S*?);(\S*)\s*/)  {		# Allow missing min
			while ($perfdata =~ s/\s*\'?(.*?)\'?=(\S+?);(\S*?);(\S*)\s*// ) 
			{
				my $label=$1;
				my $value=$2;
				my $warn=$3;
				my $crit=$4;
				push @labels,$label;
				if ($value=~/([\d\.]+)/) {
					push @values,$1;
				} else {
					push @values,$value;
				}
				push @warns,$warn;
				push @crits,$crit;
				print FP "Adding label=$label,value=$value,warn=$warn,crit=$crit \n" if $debug;
			} 
		} elsif ($perfdata =~ /\s*\'?(.*?)\'?=(\S+?);(\S*)\s*/)  {		# Allow missing crit
			while ($perfdata =~ s/\s*\'?(.*?)\'?=(\S+?);(\S*)\s*// ) 
			{
				my $label=$1;
				my $value=$2;
				my $warn=$3;
				push @labels,$label;
				if ($value=~/([\d\.]+)/) {
					push @values,$1;
				} else {
					push @values,$value;
				}
				push @warns,$warn;
				print FP "Adding label=$label,value=$value,warn=$warn \n" if $debug;
			} 
		} elsif ($perfdata =~ /\s*\'?(.*?)\'?=(\S+)\s*/ ) {		
			while ($perfdata =~ s/\s*\'?(.*?)\'?=(\S+)\s*// )
			{
				my $label=$1;
				my $value=$2;
				push @labels,$label;
				if ($value=~/([\d\.]+)/) {
					push @values,$1;
				} else {
					push @values,$value;
				}
				print FP "Adding label=$label,value=$value \n" if $debug;
			} 
		} elsif ($perfdata =~ /\s*(\S+?);(\S*?);(\S*?);(\S*?);(\S*)\s*/ ) {	# Allow missing labels, ie value[UnitOfMeasurement];[warn];[crit];[min];[max]
			while ($perfdata =~ s/\s*(\S+?);(\S*?);(\S*?);(\S*?);(\S*)\s*// )  {
				my $value=$1;
				my $warn=$2;
				my $crit=$3;
				my $min=$4;
				my $max=$5;
				if ($value=~/([\d\.]+)/) {
					push @values,$1;
				} else {
					push @values,$value;
				}
				push @warns,$warn;
				push @crits,$crit;
				push @mins,$min;
				push @maxs,$max;
				print FP "Adding value=$value,warn=$warn,crit=$crit,min=$min,max=$max \n" if $debug;
			}
		} elsif ($perfdata =~ /\s*(\S+?);(\S*?);(\S*?);(\S*)\s*/ ) {	# Allow missing labels and missing max
			while ($perfdata =~ s/\s*(\S+?);(\S*?);(\S*?);(\S*)\s*// )  {
				my $value=$1;
				my $warn=$2;
				my $crit=$3;
				my $min=$4;
				if ($value=~/([\d\.]+)/) {
					push @values,$1;
				} else {
					push @values,$value;
				}
				push @warns,$warn;
				push @crits,$crit;
				push @mins,$min;
				print FP "Adding value=$value,warn=$warn,crit=$crit,min=$min \n" if $debug;
			}
		} elsif ($perfdata =~ /\s*(\S+?);(\S*?);(\S*)\s*/ ) {	# Allow missing labels and missing min
			while ($perfdata =~ s/\s*(\S+?);(\S*?);(\S*)\s*// )  {
				my $value=$1;
				my $warn=$2;
				my $crit=$3;
				if ($value=~/([\d\.]+)/) {
					push @values,$1;
				} else {
					push @values,$value;
				}
				push @warns,$warn;
				push @crits,$crit;
				print FP "Adding value=$value,warn=$warn,crit=$crit \n" if $debug;
			}
		} elsif ($perfdata =~ /\s*(\S+?);(\S*)\s*/ ) {	# Allow missing labels and missing crit
			while ($perfdata =~ s/\s*(\S+?);(\S*)\s*// )  {
				my $value=$1;
				my $warn=$2;
				if ($value=~/([\d\.]+)/) {
					push @values,$1;
				} else {
					push @values,$value;
				}
				push @warns,$warn;
				print FP "Adding value=$value,warn=$warn \n" if $debug;
			}
		} elsif ($perfdata =~ /\s*(\S+)\s*/ ) {	# Allow missing labels, ie value[UnitOfMeasurement];[warn];[crit];[min];[max]
			while ($perfdata =~ s/\s*(\S+)\s*// )  {
				my $value=$1;
				if ($value=~/([\d\.]+)/) {
					push @values,$1;
				} else {
					push @values,$value;
				}
				print FP "Adding value=$value \n" if $debug;
			}
		} else {
			print FP "No perfdata \"label=value\" string in $perfdata \n" if $debug;
		}
		##############
	if ($#values < 0) {
		print FP "Invalid perfdata format in \"$perfdata\" \n" if $debug;
		close FP if $debug;
		exit;		# No perf handling for this host service
	}
} elsif ($parseregx) {
	$_ = $statustext;				
	@values = /$parseregx/ ;	# @values is same as array of $1,$2,$3,...
	if ($#values < 0) {
		print FP "No match in status text for regular expression $parseregx.\n" if $debug;
		close FP if $debug;
		exit;		# No perf handling for this host service
	} else {
		print FP "Match in status text for regular expression $parseregx\n" if $debug;
	}
}

# check to see if RRD exists. If not then create 
my $rrdfilename =  replace_macros("$rrdname",\%macros,\@labels);
$rrdfilename =~ s/\s/_/g;
if (!stat($rrdfilename)) {
	my $rrdcommand = replace_macros($rrdcreatestring,\%macros,\@labels,\@values,\@warns,\@crits,\@mins,\@maxs);
	my @lines = qx($rrdcommand);
	my $cmd = "chown nagios.nagios $rrdfilename";
	@lines = qx($cmd);
	$cmd = "chmod g+w $rrdfilename";
	@lines = qx($cmd);
	print FP "Create rrd command: $rrdcommand\n";
	#createsvdb($host,$svcdesc,"RRD",$configlabel,$rrdfilename);	# Call sub to insert into sv grph config database
}

# check here for existing 
createsvdb($host,$svcdesc,"RRD",$configlabel,$rrdfilename);	# Call sub to insert into sv grph config database
$dbh->disconnect();

my $rrdcommand = replace_macros($rrdupdatestring,\%macros,\@labels,\@values,\@warns,\@crits,\@mins,\@maxs);

print FP qq($rrdcommand) if $debug;
my @lines = qx($rrdcommand);
print FP "\nReturn: " . "@lines" . "\n" if $debug;

if ($debug) {
	$start_time = Time::HiRes::time() - $start_time;
	print FP "Execution time = $start_time seconds\n" ;
	close FP;
}
exit 0;

####################################################################
# Sub routine for usage information
#
sub usage {
   print "Required arguments not given!\n\n";
   print "Performance Data handler plugin for Nagios, V1.2\n";
   print "Copyright (c) 2004 Groundwork Open Source Solutions, All Rights Reserved \n\n";
   print "Usage: process_service_perfdata <host> <svc description> <perfdata>\n"   ; 
   exit $ERRORS{"UNKNOWN"};
}
####################################################################

sub replace_macros {
	my $string = shift;
	my $macros_ref = shift;
	my $labels_ref = shift;
	my $values_ref = shift;
	my $warns_ref = shift;
	my $crits_ref = shift;
	my $mins_ref = shift;
	my $maxs_ref = shift;
	my $i;

	$string =~ s/\$RRDNAME\$/$$macros_ref{'\$RRDNAME\$'}/g;		# RRDNAME may contain other macros, ie $HOST$, $SERVICE$, so substitute this first

	# Process LIST macros
	#   Substitute LABELLIST with semicolon separated labels  , ie $LABELLIST$ => label1:label2:label3
	if ($string =~ /\$LABELLIST\$/) {
		my $list = "";
		foreach my $label (@$labels_ref) {
			$list .= "$label:";
		}
		$list =~ s/:$//;	# Delete trailing :
		$string =~ s/\$LABELLIST\$/$list/g;
	}

	#   Substitute VALUELIST with semicolon separated labels  , ie $VALUELIST$ => 1:2:3
	if ($string =~ /\$VALUELIST\$/) {
		my $list = "";
		foreach my $value (@$values_ref) {
			$list .= "$value:";
		}
		$list =~ s/:$//;	# Delete trailing :
		$string =~ s/\$VALUELIST\$/$list/g;
	}

	#   Substitute LISTSTART-LISTEND marcos,  ie $LISTSTART$DS:$LABEL#$:GAUGE:900:U:U$LISTEND$ => DS:label1:GAUGE:900:U:U DS:label2:GAUGE:900:U:U DS:label3:GAUGE:900:U:U
	if ($string =~ /\$LISTSTART\$(.*?)\$LISTEND\$/) {
		print FP "Found LISTSTART and LISTEND in $string\n" if $debug;
		my $tmpstring1 = $1;
		if ($tmpstring1 =~ /\$LABEL\#\$/) {
			print FP "Found LABEL# in $tmpstring1\n" if $debug;
			my $tmpstring2 = "";
			foreach my $label (@$labels_ref) {
				$tmpstring2 .= $tmpstring1." ";
				$tmpstring2 =~ s/\$LABEL\#\$/$label/g;	
			}
			$tmpstring2 =~ s/ $//;	# Delete trailing blank
			$string =~ s/\$LISTSTART\$.*\$LISTEND\$/$tmpstring2/g;
			print FP "Reseting string to $string\n" if $debug;
		}
	}

	foreach my $macro (keys %$macros_ref) {
		$string =~ s/$macro/$$macros_ref{$macro}/g;
	}
	$i=1;
	foreach my $label (@$labels_ref) {
		$string =~ s/\$LABEL$i\$/$label/g;
		$i++;
	}
	$i=1;
	foreach my $value (@$values_ref) {
		$string =~ s/\$VALUE$i\$/$value/g;
		$i++;
	}
	$i=1;
	foreach my $warn (@$warns_ref) {
		$string =~ s/\$WARN$i\$/$warn/g;
		$i++;
	}
	$i=1;
	foreach my $crit (@$crits_ref) {
		$string =~ s/\$CRIT$i\$/$crit/g;
		$i++;
	}
	$i=1;
	foreach my $min (@$mins_ref) {
		$string =~ s/\$MIN$i\$/$min/g;
		$i++;
	}
	$i=1;
	foreach my $max (@$maxs_ref) {
		$string =~ s/\$MAX$i\$/$max/g;
		$i++;
	}

	return $string;
}


#
#	Subroutine to create an entry in the performanceconfig database for each RRD created
#	Will add entry in the 
#
sub createsvdb {
	#($host,$svcdesc,"RRD",$configlabel,$rrdfilename);	# Call sub to insert into sv grph config database
	my $host = shift;
	my $service = shift;
	my $type = shift;
	my $configlabel = shift;
	my $rrdfilename = shift;
	my $query = "SELECT hs.host_service_id FROM host_service as hs, datatype as dt ". 
				"WHERE (hs.host='$host' AND hs.service='$service' AND dt.type='$type' AND dt.location='$rrdfilename' AND hs.datatype_id=dt.datatype_id) ";
	print FP "SQL = $query\n" if $debug;
	my $sth = $dbh->prepare($query);
	if (!($sth->execute())) {
		 print FP $@;
		 return;
	}
	my $id = undef;
	while (my $row=$sth->fetchrow_hashref()) { 
			$id = $$row{host_service_id};
	}
	if ($id) {	
		print FP "Table host_service, host=$host, service=$service already has an exisitng entry for location $rrdfilename. New entry not added.\n" if $debug;
		return;
	}
	my $query = "INSERT INTO datatype (type,location) VALUES('$type','$rrdfilename')";
	print FP "SQL = $query\n" if $debug;
	my $sth = $dbh->prepare($query);
	if (!($sth->execute())) {
		 print FP $@;
		 return;
	}
	my $query = "SELECT datatype_id FROM datatype ". 
				"WHERE (type='$type' AND location='$rrdfilename') ";
	print FP "SQL = $query\n" if $debug;
	my $sth = $dbh->prepare($query);
	if (!($sth->execute())) {
		 print FP $@;
		 return;
	}
	my $id = undef;
	while (my $row=$sth->fetchrow_hashref()) { 
			$id = $$row{datatype_id};
	}
	if (!$id) {	
		print FP "No datatype_id found for type $type, location $rrdfilename. Possible error on insert into table datatyoe.\n";
		return;
	}
	my $query = "INSERT INTO host_service (host,service,label,dataname,datatype_id) VALUES('$host','$service','$configlabel','','$id')";
	print FP "SQL = $query\n" if $debug;
	my $sth = $dbh->prepare($query);
	if (!($sth->execute())) {
		 print FP $@;
		 return;
	}
	print FP "Successfully inserted entry into datatype and host_service tables.\n" if $debug;
	return;
}

__END__



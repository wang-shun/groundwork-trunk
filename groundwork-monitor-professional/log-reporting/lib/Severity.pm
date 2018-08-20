#!/usr/local/groundwork/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

package Severity;
use DBLib;

sub getSeverities{

$query = "select SeverityName from Severity";
@sevArray = ();
$sth = DBLib::executeQuery($query);
$sth->bind_col(1,\$severity);
while($sth->fetch){
	push(@sevArray,$severity);
}
return @sevArray;


}

1;
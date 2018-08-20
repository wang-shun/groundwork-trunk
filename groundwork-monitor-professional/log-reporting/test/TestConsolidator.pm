#!/usr/local/groundwork/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#
package TestConsolidator;
use lib qw(/usr/local/groundwork/log-reporting/lib);
 
use GWTest;
@ISA = qw(GWTest);

sub new{
	my ($invocant,$testName) = @_;
	my $class = ref($invocant) || $invocant;
	my $self = GWTest->new("Consolidator");
	bless($self,$class);
	###
 	$hashRef = $self->{subTests};
	my %tests = %$hashRef;
	%tests = _getSubTestList();
	$self->{subTests} = \%tests;
 	###
	return $self;
}

sub verify_consolidation{

$ruleID = shift;
 $persistence = shift;
 $groupBy = shift;
 
 #returns parsingRuleID,DATE,consolidated count
 $selQuery = qq{
 
SELECT  parsingRuleID,
	DATE(timestamp),
	sum(count) as count
FROM LogMessage
WHERE ABS(DATEDIFF(timestamp,CURRENT_DATE())) >= $persistence
and  parsingRuleID = $ruleID
group by parsingRuleID,date(timestamp)
 
 };
 
$sth = DBLib::executeQuery($selQuery);
$sth->bind_col(1,\$parsingRuleID);
$sth->bind_col(2,\$date);
$sth->bind_col(3,\$consolidatedCount); 

while($sth->fetch){
 #DELETE QUERY
$delQuery = qq{
 DELETE FROM LogMessage
 WHERE parsingRuleID = $parsingRuleID
 AND DATE(timestamp) = $date

};
DBLib::executeQuery($delQuery);

#INSERT QUERY
$insQuery = qq{
 INSERT INTO LogMessage(parsingRuleID,logFileID,timestamp,count)
values($parsingRuleID,9999,$date,$consolidatedCount)
};
DBLib::executeQuery($insQuery);

}
}
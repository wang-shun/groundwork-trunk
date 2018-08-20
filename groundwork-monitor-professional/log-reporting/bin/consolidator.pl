#!/usr/local/groundwork/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

use lib qw(/usr/local/groundwork/log-reporting/lib);
use LogMessageType;
use DBLib;

DBLib::initDB();

print "Initializing Consolidator...\n"; 

do_consolidation();

sub do_consolidation{

@logMessageTypes = LogMessageType::getTypeListCollection();
$size = @logMessageTypes;
print "Log Message Types"; 
foreach $type (@logMessageTypes){
  @parsingRules = getParsingRuleIDs4MessageType($type); 
  print ": $type->{name} ";
  foreach $ruleID(@parsingRules){
    consolidate($ruleID,$type->{persistence},$type->{groupBy});
     
  }
}
   print "\n";

}

sub consolidate{
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

sub getParsingRuleIDs4MessageType{

 $type = shift;
 my $id = $type->{id};
 
 $query = qq{
   SELECT distinct lm.parsingRuleID
   FROM LogMessage lm,ParsingRule pr
   WHERE lm.parsingRuleID = pr.parsingRuleID and 
         pr.logMessageTypeID = $id
 };
 
 $sth = DBLib::executeQuery($query);
 $sth->bind_col(1,\$parsingRuleID);
 while($sth->fetch()){
    push(@rules,$parsingRuleID);
 }
 return @rules;
}



sub old{



 
 
 #test
$query = qq{
 select  parsingRuleID,ABS(DATEDIFF(timestamp,CURRENT_DATE())) as dif,sum(count)
from LogMessage
WHERE ABS(DATEDIFF(timestamp,CURRENT_DATE())) >= 78
group by ParsingRuleID
};

#query select
$query = qq{
select parsingRuleID,9999 as logFileID,DATE(timestamp),sum(count)

from
(SELECT  parsingRuleID,
	timestamp,
	sum(count) as count
FROM LogMessage
WHERE ABS(DATEDIFF(timestamp,CURRENT_DATE())) >= 78
group by ParsingRuleID,timestamp) a

group by parsingRuleID,DATE(timestamp)
};
#query delete where List above and logfileid ne 99999


#test 
$query = qq{
select parsingRuleID,DATE(timestamp),sum(count)

from
(SELECT  parsingRuleID,
	timestamp,
	sum(count) as count
FROM LogMessage
WHERE ABS(DATEDIFF(timestamp,CURRENT_DATE())) >= 78
group by ParsingRuleID,timestamp) a

group by parsingRuleID,DATE(timestamp)
};

}
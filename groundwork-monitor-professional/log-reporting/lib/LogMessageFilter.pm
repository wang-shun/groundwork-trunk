#!/usr/local/groundwork/bin/perl 
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

package LogMessageFilter;
use ParsingRule;
use CGI;
 use GWLogger;
#$CGIquery = CGI->new();
 
	#$xmlParam =  $CGIquery->param("xml"); #tables in DB
#if($xmlParam ne ""){
#print "Content-type: text/xml\n\n";
#getFilterListXML();
#};

sub saveLogMessageFilter{

my $listOpt = shift;
my ($isEnabled,$prID,$parsingRuleText,$components,$logMessageType,$logfileType,$parsingRuleName,$severity) = split(/ZZZ/,$listOpt);
$parsingRuleID = $prID;


	#get logfileTypeIDD
	$logfileTypeID = LogFileType::getLogfileTypeID($logfileType);
	#get logMessageTypeID
	$logMessageTypeID = ParsingRule::getLogMessageTypeID($logMessageType);
	
	if( ($logfileTypeID   eq  '') || ($logfileTypeID =~ /^\s+$/) ){
      return 1;
	} 
	
$isEnabled eq "Yes"?($isEnabled = 1):($isEnabled=0);	

GWLogger::log(" isEnabled = $isEnabled \n PRID= $prID\n RuleText=$parsingRuleText\n components=$components\n msgType=$logMessageType\n fileTypeID=$logfileTypeID\n ruleName=$parsingRuleName\nseverity=$severity\n");
#IF NEW

	$parsingRuleText =~ s/\\/\\\\/g;	
	if($prID eq ""){ 	
		GWLogger::log("New entry");
	#update ParsingRule			 
	
	$query = qq{
				insert into ParsingRule(isEnabled,
				                        parsingRuleName,
										logfileTypeID,
										parsingRuleText,
										logMessageTypeID,
										severityID)
				values('$isEnabled',
				       '$parsingRuleName',
					   '$logfileTypeID',
					   '$parsingRuleText',
					(select logMessageTypeID from LogMessageType where logMessageTypeName = '$logMessageType'),
					(select severityID from Severity where SeverityName = '$severity')
					 );
				};
	GWLogger::log("query = $query");			
	#$qID = $dbh->query($query);
 	 $parsingRuleID = DBLib::executeMySQLQuery($query);
	#$parsingRuleID = $sth->insert_id;
	GWLogger::log("parsingRuleID = $parsingRuleID");
	#uupdate COMPONENTS PRCT
	@comps = split(',',$components);
	$pCnt = 1;
	foreach $c(@comps){
		$ctID = ComponentType::queryID($c);
		GWLogger::log("CID $c = $ctID");
		$query = qq{
					insert into ParsingRule_ComponentType(parsingRuleID,componentTypeID,precedence)
					values('$parsingRuleID','$ctID','$pCnt');
					
					};
		$sth = DBLib::executeQuery($query);
		$sth->finish();
		$pCnt++;
		}
	}
##IF AN UPDATE /NOT NEW
	else{
		$query = qq{update ParsingRule
					set parsingRuleName = '$parsingRuleName',
					logfileTypeID = '$logfileTypeID',
					parsingRuleText = '$parsingRuleText',
					logMessageTypeID = (select logMessageTypeID from LogMessageType where logMessageTypeName = '$logMessageType'),
					isEnabled = '$isEnabled',
					severityID = (select severityID from Severity where SeverityName = '$severity')
					WHERE parsingRuleID = '$parsingRuleID';
					};
		$sth = DBLib::executeQuery($query);
		$sth->finish();
		
		
	#UPDATE COMPONENTS###############
	
	#first delete all
    $query = qq{
               delete from ParsingRule_ComponentType 
               where parsingRuleID = '$parsingRuleID'
                 };
    $sth = DBLib::executeQuery($query);
    $sth->finish();

    #then add all
	@comps = split(',',$components);
	$pCnt = 1;
	foreach $c(@comps){
		$ctID = ComponentType::queryID($c);
		GWLogger::log("CID $c = $ctID");
		$query = qq{
					insert into ParsingRule_ComponentType(parsingRuleID,componentTypeID,precedence)
					values('$parsingRuleID','$ctID','$pCnt');
					
					};
		$sth = DBLib::executeQuery($query);
		$sth->finish();
		}	
	############
		
		}#else
		
## STILL NEED TO UPDATE COMPONENTS
return 1;
}
 

sub getFilterListXML{
DBLib::initDB();
print "<ArrayofFilters>";
#grab all but components
	$mquery = "
	select p.parsingRuleName,
			lft.typeName,
			p.parsingRuleText,
			lmt.logMessageTypeName,
			p.parsingRuleID,
			p.isEnabled,
			s.severityName
 	from 	ParsingRule p, 
 			LogfileType lft,
		LogMessageType lmt,
		Severity s
	where lft.logfileTypeID = p.logfileTypeID
	and lmt.logMessageTypeID = p.logMessageTypeID
	and s.severityID = p.severityID
	";
	
	$sth = DBLib::executeQuery($mquery);	  
	 
 	$sth->bind_col(1,\$prn); #parsing rule name
 	$sth->bind_col(2,\$tn); #log file type
 	$sth->bind_col(3,\$prt); #regex
 	$sth->bind_col(4,\$lmt); #log message type
    $sth->bind_col(5,\$pid); #parsing rule ID
    $sth->bind_col(6,\$isEnabled); #bool isEnabled
	$sth->bind_col(7,\$severity);
# foreach filter get components
$dataCnt = 0;
	
while($sth->fetch){
	$dataCnt++;
	## get the components
	$compQuery = qq{ select ct.componentTypeName
						from ComponentType ct,
							ParsingRule_ComponentType prct
						where prct.parsingRuleID = $pid
						and ct.componentTypeID = prct.componentTypeID
					order by prct.precedence
					};
	$sth2 = DBLib::executeQuery($compQuery) || print "cant execute";
	 
	$sth2->bind_col(1,\$comp) || print "cant bind"; 
	$components = "";
	while($sth2->fetch){
		$components .= "${comp},";
		}
	$sth2->finish();
	$components =~ s/,$//;
	#print qq{["$tn","$lmt","$prn","$pid","$prt","$components"],\n};
	if($isEnabled){$enabledStatus = "Yes";} 
	else{$enabledStatus = "No";}
	print qq{	<filter>
				<isEnabled>$enabledStatus</isEnabled>
				<logFileType>$tn</logFileType>
				<logMessageType>$lmt</logMessageType>
				<ruleName>$prn</ruleName>
				<severity>$severity</severity>
				<ruleID>$pid</ruleID>
				<regex>$prt</regex>
				<components>$components</components>
				</filter>
	};
	
}
 $sth->finish();

print "</ArrayofFilters>";	
}
 
sub deleteLogMessageFilter{
	my $listOpt = shift;
	
my ($isEnabled,$prID,$parsingRuleText,$components,$logMessageType,$logfileType,$parsingRuleName) = split(/ZZZ/,$listOpt);
my $parsingRuleID = $prID;
	#my $parsingRuleID = $_[0];
	GWLogger::log("parsingRuleID DEL: $parsingRuleID");
	$query = qq{
				delete from ParsingRule
				where parsingRuleID ='$parsingRuleID';
				};
	GWLogger::log("Query: $query");
	$sth = DBLib::executeQuery($query);
	$sth->finish();
	return 1;
}
1;
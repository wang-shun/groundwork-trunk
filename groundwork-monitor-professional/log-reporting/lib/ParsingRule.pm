#!/usr/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#


package ParsingRule;
use DBLib;
use LogFileType;
use ComponentType;


sub new{
	my ($invocant,$id,$regex) = @_;
	my $class = ref($invocant) || $invocant;
	my $self = {
		regex=>$regex,
		id=>$id
	};
	bless($self,$class);
	return $self;
}

 
sub getRulesByLogType{
	DBLib::initDB();
	$type = shift;
	#my $type = $self->{logMessageType};
	$logfileTypeID = LogFileType::convertToLogfileTypeID($type);
	my @parsingRuleList;
	my $query = qq{
					select parsingRuleID,
							parsingRuleText
					from	ParsingRule
					where 	logfileTypeID = '$logfileTypeID' AND
						  	isEnabled = 1;
					};
					
	#print "QUERY: $query";
	$sth = DBLib::executeQuery($query);
	$sth->bind_col(1,\$id);
	$sth->bind_col(2,\$regex);
	while($sth->fetch()){
		$newRule = ParsingRule->new($id,$regex);
		push(@parsingRuleList,$newRule);
	} 
	 $sth->finish();
	return @parsingRuleList;	
}
 

sub getRegex{
	my $self = shift;
	return $self->{regex};
}

sub setRegex{
	my ($self,$newRegex) = @_;
	$self->{regex} = $newRegex;
	return $self->{regex}; 
}

sub getMessageType{
	my $self = shift;
	return $self->{logMessageType};
}
sub setMessageType{
	my ($self,$newType) = @_;
	$self->{logMessageType} = $newType;
	return $self->{logMessageType};
}

####test this
sub getComponentTypes{
	$self = shift;
	my $parsingRuleID = $self->{id};
	#debug("getParsingRuleComponentTypes(): for ruleID $parsingRuleID");

	my $query;
	$query = qq{
				select  j.componentTypeID,ct.componentTypeName
				from	ParsingRule_ComponentType j,
						ComponentType ct
				where	parsingRuleID = '$parsingRuleID'
				and		j.componentTypeID = ct.componentTypeID
				order by precedence;
				};
	$sth = DBLib::executeQuery($query);
	$sth->bind_col(1,\$compID);
	$sth->bind_col(2,\$compName);
	while($sth->fetch()){
		$newCType = ComponentType->new($compID,$compName);
		push(@parsComp,$newCType);
		
	}
    $sth->finish();
	return @parsComp;
}
################ test and verify relevance.
sub getLogMessageTypeID{
	
 
$parsingRuleID = shift; 
$query = qq{select logMessageTypeID
			from ParsingRule
			where parsingRuleID = '$parsingRuleID'
};
	$sth = DBLib::executeQuery($query);
	$sth->bind_col(1,\$logMessageTypeID);
	while($sth->fetch()){;}
	$sth->finish();
	return $logMessageTypeID;	
}

1;
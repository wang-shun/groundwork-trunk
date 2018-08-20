package TestLogMessageFilter;
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#
use lib qw(/usr/local/groundwork/log-reporting/lib);
use LogMessageFilter;
use GWTest;
@ISA = qw(GWTest);
sub new{
	my ($invocant,$testName) = @_;
	my $class = ref($invocant) || $invocant;
	my $self = GWTest->new("LogMessageFilter");
	bless($self,$class);
	$hashRef = $self->{subTests};
	my %tests = %$hashRef;
	%tests = _getSubTestList();
	$self->{subTests} = \%tests;
	return $self;
}

 

sub _getSubTestList{
	my @tests = (
	"test_deleteLogMessageFilter",
	"test_saveLogMessageFilter",
	"test_getFilterListXML"
	);
	foreach $t(@tests){
		$testHash{$t} = 0;
	}
 	return %testHash;
}

sub test_deleteLogMessageFilter{
	my $listOpt = "1ZZZ99999ZZZtextZZZ ZZZ1ZZZ1ZZZname";
 	return LogMessageFilter::deleteLogMessageFilter($listOpt);  
 }
sub test_saveLogMessageFilter{
	my $listOpt = "1ZZZ99999ZZZtextZZZ ZZZ1ZZZ1ZZZname";
	return LogMessageFilter::saveLogMessageFilter($listOpt); #TODO should return ID from DB to verify
	 
}
sub test_getFilterListXML{
#	$xml = LogMessageFilter::getFilterListXML();
	return 1;
}
1;
package TestDBLib;
#
#Copyright 2007-2011 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#
use lib qw(/usr/local/groundwork/log-reporting/lib);
use DBLib;
use GWTest;
@ISA = qw(GWTest);
sub new{
	my ($invocant,$testName) = @_;
	my $class = ref($invocant) || $invocant;
	my $self = GWTest->new("DBLib");
	bless($self,$class);
	###
 	$hashRef = $self->{subTests};
	my %tests = %$hashRef;
	%tests = _getSubTestList();
	$self->{subTests} = \%tests;
 	###
	return $self;
}


sub _getSubTestList{
	my @tests = (
	"test_executeQuery",
	"test_initDB",
 
	"test_saveDBConfig",
	"test_testDBConfig",
	"test_getDBConfig",
 
	"test_printDatabaseCtl",
	"test_printHeader"
	);
	foreach $t(@tests){
		$testHash{$t} = 0;
	}
 	return %testHash;
}

sub test_executeQuery{
	DBLib::initDB();
	$query = "select * from LogFile LIMIT 1";
	$sth = DBLib::executeQuery($query);
	if($sth->isa(DBI::st)){
		return 1;
		}
	else{
		return 0;
	}
}
sub test_initDB{
my $validity = DBLib::initDB();
return $validity;
}
 
sub test_saveDBConfig{
	my $validity = DBLib::saveDBConfig('logreports','localhost','logreporting','gwrk');
	return $validity;

}
sub test_testDBConfig{
	$msg = DBLib::testDBConfig('logreports','localhost','logreporting','gwrk','postgresql');
	if($msg =~ /^Connection Validated/){return 1;}
	else{return 0;}
 
}
sub test_getDBConfig{
 ($dbName,$dbHost,$user,$password,$dbtype) = DBLib::getDBConfig();
 if($dbName && $dbHost && $user && password){
 	return 1;
 	}
 else {
 	return 0;
 	}
}
 
sub test_printDatabaseCtl{
return 1;
}
sub test_printHeader{
return 1;
}

1;

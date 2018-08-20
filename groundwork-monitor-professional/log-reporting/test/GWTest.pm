#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#
package GWTest;

sub new{
	my ($invocant,$testName) = @_;
	my $class = ref($invocant) || $invocant;
	%sT = {};
	$sTRef = \%sT;
	my $self = {
		testName =>$testName,
		passCount=>0,
		failCount=>0,
		testCount=>0,
		subTests=>$sTRef
	};

	bless($self,$class);

	#DBLib::initDB(); 
	return $self;
}

 

sub run{
	my $self = shift;
	my $hashRef = $self->{subTests}; 
	$retval = 1;
	
	%allTests = %$hashRef;
 	foreach $subTest (keys %allTests){
 		# print "subTest: $subTest " . $self->$subTest . "\n";
 		$allTests{$subTest} = $self->$subTest;
 		#  print "subTest: $subTest " . $allTests{$subTest} . "\n";
 		
 	}
 	#print "subTest:   " . $allTests{'test_addType'} . "\n";
	$self->{subTests} = \%allTests;
	return 1;

}

1;
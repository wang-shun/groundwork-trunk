#
#Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")
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
		unimpCount=>0,
		testCount=>0,
		subTests=>$sTRef
	};

	bless($self,$class);
 	%tests = $self->_getSubTestList();
	$self->{subTests} = \%tests;
     for my $key ( keys %tests ) {
     	   $self->{testCount}++;
   
     }
	 
	$self{testCount} = $cnt; 
 	return $self;
}


sub  _getSubTestList{
	 @tests = ();
	 %testHash = ();
	$self = shift;
	$classname = ref $self;
	$pFile =  $classname . ".pm";
  	open(PKG,$pFile) || die "Couldn't open package: $pFile: $!";
	while(<PKG>){
		$line = $_;
		chomp($line);
		if($line =~ /^#/){next;}
 		if($line =~ /sub\s+test_/){
			$line =~ s/sub//;
			
			$line =~ s/\{//;
			$line =~ s/ //g;
			#$self->{testCount}++;
			push(@tests,$line);
		}
	}
	foreach $t(@tests){
		$testHash{$t} = 0;
		}
	$ts = @tests;
 	return %testHash;
	
}

sub run{
	my $self = shift;
	my $framework = shift;
	my $hashRef = $self->{subTests}; 
	$retval = 1;
	
	%allTests = %$hashRef;
 	foreach $subTest (keys %allTests){
 		$framework->{testCount}++;
 		$name = ref $self;
 		$subTest =~ s/\s//g;
 		if($subTest eq ""){next;}
 		 #print "GWTest:run():" . $name .  "::${subTest}\n";
 		#$subTest =~ s/\s+//g;
 		 
 		$testVal = $self->$subTest;
 		$allTests{$subTest} = $testVal;
 		if($testVal == 0){
 			$self->{failCount}++;
 			$framework->{failCount}++;
 		}
 		elsif($testVal ==1){
 			$self->{passCount}++;
 			$framework->{passCount}++;
 		}
 		elsif($testVal ==-1){
 			$self->{unimpCount}++;		
 			$framework->{unimpCount}++;	
 		}
 	}
	 $self->{subTests} =  \%allTests;
	return 1;
}

sub testGet{
	$retVal = 0;
	$errMessage = "";
	($self,$classname,$property) = @_;
	$testObj = $classname->new();
	$getMethod = "get_" . $property;
	$setMethod = "set_" . $property;
	
  		 
 	if(($testObj->$setMethod("testvalue") == $testObj->$getMethod()) && ($testObj->$getMethod() ne "")){
		$retVal = 1;
	}	
	 
 	return $retVal;
}


sub testSet{
	($self,@args) = @_;
	$valid = $self->testGet(@args);
	return $valid;
}


1;
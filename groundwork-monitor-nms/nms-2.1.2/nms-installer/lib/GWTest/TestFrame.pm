#!/usr/bin/perl;
#
#Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#
package TestFrame;
use TestLib;
use Term::ANSIColor qw(:constants);
sub new{
	my ($invocant,$name,$format,$verbose) = @_;
	my $class = ref($invocant) || $invocant;
	my @tC = ();
	my $testCollectionRef = \@tC;
	my $self = {
		name=> $name,
		testCollection=>$testCollectionRef,
		format=>$format,
		failures=>0,
		failCount=>0,
		unimpCount=>0,
		passCount=>0,
		testCount=>0,
		classCount=>0,
		verbose=>$verbose
	};
	bless($self,$class);
	return $self;
}

sub addTest{
	my $self = shift;
	my $newTest = shift;
	my $testC  = $self->{testCollection}; 
	my @testCollection = @$testC;
	push(@testCollection,$newTest);
	 
	$self->{testCollection} = \@testCollection; 

}

sub run{
	my $self = shift; 
	my $testCRef = $self->{testCollection}; 
	@testC = @$testCRef;
	if($self->{format} =~ /HTML/i){ 
	   print "<title>" . $self->{name} . " Unit Tests</title>\n";
	   print "<h2 align=center>" . $self->{name} . " Unit Tests</h2>\n";
	}
	elsif($self->{format} eq "term"){
	print BOLD . "-------------------------------------------------------------------------------------\n";
		
		print  BOLD . "$self->{name} Unit Tests\n". RESET;
		$tc = @testC;
 	}
 	$self->{testCount} = 0;
	$self->{classCount} = 0;
	foreach $t(@testC){
		#$test->run();
		$self->{classCount}++;
		$runTest = $t->new(); 
			 	 
		
		#print (ref $runTest) . "\nTest Count:$runTest->{testCount}";
	 	#print "\nTest Count:$runTest->{testCount}\n";
		$runTest->run($self);
		if($self->{'format'} eq "HTML"){
			$self->formatHTML($runTest);
		}
		elsif($self->{'format'} eq "term"){
			$self->formatTerm($runTest);
		}
		else{
			$self->formatBuild($runTest);
		}

	}
	
#Summary
	$self->printSummary();
 
}

sub printSummary{
	$self = shift;
	if($self->{format} eq "build"){
		if($self->{failCount}){
			print "FAIL: " . $self->{passCount} . "/$self->{testCount} passed; $self->{failCount} failed; $self->{unimpCount} unimplemented\n";
		}
		else{
			print "OK\n";
		}
	}
	elsif($self->{format} eq "term"){
		$break = formatColumn("",95,"-");
		print "$break\n$break\n" . BOLD . "Summary\n" . RESET . "$break\n" ;
		
		if($self->{unimpCount}){
				print BOLD WHITE ON_BLUE "  UNIMPL  " . RESET . "  $self->{passCount}/$self->{testCount} passed; $self->{failCount} failed; $self->{unimpCount} unimplemented\n";
			
		}

		elsif($self->{failCount}){
			
				print BLACK ON_RED "  FAIL  " . RESET . "  $self->{passCount}/$self->{testCount} passed; $self->{failCount} failed; $self->{unimpCount} unimplemented\n";
		}
		else{
			print BLACK ON_GREEN "   OK   "  . RESET . " $self->{passCount}/$self->{testCount} passed; $self->{failCount} failed; $self->{unimpCount} unimplemented\n";
			 
		}		
	}	
	
}


sub formatColumn{
	$text = shift;
	$maxLength = shift;
	$spacer = shift;
	# FORMAT COLUMN
	$nameLength = length($text);
	$spacesMissing = $maxLength - $nameLength;
	#print "Missing = $spacesMissing\n";

	while($spacesMissing > 0){
		$text .= $spacer;
		$spacesMissing--;
	}
	return $text;
	
}

#Print a one line summary appropriate for parsing by the build mechanism
sub formatBuild{

	$failures = 0;
	my $self = shift;
	$myTest = shift;
 	
	#print BOLD,"$myTest->{testName}", RESET, "\n";
	$hashRef = $myTest->{subTests};
	%allT = %$hashRef;
	foreach $st (keys %allT){
		$val = $allT{$st};
		 $st =~ s/^test_//;
	     $failures += $val;
	}
	 $self->{failures}+=$failures;	
}

#Print output appropriate for a terminal
sub formatTerm{
	my $self = shift;
	$myTest = shift;
	$mn = ref $myTest;
	$mn =~ s/^Test//;
	print BOLD . "-------------------------------------------------------------------------------------\n";


# FORMAT COLUMN
 
	$mn = formatColumn($mn,20," ");
	print BOLD,"$mn", RESET;


# APPEND ZERO to #s < 10
if($myTest->{testCount} < 10){
	$myTest->{testCount} = " " . $myTest->{testCount};
	
}
if($myTest->{passCount} < 10){
	$myTest->{passCount} = " " . $myTest->{passCount};
	
}
if($myTest->{unimpCount} < 10){
	$myTest->{unimpCount} = " " . $myTest->{unimpCount};
	
}

if($self->{verbose}){ 
	print "\n";
	$hashRef = $myTest->{subTests};
	%allT = %$hashRef;
	foreach $st (keys %allT){
		$val = $allT{$st};
		chomp($st);
		#$st =~ s/\s+//g;
		if($st =~ /\s/){ next;}
	 	$st =~ s/^test_//;
	 	$st = formatColumn("$st...",30,".");
		print "  $st";
		if($val == 1){TestLib::ok();}
		elsif($val == 0){TestLib::fail();}
		elsif($val == -1){TestLib::unimplemented();}
		#($val?TestLib::ok():TestLib::fail());
	 
	}
	print "\n";
}
else{print "\t";}
	print BOLD . "Tests:" . RESET . $myTest->{testCount} . "     ";
	print BOLD . " Pass:" . RESET . $myTest->{passCount} . "     ";
	print  BOLD . " Fail:" .   RESET .$myTest->{failCount} . "     ";
	print  BOLD .  "Unimpl:" . RESET .  $myTest->{unimpCount} .  "    ";
	
	if($myTest->{unimpCount}> 0){
		$unimp = " UNIMPL ";
		print BOLD "Verdict:" . WHITE ON_BLUE . $unimp . RESET . "\n";
	}
	elsif($myTest->{failCount} > 0){ 
	$fail = "  FAIL  ";
	print  BOLD . "Verdict:" . RESET . BLACK ON_RED. $fail  . RESET . "\n";
	
	}
	else{
	$ok = "   OK   ";

	print BOLD . "Verdict:" . RESET . BLACK ON_GREEN . $ok  .   RESET . "\n";	
	}
}
	
#Print HTML output for display in a browser.
sub formatHTML{
	my $self = shift;
	$myTest = shift;
	$mn = ref $myTest;
	$mn =~ s/^Test//;
	#print "Content-type: text/html\n\n";
    print "<table cellpadding=3 border=0>\n";
    print "<tr><th colspan=2  bgcolor=AAAAAA>$mn</th></tr>\n";
	$hashRef = $myTest->{subTests};
	%allT = %$hashRef;
	foreach $st (keys %allT){
		$val = $allT{$st};
		 $st =~ s/^test_//;
		print "<tr><td width=300>$st</td>";
		($val?($color="00FF00"):($color="FF0000"));
		($val?($text = "OK"):($text = "FAIL"));

		print "<td bgcolor=$color align=center> $text </td></tr>\n";
		#($val?TestLib::ok():TestLib::fail());
	
	}
	print "</table>\n";
}


1;
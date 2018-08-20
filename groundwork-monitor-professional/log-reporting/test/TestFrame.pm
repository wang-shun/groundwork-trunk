#!/usr/bin/perl;
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#
package TestFrame;
use TestLib;
use Term::ANSIColor qw(:constants);
sub new{
	my ($invocant,$name) = @_;
	my $class = ref($invocant) || $invocant;
	my @tC = ();
	my $testCollectionRef = \@tC;
	my $self = {
		name=> $name,
		testCollection=>$testCollectionRef
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
	   print "<title>" . $self->{name} . " Unit Tests</title>\n";
	   print "<h2 align=center>" . $self->{name} . " Unit Tests</h2>\n";
	foreach $t(@testC){
		#$test->run();
		$runTest = $t->new(); 
	 	
		$runTest->run();
	 	 #$self->format($runTest);
		  $self->formatHTML($runTest);
	}
}
sub format{
	my $self = shift;
	$myTest = shift;
	print BOLD,"$myTest->{testName}", RESET, "\n";
	$hashRef = $myTest->{subTests};
	%allT = %$hashRef;
	foreach $st (keys %allT){
		$val = $allT{$st};
		 $st =~ s/^test_//;
		print "  $st...";
		($val?TestLib::ok():TestLib::fail());
	
	}
}
	
sub formatHTML{
	my $self = shift;
	$myTest = shift;
 
    print "<table cellpadding=3 border=0>\n";
    print "<tr><th colspan=2  bgcolor=AAAAAA>$myTest->{testName}</th></tr>\n";
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
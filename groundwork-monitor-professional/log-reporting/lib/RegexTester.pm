package RegexTester;
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

sub test{
 $regex = shift;
 #$regex = qr/$regex/;
 $testString = shift;
 my @matches = ();
 @matches = ($testString =~ m/$regex/); 
 return @matches;
}
 
1;
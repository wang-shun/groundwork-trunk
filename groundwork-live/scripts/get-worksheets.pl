#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use Spreadsheet::ParseExcel;

my $oExcel = new Spreadsheet::ParseExcel;

die "You must provide a filename to $0 to be parsed as an Excel file" unless @ARGV;
my $path_file = $ARGV[1]. $ARGV[0];
print $path_file. "\n";
my $oBook = $oExcel->Parse($path_file);
my($iR, $iC, $oWkS, $oWkC);
print "FILE  :", $oBook->{File} , "\n";
print "COUNT :", $oBook->{SheetCount} , "\n";

print "AUTHOR:", $oBook->{Author} , "\n"
 if defined $oBook->{Author};

for(my $iSheet=0; $iSheet < $oBook->{SheetCount} ; $iSheet++)
{
	$oWkS = $oBook->{Worksheet}[$iSheet];
	open (OUT, '>', $ARGV[1]. $oWkS->{Name});
	print $ARGV[1]. $oWkS->{Name}. "\n";
	print $oWkS->{Name}. "\n";
	for(my $iR = $oWkS->{MinRow} ;
		defined $oWkS->{MaxRow} && $iR <= $oWkS->{MaxRow} ;
		$iR++)
	{
		for(my $iC = $oWkS->{MinCol} ;
			defined $oWkS->{MaxCol} && $iC <= $oWkS->{MaxCol} ;
			$iC++)
		{
			$oWkC = $oWkS->{Cells}[$iR][$iC];
			print OUT $oWkC->Value. "\t" if($oWkC);
		}
		print OUT "\n";
	}
	close OUT;
}

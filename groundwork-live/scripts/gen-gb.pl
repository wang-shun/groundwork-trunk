#!/usr/local/groundwork/perl/bin/perl -w
#
#
# 2012 GroundWork Live
# reads a file, looking for a particular string in a line
# until the string line each input line is written to an output file
# on finding the string that line is written
# then a second file is opened to take in a series of HostGroup names
# each one is used in fabricating a section with the HostGroup name 
# at the end of the list the remaining lines of the first file are read and copied
# Path passed in applies to the snippet files

my $bodyfile = $ARGV[0];
my $hostgfile = $ARGV[1];
my $newfile = $ARGV[2];
my $snippetfile = $ARGV[3];
my $path = $ARGV[4];
my $trigger = $ARGV[5];

my ($line, $hg, $cruft, $snippet);

open input, '<', $bodyfile;
open HG, '<', $path . $hostgfile;
open OUTPUT, '>', $newfile;

while (<input>) {
	$_ =~ s/\r//;
	print OUTPUT $_;
	if ($_ =~ /$trigger/) {
		while ($line = <HG>) {
			($hg,$cruft) = split ('\t',$line);
			if ($hg =~ /^#/) {next;}
#			$hg =~ s/[\s,\r]//g;
			open SNIPPET, '<', $path . $snippetfile;
			while ($snippet = <SNIPPET>) {
				$snippet =~ s/\r//;
				$snippet =~ s/GGGG/$hg/g;
				print OUTPUT $snippet;
			}
			close SNIPPET;
		}
	}
}
close HG;
close input;
close OUTPUT;
exit;

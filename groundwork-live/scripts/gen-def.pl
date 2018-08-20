#!/usr/local/groundwork/perl/bin/perl -w
#
#
# 2012 GroundWork Live
# reads a file, looking for a particular string in a line
# until the string line each input line is written to an output file
# on finding the string that line is written
# the set file indicates triplets of HG, First snippet and core snippet

# the HG file has a list of hostgroup names
# the first and core snippets are applied with the first HG line substituted
# the 2nd - n HG lines are applied with the core snippet
# when the set file is exhausted the rest of the input is written out
# gen-def.pl OriginalFile SetsOfInputFile OutputFile
# SetsOfInputFile format:  HG FirstSnippetFile CoreSnippetFle

# I  H1.1F H1.1C H1.2C H1.3C... H2.1F H2.1C H2.2C H2.3C...  HN  I

my $bodyfile = $ARGV[0];
my $setfile = $ARGV[1];
my $outfile = $ARGV[2];
my $path = $ARGV[3];
my $trigger = $ARGV[4];

my ($infile, $line, $hgfile, $cruft, $snippet, $firstpart, $corepart);
my $group_hash = {};

open SET, '<', $path . $setfile;
open input, '<', $bodyfile;
open OUTPUT, '>', $outfile;

while ( $line = <SET> ) {
# content hostgroup \t firstpart \t corepart
	chomp $line;
	if ($line =~ /^#/ ) {next;}
	($hgfile, $firstpart, $corepart) = split( /\t/, $line );
	$group_hash->{$hgfile}->{firstpart} = $firstpart;
	$group_hash->{$hgfile}->{corepart} = $corepart;
}
close SET;

while ($infile = <input>) {
	$infile =~ s/\r//;
	print OUTPUT $infile;
	if ($infile =~ /$trigger/) {
		foreach $hgfile ( keys %{$group_hash} ) {
			open HG, '<', $path . $hgfile;
			$hgname = <HG>;
			$hgname =~ s/\s.*//;
			if ($hgname =~ /^#/) {next;}
			chomp $hgname;
			open SNIPPET, '<', $path . $group_hash->{$hgfile}->{firstpart};
			while ($snippet = <SNIPPET>) {
				$snippet =~ s/\r//;
				$snippet =~ s/GGGG/$hgname/g;
				print OUTPUT $snippet;
			}
			close SNIPPET;
			open SNIPPET, '<', $path . $group_hash->{$hgfile}->{corepart};
			while ($snippet = <SNIPPET>) {
				$snippet =~ s/\r//;
				$snippet =~ s/GGGG/$hgname/g;
				print OUTPUT $snippet;
			}
			close SNIPPET;
			while ($hgname = <HG>) {
				$hgname =~ s/\s.*//;
				chomp $hgname;
				if ($hgname =~ /^#/) {next;}
				open SNIPPET, '<', $path . $group_hash->{$hgfile}->{corepart};
				while ($snippet = <SNIPPET>) {
					$snippet =~ s/\r//;
					$snippet =~ s/GGGG/$hgname/g;
					print OUTPUT $snippet;
				}
				close SNIPPET;
			}
			print OUTPUT "\t</page>\n";
			close HG;
		}
	}
}
close input;
close OUTPUT;
exit;

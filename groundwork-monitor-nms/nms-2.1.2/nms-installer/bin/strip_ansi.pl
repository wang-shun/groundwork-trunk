#!/usr/bin/perl

# strips ansi escape sequences from stream
# TODO: add additional color codes as they come up

while(<STDIN>){
	$line = $_;	
        $line =~ s/\e\[0m \]//g;
	$line =~ s/\e\[0m//g;
	$line =~ s/\e\[61G\[//g;
	$line =~ s/\e\[1\;37\;32m//g;
	$line =~ s/\e\[63G//g;
	$line =~ s/\e\[1\;37\;31m//g;
	print $line;
}

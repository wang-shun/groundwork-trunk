#!/usr/bin/perl

# Convert all the files under the specified directory to use UNIX line endings.

my $path_dir = $ARGV[0];

# FIX MINOR:  The code here presumes we were handed a directory,
# but in actual use we are sometimes handed a file instead.
# Deal with that situation as well.

# FIX LATER:  Support specifying more than one path on the command line.

# FIX LATER:  We could probably have used "find $path_dir -type d -print"
# instead of du, with less effort.
my $ls_lR_output = `/usr/bin/du $path_dir`;
my @lines = split( /\n/, $ls_lR_output );

foreach my $line (@lines) {
    $line =~ m{^\d+\s+(\S+)$};
    my $DIR = $1;
    if ( $DIR !~ m{(^|/)\.svn(/|$)} ) {
	my $ls_sub_dir = `ls $DIR`;
	my @cleanlines = split( /\n/, $ls_sub_dir );
	foreach my $cleanline (@cleanlines) {
	    ## Symlinks look like files if we don't look deeper, but
	    ## we don't want to convert them, as that would change
	    ## them from symlinks to actual distinct output files.
	    if ( -f "$DIR/$cleanline" and !-l "$DIR/$cleanline" ) {
		`/usr/bin/dos2unix $DIR/$cleanline`;
	    }
	}
    }
}

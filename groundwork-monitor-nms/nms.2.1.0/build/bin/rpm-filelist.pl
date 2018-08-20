#!/usr/bin/perl
# Create filelist for rpm package

my $ls_lR_output = `ls -alLR .`;
my @lines = split(/[\n\r]+/, $ls_lR_output);
my $homedir = `pwd`;
chop $homedir;

my $dir_show = '';
my $dir_stat = './';
foreach my $line (@lines) {
  if ($line =~ m{^\./(.*):$}) {
    $dir_show = $dir_stat = $1 . '/';
  }
  elsif ($line =~ m{^             # beginning of string
            ([-d])        # match and capture - or d as $1
            (\S+)\s+      # non-spaces bounded by spaces, capture as $2
            \d+\s+        # digits bounded by spaces
            (\S+)\s+      # userid (capture as $3)
            (\S+)\s+      # group id (capture as $4)
            \d+\s+        # file size
            (?:\S+\s+){1,3} # date/time, one to three strings
            (\S+)         # filename (capture as $6)
            $}x) {        # end of string

    my $filename = $5;
    my $mode = sprintf("%04o", ((stat("$dir_stat$filename"))[2] & 07777));

    if ($1 eq 'd') {
      if (($5 ne '.') && ($5 ne '..')) {
    print "\%attr($mode,$3,$4) \%dir $homedir/$dir_show$5\n";
      }
    }
    else {
      print "\%attr($mode,$3,$4) $homedir/$dir_show$filename\n";
    }
  }
}


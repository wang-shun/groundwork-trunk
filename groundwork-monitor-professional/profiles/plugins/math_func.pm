#!/usr/local/groundwork/perl/bin/perl -w

package math_func;

use strict;
use Exporter;

our @ISA = qw(Exporter);
our @EXPORT = qw(sum avg stdev);


sub sum {
   my $arrayref = shift;
   return undef unless scalar @{$arrayref};
   my $sum = 0;
   $sum += $_ foreach @{$arrayref};
   return $sum;
}


sub avg {
   my $arrayref = shift;
   return undef unless scalar @{$arrayref};
   my $sum = sum($arrayref);
   return ( $sum / scalar @{$arrayref} );
}


sub stdev {
   my $arrayref = shift;
   my $arraysize = scalar @{$arrayref};
   if ($arraysize == 0) {
      return undef;
   }
   elsif ($arraysize == 1) {
      return 0;
   }
   my $avg = avg($arrayref);
   my $variance = 0;
   for (my $i=0; $i<$arraysize; $i++) {
      $variance += (($$arrayref[$i] - $avg) ** 2);
   }
   return sprintf("%d", sqrt($variance / $arraysize));
}

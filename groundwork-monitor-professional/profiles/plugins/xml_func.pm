#!/usr/local/groundwork/perl/bin/perl -w

package xml_func;

use strict;


sub recursive_xml {
   my $str = shift;
   my $tmp = {};
   my $hash = {};
   while ( $str =~ /<(\w+)(.*?)>(.*?)<\/\1>/g ) {
      my ($tag, $attr, $data) = ($1, $2, $3);
      if ($attr =~ /name="(\w+)"/) {
         $tag .= "-$1";
      }
      push @{ $tmp->{ $tag }}, $data;
   }

   foreach my $tag (sort keys %$tmp) {
      if (@{ $tmp->{ $tag } } > 1) {
         foreach my $data ( @{ $tmp->{ $tag } } ) {
            push @{ $hash->{ $tag } }, recursive_xml( $data );
         }
      }
      else {
         my $data = shift @{ $tmp->{ $tag } };
         $hash->{ $tag } = recursive_xml( $data );
      }
   }

   return keys %$hash ? $hash : $str;
}


return 1;

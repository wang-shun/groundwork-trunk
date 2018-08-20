#!/usr/local/groundwork/perl/bin/perl -w

package json_func;

use MIME::Base64;

use strict;
use Exporter;

our @ISA = qw(Exporter);
our @EXPORT = qw(json_decode json_encode);


sub json_decode {
   my $str = shift;
   our %hash = ();

   my $label = ();
   while ($str =~ /^{(?:"([^"]+)"(?{ $label = $^N }):(?:\[(?:(\d+)(?{ push @{$hash{$label}}, $^N }),?|"([^"]*)"(?{ push @{$hash{$label}}, decode_base64($^N) }),?)+\]|(?:(\d+)(?{ $hash{$label} = $^N })|"([^"]*)"(?{ $hash{$label} = decode_base64($^N) }))),?)+}/g) {
      $label = undef;
   }
   return \%hash;
}

sub json_encode {
   my $hash = shift;
   my @array = ();
   foreach my $key (keys %{$hash}) {
      unless (ref $$hash{$key}) {
         if ($$hash{$key} =~ /^[0-9]+$/) {
            push @array, "\"$key\":$$hash{$key}";
         }
         else {
            push @array, "\"$key\":\"@{[encode_base64($$hash{$key}, '')]}\"";
         }
      }
      elsif (ref $$hash{$key} eq 'ARRAY') {
         my @tmp = ();
	 foreach my $arr (@{$$hash{$key}}) {
	    push @tmp, '"' . encode_base64($arr, '') . '"';
	 }
	 my $tmps = join ',', @tmp;
         push @array, "\"$key\":[$tmps]";
      }
      elsif (ref $$hash{$key} eq 'HASH') {
         my @tmp = ();
	 foreach my $k (keys %{$$hash{$key}}) {
	    push @tmp, "\"$k\":\"@{[encode_base64($$hash{$key}{$k}, '')]}\"";
	 }
	 my $tmps = join ',', @tmp;
	 push @array, "\"$key\":{$tmps}";
      }
   }
   my $str = '{' . join(',', @array) . '}';
   return $str;
}


return 1;

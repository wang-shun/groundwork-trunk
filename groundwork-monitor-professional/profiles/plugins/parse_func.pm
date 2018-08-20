#!/usr/local/groundwork/perl/bin/perl -w

package parse_func;

use strict;
use Exporter;

our @ISA = qw(Exporter);
our @EXPORT = qw(parse_args parse_levels);


sub parse_args {
   my %args = ();
   @_ = map { /^-/ ? $_ : "'$_'" } @_;
   my $arguments = "@_";
   while ($arguments =~ /-{1,2}([a-zA-Z0-9]+) ?(?:'([^ '-].*?)?')?(?= -|$)/g) {
      #(my $val = $2) =~ tr/'//d;
      push @{$args{$1}}, $2;
   }

   foreach my $key (keys %args) {
      my %seen = ();
      my $x = $args{$key};
      for (my $i=0; $i<scalar @$x; $i++) {
         if (exists $$x[$i]) {
	    if (! defined $$x[$i]) {
	       splice @$x, $i, 1;
	       redo;
	    }
            $seen{$$x[$i]}++;
	    if ($seen{$$x[$i]} > 1) {
	       splice @$x, $i, 1;
	       redo;
            }
         }
      }
      my $keys = scalar @{$args{$key}};
      if ($keys == 0) {
         $args{$key} = undef;
      }
      elsif ($keys == 1) {
         $args{$key} = $args{$key}[0];
      }
   }
   return %args;
}


sub parse_levels {
   my $func = shift;
   my %funcs = ( 'cpu'     => [85, 95],
                 'cputemp' => [65, 70],
		 'conns'   => [80, 90],
		 'disk'    => [85, 95],
                 'load'    => [5.0, 3.5, 2.0],
                 'mem'     => [80, 90],
		 'proc'    => [0, 2],
		 'uptime'  => [999, 1999],
               );

   my @levels;
   if (defined $main::args{l}) {
      @levels = split(/:/, $main::args{l});
   }
   foreach (@levels) {
      unless (/^(-?(?:\d+|\d+\.\d+|\.\d+))$/) {
         print "ERROR - Level '$_' is non-numeric [levels=$main::args{l}]";
         exit 2;
      }
   }
   if ($#levels == -1) {
      # levels not defined; using defaults
      if (defined $func && exists $funcs{$func}) {
         @levels = @{$funcs{$func}};
      }
      else {
         @levels = (85, 95);
      }
   }
   return @levels;
}


sub new {
   my ($class, $argv, $options) = @_;
   my $args = {};
   my $self = {};

   for (my $i=0; $i<=$#$argv; $i++) {
      my $dashes = () = $argv->[$i] =~ m/\G(-)/g;
      my $dashes_next = () = $argv->[$i+1] =~ m/\G(-)/g if defined $argv->[$i+1];
      if ($dashes == 1) {
         pos($argv->[$i]) = 1;
         my @chars = $argv->[$i] =~ m/\G([a-zA-Z0-9])/g;
         for (my $j=0; $j<=$#chars; $j++) {
            my ($key, $value) = ($j, $chars[$j]);
            if ($key != $#chars || $dashes_next) {
               push @{ $args->{$value} }, undef;
            }
            else {
               push @{ $args->{$value} }, $argv->[$i+1];
               $i++;
            }
         }
      }
      elsif ($dashes == 2) {
         my ($value) = $argv->[$i] =~ /^--([a-zA-Z0-9_-]+)/;
         if ($dashes_next) {
            push @{ $args->{$value} }, undef;
         }
         else {
            push @{ $args->{$value} }, $argv->[$i+1];
            $i++;
         }
      }
      else {
         push @{ $args->{ARGV} }, $argv->[$i];
      }
   }

   foreach my $key (keys %$args) {
      next if $#{ $args->{$key} };
      $args->{$key} = shift @{ $args->{$key} };
   }

   # ? = optional

   if (ref($options) eq 'HASH') {
      foreach my $option (sort keys %$options) {
         if (defined $args->{$option}) {
            $self->{$option} = $args->{$option};
         }
         elsif ($option =~ /^(\S+)\?$/) {
            $self->{$1} = $args->{$1} if defined $args->{$1};
         }
         elsif ($option =~ /^(\S+)\!$/) {
            $self->{$1} = $args->{$1} if exists $args->{$1};
         }
         else {
	    (my $program = $0) =~ s{.*/}{};
            print "$program:  The '-$option' option is missing or incorrect.\n\n";
            print "Valid options are (? == optional, ! == boolean):\n";
            foreach my $opt (sort keys %$options) {
               if (ref($options->{$opt}) eq 'HASH') {
                  my ($key1, $value1) = each %{$options->{$opt}};
                  printf "           -%-2s => %s\n", $opt, $key1;
                  foreach my $type (sort keys %$value1) {
                     printf "                  -> %s\n", $type;
                  }
               }
               else {
                  printf "           -%-2s => %s\n", $opt, $options->{$opt};
               }
            }
            exit 2;
         }
      }
   }
   else {
      $self = $args;
   }

   bless($self, $class);
   return $self;
}


return 1;

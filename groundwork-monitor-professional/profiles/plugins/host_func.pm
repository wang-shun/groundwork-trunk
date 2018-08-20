#!/usr/local/groundwork/perl/bin/perl -w

package host_func;

use strict;

use constant BACKUP_USER => 'socbackup';
use constant BACKUP_PASS => 'Suu5Jan0hApJXzI2WYNb5g==';
use constant DECRYPT     => '/home/nagios/scripts/decrypt_string.pl';
use constant HOSTS       => '/var/tmp/hosts.txt';


sub new {
   my ($class, $host) = @_;
   my $self = {};
   bless $self, $class;
   if ( defined $host ) {
      return $self->retrieve_host($host) ? $self : undef;
   }
   else {
      return $self->retrieve_all_hosts() ? $self : undef;
   }
}


sub bless_child {
   my $self = shift;
   my $child = shift or return undef;
   return undef unless defined $self->{$child};
   bless my $host = $self->{$child}, __PACKAGE__;
   return $host;
}


sub retrieve_host {
   my $self = shift;
   my $host = shift or do {
      $@ = "host_func: no hostname to lookup";
      return 0;
   };
   
   open my $fh, '<', HOSTS or die "Can't open @{[HOSTS]}";
   chomp(my @headers = split /;/, <$fh>, -1) or 
      die "Can't read @{[HOSTS]}";

   while (<$fh>) {
      chomp;
      my @fields = split /;/, $_, -1;
      next unless grep { lc($host) eq lc($fields[$_]) } 0 .. 2;
      @$self{@headers} = @fields;
      last;
   }

   return (scalar keys %$self ? 1 : 0);
}


sub retrieve_all_hosts {
   my $self = shift;

   open my $fh, '<', HOSTS or die "Can't open @{[HOSTS]}";
   chomp(my @headers = split /;/, <$fh>, -1) or 
      die "Can't read @{[HOSTS]}";

   while (<$fh>) {
      chomp;
      my @fields = split /;/, $_, -1;
      my %tmp = ();
      @tmp{@headers} = @fields;
      $self->{ $tmp{name} } = \%tmp;
   }

   return (scalar keys %$self ? 1 : 0);
}


sub get {
   my ($self, $type) = @_;

   if (! exists $self->{$type}) {
      return undef;
   } 
   elsif ($type eq 'backup_user') {
      return $self->{$type} || BACKUP_USER;
   }
   elsif ($type eq 'backup_pass') {
      return $self->{$type} || BACKUP_PASS;
   }
   else {
      return $self->{$type};
   }
}


sub decrypt {
   my $self = shift;
   my $field = shift or do {
      $@ = "host_func: decrypt() missing field";
      return undef;
   };
  
   unless (-f DECRYPT) {
      $@ = "host_func: decrypt() permission denied";
      return undef;
   }
    
   local @ARGV = ( $self->get($field) );
   return do "@{[DECRYPT]}";
}


return 1;

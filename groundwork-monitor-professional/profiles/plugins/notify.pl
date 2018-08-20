#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use host_func ();
use notify_func ();
use parse_func ();

use constant PLUGIN => q(notify);
use constant RULES  => q(/usr/local/groundwork/nagios/libexec/notify.rules);

my $args = parse_func->new(\@ARGV);
my $host = host_func->new( $args->{HOSTNAME} );

# load rules from external file
my $rules = [ do { local (@ARGV,$/) = RULES; eval <> } ];

# verify that rules were parsed
if (@$rules == 0) {
   printf "Error parsing %s\n", RULES;
   exit 1;
}

# match against rulebase to determine notification action
my $functions = do {
   # instantiate temporary hash
   my $tmp = {};

   # loop through each rule
   foreach my $rule (@$rules) {
      # instantiate local variables
      my $op        = $rule->{operator} eq 'AND' ? 1 : 0;
      my $match     = $rule->{match};
      my $functions = $rule->{functions};
      my $arguments = $rule->{arguments};
      my $matches = 0;

      # loop through each match subroutine
      foreach my $sub (@$match) {
         &$sub and ++$matches and $op == 0 and last;
      }

      # check for rule match or goto next rule
      $op == 1 and $matches != @$match and next;
      $op == 0 and $matches == 0 and next;

      # rule matched; add to $tmp array
      foreach my $i (0 .. $#$functions) {
         my $function  = $functions->[$i];
         my $arguments = $arguments->[$i];
         $tmp->{$function}->{function} ||= $function;
         push @{ $tmp->{$function}->{arguments} }, @$arguments;
      }

      # leave rule checking engine if last value set
      $rule->{last} and last;
   }

   # return temporary hash value
   $tmp;
};

# loop through all actions and execute them
foreach my $func (keys %$functions) {
   my $function  = $functions->{$func}->{function};
   my $arguments = $functions->{$func}->{arguments};
   &$function( $args, $arguments );
}

print "done.";
exit 0;

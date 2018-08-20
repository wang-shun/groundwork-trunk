#!/usr/local/groundwork/perl/bin/perl -w

package fw_func;

use strict;
use Exporter;

use lib q(/usr/local/groundwork/nagios/libexec);
use cache_func;
use math_func;
use parse_func;


our @ISA = qw(Exporter);
our @EXPORT = qw(checkpoint_fw1_drops checkpoint_fw1_state_table_size);


sub checkpoint_fw1_drops {
   my @order  = qw(accepts drops rejects logs);
   my @levels = qw(-2:-3   2:3   2:3     -2:-3);
   my $keep   = 8640;
   my (@OUTPUT, @PERFDATA) = ();

   my $curtime = time();
   my $window = 1800;
   my @windows = map { [ $curtime - $window - 86400 * $_,
                         $curtime + $window - 86400 * $_ ]} 1 .. 30;

   my $store = cache_get('fw1drops-store') || {};
   my $last  = cache_get('fw1drops-last') || [];

   my @timestamps = ();
   foreach my $timestamp (sort { $a <=> $b } keys %{$store}) {
      grep { $timestamp >= $_->[0] && $timestamp <= $_->[1] } @windows or next;
      push @timestamps, $timestamp;
   }

   for my $i (0 .. $#order) {
      my $key = $order[$i];
      my $value = sprintf "%u", $_[$i] if defined $_[$i];
     
      unless (defined $value) {
         push @PERFDATA, "$key=U";
         push @OUTPUT, "UNKNOWN - $key returned undefined value";
         next;
      } 

      push @PERFDATA, "$key=$value";

      if (defined $last->[0] && ($curtime - $last->[0]) > 450) {
         # the last check was > 450 seconds ago; diff no longer valid
         push @OUTPUT, "OK - $key returned raw value $value";
         $last->[$i+1] = $value;
         next;
      }
      elsif (! defined $last->[$i+1]) {
         # the last check returned undefined
         push @OUTPUT, "OK - $key returned raw value $value";
         $last->[$i+1] = $value;
         next;
      }

      my $diff = do {
         if ($value < $last->[$i+1]) {
            $last->[$i+1] > 3221225472 ? 2**32 - $last->[$i+1] + $value : $value;
         }
         else {
            $value - $last->[$i+1];
         }
      };

      $last->[$i+1] = $value;

      my @array = map { defined $store->{ $_ }->{ $key } ? 
                        $store->{ $_ }->{ $key } : () } @timestamps;
      if (scalar @array) {
         my ($WARN, $CRIT) = split /:/, $levels[$i];
         my $avg = int(avg(\@array));
         my $stdev = stdev(\@array);
         if ($stdev > 0 && $diff > $avg + $stdev * $CRIT) {
            push @OUTPUT, "CRITICAL - $key over $CRIT standard deviations " .
                          "from average [raw=$diff avg=$avg stdev=$stdev]";
         }
         elsif ($stdev > 0 && $diff > $avg + $stdev * $WARN) {
            push @OUTPUT, "WARNING - $key over $WARN standard deviations " .
                          "from average [raw=$diff avg=$avg stdev=$stdev]";
         }
         else {
            push @OUTPUT, "OK - $key [raw=$diff avg=$avg stdev=$stdev]";
         }
      } 
      else {
         push @OUTPUT, "OK - $key [raw=$diff]";
      }

      $store->{ $curtime }->{ $key } = $diff;
   }

   if ( (my $rm = scalar(keys %{$store}) - $keep) > 0 ) {
      my @torm = (sort { $a <=> $b } keys %{$store})[0 .. $rm-1];
      delete $store->{$_} foreach @torm;
   } 
   
   $last->[0] = $curtime; 
   cache_set('fw1drops-last', $last); 
   cache_set('fw1drops-store', $store);
   
   return "@PERFDATA", @OUTPUT;
}


sub checkpoint_fw1_state_table_size {
   my $STATEFILE = '/usr/local/groundwork/stats/fw1-state-table-limit.data';
   my $FIREWALL = $_[0];
   my $NEWLIMIT = $_[1];
   my %STATE = ();

   die "You must provide a firewall name as argument 1 to function " .
       (caller(0))[3] unless defined $FIREWALL;

   if (-f "$STATEFILE") {
      open my $FH, "< $STATEFILE";
      while (<$FH> =~ /^(\S+) (\d+)/) {
         $STATE{$1} = $2;
      }
      close $FH;
   }

   if (defined $NEWLIMIT) {
      $STATE{$FIREWALL} = $NEWLIMIT;
      open my $FH, "> $STATEFILE";
      foreach my $fw (keys %STATE) {
         print $FH "$fw $STATE{$fw}\n";
      }
      close $FH;
   }

   return $STATE{$FIREWALL} if defined $STATE{$FIREWALL};
   return undef;
}


return 1;

#!/usr/local/groundwork/perl/bin/perl -w

package nagios_func;

use strict;
use IO::File ();

use lib q(/usr/local/groundwork/nagios/libexec);
use cache_func ();

use constant NAGIOSCMD => q(/usr/local/groundwork/nagios/var/spool/nagios.cmd);


sub new {
   my ($class, $args) = @_;
   my $self = {};
   $args or $class->die3("nagios_func->new missing args");
   $self->{args} = $args;
   $self->{pipe} = IO::File->new( NAGIOSCMD, 'w' ) or
      $class->die3("nagios_func->new failed to open command pipe");
   bless $self, $class;
   return $self;
}


sub die3 {
   my ($self, $msg) = @_;
   chomp($msg);
   print "ERROR - $msg\n";
   exit 3;
}


sub passive_svc_submit {
   my $self = shift;
   my $args = shift;

   $args->{time}     = time();
   $args->{command}  = q(PROCESS_SERVICE_CHECK_RESULT);
   $args->{host}     = $self->{args}->{h} or
      $self->die3("passive_svc_submit: missing host");

   defined $args->{service} or $self->die3("passive_svc_submit: missing service");
   defined $args->{status}  or $self->die3("passive_svc_submit: missing status");
   defined $args->{output}  or $self->die3("passive_svc_submit: missing output");

   my $msg = "[$args->{time}] " . 
      join(';', @$args{ qw(command host service status output) }) . "\n";
   my $bytes = syswrite( $self->{pipe}, $msg );
}


sub interface_stats_passive {
   my ($self, $int) = @_;

   my @perfdata = ( "in_bytes=$int->{int_in_oct}", 
                    "out_bytes=$int->{int_out_oct}",
                    "int_speed=0",
                  );

   my $output = "OK - $int->{int_name} interface throughput statistics " .
                "collected|@perfdata\n" .
                "in bytes:  $int->{int_in_oct}\\n" .
                "out bytes: $int->{int_out_oct}\\n";

   $self->passive_svc_submit({ 
      service => &main::PLUGIN . '_interface_stats_' . $int->{int_name},
      status  => 0,
      output  => $output
   });
}


sub interface_problems_passive {
   my ($self, $int) = @_;
   my @output = ();
   my $threshold = 0.001;   # 0.1%
   my @order = qw(int_in_pkt  int_in_err  int_in_drp 
                  int_out_pkt int_out_err int_out_drp);

   my $name = $int->{int_name};
   my $cache = cache_func->new( $self->{args}->{h} );
   my $last = $cache->get("$name-last");
   $cache->set("$name-last", [ @$int{@order} ]);

   my @perfdata = ( "in_errors=$int->{int_in_err}", 
                    "in_drops=$int->{int_in_drp}",
                    "out_errors=$int->{int_out_err}",
                    "out_drops=$int->{int_out_drp}",
                  );

   if (!defined $last || scalar(@$last) != 6) {
      my $msg = "OK - $name interface problem statistics collected|@perfdata";
      $self->passive_svc_submit({
         service => &main::PLUGIN . '_interface_problems_' . $name,
         status  => 0,
         output  => $msg,
      });
      return;
   }

   my $tmp = {};

   for my $i (0..5) {
      if ($int->{ $order[$i] } ne 'U' && $last->[0] ne 'U') {
         $tmp->{ $order[$i] } = $int->{ $order[$i] } - $last->[$i];
      }
      else {
         $tmp->{ $order[$i] } = 0;
      }
   }

   my $in_errors = $tmp->{int_in_pkt} ? 
      sprintf("%.5f", ($tmp->{int_in_err} || 0) / $tmp->{int_in_pkt}) : 0;
 
   my $in_drops = $tmp->{int_in_pkt} ? 
      sprintf("%.5f", ($tmp->{int_in_drp} || 0) / $tmp->{int_in_pkt}) : 0;

   my $out_errors = $tmp->{int_out_pkt} ? 
      sprintf("%.5f", ($tmp->{int_out_err} || 0) / $tmp->{int_out_pkt}) : 0;
   
   my $out_drops = $tmp->{int_out_pkt} ? 
      sprintf("%.5f", ($tmp->{int_out_drp} || 0) / $tmp->{int_out_pkt}) : 0;

   if ($int->{no_in_errors}) {
      push @output, "OK - Input errors exempt " .
                    "[errors=$tmp->{int_in_err} packets=$tmp->{int_in_pkt}]";
   }
   elsif ($in_errors >= $threshold) {
      push @output, "CRITICAL - Input errors exceed 0.1% threshold " .
                    "[errors=$tmp->{int_in_err} packets=$tmp->{int_in_pkt}]";
   }
   else {
      push @output, "OK - Input errors below 0.1% threshold " .
                    "[errors=$tmp->{int_in_err} packets=$tmp->{int_in_pkt}]";
   }

   if ($int->{no_in_drops}) {
      push @output, "OK - Input drops exempt " .
                    "[drops=$tmp->{int_in_drp} packets=$tmp->{int_in_pkt}]";
   }
   elsif ($in_drops >= $threshold) {
      push @output, "CRITICAL - Input drops exceed 0.1% threshold " .
                    "[drops=$tmp->{int_in_drp} packets=$tmp->{int_in_pkt}]";
   }
   else {
      push @output, "OK - Input drops below 0.1% threshold " .
                    "[drops=$tmp->{int_in_drp} packets=$tmp->{int_in_pkt}]";
   }

   if ($int->{no_out_errors}) {
      push @output, "OK - Output errors exempt " .
                    "[errors=$tmp->{int_out_err} packets=$tmp->{int_out_pkt}]";
   }
   elsif ($out_errors >= $threshold) {
      push @output, "CRITICAL - Output errors exceed 0.1% threshold " .
                    "[errors=$tmp->{int_out_err} packets=$tmp->{int_out_pkt}]";
   }
   else {
      push @output, "OK - Output errors below 0.1% threshold " .
                    "[errors=$tmp->{int_out_err} packets=$tmp->{int_out_pkt}]";
   }

   if ($int->{no_out_drops}) {
      push @output, "OK - Output drops exempt " .
                    "[drops=$tmp->{int_out_drp} packets=$tmp->{int_out_pkt}]";
   }
   elsif ($out_drops >= $threshold) {
      push @output, "CRITICAL - Output drops exceed 0.1% threshold " .
                    "[drops=$tmp->{int_out_drp} packets=$tmp->{int_out_pkt}]";
   }
   else {
      push @output, "OK - Output drops below 0.1% threshold " .
                    "[drops=$tmp->{int_out_drp} packets=$tmp->{int_out_pkt}]";
   }

   if (my @critical = grep /CRITICAL/, @output) {
      my $msg = shift(@critical) . "|@perfdata\n" . 
                join("\\n", @critical, grep(!/CRITICAL/, @output));
      $self->passive_svc_submit({
         service => &main::PLUGIN . '_interface_problems_' . $name,
         status  => 2,
         output  => $msg,
      });
   } 
   else {
      my $msg = "OK - $name interface problem statistics collected" .
                "|@perfdata\n" . join("\\n", @output);
      $self->passive_svc_submit({
         service => &main::PLUGIN . '_interface_problems_' . $name,
         status  => 0,
         output  => $msg,
      });
   }
}


sub interface_status_passive {
   my ($self, $int) = @_;
   my $states = [ qw(UNDEF UP DOWN TESTING UNKNOWN DORMANT NOT-PRESENT 
                     LOWER-LAYER-DOWN) ];

   my $admin = $int->{int_admin_status};
   my $oper  = $int->{int_oper_status};
   my $admin_txt = $states->[ $admin ];
   my $oper_txt  = $states->[ $oper ];
   my $msg = "$int->{int_name} interface status: " .
             "Admin->$admin_txt Operational->$oper_txt" .
             "|admin=$admin operational=$oper\n";
   my $status = 0;

   if ( $oper == 1 ) {
      # interface is operationally up, so must be ok
      $msg = "OK - $msg";
      $status = 0;
   }
   elsif ( $admin > 1 || $oper > 1 ) {
      $msg = "CRITICAL - $msg";
      $status = 2;
   }

   $self->passive_svc_submit({
      service => &main::PLUGIN . '_interface_status_' . $int->{int_name},
      status  => $status,
      output  => $msg,
   });
}


sub nagsort {
   my $order = [ qw(CRITICAL WARNING UNKNOWN OK) ];
   my $pkg = caller;
   my $aa = eval { "\$${pkg}::a" };
   my $bb = eval { "\$${pkg}::b" };
   my $aaa = eval $aa;
   my $bbb = eval $bb;
   my ($aaaa) = grep { $aaa =~ $order->[ $_ ] } 0 .. 3;
   my ($bbbb) = grep { $bbb =~ $order->[ $_ ] } 0 .. 3;
   return $aaaa <=> $bbbb;
}


return 1;

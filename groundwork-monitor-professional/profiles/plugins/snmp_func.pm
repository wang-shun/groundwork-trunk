#!/usr/local/groundwork/perl/bin/perl -w

package snmp_func;

use strict;
use Net::SNMP ();
use POSIX ();

use lib q(/usr/local/groundwork/nagios/libexec);
use shared_func ();


################################################################################
# instantiate and initialize a new snmp_func object                            #
# required arguments: host, version, community (v1/v2c), user (v3)             #
# optional arguments: port, snmpget_timeout, snmpwalk_timeout, retries,        #
#                     max_msg_size                                             #
# returns object                                                               #
################################################################################
sub new {
   my $class = shift;
   my $self = { @_ };

   if (! $self->{host}) {
      $class->die3("snmp_func->new missing 'host'");
   }
   elsif (! $self->{version}) {
      $class->die3("snmp_func->new missing 'version' [1|2c|3]");
   }
   elsif ($self->{version} =~ /^(?:(1)|(2)c?)$/) {
      $self->{version} = $2 || $1;
      $self->{community} or $class->die3("snmp_func->new missing 'community' string");
   }
   elsif ($self->{version} eq '3') {
      $self->{user} or $class->die3("snmp_func->new missing 'user'");
   }
   else {
      $class->die3("snmp_func->new unknown snmp version ($self->{version})");
   }

   $self->{ port } ||= 161;
   $self->{ snmpget_timeout } ||= 2;   # seconds
   $self->{ snmpwalk_timeout } ||= 5;  # seconds
   $self->{ max_msg_size } ||= 1472;   # bytes
   $self->{ retries } ||= 1;           # count
   $self->{ ec } = 1;                  # boolean (error checking)
   bless ($self, $class);
   return $self;
}


################################################################################
# die2 exits immediately with code 2 (nagios critical) with passed message     #
################################################################################
sub die2 {
   my ($self, $msg) = @_;
   chomp($msg);
   print "CRITICAL - $msg\n";
   exit 2;
}


################################################################################
# die3 exits immediately with code 3 (nagios unknown) with passed message      #
################################################################################
sub die3 {
   my ($self, $msg) = @_;
   chomp($msg);
   print "UNKNOWN - $msg\n";
   exit 3;
}


################################################################################
# check_oids verifies that oids appear valid                                   #
# pass in refernce to array containing one or more oids                        #
# it will die if error encountered                                             #
################################################################################
sub check_oids {
   my ($self, $oids) = @_;
   if (my @badoid = grep !/[0-9.]+/, @$oids) {
      $self->die3("Invalid OIDS: @badoid");
   }
}


################################################################################
# hex_to_string converts binary hex to string                                  #
# accepts one binary hex argument                                              #
# returns ascii string in array                                                #
################################################################################
sub hex_to_string {
   my ($self, $hex) = @_;
   return unpack "C*" => $hex;
}


################################################################################
# snmpget function takes a list of oids and attempts to retrieve them          #
# if "ec" enabled, will die on any oid errors                                  #
# if "ec" disabled, will return undef for any oid errors                       #
# returns array of values in order passed or first if not wantarray            #
################################################################################
sub snmpget {
   my ($self, @oids) = @_;
   my $varbindlist = {};

   # check oids validity
   $self->check_oids( \@oids );

   # build a Net::SNMP object
   my ($snmpget, $error) = Net::SNMP->session( 
      -hostname    => $self->{host},
      -port        => $self->{port},
      -nonblocking => 0,
      -version     => $self->{version},
      -timeout     => $self->{snmpget_timeout},
      -retries     => $self->{retries},
      -maxmsgsize  => $self->{max_msg_size},
      -translate   => 0,
      $self->{community} ? ( -community => $self->{community} ) : (),
      $self->{user} ? ( -username => $self->{user} ) : (),
      $self->{authkey} ? ( -authkey => $self->{authkey} ) : (),
      $self->{authpassword} ? ( -authpassword => $self->{authpassword} ) : (),
      $self->{authprotocol} ? ( -authprotocol => $self->{authprotocol} ) : (),
      $self->{privkey} ? ( -privkey => $self->{privkey} ) : (),
      $self->{privpassword} ? ( -privpassword => $self->{privpassword} ) : (),
      $self->{privprotocol} ? ( -privprotocol => $self->{privprotocol} ) : (),
   );
   $self->die2("snmpget: $error") if $error;

   if ($self->{version} == 1) {
      # snmpv1 will return undef on any errors including missing oids
      if ($self->{ec}) {
         # error-checking is enabled
         # we can use the snmpget->error* to produce an error message
         my $results = $snmpget->get_request( -varbindlist => \@oids );
         if ($results) {
            # snmpget successful
            # loop through results to normalize
            foreach my $oid (@oids) {
               chomp($varbindlist->{ $oid } = $snmpget->var_bind_list->{ $oid });
            }
         }
         elsif ($snmpget->error_status == 0) {
            # snmpget failed due to connectivity/authentication
            my $msg = sprintf "snmpget: %s", $snmpget->error;
            ref $self->{callback} eq 'CODE' and $self->{callback}->(2, $msg);
            $self->die2($msg);
         }
         elsif ($snmpget->error_status) {
            # snmpget failed due to some snmp error
            # itoa retrieves error reason; less verbose than ->error
            my $itoa = Net::SNMP::PDU::_error_status_itoa( $snmpget->error_status );
            my $msg = sprintf "snmpget: %s (%s)", $itoa,
               $oids[ $snmpget->error_index - 1 ];
            #ref $self->{callback} eq 'CODE' and $self->{callback}->(3, $msg);
            $self->die3($msg);
         } 
      }
      else {
         # error-checking disabled
         foreach my $oid (@oids) {
            my $results = $snmpget->get_request( -varbindlist => [ $oid ] );
            if ($results) {
               # snmpget successful; copy result
               chomp($varbindlist->{ $oid } = $snmpget->var_bind_list->{ $oid });
            }
            elsif ($snmpget->error_status == 0) {
               # snmpget failed due to connectivity/authentication
               # bypass error checking and die anyway
               my $msg = sprintf "snmpget: %s", $snmpget->error;
               ref $self->{callback} eq 'CODE' and $self->{callback}->(2, $msg);
               $self->die2($msg);
            }
            elsif ($snmpget->error_status) {
               # snmpget failed due to some snmp error
               # set result to undef and continue
               $varbindlist->{ $oid } = undef; 
            } 
         }
      }
   }
   else {
      # snmpv2 will return undef on connectivity and authentication problems
      # it will also return undef if every OID in the request is not found
      # snmp errors cause blank values and var_bind_type set to hex error code
      my $results = $snmpget->get_request( -varbindlist => \@oids ) or do {
         # the below line shortcuts to return an empty array if conn/auth is okay 
         # but all of the request OIDs don't seem to exist
         $snmpget->error_status == 2 and return ();
         my $msg = sprintf "snmpget: %s", $snmpget->error;
         ref $self->{callback} eq 'CODE' and $self->{callback}->(2, $msg);
         $self->die2($msg);
      };
      foreach my $oid (@oids) {
         my $type = $snmpget->var_bind_types->{ $oid };
         if ( grep { $_ == $type } 0x80 .. 0x82 ) {
            # snmp errors detected such as oid not found
            if ( $self->{ec} ) {
               # error checking enabled
               my $error_name = Net::SNMP::snmp_type_ntop( $type );
               my $msg = sprintf "snmpget: %s (%s)", $error_name, $oid;
               $self->die3($msg);
            }
            else {
               # set value to undef
               $varbindlist->{ $oid } = undef;
            }
         }
         else {
            # oid was successful; copy value
            chomp($varbindlist->{ $oid } = $snmpget->var_bind_list->{ $oid });
         }
      }
   }

   $snmpget->close;

   return wantarray ? @$varbindlist{@oids} : $varbindlist->{ $oids[0] };
}


################################################################################
# snmpget_or function takes a list of oids and returns first successful oid    #
# the "ec" flag has no meaning; use snmpget manually if no-ec is needed        #
# dies if no successful oids return                                            #
################################################################################
sub snmpget_or {
   my ($self, @oids) = @_;
   my $result = ();
   my $ec = $self->{ec};   # store original error checking value
   $self->{ec} = 0;        # disable snmp error checking
   foreach my $oid (@oids) {
      last if defined($result = $self->snmpget( $oid ));
   }
   unless ( defined($result) ) {
      my $msg = "snmpget_or: no values returned (@oids)";
      #ref $self->{callback} eq 'CODE' and $self->{callback}->(2, $msg);
      $self->die3($msg);
   }  
   $self->{ec} = $ec;      # restore original error checking value
   return $result;
}


################################################################################
# snmpwalk takes a oid and attempts to snmp walk it                            #
# if "ec" enabled, will die on any oid errors                                  #
# if "ec" disabled, will return undef for any oid errors                       #
# returns array of values in order returned by walk                            #
################################################################################
sub snmpwalk {
   my ($self, $oid ) = @_;
   my $roid = $oid;
   my $varbindlist = {};

   # check oids validity
   $self->check_oids([ $oid ]);

   # build a Net::SNMP object
   my ($snmpwalk, $error) = Net::SNMP->session(
      -hostname    => $self->{host},
      -port        => $self->{port},
      -nonblocking => 0,
      -version     => $self->{version},
      -timeout     => $self->{snmpget_timeout},
      -retries     => $self->{retries},
      -maxmsgsize  => $self->{max_msg_size},
      -translate   => 0,
      $self->{community} ? ( -community => $self->{community} ) : (),
      $self->{user} ? ( -username => $self->{user} ) : (),
      $self->{authkey} ? ( -authkey => $self->{authkey} ) : (),
      $self->{authpassword} ? ( -authpassword => $self->{authpassword} ) : (),
      $self->{authprotocol} ? ( -authprotocol => $self->{authprotocol} ) : (),
      $self->{privkey} ? ( -privkey => $self->{privkey} ) : (),
      $self->{privpassword} ? ( -privpassword => $self->{privpassword} ) : (),
      $self->{privprotocol} ? ( -privprotocol => $self->{privprotocol} ) : (),
   );
   $self->die2("snmpwalk: $error") if $error;

   # to perform a standard snmpwalk, we use snmp get-next requests
   # must loop through until we get past our $oid baseoid or hit
   # the end of the mib tree
   for (;;) {
      my $results = $snmpwalk->get_next_request( -varbindlist => [ $roid ] );
      # snmpv1 $results will be undef on any error
      # snmpv2/3 $results will be undef only on conn/auth error
      if ($results && $snmpwalk->error_status == 0) {
         # getnext succeeded and we didn't encounter any snmp errors
         my ($key, $value) = each %$results;
         $key =~ /^$oid/ or last;
         chomp($varbindlist->{ $key } = $value);
         $roid = $key;
      }
      elsif ($snmpwalk->error_status == 0) {
         # connectivity/authentication error
         my $msg = sprintf "snmpwalk: %s", $snmpwalk->error;
         ref $self->{callback} eq 'CODE' and $self->{callback}->(2, $msg);
         $self->die2($msg);
      }
      else {
         # hit end of mib tree
         # ->error_status returns a positive integer
         last;
      }
   }     

   # return the results back to the caller
   if (wantarray) {
      # caller expecting array (list) format; sending sorted list
      my @keys = Net::SNMP::oid_lex_sort( keys %$varbindlist );
      return @$varbindlist{ @keys };
   }
   else {
      # caller expecting scalar; sending pointer to varbindlist hash
      return $varbindlist;
   }
} 


################################################################################
# snmpbulkwalk takes a oid and attempts to snmp bulkwalk it                    #
# if "ec" enabled, will die on any oid errors                                  #
# if "ec" disabled, will return undef for any oid errors                       #
# returns array of values in order returned by walk                            #
################################################################################
sub snmpbulkwalk {
   my ($self, $oid ) = @_;
   my $roid = $oid;
   my $varbindlist = {};

   # snmpv1 doesn't support bulkwalk, revert to snmpwalk
   if ($self->{version} == 1) {
      return $self->snmpwalk( $oid );
   }

   # check oids validity
   $self->check_oids([ $oid ]);

   # build a Net::SNMP object
   my ($snmpbulkwalk, $error) = Net::SNMP->session(
      -hostname    => $self->{host},
      -port        => $self->{port},
      -nonblocking => 0,
      -version     => $self->{version},
      -timeout     => $self->{snmpwalk_timeout},
      -retries     => $self->{retries},
      -maxmsgsize  => $self->{max_msg_size},
      -translate   => 0,
      $self->{community} ? ( -community => $self->{community} ) : (),
      $self->{user} ? ( -username => $self->{user} ) : (),
      $self->{authkey} ? ( -authkey => $self->{authkey} ) : (),
      $self->{authpassword} ? ( -authpassword => $self->{authpassword} ) : (),
      $self->{authprotocol} ? ( -authprotocol => $self->{authprotocol} ) : (),
      $self->{privkey} ? ( -privkey => $self->{privkey} ) : (),
      $self->{privpassword} ? ( -privpassword => $self->{privpassword} ) : (),
      $self->{privprotocol} ? ( -privprotocol => $self->{privprotocol} ) : (),
   );
   $self->die2("snmpbulkwalk: $error") if $error;

   # get_bulk_request returns maxrepetitions oids at one time
   # we must loop to continue walking to the end of tree
   # maxrepetitions of 10 is the default from snmpbulkwalk cli utility
   for (;;) {
      my $results = $snmpbulkwalk->get_bulk_request( 
         -varbindlist    => [ $roid ],
         -maxrepetitions => 10 ) or do { 
         my $msg = sprintf "snmpbulkwalk: %s", $snmpbulkwalk->error;
         ref $self->{callback} eq 'CODE' and $self->{callback}->(2, $msg);
         $self->die2($msg);
      };
      if ($results && $snmpbulkwalk->error_status == 0) {
         # getbulk request successful
         foreach my $toid (Net::SNMP::oid_lex_sort( keys %$results )) {
            # leave foreach loop if bulkwalk has left $oid tree
            $toid =~ /^$oid/ or last;
            # copy oid and value
            chomp($varbindlist->{ $toid } = $results->{ $toid });
            # set oid for next get_bulk_request
            $roid = $toid;
         }
         # leave loop because we have gone outside of $oid tree
         grep !/^$oid/ => keys(%$results) and last;
      }
      else {
         # hit end of mib tree
         last;
      }
   }

   # return the results back to the caller
   if (wantarray) {
      # caller expecting array (list) format; sending sorted list
      my @keys = Net::SNMP::oid_lex_sort( keys %$varbindlist );
      return @$varbindlist{ @keys };
   }
   else {
      # caller expecting scalar; sending pointer to varbindlist hash
      return $varbindlist;
   }
}


################################################################################
# snmp_cpu retrieves host-resource-mib processor load values                   #
# accepts parse_func object as $args                                           #
# will loop through each processor found                                       #
# returns nothing                                                              #
################################################################################
sub snmp_cpu {
   my ($self, $args) = @_;
   my $hrprocessorload = q(.1.3.6.1.2.1.25.3.3.1.2);   # cpu %
   my (@output, @perfdata) = ();

   # parse the warning:critical levels or default if not specified
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 85;    # default to 85%
   $crit ||= 95;    # default to 95%

   # snmpbulkwalk the hrprocessorload oid tree
   my @procload = $self->snmpbulkwalk( $hrprocessorload ) or do {
      # if nothing returns from snmpbulkwalk
      $self->die3("No values returned for hrProcessorLoad");
   };
  
   # loop through each processor found and test against thresholds
   for my $i (0 .. $#procload) {
      my $index = $i + 1;
      my $cpu = $procload[$i];
      push @perfdata, "cpu$index=$cpu";
      if ($cpu >= $crit) {
         push @output, "CRITICAL - CPU$index at $cpu% (threshold $crit%)";
      }
      elsif ($cpu >= $warn) {
         push @output, "WARNING - CPU$index at $cpu% (threshold $warn%)";
      }
      else {
         push @output, "OK - CPU$index at $cpu%";
      } 
   }

   # generate output based on previous data collection
   if (my $critical = grep /CRITICAL/ => @output) {
      print "CRITICAL - $critical CPUs high utilization [@perfdata]|@perfdata\n";
      print join "\n" => @output;
      exit 2;
   }
   elsif (my $warning = grep /WARNING/ => @output) {
      print "WARNING - $warning CPUs high utilization [@perfdata]|@perfdata\n";
      print join "\n" => @output;
      exit 1;
   }
   else {
      my $ok = @output;
      print "OK - $ok CPUs healthy [@perfdata]|@perfdata\n";
      print join "\n" => @output;
   } 
}


################################################################################
# snmp_datetime retrieves host-resource-mib date and time                      #
# accepts parse_func object as $args                                           #
# returns nothing                                                              #
################################################################################
sub snmp_datetime {
   my ($self, $args, $reverse) = @_;
   my $oid = q(.1.3.6.1.2.1.25.1.2.0);   # hrSystemDate

   # parse the warning:critical levels or default if not specified
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 60;     # default to 60 seconds
   $crit ||= 300;    # default to 300 seconds

   # retrieve datetime oid
   # will return in hex format
   my $packed = $self->snmpget( $oid ) or 
      $self->die3("hrSystemDate returned nothing");

   # unpack hex into text
   # $datetime[0] => year [2012]
   # $datetime[1] => month [1..12]
   # $datetime[2] => day [1..31]
   # $datetime[3] => hour [0..24]
   # $datetime[4] => minute [0..59]
   # $datetime[5] => second [0..59]
   # $datetime[6] => deciseconds [??]
   # $datetime[7] => direction from utc [+/-]
   # $datetime[8] => hours from utc [0..13]
   # $datetime[9] => minutes from utc [0..59]
   my @datetime = unpack "n C6 a C2" => $packed;

   # capture local and remote times into unix time
   my $local_time = time();
   my $remote_time = do {
      my $unixtime = POSIX::strftime("%s", @datetime[5,4,3,2], $datetime[1]-1, 
                                           $datetime[0]-1900, 0, 0, 0);
      if ($reverse) {
         if ($datetime[7] && $datetime[7] eq '+') {
            $unixtime += $datetime[8] * 3600;
            $unixtime += $datetime[9] * 60;
         }
         elsif ($datetime[7] && $datetime[7] eq '-') {
            $unixtime -= $datetime[8] * 3600;
            $unixtime -= $datetime[9] * 60;
         }
      }
      else {
         if ($datetime[7] && $datetime[7] eq '+') {
            $unixtime -= $datetime[8] * 3600;
            $unixtime -= $datetime[9] * 60;
         }
         elsif ($datetime[7] && $datetime[7] eq '-') {
            $unixtime += $datetime[8] * 3600;
            $unixtime += $datetime[9] * 60;
         }
      }
      $unixtime;
   };

   # calculate the absolute difference between local and remote times
   my $diff = abs( $local_time - $remote_time );

   # convert the unix times back to human readable for text output
   my $local_time_str = scalar localtime $local_time;
   my $remote_time_str = scalar localtime $remote_time;

   my $perfdata = "skew=$diff";

   # compare against thresholds and generate output
   if ( $diff >= $crit ) {
      print "CRITICAL - System clock skew is $diff seconds " .
            "(threshold $crit)|$perfdata\n";
      printf "Local time:  %s\nRemote time: %s\n", $local_time_str, 
                                                   $remote_time_str;
      exit 2;
   }
   elsif ($diff >= $warn) {
      print "WARNING - System clock skew is $diff seconds " .
            "(threshold $warn)|$perfdata\n";
      printf "Local time:  %s\nRemote time: %s\n", $local_time_str, 
                                                   $remote_time_str;
      exit 1;
   }
   else {
      print "OK - System clock skew is $diff seconds|$perfdata\n";
      printf "Local time:  %s\nRemote time: %s\n", $local_time_str, 
                                                   $remote_time_str;
   }
}


################################################################################
# snmp_disk retrieves host-resource-mib disk values                            #
# accepts parse_func object as $args                                           #
# returns nothing                                                              #
################################################################################
sub snmp_disk {
   my ($self, $args, @ignore) = @_;
   my (@output, @perfdata) = ();

   # ignore various partition names
   push @ignore => qw(/proc procfs /dev devfs); 

   # define oids for hrStorage*
   my $oid = { 'storindex' => '.1.3.6.1.2.1.25.2.3.1.1',
               'stortype'  => '.1.3.6.1.2.1.25.2.3.1.2',
               'storname'  => '.1.3.6.1.2.1.25.2.3.1.3',
               'storunits' => '.1.3.6.1.2.1.25.2.3.1.4',
               'storsize'  => '.1.3.6.1.2.1.25.2.3.1.5',
               'storused'  => '.1.3.6.1.2.1.25.2.3.1.6',
             };

   # define mappings for hrStorageTypes
   # commented (#) lines for mib completeness
   my $hrstoragetypes = { #'.1.3.6.1.2.1.25.2.1.1' => 'other',
                          #'.1.3.6.1.2.1.25.2.1.2' => 'ram',
                          #'.1.3.6.1.2.1.25.2.1.3' => 'vm',
                          '.1.3.6.1.2.1.25.2.1.4' => 'disk',
                          #'.1.3.6.1.2.1.25.2.1.5' => 'removable_disk',
                          #'.1.3.6.1.2.1.25.2.1.6' => 'floppy',
                          #'.1.3.6.1.2.1.25.2.1.7' => 'cd',
                          #'.1.3.6.1.2.1.25.2.1.8' => 'ramdisk',
                          #'.1.3.6.1.2.1.25.2.1.8' => 'flash',
                          #'.1.3.6.1.2.1.25.2.1.8' => 'network_disk',
                        };
   
   # parse the warning:critical levels or default if not specified
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default to 80%
   $crit ||= 90;    # default to 90%

   # retrieve list of stortypes into hash
   my $stortypes = $self->snmpbulkwalk( $oid->{ stortype } );

   # hash should contain results otherwise the oid was invalid
   scalar keys %$stortypes or $self->die3("Unable to retrieve hrStorage oids");

   # loop through each stortype received
   # we will parse the index out of the snmpbulkwalk results
   # then use that index to snmpget the remainder of the values needed
   foreach my $typeoid (Net::SNMP::oid_lex_sort keys %$stortypes) {
      my ($index) = $typeoid =~ /^$oid->{stortype}\.(\d+)$/ or next;
      my $type = $hrstoragetypes->{ $stortypes->{ $typeoid } } or next;
      my @stor = $self->snmpget( map { "$oid->{$_}.$index" } 
         qw(storunits storsize storused) );

      # attempt to retrieve name of partition
      # disable error checking as this may not exist on some platforms
      $self->{ec} = 0;
      if (my $storname = $self->snmpget( "$oid->{storname}.$index" )) {
         unshift @stor, $storname;
      }
      else {
         unshift @stor, "part$index";
      }
      $self->{ec} = 1;

      # calculate partition utilization in MB and %
      my $partsize = $stor[2] * $stor[1] / 1024;
      my $partused = $stor[3] * $stor[1] / 1024;
      my $percent = $partsize ? sprintf("%d", $partused / $partsize * 100) : 0;

      # cleanup of the partition name
      $stor[0] ||= "part$index";
      my ($partname) = $stor[0] =~ m|^([a-zA-Z0-9/]+)|;
      grep { $_ eq $partname } @ignore and next;
      $stor[1] == 4096 && $stor[2] == 1 and next;

      # compare against thresholds
      if ($percent >= $crit) {
         push @output, "CRITICAL - Disk usage on $partname at $percent% " .
                       "(threshold $crit%)";
      }
      elsif ($percent >= $warn) {
         push @output, "WARNING - Disk usage on $partname at $percent% " .
                       "(threshold $warn%)";
      }
      else {
         push @output, "OK - Disk usage on $partname at $percent%";
      } 

      # further partition name cleanup for perfdata
      # perfdata doesn't allow slashes
      $partname =~ tr|^/||d;
      $partname =~ tr|/|_|;
      $partname ||= 'root';
      push @perfdata, "$partname=$percent";
   }   

   # generate output
   if (my $critical = grep /CRITICAL/ => @output) {
      print "CRITICAL - $critical partitions high usage [@perfdata]|@perfdata\n";
      print join "\n" => @output;
      exit 2;
   }
   elsif (my $warning = grep /WARNING/ => @output) {
      print "WARNING - $warning partitions high usage [@perfdata]|@perfdata\n";
      print join "\n" => @output;
      exit 1;
   }
   else {
      my $ok = @output;
      print "OK - $ok partitions healthy [@perfdata]|@perfdata\n";
      print join "\n" => @output;
   } 
}


################################################################################
# snmp_interface retrieves interface statistics for all non-tunnel interfaces  #
# no arguments needed                                                          #
# returns hash reference containing interface statistics                       #
################################################################################
sub snmp_interface {
   my $self = shift;
   my $dup = {};
   my $int = {};
   my $oid = { 'int_count'        => '.1.3.6.1.2.1.2.1.0',
               'int_index'        => '.1.3.6.1.2.1.2.2.1.1',
               'int_desc'         => '.1.3.6.1.2.1.2.2.1.2',
               'int_type'         => '.1.3.6.1.2.1.2.2.1.3',
               'int_speed'        => '.1.3.6.1.2.1.2.2.1.5',
               #'int_mac'         => '.1.3.6.1.2.1.2.2.1.6',
               'int_admin_status' => '.1.3.6.1.2.1.2.2.1.7',
               'int_oper_status'  => '.1.3.6.1.2.1.2.2.1.8',
               'int32_in_oct'     => '.1.3.6.1.2.1.2.2.1.10',
               'int32_in_ucast'   => '.1.3.6.1.2.1.2.2.1.11',
               'int32_in_nucast'  => '.1.3.6.1.2.1.2.2.1.12',
               'int_in_drp'       => '.1.3.6.1.2.1.2.2.1.13',
               'int_in_err'       => '.1.3.6.1.2.1.2.2.1.14',
               'int32_out_oct'    => '.1.3.6.1.2.1.2.2.1.16',
               'int32_out_ucast'  => '.1.3.6.1.2.1.2.2.1.17',
               'int32_out_nucast' => '.1.3.6.1.2.1.2.2.1.18',
               'int_out_drp'      => '.1.3.6.1.2.1.2.2.1.19',
               'int_out_err'      => '.1.3.6.1.2.1.2.2.1.20',
               'int_name'         => '.1.3.6.1.2.1.31.1.1.1.1',
               'int64_in_oct'     => '.1.3.6.1.2.1.31.1.1.1.6',
               'int64_in_ucast'   => '.1.3.6.1.2.1.31.1.1.1.7',
               'int64_in_mcast'   => '.1.3.6.1.2.1.31.1.1.1.8',
               'int64_in_bcast'   => '.1.3.6.1.2.1.31.1.1.1.9',
               'int64_out_oct'    => '.1.3.6.1.2.1.31.1.1.1.10',
               'int64_out_ucast'  => '.1.3.6.1.2.1.31.1.1.1.11',
               'int64_out_mcast'  => '.1.3.6.1.2.1.31.1.1.1.12',
               'int64_out_bcast'  => '.1.3.6.1.2.1.31.1.1.1.13',
             };

   # setup groups of oids to snmgpet at one time
   my @group1 = qw(int_desc int_name int_speed int_admin_status int_oper_status
                   int_in_drp int_in_err int_out_drp int_out_err);
   my @group2 = qw(int32_in_oct int32_in_ucast int32_in_nucast int32_out_oct 
                   int32_out_ucast int32_out_nucast);
   my @group3 = qw(int64_in_oct int64_in_ucast int64_in_mcast int64_in_bcast
                   int64_out_oct int64_out_ucast int64_out_mcast 
                   int64_out_bcast);

   # 64-bit counters not supported on SNMPv1
   $self->{version} == 1 and @group3 = ();
       
   # test that there are interfaces to retrieve
   $self->snmpget( $oid->{int_count} ) or return $int;          
   
   # retrieve the interface types
   # skip tunnel(131) interfaces
   {
      my $types = $self->snmpbulkwalk( $oid->{int_type} );
      while (my ($key, $value) = each %$types) {
         next if $value == 131;
         my ($index) = $key =~ /^$oid->{int_type}\.(\d+)$/ or next;
         $int->{$index}->{int_type} = $value;
      } 
   }

   # disable snmp error checking
   $self->{ec} = 0;  

   # loop through each interface to retrieve its statistics
   foreach my $ifindex (sort keys %$int) {
      my $ptr = $int->{$ifindex};
      foreach my $group (\@group1, \@group2, \@group3) {
         my @oids = map { "$_.$ifindex" } @$oid{@$group};
         @$ptr{@$group} = $self->snmpget( @oids );
      }

      # perform normalization
      $ptr->{int_name} ||= $ptr->{int_desc} || "Interface $ifindex";
      push @{ $dup->{ $ptr->{int_name} } }, $ifindex;
      defined $ptr->{int_in_drp}  or $ptr->{int_in_drp}  = 'U';
      defined $ptr->{int_out_drp} or $ptr->{int_out_drp} = 'U';
      defined $ptr->{int_in_err}  or $ptr->{int_in_err}  = 'U';
      defined $ptr->{int_out_err} or $ptr->{int_out_err} = 'U';
     
      # some devices return zero's for 64-bit counters and valid
      # values for 32-bit counters, so lets test for that 
      $ptr->{int_in_oct} = do {
         if ($ptr->{int64_in_oct}) {
            $ptr->{int64_in_oct};
         }
         elsif ($ptr->{int32_in_oct}) {
            $ptr->{int32_in_oct};
         }
         elsif (defined $ptr->{int64_in_oct}) {
            $ptr->{int64_in_oct};
         }
         elsif (defined $ptr->{int32_in_oct}) {
            $ptr->{int32_in_oct};
         }
         else {
            'U';
         }
      };

      $ptr->{int_out_oct} = do {
         if ($ptr->{int64_out_oct}) {
            $ptr->{int64_out_oct};
         }
         elsif ($ptr->{int32_out_oct}) {
            $ptr->{int32_out_oct};
         }
         elsif (defined $ptr->{int64_out_oct}) {
            $ptr->{int64_out_oct};
         }
         elsif (defined $ptr->{int32_out_oct}) {
            $ptr->{int32_out_oct};
         }
         else {
            'U';
         }
      };

      # sum 64-bit / 32-bit values to come up with total packet counters
      $ptr->{int_in_pkt} = do { 
         my @int64 = map { defined($_) ? $_ : () } @$ptr{ grep /in_[umb]cast/ => @group3 };
         my @int32 = map { defined($_) ? $_ : () } @$ptr{ grep /in_n?ucast/ => @group2 };
         if (scalar @int64) {
            $_ = eval join '+' => @int64;
         }
         elsif (scalar @int32) {
            $_ = eval join '+' => @int32;
         }
         else {
            'U';
         }
      }; 

      $ptr->{int_out_pkt} = do {
         my @int64 = map { defined($_) ? $_ : () } @$ptr{ grep /out_[umb]cast/ => @group3 };
         my @int32 = map { defined($_) ? $_ : () } @$ptr{ grep /out_n?ucast/ => @group2 };
         if (scalar @int64) {
            $_ = eval join '+' => @int64;
         }
         elsif (scalar @int32) {
            $_ = eval join '+' => @int32;
         }
         else {
            'U';
         }
      };
   }

   # look for any duplicate interface names and append ifindex for duplicates
   foreach my $ifname (keys %$dup) {
      @{ $dup->{$ifname} } == 1 and next;
      foreach my $ifindex (@{ $dup->{$ifname} }) {
         $int->{$ifindex}->{int_name} .= ".$ifindex";
      }
   }

   # returns pointer to hash containing all interface information
   return $int;
}


################################################################################
# snmp_memory retrieves host-resource-mib memory values                        #
# accepts parse_func object as $args                                           #
# returns nothing                                                              #
#                                                                              #
# BUG: Linux returns multiple vm storage types with the first being total vm   #
# which is a combination of all real + swap memory.  It also will return one   #
# or more swap types as virtual memory.  The plugin currently only accounts    #
# for the last matching virtual memory entry returned.  Need to create a       #
# a workaround to properly handle this.                                        #
################################################################################
sub snmp_memory {
   my ($self, $args) = @_;
   my $vm = {};

   # define oids for hrStorage*
   my $oid = { 'storindex' => '.1.3.6.1.2.1.25.2.3.1.1',
               'stortype'  => '.1.3.6.1.2.1.25.2.3.1.2',
               'storname'  => '.1.3.6.1.2.1.25.2.3.1.3',
               'storunits' => '.1.3.6.1.2.1.25.2.3.1.4',
               'storsize'  => '.1.3.6.1.2.1.25.2.3.1.5',
               'storused'  => '.1.3.6.1.2.1.25.2.3.1.6',
             };

   # define mappings for hrStorageTypes
   # commented (#) lines for mib completeness
   my $hrstoragetypes = { #'.1.3.6.1.2.1.25.2.1.1' => 'other',
                          '.1.3.6.1.2.1.25.2.1.2' => 'ram',
                          '.1.3.6.1.2.1.25.2.1.3' => 'vm',
                          #'.1.3.6.1.2.1.25.2.1.4' => 'disk',
                          #'.1.3.6.1.2.1.25.2.1.5' => 'removable_disk',
                          #'.1.3.6.1.2.1.25.2.1.6' => 'floppy',
                          #'.1.3.6.1.2.1.25.2.1.7' => 'cd',
                          #'.1.3.6.1.2.1.25.2.1.8' => 'ramdisk',
                          #'.1.3.6.1.2.1.25.2.1.8' => 'flash',
                          #'.1.3.6.1.2.1.25.2.1.8' => 'network_disk',
                        };
   
   # parse the warning:critical levels or default if not specified
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default to 80%
   $crit ||= 90;    # default to 90%

   # retrieve list of stortypes into hash
   my $stortypes = $self->snmpbulkwalk( $oid->{ stortype } );

   # hash should contain results otherwise the oid was invalid
   scalar keys %$stortypes or $self->die3("Unable to retrieve hrStorage oids");

   # loop through each stortype received
   # we will parse the index out of the snmpbulkwalk results
   # then use that index to snmpget the remainder of the values needed
   foreach my $typeoid (Net::SNMP::oid_lex_sort keys %$stortypes) {
      my ($index) = $typeoid =~ /^$oid->{stortype}\.(\d+)$/ or next;
      my $type = $hrstoragetypes->{ $stortypes->{ $typeoid } } or next;
      my @stor = $self->snmpget( map { "$oid->{$_}.$index" } qw(storunits storsize storused) );
      if ($type eq 'ram') {
         # found hrStorageRam
         $vm->{mem_total} = $stor[1] * $stor[0] / 1024;
         $vm->{mem_used}  = $stor[2] > $stor[1] ? $stor[2] : $stor[2] * $stor[0] / 1024;
      }
      elsif ($type eq 'vm') {
         # found hrStorageVirtualMemory
         $vm->{swap_total} = $stor[1] * $stor[0] / 1024;
         $vm->{swap_used}  = $stor[2] * $stor[0] / 1024;
      }
   }   
 
   # test for valid 'ram' values
   # swap values are optional
   $vm->{mem_total} and $vm->{mem_used} or do {
      $self->die3("Unable to retrieve memory oids");
   };

   # calculate total memory utilization
   $vm->{total} = $vm->{mem_total} + ($vm->{swap_total} || 0);
   $vm->{used}  = $vm->{mem_used}  + ($vm->{swap_used} || 0);
   $vm->{percent} = sprintf("%d", $vm->{used} / $vm->{total} * 100);

   # create perfdata 
   my $perfdata = "mem_usage=$vm->{percent}";

   # test thresholds and generate output
   if ($vm->{percent} >= $crit) {
      print "CRITICAL - Memory at $vm->{percent}% (threshold $crit%)|$perfdata";
      exit 2;
   }
   elsif ($vm->{percent} >= $warn) {
      print "WARNING - Memory at $vm->{percent}% (threshold $warn%)|$perfdata"; 
      exit 1;
   }
   else {
      print "OK - Memory at $vm->{percent}%|$perfdata";
   }
}


################################################################################
# snmp_proc retrieves host-resource-mib process names                          #
# accepts parse_func object as $args                                           #
# returns nothing                                                              #
################################################################################
sub snmp_proc {
   my ($self, $args) = @_;
   my $oid = q(.1.3.6.1.2.1.25.4.2.1.2);   # hrSWRunName

   # check for process name (-p) in $args
   $args->{p} or $self->die3("snmp_func->snmp_proc missing proc name");
   
   # parse the warning:critical levels or default if not specified
   my ($low, $high) = grep /^\d+$/ => split /:/ => $args->{l};
   $low ||= 1;    # default to 1 processes
   $high ||= 1;    # default to 1 processes

   # retrieve names of all running applications
   my @procnames = $self->snmpbulkwalk( $oid ) or
      $self->die3("No results returned for hrSWRunName");
 
   # count and group matching processes 
   my $found = {};
   my $count = 0;
   foreach (@procnames) {
      /^$args->{p}$/ or next;
      $found->{ $_ }++;
      $count++;
   }
   my $perfdata = "procs=$count";

   # test against thresholds and generate output
   if ($count < $low) {
      print "CRITICAL - Found $count $args->{p} processes " .
            "(threshold <=$low)|$perfdata\n";
      print join "\n" => map { "$_ => $found->{$_}" } keys %$found;
      exit 2;
   }
   elsif ($count > $high) {
      print "CRITICAL - Found $count $args->{p} processes " .
            "(threshold >=$high)|$perfdata\n";
      print join "\n" => map { "$_ => $found->{$_}" } keys %$found;
      exit 2;
   }
   else {
      print "OK - Found $count $args->{p} processes|$perfdata\n";
      print join "\n" => map { "$_ => $found->{$_}" } keys %$found;
   }
}


################################################################################
# snmp_uptime retrieves uptime oids from remote device                         #
# can accept 2 arguments: a custom oid and a multiplier                        #
# calls shared_func::shared_uptime to perform threshold checking               #
################################################################################
sub snmp_uptime {
   my ($self, $args, $custom_oid, $multiplier) = @_;

   # hr_uptime     => .1.3.6.1.2.1.25.1.1.0
   # disman_uptime => .1.3.6.1.2.1.1.3.0
   my @oid = qw(.1.3.6.1.2.1.25.1.1.0 .1.3.6.1.2.1.1.3.0);

   # set number of snmp retries to 3 (initial+2) to prevent snmp failure alarm
   $self->{retries} = 2;

   # retrieve uptime (timeticks) from $custom_oid or standard uptime oids
   my $uptime = $custom_oid ? $self->snmpget($custom_oid) : 
      $self->snmpget_or( @oid ); 

   # multiply by multiplier if $multiplier defined
   $uptime *= $multiplier if $multiplier;

   # convert timeticks to days
   if ( (my $days = $uptime / 100 / 60 / 60 / 24) >= 1 ) {
      # if >=1 days then make integer
      $uptime = sprintf "%d", $days;
   }
   else {
      # if <1 days then make float
      $uptime = sprintf "%.1f", $days;
   }  

   # call shared_uptime function
   shared_func::shared_uptime( $args, $uptime ); 
}


################################################################################
# string_to_hex converts ascii strings into hex for use in oids                #
# accepts one string argument                                                  #
# returns 'dot' separated hex string                                           #
################################################################################
sub string_to_hex {
   my ($self, $string) = @_;
   my $str_length = length( $string );
   my @str_array = split // => $string;
   my @hex_array = map { ord($_) } @str_array;
   return join '.' => $str_length, @hex_array;
}


################################################################################
# ucd_cpu retrieves uc-davis cpu statistics                                    #
# accepts parse_func object as $args                                           #
# returns nothing                                                              #
#                                                                              #
# for up systems the results are accurate                                      #
# for smp systems timeticks are generated for each processor and are           #
# aggregated together in net-snmp output; this makes the results valid only    #
# as a system-wide average of cpu utilization across all processors            #
################################################################################
sub ucd_cpu {
   my ($self, $args) = @_;
   my $totalticks = 0;
   my $ticks = {};
   my $percent = {};
   my $oid = { user    => '.1.3.6.1.4.1.2021.11.50.0',
               nice    => '.1.3.6.1.4.1.2021.11.51.0',
               system  => '.1.3.6.1.4.1.2021.11.52.0',
               idle    => '.1.3.6.1.4.1.2021.11.53.0',
               iowait  => '.1.3.6.1.4.1.2021.11.54.0',
               kernel  => '.1.3.6.1.4.1.2021.11.55.0',
               hardint => '.1.3.6.1.4.1.2021.11.56.0',
               softint => '.1.3.6.1.4.1.2021.11.61.0',
             };
   my @order = qw(user nice system idle iowait kernel hardint softint);
   
   # parse the warning:critical levels or default if not specified
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 85;    # default to 85%
   $crit ||= 95;    # default to 95%

   # disable error checking
   # some legacy devices don't support soft interrupts
   $self->{ec} = 0;

   # perform run #1
   my @run1 = $self->snmpget( @$oid{@order} );

   # error check to make sure all values arent undef
   if (scalar(grep { ! defined } @run1) == scalar(@order)) {
      $self->die3("ucd_cpu: unable to retrieve ucDavis cpu statistics");
   }

   # sleep for 6 seconds; net-snmp seems to only update every 5 seconds or so
   sleep 6;

   # perform run #2
   my @run2 = $self->snmpget( @$oid{@order} );

   # error check to make sure all values arent undef
   if (scalar(grep { ! defined } @run1) == scalar(@order)) {
      $self->die3("ucd_cpu: unable to retrieve ucDavis cpu statistics");
   }

   # count cpu ticks per type
   foreach my $i (0 .. $#order) {
      my $diff = (defined $run2[$i] && defined $run1[$i]) ? 
         $run2[$i] - $run1[$i] : 0;
      $ticks->{ $order[$i] } = $diff;
      $totalticks += $diff;
   }

   # calculate percentage spent on each tick type
   $percent->{ $_ } = sprintf "%.1f", $ticks->{ $_ } / $totalticks * 100
      foreach @order;

   # calculate overall cpu usage
   my $cpuusage = sprintf "%d", 100 - $percent->{idle};
   my $perfdata = "percent=$cpuusage";
   my $longoutput = join "\n" => 
      map { sprintf("%-7s : %s", $_, $percent->{$_}) } @order;

   # test against thresholds and generate output
   if ( $cpuusage >= $crit ) {
      print "CRITICAL - System average CPU utilization at $cpuusage% " .
            "(threshold $crit%)|$perfdata\n";
      print $longoutput, "\n";
      exit 2;
   }
   elsif ( $cpuusage >= $warn ) {
      print "WARNING - System average CPU utilization at $cpuusage% " .
            "(threshold $warn%)|$perfdata\n";
      print $longoutput, "\n";
      exit 1;
   }
   else {
      print "OK - System average CPU utilization at $cpuusage%|$perfdata\n";
      print $longoutput, "\n";
   }
}


################################################################################
# ucd_disk retrieves uc-davis partition statistics                             #
# accepts parse_func object as $args                                           #
# returns nothing                                                              #
################################################################################
sub ucd_disk {
   my ($self, $args) = @_;
   my (@output, @perfdata) = ();
   my $oid = { index  => '.1.3.6.1.4.1.2021.9.1.1',
               path   => '.1.3.6.1.4.1.2021.9.1.2',
               usage  => '.1.3.6.1.4.1.2021.9.1.9',
               inodes => '.1.3.6.1.4.1.2021.9.1.10',
             };
   
   # parse the warning:critical levels or default if not specified
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default to 80%
   $crit ||= 90;    # default to 90%
 
   # retrieve list of disk indices 
   my @index = $self->snmpbulkwalk( $oid->{index} ) or
      $self->die3("ucd_disk: no results returned for dskIndex"); 

   # loop through each disk index to check for space / inode usage
   foreach my $idx (@index) {
      # enable error checking
      $self->{ec} = 1;
 
      # snmpget the path and usage in percent
      my ($path, $usage) = $self->snmpget( $oid->{path}  . ".$idx",
                                           $oid->{usage} . ".$idx" );
    
      # test against thresholds for disk space usage
      if ($usage >= $crit) {
         push @output, "CRITICAL - Partition $path at $usage% utilization " .
                       "(threshold $crit%)";
      }
      elsif ($usage >= $warn) {
         push @output, "WARNING - Partition $path at $usage% utilization " .
                       "(threshold $warn%)";
      }
      else {
         push @output, "OK - Partition $path at $usage% utilization";
      }
      
      # clean up partition names for perfdata
      my $perfpath = $path;
      $perfpath =~ tr|^/||d;
      $perfpath =~ tr|/|_|;
      $perfpath ||= 'root';
      push @perfdata, "$perfpath=$usage";
 
      # disable error checking
      $self->{ec} = 0;

      # retrieve inode usage
      my $inode = $self->snmpget( $oid->{inodes} . ".$idx" ) or next;

      # test against thresholds for inode usage
      if ($inode >= $crit) {
         push @output, "CRITICAL - Partition $path inodes at $inode% " .
                       "utilization (threshold $crit%)";
      }
      elsif ($inode >= $warn) {
         push @output, "WARNING - Partition $path inodes at $inode% " .
                       "utilization (threshold $warn%)";
      }
      else {
         push @output, "OK - Partition $path inodes at $inode% utilization";
      }
   }

   # generate output
   if (my $critical = grep /CRITICAL/ => @output) {
      print "CRITICAL - $critical partitions high usage [@perfdata]|@perfdata\n";
      print join "\n" => @output;
      exit 2;
   }
   elsif (my $warning = grep /WARNING/ => @output) {
      print "WARNING - $warning partitions high usage [@perfdata]|@perfdata\n";
      print join "\n" => @output;
      exit 1;
   }
   else {
      my $ok = @output;
      print "OK - $ok partitions healthy [@perfdata]|@perfdata\n";
      print join "\n" => @output;
   }
}


################################################################################
# ucd_load retrieves uc-davis system load average                              #
# accepts parse_func object as $args                                           #
# returns nothing                                                              #
################################################################################
sub ucd_load {
   my ($self, $args) = @_;
   my @order = qw(user nice system idle iowait kernel hardint softint);
   my $oid = { min_1   => '.1.3.6.1.4.1.2021.10.1.3.1',
               min_5   => '.1.3.6.1.4.1.2021.10.1.3.2',
               min_15  => '.1.3.6.1.4.1.2021.10.1.3.3',
               user    => '.1.3.6.1.4.1.2021.11.50.0',
               nice    => '.1.3.6.1.4.1.2021.11.51.0',
               system  => '.1.3.6.1.4.1.2021.11.52.0',
               idle    => '.1.3.6.1.4.1.2021.11.53.0',
               iowait  => '.1.3.6.1.4.1.2021.11.54.0',
               kernel  => '.1.3.6.1.4.1.2021.11.55.0',
               hardint => '.1.3.6.1.4.1.2021.11.56.0',
               softint => '.1.3.6.1.4.1.2021.11.61.0',
             };

   # parse the warning:critical levels or default if not specified
   my ($one, $five, $fifteen) = grep /^[0-9.]+$/ => split /:/ => $args->{l};
   $one ||= 3;      # default to 3.0
   $five ||= 2;     # default to 2.0
   $fifteen ||= 1;  # default to 1.0

   # disable error checking
   # some legacy devices dont support soft interrupts
   $self->{ec} = 0;

   # perform cpu stats gathering round one
   my @run1 = $self->snmpget( @$oid{@order} );

   # error check to make sure all values arent undef
   if (scalar(grep { ! defined } @run1) == scalar(@order)) {
      $self->die3("ucd_cpu: unable to retrieve ucDavis cpu statistics");
   }

   # sleep for 6 seconds; net-snmp seems to only update every 5 seconds or so
   sleep 6;

   # perform run #2
   my @run2 = $self->snmpget( @$oid{@order} );

   # error check to make sure all values arent undef
   if (scalar(grep { ! defined } @run1) == scalar(@order)) {
      $self->die3("ucd_cpu: unable to retrieve ucDavis cpu statistics");
   }

   # capture number of elapsed jiffies between checks
   my $jiffies = do {
      for my $i (0 .. $#run1) {
         $run1[$i] and $run2[$i] or next;
         $_ += $run2[$i] - $run1[$i];
      }
      $_;
   };

   # default jiffies in 100/processor and snmp stats updated every 5 mins
   # using floating point will round to closest whole integer
   my $processors = sprintf("%.0f", $jiffies / 500);

   # if processors were found and counted, lets use the levels as multipliers
   if ($processors) {
      $one *= $processors;
      $five *= $processors;
      $fifteen *= $processors;
   }

   # retrieve load averages
   my @loadavg = $self->snmpget( @$oid{ qw(min_1 min_5 min_15) } ) or
      $self->die3("ucd_load: snmpget failed to retreive system load averages");

   # generate perfdata output
   my @perfdata = ( "load1min=$loadavg[0]", "load5min=$loadavg[1]",
                    "load15min=$loadavg[2]" );

   # compare thresholds and generate output
   if ( $loadavg[2] >= $fifteen ) {
      print "WARNING - High 15 minute load average [@loadavg]|@perfdata";
      exit 1;
   }
   elsif ( $loadavg[1] >= $five ) {
      print "WARNING - High 5 minute load average [@loadavg]|@perfdata";
      exit 1;
   }
   elsif ( $loadavg[0] >= $one ) {
      print "WARNING - High 1 minute load average [@loadavg]|@perfdata";
      exit 1;
   }
   else {
      print "OK - Load averages healthy [@loadavg]|@perfdata";
   }
}


################################################################################
# ucd_memroy retrieves uc-davis memory statistics                              #
# accepts parse_func object as $args                                           #
# returns nothing                                                              #
################################################################################
sub ucd_memory {
   my ($self, $args) = @_;
   my $ec = $self->{ec};   # store state of error checking
   my $oid = { swap_total => '.1.3.6.1.4.1.2021.4.3.0',
               swap_avail => '.1.3.6.1.4.1.2021.4.4.0',
               real_total => '.1.3.6.1.4.1.2021.4.5.0',
               real_avail => '.1.3.6.1.4.1.2021.4.6.0',
               buffers    => '.1.3.6.1.4.1.2021.4.14.0',
               cache      => '.1.3.6.1.4.1.2021.4.15.0',
             };
   
   # parse the warning:critical levels or default if not specified
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default to 80%
   $crit ||= 90;    # default to 90%

   # retrieve ucdavis memory oids
   my @vm = $self->snmpget( 
      @$oid{ qw(swap_total swap_avail real_total real_avail) } ) or
      $self->die3("ucd_memory: failed to retrieve real/swap memory oids");

   # retrieve buffers / cache oids
   # doesn't always exist so disable error checking
   $self->{ec} = 0; 
   my @bc = $self->snmpget( @$oid{ qw(buffers cache) } );
   $self->{ec} = $ec;

   # calculate total / available / used memory
   my $totalmem = $vm[0] + $vm[2];
   my $availmem = $vm[1] + $vm[3] + ($bc[0] || 0) + ($bc[1] || 0);
   my $usedmem = sprintf "%d", ($totalmem - $availmem) / $totalmem * 100;

   my $perfdata = "mem_usage=$usedmem";

   # convert @vm and @bc values from KB to MB
   $_ = sprintf("%d MB", $_ / 1024) foreach (@vm, @bc);

   my @longoutput = ( "swap_total: $vm[0]", "swap_avail: $vm[1]",
                      "real_total: $vm[2]", "real_avail: $vm[3]",
                      "buffers   : " . ($bc[0] ? $bc[0] : 'undef'),
                      "cache     : " . ($bc[1] ? $bc[1] : 'undef'),
                      '',
                    );

   # compare thresholds and generate output
   if ( $usedmem >= $crit ) { 
      print "CRITICAL - Memory usage at $usedmem% (threshold $crit%)" .
            "|$perfdata\n";
      print join "\n" => @longoutput;
      exit 2;
   }
   elsif ( $usedmem >= $warn ) {
      print "WARNING - Memory usage at $usedmem% (threshold $warn%)" .
            "|$perfdata\n";
      print join "\n" => @longoutput;
      exit 1;
   }
   else {
      print "OK - Memory usage at $usedmem%|$perfdata\n";
      print join "\n" => @longoutput;
   }
}


return 1;

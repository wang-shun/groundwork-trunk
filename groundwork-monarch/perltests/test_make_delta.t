#!/usr/local/groundwork/bin/perl

use lib ('/usr/local/groundwork/monarch/lib', 'lib');

use strict;
use warnings;

package UseMonarchHashDelta;
use base 'MonarchHashDelta';
use Test::More tests => 66;
use Data::Dumper;

BEGIN { use_ok( 'MonarchHashDelta' ); }
require_ok( 'MonarchHashDelta' ) or exit;
can_ok( 'MonarchHashDelta', 'delta' );

my $source =
  {   'host'      => {
		      'host1'=>  { 'address' => '1.2.3.4' },
		      'host2'=>  { 'address' => 1         },
		      'host3'=>  { 'address' => '1.2.3.4' },
		      'host6'=>  { 'address' => 1         },
		      'host7'=>  { 'address' => 1         },
		      'host8'=>  { 'address' => 1         },
		      'host9'=>  { 'address' => 1         },
		      'host10'=> { 'address' => 1         },
		      'host11'=> { 'address' => 1         },
		     },
      'hostgroup' => { 'hgrp1'=> {
				  'host1'=>1,
				  'host3'=>1,
				 },
		       'hgrp3'=> {
				  'host6'=>1,
				 },
		       'hgrp4'=> {
				  'host7'=>1,
				  'host8'=>1,
				  'host9'=>1,
				 },
		       'hgrp5'=> {
				  'host10'=>1,
				  'host11'=>1,
				 }
		     },
      'service'   => { 'host1'=> {
				  'serv1'=>1,
				  'serv2'=>1,
				 },
		       'host2'=> {
				  'serv1'=>1
				 }
		     },
  };
my $target =
  {   'host'      => { 'host1'=>  { 'address' => '1.2.3.4' },
		       'host3'=>  { 'address' => '2.3.4.5' },
		       'host4'=>  { 'address' => 1         },
		       'host5'=>  { 'address' => 1         },
		       'host7'=>  { 'address' => 1         },
		       'host8'=>  { 'address' => 1         },
		       'host10'=> { 'address' => 1         },
		       'host11'=> { 'address' => 1         },
		       'host12'=> { 'address' => 1         },
		     },
      'hostgroup' => { 'hgrp1'=> {
				  'host1'=>1,
				  'host3'=>1,
				 },
		       'hgrp2'=> {
				  'host4'=>1,
				 },
		       'hgrp4'=> {
				  'host7'=>1,
				  'host8'=>1,
				 },
		       'hgrp5'=> {
				  'host10'=>1,
				  'host11'=>1,
				  'host12'=>1,
				 }
		     },
      'service'   => { 'host1'=> {
				  'serv1'=>1,
				  'serv2'=>1,
				 },
		       'host3'=> {
				  'serv1'=>1,
				 },
		       'host4'=> {
				  'serv1'=>1,
				 }
		     },
  };

my $md = MonarchHashDelta->new({'source'=>$source, 'target'=>$target});
my $delta = $md->delta();

print STDERR Dumper($delta), "\n";

# main keys
is ((scalar (keys %$delta)), 4,
    "delta should have four keys"); # not always true. depends on above data.
ok ( defined($delta->{'delete'}),
     "delta should have a key called delete");
ok ( defined($delta->{ 'add'} ),
     "delta should have a key called add");
ok ( defined($delta->{'alter'}),
     "delta should have a key called alter");
ok ( defined($delta->{'exists'}),
     "delta should have a key called exists");
ok (!defined($delta->{'none'}),
    "delta should not have a key called none");

# outer objects under main keys
ok (defined($delta->{'delete'}{'host'}),
    "delta->{delete} should have a key called host");
ok (defined($delta->{'delete'}{'hostgroup'}),
    "delta->{delete} should have a key called hostgroup");
ok (defined($delta->{'delete'}{'service'}),
    "delta->{delete} should have a key called service");
ok (defined($delta->{'add'}{'host'}),
    "delta->{add} should have a key called host");
ok (defined($delta->{'add'}{'hostgroup'}),
    "delta->{add} should have a key called hostgroup");
ok (defined($delta->{'add'}{'service'}),
    "delta->{add} should have a key called service");

# detailed items under each object
ok (!defined($delta->{'delete'}{'host'}{'host3'}),
    "delta->{delete}{'host'} should not include a key host3");
ok (defined($delta->{'delete'}{'host'}{'host5'}),
    "delta->{delete}{'host'} should include a key host5");
ok (defined($delta->{'delete'}{'hostgroup'}{'hgrp2'}),
    "delta->{delete}{'hostgroup'} should include a key hgrp2");
ok (defined($delta->{'delete'}{'service'}{'host4'}),
    "delta->{delete}{'service'} should have a key host4");
ok (defined($delta->{'add'}{'host'}{'host2'}),
    "delta->{add}{'host'} should include a key host2");
ok (!defined($delta->{'add'}{'hostgroup'}{'hgrp1'}),
    "delta->{add}{'hostgroup'} should not have a key called hgrp1");
ok (!defined($delta->{'add'}{'hostgroup'}{'hgrp2'}),
    "delta->{add}{'hostgroup'} should not have a key called hgrp2");
ok (defined($delta->{'add'}{'service'}{'host2'}{'serv1'}),
    "delta->{add}{'service'}{'host2'} should include a key serv1");
# host1-serv1 already exists in the target, so no add
ok (!defined($delta->{'add'}{'service'}{'host1'}{'serv1'}),
    "delta->{add}{'service'}{'host1'} should not include a key serv1");
ok (!defined($delta->{'add'}{'service'}{'host1'}{'serv3'}),
    "delta->{add}{'service'}{'host1'} should not include a key serv3");

# need to verify and expand on these
ok (defined($delta->{'exists'}{'host'}),
    "delta->{exists} should have a key called host");
ok (defined($delta->{'exists'}{'hostgroup'}),
    "delta->{exists} should have a key called hostgroup");
ok (defined($delta->{'exists'}{'service'}),
    "delta->{exists} should have a key called service");
ok (defined($delta->{'exists'}{'host'}{'host3'}),
    "delta->{exists}{'host'} should include a key host3");
ok (!defined($delta->{'exists'}{'hostgroup'}{'hgrp2'}),
    "delta->{exists}{'hostgroup'} should not include a key hgrp2");
ok (!defined($delta->{'exists'}{'service'}{'host4'}),
    "delta->{exists}{'service'} should not have a key host4");
ok (defined($delta->{'exists'}{'host'}{'host2'}),
    "delta->{exists}{'host'} should include a key host2");
ok (defined($delta->{'exists'}{'hostgroup'}{'hgrp1'}),
    "delta->{exists}{'hostgroup'} should have a key called hgrp1");
ok (!defined($delta->{'exists'}{'hostgroup'}{'hgrp2'}),
    "delta->{exists}{'hostgroup'} should not have a key called hgrp2");
ok (defined($delta->{'exists'}{'service'}{'host2'}{'serv1'}),
    "delta->{exists}{'service'}{'host2'} should include a key serv1");
ok (!defined($delta->{'exists'}{'service'}{'host1'}{'serv3'}),
    "delta->{exists}{'service'}{'host1'} should not include a key serv3");

# verify a bug with services is fixed
ok (($delta->{'exists'}{'service'}{'host1'}{'serv1'} == 1),
    "delta->{exists}{'service'}{'host1'}{'serv1'} should be 1");
ok (($delta->{'exists'}{'service'}{'host1'}{'serv2'} == 1),
    "delta->{exists}{'service'}{'host1'}{'serv2'} should be 1");
ok (($delta->{'exists'}{'service'}{'host2'}{'serv1'} == 1),
    "delta->{exists}{'service'}{'host2'}{'serv1'} should be 1");
# same bug, hostgroups
ok (($delta->{'exists'}{'hostgroup'}{'hgrp1'}{'host1'} == 1),
    "delta->{exists}{'hostgroup'}{'hgrp1'}{'host1'} should be 1");
ok (($delta->{'exists'}{'hostgroup'}{'hgrp1'}{'host3'} == 1),
    "delta->{exists}{'hostgroup'}{'hgrp1'}{'host3'} should be 1");
ok (($delta->{'exists'}{'hostgroup'}{'hgrp3'}{'host6'} == 1),
    "delta->{exists}{'hostgroup'}{'hgrp3'}{'host6'} should be 1");


# detailed checks - will need to change if test data changes
# host adds
ok ($delta->{add}{host}{host2}{address} eq '1',
    "host2 should be listed for add with value 1");
ok (( ! defined $delta->{add}{host}{host1}),
    "host1 should be not listed for add");
ok (( ! defined $delta->{add}{host}{host3}),
    "host3 should be not listed for add");
ok ($delta->{add}{host}{host6}{address} eq '1',
    "host6 should be listed for add with value 1");
# host deletes
ok (( ! defined $delta->{delete}{host}{host1}),
    "host1 should be not listed for delete");
ok (( ! defined $delta->{delete}{host}{host2}),
    "host2 should be not listed for delete");
ok ($delta->{delete}{host}{host5}{address} eq '1',
    "host5 should be listed for delete with value 1");
# hostgroup adds
ok ($delta->{add}{hostgroup}{hgrp3}{host6} eq '1',
    "hostgroup hgrp3-host6 should be listed for add with value 1");
ok (( ! defined $delta->{add}{hostgroup}{hgrp2}{host4} ),
    "hostgroup hgrp2-host4 should not be listed for add");
ok (( ! defined $delta->{add}{hostgroup}{hgrp1}{host1} ),
    "hostgroup hgrp1-host1 should not be listed for add");
ok (( ! defined $delta->{add}{hostgroup}{hgrp1}{host3} ),
    "hostgroup hgrp1-host3 should not be listed for add");
# hostgroup deletes
ok ($delta->{delete}{hostgroup}{hgrp2}{host4} eq '1',
    "hostgroup hgrp2-host4 should be listed for delete with value 1");
ok (( ! defined $delta->{delete}{hostgroup}{hgrp3}{host6} ),
    "hostgroup hgrp3-host6 should not be listed for delete");
# service adds
ok ($delta->{add}{service}{host2}{serv1} eq '1',
    "service host2-serv1 should be listed for add with value 1");
ok (( ! defined $delta->{add}{service}{host1}{serv1} ),
    "service host1-serv1 should not be listed for add");
# service deletes
ok ($delta->{delete}{service}{host3}{serv1} eq '1',
    "service host3-serv1 should be listed for delete with value 1");
ok ($delta->{delete}{service}{host4}{serv1} eq '1',
    "service host3-serv1 should be listed for delete with value 1");
ok (( ! defined $delta->{delete}{service}{host2}{serv1} ),
    "service host2-serv1 should not be listed for delete");

# alter
ok (( defined $delta->{alter}{hostgroup} ),
    "delta->{alter}{hostgroup} should be defined");
ok (( defined $delta->{alter}{hostgroup}{hgrp3} ),
    "delta->{alter}{hostgroup}{hgrp3} should be defined");
ok (( defined $delta->{alter}{hostgroup}{hgrp3}{members} ),
    "delta->{alter}{hostgroup}{hgrp3}{members} should be defined");
ok ($delta->{alter}{hostgroup}{hgrp3}{members}{host6} eq '1',
    "hostgroup hgrp3 should have host host6 listed for alter");

# alter (cont.)
# these next two are not working yet, but need to - case where
# a host (host12) is deleted from a hostgroup.
ok (( defined $delta->{alter}{hostgroup}{hgrp5} ),
    "host12 deleted from hgrp5, so delta->{alter}{hostgroup}{hgrp5} should be defined.");
ok (( defined $delta->{alter}{hostgroup}{hgrp5}{members} ),
    "delta->{alter}{hostgroup}{hgrp5}{members} should be defined");


__END__
'alter' => {
    'hostgroup' => {
	'__Hosts not in any host group' => {
	    'members' => {
		'bolzano' => 1,
		'devnet-router-1' => 1
	    }
	},
	'Linux Servers' => {
	    'members' => {
		'zinal' => 1,
		'rhel532' => 1,
		'saturn' => 1,
		'localhost' => 1
	    }
	}
    }
},

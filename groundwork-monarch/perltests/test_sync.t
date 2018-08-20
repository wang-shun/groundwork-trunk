#!/usr/local/groundwork/bin/perl

use lib '/usr/local/groundwork/monarch/lib';

use strict;
use warnings;

package UseMonarchFoundationDelta;
use base 'MonarchFoundationDelta';
use Test::More tests => 21;
use Data::Dumper;

BEGIN { use_ok( 'MonarchFoundationDelta' ); }
require_ok( 'MonarchFoundationDelta' ) or exit;

can_ok( 'MonarchFoundationDelta', 'group' );
can_ok( 'MonarchFoundationDelta', 'monarch' );
can_ok( 'MonarchFoundationDelta', 'foundation' );
can_ok( 'MonarchFoundationDelta', 'delta' );
can_ok( 'MonarchFoundationDelta', 'doomed_services' );

can_ok( 'MonarchFoundationDelta', 'get_sync_xml' );
can_ok( 'MonarchFoundationDelta', 'update_delta' );
can_ok( 'MonarchFoundationDelta', 'get_sync_delta' );
can_ok( 'MonarchFoundationDelta', 'update_monarch_state' );
can_ok( 'MonarchFoundationDelta', 'get_direct_monarch_hosts' );
can_ok( 'MonarchFoundationDelta', 'get_direct_monarch_hostgroups' );
can_ok( 'MonarchFoundationDelta', 'get_hosts_in_monarch_hostgroups' );
can_ok( 'MonarchFoundationDelta', 'get_implicit_monarch_hostgroups' );
can_ok( 'MonarchFoundationDelta', 'get_monarch_service_state' );
can_ok( 'MonarchFoundationDelta', 'update_foundation_state' );
can_ok( 'MonarchFoundationDelta', 'get_foundation_host_state' );
can_ok( 'MonarchFoundationDelta', 'get_foundation_hostgroup_state' );


my $mfd = MonarchFoundationDelta->new();
$mfd->get_sync_delta();

my $m_test_state = $mfd->monarch();
my $f_test_state = $mfd->foundation();


#ok(eq_hash($m_test_state, $m_state),
#   "expected monarch state to equal monarch test state");
#ok(eq_hash($f_test_state, $f_state),
#   "expected foundation state to equal foundation test state");

my $expected_delta = get_expected();
my $actual_delta = $mfd->get_sync_delta();

ok(eq_hash($actual_delta, $expected_delta),
   "unexpected results in: " . Dumper($actual_delta));

my $xml = $mfd->get_sync_xml();
is($xml, "<xml/>", "expected xml to be <xml/>");



sub get_expected {

    my $expected = {
	'add' => {
	    'hostgroup' => {
		'hgrp3' => {
		    'host6' => 1
		}
	    },
	    'service' => {
		'host2' => {
		    'serv1' => 1
		}
	    },
	    'host' => {
		'host2' => 1,
		'host6' => 1
	    }
	},
	'alter' => {},
	'delete' => {
	    'hostgroup' => {
		'hgrp2' => {
		    'host4' => 1
		}
	    },
	    'service' => {
		'host4' => {
		    'serv1' => 1
		},
			'host3' => {
			    'serv1' => 1
		    }
	    },
	    'host' => {
		'host4' => 1,
		'host5' => 1
	    }
	},
	'exists' => {
	    'hostgroup' => {
		'hgrp3' => {
		    'host6' => 1
		},
			'hgrp1' => {
			    'host3' => 1,
			    'host1' => 1
		    }
	    },
	    'service' => {
		'host2' => {
		    'serv1' => 1
		},
			'host1' => {
			    'serv1' => 1,
			    'serv2' => 1
		    }
	    },
	    'host' => {
		'host2' => 1,
		'host3' => 1,
		'host1' => 1,
		'host6' => 1
	    }
	}
    };
    return $expected;
}



__END__
my $m_state =
  {   'host'      => { 'host1'=>1, 'host2'=>1, 'host3'=>1, 'host6'=>1 },
      'hostgroup' => { 'hgrp1'=>{'host1'=>1, 'host3'=>1},
		       'hgrp3'=>{'host6'=>1} },
      'service'   => { 'host1'=>{'serv1'=>1, 'serv2'=>1},
		       'host2'=>{'serv1'=>1} },
  };
my $f_state =
  {   'host'      => { 'host1'=>1, 'host3'=>1, 'host4'=>1, 'host5'=>1 },
      'hostgroup' => { 'hgrp1'=>{'host1'=>1, 'host3'=>1},
		       'hgrp2'=>{'host4'=>1} },
      'service'   => { 'host1'=>{'serv1'=>1, 'serv2'=>1},
		       'host3'=>{'serv1'=>1},
		       'host4'=>{'serv1'=>1} },
  };

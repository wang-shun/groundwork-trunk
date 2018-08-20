#!/usr/bin/perl

use lib '.';

use strict;
use warnings;

use MonarchDiscovery;
use Test::More tests => 13;

BEGIN { use_ok( 'MonarchDiscovery' ); }
require_ok( 'MonarchDiscovery' ) or exit;

my $disco = Discovery->new();


#
# flags
#
is ($disco->get_flag('save_method'), 0,
    "save_method flag should be 0");

$disco->set_flag('save_method', 1);
is ($disco->get_flag('save_method'), 1,
    "save_method flag should now be 1");

is ($disco->get_flag('remove_port'), 0,
    "remove_port flag should be 0");

isnt ($disco->get_flag('remove_port'), 42,
    "remove_port flag should not be 42");

# single out rename for testing since it shares a name with a Perl built-in
is ($disco->get_flag('rename'), 0,
    "rename flag should be 0");

$disco->set_flag('rename', 1);
is ($disco->get_flag('rename'), 1,
    "rename flag should now be 1");


#
# other methods
#
$disco->set_description("test");
is ($disco->get_description(), "test",
	"description should now be test");

# edit_method is no longer a flag
can_ok( 'Discovery', 'get_edit_method' );
can_ok( 'Discovery', 'set_edit_method' );

# check the same thing, two different ways
is ($disco->get_edit_method(), undef,
	"edit_method should be undefined");
isnt (defined($disco->get_edit_method()), 1,
	"edit_method should be undefined");


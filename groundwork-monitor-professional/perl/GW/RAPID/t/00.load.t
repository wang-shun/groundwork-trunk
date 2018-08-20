#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests the RAPID module loads when use'd

use warnings;
use strict;

use Test::More tests => 1;

# ----------- Test 1 - tests the GW::RAPID module will use ok ------------

BEGIN { use_ok( 'GW::RAPID' ) ; }

# ------------------------------------------------------------------------

diag( "Testing GW::RAPID module version $GW::RAPID::VERSION" );


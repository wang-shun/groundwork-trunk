#!/usr/local/groundwork/bin/perl -T

use lib qw{/usr/local/groundwork/tools/snmp/lib};

use strict;
use warnings;

use MIB::Check;
my $webapp = MIB::Check->new();
$webapp->run();


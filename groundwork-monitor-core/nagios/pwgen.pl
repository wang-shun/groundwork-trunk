#!/usr/local/groundwork/perl/bin/perl --
#
# pwgen.pl        Version 0.3
#
# Copyright (C) 2008-2016 GroundWork Open Source, Inc. (GroundWork)
# All rights reserved. Use is subject to GroundWork commercial license terms.
#

use strict;
use warnings;

my $password;
my $pwlength = 12;
my @chars = ('a'..'z', 'A'..'Z',0..9);

srand;
$password .= $chars[rand(@chars)] for (1 .. $pwlength);
`echo "nagiosadmin:$password" > /usr/local/groundwork/nagios/etc/htpasswd.users`;


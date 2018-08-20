# MonArch - Groundwork Monitor Architect
# MonarchCallOut.pm
#
############################################################################
# Release 2.5
# 7-Apr-2008
############################################################################
# Author: Scott Parris
#
# Copyright 2007, 2008 GroundWork Open Source, Inc. (GroundWork)  
# All rights reserved. This program is free software; you can redistribute
# it and/or modify it under the terms of the GNU General Public License
# version 2 as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
#


use strict;
use MonarchExternals;  # DSN
use CGI;  # DSN


package CallOut;

sub submit(@) {
	my $monarch_home = $_[1];
	my $results = undef;
# Enter you custom code here.

        # DSN - auto run of externals on commit
        my $query = new CGI;
        my $session_id = $query->param('CGISESSID');
        unless ($session_id) { $session_id = $query->cookie("CGISESSID") }

        my @errors = Externals->build_externals($session_id);
        #my $results = ("Externals module executed.");
        my $results = ("Externals module executed.<br>@errors");



	return $results;
}

1;

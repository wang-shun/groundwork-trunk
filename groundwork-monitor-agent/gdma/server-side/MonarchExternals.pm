# MonArch - Groundwork Monitor Architect
# MonarchExternals.pl
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
use MonarchStorProc;

my ($dbh, $err, $rows);

package Externals;

my $host_sequence_number = 0;

sub build_externals(@) {
	my $userid = $_[1];
	my @errors = ();
	my %hosts = StorProc->get_hosts();
	my $host_ref = gethostgroup();

        my %resources = StorProc->get_resources(); # DSN 10/08 for nagios resource substitution


	foreach my $host (sort keys %hosts) {
		my $dt = StorProc->datetime();
		my $head = qq(##########GROUNDWORK#############################################################################################
#GW
#GW\tgwmon_$host.cfg generated $dt by $userid from monarch.cgi
#GW
##########GROUNDWORK#############################################################################################
);
		my $body = undef;
		my $bodytmp= undef;
		my %w = ('host_id' => $hosts{$host});
		my @externals = StorProc->fetch_list_where('external_host','data',\%w);

                next if ( $#externals  < 0 ) ; # DSN 10/08 - prevents bogus warnings when you run exerternals

		## The following Change made to create even load
		## Across GDMA installations. Each GDMA installation
		## Uses its sequence number to determine when to place itself
		## in the system-wide data collection cycle.
		## Changed Jan. 30, 2008, Daniel Emmanuel Feinsmith

		++$host_sequence_number;
		$body .= "\nNumHostsInInstallation=\"" . keys(%hosts) . "\"";  # DSN 10/08 fixed by adding quotes to rhs vals
		$body .= "\nHostSequenceNumber=\"$host_sequence_number\"";  # DSN 10/08 fixed by adding quotes to rhs vals

		## End of Changes.

		foreach my $ext (@externals) { 
			$ext =~ s/\r//g;
			$body .= "\n$ext";
		}
		my @services = StorProc->fetch_list_where('services','service_id',\%w);
		my @allext = ();
		foreach my $service (@services) {
			%w = ('service_id' => $service);
			@externals = StorProc->fetch_list_where('external_service','data',\%w,'data');
			foreach my $ext (@externals) { 
				$ext =~ s/^[\n\r\s]+//;
				$ext =~ s/[\n\r\s]+$//;
				$ext =~ s/\r//g;
				push @allext,$ext;
			}
		}
		foreach my $ext (sort @allext) {	# Sort by external text
			$body .= "\n$ext\n";
		}

		if ($body) {

                        # Nagios resource macro substitutions DSN 10/08
                        foreach my $res (keys %resources) {  
                            if ($body =~ /$res/i) { $body =~ s/\$$res\$/$resources{$res}/ig } 
                        }

                        # Monarch group macro substitutions DSN 10/08
		        my %group_macros = StorProc->get_group_macros($host_ref->{$host}->{'gid'}  );
		        foreach my $mgmacro (keys %group_macros) { 
                            if ($body =~ /$mgmacro/i) { $body =~ s/\$$mgmacro\$/$group_macros{$mgmacro}{'value'}/ig } 
                        }

                        # Substitute $HOSTNAME$, $HOSTADDRESS$ and $HOSTALIAS$ DSN 10/08
                        my %host_vitals = gethostvitals($host);
                        if ($body =~ /\$HOSTNAME\$/i) { $body =~ s/\$HOSTNAME\$/$host_vitals{'hostname'}/ig }
                        if ($body =~ /\$HOSTADDRESS\$/i) { $body =~ s/\$HOSTADDRESS\$/$host_vitals{'address'}/ig }
                        if ($body =~ /\$HOSTALIAS\$/i) { $body =~ s/\$HOSTALIAS\$/$host_vitals{'alias'}/ig }


			my $file;
			if ($host_ref->{$host}->{'location'}) {
				$file = $host_ref->{$host}->{'location'}."/gwmon_$host.cfg";
			} else {
				$file = "/usr/local/groundwork/distribution/gwmon_$host.cfg";
			}
			open(FILE, "> $file") || push @errors, "Error: Unable to write $file.";
			print FILE $head.$body;
			close (FILE);
		}
	}
	return @errors;
}

sub gethostgroup {
        my @errors=();
	my $host_ref = undef;
	my ($dbhost, $database,$user,$passwd) = undef;

	open(FILE, "< /usr/local/groundwork/config/db.properties");
	while (my $line = <FILE>) {
			if ($line =~ /\s*monarch\.dbhost\s*=\s*(\S+)/) { $dbhost = $1 }
			if ($line =~ /\s*monarch\.database\s*=\s*(\S+)/) { $database = $1 }
			if ($line =~ /\s*monarch\.username\s*=\s*(\S+)/) { $user = $1 }
			if ($line =~ /\s*monarch\.password\s*=\s*(\S+)/) { $passwd = $1 }
	}
	close(FILE);
	my $dsn = "DBI:mysql:monarch:localhost";
	my $dbh = DBI->connect($dsn, $user, $passwd, {'RaiseError' => 1});
        my $sqlstmt = "select * from monarch_groups ";
        my $sth = $dbh->prepare($sqlstmt);
        $sth->execute;
	while(my @values = $sth->fetchrow_array()) {
		my $gid = $values[0];  # DSN 10/08 for monarch group subs in externals
		my $groupname = $values[1];
		my $location = $values[3];
		my $data = $values[5];
		if ($data !~ /.*prop name=\"nagios_etc\"\>\<\!\[CDATA\[\].*/) { next; }
		if (!stat($location)) {
			my @lines = `mkdir $location`
		}
		my $stmt = "select host_id, name from hosts where host_id in (select host_id from monarch_group_host where group_id = '$values[0]') order by name";
		my $sth2 = $dbh->prepare($stmt);
		$sth2->execute;
		while(my @vals = $sth2->fetchrow_array()) {
			$host_ref->{$vals[1]}->{'location'} = $location;
			$host_ref->{$vals[1]}->{'group'} = $groupname;
			$host_ref->{$vals[1]}->{'gid'} = $gid; # DSN 10/08 for monarch group subs in externals
		}
		$sth2->finish;
		my $stmt = "select hostgroup_id from monarch_group_hostgroup where group_id = '$values[0]'";
		$sth2 = $dbh->prepare($stmt);
		$sth2->execute;
		while(my @vals = $sth2->fetchrow_array()) {
			my $stmt = "select host_id, name from hosts where host_id in (select host_id from hostgroup_host where hostgroup_id = '$vals[0]')";
			my $sth3 = $dbh->prepare($stmt);
			$sth3->execute;
			while(my @host_vals = $sth3->fetchrow_array()) {
				$host_ref->{$host_vals[1]}->{'location'} = $location;
				$host_ref->{$host_vals[1]}->{'group'} = $groupname;
				$host_ref->{$host_vals[1]}->{'gid'} = $gid; # DSN 10/08 for monarch group subs in externals
			}
			$sth3->finish;
		}
		$sth2->finish;
	}
	$sth->finish;
	return $host_ref;
}

sub gethostvitals # DSN 10/08
{
     my ( $host ) = @_;
     my %host_vitals = ();
     my ($dbhost, $database,$user,$passwd) = undef;
     open(FILE, "< /usr/local/groundwork/config/db.properties");
     while (my $line = <FILE>)
     {
        if ($line =~ /\s*monarch\.dbhost\s*=\s*(\S+)/) { $dbhost = $1 }
        if ($line =~ /\s*monarch\.database\s*=\s*(\S+)/) { $database = $1 }
        if ($line =~ /\s*monarch\.username\s*=\s*(\S+)/) { $user = $1 }
        if ($line =~ /\s*monarch\.password\s*=\s*(\S+)/) { $passwd = $1 }
     }
     close(FILE);
     my $dsn = "DBI:mysql:monarch:localhost";
     my $dbh = DBI->connect($dsn, $user, $passwd, {'RaiseError' => 1});
     my $hoststmt = "select name, address, alias from hosts where name = '$host'";
     my $sth = $dbh->prepare ($hoststmt);
     $sth->execute;
     while (my @values = $sth->fetchrow_array())
     {
        $host_vitals{'hostname'} = $values[0];
        $host_vitals{'address'} = $values[1];
        $host_vitals{'alias'} = $values[2];
     }
     $sth->finish;
     return %host_vitals;
}


1;


#!/usr/local/groundwork/perl/bin/perl --
#
#	Copyright (C) 2009 GroundWork Open Source Solutions, Inc. (GroundWork)  
#
#   This program is free software; you can redistribute it and/or modify
#   it under the terms of version 2 of the GNU General Public License 
#   as published by the Free Software Foundation.
#
#   This program is distributed in the hope that it will be useful,
#   but WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#   GNU General Public License for more details.
#
#   You should have received a copy of the GNU General Public License
#   along with this program; if not, write to the Free Software
#   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
#
use Time::Local;
use strict;
my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
my $dayofmonth=sprintf("%02d",$mday);
my $nmonfile = "nmon.file.$dayofmonth";
my $nmon_tmp_dir = "/home/gwrk/log";
my $nmon_program = "/home/gwrk/libexec/nmon_x86_rhel4";
my $nmon_program_start = "$nmon_program -ft -s 600 -F $nmon_tmp_dir/$nmonfile -c 144";


# Check to make sure nmon is running. If not, start it.
my @lines = `ps -ef | grep \"$nmon_program_start\" | grep -v grep` ;
my $nmon_running = 0;
foreach my $line (@lines) {
	if ($line =~ /$nmon_program/) {
		$nmon_running = 1;
		last;
	}
}
if (!$nmon_running) {	# If nmon isn't running, start it.
	@lines = `$nmon_program_start`;
}

exit;

#!/usr/local/groundwork/perl/bin/perl
#
#   Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)  
#   All rights reserved. Use is subject to GroundWork commercial license terms.
#

##
##	Global vars.
##

my $pipe_open = 0;
my $nagios_spool_filename = "/usr/local/groundwork/nagios/var/spool/nagios.cmd";
my $local_spool_filename = "/usr/local/groundwork/common/var/log/syslog-ng/syslog2nagios.spool";

sub local_log
{
	my $line = shift(@_);
	open (STDOUT, ">-") || die "cannot open STDOUT!";
	syswrite(STDOUT, $line, length($line));
	syswrite(STDOUT, "\n", 1);
	close(STDOUT);
}

##
## spool_nagios_command
##
## Send a command to the nagios command pipe, or, if it doesn't exist, spool it.
##

sub spool_nagios_command
{
	my $line = shift(@_);

	local_log("spool_nagios_command: top");
	if (!$pipe_open)
	{
		local_log("pipe is not open");
		if (-e $nagios_spool_filename)
		{
			local_log("opening pipe");
			$pipe_open = open (NAGIOS_PIPE, ">>$nagios_spool_filename");
			if ($pipe_open)
			{
				local_log("opened pipe");
				if (-e $local_spool_filename)
				{
					local_log("dumping spool file to nagios pipe");
					open(EMPTYSPOOL, "<$local_spool_filename");
					while (my $l = <EMPTYSPOOL>)
					{
						syswrite(NAGIOS_PIPE, $l, length($l));
						syswrite(NAGIOS_PIPE, "\n", 1);
					}
					close(EMPTYSPOOL);
					local_log("deleting spool file");
					unlink($local_spool_filename);
				}
			}
			else
			{
				local_log("Failure to open nagios spool file.");
			}
		}
	}	

	local_log("checking if pipe is open now");
	if ($pipe_open)
	{
		local_log("writing to nagios pipe: $line");
		my $written = syswrite(NAGIOS_PIPE, $line, length($line));
		if ($written == length($line))
		{
			syswrite(NAGIOS_PIPE, "\n", 1);
		}
		else
		{
			local_log("failure in writing to nagios command pipe, spooling entry.");
			close(NAGIOS_PIPE);
			$pipe_open = 0;
			local_spool($line);
		}
	}
	else
	{
		local_log("sending to local_spool");
		local_spool($line);
	}
}

##
##	local_spool
##
##	Spool to a local intermediate file until we can open the nagios command pipe again.
##

sub local_spool
{
	my $line = shift(@_);

	local_log("in local_spool: $line");
	if (open(SPOOL_FILE, ">>$local_spool_filename"))
	{
		my $written = syswrite(SPOOL_FILE, $line, length($line));
		if ($written == length($line))
		{
			local_log("spooled line: $line");
		}
		else
		{
			local_log("unable to write to spool file");
		}
		close (SPOOL_FILE);
	}
	else
	{
		local_log("unable to open local spool file");
	}
}

sub normalize_command($)
{
        my $string = shift;
	$string =~ s/:/./g;
        return $string;
}

##
## Loop on input from STDIN
##

local_log("unlinking local spool file"); unlink($local_spool_filename); local_log("waiting for input from syslog-ng"); open (STDIN, "-") || die "cannot open STDIN!"; while (my $line = <STDIN>) {
	local_log("got line: $line");
	$line = normalize_command($line);
	spool_nagios_command($line);
	local_log("re-entering loop");
}
local_log("exiting");



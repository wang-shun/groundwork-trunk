#!/usr/bin/perl

##
##	cacti_status.pl
##	Copyright (C) 2008, Groundwork Open Source, Inc.
##
##	Determine install/running status of cacti, and return
##	a code indicating the result.
##
##	History
##		6/26/08	Created	Daniel Emmanuel Feinsmith

use Getopt::Std;

#
#	Status constants.
#

my $STATUS_GOOD			= 0;
my $STATUS_NOT_INSTALLED	= 1;
my $STATUS_CORRUPT_INSTALLATION	= 2;
my $STATUS_NOT_RUNNING		= 3;
my @status_text =	(
				"Good.",
				"RPM Package Not Installed.",
				"Corrupt Installation.",
				"Package Installed, Not Running."
			);

#
#	Globals Variables.
#

my %options=();
my $verbose=1;
my $status = $STATUS_GOOD;

#
#	Parse command line arguments.
#

getopts("qh", \%options);
if (defined $options{h})
{
	help();
}
else
{
	if (defined $options{q})
	{
		$verbose = 0;
	}

	$status = cacti_status();
}

if ($verbose)
{
	print "STATUS: $status_text[$status]\n";
}

exit($status);

#
#	Show help.
#

sub help()
{
	print "Usage: cacti_status -q -h\n";
	print "       -q    Quiet. Don't display status on stdout.\n";
	print "       -h    Help. Show this help display.\n";
	print "\n";
	print "Return code is one of the following:\n";
	my $i=0;
	foreach(@status_text)
	{
		print "       $i     $_\n";
		$i = $i + 1;
	}
}

#
#	Get NMS Component Status.
#

sub cacti_status()
{
	$rpm_package_prefix = 'groundwork-nms-cacti';
	$gw_home = '/usr/local/groundwork';
	$nms_home = $gw_home . '/nms';
	$component_home = $nms_home . '/applications/cacti';

	#
	#	First, check to see if the package is installed.
	#

	`rpm -qa|grep $rpm_package_prefix`;
	$package_installed = ($? == 0 ? 1 : 0);
	if (!$package_installed)
	{
		return($STATUS_NOT_INSTALLED);
	}

	#
	#	Next, check to see if the component
	#	is operational.
	#

	# Check to see if it is installed correctly.

	if ( ! (-e "$component_home/poller.php" ))
	{
		return($STATUS_CORRUPT_INSTALLATION);
	}

	# Check to see if it is in the crontab.

	`crontab -u nagios -l | grep poller`;
	$component_in_crontab = ($? == 0 ? 1 : 0);

	if ($component_in_crontab)
	{
		return($STATUS_GOOD);
	}
	else
	{
		return($STATUS_NOT_RUNNING);
	}
}

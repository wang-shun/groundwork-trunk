#!/usr/local/groundwork/bin/perl --
#writenmonrrd.pl $LASTCHECK$ $HOST$ $SERVICE$ '$STATUSTEXT$' '$LABEL1$' '$VALUE1$'
#
use strict;
use Time::Local;
my $log = "/usr/local/groundwork/nagios/eventhandlers/writenmonrrd.log";
my $rrdtool = "/usr/local/groundwork/bin/rrdtool";
my $lastchecktime = $ARGV[0];
my $host = $ARGV[1];
my $service = $ARGV[2];
my $statustext = $ARGV[3];
my $rrdtemplate = $ARGV[4];
my $perfdata = $ARGV[5];
my $rrdname = "/usr/local/groundwork/rrd/$host\_$service.rrd";

my %months = (
	JAN => 0,
	FEB => 1,
	MAR => 2,
	APR => 3,
	MAY => 4,
	JUN => 5,
	JUL => 6,
	AUG => 7,
	SEP => 8,
	OCT => 9,
	NOV => 10,
	DEC => 11,
  );


if (!$lastchecktime or !$host or !$service or !$perfdata) {
	die "Requried parameters not provided.\n";
}

my ($nmonhr,$nmonmin,$nmonsec,$nmonmday,$nmonmon,$nmonyear) = $statustext=~/from\s(\d\d)\:(\d\d)\:(\d\d)\s(\d+)\-(\S+)\-(\d\d\d\d)/;
my $today=sprintf("%04d%02d%02d",$nmonyear,$months{$nmonmon}+1,$nmonmday);


# Write nmon RRD
$nmonsec=0;		# may want to remove this

my $nmonepochtime=timelocal($nmonsec,$nmonmin,$nmonhr,$nmonmday,$months{$nmonmon},$nmonyear);
my $result = `$rrdtool update -t $rrdtemplate $rrdname $nmonepochtime:$perfdata`; 

exit;
sub check_dir {
	my $dir = shift;
	if (!chdir("$dir")) { 
		mkdir("$dir") or error_die("Can't create directory $dir");
		#my @line = `chown nagios.nagios $dir`;
		my @line = `chmod 775 $dir`;

	}
	return;
}
sub error_die {
	my $msg = shift;
	print LOG $msg;
	#die $msg;
	return;
}

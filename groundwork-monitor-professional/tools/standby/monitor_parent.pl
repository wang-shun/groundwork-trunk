#!/usr/bin/perl -w
#!/usr/local/groundwork/perl/bin/perl -w

#
# Copyright (c) 2000-2010 GroundWork Open Source Inc.
#
#	check a distant server for running Nagios
#	host/service checks ON/OFF; notifications ON/OFF dependent on distant Nagios state
#	accepts as arguments a distant host IP to be checked; a distant user whose key matches our Nagios key
#	Usage  monitor_parent <hostip> <user>
#

my $parent_ip = $ARGV[0];
my $remote_user = $ARGV[1];

my $key = "/home/nagios/.ssh/id_dsa";
my $port = "22";
my $timeout = "10";
my $libexec_path = "/usr/local/groundwork/nagios/libexec";
my $CommandFile = "/usr/local/groundwork/nagios/var/spool/nagios.cmd";
my $datetime;
my $command;
my $line;
my $flag = "OFF";

print "checking nagios alive\n";
$command = "$libexec_path/check_by_ssh -p $port -H $parent_ip -t $timeout -i $key -l $remote_user -C \"libexec/check_nagios -F /usr/local/groundwork/nagios/var/status.log -e 5 -C .nagios.bin\"";
print "command = $command\n";

# Loops maximum 3 times while the status is CRITICAL

for ( $i = 0; $i < 3 ; $i++ ) {
	$status = `$command`;
	print "status = $status\n";
	if( $status =~ /OK/) {
		last;
	}
	sleep 10;
}

if ($status =~ /CRITICAL|Remote command execution failed/) {
  $flag = "ON";
}

print "flag = $flag\n";

$datetime = `date +%s`;
chomp $datetime;

$command = ( $flag =~ /ON/ ? "ENABLE_NOTIFICATIONS" : "DISABLE_NOTIFICATIONS" );
$status = `/bin/echo -e "[$datetime]" '$command;$datetime' >> $CommandFile`;
if ($status) {
	print "Error $status returned on writing pipe\n";
	exit 2;
}
#$command = ( $flag =~ /ON/ ? "START_EXECUTING_SVC_CHECKS" : "STOP_EXECUTING_SVC_CHECKS" );
#$status = `/bin/echo -e "[$datetime]" '$command;$datetime' >> $CommandFile`;
#if ($status) {
#	print "Error $status returned on writing pipe\n";
#	exit 2;
#}
#$command = ( $flag =~ /ON/ ? "START_EXECUTING_HOST_CHECKS" : "STOP_EXECUTING_HOST_CHECKS" );
#$status = `/bin/echo -e "[$datetime]" '$command;$datetime' >> $CommandFile`;
#if ($status) {
#	print "Error $status returned on writing pipe\n";
#	exit 2;
#}
#$command = ( $flag =~ /ON/ ? "START_ACCEPTING_PASSIVE_HOST_CHECKS" : "STOP_ACCEPTING_PASSIVE_HOST_CHECKS" );
#$status = `/bin/echo -e "[$datetime]" '$command;$datetime' >> $CommandFile`;
#if ($status) {
#	print "Error $status returned on writing pipe\n";
#	exit 2;
#}
#$command = ( $flag =~ /ON/ ? "START_ACCEPTING_PASSIVE_SVC_CHECKS" : "STOP_ACCEPTING_PASSIVE_SVC_CHECKS" );
#$status = `/bin/echo -e "[$datetime]" '$command;$datetime' >> $CommandFile`;
#if ($status) {
#	print "Error $status returned on writing pipe\n";
#	exit 2;
#}

exit 0;

#!/usr/local/groundwork/perl/bin/perl -w --

my $insert = 1;
if (! open (CRON, '-|', '/usr/bin/crontab -u nagios -l')) {
    print "ERROR:  Cannot run crontab for the nagios user to read the existing content!\n";
    exit 1;
}
my @crontab = <CRON>;
close CRON;
for (@crontab) {
    next if /^\s*#/;
    if (/cacti_cron/) {
        $insert = 0;
    }
}
if ($insert) {
    push @crontab, "*/5 * * * * /usr/local/groundwork/common/bin/cacti_cron.sh > /dev/null 2>&1\n";
    if (! open (CRON, '|-', '/usr/bin/crontab -u nagios -')) {
        print "ERROR:  Cannot run crontab for the nagios user to add the find_cacti_graphs line!\n";
        exit 1;
    }
    print CRON @crontab;
    close CRON;
}

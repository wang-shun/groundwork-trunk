#!/bin/bash

echo "Installing cron jobs"
# See http://www.linuxsecurity.com/content/view/115462/151/
# on "Safely Creating Temporary Files in Shell Scripts".
# To avoid using a temporary file, and to simplify the pattern matching,
# we just use an embedded Perl script here.
/usr/local/groundwork/perl/bin/perl << 'EOF'
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
    if (/twoway_helpdesk.pl/) {
	$insert = 0;
    }
}
if ($insert) {
    push @crontab, "*/5 * * * * /usr/local/groundwork/helpdesk/bin/twoway_helpdesk.pl \n";
    if (! open (CRON, '|-', '/usr/bin/crontab -u nagios -')) {
        print "ERROR:  Cannot run crontab for the nagios user to add the twoway_helpdesk.pl line!\n";
        exit 1;
    }
    print CRON @crontab;
    close CRON;
}
EOF


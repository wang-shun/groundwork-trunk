#!/bin/bash

# For safety's sake, put in place what should be a safe command execution path for the superuser.
PATH=/sbin:/bin:/usr/sbin:/usr/bin

gw_home=/usr/local/groundwork
echo "Applying patches to NMS 2.1.2 ..."

if [ -f "$gw_home/nms/applications/cacti/include/auth.php" ]; then
    echo "Patching Cacti"

    cp $gw_home/nms/applications/cacti/include/auth.php $gw_home/nms/applications/cacti/include/auth.php.backup
    patch -d $gw_home/nms/applications/cacti/include < cacti-2.1.2.patch

    cp $gw_home/nms/applications/cacti/utilities.php $gw_home/nms/applications/cacti/utilities.php.backup
    patch -d $gw_home/nms/applications/cacti/ < nms-2.1.2-utilities.php.patch

    cp $gw_home/nms/applications/cacti/graph_view.php $gw_home/nms/applications/cacti/graph_view.php.backup
    patch -d $gw_home/nms/applications/cacti/ < nms-2.1.2-graph_view.php.patch

    cp $gw_home/nms/applications/cacti/graph.php $gw_home/nms/applications/cacti/graph.php.backup
    patch -d $gw_home/nms/applications/cacti/ < nms-2.1.2-graph.php.patch

    cp $gw_home/nms/applications/cacti/graph_image.php $gw_home/nms/applications/cacti/graph_image.php.backup
    patch -d $gw_home/nms/applications/cacti/ < nms-2.1.2-graph_image.php.patch

    cp $gw_home/nms/applications/cacti/plugins/thold/thold_graph.php $gw_home/nms/applications/cacti/plugins/thold/thold_graph.php.backup
    patch -d $gw_home/nms/applications/cacti/plugins/thold/ < nms-2.1.2-thold_graph.php.patch

    cp $gw_home/nms/applications/cacti/plugins/weathermap/weathermap-cacti-plugin.php $gw_home/nms/applications/cacti/plugins/weathermap/weathermap-cacti-plugin.php.backup
    patch -d $gw_home/nms/applications/cacti/plugins/weathermap < nms-2.1.2-weathermap-cacti-plugin.php.patch

    cp $gw_home/nms/applications/cacti/plugins/discovery/discover.php $gw_home/nms/applications/cacti/plugins/discovery/discover.php.backup
    patch -d $gw_home/nms/applications/cacti/plugins/discovery < nms-2.1.2-discover.php.patch
fi

if [ -f "$gw_home/nms/applications/nedi/html/index.php" ]; then
    echo "Patching NeDi"
    cp $gw_home/nms/applications/nedi/html/index.php $gw_home/nms/applications/nedi/html/index.php.backup
    patch -d $gw_home/nms/applications/nedi/html < nedi-2.1.2.patch
fi

echo "Patching NMS Apache configuration"
cp $gw_home/nms/tools/httpd/conf/httpd.conf $gw_home/nms/tools/httpd/conf/httpd.conf.backup

(echo "g/TKTAuthLoginURL/d"; echo 'wq') | ex -s $gw_home/nms/tools/httpd/conf/httpd.conf
patch -d $gw_home/nms/tools/httpd/conf<httpd-2.1.2.patch

echo "Restarting NMS Apache"
/etc/init.d/nms-httpd stop
/etc/init.d/nms-httpd start

echo "Installing NMS-Foundation feeder scripting"
cp find_cacti_graphs /usr/local/groundwork/foundation/feeder/
chmod 755            /usr/local/groundwork/foundation/feeder/find_cacti_graphs
chown nagios:nagios  /usr/local/groundwork/foundation/feeder/find_cacti_graphs

echo "Installing logrotate configuration"
cp groundwork-nms /etc/logrotate.d/
chmod 644         /etc/logrotate.d/groundwork-nms
chown root:root   /etc/logrotate.d/groundwork-nms

echo "Installing cron jobs"
# See http://www.linuxsecurity.com/content/view/115462/151/
# on "Safely Creating Temporary Files in Shell Scripts".
# To avoid using a temporary file, and to simplify the pattern matching,
# we just use an embedded Perl script here.
$gw_home/perl/bin/perl << 'EOF'
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
    if (/find_cacti_graphs/) {
	$insert = 0;
    }
}
if ($insert) {
    push @crontab, "10 * * * * /usr/local/groundwork/foundation/feeder/find_cacti_graphs < /dev/null >> /usr/local/groundwork/foundation/container/logs/find_cacti_graphs.log 2>&1\n";
    if (! open (CRON, '|-', '/usr/bin/crontab -u nagios -')) {
        print "ERROR:  Cannot run crontab for the nagios user to add the find_cacti_graphs line!\n";
        exit 1;
    }
    print CRON @crontab;
    close CRON;
}
EOF

echo "Stopping Foundation/Portal"
$gw_home/ctlscript.sh stop gwservices

echo "Installing NMS Portlet application"
cp groundwork-nms-2.1.2.war $gw_home/foundation/container/webapps/jboss/jboss-portal.sar

echo "Starting Portal/Foundation"
$gw_home/ctlscript.sh start gwservices


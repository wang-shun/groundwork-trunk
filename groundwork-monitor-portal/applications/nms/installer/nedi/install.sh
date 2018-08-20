#!/bin/bash
echo "This is the NeDi integration install script. WARNING: this installer restarts the GroundWork portal!"
read -p "Press Enter to install. Control-C to exit."
cp -r WEB-INF /usr/local/groundwork/nedi/
cp -r META-INF /usr/local/groundwork/nedi/
cp login-redirect.jsp /usr/local/groundwork/nedi/
ln -s /usr/local/groundwork/nedi/ /usr/local/groundwork/foundation/container/webapps/nedi.war
expr="^Include.+conf/groundwork/"
line="Include /usr/local/groundwork/apache2/conf/groundwork/*.conf"
httpd_conf="/usr/local/groundwork/apache2/conf/httpd.conf"
if [ ! `grep -q $expr $httpd_conf` ]; then echo $line >> $httpd_conf; fi
if [ ! -d  /usr/local/groundwork/apache2/conf/groundwork  ]; then mkdir /usr/local/groundwork/apache2/conf/groundwork;chown nagios.nagios  /usr/local/groundwork/apache2/conf/groundwork; fi
line="ProxyPass /nedi/    http://localhost:8080/nedi/"
nedi_httpd_conf=/usr/local/groundwork/apache2/conf/groundwork/nedi_httpd.conf
echo $line > $nedi_httpd_conf
cp -p ./nedi.properties /usr/local/groundwork/config/nedi.properties
chown nagios.nagios /usr/local/groundwork/config/nedi.properties
gw_home=/usr/local/groundwork
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
    if (/nedi.pl/) {
	$insert = 0;
    }
}
if ($insert) {
    push @crontab, "0 4,8,12,16,20 * * * (/usr/local/groundwork/perl/bin/perl /usr/local/groundwork/nedi/nedi.pl -po ; /usr/local/groundwork/nedi/extract_nedi.pl )> /dev/null 2>&1\n";
    push @crontab, "0 0 * * * /usr/local/groundwork/perl/bin/perl /usr/local/groundwork/nedi/nedi.pl -pob > /dev/null 2>&1\n";
    if (! open (CRON, '|-', '/usr/bin/crontab -u nagios -')) {
        print "ERROR:  Cannot run crontab for the nagios user to add the nedi.pl lines!\n";
        exit 1;
    }
    print CRON @crontab;
    close CRON;
}
EOF
res=`find /usr/local/groundwork/backup* -name nedi.conf`
lres=${#res}
if [ "$lres" -gt 0 ]; then echo "WARNING: At least one backup nedi.conf file was found. You may need to  merge the database
 credentials into /usr/local/groundwork/nedi/nedi.conf"; read -p "Press Enter to continue"; 
fi
echo "If this is the first install of NeDi, you will only need to initialize the database with /usr/local/groundwork/nedi/nedi.pl -i.  If this is an upgrade then you should consider first saving some tables that contain historical information and then replaying them after the initialization step.  Refer to http://www.nedi.ch/install:general_upgrade_procedure for more information. "; read -p "Hit Enter to continue...";

/etc/init.d/groundwork stop gwservices
/etc/init.d/groundwork restart apache
/etc/init.d/groundwork start gwservices
echo "Done with nedi integration install. You should be able to access Nedi from the Advanced tab in GroundWork."

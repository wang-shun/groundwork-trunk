%define prefix /usr/local/groundwork
%define filelist %{prefix}/filelist
%define version 5.0
%define release 0.sles10
Summary: GroundWork Monitor.
Name: groundwork-monitor-core
Version: %{version}
Release: %{release}
License: GPL
Group: System Environment/Base
Source: %{name}-%{version}-%{release}.tar.gz
BuildRoot: %{_tmppath}/%{name}
Prefix: %{prefix}
#Requires: MySQL-server-pro >= 5.0.15, MySQL-client-pro >= 5.0.15, MySQL-client-pro >= 5.0.15 
AutoReqProv: no
%description 
GroundWork Monitor is a comprehensive IT infrastructure availability and performance monitoring system based on integrated
and enhanced open source software. GroundWork Monitor leverages the functionality of Nagios, an open source IT monitoring
tool,and transforms it into an enterprise-ready IT management solution by adding functional enhancements as well as deployment services and ongoing support.
This easy-to-use monitoring system lets you quickly identify and resolve system outages or performance slowdowns before they significantly impact business processes or customers. Often, GroundWork Monitor can detect a problem before it causes an outage.If you require an advanced or specialized monitoring solution, or simply prefer professional deployment of GroundWork Monitor, GroundWork's packaged deployment service, GroundWork Install, will deliver a complete design, installation and configuration of your monitoring system.

%prep
%setup -q 

%install
rm -rf %{_tmppath}/%{name}
mkdir %{_tmppath}/%{name}/ 
mkdir %{_tmppath}/%{name}/etc 
mkdir %{_tmppath}/%{name}/usr
mkdir %{_tmppath}/%{name}/usr/local
mkdir %{_tmppath}/%{name}%{prefix}

cp -r usr %{_tmppath}/%{name}/

find . -type d | sed '1,2d;s,^\.,\%attr(-\,nagios\,nagios) \%dir ,' >  %{filelist}
find . -type f | sed 's,^\.,\%attr(-\,nagios\,nagios) ,' >>  %{filelist}
find . -type l | sed 's,^\.,\%attr(-\,nagios\,nagios) ,' >>  %{filelist}

%pre
# Check arch
%ifarch  i586 i686
# Good
%else
/bin/echo  "Sorry, but current release support only i586 i686 Architecture .... Aborting installation."
exit 1
%endif
# Check distribution 
# TODO: Narrow check to exact Distro
if [ -f /etc/redhat-release ] ; then
	DIST='RedHat'
elif [ -f /etc/SuSE-release ] ; then
	DIST='SuSE'
	VERSION=$(/bin/fgrep "VERSION" /etc/SuSE-release|awk '{print $3}')
elif [ -f /etc/mandrake-release ] ; then
	DIST='Mandrake'
%logmsg "Sorry, but current release does not support Mandrake.... Aborting installation."
elif [ -f /etc/debian_version ] ; then
	DIST="Debian"
%logmsg "Sorry, but current release does not support Debian... Aborting installation."
fi 

# Check /etc/hosts
# TODO : check for #localhost 
rel=$(/bin/fgrep "127.0.0.1" /etc/hosts|awk '{print $2}')
if ! [ $rel = "localhost" ]; then
echo "Please edit /etc/hosts file..."
echo "It must be : 127.0.0.1    localhost       localhost.localdomain"
echo "But you have..."
/bin/fgrep "127.0.0.1" /etc/hosts
exit 1
fi

# Reset NIS Client during installation
/bin/domainname ""

#create groups for nagios command
/bin/echo "Creating groups nagios and nagioscmd..."
/usr/sbin/groupadd nagios &>/dev/null
/usr/sbin/groupadd nagioscmd &>/dev/null
if ! /usr/bin/id nagios &>/dev/null; then
        /usr/sbin/useradd -r -d %{prefix}/users/nagios -s /bin/bash -g "nagios" nagios || \
%logmsg "Unexpected error adding user \"nagios\". Aborting installation."
fi

if ! /usr/bin/id nobody &>/dev/null; then
        /usr/sbin/useradd -r -d %{prefix}/users/nobody -s /bin/bash -c "nagios" nobody
        /usr/sbin/usermod -G nagios nobody &>/dev/null
fi

if [ -f /etc/SuSE-release ] ; then
	/usr/sbin/groupmod -A nagios nagios
	/usr/sbin/groupmod -A nobody nagios
	/usr/sbin/groupmod -A nobody nagioscmd
	/usr/sbin/groupmod -A nagios nagioscmd
fi

%post 

# Check ip
ip=$(ifconfig $interface | awk '$1=="inet" { print $2; }' |sed 's/addr://'| sed 's/127.0.0.1//')
# Just in case if user have more then one network interface, we will use first one
ip=$(echo $ip |awk '{ print $1; }')
echo $ip
sed -e 's/localhost/'$ip'/g' %{prefix}/apache2/conf/httpd.conf > %{prefix}/apache2/conf/httpd.conf.SAVE
mv %{prefix}/apache2/conf/httpd.conf.SAVE %{prefix}/apache2/conf/httpd.conf

/bin/echo "Creating user directories and bash_profiles for user nagios ..."
#Create Home directories
/bin/mkdir -p %{prefix}/users &>/dev/null
/bin/mkdir -p %{prefix}/users/nagios &>/dev/null
/bin/mkdir -p %{prefix}/users/nobody &>/dev/null

#Create .bashrc for nagios 
bashrc=%{prefix}/users/nagios/.bashrc
/bin/echo "# .bashrc" > $bashrc
/bin/echo "# Get the aliases and functions" >> $bashrc
/bin/echo "GW_HOME=/usr/local/groundwork"       >> $bashrc
/bin/echo "LD_LIBRARY_PATH=/usr/local/groundwork/lib" >> $bashrc
/bin/echo "LD_RUN_PATH=/usr/local/groundwork/lib"       >> $bashrc
/bin/echo  JAVA_HOME=$JAVA_HOME >> $bashrc
/bin/echo "PATH=$GW_HOME/bin:$GW_HOME/sbin:$GW_HOME/nagios/bin:$JAVA_HOME/bin:$PATH:$HOME/bin" >> $bashrc
/bin/echo "export GW_HOME" >> $bashrc
/bin/echo "export JAVA_HOME" >> $bashrc
/bin/echo "export MAVEN_HOME" >> $bashrc
/bin/echo "export PATH LD_LIBRARY_PATH LD_RUN_PATH" >> $bashrc
/bin/echo "unset USERNAME" >> $bashrc
/bin/cp %{prefix}/users/nagios/.bashrc %{prefix}/users/nobody/.bashrc

/bin/echo "Setting up the boot sequence for GroundWork Monitor modules..."
/bin/echo "Add syslog-ng ..."
/bin/cp %{prefix}/etc/syslog-ng/syslog-ng.init /etc/init.d/syslog-ng 
/bin/chmod +x  /etc/init.d/syslog-ng 
/sbin/chkconfig --add syslog-ng 

/bin/echo "Add nagios ..."
/bin/cp %{prefix}/nagios/etc/nagios.initd /etc/init.d/nagios &>/dev/null
/bin/chmod +x /etc/init.d/nagios &>/dev/null
/bin/ln -sf /etc/init.d/nagios /etc/rc.d/rc5.d/S90nagios &>/dev/null
/bin/ln -sf /etc/init.d/nagios /etc/rc.d/rc3.d/S90nagios &>/dev/null
/sbin/chkconfig --add nagios &>/dev/null

/bin/echo "Add nsca ..."
/bin/cp %{prefix}/etc/nsca.init /etc/init.d/nsca &>/dev/null
/bin/chmod +x /etc/init.d/nsca &>/dev/null
/sbin/chkconfig --add nsca &>/dev/null

/bin/echo "Add Apache2 ..."
file="/etc/init.d/apache2"
file1="/etc/init.d/apache2-save"
if [ -a "$file" ] ; then
/bin/mv "$file" "$file1" &>/dev/null
fi

/bin/echo "Add  snmptt ..."
/bin/cp %{prefix}/etc/snmpttd.init /etc/init.d/snmptt &>/dev/null
/bin/chmod +x /etc/init.d/snmptt &>/dev/null
/sbin/chkconfig --add snmptt &>/dev/null

/usr/bin/killall -9 httpd &>/dev/null
/bin/ln -sf %{prefix}/apache2/bin/apachectl /etc/init.d/gwhttpd &>/dev/null
/bin/ln -sf /etc/init.d/gwhttpd /etc/rc.d/rc5.d/S90gwhttpd &>/dev/null
/bin/ln -sf /etc/init.d/gwhttpd /etc/rc.d/rc3.d/S90gwhttpd &>/dev/null
/sbin/chkconfig --add gwhttpd &>/dev/null

#Logos link for Kiwi
/bin/ln -sf %{prefix}/nagios/share/images/logos %{prefix}/apache2/htdocs/sv/images/icons &>/dev/null

/bin/echo "Setting up the permissions..."
/bin/chown -R nagios:nagios %{prefix}
/bin/chown -R nobody:nagios %{prefix}/nagios/var/rw &>/dev/null
/bin/mkdir -p %{prefix}/collage/feeder/log &>/dev/null
/bin/mkdir -p %{prefix}/nagios/var/log &>/dev/null
/bin/mkdir -p %{prefix}/nagios/var/spool &>/dev/null
/bin/mkdir -p %{prefix}/rrd &>/dev/null
/bin/mkdir -p %{prefix}/nagios/rrd &>/dev/null
/bin/chown nagios.nagios %{prefix}/nagios/rrd
/bin/chown nagios.nagios %{prefix}/rrd
/bin/chmod u+rwx %{prefix}/rrd
/bin/chown nagios.nagioscmd %{prefix}/nagios/var/spool
/bin/chmod u+rwx %{prefix}/nagios/var/spool
/bin/chmod g+rwx %{prefix}/nagios/var/spool
/bin/chmod g+s %{prefix}/nagios/var/spool
/bin/chmod uog+x %{prefix}/nagios/libexec/*
/bin/chmod 775 %{prefix}/nagios/etc &>/dev/null
/bin/chmod 664 %{prefix}/nagios/etc/* &>/dev/null
/bin/chmod -R 775 %{prefix}/nagios/etc/private &>/dev/null
/bin/chmod g+w %{prefix}/nagios/etc/*.cfg
/bin/chmod g+w %{prefix}/nagios/etc/private/*.cfg

#Apache2 permissions
/bin/chmod -R +x %{prefix}/apache2/bin/
/bin/chmod -R oug+x %{prefix}/apache2/cgi-bin
/bin/chmod -R oug+x %{prefix}/apache2/htdocs/api_sample3.pl &>/dev/null
/bin/mkdir -p %{prefix}/apache2/htdocs/rrd &>/dev/null
/bin/chown nobody.nagios %{prefix}/apache2/htdocs/rrd  &>/dev/null

#Plugins permissions
/bin/chown root.nagios %{prefix}/nagios/libexec/check_icmp
/bin/chown root.nagios %{prefix}/nagios/libexec/check_dhcp
/bin/chmod u+s %{prefix}/nagios/libexec/check_icmp
/bin/chmod u+s %{prefix}/nagios/libexec/check_dhcp
/bin/chmod oug+rx %{prefix}/nagios/libexec/check_icmp
/bin/chmod oug+rx %{prefix}/nagios/libexec/check_dhcp
/bin/mkdir -p %{prefix}/reports/utils/log  &>/dev/null
/bin/touch %{prefix}/reports/utils/log/dashboard_data_load.log
/bin/touch %{prefix}/reports/utils/log/dashboard_avail_load.log
/bin/chown -R nagios.nagios %{prefix}/reports/utils/log
/bin/chmod -R ugo+rw %{prefix}/reports/utils/log
/bin/chown -R nobody.nobody %{prefix}/tmp
/bin/chmod 777 %{prefix}/tmp
#Reports permissions
/bin/chmod -R oug+rx %{prefix}/reports/utils

#Performance
/bin/chown nobody.nagios %{prefix}/apache2/htdocs/performance/rrd_img  &>/dev/null
/bin/chown nobody.nagios %{prefix}/performance/performance_views  &>/dev/null

/bin/echo "Starting Nagios, Apache2..."
/etc/init.d/nagios restart &>/dev/null
/etc/init.d/nsca restart &>/dev/null
/bin/sleep 2
/etc/init.d/gwhttpd stop &>/dev/null 
/etc/init.d/gwhttpd start &>/dev/null 
/etc/init.d/snmptt start &>/dev/null 

/bin/echo "Creating cron tab entries..."
if [ -f /etc/redhat-release ] ; then
cron='/var/spool/cron/nagios'
elif [ -f /etc/SuSE-release ] ; then
cron='/var/spool/cron/tabs/nagios'
fi 
/bin/echo "SHELL=/bin/bash" > "$cron"
/bin/echo "PATH=$JAVA_HOME/bin:$PATH:$HOME/bin:/usr/local/groundwork/bin" >> "$cron"
/bin/echo "HOME=/usr/local/groundwork/users/nagios" >> "$cron"
/bin/echo "50 23 * * * /usr/local/groundwork/reports/utils/dashboard_data_load.pl > /usr/local/groundwork/reports/utils/log/dashboard_data_load.log 2>&1" >> "$cron"
/bin/echo "0 1 * * * /usr/local/groundwork/reports/utils/dashboard_avail_load.pl > /usr/local/groundwork/reports/utils/log/dashboard_avail_load.log 2>&1" >> "$cron"

cron='/var/spool/cron/tabs/nobody'
/bin/echo "SHELL=/bin/bash" > "$cron"
/bin/echo "PATH=$JAVA_HOME/bin:$PATH:$HOME/bin:/usr/local/groundwork/bin" >> "$cron"
/bin/echo "HOME=/usr/local/groundwork/users/nobody" >> "$cron"
/bin/echo "0 0 * * * /usr/local/groundwork/bin/find //usr/local/groundwork/tmp/ -name "sess_*" -cmin +480 -exec rm \{} \;" >> "$cron"

logrotate='/etc/logrotate.d/syslog-ng'
/bin/echo "/usr/local/groundwork/var/log/syslog-ng/*.log {"  > "$logrotate"
/bin/echo "    daily"  >> "$logrotate"
/bin/echo "    postrotate"  >> "$logrotate"
/bin/echo "    /usr/bin/killall -HUP syslog-ng"  >> "$logrotate"
/bin/echo "    endscript"  >> "$logrotate"
/bin/echo "    rotate 8"  >> "$logrotate"
/bin/echo "}"  >> "$logrotate"

#Add iptables firewall ruleset
/bin/echo "Setting up iptables.."

iptables=%{prefix}/etc/iptables
/bin/echo "# Firewall configuration  for GroundWork"  >> "$iptables"
/bin/echo "# Note: ifup-post will punch the current nameservers through the"  >> "$iptables"
/bin/echo "# firewall; such entries will *not* be listed here."  >> "$iptables"
/bin/echo "*filter"  >> "$iptables"
/bin/echo ":INPUT ACCEPT [0:0]"  >> "$iptables"
/bin/echo ":FORWARD ACCEPT [0:0]"  >> "$iptables"
/bin/echo ":OUTPUT ACCEPT [0:0]"  >> "$iptables"
/bin/echo ":GW-INPUT - [0:0]"  >> "$iptables"
/bin/echo "-A INPUT -j GW-INPUT"  >> "$iptables"
/bin/echo "-A FORWARD -j GW-INPUT"  >> "$iptables"
/bin/echo "-A GW-INPUT -p udp -m udp --dport 162 -j ACCEPT"  >> "$iptables"
/bin/echo "-A GW-INPUT -p tcp -m tcp --dport 5667 --syn -j ACCEPT"  >> "$iptables"
/bin/echo "-A GW-INPUT -p tcp -m tcp --dport 22 --syn -j ACCEPT"  >> "$iptables"
/bin/echo "-A GW-INPUT -p tcp -m tcp --dport 80 --syn -j ACCEPT"  >> "$iptables"
/bin/echo "-A GW-INPUT -p tcp -m tcp --dport 443 --syn -j ACCEPT"  >> "$iptables"
/bin/echo "-A GW-INPUT -p tcp -m tcp --dport 8080 --syn -j ACCEPT"  >> "$iptables"
/bin/echo "-A GW-INPUT -p udp -m udp --dport 514 -j ACCEPT"  >> "$iptables"
/bin/echo "-A GW-INPUT -p udp -m udp --sport 161 -j ACCEPT"  >> "$iptables"
/bin/echo "-A GW-INPUT -p udp -m udp --sport 53 -j ACCEPT"  >> "$iptables"
/bin/echo "-A GW-INPUT -p udp -m udp --sport 123 -j ACCEPT"  >> "$iptables"
/bin/echo "-A GW-INPUT -i lo -j ACCEPT"  >> "$iptables"
/bin/echo "-A GW-INPUT -p tcp -m tcp --dport 0:1023 --syn -j REJECT"  >> "$iptables"
/bin/echo "-A GW-INPUT -p tcp -m tcp --dport 2049 --syn -j REJECT"  >> "$iptables"
/bin/echo "-A GW-INPUT -p udp -m udp --dport 0:1023 -j REJECT"  >> "$iptables"
/bin/echo "-A GW-INPUT -p udp -m udp --dport 2049 -j REJECT"  >> "$iptables"
/bin/echo "-A GW-INPUT -p tcp -m tcp --dport 6000:6009 --syn -j REJECT"  >> "$iptables"
/bin/echo "-A GW-INPUT -p tcp -m tcp --dport 7100 --syn -j REJECT"  >> "$iptables"
/bin/echo "COMMIT"  >> "$iptables"

if [ -f /etc/SuSE-release ] ; then
/bin/echo "Creating SuSE Linux specific links to boot scripts..."
ln -sf /etc/init.d/nagios /usr/sbin/rcnagios &>/dev/null
ln -sf /etc/init.d/gwhttpd /usr/sbin/rcgwapache2 &>/dev/null
ln -sf /etc/init.d/syslog-ng /usr/sbin/rcsyslog-ng &>/dev/null
ln -sf /etc/init.d/snmptt /usr/sbin/rcsnmptt &>/dev/null
/sbin/chkconfig --add nagios &>/dev/null
/sbin/chkconfig --add nsca &>/dev/null
/sbin/chkconfig --add gwhttpd &>/dev/null
/sbin/chkconfig --add syslog-ng &>/dev/null
/sbin/chkconfig --add snmptt &>/dev/null
fi

#Install sysstat.ioconf 
file="/etc/sysconfig/sysstat.ioconf"
file1="%{prefix}/etc/sysconfig/sysstat.ioconf"
if ! [ -a "$file" ] ; then
/bin/cp "$file1" "$file" &>/dev/null
fi

#Run indexer
%{prefix}/sbin/indexer -a &>/dev/null
#Run updatedb 
%{prefix}/bin/updatedb --output=%{prefix}/var/locatedb &>/dev/null &

%preun
if [ "$1" = "0" ] ; then # last uninstall
/bin/echo "Stop the services used by GroundWork Monitor..."
/etc/init.d/nagios stop &>/dev/null 
/sbin/chkconfig --del nagios &>/dev/null
/etc/init.d/nsca stop &>/dev/null 
/sbin/chkconfig --del nsca &>/dev/null
/etc/init.d/gwhttpd stop &>/dev/null	
/sbin/chkconfig --del gwhttpd &>/dev/null
/etc/init.d/syslog-ng stop &>/dev/null
/sbin/chkconfig --del syslog-ng &>/dev/null
/etc/init.d/snmptt stop &>/dev/null
/sbin/chkconfig --del snmptt &>/dev/null
fi

%postun

if [ -f /etc/redhat-release ] ; then
kill -9 `ps -u nagios -o "pid="` &> /dev/null
fuser -sk  %{prefix} &> /dev/null
fi

/bin/echo "Remove user nagios and nagioscmd..."
rm -rf %{prefix}/nagios/var/spool/nagios.cmd &>/dev/null
	/usr/sbin/userdel nagios || %logmsg "User \"nagios\" could not be deleted."
	/usr/sbin/groupdel nagioscmd || %logmsg "Group \"nagioscmd\" could not be deleted." &>/dev/null
/sbin/service nagios condrestart &>/dev/null 
rm -rf %{prefix}

/bin/echo "Removing GroundWork Monitor from boot sequence..."
rm -rf /etc/init.d/nagios
rm -rf  /etc/init.d/gwhttpd &>/dev/null

file="/etc/init.d/apache2-save"
if [ -a "$file" ] ; then
mv "$file" /etc/init.d/apache2 &>/dev/null
fi

file="/var/spool/cron/nagios"
if [ -a "$file" ] ; then
rm -rf  "$file" &>/dev/null
fi

file="/etc/rc.d/rc5.d/S90gwhttpd"
if [ -a "$file" ] ; then
rm -rf  "$file" &>/dev/null
fi

file="/etc/rc.d/rc3.d/S90gwhttpd"
if [ -a "$file" ] ; then
rm -rf  "$file" &>/dev/null
fi

file="/etc/rc.d/rc5.d/S90nagios"
if [ -a "$file" ] ; then
rm -rf  "$file" &>/dev/null
fi

file="/etc/rc.d/rc3.d/S90nagios"
if [ -a "$file" ] ; then
rm -rf  "$file" &>/dev/null
fi

file="/usr/sbin/rcgwapache2"
if [ -a "$file" ] ; then
rm -rf  "$file" &>/dev/null
fi

file="/usr/sbin/rcnagios"
if [ -a "$file" ] ; then
rm -rf  "$file" &>/dev/null
fi

file="/etc/init.d/gwhttpd"
if [ -a "$file" ] ; then
rm -rf  "$file" &>/dev/null
fi

file="/etc/init.d/nagios"
if [ -a "$file" ] ; then
rm -rf  "$file" &>/dev/null
fi

file="/etc/init.d/nsca"
if [ -a "$file" ] ; then
rm -rf  "$file" &>/dev/null
fi

file="/etc/init.d/snmptt"
if [ -a "$file" ] ; then
rm -rf  "$file" &>/dev/null
fi

file="/etc/init.d/syslog-ng"
if [ -a "$file" ] ; then
rm -rf  "$file" &>/dev/null
fi

%clean
rm -rf %{_tmppath}/%{name}
%files -f %{filelist}
#%files
#/*

%changelog

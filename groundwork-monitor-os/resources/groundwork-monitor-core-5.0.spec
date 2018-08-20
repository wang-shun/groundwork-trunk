# $Id: $
#
# Copyright (C) 2008 GroundWork Open Source, Inc. ("GroundWork")
# All rights reserved. This program is free software; you can redistribute it
# and/or modify it under the terms of the GNU General Public License version 2
# as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE.  See the GNU General Public License for 
# more details.
#
# You should have received a copy of the GNU General Public License along with
# this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
# Street, Fifth Floor, Boston, MA 02110-1301, USA.
#

%define filelist @PREFIX@/../@EXPORT_FILELIST@
%define prefix @PREFIX@
%define release @RELEASE_NUMBER@
Summary: GroundWork Monitor Community Edition (Core).
Name: @PACKAGE_NAME@
Version: @PACKAGE_VERSION@
Release: @RELEASE_NUMBER@
License: Copyright 2007 GroundWork Open Source, Inc. (GroundWork). All rights reserved. Use is subject to GroundWork commercial license terms.
Group: System Environment/Base
Source: %{name}-%{version}-%{release}.tar.gz
BuildRoot: %{_tmppath}/%{name}
Prefix: %{prefix}
Requires: groundwork-foundation-pro >= 2.0.0
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

%pre

# Check if groundwork-foundation-pro-2.3.0 is installed, due to rpm bug
if !(rpm -qa | /bin/fgrep -l 'groundwork-foundation-pro-2.3.0');then
  bin/echo  "groundwork-foundation is missing..."
  exit 1
fi

# Check arch
%ifnarch %{ix86}
/bin/echo  "Sorry, but current release supports only ix86 Architecture .... Aborting installation."
exit 1
%endif

# Check distribution 
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
rel2=$(/bin/fgrep "127.0.0.1" /etc/hosts | grep -v "#" | awk '{print $2}')
rel3=$(/bin/fgrep "127.0.0.1" /etc/hosts | grep -v "#" | awk '{print $3}')
if ! [ $rel2 = "localhost" ] ; then
  if ! [ $rel3 = "localhost" ] ; then
    echo "Please edit /etc/hosts file..."
    echo "It must be either one on the following:"
    echo " 127.0.0.1       localhost.localdomain  localhost"
    echo " 127.0.0.1       localhost  localhost.localdomain"
    echo ""
    echo "But you have..."
    /bin/fgrep "127.0.0.1" /etc/hosts | grep -v "#"
    echo ""
   exit 1
  else
    echo "/etc/hosts is OK"
  fi
else
  echo "/etc/hosts is OK"
fi

# Check for a 4.5 pro installation
if (rpm -qa | grep -l 'groundwork-monitor-pro-4.5');then
%logmsg "Please back your data and uninstall the groundwork-monitor-pro-4.5 RPM before continuing...Aborting installation."
exit 1
fi

# Check for groundwork-monitor-core 
if [ -n "$MIGRATE4550" ] && (rpm -qa | grep -l 'groundwork-monitor-core'); then
%logmsg "Please unset the environmental variable MIGRATE4550 before continuing.... Aborting installation."
exit 1
fi

# Get the current version of Nagios
if [ -e /usr/local/groundwork/nagios/bin/nagios ]
then
/usr/local/groundwork/nagios/bin/nagios | grep Nagios | head -1 | sed s/'Nagios '// > /tmp/nagiosversion.txt
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

# IF REDHAT
if [ -f /etc/redhat-release ] ; then
	/usr/sbin/usermod -G nagioscmd nagios &>/dev/null
fi

# IF SuSE
if [ -f /etc/SuSE-release ] ; then
	/usr/sbin/groupmod -A nagios nagios
	/usr/sbin/groupmod -A nagios nagioscmd
fi

#Remove session and template files which are no longer valid.
echo "Remove obsolete session files.."
/bin/rm -f /tmp/sess* &>/dev/null
/bin/rm -f /tmp/tpl* &>/dev/null

#Installation mode: install/upgrade.
install_mode="$1"
if [ "$install_mode" = "2" ] ; then  # upgrade

# No more nobody user in 5.2, change ownership from nobody to nagios
# JIRA GWMON-3882
/bin/chown -R -h nagios:nagios %{prefix}

# Get the current version of groundwork-monitor-core
if (rpm -qa | grep -l 'groundwork-monitor-core');then
GW_MON_CORE_VER=`rpm -qa | grep groundwork-monitor-core | sed 's/-/ /g' | awk '{print $4;}'`
fi

echo "Making backups"
for etc_cfg in $( ls %{prefix}/etc/*.cfg); do
  cp -rp $etc_cfg $etc_cfg.backup.$GW_MON_CORE_VER
done
for nagios_etc_cfg in $( ls %{prefix}/nagios/etc/*.cfg); do
  cp -rp $nagios_etc_cfg $nagios_etc_cfg.backup.$GW_MON_CORE_VER
done

if [ -d %{prefix}/etc/syslog-ng ] ; then
  cp -rp %{prefix}/etc/syslog-ng/syslog-ng.conf %{prefix}/etc/syslog-ng.conf.backup.$GW_MON_CORE_VER
  cp -rp %{prefix}/etc/syslog-ng/syslog-ng.init %{prefix}/etc/syslog-ng.init.backup.$GW_MON_CORE_VER
fi
cp -rp %{prefix}/apache2/conf/httpd.conf %{prefix}/apache2/conf/httpd.conf.backup.$GW_MON_CORE_VER
cp -rp %{prefix}/apache2/logs/access_log %{prefix}/apache2/logs/access_log.backup.$GW_MON_CORE_VER
cp -rp %{prefix}/apache2/logs/error_log %{prefix}/apache2/logs/error_log.backup.$GW_MON_CORE_VER
cp -rp %{prefix}/nagios/libexec/utils.pm %{prefix}/nagios/libexec/utils.pm.backup.$GW_MON_CORE_VER

/bin/mkdir -p %{prefix}/backup
#JIRA GWMON-993 etc backup keeps old php ini files that breaks upgrades 
#/bin/tar -czpf %{prefix}/backup/etc-backup.tar.gz %{prefix}/etc &>/dev/null
/bin/tar -czpf %{prefix}/backup/nagios-etc-backup.tar.gz %{prefix}/nagios/etc/*cfg &>/dev/null
/bin/tar -czpf %{prefix}/backup/nagios-var-backup.tar.gz %{prefix}/nagios/var &>/dev/null
/bin/tar -czpf %{prefix}/backup/nagios-eventhandlers-backup.tar.gz %{prefix}/nagios/eventhandlers &>/dev/null
/bin/tar -czpf %{prefix}/backup/nagios-images-backup.tar.gz %{prefix}/nagios//share/images &>/dev/null
/bin/tar -czpf %{prefix}/backup/apache2-log-backup.tar.gz %{prefix}/apache2/logs &>/dev/null
/bin/tar -czpf %{prefix}/backup/rrd-backup.tar.gz %{prefix}/rrd &>/dev/null
/bin/tar -czpf %{prefix}/backup/monarch-MonarchCallOut.pm.tar.gz %{prefix}/monarch/lib/MonarchCallOut.pm &>/dev/null
/bin/tar -czpf %{prefix}/backup/monarch-MonarchExternals.pm.tar.gz %{prefix}/monarch/lib/MonarchExternals.pm &>/dev/null
/bin/tar -czpf %{prefix}/backup/monarch-MonarchDeploy.pm.tar.gz %{prefix}/monarch/lib/MonarchDeploy.pm &>/dev/null
#/bin/tar -czpf %{prefix}/backup/apache2-conf-backup.tar.gz  %{prefix}/apache2/conf  &>/dev/null
/bin/tar -czpf %{prefix}/backup/apache2-htdocs-backup.tar.gz  %{prefix}/apache2/htdocs  &>/dev/null
/bin/tar -czpf %{prefix}/backup/gw-users-backup.tar.gz  %{prefix}/users  &>/dev/null
/bin/tar -czpf %{prefix}/backup/gw-performance-view.tar.gz  %{prefix}/performance/performance_views  &>/dev/null
/bin/tar -czpf %{prefix}/backup/https-certificates.tar.gz  %{prefix}/usr/local/groundwork/apache2/conf/ssl.key  &>/dev/null

#JIRA FIX GWMON-751 runtime and not config
#/bin/tar -czpf %{prefix}/backup/guava-config.inc.php.tar.gz %{prefix}/guava/includes/config.inc.php &>/dev/null
/bin/tar -czpf %{prefix}/backup/guava-runtime.inc.php.tar.gz %{prefix}/guava/includes/runtime.inc.php &>/dev/null

# Backup removed configuration files that are removed in 5.2
/bin/tar -czpf %{prefix}/backup/nsca-cfg-backup.tar.gz  %{prefix}/etc/nsca.cfg  &>/dev/null
/bin/tar -czpf %{prefix}/backup/snmptt-conf-backup.tar.gz  %{prefix}/etc/snmp/snmptt.conf  &>/dev/null 
/bin/tar -czpf %{prefix}/backup/syslog-ng-conf-backup.tar.gz  %{prefix}/etc/syslog-ng.conf  &>/dev/null 

# JIRA FIX GWMON-4835 upgrade to 5.2 from 5.1.3 overwrites utils.pm
/bin/tar -czpf %{prefix}/backup/nagios-libexec-backup.tar.gz %{prefix}/nagios/libexec &>/dev/null

# JIRA FIX GWMON-4464 remove Groundwork labrary path from ld.so.conf
if(grep -l '/usr/local/groundwork' /etc/ld.so.conf); 
then
  cat /etc/ld.so.conf | grep -v groundwork > /tmp/ldrewind.gwk
  cp /tmp/ldrewind.gwk /etc/ld.so.conf
  ldconfig
  wait
  rm /tmp/ldrewind.gwk
  echo "Removed /usr/local/groundwork/lib from ld.so.conf" 
else
  echo "No change to ld.so.conf"
fi

# Fix JIRA GWMON-5162
if [ -f /etc/init.d/snmptrapd ] ; then
/bin/mv /etc/init.d/snmptrapd /etc/init.d/snmptrapd.gwk
fi

#Fix JIRA GWMON-2704
if [ -d /usr/local/groundwork/nagios/etc/prairiedog ] ; then
/bin/mv /usr/local/groundwork/nagios/etc/prairiedog/* /usr/local/groundwork/nagios/etc/
fi

# GWMON-5366
if [ -f /usr/local/groundwork/apache2/htdocs/reports/gwir.cfg ] ; then
  rm -rf /usr/local/groundwork/apache2/htdocs/reports/gwir.cfg
fi
if [ -f /usr/local/groundwork/reports/utils/utils/dashboard_lwp_load.pl ] ; then
  /bin/mv /usr/local/groundwork/reports/utils/utils/dashboard_lwp_load.pl /usr/local/groundwork/reports/utils
  /bin/rm -rf /usr/local/groundwork/reports/utils/utils
fi

fi

#Fix JIRA GWMON-5032
/bin/echo "Creating cron tab entries..."
if [ -f /etc/redhat-release ] ; then
  nobodycron='/var/spool/cron/nobody'
elif [ -f /etc/SuSE-release ] ; then
  nobodycron='/var/spool/cron/tabs/nobody'
fi

if [ -f $nobodycron ] ; then
  /bin/cp $nobodycron $nobodycron.gwk
  cat $nobodycron | grep -v groundwork > /tmp/nobody.gwk
  /bin/rm -rf $nobodycron
  /bin/mv /tmp/nobody.gwk $nobodycron
  echo "Removed groundwork jobs from $nobodycron"
fi

%post 
# Check ip
ip=$(ifconfig $interface | awk '$1=="inet" { split($2, line, ":"); print line[2]; exit}') 
# Just in case if user have more then one network interface, we will use first one
ip=$(echo $ip |awk '{ print $1; }')
echo $ip
sed -e 's/localhost/'$ip'/g' %{prefix}/apache2/conf/httpd.conf > %{prefix}/apache2/conf/httpd.conf.SAVE
mv %{prefix}/apache2/conf/httpd.conf.SAVE %{prefix}/apache2/conf/httpd.conf

### This section is moved to set-properties.sh 
# Install the db.properties file for Open Source
/bin/echo "Install db.properties file..."
/bin/mv %{prefix}/config/db.properties %{prefix}/config/db.properties.foundation &>/dev/null
/bin/cp -fp %{prefix}/config/db.properties.os %{prefix}/config/db.properties &>/dev/null

# copy home-osv.xml to home.xml
/bin/cp %{prefix}/guava/packages/guava/templates/home-osv.xml %{prefix}/guava/packages/guava/templates/home.xml &>/dev/null

#Installation mode: install/upgrade.
install_mode="$1"

#TODO INSERT OR SWITCH inst5all_mode or -n $MIGRATE4550
if [ "$install_mode" = "2" ] || [ -n "$MIGRATE4550" ] ; then  # any upgrade
#Kill all processes started from groundwork-bin directory
/bin/echo "Performing an upgrade by installing a newer version... "
/bin/echo "Stop the services used by GroundWork Monitor..."
/etc/init.d/nagios stop &> /dev/null
/etc/init.d/nsca stop &> /dev/null
/etc/init.d/httpd stop &> /dev/null
/etc/init.d/syslog-ng stop &> /dev/null
/etc/init.d/gwservices stop &> /dev/null
wait
kill -9 `ps -u nagios -o "pid="` &> /dev/null
fuser -sk  %{prefix}/* &> /dev/null

#Remove Nagios feeders seek file
rm -rf %{prefix}/nagios/var/nagios_seek.tmp &>/dev/null
# Restore config and log from backups
#/bin/tar -xzpf %{prefix}/backup/etc-backup.tar.gz -C / &>/dev/null
/bin/tar -xzpf %{prefix}/backup/nagios-etc-backup.tar.gz -C / &>/dev/null
/bin/tar -xzpf %{prefix}/backup/nagios-var-backup.tar.gz -C / &>/dev/null
# GWMON-5152: No need to restore eventhandlers
#/bin/tar -xzpf %{prefix}/backup/nagios-eventhandlers-backup.tar.gz -C / &>/dev/null
/bin/tar -xzpf %{prefix}/backup/nagios-images-backup.tar.gz -C / &>/dev/null
/bin/tar -xzpf %{prefix}/backup/apache2-log-backup.tar.gz -C / &>/dev/null
/bin/tar -xzpf %{prefix}/backup/rrd-backup.tar.gz -C / &>/dev/null
/bin/tar -xzpf %{prefix}/backup/monarch-MonarchCallOut.pm.tar.gz -C / &>/dev/null
/bin/tar -xzpf %{prefix}/backup/monarch-MonarchExternals.pm.tar.gz -C / &>/dev/null
/bin/tar -xzpf %{prefix}/backup/monarch-MonarchDeploy.pm.tar.gz -C / &>/dev/null
#/bin/tar -xzpf %{prefix}/backup/apache2-conf-backup.tar.gz -C / &>/dev/null
/bin/tar -xzpf %{prefix}/backup/gw-users-backup.tar.gz -C / &>/dev/null
#/bin/tar -xzpf %{prefix}/backup/guava-config.inc.php.tar.gz -C / &>/dev/null
/bin/tar -xzpf %{prefix}/backup/guava-runtime.inc.php.tar.gz -C / &>/dev/null
/bin/tar -xzpf %{prefix}/backup/gw-performance-view.tar.gz -C / &>/dev/null
/bin/tar -xzpf %{prefix}/backup/https-certificates.tar.gz -C /  &>/dev/null

fi

#JIRA Fixes: 983,984,985,986 and 988
/bin/touch %{prefix}/guava/includes/runtime.inc.php
/bin/touch %{prefix}/guava/includes/config.inc.php

# Groundwork-monitor 4.5 --> 5.x upgrade...

if [ -n "$MIGRATE4550" ] ; then
/bin/echo "Preparing existing 4.5 databases for upgrade..."
if [ -n "$MYSQL_ROOT" ] ; then
/bin/echo "MySQL root passwd is set..."
/bin/echo "Ok..."
/bin/echo "Upgrading groundwork databases..."
mysql -uroot -p"$MYSQL_ROOT" monarch     < %{prefix}/migration/migrate-monarch.sql
%{prefix}/bin/perl  %{prefix}/migration/migrate-monarch.pl
%{prefix}/bin/php %{prefix}/migration/migrate-guava-sb.php localhost guava guava gwrk

elif [ -z "$MYSQL_ROOT" ] ; then
/bin/echo "MySQL root passwd is not set..."
/bin/echo "Ok..."
/bin/echo "Upgrading groundwork databases..."
mysql -uroot monarch     < %{prefix}/migration/migrate-monarch.sql
%{prefix}/bin/perl  %{prefix}/migration/migrate-monarch.pl
%{prefix}/bin/php %{prefix}/migration/migrate-guava-sb.php localhost guava guava gwrk
fi

fi

install_mode="$1"
if [ "$install_mode" = "2" ]  ; then  # regular upgrade... Don't drop databases..
/bin/echo "Preparing existing databases for upgrade..."
if [ -n "$MYSQL_ROOT" ] ; then
/bin/echo "MySQL root passwd is set..."
/bin/echo "Ok..."
/bin/echo "Upgrading groundwork databases..."
if [ -f %{prefix}/databases/create-bookshelf-mnogo-db.sql ] ; then
mysql -uroot -p"$MYSQL_ROOT" mysql < %{prefix}/databases/create-bookshelf-mnogo-db.sql
fi
mysql -uroot -p"$MYSQL_ROOT" monarch     < %{prefix}/migration/migrate-monarch.sql
%{prefix}/bin/perl  %{prefix}/migration/migrate-monarch.pl
%{prefix}/bin/php %{prefix}/migration/migrate-guava-sb.php localhost guava guava gwrk
elif [ -z "$MYSQL_ROOT" ] ; then
/bin/echo "MySQL root passwd is not set..."
/bin/echo "Ok..."
/bin/echo "Upgrading groundwork databases..."
if [ -f %{prefix}/databases/create-bookshelf-mnogo-db.sql ] ; then
mysql -uroot mysql < %{prefix}/databases/create-bookshelf-mnogo-db.sql
fi
mysql -uroot monarch     < %{prefix}/migration/migrate-monarch.sql
%{prefix}/bin/perl  %{prefix}/migration/migrate-monarch.pl
%{prefix}/bin/php %{prefix}/migration/migrate-guava-sb.php localhost guava guava gwrk
fi

elif [ "$install_mode" = "1" ] && [ -z "$MIGRATE4550" ]; then # first install creating databases
/bin/echo "Creating different databases..."
if [ -n "$MYSQL_ROOT" ] ; then
/bin/echo "MySQL root passwd is set..."
/bin/echo "Ok..."
mysql -uroot -p"$MYSQL_ROOT" mysql < %{prefix}/databases/create-monitor-sb-db.sql 
mysql -uroot -p"$MYSQL_ROOT" monarch     < %{prefix}/databases/monarch.sql 
mysql -uroot -p"$MYSQL_ROOT" guava       <  %{prefix}/databases/guava.sql
mysql -uroot -p"$MYSQL_ROOT" dashboard	 <	%{prefix}/databases/insightreports.sql
mysql -uroot -p"$MYSQL_ROOT" GWCollageDB <	%{prefix}/databases/foundation-pro-extension.sql

elif [ -z "$MYSQL_ROOT" ] ; then
/bin/echo "MySQL root passwd is not set..."
mysql -uroot mysql <  %{prefix}/databases/create-monitor-sb-db.sql 
mysql -uroot monarch      < %{prefix}/databases/monarch.sql 
mysql -uroot guava        <  %{prefix}/databases/guava.sql
mysql -uroot dashboard	  <	%{prefix}/databases/insightreports.sql
mysql -uroot GWCollageDB	<	%{prefix}/databases/foundation-pro-extension.sql
fi

# Migrate after creating Databases
%{prefix}/bin/php %{prefix}/migration/migrate-guava-sb.php localhost guava guava gwrk

fi

if [ "$install_mode" = "1" ] ; then  # first install
/bin/echo "Creating user directories and bash_profiles for user nagios ..."
#Create Home directories
/bin/mkdir -p %{prefix}/users &>/dev/null
/bin/mkdir -p %{prefix}/users/nagios &>/dev/null

/bin/echo "Setting up the boot sequence for GroundWork Monitor modules..."

/bin/echo "Add syslog-ng ..."
/etc/init.d/syslog stop &>/dev/null
/etc/init.d/syslog-ng stop &>/dev/null

if [ -f /etc/redhat-release ] ; then
	/bin/echo "Disable syslog for RedHat ..."
	/sbin/chkconfig --level 2345 syslog off &>/dev/null
	/sbin/chkconfig syslog-ng off &>/dev/null
fi

# IF SuSE
if [ -f /etc/SuSE-release ] ; then
	/bin/echo "Disable syslog for SuSE ..."
	/sbin/chkconfig --level 2345 syslog -f off &>/dev/null
	/sbin/chkconfig -f syslog-ng off &>/dev/null
fi

file="/etc/init.d/syslog-ng"
file1="/etc/init.d/syslog-ng.old"
if [ -a "$file" ] ; then
/bin/mv "$file" "$file1" &>/dev/null
fi
/bin/cp %{prefix}/etc/syslog-ng.init /etc/init.d/syslog-ng 
/bin/chmod +x  /etc/init.d/syslog-ng 
/sbin/chkconfig --add syslog-ng 
echo "Setting syslog-ng daemon level to 2345"
/sbin/chkconfig --level 2345 syslog-ng on
echo "Backing up syslog-ng.init..."
/bin/cp -fp %{prefix}/etc/syslog-ng.init %{prefix}/etc/syslog-ng.init.org &>/dev/null

/bin/echo "Add nagios ..."
/bin/cp %{prefix}/etc/nsca.init /etc/init.d/nsca &>/dev/null
/bin/chmod +x /etc/init.d/nsca &>/dev/null

# GWMON-4608
# NSCA should not be part of xinetd since it might be started twice xinetd and in init.d
#/bin/cp %{prefix}/etc/nsca.xinetd /etc/xinetd.d/nsca &>/dev/null
#/bin/chmod +x /etc/xinetd.d/nsca &>/dev/null

/bin/cp %{prefix}/nagios/etc/nagios.initd /etc/init.d/nagios &>/dev/null
/bin/chmod +x /etc/init.d/nagios &>/dev/null
/sbin/chkconfig --add nagios 
echo "Setting nagios daemon level to 2345"
/sbin/chkconfig --level 2345 nagios on

/bin/echo "Add Groundwork Services..."
/bin/chmod -R 755 %{prefix}/services
/bin/cp %{prefix}/services/gwservices /etc/init.d/gwservices &>/dev/null 
/bin/chmod +x /etc/init.d/gwservices &>/dev/null
/sbin/chkconfig --add gwservices 
echo "Setting gwservices daemon level to 2345"
/sbin/chkconfig --level 2345 gwservices on

# Enable Feeder script instead of nagios Event Broker.
# If the EventBroker is ready the feeder should be disabled by un-commenting
# the following line.
#/bin/touch %{prefix}/services/feeder-nagios-status/down

if [ -f /etc/SuSE-release ] ; then
/bin/echo "Creating SuSE Linux specific links to boot scripts..."
/bin/ln -sf /etc/init.d/nagios /usr/sbin/rcnagios &>/dev/null
/bin/ln -sf /etc/init.d/httpd /usr/sbin/rcgwapache2 &>/dev/null
/bin/ln -sf /etc/init.d/gwservices /usr/sbin/rcgwservices &>/dev/null
/bin/ln -sf /etc/init.d/syslog-ng /usr/sbin/rcsyslog-ng &>/dev/null
/sbin/chkconfig --add nagios &>/dev/null
/sbin/chkconfig --add nsca &>/dev/null
/sbin/chkconfig --add httpd &>/dev/null
/sbin/chkconfig --add gwservices &>/dev/null
/sbin/chkconfig --add syslog-ng &>/dev/null
fi

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
fi

#GWMON-4137
/bin/echo "Creating cron tab entries..."
if [ -f /etc/redhat-release ] ; then
  cron='/var/spool/cron/nagios'
elif [ -f /etc/SuSE-release ] ; then
  cron='/var/spool/cron/tabs/nagios'
fi

# GWMON-5045
# Backup existing cron job if there is one
if [ -f $cron ] ; then
  /bin/mv $cron $cron.gwbak
  echo "nagios cron file is backed up as:"
  echo "    $cron.gwbak"
fi
#Create and set permissons on the reports log directory
/bin/echo "SHELL=/bin/bash" > "$cron"
/bin/echo "PATH=$JAVA_HOME/bin:$PATH:$HOME/bin:/usr/local/groundwork/bin" >> "$cron"
/bin/echo "HOME=/usr/local/groundwork/users/nagios" >> "$cron"
/bin/echo "50 23 * * * /usr/local/groundwork/reports/utils/dashboard_data_load.pl > /usr/local/groundwork/reports/utils/log/dashboard_data_load.log 2>&1" >> "$cron"
/bin/echo "0 1 * * * /usr/local/groundwork/reports/utils/dashboard_avail_load.pl > /usr/local/groundwork/reports/utils/log/dashboard_avail_load.log 2>&1" >> "$cron"
/bin/echo "0 0 * * * /usr/local/groundwork/bin/find /tmp/ -maxdepth 0 -name 'sess_*' -cmin +480 -exec rm \{} \;" >> "$cron"
/bin/echo "0 0 * * * /usr/local/groundwork/bin/find /usr/local/groundwork/nagios/var/archives/ -follow -name 'nagios-*' -mtime +60 -exec rm \{} \;" >> "$cron"

/bin/echo "Add Apache2 ..."
file="/etc/init.d/httpd"
file1="/etc/init.d/httpd.GW.bak"
if [ -a "$file" ] ; then
/bin/mv "$file" "$file1" &>/dev/null
fi
/usr/bin/killall -9 httpd &>/dev/null
/bin/cp %{prefix}/apache2/conf/httpd.init/httpd.init /etc/init.d/httpd
/bin/chmod +x /etc/init.d/httpd
/sbin/chkconfig --add httpd 
echo "Setting httpd daemon level to 2345"
/sbin/chkconfig --level 2345 httpd on

if [ -f %{prefix}/users/nagios/.bashrc ] ; then
  # Make backup
  /bin/mv -f %{prefix}/users/nagios/.bashrc %{prefix}/users/nagios/.bashrc.GWMON
fi

arch=`arch`
echo "ARCH is $arch"
if [ $arch = "x86_64" ] ; then
  libdir=lib64
else
  libdir=lib
fi
echo "LIBDIR is $libdir"

#Create .bashrc for nagios
bashrc=%{prefix}/users/nagios/.bashrc
/bin/echo "# .bashrc" > $bashrc
/bin/echo "# Get the aliases and functions" >> $bashrc
/bin/echo "GW_HOME=%{prefix}"       >> $bashrc
/bin/echo "LD_LIBRARY_PATH=%{prefix}/$libdir" >> $bashrc
/bin/echo "LD_RUN_PATH=%{prefix}/$libdir"       >> $bashrc
/bin/echo "JAVA_HOME=$JAVA_HOME" >> $bashrc
/bin/echo "PATH=%{prefix}/bin:%{prefix}/sbin:%{prefix}/nagios/bin:$JAVA_HOME/bin:$PATH:$HOME/bin" >> $bashrc
/bin/echo "export GW_HOME"       >> $bashrc
/bin/echo "export LD_LIBRARY_PATH" >> $bashrc
/bin/echo "export LD_RUN_PATH LD_RUN_PATH"       >> $bashrc
/bin/echo "export MAVEN_HOME"       >> $bashrc
/bin/echo "export JAVA_HOME" >> $bashrc
/bin/echo "unset USERNAME" >> $bashrc
/bin/chown nagios.nagios %{prefix}/users/nagios/.bashrc

#Install sysstat.ioconf 
file="/etc/sysconfig/sysstat.ioconf"
file1="%{prefix}/etc/sysconfig/sysstat.ioconf"
if ! [ -a "$file" ] ; then
/bin/cp "$file1" "$file" &>/dev/null
fi

#Logos link for Kiwi
/bin/ln -sf %{prefix}/nagios/share/images/logos %{prefix}/apache2/htdocs/sv/images/icons &>/dev/null
#/bin/ln -sf /usr/local/groundwork/docs/bookshelf-data /usr/local/groundwork/guava/packages/bookshelf/bookshelf-data &>/dev/null

/bin/echo "Install logrotate for groundwork services..."
#make sure a backup directory exists
/bin/mkdir %{prefix}/backup &>/dev/null
#Cleanup files
/bin/rm /etc/logrotate.d/groundwork.backup &>dev/null
/bin/rm /etc/logrotate.d/syslog-ng.rpmnew &>dev/null
#Backup any existing files
/bin/mv /etc/logrotate.d/groundwork %{prefix}/backup/logrotate.groundwork.backup &>/dev/null
/bin/mv /etc/logrotate.d/syslog-ng %{prefix}/backup/syslog-ng.backup &>/dev/null
/bin/mv /etc/logrotate.d/syslog %{prefix}/backup/syslog.backup &>/dev/null
#Copy most up-to-date groundwork settings
/bin/cp -f %{prefix}/etc/groundwork.logrotate /etc/logrotate.d/groundwork &>/dev/null

/bin/echo "Setting up the permissions..."
/usr/sbin/usermod -G nagios nagios

if [ "$install_mode" = "2" ] ; then  # upgrade

/usr/sbin/usermod -g nagios nagios
#GWMON-4045
if [ -f /etc/SuSE-release ] ; then
  /usr/sbin/groupmod -R nobody nagios
  /usr/sbin/groupmod -R nobody nagioscmd
fi

fi

# IF SuSE
if [ -f /etc/SuSE-release ] ; then
  /bin/chmod 4755 %{prefix}/monarch/bin/nagios_reload &>/dev/null
  /bin/chmod 4755 %{prefix}/monarch/bin/nmap_scan_one &>/dev/null
else
  /bin/chmod 4750 %{prefix}/monarch/bin/nagios_reload &>/dev/null
  /bin/chmod 4750 %{prefix}/monarch/bin/nmap_scan_one &>/dev/null
fi

#GWMON-2106
/bin/chmod u+sw %{prefix}/nagios/libexec/check_by_ssh

/bin/chmod u+sw %{prefix}/nagios/libexec/check_icmp
/bin/chmod u+sw %{prefix}/nagios/libexec/check_dhcp
/bin/chmod oug+rx %{prefix}/nagios/libexec/check_icmp
/bin/chmod oug+rx %{prefix}/nagios/libexec/check_dhcp

#Database credentials restrict access
/bin/chmod og-xw %{prefix}/config/db.properties
/bin/chmod u-x %{prefix}/config/db.properties
/bin/chmod u+rw %{prefix}/config/db.properties
/bin/chown root.nagios %{prefix}/config/db.properties

#Fix JIRA: GWMON-5432
/bin/cp -rp %{prefix}/config/my.cnf /etc/my.cnf.groundwork

#Fix JIRA: GWMON-4945
/bin/chmod +s %{prefix}/sbin/fping

#Fix JIRA: GWMON-4949
%{prefix}/bin/perl  %{prefix}/bin/pwgen.pl
/bin/chown nagios.nagios %{prefix}/nagios/etc/htpasswd.users

#Fix JIRA: GWK-187 
file="/usr/lib/libgdbm.so.3"
file1="/usr/lib/libgdbm.so.2"
if [ -a "$file" ] ; then
/bin/ln -sf "$file" "$file1" &>/dev/null
fi
#Clean temp files
rm -rf /JAVA_VERSION*
rm -rf /MYSQL_VERSION
#Run indexer
%{prefix}/sbin/indexer -a &>/dev/null
#Run updatedb 
%{prefix}/bin/updatedb --output=%{prefix}/var/locatedb &>/dev/null &

#Fix JIRA: GWMON-3297
%{prefix}/bin/dbs.sh &>/dev/null

# "Starting Nagios..."
/etc/init.d/nagios start

# "Starting Syslog-ng..."
/etc/init.d/syslog-ng start
 
# "Starting Groundwork Services......"
su -l root /etc/init.d/gwservices start 
wait

# "Starting Apache2..."
/etc/init.d/httpd start
 
/bin/echo "Check Services..."
/etc/init.d/nagios status
/etc/init.d/syslog-ng status
/etc/init.d/httpd status
/etc/init.d/gwservices status

%preun
if [ "$1" = "0" ] ; then # last uninstall
/bin/echo "Stop the services used by GroundWork Monitor..."
/etc/init.d/nagios stop &>/dev/null 
/sbin/chkconfig --del nagios &>/dev/null
/etc/init.d/nsca stop &>/dev/null 
/sbin/chkconfig --del nsca &>/dev/null
/etc/init.d/httpd stop &>/dev/null	
/sbin/chkconfig --del httpd &>/dev/null
/etc/init.d/gwservices stop &>/dev/null
wait
/sbin/chkconfig --del gwservices &>/dev/null
/etc/init.d/syslog-ng stop &>/dev/null
/sbin/chkconfig --del syslog-ng &>/dev/null
fi
%postun
if [ "$1" = "0" ] ; then # last uninstall
if [ -f /etc/redhat-release ] ; then
kill -9 `ps -u nagios -o "pid="` &> /dev/null
fuser -sk  %{prefix} &> /dev/null
fi
/bin/echo "Remove user nagios and nagioscmd..."
rm -rf %{prefix}/nagios/var/spool/nagios.cmd &>/dev/null
	/usr/sbin/userdel nagios || %logmsg "User \"nagios\" could not be deleted."
	/usr/sbin/groupdel nagioscmd || %logmsg "Group \"nagioscmd\" could not be deleted." &>/dev/null
/sbin/service nagios condrestart &>/dev/null 
#Remove cron job for user nagios
/usr/bin/crontab -unagios -r &>/dev/null
#Remove directories and files that were created while running GroundWork Monitor
rm -rf %{prefix}/apache2 &>/dev/null
rm -rf %{prefix}/collage &>/dev/null
rm -rf %{prefix}/etc &>/dev/null
rm -rf %{prefix}/guava &>/dev/null
rm -rf %{prefix}/monarch &>/dev/null
rm -rf %{prefix}/reports &>/dev/null
rm -rf %{prefix}/nagios &>/dev/null
rm -rf %{prefix}/services &>/dev/null
rm -rf %{prefix}/share &>/dev/null
rm -rf %{prefix}/users &>/dev/null
rm -rf %{prefix}/var &>/dev/null
rm -rf %{prefix}/lib &>/dev/null
rm -rf %{prefix}/lib64 &>/dev/null
rm -rf %{prefix}/tmp &>/dev/null
rm -rf %{prefix}/rrd &>/dev/null

rm -rf %{prefix}/guava/packages/guava/templates/home.xml &>/dev/null

/bin/echo "Removing config file for GroundWork logrotate"
rm /etc/logrotate.d/groundwork &>/dev/null
/bin/echo "Restore original syslog-ng and syslog for logrotate..."
/bin/cp -f %{prefix}/backup/syslog-ng.backup /etc/logrotate.d/syslog-ng  &>/dev/null
/bin/cp -f %{prefix}/backup/syslog.backup /etc/logrotate.d/syslog  &>/dev/null

/bin/echo "Cleaning up /tmp directory"
rm -rf /tmp/BIRTSampleDB* &>/dev/null
rm -rf /tmp/Jetty_0_0_0_0* &>/dev/null

/bin/echo "Removing GroundWork Monitor from boot sequence..."
rm -rf /etc/init.d/nagios
rm -rf  /etc/init.d/httpd &>/dev/null
file="/etc/init.d/apache2-save"
if [ -a "$file" ] ; then
mv "$file" /etc/init.d/apache2 &>/dev/null
fi
file="/var/spool/cron/nagios"
if [ -a "$file" ] ; then
rm -rf  "$file" &>/dev/null
fi
file="/var/spool/cron/tabs/nagios"
if [ -a "$file" ] ; then
rm -rf  "$file" &>/dev/null
fi
file="/etc/rc.d/rc5.d/S90httpd"
if [ -a "$file" ] ; then
rm -rf  "$file" &>/dev/null
fi
file="/etc/rc.d/rc3.d/S90httpd"
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
file="/usr/sbin/rcgwservices"
if [ -a "$file" ] ; then
rm -rf  "$file" &>/dev/null
fi
file="/etc/init.d/httpd"
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
file="/etc/xinetd.d/nsca"
if [ -a "$file" ] ; then
rm -rf  "$file" &>/dev/null
fi
file="/etc/init.d/syslog-ng"
if [ -a "$file" ] ; then
rm -rf  "$file" &>/dev/null
fi
file="/etc/init.d/gwservices"
if [ -a "$file" ] ; then
rm -rf  "$file" &>/dev/null
fi
file="/etc/rc.d/rc5.d/S95gwservices"
if [ -a "$file" ] ; then
rm -rf  "$file" &>/dev/null
fi
file="/etc/rc.d/rc3.d/S95gwservices"
if [ -a "$file" ] ; then
rm -rf  "$file" &>/dev/null
fi

fi # End of #last uninstall

%clean
rm -rf %{_tmppath}/%{name}

%files -f %{filelist}
#%files
#/*

%changelog

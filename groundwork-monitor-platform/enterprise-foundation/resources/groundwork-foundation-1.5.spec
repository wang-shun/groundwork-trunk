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

%define prefix @PREFIX@
%define filelist @PREFIX@/../@EXPORT_FILELIST@ 
%define services  $prefix/services
Summary: GroundWork Foundation.
Name: @PACKAGE_NAME@ 
Version: @PACKAGE_VERSION@
Release: @RELEASE_NUMBER@
License: GPL
Group: Applications/Engineering
BuildArch: noarch
Source: %{name}-%{version}-%{release}.tar.gz
BuildRoot: %{_tmppath}/%{name}
Prefix: %{prefix}
# No dependency . Any MySQL are supported
#Requires: @DEPENDENCY@ 
AutoReqProv: no
%description 
GroundWork Foundation is an IT management data abstraction layer and development platform. Foundation 1.5 has been enhanced to integrate more diverse data sources into a single data store. This was achieved by introducing properties for describing the data points instead of using database fields for each measurement. The APIs and the existing feeders are backward compatible with previous versions.
The Foundation data model allows the integration of any state, event, and performance data, independent of the Monitoring Application that produces it. This offers the possibility to store data for additional systems, including open source and commercial monitoring systems, databases, and even hardware, such as detectors or sensors. It also allows the integration of Application Monitoring data known as MBeans.

%prep
%setup -q 

%pre
# Check for JAVA and make sure the it uses the JDK
if [ -z $JAVA_HOME ] ; then
/bin/echo "You don't have JAVA installed or the environment variable JAVA_HOME is not set correctly. Please install Java version version 1.5.x and set the JAVA_HOME environment variable in the root profile."
exit 1
fi
if [ ! -x "$JAVA_HOME"/bin/java -o ! -x "$JAVA_HOME"/bin/jdb -o ! -x "$JAVA_HOME"/bin/javac ]; then
/bin/echo "The JAVA_HOME environment variable is not defined correctly. This environment variable is needed to run this program. NB: JAVA_HOME should point to a JDK not a JRE."
exit 1
fi
#Check JAVA VERSION, (we need 1.5 )
JAVA=$JAVA_HOME/bin/java
$JAVA -version > JAVA_VERSION 2>&1
/bin/cat JAVA_VERSION |grep 1.5 > JAVA_VERSION.1.5
file="JAVA_VERSION.1.5"
if ! [ -s "$file" ] ; then
/bin/echo  "Please install Java version 1.5.x and and set the JAVA_HOME environment variable in the root profile."
exit 1
fi
/bin/echo "Current Java version: "
$JAVA_HOME/bin/java -version

# Check MySQL settings
mysql --user=root --password=$MYSQL_ROOT -e 'select User from mysql.user limit 1'
if [ $? -ne 0 ] ; then
  echo "ERROR:  mysql failed; exiting!"
  echo "	Make sure mysql password is set in root's environment variable:"
  echo "	export MYSQL_ROOT=mysql-passwd"
  exit 1
fi

/usr/sbin/groupadd nagios &>/dev/null
if ! /usr/bin/id nagios &>/dev/null; then
        /usr/sbin/useradd -r -d %{prefix}/users/nagios -s /bin/bash -g "nagios" nagios || \
echo "Unexpected error adding user \"nagios\". Aborting installation."
fi

#Delete temp directories
rm -rf %{prefix}/foundation/container/work &>/dev/null
rm -rf %{prefix}/foundation/container/work* &>/dev/null


%build

cp -r usr %{_tmppath}/%{name}/

find . -type d | sed '1,2d;s,^\.,\%attr(-\,nagios\,nagios) \%dir ,' >  %{filelist}
find . -type f | sed 's,^\.,\%attr(-\,nagios\,nagios) ,' >>  %{filelist}
find . -type l | sed 's,^\.,\%attr(-\,nagios\,nagios) ,' >>  %{filelist}
sed '1d' %{filelist} > %{filelist}.tmp
/bin/mv -f %{filelist}.tmp %{filelist}

%install

/bin/chmod +x %{prefix}/foundation/feeder/nagios2collage_socket.pl
/bin/chmod +x %{prefix}/foundation/feeder/check-listener.pl

/bin/chmod -R 774 %{prefix}/config
/bin/chmod -R 777 %{prefix}/foundation/container/logs

%post 

# Check ip
ip=$(ifconfig $interface | awk '$1=="inet" { split($2, line, ":"); print line[2]; exit}') 
# Just in case if user have more then one network interface, we will use first one
ip=$(echo $ip |awk '{ print $1; }')
echo Server IP address: $ip
sed -e 's/localhost/'$ip'/g' %{prefix}/foundation/container/etc/foundation.xml > %{prefix}/foundation/container/etc/foundation.xml.SAVE
mv %{prefix}/foundation/container/etc/foundation.xml.SAVE %{prefix}/foundation/container/etc/foundation.xml
/bin/chown nagios.nagios %{prefix}/foundation/container/etc/foundation.xml

install_mode="$1"
if [ -n "$MIGRATE4550" ] ; then
install_mode="2"
fi
if [ "$install_mode" = "2" ] ; then  # upgrade... Don't drop databases..
/bin/echo "Upgrade the Foundation database by invoking the migration script..."
if [ -n "$MYSQL_ROOT" ] ; then
/bin/echo "MySQL root passwd is set..."
/bin/echo "Run Foundation migration script..."
mysql -uroot -p"$MYSQL_ROOT" GWCollageDB     < %{prefix}/foundation/database/migrate-gwcollagedb.sql 

elif [ -z "$MYSQL_ROOT" ] ; then
/bin/echo "MySQL root passwd is not set..."
/bin/echo "Run Foundation migration script..."
mysql -uroot GWCollageDB     < %{prefix}/foundation/database/migrate-gwcollagedb.sql 
fi

elif [ "$install_mode" = "1" ] ; then # first install creating databases
/bin/echo "Creating GWCollage database..."
if [ -n "$MYSQL_ROOT" ] ; then
/bin/echo "MySQL root passwd is set..."
/bin/echo "Ok..."
mysql -uroot -p"$MYSQL_ROOT" mysql < %{prefix}/foundation/database/create-production-db.sql
/bin/echo "Loading Foundation Schema..."
mysql -uroot -p"$MYSQL_ROOT" GWCollageDB     < %{prefix}/foundation/database/GWCollageDB.sql 
mysql -uroot -p"$MYSQL_ROOT" GWCollageDB     < %{prefix}/foundation/database/GWCollage-State.sql 
mysql -uroot -p"$MYSQL_ROOT" GWCollageDB     < %{prefix}/foundation/database/GWCollage-Console.sql 
mysql -uroot -p"$MYSQL_ROOT" GWCollageDB     < %{prefix}/foundation/database/GWCollage-Metadata.sql
mysql -uroot -p"$MYSQL_ROOT" GWCollageDB     < %{prefix}/foundation/database/GWCollage-Version.sql 
mysql -uroot -p"$MYSQL_ROOT" GWCollageDB     < %{prefix}/foundation/database/nagios-properties.sql 
mysql -uroot -p"$MYSQL_ROOT" GWCollageDB     < %{prefix}/foundation/database/system-properties.sql
mysql -uroot -p"$MYSQL_ROOT" GWCollageDB     < %{prefix}/foundation/database/GWCollage_PerformanceLabelData.sql

elif [ -z "$MYSQL_ROOT" ] ; then
/bin/echo "MySQL root passwd is not set..."
mysql -uroot mysql <  %{prefix}/foundation/database/create-production-db.sql 
/bin/echo "Loading Foundation Schema..."
mysql -uroot GWCollageDB     < %{prefix}/foundation/database/GWCollageDB.sql 
/bin/echo "Loading State and Console Metadata..."
mysql -uroot GWCollageDB     < %{prefix}/foundation/database/GWCollage-State.sql 
mysql -uroot GWCollageDB     < %{prefix}/foundation/database/GWCollage-Console.sql 
/bin/echo "Loading Generic Metadata for Foundation..."
mysql -uroot GWCollageDB     < %{prefix}/foundation/database/GWCollage-Metadata.sql
mysql -uroot GWCollageDB     < %{prefix}/foundation/database/GWCollage-Version.sql
/bin/echo "Loading Nagios properties..."
mysql -uroot GWCollageDB     < %{prefix}/foundation/database/nagios-properties.sql
/bin/echo "Loading System properties..."
mysql -uroot GWCollageDB     < %{prefix}/foundation/database/system-properties.sql
/bin/echo "Loading Performance Label data..."
mysql -uroot GWCollageDB     < %{prefix}/foundation/database/GWCollage_PerformanceLabelData.sql
fi
fi

#Configuration Files
/bin/mkdir %{prefix}/config &>/dev/null
/bin/cp -fp %{prefix}/foundation/container/config/*.properties %{prefix}/config &>/dev/null
/bin/chmod -R 774 %{prefix}/config

#Permissions settings

/bin/chmod +x %{prefix}/foundation/feeder/nagios2collage_socket.pl
/bin/chmod +x %{prefix}/foundation/feeder/check-listener.pl

/bin/mkdir %{prefix}/foundation/foundation/container/logs &>/dev/null
/bin/chmod -R 777 %{prefix}/foundation/container/logs

%postun
if [ "$1" = "0" ] ; then # last uninstall
# Remove foundation config files
/bin/mv -f %{prefix}/foundation/container/logs %{prefix} &>/dev/null
/bin/rm -rf %{prefix}/config &>/dev/null
/bin/rm -rf %{prefix}/foundation &>/dev/null
fi

%clean
rm -rf %{_tmppath}/%{name}

%files -f %{filelist}

%changelog

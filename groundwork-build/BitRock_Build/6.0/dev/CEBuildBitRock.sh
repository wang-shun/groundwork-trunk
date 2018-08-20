#!/bin/bash -x
# GroundWork Monitor - The ultimate data integration framework.
# Copyright 2008 GroundWork Open Source, Inc. "GroundWork"
#
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
##
# Build properties for Monitor - Opensource
# The values have to be in sync with the settings in groundwork-private
#

date

Box=$(uname -n | sed 's/.groundwork.groundworkopensource.com//')

# Check distro
if [ -f /etc/redhat-release ] ; then
  RHEL_NO=$(fgrep "release" /etc/redhat-release | awk '{ print $7; }' | sed 's/\./ /g' | awk '{ print $1; }')
  distro=rhel5
  builddir=redhat
  qadir=RH
  machinearch=i386
elif [ -f /etc/SuSE-release ] ; then
  RHEL_NO=$(fgrep "VERSION" /etc/SuSE-release | awk '{ print $3; }' | sed 's/\./ /g' | awk '{ print $1; }')
  distro=rhel5
  builddir=packages
  qadir=SUSE
  machinearch=i586
elif [ -f /etc/mandrake-release ] ; then
  distro='Mandrake'
  builddir=
  echo "Plese set build directory in buildRPM.sh file..."
  exit 1
fi

qasubdir=32bit
bitrock_os=32
arch=$(arch)
if [ "$arch" == "x86_64" ] ; then
  export ARCH='_64'
  libdir=lib64
  qasubdir=64bit
  machinearch=x86_64
  bitrock_os=64
else
  libdir=lib
fi

PATH=$PATH:$HOME/bin
export BR_HOME=/root/build/BitRock/groundwork
export GW_HOME=/usr/local/groundwork
export ANT_HOME=$(which ant|sed 's/\/bin\/ant//')
export MAVEN_HOME=$(which maven|sed 's/\/bin\/maven//')
export LDFLAGS=-L$GW_HOME/$libdir
export LD_RUN_PATH=$GW_HOME/$libdir:$LD_RUN_PATH
export LD_LIBRARY_PATH=$GW_HOME/$libdir:$LD_LIBRARY_PATH
export CPPFLAGS=-I$GW_HOME/include
export RELEASE=$distro$ARCH
export DATE=$(date +%d-%m-%y)
export Build4=$1

export PATH=$JAVA_HOME/bin:$GW_HOME/bin:$PATH:$MAVEN_HOME/bin:$ANT_HOME/bin

HOME=/home/nagios
BASE=$HOME/groundwork-monitor
BASE_OS=$BASE/monitor-os
BASE_CORE=$BASE/monitor-core
BUILD_DIR=$BASE/build
RUN_DIR=/root/build

if [ "$Build4" == "pro" ] ; then
  GWMON=groundwork-pro
elif [ "$Build4" == "" ] ; then
  GWMON=groundwork
else
  echo "Wrong argument..."
  exit -1
fi

# Clean up previous builds
rm -rf /root/build/BitRock/$GWMON-output-linux-*.tar.gz
rm -rf $BR_HOME
rm -rf $GW_HOME
rm -rf $BASE

# Unpack BitRock package
cd $RUN_DIR/BitRock
if [ "$arch" == "x86_64" ] ; then
  scp -rp root@morat:/var/www/html/builds/BitRock/$GWMON-output-linux-x64-200*.tar.gz .
  tar zxf $RUN_DIR/BitRock/$GWMON-output-linux-x64-200*.tar.gz
else
  scp -rp root@morat:/var/www/html/builds/BitRock/$GWMON-output-linux-200*.tar.gz .
  tar zxf $RUN_DIR/BitRock/$GWMON-output-linux-200*.tar.gz
fi

# Update Bookshelf
cd $RUN_DIR
if [ "$bitrock_os" == "32" ] ; then
  . nightlyBuildBookshelfBitRock.sh update_svn
else
  . nightlyBuildBookshelfBitRock.sh
fi

date
# Update Foundation
cd $RUN_DIR
if [ "$bitrock_os" == "32" ] ; then
  . nightlyBuildFoundationBitRock.sh update_svn
else
  . nightlyBuildFoundationBitRock.sh
fi

date
PATH=$PATH:$HOME/bin
export BR_HOME=/root/build/BitRock/groundwork
export GW_HOME=/usr/local/groundwork
export ANT_HOME=$(which ant|sed 's/\/bin\/ant//')
export MAVEN_HOME=$(which maven|sed 's/\/bin\/maven//')
export RELEASE=$distro$ARCH
export DATE=$(date +%d-%m-%y)

export PATH=$JAVA_HOME/bin:$PATH:$MAVEN_HOME/bin:$ANT_HOME/bin

HOME=/home/nagios
BASE=$HOME/groundwork-monitor
BASE_OS=$BASE/monitor-os
BASE_CORE=$BASE/monitor-core
BUILD_DIR=$BASE/build
RUN_DIR=/root/build

# Clean up previous builds
rm -rf $BASE

cd $HOME

svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk groundwork-monitor

#cp -rp $RUN_DIR/SOURCE $BASE/build
mv $BR_HOME/apache2 $BR_HOME/apache2.bitrock
mv $BR_HOME/monarch/bin $BR_HOME/monarch/bin.bitrock
mv $BR_HOME/services $BR_HOME/services.bitrock
ln -s $BR_HOME/common/bin $BR_HOME/bin

if [ "$builddir" == "redhat" ] && [ "$distro" == "rhel5" ] ; then
  /bin/cp -f $BASE/build/set-permissions.sh.rh5 $BASE/build/set-permissions.sh
  chmod +x $BASE/build/set-permissions.sh
  if [ "$arch" == "x86_64" ] ; then
    #RH5 64 bit specific build file for apache
    /bin/cp -f $BASE_CORE/apache/httpd-64.init $BASE_CORE/apache/httpd.init
    /bin/cp -f $BASE_CORE/apache/maven.xml.rh5_64 $BASE_CORE/apache/maven.xml
    /bin/cp -f $BASE_CORE/syslib/maven.xml.rh5_64 $BASE_CORE/syslib/maven.xml
    /bin/cp -f $BASE_CORE/snmp/maven.xml.rh564 $BASE_CORE/snmp/maven.xml
    /bin/cp -rf /usr/local/groundwork/lib/php/20060613/ /usr/local/groundwork/lib64/
  else
    # For RH5 only, since it gets LDAP error with:
    # with-ldap=${org.groundwork.deploy.prefix}
    /bin/cp -f $BASE_CORE/apache/maven.xml.rh5 $BASE_CORE/apache/maven.xml
    /bin/cp -f $BASE_CORE/syslib/maven.xml.rh5 $BASE_CORE/syslib/maven.xml
    test_machine="172.28.113.224"
  fi
elif [ "$builddir" == "redhat" ] && [ "$distro" == "rhel4" ] ; then
  if [ "$arch" == "x86_64" ] ; then
    #RH4 64 bit specific build file for nagios/sqlite
    /bin/cp -f $BASE_CORE/nagios/maven.xml.RH4-64 $BASE_CORE/nagios/maven.xml
    /bin/cp -f $BASE_CORE/apache/httpd-64.init $BASE_CORE/apache/httpd.init
  else
    #RH4 32 bit specific build file for nagios/sqlite
    /bin/cp -f $BASE_CORE/nagios/maven.xml.RH4-32 $BASE_CORE/nagios/maven.xml
    test_machine="test_3"
  fi
elif [ "$builddir" == "packages" ] ; then
  if [ "$distro" == "sles9" ] ; then
    # Build GDBM package for NTOP for SuSE9
    /bin/cp -f $BASE/monitor-core/gd2/maven.xml.suse9 $BASE/monitor-core/gd2/maven.xml
  elif [ "$arch" != "x86_64" ] ; then
    # The new SuSE10 is rebuild. New SuSE10 has libexpat.so.0 linked to libexpat.so.1.=5.0
    # on its system library, but this linked-library is missing on the other SuSE10
    /bin/cp -f $BASE_OS/build/master-build.sh.suse1032 $BASE_OS/build/master-build.sh
    test_machine="172.28.113.106"
  else
    /bin/cp -f $BASE_CORE/apache/httpd-64.init $BASE_CORE/apache/httpd.init
    test_machine="vevey"
  fi
fi

/bin/cp $BASE_OS/maven-sb.xml $BASE/maven.xml
/bin/rm -rf $BUILD_DIR/master-build.sh
/bin/cp $BASE_OS/build/master-build.sh $BUILD_DIR/master-build-os.sh

# Increment core-build number
br_release=$(fgrep 6.0- $BR_HOME/project.xml | sed -e 's/6.0-/6.0 /' | sed -e 's/>/ /g' | sed -e 's/</ /g' | awk '{ print $3; }')

# Increment core-build number
release=$(fgrep "org.groundwork.rpm.$RELEASE.release.number" $BASE_OS/project.properties |awk '{ print $3; }')
new_release=`expr $release + 1`

old_release=$(fgrep "org.groundwork.rpm.release.number" $BASE_OS/project.properties |awk '{ print $3; }')

# Set new core-build release number
sed -e 's/org.groundwork.rpm.release.number = '$old_release'/org.groundwork.rpm.release.number = '$new_release'/' $BASE_OS/project.properties >  $BASE_OS/project.properties.tmp1
mv  $BASE_OS/project.properties.tmp1  $BASE_OS/project.properties

sed -e 's/org.groundwork.rpm.'$RELEASE'.release.number = '$release'/org.groundwork.rpm.'$RELEASE'.release.number = '$new_release'/' $BASE_OS/project.properties >  $BASE_OS/project.properties.tmp2
mv -f $BASE_OS/project.properties.tmp2  $BASE_OS/project.properties

# Commit project.properties back to subversion
echo "Increment build(release) number" > svnmessage
svn commit --username build --password bgwrk08 $BASE_OS/project.properties -F svnmessage
rm -rf svnmessage

# Added distro to pro release name
sed -e 's/org.groundwork.rpm.release.number = '$new_release'/org.groundwork.rpm.release.number = '$new_release'.'$RELEASE'/' $BASE_OS/project.properties > $BASE_OS/project.properties.tmp
mv $BASE_OS/project.properties.tmp  $BASE_OS/project.properties

# Updated package name
# Set new package release number
sed -e 's/6.0-'$br_release'/6.0-br'$br_release'-gw'$new_release'/' $BR_HOME/project.xml > $BR_HOME/project.xml.tmp2
mv -f $BR_HOME/project.xml.tmp2 $BR_HOME/project.xml

# Updated project.properties for BitRock
sed 's/\/usr\/local\/groundwork/\/root\/build\/BitRock\/groundwork/g' $BASE_OS/project.properties > $BASE_OS/project.properties.tmp
mv -f $BASE_OS/project.properties.tmp $BASE_OS/project.properties
sed 's/\/usr\/local\/groundwork/\/root\/build\/BitRock\/groundwork/g' $BASE_CORE/project.properties > $BASE_CORE/project.properties.tmp
mv -f $BASE_CORE/project.properties.tmp $BASE_CORE/project.properties
sed 's/\/usr\/local\/groundwork/\/root\/build\/BitRock\/groundwork/g' $BASE/project.properties > $BASE/project.properties.tmp
mv -f $BASE/project.properties.tmp $BASE/project.properties
PWD=`pwd`

# Update build properties
grep -v core $BR_HOME/build.properties > $BR_HOME/build.properties.tmp
echo core=6.0-br$br_release-gw$new_release >> $BR_HOME/build.properties.tmp
mv -f $BR_HOME/build.properties.tmp $BR_HOME/build.properties

cat $BR_HOME/groundwork-core.xml | sed 's/name="core_build" value="/name="core_build" value="6.0-br'$br_release'-gw'$new_release'/' > $BR_HOME/groundwork-core.xml.tmp
mv -f $BR_HOME/groundwork-core.xml.tmp $BR_HOME/groundwork-core.xml

# Patch for x86_64 arch
if [ "$arch" == "x86_64" ] ; then
  cd $BUILD_DIR/build-x86_64
  . install64bitPatch.sh
fi

# Run dos2unix for all the files under profiles directory
chmod +x $BUILD_DIR/d2unix.pl
$BUILD_DIR/d2unix.pl $BASE_OS/profiles

export PERLCC=gcc
echo "Default Perl compiler: ($PERLCC)"

# Start master build script
cd $BASE
maven allBuild
maven allDeploy

#*******************************
if ! [ -d $BR_HOME/monarch/cgi-bin/monarch ] ; then
  mkdir -p $BR_HOME/monarch/cgi-bin/monarch
fi
if ! [ -d $BR_HOME/reports/cgi-bin/reports ] ; then
  mkdir -p $BR_HOME/reports/cgi-bin/reports
fi
cp -rp $BASE/monarch/*.cgi $BR_HOME/monarch/cgi-bin/monarch
cp -rp $BR_HOME/apache2/cgi-bin/reports/* $BR_HOME/reports/cgi-bin/reports
cp -rp $BR_HOME/apache2/htdocs/monarch/* $BR_HOME/monarch/htdocs/monarch
cp -rp $BR_HOME/apache2/htdocs/reports/images/* $BR_HOME/reports/htdocs/reports/images
rm -rf $BR_HOME/apache2
mv $BR_HOME/apache2.bitrock $BR_HOME/apache2
mkdir $BR_HOME/apache2/conf/groundwork
#*******************************

##******************************
cp -rp $BASE_OS/database/create-monitor-sb-db.sql $BR_HOME/databases
cp -rp $BASE_OS/database/foundation-pro-extension.sql $BR_HOME/databases
##******************************

cp -rp $BASE/reports/perl/gwir.cfg $BR_HOME/reports/etc/gwir.cfg
cp -rp $BASE/reports/utils/*.pl $BR_HOME/reports/utils
cp -rp $BASE/reports/utils/dashboard_nagios_create.sql $BR_HOME/databases
rm -rf $BR_HOME/etc

###*****************************
mv -f $BR_HOME/guava/htdocs $BR_HOME/guava.htdocs
cp -rp $BR_HOME/guava/* $BR_HOME/guava.htdocs/guava
rm -rf $BR_HOME/guava/*
mv $BR_HOME/guava.htdocs $BR_HOME/guava/htdocs
cp -rp $BASE_OS/guava/includes/config.inc.php $BR_HOME/guava/htdocs/guava/includes
cp -rp $BASE_OS/guava/packages/reports/package.pkg $BR_HOME/guava/htdocs/guava/packages/reports
cp -rp $BASE_OS/guava/packages/* $BR_HOME/guava/htdocs/guava/packages
cp -rp $BASE/sv2 $BR_HOME/guava/htdocs/guava/packages
cp -rp $BASE_CORE/apache/php.ini $BR_HOME/php/etc
mkdir -p $BR_HOME/guava/htdocs/guava/backup
###*****************************

cp -rp $BASE_OS/migration/* $BR_HOME/migration
cp -rp $BASE/monarch/migration/*.pl $BR_HOME/migration
cp -rp $BASE/monarch/database/monarch.sql $BR_HOME/databases
cp -rp $BASE/monarch/*.css $BR_HOME/monarch/htdocs/monarch
cp -rp $BASE/monarch/*.js $BR_HOME/monarch/htdocs/monarch
cp -rp $BASE/monarch/*.pm $BR_HOME/monarch/lib
cp -rp $BASE/monarch/standalone $BR_HOME/monarch/lib
mv -f $BR_HOME/foundation/database/migrate-gwcollagedb.sql $BR_HOME/migration

####****************************
if ! [ -d monarch/csvimport ] ; then
  mkdir -p $BR_HOME/monarch/csvimport
fi
cp -rp $BASE/monarch/standalone $BR_HOME/monarch/cgi-bin/monarch/lib
cp -rp $BASE_OS/profiles/automation/conf/* $BR_HOME/monarch/automation/conf
rm -rf $BR_HOME/monarch/bin
mv $BR_HOME/monarch/bin.bitrock $BR_HOME/monarch/bin
####****************************

cp -rp $BASE_CORE/nagios/plugins-gwcustom/* $BR_HOME/nagios/libexec
cp -rp $BASE_CORE/nagios/etc/* $BR_HOME/nagios/etc

# New login page
rm -rf $BR_HOME/guava/htdocs/guava/themes/gwmos
cp -rp $BASE/monitor-professional/guava/themes/gwmos $BR_HOME/guava/htdocs/guava/themes

# Installer images
scp -rp $BASE/images/installer/* $BR_HOME/images

cd $BR_HOME/common/lib/openradius
rm -rf radclient
ln -s ../../bin/radclient .
cd $PWD

cp -rp $BR_HOME/services/* $BR_HOME/services.bitrock
rm -rf $BR_HOME/services
mv $BR_HOME/services.bitrock $BR_HOME/services

rm -rf $BR_HOME/share

#mkdir $BR_HOME/performance/cgi-bin/graphs
cp -rp $BASE_CORE/nagios/performance/cgi/* $BR_HOME/performance/cgi-bin/graphs

#mkdir $BR_HOME/performance/cgi-bin/performance
cp -rp $BASE_OS/performance/perfchart.cgi $BR_HOME/performance/cgi-bin/performance
cp -rp $BASE_OS/performance-core/admin/PerfConfigAdmin.pl $BR_HOME/performance/cgi-bin/performance
cp -rp $BASE_OS/performance/PerfChartsForms.pm $BR_HOME/performance/cgi-bin/lib

# New login page
rm -rf $BR_HOME/guava/htdocs/guava/themes/gwmos
cp -rp $BASE_OS/guava/themes/gwmos $BR_HOME/guava/htdocs/guava/themes

# Update Home page
cp -rp $BASE_OS/guava/packages/guava/templates/home-osv.xml $BR_HOME/guava/htdocs/guava/packages/guava/templates
export BUILDTIME=$(date +%Y-%m-%d@%H:%M | sed 's/ //')
sed -e 's/build_date_time/'$BUILDTIME' - br'$br_release' - gw'$new_release'/' $BR_HOME/guava/htdocs/guava/packages/guava/templates/home-osv.xml > $BR_HOME/guava/htdocs/guava/packages/guava/templates/home-osv.xml.tmp
mv -f $BR_HOME/guava/htdocs/guava/packages/guava/templates/home-osv.xml.tmp $BR_HOME/guava/htdocs/guava/packages/guava/templates/home-osv.xml

/usr/bin/perl -p -i -e "s:/usr/local/groundwork/nagios:\@\@BITROCK_NAGIOS_ROOTDIR\@\@:g" $BR_HOME/nagios/etc/nagios.cfg

# Set permissions
chmod +x $BR_HOME/performance/cgi-bin/graphs/*
chmod +x $BR_HOME/performance/cgi-bin/performance/*
chmod +x $BR_HOME/monarch/cgi-bin/monarch/*
chmod +x $BR_HOME/reports/cgi-bin/reports/*
chmod +x $BR_HOME/reports/utils/*.pl
chmod +x $BR_HOME/services/gwservices
chmod +x $BR_HOME/nagios/eventhandlers/*.pl
chmod +x $BR_HOME/services/feeder-nagios-status/run
chmod +x $BR_HOME/migration/*.pl
chmod +x $BR_HOME/nagios/libexec/*.pl
chmod +x $BR_HOME/nagios/libexec/*.sh
chmod +x $BR_HOME/nagios/libexec/*.php
find $BR_HOME -name run -exec chmod +x {} \;
find $BR_HOME -name perfchart.cgi -exec chmod +x {} \;

rm -rf $BR_HOME/monarch/lib/nagios2collage.pm
rm -rf $BR_HOME/performance/cgi-bin/lib
rm -rf $BR_HOME/monarch/cgi-bin/monarch/lib
rm -rf $BR_HOME/apache2/cgi-bin/monarch
rm -rf $BR_HOME/apache2/cgi-bin/reports
rm -rf $BR_HOME/apache2/htdocs/monarch
rm -rf $BR_HOME/apache2/htdocs/reports
rm -rf $BR_HOME/guava/htdocs/guava/maven.xml
rm -rf $BR_HOME/guava/htdocs/guava/project.xml
rm -rf $BR_HOME/guava/htdocs/guava/packages/sv2/maven.xml
rm -rf $BR_HOME/guava/htdocs/guava/packages/sv2/project.xml
rm -rf $BR_HOME/guava/htdocs/guava/themes/guava

# Remove unused plugins
rm -rf $BR_HOME/nagios/libexec/check_mysql.pl
rm -rf $BR_HOME/nagios/libexec/check_temp_fsc
rm -rf $BR_HOME/nagios/libexec/check_temp_cpq
rm -rf $BR_HOME/nagios/libexec/heck_swap_remote.pl
rm -rf $BR_HOME/nagios/libexec/check_backup.pl
rm -rf $BR_HOME/nagios/libexec/check_arping.pl
rm -rf $BR_HOME/bin
rm -rf $BR_HOME/lib
rm -rf $BR_HOME/tmp
rm -rf $BR_HOME/var

# GWMON-5474 & GWMON-5475
cat $BR_HOME/monarch/lib/MonarchStorProc.pm | sed 's/if ( \$res =\~ \/NAGIOS ok|Starting nagios ..done|Nagios start complete\/i ) {/if (\$res =\~ m{.\*\/nagios\/scripts\/ctl\\.sh\\s\*:\\s+nagios\\s+started.*\\s\*$}si) {/' > $BR_HOME/monarch/lib/MonarchStorProc.pm.tmp
mv -f $BR_HOME/monarch/lib/MonarchStorProc.pm.tmp $BR_HOME/monarch/lib/MonarchStorProc.pm

# GWMON-5476
rm -rf /usr/local/groundwork/services/feeder-nagios-log

# GWMON-5861
cp -rp $BR_HOME/monarch/htdocs/monarch/images/services.gif $BR_HOME/monarch/htdocs/monarch/images/logos

# GWMON-5795
cp -rp $BASE_OS/syslib/groundwork.logrotate $BR_HOME/common/etc 

# GWMON-5861
cp -rp $BASE_CORE/nagios/share/images/logos/services.gif $BR_HOME/nagios/share/images/logos

# GWMON-5856
rm -rf $BR_HOME/nagios/libexec/check_mysql_status

# GWMON-2236
rm -rf $BR_HOME/nagios/libexec/check_sensors

# GWMON-5661
rm -rf $BR_HOME/nagios/libexec/check_nagios_status_log.pl

# GWMON-5958
rm -rf $BR_HOME/nagios/share/images/logos
cp -rp $BASE_CORE/nagios/share/images/logos $BR_HOME/nagios/share/images

# GWMON-5984
chmod 755 $BR_HOME/profiles/cgi-bin
chmod 444 $BR_HOME/profiles/*.xml
chmod 444 $BR_HOME/profiles/*.gz
chmod 755 $BR_HOME/nagios/libexec

# GWMON-5369
rm -rf $BR_HOME/reports/utils/utils

# GWMON-4353
cp -rp $BR_HOME/bookshelf/docs/whphost.js $BR_HOME/bookshelf/docs/bookshelf-data

# GWMON-5751
cp -rp $BASE/guava/includes/guavaobject.inc.php $BR_HOME/guava/htdocs/guava/includes

# GWMON-6066
chmod 640 $BR_HOME/core-config/*properties*


cp -p $BASE_CORE/apache/index.html $BR_HOME/apache2/htdocs

# 6.0 only
# Update Apache config file
cp -p $BASE_CORE/apache/httpd.conf.6.0 $BR_HOME/apache2/conf/httpd.conf




find $BR_HOME -name .svn -exec rm -rf {} \;
chown -R 1001:nagioscmd $BR_HOME

svn info /home/nagios/groundwork-monitor/monitor-os/project.properties | grep Revision: | awk '{ print $2; }' > $RUN_DIR/logs/BitRock-gw$new_release

date
echo "CEBuildBitRock.sh is done..."

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

export PATH=$JAVA_HOME/bin:$GW_HOME/bin:$PATH:$MAVEN_HOME/bin:$ANT_HOME/bin

HOME=/home/nagios
BASE=$HOME/groundwork-monitor
BASE_OS=$BASE/monitor-os
BASE_CORE=$BASE/monitor-core
BUILD_DIR=$BASE/build
RUN_DIR=/root/build

# Clean up previous builds
rm -rf /groundwork-monitor-pro*
rm -rf /usr/local/groundwork/

cd $HOME
rm -rf groundwork-monitor
rm -rf groundwork-professional
# Check out from subversion
svn co --username build --password bgwrk http://geneva/groundwork-professional/trunk groundwork-professional

$HOME/groundwork-professional/build/prepare-pro.sh

# Increment core-build number 
release=$(fgrep "org.groundwork.rpm.release.number" $HOME/groundwork-monitor/monitor-professional/project.properties |awk '{ print $3; }')
if [ "$bitrock_os" == "32" ] ; then
  new_release=`expr $release + 1`
else
  new_release=$release
fi

# Set new core-build release number 
sed -e 's/org.groundwork.rpm.release.number = '$release'/org.groundwork.rpm.release.number = '$new_release'/' $HOME/groundwork-monitor/monitor-professional/project.properties >  $HOME/groundwork-monitor/monitor-professional/project.properties.tmp
mv $HOME/groundwork-monitor/monitor-professional/project.properties.tmp  $HOME/groundwork-monitor/monitor-professional/project.properties

# Commit core project.properties back to subversion 
echo "Increment build(release) number" > svnmessage
if [ "$bitrock_os" == "32" ] ; then
  svn commit --username build --password bgwrk08 $HOME/groundwork-monitor/monitor-professional/project.properties -F svnmessage
fi
rm -rf svnmessage

# Update build properties
grep -v pro $BR_HOME/build.properties > $BR_HOME/build.properties.tmp
echo pro=6.0-$new_release >> $BR_HOME/build.properties.tmp
mv -f $BR_HOME/build.properties.tmp $BR_HOME/build.properties

# Update monitor portal properties
cd $BASE
svn co -N http://archive.groundworkopensource.com/groundwork-opensource/trunk/foundation foundation.tmp

# Increment foundation OS version
foundationOs=$(fgrep "org.groundwork.os.version" $BASE/foundation.tmp/project.properties | sed 's/\./ /g' | awk '{ print $6; }')
OldPortalOS=$(fgrep "org.itgroundwork.version" $BASE/monitor-portal/project.properties | sed 's/\./ /g' | awk '{ print $7; }')

# Set new foundation-build release number
sed -e 's/org.itgroundwork.version = 3.0.'$OldPortalOS'/org.itgroundwork.version = 3.0.'$foundationOs'/' $BASE/monitor-portal/project.properties >  $BASE/monitor-portal/project.properties.tmp
mv $BASE/monitor-portal/project.properties.tmp $BASE/monitor-portal/project.properties

# Commit monitor portal project.properties back to subversion
echo "Increment build(release) number" > svnmessage
if [ "$bitrock_os" == "32" ] ; then
  svn commit --username build --password bgwrk08 $BASE/monitor-portal/project.properties -F svnmessage
fi
rm -rf svnmessage

# Update monitor's pro version
cat $BR_HOME/groundwork-pro.xml | sed 's/name="pro_build" value="/name="pro_build" value="6.0-'$new_release'/' > $BR_HOME/groundwork-pro.xml.tmp
mv -f $BR_HOME/groundwork-pro.xml.tmp $BR_HOME/groundwork-pro.xml

# Run dos2unix for all the files under profiles directory
chmod +x $BUILD_DIR/d2unix.pl
$BUILD_DIR/d2unix.pl /home/nagios/groundwork-monitor/monitor-professional/profiles

# Start master build script
cd $BASE/monitor-professional
maven allBuild allDeploy

# Build monitor portal
cd $BASE/monitor-portal
maven deploy

find /usr/local/groundwork -name .svn -exec rm -rf {} \;
find /usr/local/groundwork -name .project -exec rm -rf {} \;
find /usr/local/groundwork -name maven.xml -exec rm -rf {} \;
find /usr/local/groundwork -name project.xml -exec rm -rf {} \;

# Set permissions
cd $BASE/build
. set-permissions-pro.sh

date

# Update home page
export BUILDTIME=$(date +%Y-%m-%d@%H:%M | sed 's/ //')
new_release_core=$(grep core $BR_HOME/build.properties | sed 's/-gw/ /' | awk '{ print $2; }')

sed -e 's/Build reference: /Build reference: '$BUILDTIME' - br'$br_release' - gw'$new_release_core'/' $GW_HOME/guava/packages/guava/templates/home-pro.xml > $GW_HOME/guava/packages/guava/templates/home-pro.xml.tmp
mv $GW_HOME/guava/packages/guava/templates/home-pro.xml.tmp $GW_HOME/guava/packages/guava/templates/home-pro.xml

cp -rp $GW_HOME/apache2/cgi-bin/profiles $BR_HOME/profiles/cgi-bin

cp -rp $GW_HOME/bin/* $BR_HOME/common/bin

cp -rp $GW_HOME/config/* $BR_HOME/core-config
cp -p $BR_HOME/core-config/db.properties $BR_HOME/core-config/db.properties.pro

cp -rp $GW_HOME/databases/* $BR_HOME/databases
cp -rp $BASE/monitor-professional/database/create-monitor-professional-db.sql $BR_HOME/databases

cp -rp $GW_HOME/foundation/container/contexts/groundwork-console.xml $BR_HOME/foundation/container/contexts
cp -rp $GW_HOME/foundation/container/webapps/groundwork-console.war $BR_HOME/foundation/container/webapps
cp -rp $GW_HOME/foundation/feeder/* $BR_HOME/foundation/feeder
cp -p $GW_HOME/log-reporting/lib/LogFile.pm $BR_HOME/foundation/feeder
cp -rp $GW_HOME/foundation/scripts/reset_passive_check.sh $BR_HOME/foundation/scripts

mv $BR_HOME/guava/htdocs/guava/packages/guava $BR_HOME/guava/htdocs/guava/packages/guava.CE
cp -p $GW_HOME/guava/includes/config.inc.php.pro $BR_HOME/guava/htdocs/guava/includes
cp -rp $GW_HOME/guava/packages/* $BR_HOME/guava/htdocs/guava/packages
cp -p $BR_HOME/guava/htdocs/guava/packages/guava/templates/home-pro.xml $BR_HOME/guava/htdocs/guava/packages/guava.CE/templates
rm -rf $BR_HOME/guava/htdocs/guava/packages/guava
mv -f $BR_HOME/guava/htdocs/guava/packages/guava.CE $BR_HOME/guava/htdocs/guava/packages/guava

cp -rp $GW_HOME/gwreports/* $BR_HOME/gwreports

cp -rp $GW_HOME/log-reporting/* $BR_HOME/log-reporting

cp -p $GW_HOME/apache2/cgi-bin/snmp/mibtool/index.cgi $BR_HOME/snmp/cgi-bin/snmp/mibtool
cp -rp $GW_HOME/apache2/htdocs/snmp/mibtool/* $BR_HOME/snmp/htdocs/snmp/mibtool

cp -rp $GW_HOME/migration/* $BR_HOME/migration

cp -rp $GW_HOME/monarch/automation/conf/* $BR_HOME/monarch/automation/conf

cp -rp $BASE_CORE/nagios/etc/nagios.cfg $BR_HOME/nagios/etc
cp -rp $GW_HOME/nagios/etc/nagios.initd.pro $BR_HOME/nagios/etc
cp -rp $GW_HOME/nagios/eventhandlers/* $BR_HOME/nagios/eventhandlers

cp -rp $GW_HOME/profiles/* $BR_HOME/profiles

cp -rp $GW_HOME/sbin/* $BR_HOME/common/sbin

cp -rp $GW_HOME/tools $BR_HOME

cp -rp $GW_HOME/foundation/container/webapps/jboss/jboss-portal.sar/*.war $BR_HOME/foundation/container/webapps/jboss/jboss-portal.sar
cp -rp $GW_HOME/foundation/container/webapps/jboss/jboss-portal.sar/lib/*.jar $BR_HOME/foundation/container/webapps/jboss/jboss-portal.sar/lib

/usr/bin/perl -p -i -e "s:/usr/local/groundwork/nagios:\@\@BITROCK_NAGIOS_ROOTDIR\@\@:g" $BR_HOME/nagios/etc/nagios.cfg

cp -rp $BASE/monitor-portal/applications/statusviewer/src/main/resources/*.properties $BR_HOME/core-config
cp -rp $BASE/monitor-portal/applications/reportserver/src/main/resources/*.properties $BR_HOME/core-config
mv $BR_HOME/core-config/*ViewerResources_??.properties $BR_HOME/core-config/resources

cp -rp $BASE/foundation/container/run.conf $BR_HOME/foundation/container
cp -rp $BR_HOME/core-config/jboss-service.xml $BR_HOME/foundation/container/config/jboss

# GWMON-5830
cp -rp $BASE_OS/performance/perfchart.cgi $BR_HOME/performance/cgi-bin/performance
cp -rp $BASE_OS/performance/perfchart.cgi $BR_HOME/performance/htdocs/performance

# GWMON-5834
mkdir $GW_HOME/apache2/conf/groundwork

# GWMON-5841
cp -rp $BASE/monitor-professional/profiles/plugins/*.pl $BR_HOME/nagios/libexec
cp -rp $BASE/monitor-professional/profiles/plugins/*.sh $BR_HOME/nagios/libexec
cp -rp $BASE/monitor-professional/profiles/plugins/com $BR_HOME/nagios/libexec
cp -rp $BASE/monitor-professional/profiles/plugins/nagtomcat.jar $BR_HOME/nagios/libexec

# GWMON-6029
cp -rp $BASE/monitor-professional/profiles/plugins/lib $BR_HOME/nagios/libexec
cp -rp $BASE/monitor-professional/profiles/plugins/sql $BR_HOME/nagios/libexec
cp -rp $BASE/monitor-professional/profiles/plugins/check_oracle* $BR_HOME/nagios/libexec
cp -rp $BASE/monitor-professional/profiles/plugins/DbCheck.class $BR_HOME/nagios/libexec
cp -rp $BASE/monitor-professional/profiles/*-jdbc-oracle.xml $BR_HOME/profiles

# GWMON-5765
rm -rf $BR_HOME/profiles/service-profile-vmware_esx3_services_profile.xml

# GWMON-5861
if [ -f $BR_HOME/performance/htdocs/performance/images/logos ] ; then
  rm -f $BR_HOME/performance/htdocs/performance/images/logos
fi
mkdir -p $BR_HOME/performance/htdocs/performance/images/logos
cp -rp $BASE/foundation/misc/web-application/console/WebContent/assets/icons/services.gif $BR_HOME/performance/htdocs/performance/images/logos

# GWMON-5369
rm -rf $BR_HOME/reports/utils/utils

# GWMON-5976
chmod +x $BASE/monitor-professional/monarch/bin/*
cp -rp $BASE/monitor-professional/monarch/lib/* $BR_HOME/monarch/lib
cp -rp $BASE/monitor-professional/monarch/bin/* $BR_HOME/monarch/bin

# GWMON-5905
cp -rp $BASE/monitor-professional/migration/migrate-monarch.sql $BR_HOME/migration

# GWMON-5985
rm -rf $BR_HOME/foundation/feeder/nagios2master.pl

# GWMON-2600
cp -rp $BASE/monitor-professional/nagios/etc/nagios.cfg $BR_HOME/nagios/etc

# GWMON-5984
chmod 755 $BR_HOME/profiles
chmod 444 $BR_HOME/profiles/*.xml
chmod 444 $BR_HOME/profiles/*.gz
chmod 755 $BR_HOME/nagios/libexec
chmod +x $BR_HOME/nagios/libexec/*.sh
chmod +x $BR_HOME/nagios/libexec/*.pl

# GWMON-6066
chmod 640 $BR_HOME/core-config/*properties*





cp -rp $BASE/monitor-professional/monarch-patch60/monarch.cgi $BR_HOME/monarch/cgi-bin/monarch
#cp -rp $BASE/monitor-professional/monarch-patch60/*.pm $BR_HOME/monarch/lib






# New login page
rm -rf $BR_HOME/guava/htdocs/guava/themes/gwmpro
cp -rp $BASE/monitor-professional/guava/themes/gwmpro $BR_HOME/guava/htdocs/guava/themes

find $BR_HOME -name perfchart.cgi -exec chmod +x {} \;

# Cleanup and set ownership
find $BR_HOME -name .svn -exec rm -rf {} \;
chown -R 1001:nagioscmd $BR_HOME

date
echo "ProBuildBitRock is done..."

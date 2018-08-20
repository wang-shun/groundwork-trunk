#!/bin/bash -x
#Copyright (C) 2009  GroundWork Open Source Solutions info@groundworkopensource.com
#

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
export DATE=$(date +%Y-%m-%d)

export PATH=$JAVA_HOME/bin:$GW_HOME/bin:$PATH:$MAVEN_HOME/bin:$ANT_HOME/bin

HOME=/home/nagios
BASE=$HOME/groundwork-monitor
BASE_OS=$BASE/monitor-os
BASE_CORE=$BASE/monitor-core
BUILD_DIR=$BASE/build
RUN_DIR=/root/build
##HOME_GDK=$HOME/groundwork-gdk
BitRockDir=/opt/installbuilder-6.5.6
MoratDir=/var/www/html/tools/DEVELOPMENT

# Unpack the latest CE
cd $RUN_DIR/BitRock
rm -rf groundwork
tar zxf groundwork-ce.tar.gz

# Check out GDK from subversion
##rm -rf $HOME/groundwork-gdk
cd $HOME
##svn co --username build --password bgwrk http://geneva/groundwork-professional/trunk/gdk groundwork-gdk

# Clean up SVN foot print
##find groundwork-gdk -name .svn -exec rm -rf {} \;

date
cp -p $BR_HOME/project.xml $BR_HOME/project.xml.ce
cp -p $BR_HOME/groundwork-core.xml $BR_HOME/groundwork-core.xml.ce
NewBuildNumber=$(grep "<version>" $BR_HOME/project.xml | sed 's/<version>//' | sed 's/<\/version>//' | sed 's/ //g')
Core_Release=$(grep "value=" $BR_HOME/groundwork-core.xml | grep "default=" | sed 's/=/ /g' | awk '{ print $7; }' | sed 's/"//g')
sed -i 's/product_shortname}/product_shortname}gdk/' $BR_HOME/project.xml
scp -p root@morat:/var/www/html/builds/BitRock/groundwork-core.xml.gdk $BR_HOME/groundwork-core.xml
cat $BR_HOME/groundwork-core.xml | sed 's/name=\"core_build\" value=\"\"/name=\"core_build\" value=\"'$Core_Release'\"/' > $BR_HOME/groundwork-core.xml.tmp
mv -f $BR_HOME/groundwork-core.xml.tmp $BR_HOME/groundwork-core.xml
mkdir $BR_HOME/gdk
cd $BR_HOME/gdk
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/foundation
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/monarch
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/monitor-framework
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/monitor-portal
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/monitor-core/nagios/plugins-gwcustom
scp -rp root@morat:/var/www/html/builds/BitRock/GDK_Tools/gdk/foundation.classpath foundation/.classpath
scp -rp root@morat:/var/www/html/builds/BitRock/GDK_Tools/gdk/foundation.project foundation/.project
scp -rp root@morat:/var/www/html/builds/BitRock/GDK_Tools/gdk/monarch.classpath monarch/.classpath
scp -rp root@morat:/var/www/html/builds/BitRock/GDK_Tools/gdk/monarch.project monarch/.project
scp -rp root@morat:/var/www/html/builds/BitRock/GDK_Tools/gdk/monitor-framework.classpath monitor-framework/.classpath
scp -rp root@morat:/var/www/html/builds/BitRock/GDK_Tools/gdk/monitor-framework.project monitor-framework/.project
scp -rp root@morat:/var/www/html/builds/BitRock/GDK_Tools/gdk/monitor-portal.classpath monitor-portal/.classpath
scp -rp root@morat:/var/www/html/builds/BitRock/GDK_Tools/gdk/monitor-portal.project monitor-portal/.project
scp -rp root@morat:/var/www/html/builds/BitRock/GDK_Tools/gdk/plugins-gwcustom.classpath plugins-gwcustom/.classpath
scp -rp root@morat:/var/www/html/builds/BitRock/GDK_Tools/gdk/plugins-gwcustom.project plugins-gwcustom/.project

##cp -rp $HOME_GDK/foundation.classpath foundation/.classpath
##cp -rp $HOME_GDK/foundation.project foundation/.project
##cp -rp $HOME_GDK/monarch.classpath monarch/.classpath
##cp -rp $HOME_GDK/monarch.project monarch/.project
##cp -rp $HOME_GDK/monitor-framework.classpath monitor-framework/.classpath
##cp -rp $HOME_GDK/monitor-framework.project monitor-framework/.project
##cp -rp $HOME_GDK/monitor-portal.classpath monitor-portal/.classpath
##cp -rp $HOME_GDK/monitor-portal.project monitor-portal/.project
##cp -rp $HOME_GDK/plugins-gwcustom.classpath plugins-gwcustom/.classpath
##cp -rp $HOME_GDK/plugins-gwcustom.project plugins-gwcustom/.project

scp -rp root@morat:/var/www/html/builds/BitRock/GDK_Tools/$qasubdir tools
scp -rp root@morat:/var/www/html/builds/BitRock/GDK_Tools/gdk/.eclipse .
scp -rp root@morat:/var/www/html/builds/BitRock/GDK_Tools/gdk/.metadata .
scp -rp root@morat:/var/www/html/builds/BitRock/GDK_Tools/gdk/eclipse-plugin .

##cp -rp $HOME_GDK/.eclipse .
##cp -rp $HOME_GDK/.metadata .
##cp -rp $HOME_GDK/eclipse-plugin .

cp -rp /root/.maven .
scp -rp root@morat:/var/www/html/builds/BitRock/GDK_Tools/gdk/maven_repository_java-php-bridge ./.maven/repository/java-php-bridge
scp -rp root@morat:/var/www/html/builds/BitRock/GDK_Tools/gdk/monitor-portal_application_sample-php ./monitor-portal/applications/sample-php

##cp -rp $HOME_GDK/maven_repository_java-php-bridge ./.maven/repository/java-php-bridge
##cp -rp $HOME_GDK/monitor-portal_application_sample-php ./monitor-portal/applications/sample-php

chown -R nagios:nagios .

$BitRockDir/bin/builder build $BR_HOME/project.xml --setvars project.filesToIgnoreWhenPacking="CVS .DS_Storage"

NewGDKPackage=groundworkgdk-$NewBuildNumber-linux-$bitrock_os-installer.bin
if ! [ -f $BitRockDir/output/$NewGDKPackage ] ; then
  cat /tmp/masterbuild_error | mail -s "6.0 GDK Build FAILED in `hostname` - $DATE" build-info@gwos.com
  exit -1
fi

ssh root@morat rm -f $MoratDir/builds/BitRock/last_good_build/groundworkgdk-*
ssh root@morat rm -f /DailyBuilds/BitRock/$qasubdir/groundworkgdk-*
ssh root@morat mv -f $MoratDir/builds/BitRock/6.0/groundworkgdk-6.1-*-linux-$bitrock_os-installer.bin /DailyBuilds/BitRock/$qasubdir
scp -p $BitRockDir/output/$NewGDKPackage root@morat:$MoratDir/builds/BitRock/6.0
scp -p $BitRockDir/output/$NewGDKPackage root@morat:$MoratDir/builds/BitRock/last_good_build
scp -p $RUN_DIR/logs/CE_Revision root@morat:$MoratDir/logs/GDK_CE_Revision
scp -p $RUN_DIR/logs/CE_Revision root@morat:$MoratDir/logs/$qasubdir/GDK_CE_Revision

date
echo "nightlyBuildEnt.sh is done..."

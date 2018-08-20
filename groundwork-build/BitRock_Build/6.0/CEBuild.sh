#!/bin/bash -x
#Copyright (C) 2009  GroundWork Open Source Solutions info@groundworkopensource.com
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
MoratDir=/var/www/html/tools/DEVELOPMENT

# Clean up previous builds
rm -rf $BR_HOME
rm -rf $GW_HOME
rm -rf $BASE

cd $RUN_DIR/BitRock
if [ "$Build4" == "pro" ] ; then
  GWMON=groundwork-pro
  cp -rp /usr/local/groundwork-common.ent /usr/local/groundwork
elif [ "$Build4" == "" ] ; then
  GWMON=groundwork
  cp -rp /usr/local/groundwork-common.ce /usr/local/groundwork
else
  echo "Wrong argument..."
  exit -1
fi

# Clean, then unpack BitRock package
rm -rf /root/build/BitRock/$GWMON-output-linux-*.tar.gz
cd $RUN_DIR/BitRock
if [ "$arch" == "x86_64" ] ; then
  scp -rp root@morat:/var/www/html/builds/BitRock/$GWMON-output-linux-x64-200*.tar.gz .
  tar zxf $RUN_DIR/BitRock/$GWMON-output-linux-x64-200*.tar.gz
else
  scp -rp root@morat:/var/www/html/builds/BitRock/$GWMON-output-linux-200*.tar.gz .
  tar zxf $RUN_DIR/BitRock/$GWMON-output-linux-200*.tar.gz
fi


# Update Bookshelf build
bsh_new_release=`cat /usr/local/groundwork/bookshelf_release.txt`
grep -v bookshelf $BR_HOME/build.properties > $BR_HOME/build.properties.tmp
echo bookshelf=6.0-$bsh_new_release >> $BR_HOME/build.properties.tmp
mv -f $BR_HOME/build.properties.tmp $BR_HOME/build.properties

cat $BR_HOME/groundwork-bookshelf.xml | sed 's/name="bookshelf_build" value="/name="bookshelf_build" value="6.0-'$bsh_new_release'/' > $BR_HOME/groundwork-bookshelf.xml.tmp
mv -f $BR_HOME/groundwork-bookshelf.xml.tmp $BR_HOME/groundwork-bookshelf.xml

if ! [ -d $BR_HOME/bookshelf ] ; then
  mkdir $BR_HOME/bookshelf
fi

BookshelfHome=$BR_HOME/bookshelf/htdocs
rm -rf $BR_HOME/bookshelf/docs
mkdir -p $BookshelfHome

cp -rp /usr/local/groundwork/docs $BR_HOME/bookshelf
cp -rp /usr/local/groundwork/docs $BookshelfHome
cp -rp /usr/local/groundwork/guava/packages/bookshelf $BookshelfHome
cp -rp /usr/local/groundwork/migration/gw-bookshelf-install.php $BookshelfHome


# Update Foundation build
fou_new_release=`cat /usr/local/groundwork/foundation_release.txt`
grep -v foundation $BR_HOME/build.properties > $BR_HOME/build.properties.tmp
echo foundation=3.0-$fou_new_release >> $BR_HOME/build.properties.tmp
mv -f $BR_HOME/build.properties.tmp $BR_HOME/build.properties

cat $BR_HOME/groundwork-foundation.xml | sed 's/name="foundation_build" value="/name="foundation_build" value="3.0-'$fou_new_release'/' > $BR_HOME/groundwork-foundation.xml.tmp
mv -f $BR_HOME/groundwork-foundation.xml.tmp $BR_HOME/groundwork-foundation.xml


# Build monitor-os
cd $HOME
#svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk groundwork-monitor
svn co -N http://archive.groundworkopensource.com/groundwork-opensource/trunk groundwork-monitor
cd $BASE
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/bronx 
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/build
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/foundation 
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/guavachat 
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/images 
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/monarch 
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/monitor-core 
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/monitor-framework 
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/monitor-os 
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/monitor-portal
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/nightlybuild 
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/reports 
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/sv2tests 
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/template 


# GWMON-7428
cp -p $BASE_OS/syslib/gwservices.ce $BASE_OS/syslib/gwservices

# Update Foundation
rm -rf $BR_HOME/foundation
cp -rp $GW_HOME/foundation $BR_HOME

# Update core-config
cp -rp $GW_HOME/config/* $BR_HOME/core-config
cp -rp $BASE_OS/resources/db.properties $BR_HOME/core-config/db.properties.os
rm -rf $BR_HOME/config


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
rm -f $BASE_OS/project.properties
svn update $BASE_OS/project.properties
sleep 5

br_release=$(fgrep 6.0- $BR_HOME/project.xml | sed -e 's/6.0-/6.0 /' | sed -e 's/>/ /g' | sed -e 's/</ /g' | awk '{ print $3; }')

release=$(fgrep "org.groundwork.rpm.$RELEASE.release.number" $BASE_OS/project.properties |awk '{ print $3; }')
if [ "$Build4" == "" ] ; then
  new_release=`expr $release + 1`
else
  new_release=$release
fi

old_release=$(fgrep "org.groundwork.rpm.release.number" $BASE_OS/project.properties |awk '{ print $3; }')

# Set new core-build release number
sed -e 's/org.groundwork.rpm.release.number = '$old_release'/org.groundwork.rpm.release.number = '$new_release'/' $BASE_OS/project.properties >  $BASE_OS/project.properties.tmp1
mv  $BASE_OS/project.properties.tmp1  $BASE_OS/project.properties

sed -e 's/org.groundwork.rpm.'$RELEASE'.release.number = '$release'/org.groundwork.rpm.'$RELEASE'.release.number = '$new_release'/' $BASE_OS/project.properties >  $BASE_OS/project.properties.tmp2
mv -f $BASE_OS/project.properties.tmp2  $BASE_OS/project.properties

# Commit project.properties back to subversion
echo "Increment build(release) number" > svnmessage
if [ "$Build4" == "" ] ; then
  svn commit --username build --password bgwrk08 $BASE_OS/project.properties -F svnmessage
fi
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

cp -rp $GW_HOME/config/* $BR_HOME/core-config

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
########cp -rp $BASE_OS/database/foundation-pro-extension.sql $BR_HOME/databases
cp -rp $GW_HOME/foundation/database/foundation-base-data.sql $BR_HOME/databases
##******************************

cp -rp $BASE/reports/perl/gwir.cfg $BR_HOME/reports/etc/gwir.cfg
cp -rp $BASE/reports/utils/*.pl $BR_HOME/reports/utils
cp -rp $BASE/reports/utils/dashboard_nagios_create.sql $BR_HOME/databases
rm -rf $BR_HOME/etc

###*****************************
rm -rf $BR_HOME/guava
cp -rp $BASE_CORE/apache/php.ini $BR_HOME/php/etc
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

cp -rp $BASE_OS/resources/my.cnf $BR_HOME/mysql

cp -rp $BASE_CORE/nagios/plugins-gwcustom/* $BR_HOME/nagios/libexec
cp -rp $BASE_CORE/nagios/etc/* $BR_HOME/nagios/etc

# Installer images
cp -rp $BASE/images/installer/* $BR_HOME/images

cd $BR_HOME/common/lib/openradius
rm -rf radclient
ln -s ../../bin/radclient .
cd $PWD

cp -rp $BR_HOME/services/* $BR_HOME/services.bitrock
rm -rf $BR_HOME/services
mv $BR_HOME/services.bitrock $BR_HOME/services

rm -rf $BR_HOME/share

cp -p $BASE_OS/performance-core/eventhandler/launch_perf_data_processing $BR_HOME/nagios/eventhandlers

cp -rp $BASE_CORE/nagios/performance/cgi/* $BR_HOME/performance/cgi-bin/graphs
cp -p $BASE_OS/performance/perfchart.cgi $BR_HOME/performance/cgi-bin/performance
cp -p $BASE_OS/performance-core/admin/PerfConfigAdmin.pl $BR_HOME/performance/cgi-bin/performance
cp -p $BASE_OS/performance/PerfChartsForms.pm $BR_HOME/performance/cgi-bin/lib

/usr/bin/perl -p -i -e "s:/usr/local/groundwork/nagios:\@\@BITROCK_NAGIOS_ROOTDIR\@\@:g" $BR_HOME/nagios/etc/nagios.cfg

# Set permissions
chmod +x $BR_HOME/performance/cgi-bin/graphs/*
chmod +x $BR_HOME/performance/cgi-bin/performance/*
chmod +x $BR_HOME/monarch/cgi-bin/monarch/*
chmod +x $BR_HOME/reports/cgi-bin/reports/*
chmod +x $BR_HOME/services/gwservices
chmod +x $BR_HOME/reports/utils/*.pl
chmod +x $BR_HOME/nagios/eventhandlers/*
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


# Jira's Fixes

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
##chmod 444 $BR_HOME/profiles/*.gz
chmod 755 $BR_HOME/nagios/libexec

# GWMON-5369
rm -rf $BR_HOME/reports/utils/utils

# GWMON-4353
cp -rp $BR_HOME/bookshelf/docs/whphost.js $BR_HOME/bookshelf/docs/bookshelf-data

# GWMON-6066
chmod 640 $BR_HOME/core-config/*properties*

cp -p $BASE_CORE/apache/index.html $BR_HOME/apache2/htdocs

# 6.0 only
# Update Apache config file
cp -p $BASE_CORE/apache/httpd.conf.6.0 $BR_HOME/apache2/conf/httpd.conf

# GWMON-6842
rm -f $BR_HOME/nagios/libexec/*.c

# GWMON-6503
rm -f $BR_HOME/profiles/perfconfig-vmware_esx3_services_profile.xml

# GWMON-7188
cp -p $BASE/monarch/dassmonarch/* $BR_HOME/monarch/lib
# Remove dassmonarch directory, need to find out where dassmonarch gets copy
rm -rf $BR_HOME/monarch/lib/dassmonarch

# GWMON-5371
###GWMON-7739####cp -p $BASE_CORE/nagios/performance/eventhandlers/process_service_perf.pl $BR_HOME/nagios/eventhandlers

# GWMON-7466
rm -f $BR_HOME/nagios/libexec/check_fan*

#
cp -p $BASE_OS/performance-core/eventhandler/process_service_perfdata_file $BR_HOME/nagios/eventhandlers
chmod +x $BR_HOME/nagios/eventhandlers/*

# GWMON-7665
mkdir -p $BR_HOME/foundation/jboss/bin
cp -p $BASE/foundation/misc/web-application/jboss/twiddle.jar $BR_HOME/foundation/jboss/bin
cp -p $BASE/foundation/misc/web-application/jboss/twiddle.sh $BR_HOME/foundation/jboss/bin
cp -p $BASE_CORE/nagios/plugins-gwcustom/check_jbossjmx.sh $BR_HOME/nagios/libexec
chmod +x $BR_HOME/foundation/jboss/bin/twiddle.jar
chmod +x $BR_HOME/foundation/jboss/bin/twiddle.sh
chmod +x $BR_HOME/nagios/libexec/check_jbossjmx.sh

# GWMON-7739
rm -f $BR_HOME/nagios/eventhandlers/process_service_perf.pl
rm -f $BR_HOME/nagios/eventhandlers/process_service_perf_db.pl

if [ "$Build4" == "" ] ; then
  # Build monitor portal
  cd $BASE/monitor-portal
  maven deploy

  cp -rp $GW_HOME/config/* $BR_HOME/core-config
  cp -rp $GW_HOME/foundation/container/webapps/jboss/jboss-portal.sar/*.war $BR_HOME/foundation/container/webapps/jboss/jboss-portal.sar
  cp -rp $GW_HOME/foundation/container/webapps/jboss/jboss-portal.sar/lib/*.jar $BR_HOME/foundation/container/webapps/jboss/jboss-portal.sar/lib
  cp -rp $BR_HOME/core-config/jboss-service.xml $BR_HOME/foundation/container/config/jboss
  cp -p $GW_HOME/foundation/container/webapps/jboss/jboss-portal.sar/network-service-portlet.war $BR_HOME/foundation/container/webapps/jboss/jboss-portal.sar/network-service-portlet.war
  cp -p $GW_HOME/foundation/container/webapps/jboss/jboss-portal.sar/network-service-portlet.war $BR_HOME/network-service/libs/java

  mkdir $BR_HOME/foundation/scripts
  cp -p $BASE/foundation/resources/reset_passive_check.sh $BR_HOME/foundation/scripts
  chmod +x $BR_HOME/foundation/scripts/reset_passive_check.sh
  rm -f $BR_HOME/common/scripts/ctl-nsca.sh

  # GWMON-7405
  cp -p $BASE/monitor-framework/core/src/resources/portal-server-war/login.jsp $BR_HOME/foundation/container/webapps/jboss/jboss-portal.sar/portal-server.war
  cp -p $BASE/monitor-framework/core/src/resources/portal-core-war/images/* $BR_HOME/foundation/container/webapps/jboss/jboss-portal.sar/portal-core.war/images

  # GWMON-7686
  rm -f $BR_HOME/foundation/container/webapps/birtviewer.war
  rm -f $BR_HOME/foundation/container/webapps/foundation-reportserver.war
fi

 
# Force process_foundation_db_update is off
Process_Foundation_Db_Update=$(grep process_foundation_db_update $BR_HOME/nagios/eventhandlers/process_service_perf_db_file.pl | awk '{ print $4; }' | sed 's/;//')
sed -i 's/process_foundation_db_updates = '$Process_Foundation_Db_Update'/process_foundation_db_updates = 0/' $BR_HOME/nagios/eventhandlers/process_service_perf_db_file.pl
 
Process_Foundation_Db_Update=$(grep process_foundation_db_update $BR_HOME/nagios/eventhandlers/process_service_perfdata_file | awk '{ print $4; }' | sed 's/;//')
sed -i 's/process_foundation_db_updates = '$Process_Foundation_Db_Update'/process_foundation_db_updates = 0/' $BR_HOME/nagios/eventhandlers/process_service_perfdata_file



cp -p $BASE/foundation/resources/exec_rrdgraph.pl $BR_HOME/common/bin




# Update revision number on morat
svn info /home/nagios/groundwork-monitor/monitor-os/project.properties | grep Revision: | awk '{ print $2; }' > $RUN_DIR/logs/CE_Revision




find $BR_HOME -name .svn -exec rm -rf {} \;
chown -R 1001:nagioscmd $BR_HOME

date
echo "Monitor-ce build is done at `date`"
#########################################

echo "CEBuild.sh is done..."

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
export RELEASE=$distro$ARCH
export DATE=$(date +%d-%m-%y)

export PATH=$JAVA_HOME/bin:$GW_HOME/bin:$PATH:$MAVEN_HOME/bin:$ANT_HOME/bin

HOME=/home/nagios
BASE=$HOME/groundwork-monitor
BASE_OS=$BASE/monitor-os
BASE_CORE=$BASE/monitor-core
BUILD_DIR=$BASE/build
RUN_DIR=/root/build
MoratDir=/var/www/html/tools/DEVELOPMENT

# Clean up previous builds
rm -rf $GW_HOME
rm -rf $BASE
cp -rp /usr/local/groundwork-common.ent /usr/local/groundwork

# Build monitor-pro
rm -rf $HOME/groundwork-monitor
rm -rf $HOME/groundwork-professional

# Check out from subversion
cd $HOME
svn co -N --username build --password bgwrk http://geneva/groundwork-professional/trunk groundwork-professional
cd $HOME/groundwork-professional
svn co --username build --password bgwrk http://geneva/groundwork-professional/trunk/build 
svn co --username build --password bgwrk http://geneva/groundwork-professional/trunk/enterprise 
svn co --username build --password bgwrk http://geneva/groundwork-professional/trunk/foundation 
svn co --username build --password bgwrk http://geneva/groundwork-professional/trunk/guava-packages 
svn co --username build --password bgwrk http://geneva/groundwork-professional/trunk/images 
svn co --username build --password bgwrk http://geneva/groundwork-professional/trunk/load-test-tools 
svn co --username build --password bgwrk http://geneva/groundwork-professional/trunk/patch-scripts 
svn co --username build --password bgwrk http://geneva/groundwork-professional/trunk/plugins 
svn co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-portal 
cd $HOME/groundwork-professional
svn co -N --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional monitor-professional
cd $HOME/groundwork-professional/monitor-professional
svn co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/apache 
svn co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/bronx 
svn co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/database 
svn co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/guava 
svn co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/log-reporting 
svn co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/migration 
svn co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/monarch 
svn co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/monarch-patch60 
svn co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/nagios 
svn co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/performance 
svn co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/performance-core 
svn co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/profiles 
svn co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/resources 
svn co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/snmp 
svn co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/sqldata 
svn co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/syslib 
svn co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/tools 

$HOME/groundwork-professional/build/prepare-pro.sh


cd $BASE
# Increment core-build number 
release=$(fgrep "org.groundwork.rpm.release.number" $HOME/groundwork-monitor/monitor-professional/project.properties |awk '{ print $3; }')
if [ "$bitrock_os" == "64" ] ; then
  new_release=`expr $release + 1`
else
  new_release=$release
fi

# Set new core-build release number 
sed -e 's/org.groundwork.rpm.release.number = '$release'/org.groundwork.rpm.release.number = '$new_release'/' $HOME/groundwork-monitor/monitor-professional/project.properties >  $HOME/groundwork-monitor/monitor-professional/project.properties.tmp
mv $HOME/groundwork-monitor/monitor-professional/project.properties.tmp  $HOME/groundwork-monitor/monitor-professional/project.properties

# Commit core project.properties back to subversion 
echo "Increment build(release) number" > svnmessage
if [ "$bitrock_os" == "64" ] ; then
  svn commit --username build --password bgwrk08 $HOME/groundwork-monitor/monitor-professional/project.properties -F svnmessage
fi
rm -rf svnmessage

# Update build properties
grep -v pro $BR_HOME/build.properties > $BR_HOME/build.properties.tmp
echo pro=6.0-$new_release >> $BR_HOME/build.properties.tmp
mv -f $BR_HOME/build.properties.tmp $BR_HOME/build.properties

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
maven prepare 
maven deploy

find /usr/local/groundwork -name .svn -exec rm -rf {} \;
find /usr/local/groundwork -name .project -exec rm -rf {} \;
find /usr/local/groundwork -name maven.xml -exec rm -rf {} \;
find /usr/local/groundwork -name project.xml -exec rm -rf {} \;

# Set permissions
cd $BASE/build
. set-permissions-pro.sh

date
cp -rp $GW_HOME/apache2/cgi-bin/profiles $BR_HOME/profiles/cgi-bin

cp -rp $GW_HOME/bin/* $BR_HOME/common/bin

cp -rp $GW_HOME/config/* $BR_HOME/core-config
cp -p $BR_HOME/core-config/db.properties $BR_HOME/core-config/db.properties.pro

cp -rp $GW_HOME/databases/* $BR_HOME/databases
cp -rp $BASE/monitor-professional/database/create-monitor-professional-db.sql $BR_HOME/databases

cp -rp $GW_HOME/foundation/feeder/* $BR_HOME/foundation/feeder
cp -p $GW_HOME/log-reporting/lib/LogFile.pm $BR_HOME/foundation/feeder
cp -rp $GW_HOME/foundation/scripts $BR_HOME/foundation

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

########cp -rp $BASE/monitor-professional/database/foundation-pro-extension.sql $BR_HOME/databases

cp -p $BASE/monitor-framework/core/src/resources/portal-core-war/images/gwconnect.gif $BR_HOME/foundation/container/webapps/jboss/jboss-portal.sar/portal-core.war/images

##cp -rp $GW_HOME/foundation/container/webapps/jboss/jboss-portal.sar $BR_HOME/foundation/container/webapps/jboss
cp -rp $GW_HOME/foundation/container/webapps/jboss/jboss-portal.sar/*.war $BR_HOME/foundation/container/webapps/jboss/jboss-portal.sar
cp -rp $GW_HOME/foundation/container/webapps/jboss/jboss-portal.sar/lib/*.jar $BR_HOME/foundation/container/webapps/jboss/jboss-portal.sar/lib
cp -rp $BR_HOME/core-config/jboss-service.xml $BR_HOME/foundation/container/config/jboss
cp -p $GW_HOME/foundation/container/webapps/jboss/jboss-portal.sar/network-service-portlet.war $BR_HOME/foundation/container/webapps/jboss/jboss-portal.sar/network-service-portlet.war
cp -p $GW_HOME/foundation/container/webapps/jboss/jboss-portal.sar/network-service-portlet.war $BR_HOME/network-service/libs/java




# Update revision number on morat
svn info /home/nagios/groundwork-monitor/monitor-professional/project.properties | grep Revision: | awk '{ print $2; }' > $RUN_DIR/logs/EE_Revision 




# Update login page
##ls -l $BR_HOME/foundation/container/webapps/jboss/jboss-portal.sar/portal-server.war
##mkdir $BASE/ent-framework
##cd $BASE/ent-framework
##svn co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-framework
##cp -p $BASE/ent-framework/monitor-framework/core/src/resources/portal-server-war/login.jsp $BR_HOME/foundation/container/webapps/jboss/jboss-portal.sar/portal-server.war/login.jsp
##ls -l $BR_HOME/foundation/container/webapps/jboss/jboss-portal.sar/portal-server.war





##mkdir $BR_HOME/foundation/scripts
##cp -p $BASE/foundation/resources/reset_passive_check.sh $BR_HOME/foundation/scripts




/usr/bin/perl -p -i -e "s:/usr/local/groundwork/nagios:\@\@BITROCK_NAGIOS_ROOTDIR\@\@:g" $BR_HOME/nagios/etc/nagios.cfg

cp -rp $BASE/monitor-portal/applications/statusviewer/src/main/resources/*.properties $BR_HOME/core-config
cp -rp $BASE/monitor-portal/applications/reportserver/src/main/resources/*.properties $BR_HOME/core-config
mv $BR_HOME/core-config/*ViewerResources_??.properties $BR_HOME/core-config/resources


# Jira's fixes

# GWMON-6912
cp -rp $BR_HOME/core-config/jboss-service.xml $BR_HOME/foundation/container/config/jboss
##rm -rf $BR_HOME/foundation/container/config
##rm -rf $BR_HOME/core-config/jboss

$BUILD_DIR/d2unix.pl $BASE/enterprise/syslog-ng.conf 
$BUILD_DIR/d2unix.pl $BASE/enterprise/*.pl
cp -p $BASE/enterprise/syslog-ng.conf $BR_HOME/common/etc
cp -p $BASE/enterprise/syslog2nagios.5.3.pl $BR_HOME/common/bin/syslog2nagios.pl
chmod +x $BR_HOME/common/bin/*.pl

cd $HOME
rm -rf css-html%20deliverables
svn co http://geneva/svn/engineering/projects/GWMON-6.0/css-html%20deliverables

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
# GWMON-7102
# GWMON-7664
cp -rp $BASE/monitor-professional/profiles/plugins/data $BR_HOME/nagios/libexec
cp -rp /home/nagios/groundwork-professional/monitor-professional/profiles/plugins/lib $BR_HOME/nagios/libexec
########cp -rp $BASE/monitor-professional/profiles/plugins/lib $BR_HOME/nagios/libexec
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

# GWMON-6842
rm -f $BR_HOME/nagios/libexec/*.c

# GWMON-6503
rm -f $BR_HOME/profiles/perfconfig-vmware_esx3_services_profile.xml

# GWMON-7144
cp -p $BASE_OS/performance-core/admin/import_perfconfig.pl $BR_HOME/tools/profile_scripts
chmod +x $BR_HOME/tools/profile_scripts/*.pl

# GWMON-7428
cp -p $BASE_OS/syslib/gwservices.ent $BR_HOME/services/gwservices
chmod +x $BR_HOME/services/gwservices

# GWMON-7440
cp -p $BASE/monitor-framework/core/src/resources/portal-core-war/images/gwconnect.gif $BR_HOME/foundation/container/webapps/jboss/jboss-portal.sar/portal-core.war/images

# GWMON-7739
rm -f $BR_HOME/nagios/eventhandlers/process_service_perf.pl
rm -f $BR_HOME/nagios/eventhandlers/process_service_perf_db.pl

cp -p $BASE/monitor-professional/monarch-patch60/monarch.cgi $BR_HOME/monarch/cgi-bin/monarch
cp -p $BASE/monitor-professional/bronx/conf/bronx.cfg $BR_HOME/core-config
cp -p $BR_HOME/network-service/config/network-service.properties $BR_HOME/core-config


# Force process_foundation_db_update is on
Process_Foundation_Db_Update=$(grep process_foundation_db_update $BR_HOME/nagios/eventhandlers/process_service_perf_db_file.pl | awk '{ print $4; }' | sed 's/;//')
sed -i 's/process_foundation_db_updates = '$Process_Foundation_Db_Update'/process_foundation_db_updates = 1/' $BR_HOME/nagios/eventhandlers/process_service_perf_db_file.pl

Process_Foundation_Db_Update=$(grep process_foundation_db_update $BR_HOME/nagios/eventhandlers/process_service_perfdata_file | awk '{ print $4; }' | sed 's/;//')
sed -i 's/process_foundation_db_updates = '$Process_Foundation_Db_Update'/process_foundation_db_updates = 1/' $BR_HOME/nagios/eventhandlers/process_service_perfdata_file

sed -i 's/send_events_for_pending_to_ok = 0/send_events_for_pending_to_ok = 1/' $BR_HOME/foundation/feeder/nagios2collage_socket.pl




find $BR_HOME -name perfchart.cgi -exec chmod +x {} \;

# Cleanup and set ownership
find $BR_HOME -name .svn -exec rm -rf {} \;
chown -R 1001:nagioscmd $BR_HOME

date
echo "Monitor-pro build is done at `date`"
##########################################

echo "EntBuild.sh is done..."

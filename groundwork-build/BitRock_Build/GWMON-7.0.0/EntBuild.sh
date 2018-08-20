#!/bin/bash -x

# Copyright (c) 2009-2013 GroundWork Open Source Solutions info@groundworkopensource.com

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
  echo "Please set the build directory in the buildRPM.sh file ..."
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

GWMEE_VERSION=7.0.0

PATH=$PATH:$HOME/bin
export BR_HOME=/home/build/BitRock/groundwork
export GW_HOME=/usr/local/groundwork
export ANT_HOME=$(which ant|sed 's/\/bin\/ant//')
export MAVEN_HOME=$(which maven|sed 's/\/bin\/maven//')
export RELEASE=$distro$ARCH
export DATE=$(date +%Y-%m-%d)

export PATH=$JAVA_HOME/bin:$GW_HOME/bin:$PATH:$MAVEN_HOME/bin:$ANT_HOME/bin

HOME=/home/build
BUILD_BASE=$HOME/build7
BASE_OS=$HOME/groundwork-monitor/monitor-os

#HOME=/home/nagios
BASE=$HOME/groundwork-monitor
#BASE_OS=$BASE/monitor-os
BASE_CORE=$BASE/monitor-core
BUILD_DIR=$BASE/build

FOUNDATION_ENTERPRISE_CHECKOUT=/home/build/build7/foundation

GW_PERL_VERSION=5.8.9

NTOP_BUILD_TREE=/tmp/ntop-build
NEDI_BUILD_TREE=/tmp/nedi-build
NEDI_BUILD_BASE=$NEDI_BUILD_TREE/nedi
CACTI_BUILD_TREE=/tmp/cacti-build
CACTI_BUILD_BASE=$CACTI_BUILD_TREE/cacti
WEATHERMAP_BUILD_TREE=/tmp/weathermap-build
WEATHERMAP_BUILD_BASE=$WEATHERMAP_BUILD_TREE/cacti/htdocs/plugins

RUN_DIR=/home/build
MoratDir=/var/www/html/tools/DEVELOPMENT

# Clean up previous builds
rm -rf $GW_HOME
rm -rf $BASE
cp -rp /usr/local/groundwork-common.ent /usr/local/groundwork

# Build monitor-pro
rm -rf $HOME/groundwork-monitor
rm -rf $HOME/groundwork-professional

# Checkout function
svn_co () {
    for i in 1 0; do
	svn co $1 $2 $3 $4 $5 $6 $7
	SVN_EXIT_CODE=$?
	if [ $SVN_EXIT_CODE -eq 0 ]; then
	    break;
	elif [ $i -eq 0 ]; then
	    echo "BUILD FAILED: There has been a problem trying to checkout groundwork files." | mail -s "$GWMEE_VERSION Enterprise Build FAILED in  `hostname` - $DATE" build-info@gwos.com
	    exit 1
	fi
	sleep 30
    done
}
# Commit function
svn_commit () {
    for i in 1 0; do
        svn commit $1 $2 $3 $4 $5 $6 $7
        SVN_EXIT_CODE=$?
        if [ $SVN_EXIT_CODE -eq 0 ]; then
            break;
        elif [ $i -eq 0 ]; then
            echo "BUILD FAILED: There has been a problem trying to checkout groundwork files." | mail -s "$GWMEE_VERSION Enterprise Build FAILED in  `hostname` - $DATE" build-info@gwos.com
            exit 1
        fi
        sleep 30
    done
}

# Files from Open Source repository used for the build

echo "Check out Files from Open Source Repository"
cd $BUILD_BASE

svn_co -N http://archive.groundworkopensource.com/groundwork-opensource/trunk            groundwork-monitor
svn_co    http://archive.groundworkopensource.com/groundwork-opensource/trunk/foundation groundwork-monitor/monitor-os



# Check out from subversion
cd $HOME
svn_co -N --username build --password bgwrk http://geneva/groundwork-professional/trunk groundwork-professional
cd $HOME/groundwork-professional

rm -rf $HOME/groundwork-professional/monitor-portal

svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/build
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/enterprise
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/foundation
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/guava-packages
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/images
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/load-test-tools
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/patch-scripts
#svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/plugins
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-portal
#svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-starter

mkdir monitor-agent
mkdir monitor-agent/gdma
cd monitor-agent/gdma
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-agent/gdma/java-agent

mkdir GDMA2.1
cd GDMA2.1
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-agent/gdma/GDMA2.1/profiles
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-agent/gdma/GDMA2.1/linux/gdma/GDMA

svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-agent/gdma/GDMA2.1/linux/gdma/gdma-core

# Getting the CloudHub/Vema source code
cd $HOME/groundwork-professional/monitor-agent
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-agent/vema

cd $HOME/groundwork-professional
mkdir monitor-spool
cd monitor-spool
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-spool/scripts/services/spooler-gdma
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-spool/config

cd $HOME/groundwork-professional
svn_co -N --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional monitor-professional
cd $HOME/groundwork-professional/monitor-professional
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/apache
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/auto-registration
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/bronx
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/database
#svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/guava
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/log-reporting
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/migration
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/monarch
#svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/monarch-patch60
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/nagios
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/performance
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/performance-core
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/profiles
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/resources
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/snmp
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/sqldata
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/syslib
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/tools
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/fping
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/noma
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/perl

svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/monarch-export
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-professional/reports

# ================= THIS SECTION BELOW ON LOG ARCHIVING IS STILL UNDER DEVELOPMENT =================

# Some of this activity may be moved elsewhere in this script before we're done.

cd $HOME/groundwork-professional
mkdir monitor-archive
cd monitor-archive
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-archive/bin
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-archive/config
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-archive/scripts
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-archive/var

# FIX MAJOR:  This is a compiled binary.  We'll need to get BitRock to build it from our C source code.
# /usr/local/groundwork/core/archive/bin/control_archive_gwservices
#
# FIX MAJOR:  Other files to add here locally to the build (we will remove this comment once this is working):
# /usr/local/groundwork/config/log-archive-receive.conf
# /usr/local/groundwork/config/log-archive-send.conf
# /usr/local/groundwork/core/archive/bin/log-archive-receive.pl
# /usr/local/groundwork/core/archive/bin/log-archive-send.pl
# /usr/local/groundwork/core/archive/var/log-archive-receive.state
# /usr/local/groundwork/core/archive/var/log-archive-send.state
# /usr/local/groundwork/core/databases/postgresql/Archive_GWCollageDB_extensions.sql
# /usr/local/groundwork/core/databases/postgresql/create-fresh-archive-databases.sql
# /usr/local/groundwork/core/databases/postgresql/defragment-runtime-database.sh
# /usr/local/groundwork/core/databases/postgresql/make-archive-application-type.pl
# /usr/local/groundwork/core/databases/postgresql/postgres-xtra-functions.sql
# /usr/local/groundwork/core/databases/postgresql/set-up-archive-database.sh

# ================= THIS SECTION ABOVE ON LOG ARCHIVING IS STILL UNDER DEVELOPMENT =================

$BUILD_BASE/GWMON-$GWMEE_VERSION/prepare-pro.sh

# Get Open Source postgres databases...
echo "Checkout postgres databases from Open Source repository"

mkdir $GW_HOME/databases
cd $GW_HOME/databases
mkdir postgresql

svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/enterprise-foundation/collage/database/schema/postgresql


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
  svn_commit --username build --password bgwrk08 $HOME/groundwork-monitor/monitor-professional/project.properties -F svnmessage
fi
rm -rf svnmessage

# Update build properties
grep -v pro $BR_HOME/build.properties > $BR_HOME/build.properties.tmp
echo pro=6.7.0-$new_release >> $BR_HOME/build.properties.tmp
mv -f $BR_HOME/build.properties.tmp $BR_HOME/build.properties

# Update monitor's pro version
cat $BR_HOME/groundwork-pro.xml | sed 's/name="pro_build" value="/name="pro_build" value="6.7.0-'$new_release'/' > $BR_HOME/groundwork-pro.xml.tmp
mv -f $BR_HOME/groundwork-pro.xml.tmp $BR_HOME/groundwork-pro.xml

# Run dos2unix for all the files under profiles directory.
# GWMON-10673:  This needs a more subtle approach, which is not yet implemented.
chmod +x $BUILD_DIR/d2unix.pl

# GWMON-10673:  I have tried commenting out the following line on a temporary
# basis, to test which profile files might actually contain CR characters.  (This
# is a little difficult to see directly in Subversion, because our profile files
# come from multiple places in Subversion.)  In some cases, we might want to
# retain CRLF line termination (such as in perfconfig-*.xml files, for RRD graph
# commands, to maintain a clean multi-line structure of the commands, or possibly
# for externals which are to be exported to Windows GDMA clients).  We won't know
# the full extent of what we want and don't want until we can see the full
# range of profile files in our build that actually contain such characters.
# But that experiment failed; the BitRock packager or installer is clearly
# interfering and imposing its own dos2unix transform on all our profile files.
# So this area needs more work.
$BUILD_DIR/d2unix.pl $BASE/monitor-professional/profiles


# Start the master build script.  Note that the file-copy operations that will be invoked in this scripting
# will make a complete mess of directory and file permissions, because of Java's total blindness to this critical
# aspect of filesystems.  Unbelievable, but there it is.  So we have to clean up the mess afterward.
cd $BASE/monitor-professional
maven allBuild allDeploy

# Create the profile-categorization symlinks we want that are not directly present in Subversion.
# Then clean up afterward; there's no reason to preserve the SYMLINKS file in the final distribution.
echo "Make profile-categorization symlinks ..."
$BUILD_BASE/GWMON-$GWMEE_VERSION/makesymlinks nagios nagios $GW_HOME/profiles SYMLINKS
rm -f $GW_HOME/profiles/SYMLINKS

# Build monitor portal
cd $BASE/monitor-portal
maven clean >/dev/null
# Removed for GWM 7.0.0 build
#maven prepare
maven deploy

# Build Java Agents
echo "Build Java agents for Websphere, Jboss and Tomcat"

cd $BASE/monitor-agent/gdma/java-agent
maven build

find /usr/local/groundwork -name .svn -exec rm -rf {} \;
find /usr/local/groundwork -name .project -exec rm -rf {} \;
find /usr/local/groundwork -name maven.xml -exec rm -rf {} \;
find /usr/local/groundwork -name project.xml -exec rm -rf {} \;

#
#
# JBoss Enterprise portal was build in Common.sh and all the binaries have been deployed
# into the deployne=ment folder. Copy it into the Bitrock folder
#
#


# Set permissions
cd $BUILD_BASE/GWMON-$GWMEE_VERSION
. set-permissions-pro.sh

date
cp -rp $GW_HOME/apache2/cgi-bin/profiles $BR_HOME/profiles/cgi-bin

cp -rp $GW_HOME/bin/* $BR_HOME/common/bin

# Copy foundation.properties from Enterprise build to config folder
cp -fp $FOUNDATION_ENTERPRISE_CHECKOUT/resources/foundation.properties $GW_HOME/config/foundation.properties

cp -rp $GW_HOME/config/* $BR_HOME/core-config
cp -p $BR_HOME/core-config/db.properties $BR_HOME/core-config/db.properties.pro
cp -fp $FOUNDATION_ENTERPRISE_CHECKOUT/resources/foundation.properties $BR_HOME/core-config/foundation.properties

cp -p $HOME/groundwork-professional/monitor-archive/config/log-archive-receive.conf $BR_HOME/core-config
cp -p $HOME/groundwork-professional/monitor-archive/config/log-archive-send.conf    $BR_HOME/core-config

# These files contain some DB access credentials, so we restrict the ability to read them.
chmod 600 $BR_HOME/core-config/log-archive-receive.conf
chmod 600 $BR_HOME/core-config/log-archive-send.conf


# GWMON-9523
cp -rp $FOUNDATION_ENTERPRISE_CHECKOUT/resources/ws_client.properties $BR_HOME/core-config

cp -rp $GW_HOME/databases/* $BR_HOME/databases
cp -rp $BASE/monitor-professional/database/create-monitor-professional-db.sql $BR_HOME/databases

#Postgres databases
echo "Get all database scripts for postgresql ..."
cp -rp $BASE/monitor-professional/database/postgresql/*.sql $BR_HOME/databases/postgresql
cp -rp $GW_HOME/databases/postgresql/*.sql                  $BR_HOME/databases/postgresql

# FIX MINOR:  We might use shell globs in these commands, to simplify the copying, before we're done here.
cp -p $HOME/groundwork-professional/monitor-archive/scripts/Archive_GWCollageDB_extensions.sql $BR_HOME/databases/postgresql
cp -p $HOME/groundwork-professional/monitor-archive/scripts/create-fresh-archive-databases.sql $BR_HOME/databases/postgresql
cp -p $HOME/groundwork-professional/monitor-archive/scripts/defragment-runtime-database.sh     $BR_HOME/databases/postgresql
cp -p $HOME/groundwork-professional/monitor-archive/scripts/make-archive-application-type.pl   $BR_HOME/databases/postgresql
# FIX MAJOR:  We delay installing our updated copy of postgres-xtra-functions.sql until we are sure that it is truly up-to-date.
# cp -p $HOME/groundwork-professional/monitor-archive/scripts/postgres-xtra-functions.sql        $BR_HOME/databases/postgresql
cp -p $HOME/groundwork-professional/monitor-archive/scripts/set-up-archive-database.sh         $BR_HOME/databases/postgresql

# We purposely restrict the permissions on a few key scripts.
chmod 750 $BR_HOME/databases/postgresql/defragment-runtime-database.sh
chmod 750 $BR_HOME/databases/postgresql/make-archive-application-type.pl
chmod 750 $BR_HOME/databases/postgresql/set-up-archive-database.sh

#Update the default PostreSQL configuration
echo "Update PostgreSQL default with GroundWork settings"
cp -rp $BASE/monitor-professional/database/postgresql/postgresql.conf.default  $BR_HOME/postgresql/share/postgresql.conf.sample

cp -rp $GW_HOME/foundation/feeder/* $BR_HOME/foundation/feeder
cp -p $GW_HOME/log-reporting/lib/LogFile.pm $BR_HOME/foundation/feeder
cp -rp $GW_HOME/foundation/scripts $BR_HOME/foundation

cp -rp $GW_HOME/gwreports/* $BR_HOME/gwreports

echo "Remove obsolete reports"
rm -rf $BR_HOME/gwreports/LogReports

echo "Folder layout for reports ..."
cp -fp $HOME/groundwork-professional/monitor-portal/applications/reportserver/src/main/resources/report_en.xml $BR_HOME/gwreports
cp -fp $HOME/groundwork-professional/monitor-portal/applications/reportserver/src/main/resources/report_fr.xml $BR_HOME/gwreports

cp -rp $GW_HOME/log-reporting/* $BR_HOME/log-reporting

cp -p $GW_HOME/apache2/cgi-bin/snmp/mibtool/index.cgi $BR_HOME/snmp/cgi-bin/snmp/mibtool
cp -rp $GW_HOME/apache2/htdocs/snmp/mibtool/* $BR_HOME/snmp/htdocs/snmp/mibtool

cp -rp $GW_HOME/migration/* $BR_HOME/migration
echo "Cleanup obsolete scripts..."
rm -f $BR_HOME/migration/devclean.pl

#Copy JBoss admin script into migration folder
echo "include JBoss admin role script"
cp -rp $HOME/groundwork-professional/monitor-professional/migration/migrate_admin* $BR_HOME/migration

#Copy JBoss 6.7 data migration script into migration folder
echo "JBoss migration for GWME 6.x data to GWME 7"
cp -rp $HOME/groundwork-professional/monitor-professional/migration/jpp6x/* $BR_HOME/migration

#Copy gwcollagedb migration script for gdma plugins fix (GWMON-10084)
cp -rp $HOME/groundwork-professional/monitor-professional/migration/upgrade_gwcollage_* $BR_HOME/migration

# GWMON-10320 -- Include migration validation scripts
echo "Copy MySQL validation scripts into installer folder"
cp -p $HOME/groundwork-professional/monitor-professional/migration/delete_* $BR_HOME/migration
cp -p $HOME/groundwork-professional/monitor-professional/migration/find_* $BR_HOME/migration

echo "Create postgresql folder for migration scripts in the Bitrock installer folder"
mkdir $BR_HOME/migration/postgresql

echo "Copy all postgreSQL migration scripts into installer"
cp -rp $HOME/groundwork-professional/monitor-professional/migration/postgresql/* $BR_HOME/migration/postgresql/
mv $BR_HOME/migration/postgresql/patch-gwcollagedb.sql $BR_HOME/migration/

# GWMON-10327
mv -f $BR_HOME/migration/postgresql/validate_gw_db.pl $BR_HOME/migration/

cp -rp $GW_HOME/monarch/automation/conf/* $BR_HOME/monarch/automation/conf

cp -rp $BASE_CORE/nagios/etc/nagios.cfg $BR_HOME/nagios/etc
cp -rp $GW_HOME/nagios/etc/nagios.initd.pro $BR_HOME/nagios/etc
cp -rp $GW_HOME/nagios/eventhandlers/* $BR_HOME/nagios/eventhandlers

# GWMON-10578:  We use an explicit option to force no symlink dereferencing,
# instead of relying on the GNU cp default of not following symlinks simply
# because we're doing a recursive copy.  See "info cp" for details.
cp -rpP $GW_HOME/profiles/* $BR_HOME/profiles

cp -rp $GW_HOME/sbin/* $BR_HOME/common/sbin

cp -rp $GW_HOME/tools $BR_HOME

#cp -p $BASE/monitor-framework/core/src/resources/portal-core-war/images/gwconnect.gif $BR_HOME/foundation/container/webapps/jboss/jboss-portal.sar/portal-core.war/images

#cp -rp $GW_HOME/foundation/container/webapps/jboss/jboss-portal.sar/*.war $BR_HOME/foundation/container/webapps/jboss/jboss-portal.sar
#cp -rp $GW_HOME/foundation/container/webapps/jboss/jboss-portal.sar/lib/*.jar $BR_HOME/foundation/container/webapps/jboss/jboss-portal.sar/lib
#cp -rp $BR_HOME/core-config/jboss-service.xml $BR_HOME/foundation/container/config/jboss

#JIRA GWMON-8889
#echo "Copy monarch webextension into the installer environment..."
#cp -p $GW_HOME/foundation/container/webapps/monarch.war $BR_HOME/foundation/container/webapps

#JIRA GWMON-8945 Include packages in the build

echo "Update tomcat configs...."
cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/context.xml $BR_HOME/foundation/container/webapps/jboss/jboss-web.deployer

cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/web.xml $BR_HOME/foundation/container/webapps/jboss/jboss-web.deployer/ROOT.war/WEB-INF/

cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/jboss-web.xml $BR_HOME/foundation/container/webapps/jboss/jboss-web.deployer/ROOT.war/WEB-INF/


cd /tmp
rm -rf current-nagvis-fs*
wget http://morat/webextension-source/nagvis/current-nagvis-fs

#JIRA GWMON-9344
echo "Cleanup of any existing nagvis files before expanding the latest package"
rm -rf $BR_HOME/nagvis

tar xfz current-nagvis-fs -C $BR_HOME/

#Fix for GWMON-9365
mkdir $BR_HOME/nagvis/migration
cp $HOME/groundwork-professional/monitor-portal/applications/nagvis/migration/*pl $BR_HOME/nagvis/migration

cd /tmp
rm -f nagvis-default-maps.tar*
echo "Get the defaults maps which are a  copy of Groundwork Live"
wget http://morat/webextension-source/nagvis/nagvis-default-maps.tar.gz

echo "Replace default maps"
rm -f $BR_HOME/nagvis/etc/maps/*.cfg
tar xfz nagvis-default-maps.tar.gz -C $BR_HOME/

echo "Media files for Audible Alarms in Event Console"
cp -f $HOME/groundwork-professional/monitor-portal/applications/console/WebContent/resources/*.mp3 $BR_HOME/core-config/media

echo "Copy modified httpd.conf for enterprise version"
cp -rp $HOME/groundwork-professional/monitor-professional/apache/httpd.conf $BR_HOME/apache2/conf
#cp -rp $HOME/groundwork-professional/monitor-professional/apache/server.xml $BR_HOME/foundation/container/webapps/jboss/jboss-web.deployer

#echo "Update default JBoss web.xml with GroundWork specific one that includes the settings for the default admin user"
#cp -fp $HOME/groundwork-professional/monitor-professional/apache/web.xml $BR_HOME/foundation/container/webapps/jboss/jboss-web.deployer/conf

#Network service portlet no longer in standalone package
#cp -p $GW_HOME/foundation/container/webapps/jboss/jboss-portal.sar/network-service-portlet.war $BR_HOME/foundation/container/webapps/jboss/jboss-portal.sar/network-service-portlet.war
#cp -p $GW_HOME/foundation/container/webapps/jboss/jboss-portal.sar/network-service-portlet.war $BR_HOME/network-service/libs/java

# Ntop configuration
cp -p $NTOP_BUILD_TREE/ntop.properties $BR_HOME/core-config

# Noma configuration
echo "Copy NoMa control scripts and configuration files"
cp -p $HOME/groundwork-professional/monitor-professional/noma/scripts/ctl.sh            $BR_HOME/noma/scripts
cp -p $HOME/groundwork-professional/monitor-professional/noma/noma_2.0.3/gwos/NoMa.yaml $BR_HOME/noma/etc
cp -p $HOME/groundwork-professional/monitor-professional/apache/apache2-noma.conf       $BR_HOME/apache2/conf/groundwork/apache2-noma.conf
cp -p $HOME/groundwork-professional/monitor-professional/apache/php.ini                 $BR_HOME/apache2/conf/groundwork/php.ini

# Foundation UI protection
echo "Copy Foundation apache configuration files"
cp -p $HOME/groundwork-professional/monitor-professional/apache/foundation-ui.conf $BR_HOME/apache2/conf/groundwork/foundation-ui.conf

# Auto-Registration
echo "Copy auto-registration scripts, modules, and supporting config files into build"
cp -p $HOME/groundwork-professional/monitor-professional/auto-registration/server/*.{pl,pm} $BR_HOME/foundation/scripts
cp -p $HOME/groundwork-professional/monitor-professional/auto-registration/server/register_agent.properties $BR_HOME/core-config

# GroundWork Perl modules
echo "Copy Custom GroundWork Perl modules into distribution"
mkdir $BR_HOME/perl/lib/site_perl/$GW_PERL_VERSION/GW
cp -p $HOME/groundwork-professional/monitor-professional/perl/GW/*.pm $BR_HOME/perl/lib/site_perl/$GW_PERL_VERSION/GW

#
# Nedi configuration
#
if true; then
    # New construction for the NeDi 1.0.7 (or later) release, created directly from the
    # standard public NeDi distribution with explicit applied patches for porting to
    # PostgreSQL and newly added files to support the GroundWork context.  This part
    # should not need to be changed to support subsequent NeDi releases; only the upstream
    # scripting that populates the $NEDI_BUILD_BASE file tree will need adjustment.
    rm -rf $BR_HOME/nedi
    cp -pr $NEDI_BUILD_BASE $BR_HOME/
    cp -p $NEDI_BUILD_TREE/nedi.properties $BR_HOME/core-config/nedi.properties
    cp -p $NEDI_BUILD_TREE/nedi_httpd.conf $BR_HOME/apache2/conf/groundwork/nedi_httpd.conf
else
    # Legacy construction, no longer needed now that we will fold NeDi into the
    # base product with an entirely different manner of constructing the release.
    echo "Nedi post build updates"
    tar xfz $HOME/groundwork-professional/monitor-portal/applications/nms/conf/patches/nedi_patched.tgz -C $BR_HOME/
    chown nagios.nagios $BR_HOME/nedi/*.pl
    chown nagios.nagios $BR_HOME/nedi/*.conf

    chmod 755 $BR_HOME/nedi/*.pl
fi

#
# Cacti configuration
#
echo "Cacti post build updates"

if true; then
    # New construction for Cacti dynamically patched starting from the standard public distribution.
    # This is handled by upstream scripting with explicit applied patches for porting to PostgreSQL
    # and to add many of the newly added files to support the GroundWork context.
    rm -rf $BR_HOME/cacti
    cp -pr $CACTI_BUILD_BASE $BR_HOME/

    echo "Copy default splash screens for plugins"
    # FIX MAJOR:  The nedi splash screen will be dropped once we have BitRock establish the necessary replacement symlink, the equivalent of:
    #     rm -f                             /usr/local/groundwork/foundation/container/webapps/nedi.war
    #     ln -s /usr/local/groundwork/nedi/ /usr/local/groundwork/foundation/container/webapps/nedi.war
    # and presumably the other splash screens can be dropped as well when those components are in the build as well
    # cp -f $HOME/groundwork-professional/monitor-portal/applications/nms/src/main/webapp/plugins/nedi/* $BR_HOME/cacti/htdocs/splash/nedi
    cp -f $HOME/groundwork-professional/monitor-portal/applications/nms/src/main/webapp/plugins/ntop/* $BR_HOME/cacti/htdocs/splash/ntop
    cp -f $HOME/groundwork-professional/monitor-portal/applications/nms/src/main/webapp/plugins/weathermap/* $BR_HOME/cacti/htdocs/splash/weathermap

    # FIX MINOR:  this shouldn't be necessary once we get rid of the splash files,
    # because all ownership setting is already handled when this file tree is built
    chown -R nagios:nagios $BR_HOME/cacti

    # FIX MINOR:  remove this, as it appears to no longer be necessary
    # cacti includes ntop plugins directory by default that includes a set of dummy files
    # GWMON-9250
    # rm -rf $BR_HOME/cacti/htdocs/plugins/ntop

    cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/cacti_updates/cacti_cron.sh $BR_HOME/common/bin/cacti_cron.sh
    chmod +x $BR_HOME/common/bin/cacti_cron.sh

    cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/cacti_updates/find_cacti_graphs $BR_HOME/foundation/feeder/find_cacti_graphs
    chmod 755 $BR_HOME/foundation/feeder/find_cacti_graphs

    # Presumably, the check_cacti.conf file is moved elsewhere by the BitRock installer.
    cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/cacti_updates/check_cacti.conf $BR_HOME/cacti
    cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/cacti_updates/check_cacti.pl $BR_HOME/nagios/libexec
    chmod +x $BR_HOME/nagios/libexec/check_cacti.pl

    # Presumably, the cacti.properties file is moved elsewhere by the BitRock installer.
    cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/cacti_updates/cacti.properties $BR_HOME/cacti

    cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/cacti_updates/*.xml $BR_HOME/monarch/automation/templates

    cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/cacti_updates/php.ini $BR_HOME/php/etc/

    echo "Spine configuration"
    cp $BR_HOME/common/etc/spine.conf.dist $BR_HOME/common/etc/spine.conf

    # FIX MINOR:  remove this, as it appears to no longer be necessary -- and if reject files appear in the build again,
    # we ought to be discovering why and fix that upstream!
    # GWMON-9578 Cleanup of files
    # find $BR_HOME/cacti/htdocs -name *.rej -exec rm -f  '{}' \;

    echo "Include Weathermap"
    cp -pr $WEATHERMAP_BUILD_BASE $BR_HOME/cacti/htdocs
    cp -p $WEATHERMAP_BUILD_TREE/weathermap.properties $BR_HOME/core-config
else
    # Legacy construction, no longer needed now that we will fold the construction of Cacti from
    # first principles (the standard public distribution plus patches) into the base product.
    tar xfz $HOME/groundwork-professional/monitor-portal/applications/nms/conf/patches/cacti_patched.tgz -C $BR_HOME/

    echo "Copy default splash screens for plugins"
    cp -f $HOME/groundwork-professional/monitor-portal/applications/nms/src/main/webapp/plugins/nedi/* $BR_HOME/cacti/htdocs/splash/nedi
    cp -f $HOME/groundwork-professional/monitor-portal/applications/nms/src/main/webapp/plugins/ntop/* $BR_HOME/cacti/htdocs/splash/ntop
    cp -f $HOME/groundwork-professional/monitor-portal/applications/nms/src/main/webapp/plugins/weathermap/* $BR_HOME/cacti/htdocs/splash/weathermap

    chown -R nagios:nagios $BR_HOME/cacti

    # cacti includes ntop plugins directory by default that includes a set of dummy files
    # GWMON-9250

    rm -rf $BR_HOME/cacti/htdocs/plugins/ntop

    echo "Default database for cacti and migration scripts ..."
    cp -f $HOME/groundwork-professional/monitor-portal/applications/nms/conf/database/*.sql   $BR_HOME/cacti/scripts

    cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/cacti_updates/cacti_cron.sh $BR_HOME/common/bin/cacti_cron.sh
    chmod +x $BR_HOME/common/bin/cacti_cron.sh

    cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/cacti_updates/find_cacti_graphs $BR_HOME/foundation/feeder/find_cacti_graphs
    chmod 755 $BR_HOME/foundation/feeder/find_cacti_graphs

    cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/cacti_updates/check_cacti.conf $BR_HOME/cacti
    cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/cacti_updates/check_cacti.pl $BR_HOME/nagios/libexec
    chmod +x $BR_HOME/nagios/libexec/check_cacti.pl

    cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/cacti_updates/cacti.properties $BR_HOME/cacti

    cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/cacti_updates/*.xml $BR_HOME/monarch/automation/templates

    cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/cacti_updates/php.ini $BR_HOME/php/etc/

    echo "Spine configuration"
    cp $BR_HOME/common/etc/spine.conf.dist $BR_HOME/common/etc/spine.conf

    # GWMON-9578 Cleanup of files
    find $BR_HOME/cacti/htdocs -name *.rej -exec rm -f  '{}' \;
fi

# Create a plugin_downlaod directory
mkdir $BR_HOME/apache2/htdocs/agents/plugin_download

# Include Java Agents in deployment
mkdir $BR_HOME/apache2/htdocs/java-agents

echo "Copy Java agents and property files to Bitrock package"
cp -p $BASE/monitor-agent/gdma/java-agent/appserver/jboss/target/gwos-jboss-monitoringAgent.war  $BR_HOME/apache2/htdocs/java-agents
cp -p $BASE/monitor-agent/gdma/java-agent/appserver/websphere/target/gwos-was-monitoringAgent.war  $BR_HOME/apache2/htdocs/java-agents
cp -p $BASE/monitor-agent/gdma/java-agent/appserver/tomcat/target/gwos-tomcat-monitoringAgent.war  $BR_HOME/apache2/htdocs/java-agents
cp -p $BASE/monitor-agent/gdma/java-agent/appserver/weblogic/target/gwos-wls-monitoringAgent.war  $BR_HOME/apache2/htdocs/java-agents

# Properties files are no longer needed. All configuration is done via xml files
#cp -p $BASE/monitor-agent/gdma/java-agent/appserver/jboss/resources/gwos_jboss.properties  $BR_HOME/apache2/htdocs/java-agents
#cp -p $BASE/monitor-agent/gdma/java-agent/appserver/websphere/resources/gwos_websphere.properties  $BR_HOME/apache2/htdocs/java-agents
#cp -p $BASE/monitor-agent/gdma/java-agent/appserver/tomcat/resources/gwos_tomcat.properties  $BR_HOME/apache2/htdocs/java-agents
#cp -p $BASE/monitor-agent/gdma/java-agent/appserver/weblogic/resources/gwos_weblogic.properties  $BR_HOME/apache2/htdocs/java-agents

echo "Copy gdma profiles"
cp -rp $BASE/monitor-agent/gdma/GDMA2.1/profiles/*.pl $BR_HOME/gdmadist/automationscripts
echo "Copy GDMA library"
cp -rp $BASE/monitor-agent/gdma/GDMA2.1/GDMA $BR_HOME/perl/lib/site_perl/$GW_PERL_VERSION
echo "Copy config file for the extended Foundation API"
cp -p $FOUNDATION_ENTERPRISE_CHECKOUT/resources/gdma_plugin_update.dtd $BR_HOME/core-config

# Include Webmetrics landing page for the lead generation
mkdir $BR_HOME/apache2/htdocs/webmetrics
echo "Adding Webmetrics landing page to package"

cp -rp $HOME/groundwork-professional/monitor-professional/apache/htdocs/webmetrics/* $BR_HOME/apache2/htdocs/webmetrics

# Make sure that user is forwarded to portal login
cp -f $HOME/groundwork-professional/monitor-professional/apache/htdocs/index.html $BR_HOME/apache2/htdocs

mkdir $BR_HOME/foundation/jboss/native
mkdir $BR_HOME/foundation/jboss/native/lib
mkdir $BR_HOME/foundation/jboss/native/bin

cp $BR_HOME/php-java-bridge/libphp5*.so  $BR_HOME/foundation/jboss/native/lib
cp $BR_HOME/php-java-bridge/libphp5*.so  $BR_HOME/foundation/jboss/native/bin

# Update revision number on morat
# FIX MAJOR:  This is not working, because the project.properties file does not exist at that location.
svn info /home/nagios/groundwork-monitor/monitor-professional/project.properties | grep Revision: | awk '{ print $2; }' > $RUN_DIR/logs/EE_Revision

/usr/bin/perl -p -i -e "s:/usr/local/groundwork/nagios:\@\@BITROCK_NAGIOS_ROOTDIR\@\@:g" $BR_HOME/nagios/etc/nagios.cfg

cp -rp $BASE/monitor-portal/applications/statusviewer/src/main/resources/*.properties $BR_HOME/core-config
cp -rp $BASE/monitor-portal/applications/reportserver/src/main/resources/*.properties $BR_HOME/core-config
mv $BR_HOME/core-config/*ViewerResources_??.properties $BR_HOME/core-config/resources

mkdir $HOME/monitor-framework
mkdir $HOME/monitor-framework/josso
cd $HOME/monitor-framework/josso
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-framework/josso/src/resources

cp -rp $HOME/monitor-framework/josso/resources/josso-auth.properties          $BR_HOME/core-config/resources
cp -rp $HOME/monitor-framework/josso/resources/josso-gateway-selfservices.xml $BR_HOME/core-config/resources
cp -rp $HOME/monitor-framework/josso/resources/josso-gateway-web.xml          $BR_HOME/core-config/resources
cp -rp $HOME/monitor-framework/josso/resources/josso-gateway-db-stores.xml    $BR_HOME/core-config/resources
cp -rp $HOME/monitor-framework/josso/resources/josso-gateway-jmx.xml          $BR_HOME/core-config/resources
cp -rp $HOME/monitor-framework/josso/resources/josso-gateway-config.xml       $BR_HOME/core-config/resources
cp -rp $HOME/monitor-framework/josso/resources/josso-gateway-auth.xml         $BR_HOME/core-config/resources
cp -rp $HOME/monitor-framework/josso/resources/josso-agent-config.xml         $BR_HOME/core-config/resources
cp -rp $HOME/monitor-framework/josso/resources/josso-gateway-ldap-stores.xml  $BR_HOME/core-config/resources

# Nagios JOSSO integration needs several file in the WEB-INF directory
cp -rp $HOME/groundwork-professional/monitor-portal/applications/nagios/conf/MANIFEST.MF $BR_HOME/nagios/META-INF
cp -rp $HOME/groundwork-professional/monitor-portal/applications/nagios/conf/context.xml $BR_HOME/nagios/WEB-INF
cp -rp $HOME/groundwork-professional/monitor-portal/applications/nagios/conf/web.xml $BR_HOME/nagios/WEB-INF
cp -rp $HOME/groundwork-professional/monitor-portal/applications/nagios/conf/jboss-web.xml $BR_HOME/nagios/WEB-INF
#cp -rp $HOME/groundwork-professional/monitor-portal/applications/nagios/conf/login-redirect.jsp $BR_HOME/nagios
cp -rp $HOME/groundwork-professional/monitor-portal/applications/nagios/conf/jboss-deployment-structure.xml $BR_HOME/nagios/WEB-INF

# Vema file structure
echo "Cloud Hub directories and monitoring profiles"
mkdir $BR_HOME/vema
mkdir $BR_HOME/vema/profiles
mkdir $BR_HOME/vema/var
mkdir $BR_HOME/chrhev
mkdir $BR_HOME/chrhev/var
cp $HOME/groundwork-professional/monitor-agent/vema/common/conf/*profile.xml $BR_HOME/vema/profiles
cp $HOME/groundwork-professional/monitor-agent/vema/common/conf/vmware_monitoring_profile.xml $BR_HOME/core-config/vmware-monitoring-profile.xml
cp $HOME/groundwork-professional/monitor-agent/vema/common/conf/rhev_monitoring_profile.xml $BR_HOME/core-config/rhev-monitoring-profile.xml

# Log Archiving file structure, in the form where the installer will pick it up
# (this tree will end up as /usr/local/groundwork/core/archive/...).
echo "Log Archiving directories, scripts, and initial state files"
mkdir $BR_HOME/archive
mkdir $BR_HOME/archive/bin
mkdir $BR_HOME/archive/log-archive
mkdir $BR_HOME/archive/var

cp -p $HOME/groundwork-professional/monitor-archive/bin/log-archive-receive.pl $BR_HOME/archive/bin
cp -p $HOME/groundwork-professional/monitor-archive/bin/log-archive-send.pl    $BR_HOME/archive/bin

# Restrict the ability to run the archiving scripts, because you don't want
# anyone who has no access to the state files to run these scripts.
chmod 750 $BR_HOME/archive/bin/log-archive-receive.pl
chmod 750 $BR_HOME/archive/bin/log-archive-send.pl

cp -p $HOME/groundwork-professional/monitor-archive/var/log-archive-receive.state $BR_HOME/archive/var
cp -p $HOME/groundwork-professional/monitor-archive/var/log-archive-send.state    $BR_HOME/archive/var

# These files contain critical long-term state info that we don't want to risk being damaged
# by inadvertent editing, so we restrict the permissions.
chmod 600 $BR_HOME/archive/var/log-archive-receive.state
chmod 600 $BR_HOME/archive/var/log-archive-send.state


# Jira's fixes

# GWMON-6912
#cp -rp $BR_HOME/core-config/jboss-service.xml $BR_HOME/foundation/container/config/jboss
##rm -rf $BR_HOME/foundation/container/config
##rm -rf $BR_HOME/core-config/jboss

$BUILD_DIR/d2unix.pl $BASE/enterprise/syslog-ng.conf
$BUILD_DIR/d2unix.pl $BASE/enterprise/*.pl
cp -p $BASE/enterprise/syslog-ng.conf $BR_HOME/common/etc
cp -p $BASE/enterprise/syslog2nagios.pl $BR_HOME/common/bin/syslog2nagios.pl
chmod +x $BR_HOME/common/bin/*.pl

# GWMON-10920
cp -p $BASE/enterprise/syslog2nagios.conf $BR_HOME/core-config

# Support script added to distribution
cp -p $BASE/monitor-professional/tools/gwdiags.pl $BR_HOME/common/bin
chmod +x $BR_HOME/common/bin/gwdiags.pl
chown nagios:nagios $BR_HOME/common/bin/gwdiags.pl

cd $HOME
rm -rf css-html%20deliverables
svn_co http://geneva/svn/engineering/projects/GWMON-6.0/css-html%20deliverables

# GWMON-5830
cp -rp $BASE_OS/performance/perfchart.cgi $BR_HOME/performance/cgi-bin/performance

# GWMON-9589
#cp -rp $BASE_OS/performance/perfchart.cgi $BR_HOME/performance/htdocs/performance
rm -f $BR_HOME/performance/htdocs/performance/PerfChartsForms.pm

# The copies of dtree.css, dtree.js, and performance.css in play before the
# following file-copy actions take effect come from some very old cached version
# that BitRock includes in their build.  We must overlay those files with current
# checked-out copies from our Subversion repository, to bring them up-to-date.
cp -rp $BASE_OS/performance/dtree.css       $BR_HOME/performance/htdocs/performance
cp -rp $BASE_OS/performance/dtree.js        $BR_HOME/performance/htdocs/performance
cp -rp $BASE_OS/performance/performance.css $BR_HOME/performance/htdocs/performance

# GWMON-5834
mkdir $GW_HOME/apache2/conf/groundwork

# GWMON-5841
# GWMON-11112
cp -rp $BASE/monitor-professional/profiles/plugins/*.pl $BR_HOME/nagios/libexec
cp -rp $BASE/monitor-professional/profiles/plugins/*.pm $BR_HOME/nagios/libexec
cp -rp $BASE/monitor-professional/profiles/plugins/*.sh $BR_HOME/nagios/libexec
cp -rp $BASE/monitor-professional/profiles/plugins/com $BR_HOME/nagios/libexec
cp -rp $BASE/monitor-professional/profiles/plugins/nagtomcat.jar $BR_HOME/nagios/libexec

# GWMON-6029
# GWMON-7102
# GWMON-7664
cp -rp $BASE/monitor-professional/profiles/plugins/data $BR_HOME/nagios/libexec
cp -rp /home/nagios/groundwork-professional/monitor-professional/profiles/plugins/lib $BR_HOME/nagios/libexec
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
cp -rp FOUNDATION_ENTERPRISE_CHECKOUT/misc/web-application/console/WebContent/assets/icons/services.gif $BR_HOME/performance/htdocs/performance/images/logos

# GWMON-5369
rm -rf $BR_HOME/reports/utils/utils

# GWMON-7849
mkdir /tmp/reports
svn_co http://archive.groundworkopensource.com/groundwork-opensource/trunk/reports/utils /tmp/reports
mv /tmp/reports/dashboard_avail_load.pl $BR_HOME/reports/utils
chmod 755 $BR_HOME/reports/utils/dashboard_avail_load.pl
chown nagios:nagios $BR_HOME/reports/utils/dashboard_avail_load.pl
rm -rf /tmp/reports

# GWMON-5976
chmod +x $BASE/monitor-professional/monarch/bin/*
cp -rp $BASE/monitor-professional/monarch/lib/* $BR_HOME/monarch/lib
cp -rp $BASE/monitor-professional/monarch/bin/* $BR_HOME/monarch/bin
######chmod +x $BR_HOME/monarch/bin/nagios-foundation-sync.pl

# GWMON-5905
cp -rp $BASE/monitor-professional/migration/migrate-monarch.sql $BR_HOME/migration

# GWMON-5985
rm -rf $BR_HOME/foundation/feeder/nagios2master.pl

# GWMON-2600
cp -rp $BASE/monitor-professional/nagios/etc/nagios.cfg $BR_HOME/nagios/etc

# GWMON-5984
chmod 755 $BR_HOME/profiles
chmod 644 $BR_HOME/profiles/README
chmod 755 $BR_HOME/profiles/*/
chmod 644 $BR_HOME/profiles/*/README
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

# NEW JPP startup script
cp -p $BASE_OS/syslib/foundation-webapp $BR_HOME/services/foundation/run
chmod +x $BR_HOME/services/foundation/run


# GWMON-7440
#cp -p $BASE/monitor-framework/core/src/resources/portal-core-war/images/gwconnect.gif $BR_HOME/foundation/container/webapps/jboss/jboss-portal.sar/portal-core.war/images

# GWMON-7739
rm -f $BR_HOME/nagios/eventhandlers/process_service_perf.pl
rm -f $BR_HOME/nagios/eventhandlers/process_service_perf_db.pl

# GWMON-8544
cp -p $BASE_OS/performance-core/eventhandler/perfdata.properties.ee $BR_HOME/core-config/perfdata.properties

# GWMON-6937
rm -f $BR_HOME/core-config/migration.properties

# GWMON-6060
cp -p $BASE/monitor-portal/applications/console/src/java/console-admin-config.xml $BR_HOME/core-config

cp -p $BASE/monitor-professional/bronx/conf/bronx.cfg $BR_HOME/core-config
cp -p $BR_HOME/network-service/config/network-service.properties $BR_HOME/core-config

# GWMON-7771
cp -p $BASE/monitor-portal/applications/console/src/java/console.properties $BR_HOME/core-config

# GWMON-7803
cp -p $FOUNDATION_ENTERPRISE_CHECKOUT/misc/web-application/reportserver/reports/StatusReports/* $BR_HOME/gwreports/StatusReports

# Force process_foundation_db_update is on
Process_Foundation_Db_Update=$(grep process_foundation_db_update $BR_HOME/nagios/eventhandlers/process_service_perf_db_file.pl | awk '{ print $4; }' | sed 's/;//')
sed -i 's/process_foundation_db_updates = '$Process_Foundation_Db_Update'/process_foundation_db_updates = 1/' $BR_HOME/nagios/eventhandlers/process_service_perf_db_file.pl

Process_Foundation_Db_Update=$(grep process_foundation_db_update $BR_HOME/nagios/eventhandlers/process_service_perfdata_file | awk '{ print $4; }' | sed 's/;//')
sed -i 's/process_foundation_db_updates = '$Process_Foundation_Db_Update'/process_foundation_db_updates = 1/' $BR_HOME/nagios/eventhandlers/process_service_perfdata_file

sed -i 's/send_events_for_pending_to_ok = 0/send_events_for_pending_to_ok = 1/' $BR_HOME/foundation/feeder/nagios2collage_socket.pl

# GWMON-9398
cd $BR_HOME/core-config
rm -f status-feeder.properties
wget http://archive.groundworkopensource.com/groundwork-opensource/trunk/foundation/collagefeeder/scripts/status-feeder.properties
chown nagios:nagios status-feeder.properties

# Download postgres database driver
echo "Download postgres JDBC driver into installer package"

#cd $BR_HOME/foundation/container/lib/jboss
#wget http://archive.groundworkopensource.com/maven/postgresql/jars/postgresql-9.1-901.jdbc3.jar
#chown nagios:nagios postgresql-9.1-901.jdbc3.jar


# GWMON-9386 -- fping feeder script update
echo "Make sure latest fping feeder script is included...."
cp -fp $HOME/groundwork-professional/monitor-professional/fping/usr/local/groundwork/foundation/feeder/fping_process.pl $BR_HOME/fping-feeder/usr/local/groundwork/foundation/feeder/
cp -fp $HOME/groundwork-professional/monitor-professional/fping/usr/local/groundwork/foundation/feeder/fping_process.pl $BR_HOME/fping-feeder/usr/local/groundwork/nagios/libexec/
cp -fp $HOME/groundwork-professional/monitor-professional/fping/usr/local/groundwork/config/fping_process.conf $BR_HOME/fping-feeder/usr/local/groundwork/config/fping_process.conf
chmod 600 $BR_HOME/fping-feeder/usr/local/groundwork/config/fping_process.conf

# Spooler-gdma
cp -rp $HOME/groundwork-professional/monitor-agent/gdma/GDMA2.1/gdma-core/bin/gdma_spool_processor.pl $BR_HOME/groundwork-spooler/gdma/bin

cp -rp $HOME/groundwork-professional/monitor-spool/config/gdma_auto.conf $BR_HOME/groundwork-spooler/gdma/config
cp -rp $HOME/groundwork-professional/monitor-spool/config/gwmon_localhost.cfg $BR_HOME/groundwork-spooler/gdma/config
rm -rf  $BR_HOME/groundwork-spooler/spooler-gdma
cp -rp $HOME/groundwork-professional/monitor-spool/spooler-gdma $BR_HOME/groundwork-spooler


# Setting up the right ownership
find $BR_HOME -name perfchart.cgi -exec chmod +x {} \;

# Clean up and set ownership
find $BR_HOME -name .svn -exec rm -rf {} \;
chown -R nagios:nagios $BR_HOME

echo "Remove JBoss Community directory from the installer"
rm -rf  $BR_HOME/foundation/container/contexts
rm -rf  $BR_HOME/foundation/container/data
rm -rf  $BR_HOME/foundation/container/etc
rm -rf  $BR_HOME/foundation/container/lib
rm -rf  $BR_HOME/foundation/container/webapps
rm -f  $BR_HOME/foundation/container/*.*
mkdir $BR_HOME/foundation/container/logs
touch $BR_HOME/foundation/container/logs/README

######################################################
# Portal framework deployment ..
######################################################
echo "Copy JPP into the Bitrock Install Builder"
cp -r /usr/local/groundwork/jpp $BR_HOME/foundation/container
chmod 750 $BR_HOME/foundation/container/jpp/bin/standalone.sh
chmod 750 $BR_HOME/foundation/container/jpp/bin/jboss-cli.sh

######################################################
# favicon for login page
######################################################
cp -p $HOME/groundwork-professional/monitor-professional/favicon.ico $BR_HOME/apache2/htdocs

####################################################
# Josso server deployment
####################################################
echo "Copy tomcat with JOSSO into the Bitrock Install Builder"
cp -r /usr/local/groundwork/josso-1.8.4 $BR_HOME/foundation/container
chmod +x $BR_HOME/foundation/container/josso-1.8.4/bin/startup.sh

#######################################################################
# Josso security for performance, monarch-export, reports and profiles
#######################################################################

echo "Copy JOSSO configurations for perl scripts ..."
#cp -p $HOME/groundwork-professional/monitor-professional/performance/login-redirect.jsp $BR_HOME/performance
mkdir $BR_HOME/performance/WEB-INF
cp -p $HOME/groundwork-professional/monitor-professional/performance/WEB-INF/*.xml $BR_HOME/performance/WEB-INF

#cp -p $HOME/groundwork-professional/monitor-professional/profiles/login-redirect.jsp $BR_HOME/profiles
mkdir $BR_HOME/profiles/WEB-INF
cp -p $HOME/groundwork-professional/monitor-professional/profiles/WEB-INF/*.xml $BR_HOME/profiles/WEB-INF

#cp -p $HOME/groundwork-professional/monitor-professional/reports/login-redirect.jsp $BR_HOME/reports
mkdir $BR_HOME/reports/WEB-INF
cp -p $HOME/groundwork-professional/monitor-professional/reports/WEB-INF/*.xml $BR_HOME/reports/WEB-INF

#cp -p $HOME/groundwork-professional/monitor-professional/monarch-export/login-redirect.jsp $BR_HOME/monarch/htdocs/monarch/download/
mkdir $BR_HOME/monarch/htdocs/monarch/download/WEB-INF
cp -p $HOME/groundwork-professional/monitor-professional/monarch-export/WEB-INF/*.xml $BR_HOME/monarch/htdocs/monarch/download/WEB-INF


####################################################
# Portlet applications deployment ....
####################################################
echo "Copy nagvis webextension into the installer environment..."
cp -p $GW_HOME/foundation/container/webapps/nagvis.war $BR_HOME/foundation/container/jpp/standalone/deployments

echo "Copy RSTools webextension into the installer environment..."
cp -p $GW_HOME/foundation/container/webapps/nms-rstools*.war $BR_HOME/foundation/container/jpp/standalone/deployments

# Getting RSTool command tool set
echo "Copy RSTools command utility into the installer environment..."
cd /tmp
rm -f rstools_bsmChecker.tar.gz
wget http://morat/webextension-source/rstools/rstools_bsmChecker.tar.gz

tar xfz rstools_bsmChecker.tar.gz -C $BR_HOME/foundation/container
mkdir $BR_HOME/foundation/container/rstools/log

#echo "Copy the Cloudhub Web application into the GroundWork deployment"
#scp root@morat:/var/www/html/cloudhub/1.0.1a/*.war $BR_HOME/foundation/container/jpp/standalone/deployments

# Get ready for the CloudHub 1.1 release
scp root@morat:/var/www/html/cloudhub/1.1/7.0/cloudhub.war $BR_HOME/foundation/container/jpp/standalone/deployments
#scp root@morat:/var/www/html/cloudhub/1.1/7.0/cloudhub.war $BR_HOME/foundation/container/josso-1.8.4/webapps



echo "Placeholder for bookshelf"
mkdir $BR_HOME/bookshelf/docs
mkdir $BR_HOME/bookshelf/docs/bookshelf-data
mkdir $BR_HOME/core-config/jboss

date
echo "Monitor-pro build is done at `date`"
##########################################

echo "EntBuild.sh is done..."


 #!/bin/bash -x

# Copyright (c) 2009-2018 GroundWork, Inc. info@groundworkopensource.com

# BE SURE TO CHANGE THIS FOR A NEW GROUNDWORK MONITOR RELEASE NUMBER!
# This is a version number corresponding to the directory in which this
# script resides (e.g., GWMON-7 for the 7.1.0 release).
GWMEE_VERSION=7
GWMEE_FULL_VERSION=7.2.2
BUILD_MAIL_ADDRESSES="build-info@gwoslabs.com"

# Set this to reflect the Subversion credentials we need to commit files.
SVN_CREDENTIALS="--username build --password bgwrk08"

# Subversion repository branch name, (defaults to 'trunk').
PRO_ARCHIVE_BRANCH="trunk"
for ARG in "$@" ; do
    PRO_ARCHIVE_BRANCH_ARG="${ARG#PRO_ARCHIVE_BRANCH=}"
    if [ "$PRO_ARCHIVE_BRANCH_ARG" != "$ARG" -a "$PRO_ARCHIVE_BRANCH_ARG" != "" ] ; then
        PRO_ARCHIVE_BRANCH="$PRO_ARCHIVE_BRANCH_ARG"
    fi
done

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

BASE=$HOME/groundwork-monitor
BASE_OS=$BASE/monitor-os
BASE_CORE=$BASE/monitor-core
BUILD_DIR=$BASE/build

FOUNDATION_ENTERPRISE_CHECKOUT=/home/build/build7/foundation

# GW_PERL_VERSION=5.8.9
GW_PERL_VERSION=5.24.0

# Use site_perl for builds with Perl 5.8.9, vendor_perl for builds with Perl 5.24.0 or later.
# VENDOR_PERL=site_perl
VENDOR_PERL=vendor_perl

NTOP_BUILD_TREE=/tmp/ntop-build
NEDI_BUILD_TREE=/tmp/nedi-build
NEDI_BUILD_BASE=$NEDI_BUILD_TREE/nedi
CACTI_BUILD_TREE=/tmp/cacti-build
CACTI_BUILD_BASE=$CACTI_BUILD_TREE/cacti
WEATHERMAP_BUILD_TREE=/tmp/weathermap-build
WEATHERMAP_BUILD_BASE=$WEATHERMAP_BUILD_TREE/cacti/htdocs/plugins

RUN_DIR=/home/build
MoratDir=/var/www/html/tools/DEVELOPMENT

# "cd" and "mkdir", among other commands, are critical operations whose failure we want to be noticed and abort the build script.

check_chdir () {
    chdir_dir="$1"
    if ! cd $chdir_dir ; then
	echo "BUILD FAILED: There was an error trying to change to $chdir_dir as the current working directory." | mail -s "GWMON-$GWMEE_VERSION Enterprise Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
	exit 1
    fi
}

# Note that you can use the -p option if you want no error if the directory already exists,
# but (presumably) still an error if some other difficulty arises (such as no permission).
check_mkdir () {
    # We have to allow for more than one argument to this function.
    # For printing purposes, we capture the last argument in a clearly labeled variable.
    mkdir_dir="${!#}"
    if ! mkdir "$@"; then
	echo "BUILD FAILED: There was an error trying to create the $mkdir_dir directory." | mail -s "GWMON-$GWMEE_VERSION Enterprise Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
	exit 1
    fi
}

check_cp () {
    # We have to allow for more than one argument to this function.
    # For printing purposes, we capture the last argument in a clearly labeled variable.
    target="${!#}"
    if ! /bin/cp "$@"; then
	echo "BUILD FAILED: There was an error trying to copy to the $target location." | mail -s "GWMON-$GWMEE_VERSION Enterprise Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
	exit 1
    fi
}

check_mv () {
    # We have to allow for more than one argument to this function.
    # For printing purposes, we capture the last argument in a clearly labeled variable.
    target="${!#}"
    if ! /bin/mv "$@"; then
	echo "BUILD FAILED: There was an error trying to move to the $target location." | mail -s "GWMON-$GWMEE_VERSION Enterprise Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
	exit 1
    fi
}

check_perl () {
    # We have to allow for more than one argument to this function.
    # For printing purposes, we capture the last argument in a clearly labeled variable.
    target="${!#}"
    if ! /usr/bin/perl "$@"; then
	echo "BUILD FAILED: There was an error trying to edit the $target file." | mail -s "GWMON-$GWMEE_VERSION Enterprise Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
	exit 1
    fi
}

check_patch () {
    # We have to allow for more than one argument to this function.
    # For printing purposes, we capture the last argument in a clearly labeled variable.
    target="${!#}"
    if ! /usr/bin/patch "$@"; then
	echo "BUILD FAILED: There was an error trying to apply the $target patch." | mail -s "GWMON-$GWMEE_VERSION Enterprise Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
	exit 1
    fi
}

check_chmod () {
    # We have to allow for several arguments to this function.
    # For printing purposes, we attempt to capture only the filename argument(s) in a clearly labeled variable.
    # with individual filenames separated by newline characters for clarity in the mailed message.
    if ! /bin/chmod "$@"; then
	shift
	while [ "${1:1:1}" = '-' -o "${1:1:1}" = '+' ] || expr "$1" : '\([0-9][0-9]*\)$' > /dev/null ; do
	    shift
	done
	OLDIFS="$IFS"
# Temporarily define IFS as a newline.
IFS='
'
	files="$*"
	echo "BUILD FAILED: There was an error trying to chmod these files:$IFS$files" | mail -s "GWMON-$GWMEE_VERSION Enterprise Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
	IFS="$OLDIFS"
	exit 1
    fi
}

# Unset dangerous permission bits in a war-file.  Whether this will do any good in
# suppressing inappropriate permissions when war-files are exploded is to be determined.
clean_warfile_permissions () {
    warfile="$1"
    old_umask=`umask`
    umask 22
    warfile_dir=/tmp/warfile.$$
    rm -rf $warfile_dir
    check_mkdir $warfile_dir
    if ! unzip -q "$warfile" -d $warfile_dir; then
	rm -rf $warfile_dir
	echo "BUILD FAILED: There was an error trying to unzip the $warfile war-file." | mail -s "GWMON-$GWMEE_VERSION Enterprise Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
	exit 1
    fi
    ( check_chdir $warfile_dir ; zip -q -r - * ) > "$warfile.clean"
    if [ $? -ne 0 ]; then
	rm -rf $warfile_dir
	echo "BUILD FAILED: There was an error trying to zip the $warfile war-file." | mail -s "GWMON-$GWMEE_VERSION Enterprise Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
	exit 1
    fi
    rm -rf $warfile_dir
    ## If we try to save aside the original file, it ends up in the build.
    ## So we no longer do that.
    # check_mv "$warfile" "$warfile.orig"
    check_mv "$warfile.clean" "$warfile"
    umask $old_umask
}

# Clean up previous builds
rm -rf $GW_HOME
rm -rf $BASE
check_cp -rp /usr/local/groundwork-common.ent /usr/local/groundwork

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
	    echo "BUILD FAILED: There has been a problem trying to checkout groundwork files." | mail -s "GWMON-$GWMEE_VERSION Enterprise Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
	    exit 1
	fi
	sleep 30
    done
}

# Define a routine that will retry a Subversion export if the first attempt
# fails, to provide some protection against transient errors.
svn_export() {
    for i in 1 0; do
        if svn export "$@"; then
            break
        elif [ $i -eq 0 ]; then
	    echo "BUILD FAILED: There has been a problem trying to export groundwork files." | mail -s "GWMON-$GWMEE_VERSION Enterprise Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
	    exit 1
        fi
	# Maybe this was a transient failure that won't repeat if we wait a bit.
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
            echo "BUILD FAILED: There has been a problem trying to checkout groundwork files." | mail -s "GWMON-$GWMEE_VERSION Enterprise Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
            exit 1
        fi
        sleep 30
    done
}

# Files from Open Source repository used for the build.
# (Actually, these files are now drawn from our Pro repository.)

echo "Check out Files from (a now virtualized) Open Source Repository"
check_chdir $BUILD_BASE


# Before we check out the current code, wipe out any traces of old code from prior builds,
# to ensure that we are in fact looking only at code freshly drawn from Subversion.
rm -rf groundwork-monitor

# Check out the top-level Maven-related files that used to live at the top level of our OS repository.
svn_co -N $SVN_CREDENTIALS $PRO_ARCHIVE/archive-groundwork-monitor groundwork-monitor


# Check out from subversion
check_chdir $HOME
PRO_ARCHIVE=http://geneva/groundwork-professional/$PRO_ARCHIVE_BRANCH
svn_co -N $SVN_CREDENTIALS $PRO_ARCHIVE groundwork-professional
check_chdir $HOME/groundwork-professional

rm -rf $HOME/groundwork-professional/monitor-portal

svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/build
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/enterprise
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/foundation
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/guava-packages
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/images
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/load-test-tools
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/patch-scripts
# svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/plugins
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-portal
# svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-starter

check_mkdir monitor-agent
check_mkdir monitor-agent/gdma
check_chdir monitor-agent/gdma
#svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-agent/gdma/java-agent
# Use the new location inside monitor-platform
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-platform/agents/JDMA

check_mkdir GDMA2.1
check_chdir GDMA2.1
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-agent/gdma/GDMA2.1/profiles
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-agent/gdma/GDMA2.1/linux/gdma/gdma-core

# FIX MINOR:  This is nearly obsolete and should eventually be removed, now that we are including
# an updated GDMA spooler in GWMEE that no longer refers to the older GDMA::GDMAUtils package.
# But as of this writing (see below), there are still a couple of scripts that refer to to the
# old package.  Once they get converted to the newer package, we should drop this block.
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-agent/gdma/GDMA2.1/linux/gdma/GDMA

check_mkdir perl
check_chdir perl
# Location of the new GDMA:: Perl packages to support Auto-Setup.
# We keep these distinct from the location of the older GDMAUtils package, so as not to cause confusion.
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-agent/gdma/GDMA2.1/perl/GDMA

# Getting the CloudHub/Vema source code
check_chdir $HOME/groundwork-professional/monitor-agent
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-agent/vema

check_chdir $HOME/groundwork-professional
check_mkdir monitor-spool
check_chdir monitor-spool
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-spool/scripts/services/spooler-gdma
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-spool/config

check_chdir $HOME/groundwork-professional
svn_co -N $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-professional monitor-professional
check_chdir $HOME/groundwork-professional/monitor-professional
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-professional/apache
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-professional/auto-registration

# FIX MINOR:  Now that we have moved all of the Bronx code into the PRO repository,
# we should consolidate the bronx.cfg file we are referencing here into the same
# location as the rest of the Bronx code, and stop referring to this separate copy.
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-professional/bronx

svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-professional/database
#svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-professional/guava
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-professional/log-reporting
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-professional/migration
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-professional/monarch
#svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-professional/monarch-patch60
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-professional/nagios
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-professional/performance
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-professional/performance-core
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-professional/profiles
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-professional/resources
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-professional/snmp
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-professional/sqldata
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-professional/syslib
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-professional/tools
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-professional/fping
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-professional/noma
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-professional/perl

svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-professional/monarch-export
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-professional/reports

# ================= THIS SECTION BELOW ON LOG ARCHIVING IS STILL UNDER DEVELOPMENT =================

# Some of this activity may be moved elsewhere in this script before we're done.

check_chdir $HOME/groundwork-professional
check_mkdir monitor-archive
check_chdir monitor-archive
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-archive/bin
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-archive/config
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-archive/scripts
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/monitor-archive/var

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

$BUILD_BASE/GWMON-$GWMEE_VERSION/prepare-pro.sh PRO_ARCHIVE_BRANCH=$PRO_ARCHIVE_BRANCH

# Get Open Source postgres databases...
#echo "Checkout postgres databases from Open Source repository"


check_chdir $BASE
# Increment core-build number
echo "Comment out Build number generator in ENT BUILD. Done in nightlyBuildEE script"

# Run dos2unix for all the files under profiles directory.
# GWMON-10673:  This needs a more subtle approach, which is not yet implemented.
check_chmod +x $BUILD_DIR/d2unix.pl

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
check_chdir $BASE/monitor-professional
maven allBuild allDeploy

# Create the profile-categorization symlinks we want that are not directly present in Subversion.
# Then clean up afterward; there's no reason to preserve the SYMLINKS file in the final distribution.
echo "Make profile-categorization symlinks ..."
$BUILD_BASE/GWMON-$GWMEE_VERSION/makesymlinks nagios nagios $GW_HOME/profiles SYMLINKS
rm -f $GW_HOME/profiles/SYMLINKS


# Build Java Agents
echo "Build Java agents for Websphere, Jboss and Tomcat"

#check_chdir $BASE/monitor-agent/gdma/java-agent
check_chdir $BASE/monitor-agent/gdma/JDMA
export MAVEN_OPTS="-Dhttps.protocols=TLSv1.2"
mvn clean install

find /usr/local/groundwork -name .svn        -exec rm -rf {} \;
find /usr/local/groundwork -name .project    -exec rm -rf {} \;
find /usr/local/groundwork -name maven.xml   -exec rm -rf {} \;
find /usr/local/groundwork -name project.xml -exec rm -rf {} \;

#
#
# JBoss Enterprise portal was build in Common.sh and all the binaries have been deployed
# into the deployne=ment folder. Copy it into the Bitrock folder
#
#


# Set permissions
check_chdir $BUILD_BASE/GWMON-$GWMEE_VERSION
. set-permissions-pro.sh

date
check_cp -rp $GW_HOME/apache2/cgi-bin/profiles $BR_HOME/profiles/cgi-bin

check_cp -rp $GW_HOME/bin/* $BR_HOME/common/bin


#Make sure that resource folder is clean
rm -f $BR_HOME/core-config/resources/*
check_cp -rp $GW_HOME/config/* $BR_HOME/core-config

check_cp -p $HOME/groundwork-professional/monitor-archive/config/log-archive-receive.conf $BR_HOME/core-config
check_cp -p $HOME/groundwork-professional/monitor-archive/config/log-archive-send.conf    $BR_HOME/core-config

# These files contain some DB access credentials, so we restrict the ability to read them.
check_chmod 600 $BR_HOME/core-config/log-archive-receive.conf
check_chmod 600 $BR_HOME/core-config/log-archive-send.conf


########## Temp fix WILL BE REMOVED AFTER INSTALLER CLEANUP ############
# $BR_HOME/foundation generally already exists at this point, so we allow for that.
check_mkdir -p $BR_HOME/foundation
check_mkdir    $BR_HOME/foundation/database
check_mkdir    $BR_HOME/foundation/eclipse
check_mkdir    $BR_HOME/foundation/container
check_mkdir    $BR_HOME/foundation/container/config
touch          $BR_HOME/foundation/container/config/db.properties
touch          $BR_HOME/core-config/db.properties.os
check_mkdir    $BR_HOME/foundation/container/logs
#########################################################################


# Postgres databases
echo "Get all database scripts for postgresql ..."
rm -f $BR_HOME/databases/postgresql/*

#########################################################################################
# Force cleanup of top level directory. Installer package cleanup necessary
rm -f $BR_HOME/databases/GWCollage*
rm -f $BR_HOME/databases/*.properties.sql
rm -f $BR_HOME/databases/syslog*

check_cp -rp $BASE/monitor-professional/database/postgresql/*.sql $BR_HOME/databases/postgresql
check_cp -rp $GW_HOME/databases/postgresql/*.sql                  $BR_HOME/databases/postgresql

# FIX MINOR:  We might use shell globs in these commands, to simplify the copying, before we're done here.
check_cp -p $HOME/groundwork-professional/monitor-archive/scripts/Archive_GWCollageDB_extensions.sql $BR_HOME/databases/postgresql
check_cp -p $HOME/groundwork-professional/monitor-archive/scripts/create-fresh-archive-databases.sql $BR_HOME/databases/postgresql
check_cp -p $HOME/groundwork-professional/monitor-archive/scripts/defragment-runtime-database.sh     $BR_HOME/databases/postgresql
check_cp -p $HOME/groundwork-professional/monitor-archive/scripts/make-archive-application-type.pl   $BR_HOME/databases/postgresql
# FIX MAJOR:  We delay installing our updated copy of postgres-xtra-functions.sql until we are sure that it is truly up-to-date.
# check_cp -p $HOME/groundwork-professional/monitor-archive/scripts/postgres-xtra-functions.sql        $BR_HOME/databases/postgresql
check_cp -p $HOME/groundwork-professional/monitor-archive/scripts/set-up-archive-database.sh         $BR_HOME/databases/postgresql

# We purposely restrict the permissions on a few key scripts.
check_chmod 750 $BR_HOME/databases/postgresql/defragment-runtime-database.sh
check_chmod 750 $BR_HOME/databases/postgresql/make-archive-application-type.pl
check_chmod 750 $BR_HOME/databases/postgresql/set-up-archive-database.sh

#Update the default PostreSQL configuration
echo "Update PostgreSQL default with GroundWork settings"
check_cp -rp $BASE/monitor-professional/database/postgresql/postgresql.conf.default $BR_HOME/postgresql/share/postgresql.conf.sample

# Make sure Bitrock folder is there.  Most likely it is, so we allow for that.
check_mkdir -p $BR_HOME/foundation/feeder

echo "Remove any feeders that might come with the Bitrock package..."
rm -f $BR_HOME/foundation/feeder/*

#Get the GroundWOrk feeders that were deployed in CommonBuild
check_cp -rp   $GW_HOME/foundation/feeder/* $BR_HOME/foundation/feeder
check_cp -rp   $GW_HOME/foundation/scripts  $BR_HOME/foundation
check_chmod +x $BR_HOME/foundation/scripts/reset_passive_check.sh

# It turns out that the LogFile.pm was never used.
#check_cp -p $GW_HOME/log-reporting/lib/LogFile.pm $BR_HOME/foundation/feeder


check_cp -rp $GW_HOME/gwreports/* $BR_HOME/gwreports

echo "Remove obsolete reports"
rm -rf $BR_HOME/gwreports/LogReports

echo "Folder layout for reports ..."
check_cp -fp $HOME/groundwork-professional/monitor-portal/applications/reportserver/src/main/resources/report_en.xml $BR_HOME/gwreports
check_cp -fp $HOME/groundwork-professional/monitor-portal/applications/reportserver/src/main/resources/report_fr.xml $BR_HOME/gwreports

############ Cleanup #############################################################
# Log-reporting has been taken out of the product in 2008. No need to include it
#check_cp -rp $GW_HOME/log-reporting/* $BR_HOME/log-reporting
#Make sure log-reporting files are not included
rm -rf $BR_HOME/log-reporting/*
##################################################################################

check_cp -p $GW_HOME/apache2/cgi-bin/snmp/mibtool/index.cgi $BR_HOME/snmp/cgi-bin/snmp/mibtool
check_cp -rp $GW_HOME/apache2/htdocs/snmp/mibtool/* $BR_HOME/snmp/htdocs/snmp/mibtool

# GWMON-5445:  Use the copy of snmptt.ini from our Subversion, not an old copy that BitRock captured long ago.
check_cp -p $HOME/groundwork-professional/monitor-professional/snmp/snmptt/snmptt.ini $BR_HOME/common/etc/snmp
chown nagios:nagios                                                                   $BR_HOME/common/etc/snmp/snmptt.ini

# FIX MAJOR:  This copying did not work in one of our buildss, because /usr/local/groundwork/migration/* did not exist.
# That was during a build where we had no archive-groundwork-monitor Maven files in play.  Whether those two circumstances
# are related is subject to further testing.  In the meantime, we have disabled checking the result here.
# What to do about this?
cp -rp $GW_HOME/migration/* $BR_HOME/migration

echo "Cleanup obsolete scripts..."
rm -f $BR_HOME/migration/devclean.pl

# Copy JBoss admin script into migration folder
echo "include JBoss admin role script"
check_cp -rp $HOME/groundwork-professional/monitor-professional/migration/migrate_admin* $BR_HOME/migration

# Set the permissions for the post-upgrade scripts.  We do this before copying
# because it's easier to identify the scripts we're targeting in bulk in their
# source directory rather than individually afterward in their target directory.
echo "Set permissions for post-upgrade scripts"
check_chmod 755 $HOME/groundwork-professional/monitor-professional/migration/jpp6x/*.sh

# Copy JBoss 6.7 data migration script into migration folder
echo "JBoss migration for GWME 6.x data to GWME 7"
check_cp -rp $HOME/groundwork-professional/monitor-professional/migration/jpp6x/* $BR_HOME/migration

echo "JBoss Portal Navigation updates for the 7.1.0 Release (GWMON-12416)"
check_mkdir $BR_HOME/migration/portal-objects-710
check_cp -rp $HOME/groundwork-professional/monitor-professional/migration/portal-objects-710/*.zip $BR_HOME/migration/portal-objects-710

echo "JBoss Portal Navigation updates for the 7.2.1 Release (GWMON-13360, GWMON-13375)"
check_mkdir $BR_HOME/migration/portal-objects-721
check_cp -rp $HOME/groundwork-professional/monitor-professional/migration/portal-objects-721/navigational-nodes-to-delete $BR_HOME/migration/portal-objects-721
check_cp -rp $HOME/groundwork-professional/monitor-professional/migration/portal-objects-721/*.zip                        $BR_HOME/migration/portal-objects-721

echo "Copy cacti migration scripts into the build ..."
check_cp -p $HOME/groundwork-professional/monitor-professional/migration/702-SP3/install/migrate_cacti_feeder_config.pl $BR_HOME/migration
# For GWMON-12764 DN 2016-10-21
check_cp -p $HOME/groundwork-professional/monitor-professional/migration/702-SP3/install/migrate_RAPID_feeder_configs_711.sh $BR_HOME/migration

# Copy gwcollagedb migration script for gdma plugins fix (GWMON-10084)
check_cp -rp $HOME/groundwork-professional/monitor-professional/migration/upgrade_gwcollage_* $BR_HOME/migration

# GWMON-10320 -- Include migration validation scripts
echo "Copy MySQL validation scripts into installer folder"
check_cp -p $HOME/groundwork-professional/monitor-professional/migration/delete_* $BR_HOME/migration
check_cp -p $HOME/groundwork-professional/monitor-professional/migration/find_*   $BR_HOME/migration

# GWMON-13013 - include grafbridge/
check_cp -rp $HOME/groundwork-professional/monitor-professional/migration/grafbridge   $BR_HOME/migration
check_cp -p $HOME/groundwork-professional/monitor-professional/migration/modify_navigation_objects   $BR_HOME/migration

echo "Create postgresql folder for migration scripts in the Bitrock installer folder"
# FIX MINOR:  This directory already exists from previous scripting, so why do we think it's necessary to make it here?
# As a workaround for the time being, we have applied the -p flag to suppress errors if it already exists.
check_mkdir -p $BR_HOME/migration/postgresql

echo "Copy all postgreSQL migration scripts into installer"
check_cp -rp $HOME/groundwork-professional/monitor-professional/migration/postgresql/* $BR_HOME/migration/postgresql/
check_mv $BR_HOME/migration/postgresql/patch-gwcollagedb.sql $BR_HOME/migration/

# GWMON-10327
check_mv -f $BR_HOME/migration/postgresql/validate_gw_db.pl $BR_HOME/migration/

check_cp -rp $GW_HOME/monarch/automation/conf/* $BR_HOME/monarch/automation/conf

check_cp -rp $BASE_CORE/nagios/etc/nagios.cfg     $BR_HOME/nagios/etc
check_cp -rp $GW_HOME/nagios/etc/nagios.initd.pro $BR_HOME/nagios/etc
check_cp -rp $GW_HOME/nagios/eventhandlers/*      $BR_HOME/nagios/eventhandlers

# GWMON-10578:  We use an explicit option to force no symlink dereferencing,
# instead of relying on the GNU cp default of not following symlinks simply
# because we're doing a recursive copy.  See "info cp" for details.
check_cp -rpP $GW_HOME/profiles/* $BR_HOME/profiles

check_cp -rp $GW_HOME/sbin/* $BR_HOME/common/sbin

check_cp -rp $GW_HOME/tools $BR_HOME


echo "Update Tomcat configs...."
# FIX MAJOR:  This copying no longer works, because /home/build/BitRock/groundwork/foundation/container/webapps
# (an ancestor directory of the targets in these attempts to copy) does not exist.
# What to do about this?
cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/context.xml   $BR_HOME/foundation/container/webapps/jboss/jboss-web.deployer
cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/web.xml       $BR_HOME/foundation/container/webapps/jboss/jboss-web.deployer/ROOT.war/WEB-INF/
cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/jboss-web.xml $BR_HOME/foundation/container/webapps/jboss/jboss-web.deployer/ROOT.war/WEB-INF/


# JIRA GWMON-9344
echo "Cleanup of any existing nagvis files before expanding the latest package"
rm -rf $BR_HOME/nagvis
check_cp -rp $BUILD_BASE/monitor-platform/monitor-apps/nagvis-fs/target/nagvis-fs/nagvis $BR_HOME

# Fix for GWMON-9365
check_mkdir $BR_HOME/nagvis/migration
check_cp $GW_HOME/migration/migrate-nagvis* $BR_HOME/nagvis/migration

#check_cp $HOME/groundwork-professional/monitor-portal/applications/nagvis/migration/*pl $BR_HOME/nagvis/migration

check_chdir /tmp
rm -f nagvis-default-maps.tar*
echo "Get the defaults maps which are a  copy of Groundwork Live"
wget http://morat/webextension-source/nagvis/nagvis-default-maps.tar.gz

echo "Replace default maps"
rm -f $BR_HOME/nagvis/etc/maps/*.cfg
tar xfz nagvis-default-maps.tar.gz -C $BR_HOME/

echo "Move auto_* maps into a subdir for reference. Should not be in the main folder"
check_mkdir $BR_HOME/nagvis/etc/maps/sav
mv $BR_HOME/nagvis/etc/maps/auto_* $BR_HOME/nagvis/etc/maps/sav


echo "Media files for Audible Alarms in Event Console"
check_cp -f $HOME/groundwork-professional/monitor-portal/applications/console/WebContent/resources/*.mp3 $BR_HOME/core-config/media

echo "Copy modified httpd.conf and httpd-ssl.conf for enterprise version"
# See GWMON-12778 for GroundWork changes to the httpd-ssl.conf file.  Note that these files are
# essentially now frozen in our Subversion, and that we are completely replacing the upstream
# copies.  If the upstream Apache maintainers fix problems with them, we won't automatically
# inherit such fixes.  This is particularly an issue when we upgrade the Apache major version
# (say, 2.2 to 2.4).  So we must be vigilant about what happens there.  Perhaps at some point,
# we might modify this whole-file override (at least for httpd-ssl.conf) by a more sophisticated
# modification that (say) just replaces the values of the SSLProtocol and SSLCipherSuite options
# from our checked-in file, and leaves the rest of what BitRock provides alone.
check_cp -rp $HOME/groundwork-professional/monitor-professional/apache/httpd.conf     $BR_HOME/apache2/conf
check_cp -rp $HOME/groundwork-professional/monitor-professional/apache/httpd-ssl.conf $BR_HOME/apache2/conf/extra
check_cp -rp $HOME/groundwork-professional/monitor-professional/apache/httpd-security.conf $BR_HOME/apache2/conf/extra

echo "Copy mod_security2 security rules"
check_cp -rp $HOME/groundwork-professional/monitor-professional/apache/mod_security2/modsecurity-crs/ $BR_HOME/apache2/conf/extra/
echo "Force use of GroundWork Perl for mod_security2 Perl scripts"
check_perl -l -p -i -e       's{/usr/bin/perl}{/usr/local/groundwork/perl/bin/perl}' $BR_HOME/apache2/conf/extra/modsecurity-crs/util/av-scanning/runav.pl
check_perl -l -p -i -e 's{/opt/local/bin/perl}{/usr/local/groundwork/perl/bin/perl}' $BR_HOME/apache2/conf/extra/modsecurity-crs/util/regression-tests/rulestest.pl
check_perl -l -p -i -e 's{/opt/local/bin/perl}{/usr/local/groundwork/perl/bin/perl}' $BR_HOME/apache2/conf/extra/modsecurity-crs/util/rule-management/remove-2.7-actions.pl
check_perl -l -p -i -e 's{/opt/local/bin/perl}{/usr/local/groundwork/perl/bin/perl}' $BR_HOME/apache2/conf/extra/modsecurity-crs/util/virtual-patching/arachni2modsec.pl
check_perl -l -p -i -e 's{/opt/local/bin/perl}{/usr/local/groundwork/perl/bin/perl}' $BR_HOME/apache2/conf/extra/modsecurity-crs/util/virtual-patching/zap2modsec.pl
echo "Create a permanent directory for use by the SecDataDir directive"
check_mkdir     $BR_HOME/apache2/conf/extra/modsecurity-crs/secdatadir
check_chmod 755 $BR_HOME/apache2/conf/extra/modsecurity-crs/secdatadir

# Ntop configuration
check_cp -p $NTOP_BUILD_TREE/ntop.properties $BR_HOME/core-config

# NoMa control, configuration, and extended scripts
echo "Copy NoMa control scripts and configuration files"
check_cp -p $HOME/groundwork-professional/monitor-professional/noma/scripts/ctl.sh                    $BR_HOME/noma/scripts
check_cp -p $HOME/groundwork-professional/monitor-professional/noma/noma_2.0.3/gwos/NoMa.yaml         $BR_HOME/noma/etc
check_cp -p $HOME/groundwork-professional/monitor-professional/noma/noma_2.0.3/gwos/sendEmail_gwos.pl $BR_HOME/noma/notifier

# Old files for running PHP under Java in support of the original NoMa UI code.
# This setup is no longer used for that purpose, as of GWMEE 7.1.0.  See GWMON-12705
# for extensive detail on the use of PHP by various system components, including NoMa.
check_cp -p $HOME/groundwork-professional/monitor-professional/apache/apache2-noma.conf               $BR_HOME/apache2/conf/groundwork/apache2-noma.conf
check_cp -p $HOME/groundwork-professional/monitor-professional/apache/php.ini                         $BR_HOME/apache2/conf/groundwork/php.ini

# NoMa changes by GroundWork for Downtime scheduling
check_cp -p $HOME/groundwork-professional/monitor-professional/noma/noma_2.0.3/gwos/lib/GWDOWN.pm     $BR_HOME/noma/notifier/lib/GWDOWN.pm
check_cp -p $HOME/groundwork-professional/monitor-professional/noma/noma_2.0.3/gwos/lib/downtime.pm   $BR_HOME/noma/notifier/lib/downtime.pm

# NoMa changes by GroundWork in support of fetching hostgroup and servicegroup information from Foundation
check_cp -p $HOME/groundwork-professional/monitor-professional/noma/noma_2.0.3/gwos/lib/groupnames.pm $BR_HOME/noma/notifier/lib/groupnames.pm

# Local copies of NoMa files, that are deprecated.  We should instead be using the patched copies built by BitRock.
check_cp -p $HOME/groundwork-professional/monitor-professional/noma/noma_2.0.3/gwos/alert_via_noma.pl $BR_HOME/noma/notifier/alert_via_noma.pl
check_cp -p $HOME/groundwork-professional/monitor-professional/noma/noma_2.0.3/gwos/noma_daemon.pl    $BR_HOME/noma/notifier/noma_daemon.pl
check_cp -p $HOME/groundwork-professional/monitor-professional/noma/noma_2.0.3/gwos/lib/bundler.pm    $BR_HOME/noma/notifier/lib/bundler.pm
check_cp -p $HOME/groundwork-professional/monitor-professional/noma/noma_2.0.3/gwos/lib/contacts.pm   $BR_HOME/noma/notifier/lib/contacts.pm

# The NoMa.yaml file needs restricted permissions because it contains password data.
check_chmod 600 $BR_HOME/noma/etc/NoMa.yaml

echo "Change permissons on Notifier script"
check_chmod 755 $BR_HOME/noma/notifier/*.pl

# Foundation UI protection
echo "Copy Foundation apache configuration files"
check_cp -p $HOME/groundwork-professional/monitor-professional/apache/foundation-ui.conf $BR_HOME/apache2/conf/groundwork/foundation-ui.conf

# Grafana integration for GrafBridge
echo "Copy Grafana apache configuration file"
check_cp -p $HOME/groundwork-professional/monitor-professional/apache/grafana.conf $BR_HOME/apache2/conf/groundwork/grafana.conf

# Auto-Registration and Auto-Setup
echo "Copy auto-registration scripts, modules, and supporting config files into the build"
check_cp -p $HOME/groundwork-professional/monitor-professional/auto-registration/server/*.{pl,pm} $BR_HOME/foundation/scripts
check_cp -p $HOME/groundwork-professional/monitor-professional/auto-registration/server/autosetup $BR_HOME/groundwork-spooler/gdma/bin
check_cp -p $HOME/groundwork-professional/monitor-professional/auto-registration/server/register_agent.properties        $BR_HOME/core-config
check_cp -p $HOME/groundwork-professional/monitor-professional/auto-registration/server/register_agent_by_discovery.conf $BR_HOME/core-config

# GroundWork Perl modules
echo "Copy Custom GroundWork Perl modules into distribution"
check_mkdir $BR_HOME/perl/lib/$VENDOR_PERL/$GW_PERL_VERSION/GW
check_cp -p $HOME/groundwork-professional/monitor-professional/perl/GW/*.pm $BR_HOME/perl/lib/$VENDOR_PERL/$GW_PERL_VERSION/GW


#
# NeDi configuration
#
if true; then
    # New construction for the NeDi 1.0.7 (or later) release, created directly from the
    # standard public NeDi distribution with explicit applied patches for porting to
    # PostgreSQL and newly added files to support the GroundWork context.  This part
    # should not need to be changed to support subsequent NeDi releases; only the upstream
    # scripting that populates the $NEDI_BUILD_BASE file tree will need adjustment.
    rm -rf $BR_HOME/nedi
    check_cp -pr $NEDI_BUILD_BASE $BR_HOME/
    check_cp -p $NEDI_BUILD_TREE/nedi.properties $BR_HOME/core-config/nedi.properties
    check_cp -p $NEDI_BUILD_TREE/nedi_httpd.conf $BR_HOME/apache2/conf/groundwork/nedi_httpd.conf
else
    # Legacy construction, no longer needed now that we will fold NeDi into the
    # base product with an entirely different manner of constructing the release.
    echo "Nedi post build updates"
    tar xfz $HOME/groundwork-professional/monitor-portal/applications/nms/conf/patches/nedi_patched.tgz -C $BR_HOME/
    chown nagios.nagios $BR_HOME/nedi/*.pl
    chown nagios.nagios $BR_HOME/nedi/*.conf

    chmod 755 $BR_HOME/nedi/*.pl
fi

# create a NeDi log directory
check_mkdir     $BR_HOME/common/var/log/nedi
check_chmod 755 $BR_HOME/common/var/log/nedi

# create a working directory for use by NeDi's invocation of nfdump
check_mkdir     $BR_HOME/common/var/nfdump
check_chmod 755 $BR_HOME/common/var/nfdump

#
# Cacti configuration
#
echo "Cacti post build updates"

if true; then
    # New construction for Cacti dynamically patched starting from the standard public distribution.
    # This is handled by upstream scripting with explicit applied patches for porting to PostgreSQL
    # and to add many of the newly added files to support the GroundWork context.
    rm -rf $BR_HOME/cacti
    check_cp -pr $CACTI_BUILD_BASE $BR_HOME/

    echo "Copy default splash screens for plugins"
    # FIX MAJOR:  The nedi splash screen will be dropped once we have BitRock establish the necessary replacement symlink, the equivalent of:
    #     rm -f                             /usr/local/groundwork/foundation/container/webapps/nedi.war
    #     ln -s /usr/local/groundwork/nedi/ /usr/local/groundwork/foundation/container/webapps/nedi.war
    # and presumably the other splash screens can be dropped as well when those components are in the build as well
    # check_cp -f $HOME/groundwork-professional/monitor-portal/applications/nms/src/main/webapp/plugins/nedi/*       $BR_HOME/cacti/htdocs/splash/nedi
    check_cp -f $HOME/groundwork-professional/monitor-portal/applications/nms/src/main/webapp/plugins/ntop/*       $BR_HOME/cacti/htdocs/splash/ntop
    check_cp -f $HOME/groundwork-professional/monitor-portal/applications/nms/src/main/webapp/plugins/weathermap/* $BR_HOME/cacti/htdocs/splash/weathermap

    # FIX MINOR:  this shouldn't be necessary once we get rid of the splash files,
    # because all ownership setting is already handled when this file tree is built
    chown -R nagios:nagios $BR_HOME/cacti

    # FIX MINOR:  remove this, as it appears to no longer be necessary
    # cacti includes ntop plugins directory by default that includes a set of dummy files
    # GWMON-9250
    # rm -rf $BR_HOME/cacti/htdocs/plugins/ntop

    check_cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/cacti_updates/cacti_cron.sh $BR_HOME/common/bin/cacti_cron.sh
    chmod +x $BR_HOME/common/bin/cacti_cron.sh

    # Exact same line exists in this file. Why duplicate?
    #    check_cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/cacti_updates/find_cacti_graphs $BR_HOME/foundation/feeder/find_cacti_graphs
    #    chmod 755 $BR_HOME/foundation/feeder/find_cacti_graphs

    # Presumably, the check_cacti.conf file is moved elsewhere by the BitRock installer.
    check_cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/cacti_updates/check_cacti.conf $BR_HOME/cacti
    check_cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/cacti_updates/check_cacti.pl   $BR_HOME/nagios/libexec
    chmod +x $BR_HOME/nagios/libexec/check_cacti.pl

    # Presumably, the cacti.properties file is moved elsewhere by the BitRock installer.
    check_cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/cacti_updates/cacti.properties $BR_HOME/cacti

    check_cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/cacti_updates/*.xml $BR_HOME/monarch/automation/templates

    check_cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/cacti_updates/php.ini-php5.6 $BR_HOME/php/etc/php.ini

    echo "Spine configuration"
    check_cp $BR_HOME/common/etc/spine.conf.dist $BR_HOME/common/etc/spine.conf

    # FIX MINOR:  remove this, as it appears to no longer be necessary -- and if reject files appear in the build again,
    # we ought to be discovering why and fix that upstream (except for certain expected rejects during our PostgreSQL build)!
    # GWMON-9578 Cleanup of files
    # find $BR_HOME/cacti/htdocs -name *.rej -exec rm -f  '{}' \;

    echo "Include Weathermap"
    check_cp -pr $WEATHERMAP_BUILD_BASE $BR_HOME/cacti/htdocs
    check_cp -p $WEATHERMAP_BUILD_TREE/weathermap.properties $BR_HOME/core-config
else
    # Legacy construction, no longer needed now that we will fold the construction of Cacti from
    # first principles (the standard public distribution plus patches) into the base product.
    tar xfz $HOME/groundwork-professional/monitor-portal/applications/nms/conf/patches/cacti_patched.tgz -C $BR_HOME/

    echo "Copy default splash screens for plugins"
    check_cp -f $HOME/groundwork-professional/monitor-portal/applications/nms/src/main/webapp/plugins/nedi/*       $BR_HOME/cacti/htdocs/splash/nedi
    check_cp -f $HOME/groundwork-professional/monitor-portal/applications/nms/src/main/webapp/plugins/ntop/*       $BR_HOME/cacti/htdocs/splash/ntop
    check_cp -f $HOME/groundwork-professional/monitor-portal/applications/nms/src/main/webapp/plugins/weathermap/* $BR_HOME/cacti/htdocs/splash/weathermap

    chown -R nagios:nagios $BR_HOME/cacti

    # cacti includes ntop plugins directory by default that includes a set of dummy files
    # GWMON-9250

    rm -rf $BR_HOME/cacti/htdocs/plugins/ntop

    echo "Default database for cacti and migration scripts ..."
    check_cp -f $HOME/groundwork-professional/monitor-portal/applications/nms/conf/database/*.sql $BR_HOME/cacti/scripts

    check_cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/cacti_updates/cacti_cron.sh $BR_HOME/common/bin/cacti_cron.sh
    chmod +x $BR_HOME/common/bin/cacti_cron.sh

    # Cacti feeder comes with other feeders from monitor-platform
    #    check_cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/cacti_updates/find_cacti_graphs $BR_HOME/foundation/feeder/find_cacti_graphs
    chmod 755 $BR_HOME/foundation/feeder/find_cacti_graphs

    check_cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/cacti_updates/check_cacti.conf $BR_HOME/cacti
    check_cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/cacti_updates/check_cacti.pl   $BR_HOME/nagios/libexec
    chmod +x $BR_HOME/nagios/libexec/check_cacti.pl

    check_cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/cacti_updates/cacti.properties $BR_HOME/cacti

    check_cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/cacti_updates/*.xml $BR_HOME/monarch/automation/templates

    check_cp $HOME/groundwork-professional/monitor-portal/applications/nms/conf/cacti_updates/php.ini-php5.6 $BR_HOME/php/etc/php.ini

    echo "Spine configuration"
    check_cp $BR_HOME/common/etc/spine.conf.dist $BR_HOME/common/etc/spine.conf

    # GWMON-9578 Cleanup of files
    find $BR_HOME/cacti/htdocs -name *.rej -exec rm -f  '{}' \;
fi

# Make directories in which GDMA Auto-Setup instructions and trigger files will be made available for GDMA clients to pick up,
# and other directories also used to support Auto-Setup on the server side.  We need the NOTEMPTY files to be present not for
# our own use, but to bypass a BitRock packager or installer convention that empty directories will be skipped.
check_mkdir $BR_HOME/apache2/htdocs/gdma_instructions
check_mkdir $BR_HOME/apache2/htdocs/gdma_trigger
# These directories are handled instead by the groundwork-spooler.xml file, controlled by BitRock.
# check_mkdir $BR_HOME/groundwork-spooler/gdma/autosetup_locks
# touch       $BR_HOME/groundwork-spooler/gdma/autosetup_locks/NOTEMPTY
# check_mkdir $BR_HOME/groundwork-spooler/gdma/discovered
# touch       $BR_HOME/groundwork-spooler/gdma/discovered/NOTEMPTY

# Create a plugin_download directory
# FIX MAJOR:  This directory creation fails, because the ancestor /home/build/BitRock/groundwork/apache2/htdocs/agents/ directory does not exist.
# What to do about this?
mkdir $BR_HOME/apache2/htdocs/agents/plugin_download

# Include Java Agents in deployment
check_mkdir $BR_HOME/apache2/htdocs/java-agents

echo "Copy Java agents and property files to Bitrock package"
check_cp -p $BASE/monitor-agent/gdma/JDMA/appserver/jboss/target/gwos-jboss-monitoringAgent.war        $BR_HOME/apache2/htdocs/java-agents
check_cp -p $BASE/monitor-agent/gdma/JDMA/appserver/jboss-AS7/target/gwos-jbossas7-monitoringAgent.war $BR_HOME/apache2/htdocs/java-agents
check_cp -p $BASE/monitor-agent/gdma/JDMA/appserver/websphere/target/gwos-was-monitoringAgent.war      $BR_HOME/apache2/htdocs/java-agents
check_cp -p $BASE/monitor-agent/gdma/JDMA/appserver/tomcat/target/gwos-tomcat-monitoringAgent.war      $BR_HOME/apache2/htdocs/java-agents
check_cp -p $BASE/monitor-agent/gdma/JDMA/appserver/weblogic/target/gwos-wls-monitoringAgent.war       $BR_HOME/apache2/htdocs/java-agents

#JIRA GWMON-11720
# Copy tomcat and JBoss AS 7 agents into GroundWork deployment is done at the end of this script after the JOSSO server has been expanded
#

echo "Copy GDMA profiles"
check_cp -rp $BASE/monitor-agent/gdma/GDMA2.1/profiles/*.pl $BR_HOME/gdmadist/automationscripts

# FIX MINOR:  This is effectively obsolete and should eventually be removed, now that we are including
# an updated GDMA spooler (gdma/bin/gdma_spool_processor.pl) in GWMEE that no longer refers to the older
# GDMA::GDMAUtils package in preference to its replacement GDMA::Utils package.  But as of this writing,
# we still have residual references to this package in the nagios2collage_socket.pl and syslog2nagios.pl
# scripts that should be converted over to the GDMA::Utils package in due course.  Until that happens, we
# still need this package in our GWMEE builds.
echo "Copy obsolete GDMA package for the old server-side GDMA spooler and a couple of residual references"
check_cp -rp $BASE/monitor-agent/gdma/GDMA2.1/GDMA $BR_HOME/perl/lib/$VENDOR_PERL/$GW_PERL_VERSION

echo "Copy GDMA packages for server-side GDMA Auto-Setup support"
check_cp -rp $BASE/monitor-agent/gdma/GDMA2.1/perl/GDMA $BR_HOME/perl/lib/$VENDOR_PERL/$GW_PERL_VERSION

echo "Copy config file for the extended Foundation API"
#check_cp -p $FOUNDATION_ENTERPRISE_CHECKOUT/resources/gdma_plugin_update.dtd $BR_HOME/core-config

# Include Webmetrics landing page for the lead generation
check_mkdir $BR_HOME/apache2/htdocs/webmetrics
echo "Adding Webmetrics landing page to package"

check_cp -rp $HOME/groundwork-professional/monitor-professional/apache/htdocs/webmetrics/* $BR_HOME/apache2/htdocs/webmetrics

# Make sure that user is forwarded to portal login
check_cp -f $HOME/groundwork-professional/monitor-professional/apache/htdocs/index.html $BR_HOME/apache2/htdocs

# GWMON-12705:  We no longer provide copies of PHP libraries in the foundation/jboss/native/bin/ directory,
# because they appear to be completely useless there.  If we did need them at that location, we would be
# better off creating symlinks, not separate copies, to avoid the needless bloat.
check_mkdir                                   $BR_HOME/foundation/jboss/native
check_mkdir                                   $BR_HOME/foundation/jboss/native/lib
check_cp $BR_HOME/php-java-bridge/libphp5*.so $BR_HOME/foundation/jboss/native/lib

# Update revision number on morat
# FIX MAJOR:  This is not working, because the project.properties file does not exist at that location.
svn info /home/nagios/groundwork-monitor/monitor-professional/project.properties | grep Revision: | awk '{ print $2; }' > $RUN_DIR/logs/EE_Revision

/usr/bin/perl -p -i -e "s:/usr/local/groundwork/nagios:\@\@BITROCK_NAGIOS_ROOTDIR\@\@:g" $BR_HOME/nagios/etc/nagios.cfg


# Nagios JOSSO integration needs several file in the WEB-INF directory
check_cp -rp $HOME/groundwork-professional/monitor-portal/applications/nagios/conf/MANIFEST.MF                    $BR_HOME/nagios/META-INF
check_cp -rp $HOME/groundwork-professional/monitor-portal/applications/nagios/conf/context.xml                    $BR_HOME/nagios/WEB-INF
check_cp -rp $HOME/groundwork-professional/monitor-portal/applications/nagios/conf/web.xml                        $BR_HOME/nagios/WEB-INF
check_cp -rp $HOME/groundwork-professional/monitor-portal/applications/nagios/conf/jboss-web.xml                  $BR_HOME/nagios/WEB-INF
# check_cp -rp $HOME/groundwork-professional/monitor-portal/applications/nagios/conf/login-redirect.jsp           $BR_HOME/nagios
check_cp -rp $HOME/groundwork-professional/monitor-portal/applications/nagios/conf/jboss-deployment-structure.xml $BR_HOME/nagios/WEB-INF

echo "InfluxDB"
rm -rf $BR_HOME/influxdb
check_cp -rp $BUILD_BASE/monitor-platform/monitor-apps/influxdb/target/influxdb $BR_HOME/influxdb

echo "Grafana Server"
rm -rf $BR_HOME/grafana
check_cp -rp $BUILD_BASE/monitor-platform/monitor-apps/grafana-server/target/grafana $BR_HOME/grafana

echo "Staging a Grafana datasource - installing it into $BR_HOME/grafana/data/plugins/groundwork"
check_cp -rp $BUILD_BASE/monitor-platform/monitor-apps/grafana-datasource/dist $BR_HOME/grafana/data/plugins/groundwork

echo "Staging built Grafana plugins ..."
for plugintgz in $BUILD_BASE/monitor-platform/monitor-apps/grafana-server/src/data/plugins/*.tgz; do
    echo "--->  Unpacking and installing $plugintgz into $BR_HOME/grafana/data/plugins"
    tar xfz $plugintgz -C $BR_HOME/grafana/data/plugins  # unpack
    rm -f $BR_HOME/grafana/data/plugins/`basename $plugintgz` # don't want the tgz in the installed grafana
done

# defaults.ini contains creds so lock it down
echo "Setting 600 perms on grafana defaults.ini"
check_chmod 600 $BR_HOME/grafana/conf/defaults.ini

# grafbridge-control needs to be executable. Even though it's checked in with svn:executable property, it loses that somewhere
echo "Setting 755 perms on grafbridge-control"
check_chmod 755 $BR_HOME/grafana/scripts/grafbridge-control

# VEMA file structure
echo "Cloud Hub directories and monitoring profiles"
# FIX MINOR:  The vema/... directories already exist, so we suppress error checking here for that condition.
# But the similar chrhev/... directories do not.  Why the difference?
check_mkdir -p $BR_HOME/vema
check_mkdir -p $BR_HOME/vema/profiles
check_mkdir -p $BR_HOME/vema/var
check_mkdir $BR_HOME/chrhev
check_mkdir $BR_HOME/chrhev/var
#check_cp $HOME/groundwork-professional/monitor-agent/vema/common/conf/*profile.xml $BR_HOME/vema/profiles

check_cp $BUILD_BASE/monitor-platform/agents/cloudhub/src/profiles/*profile.xml $BR_HOME/vema/profiles
check_cp -p $BUILD_BASE/monitor-platform/agents/cloudhub/src/profiles/vmware_monitoring_profile.xml       $BR_HOME/core-config/vmware-monitoring-profile.xml
check_cp -p $BUILD_BASE/monitor-platform/agents/cloudhub/src/profiles/rhev_monitoring_profile.xml         $BR_HOME/core-config/rhev-monitoring-profile.xml
check_cp -p $BUILD_BASE/monitor-platform/agents/cloudhub/src/profiles/opendaylight_monitoring_profile.xml $BR_HOME/core-config/opendaylight-monitoring-profile.xml
check_cp -p $BUILD_BASE/monitor-platform/agents/cloudhub/src/profiles/docker_monitoring_profile.xml       $BR_HOME/core-config/docker-monitoring-profile.xml


# Log Archiving file structure, in the form where the installer will pick it up
# (this tree will end up as /usr/local/groundwork/core/archive/...).
echo "Log Archiving directories, scripts, and initial state files"
# FIX MINOR:  The archive/... directories already exist, so we suppress error checking here for that condition.
check_mkdir -p $BR_HOME/archive
check_mkdir -p $BR_HOME/archive/bin
check_mkdir -p $BR_HOME/archive/log-archive
check_mkdir -p $BR_HOME/archive/var

check_cp -p $HOME/groundwork-professional/monitor-archive/bin/log-archive-receive.pl           $BR_HOME/archive/bin
check_cp -p $HOME/groundwork-professional/monitor-archive/bin/log-archive-send.pl              $BR_HOME/archive/bin
check_cp -p $HOME/groundwork-professional/monitor-archive/bin/capture-old-status-markers.pl    $BR_HOME/archive/bin
check_cp -p $HOME/groundwork-professional/monitor-archive/bin/restore-old-status-markers.pl    $BR_HOME/archive/bin
check_cp -p $HOME/groundwork-professional/monitor-archive/bin/create-current-status-markers.pl $BR_HOME/archive/bin

# Restrict the ability to run the archiving scripts, because you don't want
# anyone who has no access to the state files to run the daemon scripts.
check_chmod 750 $BR_HOME/archive/bin/log-archive-receive.pl
check_chmod 750 $BR_HOME/archive/bin/log-archive-send.pl

# The same goes for the one-shot database-repair scripts,
# both for general database protection and because some
# of them might also access the archiving state files.
check_chmod 750 $BR_HOME/archive/bin/capture-old-status-markers.pl
check_chmod 750 $BR_HOME/archive/bin/restore-old-status-markers.pl
check_chmod 750 $BR_HOME/archive/bin/create-current-status-markers.pl

check_cp -p $HOME/groundwork-professional/monitor-archive/var/log-archive-receive.state $BR_HOME/archive/var
check_cp -p $HOME/groundwork-professional/monitor-archive/var/log-archive-send.state    $BR_HOME/archive/var

# These files contain critical long-term state info that we don't want to risk being damaged
# by inadvertent editing, so we restrict the permissions.
check_chmod 600 $BR_HOME/archive/var/log-archive-receive.state
check_chmod 600 $BR_HOME/archive/var/log-archive-send.state


# JIRA fixes

$BUILD_DIR/d2unix.pl $BASE/enterprise/syslog-ng.conf
$BUILD_DIR/d2unix.pl $BASE/enterprise/*.pl
check_cp -p $BASE/enterprise/syslog-ng.conf   $BR_HOME/common/etc
check_cp -p $BASE/enterprise/syslog2nagios.pl $BR_HOME/common/bin/syslog2nagios.pl
check_chmod +x $BR_HOME/common/bin/*.pl

# GWMON-10920
check_cp -p $BASE/enterprise/syslog2nagios.conf $BR_HOME/core-config

# Support script added to distribution
check_cp -p $BASE/monitor-professional/tools/gwdiags.pl $BR_HOME/common/bin
check_chmod +x      $BR_HOME/common/bin/gwdiags.pl
chown nagios:nagios $BR_HOME/common/bin/gwdiags.pl

# GWMON-10553
patch $BR_HOME/common/bin/sendEmail $BASE/patch-scripts/GroundWork/sendEmail-1.56/sendEmail.SSL_verify_mode.patch

# GWMON-13058:  This patch is designed to work with the check_postgres 2.22.0 package and to fail with
# later upgrades to that package.  We know that the fix being applied by this patch will be fixed in the
# 2.23.0 release, so once that version is released and we upgrade, we will no longer need this patch.
#
# GWMON-13058:  As of br480 for GWMEE 7.2.1, we have upgraded check_postgres to the 2.23.0 release,
# so we no longer apply this patch (it fails if we try to do so, thereby stopping the build).
#
## check_patch $BR_HOME/nagios/libexec/check_postgres.pl $BASE/patch-scripts/GroundWork/check_postgres-2.22.0/check_postgres.pl.casting.patch

check_chdir $HOME
rm -rf css-html%20deliverables
svn_co http://geneva/svn/engineering/projects/GWMON-6.0/css-html%20deliverables

# GWMON-5830
check_cp -rp $BASE_OS/performance/perfchart.cgi $BR_HOME/performance/cgi-bin/performance

# GWMON-9589
# check_cp -rp $BASE_OS/performance/perfchart.cgi $BR_HOME/performance/htdocs/performance
rm -f $BR_HOME/performance/htdocs/performance/PerfChartsForms.pm

# The copies of dtree.css, dtree.js, and performance.css in play before the
# following file-copy actions take effect come from some very old cached version
# that BitRock includes in their build.  We must overlay those files with current
# checked-out copies from our Subversion repository, to bring them up-to-date.
check_cp -p $BASE_OS/performance/dtree.css       $BR_HOME/performance/htdocs/performance
check_cp -p $BASE_OS/performance/dtree.js        $BR_HOME/performance/htdocs/performance
check_cp -p $BASE_OS/performance/performance.css $BR_HOME/performance/htdocs/performance

# GWMON-11015:  Pull in the versions of certain files that we now consider to be the master copies.
check_cp -p $HOME/groundwork-professional/monitor-professional/performance/calendarDateInput.js $BR_HOME/performance/htdocs/performance
check_cp -p $HOME/groundwork-professional/monitor-professional/performance/PerfChartsForms.pm   $BR_HOME/performance/lib

# GWMON-5834
check_mkdir $GW_HOME/apache2/conf/groundwork

# GWMON-5841
# GWMON-11112
check_cp -rp $BASE/monitor-professional/profiles/plugins/*.pl          $BR_HOME/nagios/libexec
check_cp -rp $BASE/monitor-professional/profiles/plugins/*.pm          $BR_HOME/nagios/libexec
check_cp -rp $BASE/monitor-professional/profiles/plugins/*.sh          $BR_HOME/nagios/libexec
check_cp -rp $BASE/monitor-professional/profiles/plugins/com           $BR_HOME/nagios/libexec
check_cp -rp $BASE/monitor-professional/profiles/plugins/nagtomcat.jar $BR_HOME/nagios/libexec

# GWMON-6029
# GWMON-7102
# GWMON-7664
check_cp -rp $BASE/monitor-professional/profiles/plugins/data                               $BR_HOME/nagios/libexec
check_cp -rp /home/nagios/groundwork-professional/monitor-professional/profiles/plugins/lib $BR_HOME/nagios/libexec
check_cp -rp $BASE/monitor-professional/profiles/plugins/sql                                $BR_HOME/nagios/libexec
check_cp -rp $BASE/monitor-professional/profiles/plugins/check_oracle*                      $BR_HOME/nagios/libexec
check_cp -rp $BASE/monitor-professional/profiles/plugins/DbCheck.class                      $BR_HOME/nagios/libexec

# GWMON-11955:  This command used to be necessary, because these profile files were parked in Subversion
# in the monitor-professional/profiles/ directory instead of the monitor-professional/profiles/default/
# directory alongside all the other profile files.  Now that said situation has been fixed in Subversion,
# this special command should no longer be necessary.
# check_cp -rp $BASE/monitor-professional/profiles/*-jdbc-oracle.xml $BR_HOME/profiles

# GWMON-12665 -- add check_casperjs to the build
check_cp -rp $BASE/monitor-professional/profiles/plugins/check_casperjs $BR_HOME/nagios/libexec
check_chmod 755 $BR_HOME/nagios/libexec/check_casperjs/
check_chmod +x  $BR_HOME/nagios/libexec/check_casperjs/*.pl

# GWMON-5765
rm -rf $BR_HOME/profiles/service-profile-vmware_esx3_services_profile.xml

# GWMON-5861
if [ -f $BR_HOME/performance/htdocs/performance/images/logos ] ; then
  rm -f $BR_HOME/performance/htdocs/performance/images/logos
fi
check_mkdir -p $BR_HOME/performance/htdocs/performance/images/logos

# GWMON-5369
rm -rf $BR_HOME/reports/utils/utils

# GWMON-7849
check_mkdir /tmp/reports
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/reports/utils /tmp/reports
check_mv /tmp/reports/dashboard_avail_load.pl $BR_HOME/reports/utils
check_chmod 755     $BR_HOME/reports/utils/dashboard_avail_load.pl
chown nagios:nagios $BR_HOME/reports/utils/dashboard_avail_load.pl
rm -rf /tmp/reports

# GWMON-5976
check_chmod +x  $BASE/monitor-professional/monarch/bin/*
# GWMON-10188
check_chmod 744 $BASE/monitor-professional/monarch/bin/synchronized_sync.pl

check_cp -rp    $BASE/monitor-professional/monarch/lib/* $BR_HOME/monarch/lib
check_cp -rp    $BASE/monitor-professional/monarch/bin/* $BR_HOME/monarch/bin
######chmod +x  $BR_HOME/monarch/bin/nagios-foundation-sync.pl

# GWMON-5905
check_cp -rp $BASE/monitor-professional/migration/migrate-monarch.sql $BR_HOME/migration

# GWMON-5985
rm -rf $BR_HOME/foundation/feeder/nagios2master.pl

# GWMON-2600
check_cp -rp $BASE/monitor-professional/nagios/etc/nagios.cfg $BR_HOME/nagios/etc

# GWMON-11909
check_chmod 600 $BR_HOME/nagios/etc/resource.cfg

# GWMON-5984
check_chmod 755 $BR_HOME/profiles
check_chmod 644 $BR_HOME/profiles/README
check_chmod 755 $BR_HOME/profiles/*/
check_chmod 644 $BR_HOME/profiles/*/README
check_chmod 444 $BR_HOME/profiles/*.xml
check_chmod 444 $BR_HOME/profiles/*.gz
check_chmod 755 $BR_HOME/nagios/libexec
check_chmod +x $BR_HOME/nagios/libexec/*.sh
check_chmod +x $BR_HOME/nagios/libexec/*.pl

# GWMON-6066
check_chmod 640 $BR_HOME/core-config/*properties*

# GWMON-6842
rm -f $BR_HOME/nagios/libexec/*.c

# GWMON-6503
rm -f $BR_HOME/profiles/perfconfig-vmware_esx3_services_profile.xml

# GWMON-11915
# We now ship a service-profile-fping_feeder.xml instead.  The older service_profile_fping_feeder.xml file
# (note punctuation differences) comes from the BitRock build tarball, and is no longer appropriate to
# include.  So here we remove it from the location where the BitRock packager must have been instructed to
# pick it up.  Hopefully this won't break the build in the short run, although it seems to me that having
# a missing file ought to trigger a serious error and abort the build.  (Someday, we should simply tell
# BitRock to not include this file in the build in the first place.)
rm -f $BR_HOME/fping-feeder/usr/local/groundwork/core/profiles/service_profile_fping_feeder.xml

# GWMON-7144
check_cp -p $BASE_OS/performance-core/admin/import_perfconfig.pl $BR_HOME/tools/profile_scripts
check_chmod +x $BR_HOME/tools/profile_scripts/*.pl

# GWMON-7428
check_cp -p $BASE_OS/syslib/gwservices.ent $BR_HOME/services/gwservices
check_chmod +x $BR_HOME/services/gwservices

# OLD Foundation startup service.
# FIX MAJOR:  This has to go away.  I've only left it here temporarily in case it completely breaks
# our builds until the BitRock packager is configured to support the service-jpp service instead.
# check_cp -p $BASE_OS/syslib/foundation-webapp $BR_HOME/services/foundation/run
# chmod +x $BR_HOME/services/foundation/run

# NEW JPP startup service for GWMEE 7.0.2, mirroring Patch 1 for 7.0.1
# FIX MINOR:  We probably want to move where these scripts are checked into Subversion, so they're
# not buried deep inside some "custom-scripts" location that makes it look as though these are somehow
# related more to overriding JPP stuff than implementing essential GWMEE base-product functionality.
check_mkdir $BR_HOME/services/service-jpp
check_mkdir $BR_HOME/services/service-jpp/log
check_mkdir $BR_HOME/services/service-jpp/log/main
check_cp -p $BUILD_BASE/monitor-platform/jpp/portal-instance-base/custom_scripts/groundwork-services/service-jpp.run     $BR_HOME/services/service-jpp/run
check_cp -p $BUILD_BASE/monitor-platform/jpp/portal-instance-base/custom_scripts/groundwork-services/service-jpp.log.run $BR_HOME/services/service-jpp/log/run
check_chmod +x $BR_HOME/services/service-jpp/run
check_chmod +x $BR_HOME/services/service-jpp/log/run

# NEW perfdata processing scanner for GWMEE 7.1.0, to run in addition to the copy run by the Nagios launcher
check_mkdir $BR_HOME/services/scanner-perfdata
check_mkdir $BR_HOME/services/scanner-perfdata/log
check_mkdir $BR_HOME/services/scanner-perfdata/log/main
check_cp -p $BUILD_BASE/monitor-platform/jpp/portal-instance-base/custom_scripts/groundwork-services/scanner-perfdata.run     $BR_HOME/services/scanner-perfdata/run
check_cp -p $BUILD_BASE/monitor-platform/jpp/portal-instance-base/custom_scripts/groundwork-services/scanner-perfdata.log.run $BR_HOME/services/scanner-perfdata/log/run
check_chmod +x $BR_HOME/services/scanner-perfdata/run
check_chmod +x $BR_HOME/services/scanner-perfdata/log/run

# GWMON-11321, GWMON-11334
check_mkdir -p $BR_HOME/services/notification-noma/log/main

# GWMON-7440
# check_cp -p $BASE/monitor-framework/core/src/resources/portal-core-war/images/gwconnect.gif $BR_HOME/foundation/container/webapps/jboss/jboss-portal.sar/portal-core.war/images

# GWMON-7739
rm -f $BR_HOME/nagios/eventhandlers/process_service_perf.pl
rm -f $BR_HOME/nagios/eventhandlers/process_service_perf_db.pl

# GWMON-8544
check_cp -p $BASE_OS/performance-core/eventhandler/perfdata.properties.ee $BR_HOME/core-config/perfdata.properties

# GWMON-6937
rm -f $BR_HOME/core-config/migration.properties

# GWMON-6060
#check_cp -p $BASE/monitor-portal/applications/console/src/java/console-admin-config.xml $BR_HOME/core-config

check_cp -p $BASE/monitor-professional/bronx/conf/bronx.cfg $BR_HOME/core-config

# GWMON-13382
# Restrict the file permissions on bronx.cfg and send_nsca.cfg because they contain some sensitive info.
check_chmod 600 $BR_HOME/core-config/bronx.cfg
check_chmod 600 $BR_HOME/common/etc/send_nsca.cfg

## As of BitRock build br351 for GWMEE 7.1.0, we are no longer including the BitRock Network Service in our builds,
## so this copy action is now intentionally commented out (as the file is no longer available for copying).
# check_cp -p $BR_HOME/network-service/config/network-service.properties $BR_HOME/core-config

# GWMON-7771 -- Maintained by monitor-platform/gw-config project
#check_cp -p $BASE/monitor-portal/applications/console/src/java/console.properties $BR_HOME/core-config

# GWMON-7803
# FIX MAJOR:  This copying does not work, because the /home/build/build7/foundation/misc/web-application/reportserver/reports
# directory is empty (except for the .svn/ subdirectory).
# What to do about this?
#cp -p $FOUNDATION_ENTERPRISE_CHECKOUT/misc/web-application/reportserver/reports/StatusReports/* $BR_HOME/gwreports/StatusReports

# GWMON-12870:  BitRock currently delivers this directory with group-write permissions, so we lock it down a bit.
check_chmod 755 $BR_HOME/nagios/libexec/check_wmi_plus/etc/check_wmi_plus/check_wmi_plus.data

# Force process_foundation_db_update is on
Process_Foundation_Db_Update=$(grep process_foundation_db_update $BR_HOME/nagios/eventhandlers/process_service_perf_db_file.pl | awk '{ print $4; }' | sed 's/;//')
sed -i 's/process_foundation_db_updates = '$Process_Foundation_Db_Update'/process_foundation_db_updates = 1/' $BR_HOME/nagios/eventhandlers/process_service_perf_db_file.pl

Process_Foundation_Db_Update=$(grep process_foundation_db_update $BR_HOME/nagios/eventhandlers/process_service_perfdata_file | awk '{ print $4; }' | sed 's/;//')
sed -i 's/process_foundation_db_updates = '$Process_Foundation_Db_Update'/process_foundation_db_updates = 1/' $BR_HOME/nagios/eventhandlers/process_service_perfdata_file

sed -i 's/send_events_for_pending_to_ok = 0/send_events_for_pending_to_ok = 1/' $BR_HOME/foundation/feeder/nagios2collage_socket.pl

# GWMON-9398 -- Maintained by monitor-platform/gw-config project
#rm -f $BR_HOME/core-config/status-feeder.properties
# check_cp -fp $FOUNDATION_ENTERPRISE_CHECKOUT/collagefeeder/scripts/status-feeder.properties $BR_HOME/core-config/status-feeder.properties
# check_chdir $BR_HOME/core-config
# rm -f status-feeder.properties
# FIX MAJOR:  Change this reference to a permanent place for this file in the PRO repository, or drop this export
# entirely, now that we presumably got the script from the $FOUNDATION_ENTERPRISE_CHECKOUT earlier in this script.
#svn_export $OS_ARCHIVE/foundation/collagefeeder/scripts/status-feeder.properties
chown nagios:nagios $BR_HOME/core-config/status-feeder.properties

# Download postgres database driver
echo "Download postgres JDBC driver into installer package"

# check_chdir $BR_HOME/foundation/container/lib/jboss
# wget http://archive.groundworkopensource.com/maven/postgresql/jars/postgresql-9.1-901.jdbc3.jar
# chown nagios:nagios postgresql-9.1-901.jdbc3.jar


# GWMON-9386, GWMON-10100 -- fping feeder script update
echo "Make sure the latest fping feeder script and run script are included ..."
check_cp -fp $HOME/groundwork-professional/monitor-professional/fping/usr/local/groundwork/core/services/feeder-nagios-fping/run $BR_HOME/fping-feeder/usr/local/groundwork/core/services/feeder-nagios-fping/
check_cp -fp $HOME/groundwork-professional/monitor-professional/fping/usr/local/groundwork/foundation/feeder/fping_process.pl    $BR_HOME/fping-feeder/usr/local/groundwork/foundation/feeder/
check_cp -fp $HOME/groundwork-professional/monitor-professional/fping/usr/local/groundwork/foundation/feeder/fping_process.pl    $BR_HOME/fping-feeder/usr/local/groundwork/nagios/libexec/
check_cp -fp $HOME/groundwork-professional/monitor-professional/fping/usr/local/groundwork/config/fping_process.conf             $BR_HOME/fping-feeder/usr/local/groundwork/config/fping_process.conf
check_chmod 600 $BR_HOME/fping-feeder/usr/local/groundwork/config/fping_process.conf

# Spooler-gdma
check_cp -rp $HOME/groundwork-professional/monitor-agent/gdma/GDMA2.1/gdma-core/bin/gdma_spool_processor.pl $BR_HOME/groundwork-spooler/gdma/bin

check_cp -rp $HOME/groundwork-professional/monitor-spool/config/gdma_auto.conf      $BR_HOME/groundwork-spooler/gdma/config
check_cp -rp $HOME/groundwork-professional/monitor-spool/config/gwmon_localhost.cfg $BR_HOME/groundwork-spooler/gdma/config
rm -rf  $BR_HOME/groundwork-spooler/spooler-gdma
check_cp -rp $HOME/groundwork-professional/monitor-spool/spooler-gdma $BR_HOME/groundwork-spooler

# GWMON-11188:  Create directories to contain additional categories of Nagios-related plugins.
# These are currently just placeholders; in the future, we expect to populate the groundwork/ and
# community/ directories to some extent, leaving the customer/ tree for on-site customer use.
check_mkdir $BR_HOME/plugins/groundwork
check_mkdir $BR_HOME/plugins/community
check_mkdir $BR_HOME/plugins/customer

# GWMON-12120:  Repackage certain war-files so they don't contain world-writable permission bits.
# Testing shows this has no beneficial effect on suppressing the unwanted permissions which are visible
# when the war-files are deployed.  But it seems to do no harm either, so we just leave it in place.
clean_warfile_permissions /usr/local/groundwork/jpp/standalone/deployments/nagvis.war
clean_warfile_permissions /usr/local/groundwork/jpp/standalone/deployments/nms-rstools.war

# GWMON-12120:  Drop world-writable permissions on python/... files.
check_chmod -R o-w $BR_HOME/python

# GWMON-12120:  Drop world-writable and execute permissions on all licenses/... files,
# since a number of them come with loose control over the permissions.
check_chmod -R go-w $BR_HOME/licenses
find $BR_HOME/licenses -type f -exec chmod a-x '{}' \;

# GWMON-12599:  Clean up CR characters that ought not to be there in Perl packages.
dos2unix $BR_HOME/perl/lib/5.24.0/Pod/Checker.pm
dos2unix $BR_HOME/perl/lib/vendor_perl/5.24.0/Net/Ifconfig/Wrapper.pm
dos2unix $BR_HOME/perl/lib/vendor_perl/5.24.0/Net/Daemon/Log.pm
dos2unix $BR_HOME/perl/lib/vendor_perl/5.24.0/Net/Daemon/Test.pm
dos2unix $BR_HOME/perl/lib/vendor_perl/5.24.0/Object/MultiType.pm
dos2unix $BR_HOME/perl/lib/vendor_perl/5.24.0/XML/Smart/FAQ.epod
dos2unix $BR_HOME/perl/lib/vendor_perl/5.24.0/XML/Smart/Base64.pm
dos2unix $BR_HOME/perl/lib/vendor_perl/5.24.0/XML/Smart/DTD.pm
dos2unix $BR_HOME/perl/lib/vendor_perl/5.24.0/XML/Smart/Tutorial.epod
dos2unix $BR_HOME/perl/lib/vendor_perl/5.24.0/Class/Generate.pod
dos2unix $BR_HOME/perl/lib/vendor_perl/5.24.0/OLE/Storage_Lite.pm
dos2unix $BR_HOME/perl/lib/vendor_perl/5.24.0/Math/Round.pm
dos2unix $BR_HOME/perl/lib/vendor_perl/5.24.0/Date/Language/Danish.pm
dos2unix $BR_HOME/perl/lib/vendor_perl/5.24.0/x86_64-linux-thread-multi/Devel/NYTProf/js/jit/jit.js
dos2unix $BR_HOME/perl/lib/vendor_perl/5.24.0/Nagios/Monitoring/Plugin/Getopt.pm
dos2unix $BR_HOME/perl/lib/vendor_perl/5.24.0/Nagios/Monitoring/Plugin/Functions.pm
dos2unix $BR_HOME/perl/lib/vendor_perl/5.24.0/Nagios/Monitoring/Plugin/Config.pm
dos2unix $BR_HOME/perl/lib/vendor_perl/5.24.0/Nagios/Monitoring/Plugin/Threshold.pm
dos2unix $BR_HOME/perl/lib/vendor_perl/5.24.0/Nagios/Monitoring/Plugin/Performance.pm
dos2unix $BR_HOME/perl/lib/vendor_perl/5.24.0/Nagios/Monitoring/Plugin/ExitResult.pm
dos2unix $BR_HOME/perl/lib/vendor_perl/5.24.0/Nagios/Monitoring/Plugin/Range.pm
dos2unix $BR_HOME/perl/lib/vendor_perl/5.24.0/Nagios/Monitoring/Plugin.pm


# Setting up the right permissions
find $BR_HOME -name perfchart.cgi -exec chmod +x {} \;

# Clean up and set ownership
find $BR_HOME -name .svn -exec rm -rf {} \;
chown -R nagios:nagios $BR_HOME

# GWMON-13411:  Change the ownership of some copy of the groundwork-fping file in hopes that
# it will translate over to the finaly deployed copy in the /etc/logrotate.d/ directory.
#
# (This command doesn't achieve the desired final effect.)
# chown root:root $BR_HOME/fping-feeder/etc/logrotate.d/groundwork-fping
#
# (This command doesn't achieve the desired final effect.)
# chown root:root /root/build/BitRock/groundwork.back/fping-feeder/etc/logrotate.d/groundwork-fping
#
# (This command doesn't achieve the desired final effect.)
# chown root:root /root/build/BitRock/groundwork/fping-feeder/etc/logrotate.d/groundwork-fping

echo "Remove JBoss Community directory from the installer"
rm -rf  $BR_HOME/foundation/container/contexts
rm -rf  $BR_HOME/foundation/container/data
rm -rf  $BR_HOME/foundation/container/etc
rm -rf  $BR_HOME/foundation/container/lib
rm -rf  $BR_HOME/foundation/container/webapps
rm -f  $BR_HOME/foundation/container/*.*
#check_mkdir $BR_HOME/foundation/container/logs
#touch $BR_HOME/foundation/container/logs/README

######################################################
# Portal framework deployment ..
######################################################
echo "Copy JPP into the Bitrock Install Builder"
check_cp -r /usr/local/groundwork/jpp $BR_HOME/foundation/container
check_chmod 750 $BR_HOME/foundation/container/jpp/bin/standalone.sh
check_chmod 750 $BR_HOME/foundation/container/jpp/bin/jboss-cli.sh

######################################################
# favicon for login page
######################################################
check_cp -p $HOME/groundwork-professional/monitor-professional/favicon.ico $BR_HOME/apache2/htdocs

####################################################
# Josso server deployment
####################################################
echo "Copy tomcat with JOSSO into the Bitrock Install Builder"
check_cp -r /usr/local/groundwork/josso-1.8.4 $BR_HOME/foundation/container
check_chmod +x $BR_HOME/foundation/container/josso-1.8.4/bin/startup.sh

#######################################################################
# Josso security for performance, monarch-export, reports and profiles
#######################################################################

echo "Copy JOSSO configurations for perl scripts ..."
# check_cp -p $HOME/groundwork-professional/monitor-professional/performance/login-redirect.jsp $BR_HOME/performance
# FIX MAJOR:  This directory creation failed because the /home/build/BitRock/groundwork/performance/WEB-INF
# directory already existed.
# As a workaround for the time being, we have applied the -p flag to suppress errors if it already exists.
check_mkdir -p $BR_HOME/performance/WEB-INF
check_cp -p $HOME/groundwork-professional/monitor-professional/performance/WEB-INF/*.xml $BR_HOME/performance/WEB-INF

# check_cp -p $HOME/groundwork-professional/monitor-professional/profiles/login-redirect.jsp $BR_HOME/profiles
check_mkdir $BR_HOME/profiles/WEB-INF
check_cp -p $HOME/groundwork-professional/monitor-professional/profiles/WEB-INF/*.xml $BR_HOME/profiles/WEB-INF

# check_cp -p $HOME/groundwork-professional/monitor-professional/reports/login-redirect.jsp $BR_HOME/reports
# FIX MAJOR:  This directory creation failed because the /home/build/BitRock/groundwork/reports/WEB-INF
# directory already existed.
# As a workaround for the time being, we have applied the -p flag to suppress errors if it already exists.
check_mkdir -p $BR_HOME/reports/WEB-INF
check_cp -p $HOME/groundwork-professional/monitor-professional/reports/WEB-INF/*.xml $BR_HOME/reports/WEB-INF

# check_cp -p $HOME/groundwork-professional/monitor-professional/monarch-export/login-redirect.jsp $BR_HOME/monarch/htdocs/monarch/download/
# FIX MAJOR:  This directory creation failed because the /home/build/BitRock/groundwork/monarch/htdocs/monarch/download/WEB-INF
# directory already existed.
# As a workaround for the time being, we have applied the -p flag to suppress errors if it already exists.
check_mkdir -p $BR_HOME/monarch/htdocs/monarch/download/WEB-INF
check_cp -p $HOME/groundwork-professional/monitor-professional/monarch-export/WEB-INF/*.xml $BR_HOME/monarch/htdocs/monarch/download/WEB-INF

#
# Copy tomcat and JBoss AS 7 agents into GroundWork deployment
#
echo "JDMA needs to be re-written before it can be included in the standard deployment. For GWME 7.1 JDMA won't be deployed for monitoring the portal"
#echo " Deploy JBOSS JDMA to monitor GroundWork instance..."
#check_cp -p $BASE/monitor-agent/gdma/JDMA/appserver/jboss-AS7/target/gwos-jbossas7-monitoringAgent.war $BR_HOME/foundation/container/jpp/standalone/deployments/
#check_cp -p $BASE/monitor-agent/gdma/JDMA/appserver/tomcat/target/gwos-tomcat-monitoringAgent.war      $BR_HOME/foundation/container/josso-1.8.4/webapps/

# Getting RSTool command tool set
echo "Copy RSTools command utility into the installer environment..."
rm -rf $BR_HOME/foundation/container/rstools
check_cp -rp $BUILD_BASE/monitor-platform/monitor-apps/rstools/target/rstools $BR_HOME/foundation/container

echo "Placeholder for bookshelf"
check_mkdir $BR_HOME/bookshelf/docs
check_mkdir $BR_HOME/bookshelf/docs/bookshelf-data

# FIX MINOR:  This directory already exists from previous scripting, so why do we think it's necessary to make it here?
# As a workaround for the time being, we have applied the -p flag to suppress errors if it already exists.
check_mkdir -p $BR_HOME/core-config/jboss

echo "cacti feeder supervise folders"
check_mkdir $BR_HOME/services/feeder-cacti
check_mkdir $BR_HOME/services/feeder-cacti/log
check_mkdir $BR_HOME/services/feeder-cacti/log/main

check_cp -p $BASE_OS/syslib/feeder-cacti.run     $BR_HOME/services/feeder-cacti/run
check_cp -p $BASE_OS/syslib/feeder-cacti.log.run $BR_HOME/services/feeder-cacti/log/run

check_chmod -R 755 $BR_HOME/services/feeder-cacti

echo "nedi feeder supervise folders"
check_mkdir $BR_HOME/services/feeder-nedi
check_mkdir $BR_HOME/services/feeder-nedi/log
check_mkdir $BR_HOME/services/feeder-nedi/log/main

check_cp -p $BASE_OS/syslib/feeder-nedi.run     $BR_HOME/services/feeder-nedi/run
check_cp -p $BASE_OS/syslib/feeder-nedi.log.run $BR_HOME/services/feeder-nedi/log/run

check_chmod -R 755 $BR_HOME/services/feeder-nedi

echo "nedi monitor supervise folders"
check_mkdir $BR_HOME/services/monitor-nedi
check_mkdir $BR_HOME/services/monitor-nedi/log
check_mkdir $BR_HOME/services/monitor-nedi/log/main

check_cp -p $BASE_OS/syslib/monitor-nedi.run     $BR_HOME/services/monitor-nedi/run
check_cp -p $BASE_OS/syslib/monitor-nedi.log.run $BR_HOME/services/monitor-nedi/log/run

# Adding latest Nagios ctl script into release (GWMON-11403, GWMON-11852)
echo "Adding Nagios ctl script to build ..."
check_cp $HOME/groundwork-professional/patch-scripts/BitRock/nagios-3.5.1/ctl.sh $BR_HOME/nagios/scripts

# Adding schema-template-NeDi-host-import.xml for GWMON-12683
check_cp $HOME/groundwork-monitor/monarch/automation/templates/schema-template-NeDi-host-import.xml $BR_HOME/monarch/automation/templates

date
echo "Monitor-pro build is done at `date`"
##########################################

echo "EntBuild.sh is done..."


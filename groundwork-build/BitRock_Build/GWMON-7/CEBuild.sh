#!/bin/bash -x
#
# Copyright (C) 2014-2017 GroundWork Inc. info@groundworkopensource.com
#

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

newline='
'

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
  echo "Please set build directory in buildRPM.sh file..."
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
export LDFLAGS=-L$GW_HOME/$libdir
export LD_RUN_PATH=$GW_HOME/$libdir:$LD_RUN_PATH
export LD_LIBRARY_PATH=$GW_HOME/$libdir:$LD_LIBRARY_PATH
export CPPFLAGS=-I$GW_HOME/include
export RELEASE=$distro$ARCH
export DATE=$(date +%Y-%m-%d)
export Build4=$1

export PATH=$JAVA_HOME/bin:$GW_HOME/bin:$PATH:$MAVEN_HOME/bin:$ANT_HOME/bin

HOME=/home/build
BASE=$HOME/groundwork-monitor
BASE_OS=$BASE/monitor-os
BASE_CORE=$BASE/monitor-core
BUILD_DIR=$BASE/build
RUN_DIR=/home/build
MoratDir=/var/www/html/tools/DEVELOPMENT

# "cd", "mkdir", "cp", "scp", and "tar" are critical operations whose failure we want to be noticed and abort the build script.

check_chdir () {
    dir="$1"
    if ! cd $dir ; then
	echo "BUILD FAILED: There was an error trying to change to $dir as the current working directory." | mail -s "GWMON-$GWMEE_VERSION CE Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
	exit 1
    fi
}

# Note that you can use the -p option if you want no error if the directory already exists,
# but (presumably) still an error if some other difficulty arises (such as no permission).
check_mkdir () {
    # We have to allow for more than one argument to this function.
    # For printing purposes, we capture the last argument in a clearly labeled variable.
    dir="${!#}"
    if ! mkdir "$@"; then
	echo "BUILD FAILED: There was an error trying to create the $dir directory." | mail -s "GWMON-$GWMEE_VERSION CE Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
	exit 1
    fi
}

check_cp () {
    # We have to allow for more than one argument to this function.
    # For printing purposes, we capture the last argument in a clearly labeled variable.
    target="${!#}"
    if ! /bin/cp "$@"; then
	echo "BUILD FAILED: There was an error trying to copy to the $target location." | mail -s "GWMON-$GWMEE_VERSION CE Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
	exit 1
    fi
}

# Drop leading options and the final target; keep only intermediate source arguments.
extract_sources() {
    while [[ $1 =~ '^-.*' ]]
    do
        # got an option, not a source; drop it 
        shift
    done    
    sources=("$@")
    (( last = ${#sources[@]} - 1 ))
    unset sources[$last]
    echo "${sources[@]}"
}

check_scp () {
    # We have to allow for more than one argument to this function.
    # For printing purposes, we capture the scp source arguments in a clearly labeled variable.
    scp_sources=( $(extract_sources "$@") ) 
    if ! /usr/bin/scp "$@"; then
	echo "BUILD FAILED: There was an error trying to remotely copy from the following location(s):${newline}    ${scp_sources[@]}" | mail -s "GWMON-$GWMEE_VERSION CE Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
	exit 1
    fi
}

# We assume here for logging purposes that the tarfile name is the last argument.
check_tar () {
    # We have to allow for more than one argument to this function.
    # For printing purposes, we capture the last argument in a clearly labeled variable.
    tarfile="${!#}"
    if ! /bin/tar "$@"; then
	echo "BUILD FAILED: There was an error trying to unroll the $tarfile tarball." | mail -s "GWMON-$GWMEE_VERSION CE Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
	exit 1
    fi
}

# Clean up previous builds
rm -rf $BR_HOME
rm -rf $GW_HOME
rm -rf $BASE

check_chdir $RUN_DIR/BitRock
if [ "$Build4" == "pro" ] ; then
  GWMON=groundwork-pro
  check_cp -rp /usr/local/groundwork-common.ent /usr/local/groundwork
elif [ "$Build4" == "" ] ; then
  GWMON=groundwork
  check_cp -rp /usr/local/groundwork-common.ce /usr/local/groundwork
else
  echo "Wrong argument ..."
  exit -1
fi

# Clean, then unpack BitRock package
rm -rf $RUN_DIR/BitRock/$GWMON-output-linux-*.tar.gz
check_chdir $RUN_DIR/BitRock
if [ "$arch" == "x86_64" ] ; then
  check_scp -rp root@morat:/var/www/html/builds/BitRock/$GWMEE_FULL_VERSION/$GWMON-output-linux-x64-201*.tar.gz .
  check_tar zxf $RUN_DIR/BitRock/$GWMON-output-linux-x64-201*.tar.gz
else
  check_scp -rp root@morat:/var/www/html/builds/BitRock/$GWMEE_FULL_VERSION/$GWMON-output-linux-201*.tar.gz .
  check_tar zxf $RUN_DIR/BitRock/$GWMON-output-linux-201*.tar.gz
fi

# Network service portlet is no longer standalone
# check_cp -p $BR_HOME/foundation/container/webapps/jboss/jboss-portal.sar/portal-network-service.war /tmp

# Update Bookshelf build
bsh_new_release=`cat /usr/local/groundwork/bookshelf_release.txt`
grep -v bookshelf $BR_HOME/build.properties > $BR_HOME/build.properties.tmp
echo bookshelf=$GWMEE_FULL_VERSION-$bsh_new_release >> $BR_HOME/build.properties.tmp
mv -f $BR_HOME/build.properties.tmp $BR_HOME/build.properties

cat $BR_HOME/groundwork-bookshelf.xml | sed 's/name="bookshelf_build" value="/name="bookshelf_build" value="$GWMEE_FULL_VERSION-'$bsh_new_release'/' > $BR_HOME/groundwork-bookshelf.xml.tmp
mv -f $BR_HOME/groundwork-bookshelf.xml.tmp $BR_HOME/groundwork-bookshelf.xml

if ! [ -d $BR_HOME/bookshelf ] ; then
    check_mkdir $BR_HOME/bookshelf
fi

BookshelfHome=$BR_HOME/bookshelf/htdocs
rm -rf $BR_HOME/bookshelf/docs
check_mkdir -p $BookshelfHome

# FIX MAJOR:  This copying no longer works, because /usr/local/groundwork/docs does not exist.
# What to do about this?
cp -rp /usr/local/groundwork/docs $BR_HOME/bookshelf

# FIX MAJOR:  This copying no longer works, because /usr/local/groundwork/docs does not exist.
# What to do about this?
cp -rp /usr/local/groundwork/docs $BookshelfHome

# FIX MAJOR:  This copying no longer works, because /usr/local/groundwork/guava/packages/bookshelf does not exist.
# What to do about this?
cp -rp /usr/local/groundwork/guava/packages/bookshelf $BookshelfHome

# FIX MAJOR:  This copying no longer works, because /usr/local/groundwork/migration/gw-bookshelf-install.php does not exist.
# What to do about this?
cp -rp /usr/local/groundwork/migration/gw-bookshelf-install.php $BookshelfHome


# Update Foundation build
fou_new_release=`cat /usr/local/groundwork/foundation_release.txt`
grep -v foundation $BR_HOME/build.properties > $BR_HOME/build.properties.tmp
echo foundation=$GWMEE_FULL_VERSION >> $BR_HOME/build.properties.tmp
mv -f $BR_HOME/build.properties.tmp $BR_HOME/build.properties

cat $BR_HOME/groundwork-foundation.xml | sed 's/name="foundation_build" value="/name="foundation_build" value="3.0-'$fou_new_release'/' > $BR_HOME/groundwork-foundation.xml.tmp
mv -f $BR_HOME/groundwork-foundation.xml.tmp $BR_HOME/groundwork-foundation.xml

# Checkout function
svn_co () {
    for i in 1 0; do
	svn co $1 $2 $3 $4 $5 $6
	SVN_EXIT_CODE=$?
	if [ $SVN_EXIT_CODE -eq 0 ]; then
	    break;
	elif [ $i -eq 0 ]; then
	    echo "BUILD FAILED: There has been a problem trying to checkout groundwork files." | mail -s "GWMON-$GWMEE_VERSION CE Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
	    exit 1
	fi
	sleep 30
    done
	
}

# SVN update function
svn_update () {
    for i in 1 0; do
	svn update $1 $2 $3 $4 $5 $6
	SVN_EXIT_CODE=$?
	if [ $SVN_EXIT_CODE -eq 0 ]; then
	    break;
	elif [ $i -eq 0 ]; then
	    echo "BUILD FAILED: There has been a problem trying to update groundwork files." | mail -s "GWMON-$GWMEE_VERSION CE Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
	    exit 1
	fi
	sleep 30
    done
	
}

# SVN commit function
svn_commit () {
    for i in 1 0; do
        svn commit $1 $2 $3 $4 $5 $6 $7
        SVN_EXIT_CODE=$?
        if [ $SVN_EXIT_CODE -eq 0 ]; then
            break;
        elif [ $i -eq 0 ]; then
            echo "BUILD FAILED: There has been a problem trying to commit groundwork files." | mail -s "GWMON-$GWMEE_VERSION CE Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
            exit 1
        fi
        sleep 30
    done

}


# Build monitor-os
check_chdir $HOME

PRO_ARCHIVE=http://geneva/groundwork-professional/$PRO_ARCHIVE_BRANCH

# FIX MAJOR:  How is it that we are able to check out code from the
# PRO repository without supplying any credentials?

# Check out the top-level Maven-related files that used to live at the top level of our OS repository.
svn_co -N $PRO_ARCHIVE/archive-groundwork-monitor groundwork-monitor

check_chdir $BASE
svn_co $PRO_ARCHIVE/monitor-core/bronx 
svn_co $PRO_ARCHIVE/build
svn_co $PRO_ARCHIVE/images 
svn_co $PRO_ARCHIVE/monarch 
svn_co $PRO_ARCHIVE/monitor-core 
svn_co $PRO_ARCHIVE/monitor-os 
svn_co $PRO_ARCHIVE/reports

# Checkout necessary files from PRO Foundation repo
check_mkdir $BASE/foundation
#check_mkdir $BASE/foundation/collagefeeder
#check_mkdir $BASE/foundation/collage
#check_mkdir $BASE/foundation/collage/api
check_mkdir $BASE/foundation/misc/
check_mkdir $BASE/foundation/misc/web-application
check_chdir $BASE/foundation/misc/web-application
svn_co $PRO_ARCHIVE/monitor-platform/enterprise-foundation/misc/web-application/jboss

# Checkout Foundation resources
check_chdir $BASE/foundation/
svn_co $PRO_ARCHIVE/monitor-platform/enterprise-foundation/resources

# Checkout scripts
#check_chdir $BASE/foundation/collagefeeder
#svn_co $PRO_ARCHIVE/monitor-platform/enterprise-foundation/collagefeeder/scripts

#check_chdir $BASE/foundation/collage/api
#svn_co $PRO_ARCHIVE/monitor-platform/enterprise-foundation/collage/api/php
#svn_co $PRO_ARCHIVE/monitor-platform/enterprise-foundation/collage/api/Perl 

# GWMON-7428
check_cp -p $BASE_OS/syslib/gwservices.ce $BASE_OS/syslib/gwservices

# Update Foundation
rm -rf $BR_HOME/foundation
check_cp -rp $GW_HOME/foundation $BR_HOME

# Update core-config
check_cp -rp $GW_HOME/config/* $BR_HOME/core-config
#check_cp -p  $BASE_OS/resources/db.properties $BR_HOME/core-config/db.properties.os

# FIX MAJOR:  Using check_cp for this, it fails.  Do we still need this?
#cp -p  $GW_HOME/foundation/misc/web-application/reportserver/resources/viewer.properties $BR_HOME/core-config/viewer.properties

rm -rf $BR_HOME/config


mv $BR_HOME/apache2 $BR_HOME/apache2.bitrock
mv $BR_HOME/monarch/bin $BR_HOME/monarch/bin.bitrock
mv $BR_HOME/services $BR_HOME/services.bitrock
ln -s $BR_HOME/common/bin $BR_HOME/bin

# FIX MAJOR:  The mucking around in this block of scripting probably doesn't apply
# any more to our GWMEE builds.  Quite probably, these lines should simply be dropped,
# unless we can find where the $BUILD_DIR/master-build-os.sh script is invoked during
# our current builds.
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

check_cp $BASE_OS/maven-sb.xml $BASE/maven.xml

# FIX MAJOR:  This mucking around with the master build script probably doesn't apply
# any more to our GWMEE builds.  Quite probably, these lines should simply be dropped,
# unless we can find where the $BUILD_DIR/master-build-os.sh script is invoked during
# our current builds.
/bin/rm -rf $BUILD_DIR/master-build.sh
check_cp $BASE_OS/build/master-build.sh $BUILD_DIR/master-build-os.sh

echo "Comment out Build number generator in CE BUILD. Done in nightlyBuildEE script"

# Increment core-build number
#rm -f $BASE_OS/project.properties
#svn_update $BASE_OS/project.properties
#sleep 5

#br_release=$(fgrep 6.1- $BR_HOME/project.xml | sed -e 's/6.1-/6.1 /' | sed -e 's/>/ /g' | sed -e 's/</ /g' | awk '{ print $3; }')
#br_release=$(fgrep "<version>" $BR_HOME/project.xml | sed -e 's/.*-br\([0-9]*\)-.*/\1/')

#release=$(fgrep "org.groundwork.rpm.$RELEASE.release.number" $BASE_OS/project.properties |awk '{ print $3; }')

#if [ "$release" == "" ]; then
#    echo "BUILD FAILED: There was an error trying to get the version number from the project.properties file. Please check the $BASE_OS/project.properties file." | mail -s "GWMON-$GWMEE_VERSION CE Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
#    exit 1
#fi

#if [ "$Build4" == "pro" ] ; then
#  new_release=`expr $release + 1`
#else
#  new_release=$release
#fi

#old_release=$(fgrep "org.groundwork.rpm.release.number" $BASE_OS/project.properties |awk '{ print $3; }')

# Set new core-build release number
#sed -e 's/org.groundwork.rpm.release.number = '$old_release'/org.groundwork.rpm.release.number = '$new_release'/' $BASE_OS/project.properties >  $BASE_OS/project.properties.tmp1
#mv  $BASE_OS/project.properties.tmp1  $BASE_OS/project.properties

#sed -e 's/org.groundwork.rpm.'$RELEASE'.release.number = '$release'/org.groundwork.rpm.'$RELEASE'.release.number = '$new_release'/' $BASE_OS/project.properties >  $BASE_OS/project.properties.tmp2
#mv -f $BASE_OS/project.properties.tmp2  $BASE_OS/project.properties

# Commit project.properties back to subversion
#echo "Increment build(release) number" > svnmessage
#if [ "$Build4" == "pro" ] ; then
#  svn update --force $SVN_CREDENTIALS $BASE_OS/project.properties
#  sleep 5
#  svn commit $SVN_CREDENTIALS $BASE_OS/project.properties -F svnmessage
#fi
#rm -rf svnmessage

# Added distro to pro release name
#sed -e 's/org.groundwork.rpm.release.number = '$new_release'/org.groundwork.rpm.release.number = '$new_release'.'$RELEASE'/' $BASE_OS/project.properties > $BASE_OS/project.properties.tmp
#mv $BASE_OS/project.properties.tmp  $BASE_OS/project.properties

# Updated package name
# Set new package release number
#sed -e 's/6.1-'$br_release'/6.1-br'$br_release'-gw'$new_release'/' $BR_HOME/project.xml > $BR_HOME/project.xml.tmp2
#sed -e 's/gwXXX/gw'$new_release'/' $BR_HOME/project.xml > $BR_HOME/project.xml.tmp2
#mv -f $BR_HOME/project.xml.tmp2 $BR_HOME/project.xml

# Updated project.properties for BitRock
#sed 's/\/usr\/local\/groundwork/\/home\/build\/BitRock\/groundwork/g' $BASE_OS/project.properties > $BASE_OS/project.properties.tmp
#mv -f $BASE_OS/project.properties.tmp $BASE_OS/project.properties
#sed 's/\/usr\/local\/groundwork/\/home\/build\/BitRock\/groundwork/g' $BASE_CORE/project.properties > $BASE_CORE/project.properties.tmp
#mv -f $BASE_CORE/project.properties.tmp $BASE_CORE/project.properties
#sed 's/\/usr\/local\/groundwork/\/home\/build\/BitRock\/groundwork/g' $BASE/project.properties > $BASE/project.properties.tmp
#mv -f $BASE/project.properties.tmp $BASE/project.properties
PWD=`pwd`

# Update build properties
#grep -v core $BR_HOME/build.properties > $BR_HOME/build.properties.tmp
#echo core=$GWMEE_FULL_VERSION-br$br_release-gw$new_release >> $BR_HOME/build.properties.tmp
#mv -f $BR_HOME/build.properties.tmp $BR_HOME/build.properties

#cat $BR_HOME/groundwork-core.xml | sed 's/name="core_build" value="/name="core_build" value="$GWMEE_FULL_VERSION-br'$br_release'-gw'$new_release'/' > $BR_HOME/groundwork-core.xml.tmp
#mv -f $BR_HOME/groundwork-core.xml.tmp $BR_HOME/groundwork-core.xml

# Patch for x86_64 arch
#if [ "$arch" == "x86_64" ] ; then
#  check_chdir $BUILD_DIR/build-x86_64
#  . install64bitPatch.sh
#fi

# Run dos2unix for all the files under profiles directory
chmod +x $BUILD_DIR/d2unix.pl
$BUILD_DIR/d2unix.pl $BASE_OS/profiles

export PERLCC=gcc
echo "Default Perl compiler: ($PERLCC)"

# Start master build script
check_chdir $BASE
maven allBuild
maven allDeploy

check_cp -rp $GW_HOME/config/* $BR_HOME/core-config

#*******************************
if ! [ -d $BR_HOME/monarch/cgi-bin/monarch ] ; then
    check_mkdir -p $BR_HOME/monarch/cgi-bin/monarch
fi
if ! [ -d $BR_HOME/reports/cgi-bin/reports ] ; then
    check_mkdir -p $BR_HOME/reports/cgi-bin/reports
fi
check_cp -rp $BASE/monarch/*.cgi $BR_HOME/monarch/cgi-bin/monarch

# FIX MAJOR:  This copying no longer works, because /home/build/BitRock/groundwork/apache2/cgi-bin/reports/* does not exist.
# What to do about this?
cp -rp $BR_HOME/apache2/cgi-bin/reports/* $BR_HOME/reports/cgi-bin/reports

# FIX MAJOR:  This copying no longer works, because /home/build/BitRock/groundwork/apache2/htdocs/monarch/* does not exist.
# What to do about this?
cp -rp $BR_HOME/apache2/htdocs/monarch/* $BR_HOME/monarch/htdocs/monarch

# FIX MAJOR:  This copying no longer works, because /home/build/BitRock/groundwork/apache2/htdocs/reports/images/* does not exist.
# What to do about this?
cp -rp $BR_HOME/apache2/htdocs/reports/images/* $BR_HOME/reports/htdocs/reports/images

rm -rf $BR_HOME/apache2
mv $BR_HOME/apache2.bitrock $BR_HOME/apache2
check_mkdir $BR_HOME/apache2/conf/groundwork
#*******************************

### REMOVE MYSQL databases
##******************************
#check_cp -rp $BASE_OS/database/create-monitor-sb-db.sql $BR_HOME/databases
#check_cp -rp $GW_HOME/foundation/database/foundation-base-data.sql $BR_HOME/databases
##******************************

check_cp -p $BASE/reports/perl/gwir.cfg $BR_HOME/reports/etc/gwir.cfg
check_cp -p $BASE/reports/utils/*.pl $BR_HOME/reports/utils
#check_cp -p $BASE/reports/utils/dashboard_nagios_create.sql $BR_HOME/databases

# FIX LATER:  I'm not sure why we keep two copies of these Insight Reports scripts around;
# that may be for historical reasons, never completely cleaned up.  We really only need
# the copies in the $BR_HOME/reports/cgi-bin/reports/ directory.
check_cp -p $BASE/reports/perl/*.pl $BR_HOME/reports/utils/perl
check_cp -p $BASE/reports/perl/*.pl $BR_HOME/reports/cgi-bin/reports

rm -rf $BR_HOME/etc

###*****************************
rm -rf $BR_HOME/guava
check_cp -rp $BASE_CORE/apache/php.ini $BR_HOME/php/etc
###*****************************

check_cp -rp $BASE_OS/migration/*         $BR_HOME/migration
check_cp -rp $BASE/monarch/migration/*.pl $BR_HOME/migration
#check_cp -rp $BASE/monarch/database/monarch.sql $BR_HOME/databases
check_cp -rp $BASE/monarch/*.css      $BR_HOME/monarch/htdocs/monarch
check_cp -rp $BASE/monarch/*.js       $BR_HOME/monarch/htdocs/monarch
check_cp -rp $BASE/monarch/cover.html $BR_HOME/monarch/htdocs/monarch
check_cp -rp $BASE/monarch/*.pm       $BR_HOME/monarch/lib
check_cp -rp $BASE/monarch/standalone $BR_HOME/monarch/lib
mv -f $BR_HOME/foundation/database/migrate-gwcollagedb.sql $BR_HOME/migration

####****************************
if ! [ -d monarch/csvimport ] ; then
    check_mkdir -p $BR_HOME/monarch/csvimport
fi
check_cp -rp $BASE/monarch/standalone $BR_HOME/monarch/cgi-bin/monarch/lib
check_cp -rp $BASE_OS/profiles/automation/conf/* $BR_HOME/monarch/automation/conf
mv -p $BR_HOME/monarch/bin/*.pl $BR_HOME/monarch/bin.bitrock
rm -rf $BR_HOME/monarch/bin
mv $BR_HOME/monarch/bin.bitrock $BR_HOME/monarch/bin

#
# Monarch scripts need to be updated to the latest
#
check_cp -p $BASE/monarch/monarch_as_nagios.pl $BR_HOME/monarch/bin
check_cp -p $BASE/monarch/nmap_scan_one.pl $BR_HOME/monarch/bin

# Adjust the permissions
chmod 755 $BR_HOME/monarch/bin/monarch_as_nagios.pl
chmod 755 $BR_HOME/monarch/bin/nmap_scan_one.pl

####****************************

check_cp -rp $BASE_OS/resources/my.cnf $BR_HOME/mysql

check_cp -rp $BASE_CORE/nagios/plugins-gwcustom/* $BR_HOME/nagios/libexec
check_cp -rp $BASE_CORE/nagios/etc/* $BR_HOME/nagios/etc

# Replace the old copy of pwgen.pl we get from BitRock with the copy in our Subversion.
check_cp -rp $BASE_CORE/nagios/pwgen.pl $BR_HOME/common/etc 

# Installer images
check_cp -rp $BASE/images/installer/* $BR_HOME/images

check_chdir $BR_HOME/common/lib/openradius
rm -rf radclient
ln -s ../../bin/radclient .
check_chdir $PWD

# FIX MAJOR:  This copying no longer works, because /home/build/BitRock/groundwork/services/* does not exist.
# What to do about this?
cp -rp $BR_HOME/services/* $BR_HOME/services.bitrock

rm -rf $BR_HOME/services
mv $BR_HOME/services.bitrock $BR_HOME/services

rm -rf $BR_HOME/share

check_cp -p $BASE_OS/performance-core/eventhandler/launch_perf_data_processing $BR_HOME/nagios/eventhandlers

check_cp -rp $BASE_CORE/nagios/performance/cgi/* $BR_HOME/performance/cgi-bin/graphs
check_cp -p $BASE_OS/performance/perfchart.cgi $BR_HOME/performance/cgi-bin/performance
check_cp -p $BASE_OS/performance-core/admin/PerfConfigAdmin.pl $BR_HOME/performance/cgi-bin/performance
check_cp -p $BASE_OS/performance/PerfChartsForms.pm $BR_HOME/performance/cgi-bin/lib

/usr/bin/perl -p -i -e "s:/usr/local/groundwork/nagios:\@\@BITROCK_NAGIOS_ROOTDIR\@\@:g" $BR_HOME/nagios/etc/nagios.cfg

# check_cp -p /tmp/portal-network-service.war $BR_HOME/foundation/container/webapps/jboss/jboss-portal.sar

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
# The following is a bogus correction, so it's now disabled.  The original JIRA (now lost to the ravages of time
# and no longer accessible) complained instead about a non-existent nagios/share/images/logos/services.gif file,
# not a non-existent monarch/htdocs/monarch/images/logos/services.gif file.  See the actual correction below.
# check_cp -rp $BR_HOME/monarch/htdocs/monarch/images/services.gif $BR_HOME/monarch/htdocs/monarch/images/logos

# GWMON-5861
# The fix here used to be wiped out but in any case was then duplicated by the immediately following fix
# for GWMON-5958, so that makes it a bogus correction.  So as not to confuse ourselves as to what is really
# happening, this initial swipe is now disabled.
# check_cp -rp $BASE_CORE/nagios/share/images/logos/services.gif $BR_HOME/nagios/share/images/logos

# GWMON-5958
# The original JIRA (now lost to the ravages of time and no longer accessible) complained about a non-existent
# nagios/share/images/logos/graph.gif file.  Why we wiped out ALL of the nagios/share/images/logos/* files in
# preparation for including some additional files (GWMON-627) is an unfathomable mystery now.  But since Nagios
# 4.2.4 does include additional files, there's no point in removing any of them from the build now.  We simply
# overlay the Nagios 4.2.4 set with our historical set of image files.  Many of them are the same.  Some are only
# in the new set, and some are only in the old set.  A few are in both sets and differ between them, namely:
#
#     firewall.gd2
#     firewall.gif
#     graph.gif
#     hub.gd2
#     hub.gif
#     nagios.gd2
#     router.gd2
#     router.gif
#     switch.gd2
#     switch.gif
#
# For the moment, for compatibility with previous GWMEE releases, we continue to include the
# old copies of these conflicting files in our build.  We might revisit that decision in the
# future.  (Keeping the new copies does not mean we would completely disable the "check_cp -rp"
# command here.  It means that we would need to ensure that this action does not overwrite any
# existing files.  We would use the check_cp -n or --no-clobber option to accomplish this.)
#
# Don't delete the full set of original upstream images any more; there's no point in doing so.
# rm -rf $BR_HOME/nagios/share/images/logos
check_cp -rp $BASE_CORE/nagios/share/images/logos $BR_HOME/nagios/share/images

# GWMON-5795
check_cp -rp $BASE_OS/syslib/groundwork.logrotate $BR_HOME/common/etc 

# GWMON-10245
if ! [ -d $BR_HOME/postgresql/etc/logrotate.d ] ; then
    check_mkdir -p $BR_HOME/postgresql/etc/logrotate.d
fi
check_cp -rp $BASE_OS/syslib/groundwork-postgresql $BR_HOME/postgresql/etc/logrotate.d

# GWMON-5856
rm -rf $BR_HOME/nagios/libexec/check_mysql_status

# GWMON-2236
rm -rf $BR_HOME/nagios/libexec/check_sensors

# GWMON-5661
rm -rf $BR_HOME/nagios/libexec/check_nagios_status_log.pl

# GWMON-5984
chmod 755 $BR_HOME/profiles/cgi-bin
chmod 444 $BR_HOME/profiles/*.xml
chmod 755 $BR_HOME/nagios/libexec

# GWMON-5369
rm -rf $BR_HOME/reports/utils/utils

# GWMON-4353
# FIX MAJOR:  This copying no longer works, because /home/build/BitRock/groundwork/bookshelf/docs/whphost.js does not exist.
# What to do about this?
cp -rp $BR_HOME/bookshelf/docs/whphost.js $BR_HOME/bookshelf/docs/bookshelf-data

# GWMON-6066
chmod 640 $BR_HOME/core-config/*properties*

check_cp -p $BASE_CORE/apache/index.html $BR_HOME/apache2/htdocs

# 6.0 only
# Update Apache config file
check_cp -p $BASE_CORE/apache/httpd.conf.6.0 $BR_HOME/apache2/conf/httpd.conf

# GWMON-6842
rm -f $BR_HOME/nagios/libexec/*.c

# GWMON-6503
rm -f $BR_HOME/profiles/perfconfig-vmware_esx3_services_profile.xml

# GWMON-7188
# FIX MAJOR:  This copying does not work without error, because /home/build/groundwork-monitor/monarch/dassmonarch/doc
# is a directory and this is not a recursive copy.
# What to do about this?
cp -p $BASE/monarch/dassmonarch/* $BR_HOME/monarch/lib

# GWMON-9222
# Execute permissions are disabled because this sample code ought not to be enabled by default for possible
# running in production environments, since it makes some adjustments to the operating monarch database.
# chmod 755 $BR_HOME/monarch/lib/sample.dassmonarch.pl

# Remove dassmonarch directory, need to find out where dassmonarch gets copy
rm -rf $BR_HOME/monarch/lib/dassmonarch

# GWMON-5371
###GWMON-7739#### check_cp -p $BASE_CORE/nagios/performance/eventhandlers/process_service_perf.pl $BR_HOME/nagios/eventhandlers

# GWMON-7466
rm -f $BR_HOME/nagios/libexec/check_fan*

# GWMON-6937
rm -f $BR_HOME/core-config/migration.properties


#
check_cp -p $BASE_OS/performance-core/eventhandler/process_service_perfdata_file $BR_HOME/nagios/eventhandlers
chmod +x $BR_HOME/nagios/eventhandlers/*

# GWMON-8544
check_cp -p $BASE_OS/performance-core/eventhandler/perfdata.properties.ce $BR_HOME/core-config/perfdata.properties

# GWMON-7665
check_mkdir -p $BR_HOME/foundation/jboss/bin
check_cp -p $BASE/foundation/misc/web-application/jboss/twiddle.jar $BR_HOME/foundation/jboss/bin
check_cp -p $BASE/foundation/misc/web-application/jboss/twiddle.sh $BR_HOME/foundation/jboss/bin
check_cp -p $BASE_CORE/nagios/plugins-gwcustom/check_jbossjmx.sh $BR_HOME/nagios/libexec
chmod +x $BR_HOME/foundation/jboss/bin/twiddle.jar
chmod +x $BR_HOME/foundation/jboss/bin/twiddle.sh
chmod +x $BR_HOME/nagios/libexec/check_jbossjmx.sh

# GWMON-7739
rm -f $BR_HOME/nagios/eventhandlers/process_service_perf.pl
rm -f $BR_HOME/nagios/eventhandlers/process_service_perf_db.pl

# GWMON-6583
sed -i 's/usr\/bin\/perl -w/usr\/local\/groundwork\/perl\/bin\/perl -w/' $BR_HOME/common/bin/sendEmail


#check_mkdir $BR_HOME/foundation/scripts
#check_cp -p $BASE/foundation/resources/reset_passive_check.sh $BR_HOME/foundation/scripts
#chmod +x $BR_HOME/foundation/scripts/reset_passive_check.sh


# FIX MAJOR:  Since we no longer check out the $OS_ARCHIVE/monitor-portal code above,
# how can we expect it to be here and to build properly at this point???
# This block of code is probably all obsolete now.
if [ "$Build4" == "" ] ; then
  # Build monitor portal
  check_chdir $BASE/monitor-portal
  if [ $? -ne 0 ]; then
    echo "BUILD FAILED: Cannot change to $BASE/monitor-portal as a working directory." | mail -s "GWMON-$GWMEE_VERSION CE Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
    exit 1
  fi
  maven deploy

  check_cp -rp $GW_HOME/config/* $BR_HOME/core-config
  check_cp -rp $GW_HOME/foundation/container/webapps/jboss/jboss-portal.sar/*.war $BR_HOME/foundation/container/webapps/jboss/jboss-portal.sar
  check_cp -rp $GW_HOME/foundation/container/webapps/jboss/jboss-portal.sar/lib/*.jar $BR_HOME/foundation/container/webapps/jboss/jboss-portal.sar/lib
  check_cp -rp $BR_HOME/core-config/jboss-service.xml $BR_HOME/foundation/container/config/jboss
# Network service portlet no longer standalone
#  check_cp -p $GW_HOME/foundation/container/webapps/jboss/jboss-portal.sar/network-service-portlet.war $BR_HOME/foundation/container/webapps/jboss/jboss-portal.sar/network-service-portlet.war
#  check_cp -p $GW_HOME/foundation/container/webapps/jboss/jboss-portal.sar/network-service-portlet.war $BR_HOME/network-service/libs/java

######  check_mkdir $BR_HOME/foundation/scripts
######  check_cp -p $BASE/foundation/resources/reset_passive_check.sh $BR_HOME/foundation/scripts
######  chmod +x $BR_HOME/foundation/scripts/reset_passive_check.sh
  rm -f $BR_HOME/common/scripts/ctl-nsca.sh

  # GWMON-7405
  # These files are now believed to be obsolete, relating only to old releases.  See the JIRA for what they related to.
  # check_cp -p $BASE/monitor-framework/core/src/resources/portal-server-war/login.jsp $BR_HOME/foundation/container/webapps/jboss/jboss-portal.sar/portal-server.war
  # check_cp -p $BASE/monitor-framework/core/src/resources/portal-core-war/images/* $BR_HOME/foundation/container/webapps/jboss/jboss-portal.sar/portal-core.war/images

  # GWMON-7686
  rm -f $BR_HOME/foundation/container/webapps/birtviewer.war
  rm -f $BR_HOME/foundation/container/webapps/foundation-reportserver.war

  # GWMON-7843
  rm -f $BR_HOME/monarch/automation/conf/discover-template-GroundWork-Discovery-Pro.xml
  rm -f $BR_HOME/monarch/automation/conf/schema-template-GroundWork-Discovery-Pro.xml
  rm -f $BR_HOME/monarch/automation/conf/snmp_scan_input_Pro.cfg
fi

# Force process_foundation_db_update is off
Process_Foundation_Db_Update=$(grep process_foundation_db_update $BR_HOME/nagios/eventhandlers/process_service_perf_db_file.pl | awk '{ print $4; }' | sed 's/;//')
sed -i 's/process_foundation_db_updates = '$Process_Foundation_Db_Update'/process_foundation_db_updates = 0/' $BR_HOME/nagios/eventhandlers/process_service_perf_db_file.pl
 
Process_Foundation_Db_Update=$(grep process_foundation_db_update $BR_HOME/nagios/eventhandlers/process_service_perfdata_file | awk '{ print $4; }' | sed 's/;//')
sed -i 's/process_foundation_db_updates = '$Process_Foundation_Db_Update'/process_foundation_db_updates = 0/' $BR_HOME/nagios/eventhandlers/process_service_perfdata_file

check_cp -p $BASE/foundation/resources/exec_rrdgraph.pl $BR_HOME/common/bin

# Update revision number on morat
#svn info /home/build/groundwork-monitor/monitor-os/project.properties | grep Revision: | awk '{ print $2; }' > $RUN_DIR/logs/CE_Revision

# Clean up the package
find $BR_HOME -name .svn -exec rm -rf {} \;
chown -R nagios:nagios $BR_HOME

date
echo "Monitor-ce build is done at `date`"
#########################################

echo "CEBuild.sh is done..."

#!/bin/bash -x
# GroundWork Monitor - The ultimate data integration framework.
# Copyright 2009 GroundWork Open Source, Inc. "GroundWork"
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

date

Box=$(uname -n | sed 's/.groundwork.groundworkopensource.com//')

PATH=$PATH:$HOME/bin
export BR_HOME=/root/build/BitRock/groundwork
export GW_HOME=/usr/local/groundwork
export ANT_HOME=$(which ant|sed 's/\/bin\/ant//')
export MAVEN_HOME=$(which maven|sed 's/\/bin\/maven//')
export LDFLAGS=-L$GW_HOME/$libdir
export LD_RUN_PATH=$GW_HOME/$libdir:$LD_RUN_PATH
export LD_LIBRARY_PATH=$GW_HOME/$libdir:$LD_LIBRARY_PATH
export CPPFLAGS=-I$GW_HOME/include
export NoCheckIns=$1

export PATH=$JAVA_HOME/bin:$GW_HOME/bin:$PATH:$MAVEN_HOME/bin:$ANT_HOME/bin

HOME=/home/nagios
BASE=$HOME/groundwork-monitor
BUILD_DIR=$BASE/build

# Clean up previous builds
rm -rf $GW_HOME
rm -rf $BASE

cd $HOME

# Check out from subversion
svn co -N http://archive.groundworkopensource.com/groundwork-opensource/trunk groundwork-monitor
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/foundation groundwork-monitor/foundation

# Increment foundation-build number
release=$(fgrep "org.groundwork.rpm.release.number" $BASE/foundation/project.properties |awk '{ print $3; }')
if [ "$NoCheckIns" == "update_svn" ] ; then
  new_release=`expr $release + 1`
else
  new_release=$release
fi

# Increment foundation OS version
OldfoundationOs=$(fgrep "org.groundwork.os.version" $BASE/foundation/project.properties | sed 's/\./ /g' | awk '{ print $6; }')

# Set new foundation-build release number
sed -e 's/org.groundwork.rpm.release.number = '$release'/org.groundwork.rpm.release.number = '$new_release'/' $BASE/foundation/project.properties >  $BASE/foundation/project.properties.tmp
sed -e 's/org.groundwork.os.version=3.0.'$OldfoundationOs'/org.groundwork.os.version=3.0.'$new_release'/' $BASE/foundation/project.properties.tmp >  $BASE/foundation/project.properties.tmp1
mv  $HOME/groundwork-monitor/foundation/project.properties.tmp1  $BASE/foundation/project.properties

# Commit foundation project.properties back to subversion
echo "Increment build(release) number" > svnmessage
if [ "$NoCheckIns" == "update_svn" ] ; then
  svn commit --username build --password bgwrk08 $BASE/foundation/project.properties -F svnmessage
fi 
rm -rf svnmessage

# Update build properties
grep -v foundation $BR_HOME/build.properties > $BR_HOME/build.properties.tmp
echo foundation=3.0-$new_release >> $BR_HOME/build.properties.tmp
mv -f $BR_HOME/build.properties.tmp $BR_HOME/build.properties

cat $BR_HOME/groundwork-foundation.xml | sed 's/name="foundation_build" value="/name="foundation_build" value="3.0-'$new_release'/' > $BR_HOME/groundwork-foundation.xml.tmp
mv -f $BR_HOME/groundwork-foundation.xml.tmp $BR_HOME/groundwork-foundation.xml

# Cleanup Maven repository from the old jar files
old_release=`expr $new_release -1`
find /root/.maven -name *-3.0.'$old_release'.jar -exec rm -f {} \;

cd $BASE/foundation
. maven allClean &>/dev/null
. maven allBuild

/usr/local/groundwork/ctlscript.sh start gwservices

cd $BASE
svn co http://geneva/svn/engineering/vendor/jboss-portal jbossportal

cd $BASE/jbossportal/build
ant -f build-gwportal.xml deploy

# Build foundation/api
mkdir -p $GW_HOME/foundation/api/perl
mkdir -p $GW_HOME/foundation/api/php
cp -p $BASE/foundation/collage/api/Perl/CollageQuery/lib/CollageQuery.pm $GW_HOME/foundation/api/perl
cp -rp $BASE/foundation/collage/api/php/adodb $GW_HOME/foundation/api
cp -rp $BASE/foundation/collage/api/php/collageapi $GW_HOME/foundation/api
cp -rp $BASE/foundation/collage/api/php/DAL $GW_HOME/foundation/api

mv $BR_HOME/foundation $BR_HOME/foundation.bitrock
cp -rp $GW_HOME/foundation $BR_HOME
cp -rp $BR_HOME/foundation.bitrock/scripts $BR_HOME/foundation

# Update core-config
cp -rp $GW_HOME/config/* $BR_HOME/core-config
cp -rp $BR_HOME/core-config/db.properties $BR_HOME/core-config/db.properties.os
rm -rf $BR_HOME/config

echo "nightlyBuildFoundationBitRock.sh build is done."

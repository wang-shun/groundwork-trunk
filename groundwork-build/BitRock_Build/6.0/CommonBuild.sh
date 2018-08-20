#!/bin/bash -x 
#Copyright (C) 2009  GroundWork Open Source Solutions info@groundworkopensource.com
#

echo "Starting common build at `date`"

Box=$(uname -n | sed 's/.groundwork.groundworkopensource.com//')

PATH=$PATH:$HOME/bin
export GW_HOME=/usr/local/groundwork
export JAVA_HOME=$(which java|sed 's/\/bin\/java//')
export ANT_HOME=$(which ant|sed 's/\/bin\/ant//')
export MAVEN_HOME=$(which maven|sed 's/\/bin\/maven//')

export PATH=$JAVA_HOME/bin:$GW_HOME/bin:$PATH:$MAVEN_HOME/bin:$ANT_HOME/bin

HOME=/home/nagios
BASE_BSH=$HOME/groundwork-bookshelf
BASE=$HOME/groundwork-monitor

# Clean up previous Bookshelf, Foundation, and JBoss builds
rm -rf $GW_HOME
rm -rf $BASE_BSH
rm -rf $BASE
ssh 172.28.113.211 rm -f /root/build/logs/start_32bit

# Check out Bookshelf from subversion
cd $HOME
svn co --username build --password bgwrk08 http://geneva/groundwork-professional/trunk/bookshelf groundwork-bookshelf
cd $HOME/groundwork-bookshelf
svn co --username build --password bgwrk08 http://geneva/groundwork-professional/trunk/bookshelf-data bookshelf-data

# Increment bookshelf-build number
release=$(fgrep "org.groundwork.rpm.release.number" $BASE_BSH/data-build/project.properties |awk '{ print $3; }')
new_release=`expr $release + 1`

# Set new bookshelf-build release number
sed -e 's/org.groundwork.rpm.release.number = '$release'/org.groundwork.rpm.release.number = '$new_release'/' $BASE_BSH/data-build/project.properties >  $BASE_BSH/data-build/project.properties.tmp
mv  $BASE_BSH/data-build/project.properties.tmp  $BASE_BSH/data-build/project.properties

# Commit bookshelf project.properties back to subversion 
echo "Increment build(release) number" > svnmessage
svn commit --username build --password bgwrk08 $BASE_BSH/data-build/project.properties -F svnmessage
rm -rf svnmessage

# Start master build script
cd $BASE_BSH
maven allBuild allDeploy

# Apply patches
cp -rf $BASE_BSH/patches/* /usr/local/groundwork/docs

# Save Bookshelf release number
echo "$new_release" > /usr/local/groundwork/bookshelf_release.txt

echo "Bookshelf build is done at `date`"
################################################################################
echo "Starting Foundation build..."

cd $HOME
# Check out Foundation from subversion
svn co -N http://archive.groundworkopensource.com/groundwork-opensource/trunk groundwork-monitor
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/foundation groundwork-monitor/foundation


# Check if any foundaation java or xml file is updated
if [ -f "/root/build/logs/FoundationIsUpdated.txt" ] ; then
  # Increment foundation build number
  release=$(fgrep "org.groundwork.rpm.release.number" $BASE/foundation/project.properties |awk '{ print $3; }')
  new_release=`expr $release + 1`

  # Increment foundation OS version
  OldfoundationOs=$(fgrep "org.groundwork.os.version" $BASE/foundation/project.properties | sed 's/\./ /g' | awk '{ print $6; }')

  # Set new foundation-build release number
  sed -i 's/org.groundwork.rpm.release.number = '$release'/org.groundwork.rpm.release.number = '$new_release'/' $BASE/foundation/project.properties 
  sed -i 's/org.groundwork.os.version=3.0.'$OldfoundationOs'/org.groundwork.os.version=3.0.'$new_release'/' $BASE/foundation/project.properties 

  # Commit foundation project.properties back to subversion
  echo "Increment build(release) number" > $HOME/svnmessage
  svn commit --username build --password bgwrk08 $BASE/foundation/project.properties -F $HOME/svnmessage

  # Cleanup Maven repository from the old jar files
  old_release=`expr $new_release - 1`
  find /root/.maven -name *-3.0.*.jar -exec rm -f {} \;

  rm -rf $HOME/monitor-portal
  # Increment monitor-portal build number for CE
  cd $HOME
  svn co -N http://archive.groundworkopensource.com/groundwork-opensource/trunk/monitor-portal
  sed -i 's/org.itgroundwork.version = 3.0.'$OldfoundationOs'/org.itgroundwork.version = 3.0.'$new_release'/' $HOME/monitor-portal/project.properties
  svn commit --username build --password bgwrk08 $HOME/monitor-portal/project.properties -F $HOME/svnmessage

  rm -rf $HOME/monitor-portal
  # Increment monitor-portal build number for EE
  cd $HOME
  svn co -N --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-portal
  sed -i 's/org.itgroundwork.version = 3.0.'$OldfoundationOs'/org.itgroundwork.version = 3.0.'$new_release'/' $HOME/monitor-portal/project.properties
  svn commit --username build --password bgwrk08 $HOME/monitor-portal/project.properties -F $HOME/svnmessage
fi



cd $BASE/foundation
. maven allClean &>/dev/null
. maven allBuild

new_release=$(grep "org.groundwork.rpm.release.number" /home/nagios/groundwork-monitor/foundation/project.properties | awk '{ print $3; }')
echo "$new_release" > /usr/local/groundwork/foundation_release.txt

rm -rf $HOME/groundwork-foundation
rm -rf /usr/local/groundwork-foundation
cp -rp $HOME/groundwork-monitor $HOME/groundwork-foundation
cp -rp $GW_HOME /usr/local/groundwork-foundation

echo "Foundation build is done at `date`"
################################################################################
echo "Starting monitor-framwork build for Ent"

cd $BASE
# Check out Framework from core-subversion
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/monitor-framework monitor-framework
mkdir $BASE/tmp
cd $BASE/tmp
svn co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-framework
mv -f $BASE/tmp/monitor-framework/core/src/resources/portal-core-sar/conf/data/default-object.xml $BASE/monitor-framework/core/src/resources/portal-core-sar/conf/data
mv -f $BASE/tmp/monitor-framework/core/src/resources/portal-server-war/login.jsp $BASE/monitor-framework/core/src/resources/portal-server-war

cd $BASE/monitor-framework/build
ant -f build-gwportal.xml deploy

# Build foundation/api
mkdir -p $GW_HOME/foundation/api/perl
mkdir -p $GW_HOME/foundation/api/php
cp -p $BASE/foundation/collage/api/Perl/CollageQuery/lib/CollageQuery.pm $GW_HOME/foundation/api/perl
cp -rp $BASE/foundation/collage/api/php/adodb $GW_HOME/foundation/api/php
cp -rp $BASE/foundation/collage/api/php/collageapi $GW_HOME/foundation/api/php
cp -rp $BASE/foundation/collage/api/php/DAL $GW_HOME/foundation/api/php

rm -rf $HOME/groundwork-common.ent
rm -rf /usr/local/groundwork-common.ent
mv $BASE $HOME/groundwork-common.ent
mv $GW_HOME /usr/local/groundwork-common.ent

echo "Jboss Portal build fot Ent is done at `date`"
################################################################################
echo "Starting monitor-framwork build for CE"

cp -rp $HOME/groundwork-foundation $HOME/groundwork-monitor
cp -rp /usr/local/groundwork-foundation $GW_HOME

cd $BASE
# Check out Framework from pro-subversion
svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/monitor-framework monitor-framework

# GWMON-7345
# Update title page
sed -i 's/%TITLE%/Groundwork Community Edition 6.0/g' $BASE/monitor-framework/core/src/bin/portal-core-war/layouts/default-dashboard/index.jsp
sed -i 's/%TITLE%/Groundwork Community Edition 6.0/g' $BASE/monitor-framework/core/src/bin/portal-core-war/layouts/generic/index.jsp
sed -i 's/%TITLE%/Groundwork Community Edition 6.0/g' $BASE/monitor-framework/core/src/bin/portal-core-war/layouts/svlayout/index.jsp
sed -i 's/%TITLE%/Groundwork Community Edition 6.0/g' $BASE/monitor-framework/core/src/bin/portal-core-war/layouts/3columns/index.jsp
sed -i 's/%TITLE%/Groundwork Community Edition 6.0/g' $BASE/monitor-framework/core/src/bin/portal-core-war/layouts/1column/index.jsp

cd $BASE/monitor-framework/build
ant -f build-gwportal.xml deploy

# Build foundation/api
mkdir -p $GW_HOME/foundation/api/perl
mkdir -p $GW_HOME/foundation/api/php
cp -p $BASE/foundation/collage/api/Perl/CollageQuery/lib/CollageQuery.pm $GW_HOME/foundation/api/perl
cp -rp $BASE/foundation/collage/api/php/adodb $GW_HOME/foundation/api/php
cp -rp $BASE/foundation/collage/api/php/collageapi $GW_HOME/foundation/api/php
cp -rp $BASE/foundation/collage/api/php/DAL $GW_HOME/foundation/api/php

rm -rf $HOME/groundwork-common.ce
rm -rf /usr/local/groundwork-common.ce
mv $BASE $HOME/groundwork-common.ce
mv $GW_HOME /usr/local/groundwork-common.ce

echo "Jboss Portal build fot CE is done at `date`"
################################################################################

ssh 172.28.113.211 touch /root/build/logs/start_32bit

date
echo "CommonBuild.sh is done."

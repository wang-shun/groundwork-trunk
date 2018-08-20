#!/bin/bash -x 
#Copyright (C) 2009  GroundWork Open Source Solutions info@groundworkopensource.com
#

echo "Starting common build at `date`"

Box=$(uname -n | sed 's/.groundwork.groundworkopensource.com//')

PATH=$PATH:$HOME/bin
export GW_HOME=/usr/local/groundwork
#export JAVA_HOME=$(which java|sed 's/\/bin\/java//')
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
ssh horw rm -f /root/build/logs/start_32bit


# Checkout function
svn_co () {
    for i in 1 0; do
        svn co $1 $2 $3 $4 $5 $6
        SVN_EXIT_CODE=$?
        if [ $SVN_EXIT_CODE -eq 0 ]; then
            break;
        elif [ $i -eq 0 ]; then
            echo "BUILD FAILED: There has been a problem trying to checkout groundwork files." | mail -s "6.4 Build FAILED in  `hostname` - $DATE" build-info@gwos.com
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
            echo "BUILD FAILED: There has been a problem trying to commit groundwork files." | mail -s "6.4 Build FAILED in  `hostname` - $DATE" build-info@gwos.com
            exit 1
        fi
        sleep 30
    done

}



# Check out Bookshelf from subversion
cd $HOME
svn_co --username build --password bgwrk08 http://geneva/groundwork-professional/trunk/bookshelf groundwork-bookshelf
cd $HOME/groundwork-bookshelf
svn_co --username build --password bgwrk08 http://geneva/groundwork-professional/trunk/bookshelf-data bookshelf-data

# Increment bookshelf-build number
release=$(fgrep "org.groundwork.rpm.release.number" $BASE_BSH/data-build/project.properties |awk '{ print $3; }')
new_release=`expr $release + 1`

# Set new bookshelf-build release number
sed -e 's/org.groundwork.rpm.release.number = '$release'/org.groundwork.rpm.release.number = '$new_release'/' $BASE_BSH/data-build/project.properties >  $BASE_BSH/data-build/project.properties.tmp
mv  $BASE_BSH/data-build/project.properties.tmp  $BASE_BSH/data-build/project.properties

# Commit bookshelf project.properties back to subversion 
echo "Increment build(release) number" > svnmessage
svn_commit --username build --password bgwrk08 $BASE_BSH/data-build/project.properties -F svnmessage
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
svn_co -N http://archive.groundworkopensource.com/groundwork-opensource/trunk groundwork-monitor
svn_co http://archive.groundworkopensource.com/groundwork-opensource/trunk/foundation groundwork-monitor/foundation


# Check if any foundaation java or xml file is updated,
# then update Foundation, Framework, and Monitor Portal's build number.
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
  svn_commit --username build --password bgwrk08 $BASE/foundation/project.properties -F $HOME/svnmessage

  # Cleanup Maven repository from the old jar files
  find /root/.maven -name *-3.0.*.jar -exec rm -f {} \;

  rm -rf $HOME/monitor-portal
  # Increment monitor-portal build number for CE
  cd $HOME
  svn_co -N http://archive.groundworkopensource.com/groundwork-opensource/trunk/monitor-portal
  sed -i 's/org.itgroundwork.version = 3.0.'$OldfoundationOs'/org.itgroundwork.version = 3.0.'$new_release'/' $HOME/monitor-portal/project.properties
  svn_commit --username build --password bgwrk08 $HOME/monitor-portal/project.properties -F $HOME/svnmessage

  rm -rf $HOME/monitor-portal
  # Increment monitor-portal build number for EE
  cd $HOME
  svn_co -N --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-portal
  sed -i 's/org.itgroundwork.version = 3.0.'$OldfoundationOs'/org.itgroundwork.version = 3.0.'$new_release'/' $HOME/monitor-portal/project.properties
  svn_commit --username build --password bgwrk08 $HOME/monitor-portal/project.properties -F $HOME/svnmessage
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
###############################################################################
echo "Starting monitor-framwork build for Enterprise"

cd $BASE
# Check out Framework from core-subversion
svn_co http://archive.groundworkopensource.com/groundwork-opensource/trunk/monitor-framework monitor-framework

# Remove core subdirectory
rm -rf monitor-framework/core/src
rm -rf monitor-framework/core-identity/src
rm -rf monitor-framework/core-identity/build.xml

mkdir $BASE/tmp
cd $BASE/tmp
svn_co --username build --password bgwrk http://geneva/groundwork-professional/trunk/monitor-framework

mv -f $BASE/tmp/monitor-framework/core/src  $BASE/monitor-framework/core

mv -f $BASE/tmp/monitor-framework/core-identity/src $BASE/monitor-framework/core-identity
mv -f $BASE/tmp/monitor-framework/core-identity/build.xml $BASE/monitor-framework/core-identity

#mv -f $BASE/tmp/monitor-framework/core/src/resources/portal-core-sar/conf/data/default-object.xml $BASE/monitor-framework/core/src/resources/portal-core-sar/conf/data
#mv -f $BASE/tmp/monitor-framework/core/src/resources/portal-server-war/login.jsp $BASE/monitor-framework/core/src/resources/portal-server-war

#
# GWMON-8114 Make sure that the local.properties for the Enterprise build is included
#
mv -f $BASE/tmp/monitor-framework/build/local.properties $BASE/monitor-framework/build

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
###############################################################################
#echo "Starting monitor-framwork build for CE"

#cp -rp $HOME/groundwork-foundation $HOME/groundwork-monitor
#cp -rp /usr/local/groundwork-foundation $GW_HOME

#cd $BASE
# Check out Framework from pro-subversion
#svn co http://archive.groundworkopensource.com/groundwork-opensource/trunk/monitor-framework monitor-framework

# GWMON-7345
# Update title page
#sed -i 's/%TITLE%/Groundwork Community Edition 6.0.3/g' $BASE/monitor-framework/core/src/bin/portal-core-war/layouts/default-dashboard/index.jsp
#sed -i 's/%TITLE%/Groundwork Community Edition 6.0.3/g' $BASE/monitor-framework/core/src/bin/portal-core-war/layouts/generic/index.jsp
#sed -i 's/%TITLE%/Groundwork Community Edition 6.0.3/g' $BASE/monitor-framework/core/src/bin/portal-core-war/layouts/svlayout/index.jsp
#sed -i 's/%TITLE%/Groundwork Community Edition 6.0.3/g' $BASE/monitor-framework/core/src/bin/portal-core-war/layouts/3columns/index.jsp
#sed -i 's/%TITLE%/Groundwork Community Edition 6.0.3/g' $BASE/monitor-framework/core/src/bin/portal-core-war/layouts/1column/index.jsp

#cd $BASE/monitor-framework/build
#ant -f build-gwportal.xml deploy

# Build foundation/api
#mkdir -p $GW_HOME/foundation/api/perl
#mkdir -p $GW_HOME/foundation/api/php
#cp -p $BASE/foundation/collage/api/Perl/CollageQuery/lib/CollageQuery.pm $GW_HOME/foundation/api/perl
#cp -rp $BASE/foundation/collage/api/php/adodb $GW_HOME/foundation/api/php
#cp -rp $BASE/foundation/collage/api/php/collageapi $GW_HOME/foundation/api/php
#cp -rp $BASE/foundation/collage/api/php/DAL $GW_HOME/foundation/api/php

#rm -rf $HOME/groundwork-common.ce
#rm -rf /usr/local/groundwork-common.ce
#mv $BASE $HOME/groundwork-common.ce
#mv $GW_HOME /usr/local/groundwork-common.ce

#echo "Jboss Portal build fot CE is done at `date`"
################################################################################

# Bookshelf, Fundation, and Framework build is done.
# 32bit build server can start the build now.
ssh horw touch /root/build/logs/start_32bit

date
echo "CommonBuild.sh is done."

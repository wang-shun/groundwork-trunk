#!/bin/bash -x 
#Copyright (C) 2008  GroundWork Open Source Solutions info@groundworkopensource.com
#

date

Box=$(uname -n | sed 's/.groundwork.groundworkopensource.com//')

PATH=$PATH:$HOME/bin
export BR_HOME=/root/build/BitRock/groundwork
export GW_HOME=/usr/local/groundwork
export JAVA_HOME=$(which java|sed 's/\/bin\/java//')
export ANT_HOME=$(which ant|sed 's/\/bin\/ant//')
export MAVEN_HOME=$(which maven|sed 's/\/bin\/maven//')
export LDFLAGS=-L$GW_HOME/$libdir
export LD_RUN_PATH=$GW_HOME/$libdir:$LD_RUN_PATH
export LD_LIBRARY_PATH=$GW_HOME/$libdir:$LD_LIBRARY_PATH
export CPPFLAGS=-I$GW_HOME/include
export NoCheckIns=$1

export PATH=$JAVA_HOME/bin:$GW_HOME/bin:$PATH:$MAVEN_HOME/bin:$ANT_HOME/bin

HOME=/home/nagios
BASE=$HOME/groundwork-bookshelf

# Clean up previous builds
rm -rf /groundwork-bookshelf*
rm -rf /usr/src/redhat/BUILD/groundwork-bookshelf*
rm -rf /usr/src/redhat/SOURCE/groundwork-bookshelf*
rm -rf /usr/local/groundwork/

# Remove the old monitor pro from RPM/noarch directory
rm -rf $HOME/groundwork-bookshelf

# Clean up previous builds
rm -rf $BASE

# Check out from subversion
cd $HOME
svn co --username build --password bgwrk08 http://geneva/groundwork-professional/trunk/bookshelf groundwork-bookshelf
cd $HOME/groundwork-bookshelf
svn co --username build --password bgwrk08 http://geneva/groundwork-professional/trunk/bookshelf-data bookshelf-data

# Increment core-build number
release=$(fgrep "org.groundwork.rpm.release.number" $BASE/data-build/project.properties |awk '{ print $3; }')
if [ "$NoCheckIns" == "update_svn" ] ; then
  new_release=`expr $release + 1`
else
  new_release=$release
fi

# Set new core-build release number
sed -e 's/org.groundwork.rpm.release.number = '$release'/org.groundwork.rpm.release.number = '$new_release'/' $BASE/data-build/project.properties >  $BASE/data-build/project.properties.tmp
mv  $BASE/data-build/project.properties.tmp  $BASE/data-build/project.properties

# Commit core project.properties back to subversion 
echo "Increment build(release) number" > svnmessage
if [ "$NoCheckIns" == "update_svn" ] ; then
  svn commit --username build --password bgwrk08 $BASE/data-build/project.properties -F svnmessage
fi
rm -rf svnmessage

# Update build properties
grep -v bookshelf $BR_HOME/build.properties > $BR_HOME/build.properties.tmp
echo bookshelf=6.0-$new_release >> $BR_HOME/build.properties.tmp
mv -f $BR_HOME/build.properties.tmp $BR_HOME/build.properties

cat $BR_HOME/groundwork-bookshelf.xml | sed 's/name="bookshelf_build" value="/name="bookshelf_build" value="6.0-'$new_release'/' > $BR_HOME/groundwork-bookshelf.xml.tmp
mv -f $BR_HOME/groundwork-bookshelf.xml.tmp $BR_HOME/groundwork-bookshelf.xml

# Start master build script
cd $BASE
maven allBuild allDeploy

# Apply patches
cp -rf $BASE/patches/* /usr/local/groundwork/docs

if ! [ -d $BR_HOME/bookshelf ] ; then
  mkdir $BR_HOME/bookshelf
fi

cp -rp /usr/local/groundwork/docs $BR_HOME/bookshelf
cp -rp /usr/local/groundwork/guava/packages/bookshelf $BR_HOME/bookshelf
cp -rp /usr/local/groundwork/migration/gw-bookshelf-install.php $BR_HOME/bookshelf

echo "nightlyBuildBookshelfBitRock.sh build is done."

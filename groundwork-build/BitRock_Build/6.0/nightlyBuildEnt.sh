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
export DATE=$(date +%d-%m-%y)

export PATH=$JAVA_HOME/bin:$GW_HOME/bin:$PATH:$MAVEN_HOME/bin:$ANT_HOME/bin

HOME=/home/nagios
BASE=$HOME/groundwork-monitor
BASE_OS=$BASE/monitor-os
BASE_CORE=$BASE/monitor-core
BUILD_DIR=$BASE/build
RUN_DIR=/root/build
BitRockDir=/opt/installbuilder-6.1.3

date
# Call nightly to build Monitor CE
cd $RUN_DIR
. CEBuild.sh pro


date
# Call nightly to build Monitor Pro
cd $RUN_DIR
. EntBuild.sh
 

# Build package using BitRock Installer
$BitRockDir/bin/builder build $BR_HOME/project.xml 

NewBuildNumber=$(grep "<version>" $BR_HOME/project.xml | sed 's/<version>//' | sed 's/<\/version>//' | sed 's/ //g')

NewPackage=groundworkenterprise-$NewBuildNumber-linux-$bitrock_os-installer.bin
MoratDir=/var/www/html/tools/DEVELOPMENT
if ! [ -f $BitRockDir/output/$NewPackage ] ; then
  cat /tmp/masterbuild_error | mail -s "6.0 Enterprise Build FAILED in `hostname` - $DATE" asoleymanzadeh@groundworkopensource.com
  exit -1
fi

# Copy package into morant
ssh root@morat rm -f $MoratDir/builds/BitRock/6.0/groundworkenterprise-6.0-*-linux-$bitrock_os-installer.bin
ssh root@morat rm -f $MoratDir/builds/BitRock/last_good_build/groundworkenterprise-6.0-*-linux-$bitrock_os-installer.bin
scp -p $BitRockDir/output/$NewPackage root@morat:$MoratDir/builds/BitRock/6.0
scp -p $BitRockDir/output/$NewPackage root@morat:$MoratDir/builds/BitRock/last_good_build
scp -p $BitRockDir/output/$NewPackage root@morat:/DailyBuilds/BitRock/$qasubdir
scp -p $BitRockDir/output/$NewPackage root@172.28.113.212:/DailyBuilds/BitRock/$qasubdir
if [ "$arch" == "x86_64" ] ; then
  scp -p $RUN_DIR/logs/EE_Revision root@morat:$MoratDir/logs
  scp -p $RUN_DIR/logs/EE_Revision root@morat:$MoratDir/logs/$qasubdir
fi

# Let the tast machines know the package is ready for installation
#scp -p $RUN_DIR/logs/$qasubdir.is_done root@kansas:/root/test
#scp -p $RUN_DIR/logs/$qasubdir.is_done root@florida:/root/test
#scp -p $RUN_DIR/logs/$qasubdir.is_done root@montana:/root/test

# Delete old package
OldPackage=$(cat $RUN_DIR/logs/PackList.$bitrock_os | awk '{ print $1; }')
ssh root@morat rm -f /DailyBuilds/BitRock/$qasubdir/$OldPackage
# Make a new list of packages
cat $RUN_DIR/logs/PackList.$bitrock_os | awk '{ print $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14}' > $RUN_DIR/logs/PackList.$bitrock_os.tmp
NewPackageList=$(cat $RUN_DIR/logs/PackList.$bitrock_os.tmp)
echo "$NewPackageList $NewPackage" > $RUN_DIR/logs/PackList.$bitrock_os

date
echo "nightlyBuildEnt.sh is done..."


#!/bin/bash -x
#Copyright (C) 2013  GroundWork Inc. info@groundworkopensource.com
#
# Main build script to build GroundWork Enterprise product
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
export LDFLAGS=-L$GW_HOME/$libdir
export LD_RUN_PATH=$GW_HOME/$libdir:$LD_RUN_PATH
export LD_LIBRARY_PATH=$GW_HOME/$libdir:$LD_LIBRARY_PATH
export CPPFLAGS=-I$GW_HOME/include
export RELEASE=$distro$ARCH
export DATE=$(date +%Y-%m-%d)

export PATH=$JAVA_HOME/bin:$GW_HOME/bin:$PATH:$MAVEN_HOME/bin:$ANT_HOME/bin

HOME=/home/build
BASE=$HOME/groundwork-monitor
BASE_OS=$BASE/monitor-os
BASE_CORE=$BASE/monitor-core
BUILD_DIR=$BASE/build
RUN_DIR=/home/build
BitRockDir=/opt/installbuilder-8.0.1
BUILD_SCRIPT_DIR=$RUN_DIR/build7/GWMON-7.0.0

# CEBuild script loads Bitrock package on system, Still needs to be called for with pro-option
date
# Call nightly to build Monitor CE
cd $BUILD_SCRIPT_DIR
. CEBuild.sh pro


date
# Call nightly to build Monitor Pro
cd $BUILD_SCRIPT_DIR
. EntBuild.sh
 

# Build package using BitRock Installer
$BitRockDir/bin/builder build $BR_HOME/project.xml 

NewBuildNumber=$(grep "<version>" $BR_HOME/project.xml | sed 's/<version>//' | sed 's/<\/version>//' | sed 's/ //g')

NewPackage=groundworkenterprise-$NewBuildNumber-linux-$bitrock_os-installer.run
MoratDir=/var/www/html/tools/DEVELOPMENT

grep -v "grep" $RUN_DIR/logs/build.log | grep "BUILD FAIL"
GREP_ERROR=$?

if [ $GREP_ERROR -eq 0 ]; then
  ssh root@morat echo "error" >> /var/www/html/tools/DEVELOPMENT/logs/EE_Revision
  grep -v "grep" $RUN_DIR/logs/build.log | grep -A 15 -B 45 "BUILD FAILED" | mail -s "7.1.0 Enterprise Build FAILED in `hostname` - $DATE" build-info@gwos.com
  exit -1
elif [ -f $BitRockDir/output/$NewPackage ] ; then
    $BitRockDir/output/$NewPackage --version | mail -s "7.1.0 Build OK in `hostname` - $DATE" build-info@gwos.com
else
    echo "Unknown error" | mail -s "7.1.0 Enterprise Build FAILED in `hostname` - $DATE" build-info@gwos.com
    exit -1
fi

# Copy package into morat
ssh root@morat rm -f $MoratDir/builds/BitRock/6.0/groundworkenterprise-7.1.0-*-linux-$bitrock_os-installer.run
ssh root@morat mv $MoratDir/builds/BitRock/last_good_build/groundworkenterprise-7.1.0-*-linux-$bitrock_os-installer.run /tmp
scp -p $BitRockDir/output/$NewPackage root@morat:$MoratDir/builds/BitRock/6.0
scp -p $BitRockDir/output/$NewPackage root@morat:$MoratDir/builds/BitRock/last_good_build
scp -p $BitRockDir/output/$NewPackage root@morat:/DailyBuilds/BitRock/$qasubdir

ssh root@morat $MoratDir/builds/BitRock/comparer-linux.bin /tmp/groundworkenterprise-7.1.0-*-linux-$bitrock_os-installer.run $MoratDir/builds/BitRock/last_good_build/groundworkenterprise-6.6.0-*-linux-$bitrock_os-installer.bin > $RUN_DIR/logs/comparer-EE-$bitrock_os.log
scp -p $RUN_DIR/logs/comparer-EE-$bitrock_os.log root@morat:$MoratDir/logs
ssh root@morat rm -f /tmp/groundworkenterprise-7.1.0-*-linux-$bitrock_os-installer.run

if [ "$arch" == "x86_64" ] ; then
  scp -p $RUN_DIR/logs/EE_Revision root@morat:$MoratDir/logs
  scp -p $RUN_DIR/logs/EE_Revision root@morat:$MoratDir/logs/$qasubdir
fi


# Delete old package
OldPackage=$(cat $RUN_DIR/logs/PackList.$bitrock_os | awk '{ print $1; }')
ssh root@morat rm -f /DailyBuilds/BitRock/$qasubdir/$OldPackage
# Make a new list of packages
cat $RUN_DIR/logs/PackList.$bitrock_os | awk '{ print $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14}' > $RUN_DIR/logs/PackList.$bitrock_os.tmp
NewPackageList=$(cat $RUN_DIR/logs/PackList.$bitrock_os.tmp)
echo "$NewPackageList $NewPackage" > $RUN_DIR/logs/PackList.$bitrock_os

date
echo "nightlyBuildEnt.sh is done..."

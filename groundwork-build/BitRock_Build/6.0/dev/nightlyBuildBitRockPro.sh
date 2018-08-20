#!/bin/bash -x
# GroundWork Monitor - The ultimate data integration framework.
# Copyright 2008 GroundWork Open Source, Inc. "GroundWork"
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
##
# Build properties for Monitor - Opensource
# The values have to be in sync with the settings in groundwork-private
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
BitRockDir=/opt/installbuilder-5.4.14

date
# Call nightly to build Monitor CE
cd $RUN_DIR
. CEBuildBitRock.sh pro

date
# Call nightly to build Monitor Pro
cd $RUN_DIR
. ProBuildBitRock.sh

# Build package using BitRock Installer
$BitRockDir/bin/builder build $BR_HOME/project.xml

NewBuildNumber=$(grep "<version>" $BR_HOME/project.xml | sed 's/<version>//' | sed 's/<\/version>//' | sed 's/    //')

NewPackage=groundworkpro-$NewBuildNumber-linux-$bitrock_os-installer.bin
MoratDir=/var/www/html
if ! [ -f $BitRockDir/output/$NewPackage ] ; then
  cat /tmp/masterbuild_error | mail -s "6.0 Build FAILED in `hostname` - $DATE" asoleymanzadeh@groundworkopensource.com
  exit -1
fi

# Copy package into morant
gw_release=$(grep "\-gw" $BR_HOME/project.xml | sed 's/</ /g' | sed 's/-/ /g' | awk '{ print $3; }')
ssh root@morat rm -f $MoratDir/builds/BitRock/6.0/groundworkpro-6.0-*-linux-$bitrock_os-installer.bin
scp -rp  $BitRockDir/output/$NewPackage root@morat:$MoratDir/builds/BitRock/6.0
scp -rp  $BitRockDir/output/$NewPackage root@morat:/DailyBuilds/BitRock/$qasubdir
scp -rp  $BitRockDir/output/$NewPackage root@172.28.113.212:/DailyBuilds/BitRock/$qasubdir
scp -rp $RUN_DIR/logs/BitRock-$gw_release root@morat:$MoratDir/logs/BitRock"$qasubdir"_Revision

# Delete old package 
OldPackage=$(cat $RUN_DIR/logs/PackList.$bitrock_os | awk '{ print $1; }')
ssh root@morat rm -f /DailyBuilds/BitRock/$qasubdir/$OldPackage
# Make a new list of packages
cat $RUN_DIR/logs/PackList.$bitrock_os | awk '{ print $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14}' > $RUN_DIR/logs/PackList.$bitrock_os.tmp
NewPackageList=$(cat $RUN_DIR/logs/PackList.$bitrock_os.tmp)
echo "$NewPackageList $NewPackage" > $RUN_DIR/logs/PackList.$bitrock_os

date
echo "BitRockpro build for 6.0 is done..."

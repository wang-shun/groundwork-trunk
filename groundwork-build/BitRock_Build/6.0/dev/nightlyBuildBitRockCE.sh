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

export PATH=$JAVA_HOME/bin:$PATH:$MAVEN_HOME/bin:$ANT_HOME/bin

HOME=/home/nagios
BASE=$HOME/groundwork-monitor
BASE_OS=$BASE/monitor-os
BASE_CORE=$BASE/monitor-core
BUILD_DIR=$BASE/build
RUN_DIR=/root/build

date
# Call CEBuildBitRock.sh to build Monitor CE
. CEBuildBitRock.sh

# Build package using BitRock Installer
/opt/installbuilder-5.4.14/bin/builder build $BR_HOME/project.xml

NewBuildNumber=$(grep "<version>" $BR_HOME/project.xml | sed 's/<version>//' | sed 's/<\/version>//' | sed 's/    //')

# Copy package into morat
#ssh root@morat rm -f /var/www/html/builds/BitRock/groundwork-5.3.0-*-linux-$bitrock_os-installer.bin
#scp -rp  /opt/installbuilder-5.4.14/output/groundwork-"$NewBuildNumber"-linux-$bitrock_os-installer.bin root@morat:/var/www/html/builds/BitRock
#scp -rp  /opt/installbuilder-5.4.14/output/groundwork-"$NewBuildNumber"-linux-$bitrock_os-installer.bin root@morat:/DailyBuilds/BitRock/$qasubdir
#scp -rp  /opt/installbuilder-5.4.14/output/groundwork-"$NewBuildNumber"-linux-$bitrock_os-installer.bin root@172.28.113.212:/DailyBuilds/BitRock/$qasubdir
#scp -rp $RUN_DIR/logs/BitRock"$bitrock_os"bit_Revision root@morat:/var/www/html/logs

date
echo "BitRock build for Monitor CE is done..."

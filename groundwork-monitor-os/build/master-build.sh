#!/bin/bash
# description: monitor-core build script
# GroundWork Monitor - The ultimate data integration framework.
# Copyright 2007 GroundWork Open Source, Inc. "GroundWork"
#
#    This program is free software; you can redistribute it and/or modify
#    it under the terms of version 2 of the GNU General Public License
#    as published by the Free Software Foundation and reprinted below;
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program; if not, write to the Free Software
#    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
#

BASE=$PWD/..
BUILD_DIR=$BASE/build

. checkEnv.sh

cd $BUILD_DIR
. checkEnv.sh
. downloadSources.sh

cd $BUILD_DIR
. buildBaseComponents.sh

BUILD_DIR=$BASE/build

cd $BUILD_DIR
. buildCore.sh

rm -rf /usr/local/groundwork/foundation*

#Build Monitor Pro RPM

cd $BASE
maven allBuild
maven allDeploy

find /usr/local/groundwork -name .svn -exec rm -rf {} \;
find /usr/local/groundwork -name .project -exec rm -rf {} \;
find /usr/local/groundwork -name maven.xml -exec rm -rf {} \;
find /usr/local/groundwork -name project.xml -exec rm -rf {} \;

cd $BUILD_DIR
. set-permissions.sh

cd /usr/local/groundwork
$BUILD_DIR/rpm-filelist.pl > /usr/local/core-filelist

cd $BASE/monitor-os/spec/
chmod +x buildRPM.sh
. buildRPM.sh

cd $BUILD_DIR/../build
. checkBuild.sh 
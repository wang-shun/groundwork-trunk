#!/bin/bash
# description: monitor-core build script
#Copyright (C) 2004-2006  GroundWork Open Source Solutions info@itgroundwork.com
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
. downloadSources.sh

cd $BUILD_DIR
. buildBaseComponents.sh

BUILD_DIR=$BASE/build

cd $BUILD_DIR
. buildCore.sh

cd $BASE
maven allBuild
maven allDeploy

find /usr/local/groundwork -name .svn -exec rm -rf {} \;
find /usr/local/groundwork -name .project -exec rm -rf {} \;
find /usr/local/groundwork -name maven.xml -exec rm -rf {} \;
find /usr/local/groundwork -name project.xml -exec rm -rf {} \;

cd $BASE/monitor-os/spec/
chmod +x buildRPM.sh

. buildRPM.sh
#. checkBuild.sh

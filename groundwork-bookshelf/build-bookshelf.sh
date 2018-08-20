#!/bin/bash
# description: monitor-bookshelf build script
#
# Copyright 2008 GroundWork Open Source, Inc. (âGroundWorkâ)
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
#

BASE=/home/nagios/groundwork-professional
BUILD_DIR=$BASE/bookshelf

rm -rf /usr/local/groundwork

cd $BUILD_DIR
maven allBuild
maven allDeploy

find /usr/local/groundwork -name .svn -exec rm -rf {} \;
find /usr/local/groundwork -name .project -exec rm -rf {} \;
find /usr/local/groundwork -name maven.xml -exec rm -rf {} \;
find /usr/local/groundwork -name project.xml -exec rm -rf {} \;

cd $BUILD_DIR/spec/
chmod +x buildRPM.sh

. buildRPM.sh

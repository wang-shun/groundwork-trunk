#!/bin/sh
##
##	build_cacti_ping_executor.sh
##
##	Daniel Emmanuel Feinsmith
##	Groundwork Open Source
##
##	Modification History
##
##		Created 2/15/08
##
##	Method:
##		1. Initialize
##		2. Prepare
##		3. Build
##		4. Install
##		5. Make RPM
##		6. Copy RPM to Distribution Location.
##

source ./error_handling.sh

#
#	Set global variables.
#

CACTIDIR=$NMSDIR/applications/cacti
COMPONENT=cacti_ping_executor
INSTALLDIR=$CACTIDIR/cli
BUILD_ROOT=`pwd`/../proprietary/$COMPONENT

#
#	Preface Banner
#

echo "======================================================================"
echo "NMS Build System"
echo "----------------"
echo "  Building: $COMPONENT"
echo "  Build Root: $BUILD_ROOT"
echo

#
#	Step 1: Initialize
#	=============
#	Remove artifacts from previous build.
#

echo "  Initializing."
CWD=`pwd`
cd $BUILD_ROOT
bomb_out "cacti_ping_executor chdir to $BUILD_ROOT"

#
#	Step 2: Prepare
#	===============
#	Untar the files in our build directory and
#	prepare the source files for building
#

make clean
bomb_out "cacti_ping_executor make clean"
make
bomb_out "cacti_ping_executor make"
make install
bomb_out "cacti_ping_executor make install"
cp -f $CWD/cacti/host.php $CACTIDIR/host.php
bomb_out "cacti copy of host.php"

#
#	Closing Banner
#

echo "======================================================================"
cd $CWD
exit

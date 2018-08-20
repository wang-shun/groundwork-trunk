#!/bin/sh
##
##	build_enterprise.sh
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

DEPLOYDIR=$GWDIR
BUILD_ROOT=`pwd`/..
COMPONENT=enterprise
INSTALLERDIR=$DEPLOYDIR/nms/tools/installer

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

#
#	Step 2: Prepare
#	===============
#	Untar the files in our build directory and
#	prepare the source files for building
#

echo "  Installing $COMPONENT"
cp -r $BUILD_ROOT/proprietary/$COMPONENT $DEPLOYDIR
chmod 700 $DEPLOYDIR/enterprise/config/enterprise.properties
bomb_out "enterprise recursive copy of $$BUILD_ROOT/proprietary/COMPONENT"

mkdir $INSTALLERDIR
cp -rf $BUILD_ROOT/proprietary/enterprise/bin/components/cacti $INSTALLERDIR
cp -rf $BUILD_ROOT/proprietary/enterprise/bin/components/guava-packages $INSTALLERDIR
cp -rf $BUILD_ROOT/proprietary/enterprise/bin/components/httpd $INSTALLERDIR
cp -rf $BUILD_ROOT/proprietary/enterprise/bin/components/nedi $INSTALLERDIR
cp -rf $BUILD_ROOT/proprietary/enterprise/bin/components/ntop $INSTALLERDIR
cp -rf $BUILD_ROOT/proprietary/enterprise/bin/components/plugins $INSTALLERDIR
chmod 700 $INSTALLERDIR

#
#	Step 5: Make RPM
#	================
#	Build the RPM
#

#
#	Step 6: Install RPM
#	===================
#	Copy the RPM to its distribution directory.
#

echo "  Done."

#
#	Closing Banner
#

echo "======================================================================"
cd $CWD
exit

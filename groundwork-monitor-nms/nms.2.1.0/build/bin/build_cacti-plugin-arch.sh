#!/bin/bash
##
##	build_cacti-plugin-arch.sh
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

COMPONENT=cacti-plugin-arch
TARBALL=$COMPONENT.tar.gz
INSTALLDIR=$NMSDIR/applications/$COMPONENT
CACTIDIR=$NMSDIR/applications/cacti
PATCHFILE=cacti-plugin-0.8.7b-PA-v2.0.diff
BUILD_ROOT=`pwd`/..

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
bomb_out "cacti-plugin-arch chdir to $BUILD_ROOT"
if [ ! -d tmp ]
then
	echo "    Making Temporary Build Directory."
	mkdir tmp
	bomb_out "cacti-plugin-arch mkdir tmp"
else
	if [ -d tmp/$COMPONENT ]
	then
		echo "    Cleaning previous build."
		rm -rf tmp/$COMPONENT
	fi
fi

#
#	Step 2: Prepare
#	===============
#	Untar the files in our build directory and
#	prepare the source files for building
#

echo "  Preparing source files from opensource."
cd tmp
tar xzf "../opensource/$TARBALL"
bomb_out "cacti-plugin-arch un-tar"

#
#	Step 3: Build
#	=============
#	Build the binary from the source.
#

echo "  Patching Cacti."
#cp -r $COMPONENT/files-0.8.7b/* $CACTIDIR
cp $COMPONENT/$PATCHFILE $CACTIDIR
bomb_out "cacti-plugin-arch patch file copy"
COMPONENTDIR=`pwd`/$COMPONENT
cd $CACTIDIR
bomb_out "cacti-plugin-arch chdir to $CACTIDIR"
DIRNOW=`pwd`

#
#	Step 4: Install
#	===============
#	Install the package to where it needs to
#	be.
#

echo "  Patching Cacti for Plugin Architecture, patch file: $PATCHFILE"
patch -p1 -N <$PATCHFILE
#bomb_out "cacti-plugin-arch patch from $PATCHFILE"
cp $CWD/cacti-plugin-arch/plugins.php $CACTIDIR/include
bomb_out "cacti-plugin-arch copy of plugins.php"

echo "  Fixing file permissions."
chown -R nagios:nagios $CACTIDIR
bomb_out "cacti-plugin-arch recursive chown"

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


#
#	Closing Banner
#

echo "======================================================================"
cd $CWD
exit

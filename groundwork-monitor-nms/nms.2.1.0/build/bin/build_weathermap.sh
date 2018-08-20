#!/bin/sh
##
##	build_cacti.sh
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
COMPONENT=weathermap
TARBALL=php-weathermap-0.941.zip
INSTALLDIR=$NMSDIR/applications/$COMPONENT
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
bomb_out "weathermap chdir to $BUILD_ROOT"
if [ ! -d tmp ]
then
	echo "    Making Temporary Build Directory."
	mkdir tmp
	bomb_out "weathermap mkdir tmp"
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
bomb_out "weathermap chdir to tmp"
unzip "../opensource/$TARBALL"
bomb_out "weathermap unzip"

#
#	Step 3: Build
#	=============
#	Build the binary from the source.
#	There is nothing to do here for Cacti.
#

echo "  Patching $COMPONENT"
sed "s|/usr/local/bin/php|$NMSDIR/tools/php/bin/php|g" -i weathermap/weathermap
sed "s|/usr/local/bin/rrdtool|$NMSDIR/tools/rrdtool/bin/rrdtool|g" -i weathermap/weathermap

#
#	Step 4: Install
#	===============
#	Install the package to where it needs to
#	be.
#

echo "  Installing $COMPONENT"
cp -rf $COMPONENT $INSTALLDIR
bomb_out "weathermap recursive copy of $COMPONENT"
chown -R nagios:nagios $INSTALLDIR
bomb_out "weathermap recursive chown"

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

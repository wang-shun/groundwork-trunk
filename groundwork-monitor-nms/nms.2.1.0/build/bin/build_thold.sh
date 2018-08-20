#!/bin/sh
##
##	build_thold.sh
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
COMPONENT=thold-0.3.9
COMPONENT_CLEANNAME=thold
TARBALL=$COMPONENT.tar.gz
INSTALLDIR=$CACTIDIR/plugins
LIBDIR=$INSTALLDIR/lib
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
bomb_out "thold chdir to $BUILD_ROOT"
if [ ! -d tmp ]
then
	echo "    Making Temporary Build Directory."
	mkdir tmp
	bomb_out "thold mkdir tmp"
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
bomb_out "thold chdir to tmp"
tar xzvf "../opensource/$TARBALL"
bomb_out "thold un-tar"

#
#	Step 3: Build
#	=============
#	Build the binary from the source.
#	There is nothing to do here for Cacti.
#

#
#	Step 4: Install
#	===============
#	Install the package to where it needs to
#	be.
#

echo "  Installing $COMPONENT"
cp -rf $COMPONENT_CLEANNAME $INSTALLDIR			|| bomb_out "thold recursive copy of $COMPONENT_CLEANNAME"
chown -R nagios:nagios $INSTALLDIR			|| bomb_out "thold recursive chown"
CONFIGFILE=$CACTIDIR/include/config.php
grep -v "?>" $CONFIGFILE      > /tmp/build_thold.tmp	|| bomb_out "thold creation of /tmp/build_thold.tmp"
echo "\$plugins[] = 'thold';" >>/tmp/build_thold.tmp	|| bomb_out "thold extension to /tmp/build_thold.tmp"
echo "?>"                     >>/tmp/build_thold.tmp	|| bomb_out "thold finishing of /tmp/build_thold.tmp"
cp -f /tmp/build_thold.tmp $CONFIGFILE			|| bomb_out "thold copy of /tmp/build_thold.tmp"
rm -f /tmp/build_thold.tmp

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

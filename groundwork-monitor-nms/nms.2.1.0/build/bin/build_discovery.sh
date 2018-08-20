#!/bin/sh
##
##	build_discovery.sh
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
COMPONENT=discovery-0.8.4
COMPONENT_CLEANNAME=discovery
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
bomb_out "discovery chdir to $BUILD_ROOT"
if [ ! -d tmp ]
then
	echo "    Making Temporary Build Directory."
	mkdir tmp
	bomb_out "discovery mkdir tmp"
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
bomb_out "discovery chdir to tmp"
tar xzvf "../opensource/$TARBALL"
bomb_out "discovery un-tar"

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
cp -rf $COMPONENT_CLEANNAME $INSTALLDIR			|| bomb_out "discovery recursive copy of $COMPONENT_CLEANNAME"
chown -R nagios:nagios $INSTALLDIR			|| bomb_out "discovery recursive chown"
CONFIGFILE=$CACTIDIR/include/config.php
grep -v "?>" $CONFIGFILE      > /tmp/build_discovery.tmp	|| bomb_out "discovery creation of /tmp/build_discovery.tmp"
echo "\$plugins[] = 'discovery';" >>/tmp/build_discovery.tmp	|| bomb_out "discovery extension to /tmp/build_discovery.tmp"
echo "?>"                     >>/tmp/build_discovery.tmp	|| bomb_out "discovery finishing of /tmp/build_discovery.tmp"
cp -f /tmp/build_discovery.tmp $CONFIGFILE			|| bomb_out "discovery copy of /tmp/build_discovery.tmp"
rm -f /tmp/build_discovery.tmp

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

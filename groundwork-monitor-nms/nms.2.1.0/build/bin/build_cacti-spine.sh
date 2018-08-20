#!/bin/bash
##
##	build_cacti_spine.sh
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

COMPONENT=cacti-spine-0.8.7a
COMPONENT_CLEANNAME=cacti-spine
TARBALL=$COMPONENT.tar.gz
INSTALLDIR=$NMSDIR/applications/$COMPONENT_CLEANNAME
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
bomb_out "cacti-spine chdir to $BUILD_ROOT"
if [ ! -d tmp ]
then
	echo "    Making Temporary Build Directory."
	mkdir tmp
	bomb_out "cacti-spine mkdir tmp"
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
bomb_out "cacti-spine chdir to tmp"
tar xzf "../opensource/$TARBALL"
bomb_out "cacti-spine un-tar"

#
#	Step 3: Build
#	=============
#	Build the binary from the source.
#

cd $COMPONENT
bomb_out "cacti-spine chdir to $COMPONENT"
/usr/bin/aclocal
bomb_out "cacti-spine aclocal"
/usr/bin/libtoolize --force
bomb_out "cacti-spine libtoolize"
/usr/bin/autoheader
bomb_out "cacti-spine autoheader"
/usr/bin/autoconf
bomb_out "cacti-spine autoconf"
/usr/bin/automake
bomb_out "cacti-spine automake"
./configure --prefix=$INSTALLDIR --libdir=$LIBDIR --with-snmp=$NMSDIR/tools/net-snmp LDFLAGS="-L$NMSDIR/tools/net-snmp/lib"
bomb_out "cacti-spine configure"
make
bomb_out "cacti-spine make"

#
#	Step 4: Install
#	===============
#	Install the package to where it needs to
#	be.
#

echo "  Installing $COMPONENT"
make install
bomb_out "cacti-spine make install"
chown -R nagios:nagios $INSTALLDIR
bomb_out "cacti-spine recursive chown"

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

#!/bin/bash
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

COMPONENT=cacti-0.8.7b
COMPONENT_CLEANNAME=cacti
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
bomb_out "cacti chdir to $BUILD_ROOT"
if [ ! -d tmp ]
then
	echo "    Making Temporary Build Directory."
	mkdir tmp
	bomb_out "cacti mkdir tmp"
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
bomb_out "cacti chdir to tmp"
tar xzf "../opensource/$TARBALL"
bomb_out "cacti un-tar"

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
cp -r $COMPONENT $INSTALLDIR
bomb_out "cacti recursive copy of $COMPONENT"
cp -f $CWD/cacti/auth.php $INSTALLDIR/include/auth.php
bomb_out "cacti copy of auth.php"
cp -f $CWD/cacti/ifoper.xml $INSTALLDIR/resource/snmp_queries/ifoper.xml
bomb_out "cacti copy of ifoper.xml"

CONFIGFILE=$INSTALLDIR/include/config.php
grep -v "?>" $CONFIGFILE      > /tmp/build_tmp.tmp    || bomb_out "creation of /tmp/build_tmp.tmp"
`echo "\\$plugins = array();" >>/tmp/build_tmp.tmp`;
echo "?>"                     >>/tmp/build_tmp.tmp    || bomb_out "finishing of /tmp/build_tmp.tmp"
cp -f /tmp/build_tmp.tmp $CONFIGFILE                  || bomb_out "copy of /tmp/build_tmp.tmp"
rm -f /tmp/build_tmp.tmp

chown -R nagios:nagios $INSTALLDIR
bomb_out "cacti recursive chown"
chmod 644 $INSTALLDIR/cacti.sql
#bomb_out "cacti chown cacti.sql"

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

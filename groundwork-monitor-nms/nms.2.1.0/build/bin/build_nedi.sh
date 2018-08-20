#!/bin/bash
##
##	build_nedi.sh
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

COMPONENT=nedi-1.0-rc6
COMPONENT_CLEANNAME=nedi
TARBALL=$COMPONENT.tgz
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
bomb_out "nedi chdir to $BUILD_ROOT"
if [ ! -d tmp ]
then
	echo "    Making Temporary Build Directory."
	mkdir tmp
	bomb_out "nedi mkdir tmp"
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

echo "  Preparing source files from opensource, tarball=$TARBALL."
cd tmp
bomb_out "nedi chdir tmp"
tar xzf "../opensource/$TARBALL"
bomb_out "nedi un-tar"
rm -f $COMPONENT_CLEANNAME/contrib/renedi.pl

#
#	Step 3: Build
#	=============
#	Build the binary from the source.
#	There is nothing to do here for Nedi.
#

echo "  Patching."


#
#	Step 4: Install
#	===============
#	Install the package to where it needs to
#	be.
#

echo "  Installing $COMPONENT"
cp -r $COMPONENT_CLEANNAME $INSTALLDIR
bomb_out "nedi recursive copy of $COMPONENT_CLEANNAME"
sed "s/DBI:mysql:mysql:\$misc::dbhost/DBI:mysql:host=localhost/g" -i $INSTALLDIR/inc/libdb-msq.pl
sed "s/\$nedihost\\\' IDENTIFIED/%\\\' IDENTIFIED/g" -i $INSTALLDIR/inc/libdb-msq.pl
sed "s/\$nedihost\\\' = OLD/%\\\' = OLD/g" -i $INSTALLDIR/inc/libdb-msq.pl
sed "s|rrdcmd  =.*|rrdcmd = \"$NMSDIR/tools/rrdtool/bin/rrdtool\";|g" -i $INSTALLDIR/html/inc/libgraph.php
sed "s|rrdcmd =.*|rrdcmd = \"$NMSDIR/tools/rrdtool/bin/rrdtool\";|g" -i $INSTALLDIR/html/inc/libgraph.php
sed "s|rrdpath =.*|rrdpath = \"$INSTALLDIR/rrd\";|g" -i $INSTALLDIR/html/inc/libgraph.php


chown -R nagios:nagios $INSTALLDIR
bomb_out "nedi recursive chown"

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

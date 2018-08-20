#!/bin/bash
##
##	build_php.sh
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

COMPONENT=perl-5.8.8
COMPONENT_CLEANNAME=perl
TARBALL=$COMPONENT.tar.gz
INSTALLDIR=$NMSDIR/tools/$COMPONENT_CLEANNAME
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
bomb_out "perl chdir to $BUILD_ROOT"
if [ ! -d tmp ]
then
	echo "    Making Temporary Build Directory."
	mkdir tmp
	bomb_out "perl mkdir tmp"
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
bomb_out "perl chdir to tmp"
tar xzf "../opensource/$TARBALL"
bomb_out "perl un-tar"

#
#	Step 3: Build
#	=============
#	Build the binary from the source.
#

cd $COMPONENT
bomb_out "perl chdir to $COMPONENT"
echo >/tmp/build_perl.tmp
bomb_out "perl creation of /tmp/build_perl.tmp"
./Configure -de -Ui_db -Ui_dbm -Ui_ndbm -Dprefix=$NMSDIR/tools/perl -Dusethreads -Duseshrplib D -shared -lm </tmp/build_perl.tmp
bomb_out "perl configure"
make
bomb_out "perl make"
rm -f /tmp/build_perl.tmp
rm -rf lib/auto/DB_File lib/auto/NDBM_File
#find . -name "GDBM_*" -exec rm -rf {} \;

#
#	Step 4: Install
#	===============
#	Install the package to where it needs to
#	be.
#

echo "  Installing $COMPONENT"
make install
bomb_out "perl make install"
chown -R nagios:nagios $INSTALLDIR
bomb_out "perl recursive chown"

#
#	Closing Banner
#

echo "======================================================================"
cd $CWD
exit

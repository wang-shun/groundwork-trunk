#!/bin/bash
##
##	build_httpd.sh
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


COMPONENT=mod_auth_tkt-2.0.0rc3
TARBALL=$COMPONENT.tar.gz
INSTALLDIR=$NMSDIR/tools/httpd/modules
LIBDIR=$NMSDIR/lib
BUILD_ROOT=`pwd`/..

XML2=libxml2-2.6.26
LIBX2_INSTALLDIR=XML2

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

echo "  Initializing5."
CWD=`pwd`
cd $BUILD_ROOT
bomb_out "mod_auth_tkt chdir to $BUILD_ROOT"
if [ ! -d tmp ]
then
	echo "    Making Temporary Build Directory."
	mkdir tmp
	bomb_out "mod_auth_tkt mkdir tmp"
else
	if [ -d tmp/$COMPONENT ]
	then
		echo "    Cleaning previous build."
		rm -rf tmp/$COMPONENT
	fi
	if [ -d tmp/$XML2 ]
	then
		echo "    Cleaning previous build."
		rm -rf tmp/$XML2
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
bomb_out "mod_auth_tkt chdir to tmp"
tar xzf "../opensource/$TARBALL"
bomb_out "mod_auth_tkt un-tar"
tar xzf "../opensource/$XML2.tar.gz"
bomb_out "xml2 un-tar"

#
#	Step 3: Build
#	=============
#	Build the binary from the source.
#

cd $XML2
bomb_out "xml2 chdir to $$XML2"
./configure --prefix=$NMSDIR/tools/httpd/xml2
make
bomb_out "mod_auth_tkt make"
make install
bomb_out "mod_auth_tkt make install"

echo "../$COMPONENT"
cd ../$COMPONENT
bomb_out "mod_auth_tkt chdir to $COMPONENT"
echo "./configure --apxs=$NMSDIR/tools/httpd/bin/apxs --apachever=2.2"
./configure --apxs=$NMSDIR/tools/httpd/bin/apxs --apachever=2.2
bomb_out "mod_auth_tkt configure"
make
bomb_out "mod_auth_tkt make"

#
#	Step 4: Install
#	===============
#	Install the package to where it needs to
#	be.
#

echo "  Installing $COMPONENT."
cp src/.libs/mod_auth_tkt.so $INSTALLDIR
bomb_out "mod_auth_tkt copy of src/.libs/mod_auth_tkt.so"
chown -R nagios:nagios $INSTALLDIR
bomb_out "mod_auth_tkt recursive chown"

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

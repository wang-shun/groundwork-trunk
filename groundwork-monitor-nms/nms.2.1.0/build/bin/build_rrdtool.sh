#!/bin/bash
##
##	build_rrdtool.sh
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

COMPONENT=rrdtool-1.2.27
TARBALL=$COMPONENT.tar.gz
INSTALLDIR=$NMSDIR/tools/rrdtool
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
bomb_out "rrdtool chdir to $BUILD_ROOT"
if [ ! -d tmp ]
then
	echo "    Making Temporary Build Directory."
	mkdir tmp
	bomb_out "rrdtool mkdir tmp"
else
	if [ -d tmp/$COMPONENT ]
	then
		echo "    Cleaning previous build."
		rm -rf tmp/$COMPONENT
	fi
fi

# Header.

echo "  Preparing source files from opensource."
cd tmp

#
#       Build Dependencies
#

if [ -d $INSTALLDIR ]
then
        rm -rf $INSTALLDIR
fi
mkdir $INSTALLDIR

# zlib

ZLIB=zlib-1.2.3
tar xzf ../opensource/$ZLIB.tar.gz
bomb_out "zlib un-tar"
cd $ZLIB
env CFLAGS="-O3 -fPIC" ./configure --prefix=$INSTALLDIR/lb
bomb_out "zlib configure"
make
bomb_out "zlib make"
make install
bomb_out "zlib make install"
cd ..

# libpng

LIBPNG=libpng-1.2.10
tar zxvf ../opensource/$LIBPNG.tar.gz
bomb_out "libpng un-tar"
cd $LIBPNG
env CPPFLAGS="-I$INSTALLDIR/lb/include" LDFLAGS="-L$INSTALLDIR/lb/lib" CFLAGS="-O3 -fPIC" ./configure --prefix=$INSTALLDIR/lb
bomb_out "libpng configure"
make
bomb_out "libpng make"
make install
bomb_out "libpng make install"
cd ..

# freetype

FREETYPE=freetype-2.1.10
tar jxvf ../opensource/$FREETYPE.tar.bz2
bomb_out "freetype un-tar"
cd $FREETYPE
env CPPFLAGS="-I$INSTALLDIR/lb/include" LDFLAGS="-L$INSTALLDIR/lb/lib" CFLAGS="-O3 -fPIC"  ./configure --prefix=$INSTALLDIR/lb
bomb_out "freetype configure"
make
bomb_out "freetype make"
make install
bomb_out "freetype make install"
cd ..

# libart

LIBART=libart_lgpl-2.3.17
tar zxvf ../opensource/$LIBART.tar.gz
bomb_out "libart un-tar"
cd $LIBART
env CFLAGS="-O3 -fPIC" ./configure --prefix=$INSTALLDIR/lb
bomb_out "libart configure"
make
bomb_out "libart make"
make install
bomb_out "libart make install"
cd ..

#
#	Step 2: Prepare
#	===============
#	Untar the files in our build directory and
#	prepare the source files for building
#

tar xzf "../opensource/$TARBALL"
bomb_out "rrdtool un-tar"

#
#	Step 3: Build
#	=============
#	Build the binary from the source.
#

cd $COMPONENT

CPPFLAGS="-I$NMSDIR/tools/rrdtool/lib/include -I$NMSDIR/tools/rrdtool/lb/include/libart-2.0 -I$NMSDIR/tools/rrdtool/lb/include/freetype2 -I$NMSDIR/tools/rrdtool/lb/include/libpng12 -L$NMSDIR/tools/rrdtool/lb/lib"
LDFLAGS=-L$NMSDIR/tools/rrdtool/lb/lib
export CPPFLAGS
export LDFLAGS
echo $CPPFLAGS
echo $LDFLAGS
./configure --prefix=$NMSDIR/tools/rrdtool --libdir=$NMSDIR/tools/rrdtool/lib --with-gnu-ld --enable-perl-site-install --disable-tcl --disable-python --disable-perl
bomb_out "rrdtool configure"
make
bomb_out "rrdtool make"

#
#	Step 4: Install
#	===============
#	Install the package to where it needs to
#	be.
#

echo "  Installing $COMPONENT."
make install
bomb_out "rrdtool make install"
rm -rf $INSTALLDIR/share/rrdtool/examples
chown -R nagios:nagios $INSTALLDIR
bomb_out "rrdtool recursive chown"

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

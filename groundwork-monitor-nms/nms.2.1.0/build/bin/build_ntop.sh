#!/bin/bash
##
##	build_ntop.sh
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
##

source ./error_handling.sh

#
#	Set global variables.
#

COMPONENT=ntop
COMPONENT_FULLNAME=ntop
TARBALL=ntop-3.3.6.tar.gz
LIBPCAP=libpcap-0.9.8
INSTALLDIR=$NMSDIR/applications/$COMPONENT
LIBDIR=$INSTALLDIR/lib
RRDDIR=$NMSDIR/tools/rrdtool
BUILD_ROOT=`pwd`/..

#
#	Determine architecture so as to
#	use the correct password bootstrap ntop database.
#

uname -p | grep "x86_64" > /dev/null
if [ $? == 1 ]; then
	NTOP_PW_DB="ntop_pw.db"
	HTTP_LIBDIR=lib
else
	NTOP_PW_DB="ntop_pw_64.db"
	HTTP_LIBDIR=lib64
fi

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
#	Initialize
#	=============
#	Remove artifacts from previous build.
#

echo "  Initializing."
CWD=`pwd`
cd $BUILD_ROOT
bomb_out "ntop chdir to $BUILD_ROOT"
if [ ! -d tmp ]
then
	echo "    Making Temporary Build Directory."
	mkdir tmp
	bomb_out "ntop mkdir tmp"
else
	if [ -d tmp/$COMPONENT ]
	then
		echo "    Cleaning previous build."
		rm -rf tmp/$COMPONENT
	fi
	if [ -d tmp/$LIBPCAP ]
	then
		echo "    Cleaning previous build dependencies."
		rm -rf tmp/$LIBPCAP
	fi
fi

#
#	Prepare
#	===============
#	Untar the files in our build directory and
#	prepare the source files for building
#

echo "  Preparing source files from opensource."
cd tmp
bomb_out "ntop chdir to tmp"
tar xzf "../opensource/$LIBPCAP.tar.gz"
bomb_out "libpcap un-tar"
tar xzf "../opensource/$TARBALL"
bomb_out "ntop un-tar"

#
#	Build
#	=============
#	Build the binary from the source.
#

#
#	First, build the pcap dependency.
#

NTOP_BUILD_DIR=`pwd`/ntop
cd $LIBPCAP
PCAP_ROOT=`pwd`
bomb_out "ntop:libpcap chdir to $LIBPCAP"
if [ ! -d $INSTALLDIR ]
then
	mkdir -p $INSTALLDIR
	bomb_out "ntop:libpcap mkdir $INSTALLDIR"
fi

if [ ! -d $LIBDIR ]
then
	mkdir -p $LIBDIR
	bomb_out "ntop:libpcap mkdir $LIBDIR"
fi
echo "  Patching Dependencies"
patch fad-getad.c ../../bin/ntop/fad-getad.patch
bomb_out "ntop:libpcap patch to fad-getad.c"
echo "  Configuring Dependencies"
./configure --libdir=$LIBDIR --includedir=$NTOP_BUILD_DIR
bomb_out "ntop:libpcap configure"
make
bomb_out "ntop:libpcap make"
#==== BELOW FOR USING LIBPCAP SHARED OBJECT. We're dynamically linking,
#==== so do not require it.
#make shared
#bomb_out "ntop:libpcap make"
#make install-shared
#bomb_out "ntop:libpcap make install"
#pushd $LIBDIR
#ln -s libpcap.so.0.9.8 libpcap.so
#popd
#====
cd ..

#
#	Next, build ntop itself.
#

cd $COMPONENT_FULLNAME
echo "  Configuring $COMPONENT"
bomb_out "ntop chdir to $COMPONENT"
./autogen.sh --with-rrd-home=$RRDDIR --with-pcap-root=$PCAP_ROOT --prefix=$INSTALLDIR --libdir=$LIBDIR --with-ossl-root=$NMSDIR/tools/httpd/ssl LDFLAGS="-L$LIBDIR -L$NMSDIR/tools/rrdtool/lib -L$NMSDIR/tools/rrdtool/lb/lib" CPPFLAGS="-I$NMSDIR/tools/net-snmp/include -L$NMSDIR/tools/rrdtool/lib -L$NMSDIR/tools/rrdtool/lb/lib -L$LIBDIR"
bomb_out "ntop autogen"
patch http.c ../../bin/ntop/traffic_map.patch
make
bomb_out "ntop make"
echo "  Installing $COMPONENT"
make install
bomb_out "ntop make install"
cp -f $NMSDIR/tools/rrdtool/lib/librrd* $LIBDIR
cp -f $NMSDIR/tools/rrdtool/lb/lib/libpng* $LIBDIR
cp -f $NMSDIR/tools/httpd/$HTTP_LIBDIR/libssl* $LIBDIR
cp -f $NMSDIR/tools/httpd/$HTTP_LIBDIR/libcrypt* $LIBDIR
rm -f $INSTALLDIR/lib/ntop/plugins/remotePlugin.so
rm -f $INSTALLDIR/lib/libremotePlugin*
cd ..

#
#	Install
#	===============
#	Install the package to where it needs to
#	be.
#


echo "  Copying configuration files."
#cp -f $CWD/ntop/ntop.conf $INSTALLDIR/etc/ntop.conf || bomb_out "ntop copy of ntop.conf"
mkdir -p $INSTALLDIR/db
bomb_out "ntop mkdir $INSTALLDIR/db"
cp -f $CWD/ntop/$NTOP_PW_DB $INSTALLDIR/db/ntop_pw.db
bomb_out "ntop copy of $CWD/ntop/ntop_pw.db"
echo "  Setting ownership parameters."
chown -R nagios:nagios $INSTALLDIR
bomb_out "ntop recursive chown"

#
#	Closing Banner
#

echo "======================================================================"
cd $CWD
exit

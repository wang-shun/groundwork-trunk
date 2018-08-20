#!/bin/bash
##
##	build_net-snmp.sh
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

COMPONENT=net-snmp-5.4.1
COMPONENT_CLEANNAME=net-snmp
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
bomb_out "net-snmp chdir to $BUILD_ROOT"
if [ ! -d tmp ]
then
	echo "    Making Temporary Build Directory."
	mkdir tmp
	bomb_out "net-snmp mkdir tmp"
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
bomb_out "net-snmp chdir to tmp"

#
#       Build Dependencies
#

if [ -d $INSTALLDIR ]
then
        rm -rf $INSTALLDIR
fi
mkdir $INSTALLDIR

# zlib

BEECRYPT=beecrypt-4.1.2
tar xzf ../opensource/$BEECRYPT.tar.gz
bomb_out "beecrypt un-tar"
cd $BEECRYPT
./configure --prefix=$LIBDIR --libdir=$LIBDIR/lib64
bomb_out "beecrypt configure"
make
bomb_out "beecrypt make"
make install
bomb_out "beecrypt make install"
if [ -d $LIBDIR/lib ]; then
       rm -f $LIBDIR/lib/libbeecrypt_java*
else
       rm -f $LIBDIR/lib64/libbeecrypt_java*
       #mv $LIBDIR/lib64 $LIBDIR/lib
fi
cd ..

#
#	Step 2: Prepare
#	===============
#	Untar the files in our build directory and
#	prepare the source files for building
#

tar xzf "../opensource/$TARBALL"
bomb_out "net-snmp un-tar"

#
#	Step 3: Build
#	=============
#	Build the binary from the source.
#

cd $COMPONENT
bomb_out "net-snmp chdir to $COMPONENT"
./configure --prefix=$INSTALLDIR --libdir=$LIBDIR --with-default-snmp-version=2 --with-sys-contact=dfeinsmith@groundworkopensource.com --with-sys-location=Unknown --with-logfile=/var/log/snmpd.log --with-persistent-directory=/var/net-snmp
bomb_out "net-snmp configure"
make
bomb_out "net-snmp make"

#
#	Step 4: Install
#	===============
#	Install the package to where it needs to
#	be.
#

echo "  Installing $COMPONENT"
make install
bomb_out "net-snmp make install"
chown -R nagios:nagios $INSTALLDIR
bomb_out "net-snmp recursive chown"
rm -f $INSTALLDIR/share/snmp/mibs/.index
bomb_out "net-snmp rm of mibs/.index"

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

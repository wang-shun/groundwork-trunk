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
##

source ./error_handling.sh

#
#	Set global variables.
#

COMPONENT=php-5.2.0
COMPONENT_CLEANNAME=php
TARBALL=$COMPONENT.tar.gz
INSTALLDIR=$NMSDIR/tools/$COMPONENT_CLEANNAME
LIBDIR=$INSTALLDIR/lib
BUILD_ROOT=`pwd`/..
PNG_ROOT=$NMSDIR/tools/rrdtool/lb

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

echo "  Initializing6."
CWD=`pwd`
cd $BUILD_ROOT
bomb_out "php chdir to $BUILD_ROOT"
if [ ! -d tmp ]
then
	echo "    Making Temporary Build Directory."
	mkdir tmp
	bomb_out "php mkdir tmp"
else
	if [ -d tmp/$COMPONENT ]
	then
		echo "    Cleaning previous build."
		rm -rf tmp/$COMPONENT
	fi
fi

# If we need to, create a hyperlink for mysql under /usr/lib

if [ -d /usr/lib64 ]; then
	if [ ! -d /usr/lib/mysql ]; then
		ln -s /usr/lib64/mysql /usr/lib/mysql
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
bomb_out "php chdir to tmp"
tar xzf "../opensource/$TARBALL"
bomb_out "php un-tar"

#
#	Step 3: Build
#	=============
#	Build the binary from the source.
#

if [ -e /usr/lib64 ]; then
	PHP_LIBDIR=lib64
else
	PHP_LIBDIR=lib
fi

cd $COMPONENT
bomb_out "php chdir to $COMPONENT"
./buildconf --force
bomb_out "php buildconf"
./configure --prefix=$INSTALLDIR --exec-prefix=$INSTALLDIR --with-config-file-path=$INSTALLDIR/etc --with-mysql --with-apxs2=$NMSDIR/tools/httpd/bin/apxs --enable-force-cgi-redirect --with-mod_charset --enable-safe-mode --enable-shared --with-layout=GNU --enable-libxml --with-libxml-dir=$NMSDIR/tools/httpd/xml2 --enable-spl --with-regex=php --disable-ipv6 --enable-session --with-openssl=$NMSDIR/tools/httpd/ssl --with-zlib --enable-calendar --enable-ctype --with-freetype --enable-soap --enable-bcmath --with-snmp=$NMSDIR/tools/net-snmp --enable-ucd-snmp-hack --enable-sockets --with-jpeg --with-png --with-png-dir=$PNG_ROOT --with-gd --enable-gd --enable-gd-native-ttf --enable-pcre --with-freetype-dir=$NMSDIR/tools/rrdtool/lb
# --with-libdir=$PHP_LIBDIR
bomb_out "php configure"
make
bomb_out "php make"

#
#	Step 4: Install
#	===============
#	Install the package to where it needs to
#	be.
#

echo "  Installing $COMPONENT"
make install
bomb_out "php make install"

echo "  Performing Post-Install."
cd $CWD
bomb_out "php chdir to $CWD"
cp ./php/php.ini $INSTALLDIR/etc
bomb_out "php copy of ./php/php.ini"
if [ ! -d $INSTALLDIR/etc/php.d ]
then
        mkdir $INSTALLDIR/etc/php.d
	bomb_out "php mkdir $INSTALLDIR/etc/php.d"
fi

chown -R nagios:nagios $INSTALLDIR
bomb_out "php recursive chown"

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

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

source ./error_handling.sh

#
#	Are we building on/for a 64-bit platform?
#

uname -p | grep "x86_64" > /dev/null
BITS_64=$?

#
#	Set global variables.
#

COMPONENT=httpd-2.2.8
TARBALL=$COMPONENT.tar.gz
INSTALLDIR=$NMSDIR/tools/httpd
if [ $BITS_64 == 0 ]; then
	LIBDIR=$INSTALLDIR/lib64
	EXTRA_PRAGMAS="--enable-lib64"
else
	LIBDIR=$INSTALLDIR/lib
	EXTRA_PRAGMAS=""
fi
BUILD_ROOT=`pwd`/..
EXPAT=expat-2.0.1
LDAP=openldap-2.3.25
SSL=openssl-0.9.7m
EXPAT_INSTALLDIR=$INSTALLDIR/expat
LDAP_INSTALLDIR=$INSTALLDIR/ldap
SSL_INSTALLDIR=$INSTALLDIR/ssl

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

echo "  Initializing3."
CWD=`pwd`
cd $BUILD_ROOT
bomb_out "httpd chdir to $BUILD_ROOT"
if [ ! -d tmp ]
then
	echo "    Making Temporary Build Directory."
	mkdir tmp
	bomb_out "httpd mkdir tmp"
else
	if [ -d tmp/$COMPONENT ]
	then
		echo "    Cleaning previous build."
		rm -rf tmp/$COMPONENT
	fi
	if [ -d tmp/$LDAP ]
	then
		echo "    Cleaning previous build dependencies."
		rm -rf tmp/$LDAP
	fi
	if [ -d tmp/$SSL ]
	then
		echo "    Cleaning previous build dependencies."
		rm -rf tmp/$SSL
	fi
        if [ -d tmp/$EXPAT ]
        then
                echo "    Cleaning previous build dependencies."
                rm -rf tmp/$EXPAT
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
bomb_out "httpd chdir to tmp"
tar xzf "../opensource/$LDAP.tar.gz"
bomb_out "ldap un-tar"
tar xzf "../opensource/$SSL.tar.gz"
bomb_out "ssl un-tar"
tar xzf "../opensource/$EXPAT.tar.gz"
bomb_out "expat un-tar"
tar xzf "../opensource/$TARBALL"
bomb_out "httpd un-tar"

#
#	Build
#	=============
#	Build the binary from the source.
#

#
#	First, build the expat dependency.
#

HTTPD_BUILD_DIR=`pwd`/$COMPONENT

cd $LDAP
LDAP_ROOT=`pwd`
bomb_out "httpd:ldap chdir to $LDAP"
if [ ! -d $LDAP_INSTALLDIR ]
then
        mkdir -p $LDAP_INSTALLDIR
        bomb_out "httpd:ldap mkdir $LDAP_INSTALLDIR"
fi
echo "  Configuring ldap Dependencies"
./configure --prefix=$LDAP_INSTALLDIR
bomb_out "httpd:ldap configure"
make
make install
cd ..

cd $SSL
SSL_ROOT=`pwd`
bomb_out "httpd:ssl chdir to $SSL"
if [ ! -d $SSL_INSTALLDIR ]
then
        mkdir -p $SSL_INSTALLDIR
        bomb_out "httpd:ssl mkdir $SSL_INSTALLDIR"
fi
ARCH=`arch`
if [ $ARCH = "x86_64" ]
then
        PLATFORM=linux-x86_64
else
        PLATFORM=linux-pentium
fi
echo "  Configuring ssl Dependencies"
./Configure $PLATFORM shared --prefix=$SSL_INSTALLDIR 
bomb_out "httpd:ssl configure"
make
make install
cd ..

cd $EXPAT
EXPAT_ROOT=`pwd`
bomb_out "httpd:expat chdir to $EXPAT"
if [ ! -d $EXPAT_INSTALLDIR ]
then
        mkdir -p $EXPAT_INSTALLDIR
        bomb_out "httpd:expat mkdir $EXPAT_INSTLLDIR"
fi

echo "  Configuring expat Dependencies"
./configure --prefix=$EXPAT_INSTALLDIR
bomb_out "httpd:expat configure"
make
make install
cd ..

#
#	Next, build httpd
#

cd $COMPONENT
bomb_out "httpd chdir to $COMPONENT"
./configure --with-included-apr --prefix=$INSTALLDIR LDFLAGS="-L$LIBDIR -L/usr/local/lib" --with-z=$NMSDIR/tools/rrdtool/lb/lib --enable-ssl --enable-module=so --enable-rewrite=shared --enable-speling=shared --enable-v4-mapped --enable-exception-hook --enable-auth-dbm --enable-mime-magic --enable-proxy --enable-proxy-connect --enable-proxy-http --enable-proxy-html --with-pthread --with-jni --enable-mods-shared=all --enable-cgi --enable-logio --with-ldap --enable-ldap --with-authnz_ldap --enable-authnz_ldap --enable-snmp --libdir=$LIBDIR $EXTRA_PRAGMAS --with-expat=$EXPAT_INSTALLDIR --with-ldap-include=$INSTALLDIR/ldap/include --with-ldap-lib=$INSTALLDIR/ldap/lib --with-ssl=$SSL_INSTALLDIR
bomb_out "httpd configure"
make
bomb_out "httpd make"


#
#	Install
#	===============
#	Install the package to where it needs to
#	be.
#

echo "  Installing $COMPONENT."
make install
bomb_out "httpd make install"
cp $CWD/httpd/httpd.conf $INSTALLDIR/conf
bomb_out "httpd copy of httpd.conf"
cp -rf $SSL_INSTALLDIR/lib/* $LIBDIR
cp -rf $LDAP_INSTALLDIR/lib/* $LIBDIR
cp -rf $EXPAT_INSTALLDIR/lib/* $LIBDIR
chown -R nagios:nagios $INSTALLDIR
bomb_out "httpd recursive chown"

#
#	Closing Banner
#

echo "======================================================================"
cd $CWD
exit

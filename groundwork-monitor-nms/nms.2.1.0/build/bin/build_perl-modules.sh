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
##

source ./error_handling.sh

#
#	Set global variables.
#

COMPONENT=perl
INSTALLDIR=$NMSDIR/tools/$COMPONENT
BUILD_ROOT=`pwd`/..
PERL=$NMSDIR/tools/perl/bin/perl

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
bomb_out "perl-modules chdir to $BUILD_ROOT"
if [ ! -d tmp ]
then
	echo "    Making Temporary Build Directory."
	mkdir tmp
	bomb_out "perl-modules mkdir tmp"
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
cd tmp							|| bomb_out "perl-modules chdir tmp"
tar xzf "../opensource/Crypt-IDEA-1.08.tar.gz"  || bomb_out "perl-modules un-tar of Net-SSH"
tar xzf "../opensource/Net-SSH-Perl-1.30.tar.gz"        || bomb_out "perl-modules un-tar of Net-SSH"
# New for LWP
tar xzf "../opensource/libwww-perl-5.816.tar.gz"        || bomb_out "perl-modules un-tar of LWP"
tar xzf "../opensource/URI-1.37.tar.gz"        || bomb_out "perl-modules un-tar of URI-1.37"
tar xzf "../opensource/Compress-Zlib-2.015.tar.gz"        || bomb_out "perl-modules un-tar of Compress-Zlib"
# End New for LWP
tar xzf "../opensource/Convert-PEM-0.07.tar.gz"
tar xzf "../opensource/Crypt-DH-0.06.tar.gz"
tar xzf "../opensource/Crypt-DSA-0.14.tar.gz"
tar xzf "../opensource/Math-GMP-2.04.tar.gz"
tar xzf "../opensource/Math-Pari-2.010800.tar.gz"
tar xzf "../opensource/String-CRC32-1.4.tar.gz"
tar xzf "../opensource/Class-ErrorHandler-0.01.tar.gz"
tar xzf "../opensource/Convert-ASN1-0.21.tar.gz"
tar xzf "../opensource/Crypt-DES_EDE3-0.01.tar.gz"
tar xzf "../opensource/Data-Buffer-0.04.tar.gz"
tar xzf "../opensource/pari-2.1.7.tgz"
tar xzf "../opensource/Crypt-DES-2.05.tar.gz"		|| bomb_out "perl-modules un-tar of Crypt-DES"
tar xzf "../opensource/Algorithm-Diff-1.15.tar.gz"	|| bomb_out "perl-modules un-tar of Algorithm-Diff"
tar xzf "../opensource/Net-SNMP-5.2.0.tar.gz"		|| bomb_out "perl-modules un-tar of Net-SNMP"
tar xzf "../opensource/Net-Telnet-3.03.tar.gz"		|| bomb_out "perl-modules un-tar of Net-Telnet"
tar xzf "../opensource/Net-SSH-Perl-1.30.tar.gz"	|| bomb_out "perl-modules un-tar of Net-SSH"
tar xzf "../opensource/Net-Telnet-Cisco-1.10.tar.gz"	|| bomb_out "perl-modules un-tar of Net-Telnet-Cisco"
tar xzf "../opensource/DBI-1.602.tar.gz"		|| bomb_out "perl-modules un-tar of DBI"
tar xzf "../opensource/DBD-mysql-4.007.tar.gz"		|| bomb_out "perl-modules un-tar of DBD-mysql"
tar xzf "../opensource/Digest-HMAC-1.01.tar.gz"		|| bomb_out "perl-modules un-tar of Digest-HMAC"
tar xzf "../opensource/Digest-SHA1-2.11.tar.gz"		|| bomb_out "perl-modules un-tar of Digest-SHA1"
tar xzf "../opensource/FCGI-0.67.tar.gz"		|| bomb_out "perl-modules un-tar of FCGI"
tar xzf "../opensource/PlRPC-0.2020.tar.gz"		|| bomb_out "perl-modules un-tar of PlRPC-0.2020"
tar xzf "../opensource/Socket6-0.20.tar.gz"		|| bomb_out "perl-modules un-tar of Socket6-0.20"
tar xzf "../opensource/Tk-804.028.tar.gz"		|| bomb_out "perl-modules un-tar of Tk-804.028"
tar xzf "../opensource/Tk-Pod-0.9938.tar.gz"		|| bomb_out "perl-modules un-tar of Tk-Pod-0.9938"
tar xzf "../opensource/Config-General-2.38.tar.gz"      || bomb_out "perl-modules un-tar of Config-General-2.38"

#
#	Step 3: Build
#	=============
#	Build the binary from the source.
#

echo "  Building and Installing Perl Modules."
cd pari-2.1.7
./Configure --prefix=$INSTALLDIR
make
make install

# New for LWP
cd ../Compress-Zlib-2.015
$PERL Makefile.PL PREFIX=$INSTALLDIR || bomb-out "perl-modules perl make of Compress-Zlib"
make || bomb_out "perl modules make of Compress-Zlib"
make install || bomb-out "perl modules make install of Compress-Zlib"

cd ../URI-1.37
$PERL Makefile.PL PREFIX=$INSTALLDIR || bomb-out "perl-modules perl make of URI-1.37"
make || bomb-out "perl-modules make of URI-1.37"
make install || bomb-out "perl-modules make install of URI-1.37"

cd ../libwww-perl-5.816
$PERL Makefile.PL PREFIX=$INSTALLDIR || bomb-out "perl-modules perl make of libwww-perl-5.816"
make || bomb-out "perl-modules make of libwww-perl-5.816"
make install || bomb-out "perl-modules make install of libwww-perl-5.816"

# End New for LWP

cd ../Math-Pari-2.010800
$PERL Makefile.PL PREFIX=$INSTALLDIR Configure  || bomb_out "perl-modules perl make of Net-Telnet-Cisco"
#$PERL Makefile.PL PREFIX=$INSTALLDIR || bomb_out "perl-modules perl make of Net-Telnet-Cisco"
make                                    || bomb_out "perl-modules make of Net-Telnet-Cisco"
make install                            || bomb_out "perl-modules make install of Net-Telnet-Cisco"
cd ../Crypt-IDEA-1.08
$PERL Makefile.PL PREFIX=$INSTALLDIR    || bomb_out "perl-modules perl make of Net-Telnet-Cisco"
make                                    || bomb_out "perl-modules make of Net-Telnet-Cisco"
make install                            || bomb_out "perl-modules make install of Net-Telnet-Cisco"

cd ../Data-Buffer-0.04
$PERL Makefile.PL PREFIX=$INSTALLDIR    || bomb_out "perl-modules perl make of Net-Telnet-Cisco"
make                                    || bomb_out "perl-modules make of Net-Telnet-Cisco"
make install                            || bomb_out "perl-modules make install of Net-Telnet-Cisco"

cd ../Class-ErrorHandler-0.01
$PERL Makefile.PL PREFIX=$INSTALLDIR    || bomb_out "perl-modules perl make of Net-Telnet-Cisco"
make                                    || bomb_out "perl-modules make of Net-Telnet-Cisco"
make install                            || bomb_out "perl-modules make install of Net-Telnet-Cisco"

cd ../Convert-ASN1-0.21
$PERL Makefile.PL PREFIX=$INSTALLDIR    || bomb_out "perl-modules perl make of Net-Telnet-Cisco"
make                                    || bomb_out "perl-modules make of Net-Telnet-Cisco"
make install                            || bomb_out "perl-modules make install of Net-Telnet-Cisco"

cd ../Crypt-DES-2.05			|| bomb_out "perl-modules chdir to Crypt-DES"
$PERL Makefile.PL PREFIX=$INSTALLDIR	|| bomb_out "perl-modules perl make of Crypt-DES"
make					|| bomb_out "perl-modules make of Crypt-DES"
make install				|| bomb_out "perl-modules make install of Crypt-DES"

cd ../Crypt-DES_EDE3-0.01
$PERL Makefile.PL PREFIX=$INSTALLDIR    || bomb_out "perl-modules perl make of Net-Telnet-Cisco"
make                                    || bomb_out "perl-modules make of Net-Telnet-Cisco"
make install                            || bomb_out "perl-modules make install of Net-Telnet-Cisco"

cd ../Convert-PEM-0.07
$PERL Makefile.PL PREFIX=$INSTALLDIR    || bomb_out "perl-modules perl make of Net-Telnet-Cisco"
make                                    || bomb_out "perl-modules make of Net-Telnet-Cisco"
make install                            || bomb_out "perl-modules make install of Net-Telnet-Cisco"

cd ../Digest-SHA1-2.11	 		|| bomb_out "perl-modules chdir to Digest-SHA1"
$PERL Makefile.PL PREFIX=$INSTALLDIR	|| bomb_out "perl-modules perl make of Digest-SHA1"
make					|| bomb_out "perl-modules make of Digest-SHA1"
make install				|| bomb_out "perl-modules make install of Digest-SHA1"
cd ../Crypt-DH-0.06
$PERL Makefile.PL PREFIX=$INSTALLDIR    || bomb_out "perl-modules perl make of Net-Telnet-Cisco"
make                                    || bomb_out "perl-modules make of Net-Telnet-Cisco"
make install                            || bomb_out "perl-modules make install of Net-Telnet-Cisco"
cd ../Crypt-DSA-0.14
$PERL Makefile.PL PREFIX=$INSTALLDIR    || bomb_out "perl-modules perl make of Net-Telnet-Cisco"
make                                    || bomb_out "perl-modules make of Net-Telnet-Cisco"
make install                            || bomb_out "perl-modules make install of Net-Telnet-Cisco"
cd ../Math-GMP-2.04
$PERL Makefile.PL PREFIX=$INSTALLDIR    || bomb_out "perl-modules perl make of Net-Telnet-Cisco"
make                                    || bomb_out "perl-modules make of Net-Telnet-Cisco"
make install                            || bomb_out "perl-modules make install of Net-Telnet-Cisco"
cd ../String-CRC32-1.4
$PERL Makefile.PL PREFIX=$INSTALLDIR    || bomb_out "perl-modules perl make of Net-Telnet-Cisco"
make                                    || bomb_out "perl-modules make of Net-Telnet-Cisco"
make install                            || bomb_out "perl-modules make install of Net-Telnet-Cisco"

cd ../Algorithm-Diff-1.15		|| bomb_out "perl-modules chdir to Algorithm-Diff"
$PERL Makefile.PL PREFIX=$INSTALLDIR	|| bomb_out "perl-modules perl make of Algorithm-Diff"
make					|| bomb_out "perl-modules make of Algorithm-Diff"
make install				|| bomb_out "perl-modules make install of Algorithm-Diff"
cd ../Net-SNMP-5.2.0			|| bomb_out "perl-modules chdir to Net-SNMP"
$PERL Makefile.PL PREFIX=$INSTALLDIR	|| bomb_out "perl-modules perl make of Net-SNMP"
make					|| bomb_out "perl-modules make of Net-SNMP"
make install				|| bomb_out "perl-modules make install of Net-SNMP"
cd ../Net-Telnet-3.03			|| bomb_out "perl-modules chdir to Net-Telnet"
$PERL Makefile.PL PREFIX=$INSTALLDIR	|| bomb_out "perl-modules perl make of Net-Telnet"
make					|| bomb_out "perl-modules make of Net-Telnet"
make install				|| bomb_out "perl-modules make install of Net-Telnet"
cd ../Net-Telnet-Cisco-1.10		|| bomb_out "perl-modules chdir to Net-Telnet-Cisco"
$PERL Makefile.PL PREFIX=$INSTALLDIR	|| bomb_out "perl-modules perl make of Net-Telnet-Cisco"
make					|| bomb_out "perl-modules make of Net-Telnet-Cisco"
make install				|| bomb_out "perl-modules make install of Net-Telnet-Cisco"
cd ../Net-SSH-Perl-1.30			|| bomb_out "perl-modules chdir to Net-SSH-Perl"
echo "3" >/tmp/net_ssh.tmp
echo "1" >>/tmp/net_ssh.tmp
echo "n" >>/tmp/net_ssh.tmp
echo "n" >>/tmp/net_ssh.tmp
$PERL Makefile.PL PREFIX=$INSTALLDIR </tmp/net_ssh.tmp	|| bomb_out "perl-modules perl make of Net-Telnet-Cisco"
make 					|| bomb_out "perl-modules make of Net-Telnet-Cisco"
make install				|| bomb_out "perl-modules make install of Net-Telnet-Cisco"


cd ../DBI-1.602				|| bomb_out "perl-modules chdir to DBI"
$PERL Makefile.PL PREFIX=$INSTALLDIR	|| bomb_out "perl-modules perl make of DBI"
make					|| bomb_out "perl-modules make of DBI"
make install				|| bomb_out "perl-modules make install of DBI"
cd ../DBD-mysql-4.007			|| bomb_out "perl-modules chdir to DBD-mysql"
#$PERL Makefile.PL --embedded=/usr/lib64/mysql --libs=-L$NMSDIR/tools/rrdtool/lb/lib PREFIX=$INSTALLDIR       || bomb_out "perl-modules perl make of DBD-mysql"
$PERL Makefile.PL PREFIX=$INSTALLDIR	|| bomb_out "perl-modules perl make of DBD-mysql"
make					|| bomb_out "perl-modules make of DBD-mysql"
make install				|| bomb_out "perl-modules make install of DBD-mysql"

## New Modules:
cd ../Digest-HMAC-1.01	 		|| bomb_out "perl-modules chdir to Digest-HMAC"
$PERL Makefile.PL PREFIX=$INSTALLDIR	|| bomb_out "perl-modules perl make of Digest-HMAC"
make					|| bomb_out "perl-modules make of Digest-HMAC"
make install				|| bomb_out "perl-modules make install of Digest-HMAC"
cd ../FCGI-0.67		 		|| bomb_out "perl-modules chdir to FCGI-0.67"
$PERL Makefile.PL PREFIX=$INSTALLDIR	|| bomb_out "perl-modules perl make of FCGI-0.67"
make					|| bomb_out "perl-modules make of FCGI-0.67"
make install				|| bomb_out "perl-modules make install of FCGI-0.67"
cd ../PlRPC			 	|| bomb_out "perl-modules chdir to PlRPC-0.2020"
$PERL Makefile.PL PREFIX=$INSTALLDIR	|| bomb_out "perl-modules perl make of PlRPC-0.2020"
make					|| bomb_out "perl-modules make of PlRPC-0.2020"
make install				|| bomb_out "perl-modules make install of PlRPC-0.2020"
cd ../Socket6-0.20	 		|| bomb_out "perl-modules chdir to Socket6-0.20"
$PERL Makefile.PL PREFIX=$INSTALLDIR	|| bomb_out "perl-modules perl make of Socket6-0.20"
make					|| bomb_out "perl-modules make of Socket6-0.20"
make install				|| bomb_out "perl-modules make install of Socket6-0.20"
cd ../Tk-804.028		 	|| bomb_out "perl-modules chdir to Tk-804.028"
$PERL Makefile.PL --libs=-L$NMSDIR/tools/rrdtool/lb/lib PREFIX=$INSTALLDIR	|| bomb_out "perl-modules perl make of Tk-804.028"
cd PNG/libpng
./configure
make
cd ../..
make					|| bomb_out "perl-modules make of Tk-804.028"
make install				|| bomb_out "perl-modules make install of Tk-804.028"
cd ../Tk-Pod-0.9938	 		|| bomb_out "perl-modules chdir to Tk-Pod-0.9938"
$PERL Makefile.PL --cflags=-L$NMSDIR/tools/rrdtool/lb/lib --libs=-L$NMSDIR/tools/rrdtool/lb/libPREFIX=$INSTALLDIR	|| bomb_out "perl-modules perl make of Tk-Pod-0.9938"
make					|| bomb_out "perl-modules make of Tk-Pod-0.9938"
make install				|| bomb_out "perl-modules make install of Tk-Pod-0.9938"
## End New Modules

## Glenn H.'s TypedConfig and Dependencies {

cd ../Config-General-2.38		|| bomb_out "perl-modules chdir to Tk-Pod-0.9938"
$PERL Makefile.PL PREFIX=$INSTALLDIR	|| bomb_out "perl-modules perl make of Tk-Pod-0.9938"
make					|| bomb_out "perl-modules make of Tk-Pod-0.9938"
make install				|| bomb_out "perl-modules make install of Tk-Pod-0.9938"

## And TypedConfig, Glenn's module required for check_cacti.pl

`cp $CWD/cacti/TypedConfig.pm $NMSDIR/tools/perl/lib/5.8.8`;

## Glenn H.'s TypedConfig }

#
#	Step 4: Install
#	===============
#	Install the package to where it needs to
#	be.
#

echo "  Fixing file permissions."
chown -R nagios:nagios $INSTALLDIR
bomb_out "perl-modules recursive chown"

#
#	Closing Banner
#

echo "======================================================================"
cd $CWD
exit

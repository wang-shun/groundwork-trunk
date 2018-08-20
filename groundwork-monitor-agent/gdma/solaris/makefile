# Top-level GroundWork Distributed Monitoring Agent makefile.

# Copyright 2007 GroundWork Open Source, Inc. ("GroundWork").  All rights
# reserved.  Use is subject to GroundWork commercial license terms.

# Note:  You must use Sun make, not gmake, to run this makefile.
# Sun make is found in /usr/ccs/bin/make which means that /usr/ccs/bin
# should be in your command-search path.  Possibly, gmake might work
# as well, but we haven't tested for it.

#---------------------------------------------------------------#

# This value must be updated for each new release, and the value must match
# the version information defined in the released files, if any,
GDMA_RELEASE = 2.0.9

# Set this to reflect the version of the third-party code you wish to
# incorporate into your packages.
PLUGINS_RELEASE = 1.4.11

# THis is the top-level directory for all groundwork-related software,
# not just the GDMA package.
GROUNDWORK_PREFIX = /opt/groundwork

# Set this to reflect where you wish the compiled plugins to be installed;
# in actuality, the plugins themselves will be placed in the
# $(GDMA_PREFIX)/libexec/ directory.
GDMA_PREFIX = $(GROUNDWORK_PREFIX)/gdma

# If we wanted to label the packages with the build number, just like we do
# with RPMs, here's the build number we would use.
# SUBVERSION_BUILD :sh = svn info -r HEAD | fgrep Revision | gawk '{print $2}'

# This date can be useful for tagging specific releases.
BUILD_TIME :sh = date +'%Y-%m-%d.%R'
GDMA_TIME_PSTAMP    :sh = date +'gdma%Y%m%d%H%M%S'
GDMAKEY_TIME_PSTAMP :sh = date +'gdmakey%Y%m%d%H%M%S'

# This package name is also the name of a directory where we'll construct the
# tree of files for the package.
GDMA_PACKAGE_ABBREV	= GWOSgdma

GDMA_PACKAGE_NAME	= GroundWork Distributed Monitoring Agent

# For Solaris 10 and above, this package base name is also the initial substring
# of the directory name where we'll construct the tree of files for the package.
# We will append the server name to this base to form the complete package and
# directory names.  That allows us to avoid accidentally mixing files from
# different builds targeted at different customers.
GDMAKEY_PACKAGE_ABBREV_BASE	= GWOSgdmakey

# For Solaris 9 and below, we have to restrict the name of a package to no more
# than 9 characters.  Apparently this restriction was lifted in Solaris 9 12/03,
# but we have no documentation to prove that the extended package names were
# available in earlier releases of Solaris 9.  So there is no ambiguity about
# what's getting built and where it's getting built, We'll still append the
# server name to this package abbreviation to construct both our build directory
# name and the final package filename; it's only the internal package itself
# that will be constrained to 9 characters.
GDMAKEY_PACKAGE_SHORT_ABBREV	= GWOSgdmak

# This directory must be established as the home directory for the gdma user
# on all the machines where the GDMA packages are installed.
GDMA_HOME_DIR = $(GROUNDWORK_PREFIX)/home/gdma

ARCH :sh = uname -p

# We only deal with Solaris 2.6 and above (Solaris 7, 8, 9, 10, ...), and not
# the older Solaris 2.5 or 2.5.1 releases, so this simple pattern matching
# suffices, producing "6" for Solaris 2.6 (SunOS 5.6) even though that was
# never the marketing designation.
SOLARIS_RELEASE :sh = uname -r | sed 's/.*\.//'

#---------------------------------------------------------------#

default		: usage

usage		:
		@echo
		@echo 'Useful targets here are:'
		@echo '    make gdma_package'
		@echo 'which makes the GWOSgdma package for the same platform you"re building on, and'
		@echo '    make gdmakey_package'
		@echo 'which makes the GWOSgdmakey-{servername} package for a particular customer.'
		@echo 'For the latter, you will be prompted for the server name and IP address.'
		@echo 'The usual "make clean" and "make distclean" targets are also available.'
		@echo
		@echo 'Note that Solaris 2.6 does not have a native perl installed, you can obtain'
		@echo 'one from www.sunfreeware.com .  Solaris 2.6 also does not have a native'
		@echo 'ssh-keygen installed; for this platform, you should probably just rename a'
		@echo 'gdmakey package built on a later release of Solaris.'
		@echo
		@echo 'WARNING:  An initial "make gdma_package" will wipe out any customizations'
		@echo '          you have made to the untarred nagios plugins, and re-build from'
		@echo '          scratch.  Be sure to back up any such modifications you have made,'
		@echo '          before invoking this target!'
		@echo

all		: gdma_package gdmakey_package

gdma_package	: GWOSgdma-$(GDMA_RELEASE)-solaris$(SOLARIS_RELEASE)-$(ARCH).pkg

# We include in our package all the libraries that might not be installed on the target machine
# because they are optional software, not part of the base OS release.  We don't worry about
# redistribution rights because all the libraries we are including are either open-source code
# (e.g., OpenSSH and OpenSSL) or we are already covered by a redistribution license (e.g., MySQL)
# which applies to our base GroundWork Monitor product.
GWOSgdma-$(GDMA_RELEASE)-solaris$(SOLARIS_RELEASE)-$(ARCH).pkg	: nagios-plugins-$(PLUGINS_RELEASE)-build local-openssl-build local-nagios-tar
		@:
		mkdir -p 																				$(GDMA_PACKAGE_ABBREV);										\
		libexec=$(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/libexec;						\
		mkdir -p                                                                      $$libexec;	\
		cp -p nagios-plugins-$(PLUGINS_RELEASE)/build-solaris$(GDMA_PREFIX)/libexec/* $$libexec;	\
		cp -p nagios-plugins-$(PLUGINS_RELEASE)/plugins-root/check_dhcp               $$libexec;	\
		cp -p nagios-plugins-$(PLUGINS_RELEASE)/plugins-root/check_icmp               $$libexec;	\
		cp -p opt/groundwork/gdma/libexec/rblcheck-web						$$libexec;	\
		cp -p opt/groundwork/gdma/libexec/rblcheck-dns						$$libexec;	\
		cp -p opt/groundwork/gdma/libexec/check_pfstate						$$libexec;	\
		mkdir -p                                        $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/bin;	\
		:;
		@:
		mkdir -p                                        $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;
		if [ $(SOLARIS_RELEASE) = 6 ]; then								\
		    cp -p /usr/local/ssl/lib/libcrypto.so.0.9.8 $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;	\
		    cp -p /usr/local/ssl/lib/libssl.so.0.9.8    $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;	\
		fi;												\
		if [ $(SOLARIS_RELEASE) = 8 ]; then								\
		    cp -p /usr/local/ssl/lib/libcrypto.so.0.9.8 $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;	\
		    cp -p /usr/local/ssl/lib/libssl.so.0.9.8    $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;	\
		fi;												\
		if [ $(SOLARIS_RELEASE) = 9 ]; then								\
		    cp -p /usr/local/ssl/lib/libcrypto.so.0.9.8 $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;	\
		    cp -p /usr/local/ssl/lib/libssl.so.0.9.8    $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;	\
		    cp -p /opt/sfw/lib/liblber.so.2.0.122       $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;	\
		    cp -p /opt/sfw/lib/libldap.so.2.0.122       $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;	\
		fi;												\
		if [ $(SOLARIS_RELEASE) = 10 ]; then								\
		    cp -p /usr/sfw/lib/libcrypto.so.0.9.7       $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;	\
		    cp -p /usr/sfw/lib/libssl.so.0.9.7          $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;	\
		    cp -p /usr/sfw/lib/libmysqlclient.so.12.0.0 $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;	\
		    cp -p /opt/sfw/lib/liblber-2.2.so.7.0.10    $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;	\
		    cp -p /opt/sfw/lib/libldap-2.2.so.7.0.10    $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;	\
		fi;												\
		:;
		@:
		mkdir -p $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/include;	\
		mkdir -p $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/bin;						\
		mkdir -p $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/openssl;						\
		mkdir -p $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/include;						\
		mkdir -p $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib/pkgconfig;						\
		cp -R opt/groundwork/gdma/bin/* $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/bin;			\
		cp -p opt/groundwork/gdma/bin/gdma_check.pl     $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/bin;	\
		cp -p opt/groundwork/gdma/bin/gdma_getconfig.pl $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/bin;	\
		cp -p opt/groundwork/gdma/bin/send_nsca.pl      $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/bin;	\
		cp -Rp opt/groundwork/gdma/openssl/* $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/openssl;			\
		cp -Rp opt/groundwork/gdma/lib/* $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;			\
		cp -Rp opt/groundwork/gdma/include/* $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/include;			\
		cp -R opt/groundwork/gdma/libexec/*.pl $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/libexec;			\
		cp -R opt/groundwork/gdma/libexec/*.pm $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/libexec;			\
		cp -R opt/groundwork/gdma/libexec/*.sh $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/libexec;			\
		mkdir -p $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/log;						\
		mkdir -p $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/spool;						\
		mkdir -p              $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/etc/init.d;						\
		mkdir -p              $(GDMA_PACKAGE_ABBREV)/etc/init.d;						\
		cp -p etc/init.d/gdma $(GDMA_PACKAGE_ABBREV)/etc/init.d;						\
		cp -p etc/init.d/gdma $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/etc/init.d;						\
		:;
		@:
		pkginfo="$(GDMA_PACKAGE_ABBREV)/pkginfo";							\
		prototype="$(GDMA_PACKAGE_ABBREV)/prototype";							\
		copyright="$(GDMA_PACKAGE_ABBREV)/copyright";							\
		space="$(GDMA_PACKAGE_ABBREV)/space";								\
		:;												\
		echo "PKG='$(GDMA_PACKAGE_ABBREV)'"						>  $$pkginfo;	\
		echo "NAME='$(GDMA_PACKAGE_NAME)'"						>> $$pkginfo;	\
		echo "DESC='Plugins and other scripts for the distributed monitoring agent.'"	>> $$pkginfo;	\
		echo "ARCH='$(ARCH)'"								>> $$pkginfo;	\
		echo "VERSION='$(GDMA_RELEASE),REV=$(BUILD_TIME)'"				>> $$pkginfo;	\
		echo "CATEGORY='application,monitoring'"					>> $$pkginfo;	\
		echo "VENDOR='GroundWork Open Source, Inc.'"					>> $$pkginfo;	\
		echo "EMAIL='support@groundworkopensource.com'"					>> $$pkginfo;	\
		echo "PSTAMP='$(GDMA_TIME_PSTAMP)'"						>> $$pkginfo;	\
		echo "BASEDIR='/opt'"								>> $$pkginfo;	\
		if [ $(SOLARIS_RELEASE) = 6 ]; then								\
		    echo "CLASSES='none cron'"							>> $$pkginfo;	\
		else												\
		    echo "CLASSES='none'"							>> $$pkginfo;	\
		fi;												\
		:;												\
		echo "====================================================================="	>  $$copyright;	\
		echo "Copyright (c) 2007 GroundWork Open Source, Inc.  All rights reserved."	>> $$copyright;	\
		echo "Use is subject to GroundWork commercial license terms."			>> $$copyright;	\
		echo "====================================================================="	>> $$copyright;	\
		:;												\
		echo "$(GROUNDWORK_PREFIX) 512 20"						>  $$space;	\
		:;												\
		sed -e 's/bin bin/root bin/' -e 's@opt/@@' nagios-plugins-$(PLUGINS_RELEASE)/build-solaris/prototype	>  $$prototype;	\
		find opt/groundwork/gdma/libexec -type f -name "*.pl" -o -name "*.sh" -o -name "*.pm" | egrep -v -e "utils.pm|utils.sh" | pkgproto | sed -e 's@opt/@@' >> $$prototype; \
		echo 'd none groundwork/gdma/bin                     0755 gdma gdma'					>> $$prototype;	\
		echo 'd none groundwork/gdma/log                     0755 gdma gdma'					>> $$prototype;	\
		echo 'd none groundwork/gdma/spool                   0755 gdma gdma'					>> $$prototype;	\
		echo "f none /etc/init.d/gdma=$$PWD/etc/init.d/gdma  0744 root sys"					>> $$prototype;	\
		echo 'l none /etc/rc0.d/K02gdma=../init.d/gdma'								>> $$prototype;	\
		echo 'l none /etc/rc1.d/K02gdma=../init.d/gdma'								>> $$prototype;	\
		echo 'l none /etc/rc2.d/K02gdma=../init.d/gdma'								>> $$prototype;	\
		echo 'l none /etc/rc3.d/S97gdma=../init.d/gdma'								>> $$prototype;	\
		echo 'l none /etc/rcS.d/K02gdma=../init.d/gdma'								>> $$prototype;	\
		echo 'd none groundwork/gdma/lib                          0755 gdma gdma'					>> $$prototype;	\
		if [ $(SOLARIS_RELEASE) = 6 ]; then											\
		    echo 'f none groundwork/gdma/lib/libcrypto.so.0.9.8       0755 root bin'					>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/libssl.so.0.9.8          0755 root bin'					>> $$prototype;	\
		fi;															\
		if [ $(SOLARIS_RELEASE) = 8 ]; then											\
		    echo 'f none groundwork/gdma/lib/libcrypto.so.0.9.8       0755 root bin'					>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/libssl.so.0.9.8          0755 root bin'					>> $$prototype;	\
		fi;															\
		if [ $(SOLARIS_RELEASE) = 9 ]; then											\
		    echo 'f none groundwork/gdma/lib/libcrypto.so.0.9.8       0755 root bin'					>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/libssl.so.0.9.8          0755 root bin'					>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/liblber.so.2.0.122       0755 root bin'					>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/libldap.so.2.0.122       0755 root bin'					>> $$prototype;	\
		    echo 's none groundwork/gdma/lib/liblber.so.2=liblber.so.2.0.122'					>> $$prototype;	\
		    echo 's none groundwork/gdma/lib/libldap.so.2=libldap.so.2.0.122'					>> $$prototype;	\
		fi;															\
		if [ $(SOLARIS_RELEASE) = 10 ]; then											\
		    echo 'f none groundwork/gdma/lib/libcrypto.so.0.9.7       0755 root bin'					>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/libssl.so.0.9.7          0755 root bin'					>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/libmysqlclient.so.12.0.0 0755 root bin'					>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/liblber-2.2.so.7.0.10    0755 root bin'					>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/libldap-2.2.so.7.0.10    0755 root bin'					>> $$prototype;	\
		    echo 's none groundwork/gdma/lib/libmysqlclient.so.12=libmysqlclient.so.12.0.0'				>> $$prototype;	\
		    echo 's none groundwork/gdma/lib/liblber-2.2.so.7=liblber-2.2.so.7.0.10'					>> $$prototype;	\
		    echo 's none groundwork/gdma/lib/libldap-2.2.so.7=libldap-2.2.so.7.0.10'					>> $$prototype;	\
		fi;															\
		echo 'f none groundwork/gdma/bin/gdma_check.pl 0755 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/bin/gdma_getconfig.pl 0755 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/bin/send_nsca.pl 0755 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/bin/c_rehash 0755 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/bin/fipsld 0755 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/bin/openssl 0755 gdma gdma'                          >> $$prototype; \
		echo 'd none groundwork/gdma/lib/engines 0755 gdma gdma'											>> $$prototype; \
		echo 'f none groundwork/gdma/lib/engines/lib4758cca.so 0555 gdma gdma'								>> $$prototype; \
		echo 'f none groundwork/gdma/lib/engines/libaep.so 0555 gdma gdma'                          		>> $$prototype; \
		echo 'f none groundwork/gdma/lib/engines/libatalla.so 0555 gdma gdma'                          		>> $$prototype; \
		echo 'f none groundwork/gdma/lib/engines/libcswift.so 0555 gdma gdma'                          		>> $$prototype; \
		echo 'f none groundwork/gdma/lib/engines/libgmp.so 0555 gdma gdma'                          		>> $$prototype; \
		echo 'f none groundwork/gdma/lib/engines/libchil.so 0555 gdma gdma'                          		>> $$prototype; \
		echo 'f none groundwork/gdma/lib/engines/libnuron.so 0555 gdma gdma'                          		>> $$prototype; \
		echo 'f none groundwork/gdma/lib/engines/libsureware.so 0555 gdma gdma'                          	>> $$prototype; \
		echo 'f none groundwork/gdma/lib/engines/libubsec.so 0555 gdma gdma'                          		>> $$prototype; \
		echo 'd none groundwork/gdma/lib/pkgconfig 0755 gdma gdma'                          				>> $$prototype; \
		echo 'f none groundwork/gdma/lib/pkgconfig/libcrypto.pc 0644 gdma gdma'								>> $$prototype; \
		echo 'f none groundwork/gdma/lib/pkgconfig/libssl.pc 0644 gdma gdma'                          		>> $$prototype; \
		echo 'f none groundwork/gdma/lib/pkgconfig/openssl.pc 0644 gdma gdma'                          		>> $$prototype; \
		echo 'f none groundwork/gdma/lib/fipscanister.o 0444 gdma gdma'                          			>> $$prototype; \
		echo 'f none groundwork/gdma/lib/fipscanister.o.sha1 0444 gdma gdma'								>> $$prototype; \
		echo 'f none groundwork/gdma/lib/fips_premain.c 0444 gdma gdma'										>> $$prototype; \
		echo 'f none groundwork/gdma/lib/fips_premain.c.sha1 0444 gdma gdma'								>> $$prototype; \
		echo 'f none groundwork/gdma/lib/libcrypto.a 0644 gdma gdma'										>> $$prototype; \
		echo 'f none groundwork/gdma/lib/libssl.a 0644 gdma gdma'                          					>> $$prototype; \
		echo 's none groundwork/gdma/lib/libcrypto.so=libcrypto.so.0'                          				>> $$prototype; \
		echo 's none groundwork/gdma/lib/libssl.so=libssl.so.0'                          					>> $$prototype; \
		echo 's none groundwork/gdma/lib/libcrypto.so.0=libcrypto.so.0.9.7'                          		>> $$prototype; \
		echo 'f none groundwork/gdma/lib/libcrypto.so.0.9.7 0555 gdma gdma'                          		>> $$prototype; \
		echo 'f none groundwork/gdma/lib/libssl.so.0.9.7 0555 gdma gdma'                          			>> $$prototype; \
		echo 's none groundwork/gdma/lib/libssl.so.0=libssl.so.0.9.7'                          				>> $$prototype; \
		echo 'd none groundwork/gdma/etc/init.d 0755 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/etc/init.d/gdma 0744 gdma gdma'                          >> $$prototype; \
		echo 'd none groundwork/gdma/openssl 0755 gdma gdma'                          >> $$prototype; \
		echo 'd none groundwork/gdma/openssl/misc 0755 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/openssl/misc/c_issuer 0755 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/openssl/misc/CA.sh 0755 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/openssl/misc/CA.pl 0755 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/openssl/misc/c_hash 0755 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/openssl/misc/c_info 0755 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/openssl/misc/c_name 0755 gdma gdma'                          >> $$prototype; \
		echo 'd none groundwork/gdma/openssl/certs 0755 gdma gdma'                          >> $$prototype; \
		echo 'd none groundwork/gdma/openssl/private 0755 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/openssl/openssl.cnf 0644 gdma gdma'                          >> $$prototype; \
		echo 'd none groundwork/gdma/include 0755 gdma gdma'                          >> $$prototype; \
		echo 'd none groundwork/gdma/include/openssl 0755 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/e_os2.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/crypto.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/tmdiff.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/opensslv.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/opensslconf.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/ebcdic.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/symhacks.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/ossl_typ.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/objects.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/obj_mac.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/md2.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/md4.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/md5.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/sha.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/mdc2.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/hmac.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/ripemd.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/des.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/des_old.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/rc2.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/rc4.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/rc5.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/idea.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/blowfish.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/cast.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/bn.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/ec.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/rsa.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/dsa.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/dh.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/dso.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/engine.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/aes.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/buffer.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/bio.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/stack.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/safestack.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/lhash.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/rand.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/err.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/evp.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/asn1.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/asn1_mac.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/asn1t.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/pem.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/pem2.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/x509.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/x509_vfy.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/x509v3.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/conf.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/conf_api.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/txt_db.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/pkcs7.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/pkcs12.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/comp.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/ocsp.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/ui.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/ui_compat.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/krb5_asn.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/fips.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/fips_sha.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/fips_rand.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/ssl.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/ssl2.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/ssl3.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/ssl23.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/tls1.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/kssl.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/ecdsa.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/ecdh.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/store.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/pqueue.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/pq_compat.h 0644 gdma gdma'                          >> $$prototype; \
		echo 'f none groundwork/gdma/include/openssl/dtls1.h 0644 gdma gdma'                          >> $$prototype; \
		echo "i copyright=$$PWD/$$copyright"									>> $$prototype;	\
		echo "i space=$$PWD/$$space"										>> $$prototype;	\
		echo "i preinstall=$$PWD/scripts/$(GDMA_PACKAGE_ABBREV).preinstall"					>> $$prototype;	\
		echo "i preremove=$$PWD/scripts/$(GDMA_PACKAGE_ABBREV).preremove"					>> $$prototype;	\
		:;
		@:;							\
		cd $(GDMA_PACKAGE_ABBREV);				\
		rm -rf $$PWD/tmp/*;					\
		pkgmk -b $$PWD/$(GDMA_PACKAGE_ABBREV)/opt -d $$PWD/tmp;	\
		cd $$PWD;						\
		echo | pkgtrans -s $$PWD/tmp $$PWD/$(GDMA_PACKAGE_ABBREV)-$(GDMA_RELEASE)-solaris$(SOLARIS_RELEASE)-$(ARCH).pkg;	\
		echo | pkgchk -d             $$PWD/$(GDMA_PACKAGE_ABBREV)-$(GDMA_RELEASE)-solaris$(SOLARIS_RELEASE)-$(ARCH).pkg;	\
		chmod 444                    $$PWD/$(GDMA_PACKAGE_ABBREV)-$(GDMA_RELEASE)-solaris$(SOLARIS_RELEASE)-$(ARCH).pkg;	\
		echo "Compressing the final package ...";										\
		gzip --best                  $$PWD/$(GDMA_PACKAGE_ABBREV)-$(GDMA_RELEASE)-solaris$(SOLARIS_RELEASE)-$(ARCH).pkg;	\
		:;

nagios-plugins-$(PLUGINS_RELEASE)-build	:
		./make_nagios_plugins_on_solaris -r $(PLUGINS_RELEASE) -p $(GDMA_PREFIX)
		touch nagios-plugins-$(PLUGINS_RELEASE)-build

# For this package, once we know the target servername, we enforce a cleanup before making the
# package, to get rid of any leftover data from previous runs.  Then we build a package named
# GWOSgdmakey-{servername} where the servername represents the site's GroundWork Monitor server.
#
# Note that Solaris package names may be no longer than 32 characters.  In Solaris 2.5.1 the limit
# was shorter, perhaps as little as 9 characters, but we're not anticpating supporting that platform.

local-openssl-build :
		gdma-nfcu/openssl-build.sh

local-nagios-tar :
		/usr/bin/tar -xf solarisagent.sol9.32.tar;						\
		cp -p libexec/* opt/groundwork/gdma/libexec;						\
		/usr/bin/tar -xf gdmabin.tar;								\
		cp -p bin/* opt/groundwork/gdma/bin;							\

gdmakey_package	:
		@:											
		echo "======================================";						\
		echo "==  Build Solaris GDMA Key Package  ==";						\
		echo "======================================";						\
		printf 'Enter the Groundwork Server Name: ';						\
		read servername;									\
		if [ -z "$$servername" ]; then								\
		    echo "Empty server name; the GDMA Key package will not be created.";		\
		    exit 1;										\
		fi;											\
		if [ $(SOLARIS_RELEASE) -ge 10 ]; then							\
		    package_name=$(GDMAKEY_PACKAGE_ABBREV_BASE)-$$servername;				\
		    gdmakey_install_root="$$package_name";						\
		    if [ "`expr \"$$package_name\" : '.*'`" -gt 32 ]; then				\
			echo "Your server name '$$servername'";						\
			echo "is too long to be included in the package name.";				\
			echo "It can be no longer than 20 characters.  Aborting!";			\
			exit 1;										\
		    fi;											\
		else											\
		    package_name=$(GDMAKEY_PACKAGE_SHORT_ABBREV);					\
		    gdmakey_install_root="$$package_name-$$servername";					\
		fi;											\
		printf 'Enter the Groundwork Server IP Address: ';					\
		read ipaddr;										\
		if [ -z "$$ipaddr" ]; then								\
		    echo "The GDMA Key package will not be created.";					\
		    exit 1;										\
		fi;											\
		if [ "`sh -c 'expr '$$ipaddr' : \"\([0-9]\{1,3\}[.][0-9]\{1,3\}[.][0-9]\{1,3\}[.][0-9]\{1,3\}\)$$\"'; true`" = "$$ipaddr" ]; then	\
			echo "OK numeric address '$$ipaddr'; the GDMA Key Package will be created";	\
		else											\
		    echo "it is not an IP address '$$ipaddr'; but the GDMA Key package will be created.";	\
		fi;											\
		base_dir="$$gdmakey_install_root$(GDMA_HOME_DIR)/.ssh";					\
		rm -rf   $$base_dir;									\
		mkdir -p $$base_dir;									\
		config_dir="$$gdmakey_install_root$(GDMA_HOME_DIR)/config";				\
		mkdir -p        $$config_dir;								\
		echo $$ipaddr > $$config_dir/gdma_server.conf;						\
		rm -f "$$base_dir/.ssh/id_dsa";								\
		rm -f "$$base_dir/.ssh/id_dsa.pub";							\
		printf "Generating an SSH key ...";							\
		result="`ssh-keygen -t dsa -b 1024 -q -f $$base_dir/id_dsa -N '' 2>&1`";		\
		if [ -n "$$result" ]; then								\
		    echo "";										\
		    echo "    $$result";								\
		    echo "Aborting!";									\
		    exit 1;										\
		fi;											\
		echo "    Done.";									\
		echo "==========================================================================";	\
		echo "Building GWOSgdmakey-$$servername package for `uname -srp`";			\
		echo "    customized for server $$servername at IP address $$ipaddr";			\
		echo "==========================================================================";	\
		:;											\
		mkdir -p $(GDMAKEY_PACKAGE_SHORT_ABBREV)$(GDMA_HOME_DIR)/config;                        \
		cp -p $$config_dir/gdma_server.conf $(GDMAKEY_PACKAGE_SHORT_ABBREV)$(GDMA_HOME_DIR)/config;  \
		:;											\
		pkginfo="$$gdmakey_install_root/pkginfo";						\
		prototype="$$gdmakey_install_root/prototype";						\
		copyright="$$gdmakey_install_root/copyright";						\
		depend="$$gdmakey_install_root/depend";							\
		:;											\
		echo "PKG='$$package_name'"						>  $$pkginfo;	\
		echo "NAME='$(GDMA_PACKAGE_NAME) key files'"				>> $$pkginfo;	\
		echo "DESC='These files allow the distributed agent to contact the central monitoring server to maintain its configuration.'"	>> $$pkginfo;	\
		echo "ARCH='$(ARCH)'"							>> $$pkginfo;	\
		echo "VERSION='$(GDMA_RELEASE),REV=$(BUILD_TIME)'"			>> $$pkginfo;	\
		echo "CATEGORY='application,monitoring'"				>> $$pkginfo;	\
		echo "VENDOR='GroundWork Open Source, Inc.'"				>> $$pkginfo;	\
		echo "EMAIL='support@groundworkopensource.com'"				>> $$pkginfo;	\
		echo "PSTAMP='$(GDMAKEY_TIME_PSTAMP)'"					>> $$pkginfo;	\
		echo "BASEDIR='/opt'"							>> $$pkginfo;	\
		echo "CLASSES='none'"							>> $$pkginfo;	\
		:;											\
		echo "====================================================================="	>  $$copyright;	\
		echo "Copyright (c) 2007 GroundWork Open Source, Inc.  All rights reserved."	>> $$copyright;	\
		echo "Use is subject to GroundWork commercial license terms."			>> $$copyright;	\
		echo "====================================================================="	>> $$copyright;	\
		:;												\
		echo "P $(GDMA_PACKAGE_ABBREV) $(GDMA_PACKAGE_NAME)"				>  $$depend;	\
		:;												\
		echo "i pkginfo=$$PWD/$$pkginfo"						>  $$prototype;	\
		echo "i copyright=$$PWD/$$copyright"						>> $$prototype;	\
		echo "i depend=$$PWD/$$depend"							>> $$prototype;	\
		echo 'd none groundwork/home                              0755 gdma gdma'	>> $$prototype;	\
		echo 'd none groundwork/home/gdma                         0755 gdma gdma'	>> $$prototype;	\
		echo 'd none groundwork/home/gdma/.ssh                    0700 gdma gdma'	>> $$prototype;	\
		echo 'f none groundwork/home/gdma/.ssh/id_dsa             0600 gdma gdma'	>> $$prototype;	\
		echo 'f none groundwork/home/gdma/.ssh/id_dsa.pub         0644 gdma gdma'	>> $$prototype;	\
		echo 'd none groundwork/home/gdma/config                  0755 gdma gdma'	>> $$prototype;	\
		echo 'f none groundwork/home/gdma/config/gdma_server.conf 0644 gdma gdma'	>> $$prototype;	\
		cd $$gdmakey_install_root;									\
		rm -rf $$PWD/tmp/*;										\
		pkgmk -b $$PWD/$$gdmakey_install_root/opt -d $$PWD/tmp;						\
		cd $$PWD;											\
		echo | pkgtrans -s $$PWD/tmp $$PWD/$$gdmakey_install_root-$(GDMA_RELEASE)-solaris$(SOLARIS_RELEASE)-$(ARCH).pkg;	\
		echo | pkgchk -d             $$PWD/$$gdmakey_install_root-$(GDMA_RELEASE)-solaris$(SOLARIS_RELEASE)-$(ARCH).pkg;	\
		chmod 444                    $$PWD/$$gdmakey_install_root-$(GDMA_RELEASE)-solaris$(SOLARIS_RELEASE)-$(ARCH).pkg;	\
		echo "Compressing the final package ...";										\
		gzip --best                  $$PWD/$$gdmakey_install_root-$(GDMA_RELEASE)-solaris$(SOLARIS_RELEASE)-$(ARCH).pkg;	\
		:;															\
		echo "Creating the standalone key file for the GroundWork server ...";							\
		cp -p $$base_dir/id_dsa.pub $$PWD/$$gdmakey_install_root-$(GDMA_RELEASE)-solaris$(SOLARIS_RELEASE)-$(ARCH).id_dsa.pub;	\
		chmod 444                   $$PWD/$$gdmakey_install_root-$(GDMA_RELEASE)-solaris$(SOLARIS_RELEASE)-$(ARCH).id_dsa.pub;	\
		:;

install		:

clean		:
		rm -f  nagios-plugins-$(PLUGINS_RELEASE)-build
		rm -rf nagios-plugins-$(PLUGINS_RELEASE)
		rm -f  nagios-plugins-$(PLUGINS_RELEASE)-sol$(SOLARIS_RELEASE)-$(ARCH)-local
		rm -f  $(GDMA_PACKAGE_ABBREV)-*-solaris$(SOLARIS_RELEASE)-$(ARCH).pkg
		rm -f  $(GDMA_PACKAGE_ABBREV)-*-solaris$(SOLARIS_RELEASE)-$(ARCH).pkg.gz
		rm -rf $(GDMA_PACKAGE_ABBREV)
		rm -f  $(GDMAKEY_PACKAGE_ABBREV_BASE)-*-*-solaris$(SOLARIS_RELEASE)-$(ARCH).pkg
		rm -f  $(GDMAKEY_PACKAGE_ABBREV_BASE)-*-*-solaris$(SOLARIS_RELEASE)-$(ARCH).pkg.gz
		rm -rf $(GDMAKEY_PACKAGE_ABBREV_BASE)-*
		rm -f  $(GDMAKEY_PACKAGE_SHORT_ABBREV)-*-*-solaris$(SOLARIS_RELEASE)-$(ARCH).pkg
		rm -f  $(GDMAKEY_PACKAGE_SHORT_ABBREV)-*-*-solaris$(SOLARIS_RELEASE)-$(ARCH).pkg.gz
		rm -rf $(GDMAKEY_PACKAGE_SHORT_ABBREV)-*
		rm -rf tmp/*

distclean	: clean

#---------------------------------------------------------------#

#	Here's the complete set of what should go into each Solaris package:
#
#	GWOSgdma package:
#	------------------------------------------------------------------------------------------------
#	-rwxr--r-- root sys /etc/init.d/gdma			(plus links in /etc/rc*.d/ directories)
#	-rwxr-xr-x root bin /opt/groundwork/gdma/bin/...	(misc scripts)
#	-rwxr-xr-x root bin /opt/groundwork/gdma/log/		(empty directory)
#	-rwxr-xr-x root bin /opt/groundwork/gdma/spool/		(empty directory)
#	           root bin /opt/groundwork/gdma/libexec/...	(plugins)
#	           root bin /opt/groundwork/gdma/lib/...	(libcrypto, libssl) [Solaris 9 only?]
#
#	GWOSgdmakey package:
#	------------------------------------------------------------------------------------------------
#	make user:group as gdma:gdma, with home directory /opt/groundwork/home/gdma/
#	drwxr-xr-x gdma gdma /opt/groundwork/home/
#	drwxr-xr-x gdma gdma /opt/groundwork/home/gdma/
#	drwxr-xr-x gdma gdma /opt/groundwork/home/gdma/.ssh/
#	-rw------- gdma gdma /opt/groundwork/home/gdma/.ssh/id_dsa
#	-rw-r--r-- gdma gdma /opt/groundwork/home/gdma/.ssh/id_dsa.pub
#	drwxr-xr-x gdma gdma /opt/groundwork/gdma/config/
#	-rw-r--r-- gdma gdma /opt/groundwork/gdma/config/gdma_server.conf

#---------------------------------------------------------------#

# Top-level GroundWork Distributed Monitoring Agent makefile.

# Copyright 2009 GroundWork Open Source, Inc. ("GroundWork").  All rights
# reserved.  Use is subject to GroundWork commercial license terms.

# Note:  You must use Sun make, not gmake, to run this makefile.
# Sun make is found in /usr/ccs/bin/make which means that /usr/ccs/bin
# should be in your command-search path.  Possibly, gmake might work
# as well, but we haven't tested for it.

#---------------------------------------------------------------#

# This value must be updated for each new release, and the value must
# match the version information defined in the released files, if any.
# Please keep this version number for the NFCU releases in the 2.0.X range,
# so there is no possibility of confusing the special NFCU releases with
# any 2.1.X release from the main line of code development.
GDMA_RELEASE = 2.1.0

# Set this to reflect the version of the openssl library you wish to
# incorporate into your packages.
OPENSSL_RELEASE = 0.9.8j

# This is the top-level directory for all groundwork-related software,
# not just the GDMA package.
GROUNDWORK_PREFIX = /opt/groundwork

# Set this to reflect where you wish the compiled plugins to be installed;
# in actuality, the plugins themselves will be placed in the
# $(GDMA_PREFIX)/libexec/ directory.
GDMA_PREFIX = $(GROUNDWORK_PREFIX)/gdma

# This directory must be established as the home directory for the gdma user
# on all the machines where the GDMA packages are installed.
GDMA_HOME_DIR = $(GROUNDWORK_PREFIX)/gdma

# This directory is where our own copy of openssl will be located when it
# is installed.
OPENSSL_PREFIX = $(GDMA_PREFIX)

# If we wanted to label the packages with the build number, just like we do
# with RPMs, here's the build number we would use.
# SUBVERSION_BUILD :sh = svn info -r HEAD | fgrep Revision | gawk '{print $2}'

# The build date can be useful for tagging specific releases.
BUILD_TIME          :sh = date +'%Y-%m-%d.%R'
GDMA_TIME_PSTAMP    :sh = date +'gdma%Y%m%d%H%M%S'

# This package name is also the name of a directory where we'll construct the
# tree of files for the package.
GDMA_PACKAGE_ABBREV	= GWOSgdma

GDMA_PACKAGE_NAME	= GroundWork Distributed Monitoring Agent

PROCESSOR :sh = uname -p

ARCH :sh = arch

# We only deal with Solaris 2.6 and above (Solaris 7, 8, 9, 10, ...), and not
# the older Solaris 2.5 or 2.5.1 releases, so this simple pattern matching
# suffices, producing "6" for Solaris 2.6 (SunOS 5.6) even though that was
# never the marketing designation.
SOLARIS_RELEASE :sh = uname -r | sed 's/.*\.//'

#---------------------------------------------------------------#

default		: usage

usage		:
		@echo
		@echo 'Useful target is:'
		@echo '    make gdma_package'
		@echo 'which makes the GWOSgdma package for the same platform you"re building on.'
		@echo 'Don't build as root'
		@echo

all		: block_root gdma_package

# Building as root is just unspeakably dangerous.  All kinds of system corruption can result.
# It should not be necessary to remind people of this, but apparently we have too many folks
# who have gotten used to being sloppy by spending too much time on Windows machines.
block_root	:
		@if [ "`/usr/bin/id`" = 'uid=0(root) gid=0(root)' ]; then		\
		    echo;								\
		    echo '    ERROR:  You are not allowed to build this software';	\
		    echo '    while logged in as root.';				\
		    echo;								\
		    false;								\
		fi

check_path	: block_root
		@# Check to ensure that we won't run into problems later on.
		@echo $(PATH) | awk -F: '						\
		{									\
		    for (i = 1; i < NF; ++i) {						\
			if (($$i == "/usr/ccs/bin") || ($$i == "/usr/ccs/bin/")) {	\
			    gotmakepath = 1;						\
			}								\
		    }									\
		}									\
		END{					\
		    if (! gotmakepath) {		\
			print "===========================================================================";	\
			print "ERROR: You need /usr/ccs/bin in your command search path to run this build.";	\
			print "===========================================================================";	\
			exit 1;				\
		    }					\
		}'

gdma_package	: block_root opt_groundwork_gdma GWOSgdma-$(GDMA_RELEASE)-solaris$(SOLARIS_RELEASE)-$(PROCESSOR).pkg

opt_groundwork_gdma	: block_root
		if /usr/bin/id gdma 1>/dev/null 2>&1 && [ -d /opt/groundwork ]; then		\
		    : # The gdma user and the critical build directory already exist.;		\
		    : # For now, we'll assume without checking that the same is true;		\
		    : # for the gdma group.;							\
		    : # do nothing;								\
		else										\
		    echo 'Enter the root password when prompted, to make the gdma user:';	\
		    su root -c ./make_gdma_user;						\
		fi
		if [ `ls -ld /opt/groundwork | awk '{print $$1}'` != 'drwxrwxrwx' ]; then	\
		    echo 'ERROR:  The /opt/groundwork/ directory does not have sufficient';	\
		    echo 'permissions for this build to proceed.  Aborting!';			\
		    exit 1;									\
		fi
		mkdir -p /opt/groundwork/gdma

# We include in our package all the libraries that might not be installed on the target machine
# because they are optional software, not part of the base OS release.  We don't worry about
# redistribution rights because all the libraries we are including are either open-source code
# (e.g., OpenSSH and OpenSSL) or we are already covered by a redistribution license (e.g., MySQL)
# which applies to our base GroundWork Monitor product.
GWOSgdma-$(GDMA_RELEASE)-solaris$(SOLARIS_RELEASE)-$(PROCESSOR).pkg	: block_root local-openssl-build local-perl-build
		@:
		rm -rf   $(GDMA_PACKAGE_ABBREV);								\
		mkdir -p $(GDMA_PACKAGE_ABBREV);								\
		libexec=$(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/libexec;						\
		mkdir -p                                                                      $$libexec;	\
		:;
		@:
		mkdir -p                                        $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;	\
		if [ $(SOLARIS_RELEASE) = 6 ]; then								\
		    : # cp -p /usr/local/ssl/lib/libcrypto.so.0.9.8 $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;	\
		    : # cp -p /usr/local/ssl/lib/libssl.so.0.9.8    $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;	\
		fi;												\
		if [ $(SOLARIS_RELEASE) = 8 ]; then								\
		    : # cp -p /usr/local/ssl/lib/libcrypto.so.0.9.8 $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;	\
		    : # cp -p /usr/local/ssl/lib/libssl.so.0.9.8    $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;	\
		fi;												\
		if [ $(SOLARIS_RELEASE) = 9 ]; then								\
		    cp -p /opt/sfw/lib/liblber.so.2.0.122       $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;	\
		    cp -p /opt/sfw/lib/libldap.so.2.0.122       $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;	\
		fi;												\
		if [ $(SOLARIS_RELEASE) = 10 ]; then								\
		    if [ $(PROCESSOR) = sparc ]; then								\
			cp -p /opt/sfw/lib/liblber-2.2.so.7.0.10    $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;	\
			cp -p /opt/sfw/lib/libldap-2.2.so.7.0.10    $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;	\
		    fi;												\
		    cp -p /usr/sfw/lib/libmysqlclient.so.12.0.0 $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;	\
		fi;												\
		cp -Rp GDMA                                     $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;	\
		:;
		@:;														\
		mkdir -p                                          $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/bin;			\
		cp -p   opt/groundwork/gdma/bin/gdma_poll.pl     $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/bin;			\
		cp -p   opt/groundwork/gdma/bin/gdma_spool_processor.pl $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/bin;			\
		cp -p   opt/groundwork/gdma/bin/send_nsca.pl      $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/bin;			\
		cp -p  /opt/groundwork/gdma/bin/openssl           $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/bin;			\
		mkdir -p                                          $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/openssl;			\
		cp -Rp  opt/groundwork/gdma/openssl/*             $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/openssl;			\
		mkdir -p                                          $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib/pkgconfig;		\
		cp -Rp  opt/groundwork/gdma/lib/*                 $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;			\
		mkdir -p                                          $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/log;			\
		mkdir -p                                          $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/spool;			\
		mkdir -p                                          $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/tmp;			\
		mkdir -p                                          $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/config;		        \
                cp -p config/gdma_auto.conf                       $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/config;	
		mkdir -p                                          $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/etc/init.d;		\
		mkdir -p                                          $(GDMA_PACKAGE_ABBREV)/etc/init.d;				\
		cp -p etc/init.d/gdma                             $(GDMA_PACKAGE_ABBREV)/etc/init.d;				\
		cp -p etc/init.d/gdma                             $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/etc/init.d;		\
		:;
		# FIX THIS:  generalize to older Solaris releases as well, by testing in those environments
		if [ $(SOLARIS_RELEASE) = 9 -o $(SOLARIS_RELEASE) = 10 ]; then							\
		    cp -pr /opt/groundwork/gdma/lib/site_perl     $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;			\
		fi;														\
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
		echo "ARCH='$(PROCESSOR)'"							>> $$pkginfo;	\
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
		echo "Copyright (c) 2009 GroundWork Open Source, Inc.  All rights reserved."	>> $$copyright;	\
		echo "Use is subject to GroundWork commercial license terms."			>> $$copyright;	\
		echo "====================================================================="	>> $$copyright;	\
		:;												\
		echo "$(GROUNDWORK_PREFIX) 512 20"						>  $$space;	\
		:;												\
		echo "i pkginfo"											>> $$prototype;	\
		echo "f none /etc/init.d/gdma=$$PWD/$(GDMA_PACKAGE_ABBREV)/etc/init.d/gdma 0744 root sys"		>> $$prototype;	\
		echo 'l none /etc/rc0.d/K02gdma=../init.d/gdma'								>> $$prototype;	\
		echo 'l none /etc/rc1.d/K02gdma=../init.d/gdma'								>> $$prototype;	\
		echo 'l none /etc/rc2.d/K02gdma=../init.d/gdma'								>> $$prototype;	\
		echo 'l none /etc/rc3.d/S97gdma=../init.d/gdma'								>> $$prototype;	\
		echo 'l none /etc/rcS.d/K02gdma=../init.d/gdma'								>> $$prototype;	\
		echo 'd none groundwork/gdma/bin                              0755 gdma gdma'				>> $$prototype;	\
		echo 'f none groundwork/gdma/bin/gdma_poll.pl                0744 gdma gdma'				>> $$prototype;	\
		echo 'f none groundwork/gdma/bin/gdma_spool_processor.pl            0744 gdma gdma'				>> $$prototype;	\
		echo 'f none groundwork/gdma/bin/send_nsca.pl                 0744 gdma gdma'				>> $$prototype;	\
		echo 'd none groundwork/gdma/etc                              0755 gdma gdma'				>> $$prototype;	\
		echo 'd none groundwork/gdma/etc/init.d                       0755 gdma gdma'				>> $$prototype;	\
		echo 'f none groundwork/gdma/etc/init.d/gdma                  0744 gdma gdma'				>> $$prototype;	\
		echo 'd none groundwork/gdma/tmp                              0755 gdma gdma'	>> $$prototype;	\
		echo 'd none groundwork/gdma/config                           0755 gdma gdma'	>> $$prototype;	\
		echo 'f none groundwork/gdma/config/gdma_auto.conf            0644 gdma gdma'	>> $$prototype;	\
		echo 'd none groundwork/gdma/lib                              0755 root bin'				>> $$prototype;	\
		if [ $(SOLARIS_RELEASE) = 6 ]; then											\
		    echo 'f none groundwork/gdma/lib/libcrypto.so.0.9.8           0755 root bin'			>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/libssl.so.0.9.8              0755 root bin'			>> $$prototype;	\
		fi;															\
		if [ $(SOLARIS_RELEASE) = 8 ]; then											\
		    echo 'f none groundwork/gdma/lib/libcrypto.so.0.9.8           0755 root bin'			>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/libssl.so.0.9.8              0755 root bin'			>> $$prototype;	\
		fi;															\
		if [ $(SOLARIS_RELEASE) = 9 ]; then											\
		    echo 'f none groundwork/gdma/lib/libcrypto.so.0.9.8           0755 root bin'			>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/libssl.so.0.9.8              0755 root bin'			>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/liblber.so.2.0.122           0755 root bin'			>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/libldap.so.2.0.122           0755 root bin'			>> $$prototype;	\
		    echo 's none groundwork/gdma/lib/liblber.so.2=liblber.so.2.0.122'					>> $$prototype;	\
		    echo 's none groundwork/gdma/lib/libldap.so.2=libldap.so.2.0.122'					>> $$prototype;	\
		fi;															\
		if [ $(SOLARIS_RELEASE) = 10 ]; then											\
		    echo 'f none groundwork/gdma/lib/libcrypto.so.0.9.8           0755 root bin'			>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/libssl.so.0.9.8              0755 root bin'			>> $$prototype;	\
		    if [ $(PROCESSOR) = sparc ]; then											\
			echo 'f none groundwork/gdma/lib/liblber-2.2.so.7.0.10        0755 root bin'			>> $$prototype;	\
			echo 'f none groundwork/gdma/lib/libldap-2.2.so.7.0.10        0755 root bin'			>> $$prototype;	\
			echo 's none groundwork/gdma/lib/liblber-2.2.so.7=liblber-2.2.so.7.0.10'			>> $$prototype;	\
			echo 's none groundwork/gdma/lib/libldap-2.2.so.7=libldap-2.2.so.7.0.10'			>> $$prototype;	\
		    fi;															\
		    echo 'f none groundwork/gdma/lib/libmysqlclient.so.12.0.0     0755 root bin'			>> $$prototype;	\
		    echo 's none groundwork/gdma/lib/libmysqlclient.so.12=libmysqlclient.so.12.0.0'			>> $$prototype;	\
		fi;															\
		echo 'f none groundwork/gdma/bin/openssl                                0755 root bin'			>> $$prototype;	\
		echo 'd none groundwork/gdma/lib/GDMA                                   0755 root bin'			>> $$prototype;	\
		echo 'f none groundwork/gdma/lib/GDMA/GDMAUtils.pm                      0755 root bin'			>> $$prototype;	\
		echo 'd none groundwork/gdma/lib/engines                                0755 root bin'			>> $$prototype;	\
		echo 'f none groundwork/gdma/lib/engines/lib4758cca.so                  0555 root bin'			>> $$prototype;	\
		echo 'f none groundwork/gdma/lib/engines/libaep.so                      0555 root bin'			>> $$prototype;	\
		echo 'f none groundwork/gdma/lib/engines/libatalla.so                   0555 root bin'			>> $$prototype;	\
		echo 'f none groundwork/gdma/lib/engines/libcswift.so                   0555 root bin'			>> $$prototype;	\
		echo 'f none groundwork/gdma/lib/engines/libgmp.so                      0555 root bin'			>> $$prototype;	\
		echo 'f none groundwork/gdma/lib/engines/libchil.so                     0555 root bin'			>> $$prototype;	\
		echo 'f none groundwork/gdma/lib/engines/libnuron.so                    0555 root bin'			>> $$prototype;	\
		echo 'f none groundwork/gdma/lib/engines/libsureware.so                 0555 root bin'			>> $$prototype;	\
		echo 'f none groundwork/gdma/lib/engines/libubsec.so                    0555 root bin'			>> $$prototype;	\
		echo 'd none groundwork/gdma/lib/pkgconfig                              0755 root bin'			>> $$prototype;	\
		echo 'f none groundwork/gdma/lib/pkgconfig/libcrypto.pc                 0644 root bin'			>> $$prototype;	\
		echo 'f none groundwork/gdma/lib/pkgconfig/libssl.pc                    0644 root bin'			>> $$prototype;	\
		echo 'f none groundwork/gdma/lib/pkgconfig/openssl.pc                   0644 root bin'			>> $$prototype;	\
		echo 's none groundwork/gdma/lib/libcrypto.so=libcrypto.so.0'						>> $$prototype;	\
		echo 's none groundwork/gdma/lib/libssl.so=libssl.so.0'							>> $$prototype;	\
		echo 's none groundwork/gdma/lib/libcrypto.so.0=libcrypto.so.0.9.8'					>> $$prototype;	\
		echo 's none groundwork/gdma/lib/libssl.so.0=libssl.so.0.9.8'						>> $$prototype;	\
		echo 'd none groundwork/gdma/log                                        0755 gdma gdma'			>> $$prototype;	\
		echo 'd none groundwork/gdma/openssl                                    0755 root bin'			>> $$prototype;	\
		echo 'd none groundwork/gdma/openssl/misc                               0755 root bin'			>> $$prototype;	\
		echo 'f none groundwork/gdma/openssl/misc/c_issuer                      0755 root bin'			>> $$prototype;	\
		echo 'f none groundwork/gdma/openssl/misc/CA.sh                         0755 root bin'			>> $$prototype;	\
		echo 'f none groundwork/gdma/openssl/misc/CA.pl                         0755 root bin'			>> $$prototype;	\
		echo 'f none groundwork/gdma/openssl/misc/c_hash                        0755 root bin'			>> $$prototype;	\
		echo 'f none groundwork/gdma/openssl/misc/c_info                        0755 root bin'			>> $$prototype;	\
		echo 'f none groundwork/gdma/openssl/misc/c_name                        0755 root bin'			>> $$prototype;	\
		echo 'd none groundwork/gdma/openssl/certs                              0755 root bin'			>> $$prototype;	\
		echo 'd none groundwork/gdma/openssl/private                            0755 root bin'			>> $$prototype;	\
		echo 'f none groundwork/gdma/openssl/openssl.cnf                        0644 gdma gdma'			>> $$prototype;	\
		echo 'd none groundwork/gdma/spool                                      0755 gdma gdma'			>> $$prototype;	\
		if [ $(SOLARIS_RELEASE) = 9 -o $(SOLARIS_RELEASE) = 10 ]; then								\
		    echo 'd none groundwork/gdma/lib/site_perl                                                           0755 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int                                     0755 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto                                0755 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Compress                       0755 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Compress/Raw                   0755 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Compress/Raw/Zlib              0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Compress/Raw/Zlib/.packlist    0644 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Compress/Raw/Zlib/Zlib.bs      0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Compress/Raw/Zlib/Zlib.so      0555 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Compress/Raw/Zlib/autosplit.ix 0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Compress/Zlib                  0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Compress/Zlib/.packlist        0644 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Crypt                          0755 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Crypt/SSLeay                   0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Crypt/SSLeay/.packlist         0644 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Crypt/SSLeay/SSLeay.bs         0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Crypt/SSLeay/SSLeay.so         0555 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/HTML                           0755 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/HTML/Parser                    0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/HTML/Parser/.packlist          0644 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/HTML/Parser/Parser.bs          0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/HTML/Parser/Parser.so          0555 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/HTML/Tagset                    0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/HTML/Tagset/.packlist          0644 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/IO                             0755 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/IO/Compress                    0755 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/IO/Compress/Base               0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/IO/Compress/Base/.packlist     0644 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/IO/Compress/Zlib               0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/IO/Compress/Zlib/.packlist     0644 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/LWP                            0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/LWP/.packlist                  0644 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/List                           0755 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/List/Util                      0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/List/Util/.packlist            0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/List/Util/Util.bs              0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/List/Util/Util.so              0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/URI                            0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/URI/.packlist                  0644 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/Storable.pm                              0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Storable                            0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Storable/logcroak.al                0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Storable/Storable.so                0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Storable/Storable.bs                0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Storable/logcarp.al                 0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Storable/CAN_FLOCK.al               0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Storable/show_file_magic.al         0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Storable/file_magic.al              0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Storable/read_magic.al              0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Storable/BIN_VERSION_NV.al          0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Storable/BIN_WRITE_VERSION_NV.al    0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Storable/store.al                   0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Storable/nstore.al                  0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Storable/lock_store.al              0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Storable/lock_nstore.al             0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Storable/_store.al                  0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Storable/store_fd.al                0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Storable/nstore_fd.al               0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Storable/_store_fd.al               0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Storable/freeze.al                  0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Storable/nfreeze.al                 0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Storable/_freeze.al                 0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Storable/retrieve.al                0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Storable/thaw.al                    0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Storable/lock_retrieve.al           0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Storable/_retrieve.al               0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Storable/fd_retrieve.al             0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Storable/autosplit.ix               0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/Compress                            0755 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/Compress/Raw                        0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/Compress/Raw/Zlib.pm                0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/Crypt                               0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/Crypt/SSLeay.pm                     0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/Crypt/SSLeay                        0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/Crypt/SSLeay/CTX.pm                 0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/Crypt/SSLeay/Conn.pm                0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/Crypt/SSLeay/Err.pm                 0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/Crypt/SSLeay/MainContext.pm         0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/Crypt/SSLeay/X509.pm                0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/HTML                                0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/HTML/Entities.pm                    0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/HTML/Filter.pm                      0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/HTML/HeadParser.pm                  0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/HTML/LinkExtor.pm                   0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/HTML/Parser.pm                      0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/HTML/PullParser.pm                  0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/HTML/TokeParser.pm                  0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/List                                0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/List/Util.pm                        0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/Net                                 0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/Net/SSL.pm                          0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/Scalar                              0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/Scalar/Util.pm                      0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/perllocal.pod                       0644 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/auto                                                      0755 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/auto/Compress                                             0755 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/auto/Compress/Zlib                                        0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/auto/Compress/Zlib/autosplit.ix                           0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/Bundle                                                    0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/Bundle/LWP.pm                                             0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/Compress                                                  0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/Compress/Zlib.pm                                          0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/File                                                      0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/File/GlobMapper.pm                                        0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/File/Listing.pm                                           0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/HTML                                                      0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/HTML/Form.pm                                              0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/HTML/Tagset.pm                                            0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/HTTP                                                      0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/HTTP/Config.pm                                            0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/HTTP/Cookies.pm                                           0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/HTTP/Daemon.pm                                            0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/HTTP/Date.pm                                              0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/HTTP/Headers.pm                                           0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/HTTP/Message.pm                                           0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/HTTP/Negotiate.pm                                         0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/HTTP/Request.pm                                           0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/HTTP/Response.pm                                          0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/HTTP/Status.pm                                            0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/HTTP/Headers                                              0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/HTTP/Headers/Auth.pm                                      0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/HTTP/Headers/ETag.pm                                      0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/HTTP/Headers/Util.pm                                      0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/HTTP/Request                                              0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/HTTP/Request/Common.pm                                    0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/HTTP/Cookies                                              0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/HTTP/Cookies/Microsoft.pm                                 0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/HTTP/Cookies/Netscape.pm                                  0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/IO                                                        0755 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/IO/Compress                                               0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/IO/Compress/Base.pm                                       0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/IO/Compress/Base                                          0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/IO/Compress/Base/Common.pm                                0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/IO/Compress/Deflate.pm                                    0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/IO/Compress/Gzip.pm                                       0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/IO/Compress/RawDeflate.pm                                 0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/IO/Compress/Zip.pm                                        0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/IO/Compress/Adapter                                       0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/IO/Compress/Adapter/Deflate.pm                            0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/IO/Compress/Adapter/Identity.pm                           0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/IO/Compress/Gzip                                          0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/IO/Compress/Gzip/Constants.pm                             0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/IO/Compress/Zlib                                          0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/IO/Compress/Zlib/Constants.pm                             0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/IO/Compress/Zlib/Extra.pm                                 0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/IO/Compress/Zip                                           0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/IO/Compress/Zip/Constants.pm                              0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/IO/Uncompress                                             0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/IO/Uncompress/AnyInflate.pm                               0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/IO/Uncompress/AnyUncompress.pm                            0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/IO/Uncompress/Base.pm                                     0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/IO/Uncompress/Gunzip.pm                                   0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/IO/Uncompress/Inflate.pm                                  0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/IO/Uncompress/RawInflate.pm                               0555 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/IO/Uncompress/Unzip.pm                                    0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/IO/Uncompress/Adapter                                     0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/IO/Uncompress/Adapter/Identity.pm                         0555 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/IO/Uncompress/Adapter/Inflate.pm                          0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/LWP.pm                                                    0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/lwptut.pod                                                0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/lwpcook.pod                                               0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/LWP                                                       0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/LWP/ConnCache.pm                                          0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/LWP/Debug.pm                                              0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/LWP/DebugFile.pm                                          0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/LWP/MediaTypes.pm                                         0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/LWP/MemberMixin.pm                                        0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/LWP/Protocol.pm                                           0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/LWP/RobotUA.pm                                            0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/LWP/Simple.pm                                             0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/LWP/UserAgent.pm                                          0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/LWP/media.types                                           0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/LWP/Authen                                                0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/LWP/Authen/Basic.pm                                       0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/LWP/Authen/Digest.pm                                      0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/LWP/Authen/Ntlm.pm                                        0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/LWP/Protocol                                              0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/LWP/Protocol/GHTTP.pm                                     0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/LWP/Protocol/cpan.pm                                      0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/LWP/Protocol/data.pm                                      0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/LWP/Protocol/file.pm                                      0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/LWP/Protocol/ftp.pm                                       0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/LWP/Protocol/gopher.pm                                    0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/LWP/Protocol/http.pm                                      0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/LWP/Protocol/http10.pm                                    0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/LWP/Protocol/https.pm                                     0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/LWP/Protocol/https10.pm                                   0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/LWP/Protocol/loopback.pm                                  0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/LWP/Protocol/mailto.pm                                    0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/LWP/Protocol/nntp.pm                                      0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/LWP/Protocol/nogo.pm                                      0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/Net                                                       0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/Net/Telnet.pm                                               0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/Net/HTTP.pm                                               0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/Net/HTTPS.pm                                              0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/Net/HTTP                                                  0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/Net/HTTP/Methods.pm                                       0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/Net/HTTP/NB.pm                                            0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI.pm                                                    0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/URI                                                       0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/Escape.pm                                             0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/Heuristic.pm                                          0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/QueryParam.pm                                         0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/Split.pm                                              0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/URL.pm                                                0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/WithBase.pm                                           0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/_foreign.pm                                           0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/_generic.pm                                           0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/_ldap.pm                                              0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/_login.pm                                             0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/_query.pm                                             0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/_segment.pm                                           0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/_server.pm                                            0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/_userpass.pm                                          0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/data.pm                                               0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/file.pm                                               0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/ftp.pm                                                0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/gopher.pm                                             0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/http.pm                                               0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/https.pm                                              0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/ldap.pm                                               0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/ldapi.pm                                              0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/ldaps.pm                                              0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/mailto.pm                                             0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/mms.pm                                                0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/news.pm                                               0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/nntp.pm                                               0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/pop.pm                                                0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/rlogin.pm                                             0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/rsync.pm                                              0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/rtsp.pm                                               0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/rtspu.pm                                              0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/sip.pm                                                0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/sips.pm                                               0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/snews.pm                                              0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/ssh.pm                                                0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/telnet.pm                                             0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/tn3270.pm                                             0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/urn.pm                                                0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/URI/file                                                  0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/file/Base.pm                                          0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/file/FAT.pm                                           0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/file/Mac.pm                                           0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/file/OS2.pm                                           0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/file/QNX.pm                                           0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/file/Unix.pm                                          0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/file/Win32.pm                                         0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/URI/urn                                                   0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/urn/isbn.pm                                           0444 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/URI/urn/oid.pm                                            0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/WWW                                                       0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/WWW/RobotRules.pm                                         0444 root root'	>> $$prototype;	\
		    echo 'd none groundwork/gdma/lib/site_perl/WWW/RobotRules                                            0755 root root'	>> $$prototype;	\
		    echo 'f none groundwork/gdma/lib/site_perl/WWW/RobotRules/AnyDBM_File.pm                             0444 root root'	>> $$prototype;	\
		fi;															\
		if [ $(SOLARIS_RELEASE) = 9 ]; then											\
                    echo 'd none groundwork/gdma/lib/site_perl/App                                                    0755 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/App/Prove.pm                                           0444 root root'	>> $$prototype; \
                    echo 'd none groundwork/gdma/lib/site_perl/App/Prove                                              0755 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/App/Prove/State.pm                                     0444 root root'	>> $$prototype; \
                    echo 'd none groundwork/gdma/lib/site_perl/App/Prove/State                                        0755 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/App/Prove/State/Result.pm                              0444 root root'	>> $$prototype; \
                    echo 'd none groundwork/gdma/lib/site_perl/App/Prove/State/Result                                 0755 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/App/Prove/State/Result/Test.pm                         0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/Digest.pm                                              0444 root root'	>> $$prototype; \
                    echo 'd none groundwork/gdma/lib/site_perl/Digest                                                 0755 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/Digest/base.pm                                         0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/Digest/file.pm                                         0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/Net/Cmd.pm                                             0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/Net/Config.pm                                          0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/Net/Domain.pm                                          0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/Net/FTP.pm                                             0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/Net/NNTP.pm                                            0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/Net/Netrc.pm                                           0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/Net/POP3.pm                                            0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/Net/SMTP.pm                                            0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/Net/Time.pm                                            0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/Net/libnet.cfg                                         0644 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/Net/libnetFAQ.pod                                      0444 root root'	>> $$prototype; \
                    echo 'd none groundwork/gdma/lib/site_perl/Net/FTP                                                0755 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/Net/FTP/A.pm                                           0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/Net/FTP/E.pm                                           0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/Net/FTP/I.pm                                           0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/Net/FTP/L.pm                                           0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/Net/FTP/dataconn.pm                                    0444 root root'	>> $$prototype; \
                    echo 'd none groundwork/gdma/lib/site_perl/TAP                                                    0755 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Base.pm                                            0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Harness.pm                                         0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Object.pm                                          0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Parser.pm                                          0444 root root'	>> $$prototype; \
                    echo 'd none groundwork/gdma/lib/site_perl/TAP/Formatter                                          0755 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Formatter/Color.pm                                 0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Formatter/Console.pm                               0444 root root'	>> $$prototype; \
                    echo 'd none groundwork/gdma/lib/site_perl/TAP/Formatter/Console                                  0755 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Formatter/Console/ParallelSession.pm               0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Formatter/Console/Session.pm                       0444 root root'	>> $$prototype; \
                    echo 'd none groundwork/gdma/lib/site_perl/TAP/Parser                                             0755 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Parser/Aggregator.pm                               0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Parser/Grammar.pm                                  0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Parser/Iterator.pm                                 0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Parser/IteratorFactory.pm                          0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Parser/Multiplexer.pm                              0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Parser/Result.pm                                   0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Parser/ResultFactory.pm                            0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Parser/Scheduler.pm                                0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Parser/Source.pm                                   0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Parser/Utils.pm                                    0444 root root'	>> $$prototype; \
                    echo 'd none groundwork/gdma/lib/site_perl/TAP/Parser/Iterator                                    0755 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Parser/Iterator/Array.pm                           0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Parser/Iterator/Process.pm                         0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Parser/Iterator/Stream.pm                          0444 root root'	>> $$prototype; \
                    echo 'd none groundwork/gdma/lib/site_perl/TAP/Parser/Result                                      0755 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Parser/Result/Bailout.pm                           0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Parser/Result/Comment.pm                           0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Parser/Result/Plan.pm                              0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Parser/Result/Pragma.pm                            0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Parser/Result/Test.pm                              0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Parser/Result/Unknown.pm                           0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Parser/Result/Version.pm                           0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Parser/Result/YAML.pm                              0444 root root'	>> $$prototype; \
                    echo 'd none groundwork/gdma/lib/site_perl/TAP/Parser/Scheduler                                   0755 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Parser/Scheduler/Job.pm                            0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Parser/Scheduler/Spinner.pm                        0444 root root'	>> $$prototype; \
                    echo 'd none groundwork/gdma/lib/site_perl/TAP/Parser/Source                                      0755 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Parser/Source/Perl.pm                              0444 root root'	>> $$prototype; \
                    echo 'd none groundwork/gdma/lib/site_perl/TAP/Parser/YAMLish                                     0755 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Parser/YAMLish/Reader.pm                           0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/TAP/Parser/YAMLish/Writer.pm                           0444 root root'	>> $$prototype; \
                    echo 'd none groundwork/gdma/lib/site_perl/Test                                                   0755 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/Test/Builder.pm                                        0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/Test/HACKING.pod                                       0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/Test/Harness.pm                                        0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/Test/More.pm                                           0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/Test/Simple.pm                                         0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/Test/Tutorial.pod                                      0444 root root'	>> $$prototype; \
                    echo 'd none groundwork/gdma/lib/site_perl/Test/Builder                                           0755 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/Test/Builder/Module.pm                                 0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/Test/Builder/Tester.pm                                 0444 root root'	>> $$prototype; \
                    echo 'd none groundwork/gdma/lib/site_perl/Test/Builder/Tester                                    0755 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/Test/Builder/Tester/Color.pm                           0444 root root'	>> $$prototype; \
                    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/Digest                           0755 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/Digest/MD5.pm                    0444 root root'	>> $$prototype; \
                    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/MIME                             0755 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/MIME/Base64.pm                   0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/MIME/QuotedPrint.pm              0444 root root'	>> $$prototype; \
                    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Digest                      0755 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Digest/.packlist            0644 root root'	>> $$prototype; \
                    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Digest/MD5                  0755 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Digest/MD5/.packlist        0644 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Digest/MD5/MD5.bs           0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Digest/MD5/MD5.so           0555 root root'	>> $$prototype; \
                    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/MIME                        0755 root root'	>> $$prototype; \
                    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/MIME/Base64                 0755 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/MIME/Base64/.packlist       0644 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/MIME/Base64/Base64.bs       0444 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/MIME/Base64/Base64.so       0555 root root'	>> $$prototype; \
                    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Net                         0755 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Net/.packlist               0644 root root'	>> $$prototype; \
                    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Test                        0755 root root'	>> $$prototype; \
                    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Test/Harness                0755 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Test/Harness/.packlist      0644 root root'	>> $$prototype; \
                    echo 'd none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Test/Simple                 0755 root root'	>> $$prototype; \
                    echo 'f none groundwork/gdma/lib/site_perl/$(ARCH)-solaris-64int/auto/Test/Simple/.packlist       0644 root root'	>> $$prototype; \
		fi;															\
		echo "i copyright=$$PWD/$$copyright"									>> $$prototype;	\
		echo "i space=$$PWD/$$space"										>> $$prototype;	\
		echo "i preinstall=$$PWD/scripts/$(GDMA_PACKAGE_ABBREV).preinstall"					>> $$prototype;	\
		echo "i postinstall=$$PWD/scripts/$(GDMA_PACKAGE_ABBREV).postinstall"					>> $$prototype;	\
		echo "i preremove=$$PWD/scripts/$(GDMA_PACKAGE_ABBREV).preremove"					>> $$prototype;	\
		echo "i postremove=$$PWD/scripts/$(GDMA_PACKAGE_ABBREV).postremove"					>> $$prototype;	\
		:;
		@:;							\
		cd $(GDMA_PACKAGE_ABBREV);				\
		rm -rf $$PWD/tmp/*;					\
		pkgmk -b $$PWD/$(GDMA_PACKAGE_ABBREV)/opt -d $$PWD/tmp;	\
		cd $$PWD;						\
		echo | pkgtrans -s $$PWD/tmp $$PWD/$(GDMA_PACKAGE_ABBREV)-$(GDMA_RELEASE)-solaris$(SOLARIS_RELEASE)-$(PROCESSOR).pkg;	\
		echo | /usr/sbin/pkgchk -d   $$PWD/$(GDMA_PACKAGE_ABBREV)-$(GDMA_RELEASE)-solaris$(SOLARIS_RELEASE)-$(PROCESSOR).pkg;	\
		chmod 444                    $$PWD/$(GDMA_PACKAGE_ABBREV)-$(GDMA_RELEASE)-solaris$(SOLARIS_RELEASE)-$(PROCESSOR).pkg;	\
		echo "Compressing the final package ...";										\
		gzip --best                  $$PWD/$(GDMA_PACKAGE_ABBREV)-$(GDMA_RELEASE)-solaris$(SOLARIS_RELEASE)-$(PROCESSOR).pkg;	\
		:;

local-openssl-build : block_root check_path
		./openssl-build -r $(OPENSSL_RELEASE) -p $(OPENSSL_PREFIX)
		touch local-openssl-build

local-perl-build : block_root check_path
		cd perl_modules; make
		touch local-perl-build

install		:

perl-clean	: block_root check_path
		cd perl_modules; make clean
		rm -f local-perl-build

openssl-clean	: block_root
		rm -rf openssl-$(OPENSSL_RELEASE)
		rm -f  opt/groundwork/gdma/bin/c_rehash
		rm -f  opt/groundwork/gdma/bin/openssl
		rm -rf opt/groundwork/gdma/include
		rm -rf opt/groundwork/gdma/lib
		rm -rf opt/groundwork/gdma/man
		rm -rf opt/groundwork/gdma/openssl
		rm -f  local-openssl-build

gdma-clean	: block_root
		rm -rf tmp/*
		rm -rf $(GDMA_PACKAGE_ABBREV)

package-clean	: block_root
		rm -f  $(GDMA_PACKAGE_ABBREV)-*-solaris$(SOLARIS_RELEASE)-$(PROCESSOR).pkg
		rm -f  $(GDMA_PACKAGE_ABBREV)-*-solaris$(SOLARIS_RELEASE)-$(PROCESSOR).pkg.gz

clean		: block_root perl-clean openssl-clean gdma-clean
		rm -rf /opt/groundwork/gdma

distclean	: block_root clean package-clean

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
#---------------------------------------------------------------#

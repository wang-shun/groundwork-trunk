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
GDMA_RELEASE = 2.0.17

# Set this to reflect the version of the third-party code you wish to
# incorporate into your packages.
PLUGINS_RELEASE = 1.4.11

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
GDMA_HOME_DIR = $(GROUNDWORK_PREFIX)/home/gdma

# This directory is where our own copy of openssl will be located when it
# is installed.
OPENSSL_PREFIX = $(GDMA_PREFIX)

# If we wanted to label the packages with the build number, just like we do
# with RPMs, here's the build number we would use.
# SUBVERSION_BUILD :sh = svn info -r HEAD | fgrep Revision | gawk '{print $2}'

# The build date can be useful for tagging specific releases.
BUILD_TIME          :sh = date +'%Y-%m-%d.%R'
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

check_path	:
		@# Check to ensure that we won't run into problems later on.
		@echo $(PATH) | awk -F: '		\
		{					\
		    for (i = 1; i < NF; ++i) {		\
			if ($$i == "/usr/ccs/bin") {	\
			    gotmakepath = 1;		\
			}				\
		    }					\
		}					\
		END{					\
		    if (! gotmakepath) {		\
			print "===========================================================================";	\
			print "ERROR: You need /usr/ccs/bin in your command search path to run this build.";	\
			print "===========================================================================";	\
			exit 1;				\
		    }					\
		}'

gdma_package	: opt_groundwork_gdma GWOSgdma-$(GDMA_RELEASE)-solaris$(SOLARIS_RELEASE)-$(PROCESSOR).pkg

opt_groundwork_gdma	:
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
GWOSgdma-$(GDMA_RELEASE)-solaris$(SOLARIS_RELEASE)-$(PROCESSOR).pkg	: local-openssl-build local-perl-build nagios-plugins-build
		@:
		rm -rf   $(GDMA_PACKAGE_ABBREV);								\
		mkdir -p $(GDMA_PACKAGE_ABBREV);								\
		libexec=$(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/libexec;						\
		mkdir -p                                                                      $$libexec;	\
		cp -p nagios-plugins-$(PLUGINS_RELEASE)/build-solaris$(GDMA_PREFIX)/libexec/* $$libexec;	\
		cp -p nagios-plugins-$(PLUGINS_RELEASE)/plugins-root/check_dhcp               $$libexec;	\
		cp -p nagios-plugins-$(PLUGINS_RELEASE)/plugins-root/check_icmp               $$libexec;	\
		rm -f nagios-plugins-$(PLUGINS_RELEASE)/contrib/check_compaq_insight.pl;			\
		cp -p nagios-plugins-$(PLUGINS_RELEASE)/contrib/*.pl                          $$libexec;	\
		cp -p nagios-plugins-$(PLUGINS_RELEASE)/contrib/*.pm                          $$libexec;	\
		cp -p nagios-plugins-$(PLUGINS_RELEASE)/contrib/*.py                          $$libexec;	\
		cp -p nagios-plugins-$(PLUGINS_RELEASE)/contrib/*.sh                          $$libexec;	\
		cp -p nagios-plugins-$(PLUGINS_RELEASE)/contrib/check_fan_cpq_present         $$libexec;	\
		cp -p nagios-plugins-$(PLUGINS_RELEASE)/contrib/check_fan_fsc_present         $$libexec;	\
		cp -p nagios-plugins-$(PLUGINS_RELEASE)/contrib/check_frontpage               $$libexec;	\
		cp -p nagios-plugins-$(PLUGINS_RELEASE)/contrib/check_temp_cpq                $$libexec;	\
		cp -p nagios-plugins-$(PLUGINS_RELEASE)/contrib/check_temp_fsc                $$libexec;	\
		cp -p nagios-plugins-$(PLUGINS_RELEASE)/contrib/rblcheck-dns                  $$libexec;	\
		cp -p nagios-plugins-$(PLUGINS_RELEASE)/contrib/rblcheck-web                  $$libexec;	\
		sed -e "s@/usr/local/nagios/libexec@$(GDMA_PREFIX)/libexec@"									\
		    nagios-plugins-$(PLUGINS_RELEASE)/contrib/check_apc_ups.pl			> $$libexec/check_apc_ups.pl;			\
		sed -e "s@/usr/local/nagios/libexec@$(GDMA_PREFIX)/libexec@"									\
		    -e '/-----/,$$d'														\
		    nagios-plugins-$(PLUGINS_RELEASE)/contrib/check_appletalk.pl		> $$libexec/check_appletalk.pl;			\
		sed -e "s@/usr/lib/nagios/plugins@$(GDMA_PREFIX)/libexec@"									\
		    nagios-plugins-$(PLUGINS_RELEASE)/contrib/check_arping.pl			> $$libexec/check_arping.pl;			\
		sed -e "s@&is_error@@"														\
		    nagios-plugins-$(PLUGINS_RELEASE)/contrib/check_backup.pl			> $$libexec/check_backup.pl;			\
		sed -e "s@/usr/local/nagios/libexec@$(GDMA_PREFIX)/libexec@"									\
		    -e "s@/usr/local/nagios/etc/@$(GDMA_PREFIX)/etc/@"										\
		    nagios-plugins-$(PLUGINS_RELEASE)/contrib/check_dns_random.pl		> $$libexec/check_dns_random.pl;		\
		sed -e "s@/usr/local/nagios/libexec@$(GDMA_PREFIX)/libexec@"									\
		    nagios-plugins-$(PLUGINS_RELEASE)/contrib/check_ica_master_browser.pl	> $$libexec/check_ica_master_browser.pl;	\
		sed -e "s@/usr/local/nagios/libexec@$(GDMA_PREFIX)/libexec@"									\
		    nagios-plugins-$(PLUGINS_RELEASE)/contrib/check_ica_metaframe_pub_apps.pl	> $$libexec/check_ica_metaframe_pub_apps.pl;	\
		sed -e "s@/usr/local/libexec/nagios@$(GDMA_PREFIX)/libexec@"									\
		    nagios-plugins-$(PLUGINS_RELEASE)/contrib/check_inodes-freebsd.pl		> $$libexec/check_inodes-freebsd.pl;		\
		sed -e "s@utils.pm@$(GDMA_PREFIX)/libexec@"											\
		    nagios-plugins-$(PLUGINS_RELEASE)/contrib/check_javaproc.pl			> $$libexec/check_javaproc.pl;			\
		sed -e "s@/usr/local/nagios/libexec@$(GDMA_PREFIX)/libexec@"									\
		    nagios-plugins-$(PLUGINS_RELEASE)/contrib/check_linux_raid.pl		> $$libexec/check_linux_raid.pl;		\
		sed -e "s@/usr/local/nagios/libexec@$(GDMA_PREFIX)/libexec@"									\
		    nagios-plugins-$(PLUGINS_RELEASE)/contrib/check_lotus.pl			> $$libexec/check_lotus.pl;			\
		sed -e 's@/usr/local/bin/perl@/usr/bin/perl@'											\
		    nagios-plugins-$(PLUGINS_RELEASE)/contrib/check_nagios_db.pl		> $$libexec/check_nagios_db.pl;			\
		sed -e "s@/usr/local/nagios/libexec@$(GDMA_PREFIX)/libexec@"									\
		    -e 's@/usr/local/bin/perl@/usr/bin/perl@'											\
		    nagios-plugins-$(PLUGINS_RELEASE)/contrib/check_oracle_tbs			> $$libexec/check_oracle_tbs;			\
		sed -e "s@/usr/local/nagios/libexec@$(GDMA_PREFIX)/libexec@"									\
		    nagios-plugins-$(PLUGINS_RELEASE)/contrib/check_pfstate			> $$libexec/check_pfstate;			\
		sed -e "s@/usr/local/nagios/@/usr/local/groundwork/nagios/@"									\
		    nagios-plugins-$(PLUGINS_RELEASE)/contrib/check_remote_nagios_status.pl	> $$libexec/check_remote_nagios_status.pl;	\
		sed -e "s@/usr/libexec/nagios/plugins@$(GDMA_PREFIX)/libexec@"									\
		    nagios-plugins-$(PLUGINS_RELEASE)/contrib/check_rrd_data.pl			> $$libexec/check_rrd_data.pl;			\
		sed -e "s@/usr/local/nagios/libexec@$(GDMA_PREFIX)/libexec@"									\
		    -e 's@check_sap @check_sap.sh @'												\
		    nagios-plugins-$(PLUGINS_RELEASE)/contrib/check_sap.sh			> $$libexec/check_sap.sh;			\
		sed -e "s@/opt/nagios/libexec@$(GDMA_PREFIX)/libexec@"										\
		    -e 's@/usr/local/bin/perl@/usr/bin/perl@'											\
		    nagios-plugins-$(PLUGINS_RELEASE)/contrib/check_snmp_disk_monitor.pl	> $$libexec/check_snmp_disk_monitor.pl;		\
		sed -e 's@/usr/local/bin/perl@/usr/bin/perl@'											\
		    nagios-plugins-$(PLUGINS_RELEASE)/contrib/check_snmp_printer.pl		> $$libexec/check_snmp_printer.pl;		\
		sed -e "s@/opt/nagios/libexec /usr/local/libexec@$(GDMA_PREFIX)/libexec@"							\
		    -e 's@/usr/local/bin/perl@/usr/bin/perl@'											\
		    nagios-plugins-$(PLUGINS_RELEASE)/contrib/check_snmp_process_monitor.pl	> $$libexec/check_snmp_process_monitor.pl;	\
		sed -e "s@/usr/local/nagios/libexec@$(GDMA_PREFIX)/libexec@"									\
		    nagios-plugins-$(PLUGINS_RELEASE)/contrib/check_snmp_procs.pl		> $$libexec/check_snmp_procs.pl;		\
		sed -e "s@/usr/local/nagios/libexec@$(GDMA_PREFIX)/libexec@"									\
		    nagios-plugins-$(PLUGINS_RELEASE)/contrib/check_wins.pl			> $$libexec/check_wins.pl;			\
		sed -e "s@/usr/libexec/nagios/plugins@$(GDMA_PREFIX)/libexec@"									\
		    nagios-plugins-$(PLUGINS_RELEASE)/contrib/restrict.pl			> $$libexec/restrict.pl;			\
		sed -e "s@/usr/local/nagios/var/rw/nagios.cmd@/usr/local/groundwork/nagios/var/spool/nagios.cmd@"				\
		    nagios-plugins-$(PLUGINS_RELEASE)/contrib/sched_downtime.pl			> $$libexec/sched_downtime.pl;			\
		cp -p     nagios-plugins-$(PLUGINS_RELEASE)/plugins-scripts/check_*			$$libexec;				\
		cp -p     nagios-plugins-$(PLUGINS_RELEASE)/plugins-scripts/utils.pm			$$libexec;				\
		cp -p     nagios-plugins-$(PLUGINS_RELEASE)/plugins-scripts/utils.sh			$$libexec;				\
		sed -e "s@utils.pm@qw(/opt/groundwork/gdma/libexec)@"										\
		    nagios-plugins-$(PLUGINS_RELEASE)/plugins-scripts/check_breeze.pl		> $$libexec/check_breeze.pl;			\
		sed -e "s@utils.pm@qw(/opt/groundwork/gdma/libexec)@"										\
		    nagios-plugins-$(PLUGINS_RELEASE)/plugins-scripts/check_flexlm.pl		> $$libexec/check_flexlm.pl;			\
		sed -e 's@/usr/local/bin/perl@/usr/bin/perl@'											\
		    nagios-plugins-$(PLUGINS_RELEASE)/plugins-scripts/check_ifoperstatus.pl	> $$libexec/check_ifoperstatus.pl;		\
		sed -e 's@/usr/local/bin/perl@/usr/bin/perl@'											\
		    nagios-plugins-$(PLUGINS_RELEASE)/plugins-scripts/check_ifstatus.pl		> $$libexec/check_ifstatus.pl;			\
		sed -e "s@utils.pm@qw(/opt/groundwork/gdma/libexec)@"										\
		    nagios-plugins-$(PLUGINS_RELEASE)/plugins-scripts/check_ircd.pl		> $$libexec/check_ircd.pl;			\
		sed -e "s@utils.pm@qw(/opt/groundwork/gdma/libexec)@"										\
		    -e "s@/usr/local/bin/perl@/usr/bin/perl@"											\
		    nagios-plugins-$(PLUGINS_RELEASE)/plugins-scripts/check_rpc.pl		> $$libexec/check_rpc.pl;			\
		:;
		# FIX THIS:  future changes to plugins we distribute in this package:
		# (*) Convert pathnames in comments in check_digitemp.pl
		# (*) See why the base-product GroundWOrk check_ntp.pl plugin diverges significantly from
		#     the 1.4.11 plugin, and resolve the differences.  For now, we don't trust that the
		#     standard Nagios plugin will work, so we will stick with the GroundWork version at
		#     this time.
		# (*) Compare the GroundWork and the Nagios versions of the check_disk_smb.pl plugin.
		#     It's not clear at the moment which copy is more robust; that will take some testing.
		#     So for the moment, we're going to stick with the GroundWork version, though it's
		#     not at all clear that that's the right decision.
		# (*) Compare the GroundWork and the Nagios versions of the check_mailq.pl plugin.  It's
		#     not clear at the moment which copy is more robust; that will take some testing.
		#     So for the moment, we're going to stick with the GroundWork version, though it's
		#     not at all clear that that's the right decision.
		# (*) See why the base-product GroundWork check_wave.pl plugin diverges radically from the
		#     1.4.11 plugin, which is what we will currently ship with GDMA.
		#     (FIX THIS:  plugins has both contrib and plugins-scripts copies; check this out)
		# (*) For now, we will use the GroundWork-extended copy of check_mem.pl .  Possibly we
		#     should contribute that back to the Nagios plugins developers.
		# (*) The contributed check_compaq_insight.pl plugin is actually a folded-line email
		#     message the way it ships in the Nagios plugins distribution, and this format is
		#     unsuitable for execution.  Thus we delete it from our distribution.
		#
		# FIX THIS:  fix Solaris 6 and Solaris 8 support so they depend on our build of openssl
		# as well, by not copying libcrypto and libssl from an external source.
		#
		# FIX THIS:  build our own libldap and liblber libraries, and make sure that the nagios
		# check_ldap and check_ldaps plugins, and the libldap.so library, all refer internally
		# with proper RPATH settings only to our own copies of these librarys.
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
		case $(PLUGINS_RELEASE) in										\
		    1.4.12 | 1.4.13)											\
			mkdir -p                                $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;		\
			cp -p nagios-plugins-$(PLUGINS_RELEASE)/build-solaris/opt/groundwork/gdma/lib/charset.alias	\
								$(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;		\
			;;												\
		esac;													\
		:;
		@:;														\
		mkdir -p                                          $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/bin;			\
		cp -p   opt/groundwork/gdma/bin/gdma_check.pl     $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/bin;			\
		cp -p   opt/groundwork/gdma/bin/gdma_getconfig.pl $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/bin;			\
		cp -p   opt/groundwork/gdma/bin/send_nsca.pl      $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/bin;			\
		cp -p  /opt/groundwork/gdma/bin/openssl           $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/bin;			\
		mkdir -p                                          $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/openssl;			\
		cp -Rp  opt/groundwork/gdma/openssl/*             $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/openssl;			\
		mkdir -p                                          $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib/pkgconfig;		\
		cp -Rp  opt/groundwork/gdma/lib/*                 $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/lib;			\
		mkdir -p                                          $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/log;			\
		mkdir -p                                          $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/spool;			\
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
		# === What follows are last-minute patches for NFCU to augment and override what comes before, in certain cases. ===
		cp -p solaris_plugin_scripts/libexec/*            $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/libexec;			\
		sed -e "s@/usr/local/groundwork/bin/sar@/usr/bin/sar@"								\
		    solaris_plugin_scripts/libexec/check_cpu.pl > $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/libexec/check_cpu.pl;	\
		if [ $(SOLARIS_RELEASE) = 9 ]; then										\
		    cp -p solaris_updates/bin/gdma_check.pl.scp   $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/bin/gdma_check.pl;	\
		fi;														\
		if [ $(SOLARIS_RELEASE) = 10 ]; then										\
		    cp -p solaris_updates/bin/gdma_check.pl.https $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/bin/gdma_check.pl;	\
		fi;														\
		cp -p solaris_updates/etc/init.d/gdma            $(GDMA_PACKAGE_ABBREV)/etc/init.d;				\
		cp -p solaris_updates/etc/init.d/gdma            $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/etc/init.d;		\
		cp -p solaris_updates/libexec/check_cpu_stats.sh $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/libexec;			\
		cp -p solaris_updates/libexec/check_log2.pl      $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/libexec;			\
		cp -p solaris_updates/libexec/check_mem.pl       $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/libexec;			\
		cp -p solaris_updates/libexec/check_procl.sh     $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/libexec;			\
		cp -p solaris_updates/libexec/check_procr.sh     $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/libexec;			\
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
		sed -e 's/bin bin/root bin/' -e 's@opt/@@' nagios-plugins-$(PLUGINS_RELEASE)/build-solaris/prototype	>  $$prototype;	\
		find $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/libexec -type f \( -name "*.pl" -o -name "*.sh" -o -name "*.py" \) -print | xargs chmod 755;	\
		find $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/libexec -type f \( -name "*.pm" \)                                 -print | xargs chmod 644;	\
		find $(GDMA_PACKAGE_ABBREV)$(GDMA_PREFIX)/libexec -type f \( -name "*.pl" -o -name "*.sh" -o -name "*.pm" -o -name "*.py" \) -print	\
		    | egrep -v -e "/utils.pm|/utils.sh" | pkgproto											\
		    | sed -e "s@$(GDMA_PACKAGE_ABBREV)/opt/@@" -e "s@`/usr/xpg4/bin/id -un` `/usr/xpg4/bin/id -gn`@root bin@"	>> $$prototype;	\
		echo 'f none groundwork/gdma/libexec/check_dhcp                            4755 root bin'		>> $$prototype;	\
		echo 'f none groundwork/gdma/libexec/check_icmp                            4755 root bin'		>> $$prototype;	\
		echo 'f none groundwork/gdma/libexec/check_fan_cpq_present                 0755 root bin'		>> $$prototype;	\
		echo 'f none groundwork/gdma/libexec/check_fan_fsc_present                 0755 root bin'		>> $$prototype;	\
		echo 'f none groundwork/gdma/libexec/check_frontpage                       0755 root bin'		>> $$prototype;	\
		echo 'f none groundwork/gdma/libexec/check_temp_cpq                        0755 root bin'		>> $$prototype;	\
		echo 'f none groundwork/gdma/libexec/check_temp_fsc                        0755 root bin'		>> $$prototype;	\
		echo 'f none groundwork/gdma/libexec/rblcheck-dns                          0755 root bin'		>> $$prototype;	\
		echo 'f none groundwork/gdma/libexec/rblcheck-web                          0755 root bin'		>> $$prototype;	\
		echo 'f none groundwork/gdma/libexec/check_oracle_tbs                      0755 root bin'		>> $$prototype;	\
		echo 'f none groundwork/gdma/libexec/check_pfstate                         0755 root bin'		>> $$prototype;	\
		echo "f none /etc/init.d/gdma=$$PWD/$(GDMA_PACKAGE_ABBREV)/etc/init.d/gdma 0744 root sys"		>> $$prototype;	\
		echo 'l none /etc/rc0.d/K02gdma=../init.d/gdma'								>> $$prototype;	\
		echo 'l none /etc/rc1.d/K02gdma=../init.d/gdma'								>> $$prototype;	\
		echo 'l none /etc/rc2.d/K02gdma=../init.d/gdma'								>> $$prototype;	\
		echo 'l none /etc/rc3.d/S97gdma=../init.d/gdma'								>> $$prototype;	\
		echo 'l none /etc/rcS.d/K02gdma=../init.d/gdma'								>> $$prototype;	\
		echo 'd none groundwork/gdma/bin                              0755 gdma gdma'				>> $$prototype;	\
		echo 'f none groundwork/gdma/bin/gdma_check.pl                0744 gdma gdma'				>> $$prototype;	\
		echo 'f none groundwork/gdma/bin/gdma_getconfig.pl            0744 gdma gdma'				>> $$prototype;	\
		echo 'f none groundwork/gdma/bin/send_nsca.pl                 0744 gdma gdma'				>> $$prototype;	\
		echo 'd none groundwork/gdma/etc                              0755 gdma gdma'				>> $$prototype;	\
		echo 'd none groundwork/gdma/etc/init.d                       0755 gdma gdma'				>> $$prototype;	\
		echo 'f none groundwork/gdma/etc/init.d/gdma                  0744 gdma gdma'				>> $$prototype;	\
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
		echo "i preremove=$$PWD/scripts/$(GDMA_PACKAGE_ABBREV).preremove"					>> $$prototype;	\
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

local-openssl-build : check_path
		./openssl-build -r $(OPENSSL_RELEASE) -p $(OPENSSL_PREFIX)
		touch local-openssl-build

local-perl-build : check_path
		cd perl_modules; make
		touch local-perl-build

nagios-plugins-build	: nagios-plugins-$(PLUGINS_RELEASE)-build

nagios-plugins-$(PLUGINS_RELEASE)-build	: check_path
		# This library copying and symlink creation is probably a vain attempt to get check_ldap to
		# link properly to our private copy of the appropriate libararies during the plugins build.
		# Trouble is, the /opt/sfw/lib/libldap.so.2.0.122 library contains this RPATH reference:
		#     liblber.so.2 => /opt/sfw/lib/liblber.so.2
		# and similarly, the /opt/sfw/lib/libldap-2.2.so.7.0.10 library contains this RPATH reference
		#     liblber-2.2.so.7 => /opt/sfw/lib/liblber-2.2.so.7
		# so even copying the library to our own location won't have the desired effect.
		# To really get this to work will require that we build our own ldap library.
		mkdir -p                                        $(GDMA_PREFIX)/lib;				\
		if [ $(SOLARIS_RELEASE) = 9 ]; then								\
		    cp -p /opt/sfw/lib/liblber.so.2.0.122       $(GDMA_PREFIX)/lib;				\
		    cp -p /opt/sfw/lib/libldap.so.2.0.122       $(GDMA_PREFIX)/lib;				\
		    ln -s liblber.so.2.0.122                    $(GDMA_PREFIX)/lib/liblber.so.2;		\
		    ln -s libldap.so.2.0.122                    $(GDMA_PREFIX)/lib/libldap.so.2;		\
		fi;												\
		if [ $(SOLARIS_RELEASE) = 10 ]; then								\
		    if [ $(PROCESSOR) = sparc ]; then								\
			cp -p /opt/sfw/lib/liblber-2.2.so.7.0.10    $(GDMA_PREFIX)/lib;				\
			cp -p /opt/sfw/lib/libldap-2.2.so.7.0.10    $(GDMA_PREFIX)/lib;				\
			ln -s liblber-2.2.so.7.0.10                 $(GDMA_PREFIX)/lib/liblber-2.2.so.7;	\
			ln -s libldap-2.2.so.7.0.10                 $(GDMA_PREFIX)/lib/libldap-2.2.so.7;	\
		    fi;												\
		    cp -p /usr/sfw/lib/libmysqlclient.so.12.0.0 $(GDMA_PREFIX)/lib;				\
		    ln -s libmysqlclient.so.12.0.0              $(GDMA_PREFIX)/lib/libmysqlclient.so.12;	\
		fi;												\
		:;
		./make_nagios_plugins_on_solaris -r $(PLUGINS_RELEASE) -p $(GDMA_PREFIX) -s $(OPENSSL_PREFIX)
		touch nagios-plugins-$(PLUGINS_RELEASE)-build

# For this package, once we know the target servername, we enforce a cleanup before making the
# package, to get rid of any leftover data from previous runs.  Then we build a package named
# GWOSgdmakey-{servername} where the servername represents the site's GroundWork Monitor server.
#
# Note that Solaris package names may be no longer than 32 characters.  In Solaris 2.5.1 the limit
# was shorter, perhaps as little as 9 characters, but we're not anticpating supporting that platform.

gdmakey_package	:
		@:;											\
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
		    echo "OK numeric address '$$ipaddr'; the GDMA Key Package will be created.";	\
		else											\
		    echo "'$$ipaddr' is not an IP address; but the GDMA Key package will be created.";	\
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
		echo "ARCH='$(PROCESSOR)'"						>> $$pkginfo;	\
		echo "VERSION='$(GDMA_RELEASE),REV=$(BUILD_TIME)'"			>> $$pkginfo;	\
		echo "CATEGORY='application,monitoring'"				>> $$pkginfo;	\
		echo "VENDOR='GroundWork Open Source, Inc.'"				>> $$pkginfo;	\
		echo "EMAIL='support@groundworkopensource.com'"				>> $$pkginfo;	\
		echo "PSTAMP='$(GDMAKEY_TIME_PSTAMP)'"					>> $$pkginfo;	\
		echo "BASEDIR='/opt'"							>> $$pkginfo;	\
		echo "CLASSES='none'"							>> $$pkginfo;	\
		:;											\
		echo "====================================================================="	>  $$copyright;	\
		echo "Copyright (c) 2009 GroundWork Open Source, Inc.  All rights reserved."	>> $$copyright;	\
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
		echo | pkgtrans -s $$PWD/tmp $$PWD/$$gdmakey_install_root-$(GDMA_RELEASE)-solaris$(SOLARIS_RELEASE)-$(PROCESSOR).pkg;		\
		echo | /usr/sbin/pkgchk -d   $$PWD/$$gdmakey_install_root-$(GDMA_RELEASE)-solaris$(SOLARIS_RELEASE)-$(PROCESSOR).pkg;		\
		chmod 444                    $$PWD/$$gdmakey_install_root-$(GDMA_RELEASE)-solaris$(SOLARIS_RELEASE)-$(PROCESSOR).pkg;		\
		echo "Compressing the final package ...";											\
		gzip --best                  $$PWD/$$gdmakey_install_root-$(GDMA_RELEASE)-solaris$(SOLARIS_RELEASE)-$(PROCESSOR).pkg;		\
		:;																\
		echo "Creating the standalone key file for the GroundWork server ...";								\
		cp -p $$base_dir/id_dsa.pub $$PWD/$$gdmakey_install_root-$(GDMA_RELEASE)-solaris$(SOLARIS_RELEASE)-$(PROCESSOR).id_dsa.pub;	\
		chmod 444                   $$PWD/$$gdmakey_install_root-$(GDMA_RELEASE)-solaris$(SOLARIS_RELEASE)-$(PROCESSOR).id_dsa.pub;	\
		:;

install		:

perl-clean	: check_path
		cd perl_modules; make clean
		rm -f local-perl-build

openssl-clean	:
		rm -rf openssl-$(OPENSSL_RELEASE)
		rm -f  opt/groundwork/gdma/bin/c_rehash
		rm -f  opt/groundwork/gdma/bin/openssl
		rm -rf opt/groundwork/gdma/include
		rm -rf opt/groundwork/gdma/lib
		rm -rf opt/groundwork/gdma/man
		rm -rf opt/groundwork/gdma/openssl
		rm -f  local-openssl-build

plugins-clean	:
		if [ $(SOLARIS_RELEASE) = 9 ]; then			\
		    rm -f $(GDMA_PREFIX)/lib/liblber.so.2.0.122;	\
		    rm -f $(GDMA_PREFIX)/lib/libldap.so.2.0.122;	\
		    rm -f $(GDMA_PREFIX)/lib/liblber.so.2;		\
		    rm -f $(GDMA_PREFIX)/lib/libldap.so.2;		\
		fi;							\
		if [ $(SOLARIS_RELEASE) = 10 ]; then			\
		    rm -f $(GDMA_PREFIX)/lib/liblber-2.2.so.7.0.10;	\
		    rm -f $(GDMA_PREFIX)/lib/libldap-2.2.so.7.0.10;	\
		    rm -f $(GDMA_PREFIX)/lib/libmysqlclient.so.12.0.0;	\
		    rm -f $(GDMA_PREFIX)/lib/liblber-2.2.so.7;		\
		    rm -f $(GDMA_PREFIX)/lib/libldap-2.2.so.7;		\
		    rm -f $(GDMA_PREFIX)/lib/libmysqlclient.so.12;	\
		fi;							\
		:;
		rm -rf nagios-plugins-$(PLUGINS_RELEASE)
		rm -f  nagios-plugins-$(PLUGINS_RELEASE)-sol$(SOLARIS_RELEASE)-$(PROCESSOR)-local
		rm -f  nagios-plugins-$(PLUGINS_RELEASE)-build

gdma-clean	:
		rm -rf tmp/*
		rm -rf $(GDMA_PACKAGE_ABBREV)

package-clean	:
		rm -f  $(GDMA_PACKAGE_ABBREV)-*-solaris$(SOLARIS_RELEASE)-$(PROCESSOR).pkg
		rm -f  $(GDMA_PACKAGE_ABBREV)-*-solaris$(SOLARIS_RELEASE)-$(PROCESSOR).pkg.gz
		rm -f  $(GDMAKEY_PACKAGE_ABBREV_BASE)-*-*-solaris$(SOLARIS_RELEASE)-$(PROCESSOR).pkg
		rm -f  $(GDMAKEY_PACKAGE_ABBREV_BASE)-*-*-solaris$(SOLARIS_RELEASE)-$(PROCESSOR).pkg.gz
		rm -rf $(GDMAKEY_PACKAGE_ABBREV_BASE)-*
		rm -f  $(GDMAKEY_PACKAGE_SHORT_ABBREV)-*-*-solaris$(SOLARIS_RELEASE)-$(PROCESSOR).pkg
		rm -f  $(GDMAKEY_PACKAGE_SHORT_ABBREV)-*-*-solaris$(SOLARIS_RELEASE)-$(PROCESSOR).pkg.gz
		rm -rf $(GDMAKEY_PACKAGE_SHORT_ABBREV)-*

clean		: perl-clean openssl-clean plugins-clean gdma-clean
		rm -rf /opt/groundwork/gdma

distclean	: clean package-clean

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

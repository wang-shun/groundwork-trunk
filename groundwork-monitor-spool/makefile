# makefile for GroundWork Monitor Spool

# Copyright 2010 GroundWork Open Source, Inc. ("GroundWork").  All rights
# reserved.  Use is subject to GroundWork commercial license terms.

#---------------------------------------------------------------#

# How to build the Monitor Spool from scratch:
#
#     make clean rpms
#
# will yield an RPM in the rpmbuild/RPMS/ directory.

#---------------------------------------------------------------#

# This value should be constant throughout the lifespan of this RPM package.
RPM_PACKAGE_NAME = groundwork-monitor-spool

# This value must be updated for each new release, and the value must match the
# ${version} defined in the rpmsetup/$(RPM_PACKAGE_NAME)-%{version}.spec file.
RPM_PACKAGE_RELEASE = 0.0.1

# Let's label the tarballs with the build number, just like the RPMs.
SUBVERSION_BUILD := $(shell svn info -r HEAD | fgrep Revision | gawk '{print $$2}')

# We expect the HARDWARE_PLATFORM to be one of: 
#     i386      (32-bit platform)
#     x86_64    (64-bit platform)
# to match up with the short string that Perl uses when it compiles modules.
HARDWARE_PLATFORM := $(shell uname -i)

# Where to find the related code that we will pull into this RPM.
GDMA_BASE = ../monitor-agent/gdma/GDMA2.1/linux/gdma

#---------------------------------------------------------------#

default         : release

install         :

clean           : rpmclean

distclean	: clean

buildclean      : clean

#---------------------------------------------------------------#

all		:

build_info	:
		scripts/make_build_info $(RPM_PACKAGE_RELEASE) $(SUBVERSION_BUILD) > info/build_info

#---------------------------------------------------------------#

# Note:  The INSTALL_ROOT path must match the definition of %_installroot in the rpmsetup/$(RPM_PACKAGE_NAME).rpmmacros file.
# There doesn't seem to be any good way to automate this correspondence, so we just define it blindly here.
INSTALL_ROOT = $(PWD)/rpmbuild/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)/INSTALL_ROOT

GWPATH = /usr/local/groundwork

# monitor_spool_install     : build_info
monitor_spool_install     :
	@:
	mkdir -p                              $(INSTALL_ROOT)$(GWPATH)/config
	cp -p config/status-feeder.properties $(INSTALL_ROOT)$(GWPATH)/config
	@:
	mkdir -p                              $(INSTALL_ROOT)$(GWPATH)/common/doc/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)
	cp -p doc/SPOOL_INSTALL_NOTES         $(INSTALL_ROOT)$(GWPATH)/common/doc/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)
	# cp -p doc/SPOOL_RELEASE_NOTES         $(INSTALL_ROOT)$(GWPATH)/doc/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)
	# cp -p doc/$(RPM_PACKAGE_NAME).0.0.pdf $(INSTALL_ROOT)$(GWPATH)/doc/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)
	# @:
	@:
	mkdir -p                               $(INSTALL_ROOT)$(GWPATH)/foundation/feeder
	cp -p scripts/nagios2collage_socket.pl $(INSTALL_ROOT)$(GWPATH)/foundation/feeder
	@:
	mkdir -p                             $(INSTALL_ROOT)$(GWPATH)/perl/lib/site_perl/5.8.8/GDMA
	cp -p $(GDMA_BASE)/GDMA/GDMAUtils.pm $(INSTALL_ROOT)$(GWPATH)/perl/lib/site_perl/5.8.8/GDMA
	@:
	mkdir -p                                                 $(INSTALL_ROOT)$(GWPATH)/gdma/bin
	cp -p $(GDMA_BASE)/gdma-core/bin/gdma_spool_processor.pl $(INSTALL_ROOT)$(GWPATH)/gdma/bin
	@:
	mkdir -p                 $(INSTALL_ROOT)$(GWPATH)/core/services
	cp -p scripts/gwservices $(INSTALL_ROOT)$(GWPATH)/core/services
	@:
	mkdir -p                                $(INSTALL_ROOT)$(GWPATH)/core/services/spooler-gdma
	cp -p scripts/services/spooler-gdma/run $(INSTALL_ROOT)$(GWPATH)/core/services/spooler-gdma
	@:
	mkdir -p                                    $(INSTALL_ROOT)$(GWPATH)/core/services/spooler-gdma/log
	cp -p scripts/services/spooler-gdma/log/run $(INSTALL_ROOT)$(GWPATH)/core/services/spooler-gdma/log
	@:
	mkdir -p                                         $(INSTALL_ROOT)$(GWPATH)/core/services/spooler-gdma/log/main
	cp -p scripts/services/spooler-gdma/log/main/log $(INSTALL_ROOT)$(GWPATH)/core/services/spooler-gdma/log/main
	@:
	mkdir -p                                               $(INSTALL_ROOT)$(GWPATH)/core/services/spooler-gdma/log/supervise
	cp -p scripts/services/spooler-gdma/log/supervise/lock $(INSTALL_ROOT)$(GWPATH)/core/services/spooler-gdma/log/supervise
	@:
	mkdir -p                                           $(INSTALL_ROOT)$(GWPATH)/core/services/spooler-gdma/supervise
	cp -p scripts/services/spooler-gdma/supervise/lock $(INSTALL_ROOT)$(GWPATH)/core/services/spooler-gdma/supervise
	@:
	rm -f     $(INSTALL_ROOT)$(GWPATH)/core/services/spooler-gdma/log/supervise/control
	mknod     $(INSTALL_ROOT)$(GWPATH)/core/services/spooler-gdma/log/supervise/control p
	chmod 600 $(INSTALL_ROOT)$(GWPATH)/core/services/spooler-gdma/log/supervise/control
	rm -f     $(INSTALL_ROOT)$(GWPATH)/core/services/spooler-gdma/supervise/control
	mknod     $(INSTALL_ROOT)$(GWPATH)/core/services/spooler-gdma/supervise/control p
	chmod 600 $(INSTALL_ROOT)$(GWPATH)/core/services/spooler-gdma/supervise/control
	@:
	mkdir -p                         $(INSTALL_ROOT)$(GWPATH)/gdma/config
	cp -p config/gdma_auto.conf      $(INSTALL_ROOT)$(GWPATH)/gdma/config
	cp -p config/gwmon_localhost.cfg $(INSTALL_ROOT)$(GWPATH)/gdma/config
	@:
	mkdir -p $(INSTALL_ROOT)$(GWPATH)/gdma/log
	mkdir -p $(INSTALL_ROOT)$(GWPATH)/gdma/spool
	mkdir -p $(INSTALL_ROOT)$(GWPATH)/gdma/tmp

#---------------------------------------------------------------#

# Note on how we build RPMs:
#
# The standard rpmbuild setup depends on a ~/.rpmmacros file.  That's a truly idiotic design, because the settings
# in that file will vary from person to person, and there is absolutely no guarantee that your settings won't be
# incompatible with what it takes to build a given package.  Instead, we force all details to be specified here
# inside our source tree, and we intentionally do not depend on any resource outside the source tree.
#
# Also note that we don't use ths standard organization of build directories.  Again, such a setup might suit Red Het's
# internal processes for a Linux distribution, but it creates a nightmare of files spread out across multiple directories
# and interleaved with the files from completely separate packages.  Instead, we create a package-specific file tree for
# everything related to the one package we're trying to build.
#
# Also note that, for the time being, we're emphatically not trying to build from a source RPM.  We have subversion
# for that, and we already have a checked-out copy of the source code.  Maybe at some point in the future we might
# consider building a source-RPM distribution, but certainly not until we've got all the kinks worked out from the
# process for building binary RPMs from the source code.
#
# Also note that the rpmbuild(8) man page is apparently poorly maintained, and does not reflect all the command-line
# options available.  Try "rpmbuild --help" for more detail.
#
# Also note that we haven't been able to get the "rpmbuild --macros" option to work, even though it is listed in the
# help output.  So we set this value instead as "macrofiles:" in the rcfile.
#
# If we had need of supporting other platforms, we would need to generalize the "redhat" platform component in the
# "/usr/lib/rpm/redhat/rpmrc" pathname below.

rpms            : monitor_spool_install
		rpmbuild												\
		    --rcfile /usr/lib/rpm/rpmrc:/usr/lib/rpm/redhat/rpmrc:$(PWD)/rpmsetup/$(RPM_PACKAGE_NAME).rpmrc	\
		    --dbpath $(PWD)/rpmsetup										\
		    --quiet												\
		    -bb --short-circuit rpmsetup/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE).spec 2>&1

rpmclean        :
		rm -rf rpmbuild
		rm -rf info/build_info

#---------------------------------------------------------------#

# This target makes a full release from scratch, all the way from complete cleanup of any debris left behind
# by previous builds, to recompiling the world and doing the final packaging.  We establish this target to
# ensure we have a repeatable way to guarantee that the final packaged bits really do represent the version
# of the checked-in code that it's supposed to.

# Note:  For a formal external release, before building this target you must:
# * update the RPM_PACKAGE_RELEASE value above
# * make sure the rpmsetup/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE).spec file has its version-component values correctly set
# * check in the revised copies of this makefile and the specfile
# * make sure all the checked-out code is up-to-date (run "svn status -u" to check this)

release         : rpmsetup/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE).spec
		make buildclean
		make rpms

# makefile for the groundwork fping feeder module

# Copyright 2009 GroundWork Open Source, Inc. ("GroundWork").  All rights
# reserved.  Use is subject to GroundWork commercial license terms.

#---------------------------------------------------------------#

# This value must be updated for each new release, and the value must match the ${version}
# defined in the rpmsetup/groundwork-fping-feeder-%{version}.spec file.
FPING_FEEDER_RELEASE = 4.1.1

# Let's label the tarballs with the build number, just like the RPMs.
SUBVERSION_BUILD := $(shell svn info -r HEAD | fgrep Revision | gawk '{print $$2}')

#---------------------------------------------------------------#

# We expect the MACHINE_TYPE to be one of:
#     i686	(32-bit platform)
#     x86_64	(64-bit platform)
MACHINE_TYPE := $(shell uname -m)

# We expect the HARDWARE_PLATFORM to be one of:
#     i386	(32-bit platform)
#     x86_64	(64-bit platform)
# HARDWARE_PLATFORM := $(shell uname -i)

# But until we distribute some platform-specific binaries,
# we'll treat this as an anonymous platform.
HARDWARE_PLATFORM := noarch

#---------------------------------------------------------------#

default		: release

install		:

clean		: rpmclean tarballclean

buildclean	: clean

#---------------------------------------------------------------#

# For the time being, we won't supply any BIN_FILES as part of our RPMs or tarballs.
# Instead, we'll allow them to be downloaded separately, when we distribute this code,
# or they should be supplied in the base GroundWork Monitor 5.3 (or later) release.

BIN_FILES=						\
	bin/fping.rhel4.i386				\
	bin/fping.rhel4_64.x86_64

CONFIG_FILES=						\
	config/fping_process.conf

# This filename must be updated for each new release.
DOC_FILES=						\
	doc/FPING_FEEDER_RELEASE_NOTES			\
	doc/groundwork-fping-feeder.3.0.pdf

SCRIPT_FILES=						\
	scripts/fping_process.pl			\
	scripts/install_fping.sh			\
	scripts/service_profile_fping_feeder.xml

# Note:  For the named pipes which are part of the distribution, we must create them
# rather than copy them from an existing file tree, because Subversion does not support
# having them as a checked-in file type.

#---------------------------------------------------------------#

# tarballs are obsolescent, but may still find some use in some installations.

RELEASE_DIR		= groundwork-fping-feeder-$(FPING_FEEDER_RELEASE)-$(SUBVERSION_BUILD).$(HARDWARE_PLATFORM)
ALL_RELEASE_DIRS	= groundwork-fping-feeder-*-*.$(HARDWARE_PLATFORM)

tars			: fpingfeedertarball

$(RELEASE_DIR)		:
			mkdir -p $@

tarballs		:
			mkdir -p $@

# We cannot do recursive copies, because we might then include a bunch of unwanted .../.svn/... file trees.

fpingfeedertarball	: $(RELEASE_DIR) tarballs
			@:
			cp -p $(CONFIG_FILES) $(RELEASE_DIR)
			chmod 600             $(RELEASE_DIR)/fping_process.conf
			cp -p $(DOC_FILES)    $(RELEASE_DIR)
			cp -p $(SCRIPT_FILES) $(RELEASE_DIR)
			@:
			mkdir -p                                       $(RELEASE_DIR)/services/feeder-nagios-fping
			cp -p scripts/services/feeder-nagios-fping/run $(RELEASE_DIR)/services/feeder-nagios-fping
			@:
			mkdir -p                                           $(RELEASE_DIR)/services/feeder-nagios-fping/log
			cp -p scripts/services/feeder-nagios-fping/log/run $(RELEASE_DIR)/services/feeder-nagios-fping/log
			@:
			mkdir -p                                                $(RELEASE_DIR)/services/feeder-nagios-fping/log/main
			cp -p scripts/services/feeder-nagios-fping/log/main/log $(RELEASE_DIR)/services/feeder-nagios-fping/log/main
			@:
			mkdir -p                                                        $(RELEASE_DIR)/services/feeder-nagios-fping/log/supervise
			cp -p scripts/services/feeder-nagios-fping/log/supervise/status $(RELEASE_DIR)/services/feeder-nagios-fping/log/supervise
			cp -p scripts/services/feeder-nagios-fping/log/supervise/lock   $(RELEASE_DIR)/services/feeder-nagios-fping/log/supervise
			@:
			mkdir -p                                                    $(RELEASE_DIR)/services/feeder-nagios-fping/supervise
			cp -p scripts/services/feeder-nagios-fping/supervise/status $(RELEASE_DIR)/services/feeder-nagios-fping/supervise
			cp -p scripts/services/feeder-nagios-fping/supervise/lock   $(RELEASE_DIR)/services/feeder-nagios-fping/supervise
			@:
			rm -f     $(RELEASE_DIR)/services/feeder-nagios-fping/log/supervise/control
			mknod     $(RELEASE_DIR)/services/feeder-nagios-fping/log/supervise/control p
			chmod 600 $(RELEASE_DIR)/services/feeder-nagios-fping/log/supervise/control
			rm -f     $(RELEASE_DIR)/services/feeder-nagios-fping/supervise/control
			mknod     $(RELEASE_DIR)/services/feeder-nagios-fping/supervise/control p
			chmod 600 $(RELEASE_DIR)/services/feeder-nagios-fping/supervise/control
			@:
			mkdir -p                       $(RELEASE_DIR)/etc/logrotate.d
			cp -p scripts/groundwork-fping $(RELEASE_DIR)/etc/logrotate.d
			@:
			tar cfz tarballs/$(RELEASE_DIR).tar.gz $(RELEASE_DIR)

tarballclean		:
			rm -rf $(ALL_RELEASE_DIRS) tarballs

#---------------------------------------------------------------#

# Note:  The INSTALL_ROOT path must match the definition of %_installroot in the rpmsetup/groundwork-fping-feeder.rpmmacros file.
# There doesn't seem to be any good way to automate this correspondence, so we just define it blindly here.
INSTALL_ROOT = $(PWD)/rpmbuild/groundwork-fping-feeder-$(FPING_FEEDER_RELEASE)/INSTALL_ROOT

GWPATH = /usr/local/groundwork

# We don't include the scripts/install_fping.sh script in an RPM,
# because its functionality is subsumed by the RPM itself.

# We cannot do recursive copies, because we might then include a bunch of unwanted .../.svn/... file trees.

fping_feeder_install	:
	@:
	mkdir -p              $(INSTALL_ROOT)$(GWPATH)/common/etc
	cp -p $(CONFIG_FILES) $(INSTALL_ROOT)$(GWPATH)/common/etc
	@:
	mkdir -p           $(INSTALL_ROOT)$(GWPATH)/common/doc/groundwork-fping-feeder-$(FPING_FEEDER_RELEASE)
	cp -p $(DOC_FILES) $(INSTALL_ROOT)$(GWPATH)/common/doc/groundwork-fping-feeder-$(FPING_FEEDER_RELEASE)
	@:
	mkdir -p                       $(INSTALL_ROOT)$(GWPATH)/nagios/libexec
	cp -p scripts/fping_process.pl $(INSTALL_ROOT)$(GWPATH)/nagios/libexec
	@:
	mkdir -p                       $(INSTALL_ROOT)$(GWPATH)/foundation/feeder
	cp -p scripts/fping_process.pl $(INSTALL_ROOT)$(GWPATH)/foundation/feeder
	@:
	mkdir -p                                       $(INSTALL_ROOT)$(GWPATH)/core/profiles
	cp -p scripts/service_profile_fping_feeder.xml $(INSTALL_ROOT)$(GWPATH)/core/profiles
	@:
	mkdir -p                                       $(INSTALL_ROOT)$(GWPATH)/core/services/feeder-nagios-fping
	cp -p scripts/services/feeder-nagios-fping/run $(INSTALL_ROOT)$(GWPATH)/core/services/feeder-nagios-fping
	@:
	mkdir -p                                           $(INSTALL_ROOT)$(GWPATH)/core/services/feeder-nagios-fping/log
	cp -p scripts/services/feeder-nagios-fping/log/run $(INSTALL_ROOT)$(GWPATH)/core/services/feeder-nagios-fping/log
	@:
	mkdir -p                                                $(INSTALL_ROOT)$(GWPATH)/core/services/feeder-nagios-fping/log/main
	cp -p scripts/services/feeder-nagios-fping/log/main/log $(INSTALL_ROOT)$(GWPATH)/core/services/feeder-nagios-fping/log/main
	@:
	mkdir -p                                                        $(INSTALL_ROOT)$(GWPATH)/core/services/feeder-nagios-fping/log/supervise
	cp -p scripts/services/feeder-nagios-fping/log/supervise/status $(INSTALL_ROOT)$(GWPATH)/core/services/feeder-nagios-fping/log/supervise
	cp -p scripts/services/feeder-nagios-fping/log/supervise/lock   $(INSTALL_ROOT)$(GWPATH)/core/services/feeder-nagios-fping/log/supervise
	@:
	mkdir -p                                                    $(INSTALL_ROOT)$(GWPATH)/core/services/feeder-nagios-fping/supervise
	cp -p scripts/services/feeder-nagios-fping/supervise/status $(INSTALL_ROOT)$(GWPATH)/core/services/feeder-nagios-fping/supervise
	cp -p scripts/services/feeder-nagios-fping/supervise/lock   $(INSTALL_ROOT)$(GWPATH)/core/services/feeder-nagios-fping/supervise
	@:
	rm -f     $(INSTALL_ROOT)$(GWPATH)/core/services/feeder-nagios-fping/log/supervise/control
	mknod     $(INSTALL_ROOT)$(GWPATH)/core/services/feeder-nagios-fping/log/supervise/control p
	chmod 600 $(INSTALL_ROOT)$(GWPATH)/core/services/feeder-nagios-fping/log/supervise/control
	rm -f     $(INSTALL_ROOT)$(GWPATH)/core/services/feeder-nagios-fping/supervise/control
	mknod     $(INSTALL_ROOT)$(GWPATH)/core/services/feeder-nagios-fping/supervise/control p
	chmod 600 $(INSTALL_ROOT)$(GWPATH)/core/services/feeder-nagios-fping/supervise/control
	@:
	mkdir -p                       $(INSTALL_ROOT)/etc/logrotate.d
	cp -p scripts/groundwork-fping $(INSTALL_ROOT)/etc/logrotate.d

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

rpms		: fping_feeder_install
		rpmbuild												\
		    --rcfile /usr/lib/rpm/rpmrc:/usr/lib/rpm/redhat/rpmrc:$(PWD)/rpmsetup/groundwork-fping-feeder.rpmrc	\
		    --dbpath $(PWD)/rpmsetup										\
		    --quiet												\
		    -bb --short-circuit rpmsetup/groundwork-fping-feeder-$(FPING_FEEDER_RELEASE).spec 2>&1

rpmclean	:
		rm -rf rpmbuild

#---------------------------------------------------------------#

# This target makes a full release from scratch, all the way from complete cleanup of any debris left behind
# by previous builds, to recompiling the world and doing the final packaging.  We establish this target to
# ensure we have a repeatable way to guarantee that the final packaged bits really do represent the version
# of the checked-in code that it's supposed to.

# Note:  For a formal external release, before building this target you must:
# * update the FPING_FEEDER_RELEASE value above
# * make sure the rpmsetup/groundwork-fping-feeder-$(FPING_FEEDER_RELEASE).spec file has its version-component values correctly set
# * check in the revised copies of this makefile and the specfile
# * make sure all the checked-out code is up-to-date (run "svn status -u" to check this)

release		: rpmsetup/groundwork-fping-feeder-$(FPING_FEEDER_RELEASE).spec
		make buildclean
		make rpms tars

# makefile for the groundwork perl typedconfig package

# Copyright 2009 GroundWork Open Source, Inc. ("GroundWork").  All rights
# reserved.  Use is subject to GroundWork commercial license terms.

#---------------------------------------------------------------#

# This value must be updated for each new release, and the value must match the ${version}
# defined in the rpmsetup/groundwork-perl-typedconfig-%{version}.spec file.
PERL_TYPEDCONFIG_RELEASE = 2.0.0

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

# tarballs are obsolescent, but may still find some use in some installations.

RELEASE_DIR		= groundwork-perl-typedconfig-$(PERL_TYPEDCONFIG_RELEASE)-$(SUBVERSION_BUILD).$(HARDWARE_PLATFORM)
ALL_RELEASE_DIRS	= groundwork-perl-typedconfig-*-*.$(HARDWARE_PLATFORM)

tars			: typedconfigtarball

$(RELEASE_DIR)		:
			mkdir -p $@

tarballs		:
			mkdir -p $@

typedconfigtarball	: $(RELEASE_DIR) tarballs
			@:
			mkdir -p                     $(RELEASE_DIR)
			cp -p scripts/TypedConfig.pm $(RELEASE_DIR)
			@:
			tar cfz tarballs/$(RELEASE_DIR).tar.gz $(RELEASE_DIR)

tarballclean		:
			rm -rf $(ALL_RELEASE_DIRS) tarballs

#---------------------------------------------------------------#

# Note:  The INSTALL_ROOT path must match the definition of %_installroot in the rpmsetup/groundwork-perl-typedconfig.rpmmacros file.
# There doesn't seem to be any good way to automate this correspondence, so we just define it blindly here.
INSTALL_ROOT = $(PWD)/rpmbuild/groundwork-perl-typedconfig-$(PERL_TYPEDCONFIG_RELEASE)/INSTALL_ROOT

GWPATH = /usr/local/groundwork

perl_typedconfig_install	:
	@:
	mkdir -p                     $(INSTALL_ROOT)$(GWPATH)/nagios/libexec
	cp -p scripts/TypedConfig.pm $(INSTALL_ROOT)$(GWPATH)/nagios/libexec

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

rpms		: perl_typedconfig_install
		rpmbuild													\
		    --rcfile /usr/lib/rpm/rpmrc:/usr/lib/rpm/redhat/rpmrc:$(PWD)/rpmsetup/groundwork-perl-typedconfig.rpmrc	\
		    --dbpath $(PWD)/rpmsetup											\
		    --quiet													\
		    -bb --short-circuit rpmsetup/groundwork-perl-typedconfig-$(PERL_TYPEDCONFIG_RELEASE).spec 2>&1

rpmclean	:
		rm -rf rpmbuild

#---------------------------------------------------------------#

# This target makes a full release from scratch, all the way from complete cleanup of any debris left behind
# by previous builds, to recompiling the world and doing the final packaging.  We establish this target to
# ensure we have a repeatable way to guarantee that the final packaged bits really do represent the version
# of the checked-in code that it's supposed to.

# Note:  For a formal external release, before building this target you must:
# * update the PERL_TYPEDCONFIG_RELEASE value above
# * make sure the rpmsetup/groundwork-perl-typedconfig-$(PERL_TYPEDCONFIG_RELEASE).spec file has its version-component values correctly set
# * check in the revised copies of this makefile and the specfile
# * make sure all the checked-out code is up-to-date (run "svn status -u" to check this)

release		: rpmsetup/groundwork-perl-typedconfig-$(PERL_TYPEDCONFIG_RELEASE).spec
		make buildclean
		make rpms tars

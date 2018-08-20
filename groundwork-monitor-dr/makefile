# makefile for GroundWork DR replication

# Copyright 2010 GroundWork Open Source, Inc. ("GroundWork").  All rights
# reserved.  Use is subject to GroundWork commercial license terms.

#---------------------------------------------------------------#

# To do:
# (*) Perhaps implement a new make target to look for installed-RPM changes,
#     list them, and compare to corresponding checked-out subversion files,
#     to see if you have captured all your local in-place edits made in the
#     working installed copy of the RPM on your development machine.

#---------------------------------------------------------------#

# This value should be constant throughout the lifespan of this RPM package.
RPM_PACKAGE_NAME = groundwork-disaster-recovery

# This value must be updated for each new release, and the value must match the
# ${version} defined in the rpmsetup/$(RPM_PACKAGE_NAME)-%{version}.spec file.
RPM_PACKAGE_RELEASE = 0.3.0

# Let's label the tarballs with the build number, just like the RPMs.
SUBVERSION_BUILD := $(shell svn info -r HEAD | fgrep Revision | gawk '{print $$2}')

# We expect the HARDWARE_PLATFORM to be one of: 
#     i386      (32-bit platform)
#     x86_64    (64-bit platform)
# to match up with the short string that Perl uses when it compiles modules.
HARDWARE_PLATFORM := $(shell uname -i)

#---------------------------------------------------------------#

default		: release

install		:

clean		: rpmclean
		cd packages; make clean

distclean	: buildclean
		cd packages; make distclean
		# rm -rf logs

buildclean	: clean
		rm -f info/build_info

#---------------------------------------------------------------#

# Here we provide a standardized way to collect statistics on the entire
# body of code.  In the future, we might offer additional metrics, such
# as the number of lines of code in each language, or the breakdown of
# lines of code by {blank, comments, braces, logic}, or the number of FIX
# breadcrumbs of each type (FIX THIS, FIX MAJOR, FIX MINOR, FIX LATER).

SOURCE_FILES = \
	actions/*/*/*				\
	bin/*					\
	config/replication.conf			\
	doc/file_structures_and_protocols	\
	doc/notification_authority		\
	doc/INSTALLATION			\
	doc/OPERATION				\
	makefile				\
	modules/Replication/*			\
	modules/makefile			\
	packages/makefile			\
	patches/*				\
	profiles/*				\
	rpmsetup/*				\
	scripts/*

counts	:
	wc $(SOURCE_FILES)

#---------------------------------------------------------------#

all		: perl conf logs

perl		: perl_packages perl_modules

perl_packages	:
		cd packages; make

perl_modules	:
		cd modules; make

# For some idiotic reason, "make rpms" executed here will turn the desired
# 600 permissions on the config/replication.conf file to 644.  How this can
# happen is mysterious and stupid, as making an RPM should not touch any
# code or files other than perhaps what is in the build root.  Eventually
# we will track down the cause and deal with it.  In the meantime, we
# provide this next make target to clean up.
# FIX THIS:  track down why that happens; fix it; and submit a bug report
# to the rpmbuild developers
conf		: config/replication.conf
		chmod 600 config/replication.conf

logs		:
		mkdir -p logs

build_info	:
		scripts/make_build_info $(RPM_PACKAGE_RELEASE) $(SUBVERSION_BUILD) > info/build_info

# This target is here as a convenience in constructing a filelist for the specfile.
# It is not to be run in an automated fashion for every build, as that would defeat
# the purpose of having the RPM build do a lot of checking to ensure that you got
# everything and only the things included in the RPM that you intended to.  Rather,
# this should be run only when you believe you have a properly and fully constructed
# tree of files, and then the result should be saved for future use.
filelist	: perl_filelist

perl_filelist	:
		scripts/make_filelist nagios nagios '%{reppath}' perl/* \
		    > rpmsetup/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)-$(HARDWARE_PLATFORM).perl_filelist

#---------------------------------------------------------------#

# Note:  The INSTALL_ROOT path must match the definition of %_installroot in the rpmsetup/$(RPM_PACKAGE_NAME).rpmmacros file.
# There doesn't seem to be any good way to automate this correspondence, so we just define it blindly here.
INSTALL_ROOT = $(PWD)/rpmbuild/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)/INSTALL_ROOT

GWPATH = /usr/local/groundwork
REPPATH = $(GWPATH)/replication

# perllocal.pod overlays an existing file from GroundWork Monitor.
# So for the time being, as it seems not to be critical to the operation of this module, we suppress it.

# "perl" ought to be a dependency here too, so "make rpms" will do everything from scratch.
# But in our present construction of that target, it remakes everything under perl/... even
# when that's not really needed.  So since in practice we change the perl tree infrequently,
# we depend here instead on a preceding "make all" or "make perl" to get this dependency built.
disaster_recovery_install	: perl_modules build_info
	@:
	mkdir -p    $(INSTALL_ROOT)$(REPPATH)/bin
	cp -p bin/* $(INSTALL_ROOT)$(REPPATH)/bin
	@:
	mkdir -p       $(INSTALL_ROOT)$(REPPATH)/config
	cp -p config/* $(INSTALL_ROOT)$(REPPATH)/config
	@:
	mkdir -p                              $(INSTALL_ROOT)$(REPPATH)/doc/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)
	cp -p doc/INSTALLATION                $(INSTALL_ROOT)$(REPPATH)/doc/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)
	cp -p doc/OPERATION                   $(INSTALL_ROOT)$(REPPATH)/doc/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)
	# cp -p doc/RECOVERY_RELEASE_NOTES      $(INSTALL_ROOT)$(REPPATH)/doc/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)
	# cp -p doc/$(RPM_PACKAGE_NAME).0.0.pdf $(INSTALL_ROOT)$(REPPATH)/doc/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)
	@:
	mkdir -p      $(INSTALL_ROOT)$(REPPATH)/perl
	cp -pr perl/* $(INSTALL_ROOT)$(REPPATH)/perl
	@:
	mkdir -p              $(INSTALL_ROOT)$(REPPATH)/info
	cp -p info/build_info $(INSTALL_ROOT)$(REPPATH)/info
	@:
	mkdir -p                        $(INSTALL_ROOT)$(GWPATH)/nagios/libexec
	cp -p scripts/check_replication $(INSTALL_ROOT)$(GWPATH)/nagios/libexec
	@:
	mkdir -p                                             $(INSTALL_ROOT)$(GWPATH)/core/profiles
	cp -p profiles/service-profile-disaster-recovery.xml $(INSTALL_ROOT)$(GWPATH)/core/profiles
	@:
	mkdir -p                                   $(INSTALL_ROOT)$(REPPATH)/scripts
	cp -p scripts/erase_readies_obj            $(INSTALL_ROOT)$(REPPATH)/scripts
	cp -p scripts/generic_*                    $(INSTALL_ROOT)$(REPPATH)/scripts
	cp -p scripts/make_backup_obj              $(INSTALL_ROOT)$(REPPATH)/scripts
	cp -p scripts/make_ready_obj               $(INSTALL_ROOT)$(REPPATH)/scripts
	cp -p scripts/make_replica_*               $(INSTALL_ROOT)$(REPPATH)/scripts
	cp -p scripts/make_staged_*                $(INSTALL_ROOT)$(REPPATH)/scripts
	cp -p scripts/make_working_*               $(INSTALL_ROOT)$(REPPATH)/scripts
	cp -p scripts/monarch_preflight_and_commit $(INSTALL_ROOT)$(REPPATH)/scripts
	cp -p scripts/prune_backups_obj            $(INSTALL_ROOT)$(REPPATH)/scripts
	cp -p scripts/selective_copy               $(INSTALL_ROOT)$(REPPATH)/scripts
	(cd $(INSTALL_ROOT)$(REPPATH)/scripts; rm -f make_shadow_app; ln -s make_staged_app make_shadow_app)
	(cd $(INSTALL_ROOT)$(REPPATH)/scripts; rm -f make_shadow_db ; ln -s make_staged_db  make_shadow_db )
	@:
	mkdir -p $(INSTALL_ROOT)$(REPPATH)/backups
	mkdir -p $(INSTALL_ROOT)$(REPPATH)/pending
	@:
	mkdir -p $(INSTALL_ROOT)$(REPPATH)/logs
	mkdir -p $(INSTALL_ROOT)$(REPPATH)/var
	@:
	# We don't use "cp -pr" here for the recursive copying because we
	# need to exclude all the Subversion files (.svn/...) in this tree,
	# along with any transient edit-session buffer files (*.swp, for vi).
	#
	# The alternative would be to copy such files and then delete them:
	# cp -pr actions $(INSTALL_ROOT)$(REPPATH)
	# find $(INSTALL_ROOT)$(REPPATH)/actions -depth -name .svn -exec rm -rf '{}' \;
	#
	(tar cf - --exclude .svn --exclude '*.swp' actions) | (cd $(INSTALL_ROOT)$(REPPATH) ; tar xvpBf -)

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

rpms		: disaster_recovery_install
		rpmbuild												\
		    --rcfile /usr/lib/rpm/rpmrc:/usr/lib/rpm/redhat/rpmrc:$(PWD)/rpmsetup/$(RPM_PACKAGE_NAME).rpmrc	\
		    --dbpath $(PWD)/rpmsetup										\
		    --quiet												\
		    -bb --short-circuit rpmsetup/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE).spec 2>&1
		# rpmbuild inappropriately mangles the permissions of files to make them world-readable.
		# We need to track down why that is happening, and report it as a bug, since it should not
		# be touching anything outside of the build root.  In any case, we need to put back the
		# permissions into their proper condition for local testing.
		make conf

rpmclean	:
		rm -rf rpmbuild

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

release		: rpmsetup/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE).spec
		make buildclean
		make rpms

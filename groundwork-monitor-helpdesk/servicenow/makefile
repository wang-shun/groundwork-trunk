# makefile for GroundWork ServiceNow Integration

# Copyright 2015 GroundWork Open Source, Inc. ("GroundWork").  All
# rights reserved.  Use is subject to GroundWork commercial license terms.

#---------------------------------------------------------------#

# Before building any RPMs, make sure the patch for /usr/lib/rpm/perl.req
# that is described in patches/README is applied to your system, if need be.

#---------------------------------------------------------------#

# How to build the ServiceNow Integration from scratch, immediately after checkout:
#
#     make distclean perl
#     make clean rpms
#
# will yield an RPM in the rpmbuild/RPMS/ directory.
#
# Once you have executed "make distclean perl" and then "make clean", you will be
# left with a clean set of perl/*/ files to use for additional runs of "make rpms".
# Either don't mess with this, or "make distclean" to start again from scratch.

#---------------------------------------------------------------#

# How to rebuild the filelists from scratch:
#
# This should be done only rarely, namely under one of these conditions:
# * you need to add a new Perl package to the build
# * you need to delete some Perl package from the build
# * you need to update some Perl package in the build
#
# We want to make sure we respect the read-only nature of the constructed Perl
# files.  To that end, you should only "make perl_filelist" immediately after
# having executed "make distclean".  (Rather stupidly, the "rpm" command messes
# with the permissions on the source files, so we need to start with a clean
# setup before constructing the filelists, which have file permissions embedded
# within them.)  So:
#
#     make distclean perl
#     make clean perl_filelist
#
# Once you have pristine filelists built (in the rpmsetup/ subdirectory), they
# should be checked in and used thereafter for future builds, without needing
# to re-create them.

#---------------------------------------------------------------#

# This value should be constant throughout the lifespan of this RPM package.
RPM_PACKAGE_NAME = groundwork-servicenow-integration

# This value must be updated for each new release, and the value must match the
# ${version} defined in the rpmsetup/$(RPM_PACKAGE_NAME)-%{version}.spec file, and
# also in whatever must be updated in the doc/SERVICENOW_INTEGRATION_INSTALL_NOTES
# file.
RPM_PACKAGE_RELEASE = 2.0.0

# Let's include the build number in the generated build info file.
SUBVERSION_BUILD := $(shell svn info -r HEAD | fgrep Revision | gawk '{print $$2}')

# We expect the HARDWARE_PLATFORM to be one of:
#     i386      (32-bit platform)
#     x86_64    (64-bit platform)
# to match up with the short string that Perl uses when it compiles modules.
HARDWARE_PLATFORM := $(shell uname -i)

#---------------------------------------------------------------#

# Certain configuration files ought to have restricted permissions because
# they contain credentials for accessing system resources.
RESTRICTED_FILES =			\
	config/servicenow_module.conf

#---------------------------------------------------------------#

# Making the default target depends on a previous "make perl".  But we don't make
# that a dependency because it takes too much time for normal development, when it
# is almost always unnecessary after the first time the Perl code has been built.
# (The only time you need to repeat that is after changing what goes into the Perl
# libraries in this RPM.)  See the notes at the top of the makefile for more detail
# on building from scratch.
default         : release

install         :

clean           : rpmclean
		cd packages; make clean

distclean	: clean
		cd packages; make distclean

buildclean      : clean

#---------------------------------------------------------------#

# Here we provide a standardized way to collect statistics on the entire
# body of code.  In the future, we might offer additional metrics, such
# as the number of lines of code in each language, or the breakdown of
# lines of code by {blank, comments, braces, logic}, or the number of FIX
# breadcrumbs of each type (FIX THIS, FIX MAJOR, FIX MINOR, FIX LATER).

SOURCE_FILES = \
	bin/*			\
	config/*		\
	db/*			\
	doc/*			\
	makefile		\
	modules/makefile	\
	modules/*/*		\
	packages/README		\
	packages/makefile	\
	rpmsetup/*		\
	scripts/*

counts  :
	@ echo "lines, words, characters of code for this package:"
	@ echo "=================================================="
	@ wc $(SOURCE_FILES)

#---------------------------------------------------------------#

all		: perl conf

perl		: perl_packages perl_modules

perl_packages	:
		cd packages; make

perl_modules	:
		cd modules; make

# This target is used only for development, to identify any dependencies on perl modules
# that need to be suppressed via the __find_requires processing in the specfile.  Run this
# after a "make rpms", so you have an RPM file to examine.
#
# This analysis allows dependencies on the system perl to be ignored, as they will be
# resolved at RPM install time against the system "perl" package even though they will
# instead be resolved against the GroundWork-supplied Perl distribution at run time.
perl_deps	:
		@ # This RPM-name construction wildcards the distribution and platform information, as we assume
		@ # there will only be one copy of the RPM file of that build number in that directory.
		@ rpm -q -p --provides rpmbuild/RPMS/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)-$(SUBVERSION_BUILD).*.rpm |	\
		    sed -e 's/ [<>]*=.*//' -e 's/  *$$//' | sort -u > /tmp/$(RPM_PACKAGE_NAME).provides
		@ rpm -q -p --requires rpmbuild/RPMS/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)-$(SUBVERSION_BUILD).*.rpm |	\
		    sed -e 's/ [<>]*=.*//' -e 's/  *$$//' | sort -u > /tmp/$(RPM_PACKAGE_NAME).requires
		@ rpm -q    --provides perl                                                                               |	\
		    sed -e 's/ [<>]*=.*//' -e 's/  *$$//' | sort -u > /tmp/system-perl.provides
		@ comm -1 -3 /tmp/$(RPM_PACKAGE_NAME).provides /tmp/$(RPM_PACKAGE_NAME).requires > /tmp/$(RPM_PACKAGE_NAME).unresolved
		@ # Stupidly, fgrep exits with a non-zero error code if it finds nothing.  We have to override that with "|| true".
		@ deps=`comm -1 -3 /tmp/system-perl.provides /tmp/$(RPM_PACKAGE_NAME).unresolved | fgrep perl || true`;	\
		if [ -n "$$deps" ]; then										\
		    echo "The following perl dependencies are not currently being suppressed, but ought to be.";	\
		    echo "$$deps";											\
		else													\
		    echo "No unresolved perl dependencies were found.";							\
		fi

# For some idiotic reason, "make rpms" executed here will turn the desired
# 600 permissions on the $(RESTRICTED_FILES) files to 644.  How this can
# happen is mysterious and stupid, as making an RPM should not touch any
# code or files other than perhaps what is in the build root.  Eventually
# we will track down the cause and deal with it.  In the meantime, we
# provide this next make target to clean up.
# FIX LATER:  track down why that happens; fix it; and submit a bug report
# to the rpmbuild developers
conf		: $(RESTRICTED_FILES)
		chmod 600 $(RESTRICTED_FILES)

build_info	:
		scripts/make_build_info $(RPM_PACKAGE_RELEASE) $(SUBVERSION_BUILD) > info/build_info

#---------------------------------------------------------------#

# Note:  The INSTALL_ROOT path must match the definition of %_installroot in the rpmsetup/$(RPM_PACKAGE_NAME).rpmmacros file.
# There doesn't seem to be any good way to automate this correspondence, so we just define it blindly here.
INSTALL_ROOT = $(PWD)/rpmbuild/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)/INSTALL_ROOT

GWPATH = /usr/local/groundwork
SERVICENOWPATH = $(GWPATH)/servicenow

# See the notes at the start of this makefile for how to build the filelists.

# When we were trying to compile the Perl modules into pathnames that would allow them to be overlaid
# onto the GroundWork Perl installation, we needed to filter out certain directories and files to ensure
# that we didn't collide with those paths already in the base product.  For the time being, though, we
# will instead be installing those modules separately, because we haven't yet figure out how to build
# them successfully in that kind of setup.
#		scripts/make_filelist nagios nagios '%{gwpath}' perl/* | scripts/filter_perl_modules \
#

perl_filelist	:
		scripts/make_filelist nagios nagios '%{servicenowpath}' perl/* \
		    > $(PWD)/rpmsetup/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)-$(HARDWARE_PLATFORM).perl_filelist

servicenow_integration_install	: perl_modules build_info
	@:
	mkdir -p                     $(INSTALL_ROOT)$(SERVICENOWPATH)/bin
	cp -p bin/oneway_helpdesk.pl $(INSTALL_ROOT)$(SERVICENOWPATH)/bin
	cp -p bin/twoway_helpdesk.pl $(INSTALL_ROOT)$(SERVICENOWPATH)/bin
	@:
	mkdir -p                                                            $(INSTALL_ROOT)$(SERVICENOWPATH)/config
	cp -p config/authorized_users.conf                                  $(INSTALL_ROOT)$(SERVICENOWPATH)/config
	cp -p config/console-admin-config.xml                               $(INSTALL_ROOT)$(SERVICENOWPATH)/config
	cp -p config/hostgroup_and_service_to_assignment_group_mapping.conf $(INSTALL_ROOT)$(SERVICENOWPATH)/config
	cp -p config/hostgroup_to_category_and_subcategory_mapping.conf     $(INSTALL_ROOT)$(SERVICENOWPATH)/config
	cp -p config/monarch_group_to_location_mapping.conf                 $(INSTALL_ROOT)$(SERVICENOWPATH)/config
	cp -p config/oneway_helpdesk.conf                                   $(INSTALL_ROOT)$(SERVICENOWPATH)/config
	cp -p config/servicegroup_to_assignment_group_mapping.conf          $(INSTALL_ROOT)$(SERVICENOWPATH)/config
	cp -p config/servicenow_module.conf                                 $(INSTALL_ROOT)$(SERVICENOWPATH)/config
	cp -p config/twoway_helpdesk.conf                                   $(INSTALL_ROOT)$(SERVICENOWPATH)/config
	@:
	mkdir -p                                     $(INSTALL_ROOT)$(SERVICENOWPATH)/db
	cp -p db/delete_helpdesk_actions.sql         $(INSTALL_ROOT)$(SERVICENOWPATH)/db
	cp -p db/helpdesk_actions_seed_file.sql      $(INSTALL_ROOT)$(SERVICENOWPATH)/db
	cp -p db/helpdesk_dynamic_property_types.sql $(INSTALL_ROOT)$(SERVICENOWPATH)/db
	cp -p db/helpdesk_initialize_bridge_db.sql   $(INSTALL_ROOT)$(SERVICENOWPATH)/db
	cp -p db/prepare_databases_for_helpdesk      $(INSTALL_ROOT)$(SERVICENOWPATH)/db
	cp -p db/scrub_databases_for_helpdesk        $(INSTALL_ROOT)$(SERVICENOWPATH)/db
	@:
	mkdir -p              $(INSTALL_ROOT)$(SERVICENOWPATH)/info
	cp -p info/build_info $(INSTALL_ROOT)$(SERVICENOWPATH)/info
	@:
	mkdir -p                                        $(INSTALL_ROOT)$(SERVICENOWPATH)/doc/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)
	cp -p doc/SERVICENOW_INTEGRATION_INSTALL_NOTES   $(INSTALL_ROOT)$(SERVICENOWPATH)/doc/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)
	# cp -p doc/SERVICENOW_INTEGRATION_RELEASE_NOTES $(INSTALL_ROOT)$(SERVICENOWPATH)/doc/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)
	# cp -p doc/$(RPM_PACKAGE_NAME).0.0.pdf         $(INSTALL_ROOT)$(SERVICENOWPATH)/doc/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)
	# @:
	# These lines were for installing our Perl modules overlaying the GroundWork Perl installation.
	#     mkdir -p      $(INSTALL_ROOT)$(GWPATH)/perl
	#     cp -pr perl/* $(INSTALL_ROOT)$(GWPATH)/perl
	#     # Clean up the contents of the .packlist files so they reflect where the packages will be installed by the RPM.
	#     find $(INSTALL_ROOT)$(GWPATH)/perl -name .packlist -print | xargs perl -pi -e 's{.*/[.][.]/}{/usr/local/groundwork/}'
	# But until we can figure out how to build for the GroundWork paths, we need to supply a separate Perl tree instead.
	@:
	mkdir -p      $(INSTALL_ROOT)$(SERVICENOWPATH)/perl
	cp -pr perl/* $(INSTALL_ROOT)$(SERVICENOWPATH)/perl
	@:
	mkdir -p $(INSTALL_ROOT)$(SERVICENOWPATH)/logs

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

rpms            : servicenow_integration_install
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

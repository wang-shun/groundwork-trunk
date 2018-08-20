# makefile for GroundWork Ticketing Integration

# Copyright 2013-2017 GroundWork Open Source, Inc. ("GroundWork").  All
# rights reserved.  Use is subject to GroundWork commercial license terms.

#---------------------------------------------------------------#

# This makefile is set up for building an RPM on CentOS7, with the version
# of rpmbuild that is present in that release.  See extensive notes below
# on that construction.  One side effect of building on CentOS7 is that the
# patch we applied in CentOS5 for /usr/lib/rpm/perl.req that is described in
# ../servicenow/patches/README no longer needs to be applied to your system.

#---------------------------------------------------------------#

# How to build the Ticketing Integration from scratch, immediately after checkout:
#
# (1) Select the desired TICKETING_PROVIDER (below, in this makefile).
#
# (2) Build the software from scratch:
#
#     make distclean perl
#     make clean perl_filelist
#     make clean rpms
#
# This will yield an RPM in the rpmbuild/RPMS/ subdirectory.
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
# within them.)  So, you must execute the following commands.  These must be
# run separately so the dependency of distclean on clean does not cause clean
# to be skipped after the perl target is built.
#
#     make distclean perl
#     make clean perl_filelist
#
# Once you have pristine filelists built (in the rpmsetup/ subdirectory), they
# should be checked in and used thereafter for future builds, without needing
# to re-create them.

#---------------------------------------------------------------#

# Select here the particular ticketing provider for which you wish to build an
# RPM package.  As of this writing, your choices are ServiceNow, JIRA, and OTRS.
TICKETING_PROVIDER = JIRA
LOWERCASE_TICKETING_PROVIDER := $(shell echo $(TICKETING_PROVIDER) | tr A-Z a-z)
UPPERCASE_TICKETING_PROVIDER := $(shell echo $(TICKETING_PROVIDER) | tr a-z A-Z)

#---------------------------------------------------------------#

# These values should be constant throughout the lifespan of this RPM package.
RPM_PACKAGE_NAME = groundwork-$(LOWERCASE_TICKETING_PROVIDER)-integration
RPM_GENERIC_PACKAGE_NAME = groundwork-ticketing-integration

# This value must be updated for each new release, and the value must match the ${version}
# defined in the rpmsetup/$(RPM_VERSIONED_GENERIC_PACKAGE).spec file, and also in whatever
# must be updated in the doc/$(UPPERCASE_TICKETING_PROVIDER)_INTEGRATION_INSTALL_NOTES file.
RPM_PACKAGE_RELEASE = 3.0.0

# Convenience macros.
RPM_VERSIONED_PACKAGE = $(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)
RPM_VERSIONED_GENERIC_PACKAGE = $(RPM_GENERIC_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)

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
RESTRICTED_FILES =		\
	config/*_module.conf

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
		cd packages; make LOWERCASE_TICKETING_PROVIDER=$(LOWERCASE_TICKETING_PROVIDER)

perl_modules	:
		cd modules; make TICKETING_PROVIDER=$(TICKETING_PROVIDER)

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
		@ rpm -q -p --provides rpmbuild/RPMS/$(RPM_VERSIONED_PACKAGE)-$(SUBVERSION_BUILD).*.rpm |	\
		    sed -e 's/ [<>]*=.*//' -e 's/  *$$//' | sort -u > /tmp/$(RPM_PACKAGE_NAME).provides
		@ rpm -q -p --requires rpmbuild/RPMS/$(RPM_VERSIONED_PACKAGE)-$(SUBVERSION_BUILD).*.rpm |	\
		    sed -e 's/ [<>]*=.*//' -e 's/  *$$//' | sort -u > /tmp/$(RPM_PACKAGE_NAME).requires
		@ rpm -q    --provides perl                                                             |	\
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

# For some idiotic reason, "make rpms" executed here will turn the desired 600 permissions
# on the $(RESTRICTED_FILES) files to 644.  That even happens for the $() that are not
# referenced when building a particular RPM!  How this can happen is mysterious and
# stupid, as making an RPM should not touch any code or files other than perhaps what is
# in the build root.  Eventually we will track down the cause and deal with it.  In the
# meantime, we provide this next make target to clean up.
#
# FIX LATER:  track down why that happens; fix it; and submit a bug report to the rpmbuild
# developers (http://rpm.org/community.html).
#
conf		: $(RESTRICTED_FILES)
		chmod 600 $(RESTRICTED_FILES)

info		:
		mkdir -p info

build_info	: info
		scripts/make_build_info $(RPM_PACKAGE_RELEASE) $(SUBVERSION_BUILD) > info/build_info

#---------------------------------------------------------------#

# Note:  The INSTALL_ROOT path must match the definition of %_installroot in the rpmsetup/$(RPM_PACKAGE_NAME).rpmmacros file.
# There doesn't seem to be any good way to automate this correspondence, so we just define it blindly here.
INSTALL_ROOT = $(PWD)/rpmbuild/$(RPM_VERSIONED_PACKAGE)/INSTALL_ROOT

GWPATH = /usr/local/groundwork
TICKETINGPATH = $(GWPATH)/ticketing
LOGROTATEPATH = /etc/logrotate.d

# See the notes at the start of this makefile for how to build the filelists.

# When we were trying to compile the Perl modules into pathnames that would allow them to be overlaid
# onto the GroundWork Perl installation, we needed to filter out certain directories and files to ensure
# that we didn't collide with those paths already in the base product.  For the time being, though, we
# will instead be installing those modules separately, because we haven't yet figure out how to build
# them successfully in that kind of setup.
#		scripts/make_filelist nagios nagios '%{gwpath}' perl/* | scripts/filter_perl_modules \
#

# FIX MINOR:  This seems to be working just fine, but we're presently building a noarch RPM.
# Do we want a noarch perl_filelist instead?
#
perl_filelist	:
		scripts/make_filelist nagios nagios '%{ticketingpath}' perl/* \
		    > $(PWD)/rpmsetup/$(RPM_VERSIONED_PACKAGE)-$(HARDWARE_PLATFORM).perl_filelist

# FIX MAJOR:  To customize the build for a particular ticketing provider, we will need to:
#
# (*) Adjust the ResolvedStates value in the config/twoway_helpdesk.conf config file to
#     automatically depend on the ticketing provider
#
ticketing_integration_install	: perl_modules build_info
	@:
	mkdir -p                     $(INSTALL_ROOT)$(TICKETINGPATH)/bin
	cp -p bin/oneway_helpdesk.pl $(INSTALL_ROOT)$(TICKETINGPATH)/bin
	cp -p bin/twoway_helpdesk.pl $(INSTALL_ROOT)$(TICKETINGPATH)/bin
	@:
	mkdir -p                          $(INSTALL_ROOT)$(LOGROTATEPATH)
	cp -p config/groundwork-ticketing $(INSTALL_ROOT)$(LOGROTATEPATH)
	@:
	mkdir -p                           $(INSTALL_ROOT)$(TICKETINGPATH)/config
	cp -p config/authorized_users.conf $(INSTALL_ROOT)$(TICKETINGPATH)/config
	if [ "$(LOWERCASE_TICKETING_PROVIDER)" = "servicenow" ]; then							\
	    cp -p config/console-admin-config.xml                               $(INSTALL_ROOT)$(TICKETINGPATH)/config;	\
	    cp -p config/hostgroup_and_service_to_assignment_group_mapping.conf $(INSTALL_ROOT)$(TICKETINGPATH)/config;	\
	    cp -p config/hostgroup_to_category_and_subcategory_mapping.conf     $(INSTALL_ROOT)$(TICKETINGPATH)/config;	\
	    cp -p config/servicegroup_to_assignment_group_mapping.conf          $(INSTALL_ROOT)$(TICKETINGPATH)/config;	\
	    sed -e 's/LOWERCASE_TICKETING_PROVIDER/$(LOWERCASE_TICKETING_PROVIDER)/g'					\
		config/monarch_group_to_location_mapping.conf								\
		> $(INSTALL_ROOT)$(TICKETINGPATH)/config/monarch_group_to_location_mapping.conf;			\
	fi
	cp -p config/$(LOWERCASE_TICKETING_PROVIDER)_module.conf            $(INSTALL_ROOT)$(TICKETINGPATH)/config
	@:
	sed -e 's/LOWERCASE_TICKETING_PROVIDER/$(LOWERCASE_TICKETING_PROVIDER)/g'	\
	    -e 's/TICKETING_PROVIDER/$(TICKETING_PROVIDER)/g'				\
	    config/oneway_helpdesk.conf							\
	    > $(INSTALL_ROOT)$(TICKETINGPATH)/config/oneway_helpdesk.conf
	sed -e 's/LOWERCASE_TICKETING_PROVIDER/$(LOWERCASE_TICKETING_PROVIDER)/g'	\
	    -e 's/TICKETING_PROVIDER/$(TICKETING_PROVIDER)/g'				\
	    config/twoway_helpdesk.conf							\
	    | perl -ln scripts/conditional_filter					\
	    > $(INSTALL_ROOT)$(TICKETINGPATH)/config/twoway_helpdesk.conf
	@:
	mkdir -p                                     $(INSTALL_ROOT)$(TICKETINGPATH)/db
	sed -e 's/LOWERCASE_TICKETING_PROVIDER/$(LOWERCASE_TICKETING_PROVIDER)/g'	\
	    -e 's/TICKETING_PROVIDER/$(TICKETING_PROVIDER)/g'				\
	    db/delete_helpdesk_actions.sql						\
	    | perl -ln scripts/conditional_filter					\
	    > $(INSTALL_ROOT)$(TICKETINGPATH)/db/delete_helpdesk_actions.sql
	sed -e 's/LOWERCASE_TICKETING_PROVIDER/$(LOWERCASE_TICKETING_PROVIDER)/g'	\
	    -e 's/TICKETING_PROVIDER/$(TICKETING_PROVIDER)/g'				\
	    db/helpdesk_actions_seed_file.sql						\
	    | perl -ln scripts/conditional_filter					\
	    > $(INSTALL_ROOT)$(TICKETINGPATH)/db/helpdesk_actions_seed_file.sql
	cp -p db/helpdesk_dynamic_property_types.sql $(INSTALL_ROOT)$(TICKETINGPATH)/db
	cp -p db/helpdesk_initialize_bridge_db.sql   $(INSTALL_ROOT)$(TICKETINGPATH)/db
	cp -p db/prepare_databases_for_helpdesk      $(INSTALL_ROOT)$(TICKETINGPATH)/db
	cp -p db/scrub_databases_for_helpdesk        $(INSTALL_ROOT)$(TICKETINGPATH)/db
	@:
	mkdir -p              $(INSTALL_ROOT)$(TICKETINGPATH)/info
	cp -p info/build_info $(INSTALL_ROOT)$(TICKETINGPATH)/info
	@:
	mkdir -p                                                            $(INSTALL_ROOT)$(TICKETINGPATH)/doc/$(RPM_VERSIONED_PACKAGE)
	cp -p doc/$(UPPERCASE_TICKETING_PROVIDER)_INTEGRATION_INSTALL_NOTES $(INSTALL_ROOT)$(TICKETINGPATH)/doc/$(RPM_VERSIONED_PACKAGE)
	cp -p doc/$(UPPERCASE_TICKETING_PROVIDER)_INTEGRATION_RELEASE_NOTES $(INSTALL_ROOT)$(TICKETINGPATH)/doc/$(RPM_VERSIONED_PACKAGE)
	# cp -p doc/$(RPM_PACKAGE_NAME).0.0.pdf                             $(INSTALL_ROOT)$(TICKETINGPATH)/doc/$(RPM_VERSIONED_PACKAGE)
	# @:
	# These lines were for installing our Perl modules overlaying the GroundWork Perl installation.
	#     mkdir -p      $(INSTALL_ROOT)$(GWPATH)/perl
	#     cp -pr perl/* $(INSTALL_ROOT)$(GWPATH)/perl
	#     # Clean up the contents of the .packlist files so they reflect where the packages will be installed by the RPM.
	#     find $(INSTALL_ROOT)$(GWPATH)/perl -name .packlist -print | xargs perl -pi -e 's{.*/[.][.]/}{/usr/local/groundwork/}'
	# But until we can figure out how to build for the GroundWork paths, we need to supply a separate Perl tree instead.
	@:
	mkdir -p      $(INSTALL_ROOT)$(TICKETINGPATH)/perl
	cp -pr perl/* $(INSTALL_ROOT)$(TICKETINGPATH)/perl
	@:
	mkdir -p $(INSTALL_ROOT)$(TICKETINGPATH)/logs

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
# Also note that, for the time being, we're emphatically not trying to build from a source RPM.  We have Subversion
# for that, and we already have a checked-out copy of the source code.  Maybe at some point in the future we might
# consider building a source-RPM distribution, but certainly not until we've got all the kinks worked out from the
# process for building binary RPMs from the source code.
#
# Also note that the rpmbuild(8) man page is apparently poorly maintained, and does not reflect all the command-line
# options available, such as the --macros option.  Try "rpmbuild --help" for more detail.  Even that usage message
# does not list all the available options, as we have discovered by looking at the source code.
#
# Also note that in CentOS5, we were not able to get the "rpmbuild --macros" option to work, even though it was listed
# in the help output.  So we set this value instead as "macrofiles:" in the rcfile.  In CentOS7, the situation is now
# reversed:  the "rpmbuild --macros" option works, and the "macrofiles:" directive in the rcfile simply does not work.
# (Or maybe the rcfile itself is getting skipped now even when it is specified in the --rcfile option pathlist; we
# haven't found a good way to test for that possibility.)
#
# If we had need of supporting other platforms, we would need to generalize the "redhat" platform component in the
# "/usr/lib/rpm/redhat/rpmrc" pathname below.

# Notes on the porting of the rpmbuild command below to work with the updated rpmbuild release in CentOS7:
#
# (*) For some unknown reason, the rpmbuild --buildroot option is needed now under CentOS7.  This setting is no longer
#     simply drawn from the specfile as it used to be in CentOS5.
#
# (*) We had to change the value of the rpmbuild --dbpath option from what we used before, to avoid leaving a lot of debris
#     in the rpmsetup/ directory after a build.  Now all the extra stuff gets its own subdirectory, that we clean up along
#     with all the other build artifacts.
#
# (*) The rpmbuild --short-circuit option no longer works with the updated rpmbuild and our specfile.  Applying that option
#     fails to run the %setup section commands.  So we drop that option now.
#
# (*) The _topdir macro is defined in /usr/lib/rpm/macros file as %{getenv:HOME}/rpmbuild which definition will be in play if
#     we do not override it.  But using that system definition would break the locality and distinctness of our build artifacts.
#     If we do see the system definition in play, it means that our rpmbuild construction here has failed its intent.  To block
#     such a failure from silently doing something we don't want, we create ~/rpmbuild outside of this makefile as a regular
#     file instead of as a directory.  That will cause the build to fail if somehow the system definition of the _topdir macro
#     comes back into play, flagging the error immediately so we can get it corrected in our makefile and *.rpmmacros files.
#
# (*) We have to go to a lot of trouble to see that the _topdir macro is not defined as ~/rpmbuild instead of ./rpmbuild
#     as we need it to be.  That is to say, our macros are not invoked if we simply mention our foo.rpmmacros file in the
#     rpmbuild --rcfile pathlist as we used to do.  Either the entire macros file is being ignored, or the macrofiles:
#     directive within it is ignored; we haven't devised a test to distinguish those two possibilities.  What we got working
#     instead was to mention our macros file directly on the command line using the rpmbuild --macros option, and simplifying
#     the rpmbuild --rcfile option so it no longer refers to our foo.rpmrc file.
#
# (*) There is one problem with the rpmbuild --macros option.  It doesn't work (it's still accepted, but somehow silently
#     becomes inoperative) if we also specify an rpmbuild --define option (see below).  Fortunately, it does still work just
#     fine if we use the undocumented (even in the rpmbuild --help message) rpmbuild --predefine option.
#
# (*) Perhaps because we had to use the rpmbuild --macros option, the macros we used to use in our *.rpmmacros file to define
#     the _rpmtopdir macro as rpmbuild/%{name}-%{version}/ were not being expanded before that directory is accessed in our build.
#     Now, the value of _rpmtopdir is being used at least once before the name and version macros are defined from the specfile
#     (although some other references to _rpmtopdir are used and expanded after those constituent macros are available).  There
#     are issues with trying to remove the _rpmtopdir reference from the make_all_rpm_build_dirs macro definition in an attempt
#     to cure this.  So we punt and rejigger our definition of _rpmtopdir to depend instead on an rpm_versioned_package macro
#     that we define here at the makefile level, no longer trying to directly use macros from the specfile.
#
# (*) We tried to define the rpm_versioned_package macro directly on the command line with the rpmbuild --define option.  But when
#     we do that, for some unknown reason the rpmbuild --macros option gets ignored (and thus the _topdir definition reverts from
#     ./rpmbuild as we want it back to the ~/rpmbuild value that is found in the system macros files).  That breaks the sanctity
#     of our builds (restriction of every last build artifact to a localized file tree).  Luckily, we looked in the rpmbuild source
#     code and found the undocumented rpmbuild --predefine option, which gets the definition established but does not have the same
#     conflict with the rpmbuild --macros option.  So we use that instead.
#
# FIX LATER:  Report the difficulties we documented above to the upstream rpmbuild maintainers.

# FIX LATER:  Does the rpmsetup/*.perl_filelist need to be made in a noarch flavor?  What about the reference to that list inside
# the specfile itself?  If the _arch macro is being expanded to x86_64 then why are we creating a noarch RPM?  With respect to
# the latter, it's because we have no actual binaries package within the RPM.  But then why are we using the _arch macro as part
# of the .perl_filelist filename instead of some other macro?  Perhaps because there is apparently no macro automatically defined
# with a "noarch" value, that might change to x86_64 automatically if we did have some binaries packaged within the RPM.

# We must re-analyze and fix the "rpm -q -p --requires foo.rpm" and "rpm -q -p --provides foo.rpm" stuff before final
# release, to ensure that the final RPM does not depend on any packages not already supplied by the base GWMEE release.
# See the perl_deps target above for an automated analysis.
#
rpms            : ticketing_integration_install
		rpmbuild															\
		    --predefine "rpm_versioned_package $(RPM_VERSIONED_PACKAGE)"								\
		    --predefine "lowercase_ticketing_provider $(LOWERCASE_TICKETING_PROVIDER)"							\
		    --predefine "uppercase_ticketing_provider $(UPPERCASE_TICKETING_PROVIDER)"							\
		    --rcfile /usr/lib/rpm/rpmrc:/usr/lib/rpm/redhat/rpmrc									\
		    --macros '/usr/lib/rpm/macros:/usr/lib/rpm/platform/%{_target}/macros:rpmsetup/$(RPM_GENERIC_PACKAGE_NAME).rpmmacros'	\
		    --dbpath $(PWD)/dbpath													\
		    --buildroot $(INSTALL_ROOT)													\
		    --quiet															\
		    -bb rpmsetup/$(RPM_VERSIONED_GENERIC_PACKAGE).spec 2>&1
		# rpmbuild inappropriately mangles the permissions of files to make them world-readable.
		# We need to track down why that is happening, and report it as a bug, since it should not
		# be touching anything outside of the build root.  In any case, we need to put back the
		# permissions into their proper condition for local testing.
		make conf

rpmclean        :
		rm -rf rpmbuild
		rm -rf dbpath
		rm -rf info

#---------------------------------------------------------------#

# This target makes a full release from scratch, all the way from complete cleanup of any debris left behind
# by previous builds, to recompiling the world and doing the final packaging.  We establish this target to
# ensure we have a repeatable way to guarantee that the final packaged bits really do represent the version
# of the checked-in code that it's supposed to.

# Note:  For a formal external release, before building this target you must:
# * update the RPM_PACKAGE_RELEASE value above
# * make sure the rpmsetup/$(RPM_VERSIONED_PACKAGE).spec file has its version-component values correctly set
# * check in the revised copies of this makefile and the specfile
# * make sure all the checked-out code is up-to-date (run "svn status -u" to check this)

release         : rpmsetup/$(RPM_VERSIONED_GENERIC_PACKAGE).spec
		make buildclean
		make rpms

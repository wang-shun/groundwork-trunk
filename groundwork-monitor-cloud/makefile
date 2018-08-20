# makefile for GroundWork Cloud Connector

# Copyright 2010-2011 GroundWork Open Source, Inc. ("GroundWork").  All
# rights reserved.  Use is subject to GroundWork commercial license terms.

#---------------------------------------------------------------#

# Special note on building the Cloud Connector:
#
# To build this RPM, you must first have the sibling ../monitor-portal/
# directory checked out from Subversion (in the same relative location),
# and you must have maven (and probably apache-ant as well) installed on
# your build machine.  That is because the portlet/portal pieces of the
# Cloud Connector will be built over under the sibling directory, not in
# the portal/ subdirectory here (that code now being obsolete).

#---------------------------------------------------------------#

# How to build the Cloud Connector from scratch, immediately after checkout:
#
#     make perl clean rpms
#
# will yield an RPM in the rpmbuild/RPMS/ directory.
#
# Once you have executed "make perl clean", you will be left with a clean
# set of perl/*/ files to use for additional runs of "make rpms".  Either
# don't mess with this, or "make distclean" to start again from scratch.

#---------------------------------------------------------------#

# How to rebuild the filelists from scratch:
#
# This should be done only rarely, namely under one of these conditions:
# * you need to add a new Perl package to the build
# * you need to delete some Perl package from the build
# * you need to update some Perl package in the build
# * you need to update the EC2 tools version in the build
#
# We want to make sure we respect the read-only nature of the constructed Perl
# files.  To that end, you should only "make perl_filelist" immediately after
# having executed "make distclean".  (Rather stupidly, the "rpm" command messes
# with the permissions on the source files, so we need to start with a clean
# setup before constructing the filelists, which have file permissions embedded
# within them.)  So:
#
#     make distclean perl clean perl_filelist
#
# Similarly, we only "make ec2_filelist" under carefully controlled conditions,
# namely after the perl_filelist has already been built, then:
#
#     make clean rpms
#     make ec2_filelist
#
# The "make rpms" will fail, but it will create all the files in the proper
# places for the ec2_filelist to be prepared.
#
# Once you have pristine filelists built (in the rpmsetup/ subdirectory), they
# should be checked in and used thereafter for future builds, without needing
# to re-create them.

#---------------------------------------------------------------#

# This value should be constant throughout the lifespan of this RPM package.
RPM_PACKAGE_NAME = groundwork-cloud-connector

# This value must be updated for each new release, and the value must match the
# ${version} defined in the rpmsetup/$(RPM_PACKAGE_NAME)-%{version}.spec file,
# as well as portal/portal-cloud-connector/project.xml (may no longer be used,
# in preference to the PORTAL_APPLICATIONS versions of the portal stuff, which
# is automatically maintained in the scripting below) and also in whatever must
# be updated in the doc/CLOUD_INSTALL_NOTES file.
RPM_PACKAGE_RELEASE = 2.1.0

# Let's label the tarballs with the build number, just like the RPMs.
SUBVERSION_BUILD := $(shell svn info -r HEAD | fgrep Revision | gawk '{print $$2}')

# We expect the HARDWARE_PLATFORM to be one of: 
#     i386      (32-bit platform)
#     x86_64    (64-bit platform)
# to match up with the short string that Perl uses when it compiles modules.
HARDWARE_PLATFORM := $(shell uname -i)

# Where to find the related code that manages the environment in which the
# Cloud Connector is displayed to the user.  It is now relocated to a remote
# directory rather than just being in a subdirectory, primarily to conveniently
# satisfy certain dependencies.
PORTAL_APPLICATIONS = ../monitor-portal/applications

# We would like to use the newer 1.3-57419 release, but so far it has proven
# problematic in testing, at least against Eucalyptus 1.6.2 servers.
# EC2_API_TOOLS_RELEASE = 1.3-57419
EC2_API_TOOLS_RELEASE = 1.3-46266

#---------------------------------------------------------------#

PORTAL_DIR = $(PORTAL_APPLICATIONS)/portal-cloud-connector
WEBAPP_DIR = $(PORTAL_APPLICATIONS)/cloud-connector

PORTAL_PROJECT_XML = $(PORTAL_DIR)/project.xml
WEBAPP_PROJECT_XML = $(WEBAPP_DIR)/project.xml

#---------------------------------------------------------------#

# Making the default target depends on a previous "make perl".  But we don't make
# that a dependency because it takes too much time for normal development, when it
# is almost always unnecessary after the first time the Perl code has been built.
# (The only time you need to repeat that is after changing what goes into the Perl
# libraries in this RPM.)  See the notes at the top of the makefile for more detail
# on building from scratch.
default         : release

install         :

# The project.xml files are needed for "maven clean" to do its job,
# so possibly we need to create them just a bit before we remove them.
clean           : rpmclean $(PORTAL_PROJECT_XML) $(WEBAPP_PROJECT_XML)
		cd packages; make clean
		cd $(PORTAL_DIR); maven clean
		cd $(WEBAPP_DIR); maven clean
		# We remove these two project.xml files because they should not be part 
		# of the permanently archived portal code.  Rather, they should only be 
		# automatically maintained here from this makefile:  constructed below
		# with up-to-date content, and removed during a cleanup operation.
		rm -f $(PORTAL_PROJECT_XML)
		rm -f $(WEBAPP_PROJECT_XML)

distclean	: clean
		cd packages; make distclean

buildclean      : clean

#---------------------------------------------------------------#

all		: perl

perl		: perl_packages perl_modules

perl_packages	:
		cd packages; make

perl_modules	:
		cd modules; make

build_info	:
		scripts/make_build_info $(RPM_PACKAGE_RELEASE) $(SUBVERSION_BUILD) > info/build_info

#---------------------------------------------------------------#

# Note:  The INSTALL_ROOT path must match the definition of %_installroot in the rpmsetup/$(RPM_PACKAGE_NAME).rpmmacros file.
# There doesn't seem to be any good way to automate this correspondence, so we just define it blindly here.
INSTALL_ROOT = $(PWD)/rpmbuild/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)/INSTALL_ROOT

GWPATH = /usr/local/groundwork
CLOUDPATH = $(GWPATH)/cloud

# See the notes at the start of this makefile for how to build the filelists.

ec2_filelist	:
		cd $(INSTALL_ROOT)$(CLOUDPATH); \
		$(PWD)/scripts/make_filelist nagios nagios '%{cloudpath}' ec2-api-tools-$(EC2_API_TOOLS_RELEASE) \
		    > $(PWD)/rpmsetup/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)-$(HARDWARE_PLATFORM).ec2_filelist

# When we were trying to compile the Perl modules into pathnames that would allow them to be overlaid
# onto the GroundWork Perl installation, we needed to filter out certain directories and files to ensure
# that we didn't collide with those paths already in the base product.  For the time being, though, we
# will instead be installing those modules separately, because we haven't yet figure out how to build
# them successfully in that kind of setup.
#		scripts/make_filelist nagios nagios '%{gwpath}' perl/* | scripts/filter_perl_modules \
#

perl_filelist	:
		scripts/make_filelist nagios nagios '%{cloudpath}' perl/* \
		    > $(PWD)/rpmsetup/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)-$(HARDWARE_PLATFORM).perl_filelist

$(PORTAL_PROJECT_XML)	:
	cd $(PORTAL_DIR); sed -e "s/{RELEASE}/$(RPM_PACKAGE_RELEASE)/g" project.xml.template > project.xml

$(WEBAPP_PROJECT_XML)	:
	cd $(WEBAPP_DIR); sed -e "s/{RELEASE}/$(RPM_PACKAGE_RELEASE)/g" project.xml.template > project.xml

PORTAL_WAR_FILE = $(PORTAL_DIR)/target/portal-cloud-connector.war
WEBAPP_WAR_FILE = $(WEBAPP_DIR)/target/cloud-connector.war

# In fact, there are more dependencies than just the ones listed here.
# But this will catch the updates needed when we edit the usual suspects.
# A "make clean" before building the RPM will force a complete rebuild
# of the .war files.

PORTAL_WAR_DEPENDENCIES = $(PORTAL_DIR)/src/main/webapp/WEB-INF/*
WEBAPP_WAR_DEPENDENCIES = $(WEBAPP_DIR)/src/main/WebContent/WEB-INF/cgi/*

$(PORTAL_WAR_FILE)	: $(PORTAL_PROJECT_XML) $(PORTAL_WAR_DEPENDENCIES)
	cd $(PORTAL_DIR); maven clean war

$(WEBAPP_WAR_FILE)	: $(WEBAPP_PROJECT_XML) $(WEBAPP_WAR_DEPENDENCIES)
	cd $(WEBAPP_DIR); maven clean war

war	: $(PORTAL_WAR_FILE) $(WEBAPP_WAR_FILE)
	@# Happily, in the normal case when the dependency is already built, this is all you'll see.
	@echo "not love?"

cloud_connector_install     : build_info war
	@:
	mkdir -p $(INSTALL_ROOT)$(CLOUDPATH)
	cd       $(INSTALL_ROOT)$(CLOUDPATH);					\
	unzip -o -a $(PWD)/ec2/ec2-api-tools-$(EC2_API_TOOLS_RELEASE).zip;	\
	rm ec2-api-tools-$(EC2_API_TOOLS_RELEASE)/bin/*.cmd
	@:
	mkdir -p                              $(INSTALL_ROOT)$(CLOUDPATH)/scripts
	cp -p scripts/convert_eucarc_for_perl $(INSTALL_ROOT)$(CLOUDPATH)/scripts
	cp -p scripts/convert_eucarc_for_tcsh $(INSTALL_ROOT)$(CLOUDPATH)/scripts
	cp -p scripts/create_cloud_objects    $(INSTALL_ROOT)$(CLOUDPATH)/scripts
	cp -p scripts/setenv-cloud            $(INSTALL_ROOT)$(CLOUDPATH)/scripts
	sed -e 's/{EC2_API_TOOLS_RELEASE}/$(EC2_API_TOOLS_RELEASE)/g' scripts/cloud_config.pl   > $(INSTALL_ROOT)$(CLOUDPATH)/scripts/cloud_config.pl
	sed -e 's/{EC2_API_TOOLS_RELEASE}/$(EC2_API_TOOLS_RELEASE)/g' scripts/setenv-cloud.bash > $(INSTALL_ROOT)$(CLOUDPATH)/scripts/setenv-cloud.bash
	sed -e 's/{EC2_API_TOOLS_RELEASE}/$(EC2_API_TOOLS_RELEASE)/g' scripts/setenv-cloud.tcsh > $(INSTALL_ROOT)$(CLOUDPATH)/scripts/setenv-cloud.tcsh
	@:
	mkdir -p                                            $(INSTALL_ROOT)$(GWPATH)/nagios/libexec
	cp -p scripts/check_ec2_availability_zone.pl        $(INSTALL_ROOT)$(GWPATH)/nagios/libexec
	cp -p scripts/check_eucalyptus_availability_zone.pl $(INSTALL_ROOT)$(GWPATH)/nagios/libexec
	@:
	# mkdir -p    $(INSTALL_ROOT)$(CLOUDPATH)/bin
	# cp -p bin/* $(INSTALL_ROOT)$(CLOUDPATH)/bin
	@:
	mkdir -p                          $(INSTALL_ROOT)$(CLOUDPATH)/config
	cp -p config/cloud_connector.conf $(INSTALL_ROOT)$(CLOUDPATH)/config
	@:
	mkdir -p $(INSTALL_ROOT)$(CLOUDPATH)/credentials
	@:
	mkdir -p              $(INSTALL_ROOT)$(CLOUDPATH)/info
	cp -p info/build_info $(INSTALL_ROOT)$(CLOUDPATH)/info
	@:
	mkdir -p         $(INSTALL_ROOT)$(GWPATH)/core/profiles
	cp -p profiles/* $(INSTALL_ROOT)$(GWPATH)/core/profiles
	@:
	mkdir -p                              $(INSTALL_ROOT)$(CLOUDPATH)/doc/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)
	cp -p doc/CLOUD_INSTALL_NOTES         $(INSTALL_ROOT)$(CLOUDPATH)/doc/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)
	# cp -p doc/CLOUD_RELEASE_NOTES         $(INSTALL_ROOT)$(CLOUDPATH)/doc/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)
	# cp -p doc/$(RPM_PACKAGE_NAME).0.0.pdf $(INSTALL_ROOT)$(CLOUDPATH)/doc/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)
	# @:
	# These lines were for installing our Perl modules overlaying the GroundWork Perl installation.
	#     mkdir -p      $(INSTALL_ROOT)$(GWPATH)/perl
	#     cp -pr perl/* $(INSTALL_ROOT)$(GWPATH)/perl
	#     # Clean up the contents of the .packlist files so they reflect where the packages will be installed by the RPM.
	#     find $(INSTALL_ROOT)$(GWPATH)/perl -name .packlist -print | xargs perl -pi -e 's{.*/[.][.]/}{/usr/local/groundwork/}'
	# But until we can figure out how to build for the GroundWork paths, we need to supply a separate Perl tree instead.
	@:
	mkdir -p      $(INSTALL_ROOT)$(CLOUDPATH)/perl
	cp -pr perl/* $(INSTALL_ROOT)$(CLOUDPATH)/perl
	@:
	mkdir -p $(INSTALL_ROOT)$(CLOUDPATH)/logs
	mkdir -p $(INSTALL_ROOT)$(CLOUDPATH)/var
	@:
	mkdir -p                         $(INSTALL_ROOT)$(GWPATH)/core/monarch/cgi-bin/monarch
	cp -p scripts/monarch_clouds.cgi $(INSTALL_ROOT)$(GWPATH)/core/monarch/cgi-bin/monarch
	@:
	mkdir -p                       $(INSTALL_ROOT)$(GWPATH)/core/monarch/lib
	cp -p scripts/MonarchClouds.pm $(INSTALL_ROOT)$(GWPATH)/core/monarch/lib
	@:
	mkdir -p                 $(INSTALL_ROOT)$(GWPATH)/foundation/container/webapps
	cp -p $(WEBAPP_WAR_FILE) $(INSTALL_ROOT)$(GWPATH)/foundation/container/webapps
	@:
	mkdir -p                 $(INSTALL_ROOT)$(GWPATH)/foundation/container/webapps/jboss/jboss-portal.sar
	cp -p $(PORTAL_WAR_FILE) $(INSTALL_ROOT)$(GWPATH)/foundation/container/webapps/jboss/jboss-portal.sar

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

rpms            : cloud_connector_install
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

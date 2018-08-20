# makefile for the GroundWork Ganglia Integration Module

# Copyright 2006-2017 GroundWork Open Source, Inc. ("GroundWork").  All
# rights reserved.  Use is subject to GroundWork commercial license terms.

#---------------------------------------------------------------#

# Special note on building the Ganglia Integration Module:
#
# To build this RPM, you must first have the sibling ../monitor-portal/
# directory checked out from Subversion (in the same relative location),
# and you must have maven (and probably apache-ant as well) installed on
# your build machine.  That is because the portlet/portal pieces of the
# Ganglia Integration Module will be built over under the sibling directory.

#---------------------------------------------------------------#

# How to build the Ganglia Integration Module from scratch, immediately
# after checkout:
#
#     make clean
#     make rpms
#
# will yield an RPM in the rpmbuild/RPMS/ directory.

# FIX MINOR:  The "clean"-target dependencies on $(PORTAL_PROJECT_XML)
# and $(WEBAPP_PROJECT_XML) are currently set to ensure that both these
# and other portlet-related files get wiped out when we clean up, to
# prevent the transient copies of these files from being inadvertently
# checked into Subversion.  However, the single-pass dependency checking
# being done by "make" means that the "$(PORTAL_WAR_FILE)"-target and
# "$(WEBAPP_WAR_FILE)"-target dependencies on these same files are not being
# re-evaluated later on if we attempt to clean and build in the same run:
#
#     make clean rpms
#
# So for the time being, until we figure out how to get "make" to re-evaluate
# the dependencies and ensure that these files are present when needed, you
# need to perform the build in two separate steps, as described above.

#---------------------------------------------------------------#

# This value should be constant throughout the lifespan of this RPM package.
RPM_PACKAGE_NAME = groundwork-ganglia-integration
RPM_GENERIC_PACKAGE_NAME = groundwork-ganglia-integration

# This value must be updated for each new release, and the value must match the
# ${version} defined in the rpmsetup/$(RPM_VERSIONED_GENERIC_PACKAGE).spec file
# and the scripts/GangliaConfigAdmin.cgi and scripts/GangliaWebServers.cgi files.
# The Ganglia Integration System Administrator Guide in the doc/ directory must
# also be updated to reflect the new release level (as well as any user-visible
# changes in the software).  The corresponding version numbers in the portal
# stuff are automatically maintained in the PORTAL_APPLICATIONS scripting below.
RPM_PACKAGE_RELEASE = 7.0.0

# Convenience macros.
RPM_VERSIONED_PACKAGE = $(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)
RPM_VERSIONED_GENERIC_PACKAGE = $(RPM_GENERIC_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)

# We need the build number to stuff into the build_info file.
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

#---------------------------------------------------------------#

# Certain configuration files ought to have restricted permissions because
# they contain credentials for accessing system resources.
RESTRICTED_FILES =						\
	config/GangliaConfigAdmin.conf				\
	config/GangliaWebServers.conf				\
	config/check_ganglia.conf				\
	scripts/services/check-ganglia/supervise/lock		\
	scripts/services/check-ganglia/log/supervise/lock	\
	scripts/create-ganglia-db.sql

#---------------------------------------------------------------#

# These variables are set to enforce building a version of the portal .war files that are
# compatible with the Java 1.5 environment used in the GWMEE 6.5 and earlier MySQL-based
# GroundWork Monitor releases.  These files will still be compatible with the portal
# environment in the GWMEE 6.6.1 and later PostgreSQL-based GroundWork Monitor releases.
# We want this kind of backward compatibility so we can upgrade the Ganglia Integration
# Module on GWMEE 6.5 systems, primarily during an upgrade to a later release of GWMEE,
# and be able to demonstrate that this portion of the overall upgrade was successful.
#
# JAVA_HOME             = /usr/java/jdk1.5.0_22
# JAVAC                 = $(JAVA_HOME)/bin/javac
# REQUIRED_JAVA_VERSION = 1.5

# In trial for use with GWMEE 7.1.1.
JAVA_HOME             = /usr/java/jdk1.7.0_51
JAVAC                 = $(JAVA_HOME)/bin/javac
REQUIRED_JAVA_VERSION = 1.7

#---------------------------------------------------------------#

PORTAL_DIR = $(PORTAL_APPLICATIONS)/portal-ganglia-integration
WEBAPP_DIR = $(PORTAL_APPLICATIONS)/ganglia-integration

PORTAL_PROJECT_XML = $(PORTAL_DIR)/pom.xml
WEBAPP_PROJECT_XML = $(WEBAPP_DIR)/pom.xml

#---------------------------------------------------------------#

default		: release

install		:

# The pom.xml files are needed for "mvn clean" to do its job,
# so possibly we need to create them just a bit before we remove them.
oldclean	: rpmclean $(PORTAL_PROJECT_XML) $(WEBAPP_PROJECT_XML)
		cd $(PORTAL_DIR); mvn clean
		cd $(WEBAPP_DIR); mvn clean
		# We remove these two pom.xml files because they should not be part
		# of the permanently archived portal code.  Rather, they should only be
		# automatically maintained here from this makefile:  constructed below
		# with up-to-date content, and removed during a cleanup operation.
		rm -f $(PORTAL_PROJECT_XML)
		rm -f $(WEBAPP_PROJECT_XML)

clean		: rpmclean

distclean	: clean

buildclean	: clean

#---------------------------------------------------------------#

all		:

# This target is used only for development, to identify any dependencies on perl modules
# that need to be suppressed via the __find_requires processing in the specfile.  Run this
# after a "make rpms", so you have an RPM file to examine.
#
# This analysis allows dependencies on the system perl to be ignored, as they will be
# resolved at RPM install time against the system "perl" package even though they will
# instead be resolved against the GroundWork-supplied Perl distribution at run time.
perl_deps       :
		@ # This RPM-name construction wildcards the distribution and platform information, as we assume
		@ # there will only be one copy of the RPM file of that build number in that directory.
		@ rpm -q -p --provides rpmbuild/RPMS/$(RPM_VERSIONED_PACKAGE)-$(SUBVERSION_BUILD).*.rpm |       \
		    sed -e 's/ [<>]*=.*//' -e 's/  *$$//' | sort -u > /tmp/$(RPM_PACKAGE_NAME).provides
		@ rpm -q -p --requires rpmbuild/RPMS/$(RPM_VERSIONED_PACKAGE)-$(SUBVERSION_BUILD).*.rpm |       \
		    sed -e 's/ [<>]*=.*//' -e 's/  *$$//' | sort -u > /tmp/$(RPM_PACKAGE_NAME).requires
		@ rpm -q    --provides perl                                                             |       \
		    sed -e 's/ [<>]*=.*//' -e 's/  *$$//' | sort -u > /tmp/system-perl.provides
		@ comm -1 -3 /tmp/$(RPM_PACKAGE_NAME).provides /tmp/$(RPM_PACKAGE_NAME).requires > /tmp/$(RPM_PACKAGE_NAME).unresolved
		@ # Stupidly, fgrep exits with a non-zero error code if it finds nothing.  We have to override that with "|| true".
		@ deps=`comm -1 -3 /tmp/system-perl.provides /tmp/$(RPM_PACKAGE_NAME).unresolved | fgrep perl || true`; \
		if [ -n "$$deps" ]; then                                                                                \
		    echo "The following perl dependencies are not currently being suppressed, but ought to be.";        \
		    echo "$$deps";                                                                                      \
		else                                                                                                    \
		    echo "No unresolved perl dependencies were found.";                                                 \
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
conf            : $(RESTRICTED_FILES)
		chmod 600 $(RESTRICTED_FILES)

build_info	:
		scripts/make_build_info $(RPM_PACKAGE_RELEASE) $(SUBVERSION_BUILD) > info/build_info

#---------------------------------------------------------------#

# Note:  The INSTALL_ROOT path must match the definition of %_installroot in the rpmsetup/$(RPM_PACKAGE_NAME).rpmmacros file.
# There doesn't seem to be any good way to automate this correspondence, so we just define it blindly here.
INSTALL_ROOT = $(PWD)/rpmbuild/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)/INSTALL_ROOT

GWPATH = /usr/local/groundwork
GANGLIAPATH = $(GWPATH)/ganglia

$(PORTAL_PROJECT_XML)	:
	cd $(PORTAL_DIR); sed -e "s/{RELEASE}/$(RPM_PACKAGE_RELEASE)/g" pom.xml.template > pom.xml;

$(WEBAPP_PROJECT_XML)	:
	cd $(WEBAPP_DIR); sed -e "s/{RELEASE}/$(RPM_PACKAGE_RELEASE)/g" pom.xml.template > pom.xml;

PORTAL_WAR_FILE = $(PORTAL_DIR)/target/portal-ganglia-integration.war
WEBAPP_WAR_FILE = $(WEBAPP_DIR)/target/ganglia-integration.war

# In fact, there are more dependencies than just the ones listed here.
# But this will catch the updates needed when we edit the usual suspects.
# A "make clean" before building the RPM will force a complete rebuild
# of the .war files.

PORTAL_WAR_DEPENDENCIES = $(PORTAL_DIR)/src/main/webapp/WEB-INF/*
WEBAPP_WAR_DEPENDENCIES = $(WEBAPP_DIR)/src/main/WebContent/WEB-INF/cgi/*

java_version_check	:
	@if [ ! -f '$(JAVAC)' ]; then echo "ERROR:  Cannot find the Java compiler."; exit 1; fi
	@java_version=`$(JAVAC) -version 2>&1 | fgrep javac | fgrep . | awk '{print $$2}'`; \
	echo "Java compiler version is: $$java_version"; \
	if [ `expr match $$java_version $(REQUIRED_JAVA_VERSION).` -ne 4 ]; then echo "ERROR:  The Java compiler is not version $(REQUIRED_JAVA_VERSION)."; exit 1; fi

$(PORTAL_WAR_FILE)	: java_version_check $(PORTAL_PROJECT_XML) $(PORTAL_WAR_DEPENDENCIES)
	cd $(PORTAL_DIR); mvn clean war

$(WEBAPP_WAR_FILE)	: java_version_check $(WEBAPP_PROJECT_XML) $(WEBAPP_WAR_DEPENDENCIES)
	cd $(WEBAPP_DIR); mvn clean war

war	: $(PORTAL_WAR_FILE) $(WEBAPP_WAR_FILE)
	@# Happily, in the normal case when the dependency is already built, this is all you'll see.
	@echo "not love?"

# ganglia_integration_install	: build_info war
ganglia_integration_install	: build_info
	@:
	mkdir -p $(INSTALL_ROOT)$(GANGLIAPATH)/WEB-INF/classes
	mkdir -p $(INSTALL_ROOT)$(GANGLIAPATH)/WEB-INF/classes/org
	mkdir -p $(INSTALL_ROOT)$(GANGLIAPATH)/WEB-INF/classes/org/groundwork
	mkdir -p $(INSTALL_ROOT)$(GANGLIAPATH)/WEB-INF/classes/org/groundwork/portlet
	mkdir -p $(INSTALL_ROOT)$(GANGLIAPATH)/WEB-INF/classes/org/groundwork/portlet/iframe
	mkdir -p $(INSTALL_ROOT)$(GANGLIAPATH)/WEB-INF/classes/org/jboss
	mkdir -p $(INSTALL_ROOT)$(GANGLIAPATH)/WEB-INF/classes/org/jboss/portlet
	mkdir -p $(INSTALL_ROOT)$(GANGLIAPATH)/WEB-INF/classes/org/jboss/portlet/iframe
	mkdir -p $(INSTALL_ROOT)$(GANGLIAPATH)/WEB-INF/lib
	@:
	mkdir -p                            $(INSTALL_ROOT)$(GWPATH)/core/databases
	cp -p scripts/ganglia_db_create.sql $(INSTALL_ROOT)$(GWPATH)/core/databases
	@:
	mkdir -p                            $(INSTALL_ROOT)$(GWPATH)/core/databases/postgresql
	cp -p scripts/create-ganglia-db.sql $(INSTALL_ROOT)$(GWPATH)/core/databases/postgresql
	cp -p scripts/ganglia-db.sql        $(INSTALL_ROOT)$(GWPATH)/core/databases/postgresql
	cp -p scripts/ganglia-seed.sql      $(INSTALL_ROOT)$(GWPATH)/core/databases/postgresql
	@:
	mkdir -p                       $(INSTALL_ROOT)$(GWPATH)/nagios/libexec
	cp -p scripts/check_ganglia.pl $(INSTALL_ROOT)$(GWPATH)/nagios/libexec
	@:
	mkdir -p                             $(INSTALL_ROOT)$(GWPATH)/config
	cp -p config/GangliaConfigAdmin.conf $(INSTALL_ROOT)$(GWPATH)/config
	cp -p config/GangliaWebServers.conf  $(INSTALL_ROOT)$(GWPATH)/config
	cp -p config/check_ganglia.conf      $(INSTALL_ROOT)$(GWPATH)/config
	@:
	mkdir -p                                            $(INSTALL_ROOT)$(GANGLIAPATH)/portal
	cp -p config/navigation-ganglia-thresholds.xml      $(INSTALL_ROOT)$(GANGLIAPATH)/portal
	cp -p config/navigation-ganglia-web-servers.xml     $(INSTALL_ROOT)$(GANGLIAPATH)/portal
	cp -p config/pages.xml                              $(INSTALL_ROOT)$(GANGLIAPATH)/portal
	cp -p config/pg_migrate_jboss-idm.sql.ganglia-patch $(INSTALL_ROOT)$(GANGLIAPATH)/portal
	@:
	mkdir -p $(INSTALL_ROOT)$(GANGLIAPATH)/backups
	@:
	mkdir -p              $(INSTALL_ROOT)$(GANGLIAPATH)/info
	cp -p info/build_info $(INSTALL_ROOT)$(GANGLIAPATH)/info
	@:
	@:
	mkdir -p                                               $(INSTALL_ROOT)$(GANGLIAPATH)/scripts
	cp -p scripts/add-ganglia-portal-objects               $(INSTALL_ROOT)$(GANGLIAPATH)/scripts
	cp -p scripts/gwservices                               $(INSTALL_ROOT)$(GANGLIAPATH)/scripts
	cp -p scripts/master_migration_to_pg.pl.6.7.0_extended $(INSTALL_ROOT)$(GANGLIAPATH)/scripts
	cp -p scripts/mysql2postgresql.sh.6.7.0_extended       $(INSTALL_ROOT)$(GANGLIAPATH)/scripts
	cp -p scripts/mysql_ganglia_show_duplicates.sql        $(INSTALL_ROOT)$(GANGLIAPATH)/scripts
	cp -p scripts/mysql_ganglia_unique_constraints.sql     $(INSTALL_ROOT)$(GANGLIAPATH)/scripts
	@:
	mkdir -p                                               $(INSTALL_ROOT)$(GWPATH)/core/migration
	cp -p scripts/modify_navigation_objects                $(INSTALL_ROOT)$(GWPATH)/core/migration
	@:
	mkdir -p    $(INSTALL_ROOT)$(GWPATH)/common/doc/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)
	cp -p doc/GroundWork_Monitor_and_Ganglia_Integration_System_Administrator_Guide_$(RPM_PACKAGE_RELEASE).pdf \
		    $(INSTALL_ROOT)$(GWPATH)/common/doc/$(RPM_PACKAGE_NAME)-$(RPM_PACKAGE_RELEASE)
	@:
	mkdir -p             $(INSTALL_ROOT)$(GANGLIAPATH)/jsp
	cp -p jsp/iframe.jsp $(INSTALL_ROOT)$(GANGLIAPATH)/jsp
	@:
	mkdir -p                             $(INSTALL_ROOT)$(GANGLIAPATH)/cgi-bin
	cp -p scripts/GangliaConfigAdmin.cgi $(INSTALL_ROOT)$(GANGLIAPATH)/cgi-bin
	cp -p scripts/GangliaWebServers.cgi  $(INSTALL_ROOT)$(GANGLIAPATH)/cgi-bin
	@:
	mkdir -p                                     $(INSTALL_ROOT)$(GANGLIAPATH)/WEB-INF
	cp -p WEB-INF/context.xml                    $(INSTALL_ROOT)$(GANGLIAPATH)/WEB-INF
	cp -p WEB-INF/jboss-deployment-structure.xml $(INSTALL_ROOT)$(GANGLIAPATH)/WEB-INF
	cp -p WEB-INF/jboss-web.xml                  $(INSTALL_ROOT)$(GANGLIAPATH)/WEB-INF
	cp -p WEB-INF/portlet.xml                    $(INSTALL_ROOT)$(GANGLIAPATH)/WEB-INF
	cp -p WEB-INF/web.xml                        $(INSTALL_ROOT)$(GANGLIAPATH)/WEB-INF
	@:
	mkdir -p                                 $(INSTALL_ROOT)/$(GWPATH)/core/services/check-ganglia
	cp -p scripts/services/check-ganglia/run $(INSTALL_ROOT)/$(GWPATH)/core/services/check-ganglia
	@:
	mkdir -p                                     $(INSTALL_ROOT)/$(GWPATH)/core/services/check-ganglia/log
	cp -p scripts/services/check-ganglia/log/run $(INSTALL_ROOT)/$(GWPATH)/core/services/check-ganglia/log
	@:
	mkdir -p                                          $(INSTALL_ROOT)/$(GWPATH)/core/services/check-ganglia/log/main
	cp -p scripts/services/check-ganglia/log/main/log $(INSTALL_ROOT)/$(GWPATH)/core/services/check-ganglia/log/main
	@:
	mkdir -p                                                  $(INSTALL_ROOT)/$(GWPATH)/core/services/check-ganglia/log/supervise
	cp -p scripts/services/check-ganglia/log/supervise/status $(INSTALL_ROOT)/$(GWPATH)/core/services/check-ganglia/log/supervise
	cp -p scripts/services/check-ganglia/log/supervise/lock   $(INSTALL_ROOT)/$(GWPATH)/core/services/check-ganglia/log/supervise
	@:
	mkdir -p                                              $(INSTALL_ROOT)/$(GWPATH)/core/services/check-ganglia/supervise
	cp -p scripts/services/check-ganglia/supervise/status $(INSTALL_ROOT)/$(GWPATH)/core/services/check-ganglia/supervise
	cp -p scripts/services/check-ganglia/supervise/lock   $(INSTALL_ROOT)/$(GWPATH)/core/services/check-ganglia/supervise
	@:
	mkdir -p                                             $(INSTALL_ROOT)/$(GWPATH)/logs
	ln -s -f ../core/services/check-ganglia/log/main/log $(INSTALL_ROOT)/$(GWPATH)/logs/check_ganglia.log
	@:
	mkdir -p                $(INSTALL_ROOT)/$(GWPATH)/foundation/container/jpp/standalone/deployments
	ln -s -f $(GANGLIAPATH) $(INSTALL_ROOT)/$(GWPATH)/foundation/container/jpp/standalone/deployments/ganglia-app.war
	@:
	rm -f     $(INSTALL_ROOT)/$(GWPATH)/core/services/check-ganglia/log/supervise/control
	mknod     $(INSTALL_ROOT)/$(GWPATH)/core/services/check-ganglia/log/supervise/control p
	chmod 600 $(INSTALL_ROOT)/$(GWPATH)/core/services/check-ganglia/log/supervise/control
	rm -f     $(INSTALL_ROOT)/$(GWPATH)/core/services/check-ganglia/supervise/control
	mknod     $(INSTALL_ROOT)/$(GWPATH)/core/services/check-ganglia/supervise/control p
	chmod 600 $(INSTALL_ROOT)/$(GWPATH)/core/services/check-ganglia/supervise/control
	@:
	## This is no longer needed for the GWMEE 7.X.X portal structure.
	# mkdir -p                 $(INSTALL_ROOT)$(GWPATH)/foundation/container/webapps
	# cp -p $(WEBAPP_WAR_FILE) $(INSTALL_ROOT)$(GWPATH)/foundation/container/webapps
	@:
	## This is no longer needed for the GWMEE 7.X.X portal structure.
	# mkdir -p                 $(INSTALL_ROOT)$(GWPATH)/foundation/container/webapps/jboss/jboss-portal.sar
	# cp -p $(PORTAL_WAR_FILE) $(INSTALL_ROOT)$(GWPATH)/foundation/container/webapps/jboss/jboss-portal.sar
	@:
	mkdir -p                         $(INSTALL_ROOT)/etc/logrotate.d
	cp -p scripts/groundwork-ganglia $(INSTALL_ROOT)/etc/logrotate.d

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

# We must re-analyze and fix the "rpm -q -p --requires foo.rpm" and "rpm -q -p --provides foo.rpm" stuff before final
# release, to ensure that the final RPM does not depend on any packages not already supplied by the base GWMEE release.
# See the perl_deps target above for an automated analysis.
#
rpms		: ganglia_integration_install
		rpmbuild                                                                                                                        \
		    --predefine "rpm_versioned_package $(RPM_VERSIONED_PACKAGE)"                                                                \
		    --rcfile /usr/lib/rpm/rpmrc:/usr/lib/rpm/redhat/rpmrc                                                                       \
		    --macros '/usr/lib/rpm/macros:/usr/lib/rpm/platform/%{_target}/macros:rpmsetup/$(RPM_GENERIC_PACKAGE_NAME).rpmmacros'       \
		    --dbpath $(PWD)/dbpath                                                                                                      \
		    --buildroot $(INSTALL_ROOT)                                                                                                 \
		    --quiet                                                                                                                     \
		    -bb rpmsetup/$(RPM_VERSIONED_GENERIC_PACKAGE).spec 2>&1
		# rpmbuild inappropriately mangles the permissions of files to make them world-readable.
		# We need to track down why that is happening, and report it as a bug, since it should not
		# be touching anything outside of the build root.  In any case, we need to put back the
		# permissions into their proper condition for local testing.
		make conf

rpmclean	:
		rm -rf rpmbuild
		rm -rf dbpath
		rm -rf info/build_info

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

release		: rpmsetup/$(RPM_VERSIONED_GENERIC_PACKAGE).spec
		make buildclean
		make rpms

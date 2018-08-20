# makefile for the gdma packages

# Copyright 2008 GroundWork Open Source, Inc. ("GroundWork").  All rights
# reserved.  Use is subject to GroundWork commercial license terms.

#---------------------------------------------------------------#

# This value must be updated for each new release, and the value must match the ${version}
# defined in the rpmsetup/gdma-%{version}.spec file.
# Other places to also modify:  the specfile filename and various literal version references within it.
GDMA_RELEASE = 2.0.5

#---------------------------------------------------------------#

default		: release

install		:

clean		: rpmclean
		rm -rf tmp

buildclean	: clean

#---------------------------------------------------------------#

gdma_install	:
		cd bin; ./gdma_build.pl $(GDMA_RELEASE)

#---------------------------------------------------------------#

# Note on how we build RPMs:
#
# The standard rpmbuild setup depends on a ~/.rpmmacros file.  That's a truly idiotic design, because the settings
# in that file will vary from person to person, and there is absolutely no guarantee that your settings won't be
# incompatible with what it takes to build a given package.  Instead, we force all details to be specified here
# inside our source tree, and we intentionally do not depend on any resource outside the source tree.
#
# Also note that we don't use the standard organization of build directories.  Again, such a setup might suit Red Het's
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
# That all said, for the GDMA RPM builds we rely not on detailed commands here, but on a more thorough build process
# detailed in the bin/gdma_build.pl script.  What we currently have at this level is just a convenience mechanism
# for invoking that script, because of the complexities of building the RPMs as customized for individual customers
# and for multiple platforms, with a build setup that enables us to create all the Linux RPMs from one platform.
#
# What this doesn't give us is the ability to run the customer-customized builds from within this makefile.  For
# that kind of build, you'll need to descend into the bin directory and run the gdma_build.pl script directly,
# with the extra arguments you need for that customization.

rpms		: gdma_install

rpmclean	:
		rm -rf rpmbuild
		rm -f rpmsetup/gdma_arch.rpmmacros

#---------------------------------------------------------------#

# This target makes a full release from scratch, all the way from complete cleanup of any debris left behind
# by previous builds, to recompiling the world and doing the final packaging.  We establish this target to
# ensure we have a repeatable way to guarantee that the final packaged bits really do represent the version
# of the checked-in code that it's supposed to.

# Note:  For a formal external release, before building this target you must:
# * update the GDMA_RELEASE value above
# * make sure the rpmsetup/gdma-$(GDMA_RELEASE).spec file has its version-component values correctly set
# * check in the revised copies of this makefile and the specfile
# * make sure all the checked-out code is up-to-date (run "svn status -u" to check this)

release		: rpmsetup/gdma-$(GDMA_RELEASE).spec
		make buildclean
		make rpms

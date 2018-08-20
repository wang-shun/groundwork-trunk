# makefile for Perl packages used in GroundWork ticketing integration

# Copyright 2013-2017 GroundWork Open Source, Inc. ("GroundWork").  All rights
# reserved.  Use is subject to GroundWork commercial license terms.

#---------------------------------------------------------------#

# The version of "perl" to use when compiling packages.  This will make a
# difference because we need to run some of our scripts in a context where
# the environment variables will point to the GroundWork copy of Perl, and
# that will bring in some of the GroundWork Perl library modules, and some of
# those will check version compatibility against the running version of Perl.
#
# PERL = /usr/local/groundwork/perl/bin/perl
# 
# Our definition of PERL is intentionally set to .../.perl.bin instead of
# .../perl to override the irritating behavior of the GroundWork "perl" binary
# to prefix the command-line options with some -I options of its own, thereby
# overriding our overriding of the ordering of certain @INC components to favor
# our local copy instead of the GroundWork copy.  Whether this will remain a
# problem once everything is compiled and running remains to be seen.
#
PERL = /usr/local/groundwork/perl/bin/.perl.bin

# The list of Perl packages we compile includes some that are not available in
# the base Perl distribution, and some for which a newer version is demanded.

# Define the INCLUDE_MOZILLA_CA flag as either 0 or 1, to select whether you wish
# to include the Mozilla::CA package in the build to provide a standard collection
# of CA root certificates, including the particular one we actually care about for
# accessing the ServiceNow SOAP endpoint.
#
# Note that our GWMEE 7.1.1 release already includes the Mozilla::CA 20160104
# package, so there should be little reason to package it in here unless we see
# some need to override the version that we now provide in our base GWMEE release.
#
# FIX MAJOR:  Generalize the references to ServiceNow-specific files here.
# In practice, we generally prefer NOT to include Mozilla::CA, and instead we
# depend on setting the ServiceNow_SSL_CA_FILE option in the servicenow_module.conf
# config file to point to a file containing just the one CA root certificate we
# care about, that is used to validate the ServiceNow SSL server certificate.
# See the ../doc/SERVICENOW_INTEGRATION_INSTALL_NOTES file for details.
#
# Note that if you do wish to include that package, you should verify that we have
# the latest version and are building it here, to ensure that we are referencing
# the most recent SSL root certificates from public Certificate Authorities, and
# not some other, since-revoked copies.
INCLUDE_MOZILLA_CA = 0

# The PERL_XXX lists here are presented in an order which has been demonstrated
# to resolve all the inter-module build dependencies.  Don't touch this unless
# you get build errors and the only way to resolve them is to change ordering!

PERL_TARBALLS =
PERL_PACKAGES =
PERL_PACKAGE_DIRECTORIES =
ALL_PERL_PACKAGE_DIRECTORIES =

ifeq ($(LOWERCASE_TICKETING_PROVIDER),servicenow)
	PERL_TARBALLS            += ServiceNow-1.01.tar.gz
	PERL_PACKAGES            += ServiceNow
	PERL_PACKAGE_DIRECTORIES += ServiceNow-1.01
endif
ALL_PERL_PACKAGE_DIRECTORIES += ServiceNow-1.01

ifeq ($(INCLUDE_MOZILLA_CA),1)
	PERL_TARBALLS            += Mozilla-CA-20160104.tar.gz
	PERL_PACKAGES            += Mozilla-CA
	PERL_PACKAGE_DIRECTORIES += Mozilla-CA-20160104
endif
ALL_PERL_PACKAGE_DIRECTORIES += Mozilla-CA-20160104

PERL_INCLUDES =	\
	-I$(PWD)/../perl/lib
# "Programming Perl", 3/e, page 497 claims that -I implicitly adds
# platform-sepcific directories.  But we find that not to be the case
# with Perl 5.8.5, so we need to add such directories explicitly here.
ifeq ($(shell uname -p),x86_64)
	PERL_INCLUDES += -I$(PWD)/../perl/lib/x86_64-linux-thread-multi
endif
ifeq ($(shell uname -p),i686)
	PERL_INCLUDES += -I$(PWD)/../perl/lib/i386-linux-thread-multi
endif

# Note:  We're very careful here to use a "simply expanded variable" (the ":="
# operator, not the "=" operator) when defining INSTALLSITEARCH.  That ensures
# that each shell command will only be evaluated once, for efficiency.  If we
# were defining other values that depended on the current time, it would also
# ensure that a consistent value would be used throughout the entire make.

# We need a custom definition for INSTALLSITEARCH for use when installing
# packages that use Module::Build instead of ExtUtils::MakeMaker, and the
# generated code for such a package will be put in an arch-related subdirectory
# rather than a lib-related directory.  The standard behavior of Module::Build
# is to stick "perl5" into the generated lib and arch paths when --install_base
# is used on a "./Build install" command.  The Module::Build author has put a
# comment about that (could have used $Config{installstyle}, but didn't) in the
# Module/Build/Base.pm file, but he chose poorly by being inconsistent about the
# application of that option, and trying to force all users into one mold (the
# wrong one, as far as we're concerned).  To simplify our application-script
# references to the packages we are adding to the base-product Perl, we don't
# want to require the use of this extra lib/perl5 directory in @INC.  We do
# want the standard archname path component (e.g., "x86_64-linux-thread-multi")
# still left in that situation, so we must discover it here and reference it
# as needed.  This fine-tuning should not be necessary once we fold all these
# packages directly into the base release as ordinary "site"-added packages
# (/usr/local/groundwork/perl/lib/site_perl/...).  [Though truth be told, all
# of the GroundWork-added packages beyond the base set of packages that come
# with the basic Perl distribution should be installed by Bitrock as "vendor"
# packages (which will result in ".../vendor_perl/..." pathnames) rather than
# as "site" packages (which results in ".../site_perl/..." pathnames).]
INSTALLSITEARCH:=$(shell echo $(PWD)/../perl/lib/`$(PERL) -e 'use Config; print "$$Config{archname}\n";'`)

# Trying to build an RPM package under CentOS 7 is a bit more challenging
# than it used to be under CentOS 5.  That's because an ordinary shell under
# this OS has its environment modified when the shell first starts.  For
# the tcsh that we use, that happens via /etc/csh.cshrc and from there via
# /etc/profile.d/perl-homedir.csh which makes ~/perl5/ if allowed to, and
# creates the following environment variables:
#
#     PERL5LIB=$HOME/perl5/lib/perl5
#     PERL_HOMEDIR=1
#     PERL_LOCAL_LIB_ROOT=$HOME/perl5
#     PERL_MB_OPT=--install_base $HOME/perl5
#     PERL_MM_OPT=INSTALL_BASE=$HOME/perl5
#
# If ~/perl5/ cannot be created, PERL_HOMEDIR is set anyway, but the other
# environment variables are not set.  You can block some of this by having
# a ~/.perl-homedir script that contains "setenv PERL_HOMEDIR 0", but we
# would rather not depend on any particular external setup when we attempt
# a build here.

# Some of these settings are documented in Programming Perl, 3/e, page 553.
# But experience with compiling lots of CPAN modules for other GroundWork
# add-on packages (e.g., DR, Cloud Connector) has shown the larger set
# shown here is really needed.
OLD_PERL_INSTALL =                  				\
	LIB=$(PWD)/../perl/lib     				\
	INSTALLBIN=$(PWD)/../perl/bin     			\
	INSTALLSCRIPT=$(PWD)/../perl/bin     			\
	INSTALLMAN1DIR=$(PWD)/../perl/share/man/man1		\
	INSTALLMAN3DIR=$(PWD)/../perl/share/man/man3		\
	INSTALLSITEBIN=$(PWD)/../perl/bin     			\
	INSTALLSITESCRIPT=$(PWD)/../perl/bin     		\
	INSTALLSITEMAN1DIR=$(PWD)/../perl/share/man/man1	\
	INSTALLSITEMAN3DIR=$(PWD)/../perl/share/man/man3	\
	PERL="$(PERL) $(PERL_INCLUDES)"

# For these settings, look at Programming Perl, 4/e, page 637.
# FIX MAJOR:  clean this up
ALT_PERL_INSTALL =						\
	INSTALL_BASE=$(PWD)/../perl				\
	PERL="$(PERL) $(PERL_INCLUDES)"

PERL_INSTALL =                  				\
	INSTALL_BASE=$(PWD)/../perl

all	: ../perl $(PERL_PACKAGES)

../perl	:
	mkdir -p ../perl

# Note:
#   $(PERL) Makefile.PL --default
# is not a full alternative to setting PERL_MM_USE_DEFAULT, because
# it still holds up the build by pausing for interactive input.
# Instead, we want the build to run to completion, and we'll look
# at the final build log file for build errors.

Mozilla-CA	: Mozilla-CA-20160104.tar.gz
	tar xvfz Mozilla-CA-20160104.tar.gz
	cd Mozilla-CA-20160104;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

ServiceNow	: ServiceNow-1.01.tar.gz
	tar xvfz ServiceNow-1.01.tar.gz
	patch -p0 < ServiceNow-1.01.patch
	cd ServiceNow-1.01;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

clean	:
	rm -rf $(ALL_PERL_PACKAGE_DIRECTORIES)

distclean	: clean
	rm -rf ../perl

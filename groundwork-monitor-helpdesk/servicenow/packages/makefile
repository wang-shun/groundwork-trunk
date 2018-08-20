# makefile for Perl packages used in GroundWork ServiceNow integration

# Copyright 2015 GroundWork Open Source, Inc. ("GroundWork").  All rights
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

PERL_TARBALLS = 			\
	Email-Date-Format-1.005.tar.gz	\
	MIME-Types-2.09.tar.gz		\
	MIME-Lite-3.030.tar.gz		\
	ExtUtils-MakeMaker-7.04.tar.gz	\
	Class-Inspector-1.28.tar.gz	\
	Task-Weaken-1.04.tar.gz		\
	IO-SessionData-1.03.tar.gz	\
	SOAP-Lite-1.12.tar.gz		\
	ServiceNow-1.01.tar.gz

PERL_PACKAGES =			\
	Email-Date-Format	\
	MIME-Types		\
	MIME-Lite		\
	ExtUtils-MakeMaker	\
	Class-Inspector		\
	Task-Weaken		\
	IO-SessionData		\
	SOAP-Lite		\
	ServiceNow

PERL_PACKAGE_DIRECTORIES =	\
	Email-Date-Format-1.005	\
	MIME-Types-2.09		\
	MIME-Lite-3.030		\
	ExtUtils-MakeMaker-7.04	\
	Class-Inspector-1.28	\
	Task-Weaken-1.04	\
	IO-SessionData-1.03	\
	SOAP-Lite-1.12		\
	ServiceNow-1.01

ifeq ($(INCLUDE_MOZILLA_CA),1)
	PERL_TARBALLS            += Mozilla-CA-20141217.tar.gz
	PERL_PACKAGES            += Mozilla-CA
	PERL_PACKAGE_DIRECTORIES += Mozilla-CA-20141217
endif

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

# Some of these settings are documented in Programming Perl, 3/e, page 553.
# But experience with compiling lots of CPAN modules for other GroundWork
# add-on packages (e.g., DR, Cloud Connector) has shown the larger set
# shown here is really needed.
PERL_INSTALL =                  				\
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

all	: ../perl $(PERL_PACKAGES)

../perl	:
	mkdir -p ../perl

# Note:
#   $(PERL) Makefile.PL --default
# is not a full alternative to setting PERL_MM_USE_DEFAULT, because
# it still holds up the build by pausing for interactive input.
# Instead, we want the build to run to completion, and we'll look
# at the final build log file for build errors.

Class-Inspector	: Class-Inspector-1.28.tar.gz
	tar xvfz Class-Inspector-1.28.tar.gz
	cd Class-Inspector-1.28;				\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

Email-Date-Format	: Email-Date-Format-1.005.tar.gz
	tar xvfz Email-Date-Format-1.005.tar.gz
	cd Email-Date-Format-1.005;				\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

ExtUtils-MakeMaker	: ExtUtils-MakeMaker-7.04.tar.gz
	tar xvfz ExtUtils-MakeMaker-7.04.tar.gz
	cd ExtUtils-MakeMaker-7.04;				\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

IO-SessionData	: IO-SessionData-1.03.tar.gz
	tar xvfz IO-SessionData-1.03.tar.gz
	cd IO-SessionData-1.03;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

MIME-Lite	: MIME-Lite-3.030.tar.gz
	tar xvfz MIME-Lite-3.030.tar.gz
	cd MIME-Lite-3.030;					\
	export PERL_MM_USE_DEFAULT=1;				\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

MIME-Types	: MIME-Types-2.09.tar.gz
	tar xvfz MIME-Types-2.09.tar.gz
	cd MIME-Types-2.09;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

Mozilla-CA	: Mozilla-CA-20141217.tar.gz
	tar xvfz Mozilla-CA-20141217.tar.gz
	cd Mozilla-CA-20141217;					\
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

SOAP-Lite	: SOAP-Lite-1.12.tar.gz
	tar xvfz SOAP-Lite-1.12.tar.gz
	cd SOAP-Lite-1.12;					\
	export PERL_MM_USE_DEFAULT=1;				\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

Task-Weaken	: Task-Weaken-1.04.tar.gz
	tar xvfz Task-Weaken-1.04.tar.gz
	cd Task-Weaken-1.04;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

clean	:
	rm -rf $(PERL_PACKAGE_DIRECTORIES)

distclean	: clean
	rm -rf ../perl

# makefile for Perl packages used in GroundWork Cloud Connector

# Copyright 2010 GroundWork Open Source, Inc. ("GroundWork").  All rights
# reserved.  Use is subject to GroundWork commercial license terms.

#---------------------------------------------------------------#

# The version of "perl" to use when compiling packages.  This will make a
# difference because we need to run some of our scripts in a context where
# the environment variables will point to the GroundWork copy of Perl, and
# that will bring in some of the GroundWork Perl library modules, and some of 
# those will check version compatibility against the running version of Perl.
#
PERL = /usr/local/groundwork/perl/bin/perl
PERL_VERSION := $(shell $(PERL) -e 'printf "%vd", $$^V;')

# The list of Perl packages we compile includes some that are not available in
# the base Perl distribution, and some for which a newer version is demanded.

PERL_TARBALLS =				\
	Sys-Syslog-0.27.tar.gz		\
	Compress-Raw-Bzip2-2.026.tar.gz	\
	Compress-Raw-Zlib-2.026.tar.gz	\
	IO-Compress-2.026.tar.gz	\
	File-Path-2.08.tar.gz

PERL_PACKAGES =			\
	Sys-Syslog		\
	Compress-Raw-Bzip2	\
	Compress-Raw-Zlib	\
	IO-Compress		\
	File-Path

PERL_PACKAGE_DIRECTORIES =		\
	Sys-Syslog-0.27			\
	Compress-Raw-Bzip2-2.026	\
	Compress-Raw-Zlib-2.026		\
	IO-Compress-2.026		\
	File-Path-2.08

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

# Because the Perl modules we compile here will be installed under the
# standard GroundWork Perl locations, because they will be referenced
# by scripts masquerading as standard GroundWork product components
# until they eventually do get folded into the product, we need to set
# these paths to something similar to what they will be when they will
# appear in the GroundWork Perl distribution.  Some further adjustments
# will need to be made by our makefiles after the modules are built, to
# get them folded into our RPM correctly, but that is a separate matter.
GROUNDWORK_PERL_INSTALL =	\
	LIB=$(PWD)/../perl/lib/site_perl/$(PERL_VERSION)	\
	INSTALLBIN=$(PWD)/../perl/bin				\
	INSTALLSCRIPT=$(PWD)/../perl/bin			\
	INSTALLMAN1DIR=$(PWD)/../perl/man/man1			\
	INSTALLMAN3DIR=$(PWD)/../perl/man/man3			\
	INSTALLSITEBIN=$(PWD)/../perl/bin			\
	INSTALLSITESCRIPT=$(PWD)/../perl/bin			\
	INSTALLSITEMAN1DIR=$(PWD)/../perl/man/man1		\
	INSTALLSITEMAN3DIR=$(PWD)/../perl/man/man3		\
	PERL="$(PERL) $(PERL_INCLUDES)"

# Well, that's what we thought, anyway.  But those settings break the build
# by not being able to find dependencies.  So we're going to have to find some
# other way to either build these modules or reference tham at run time.
PERL_INSTALL =							\
	LIB=$(PWD)/../perl/lib					\
	INSTALLBIN=$(PWD)/../perl/bin				\
	INSTALLSCRIPT=$(PWD)/../perl/bin			\
	INSTALLMAN1DIR=$(PWD)/../perl/share/man/man1		\
	INSTALLMAN3DIR=$(PWD)/../perl/share/man/man3		\
	INSTALLSITEBIN=$(PWD)/../perl/bin			\
	INSTALLSITESCRIPT=$(PWD)/../perl/bin			\
	INSTALLSITEMAN1DIR=$(PWD)/../perl/share/man/man1	\
	INSTALLSITEMAN3DIR=$(PWD)/../perl/share/man/man3	\
	PERL="$(PERL) $(PERL_INCLUDES)"

all	: ../perl $(PERL_PACKAGES)

../perl	:
	mkdir -p ../perl

# Note:
#   $(PERL) Makefile.PL --default
# may be an alternative to setting PERL_MM_USE_DEFAULT.

Sys-Syslog	: Sys-Syslog-0.27.tar.gz
	tar xvfz Sys-Syslog-0.27.tar.gz
	cd Sys-Syslog-0.27;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

Compress-Raw-Zlib       : Compress-Raw-Zlib-2.026.tar.gz
	tar xvfz Compress-Raw-Zlib-2.026.tar.gz
	cd Compress-Raw-Zlib-2.026;				\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install

Compress-Raw-Bzip2       : Compress-Raw-Bzip2-2.026.tar.gz
	tar xvfz Compress-Raw-Bzip2-2.026.tar.gz
	cd Compress-Raw-Bzip2-2.026;				\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install

IO-Compress       : IO-Compress-2.026.tar.gz
	tar xvfz IO-Compress-2.026.tar.gz
	cd IO-Compress-2.026;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install

File-Path       : File-Path-2.08.tar.gz
	tar xvfz File-Path-2.08.tar.gz
	cd File-Path-2.08;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install

clean	:
	rm -rf $(PERL_PACKAGE_DIRECTORIES)

distclean	: clean
	rm -rf ../perl

# makefile for GDMA Perl modules

# ========================================================================

# Copyright (c) 2009 GroundWork Open Source, Inc.  All rights reserved.
# Use is subject to GroundWork commercial license terms.

# ========================================================================

# Modify this setting to be "true" or "false" (without quotes) as needed
# for initial development and building.
INSTALL = true

# This is the top-level directory for all groundwork-related software,
# not just the GDMA package.
GROUNDWORK_PREFIX = /opt/groundwork

# Set this to reflect where you wish the compiled Perl modules to be installed;
# in actuality, the plugins themselves will be placed in some directory underneath this one.
GDMA_PREFIX = $(GROUNDWORK_PREFIX)/gdma

# We only deal with Solaris 2.6 and above (Solaris 7, 8, 9, 10, ...), and not
# the older Solaris 2.5 or 2.5.1 releases, so this simple pattern matching
# suffices, producing "6" for Solaris 2.6 (SunOS 5.6) even though that was
# never the marketing designation.
SOLARIS_RELEASE :sh = uname -r | sed 's/.*\.//'

# Solaris 8 doesn't have a sufficiently up-to-date Perl to compile and run
# these Perl modules.  Perl 5.006 is required, but 5.00503 is the standard
# version on this platform.
MINIMUM_SOLARIS_RELEASE = 9

# Version numbers for all the Perl packages, centrally established here
# for ease of maintenance as we possibly bring in additional modules in
# the future.

# These modules are needed for Solaris 10 and earlier.
Compress_Raw_Zlib_RELEASE = 2.015
Compress_Zlib_RELEASE     = 2.015
Crypt_SSLeay_RELEASE      = 0.57
HTML_Parser_RELEASE       = 3.58
HTML_Tagset_RELEASE       = 3.20
IO_Compress_Base_RELEASE  = 2.015
IO_Compress_Zlib_RELEASE  = 2.015
Scalar_List_Utils_RELEASE = 1.19
URI_RELEASE               = 1.37
libwww_perl_RELEASE       = 5.820
Telnet_RELEASE            = 3.03
Storable_RELEASE          = 2.20
# These modules are needed for Solaris 9 and earlier.
Digest_RELEASE       = 1.15
Digest_MD5_RELEASE   = 2.38
libnet_RELEASE       = 1.22
MIME_Base64_RELEASE  = 3.07
Test_Harness_RELEASE = 3.14
Test_Simple_RELEASE  = 0.86

# The modules listed here are ordered to minimize the amount of
# unresolved dependencies detected during the build process.

# FIX THIS:  port to OpenSolaris by finding out exactly which
# modules we need in that environment.

SOLARIS_9_PERL_MODULES =	\
	MIME-Base64		\
	Digest			\
	Digest-MD5		\
	Test-Harness		\
	Test-Simple		\
	libnet			\
	Telnet			\
	Storable

SOLARIS_10_PERL_MODULES =	\
	Scalar-List-Utils	\
	Compress-Raw-Zlib	\
	IO-Compress-Base	\
	IO-Compress-Zlib	\
	Compress-Zlib		\
	HTML-Tagset		\
	HTML-Parser		\
	Crypt-SSLeay		\
	URI			\
	libwww-perl             \
	Telnet			\
	Storable

ALL_PERL_MODULES = $(SOLARIS_9_PERL_MODULES) $(SOLARIS_10_PERL_MODULES)

# FIX THIS:  port this rule to OpenSolaris, too
default	:
	if [ $(SOLARIS_RELEASE) -ge $(MINIMUM_SOLARIS_RELEASE) -a $(SOLARIS_RELEASE) -le 9 ]; then	\
	    make $(SOLARIS_9_PERL_MODULES);								\
	fi;
	if [ $(SOLARIS_RELEASE) -ge $(MINIMUM_SOLARIS_RELEASE) -a $(SOLARIS_RELEASE) -le 10 ]; then	\
	    make $(SOLARIS_10_PERL_MODULES);								\
	fi;

clean	:
	rm -rf							\
	    Compress-Raw-Zlib-$(Compress_Raw_Zlib_RELEASE)	\
	    Compress-Zlib-$(Compress_Zlib_RELEASE)		\
	    Crypt-SSLeay-$(Crypt_SSLeay_RELEASE)		\
	    HTML-Parser-$(HTML_Parser_RELEASE)			\
	    HTML-Tagset-$(HTML_Tagset_RELEASE)			\
	    IO-Compress-Base-$(IO_Compress_Base_RELEASE)	\
	    IO-Compress-Zlib-$(IO_Compress_Zlib_RELEASE)	\
	    Scalar-List-Utils-$(Scalar_List_Utils_RELEASE)	\
	    URI-$(URI_RELEASE)					\
	    libwww-perl-$(libwww_perl_RELEASE)			\
	    Digest-$(Digest_RELEASE)				\
	    Digest-MD5-$(Digest_MD5_RELEASE)			\
	    libnet-$(libnet_RELEASE)				\
	    MIME-Base64-$(MIME_Base64_RELEASE)			\
	    Test-Harness-$(Test_Harness_RELEASE)		\
	    Test-Simple-$(Test_Simple_RELEASE)                  \
            Net-Telnet-${Telnet_RELEASE}			\
            Storable-${Storable_RELEASE}			\
	rm -f $(ALL_PERL_MODULES)

Compress-Raw-Zlib	:
	gunzip -c Compress-Raw-Zlib-$(Compress_Raw_Zlib_RELEASE).tar.gz | tar xf -
	cd Compress-Raw-Zlib-$(Compress_Raw_Zlib_RELEASE);				\
	if [ -f Makefile ]; then make distclean; fi;					\
	PERL5LIB=/opt/groundwork/gdma/lib/site_perl					\
	    perl Makefile.PL PREFIX=$(GDMA_PREFIX) LIB=$(GDMA_PREFIX)/lib/site_perl;	\
	make;										\
	if $(INSTALL); then make install; fi
	touch Compress-Raw-Zlib

Compress-Zlib	:
	gunzip -c Compress-Zlib-$(Compress_Zlib_RELEASE).tar.gz | tar xf -
	cd Compress-Zlib-$(Compress_Zlib_RELEASE);					\
	if [ -f Makefile ]; then make distclean; fi;					\
	PERL5LIB=/opt/groundwork/gdma/lib/site_perl					\
	    perl Makefile.PL PREFIX=$(GDMA_PREFIX) LIB=$(GDMA_PREFIX)/lib/site_perl;	\
	make;										\
	if $(INSTALL); then make install; fi
	touch Compress-Zlib

# --lib here lets the build find our copy of the openssl library.
# We close the standard input stream when running perl so it won't prompt and wait for input.
Crypt-SSLeay	:
	gunzip -c Crypt-SSLeay-$(Crypt_SSLeay_RELEASE).tar.gz | tar xf -
	cd Crypt-SSLeay-$(Crypt_SSLeay_RELEASE);					\
	if [ -f Makefile ]; then make distclean; fi;					\
	mv Makefile.PL Makefile.PL.orig;						\
	cp ../Crypt-SSLeay-$(Crypt_SSLeay_RELEASE)_fixed_Makefile.PL Makefile.PL;	\
	PERL5LIB=/opt/groundwork/gdma/lib/site_perl					\
	    perl Makefile.PL PREFIX=$(GDMA_PREFIX) LIB=$(GDMA_PREFIX)/lib/site_perl --lib=$(GDMA_PREFIX) <&-;	\
	make;										\
	if $(INSTALL); then make install; fi
	touch Crypt-SSLeay

Digest	:
	gunzip -c Digest-$(Digest_RELEASE).tar.gz | tar xf -
	cd Digest-$(Digest_RELEASE);							\
	if [ -f Makefile ]; then make distclean; fi;					\
	PERL5LIB=/opt/groundwork/gdma/lib/site_perl					\
	    perl Makefile.PL PREFIX=$(GDMA_PREFIX) LIB=$(GDMA_PREFIX)/lib/site_perl;	\
	make;										\
	if $(INSTALL); then make install; fi
	touch Digest

Digest-MD5	:
	gunzip -c Digest-MD5-$(Digest_MD5_RELEASE).tar.gz | tar xf -
	cd Digest-MD5-$(Digest_MD5_RELEASE);						\
	if [ -f Makefile ]; then make distclean; fi;					\
	PERL5LIB=/opt/groundwork/gdma/lib/site_perl					\
	    perl Makefile.PL PREFIX=$(GDMA_PREFIX) LIB=$(GDMA_PREFIX)/lib/site_perl;	\
	make;										\
	if $(INSTALL); then make install; fi
	touch Digest-MD5

HTML-Parser	:
	gunzip -c HTML-Parser-$(HTML_Parser_RELEASE).tar.gz | tar xf -
	cd HTML-Parser-$(HTML_Parser_RELEASE);						\
	if [ -f Makefile ]; then make distclean; fi;					\
	PERL5LIB=/opt/groundwork/gdma/lib/site_perl					\
	    perl Makefile.PL PREFIX=$(GDMA_PREFIX) LIB=$(GDMA_PREFIX)/lib/site_perl;	\
	make;										\
	if $(INSTALL); then make install; fi
	touch HTML-Parser

HTML-Tagset	:
	gunzip -c HTML-Tagset-$(HTML_Tagset_RELEASE).tar.gz | tar xf -
	cd HTML-Tagset-$(HTML_Tagset_RELEASE);						\
	if [ -f Makefile ]; then make distclean; fi;					\
	PERL5LIB=/opt/groundwork/gdma/lib/site_perl					\
	    perl Makefile.PL PREFIX=$(GDMA_PREFIX) LIB=$(GDMA_PREFIX)/lib/site_perl;	\
	make;										\
	if $(INSTALL); then make install; fi
	touch HTML-Tagset

IO-Compress-Base	:
	gunzip -c IO-Compress-Base-$(IO_Compress_Base_RELEASE).tar.gz | tar xf -
	cd IO-Compress-Base-$(IO_Compress_Base_RELEASE);				\
	if [ -f Makefile ]; then make distclean; fi;					\
	PERL5LIB=/opt/groundwork/gdma/lib/site_perl					\
	    perl Makefile.PL PREFIX=$(GDMA_PREFIX) LIB=$(GDMA_PREFIX)/lib/site_perl;	\
	make;										\
	if $(INSTALL); then make install; fi
	touch IO-Compress-Base

IO-Compress-Zlib	:
	gunzip -c IO-Compress-Zlib-$(IO_Compress_Zlib_RELEASE).tar.gz | tar xf -
	cd IO-Compress-Zlib-$(IO_Compress_Zlib_RELEASE);				\
	if [ -f Makefile ]; then make distclean; fi;					\
	PERL5LIB=/opt/groundwork/gdma/lib/site_perl					\
	    perl Makefile.PL PREFIX=$(GDMA_PREFIX) LIB=$(GDMA_PREFIX)/lib/site_perl;	\
	make;										\
	if $(INSTALL); then make install; fi
	touch IO-Compress-Zlib

MIME-Base64	:
	gunzip -c MIME-Base64-$(MIME_Base64_RELEASE).tar.gz | tar xf -
	cd MIME-Base64-$(MIME_Base64_RELEASE);				\
	if [ -f Makefile ]; then make distclean; fi;					\
	PERL5LIB=/opt/groundwork/gdma/lib/site_perl					\
	    perl Makefile.PL PREFIX=$(GDMA_PREFIX) LIB=$(GDMA_PREFIX)/lib/site_perl;	\
	make;										\
	if $(INSTALL); then make install; fi
	touch MIME-Base64

Scalar-List-Utils	:
	gunzip -c Scalar-List-Utils-$(Scalar_List_Utils_RELEASE).tar.gz | tar xf -
	cd Scalar-List-Utils-$(Scalar_List_Utils_RELEASE);				\
	if [ -f Makefile ]; then make distclean; fi;					\
	PERL5LIB=/opt/groundwork/gdma/lib/site_perl					\
	    perl Makefile.PL PREFIX=$(GDMA_PREFIX) LIB=$(GDMA_PREFIX)/lib/site_perl;	\
	make;										\
	if $(INSTALL); then make install; fi
	touch Scalar-List-Utils

Test-Harness	:
	gunzip -c Test-Harness-$(Test_Harness_RELEASE).tar.gz | tar xf -
	cd Test-Harness-$(Test_Harness_RELEASE);					\
	if [ -f Makefile ]; then make distclean; fi;					\
	PERL5LIB=/opt/groundwork/gdma/lib/site_perl					\
	    perl Makefile.PL PREFIX=$(GDMA_PREFIX) LIB=$(GDMA_PREFIX)/lib/site_perl;	\
	make;										\
	if $(INSTALL); then make install; fi
	touch Test-Harness

Test-Simple	:
	gunzip -c Test-Simple-$(Test_Simple_RELEASE).tar.gz | tar xf -
	cd Test-Simple-$(Test_Simple_RELEASE);						\
	if [ -f Makefile ]; then make distclean; fi;					\
	PERL5LIB=/opt/groundwork/gdma/lib/site_perl					\
	    perl Makefile.PL PREFIX=$(GDMA_PREFIX) LIB=$(GDMA_PREFIX)/lib/site_perl;	\
	make;										\
	if $(INSTALL); then make install; fi
	touch Test-Simple

URI	:
	gunzip -c URI-$(URI_RELEASE).tar.gz | tar xf -
	cd URI-$(URI_RELEASE);								\
	if [ -f Makefile ]; then make distclean; fi;					\
	PERL5LIB=/opt/groundwork/gdma/lib/site_perl					\
	    perl Makefile.PL PREFIX=$(GDMA_PREFIX) LIB=$(GDMA_PREFIX)/lib/site_perl;	\
	make;										\
	if $(INSTALL); then make install; fi
	touch URI

libnet	:
	gunzip -c libnet-$(libnet_RELEASE).tar.gz | tar xf -
	cd libnet-$(libnet_RELEASE);							\
	if [ -f Makefile ]; then make distclean; fi;					\
	sed -e 's@/usr/local/bin/perl@/usr/bin/perl@' Configure > configure;		\
	chmod +x configure;								\
	./configure -d;									\
	PERL5LIB=/opt/groundwork/gdma/lib/site_perl					\
	    perl Makefile.PL PREFIX=$(GDMA_PREFIX) LIB=$(GDMA_PREFIX)/lib/site_perl;	\
	make;										\
	if $(INSTALL); then make install; fi
	touch libnet

Telnet	:
	gunzip -c Net-Telnet-${Telnet_RELEASE}.tar.gz | tar xf - 
	cd Net-Telnet-${Telnet_RELEASE};								\
	    perl Makefile.PL INSTALLSITELIB=$(GDMA_PREFIX)/lib/site_perl INSTALLMAN3DIR=$(GDMA_PREFIX)/share/man/man3/;	\
	make;												\
	make test;											\
	make pure_install;										\
	touch Telnet

libwww-perl	:
	gunzip -c libwww-perl-$(libwww_perl_RELEASE).tar.gz | tar xf -
	cd libwww-perl-$(libwww_perl_RELEASE);						\
	if [ -f Makefile ]; then make distclean; fi;					\
	PERL5LIB=/opt/groundwork/gdma/lib/site_perl					\
	    perl Makefile.PL PREFIX=$(GDMA_PREFIX) LIB=$(GDMA_PREFIX)/lib/site_perl;	\
	make;										\
	if $(INSTALL); then make install; fi
	touch libwww-perl

Storable	:
	gunzip -c Storable-$(Storable_RELEASE).tar.gz | tar xf -
	cd Storable-$(Storable_RELEASE);														\
	perl Makefile.PL LIB=$(GDMA_PREFIX)/lib/site_perl INSTALLMAN1DIR=$(GDMA_PREFIX)/share/man/man1/ INSTALLMAN3DIR=$(GDMA_PREFIX)/share/man/man3/;	\
	make;																		\
	make install;																	\
	touch Storable																	\

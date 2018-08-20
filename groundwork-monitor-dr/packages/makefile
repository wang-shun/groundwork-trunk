# makefile for Perl packages used in GroundWork DR replication

# Copyright 2010 GroundWork Open Source, Inc. ("GroundWork").  All rights
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
# For the time being, though, we are forced to use the OS-supplied Perl because
# the GW-supplied Perl does not include the syscall.ph file (GWMON-8508), which
# is needed by some of the scripting that references these packages.
#
PERL = /usr/bin/perl

# The list of Perl packages we compile includes some that are not available in
# the base Perl distribution, and some for which a newer version is demanded.

PERL_TARBALLS = 			\
	Sys-Syslog-0.27.tar.gz		\
	ExtUtils-MakeMaker-6.56.tar.gz	\
	Parse-RecDescent-1.964.tar.gz	\
	IO-1.25.tar.gz			\
	IO-Tty-1.08.tar.gz		\
	Storable-2.21.tar.gz		\
	Devel-Size-0.71.tar.gz		\
	Test-Harness-3.21.tar.gz	\
	ExtUtils-CBuilder-0.2702.tar.gz	\
	ExtUtils-ParseXS-2.2203.tar.gz	\
	Module-Build-0.3603.tar.gz	\
	Module-CoreList-2.26.tar.gz	\
	Number-Compare-0.01.tar.gz	\
	Text-Glob-0.08.tar.gz		\
	File-Find-Rule-0.32.tar.gz	\
	Devel-Symdump-2.08.tar.gz	\
	Pod-Coverage-0.20.tar.gz	\
	Pod-Escapes-1.04.tar.gz		\
	Pod-Simple-3.13.tar.gz		\
	Test-Simple-0.94.tar.gz		\
	Test-Pod-1.41.tar.gz		\
	Test-Pod-Coverage-1.08.tar.gz	\
	Test-Distribution-2.00.tar.gz	\
	Test-Tester-0.107.tar.gz	\
	Test-NoWarnings-1.01.tar.gz	\
	POE-Test-Loops-1.033.tar.gz	\
	POE-1.287.tar.gz		\
	POE-API-Peek-1.34.tar.gz	\
	POE-Component-IKC-0.2200.tar.gz	\
	YAML-LibYAML-0.32.tar.gz	\
	boolean-0.20.tar.gz		\
	DBI-1.609.tar.gz		\
	DBD-mysql-4.013.tar.gz		\
	File-Path-2.08.tar.gz		\
	PathTools-3.31.tar.gz

PERL_PACKAGES =			\
	Sys-Syslog		\
	ExtUtils-MakeMaker	\
	Parse-RecDescent	\
	IO			\
	IO-Tty			\
	Storable		\
	Devel-Size		\
	Test-Harness		\
	ExtUtils-CBuilder	\
	ExtUtils-ParseXS	\
	Module-Build		\
	Module-CoreList		\
	Number-Compare		\
	Text-Glob		\
	File-Find-Rule		\
	Devel-Symdump		\
	Pod-Coverage		\
	Pod-Escapes		\
	Pod-Simple		\
	Test-Simple		\
	Test-Pod		\
	Test-Pod-Coverage	\
	Test-Distribution	\
	Test-Tester		\
	Test-NoWarnings		\
	POE-Test-Loops		\
	POE			\
	POE-API-Peek		\
	POE-Component-IKC	\
	YAML-LibYAML		\
	boolean			\
	DBI			\
	DBD-mysql		\
	File-Path		\
	PathTools

PERL_PACKAGE_DIRECTORIES =		\
	Sys-Syslog-0.27			\
	ExtUtils-MakeMaker-6.56		\
	Parse-RecDescent-1.964		\
	IO-1.25				\
	IO-Tty-1.08			\
	Storable-2.21			\
	Devel-Size-0.71			\
	Test-Harness-3.21		\
	ExtUtils-CBuilder-0.2702	\
	ExtUtils-ParseXS-2.2203		\
	Module-Build-0.3603		\
	Module-CoreList-2.26		\
	Number-Compare-0.01		\
	Text-Glob-0.08			\
	File-Find-Rule-0.32		\
	Devel-Symdump-2.08		\
	Pod-Coverage-0.20		\
	Pod-Escapes-1.04		\
	Pod-Simple-3.13			\
	Test-Simple-0.94		\
	Test-Pod-1.41			\
	Test-Pod-Coverage-1.08		\
	Test-Distribution-2.00		\
	Test-Tester-0.107		\
	Test-NoWarnings-1.01		\
	POE-Test-Loops-1.033		\
	POE-1.287			\
	POE-API-Peek-1.34		\
	POE-Component-IKC-0.2200	\
	YAML-LibYAML-0.32		\
	boolean-0.20			\
	DBI-1.609			\
	DBD-mysql-4.013			\
	File-Path-2.08			\
	PathTools-3.31

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
# may be an alternative to setting PERL_MM_USE_DEFAULT.

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
Sys-Syslog	: Sys-Syslog-0.27.tar.gz
	tar xvfz Sys-Syslog-0.27.tar.gz
	cd Sys-Syslog-0.27;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
ExtUtils-MakeMaker	: ExtUtils-MakeMaker-6.56.tar.gz
	tar xvfz ExtUtils-MakeMaker-6.56.tar.gz
	cd ExtUtils-MakeMaker-6.56;				\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
Parse-RecDescent	: Parse-RecDescent-1.964.tar.gz
	tar xvfz Parse-RecDescent-1.964.tar.gz
	cd Parse-RecDescent-1.964;				\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
IO	: IO-1.25.tar.gz
	tar xvfz IO-1.25.tar.gz
	cd IO-1.25;						\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
IO-Tty	: IO-Tty-1.08.tar.gz
	tar xvfz IO-Tty-1.08.tar.gz
	cd IO-Tty-1.08;						\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
Storable	: Storable-2.21.tar.gz
	tar xvfz Storable-2.21.tar.gz
	cd Storable-2.21;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
Devel-Size	: Devel-Size-0.71.tar.gz
	tar xvfz Devel-Size-0.71.tar.gz
	cd Devel-Size-0.71;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
Test-Harness	: Test-Harness-3.21.tar.gz
	tar xvfz Test-Harness-3.21.tar.gz
	cd Test-Harness-3.21;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
ExtUtils-CBuilder	: ExtUtils-CBuilder-0.2702.tar.gz
	tar xvfz ExtUtils-CBuilder-0.2702.tar.gz
	cd ExtUtils-CBuilder-0.2702;				\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
ExtUtils-ParseXS	: ExtUtils-ParseXS-2.2203.tar.gz
	tar xvfz ExtUtils-ParseXS-2.2203.tar.gz
	cd ExtUtils-ParseXS-2.2203;				\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
Module-Build	: Module-Build-0.3603.tar.gz
	tar xvfz Module-Build-0.3603.tar.gz
	cd Module-Build-0.3603;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
Module-CoreList	: Module-CoreList-2.26.tar.gz
	tar xvfz Module-CoreList-2.26.tar.gz
	cd Module-CoreList-2.26;				\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
Number-Compare	: Number-Compare-0.01.tar.gz
	tar xvfz Number-Compare-0.01.tar.gz
	cd Number-Compare-0.01;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
Text-Glob	: Text-Glob-0.08.tar.gz
	tar xvfz Text-Glob-0.08.tar.gz
	cd Text-Glob-0.08;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
File-Find-Rule	: File-Find-Rule-0.32.tar.gz
	tar xvfz File-Find-Rule-0.32.tar.gz
	cd File-Find-Rule-0.32;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
Devel-Symdump	: Devel-Symdump-2.08.tar.gz
	tar xvfz Devel-Symdump-2.08.tar.gz
	cd Devel-Symdump-2.08;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
Pod-Coverage	: Pod-Coverage-0.20.tar.gz
	tar xvfz Pod-Coverage-0.20.tar.gz
	cd Pod-Coverage-0.20;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
Pod-Escapes	: Pod-Escapes-1.04.tar.gz
	tar xvfz Pod-Escapes-1.04.tar.gz
	cd Pod-Escapes-1.04;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
Pod-Simple	: Pod-Simple-3.13.tar.gz
	tar xvfz Pod-Simple-3.13.tar.gz
	cd Pod-Simple-3.13;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
Test-Simple	: Test-Simple-0.94.tar.gz
	tar xvfz Test-Simple-0.94.tar.gz
	cd Test-Simple-0.94;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
Test-Pod	: Test-Pod-1.41.tar.gz
	tar xvfz Test-Pod-1.41.tar.gz
	cd Test-Pod-1.41;					\
	$(PERL) $(PERL_INCLUDES) Build.PL $(PERL_INSTALL);	\
	./Build;						\
	./Build install --install_base $(PWD)/../perl --install_path lib=$(PWD)/../perl/lib

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
Test-Pod-Coverage	: Test-Pod-Coverage-1.08.tar.gz
	tar xvfz Test-Pod-Coverage-1.08.tar.gz
	cd Test-Pod-Coverage-1.08;				\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
Test-Distribution	: Test-Distribution-2.00.tar.gz
	tar xvfz Test-Distribution-2.00.tar.gz
	cd Test-Distribution-2.00;				\
	$(PERL) $(PERL_INCLUDES) Build.PL $(PERL_INSTALL);	\
	./Build;						\
	./Build install --install_base $(PWD)/../perl --install_path lib=$(PWD)/../perl/lib

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
Test-Tester	: Test-Tester-0.107.tar.gz
	tar xvfz Test-Tester-0.107.tar.gz
	cd Test-Tester-0.107;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
Test-NoWarnings	: Test-NoWarnings-1.01.tar.gz
	tar xvfz Test-NoWarnings-1.01.tar.gz
	cd Test-NoWarnings-1.01;				\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
POE-Test-Loops	: POE-Test-Loops-1.033.tar.gz
	tar xvfz POE-Test-Loops-1.033.tar.gz
	cd POE-Test-Loops-1.033;				\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
POE	: POE-1.287.tar.gz
	tar xvfz POE-1.287.tar.gz
	cd POE-1.287;						\
	export PERL_MM_USE_DEFAULT=1;				\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
POE-API-Peek	: POE-API-Peek-1.34.tar.gz
	tar xvfz POE-API-Peek-1.34.tar.gz
	cd POE-API-Peek-1.34;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
POE-Component-IKC	: POE-Component-IKC-0.2200.tar.gz
	tar xvfz POE-Component-IKC-0.2200.tar.gz
	patch -p0 < ../patches/POE-Component-IKC-0.2200::IKC::Responder.patch
	cd POE-Component-IKC-0.2200;				\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
YAML-LibYAML	: YAML-LibYAML-0.32.tar.gz
	tar xvfz YAML-LibYAML-0.32.tar.gz
	cd YAML-LibYAML-0.32;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
boolean	: boolean-0.20.tar.gz
	tar xvfz boolean-0.20.tar.gz
	cd boolean-0.20;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
DBI	: DBI-1.609.tar.gz
	tar xvfz DBI-1.609.tar.gz
	cd DBI-1.609;						\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
DBD-mysql	: DBD-mysql-4.013.tar.gz
	tar xvfz DBD-mysql-4.013.tar.gz
	# The build of this package needs to find mysql_config in $PATH.
	# We make sure it finds the right one by prepending its location
	# to the existing PATH.  But see also GWMON-8538 for how we must
	# patch mysql_config to emit the correct --socket path for our use.
	PATH=/usr/local/groundwork/mysql/bin:$$PATH;		\
	cd DBD-mysql-4.013;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
File-Path	: File-Path-2.08.tar.gz
	tar xvfz File-Path-2.08.tar.gz
	cd File-Path-2.08;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

# FIX THIS:  why don't we reference $(PERL_INCLUDES) here?  or, do we need it?
PathTools	: PathTools-3.31.tar.gz
	tar xvfz PathTools-3.31.tar.gz
	cd PathTools-3.31;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

clean	:
	rm -rf $(PERL_PACKAGE_DIRECTORIES)

distclean	: clean
	rm -rf ../perl

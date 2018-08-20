# makefile for Perl packages used in GroundWork Webmetrics integration

# Copyright 2011 GroundWork Open Source, Inc. ("GroundWork").  All rights
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

# At the time of this writing, essentially all of the modules included here are
# present in support of providing the DateTime package to our application, due
# to long chains of inter-module dependencies.
#
# The PERL_XXX lists here are presented in an order which has been demonstrated
# to resolve all the inter-module build dependencies.  Don't touch this unless
# you get build errors and the only way to resolve them is to change ordering!

PERL_TARBALLS = 				\
	Config-General-2.50.tar.gz		\
	POSIX-RT-Timer-0.009.tar.gz		\
	Sys-Syslog-0.29.tar.gz			\
	version-0.88.tar.gz			\
	Test-Harness-3.23.tar.gz		\
	Locale-Maketext-Simple-0.21.tar.gz	\
	Params-Check-0.28.tar.gz		\
	Module-CoreList-2.46.tar.gz		\
	Module-Load-0.18.tar.gz			\
	Module-Load-Conditional-0.44.tar.gz	\
	IPC-Cmd-0.70.tar.gz			\
	ExtUtils-Manifest-1.58.tar.gz		\
	ExtUtils-Install-1.54.tar.gz		\
	ExtUtils-MakeMaker-6.56.tar.gz		\
	ExtUtils-Command-1.17.tar.gz		\
	ExtUtils-CBuilder-0.280202.tar.gz	\
	ExtUtils-ParseXS-2.2206.tar.gz		\
	Test-Simple-0.98.tar.gz			\
	Version-Requirements-0.101020.tar.gz	\
	JSON-PP-2.27105.tar.gz			\
	File-Temp-0.22.tar.gz			\
	CPAN-Meta-YAML-0.003.tar.gz		\
	Parse-CPAN-Meta-1.4401.tar.gz		\
	CPAN-Meta-2.110930.tar.gz		\
	Perl-OSType-1.002.tar.gz		\
	Module-Metadata-1.000004.tar.gz		\
	Module-Build-0.3800.tar.gz		\
	Try-Tiny-0.09.tar.gz			\
	Exporter-5.63.tar.gz			\
	Test-Fatal-0.005.tar.gz			\
	Class-Load-0.06.tar.gz			\
	Class-Singleton-1.4.tar.gz		\
	parent-0.225.tar.gz			\
	DateTime-TimeZone-1.34.tar.gz		\
	DateTime-Locale-0.45.tar.gz		\
	Math-Round-0.06.tar.gz			\
	DateTime-0.70.tar.gz

PERL_PACKAGES =				\
	Config-General			\
	POSIX-RT-Timer			\
	Sys-Syslog			\
	version				\
	Test-Harness			\
	Locale-Maketext-Simple		\
	Params-Check			\
	Module-CoreList			\
	Module-Load			\
	Module-Load-Conditional		\
	IPC-Cmd				\
	ExtUtils-Manifest		\
	ExtUtils-Install		\
	ExtUtils-MakeMaker		\
	ExtUtils-Command		\
	ExtUtils-CBuilder		\
	ExtUtils-ParseXS		\
	Test-Simple			\
	Version-Requirements		\
	JSON-PP				\
	File-Temp			\
	CPAN-Meta-YAML			\
	Parse-CPAN-Meta			\
	CPAN-Meta			\
	Perl-OSType			\
	Module-Metadata			\
	Module-Build			\
	Try-Tiny			\
	Exporter			\
	Test-Fatal			\
	Class-Load			\
	Class-Singleton			\
	parent				\
	DateTime-TimeZone		\
	DateTime-Locale			\
	Math-Round			\
	DateTime

PERL_PACKAGE_DIRECTORIES =		\
	Config-General-2.50		\
	POSIX-RT-Timer-0.009		\
	Sys-Syslog-0.29			\
	version-0.88			\
	Test-Harness-3.23		\
	Locale-Maketext-Simple-0.21	\
	Params-Check-0.28		\
	Module-CoreList-2.46		\
	Module-Load-0.18		\
	Module-Load-Conditional-0.44	\
	IPC-Cmd-0.70			\
	ExtUtils-Manifest-1.58		\
	ExtUtils-Install-1.54		\
	ExtUtils-MakeMaker-6.56		\
	ExtUtils-Command-1.17		\
	ExtUtils-CBuilder-0.280202	\
	ExtUtils-ParseXS-2.2206		\
	Test-Simple-0.98		\
	Version-Requirements-0.101020	\
	JSON-PP-2.27105			\
	File-Temp-0.22			\
	CPAN-Meta-YAML-0.003		\
	Parse-CPAN-Meta-1.4401		\
	CPAN-Meta-2.110930		\
	Perl-OSType-1.002		\
	Module-Metadata-1.000004	\
	Module-Build-0.3800		\
	Try-Tiny-0.09			\
	Exporter-5.63			\
	Test-Fatal-0.005		\
	Class-Load-0.06			\
	Class-Singleton-1.4		\
	parent-0.225			\
	DateTime-TimeZone-1.34		\
	DateTime-Locale-0.45		\
	Math-Round-0.06			\
	DateTime-0.70

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
# may be an alternative to setting PERL_MM_USE_DEFAULT.

Class-Load	: Class-Load-0.06.tar.gz
	tar xvfz Class-Load-0.06.tar.gz
	cd Class-Load-0.06;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

Class-Singleton	: Class-Singleton-1.4.tar.gz
	tar xvfz Class-Singleton-1.4.tar.gz
	cd Class-Singleton-1.4;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

Config-General	: Config-General-2.50.tar.gz
	tar xvfz Config-General-2.50.tar.gz
	cd Config-General-2.50;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install

CPAN-Meta	: CPAN-Meta-2.110930.tar.gz
	tar xvfz CPAN-Meta-2.110930.tar.gz
	cd CPAN-Meta-2.110930;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install

CPAN-Meta-YAML	: CPAN-Meta-YAML-0.003.tar.gz
	tar xvfz CPAN-Meta-YAML-0.003.tar.gz
	cd CPAN-Meta-YAML-0.003;				\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install

DateTime	: DateTime-0.70.tar.gz
	tar xvfz DateTime-0.70.tar.gz
	cd DateTime-0.70;					\
	$(PERL) $(PERL_INCLUDES) Build.PL $(PERL_INSTALL);	\
	./Build;						\
	./Build install --install_base $(PWD)/../perl		\
	    --install_path lib=$(PWD)/../perl/lib		\
	    --install_path arch=$(INSTALLSITEARCH)

DateTime-Locale	: DateTime-Locale-0.45.tar.gz
	tar xvfz DateTime-Locale-0.45.tar.gz
	cd DateTime-Locale-0.45;				\
	$(PERL) $(PERL_INCLUDES) Build.PL $(PERL_INSTALL);	\
	./Build;						\
	./Build install --install_base $(PWD)/../perl		\
	    --install_path lib=$(PWD)/../perl/lib		\
	    --install_path arch=$(INSTALLSITEARCH)

DateTime-TimeZone	: DateTime-TimeZone-1.34.tar.gz
	tar xvfz DateTime-TimeZone-1.34.tar.gz
	cd DateTime-TimeZone-1.34;				\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

Exporter	: Exporter-5.63.tar.gz
	tar xvfz Exporter-5.63.tar.gz
	cd Exporter-5.63;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

ExtUtils-CBuilder	: ExtUtils-CBuilder-0.280202.tar.gz
	tar xvfz ExtUtils-CBuilder-0.280202.tar.gz
	cd ExtUtils-CBuilder-0.280202;				\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install

ExtUtils-Command	: ExtUtils-Command-1.17.tar.gz
	tar xvfz ExtUtils-Command-1.17.tar.gz
	cd ExtUtils-Command-1.17;				\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install

ExtUtils-Install	: ExtUtils-Install-1.54.tar.gz
	tar xvfz ExtUtils-Install-1.54.tar.gz
	cd ExtUtils-Install-1.54;				\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install

ExtUtils-MakeMaker	: ExtUtils-MakeMaker-6.56.tar.gz
	tar xvfz ExtUtils-MakeMaker-6.56.tar.gz
	cd ExtUtils-MakeMaker-6.56;				\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install

ExtUtils-Manifest	: ExtUtils-Manifest-1.58.tar.gz
	tar xvfz ExtUtils-Manifest-1.58.tar.gz
	cd ExtUtils-Manifest-1.58;				\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install

ExtUtils-ParseXS	: ExtUtils-ParseXS-2.2206.tar.gz
	tar xvfz ExtUtils-ParseXS-2.2206.tar.gz
	cd ExtUtils-ParseXS-2.2206;				\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install

File-Temp	: File-Temp-0.22.tar.gz
	tar xvfz File-Temp-0.22.tar.gz
	cd File-Temp-0.22;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

IPC-Cmd	: IPC-Cmd-0.70.tar.gz
	tar xvfz IPC-Cmd-0.70.tar.gz
	cd IPC-Cmd-0.70;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install

JSON-PP	: JSON-PP-2.27105.tar.gz
	tar xvfz JSON-PP-2.27105.tar.gz
	cd JSON-PP-2.27105;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

Locale-Maketext-Simple	: Locale-Maketext-Simple-0.21.tar.gz
	tar xvfz Locale-Maketext-Simple-0.21.tar.gz
	cd Locale-Maketext-Simple-0.21;				\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install

Math-Round	: Math-Round-0.06.tar.gz
	tar xvfz Math-Round-0.06.tar.gz
	cd Math-Round-0.06;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

Module-Build	: Module-Build-0.3800.tar.gz
	tar xvfz Module-Build-0.3800.tar.gz
	cd Module-Build-0.3800;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

Module-CoreList	: Module-CoreList-2.46.tar.gz
	tar xvfz Module-CoreList-2.46.tar.gz
	cd Module-CoreList-2.46;				\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install

Module-Load	: Module-Load-0.18.tar.gz
	tar xvfz Module-Load-0.18.tar.gz
	cd Module-Load-0.18;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install

Module-Load-Conditional	: Module-Load-Conditional-0.44.tar.gz
	tar xvfz Module-Load-Conditional-0.44.tar.gz
	cd Module-Load-Conditional-0.44;			\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install

Module-Metadata	: Module-Metadata-1.000004.tar.gz
	tar xvfz Module-Metadata-1.000004.tar.gz
	cd Module-Metadata-1.000004;				\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install

Params-Check	: Params-Check-0.28.tar.gz
	tar xvfz Params-Check-0.28.tar.gz
	cd Params-Check-0.28;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install

Parse-CPAN-Meta	: Parse-CPAN-Meta-1.4401.tar.gz
	tar xvfz Parse-CPAN-Meta-1.4401.tar.gz
	cd Parse-CPAN-Meta-1.4401;				\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install

Perl-OSType	: Perl-OSType-1.002.tar.gz
	tar xvfz Perl-OSType-1.002.tar.gz
	cd Perl-OSType-1.002;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

POSIX-RT-Timer	: POSIX-RT-Timer-0.009.tar.gz
	tar xvfz POSIX-RT-Timer-0.009.tar.gz
	cd POSIX-RT-Timer-0.009;				\
	$(PERL) $(PERL_INCLUDES) Build.PL $(PERL_INSTALL);	\
	./Build;						\
	./Build install --install_base $(PWD)/../perl		\
	    --install_path lib=$(PWD)/../perl/lib		\
	    --install_path arch=$(INSTALLSITEARCH)

Sys-Syslog	: Sys-Syslog-0.29.tar.gz
	tar xvfz Sys-Syslog-0.29.tar.gz
	cd Sys-Syslog-0.29;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

Test-Fatal	: Test-Fatal-0.005.tar.gz
	tar xvfz Test-Fatal-0.005.tar.gz
	cd Test-Fatal-0.005;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

Test-Harness	: Test-Harness-3.23.tar.gz
	tar xvfz Test-Harness-3.23.tar.gz
	cd Test-Harness-3.23;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install

Test-Simple	: Test-Simple-0.98.tar.gz
	tar xvfz Test-Simple-0.98.tar.gz
	cd Test-Simple-0.98;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

Try-Tiny	: Try-Tiny-0.09.tar.gz
	tar xvfz Try-Tiny-0.09.tar.gz
	cd Try-Tiny-0.09;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

Version-Requirements	: Version-Requirements-0.101020.tar.gz
	tar xvfz Version-Requirements-0.101020.tar.gz
	cd Version-Requirements-0.101020;			\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

parent	: parent-0.225.tar.gz
	tar xvfz parent-0.225.tar.gz
	cd parent-0.225;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

version	: version-0.88.tar.gz
	tar xvfz version-0.88.tar.gz
	cd version-0.88;					\
	$(PERL) $(PERL_INCLUDES) Makefile.PL $(PERL_INSTALL);	\
	make;							\
	make install 

clean	:
	rm -rf $(PERL_PACKAGE_DIRECTORIES)

distclean	: clean
	rm -rf ../perl

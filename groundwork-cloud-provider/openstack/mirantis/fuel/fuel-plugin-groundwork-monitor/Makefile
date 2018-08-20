# Top-level Makefile for the fuel-plugin-groundwork-monitor distribution.

#
# Copyright 2016-2017 GroundWork Open Source, Inc. (GroundWork)
# All rights reserved. This program is free software; you can redistribute
# it and/or modify it under the terms of the GNU General Public License
# version 2 as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
#

# WHAT TO DO FOR A NEW GROUNDWORK RELEASE:
#
# (*) Update the GENUINE_FILENAME value below in this Makefile.
# (*) Carefully and thoroughly read the ./README.md and ./doc/source/*.rst
#     files, and update them as needed to reflect changes in the new software
#     and in references to GroundWork-site web pages.  Pay special attention
#     to all pathnames and other indications of the release number.
# (*) Review the various Fuel plugin config files (./*.yaml), and make manual
#     changes as needed.  It's quite likely you won't need to change anything
#     at all.  GroundWork-related version numbers in these files will be
#     automatically handled by the build scripting.
# (*) Remove the old GroundWork installer from the deployment_scripts/
#     subdirectory, so it will no longer be part of this plugin.
# (*) Obtain a copy of the new GroundWork installer and place it into the
#     deployment_scripts/ subdirectory so "make checksums" and "make plugin"
#     can find it.
# (*) Type "make checksums" to calculate the proper checksums for validating
#     the new release.
# (*) Edit this Makefile and replace the existing checksum lines with those
#     produced by the previous step.
# (*) Type "make clean plugin".
# (*) You will find the resulting RPM file here in this directory.
# (*) Transfer the RPM to a Fuel Master machine, and install it there.  See
#     instructions in the ./README.md file for details on managing the plugin
#     on the Fuel Master.
# (*) Run some deployments.  Maybe even do so in a formal manner, following
#     an official test plan.

# FIX MINOR:  Update the instructions above to reflect possible changes in the
# test plan and test report.

# This is where the GroundWork installer and other deployment scripts live,
# both here in the source tree and within the installed RPM.  It is NOT,
# however, used on deployed OpenStack client machines.
DEPLOYMENT_SUBDIRECTORY = deployment_scripts

# The GENUINE_PATHNAME path is where you need to drop a copy of the GroundWork
# installer in order to calculate checksums.  It won't be included in the RPM,
# though, simply because you placed in there; we construct the RPM to explicitly
# exclude it.  If the plugin is somehow installed on the Fuel master and the
# GroundWork installer never makes it there, the deployment scripting instead
# runs an installer emulator that will essentially behave as an installer
# (accept a few command-line arguments, emit a bit of output to the standard
# output stream, and exit with a zero exit code) and allow you to test all
# other aspects of deploying this Fuel plugin.
#
GENUINE_FILENAME = groundworkenterprise-7.1.1-br415-gw3089-linux-64-installer.run
STANDIN_FILENAME = groundwork-installer-emulator
GENUINE_PATHNAME = $(DEPLOYMENT_SUBDIRECTORY)/$(GENUINE_FILENAME)
STANDIN_PATHNAME = $(DEPLOYMENT_SUBDIRECTORY)/$(STANDIN_FILENAME)

# These definitions all form a group.  Run "make checksums" to generate these
# values once the chosen GENUINE_FILENAME value is set above, to generate and
# print the coresponding checksums.  Then edit those generated values into the
# assignments here in the Makefile.
#
GENUINE_MD5SUM    = a1a68a525cbd37e94b7fbe9b4fa21092
GENUINE_SHA1SUM   = acd38e6adb18199691f8eb51938b08b85be07ac6
GENUINE_SHA256SUM = dba3267cab47f22d95e13e90c4695dadc6c0c332f1d172c7b9bfdd786e822ec0
#
STANDIN_MD5SUM    = 676b679de3026de4b74cd0ff9e579ba8
STANDIN_SHA1SUM   = 202165b2fdbdd7e4c6bd0ac88d9151871222c0d9
STANDIN_SHA256SUM = 8e9e1652c8b80fc3d92ac7cbf8589ff6dac4e39509e902fae56b6209cfb36642

# VERIFY_CHECKSUM must be defined as "none", "md5", "sha1", or "sha256".
# It specifies the plugin actions at both install time and deployment time
# in terms of verifying one of the given checksums for the installer.
# md5 is known to be subject to collision attacks, so that's not secure
# enough for production use here.  sha1 is also somewhat weak in the same
# regard, against a well-funded opponent.  Testing shows that sha256 is
# not very much more expensive to compute (and it's far less time-consuming
# than reading the file from disk in the first place), so we use that.
VERIFY_CHECKSUM = sha256

# The ABORT_IF_INSTALLER_MISSING boolean flag determines what to do if we
# don't find the official GroundWork installer at plugin install time.  We
# would like this value to reflect whether or not the official GroundWork
# installer was present in the DEPLOYMENT_SUBDIRECTORY at plugin RPM build
# time (1 if present, 0 if not).  I'd like to do this all in a one-liner,
# but that doesn't seem to be the standard way to test for file presence.
ifeq ("$(wildcard $(GENUINE_PATHNAME))","")
    # The GroundWork installer does not exist.
    ABORT_IF_INSTALLER_MISSING = 0
else
    # The GroundWork installer exists.
    ABORT_IF_INSTALLER_MISSING = 1
endif

# Calculated values that we'll use later on.
INSTALLER_PATCH := $(shell expr match $(GENUINE_FILENAME) '[^-]*-\([^-]*\)-' )
INSTALLER_MINOR := $(basename $(INSTALLER_PATCH))

# The INSTALLER_DIRECTORY is the location on each deployment target machine
# where the Fuel software directly copies all the software that lives under
# $(PACKAGE_DIRECTORY)/$(DEPLOYMENT_SUBDIRECTORY) on the Fuel Master.
# This path is essentially defined by the Fuel software, not something
# arbitrarily set here.
INSTALLER_DIRECTORY = /etc/fuel/plugins/fuel-plugin-groundwork-monitor-$(INSTALLER_MINOR)

# You can define RPMBUILD_VERBOSITY as "--quiet", a completely undefined
# value (not a quoted empty string), "-v", or "-vv" to print successively
# more detail during an explicit "rpmbuild" command below.
RPMBUILD_VERBOSITY = --quiet

# Potential build targets.
NOARCH_RPM	= fuel-plugin-groundwork-monitor-$(INSTALLER_MINOR)-$(INSTALLER_PATCH)-1.noarch.rpm
PLATFORM_RPM	= fuel-plugin-groundwork-monitor-$(INSTALLER_MINOR)-$(INSTALLER_PATCH)-1.x86_64.rpm
NOARCH_DEB	= fuel-plugin-groundwork-monitor-$(INSTALLER_MINOR)_$(INSTALLER_PATCH)-1_all.deb
PLATFORM_DEB	= fuel-plugin-groundwork-monitor-$(INSTALLER_MINOR)_$(INSTALLER_PATCH)-1_amd64.deb

# Where "fpb --build ." drops the RPM specfile that it generates.
SPECFILE	= .build/rpm/plugin_rpm.spec

# Name of the PDF file generated by "make pdf".  This just records the value used
# by the lower-level document-formatting scripting; it's not set arbitrarily here.
DOC_PDF		= fuel-plugin-groundwork-monitor.pdf

# This must be the directory where the $(SPECFILE) built by "fpb" says the software
# will be installed when the generated RPM is intalled on the Fuel Master.
PACKAGE_DIRECTORY = /var/www/nailgun/plugins/fuel-plugin-groundwork-monitor-7.1

# To understand the execution of the embedded scripts in our package, we ran some
# tests to spill out the location where those scripts are placed in the filesystem
# before execution.  Here are the script paths used by Ubuntu 16.04:
#
# === The          %pre script path is:  /var/lib/dpkg/tmp.ci/preinst
# === The         %post script path is:  /var/lib/dpkg/info/fuel-plugin-groundwork-monitor-7.1.postinst
# === The        %preun script path is:  /var/lib/dpkg/info/fuel-plugin-groundwork-monitor-7.1.prerm
# === The       %postun script path is:  /var/lib/dpkg/info/fuel-plugin-groundwork-monitor-7.1.postrm
# === The       %postun script path is:  /var/lib/dpkg/info/fuel-plugin-groundwork-monitor-7.1.postrm
#
# I don't know how dkp on Ubuntu ensures the the /var/lib/dpkg/tmp.ci/ directory
# is not shared with some other package being concurrently installed.
#
# Note that the %postun script gets called twice when "dpkg -P" is invoked:
# once when removing the package as a whole. and once again when purging
# configuration files.  The script path is the same in both cases.
#
# Why isn't the %verifyscript path listed?  That's partly because the %verifyscript
# script is not included in the package when we convert it from a .rpm file to a .deb
# file using "fakeroot alien -c -k $(NOARCH_RPM)", at least through alien version 8.95.
# (The relevant code can be found here on Unbuntu:
#     /usr/share/perl5/Alien/Package/Rpm.pm
# if you want to experiment with adding such a capability to "alien".)  That can be
# shown afterward using this command:
#     dpkg-deb -e fuel-plugin-groundwork-monitor-7.1_7.1.0-1_all.deb
# which pulls out all the scripting in places it into a DEBIAN/ subdirectory.
# The point is, without that script included in the .deb file, "dpkg -V" has
# no chance of running it, whether or not it knows about this type of script.
# But in fact, dpkg doesn't have any support for running a package-provided
# %verifyscript or equivalent when "dpkg -V" is run.  I have verified that in
# the dpkg 1.18.7 source code.  So there is no way we can depend on having
# such a script available for our use; all validation checking must instead
# occur when the plugin is installed and when it is deployed.
#
# Here are the script paths used by CentOS 5.11:
#
# === The          %pre script path is:  /var/tmp/rpm-tmp.83429
# === The         %post script path is:  /var/tmp/rpm-tmp.83368
# === The        %preun script path is:  /var/tmp/rpm-tmp.24735
# === The       %postun script path is:  /var/tmp/rpm-tmp.24735
# === The %verifyscript script path is:  /var/tmp/rpm-tmp.16563
#
# Those numbers at the ends of these paths are presumably process IDs, so there
# will be no collisions with other packages being concurrently operated upon.
#
# In all cases on both platforms, the script working directory is always "/".
#
# The point of these tests is that none of these primitive signals give the
# package-provided script any idea where the package .rpm or .deb file is
# located.  And the RPM documentation says that the arguments passed to the
# scripts provide no further clues.  So we have no way to know where the
# GroundWork installer is located when the package is installed, and we'll
# have to insist that the end-user plant it in some fixed directory like
# /tmp so our install scripts know where to look for it.  Our uninstall
# scripts do know where to look, since the package installation directory
# is fixed (see the PACKAGE_DIRECTORY definition above).  And likewise, our
# deployment scripting knows where to look.

default		: help

help		:
		@echo
		@echo "You probably want to run this command to produce the final RPM:"
		@echo
		@echo "    make clean plugin"
		@echo
		@echo "The following make targets are available:"
		@echo
		@echo "    make version     prints the GroundWork installer version data"
		@echo "    make check       validates the Fuel plugin code"
		@echo "    make pdf         creates a PDF of the plugin documentation"
		@echo "    make checksums   calculates checksums for insertion into this Makefile"
		@echo "    make plugin      creates the Fuel plugin in standard form"
		@echo "    make rpm         synonym for:  make plugin"
		@echo "    make deb         creates a Debian/Ubuntu package for the Fuel plugin"
		@echo "                     (This is for limited local development testing outside of"
		@echo "                     an actual Fuel plugin deployment context.  Installing"
		@echo "                     the resulting package will not run deployment scripting.)"
		@echo "    make clean       cleans up all build files"
		@echo "    make distclean   cleans up everything back to the original checkout;"
		@echo "                     most specifically, that includes deleting the local"
		@echo "                     copy of the GroundWork Monitor BitRock installer"
		@echo
		@echo 'You can make the HTML documentation with:'
		@echo
		@echo '    cd doc/source; make clean html'
		@echo
		@echo 'and the formatted files will then be found in the doc/source/_build/html/'
		@echo 'directory.  However, that processing alone does not update certain variables'
		@echo 'in the documentation that depend on the current GroundWork installer.  Those'
		@echo 'edits are made automatically when you "make plugin", which also internally'
		@echo 'forces the HTML documentation to be re-built after those edits are in place.'
		@echo

version		:
		@echo INSTALLER_PATCH = $(INSTALLER_PATCH)
		@echo INSTALLER_MINOR = $(INSTALLER_MINOR)

check		:
		fpb --check .

pdf		:
		cd doc/source; make latexpdf
		cp -p doc/source/_build/latex/$(DOC_PDF) .

# Here are times to compute the checksums on the real installer, run on my laptop
# with a slow disk when the installer has already been read by a previous checksum
# calculation and cached in memory, and the CPU operating at 2.0 GHz:
#
#    md5sum:  2.387u 0.445s 0:02.89 97.5%
#   sha1sum:  3.679u 0.426s 0:04.17 98.0%
# sha256sum:  8.379u 0.427s 0:08.97 97.9%
#
# To those calculations must be added the time to read the data from disk.  Again
# on my slow laptop, such a run of md5 takes 0:32.47, during which the CPU meter
# was nowhere near pinned.  So this calculation is definitely normally i/o bound.
# And this is a 827,497,515-byte file.  So a sha256sum is definitely within reason
# as long as that program is installed on the machine.
#
# On both Ubuntu and CentOS platforms, the "md5sum", "sha1sum", and "sha256sum"
# programs are all provided by the coreutils package.  coreutils also provides
# most of the standard GNU utilities like "ls", so it is impossible for it not
# to be installed on the machine.  Hence we should have no hesitation in calling
# any of these programs for verification purposes.
#
# FIX LATER:  We could potentially add the file sizes to the set of checks that
# we run both here and in "make rpm" processing.
#
checksums	: $(STANDIN_PATHNAME)
		@echo
		@echo "Replace the following lines in the Makefile with these values: "
		@echo
		@if [ -f $(GENUINE_PATHNAME) ]; then							\
		    echo "GENUINE_MD5SUM    = ` md5sum    -b $(GENUINE_PATHNAME) | sed -e 's/ .*//' `";	\
		    echo "GENUINE_SHA1SUM   = ` sha1sum   -b $(GENUINE_PATHNAME) | sed -e 's/ .*//' `";	\
		    echo "GENUINE_SHA256SUM = ` sha256sum -b $(GENUINE_PATHNAME) | sed -e 's/ .*//' `";	\
		    echo "#";										\
		fi
		@echo "STANDIN_MD5SUM    = ` md5sum    -b $(STANDIN_PATHNAME) | sed -e 's/ .*//' `"
		@echo "STANDIN_SHA1SUM   = ` sha1sum   -b $(STANDIN_PATHNAME) | sed -e 's/ .*//' `"
		@echo "STANDIN_SHA256SUM = ` sha256sum -b $(STANDIN_PATHNAME) | sed -e 's/ .*//' `"
		@echo

plugin		: rpm

rpm		:
		# Automatically substitute certain parameters into the deployment
		# scripting and config files, so we have only one place (the top
		# of this Makefile) to modify said values for a new release.

		# Before we substitute the checksums in this Makefile into other files, check them
		# against the current respective source files to make sure they are up-to-date.
		#
		if [ -f $(GENUINE_PATHNAME) ]; then									\
		    if [ "`md5sum -b $(GENUINE_PATHNAME) | sed -e 's/ .*//'`" != "$(GENUINE_MD5SUM)" ]; then		\
			echo "ERROR:  The GroundWork installer fails md5 checksum verification.";			\
			false;												\
		    fi;													\
		    if [ "`sha1sum -b $(GENUINE_PATHNAME) | sed -e 's/ .*//'`" != "$(GENUINE_SHA1SUM)" ]; then		\
			echo "ERROR:  The GroundWork installer fails sha1 checksum verification.";			\
			false;												\
		    fi;													\
		    if [ "`sha256sum -b $(GENUINE_PATHNAME) | sed -e 's/ .*//'`" != "$(GENUINE_SHA256SUM)" ]; then	\
			echo "ERROR:  The GroundWork installer fails sha256 checksum verification.";			\
			false;												\
		    fi;													\
		fi
		if [ "`md5sum -b $(STANDIN_PATHNAME) | sed -e 's/ .*//'`" != "$(STANDIN_MD5SUM)" ]; then	\
		    echo "ERROR:  The GroundWork emulator fails md5 checksum verification.";			\
		    false;											\
		fi
		if [ "`sha1sum -b $(STANDIN_PATHNAME) | sed -e 's/ .*//'`" != "$(STANDIN_SHA1SUM)" ]; then	\
		    echo "ERROR:  The GroundWork emulator fails sha1 checksum verification.";			\
		    false;											\
		fi
		if [ "`sha256sum -b $(STANDIN_PATHNAME) | sed -e 's/ .*//'`" != "$(STANDIN_SHA256SUM)" ]; then	\
		    echo "ERROR:  The GroundWork emulator fails sha256 checksum verification.";			\
		    false;											\
		fi

		sed -i \
		    -e "s:GENUINE_FILENAME='.*':GENUINE_FILENAME='$(GENUINE_FILENAME)':"				\
		    -e "s:ABORT_IF_INSTALLER_MISSING='.*':ABORT_IF_INSTALLER_MISSING='$(ABORT_IF_INSTALLER_MISSING)':"	\
		    rpm_scripts/pre_install.sh

		sed -i \
		    -e "s:DEPLOYMENT_SUBDIRECTORY='.*':DEPLOYMENT_SUBDIRECTORY='$(DEPLOYMENT_SUBDIRECTORY)':"		\
		    -e "s:PACKAGE_DIRECTORY='.*':PACKAGE_DIRECTORY='$(PACKAGE_DIRECTORY)':"				\
		    -e "s:GENUINE_FILENAME='.*':GENUINE_FILENAME='$(GENUINE_FILENAME)':"				\
		    -e "s:GENUINE_PATHNAME='.*':GENUINE_PATHNAME='$(GENUINE_PATHNAME)':"				\
		    -e "s:GENUINE_MD5SUM='.*':GENUINE_MD5SUM='$(GENUINE_MD5SUM)':"					\
		    -e "s:GENUINE_SHA1SUM='.*':GENUINE_SHA1SUM='$(GENUINE_SHA1SUM)':"					\
		    -e "s:GENUINE_SHA256SUM='.*':GENUINE_SHA256SUM='$(GENUINE_SHA256SUM)':"				\
		    -e "s:VERIFY_CHECKSUM='.*':VERIFY_CHECKSUM='$(VERIFY_CHECKSUM)':"					\
		    -e "s:VERIFY_CHECKSUM='.*':VERIFY_CHECKSUM='$(VERIFY_CHECKSUM)':"					\
		    -e "s:ABORT_IF_INSTALLER_MISSING='.*':ABORT_IF_INSTALLER_MISSING='$(ABORT_IF_INSTALLER_MISSING)':"	\
		    rpm_scripts/post_install.sh

		sed -i \
		    -e "s:PACKAGE_DIRECTORY='.*':PACKAGE_DIRECTORY='$(PACKAGE_DIRECTORY)':"	\
		    -e "s:GENUINE_PATHNAME='.*':GENUINE_PATHNAME='$(GENUINE_PATHNAME)':"	\
		    rpm_scripts/pre_uninstall.sh

		sed -i \
		    -e "s:PACKAGE_DIRECTORY='.*':PACKAGE_DIRECTORY='$(PACKAGE_DIRECTORY)':"	\
		    -e "s:GENUINE_PATHNAME='.*':GENUINE_PATHNAME='$(GENUINE_PATHNAME)':"	\
		    -e "s:STANDIN_PATHNAME='.*':STANDIN_PATHNAME='$(STANDIN_PATHNAME)':"	\
		    -e "s:GENUINE_MD5SUM='.*':GENUINE_MD5SUM='$(GENUINE_MD5SUM)':"		\
		    -e "s:GENUINE_SHA1SUM='.*':GENUINE_SHA1SUM='$(GENUINE_SHA1SUM)':"		\
		    -e "s:GENUINE_SHA256SUM='.*':GENUINE_SHA256SUM='$(GENUINE_SHA256SUM)':"	\
		    -e "s:STANDIN_MD5SUM='.*':STANDIN_MD5SUM='$(STANDIN_MD5SUM)':"		\
		    -e "s:STANDIN_SHA1SUM='.*':STANDIN_SHA1SUM='$(STANDIN_SHA1SUM)':"		\
		    -e "s:STANDIN_SHA256SUM='.*':STANDIN_SHA256SUM='$(STANDIN_SHA256SUM)':"	\
		    -e "s:VERIFY_CHECKSUM='.*':VERIFY_CHECKSUM='$(VERIFY_CHECKSUM)':"		\
		    rpm_scripts/verifyscript.sh

		sed -i \
		    -e "s:INSTALLER_DIRECTORY='.*':INSTALLER_DIRECTORY='$(INSTALLER_DIRECTORY)':"	\
		    -e "s:GENUINE_FILENAME='.*':GENUINE_FILENAME='$(GENUINE_FILENAME)':"		\
		    -e "s:STANDIN_FILENAME='.*':STANDIN_FILENAME='$(STANDIN_FILENAME)':"		\
		    -e "s:GENUINE_MD5SUM='.*':GENUINE_MD5SUM='$(GENUINE_MD5SUM)':"			\
		    -e "s:GENUINE_SHA1SUM='.*':GENUINE_SHA1SUM='$(GENUINE_SHA1SUM)':"			\
		    -e "s:GENUINE_SHA256SUM='.*':GENUINE_SHA256SUM='$(GENUINE_SHA256SUM)':"		\
		    -e "s:STANDIN_MD5SUM='.*':STANDIN_MD5SUM='$(STANDIN_MD5SUM)':"			\
		    -e "s:STANDIN_SHA1SUM='.*':STANDIN_SHA1SUM='$(STANDIN_SHA1SUM)':"			\
		    -e "s:STANDIN_SHA256SUM='.*':STANDIN_SHA256SUM='$(STANDIN_SHA256SUM)':"		\
		    -e "s:VERIFY_CHECKSUM='.*':VERIFY_CHECKSUM='$(VERIFY_CHECKSUM)':"			\
		    $(DEPLOYMENT_SUBDIRECTORY)/deploy.sh

		sed -i \
		    -e "s%^version:.*%version: $(INSTALLER_PATCH)%"	\
		    metadata.yaml

		# Most documentation-file version-number-related adjustments will need
		# to be handled manually, given that they are embedded in readable text.
		# But a couple of definitions make sense to handle automatically, so
		# they can then be used as |macro| references within the documentation
		# where that is appropriate.  The files not handled by this automatic
		# replacement are the ./README.md file in this directory, and external
		# document references in certain ./doc/source/*.rst files.
		#
		# These substitutions would make sense if the documentation used a
		# version/release format that was understandable to an ordinary user
		# without a Rosetta Stone immediately at hand.  That being not the
		# case, the code below uses a similar but different construction.
		#    -e "s:^version = .*:version = u'$(INSTALLER_MINOR)':"	\
		#    -e "s:^release = .*:release = u'$(INSTALLER_PATCH)':"	\
		#
		sed -i \
		    -e "s:^version = .*:version = u'$(INSTALLER_PATCH)':"	\
		    doc/source/conf.py

		sed -i \
		    -e 's%|groundwork_installer| replace:: .*%|groundwork_installer| replace:: $(GENUINE_FILENAME)%'	\
		    -e 's%|groundwork_rpm| replace:: .*%|groundwork_rpm| replace:: $(NOARCH_RPM)%'			\
		    doc/source/definitions.txt

		# This not only makes the HTML documentation (which will then be part of the RPM,
		# assuming that's what we want), it also cleans up any files left over from a
		# previous "cd doc/source; make latexpdf" run, if any, so they are not present
		# in the RPM.
		cd doc/source; make clean html

		# Now we go through a process of creating an RPM that contains our own
		# %pre, %post, %preun, %postun, and %verifyscript scripts.  To do that,
		# we first need to execute a dummy run of "fpb", enough for it to generate
		# the specfile to which we will add our extra RPM scripting.
		#
		# Note that the "rpmbuild" command internally run by "fpb" will fail if we
		# have "Arch dependent binaries in noarch package", which will be the case
		# if we have the GroundWork installer parked in the deployment_scripts/
		# subdirectory when we execute this step.  It's convenient to park it there
		# to make it accessible for calculating and verifying checksums, but now it
		# causes a build disruption.  The only way around this is to suppress the
		# "BuildArch:" line in the generated $(SPECFILE), but "fpb" does not provide
		# any hook for us to intercede to do so in the middle of its processing.
		# One possibility would be to modify the copy of the plugin_rpm.spec file
		# that "fpb" uses as a template, for the duration of this build.  That file
		# resides here on my system:
		#
		# /home/anon/.local/lib/python2.7/site-packages/fuel_plugin_builder/templates/v3/build/plugin_rpm.spec.mako
		#
		# However, performing a temporary modification on that file seems like an
		# ugly solution.  Better would be if we could point "fpb" to a temporary
		# copy which we modify solely for purposes of this build.

		# Delete any existing copy of the specfile, so we can be assured that the
		# copy we see after the "fpb --build" run is really one that it just created.
		rm -f $(SPECFILE)

		@# The fpb command will say "Plugin is built", but that is misleading at this
		@# stage.  So we prefix that message with an contextual message of our own.
		@echo ""
		@echo "Creating a transient copy of the plugin for build purposes ..."
		#
		# We intentionally ignore error codes from the "fpb --build" because if
		# we have the GroundWork installer parked in the deployment_scripts/
		# subdirectory to make it easy to compute checksums, that will violate
		# the "BuildArch: noarch" specification in the generated specfile.
		-fpb --build .

		# Check to make sure that we have a specfile to operate on now.
		[ -f $(SPECFILE) ]

		# Insert the %postun and %verifyscript directives into the specfile, because
		# they are not there in fpb's generated specfile.  This tricky formulation of
		# newline handling comes from the GNU autoconf documentation, in the section
		# "Newlines in Make Rules".  It's a bit ugly, but all the other possible hacks
		# are just as bad.

		nlinit=`echo 'nl="'; echo '"'`; eval "$$nlinit"; \
		sed -i -e "s/^%files$$/%postun\\$${nl}\\$${nl}\\$${nl}%files/" $(SPECFILE)

		nlinit=`echo 'nl="'; echo '"'`; eval "$$nlinit"; \
		sed -i -e "s/^%files$$/%verifyscript\\$${nl}\\$${nl}\\$${nl}%files/" $(SPECFILE)

		# Zipper our various scripts into the corresponding directive sections in the
		# specfile.  Note that in the version of fpb that we are running (which seems
		# to have no obvious associated release/version number), the following files
		# in the current directory, if present, are already substituted in when the
		# specfile is constructed by "fpb --build".
		#
		#     pre_install.sh   (for %pre work)
		#     post_install.sh  (for %post work)
		#     uninstall.sh     (for %preun work)
		#
		# But we have the other scripts to handle ourselves, so it's best to
		# just handle them all in a uniform fashion.
		sed -i								\
		    -e '/^%pre$$/          r rpm_scripts/pre_install.sh'	\
		    -e '/^%post$$/         r rpm_scripts/post_install.sh'	\
		    -e '/^%preun$$/        r rpm_scripts/pre_uninstall.sh'	\
		    -e '/^%postun$$/       r rpm_scripts/post_uninstall.sh'	\
		    -e '/^%verifyscript$$/ r rpm_scripts/verifyscript.sh'	\
		    $(SPECFILE)

		# We need to have easy access to the GENUINE_FILENAME file somewhere to calculate
		# checksums, but it can't be parked in this file tree unless we have some means
		# to exclude it from the RPM.  We do so now, in addition to excluding any other
		# build artifacts, source-code files, test docs, and other stuff not intended
		# for distribution in the RPM.  This processing is obviously not perfect, as it
		# only excludes objects we know about in advance.  Still, it handles all the
		# usual suspects.
		#
		# All paths we specify in an %exclude directive must be absolute pathnames,
		# based on where these paths would reside in the installed plugin, not on where
		# they reside in this source filesystem.  Hence the use of the $(PACKAGE_DIRECTORY)
		# prefix in all of these lines.
		#
		# rpmbuild limits you to one filename (or perhaps glob pattern) on each
		# %exclude line.  So we append multiple %exclude lines to the specfile here.
		#
		# For what it's worth, the "fpb --build" process seems to already remove any
		# .rpm files (explicitly from the filesystem, not just from the newly generated
		# RPM), so we don't actually need to declare those here.  We do so anyway, for
		# simple completeness; .deb files do need explicit exclusion here.
		#
		# We exclude the "specs" file tree because it contains some boilerplate templates
		# which have not been customized to date for this plugin, and because it may also
		# contain the LibreOffice .odt source files for the Test Plan and Test Report
		# for this plugin.  In particular, the Test Report contains the md5 checksum of
		# the Fuel plugin, which cannot be calculated in advance of actually building the
		# plugin.  So the plugin itself cannot contain such Test documents.
		#
		# Filenames starting with a comma are a convenient convention for tagging as
		# being of a temporary nature.  Such files may be usefully created during
		# development activities, but then might be forgotten about and inadvertently
		# included in the RPM.  So we exclude them as well.  (We'd really prefer some
		# glob pattern that would exclude such files at all levels of the file tree,
		# but I don't know of a simple pattern that would do so.)
		#
		# That all said, we have found a problem with using the %exclude directive, and
		# it's not of our own making.  rpmbuild before version 4.9.1.2-6 treats a file
		# or glob pattern mentioned in an %exclude directive as an error instead of a
		# warning if that file or glob does not exist.  This is supremely silly, and it
		# did eventually get fixed in the rpmbuild program.  But sometimes, we might
		# desire to build this Fuel plugin using an earlier version of rpmbuild, such as
		# the version 4.8.0 that is the last version available on CentOS 6.7.  Having the
		# %exclude directives be treated as errors causes the entire rpmbuild activity to
		# fail, and we don't get an RPM file out.
		#
		# The upshot of that bug is that we need to work around it here, by using some
		# other mechanism to ensure that the files we desire not to be in the build do
		# not make it in.  It seems like the way to do that is to edit the middle of the
		# $(SPECFILE), in the %install section, removing any such files that get copied
		# to the buildroot.  Unfortunately, cleanly appending lines to some particular
		# spot in the middle of a file is extremely tricky, when executed in the context
		# of a Makefile.  We have to be extraordinarily careful not to entangle the use
		# of backslashes to concatenate consecutive lines in the Makefile recipe with
		# whatever punctuation is needed by the specific command we want to execute, for
		# it to recognize a long "thing" to process.  (That extra care explains why we
		# really didn't want to take this route in the first place.)  After many, many
		# experiments, we finally found a semi-reasonable construction, using Perl.
		#
		if false; then										\
		    echo "%exclude $(PACKAGE_DIRECTORY)/$(GENUINE_PATHNAME)" >> $(SPECFILE);		\
		    echo "%exclude $(PACKAGE_DIRECTORY)/$(DOC_PDF)"          >> $(SPECFILE);		\
		    echo "%exclude $(PACKAGE_DIRECTORY)/$(NOARCH_RPM)"       >> $(SPECFILE);		\
		    echo "%exclude $(PACKAGE_DIRECTORY)/$(PLATFORM_RPM)"     >> $(SPECFILE);		\
		    echo "%exclude $(PACKAGE_DIRECTORY)/$(NOARCH_DEB)"       >> $(SPECFILE);		\
		    echo "%exclude $(PACKAGE_DIRECTORY)/$(PLATFORM_DEB)"     >> $(SPECFILE);		\
		    echo "%exclude $(PACKAGE_DIRECTORY)/specs"               >> $(SPECFILE);		\
		    echo "%exclude $(PACKAGE_DIRECTORY)/,*"                  >> $(SPECFILE);		\
		else											\
		    perl -pli										\
			-e 'if (/cp.*buildroot/) {'							\
			-e '    print; $$_ = "";'							\
			-e '    print "rm -f  %{buildroot}$(PACKAGE_DIRECTORY)/$(GENUINE_PATHNAME)";'	\
			-e '    print "rm -f  %{buildroot}$(PACKAGE_DIRECTORY)/$(DOC_PDF)";'		\
			-e '    print "rm -f  %{buildroot}$(PACKAGE_DIRECTORY)/$(NOARCH_RPM)";'		\
			-e '    print "rm -f  %{buildroot}$(PACKAGE_DIRECTORY)/$(PLATFORM_RPM)";'	\
			-e '    print "rm -f  %{buildroot}$(PACKAGE_DIRECTORY)/$(NOARCH_DEB)";'		\
			-e '    print "rm -f  %{buildroot}$(PACKAGE_DIRECTORY)/$(PLATFORM_DEB)";'	\
			-e '    print "rm -fr %{buildroot}$(PACKAGE_DIRECTORY)/specs";'			\
			-e '    print "rm -fr %{buildroot}$(PACKAGE_DIRECTORY)/,*";'			\
		        -e '}'										\
			$(SPECFILE);									\
		fi

		# Finally, build the permanent copy of the desired RPM.
		rpmbuild $(RPMBUILD_VERBOSITY) --nodeps		\
		    --define "_topdir $(PWD)/.build/rpm"	\
		    --define "__os_install_post %{nil}"		\
		    -bb $(PWD)/$(SPECFILE)
		mv .build/rpm/RPMS/noarch/$(NOARCH_RPM) .

		@echo ""
		@echo "INFO:  The Fuel plugin noarch build is complete."

deb		: rpm
		if [ -f $(NOARCH_RPM) ]; then					\
		    fakeroot alien -c -k $(NOARCH_RPM) ;			\
		elif [ -f $(PLATFORM_RPM) ]; then				\
		    fakeroot alien -c -k $(PLATFORM_RPM) ;			\
		else								\
		    echo "ERROR:  Cannot find an RPM file to convert." ;	\
		    false ;							\
		fi

clean		:
		cd doc/source; make clean
		rm -rf .build
		rm -f $(NOARCH_RPM) $(PLATFORM_RPM)
		rm -f $(NOARCH_DEB) $(PLATFORM_DEB)
		rm -f $(DOC_PDF)

distclean	: clean
		# We keep around the GroundWork installer for an ordinary clean operation,
		# but to get back to Ground Zero, it has to go too.
		rm -f $(GENUINE_PATHNAME)

# Undocumented target for development work.
tarball		: distclean
		cd .. && tar cfz fuel-plugin-groundwork-monitor.tar.gz --exclude=.git fuel-plugin-groundwork-monitor

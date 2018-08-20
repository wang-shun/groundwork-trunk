#
# Makefile for building Nagios 4.2.4 in the GroundWork context,
# so that it can work with the Bronx event broker
#
# Copyright (c) 2017 GroundWork Open Source, Inc.  All rights reserved.
#

# Given that we have Bronx run as an independent thread within the main Nagios
# process, we must take certain steps in the compilation of Nagios to ensure that
# it compiles in re-entrant versions of system routines, uses the FD_CLOEXEC flag
# on certain file descriptors, handles signals in a threadsafe manner, and so
# forth.  Some of this used to be handled in Nagios 3.X, but now in Nagios 4.X,
# the main Nagios process is supposed to be single-threaded.  So we can no longer
# count on it doing all of these things right, from our perspective.
#
# We prepare Nagios for multi-threaded compilation via a CFLAGS option (set
# below).  This compensates for _REENTRANT no longer being explicitly defined
# in the Nagios include/common.h header; it will be implicitly defined instead.
# Also, we forcibly inject an "#include <pthread.h>" preprocessor statement in a
# place similar to where it used to live in Nagios 3.X releases, to ensure that
# the necessary declarations are available when needed.
#
# Nagios 4.X does set FD_CLOEXEC in many places, in fact in more places than
# Nagios 3.X did.  (Nagios knows it will spawn child worker programs from time to
# time, meaning this sort of cleanup will be required.)  So I'm going to assume
# that we need not take additional effort on our part to get that right.
#
# Nagios (as of 4.2.0) seems to use sigaction() when it's available (as it is on
# the platforms we compile for) with the SA_RESTART flag for most uses.  So Bronx
# should be fairly insensitive to incoming signals; its own system calls should be
# restarted automatically if that's where Bronx was executing when a signal came
# in and the signal ended up being handled on a Bronx thread.  At most, the Bronx
# thread will be temporarily delayed, with no apparent adverse effects.

# The instructions for building Nagios, the various patches we apply, the
# associated CGI programs, and Bronx have all gotten too complicated to keep
# track of through text descriptions alone.  It's therefore appropriate to
# capture all the processes and procedures on the Nagios side in this top-level
# build Makefile.  (Building Bronx is a separate exercise, to be undertaken
# after we have executed "make install" here to make the Nagios headers
# available within a GroundWork installation.)

# This is where we will park the files we extract from Subversion for building
# the Nagios stuff.
#
# WARNING:  Choose the $NAGIOS_BUILD_TREE path carefully, because we will
# completely wipe it out before using it!  Be sure to use an absolute pathname
# for the $NAGIOS_BUILD_TREE definition.
NAGIOS_BUILD_TREE = /tmp/nagios-build

# What you would expect.
NAGIOS_VERSION = 4.2.4

# Where the build places the final compiled binary for the Nagios monitoring engine.
NAGIOS_COMPILED_BINARY = $(NAGIOS_BUILD_TREE)/nagios-$(NAGIOS_VERSION)/base/nagios

# Where the build places the final compiled binary for the Nagios statistics program.
NAGIOSTATS_COMPILED_BINARY = $(NAGIOS_BUILD_TREE)/nagios-$(NAGIOS_VERSION)/base/nagiostats

# The usual form for the upstream tarball.  We expect this to expand into a
# nagios-$(NAGIOS_VERSION) directory without any help on our side, with
# everything under that.
NAGIOS_TARBALL = nagios-$(NAGIOS_VERSION).tar.gz

# Where we live.
GROUNDWORK_BASE = /usr/local/groundwork

# When we install GroundWork Monitor, we create this symlink to whatever mail program
# is installed on the local Linux distribution.  This allows us to invoke a common
# setup across platforms that might place the mail program in different locations.
GROUNDWORK_MAIL_PROGRAM = $(GROUNDWORK_BASE)/common/bin/mail

# This is where we find the gd.h header and the libgd.so library to link with.
GROUNDWORK_GD_INCLUDE_DIRECTORY = $(GROUNDWORK_BASE)/common/include
GROUNDWORK_GD_LIBRARY_DIRECTORY = $(GROUNDWORK_BASE)/common/lib

# It's probably not critical that we set this for the build, but there's no point
# in not having set to accurately reflect the context of GroundWork Monitor.
APACHE_CONFIG_DIRECTORY = $(GROUNDWORK_BASE)/apache2/conf

# This is the top level of where we direct the build to install the software.
NAGIOS_INSTALL_BASE_DIRECTORY = $(GROUNDWORK_BASE)/nagios

# In an unmodified Nagios distribution, this normally defaults in the build to "/tmp"
# (as the value of DEFAULT_TEMP_PATH in the locations.h header), and that default does
# not change (though it ought to) when we use the following setting in an option to the
# ./configure program.  But since we set it otherwise in our standard Monarch seed data,
# we may as well reflect that value here.  We do modify the locations.h header in one of
# our patches to change the value there to reflect what we're specifying here, to keep
# it all consistent.
NAGIOS_TEMP_DIRECTORY = $(NAGIOS_INSTALL_BASE_DIRECTORY)/tmp

# In an unmodified Nagios distribution, this normally defaults in the build to
# ${prefix}/var/spool/checkresults (as the value of DEFAULT_CHECK_RESULT_PATH in the
# locations.h header), and that default does not change (though it ought to) when we
# use the following setting in an option to the ./configure program.  But since we
# set it otherwise in our standard Monarch seed data, we may as well reflect that
# value here.  We do modify the locations.h header in one of our patches to change
# the value there to feflect what we're specifying here, to keep it all consistent.
NAGIOS_CHECK_RESULTS_DIRECTORY = $(NAGIOS_INSTALL_BASE_DIRECTORY)/var/checkresults

# This already defaults in the build (as the value of DEFAULT_LOCK_FILE in the locations.h
# header) to the same place that we name in our standard Monarch seed data, but we force it
# here simply to exercise some control over that setting in the build.
NAGIOS_LOCK_FILE = $(NAGIOS_INSTALL_BASE_DIRECTORY)/var/nagios.lock

# This is where Bronx expects to find the Nagios header files (in the nagios/ subdirectory
# of what we specify here).
NAGIOS_HEADERS_INSTALL_BASE_DIRECTORY = $(GROUNDWORK_BASE)/common/include

# We need to apply CFLAGS=-pthread to get the compiler to recognize that
# the code will ultimately be executed in multi-threaded mode, even
# though the Nagios code itself is just single-threaded.  (Bronx will add
# additional threads when it runs.)  An alternative construction would be
# CFLAGS="-D_REENTRANT -lpthread", but the single flag seems simpler and
# will take care of any other special options for multi-threaded code that
# are known to the compiler.  Oddly, adding this one option slows down the
# configuration and compilation considerably; I don't know why.
#
CFLAGS = -pthread

# Add the following flag if compiling Nagios 4.2.4 or a later release, and
# you wish to switch to the double-linked check_result list (which was
# implemented for possibly greater efficiency during list insertions).
#
CFLAGS += -DUSE_CHECK_RESULT_DOUBLE_LINKED_LIST

# Add the optimization flag that ./configure would normally generate on its own,
# but that it declines to apply to a GCC compilation when we set CFLAGS explicitly.
CFLAGS += -O2

# All the patches that we apply to this Nagios release.
#
# In some cases, multiple patches may modify the same file, and some patch
# areas in such a file may overlap.  Because that may be the case, conflicting
# patches have been constructed so as to rely on a specific ordering of patch
# application to accommodate earlier patches without complaining about offsets
# when later patches are applied.  Therefore, to avoid difficulty, PATCHES MUST
# BE SPECIFIED HERE IN THE ORDER THAT THEY MUST BE APPLIED.
#
ALL_NAGIOS_PATCHES = \
	nagios-4.2.4-non_static_logging.patch		\
	nagios-4.2.4-check_result_list.patch		\
	nagios-4.2.4-cgi_refresh.patch			\
	nagios-4.2.4-Makefile.in.statuswml2.patch	\
	nagios-4.2.4-no_deprecation_warnings.patch	\
	nagios-4.2.4-groundwork_paths.patch		\
	nagios-4.2.4-traceroute_cgi_perl.patch		\
	nagios-4.2.4-no_update_check.patch		\
	nagios-4.2.4-no_nagios_php.patch

# Let's make the default make target always safe to run.
#
default	: help

help	:
	@echo ""
	@echo "Primary build targets:"
	@echo ""
	@echo "    make help"
	@echo "    make all"
	@echo "    make verify"
	@echo "    make clean"
	@echo "    make distclean"
	@echo ""
	@echo "Internal build targets:"
	@echo ""
	@echo "    make base"
	@echo "    make patches"
	@echo "    make configure"
	@echo ""
	@echo "Primary install targets:"
	@echo "(must be run as root or nagios)"
	@echo ""
	@echo "    make install"
	@echo "    make installheaders"
	@echo ""
	@echo "Development/maintenance targets:"
	@echo ""
	@echo "    make newpatches"
	@echo "    make diffpatches"
	@echo "    make basepatch"
	@echo ""

# Target to splay out the upstream software.
#
# Unlike the Nagios 3.5.0 and 3.5.1 releases, which untarred into an anonymous
# "nagios" directory not labeled with the release number, the nagios-4.2.4.tar.gz
# tarball untars into the nagios-4.2.4 directory as we would hope.
#
base	:
	mkdir -p $(NAGIOS_BUILD_TREE)
	cp -p nagios-$(NAGIOS_VERSION).tar.gz $(NAGIOS_BUILD_TREE)
	cd $(NAGIOS_BUILD_TREE); tar xfz $(NAGIOS_TARBALL)

# Internal target.
#
copypatches	: base
	cp -p cgi/statuswml2.c $(NAGIOS_BUILD_TREE)/nagios-$(NAGIOS_VERSION)/cgi
	for patch in $(ALL_NAGIOS_PATCHES); do \
	    echo "Copying $$patch ..."; \
	    cp -p $$patch $(NAGIOS_BUILD_TREE); \
	done

# Internal target.
#
applypatches	: copypatches
	cd $(NAGIOS_BUILD_TREE)/nagios-$(NAGIOS_VERSION); \
	    for patch in $(ALL_NAGIOS_PATCHES); do \
		echo ""; \
		echo "Installing patch $$patch ..."; \
		patch -p1 -b < ../$$patch ; \
	    done

# Target to apply all our local patches to the base upstream software.
#
patches	: applypatches

# Target to create new patch files.  Usually this is done after "make patches"
# and then whatever manual editing is done to include other changes, to capture
# those changes and replace existing patches.  But see the WARNING below about
# the full procedure needed to make the new patch files accurately reflect what
# you need them to reflect.
#
# NOTE:  The computational approach used here gathers all the patch data only
# after ALL of the original patches have been applied.  But in reality, we need to
# instead generate each new individual patch only after patches have been applied
# up to and including that old patch, plus new edits to the files modified by that
# patch, but no farther.  That's because each patch should be generated according
# to the context that will be seen when that patch is applied, but we have some
# files that are modified by more than one patch.  We don't want to include in
# each patch the changes that should instead be applied by some later patch.  But
# we don't want to revert back to a clean slate and apply patches here to attempt
# to reconstruct such an original context, both because we want to assume they've
# already been applied and most importantly because we don't want to destroy any
# editing you've done after such patch application.  This is a conundrum.  See the
# WARNING below for the procedure that must be followed to make this workable.  We
# purposely print out that procedure on every invocation of "make newpatches" in
# order to remind ourselves that this is an ever-present danger, in case we're
# away from this development for awhile.
#
newpatches :
	#
	# WARNING:  "make newpatches" is an imperfect tool.  It does not and cannot
	# properly deal with source files that are modified by multiple patches.  This
	# happens currently, for instance, with the nagios-4.2.4-check_result_list.patch
	# and nagios-4.2.4-no_update_check.patch patches, which both independently modify
	# the base/utils.c file.  In particular, "make newpatches" cannot know whether a
	# new change to a file which is modified by both patch A and patch B is supposed
	# to be part of patch A or whether it's supposed to be part of patch B instead.
	# If you need to edit a source file which has such a conflict between multiple
	# patches, a particular procedure must be followed.
	#
	# (1) Identify the patch you need to work on.
	# (2) Temporarily remove from the definition of ALL_NAGIOS_PATCHES above all
	#     later patches that modify any of the same files as the patch you need
	#     to work on.
	# (3) "make distclean patches" to start from scratch and get the source code
	#     set up to the point where it is ready for your editing of the files
	#     modified by the patch you need to work on.
	# (4) Edit all the source-code files you need to modify to incorporate your
	#     intended changes for the updated patch, without any of those later
	#     patches having been already applied.
	# (5) Run "make newpatches", and grab the updated (".new") copy of the patch
	#     you just worked on.
	# (6) Adopt the updated copy of the patch so it becomes the standard copy.
	# (7) Ignore the output from "make newpatches" for the patches you
	#     temporarily removed from the definition of ALL_NAGIOS_PATCHES above.
	# (8) Restore the later patches into the defintion of ALL_NAGIOS_PATCHES
	#     above, for subsequent building.
	# (9) Run "make newpatches" and "make diffpatches" to see if either the
	#     patch-application line numbers or patch-application context have
	#     changed in a way that makes it necessary to update the later patches
	#     as well.  If so, make appropriate adjustments.
	# (10) Once you believe everything is correct (and in particular, only after
	#     you've saved the useful updated patches so you don't lose any of your
	#     recent edits), run "make distclean patches" to wipe out everything and
	#     build from scratch through the point where all the patches have been
	#     applied.  Make sure that all the patches got fully applied without any
	#     patch hunk rejections, offsets, or fuzz.  Run "make newpatches" again
	#     if you see that any corrections need to be made for such effects in
	#     later patches that modify some files modified by earlier patches you
	#     just updated, and adopt the updated later patches as necessary.
	#
	cd $(NAGIOS_BUILD_TREE); \
	    ( \
	    for file in base/logging.c include/logging.h ; do \
	        diff -u nagios-$(NAGIOS_VERSION)/$$file.orig nagios-$(NAGIOS_VERSION)/$$file || true; \
	    done \
	    ) > nagios-4.2.4-non_static_logging.patch.new
	cd $(NAGIOS_BUILD_TREE); \
	    ( \
	    for file in include/config.h.in include/objects.h include/nagios.h base/checks.c base/utils.c ; do \
	        diff -u nagios-$(NAGIOS_VERSION)/$$file.orig nagios-$(NAGIOS_VERSION)/$$file || true; \
	    done \
	    ) > nagios-4.2.4-check_result_list.patch.new
	cd $(NAGIOS_BUILD_TREE); \
	    ( \
	    for file in cgi/extinfo.c cgi/outages.c cgi/status.c cgi/statusmap.c cgi/tac.c ; do \
	        diff -u nagios-$(NAGIOS_VERSION)/$$file.orig nagios-$(NAGIOS_VERSION)/$$file || true; \
	    done \
	    ) > nagios-4.2.4-cgi_refresh.patch.new
	cd $(NAGIOS_BUILD_TREE); \
	    ( \
	    for file in cgi/Makefile.in ; do \
	        diff -u nagios-$(NAGIOS_VERSION)/$$file.orig nagios-$(NAGIOS_VERSION)/$$file || true; \
	    done \
	    ) > nagios-4.2.4-Makefile.in.statuswml2.patch.new
	cd $(NAGIOS_BUILD_TREE); \
	    ( \
	    for file in base/config.c xdata/xodtemplate.c ; do \
	        diff -u nagios-$(NAGIOS_VERSION)/$$file.orig nagios-$(NAGIOS_VERSION)/$$file || true; \
	    done \
	    ) > nagios-4.2.4-no_deprecation_warnings.patch.new
	cd $(NAGIOS_BUILD_TREE); \
	    ( \
	    for file in include/locations.h.in configure Makefile.in ; do \
	        diff -u nagios-$(NAGIOS_VERSION)/$$file.orig nagios-$(NAGIOS_VERSION)/$$file || true; \
	    done \
	    ) > nagios-4.2.4-groundwork_paths.patch.new
	cd $(NAGIOS_BUILD_TREE); \
	    ( \
	    for file in contrib/traceroute.cgi ; do \
	        diff -u nagios-$(NAGIOS_VERSION)/$$file.orig nagios-$(NAGIOS_VERSION)/$$file || true; \
	    done \
	    ) > nagios-4.2.4-traceroute_cgi_perl.patch.new
	cd $(NAGIOS_BUILD_TREE); \
	    ( \
	    for file in base/utils.c include/defaults.h ; do \
	        diff -u nagios-$(NAGIOS_VERSION)/$$file.orig nagios-$(NAGIOS_VERSION)/$$file || true; \
	    done \
	    ) > nagios-4.2.4-no_update_check.patch.new
	cd $(NAGIOS_BUILD_TREE); \
	    ( \
	    for file in html/Makefile.in ; do \
	        diff -u nagios-$(NAGIOS_VERSION)/$$file.orig nagios-$(NAGIOS_VERSION)/$$file || true; \
	    done \
	    ) > nagios-4.2.4-no_nagios_php.patch.new

# Here's a possible alternative formulation to what the "newpatches" target does,
# less literal and more table-driven.  Unfortunately for our purposes here, "make"
# doesn't allow all the punctuation we use in the patch filenames in make symbols,
# nor does it provide any sort of hash functionality that we could use to map a patch
# filename to the list of modified files that it represents.  It seems the best we
# can do here is to build in some strings that reflect the missing functionality.
#
# Update:  Look at the GNU Make Standard Library (http://gmsl.sourceforge.net/) for
# an implementation of hash functionality in GNU Make.  Whether that might suit our
# needs here has not been tested.
#
altpatches :
	#
	# WARNING:  "make altpatches" is an imperfect tool.  See the WARNING in the
	# Makefile for the "make newpatches" target for the procedure you must follow
	# when editing a file which is modified by more than one patch.
	#
	cd $(NAGIOS_BUILD_TREE); \
	    patches=( \
		"nagios-4.2.4-non_static_logging.patch      base/logging.c include/logging.h" \
		"nagios-4.2.4-check_result_list.patch       include/config.h.in include/objects.h include/nagios.h base/checks.c base/utils.c" \
		"nagios-4.2.4-cgi_refresh.patch             cgi/extinfo.c cgi/outages.c cgi/status.c cgi/statusmap.c cgi/tac.c" \
		"nagios-4.2.4-Makefile.in.statuswml2.patch  cgi/Makefile.in" \
		"nagios-4.2.4-no_deprecation_warnings.patch base/config.c xdata/xodtemplate.c" \
		"nagios-4.2.4-groundwork_paths.patch        include/locations.h.in configure Makefile.in" \
		"nagios-4.2.4-traceroute_cgi_perl.patch     contrib/traceroute.cgi" \
		"nagios-4.2.4-no_update_check.patch         base/utils.c include/defaults.h" \
		"nagios-4.2.4-no_nagios_php.patch           html/Makefile.in" \
	    ); \
	    for patch in "$${patches[@]}"; do \
		patchfiles=($$patch); \
		patchfile=$${patchfiles[0]}; \
		unset patchfiles[0]; \
		( \
		for file in "$${patchfiles[@]}" ; do \
		    diff -u nagios-$(NAGIOS_VERSION)/$$file.orig nagios-$(NAGIOS_VERSION)/$$file || true; \
		done \
		) > $$patchfile.new; \
	    done

# Target to produce a rough comparison of changes after a "make newpatches".
# This is mainly useful for verifying that nothing has inadvertently changed;
# lots of detail of the differences between old and new patches is purposely
# swept under the rug to obtain a very short report.
#
diffpatches	:
	#
	# WARNING:  "make diffpatches" is an imperfect tool, because of the same issues
	# that afflict the "make newpatches" target:  it does not and cannot properly
	# deal with source files that are modified by multiple patches.  This happens
	# currently, for instance, with the nagios-4.2.4-check_result_list.patch and
	# nagios-4.2.4-no_update_check.patch patches, which both independently modify
	# the base/utils.c file.  In particular, "make diffpatches" can only compare
	# the original and new patches it finds; it does not attempt to correct for any
	# problems in generating a new patch, as documented above for "make newpatches".
	# So if you need to look for changes in a patch that modifies a source file
	# which has such a conflict between multiple patches, the same sort of procedure
	# must be followed as with "make newpatches".  Namely, to check whether a
	# particular patch needs updating, it must be generated using a process that
	# starts with commenting out all later-applied patches in ALL_NAGIOS_PATCHES
	# that affect the same source fies.
	#
	@cd $(NAGIOS_BUILD_TREE); \
	    for patch in $(ALL_NAGIOS_PATCHES); do \
		echo "Comparing patch $$patch ..."; \
		#                                                                                      \
		# We used to suppress "@@" lines in the diffs, because we originally thought those     \
		# lines to be unimportant with respect to knowing whether the patch content had        \
		# changed.  But then we realized that changes to such lines do reflect important       \
		# modifications to patches, that will be reflected in the patch applying with an       \
		# offset if we don't correct it.  So now we do include the "@@" lines in this output.  \
		#                                                                                      \
		# diff $$patch $$patch.new | egrep -v '^([<>] (\+\+\+|---)|---$$|[0-9,]+[acd][0-9,]+$$|[<>] @@.*@@$$)' || true; \
		diff $$patch $$patch.new | egrep -v '^([<>] (\+\+\+|---)|---$$|[0-9,]+[acd][0-9,]+$$)' || true; \
	    done

# Target to configure the Nagios software before compiling it.
#
# To get the "Advanced > Nagios > Nagios Statusmap" and "Nagios Trend
# Report" screens to work, we need to get certain extra CGIs built and
# installed with Nagios 4.2.4 versions:
#
#     statusmap.cgi
#     trends.cgi
#
# We do so by running the top-level Nagios ./configure script with the
# following additional options, so the extra CGI programs can find the
# gd.h header and libgd.so library to link with.
#
#     --with-gd-inc=/usr/local/groundwork/common/include
#     --with-gd-lib=/usr/local/groundwork/common/lib
#
# With those options in play, the nagios-4.2.4/cgi/Makefile.in file has
# the CGIEXTRAS variable value extended to include the extra CGI programs.
#
# NOTE:  The --with-temp-dir and --with-checkresult-dir settings we specify here
# are not automatically reflected by the standard Nagios build process into the
# corresponding DEFAULT_TEMP_PATH and DEFAULT_CHECK_RESULT_PATH values in the
# generated locations.h file.  I would consider this lack of coordination to be
# a bug in the upstream distribution.  However, one of the GroundWork patches
# adjusts those settings in the locations.h file to match up with what we're
# specifying here, so there is no mismatch of expectations in our build.
#
configure	: patches
	cd $(NAGIOS_BUILD_TREE)/nagios-$(NAGIOS_VERSION); \
	    CFLAGS="$(CFLAGS)" ./configure --prefix=$(NAGIOS_INSTALL_BASE_DIRECTORY) \
		--enable-event-broker \
		--includedir=$(NAGIOS_HEADERS_INSTALL_BASE_DIRECTORY) \
		--with-lockfile=$(NAGIOS_LOCK_FILE) \
		--with-temp-dir=$(NAGIOS_TEMP_DIRECTORY) \
		--with-checkresult-dir=$(NAGIOS_CHECK_RESULTS_DIRECTORY) \
		--with-httpd-conf=$(APACHE_CONFIG_DIRECTORY) \
		--with-mail=$(GROUNDWORK_MAIL_PROGRAM) \
		--with-gd-inc=$(GROUNDWORK_GD_INCLUDE_DIRECTORY) \
		--with-gd-lib=$(GROUNDWORK_GD_LIBRARY_DIRECTORY)

# Target to compile all the software.  Target dependencies have been chained so
# it is splayed out, patched, and configured before we execute these commands.
#
all	: configure
	cd $(NAGIOS_BUILD_TREE)/nagios-$(NAGIOS_VERSION);         make all
	cd $(NAGIOS_BUILD_TREE)/nagios-$(NAGIOS_VERSION)/contrib; make

# Target to verify that the software has been built properly.  Additional tests
# may be added as we think of them.
#
verify	: $(NAGIOS_COMPILED_BINARY)
	@echo "Checking for a reference to the pthread library in the compiled nagios binary ..."
	@if ldd $(NAGIOS_COMPILED_BINARY) | fgrep libpthread.so; then \
	    echo "... pthread library reference is present."; \
	else \
	    echo "ERROR:  pthread library reference is missing in $(NAGIOS_COMPILED_BINARY) !"; \
	    false; \
	fi
	@echo "Checking for a working compiled nagios binary ..."
	@if $(NAGIOS_COMPILED_BINARY) --version | fgrep 'Nagios Core $(NAGIOS_VERSION)'; then \
	    echo "... binary is able to print its own version number."; \
	else \
	    echo "ERROR:  $(NAGIOS_COMPILED_BINARY) cannot print its own version number!"; \
	    false; \
	fi
	@echo "Checking for a working compiled nagiostats binary ..."
	@if $(NAGIOSTATS_COMPILED_BINARY) --version | fgrep 'Nagios Stats $(NAGIOS_VERSION)'; then \
	    echo "... binary is able to print its own version number."; \
	else \
	    echo "ERROR:  $(NAGIOSTATS_COMPILED_BINARY) cannot print its own version number!"; \
	    false; \
	fi

# Target to install the Nagios daemon, CGI programs, and all associated
# artifacts that we will need in a GWMEE release, into a GroundWork system.
# This is useful both for immediate development testing and for BitRock builds.
# We depend on the $(GROUNDWORK_BASE) directory already being in place; this
# Makefile makes no attempt to create that directory.
#
install	: $(NAGIOS_COMPILED_BINARY) $(GROUNDWORK_BASE)
	@if [ "`id -u`" -ne 0 -a "`id -un`" != nagios ]; then \
	    echo "ERROR:  You must be either root or nagios to install Nagios."; \
	    exit 1; \
	fi
	#
	# "make install" in the Nagios distribution appears to take care of what all of
	# the separate "make install-base", "make install-html", "make install-cgis",
	# and "make install-exfoliation" targets would have done, plus creating certain
	# directories we want for production, such as nagios/libexec/ and nagios/var/ .
	# The "make install-exfoliation" part installs the "Exfoliation theme" for the
	# CGI screens.
	#
	cd $(NAGIOS_BUILD_TREE)/nagios-$(NAGIOS_VERSION); make install
	#
	# If we wanted the "classic Nagios theme" instead of the "Exfoliation theme", we
	# would also need to "make install-classicui" after the basic installation is
	# done.  We can try the classic UI using the following command.  Personally, I
	# like the classic theme better, because the colors are brighter and because it
	# blends a bit better with the theme in the rest of GroundWork Monitor.  But for
	# the time being, we are sticking with the Exfoliation theme and are not running
	# this, because (1) we have apparently been shipping the Exfoliation theme for a
	# long time already, as part of Nagios 3.5.1, and (2) the Exfoliation theme is the
	# default, and I'm taking a wild guess that the default is better maintained.
	#
	# cd $(NAGIOS_BUILD_TREE)/nagios-$(NAGIOS_VERSION); make install-classicui
	#
	# The nagios/var/rw/ directory is only created if we ask for it separately,
	# via "make install-commandmode".
	#
	cd $(NAGIOS_BUILD_TREE)/nagios-$(NAGIOS_VERSION); make install-commandmode
	#
	# We could also do this, to install the new libnagios.a file which contains
	# various library functions that have been pulled out of Nagios proper.  We
	# don't currently have any dependency on them in Bronx or the rest of our own
	# code, but someone else might conceivably want them to link in some secondary
	# event broker that might use them.  If that happens, here's how to obtain the
	# installed copy of that library.
	#
	# cd $(NAGIOS_BUILD_TREE)/nagios-$(NAGIOS_VERSION); make install-lib
	#
	# The following programs are only installed if we explicitly ask for them,
	# by separately installing the contrib stuff.
	#
	#     nagios/bin/convertcfg
	#     nagios/sbin/daemonchk.cgi
	#     nagios/sbin/traceroute.cgi
	#
	cd $(NAGIOS_BUILD_TREE)/nagios-$(NAGIOS_VERSION)/contrib; make install

# Target to install the Nagios header files in the proper place within a
# GroundWork system where a Bronx compilation is expecting to pick them up.
# This does not include the nagios/config.h and nagios/config_pwd.h files,
# which get deleted.  But we have modified Bronx to not depend on them.
#
installheaders	: $(NAGIOS_COMPILED_BINARY) $(GROUNDWORK_BASE)
	@if [ "`id -u`" -ne 0 -a "`id -un`" != nagios ]; then \
	    echo "ERROR:  You must be either root or nagios to install the Nagios headers."; \
	    exit 1; \
	fi
	cd $(NAGIOS_BUILD_TREE)/nagios-$(NAGIOS_VERSION); make install-headers
	chown -R nagios:nagios $(NAGIOS_HEADERS_INSTALL_BASE_DIRECTORY)/nagios

# Target to create a clean patch file for a previous release, starting from a
# clean installed copy of the GWMEE 7.1.2 release.  We presume that you have run
# "make distclean all verify" before making this target, but that you have not
# run a "make install" yet.
#
basepatch	:
	@if [ "`id -u`" -ne 0 -a "`id -un`" != nagios ]; then \
	    echo "ERROR:  You must be root to make a basepatch."; \
	    exit 1; \
	fi
	#
	# Before we collect all the updated files, make sure there is
	# no interference from any running component of the system.
	service groundwork stop
	#
	# Move aside the existing files to ensure that we start with a clean pallette
	# for collecting the new files.
	mv /usr/local/groundwork/nagios /usr/local/groundwork/nagios.orig
	mkdir $(NAGIOS_INSTALL_BASE_DIRECTORY)
	chown nagios:nagios $(NAGIOS_INSTALL_BASE_DIRECTORY)
	chmod 755           $(NAGIOS_INSTALL_BASE_DIRECTORY)
	#
	# Create the desired new files in-place.  We presume you've already done a
	# "make distclean all verify" before you ran "make basepatch", as described above.
	make install
	#
	# Edit what we just installed slightly, to match the already-existing structure of
	# the BitRock-compiled binaries, and to remove some empty directories which have
	# no value in this patch (because we know these directories already exist in the
	# base release we will be patching, and there's no point in changing permissions
	# on the already-installed directories to what this build sets them to).
	mv $(NAGIOS_INSTALL_BASE_DIRECTORY)/bin/convertcfg $(NAGIOS_INSTALL_BASE_DIRECTORY)/bin/.convertcfg.bin
	mv $(NAGIOS_INSTALL_BASE_DIRECTORY)/bin/nagios     $(NAGIOS_INSTALL_BASE_DIRECTORY)/bin/.nagios.bin
	mv $(NAGIOS_INSTALL_BASE_DIRECTORY)/bin/nagiostats $(NAGIOS_INSTALL_BASE_DIRECTORY)/bin/.nagiostats.bin
	rmdir $(NAGIOS_INSTALL_BASE_DIRECTORY)/libexec
	rmdir $(NAGIOS_INSTALL_BASE_DIRECTORY)/share/docs/images
	rmdir $(NAGIOS_INSTALL_BASE_DIRECTORY)/share/docs
	rmdir $(NAGIOS_INSTALL_BASE_DIRECTORY)/share/includes
	rmdir $(NAGIOS_INSTALL_BASE_DIRECTORY)/share/media
	rmdir $(NAGIOS_INSTALL_BASE_DIRECTORY)/share/ssi
	rmdir $(NAGIOS_INSTALL_BASE_DIRECTORY)/tmp
	rmdir $(NAGIOS_INSTALL_BASE_DIRECTORY)/var/archives
	rmdir $(NAGIOS_INSTALL_BASE_DIRECTORY)/var/checkresults
	rmdir $(NAGIOS_INSTALL_BASE_DIRECTORY)/var/spool
	rmdir $(NAGIOS_INSTALL_BASE_DIRECTORY)/var
	#
	# Find all the files we will want to be part of this patch.  The trick here is that when we
	# tar up the files, we must avoid the usual recursive behavior of tar.  By default, if you
	# give tar a directory name in the list of files to collect, it will automatically recurse
	# into that directory.  So we would only be able to name either completely-new directories
	# (and avoid listing any of the files and directories under each such top-level directory),
	# and new or updated files within directories which already existed in the old release.  But
	# since we have 7.1.2 installed so we can pick up the BitRock-compiled code, we have no
	# reference for which directories did not exist in the 7.1.1 release that we are patching.
	# We work around that by using the tar --no-recursion option when we collect the files.
	find \
	    $(NAGIOS_INSTALL_BASE_DIRECTORY)/*				\
	    $(GROUNDWORK_BASE)/common/lib/libbronx.so			\
	    $(GROUNDWORK_BASE)/core/monarch/lib/MonarchForms.pm		\
	    $(GROUNDWORK_BASE)/core/monarch/lib/MonarchStorProc.pm	\
	    -print > $(NAGIOS_BUILD_TREE)/groundwork-patch-filelist
	#
	# Put back the GWMEE 7.1.2 release files, so we pick up the BitRock-compiled files
	# instead of the ones we just compiled.
	rm -rf $(NAGIOS_INSTALL_BASE_DIRECTORY)
	mv $(NAGIOS_INSTALL_BASE_DIRECTORY).orig $(NAGIOS_INSTALL_BASE_DIRECTORY)
	#
	# Collect all the files we will want as part of this patch.
	tar cfz $(NAGIOS_BUILD_TREE)/groundwork-nagios-4.2.4-upgrade-files.tar.gz \
	    --no-recursion --files-from=$(NAGIOS_BUILD_TREE)/groundwork-patch-filelist

# The usual suspects.

clean	:
	cd $(NAGIOS_BUILD_TREE)/nagios-$(NAGIOS_VERSION); make clean

distclean	:
	rm -rf $(NAGIOS_BUILD_TREE)


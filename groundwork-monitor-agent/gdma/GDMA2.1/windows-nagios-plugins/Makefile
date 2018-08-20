# Makefile for compiling Nagios plugins under Cygwin.

# ================================================================
# NAGIOS PLUGINS CYGWIN-BUILD NOTES
# ================================================================
#
# Note #1:
#
# If we use the symlinks that a normal build under Cygwin would create,
# then tar will capture the .lnk files (Windows shortcut files that Cygwin
# abuses and treats as symlinks) as UNIX links.  But we want to treat
# these as files for purposes of transporting them to a UNIX system for
# checkin to Subversion, if we are to mirror the construction that was
# used by the third-party build of the Nagios Plugins 1.4.5 release.
#
# Further investigation shows that Windows has a complicated relationship
# with symlink and symlink-similar capabilities.  Notably, .lnk files are
# usually shortcuts, not symlinks.  Because of all this complication, we
# want to abandon the .lnk files that we used with the Nagios Plugins
# 1.4.5 release, and move to either the use of NTFS symlinks or simple
# copies of the target files instead of some sort of links to them.  Such
# alternative schemes mean we need to make careful changes to the way that
# links are handled in the configure script and in some Makefile files.
#
# If we did stick with shortcuts, then we would probably need to modify
# the way they are constructed so we didn't have:
#     check_udp.lnk -> check_tcp
# as the original scripting generated, but instead would have
#     check_udp.lnk -> check_tcp.exe
# to ensure that the shortcut refers to a file that might work.  Even then,
# we would need to test to ensure that the invoked program will see the
# symlink source name as its first argument, and not the symlink target
# name, so it can adapt its behavior appropriately.
#
# Our patches to the distributed Nagios Plugins code modify the build files
# so they will correctly generate whatever types of linked or copied target
# files that we desire.
#
# Note #2:
#
# Why do our compiled programs reference two different versions of these
# libraries, so we need to include both of them in our distribution?
#
# cygcrypto-0.9.8.dll
# cygcrypto-1.0.0.dll
# cygssl-0.9.8.dll
# cygssl-1.0 .0.dll
#
# These references arise solely from the check_pgsql.exe plugin, and
# in turn from the cygpq.dll library that it references.  That library
# includes references to both old and new versions of the crypto and
# ssl libraries.  Since we obtain our copy of the cygpq.dll library from
# the precompiled /usr/bin/cygpq.dll file, we're going to assume this
# is acceptable in spite of the seeming likely conflict.  The Cygwin
# installer labels the older libopenssl098 package as "compat", so
# perhaps it is only supplying certain symbols which are no longer
# supplied in the libopenssl100 package.
#
# Note #3:
#
# We have a relocation problem.  These plugins are being compiled with this definition:
#     -DLOCALEDIR=\"/home/Administrator/nagios-plugins/build_tree/share/locale\"
# which results in that absolute build-time pathname being embedded in the binaries.
# One other definition suffers the same fate:
#     -DNP_STATE_DIR_PREFIX=\"/home/Administrator/nagios-plugins/build_tree/var\"
# A bunch of other pathnames are embedded in the build-generated
# nagios-plugins-1.4.16/gl/configmake.h header file, but only the
# two mentioned above seem to end up in the compiled binaries:
#
#      echo '#define PREFIX "/home/Administrator/nagios-plugins/build_tree"'; \
#      echo '#define EXEC_PREFIX "/home/Administrator/nagios-plugins/build_tree"'; \
#      echo '#define BINDIR "/home/Administrator/nagios-plugins/build_tree/bin"'; \
#      echo '#define SBINDIR "/home/Administrator/nagios-plugins/build_tree/sbin"'; \
#      echo '#define LIBEXECDIR "/home/Administrator/nagios-plugins/build_tree/libexec"'; \
#      echo '#define DATAROOTDIR "/home/Administrator/nagios-plugins/build_tree/share"'; \
#      echo '#define DATADIR "/home/Administrator/nagios-plugins/build_tree/share"'; \
#      echo '#define SYSCONFDIR "/home/Administrator/nagios-plugins/build_tree/etc"'; \
#      echo '#define SHAREDSTATEDIR "/home/Administrator/nagios-plugins/build_tree/com"'; \
#      echo '#define LOCALSTATEDIR "/home/Administrator/nagios-plugins/build_tree/var"'; \
#      echo '#define INCLUDEDIR "/home/Administrator/nagios-plugins/build_tree/include"'; \
#      echo '#define DOCDIR "/home/Administrator/nagios-plugins/build_tree/share/doc/nagios-plugins"'; \
#      echo '#define INFODIR "/home/Administrator/nagios-plugins/build_tree/share/info"'; \
#      echo '#define HTMLDIR "/home/Administrator/nagios-plugins/build_tree/share/doc/nagios-plugins"'; \
#      echo '#define DVIDIR "/home/Administrator/nagios-plugins/build_tree/share/doc/nagios-plugins"'; \
#      echo '#define PDFDIR "/home/Administrator/nagios-plugins/build_tree/share/doc/nagios-plugins"'; \
#      echo '#define PSDIR "/home/Administrator/nagios-plugins/build_tree/share/doc/nagios-plugins"'; \
#      echo '#define LIBDIR "/home/Administrator/nagios-plugins/build_tree/lib"'; \
#      echo '#define LOCALEDIR "/home/Administrator/nagios-plugins/build_tree/share/locale"'; \
#      echo '#define MANDIR "/home/Administrator/nagios-plugins/build_tree/share/man"'; \
#      echo '#define PKGDATADIR "/home/Administrator/nagios-plugins/build_tree/share/nagios-plugins"'; \
#      echo '#define PKGINCLUDEDIR "/home/Administrator/nagios-plugins/build_tree/include/nagios-plugins"'; \
#      echo '#define PKGLIBDIR "/home/Administrator/nagios-plugins/build_tree/lib/nagios-plugins"'; \
#      echo '#define PKGLIBEXECDIR "/home/Administrator/nagios-plugins/build_tree/libexec/nagios-plugins"'; \
#
# Here are the build-tree references that actually show up embedded in the code:
#
#     /home/Administrator/nagios-plugins/build_tree/share/locale
#     /home/Administrator/nagios-plugins/build_tree/var
#     use lib  "/home/Administrator/nagios-plugins/build_tree/libexec" ;
#     use lib "/home/Administrator/nagios-plugins/build_tree/libexec"  ;
#     use lib "/home/Administrator/nagios-plugins/build_tree/libexec" ;
#
# We could try to find some way to change these two paths at build time from an
# absolute path to a path relative to the directory where the binary lives (though
# that is not necessarily the working directory when the binary runs).  That would
# allow users to continue to relocate the GDMA package to some alternate directory
# when they install GDMA on a client machine.  But then the code would need to be
# modified to discover where the binary lives, even if it is run from elsewhere
# (not an easy thing to do), and to apply that base-directory relocation.  Failing
# that, we would need to change this absolute pathname to the standard default
# install path used by the Windows GDMA installer, and there would be no good way
# to allow relocation.
#
# A third possibility is suggested by the way the plugin code itself already handles
# the var directory path.  The NAGIOS_PLUGIN_STATE_DIRECTORY environment variable is
# supported as a means of specifying at run time an alternative to the compiled-in
# NP_STATE_DIR_PREFIX value.  We can invent a similar NAGIOS_PLUGIN_LOCALE_DIRECTORY
# environment variable to supply the share/locale directory path, and extend the
# code to look for that environment variable and use its value if it is found.  We
# can then define these environment variables in the GDMA code, to absolute values
# which take into account where GDMA itself got installed, and have them inherited
# by the plugin code when it is run.
#
# We have now taken the third approach when we build the Nagios plugins code here.
#
# In Perl scripting, we have solved the problem via a code rewrite at build time,
# to include code that will find the installed location and construct the necessary
# absolute pathname at run time instead of either having a fixed path compiled in
# or accepting an environment variable to supply this information.

# ================================================================
# NAGIOS PLUGINS CONFIGURATION VARIABLES
# ================================================================

PLUGINS_RELEASE			:= 1.4.16
PLUGINS_SOURCE_TARBALL		:= $(PWD)/nagios-plugins-$(PLUGINS_RELEASE).tar.gz
PLUGINS_SOURCE_DIRECTORY	:= $(PWD)/nagios-plugins-$(PLUGINS_RELEASE)
PLUGINS_TARGET_DIRECTORY	:= $(PWD)/build_tree
PLUGINS_TARGET_TARBALL		:= $(PWD)/cygwin-nagios-plugins-$(PLUGINS_RELEASE).tar.gz
PLUGINS_TARGET_NOPERLTARBALL	:= $(PWD)/cygwin-nagios-noperl-plugins-$(PLUGINS_RELEASE).tar.gz
PLUGINS_TARGET_SVNTARBALL	:= $(PWD)/cygwin-nagios-svn-plugins-$(PLUGINS_RELEASE).tar.gz
PLUGINS_TARGET_ZIPFILE		:= $(PWD)/cygwin-nagios-plugins-$(PLUGINS_RELEASE).zip
PLUGINS_TARGET_NOPERLZIPFILE	:= $(PWD)/cygwin-nagios-noperl-plugins-$(PLUGINS_RELEASE).zip
PLUGINS_TARGET_PERLZIPFILE	:= $(PWD)/cygwin-nagios-perl-plugins-$(PLUGINS_RELEASE).zip
PLUGINS_LOCALE_SOURCES		:= \
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_by_ssh.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_cluster.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_dig.c		\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_disk.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_dns.c		\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_dummy.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_fping.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_game.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_hpjd.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_http.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_ide_smart.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_ldap.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_load.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_mrtg.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_mrtgtraf.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_mysql.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_mysql_query.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_nagios.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_nt.c		\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_ntp.c		\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_ntp_peer.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_ntp_time.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_nwstat.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_overcr.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_pgsql.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_ping.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_procs.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_radius.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_real.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_smtp.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_snmp.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_ssh.c		\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_swap.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_tcp.c		\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_time.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_ups.c		\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/check_users.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/negate.c		\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins/urlize.c		\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins-root/check_dhcp.c	\
	$(PLUGINS_SOURCE_DIRECTORY)/plugins-root/check_icmp.c

# ================================================================
# NAGIOS PLUGINS BUILD RULES
# ================================================================

# Logically, there ought to be more dependencies between these targets,
# but we avoid that so the developer can run individual phases at will
# for development/test purposes.
all	: untar patch config build addlibs svntarball perlzipfile

targets	:
	@echo "Here are the possible make targets:"
	@echo "    clean untar patch config build addlibs"
	@echo "    tarball noperltarball"
	@echo "    zipfile noperlzipfile perlzipfile"

clean	:
	rm -rf $(PLUGINS_SOURCE_DIRECTORY)
	rm -rf $(PLUGINS_TARGET_DIRECTORY)
	rm -f  $(PLUGINS_TARGET_TARBALL)
	rm -f  $(PLUGINS_TARGET_NOPERLTARBALL)
	rm -f  $(PLUGINS_TARGET_SVNTARBALL)
	rm -f  $(PLUGINS_TARGET_ZIPFILE)
	rm -f  $(PLUGINS_TARGET_NOPERLZIPFILE)
	rm -f  $(PLUGINS_TARGET_PERLZIPFILE)

untar	: clean
	tar xfz $(PLUGINS_SOURCE_TARBALL)

# Fix up the distributed build scripts and code to make it suitable for building
# under Cygwin on Windows.
patch	:
	patch -p0 -b < configure.patch
	patch -p0 -b < plugins-check_ping.c.patch
	patch -p0 -b < plugins-Makefile.in.patch
	patch -p0 -b < plugins-root-Makefile.in.patch
	patch -p0 -b < plugins-scripts-Makefile.in.patch
	patch -p0 -b < plugins-scripts-subst.in.patch
	for file in $(PLUGINS_LOCALE_SOURCES); do \
	    perl -pi -e 's/\Qbindtextdomain (PACKAGE, LOCALEDIR);\E/char *env_dir; bindtextdomain (PACKAGE, ((env_dir = getenv("NAGIOS_PLUGIN_LOCALE_DIRECTORY")) && env_dir[0]) ? env_dir : LOCALEDIR);/' $$file; \
	done

# We redefine how symlinks are handled using the LN_S and LNKEXT definition overrides,
# along with support from some of the patches to the "configure" script, so we don't
# have the build trying to make Cygwin-style symlinks using Windows shortcut files
# (.lnk files) that don't work as we would expect symlinks to behave when accessed
# under Windows itself.  We could have, if we so desired, replaced the LN_S definition
# here (and the supporting parts of the "configure" script patch with a PATH setting
# that invokes "ln" as "/bin/false" for the duration of the build.  But that seems
# even more obscure and hard to understand.
#
# For more details on that issue, see these discussions:
# http://www.mentby.com/Group/autoconf-discuss/overriding-lns.html
# http://old.nabble.com/Re%3A-Overriding-LN_S-td29969280.html
#
# If we have the Cygwin version of "ping" installed, we need to logically disable
# calling "ping" during the ./configure processing with the --without-ping-command
# option, because if we don't do that, the configure will hang forever. That's
# because the Cygwin ping command is limited in its capabilities, and at least one
# of the ping commands that ./configure will try to run while looking for the right
# command-line options to use will execute forever without any limitation on the
# number of cycles or any overall timeout. The Windows-native copy of ping doesn't
# have this problem, so it's probably best to simply not to have the Cygwin "ping"
# package installed, for the duration of the build.
#
#	if [ -x /usr/bin/ping ]; then PING_OPTION=--without-ping-command; fi; \
#
# However, that's an ugly solution, because killing the ping command that way does
# not allow it to discover the Windows-native copy of "ping". (Though if ./configure
# does try to run the Windows-native copy, its detection tests will fail because
# they don't currently recognize the difference in its output compared to UNIX ping.
# A simple fix to the "configure" script would address that, but we don't want to
# leave the compile subject to the whims of whether or not the Cygwin ping package is
# installed, so we don't bother to patch "configure" for this purpose, and instead we
# just specify the ping commands explicitly at build time.)
#
# If it's installed here on our build machine, the Cygwin version of "ping" will be
# detected unless we're careful here to avoid that. We cannot allow that command to
# be compiled into the code because it won't be there on customer systems. Instead,
# we refer to the Windows-native version of "ping". Unfortunately, the Windows-native
# version of "ping" produces output which is different from that of any of the UNIX
# versions, so if we do this, we must also patch check_ping.c to recognize particular
# strings from its output.
config	:
	cd $(PLUGINS_SOURCE_DIRECTORY); \
	PING_OPTION='--with-ping-command=C:\\Windows\\System32\\ping.exe -n %d %s'; \
	PING6_OPTION='--with-ping6-command=C:\\Windows\\System32\\ping.exe -n %d -6 %s'; \
	LN_S="cp -p" LNKEXT=.exe ./configure --prefix=$(PLUGINS_TARGET_DIRECTORY) \
	    $${PING_OPTION:+"$$PING_OPTION"} $${PING6_OPTION:+"$$PING6_OPTION"}

# If we didn't patch the plugins-root/Makefile.in file above, then we would need to
# use the "make -k" flag on both the "make" and the "make install", to keep the build
# going despite the failure of check_icmp.c to compile.  If you wonder why it won't
# compile under Cygwin, see these links:
#
# http://cygwin.com/ml/cygwin/2007-10/msg00479.html
# http://cygwin.com/ml/cygwin/2007-10/msg00493.html
# http://cygwin.com/ml/cygwin/2007-10/msg00493.html
#
# http://cygwin.com/ml/cygwin-patches/2011-q1/msg00081.html
# http://cygwin.com/ml/cygwin-patches/2011-q1/msg00082.html
# http://cygwin.com/ml/cygwin-patches/2011-q1/msg00083.html
#
# Instead of using the -k flag, we now suppress the build of that one program
# by effectively patching the relevant generated Makefile, above.  This allows
# us to still see whether any other parts of the build fail, which is an
# important part knowing that we have a clean and complete build.
build	:
	cd $(PLUGINS_SOURCE_DIRECTORY); make; make install

# Here we add the library files that are needed to run the compiled programs
# on a Windows machine without Cygwin being present as well.
addlibs	:
	files=`ldd $(PLUGINS_TARGET_DIRECTORY)/libexec/*.exe | sort -u | fgrep .dll | awk '{print $$3}' | egrep -v '(kernel32|ntdll).dll'`; \
	cp -p $$files $(PLUGINS_TARGET_DIRECTORY)/libexec

# The tarball target is for transporting the full set of compiled and still-scripted
# plugins to a UNIX machine for Subversion check-in or other archival storage, in case
# there is any difficulty in using Subversion directly on this build machine.
#
# The Nagios plugins install process also creates an "include" subdirectory,
# but it is empty so we do not include it in this tarball.
tarball	:
	cd $(PLUGINS_TARGET_DIRECTORY); tar cvfz $(PLUGINS_TARGET_TARBALL) libexec share

noperltarball	:
	cd $(PLUGINS_TARGET_DIRECTORY); tar cvfz $(PLUGINS_TARGET_NOPERLTARBALL) --exclude '*.pl' --exclude '*.pm' libexec share

svntarball	:
	cd $(PLUGINS_TARGET_DIRECTORY); tar cvfz $(PLUGINS_TARGET_SVNTARBALL) -C $$PWD/libexec . -C $$PWD ./share

# The zipfile target is for transporting the full set of compiled and still-scripted
# plugins to some other Windows machine for testing.
#
# The Nagios plugins install process also creates an "include" subdirectory,
# but it is empty so we do not include it in this zipfile.
zipfile	:
	# zip seems to have no option to ignore an existing zipfile and
	# just write from scratch, so we have to force that externally.
	rm -f $(PLUGINS_TARGET_ZIPFILE)
	cd $(PLUGINS_TARGET_DIRECTORY); zip -r $(PLUGINS_TARGET_ZIPFILE) libexec share

noperlzipfile	:
	rm -f $(PLUGINS_TARGET_NOPERLZIPFILE)
	cd $(PLUGINS_TARGET_DIRECTORY); zip -r $(PLUGINS_TARGET_NOPERLZIPFILE) libexec share --exclude '*.pl' '*.pm'

# The perlzipfile target is for transporting the final built Perl scripts to
# some other location (.../windows/src/plugins/ in Subversion) for compilation
# into Windows binary .exe files.  That's why we copy all the files we care
# about to a separate directory, which matches the name of the directory where
# these files will be checked in.  This will make it easy to unzip the file on
# our Perl build machine.
perlzipfile	:
	cd $(PLUGINS_TARGET_DIRECTORY); rm -rf plugins; mkdir -p plugins; cp -p libexec/*.pl libexec/*.pm plugins
	rm -f $(PLUGINS_TARGET_PERLZIPFILE)
	cd $(PLUGINS_TARGET_DIRECTORY); zip -r $(PLUGINS_TARGET_PERLZIPFILE) plugins

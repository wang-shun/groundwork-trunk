# makefile for Apache mod_security2 and related components,
# to be used to create a patch for GW6.0 / GW6.0.1 / GW6.1 and
# for hints as to how these pieces should be folded directly
# into later product builds.

# The purpose of this makefile is not just to get the related components
# to build, but also (during the development cycle) to provide a convenient
# mechanism for identifying all the individual files that get installed,
# so we can produce a patch containing just exactly those files.

# ================================================================

# Components needed beyond what we ship in GW6.1:
# libcurl v7.15.1 or higher (http://curl.haxx.se/libcurl/) [required to support audit logging]
# liblua v5.1.x (http://www.lua.org/download.html) [optional]
# modsecurity-apache v2.5.11 (the current release as of Nov 6, 2009) (http://www.modsecurity.org/)
# mod_unique_id (packaged with Apache httpd) [required]

# ================================================================

# WARNING:  Do not build as root, except for creating the final tarball!
# That will overwrite files on your existing machine, if you perform an
# unredirected installation during the build (having "DO_INSTALL = 1"
# and "REDIRECT_BUILD = 0" in play below).  Such a build overwrites some
# files in the original GroundWork Monitor installation and is therefore
# possibly somewhat dangerous.  (Afterward, you should consider your
# installation of GroundWork Monitor to be corrupted, and uninstall and
# reinstall it.)
#
# Instead, use some other account for most of the building and testing,
# and the "nagios" user for building the final distribution tarball.
# However, to create a clean test build, you will need to "make prepare"
# as root, to be able to copy the entire /usr/local/groundwork tree to
# /tmp/local/groundwork for the test compilations.

# How to create test builds:
# Have "REDIRECT_BUILD = 1" set in this makefile.
# as root:  make prepare
# as user:  make all
# as user:  make tarball

# How to build the final tarball:
# as root:  make REDIRECT_BUILD=0 prepare tarball

# Creating the final tarball will splay files around directly in
# your GroundWork Monitor distribution, so it's not something you
# want to do until you're satisfied the rest of the build is ready.

# ================================================================

# Environmental factors.

ifeq ($(USER),root)
I_AM_ROOT = 1
else
I_AM_ROOT = 0
endif

GROUNDWORK_RELEASE = $(shell egrep 'Core|version' /usr/local/groundwork/Info.txt | head -1 | awk '{print $$NF}' | sed -e 's/-.*//')
SUBVERSION_RELEASE = $(shell svn info -r HEAD | fgrep Revision | gawk '{print $$2}')
OS_DISTRIBUTION    = $(shell if [ -f /usr/lib/rpm/redhat/dist.sh ]; then /usr/lib/rpm/redhat/dist.sh; fi)
PROCESSOR_TYPE     = $(shell uname -p)
PATCH_RELEASE      = GW$(GROUNDWORK_RELEASE)$(patsubst %,-%,$(SUBVERSION_RELEASE))$(OS_DISTRIBUTION).$(PROCESSOR_TYPE)

# ================================================================

# Set this to 0 to just compile the various modules (e.g., to work out
# what combinations of build flags need to be applied, without going
# further).  Set this to 1 to also attempt to install the software.
DO_INSTALL = 1

# Set this to 0 for a direct installation on top of an existing
# GroundWork Monitor product, or to 1 to redirect the build and
# installation to a separate file tree (from /usr/local/groundwork/...
# to /tmp/local/groundwork/...) to make it easier to identify
# exactly which files get added in this build.
REDIRECT_BUILD = 1

# Whomever is building this software.  This has to be a fixed string,
# not defined as $(USER) because we need to reference it while
# running "make" as root, to change files to this user.group so that
# user can run the rest of the build.
BUILD_USER = gherteg
BUILD_GROUP = $(shell id -gn $(BUILD_USER))
BUILD_USER_AND_GROUP = $(BUILD_USER):$(BUILD_GROUP)

DIFFS_FILE = /tmp/,diffs

TEMP_BASE_DIRECTORY = /tmp/local/groundwork
REAL_BASE_DIRECTORY = /usr/local/groundwork

# Temporary directories are side locations, to make it easier to
# see just the files that get installed in the real location.  We
# can build most of the packages with this as the target, then
# just do a recursive file listing on this directory to find the
# new files that will be added to an existing product distribution.
TEMP_COMMON_INSTALL_DIR = $(TEMP_BASE_DIRECTORY)/common
TEMP_APACHE_INSTALL_DIR = $(TEMP_BASE_DIRECTORY)/apache2
REAL_COMMON_INSTALL_DIR = $(REAL_BASE_DIRECTORY)/common
REAL_APACHE_INSTALL_DIR = $(REAL_BASE_DIRECTORY)/apache2

ifeq ($(REDIRECT_BUILD),1)
GROUNDWORK_BASE_DIR = $(TEMP_BASE_DIRECTORY)
else
GROUNDWORK_BASE_DIR = $(REAL_BASE_DIRECTORY)
endif

ifeq ($(REDIRECT_BUILD),1)
COMMON_INSTALL_DIR = $(TEMP_COMMON_INSTALL_DIR)
else
COMMON_INSTALL_DIR = $(REAL_COMMON_INSTALL_DIR)
endif

ifeq ($(REDIRECT_BUILD),1)
APACHE_INSTALL_DIR = $(TEMP_APACHE_INSTALL_DIR)
else
APACHE_INSTALL_DIR = $(REAL_APACHE_INSTALL_DIR)
endif

MODIFIED_BUILD_FILES = \
	$(TEMP_APACHE_INSTALL_DIR)/bin/apxs			\
	$(TEMP_APACHE_INSTALL_DIR)/build/config_vars.mk		\
	$(TEMP_COMMON_INSTALL_DIR)/bin/apr-1-config		\
	$(TEMP_COMMON_INSTALL_DIR)/bin/apu-1-config		\
	$(TEMP_COMMON_INSTALL_DIR)/bin/pcre-config		\
	$(TEMP_COMMON_INSTALL_DIR)/lib/apr.exp			\
	$(TEMP_COMMON_INSTALL_DIR)/lib/aprutil.exp		\
	$(TEMP_COMMON_INSTALL_DIR/lib/pkgconfig/apr-1.pc	\
	$(TEMP_COMMON_INSTALL_DIR)/lib/pkgconfig/apr-util-1.pc

CURL_VERSION    = 7.19.7
HTTPD_VERSION   = 2.2.9
LUA_VERSION     = 5.1.4
MODSEC2_VERSION = 2.5.11

ifeq ($(PROCESSOR_TYPE),x86_64)
MACHINE_TYPE_OPTION = -m64
else
MACHINE_TYPE_OPTION = -m32
endif

# ----------------------------------------------------------------

# LDFLAGS is set here in a manner which is needed both to recognize our own copies
# of various libraries, and to get them linked correctly into the generated code.
# But additional help is needed during the build to reverse the order in which
# libraries are accessed, so that our versions of libraries are used in preference
# to system copies.  See the libtool adjustment later on.

CURL_CONFIG_OPTIONS = \
	LDFLAGS="-L$(COMMON_INSTALL_DIR)/lib -R$(COMMON_INSTALL_DIR)/lib -Wl,-rpath -Wl,$(COMMON_INSTALL_DIR)/lib"	\
	CPPFLAGS="-I$(COMMON_INSTALL_DIR)/include"	\
	--prefix=$(COMMON_INSTALL_DIR)			\
	--enable-ldaps					\
	--with-ssl=$(COMMON_INSTALL_DIR)		\
	--with-zlib=$(COMMON_INSTALL_DIR)		\
	--with-ldap-lib=ldap				\
	--with-lber-lib=lber				\
	--without-gnutls				\
	--without-krb4					\
	--without-spnego				\
	--without-libssh2				\
	--without-nss					\
	--without-gssapi

# ----------------------------------------------------------------

# Most of the settings in this section are intended just to emulate the manner
# in which Bitrock builds Apache.  But all we really care about in this part of
# the build is to get apache2/modules/mod_unique_id.so built in a clean way, as
# a shared library.  When Bitrock takes over this code, all they will really
# care about as a delta to the current build is to add the following flag:
#
#     This gets mod_unique_id compiled, but linked into httpd statically.
#     That condition is discernable afterward by running "httpd -l | fgrep uniq".
#	--enable-unique-id
#
#     This gets mod_unique_id compiled and provided as a shared library.
#	--enable-unique-id=shared
#
#     This works exactly like --enable-unique-id=shared
#	--enable-mods-shared=unique_id

# Note: --with-ldap is not mentioned in the httpd's "./configure --help" output,
# but it is apparently necessary (even aside from specifying --enable-ldap) if
# --enable-authnz-ldap is specified.

HTTPD_CONFIG_OPTIONS = \
	CC=gcc						\
	CFLAGS="$(MACHINE_TYPE_OPTION) -fPIC"		\
	LDFLAGS="-L$(APACHE_INSTALL_DIR)/lib -R$(APACHE_INSTALL_DIR)/lib -L$(COMMON_INSTALL_DIR)/lib -R$(COMMON_INSTALL_DIR)/lib -Wl,-rpath -Wl,$(APACHE_INSTALL_DIR)/lib:$(COMMON_INSTALL_DIR)/lib"	\
	CPPFLAGS="-I$(COMMON_INSTALL_DIR)/include"	\
	--prefix=$(APACHE_INSTALL_DIR)		\
	--with-included-apr			\
	--enable-so				\
	--enable-mods-shared=most		\
	--disable-auth_dbm			\
	--without-berkeley-db			\
	--enable-deflate			\
	--enable-ssl				\
	--with-ssl=$(COMMON_INSTALL_DIR)	\
	--with-expat=$(COMMON_INSTALL_DIR)	\
	--with-iconv=$(COMMON_INSTALL_DIR)	\
	--with-ldap				\
	--enable-proxy				\
	--enable-proxy-http			\
	--enable-proxy-balancer			\
	--enable-rewrite			\
	--enable-proxy-connect			\
	--enable-proxy-ftp			\
	--enable-proxy-ajp			\
	--enable-apc-mmap			\
	--enable-ldap				\
	--enable-authnz-ldap			\
	--enable-status				\
	--enable-autoindex			\
	--enable-asis				\
	--enable-info				\
	--enable-cgi				\
	--enable-dav-fs				\
	--enable-vhost-alias			\
	--enable-negotiation			\
	--enable-dir				\
	--enable-imagemap			\
	--enable-actions			\
	--enable-speling			\
	--enable-userdir			\
	--enable-alias				\
	--enable-rewrite			\
	--enable-unique-id=shared

# ----------------------------------------------------------------

LUA_MAKE_OPTIONS = \
	INSTALL_TOP=$(COMMON_INSTALL_DIR)

# ----------------------------------------------------------------

# --prefix apparently has no effect with modsec.  Instead, most pathnames
# are determined by apxs.  So we don't bother to take any steps to identify
# which files need to be shipped, using that mechanism.  We do a separate
# investigation to understand what we need to put in our patch distribution.

MODSEC2_CONFIG_OPTIONS = \
	LDFLAGS="-L$(COMMON_INSTALL_DIR)/lib -R$(COMMON_INSTALL_DIR)/lib -Wl,-rpath -Wl,$(COMMON_INSTALL_DIR)/lib"	\
	CPPFLAGS="-I$(COMMON_INSTALL_DIR)/include"		\
	PERL=$(GROUNDWORK_BASE_DIR)/perl/bin/perl		\
	--with-apxs=$(APACHE_INSTALL_DIR)/bin/apxs		\
	--with-pcre=$(COMMON_INSTALL_DIR)/bin/pcre-config	\
	--with-apr=$(COMMON_INSTALL_DIR)/bin/apr-1-config	\
	--with-apu=$(COMMON_INSTALL_DIR)/bin/apu-1-config	\
	--with-libxml=$(COMMON_INSTALL_DIR)/bin/xml2-config	\
	--with-curl=$(COMMON_INSTALL_DIR)/bin/curl-config	\
	--with-lua=$(COMMON_INSTALL_DIR)

# ================================================================
# What gets installed from the modsec build is a bit confusing.
# Lines printed during the install process suggest that all of
# these files will be copied:
#
#	/usr/local/groundwork/apache2/modules/mod_security2.a
#	/usr/local/groundwork/apache2/modules/mod_security2.la
#	/usr/local/groundwork/apache2/modules/mod_security2.so
#
# with these commands:
#
#	cd modsecurity-apache_2.5.11/apache2
#	cp .libs/mod_security2.so /usr/local/groundwork/apache2/modules/mod_security2.so
#	cp .libs/mod_security2.lai /usr/local/groundwork/apache2/modules/mod_security2.la
#	cp .libs/mod_security2.a /usr/local/groundwork/apache2/modules/mod_security2.a
#	ranlib /usr/local/groundwork/apache2/modules/mod_security2.a
#	chmod 644 /usr/local/groundwork/apache2/modules/mod_security2.a
#	chmod 755 /usr/local/groundwork/apache2/modules/mod_security2.so
#
# but in fact the only one we see afterward is:
#
#       /usr/local/groundwork/apache2/modules/mod_security2.so
#
# That's probably fine with us, as the .la and .a files would only be
# used for further build-time linking and static linking, respectively.
# But we also need to track down what happens to the rules that modsec
# ships with, so they are also made part of our distribution.
# ================================================================

DISTRIBUTION_DIRECTORIES_AND_FILES = \
	apache2/bin/mlogc					\
	apache2/bin/mlogc-batch-load.pl				\
	apache2/bin/rules-updater.pl				\
	apache2/conf/mlogc-default.conf				\
	apache2/conf/modsecurity				\
	apache2/conf/rules-updater-example.conf			\
	apache2/doc						\
	apache2/modules/mod_security2.so			\
	apache2/modules/mod_unique_id.so			\
	common/bin/curl						\
	common/bin/curl-config					\
	common/bin/lua						\
	common/bin/luac						\
	common/include/curl					\
	common/include/lauxlib.h				\
	common/include/lua.h					\
	common/include/lua.hpp					\
	common/include/luaconf.h				\
	common/include/lualib.h					\
	common/lib/libcurl.a					\
	common/lib/libcurl.la					\
	common/lib/libcurl.so					\
	common/lib/libcurl.so.4					\
	common/lib/libcurl.so.4.1.1				\
	common/lib/liblua.a					\
	common/lib/liblua5.1.so					\
	common/lib/lua						\
	common/lib/lua/5.1					\
	common/lib/pkgconfig/libcurl.pc				\
	common/man/man1/lua.1					\
	common/man/man1/luac.1					\
	common/share/lua					\
	common/share/lua/5.1					\
	common/share/man/man1/curl-config.1			\
	common/share/man/man1/curl.1				\
	common/share/man/man3/curl_easy_cleanup.3		\
	common/share/man/man3/curl_easy_duphandle.3		\
	common/share/man/man3/curl_easy_escape.3		\
	common/share/man/man3/curl_easy_getinfo.3		\
	common/share/man/man3/curl_easy_init.3			\
	common/share/man/man3/curl_easy_pause.3			\
	common/share/man/man3/curl_easy_perform.3		\
	common/share/man/man3/curl_easy_recv.3			\
	common/share/man/man3/curl_easy_reset.3			\
	common/share/man/man3/curl_easy_send.3			\
	common/share/man/man3/curl_easy_setopt.3		\
	common/share/man/man3/curl_easy_strerror.3		\
	common/share/man/man3/curl_easy_unescape.3		\
	common/share/man/man3/curl_escape.3			\
	common/share/man/man3/curl_formadd.3			\
	common/share/man/man3/curl_formfree.3			\
	common/share/man/man3/curl_formget.3			\
	common/share/man/man3/curl_free.3			\
	common/share/man/man3/curl_getdate.3			\
	common/share/man/man3/curl_getenv.3			\
	common/share/man/man3/curl_global_cleanup.3		\
	common/share/man/man3/curl_global_init.3		\
	common/share/man/man3/curl_global_init_mem.3		\
	common/share/man/man3/curl_mprintf.3			\
	common/share/man/man3/curl_multi_add_handle.3		\
	common/share/man/man3/curl_multi_assign.3		\
	common/share/man/man3/curl_multi_cleanup.3		\
	common/share/man/man3/curl_multi_fdset.3		\
	common/share/man/man3/curl_multi_info_read.3		\
	common/share/man/man3/curl_multi_init.3			\
	common/share/man/man3/curl_multi_perform.3		\
	common/share/man/man3/curl_multi_remove_handle.3	\
	common/share/man/man3/curl_multi_setopt.3		\
	common/share/man/man3/curl_multi_socket.3		\
	common/share/man/man3/curl_multi_socket_action.3	\
	common/share/man/man3/curl_multi_strerror.3		\
	common/share/man/man3/curl_multi_timeout.3		\
	common/share/man/man3/curl_share_cleanup.3		\
	common/share/man/man3/curl_share_init.3			\
	common/share/man/man3/curl_share_setopt.3		\
	common/share/man/man3/curl_share_strerror.3		\
	common/share/man/man3/curl_slist_append.3		\
	common/share/man/man3/curl_slist_free_all.3		\
	common/share/man/man3/curl_strequal.3			\
	common/share/man/man3/curl_unescape.3			\
	common/share/man/man3/curl_version.3			\
	common/share/man/man3/curl_version_info.3		\
	common/share/man/man3/libcurl-easy.3			\
	common/share/man/man3/libcurl-errors.3			\
	common/share/man/man3/libcurl-multi.3			\
	common/share/man/man3/libcurl-share.3			\
	common/share/man/man3/libcurl-tutorial.3		\
	common/share/man/man3/libcurl.3

# ================================================================

howto	:
	@echo
	@echo "GROUNDWORK_RELEASE is:  $(GROUNDWORK_RELEASE)"
	@echo "SUBVERSION_RELEASE is:  $(SUBVERSION_RELEASE)"
	@echo "OS_DISTRIBUTION    is:  $(OS_DISTRIBUTION)"
	@echo "PROCESSOR_TYPE     is:  $(PROCESSOR_TYPE)"
	@echo "PATCH_RELEASE      is:  $(PATCH_RELEASE)"
	@echo "Patch tarball will be:  modsecurity-$(MODSEC2_VERSION)-$(PATCH_RELEASE).tar.gz"
	@echo
	@echo "See the makefile for details on how to build the tarball."
	@echo

all	: curl httpd lua modsec doc

curl-$(CURL_VERSION)	: curl-$(CURL_VERSION).tar.gz
	tar xvfz curl-$(CURL_VERSION).tar.gz

curl	: curl-$(CURL_VERSION)
	cd curl-$(CURL_VERSION); ./configure $(CURL_CONFIG_OPTIONS)
	#
	# Patch libtool so we get the GroundWork library directory in the embedded RPATH
	# before the system directories.  The construction here is probably a bit confused
	# in that it causes different library orderings to be used at various points in
	# the build, but the final result appears to be correct, so we'll go with it.
	cd curl-$(CURL_VERSION);						\
	cp -p libtool libtool.orig;						\
	sed -e 's/"$$finalize_rpath $$libdir"/" $$libdir $$finalize_rpath"/'	\
	    -e 's/"$$compile_rpath$$rpath"/"$$rpath $$compile_rpath"/'		\
	    libtool.orig > libtool
	#
	cd curl-$(CURL_VERSION); make
	(( $(DO_INSTALL) )) && ( cd curl-$(CURL_VERSION); make install )

httpd-$(HTTPD_VERSION)	: httpd-$(HTTPD_VERSION).tar.gz
	tar xvfz httpd-$(HTTPD_VERSION).tar.gz

httpd	: httpd-$(HTTPD_VERSION)
	cd httpd-$(HTTPD_VERSION); ./configure $(HTTPD_CONFIG_OPTIONS)
	cd httpd-$(HTTPD_VERSION); make
	(( $(DO_INSTALL) )) && ( cd httpd-$(HTTPD_VERSION); make install )

lua-$(LUA_VERSION)	: lua-$(LUA_VERSION).tar.gz
	tar xvfz lua-$(LUA_VERSION).tar.gz
	patch -p 0 < lua-5.1.4.patches

lua	: lua-$(LUA_VERSION)
	cd lua-$(LUA_VERSION); make $(LUA_MAKE_OPTIONS) linux
	cd lua-$(LUA_VERSION); make $(LUA_MAKE_OPTIONS) test
	(( $(DO_INSTALL) )) && ( cd lua-$(LUA_VERSION); make $(LUA_MAKE_OPTIONS) install )

modsecurity-apache_$(MODSEC2_VERSION)	: modsecurity-apache_$(MODSEC2_VERSION).tar.gz
	tar xvfz modsecurity-apache_$(MODSEC2_VERSION).tar.gz

modsec	: modsecurity-apache_$(MODSEC2_VERSION)
	cd modsecurity-apache_$(MODSEC2_VERSION)/apache2; ./configure $(MODSEC2_CONFIG_OPTIONS)
	cd modsecurity-apache_$(MODSEC2_VERSION)/apache2; make
	cd modsecurity-apache_$(MODSEC2_VERSION)/apache2; make mlogc
	(( $(DO_INSTALL) )) && (					\
	    cd modsecurity-apache_$(MODSEC2_VERSION)/apache2;		\
	    make install;						\
	)
	(( $(DO_INSTALL) )) && (						\
	    cd modsecurity-apache_$(MODSEC2_VERSION)/tools;			\
	    cp -p mlogc mlogc-batch-load.pl  $(APACHE_INSTALL_DIR)/bin;		\
	    cp -p rules-updater.pl           $(APACHE_INSTALL_DIR)/bin;		\
	    cp -p rules-updater-example.conf $(APACHE_INSTALL_DIR)/conf;	\
	)
	(( $(DO_INSTALL) )) && (					\
	    cd modsecurity-apache_$(MODSEC2_VERSION)/apache2/mlogc-src;	\
	    mkdir -p      $(APACHE_INSTALL_DIR)/doc;			\
	    cp -p INSTALL $(APACHE_INSTALL_DIR)/doc/INSTALL.mlogc;	\
	    cp -p mlogc-default.conf $(APACHE_INSTALL_DIR)/conf;	\
	)
	(( $(DO_INSTALL) )) && (					\
	    cd modsecurity-apache_$(MODSEC2_VERSION)/rules;		\
	    mkdir -p $(APACHE_INSTALL_DIR)/conf/modsecurity;		\
	    cp -pr * $(APACHE_INSTALL_DIR)/conf/modsecurity;		\
	)

doc	:
	(( $(DO_INSTALL) )) && (					\
	    mkdir -p                  $(APACHE_INSTALL_DIR)/doc;	\
	    cp -p INSTALL.modsecurity $(APACHE_INSTALL_DIR)/doc;	\
	)

ownership	:
	-(( $(DO_INSTALL) )) && (( $(I_AM_ROOT) )) && (				\
	    cd $(GROUNDWORK_BASE_DIR);						\
	    chown -R nagios:nagios $(DISTRIBUTION_DIRECTORIES_AND_FILES);	\
	)

tarball	: all ownership
	cd $(GROUNDWORK_BASE_DIR); \
	tar cvfz /tmp/modsecurity-$(MODSEC2_VERSION)-$(PATCH_RELEASE).tar.gz $(DISTRIBUTION_DIRECTORIES_AND_FILES)

# What to do to get the temporary directories set up for a build.
prepare	:
	# Clean up prior state.
	rm -rf $(TEMP_BASE_DIRECTORY)
	# Shut down GroundWork Monitor so all the source files are stable.
	service groundwork stop
	# Copy the source file tree into place.
	mkdir -p $(TEMP_BASE_DIRECTORY)
	# Clean up the existing base installation so that it points to the correct
	# version of the libexpat.so library, corresponding to the Apache httpd
	# package we are compiling.
	if [ "$(HTTPD_VERSION)" = "2.2.9" ]; then				\
	    expat_lib=`readlink $(REAL_BASE_DIRECTORY)/common/lib/libexpat.so`;	\
	    if [ "`expr match $$expat_lib libexpat.so.0 || true`" -eq 0 ]; then	\
	        echo "The libexpat.so link needs modification.";		\
		cd $(REAL_BASE_DIRECTORY)/common/lib;				\
		mv libexpat.so libexpat.so.orig;				\
		ln -s libexpat.so.0.*.* libexpat.so;				\
	    else								\
	        echo "The libexpat.so link is good to go.";			\
	    fi;									\
	else									\
	    expat_lib=`readlink $(REAL_BASE_DIRECTORY)/common/lib/libexpat.so`;	\
	    if [ "`expr match $$expat_lib libexpat.so.0 || true`" -ne 0 ]; then	\
	        echo "The libexpat.so link needs modification.";		\
		cd $(REAL_BASE_DIRECTORY)/common/lib;				\
		mv libexpat.so libexpat.so.orig;				\
		ln -s libexpat.so.1.*.* libexpat.so;				\
	    else								\
	        echo "The libexpat.so link is good to go.";			\
	    fi;									\
	fi
	# We had this copy command in place, until we ran across a build machine
	# where /usr/local/groundwork was a symlink to somewhere else.  That
	# broke the command, so now we do the copying a bit more carefully.
	# cp -pr $(REAL_BASE_DIRECTORY) $(TEMP_BASE_DIRECTORY)/..
	(cd $(REAL_BASE_DIRECTORY); cp -pr . $(TEMP_BASE_DIRECTORY))
	# Allow our ordinary build user to access the files and directories,
	# so these copies may be overwritten as needed during the build.
	chown -R $(BUILD_USER_AND_GROUP) $(TEMP_BASE_DIRECTORY)
	# Modify references in the build tools we use, to point to our temporary copy.
	perl -i -p -e 's{$(REAL_BASE_DIRECTORY)}{$(TEMP_BASE_DIRECTORY)}' $(MODIFIED_BUILD_FILES)

diffs	:
	diff -r $(REAL_BASE_DIRECTORY) $(TEMP_BASE_DIRECTORY) > $(DIFFS_FILE) || true
	chown $(BUILD_USER_AND_GROUP) $(DIFFS_FILE)

clean	:
	rm -rf curl-$(CURL_VERSION) httpd-$(HTTPD_VERSION) lua-$(LUA_VERSION) modsecurity-apache_$(MODSEC2_VERSION)

distclean	: clean
	rm -rf $(TEMP_COMMON_INSTALL_DIR) $(TEMP_APACHE_INSTALL_DIR) $(TEMP_BASE_DIRECTORY)

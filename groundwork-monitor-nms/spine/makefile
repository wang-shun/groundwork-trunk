# makefile for the creation of a Cacti Spine binary that uses
# PostgreSQL instead of MySQL.

# Copyright (c) 2011 by GroundWork Open Source, Inc.  All rights reserved.
# Use is subject to GroundWork commercial license terms.

# ================================================================

# Adjust the choices in this section as needed.

# Here you must choose which type of database you want to build for,
# either "mysql" or "postgresql".
# DBTYPE = mysql
DBTYPE = postgresql

# These definitions might need redefinition on the "make" command line,
# for example if your compilation environment does not provide MySQL or
# PostgreSQL include files and libraries under these paths at the time
# of the build.
SNMP_COMPILE_BASE       = /usr/local/groundwork/common
MYSQL_COMPILE_BASE      = /usr/local/groundwork/mysql
POSTGRESQL_COMPILE_BASE = /usr/local/groundwork/postgresql
GROUNDWORK_COMPILE_BASE = /usr/local/groundwork/common

# This is the base directory for the final install.
INSTALL_PREFIX = /usr/local/groundwork/common

# This is the Spine distribution we are compiling against, relative to
# the directory in which the makefile you are reading lives.
SPINE_BASE = cacti-spine-0.8.7g

# For what we need here (just a little build-time automated file editing),
# the system perl could be used instead.
PERL = /usr/local/groundwork/perl/bin/perl

# ================================================================

# Fixed and derivative configuration settings.
# You should not need to change any definitions in the rest of this makefile.

MYSQL_INCLUDE           = $(MYSQL_COMPILE_BASE)/include
MYSQL_LIB               = $(MYSQL_COMPILE_BASE)/lib
POSTGRESQL_INCLUDE      = $(POSTGRESQL_COMPILE_BASE)/include
POSTGRESQL_LIB          = $(POSTGRESQL_COMPILE_BASE)/lib
GROUNDWORK_INCLUDE      = $(GROUNDWORK_COMPILE_BASE)/include
GROUNDWORK_LIB          = $(GROUNDWORK_COMPILE_BASE)/lib

# Wherever we get the SNMP library and the MySQL and/or PostgreSQL client
# library from at link time, which may not be the final deployment location,
# we want to force the RPATH values specified in the compiled file to point
# to where these libraries actually live when deployed.  So these definitions
# MUST NOT be overridden on the command line during the build.
MYSQL_LINK_BASE         = /usr/local/groundwork/mysql
POSTGRESQL_LINK_BASE    = /usr/local/groundwork/postgresql
GROUNDWORK_LINK_BASE    = /usr/local/groundwork/common

MYSQL_LIB_RPATH         = $(MYSQL_LINK_BASE)/lib
POSTGRESQL_LIB_RPATH    = $(POSTGRESQL_LINK_BASE)/lib
GROUNDWORK_LIB_RPATH    = $(GROUNDWORK_LINK_BASE)/lib

# ================================================================

# These definitions should remain intact. 

SPINE_BINARY = $(SPINE_BASE)/spine

SNMP_OPTION  = --with-snmp=$(SNMP_COMPILE_BASE)
MYSQL_OPTION = --with-mysql=$(MYSQL_COMPILE_BASE)

LINKOPT = -Wl,

LDFLAGS = -L$(GROUNDWORK_LIB) $(LINKOPT)-R$(GROUNDWORK_LIB_RPATH)

ifeq ($(DBTYPE),mysql)
	LDFLAGS += -L$(MYSQL_LIB) $(LINKOPT)-R$(MYSQL_LIB_RPATH)
	EDIT_MAKEFILE = true
else ifeq ($(DBTYPE),postgresql)
	CFLAGS = -DUSE_POSTGRESQL=1 -I../postgresql_as_mysql -I$(POSTGRESQL_INCLUDE)
	LIBS = ../postgresql_as_mysql/postgresql_as_mysql.o -lpq
	LDFLAGS += -L$(POSTGRESQL_LIB) $(LINKOPT)-R$(POSTGRESQL_LIB_RPATH)
	EDIT_MAKEFILE = $(PERL) -pi.mysql_version -e 's/-lmysqlclient_r//g' Makefile
endif

# ================================================================

default	: postgresql_as_mysql_emulation spine validate

postgresql_as_mysql_emulation	:
	cd postgresql_as_mysql && $(MAKE)

spine	: $(SPINE_BINARY)

# Editing the Makefile after ./configure is run is a bit of a hack.  We
# do it simply because the normal configuration process (as a result of
# the --with-mysql definition) includes -lmysqlclient_r in the definition
# of LIBS in the Makefile, when it really doesn't belong there if we are
# compiling for PostgreSQL support instead.  Better would be to construct
# $(SPINE_BASE)/configure.ac to have some option to avoid this, but that
# will await later development.
$(SPINE_BINARY)	:
	cd $(SPINE_BASE) &&			\
	./configure --prefix=$(INSTALL_PREFIX)	\
	    $(SNMP_OPTION)			\
	    $(MYSQL_OPTION)			\
	    CFLAGS="$(CFLAGS)"			\
	    LIBS="$(LIBS)"			\
	    LDFLAGS="$(LDFLAGS)" &&		\
	$(EDIT_MAKEFILE) &&			\
	$(MAKE) $(@F)

# We don't make the "validate" target depend on $(SPINE_BINARY),
# though logically we could, because we don't want an attempt to
# validate to automatically trigger the creation of the file.
# Instead, we check explicitly for the existence of that prerequisite.
validate	:
	@if [ ! -f $(SPINE_BINARY) ]; then							\
	    echo "ERROR:  $(SPINE_BINARY) does not exist and thus cannot be validated.";	\
	    exit 1;										\
	fi
	@if ldd $(SPINE_BINARY) | fgrep 'not found'; then		\
	    echo "ERROR:  Some libraries are missing from the spine binary.";	\
	    exit 1;							\
	fi
	@if ldd $(SPINE_BASE)/spine | fgrep libnetsnmp.so | fgrep -v /usr/local/groundwork/; then		\
	    echo "ERROR:  The spine binary is linked to the wrong copy of the libnetsnmp.so library.";		\
	    exit 1;												\
	fi
	@if ldd $(SPINE_BASE)/spine | fgrep libmysqlclient_r.so | fgrep -v /usr/local/groundwork/; then	\
	    echo "ERROR:  The spine binary is linked to the wrong copy of the libmysqlclient_r.so library.";	\
	    exit 1;												\
	fi
	@if ldd $(SPINE_BASE)/spine | fgrep libcrypto.so | fgrep -v /usr/local/groundwork/; then		\
	    echo "ERROR:  The spine binary is linked to the wrong copy of the libcrypto.so library.";		\
	    exit 1;												\
	fi
	@if ldd $(SPINE_BASE)/spine | fgrep libz.so | fgrep -v /usr/local/groundwork/; then		\
	    echo "ERROR:  The spine binary is linked to the wrong copy of the libz.so library.";		\
	    exit 1;												\
	fi

clean	:
	rm -f $(SPINE_BASE)/Makefile.mysql_version
	cd postgresql_as_mysql && make clean
	cd $(SPINE_BASE)       && make clean

distclean	:
	rm -f $(SPINE_BASE)/Makefile.mysql_version
	cd postgresql_as_mysql && make distclean
	cd $(SPINE_BASE)       && make distclean

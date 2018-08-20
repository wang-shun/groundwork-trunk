# makefile for commit_check, add_check, and check_hosts

# Copyright (c) 2010-2017 by GroundWork Open Source, Inc.
# All rights reserved.

# Choose your weapon(s) [define each as either 0 or 1].
# FIX MINOR:  For commit_check and add_check, the obfuscator currently cannot
# handle the case where these are not both defined as 1, because of the way
# it hoists code out of #if blocks.  So for the time being, we must compile
# with support for both databases.
SUPPORT_MYSQL		= 1
SUPPORT_POSTGRESQL	= 1

# These definitions might need redefinition on the "make" command line,
# if your compilation environment does not provide MySQL or PostgreSQL
# include files and libraries under these paths at the time of the build.
MYSQL_INCLUDE		= /usr/local/groundwork/mysql/include
MYSQL_LIB		= /usr/local/groundwork/mysql/lib
POSTGRESQL_INCLUDE	= /usr/local/groundwork/postgresql/include
POSTGRESQL_LIB		= /usr/local/groundwork/postgresql/lib

# Wherever we get the MySQL and/or PostgreSQL client library from at link time,
# which may not be the final deployment location, we want to force the RPATH
# specified in the compiled file to point to where it actually lives when
# deployed.  So these definitions MUST NOT be overridden on the command line
# during the build.
MYSQL_LIB_RPATH		= /usr/local/groundwork/mysql/lib
POSTGRESQL_LIB_RPATH	= /usr/local/groundwork/postgresql/lib

MYSQL_DEFINES			= -DSUPPORT_MYSQL=$(SUPPORT_MYSQL)
MYSQL_INCLUDES			= -I$(MYSQL_INCLUDE)
MYSQL_LINK_OPTIONS		= -L$(MYSQL_LIB) $(LINKOPT)-R$(MYSQL_LIB_RPATH)
MYSQL_LIBRARIES_TO_LINK		= -lmysqlclient_r

POSTGRESQL_DEFINES		= -DSUPPORT_POSTGRESQL=$(SUPPORT_POSTGRESQL)
POSTGRESQL_INCLUDES		= -I$(POSTGRESQL_INCLUDE)
POSTGRESQL_LINK_OPTIONS		= -L$(POSTGRESQL_LIB) $(LINKOPT)-R$(POSTGRESQL_LIB_RPATH)
POSTGRESQL_LIBRARIES_TO_LINK	= -lpq

ALL_DEFINES		= $(MYSQL_DEFINES)           $(POSTGRESQL_DEFINES)
ALL_INCLUDES		= $(MYSQL_INCLUDES)          $(POSTGRESQL_INCLUDES)
ALL_LINK_OPTIONS	= $(MYSQL_LINK_OPTIONS)      $(POSTGRESQL_LINK_OPTIONS)
ALL_LIBRARIES_TO_LINK	= $(MYSQL_LIBRARIES_TO_LINK) $(POSTGRESQL_LIBRARIES_TO_LINK)

# These definitions should remain intact.
LDFLAGS	= -s
LINKOPT	= -Wl,

default	: commit_check add_check check_hosts

commit_check	: scrambled.c
	$(CC) -o commit_check scrambled.c -DCOMMIT_CHECK $(ALL_DEFINES) $(ALL_INCLUDES) $(LDFLAGS) $(ALL_LINK_OPTIONS) $(ALL_LIBRARIES_TO_LINK)

add_check	: scrambled.c
	$(CC) -o add_check scrambled.c -DADD_CHECK $(ALL_DEFINES) $(ALL_INCLUDES) $(LDFLAGS) $(ALL_LINK_OPTIONS) $(ALL_LIBRARIES_TO_LINK)

check_hosts	: check_hosts_scrambled.c
	$(CC) -o check_hosts check_hosts_scrambled.c $(POSTGRESQL_DEFINES) $(POSTGRESQL_INCLUDES) $(LDFLAGS) $(POSTGRESQL_LINK_OPTIONS) $(POSTGRESQL_LIBRARIES_TO_LINK)

scrambled.c	: makefile obfuscate commit_check.c
	./obfuscate commit_check.c > scrambled.c

check_hosts_scrambled.c	: makefile obfuscate2 check_hosts.c
	./obfuscate2 check_hosts.c > check_hosts_scrambled.c

clean	:
	rm -f commit_check add_check check_hosts scrambled.c check_hosts_scrambled.c

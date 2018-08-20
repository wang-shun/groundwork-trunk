# makefile for the creation of a library emulating the MySQL client API
# but accessing a PostgreSQL database instead

# Copyright (c) 2011 by GroundWork Open Source, Inc.  All rights reserved.
# Use is subject to GroundWork commercial license terms.

# ================================================================

# These definitions might need redefinition on the "make" command line,
# if your compilation environment does not provide MySQL or PostgreSQL
# include files and libraries under these paths at the time of the build.
POSTGRESQL_INCLUDE = /usr/local/groundwork/postgresql/include

# ================================================================

INCLUDES = -I$(POSTGRESQL_INCLUDE)

# ================================================================

default	: postgresql_as_mysql.o

postgresql_as_mysql.o	: postgresql_as_mysql.c postgresql_as_mysql.h
	$(CC) -Wall -c postgresql_as_mysql.c $(INCLUDES)

clean	:
	rm -f postgresql_as_mysql.o

distclean	: clean

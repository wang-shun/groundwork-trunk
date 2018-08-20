# makefile for log-archive distributions
# (prior to this capability being folded into the base GWMEE product)

# Copyright (c) 2013 GroundWork, Inc. (www.gwos.com).  All rights reserved.
# Use is subject to GroundWork commercial license terms.

# FIX MAJOR:  include a copy of core/services3/gwservices in some new
# tarball, designed to be installed specifically on the archive server
# (not the target server, unless it happens to be the same machine)

TOP_BUILD_DIR            = $(CURDIR)
SEND_BUILD_ROOT          = send_build_root
RECEIVE_BUILD_ROOT       = receive_build_root
ARCHIVE_DB_BUILD_ROOT    = archive_db_build_root
BUILD_TARGET_ROOT        = build
RELATIVE_GWPATH          = usr/local/groundwork
RELATIVE_ARCHIVEPATH     = $(RELATIVE_GWPATH)/core/archive
RELATIVE_ARCHIVE_DBPATH  = $(RELATIVE_GWPATH)/core/databases/postgresql
SEND_BUILD_VERSION       := $(shell bin/log-archive-send.pl            -v | awk '/Version:/{print $$NF}')
RECEIVE_BUILD_VERSION    := $(shell bin/log-archive-receive.pl         -v | awk '/Version:/{print $$NF}')
ARCHIVE_DB_BUILD_VERSION := $(shell scripts/set-up-archive-database.sh -v | awk '/Version:/{print $$NF}')
SEND_BUILD_TARGET        = $(BUILD_TARGET_ROOT)/log-archive-send-$(SEND_BUILD_VERSION).tar.gz
RECEIVE_BUILD_TARGET     = $(BUILD_TARGET_ROOT)/log-archive-receive-$(RECEIVE_BUILD_VERSION).tar.gz
ARCHIVE_DB_BUILD_TARGET  = $(BUILD_TARGET_ROOT)/log-archive-db-$(ARCHIVE_DB_BUILD_VERSION).tar.gz

ARCHIVE_CONTROL_PROGRAM  = src/control_archive_gwservices

SEND_DIRECTORIES = \
	$(RELATIVE_ARCHIVEPATH) \
	$(RELATIVE_ARCHIVEPATH)/bin \
	$(RELATIVE_ARCHIVEPATH)/log-archive \
	$(RELATIVE_ARCHIVEPATH)/var

# FIX MINOR:  The logs/ symlink should be part of the send tarball, but only once we reconcile
# the fact that it will be placed in an existing logs/ directory that is owned by root, not by
# nagios, and nagios has no permissions to write a new symlink into that directory.  Since we
# want the send tarball to be unpacked by the nagios user, we leave out the symlink for now.
#	$(RELATIVE_GWPATH)/logs/log-archive-send.log \

SEND_FILES = \
	$(RELATIVE_ARCHIVE_DBPATH)/defragment-runtime-database.sh \
	$(RELATIVE_ARCHIVE_DBPATH)/make-archive-application-type.pl \
	$(RELATIVE_GWPATH)/config/log-archive-send.conf \
	$(RELATIVE_ARCHIVEPATH)/bin/log-archive-send.pl \
	$(RELATIVE_ARCHIVEPATH)/var/log-archive-send.state

RECEIVE_DIRECTORIES = \
	$(RELATIVE_ARCHIVEPATH) \
	$(RELATIVE_ARCHIVEPATH)/bin \
	$(RELATIVE_ARCHIVEPATH)/log-archive \
	$(RELATIVE_ARCHIVEPATH)/var

# FIX MINOR:  The logs/ symlink should be part of the receive tarball, but only once we reconcile
# the fact that it will be placed in an existing logs/ directory that is owned by root, not by
# nagios, and nagios has no permissions to write a new symlink into that directory.  Since we
# want the receive tarball to be unpacked by the nagios user, we leave out the symlink for now.
#	$(RELATIVE_GWPATH)/logs/log-archive-receive.log \

RECEIVE_FILES = \
	$(RELATIVE_GWPATH)/config/log-archive-receive.conf \
	$(RELATIVE_ARCHIVEPATH)/bin/log-archive-receive.pl \
	$(RELATIVE_ARCHIVEPATH)/bin/control_archive_gwservices \
	$(RELATIVE_ARCHIVEPATH)/var/log-archive-receive.state

ARCHIVE_DB_DIRECTORIES =

ARCHIVE_DB_FILES = \
	$(RELATIVE_ARCHIVE_DBPATH)/Archive_GWCollageDB_extensions.sql \
	$(RELATIVE_ARCHIVE_DBPATH)/create-fresh-archive-databases.sql \
	$(RELATIVE_ARCHIVE_DBPATH)/postgres-xtra-functions.sql \
	$(RELATIVE_ARCHIVE_DBPATH)/set-up-archive-database.sh

default	: $(SEND_BUILD_TARGET) $(RECEIVE_BUILD_TARGET) $(ARCHIVE_DB_BUILD_TARGET)

# We make sure that all new directories not included in the base product are included in the tarballs.

# We would like to put 644 permissions on the log-archive-send.state and log-archive-receive.state files,
# as that's all the protection we logically need, but we're currently using TypedConfig::secure_new() to
# read them (as a way of enforcing no world-writable permissions), and that requires 600 permissions.
# When we provide an intermediate level of trust (e.g., TypedConfig::protected_new() instead), we can
# relax the permissions on these files back to 644.

$(SEND_BUILD_TARGET)	:
	rm -rf $(SEND_BUILD_ROOT)
	@:
	mkdir -p -m 755                                $(SEND_BUILD_ROOT)/$(RELATIVE_ARCHIVE_DBPATH)
	cp -p scripts/defragment-runtime-database.sh   $(SEND_BUILD_ROOT)/$(RELATIVE_ARCHIVE_DBPATH)
	cp -p scripts/make-archive-application-type.pl $(SEND_BUILD_ROOT)/$(RELATIVE_ARCHIVE_DBPATH)
	chmod 750                                      $(SEND_BUILD_ROOT)/$(RELATIVE_ARCHIVE_DBPATH)/defragment-runtime-database.sh
	chmod 750                                      $(SEND_BUILD_ROOT)/$(RELATIVE_ARCHIVE_DBPATH)/make-archive-application-type.pl
	@:
	mkdir -p -m 755                    $(SEND_BUILD_ROOT)/$(RELATIVE_GWPATH)/config
	cp -p config/log-archive-send.conf $(SEND_BUILD_ROOT)/$(RELATIVE_GWPATH)/config
	chmod 600                          $(SEND_BUILD_ROOT)/$(RELATIVE_GWPATH)/config/log-archive-send.conf
	@:
	mkdir -p -m 755                                         $(SEND_BUILD_ROOT)/$(RELATIVE_GWPATH)/logs
	ln -s ../foundation/container/logs/log-archive-send.log $(SEND_BUILD_ROOT)/$(RELATIVE_GWPATH)/logs
	@:
	mkdir -p -m 755               $(SEND_BUILD_ROOT)/$(RELATIVE_ARCHIVEPATH)/bin
	cp -p bin/log-archive-send.pl $(SEND_BUILD_ROOT)/$(RELATIVE_ARCHIVEPATH)/bin
	chmod 750                     $(SEND_BUILD_ROOT)/$(RELATIVE_ARCHIVEPATH)/bin/log-archive-send.pl
	@:
	mkdir -p -m 755                  $(SEND_BUILD_ROOT)/$(RELATIVE_ARCHIVEPATH)/var
	cp -p var/log-archive-send.state $(SEND_BUILD_ROOT)/$(RELATIVE_ARCHIVEPATH)/var
	chmod 600                        $(SEND_BUILD_ROOT)/$(RELATIVE_ARCHIVEPATH)/var/log-archive-send.state
	@:
	mkdir -p -m 755 $(SEND_BUILD_ROOT)/$(RELATIVE_ARCHIVEPATH)/log-archive
	@:
	mkdir -p $(BUILD_TARGET_ROOT)
	cd $(SEND_BUILD_ROOT); tar cvfz $(TOP_BUILD_DIR)/$@ --no-recursion $(SEND_DIRECTORIES) $(SEND_FILES)

$(RECEIVE_BUILD_TARGET)	: $(ARCHIVE_CONTROL_PROGRAM)
	rm -rf $(RECEIVE_BUILD_ROOT)
	@:
	mkdir -p -m 755                       $(RECEIVE_BUILD_ROOT)/$(RELATIVE_GWPATH)/config
	cp -p config/log-archive-receive.conf $(RECEIVE_BUILD_ROOT)/$(RELATIVE_GWPATH)/config
	chmod 600                             $(RECEIVE_BUILD_ROOT)/$(RELATIVE_GWPATH)/config/log-archive-receive.conf
	@:
	mkdir -p -m 755                                            $(RECEIVE_BUILD_ROOT)/$(RELATIVE_GWPATH)/logs
	ln -s ../foundation/container/logs/log-archive-receive.log $(RECEIVE_BUILD_ROOT)/$(RELATIVE_GWPATH)/logs
	@:
	mkdir -p -m 755                  $(RECEIVE_BUILD_ROOT)/$(RELATIVE_ARCHIVEPATH)/bin
	cp -p bin/log-archive-receive.pl $(RECEIVE_BUILD_ROOT)/$(RELATIVE_ARCHIVEPATH)/bin
	cp -p $(ARCHIVE_CONTROL_PROGRAM) $(RECEIVE_BUILD_ROOT)/$(RELATIVE_ARCHIVEPATH)/bin
	chmod 750                        $(RECEIVE_BUILD_ROOT)/$(RELATIVE_ARCHIVEPATH)/bin/log-archive-receive.pl
	chmod 750                        $(RECEIVE_BUILD_ROOT)/$(RELATIVE_ARCHIVEPATH)/bin/control_archive_gwservices
	@:
	mkdir -p -m 755                     $(RECEIVE_BUILD_ROOT)/$(RELATIVE_ARCHIVEPATH)/var
	cp -p var/log-archive-receive.state $(RECEIVE_BUILD_ROOT)/$(RELATIVE_ARCHIVEPATH)/var
	chmod 600                           $(RECEIVE_BUILD_ROOT)/$(RELATIVE_ARCHIVEPATH)/var/log-archive-receive.state
	@:
	mkdir -p -m 755 $(RECEIVE_BUILD_ROOT)/$(RELATIVE_ARCHIVEPATH)/log-archive
	@:
	mkdir -p $(BUILD_TARGET_ROOT)
	cd $(RECEIVE_BUILD_ROOT); tar cvfz $(TOP_BUILD_DIR)/$@ --no-recursion $(RECEIVE_DIRECTORIES) $(RECEIVE_FILES)

$(ARCHIVE_DB_BUILD_TARGET)	:
	rm -rf $(ARCHIVE_DB_BUILD_ROOT)
	@:
	mkdir -p -m 755                                  $(ARCHIVE_DB_BUILD_ROOT)/$(RELATIVE_ARCHIVE_DBPATH)
	cp -p scripts/Archive_GWCollageDB_extensions.sql $(ARCHIVE_DB_BUILD_ROOT)/$(RELATIVE_ARCHIVE_DBPATH)
	cp -p scripts/create-fresh-archive-databases.sql $(ARCHIVE_DB_BUILD_ROOT)/$(RELATIVE_ARCHIVE_DBPATH)
	cp -p scripts/postgres-xtra-functions.sql        $(ARCHIVE_DB_BUILD_ROOT)/$(RELATIVE_ARCHIVE_DBPATH)
	cp -p scripts/set-up-archive-database.sh         $(ARCHIVE_DB_BUILD_ROOT)/$(RELATIVE_ARCHIVE_DBPATH)
	chmod 644                                        $(ARCHIVE_DB_BUILD_ROOT)/$(RELATIVE_ARCHIVE_DBPATH)/Archive_GWCollageDB_extensions.sql
	chmod 644                                        $(ARCHIVE_DB_BUILD_ROOT)/$(RELATIVE_ARCHIVE_DBPATH)/create-fresh-archive-databases.sql
	chmod 644                                        $(ARCHIVE_DB_BUILD_ROOT)/$(RELATIVE_ARCHIVE_DBPATH)/postgres-xtra-functions.sql
	chmod 750                                        $(ARCHIVE_DB_BUILD_ROOT)/$(RELATIVE_ARCHIVE_DBPATH)/set-up-archive-database.sh
	@:
	mkdir -p $(BUILD_TARGET_ROOT)
	cd $(ARCHIVE_DB_BUILD_ROOT); tar cvfz $(TOP_BUILD_DIR)/$@ --no-recursion $(ARCHIVE_DB_DIRECTORIES) $(ARCHIVE_DB_FILES)

$(ARCHIVE_CONTROL_PROGRAM)	:
	cd src; make

clean	:
	rm -rf $(BUILD_TARGET_ROOT)
	rm -rf $(SEND_BUILD_ROOT)
	rm -rf $(RECEIVE_BUILD_ROOT)
	rm -rf $(ARCHIVE_DB_BUILD_ROOT)
	cd src; make clean

distclean	:
	clean

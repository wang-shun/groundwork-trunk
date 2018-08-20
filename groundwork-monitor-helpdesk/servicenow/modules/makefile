# makefile for local Perl modules used in GroundWork HelpDesk integrations

# Copyright 2011-2015 GroundWork Open Source, Inc. ("GroundWork").  All rights
# reserved.  Use is subject to GroundWork commercial license terms.

#---------------------------------------------------------------#

MODULE_DIRECTORIES =	\
	GW		\
	ServiceNow

# We no longer include GW::Nagios in the ServiceNow integration, because
# a later version of this package is now part of the base GWMEE release.
# There's no sense in getting the two versions confused.
EXCLUDED_FILES =	\
	GW/Nagios.pm

EXCLUDE_FILES = $(EXCLUDED_FILES:%=--exclude %)

#---------------------------------------------------------------#

all		: perl_modules

../perl/lib	:
		mkdir -p ../perl/lib

perl_modules	: ../perl/lib
		# We don't use "cp -pr" here for the recursive copying because we
		# need to exclude all the Subversion files (.svn/...) in this tree,
		# along with any transient edit-session buffer files (*.swp, for vi).
		(tar cf - --exclude .svn --exclude '*.swp' $(EXCLUDE_FILES) $(MODULE_DIRECTORIES)) | (cd ../perl/lib ; tar xvpBf -)

clean		:

distclean	: clean

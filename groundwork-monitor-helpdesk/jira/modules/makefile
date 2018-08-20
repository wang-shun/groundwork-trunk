# makefile for local Perl modules used in GroundWork HelpDesk integrations

# Copyright 2013 GroundWork Open Source, Inc. ("GroundWork").  All rights
# reserved.  Use is subject to GroundWork commercial license terms.

#---------------------------------------------------------------#

MODULE_DIRECTORIES =	\
	GW		\
	JIRA

#---------------------------------------------------------------#

all		: perl_modules

../perl/lib	:
		mkdir -p ../perl/lib

perl_modules	: ../perl/lib
		# We don't use "cp -pr" here for the recursive copying because we
		# need to exclude all the Subversion files (.svn/...) in this tree,
		# along with any transient edit-session buffer files (*.swp, for vi).
		(tar cf - --exclude .svn --exclude '*.swp' $(MODULE_DIRECTORIES)) | (cd ../perl/lib ; tar xvpBf -)

clean		:

distclean	: clean

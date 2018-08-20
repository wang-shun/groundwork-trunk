# makefile for local Perl modules used in the GroundWork Webmetrics integration

# Copyright 2011 GroundWork Open Source, Inc. ("GroundWork").  All rights
# reserved.  Use is subject to GroundWork commercial license terms.

#---------------------------------------------------------------#

MODULE_DIRECTORIES =	\
	GW

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

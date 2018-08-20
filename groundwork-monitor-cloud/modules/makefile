# makefile for local Perl modules used in GroundWork DR replication

# Copyright 2010 GroundWork Open Source, Inc. ("GroundWork").  All rights
# reserved.  Use is subject to GroundWork commercial license terms.

#---------------------------------------------------------------#

all		: perl_modules

../perl/lib	:
		mkdir -p ../perl/lib

perl_modules	: ../perl/lib
		# We don't use "cp -pr" here for the recursive copying because we
		# need to exclude all the Subversion files (.svn/...) in this tree,
		# along with any transient edit-session buffer files (*.swp, for vi).
		(tar cf - --exclude .svn --exclude '*.swp' Clouds) | (cd ../perl/lib ; tar xvpBf -)
		# The un-tar above will delete and re-create any existing file, so the
		# fact that we establish read-only permissions won't prevent an update
		# if this is re-run in the future.
		cd ../perl/lib/Clouds; chmod -R a-w *

clean		:

distclean	: clean

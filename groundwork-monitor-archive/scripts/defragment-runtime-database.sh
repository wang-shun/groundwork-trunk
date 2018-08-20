#!/bin/bash

# Script to defragment a runtime database.

# Copyright (c) 2013 GroundWork, Inc. (www.gwos.com).  All rights reserved.
# Use is subject to GroundWork commercial license terms.

# FIX MAJOR:  Should we have some sort of option to report errors to syslog?

version="0.0.6"

print_usage() {
    echo "usage:  $0 -v"
    echo "        $0 [-d] dbhost"
    echo "where:  -v prints the version of this script"
    echo "        -d prints more detail of the results of the database commands"
    echo "        dbhost is the machine on which the gwcollagedb database resides"
}

if [ $# -lt 1 -o $# -gt 2 ]; then
    print_usage
    exit 1
fi

detail=0
if [ "$1" = "-d" ]; then
    detail=1
    shift
fi

if [ $# -ne 1 ]; then
    print_usage
    exit 1
fi

if [ "$1" = "-v" ]; then
    echo "Version: $version"
    exit 0
fi

dbhost="$1"

echo "Set up the environment ..."

source /usr/local/groundwork/scripts/setenv.sh

outcome=1

# The VACUUM and REINDEX commands we invoke here seem to be inherently noisy, even if we don't
# use the VACUUM VERBOSE option, and the psql --quiet option doesn't subdue most of that output.

if [ $detail -ne 0 ]; then
    verbosity_opts="-v VERBOSITY=default"
    vacuum_verbosity=", VERBOSE"
else
    verbosity_opts="-q -v VERBOSITY=terse"
    vacuum_verbosity=""
fi

# We have psql never issue a password prompt, because you should only be running this script as
# the nagios user, which will have database access already set up.

if [ $outcome = 1 ]; then
    echo "Vacuuming the runtime database ..."
    psql -h "$dbhost" -w $verbosity_opts -v ON_ERROR_STOP= -c "VACUUM (FULL, ANALYZE $vacuum_verbosity);" gwcollagedb collage 2>&1
    if [ $? -ne 0 ]; then
	echo "ERROR:  Failed to vacuum the runtime database.";
	outcome=0
    fi
fi

if [ $outcome = 1 ]; then
    echo "Reindexing all objects in the runtime database ..."
    psql -h "$dbhost" -w $verbosity_opts -v ON_ERROR_STOP= -c "REINDEX DATABASE gwcollagedb;" gwcollagedb collage 2>&1
    if [ $? -ne 0 ]; then
	echo "ERROR:  Failed to reindex the runtime database.";
	outcome=0
    fi
fi

if [ $outcome = 1 ]; then
    echo "Done defragmenting the runtime database."
    exit 0;
fi

# Here, failure is the default exit status.  We have to affirmatively conclude
# above that everything worked okay in order to return a success status.
exit 1;


#!/bin/bash

# Script to build an archive_gwcollagedb database.

# Copyright (c) 2013 GroundWork, Inc. (www.gwos.com).  All rights reserved.
# Use is subject to GroundWork commercial license terms.

version="0.0.6"

if [ $# -ne 1 ]; then
    echo "usage:  $0 -v"
    echo "        $0 dbhost"
    echo "where:  -v prints the version of this script"
    echo "        dbhost is the machine on which the archive_gwcollagedb"
    echo "            database is to reside"
    exit 1
fi

if [ "$1" = "-v" ]; then
    echo "Version: $version"
    exit 0
fi

dbhost="$1"

echo ""
echo "In the following steps, you will be asked several times to enter some"
echo "unspecified password.  In each case, the request is for the database"
echo "password of the postgres database-user.  This is necessary to run"
echo "certain privileged operations to create the archive_gwcollagedb"
echo "database and populate it with certain minimal data."
echo ""

echo "Setting up the environment ..."

source /usr/local/groundwork/scripts/setenv.sh

outcome=1

# This script is potentially very destructive.  If you run it when you already
# have an archive_gwcollagedb database on $dbhost, there is a danger that
# the database will be completely destroyed, and a fresh clean copy will be
# constructed.  To prevent such automatic severe tire damage, we implement a
# safety check here, to first reach out and see if the database exists.  If it
# does, the user is asked whether that entire database should be destroyed,
# before proceeding.  There must be no default answer to the question, and we
# must only accept the complete word "yes" as an affirmative response, not just
# a single-character "y" response.  Any other response must cause this script
# to abort without making any changes whatsoever.
#
# We assume the worst, in a certain sense (that is, an already-existing database),
# and must affirmatively deny this assumption by explicit checking, to proceed
# with creating and populating a new copy of the database.
have_archive_gwcollagedb=1

if [ $outcome = 1 ]; then
    echo
    echo "Checking for the prior existence of the archive_gwcollagedb database ..."
    # There are two separate possible outcomes for an unsuccessful attempt to
    # connect to the archive_gwcollagedb database, and to avoid damaging an
    # existing database, we must carefully distinguish between them.
    #
    # (1) Perhaps we simply cannot contact PostgreSQL, at this instant.
    #     The entire PostgreSQL instance might be briefly down, or temporarily
    #     inaccessible because of network problems.  We must be careful not
    #     to believe that this means the database does not exist, since if we
    #     go on and try to create it, we could destroy its existing content.
    #
    # (2) If we can contact PostgreSQL but the database does not exist, we
    #     should receive back a specific error value that tells of the absence
    #     of this database.  Specifically, we should see:
    #
    #         'psql.bin: FATAL:  database "archive_gwcollagedb" does not exist'
    #
    #     along with an exit code of 2.
    #
    output=`psql -h localhost -v ON_ERROR_STOP= -c "select 'successful connection' as \"Connection Status\";" archive_gwcollagedb postgres 2>&1`
    # Here, we need to capture the database-access execution status right away so we
    # can probe it multiple times later on while knowing that we are consistently
    # testing a stable value and not the value from some other command.
    status=$?
    if [ $status -eq 0 ]; then
        # We successfully connected to the database.  There is nothing further
	# to do now in this script, unless the user really wants to wipe out
	# the old data and start afresh.  We give the user the opportunity to
	# do so below, but we're careful to only accept strict confirmation
	# and to time out the supplying of that confirmation.
	echo
	echo "$output"
	echo
	echo "WARNING:  You already have an existing archive_gwcollagedb database on $dbhost.";
	have_archive_gwcollagedb=1
    elif [ $status -eq 2 -a "$output" = 'psql.bin: FATAL:  database "archive_gwcollagedb" does not exist' ]; then
	# This is not a failure per se.  It simply means that this script should
	# continue on to create and populate the archive_gwcollagedb database.
	echo
	echo "NOTICE:  You currently have no archive_gwcollagedb database on $dbhost."
	echo "         The set-up-archive-database.sh script will attempt to"
	echo "         create and populate this database."
	have_archive_gwcollagedb=0
    else
	echo
	echo "ERROR:  Failed to connect to PostgreSQL on $dbhost:"
	echo
	echo "$output"
	echo
	echo "FATAL:  The set-up-archive-database.sh script is aborting"
	echo "        without creating the archive_gwcollagedb database."
	echo
	exit 1
    fi
fi

if [ $have_archive_gwcollagedb -ne 0 ]; then
    echo ""
    echo "================================================================="
    echo "  WARNING:  This script will destroy any existing copy of the"
    echo "  archive_gwcollagedb database on the $dbhost machine."
    echo "  Only an affirmative answer of \"yes\", fully spelled out,"
    echo "  will get you past the following question, destroy any"
    echo "  existing copy of the database, and create a fresh copy."
    echo "  You have 30 seconds to answer."
    echo "================================================================="
    echo ""
    # This prompt is displayed only if the input is coming from a terminal.
    read -p "Do you wish to continue? " -t 30 answer
    if [ $? -ne 0 ]; then
	echo ""
	echo ""
	echo "The question timed out.  Aborting execution of this script."
	echo "No archive_gwcollagedb database will be created."
	# We intentionallyy return a "success" exit status here, because the database
	# already exists and you didn't answer the question, which is what will happen
	# when this script is run from some other script.
	exit 0
    fi
    if [ "$answer" != "yes" ]; then
	echo ""
	echo "You did not answer affirmatively.  Aborting execution of this script."
	echo "No archive_gwcollagedb database will be created."
	# We intentionally return a "failure" exit statuus here, even though the
	# database already exists, because you did try to answer the question,
	# but you didn't choose to create the database.  In this case, we want
	# to reflect the failure of receiving an affirmative response into the
	# exit status, even though it is likely that this exit status will never
	# be examined.  This way, a failure of the calling cntext can be detected.
	exit 1
    fi
fi

if [ $outcome = 1 ]; then
    echo
    echo "Creating the archive_gwcollagedb database ..."
    psql -h "$dbhost" -v ON_ERROR_STOP= -f /usr/local/groundwork/core/databases/postgresql/create-fresh-archive-databases.sql postgres
    if [ $? -ne 0 ]; then
	echo "ERROR:  Failed to create a fresh archive_gwcollagedb database.";
	outcome=0
    fi
fi

if [ $outcome = 1 ]; then
    echo
    echo "Creating the schema and tables ..."
    psql -h "$dbhost" -v ON_ERROR_STOP= -f /usr/local/groundwork/core/databases/postgresql/GWCollageDB.sql archive_gwcollagedb postgres
    if [ $? -ne 0 ]; then
	echo "ERROR:  Failed to create the archive_gwcollagedb database schema and tables.";
	outcome=0
    fi
fi

if [ $outcome = 1 ]; then
    echo
    echo "Altering some tables and indexes ..."
    psql -h "$dbhost" -v ON_ERROR_STOP= -f /usr/local/groundwork/core/databases/postgresql/Archive_GWCollageDB_extensions.sql archive_gwcollagedb postgres
    if [ $? -ne 0 ]; then
	echo "ERROR:  Failed to alter the archive_gwcollagedb database tables and indexes.";
	outcome=0
    fi
fi

if [ $outcome = 1 ]; then
    # We load some special functions not provided by PostgreSQL, essentially to mimic
    # what is available in MySQL.  These will never be mirrored from the runtime database.
    echo
    echo "Loading required functions ..."
    psql -h "$dbhost" -v ON_ERROR_STOP= -f /usr/local/groundwork/core/databases/postgresql/postgres-xtra-functions.sql archive_gwcollagedb postgres
    if [ $? -ne 0 ]; then
	echo "ERROR:  Failed to create the required database functions.";
	outcome=0
    fi
fi

if [ $outcome = 1 ]; then
    # We only load the minimal amount of seed data into the archive_gwcollagedb
    # database tables, because we never want any information directly loaded into
    # these tables to possibly conflict with similar information loaded later on from
    # the runtime database.  So the only things we load are some overhead information
    # about the version of the database schema being used by the archive_gwcollagedb
    # database.  These things will never be copied from the runtime database.
    echo
    echo "Loading minimal seed data ..."
    psql -h "$dbhost" -v ON_ERROR_STOP= -f /usr/local/groundwork/core/databases/postgresql/GWCollage-Version.sql archive_gwcollagedb postgres
    if [ $? -ne 0 ]; then
	echo "ERROR:  Failed to load the schema version info.";
	outcome=0
    fi
fi

if [ $outcome = 1 ]; then
    echo
    echo "Done creating and populating the archive_gwcollagedb database."
    exit 0;
fi

# Here, failure is the default exit status.  We have to affirmatively conclude
# above that everything worked okay in order to return a success status.
exit 1;


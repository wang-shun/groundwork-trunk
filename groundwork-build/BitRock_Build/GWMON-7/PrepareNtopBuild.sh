#!/bin/bash -ex

# Copyright (c) 2014 GroundWork, Inc.  All Rights Reserved.

# This script builds the few files needed to enable the GroundWork Ntop
# distribution within the GroundWork portal environment.  Compiling the
# Ntop release itself is handled elsewhere.

# First, let's set up to report failure to the calling context if we abort.
# We don't exit here, because the bash -e option above takes care of that for us.
report_error() {
    # We don't generate an email if this happens.
    # That duty will be handled by the calling scripts.
    echo "BUILD FAILED:  There has been an error in preparing the Ntop build files."
}

# Note that our trap will not be invoked if an error occurs within a function,
# although the script will still die in that circumstance.  The bash documentation
# makes no exception for this situation, but experience shows that to be a problem.
# One workaround is to have any functions we define return a non-zero exit code if
# it is only controlled errors that we want to report this way.  A more-robust
# alternative is to also establish this trap separately in each function, to also
# catch and report any unexpected errors.
trap report_error ERR

# This is where we will park the files we extract from Subversion.
# WARNING:  Choose the $NTOP_BUILD_TREE path carefully, because we will
# completely wipe it out before using it!  Be sure to use an absolute
# pathname for the $NTOP_BUILD_TREE definition.
NTOP_BUILD_TREE=/tmp/ntop-build

# Set this to reflect the Subversion credentials we need to export files.
SVN_CREDENTIALS="--username build --password bgwrk08"

# Subversion repository branch name, (defaults to 'trunk').
PRO_ARCHIVE_BRANCH="trunk"
for ARG in "$@" ; do
    PRO_ARCHIVE_BRANCH_ARG="${ARG#PRO_ARCHIVE_BRANCH=}"
    if [ "$PRO_ARCHIVE_BRANCH_ARG" != "$ARG" -a "$PRO_ARCHIVE_BRANCH_ARG" != "" ] ; then
        PRO_ARCHIVE_BRANCH="$PRO_ARCHIVE_BRANCH_ARG"
    fi
done

# Set the default umask to something sensible.
umask 022

# Define a routine that will retry a Subversion export if the first attempt
# fails, to provide some protection against transient errors.
svn_export() {
    # Per the comment above, we must repeat the trap setting in each function.
    trap report_error ERR
    for i in 1 0; do
	if svn export "$@"; then
	    return 0
	elif [ $i -ne 0 ]; then
	    # Maybe this was a transient failure that won't repeat if we wait a bit.
	    sleep 30
	fi
    done
    echo "ERROR:  Subversion export failed."
    # A purposeful failure should trigger our trap and report the error.
    false
}

echo === Starting the Ntop Build

/bin/rm -rf $NTOP_BUILD_TREE
mkdir -p    $NTOP_BUILD_TREE
cd          $NTOP_BUILD_TREE

echo === Stage the New Files
PRO_ARCHIVE=http://geneva/groundwork-professional/$PRO_ARCHIVE_BRANCH

NTOP_GROUNDWORK_SVN_BASE=$PRO_ARCHIVE/monitor-nms/ntop/ntop-3.3.10

svn_export $SVN_CREDENTIALS $NTOP_GROUNDWORK_SVN_BASE/ntop.properties

# For reasons we don't yet understand, exported files don't come with
# sensible permissions bits, so we need to set them explicitly here.
chmod 600 ntop.properties

echo === Change Ownership in the Prepared File Tree

chown nagios:nagios $NTOP_BUILD_TREE/ntop.properties

echo === Ending the Ntop Build

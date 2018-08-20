#!/bin/bash -x

# Copyright (c) 2009-2014 GroundWork Open Source Solutions info@groundworkopensource.com

# BE SURE TO CHANGE THIS FOR A NEW GROUNDWORK MONITOR RELEASE NUMBER!
# This is a version number corresponding to the directory in which this
# script resides (e.g., GWMON-7 for the 7.1.0 release).
GWMEE_VERSION=7
BUILD_MAIL_ADDRESSES="build-info@gwoslabs.com"

# Set this to reflect the Subversion credentials we need to commit files.
SVN_CREDENTIALS="--username build --password bgwrk08"

# Subversion repository branch name, (defaults to 'trunk').
PRO_ARCHIVE_BRANCH="trunk"
for ARG in "$@" ; do
    PRO_ARCHIVE_BRANCH_ARG="${ARG#PRO_ARCHIVE_BRANCH=}"
    if [ "$PRO_ARCHIVE_BRANCH_ARG" != "$ARG" -a "$PRO_ARCHIVE_BRANCH_ARG" != "" ] ; then
        PRO_ARCHIVE_BRANCH="$PRO_ARCHIVE_BRANCH_ARG"
    fi
done

date

# Build main directory
cd /home/build/build7

# Checkout function
svn_co () {
    for i in 1 0; do
	svn co $1 $2 $3 $4 $5 $6 $7
	SVN_EXIT_CODE=$?
	if [ $SVN_EXIT_CODE -eq 0 ]; then
	    break;
	elif [ $i -eq 0 ]; then
	    echo "BUILD FAILED: There has been a problem trying to checkout GWMON-$GWMEE_VERSION build script files." | mail -s "GroundWork Monitor Enterprise Build FAILED on `hostname` - $DATE" $BUILD_MAIL_ADDRESSES
	    exit 1
	fi
	sleep 30
    done
}

echo "Get the build scripts for GroundWork Monitor $PRO_ARCHIVE_BRANCH GWMON-$GWMEE_VERSION from subversion"

rm -rf GWMON-$GWMEE_VERSION-OLD
mv GWMON-$GWMEE_VERSION GWMON-$GWMEE_VERSION-OLD

PRO_ARCHIVE=http://geneva/groundwork-professional/$PRO_ARCHIVE_BRANCH
svn_co $SVN_CREDENTIALS $PRO_ARCHIVE/build/BitRock_Build/GWMON-$GWMEE_VERSION

echo "Update the copy of the build-script update script (in the place where it is actually executed,"
echo "directly by the build cron job) to the latest version, for the next time we do a build ..."
cp -f GWMON-$GWMEE_VERSION/gwmon-7-0-build-scripts.sh .

PRO_ARCHIVE_BRANCH_ARG=
if [ "$PRO_ARCHIVE_BRANCH" != "trunk" ] ; then
    PRO_ARCHIVE_BRANCH_ARG="PRO_ARCHIVE_BRANCH=$PRO_ARCHIVE_BRANCH"
fi
echo "Build-script checkout is done -- start the build by invoking: GWMON-$GWMEE_VERSION/master-nightlyBuild.sh $PRO_ARCHIVE_BRANCH_ARG"


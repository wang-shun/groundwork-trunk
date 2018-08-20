#!/bin/bash -ex

# Copyright (c) 2012 GroundWork, Inc.  All Rights Reserved.

# This script builds the GroundWork Weathermap distribution, starting from the
# standard Weathermap distribution and applying our patches and new files.  The
# current script starts with the Weathermap 0.97a MySQL release as the basis for
# our distribution.  In the future, once the Weathermap maintainer folds in our
# PostgreSQL-porting changes, the processing here should be greatly simplified.

# First, let's set up to report failure to the calling context if we abort.
# We don't exit here, because the bash -e option above takes care of that for us.
report_error() {
    # We don't generate an email if this happens.
    # That duty will be handled by the calling scripts.
    echo "BUILD FAILED:  There has been an error in preparing the Weathermap build files."
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
# WARNING:  Choose the $WEATHERMAP_BUILD_TREE path carefully, because we will
# completely wipe it out before using it!  Be sure to use an absolute
# pathname for the $WEATHERMAP_BUILD_TREE definition.
WEATHERMAP_BUILD_TREE=/tmp/weathermap-build

# Set this to match the EntBuild.sh script setting, so the files we prepare here
# will be picked up and put into the product distribution we are constructing.
# WARNING:  Choose the $WEATHERMAP_BUILD_BASE path carefully, because we will
# completely wipe it out before using it!  Be sure to use an absolute
# pathname for the $WEATHERMAP_BUILD_BASE definition.  Normally, we just make
# this a subdirectory of the $WEATHERMAP_BUILD_TREE directory, using a path
# similar to how the plugin will be installed under Cacti.
WEATHERMAP_BUILD_BASE=$WEATHERMAP_BUILD_TREE/cacti/htdocs/plugins

# Set this to reflect the Subversion credentials we need to export files.
SVN_CREDENTIALS="--username build --password bgwrk"

# Set this to reflect the degree to which you want to retain backup files
# during patching activity.  Set to a non-empty value if you wish to retain
# backup files.  To suppress backup files, leave this as an empty value.
RETAIN_BACKUPS=

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

echo === Starting the Weathermap Build

/bin/rm -rf $WEATHERMAP_BUILD_TREE
mkdir -p    $WEATHERMAP_BUILD_TREE
cd          $WEATHERMAP_BUILD_TREE

echo === Stage the Raw Distribution, Patch Files, and New Files

           WEATHERMAP_SVN_BASE=http://archive.groundworkopensource.com/groundwork-opensource/trunk/monitor-core/cacti/postgresql/weathermap
WEATHERMAP_GROUNDWORK_SVN_BASE=http://geneva/groundwork-professional/trunk/monitor-nms/weathermap/weathermap-0.97a

svn_export $SVN_CREDENTIALS $WEATHERMAP_SVN_BASE/php-weathermap-0.97a.zip
svn_export $SVN_CREDENTIALS $WEATHERMAP_SVN_BASE/setup.php.initial_diff
svn_export $SVN_CREDENTIALS $WEATHERMAP_SVN_BASE/weathermap-0.97a-patch-for-postgres9

svn_export $SVN_CREDENTIALS $WEATHERMAP_GROUNDWORK_SVN_BASE/auto-overlib.pl.patch
svn_export $SVN_CREDENTIALS $WEATHERMAP_GROUNDWORK_SVN_BASE/editor.php.initial_diff
svn_export $SVN_CREDENTIALS $WEATHERMAP_GROUNDWORK_SVN_BASE/tab_weathermap.png
svn_export $SVN_CREDENTIALS $WEATHERMAP_GROUNDWORK_SVN_BASE/tab_weathermap_red.png
svn_export $SVN_CREDENTIALS $WEATHERMAP_GROUNDWORK_SVN_BASE/weathermap.initial_diff
svn_export $SVN_CREDENTIALS $WEATHERMAP_GROUNDWORK_SVN_BASE/weathermap.properties

# For reasons we don't yet understand, exported files don't come with
# sensible permissions bits, so we need to set them explicitly here.
chmod 644 tab_weathermap.png
chmod 644 tab_weathermap_red.png
chmod 600 weathermap.properties

echo === Create the Build Directory

/bin/rm -rf $WEATHERMAP_BUILD_BASE
mkdir -p    $WEATHERMAP_BUILD_BASE
cd          $WEATHERMAP_BUILD_BASE

echo === Unpack the Raw Weathermap release.
cd $WEATHERMAP_BUILD_BASE
unzip -q $WEATHERMAP_BUILD_TREE/php-weathermap-0.97a.zip

echo === Apply baseline patches for the Weathermap plugin, to fix non-PostgreSQL bugs.
cd $WEATHERMAP_BUILD_BASE
patch ${RETAIN_BACKUPS:+ -b -V simple -z .pre_initial } -p0 < $WEATHERMAP_BUILD_TREE/setup.php.initial_diff

echo === Apply Initial GroundWork-context patches for the Weathermap plugin.
# The Weathermap maintainer should skip these commands.
cd $WEATHERMAP_BUILD_BASE
patch ${RETAIN_BACKUPS:+ -b -V simple -z .pre_gw } -p0 < $WEATHERMAP_BUILD_TREE/editor.php.initial_diff
patch ${RETAIN_BACKUPS:+ -b -V simple -z .pre_gw } -p0 < $WEATHERMAP_BUILD_TREE/weathermap.initial_diff

echo === Apply patches to port Weathermap to allow access to PostgreSQL.
cd $WEATHERMAP_BUILD_BASE
patch ${RETAIN_BACKUPS:+ -b -V simple -z .pre_pg } -p0 < $WEATHERMAP_BUILD_TREE/weathermap-0.97a-patch-for-postgres9

echo === Add New GroundWork Files to the Raw Distribution
cp -p $WEATHERMAP_BUILD_TREE/tab_weathermap.png     $WEATHERMAP_BUILD_BASE/weathermap/images
cp -p $WEATHERMAP_BUILD_TREE/tab_weathermap_red.png $WEATHERMAP_BUILD_BASE/weathermap/images

echo === Apply Final GroundWork-context patches for the Weathermap plugin.
# GroundWork applies these changes for its own distribution of
# Weathermap, but the Weathermap maintainer should skip these
# commands.  These particular changes must be applied after the
# PostgreSQL patches applied above.
cd $WEATHERMAP_BUILD_BASE
patch ${RETAIN_BACKUPS:+ -b -V simple -z .pre_gw } -p0 < $WEATHERMAP_BUILD_TREE/auto-overlib.pl.patch

echo === Change Ownership in the Prepared File Tree

chown -R nagios:nagios $WEATHERMAP_BUILD_BASE
chown    nagios:nagios $WEATHERMAP_BUILD_TREE/weathermap.properties

echo === Ending the Weathermap Build

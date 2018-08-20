#!/bin/bash -ex

# Copyright (c) 2012 GroundWork, Inc.  All Rights Reserved.

# This script builds the GroundWork NeDi distribution, starting from the standard
# NeDi distribution and applying our patches and new files.  The current script
# starts with the NeDi 1.0.7 MySQL release as the basis for our distribution.
# In the future, once the NeDi maintainer folds in our PostgreSQL-porting
# changes, the processing here should be greatly simplified.

# First, let's set up to report failure to the calling context if we abort.
# We don't exit here, because the bash -e option above takes care of that for us.
report_error() {
    # We don't generate an email if this happens.
    # That duty will be handled by the calling scripts.
    echo "BUILD FAILED:  There has been an error in preparing the NeDi build files."
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
# WARNING:  Choose the $NEDI_BUILD_TREE path carefully, because we will
# completely wipe it out before using it!  Be sure to use an absolute
# pathname for the $NEDI_BUILD_TREE definition.
NEDI_BUILD_TREE=/tmp/nedi-build

# Set this to match the EntBuild.sh script setting, so the files we prepare here
# will be picked up and put into the product distribution we are constructing.
# WARNING:  Choose the $NEDI_BUILD_BASE path carefully, because we will
# completely wipe it out before using it!  Be sure to use an absolute
# pathname for the $NEDI_BUILD_BASE definition.  Normally, we just make
# this a subdirectory of the $NEDI_BUILD_TREE directory.
NEDI_BUILD_BASE=$NEDI_BUILD_TREE/nedi

# Set this to reflect the Subversion credentials we need to export files.
SVN_CREDENTIALS="--username build --password bgwrk"

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

echo === Starting the NeDi Build

/bin/rm -rf $NEDI_BUILD_TREE
mkdir -p    $NEDI_BUILD_TREE
cd          $NEDI_BUILD_TREE

echo === Stage the Raw Distribution, Patch Files, and New Files

       NEDI_SVN_BASE=http://geneva/groundwork-professional/trunk/monitor-nms/nedi/nedi-1.0.7
NEDI_PORTAL_SVN_BASE=http://geneva/groundwork-professional/trunk/monitor-portal/applications/nms/installer/nedi

svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/nedi-1.0.7.tgz
svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/nedi-1.0.7-1.patch_.zip
svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/all-postgresql-patches-for-nedi-1.0.7.patch
svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/missing_plugin.php

chmod 744 $NEDI_BUILD_TREE/missing_plugin.php

echo === Unzip the Official NeDi Patch File

unzip nedi-1.0.7-1.patch_.zip

echo === Unpack and Patch the Raw Distribution

/bin/rm -rf $NEDI_BUILD_BASE
mkdir -p    $NEDI_BUILD_BASE
cd          $NEDI_BUILD_BASE

tar xfz     $NEDI_BUILD_TREE/nedi-1.0.7.tgz
patch -p0 < $NEDI_BUILD_TREE/nedi-1.0.7-1.patch

echo === Clean Up the Raw Distribution

perl -pe 's/\r/\n/g' -i $NEDI_BUILD_BASE/contrib/bulkdelete.sh
dos2unix $NEDI_BUILD_BASE/contrib/CheckNewMac.pl
dos2unix $NEDI_BUILD_BASE/contrib/ccc.pl
dos2unix $NEDI_BUILD_BASE/contrib/nediportcapacity.pl
dos2unix $NEDI_BUILD_BASE/contrib/renedi.pl
dos2unix $NEDI_BUILD_BASE/html/inc/menutheme.js
dos2unix $NEDI_BUILD_BASE/html/inc/snmpget.php
dos2unix $NEDI_BUILD_BASE/html/inc/snmpwalk.php
dos2unix $NEDI_BUILD_BASE/html/test/env.php
dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.25506.1.334.def
dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.25506.1.341.def
dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.25506.1.462.def
dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.2636.1.1.1.4.31.1.def
dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.266.1.3.27.def
dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.9.1.110.def 
dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.9.1.122.def
dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.9.1.1227.def
dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.9.1.1287.def
dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.9.1.1317.def
dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.9.1.392.def
dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.9.1.417.def
dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.9.1.488.def
dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.9.1.577alt.def
dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.9.1.697.def
dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.9.1.758.def

chmod 755 $NEDI_BUILD_BASE
chmod 600 $NEDI_BUILD_BASE/nedi.conf
chmod 755 $NEDI_BUILD_BASE/html/log
chmod 644 $NEDI_BUILD_BASE/html/log/devtools.php
chmod 644 $NEDI_BUILD_BASE/html/log/map-top.png
chmod 644 $NEDI_BUILD_BASE/html/log/msg.txt
chmod -x  $NEDI_BUILD_BASE/sysobj/*.def

echo === Apply PostgreSQL Patches to the Raw Distribution

patch -p1 < $NEDI_BUILD_TREE/all-postgresql-patches-for-nedi-1.0.7.patch

echo === Add New NeDi Files to the Raw Distribution

cp -p $NEDI_BUILD_TREE/missing_plugin.php $NEDI_BUILD_BASE/html

# ====================================================================
# At this point, we have built the standard distribution as cleaned up
# and ported to PostgreSQL.  The remaining steps apply the additional
# changes needed to run NeDi in the GroundWork Monitor context.
# ====================================================================

echo === Stage the GroundWork Patch Files and New Files

# nedi.properties and nedi_httpd.conf won't be used by this script.  We export them here so the
# associated build scripting can pick them up without itself checking them out from Subversion.
cd $NEDI_BUILD_TREE
svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/extract_nedi.pl
svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_index.php.patch
svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_log_msg.txt.patch
svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/nedi.conf.patch
svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/nedi.properties
svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/nedi_httpd.conf
svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/META-INF/context.xml
svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/WEB-INF/jboss-web.xml
svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/WEB-INF/web.xml
svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/login-redirect.jsp

# For reasons we don't yet understand, exported files don't come with
# sensible permissions bits, so we need to set them explicitly here.
chmod 644 context.xml
chmod 755 extract_nedi.pl
chmod 644 jboss-web.xml
chmod 644 login-redirect.jsp
chmod 600 nedi.properties
chmod 644 nedi_httpd.conf
chmod 644 web.xml

# These files get installed outside of the /usr/local/groundwork/nedi/ tree,
# so we set their ownership explicitly here.
chown nagios:nagios nedi.properties
chown nagios:nagios nedi_httpd.conf

echo === Add New GroundWork Files to the Raw Distribution

mkdir $NEDI_BUILD_BASE/META-INF
mkdir $NEDI_BUILD_BASE/WEB-INF
chmod 755 $NEDI_BUILD_BASE/META-INF
chmod 755 $NEDI_BUILD_BASE/WEB-INF

cp -p extract_nedi.pl    $NEDI_BUILD_BASE
cp -p login-redirect.jsp $NEDI_BUILD_BASE

cp -p context.xml        $NEDI_BUILD_BASE/META-INF
cp -p jboss-web.xml      $NEDI_BUILD_BASE/WEB-INF
cp -p web.xml            $NEDI_BUILD_BASE/WEB-INF

echo === Apply GroundWork Patches to the Raw Distribution

cd $NEDI_BUILD_BASE

patch -p1 < $NEDI_BUILD_TREE/html_index.php.patch
patch -p1 < $NEDI_BUILD_TREE/html_log_msg.txt.patch
patch -p1 < $NEDI_BUILD_TREE/nedi.conf.patch

echo === Change GroundWork Paths in the Raw Distribution

perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i contrib/CheckNewMac.pl
perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i contrib/ccc.pl
perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i contrib/flood.pl
perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i contrib/nbt.pl
perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i contrib/nedilaunch.pl
perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i html/inc/devwrite.pl
perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i moni.pl
perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i nedi.pl
perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i syslog.pl
perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i trap.pl

perl -pe 's{/usr/local/bin/perl}{/usr/local/groundwork/perl/bin/perl}' -i contrib/nediDeviceConnections.pl
perl -pe 's{/usr/local/bin/perl}{/usr/local/groundwork/perl/bin/perl}' -i contrib/nediportcapacity.pl
perl -pe 's{/usr/local/bin/perl}{/usr/local/groundwork/perl/bin/perl}' -i contrib/renedi.pl

perl -pe 's{/etc/nedi.conf}{/usr/local/groundwork/nedi/nedi.conf}' -i html/inc/libmisc.php

perl -p -e 's{/usr/bin/mysql}{/usr/local/groundwork/mysql/bin/mysql};s{/usr/bin/psql}{/usr/local/groundwork/postgresql/bin/psql}' -i contrib/bulkdelete.sh

echo === Change Ownership in the Prepared File Tree

chown -R nagios:nagios $NEDI_BUILD_BASE

echo === Ending the NeDi Build

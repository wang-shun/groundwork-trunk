#!/bin/bash -ex

# Copyright (c) 2014-2018 GroundWork, Inc.  All Rights Reserved.

# This script builds the GroundWork NeDi distribution, starting from a standard
# NeDi distribution and applying our patches and new files.  The current script
# starts with a given upstream release, such as:
#
# * the NeDi 1.0.7 MySQL-only release
# * the NeDi 1.1.0 release
# * the NeDi 1.4.300 release
# * the NeDi 1.5.255 release
# * the NeDi 1.6.100 release
# * the NeDi 1.7.090 release
# * the NeDi 1.8.100 release
#
# The specific release we want to use is selectable below, as the basis for
# our distribution.  Now that the NeDi maintainer has folded basic support for
# PostgreSQL into the 1.1.0 release, we no longer need patches specifically to
# change MySQL-based code to PostgreSQL-based code, if we are building such a
# later release.  However, if we use simple code substitutions, we might not get
# the error-checking that a patch application provides.  So a future version of
# this script might re-implement certain 1.1.0 changes using patch files.

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

# Specify the NeDi version we wish to build.  Acceptable values are currently
# "1.0.7", "1.1.0", "1.4.300", "1.5.225", "1.6.100", "1.7.090", and "1.8.100".
# There is sometimes also some special handling from upstream.  For instance:
#
# * The 1.4.300 release build also includes the contents of a separate nedi-1.4p3.tgz
#   tarball, which simply overlays several files from the base release.
# * The 1.6.100 release build also includes the contents of a separate nedi-1.6p2.tgz
#   tarball, which simply overlays several files from the base release.
# * The 1.6.100 release build also includes a separate nedi_feeder.pl script.
#
# NEDI_VERSION="1.4.300"
# NEDI_VERSION="1.5.225"
# NEDI_VERSION="1.6.100"
# NEDI_VERSION="1.7.090"
NEDI_VERSION="1.8.100"

# Specify the NeDi tarball we wish to unroll.  Acceptable values are currently:
#
# nedi-1.0.7.tgz        (the proper tarball for 1.0.7, in conjunction with nedi-1.0.7-1.patch_.zip)
# nedi-155.tgz          (Remo's first beta tarball for the 1.1.0 release)
# nedi-225.tgz          (an early tarball from Remo for the 1.1.0 release)
# nedi-237.tgz          (the last tarball from Remo for the 1.1.0 release)
# nedi-1.4.tgz          (first tarball from Remo for the 1.4.300 release)
# nedi-1.5.225.tgz      (first tarball from Remo for the 1.5.225 release)
# nedi-1.6.100.tar.gz   (renamed and compressed tarball from Remo for the 1.6.100 release)
# nedi-1.7.090.tgz      (first tarball from Remo for the 1.7.090 release)
# nedi-1.8.100.tgz      (first tarball from Remo for the 1.8.100 release)
#
# NEDI_TARBALL=nedi-1.4.tgz
# NEDI_TARBALL=nedi-1.5.225.tgz
# NEDI_TARBALL=nedi-1.6.100.tar.gz
# NEDI_TARBALL=nedi-1.7.090.tgz
NEDI_TARBALL=nedi-1.8.100.tgz

# Specify the NoDi tarball, if any, we wish to unroll.  Acceptable values are currently:
#
# Node-Discovery.tar.gz (the initial tarball, used with NeDi 1.8.100)
#
NODI_TARBALL=Node-Discovery.tar.gz

# Specify the NeDi patch tarball, if any, we wish to unroll.  Acceptable
# values are currently:
#
# nedi-1.4p3.tgz     (if using nedi-1.4.tgz        as the base NEDI_TARBALL release)
# nedi-1.5p1.tgz     (if using nedi-1.5.225.tgz    as the base NEDI_TARBALL release)
# nedi-1.6p2.tar.gz  (if using nedi-1.6.100.tar.gz as the base NEDI_TARBALL release)
# nedi-1.6p3.tgz     (if using nedi-1.6.100.tar.gz as the base NEDI_TARBALL release)
# nedi-1.7p1.tgz     (if using nedi-1.7.090.tgz    as the base NEDI_TARBALL release)
# nedi-1.8p1.tar.gz  (if using nedi-1.8.100.tgz    as the base NEDI_TARBALL release)
#
# If the release you wish to build has no patch tarball to apply, just define
# this as an empty string.  (Leaving it undefined effectively does that.)  All
# references to this variable must be enclosed in double-quotes to allow for
# the possibility that this variable might be undefined or an empty string.
#
# NEDI_PATCH_TARBALL=nedi-1.4p3.tgz
# NEDI_PATCH_TARBALL=nedi-1.5p1.tgz
# NEDI_PATCH_TARBALL=nedi-1.6p3.tgz
# NEDI_PATCH_TARBALL=nedi-1.7p1.tgz
NEDI_PATCH_TARBALL=nedi-1.8p1.tar.gz

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

echo === Starting the NeDi Build

/bin/rm -rf $NEDI_BUILD_TREE
mkdir -p    $NEDI_BUILD_TREE
cd          $NEDI_BUILD_TREE

echo === Stage the Raw Distribution, Patch Files, and New Files
PRO_ARCHIVE=http://geneva/groundwork-professional/$PRO_ARCHIVE_BRANCH

       NEDI_SVN_BASE=$PRO_ARCHIVE/monitor-nms/nedi/nedi-$NEDI_VERSION
NEDI_PORTAL_SVN_BASE=$PRO_ARCHIVE/monitor-portal/applications/nms/installer/nedi

if [ $NEDI_VERSION = "1.0.7" ]; then

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

    cd $NEDI_BUILD_TREE

    # nedi.properties and nedi_httpd.conf won't be used by this script.  We export them here so the
    # associated build scripting can pick them up without itself checking them out from Subversion.
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/extract_nedi.pl
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_index.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_log_msg.txt.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/nedi.conf.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/nedi.properties
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/nedi_httpd.conf
    svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/META-INF/context.xml
    svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/WEB-INF/jboss-web.xml
    svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/WEB-INF/web.xml
    svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/WEB-INF/jboss-deployment-structure.xml

    # For reasons we don't yet understand, exported files don't come with
    # sensible permissions bits, so we need to set them explicitly here.
    chmod 644 context.xml
    chmod 755 extract_nedi.pl
    chmod 644 jboss-web.xml
    chmod 644 jboss-deployment-structure.xml
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

    cp -p extract_nedi.pl                $NEDI_BUILD_BASE
    cp -p context.xml                    $NEDI_BUILD_BASE/META-INF
    cp -p jboss-deployment-structure.xml $NEDI_BUILD_BASE/WEB-INF
    cp -p jboss-web.xml                  $NEDI_BUILD_BASE/WEB-INF
    cp -p web.xml                        $NEDI_BUILD_BASE/WEB-INF

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

elif [ $NEDI_VERSION = "1.1.0" ]; then

    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/$NEDI_TARBALL

    echo === Unpack and Patch the Raw Distribution

    /bin/rm -rf $NEDI_BUILD_BASE
    mkdir -p    $NEDI_BUILD_BASE
    cd          $NEDI_BUILD_BASE

    tar xfz $NEDI_BUILD_TREE/$NEDI_TARBALL

    echo === Clean Up the Raw Distribution

    # We asked Remo to clean up the line endings in these files.  He did so by
    # dropping the contrib/ directory entirely, and fixing up the other files.
    if [ $NEDI_TARBALL = nedi-155.tgz ]; then
	perl -pe 's/\r/\n/g' -i $NEDI_BUILD_BASE/contrib/bulkdelete.sh
	dos2unix $NEDI_BUILD_BASE/contrib/CheckNewMac.pl
	dos2unix $NEDI_BUILD_BASE/contrib/Reports-Serials.php
	dos2unix $NEDI_BUILD_BASE/contrib/ccc.pl
	dos2unix $NEDI_BUILD_BASE/contrib/nediportcapacity.pl
	dos2unix $NEDI_BUILD_BASE/contrib/renedi.pl
	dos2unix $NEDI_BUILD_BASE/html/inc/browse-img.php
	dos2unix $NEDI_BUILD_BASE/html/inc/libsnmp.php
	dos2unix $NEDI_BUILD_BASE/html/inc/rt-popup.php
    fi
    dos2unix $NEDI_BUILD_BASE/html/languages/deutsch/style.css
    dos2unix $NEDI_BUILD_BASE/html/languages/english/style.css
    if [ $NEDI_TARBALL = nedi-237.tgz ]; then
	mv $NEDI_BUILD_BASE/sysobj/"1.3.6.1.4.1.9.1.1274 .def" $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.9.1.1274.def
	mv $NEDI_BUILD_BASE/sysobj/"1.3.6.1.4.1.9.1.1642 .def" $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.9.1.1642.def
    fi
    if [ $NEDI_TARBALL = nedi-155.tgz ]; then
	dos2unix $NEDI_BUILD_BASE/html/log/msg.txt
	dos2unix $NEDI_BUILD_BASE/html/test/env.php
	dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.11.2.3.7.11.129.def
	dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.11.2.3.7.11.132.def
	dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.11.2.3.7.11.88.def
	dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.12356.101.1.1004.def
	dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.6486.800.1.1.2.1.10.1.1.def
	dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.674.10895.3010.def
	dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.674.10895.3024.def
	dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.9.1.1178.def
	dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.9.1.1732.def
	dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.9.1.560.def
	dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.9.1.569.def
	dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.9.1.716.def
	dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.9.1.796.def
	dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.9.1.876.def
	dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.9.12.3.1.3.375.def
	dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.9.12.3.1.3.414.def
    fi

    # FIX MINOR:  Verify whether any of these commands are still needed whenever
    # we get a new release.  Kick any new requirements here back to the upstream
    # NeDi developer.
    # (*) Permissions on the $NEDI_BUILD_BASE directory are 755 in the
    #     tarball, but when the tarball is unrolled, those permissions don't
    #     override any more-lenient permissions already established for this
    #     directory, so we set them explicitly here on that directory.
    # (*) Permissions on the $NEDI_BUILD_BASE/nedi.conf file directory are 644
    #     in the standard distribution.  But we include some DB credentials in
    #     this file, so we want to be more restrictive.
    chmod 755 $NEDI_BUILD_BASE
    chmod 600 $NEDI_BUILD_BASE/nedi.conf

    # Fix modes on certain directories and files which shipped with much-too-open
    # permissions.  This was true in both of the nedi-155.tgz and nedi-225.tgz
    # tarballs, so we don't bother to wrap this in a $NEDI_TARBALL test.
    if [ $NEDI_TARBALL = nedi-155.tgz -o $NEDI_TARBALL = nedi-225.tgz ]; then
	chmod 755 $NEDI_BUILD_BASE/html/log
	chmod 644 $NEDI_BUILD_BASE/html/log/Readme.txt
	chmod 644 $NEDI_BUILD_BASE/html/log/devtools.php
	chmod 644 $NEDI_BUILD_BASE/html/log/msg.txt
    fi

    chmod -x  $NEDI_BUILD_BASE/sysobj/*.def

    # ====================================================================
    # At this point, we have built the standard distribution with only
    # slight cleanups.  The remaining steps apply the additional changes
    # needed to run NeDi in the GroundWork Monitor context.
    # ====================================================================

    echo === Stage the GroundWork Patch Files and New Files

    cd $NEDI_BUILD_TREE

    # nedi.properties and nedi_httpd.conf won't be used by this script.  We export them here so the
    # associated build scripting can pick them up without itself checking them out from Subversion.

    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/extract_nedi.pl

    # We asked Remo to fold these two patches into the base release.
    # That happened, so we no longer process them here.
    if [ $NEDI_TARBALL = nedi-155.tgz ]; then
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_inc_libdb-mysql.php.patch
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_inc_libdb-pg.php.patch
    fi

    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_index.php.patch
    ## svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_log_msg.txt.patch
    ## svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/nedi.conf.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/nedi.properties
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/nedi_httpd.conf
    svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/META-INF/context.xml
    svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/WEB-INF/jboss-web.xml
    svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/WEB-INF/web.xml
    svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/WEB-INF/jboss-deployment-structure.xml

    # Use of these files is obsolete, now that Remo dropped the entire
    # contrib/ directory from his distribution.
    if [ $NEDI_TARBALL = nedi-155.tgz ]; then
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/CheckNewMac.pl.mysql
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/bulkdelete.sh.mysql
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/ccc.pl.mysql
    fi

    # For reasons we don't yet understand, exported files don't come with
    # sensible permissions bits, so we need to set them explicitly here.
    chmod 644 context.xml
    chmod 755 extract_nedi.pl
    chmod 644 jboss-web.xml
    chmod 644 jboss-deployment-structure.xml
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

    cp -p extract_nedi.pl                $NEDI_BUILD_BASE
    cp -p context.xml                    $NEDI_BUILD_BASE/META-INF
    cp -p jboss-deployment-structure.xml $NEDI_BUILD_BASE/WEB-INF
    cp -p jboss-web.xml                  $NEDI_BUILD_BASE/WEB-INF
    cp -p web.xml                        $NEDI_BUILD_BASE/WEB-INF

    # Use of these files is obsolete, now that Remo dropped the entire
    # contrib/ directory from his distribution.
    if [ $NEDI_TARBALL = nedi-155.tgz ]; then
	cp -p CheckNewMac.pl.mysql $NEDI_BUILD_BASE/contrib/CheckNewMac.pl
	cp -p bulkdelete.sh.mysql  $NEDI_BUILD_BASE/contrib/bulkdelete.sh
	cp -p ccc.pl.mysql         $NEDI_BUILD_BASE/contrib/ccc.pl
    fi

    echo === Apply GroundWork Patches to the Raw Distribution

    cd $NEDI_BUILD_BASE

    # We asked Remo to fold these two patches into the base release.
    # That happened, so we no longer process them here.
    if [ $NEDI_TARBALL = nedi-155.tgz ]; then
	patch -p1 < $NEDI_BUILD_TREE/html_inc_libdb-mysql.php.patch
	patch -p1 < $NEDI_BUILD_TREE/html_inc_libdb-pg.php.patch
    fi
    ## FIX MINOR:  Re-create this patch against the nedi-255.tgz and nedi-237.tgz
    ## distributions, so it doesn't get applied "with fuzz 1".
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_index.php.patch

    # FIX MINOR:  This is historical stuff that might come back into play if we
    # move some of the on-the-fly changes made below into formal patch scripts.
    ## patch -p1 < $NEDI_BUILD_TREE/html_log_msg.txt.patch
    ## patch -p1 < $NEDI_BUILD_TREE/nedi.conf.patch

    # FIX MINOR:  We might move this and much of the other on-the-fly patching here
    # into formal patch files, in order that we get proper error notification if the
    # intended changes don't get made (as opposed to the current behavior, which is
    # that a non-matching substitution pattern simply won't throw any kind of error).
    # We'll do that kind of cleanup work once we have the NeDi 1.1.0 beta2 release
    # in hand.
    perl -p \
	-e 's{(rrdstep\s+)3600}{# 14400 matches the default 4-hour interval between nedi.pl cron jobs\n# established in a standard GroundWork installation.\n${1}14400};' \
	-e 's{(backend\s+)mysql}{${1}Pg};' \
	-e 's{(rrdcmd\s+)rrdtool}{${1}/usr/local/groundwork/common/bin/rrdtool};' \
	-e 's{(nedipath\s+)/var/nedi}{${1}/usr/local/groundwork/nedi};' \
	-e 's{(cacticli\s+)/usr/bin/php /usr/share/cacti/site/cli}{${1}/usr/local/groundwork/php/bin/php /usr/local/groundwork/cacti/htdocs/cli};' \
	-e 's{(cactiuser\s+)cacti}{${1}cactiuser};' \
	-e 's{(cactipass\s+)cactipa55}{${1}cactiuser};' \
	-e 's{(cactiurl\s+)/cacti}{${1}/portal/auth/portal/groundwork-monitor/nagios/Cacti};' \
	-e 's{(nagpipe\s+)/Data/nagios/rw/nagios.cmd}{${1}/usr/local/groundwork/nagios/var/spool/nagios.cmd};' \
	-i nedi.conf

    # The need for this fixup disappeared starting with the nedi-225.tgz tarball,
    # so we don't encapsulate it in a formal patch file.  More to the point,
    # attempting to fix the bug we found that this addresses, by putting this
    # change in place, ends up breaking other code elsewhere in NeDi.
    #
    # Note that this must be tested by going into the User Profile and attempting
    # to change the timezone.  Also test logging in as "user" instead of as "admin".
    ## perl -pe 's{ WHERE \$col \$ord \$lim}{ WHERE \$col \$ord '"'"'\$lim'"'"'}' -i html/inc/libdb-pg.php

    # The need for this fixup has disappeared at least in the nedi-237.tgz tarball,
    # if not sooner.
    if [ $NEDI_TARBALL = nedi-155.tgz -o $NEDI_TARBALL = nedi-225.tgz ]; then
	perl -pe 's{width: 540px;}{min-width: 540px;}' -i html/themes/default.css
    fi

    # FIX MINOR:  The need for this fixup should disappear in a future tarball
    # before the formal 1.4.0 release, so we aren't currently encapsulating it
    # in a formal patch file.
    # This is still needed as of the nedi-237.tgz tarball.
    perl -pe 's{/\(1000000\*\$rrdstep\),}{/\$rrdstep/1000000,}g' -i inc/libmisc.pm

    # FIX MINOR:  The need for this fixup should disappear in a future tarball
    # before the formal 1.4.0 release, so we aren't currently encapsulating it
    # in a formal patch file.
    # Possibly this edit might have been appropriate for nedi-155.tgz as well,
    # but we're past that now so we won't bother to check.
    if [ $NEDI_TARBALL = nedi-225.tgz -o $NEDI_TARBALL = nedi-237.tgz ]; then
	perl -pe 's:defined \$_\[0\] and :!defined \$_\[0\] or :' -i inc/libmisc.pm
    fi

    # Make certain corrections to files provided in the NeDi distribution
    # that specify the use of MySQL instead of PostgreSQL.
    if [ $NEDI_TARBALL = nedi-155.tgz ]; then
	perl -pe 's{(\$dbtype =) "mysql"}{$1 "postgresql"}' -i contrib/ccc.pl
    fi

    # Make certain corrections to messages.
    perl -pe 's{1h, adjust rrdstep in nedi.conf and delete all files in /var/nedi/rrd}{4h, adjust rrdstep in nedi.conf and delete all files in /usr/local/groundwork/nedi/rrd/}' -i html/log/msg.txt
    perl -pe 's{Dont forget to change the admin password!}{Do not change NeDi user passwords with the NeDi user interface.}' -i html/log/msg.txt

    echo === Change GroundWork Paths in the Raw Distribution

    if [ $NEDI_TARBALL = nedi-155.tgz ]; then
	perl -p \
	    -e 's{mysql=mysql}{mysql=/usr/local/groundwork/mysql/bin/mysql};' \
	    -e 's{psql=psql}{psql=/usr/local/groundwork/postgresql/bin/psql};' \
	    -i contrib/bulkdelete.sh

	perl -p \
	    -e 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl};' \
	    -e 's{(\$dbtype\s*=) "mysql";     }{$1 "postgresql";};' \
	    -e 's{(\$dbport\s*=) "3306"}{$1 "5432"};' \
	    -i contrib/CheckNewMac.pl

	perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i contrib/addstatus2ifrrd.pl
	perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i contrib/ccc.pl
	perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i contrib/flood.pl
	perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i contrib/nbt.pl
	perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i contrib/nedilaunch.pl
    fi
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i inc/devwrite.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i master.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i moni.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i nedi.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i stati.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i syslog.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i trap.pl

    if [ $NEDI_TARBALL = nedi-155.tgz ]; then
	perl -p \
	    -e 's{/usr/local/bin/perl}{/usr/local/groundwork/perl/bin/perl};' \
	    -e 's{/usr/share/nedi}{/usr/local/groundwork/nedi};' \
	    -e 's{inc/libmisc.pl}{inc/libmisc.pm};' \
	    -e 's{inc/libsnmp.pl}{inc/libsnmp.pm};' \
	    -i contrib/nediDeviceConnections.pl

	perl -pe 's{/usr/local/bin/perl}{/usr/local/groundwork/perl/bin/perl}' -i contrib/nediportcapacity.pl
	perl -pe 's{/usr/local/bin/perl}{/usr/local/groundwork/perl/bin/perl}' -i contrib/renedi.pl
    fi

elif [ $NEDI_VERSION = "1.4.300" ]; then

    # FIX MINOR:  STILL TO FIX FOR THIS RELEASE:
    # (*) Suppress certain NeDi menus in the nedi.conf we ship (GWMON-12042).
    # (*) Test by going into the User Profile and attempting to change the timezone.
    # (*) Test logging in as "user" instead of as "admin".
    # (*) Look at other historical NeDi-related JIRAs to see if any of them can be dealt with at this time.
    # (*) Review email exchanges with Remo at the end of October 2014 and the beginning of November 2014,
    #     about residual bugs that got noticed after the nedi-1.4.tgz tarball was released but likely were
    #     not later included in the 1.4p2 patch.  (At least some of them, if not all, have been addressed
    #     in the nedi-1.4p3.tgz patch.)

    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/$NEDI_TARBALL
    if [ -n "$NEDI_PATCH_TARBALL" ]; then
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/$NEDI_PATCH_TARBALL
    fi

    echo === Unpack and Patch the Raw Distribution

    /bin/rm -rf $NEDI_BUILD_BASE
    mkdir -p    $NEDI_BUILD_BASE
    cd          $NEDI_BUILD_BASE

    tar xfz $NEDI_BUILD_TREE/$NEDI_TARBALL
    if [ -n "$NEDI_PATCH_TARBALL" ]; then
	tar xfz $NEDI_BUILD_TREE/$NEDI_PATCH_TARBALL
    fi

    echo === Clean Up the Raw Distribution

    # FIX LATER:  Verify whether any of these commands are still needed whenever
    # we get a new release.  Kick any new requirements here back to the upstream
    # NeDi developer.
    # (*) Permissions on the $NEDI_BUILD_BASE directory are 755 in the
    #     tarball, but when the tarball is unrolled, those permissions don't
    #     override any more-lenient permissions already established for this
    #     directory, so we set them explicitly here on that directory.
    # (*) Permissions on the $NEDI_BUILD_BASE/nedi.conf file directory are 644
    #     in the standard distribution.  But we include some DB credentials in
    #     this file, so we want to be more restrictive.
    chmod 755 $NEDI_BUILD_BASE
    chmod 600 $NEDI_BUILD_BASE/nedi.conf

    # Fix modes on certain directories and files which shipped with much-too-open
    # permissions.
    #
    # FIX LATER:  Verify this stuff on every new release, since the set of
    # directories and files that need such corrections shifts around with
    # every upstream distribution.
    if [ $NEDI_TARBALL = nedi-1.4.tgz ]; then
	chmod 644 $NEDI_BUILD_BASE/html/*/*/*.css
	chmod 644 $NEDI_BUILD_BASE/html/*/*/*.html
	chmod 644 $NEDI_BUILD_BASE/html/img/*/*.png
	chmod 644 $NEDI_BUILD_BASE/html/img/favicon.ico
	chmod 644 $NEDI_BUILD_BASE/html/img/oui/lac.PNG
	chmod 644 $NEDI_BUILD_BASE/html/img/tel.png
	chmod 644 $NEDI_BUILD_BASE/html/log/Readme.txt
	chmod 644 $NEDI_BUILD_BASE/html/log/iftools.php
	chmod 644 $NEDI_BUILD_BASE/html/themes/*.css
	chmod 644 $NEDI_BUILD_BASE/html/themes/*.jpg
	chmod 644 $NEDI_BUILD_BASE/html/themes/*.png
    fi

    # This is no longer needed in the 1.4 release.  But it must be checked again
    # when we receive future upstream distributions.
    ## chmod -x $NEDI_BUILD_BASE/sysobj/*.def

    # This is needed for one file after the preliminary 1.4p3 patch is applied,
    # so we just go ahead and apply it in a blanket fashion to ensure that all
    # similar files also have clean permissions.
    chmod go-w $NEDI_BUILD_BASE/sysobj/*.def

    # ====================================================================
    # At this point, we have built the standard distribution with only
    # slight cleanups.  The remaining steps apply the additional changes
    # needed to run NeDi in the GroundWork Monitor context.
    # ====================================================================

    echo === Stage the GroundWork Patch Files and New Files

    cd $NEDI_BUILD_TREE

    # nedi.properties and nedi_httpd.conf won't be used by this script.  We export them here so the
    # associated build scripting can pick them up without itself checking them out from Subversion.

    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/extract_nedi.pl
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_index.php.patch

    # The NeDi PHP code references preg_replace_callback() in a manner not
    # supported by the PHP 5.2.17 release that we are still using in GWMEE
    # 7.1.0.  The problem is that PHP 5.2.17 can't handle an in-line function
    # declaration, and needs to instead have the function already defined
    # and then just a reference (the function name, as a literal string)
    # passed in to the preg_replace_callback() function.  Anonymous functions
    # (closures) apparently only became available starting with PHP 5.3.0;
    # see http://php.net/manual/en/functions.anonymous.php for details.  The
    # affected code (that is, the call to preg_replace_callback()) is found in
    # the nedi/html/Devices-Status.php file.  This has been addressed natively
    # in the final p3 nedi-1.4p3.tgz patch file, so we only apply our own patch
    # conditionally.
    if [ $NEDI_TARBALL = nedi-1.4.tgz -a "$NEDI_PATCH_TARBALL" = nedi-1.4p2.tgz ]; then
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_Devices-Status.php.patch
    fi

    # The NeDi PHP code dereferences a null object in a couple of places,
    # resulting in confusing on-screen warnings.  These two patches sidestep the
    # inappropriate null-object dereferences.
    #
    # Even with the nedi-1.4p2.tgz patch applied, the Reports-Wlan.php file
    # needed our own patch for this.  The nedi-1.4p3.tgz patch file (the
    # final p3 patches) addresses the issue appropriately.  Note that the
    # html_Reports-Wlan.php.patch file now in our Subversion actually applies
    # to the preliminary nedi-1.4p3.tar.gz patch file and not the result of
    # applying the nedi-1.4p2.tgz patch file, since the preliminary p3 patch
    # didn't quite get this right, either, even as it addressed other problems
    # in the file.
    if [ $NEDI_TARBALL = nedi-1.4.tgz -a "$NEDI_PATCH_TARBALL" = nedi-1.4p2.tgz ]; then
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_Reports-Wlan.php.patch
    fi
    #
    # The nedi-1.4p3.tgz patch file corrects the problem natively in the
    # System-Files.php file, so our own patch is now only applied conditionally.
    if [ $NEDI_TARBALL = nedi-1.4.tgz -a "$NEDI_PATCH_TARBALL" = nedi-1.4p2.tgz ]; then
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_System-Files.php.patch
    fi

    # Especially when NeDi is first run in exploratory mode, NeDi might attempt
    # to draw RRD graphs when there is not yet data to draw.  Such failures
    # can result in a confusing broken-image icon in the browser.  This
    # patch replaces that icon with a small image that more clearly suggests
    # the nature of the failure.  This has been addressed natively in the
    # nedi-1.4p3.tgz patch file, so we only apply our own patch conditionally.
    if [ $NEDI_TARBALL = nedi-1.4.tgz -a "$NEDI_PATCH_TARBALL" = nedi-1.4p2.tgz ]; then
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_inc_drawrrd.php.patch
    fi

    # The need for this patch has been communicated to the upstream developer.
    # It has been addressed in the nedi-1.4p3.tgz patch file, but it didn't
    # make it into the earlier 1.4p2 patch.  Hence why we wrap this in a
    # conditional.
    if [ $NEDI_TARBALL = nedi-1.4.tgz -a "$NEDI_PATCH_TARBALL" = nedi-1.4p2.tgz ]; then
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/sysobj_1.3.6.1.4.1.25506.11.1.4.def.patch
    fi

    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_log_msg.txt.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/nedi.conf.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/nedi.properties
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/nedi_httpd.conf
    svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/META-INF/context.xml
    svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/WEB-INF/jboss-web.xml
    svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/WEB-INF/web.xml
    svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/WEB-INF/jboss-deployment-structure.xml

    # For reasons we don't yet understand, exported files don't come with
    # sensible permissions bits, so we need to set them explicitly here.
    chmod 644 context.xml
    chmod 755 extract_nedi.pl
    chmod 644 jboss-web.xml
    chmod 644 jboss-deployment-structure.xml
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

    cp -p extract_nedi.pl                $NEDI_BUILD_BASE
    cp -p context.xml                    $NEDI_BUILD_BASE/META-INF
    cp -p jboss-deployment-structure.xml $NEDI_BUILD_BASE/WEB-INF
    cp -p jboss-web.xml                  $NEDI_BUILD_BASE/WEB-INF
    cp -p web.xml                        $NEDI_BUILD_BASE/WEB-INF

    echo === Apply GroundWork Patches to the Raw Distribution

    cd $NEDI_BUILD_BASE

    # This patch is very GroundWork-specific, so to make it easy to identify
    # later on that we have modified this file, we purposely leave around a
    # patch-backup file.
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_index.php.patch

    if [ $NEDI_TARBALL = nedi-1.4.tgz -a "$NEDI_PATCH_TARBALL" = nedi-1.4p2.tgz ]; then
	patch -p1 < $NEDI_BUILD_TREE/html_Devices-Status.php.patch
    fi
    if [ $NEDI_TARBALL = nedi-1.4.tgz -a "$NEDI_PATCH_TARBALL" = nedi-1.4p2.tgz ]; then
	patch -p1 < $NEDI_BUILD_TREE/html_Reports-Wlan.php.patch
    fi
    if [ $NEDI_TARBALL = nedi-1.4.tgz -a "$NEDI_PATCH_TARBALL" = nedi-1.4p2.tgz ]; then
	patch -p1 < $NEDI_BUILD_TREE/html_System-Files.php.patch
    fi
    if [ $NEDI_TARBALL = nedi-1.4.tgz -a "$NEDI_PATCH_TARBALL" = nedi-1.4p2.tgz ]; then
	patch -p1 < $NEDI_BUILD_TREE/html_inc_drawrrd.php.patch
    fi

    # The need for this patch has been communicated to the upstream developer.
    # It has been addressed in the nedi-1.4p3.tgz patch file, but it didn't
    # make it into the earlier 1.4p2 patch.  Hence why we wrap this in a
    # conditional.
    if [ $NEDI_TARBALL = nedi-1.4.tgz -a "$NEDI_PATCH_TARBALL" = nedi-1.4p2.tgz ]; then
	patch -p1 < $NEDI_BUILD_TREE/sysobj_1.3.6.1.4.1.25506.11.1.4.def.patch
    fi

    patch -p1 < $NEDI_BUILD_TREE/html_log_msg.txt.patch
    patch -p1 < $NEDI_BUILD_TREE/nedi.conf.patch

    echo === Change GroundWork Paths in the Raw Distribution

    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i inc/devwrite.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i master.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i moni.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i nedi.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i stati.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i syslog.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i trap.pl

elif [ $NEDI_VERSION = "1.5.225" ]; then

    # FIX MINOR:  STILL TO FIX FOR THIS RELEASE:
    # (*) Suppress certain NeDi menus in the nedi.conf we ship (GWMON-12042).
    # (*) Test by going into the User Profile and attempting to change the timezone.
    # (*) Test logging in as "user" instead of as "admin".
    # (*) Look at other historical NeDi-related JIRAs to see if any of them can be dealt with at this time.
    # (*) Review email exchanges with Remo at the end of October 2014 and the beginning of November 2014,
    #     about residual bugs that got noticed after the nedi-1.4.tgz tarball was released but likely were
    #     not later included in the 1.4p2 patch.  (At least some of them, if not all, have been addressed
    #     in the nedi-1.4p3.tgz patch.)

    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/$NEDI_TARBALL
    if [ -n "$NEDI_PATCH_TARBALL" ]; then
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/$NEDI_PATCH_TARBALL
    fi

    echo === Unpack and Patch the Raw Distribution

    /bin/rm -rf $NEDI_BUILD_BASE
    mkdir -p    $NEDI_BUILD_BASE
    cd          $NEDI_BUILD_BASE

    # Tarballs for this release direct from the Nedi site are not actually
    # compressed (though the .tgz extension makes it seem so).  So we don't
    # use the "tar z" option here.
    #
    tar xf $NEDI_BUILD_TREE/$NEDI_TARBALL
    if [ -n "$NEDI_PATCH_TARBALL" ]; then
	tar xf $NEDI_BUILD_TREE/$NEDI_PATCH_TARBALL
    fi

    echo === Clean Up the Raw Distribution

    # FIX LATER:  Verify whether any of these commands are still needed whenever
    # we get a new release.  Kick any new requirements here back to the upstream
    # NeDi developer.
    # (*) Permissions on the $NEDI_BUILD_BASE directory are 755 in the
    #     tarball, but when the tarball is unrolled, those permissions don't
    #     override any more-lenient permissions already established for this
    #     directory, so we set them explicitly here on that directory.
    # (*) Permissions on the $NEDI_BUILD_BASE/nedi.conf file directory are 644
    #     in the standard distribution.  But we include some DB credentials in
    #     this file, so we want to be more restrictive.
    chmod 755 $NEDI_BUILD_BASE
    chmod 600 $NEDI_BUILD_BASE/nedi.conf

    # Fix modes on certain directories and files which shipped with much-too-open
    # permissions.
    #
    # FIX LATER:  Verify this stuff on every new release, since the set of
    # directories and files that need such corrections shifts around with
    # every upstream distribution.
    if [ $NEDI_TARBALL = nedi-1.5.225.tgz ]; then
	chmod 644 $NEDI_BUILD_BASE/html/*/*/*.css
	chmod 644 $NEDI_BUILD_BASE/html/*/*/*.html
	chmod 644 $NEDI_BUILD_BASE/html/img/*/*.png
	chmod 644 $NEDI_BUILD_BASE/html/img/favicon.ico
	chmod 644 $NEDI_BUILD_BASE/html/img/tel.png
	chmod 644 $NEDI_BUILD_BASE/html/log/Readme.txt
	chmod 644 $NEDI_BUILD_BASE/html/log/iftools.php
	chmod 644 $NEDI_BUILD_BASE/html/themes/*.css
	chmod 644 $NEDI_BUILD_BASE/html/themes/*.jpg
	chmod 644 $NEDI_BUILD_BASE/html/themes/*.png
    fi

    # This is no longer needed in the 1.4 release.  But it must be checked again
    # when we receive future upstream distributions.
    ## chmod -x $NEDI_BUILD_BASE/sysobj/*.def

    # This is needed for one file after the preliminary 1.4p3 patch is applied,
    # so we just go ahead and apply it in a blanket fashion to ensure that all
    # similar files also have clean permissions.
    chmod go-w $NEDI_BUILD_BASE/sysobj/*.def

    # ====================================================================
    # At this point, we have built the standard distribution with only
    # slight cleanups.  The remaining steps apply the additional changes
    # needed to run NeDi in the GroundWork Monitor context.
    # ====================================================================

    echo === Stage the GroundWork Patch Files and New Files

    cd $NEDI_BUILD_TREE

    # nedi.properties and nedi_httpd.conf won't be used by this script.  We export them here so the
    # associated build scripting can pick them up without itself checking them out from Subversion.

    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/extract_nedi.pl
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_index.php.patch

    # The NeDi PHP code dereferences a null object in some places, resulting
    # in confusing on-screen warnings.  This patch sidesteps the inappropriate
    # null-object dereferences.
    #
    # The nedi-1.4p3.tgz patch file corrects the problem natively in the
    # System-Files.php file, so our own patch is now only applied conditionally.
    #
    if [ $NEDI_TARBALL = nedi-1.5.225.tgz -a "$NEDI_PATCH_TARBALL" = nedi-1.5p1.tgz ]; then
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_System-Files.php.patch
    fi

    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_log_msg.txt.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/nedi.conf.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/nedi.properties
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/nedi_httpd.conf
    svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/META-INF/context.xml
    svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/WEB-INF/jboss-web.xml
    svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/WEB-INF/web.xml
    svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/WEB-INF/jboss-deployment-structure.xml

    # For reasons we don't yet understand, exported files don't come with
    # sensible permissions bits, so we need to set them explicitly here.
    chmod 644 context.xml
    chmod 755 extract_nedi.pl
    chmod 644 jboss-web.xml
    chmod 644 jboss-deployment-structure.xml
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

    cp -p extract_nedi.pl                $NEDI_BUILD_BASE
    cp -p context.xml                    $NEDI_BUILD_BASE/META-INF
    cp -p jboss-deployment-structure.xml $NEDI_BUILD_BASE/WEB-INF
    cp -p jboss-web.xml                  $NEDI_BUILD_BASE/WEB-INF
    cp -p web.xml                        $NEDI_BUILD_BASE/WEB-INF

    echo === Apply GroundWork Patches to the Raw Distribution

    cd $NEDI_BUILD_BASE

    # This patch is very GroundWork-specific, so to make it easy to identify
    # later on that we have modified this file, we purposely leave around a
    # patch-backup file.
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_index.php.patch

    if [ $NEDI_TARBALL = nedi-1.5.225.tgz -a "$NEDI_PATCH_TARBALL" = nedi-1.5p1.tgz ]; then
	patch -p1 < $NEDI_BUILD_TREE/html_System-Files.php.patch
    fi

    # File is in DOS EOL and needs to be converted
    dos2unix $NEDI_BUILD_BASE/html/log/msg.txt

    patch -p1 < $NEDI_BUILD_TREE/html_log_msg.txt.patch
    patch -p1 < $NEDI_BUILD_TREE/nedi.conf.patch

    echo === Change GroundWork Paths in the Raw Distribution

    # In each new release, we must check this list of adjustments, find out whether whether
    # any of these files have disappeared, and whether there are also others we must modify.
    #
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i contrib/flood.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i exe/asa-inventory.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i inc/devwrite.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i master.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i moni.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i nedi.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i stati.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i syslog.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i trap.pl

elif [ $NEDI_VERSION = "1.6.100" ]; then

    # FIX MAJOR:  STILL TO FIX FOR THIS RELEASE:
    # (*) Test by going into the User Profile and attempting to change the timezone.
    # (*) Test logging in as "user" instead of as "admin".
    # (*) Look at other historical NeDi-related JIRAs to see if any of them can be dealt with at this time.
    # (*) Review email exchanges with Remo at the end of October 2014 and the beginning of November 2014,
    #     about residual bugs that got noticed after the nedi-1.4.tgz tarball was released but likely were
    #     not later included in the 1.4p2 patch.  (At least some of them, if not all, have been addressed
    #     in the nedi-1.4p3.tgz patch.)

    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/$NEDI_TARBALL
    if [ -n "$NEDI_PATCH_TARBALL" ]; then
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/$NEDI_PATCH_TARBALL
    fi

    echo === Unpack and Patch the Raw Distribution

    /bin/rm -rf $NEDI_BUILD_BASE
    mkdir -p    $NEDI_BUILD_BASE
    cd          $NEDI_BUILD_BASE

    # Tarballs for this release direct from the Nedi site are not actually
    # compressed (though the .tgz extension makes it seem so).  So we locally
    # renamed and compressed them, using .tar.gz extensions to distinguish these
    # files from the originals, and we therefore use the "tar z" option here.
    #
    tar xfz $NEDI_BUILD_TREE/$NEDI_TARBALL
    if [ -n "$NEDI_PATCH_TARBALL" ]; then
	tar xfz $NEDI_BUILD_TREE/$NEDI_PATCH_TARBALL
    fi

    echo === Clean Up the Raw Distribution

    # FIX LATER:  Verify whether any of these commands are still needed whenever
    # we get a new release.  Kick any new requirements here back to the upstream
    # NeDi developer.
    #
    # (*) Permissions on the $NEDI_BUILD_BASE directory are 755 in the
    #     tarball, but when the tarball is unrolled, those permissions don't
    #     override any more-lenient permissions already established for this
    #     directory, so we set them explicitly here on that directory.
    # (*) Permissions on the $NEDI_BUILD_BASE/nedi.conf file directory are 644
    #     in the standard distribution.  But we include some DB credentials in
    #     this file, so we want to be more restrictive.
    #
    chmod 755 $NEDI_BUILD_BASE
    chmod 600 $NEDI_BUILD_BASE/nedi.conf

    # Fix modes on certain directories and files which shipped with much-too-open
    # permissions.
    #
    # FIX LATER:  Verify this stuff on every new release, since the set of
    # directories and files that need such corrections shifts around with
    # every upstream distribution.
    #
    if [ $NEDI_TARBALL = nedi-1.6.100.tar.gz ]; then
	# After careful checking, we see that the 1.6.100 release doesn't need any of
	# these adjustments, because all the files are delivered in fine shape.  But
	# we still run these adjustments anyway, to provide a model for any future
	# releases where things upstream might go awry and we then need them.
	chmod 644 $NEDI_BUILD_BASE/html/*/*/*.css
	chmod 644 $NEDI_BUILD_BASE/html/*/*/*.html
	chmod 644 $NEDI_BUILD_BASE/html/img/*/*.png
	chmod 644 $NEDI_BUILD_BASE/html/img/favicon.ico
	chmod 644 $NEDI_BUILD_BASE/html/img/tel.png
	chmod 644 $NEDI_BUILD_BASE/html/log/Readme.txt
	chmod 644 $NEDI_BUILD_BASE/html/log/iftools.php
	chmod 644 $NEDI_BUILD_BASE/html/themes/*.css
	chmod 644 $NEDI_BUILD_BASE/html/themes/*.jpg
	chmod 644 $NEDI_BUILD_BASE/html/themes/*.png
    fi

    # This is no longer needed in the 1.6.100 release.  But it must be
    # checked again when we receive future upstream distributions.
    ## chmod -x $NEDI_BUILD_BASE/sysobj/*.def

    # This is no longer needed in the 1.6.100 release.  But it must be
    # checked again when we receive future upstream distributions.
    ## chmod go-w $NEDI_BUILD_BASE/sysobj/*.def

    # ====================================================================
    # At this point, we have built the standard distribution with only
    # slight cleanups.  The remaining steps apply the additional changes
    # needed to run NeDi in the GroundWork Monitor context.
    # ====================================================================

    echo === Stage the GroundWork Patch Files and New Files

    cd $NEDI_BUILD_TREE

    # nedi.properties and nedi_httpd.conf won't be used by this script.  We export them here so the
    # associated build scripting can pick them up without itself checking them out from Subversion.

    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/extract_nedi.pl

    if [ $NEDI_TARBALL = nedi-1.6.100.tar.gz -a "$NEDI_PATCH_TARBALL" = nedi-1.6p2.tar.gz ]; then
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_index.php.patch-p2
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_inc_libdb-pg.php.patch-p2
    fi
    if [ $NEDI_TARBALL = nedi-1.6.100.tar.gz -a "$NEDI_PATCH_TARBALL" = nedi-1.6p3.tgz ]; then
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_index.php.patch-p3
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_inc_libdb-pg.php.patch-p3
    fi

    # This patch fixes a typo in the upstream code.
    #
    if [ $NEDI_TARBALL = nedi-1.6.100.tar.gz -a "$NEDI_PATCH_TARBALL" = nedi-1.6p2.tar.gz ]; then
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/cusdi.pl.patch
    fi

    # The NeDi PHP code dereferences a null object in some places, resulting
    # in confusing on-screen warnings.  This patch sidesteps the inappropriate
    # null-object dereferences.
    #
    # This might eventually be (but is not yet) fixed upstream natively in the
    # System-Files.php file, so our own patch is only applied conditionally.
    #
    # This patch now also includes a fix to undo the effects of having PHP
    # Magic Quotes in effect.  That part, we will need to remove once we
    # move to a PHP that does not support Magic Quotes.
    #
    if [ $NEDI_TARBALL = nedi-1.6.100.tar.gz -a "$NEDI_PATCH_TARBALL" = nedi-1.6p2.tar.gz ]; then
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_System-Files.php.patch
    fi
    if [ $NEDI_TARBALL = nedi-1.6.100.tar.gz -a "$NEDI_PATCH_TARBALL" = nedi-1.6p3.tgz ]; then
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_System-Files.php.patch
    fi

    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_log_msg.txt.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/nedi.conf.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/nedi.properties
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/nedi_httpd.conf
    svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/META-INF/context.xml
    svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/WEB-INF/jboss-web.xml
    svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/WEB-INF/web.xml
    svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/WEB-INF/jboss-deployment-structure.xml

    # These are a set of patches which are mainly designed to better support
    # a "groundwork.css" visual theme for NeDi, to allow its look and feel to
    # integrate better into the GroundWork Monitor environment.  In some cases,
    # these are pure fixes to the GUI behavior, that we hope the upstream
    # maintainer will adopt.  So we will need to screen future releases of NeDi
    # to see which of these patches still need to be applied.
    #
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_Devices-Config.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_Devices-Graph.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_Devices-Write.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_Nodes-RogueAP.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_Nodes-Status.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_Reports-Combination.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_Reports-Devices.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_Reports-Interfaces.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_Reports-Modules.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_Reports-Monitoring.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_Reports-Networks.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_Reports-Nodes.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_System-Database.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_System-Services.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_inc_header.php.patch
    if [ $NEDI_TARBALL = nedi-1.6.100.tar.gz -a "$NEDI_PATCH_TARBALL" = nedi-1.6p2.tar.gz ]; then
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/inc_libmisc.pm.patch
    fi
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/groundwork.css

    # The remo-groundwork.css theme was the first attempt to provide a
    # GroundWork-compatible look and feel for NeDi.  We included it for
    # a moment as a point of comparision, but have decided it is not a
    # sufficiently interesting theme to adopt.  Instead, we now recommend
    # the groundwork.css theme.
    #
    # svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/remo-groundwork.css

    # For reasons we don't yet understand, exported files don't come with
    # sensible permissions bits, so we need to set them explicitly here.
    chmod 644 context.xml
    chmod 755 extract_nedi.pl
    chmod 644 jboss-web.xml
    chmod 644 jboss-deployment-structure.xml
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

    cp -p extract_nedi.pl                $NEDI_BUILD_BASE
    cp -p context.xml                    $NEDI_BUILD_BASE/META-INF
    cp -p jboss-deployment-structure.xml $NEDI_BUILD_BASE/WEB-INF
    cp -p jboss-web.xml                  $NEDI_BUILD_BASE/WEB-INF
    cp -p web.xml                        $NEDI_BUILD_BASE/WEB-INF

    echo === Apply GroundWork Patches to the Raw Distribution

    cd $NEDI_BUILD_BASE

    # These patches are very GroundWork-specific, so to make it easy to identify
    # later on that we have modified these files, we purposely leave around a
    # patch-backup file.
    if [ $NEDI_TARBALL = nedi-1.6.100.tar.gz -a "$NEDI_PATCH_TARBALL" = nedi-1.6p2.tar.gz ]; then
	patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_index.php.patch-p2
	patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_inc_libdb-pg.php.patch-p2
    fi
    if [ $NEDI_TARBALL = nedi-1.6.100.tar.gz -a "$NEDI_PATCH_TARBALL" = nedi-1.6p3.tgz ]; then
	patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_index.php.patch-p3
	patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_inc_libdb-pg.php.patch-p3
    fi

    # This patch fixes an upstream typo.  We only leave around a backup file so
    # we can more readily identify in the future exactly what the original code
    # looked like, in case we need to regnerate this patch.
    #
    if [ $NEDI_TARBALL = nedi-1.6.100.tar.gz -a "$NEDI_PATCH_TARBALL" = nedi-1.6p2.tar.gz ]; then
	patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/cusdi.pl.patch
    fi

    if [ $NEDI_TARBALL = nedi-1.6.100.tar.gz -a "$NEDI_PATCH_TARBALL" = nedi-1.6p2.tar.gz ]; then
	patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_System-Files.php.patch
    fi
    if [ $NEDI_TARBALL = nedi-1.6.100.tar.gz -a "$NEDI_PATCH_TARBALL" = nedi-1.6p3.tgz ]; then
	patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_System-Files.php.patch
    fi

    # These files are in DOS EOL and need to be converted.  We must scan any
    # new release for such files and make adjustments to this list as needed.
    #
    dos2unix $NEDI_BUILD_BASE/html/log/msg.txt
    dos2unix $NEDI_BUILD_BASE/html/inc/leaflet.css
    dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.9.1.852.def
    dos2unix $NEDI_BUILD_BASE/inc/iab.txt
    dos2unix $NEDI_BUILD_BASE/inc/oui.txt

    patch -p1 < $NEDI_BUILD_TREE/html_log_msg.txt.patch
    patch -p1 < $NEDI_BUILD_TREE/nedi.conf.patch

    # Fix certain HTML to either better integrate with the "monarch" theme,
    # or in some cases to just operate or look better.
    #
    # For the first cut of applying these patches, to make it easy to identify
    # later on that we have modified these files, we purposely leave around a
    # patch-backup file.
    #
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_Devices-Config.php.patch
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_Devices-Graph.php.patch
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_Devices-Write.php.patch
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_Nodes-RogueAP.php.patch
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_Nodes-Status.php.patch
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_Reports-Combination.php.patch
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_Reports-Devices.php.patch
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_Reports-Interfaces.php.patch
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_Reports-Modules.php.patch
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_Reports-Monitoring.php.patch
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_Reports-Networks.php.patch
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_Reports-Nodes.php.patch
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_System-Database.php.patch
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_System-Services.php.patch
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_inc_header.php.patch
    if [ $NEDI_TARBALL = nedi-1.6.100.tar.gz -a "$NEDI_PATCH_TARBALL" = nedi-1.6p2.tar.gz ]; then
	patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/inc_libmisc.pm.patch
    fi

    # Install a new NeDi theme.  The remo-groundwork.css theme was experimental,
    # and is not useful enough to be included.
    cp -p $NEDI_BUILD_TREE/groundwork.css      $NEDI_BUILD_BASE/html/themes/
    # cp -p $NEDI_BUILD_TREE/remo-groundwork.css $NEDI_BUILD_BASE/html/themes/

    echo === Change GroundWork Paths in the Raw Distribution

    # In each new release, we must check this list of adjustments, find out whether whether
    # any of these files have disappeared, and whether there are also others we must modify.
    #
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i contrib/cloudlink.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i contrib/flood.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i cusdi.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i exe/asa-inventory.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i flowi.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i inc/devwrite.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i master.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i moni.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i nedi.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i stati.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i syslog.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i test.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i trap.pl

elif [ $NEDI_VERSION = "1.7.090" ]; then

    # FIX MAJOR:  STILL TO FIX FOR THIS RELEASE:
    # (*) The upstream maintainer is finalizing patch #1 for NeDi 1.7.090.  We have not yet received it,
    #     but we will need to apply it here once available.
    # (*) Test by going into the User Profile and attempting to change the timezone.
    # (*) Test logging in as "user" instead of as "admin".
    # (*) Test the upgrade capability of this release.  We'll probably want to modify our pg_migrate_nedi.pl
    #     script to execute that code instead of just printing out a message about re-initializing the whole
    #     database from scratch.  We may find some very old initial databases, and not be able to migrate
    #     them.  The existing upgrade code from upstream might not check that carefully; we must understand
    #     exactly what it is capable of, and adapt accordingly.
    # (*) Look at other historical NeDi-related JIRAs to see if any of them can be dealt with at this time.
    # (*) Review email exchanges with Remo at the end of October 2014 and the beginning of November 2014,
    #     about residual bugs that got noticed after the nedi-1.4.tgz tarball was released but likely were
    #     not later included in the 1.4p2 patch.  (At least some of them, if not all, have been addressed
    #     in the nedi-1.4p3.tgz patch.)

    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/$NEDI_TARBALL
    if [ -n "$NEDI_PATCH_TARBALL" ]; then
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/$NEDI_PATCH_TARBALL
    fi

    echo === Unpack and Patch the Raw Distribution

    /bin/rm -rf $NEDI_BUILD_BASE
    mkdir -p    $NEDI_BUILD_BASE
    cd          $NEDI_BUILD_BASE

    tar xfz $NEDI_BUILD_TREE/$NEDI_TARBALL
    if [ -n "$NEDI_PATCH_TARBALL" ]; then
	tar xfz $NEDI_BUILD_TREE/$NEDI_PATCH_TARBALL
	## The upstream maintainer left around an extra copy of this file, for no apparent reason.
	## It doesn't do us any good, so we just remove it.
	# FIX MAJOR:  Put this back once we have validated that the extra file contains no info of interest.
	# rm -f 'nedi/inc/libdb (copy).pm'
    fi

    echo === Clean Up the Raw Distribution

    # FIX LATER:  Verify whether any of these commands are still needed whenever
    # we get a new release.  Kick any new requirements here back to the upstream
    # NeDi developer.
    #
    # (*) Permissions on the $NEDI_BUILD_BASE directory are not set in the
    #     upstream tarball for the 1.7.090 release, because unlike previous
    #     releases, that tarball does not contain the base directory.  So we
    #     set the permissions here explicitly on that directory.
    # (*) Permissions on the $NEDI_BUILD_BASE/nedi.conf file directory are 644
    #     in the standard distribution.  But we include some DB credentials in
    #     this file, so we want to be more restrictive.
    #
    chmod 755 $NEDI_BUILD_BASE
    chmod 600 $NEDI_BUILD_BASE/nedi.conf

    # Fix modes on certain directories and files which shipped with much-too-open
    # permissions.
    #
    # FIX LATER:  Verify this stuff on every new release, since the set of
    # directories and files that need such corrections shifts around with
    # every upstream distribution.
    #
    if [ $NEDI_TARBALL = nedi-1.7.090.tgz ]; then
	# After careful checking, we see that the base 1.7.090 release needs only some of
	# these adjustments, because almost all the files are delivered in fine shape.
	# The upcoming NeDi 1.7 patch #1 might fix the few remaining oddities.
	# But we still run these adjustments anyway, to provide a model for any future
	# releases where things upstream might go awry and we then need them.
	chmod 644 $NEDI_BUILD_BASE/html/*/*.css
	chmod 644 $NEDI_BUILD_BASE/html/*/*/*.html
	chmod 644 $NEDI_BUILD_BASE/html/img/*/*.png
	chmod 644 $NEDI_BUILD_BASE/html/img/favicon.ico
	chmod 644 $NEDI_BUILD_BASE/html/img/tel.png
	chmod 644 $NEDI_BUILD_BASE/html/log/Readme.txt
	chmod 644 $NEDI_BUILD_BASE/html/log/iftools.php
	chmod 644 $NEDI_BUILD_BASE/html/themes/*.css
	chmod 644 $NEDI_BUILD_BASE/html/themes/*.jpg
	chmod 644 $NEDI_BUILD_BASE/html/themes/*.png
    fi

    # This is still needed in the base 1.7.090 release, though it might be fixed in patch #1 for
    # this release.  It must be checked again when we receive future upstream distributions.
    chmod -x $NEDI_BUILD_BASE/sysobj/*.def

    # This is still needed in the base 1.7.090 release, though it might be fixed in patch #1 for
    # this release.  It must be checked again when we receive future upstream distributions.
    chmod go-w $NEDI_BUILD_BASE/sysobj/*.def

    # This is special in the base 1.7.090 release, though it might be fixed in patch #1 for
    # this release.  It must be checked again when we receive future upstream distributions.
    chmod o+r $NEDI_BUILD_BASE/sysobj/*.def

    # ====================================================================
    # At this point, we have built the standard distribution with only
    # slight cleanups.  The remaining steps apply the additional changes
    # needed to run NeDi in the GroundWork Monitor context.
    # ====================================================================

    echo === Stage the GroundWork Patch Files and New Files

    cd $NEDI_BUILD_TREE

    # nedi.properties and nedi_httpd.conf won't be used by this script.  We export them here so the
    # associated build scripting can pick them up without itself checking them out from Subversion.

    # The following patch fixes problems with the PostgreSQL implementation of the "nedi"
    # database in the base 1.7.090 release.  The issues fixed here will presumably be
    # addressed in the forthcoming upstream patch #1 for NeDi 1.7.090.  So we make the
    # application of this GroundWork patch be conditional on not having a patch tarball
    # in play for this release.  That presumption will need to be verified, and this
    # comment modified, once patch #1 becomes available.
    if [ $NEDI_TARBALL = nedi-1.7.090.tgz -a -z "$NEDI_PATCH_TARBALL" ]; then
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/inc_libdb.pm.patch
    fi

    # The following GroundWork-specific patch should be applied regardless of whether
    # we have an upstream patch for the inc/libdb.pm file.  This patch defaults the
    # "groundwork" theme if the "nedi" database is reinitialized.
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/inc_libdb.pm.patch.2

    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/extract_nedi.pl

    if [ $NEDI_TARBALL = nedi-1.7.090.tgz ]; then
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_index.php.patch
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_inc_libdb-pg.php.patch
    fi

    # The NeDi PHP code dereferences a null object in some places, resulting
    # in confusing on-screen warnings.  This patch sidesteps the inappropriate
    # null-object dereferences.
    #
    # This might eventually be (but is not yet) fixed upstream natively in the
    # System-Files.php file, so our own patch is only applied conditionally.
    #
    if [ $NEDI_TARBALL = nedi-1.7.090.tgz ]; then
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_System-Files.php.patch
    fi

    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_log_msg.txt.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/nedi.conf.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/nedi.properties
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/nedi_httpd.conf
    svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/META-INF/context.xml
    svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/WEB-INF/jboss-web.xml
    svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/WEB-INF/web.xml
    svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/WEB-INF/jboss-deployment-structure.xml

    # These are a set of patches which are mainly designed to better support a "groundwork.css"
    # visual theme for NeDi, to allow its look and feel to integrate better into the GroundWork
    # Monitor environment.  In some cases, these are pure fixes to the GUI behavior, that we
    # hope the upstream maintainer will adopt.  (Many of our fixes for NeDi 1.6.100 were in
    # fact adopted upstream for the NeDi 1.7.090 release.)  So we will need to screen future
    # releases of NeDi to see which of these patches still need to be applied.
    #
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_Nodes-RogueAP.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_Reports-Combination.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_Reports-Devices.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_Reports-Interfaces.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_Reports-Modules.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_Reports-Monitoring.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_Reports-Networks.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_Reports-Nodes.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/groundwork.css

    # For reasons we don't yet understand, exported files don't come with
    # sensible permissions bits, so we need to set them explicitly here.
    chmod 644 context.xml
    chmod 755 extract_nedi.pl
    chmod 644 jboss-web.xml
    chmod 644 jboss-deployment-structure.xml
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

    cp -p extract_nedi.pl                $NEDI_BUILD_BASE
    cp -p context.xml                    $NEDI_BUILD_BASE/META-INF
    cp -p jboss-deployment-structure.xml $NEDI_BUILD_BASE/WEB-INF
    cp -p jboss-web.xml                  $NEDI_BUILD_BASE/WEB-INF
    cp -p web.xml                        $NEDI_BUILD_BASE/WEB-INF

    echo === Apply GroundWork Patches to the Raw Distribution

    cd $NEDI_BUILD_BASE

    # The following patch fixes problems with the PostgreSQL implementation of the "nedi"
    # database in the base 1.7.090 release.  The issues fixed here will presumably be
    # addressed in the forthcoming upstream patch #1 for NeDi 1.7.090.  So we make the
    # application of this GroundWork patch be conditional on not having a patch tarball
    # in play for this release.  That presumption will need to be verified, and this
    # comment modified, once patch #1 becomes available.
    if [ $NEDI_TARBALL = nedi-1.7.090.tgz -a -z "$NEDI_PATCH_TARBALL" ]; then
	patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/inc_libdb.pm.patch
    fi

    # The following GroundWork-specific patch should be applied regardless of whether
    # we have an upstream patch for the inc/libdb.pm file.  This patch defaults the
    # "groundwork" theme if the "nedi" database is reinitialized.
    patch -b -V simple -z .pre_gw_theme -p1 < $NEDI_BUILD_TREE/inc_libdb.pm.patch.2

    # These patches are very GroundWork-specific, so to make it easy to identify
    # later on that we have modified these files, we purposely leave around a
    # patch-backup file.
    if [ $NEDI_TARBALL = nedi-1.7.090.tgz ]; then
	patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_index.php.patch
	patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_inc_libdb-pg.php.patch
    fi

    if [ $NEDI_TARBALL = nedi-1.7.090.tgz ]; then
	patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_System-Files.php.patch
    fi

    # In the base 1.7.090 release, these files are in DOS EOL and need to be converted.
    # Many or all of these might be converted by patch #1 for the 1.7.090 release, since
    # the mechanisms for finding and fixing them have been percolated upstream.  But we
    # do see this sort of thing creep in again in successive major upstream releases, so
    # we must always scan any new release for such files and make adjustments to this
    # list as needed.
    #
    dos2unix $NEDI_BUILD_BASE/html/log/msg.txt
    dos2unix $NEDI_BUILD_BASE/html/inc/leaflet.css
    dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.25506.11.1.172.def
    dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.25506.11.1.188.def
    dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.25506.11.1.189.def
    dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.25506.11.1.33.def
    dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.25506.11.1.34.def
    dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.9.1.1042n.def
    dos2unix $NEDI_BUILD_BASE/sysobj/1.3.6.1.4.1.9.1.495.def
    dos2unix $NEDI_BUILD_BASE/inc/iab.csv
    dos2unix $NEDI_BUILD_BASE/inc/oui.csv
    dos2unix $NEDI_BUILD_BASE/inc/cid.csv
    dos2unix $NEDI_BUILD_BASE/inc/oui36.csv
    dos2unix $NEDI_BUILD_BASE/inc/mam.csv
    dos2unix $NEDI_BUILD_BASE/html/languages/espanol/gui.php
    dos2unix $NEDI_BUILD_BASE/html/inc/phpqrcode.php
    dos2unix $NEDI_BUILD_BASE/html/inc/qr.php

    patch -p1 < $NEDI_BUILD_TREE/html_log_msg.txt.patch
    patch -p1 < $NEDI_BUILD_TREE/nedi.conf.patch

    # Fix certain HTML to either better integrate with the "monarch" theme,
    # or in some cases to just operate or look better.
    #
    # For the first cut of applying these patches, to make it easy to identify
    # later on that we have modified these files, we purposely leave around a
    # patch-backup file.
    #
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_Nodes-RogueAP.php.patch
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_Reports-Combination.php.patch
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_Reports-Devices.php.patch
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_Reports-Interfaces.php.patch
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_Reports-Modules.php.patch
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_Reports-Monitoring.php.patch
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_Reports-Networks.php.patch
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_Reports-Nodes.php.patch

    # Install a new NeDi theme, to blend well with the GroundWork context.
    cp -p $NEDI_BUILD_TREE/groundwork.css $NEDI_BUILD_BASE/html/themes/

    echo === Change GroundWork Paths in the Raw Distribution

    # In each new release, we must check this list of adjustments, find out whether whether
    # any of these files have disappeared, and whether there are also others we must modify.
    #
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i contrib/cloudlink.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i contrib/flood.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i cusdi.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i exe/asa-inventory.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i flowi.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i inc/devwrite.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i master.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i moni.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i nedi.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i stati.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i syslog.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i test.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i trap.pl

elif [ $NEDI_VERSION = "1.8.100" ]; then

    # FIX MAJOR:  STILL TO FIX FOR THIS RELEASE:
    # (*) Test by going into the User Profile and attempting to change the timezone.
    # (*) Test logging in as "user" instead of as "admin".
    # (*) Test the upgrade capability of this release.  We'll probably want to modify our pg_migrate_nedi.pl
    #     script to execute that code instead of just printing out a message about re-initializing the whole
    #     database from scratch.  We may find some very old initial databases, and not be able to migrate
    #     them.  The existing upgrade code from upstream might not check that carefully; we must understand
    #     exactly what it is capable of, and adapt accordingly.
    # (*) Look at other historical NeDi-related JIRAs to see if any of them can be dealt with at this time.
    # (*) Review email exchanges with Remo at the end of October 2014 and the beginning of November 2014,
    #     about residual bugs that got noticed after the nedi-1.4.tgz tarball was released but likely were
    #     not later included in the 1.4p2 patch.  (At least some of them, if not all, have been addressed
    #     in the nedi-1.4p3.tgz patch.)

    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/$NEDI_TARBALL
    if [ -n "$NEDI_PATCH_TARBALL" ]; then
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/$NEDI_PATCH_TARBALL
    fi
    if [ -n "$NODI_TARBALL" ]; then
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/$NODI_TARBALL
    fi

    echo === Unpack and Patch the Raw Distribution

    /bin/rm -rf $NEDI_BUILD_BASE
    mkdir -p    $NEDI_BUILD_BASE
    cd          $NEDI_BUILD_BASE

    tar xfz $NEDI_BUILD_TREE/$NEDI_TARBALL
    if [ -n "$NEDI_PATCH_TARBALL" ]; then
	tar xfz $NEDI_BUILD_TREE/$NEDI_PATCH_TARBALL
    fi
    if [ -n "$NODI_TARBALL" ]; then
	tar xfz $NEDI_BUILD_TREE/$NODI_TARBALL
    fi

    echo === Clean Up the Raw Distribution

    # FIX LATER:  Verify whether any of these commands are still needed whenever
    # we get a new release.  Kick any new requirements here back to the upstream
    # NeDi developer.
    #
    # (*) Permissions on the $NEDI_BUILD_BASE directory are not set in the
    #     upstream tarball for the 1.7.090 release, because unlike previous
    #     releases, that tarball does not contain the base directory.  So we
    #     set the permissions here explicitly on that directory.
    # (*) Permissions on the $NEDI_BUILD_BASE/nedi.conf file directory are 644
    #     in the standard distribution.  But there are some DB credentials in
    #     this file, so we want to be more restrictive.
    # (*) Permissions on the $NEDI_BUILD_BASE/nodi.conf file directory are 644
    #     in the standard distribution.  But there are some DB credentials in
    #     this file, so we want to be more restrictive.
    #
    chmod 755 $NEDI_BUILD_BASE
    chmod 600 $NEDI_BUILD_BASE/nedi.conf
    if [ -n "$NODI_TARBALL" ]; then
	chmod 600 $NEDI_BUILD_BASE/nodi.conf
    fi

    # Fix modes on certain directories and files which shipped with much-too-open
    # permissions.
    #
    # FIX LATER:  Verify this stuff on every new release, since the set of
    # directories and files that need such corrections shifts around with
    # every upstream distribution.
    #
    if [ $NEDI_TARBALL = nedi-1.8.100.tgz ]; then
	:
	# After careful checking, we see that the base 1.8.100 release needs none of
	# these adjustments, because all the files are delivered in fine shape.
	# But we still list these adjustments anyway, to provide a model for any future
	# releases where things upstream might go awry and we then need them.
	## chmod 644 $NEDI_BUILD_BASE/html/*/*.css
	## chmod 644 $NEDI_BUILD_BASE/html/*/*/*.html
	## chmod 644 $NEDI_BUILD_BASE/html/img/*/*.png
	## chmod 644 $NEDI_BUILD_BASE/html/img/favicon.ico
	## chmod 644 $NEDI_BUILD_BASE/html/img/tel.png
	## chmod 644 $NEDI_BUILD_BASE/html/log/Readme.txt
	## chmod 644 $NEDI_BUILD_BASE/html/log/iftools.php
	## chmod 644 $NEDI_BUILD_BASE/html/themes/*.css
	## chmod 644 $NEDI_BUILD_BASE/html/themes/*.jpg
	## chmod 644 $NEDI_BUILD_BASE/html/themes/*.png
    fi

    # This is fine in the base 1.8.100 release.
    # It must be checked again when we receive future upstream distributions.
    ## chmod -x $NEDI_BUILD_BASE/sysobj/*.def

    # This is fine in the base 1.8.100 release.
    # It must be checked again when we receive future upstream distributions.
    ## chmod go-w $NEDI_BUILD_BASE/sysobj/*.def

    # This is fine in the base 1.8.100 release.
    # It must be checked again when we receive future upstream distributions.
    ## chmod o+r $NEDI_BUILD_BASE/sysobj/*.def

    # ====================================================================
    # At this point, we have built the standard distribution with only
    # slight cleanups.  The remaining steps apply the additional changes
    # needed to run NeDi in the GroundWork Monitor context.
    # ====================================================================

    echo === Stage the GroundWork Patch Files and New Files

    cd $NEDI_BUILD_TREE

    # The following patch fixes problems with the PostgreSQL implementation of the "nedi"
    # database in the base 1.8.100 release.  Part of this is specific to GroundWork (the
    # fact that we might encounter both 1.7.090 and 1.7.090p1 as the listed version before
    # updating the database) and part is due to some mistakes in the upstream code.  This
    # patch might need to be revisited if we were to receive a 1.8.100p1 patch from upstream.
    if [ $NEDI_TARBALL = nedi-1.8.100.tgz ]; then
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/inc_libdb.pm.patch
    fi

    # The following GroundWork-specific patch should be applied regardless of whether
    # we have an upstream patch for the inc/libdb.pm file.  This patch defaults the
    # "groundwork" theme if the "nedi" database is reinitialized.
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/inc_libdb.pm.patch.2

    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/extract_nedi.pl

    if [ $NEDI_TARBALL = nedi-1.8.100.tgz ]; then
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_index.php.patch
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_inc_libdb-pg.php.patch
    fi

    # The NeDi PHP code dereferences a null object in some places, resulting
    # in confusing on-screen warnings.  This patch sidesteps the inappropriate
    # null-object dereferences.
    #
    # This might eventually be (but is not yet) fixed upstream natively in the
    # System-Files.php file, so our own patch is only applied conditionally.
    #
    if [ $NEDI_TARBALL = nedi-1.8.100.tgz ]; then
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_System-Files.php.patch
    fi

    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_log_msg.txt.patch

    # We make adjustments to various NeDi configuration settings, including the list
    # of screens that we are willing to expose by default in the GroundWork context.
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/nedi.conf.patch
    if [ -n "$NODI_TARBALL" ]; then
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/nodi.conf.patch
	svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/nodi.pl.patch
    fi

    # nedi.properties and nedi_httpd.conf won't be used by this script, except to adjust their file
    # metadata (permissions and ownership) for the build.  We export them here so the associated
    # build scripting can pick them up without itself checking them out from Subversion.
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/nedi.properties
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/nedi_httpd.conf

    # Files to support NeDi as a portal application.
    svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/META-INF/context.xml
    svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/WEB-INF/jboss-web.xml
    svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/WEB-INF/web.xml
    svn_export $SVN_CREDENTIALS $NEDI_PORTAL_SVN_BASE/WEB-INF/jboss-deployment-structure.xml

    # These are a set of patches which are mainly designed to better support a "groundwork.css"
    # visual theme for NeDi, to allow its look and feel to integrate better into the GroundWork
    # Monitor environment.  In some cases, these are pure fixes to the GUI behavior, that we
    # hope the upstream maintainer will adopt.  (Many of our fixes for NeDi 1.6.100 were in
    # fact adopted upstream for the NeDi 1.7.090 release.)  So we will need to screen future
    # releases of NeDi to see which of these patches still need to be applied.
    #
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_Nodes-RogueAP.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_Reports-Combination.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_Reports-Devices.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_Reports-Interfaces.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_Reports-Modules.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_Reports-Monitoring.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_Reports-Networks.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/html_Reports-Nodes.php.patch
    svn_export $SVN_CREDENTIALS $NEDI_SVN_BASE/groundwork.css

    # For reasons we don't yet understand, exported files don't come with
    # sensible permissions bits, so we need to set them explicitly here.
    chmod 644 context.xml
    chmod 755 extract_nedi.pl
    chmod 644 jboss-web.xml
    chmod 644 jboss-deployment-structure.xml
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

    cp -p extract_nedi.pl                $NEDI_BUILD_BASE
    cp -p context.xml                    $NEDI_BUILD_BASE/META-INF
    cp -p jboss-deployment-structure.xml $NEDI_BUILD_BASE/WEB-INF
    cp -p jboss-web.xml                  $NEDI_BUILD_BASE/WEB-INF
    cp -p web.xml                        $NEDI_BUILD_BASE/WEB-INF

    echo === Apply GroundWork Patches to the Raw Distribution

    cd $NEDI_BUILD_BASE

    # The following patch fixes problems with the PostgreSQL implementation of the "nedi"
    # database in the base 1.8.100 release.  Part of this is specific to GroundWork (the
    # fact that we might encounter both 1.7.090 and 1.7.090p1 as the listed version before
    # updating the database) and part is due to some mistakes in the upstream code.  This
    # patch might need to be revisited if we were to receive a 1.8.100p1 patch from upstream.
    if [ $NEDI_TARBALL = nedi-1.8.100.tgz ]; then
	patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/inc_libdb.pm.patch
    fi

    # The following GroundWork-specific patch should be applied regardless of whether
    # we have an upstream patch for the inc/libdb.pm file.  This patch defaults the
    # "groundwork" theme if the "nedi" database is reinitialized.
    patch -b -V simple -z .pre_gw_theme -p1 < $NEDI_BUILD_TREE/inc_libdb.pm.patch.2

    # These patches are very GroundWork-specific, so to make it easy to identify
    # later on that we have modified these files, we purposely leave around a
    # patch-backup file.
    if [ $NEDI_TARBALL = nedi-1.8.100.tgz ]; then
	patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_index.php.patch
	patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_inc_libdb-pg.php.patch
    fi

    if [ $NEDI_TARBALL = nedi-1.8.100.tgz ]; then
	patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_System-Files.php.patch
    fi

    # In the base 1.8.100 release, no files are in DOS EOL and need to be converted.
    # The mechanisms for finding and fixing such files have been percolated upstream.
    # But we do need to check each new release to see if this sort of thing has crept
    # in again, and make adjustments to this list as needed.  Here we just present a
    # couple of example conversion commands that would accomplish the required fixup
    # if it were actually needed.
    #
    ## dos2unix $NEDI_BUILD_BASE/html/log/msg.txt
    ## dos2unix $NEDI_BUILD_BASE/html/inc/leaflet.css

    patch -p1 < $NEDI_BUILD_TREE/html_log_msg.txt.patch

    patch -p1 < $NEDI_BUILD_TREE/nedi.conf.patch
    if [ -n "$NODI_TARBALL" ]; then
	patch -p1 < $NEDI_BUILD_TREE/nodi.conf.patch
	patch -p1 < $NEDI_BUILD_TREE/nodi.pl.patch
    fi

    # Fix certain HTML to either better integrate with the "monarch" theme,
    # or in some cases to just operate or look better.
    #
    # For the first cut of applying these patches, to make it easy to identify
    # later on that we have modified these files, we purposely leave around a
    # patch-backup file.
    #
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_Nodes-RogueAP.php.patch
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_Reports-Combination.php.patch
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_Reports-Devices.php.patch
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_Reports-Interfaces.php.patch
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_Reports-Modules.php.patch
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_Reports-Monitoring.php.patch
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_Reports-Networks.php.patch
    patch -b -V simple -z .pre_gw -p1 < $NEDI_BUILD_TREE/html_Reports-Nodes.php.patch

    # Install a new NeDi theme, to blend well with the GroundWork context.
    cp -p $NEDI_BUILD_TREE/groundwork.css $NEDI_BUILD_BASE/html/themes/

    echo === Change GroundWork Paths in the Raw Distribution

    # In each new release, we must check this list of adjustments, find out whether whether
    # any of these files have disappeared, and whether there are also others we must modify.
    #
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i contrib/cloudlink.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i contrib/flood.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i cusdi.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i exe/asa-inventory.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i flowi.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i inc/devwrite.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i master.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i moni.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i nedi.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i stati.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i syslog.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i test.pl
    perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i trap.pl
    if [ -n "$NODI_TARBALL" ]; then
	perl -pe 's{#!/usr/bin/perl}{#!/usr/local/groundwork/perl/bin/perl}' -i nodi.pl
    fi

else

    echo "ERROR:  Failed to build unknown NeDi version $NEDI_VERSION !"
    exit 1

fi

echo === Change Ownership in the Prepared File Tree

chown -R nagios:nagios $NEDI_BUILD_BASE

echo === Ending the NeDi Build

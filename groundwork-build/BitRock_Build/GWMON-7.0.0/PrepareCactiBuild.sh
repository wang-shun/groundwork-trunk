#!/bin/bash -ex

# Copyright (c) 2012 GroundWork, Inc.  All Rights Reserved.

# This script builds the GroundWork Cacti PHP distribution, starting from the
# standard Cacti 0.8.7g distribution and applying our patches and new files.
# The current script starts with the Cacti 0.8.7g MySQL release as the basis
# for our distribution.  In the future, once the Cacti maintainers fold in our
# PostgreSQL-porting changes, the processing here should be greatly simplified.

# FIX LATER:  In future releases of this script, we might not bother to back up
# most of the Cacti source files as we patch them.  That might hold true, for
# instance, at least for certain classes of patches.

# First, let's set up to report failure to the calling context if we abort.
# We don't exit here, because the bash -e option above takes care of that for us.
report_error() {
    # We don't generate an email if this happens.
    # That duty will be handled by the calling scripts.
    echo "BUILD FAILED:  There has been an error in preparing the Cacti build files."
}

# Note that our trap will not be invoked if an error occurs within a function,
# although the script will still die in that circumstance.  The bash documentation
# makes no exception for this situation, but experience shows that to be a problem.
# One workaround is to have any functions we define return a non-zero exit code if
# it is only controlled errors that we want to report this way.  A more-robust
# alternative is to also establish this trap separately in each function, to also
# catch and report any unexpected errors.
trap report_error ERR

# This definition will be used more broadly below, in the future.  That should
# occur once the Cacti team folds our PostgreSQL porting patches into their base
# release.  In the meantime, we still use exact pathnames for files that relate
# to our constructing the full Cacti release for GroundWork Monitor from their
# earlier release plus our patches.
CACTI_RELEASE=0.8.7g

# This is where we will park the files we extract from Subversion.
# WARNING:  Choose the $CACTI_BUILD_TREE path carefully, because we will
# completely wipe it out before using it!  Be sure to use an absolute
# pathname for the $CACTI_BUILD_TREE definition.
CACTI_BUILD_TREE=/tmp/cacti-build

# Set this to match the EntBuild.sh script setting, so the files we prepare here
# will be picked up and put into the product distribution we are constructing.
# WARNING:  Choose the $CACTI_BUILD_BASE path carefully, because we will
# completely wipe it out before using it!  Be sure to use an absolute
# pathname for the $CACTI_BUILD_BASE definition.  Normally, we just make
# this a subdirectory of the $CACTI_BUILD_TREE directory.
CACTI_BUILD_BASE=$CACTI_BUILD_TREE/cacti

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

echo === Starting the Cacti Build

/bin/rm -rf $CACTI_BUILD_TREE
mkdir -p    $CACTI_BUILD_TREE
cd          $CACTI_BUILD_TREE

echo === Stage the Raw Distribution, Patch Files, and New Files

         CACTI_SVN_BASE=http://archive.groundworkopensource.com/groundwork-opensource/trunk/monitor-core/cacti/postgresql/cacti
  CACTI_PORTAL_SVN_BASE=http://geneva/groundwork-professional/trunk/monitor-portal/applications/nms/installer/cacti
 CACTI_SCRIPTS_SVN_BASE=http://geneva/groundwork-professional/trunk/monitor-portal/applications/nms/conf/cacti_updates
CACTI_DATABASE_SVN_BASE=http://geneva/groundwork-professional/trunk/monitor-portal/applications/nms/conf/database
 CACTI_PREDICT_SVN_BASE=http://archive.groundworkopensource.com/groundwork-opensource/trunk/monitor-core/cacti/postgresql/predict

svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/add_graphs.php.graph_title_porting.patch
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/adodb5.14-patch-for-postgres9.tar.gz
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/adodb514.zip
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/auth.php.initial_diff
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/cacti-0.8.7g-patch-for-postgres9.tar.gz
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/cacti-0.8.7g.tar.gz
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/cacti-db.sql
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/cacti-plugin-0.8.7g-PA-v2.8.tar.gz
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/cacti-seed.sql
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/config.php.initial_diff
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/data_source_deactivate.patch
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/data_sources.php.filter_and_display.patch
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/data_sources.php.filter_porting.patch
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/data_sources.php.initial_diff
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/database.php.debug_backtrace_display.patch
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/discovery-0.8.5-findhosts.php.initial_diff
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/discovery-0.8.5-patch-for-postgres9.tar.gz
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/discovery-0.8.5.tar.gz
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/export.php.error_handling.patch
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/global_arrays.php.error_messages.patch
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/global_arrays.php.initial_diff
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/global_settings.php.initial_diff
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/graph_list_view.patch
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/host.php-r7420.patch
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/html_output.patch
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/import.php.graph_and_data_template_save.patch
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/install-index.php-r7420.patch
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/ldap_group_authenication.patch
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/lib-api_device.php-r7420.patch
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/lib-api_poller.php-r7394.patch
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/lib-rrd.php-r7393.patch
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/lib-snmp.php-r7392.patch
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/lib-utility.php-r7394.patch
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/lib_database.php.post_patch_fixes
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/ping.patch
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/poller_interval.patch.1-line-context
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/script_server_command_line_parse.patch
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/settings-0.5.tar.gz
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/thold-0.4.2-includes-settings.php.initial_diff
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/thold-0.4.2-listthold.php.initial_diff
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/thold-0.4.2-patch-for-postgres9.tar.gz
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/thold-0.4.2-setup.php.initial_diff
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/thold-0.4.2-thold_functions.php.initial_diff
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/thold-0.4.2-thold_graph.php.initial_diff
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/thold-0.4.2.tar.gz
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/top_graph_header.php.initial_diff
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/top_header.php.initial_diff
svn_export $SVN_CREDENTIALS $CACTI_SVN_BASE/utility.php.graph_template_save.patch

svn_export $SVN_CREDENTIALS $CACTI_PORTAL_SVN_BASE/META-INF/context.xml
svn_export $SVN_CREDENTIALS $CACTI_PORTAL_SVN_BASE/WEB-INF/jboss-web.xml
svn_export $SVN_CREDENTIALS $CACTI_PORTAL_SVN_BASE/WEB-INF/web.xml
svn_export $SVN_CREDENTIALS $CACTI_PORTAL_SVN_BASE/WEB-INF/jboss-deployment-structure.xml

svn_export $SVN_CREDENTIALS $CACTI_SCRIPTS_SVN_BASE/crontab_cacti.pl
svn_export $SVN_CREDENTIALS $CACTI_SCRIPTS_SVN_BASE/crontab_find_cacti.pl
svn_export $SVN_CREDENTIALS $CACTI_SCRIPTS_SVN_BASE/extract_cacti.pl

svn_export $SVN_CREDENTIALS $CACTI_DATABASE_SVN_BASE/cacti.import_schema.sql
svn_export $SVN_CREDENTIALS $CACTI_DATABASE_SVN_BASE/cacti.paths.sql
svn_export $SVN_CREDENTIALS $CACTI_DATABASE_SVN_BASE/cacti.pluginarch.sql
svn_export $SVN_CREDENTIALS $CACTI_DATABASE_SVN_BASE/cacti.poller.sql
svn_export $SVN_CREDENTIALS $CACTI_DATABASE_SVN_BASE/cacti.userauth.sql
svn_export $SVN_CREDENTIALS $CACTI_DATABASE_SVN_BASE/cacti_install.sql

svn_export $SVN_CREDENTIALS $CACTI_PREDICT_SVN_BASE/userplugin:predict_1.0.0.zip

chmod 644 context.xml
chmod 644 jboss-web.xml
chmod 644 web.xml
chmod 644 jboss-deployment-structure.xml

chmod 755 crontab_cacti.pl
chmod 755 crontab_find_cacti.pl
chmod 755 extract_cacti.pl

chmod 644 cacti.import_schema.sql
chmod 644 cacti.paths.sql
chmod 644 cacti.pluginarch.sql
chmod 644 cacti.poller.sql
chmod 644 cacti.userauth.sql
chmod 644 cacti_install.sql

echo === Unpack certain tarballs to reveal their content, for later use.
cd $CACTI_BUILD_TREE
tar xzf $CACTI_BUILD_TREE/cacti-plugin-0.8.7g-PA-v2.8.tar.gz
tar xzf $CACTI_BUILD_TREE/adodb5.14-patch-for-postgres9.tar.gz
tar xzf $CACTI_BUILD_TREE/cacti-0.8.7g-patch-for-postgres9.tar.gz
tar xzf $CACTI_BUILD_TREE/discovery-0.8.5-patch-for-postgres9.tar.gz
tar xzf $CACTI_BUILD_TREE/thold-0.4.2-patch-for-postgres9.tar.gz

echo === Unpack the full Cacti 0.8.7g release.
/bin/rm -rf $CACTI_BUILD_BASE
mkdir -p    $CACTI_BUILD_BASE
cd          $CACTI_BUILD_BASE
tar xvzf $CACTI_BUILD_TREE/cacti-0.8.7g.tar.gz

echo === Apply the Plugin Architecture overlay.
cd $CACTI_BUILD_BASE/cacti-0.8.7g
# FIX MINOR:  don't use a backup here
patch -b -V simple -z .pre_pia -p1 -N < $CACTI_BUILD_TREE/cacti-plugin-arch/cacti-plugin-0.8.7g-PA-v2.8.diff

echo === Include a few plugins.
# GroundWork has also provided patches (see below) to port these versions to use PostgreSQL.
cd $CACTI_BUILD_BASE/cacti-0.8.7g/plugins
tar xzf $CACTI_BUILD_TREE/discovery-0.8.5.tar.gz
tar xzf $CACTI_BUILD_TREE/settings-0.5.tar.gz
tar xzf $CACTI_BUILD_TREE/thold-0.4.2.tar.gz
dos2unix -o thold/LICENSE thold/README thold/thold.sql

# The official release of the Cacti "predict" plugin includes the GroundWork porting changes to support PostgreSQL.
unzip $CACTI_BUILD_TREE/userplugin:predict_1.0.0.zip
# Fix the too-permissive permissions that come with the standard release of this plugin.
chmod 755 predict/tmp

echo === Apply baseline patches for the discovery and thold plugins.
# These fix a number of bugs in these plugin versions that GroundWork
# addressed before we started the PostgreSQL porting effort. 
cd $CACTI_BUILD_BASE/cacti-0.8.7g
# FIX MINOR:  don't use a backup here
patch -b -V simple -z .pre_initial -p2 < $CACTI_BUILD_TREE/discovery-0.8.5-findhosts.php.initial_diff
patch -b -V simple -z .pre_initial -p2 < $CACTI_BUILD_TREE/thold-0.4.2-includes-settings.php.initial_diff
patch -b -V simple -z .pre_initial -p2 < $CACTI_BUILD_TREE/thold-0.4.2-listthold.php.initial_diff
patch -b -V simple -z .pre_initial -p2 < $CACTI_BUILD_TREE/thold-0.4.2-setup.php.initial_diff
patch -b -V simple -z .pre_initial -p2 < $CACTI_BUILD_TREE/thold-0.4.2-thold_functions.php.initial_diff
patch -b -V simple -z .pre_initial -p2 < $CACTI_BUILD_TREE/thold-0.4.2-thold_graph.php.initial_diff

echo === Apply official Cacti patches for the 0.8.7g release.
# The following official patches are not part of the GroundWork
# Monitor 6.6.1 release, because we did not realize in time that
# they were available to be applied against the Cacti 0.8.7g
# release.  If we had understood earlier that these patches 
# were available, we would have executed the following commands
# to install all of them, at this point in the patch sequence.
# Executing these commands is strongly recommended for the Cacti
# team, because we want to ensure that all the known patches are
# applied and we don't have any regressions due to simple oversight.
# There are some adjustments to be made to the standard means of
# installing these patches:
# * The "-F 3" option is needed on one of these patches to counter 
#   the effect of having previously applied the standard Plugin
#   Architecture cacti-plugin-0.8.7g-PA-v2.8.diff patch, above.
# * The poller_interval.patch.1-line-context patch file is different
#   from the poller_interval.patch patch file which is downloadable
#   from the Cacti web site not in its ultimate effect, but in that
#   it is reworked to also get around the effect of having previously
#   applied the cacti-plugin-0.8.7g-PA-v2.8.diff patch, above.
cd $CACTI_BUILD_BASE/cacti-0.8.7g
patch -b -V simple -z .pre_official -p1 -N      < $CACTI_BUILD_TREE/data_source_deactivate.patch
patch -b -V simple -z .pre_official -p1 -N      < $CACTI_BUILD_TREE/graph_list_view.patch
patch -b -V simple -z .pre_official -p1 -N -F 3 < $CACTI_BUILD_TREE/html_output.patch
patch -b -V simple -z .pre_official -p1 -N      < $CACTI_BUILD_TREE/ldap_group_authenication.patch
patch -b -V simple -z .pre_official -p1 -N      < $CACTI_BUILD_TREE/script_server_command_line_parse.patch
patch -b -V simple -z .pre_official -p1 -N      < $CACTI_BUILD_TREE/ping.patch
patch -b -V simple -z .pre_official -p1 -N      < $CACTI_BUILD_TREE/poller_interval.patch.1-line-context

echo === Apply patches to make Cacti fit into the GroundWork context.
# GroundWork wants to apply all of these for its own distribution of Cacti, but
# the Cacti team should apply only the global_settings.php.initial_diff patch.
# NOTE:  Even for GroundWork, the data_sources.php.initial_diff patch should be
# completely ignored if the html_output.patch patch was applied above, as the
# latter is more thorough and should take precedence.
cd $CACTI_BUILD_BASE/cacti-0.8.7g
# FIX MINOR:  don't use a backup here
    patch -b -V simple -z .pre_gw -p2 < $CACTI_BUILD_TREE/auth.php.initial_diff
    patch -b -V simple -z .pre_gw -p2 < $CACTI_BUILD_TREE/config.php.initial_diff
##  patch -b -V simple -z .pre_gw -p2 < $CACTI_BUILD_TREE/data_sources.php.initial_diff
    patch -b -V simple -z .pre_gw -p2 < $CACTI_BUILD_TREE/global_arrays.php.initial_diff
    patch -b -V simple -z .pre_gw -p2 < $CACTI_BUILD_TREE/global_settings.php.initial_diff
    patch -b -V simple -z .pre_gw -p2 < $CACTI_BUILD_TREE/top_graph_header.php.initial_diff
    patch -b -V simple -z .pre_gw -p2 < $CACTI_BUILD_TREE/top_header.php.initial_diff

echo === Apply patches to port Cacti to support PostgreSQL.
# Now we address the purpose of this whole effort.  Applying the
# cacti-0.8.7g-patch-for-postgres9 patches here provides the basic
# porting of PHP code to allow access to PostgreSQL.  Note that if
# you have applied the official 0.8.7g patches above, a few of the
# patches within this file will be rejected:
# * One hunk applied to data_sources.php will fail if you applied
#   the html_output.patch patch via the commands above.  You can
#   safely ignore this, as the html_output.patch patch above already
#   addressed this issue.
# * Four hunks applied to graph_view.php will fail if you applied
#   the graph_list_view.patch patch via the commands above.  You can
#   safely ignore this, as the graph_list_view.patch patch above
#   already addressed these issues in what is probably a better way.
# * Four hunks applied to lib/database.php will fail if you
#   applied the poller_interval.patch.1-line-context patch via the
#   commands above.  The fixes in these hunks are critical to the
#   PostgreSQL porting, so in this case you MUST apply the separate
#   lib_database.php.post_patch_fixes patch file, as noted below.
# Because of these rejects, we must append ||true to the running
# of this patch, so the enclosing script ignores the fact that it
# returns a failure exit code.
cd $CACTI_BUILD_BASE/cacti-0.8.7g
# FIX MINOR:  don't use a backup here
patch -b -V simple -z .pre_pg -p0 < $CACTI_BUILD_TREE/cacti-0.8.7g-patch-for-postgres9 || true
patch -b -V simple -z .pre_pg -p0 < $CACTI_BUILD_TREE/discovery-0.8.5-patch-for-postgres9
patch -b -V simple -z .pre_pg -p0 < $CACTI_BUILD_TREE/thold-0.4.2-patch-for-postgres9

echo === Apply a patch to fix a failed previous patch.
# NOTE:  Apply this next patch if and only if you previously applied
# the poller_interval.patch.1-line-context patch for 0.8.7g as noted
# above.  It mirrors a few critical cacti-0.8.7g-patch-for-postgres9
# fixes for PostgreSQL support that were rejected if the
# poller_interval.patch.1-line-context patch was applied before the
# cacti-0.8.7g-patch-for-postgres9 patch was applied, because those
# particular patches no longer matched the resulting source code.
# This extra patch file is designed to compensate for that.
cd $CACTI_BUILD_BASE/cacti-0.8.7g
patch -b -V simple -z .mid_pg -p0 < $CACTI_BUILD_TREE/lib_database.php.post_patch_fixes

echo === Apply patches to fix things we overlooked in the large cacti-0.8.7g-patch-for-postgres9 patch file.
cd $CACTI_BUILD_BASE/cacti-0.8.7g
# FIX MINOR:  don't use backups here
patch -b -V simple -z .pre_graph_title_porting_fix -p2 < $CACTI_BUILD_TREE/add_graphs.php.graph_title_porting.patch
patch -b -V simple -z .pre_filter_porting_fix      -p2 < $CACTI_BUILD_TREE/data_sources.php.filter_porting.patch
patch -b -V simple -z .pre_backtrace_display_fix   -p2 < $CACTI_BUILD_TREE/database.php.debug_backtrace_display.patch
patch -b -V simple -z .pre_template_save_fix       -p2 < $CACTI_BUILD_TREE/import.php.graph_and_data_template_save.patch
patch -b -V simple -z .pre_template_save_fix       -p2 < $CACTI_BUILD_TREE/utility.php.graph_template_save.patch

echo === Apply patches to apply fixes we backported from later Cacti releases or have reported to the Cacti team.
cd $CACTI_BUILD_BASE/cacti-0.8.7g
# FIX MINOR:  don't use backups here
patch -b -V simple -z .pre_filter_display_fix -p2 < $CACTI_BUILD_TREE/data_sources.php.filter_and_display.patch
patch -b -V simple -z .pre_error_handling_fix -p2 < $CACTI_BUILD_TREE/export.php.error_handling.patch
patch -b -V simple -z .pre_error_message_fix  -p2 < $CACTI_BUILD_TREE/global_arrays.php.error_messages.patch

echo === Unpack the base ADODB release.
cd $CACTI_BUILD_BASE/cacti-0.8.7g/lib
unzip $CACTI_BUILD_TREE/adodb514.zip

echo === Overlay the GroundWork changes to ADODB to improve its PostgreSQL support sufficiently to support Cacti.
cd $CACTI_BUILD_BASE/cacti-0.8.7g/lib/adodb5
# FIX MINOR:  don't use a backup here
patch -b -V simple -z .old -p1 < $CACTI_BUILD_TREE/adodb5.14-patch-for-postgres9

echo === Apply security patches backported from upstream changes.
# These patches must be applied at the end, after GroundWork's PostgreSQL-porting fixes.
cd $CACTI_BUILD_BASE/cacti-0.8.7g
patch -b -V simple -z .pre_sec -p2 < $CACTI_BUILD_TREE/host.php-r7420.patch
patch -b -V simple -z .pre_sec -p2 < $CACTI_BUILD_TREE/install-index.php-r7420.patch
patch -b -V simple -z .pre_sec -p2 < $CACTI_BUILD_TREE/lib-api_device.php-r7420.patch
patch -b -V simple -z .pre_sec -p2 < $CACTI_BUILD_TREE/lib-api_poller.php-r7394.patch
patch -b -V simple -z .pre_sec -p2 < $CACTI_BUILD_TREE/lib-rrd.php-r7393.patch
patch -b -V simple -z .pre_sec -p2 < $CACTI_BUILD_TREE/lib-snmp.php-r7392.patch
patch -b -V simple -z .pre_sec -p2 < $CACTI_BUILD_TREE/lib-utility.php-r7394.patch

# ========================================================================
# That's the end of the stuff relating to our porting to PostgreSQL.
# The remainder of this build relates strictly to incorporating Cacti
# into the GroundWork Monitor product.
# ========================================================================

echo === Transform the patched Cacti distribution into a form more suitable for building GroundWork Monitor.
mv $CACTI_BUILD_BASE/cacti-$CACTI_RELEASE $CACTI_BUILD_BASE/htdocs
chmod 755                                 $CACTI_BUILD_BASE
chmod 755                                 $CACTI_BUILD_BASE/htdocs

touch     $CACTI_BUILD_BASE/htdocs/rra/NOTEMPTY
chmod 644 $CACTI_BUILD_BASE/htdocs/rra/NOTEMPTY

mkdir $CACTI_BUILD_BASE/htdocs/META-INF
mkdir $CACTI_BUILD_BASE/htdocs/WEB-INF
mkdir $CACTI_BUILD_BASE/scripts

chmod 755 $CACTI_BUILD_BASE/htdocs/META-INF
chmod 755 $CACTI_BUILD_BASE/htdocs/WEB-INF
chmod 755 $CACTI_BUILD_BASE/scripts

# FIX LATER:  It's not clear we really need these splash directories any more.
# Revisit this section once we have all the NMS components integrated into the
# GroundWork Monitor base product.
mkdir $CACTI_BUILD_BASE/htdocs/splash
mkdir $CACTI_BUILD_BASE/htdocs/splash/nedi
mkdir $CACTI_BUILD_BASE/htdocs/splash/ntop
mkdir $CACTI_BUILD_BASE/htdocs/splash/weathermap

chmod 755 $CACTI_BUILD_BASE/htdocs/splash
chmod 755 $CACTI_BUILD_BASE/htdocs/splash/nedi
chmod 755 $CACTI_BUILD_BASE/htdocs/splash/ntop
chmod 755 $CACTI_BUILD_BASE/htdocs/splash/weathermap

echo === Add additional files needed to integrate with GroundWork Monitor.
cp -p $CACTI_BUILD_TREE/jboss-deployment-structure.xml  $CACTI_BUILD_BASE/htdocs/WEB-INF/
cp -p $CACTI_BUILD_TREE/context.xml        $CACTI_BUILD_BASE/htdocs/META-INF/
cp -p $CACTI_BUILD_TREE/jboss-web.xml      $CACTI_BUILD_BASE/htdocs/WEB-INF/
cp -p $CACTI_BUILD_TREE/web.xml            $CACTI_BUILD_BASE/htdocs/WEB-INF/

# FIX MINOR:  find where these scripts are referenced; perhaps drop them now,
# or port them to PostgreSQL as needed
cp -p $CACTI_BUILD_TREE/cacti.import_schema.sql $CACTI_BUILD_BASE/scripts/
cp -p $CACTI_BUILD_TREE/cacti.paths.sql         $CACTI_BUILD_BASE/scripts/
cp -p $CACTI_BUILD_TREE/cacti.pluginarch.sql    $CACTI_BUILD_BASE/scripts/
cp -p $CACTI_BUILD_TREE/cacti.poller.sql        $CACTI_BUILD_BASE/scripts/
cp -p $CACTI_BUILD_TREE/cacti.userauth.sql      $CACTI_BUILD_BASE/scripts/
cp -p $CACTI_BUILD_TREE/cacti_install.sql       $CACTI_BUILD_BASE/scripts/
cp -p $CACTI_BUILD_TREE/crontab_cacti.pl        $CACTI_BUILD_BASE/scripts/
cp -p $CACTI_BUILD_TREE/crontab_find_cacti.pl   $CACTI_BUILD_BASE/scripts/

cp -p $CACTI_BUILD_TREE/extract_cacti.pl   $CACTI_BUILD_BASE/

# FIX LATER:  In a later release, we should replace the cacti.sql file with
# a PostgreSQL version.  Presumably, that will come from the Cacti team.
mv $CACTI_BUILD_BASE/htdocs/cacti.sql $CACTI_BUILD_BASE/scripts/

# FIX MAJOR:  Still need:
# (*) figure out why various .png files are different now (they look
#     corrupted in our 6.6.1 build; why didn't we detect this before?)

# diff /usr/local/groundwork/cacti/htdocs/docs/html/images/data_source_title_example1.png       cacti/htdocs/docs/html/images/data_source_title_example1.png
# diff /usr/local/groundwork/cacti/htdocs/docs/html/images/data_source_title_example2.png       cacti/htdocs/docs/html/images/data_source_title_example2.png
# diff /usr/local/groundwork/cacti/htdocs/docs/html/images/data_source_title_template.png       cacti/htdocs/docs/html/images/data_source_title_template.png
# diff /usr/local/groundwork/cacti/htdocs/docs/html/images/data_template.png                    cacti/htdocs/docs/html/images/data_template.png
# diff /usr/local/groundwork/cacti/htdocs/docs/html/images/export_template.png                  cacti/htdocs/docs/html/images/export_template.png
# diff /usr/local/groundwork/cacti/htdocs/docs/html/images/graph_template.png                   cacti/htdocs/docs/html/images/graph_template.png
# diff /usr/local/groundwork/cacti/htdocs/docs/html/images/graph_tree.png                       cacti/htdocs/docs/html/images/graph_tree.png
# diff /usr/local/groundwork/cacti/htdocs/docs/html/images/host_template.png                    cacti/htdocs/docs/html/images/host_template.png
# diff /usr/local/groundwork/cacti/htdocs/docs/html/images/import_template.png                  cacti/htdocs/docs/html/images/import_template.png
# diff /usr/local/groundwork/cacti/htdocs/docs/html/images/new_device.png                       cacti/htdocs/docs/html/images/new_device.png
# diff /usr/local/groundwork/cacti/htdocs/docs/html/images/new_graphs.png                       cacti/htdocs/docs/html/images/new_graphs.png
# diff /usr/local/groundwork/cacti/htdocs/docs/html/images/principles_of_operation.png          cacti/htdocs/docs/html/images/principles_of_operation.png
# diff /usr/local/groundwork/cacti/htdocs/docs/html/images/user_management_batch_copy_1.png     cacti/htdocs/docs/html/images/user_management_batch_copy_1.png
# diff /usr/local/groundwork/cacti/htdocs/docs/html/images/user_management_batch_copy_2.png     cacti/htdocs/docs/html/images/user_management_batch_copy_2.png
# diff /usr/local/groundwork/cacti/htdocs/docs/html/images/user_management_copy_1.png           cacti/htdocs/docs/html/images/user_management_copy_1.png
# diff /usr/local/groundwork/cacti/htdocs/docs/html/images/user_management_copy_2.png           cacti/htdocs/docs/html/images/user_management_copy_2.png
# diff /usr/local/groundwork/cacti/htdocs/docs/html/images/user_management_delete_1.png         cacti/htdocs/docs/html/images/user_management_delete_1.png
# diff /usr/local/groundwork/cacti/htdocs/docs/html/images/user_management_delete_2.png         cacti/htdocs/docs/html/images/user_management_delete_2.png
# diff /usr/local/groundwork/cacti/htdocs/docs/html/images/user_management_edit.png             cacti/htdocs/docs/html/images/user_management_edit.png
# diff /usr/local/groundwork/cacti/htdocs/docs/html/images/user_management_enable_disable_1.png cacti/htdocs/docs/html/images/user_management_enable_disable_1.png
# diff /usr/local/groundwork/cacti/htdocs/docs/html/images/user_management_enable_disable_2.png cacti/htdocs/docs/html/images/user_management_enable_disable_2.png
# diff /usr/local/groundwork/cacti/htdocs/docs/html/images/user_management_list.png             cacti/htdocs/docs/html/images/user_management_list.png
# diff /usr/local/groundwork/cacti/htdocs/docs/html/images/user_management_new.png              cacti/htdocs/docs/html/images/user_management_new.png

# (*) apply a separate patch to fix Cacti breadcrumbs (?? -- need to replicate the problem first)
# (*) include the full weathermap plugin
#     /usr/local/groundwork/cacti/htdocs/plugins/weathermap/...
# (*) add various GroundWork-supplied files; these are currently handled by the
#     EntBuild.sh script, but once all the NMS components are integrated into
#     the base product, we may simply drop these files from the product
#     /usr/local/groundwork/cacti/htdocs/splash/nedi/index.php
#     /usr/local/groundwork/cacti/htdocs/splash/nedi/nedi.png
#     /usr/local/groundwork/cacti/htdocs/splash/ntop/index.php
#     /usr/local/groundwork/cacti/htdocs/splash/ntop/ntop_world.png
#     /usr/local/groundwork/cacti/htdocs/splash/weathermap/index.php
#     /usr/local/groundwork/cacti/htdocs/splash/weathermap/weathermap.png

# (*) What's this about?
# diff -r /usr/local/groundwork/cacti/htdocs/cli/upgrade_database.php cacti/htdocs/cli/upgrade_database.php
# 64,67d63
# <       '0.8.7d' => '0_8_7c_to_0_8_7d.php',
# <       '0.8.7e' => '0_8_7d_to_0_8_7e.php',
# <       '0.8.7f' => '0_8_7e_to_0_8_7f.php',
# <       '0.8.7g' => '0_8_7f_to_0_8_7g.php',

# To drop from the 6.6.1 tarball:
# /usr/local/groundwork/cacti/htdocs/lib/;  (yes, a literal semicolon as the filename)

# There's no need to carry out these steps during the ordinary build
# of the Cacti code for GroundWork.  These actions will be dealt with
# elsewhere in our deployment process.
if false; then
    echo === Create the Cacti database in PostgreSQL.
    psql -U postgres
    CREATE USER cactiuser WITH PASSWORD 'cactipass';
    CREATE DATABASE cacti ENCODING='LATIN1' OWNER=cactiuser;
    GRANT ALL PRIVILEGES ON DATABASE cacti to cactiuser;
    \q

    echo === Create all the Cacti database tables and associated objects.
    # This will include all the tables and extra fields needed for the Thold
    # and Weathermap plugins (at least, the version of Thold installed
    # above, and the version of Weathermap we are still in the process
    # of porting to PostgreSQL).  Then stuff initial seed data into the
    # database.  We find it generally easier to do this than to depend
    # on Cacti to populate the tables during self-initialize actions.
    psql -U cactiuser -d cacti < $CACTI_BUILD_TREE/cacti-db.sql
    psql -U cactiuser -d cacti < $CACTI_BUILD_TREE/cacti-seed.sql
fi

echo === Change Ownership in the Prepared File Tree

chown -R nagios:nagios $CACTI_BUILD_BASE

echo === Ending the Cacti Build

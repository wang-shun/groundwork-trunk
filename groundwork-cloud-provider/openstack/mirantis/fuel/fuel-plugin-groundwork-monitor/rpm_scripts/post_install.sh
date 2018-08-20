# This is the post.sh script for the fuel-plugin-groundwork-monitor RPM.

#
# Copyright 2016-2017 GroundWork Open Source, Inc. (GroundWork)
# All rights reserved. This program is free software; you can redistribute
# it and/or modify it under the terms of the GNU General Public License
# version 2 as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
#

# The RPM documentation is completely silent on what shell will be used
# to run this script, so we cannot assume any more than that it will be
# run as /bin/sh, which translates to the limited-functionality "dash"
# shell on Ubuntu.  That means we must be very careful not to use any
# bash-specific syntax here.

# Establish a known-safe command-search path, to ensure that we ignore any
# inappropriate directories or ordering inherited from the calling environment.
PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

# This is an installation script that may fail for unknown reasons.
# We need to signal that fact immediately if problems arise.
set -e

# The PACKAGE_DIRECTORY pathname is set here automatically by the plugin
# build process, from the PACKAGE_DIRECTORY value in the parent directory's
# Makefile.  The value is considered to be not hardcoded here, to simplify
# maintenance of this plugin for future releases.
PACKAGE_DIRECTORY='/var/www/nailgun/plugins/fuel-plugin-groundwork-monitor-7.1'

# The GENUINE_FILENAME filename is set here automatically by the plugin RPM
# build process, from the equivalent value in the parent directory's Makefile.
# The value is considered to be not hardcoded here, to simplify maintenance
# of this plugin for future releases.
GENUINE_FILENAME='groundworkenterprise-7.1.1-br415-gw3089-linux-64-installer.run'

# The GENUINE_PATHNAME pathname is set here automatically by the plugin RPM
# build process, from the equivalent value in the parent directory's Makefile.
# The value is considered to be not hardcoded here, to simplify maintenance
# of this plugin for future releases.
GENUINE_PATHNAME='deployment_scripts/groundworkenterprise-7.1.1-br415-gw3089-linux-64-installer.run'

# The place under $PACKAGE_DIRECTORY where the $GENUINE_FILENAME will live.
# So GENUINE_PATHNAME will be $DEPLOYMENT_SUBDIRECTORY/$GENUINE_FILENAME
# in any sensible build.  We need this path compoent separated out so we
# can tell exactly where the GroundWork installer should be copied during
# the post-install processing.
DEPLOYMENT_SUBDIRECTORY='deployment_scripts'

# This is a temporary pathname under which the GroundWork installer gets
# parked during the post-installation process, before we trust it fully.
TRIAL_PATHNAME="$PACKAGE_DIRECTORY/$DEPLOYMENT_SUBDIRECTORY/trial-installer"

# This is where we expect to find the GroundWork installer when the RPM is
# being installed, having been previously put there by the system administrator.
TMP_GENUINE_PATHNAME="/tmp/$GENUINE_FILENAME"

# This is where the installer lives on the Fuel Master machine.
ABS_GENUINE_PATHNAME="$PACKAGE_DIRECTORY/$GENUINE_PATHNAME"

# Checksums for use in validating the GENUINE_PATHNAME program.
# Set here during RPM build by the parent directory's Makefile.
#
GENUINE_MD5SUM='a1a68a525cbd37e94b7fbe9b4fa21092'
GENUINE_SHA1SUM='acd38e6adb18199691f8eb51938b08b85be07ac6'
GENUINE_SHA256SUM='dba3267cab47f22d95e13e90c4695dadc6c0c332f1d172c7b9bfdd786e822ec0'
#
VERIFY_CHECKSUM='sha256'

# This boolean flag determines what to do if we don't find the official
# GroundWork installer at plugin install time.  It is set here during RPM
# build by the parent directory's Makefile.
ABORT_IF_INSTALLER_MISSING='1'

verify_installer () {
	installer_path="$1"
	checksum_type="$2"
	md5_checksum="$3"
	sha1_checksum="$4"
	sha256_checksum="$5"
	summer=false
	target_checksum="invalid_checksum"
	case "$checksum_type" in
	    (none)
		echo "NOTICE:  The GroundWork installer is intentionally"
		echo "         not being checksum-verified."
		return 0
		;;
	    (md5)    summer=md5sum;    target_checksum="$md5_checksum";    ;;
	    (sha1)   summer=sha1sum;   target_checksum="$sha1_checksum";   ;;
	    (sha256) summer=sha256sum; target_checksum="$sha256_checksum"; ;;
	    (*)
		echo "ERROR:  The RPM %post script checksumming choice is misconfigured;"
		echo "        the GroundWork installer is not being checksum-verified."
		exit 1
		;;
	esac

	actual_checksum="`$summer -b "$installer_path" | sed -e 's/ .*//'`"
	if [ "$actual_checksum" != "$target_checksum" ]; then
		# Symmetry is broken.  Get out now while you still can.
		echo "NOTICE:  installer is $installer_path"
		echo "ERROR:  The GroundWork installer fails $checksum_type checksum verification."
		exit 1
	fi
}

copy_installer () {
	if [ -f "$TMP_GENUINE_PATHNAME" ]; then
		# Let's verify the GroundWork installer where it initially lives, before
		# copying it.  This provides very basic protection (but subject to race
		# conditions) against a Trojan Horse being let in the door.  We will
		# re-verify the final copy after we have finished copying it and before
		# we rename it, to further reduce the window of vulnerability.
		verify_installer "$TMP_GENUINE_PATHNAME" "$VERIFY_CHECKSUM" "$GENUINE_MD5SUM" "$GENUINE_SHA1SUM" "$GENUINE_SHA256SUM"

		# Make a temporary copy of the installer under another name, since we don't
		# necessarily yet trust that it hasn't been subject to a race condition.
		# Then immediately remove execute permissions, for the same reason, so
		# we don't leave behind some unknown executable that looks acceptable if
		# something fails beyond this point.
		cp -p "$TMP_GENUINE_PATHNAME" "$TRIAL_PATHNAME"
		chmod 444                     "$TRIAL_PATHNAME"

		# Re-verify the final copy, as it sits in place.  This can take up to
		# 30 seconds or so on a slow disk, but security is worth that small
		# price.  And it's reasonably likely that the kernel will still have
		# the entire file buffered in memory, so it might take a lot less time.
		verify_installer "$TRIAL_PATHNAME" "$VERIFY_CHECKSUM" "$GENUINE_MD5SUM" "$GENUINE_SHA1SUM" "$GENUINE_SHA256SUM"

		# Looks okay (that is, we didn't abort this script during the last checksum
		# verification), so let's accept the copy as a valid installer, under the
		# name that will be used during deployments.  Set the file permissions as
		# we will need them when the plugin gets deployed.
		mv "$TRIAL_PATHNAME" "$ABS_GENUINE_PATHNAME"
		chmod 755            "$ABS_GENUINE_PATHNAME"
	elif [ "$ABORT_IF_INSTALLER_MISSING" -ne 0 ]; then
		#
		# This is the usual (production) branch.
		#
		echo "ERROR:  Installation of the fuel-plugin-groundwork-monitor RPM failed because"
		echo "        the GroundWork installer is not present in the /tmp directory."
		exit 1
	else
		#
		# In this branch, used for development testing, we don't actually prevent
		# installation if you are missing the GroundWork installer.  Instead, the
		# plugin will operate in a degraded mode, executing an installer emulator
		# instead of the real thing.  That can be helpful for testing of the
		# plugin itself.
		#
		echo "WARNING:  Installation of the fuel-plugin-groundwork-monitor RPM will be"
		echo "          incomplete because the GroundWork installer is not present in"
		echo "          the /tmp directory."
	fi
}

copy_installer

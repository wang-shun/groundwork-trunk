# This is the pre.sh script for the fuel-plugin-groundwork-monitor RPM.

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

# We could have used this pre-install scripting to run an initial checksum
# on the GroundWork installer once we verify that it exists where we expect
# it to be.  But we would still need to perform such checksums anyway when
# we copy the file in the post-install scripting, so we'll just defer until
# then.  There is no great loss of functionality in this setup.

# The GENUINE_FILENAME filename is set here automatically by the plugin RPM
# build process, from the equivalent value in the parent directory's Makefile.
# The value is considered to be not hardcoded here, to simplify maintenance
# of this plugin for future releases.
GENUINE_FILENAME='groundworkenterprise-7.1.1-br415-gw3089-linux-64-installer.run'

# This is where we expect to find the GroundWork installer when the RPM is
# being installed, having been previously put there by the system administrator.
TMP_GENUINE_PATHNAME="/tmp/$GENUINE_FILENAME"

# This boolean flag determines what to do if we don't find the official
# GroundWork installer at plugin install time.  It is set here during RPM
# build by the parent directory's Makefile.
ABORT_IF_INSTALLER_MISSING='1'

check_for_installer () {
	if [ -f "$TMP_GENUINE_PATHNAME" ]; then
		# There's nothing to do here; we found the installer.
		:
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

check_for_installer

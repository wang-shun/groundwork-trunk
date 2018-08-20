# This is the preun.sh script for the fuel-plugin-groundwork-monitor RPM.

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

# This is an uninstallation script that may fail for unknown reasons.
# We need to signal that fact immediately if problems arise.
set -e

# The PACKAGE_DIRECTORY pathname is set here automatically by the plugin
# build process, from the PACKAGE_DIRECTORY value in the parent directory's
# Makefile.  The value is considered to be not hardcoded here, to simplify
# maintenance of this plugin for future releases.
PACKAGE_DIRECTORY='/var/www/nailgun/plugins/fuel-plugin-groundwork-monitor-7.1'

# The GENUINE_PATHNAME pathname is set here automatically by the plugin RPM
# build process, from the equivalent value in the parent directory's Makefile.
# The value is considered to be not hardcoded here, to simplify maintenance
# of this plugin for future releases.
GENUINE_PATHNAME='deployment_scripts/groundworkenterprise-7.1.1-br415-gw3089-linux-64-installer.run'

# This is where the installer lives on the Fuel Master machine.
ABS_GENUINE_PATHNAME="$PACKAGE_DIRECTORY/$GENUINE_PATHNAME"

delete_installer () {
	# There's not much to do here, except to make the installer disappear,
	# since it was not part of the RPM proper.  The "rm -f" option will
	# suppress bad exit codes if the file does not exist, so we probably
	# won't find out about any errors here.
	rm -f "$ABS_GENUINE_PATHNAME"
}

delete_installer

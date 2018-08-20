# This is the verifyscript.sh script for the fuel-plugin-groundwork-monitor RPM.

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

# This is a verification script.  We may as well fail if problems arise.
set -e

# The stdout output from %verifyscript as run by "rpm -V" appears only when it
# is also run in verbose mode (which also produces a lot of other output, from
# rpm itself, not this verification script).  So for simplicity of execution,
# not wanting to have to invoke the additional option that we're likely to forget
# about, we simply force this diagnostic output to the stderr stream.  Neither
# the stdout output nor the stderr output from %verifyscript from "dpkg -V" is
# never produced, because dpkg has no support at all for this script.
exec 1>&2

# The PACKAGE_DIRECTORY pathname is set here automatically by the plugin
# build process, from the PACKAGE_DIRECTORY value in the parent directory's
# Makefile.  The value is considered to be not hardcoded here, to simplify
# maintenance of this plugin for future releases.
PACKAGE_DIRECTORY='/var/www/nailgun/plugins/fuel-plugin-groundwork-monitor-7.1'

# The GENUINE_PATHNAME and STANDIN_PATHNAME pathnames are set here automatically
# by the plugin RPM build process, from the equivalent values in the parent
# directory's Makefile.  The values are considered to be not hardcoded here,
# to simplify maintenance of this plugin for future releases.
GENUINE_PATHNAME='deployment_scripts/groundworkenterprise-7.1.1-br415-gw3089-linux-64-installer.run'
STANDIN_PATHNAME='deployment_scripts/groundwork-installer-emulator'

# This is where the installer lives on the Fuel Master machine.
ABS_GENUINE_PATHNAME="$PACKAGE_DIRECTORY/$GENUINE_PATHNAME"
ABS_STANDIN_PATHNAME="$PACKAGE_DIRECTORY/$STANDIN_PATHNAME"

# Checksums for use in validating the GENUINE_PATHNAME or STANDIN_PATHNAME program.
# Set here during RPM build by the parent directory's Makefile.
#
GENUINE_MD5SUM='a1a68a525cbd37e94b7fbe9b4fa21092'
GENUINE_SHA1SUM='acd38e6adb18199691f8eb51938b08b85be07ac6'
GENUINE_SHA256SUM='dba3267cab47f22d95e13e90c4695dadc6c0c332f1d172c7b9bfdd786e822ec0'
#
STANDIN_MD5SUM='676b679de3026de4b74cd0ff9e579ba8'
STANDIN_SHA1SUM='202165b2fdbdd7e4c6bd0ac88d9151871222c0d9'
STANDIN_SHA256SUM='8e9e1652c8b80fc3d92ac7cbf8589ff6dac4e39509e902fae56b6209cfb36642'
#
VERIFY_CHECKSUM='sha256'

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
		echo "ERROR:  The RPM %verifyscript script checksumming choice is misconfigured;"
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

if [ -f "$ABS_GENUINE_PATHNAME" ]; then
    verify_installer "$ABS_GENUINE_PATHNAME" "$VERIFY_CHECKSUM" "$GENUINE_MD5SUM" "$GENUINE_SHA1SUM" "$GENUINE_SHA256SUM" 
else
    echo "WARNING:  The GroundWork Monitor BitRock installer is not present."
fi
verify_installer "$ABS_STANDIN_PATHNAME" "$VERIFY_CHECKSUM" "$STANDIN_MD5SUM" "$STANDIN_SHA1SUM" "$STANDIN_SHA256SUM" 

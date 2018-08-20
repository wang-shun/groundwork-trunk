#!/bin/bash -e

# In case this script is invoked by means other than via the shebang
# line above, we force the strict-error-checking option on again.
set -e

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

# This deployment script gets deployed on the Fuel client machine to:
#     /etc/fuel/plugins/fuel-plugin-groundwork-monitor-7.1/deploy.sh
# (or equivalent, for later minor releases) as does any other deployment
# script for this plugin.

# For easier development testing (only), we can run "./deploy.ssh tty"
# so we don't need to keep looking at the log file after each test run.
log_to_tty=0
if [ "$1" = "tty" ]; then
	log_to_tty=1
fi

# FIX MINOR:  This is my own private logfile for this plugin's installer.
# I don't know whether this is an acceptable convention.
LOG_ROOT="/var/log/fuel-plugin-groundwork-monitor"
LOG_FILE=$LOG_ROOT/deployment.log

# The INSTALLER_DIRECTORY pathname is set here automatically by the plugin
# build process, derived from the GENUINE_FILENAME value in the parent
# directory's Makefile.  The value is considered to be not hardcoded
# here, to simplify maintenance of this plugin for future releases.
#
# This is actually supposed to have been set as the current working directory
# when this script is run by Fuel as it deploys the plugin on a target node.
# But we don't depend on that anywhere in this script, preferring to lock
# things down completely.
INSTALLER_DIRECTORY='/etc/fuel/plugins/fuel-plugin-groundwork-monitor-7.1'

# The GENUINE_FILENAME and STANDIN_FILENAME filenames are set here automatically
# by the plugin RPM build process, from the equivalent values in the parent
# directory's Makefile.  The values are considered to be not hardcoded here,
# to simplify maintenance of this plugin for future releases.
GENUINE_FILENAME='groundworkenterprise-7.1.1-br415-gw3089-linux-64-installer.run'
STANDIN_FILENAME='groundwork-installer-emulator'

# Checksums for use in validating the GENUINE_FILENAME or STANDIN_FILENAME program.
# Set here during RPM build by the parent directory's Makefile.
#
GENUINE_MD5SUM='a1a68a525cbd37e94b7fbe9b4fa21092'
GENUINE_SHA1SUM='acd38e6adb18199691f8eb51938b08b85be07ac6'
GENUINE_SHA256SUM='dba3267cab47f22d95e13e90c4695dadc6c0c332f1d172c7b9bfdd786e822ec0'
#
STANDIN_MD5SUM='676b679de3026de4b74cd0ff9e579ba8'
STANDIN_SHA1SUM='202165b2fdbdd7e4c6bd0ac88d9151871222c0d9'
STANDIN_SHA256SUM='8e9e1652c8b80fc3d92ac7cbf8589ff6dac4e39509e902fae56b6209cfb36642'

# Which installer checksum to verify, if any.
# Allowed values are "none", "md5", "sha1", and "sha256".
# Set here during RPM build by the parent directory's Makefile.
VERIFY_CHECKSUM='sha256'

# This is one of the very last files touched by the BitRock installer
# during a successful install.  So we use it as a sentinel.
INFO_FILE=/usr/local/groundwork/Info.txt

# We select the actual installer at run time, depending on what is then
# available.  But this will do as a simple default.
installer=/bin/false

# Default is to flag a failed install (a non-zero value).  We must later
# affirmatively set this to a zero value to flag a successful install.
installer_status=1

# Capture some invisible characters for later use in pattern matching.
newline='
'
tab='	'

# Working global variables.
installer=/bin/false
installer_md5sum="invalid"
installer_sha1sum="invalid"
installer_sha256sum="invalid"

begin_deployment () {
	# We don't actually expect anything much to show up in the $LOG_FILE,
	# since all the most interesting information will be in the separate
	# log file created by the BitRock installer.  But the $LOG_FILE might
	# contain some information from a failure of this deployment script.
	#
	# We capture both stdout and stderr, just in case ...
	if [ $log_to_tty -eq 0 ]; then
		mkdir -p $LOG_ROOT
		exec &>>$LOG_FILE
		if [ -s $LOG_FILE ]; then echo ""; fi
	fi
	echo "=== Running $0 to deploy the fuel-plugin-groundwork-monitor plugin ==="
}

end_deployment () {
	:
}

find_installer () {
	original="$INSTALLER_DIRECTORY/$GENUINE_FILENAME"
	emulator="$INSTALLER_DIRECTORY/$STANDIN_FILENAME"

	# FIX LATER:  For development debugging only:
	# emulator="./$STANDIN_FILENAME"

	if   [ -f "$original" -a -s "$original" -a -x "$original" -a ! -h "$original" ]; then
		installer=$original
		installer_md5sum="$GENUINE_MD5SUM"
		installer_sha1sum="$GENUINE_SHA1SUM"
		installer_sha256sum="$GENUINE_SHA256SUM"
	elif [ -f "$emulator" -a -s "$emulator" -a -x "$emulator" -a ! -h "$emulator" ]; then
		installer=$emulator
		installer_md5sum="$STANDIN_MD5SUM"
		installer_sha1sum="$STANDIN_SHA1SUM"
		installer_sha256sum="$STANDIN_SHA256SUM"
	else
		echo "ERROR:  The fuel-plugin-groundwork-monitor plugin"
		echo "        cannot find the GroundWork installer."
		exit 1
	fi
}

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
		echo "ERROR:  The deploy.sh script checksumming choice is misconfigured;"
		echo "        the GroundWork installer is not being checksum-verified."
		exit 1
		;;
	esac

	actual_checksum="`$summer -b "$installer_path" | sed -e 's/ .*//'`"
	if [ "$actual_checksum" != "$target_checksum" ]; then
		# Symmetry is broken.  Get out now while you still can.
		echo "NOTICE:  installer is $installer_path"
		echo "ERROR:  The GroundWork installer fails $checksum_type checksum verification."
		echo "        target checksum:  $target_checksum"
		echo "        actual checksum:  $actual_checksum"
		exit 1
	fi
}

install_groundwork () {
	local admin_password

	# We set $admin_password only locally, and not from any Fuel Plugin
	# setup, to prevent its being stored in cleartext anywhere on the
	# Fuel Master or the deployed node.  That would make it insecure.
	# (Using a Fuel Plugin configuration variable for this purpose
	# would not sidestep this problem.)  But it is visible in cleartext
	# here in this script, and it is only a fixed value, so it MUST be
	# manually changed on the GroundWork Monitor machine after the Fuel
	# plugin is deployed to that machine, even if you change the value
	# used here.  See the documentation for this plugin for a link to
	# the GroundWork documentation that describes how to modify the
	# appropriate administrative credentials.
	admin_password='convert-splash-arcane'

	echo "NOTICE:  Installation of GroundWork Monitor begins at: " `date`

	# NOTE:  ALL uses of $admin_password in this script MUST be double-quoted,
	# to avoid potential string-injection security problems.

	# We validate the password to make sure it contains no security-related
	# problem characters, before testing or using it.  Here are some simple
	# validation rules.  It should match this Perl regular expression pattern:
	#
	#     /^[^ \n\t\\!`"'$]{6,}$/
	#
	# and an appropriate error message if validation fails might be:
	#
	#     ERROR:  The administrative password must be at least 6 characters, and
	#             it cannot contain any of these characters:  space \ ! ` " '' $
	#
	# Bash seems to support the limited regular expression pattern matching
	# that we need, so we use that.
	#
	if [ -z "$admin_password" ]; then
		echo "ERROR:  Installation is impossible, because the PostgreSQL administrator password is empty."
		exit 1
	fi
	valid_pw='^[^ '"$newline$tab"'\\!`"'"'"'$]{6,}$'
	if [[ ! "$admin_password" =~ $valid_pw ]]; then
		echo 'ERROR:  The administrative password must be at least 6 characters, and'
		echo '        it cannot contain any of these characters:  space \ ! ` " '' $'
		exit 1
	fi

	installer_status=0
	# echo "DEBUG:  installer is $installer"
	"$installer" --mode unattended --postgres_password "$admin_password" || installer_status=$?

	if [ $installer_status -ne 0 ]; then
		echo "ERROR:  Installation has failed, as recognized by the GroundWork installer."

	# Under some circumstances, the installer might fail to set the exit code to
	# indicate a problem when it aborted early without actually installing the product.
	# Here we execute a simple, non-comprehensive check to see if the install failed.
	elif [ ! -f $INFO_FILE ]; then
		echo "ERROR:  Installation has failed; there is no $INFO_FILE"
		echo "        file after the installation attempt."
		installer_status=1

	# This is just an extra, non-comprehensive integrity check.
	elif [ ! -s $INFO_FILE ]; then
		echo "ERROR:  Installation has failed; the $INFO_FILE file"
		echo "        is empty after the installation attempt."
		installer_status=1

	# FIX MINOR:  The /usr/local/groundwork/uninstall program seems to be the actual
	# last file modified by the BitRock installer during a successful install.  We
	# could perhaps test for the existence of that file with another "elif" clause,
	# as well, before concluding that everything has gone as planned.

	else
		# No need for secrecy here; let's provide positive confirmation in the logfile.
		echo "INFO:  Installation of GroundWork Monitor on \"`hostname`\" has succeeded."
	fi

	echo "NOTICE:  Installation of GroundWork Monitor ends at: " `date`
}

have_prior_deployment () {
	# The "return" value from this function is detectable in the caller as $?,
	# the exit status of the function, and can be tested in a condition just as
	# if the caller had called an external program instead of a function.
	if [ -f $INFO_FILE ]; then
		# 0 => success, just like a program exit status (we do have a prior deployment)
		return 0
	fi
	# 1 => failure, just like a program exit status (we don't have a prior deployment)
	return 1
}

begin_deployment
if have_prior_deployment; then
	echo "NOTICE:  Installation of GroundWork Monitor on \"`hostname`\""
	echo "         is being bypassed; it is already present on this node."
	installer_status=0
else
	find_installer
	verify_installer "$installer" "$VERIFY_CHECKSUM" "$installer_md5sum" "$installer_sha1sum" "$installer_sha256sum" 
	install_groundwork
fi
end_deployment

exit $installer_status


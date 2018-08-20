# Copyright (c) 2011-2012 GroundWork Open Source, Inc.
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; version 2
# of the License.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
#
# Author - Tevfik Karagulle, post at itefix dot no
#
# Revision
#
# 2011-07-06 - initial version, tk
# 2011-07-21 - query bugfix, code cleanup, tk
# 2012-07-09 - fix perf data to always follow plugin guidelines;
#              make trailing backslash on path argument optional

# USE OF THIS PROGRAM IS STRONGLY DISCOURAGED.  Its logic and
# implementation have a number of problems, but this script will not
# be further developed.  Use of the diskfree.ps1 plugin is recommended
# instead of this mountpoint.ps1 plugin.

# Arguments with defaults
param
(
    [int]$warning   = 15,
    [int]$critical  = 5,
    [string]$path   = "",
    [switch]$verbose,
    [switch]$help
)

# Constants
$exitCodes =
@{
    "UNKNOWN"    = 3;
    "CRITICAL"   = 2;
    "WARNING"    = 1;
    "OK"         = 0
}

function setState($value, $warning, $critical)
{
	# Set plugin state and exit code
	$state="OK"

	if ($warning -le $critical) {
		if ($value -gt $warning) {
			$state="WARNING"
		}
		if ($value -gt $critical) {
			$state="CRITICAL"
		}
	} else {
		if ($value -lt $warning) {
			$state="WARNING"
		}
		if ($value -lt $critical) {
			$state="CRITICAL"
		}
	}

	if ($verbose) { write-host "State: $state" }
	$state
}

# If we are a 32-bit process running on a 64-bit capable system then reinvoke under 64-bit
function Fork64IfWoW ()
{
	if ($ENV:Processor_Architecture -eq 'x86' -and (test-path env:\PROCESSOR_ARCHITEW6432))
	{
		# echo 'WOW layer 64 bit OS/32 bit process'
		&"$env:windir\sysnative\windowspowershell\v1.0\powershell.exe" -noninteractive -noprofile $myinvocation.Line
		exit $LASTEXITCODE
	}
}

function printHelp
{
	write-host @"

Usage:

	mountpoint.ps1 -warning <warning value> -critical <critical value> -path <path> -verbose -help

	This plugin will check the used space on a volume mounted under a directoy, as opposed to a drive letter.

	If the Warning value is less than or equal to the Critical value:
	Warning value: if the returned numeric value is above this, the state will be WARNING (default $warning).
	Critical Value: if the returned value is above this, the state will be CRITICAL (default $critical)

	If the Critical Value is less than the Warning Value, the sense will be reversed:
	Values above the warning value will be OK, above the critical value but below the warning
	value will be WARNING, and values below the Critical Value will be CRITICAL.

	path: mount point path

	verbose: print detailed output for debugging

	help: prints this message.

"@

}

###############################################################
# Main program
###############################################################

if ($help)
{
	printHelp
	exit $exitCodes["UNKNOWN"]
}

if ($ENV:Processor_Architecture -eq 'x86' -and (test-path env:\PROCESSOR_ARCHITEW6432))
{
	# echo 'WOW layer 64 bit OS/32 bit process'
	&"$env:windir\sysnative\windowspowershell\v1.0\powershell.exe" -noninteractive -noprofile $myinvocation.Line
	exit $LASTEXITCODE
}

# Trailing \ is required for probing, added here for convenience if omitted on the command line.
if ($path -notmatch "\\$")
{
	$path = "$path\"
}
$mountpoint = Get-WmiObject -Class Win32_Volume | Where {$_.Name -eq "$path" -and $_.DriveLetter -eq $null}

if ($mountpoint)
{
	$freepct = ($mountpoint.FreeSpace / $mountpoint.Capacity) * 100
	$state = setState $freepct $warning $critical

	$freepctstat  =  $freepct.ToString("N2")
	$freepctperf  =  $freepct.ToString("F2", [System.Globalization.NumberFormatInfo]::InvariantInfo)

	# Since we declared $warning and $critical to be integers, there is no danger of any confusion with
	# using the wrong character for the decimal separator, so we don't bother to format these numbers
	# here.  Also, testing shows (somewhat surprisingly) that no thousands separators are introduced
	# when formatting integers this way, so again we're not forced to use the invariant format.
	$status_msg = "The Volume mounted at $path has $freepctstat percent free space."
	$perfdata_msg = "pct_free=$freepctperf%;$warning;$critical;;"

	write-host $state": $status_msg|$perfdata_msg"
} else {
	$state = "UNKNOWN"
	$status_msg = "No mount point can be located at $path"
	write-host $state": $status_msg"
}

exit $exitCodes[$state]

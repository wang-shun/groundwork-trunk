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
# 2011-07-06	tk	initial version
# 2011-07-21	tk	query bugfix, code cleanup
# 2011-09-07	tk	don't implement main as a function.
#			add option -list to list unmounted volumes with details.
# 2012-06-12	tk	Introducing * wildcard for all paths.
#			Allow list of paths, warnings and critical values.
# 2012-07-09	gh	Fix perf data to always follow plugin guidelines.
#			Make trailing backslash on path argument optional.
# 2012-07-25	gh	Output perf data even when the overall plugin status is OK.
#			Label the status message disk percentage as "free".
# 2012-09-25	gh	Better handle the cases where no mount points are found or
#			a mount point is found but statistics for it are unavailable
#			(which might be just an artifact of no mounts being found).
#			Fix usage message.  Fix perf-data labels.  Provide more-robust
#			mount-point path matching.
# 2012-12-19	gh	Don't abort early if a pattern match yields no results.
#			Fix usage message (completely overhauled).
#			Fix the -list option so its action is actually useful.
#			Document the manner in which the plugin must be run.

# Arguments, with defaults.  This construction assumes that this plugin has been
# called by a command equivalent to this ugly formulation, which is wrapped here
# for easy viewing but must be all on one line in the GDMA command setup:
#
#     cmd /c "echo c:\progra~2\groundwork\gdma\libexec\v3\diskfree.ps1
#         -path c:,c:\smalldisk -warning 40,45 -critical 30,35; exit $LASTEXITCODE"
#         | powershell -noprofile -noninteractive -command -
#
# if this script lives under C:\Program Files (x86)\... (which maps to c:\progra~2\...).
# A critical point here is to use a path to the plugin that does not contain any spaces.
#
# Use of the powershell -file option instead of the -command option will fail
# if any of the arguments are specified as comma-separated lists, so such
# usage is NOT recommended, in spite of the difficulty with Windows paths
# containing spaces.  The powershell -file option breaks compatibility with the
# -command option in that it cannot handle array parameters correctly; this is a
# MicroSoft-imposed limitation.
param
(
    [int[]]$warning,
    [int[]]$critical,
    [string[]]$path,
    [switch]$list,
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

# This default threshold value is essentially useless, when comparing
# against percentages which range from 0 through 100.
$MAX_THRESHOLD = 999

# Determine state.
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
function Fork64IfWoW
{
	if ($ENV:Processor_Architecture -eq 'x86' -and (test-path env:\PROCESSOR_ARCHITEW6432))
	{
		# echo 'WOW layer 64 bit OS/32 bit process'
		&"$env:windir\sysnative\windowspowershell\v1.0\powershell.exe" -noninteractive -noprofile $myinvocation.Line
		exit $LASTEXITCODE
	}
}

function exitPlugin([string] $state)
{
	exit $exitCodes[$state]
}

function printHelp
{
	write-host @"

Usage:

	diskfree.ps1 -warning <warning value(s)> -critical <critical value(s)> -path <path(s)> [-list] [-verbose] [-help]

	This plugin will check the free space on one or more local volumes
	mounted under drive letters or subsidiary directories.	It is
	possible to specify comma-separated lists of paths (drive letters or
	directories), Warning-threshold values, and Critical-threshold values.
	The overall plugin result is the worst-case state of all the mount
	points discovered and tested.  States and free-space percentages
	of the individual mount points are listed in the text message and
	performance data.  The status text won't list the individual volumes
	if all are in the OK state.

	Shared network drives mounted locally are invisible to this plugin.

	If the Critical threshold value for a particular path is less than the
	corresponding Warning threshold value, the following rules will hold:
	* disk-free percentage values at or above the Warning threshold will be OK.
	* disk-free percentage values below the Warning threshold but at or above
	  the Critical threshold will generate a WARNING state.
	* disk-free percentage values below the Critical threshold will generate
	  a CRITICAL state.
	This is the normal type of configuration you want, to alarm if a
	volume is running out of space.

	If a Warning threshold value for a particular path is less than or equal
	to the corresponding Critical threshold value, the sense will be reversed,
	and the following rules will hold:
	* disk-free percentage values at or below the Warning threshold will be OK.
	* disk-free percentage values above the Warning threshold but at or below
	  the Critical threshold will generate a WARNING state.
	* disk-free percentage values above the Critical threshold will generate
	  a CRITICAL state.
	This type of configuration would only be used if you want to alarm
	if the free space on the volume has fallen too low -- probably an
	unusual tpe of check.

	Mount point path(s) may be specified either explicitly as drive letters
	or directory paths, or as wildcard patterns that can match more than
	one volume.  The following wildcard patterns are currently supported:

	::      Check all local-drive, on-drive-letter mounts only.

	:\      Check all local-drive, on-subdirectory mounts only.

	:*      Check all local-drive, on-drive-letter or on-subdirectory mounts.

	A caveat:  the :: and :* patterns will match drive letters which
	are available but currently unused for mounting removable media
	such as CDs or DVDs.  The states of such paths will be UNKNOWN,
	and this will be taken as the worst-case state of all the volumes,
	so the status of the entire service check will also be UNKNOWN.
	(Unmounted floppy drives, however, are ignored.)

	A second caveat:  using wildcard patterns on a host where the
	number and/or names of the mount points might change over time is
	a bad idea.  The downstream performance-data processing will adapt
	to the form of the first service check result it sees, and it will
	either choke on later results if the number of mount points changes,
	or misapply the data to the wrong categories if the names change.
	Thus, wildcarding the path names is not generally recommended.

	-list:  Lists mounted and unmounted volumes with detailed information.
	All other options (except -help, which overrides -list) are ignored.

	-verbose:  Print detailed output for debugging; not for production use.

	-help:  Prints this message.  All other options are ignored.

Examples:

	diskfree -path c:,d:\mountpoint -warning 20,25 -critical 10,15

	    This command checks free space on both the "C:" drive and the
	    volume mounted at "D:\mountpoint".

	    If the free space on C: is less than 20%, a WARNING state results.
	    If the free space on C: is less than 10%, a CRITICAL state results instead.
	    If the free space on D:\mountpoint is less than 25%, a WARNING state results.
	    If the free space on D:\mountpoint is less than 15%, a CRITICAL state results instead.

	diskfree -path :: -warning 20 -critical 10

	    This command checks free space on all drive letters.
	    For each drive letter ("X:") found:

	    If the free space on X: is less than 25%, a WARNING state results.
	    If the free space on X: is less than 15%, a CRITICAL state results instead.

	    Sample output, wrapped here for convenient viewing:

	    OK C:\ 32.59% free, OK D:\ 71.64% free, UNKNOWN E:\, OK F:\ 99.62% free|
	      C_drive=32.59%;20;10;; D_drive=71.64%;20;10;; E_drive=0%;20;10;; F_drive=99.62%;20;10;;

"@

}

###############################################################
# Main program
###############################################################

if ($help)
{
	printHelp
	exitPlugin("UNKNOWN")
}

#Fork64IfWoW

# Special list option to spill out all mounted or unmounted volumes.
if ($list)
{
	$mountpoints = @( Get-WmiObject -Class Win32_Volume | sort Name )

	if ($mountpoints.length -eq 0) {
		write-host "No mount points found."
	} else {
		foreach ($mountpoint in $mountpoints)
		{
			write-host "Volume name: " $mountpoint.Name
			write-host "`t    Device ID: " $mountpoint.DeviceID
			write-host "`t  File system: " $mountpoint.FileSystem
			write-host "`t   Block size: " $mountpoint.BlockSize
			write-host "`t        Label: " $mountpoint.Label
			write-host "`t     Capacity: " $mountpoint.Capacity
			write-host "`t   Free space: " $mountpoint.FreeSpace
			write-host "`t   Auto mount: " $mountpoint.AutoMount
			write-host "`t   Compressed: " $mountpoint.Compressed
			write-host "`tDirty bit set: " $mountpoint.DirtyBitSet
			write-host ""
		}
	}

	exitPlugin("UNKNOWN")
}

if ($path -isnot [system.array])
{
	write-host "At least one path is required."
	exitPlugin("UNKNOWN")
}

$state = "OK"
$status_msg = ""
$perfdata_msg = " "

# Process each path
For($i=0; $i -lt $path.Count; $i++)
{
	$wpath = $path[$i]

	# Empty values are maxed out for proper comparison
	if ($warning -is [system.array])
	{
		$warning_threshold = $warning[$i]
	} else {
		$warning_threshold = $MAX_THRESHOLD
	}

	if ($critical -is [system.array])
	{
		$critical_threshold = $critical[$i]
	} else {
		$critical_threshold = $MAX_THRESHOLD
	}

	# Select target mount points.  We adopt certain suggestive wildcard conventions for choosing particular types of mounts.
	# Perhaps we want a full wildcard-symbol convention something like this:
	#   "::"   only local-drive on-drive-letter mounts
	#   ":\"   only local-drive on-subdirectory mounts
	#   ":*"   both local-drive on-drive-letter and local-drive on-subdirectory mounts
	#   ":"    all local-drive mounts; equivalent to ":*"
	#   "\:"   only remote-drive on-drive-letter mounts
	#   "\\"   only remote-drive on-subdirectory mounts
	#   "\*"   both remote-drive on-drive-letter and remote-drive on-subdirectory mounts
	#   "\"    all remote-drive mounts; equivalent to "\*"
	#   "*:"   both local-drive on-drive-letter and remote-drive on-drive-letter mounts
	#   "*\"   both local-drive on-subdirectory and remote-drive on-subdirectory mounts
	#   "**"   all mounts, local-drive and remote-drive, on-drive-letter or on-subdirectory
	#   "*"    all mounts; equivalent to "**"
	# For the wildcarded queries, which might return more than one object, we sort by Name to achieve a fixed canonical order.
	# For all queries, we force the result to be an array so we can unambiguously check to see if we got any results back.
	# FIX MINOR:  Also look at Win32_MappedLogicalDisk for info on remote mounts which are mounted directly on a local drive letter,
	# with DeviceID "X:", FreeSpace, Name "X:", ProviderName "\\remotemachine\remotedir", and Size (not Capacity!) fields.
	if ($wpath -eq ":*") {
		# The intent here is to get info on all local-drive, on-drive-letter or on-subdirectory mounts.
		# FIX MINOR:  We haven't yet tested to see if this excludes remote-drive on-subdirectory mounts.
		$mountpoints = @( Get-WmiObject -Class Win32_Volume | sort Name )
	} elseif ($wpath -eq ":\") {
		# The intent here is to get info on all local-drive, on-subdirectory mounts only.
		# FIX MINOR:  We haven't yet tested to see if this excludes remote-drive on-subdirectory mounts.
		$mountpoints = @( Get-WmiObject -Class Win32_Volume | Where {$_.DriveLetter -eq $null} | sort Name )
	} elseif ($wpath -eq "::") {
		# The intent here is to get info on all local-drive, on-drive-letter mounts only.
		$mountpoints = @( Get-WmiObject -Class Win32_Volume | Where {$_.DriveLetter -ne $null} | sort Name )
	} else {
		# Some non-wildcarded, particular mount path.
		# A trailing \ is required for probing of Win32_Volume.Name values.
		# It is added here for convenience if it was omitted on the command line.
		if ($wpath -notmatch "\\$")
		{
			$wpath = "$wpath\"
		}
		# Note that the "-eq $path" comparison is case-insensitive.
		if ($wpath -match "^[A-Za-z]:\\$") {
			$mountpoints = @( Get-WmiObject -Class Win32_Volume | Where {$_.Name -eq "$wpath"} )
		} else {
			$mountpoints = @( Get-WmiObject -Class Win32_Volume | Where {$_.Name -eq "$wpath" -and $_.DriveLetter -eq $null} )
		}
	}

	if ($mountpoints.length -eq 0) {
		$state = "UNKNOWN"
		$status_msg += "UNKNOWN (no mount points for path ""$wpath""), "
	} else {
		foreach ($mountpoint in $mountpoints)
		{
			if ($verbose)
			{
				write-host "Volume: "$mountpoint.Name
			}

			# $mountpoint.Name likely includes characters which are not legal in a performance-data label.
			if ($mountpoint.Name -match "^([A-Za-z]):\\$") {
				$mountname = $matches[1] + "_drive"
			}
			else {
				# Make the performance data label fit the constraints of an RRD DS name.
				# A more-sophisticated different version of this script will allow the user
				# to specify his own replacement string (because no calculation here can ever
				# be clever enough to cover all customer situations), but in this script we
				# have no such information available, so we impose a simple fixed algorithm.
				$mountname = $mountpoint.Name
				$mountname = $mountname -replace '[^a-zA-Z0-9_]+','_'
				if ($mountname.length -gt 19) {
					$mountname = $mountname.substring(0,19)
				}
			}

			if ($mountpoint.FreeSpace -eq $null -or $mountpoint.Capacity -eq $null) {
				$state = "UNKNOWN"
				$status_msg += "UNKNOWN " + $mountpoint.Name + ", "
				# Logically, we would like to report "U%" as the performance result here, but such
				# a special value is not (yet) supported by the Nagios Plugin Development Guidelines.
				# So in the meantime, we report a worst-case 0% value as the performance metric.
				$perfdata_msg += $mountname + "=0%;$warning_threshold;$critical_threshold;; "
			} else {
				$freepct = ($mountpoint.FreeSpace / $mountpoint.Capacity) * 100
				$mstate = setState $freepct $warning_threshold $critical_threshold

				$freepctstat = $freepct.ToString("N2")
				$freepctperf = $freepct.ToString("F2", [System.Globalization.NumberFormatInfo]::InvariantInfo)

				# Check for the worst state
				If ($exitCodes[$mstate] -gt $exitCodes[$state])
				{
					$state = $mstate
				}

				# Since we declared $warning and $critical to be integers, there is no danger of any confusion with
				# using the wrong character for the decimal separator, so we don't bother to format these numbers
				# here.  Also, testing shows (somewhat surprisingly) that no thousands separators are introduced
				# when formatting integers this way, so again we're not forced to use the invariant format.
				$status_msg += $mstate + " " + $mountpoint.Name + " $freepctstat% free, "
				$perfdata_msg += $mountname + "=$freepctperf%;$warning_threshold;$critical_threshold;; "

				# remove max threshold
				$perfdata_msg = $perfdata_msg.Replace("$MAX_THRESHOLD", "")
			}
		}
	}
}

If ($state -ne "OK")
{
	# remove last chars in status string
	$status_msg = $status_msg.Substring(0, $status_msg.Length-2)
} else {
	$status_msg = "OK: All mount points within spec"
}

# remove last char in perfdata string
$perfdata_msg = $perfdata_msg.Substring(0, $perfdata_msg.Length-1)

write-host "$status_msg|$perfdata_msg"
exitPlugin($state)


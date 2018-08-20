# Copyright 2010 GroundWork Open Source Inc.
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
# Author Dr. Dave Blunt, GroundWork Open Source Inc. (dblunt at gwos dot com)
#
# getWMI.ps1

# Sample script that uses the Get-WmiObject method
# Revision
# 2010-12-03 0.1


###############################################################
# Functions
###############################################################

# Is this a Win64 machine regardless of whether or not we are currently 
# running in a 64 bit mode 
function Test-Win64Machine() {
    return test-path (join-path $env:WinDir "SysWow64") 
}
# Is this a Wow64 powershell host
function Test-Wow64() {
    return (Test-Win32) -and (test-path env:\PROCESSOR_ARCHITEW6432)
}
# Is this a 64 bit process
function Test-Win64() {
    return [IntPtr]::size -eq 8
}
# Is this a 32 bit process
function Test-Win32() {
    return [IntPtr]::size -eq 4
}

function getWMI($server,$query,$field,$warning,$critical)
{

 # work out if we need to retrieve data for all instances or specific instance
 # if we return the data for all instances then we need to return the value and the
 # instance name

 $wmiresults      = Get-WmiObject -computername $server -query "$query" | Select-Object $field
 $wmiresults

}

###############################################################
# Main Program
###############################################################

# If we are a 32-bit process running on a 64-bit capable system then reinvoke under 64-bit
# Note that sysnative is only available on Vista or above so XP 64-bit won't work
if ((Test-Win32) -and (Test-Win64Machine))

{
#      write-warning "Running 64-bit PowerShell..."
	&"$env:windir\sysnative\windowspowershell\v1.0\powershell.exe" -noninteractive -noprofile $myinvocation.Line
      exit
}

if (Test-Win32)
{
        $bitversion = 32
} else {
        $bitversion = 64
}

# Constants
$exitCodes = @{
  "UNKNOWN"    = 3;
  "CRITICAL"   = 2;
  "WARNING"    = 1;
  "OK"         = 0
}

# Defaults

$server    = "127.0.0.1"
$query    = "Select DeviceID,LoadPercentage from Win32_Processor"
$field   = "LoadPercentage"

$warning   = 10
$critical  = 5


$a = $args.length


#if ($a -ne 6) {
#  write-warning "Usage: script.ps1 <hostname> <object> <counter> <instance> <warning> <critical>."
#  exit $exitCodes["UNKNOWN"]
#} else {
#  $server   = $args[0]
#  $object   = $args[1]
#  $counter  = $args[2]
#  $instance = $args[3]
#  $warning  = $args[4]
#  $critical = $args[5]
#}

$result = getWMI $server $query $field $warning $critical

write-host "Collected with "$bitversion"-bit PowerShell - WMI "$field" = " $result

exit $exitCodes["OK"]
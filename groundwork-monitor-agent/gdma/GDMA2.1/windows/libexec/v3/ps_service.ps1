# Copyright 2012 GroundWork Open Source Inc.
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
# 2012-07-05 - tk; initial version
# 2012-09-30 - gh; fix arguments specification

# Arguments 
param
(
    [switch]$verbose,
    [string]$service,
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

function serviceRunning{
    param($ServiceName)
    
    $arrService = Get-Service -Name $ServiceName
    
    if ($arrService.Status -eq "Running") {
        $running = $TRUE
    } else {
        $running = $FALSE
    }
    
    if ($verbose) { write-host "Service $ServiceName : "$arrService.Status }
    
    $running
}

function setState($value, $warning, $critical)
{
	# Set plugin state and exit code
	$state="OK"

	if ($warning -le $critical) {
		if ($value -gt $warning) {
			$state="WARNING"
		}
		if ($value -gt $critical){
			$state="CRITICAL"
		}
	} else {
		if ($value -lt $warning) {
			$state="WARNING"
		}
		if ($value -lt $critical){
			$state="CRITICAL"
		}
	}
	
	if ($verbose) { write-host "State: $state" }
	$state
}

# If we are a 32-bit process running on a 64-bit capable system then reinvoke under 64-bit
# Note that sysnative is only available on Vista or above so XP 64-bit won't work
function Fork64IfWoW
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

	ps_service.ps1 -service <name of service to check> -help -verbose
    
    This plugin is a general service check that looks at the argument -service service and alerts critical 
    if it is not running 
	
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

#Fork64IfWoW

$s1 = serviceRunning "$service"

if ($s1) {

    $state = "OK"
    $status_msg = "The $service service is running."
    
} else {

    $state = "CRITICAL"
    $status_msg = "The $service service is not running."

}

write-host $state": $status_msg"

exit $exitCodes[$state]

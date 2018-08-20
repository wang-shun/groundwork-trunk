# Copyright 2011 GroundWork Open Source Inc.
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
# 2012-07-05 - initial version, tk

# Arguments 
param
(
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

	exch_ad_topology_services -help -verbose
    
    This plugin is a custom service check that looks at these 4 windows services and alerts critical 
    if any of them are not running (MSExchangeADTopology,IISAdmin,MSExchangeServiceHost,MSExchangeFDS)
	
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

$s1 = serviceRunning "MSExchangeADTopology"
$s2 = serviceRunning "IISAdmin"
$s3 = serviceRunning "MSExchangeServiceHost"
$s4 = serviceRunning "MSExchangeFDS"

if ($s1 -and $s2 -and $s3 -and $s4) {

    $state = "OK"
    $status_msg = "The AD Topology group of services are running."
    
} else {

    $state = "CRITICAL"
    $status_msg = "The AD Topology group of services are not running."

}

write-host $state": $status_msg"

exit $exitCodes[$state]

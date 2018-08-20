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
# 2011-06-21 - initial version, tk
# 2011-07-19 - add try/catch for exception handling, tk

# Arguments with defaults 
param
(
    [int]$warning   = 50,
    [int]$critical  = 100,
    [int]$waittime  = 2,
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

# Performance counter types with raw values
# ref http://msdn.microsoft.com/en-us/system.diagnostics.performancecountertype.aspx

$perfRawCounters =	
@(
	"NumberOfItems32",
	"NumberOfItems64",
	"NumberOfItemsHEX32",
	"NumberOfItemsHEX64"
)

###############################################################
# Functions
###############################################################

function getCounter($object,$counter,$instance)
{
	try
	{	
		$pptv = 0
	
		if ($instance)
		{
			if ($instance -eq "*")
			{
				# Multiple instances and values
				$perfc              = New-Object System.Diagnostics.PerformanceCounterCategory
				$perfc.CategoryName = $object
				$instances          = $perfc.GetInstanceNames()

				foreach ($objInstance in $instances) {
					$perft   = New-Object System.Diagnostics.PerformanceCounter
					$perft.CategoryName = $object
					$perft.CounterName = $counter
					$perft.InstanceName = $objInstance
					if ($verbose) { write-host "Instance : $objInstance" }
					$pptv += getCounterValue $object $counter $objInstance
				}
			} else {
				# one specific instance, one value
				$pptv = getCounterValue $object $counter $instance
			}
		} else {
			# no instance, one value
			$pptv = getCounterValue $object $counter
		}
	}
	
	catch
	{
		MetricNotFound
	}
	
	$pptv
		
}
    	
function getCounterValue ($object,$counter,$instance)
{
	try
	{
		$perft = New-Object System.Diagnostics.PerformanceCounter
		$perft.CategoryName = $object
		$perft.CounterName = $counter
	
		if ($instance)
		{
			$perft.InstanceName = $instance
		}
	
		$pptv = $perft.NextValue()
		$ptype = $perft.CounterType
		if ($verbose) { write-host "Counter type: $ptype" }
	
		# check if we need to sample once more for calculated values
		if (! ($perfRawCounters -contains $ptype))
		{
			start-sleep -Seconds ($waittime)
			$pptv = $perft.NextValue()
		}
    
		if ($verbose) { write-host "$object($instance) - $counter : $pptv" }
	}
	
	catch
	{
		MetricNotFound
	}
	
    $pptv
}

function MetricNotFound
{
	$state = "UNKNOWN"
	write-host $state": The metric you requested was not found on this system."
	exit $exitCodes[$state]
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

	exch_ad.ps1 -warning <warning value> -critical <critical value> -waittime <wait time secs> -verbose -help
  
	This plugin checks the health and performance of the MS Exchange access to the 
	Active Directory service. 
	Long search and read times, timeouts, or long-running operations indicate that 
	Exchange is having issues accessing Active Directory.  
	The metrics polled are: 
	LDAP Read Time
	LDAP Search Time
	LDAP Searches Timed out Per minute
	Long Running LDAP operations/min
	Thresholds apply to the search and read time. 
	Default Thresholds are warning = $warning, critical = $critical in ms.
 
	Warning value: if the returned numeric value is above this, the state will be WARNING (default $warning).
	
	Critical Value: if the returned value is above this, the state will be CRITICAL (default $critical)

	If the Critical Value is less than the Warning Value, the sense will be reversed:
	Values above the warning value will be OK, above the critical value but below the warning 
	value will be WARNING, and values below the Critical Value will be CRITICAL.
	
	wait time secs: time between samples for calculated perf.counters (default $waittime secs)
	
	verbose: print detailed output for debugging
	
	help: prints this message.

"@

}

###############################################################
# Main program
###############################################################

function main
{

	if ($help)
	{
		printHelp
		exit $exitCodes["UNKNOWN"]
	}

#	Fork64IfWoW
	
	$r = getCounter "MSExchange ADAccess Processes" "LDAP Read Time" "*"
	$s = getCounter "MSExchange ADAccess Processes" "LDAP Search Time" "*"
	$t = getCounter "MSExchange ADAccess Domain Controllers" "LDAP Searches Timed Out per Minute" "*"
	$l = getCounter "MSExchange ADAccess Domain Controllers" "Long Running LDAP Operations/min" "*"
	
	# Check thresholds for s and r
	$state = setState $s $warning $critical
	$state = setState $r $warning $critical	

	$status_msg = "LDAP read time is $r, the search time is $s milliseconds. There are $l long running and $t timed out operations." 
	$perfdata_msg = "ldap_reads_ms=$r;$warning;$critical;; ldap_search_ms=$s;$warning;$critical;; long_ops=$l;;;; timeouts=$t;;;;"

	write-host $state": $status_msg|$perfdata_msg"

	exit $exitCodes[$state]
}

main

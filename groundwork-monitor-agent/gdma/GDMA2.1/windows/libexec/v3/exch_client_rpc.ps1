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
# 2011-07-21 - initial version, tk
# 2011-09-07 - don't implement main as a function

# Arguments with defaults 
param
(
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

	exch_client_rpc.ps1 -waittime <wait time secs> -verbose -help
  
	This plugin checks the cumulative counter for failed RPCs.
	
	wait time secs: time between samples for calculated perf.counters (default $waittime secs)
	
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
	
$tmpfile = "exch_client_rpc.tmp"
	
# check if there is p0 available on disk
if (Test-Path $tmpfile)
{
	$f0 = Get-Content $tmpfile -totalcount 1
} else {
	$f0 = 0
}
	
$f = getCounter "MSExchangeIS" "Client: RPCs Failed: Server Too Busy"
if ($verbose) { write-host "f0: $f0, f: $f" }
# write f as f0 back to disk
$f | out-File $tmpfile
	
if ($f0)
{
	$d = $f - $f0

	if ($d -lt 0)
	{					
		# write f as f0 back to disk (probably a restart)
		$f | out-File $tmpfile
	}
		
	$state = setState $d 0 0
			
	switch ($state) 
	{ 
		"OK" 
			{ $status_msg = "There are no RPC Failures due to the server being too busy.."  }
		"CRITICAL" 
			{ $status_msg = "There are $d RPC Failures due to the server being too busy!" }	
	}
		
} else {
	$d = 0
	$state = "UNKNOWN"
	$status_msg = "Not enough information is available. Wait for next poll."
}

$perfdata_msg = "prc_fails=$d;;;;"

write-host $state": $status_msg|$perfdata_msg"

exit $exitCodes[$state]

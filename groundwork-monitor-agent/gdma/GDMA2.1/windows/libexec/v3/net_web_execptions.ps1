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
# 2011-07-19 -
#	add try/catch for exception handling
#	bugfix - use correct counter (Exceps Thrown/_Global_), tk

# Arguments with defaults 
param
(
    [int]$warning   = 5,
    [int]$critical  = 10,
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

	net_web_execptions.ps1 -warning <warning value> -critical <critical value> -waittime <wait time secs> -verbose -help
  
	This plugin checks the ratio of exceptions to connections. The thresholds are applied to the ratio
	in percentage, that is, if the warning threshold is W, and the Critical is C, then:
	Warning will be issued if Exceptions > W*0.01*Connection Attempts, and 
	Critical will be issued if Exceptions >C*0.01*Connection Attempts. 
	This is a measure of the server's ability to process incoming web connections without error. 
	Failures should be investigated at the network/driver level. 
	Default thresholds are warning = $warning, critical = $critical in percent of exceptions. 

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

	$n = getCounter ".NET CLR Exceptions" "# of Exceps Thrown" "_Global_"
	$a = getCounter "Web Service" "Connection Attempts/sec" "_Total"

	$wthold = $warning * 0.01 * $a
	$cthold = $critical * 0.01 * $a
	$state = setState $n $wthold $cthold

	switch ($state) 
    { 
        "OK" 
			{ $status_msg = "There are relatively few exceptions ($n) per second compared to connections ($a)"  }
        "WARNING" 
			{ $status_msg = "There are relatively many exceptions ($n) per second compared to connections ($a)" }
		"CRITICAL" 
			{ $status_msg = "There are a large number of exceptions ($n) per second compared to connections ($a). This indicates a potential performance issue, and should be investigated." }	
	}
	
	$perfdata_msg = "exceptions=$n;$wthold;$cthold;; connections=$a;;;;"

	write-host $state": $status_msg|$perfdata_msg"

	exit $exitCodes[$state]
}

main

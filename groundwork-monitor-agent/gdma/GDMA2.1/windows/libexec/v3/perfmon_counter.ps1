##############################################################################
#
# NAME: 		perfmon_counter.ps1
#
# ORIGINAL AUTHORS: 	Markus Wälchli, Giuseppe Cancilleri
# MODIFIED BY:		GroundWork Open Source
#
# COMMENT:		Script to check Performance Counters for Groundwork
#
# Usage: perfmon_counter.ps1 -description <Description> -perfcounter <Countername> -warning <Warning Threshold> -critical <Critical Threshold>"
#
# Sample for Actice RDS Sessions:
#        perfmon_counter.ps1 -description ""Active Terminal Sessions"" -perfcounter ""\Terminal Services\Active Sessions"" -warning 5 -critical 10"
#
# CHANGELOG:
# 1.0 27.12.2013 - Initial (named mizu_check_perfmon_counter.ps1 or mizu_perfmon_counter.ps1, not sure)
# 1.1 06.01.2014 - customize output for performance-datas nagios (g.cancilleri)
# 1.2 10.23.2014 - internal script cleanup and script rename (to perfmon_counter.ps1)
#
##############################################################################

#Arguments
param (
         [string]$description,
         [string]$perfcounter,
         [int]$warning,
         [int]$critical
)

$scriptname = "perfmon_counter.ps1"

# Standard Groundwork Exit Codes
$returnStateOK = 0
$returnStateWarning = 1
$returnStateCritical = 2
$returnStateUnknown = 3

$countOK = 0

# Check Arguments
if(!$description -or !$perfcounter) {
    Write-Host ""
    Write-Host "usage: $scriptname -description ""Description"" -perfcounter ""Countername"" [-warning ""Warning Threshold""] [-critical ""Critical Threshold""]"
    Write-Host ""
    Write-Host "example: $scriptname -description ""Active Terminal Sessions"" -perfcounter ""\Terminal Services\Active Sessions"" -warning 5 -critical 10"
    exit 3
}

# Define the Counter Object
$load = (Get-Counter $perfcounter).CounterSamples | select-object CookedValue
$load = $load.CookedValue
#Write-Host $load

#Write-Host $perfcounter

if (($warning -or $critical) -and ($load -ge $critical)) {
    Write-Host "CRITICAL - $description = $load | value=$load;$warning;$critical;;"
    exit $returnStateCritical
}

if (($warning -or $critical) -and ($load -ge $warning)) {
    Write-Host "WARNING - $description = $load | value=$load;$warning;$critical;;"
    exit $returnStateWarning
}

if ($load -ge $countOK) {
    Write-Host "OK - $description = $load | value=$load;$warning;$critical;;"
    exit $returnStateOK
}

Write-Host "UNKNOWN - $description = $load | value=$load;$warning;$critical;;"
exit $returnStateUnknown

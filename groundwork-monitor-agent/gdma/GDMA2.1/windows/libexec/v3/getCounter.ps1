# Copyright 2009 GroundWork Open Source Inc.
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
# Revision
#
# 2009-11-09 0.1


###############################################################
# Functions
###############################################################


function getCounter($server,$object,$counter,$instance,$warning,$critical)
{

 # work out if we need to retrieve data for all instances or specific instance
 # if we return the data for all instances then we need to return the value and the
 # instance name

 # we may have to interpret CounterType and transform the RawValue...

 $perfout = ""

 if ($instance -eq "*") {

   $perfc              = New-Object System.Diagnostics.PerformanceCounterCategory
   $perfc.categoryname = $object
   $instances          = $perfc.GetInstanceNames()

   foreach ($objItem in $instances) {
     $perft   = New-Object System.Diagnostics.PerformanceCounter($object,$counter,$objItem,$server)
     $perfout = "$($perfout)$($objItem)=$($perft.RawValue);"
   }
 } else {
   $perft = New-Object System.Diagnostics.PerformanceCounter($object,$counter,$instance,$server)
   $perfout = "$($instance)=$($perft.RawValue);" 
 }


 $perfout = "$($perft.CounterType) - $($perfout)"
 $perfout

}



###############################################################
# Main Program
###############################################################

# Constants
$exitCodes = @{
  "UNKNOWN"    = 3;
  "CRITICAL"   = 2;
  "WARNING"    = 1;
  "OK"         = 0
}

# Defaults

$server    = "127.0.0.1"
$object    = "LogicalDisk"
$counter   = "% Free Space"
$instance  = "C:"

$warning   = 10
$critical  = 5


$a = $args.length


if ($a -ne 6) {
  write-warning "Usage: script.ps1 <hostname> <object> <counter> <instance> <warning> <critical>."
  exit $exitCodes["UNKNOWN"]
} else {
  $server   = $args[0]
  $object   = $args[1]
  $counter  = $args[2]
  $instance = $args[3]
  $warning  = $args[4]
  $critical = $args[5]
}

$result = getCounter $server $object $counter $instance $warning $critical

write-host "PerformanceCounter "$object"\"$counter" = " $result

exit $exitCodes["OK"]
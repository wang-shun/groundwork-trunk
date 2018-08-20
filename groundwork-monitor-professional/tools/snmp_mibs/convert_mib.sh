#!/bin/sh

# This script is designed to easily convert multiple SNMP MIB files into
# an equal number of SNMPTT configuration files.

# Successful conversion of SNMP MIB files requires that the MIBs be valid.

# Copyright 2007 GroundWork Open Source, Inc. ("GroundWork").  All rights
# reserved.  Use is subject to GroundWork commercial license terms.

print_usage()
    {
    echo ""
    echo "usage:  convert_mib.sh [FILE] ..."
    echo "where:  [FILE] is a valid SNMP MIB file"
    echo ""
    echo "  An SNMPTT configuration file with the extension .conf will be"
    echo "  written out to the current directory."
    }

if [ $# -eq 0 ]; then 
    print_usage
    exit 1  
fi

export MIBDIRS=/usr/local/groundwork/share/snmp/mibs

for file in $*
do
  outfile=`echo $file | sed -e 's/.*\///'`
  /usr/local/groundwork/bin/snmpttconvertmib --in=$file --out=$outfile.conf --format_desc=3 --socket='localhost:4913;localhost:5667;$x;$X;$aA;$A;$o;$O;$s;$N;$c;$+*;$Fz'
done


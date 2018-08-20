#!/bin/sh

# This script is designed to easily convert SNMPTT trap configuration files
# used in GroundWork Monitor Professional (prior to v5.2) to support the new
# SOCKET directive that enables SNMPTT to directly submit trap results to
# NSCA and Foundation instead of having to run an external program.

# The script replaces any EXEC line with a correctly formatted SOCKET line

# Copyright 2007 GroundWork Open Source, Inc. ("GroundWork").  All rights
# reserved.  Use is subject to GroundWork commercial license terms.

print_usage()
    {
    echo ""
    echo "usage:  upgrade_snmptt_confs.sh [FILE] ..."
    echo "where:  [FILE] is an SNMPTT trap configuration file"
    echo ""
    echo "  An SNMPTT configuration file with the extension .new will be"
    echo "  written out to the current directory."
    }

if [ $# -eq 0 ]; then
    print_usage
    exit 1
fi

replacement='localhost:4913;localhost:5667;$x;$X;$aA;$A;$o;$O;$s;$N;$c;$+*;$Fz'

for file in $*
do
  outfile=`echo $file | sed -e 's/.*\///'`
  cat $file | sed -e 's/^EXEC.*/SOCKET '$replacement'/' > $outfile.new
done

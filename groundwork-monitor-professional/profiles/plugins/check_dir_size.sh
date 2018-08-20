#! /bin/sh

# Copyright 2009 GroundWork Open Source, Inc. ("GroundWork")
# All rights reserved. This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License version 2 as published
# by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE.  See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with this
# program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
# Fifth Floor, Boston, MA 02110-1301, USA.
#
# Change Log
#----------------
# 2009-01-23 Dave Blunt
#       Initial revision

PATH=/usr/local/groundwork/common/sbin:/usr/local/groundwork/common/libexec:/usr/local/groundwork/common/bin:/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/bin:/usr/local/sbin

PROGNAME=`basename $0`
PROGPATH=`echo $0 | sed -e 's,[\\/][^\\/][^\\/]*$,,'`
REVISION=`echo '$Revision: 1.1.1.1 $' | sed -e 's/[^0-9.]//g'`
STATUS=""

. $PROGPATH/utils.sh


print_usage() {
        echo "Usage: $PROGNAME <directory> <warning size in files> <critical size in files>"
}

print_help() {
        print_revision $PROGNAME $REVISION
        echo ""
        print_usage
        echo ""
        echo "This plugin checks a directory for the number of files it contains "
	echo "and alarms if that number is above supplied thresholds.  If warning "
	echo "threshold is lower than the critical threshold then the comparison is "
	echo "reversed."
        echo ""
        support
        exit 0
}

if [ $# -ne 3 ]; then
        print_usage
        exit $STATE_UNKNOWN
fi

case "$1" in
        --help)
                print_help
                exit $STATE_UNKNOWN
                ;;
        -h)
                print_help
                exit $STATE_UNKNOWN
                ;;
        --version)
        print_revision $PROGNAME $REVISION
                exit $STATE_UNKNOWN
                ;;
        -V)
                print_revision $PROGNAME $REVISION
                exit $STATE_UNKNOWN
                ;;
        *)
		DIRECTORY=$1
                WARNING=$2
		CRITICAL=$3
		LENGTH=`/bin/ls $DIRECTORY | /usr/bin/wc -l`
		perf_string="length=$LENGTH;$WARNING;$CRITICAL;0;"
		status_string="Directory length = $LENGTH."

		if [ $CRITICAL -gt $WARNING ]; then
			if [ $LENGTH -gt $CRITICAL ]; then
				exit_status=$STATE_CRITICAL
				status="CRITICAL"
			elif [ $LENGTH -gt $WARNING ]; then
				exit_status=$STATE_WARNING
				status="WARNING"
			else
				exit_status=$STATE_OK
				status="OK"
			fi
		else
			if [ $LENGTH -lt $CRITICAL ]; then
				exit_status=$STATE_CRITICAL
				status="CRITICAL"
			elif [ $LENGTH -lt $WARNING ]; then
				exit_status=$STATE_WARNING
				status="WARNING"
			else
				exit_status=$STATE_OK
				status="OK"
			fi
		fi

		echo "$status: $status_string | $perf_string"
		exit $exit_status
                ;;
esac

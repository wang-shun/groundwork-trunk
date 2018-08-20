#!/bin/bash
#
# Write a lot of snmp traps to the supplied  host.
datetime=`date +%s`
j=0
      while [ $j -lt $1 ]
      do
      seed=`date +%N`
      rnd=`expr $seed \/ 10000000`
      starttime=`date +%s`
      i=0
      while [ $i -lt 100 ]
      do
        if [ "$i" -eq "$rnd" ]
           then
             res=`exec /usr/local/groundwork/bin/snmptrap -v 1 -c public $2 "" "192.168.23.$i" 1 0 ""`
        fi
        i=`expr $i + 1`
      done
	j=`expr $j + 1`
done
endtime=`date +%s`
diff=`expr $endtime - $datetime`
echo "processed $1 traps in $diff seconds"


exit 0


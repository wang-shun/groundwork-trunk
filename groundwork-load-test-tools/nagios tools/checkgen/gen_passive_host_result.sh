#!/bin/bash
number=$1
mystart=`date +%s`
mydate=`date`
echo "Starting at $mydate"
t=1
echocmd="/bin/echo"
CommandFile="/usr/local/groundwork/nagios/var/spool/nagios.cmd"
while [ "$t" -le "$3" ]
do
    while [ "$number" -le $2 ]
    do
        echo "Setting host $number down"
        if test $number -le 9
        then
            cmdline="[$datetime] PROCESS_HOST_CHECK_RESULT;random_counter_host_0$number;1;test down|"
            # append the command to the end of the command file
            `$echocmd $cmdline >> $CommandFile`
        else
            cmdline="[$datetime] PROCESS_HOST_CHECK_RESULT;random_counter_host_$number;1;test down|"
            # append the command to the end of the command file
            `$echocmd $cmdline >> $CommandFile`
        fi
        let "number=$number + 1"
    done
    number=$1
    let "t = $t +1"
done
myend=`date +%s`
let "mytime = $myend - $mystart"
echo "Time Taken to send: $mytime"
mytime=`date`
echo "Completed at $mytime"
exit 0

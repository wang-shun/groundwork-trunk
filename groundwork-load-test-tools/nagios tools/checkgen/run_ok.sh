#!/bin/bash
number=$1
mystart=`date +%s`
mydate=`date`
echo "Starting at $mydate"
t=1
while [ "$t" -le "$3" ]
do
        while [ "$number" -le $2 ]
        do
                echo "Setting OK for host $number"
                if test $number -le 9
        then
                        exec `./gen_passive_service_nsca.pl  -c ./input_conf_blast_OK_changes.cfg -S random_counter_host_0$number -l ./log_host_0$number.log > /dev/null` &
                else
                        exec `./gen_passive_service_nsca.pl  -c ./input_conf_blast_OK_changes.cfg -S random_counter_host_$number -l ./log_host_$number.log > /dev/null` &
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

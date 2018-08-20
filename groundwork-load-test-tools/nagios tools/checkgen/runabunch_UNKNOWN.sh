#!/bin/bash
number=0
while [ "$number" -le $1 ]
do
    echo "Running host $number"
    if test $number -le 9
    then
		 exec `./gen_passive_service_nsca.pl  -c ./input_conf_blast_UNKNOWN.cfg -S random_counter_host_0$number -l ./log_host_0$number.log > /dev/null` &

    else
     exec `./gen_passive_service_nsca.pl  -c ./input_conf_blast_UNKNOWN.cfg -S random_counter_host_$number -l ./log_host_$number.log > /dev/null` &
	fi
    let "number=$number + 1"
done

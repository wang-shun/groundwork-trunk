#!/usr/local/bin/bash
# this script can be used in a busybot container to simulate load. Recommend varying sleep values with 0. 0.05, 0.1, 1, 2
# docker run -it --rm --name bash bash
# Then paste in and run this busy.sh script  (chmod +x)
# To see usage 'docker stats' or run CloudHub
N=1
if [ ! -z "$1" ]
  then
    N=$1
fi
while true
do
	for ((i=1;i<=100;i++));
	do
	   # your-unix-command-here
	   let x=2000*$i+1500*10
	done
	echo "sleeping for $N, press ctrl-c to exit"
	sleep $N

done

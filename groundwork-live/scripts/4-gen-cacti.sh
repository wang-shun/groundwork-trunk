#!/bin/bash

# uses the content of the previously created HOSTS file to add items to Cacti
# Rules about what to add are in the "add_devices.pl" script
# and should be consulted
# The LOCAL snmpd daemon is enabled to be responsive to ALL these polls from Cacti 

source /usr/local/groundwork/scripts/setenv.sh
yum install -y net-snmp
cp ../input/$1/snmpd.conf /etc/snmp/snmpd.conf
service snmpd stop
service snmpd start
./add_devices.pl ../input/$1/HOSTS ../input/$1/add-cacti.log $1
./add_graphs.pl ../input/$1/HOSTS ../input/$1/add-cacti-graph.log $1
exit

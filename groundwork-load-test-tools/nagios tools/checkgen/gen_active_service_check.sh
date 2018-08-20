#!/bin/bash
#
# Write a command to the Nagios command file to cause a service check to be scheduled.  
# do it a lot. 
# get the current date/time in seconds since UNIX epoch
echocmd="/bin/echo"

CommandFile="/usr/local/groundwork/nagios/var/spool/nagios.cmd"
starttime=`date +%s`
i=0
      while [ $i -le $1 ]
      do
datetime=`date +%s`
# create the command line to add to the command file
cmdline="[$datetime] PROCESS_SERVICE_CHECK_RESULT;localhost$i;Current Load;3;Initialize test|;$starttime"
`$echocmd $cmdline >> $CommandFile`
cmdline="[$datetime] PROCESS_SERVICE_CHECK_RESULT;localhost$i;Current Users;3;Initialize test|;$starttime"
`$echocmd $cmdline >> $CommandFile`
cmdline="[$datetime] PROCESS_SERVICE_CHECK_RESULT;localhost$i;Local_Disk_Root;3;Initialize test|;$starttime"
`$echocmd $cmdline >> $CommandFile`
cmdline="[$datetime] PROCESS_SERVICE_CHECK_RESULT;localhost$i;Local_MySql_Database;3;Initialize test|;$starttime"
`$echocmd $cmdline >> $CommandFile`
cmdline="[$datetime] PROCESS_SERVICE_CHECK_RESULT;localhost$i;Local_MySql_Engine;3;Initialize test|;$starttime"
`$echocmd $cmdline >> $CommandFile`
cmdline="[$datetime] PROCESS_SERVICE_CHECK_RESULT;localhost$i;Local_MySql_Engine_NoPw;3;Initialize test|;$starttime"
`$echocmd $cmdline >> $CommandFile`
cmdline="[$datetime] PROCESS_SERVICE_CHECK_RESULT;localhost$i;Local_MySqld;3;Initialize test|;$starttime"
`$echocmd $cmdline >> $CommandFile`
cmdline="[$datetime] PROCESS_SERVICE_CHECK_RESULT;localhost$i;Local_MySqld_Safe;3;Initialize test|;$starttime"
`$echocmd $cmdline >> $CommandFile`
cmdline="[$datetime] PROCESS_SERVICE_CHECK_RESULT;localhost$i;Local_mem;3;Initialize test|;$starttime"
`$echocmd $cmdline >> $CommandFile`
cmdline="[$datetime] PROCESS_SERVICE_CHECK_RESULT;localhost$i;Local_swap;3;Initialize test|;$starttime"
`$echocmd $cmdline >> $CommandFile`
cmdline="[$datetime] PROCESS_SERVICE_CHECK_RESULT;localhost$i;local_nagios;3;Initialize test|;$starttime"
`$echocmd $cmdline >> $CommandFile`
cmdline="[$datetime] PROCESS_SERVICE_CHECK_RESULT;localhost$i;local_nagios_latency;3;Initialize test|;$starttime"
`$echocmd $cmdline >> $CommandFile`
cmdline="[$datetime] PROCESS_SERVICE_CHECK_RESULT;localhost$i;local_procs_gw_feeders;3;Initialize test|;$starttime"
`$echocmd $cmdline >> $CommandFile`
cmdline="[$datetime] PROCESS_SERVICE_CHECK_RESULT;localhost$i;local_procs_gw_listener;3;Initialize test|;$starttime"
`$echocmd $cmdline >> $CommandFile`
cmdline="[$datetime] PROCESS_SERVICE_CHECK_RESULT;localhost$i;local_procs_nsca;3;Initialize test|;$starttime"
`$echocmd $cmdline >> $CommandFile`
cmdline="[$datetime] PROCESS_SERVICE_CHECK_RESULT;localhost$i;local_procs_snmptrapd;3;Initialize test|;$starttime"
`$echocmd $cmdline >> $CommandFile`
cmdline="[$datetime] PROCESS_SERVICE_CHECK_RESULT;localhost$i;local_procs_snmptt;3;Initialize test|;$starttime"
`$echocmd $cmdline >> $CommandFile`
        i=`expr $i + 1`
      done
endtime=`date +%s`
diff=`expr $endtime - $starttime`
echo "processed $1 events in $diff seconds"
exit 0

#$Id: $
# Copyright (C) 2013 GroundWork Open Source, Inc. ("GroundWork")  
# All rights reserved. Use is subject to GroundWork commercial license terms.
#

HOME=/home/build
BASE=$HOME/groundwork-monitor

#BASE=/home/nagios/groundwork-monitor
prefix=/usr/local/groundwork

mkdir -p $prefix/nagios/libexec
/bin/cp -f $BASE/monitor-professional/database/monarch-remove-event-broker.sql $prefix/databases/monarch-remove-event-broker.sql
/bin/cp -rp $BASE/monitor-professional/profiles/plugins/* $prefix/nagios/libexec

/bin/echo "Setting up the permissions..."
/bin/chown -R nagios:nagios $prefix

#Database configuration file
/bin/mv -f  $prefix/config/db.properties.pro $prefix/config/db.properties
/bin/mv -f  $prefix/config/foundation.properties.pro $prefix/config/foundation.properties

# Set permissions for feeder script
/bin/chmod ugo+x $prefix/foundation/feeder/nagios2collage_eventlog.pl

# Set permissions for foundation pro script
/bin/chmod ugo+x $prefix/foundation/scripts/reset_passive_check.sh

# Snmptt permissions
/bin/chown -R nagios:nagios $prefix/var/spool &>/dev/null
/bin/chmod 750 $prefix/var/spool &>/dev/null
/bin/chown -R root:nagios $prefix/var/spool/snmptt &>/dev/null
/bin/chmod 770 $prefix/var/spool &>/dev/null

/bin/chmod -R uog+w $prefix/profiles
/bin/chmod 755 $prefix/profiles

#Tools execute permission
/bin/chown -R -h nagios.nagios $prefix/tools &>/dev/null
/bin/chmod -R uog+x $prefix/tools &>/dev/null
/bin/cp -f $BASE/monitor-professional/resources/update-nagios-cfg.pl $prefix/bin
/bin/chmod +x $prefix/bin/update-nagios-cfg.pl

#Apache2 permissions
/bin/chmod -R oug+x $prefix/apache2/cgi-bin &>/dev/null

#Guava permissions
/bin/chown -R -h nagios.nagios $prefix/guava

#Log Reporting permissions
/bin/chmod -R +x $prefix/log-reporting &>/dev/null

/bin/mkdir -p $prefix/log-reporting/logs &>/dev/null
/bin/touch $prefix/log-reporting/logs/log-reporting.log  &>/dev/null
/bin/chmod -R a+rwx $prefix/log-reporting/logs &>/dev/null

#Foundation LOG permissions so that Monarch and other apps can write to it
/bin/chmod 777 $prefix/foundation/container/logs &>/dev/null

/bin/chown -R nagios.nagios $prefix/tmp
/bin/chmod 770 $prefix/tmp
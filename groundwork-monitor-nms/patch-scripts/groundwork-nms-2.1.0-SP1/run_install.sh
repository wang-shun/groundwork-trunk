#!/bin/bash 
# Copyright (C) 2009 GroundWork Open Source, Inc. ("GroundWork")
# All rights reserved. Use is subject to GroundWork commercial license terms.

gw_home=/usr/local/groundwork
nms_home=$gw_home/nms
ent_home=$gw_home/enterprise

# Check if monitor-pro-5.3.0 or monitor-enterprise-5.3.0 is installed.
if !(grep 'Pro' $gw_home/Info.txt | grep '5.3.0-' > /dev/null); then
  echo "Groundwork Monitor Professional 5.3.0 is not installed in this machine."
  echo "  Exiting..."
  exit -1
fi

# Check if monitor-nms-2.1.0 is installed.
if !(rpm -qa | grep 'groundwork-nms-core-2.1.0' > /dev/null); then
  echo "Groundwork Monitor NMS 2.1.0 is not installed in this machine."
  echo "  Exiting..."
  exit -1
fi

# Check if monitor-nms-2.1.0-SP1 is installed
if (grep 'core/monarch/automation' $ent_home/bin/components/deploy_application_nms-automation.pl > /dev/null); then
  echo "Groundwork Monitor NMS 2.1.0 SP1 is already installed in this machine."
  echo "  Exiting..."
  exit -1
fi

# Backup monitor-nms-2.1.0
mkdir -p $gw_home/nms-2.1.0.backup/core/monarch/automation
cp -rp $nms_home $gw_home/nms-2.1.0.backup
cp -rp $ent_home $gw_home/nms-2.1.0.backup
cp -rp $gw_home/core/monarch/automation/templates $gw_home/nms-2.1.0.backup/core/monarch/automation
crontab -u nagios -l > $gw_home/nms-2.1.0.backup/nagios_cron.bak

# Set permissions 
/bin/cp -rp ./src /tmp/nms_src_tmp
/bin/chmod +x /tmp/nms_src_tmp/*.pl
/bin/chmod +x /tmp/nms_src_tmp/*.sh

# Copy patch files
/bin/cp -pf /tmp/nms_src_tmp/deploy_application_nms-automation.pl $ent_home/bin/components
/bin/cp -pf /tmp/nms_src_tmp/deploy.pl 		$ent_home/bin

/bin/cp -pf /tmp/nms_src_tmp/import_schema.sql 	$nms_home/tools/automation/templates 

# Special patch for SuSE
if [ -f /etc/SuSE-release ] ; then
  /bin/cp -pf /tmp/nms_src_tmp/nms-httpd.suse 	$ent_home/bin/components/httpd
  /bin/cp -pf /tmp/nms_src_tmp/nms-httpd.suse	$nms_home/tools/installer/httpd
else
  /bin/cp -pf /tmp/nms_src_tmp/nms-httpd	$ent_home/bin/components/httpd
  /bin/cp -pf /tmp/nms_src_tmp/nms-httpd	$nms_home/tools/installer/httpd
fi

# Patch Ntop
if (rpm -qa | grep 'groundwork-nms-ntop-2.1.0' > /dev/null); then
  /bin/cp -pf /tmp/nms_src_tmp/deploy_application_ntop-pkg.pl $ent_home/bin/components
fi

# Path Weathermap
if (rpm -qa | grep 'groundwork-nms-weathermap-2.1.0' > /dev/null); then
  /bin/cp -pf /tmp/nms_src_tmp/deploy_application_weathermap-editor-pkg.pl $ent_home/bin/components
fi

# Patch Nedi
if (rpm -qa | grep 'groundwork-nms-nedi-2.1.0' > /dev/null); then
  /bin/cp -pf /tmp/nms_src_tmp/schema-template-NeDi-host-import-2.1.0.xml $nms_home/tools/automation/templates
  /bin/cp -pf /tmp/nms_src_tmp/schema-template-NeDi-host-import-2.1.0.xml $gw_home/core/monarch/automation/templates
  /bin/cp -pf /tmp/nms_src_tmp/schema-template-NeDi-parent-child-sync-2.1.0.xml $nms_home/tools/automation/templates
  /bin/cp -pf /tmp/nms_src_tmp/schema-template-NeDi-parent-child-sync-2.1.0.xml $gw_home/core/monarch/automation/templates
  /bin/cp -pf /tmp/nms_src_tmp/deploy_application_nedi-pkg.pl $ent_home/bin/components
  /bin/cp -pf /tmp/nms_src_tmp/extract_nedi.pl	$nms_home/tools/automation/scripts
  # Update nagios' crontab for Nedi
  crontab -u nagios -l > /tmp/nms_src_tmp/nagios_cron.tmp
  /bin/echo "" >> /tmp/nms_src_tmp/nagios_cron.tmp
  /bin/echo "0 4,8,12,16,20 * * * (/usr/local/groundwork/nms/tools/perl/bin/perl /usr/local/groundwork/nms/applications/nedi/nedi.pl -clo ; /usr/local/groundwork/nms/tools/automation/scripts/extract_nedi.pl )> /dev/null 2>&1" >> /tmp/nms_src_tmp/nagios_cron.tmp
  /bin/echo "0 0 * * * /usr/local/groundwork/nms/tools/perl/bin/perl /usr/local/groundwork/nms/applications/nedi/nedi.pl -clob > /dev/null 2>&1" >> /tmp/nms_src_tmp/nagios_cron.tmp
  crontab -u nagios /tmp/nms_src_tmp/nagios_cron.tmp
fi

/bin/sleep 5
# Patch Cacti
if (rpm -qa | grep 'groundwork-nms-cacti-2.1.0' > /dev/null); then
  /bin/cp -pf /tmp/nms_src_tmp/schema-template-Cacti-host-profile-sync-2.1.0.xml $nms_home/tools/automation/templates
  /bin/cp -pf /tmp/nms_src_tmp/schema-template-Cacti-host-profile-sync-2.1.0.xml $gw_home/core/monarch/automation/templates
  /bin/cp -pf /tmp/nms_src_tmp/deploy_application_cacti-pkg.pl $ent_home/bin/components 
  /bin/cp -pf /tmp/nms_src_tmp/check_cacti.pl	$ent_home/bin/components/plugins/cacti/scripts
  /bin/cp -pf /tmp/nms_src_tmp/check_cacti.pl	$nms_home/tools/installer/plugins/cacti/scripts
  /bin/cp -pf /tmp/nms_src_tmp/extract_cacti.pl $nms_home/tools/automation/scripts
  /bin/cp -pf /tmp/nms_src_tmp/cacti_cron.sh	$gw_home/common/bin
  
  # NMS-335
  /bin/chmod 600 $ent_home/bin/components/plugins/cacti/config/check_cacti.conf
  
  # Update nagios' crontab for Cacti
  crontab -u nagios -l | grep -v extract_cacti.pl > /tmp/nms_src_tmp/nagios_cron.tmp
  /bin/echo "" >> /tmp/nms_src_tmp/nagios_cron.tmp
  /bin/echo "*/5 * * * * /usr/local/groundwork/common/bin/cacti_cron.sh > /dev/null 2>&1" >> /tmp/nms_src_tmp/nagios_cron.tmp
  crontab -u nagios /tmp/nms_src_tmp/nagios_cron.tmp
fi

# Set ownerships
chown -R nagios:nagios $gw_home/nms
chown -R nagios:nagios $gw_home/enterprise
chown nagios:nagios $gw_home/common/bin/cacti_cron.sh
chown nagios:nagios $gw_home/core/monarch/automation/templates/*.xml
chown nagios:nagios $gw_home/enterprise/bin/components/httpd

# Restart Apache
$gw_home/ctlscript.sh restart apache 
/etc/init.d/nms-httpd restart

# Cleanup /tmp
/bin/rm -rf /tmp/nms_src_tmp

/bin/echo "You have successfully installed Groundwork NMS 2.1.0 SP1..."
/bin/echo "    Your original NMS is backed up at $gw_home/nms-2.1.0.backup"

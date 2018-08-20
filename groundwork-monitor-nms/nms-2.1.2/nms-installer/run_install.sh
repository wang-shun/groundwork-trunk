#!/bin/sh
#
#Copyright (C) 2009 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#
 
echo "GroundWork Monitor NMS 2.1.2 Installer"

gw_home=/usr/local/groundwork
perlCursesRPM=`ls -1 packages/perl-Curses* 2>/dev/null`;
 

hasPerl=`perl  -v 2> /dev/null | grep -c 'This is perl'`
noCurses=`perl -MCurses -e 'print $Curses::VERSION' 2>&1 | grep -c '^Can'`
noTermReadKey=`perl -MTerm::ReadKey -e 'print $Term::ReadKey::VERSION' 2>&1 | grep -c '^Can'`;

if [ "$hasPerl" = "1" ]
then
	# check if perl-Curses is installed
	###########################################
	 if [ "$noCurses" = "1" ]
	 then 
	 	echo "perl-Curses is required to run this installer."
	 	echo -n "May I install perl-Curses now? (y/n)"
	 	read doInstall
	 	if [ "$doInstall" = "y" ] || [ "$doInstall" = "yes" ]
	 	then
	 		rpm -ivh ${perlCursesRPM}
	 	else
	 		echo "Exiting Installer"
	 		exit
	    fi
	fi	 
	
	# check if perl-Term-Readkey is installed
	###########################################
	if [ "$noTermReadKey" = "1" ]
	then
		echo "perl-Term-ReadKey is required to run this installer."
		echo -n "May I install perl-Term-Readkey now? (y/n)"
		read doInstall
	 	if [ "$doInstall" = "y" ] || [ "$doInstall" = "yes" ]
	 	then
	 		rpm -ivh packages/perl-Term*rpm
	 	else
	 		echo "Exiting Installer"
	 		exit
	    fi
	fi	
		
	
	# Install GroundWork Monitor
	#############################	 	
	echo running GroundWork Installer....

	source $gw_home/scripts/setenv.sh

	sleep  1
	bin/nms-installer.pl
else echo "Perl is required to run this installer. Please install a copy and rerun this script."
fi

# Stop Groundwork Services
/etc/init.d/groundwork stop

/bin/cp -pf ./src/import_schema.sql $gw_home/nms/tools/automation/templates &>/dev/null
/bin/cp -pf ./src/*.xml $gw_home/nms/tools/automation/templates &>/dev/null
/bin/cp -pf ./src/*.xml $gw_home/core/monarch/automation/templates &>/dev/null
/bin/cp -pf ./src/check_cacti.pl $gw_home/nms/tools/installer/plugins/cacti/scripts &>/dev/null
/bin/cp -pf $gw_home/enterprise/bin/components/plugins/cacti/scripts/check_cacti.pl $gw_home/nms/tools/installer/plugins/cacti/scripts/check_cacti.pl

/bin/chown nagios:nagios $gw_home/nms/tools/automation/templates/import_schema.sql &>/dev/null
/bin/chown nagios:nagios $gw_home/nms/tools/automation/templates/* &>/dev/null
/bin/chown nagios:nagios $gw_home/core/monarch/automation/templates/* &>/dev/null
/bin/chown nagios:nagios $gw_home/nms/tools/installer/plugins/cacti/scripts/check_cacti.pl &>/dev/null


# 6.0

/bin/echo "Install NMS Portlet application"
# Installing an empty groundwork-monitor-core RPM, so NMS un-install doesn't remove nagios user
/bin/cp -p ./src/groundwork-nms-2.1.2.war $gw_home/foundation/container/webapps/jboss/jboss-portal.sar
if [ -f $gw_home/uninstall ] ; then
  /bin/rpm -ivh ./src/groundwork-monitor-core-0-0.noarch.rpm 
fi

/bin/echo "Patching cacti"
patch -d $gw_home/nms/applications/cacti/include < ./conf/cacti-2.1.2.patch

/bin/echo "Patching nedi"
patch -d $gw_home/nms/applications/nedi/html < ./conf/nedi-2.1.2.patch

/bin/echo "Patching NMS Apache configuration"
(echo "g/TKTAuthLoginURL/d"; echo 'wq') | ex -s $gw_home/nms/tools/httpd/conf/httpd.conf
patch -d $gw_home/nms/tools/httpd/conf < ./conf/httpd-2.1.2.patch

# Start Groundwork Services
/etc/init.d/groundwork start

# Re-start NMS Service
/etc/init.d/nms-httpd stop
/etc/init.d/nms-httpd start

echo "Exiting Installer"

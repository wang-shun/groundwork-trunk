#!/bin/sh
#
#Copyright 2009 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#
 
echo "GroundWork Monitor NMS 2.1.1 Installer"

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

	source /usr/local/groundwork/scripts/setenv.sh

	sleep  1
	bin/nms-installer.pl
else echo "Perl is required to run this installer. Please install a copy and rerun this script."
fi

gw_home=/usr/local/groundwork
/bin/cp -pf ./src/import_schema.sql $gw_home/nms/tools/automation/templates &>/dev/null
/bin/cp -pf ./src/*.xml $gw_home/nms/tools/automation/templates &>/dev/null
/bin/cp -pf ./src/*.xml $gw_home/core/monarch/automation/templates &>/dev/null
/bin/cp -pf ./src/check_cacti.pl $gw_home/nms/tools/installer/plugins/cacti/scripts &>/dev/null
/bin/cp -pf $gw_home/enterprise/bin/components/plugins/cacti/scripts/check_cacti.pl $gw_home/nms/tools/installer/plugins/cacti/scripts/check_cacti.pl

/bin/chown nagios:nagios $gw_home/nms/tools/automation/templates/import_schema.sql &>/dev/null
/bin/chown nagios:nagios $gw_home/nms/tools/automation/templates/* &>/dev/null
/bin/chown nagios:nagios $gw_home/core/monarch/automation/templates/* &>/dev/null
/bin/chown nagios:nagios $gw_home/nms/tools/installer/plugins/cacti/scripts/check_cacti.pl &>/dev/null

$gw_home/ctlscript.sh restart apache &>/dev/null
echo "Exiting Installer"

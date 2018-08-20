<?php
/*
Guava - A PHP Based Application Framework and Environment
Copyright (C) 2007 Groundwork Open Source Solutions

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA. 
*/

/*
	Make sure your webserver has write access to this file.  Do *not*
	modify this file by hand unless you know strictly what you are doing.
	
*/

// start.guavacore
require_once(GUAVA_FS_ROOT . 'packages/guava/views/home.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/guava/views/administration.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/guava/views/wrappit.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/guava/sysmodules/ldapauth.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/guava/support/groupmanagement.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/guava/support/package.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/guava/support/packagemanagement.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/guava/support/rolemanagement.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/guava/support/theme.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/guava/support/thememanagement.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/guava/support/usermanagement.inc.php');
// end.guavacore

// start.gwmonarchez
require_once(GUAVA_FS_ROOT . 'packages/ezmonarch/views/configurationez.inc.php');
// end.gwmonarchez
// start.gwmonarch
require_once(GUAVA_FS_ROOT . 'packages/monarch/views/configuration.inc.php');
// end.gwmonarch
// start.nagios
require_once(GUAVA_FS_ROOT . 'packages/nagios/views/nagios.inc.php');
// end.nagios
// start.nagiosmap
require_once(GUAVA_FS_ROOT . 'packages/nagiosmap/views/nagiosmap.inc.php');
// end.nagiosmap
// start.nagiosreports
require_once(GUAVA_FS_ROOT . 'packages/nagiosreports/views/nagiosreports.inc.php');
// end.nagiosreports
// start.bookshelf
//require_once(GUAVA_FS_ROOT . 'packages/bookshelf/views/bookshelf.inc.php');
// end.bookshelf
// start.gwstatusviewer2
require_once(GUAVA_FS_ROOT . 'packages/sv2/support/sv2config.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/sv2/support/sv2navnode.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/sv2/support/troubleview.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/sv2/support/sv2commandview.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/sv2/support/sv2hostgroupview.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/sv2/support/sv2hostlist.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/sv2/support/sv2hostview.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/sv2/support/sv2hostgrouplist.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/sv2/support/sv2overview.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/sv2/support/sv2searchresults.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/sv2/support/sv2servicelist.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/sv2/support/sv2serviceview.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/sv2/support/sv2presenter.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/sv2/support/sv2perfgraphs.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/sv2/support/sv2piegraph.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/sv2/support/sv2filterview.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/sv2/sysmodules/sv2.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/sv2/apps/sv2.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/sv2/support/sv2tooltipicon.inc.php');
// end.gwstatusviewer2
// start.reports
require_once(GUAVA_FS_ROOT . 'packages/reports/views/reports.inc.php');
// end.reports
// start.performanceconfiguration
require_once(GUAVA_FS_ROOT . 'packages/performanceconfiguration/views/performanceconfiguration.inc.php');
// end.performanceconfiguration
// start.monitoringserver
require_once(GUAVA_FS_ROOT . 'packages/monitoringserver/views/monitoringserver.inc.php');
// end.monitoringserver
// start.gwfoundation
require_once(GUAVA_FS_ROOT . 'packages/foundation/support/foundationconfig.inc.php');
require_once(GUAVA_FS_ROOT . 'packages/foundation/sysmodules/foundation.inc.php');
// end.gwfoundation
// start.performance
require_once(GUAVA_FS_ROOT . 'packages/performance/views/performance.inc.php');
// end.performance
// end.file
?>

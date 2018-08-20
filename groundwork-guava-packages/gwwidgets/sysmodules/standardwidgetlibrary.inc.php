<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/

class GWStandardWidgetLibrary extends SystemModule {

    function __construct() {
    	parent::__construct("GWStandardWidgetLibrary");
    	
    }
    
    function init()     { 
    	$this->registerWidgets(); 
	}
	
    function restart()  { 
    	$this->registerWidgets();
    }

    function registerWidgets() {
		global $widgetdaemon;
		$widgetdaemon->register("gwwidgets.hostgrouplist",    "GWWidgetsHostGroupListWidget", "HostGroup List");
		$widgetdaemon->register("gwwidgets.hostlist",    "GWWidgetsHostListWidget", "Host List");
		$widgetdaemon->register("gwwidgets.troubledhostslist",    "GWWidgetsTroubledHostsListWidget", "Troubled Hosts List");
		$widgetdaemon->register("gwwidgets.troubledserviceslist",    "GWWidgetsTroubledServicesListWidget", "Troubled Services List");
		$widgetdaemon->register("gwwidgets.servicelist", "GWWidgetsServiceListWidget", "Service List");
		$widgetdaemon->register("gwwidgets.perfgraph", "GWWidgetsPerformanceGraphWidget", "Performance Graph");
		$widgetdaemon->register("gwwidgets.tacoverview", "GWWidgetsTacOverviewWidget", "Tactical Overview");
		$widgetdaemon->register("gwwidgets.console", "GWWidgetsConsoleWidget", "Console");
		$widgetdaemon->register("gwwidgets.url","ExternalWidget","External URL Widget");
    }

}

?>
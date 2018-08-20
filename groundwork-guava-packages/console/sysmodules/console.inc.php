<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/ 

class ConsoleSystemModule extends SystemModule {
	
	function __construct() {
		global $guava;
		global $sv;
		parent::__construct("ConsoleSystemModule");		
	}
	
	function init() {
		global $sv;
		global $foundationDB;

	}
	
	function restart() {
		// empty?
	}
}
?>
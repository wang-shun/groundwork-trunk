#!/usr/local/groundwork/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

package GWUtils;

  
sub isInt{
	 $num = shift; 
	 if($num =~ /(\d+)/){
	 	return 1;
	}
	else{
		print "<B>NUMBER $num</b>\n";
	  return 0;
	}
}#tnIsI 
 

sub reportError{
	$severity = $_[0];
	$errorText = $_[1];	
	
}
 
sub debug{
	if ($DEBUG == 1){	
		my $message = $_[0]; 
		print "$message\n\n";	
	}
}

sub printHeader{
	debug("printHeader($type);");
	$type = $_[0];
	$title = $_[1];
 
	
	print "Content-type: text/$type\n";
	print "Expires: 0\n";
	print qq{
 
	     <html>
		<head>
		<title>$title</title>
		<meta http-equiv="Cache-Control" content="no-cache" />
		<meta http-equiv="Cache-Control" content="no-store" />
		<meta http-equiv="Cache-Control" content="must-revalidate" />
		<meta http-equiv="Pragma" content="no-cache" />
		<META HTTP-EQUIV="expires" CONTENT="0" />

	<!-- DHTMLX GRID -->		
	<link rel="STYLESHEET" type="text/css" href="../dHTMLxGrid/codebase/dhtmlxgrid.css">
	<link rel="STYLESHEET" type="text/css" href="../dHTMLxGrid/codebase/dhtmlxgrid_skins.css">
	<link rel='STYLESHEET' type='text/css' href='../dHTMLxGrid/common/style.css'>
	<script  src="../dHTMLxGrid/codebase/dhtmlxcommon.js"></script>
	<script  src="../dHTMLxGrid/codebase/dhtmlxgrid.js"></script>		
	<script  src="../dHTMLxGrid/codebase/dhtmlxgridcell.js"></script>	
	<!-- END DHTMLX GRID -->	
		
		 <link rel='stylesheet' type='text/css' href='../styles/guava.css'>
		</link>
		<link href="../aw/grid.css" rel="stylesheet" type="text/css" ></link>
	<script src="../aw/grid.js" type='text/javascript' ></script>
		
		 <script language='JavaScript' src='reports.js' type='text/javascript' />
	 <script language='JavaScript'  type='text/javascript'>var listOpt= ''</script>
	  </head>
	 
		};
	
		
}

 

 

sub sendInvalidRequest
{
	print qq{
		<alarm>Invalid Request</alarm>
	};
}
1;
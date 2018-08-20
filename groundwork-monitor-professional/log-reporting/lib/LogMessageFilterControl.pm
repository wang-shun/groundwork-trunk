#!/usr/local/groundwork/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

package LogMessageFilterControl;
use lib qq(/usr/local/groundwork/log-reporting/lib);
use LogFileType;
use LogMessageType;
use ComponentType;
use Severity;

sub draw{
	$refresh = shift;
	
	## Print Header
unless($refresh){
print qq{

<head>

<meta http-equiv="CACHE-CONTROL" content="NO-CACHE" />
	<style> body, html {margin:0px; padding: 0px; overflow: auto;} </style>
	<!-- ActiveWidgets stylesheet and scripts -->
	<link href="../aw/grid.css" rel="stylesheet" type="text/css" ></link>
	<script src="../aw/grid.js"></script>
</head>
<body>
 <div id='lmfIFrame' style='overflow:auto;' >
 };

} 
	
 
#Only print title bar if not refreshing
	 	print qq{
			<table width=100% class='window'><tr><td>
			<table width=100% border=0  class='windowContent'>
			<tr>
			<td  colspan=5 class='windowHeader' width=100%'>Log Message Filters
			</td>
			</tr></table> 
 			</td></tr></table>
			};

	 
LogMessageFilterControl::defineGrid();
print "</div><div    width=400px id='LogMessageFilterForm'>";
LogMessageFilterControl::printControlForm();
print "</div>";
unless($refresh){
print "	<div style='overflow:auto;'  id=LogMessageFilterRefresh></div>
<div id=LogMessageFilter>";
	print "</div></div></body>";
}
 

} #END printLogMessageFilterCtlNEW


sub printControlForm{
	
#form part
@ltypes = LogFileType::getTypeList();
@mtypes = LogMessageType::getTypeListNEW();

 
print qq{ 	 
	<form name=messageFilterForm>
 	<input type=hidden name=parsingRuleID></input>
	<table cellpadding=5 cellspacing=15 border=0><tr><td>
	<table  >
	<tr>
	<td>Filter Name:</td>
 	<td><input size=40 type=text name=filterName value=></input></td>
 	</tr>
 	
 	<tr>
 	<td>Filter (regular expression):</td>
 	<td>
 	<input size=40 type=text name=regex value=></input>
 	<a onclick="javascript:pop('/log-reporting/bin/ControlPanelSrv.pl?control=RegexTester',450,350);"> <img src='../images/arrow.gif' width=10></a>
 	 
 	</td>
 	</tr>
 	
 	<tr>
 	<td>Message Type</td>
 	<td>
 	};
 	print "  <select name='messageTypes'><option value=''>&lt;&lt;select&gt;&gt;</option>";
foreach $mt (@mtypes){
	my ($id,$tname) = split(/ZZZ/,$mt);
	print "<option value='$tname'>$tname</option>";
	}
 	
 	print qq{
 	 </select>
 	</td>
 	</tr>
 	
 	<tr>
 	<td>File Type</td>
 	<td>
 	<select name='logTypes' >
 	<option value=''>&lt;&lt;select&gt;&gt;</option>
 	
 	};
 	
foreach $type (@ltypes){
	my ($id,$tname) = split(/ZZZ/,$type);
	print "<option value='$tname'>$tname</option>";	
}

print qq{
 	</select>
 	
 	</td>
 	</tr>
 	};

@severities = Severity::getSeverities();
print qq{
	<tr>
	<td>Severity</td>
	<td>
	<select name='severity'>
	};
	
foreach $sev(@severities){
	print "<option value='$sev'>$sev</option>";	
	}
print qq{	
	</select>
	</td>
	</tr>
}; 	
 	
 print qq{
 	<tr>
 	<td>
 	Is Enabled?
 	</td>
 	<td>
 	<select name='isEnabled'>
 	<option value='Yes'>Yes</option>
 	<option value='No'>No</option>
 	</select>
 	</td>
 	</tr>
	</table>
	</td>
	<td valign=top>
	Components<br>
	};

print qq{
 <table>
 <tr>
 <td >	
<select size=5 name=myComponents></select> 
</td>
<td>
<input type=button value='>>' onclick="javascript:removeComponent();"><br>
<input type=button value='<<' onclick="javascript:addComponent();">

</td>
<td valign=top>
 
};
## LIST ALL COMPONENTS
@components = ComponentType::getTypeList();
print "<select size=5 name=AllComponents>";
foreach $c (@components){
		print "<option value=$c>$c</option>";
		}
print "</select>";

print "</td></tr></table>";
	
	print"</td>
	</tr>
	</table></td></tr></table>";
	
	
 

## CONTROL BUTTONS
print qq{	
	<P>
<div name='warning' id='warning'>.</div>
<div align='left'>
<input type=button value='Reset' onclick="javascript:getlistOpt();sendDataReq('LogMessageFilterForm','refresh',listOpt);">
<input value='Save' type='button' onclick="javascript:getlistOpt();sendDataReq('LogMessageFilterForm','save',listOpt);;window.setTimeout('refreshGrid()',175);"></input> 
<input value='Delete' type='button' onclick="javascript:getlistOpt();sendDataReq('LogMessageFilterForm','del',listOpt);window.setTimeout('refreshGrid()',175);"></input> 
 </div>
	</form>
	 
	};
}


sub defineGrid{    
	
	print qq{
		<!-- grid format -->        <style>
                .aw-grid-control {height: 200px; width: 85%; border: none; font: menu;}

                .aw-column-0 {width:  80px;}
                .aw-column-1 {width: 200px; background-color: threedlightshadow;}
                .aw-column-2 {text-align: right;}
                .aw-column-3 {text-align: right;}
                .aw-column-4 {text-align: right;}

                .aw-grid-cell {border-right: 1px solid threedshadow;}
                .aw-grid-row {border-bottom: 1px solid threedlightshadow;}
        </style>
	
	<style>
		.active-controls-grid { height: 200px; width: 98%; font: menu;}
		.active-column-0 {width: 55px;}
		.active-column-1 {width:  80px;}
		.active-column-2 {width: 80px;}
		.active-column-3 {text-align: left;width:200px;}
		.active-column-4 {width: 80px;}
		.active-column-5 {width: 100px;} 
		.active-column-6 {width: 200px;}
		.active-column-7 {width: 100px;}

.active-scroll-left
    { 
       display: none; 
    }  	 
      
	

		.active-grid-column {border-right: 1px solid threedlightshadow;}
		.active-grid-row {border-bottom: 1px solid threedlightshadow;}
	</style>

	<!-- grid data -->
			<script language=JavaScript>
};  
 
	print qq{
 
		
	  //      create ActiveWidgets data model - XML-based table
        table = new Active.XML.Table;

        //      provide data URL
   		var randomNumber = Math.round(Math.random()*999999999999);
        table.setURL("/log-reporting/bin/reports.pl?struct=LogMessageFilter&action=listXML&nocache=" + randomNumber);

        //      start asyncronous data retrieval
        table.request();

        //      define column labels
	var columns = [
			"Enabled","File Type", "Message Type", "Filter Name","Severity","Rule ID","Filter (regular expression)","Components"
		];
	


 	obj = new Active.Controls.Grid;
    // provide column labels
    obj.setColumnProperty("texts", columns);

    // provide external model as a grid data source
    obj.setDataModel(table);
    obj.setColumnValues([0,1,2,3,4,6,7]);
 
    
    //set onclick
    obj.setAction("click", function(src){
    var row = src.getRowProperty("index");
    var count = this.getColumnProperty("count");
    var components = this.getDataProperty("text",row,7);
    var regex = this.getDataProperty("text", row, 6);
    var name = this.getDataProperty("text",row,3);
    var mType = this.getDataProperty("text",row,2);
    var fType = this.getDataProperty("text",row,1);
    var enabledStatus = this.getDataProperty("text",row,0);
    var parsingRuleID = this.getDataProperty("text",row,5);
    var severity = this.getDataProperty("text",row,4);
    
   	document.forms['messageFilterForm'].isEnabled.value = enabledStatus;
    document.forms['messageFilterForm'].parsingRuleID.value = parsingRuleID;
    document.forms['messageFilterForm'].regex.value = regex;
    document.forms['messageFilterForm'].filterName.value = name; 
    document.forms['messageFilterForm'].severity.value = severity;
    document.getElementById('warning').innerHTML = "<B><font color=red>Warning: Deleting a Log Message Filter may Orphan Log Messages in your Database!</font><P/></b>";
    selectValueSet('messageTypes',mType);
  	selectValueSet('logTypes',fType);
  
   if(components.length>0){
    var componentArray= components.split(",");
    setComponentOptions(componentArray);
    }
   else{
   var empty;
   setComponentOptions(empty);
   }
    
 
}); 
	// write grid html to the page
    document.write(obj); 
	};
	
	 
	
print qq{
	 </script>
</head>
<body>

};
	
} #end define grid

1;

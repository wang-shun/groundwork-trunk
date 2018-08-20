#!/usr/local/groundwork/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

package ComponentTypeControl;
use ComponentType;
sub draw{
	 
@ltypes = ComponentType::getTypeList();

print qq{
 <reportMessage>
 <htmlData id='htmlData'>
 <div id='ComponentType'>
 <!--Log Directory Control -->
 	<table table id="logReporting" border="0" cellpadding="0" cellspacing="0" > 
 
	<tr   class='windowHeader'><td  class='windowHeader' colspan='2'>Component Types</td></tr>
<tr>
<td>
 <!-- form name='ComponentTypeForm'   METHOD='post' -->
 <input type='hidden' name='struct' value='ComponentType'></input>
 <input type='hidden' name='action' value='add'></input>
 <table>
 <tr>
 <td><input type='text' onkeypress="check_submit(event,this.form,'ComponentType','add',document.getElementById('newComponentType').value);" id='newComponentType' name='newComponentType' size='20'></input>
 <input value='Add' type='button' onclick="javascript:sendDataReq('ComponentType','add',document.getElementById('newComponentType').value);"></input></td></tr>
 <tr>
 <td>
   </td></tr></table> <!--- why? -->
 <!-- /form -->
 <form name='ComponentTypeDel'   method='post'>
 <select style='width: 125px;' size='5' name='selection'>
 };
foreach $type (@ltypes){
	print "<option value='$type'>$type</option>";	
	
}
print "</select>";
 
 print qq{
</select>
<input value='Delete' type='button' onclick="javascript:sendDataReq('ComponentType','del',document.forms.ComponentTypeDel.selection.value);"></input></form></td></tr>
</table>
 
</div>
</htmlData>
</reportMessage>
 };
}

1;
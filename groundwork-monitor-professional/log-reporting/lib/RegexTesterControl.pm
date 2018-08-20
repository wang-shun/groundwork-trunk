#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#
package RegexTesterControl;


sub draw{

print qq{
     <form onsubmit="sendDataReq('RegexTester','test',(document.RegexTesterForm.regex.value + 'ZZZZ' + document.RegexTesterForm.testString.value));" name='RegexTesterForm'>
  	 <table   id="logReporting" border="0" cellpadding="0" cellspacing="0">
	 	 
			<tr  class='windowHeader'>
			<td   class='windowHeader' colspan='2'>Regular Expression Tester
			</td>
			</tr>
			
			<tr>
			<td>
			
			
			<table>
			<tr>
			<td>regex:</td><td><input size=60 type=text name=regex></td>
			</tr>
			<tr>
			<td>Test String</td><td><input size=60 type=text name=testString></td>
			</tr>
			</table>
			<input type='button' value='Test' onclick="sendDataReq('RegexTester','test',(document.RegexTesterForm.regex.value + 'ZZZZ' + document.RegexTesterForm.testString.value));">
			
			
			
			</td>
			</tr>
		</table>
		</form>
		<P>
		<div id="RegexTester"></div>
   
 
};

}

sub updateStatus{
  $matchRef = shift;
  @myMatches = @$matchRef;
  $matchCnt = @myMatches;
  if($matchCnt > 0){
   print "<table width=100%><tr><td colspan=2 bgcolor=00FF00 align=center>Matched</td></tr>";
   $cnt=1;
   foreach $m(@myMatches){
     print "<tr><td>Component $cnt</td><td>$m</td></tr>";
     $cnt++;
   }
   
   print "</table>";
   
  }
  else{
  print "<table width=100%><tr><td bgcolor=FF0000 align=center>NO Match</td></tr></table>";
  }
 
}
1;
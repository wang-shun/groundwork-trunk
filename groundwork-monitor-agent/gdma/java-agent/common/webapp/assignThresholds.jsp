<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    <%@ page import="java.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Groundwork @appserver_camelcase@ JDMA</title>
<script type="text/javascript" src="js/java-agent.js"></script>
<link type="text/css" href="css/java-agent.css" rel="Stylesheet" />
<style type="text/css" title="currentStyle">
@import "css/jdma_page.css";
@import "css/jdma_table.css";
</style>
<script type="text/javascript" language="javascript" src="js/jquery.js"></script>
<script type="text/javascript" language="javascript" src="js/jquery.dataTables.js"></script>
<script type="text/javascript" charset="utf-8">
function validateOnSubmit()
{
	var cache = new Object();
	for (var rowid = 0; rowid < document.assignForm.alias.length; rowid++)
    {
        if (document.assignForm.alias[rowid].value =='' || document.assignForm.critThreshold[rowid].value == '' || document.assignForm.warnThreshold[rowid].value == '')
        {
                alert ('Please enter a valid alias, critical threshold and warning threshold for ' + document.assignForm.alias[rowid].value);
                return false;
        }
        else
        {
            var str = document.assignForm.alias[rowid].value;
            if (cache[str]) {
                /* DUPLICATE FOUND */
                    alert ('Duplicate alias found.Please enter a valid unique alias!' );
                    return false;
            }
            else
            {
                    if (!isSpclChar(str, document.assignForm.alias[rowid].value))
                    {
                            return false;
                    } // end if
                    if (!isNumeric(document.assignForm.critThreshold[rowid].value,document.assignForm.alias[rowid].value) || !isNumeric(document.assignForm.warnThreshold[rowid].value,document.assignForm.alias[rowid].value))
                    {
                            return false;
                    } // end if
            } // end if/else
            cache[str]=true;
        }  // end if
	    
    } // emd for
     return true;
}
function isSpclChar(fieldval, disp){ 
	var iChars = "!@#$%^&*+=-[]\\\;,/{}|\":<>?  ()"; 
	for (var i = 0; i < fieldval.length; i++) {        	
		if (iChars.indexOf(fieldval.charAt(i)) != -1) 
		{ 
	    	alert ("The '"+disp+"' alias field has special characters. \nOnly (.) and (_) are allowed.\n"); 
	        return false; 
		} 
	}
	return true; 
}   
function isNumeric(sText, disp)

{
   var ValidChars = "0123456789";
   var IsNumber=true;
   var Char;

 
   for (i = 0; i < sText.length && IsNumber == true; i++) 
      { 
      Char = sText.charAt(i); 
      if (ValidChars.indexOf(Char) == -1) 
         {
    	  	alert ('Invalid warning or critical threshold for ' + disp + '. Only numerics are allowed !'); 
       		IsNumber = false;
         }
      }
   return IsNumber;
   
   }


</script>
</head>
<body>
<form name="assignForm"
	ACTION="/gwos-@appserver_shortname@-monitoringAgent/GWOS@appserver_camelcase@Servlet"
	method="post" onsubmit="return validateOnSubmit();">
	<p align="center"><img align="top" src="images/gwlogo.gif"></p>
<p align="center">JDMA for GWOS@appserver_camelcase@</p>
<div id="controlbg">
<div class="controltop">
<div class="cornerul"></div>
<div class="cornerur"></div>
</div>
<div class="controlheader">Assign alias & thresholds</div>
<div class="controlcontent">
<p>
<div id="container">
<table cellpadding="0" cellspacing="0" border="0" class="display" id="example" style="width:auto;">
	<thead>
		
		<tr>
			<th align="left">Attribute</th>
			<th align="left">Alias(max 255 chars. no special chars. periods
			allowed)</th>
			<th align="left">Warning Threshold</th>
			<th align="left">Critical Threshold</th>
		</tr>
	</thead>
	<tbody>	

		<%
			Object objServices = request.getSession().getAttribute("selectedComponents");
			
			if (objServices != null) {
				List<String> services = (List) objServices;
				
				
				for (String service : services) {
					
		%><tr>
			<td><%=service%></td>
			<td><input type="text" name="alias" id="alias" size="50"
				maxlength="255" value="<%=service%>" class="text"/></td>
			<td><input type="text" name="warnThreshold" id="warnThreshold"
				size="10" maxlength="10" value="5" class="text"/></td>
			<td><input type="text" name="critThreshold" id="critThreshold"
				size="10" maxlength="10" value="10" class="text"/></td>
		</tr>
		<%
				
				} // end for
			} // end if
		%>

	</tbody>
</table>
</div>
</p>
</div>
<div class="controlbottom">
<div class="cornerll"></div>
<div class="cornerlr"></div>
</div>
</div>

<p align="center"><input type="button" value="Home"
	onclick="location.href='index.html'" class="button"/><INPUT VALUE="Next"
	TYPE="SUBMIT" class="button"></p>
<input type="hidden" name="action" value="create_from_ui_assign_page" />
</form>
</body>
</html>
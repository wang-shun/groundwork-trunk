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
$(document).ready(function() {
	$('#example').dataTable( {
		"sScrollY": "400px",
		"bPaginate": false
	} );
} );
		</script>
<script type="text/javascript">
function checkAll(flag)
{
   if ( document.selectForm.services.length )
   {
      for (var x = 0; x < document.selectForm.services.length; x++)
      {
         if (flag == 1)
         {
            document.selectForm.services[x].checked = true;  
            
         }
         else
         {
            document.selectForm.services[x].checked = false;
            
         }
         
      }
   }
   else
   {
      if (flag == 1)
      {
         document.selectForm.services.checked = true; 
         
      }
      else
      {
         document.selectForm.services.checked = false;
         
      }
   }
}

function validateOnSubmit()
{
	var selectedComps = 0;
	for (var j = 0; j < document.selectForm.services.length; j++)
    {
		if (document.selectForm.services[j].checked)
	    {
			selectedComps = selectedComps + 1;
	    }		
	} // end for
	if (selectedComps == 0)
	{
		alert ('Please select atleast one mbean attribute!');
	    return false;
	} // end if
}
</script>
</head>
<body id="dt_example">

<form name="selectForm"
	ACTION="/gwos-@appserver_shortname@-monitoringAgent/GWOS@appserver_camelcase@Servlet"
	method="post" onsubmit="return validateOnSubmit();">
<p align="center"><img align="top" src="images/gwlogo.gif"></p>
<p align="center">JDMA for @appserver_camelcase@</p>
<div id="controlbg">
<div class="controltop">
<div class="cornerul"></div>
<div class="cornerur"></div>
</div>
<div class="controlheader">Select Mbean Attributes</div>
<div class="controlcontent">
<p>
<div id="container">
<table cellpadding="0" cellspacing="0" border="0" class="display" id="example" style="width:auto;">
	<thead>
		<tr>
			<th align="left" colspan="1"><a href="#" onclick="checkAll(1);">Check
			All</a> | <a href="#" onclick="checkAll(0);">UnCheck All</a></th>
		</tr>	
		<tr>
			<th align="left">Attribute</th>
			
		</tr>
	</thead>
	<tbody>	

		<%
			Object objServices = request.getAttribute("services");
			
			if (objServices != null) {
				List<String> services = (List) objServices;
				
				
				for (String service : services) {
					
		%><tr>
			<td><input type="checkbox" name="services" value="<%=service%>"
				id="services"> <%=service%></td>			
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

<p align="center"><input type="button" value="Back"
	onclick="location.href='newConnectionWizard.jsp'" class="button"/><INPUT VALUE="Next"
	TYPE="SUBMIT" class="button"></p>

<input type="hidden" name="action" value="create_from_ui_select_page" />



</form>

</body>
</html>

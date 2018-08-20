<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>CloudHub for @virt_target_label@</title>
<script type="text/javascript" src="js/java-agent.js"></script>
<link type="text/css" href="css/java-agent.css" rel="Stylesheet" />
<style type="text/css" title="currentStyle">
@import "css/jdma_page.css";

@import "css/jdma_table.css";
</style>
<script type="text/javascript" language="javascript" src="js/jquery.js"></script>
<script type="text/javascript" language="javascript"
	src="js/jquery.dataTables.js"></script>
<script type="text/javascript" charset="utf-8">
	$(document).ready(function() {
		$('#services').dataTable({
			"sScrollY" : "400px",
			"bPaginate" : false
		});
	});
</script>
<script type="text/javascript">
	function checkAll(flag) {
		if (document.selectForm.services.length) {
			for ( var x = 0; x < document.selectForm.services.length; x++) {
				document.selectForm.services[x].checked = (flag == 1);
			}
		} else {
			document.selectForm.services.checked = (flag == 1);
		}
	}

	function validateOnSubmit() {
		var selectedComps = 0;
		for ( var x = 0; x < document.selectForm.services.length; x++) {
			if (document.selectForm.services[x].checked) {
				selectedComps += 1;
			}
		}
		if (selectedComps == 0) {
			alert('Please select atleast one attribute to monitor!');
			return false;
		}
		return true;
	}
</script>
</head>
<body id="dt_example">

	<form name="selectForm"
		ACTION="/@virt_agent_name@/@virt_target@Servlet" method="post"
		onsubmit="return validateOnSubmit();">
		<div id="container">
			@virt_header@
			<p align="center" class="agent_title">CloudHub Configuration wizard
				for @virt_target_label@</p>
			<div id="controlbg">
				<div class="controltop">
					<div class="cornerul"></div>
					<div class="cornerur"></div>
				</div>
				<div class="controlheader">Select your Virtual Machine
					Attributes to monitor</div>
				<div class="controlcontent">

					<table cellpadding="0" cellspacing="0" border="0" class="display"
						id="services">
						<thead>
							<!-- <tr>
								<th align="left" colspan="1"><a href="#"
									onclick="checkAll(1);">Check All</a> | <a href="#"
									onclick="checkAll(0);">UnCheck All</a></th>
							</tr> -->
							<tr>
								<th align="left">Attribute</th>

							</tr>
						</thead>
						<tbody>

							<%
								List<String> services = (List<String>)request.getAttribute("services");
								if (services != null)
								{
									for (String service : services)
									{
							%><tr>
								<td><input type="checkbox" name="services"
									value="<%=service%>" id="services"> <%=service%></td>
							</tr>
							<%
									} // end for
								} // end if
							%>

						</tbody>
					</table>
					<table cellpadding="0" cellspacing="0" border="0" class="display">
						<tr>
							<td align="right"><input type="button" value="Back"
								onclick="location.href='newConnectionWizard.jsp'" class="button" /></td>
							<td align="left"><INPUT VALUE="Next" TYPE="SUBMIT"
								class="button" /></td>
						</tr>
					</table>

				</div>
				<input type="hidden" name="action"
					value="create_from_ui_select_page" />
			</div>
			<div class="controlbottom">
				<div class="cornerll"></div>
				<div class="cornerlr"></div>
			</div>
		</div>
	</form>
</body>
</html>

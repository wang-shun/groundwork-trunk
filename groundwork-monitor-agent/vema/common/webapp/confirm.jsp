<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<HTML>
<HEAD>
<title>CloudHub for @virt_target_label@</title>
<script type="text/javascript" src="js/java-agent.js"></script>
<link type="text/css" href="css/java-agent.css" rel="Stylesheet" />
</HEAD>
<BODY>
	@virt_header@
	
	<p align="center" class="agent_title">CloudHub for @virt_target_label@</p>
	<div class="sidebox">
		<div class="boxhead">
			<h2>Confirmation</h2>
		</div>
		<div class="boxbody">
			<%
				Object messageObj = request.getAttribute("message");
				String message = (messageObj == null) ? "" : (String) messageObj;
			%>
			<table>
				<tr>
					<td><img alt="" src="images/success-icon.png" height="50px" width="50px"></td>
					<td>Configuration saved successfully. CloudHub for
						@virt_target_label@ is now ready to send check results to Groundwork server</td>
				</tr>
				<tr>
					<td colspan="2" align="center"><input type="button" value="Home"
						onclick="location.href='index.html'" class="button" /></td>
				</tr>
			</table>
		</div>
	</div>
</BODY>
</HTML>

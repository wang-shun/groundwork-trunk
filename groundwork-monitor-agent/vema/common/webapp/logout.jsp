<HTML>
<HEAD>
<title>Groundwork @virt_target_label@ CloudHub</title>
<script type="text/javascript" src="js/java-agent.js"></script>
<link type="text/css" href="css/java-agent.css" rel="Stylesheet" />
</HEAD>
<BODY>
	<form>
		<p align="center">		<img align="top" src="images/logo.png"></p>
		<div class="sidebox">
			<div class="boxhead">
				<h2>CloudHub Logout Successful</h2>
			</div>
			<div class="boxbody">
			<p align="center">
			<%session.invalidate();%>
				<table>
					<tr>
						<td colspan="3">You have logged out. Please <a href="index.html"><b>Login</b></a> !</td>
					</tr>					
				</table>	
				</p>			
			</div>
		</div>
	</FORM>
</BODY>
</HTML>





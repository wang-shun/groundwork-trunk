<%@page import="org.itgroundwork.foundation.joxbeans.Metric"%>
<%@page import="org.itgroundwork.foundation.joxbeans.VM"%>
<%@page import="org.itgroundwork.foundation.joxbeans.Hypervisor"%>
<%@page import="org.itgroundwork.foundation.joxbeans.VemaMonitoring"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
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
<script type="text/javascript" language="javascript" src="js/jquery.validate.min.js"></script>
<script type="text/javascript" language="javascript" src="js/jquery.dataTables.js"></script>

<script>
	$(document).ready(function() {
		$("#assignForm").validate();
	});
</script>

<script type="text/javascript">
function hypOnChangeCheckbox (checkbox) {
	var id = checkbox.id;
 	var index = id.substring(id.lastIndexOf("_")+1,id.length);
	document.getElementById('hyp_graphed_'+index).checked = 
		(checkbox.checked ? 'checked' : false);
}
function vmOnChangeCheckbox (checkbox) {
	var id = checkbox.id;
 	var index = id.substring(id.lastIndexOf("_")+1,id.length);
	document.getElementById('vm_graphed_'+index).checked = 
		(checkbox.checked ? 'checked' : false);
}
</script>
</head>
<body id="dt_example">
	<form name="assignForm" id="assignForm"
		ACTION="/@virt_agent_name@/@virt_target@Servlet" method="post">
		<div id="container">
			@virt_header@
			<p align="center" class="agent_title">CloudHub Configuration wizard
				for @virt_target_label@</p>
			<jsp:useBean id="vemaBean"
				class="org.itgroundwork.foundation.joxbeans.VemaMonitoring"
				scope="session" />
			<jsp:setProperty name="vemaBean" property="*" />
			<div id="controlbg">
				<div class="controltop">
					<div class="cornerul"></div>
					<div class="cornerur"></div>
				</div>
				<div class="controlheader">Hypervisor thresholds</div>
				<div class="controlcontent">
					<table cellpadding="0" cellspacing="0" border="0" class="display"
						id="example" style="width: auto;">
						<thead>
							<tr>
								<th align="left">Attribute</th>
								<th align="left">Monitored ?</th>
								<th align="left">Graphed ?</th>
								<th align="left">Warning Threshold</th>
								<th align="left">Critical Threshold</th>
							</tr>
						</thead>
						<tbody>
							<c:forEach items="${vemaBean.hypervisor.metric}" var="metric"
								varStatus="rowCounter">
								<tr>
									<td>${metric.name} <input type="hidden" name="hyp_alias"
										value="${metric.name}" /></td>
									<c:if test="${metric.monitored == true}">
										<td><input type="checkbox"
											id="hyp_monitored_${rowCounter.count}" name="hyp_monitored"
											checked="checked" value="${metric.name}" onchange="hypOnChangeCheckbox(this)"/></td>
									</c:if>
									<c:if test="${metric.monitored == false}">
										<td><input type="checkbox"
											id="hyp_monitored_${rowCounter.count}" name="hyp_monitored"
											value="${metric.name}" onchange="hypOnChangeCheckbox(this)"/></td>
									</c:if>
									<c:if test="${metric.graphed == false}">
										<td><input type="checkbox"
											id="hyp_graphed_${rowCounter.count}" name="hyp_graphed"
											value="${metric.name}" /></td>
									</c:if>
									<c:if test="${metric.graphed == true}">
										<td><input type="checkbox"
											id="hyp_graphed_${rowCounter.count}" name="hyp_graphed"
											checked="checked" value="${metric.name}" /></td>
									</c:if>
									<td><input type="text"
										id="hyp_warningThreshold_${rowCounter.count}"
										name="hyp_warningThreshold_${rowCounter.count}" size="10"
										maxlength="10" class="text required number"
										value="${metric.warningThreshold}" /></td>
									<td><input type="text"
										id="hyp_criticalThreshold_${rowCounter.count}"
										name="hyp_criticalThreshold_${rowCounter.count}" size="10"
										maxlength="10" class="text required number"
										value="${metric.criticalThreshold}" /></td>
								</tr>
							</c:forEach>
						</tbody>
					</table>

				</div>
				<div class="controlbottom">
					<div class="cornerll"></div>
					<div class="cornerlr"></div>
				</div>
			</div>
			<br>
			<div id="controlbg">
				<div class="controltop">
					<div class="cornerul"></div>
					<div class="cornerur"></div>
				</div>
				<div class="controlheader">Virtual Machine thresholds</div>
				<div class="controlcontent">
					<table cellpadding="0" cellspacing="0" border="0" class="display"
						id="example" style="width: auto;">
						<thead>
							<tr>
								<th align="left">Attribute</th>
								<th align="left">Monitored ?</th>
								<th align="left">Graphed ?</th>
								<th align="left">Warning Threshold</th>
								<th align="left">Critical Threshold</th>
							</tr>
						</thead>
						<tbody>
							<c:forEach items="${vemaBean.vm.metric}" var="metric"
								varStatus="rowCounter">
								<tr>
									<td>${metric.name} <input type="hidden" name="vm_alias"
										value="${metric.name}" /></td>
									<c:if test="${metric.monitored == true}">
										<td><input type="checkbox"
											id="vm_monitored_${rowCounter.count}" name="vm_monitored"
											checked="checked" value="${metric.name}" onchange="vmOnChangeCheckbox(this)"/></td>
									</c:if>
									<c:if test="${metric.monitored == false}">
										<td><input type="checkbox"
											id="vm_monitored_${rowCounter.count}" name="vm_monitored"
											value="${metric.name}" onchange="vmOnChangeCheckbox(this)"/></td>
									</c:if>
									<c:if test="${metric.graphed == false}">
										<td><input type="checkbox"
											id="vm_graphed_${rowCounter.count}" name="vm_graphed"
											value="${metric.name}" /></td>
									</c:if>
									<c:if test="${metric.graphed == true}">
										<td><input type="checkbox"
											id="vm_graphed_${rowCounter.count}" name="vm_graphed"
											checked="checked" value="${metric.name}" /></td>
									</c:if>
									<td><input type="text"
										id="vm_warningThreshold_${rowCounter.count}"
										name="vm_warningThreshold_${rowCounter.count}" size="10"
										maxlength="10" class="text required number"
										value="${metric.warningThreshold}" /></td>
									<td><input type="text"
										id="vm_criticalThreshold_${rowCounter.count}"
										name="vm_criticalThreshold_${rowCounter.count}" size="10"
										maxlength="10" class="text required number"
										value="${metric.criticalThreshold}" /></td>
								</tr>
							</c:forEach>
						</tbody>
					</table>
					<table cellpadding="0" cellspacing="0" border="0" class="display">
						<tr>
							<td align="right"><input type="button" value="Home"
								onclick="location.href='index.html'" class="button" /></td>
							<td align="left"><INPUT VALUE="Save" TYPE="SUBMIT"
								class="button" /></td>
						</tr>
					</table>
					<input type="hidden" name="action"
						value="create_from_ui_assign_page" />
				</div>
				<div class="controlbottom">
					<div class="cornerll"></div>
					<div class="cornerlr"></div>
				</div>
			</div>
		</div>
	</form>
</body>
</html>

<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>CloudHub for Docker</title>
<script type="text/javascript" src="/cloudhub/resources/js/java-agent.js"></script>
<link type="text/css" href="/cloudhub/resources/css/java-agent.css" rel="Stylesheet" />
<style type="text/css" title="currentStyle">
@import "/cloudhub/resources/css/jdma_page.css";
@import "/cloudhub/resources/css/jdma_table.css";
</style>

<script type="text/javascript" language="javascript" src="/cloudhub/resources/js/jquery.js"></script>
<script type="text/javascript" language="javascript" src="/cloudhub/resources/js/jquery.dataTables.js"></script>
<script type="text/javascript" src="/cloudhub/resources/js/common.js"></script>
<script type="text/javascript">
	$(function() {
		initCustomNameValidation(true);
		var formModified = false;
		$("form").bind('change', function() {
			formModified = true;
		});
		$(window).bind('beforeunload', function(e) {
			if (formModified) {
				$("body").css("cursor", "default");
				return 'Modifications have been made to the form without saving.';
			}
			return;
		});
		$("#btn-save").bind('click', function(e) {
			formModified = false;
		});
	});
</script>
</head>
<body id="dt_example">
	<form:form name="assignForm" id="assignForm" modelAttribute="profileBean" method="POST" action="/cloudhub/mvc/docker2/saveConnectionProfile">
        <c:url value="/mvc/updateConfiguration" var="updateConfigURL">
            <c:param name="filePath" value="${profileBean.configFilePath}"/>
            <c:param name="fileName" value="${profileBean.configFileName}"/>
        </c:url>

		<div id="container">
			
			<p align="center" class="agent_title">Cloud Hub Configuration wizard for Docker</p>

			<div id="messages" class="message">
				<div id="testConResultMsg">

					<c:if test="${result == 'remoteProfileDoesNotExists'}">
						<p><font size="3" color="red">Remote ground work profile could not be accessed. These are only the metrics from the local ground work profile. Please check if you have proper rights and credentials!</font></p>
					</c:if>
	
					<c:if test="${result == 'success'}">
						<p><font size="3">Docker profile updated successfully.</font></p>
					</c:if>
	
					<c:if test="${result == 'savefailure'}">
						<p><font size="3" color="red">Sorry some problem occurred in saving Docker profile</font></p>
					</c:if>

                    <c:if test="${result == 'readfailure'}">
                        <p><font size="3" color="red">Sorry some problem occurred in reading Docker profile</font></p>
                    </c:if>

                </div>
			</div>
			
			<div id="controlbg">
				<div class="controltop">
					<div class="cornerul"></div>
					<div class="cornerur"></div>
				</div>
				<div class="controlheader heading3">Docker Engine Thresholds</div>
				<div class="controlcontent notlast">
					<table cellpadding="0" cellspacing="0" border="0" class="display"
						id="example">
						<thead>
							<tr>
								<th class="tableHeader1 left">Attribute</th>
								<th class="tableHeader2 center">Monitored </th>
								<th class="tableHeader3 center">Graphed </th>
								<th class="tableHeader4 left">Warning Threshold</th>
								<th class="tableHeader5 left">Critical Threshold</th>
								<th class="tableHeader6 left">Service Name</th>
								<th class="tableHeader7 left">Description</th>
							</tr>
						</thead>
						<tbody>
							<c:forEach items="${profileBean.engineMetrics}" var="metric"
								varStatus="rowCounter">
								<tr>
									<td class="tableRow1">
										${metric.name}
										<form:hidden path="engineMetrics[${rowCounter.index}].name" value="${metric.name}"/>
									</td>
									<td class="tableRow2 center">
										<form:checkbox id="hyp_monitored_${rowCounter.count}" name="hyp_monitored" path="engineMetrics[${rowCounter.index}].monitored" onchange="hypMonitorOnChangeCheckbox(this)"/>
									</td>
	
									
									<td class="tableRow3 center">
										<form:checkbox id="hyp_graphed_${rowCounter.count}" name="hyp_graphed" path="engineMetrics[${rowCounter.index}].graphed" onchange="hypGraphOnChangeCheckbox(this)"/>
									</td>

									<td class="tableRow4">
										<form:input type="text"
										id="hyp_warningThreshold_${rowCounter.count}"
										name="hyp_warningThreshold_${rowCounter.count}" size="10"
										maxlength="16" class="text required number"
										path="engineMetrics[${rowCounter.index}].uiWarningThreshold" value="${metric.uiWarningThreshold}"/>
										<font color='red'><form:errors path='engineMetrics[${rowCounter.index}].uiWarningThreshold' /></font>
									</td>
									<td class="tableRow5">
										<form:input type="text"
										id="hyp_criticalThreshold_${rowCounter.count}"
										name="hyp_criticalThreshold_${rowCounter.count}" size="10"
										maxlength="16" class="text required number"
										path="engineMetrics[${rowCounter.index}].uiCriticalThreshold" value="${metric.uiCriticalThreshold}"/>
										<font color='red'><form:errors path='engineMetrics[${rowCounter.index}].uiCriticalThreshold' /></font>
									</td>
									<td class="tableRow5b">
										<form:input type="text" id="engine_customName_${rowCounter.count}" class="customName" path="engineMetrics[${rowCounter.index}].customName" value="${metric.customName}" size="15" maxlength="60"/>
										<font color='red'><span id='engine_customName_${rowCounter.count}_error' /></font>
									</td>
									<td class="tableRow6">
                                        ${metric.description}
                                        <form:hidden path="engineMetrics[${rowCounter.index}].description" value="${metric.description}"/>
                                    </td>
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
			<div id="controlbg">
				<div class="controltop">
					<div class="cornerul"></div>
					<div class="cornerur"></div>
				</div>
				<div class="controlheader heading3">Docker Container Thresholds</div>
				<div class="controlcontent">
					<table cellpadding="0" cellspacing="0" border="0" class="display"
						id="example">
						<thead>
							<tr>
								<th class="tableHeader1 left">Attribute</th>
								<th class="tableHeader2 center">Monitored </th>
								<th class="tableHeader3 center">Graphed </th>
								<th class="tableHeader4 left">Warning Threshold</th>
								<th class="tableHeader5 left">Critical Threshold</th>
								<th class="tableHeader6 left">Service Name</th>
								<th class="tableHeader7 left">Description</th>
							</tr>
						</thead>
						<tbody>
						
							<c:forEach items="${profileBean.containerMetrics}" var="metric"
								varStatus="rowCounter">
								<tr>
									<td class="tableRow1">
										${metric.name}
										<form:hidden path="containerMetrics[${rowCounter.index}].name" value="${metric.name}"/>
									</td>
									<td class="tableRow2 center">	
										<form:checkbox id="vm_monitored_${rowCounter.count}" name="vm_monitored" path="containerMetrics[${rowCounter.index}].monitored" onchange="vmMonitorOnChangeCheckbox(this)"/>
									</td>	

									<td class="tableRow3 center">
										<form:checkbox id="vm_graphed_${rowCounter.count}" name="vm_graphed" path="containerMetrics[${rowCounter.index}].graphed" onchange="vmGraphOnChangeCheckbox(this)"/>
									</td>

									<td class="tableRow4">
										<form:input type="text"
										id="vm_warningThreshold_${rowCounter.count}"
										name="vm_warningThreshold_${rowCounter.count}" size="10"
										maxlength="16" class="text required number"
										path="containerMetrics[${rowCounter.index}].uiWarningThreshold" value="${metric.uiWarningThreshold}"/>
										<font color='red'><form:errors path='containerMetrics[${rowCounter.index}].uiWarningThreshold' /></font>
									</td>
									<td class="tableRow5">
										<form:input type="text"
										id="vm_criticalThreshold_${rowCounter.count}"
										name="vm_criticalThreshold_${rowCounter.count}" size="10"
										maxlength="16" class="text required number"
										path="containerMetrics[${rowCounter.index}].uiCriticalThreshold" value="${metric.uiCriticalThreshold}"/>
										<font color='red'><form:errors path='containerMetrics[${rowCounter.index}].uiCriticalThreshold' /></font>
									</td>
									<td class="tableRow5b">
										<form:input type="text" id="container_customName_${rowCounter.count}" class="customName" path="containerMetrics[${rowCounter.index}].customName" value="${metric.customName}" size="15" maxlength="60"/>
										<font color='red'><span id='container_customName_${rowCounter.count}_error' /></font>
									</td>
                                    <td class="tableRow6">
                                            ${metric.description}
                                        <form:hidden path="containerMetrics[${rowCounter.index}].description" value="${metric.description}"/>
                                    </td>
								</tr>
							</c:forEach>
						</tbody>
					</table>
                    <br />
					<p align="center">
						<input type="button" value="Home" onclick="location.href='/cloudhub'" class="button-l" />
						<input type="button" value="Back" onclick="location.href='<c:out value="${updateConfigURL}"/>'" class="button-l" />
						<INPUT VALUE="Save" TYPE="SUBMIT" class="button-l" id="btn-save"/>
					</p>	
					<input type="hidden" name="action"
						value="create_from_ui_assign_page" />
				</div>
				<div class="controlbottom">
					<div class="cornerll"></div>
					<div class="cornerlr"></div>
				</div>
			</div>
		</div>

		<form:hidden path="configFileName"/>
		<form:hidden path="configFilePath"/>
        <form:hidden path="agent"/>
        <form:hidden path="profileType"/>

	</form:form>
</body>
</html>

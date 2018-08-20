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
    <link rel="stylesheet" href="css/bootstrap.min.css">
    <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css">
    <script src="js/jquery.min.js"></script>
    <script src="js/bootstrap.min.js"></script>
    <script src="js/jquery.bootstrap-duallistbox.min.js"></script>
    <link rel="stylesheet" type="text/css" href="css/bootstrap-duallistbox.css">
<style type="text/css" title="currentStyle">
@import "css/jdma_page.css";
@import "css/jdma_table.css";
</style>
</head>
<body id="dt_example">

<form name="selectForm"
	ACTION="/gwos-@appserver_shortname@-monitoringAgent/GWOS@appserver_camelcase@Servlet"
	method="post">
<p align="center"><img align="top" src="images/gwlogo.png" width="264px"></p>
<p align="center">JDMA for @appserver_camelcase@</p>
<div id="controlbg" class="controlbg2">
<div class="controltop">
<div class="cornerul"></div>
<div class="cornerur"></div>
</div>
<div class="controlheader">Select Mbean Attributes</div>
<div class="controlcontent">
<p>
<div id="container">

    <select name="services[]" id="services[]" multiple="multiple" size="10" >
        <%
            Object objServices = request.getAttribute("services");

            if (objServices != null) {
                List<String> services = (List) objServices;


                for (String service : services) {

        %><option value="<%=service%>"><%=service%></option>
        <%

                } // end for
            } // end if
        %>
            <option value="${service}">${service}</option>

    </select>

</div>


</p>
</div>
<div class="controlbottom">
<div class="cornerll"></div>
<div class="cornerlr"></div>
</div>
</div>
<p align="center"><input type="button" value="Back"
	onclick="location.href='newConnectionWizard.jsp'" class="button"/><button
	type="submit" class="button">Next</button></p>
    <input type="hidden" name="selectedServices" />
<input type="hidden" name="action" value="create_from_ui_select_page" />
    <script>
        var duallist = $('select[name="services[]"]').bootstrapDualListbox();
        $('form').submit(function() {
            var serviceList = $('[name="services[]"]').val();
            if (serviceList !== null && serviceList !== undefined) {
                document.selectForm.selectedServices.value = serviceList;
                return true;
            }
            else {
                alert('Please select atleast one mbean service to proceed!');
                return false;
            }
        });
    </script>
</form>


</body>
</html>

<%@ page import="org.jboss.portal.server.PortalConstants" %>
<%@page import="java.util.ResourceBundle"%>
<%@ taglib uri="/WEB-INF/theme/portal-layout.tld" prefix="p" %>
<%@ taglib uri="/WEB-INF/gwmon.tld" prefix="gw" %>
<% ResourceBundle rb = ResourceBundle.getBundle("Resource", request.getLocale()); %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
   <title><p:title default="GroundWork Monitor Enterprise"/></title>
   <meta http-equiv="Content-Type" content="text/html;"/>
   <!-- to correct the unsightly Flash of Unstyled Content. -->
   <script type="text/javascript">
   </script>
   <!-- inject the theme, default to the Renewal theme if nothing is selected for the portal or the page -->
   <p:theme themeName="groundwork"/>
   <!--[if IE]><link rel="stylesheet" type="text/css" id="main_css" href="/portal-core/themes/groundwork/portal_style_ie.css" /><![endif]-->
   <!-- insert header content that was possibly set by portlets on the page -->
   <p:headerContent/>
       <!--[if lt IE 8]><link rel="stylesheet" type="text/css" href="/portal-identity/style-ie.css" /><![endif]-->
   <link type="text/css" href="/portal-core/themes/common/jquery-ui.css" rel="Stylesheet" />
   <link type="text/css" href="/portal-core/css/jquery.autocomplete.css" rel="Stylesheet" />
   <link type="text/css" href="/portal-statusviewer/css/xp.css" rel="Stylesheet" />
   <link type="text/css" href="/portal-statusviewer/css/custom.css" rel="Stylesheet" />

   <script type="text/javascript" src="/portal-core/js/jquery.js"></script>
   <script type="text/javascript" src="/portal-core/js/jquery-ui.js"></script>
   <script type="text/javascript" src="/portal-core/js/jquery.autocomplete.js"></script>
   <script type="text/javascript" src="/portal-core/js/jquery.blockUI.js"></script>
   <script type="text/javascript" src="/portal-core/js/blockNavigation.js"></script>
   <script type="text/javascript" src="/portal-core/js/BoxOver.js"></script>
   <script type="text/javascript" src="/portal-core/js/modal.js"></script>
   <script type="text/javascript" src="/portal-core/js/clock.js"></script>
   <script type="text/javascript" src="/portal-core/js/jquery.dynDateTime.js"></script>
   <script type="text/javascript" src="/portal-core/js/calendar-en.js"></script>
   <script type="text/javascript" src="/portal-core/js/jquery.contextmenu.r2.js"></script>
   <script type="text/javascript" src="/portal-core/js/GWFunctions.js"></script>
   <script type="text/javascript" src="/portal-core/js/gw.jquery.autoheight.js"></script>
</head>

<body id="body" onload="updateClock(); setTimeout('updateClock(); setInterval(\'updateClock()\', 10000)', 10500 - ((new Date()).getTime() % 10000))">
<p:region regionName='AJAXScripts' regionID='AJAXScripts'/>
<div id="portal-container">
   <div id="sizer">
      <div id="expander">
         <div id="logoName"></div>
         <table border="0" cellpadding="0" cellspacing="0" id="header-container">
            <tr>
               <td align="center" valign="top" id="header">

                  <!-- Utility controls -->
                  <p:region regionName='dashboardnav' regionID='dashboardnav'/>

                  <!-- navigation tabs and such -->
                  <p:region regionName='navigation' regionID='navigation'/>
                  <div id="spacer"></div>
               </td>
            </tr>
            </table>
         <div id="content-container">
<table border="0" cellpadding="0" cellspacing="0"  style="min-width:963px; width:100%;">
    <tr>
        <td width="50%" valign="top"><p:region regionName='row1col1' regionID='row1col1'/></td>
        <td width="50%" valign="top"><p:region regionName='row1col2' regionID='row1col2'/></td>
      </tr>
</table>
<table border="0" cellpadding="0" cellspacing="0"  style="min-width:963px; width:100%;">
      <tr>
        <td width="33%" valign="top"><p:region regionName='row2col1' regionID='row2col1'/></td>
        <td width="33%" valign="top"><p:region regionName='row2col2' regionID='row2col2'/></td>
        <td width="33%" valign="top"><p:region regionName='row2col3' regionID='row2col3'/></td>
    </tr>
</table>
 <table border="0" cellpadding="0" cellspacing="0"  style="min-width:963px; width:100%;">
      <tr>
        <td width="50" valign="top"><p:region regionName='row3col1' regionID='row3col1'/></td>
        <td width="50" valign="top"><p:region regionName='row3col2' regionID='row3col2'/></td>
      </tr>
</table>
<table border="0" cellpadding="0" cellspacing="0"  style="min-width:963px; width:100%;">
    <tr>
        <td colspan="3"><p:region regionName='dash-bottom' regionID='dash-bottom'/></td>
    </tr>
</table>

<hr class="cleaner"/>
         </div>
      </div>
   </div>
</div>

<div id="footer-container" class="portal-copyright"><%= rb.getString("POWERED_BY") %>
<a class="portal-copyright" href="http://www.jboss.com/products/jbossportal">JBoss Portal</a><br/>
</div>

<!-- <p:region regionName='AJAXFooter' regionID='AJAXFooter'/> -->

</body>
</html>
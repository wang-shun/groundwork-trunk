<!-- 
    Coopyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
    All rights reserved. Use is subject to GroundWork commercial license terms.
 -->
<%@ page pageEncoding="utf-8" %>
<%@page import="org.jboss.portal.identity.UserStatus"%>
<%@page import="java.util.ResourceBundle"%>

<%
   ResourceBundle rb = ResourceBundle.getBundle("Resource", request.getLocale());

 /* retrive the server name from the URL */
 StringBuffer requestURL = request.getRequestURL();
 String serverURL = requestURL.substring(requestURL.indexOf("//")+2,requestURL.indexOf(request.getRequestURI()));
 
 /*Set an attribute on the session that will be read by the IFrameportlets */
 request.getSession().setAttribute("serverOfRequestURL", serverURL);
 
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
    <head>
    <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
    
    <title>GroundWork Monitor Starter Edition 6.0</title>
    <link rel="stylesheet" type="text/css" href="/portal-core/themes/groundwork/portal_style.css">
    <!--[if IE]><link rel="stylesheet" type="text/css" id="main_css" href="/portal-core/themes/groundwork/portal_style_ie.css" /><![endif]-->
     
    </head>
    <body style="margin:0; padding:0;background:#999;">

        <div id="login">
            <table id="loginpage" cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                    <td id="top">
                        <table cellpadding="0" cellspacing="0" border="0" width="100%">
                            <tr>
                                <td align="left">
                                    <img src="/portal-core/images/gwstarter.gif" height="66" />
                                </td>
                                <td align="right">
                                    <table cellpadding="0" cellspacing="0">
                                        <tr>
                                            <td style="<%=(request.getAttribute(!UserStatus.OK.equals("org.jboss.portal.userStatus") ? "" : "display:none"))%>;" id="error">
                    <%
         
                        if (UserStatus.DISABLE.equals(request.getAttribute("org.jboss.portal.userStatus")))
                        {
                           out.println(rb.getString("ACCOUNT_DISABLED"));
                        }
                        else if (UserStatus.WRONGPASSWORD.equals(request.getAttribute("org.jboss.portal.userStatus")) || UserStatus.UNEXISTING.equals(request.getAttribute("org.jboss.portal.userStatus")))
                        {
                           out.println(rb.getString("ACCOUNT_INEXISTING_OR_WRONG_PASSWORD"));
                        }
                        else if (UserStatus.NOTASSIGNEDTOROLE.equals(request.getAttribute("org.jboss.portal.userStatus")))
                        {
                           out.println(rb.getString("ACCOUNT_NOTASSIGNEDTOROLE"));
                        }
                    %>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>
                    
                                    <form method="post" action="<%= response.encodeURL("j_security_check") %>" name="loginform" id="loginform" target="_parent">
                                        <label for="j_username"><%= rb.getString("LOGIN_USERNAME") %> &nbsp;&nbsp;
                                            <input id="j_username" name="j_username" value="" size="20" maxlength="255" onKeyPress="if(window.event) keyvalue = event.keyCode; else keyvalue = event.which; " type="text">
                                        </label>
                                        <label for="j_password"><%= rb.getString("LOGIN_PASSWORD") %> &nbsp;&nbsp;&nbsp;
                                            <input id="j_password" name="j_password" value="" size="20" maxlength="255" onKeyPress="if(window.event) keyvalue = event.keyCode; else keyvalue = event.which; " type="password">
                                        </label>
                                        <input type="submit" name="submit" id="submit" value="<%= rb.getString("LOGIN_SUBMIT")  %>" class="portlet-form-button" />
                                    </form>
                                    
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td id="middle">
                        <table cellpadding="0" cellspacing="0" border="0" width="100%">
                            <tr>
                                <td>
                                    <a href="http://www.groundworkopensource.com/services/training/" target="self">
                                        <img src="/portal-core/images/training.gif" width="150" height="120" border="0" />
                                    </a>
                                </td>
                                <td>
                                    <a href="http://www.groundworkopensource.com/services/training/gwh.html" target="self">
                                        <img src="/portal-core/images/techvideos.gif" width="150" height="120" border="0" />
                                    </a>
                                </td>
                                <td>
                                    <a href="http://www.groundworkopensource.com/services/gwconnect.html" target="self">
                                        <img src="/portal-core/images/gwconnect.gif" width="150" height="120" border="0" />
                                    </a>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td id="bottom">
                        <table cellpadding="0" cellspacing="0" border="0" width="100%">
                            <tr>
                                <td align="left">
                                    <h2>GroundWork Open Source, Inc.<br>
                                    139 Townsend Street, Suite&nbsp;500<br>
                                    San Francisco, CA 94107</h2>
                                </td>
                                <td align="right">
                                    <h2>phone +1 866-899-4342<br>
                                    fax +1 415-947-0684<br>
                                    <a href="http://www.groundworkopensource.com/">www.groundworkopensource.com</a></h2>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2" align="center">
                                    <p>&copy; 2009 GroundWork Open Source, Inc.&nbsp; All rights reserved.</p>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </div>
        <script type="text/javascript">
            //<![CDATA[
            document.getElementsByTagName('input')[0].focus();
            // ]]>
        </script>
    </body>
</html>

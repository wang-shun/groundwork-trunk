<%@page import="org.jboss.portal.api.PortalURL" %>
<%@page import="java.security.Principal" %>
<%@page import="java.util.ResourceBundle"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.Map"%>
<%@page import="org.jboss.portal.identity.User"%>
<%@page import="org.jboss.portal.identity.UserModule"%>
<%@page import="org.jboss.portal.identity.UserProfileModule"%>
<%@page import="javax.naming.InitialContext"%>
<%@page import="javax.naming.NamingException"%>
<%@page import="org.apache.log4j.Logger" %>
<%
   Logger log = Logger.getLogger(getClass());
   ResourceBundle rb = ResourceBundle.getBundle("Resource", request.getLocale());
   Principal principal = (Principal)request.getAttribute("org.jboss.portal.header.PRINCIPAL");
   PortalURL dashboardURL = (PortalURL)request.getAttribute("org.jboss.portal.header.DASHBOARD_URL");
   PortalURL loginURL = (PortalURL)request.getAttribute("org.jboss.portal.header.LOGIN_URL");
   PortalURL defaultPortalURL = (PortalURL)request.getAttribute("org.jboss.portal.header.DEFAULT_PORTAL_URL");
   PortalURL adminPortalURL = (PortalURL)request.getAttribute("org.jboss.portal.header.ADMIN_PORTAL_URL");
   PortalURL editDashboardURL = (PortalURL)request.getAttribute("org.jboss.portal.header.EDIT_DASHBOARD_URL");
   PortalURL copyToDashboardURL = (PortalURL)request.getAttribute("org.jboss.portal.header.COPY_TO_DASHBOARD_URL");
   String signOutURL = (String) request.getAttribute("org.jboss.portal.header.SIGN_OUT_URL");
   String currentURL = (request.getQueryString() != null ? 
           request.getRequestURI() + "?" + request.getQueryString() : 
               request.getRequestURI());
   Map<String,String> userProfile = 
       (Map<String,String>) request.getAttribute("org.jboss.portal.header.USER_PROFILE");

   // If this is the first time an upgrade user has logged in, make them change their password
   if (userProfile != null && Boolean.valueOf(userProfile.get("portal.user.changePassword"))) {
       String error = null;
       boolean showDialog = true;
       if (Boolean.valueOf(request.getParameter("passwordChanged"))) {
           String newPassword = request.getParameter("newPassword");
           String newPasswordConfirm = request.getParameter("newPasswordConfirm");
           
           // TODO: Integrate the IdentityUserPortlet's password validation logic to make
           // sure that we have the consistent password validation rules throughout the 
           // application.
           if (newPassword == null || (newPassword = newPassword.trim()).equals("") ||
                   newPasswordConfirm == null || (newPasswordConfirm = 
                       newPasswordConfirm.trim()).equals("")) {
               error = "Please fill in all fields";
           } else if (!newPassword.equals(newPasswordConfirm)) {
               error = "'New Password' and 'Retype New Password' fields do not match";
           } else {
               // Set the user password and reset the "changePassword" property in the user
               // profile
               try {
                   UserModule userModule = 
                       (UserModule) new InitialContext().lookup("java:portal/UserModule");
                   UserProfileModule userProfileModule = 
                       (UserProfileModule) new InitialContext().lookup("java:portal/UserProfileModule");
                   User user = userModule.findUserByUserName(principal.getName());
                   user.updatePassword(newPassword);
                   userProfileModule.setProperty(user, "portal.user.changePassword", Boolean.toString(false));
                   
                   // HACK: The stupid JBoss Portal user profile cache attribute 
                   // doesn't get updated until the user logs out and back in, so
                   // we'll update it manually here.
                   userProfile.put("portal.user.changePassword", Boolean.toString(false));
                   
                   showDialog = false;
               } catch (Exception exception) {
                   log("Error while attempting to access UserProfileModule", exception);
                   error = "An unknown error occurred while processing your request.  Please try again, or contact your system administrator.";
               }
           }
       }
       
       if (showDialog) {
%>
<script type="text/javascript">
jQuery(function() {
	jQuery("#changePassword").dialog({
		bgiframe: true,
		modal: true,
		closeOnEscape: false,
		resizable: false
	});
});
</script>
<div id="changePassword" title="Change Password">
    <form method="POST" action="<%=currentURL%>">
        <p>Please change your password</p>
        <%=(error != null ? "<p><b><font color=\"red\">" + error + "</font></b></p>" : "")%>
        <label for="newPassword">New Password</label>
        <input id="newPassword" name="newPassword" type="password">
        <br>
        <label for="newPasswordConfirm">Retype New Password</label>
        <input id="newPasswordConfirm" name="newPasswordConfirm" type="password">
        <input type="hidden" name="passwordChanged" value="true">
        <input type="submit" value="Submit">
    </form>
</div>
<%
       }
   }
%>

<div class="welcomeMessage">
	<p>Welcome, <a href="#empty"><%=principal.getName() %></a></p>
</div>

<%
Object softlimitObj = request.getSession().getAttribute("softlimitexceeded");
if (softlimitObj != null)
{
	String softlimit = (String) softlimitObj;
	if (softlimit.equals ("true"))
	{
		out.println("<div style=\"background:black;color:white;display:inline;position:absolute;left:200px;\"><p>Your current license is approaching its validity limit. Please contact support@gwos.com for further assistance.</p></div>");
	}
}
%>

<div class="auxNavWrapper">
	<a href="#empty"><img src="/portal-core/themes/groundwork/images/icons/newWindow.png" alt="New Window" class="auxNavIcons" /></a>
	<p class="auxNavLinks"><a href="<%=currentURL%>" target="_blank">New Window</a></p>
	<div class="auxNavDivider"></div>
	<a href="#empty"><img src="/portal-core/themes/groundwork/images/icons/preferences.png" alt="My Preferences" class="auxNavIcons" /></a>
	<p class="auxNavLinks"><a href="/portal/portal/groundwork-monitor/prefs">My Preferences</a></p>
	<div class="auxNavDivider"></div>
	<a href="#empty"><img src="/portal-core/themes/groundwork/images/icons/help.png" alt="Help" class="auxNavIcons" /></a>
	<p class="auxNavLinks"><a href="/bookshelf-data/Bookshelf.htm" target="Bookshelf">Help</a></p>
	<div class="auxNavDivider"></div>
	<a href="#empty"><img src="/portal-core/themes/groundwork/images/icons/logOut.png" alt="Log Out" class="auxNavIcons" /></a>
	<p class="auxNavLinks"><a href="/portal/josso_logout/" onclick="return delete_cookies();">Log Out</a></p>
</div>
<div class="headerTimeStamp">
<span id="clock">&nbsp;</span>
 </div>

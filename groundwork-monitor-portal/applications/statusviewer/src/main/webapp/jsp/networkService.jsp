<!-- 
    Coopyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
    All rights reserved. This program is free software; you can redistribute
    it and/or modify it under the terms of the GNU General Public License
    version 2 as published by the Free Software Foundation.
   
    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    General Public License for more details.
  
    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 -->
<%@ page contentType="text/html"%>
<%@ page pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@ page import="javax.portlet.*" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="javax.portlet.PortletSession" %>
<%@ page import="com.groundworkopensource.portal.statusviewer.bean.networkservice.Notification" %>
<%@ page import="com.groundworkopensource.portal.statusviewer.bean.networkservice.NetworkServiceConfig" %>
<%@ page import="com.groundworkopensource.portal.statusviewer.bean.networkservice.NetworkServiceDatabase" %>
<portlet:defineObjects/>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/modal.js"></script>
<%PortletPreferences prefs = renderRequest.getPreferences();%>

 
<% // ###### MAIN CONDITION: if Network Service is Activated %>
<% if ( ! NetworkServiceConfig.isActivated()) { %>
<div>
     <p><%= NetworkServiceConfig.get("ns.msg.ns_inactive") %></p>
     <a id="reload_button" href="<portlet:actionURL><portlet:param name="reload_config" value="reload" /></portlet:actionURL>">Reload</a>
     <a id="ns_show_info_button" href="" onclick="toggleThisContent('moreinfo'); return false;" class="iceCmdLnk">Show Install Info</a>
     <div style="clear:both"> &nbsp; </div>
     <div id="moreinfo" style="display:none">
    		<h3>Installation Information</h3>
    		<span class="last_checked" id="last_checked"><%= prefs.getValue("network_service_plugin_info", "") %></span>
    	</div>
</div>
<% } else { %>

<% if ( NetworkServiceConfig.useInternalCss()) { %>
	<script type="text/javascript">
		var stylesheetLink = document.createElement('link');
		stylesheetLink.type = 'text/css';
		stylesheetLink.rel = 'stylesheet';
		stylesheetLink.href = '${renderRequest.contextPath}/css/ns_notifications.css';
		document.getElementsByTagName('head')[0].appendChild(stylesheetLink);
	</script>
<% } else { %>
	<script type="text/javascript">
		var stylesheetLink = document.createElement('link');
		stylesheetLink.type = 'text/css';
		stylesheetLink.rel = 'stylesheet';
		stylesheetLink.href = '${renderRequest.contextPath}/css/xp.css';
		document.getElementsByTagName('head')[0].appendChild(stylesheetLink);
	</script>
<% } %>
<div class="ns_notifications_box">  
    <!--<h1><%= NetworkServiceConfig.get("ns.msg.main_header") %></h1>-->
    <ul id="ns_notification_list" type="none">
 
   <%
   NetworkServiceDatabase networkServiceDatabaseInstance = NetworkServiceDatabase
   .getInstance();
   ArrayList<Notification> notifications = new ArrayList<Notification>();
   if (prefs.getValue("show_mode","unread").equalsIgnoreCase("all")) {
       notifications = networkServiceDatabaseInstance.getAllNotifications();
   } else {
       notifications = networkServiceDatabaseInstance.getUnreadNotifications();
   }
   
   	// Iterator<Notification> iterator = results.getResults().iterator();
   	Iterator<Notification> iterator = notifications.iterator();
      while (iterator.hasNext()) {
        Notification n = iterator.next();
   %>

       <% if (request.getParameter("ns_update_id") != null && Integer.parseInt(request.getParameter("ns_update_id")) == n.getId()) {%>
       <li class="<%= n.getCssClass(1)%>" style="list-style:none;">
           <img src="/portal-statusviewer/images/<%= n.getIconName()%>" alt="<%= n.getType()%>" align="top"/>
           <span class="ns_title"><%= n.getTitle()%></span>
           <div class="ns_content">
               <p><span class="ns_date">Date: <%= n.getCreatedAt()%></span></p>
               <p><%= n.getDescription()%></p>
               <p><a href="<%= n.getWebpageUrl() %>" class="iceCmdLnk"><%= n.getWebpageUrlDescription() != null ? n.getWebpageUrlDescription() : "more info"%></a></p>

               <% if (n.getType() == "update") { %>
               <p><small>INSTRUCTIONS</small><br/> n.getUpdateInstruction()</p>
               <p><small>COMMAND LINE SWITCHES:</small> n.getCmdSwitch()</p>
               <p><small>TYPE:</small> n.getUpdateType()</p>
               <p><small>SIZE:</small> n.getSize()</p>
               <p><small>OS:</small> n.getOs()</p>
               <p><small>MD5:</small> n.getMd5()</p>
               <p><a href="getUpdateUrl()" class="iceCmdLnk">get update</a></p>
               <% } %>
           </div>
           <div class="ns_controls">
               <% if (n.isRead()) { %>
               <a href="<portlet:actionURL><portlet:param name="ns_update" value="unread" /><portlet:param name="ns_update_id" value="<%= String.valueOf(n.getId()) %>" /></portlet:actionURL>" class="iceCmdLnk">mark as unread</a>
               <% } else { %>
               <a href="<portlet:actionURL><portlet:param name="ns_update" value="read" /><portlet:param name="ns_update_id" value="<%= String.valueOf(n.getId()) %>" /></portlet:actionURL>" class="iceCmdLnk">mark as read</a>
               <% } %>
        <!--       <a href="<portlet:actionURL><portlet:param name="ns_update" value="archived" /><portlet:param name="ns_update_id" value="<%= String.valueOf(n.getId()) %>" /></portlet:actionURL>">archive</a> -->
           </div>
       </li>
       <% } else {%>
       <li class="<%= n.getCssClass(0)%>" style="list-style:none;">
           <div>
               <img src="/portal-statusviewer/images/<%= n.getIconName()%>" alt="<%= n.getType()%>" align="top"/>
               <a href="<portlet:actionURL>
                      <portlet:param name="ns_update_id" value="<%= String.valueOf(n.getId()) %>" />
                  </portlet:actionURL>
                  " class="iceCmdLnk">
                   <%= n.getTitle()%>
               </a>
           </div>
       </li>
       <% }%>
    <% } // end of while %>
    
    <% if (notifications.size() < 1) { %>
       <li id="ns_notification_list">
       <%= prefs.getValue("db_info", NetworkServiceConfig.get("ns.msg.db_connection_problems")) %>
       </li>
    <% } %>
        
</ul>

    <div class="ns_notifications_list_footer">
        <span class="last_checked"><%= prefs.getValue("custom_text", "") %></span>
        <span class="last_checked">Last updated: <%= networkServiceDatabaseInstance.lastChecked() %></span>
        <% if (prefs.getValue("show_mode","unread").equalsIgnoreCase("all")) { %>
           <a id="ns_show_unread_button" href="<portlet:actionURL><portlet:param name="ns_update" value="show_unread" /></portlet:actionURL>" class="iceCmdLnk">Show Unread</a>
           <!--<a id="ns_copy_to_cp_button" href="" onclick="copyToClipboard('last_checked'); return false;" class="iceCmdLnk"></a>-->
           <a id="ns_show_info_button" href="" onclick="toggleThisContent('moreinfo'); return false;" class="iceCmdLnk">Show Install Info</a>
       	   <div style="clear:both"> &nbsp; </div>
       <% } else { %>
           <a id="ns_show_all_button" href="<portlet:actionURL><portlet:param name="ns_update" value="show_all" /></portlet:actionURL>" class="iceCmdLnk">Show All</a>
           <!--<a id="ns_copy_to_cp_button" href="" onclick="copyToClipboard('last_checked'); return false;" class="iceCmdLnk"></a>-->
           <a id="ns_show_info_button" href="" onclick="toggleThisContent('moreinfo'); return false;" class="iceCmdLnk">Show Install Info</a>
           <div style="clear:both"> &nbsp; </div>
    <% } %>
    	<div id="moreinfo" style="display:none">
    		<h3>Installation Information</h3>
    		<span class="last_checked" id="last_checked"><%= prefs.getValue("network_service_plugin_info", "") %></span>
    	</div>
    </div>
</div>
<% // ###### MAIN CONDITION: if Network Service is Activated %>
<% } %>

<!-- 
    Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
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
<portlet:defineObjects/>

<script type="text/javascript">
    var stylesheetLink = document.createElement('link');
    stylesheetLink.type = 'text/css';
    stylesheetLink.rel = 'stylesheet';
    stylesheetLink.href = '${renderRequest.contextPath}/css/ns_notifications.css';
    document.getElementsByTagName('head')[0].appendChild(stylesheetLink);
</script>

<div class="ns_notifications_box">
    <div class="ns_notifications_list_footer">
     <a id="ns_show_info_button" href="" onclick="toggleThisContent('moreinfo'); return false;" class="iceCmdLnk">Show Install Info</a>
     <div style="clear:both"> &nbsp; </div>
     <div id="moreinfo" style="display:none">
    		<h3>Installation Information</h3>
         <span class="last_checked" id="last_checked"><%= request.getAttribute("gwInstallInfo") %></span>
     </div>
    </div>
</div>

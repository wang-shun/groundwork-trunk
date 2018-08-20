<%@ page import="java.util.Iterator" %>
<%@ page import="org.jboss.portal.api.node.PortalNode" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Stack" %>
<%@ page import="org.jboss.portal.api.PortalRuntimeContext" %>


<%
   PortalNode portal = (PortalNode)request.getAttribute("org.jboss.portal.api.PORTAL_NODE");

   PortalNode currentMainPage = 
     (PortalNode)
     request.getAttribute("com.groundworkopensource.portal.CURRENT_MAIN_PAGE");
   PortalNode currentSubPage = 
     (PortalNode)
     request.getAttribute("com.groundworkopensource.portal.CURRENT_SUB_PAGE");
   
   PortalNode currentLevel3Page = 
	     (PortalNode)
	     request.getAttribute("com.groundworkopensource.portal.CURRENT_LEVEL3_PAGE");
   
   Collection<PortalNode> mainPages =
     (Collection<PortalNode>)
     request.getAttribute("com.groundworkopensource.portal.MAIN_PAGES");
   Collection<PortalNode> subPages =
     (Collection<PortalNode>)
     request.getAttribute("com.groundworkopensource.portal.SUB_PAGES");
   
   Collection<PortalNode> level3Pages =
	     (Collection<PortalNode>)
	     request.getAttribute("com.groundworkopensource.portal.LEVEL3_PAGES");
   Long requestId = (Long) request.getAttribute("requestId");
   
   

   PortalRuntimeContext context = (PortalRuntimeContext)request.getAttribute("org.jboss.portal.api.PORTAL_RUNTIME_CONTEXT");

   // Get a locale
   Locale locale = request.getLocale();
   if (locale == null)
   {
      locale = Locale.getDefault();
   }
%>

<ul id="tabsHeader">
<%
   // Insert the top-level navigation tabs
   boolean onMainPage = false;
   Stack<String> paramStack = new Stack<String>();
   if (requestId != null) {
       paramStack.push("requestId=" + requestId.toString());
   }
   
   String params = addParams(paramStack);
   for (PortalNode mainPage : mainPages) {
       String pageName = mainPage.getName();
       // HACK: The user preferences page should not appear in the top-level
       // nav
       if (pageName != null && 
           pageName.toLowerCase().contains("prefs")) {
           continue;
       }

       onMainPage = mainPage.getName().equals(currentMainPage.getName());
 %>
   <li <%=(onMainPage ? "id=\"current\"" : "")%>
       onmouseover="this.className='hoverOn'"
       onmouseout="this.className='hoverOff'">
       <a href="<%=mainPage.createURL(context) + params %>"><%= mainPage.getDisplayName(locale) %></a></li>
<%
   } // end for (PortalNode mainPage...)
%>
</ul>
<%
    if (subPages != null && subPages.size() > 0) {
%>
<ul id="subnav">
<%
       
        String subPageDeleteParams = addParams(paramStack);
        for (PortalNode subPage : subPages) {
            if (subPage.getType() != PortalNode.TYPE_PAGE) {
                continue;
            }

            String subPageName = subPage.getName();
            boolean onSubPage = (currentSubPage != null && 
                                 subPage.getName().equals(
                                     currentSubPage.getName()));
%>
	<li <%=(onSubPage ? "class=\"currentTab\"" : "")%>><a class= "" href="<%= subPage.createURL(context) + params %>" onclick="blockNavigation();"><%= subPage.getDisplayName(locale) %></a>
	
	</li>
<%
        } // end for (PortalNode subPage...)
%>
</ul>
<!--[if lte IE 6]></td></tr></table></a><![endif]-->
<%
    } //end if (subPages...)
%>

<!-- Level 3 -->
<%
    if (level3Pages != null && level3Pages.size() > 0) {
%>
<ul id="level3">
<%
       
        String level3DeleteParams = addParams(paramStack);
        for (PortalNode level3Page : level3Pages) {
            if (level3Page.getType() != PortalNode.TYPE_PAGE) {
                continue;
            }

            String level3PageName = level3Page.getName();
            boolean onLevel3Page = (currentLevel3Page != null && 
            		level3Page.getName().equals(
            				currentLevel3Page.getName()));
%>
	<li <%=(onLevel3Page ? "class=\"currentTab\"" : "")%>><a class= "" href="<%= level3Page.createURL(context) + params %>" onclick="blockNavigation();"><%= level3Page.getDisplayName(locale) %></a>
	
	</li>
<%
        } // end for (PortalNode subPage...)
%>
</ul>
<!--[if lte IE 6]></td></tr></table></a><![endif]-->
<%
    } //end if (subPages...)
%>


<%!
public String addParams(Stack paramStack) {   
    StringBuffer params = new StringBuffer();
    if (paramStack != null && paramStack.size() > 0) {
        params.append('?');
        Iterator<String> i = paramStack.iterator();
        while (i.hasNext()) {
            params.append(i.next());
            if (i.hasNext()) {
                params.append('&');
            }
        }
    }
    
    return params.toString();
}
%>

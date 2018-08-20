<%--
  ~ JOSSO: Java Open Single Sign-On
  ~
  ~ Copyright 2004-2009, Atricore, Inc.
  ~
  ~ This is free software; you can redistribute it and/or modify it
  ~ under the terms of the GNU Lesser General Public License as
  ~ published by the Free Software Foundation; either version 2.1 of
  ~ the License, or (at your option) any later version.
  ~
  ~ This software is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  ~ Lesser General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Lesser General Public
  ~ License along with this software; if not, write to the Free
  ~ Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  ~ 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  ~
  --%>
<%@ page contentType="text/html; charset=iso-8859-1" language="java" %>
<%@ taglib uri="/WEB-INF/tlds/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tlds/struts-bean.tld" prefix="bean" %>


<!-- PAGE HEADER  -->

<script type="text/javascript" language=JavaScript>
function drop_cookies() {
    document.cookie = "CGISESSID=;path=/;expires=Fri, 3 Aug 1970 20:47:11 UTC";
    document.cookie = "PHPSESSID=;path=/;expires=Fri, 3 Aug 1970 20:47:11 UTC";
    document.cookie = "nagvis_session=;path=/nagvis;expires=Fri, 3 Aug 1970 20:47:11 UTC";
    document.cookie = "treeArrayC=;path=/nms-rstools/php/rstools/;expires=Fri, 3 Aug 1970 20:47:11 UTC";
}
</script>

    <div id="header">
        <h1> <!-- Logo GWOS-->
            <a href="http://www.gwos.com" title="Click here to go to the homepage">
                <img src="/josso/resources/img/logo-gwos-enterprise.gif" height="81" onload="drop_cookies()"/>
            </a>
        </h1> <!-- /Logo JOSSO -->
    </div>
        <div id="authentication">

                <html:errors/>

                <div id="subwrapper">

                    <div class="main">
					

                        <html:form styleId="loginForm" action="/signon/usernamePasswordLogin" focus="josso_username">

                            <fieldset>
                                <html:hidden property="josso_cmd" value="login"/>
                                <html:hidden property="josso_back_to"/>

                                <div><label for="username"><bean:message key="sso.label.username"/> </label> <html:text styleClass="text" property="josso_username" value="" />
                                </div>
                                <div><label for="password"><bean:message key="sso.label.password"/> </label> <html:password styleClass="text" property="josso_password" value="" /></div>

				<div><label>&nbsp;</label><input class="button" type="submit" value="Login"/></div>
                            </fieldset>
                        </html:form>

                        <p>
                        <center>GroundWork Monitor Enterprise $GROUNDWORK_VERSION</center>
                        </p>

                           <!-- <p><bean:message key="sso.text.login.help"/>.</p> -->
						<div id="tips">
							<ul>
								<li>
									<a href="https://kb.groundworkopensource.com/" target="self">Knowledge Base</a> - Search the Knowledge Base
								</li>
								<li>
									<a href="https://cases.groundworkopensource.com/secure/Dashboard.jspa" target="self">Case Manager</a> - Submit a case to the Case Manager
								</li>
							</ul>
						</div>
						
						<div id="address">
							<p><b>GroundWork Inc.</b><br>
					Grand Rapids, MI 49525
                                    </p>
                                
                                    <p>phone +1 866-899-4342<br>
                                    	<a href="http://www.gwos.com/">www.gwos.com</a>
                                    </p>
                                    
                                    <p>&copy; 2018 GroundWork Inc. &nbsp;All&nbsp;rights&nbsp;reserved.</p>
				    <p>&nbsp;</p>
			    </div>
                    </div>
                    <!-- /main -->
                </div>

            </div> <!-- /authentication -->
            <script type="text/javascript" language="JavaScript">
				var f = document.getElementById('loginForm'); 
				var u = f.elements['josso_username']; 
				var p = f.elements['josso_password'];
				f.setAttribute("autocomplete", "off"); 
				// For Firefox each input need to set to off
				u.setAttribute("autocomplete", "off"); 
				p.setAttribute("autocomplete", "off");
				u.value='';
				p.value='';
				u.focus(); 
			</script>
        

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

<%
StringBuffer requestURL = request.getRequestURL();
String requestURI = request.getRequestURI();
String serverURL = requestURL.substring(requestURL.indexOf("//")+2,requestURL.indexOf(requestURI));
 // Modify the short server name to the actual servername
 String back_to = request.getParameter("josso_back_to");
 String protocol = back_to.substring(0,back_to.indexOf(":"));
 String new_back_to = protocol + "://" + serverURL + "/portal/josso_security_check";
%>
<!-- PAGE HEADER  -->

    <div id="header">
        <h1> <!-- Logo GWOS-->
            <a href="http://www.gwos.com" title="Click here to go to the homepage">
                <img src="/portal-core/images/logo-gwos-enterprise.gif" height="81" />
            </a>
        </h1> <!-- /Logo JOSSO -->
    </div>
        <div id="authentication">

                <html:errors/>

                <div id="subwrapper">

                    <div class="main">


                        <html:form action="/signon/usernamePasswordLogin" focus="josso_username" >

                            <fieldset>
                                <html:hidden property="josso_cmd" value="login"/>
                                <html:hidden property="josso_back_to" value="<%=new_back_to%>"/>

                                <div><label for="username"><bean:message key="sso.label.username"/> </label> <html:text styleClass="text" property="josso_username" />
                                </div>
                                <div><label for="password"><bean:message key="sso.label.password"/> </label> <html:password styleClass="text" property="josso_password" /></div>
                            </fieldset>

                            <div><input class="button indent" type="submit" value="Login"/></div>
                        </html:form>

                           <!-- <p><bean:message key="sso.text.login.help"/>.</p> -->
						<div id="tips">
							<ul>
								<li>
									<a href="http://www.groundworkopensource.com/services/training/" target="self">Training</a> - Maximize your monitoring
								</li>
								<li>
									<a href="http://www.groundworkopensource.com/services/training/gwh.html" target="self">Technical Videos</a> - Bite-size how-tos for GroundWork Monitor
								</li>
								<li>
									<a href="https://kb.groundworkopensource.com/display/SUPPORT/Home" target="self">GroundWork Connect</a> - GroundWork&#39;s Support Portal
								</li>
							</ul>
						</div>
						
						<div id="address">
							<p>GroundWork Inc.<br>
					201 Spear Street, Suite&nbsp;1650<br>
					San Francisco, CA 94105
                                    </p>
                                
                                    <p>phone +1 866-899-4342<br>
                                    	fax +1 866-414-7358<br>
                                    	<a href="http://www.groundworkopensource.com/">www.groundworkopensource.com</a>
                                    </p>
                                    
                                    <p>&copy; 2012 GroundWork Inc.&nbsp; All rights reserved.</p>
						</div>


                    </div>
                    <!-- /main -->
                </div>

            </div> <!-- /authentication -->
        

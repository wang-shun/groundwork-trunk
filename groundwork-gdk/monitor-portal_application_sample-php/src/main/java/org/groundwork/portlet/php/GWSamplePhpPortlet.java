package org.groundwork.portlet.php;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletContext;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSecurityException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import org.jboss.portal.portlet.aspects.portlet.ContextDispatcherInterceptor;
import org.jboss.portal.portlet.container.PortletApplication;
import org.jboss.portal.portlet.container.PortletApplicationContext;
import org.jboss.portal.portlet.impl.jsr168.api.PortletContextImpl;
import org.jboss.portal.portlet.impl.jsr168.api.PortletRequestImpl;
import org.jboss.portal.portlet.invocation.PortletInvocation;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.EventRequest;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import java.io.*;
import javax.servlet.Servlet;

/**
 * User: Chris Mills (millsy@jboss.com)
 * Date: 04-Mar-2006
 * Time: 10:45:10
 */

public class GWSamplePhpPortlet extends GenericPortlet
{
   /* INIT Parameter defining the URL */
   private static final String PARAM_URL = "URL";
   /* INIT Parameter defining the SCRIPT */
   private static final String PARAM_SCRIPT = "SCRIPT";
   /* URL for IFrame set default to GroundWork */
   private String urlToRender = "php/index.jsp";
   private String scriptToRun = "PHP";
   private PortletContext portletContext;

   /**
   * Default constructor
   */
   public GWSamplePhpPortlet() {
		
   }
	
	
   /**
   * Init phase of the portlet. Using it to read INIT params defined in the portlet.xml
   * @param config
   * @throws PortletException
   */
   public void init(PortletConfig config) throws PortletException
   {
       super.init(config);
       this.urlToRender = config.getInitParameter(PARAM_URL);
       this.scriptToRun = config.getInitParameter(PARAM_SCRIPT);
    }	
	

   public void processAction(ActionRequest request, ActionResponse response) throws PortletException, PortletSecurityException, IOException
   {
	   response.setPortletMode(PortletMode.VIEW);
   }

   public void doView(RenderRequest request, RenderResponse response)
   {
      try
      {
         response.setContentType("text/html");
         request.setAttribute("url",this.urlToRender);

	 if (this.scriptToRun.equals("PHP"))
	 {
         	PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/php/index.jsp");
         	prd.include(request, response);
	 }
	 else
		response.getWriter().println("This is an unsupported script ["+this.scriptToRun+"]");
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }
   
}



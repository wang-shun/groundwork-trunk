/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2006, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.jboss.portlet;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletSecurityException;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.jboss.portal.common.text.CharBuffer;
import org.jboss.portal.common.text.EntityEncoder;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * The JBossPortlet.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 10781 $
 */
public class JBossPortlet implements Portlet
{
   /** . */
   private static final Class[] ACTION_LOOKUP = new Class[]{JBossActionRequest.class, JBossActionResponse.class};

   /** . */
   private static final PortletMode ADMIN = new PortletMode("admin");

   /** . */
   private PortletConfig config;

   public JBossPortlet()
   {
   }

   /** Return the string <i>main</i>, it can be overriden to return another value by subclasses. */
   public String getDefaultOperation()
   {
      return "main";
   }

   /** Return the string <i>op</i>, it can be overriden to return another value by subclasses. */
   public String getOperationName()
   {
      return "op";
   }

   public void init() throws PortletException
   {
   }

   public PortletConfig getPortletConfig()
   {
      return config;
   }

   public String getPortletName()
   {
      return config.getPortletName();
   }

   public PortletContext getPortletContext()
   {
      return config.getPortletContext();
   }

   /** Calls <code>doDispatch(JBossActionRequest,JBossActionResponse)</code>. */
   protected void processAction(JBossActionRequest req, JBossActionResponse resp) throws PortletException, PortletSecurityException, IOException
   {
      processDispatch(req, resp);
   }

   /**
    * <p>This method looks up the method corresponding to the action. It uses the action parameter using the parameter
    * name defines by the <code>operationName</code> field of this class. If not method is found it uses the method
    * defined by the return of the method <code>getDefaultOperation()</code> of this class. In order to be found a
    * method must use <code>JBossActionRequest</code> and <JBossActionResponse> in the signature.</p> <p/> <p>If not
    * valid dispatcher is found it throws a PortletException, otherwise it invokes the method by reflection. The invoked
    * method may declare exceptions in the throws clause of the method. Whenever an exception is raised during the
    * invocation of the method, a decision is taken depending on the nature of the exception :</p>
    * <p/>
    * <ul> <li>If the exception is an instanceof <code>PortletException</code>, <code>IOException</code> then this
    * exception is rethrown as is since this method declares them in its throws clause</li> <li>If the exception is an
    * instance of <code>RuntimeException</code> or <code>Error>/code>, it is rethrown as is</li> <li>Otherwise a
    * <code>PortletException</code> is created with the caught exception as cause and thrown</li> </ul>
    */
   protected void processDispatch(JBossActionRequest req, JBossActionResponse resp) throws PortletException, PortletSecurityException, IOException
   {
      PortletMode portletMode = req.getPortletMode();
      if (PortletMode.VIEW.equals(portletMode))
      {
         processView(req, resp);
      }
      else if (PortletMode.HELP.equals(portletMode))
      {
         processHelp(req, resp);
      }
      else if (PortletMode.EDIT.equals(portletMode))
      {
         processEdit(req, resp);
      }
      else if (ADMIN.equals(portletMode))
      {
         processAdmin(req, resp);
      }
   }

   /** Default doEdit method that works in coordination with doEdit(JBossRenderRequest,JBossRenderResponse). */
   public void processEdit(JBossActionRequest req, JBossActionResponse resp) throws PortletException, PortletSecurityException, IOException
   {
      PortletPreferences prefs = req.getPreferences();
      Map map = prefs.getMap();
      for (Iterator i = req.getParameterMap().entrySet().iterator(); i.hasNext();)
      {
         Map.Entry entry = (Map.Entry)i.next();
         String name = (String)entry.getKey();
         String[] values = (String[])entry.getValue();
         if (map.containsKey(name))
         {
            prefs.setValues(name, values);
         }
      }
      prefs.store();
   }

   /**
    *
    */
   public void processHelp(JBossActionRequest req, JBossActionResponse resp) throws PortletException, PortletSecurityException, IOException
   {
      throw new PortletException();
   }

   /**
    *
    */
   public void processAdmin(JBossActionRequest req, JBossActionResponse resp) throws PortletException, PortletSecurityException, IOException
   {
      throw new PortletException();
   }

   /**
    *
    */
   public void processView(JBossActionRequest req, JBossActionResponse resp) throws PortletException, PortletSecurityException, IOException
   {
      // Try to locate specific operation
      Method dispatcher = null;
      String operation = req.getParameter(getOperationName());
      if (operation != null)
      {
         dispatcher = lookupMethod(operation, ACTION_LOOKUP);
      }

      // If it null try to getPortalObjectContext the default operation
      if (dispatcher == null)
      {
         dispatcher = lookupMethod(getDefaultOperation(), ACTION_LOOKUP);
      }

      // Invoke the operation
      if (dispatcher != null)
      {
         try
         {
            dispatcher.invoke(this, new Object[]{req, resp});
         }
         catch (IllegalAccessException e)
         {
            throw new PortletException(e);
         }
         catch (InvocationTargetException e)
         {
            Throwable t = e.getCause();
            if (t instanceof PortletException)
            {
               throw (PortletException)t;
            }
            else if (t instanceof IOException)
            {
               throw (IOException)t;
            }
            else if (t instanceof RuntimeException)
            {
               throw (RuntimeException)t;
            }
            else if (t instanceof Error)
            {
               throw (Error)t;
            }
            else
            {
               throw new PortletException("Unexpected exception when dispatching the operation", e);
            }
         }
      }
      else
      {
         throw new PortletException("Nothing to invoke");
      }
   }

   /** Calls <code>doDispatch(JBossRenderRequest,JBossRenderResponse)</code>. */
   protected void render(JBossRenderRequest req, JBossRenderResponse resp) throws PortletException, PortletSecurityException, IOException
   {
      resp.setTitle(getTitle(req));
      doDispatch(req, resp);
   }

   /**
    *
    */
   protected void doDispatch(JBossRenderRequest request, JBossRenderResponse response) throws PortletException, PortletSecurityException, IOException
   {
      if (!WindowState.MINIMIZED.equals(request.getWindowState()))
      {
         PortletMode portletMode = request.getPortletMode();
         if (PortletMode.VIEW.equals(portletMode))
         {
            doView(request, response);
         }
         else if (PortletMode.HELP.equals(portletMode))
         {
            doHelp(request, response);
         }
         else if (PortletMode.EDIT.equals(portletMode))
         {
            doEdit(request, response);
         }
         else if (ADMIN.equals(portletMode))
         {
            doAdmin(request, response);
         }
      }
   }

   /** Throw a <code>PortletException</code>. */
   protected void doView(JBossRenderRequest request, JBossRenderResponse response) throws PortletException, PortletSecurityException, IOException
   {
      throw new PortletException();
   }

   /** Throw a <code>PortletException</code>. */
   protected void doHelp(JBossRenderRequest request, JBossRenderResponse response) throws PortletException, PortletSecurityException, IOException
   {
      throw new PortletException();
   }

   /** Provide a default generic editor for preferences that produce HTML markup. */
   protected void doEdit(JBossRenderRequest request, JBossRenderResponse response) throws PortletException, PortletSecurityException, IOException
   {
      response.setContentType("text/html");
      PrintWriter writer = response.getWriter();

      //
      PortletURL url = response.createActionURL();

      //
      writer.print("<table> " +
         "<tr><td class=\"portlet-section-alternate\">" + "Name" +
         "</td><td class=\"portlet-section-alternate\">" + "Value" +
         "</td></tr>" +
         "<form action=\"");
      writer.print(url.toString());
      writer.print("\" method=\"post\">");

      //
      PortletPreferences prefs = request.getPreferences();
      for (Iterator i = prefs.getMap().entrySet().iterator(); i.hasNext();)
      {
         Map.Entry entry = (Map.Entry)i.next();
         String name = (String)entry.getKey();
         String[] values = (String[])entry.getValue();

         // Perform HTML entity replacement
         CharBuffer nameBuffer = new CharBuffer();
         EntityEncoder.FULL.encode(name, nameBuffer);
         name = nameBuffer.asString();
         
         CharBuffer valueBuffer = new CharBuffer();
         for (int j = 0; j < values.length; j++)
         {
            String value = values[j];
            if (value != null)
            {
            	valueBuffer.reset();
            	EntityEncoder.FULL.encode(value, valueBuffer);
            	values[j] = valueBuffer.asString();
            }
         }

         //
         writer.print("<tr><td class=\"portlet-section-body\">");
         writer.print(name);
         writer.print("</td><td class=\"portlet-section-body\">");
         if (prefs.isReadOnly(name))
         {
            writer.print(name);
         }
         else
         {
            writer.print("<input class=\"portlet-form-input-field\" type=\"text\" name=\"");
            writer.print(name);
            writer.print("\" value=\"");
            writer.print(values.length >= 1 ? values[0] : "");
            writer.print("\"/>");
         }
         writer.print("</td></tr>");
      }

      writer.print("<tr><td colspan=\"2\" class=\"portlet-section-alternate\">" +
         "<input class=\"portlet-form-button\" type=\"submit\" value=\"Save\"/>" +
         "</td></tr>" +
         "</form></table>");
   }

   /** Throw a <code>PortletException</code>. */
   protected void doAdmin(JBossRenderRequest request, JBossRenderResponse response) throws PortletException, PortletSecurityException, IOException
   {
      throw new PortletException();
   }

   public ResourceBundle getResourceBundle(Locale locale)
   {
      return getPortletConfig().getResourceBundle(locale);
   }

   protected String getTitle(RenderRequest request)
   {
      ResourceBundle bundle = getResourceBundle(request.getLocale());
      return bundle.getString("javax.portlet.title");
   }

   public String getInitParameter(String name) throws IllegalArgumentException
   {
      return getPortletConfig().getInitParameter(name);
   }

   public Enumeration getInitParameterNames()
   {
      return getPortletConfig().getInitParameterNames();
   }

   // javax.portlet.Portlet implementation *****************************************************************************

   public void init(PortletConfig config) throws PortletException
   {
      this.config = config;
      init();
   }

   public void processAction(ActionRequest request, ActionResponse response) throws PortletException, PortletSecurityException, IOException
   {
      processAction((JBossActionRequest)request, (JBossActionResponse)response);
   }

   public void render(RenderRequest req, RenderResponse resp) throws PortletException, PortletSecurityException, IOException
   {
      if (req instanceof JBossRenderRequest && resp instanceof JBossRenderResponse)
      {
         render((JBossRenderRequest)req, (JBossRenderResponse)resp);
      }
      else
      {
         throw new PortletException("The request isn't a JBossRenderRequest, you probably need to activate the JBoss Portlet Filter: org.jboss.portlet.filter.JBossPortletFilter on " + getPortletName());
      }
   }

   public void destroy()
   {
   }

   // Private **********************************************************************************************************

   /** Locate a method. */
   private Method lookupMethod(String operation, Class[] parameterTypes)
   {
      try
      {
         Method m = getClass().getMethod(operation, parameterTypes);
         if (m.getReturnType() == void.class &&
            Modifier.isPublic(m.getModifiers()))
         {
            return m;
         }
      }
      catch (NoSuchMethodException e)
      {
         // Does not exist
      }
      return null;
   }
}

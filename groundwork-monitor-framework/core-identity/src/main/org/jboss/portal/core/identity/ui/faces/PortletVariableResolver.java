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
package org.jboss.portal.core.identity.ui.faces;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.VariableResolver;
import javax.portlet.PortletRequest;


import org.jboss.logging.Logger;


/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class PortletVariableResolver extends VariableResolver
{
   /** . */
   private VariableResolver delegate;
   
   /** . */
   private final static String PORTLET_CONFIG = "portletConfig";

   /** . */
   private final static String PORTLET_SESSION_SCOPE = "sessionPortletScope";
   
   /** . */
   private final static String PORTLET_APPLICATION_SCOPE = "portletApplicationScope";
   
   /** . */
   private final static String PORTLET_RENDER_PARAMETER = "portletRenderParameter";

   /** . */
   private final static String PORTLET_PREFERENCE_VALUE = "portletPreferenceValue";

   /** . */
   private final static String PORTLET_PREFERENCE_VALUES = "portletPreferenceValues";

   /** .*/
   private static final Logger log = Logger.getLogger(PortletVariableResolver.class);

   public PortletVariableResolver(VariableResolver delegate)
   {
      this.delegate = delegate;
   }

   public Object resolveVariable(FacesContext facesContext, String variable) throws EvaluationException
   {
      ExternalContext ectx = facesContext.getExternalContext();
      /**
       * TODO
       * portletConfig and  test if portletApplicationScope is resolved correctly 
       */
      if (PORTLET_SESSION_SCOPE.equals(variable))
      {
         return ectx.getSessionMap();
      }
      else if (PORTLET_APPLICATION_SCOPE.equals(variable))
      {
         return ectx.getApplicationMap();
      }
      else if (PORTLET_RENDER_PARAMETER.equals(variable))
      {
         return getRenderValueMap(ectx);
      }
      else if (PORTLET_PREFERENCE_VALUE.equals(variable))
      {
         return getValueMap(ectx);
      }
      else if (PORTLET_PREFERENCE_VALUES.equals(variable))
      {
         PortletRequest request = (PortletRequest) ectx.getRequest();
         return request.getPreferences().getMap();
      }
      return delegate.resolveVariable(facesContext, variable);
   }
   
   private Map getValueMap(ExternalContext ectx)
   {
      Map map = new HashMap();
      PortletRequest request = (PortletRequest) ectx.getRequest();
      Enumeration en = request.getPreferences().getNames();

      while (en.hasMoreElements())
      {
         String key = (String) en.nextElement();
         String value = request.getPreferences().getValue(key, null);
         if (value != null)
         {
            map.put(key, value);
         }
      }
      return map;
   }
   
   private Map getRenderValueMap(ExternalContext ectx)
   {
      Map map = new HashMap();
      PortletRequest request = (PortletRequest) ectx.getRequest();
      Enumeration en = request.getParameterNames();

      while (en.hasMoreElements())
      {
         String key = (String) en.nextElement();
         String value = request.getParameter(key);
         if (value != null)
         {
            map.put(key, value);
         }
      }
      return map;
   }
   
}

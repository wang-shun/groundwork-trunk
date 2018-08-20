/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2008, Red Hat Middleware, LLC, and individual                    *
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
package org.jboss.portal.core.portlet;

import org.jboss.portal.Mode;
import org.jboss.portal.WindowState;
import org.jboss.portal.common.NotYetImplemented;
import org.jboss.portal.common.util.ParameterMap;
import org.jboss.portal.portlet.ParametersStateString;
import org.jboss.portal.portlet.StateString;
import org.jboss.portal.portlet.cache.CacheLevel;

import java.util.Map;

/**
 * This class is designed to provide the encoding in the query string of a URL of the following state : <ul> <li>A set
 * of parameters</li> <li>A mode value</li> <li>A window state value</li> <li>A invocation type (action or render)</li>
 * </ul>
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public class PortletRequestEncoder
{

   /** . */
   private ParameterMap queryParameters;

   public PortletRequestEncoder(ParameterMap queryParameters)
   {
      if (queryParameters == null)
      {
         throw new IllegalArgumentException();
      }

      //
      this.queryParameters = queryParameters;
   }

   public PortletRequestEncoder()
   {
      this(new ParameterMap());
   }

   public void encodeResource(
           CacheLevel cacheability,
           String resourceId,
           StateString resourceState)
   {
      queryParameters.clear();

      //
      int meta = PortletRequestDecoder.RESOURCE_PHASE;

      //
      if (resourceState != null)
      {
         if (resourceState instanceof ParametersStateString)
         {
            // Add the parameters
            Map<String, String[]> parameters = ((ParametersStateString)resourceState).getParameters();
            configure(parameters);
         }
         else
         {
            throw new NotYetImplemented("We do not implement resource serving for wsrp");
         }
      }

      //
      if (cacheability != null)
      {
         meta |= PortletRequestDecoder.CACHEABILITY_MASK;
         setMetaParameter(PortletRequestDecoder.CACHEABILITY_PARAMETER, cacheability.toString());
      }

      //
      if (resourceId != null)
      {
         meta |= PortletRequestDecoder.RESOURCE_ID_MASK;
         setMetaParameter(PortletRequestDecoder.RESOURCE_ID_PARAMETER, resourceId);
      }

      //
      setMetaParameter(PortletRequestDecoder.META_PARAMETER, Integer.toHexString(meta));
   }

   public void encodeAction(
           StateString navigationalState,
           StateString interactionState,
           Mode mode,
           WindowState windowState) throws IllegalArgumentException
   {
      queryParameters.clear();

      //
      int meta = PortletRequestDecoder.ACTION_PHASE;

      //
      if (interactionState != null)
      {
         if (interactionState instanceof ParametersStateString)
         {
            // we don't need to encode the navigational state in the URL (stored in session)
            // but we shouldn't throw an exception here because it's needed for template creation in WSRP
            // so just discard it
            navigationalState = null;

            // Add the parameters
            Map<String, String[]> parameters = ((ParametersStateString)interactionState).getParameters();
            configure(parameters);
         }
         else
         {
            meta |= PortletRequestDecoder.OPAQUE_MASK;

            // Set interaction state
            queryParameters.setValue(PortletRequestDecoder.INTERACTION_STATE_PARAMETER, interactionState.getStringValue());

            // We may have navigational state
            if (navigationalState != null)
            {
               queryParameters.setValue(PortletRequestDecoder.NAVIGATIONAL_STATE_PARAMETER, navigationalState.getStringValue());
            }
         }
      }

      //
      configure(meta, mode, windowState);
   }

   public void encodeRender(
           StateString navigationalState,
           Mode mode,
           WindowState windowState)
   {
      queryParameters.clear();

      //
      if (navigationalState != null)
      {
         int meta = PortletRequestDecoder.RENDER_PHASE;

         //
         if (navigationalState instanceof ParametersStateString)
         {
            // Add the parameters
            Map<String, String[]> parameters = ((ParametersStateString)navigationalState).getParameters();
            configure(parameters);
         }
         else
         {
            meta |= PortletRequestDecoder.OPAQUE_MASK;

            //
            queryParameters.setValue(PortletRequestDecoder.NAVIGATIONAL_STATE_PARAMETER, navigationalState.getStringValue());
         }

         //
         configure(meta, mode, windowState);
      }
      else
      {
         if (mode != null)
         {
            queryParameters.setValue(PortletRequestDecoder.MODE_PARAMETER, mode.toString());
         }

         //
         if (windowState != null)
         {
            queryParameters.setValue(PortletRequestDecoder.WINDOW_STATE_PARAMETER, windowState.toString());
         }
      }
   }

   public ParameterMap getQueryParameters()
   {
      return queryParameters;
   }

   private void configure(Map<String, String[]> parameters)
   {
      for (Map.Entry<String, String[]> entry : parameters.entrySet())
      {
         String name = entry.getKey();
         String[] values = entry.getValue();
         queryParameters.setValues(name, values);
      }
   }

   private void configure(int meta, Mode mode, WindowState windowState)
   {
      if (mode != null)
      {
         meta |= PortletRequestDecoder.MODE_MASK;
         setMetaParameter(PortletRequestDecoder.MODE_PARAMETER, mode.toString());
      }
      if (windowState != null)
      {
         meta |= PortletRequestDecoder.WINDOW_STATE_MASK;
         setMetaParameter(PortletRequestDecoder.WINDOW_STATE_PARAMETER, windowState.toString());
      }
      setMetaParameter(PortletRequestDecoder.META_PARAMETER, Integer.toHexString(meta));
   }

   private void setMetaParameter(String name, String value)
   {
      String[] values = queryParameters.getValues(name);
      if (values == null)
      {
         values = new String[]{value};
      }
      else
      {
         String[] tmp = new String[values.length + 1];
         System.arraycopy(values, 0, tmp, 1, values.length);
         tmp[0] = value;
         values = tmp;
      }
      queryParameters.setValues(name, values);
   }
}
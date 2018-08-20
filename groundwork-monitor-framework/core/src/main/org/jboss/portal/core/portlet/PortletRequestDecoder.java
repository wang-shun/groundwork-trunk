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
import org.jboss.portal.common.util.ParameterMap;
import org.jboss.portal.common.util.ParameterValidation;
import org.jboss.portal.portlet.OpaqueStateString;
import org.jboss.portal.portlet.ParametersStateString;
import org.jboss.portal.portlet.StateString;
import org.jboss.portal.portlet.cache.CacheLevel;

import java.util.Map;

/**
 * This class is a possible implementation for the behavior of a request made to a portlet. Which means that this
 * implementation does not preclude other implementations.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 6549 $
 */
public class PortletRequestDecoder
{

   /** The action phase. */
   public static final int ACTION_PHASE = 1;

   /** The render phase. */
   public static final int RENDER_PHASE = 2;

   /** The resource phase. */
   public static final int RESOURCE_PHASE = 3;

   /** The mask for action. */
   public static final int PHASE_MASK = 0x00000003;

   /** The mask for mode. */
   public static final int MODE_MASK = 0x00000004;

   /** The mask for resource id. */
   public static final int RESOURCE_ID_MASK = 0x00000004;

   /** The mask for window state. */
   public static final int WINDOW_STATE_MASK = 0x00000008;

   /** The mask for cacheability. */
   public static final int CACHEABILITY_MASK = 0x00000008;

   /** The mask for opacity. */
   public static final int OPAQUE_MASK = 0x00000010;

   /** The name of the URL parameter containing the mode. */
   public static final String MODE_PARAMETER = "mode";

   /** The name of the URL parameter containing the window state. */
   public static final String WINDOW_STATE_PARAMETER = "windowstate";

   /** The name of the URL parameter containing the interaction state. */
   public static final String INTERACTION_STATE_PARAMETER = "is";

   /** The name of the URL parameter containing the navigational state. */
   public static final String NAVIGATIONAL_STATE_PARAMETER = "ns";

   /** The name of the URL parameter containing the resource state. */
   public static final String RESOURCE_STATE_PARAMETER = "rs";

   /** The name of the URL parameter containing the cacheability. */
   public static final String CACHEABILITY_PARAMETER = "cacheability";

   /** The name of the URL parameter containing the resource id. */
   public static final String RESOURCE_ID_PARAMETER = "id";

   /** The name of the URL parameter containing the meta information. */
   public static final String META_PARAMETER = "action";

   /** . */
   public static final int ACTION_TYPE = 0;

   /** . */
   public static final int RENDER_TYPE = 1;

   /** . */
   public static final int NAV_TYPE = 2;

   /** . */
   public static final int RESOURCE_TYPE = 3;

   /** . */
   private Mode mode;

   /** . */
   private WindowState windowState;

   /** . */
   private StateString navigationalState;

   /** . */
   private StateString interactionState;

   /** . */
   private StateString resourceState;

   /** . */
   private ParameterMap form;

   /** . */
   private String resourceId;

   /** . */
   private CacheLevel cacheability;

   /** . */
   private int type;

   public void decode(Map<String, String[]> queryParams, Map<String, String[]> bodyParams) throws IllegalArgumentException
   {
      mode = null;
      windowState = null;
      navigationalState = null;
      interactionState = null;
      form = null;
      resourceId = null;
      cacheability = null;

      // The meta info from the URL
      int meta = 0;
      String[] metaParam = queryParams.get(META_PARAMETER);
      if (metaParam != null)
      {
         try
         {
            meta = Integer.parseInt(metaParam[0], 16);
         }
         catch (NumberFormatException ignore)
         {
            // If mask is not present then we assume that it can only be a navigation URL (NAV_TYPE)
         }
      }

      //
      int phase = meta & PHASE_MASK;

      //
      if (phase != 0)
      {
         switch (phase)
         {
            case ACTION_PHASE:
               type = ACTION_TYPE;
               break;
            case RENDER_PHASE:
               type = RENDER_TYPE;
               break;
            case RESOURCE_PHASE:
               type = RESOURCE_TYPE;
               break;
            default:
               throw new AssertionError();
         }

         //
         if (type == RESOURCE_TYPE)
         {
            // Get the resource id from the parameters if it exists
            if ((meta & RESOURCE_ID_MASK) != 0)
            {
               String[] resourceIdParam = queryParams.get(RESOURCE_ID_PARAMETER);
               ParameterValidation.throwIllegalArgExceptionIfNull(resourceIdParam, "resource id");
               resourceId = resourceIdParam[0];
            }

            // Get the cacheability from the parameters if it exists
            if ((meta & CACHEABILITY_MASK) != 0)
            {
               String[] cacheabilityParam = queryParams.get(CACHEABILITY_PARAMETER);
               ParameterValidation.throwIllegalArgExceptionIfNull(cacheabilityParam, "cacheability");
               cacheability = CacheLevel.valueOf(cacheabilityParam[0]);
            }
         }
         else
         {
            // Get the mode from the parameters if it exists
            if ((meta & MODE_MASK) != 0)
            {
               String[] modeParam = queryParams.get(MODE_PARAMETER);
               ParameterValidation.throwIllegalArgExceptionIfNull(modeParam, "mode");
               mode = Mode.create(modeParam[0]);
            }

            // Get the window state from the parameters if it exists
            if ((meta & WINDOW_STATE_MASK) != 0)
            {
               String[] windowStateParam = queryParams.get(WINDOW_STATE_PARAMETER);
               ParameterValidation.throwIllegalArgExceptionIfNull(windowStateParam, "window state");
               windowState = WindowState.create(windowStateParam[0]);
            }
         }

         boolean opaque = (meta & OPAQUE_MASK) != 0;
         if (!opaque)
         {
            // Compute the parameters skipping the portlet navigational state that may be encoded as well
            ParametersStateString query = ParametersStateString.create();
            for (Map.Entry<String, String[]> entry : queryParams.entrySet())
            {
               int index = 0;
               String name = entry.getKey();
               String[] queryValues = entry.getValue();

               //
               if (META_PARAMETER.equals(name))
               {
                  index = 1;
               }
               else if (type == RESOURCE_TYPE)
               {
                  if ((meta & RESOURCE_ID_MASK) != 0 && RESOURCE_ID_PARAMETER.equals(name))
                  {
                     index = 1;
                  }
                  else if ((meta & CACHEABILITY_MASK) != 0 && CACHEABILITY_PARAMETER.equals(name))
                  {
                     index = 1;
                  }
               }
               else
               {
                  if ((meta & MODE_MASK) != 0 && MODE_PARAMETER.equals(name))
                  {
                     index = 1;
                  }
                  else if ((meta & WINDOW_STATE_MASK) != 0 && WINDOW_STATE_PARAMETER.equals(name))
                  {
                     index = 1;
                  }
               }

               // We have interaction param(s) in the query string
               if (index < queryValues.length)
               {
                  String[] values = new String[queryValues.length - index];
                  System.arraycopy(queryValues, index, values, 0, values.length);
                  query.setValues(name, values);
               }
            }

            // Julien :
            ParameterMap form = new ParameterMap();
            if (bodyParams != null)
            {
               form.putAll(bodyParams);
            }

            //
            switch (type)
            {
               case ACTION_TYPE:
                  this.interactionState = query;
                  this.form = form;
                  break;
               case RENDER_TYPE:
                  this.navigationalState = query;
                  break;
               case RESOURCE_TYPE:
                  this.resourceState = query;
                  this.form = form;
                  break;
            }
         }
         else
         {
            // Decode the navigational state
            String[] ns = queryParams.get(NAVIGATIONAL_STATE_PARAMETER);
            if (ns != null)
            {
               navigationalState = new OpaqueStateString(ns[0]);
            }

            // Decode more if we have an action
            if (type == ACTION_TYPE)
            {
               // Decode the interaction state
               String[] is = queryParams.get(INTERACTION_STATE_PARAMETER);
               if (is != null)
               {
                  interactionState = new OpaqueStateString(is[0]);
               }

               //
               form = new ParameterMap();
               if (bodyParams != null)
               {
                  form.putAll(bodyParams);
               }
            }
         }
      }
      else
      {
         // Set to nav type
         type = NAV_TYPE;

         // Get the mode from the parameters if it exists
         String[] modeParam = queryParams.get(MODE_PARAMETER);
         if (modeParam != null)
         {
            mode = Mode.create(modeParam[0]);
         }

         // Get the window state from the parameters if it exists
         String[] windowStateParam = queryParams.get(WINDOW_STATE_PARAMETER);
         if (windowStateParam != null)
         {
            windowState = WindowState.create(windowStateParam[0]);
         }
      }
   }

   public Mode getMode()
   {
      return mode;
   }

   public WindowState getWindowState()
   {
      return windowState;
   }

   public StateString getNavigationalState()
   {
      return navigationalState;
   }

   public StateString getInteractionState()
   {
      return interactionState;
   }

   public ParameterMap getForm()
   {
      return form;
   }

   public int getType()
   {
      return type;
   }

   public String getResourceId()
   {
      return resourceId;
   }

   public CacheLevel getCacheability()
   {
      return cacheability;
   }

   public StateString getResourceState()
   {
      return resourceState;
   }
}
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
package org.jboss.portal.test.core.model.instance;

import org.jboss.portal.portlet.InvalidPortletIdException;
import org.jboss.portal.portlet.NoSuchPortletException;
import org.jboss.portal.portlet.Portlet;
import org.jboss.portal.portlet.PortletContext;
import org.jboss.portal.portlet.PortletInvoker;
import org.jboss.portal.portlet.PortletInvokerException;
import org.jboss.portal.portlet.impl.info.ContainerPortletInfo;
import org.jboss.portal.portlet.info.PortletInfo;
import org.jboss.portal.portlet.invocation.PortletInvocation;
import org.jboss.portal.portlet.invocation.response.PortletInvocationResponse;
import org.jboss.portal.portlet.state.DestroyCloneFailure;
import org.jboss.portal.portlet.state.PropertyChange;
import org.jboss.portal.portlet.state.PropertyMap;
import org.jboss.portal.portlet.state.SimplePropertyMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class PortletInvokerSupport implements PortletInvoker
{

   /** . */
   private Map<String, Portlet> portlets;

   public PortletInvokerSupport()
   {
      this.portlets = new HashMap<String, Portlet>();
   }

   public void setValid(String portletId, boolean valid)
   {
      getInternalPortlet(portletId).valid = valid;
   }

   public InternalPortlet addInternalPortlet(String portletId)
   {
      return addInternalPortlet(portletId, new PortletSupport());
   }

   public InternalPortlet addInternalPortlet(String portletId, PortletSupport support)
   {
      if (portletId == null)
      {
         throw new IllegalArgumentException();
      }
      if (support == null)
      {
         throw new IllegalArgumentException();
      }
      InternalPortlet portlet = new InternalPortlet(portletId, support);
      if (portlets.put(portletId, portlet) != null)
      {
         throw new IllegalStateException();
      }
      return portlet;
   }

   public PortletInvokerSupport removeInternalPortlet(String portletId)
   {
      if (portlets.remove(portletId) == null)
      {
         throw new IllegalStateException();
      }
      return this;
   }

   public InternalPortlet getInternalPortlet(String portletId)
   {
      if (portletId == null)
      {
         throw new IllegalArgumentException();
      }
      InternalPortlet portlet = (InternalPortlet)portlets.get(portletId);
      if (portlet == null)
      {
         throw new IllegalArgumentException();
      }
      return portlet;
   }

   public Set<Portlet> getPortlets()
   {
      return new HashSet<Portlet>(portlets.values());
   }

   public Portlet getPortlet(PortletContext portletContext) throws IllegalArgumentException, PortletInvokerException
   {
      return internalGetPortlet(portletContext);
   }

   public PortletInvocationResponse invoke(PortletInvocation invocation) throws PortletInvokerException
   {
      PortletContext portletContext = invocation.getTarget();
      InternalPortlet portlet = internalGetPortlet(portletContext);
      return portlet.support.invoke(invocation);
   }

   private InternalPortlet internalGetPortlet(PortletContext portletContext) throws IllegalArgumentException, PortletInvokerException
   {
      if (portletContext == null)
      {
         throw new IllegalArgumentException();
      }
      String portletId = portletContext.getId();
      InternalPortlet portlet = (InternalPortlet)portlets.get(portletId);
      if (portlet == null)
      {
         throw new NoSuchPortletException(portletId);
      }
      if (!portlet.isValid())
      {
         throw new InvalidPortletIdException(portletId);
      }
      return portlet;
   }

   public static class InternalPortlet implements Portlet
   {

      /** . */
      private final PortletContext portletContext;

      /** . */
      private final PortletSupport support;

      /** . */
      private boolean valid;

      /** . */
      private Map<String, List<String>> state;

      public InternalPortlet(String portletId, PortletSupport support)
      {
         if (portletId == null)
         {
            throw new IllegalArgumentException();
         }
         if (support == null)
         {
            throw new IllegalArgumentException();
         }
         this.portletContext = PortletContext.createPortletContext(portletId);
         this.support = support;
         this.valid = true;
         this.state = new HashMap<String, List<String>>();
      }

      public void addPreference(String key, List<String> value)
      {
         ((ContainerPortletInfo)support.getPortletInfo()).getPreferences().addContainerPreference(key, value, false, null, null);
         state.put(key, value);
      }

      public void addPreference(String key, List<String> value, Boolean readOnly)
      {
         ((ContainerPortletInfo)support.getPortletInfo()).getPreferences().addContainerPreference(key, value, readOnly, null, null);
         state.put(key, value);
      }

      public PortletContext getContext()
      {
         return portletContext;
      }

      public PortletInfo getInfo()
      {
         return support.getPortletInfo();
      }

      public boolean isRemote()
      {
         return false;
      }

      public boolean isValid()
      {
         return valid;
      }
   }

   public PropertyMap getProperties(PortletContext portletContext, Set keys) throws IllegalArgumentException, PortletInvokerException, UnsupportedOperationException
   {
      InternalPortlet internalPortlet = internalGetPortlet(portletContext);
      PropertyMap props = new SimplePropertyMap();
      for (Object object : keys)
      {
         String key = (String)object;
         List<String> value = internalPortlet.state.get(key);
         if (value != null)
         {
            props.put(key, value);
         }
      }
      return props;
   }

   public PropertyMap getProperties(PortletContext portletContext) throws IllegalArgumentException, PortletInvokerException, UnsupportedOperationException
   {
      InternalPortlet internalPortlet = internalGetPortlet(portletContext);
      PropertyMap props = new SimplePropertyMap();
      for (String key : internalPortlet.state.keySet())
      {
         List<String> value = internalPortlet.state.get(key);
         if (value != null)
         {
            props.put(key, value);
         }
      }
      return props;
   }

   public PortletContext createClone(PortletContext portletContext) throws IllegalArgumentException, PortletInvokerException, UnsupportedOperationException
   {
      throw new UnsupportedOperationException();
   }

   public List<DestroyCloneFailure> destroyClones(List portletContexts) throws IllegalArgumentException, PortletInvokerException, UnsupportedOperationException
   {
      throw new UnsupportedOperationException();
   }

   public PortletContext setProperties(PortletContext portletContext, PropertyChange[] changes) throws IllegalArgumentException, PortletInvokerException, UnsupportedOperationException
   {
      throw new UnsupportedOperationException();
   }
}

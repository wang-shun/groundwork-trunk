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
package org.jboss.portal.core.impl.portlet.state;

import org.jboss.portal.portlet.Portlet;
import org.jboss.portal.portlet.PortletContext;
import org.jboss.portal.portlet.PortletInvoker;
import org.jboss.portal.portlet.PortletInvokerException;
import org.jboss.portal.portlet.invocation.PortletInvocation;
import org.jboss.portal.portlet.invocation.response.PortletInvocationResponse;
import org.jboss.portal.portlet.state.PropertyChange;
import org.jboss.portal.portlet.state.PropertyMap;

import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class LocalPortletInvoker implements PortletInvoker
{

   /** . */
   private static final ThreadLocal local = new ThreadLocal();

   /** . */
   private PortletInvoker portletInvoker;

   public PortletInvoker getPortletInvoker()
   {
      return portletInvoker;
   }

   public void setPortletInvoker(PortletInvoker portletInvoker)
   {
      this.portletInvoker = portletInvoker;
   }

   public static boolean isLocal()
   {
      return Boolean.TRUE.equals(local.get());
   }

   public Set getPortlets() throws PortletInvokerException
   {
      try
      {
         local.set(Boolean.TRUE);

         //
         return portletInvoker.getPortlets();
      }
      finally
      {
         local.set(null);
      }
   }

   public Portlet getPortlet(PortletContext portletContext) throws IllegalArgumentException, PortletInvokerException
   {
      try
      {
         local.set(Boolean.TRUE);

         //
         return portletInvoker.getPortlet(portletContext);
      }
      finally
      {
         local.set(null);
      }
   }

   public PortletInvocationResponse invoke(PortletInvocation invocation) throws IllegalArgumentException, PortletInvokerException
   {
      try
      {
         local.set(Boolean.TRUE);

         //
         return portletInvoker.invoke(invocation);
      }
      finally
      {
         local.set(null);
      }
   }

   public PortletContext createClone(PortletContext portletContext) throws IllegalArgumentException, PortletInvokerException, UnsupportedOperationException
   {
      try
      {
         local.set(Boolean.TRUE);

         //
         return portletInvoker.createClone(portletContext);
      }
      finally
      {
         local.set(null);
      }
   }

   public List destroyClones(List portletContexts) throws IllegalArgumentException, PortletInvokerException, UnsupportedOperationException
   {
      try
      {
         local.set(Boolean.TRUE);

         //
         return portletInvoker.destroyClones(portletContexts);
      }
      finally
      {
         local.set(null);
      }
   }

   public PropertyMap getProperties(PortletContext portletContext, Set keys) throws IllegalArgumentException, PortletInvokerException, UnsupportedOperationException
   {
      try
      {
         local.set(Boolean.TRUE);

         //
         return portletInvoker.getProperties(portletContext, keys);
      }
      finally
      {
         local.set(null);
      }
   }

   public PropertyMap getProperties(PortletContext portletContext) throws IllegalArgumentException, PortletInvokerException, UnsupportedOperationException
   {
      try
      {
         local.set(Boolean.TRUE);

         //
         return portletInvoker.getProperties(portletContext);
      }
      finally
      {
         local.set(null);
      }
   }

   public PortletContext setProperties(PortletContext portletContext, PropertyChange[] changes) throws IllegalArgumentException, PortletInvokerException, UnsupportedOperationException
   {
      try
      {
         local.set(Boolean.TRUE);

         //
         return portletInvoker.setProperties(portletContext, changes);
      }
      finally
      {
         local.set(null);
      }
   }
}

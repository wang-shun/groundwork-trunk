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
package org.jboss.portal.core.impl.api.event;

import org.jboss.mx.util.MBeanProxy;
import org.jboss.mx.util.MBeanProxyCreationException;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.portal.api.event.PortalEvent;
import org.jboss.portal.api.event.PortalEventContext;
import org.jboss.portal.api.event.PortalEventListener;
import org.jboss.portal.api.session.event.PortalSessionEvent;
import org.jboss.portal.api.user.event.UserAuthenticationEvent;
import org.jboss.portal.core.event.PortalEventListenerRegistry;
import org.jboss.portal.core.impl.api.PortalRuntimeContextImpl;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.Iterator;

/**
 * Bridge servlet event to portal events.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class ServletEventBridge implements HttpSessionListener, ServletContextListener, HttpSessionAttributeListener
{

   /** . */
   private PortalEventListenerRegistry listenerRegistry;

   // ServletContextListener implementation ****************************************************************************

   public void contextInitialized(ServletContextEvent event)
   {
      try
      {
         listenerRegistry = (PortalEventListenerRegistry)MBeanProxy.get(PortalEventListenerRegistry.class, ObjectNameFactory.create("portal:service=ListenerRegistry"), MBeanServerLocator.locateJBoss());
      }
      catch (MBeanProxyCreationException e)
      {
         e.printStackTrace();
      }
   }

   public void contextDestroyed(ServletContextEvent event)
   {
      listenerRegistry = null;
   }

   // HttpSessionListener implementation *******************************************************************************

   public void sessionCreated(HttpSessionEvent event)
   {
      PortalRuntimeContextImpl rt = new PortalRuntimeContextImpl(event.getSession());
      PortalEventContextImpl uec = new PortalEventContextImpl(rt);
      PortalSessionEvent use = new PortalSessionEvent(PortalSessionEvent.SESSION_CREATED);
      fireEvent(uec, use);
   }

   public void sessionDestroyed(HttpSessionEvent event)
   {
      PortalRuntimeContextImpl rt = new PortalRuntimeContextImpl(event.getSession());
      PortalEventContextImpl uec = new PortalEventContextImpl(rt);
      PortalSessionEvent use = new PortalSessionEvent(PortalSessionEvent.SESSION_DESTROYED);
      fireEvent(uec, use);
   }

   // HttpSessionAttributeListener implementation **********************************************************************


   public void attributeAdded(HttpSessionBindingEvent event)
   {
      if ("PRINCIPAL_TOKEN".equals(event.getName()))
      {
         String userId = (String)event.getValue();
         PortalRuntimeContextImpl rt = new PortalRuntimeContextImpl(event.getSession(), userId);
         PortalEventContextImpl uec = new PortalEventContextImpl(rt);
         UserAuthenticationEvent uae = new UserAuthenticationEvent(userId, UserAuthenticationEvent.SIGN_IN);
         fireEvent(uec, uae);
      }
   }

   public void attributeRemoved(HttpSessionBindingEvent event)
   {
      if ("PRINCIPAL_TOKEN".equals(event.getName()))
      {
         String userId = (String)event.getValue();
         PortalRuntimeContextImpl rt = new PortalRuntimeContextImpl(event.getSession(), userId);
         PortalEventContextImpl uec = new PortalEventContextImpl(rt);
         UserAuthenticationEvent uae = new UserAuthenticationEvent(userId, UserAuthenticationEvent.SIGN_OUT);
         fireEvent(uec, uae);
      }
   }

   public void attributeReplaced(HttpSessionBindingEvent event)
   {
   }

   private void fireEvent(PortalEventContext eventContext, PortalEvent event)
   {
      for (Iterator i = listenerRegistry.getListeners().iterator(); i.hasNext();)
      {
         Object o = i.next();
         if (o instanceof PortalEventListener)
         {
            PortalEventListener listener = (PortalEventListener)o;
            try
            {
               listener.onEvent(eventContext, event);
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }
         }
      }
   }
}

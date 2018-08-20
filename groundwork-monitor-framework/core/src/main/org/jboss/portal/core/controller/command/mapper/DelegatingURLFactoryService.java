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
package org.jboss.portal.core.controller.command.mapper;

import org.jboss.portal.core.controller.ControllerCommand;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.server.ServerInvocation;
import org.jboss.portal.server.ServerURL;

import java.util.ArrayList;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class DelegatingURLFactoryService extends AbstractURLFactory implements DelegatingURLFactory
{

   /** The next factory. */
   private URLFactory nextFactory;

   /** The different delegates. */
   private ArrayList delegates = new ArrayList();

   public URLFactory getNextFactory()
   {
      return nextFactory;
   }

   public void setNextFactory(URLFactory nextFactory)
   {
      this.nextFactory = nextFactory;
   }

   public ServerURL doMapping(ControllerContext controllerContext, ServerInvocation invocation, ControllerCommand cmd)
   {
      if (cmd == null)
      {
         throw new IllegalArgumentException("No null command accepted");
      }

      //
      if (delegates != null)
      {
         for (int i = 0; i < delegates.size(); i++)
         {
            URLFactory delegate = (URLFactory)delegates.get(i);
            ServerURL url = delegate.doMapping(controllerContext, invocation, cmd);
            if (url != null)
            {
               return url;
            }
         }
      }

      //
      if (nextFactory != null)
      {
         return nextFactory.doMapping(controllerContext, invocation, cmd);
      }

      //
      return null;
   }

   public void register(URLFactoryDelegate factory)
   {
      if (factory == null)
      {
         throw new IllegalArgumentException("Command class name must not be null");
      }
      synchronized (this)
      {
         if (delegates.contains(factory))
         {
            log.warn("Dual registration of URL factory " + factory);
         }
         else
         {
            ArrayList copy = new ArrayList(delegates);
            copy.add(factory);
            delegates = copy;
         }
      }
   }

   public void unregister(URLFactoryDelegate factory)
   {
      if (factory == null)
      {
         throw new IllegalArgumentException("Command class name must not be null");
      }
      synchronized (this)
      {
         if (delegates.contains(factory))
         {
            log.warn("Unregistration of URL factory " + factory + " failed because it is not registered");
         }
         else
         {
            ArrayList copy = new ArrayList(delegates);
            copy.remove(factory);
            delegates = copy;
         }
      }
   }
}

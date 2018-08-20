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
package org.jboss.portal.core.event;

import org.jboss.portal.jems.as.system.AbstractJBossService;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class PortalEventListenerServiceImpl extends AbstractJBossService implements PortalEventListenerService
{

   protected String registryId;
   protected PortalEventListenerRegistry registry;
   protected Object listener;
   protected String listenerClassName;

   public String getListenerClassName()
   {
      return listenerClassName;
   }

   public void setListenerClassName(String listenerClassName)
   {
      this.listenerClassName = listenerClassName;
   }

   public String getRegistryId()
   {
      return registryId;
   }

   public void setRegistryId(String registryId)
   {
      this.registryId = registryId;
   }

   public PortalEventListenerRegistry getRegistry()
   {
      return registry;
   }

   public void setRegistry(PortalEventListenerRegistry registry)
   {
      this.registry = registry;
   }

   public Object getListener()
   {
      return listener;
   }

   protected void startService() throws Exception
   {
      log.debug("Getting listener class " + listenerClassName);
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class listenerClass = loader.loadClass(listenerClassName);
      log.debug("Creating listener instance of  " + listenerClass.getName());
      listener = listenerClass.newInstance();

      //
      log.debug("Registering listener instance " + listenerClass.getName() + " with id " + registryId);
      registry.registerListener(registryId, listener);
   }

   protected void stopService()
   {
      log.debug("Unregistering listener instance with id " + registryId);
      registry.unregisterListener(registryId);

      //
      listener = null;
   }
}

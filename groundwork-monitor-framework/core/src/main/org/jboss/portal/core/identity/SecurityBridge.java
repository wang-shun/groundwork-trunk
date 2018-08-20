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
package org.jboss.portal.core.identity;

import org.jboss.portal.identity.event.IdentityEvent;
import org.jboss.portal.identity.event.IdentityEventEmitter;
import org.jboss.portal.identity.event.IdentityEventListener;
import org.jboss.portal.identity.event.UserDestroyedEvent;
import org.jboss.portal.jems.as.system.AbstractJBossService;
import org.jboss.security.auth.login.DynamicLoginConfigMBean;

/**
 * Use identity destroy events to destroy corresponding dashboards.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class SecurityBridge extends AbstractJBossService implements IdentityEventListener
{

   /** . */
   private IdentityEventEmitter identityEventEmitter;

   /** . */
   private DynamicLoginConfigMBean dynamicLoginConfig;

   public IdentityEventEmitter getIdentityEventEmitter()
   {
      return identityEventEmitter;
   }

   public void setIdentityEventEmitter(IdentityEventEmitter identityEventEmitter)
   {
      this.identityEventEmitter = identityEventEmitter;
   }

   public DynamicLoginConfigMBean getDynamicLoginConfig()
   {
      return dynamicLoginConfig;
   }

   public void setDynamicLoginConfig(DynamicLoginConfigMBean dynamicLoginConfig)
   {
      this.dynamicLoginConfig = dynamicLoginConfig;
   }

   protected void startService() throws Exception
   {
      identityEventEmitter.addListener(this);
   }

   protected void stopService() throws Exception
   {
      identityEventEmitter.removeListener(this);
   }

   public void onEvent(IdentityEvent event)
   {
      if (event instanceof UserDestroyedEvent)
      {
         final UserDestroyedEvent destroyedEvent = (UserDestroyedEvent)event;

         //
         log.debug("User (userName=" + destroyedEvent.getUserName() + ",id=" + destroyedEvent.getUserId() +
            " ) is destroyed, will invalidate security manager cache instead ");

         //
         try
         {
            dynamicLoginConfig.flushAuthenticationCaches();
         }
         catch (Exception e)
         {
            log.warn("Error while flushing authentication cache", e);
         }
      }
   }
}

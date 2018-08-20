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
package org.jboss.portal.core.impl.model.instance;

import org.apache.log4j.Logger;
import org.jboss.portal.core.model.instance.Instance;
import org.jboss.portal.core.model.instance.InstanceContainer;
import org.jboss.portal.core.model.instance.InstanceCustomization;
import org.jboss.portal.core.model.instance.InstanceDefinition;
import org.jboss.portal.portlet.Portlet;
import org.jboss.portal.portlet.PortletContext;
import org.jboss.portal.portlet.PortletInvoker;
import org.jboss.portal.portlet.PortletInvokerException;
import org.jboss.portal.portlet.invocation.PortletInvocation;
import org.jboss.portal.portlet.invocation.response.PortletInvocationResponse;
import org.jboss.portal.portlet.state.AccessMode;
import org.jboss.portal.portlet.state.DestroyCloneFailure;
import org.jboss.portal.portlet.state.PropertyChange;
import org.jboss.portal.portlet.state.PropertyMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11068 $
 */
public abstract class AbstractInstance implements Instance
{

   /** . */
   private static final Logger log = Logger.getLogger(AbstractInstance.class);

   /** . */
   protected String portletRef;

   /** . */
   protected byte[] state;

   /** Composite representation of portletRef + state. */
   protected PortletContext portletContext;

   protected AbstractInstance()
   {
   }

   public String getPortletRef()
   {
      return portletRef;
   }

   public void setPortletRef(String portletRef)
   {
      this.portletRef = portletRef;

      // Invalidate
      this.portletContext = null;
   }

   public byte[] getState()
   {
      return state;
   }

   public void setState(byte[] state)
   {
      this.state = state;

      // Invalidate
      this.portletContext = null;
   }

   public final PortletContext getPortletContext()
   {
      if (portletContext == null)
      {
         portletContext = PortletContext.createPortletContext(portletRef, state);
      }
      return portletContext;
   }

   public final boolean isModifiable()
   {
      return isMutable();
   }

   protected abstract boolean isMutable();

   protected abstract void setMutable(boolean modifiable);

   protected abstract Logger getLogger();

   protected abstract AccessMode getAccessMode();

   protected abstract void cloned(PortletContext portletContext);

   protected abstract AbstractInstanceDefinition getOwner();

   protected abstract InstanceContainerContext getContainerContext();

   protected abstract String getInstanceId();

   protected final void modified(PortletContext portletContext)
   {
      getContainerContext().updateInstance(this, portletContext);
   }

   public final InstanceDefinition getDefinition()
   {
      return getOwner();
   }

   public final Portlet getPortlet() throws PortletInvokerException
   {
      PortletInvoker invoker = getContainer().getPortletInvoker();
      PortletContext ctx = getOwner().getPortletContext();
      return invoker.getPortlet(ctx);
   }

   public final InstanceContainer getContainer()
   {
      return ((JBossInstanceContainerContext)getContainerContext()).getContainer();
   }

   public final void setProperties(PropertyChange[] changes) throws PortletInvokerException
   {
      if (changes == null)
      {
         throw new IllegalArgumentException("No null changes accepted");
      }
      boolean debug = getLogger().isDebugEnabled();

      // Get the invoker
      PortletInvoker portletInvoker = getContainer().getPortletInvoker();

      //
      PortletContext portletContext = getPortletContext();

      //
      if (isModifiable() == false)
      {
         // Clone the portlet
         if (debug)
         {
            getLogger().debug("Need to clone non modifiable instance before setting properties " /*+ instanceId + "/"*/ + portletContext);
         }
         portletContext = portletInvoker.createClone(portletContext);
         if (debug)
         {
            getLogger().debug("Received updated portlet context " + portletContext + " for instance " /*+ instanceId*/ + " after explicit clone");
         }

         // Update the state
         getContainerContext().updateInstance(this, portletContext, true);
      }

      //
      if (debug)
      {
         getLogger().debug("Setting properties on " + /*instanceId + "/" +*/ portletContext + " : " + Arrays.asList(changes));
      }
      portletContext = portletInvoker.setProperties(portletContext, changes);
      if (debug)
      {
         getLogger().debug("Received updated portlet context " + portletContext + " for instance " + /*instanceId +*/ " after setting properties");
      }

      // Update state
      getContainerContext().updateInstance(this, portletContext);
   }

   public final PropertyMap getProperties() throws PortletInvokerException
   {
      PortletInvoker invoker = getContainer().getPortletInvoker();
      PortletContext portletContext = getPortletContext();
      return invoker.getProperties(portletContext);
   }

   public final PropertyMap getProperties(Set keys) throws PortletInvokerException
   {
      PortletInvoker invoker = getContainer().getPortletInvoker();
      PortletContext portletContext = getPortletContext();
      return invoker.getProperties(portletContext, keys);
   }

   public final PortletInvocationResponse invoke(PortletInvocation invocation) throws PortletInvokerException
   {
      boolean debug = getLogger().isDebugEnabled();

      //
      AbstractInstance instance = this;
      AccessMode accessMode = getAccessMode();

      //
      PortletContext portletContext = instance.getPortletContext();

      // The instance context for the invocation
      InstanceContextImpl instanceContext = new InstanceContextImpl(this, accessMode);

      try
      {
         invocation.setAttribute(INSTANCE_ID_ATTRIBUTE, getInstanceId());
         invocation.setTarget(portletContext);
         invocation.setInstanceContext(instanceContext);

         // Perform invocation
         InstanceContainerImpl container = (InstanceContainerImpl)getContainer();
         PortletInvocationResponse response = container.invoke(invocation);

         // Create user instance if a clone operation occured
         if (instanceContext.accessMode == AccessMode.CLONE_BEFORE_WRITE)
         {
            if (instanceContext.clonedContext != null)
            {
               if (debug)
               {
//                    log.debug("About to reference clone of (" + instanceId + "," + portletContext +
//                              ") having id " + instanceContext.clonedContext + " for user " + userId);
               }
               cloned(instanceContext.clonedContext);
            }
            else
            {
               // Does not make sense
            }
         }
         else if (instanceContext.accessMode == AccessMode.READ_WRITE)
         {
            if (instanceContext.modifiedContext != null)
            {
               if (debug)
               {
//                 log.debug("About to update portlet context (" + instanceId + "," + portletContext +
//                           ") having id " + instanceContext.clonedContext + " for user " + userId);
               }
               modified(instanceContext.modifiedContext);
            }
            else
            {
               // Does not make sense
            }
         }

         //
         return response;
      }
      finally
      {
         // Reset state before invocation
         invocation.removeAttribute(INSTANCE_ID_ATTRIBUTE);
         invocation.setTarget(null);
         invocation.setInstanceContext(null);
      }
   }

   public final InstanceCustomization getCustomization(String customizationId)
   {
      if (customizationId == null)
      {
         throw new IllegalArgumentException();
      }

      // Check if we have an instance for this particular customized id
      AbstractInstanceCustomization customization = getContainerContext().getCustomization(getOwner(), customizationId);

      //
      if (customization == null)
      {
         AbstractInstanceDefinition def = getOwner();
         customization = getContainerContext().newInstanceCustomization(def, customizationId, getPortletContext());
      }

      //
      return customization;
   }

   public final void destroyCustomization(String customizationId)
   {
      AbstractInstanceCustomization customization = getContainerContext().getCustomization(getOwner(), customizationId);

      //
      if (customization != null)
      {
         try
         {
            // Get what we need to destroy
            PortletContext customizationPortletContext = customization.getPortletContext();
            List toDestroy = Collections.singletonList(customizationPortletContext);
            PortletInvoker portletInvoker = getContainer().getPortletInvoker();

            // Destroy local customization first
            getContainerContext().destroyInstanceCustomization(customization);

            // Destroy the related customization
            List failures = portletInvoker.destroyClones(toDestroy);
            if (failures.size() > 0)
            {
               for (Iterator i = failures.iterator(); i.hasNext();)
               {
                  DestroyCloneFailure failure = (DestroyCloneFailure)i.next();
                  log.error("Was not able to destroy " + failure.getPortletId() + " for customization " +
                     customizationId + ", reason is : " + failure.getMessage());
               }
            }
         }
         catch (PortletInvokerException e)
         {
            log.error("Could not destroy customization " + customizationId, e);
         }
      }
   }
}

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
package org.jboss.portal.core.model.instance;

import org.jboss.portal.core.model.instance.metadata.InstanceMetaData;
import org.jboss.portal.portlet.PortletInvoker;
import org.jboss.portal.portlet.PortletInvokerException;
import org.jboss.portal.security.spi.provider.AuthorizationDomain;

import java.util.Collection;

/**
 * A container for instances of component.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 10684 $
 */
public interface InstanceContainer
{

   /**
    * Return the underlying portlet invoker for the instance container.
    *
    * @return the portlet invoker
    */
   PortletInvoker getPortletInvoker();

   /**
    * Return the specified instance from its id or null if it does not exist.
    *
    * @param id the instance id
    * @throws IllegalArgumentException if the instance id is null
    */
   InstanceDefinition getDefinition(String id) throws IllegalArgumentException;

   /**
    * Create a new instance of the specified portlet.
    *
    * @param id
    * @param portletId the portlet id
    * @return the newly created instance
    * @throws DuplicateInstanceException if the instance already exist
    * @throws IllegalArgumentException   if the instance id is null
    */
   InstanceDefinition createDefinition(String id, String portletId) throws DuplicateInstanceException, IllegalArgumentException, PortletInvokerException;

   /**
    * Create a new instance of the specified portlet.
    *
    * @param instanceMetaData the instance Metadata
    * @return the newly created instance
    * @throws DuplicateInstanceException if the instance already exist
    * @throws IllegalArgumentException   if the instance id is null
    */
   InstanceDefinition createDefinition(InstanceMetaData instanceMetaData) throws DuplicateInstanceException, IllegalArgumentException, PortletInvokerException;

   /**
    * Create a new instance of the specified portlet.
    *
    * @param id
    * @param portletId the portlet id
    * @param clone     force a clone of the portlet
    * @return the newly created instance
    * @throws DuplicateInstanceException if the instance already exist
    * @throws IllegalArgumentException   if the instance id is null
    */
   InstanceDefinition createDefinition(String id, String portletId, boolean clone) throws DuplicateInstanceException, IllegalArgumentException, PortletInvokerException;

   /**
    * Destroy the specified instance.
    *
    * @param id
    * @throws IllegalArgumentException if the instance id is null
    */
   void destroyDefinition(String id) throws NoSuchInstanceException, PortletInvokerException, IllegalArgumentException;

   /**
    * Return all the instances in the container.
    *
    * @return a collection containing the instances in the container
    */
   Collection<InstanceDefinition> getDefinitions();

   /**
    * Return the AuthorizationDomain
    *
    * @return the authorization domain
    */
   AuthorizationDomain getAuthorizationDomain();
}

/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2009, Red Hat Middleware, LLC, and individual                    *
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

import org.jboss.portal.core.model.HasDisplayName;
import org.jboss.portal.portlet.Portlet;
import org.jboss.portal.portlet.PortletInvokerException;
import org.jboss.portal.portlet.invocation.PortletInvocation;
import org.jboss.portal.portlet.invocation.response.PortletInvocationResponse;
import org.jboss.portal.portlet.state.PropertyChange;
import org.jboss.portal.portlet.state.PropertyMap;

import java.util.Set;

/**
 * A shared portlet instance.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 12832 $
 */
public interface Instance extends HasDisplayName
{
   /** The attribute name under which the instance id can be accessed. */
   String INSTANCE_ID_ATTRIBUTE = "instanceid";

   /**
    * Return the id.
    *
    * @return the id
    */
   String getId();

   /**
    * Return the runtime metadata for this portlet.
    *
    * @return the info
    */
   Portlet getPortlet() throws PortletInvokerException;

   /**
    * Return the container of this object.
    *
    * @return the instance container
    */
   InstanceContainer getContainer();

   /**
    * Invoke the instance
    *
    * @param invocation the invocation
    */
   PortletInvocationResponse invoke(PortletInvocation invocation) throws PortletInvokerException;

   /**
    * Return the instance preferences.
    *
    * @return the prefs
    */
   PropertyMap getProperties() throws PortletInvokerException;

   /**
    * Return the instance preferences.
    *
    * @return the prefs
    */
   PropertyMap getProperties(Set keys) throws PortletInvokerException;

   /**
    * Update the prefs of this instance.
    *
    * @param changes the changes
    */
   void setProperties(PropertyChange[] changes) throws PortletInvokerException;

   /**
    * Return a customization of this instance related to the provided customization id.
    *
    * @return an instance customization
    */
   InstanceCustomization getCustomization(String customizationId);

   /**
    * Destroy the customization
    *
    * @param customizationId the id of the customization to destroy
    */
   void destroyCustomization(String customizationId);
}

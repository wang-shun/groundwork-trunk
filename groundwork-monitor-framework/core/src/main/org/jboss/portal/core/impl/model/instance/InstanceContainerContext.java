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

import org.jboss.portal.core.model.instance.DuplicateInstanceException;
import org.jboss.portal.core.model.instance.InstanceDefinition;
import org.jboss.portal.core.model.instance.InstancePermission;
import org.jboss.portal.core.model.instance.metadata.InstanceMetaData;
import org.jboss.portal.portlet.PortletContext;

import java.util.Collection;
import java.util.Set;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 10684 $
 */
public interface InstanceContainerContext
{

   Collection<InstanceDefinition> getInstanceDefinitions();

   AbstractInstanceDefinition getInstanceDefinition(String id);

   AbstractInstanceDefinition newInstanceDefinition(String id, String portletRef);

   AbstractInstanceDefinition newInstanceDefinition(InstanceMetaData instanceMetaData);

   void createInstanceDefinition(AbstractInstanceDefinition instanceDef) throws DuplicateInstanceException;

   void destroyInstanceDefinition(AbstractInstanceDefinition instanceDef);

   void destroyInstanceCustomization(AbstractInstanceCustomization customization);

   AbstractInstanceCustomization getCustomization(AbstractInstanceDefinition instanceDef, String customizationId);

   AbstractInstanceCustomization newInstanceCustomization(AbstractInstanceDefinition def, String id, PortletContext portletContext);

   void createInstanceCustomizaton(AbstractInstanceCustomization customization);

   void updateInstance(AbstractInstance instance, PortletContext portletContext, boolean mutable);

   void updateInstance(AbstractInstance instance, PortletContext portletContext);

   void updateInstanceDefinition(AbstractInstanceDefinition def, Set securityBindings);

   boolean checkPermission(InstancePermission perm);

}

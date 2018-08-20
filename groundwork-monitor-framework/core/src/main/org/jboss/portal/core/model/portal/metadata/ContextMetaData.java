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
package org.jboss.portal.core.model.portal.metadata;

import org.jboss.portal.common.xml.XMLTools;
import org.jboss.portal.core.model.content.spi.ContentProviderRegistry;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.PortalObjectContainer;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11767 $
 */
public class ContextMetaData extends PortalObjectMetaData
{

   protected PortalObject newInstance(BuildContext buildContext, PortalObject parent) throws Exception
   {
//      if (parent instanceof PortalContainer == false)
//      {
//         throw new IllegalArgumentException("Not a context");
//      }
//
//      //
//      return ((PortalContainer)parent).createPortalContainer(getName());

      // Get portal object container
      PortalObjectContainer portalObjectContainer = buildContext.getContainer();

      // Get namespace value
      String namespace = getName();

      //
      return portalObjectContainer.createContext(namespace);
   }

   /** Parse the following XML elements : context-name. */
   public static ContextMetaData buildContextMetaData(ContentProviderRegistry contentProviderRegistry, Element contextElt) throws Exception
   {
      ContextMetaData contextMD = new ContextMetaData();

      //
      String contextName = XMLTools.asString(XMLTools.getUniqueChild(contextElt, "context-name", true));
      if (contextName != null && contextName.indexOf(".") < 0 && contextName.indexOf(":") < 0)
      {
         contextMD.setName(contextName);
      }
      else
      {
         throw new IllegalArgumentException("Invalid context-name: '" + contextName
            + "'. Must not be null and must not contain a '.' or ':'");
      }

      //
      for (Element portalElt : XMLTools.getChildren(contextElt, "portal"))
      {
         PortalMetaData pageMD = (PortalMetaData)PortalObjectMetaData.buildMetaData(contentProviderRegistry, portalElt);
         contextMD.getChildren().put(pageMD.getName(), pageMD);
      }

      //
      return contextMD;
   }
}

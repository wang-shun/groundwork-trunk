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
package org.jboss.portal.core.model.portal.command.mapping;

import org.jboss.portal.common.text.FastURLEncoder;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.PortalObjectContainer;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.server.servlet.PathMapping;
import org.jboss.portal.server.servlet.PathMappingResult;
import org.jboss.portal.server.servlet.PathParser;

import java.util.Iterator;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8811 $
 */
public class DefaultPortalObjectPathMapper implements PortalObjectPathMapper
{

   /** . */
   protected String namespace;

   /** . */
   protected PortalObjectContainer container;

   /** . */
   protected PathMapping mapping;

   /** . */
   protected PathParser pathParser = new PathParser();

   /** . */
   protected String effectiveNamespace;

   public String getNamespace()
   {
      return namespace;
   }

   public void setNamespace(String namespace)
   {
      this.namespace = namespace;
   }

   public PortalObjectContainer getContainer()
   {
      return container;
   }

   public void setContainer(PortalObjectContainer container)
   {
      this.container = container;
   }

   public void start()
   {
      effectiveNamespace = namespace == null ? "" : namespace;
      mapping = new PathMapping()
      {
         public Object getRoot()
         {
            return container.getContext(namespace != null ? namespace : "");
         }

         public Object getChild(Object parent, String name)
         {
            PortalObject po = (PortalObject)parent;
            return po.getChild(name);
         }
      };
   }

   public void stop()
   {
      mapping = null;
   }

   public PortalObject getTarget(ControllerContext controllerContext, String path)
   {
      if (path.length() == 0 || "/".equals(path))
      {
         return container.getContext(effectiveNamespace);
      }
      else
      {
         PathMappingResult result = pathParser.map(mapping, path);
         return (PortalObject)result.getTarget();
      }
   }

   public PortalObject getDefaultTarget()
   {
      return container.getContext(namespace);
   }

   public PathMapping createPathMapper(ControllerContext controllerContext)
   {
      return mapping;
   }

   public void appendPath(StringBuffer buffer, PortalObjectId id)
   {
      for (Iterator i = id.getPath().names(); i.hasNext();)
      {
         String name = (String)i.next();
         name = FastURLEncoder.getUTF8Instance().encode(name);
         buffer.append('/').append(name);
      }
   }
}

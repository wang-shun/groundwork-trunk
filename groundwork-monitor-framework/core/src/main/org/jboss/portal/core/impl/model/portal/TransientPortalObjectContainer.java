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
package org.jboss.portal.core.impl.model.portal;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentHashMap;
import org.jboss.portal.core.model.portal.DuplicatePortalObjectException;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.PortalObjectPath;

import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class TransientPortalObjectContainer extends AbstractPortalObjectContainer
{

   /** . */
   protected Map roots;

   /** . */
   protected ContainerContext ctx;

   protected void createService() throws Exception
   {
      super.createService();

      //
      ctx = new ContainerContext();

      //
      roots = new ConcurrentHashMap();
   }


   protected ContextImpl createRoot(String namespace) throws DuplicatePortalObjectException
   {
      if (roots.containsKey(namespace))
      {
         throw new DuplicatePortalObjectException();
      }
      else
      {
         ObjectNode root = new ObjectNode(ctx, new PortalObjectId(namespace, PortalObjectPath.ROOT_PATH), namespace + ":");
         ContextImpl ctx = new ContextImpl(false);
         root.setObject(ctx);
         ctx.setObjectNode(root);
         roots.put(namespace, root);
         return ctx;
      }
   }

   protected ObjectNode getObjectNode(PortalObjectId id)
   {
      ObjectNode node = (ObjectNode)roots.get(id.getNamespace());
      for (Iterator i = id.getPath().names(); node != null && i.hasNext();)
      {
         String name = (String)i.next();
         node = (ObjectNode)node.getChildren().get(name);
      }

      //
      return node;
   }
}

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
package org.jboss.portal.core.impl.api.node;

import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.command.view.ViewPageCommand;
import org.jboss.portal.server.request.URLContext;
import org.jboss.portal.server.request.URLFormat;

import java.util.Map;
import java.util.Collections;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11549 $
 */
public class PageURL extends AbstractPortalNodeURL
{

   /** . */
   private static final Map<String, String[]> EMPTY_PARAMETERS = Collections.emptyMap();

   public PageURL(PortalObjectId id, ControllerContext controllerContext)
   {
      super(id, controllerContext);
   }

   public String toString()
   {
      Map<String, String[]> parameters = this.parameters != null ? this.parameters : EMPTY_PARAMETERS;
      ViewPageCommand cmd = new ViewPageCommand(id, parameters);
      URLContext urlContext = getURLContext();
      return controllerContext.renderURL(cmd, urlContext, URLFormat.newInstance(relative, true));
   }
}

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

package org.jboss.portal.core.model.portal.command.view;

import org.jboss.portal.core.controller.ControllerCommand;
import org.jboss.portal.core.controller.ControllerException;
import org.jboss.portal.core.controller.ControllerResponse;
import org.jboss.portal.core.controller.command.info.CommandInfo;
import org.jboss.portal.core.controller.command.info.ViewCommandInfo;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.command.PageCommand;
import org.jboss.portal.core.model.portal.command.response.UpdatePageResponse;
import org.jboss.portal.core.model.portal.navstate.PageNavigationalState;
import org.jboss.portal.core.navstate.NavigationalStateContext;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 12958 $
 */
public class ViewPageCommand extends PageCommand
{

   /** . */
   private static final CommandInfo info = new ViewCommandInfo();

   /** . */
   private static final Map<String, String[]> EMPTY_PARAMETERS = Collections.emptyMap();

   /** . */
   private Map<String, String[]> parameters;

   public ViewPageCommand(PortalObjectId pageId, Map<String, String[]> parameters)
   {
      super(pageId);

      //
      if (parameters == null)
      {
         throw new IllegalArgumentException("No null parameters accepted");
      }

      //
      this.parameters = parameters;
   }

   public ViewPageCommand(PortalObjectId pageId)
   {
      this(pageId, EMPTY_PARAMETERS);
   }

   protected Page initPage()
   {
      return (Page)getTarget();
   }

   public CommandInfo getInfo()
   {
      return info;
   }

   public Map<String, String[]> getParameters()
   {
      return parameters;
   }

   public ControllerResponse execute() throws ControllerException
   {
      NavigationalStateContext nsContext = (NavigationalStateContext)context.getAttributeResolver(ControllerCommand.NAVIGATIONAL_STATE_SCOPE);

      String pageId = getPage().getId().toString();

      if (parameters.size() > 0)
      {
         Map<QName, String[]> state = new HashMap<QName, String[]>();

         for (Map.Entry<String, String[]> entry : parameters.entrySet())
         {
            state.put(new QName(XMLConstants.DEFAULT_NS_PREFIX, entry.getKey()), entry.getValue());
         }

         nsContext.setPageNavigationalState(pageId, new PageNavigationalState(state));
      }
      else
      {
         nsContext.setPageNavigationalState(pageId, null);
      }

      //
      return new UpdatePageResponse(page.getId());
   }
}

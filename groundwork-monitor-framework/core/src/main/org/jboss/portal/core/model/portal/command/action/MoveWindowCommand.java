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
package org.jboss.portal.core.model.portal.command.action;

import org.jboss.portal.common.util.ListMap;
import org.jboss.portal.core.controller.ControllerException;
import org.jboss.portal.core.controller.ControllerResponse;
import org.jboss.portal.core.controller.command.info.ActionCommandInfo;
import org.jboss.portal.core.controller.command.info.CommandInfo;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.Window;
import org.jboss.portal.core.model.portal.command.WindowCommand;
import org.jboss.portal.theme.ThemeConstants;
import org.jboss.portal.theme.ThemeTools;

import java.util.Comparator;
import java.util.Iterator;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class MoveWindowCommand extends WindowCommand
{

   /** . */
   private static final CommandInfo info = new ActionCommandInfo(false);

   /** . */
   private int fromPos;

   /** . */
   private String fromRegion;

   /** . */
   private int toPos;

   /** . */
   private String toRegion;

   public MoveWindowCommand(PortalObjectId windowId, int fromPos, String fromRegion, int toPos, String toRegion)
      throws IllegalArgumentException
   {
      super(windowId);

      //
      this.fromPos = fromPos;
      this.fromRegion = fromRegion;
      this.toPos = toPos;
      this.toRegion = toRegion;
   }

   public CommandInfo getInfo()
   {
      return info;
   }

   public ControllerResponse execute() throws ControllerException
   {
      if (isDashboard())
      {
         // First relayout all windows correctly except the target window
         ListMap blah = new ListMap(tmp);
         for (Iterator i = page.getChildren(PortalObject.WINDOW_MASK).iterator(); i.hasNext();)
         {
            Window window = (Window)i.next();
            if (window != target)
            {
               String region = window.getDeclaredProperty(ThemeConstants.PORTAL_PROP_REGION);
               if (region != null)
               {
                  blah.put(region, window);
               }
            }
         }

         //
         for (Iterator i = blah.keySet().iterator(); i.hasNext();)
         {
            String key = (String)i.next();

            //
            boolean processFrom = key.equals(fromRegion);
            boolean processTo = key.equals(toRegion);

            //
            if (!processFrom && !processTo)
            {
               int order = 0;
               for (Iterator j = blah.iterator(key); j.hasNext();)
               {
                  Window window = (Window)j.next();
                  window.setDeclaredProperty(ThemeConstants.PORTAL_PROP_ORDER, Integer.toString(order++));
               }
            }
            else
            {
               if (processFrom)
               {
                  int order = 0;
                  for (Iterator j = blah.iterator(key); j.hasNext();)
                  {
                     Window window = (Window)j.next();

                     //
                     if (window == target)
                     {
                        order--;
                     }
                     else
                     {
                        window.setDeclaredProperty(ThemeConstants.PORTAL_PROP_ORDER, Integer.toString(order++));
                     }
                  }
               }
               if (processTo)
               {
                  int order = 0;
                  for (Iterator j = blah.iterator(key); j.hasNext();)
                  {
                     Window window = (Window)j.next();

                     //
                     if (order == toPos)
                     {
                        order++;
                     }

                     //
                     window.setDeclaredProperty(ThemeConstants.PORTAL_PROP_ORDER, Integer.toString(order++));
                  }
               }
            }
         }

         //
         target.setDeclaredProperty(ThemeConstants.PORTAL_PROP_REGION, toRegion);
         target.setDeclaredProperty(ThemeConstants.PORTAL_PROP_ORDER, Integer.toString(toPos));
      }

      //
      return null;
   }

   private static final Comparator tmp = new Comparator()
   {
      public int compare(Object o1, Object o2)
      {
         Window window1 = (Window)o1;
         Window window2 = (Window)o2;
         String order1 = window1.getDeclaredProperty(ThemeConstants.PORTAL_PROP_ORDER);
         String order2 = window2.getDeclaredProperty(ThemeConstants.PORTAL_PROP_ORDER);
         return ThemeTools.compareWindowOrder(order1, window1.getName(), order2, window2.getName());
      }
   };
}

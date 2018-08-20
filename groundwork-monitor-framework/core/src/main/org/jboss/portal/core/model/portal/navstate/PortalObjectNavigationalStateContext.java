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
package org.jboss.portal.core.model.portal.navstate;

import org.jboss.logging.Logger;
import org.jboss.portal.WindowState;
import org.jboss.portal.common.invocation.AttributeResolver;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.core.navstate.NavigationalStateContext;
import org.jboss.portal.core.navstate.NavigationalStateKey;
import org.jboss.portal.core.navstate.NavigationalStateObjectChange;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 12958 $
 */
public class PortalObjectNavigationalStateContext implements NavigationalStateContext
{
   /** . */
   private static final Logger logger = Logger.getLogger(PortalObjectNavigationalStateContext.class);

   /** . */
   private static final String VIEW_ID_KEY = "view_id";

   /** Where we delegate the storage. */
   private AttributeResolver store;

   /** The changes. */
   private LinkedHashMap<NavigationalStateKey, NavigationalStateObjectChange> changes;

   public PortalObjectNavigationalStateContext(AttributeResolver store)
   {
      this.store = store;
   }

   public Set getKeys()
   {
      Set storeKeys = store.getKeys();
      Set<String> keys = new HashSet<String>(storeKeys.size() + changes.size());
      keys.addAll(storeKeys);
      for (NavigationalStateKey key : changes.keySet())
      {
         keys.add(key.getId().toString());
      }
      return keys;
   }

   public Object getAttribute(Object attrKey) throws IllegalArgumentException
   {
      NavigationalStateKey nsKey = (NavigationalStateKey)attrKey;

      // Browse changes first
      if (changes != null)
      {
         NavigationalStateObjectChange change = changes.get(nsKey);
         if (change != null)
         {
            switch (change.getType())
            {
               case NavigationalStateObjectChange.CREATE:
               case NavigationalStateObjectChange.UPDATE:
                  return change.getNewValue();
               case NavigationalStateObjectChange.DESTROY:
                  return null;
            }
         }
      }

      //
      PortalObjectId id = (PortalObjectId)nsKey.getId();
      return store.getAttribute(id.toString());
   }

   public void setAttribute(Object attrKey, Object newNS) throws IllegalArgumentException
   {
      NavigationalStateKey wantedKey = (NavigationalStateKey)attrKey;

      //
      Class typeClass = wantedKey.getType();
      if (typeClass != WindowNavigationalState.class && typeClass != PageNavigationalState.class)
      {
         throw new IllegalArgumentException("Can only set WindowNavigationalSate or PageNavigationalState attributes, was given " + typeClass.getName());
      }

      //
      Object oldNS = null;

      // Look first the old ns in the changes
      if (changes != null)
      {
         NavigationalStateObjectChange change = changes.get(wantedKey);
         if (change != null)
         {
            // Discard any change done so far
            changes.remove(wantedKey);

            //
            switch (change.getType())
            {
               case NavigationalStateObjectChange.CREATE:
               case NavigationalStateObjectChange.UPDATE:
                  oldNS = change.getOldValue();
            }
         }
      }

      // If we don't have the old ns then we try the store
      if (oldNS == null)
      {
         PortalObjectId id = (PortalObjectId)wantedKey.getId();
         Object storedNS = store.getAttribute(id.toString());
         if (storedNS instanceof WindowNavigationalState || storedNS instanceof PageNavigationalState)
         {
            oldNS = storedNS;
         }
      }

      //
      NavigationalStateObjectChange change;
      if (newNS == null)
      {
         if (oldNS != null)
         {
            change = NavigationalStateObjectChange.newDestroy(wantedKey, oldNS);
         }
         else
         {
            // Remove a non existing value, we do nothing
            change = null;
         }
      }
      else
      {
         if (oldNS != null)
         {
            change = NavigationalStateObjectChange.newUpdate(wantedKey, oldNS, newNS);
         }
         else
         {
            change = NavigationalStateObjectChange.newCreate(wantedKey, newNS);
         }
      }

      // Store the change
      if (change != null)
      {
         if (changes == null)
         {
            changes = new LinkedHashMap<NavigationalStateKey, NavigationalStateObjectChange>();
         }

         //
         changes.put(wantedKey, change);
      }
   }

   public Iterator<NavigationalStateObjectChange> getChanges()
   {
      if (changes == null)
      {
         return null;
      }
      return changes.values().iterator();
   }

   public WindowNavigationalState getWindowNavigationalState(String windowId)
   {
      NavigationalStateKey key = createWindowKey(windowId);
      return (WindowNavigationalState)getAttribute(key);
   }

   public void setWindowNavigationalState(String windowId, WindowNavigationalState windowNavigationalState)
   {
      NavigationalStateKey key = createWindowKey(windowId);
      setAttribute(key, windowNavigationalState);
   }

   public PageNavigationalState getPageNavigationalState(String pageId)
   {
      NavigationalStateKey key = createPageKey(pageId);
      return (PageNavigationalState)getAttribute(key);
   }

   public void setPageNavigationalState(String pageId, PageNavigationalState pageNavigationalState)
   {
      NavigationalStateKey key = createPageKey(pageId);
      setAttribute(key, pageNavigationalState);
   }

   /**
    * Apply the navigational state changes to the real storage.
    *
    * @return true if state changed
    */
   public boolean applyChanges()
   {
      if (changes != null && changes.size() > 0)
      {
         Object maximizedKey = null;
         for (NavigationalStateObjectChange change : changes.values())
         {
            if (change.getNewValue() instanceof WindowNavigationalState)
            {
               WindowNavigationalState wns = (WindowNavigationalState)change.getNewValue();
               if (wns.getWindowState().equals(WindowState.MAXIMIZED))
               {
                  if (maximizedKey != null)
                  {
                     // Should never happen, 2 windows shouldn't be maximized at once
                     logger.error("Two windows are maximized at once, it should not be possible");
                  }
                  maximizedKey = change.getKey();
               }
            }

            //
            PortalObjectId id = (PortalObjectId)change.getKey().getId();
            store.setAttribute(id.toString(), change.getNewValue());
         }

         // Unmaximize other windows if necessary
         if (maximizedKey != null)
         {
            Set keys = store.getKeys();
            for (Object key : keys)
            {
               Object object = store.getAttribute(key);
               if (object instanceof WindowNavigationalState)
               {
                  WindowNavigationalState wns = (WindowNavigationalState)object;
                  if (!key.equals(maximizedKey) && wns.getWindowState().equals(WindowState.MAXIMIZED))
                  {
                     WindowNavigationalState wns2 = new WindowNavigationalState(WindowState.NORMAL, wns.getMode(), wns
                        .getContentState(), wns.getPublicContentState());
                     store.setAttribute(key, wns2);
                  }
               }
            }
         }

         //
         changes.clear();

         // Increase view id
         Integer viewId = (Integer)store.getAttribute(VIEW_ID_KEY);
         if (viewId == null)
         {
            viewId = 0;
         }
         else
         {
            viewId = viewId + 1;
         }
         store.setAttribute(VIEW_ID_KEY, viewId);

         //
         return true;
      }
      else
      {
         return false;
      }
   }

   public String getViewId()
   {
      Integer viewId = (Integer)store.getAttribute(VIEW_ID_KEY);
      return viewId != null ? viewId.toString() : "0";
   }

   private NavigationalStateKey createWindowKey(String windowId)
   {
      return new NavigationalStateKey(WindowNavigationalState.class, PortalObjectId.parse(windowId, PortalObjectPath.CANONICAL_FORMAT));
   }

   private NavigationalStateKey createPageKey(String pageId)
   {
      return new NavigationalStateKey(PageNavigationalState.class, PortalObjectId.parse(pageId, PortalObjectPath.CANONICAL_FORMAT));
   }
}

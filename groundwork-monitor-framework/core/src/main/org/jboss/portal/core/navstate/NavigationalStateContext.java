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
package org.jboss.portal.core.navstate;

import org.jboss.portal.common.invocation.AttributeResolver;
import org.jboss.portal.core.model.portal.navstate.WindowNavigationalState;
import org.jboss.portal.core.model.portal.navstate.PageNavigationalState;

import java.util.Iterator;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 10540 $
 */
public interface NavigationalStateContext extends AttributeResolver
{
   /**
    * Retrieves the navigational state associated with the specified window identifier.
    *
    * @param windowId a String identifying the window which navigational state is to be retrieved
    * @return the navigational state associated with the specified window identifier or <code>null</code> if the given
    *         window identifier is not known by this NavigationalStateContext or no navigational state is associated
    *         with the given identifier.
    */
   WindowNavigationalState getWindowNavigationalState(String windowId);

   /**
    * Set the navigational state associated with the window identified by the given identifier.
    *
    * @param windowId                the window identifier
    * @param windowNavigationalState the window navigational state
    */
   void setWindowNavigationalState(String windowId, WindowNavigationalState windowNavigationalState);

   /**
    * Retrieves the navigational state associated with the specified page identifier.
    *
    * @param pageId a String identifying the page which navigational state is to be retrieved
    * @return the navigational state associated with the specified window identifier or <code>null</code> if the given
    *         page identifier is not known by this NavigationalStateContext or no navigational state is associated
    *         with the given identifier.
    */
   PageNavigationalState getPageNavigationalState(String pageId);

   /**
    * Set the navigational state associated with the page identified by the given identifier.
    *
    * @param pageId                the page identifier
    * @param pageNavigationalState the page navigational state
    */
   void setPageNavigationalState(String pageId, PageNavigationalState pageNavigationalState);

   /**
    * Apply the navigational state changes made to this NavigationalStateContext.
    *
    * @return true if state changed
    */
   boolean applyChanges();

   /**
    * Returns the current view id.
    *
    * @return the view id
    */
   String getViewId();

   /**
    * Retrieve an iterator over the current changes.
    *
    * @return an iterator over the current changes.
    */
   Iterator<? extends NavigationalStateChange> getChanges();
}

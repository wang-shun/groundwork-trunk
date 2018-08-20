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
package org.jboss.portal.core.model.content.spi.handler;

import org.jboss.portal.core.model.content.Content;

/**
 * The content handler act as a factory for <code>Content</code> objects from their state. The interface receives also
 * callbacks of the content lifecycle in order to be able to manage additional resources related to the content.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public interface ContentHandler
{
   /**
    * Factory method that creates an instance of content object. This method is called whenever the frameworks needs a
    * runtime representation of the content state which can be used at runtime by content clients.
    *
    * @param contextId the context id in which the state is used
    * @param state     the state
    * @return the content interface implementation
    */
   Content newContent(String contextId, ContentState state);

   /**
    * Life cycle method to signal state creation.
    *
    * @param contextId the context id in which the state is created
    * @param state     the state
    */
   void contentCreated(String contextId, ContentState state);

   /**
    * Life cycle method to signal state destruction.
    *
    * @param contextId the context id in which the state is destroyed
    * @param state     the state
    */
   void contentDestroyed(String contextId, ContentState state);
}

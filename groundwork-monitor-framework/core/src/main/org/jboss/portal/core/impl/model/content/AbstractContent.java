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
package org.jboss.portal.core.impl.model.content;

import org.jboss.portal.core.model.content.Content;
import org.jboss.portal.core.model.content.spi.handler.ContentState;

import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11168 $
 */
public abstract class AbstractContent implements Content
{

   /** . */
   protected final ContentState state;

   public AbstractContent(ContentState state)
   {
      this.state = state;
   }

   public boolean isMutable()
   {
      return true;
   }

   public String getURI()
   {
      return state.getURI();
   }

   public void setURI(String uri)
   {
      state.setURI(uri);
   }

   public void clearParameters()
   {
      state.clearParameters();
   }

   public String getParameter(String name) throws IllegalArgumentException
   {
      return state.getParameter(name);
   }

   public void setParameter(String name, String value) throws IllegalArgumentException
   {
      state.setParameter(name, value);
   }

   public void setParameters(Map<String, String> parameters) throws IllegalArgumentException
   {
      state.setParameters(parameters);
   }

   public Iterator<String> getParameterNames()
   {
      return state.getParameterNames();
   }
}

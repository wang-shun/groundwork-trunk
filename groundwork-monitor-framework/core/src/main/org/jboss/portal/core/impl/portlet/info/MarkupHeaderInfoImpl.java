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
package org.jboss.portal.core.impl.portlet.info;

import org.jboss.portal.core.metadata.portlet.ElementMetaData;
import org.jboss.portal.core.metadata.portlet.HeaderContentMetaData;
import org.jboss.portal.core.metadata.portlet.MarkupElement;
import org.jboss.portal.core.portlet.info.MarkupHeaderInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 10269 $
 */
public class MarkupHeaderInfoImpl implements MarkupHeaderInfo
{

   /** . */
   private final Collection<MarkupElement> markupElements;

   public MarkupHeaderInfoImpl(HeaderContentMetaData headerContentMD)
   {
      ArrayList<MarkupElement> markupElements = new ArrayList<MarkupElement>(headerContentMD.getElements().size());
      for (ElementMetaData elementMetaData : headerContentMD.getElements())
      {
         markupElements.add(elementMetaData.getElement());
      }

      //
      this.markupElements = Collections.unmodifiableCollection(markupElements);
   }

   public Collection<MarkupElement> getMarkupElements()
   {
      return markupElements;
   }
}

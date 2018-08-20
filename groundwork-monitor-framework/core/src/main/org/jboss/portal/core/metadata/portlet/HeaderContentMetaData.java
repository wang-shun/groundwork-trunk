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
package org.jboss.portal.core.metadata.portlet;

import java.util.ArrayList;
import java.util.List;

/**
 * Meta data to describe a portlet's data to inject into the header of the page. <p>A portlet can define additional
 * script or link tags in its descriptor (jboss-portlet.xml) that will be injected into the head (via a separate jsp
 * tag)</p>
 *
 * @author <a href="mailto:mholzner@novell.com>Martin Holzner</a>
 * @version $LastChangedRevision: 10269 $, $LastChangedDate: 2008-03-12 02:59:37 -0400 (Wed, 12 Mar 2008) $
 */
public class HeaderContentMetaData
{

   /** . */
   private List<ElementMetaData> elements;

   public HeaderContentMetaData()
   {
      elements = new ArrayList<ElementMetaData>();
   }

   /**
    * Get the list of header elements to inject into the header
    *
    * @return a list of header elements (HeaderContentMetaData.Element)
    */
   public List<ElementMetaData> getElements()
   {
      return elements;
   }
}

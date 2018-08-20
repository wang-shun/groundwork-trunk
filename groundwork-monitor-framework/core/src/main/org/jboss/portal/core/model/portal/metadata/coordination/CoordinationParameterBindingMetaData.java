/*
* JBoss, a division of Red Hat
* Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/

package org.jboss.portal.core.model.portal.metadata.coordination;

import org.jboss.portal.common.xml.XMLTools;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:boleslaw dot dawidowicz at redhat anotherdot com">Boleslaw Dawidowicz</a>
 * @version : 0.1 $
 */
public class CoordinationParameterBindingMetaData
{
   private String name;

   List<CoordinationWindowMultiQNameMetaData> windows = new LinkedList<CoordinationWindowMultiQNameMetaData>();

   public CoordinationParameterBindingMetaData(String name)
   {
      this.name = name;
   }

   public List<CoordinationWindowMultiQNameMetaData> getWindows()
   {
      return windows;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   private void addWindow(CoordinationWindowMultiQNameMetaData window)
   {
      windows.add(window);
   }

   public static CoordinationParameterBindingMetaData buildMetaData(Element bindingElement)
   {
      Element nameElt = XMLTools.getUniqueChild(bindingElement, "id", true);
      CoordinationParameterBindingMetaData paramMetaData = new CoordinationParameterBindingMetaData(XMLTools.asString(nameElt));

      Iterator windowIter = XMLTools.getChildrenIterator(bindingElement, "window-coordination");

      while (windowIter.hasNext())
      {
         Element element = (Element)windowIter.next();

         paramMetaData.addWindow(CoordinationWindowMultiQNameMetaData.buildMetaData(element));
      }

      return paramMetaData;
   }
}

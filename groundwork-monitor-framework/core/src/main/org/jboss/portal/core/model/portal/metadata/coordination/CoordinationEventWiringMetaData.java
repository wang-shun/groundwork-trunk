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

import org.w3c.dom.Element;
import org.jboss.portal.common.xml.XMLTools;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

/**
 * @author <a href="mailto:boleslaw dot dawidowicz at redhat anotherdot com">Boleslaw Dawidowicz</a>
 * @version : 0.1 $
 */

public class CoordinationEventWiringMetaData
{

   private String name;

   private List<CoordinationWindowSimpleQNameMetaData> sources = new LinkedList<CoordinationWindowSimpleQNameMetaData>();

   private List<CoordinationWindowSimpleQNameMetaData> destinations = new LinkedList<CoordinationWindowSimpleQNameMetaData>();

   public CoordinationEventWiringMetaData(String name)
   {
      this.name = name;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public List<CoordinationWindowSimpleQNameMetaData> getSources()
   {
      return sources;
   }

   private void addSource(CoordinationWindowSimpleQNameMetaData window)
   {
      this.sources.add(window);
   }

   public List<CoordinationWindowSimpleQNameMetaData> getDestinations()
   {
      return destinations;
   }

   private void addDestination(CoordinationWindowSimpleQNameMetaData window)
   {
      this.destinations.add(window);
   }

   public static CoordinationEventWiringMetaData buildMetaData(Element wiringElement)
   {
      Element nameElt = XMLTools.getUniqueChild(wiringElement, "name", true);

      CoordinationEventWiringMetaData wiringMD = new CoordinationEventWiringMetaData(XMLTools.asString(nameElt));

      Element sourcesElt = XMLTools.getUniqueChild(wiringElement, "sources", true);

      Iterator windowIter = XMLTools.getChildrenIterator(sourcesElt, "window-coordination");

      while (windowIter.hasNext())
      {
         Element element = (Element)windowIter.next();

         wiringMD.addSource(CoordinationWindowSimpleQNameMetaData.buildMetaData(element));
      }

      Element destinationsElt = XMLTools.getUniqueChild(wiringElement, "destinations", true);

      windowIter = XMLTools.getChildrenIterator(destinationsElt, "window-coordination");

      while (windowIter.hasNext())
      {
         Element element = (Element)windowIter.next();

         wiringMD.addDestination(CoordinationWindowSimpleQNameMetaData.buildMetaData(element));
      }

      return wiringMD;


   }
}

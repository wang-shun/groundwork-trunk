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
import org.jboss.portal.core.controller.coordination.IllegalCoordinationException;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.PageContainer;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.Window;
import org.jboss.portal.core.model.portal.metadata.BuildContext;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:boleslaw dot dawidowicz at redhat anotherdot com">Boleslaw Dawidowicz</a>
 * @version : 0.1 $
 */
public class CoordinationMetaData
{

   private String implicitModeEvent;

   private String implicitModeParameter;

   List<CoordinationParameterBindingMetaData> parameterBindings = new LinkedList<CoordinationParameterBindingMetaData>();

   List<CoordinationAliasBindingMetaData> aliasBindings = new LinkedList<CoordinationAliasBindingMetaData>();

   List<CoordinationEventWiringMetaData> wirings = new LinkedList<CoordinationEventWiringMetaData>();

   public static CoordinationMetaData buildMetaData(Element coordinationElt)
   {

      CoordinationMetaData coordinationMetaData = new CoordinationMetaData();

      Element wiringsElement = XMLTools.getUniqueChild(coordinationElt, "wirings", false);

      if (wiringsElement != null)
      {
         // Implicit mode

         Element wiringImplicitModeElt = XMLTools.getUniqueChild(wiringsElement, "implicit-mode", false);

         if (wiringImplicitModeElt != null)
         {
            coordinationMetaData.setImplicitModeEvent(XMLTools.asString(wiringImplicitModeElt));
         }

         // The rest

         coordinationMetaData.setWirings(buildWiringsMetaData(wiringsElement));
      }


      Element bindingsElement = XMLTools.getUniqueChild(coordinationElt, "bindings", false);

      if (bindingsElement != null)
      {

         // Implicit mode

         Element parameterImplicitModeElt = XMLTools.getUniqueChild(bindingsElement, "implicit-mode", false);

         if (parameterImplicitModeElt != null)
         {
            coordinationMetaData.setImplicitModeParameter(XMLTools.asString(parameterImplicitModeElt));
         }

         // Param bindings

         Iterator paramBindingsIter = XMLTools.getChildrenIterator(bindingsElement, "parameter-binding");
         while (paramBindingsIter.hasNext())
         {
            Element bindingElement = (Element)paramBindingsIter.next();
            coordinationMetaData.addParameterBinding(CoordinationParameterBindingMetaData.buildMetaData(bindingElement));
         }

         // Alias bindings
         Iterator aliasBindingsIter = XMLTools.getChildrenIterator(bindingsElement, "alias-binding");
         while (aliasBindingsIter.hasNext())
         {
            Element aliasElement = (Element)aliasBindingsIter.next();
            coordinationMetaData.addAliasBinding(CoordinationAliasBindingMetaData.buildMetaData(aliasElement));
         }

      }


      return coordinationMetaData;
   }

   private static List<CoordinationEventWiringMetaData> buildWiringsMetaData(Element wiringsElement)
   {
      List<CoordinationEventWiringMetaData> w = new LinkedList<CoordinationEventWiringMetaData>();

      Iterator wiringsIter = XMLTools.getChildrenIterator(wiringsElement, "event-wiring");

      while (wiringsIter.hasNext())
      {
         Element element = (Element)wiringsIter.next();

         w.add(CoordinationEventWiringMetaData.buildMetaData(element));
      }

      return w;
   }

   public void configure(BuildContext buildContext, PortalObject object) throws IllegalCoordinationException
   {
      if (getImplicitModeEvent() != null)
      {
         buildContext.getCoordinationConfigurator().setEventWiringImplicitMode((PageContainer)object, Boolean.valueOf(getImplicitModeEvent()));
      }

      if (getImplicitModeParameter() != null)
      {
         buildContext.getCoordinationConfigurator().setParameterBindingImplicitMode((PageContainer)object, Boolean.valueOf(getImplicitModeParameter()));
      }

      if (object instanceof Page)
      {
         Page page = (Page)object;

         for (CoordinationParameterBindingMetaData parameterBinding : parameterBindings)
         {
            Map<Window, Set<QName>> bindings = new HashMap<Window, Set<QName>>();

            for (CoordinationWindowMultiQNameMetaData windowMD : parameterBinding.getWindows())
            {
               String windowName = windowMD.getWindowName();
               Window window = page.getWindow(windowName);

               if (window == null)
               {
                  throw new IllegalCoordinationException("Cannot obtain window: \"" + windowName + "\" on page: "
                          + page.getName());
               }

               bindings.put(window, windowMD.getNames());
            }

            buildContext.getCoordinationConfigurator().setParameterBinding(parameterBinding.getName(), bindings);
         }

         for (CoordinationAliasBindingMetaData windowBinding : aliasBindings)
         {
            buildContext.getCoordinationConfigurator().setAliasBinding(page, windowBinding.getName(), windowBinding.getQnames());
         }

         for (CoordinationEventWiringMetaData wiring : wirings)
         {
            Map<Window, QName> sources = new HashMap<Window, QName>();
            Map<Window, QName> destinations = new HashMap<Window, QName>();

            populateWindows(sources, wiring.getSources(), page);
            populateWindows(destinations, wiring.getDestinations(), page);

            buildContext.getCoordinationConfigurator().setEventWiring(sources, destinations, wiring.getName());

         }

      }

   }

   private static void populateWindows(Map<Window, QName> map, List<CoordinationWindowSimpleQNameMetaData> windows, Page page) throws IllegalCoordinationException
   {
      for (CoordinationWindowSimpleQNameMetaData windowMD : windows)
      {
         QName name = windowMD.getQname();
         Window window = page.getWindow(windowMD.getWindowName());

         if (window == null)
         {
            throw new IllegalCoordinationException("Cannot obtain window: \"" + windowMD.getWindowName() + "\" on page: "
                    + page.getName());
         }

         map.put(window, name);
      }
   }

   public String getImplicitModeEvent()
   {
      return implicitModeEvent;
   }

   public void setImplicitModeEvent(String implicitModeEvent)
   {
      this.implicitModeEvent = implicitModeEvent;
   }

   public String getImplicitModeParameter()
   {
      return implicitModeParameter;
   }

   public void setImplicitModeParameter(String implicitModeParameter)
   {
      this.implicitModeParameter = implicitModeParameter;
   }


   public List<CoordinationEventWiringMetaData> getWirings()
   {
      return wirings;
   }

   public void setWirings(List<CoordinationEventWiringMetaData> wirings)
   {
      this.wirings = wirings;
   }

   public void addWirings(CoordinationEventWiringMetaData wiring)
   {
      this.wirings.add(wiring);
   }

   public List<CoordinationParameterBindingMetaData> getParameterBindings()
   {
      return parameterBindings;
   }

   public void setParameterBindings(List<CoordinationParameterBindingMetaData> parameterBindings)
   {
      this.parameterBindings = parameterBindings;
   }

   public void addParameterBinding(CoordinationParameterBindingMetaData binding)
   {
      this.parameterBindings.add(binding);
   }

   public List<CoordinationAliasBindingMetaData> getAliasBindings()
   {
      return aliasBindings;
   }

   public void setAliasBindings(List<CoordinationAliasBindingMetaData> aliasBindings)
   {
      this.aliasBindings = aliasBindings;
   }

   public void addAliasBinding(CoordinationAliasBindingMetaData aliasBinding)
   {
      this.aliasBindings.add(aliasBinding);
   }
}

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

package org.jboss.portal.core.impl.coordination;

import org.jboss.portal.common.util.ParameterValidation;
import org.jboss.portal.core.controller.coordination.AliasBindingInfo;
import org.jboss.portal.core.controller.coordination.CoordinationConfigurator;
import org.jboss.portal.core.controller.coordination.CoordinationManager;
import org.jboss.portal.core.controller.coordination.EventConverter;
import org.jboss.portal.core.controller.coordination.EventWiringInfo;
import org.jboss.portal.core.controller.coordination.IllegalCoordinationException;
import org.jboss.portal.core.controller.coordination.ParameterBindingInfo;
import org.jboss.portal.core.controller.coordination.Utils;
import org.jboss.portal.core.controller.portlet.ControllerPortletControllerContext;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.PageContainer;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.PortalObjectContainer;
import org.jboss.portal.core.model.portal.Window;
import org.jboss.portal.jems.as.system.AbstractJBossService;
import org.jboss.portal.portlet.controller.event.PortletWindowEvent;
import org.jboss.portal.portlet.info.EventInfo;
import org.jboss.portal.portlet.info.PortletInfo;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:boleslaw dot dawidowicz at redhat anotherdot com">Boleslaw Dawidowicz</a>
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version : 0.1 $
 */
public class CoordinationService extends AbstractJBossService implements CoordinationManager, CoordinationConfigurator
{
   private static final String EVENT_ROLE_SOURCE = "source";
   private static final String EVENT_ROLE_DESTINATION = "destination";
   public static final String BINDING = "binding";
   public static final String PREFIX = "coordination";
   public static final String QNAME_SEPARATOR = ";";

   public static final String PREFIX_EVENT = PREFIX + ".event";
   public static final String EVENT_IMPLICIT_MODE = PREFIX_EVENT + ".implicit_mode";
   public static final String PREFIX_EVENT_NAME = PREFIX_EVENT + ".name.";
   public static final String PREFIX_EVENT_WIRING = PREFIX_EVENT + ".wiring.";
   private static final int PREFIX_EVENT_NAME_LENGTH = PREFIX_EVENT_NAME.length();

   public static final String PREFIX_PARAMETER = PREFIX + ".parameter";
   public static final String PARAMETER_IMPLICIT_MODE = PREFIX_PARAMETER + ".implicit_mode";
   public static final String PREFIX_PARAMETER_NAME = PREFIX_PARAMETER + ".name.";
   public static final String PREFIX_PARAMETER_BINDING = PREFIX_PARAMETER + ".binding.";
   private static final int PREFIX_PARAMETER_BINDING_LENGTH = PREFIX_PARAMETER_BINDING.length();

   public static final String PREFIX_PARAMETER_ALIAS = PREFIX_PARAMETER + ".alias";
   public static final String PREFIX_PARAMETER_ALIAS_NAME = PREFIX_PARAMETER_ALIAS + ".name.";
   private static final int PREFIX_PARAMETER_ALIAS_LENGTH = PREFIX_PARAMETER_ALIAS_NAME.length();

   public static final Boolean DEFAULT_IMPLICIT_MODE = true;
   protected EventConverter eventConverter = new SimpleEventConverter();
   protected PortalObjectContainer portalObjectContainer;

   protected void startService() throws Exception
   {
      super.startService();

      if (portalObjectContainer == null)
      {
         throw new IllegalStateException("Cannot instantiate CoordinationManager: no PortalObjectContainer present");
      }
   }


   public Map<Window, PortletWindowEvent> getEventWindows(PortletWindowEvent event, ControllerPortletControllerContext context) throws IllegalCoordinationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(event, "event");
      ParameterValidation.throwIllegalArgExceptionIfNull(context, "context");

      // Obtain page for which we scope this event
      Page page = context.getWindow(event.getWindowId()).getPage();
      boolean implicitMode = resolveEventWiringImplicitModeEnabled(page);

      // Only explicit wirings
      Map<Window, PortletWindowEvent> windows;
      windows = getEventWindowsExplicit(page, event, context);

      // If no explicit wirings for this event fallback to implicit
      if (implicitMode && windows.size() == 0)
      {
         windows = getEventWindowsImplicit(page, event, context);
      }

      return windows;
   }

   public Collection<String> getBindingNames(Window window, QName name)
   {
      Set<String> names = new HashSet<String>();

      for (ParameterBindingInfo info : getParameterBindings(window))
      {
         for (Map.Entry<Window, Set<QName>> entry : info.getMappings().entrySet())
         {
            if (entry.getKey().getName().equals(window.getName()))
            {
               for (QName qName : entry.getValue())
               {
                  if (name.equals(qName))
                  {
                     names.add(info.getName());
                  }
               }
            }
         }
      }

      // If we have nothing let's try aliases
      if (names.isEmpty())
      {
         for (AliasBindingInfo info : getAliasBindings(window.getPage()))
         {
            if (info.getParameterNames().contains(name))
            {
               names.add(info.getName());
            }
         }
      }

      return names;
   }

   private Map<Window, PortletWindowEvent> getEventWindowsExplicit(Page page, PortletWindowEvent event, ControllerPortletControllerContext context)
   {
      Map<Window, PortletWindowEvent> windows = new HashMap<Window, PortletWindowEvent>();
      Collection<EventWiringInfo> infos = getEventWirings(page);
      for (EventWiringInfo info : infos)
      {
         for (Map.Entry<Window, QName> entry : info.getSources().entrySet())
         {
            if (entry.getKey().getName().equals(event.getWindowId()) &&
                    entry.getValue().equals(event.getName()))
            {
               for (Window window : info.getDestinations().keySet())
               {
                  PortletInfo portletInfo = context.getPortletInfo(window.getName());
                  QName destEventName = info.getDestinations().get(window);
                  EventInfo destEventInfo = portletInfo.getEventing().getConsumedEvents().get(destEventName);
                  if (destEventInfo != null)
                  {
                     windows.put(window, getEventConverter().convertEvent(event, destEventInfo, window));
                  }

               }
               break;
            }
         }
      }

      return windows;
   }

   private Map<Window, PortletWindowEvent> getEventWindowsImplicit(Page page, PortletWindowEvent event, ControllerPortletControllerContext context)
   {
      Map<Window, PortletWindowEvent> windows = new HashMap<Window, PortletWindowEvent>();
      for (String windowName : context.getWindowNames())
      {
         PortletInfo info = context.getPortletInfo(windowName);

         //
         if (info.getEventing() != null && info.getEventing().getConsumedEvents().containsKey(event.getName()))
         {
            PortletWindowEvent distributedEvent = new PortletWindowEvent(event.getName(), event.getPayload(), windowName);
            windows.put(context.getWindow(windowName), distributedEvent);
         }
      }

      return windows;
   }

   public void setEventWiring(Map<Window, QName> sources, Map<Window, QName> destinations, String eventName) throws IllegalCoordinationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(sources, "sources");
      ParameterValidation.throwIllegalArgExceptionIfNull(destinations, "destinations");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(eventName, "event name", null);

      if (sources.isEmpty())
      {
         throw new IllegalArgumentException("empty sources map");
      }

      if (destinations.isEmpty())
      {
         throw new IllegalArgumentException("empty destinations map");
      }

      // Obtain parent page and check that all windows are in one branch...

      Page parentPage = null;

      Set<Window> sw = sources.keySet();
      Set<Window> dw = destinations.keySet();

      Set<Window> all = new HashSet<Window>();
      all.addAll(sw);
      all.addAll(dw);


      for (Window window : all)
      {
         if (parentPage == null)
         {
            parentPage = window.getPage();
         }

         if (!window.getPage().getId().equals(parentPage.getId()))
         {
            throw new IllegalCoordinationException("Parent page is not the same for all windows");
         }
      }

      // Check if the same window is not both in sources and destinations map

      for (Window window : sw)
      {
         if (dw.contains(window))
         {
            throw new IllegalCoordinationException("The same window '" + window.getName()
                    + "' cannot be source and destination of the same explicit wiring");
         }
      }

      // Set the sources

      String prop_name = PREFIX_EVENT_NAME + eventName;
      String prop_wiring = PREFIX_EVENT_WIRING + eventName;

      for (Window window : sw)
      {
         window.setDeclaredProperty(prop_name, EVENT_ROLE_SOURCE);
         window.setDeclaredProperty(prop_wiring, sources.get(window).toString());
      }

      // Set the sources

      for (Window window : dw)
      {
         window.setDeclaredProperty(prop_name, EVENT_ROLE_DESTINATION);
         window.setDeclaredProperty(prop_wiring, destinations.get(window).toString());
      }

   }

   public void removeEventWiring(EventWiringInfo info) throws IllegalCoordinationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(info, "EventWiringInfo");
      removeEventWiring(info.getPage(), info.getName());
   }

   public void removeEventWiring(Page page, String wiringName)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(wiringName, "event wiring name");
      ParameterValidation.throwIllegalArgExceptionIfNull(page, "Page");

      String prop_name = PREFIX_EVENT_NAME + wiringName;
      String prop_wiring = PREFIX_EVENT_WIRING + wiringName;

      for (PortalObject window : page.getChildren(PortalObject.WINDOW_MASK))
      {
         window.setDeclaredProperty(prop_name, null);
         window.setDeclaredProperty(prop_wiring, null);
      }
   }

   public void renameEventWiring(EventWiringInfo eventWiring, String newName) throws IllegalCoordinationException
   {
      removeEventWiring(eventWiring);
      setEventWiring(eventWiring.getSources(), eventWiring.getDestinations(), newName);
   }

   public void renameEventWiring(Page page, String wiringName, String newName) throws IllegalCoordinationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(wiringName, "Event wiring name");
      ParameterValidation.throwIllegalArgExceptionIfNull(page, "Page");

      EventWiringInfo info = getEventWiring(page, wiringName);
      removeEventWiring(page, wiringName);
      setEventWiring(info.getSources(), info.getDestinations(), newName);
   }

   public void setEventWiringImplicitMode(PageContainer pageContainer, boolean mode) throws IllegalCoordinationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(pageContainer, "PageContainer");
      setImplicitMode(pageContainer, mode, EVENT_IMPLICIT_MODE);
   }

   public void removeEventWiringImplicitMode(PageContainer pageContainer) throws IllegalCoordinationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(pageContainer, "PageContainer");
      setImplicitMode(pageContainer, null, EVENT_IMPLICIT_MODE);
   }

   public Boolean isEventWiringImplicitModeEnabled(PageContainer pageContainer)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(pageContainer, "PageContainer");
      return decodeImplicitMode(pageContainer, EVENT_IMPLICIT_MODE);
   }

   public Boolean resolveEventWiringImplicitModeEnabled(PageContainer page)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(page, "PageContainer");
      return resolveImplicitMode(page, EVENT_IMPLICIT_MODE);
   }

   public Collection<EventWiringInfo> getEventWirings(Page page, QName eventQName)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(page, "Page");
      ParameterValidation.throwIllegalArgExceptionIfNull(eventQName, "event QName");

      Collection<EventWiringInfo> pageEvents = getEventWirings(page);

      Set<EventWiringInfo> events = new HashSet<EventWiringInfo>();

      for (EventWiringInfo event : pageEvents)
      {
         if (event.getSources().containsValue(eventQName) || event.getDestinations().containsValue(eventQName))
         {
            pageEvents.add(event);
         }
      }

      return events;
   }

   public EventWiringInfo getEventWiring(Page page, String name) throws IllegalCoordinationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(page, "Page");
      ParameterValidation.throwIllegalArgExceptionIfNull(name, "event wiring name");

      String wiringProp = PREFIX_EVENT_WIRING + name;
      String nameProp = PREFIX_EVENT_NAME + name;

      Collection<PortalObject> windows = page.getChildren(PortalObject.WINDOW_MASK);
      Map<Window, QName> sources = new HashMap<Window, QName>(windows.size());
      Map<Window, QName> destinations = new HashMap<Window, QName>(windows.size());
      for (PortalObject window : windows)
      {
         String eventName = window.getDeclaredProperty(wiringProp);

         if (eventName != null)
         {
            String role = window.getDeclaredProperty(nameProp);
            if (role == null)
            {
               throw new IllegalCoordinationException("Couldn't find role associated to event '" + name + "' in window "
                       + window.getId());
            }

            QName qname = QName.valueOf(eventName);

            if (role.equalsIgnoreCase(EVENT_ROLE_SOURCE))
            {
               sources.put((Window)window, qname);
            }
            else if (role.equalsIgnoreCase(EVENT_ROLE_DESTINATION))
            {
               destinations.put((Window)window, qname);
            }
         }
      }

      boolean emptySources = sources.isEmpty();
      boolean emptyDestinations = destinations.isEmpty();
      if (emptySources && emptyDestinations)
      {
         return null;
      }
      else if ((emptySources && !emptyDestinations) || emptyDestinations)
      {
         throw new IllegalCoordinationException("Couldn't find sources or destinations for event '" + name + "'");
      }
      else
      {
         return new EventInfoPOJO(name, page, sources, destinations);
      }
   }

   public Collection<EventWiringInfo> getEventWirings(Page page)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(page, "Page");

      // Examine window properties and create page events

      Map<String, EventInfoPOJO> events = new HashMap<String, EventInfoPOJO>();
      for (PortalObject window : page.getChildren(PortalObject.WINDOW_MASK))
      {
         Set<String> propNames = window.getDeclaredProperties().keySet();

         for (String propName : propNames)
         {
            if (propName.startsWith(PREFIX_EVENT_NAME))
            {
               String en = propName.substring(PREFIX_EVENT_NAME_LENGTH);
               EventInfoPOJO info;

               if (!events.keySet().contains(en))
               {
                  info = new EventInfoPOJO(en, page);
                  events.put(en, info);
               }
               else
               {
                  info = events.get(en);
               }

               String prop_wiring = PREFIX_EVENT_WIRING + en;

               //TODO: if information from properties is not consistent should we throw exception?
               String name = window.getDeclaredProperty(prop_wiring);
               if (name != null)
               {
                  QName qname = QName.valueOf(name);
                  String role = window.getDeclaredProperty(propName);

                  if (role != null && role.equalsIgnoreCase(EVENT_ROLE_SOURCE))
                  {
                     info.getSources().put((Window)window, qname);
                  }
                  else if (role != null && role.equalsIgnoreCase(EVENT_ROLE_DESTINATION))
                  {
                     info.getDestinations().put((Window)window, qname);
                  }
               }
            }
         }
      }

      // Make immutable copy
      Collection<EventWiringInfo> immutableEvents = new HashSet<EventWiringInfo>();

      for (EventInfoPOJO info : events.values())
      {
         immutableEvents.add(info.getImmutableWiringInfo());
      }

      return immutableEvents;
   }

   public Collection<EventWiringInfo> getEventSourceWirings(Window window)
   {

      Collection<EventWiringInfo> pageEvents = getEventWirings(window.getPage());

      Set<EventWiringInfo> events = new HashSet<EventWiringInfo>();

      for (EventWiringInfo pageEvent : pageEvents)
      {
         if (pageEvent.getSources().keySet().contains(window))
         {
            events.add(pageEvent);
         }
      }

      return events;
   }

   public Collection<EventWiringInfo> getEventDestinationWirings(Window window)
   {
      Collection<EventWiringInfo> pageEvents = getEventWirings(window.getPage());

      Set<EventWiringInfo> events = new HashSet<EventWiringInfo>();

      for (EventWiringInfo pageEvent : pageEvents)
      {
         if (pageEvent.getDestinations().keySet().contains(window))
         {
            events.add(pageEvent);
         }
      }

      return events;
   }

   // Binding stuff *****************************************************************************************

   public void setParameterBinding(String name, Map<Window, Set<QName>> parameterMappings) throws IllegalCoordinationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(parameterMappings, "parameter mappings");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(name, "name", "parameter binding");
      if (parameterMappings.isEmpty())
      {
         throw new IllegalArgumentException("empty parameter mappings");
      }

      // Obtain parent page and check that all windows are in one branch...
      Page parentPage = null;
      for (Window window : parameterMappings.keySet())
      {
         if (parentPage == null)
         {
            parentPage = window.getPage();
         }
         if (!window.getPage().getId().equals(parentPage.getId()))
         {
            throw new IllegalCoordinationException("Parent page is not the same for all windows");
         }
      }

      // Set window properties
      String prop_wiring = PREFIX_PARAMETER_BINDING + name;

      //
      for (Window window : parameterMappings.keySet())
      {
         window.setDeclaredProperty(prop_wiring, concatenateQNames(parameterMappings.get(window)));
      }
   }

   public void removeParameterBinding(ParameterBindingInfo info) throws IllegalCoordinationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(info, "ParameterBindingInfo");

      removeParameterBinding(info.getPage(), info.getName());
   }

   public void removeParameterBinding(Page page, String name)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(name, "parameter binding name");
      ParameterValidation.throwIllegalArgExceptionIfNull(page, "Page");

      String nameProp = PREFIX_PARAMETER_BINDING + name;
      for (PortalObject window : page.getChildren(PortalObject.WINDOW_MASK))
      {
         window.setDeclaredProperty(nameProp, null);
      }
   }

   public void renameParameterBinding(ParameterBindingInfo parameterBinding, String newName) throws IllegalCoordinationException
   {
      removeParameterBinding(parameterBinding);
      setParameterBinding(newName, parameterBinding.getMappings());
   }

   public void renameParameterBinding(Page page, String bindingName, String newName) throws IllegalCoordinationException
   {
      ParameterBindingInfo info = getParameterBinding(page, bindingName);
      removeParameterBinding(page, bindingName);
      setParameterBinding(newName, info.getMappings());
   }

   public ParameterBindingInfo getParameterBinding(Page page, String name) throws IllegalCoordinationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(page, "Page");
      ParameterValidation.throwIllegalArgExceptionIfNull(name, "parameter binding name");

      String nameProp = PREFIX_PARAMETER_BINDING + name;

      // Examine window properties and create page parameters
      Collection<PortalObject> children = page.getChildren(PortalObject.WINDOW_MASK);
      Map<Window, Set<QName>> mappings = new HashMap<Window, Set<QName>>(children.size());
      for (PortalObject window : children)
      {
         String qNameList = window.getDeclaredProperty(nameProp);

         if (qNameList != null)
         {
            mappings.put((Window)window, extractQNames(qNameList));
         }
      }

      if (mappings.isEmpty())
      {
         return null;
      }
      else
      {
         return new ParameterInfoPOJO(name, page, mappings);
      }
   }

   public Boolean isParameterBindingImplicitModeEnabled(PageContainer pageContainer)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(pageContainer, "PageContainer");
      return decodeImplicitMode(pageContainer, PARAMETER_IMPLICIT_MODE);
   }

   public void setParameterBindingImplicitMode(PageContainer pageContainer, boolean mode) throws IllegalCoordinationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(pageContainer, "PageContainer");
      setImplicitMode(pageContainer, mode, PARAMETER_IMPLICIT_MODE);
   }

   public Boolean resolveParameterBindingImplicitModeEnabled(PageContainer pageContainer)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(pageContainer, "PageContainer");
      return resolveImplicitMode(pageContainer, PARAMETER_IMPLICIT_MODE);
   }

   public void removeParameterBindingImplicitMode(PageContainer pageContainer) throws IllegalCoordinationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(pageContainer, "PageContainer");
      setImplicitMode(pageContainer, null, PARAMETER_IMPLICIT_MODE);
   }

   public void setAliasBinding(Page page, String aliasName, Set<QName> qnames) throws IllegalCoordinationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(page, "Page");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(aliasName, "name", "alias binding");
      ParameterValidation.throwIllegalArgExceptionIfNull(qnames, "aliased QNames");
      if (qnames.isEmpty())
      {
         throw new IllegalArgumentException("Aliased QNames set is empty!");
      }

      String propName = PREFIX_PARAMETER_ALIAS_NAME + aliasName;

      page.setDeclaredProperty(propName, concatenateQNames(qnames));
   }

   private String concatenateQNames(Set<QName> qnames) throws IllegalCoordinationException
   {
      StringBuilder qnameList = new StringBuilder();
      for (Iterator i = qnames.iterator(); i.hasNext();)
      {
         QName qname = (QName)i.next();
         // Check if qname contains separator string
         if (qname.toString().contains(QNAME_SEPARATOR))
         {
            throw new IllegalCoordinationException("Qname: " + qname + " contains forbidden character: " + QNAME_SEPARATOR);
         }
         qnameList.append(qname.toString());
         if (i.hasNext())
         {
            qnameList.append(QNAME_SEPARATOR);
         }
      }
      return qnameList.toString();
   }

   public void removeAliasBinding(AliasBindingInfo aliasInfo) throws IllegalCoordinationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(aliasInfo, "AliasBindingInfo");
      removeAliasBinding(aliasInfo.getPage(), aliasInfo.getName());
   }

   public void removeAliasBinding(Page page, String name)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(name, "alias binding name");
      ParameterValidation.throwIllegalArgExceptionIfNull(page, "Page");

      String nameProp = PREFIX_PARAMETER_ALIAS_NAME + name;
      page.setDeclaredProperty(nameProp, null);
   }

   public void renameAliasBinding(AliasBindingInfo aliasBinding, String newName) throws IllegalCoordinationException
   {
      removeAliasBinding(aliasBinding);
      setAliasBinding(aliasBinding.getPage(), newName, aliasBinding.getParameterNames());
   }

   public void renameAliasBinding(Page page, String bindingName, String newName) throws IllegalCoordinationException
   {
      AliasBindingInfo info = getAliasBinding(page, bindingName);
      removeAliasBinding(page, bindingName);
      setAliasBinding(page, newName, info.getParameterNames());
   }

   public Collection<AliasBindingInfo> getAliasBindings(Page page)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(page, "Page");

      HashSet<AliasBindingInfo> aliases = new HashSet<AliasBindingInfo>();

      for (String propertyName : page.getDeclaredProperties().keySet())
      {
         if (propertyName.startsWith(PREFIX_PARAMETER_ALIAS_NAME))
         {
            String aliasName = propertyName.substring(PREFIX_PARAMETER_ALIAS_LENGTH);
            AliasInfoPOJO info = new AliasInfoPOJO(aliasName, page);
            String qnameList = page.getDeclaredProperty(propertyName);
            String[] qnames = qnameList.split(QNAME_SEPARATOR);
            if (qnames != null)
            {
               for (String string : qnames)
               {
                  QName qname = QName.valueOf(string);
                  info.add(qname);
               }
            }
            aliases.add(info);
         }
      }

      return aliases;
   }

   public AliasBindingInfo getAliasBinding(Page page, String name)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(page, "Page");
      ParameterValidation.throwIllegalArgExceptionIfNull(name, "alias binding name");

      String nameProp = PREFIX_PARAMETER_ALIAS_NAME + name;
      String qNameList = page.getDeclaredProperty(nameProp);
      if (qNameList != null)
      {
         Set<QName> names = extractQNames(qNameList);
         return new AliasInfoPOJO(name, page, names);
      }
      else
      {
         return null;
      }
   }

   private Set<QName> extractQNames(String qNameList)
   {
      String[] qnames = qNameList.split(QNAME_SEPARATOR);
      Set<QName> names = new HashSet<QName>(qnames.length);
      for (String string : qnames)
      {
         QName qname = QName.valueOf(string);
         names.add(qname);
      }
      return names;
   }

   public Collection<? extends ParameterBindingInfo> getParameterBindings(Page page, QName parameterName)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(page, "Page");
      ParameterValidation.throwIllegalArgExceptionIfNull(parameterName, "parameter QName");

      Collection<? extends ParameterBindingInfo> pageParams = getParameterBindings(page);
      Collection<ParameterBindingInfo> infos = new HashSet<ParameterBindingInfo>();
      for (ParameterBindingInfo pageParam : pageParams)
      {
         Collection<Set<QName>> allQNames = pageParam.getMappings().values();
         for (Set<QName> qNameSet : allQNames)
         {
            if (qNameSet.contains(parameterName))
            {
               infos.add(pageParam);
            }
         }
      }
      return infos;
   }

   public Collection<ParameterBindingInfo> getParameterBindings(Window window)
   {
      Collection<? extends ParameterBindingInfo> pageParams = getParameterBindings(window.getPage());
      Collection<ParameterBindingInfo> infos = new HashSet<ParameterBindingInfo>();
      for (ParameterBindingInfo pageParam : pageParams)
      {
         if (pageParam.getMappings().keySet().contains(window))
         {
            infos.add(pageParam);
         }
      }
      return infos;
   }

   public Collection<? extends ParameterBindingInfo> getParameterBindings(Page page)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(page, "Page");

      // Examine window properties and create page parameters
      Collection<PortalObject> children = page.getChildren(PortalObject.WINDOW_MASK);

      Map<String, ParameterInfoPOJO> params = new HashMap<String, ParameterInfoPOJO>();
      for (PortalObject child : children)
      {
         Set<String> propNames = child.getProperties().keySet();

         for (String propName : propNames)
         {
            if (propName.startsWith(PREFIX_PARAMETER_BINDING))
            {
               String pn = propName.substring(PREFIX_PARAMETER_BINDING_LENGTH);
               ParameterInfoPOJO info;
               if (!params.keySet().contains(pn))
               {
                  info = new ParameterInfoPOJO(pn, page);
                  params.put(pn, info);
               }
               else
               {
                  info = params.get(pn);
               }

               String binding = child.getProperty(propName);
               //TODO: if the information from properties is not consistent should we throw exception?
               if (binding != null)
               {
                  QName qname = QName.valueOf(binding);
                  if (qname != null)
                  {
                     info.addMapping((Window)child, qname);
                  }
               }
            }
         }
      }

      return Collections.unmodifiableCollection(params.values());
   }

   // SETTERS & GETTERS

   public PortalObjectContainer getPortalObjectContainer()
   {
      return portalObjectContainer;
   }

   public void setPortalObjectContainer(PortalObjectContainer portalObjectContainer)
   {
      this.portalObjectContainer = portalObjectContainer;
   }

   public EventConverter getEventConverter()
   {
      return eventConverter;
   }

   public void setEventConverter(EventConverter eventConverter)
   {
      this.eventConverter = eventConverter;
   }

   // HELPER METHODS

   private void setImplicitMode(PageContainer pageContainer, Boolean mode, String prefix)
   {
      if (mode == null)
      {
         pageContainer.setDeclaredProperty(prefix, null);
      }
      else
      {
         pageContainer.setDeclaredProperty(prefix, mode.toString());
      }

   }

   private Boolean decodeImplicitMode(PageContainer pageContainer, String prefix)
   {
      String value = pageContainer.getDeclaredProperty(prefix);
      return value != null ? Boolean.valueOf(value) : null;
   }


   private Boolean resolveImplicitMode(PageContainer pageContainer, String prefix)
   {
      String value = pageContainer.getDeclaredProperty(prefix);
      Boolean mode;

      // Try to obtain recursive implicit mode from parents
      if (value == null)
      {
         mode = resolveRecursiveImplicitMode(pageContainer, prefix);
      }
      else
      {
         mode = Boolean.valueOf(value);
      }

      if (mode != null)
      {
         return mode;
      }
      else
      {
         return DEFAULT_IMPLICIT_MODE;
      }

   }

   // Search for the mode in upper object hierarchy
   private Boolean resolveRecursiveImplicitMode(PortalObject po, String prefix)
   {
      String value = po.getDeclaredProperty(prefix);
      Boolean mode = null;

      if (value != null)
      {
         mode = Boolean.valueOf(value);
      }

      if (mode != null)
      {
         return mode;
      }
      else if (!(po instanceof Portal))
      {
         return resolveRecursiveImplicitMode(po.getParent(), prefix);
      }

      return null;
   }


   private class EventInfoPOJO implements EventWiringInfo
   {
      private final String name;
      private final Map<Window, QName> sources;
      private final Map<Window, QName> destinations;
      private final Page page;

      private EventInfoPOJO(String name, Page page, Map<Window, QName> sources, Map<Window, QName> destinations)
      {
         this.name = name;
         this.sources = sources;
         this.destinations = destinations;
         this.page = page;
      }

      private EventInfoPOJO(String name, Page page)
      {
         this(name, page, new HashMap<Window, QName>(), new HashMap<Window, QName>());
      }

      public String getName()
      {
         return name;
      }

      public Map<Window, QName> getSources()
      {
         return sources;
      }

      public Map<Window, QName> getDestinations()
      {
         return destinations;
      }

      EventWiringInfo getImmutableWiringInfo()
      {
         return new EventInfoPOJO(name, page, Collections.unmodifiableMap(sources), Collections.unmodifiableMap(destinations));
      }

      public Page getPage()
      {
         return page;
      }
   }

   private class ParameterInfoPOJO implements ParameterBindingInfo
   {
      private final String name;
      private Map<Window, Set<QName>> mappings;
      private final Page page;

      private ParameterInfoPOJO(String name, Page page, Map<Window, Set<QName>> mappings)
      {
         this.name = name;
         this.mappings = mappings;
         this.page = page;
      }

      private ParameterInfoPOJO(String name, Page page)
      {
         this(name, page, new HashMap<Window, Set<QName>>());
      }

      public String getName()
      {
         return name;
      }

      public Map<Window, Set<QName>> getMappings()
      {
         return Collections.unmodifiableMap(mappings);
      }

      public Page getPage()
      {
         return page;
      }

      private void addMapping(Window window, QName qname)
      {
         mappings = Utils.addToMultiMap(mappings, window, qname);
      }
   }

   private class AliasInfoPOJO implements AliasBindingInfo
   {
      private final String name;
      private final Page page;
      private final Set<QName> names;

      private AliasInfoPOJO(String name, Page page, Set<QName> names)
      {
         this.name = name;
         this.page = page;
         this.names = names;
      }

      private AliasInfoPOJO(String name, Page page)
      {
         this(name, page, new HashSet<QName>());
      }

      public String getName()
      {
         return name;
      }

      public Set<QName> getParameterNames()
      {
         return Collections.unmodifiableSet(names);
      }

      public Page getPage()
      {
         return page;
      }

      private void add(QName qname)
      {
         names.add(qname);
      }
   }
}

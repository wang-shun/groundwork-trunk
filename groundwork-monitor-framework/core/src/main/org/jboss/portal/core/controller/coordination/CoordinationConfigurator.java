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

package org.jboss.portal.core.controller.coordination;

import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.PageContainer;
import org.jboss.portal.core.model.portal.Window;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:boleslaw dot dawidowicz at redhat anotherdot com">Boleslaw Dawidowicz</a>
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version : 0.1 $
 */
public interface CoordinationConfigurator
{
   // Management

   /**
    * Defines a wiring for a given even QName between multiple source and destination windows
    *
    * @param sources
    * @param targets
    * @param eventName
    * @throws IllegalCoordinationException
    */
   void setEventWiring(Map<Window, QName> sources, Map<Window, QName> targets, String eventName) throws IllegalCoordinationException;

   /**
    * Remove wiring
    *
    * @param eventWiringInfo
    * @throws IllegalCoordinationException
    */
   void removeEventWiring(EventWiringInfo eventWiringInfo) throws IllegalCoordinationException;

   /**
    * Removes the wiring found in the given page and identified with the given name
    *
    * @param page
    * @param wiringName
    */
   void removeEventWiring(Page page, String wiringName);

   /**
    * Set event wiring implicit mode for a given page container. This will be inherited recursively by all children page
    * containers
    *
    * @param pageContainer
    * @param mode
    * @throws IllegalCoordinationException
    */
   void setEventWiringImplicitMode(PageContainer pageContainer, boolean mode) throws IllegalCoordinationException;

   /**
    * Renames the specified wiring to the new name
    *
    * @param eventWiring the wiring to be renamed
    * @param newName     the wiring new name
    * @throws IllegalCoordinationException
    */
   void renameEventWiring(EventWiringInfo eventWiring, String newName) throws IllegalCoordinationException;

   /**
    * Renames the named wiring in the given Page to the spefified new name.
    *
    * @param page       the Page in which the wiring is supposed to be found
    * @param wiringName the name of the wiring to be renamed
    * @param newName    the new name for the wiring
    * @throws IllegalCoordinationException
    */
   void renameEventWiring(Page page, String wiringName, String newName) throws IllegalCoordinationException;

   /**
    * Checks if implicit event wiring is enabled for a given page container. May return null if no implicit mode is not
    * set for this portal object
    *
    * @param pageContainer
    * @return
    */
   Boolean isEventWiringImplicitModeEnabled(PageContainer pageContainer);

   /**
    * Removes event wiring implicit mode entry for a given page container
    *
    * @param pageContainer
    * @throws IllegalCoordinationException
    */
   void removeEventWiringImplicitMode(PageContainer pageContainer) throws IllegalCoordinationException;

   /**
    * @param page
    * @param eventQName
    * @return all wirings defined for a given qname
    */
   Collection<EventWiringInfo> getEventWirings(Page page, QName eventQName);

   /**
    * @param page
    * @return all wirings defined in the scope of a given page
    */
   Collection<EventWiringInfo> getEventWirings(Page page);

   /**
    * Retrieves the event wiring found in the given page and identified with the specified name
    *
    * @param page
    * @param name
    * @return
    * @throws IllegalCoordinationException
    */
   EventWiringInfo getEventWiring(Page page, String name) throws IllegalCoordinationException;

   /**
    * @param window
    * @return all wirings where given window is a source
    */
   Collection<EventWiringInfo> getEventSourceWirings(Window window);

   /**
    * @param window
    * @return all wirings where given window is a destination
    */
   Collection<EventWiringInfo> getEventDestinationWirings(Window window);

   /**
    * Defines shared parameter binding for a given collection of windows.
    *
    * @param name
    * @param parameterMappings
    * @throws IllegalCoordinationException
    */
   void setParameterBinding(String name, Map<Window, Set<QName>> parameterMappings) throws IllegalCoordinationException;

   /**
    * Removes given parameter binding
    *
    * @param parameterBinding
    * @throws IllegalCoordinationException
    */
   void removeParameterBinding(ParameterBindingInfo parameterBinding) throws IllegalCoordinationException;

   /**
    * @param page
    * @param name
    */
   void removeParameterBinding(Page page, String name);

   /**
    * Renames the specified window binding to the new name
    *
    * @param parameterBinding the window binding to be renamed
    * @param newName          the binding new name
    * @throws IllegalCoordinationException
    */
   void renameParameterBinding(ParameterBindingInfo parameterBinding, String newName) throws IllegalCoordinationException;

   /**
    * @param page
    * @param bindingName
    * @param newName
    * @throws IllegalCoordinationException
    */
   void renameParameterBinding(Page page, String bindingName, String newName) throws IllegalCoordinationException;

   /**
    * @param page
    * @return window bindings define in the scope of a given page
    */
   Collection<? extends ParameterBindingInfo> getParameterBindings(Page page);

   /**
    * @param window
    * @return window bindings where given window is involved
    */
   Collection<? extends ParameterBindingInfo> getParameterBindings(Window window);

   /**
    * @param page
    * @param name
    * @return
    * @throws IllegalCoordinationException
    */
   ParameterBindingInfo getParameterBinding(Page page, String name) throws IllegalCoordinationException;

   /**
    * Set parameter binding implicit mode for a given page container. This will be inherited recursively by all children
    * page containers
    *
    * @param pageContainer
    * @param mode
    * @throws IllegalCoordinationException
    */
   void setParameterBindingImplicitMode(PageContainer pageContainer, boolean mode) throws IllegalCoordinationException;

   /**
    * Removes parameter binding implicit mode entry for a given page container
    *
    * @param pageContainer
    * @throws IllegalCoordinationException
    */
   void removeParameterBindingImplicitMode(PageContainer pageContainer) throws IllegalCoordinationException;

   /**
    * Checks if implicit binding is enabled for a given page container. May return null if implicit mode is not set for
    * this portal object
    *
    * @param pageContainer
    * @return
    */
   Boolean isParameterBindingImplicitModeEnabled(PageContainer pageContainer);

   /**
    * Set alias binding for a given page. If alias with given name already exists it will be overwritten
    *
    * @param page
    * @param aliasName
    * @param qnames
    * @throws IllegalCoordinationException
    */
   void setAliasBinding(Page page, String aliasName, Set<QName> qnames) throws IllegalCoordinationException;

   /**
    * Removes an alias binding.
    *
    * @param aliasInfo
    * @throws IllegalCoordinationException
    */
   void removeAliasBinding(AliasBindingInfo aliasInfo) throws IllegalCoordinationException;

   /**
    * @param page
    * @param name
    * @throws IllegalCoordinationException
    */
   void removeAliasBinding(Page page, String name);

   /**
    * Renames the specified alias to the new name
    *
    * @param aliasBinding the alias binding to be renamed
    * @param newName      the alias new name
    * @throws IllegalCoordinationException
    */
   void renameAliasBinding(AliasBindingInfo aliasBinding, String newName) throws IllegalCoordinationException;

   /**
    * @param page
    * @param bindingName
    * @param newName
    * @throws IllegalCoordinationException
    */
   void renameAliasBinding(Page page, String bindingName, String newName) throws IllegalCoordinationException;

   /**
    * @param page
    * @return collection of alias bindings connected to the given page
    */
   Collection<? extends AliasBindingInfo> getAliasBindings(Page page);

   /**
    * @param page
    * @param name
    * @return
    */
   AliasBindingInfo getAliasBinding(Page page, String name);

   /**
    * @param page
    * @param parameterQName
    * @return window bindings for a given parameter qname
    */
   Collection<? extends ParameterBindingInfo> getParameterBindings(Page page, QName parameterQName);
}

/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2009, Red Hat Middleware, LLC, and individual                    *
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
package org.jboss.portal.core.model.portal;

import org.jboss.portal.core.model.HasDisplayName;

import java.util.Collection;
import java.util.Map;

/**
 * The base interface for all portal objects.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 12832 $
 */
public interface PortalObject extends Comparable, HasDisplayName
{

   /** Portal property name that indicates the name of the default portal to lookup. */
   String PORTAL_PROP_DEFAULT_OBJECT_NAME = "portal.defaultObjectName";

   /** The default portal name. */
   String DEFAULT_OBJECT_NAME = "default";

   /** . */
   int TYPE_CONTEXT = 0;

   /** . */
   int TYPE_PORTAL = 1;

   /** . */
   int TYPE_PAGE = 2;

   /** . */
   int TYPE_WINDOW = 3;

   /** . */
   int CONTEXT_MASK = 0x00000001;

   /** . */
   int PORTAL_MASK = 0x00000002;

   /** . */
   int PAGE_MASK = 0x00000004;

   /** . */
   int WINDOW_MASK = 0x00000008;

   /**
    * Return the object id unique in the scope of its container.
    *
    * @return the object id
    */
   PortalObjectId getId();

   /**
    * Returns the type of the object which is a value that discriminates the object type.
    *
    * @return the object type.
    */
   int getType();

   /**
    * Return the object name unique in the scope of its parent.
    *
    * @return the object name
    */
   String getName();

   /**
    * Returns the listener id or null if there is none.
    *
    * @return the listener
    */
   String getListener();

   /**
    * Set a listener id.
    *
    * @param listener the listener id
    */
   void setListener(String listener);

   /**
    * Return all the children of this object.
    *
    * @return the children
    */
   Collection<PortalObject> getChildren();

   /**
    * Return all the children of this object filtered with a particular mask.
    *
    * @return the children
    */
   Collection<PortalObject> getChildren(int mask);

   /**
    * Return the parent object.
    *
    * @return the parent object.
    */
   PortalObject getParent();

   /**
    * Return a specific child object or null if it does not exist.
    *
    * @return a child object.
    */
   PortalObject getChild(String name);

   /**
    * Returns the child of the specified type and with the given name or <code>null</code> if it cannot be found.
    *
    * @param name         the child's name
    * @param expectedType the expected type of the child to be retrieved
    * @param <T>          a class extending PortalObject
    * @return the named child or <code>null</code> if it cannot be found
    * @throws IllegalArgumentException if the specified name or the specified class is <code>null</code>
    * @since 2.7
    */
   <T extends PortalObject> T getChild(String name, Class<T> expectedType);

   /**
    * Destroy an existing child.
    *
    * @param name the child name
    * @throws NoSuchPortalObjectException if the child does not exist
    * @throws IllegalArgumentException    if the name argument is null
    */
   void destroyChild(String name) throws NoSuchPortalObjectException, IllegalArgumentException;

   /**
    * Copy the portal object as a child of the specified object.
    *
    * @param parent the parent of the copy
    * @param name   the name of the child
    * @param deep   true copies recursively children
    * @return the newly created node child of the specified parent
    * @throws DuplicatePortalObjectException if the specified parent has already a node with such a name
    * @throws IllegalArgumentException       if the specified parent is null
    */
   PortalObject copy(PortalObject parent, String name, boolean deep) throws DuplicatePortalObjectException, IllegalArgumentException;

   /**
    * Return a property of that object.
    *
    * @return the property value
    * @throws IllegalArgumentException if the name is null
    */
   String getProperty(String name) throws IllegalArgumentException;

   /**
    * Returns a read only map that contains the object properties.
    *
    * @return a map of the object properties
    */
   Map<String, String> getProperties();

   /**
    * Return a property declared on that object.
    *
    * @return the property value
    * @throws IllegalArgumentException if the name is null
    */
   String getDeclaredProperty(String name) throws IllegalArgumentException;

   /**
    * Update a property declared on that object.
    *
    * @param name  the property name
    * @param value the property value
    * @throws IllegalArgumentException if the name argument is null
    */
   void setDeclaredProperty(String name, String value) throws IllegalArgumentException;

   /**
    * Return a map that contains the object declared properties.
    *
    * @return a map of the properties declared by the object
    */
   Map<String, String> getDeclaredProperties();

//   /**
//    * Return true if the portal object is part of a dashboard.
//    *
//    * @return if it is a dashboard
//    */
//   boolean isDashboard();
}

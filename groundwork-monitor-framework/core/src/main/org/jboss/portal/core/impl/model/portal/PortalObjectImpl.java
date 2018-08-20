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

package org.jboss.portal.core.impl.model.portal;

import org.apache.log4j.Logger;
import org.jboss.portal.common.NotYetImplemented;
import org.jboss.portal.common.i18n.LocalizedString;
import org.jboss.portal.common.util.ParameterValidation;
import org.jboss.portal.core.model.portal.DuplicatePortalObjectException;
import org.jboss.portal.core.model.portal.NoSuchPortalObjectException;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.PortalObjectId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 12653 $
 */
public abstract class PortalObjectImpl implements PortalObject
{

   /** The logger. */
   protected static final Logger log = Logger.getLogger(PortalObjectImpl.class);

   /** . */
   protected static final int ALL_TYPES_MASK = CONTEXT_MASK | PORTAL_MASK | PAGE_MASK | WINDOW_MASK;

   // Persistent fields

   /** The primary key when the object is persisted */
   private Long key;

   /** . */
   private Map<String, String> declaredPropertyMap;

   /** . */
   private String listener;

   /** . */
   private Map displayNames;

   /** The node. */
   private ObjectNode objectNode;

   // Runtime fields
   private Map properties;
   private Map unmodifiableProperties;
   private SortedSet accessedChildren;

   public PortalObjectImpl()
   {
      this(true);
   }

   public PortalObjectImpl(boolean initState)
   {
      this.declaredPropertyMap = new HashMap();
      this.listener = null;

      //
      this.properties = null;
      this.unmodifiableProperties = null;
      this.accessedChildren = null;
   }

   public Long getKey()
   {
      return key;
   }

   public void setKey(Long key)
   {
      this.key = key;
   }

   public ObjectNode getObjectNode()
   {
      return objectNode;
   }

   public void setObjectNode(ObjectNode objectNode)
   {
      this.objectNode = objectNode;
   }

   public PortalObjectId getId()
   {
      return objectNode.getPath();
   }

   public int compareTo(Object o)
   {
      PortalObject po = (PortalObject)o;

      return getId().compareTo(po.getId());
   }

   public void destroyChild(String name) throws NoSuchPortalObjectException
   {
      objectNode.removeChild(name);
   }

   public String getName()
   {
      return objectNode.getName();
   }

   public void setDisplayNames(Map displayNames)
   {
      this.displayNames = displayNames;
   }

   public Map getDisplayNames()
   {
      return displayNames;
   }

   public void setDisplayName(LocalizedString displayName)
   {
      if (displayName == null)
      {
         throw new IllegalArgumentException("No null display name accepted");
      }

      displayNames = new HashMap();

      Map map = displayName.getValues();
      Iterator it = map.values().iterator();
      while (it.hasNext())
      {
         LocalizedString.Value value = (LocalizedString.Value)it.next();
         displayNames.put(value.getLocale(), value.getString());
      }
   }

   public LocalizedString getDisplayName()
   {
      if (displayNames != null)
      {
         return new LocalizedString(displayNames, Locale.ENGLISH);
      }
      else
      {
         return null;
      }
   }

   public PortalObject copy(PortalObject parent, String name, boolean deep) throws DuplicatePortalObjectException, IllegalArgumentException
   {
      if (parent == null)
      {
         throw new IllegalArgumentException("No null parent accepted");
      }
      if (name == null)
      {
         throw new IllegalArgumentException("No null name accepted");
      }
      return copy((PortalObjectImpl)parent, name, deep);
   }

   public Collection getChildren()
   {
      return getChildren(ALL_TYPES_MASK);
   }

   private class ChildrenCollection implements Collection
   {
      /** . */
      private final int mask;
      private final SortedMap children;

      public ChildrenCollection(int mask, Map children)
      {
         this.mask = mask;
         this.children = new TreeMap(children);
      }

      public void clear()
      {
         throw new UnsupportedOperationException();
      }

      public boolean add(Object o)
      {
         throw new UnsupportedOperationException();
      }

      public boolean remove(Object o)
      {
         throw new UnsupportedOperationException();
      }

      public boolean addAll(Collection c)
      {
         throw new UnsupportedOperationException();
      }

      public boolean removeAll(Collection c)
      {
         throw new UnsupportedOperationException();
      }

      public boolean retainAll(Collection c)
      {
         throw new UnsupportedOperationException();
      }

      public boolean containsAll(Collection c)
      {
         throw new NotYetImplemented();
      }

      public boolean contains(Object o)
      {
         throw new NotYetImplemented();
      }

      public boolean isEmpty()
      {
         return children.isEmpty();
      }

      public int size()
      {
         if (mask != ALL_TYPES_MASK)
         {
            int count = 0;

            for (Object object : children.values())
            {
               ObjectNode childNode = (ObjectNode)object;
               PortalObjectImpl childObject = childNode.getObject();
               if (isMatchingMask(childObject, mask))
               {
                  count++;
               }
            }

            return count;
         }
         else
         {
            return children.size();
         }
      }

      public Object[] toArray()
      {
         return toArray(new Object[size()]);
      }

      public Iterator iterator()
      {
         return new ChildrenIterator();
      }

      public Object[] toArray(Object a[])
      {
         ArrayList tmp = new ArrayList(children.size());
         for (Object child : this)
         {
            // no need to add to accessedChildren here as it's already done by ChildrenIterator.next()...
            tmp.add(child);
         }

         return tmp.toArray(a);
      }

      @Override
      public String toString()
      {
         StringBuilder sb = new StringBuilder(512);
         sb.append("[");
         for (Object o : this)
         {
            sb.append(" ").append(o);
         }
         sb.append(" ]");
         return sb.toString();
      }

      private class ChildrenIterator implements Iterator
      {
         private final Iterator iterator;

         private PortalObject nextChild = null;

         public ChildrenIterator()
         {
            // Make sure the children are sorted for consistent ordering downstream
            iterator = children.values().iterator();
         }

         public void remove()
         {
            throw new UnsupportedOperationException();
         }

         public boolean hasNext()
         {
            if (nextChild == null)
            {
               while (nextChild == null && iterator.hasNext())
               {
                  ObjectNode childNode = (ObjectNode)iterator.next();
                  PortalObjectImpl childObject = childNode.getObject();
                  if (isMatchingMask(childObject, mask))
                  {
                     nextChild = childObject;
                  }
               }
            }
            return nextChild != null;
         }

         public Object next()
         {
            if (this.nextChild == null)
            {
               hasNext();
            }
            if (this.nextChild == null)
            {
               throw new NoSuchElementException();
            }

            //
            getAccessedChildren().add(nextChild);

            //
            PortalObject nextChild = this.nextChild;
            this.nextChild = null;

            //
            return nextChild;
         }
      }
   }

   private boolean isMatchingMask(PortalObjectImpl object, int mask)
   {
      return (mask == ALL_TYPES_MASK || (object.getMask() & mask) != 0);
   }

   public Collection getChildren(int wantedMask)
   {
      // Correct eventually the mask
      final int mask = wantedMask & ALL_TYPES_MASK;

      return new ChildrenCollection(mask, objectNode.getChildren());
   }

   public String getListener()
   {
      return listener;
   }

   public void setListener(String listener)
   {
      this.listener = listener;
   }

   public PortalObject getParent()
   {
      if (objectNode.getParent() != null)
      {
         return objectNode.getParent().getObject();
      }
      else
      {
         return null;
      }
   }

   public PortalObject getChild(String name)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(name, "child name");
      ObjectNode childNode = (ObjectNode)objectNode.getChildren().get(name);
      if (childNode != null)
      {
         PortalObjectImpl childObject = childNode.getObject();

         // Track it
         getAccessedChildren().add(childObject);

         //
         return childObject;
      }
      else
      {
         return null;
      }
   }

   public <T extends PortalObject> T getChild(String name, Class<T> expectedType)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(expectedType, "expected type");
      PortalObject child = getChild(name);

      // only return the child if it matches the expected class
      if (expectedType.isInstance(child))
      {
         return expectedType.cast(child);
      }

      return null;
   }

   private Set getAccessedChildren()
   {
      if (accessedChildren == null)
      {
         accessedChildren = new TreeSet();
      }
      return accessedChildren;
   }

   /** Get the aggregated properties in a lazy manner. */
   public Map getProperties()
   {
      // Lazy compute properties
      if (properties == null)
      {
         ObjectNode parent = objectNode.getParent();
         if (parent == null)
         {
            this.properties = declaredPropertyMap;
         }
         else
         {
            Map properties = new HashMap();
            properties.putAll(parent.getObject().getProperties());
            properties.putAll(declaredPropertyMap);
            this.properties = properties;
         }

         //
         this.unmodifiableProperties = Collections.unmodifiableMap(properties);
      }

      //
      return unmodifiableProperties;
   }

   public Map<String, String> getDeclaredPropertyMap()
   {
      return declaredPropertyMap;
   }

   public void setDeclaredPropertyMap(Map<String, String> properties)
   {
      this.declaredPropertyMap = properties;
   }

   public Map<String, String> getDeclaredProperties()
   {
      return Collections.unmodifiableMap(declaredPropertyMap);
   }

   public String getDeclaredProperty(String name)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(name, "property name");
      return declaredPropertyMap.get(name);
   }

   public void setDeclaredProperty(String name, String value)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(name, "property name");
      if (value == null)
      {
         declaredPropertyMap.remove(name);
         PortalObject parent = getParent();
         String parentValue = null;
         if (parent != null)
         {
            parentValue = parent.getProperty(name);
         }

         propagatePropertyUpdate(name, parentValue, true);
      }
      else
      {
         declaredPropertyMap.put(name, value);
         propagatePropertyUpdate(name, value, true);
      }
   }

   /**
    * This method propagates a property value update to descendants which have been *loaded* from the database. It
    * considers that if the <code>properties</code> field of the runtime state is null then it means that the object is
    * loaded but children have not made an attempt to read the properties of this object. Indeed if a child is loaded
    * any attempt to access its aggregated properties will trigger the computation of the aggregated properties of this
    * object.
    * <p/>
    * Null property values are considered as removal
    *
    * @param name  the property name
    * @param value the new property value
    * @param force the update
    */
   private void propagatePropertyUpdate(String name, String value, boolean force)
   {
      if (properties != null)
      {
         if (force || !declaredPropertyMap.containsKey(name))
         {
            if (value == null)
            {
               properties.remove(name);
            }
            else
            {
               properties.put(name, value);
            }

            //
            if (accessedChildren != null)
            {
               for (Iterator i = accessedChildren.iterator(); i.hasNext();)
               {
                  PortalObjectImpl child = (PortalObjectImpl)i.next();
                  child.propagatePropertyUpdate(name, value, false);
               }
            }
         }
      }
   }

   public String getProperty(String name)
   {
      if (name == null)
      {
         throw new IllegalArgumentException();
      }

      // Trigger the lazy loading
      Map properties = getProperties();

      // Lookup the property
      return (String)properties.get(name);
   }

   public abstract int getType();

   public String toString()
   {
      return objectNode.toString();
   }

   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }
      if (obj instanceof PortalObjectImpl)
      {
         PortalObjectImpl that = (PortalObjectImpl)obj;
         return getId().equals(that.getId());
      }
      return false;
   }

   public int hashCode()
   {
      return getId().hashCode();
   }

   /** Return the default child of this object based on the declared property that specifies the default object name. */
   protected PortalObject getDefaultChild()
   {
      String portalName = getDeclaredProperty(PORTAL_PROP_DEFAULT_OBJECT_NAME);
      if (portalName == null)
      {
         portalName = DEFAULT_OBJECT_NAME;
      }
      return getChild(portalName);
   }

   protected abstract PortalObjectImpl cloneObject();

   /** Overridable callback. */
   protected void destroy()
   {
   }

   protected final int getMask()
   {
      return getMask(getType());
   }

   /** Returns the mask for this kind of object. */
   protected final int getMask(int portalObjectType)
   {
      switch (portalObjectType)
      {
         case TYPE_CONTEXT:
            return CONTEXT_MASK;
         case TYPE_PORTAL:
            return PORTAL_MASK;
         case TYPE_PAGE:
            return PAGE_MASK;
         case TYPE_WINDOW:
            return WINDOW_MASK;
         default:
            throw new IllegalArgumentException("Unknown type " + portalObjectType);
      }
   }

//   protected final DashboardContext getDashboardContext()
//   {
//      if (dashboardContext == null)
//      {
//         // Get parent object
//         PortalObjectImpl parent = (PortalObjectImpl)getParent();
//
//         // We need the parent to find out the context details
//         if (this instanceof ContextImpl)
//         {
//            String dashboard = getDeclaredProperty("dashboard");
//            if ("true".equals(dashboard))
//            {
//               String dashboardId = getName();
//               dashboardContext = new DashboardContext(dashboardId);
//            }
//         }
//         else if (parent != null)
//         {
//            dashboardContext = parent.getDashboardContext();
//         }
//      }
//
//      //
//      return dashboardContext;
//   }

   protected final void addChild(String name, PortalObjectImpl childObject) throws DuplicatePortalObjectException, IllegalArgumentException
   {
      objectNode.addChild(name, childObject);

      //
      getAccessedChildren().add(childObject);
   }

   private PortalObjectImpl copy(PortalObjectImpl parent, String name, boolean deep) throws DuplicatePortalObjectException
   {
      // Clone this node
      PortalObjectImpl clone = cloneObject();

      // Add the clone to the specified parent
      parent.addChild(name, clone);

      // Clone children recursively
      if (deep)
      {
         for (Iterator i = getChildren().iterator(); i.hasNext();)
         {
            PortalObjectImpl child = (PortalObjectImpl)i.next();

            //
            child.copy(clone, child.getName(), true);
         }
      }

      //
      return clone;
   }
}



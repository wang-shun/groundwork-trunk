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
package org.jboss.portal.core.impl.api.node;

import org.jboss.portal.api.PortalRuntimeContext;
import org.jboss.portal.api.node.PortalNode;
import org.jboss.portal.api.node.PortalNodeURL;
import org.jboss.portal.common.i18n.LocalizedString;
import org.jboss.portal.common.i18n.ResourceBundleManager;
import org.jboss.portal.common.i18n.SimpleResourceBundleFactory;
import org.jboss.portal.common.path.RelativePathParser;
import org.jboss.portal.core.impl.api.PortalRuntimeContextImpl;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.PortalObjectPermission;
import org.jboss.portal.security.spi.auth.PortalAuthorizationManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 12623 $
 */
public class PortalNodeImpl implements PortalNode
{

   /** Order. */
   private static final String ORDER = "order";

   /** The prefix for resources in the bundle. */
   private static final String RESOURCE_PREFIX = "PAGENAME_";

   /** . */
   private static final String BUNDLE_BASE_NAME = "conf.bundles.Resource";

   /** . */
   private static final ResourceBundleManager resourceBundles = new ResourceBundleManager(null, new SimpleResourceBundleFactory(BUNDLE_BASE_NAME, PortalNodeImpl.class.getClassLoader()));

   /** The wrapped portal object. */
   final PortalObject object;

   /** The parent node. */
   private PortalNodeImpl parentNode;

   /** The children. */
   private final NodeList children;

   /** The root node. */
   private PortalNode root;

   /** The key for the display name in the resource bundle. */
   private String displayNameKey;

   /** The security checks. */
   private final PortalAuthorizationManager portalAuthorizationManager;

   /**
    * Build a portal node object wrapping the specified portal object.
    *
    * @param object the wrapped portal object
    * @throws IllegalArgumentException if the specified object is null
    */
   public PortalNodeImpl(PortalAuthorizationManager portalAuthorizationManager, PortalObject object) throws IllegalArgumentException
   {
      if (object == null)
      {
         throw new IllegalArgumentException();
      }
      this.portalAuthorizationManager = portalAuthorizationManager;
      this.object = object;
      this.children = new Children(this);
   }

   /**
    * Used during the construction of a children list.
    *
    * @param parentNode the parent node of this node
    * @param object     the wrapped portal object
    */
   private PortalNodeImpl(PortalNodeImpl parentNode, PortalObject object)
   {
      this.portalAuthorizationManager = parentNode.portalAuthorizationManager;
      this.parentNode = parentNode;
      this.object = object;
      this.children = new Children(this);
   }

   /**
    * Used when building the parent.
    *
    * @param object    the wrapped portal object
    * @param childNode the child node creating that object
    */
   private PortalNodeImpl(PortalObject object, PortalNodeImpl childNode)
   {
      this.portalAuthorizationManager = childNode.portalAuthorizationManager;
      this.object = object;
      this.children = new Siblings(childNode);
   }

   public int getType()
   {
      return object.getType();
   }

   public PortalNode getRoot()
   {
      if (root == null)
      {
         PortalNode parent = getParent();
         if (parent == null)
         {
            root = this;
         }
         else
         {
            root = parent.getRoot();
         }
      }
      return root;
   }

   public PortalNode getParent()
   {
      if (parentNode == null)
      {
         PortalObject objectParent = object.getParent();
         if (objectParent != null)
         {
            parentNode = new PortalNodeImpl(objectParent, this);
         }
      }
      return parentNode;
   }

   public String getName()
   {
      return object.getName();
   }

   public String getDisplayName(Locale locale)
   {
      LocalizedString ldisplayName = object.getDisplayName();
      if (ldisplayName != null)
      {
         String result = ldisplayName.getString(locale, true);
         if (result != null)
         {
            return result;
         }
      }

      // Lazily compute the display name
      if (displayNameKey == null)
      {
         displayNameKey = RESOURCE_PREFIX + object.getName();
      }

      // Try to get the display name from the resource bundles for backward compatibility
      String displayName = null;
      ResourceBundle bundle = null;
      try
      {
         bundle = resourceBundles.getResourceBundle(locale);
      }
      catch (MissingResourceException ignore)
      {
      }

      if (bundle != null)
      {
         try
         {
            displayName = bundle.getString(displayNameKey);
         }
         catch (MissingResourceException ignore)
         {
         }
      }

      // If nothing found just use the name
      if (displayName == null)
      {
         displayName = object.getName();
      }

      //
      return displayName;
   }

   public PortalNode getChild(String name)
   {
      return (PortalNode)children.getMap().get(name);
   }

   public Collection getChildren()
   {
      return children.getList();
   }

   public PortalNode resolve(String relativePath)
   {
      // Use this as a starting point
      PortalNode node = this;

      //
      RelativePathParser cursor = new RelativePathParser(relativePath);
      for (int i = cursor.next(); i != RelativePathParser.NONE && node != null; i = cursor.next())
      {
         switch (i)
         {
            case RelativePathParser.DOWN:
               String name = relativePath.substring(cursor.getOffset(), cursor.getOffset() + cursor.getLength());
               node = node.getChild(name);
               break;
            case RelativePathParser.UP:
               node = node.getParent();
               break;
         }
      }
      return node;
   }

   public Map getProperties()
   {
      return object.getProperties();
   }

   public PortalNodeURL createURL(PortalRuntimeContext portalRuntimeContext)
   {
      PortalRuntimeContextImpl crc = (PortalRuntimeContextImpl)portalRuntimeContext;

      //
      return crc.getURLFactory().createURL(this);
   }

   public PortalObjectId getObjectId()
   {
      return object.getId();
   }

   private float getWeight()
   {
      switch (object.getType())
      {
         case PortalObject.TYPE_CONTEXT:
            return 0;
         case PortalObject.TYPE_PORTAL:
            return 1;
         case PortalObject.TYPE_PAGE:
            return 2;
         case PortalObject.TYPE_WINDOW:
            return 3;
         default:
            return 4;
      }
   }

   private static final Comparator siblingComparator = new Comparator()
   {
      public int compare(Object o1, Object o2)
      {
         PortalNodeImpl node1 = (PortalNodeImpl)o1;
         PortalNodeImpl node2 = (PortalNodeImpl)o2;
         float weight1 = node1.getWeight();
         float weight2 = node2.getWeight();
         if (weight1 == weight2)
         {
            if (PortalObject.TYPE_PAGE == node1.getType())
            {
               String orderProperty1S = (String)node1.getProperties().get(ORDER);
               String orderProperty2S = (String)node2.getProperties().get(ORDER);

               if (orderProperty1S != null && orderProperty2S == null)
               {
                  return -1;
               }
               else if (orderProperty1S == null && orderProperty2S != null)
               {
                  return 1;
               }
               else if (orderProperty1S != null && orderProperty2S != null)
               {
                  float orderProperty1 = -1;
                  float orderProperty2 = -1;

                  try
                  {
                     orderProperty1 = Float.parseFloat(orderProperty1S);
                     orderProperty2 = Float.parseFloat(orderProperty2S);
                     if (orderProperty1 > orderProperty2)
                     {
                        return 1;
                     }
                     else if (orderProperty1 < orderProperty2)
                     {
                        return -1;
                     }
                  }
                  catch (NumberFormatException e)
                  {
                     // ignore
                  }
               }
            }
            return node1.getName().compareTo(node2.getName());
         }
         else if (weight1 < weight2)
         {
            return -1;
         }
         else
         {
            return 1;
         }
      }
   };

   private abstract class NodeList
   {

      /** . */
      private Map map;

      /** . */
      private List list;

      protected abstract Map createMap();

      public final List getList()
      {
         if (list == null)
         {
            Map childrenMap = getMap();

            //
            list = new ArrayList(childrenMap.values());
            Collections.sort(list, siblingComparator);
            list = Collections.unmodifiableList(list);
         }

         //
         return list;
      }

      public final Map getMap()
      {
         if (map == null)
         {
            map = createMap();
         }

         //
         return map;
      }

      /** Compute and returns a modifiable map made of the children nodes. */
      protected final Map<String, PortalNode> buildChildMap(PortalNodeImpl objectNode)
      {
         PortalObject object = objectNode.object;

         //
         Collection<PortalObject> tmp = object.getChildren();

         //
         if (tmp.size() > 0)
         {
            Map<String, PortalNode> childrenMap = new HashMap<String, PortalNode>();

            // See if we have recursive permission on the provided node that will avoid to make a check for each of them
            boolean allVisible = portalAuthorizationManager.checkPermission(new PortalObjectPermission(objectNode.object.getId(), PortalObjectPermission.VIEW_RECURSIVE_ACTION));

            //
            for (PortalObject childObject : tmp)
            {
               // It is visible if the parent has recursive view enabled
               boolean visible = allVisible;

               // Check for the particular node
               if (!visible)
               {
                  visible = portalAuthorizationManager.checkPermission(new PortalObjectPermission(childObject.getId(), PortalObjectPermission.VIEW_MASK));
               }

               // We only add it if the user can view the node
               if (visible)
               {
                  PortalNodeImpl child = new PortalNodeImpl(objectNode, childObject);
                  childrenMap.put(child.getName(), child);
               }
            }

            //
            return childrenMap;
         }
         else
         {
            return new HashMap<String, PortalNode>();
         }
      }
   }

   private class Children extends NodeList
   {

      /** . */
      private PortalNodeImpl node;

      public Children(PortalNodeImpl node)
      {
         this.node = node;
      }

      protected Map createMap()
      {
         return buildChildMap(node);
      }
   }

   private class Siblings extends NodeList
   {

      /** . */
      private PortalNodeImpl node;

      private Siblings(PortalNodeImpl node)
      {
         this.node = node;
      }

      protected Map createMap()
      {
         Map childrenNodes = buildChildMap(node.parentNode);

         // Replace the node with the one provided
         childrenNodes.put(node.getName(), node);

         //
         return childrenNodes;
      }
   }
}

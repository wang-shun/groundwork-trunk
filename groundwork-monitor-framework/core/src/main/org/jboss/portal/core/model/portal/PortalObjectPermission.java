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
package org.jboss.portal.core.model.portal;

import org.jboss.portal.security.PortalPermission;
import org.jboss.portal.security.PortalPermissionCollection;
import org.jboss.portal.security.PortalSecurityException;
import org.jboss.portal.security.spi.provider.PermissionRepository;

import javax.security.auth.Subject;
import java.security.Permission;
import java.security.Principal;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * The permission for portal objects hierarchy.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 9081 $
 */
public final class PortalObjectPermission extends PortalPermission
{

   /** The serialVersionUID */
   private static final long serialVersionUID = -4796595968918579499L;

   /** The create action name. */
   public static final String CREATE_ACTION = "create";

   /** The view action name. */
   public static final String VIEW_ACTION = "view";

   /** The view recursive action name. */
   public static final String VIEW_RECURSIVE_ACTION = "viewrecursive";

   /** The personalize action name. */
   public static final String PERSONALIZE_ACTION = "personalize";

   /** The personalize action name. */
   public static final String PERSONALIZE_RECURSIVE_ACTION = "personalizerecursive";

   /** The create action name. */
   public static final String DASHBOARD_ACTION = "dashboard";

   /** No Perms mask. */
   public static final int NONE_MASK = 0x00;

   /** The view mask. */
   public static final int VIEW_MASK = 0x01;

   /** The create mask. */
   public static final int CREATE_MASK = 0x02;

   /** The create mask. */
   public static final int PERSONALIZE_MASK = 0x04;

   /** The dashboard mask. */
   public static final int DASHBOARD_MASK = 0x08;

   /** The action names. */
   private static final String[] ACTION_NAMES = {VIEW_ACTION, CREATE_ACTION, PERSONALIZE_ACTION, DASHBOARD_ACTION};

   /** . */
   private PortalObjectId id;

   /** The imply mask. */
   private int mask;

   /** The recursive imply mask. */
   private int recursiveMask;

   /** The actions string. */
   private String actions;

   /** . */
   public static final String PERMISSION_TYPE = "portalobject";

   public PortalObjectPermission(PortalPermissionCollection collection)
   {
      super("portalobjectpermission", collection);
   }

   public PortalObjectPermission(PortalObjectId id, Collection actions)
   {
      super("portalobjectpermission", id.toString(PortalObjectPath.CANONICAL_FORMAT));

      //
      if (actions == null)
      {
         throw new IllegalArgumentException("Actions agurment cannot be null");
      }

      //
      this.id = id;

      //
      for (Iterator i = actions.iterator(); i.hasNext();)
      {
         String action = (String)i.next();
         addAction(action);
      }

      // Update the mask with the recursive mask
      mask |= recursiveMask;
   }

   public PortalObjectPermission(PortalObjectId id, String actions)
   {
      super("portalobjectpermission", id.toString(PortalObjectPath.CANONICAL_FORMAT));
      if (actions == null)
      {
         throw new IllegalArgumentException("Actions agurment cannot be null");
      }

      //
      this.id = id;

      // Parse the actions into the mask
      StringTokenizer tokenizer = new StringTokenizer(actions, ",");
      while (tokenizer.hasMoreTokens())
      {
         String action = tokenizer.nextToken();
         addAction(action);
      }

      // Update the mask with the recursive mask
      mask |= recursiveMask;
   }

   public PortalObjectPermission(PortalObjectId id, int mask, int recursiveMask)
   {
      super("portalobjectpermission", id.toString(PortalObjectPath.CANONICAL_FORMAT));

      //
      this.id = id;
      this.mask = mask | recursiveMask;
      this.recursiveMask = recursiveMask;
   }

   public PortalObjectPermission(PortalObjectId id, int mask)
   {
      super("portalobjectpermission", id.toString(PortalObjectPath.CANONICAL_FORMAT));

      //
      this.id = id;
      this.mask = mask;
      this.recursiveMask = 0;
   }

   private void addAction(String action) throws IllegalArgumentException
   {
      if (VIEW_ACTION.equals(action))
      {
         mask |= VIEW_MASK;
      }
      else if (VIEW_RECURSIVE_ACTION.equals(action))
      {
         recursiveMask |= VIEW_MASK;
      }
      else if (CREATE_ACTION.equals(action))
      {
         mask |= CREATE_MASK;
      }
      else if (PERSONALIZE_ACTION.equals(action))
      {
         mask |= PERSONALIZE_MASK;
      }
      else if (PERSONALIZE_RECURSIVE_ACTION.equals(action))
      {
         recursiveMask |= PERSONALIZE_MASK;
      }
      else if (DASHBOARD_ACTION.equals(action))
      {
         mask |= DASHBOARD_MASK;
      }
      else
      {
         throw new IllegalArgumentException("Illegal action " + action);
      }
   }

   public boolean implies(PermissionRepository repository, Subject caller, String roleName, PortalPermission permission) throws PortalSecurityException
   {
      if (permission instanceof PortalObjectPermission)
      {
         PortalObjectPermission pop = (PortalObjectPermission)permission;

         // If no uri then the permission is a container
         if (pop.isContainer())
         {
            return false;
         }
         else
         {
            String namespace = pop.id.getNamespace();
            PortalObjectPath path = pop.id.getPath();

            //
            while (true)
            {
               String uri = PortalObjectId.toString(namespace, path, PortalObjectPath.CANONICAL_FORMAT);

               // Try to load the permission from the repository
               PortalObjectPermission loaded = (PortalObjectPermission)repository.getPermission(roleName, uri);

               // If it is loaded and implies then we return true
               if (loaded != null && loaded.implies(pop, caller))
               {
                  return true;
               }

               // Get the parent uri
               if (path.getLength() == 0)
               {
                  return false;
               }

               // Get parent path
               path = path.getParent();
            }
         }
      }
      return false;
   }

   public boolean implies(Permission permission)
   {
      return implies(permission, null);
   }

   public boolean implies(Permission permission, Subject caller)
   {
      if (permission instanceof PortalObjectPermission && !isContainer())
      {
         PortalObjectPermission that = (PortalObjectPermission)permission;

         //
         if (!that.isContainer())
         {
            if (!id.getNamespace().equals(that.id.getNamespace()))
            {
               return false;
            }

            PortalObjectPath thisPath = id.getPath();
            PortalObjectPath thatPath = that.id.getPath();

            //
            if ((this.mask & DASHBOARD_MASK) == DASHBOARD_MASK &&
               caller != null &&
               thisPath.getLength() < thatPath.getLength())
            {
               Set tmp = caller.getPrincipals();
               if (tmp.size() > 0)
               {
                  Iterator i1 = thisPath.names();
                  Iterator i2 = thatPath.names();

                  //
                  while (i1.hasNext())
                  {
                     String name1 = (String)i1.next();
                     String name2 = (String)i2.next();
                     if (!name1.equals(name2))
                     {
                        return false;
                     }
                  }

                  //
                  Iterator i = tmp.iterator();
                  Principal user = (Principal)i.next();
                  String userName = user.getName();

                  //
                  return userName.equals(i2.next());
               }
            }

            // Could check namespace and id instead
            if (that.uri.equals(this.uri))
            {
               if (that.recursiveMask != 0 && (this.recursiveMask & that.mask) != that.mask)
               {
                  return false;
               }
               return (this.mask & that.mask) == that.mask;
            }
            else if (that.uri.startsWith(this.uri))
            {
               return (this.recursiveMask & that.mask) == that.mask;
            }
         }
      }
      return false;
   }

   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }
      if (obj instanceof PortalObjectPermission)
      {
         PortalObjectPermission that = (PortalObjectPermission)obj;
         if (this.isContainer())
         {
            return that.isContainer();
         }
         return this.mask == that.mask && this.uri.equals(that.uri);
      }
      return false;
   }

   public int hashCode()
   {
      if (isContainer())
      {
         return 0;
      }
      else
      {
         return uri.hashCode() * 43 + mask;
      }
   }

   public String getActions()
   {
      if (actions == null)
      {
         StringBuffer tmp = new StringBuffer();

         //
         for (int i = 0; i < ACTION_NAMES.length; i++)
         {
            int mask = 2 >> i;
            if ((this.mask & mask) == mask)
            {
               tmp.append(ACTION_NAMES[i]);
               if ((recursiveMask & mask) == mask)
               {
                  tmp.append("recursive,");
               }
               else
               {
                  tmp.append(',');
               }
            }
            else if ((recursiveMask & mask) == mask)
            {
               tmp.append(ACTION_NAMES[i]).append("recursive,");
            }

         }

         //
         int length = tmp.length();
         if (length > 0)
         {
            tmp.setLength(length - 1);
         }
         actions = tmp.toString();
      }
      return actions;
   }

   public String getType()
   {
      return PERMISSION_TYPE;
   }

   public PortalObjectId getId()
   {
      return id;
   }
}

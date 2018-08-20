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
package org.jboss.portal.core.model.instance;

import org.jboss.portal.security.PortalPermission;
import org.jboss.portal.security.PortalPermissionCollection;
import org.jboss.portal.security.PortalSecurityException;
import org.jboss.portal.security.spi.provider.PermissionRepository;

import javax.security.auth.Subject;
import java.security.Permission;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * The permission for instance.
 *
 * @author <a href="mailto:anil.saldhana@jboss.org">Anil Saldhana</a>
 * @version $Revision: 8786 $
 */
public final class InstancePermission extends PortalPermission
{

   /** The serialVersionUID */
   private static final long serialVersionUID = 8445291296726152562L;

   /** The view action name. */
   public static final String VIEW_ACTION = "view";

   /** The create action name. */
   public static final String CREATE_ACTION = "create";

   /** The destroy action name. */
   public static final String DESTROY_ACTION = "destroy";

   /** The customize action name. */
   public static final String CUSTOMIZE_ACTION = "customize";

   /** The customize action name. */
   public static final String ADMIN_ACTION = "admin";

   /** . */
   public static final int VIEW_MASK = 0x01;

   /** . */
   public static final int CREATE_MASK = 0x02;

   /** . */
   public static final int DESTROY_MASK = 0x04;

   /** . */
   public static final int CUSTOMIZE_MASK = 0x08;

   /** . */
   public static final int ADMIN_MASK = VIEW_MASK | CREATE_MASK | DESTROY_MASK | CUSTOMIZE_MASK;

   /** The imply mask. */
   private int mask;

   /** The actions string. */
   private String actions;

   /** . */
   public static final String PERMISSION_TYPE = "instance";

   /** The action names. */
   private static final String[] ACTION_NAMES = {VIEW_ACTION, CREATE_ACTION, DESTROY_ACTION, CUSTOMIZE_ACTION};

   public InstancePermission(PortalPermissionCollection collection)
   {
      super("instancepermission", collection);
   }

   public InstancePermission(String uri, Collection actions)
   {
      super("instancepermission", uri);
      if (actions == null)
      {
         throw new IllegalArgumentException("Actions agurment cannot be null");
      }

      //
      for (Iterator i = actions.iterator(); i.hasNext();)
      {
         String action = (String)i.next();
         addAction(action);
      }
   }

   public InstancePermission(String uri, int mask)
   {
      super("instancepermission", uri);
      this.mask = mask;
   }

   public InstancePermission(String uri, String actions)
   {
      super("instancepermission", uri);
      if (actions == null)
      {
         throw new IllegalArgumentException("Actions agurment cannot be null");
      }

      // Parse the actions into the mask
      StringTokenizer tokenizer = new StringTokenizer(actions, ",");
      while (tokenizer.hasMoreTokens())
      {
         String action = tokenizer.nextToken();
         addAction(action);
      }
   }

   private void addAction(String action) throws IllegalArgumentException
   {
      if (VIEW_ACTION.equals(action))
      {
         mask |= VIEW_MASK;
      }
      else if (CREATE_ACTION.equals(action))
      {
         mask |= CREATE_MASK;
      }
      else if (DESTROY_ACTION.equals(action))
      {
         mask |= DESTROY_MASK;
      }
      else if (CUSTOMIZE_ACTION.equals(action))
      {
         mask |= CUSTOMIZE_MASK;
      }
      else if (ADMIN_ACTION.equals(action))
      {
         mask |= ADMIN_MASK;
      }
      else
      {
         throw new IllegalArgumentException("Illegal action " + action);
      }
   }

   public boolean implies(PermissionRepository repository, Subject caller, String roleName, PortalPermission permission) throws PortalSecurityException
   {
      if (permission instanceof InstancePermission)
      {
         InstancePermission pp = (InstancePermission)permission;

         // If no uri then the permission is a container
         if (pp.isContainer())
         {
            return false;
         }
         else
         {
            String uri = pp.getURI();
            PortalPermission loaded = repository.getPermission(roleName, uri);
            if (loaded != null && loaded.implies(pp))
            {
               return true;
            }
         }
      }
      return false;
   }

   public boolean implies(Permission permission)
   {
      if (permission instanceof InstancePermission && isContainer() == false)
      {
         InstancePermission that = (InstancePermission)permission;

         //
         if (that.isContainer() == false && that.uri.equals(this.uri))
         {
            return (this.mask & that.mask) == that.mask;
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
      if (obj instanceof InstancePermission)
      {
         InstancePermission that = (InstancePermission)obj;
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
               tmp.append(ACTION_NAMES[i]).append(',');
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
}

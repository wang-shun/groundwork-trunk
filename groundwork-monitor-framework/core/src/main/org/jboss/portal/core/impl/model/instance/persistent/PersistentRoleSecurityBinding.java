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
package org.jboss.portal.core.impl.model.instance.persistent;

import org.jboss.portal.core.model.instance.Instance;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Security Constraint for an instance
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
final class PersistentRoleSecurityBinding implements Serializable
{

   /** The serialVersionUID */
   private static final long serialVersionUID = -2832148715381794267L;

   /** The primary key. */
   private Long key;

   /** The role of this contraint. */
   private String role;

   /** The set of actions of this constraint. */
   private Set actions;

   /** The cached toString value. */
   private transient String toString;

   /** The cached hash code. */
   private transient int hashCode;

   /** The cached actions as a string. */
   private transient String actionsAsString;

   private Instance instance;

   public PersistentRoleSecurityBinding()
   {
      super();
   }

   /**
    * Create a new constraint with the provided actions for the specified role.
    *
    * @param actions a comma separated list of allowed actions
    * @param role    the role name
    */
   public PersistentRoleSecurityBinding(String actions, String role)
   {
      if (role == null)
      {
         throw new IllegalArgumentException("Role cannot be null");
      }
      if (actions == null)
      {
         throw new IllegalArgumentException("Actions cannot be null");
      }

      //
      StringTokenizer tokens = new StringTokenizer(actions, ",");
      Set set = new HashSet();
      while (tokens.hasMoreTokens())
      {
         set.add(tokens.nextToken().trim());
      }

      //
      this.role = role;
      this.actions = Collections.unmodifiableSet(set);
   }

   /**
    * Create a new constraint with the provided actions and the specified role.
    *
    * @param actions the set of actions
    * @param role    the role name
    */
   public PersistentRoleSecurityBinding(Set actions, String role)
   {
      if (role == null)
      {
         throw new IllegalArgumentException("Role cannot be null");
      }
      if (actions == null)
      {
         throw new IllegalArgumentException("Actions cannot be null");
      }

      //
      this.role = role;
      this.actions = Collections.unmodifiableSet(new HashSet(actions));
   }

   /** Copy constructor. */
   public PersistentRoleSecurityBinding(PersistentRoleSecurityBinding other)
   {
      if (other == null)
      {
         throw new IllegalArgumentException("The constraint to clone cannot be null");
      }

      //
      this.role = other.role;
      this.actions = other.actions;
   }

   /**
    * Return a <code>java.util.Set<String></code> of allowed actions.
    *
    * @return the action set
    */
   public Set getActions()
   {
      return actions;
   }

   /**
    * Return the role of this constraint
    *
    * @return the role
    */
   public String getRole()
   {
      return role;
   }

   /**
    * Return a comma separated list of actions.
    *
    * @return the action string representation
    */
   public String getActionsAsString()
   {
      if (actionsAsString == null)
      {
         StringBuffer tmp = new StringBuffer();
         for (Iterator i = actions.iterator(); i.hasNext();)
         {
            String action = (String)i.next();
            if (i.hasNext())
            {
               tmp.append(", ");
            }
            tmp.append(action);
         }
         actionsAsString = tmp.toString();
      }
      return actionsAsString;
   }

   /** @see Object#toString */
   public String toString()
   {
      if (toString == null)
      {
         StringBuffer tmp = new StringBuffer("SecurityConstraint: actions [");
         for (Iterator i = actions.iterator(); i.hasNext();)
         {
            String action = (String)i.next();
            if (i.hasNext())
            {
               tmp.append(", ");
            }
            tmp.append(action);
         }
         tmp.append("] role [").append(role).append("]");
         toString = tmp.toString();
      }
      return toString;
   }

   public boolean equals(Object o)
   {
      if (this == o)
      {
         return true;
      }
      if (o instanceof PersistentRoleSecurityBinding)
      {
         PersistentRoleSecurityBinding that = (PersistentRoleSecurityBinding)o;
         return actions.equals(that.actions) && role.equals(that.role);
      }
      return false;
   }

   public int hashCode()
   {
      if (hashCode == 0)
      {
         int hashCode;
         hashCode = actions.hashCode();
         hashCode = 29 * hashCode + role.hashCode();
         this.hashCode = hashCode;
      }
      return hashCode;
   }

   protected void setKey(Long k)
   {
      key = k;
   }

   protected Long getKey()
   {
      return key;
   }

   public void setActions(Set actions)
   {
      this.actions = actions;
   }

   public void setRole(String role)
   {
      this.role = role;
   }

   public Instance getInstance()
   {
      return instance;
   }

   public void setInstance(Instance instance)
   {
      this.instance = instance;
   }
}

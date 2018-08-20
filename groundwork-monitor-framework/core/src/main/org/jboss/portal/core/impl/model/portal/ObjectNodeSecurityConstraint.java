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
package org.jboss.portal.core.impl.model.portal;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Binds a role and a set of actions together. This object is immutable. <p>A portal resource (portal, page, window,
 * instance, portlet...) is secured via a set of security constraints. each security constraint holds the information
 * about what roles are allowed what actions.</p>
 *
 * @author <a href="mailto:mholzner@novell.com">Martin Holzner</a>
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public final class ObjectNodeSecurityConstraint implements Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -4506735776282080236L;

   private long id;

   /** The role of this contraint. */
   private String role;

   /** The set of actions of this constraint. */
   private Set actions;

   /** The cached toString value. */
   private transient String toString;

   /** The ObjectNode to which this security constraint is attached to . */
   private ObjectNode objectNode;


   public ObjectNodeSecurityConstraint()
   {
   }

   /**
    * Create a new constraint with the provided actions and the specified role.
    *
    * @param actions the set of actions
    * @param role    the role name
    */
   public ObjectNodeSecurityConstraint(Set actions, String role)
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
      this.actions = new HashSet(actions);
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

   public void setActions(Set actions)
   {
      this.actions = actions;
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

   public void setRole(String role)
   {
      this.role = role;
   }

   public ObjectNode getObjectNode()
   {
      return objectNode;
   }

   public void setObjectNode(ObjectNode objectNode)
   {
      this.objectNode = objectNode;
   }

   protected void setKey(long key)
   {
      id = key;
   }

   protected long getKey()
   {
      return id;
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
}

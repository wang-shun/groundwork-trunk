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

import org.jboss.portal.core.model.portal.Context;

import java.util.Collection;

/**
 * An implementation of the <code>Context</code> interface for dashboards.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public abstract class DashboardContext implements Context
{


   public Collection getChildren()
   {
      throw new UnsupportedOperationException();
   }

   public Collection getChildren(int mask)
   {
      throw new UnsupportedOperationException();
   }

//
//   public Portal getPortal(String name) throws IllegalArgumentException
//   {
//      return null;  //To change body of implemented methods use File | Settings | File Templates.
//   }
//
//   public Portal createPortal(String name) throws DuplicatePortalObjectException, IllegalArgumentException
//   {
//      return null;  //To change body of implemented methods use File | Settings | File Templates.
//   }
//
//   public Portal getDefaultPortal()
//   {
//      return null;  //To change body of implemented methods use File | Settings | File Templates.
//   }
//
//   public PortalContainer getPortalContainer(String name) throws IllegalArgumentException
//   {
//      return null;  //To change body of implemented methods use File | Settings | File Templates.
//   }
//
//   public PortalContainer createPortalContainer(String name) throws DuplicatePortalObjectException, IllegalArgumentException
//   {
//      return null;  //To change body of implemented methods use File | Settings | File Templates.
//   }
//
//   public PortalObjectId getId()
//   {
//      return null;  //To change body of implemented methods use File | Settings | File Templates.
//   }
//
//   public int getType()
//   {
//      return 0;  //To change body of implemented methods use File | Settings | File Templates.
//   }
//
//   public String getName()
//   {
//      return null;  //To change body of implemented methods use File | Settings | File Templates.
//   }
//
//   public String getListener()
//   {
//      return null;  //To change body of implemented methods use File | Settings | File Templates.
//   }
//
//   public void setListener(String listener)
//   {
//      //To change body of implemented methods use File | Settings | File Templates.
//   }
//
//   public Collection getChildren()
//   {
//      return null;  //To change body of implemented methods use File | Settings | File Templates.
//   }
//
//   public Collection getChildren(int mask)
//   {
//      return null;  //To change body of implemented methods use File | Settings | File Templates.
//   }
//
//   public PortalObject getParent()
//   {
//      return null;  //To change body of implemented methods use File | Settings | File Templates.
//   }
//
//   public PortalObject getChild(String name)
//   {
//      return null;  //To change body of implemented methods use File | Settings | File Templates.
//   }
//
//   public void destroyChild(String name) throws NoSuchPortalObjectException, IllegalArgumentException
//   {
//      //To change body of implemented methods use File | Settings | File Templates.
//   }
//
//   public PortalObject copy(PortalObject parent, String name, boolean deep) throws DuplicatePortalObjectException, IllegalArgumentException
//   {
//      return null;  //To change body of implemented methods use File | Settings | File Templates.
//   }
//
//   public String getProperty(String name) throws IllegalArgumentException
//   {
//      return null;  //To change body of implemented methods use File | Settings | File Templates.
//   }
//
//   public Map getProperties()
//   {
//      return null;  //To change body of implemented methods use File | Settings | File Templates.
//   }
//
//   public String getDeclaredProperty(String name) throws IllegalArgumentException
//   {
//      return null;  //To change body of implemented methods use File | Settings | File Templates.
//   }
//
//   public void setDeclaredProperty(String name, String value) throws IllegalArgumentException
//   {
//      //To change body of implemented methods use File | Settings | File Templates.
//   }
//
//   public Map getDeclaredProperties()
//   {
//      return null;  //To change body of implemented methods use File | Settings | File Templates.
//   }
}

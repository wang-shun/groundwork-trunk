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
package org.jboss.portal.core.impl.portlet.state;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;
import org.jboss.portal.registration.RegistrationStatus;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class RegistrationStatusUserType implements UserType
{

   /** . */
   private static final int[] SQL_TYPES = {Hibernate.INTEGER.sqlType()};

   public int[] sqlTypes()
   {
      return SQL_TYPES;
   }

   public Class returnedClass()
   {
      return RegistrationStatus.class;
   }

   public boolean equals(Object x, Object y) throws HibernateException
   {
      if (x == y)
      {
         return true;
      }
      else if (x == null || y == null)
      {
         return false;
      }
      else
      {
         return x.equals(y);
      }
   }

   public int hashCode(Object x) throws HibernateException
   {
      return x.hashCode();
   }

   public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner) throws HibernateException, SQLException
   {
      int value = resultSet.getInt(names[0]);
      if (resultSet.wasNull())
      {
         return null;
      }

      //
      switch (value)
      {
         case 1:
            return RegistrationStatus.VALID;
         case 2:
            return RegistrationStatus.INVALID;
         default:
            return RegistrationStatus.PENDING;
      }
   }

   public void nullSafeSet(PreparedStatement statement, Object value, int index) throws HibernateException, SQLException
   {
      if (value == null)
      {
         statement.setNull(index, SQL_TYPES[0]);
      }
      else
      {
         RegistrationStatus status = (RegistrationStatus)value;
         int i = 0;
         if (status == RegistrationStatus.VALID)
         {
            i = 1;
         }
         else if (status == RegistrationStatus.INVALID)
         {
            i = 2;
         }
         statement.setInt(index, i);
      }
   }

   public Object deepCopy(Object value) throws HibernateException
   {
      return value;
   }

   public boolean isMutable()
   {
      return false;
   }

   public Serializable disassemble(Object value) throws HibernateException
   {
      return (RegistrationStatus)value;
   }

   public Object assemble(Serializable cached, Object owner) throws HibernateException
   {
      return cached;
   }

   public Object replace(Object original, Object target, Object owner) throws HibernateException
   {
      return original;
   }
}

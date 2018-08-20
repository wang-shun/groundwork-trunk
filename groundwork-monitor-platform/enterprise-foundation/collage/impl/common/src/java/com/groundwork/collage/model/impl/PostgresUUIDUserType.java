/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2014  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.groundwork.collage.model.impl;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

/**
 * PostgresUUIDUserType
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class PostgresUUIDUserType implements UserType {

    @Override
    public int[] sqlTypes() {
        return new int[] {Types.OTHER};
    }

    @Override
    public Class returnedClass() {
        return UUID.class;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        if (!UUID.class.isAssignableFrom(x.getClass())) {
			throw new HibernateException(x.getClass().toString() + " cannot be cast to UUID");
		} else if (!UUID.class.isAssignableFrom(y.getClass())) {
			throw new HibernateException(y.getClass().toString() + " cannot be cast to UUID");
		}
        return x.equals(y);
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        if (!UUID.class.isAssignableFrom(x.getClass())) {
            throw new HibernateException(x.getClass().toString() + " cannot be cast to UUID");
        }
        return x.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
        Object value = rs.getObject(names[0]);
        return ((value == null) ? null : (UUID.class.isAssignableFrom(value.getClass()) ? (UUID)value : UUID.fromString(value.toString())));
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            if (!UUID.class.isAssignableFrom(value.getClass())) {
                throw new HibernateException(value.getClass().toString() + " cannot be cast to UUID");
            }
            st.setObject(index, value, Types.OTHER);
        }
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        if (!UUID.class.isAssignableFrom(value.getClass())) {
            throw new HibernateException(value.getClass().toString() + " cannot be cast to UUID");
        }
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        if (!UUID.class.isAssignableFrom(value.getClass())) {
            throw new HibernateException(value.getClass().toString() + " cannot be cast to UUID");
        }
        return (Serializable)value;
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        if (!UUID.class.isAssignableFrom(cached.getClass())) {
            throw new HibernateException(cached.getClass().toString() + " cannot be cast to UUID");
        }
        return cached;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        if (!UUID.class.isAssignableFrom(original.getClass())) {
            throw new HibernateException(original.getClass().toString() + " cannot be cast to UUID");
        }
        return original;
    }
}

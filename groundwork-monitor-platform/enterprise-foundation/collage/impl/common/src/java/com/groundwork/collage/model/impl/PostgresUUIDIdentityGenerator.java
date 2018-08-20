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
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.AbstractPostInsertGenerator;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerationException;
import org.hibernate.id.PostInsertIdentityPersister;
import org.hibernate.id.insert.AbstractReturningDelegate;
import org.hibernate.id.insert.IdentifierGeneratingInsert;
import org.hibernate.id.insert.InsertGeneratedIdentifierDelegate;
import org.hibernate.type.Type;
import org.hibernate.util.GetGeneratedKeysHelper;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;

/**
 * PostgresUUIDIdentityGenerator
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class PostgresUUIDIdentityGenerator extends AbstractPostInsertGenerator implements Configurable {

    private String entityName;

    @Override
    public Serializable generate(SessionImplementor session, Object obj) {
        // check for assigned id
        Serializable id = session.getEntityPersister(entityName, obj).getIdentifier(obj, session.getEntityMode());
        if (id != null) {
            return id;
        }
        // post insert generator
        return super.generate(session, obj);
    }

    @Override
    public InsertGeneratedIdentifierDelegate getInsertGeneratedIdentifierDelegate(PostInsertIdentityPersister persister, Dialect dialect, boolean isGetGeneratedKeysEnabled) throws HibernateException {
        if (!isGetGeneratedKeysEnabled) {
            throw new HibernateException("Get generated keys support required for generator.");
        }
        return new GetGeneratedKeysDelegate(persister, dialect);
    }

    public static class GetGeneratedKeysDelegate extends AbstractReturningDelegate implements InsertGeneratedIdentifierDelegate {

        private final PostInsertIdentityPersister persister;
        private final Dialect dialect;

        public GetGeneratedKeysDelegate(PostInsertIdentityPersister persister, Dialect dialect) {
            super(persister);
            this.persister = persister;
            this.dialect = dialect;
        }

        @Override
        protected PreparedStatement prepare(String insertSQL, SessionImplementor session) throws SQLException {
            return session.getBatcher().prepareStatement(insertSQL, true);
        }

        @Override
        protected Serializable executeAndExtract(PreparedStatement insert) throws SQLException {
            insert.executeUpdate();
            ResultSet rs = GetGeneratedKeysHelper.getGeneratedKey(insert);
            if (!rs.next()) {
                throw new HibernateException("Database did not return generated keys for generator.");
            }
            Class typeClass = persister.getIdentifierType().getReturnedClass();
            if (typeClass != UUID.class) {
                throw new IdentifierGenerationException("Generator is expected to return UUID");
            }
            Object id = rs.getObject(1);
            return ((id == null) ? null : (UUID.class.isAssignableFrom(id.getClass()) ? (UUID)id : UUID.fromString(id.toString())));
        }

        @Override
        public IdentifierGeneratingInsert prepareIdentifierGeneratingInsert() {
            IdentifierGeneratingInsert insert = new IdentifierGeneratingInsert(dialect);
            insert.addIdentityColumn(persister.getRootTableKeyColumnNames()[0]);
            return insert;
        }
    }

    @Override
    public void configure(Type type, Properties params, Dialect dialect) throws MappingException {
        entityName = params.getProperty(ENTITY_NAME);
        if (entityName == null) {
            throw new MappingException("no entity name");
        }
    }
}

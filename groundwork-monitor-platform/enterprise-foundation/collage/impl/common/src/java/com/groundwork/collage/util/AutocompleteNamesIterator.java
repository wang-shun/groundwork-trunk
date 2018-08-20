/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2015  GroundWork Open Source Solutions info@groundworkopensource.com

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

package com.groundwork.collage.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Iterator;

/**
 * AutocompleteNamesIterator
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class AutocompleteNamesIterator implements Iterator<AutocompleteName>  {

    private static final int NAMES_QUERY_FETCH_SIZE = 100;

    private Session session;
    private boolean saveAutoCommit;
    private Statement statement;
    private ResultSet results;
    private ResultSetMetaData metadata;
    private boolean closed;
    private AutocompleteName nextName;

    /**
     * Construct new iterator based on SQL query returning names or alias name and
     * canonical name.
     *
     * @param sessionFactory hibernate session factory
     * @param namesQuery SQL names query
     */
    public AutocompleteNamesIterator(SessionFactory sessionFactory, String namesQuery) {
        try {
            // perform SQL names query using cursor and current session,
            // (connection set to no auto commit to enable cursor)
            session = sessionFactory.getCurrentSession();
            if (saveAutoCommit = session.connection().getAutoCommit()) {
                session.connection().setAutoCommit(false);
            }
            statement = session.connection().createStatement();
            statement.setFetchSize(NAMES_QUERY_FETCH_SIZE);
            results = statement.executeQuery(namesQuery);
            metadata = results.getMetaData();
        } catch (Exception e) {
            // close on error
            close();
            throw new IllegalStateException("Error initializing iterator: "+e, e);
        }
    }

    @Override
    public boolean hasNext() {
        // fetch next name from cursor results and return
        // true if next name fetched
        if (!closed && (nextName == null)) {
            try {
                if (results.next()) {
                    // fetch next name
                    if (metadata.getColumnCount() == 1) {
                        nextName = new AutocompleteName(results.getString(1));
                    } else if (metadata.getColumnCount() >= 2) {
                        nextName = new AutocompleteName(results.getString(1), results.getString(2));
                    }
                } else {
                    // close if at end
                    close();
                }
            } catch (Exception e) {
                // close on error
                close();
            }
        }
        return (!closed && (nextName != null));
    }

    @Override
    public AutocompleteName next() {
        // fetch next name if not already fetched
        if ((nextName == null) && !hasNext()) {
            throw new IllegalStateException("HostNamesIterator has no next.");
        }
        // return fetched next name
        AutocompleteName next = nextName;
        nextName = null;
        return next;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("HostNamesIterator does not support remove.");
    }

    public void close() {
        // close cursor resources and restore connection
        // if not already closed
        if (!closed) {
            closed = true;
            if (results != null) {
                try {
                    results.close();
                } catch (Exception e) {
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                }
            }
            if (saveAutoCommit && (session != null)) {
                try {
                    session.connection().setAutoCommit(saveAutoCommit);
                } catch (Exception e) {
                }
            }
        }
    }
}

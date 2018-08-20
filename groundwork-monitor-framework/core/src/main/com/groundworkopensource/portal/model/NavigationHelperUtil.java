/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package com.groundworkopensource.portal.model;

import java.io.IOException;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;

/**
 * Basic NavigationHelper helper class, handles SessionFactory. Uses a static
 * initializer for the initial SessionFactory creation.
 * 
 * @author manish_kjain
 * 
 */
public class NavigationHelperUtil {

    /**
     * Logger
     */
    private static final Logger LOGGER = Logger
            .getLogger(NavigationHelperUtil.class);
    /**
     * build the session factory
     */
    private static SessionFactory sessionFactory = buildSessionFactory();

    /**
     * Get Session factory instance
     * 
     * @return SessionFactory
     * @throws IOException
     */
    private static SessionFactory buildSessionFactory() {
        try {
            return (SessionFactory) new InitialContext()
                    .lookup("java:/portal/PortletSessionFactory");
        } catch (NamingException badDatabaseName) {
            LOGGER.error("Error in obtaining Session Factory");
            return null;
        }
    }

    /**
     * Rebuild the SessionFactory with the static Configuration.
     * 
     */
    public static void rebuildSessionFactory() {
        synchronized (sessionFactory) {
            try {
                sessionFactory = buildSessionFactory();
            } catch (Exception ex) {
                LOGGER.error("Error in rebuld SessionFactory");
            }
        }
    }

    /**
     * get sessionFactory
     * 
     * @return sessionFactory
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

}

/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

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

package com.groundwork.collage;

import org.groundwork.foundation.dao.FoundationDAO;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

import com.groundwork.collage.exception.CollageException;

/**
 * Basic Hibernate helper class, handles SessionFactory, Session and
 * Transaction.
 * <p>
 * Uses a static initializer for the initial SessionFactory creation and holds
 * Session and Transactions in thread local variables. All exceptions are
 * wrapped in an unchecked InfrastructureException.
 * 
 * @author <a href="mailto:dtaylor@itgroundwork.com">David Sean Taylor </a>
 * @version $Id: HibernateUtil.java 8692 2007-10-15 20:49:04Z glee $
 */
public class HibernateUtil {
    private static Log log = LogFactory.getLog(HibernateUtil.class);

    private static Configuration configuration;

    private static SessionFactory sessionFactory;

    private static final ThreadLocal threadSession = new ThreadLocal();

    private static final ThreadLocal threadTransaction = new ThreadLocal();

    private static final ThreadLocal threadInterceptor = new ThreadLocal();

    // Create the initial SessionFactory from the default configuration files
    static {
        try {
            /*
            configuration = new Configuration();
            sessionFactory = configuration.configure().buildSessionFactory();
            */
            CollageFactory collageService =  CollageFactory.getInstance();
            collageService.initializeSystem();
            sessionFactory = (SessionFactory)collageService.getAPIObject(CollageFactory.HIBERNATE_SESSION_FACTORY);
            configuration = ((LocalSessionFactoryBean)collageService.getAPIObject("&"+CollageFactory.HIBERNATE_SESSION_FACTORY)).getConfiguration();

            // We could also let Hibernate bind it to JNDI:
            // configuration.configure().buildSessionFactory()
        } catch (Throwable ex) {
            // We have to catch Throwable, otherwise we will miss
            // NoClassDefFoundError and other subclasses of Error
            log.error("Building SessionFactory failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Returns the SessionFactory used for this static class.
     * 
     * @return SessionFactory
     */
    public static SessionFactory getSessionFactory() {
        /*
         * Instead of a static variable, use JNDI: SessionFactory sessions =
         * null; try { Context ctx = new InitialContext(); String jndiName =
         * "java:hibernate/HibernateFactory"; sessions =
         * (SessionFactory)ctx.lookup(jndiName); } catch (NamingException ex) {
         * throw new CollageException(ex); } return sessions;
         */
        return sessionFactory;
    }

    /**
     * Returns the original Hibernate configuration.
     * 
     * @return Configuration
     */
    public static Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Rebuild the SessionFactory with the static Configuration.
     *  
     */
    public static void rebuildSessionFactory() throws CollageException {
        synchronized (sessionFactory) {
            try {
                sessionFactory = getConfiguration().buildSessionFactory();
            } catch (Exception ex) {
                throw new CollageException(ex);
            }
        }
    }

    /**
     * Rebuild the SessionFactory with the given Hibernate Configuration.
     * 
     * @param cfg
     */
    public static void rebuildSessionFactory(Configuration cfg)
            throws CollageException {
        synchronized (sessionFactory) {
            try {
                sessionFactory = cfg.buildSessionFactory();
                configuration = cfg;
            } catch (Exception ex) {
                throw new CollageException(ex);
            }
        }
    }

    /**
     * Retrieves the current Session local to the thread. <p/>If no Session is
     * open, opens a new Session for the running thread.
     * 
     * @return Session
     */
    public static Session getSession() throws CollageException {
        Session s = (Session) threadSession.get();
        try {
            if (s == null) {
                log.debug("Opening new Session for this thread.");
                if (getInterceptor() != null) {
                    log.debug("Using interceptor: "
                            + getInterceptor().getClass());
                    s = getSessionFactory().openSession(getInterceptor());
                } else {
                    s = getSessionFactory().openSession();
                }
                threadSession.set(s);
            }
        } catch (HibernateException ex) {
            throw new CollageException(ex);
        }
        return s;
    }

    /**
     * Closes the Session local to the thread.
     */
    public static void closeSession() throws CollageException {
        try {
            Session s = (Session) threadSession.get();
            threadSession.set(null);
            if (s != null && s.isOpen()) {
                log.debug("Closing Session of this thread.");
                s.close();
            }
        } catch (HibernateException ex) {
            throw new CollageException(ex);
        }
    }

    /**
     * Start a new database transaction.
     */
    public static void beginTransaction() throws CollageException {
        Transaction tx = (Transaction) threadTransaction.get();
        try {
            if (tx == null) {
                log.debug("Starting new database transaction in this thread.");
                tx = getSession().beginTransaction();
                threadTransaction.set(tx);
            }
        } catch (HibernateException ex) {
            throw new CollageException(ex);
        }
    }

    /**
     * Commit the database transaction.
     */
    public static void commitTransaction() throws CollageException {
        Transaction tx = (Transaction) threadTransaction.get();
        try {
            if (tx != null && !tx.wasCommitted() && !tx.wasRolledBack()) {
                log.debug("Committing database transaction of this thread.");
                tx.commit();
            }
            threadTransaction.set(null);
        } catch (HibernateException ex) {
            rollbackTransaction();
            throw new CollageException(ex);
        }
    }

    /**
     * Commit the database transaction.
     */
    public static void rollbackTransaction() throws CollageException {
        Transaction tx = (Transaction) threadTransaction.get();
        try {
            threadTransaction.set(null);
            if (tx != null && !tx.wasCommitted() && !tx.wasRolledBack()) {
                log
                        .debug("Tyring to rollback database transaction of this thread.");
                tx.rollback();
            }
        } catch (HibernateException ex) {
            throw new CollageException(ex);
        } finally {
            closeSession();
        }
    }

    /**
     * Reconnects a Hibernate Session to the current Thread.
     * 
     * @param session
     *                   The Hibernate Session to be reconnected.
     */
    public static void reconnect(Session session) throws CollageException {
        try {
            session.reconnect();
            threadSession.set(session);
        } catch (HibernateException ex) {
            throw new CollageException(ex);
        }
    }

    /**
     * Disconnect and return Session from current Thread.
     * 
     * @return Session the disconnected Session
     */
    public static Session disconnectSession() throws CollageException {

        Session session = getSession();
        try {
            threadSession.set(null);
            if (session.isConnected() && session.isOpen())
                session.disconnect();
        } catch (HibernateException ex) {
            throw new CollageException(ex);
        }
        return session;
    }

    /**
     * Register a Hibernate interceptor with the current thread.
     * <p>
     * Every Session opened is opened with this interceptor after registration.
     * Has no effect if the current Session of the thread is already open,
     * effective on next close()/getSession().
     */
    public static void registerInterceptor(Interceptor interceptor) {
        threadInterceptor.set(interceptor);
    }

    private static Interceptor getInterceptor() {
        Interceptor interceptor = (Interceptor) threadInterceptor.get();
        return interceptor;
    }

    /**
     * initTransaction Make sure that the session is flushed before doing any
     * new queries. This function is typically called at the begining of an
     * application (Servlet/API)
     * 
     * @throws CollageException
     */
    public static void initTransaction() throws CollageException {
        rollbackTransaction();
    }
}

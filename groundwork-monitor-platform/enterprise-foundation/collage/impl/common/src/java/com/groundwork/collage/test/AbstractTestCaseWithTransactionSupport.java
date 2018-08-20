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

package com.groundwork.collage.test;

import com.groundwork.collage.CollageFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 ***************************************************************************************************
 * Extend this class when the Test suite must run tests that change the state of the test database, 
 * and when it is desireable to rollback the changes to the database for one or all of the tests;
 * this class implements beginTransaction() and rollbackTransaction() methods that retrieve the
 * Spring TransactionManager defined in the Spring configuration file, and initiate a new
 * Transaction;  The tearDown method, in turn, rollsback the transaction and any changes that may
 * have occurred to the database during the test.
 *
 * @author  <a href="mailto:philippe.paravicini@eCommerceStudio.com">Philippe Paravicini</a>
 * @version $Revision: 8692 $ - $Date: 2007-10-15 13:49:04 -0700 (Mon, 15 Oct 2007) $
 ***************************************************************************************************
 */
abstract public class AbstractTestCaseWithTransactionSupport extends TestCase
{
	protected Log log = LogFactory.getLog(this.getClass());

	boolean wrapAll = false;

	private PlatformTransactionManager transactionManager;
	private TransactionStatus          transaction;
    private SessionFactory             sessionFactory;
    private Priority                   saveLoggingThreshold;

	public AbstractTestCaseWithTransactionSupport(String x) { super(x); }


	/** 
	 * initiates a Transaction - if all of the tests in the Test suite should be rolled back, 
	 * this method can be called in the setUp() method of a subclass, 
	 * and rollbackTransaction() in the tearDown() method; 
	 * otherwise if only discrete tests should be wrapped in a transaction,
	 * beginTransaction() and rollbackTransaction() can be called at the
	 * suitable points within individual tests
	 */
	public void beginTransaction(boolean bReadOnly)
	{
		if (log.isDebugEnabled()) log.debug("initiating transaction...");
		CollageFactory collage = CollageFactory.getInstance(); 
		assertNotNull("collage factory", collage);

		transactionManager = 
			(PlatformTransactionManager)collage.getAPIObject(CollageFactory.TRANSACTION_MANAGER);
		assertNotNull("transaction manager", transactionManager);


		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
		def.setReadOnly(bReadOnly);

		transaction = transactionManager.getTransaction(def);
	}

	/** 
	 * initiates a Transaction - if all of the tests in the Test suite should be rolled back, 
	 * this method can be called in the setUp() method of a subclass, 
	 * and rollbackTransaction() in the tearDown() method; 
	 * otherwise if only discrete tests should be wrapped in a transaction,
	 * beginTransaction() and rollbackTransaction() can be called at the
	 * suitable points within individual tests
	 */
	public void beginTransaction()
	{
		beginTransaction(false);
	}
	
	/** 
	 * rollsback the transaction to prevent changes to the database - 
	 * depending on where a TestCase calls beginTransaction(),
	 * rollbackTransaction() should be called either in the tearDown() method 
	 * or at a suitable point within a test
	 */
	public void rollbackTransaction() {
		if (log.isDebugEnabled()) log.debug("rolling back transaction...");
		transactionManager.rollback(transaction);
        transaction = null;
	}

	public void commitTransaction()
	{
		if (log.isDebugEnabled()) log.debug("committing back transaction...");
		transactionManager.commit(transaction);
        transaction = null;
	}

    /**
     * Get current Hibernate session.
     *
     * @return Hibernate session
     */
    public Session getSession() {
        if (sessionFactory == null) {
            sessionFactory = (SessionFactory) CollageFactory.getInstance().getAPIObject(CollageFactory.HIBERNATE_SESSION_FACTORY);
        }
        return SessionFactoryUtils.getSession(sessionFactory, false);
    }

    /**
     * Flush and clear Hibernate session.
     */
    public void flushAndClearSession() {
        Session session = getSession();
        session.flush();
        session.clear();
    }

    /**
	 * rollback the transaction if it exists; this is to make sure that any
	 * open transactions are rolled back after an assertion fails
	 */
	protected void tearDown()
	{
        if (transaction != null) {
			if (log.isDebugEnabled()) log.debug("rolling back interrupted transaction...");
			transactionManager.rollback(transaction);
		}

        try {
            super.tearDown();
        }
        catch (Exception exc) {
            log.error(exc.getMessage());
        }
	}

    /**
     * Disable Log4J Hibernate logging.
     */
    protected void disableHibernateLogging() {
        ConsoleAppender loggingConsoleAppender = (ConsoleAppender) Logger.getRootLogger().getAppender("CONSOLE");
        if (loggingConsoleAppender != null) {
            saveLoggingThreshold = loggingConsoleAppender.getThreshold();
            loggingConsoleAppender.setThreshold(Priority.FATAL);
        }
    }

    /**
     * Reenable disabled Log4J Hibernate logging.
     */
    protected void reenableHibernateLogging() {
        ConsoleAppender loggingConsoleAppender = (ConsoleAppender) Logger.getRootLogger().getAppender("CONSOLE");
        if ((loggingConsoleAppender != null) && (saveLoggingThreshold != null)) {
            loggingConsoleAppender.setThreshold(saveLoggingThreshold);
        }
    }
}

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

package com.groundwork.collage;

import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.metrics.CollageMetrics;
import com.groundwork.collage.metrics.CollageTimer;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * HibernateProgrammaticTxnSupport - execute runnable in programmatic
 * transaction with retry.
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class HibernateProgrammaticTxnSupport {

    private static CollageMetrics collageMetrics;

    private static CollageMetrics getCollageMetrics() {
        if (collageMetrics == null) {
            collageMetrics = CollageFactory.getInstance().getCollageMetrics();
        }
        return collageMetrics;
    }

    public static CollageTimer startMetricsTimer(String methodName) {
        CollageMetrics collageMetrics = getCollageMetrics();
        return (collageMetrics == null ? null : collageMetrics.startTimer("HibernateProgrammaticTxnSupport", methodName));
    }

    public static void stopMetricsTimer(CollageTimer timer) {
        CollageMetrics collageMetrics = getCollageMetrics();
        if (collageMetrics != null) getCollageMetrics().stopTimer(timer);
    }


    public enum RunInTxnRetry { RETRY, RETURN, EXCEPTION };

    /**
     * Interface to describe runnable to execute with a programmatic
     * transaction with retry.
     */
    public interface RunInTxn {
        /**
         * Execute runnable in transaction.
         * @return runnable result
         * @throws Exception
         */
        Object run() throws Exception;

        /**
         * Test runnable result for failure that will result in
         * retrying the runnable outside a single transaction.
         *
         * @param result runnable result
         * @return failed status
         */
        boolean failed(Object result);

        /**
         * Retry notification callback, returning flag to continue
         * retry run, return result, or throw exception.
         *
         * @param result runnable result
         * @param exception exception triggering retry
         * @return
         */
        RunInTxnRetry retryNotification(Object result, Exception exception);

        /**
         * Retry runnable outside transaction.
         *
         * @return runnable result
         * @throws Exception
         */
        Object retry() throws Exception;
    }

    /**
     * Adapter to describe runnable to execute with a programmatic
     * transaction with retry.
     */
    public static abstract class RunInTxnAdapter implements RunInTxn {
        @Override
        public boolean failed(Object result) {
            return (result == null);
        }

        @Override
        public RunInTxnRetry retryNotification(Object result, Exception exception) {
            return RunInTxnRetry.RETRY;
        }

        @Override
        public Object retry() throws Exception {
            return run();
        }
    }

    /**
     * Execute runnable in programmatic transaction with optional retry. Retry
     * is run after rolling back the initial transaction and is not run
     * within a transaction. The retry execution is intended to support partial
     * results where the runnable can be broken up into discrete operations. For
     * example, retrying individual executions in a batch process.
     *
     * @param run runnable to execute
     * @param sessionFlushMode hibernate session flush mode for transaction
     * @return execution result
     * @throws CollageException if runnable or transaction logic throws
     */
    public static Object executeInTxn(RunInTxn run, FlushMode sessionFlushMode) throws CollageException {
        return executeInTxn(run, sessionFlushMode, false);
    }

    /**
     * Execute runnable in programmatic transaction with optional retry. Retry
     * is run after rolling back the initial transaction and is not run
     * within a transaction. The retry execution is intended to support partial
     * results where the runnable can be broken up into discrete operations. For
     * example, retrying individual executions in a batch process. Read only
     * transactions are set for rollback only, (i.e. no commit).
     *
     * @param run runnable to execute
     * @param sessionFlushMode hibernate session flush mode for transaction
     * @param readOnly run in read only transaction
     * @return execution result
     * @throws CollageException if runnable or transaction logic throws
     */
    public static Object executeInTxn(RunInTxn run, FlushMode sessionFlushMode, boolean readOnly) throws CollageException {
        // try to run in one transaction: disable Hibernate session flush
        // and start transaction for operation.
        Object result = null;
        Session hibernateSession = null;
        boolean hibernateSessionOpened = false;
        FlushMode savedHibernateSessionFlushMode = null;
        PlatformTransactionManager transactionManager = null;
        TransactionStatus transaction = null;
        CollageTimer timer = startMetricsTimer("executeInTxn");
        try {
            // disable Hibernate session flush
            SessionFactory hibernateSessionFactory = (SessionFactory) CollageFactory.getInstance().getAPIObject(CollageFactory.HIBERNATE_SESSION_FACTORY);
            if (SessionFactoryUtils.hasTransactionalSession(hibernateSessionFactory)) {
                hibernateSession = SessionFactoryUtils.getSession(hibernateSessionFactory, false);
            } else {
                hibernateSession = SessionFactoryUtils.getSession(hibernateSessionFactory, true);
                hibernateSessionOpened = true;
            }
            savedHibernateSessionFlushMode = hibernateSession.getFlushMode();
            hibernateSession.setFlushMode(sessionFlushMode);
            // start transaction
            transactionManager = (PlatformTransactionManager)CollageFactory.getInstance().getAPIObject(CollageFactory.TRANSACTION_MANAGER);
            DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED);
            transactionDefinition.setReadOnly(readOnly);
            transaction = transactionManager.getTransaction(transactionDefinition);
            // run transactionally
            result = run.run();
            // check transaction, (rollbackOnly is set on exceptions thrown by methods
            // proxied by declarative transaction interceptors)
            if (transaction.isCompleted() || transaction.isRollbackOnly() || run.failed(result)) {
                throw new RuntimeException("Unexpected transaction state or run failure");
            }
            // complete transaction
            if (!readOnly) {
                transactionManager.commit(transaction);
            } else {
                transactionManager.rollback(transaction);
            }
            // restore Hibernate session
            hibernateSession.setFlushMode(savedHibernateSessionFlushMode);
        } catch (Exception runException) {
            // rollback transaction
            if ((transactionManager != null) && (transaction != null)) {
                if (!transaction.isCompleted()) {
                    try {
                        transactionManager.rollback(transaction);
                    } catch (Exception ignore) {
                    }
                }
            }
            // restore Hibernate session
            if (hibernateSession != null) {
                hibernateSession.setFlushMode(savedHibernateSessionFlushMode);
                if (hibernateSessionOpened) {
                    SessionFactoryUtils.closeSession(hibernateSession);
                }
            }
            // retry notification: rethrow wrapped exception if not retrying
            switch (run.retryNotification(result, runException)) {
                case RETURN: return result;
                case EXCEPTION: throw new CollageException("Exception running in transaction: "+runException, runException);
                case RETRY: break;
            }
            // retry running outside transaction
            try {
                result = run.retry();
            } catch (Exception retryException) {
                throw new CollageException("Exception retrying outside transaction: "+retryException, retryException);
            }
        } finally {
            stopMetricsTimer(timer);
        }
        return result;
    }
}

package org.groundwork.rs.async;

import com.groundwork.collage.CollageFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class RestTransaction {

    protected static Log log = LogFactory.getLog(RestTransaction.class);

    public Session startTransaction() {
        SessionFactory sessionFactory = getSessionFactory();
        Session session = SessionFactoryUtils.getSession(getSessionFactory(), true);
        FlushMode flushMode = FlushMode.MANUAL;
        if (flushMode != null) {
            session.setFlushMode(flushMode);
        }
        TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
        return session;

    }

    public void releaseSession() {
        SessionHolder sessionHolder = null;
        try {
//            System.out.println("--- releasing " + TransactionSynchronizationManager.isSynchronizationActive()
//                + ", " + uri);
            sessionHolder =
                    (SessionHolder) TransactionSynchronizationManager.unbindResource(getSessionFactory());

        }
        catch (Exception e) {
            log.error("failed to close session ", e);

        }
        //logger.debug("Closing Hibernate Session in SharedSessionInViewFilter");
        //closeSession(sessionHolder.getSession(), sessionFactory);
        if (sessionHolder != null)
            SessionFactoryUtils.closeSession(sessionHolder.getSession());
    }

    private SessionFactory getSessionFactory() {
        CollageFactory collage = CollageFactory.getInstance();
        return (SessionFactory)collage.getAPIObject(CollageFactory.HIBERNATE_SESSION_FACTORY);
    }

}

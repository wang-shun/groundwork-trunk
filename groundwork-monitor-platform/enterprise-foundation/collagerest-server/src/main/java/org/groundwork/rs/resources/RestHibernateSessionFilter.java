package org.groundwork.rs.resources;

import com.groundwork.collage.CollageFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;

public class RestHibernateSessionFilter implements Filter {
    protected static Log log = LogFactory.getLog(RestHibernateSessionFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;

        //Get the IP address of client machine.
        String ipAddress = request.getRemoteAddr();

        //Log the IP address and current timestamp.
        if (log.isTraceEnabled()) {
            log.trace("IP " + ipAddress + ", Time " + new Date().toString());
        }         

        String async  = request.getParameter("async");
        if (async != null && async.equalsIgnoreCase("true")) {
            if (log.isInfoEnabled())
                log.info("Processing async rest request, bypassing hibernate session filter");
            chain.doFilter(req, res);
            return;
        }
        Session session = null;
        try {
            //System.out.println("--- starting trans " + request.getRequestURI());
            session = startTransaction();
            //HibernateUtil.getSession();
        }
        catch (Exception e) {
            //System.out.println("--- exception trans " + request.getRequestURI());
            log.error("Exception getting session ", e);
        }
        try {
            chain.doFilter(req, res);
        }
        finally {
            try {
                //HibernateUtil.closeSession();
                if (session != null)
                    releaseSession(session, request.getRequestURI());
            }
            catch (Exception e) {
                log.error("Exception releasing JAX-RS session.", e);
            }
        }
    }

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

    public void releaseSession(Session session, String uri) {
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

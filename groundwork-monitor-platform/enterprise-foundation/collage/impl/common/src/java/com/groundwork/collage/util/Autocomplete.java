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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Autocomplete
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class Autocomplete {

    private static Log log = LogFactory.getLog(Autocomplete.class);

    public static final String ALL_PREFIX = "";
    public static final String WILDCARD_PREFIX = "*";
    public static final int DEFAULT_NAMES_LIMIT = 10;

    public static final long REFRESH_SETTLE_MIN_WAIT = 100L;
    public static final long TERMINATE_MAX_WAIT = 10000L;

    private final AutocompleteNames names;
    private final Thread refreshThread;
    private final AtomicInteger refreshDisabled = new AtomicInteger();

    private volatile List<AutocompleteName> cachedNames = Collections.EMPTY_LIST;
    private volatile boolean refresh;
    private volatile boolean terminate;

    /**
     * Construct transient autocomplete instance.
     *
     * @param names autocomplete names implementation to supply names
     * @param refreshThreadName background refresh thread name
     */
    public Autocomplete(AutocompleteNames names, String refreshThreadName) {
        this(names, null, null, refreshThreadName);
    }

    /**
     * Construct typed transient autocomplete instance.
     *
     * @param names autocomplete names implementation to supply names
     * @param namesEntityType autocomplete names entity type
     * @param refreshThreadName background refresh thread name
     */
    public Autocomplete(AutocompleteNames names, String namesEntityType, String refreshThreadName) {
        this(names, namesEntityType, null, refreshThreadName);
    }

    /**
     * Construct persistent autocomplete instance.
     *
     * @param names autocomplete names implementation to supply names
     * @param sessionFactory hibernate session factory
     * @param refreshThreadName background refresh thread name
     */
    public Autocomplete(final AutocompleteNames names, final SessionFactory sessionFactory, String refreshThreadName) {
        this(names, null, sessionFactory, refreshThreadName);
    }

    /**
     * Construct typed persistent autocomplete instance.
     *
     * @param names autocomplete names implementation to supply names
     * @param namesEntityType autocomplete names entity type
     * @param sessionFactory hibernate session factory
     * @param refreshThreadName background refresh thread name
     */
    public Autocomplete(final AutocompleteNames names, final String namesEntityType, final SessionFactory sessionFactory, String refreshThreadName) {
        this.names = names;
        this.refreshThread = new Thread(new Runnable() {
            /**
             * Names refresh thread runnable.
             */
            @Override
            public void run() {
                // track refresh time
                long lastRefreshTime = 0L;
                do {
                    // wait for refresh request or terminate
                    synchronized (refreshThread) {
                        while (!refresh && !terminate) {
                            try {
                                refreshThread.wait();
                            } catch (InterruptedException ie) {
                            }
                        }
                    }
                    // wait for incoming refresh requests to settle, (this is done
                    // instead of requiring all batch jobs to enable/disable refresh);
                    // waiting for time based off last refresh time limits duty cycle
                    // of refresh thread processing if system slows
                    if (!terminate) {
                        long refreshSettleTime = Math.max(lastRefreshTime * 2, REFRESH_SETTLE_MIN_WAIT);
                        synchronized (refreshThread) {
                            do {
                                // clear refresh request
                                refresh = false;
                                // refresh request settle wait
                                try {
                                    refreshThread.wait(refreshSettleTime);
                                } catch (InterruptedException ie) {
                                }
                            } while (refresh && !terminate);
                        }
                    }
                    // refresh names, (if not terminated)
                    if (!terminate) {
                        // clear refresh request
                        refresh = false;
                        // load refreshed names in Hibernate transaction, (abort if
                        // new refresh requested or terminated)
                        long startRefreshTime = System.currentTimeMillis();
                        Iterator<AutocompleteName> namesIter = null;
                        TreeSet<AutocompleteName> refreshedNamesSet = new TreeSet<AutocompleteName>();
                        try {
                            // open Hibernate transaction
                            openTransaction();
                            // open names iterator
                            namesIter = names.openNamesIterator(namesEntityType);
                            // load names iterator
                            while (namesIter.hasNext() && !refresh && !terminate) {
                                refreshedNamesSet.add(namesIter.next());
                            }
                        } catch (Exception e) {
                            log.error("Autocomplete refresh load error: "+e, e);
                        } finally {
                            try {
                                // close names iterator
                                if (namesIter != null) {
                                    names.closeNamesIterator(namesIter);
                                }
                                // close Hibernate transaction
                                closeTransaction();
                            } catch (Exception e) {
                                log.error("Autocomplete refresh close error: "+e, e);
                            }
                        }
                        // cache refreshed names, (abort if new refresh requested or
                        // terminated)
                        if (!refresh && !terminate) {
                            namesIter = refreshedNamesSet.iterator();
                            ArrayList refreshedNames = new ArrayList(refreshedNamesSet.size());
                            while (namesIter.hasNext() && !refresh && !terminate) {
                                refreshedNames.add(namesIter.next());
                            }
                            if (!refresh && !terminate) {
                                // update cached refresh names and track refresh time
                                cachedNames = refreshedNames;
                                lastRefreshTime = System.currentTimeMillis()-startRefreshTime;
                            }
                        }
                    }
                } while (!terminate);
            }

            /**
             * Open Hibernate transaction for calling refresh thread.
             */
            private void openTransaction() {
                if (sessionFactory != null) {
                    Session session = SessionFactoryUtils.getSession(sessionFactory, true);
                    TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
                }
            }

            /**
             * Close Hibernate transaction for calling refresh thread.
             */
            private void closeTransaction() {
                if (sessionFactory != null) {
                    SessionHolder sessionHolder =
                            (SessionHolder) TransactionSynchronizationManager.unbindResource(sessionFactory);
                    SessionFactoryUtils.closeSession(sessionHolder.getSession());
                }
            }
        }, refreshThreadName);
        // background thread: daemon and min priority
        this.refreshThread.setDaemon(true);
        this.refreshThread.setPriority(Thread.MIN_PRIORITY);
    }

    /**
     * Initialize, starting background refresh thread.
     */
    public void initialize() {
        // enable initial refresh and start refresh thread
        refresh = true;
        refreshThread.start();
    }

    /**
     * Terminate, stop and wait for background refresh thread.
     */
    public void terminate() {
        // notify refresh thread to terminate
        synchronized (refreshThread) {
            terminate = true;
            refreshThread.notifyAll();
        }
        // wait for terminated refresh thread
        try {
            refreshThread.join(TERMINATE_MAX_WAIT);
        } catch (InterruptedException ie) {
        }
    }

    /**
     * Get matching autocomplete names matching prefix. The prefix
     * can be null, blank, or the '*' character to match all names.
     * The default limit of names is returned.
     *
     * @param prefix autocomplete prefix
     * @return ordered names list
     */
    public List<AutocompleteName> autocomplete(String prefix) {
        return autocomplete(prefix, DEFAULT_NAMES_LIMIT);
    }

    /**
     * Get matching autocomplete names matching prefix. The number
     * of names returned is limited, but will be unlimited if a
     * negative limit is specified. Name uniqueness is determined
     * by comparing canonical names if available. In this case, the
     * total number of names returned can exceed the limit since
     * it is limiting the number of unique canonical names. The
     * prefix can be null, blank, or the '*' character to match all
     * names.
     *
     * @param prefix autocomplete prefix
     * @param limit limit of unique names returned, (-1 for unlimited)
     * @return ordered names list
     */
    public List<AutocompleteName> autocomplete(String prefix, int limit) {
        // validate prefix and limit
        prefix = ((prefix != null) ? prefix.toLowerCase().trim() : ALL_PREFIX);
        if (prefix.equals(WILDCARD_PREFIX)) {
            prefix = ALL_PREFIX;
        }
        if (limit == 0) {
            return Collections.EMPTY_LIST;
        }
        // snapshot sorted search names
        List<AutocompleteName> searchNames = cachedNames;
        // binary search across names for prefix
        int searchIndex = Collections.binarySearch(searchNames, new AutocompleteName(prefix));
        searchIndex = ((searchIndex >= 0) ? searchIndex : -searchIndex-1);
        // return limited prefix matching names
        List<AutocompleteName> names = new ArrayList<AutocompleteName>();
        if (searchIndex < searchNames.size()) {
            if ((limit > 0) && (searchNames.get(searchIndex).getCanonicalName() != null)) {
                // return limited search canonical names
                Set<String> uniqueCanonicalNames = new HashSet<String>();
                for (int i = searchIndex; (i < searchNames.size()); i++) {
                    AutocompleteName searchName = searchNames.get(i);
                    if (uniqueCanonicalNames.add(searchName.getCanonicalName()) && (uniqueCanonicalNames.size() > limit)) {
                        break;
                    }
                    if (searchName.getLowerCaseName().startsWith(prefix)) {
                        names.add(searchName);
                    } else {
                        break;
                    }
                }
            } else {
                // return limited search names
                limit = ((limit > 0) ? Math.min(searchIndex+limit, searchNames.size()) : searchNames.size());
                for (int i = searchIndex; (i < limit); i++) {
                    AutocompleteName searchName = searchNames.get(i);
                    if (searchName.getLowerCaseName().startsWith(prefix)) {
                        names.add(searchName);
                    } else {
                        break;
                    }
                }
            }
        }
        return names;
    }

    /**
     * Trigger refresh of autocomplete names. Triggering refresh
     * cancels any running refresh to ensure refresh is complete.
     */
    public void refresh() {
        // notify refresh thread to refresh
        if (refreshDisabled.get() == 0) {
            synchronized (refreshThread) {
                refresh = true;
                refreshThread.notifyAll();
            }
        }
    }

    /**
     * Disable refresh for before batch updates. Invocation of disable
     * must be paired with a later invocation of enable refresh. Refresh
     * processing attempts to let incoming refresh requests settle to
     * suppress refreshes during batch operation, but explicitly disabling
     * and enabling refresh requests around a batch operations is tighter
     * if the batch operation runs slower than will be caught by the
     * refresh request settling.
     */
    public void disableRefresh() {
        refreshDisabled.incrementAndGet();
    }

    /**
     * Enable and trigger refresh after batch updated. Invocation of
     * enable must follow an earlier invocation of disable refresh.
     * Refresh processing attempts to let incoming refresh requests settle
     * to suppress refreshes during batch operation, but explicitly
     * disabling and enabling refresh requests around a batch operations
     * is tighter if the batch operation runs slower than will be caught
     * by the refresh request settling.
     *
     * @throws IllegalStateException if enable/disable nesting error detected
     */
    public void enableRefresh() {
        int disabled = refreshDisabled.decrementAndGet();
        if (disabled == 0) {
            refresh();
        } else if (disabled < 0) {
            refreshDisabled.incrementAndGet();
            throw new IllegalStateException("Autocomplete refresh enabled before disabled.");
        }
    }
}

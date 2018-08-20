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

package com.groundwork.collage.biz;

import com.groundwork.collage.biz.model.Suggestion;
import com.groundwork.collage.biz.model.SuggestionEntityType;
import com.groundwork.collage.biz.model.Suggestions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.category.CategoryService;
import org.groundwork.foundation.bs.metadata.MetadataService;
import org.groundwork.foundation.dao.FoundationDAO;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SuggestionsServiceImpl
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class SuggestionsServiceImpl extends HibernateDaoSupport implements SuggestionsService {

    private static Log log = LogFactory.getLog(SuggestionsServiceImpl.class);

    private static final int SINGLE_THREAD_QUERY_LIMIT = 1;
    private static final int MAX_THREAD_QUERY_LIMIT = 5;
    private static final int MAX_THREAD_QUERY_WAIT = 2000;

    private FoundationDAO foundationDAO;
    private MetadataService metadataService;
    private ExecutorService queryThreadPool;

    /**
     * SuggestionsService constructor.
     *
     * @param foundationDAO foundation DB utility
     * @param metadataService metadata service
     */
    public SuggestionsServiceImpl(FoundationDAO foundationDAO, MetadataService metadataService) {
        this.foundationDAO = foundationDAO;
        this.metadataService = metadataService;
    }

    /**
     * Initialize SuggestionsService.
     */
    public void initialize() {
        // initialize suggestions query thread pool
        queryThreadPool = Executors.newCachedThreadPool(new ThreadFactory() {
            public Thread newThread(Runnable task) {
                Thread thread = new Thread(task, "SuggestionsServiceQueryThread");
                thread.setDaemon(true);
                return thread;
            }
        });
    }

    /**
     * Terminate SuggestionsService.
     */
    public void terminate() {
        // terminate suggestions query thread pool
        queryThreadPool.shutdownNow();
    }

    @Override
    public Suggestions querySuggestions(String name, int limit, SuggestionEntityType entityType) throws Exception {
        Set<SuggestionEntityType> entityTypes = new HashSet<SuggestionEntityType>(Arrays.asList(new SuggestionEntityType[]{entityType}));
        return querySuggestions(name, limit, entityTypes);
    }

    @Override
    public Suggestions querySuggestions(String name, int limit, Set<SuggestionEntityType> entityTypes) throws Exception {
        // convert name regex to SQL like syntax
        if ((name != null) && (name.length() > 0)) {
            name = regexToSQLLike(name).toLowerCase();
        } else {
            name = null;
        }
        // map entity types to suggestion name queries and parameters
        List<SuggestionsQuery> suggestionsQueries = new ArrayList<SuggestionsQuery>();
        for (SuggestionEntityType entityType : entityTypes) {
            int categoryEntityType = -1;
            switch (entityType) {
                case SERVICE_GROUP:
                    categoryEntityType = metadataService.getEntityTypeByName(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP).getEntityTypeId();
                    break;
                case CUSTOM_GROUP:
                    categoryEntityType = metadataService.getEntityTypeByName(CategoryService.ENTITY_TYPE_CODE_CUSTOMGROUP).getEntityTypeId();
                    break;
            }
            if (name != null) {
                if (limit > 0) {
                    switch (entityType) {
                        case HOST:
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select h.hostname, lower(h.hostname) from host h where lower(h.hostname) like ? order by lower(h.hostname) limit ?",
                                    new Object[]{name, limit}, entityType));
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select count(*) from host h where lower(h.hostname) like ?",
                                    new Object[]{name}));
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select hn.hostname, lower(hn.hostname) from hostname hn where lower(hn.hostname) like ? order by lower(hn.hostname) limit ?",
                                    new Object[]{name, limit}, entityType));
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select count(*) from hostname hn where lower(hn.hostname) like ?",
                                    new Object[]{name}));
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select -count(*) from hostidentity hi where lower(hi.hostname) like ?",
                                    new Object[]{name}));
                            break;
                        case SERVICE:
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select distinct s.servicedescription, lower(s.servicedescription) from servicestatus s where lower(s.servicedescription) like ? order by lower(s.servicedescription) limit ?",
                                    new Object[]{name, limit}, entityType));
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select count(*) from (select distinct s.servicedescription from servicestatus s where lower(s.servicedescription) like ?) as temp",
                                    new Object[]{name}));
                            break;
                        case HOSTGROUP:
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select hg.name, lower(hg.name) from hostgroup hg where lower(hg.name) like ? order by lower(hg.name) limit ?",
                                    new Object[]{name, limit}, entityType));
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select count(*) from hostgroup hg where lower(hg.name) like ?",
                                    new Object[]{name}));
                            break;
                        case SERVICE_GROUP:
                        case CUSTOM_GROUP:
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select c.name, lower(c.name) from category c where lower(c.name) like ? and c.entitytypeid = ? order by lower(c.name) limit ?",
                                    new Object[]{name, categoryEntityType, limit}, entityType));
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select count(*) from category c where lower(c.name) like ? and c.entitytypeid = ?",
                                    new Object[]{name, categoryEntityType}));
                            break;
                    }
                } else {
                    switch (entityType) {
                        case HOST:
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select h.hostname, lower(h.hostname) from host h where lower(h.hostname) like ? order by lower(h.hostname)",
                                    new Object[]{name}, entityType));
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select hn.hostname, lower(hn.hostname) from hostname hn where lower(hn.hostname) like ? order by lower(hn.hostname)",
                                    new Object[]{name}, entityType));
                            break;
                        case SERVICE:
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select distinct s.servicedescription, lower(s.servicedescription) from servicestatus s where lower(s.servicedescription) like ? order by lower(s.servicedescription)",
                                    new Object[]{name}, entityType));
                            break;
                        case HOSTGROUP:
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select hg.name, lower(hg.name) from hostgroup hg where lower(hg.name) like ? order by lower(hg.name)",
                                    new Object[]{name}, entityType));
                            break;
                        case SERVICE_GROUP:
                        case CUSTOM_GROUP:
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select c.name, lower(c.name) from category c where lower(c.name) like ? and c.entitytypeid = ? order by lower(c.name)",
                                    new Object[]{name, categoryEntityType}, entityType));
                            break;
                    }
                }
            } else {
                if (limit > 0) {
                    switch (entityType) {
                        case HOST:
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select h.hostname, lower(h.hostname) from host h order by lower(h.hostname) limit ?",
                                    new Object[]{limit}, entityType));
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select count(*) from host h"));
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select hn.hostname, lower(hn.hostname) from hostname hn order by lower(hn.hostname) limit ?",
                                    new Object[]{limit}, entityType));
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select count(*) from hostname hn"));
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select -count(*) from hostidentity hi"));
                            break;
                        case SERVICE:
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select distinct s.servicedescription, lower(s.servicedescription) from servicestatus s order by lower(s.servicedescription) limit ?",
                                    new Object[]{limit}, entityType));
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select count(*) from (select distinct s.servicedescription from servicestatus s) as temp"));
                            break;
                        case HOSTGROUP:
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select hg.name, lower(hg.name) from hostgroup hg order by lower(hg.name) limit ?",
                                    new Object[]{limit}, entityType));
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select count(*) from hostgroup hg"));
                            break;
                        case SERVICE_GROUP:
                        case CUSTOM_GROUP:
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select c.name, lower(c.name) from category c where c.entitytypeid = ? order by lower(c.name) limit ?",
                                    new Object[]{categoryEntityType, limit}, entityType));
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select count(*) from category c where c.entitytypeid = ?",
                                    new Object[]{categoryEntityType}));
                            break;
                    }
                } else {
                    switch (entityType) {
                        case HOST:
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select h.hostname, lower(h.hostname) from host h order by lower(h.hostname)",
                                    entityType));
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select hn.hostname, lower(hn.hostname) from hostname hn order by lower(hn.hostname)",
                                    entityType));
                            break;
                        case SERVICE:
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select distinct s.servicedescription, lower(s.servicedescription) from servicestatus s order by lower(s.servicedescription)",
                                    entityType));
                            break;
                        case HOSTGROUP:
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select hg.name, lower(hg.name) from hostgroup hg order by lower(hg.name)",
                                    entityType));
                            break;
                        case SERVICE_GROUP:
                        case CUSTOM_GROUP:
                            suggestionsQueries.add(new SuggestionsQuery(
                                    "select c.name, lower(c.name) from category c where c.entitytypeid = ? order by lower(c.name)",
                                    new Object[]{categoryEntityType}, entityType));
                            break;
                    }
                }
            }
        }
        // perform suggestions queries
        performSuggestionsQueries(suggestionsQueries);
        // aggregate suggestions query results
        int aggregateCount = 0;
        List<List<Suggestion>> aggregateSuggestionResults = new ArrayList<List<Suggestion>>();
        for (SuggestionsQuery suggestionsQuery : suggestionsQueries) {
            List<Object> results = suggestionsQuery.results;
            if (!results.isEmpty()) {
                if ((suggestionsQuery.entityType != null) && (results.get(0) instanceof Object[])) {
                    List<Suggestion> suggestionResults = new ArrayList<Suggestion>(results.size());
                    for (Object result : results) {
                        suggestionResults.add(new Suggestion((String) ((Object[])result)[0], suggestionsQuery.entityType));
                    }
                    aggregateSuggestionResults.add(suggestionResults);
                } else if ((results.size() == 1) && (results.get(0) instanceof Number)) {
                    aggregateCount += ((Number)results.get(0)).intValue();
                } else {
                    log.error("Unexpected query result: "+results.get(0).getClass().getName());
                    throw new Exception("Unexpected query result: "+results.get(0).getClass().getName());
                }
            }
        }
        List<Suggestion> aggregateSuggestions;
        if (aggregateSuggestionResults.size() > 1) {
            aggregateSuggestions = mergeUnique(aggregateSuggestionResults);
        } else if (aggregateSuggestionResults.size() == 1) {
            aggregateSuggestions = aggregateSuggestionResults.get(0);
        } else {
            aggregateSuggestions = Collections.EMPTY_LIST;
        }
        if (aggregateCount == 0) {
            aggregateCount = aggregateSuggestions.size();
        }
        // limit and return suggestions if multiple queries performed
        if ((limit > 0) && (aggregateSuggestions.size() > limit)) {
            aggregateSuggestions = aggregateSuggestions.subList(0, limit);
        }
        return (((aggregateCount > 0) || !aggregateSuggestions.isEmpty()) ? new Suggestions(aggregateCount, aggregateSuggestions) : null);
    }

    /**
     * Inner class used to capture suggestions query and results.
     */
    private static class SuggestionsQuery {

        public String query;
        public Object[] parameters;
        public SuggestionEntityType entityType;
        public List<Object> results;
        public Exception exception;

        public SuggestionsQuery(String query) {
            this(query, null, null);
        }

        public SuggestionsQuery(String query, Object[] parameters) {
            this(query, parameters, null);
        }

        public SuggestionsQuery(String query, SuggestionEntityType entityType) {
            this(query, null, entityType);
        }

        public SuggestionsQuery(String query, Object[] parameters, SuggestionEntityType entityType) {
            this.query = query;
            this.parameters = parameters;
            this.entityType = entityType;
        }
    }

    /**
     * Perform suggestions queries in current thread or using thread pool.
     *
     * @param suggestionsQueries suggestions queries to perform
     * @throws Exception on query error
     */
    private void performSuggestionsQueries(List<SuggestionsQuery> suggestionsQueries) throws Exception {
        if (suggestionsQueries.size() <= SINGLE_THREAD_QUERY_LIMIT) {
            // perform suggestions queries in current thread
            long startQueriesTime = System.currentTimeMillis();
            for (SuggestionsQuery suggestionsQuery : suggestionsQueries) {
                // abort suggestions queries if timeout expired
                if (System.currentTimeMillis()-startQueriesTime > MAX_THREAD_QUERY_WAIT) {
                    log.warn("Suggestions queries timed out.");
                    throw new Exception("Suggestions queries timed out.");
                }
                // perform suggestions query
                suggestionsQuery.results = foundationDAO.sqlQuery(suggestionsQuery.query, suggestionsQuery.parameters);
            }
        } else {
            // perform suggestions queries using thread pool
            int queryThreadCount = Math.min(suggestionsQueries.size(), MAX_THREAD_QUERY_LIMIT);
            CountDownLatch done = new CountDownLatch(queryThreadCount);
            AtomicBoolean exit = new AtomicBoolean(false);
            for (int i = 0; (i < queryThreadCount); i++) {
                queryThreadPool.execute(new PerformSuggestionsQuery(suggestionsQueries, i, queryThreadCount, done, exit));
            }
            // wait for suggestions queries to finish
            done.await(MAX_THREAD_QUERY_WAIT, TimeUnit.MILLISECONDS);
            // abort suggestions queries and throw exception if not all finished
            if (done.getCount() > 0) {
                exit.set(true);
                log.warn("Suggestions queries timed out.");
                throw new Exception("Suggestions queries timed out.");
            }
            // throw exception on query exception
            for (SuggestionsQuery suggestionsQuery : suggestionsQueries) {
                if (suggestionsQuery.exception != null) {
                    log.error("Suggestions queries exception: " + suggestionsQuery.exception, suggestionsQuery.exception);
                    throw suggestionsQuery.exception;
                }
            }
        }
    }

    /**
     * Runnable to perform suggestions queries in thread pool thread.
     */
    private class PerformSuggestionsQuery implements Runnable {

        private List<SuggestionsQuery> suggestionsQueries;
        private int queryThread;
        private int queryThreadCount;
        private CountDownLatch done;
        private AtomicBoolean exit;

        public PerformSuggestionsQuery(List<SuggestionsQuery> suggestionsQueries, int queryThread, int queryThreadCount,
                                       CountDownLatch done, AtomicBoolean exit) {
            this.suggestionsQueries = suggestionsQueries;
            this.queryThread = queryThread;
            this.queryThreadCount = queryThreadCount;
            this.done = done;
            this.exit = exit;
        }

        @Override
        public void run() {
            Session session = null;
            try {
                // start Hibernate session for thread
                session = SessionFactoryUtils.getSession(getSessionFactory(), true);
                // perform suggestions queries allocated to thread
                for (int i = queryThread, limit = suggestionsQueries.size(); ((i < limit) && !exit.get()); i += queryThreadCount) {
                    SuggestionsQuery suggestionsQuery = suggestionsQueries.get(i);
                    try {
                        suggestionsQuery.results = foundationDAO.sqlQuery(suggestionsQuery.query, suggestionsQuery.parameters);
                    } catch (Exception e) {
                        suggestionsQuery.exception = e;
                    }
                }
            } catch (Exception e) {
                // set exception on suggestions queries not performed
                for (int i = queryThread, limit = suggestionsQueries.size(); ((i < limit) && !exit.get()); i += queryThreadCount) {
                    SuggestionsQuery suggestionsQuery = suggestionsQueries.get(i);
                    if ((suggestionsQuery.results == null) && (suggestionsQuery.exception == null)) {
                        suggestionsQuery.exception = e;
                    }
                }
            } finally {
                // suggestions queries for thread done
                done.countDown();
                // close Hibernate session for thread
                SessionFactoryUtils.closeSession(session);
            }
        }
    }

    /**
     * Convert path regex to SQL like syntax. Map '*' to '%' and
     * '?' to '_' supporting '\' as the escape character.
     *
     * @param regex path regex name expression
     * @return SQL like name expression
     */
    private static String regexToSQLLike(String regex) {
        StringBuilder sqlLike = new StringBuilder();
        boolean escape = false;
        for (char regexChar : regex.toCharArray()) {
            if (escape) {
                if (regexChar == '\\') {
                    sqlLike.append('\\');
                    sqlLike.append('\\');
                } else if (regexChar == '%') {
                    sqlLike.append('\\');
                    sqlLike.append('%');
                } else if (regexChar == '_') {
                    sqlLike.append('\\');
                    sqlLike.append('_');
                } else {
                    sqlLike.append(regexChar);
                }
                escape = false;
            } else if (regexChar == '\\') {
                escape = true;
            } else if (regexChar == '*') {
                sqlLike.append('%');
            } else if (regexChar == '?') {
                sqlLike.append('_');
            } else if (regexChar == '%') {
                sqlLike.append('\\');
                sqlLike.append('%');
            } else if (regexChar == '_') {
                sqlLike.append('\\');
                sqlLike.append('_');
            } else {
                sqlLike.append(regexChar);
            }
        }
        return sqlLike.toString();
    }

    /**
     * Merge a collection of lists into a single returned list with
     * unique elements.
     *
     * @param lists collection of lists to merge
     * @return merged unique lists
     */
    public static <E extends Comparable<? super E>> List<E> mergeUnique(Collection<? extends List<? extends E>> lists) {
        PriorityQueue<ComparableIterator<E>> queue = new PriorityQueue<ComparableIterator<E>>();
        int size = 0;
        for (List<? extends E> list : lists) {
            if (!list.isEmpty()) {
                queue.add(new ComparableIterator<E>(list.iterator()));
                size += list.size();
            }
        }
        List<E> mergedUnique = new ArrayList<E>(size);
        E lastElement = null;
        while (!queue.isEmpty()) {
            ComparableIterator<E> next = queue.remove();
            E element = next.next();
            if ((lastElement == null) || (lastElement.compareTo(element) != 0)) {
                mergedUnique.add(element);
                lastElement = element;
            }
            if (next.hasNext()) {
                queue.add(next);
            }
        }
        return mergedUnique;
    }

    /**
     * Inner comparable iterator class used to implement merge unique.
     */
    private static class ComparableIterator<E extends Comparable<? super E>> implements Iterator<E>, Comparable<ComparableIterator<E>> {
        private E peek;
        private Iterator<? extends E> iterator;

        public ComparableIterator(Iterator<? extends E> iterator) {
            this.iterator = iterator;
            this.peek = (iterator.hasNext() ? iterator.next() : null);
        }

        @Override
        public boolean hasNext() {
            return peek != null;
        }

        @Override
        public E next() {
            E next = peek;
            peek = (iterator.hasNext() ? iterator.next() : null);
            return next;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int compareTo(ComparableIterator<E> other) {
            return ((peek == null) ? 1 : peek.compareTo(other.peek));
        }
    }

    @Override
    public List<String> hostServiceDescriptions(String hostName) {
        // validate host name
        if ((hostName == null) || (hostName.length() == 0)) {
            return Collections.EMPTY_LIST;
        }
        hostName = hostName.toLowerCase();
        // query for unique service descriptions for host name
        String query = "select s.servicedescription " +
                "from host h " +
                "join servicestatus s on (h.hostid = s.hostid) " +
                "where lower(h.hostname) = ? " +
                "union " +
                "select s.servicedescription " +
                "from hostname hn " +
                "join hostidentity hi on (hn.hostidentityid = hi.hostidentityid) " +
                "join servicestatus s on (hi.hostid = s.hostid) " +
                "where lower(hn.hostname) = ?";
        List<Object> results = foundationDAO.sqlQuery(query, new Object[]{hostName, hostName});
        List<String> stringsResult = new ArrayList<String>(results.size());
        for (Object result : results) {
            stringsResult.add((String)result);
        }
        Collections.sort(stringsResult);
        return stringsResult;
    }

    @Override
    public Map<String,String> serviceHostNames(String serviceDescription) {
        // validate service description
        if ((serviceDescription == null) || (serviceDescription.length() == 0)) {
            return Collections.EMPTY_MAP;
        }
        // query for unique host names for service description, (aliases required
        // to prevent result set confusion for columns with the same name)
        String query = "select h.hostname as hostname, h.hostname as canonicalhostname " +
                "from servicestatus s " +
                "join host h on (s.hostid = h.hostid) " +
                "where s.servicedescription = ? " +
                "union " +
                "select hn.hostname as hostname, hi.hostname as canonicalhostname " +
                "from servicestatus s " +
                "join hostidentity hi on (s.hostid = hi.hostid) "+
                "join hostname hn on (hi.hostidentityid = hn.hostidentityid) " +
                "where s.servicedescription = ?";
        List<Object []> results = foundationDAO.sqlQuery(query, new Object[]{serviceDescription, serviceDescription});
        Map<String,String> stringsResults = new TreeMap<String,String>();
        for (Object [] result : results) {
            stringsResults.put((String) result[0], (String) result[1]);
        }
        return stringsResults;
    }
}

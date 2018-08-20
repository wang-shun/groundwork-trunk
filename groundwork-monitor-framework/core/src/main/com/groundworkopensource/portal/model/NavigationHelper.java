/*
 * Copyright 2009 GroundWork Open Source, Inc. ("GroundWork") All rights
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
import java.math.BigInteger;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

/**
 * Navigation Helper is database access class, that is used to retrieve and
 * store Navigation History objects to and from jboss portal database.
 * 
 * @author nitin_jadhav
 * @version GWMON - 6.1.1
 */
public class NavigationHelper {

    /*
     * SQL Query for creating 'USER_NAVIGATION' table in 'jbossportal' database:
     * 
     * CREATE TABLE `USER_NAVIGATION` ( `ID` int(20) NOT NULL auto_increment,
     * `USER_ID` bigint(20) NOT NULL, `NODE_ID` int(20) NOT NULL, `NODE_NAME`
     * varchar(254) NOT NULL, `NODE_TYPE` varchar(15) NOT NULL, `PARENT_INFO`
     * varchar(600) default NULL, PRIMARY KEY (`ID`), INDEX
     * user_navigation_index (USER_ID), FOREIGN KEY (USER_ID) REFERENCES
     * jbp_users(jbp_uid) ON DELETE CASCADE ) ENGINE=InnoDB AUTO_INCREMENT=1
     * DEFAULT CHARSET=latin1;
     */
    /**
     * one constant
     */
    public static final int ONE = 1;

    /**
     * One constant.
     */
    public static final int ZERO = 0;

    /**
     * constant
     */
    public static final int TEN = 10;
    /**
     * constant
     */
    public static final int NINE = 9;
    /**
     * constant
     */
    public static final int EIGHT = 8;
    /**
     * constant
     */
    public static final int SEVEN = 7;
    /**
     * constant
     */
    public static final int SIX = 6;
    /**
     * constant
     */
    public static final int FIVE = 5;
    /**
     * constant
     */
    public static final int FOUR = 4;
    /**
     * constant
     */
    public static final int THREE = 3;
    /**
     * constant
     */
    public static final int TWO = 2;
    /**
     * /** Logger
     */
    private static final Logger LOGGER = Logger
            .getLogger(NavigationHelper.class);

    /**
     * Returns all Navigation History records for provided user Id.
     * 
     * Format of return list: List < Object[] > where each object[] contains:
     * object[0]=record id (Integer, should not be used in processing)<br>
     * object[1]=User Id(Integer)<br>
     * object[0]=Node Id(Integer)<br>
     * object[0]=Node Name(String) object[0]=Node Type(String)<br>
     * object[0]=Node Parent(String)<br>
     * object[0]=Node Sequence(Integer)<br>
     * 
     * @param userId
     * @param app_type
     * @return List
     * @throws IOException
     */

    @SuppressWarnings("unchecked")
    public List<Object[]> getHistoryRecords(int userId, String app_type)
            throws IOException {
        Session session = getSessionFactory().openSession();
        try {
            Query query = session
                    .createSQLQuery(
                            "select * from USER_NAVIGATION n where n.USER_ID = ? and n.APP_TYPE = ?")
                    .addScalar("ID", Hibernate.LONG).addScalar("USER_ID",
                            Hibernate.LONG)
                    .addScalar("NODE_ID", Hibernate.LONG).addScalar(
                            "NODE_NAME", Hibernate.STRING).addScalar(
                            "NODE_TYPE", Hibernate.STRING).addScalar(
                            "PARENT_INFO", Hibernate.STRING).addScalar(
                            "TOOLTIP", Hibernate.STRING).addScalar(
                            "TAB_HISTORY", Hibernate.STRING).addScalar(
                            "NODE_LABEL", Hibernate.STRING);
            query.setParameter(0, userId);
            // check if application type is null or empty String then use
            // default "statusviewer" application type.
            if (null == app_type || app_type.equals("")) {
                app_type = "statusviewer";
            }
            query.setParameter(1, app_type);
            return query.list();
        } catch (HibernateException he) {
            LOGGER.error("Error while retriving records for user Id : "
                    + userId);
            throw new IOException();
        } finally {
            session.flush();
            session.close();
        }
    }

    /**
     * This method is used to add single record to Navigation History database.<br>
     * Note: record Id is not provided. Its auto generated by database.
     * 
     * @param userId
     * @param nodeId
     * @param nodeName
     * @param nodeType
     * @param parentInfo
     * @param toolTip
     * @param app_type
     * @throws IOException
     */
    public void addHistoryRecord(int userId, int nodeId, String nodeName,
            String nodeType, String parentInfo, String toolTip, String app_type)
            throws IOException {
        Session session = getSessionFactory().openSession();

        try {
            Transaction transaction = session.beginTransaction();
            Query query = session
                    .createSQLQuery("insert into USER_NAVIGATION (id,USER_ID,NODE_ID, NODE_NAME, NODE_TYPE, PARENT_INFO, TOOLTIP,APP_TYPE) values (nextval('nav_seq'),?,?,?,?,?,?,?)");
            query.setParameter(ZERO, userId);
            query.setParameter(ONE, nodeId);
            query.setParameter(TWO, nodeName);
            query.setParameter(THREE, nodeType);
            query.setParameter(FOUR, parentInfo);
            query.setParameter(FIVE, toolTip);
            // check if application type is null or empty String then use
            // default "statusviewer" application type.
            if (null == app_type || app_type.equals("")) {
                app_type = "statusviewer";
            }
            query.setParameter(6, app_type);

            // execute update
            query.executeUpdate();
            // commit
            transaction.commit();
        } catch (HibernateException he) {
            LOGGER.error("Error while inserting record for user [" + userId
                    + "]");
            throw new IOException();
        } finally {
            // session.getTransaction().commit();
            session.flush();
            session.close();
        }
    }

    /**
     * This method is used to add single record to Navigation History database.<br>
     * Note: record Id is not provided. Its auto generated by database.
     * 
     * @param userId
     * @param nodeId
     * @param nodeName
     * @param nodeType
     * @param parentInfo
     * @param toolTip
     * @param app_type
     * @param nodeLabel
     * @throws IOException
     */
    public void addHistoryRecord(int userId, int nodeId, String nodeName,
            String nodeType, String parentInfo, String toolTip,
            String app_type, String nodeLabel) throws IOException {
        Session session = getSessionFactory().openSession();

        try {
            Transaction transaction = session.beginTransaction();
            Query query = session
                    .createSQLQuery("insert into USER_NAVIGATION (id,USER_ID,NODE_ID, NODE_NAME, NODE_TYPE, PARENT_INFO, TOOLTIP,APP_TYPE,NODE_LABEL) values (nextval('nav_seq'),?,?,?,?,?,?,?,?)");
            query.setParameter(ZERO, userId);
            query.setParameter(ONE, nodeId);
            query.setParameter(TWO, nodeName);
            query.setParameter(THREE, nodeType);
            query.setParameter(FOUR, parentInfo);
            query.setParameter(FIVE, toolTip);
            // check if application type is null or empty String then use
            // default "statusviewer" application type.
            if (null == app_type || app_type.equals("")) {
                app_type = "statusviewer";
            }
            query.setParameter(SIX, app_type);
            query.setParameter(SEVEN, nodeLabel);
            // execute update
            query.executeUpdate();
            // commit
            transaction.commit();
        } catch (HibernateException he) {
            LOGGER.error("Error while inserting record for user [" + userId
                    + "]");
            throw new IOException();
        } finally {
            // session.getTransaction().commit();
            session.flush();
            session.close();
        }
    }

    /**
     * This method is used to delete a single record of a user identified by
     * "userId" from Navigation History database.
     * 
     * @param userId
     * @param nodeId
     * @param nodeType
     * @param app_type
     * @throws IOException
     */
    public void deleteHistoryRecord(int userId, int nodeId, String nodeType,
            String app_type) throws IOException {
        Session session = getSessionFactory().openSession();

        try {
            Transaction transaction = session.beginTransaction();
            Query query = session
                    .createSQLQuery("delete from USER_NAVIGATION where USER_ID=? and NODE_ID=? and NODE_TYPE=? and APP_TYPE=?");
            query.setParameter(ZERO, userId);
            query.setParameter(ONE, nodeId);
            query.setParameter(TWO, nodeType);
            // check if application type is null or empty String then use
            // default "statusviewer" application type.
            if (null == app_type || app_type.equals("")) {
                app_type = "statusviewer";
            }
            query.setParameter(THREE, app_type);
            query.executeUpdate();

            transaction.commit();
        } catch (HibernateException he) {
            LOGGER.error("Error while deleting record for user" + userId);
            throw new IOException();
        } finally {
            session.flush();
            session.close();
        }
    }

    /**
     * This method is used to delete all records of a user identified by
     * "userId" from Navigation History database.
     * 
     * @param userId
     * @param app_type
     * @throws IOException
     */
    public void deleteAllHistoryRecords(int userId, String app_type)
            throws IOException {
        Session session = getSessionFactory().openSession();
        session.beginTransaction();

        try {
            Query query = session
                    .createSQLQuery("delete from USER_NAVIGATION where USER_ID=? and APP_TYPE=?");
            query.setInteger(ZERO, userId);
            // check if application type is null or empty String then use
            // default "statusviewer" application type.
            if (null == app_type || app_type.equals("")) {
                app_type = "statusviewer";
            }
            query.setParameter(ONE, app_type);
            query.executeUpdate();
        } catch (HibernateException he) {
            LOGGER.error("Error while deleting all records for user Id: "
                    + userId);
            throw new IOException();
        } finally {
            session.getTransaction().commit();
            session.flush();
            session.close();
        }
    }

    /**
     * This method returns User Id integer which corresponds to given user name.
     * 
     * @param userName
     * @return int user Id
     * @throws IOException
     */
    public int getUserIdFromName(String userName) throws IOException {
        Session session = getSessionFactory().openSession();
        try {
            Query query = session
                    .createSQLQuery("select u.jbp_uid from jbp_users u where u.jbp_uname = ?");
            query.setParameter(ZERO, userName);

            Object result = query.uniqueResult();
            if (result != null) {
                BigInteger bigId = (BigInteger) result;
                return bigId.intValue();
            } else {
                throw new IOException();
            }
        } catch (HibernateException he) {
            LOGGER.error("Error while retriving user Id for user: " + userName);
            throw new IOException();
        } finally {
            session.flush();
            session.close();
        }

    }

    /**
     * Get Session factory instance
     * 
     * @return SessionFactory
     * @throws IOException
     */
    public SessionFactory getSessionFactory() throws IOException {
        SessionFactory sessionFactory = NavigationHelperUtil
                .getSessionFactory();
        if (sessionFactory == null) {
            LOGGER.debug("Rebuild Navigation Session Factory");
            NavigationHelperUtil.rebuildSessionFactory();
            sessionFactory = NavigationHelperUtil.getSessionFactory();
        }
        return sessionFactory;
    }

    /**
     * 
     * @param userId
     * @param nodeId
     * @param nodeName
     * @param nodeType
     * @param app_type
     * @return boolean
     * @throws IOException
     */
    public boolean updateHistoryRecord(int userId, int nodeId, String nodeName,
            String nodeType, String app_type) throws IOException {
        Session session = getSessionFactory().openSession();
        session.beginTransaction();

        try {
            Query query = session
                    .createSQLQuery("update USER_NAVIGATION set NODE_NAME=?,NODE_TYPE=? where USER_ID=? and NODE_ID=? and APP_TYPE=?");
            query.setParameter(0, nodeName);
            query.setParameter(1, nodeType);
            query.setParameter(2, userId);
            query.setParameter(3, nodeId);
            // check if application type is null or empty String then use
            // default "statusviewer" application type.
            if (null == app_type || app_type.equals("")) {
                app_type = "statusviewer";
            }
            query.setParameter(4, app_type);

            query.executeUpdate();
            return true;

        } catch (HibernateException he) {
            LOGGER.error("Error while retriving user Id for user: " + userId);
        } finally {
            session.getTransaction().commit();
            session.close();
        }

        return false;

    }
    /**
     * Update Node_Name and Node_type to Navigation History database.
     * 
     * @param userId
     * @param nodeId
     * @param nodeName
     * @param nodeType
     * @param app_type
     * @param tabHistory
     * @param nodeLabel
     * @return boolean
     * @throws IOException
     */
    public boolean updateHistoryRecord(int userId, int nodeId, String nodeName,
            String nodeType, String app_type, String tabHistory,
            String nodeLabel) throws IOException {
        Session session = getSessionFactory().openSession();
        session.beginTransaction();

        try {
            Query query = session
                    .createSQLQuery("update USER_NAVIGATION set NODE_NAME=?,NODE_TYPE=?,TAB_HISTORY=?,NODE_LABEL=? where USER_ID=? and NODE_ID=? and APP_TYPE=?");
            query.setParameter(0, nodeName);
            query.setParameter(1, nodeType);
            query.setParameter(2, tabHistory);
            query.setParameter(3, nodeLabel);
            query.setParameter(4, userId);
            query.setParameter(5, nodeId);
            // check if application type is null or empty String then use
            // default "statusviewer" application type.
            if (null == app_type || app_type.equals("")) {
                app_type = "statusviewer";
            }
            query.setParameter(6, app_type);

            query.executeUpdate();
            return true;

        } catch (HibernateException he) {
            LOGGER.error("Error while retriving user Id for user: " + userId);
        } finally {
            session.getTransaction().commit();
            session.close();
        }

        return false;

    }

    /**
     * Update tab history column
     * 
     * @param userId
     * @param nodeId
     * @param app_type
     * @param tabHistory
     * @return boolean
     * @throws IOException
     */
    public boolean updateTabHistoryRecord(int userId, int nodeId,
            String app_type, String tabHistory) throws IOException {
        Session session = getSessionFactory().openSession();
        session.beginTransaction();

        try {
            Query query = session
                    .createSQLQuery("update USER_NAVIGATION set TAB_HISTORY=? where USER_ID=? and NODE_ID=? and APP_TYPE=?");
            query.setParameter(0, tabHistory);
            query.setParameter(1, userId);
            query.setParameter(2, nodeId);
            // check if application type is null or empty String then use
            // default "statusviewer" application type.
            if (null == app_type || app_type.equals("")) {
                app_type = "statusviewer";
            }
            query.setParameter(3, app_type);

            query.executeUpdate();
            return true;

        } catch (HibernateException he) {
            LOGGER.error("Error while retriving user Id for user: " + userId);
        } finally {
            session.getTransaction().commit();
            session.close();
        }

        return false;

    }

    /**
     * Update Node label column
     * 
     * @param userId
     * @param nodeId
     * @param app_type
     * @param nodeLabel
     * @return boolean
     * @throws IOException
     */
    public boolean updateNodeLabelRecord(int userId, int nodeId,
            String app_type, String nodeLabel) throws IOException {
        Session session = getSessionFactory().openSession();
        session.beginTransaction();

        try {
            Query query = session
                    .createSQLQuery("update USER_NAVIGATION set NODE_LABEL=? where USER_ID=? and NODE_ID=? and APP_TYPE=?");
            query.setParameter(0, nodeLabel);
            query.setParameter(1, userId);
            query.setParameter(2, nodeId);
            // check if application type is null or empty String then use
            // default "statusviewer" application type.
            if (null == app_type || app_type.equals("")) {
                app_type = "statusviewer";
            }
            query.setParameter(3, app_type);

            query.executeUpdate();
            return true;

        } catch (HibernateException he) {
            LOGGER.error("Error while retriving user Id for user: " + userId);
        } finally {
            session.getTransaction().commit();
            session.close();
        }

        return false;

    }

    /**
     * get max node id
     * 
     * @param userId
     * @param app_type
     * @return max node id
     * @throws IOException
     */
    public int getMaxNodeID(int userId, String app_type) throws IOException {
        Session session = getSessionFactory().openSession();
        try {
            Query query = session
                    .createSQLQuery("select max(NODE_ID) from USER_NAVIGATION where USER_ID=? and APP_TYPE=?");
            query.setParameter(0, userId);
            // check if application type is null or empty String then use
            // default "statusviewer" application type.
            if (null == app_type || app_type.equals("")) {
                app_type = "statusviewer";
            }
            query.setParameter(1, app_type);
            List list = query.list();
            if (list != null && list.size() > 0) {
                Integer integer = (Integer) list.get(0);
                if (integer != null) {
                    return integer.intValue();
                } else {
                    return 0;
                }
            }

        } catch (HibernateException he) {
            LOGGER.error("Error while retriving records for user Id : "
                    + userId);
            throw new IOException();
        } finally {
            session.flush();
            session.close();
        }
        return 0;
    }

    /**
     * This method is used to delete a single record of a user identified by
     * "userId" from Navigation History database.
     * 
     * @param userId
     * @param nodeId
     * @param app_type
     * @throws IOException
     */
    public void deleteHistoryRecord(int userId, int nodeId, String app_type)
            throws IOException {
        Session session = getSessionFactory().openSession();

        try {
            Transaction transaction = session.beginTransaction();
            Query query = session
                    .createSQLQuery("delete from USER_NAVIGATION where USER_ID=? and NODE_ID=?  and APP_TYPE=?");
            query.setParameter(ZERO, userId);
            query.setParameter(ONE, nodeId);

            // check if application type is null or empty String then use
            // default "statusviewer" application type.
            if (null == app_type || app_type.equals("")) {
                app_type = "statusviewer";
            }
            query.setParameter(TWO, app_type);
            query.executeUpdate();

            transaction.commit();
        } catch (HibernateException he) {
            LOGGER.error("Error while deleting record for user" + userId);
            throw new IOException();
        } finally {
            session.flush();
            session.close();
        }
    }
}

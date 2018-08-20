package com.groundworkopensource.webapp.license.manager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.Session;

import com.groundworkopensource.webapp.license.hibernate.OrderInfo;
import com.groundworkopensource.webapp.license.utils.HibernateUtil;

import org.apache.log4j.Logger;

public class OrderManager {
	/* Enable log4j */
	private static final Logger LOGGER = Logger
	.getLogger(OrderManager.class.getName());

    /**
     * Create order
     * 
     * @param orderInfo
     */
    public static void createOrder(OrderInfo orderInfo) {
    	//Session session = HibernateUtil.getSessionFactory().getCurrentSession();
    	Session session = null;
    	try {
	        session = HibernateUtil.getSessionFactory().openSession();
	        session.beginTransaction();
	        session.save(orderInfo.getCustomer());
	        session.save(orderInfo);
	        session.getTransaction().commit();
        } catch (Exception e) {
        	LOGGER.equals("Exception persisting order. Error:" +e);
    	}
    	finally {
    		if (session != null)
    			session.close();
    	}
    }

    /**
     * Find order
     * 
     * @param orderID
     * @return OrderInfo
     */
    public static OrderInfo findOrder(String orderID) {
    	Session session = HibernateUtil.getSessionFactory().openSession();
        //session.beginTransaction();
        OrderInfo order = null;
        try {
            order = (OrderInfo) session.load(OrderInfo.class, orderID);
        } catch (ObjectNotFoundException one) {
            //LOGGER.error("Exception in findOrder. Error " + one);
        } finally {
           // session.getTransaction().commit();
            session.close();
        } // end try/catch
        return order;
    }

    /**
     * returns list of order id
     * 
     * @return List
     */
    public static List getAllOrderID() {
        ArrayList<String> orderList = new ArrayList<String>();
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        String allOrderID = "select orderInfo.orderInfoId from OrderInfo orderInfo";
        Query allOrderIDQuery = session.createQuery(allOrderID);
        for (Iterator it = allOrderIDQuery.iterate(); it.hasNext();) {
            Object nextOderID = it.next();
            orderList.add(nextOderID.toString());
        }
        /* Close session */
        if (session != null )
        	session.close();
        
        return orderList;
    }

    /**
     * Update Order
     * 
     * @param orderInfo
     */
    public static void updateOrder(OrderInfo orderInfo) {
    	Session session = null;
    	try {
	        session = HibernateUtil.getSessionFactory().openSession();
	        session.beginTransaction();
	        session.update(orderInfo.getCustomer());
	        session.update(orderInfo);
	        session.getTransaction().commit();
    	} catch (Exception e) {
    		LOGGER.error("Exception in updateOrder. Error " + e);
    		
    	} finally {
    		session.close();
    	}
    }

}

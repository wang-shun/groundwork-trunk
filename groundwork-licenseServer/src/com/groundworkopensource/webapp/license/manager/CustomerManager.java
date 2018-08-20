package com.groundworkopensource.webapp.license.manager;

import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;

import com.groundworkopensource.webapp.license.hibernate.Customer;
import com.groundworkopensource.webapp.license.utils.HibernateUtil;

public class CustomerManager {

	public static Integer createCustomer(Session session, Customer customer) {
		session.save(customer);
		return customer.getCustomerId();
		
	}

	public static Customer findCustomer(int customerID) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		Customer customer = null;
		try {
			customer = (Customer) session.load(Customer.class, new Integer(
					customerID));
		} catch (ObjectNotFoundException one) {
			//one.printStackTrace();
		} finally {
			session.getTransaction().commit();
			session.close();
		}
		return customer;
	}
}

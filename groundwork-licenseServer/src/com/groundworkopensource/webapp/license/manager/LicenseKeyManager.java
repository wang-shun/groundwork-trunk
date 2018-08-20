package com.groundworkopensource.webapp.license.manager;

import org.hibernate.Session;
import org.apache.log4j.Logger;
import com.groundworkopensource.webapp.license.hibernate.LicenseKey;
import com.groundworkopensource.webapp.license.utils.HibernateUtil;

public class LicenseKeyManager {
	
	private static final Logger LOGGER = Logger
	.getLogger(LicenseKeyManager.class.getName());

	public static void create(LicenseKey licenseKey) {
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			session.save(licenseKey);
			session.getTransaction().commit(); 
		} catch (Exception e) {
			LOGGER.error("Exception in Create License Key. Error : " + e);
			
		} finally {
			if (session != null)
				session.close();
		}
	}

	

}

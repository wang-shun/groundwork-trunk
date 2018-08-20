/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork") All rights
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
package com.groundworkopensource.portal.identity.extendedui;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.database.HibernateService;
import org.apache.log4j.Logger;
import org.picocontainer.Startable;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;

/**
 * This SQL Loader service will load custom groundwork seed data to the
 * jboss-idm database.
 * 
 * @author Arul Shanmugam
 */
public class JBossIDMSQLLoaderServiceImpl implements Startable {

	/** logger. */
	private static final Logger LOGGER = Logger
			.getLogger(JBossIDMSQLLoaderServiceImpl.class.getName());

	private static final String SEED_DATA_FILE = "gw-jboss-idm-seed-data.sql";

	/**
	 * License service
	 */
	public JBossIDMSQLLoaderServiceImpl() {

	}

	public void start() {
		LOGGER.info("Starting SQL Loader Service...");
		this.loadSQL();
	}

	/**
	 * Loads the SQL from the file via hibernate. No transaction needed. If
	 * fails, just log the error and continue.
	 */
	private void loadSQL() {
		LOGGER.info("Loading seed data for custom groundwork tables...");
		Session session = null;
		BufferedReader br = null;
		try {
			session = getSessionFactory().openSession();
			br = new BufferedReader(new InputStreamReader(
					JBossIDMSQLLoaderServiceImpl.class.getClassLoader()
							.getResourceAsStream(SEED_DATA_FILE)));
			String sql;
			while ((sql = br.readLine()) != null) {
				if (!sql.startsWith("#")) { //Ignore the comments
					LOGGER.info(sql);
					try {
						Query query = session.createSQLQuery(sql);
						int result = query.executeUpdate();
					}
					catch (ConstraintViolationException cve) {
						LOGGER.debug("Ignoring the duplicating data loading..");
					}
				} // end if
			} // end while

		} catch (Exception e) {
			String message = "Error loading seed data for custom groundwork tables";
			LOGGER.error(message, e);
		} finally {
			if (session != null) {
				session.flush();
				session.close();
			} // end if
			if (br != null) {
				try {
					br.close();
				} catch (Exception e) {
					String message = "Error closing buffered reader";
					LOGGER.error(message, e);
				}
			} // end if

		}
	}

	/**
	 * Get Session factory instance
	 * 
	 * @return SessionFactory
	 * @throws IOException
	 */
	public SessionFactory getSessionFactory() {
		PortalContainer manager = PortalContainer.getInstance();
		HibernateService service_ = (HibernateService) manager
				.getComponentInstanceOfType(HibernateService.class);
		SessionFactory sessionFactory = service_.getSessionFactory();
		return sessionFactory;
	}

	public void stop() {

	}
}

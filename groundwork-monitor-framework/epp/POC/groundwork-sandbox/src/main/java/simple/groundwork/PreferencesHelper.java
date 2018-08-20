///*
// * Copyright 2009 GroundWork Open Source, Inc. ("GroundWork") All rights
// * reserved. This program is free software; you can redistribute it and/or
// * modify it under the terms of the GNU General Public License version 2 as
// * published by the Free Software Foundation.
// * 
// * This program is distributed in the hope that it will be useful, but WITHOUT
// * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
// * details.
// * 
// * You should have received a copy of the GNU General Public License along with
// * this program; if not, write to the Free Software Foundation, Inc., 51
// * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
// */
//package simple.groundwork;
//
//import org.gatein.pc.api.PortletInvokerException;
//import org.gatein.pc.api.state.AccessMode;
//import org.gatein.pc.api.state.PropertyContext;
//import org.gatein.pc.api.state.PropertyMap;
//import org.gatein.pc.portlet.state.AbstractPropertyContext;
//import org.gatein.pc.portlet.state.SimplePropertyMap;
//import org.hibernate.Session;
//import org.hibernate.SessionFactory;
//import org.hibernate.Query;
//import javax.naming.InitialContext;
//import java.util.List;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Iterator;
//import org.apache.log4j.Logger;
//
///**
// * Preferences Helper to get the admin preferences
// * 
// * @author Arul Shanmugam
// * @version $Revision$
// * @since GWMON 6.0.1
// */
//public class PreferencesHelper {
//	private static final Logger log = Logger.getLogger(PreferencesHelper.class);
//
//	/**
//	 * Finds the adminPreferences for the given window instance.
//	 */
//	public static PropertyContext findAdminPreferencesByWindowInstanceId(
//			String instanceId, String defaultDashboardAdminUser) throws PortletInvokerException {
//		HashMap<String, List<String>> adminPref = new HashMap<String, List<String>>();
//		PropertyMap propMap = null;
//		PropertyContext prefs = new AbstractPropertyContext(
//				AccessMode.READ_WRITE, null, false);
//		if (instanceId != null) {
//			Session session = null;
//			try {
//				// Get all of the relevant JBoss session factory
//				SessionFactory sessionFactory = (SessionFactory) new InitialContext()
//						.lookup("java:/portal/PortletSessionFactory");
//				session = sessionFactory.openSession();
//				String sqlQuery = "select e.NAME,v.jbp_value from JBP_INSTANCE i," +
//						"JBP_INSTANCE_PER_USER u," +
//						"JBP_PORTLET_STATE_ENTRY e," +
//						"JBP_PORTLET_STATE_ENTRY_VALUE v " +
//						"where i.ID=? " +
//						"AND i.PK=u.INSTANCE_PK " +
//						"AND u.USER_id=? " +
//						"AND e.PK=v.PK AND " +
//						"e.ENTRY_KEY=SUBSTRING_INDEX(u.PORTLET_REF,'_',-1)";
//				
//				Query query = session.createSQLQuery(sqlQuery);
//				query.setParameter(0, instanceId);
//				query.setParameter(1, defaultDashboardAdminUser);
//				List list = query.list();
//				Iterator itPref = list.iterator();
//				while (itPref.hasNext()) {
//					Object[] vals = (Object[]) itPref.next();
//					String name = (String) vals[0];
//					String value = (String) vals[1];
//					log.debug("***************Name=" + name + ",value=" + value);
//					List<String> valueList = new ArrayList<String>();
//					valueList.add(value);
//					adminPref.put(name, valueList);
//				}
//				if (adminPref.size() > 0) {
//					propMap = new SimplePropertyMap(adminPref);
//					prefs = new AbstractPropertyContext(AccessMode.READ_WRITE,
//							propMap, false);
//				} // endif
//			} catch (Exception e) {
//				throw new PortletInvokerException(e.getMessage());
//			} finally {
//				try {
//					if (session != null) {
//						session.flush();
//						session.close();
//					}
//				} catch (Exception e) {
//					throw new PortletInvokerException(e.getMessage());
//				}
//			}// end try/catch block
//		} // endif
//		return prefs;
//	}
//}

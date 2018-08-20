/*
 * Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork) All rights
 * reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.webapp.console;

/*
 * 
 * 
 * All rights reserved. This program is free software; you can redistribute it
 * and/or modify it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.
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
import com.groundworkopensource.portal.model.ExtendedUIRole;
import com.groundworkopensource.portal.statusviewer.handler.ReferenceTreeMetaModel;
import org.apache.commons.digester.Digester;
import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.FilterOperator;
import org.xml.sax.SAXException;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.portlet.PortletContext;
import javax.security.jacc.PolicyContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Class ConsoleHelper.
 * 
 */
public class ConsoleHelper {

	// /**
	// * Logger.
	// */
	// private static final Logger LOGGER = Logger.getRootLogger();

	/**
	 * logger.
	 */
	private static Logger logger = Logger.getLogger(ConsoleHelper.class
			.getName());

	/** are console links to status viewer enabled?. */
	private static Boolean linksEnabled;

	// /**
	// * is the access secure and using HTTPS?
	// */
	//
	// private static boolean secureAccess;

	/**
	 * Base URL consisting host,port etc for cross linking
	 */
	private static String baseURL;
	
	public static final String EXTENDED_ROLE_ATT_CONSOLE = "com.gwos.portal.ext_role_atts.CONSOLE";

	static {

		// build base URL

		baseURL = "/portal-statusviewer/urlmap?";

		// Check if the links to statusviewer are enabled in properties file
		linksEnabled = Boolean.parseBoolean(PropertyUtils
				.getProperty(ConsoleConstants.SV_LINKS_ENABLED));
	}

	/**
	 * Helper method to get the DataTableBean
	 * 
	 * @return
	 */
	public static DataTableBean getEventTableBean() {

		TabsetBean tabset = ConsoleHelper.getTabSetBean();
		Tab tab = tabset.getTabs().get(tabset.getTabIndex());
		DataTableBean eventTableBean = tab.getDataTableBean();
		return eventTableBean;
	}

	/**
	 * Helper method to get the Console manager
	 * 
	 * @return
	 */
	public static ConsoleManager getConsoleManager() {
		ConsoleManager mgr = null;
		Object consoleMgrObject = ManagedBeanFactory
				.getManagedBean(ConsoleConstants.MANAGED_BEAN_CONSOLE_MGR);
		if (consoleMgrObject instanceof ConsoleManager) {
			mgr = (ConsoleManager) consoleMgrObject;
		}
		return mgr;

	}

	/**
	 * Helper method to get the System FilterBean
	 * 
	 * @return
	 */
	public static SystemFilterTreeBean getSystemFilterBean() {
		SystemFilterTreeBean systemTree = null;
		Object systemFilterObject = ManagedBeanFactory
				.getManagedBean(ConsoleConstants.MANAGED_BEAN_FILTER_TREE);
		if (systemFilterObject instanceof SystemFilterTreeBean) {
			systemTree = (SystemFilterTreeBean) systemFilterObject;
		}
		return systemTree;

	}

	/**
	 * Helper method to get the System FilterBean
	 * 
	 * @return
	 */
	public static PublicFilterTreeBean getPublicFilterBean() {
		PublicFilterTreeBean publicFilterTree = null;
		Object publicFilterObject = ManagedBeanFactory
				.getManagedBean(ConsoleConstants.MANAGED_BEAN_PUBLIC_FILTER_TREE);
		if (publicFilterObject instanceof PublicFilterTreeBean) {
			publicFilterTree = (PublicFilterTreeBean) publicFilterObject;
		}
		return publicFilterTree;

	}

	/**
	 * Gets the filter bean in session
	 * 
	 * @return
	 */
	public static FilterBean getFilterBean() {
		FilterBean filterInSession = null;
		Object filterObject = ManagedBeanFactory
				.getManagedBean(ConsoleConstants.MANAGED_BEAN_FILTER);
		if (filterObject instanceof FilterBean) {
			filterInSession = (FilterBean) filterObject;
		}
		return filterInSession;
	}

	/**
	 * Helper method to get the Tabset
	 * 
	 * @return
	 */
	public static TabsetBean getTabSetBean() {
		TabsetBean tabset = null;
		Object tabsetObject = ManagedBeanFactory
				.getManagedBean(ConsoleConstants.MANAGED_BEAN_TABSET);
		if (tabsetObject instanceof TabsetBean) {
			tabset = (TabsetBean) tabsetObject;
		}
		return tabset;

	}

	/**
	 * Helper method to get the Search
	 * 
	 * @return
	 */
	public static ConsoleBean getConsoleBean() {
		ConsoleBean consoleBean = null;
		Object consoleObject = ManagedBeanFactory
				.getManagedBean(ConsoleConstants.MANAGED_BEAN_CONSOLE);
		if (consoleObject instanceof ConsoleBean) {
			consoleBean = (ConsoleBean) consoleObject;
		}
		return consoleBean;

	}

	/**
	 * Helper method to get the Search
	 * 
	 * @return
	 */
	public static ReferenceTreeMetaModel getRTMM() {
		ReferenceTreeMetaModel rtmm = null;
		Object rtmmObject = ManagedBeanFactory
				.getManagedBean(ConsoleConstants.MANAGED_BEAN_RTMM);
		if (rtmmObject != null && rtmmObject instanceof ReferenceTreeMetaModel) {
			rtmm = (ReferenceTreeMetaModel) rtmmObject;
		}
		return rtmm;

	}
	
	/**
	 * Helper method to get the Search
	 * 
	 * @return
	 */
	/*public static EventsPieHandler getEventPieHandler() {
		EventsPieHandler eventPieHandler = null;
		Object eventPieObject = ManagedBeanFactory
				.getManagedBean(ConsoleConstants.MANAGED_BEAN_EVENTPIE_HANDLER);
		if (eventPieObject != null && eventPieObject instanceof EventsPieHandler) {
			eventPieHandler = (EventsPieHandler) eventPieObject;
		}
		return eventPieHandler;

	}*/

	/**
	 * Helper method to get the Popup
	 * 
	 * @return
	 */
	public static PopupBean getPopupBean() {
		PopupBean popup = null;
		Object popupObject = ManagedBeanFactory
				.getManagedBean(ConsoleConstants.MANAGED_BEAN__POPUP);
		if (popupObject instanceof PopupBean) {
			popup = (PopupBean) popupObject;
		}
		return popup;

	}

	/**
	 * Helper method to get the Search
	 * 
	 * @return
	 */
	public static Filter createSearchFilter(SearchBean searchBean,
			FilterBean filterInSession) {
		Filter searchFilter = null;
		Filter hostFilter = null;
		if (searchBean.getHost() != null
				&& !searchBean.getHost().trim().equals("")) {
			// host filters by device display name, device host name, or device IP address ("identification")
			Filter deviceDisplayNameFilter = new Filter(ConsoleConstants.FILTER_DEVICE,
					FilterOperator.LIKE, searchBean.getHost());
			Filter deviceHostNameFilter = new Filter(ConsoleConstants.FILTER_DEVICE_HOST,
					FilterOperator.LIKE, searchBean.getHost());
			Filter deviceAddressFilter = new Filter(ConsoleConstants.FILTER_ADDRESS,
					FilterOperator.LIKE, searchBean.getHost());
			hostFilter = Filter.OR(Filter.OR(deviceDisplayNameFilter, deviceHostNameFilter), deviceAddressFilter);
		}
		Filter messageFilter = null;
		if (searchBean.getMessage() != null
				&& !searchBean.getMessage().equals("")) {
			messageFilter = new Filter(ConsoleConstants.FILTER_TEXT_MESSAGE,
					FilterOperator.LIKE, searchBean.getMessage());
		}
		Filter dateRangeFromFilter = null;
		Filter dateRangeToFilter = null;
		if (searchBean != null && searchBean.getAgeType() != null
				&& searchBean.getAgeType().equals("preset")) {
			Calendar calFrom = null;
			if (searchBean.getPresetValue() != null) {
				if (!searchBean.getPresetValue().equalsIgnoreCase(
						SearchBean.PRESET_NONE)) {

					String presetValue = searchBean.getPresetValue();

					if (presetValue.equalsIgnoreCase(SearchBean.PRESET_LASTHR)) {
						calFrom = Calendar.getInstance();
						calFrom.add(Calendar.HOUR_OF_DAY, -1);

					} else if (presetValue
							.equalsIgnoreCase(SearchBean.PRESET_LAST6HR)) {
						calFrom = Calendar.getInstance();
						calFrom.add(Calendar.HOUR_OF_DAY, -6);

					} else if (presetValue
							.equalsIgnoreCase(SearchBean.PRESET_LAST12HR)) {
						calFrom = Calendar.getInstance();
						calFrom.add(Calendar.HOUR_OF_DAY, -12);

					} else if (presetValue
							.equalsIgnoreCase(SearchBean.PRESET_LAST24HR)) {
						calFrom = Calendar.getInstance();
						calFrom.add(Calendar.HOUR_OF_DAY, -24);
					} else if (presetValue
							.equalsIgnoreCase(SearchBean.PRESET_LAST30MINS)) {
						calFrom = Calendar.getInstance();
						calFrom.add(Calendar.MINUTE, -30);
					} else if (presetValue
							.equalsIgnoreCase(SearchBean.PRESET_LAST10MINS)) {
						calFrom = Calendar.getInstance();
						calFrom.add(Calendar.MINUTE, -10);
					} // end if
					dateRangeFromFilter = new Filter(
							ConsoleConstants.FILTER_REPORT_DATE,
							FilterOperator.GE, calFrom.getTime());
					dateRangeToFilter = new Filter(
							ConsoleConstants.FILTER_REPORT_DATE,
							FilterOperator.LE, Calendar.getInstance().getTime());
				}

			}
		} else {
			if (searchBean.getAgeValueFrom() != null
					&& searchBean.getAgeValueTo() != null) {
				dateRangeFromFilter = new Filter(
						ConsoleConstants.FILTER_REPORT_DATE, FilterOperator.GE,
						searchBean.getAgeValueFrom());
				// to
				// datetime
				Date toDate = searchBean.getAgeValueTo();
				Calendar calToDate = Calendar.getInstance();
				calToDate.setTime(toDate);
				calToDate.add(Calendar.HOUR, 23);
				calToDate.add(Calendar.MINUTE, 59);
				calToDate.add(Calendar.SECOND, 59);

				dateRangeToFilter = new Filter(
						ConsoleConstants.FILTER_REPORT_DATE, FilterOperator.LE,
						calToDate.getTime()); // TODO:Convert

			}
		} // end if

		Map<String, Filter> searchCriteria = new HashMap<String, Filter>();
		searchCriteria.put(ConsoleConstants.HOST, hostFilter);
		searchCriteria.put("message", messageFilter);
		searchCriteria.put("daterangefrom", dateRangeFromFilter);
		searchCriteria.put("daterangeto", dateRangeToFilter);

		if (searchBean.getSeverity() != null
				&& !CommonUtils.isEmpty(searchBean.getSeverity())) {
			Filter sevFilter = new Filter(ConsoleConstants.FILTER_SEVERITY,
					FilterOperator.LIKE, searchBean.getSeverity());
			searchCriteria.put("severity", sevFilter);
		} // end if

		if (searchBean.getOpStatus() != null
				&& !CommonUtils.isEmpty(searchBean.getOpStatus())) {
			Filter opStatusFilter = new Filter(
					ConsoleConstants.FILTER_OPSTATUS, FilterOperator.LIKE,
					searchBean.getOpStatus());
			searchCriteria.put("operationStatus", opStatusFilter);
		} // end if

		if (searchBean.getMonStatus() != null
				&& !CommonUtils.isEmpty(searchBean.getMonStatus())) {
			Filter monStatusFilter = new Filter(
					ConsoleConstants.FILTER_MONSTAUTS, FilterOperator.LIKE,
					searchBean.getMonStatus());
			searchCriteria.put("monitorStatus", monStatusFilter);
		} // end if

		Set<String> keySet = searchCriteria.keySet();
		Iterator<String> iter = keySet.iterator();
		Calendar temp = Calendar.getInstance();
		temp.add(Calendar.YEAR, -100);
		searchFilter = new Filter(ConsoleConstants.FILTER_REPORT_DATE,
				FilterOperator.GE, temp.getTime());
		if (filterInSession != null && filterInSession.getFilter() != null) {
			searchFilter = Filter
					.AND(filterInSession.getFilter(), searchFilter);
		} // end if

		while (iter.hasNext()) {
			String key = iter.next();
			Filter filter = searchCriteria.get(key);
			if (filter != null) {
				searchFilter = Filter.AND(searchFilter, filter);
			} // endif
		} // end while
		return searchFilter;

	}

	/**
	 * Helper to highlight the tree text.
	 * 
	 * @param nodeName
	 */
	public static void hightlightNode(String nodeName) {
		if (nodeName.equals(ConsoleConstants.SYSTEM_FILTER_HOST_GROUPS)) {
			ConsoleHelper.getConsoleBean().setAllEventsStyleClass(
					ConsoleConstants.STYLE_NONE);
			DynamicNodeUserObject selectedNodeObject = ConsoleHelper
					.getSystemFilterBean().getSelectedNodeObject();
			if (selectedNodeObject != null) {
				selectedNodeObject
						.setStyleClass(ConsoleConstants.STYLE_HIGHLIGHT_NAVIGATOR);
			}

			DynamicNodeUserObject pubFilterDynaNodeUserObj = ConsoleHelper
					.getPublicFilterBean().getSelectedNodeObject();
			if (pubFilterDynaNodeUserObj != null) {
				pubFilterDynaNodeUserObj
						.setStyleClass(ConsoleConstants.STYLE_NONE);
			}
		} else if (nodeName
				.equals(ConsoleConstants.SYSTEM_FILTER_SERVICE_GROUPS)) {
			ConsoleHelper.getConsoleBean().setAllEventsStyleClass(
					ConsoleConstants.STYLE_NONE);
			DynamicNodeUserObject selectedNodeObject = ConsoleHelper
					.getSystemFilterBean().getSelectedNodeObject();
			if (selectedNodeObject != null) {
				selectedNodeObject
						.setStyleClass(ConsoleConstants.STYLE_HIGHLIGHT_NAVIGATOR);
			}

			DynamicNodeUserObject pubFilterDynaNodeUserObj = ConsoleHelper
					.getPublicFilterBean().getSelectedNodeObject();
			if (pubFilterDynaNodeUserObj != null) {
				pubFilterDynaNodeUserObj
						.setStyleClass(ConsoleConstants.STYLE_NONE);
			}
		} else if (nodeName.equals(ConsoleConstants.SYSTEM_FILTER_APPLICATIONS)) {
			ConsoleHelper.getConsoleBean().setAllEventsStyleClass(
					ConsoleConstants.STYLE_NONE);
			DynamicNodeUserObject selectedNodeObject = ConsoleHelper
					.getSystemFilterBean().getSelectedNodeObject();
			if (selectedNodeObject != null) {
				selectedNodeObject
						.setStyleClass(ConsoleConstants.STYLE_HIGHLIGHT_NAVIGATOR);
			}

			DynamicNodeUserObject pubFilterDynaNodeUserObj = ConsoleHelper
					.getPublicFilterBean().getSelectedNodeObject();
			if (pubFilterDynaNodeUserObj != null) {
				pubFilterDynaNodeUserObj
						.setStyleClass(ConsoleConstants.STYLE_NONE);
			}
		} else if (nodeName
				.equals(ConsoleConstants.SYSTEM_FILTER_OPERATION_STATUS)) {
			ConsoleHelper.getConsoleBean().setAllEventsStyleClass(
					ConsoleConstants.STYLE_NONE);
			DynamicNodeUserObject selectedNodeObject = ConsoleHelper
					.getSystemFilterBean().getSelectedNodeObject();
			if (selectedNodeObject != null) {
				selectedNodeObject
						.setStyleClass(ConsoleConstants.STYLE_HIGHLIGHT_NAVIGATOR);
			}

			DynamicNodeUserObject pubFilterDynaNodeUserObj = ConsoleHelper
					.getPublicFilterBean().getSelectedNodeObject();
			if (pubFilterDynaNodeUserObj != null) {
				pubFilterDynaNodeUserObj
						.setStyleClass(ConsoleConstants.STYLE_NONE);
			}
		} else if (nodeName
				.equals(ResourceUtils
						.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_DEFAULT))) {
			ConsoleHelper.getConsoleBean().setAllEventsStyleClass(
					ConsoleConstants.STYLE_HIGHLIGHT_NAVIGATOR);
			DynamicNodeUserObject selectedNodeObject = ConsoleHelper
					.getSystemFilterBean().getSelectedNodeObject();
			if (selectedNodeObject != null) {
				selectedNodeObject.setStyleClass(ConsoleConstants.STYLE_NONE);
			}

			DynamicNodeUserObject pubFilterDynaNodeUserObj = ConsoleHelper
					.getPublicFilterBean().getSelectedNodeObject();
			if (pubFilterDynaNodeUserObj != null) {
				pubFilterDynaNodeUserObj
						.setStyleClass(ConsoleConstants.STYLE_NONE);
			}
		} else {
			ConsoleHelper.getConsoleBean().setAllEventsStyleClass(
					ConsoleConstants.STYLE_NONE);
			DynamicNodeUserObject sysFilterDynaNodeUserObj = ConsoleHelper
					.getSystemFilterBean().getSelectedNodeObject();
			if (sysFilterDynaNodeUserObj != null) {
				sysFilterDynaNodeUserObj
						.setStyleClass(ConsoleConstants.STYLE_NONE);
			}
			DynamicNodeUserObject selectedNodeObject = ConsoleHelper
					.getPublicFilterBean().getSelectedNodeObject();
			if (selectedNodeObject != null) {
				selectedNodeObject
						.setStyleClass(ConsoleConstants.STYLE_HIGHLIGHT_NAVIGATOR);
			}

		} // end if
	} // end method

	/**
	 * Gets the public filters configuration
	 * 
	 * @return
	 */
	public static PublicFiltersConfigBean getPublicFilters() {
		PublicFiltersConfigBean pubFilters = null;
		ExternalContext exContext = FacesContext.getCurrentInstance()
				.getExternalContext();
		if (exContext != null) {
			Object context = exContext.getContext();
			if (context != null) {
				PortletContext servContext = (PortletContext) exContext
						.getContext();
				pubFilters = (PublicFiltersConfigBean) servContext
						.getAttribute(ConsoleConstants.CONSOLE_ADMIN_CONFIG_PROP);
			} // end if

		} // end if
		return pubFilters;
	}

	/**
	 * Gets the public filters configuration
	 * 
	 * @return
	 */
	public static void refreshPublicFilters() {
		try {
			PublicFiltersConfigBean pubFilters = refreshPublicFiltersConfigBean();
			ExternalContext exContext = FacesContext.getCurrentInstance()
					.getExternalContext();
			if (exContext != null) {
				Object context = exContext.getContext();
				if (context != null) {
					PortletContext servContext = (PortletContext) exContext
							.getContext();
					servContext.setAttribute(
							ConsoleConstants.CONSOLE_ADMIN_CONFIG_PROP,
							pubFilters);
				} // end if
			} // end if
		} catch (Exception ioe) {
			logger.error("Error loading console-admin-config.xml from default location "
					+ ioe.getMessage()
					+ ConsoleConstants.CONSOLE_ADMIN_CONFIG_PATH);
		}
	}

	/**
	 * Refresh public filters
	 */
	public static PublicFiltersConfigBean refreshPublicFiltersConfigBean()
			throws IOException, SAXException {
		PublicFiltersConfigBean pubFilters = null;
		Digester digester = new Digester();

		digester.setValidating(false);

		digester.addObjectCreate("PublicFilters", PublicFiltersConfigBean.class);

		digester.addObjectCreate("PublicFilters/Filter", FilterConfigBean.class);
		digester.addBeanPropertySetter("PublicFilters/Filter/Name", "name");
		digester.addBeanPropertySetter("PublicFilters/Filter/Label", "label");
		digester.addBeanPropertySetter("PublicFilters/Filter/AppType",
				"appType");
		digester.addBeanPropertySetter("PublicFilters/Filter/HostGroup",
				"hostGroup");
		digester.addBeanPropertySetter("PublicFilters/Filter/MonitorStatus",
				"monitorStatus");
		digester.addBeanPropertySetter("PublicFilters/Filter/Severity",
				"severity");
		digester.addBeanPropertySetter("PublicFilters/Filter/OpStatus",
				"opStatus");

		digester.addObjectCreate("PublicFilters/Filter/Fetch",
				FetchConfigBean.class);
		digester.addBeanPropertySetter("PublicFilters/Filter/Fetch/Size",
				"size");
		digester.addBeanPropertySetter("PublicFilters/Filter/Fetch/Order",
				"order");
		digester.addSetNext("PublicFilters/Filter/Fetch", "setFetch");
		digester.addObjectCreate("PublicFilters/Filter/Time",
				TimeConfigBean.class);
		digester.addBeanPropertySetter("PublicFilters/Filter/Time/Unit", "unit");
		digester.addBeanPropertySetter("PublicFilters/Filter/Time/Measurement",
				"measurement");
		digester.addSetNext("PublicFilters/Filter/Time", "setTime");

		digester.addObjectCreate("PublicFilters/Filter/DynaProperty",
				DynaProperty.class);
		digester.addBeanPropertySetter(
				"PublicFilters/Filter/DynaProperty/PropName", "propName");
		digester.addBeanPropertySetter(
				"PublicFilters/Filter/DynaProperty/PropValue", "propValue");
		digester.addBeanPropertySetter(
				"PublicFilters/Filter/DynaProperty/DataType", "dataType");
		digester.addBeanPropertySetter(
				"PublicFilters/Filter/DynaProperty/Operator", "operator");
		digester.addSetNext("PublicFilters/Filter/DynaProperty",
				"setDynaProperty");

		digester.addSetNext("PublicFilters/Filter", "addFilterConfigs");

		File inputFile = new File(ConsoleConstants.CONSOLE_ADMIN_CONFIG_PATH);
		pubFilters = (PublicFiltersConfigBean) digester.parse(inputFile);

		return pubFilters;
	}

	/**
	 * Helper method to get the LocaleBean
	 * 
	 * @return
	 */
	public static LocaleBean getLocaleBean() {

		LocaleBean localeBean = null;
		Object localeObject = ManagedBeanFactory
				.getManagedBean(ConsoleConstants.MANAGED_BEAN_LOCALE);
		if (localeObject instanceof LocaleBean) {
			localeBean = (LocaleBean) localeObject;
		}
		return localeBean;
	}

	/**
	 * Overloaded version of buildStatusviewerURL
	 * 
	 * @param name
	 * @param nodeType
	 * @return
	 */
	public static String buildStatusviewerURL(String name, String nodeType) {
		return buildStatusviewerURL(name, nodeType, null, null);
	}

	/**
	 * Returns statusviewer URL of given node
	 * 
	 * @param nodeNameParam
	 * @param nodeType
	 * @param parentType
	 * @param parentNameParam
	 * @return statusviewer URL of node
	 */
	public static String buildStatusviewerURL(String nodeType,
			String nodeNameParam, String parentType, String parentNameParam) {

		String nodeName, parentName;

		if ("host".equalsIgnoreCase(nodeType)
				&& "127.0.0.1".equalsIgnoreCase(nodeNameParam)) {
			nodeName = "localhost";
		} else {
			nodeName = nodeNameParam;
		}

		if (ConsoleConstants.HOST.equalsIgnoreCase(parentType)
				&& "127.0.0.1".equalsIgnoreCase(parentNameParam)) {
			parentName = "localhost";
		} else {
			parentName = parentNameParam;
		}

		/* the host contains hostname:port */
		// if (secureAccess) {
		// host = "https://" + request.getServerName();
		//
		// } else {
		// }
		// logger.info("Report URL: " + mappingURL);
		/* building the actual report URL */
		if (nodeType.equalsIgnoreCase(ConsoleConstants.HOST)) {
			return baseURL + ConsoleConstants.HOST + ConsoleConstants.EQ
					+ nodeName;
		} else if (nodeType.equalsIgnoreCase(ConsoleConstants.HOSTGROUP)) {
			return baseURL + ConsoleConstants.HOSTGROUP + ConsoleConstants.EQ
					+ nodeName;
		} else if (nodeType.equalsIgnoreCase(ConsoleConstants.SERVICEGROUP)) {
			return baseURL + ConsoleConstants.SERVICEGROUP
					+ ConsoleConstants.EQ + nodeName;
		} else if (nodeType.equalsIgnoreCase(ConsoleConstants.SERVICE)) {
			// service
			// if service is under host
			if (ConsoleConstants.HOST.equalsIgnoreCase(parentType)) {
				return baseURL + ConsoleConstants.HOST + ConsoleConstants.EQ
						+ parentName + ConsoleConstants.AMP
						+ ConsoleConstants.SERVICE + ConsoleConstants.EQ
						+ nodeName;
			} else if (ConsoleConstants.SERVICEGROUP
					.equalsIgnoreCase(parentType)) {
				return baseURL + ConsoleConstants.SERVICEGROUP
						+ ConsoleConstants.EQ + parentName
						+ ConsoleConstants.AMP + ConsoleConstants.SERVICE
						+ ConsoleConstants.EQ + nodeName;
			}
		}
		return "";
	}

	/**
	 * get base URL
	 * 
	 * @return base url
	 */
	public static String getBaseURL() {
		return baseURL;
	}

	/**
	 * Returns whether links to SV are enabled.
	 * 
	 * @return the linksEnabled
	 */
	public static Boolean isLinksEnabled() {
		return linksEnabled;
	}

	/**
	 * Protected Constructor - Rationale: Instantiating utility classes does not
	 * make sense. Hence the constructors should either be private or (if you
	 * want to allow sub-classing) protected. <br>
	 * 
	 * Refer to "HideUtilityClassConstructor" section in
	 * http://checkstyle.sourceforge.net/config_design.html.
	 */
	protected ConsoleHelper() {
		// prevents calls from subclass
		throw new UnsupportedOperationException();
	}

	/**
	 * Gets the ExtendedRole Attributes
	 * 
	 * @return List<ExtendedUIRole>
	 */

	public static List<ExtendedUIRole> getExtendedRoleAttributes() {
		List<ExtendedUIRole> retObj = null;
		try {
			HttpServletRequest request = (HttpServletRequest) PolicyContext
					.getContext("javax.servlet.http.HttpServletRequest");
			if (request != null && request.getSession() != null) {
				Object obj = request.getSession().getAttribute(
						EXTENDED_ROLE_ATT_CONSOLE);
				if (obj == null) {
					retObj = com.groundworkopensource.portal.common.FacesUtils
							.getExtendedRoles().getList();
					request.getSession().setAttribute(EXTENDED_ROLE_ATT_CONSOLE,
							retObj);
				}
			} // end if
		} catch (Exception exc) {
			logger.error("Unable to read extendedrole attributes");
		}
		return retObj;
	}

	/**
	 * Helper method to get the Tabset
	 * 
	 * @return
	 */
	public static ExtendedUIRoleBean getExtendedUIRoleBean() {
		ExtendedUIRoleBean ExtendedUIRoleBean = null;
		Object ExtendedUIRoleBeanObject = ManagedBeanFactory
				.getManagedBean("extendedUIRoleBean");
		if (ExtendedUIRoleBeanObject instanceof ExtendedUIRoleBean) {
			ExtendedUIRoleBean = (ExtendedUIRoleBean) ExtendedUIRoleBeanObject;
		}
		return ExtendedUIRoleBean;

	}

    /**
     * Host group bubble up status to bubble up status icon paths mapping.
     */
    private static final Map<String,String> HOST_GROUP_BUBBLE_UP_STATUS_ICON_PATHS = new HashMap<String,String>();
    static {
        HOST_GROUP_BUBBLE_UP_STATUS_ICON_PATHS.put("WARNING", ConsoleConstants.HOST_GROUP_WARNING);
        HOST_GROUP_BUBBLE_UP_STATUS_ICON_PATHS.put("WARNING_HOST", ConsoleConstants.HOST_GROUP_WARNING);
        HOST_GROUP_BUBBLE_UP_STATUS_ICON_PATHS.put("PENDING", ConsoleConstants.HOST_GROUP_PENDING);
        HOST_GROUP_BUBBLE_UP_STATUS_ICON_PATHS.put("PENDING_HOST", ConsoleConstants.HOST_GROUP_PENDING);
        HOST_GROUP_BUBBLE_UP_STATUS_ICON_PATHS.put("DOWN", ConsoleConstants.HOST_GROUP_DOWN_UNSCHEDULED);
        HOST_GROUP_BUBBLE_UP_STATUS_ICON_PATHS.put("UNSCHEDULED DOWN", ConsoleConstants.HOST_GROUP_DOWN_UNSCHEDULED);
        HOST_GROUP_BUBBLE_UP_STATUS_ICON_PATHS.put("SCHEDULED DOWN", ConsoleConstants.HOST_GROUP_DOWN_SCHEDULED);
        HOST_GROUP_BUBBLE_UP_STATUS_ICON_PATHS.put("UNREACHABLE", ConsoleConstants.HOST_GROUP_UNREACHABLE);
        HOST_GROUP_BUBBLE_UP_STATUS_ICON_PATHS.put("UP", ConsoleConstants.HOST_GROUP_UP);
    }

	/**
	 * Translate bubble up status to icon path for the hostGroup here.
	 * 
	 * @param bubbleUpStatus host group up status
	 * @return bubble up status icon path
	 */
	public static String determineBubbleUpStatusForHostGroup(String bubbleUpStatus) {
        if (bubbleUpStatus != null) {
            bubbleUpStatus = HOST_GROUP_BUBBLE_UP_STATUS_ICON_PATHS.get(bubbleUpStatus.toUpperCase());
        }
		return ((bubbleUpStatus != null) ? bubbleUpStatus : ConsoleConstants.HOST_GROUP_NO_STATUS);
	}

    /**
     * Service group bubble up status to bubble up status icon paths mapping.
     */
    private static final Map<String,String> SERVICE_GROUP_BUBBLE_UP_STATUS_ICON_PATHS = new HashMap<String,String>();
    static {
        SERVICE_GROUP_BUBBLE_UP_STATUS_ICON_PATHS.put("CRITICAL", ConsoleConstants.SERVICE_GROUP_CRITICAL_UNSCHEDULED);
        SERVICE_GROUP_BUBBLE_UP_STATUS_ICON_PATHS.put("UNSCHEDULED CRITICAL", ConsoleConstants.SERVICE_GROUP_CRITICAL_UNSCHEDULED);
        SERVICE_GROUP_BUBBLE_UP_STATUS_ICON_PATHS.put("WARNING", ConsoleConstants.SERVICE_GROUP_WARNING);
        SERVICE_GROUP_BUBBLE_UP_STATUS_ICON_PATHS.put("WARNING_SERVICE", ConsoleConstants.SERVICE_GROUP_WARNING);
        SERVICE_GROUP_BUBBLE_UP_STATUS_ICON_PATHS.put("PENDING", ConsoleConstants.SERVICE_GROUP_PENDING);
        SERVICE_GROUP_BUBBLE_UP_STATUS_ICON_PATHS.put("PENDING_SERVICE", ConsoleConstants.SERVICE_GROUP_PENDING);
        SERVICE_GROUP_BUBBLE_UP_STATUS_ICON_PATHS.put("SCHEDULED CRITICAL", ConsoleConstants.SERVICE_GROUP_CRITICAL_SCHEDULED);
        SERVICE_GROUP_BUBBLE_UP_STATUS_ICON_PATHS.put("UNKNOWN", ConsoleConstants.SERVICE_GROUP_UNKNOWN);
        SERVICE_GROUP_BUBBLE_UP_STATUS_ICON_PATHS.put("OK", ConsoleConstants.SERVICE_GROUP_OK);
    }

    /**
     * Translate bubble up status to icon path for the serviceGroup here.
	 *
	 * @param bubbleUpStatus service group bubble up status
	 * @return bubble up status icon path
	 */
	public static String determineBubbleUpStatusForServiceGroup(String bubbleUpStatus) {
        if (bubbleUpStatus != null) {
            bubbleUpStatus = SERVICE_GROUP_BUBBLE_UP_STATUS_ICON_PATHS.get(bubbleUpStatus.toUpperCase());
        }
        return ((bubbleUpStatus != null) ? bubbleUpStatus : ConsoleConstants.SERVICE_GROUP_NO_STATUS);
	}
}

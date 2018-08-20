/*
 * Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork) All rights
 * reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.webapp.console;

import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.portlet.PortletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.NavigationHelper;
import com.groundworkopensource.portal.model.UserNavigation;
import com.icesoft.faces.async.render.SessionRenderer;
import com.icesoft.faces.component.ext.HtmlCommandButton;
import com.icesoft.faces.component.outputmedia.OutputMedia;
import com.icesoft.faces.component.paneltabset.TabChangeEvent;
import com.icesoft.faces.context.effects.JavascriptContext;

/**
 * The Class TabsetBean.
 */
public class TabsetBean extends EventConsoleServerPush {

	/**
	 * Servlet which reads MP3 files from file system.
	 */
	private static final String GET_MP3_FILE_SERVLET_PATH = "/getmp3?filePath=";

	/**
	 * PORTAL_CONSOLE_RESOURCES_PATH used in jqeury jplayer plugin as a "swf"
	 * path
	 */
	private static final String PORTAL_CONSOLE_RESOURCES_PATH = "/portal-console/resources/";

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -5811057606608263411L;

	/** The Constant NUMBER_OF_TABS. */
	private static final int NUMBER_OF_TABS = 2;

	/** The logger. */
	private static Logger logger = Logger.getLogger(TabsetBean.class.getName());

	/** The tabs. */
	private List<Tab> tabs = new ArrayList<Tab>(NUMBER_OF_TABS);

	/** The filter bean. */
	private FilterBean filterBean = null;

	/** The tab index. */
	private int tabIndex;

	/**
	 * Id of logged in user.
	 */
	private String userId;
	/**
	 * tabIndexID
	 */
	private int tabMaxIndexID;

	/**
	 * NavigationHelper
	 */
	private NavigationHelper navigationHelper;

	/**
	 * tabFilterRender
	 */
	private boolean tabFilterRender;

	/**
	 * hiddenField to initialize tab filter.
	 */
	private String hiddenField = ConsoleConstants.EMPTY_STRING;

	/** The extended ui role bean. */
	private ExtendedUIRoleBean extendedUIRoleBean;
	/**
	 * tabSize
	 */
	private int tabSize;

	/** The output media. */
	private OutputMedia outputMedia;
	/**
 * 
 */
	private String sourceMedia;
	/**
	 * autoStart
	 */
	private int autoStart = 0;
	/**
	 * alarm Severity
	 */
	private String alarmSeverity;
	/**
	 * source Media Path Map
	 */
	private static Map<String, String> sourceMediaPathMap = new HashMap<String, String>();
	/**
	 * loop of media file.Default is true
	 */
	private int mediaLoop = 0;
	/**
	 * SilenceAlarm
	 */
	private boolean silenceAlarm = true;

	/** The faces context. */
	private FacesContext facesContext;

	/** The init jplayer. */
	private boolean initJPlayer = false;

	/**
	 * enable/disable alarm
	 */
	private boolean enableAlarm = false;

	/** The alarm media file path. */
	private String alarmMediaFilePath;

	static {
		sourceMediaPathMap.put("DOWN", ConsoleConstants.EMPTY_STRING);
		sourceMediaPathMap.put("CRITICAL", ConsoleConstants.EMPTY_STRING);
		sourceMediaPathMap.put("UNREACHABLE", ConsoleConstants.EMPTY_STRING);

		sourceMediaPathMap.put("WARNING", ConsoleConstants.EMPTY_STRING);
		sourceMediaPathMap.put("PENDING", ConsoleConstants.EMPTY_STRING);

		sourceMediaPathMap.put("UNKNOWN", ConsoleConstants.EMPTY_STRING);
		sourceMediaPathMap.put("UP", ConsoleConstants.EMPTY_STRING);
		sourceMediaPathMap.put("OK", ConsoleConstants.EMPTY_STRING);
	}

	/**
	 * Instantiates a new tabset bean.
	 */
	public TabsetBean() {
		navigationHelper = new NavigationHelper();
		/**
		 * Faces context will be null on JMS or Non JSF thread. Perform a null
		 * check. Make increase the visibility of the statisbean to class level
		 * for the JMS thread.
		 */
		facesContext = FacesContext.getCurrentInstance();

		if (facesContext != null) {
			extendedUIRoleBean = ConsoleHelper.getExtendedUIRoleBean();
		}
		// take id of logged in user

		userId = FacesUtils.getLoggedInUser();

		// fetch tab history and add to tabs
		fetchTabHistory(userId);

		Tab toAddNew = new Tab(
				ResourceUtils
						.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_NEW));
		toAddNew.setHiddenLabel(ResourceUtils
				.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_NEW));
		toAddNew.setTabId(Tab.SEARCH_PANELID_PREFIX + ++tabMaxIndexID);
		tabs.add(toAddNew);
		toAddNew.setRendered(false);
		try {
			// getting property from console.properties file to enable/disable
			// alarm
			String enableAlarms = PropertyUtils
					.getProperty("console.enable.audible.alarms");
			if (null != enableAlarms) {
				enableAlarm = Boolean.parseBoolean(enableAlarms.trim());
			}
		} catch (Exception e) {
			logger.error(" exception while getting property console.enable.audible.alarms");
			enableAlarm = false;
		}

		filterBean = ConsoleHelper.getFilterBean();
		tabFilterRender = true;

		alarmSeverity = PropertyUtils.getProperty("alarm_severity");

		// get alarm media file path
		alarmMediaFilePath = PropertyUtils.getProperty("alarm.file.media.path");
		if (null == alarmMediaFilePath
				|| ConsoleConstants.EMPTY_STRING.equals(alarmMediaFilePath)) {
			// assign default media path
			alarmMediaFilePath = "/usr/local/groundwork/config/media/";
		}

		PortletRequest portletRequest = (PortletRequest) facesContext
				.getExternalContext().getRequest();

		String requestContextPath = portletRequest.getScheme() + "://"
				+ portletRequest.getServerName()
				+ portletRequest.getContextPath();

		String servletName = GET_MP3_FILE_SERVLET_PATH;
		// get severity and file mapping
		String statusFileMappingProperty = PropertyUtils
				.getProperty("alarm.file.status.mapping");
		if (null != statusFileMappingProperty
				&& !ConsoleConstants.EMPTY_STRING
						.equals(statusFileMappingProperty)) {
			String[] split = statusFileMappingProperty.split(",");
			if (null != split) {
				for (String statusFileMapping : split) {
					String[] statusAndFile = statusFileMapping.split(":");
					if (statusAndFile != null && statusAndFile.length == 2) {
						sourceMediaPathMap.put(statusAndFile[0].trim()
								.toUpperCase(), requestContextPath
								+ servletName + alarmMediaFilePath
								+ statusAndFile[1].trim());
					}
				}
			}
		}

		initJPlayer = true;
	}

	/**
	 * Receive message from topic subscriber (non-Javadoc).
	 * 
	 * @param xmlMessage
	 *            the xml message
	 * 
	 * @see com.groundworkopensource.webapp.console.ServerPush#refresh(java.lang.String)
	 */
	@Override
	public void refresh(String xmlMessage) {
		logger.debug("Enter refresh method on TabsetBean ");
		try {
			if (xmlMessage != null) {
				logger.debug("Updating the active tab-" + tabIndex);
				Tab curTab = tabs.get(tabIndex);
				/*
				 * Highlight highlightEffect = new Highlight("#FF0000");
				 * highlightEffect.setDuration(new Float(5.0f));
				 * highlightEffect.setStartColor("#FF0000");
				 * 
				 * Shake shake = new Shake(); shake.setDuration(5f);
				 * curTab.setNewMessageEffect(highlightEffect);
				 */
				DataTableBean dataTableBean = curTab.getDataTableBean();
				dataTableBean.setLastPage(null);
				dataTableBean.setFilterBean(filterBean);
				DataPage page = dataTableBean.fetchPage(0, Integer
						.parseInt(PropertyUtils
								.getProperty(ConsoleConstants.PROP_PAGE_SIZE)));
				dataTableBean.setPage(page);

				List<EventBean> eventList = page.getData();
				EventBean[] events = eventList.toArray(new EventBean[eventList
						.size()]);
				dataTableBean.setEvents(events);
				if (!isSilenceAlarm() && enableAlarm) {

					String xml = "<LOGMESSAGE>" + xmlMessage + "</LOGMESSAGE>";
					int[] eventID = getLogMessageID(xml);

					if (eventID != null) {
						List<EventBean> eventsByIds = new EventQueryManager()
								.queryForEventsByIds(eventID, null, -1, -1);
						if (!eventsByIds.isEmpty()) {

							Map<String, Boolean> alarmSeverityMap = getAlarmSeverityMap();
							Iterator<EventBean> iterator = eventsByIds
									.iterator();
							while (iterator.hasNext()) {
								EventBean eventBean = iterator.next();

								String status = eventBean.getMonitorStatus()
										.getValue();
								alarmSeverityMap
										.put(status.toUpperCase(), true);
							}
							Set<Entry<String, Boolean>> entrySet = alarmSeverityMap
									.entrySet();
							Iterator<Entry<String, Boolean>> alarmSeverityMapIterator = entrySet
									.iterator();
							String severity = null;
							while (alarmSeverityMapIterator.hasNext()) {
								Entry<String, Boolean> entry = alarmSeverityMapIterator
										.next();
								severity = entry.getKey();
								Boolean severityValue = entry.getValue();
								if (severityValue) {
									break;
								}

							}
							// setSourceMedia(sourceMediaPathMap.get(Severity));

							// construct GetMP3File servlet path

							if (initJPlayer) {
								// call JS method to initialize jplayer and play
								// the file
								JavascriptContext
										.addJavascriptCall(
												facesContext,
												"initializeAlarm('"
														+ PORTAL_CONSOLE_RESOURCES_PATH
														+ "', '"
														+ sourceMediaPathMap
																.get(severity)
														+ "'); ");
								initJPlayer = false;
							} else {
								// call JS method to play sound
								JavascriptContext
										.addJavascriptCall(
												facesContext,
												"playAlarm('"
														+ sourceMediaPathMap
																.get(severity)
														+ "'); ");
								// setAutoStart(1);
							}

						}
					}
				}
				logger.debug("Unpause mode for incoming events..");
				// renderer.requestRender();
				/*
				 * RenderManager.getInstance()
				 * .getOnDemandRenderer(groupRenderName).requestRender();
				 */
				SessionRenderer.render(groupRenderName);
				logger.debug("On demand  render  for incoming events..");

			} // end try/catch
		} catch (Exception exc) {
			logger.error(exc.getMessage());
		}
	}

	/**
	 * return alarm severity map
	 */
	private Map<String, Boolean> getAlarmSeverityMap() {
		Map<String, Boolean> alarmSeverityMap = new LinkedHashMap<String, Boolean>();
		if (alarmSeverity == null) {
			alarmSeverity = "UNSCHEDULED DOWN,UNSCHEDULED CRITICAL,DOWN,CRITICAL,UNREACHABLE,SCHEDULED DOWN,WARNING,PENDING,SCHEDULED CRITICAL,UNKNOWN,UP,OK";
		}

		String[] splitAlarmSeverity = alarmSeverity.split(",");
		for (int i = 0; i < splitAlarmSeverity.length; i++) {
			alarmSeverityMap.put(splitAlarmSeverity[i], false);
		}

		return alarmSeverityMap;
	}

	/**
	 * returns log message id array
	 * 
	 * @param xmlMessage
	 */
	private int[] getLogMessageID(String xmlMessage) {
		int[] logMessgaeID = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xmlMessage));
			Document doc = builder.parse(is);
			NodeList deviceElements = doc.getElementsByTagName("EVENT");
			logMessgaeID = new int[deviceElements.getLength()];
			for (int i = 0; i < deviceElements.getLength(); i++) {
				Element lstElmnt = (Element) deviceElements.item(i);
				Node firstChild = lstElmnt.getFirstChild();

				String nodeValue = firstChild.getNodeValue();
				String[] split = nodeValue.split(";");
				String entityID = split[split.length - 1].split("=")[1];
				logMessgaeID[i] = Integer.parseInt(entityID);
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return logMessgaeID;
	}

	/**
	 * Gets the tabs.
	 * 
	 * @return the tabs
	 */
	public List<Tab> getTabs() {

		return tabs;
	}

	/**
	 * Sets the tabs.
	 * 
	 * @param tabs
	 *            the new tabs
	 */
	public void setTabs(List<Tab> tabs) {
		this.tabs = tabs;
	}

	/**
	 * Listener for tabSelection.
	 * 
	 * @param e
	 * 
	 */
	public void tabSelection(TabChangeEvent e) {

		logger.debug("Enter tabSelection method");
		Tab tab = tabs.get(tabIndex);
		String tabLabel = tab.getHiddenLabel();

		if (tabLabel
				.equals(ResourceUtils
						.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_NEW))) {

			// store selected node (navigation history) into database as per the
			// user id.
			try {
				navigationHelper
						.addHistoryRecord(
								userId,
								tabMaxIndexID,
								ResourceUtils
										.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_DEFAULT),
								ConsoleConstants.EMPTY_STRING,
								null,
								ResourceUtils
										.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_DEFAULT),
								ConsoleConstants.APP_TYPE_CONSOLE,
								ResourceUtils
										.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_DEFAULT));
			} catch (NumberFormatException e1) {
				// ignore
				logger.debug("NumberFormatException : nodeId seems to be incorrect ["
						+ tabMaxIndexID + "]");
			} catch (IOException e2) {
				// ignore
				logger.warn("Failed to add node navigation information into database for user with Id ["
						+ userId
						+ "]. Node name for the tab ["
						+ ResourceUtils
								.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_NEW)
						+ "]");
			}
			tabMaxIndexID++;
			Tab toAddNew = new Tab(
					ResourceUtils
							.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_NEW));
			toAddNew.setHiddenLabel(ResourceUtils
					.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_NEW));
			toAddNew.setTabId(Tab.SEARCH_PANELID_PREFIX + tabMaxIndexID);
			this.addTab(toAddNew);

			tab.setLabel(ResourceUtils
					.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_DEFAULT));
			tab.setHiddenLabel(ResourceUtils
					.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_DEFAULT));
			filterBean.setFilter(null);
			// Tab prevTab = tabs.get(selectedIndex - 1);
			Tab prevTab = tabs.get(tabIndex - 1);
			prevTab.setRendered(false);
			DataTableBean prevDataTable = prevTab.getDataTableBean();
			prevDataTable.setDynamicColumns(null);
			prevDataTable.constructComponent();
			DataTableBean dataBean = new DataTableBean();
			tab.setDataTableBean(dataBean);
			tab.setRendered(true);
			tab.setSearchCriteria(new SearchBean());
		} else {
			tab.getActionBean().reset();
			SearchBean searchBean = tabs.get(this.tabIndex).getSearchCriteria();
			ConsoleManager mgr = ConsoleHelper.getConsoleManager();
			mgr.populateData(tab, searchBean);
			tab.setRendered(true);
			FreezeTableBean freezeBean = tab.getFreezeBean();
			if (freezeBean
					.getFreezeButtonText()
					.equalsIgnoreCase(
							ResourceUtils
									.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_RESUME_EVENTS))) {
				freezeBean.reset();
				// dynamicTabSet.setSelectedIndex(selectedIndex);
			}
		} // end if

		if (e != null) {
			int oldTabIndex = e.getOldTabIndex();
			DataTableBean oldTabDataTable = tabs.get(oldTabIndex)
					.getDataTableBean();
			DataTableBean currentTabDataTable = tabs.get(tabIndex)
					.getDataTableBean();
			if (oldTabDataTable.getDynamicColumns() != null
					&& currentTabDataTable.getDynamicColumns() == null) {
				oldTabDataTable.setDynamicColumns(null);
				oldTabDataTable.constructComponent();
			} else if (oldTabDataTable.getDynamicColumns() == null
					&& currentTabDataTable.getDynamicColumns() != null) {
				oldTabDataTable.setDynamicColumns(currentTabDataTable
						.getDynamicColumns());
				oldTabDataTable.constructComponent();
			} else if (oldTabDataTable.getDynamicColumns() != null
					&& currentTabDataTable.getDynamicColumns() != null) {
				oldTabDataTable.setDynamicColumns(currentTabDataTable
						.getDynamicColumns());
				oldTabDataTable.constructComponent();
			}

			logger.debug("Exit tabSelection method");
		}
	}

	/**
	 * Adds a new tab.
	 * 
	 * @param tab
	 *            the tab
	 */
	public void addTab(Tab tab) {
		// tab.setTabId(Tab.SEARCH_PANELID_PREFIX + tabs.size() + 1);
		tabs.add(tab);

	}


	/**
	 * Closes the tab.
	 * 
	 * @param e
	 * 
	 */
	public void closeTab(ActionEvent e) {
		logger.debug("Close Clicked");
		HtmlCommandButton button = (HtmlCommandButton) e.getComponent();
		if (button != null) {
			String selectedTabID = (String) button.getValue();
			Tab closeTab = null;
			int closeTabIndex;
			if ((isAtleastOneNewTabDown() || isAtleastOneNewTabUp())
					&& tabs.size() > 2) {

				Tab currentlySelectedTab = tabs.get(tabIndex);
				if (!currentlySelectedTab.getTabId().equalsIgnoreCase(
						selectedTabID)) {
					// tab to be close
					closeTab = searchTab(selectedTabID);
					if (closeTab == null) {
						return;
					}
					closeTabIndex = tabs.indexOf(closeTab);
				} else {
					closeTab = currentlySelectedTab;
					closeTabIndex = tabIndex;
				}

				String tabid = closeTab.getTabId();
				int tabidIndex = 0;
				try {
					tabidIndex = Integer.parseInt(tabid
							.substring(Tab.SEARCH_PANELID_PREFIX.length()));
					navigationHelper.deleteHistoryRecord(userId, tabidIndex,
							ConsoleConstants.APP_TYPE_CONSOLE);
				} catch (NumberFormatException numberFormatException) {
					// ignore
					logger.debug("NumberFormatException : tabId seems to be incorrect ["
							+ tabidIndex + "]");
				} catch (IOException ioException) {
					// ignore
					logger.warn("Failed to remove/delete node navigation information from database for user with Id ["
							+ userId
							+ "].  for the tab ["
							+ closeTab.getLabel() + "]");
				} catch (Exception exception) {
					// ignore
					logger.warn("Failed to remove/delete node navigation information from database for user with Id ["
							+ userId
							+ "].  for the tab ["
							+ closeTab.getLabel() + "]");
				}

				tabs.remove(closeTabIndex);

				if (tabIndex == closeTabIndex) {
					if (tabIndex > 0) {
						tabIndex--;
					}
					this.tabSelection(null);

				} else if (tabIndex > closeTabIndex) {
					tabIndex--;
				}

			} // end if
		}
	}

	/**
	 * Searches tab (based on Tab id).
	 * 
	 * @param tabId
	 * 
	 * @return searched tab on successful search. If search fails, returns null.
	 */
	private Tab searchTab(String tabId) {
		for (Tab tab : tabs) {
			if (tabId.equals(tab.getTabId())) {
				return tab;
			}
		}
		return null;
	}

	/**
	 * Checks if there is any new tab down the selected tab.
	 * 
	 * @return true, if checks if is atleast one new tab down
	 */
	public boolean isAtleastOneNewTabDown() {
		boolean result = false;
		for (int i = 0; i < tabIndex; i++) {
			if (tabs.get(i)
					.getLabel()
					.equals(ResourceUtils
							.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_NEW))) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * Checks if there is any new tab up the selected tab.
	 * 
	 * @return true, if checks if is atleast one new tab up
	 */
	public boolean isAtleastOneNewTabUp() {
		boolean result = false;
		if (tabIndex == tabs.size() - 1) {
			return false;
		}
		for (int i = tabIndex; i <= tabs.size() - 1; i++) {
			if (tabs.get(i)
					.getLabel()
					.equals(ResourceUtils
							.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_NEW))) {
				result = true;
				break;
			} // end if
		} // end for
		return result;
	}

	/**
	 * Stops the topic connection.
	 */
	public void stopTopicConnection() {
		try {
			// connection.stop();
			this.listenToTopic = null;
			logger.debug("Topic connection stopped...");
		} catch (Exception exc) {
			logger.error(exc.getMessage());
		} // end try/catch block
	}

	/**
	 * Starts the topic connection.
	 */
	public void startTopicConnection() {
		try {
			// connection.start();
			this.listenToTopic = "event.topic.name";
			logger.debug("Topic connection started...");
		} catch (Exception exc) {
			logger.error(exc.getMessage());
		} // end try/catch block
	}

	/**
	 * Gets the tab index.
	 * 
	 * @return the tab index
	 */
	public int getTabIndex() {
		return tabIndex;
	}

	/**
	 * Sets the tab index.
	 * 
	 * @param tabIndex
	 *            the new tab index
	 */
	public void setTabIndex(int tabIndex) {
		this.tabIndex = tabIndex;
	}

	/**
	 * fetchNavigationHistory
	 * 
	 * @param userId
	 */
	public void fetchTabHistory(String userId) {
		try {
			List<UserNavigation> historyRecords = navigationHelper.getHistoryRecords(
                    userId, ConsoleConstants.APP_TYPE_CONSOLE);
			tabMaxIndexID = navigationHelper.getMaxNodeID(userId,
					ConsoleConstants.APP_TYPE_CONSOLE);

			// check if console navigation history null or empty , add default
			// tab.
			if (historyRecords == null || historyRecords.isEmpty()) {
				tabMaxIndexID++;
				addDefaultTab(userId);
				return;
			}
			boolean render = true;

			for (UserNavigation historyRecord : historyRecords) {
				String filterType = historyRecord.getNodeType();
				String tabHiddenLabel = historyRecord.getNodeName();
				String tabId = String.valueOf(historyRecord.getNodeId());
				String searchCriteria = null;
				if (null != historyRecord.getTabHistory()) {
					searchCriteria = historyRecord.getTabHistory();
				}
				String tabLabel = null;
				if (null != historyRecord.getNodeLabel()) {
					tabLabel = historyRecord.getNodeLabel();
				}

				if (ConsoleConstants.SYSTEM_FILTER_HOST_GROUPS
						.equals(filterType)) {

					if (extendedUIRoleBean.getHostGroupList().isEmpty()
							|| extendedUIRoleBean.getHostGroupList().size() == 0) {
						addNavigationTab(render, filterType, tabHiddenLabel,
								tabId, searchCriteria, tabLabel);
						render = false;
						continue;

					} else {
						int filterNameIndex = tabHiddenLabel.indexOf("=");
						String filterStr = tabHiddenLabel.substring(
								filterNameIndex + 1, tabHiddenLabel.length());
						if (filterStr != null
								&& !extendedUIRoleBean.getHostGroupList()
										.contains(filterStr.toLowerCase())) {
							// if host group is not available in extended role
							// host group list,delete tab history record from
							// database.
							removeTabHistory(userId, tabHiddenLabel, tabId);
							continue;
						}
					}
				} else if (ConsoleConstants.SYSTEM_FILTER_SERVICE_GROUPS
						.equals(filterType)) {
					if (extendedUIRoleBean.getServiceGroupList().isEmpty()
							|| extendedUIRoleBean.getServiceGroupList().size() == 0) {
						addNavigationTab(render, filterType, tabHiddenLabel,
								tabId, searchCriteria, tabLabel);
						render = false;
						continue;

					} else {
						int filterNameIndex = tabHiddenLabel.indexOf("=");
						String filterStr = tabHiddenLabel.substring(
								filterNameIndex + 1, tabHiddenLabel.length());
						if (filterStr != null
								&& !extendedUIRoleBean.getServiceGroupList()
										.contains(filterStr.toLowerCase())) {
							// if service group is not available in extended
							// role
							// service group list ,delete tab history record
							// from
							// data base.
							removeTabHistory(userId, tabHiddenLabel, tabId);
							continue;
						}
					}
				}
				addNavigationTab(render, filterType, tabHiddenLabel, tabId,
						searchCriteria, tabLabel);
				render = false;

				// tabs.add(newTab);
			}
			// no tab added to user ,adding default tab
			if (render) {
				addDefaultTab(userId);
			}
		} catch (IOException e) {
			logger.error("IOException while fetching Navigation History for user with Id ["
					+ userId + "]. Actual Exception : " + e);
		}
	}

	/**
	 * add navigation tab into tabs list
	 * 
	 * @param render
	 * @param filterType
	 * @param tabHiddenLabel
	 * @param tabId
	 */
	private void addNavigationTab(boolean render, String filterType,
			String tabHiddenLabel, String tabId, String searchCriteriaXML,
			String tabLabel) {
		if (null == tabLabel) {
			tabLabel = tabHiddenLabel;
		}
		Tab newTab = new Tab(tabLabel);
		newTab.setHiddenLabel(tabHiddenLabel);
		newTab.setTabId(Tab.SEARCH_PANELID_PREFIX + tabId);
		newTab.setFilterType(filterType);
		if (null != searchCriteriaXML) {
			SearchBean searchBean = getSearchBeanByXML(searchCriteriaXML);
			newTab.setSearchCriteria(searchBean);
		}
		newTab.setRendered(render);
		tabs.add(newTab);

	}

	/**
	 * Get search bean form search criteria XML.
	 * 
	 * @param searchCriteriaXML
	 * @return SearchBean
	 */
	private SearchBean getSearchBeanByXML(String searchCriteriaXML) {
		SearchBean searchBean = new SearchBean();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(
					new StringReader(searchCriteriaXML));
			Document doc = builder.parse(is);
			NodeList deviceElements = doc
					.getElementsByTagName(ConsoleConstants.DEVICE);
			String device = getChildNodeValue(deviceElements);
			if (null != device
					&& !ConsoleConstants.EMPTY_STRING.equalsIgnoreCase(device)) {
				searchBean.setHost(device);
			}
			NodeList messageElements = doc
					.getElementsByTagName(ConsoleConstants.MESSAGE);
			String message = getChildNodeValue(messageElements);
			if (null != message
					&& !ConsoleConstants.EMPTY_STRING.equalsIgnoreCase(message)) {
				searchBean.setMessage(message);
			}
			NodeList datetimeElements = doc
					.getElementsByTagName(ConsoleConstants.DATETIME);
			String datetimeValue = getChildNodeValue(datetimeElements);
			String datetimeAttrValue = getChildNodeAttrValue(datetimeElements,
					ConsoleConstants.TYPE);
			if (null != datetimeAttrValue
					&& !ConsoleConstants.EMPTY_STRING
							.equalsIgnoreCase(datetimeAttrValue)) {
				if (datetimeAttrValue.equalsIgnoreCase(ConsoleConstants.PRESET)) {
					searchBean.setPresetRendered(true);
					searchBean.setCustomRendered(false);
					searchBean.setAgeType(ConsoleConstants.PRESET);
					searchBean.setPresetValue(datetimeValue);
				} else {
					searchBean.setCustomRendered(true);
					searchBean.setPresetRendered(false);
					searchBean.setAgeType(ConsoleConstants.CUSTOM);
					int indexOf = datetimeValue.indexOf("@#%*");
					if (indexOf != -1) {
						DateFormat dateFormat = DateUtils
								.createDateFormat(ConsoleConstants.DEFAULT_DATE_PATTERN);
						try {
							Date ageValueFrom = dateFormat.parse(datetimeValue
									.substring(0, indexOf));
							searchBean.setAgeValueFrom(ageValueFrom);
						} catch (ParseException e) {
							// Ignore parse exception for null date value
						}
						try {
							Date ageValueTo = dateFormat.parse(datetimeValue
									.substring(indexOf + 4,
											datetimeValue.length()));
							searchBean.setAgeValueTo(ageValueTo);
						} catch (ParseException e) {
							// Ignore parse exception for null date value
						}

					}
				}

			}
		} catch (Exception e) {
			// For now ignore the exception
			logger.debug("Failed to get search bean  from XML ");
		}
		return searchBean;

	}

	/**
	 * @param nodeList
	 * @return ChildNodeValue
	 */
	private String getChildNodeValue(NodeList nodeList) {
		Element lstElement = (Element) nodeList.item(0);
		NodeList childNodes = lstElement.getChildNodes();
		if (null == childNodes.item(0)) {
			return ConsoleConstants.EMPTY_STRING;
		}
		return childNodes.item(0).getNodeValue();
	}

	/**
	 * 
	 * @param nodeList
	 * @param attribute
	 * @return String
	 */
	private String getChildNodeAttrValue(NodeList nodeList, String attribute) {
		Element lstElement = (Element) nodeList.item(0);
		return lstElement.getAttribute(attribute);
	}

	/**
	 * @param userId
	 * @param tabLabel
	 * @param tabId
	 */
	private void removeTabHistory(String userId, String tabLabel, String tabId) {
		try {

			navigationHelper.deleteHistoryRecord(userId,
					Integer.parseInt(tabId), ConsoleConstants.APP_TYPE_CONSOLE);
		} catch (NumberFormatException numberFormatException) {
			// ignore
			logger.debug("NumberFormatException : tabId seems to be incorrect ["
					+ tabId + "]");
		} catch (IOException ioException) {
			// ignore
			logger.warn("Failed to remove/delete node navigation information from database for user with Id ["
					+ userId + "].  for the tab [" + tabLabel + "]");
		} catch (Exception exception) {
			// ignore
			logger.warn("Failed to remove/delete node navigation information from database for user with Id ["
					+ userId + "].  for the tab [" + tabLabel + "]");
		}
	}

	/**
	 * add default tab
	 * 
	 * @param userId
	 */
	private void addDefaultTab(String userId) {
		Tab toAddDefault = new Tab(
				ResourceUtils
						.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_DEFAULT));
		toAddDefault
				.setHiddenLabel(ResourceUtils
						.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_DEFAULT));
		toAddDefault.setTabId(Tab.SEARCH_PANELID_PREFIX + tabMaxIndexID);
		toAddDefault.setRendered(true);
		tabs.add(toAddDefault);

		// store selected node (navigation history) into database as per
		// the
		// user id
		try {
			navigationHelper
					.addHistoryRecord(
							userId,
							tabMaxIndexID,
							ResourceUtils
									.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_DEFAULT),
							ConsoleConstants.EMPTY_STRING,
							null,
							ResourceUtils
									.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_DEFAULT),
							ConsoleConstants.APP_TYPE_CONSOLE);
		} catch (NumberFormatException e1) {
			// ignore
			logger.debug("NumberFormatException : nodeId seems to be incorrect ["
					+ tabMaxIndexID + "]");
		} catch (IOException e2) {
			// ignore
			logger.warn("Failed to add node navigation information into database for user with Id ["
					+ userId
					+ "]. Node name for the tab ["
					+ ResourceUtils
							.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_NEW)
					+ "]");
		}
	}

	/**
	 * Returns the navigationHelper.
	 * 
	 * @return the navigationHelper
	 */
	public NavigationHelper getNavigationHelper() {
		return navigationHelper;
	}

	/**
	 * Sets the navigationHelper.
	 * 
	 * @param navigationHelper
	 *            the navigationHelper to set
	 */
	public void setNavigationHelper(NavigationHelper navigationHelper) {
		this.navigationHelper = navigationHelper;
	}

	/**
	 * Returns the userId.
	 * 
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Sets the userId.
	 * 
	 * @param userId
	 *            the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * Sets the tabMaxIndexID.
	 * 
	 * @param tabMaxIndexID
	 *            the tabMaxIndexID to set
	 */
	public void setTabMaxIndexID(int tabMaxIndexID) {
		this.tabMaxIndexID = tabMaxIndexID;
	}

	/**
	 * Returns the tabMaxIndexID.
	 * 
	 * @return the tabMaxIndexID
	 */
	public int getTabMaxIndexID() {
		return tabMaxIndexID;
	}

	/**
	 * setting current tab filter
	 */
	private void initializeTabFilter() {
		Tab tab = tabs.get(tabIndex);
		ConsoleManager mgr = ConsoleHelper.getConsoleManager();
		mgr.populateData(tab, tab.getSearchCriteria());
	}

	/**
	 * Sets the hiddenField.
	 * 
	 * @param hiddenField
	 *            the hiddenField to set
	 */
	public void setHiddenField(String hiddenField) {
		this.hiddenField = hiddenField;
	}

	/**
	 * Returns the hiddenField.
	 * 
	 * @return the hiddenField
	 */
	public String getHiddenField() {
		if (tabFilterRender) {
			this.initializeTabFilter();
			tabFilterRender = false;
		}
		return hiddenField;
	}

	/**
	 * Sets the tabSize.
	 * 
	 * @param tabSize
	 *            the tabSize to set
	 */
	public void setTabSize(int tabSize) {
		this.tabSize = tabSize;
	}

	/**
	 * Returns the tabSize.
	 * 
	 * @return the tabSize
	 */
	public int getTabSize() {
		return tabs.size();
	}

	/**
	 * Sets the outputMedia.
	 * 
	 * @param outputMedia
	 *            the outputMedia to set
	 */
	public void setOutputMedia(OutputMedia outputMedia) {
		this.outputMedia = outputMedia;
	}

	/**
	 * Returns the outputMedia.
	 * 
	 * @return the outputMedia
	 */
	public OutputMedia getOutputMedia() {
		return outputMedia;
	}

	/**
	 * Sets the sourceMedia.
	 * 
	 * @param sourceMedia
	 *            the sourceMedia to set
	 */
	public void setSourceMedia(String sourceMedia) {
		this.sourceMedia = sourceMedia;
	}

	/**
	 * Returns the sourceMedia.
	 * 
	 * @return the sourceMedia
	 */
	public String getSourceMedia() {
		return sourceMedia;
	}

	/**
	 * Sets the autoStart.
	 * 
	 * @param autoStart
	 *            the autoStart to set
	 */
	public void setAutoStart(int autoStart) {
		this.autoStart = autoStart;
	}

	/**
	 * Returns the autoStart.
	 * 
	 * @return the autoStart
	 */
	public int getAutoStart() {
		return autoStart;
	}

	/**
	 * Sets the alarmSeverity.
	 * 
	 * @param alarmSeverity
	 *            the alarmSeverity to set
	 */
	public void setAlarmSeverity(String alarmSeverity) {
		this.alarmSeverity = alarmSeverity;
	}

	/**
	 * Returns the alarmSeverity.
	 * 
	 * @return the alarmSeverity
	 */
	public String getAlarmSeverity() {
		return alarmSeverity;
	}

	/**
	 * Sets the mediaLoop.
	 * 
	 * @param mediaLoop
	 *            the mediaLoop to set
	 */
	public void setMediaLoop(int mediaLoop) {
		this.mediaLoop = mediaLoop;
	}

	/**
	 * Returns the mediaLoop.
	 * 
	 * @return the mediaLoop
	 */
	public int getMediaLoop() {
		return mediaLoop;
	}

	/**
	 * Sets the silenceAlarm.
	 * 
	 * @param silenceAlarm
	 *            the silenceAlarm to set
	 */
	public void setSilenceAlarm(boolean silenceAlarm) {
		this.silenceAlarm = silenceAlarm;
	}

	/**
	 * Returns the silenceAlarm.
	 * 
	 * @return the silenceAlarm
	 */
	public boolean isSilenceAlarm() {
		return silenceAlarm;
	}

	/**
	 * Returns the enableAlarm.
	 * 
	 * @return the enableAlarm
	 */
	public boolean isEnableAlarm() {
		return enableAlarm;
	}

	/**
	 * Sets the enableAlarm.
	 * 
	 * @param enableAlarm
	 *            the enableAlarm to set
	 */
	public void setEnableAlarm(boolean enableAlarm) {
		this.enableAlarm = enableAlarm;
	}

}

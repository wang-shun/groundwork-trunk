package com.groundworkopensource.webapp.console;

/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")  
 * All rights reserved. This program is free software; you can redistribute it
 * and/or modify it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.faces.event.ActionEvent;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.Filter;

import com.icesoft.faces.async.render.OnDemandRenderer;
import com.icesoft.faces.async.render.RenderManager;
import com.icesoft.faces.async.render.Renderable;
import com.icesoft.faces.component.ext.HtmlDataTable;
import com.icesoft.faces.component.paneltabset.PanelTabSet;
import com.icesoft.faces.component.paneltabset.TabChangeEvent;
import com.icesoft.faces.webapp.xmlhttp.PersistentFacesState;
import com.icesoft.faces.webapp.xmlhttp.RenderingException;

public class TabsetBean implements javax.jms.MessageListener, Renderable {
	private static final int NUMBER_OF_TABS = 2;
	private static Logger logger = Logger.getLogger(TabsetBean.class.getName());

	private List<Tab> tabs = new ArrayList<Tab>(NUMBER_OF_TABS);
	private PersistentFacesState state;
	private RenderManager renderManager;
	private OnDemandRenderer renderer;
	private FilterBean filterBean = null;
	private Connection connection = null;
	private int tabIndex;

	public TabsetBean() {
		Tab toAddDefault = new Tab(ResourceUtils.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_DEFAULT));
		toAddDefault.setTabId(Tab.SEARCH_PANELID_PREFIX + tabs.size() + 1);
		toAddDefault.setRendered(true);
		
		tabs.add(toAddDefault);
		Tab toAddNew = new Tab(ResourceUtils.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_NEW));
		toAddNew.setTabId(Tab.SEARCH_PANELID_PREFIX + tabs.size() + 1);
		tabs.add(toAddNew);
		toAddNew.setRendered(false);
		initJMS();
		state = PersistentFacesState.getInstance();
		filterBean = ConsoleHelper.getFilterBean();
	}

	/**
	 * Gets the faces state for the server initiated rendering.
	 */
	public PersistentFacesState getState() {
		return state;

	}

	/**
	 * Implementation for the renderable for server initiated rendering.
	 */
	public void renderingException(RenderingException renderingException) {
		System.out.println(renderingException.getMessage());
		logger.error(renderingException);
		if (renderer != null) {
			renderer.remove(this);
			renderer = null;
		}

	}

	/**
	 * Gets the onDemand renderer
	 * 
	 * @return
	 */
	public OnDemandRenderer getRenderer() {
		return renderer;
	}

	/**
	 * Sets the renderManger.This is done thru the faces-config.xml
	 * 
	 * @param renderManager
	 */
	public void setRenderManager(RenderManager renderManager) {
		this.renderManager = renderManager;
		renderer = renderManager.getOnDemandRenderer(ConsoleConstants.RENDERER);
		renderer.add(this);
	}

	/**
	 * Initializes the JMS connections. Subscribes the datatableBean to the
	 * topic
	 */
	private void initJMS() {
		try {
			// Obtain a JNDI connection
			Properties env = new Properties();
			// ... specify the JNDI properties specific to the vendor
			env.put("java.naming.factory.initial", PropertyUtils
					.getProperty(ConsoleConstants.PROP_FACTORY_INIT));
			env.put("java.naming.factory.host", PropertyUtils
					.getProperty(ConsoleConstants.PROP_FACTORY_HOST));
			env.put("java.naming.factory.port", PropertyUtils
					.getProperty(ConsoleConstants.PROP_FACTORY_PORT));
			// filterBean = ConsoleHelper.getFilterBean();
			InitialContext jndi = new InitialContext(env);

			// Look up a JMS connection factory
			ConnectionFactory conFactory = (ConnectionFactory) jndi
					.lookup(PropertyUtils
							.getProperty(ConsoleConstants.PROP_CONTEXT_FACTORY));

			// Create a JMS connection
			connection = conFactory.createConnection();

			// Create JMS session objects

			Session subSession = connection.createSession(false,
					Session.CLIENT_ACKNOWLEDGE);

			// Look up a JMS topic
			Topic topic = (Topic) jndi.lookup(PropertyUtils
					.getProperty(ConsoleConstants.PROP_TOPIC_NAME));

			MessageConsumer subscriber = subSession.createConsumer(topic);

			// Set a JMS message listener
			subscriber.setMessageListener(this);
			connection.start();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * Receive message from topic subscriber TODO: initialize the page to show
	 * the new events
	 */
	public void onMessage(Message message) {
		logger.debug("Enter onMessage method");
		try {
			TextMessage textMessage = (TextMessage) message;
			String text = textMessage.getText();
			logger.debug(text);
			logger.debug("Updating the active tab-"
					+ tabIndex);
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
			EventBean[] events = (EventBean[]) eventList
					.toArray(new EventBean[eventList.size()]);
			dataTableBean.setEvents(events);
			logger.debug("Unpause mode for incoming events..");
			renderer.requestRender();
		} catch (JMSException exc) {
			logger.error(exc.getMessage());
		} // end try/catch

	}

	public List<Tab> getTabs() {
		return tabs;
	}

	public void setTabs(List<Tab> tabs) {
		this.tabs = tabs;
	}

	/**
	 * Listener for tabSelection
	 * 
	 * @param e
	 */
	public void tabSelection(TabChangeEvent e) {
		logger.debug("Enter tabSelection method");
	
		Tab tab = tabs.get(tabIndex);

		String tabLabel = tab.getLabel(); 

		if (tabLabel.equals(ResourceUtils.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_NEW))) {
			Tab toAddNew = new Tab(ResourceUtils.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_NEW));
			toAddNew.setTabId(Tab.SEARCH_PANELID_PREFIX + tabs.size() + 1);
			this.addTab(toAddNew);
			tab.setLabel(ResourceUtils.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_DEFAULT));
			tab.setHiddenLabel(ResourceUtils.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_DEFAULT));
			filterBean.setFilter(null);
			//Tab prevTab = tabs.get(selectedIndex - 1);
			Tab prevTab = tabs.get(tabIndex - 1);
			DataTableBean prevDataTable = prevTab.getDataTableBean();
			prevDataTable.setDynamicColumns(null);
			prevDataTable.constructComponent();
			DataTableBean dataBean = new DataTableBean();
			tab.setDataTableBean(dataBean);
			tab.setRendered(true);
			tab.setSearchCriteria(new SearchBean());
		} else {
			int filterNameIndex = tabLabel.indexOf("=");
			String filterStr = tabLabel.substring(filterNameIndex + 1, tabLabel
					.length());
			logger.debug("Filter=" + filterStr + "--FilterType="
					+ tab.getFilterType());
			tab.getActionBean().reset();
			this.populateData(tab.getFilterType(), filterStr);

			FreezeTableBean freezeBean = tab.getFreezeBean();
			if (freezeBean.getFreezeButtonText().equalsIgnoreCase(
					ResourceUtils.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_RESUME_EVENTS))) 
				freezeBean.reset();
			//dynamicTabSet.setSelectedIndex(selectedIndex);
		} // end if
		logger.debug("Exit tabSelection method");
	}

	/**
	 * Adds a new tab
	 * 
	 * @param tab
	 */
	public void addTab(Tab tab) {
		tab.setTabId(Tab.SEARCH_PANELID_PREFIX + tabs.size() + 1);
		tabs.add(tab);
		
	}

	/**
	 * Populates the data.
	 * 
	 * @param filterType
	 * @param filter
	 */
	private void populateData(String filterType, String filter) {
		ConsoleManager mgr = ConsoleHelper.getConsoleManager();
		SearchBean searchBean = tabs.get(
				this.tabIndex).getSearchCriteria();
		Filter searchFilter = ConsoleHelper
				.createSearchFilter(searchBean, null);
		if (filterType != null) {
			ConsoleHelper.hightlightNode(filterType);
			if (filterType.equals(ConsoleConstants.SYSTEM_FILTER_HOST_GROUPS)) {
				mgr.populateEventsByHostGroup(filter, searchFilter);
			} else if (filterType
					.equals(ConsoleConstants.SYSTEM_FILTER_APPLICATIONS)) {
				mgr.populateEventsByApplicationType(filter, searchFilter);
			} else if (filterType
					.equals(ConsoleConstants.SYSTEM_FILTER_OPERATION_STATUS)) {
				mgr.populateEventsByOperationStatus(filter, searchFilter);
			} else {
				PublicFiltersConfigBean configBean = ConsoleHelper
						.getPublicFilters();
				Vector<FilterConfigBean> filters = configBean
						.getFilterConfigs();
				for (int i = 0; i < filters.size(); i++) {
					FilterConfigBean filterConfig = filters.get(i);
					if (filter.equalsIgnoreCase(filterConfig.getLabel())) {
						mgr.populateEventsByCombinedFilters(filterConfig,
								searchFilter);
						ConsoleHelper.hightlightNode(filter);
						break;
					} // end if
				} // end for
			}
		} else {
			mgr.populateAllOpenEvents(searchFilter);
		} // end if
	}

	/**
	 * Closes the tab
	 * 
	 * @param e
	 */
	public void closeTab(ActionEvent e) {
		logger.debug("Close Clicked");
		
		if ((isAtleastOneNewTabDown() || isAtleastOneNewTabUp())
				&& tabs.size() > 2) {
			tabs.remove(tabIndex);
			
			 // try and fine a valid index
            if (tabIndex > 0) {
                tabIndex--;
            }
		} // end if
	}

	/**
	 * Checks if there is any new tab down the selected tab
	 * 
	 * @return
	 */
	public boolean isAtleastOneNewTabDown() {
		boolean result = false;
		for (int i = 0; i < tabIndex; i++) {
			if (tabs.get(i).getLabel().equals(ResourceUtils.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_NEW))) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * Checks if there is any new tab up the selected tab
	 * 
	 * @return
	 */
	public boolean isAtleastOneNewTabUp() {
		boolean result = false;
		if (tabIndex == tabs.size() - 1)
			return false;
		for (int i = tabIndex; i <= tabs.size() - 1; i++) {
			if (tabs.get(i).getLabel().equals(ResourceUtils.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_NEW))) {
				result = true;
				break;
			} // end if
		} // end for
		return result;
	}

	
	/**
	 * Stops the topic connection
	 */
	public void stopTopicConnection() {
		try {
			connection.stop();
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
			connection.start();
			logger.debug("Topic connection started...");
		} catch (Exception exc) {
			logger.error(exc.getMessage());
		} // end try/catch block
	}

	public int getTabIndex() {
		return tabIndex;
	}

	public void setTabIndex(int tabIndex) {
		this.tabIndex = tabIndex;
	}

}

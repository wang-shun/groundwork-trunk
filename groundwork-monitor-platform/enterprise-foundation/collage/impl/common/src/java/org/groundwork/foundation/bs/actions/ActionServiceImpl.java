/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

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
package org.groundwork.foundation.bs.actions;

import com.groundwork.collage.model.Action;
import com.groundwork.collage.model.ActionType;
import com.groundwork.collage.model.ApplicationType;
import com.groundwork.collage.model.impl.ActionPerform;
import com.groundwork.collage.model.impl.ActionReturn;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.EntityBusinessServiceImpl;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationDAO;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ActionServiceImpl extends EntityBusinessServiceImpl implements ActionService, RejectedExecutionHandler
{	
	// Configuration Property Keys
	private static final String PROP_CORE_POOL_SIZE = "fas.executor.core.pool.size";
	private static final String PROP_MAX_POOL_SIZE = "fas.executor.max.pool.size";
	private static final String PROP_QUEUE_SIZE = "fas.executor.queue.size";
	private static final String PROP_KEEP_ALIVE = "fas.executor.keep.alive";	
	private static final String PROP_INTERRUPT = "fas.executor.interrupt";
	private static final String ACKNOWLEDGE_LOG_MESSAGE="Acknowledge Log Message";
	/** Default Sort Criteria */
	private static final SortCriteria DEFAULT_SORT_CRITERIA = SortCriteria.asc(Action.HP_NAME);
	
	/*  Action Thread Pool */
	private int corePoolSize = 5;
	private int maxPoolSize = 25;
	private int queueSize = 100;
	private int keepAliveSeconds = 30;
	private long interruptSeconds = 2L;

	ThreadPoolExecutor executor = null;
	
	/** Enable Logging **/
	protected static Log log = LogFactory.getLog(ActionServiceImpl.class);
	
	public ActionServiceImpl(FoundationDAO foundationDAO, Properties configuration) 
	{
		super(foundationDAO, Action.INTERFACE_NAME, Action.COMPONENT_NAME);
		
		// Retrieve configuration values for service
		if (configuration != null)
		{
			String val = configuration.getProperty(PROP_INTERRUPT, "2");
			try 
			{
				interruptSeconds = Long.parseLong(val);
			} catch (NumberFormatException nfe)
			{
				log.warn("Invalid configuration property value for " + PROP_INTERRUPT + " = " + val);
			}
			


			val = configuration.getProperty(PROP_CORE_POOL_SIZE, "5");
			try 
			{
				corePoolSize = Integer.parseInt(val);
			} catch (NumberFormatException nfe)
			{
				log.warn("Invalid configuration property value for " + PROP_CORE_POOL_SIZE + " = " + val);
			}
			
			val = configuration.getProperty(PROP_MAX_POOL_SIZE, "25");
			try 
			{
				maxPoolSize = Integer.parseInt(val);
			} catch (NumberFormatException nfe)
			{
				log.warn("Invalid configuration property value for " + PROP_MAX_POOL_SIZE + " = " + val);
			}
			
			val = configuration.getProperty(PROP_QUEUE_SIZE, "5");
			try 
			{
				queueSize = Integer.parseInt(val);
			} catch (NumberFormatException nfe)
			{
				log.warn("Invalid configuration property value for " + PROP_QUEUE_SIZE + " = " + val);
			}
			
			val = configuration.getProperty(PROP_KEEP_ALIVE, "30");
			try 
			{
				keepAliveSeconds = Integer.parseInt(val);
			} catch (NumberFormatException nfe)
			{
				log.warn("Invalid configuration property value for " + PROP_KEEP_ALIVE + " = " + val);
			}
		}
	}
	
	public void initialize() throws BusinessServiceException 
	{
		// Create execution thread
		executor = new ThreadPoolExecutor(corePoolSize, 
										  maxPoolSize, 
										  keepAliveSeconds, 
										  TimeUnit.SECONDS, 
										  new ArrayBlockingQueue<Runnable>(queueSize, true), 
										  this);	
	}

	@Override
	public void uninitialize() throws BusinessServiceException
	{
		if (executor != null)
		{
			try {
				executor.shutdown();
				executor.awaitTermination(5000, TimeUnit.SECONDS);				
			}
			catch (Exception e)
			{
				log.error("Error shutting down ActionService Thread Pool.", e);
			}
			
		}
	}	
	
	// Query Methods
	public FoundationQueryList getActionsByCriteria(FilterCriteria filterCriteria,
											SortCriteria sortCriteria, 
											int firstResult, 
											int maxResults) 
	throws BusinessServiceException
	{
		return this.query(filterCriteria, sortCriteria, firstResult, maxResults);
	}
	
	public FoundationQueryList getActionByApplicationType(String appTypeName, boolean includeSystem) 
	throws BusinessServiceException
	{
		if (appTypeName == null || appTypeName.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty application type name parameter.");
		
		FilterCriteria filterCriteria = null;
		
		// Include system actions if specified.  Note, we make sure the app name specified is not SYSTEM 
		if ((ApplicationType.SYSTEM_APPLICATION_TYPE_NAME.equalsIgnoreCase(appTypeName) == false) && includeSystem == true)
		{
			filterCriteria = FilterCriteria.eq(Action.HP_APPLICATION_TYPE_NAME, ApplicationType.SYSTEM_APPLICATION_TYPE_NAME);
		}
				
		if (filterCriteria != null)
		{
			filterCriteria.or(FilterCriteria.eq(Action.HP_APPLICATION_TYPE_NAME, appTypeName));
			// Filterout Acknowledge Log Message. There is already one for Nagios
			if (("NAGIOS".equalsIgnoreCase(appTypeName) == true) && includeSystem == true) {
				FilterCriteria exceptFilter = FilterCriteria.ne(Action.HP_NAME, ACKNOWLEDGE_LOG_MESSAGE);
				filterCriteria.and(exceptFilter);
			}
		}
		else {
			filterCriteria = FilterCriteria.eq(Action.HP_APPLICATION_TYPE_NAME, appTypeName);
		}		
			
		return this.query(filterCriteria, DEFAULT_SORT_CRITERIA, -1, -1);
	}
	
	public Action getActionById (int id)
	{
		return (Action)this.queryById(id);
	}
		
	public FoundationQueryList getActionTypes(FilterCriteria filterCriteria, 
											SortCriteria sortCriteria, 
											int firstResult, 
											int maxResults)
	throws BusinessServiceException
	{
		return _foundationDAO.query(ActionType.COMPONENT_NAME, 
				filterCriteria, 
				SortCriteria.asc(ActionType.HP_NAME), 
				null, 
				-1, 
				-1);
	}
	
	// Action Methods
	
	/**
	 * Performs the specified actions.  Note:  An actionReturn is provided for each action specified.
	 * Note:  Actions are performed asynchronously.  This performActions method will return after all actions have
	 * been performed or timeout. 
	 */
	public List<ActionReturn> performActions (List<ActionPerform> actionPerforms) throws BusinessServiceException
	{
		if (actionPerforms == null || actionPerforms.size() == 0)
			throw new IllegalArgumentException("Invalid action perform list - no actions to perform.");
		
		if (executor == null || (executor.isTerminating() == true) || executor.isShutdown() == true)
		{
			throw new BusinessServiceException("Unable to execute actions b/c executor has been shutdown.");
		}
		
		// Perform each action
		Action action = null;
		ActionType actionType = null;
		ActionPerform actionPerform = null;

		List<ActionReturn> listReturns = new  ArrayList<ActionReturn>(actionPerforms.size());

		// we keep a two lists of return codes. Some actions might be interrupted/cancelled by the executor
		// due to a timeout value: PROP_INTERRUPT. Therefore, the callback is lost. Be creating a copy of
		// the initial actionIDs we restore the whole list by assigning them a default value of error for
		// those which fail due to some unknown reason.

		List<ActionReturn> listReturnsCopy = new  ArrayList<ActionReturn>(actionPerforms.size());
		Collection<Callable<ActionReturn>> tasks = new ArrayList<Callable<ActionReturn>>(actionPerforms.size());
		String msg2 = "Action failed to be executed";
		Iterator<ActionPerform> it = actionPerforms.iterator();
		while (it.hasNext())
		{
			actionPerform = it.next();

			// Lookup action by id
			action = getActionById(actionPerform.getActionId());
			if (action == null)
			{
				String msg = "Unable to execute action - Action not found with ID: " + actionPerform.getActionId(); 
				log.warn(msg);
				listReturns.add(new ActionReturn(action.getActionId(), ActionReturn.CODE_INTERNAL_ERROR, null));
				listReturnsCopy.add(new ActionReturn(action.getActionId(), ActionReturn.CODE_INTERNAL_ERROR, msg2));
				continue;
			}
			
			actionType = action.getActionType();
			if (actionType == null || actionType.getClassName() == null)
			{
				String msg = "Unable to execute action - Invalid ActionType - Null action type or no class name defined."; 
				log.warn(msg);
				listReturns.add(new ActionReturn(action.getActionId(), ActionReturn.CODE_INTERNAL_ERROR, msg));
				listReturnsCopy.add(new ActionReturn(action.getActionId(), ActionReturn.CODE_INTERNAL_ERROR, msg2));
				continue;
			}
			
			try {
				Class<FoundationAction> actionTypeClass = (Class<FoundationAction>)Class.forName(actionType.getClassName());
				FoundationAction actionInstance = actionTypeClass.newInstance();

				// Initialize action - We add the task even if initialization fails.  This allows us
				// to return an ActionReturn value for each task
				actionInstance.initialize(action, actionPerform.getParameters());
				tasks.add(actionInstance);

				// here we assign a defaul value of CODE_SUCCESS, for the case that the action fails, because 
				// on the Console a selected item must be deselected, which happens only when the return code is
				// CODE_SUCCESS. We do this for each actionID, just in case one of the action will be 
				// cancelled or interrupted by the executor, and we don't know exactly the reason:
				listReturnsCopy.add(new ActionReturn(action.getActionId(), ActionReturn.CODE_SUCCESS, msg2));
			}
			catch (Exception e)
			{
				String msg = "Unable to instanstiate or perform action."; 
				log.error(msg, e);
				listReturns.add(new ActionReturn(action.getActionId(), ActionReturn.CODE_INTERNAL_ERROR, msg));
				listReturnsCopy.add(new ActionReturn(action.getActionId(), ActionReturn.CODE_INTERNAL_ERROR, msg2));
			}
		}

		// Run tasks and get return values
		try {	
			int numberOfTasks = tasks.size();
			log.debug("Futures list length: ["+ numberOfTasks+"]");
			List<Future<ActionReturn>> futures = executor.invokeAll(tasks, interruptSeconds, TimeUnit.SECONDS);

			Iterator<Future<ActionReturn>> itFutures = futures.iterator();

			while (itFutures.hasNext())
			{
				Future<ActionReturn> fut = itFutures.next();
				log.debug("this future's state is (isCancelled?): ["+fut.isCancelled() +"]");
				if (!fut.isCancelled()) listReturns.add(fut.get());
				//listReturns.add(itFutures.next().get());
				log.debug("after listReturns.add (future) ");
			}
			log.debug("listReturns.size: ["+listReturns.size()+"]  listReturnsCopy.size: [" + listReturnsCopy.size()+"]");
			// replace for those actionIDs which succeded, the defaul values of error, with their 
			// real return codes and values. The new merged list should contain all actionIDs
			listReturns = returnActions (listReturnsCopy, listReturns, actionPerforms.size());
			log.debug("The merged listReturns.size: "+ listReturns.size());
		}
		catch (Exception e)
		{
			log.error("Unable to execute actions."+ e.toString());
			throw new BusinessServiceException("Unable to execute actions.", e);
		}

		if (log.isDebugEnabled())
		{
			log.debug("Final listReturns.size: "+ listReturns.size());
			for (ActionReturn a:listReturns) 
			log.debug("actionReturnID: ["+a.getActionId()+"]  actionReturnCode: ["+a.getReturnCode()+"]    actionReturnValue: ["+a.getReturnValue()+"]");
		}
		return listReturns;
	}
	
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) 
	{
		log.error("ActionService Executor - Cannot execute action task b/c all threads are active and the queue is full.");
	}	

	public  synchronized  List<ActionReturn> returnActions (List<ActionReturn> L1, List<ActionReturn> L2, int size)
	{
		// we keep two lists of Return Codes for the list of actions. Some actions might be
		// be interrupted/cancelled (due to the timeout value of the executor) which impacts the 
		// callback for those actions: the return code is lost. Therefore, we keep a default list of return
		// codes, with the value of error, which are replaced below with the successful ones (those
		// actions which finished execution)

		List<ActionReturn> listReturns = new  ArrayList<ActionReturn>(size);
		for (ActionReturn actReturn:L1)
		{
			ActionReturn a = getActionReturn(actReturn, L2);
			if (a != null)
				listReturns.add(a);  // a success replaces the default error
			else
				listReturns.add(actReturn);  // the default error stays

		}	
		return listReturns;

	}

	public ActionReturn getActionReturn(ActionReturn aRet, List<ActionReturn> L)
	{
			for (ActionReturn a:L)
			{
				if (aRet.getActionId ()==a.getActionId ())
					return a; // this is a successful action, its return code replaces
						  // the default in the listReturnsCopy
			}
			return null;
	}
}

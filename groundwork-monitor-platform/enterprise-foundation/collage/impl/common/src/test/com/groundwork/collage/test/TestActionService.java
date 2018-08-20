package com.groundwork.collage.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.groundwork.foundation.bs.actions.ActionService;
import org.groundwork.foundation.bs.logmessage.LogMessageService;
import org.groundwork.foundation.dao.FoundationQueryList;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.model.LogMessage;
import com.groundwork.collage.model.impl.ActionPerform;
import com.groundwork.collage.model.impl.ActionReturn;

public class TestActionService extends AbstractTestCaseWithTransactionSupport 
{
	private ActionService actionService = null;
	private LogMessageService logMsgService = null;
	
	public TestActionService(String x) {
		super(x);
	}

	/** define the tests to be run in this class */
	public static Test suite()
	{
		TestSuite suite = new TestSuite();

		executeScript(false, "testdata/monitor-data.sql");

		// run all tests
		suite = new TestSuite(TestActionService.class);

		// or a subset thereoff
		//suite.addTest(new TestCategoryService("testGetActions"));

		return suite;
	}

    public void setUp() throws Exception
    {
        super.setUp();
		
		// Retrieve action business service
		actionService = collage.getActionService();		
		assertNotNull(actionService);
		
		logMsgService = collage.getLogMessageService();
		assertNotNull(logMsgService);
	}	
	
	public void testGetActions()
	{	
		//beginTransaction();
		
		FoundationQueryList actions = actionService.getActionsByCriteria(null, null, -1, -1);
		assertNotNull(actions);
		assertEquals("Number of actions", 11, actions.size());
		
//		Iterator it = actions.getResults().iterator();
//		while (it.hasNext())
//		{
//			Action action = (Action)it.next();
//			
//			System.out.println("Action: " + action.getName() + ", ID: " + action.getActionId());
//			
//			System.out.println("------- Properties -----------");
//			Set props = action.getActionProperties();
//			Iterator itProps = props.iterator();
//			while (itProps.hasNext())
//			{
//				ActionProperty prop = (ActionProperty)itProps.next();
//				
//				System.out.println("Action Property: " + prop.getName() + ", ID: " + prop.getActionPropertyId());
//			}
//			
//			System.out.println("------- Application Types -----------");
//			Set appTypes = action.getApplicationTypes();
//			Iterator itApps = appTypes.iterator();
//			while (itApps.hasNext())
//			{
//				ApplicationType appType = (ApplicationType)itApps.next();
//				
//				System.out.println("App Type: " + appType.getName() + ", ID: " + appType.getApplicationTypeId());
//			}			
//		}
		
//		rollbackTransaction();
		
		actions = actionService.getActionByApplicationType("NAGIOS", true);
		assertNotNull(actions);
		assertEquals("Number of actions for NAGIOS", 5, actions.size());	
		
		actions = actionService.getActionByApplicationType("SYSTEM", true);
		assertNotNull(actions);
		assertEquals("Number of actions for SYSTEM", 5, actions.size());
	}
	
	public void testGetActionTypes ()
	{
		FoundationQueryList actionTypes = actionService.getActionTypes(null, null, -1, -1);
		assertNotNull(actionTypes);
		assertEquals("Number of action types", 5, actionTypes.size());		
	}
	
	public void testPerformActions ()
	{
		List<ActionPerform> actionPerforms = new ArrayList<ActionPerform>(2);
		
		// Query first three log messages
		FoundationQueryList results = logMsgService.getLogMessages(null, null, 0, 3);
		assertNotNull(results);
		assertEquals("Log Message Query", 3, results.size());	
		
		StringBuilder sbIds = new StringBuilder(16);
		sbIds.append(((LogMessage)results.get(0)).getLogMessageId().toString());
		sbIds.append(",");
		sbIds.append(((LogMessage)results.get(1)).getLogMessageId().toString());
		sbIds.append(",");
		sbIds.append(((LogMessage)results.get(2)).getLogMessageId().toString());		

		// Acknowledge nagios - Action ID: 1
		// Change log message operation status to CLOSED - Action ID: 3
		Map<String, String> parameters = new HashMap<String, String>(3);
		parameters.put("LogMessageIds", sbIds.toString());
        parameters.put("user_comment","unit-test1");
        parameters.put("UserName","unit-tester");
        parameters.put("user","unit-tester");
		
		actionPerforms.add(new ActionPerform(1, parameters));
		actionPerforms.add(new ActionPerform(3, parameters));
		
		List<ActionReturn> retVals = actionService.performActions(actionPerforms);
		assertNotNull(retVals);
		assertEquals("Number of action return values", 2, retVals.size());	
		
		Iterator<ActionReturn> it = retVals.iterator();
		while (it.hasNext())
		{
			ActionReturn ret = it.next();
			
			log.info("ActionReturn: " + ret.getActionId() 
					+ ", Code: " + ret.getReturnCode() 
					+ ", Value: " + ret.getReturnValue());
		}		
	}
}

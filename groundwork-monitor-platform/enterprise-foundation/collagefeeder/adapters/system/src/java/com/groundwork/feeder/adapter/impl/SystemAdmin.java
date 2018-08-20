package com.groundwork.feeder.adapter.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.CollageCommand;
import com.groundwork.collage.CollageFactory;
import com.groundwork.feeder.adapter.FeederBase;

public class SystemAdmin implements FeederBase {

	// Enable log for log4j
	private Log log = LogFactory.getLog(this.getClass());

	private static String ADAPTER_NAME = "SYSTEMADMIN";

	public String getName() {
		return ADAPTER_NAME;
	}

	public void initialize() {
		// TODO Auto-generated method stub

	}

	public void process(Object beanFactory, FoundationMessage message) 
	{
		// Get the CollageAdmin interface
		CollageAdminInfrastructure admin = (CollageAdminInfrastructure) 
			((CollageFactory) beanFactory).getAPIObject("com.groundwork.collage.CollageAdmin");
		
		if (admin == null) {
			// Interface not available throw an error
			log.error("CollageAdmin implementation not available -- Bean factory returned null. Make sure the collage-admin-impl jar is included in the path.");
			return;
		}

		if (message == null) {
			log.debug("SystemAdmin Adapter: Null FoundationMessage.");
			log.error("SystemAdmin Adapter: Null FoundationMessage.");
			return;
		}

		List<CollageCommand> commands = message.getCommands();
		if (commands == null || commands.size() == 0) {
			log.debug("SystemAdmin Adapter: No commands in xml "
							+ message);
			log.error("SystemAdmin Adapter: Could not find any commands in xml "
					+ message);
			return;
		}
		
		// Note:  We moved the logic to the CollageAdmin in order to complete the entire SystemAdmin message
		// in one transaction.
		admin.executeCommands(commands);
	}	
	
	public void uninitialize() 
	{
		// TODO Auto-generated method stub
	}
}

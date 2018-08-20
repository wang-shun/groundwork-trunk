package com.groundwork.report.birt.eventhandler;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.birt.report.engine.api.script.IReportContext;
import org.eclipse.birt.report.engine.api.script.ScriptException;
import org.eclipse.birt.report.engine.api.script.eventadapter.DataSetEventAdapter;
import org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance;

/**
 * Event handler for parameters of "Event History" table
 * 
 * @author nitin_jadhav
 * 
 */
public class EventHostListAdapter extends DataSetEventAdapter {

	/**
	 * Name of parameter which brings "user name"
	 */
	private static final String USER = "user";

	/**
	 * Logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * MSP_QUERY_HOSTGROUP
	 */
	private static final String QUERY_HOSTGROUP = "select 'ALL' as Name from HostGroup UNION (select hg.Name from HostGroup hg, ApplicationType appType where hg.ApplicationTypeID=appType.ApplicationTypeID and appType.Name= '<appType>' [and hg.Name IN (<list>)] order by Name);";

	/**
	 * MSP_QUERY_HOST
	 */
	private static final String QUERY_HOST = "select 'ALL' as HostName from Host UNION (select distinct HostName from Host,HostGroup,HostGroupCollection where Host.HostID = HostGroupCollection.HostID and HostGroup.HostGroupID = HostGroupCollection.HostGroupID and HostGroup.Name = '<hg>' order by HostName);";

	/**
	 * QUERY_HOSTGROUP
	 */
	private static final String MSP_QUERY_HOSTGROUP = "select hg.Name from HostGroup hg, ApplicationType appType where hg.ApplicationTypeID=appType.ApplicationTypeID and appType.Name= '<appType>' [and hg.Name IN (<list>)] order by Name;";

	/**
	 * QUERY_HOST
	 */
	private static final String MSP_QUERY_HOST = "select distinct HostName from Host,HostGroup,HostGroupCollection where Host.HostID = HostGroupCollection.HostID and HostGroup.HostGroupID = HostGroupCollection.HostGroupID and HostGroup.Name = '<hg>' order by HostName;";

	/**
	 * DEFAULT_QUERY_HOSTGROUP
	 */
	private static final String DEFAULT_MSP_QUERY_HOSTGROUP = "select distinct HostName from Host [,HostGroup,HostGroupCollection where Host.HostID = HostGroupCollection.HostID and HostGroup.HostGroupID = HostGroupCollection.HostGroupID and HostGroup.Name IN (<list>)] order by HostName;";

	/**
	 * DEFAULT_QUERY_HOST
	 */
	private static final String DEFAULT_MSP_QUERY_HOST = "select Name from HostGroup [where Name IN (<list>)] order by Name;";

	/**
	 * Event handler for datasets
	 */
	@Override
	public void beforeOpen(IDataSetInstance dataSet,
			IReportContext reportContext) {
		Object appTypeParam = reportContext
				.getParameterValue("Application Type");
		Object hgParam = reportContext.getParameterValue("Host Group");

		String userParam = ((HttpServletRequest) reportContext
				.getHttpServletRequest()).getParameter(USER);

		try {

			if (dataSet.getName().equals("HostGroupChooser")) {

				// Method is called for HostGroupChooser dataset

				if (appTypeParam == null || appTypeParam.equals("")) {
					if (ReportHelper.isMSPUser(reportContext))
						dataSet.setQueryText(ReportHelper
								.preProcessQuery(DEFAULT_MSP_QUERY_HOSTGROUP,reportContext));
					else
						dataSet.setQueryText(ReportHelper
								.preProcessQuery(dataSet.getQueryText(),reportContext));
				} else {
					if (ReportHelper.isMSPUser(reportContext))
						dataSet.setQueryText(ReportHelper
								.preProcessQuery(MSP_QUERY_HOSTGROUP.replace(
										"<appType>", appTypeParam.toString()),reportContext));
					else
						dataSet.setQueryText(ReportHelper
								.preProcessQuery(QUERY_HOSTGROUP.replace(
										"<appType>", appTypeParam.toString()),reportContext));

				}
			} else if (dataSet.getName().equals("HostChooser")) {

				// Method is called for HostChooser dataset

				if (hgParam == null || hgParam.equals("")) {
					if (ReportHelper.isMSPUser(reportContext))
						dataSet.setQueryText(ReportHelper
								.preProcessQuery(DEFAULT_MSP_QUERY_HOST,reportContext));
					else
						dataSet.setQueryText(ReportHelper
								.preProcessQuery(dataSet.getQueryText(),reportContext));

				} else {
					if (ReportHelper.isMSPUser(reportContext))
						dataSet.setQueryText(MSP_QUERY_HOST.replace("<hg>",
								hgParam.toString()));
					else
						dataSet.setQueryText(QUERY_HOST.replace("<hg>",
								hgParam.toString()));
				}
			}

		} catch (IOException e) {
			logger.severe("Error reading data from database: " + e.getMessage());
		} catch (ScriptException e) {
			logger.severe("ScriptException occured while reading data from database: "
					+ e.getMessage());
		}
	}
}

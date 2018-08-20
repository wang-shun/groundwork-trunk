package com.groundwork.report.birt.eventhandler;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.birt.report.engine.api.script.IReportContext;
import org.eclipse.birt.report.engine.api.script.ScriptException;
import org.eclipse.birt.report.engine.api.script.eventadapter.DataSetEventAdapter;
import org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance;
import org.hibernate.HibernateException;

/**
 * This class is event handler for scripted data source
 * 
 * @author nitin_jadhav
 * 
 */
public class HostGroupDataSetEventHandler extends DataSetEventAdapter {

    /**
     * Name of parameter which brings "user name"
     */
    private static final String USER = "user";
    /**
     * Logger
     */
    Logger logger = Logger.getLogger(this.getClass().getName());
    /**
     * Count for Host group rows for every role
     */
    int count = 0;

    /**
     * Fetches the hostgroup data from jbossportal database.
     * 
     * @see org.eclipse.birt.report.engine.api.script.eventadapter.DataSetEventAdapter#beforeOpen(org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance,
     *      org.eclipse.birt.report.engine.api.script.IReportContext)
     */

    @Override
    public void beforeOpen(IDataSetInstance dataSet,
            IReportContext reportContext) {

        HttpServletRequest request = (HttpServletRequest) reportContext
                .getHttpServletRequest();

        String userParam = request.getParameter(USER);

        try {
            String query = ReportHelper.preProcessQuery(dataSet.getQueryText(),reportContext);
            dataSet.setQueryText(query);

        } catch (HibernateException e1) {
            logger
                    .log(
                            Level.WARNING,
                            "Hibernate error while fetching role-based host groups from jbossportla database.");
        } catch (IOException e1) {
            logger
                    .log(Level.WARNING,
                            "IOException while fetching role-based host groups from jbossportla database.");

        } catch (ScriptException e) {
            logger.log(Level.WARNING, e.getMessage());
        }
        // super.beforeOpen(dataSet, reportContext);
    }

}


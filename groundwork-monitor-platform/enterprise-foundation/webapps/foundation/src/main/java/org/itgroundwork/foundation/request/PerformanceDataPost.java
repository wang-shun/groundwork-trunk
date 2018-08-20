/**
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@itgroundwork.com

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
package org.itgroundwork.foundation.request;
import java.io.FileInputStream;
import java.util.Properties;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.performancedata.PerformanceDataService;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.exception.CollageException;


/**
 * @author rogerrut
 *
 */
public class PerformanceDataPost extends HttpServlet {
	static final long serialVersionUID = 1;
	
    private Log log = LogFactory.getLog(this.getClass());
    
    // Used to get access to the beans
    private CollageFactory service = null;
    
    private PerformanceDataService performanceService = null;
    
    private String rollupInterval =null;
    
    public final void init( ServletConfig config ) throws ServletException
    {
    	super.init(config);
    	log.info("PerformanceDataPost: Init Servlet.");
    	
       	service = CollageFactory.getInstance();
       	performanceService = (PerformanceDataService)service.getPerformanceDataService();
       	
       	rollupInterval = getRollUp();
    }
    
    public final void init( HttpServletRequest request, HttpServletResponse response )
    {
        synchronized (PerformanceDataPost.class)
        {
         }
    }

    // -------------------------------------------------------------------
    // R E Q U E S T P R O C E S S I N G
    // -------------------------------------------------------------------

    /**
     * The primary method invoked when the Foundation servlet is executed.
     * 
     * @param req
     *            Servlet request.
     * @param res
     *            Servlet response.
     * @exception IOException
     *                a servlet exception.
     * @exception ServletException
     *                a servlet exception.
     */
    public final void doGet( HttpServletRequest req, HttpServletResponse res ) throws IOException, ServletException
    {
        try
        {
        	String cmd = req.getParameter("cmd");
        	if (cmd != null && cmd.equals("Update Data") )
        	{
        		PrintWriter out = res.getWriter();
        		updatePerformanceData(req);
        		out.println(performanceService.getPerformanceDataLabel());
        	}
        	else
        	if (cmd != null && cmd.equals("getperformancedata") )
        	{
        		PrintWriter out = res.getWriter();
        		out.println("<form method=\"POST\" action=\"\">"+performanceService.getPerformanceDataLabel()+"</form>");
        	}
        	else
        	insertPerformanceData(req, res);
        	
        }
        catch (Exception e)
        {
            final String msg = "Error while adding performance data: "+e.toString();
            log.fatal(msg, e);
            throw new ServletException(msg, e);
        }
    }

    /**
     * In this application doGet and doPost are the same thing.
     * 
     * @param req
     *            Servlet request.
     * @param res
     *            Servlet response.
     * @exception IOException
     *                a servlet exception.
     * @exception ServletException
     *                a servlet exception.
     */
    public final void doPost( HttpServletRequest req, HttpServletResponse res ) throws IOException, ServletException
    {
        doGet(req, res);
    }

    // -------------------------------------------------------------------
    // S E R V L E T S H U T D O W N
    // -------------------------------------------------------------------

    /**
     * The <code>Servlet</code> destroy method. Invokes
     * <code>ServiceBroker</code> tear down method.
     */
    public final void destroy()
    {
        log.info("PerformanceData Servlet: Done shutting down!");
    }

    public final String getRollUp()
    {
    	String rollup = "day";
		String configFile = System.getProperty("configuration","/usr/local/groundwork/config/foundation.properties");
		Properties configuration = new Properties();
		try 
		{
			FileInputStream fis = new FileInputStream(configFile);
			configuration.load(fis);
			rollup = configuration.getProperty("fp.data.rollup", "day").trim();
		} catch (Exception e) {log.warn("WARNING: Could not load foundation properties. Using default rollup = day");}
		return rollup;
    }
    
    
    
    public final void insertPerformanceData( HttpServletRequest req, HttpServletResponse res ) throws IOException, ServletException
    {
        try
        {
        	String hostName= req.getParameter("hostname");
        	String serviceDescription = req.getParameter("servicedescription");
        	String performanceDataLabel = req.getParameter("performancedatalabel");
        	String performanceValueStr = req.getParameter("performancevalue");
        	String checkDate = req.getParameter("checkdate");

        	double performanceValue = 0;
        	/* rollUp interval is set in INIT to read the properties file. If it is null use the default which is day */
        	String rollUp = rollupInterval;
        	if (rollUp == null )
        		rollUp = "day";
        	
        	if (log.isDebugEnabled())
        	log.debug("hostname="+hostName+"  serviceDesc="+serviceDescription+
        			"  perflabel="+performanceDataLabel+"  perfvalue="+performanceValueStr+"  checkDate="+checkDate+"  rollup="+rollUp+"<<");
        	
        	try{
        		performanceValue = new Double(performanceValueStr).doubleValue();
        	}catch (Exception e)
        	{
        		PrintWriter out = res.getWriter();
                out.println("<HTML><HEAD>Performance Servlet</HEAD><BODY><H1>Error: PerformanceValue parameter is not provided or is not a double.</BODY></HTML>");
                return;
        	}
        	
        	if (hostName == null || serviceDescription == null || performanceDataLabel == null || performanceValueStr == null || checkDate == null)
        	{
        	  	PrintWriter out = res.getWriter();
                out.println("<HTML><HEAD>Performance Servlet</HEAD><BODY><H1>Error: not all required values provided. Make sure that you define hostname, servicedescription, performancedatalabel, performancevalue and checkdate as parameters to this request.</BODY></HTML>");
        	}
        	
        	try
        	{
        	// Read the post parameters and call the DAO
        		if ( performanceService != null )
        		{
        			performanceService.createOrUpdatePerformanceData(hostName, serviceDescription, performanceDataLabel, performanceValue, checkDate, rollUp);
        		}
        		else
        		{
        			PrintWriter out = res.getWriter();
                    out.println("<HTML><HEAD>Performance Servlet</HEAD><BODY><H1>Error: Performance Data DAO not initialized</BODY></HTML>");
                    return;
        		}
            	PrintWriter out = res.getWriter();
                out.println("<HTML><HEAD>Performance Servlet</HEAD><BODY><H1>Performance Data Added</BODY></HTML>");

        	}
        	catch(CollageException ce)
        	{
               	PrintWriter out = res.getWriter();
                out.println("<HTML><HEAD>Performance Servlet</HEAD><BODY><H1>Error while adding data</H1><BR><P>" + ce + "</P></BODY></HTML>");

        	}
        }
        catch (Exception e)
        {
            final String msg = "Error while adding performance data: "+e.toString();
            log.fatal(msg, e);
            throw new ServletException(msg, e);
        }
    }
    
    

	public void updatePerformanceData(HttpServletRequest req)
	{
		String [] rows = req.getParameterValues("performanceDataLabelID");
		if (rows !=null && rows.length>0)
		for (int i=0;i<rows.length;i++)
		{
			int k = Integer.parseInt(rows[i]);
			performanceService.updatePerformanceDataLabelEntry (k, req.getParameter("serviceDisplayName"+"."+k), req.getParameter("metricLabel"+"."+k), req.getParameter("unit"+"."+k));
 
		}

	}

    
    
    
    
}

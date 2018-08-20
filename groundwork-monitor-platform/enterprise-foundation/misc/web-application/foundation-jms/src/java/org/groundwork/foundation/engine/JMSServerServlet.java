/*$Id: $
* Collage - The ultimate data integration framework.
*
* Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")  
* All rights reserved. This program is free software; you can redistribute it
* and/or modify it under the terms of the GNU General Public License version 2
* as published by the Free Software Foundation.
*
* This program is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
* FOR A PARTICULAR PURPOSE.  See the GNU General Public License for 
* more details.
*
* You should have received a copy of the GNU General Public License along with
* this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
* Street, Fifth Floor, Boston, MA 02110-1301, USA.
*
*/


/*Created on: Mar 28, 2006 */

package org.groundwork.foundation.engine;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.jms.FoundationJMSException;


/**
 * @author rogerrut
 * 
 * Foundation servlet that gets invoked at startup of the container.
 * It launches the port listeners and it initializes the hibernate
 * libraries.
 *
 */
public class JMSServerServlet extends HttpServlet
{	
	static final long serialVersionUID = 1;

	/** Location of Foundation Properties File **/
	/** TODO:  This location should be configurable **/
	private static final String FOUNDATION_PROPERTIES = "/usr/local/groundwork/config/foundation.properties";
	
	/** Enable logging **/
    private Log log = LogFactory.getLog(this.getClass());
    
    public static final String CONFIG_NAMESPACE = "org.groundwork.foundation";
    
    /**
     * In certain situations the init() method is called more than once,
     * sometimes even concurrently. This causes bad things to happen, so we use
     * this flag to prevent it.
     */
    private static boolean firstInit = true;

    /**
     * Whether init succeeded or not.
     */
    private static Throwable initFailure = null;
    
    /** Persistence Server instance */
    PersistentService persistentServer = null;
    
     
//  -------------------------------------------------------------------
    // I N I T I A L I Z A T I O N  M E S S A G E S
    // -------------------------------------------------------------------
    private static final String INIT_START_MSG = "[INFO] Foundation JMS Starting Initialization...";
    private static final String INIT_DONE_MSG = "[INFO] Foundation JMS Initialization complete, Ready to accept JMS messages.";
    
    
    /**
     * Initialize Servlet.
     */
    public final void init( ServletConfig config ) throws ServletException
    {
    	log.info("FoundationServlet: Init Servlet.");
    	
        synchronized (this.getClass())
        {
            log.info(INIT_START_MSG);

            super.init(config);

            if (!firstInit)
            {
                log.info("Double initialization of Foundation JMS was attempted!");
                 return;
            }
            // executing init will trigger some static initializers, so we have
            // only one chance.
            firstInit = false;

            try
            {
            	log.info("JMSServerServlet attempting to initialize the service...");
            	
                /**
                 * Start the Foundation JMS service
                 */
            	this.persistentServer = new PersistentService();
                
                // Check if agent is running
                if (persistentServer != null )
                {
                	try
                	{
                		System.out.println("Starting persistent server instance..");
                		
                		persistentServer.startPersistenceService(FOUNDATION_PROPERTIES);
                		
                		System.out.println("SUCCESS Starting persistent server instance..");
                		log.info("JMSServerServlet has successfuly initialized the JMS Server...");
                		log.info(INIT_DONE_MSG);
                	}
                	catch (FoundationJMSException fje)
                	{
                		System.out.println("Error Starting persistent server instance..");
                		log.error("Initialization of JMSServer failed");
                		final String msg = "Foundation JMS: init() failed: ";
                        log.fatal(msg, fje);
                	}                	                	
                }
                else
                {
                	log.error("Initialization of JMSServer failed");
                	final String msg = "Foundation JMS: init() failed: ";
                    log.fatal(msg, null);
                }                
                
            }
            catch (Throwable e)
            {
                // save the exception to complain loudly later :-)
                final String msg = "Foundation JMS: init() failed: ";
                initFailure = e;               
                log.fatal(msg, e);
            }
        }
    }

    /**
     * Initializes the services which need <code>RunData</code> to initialize
     * themselves (post startup).
     * 
     * @param data
     *            The first <code>GET</code> request.
     */
    public final void init( HttpServletRequest request, HttpServletResponse response )
    {
        synchronized (JMSServerServlet.class)
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
            // Check to make sure that we started up properly.
            if (initFailure != null)
            {
                throw new ServletException("Failed to initalize Foundation.  "+initFailure.toString(), initFailure);
            }
            
            /**
             * Just put a status on the screen
             */
            res.setContentType("text/html");
            PrintWriter out = res.getWriter();
            out.println("<HTML><HEAD>Foundation Engine</HEAD><BODY><H1>Foundation 1.5 Engine</H1><BR><P>Foundation platform is up and running and is accepting feeds.</P></BODY></HTML>");

        }
        catch (Exception e)
        {
            final String msg = "Fatal error encountered while processing portal request: "+e.toString();
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
        try
        {
        	// Clean shutdown
        	if (this.persistentServer == null)
        		log.info("Foundation Servlet shutdown. JMS Server not running.");
        	else
        	{
        		this.persistentServer.stopPersistenceService();
        		log.info("Foundation Servlet shutdown. Stop JMS Server.");        		
        	}
        }
        catch (Exception e)
        {
            log.fatal("Foundation: shutdown() failed: ", e);
            System.err.println(ExceptionUtils.getStackTrace(e));
        }

        // Allow turbine to be started back up again.
        firstInit = true;

        log.info("Foundation. Done shutting down!");
    }
    
    /**
     * Finds the specified servlet configuration/initialization
     * parameter, looking first for a servlet-specific parameter, then
     * for a global parameter, and using the provided default if not
     * found.
     */
    public static final String findInitParameter(ServletContext context,
                                                    ServletConfig config,
                                                    String name,
                                                    String defaultValue)
    {
        String path = null;

        // Try the name as provided first.
        boolean usingNamespace = name.startsWith(CONFIG_NAMESPACE);
        while (true)
        {
            path = config.getInitParameter(name);
            if (StringUtils.isEmpty(path))
            {
                path = context.getInitParameter(name);
                if (StringUtils.isEmpty(path))
                {
                    // The named parameter didn't yield a value.
                    if (usingNamespace)
                    {
                        path = defaultValue;
                    }
                    else
                    {
                        // Try again using Foundation's namespace.
                        name = CONFIG_NAMESPACE + '.' + name;
                        usingNamespace = true;
                        continue;
                    }
                }
            }
            break;
        }

        return path;
    }
}

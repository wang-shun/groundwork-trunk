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

/*Created on: Mar 28, 2006 */

package org.itgroundwork.foundation.engine;

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

import com.groundwork.feeder.service.ProcessFeederData;
import com.groundwork.feeder.service.PerfDataService;

/**
 * @author rogerrut
 * 
 *         Foundation servlet that gets invoked at startup of the container. It
 *         launches the port listeners and it initializes the hibernate
 *         libraries.
 * 
 */
public class FoundationServlet extends HttpServlet {

	static final long serialVersionUID = 1;

	private Log log = LogFactory.getLog(this.getClass());

	public static final String CONFIG_NAMESPACE = "org.itgroundwork.foundation";

	/**
	 * In certain situations the init() method is called more than once,
	 * somtimes even concurrently. This causes bad things to happen, so we use
	 * this flag to prevent it.
	 */
	private static boolean firstInit = true;

	/**
	 * Whether init succeeded or not.
	 */
	private static Throwable initFailure = null;

	/**
	 * Collage Factory
	 */
	private static ProcessFeederData listenerService = null;

	/**
	 * Collage Factory
	 */
	private static PerfDataService vemaPerfDataService = null;

	// -------------------------------------------------------------------
	// I N I T I A L I Z A T I O N M E S S A G E S
	// -------------------------------------------------------------------
	private static final String INIT_START_MSG = "[INFO] Foundation Starting Initialization...";
	private static final String INIT_DONE_MSG = "[INFO] Foundation Initialization complete, Ready to service requests.";

	/**
	 * Intialize Servlet.
	 */
	public final void init(ServletConfig config) throws ServletException {
		log.info("FoundationServlet: Init Servlet.");

		synchronized (this.getClass()) {
			log.info(INIT_START_MSG);

			super.init(config);

			if (!firstInit) {
				log.info("Double initialization of Foundation was attempted!");
				return;
			}
			// executing init will trigger some static initializers, so we have
			// only one chance.
			firstInit = false;

			try {
				log.info("FoundationServlet attempting to initialize the  servlet container...");

				/**
				 * Start the Foundation listener service
				 */
				listenerService = new ProcessFeederData();

				// Start listening and processing thread
				if (listenerService != null
						&& (listenerService.startProcessing() == true)) {
					log.info("FoundationServlet has successfuly initialized the portlet container...");
					log.info(INIT_DONE_MSG);
				} else {
					log.error("Initialization of listener framework failed");
					listenerService = null;
				}

				// Start the vema perfData service here. Constructor argument should match the type in the
				// foundation.properties file. For ex, VEMA scheduled to run every 30 secs
				vemaPerfDataService = new PerfDataService("perfdata.vema.jms.queue.name", 30, "perfdata.vema.writers");
				vemaPerfDataService.start();

			} catch (Throwable e) {
				// save the exception to complain loudly later :-)
				final String msg = "Foundation: init() failed: ";
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
	public final void init(HttpServletRequest request,
			HttpServletResponse response) {
		synchronized (FoundationServlet.class) {
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
	public final void doGet(HttpServletRequest req, HttpServletResponse res)
			throws IOException, ServletException {
		try {
			// Check to make sure that we started up properly.
			if (initFailure != null) {
				throw new ServletException("Failed to initalize Foundation.  "
						+ initFailure.toString(), initFailure);
			}

			/**
			 * Just put a status on the screen
			 */
			res.setContentType("text/html");
			PrintWriter out = res.getWriter();
			out.println("<HTML><HEAD>Foundation Engine</HEAD><BODY><H1>Foundation 1.5 Engine</H1><BR><P>Foundation platform is up and running and is accepting feeds.</P></BODY></HTML>");

		} catch (Exception e) {
			final String msg = "Fatal error encountered while processing portal request: "
					+ e.toString();
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
	public final void doPost(HttpServletRequest req, HttpServletResponse res)
			throws IOException, ServletException {
		doGet(req, res);
	}

	// -------------------------------------------------------------------
	// S E R V L E T S H U T D O W N
	// -------------------------------------------------------------------

	/**
	 * The <code>Servlet</code> destroy method. Invokes
	 * <code>ServiceBroker</code> tear down method.
	 */
	public final void destroy() {
		try {
			// Clean shutdown
			if (listenerService == null)
				log.info("Foundation Servlet shutdown. Listener not running.");
			else {
				log.info("Foundation Servlet shutdown. Stop running listener.");
				listenerService.unInitializeSystem();
			}

			if (vemaPerfDataService != null) {
				vemaPerfDataService.shutdown();
			}
		} catch (Exception e) {
			log.fatal("Foundation: shutdown() failed: ", e);
			System.err.println(ExceptionUtils.getStackTrace(e));
		}

		// Allow turbine to be started back up again.
		firstInit = true;

		log.info("Foundation. Done shutting down!");
	}

	/**
	 * Finds the specified servlet configuration/initialization parameter,
	 * looking first for a servlet-specific parameter, then for a global
	 * parameter, and using the provided default if not found.
	 */
	public static final String findInitParameter(ServletContext context,
			ServletConfig config, String name, String defaultValue) {
		String path = null;

		// Try the name as provided first.
		boolean usingNamespace = name.startsWith(CONFIG_NAMESPACE);
		while (true) {
			path = config.getInitParameter(name);
			if (StringUtils.isEmpty(path)) {
				path = context.getInitParameter(name);
				if (StringUtils.isEmpty(path)) {
					// The named parameter didn't yield a value.
					if (usingNamespace) {
						path = defaultValue;
					} else {
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

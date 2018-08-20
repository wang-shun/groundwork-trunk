package com.groundworkopensource.webapp.console;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

public class ConsoleFilter implements Filter {

	private static Logger logger = Logger.getLogger(ConsoleFilter.class
			.getName());

	public void destroy() {
		// TODO Auto-generated method stub

	}

	/**
	 * Intercepts all JSF/JSP calls. For 5.2.1 only one parameter is
	 * accepted.For ex, user=admin. Encryption is only for sessionid and not for
	 * logout
	 */
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		logger.debug("Enter doFilter method");
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		try {
			Object userObj = null;
			// Object userObj =
			// request.getParameter(ConsoleConstants.REQ_PARAM_USER);
			Object logOutObj = request
					.getParameter(ConsoleConstants.REQ_PARAM_LOGOUT);
			Object sessionId = request
					.getParameter(ConsoleConstants.REQ_PARAM_SESSIONID);
			if (sessionId != null) {
				String hex = (String) sessionId;
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < hex.length(); i = i + 2) {
					String tempStr = hex.substring(i, i + 2);
					int temp = Integer.parseInt(tempStr, 16);
					String aChar = new Character((char) temp).toString();
					sb.append(aChar);
				} // end for
				if (sb != null) {
					StringTokenizer stkn = new StringTokenizer(sb.toString(),
							"=");
					stkn.nextElement();
					userObj = stkn.nextElement();
				} // end if
			} // end if
			if (logOutObj != null) {
				logger.info("logOutObj is not null");
				String logOut = (String) logOutObj;
				if (logOut.equalsIgnoreCase(ConsoleConstants.LOGOUT_TRUE)) {
					logger.debug("logOut is true");
					HttpSession session = ((HttpServletRequest) request)
							.getSession();
					session.invalidate();
					logger.debug("Session made invalidate");

					httpResponse.sendRedirect("consoleLogout.html");
				} // end if
			} // end if
			else if (userObj != null) {
				String user = (String) userObj;
				logger.debug("User passed from guava is " + user);
				HttpSession session = ((HttpServletRequest) request)
						.getSession();
				Object sessionUserObj = session
						.getAttribute(ConsoleConstants.SESSION_LOGIN_USER);
				if (sessionUserObj == null) {
					logger.debug("User not in session.Adding user to session");
					session.setAttribute(ConsoleConstants.SESSION_LOGIN_USER,
							user);
				}
				chain.doFilter(request, response);

			} // end if
		} catch (Exception exc) {
			try {
				logger.error(exc.getMessage());
				httpRequest.getSession().setAttribute("error",exc.getMessage());
				httpResponse.sendRedirect("errorPage.jsp");
			} catch (IOException ioe) {
				logger.error(ioe.getMessage());
			} // end try/catch
		} // end try/catch
	} 

	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub

	}

}

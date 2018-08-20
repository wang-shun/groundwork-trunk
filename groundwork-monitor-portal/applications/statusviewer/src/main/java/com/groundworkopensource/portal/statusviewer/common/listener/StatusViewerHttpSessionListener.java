package com.groundworkopensource.portal.statusviewer.common.listener;
 
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.apache.log4j.Logger;
import javax.servlet.http.HttpSession;
import com.groundworkopensource.portal.statusviewer.bean.UserExtendedRoleBean;
import com.groundworkopensource.portal.statusviewer.common.Constant;

/**
 * Statusviewer http listener to debug memory leaks due to session bloating..
 * @author ArulShanmugam
 *
 */
public class StatusViewerHttpSessionListener implements HttpSessionListener {
	
	 /** Logger. */
    private static final Logger LOGGER = Logger
            .getLogger(StatusViewerHttpSessionListener.class.getName());
 
  private static int totalActiveSessions;
 
  public static int getTotalActiveSession(){
	return totalActiveSessions;
  }
 
  @Override
  public void sessionCreated(HttpSessionEvent se) {
	totalActiveSessions++;
	LOGGER.debug("sessionCreated - add one session into counter");
  }
 
  @Override
  public void sessionDestroyed(HttpSessionEvent se) {
	HttpSession session = se.getSession();
	totalActiveSessions--;
	LOGGER.info("sessionDestroyed - deduct one session from the following counter");
	LOGGER.info("Total active sessions==" + totalActiveSessions);
	LOGGER.info("MaxInactiveInterval ===>" + session.getMaxInactiveInterval() + " secs" );
	// Following is one example to check object in session
	UserExtendedRoleBean userSessionBean = (UserExtendedRoleBean) session.getAttribute(Constant.USER_EXTENDED_ROLE_BEAN);
	if (userSessionBean != null) {
		LOGGER.debug("UserExtendedRoleBean still exist in session!");
	}
  }	
}
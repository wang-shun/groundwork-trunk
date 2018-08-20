package com.groundworkopensource.portal.webui;

import java.util.Collections;
import java.util.Collection;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageService;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.webui.page.UIPage;
import org.exoplatform.portal.webui.page.UIPageBody;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.MembershipEntry;
import org.apache.log4j.Logger;

/**
 * GroundWorkUIAdminToolbarPortlet replaces default UIAdminToolbarPortlet in the
 * exoadmin. The links Dashboard Editor and Site Editor in the Toolbar
 * strip(which has Group, Dashboard links) are controlled in this portlet.
 * 
 * @author ArulShanmugam
 * 
 */
@ComponentConfig(lifecycle = UIApplicationLifecycle.class, template = "app:/groovy/Toolbar/GroundWorkUIAdminToolbarPortlet.gtmpl")
public class GroundWorkUIAdminToolbarPortlet extends UIPortletApplication {

	private static final Logger log = Logger
			.getLogger(GroundWorkUIAdminToolbarPortlet.class);

	private Identity guest = null;

	private final Collection<MembershipEntry> NO_MEMBERSHIP = Collections
			.emptyList();

	private final Collection<String> NO_ROLES = Collections.emptyList();

	public GroundWorkUIAdminToolbarPortlet() throws Exception {
		log.debug("Entering GroundWorkUIAdminToolbarPortlet constructor");
		guest = new Identity(null, NO_MEMBERSHIP, NO_ROLES);
	}

	public UserNavigation getSelectedNavigation() throws Exception {
		return Util.getUIPortal().getUserNavigation();
	}

	@Override
	public void processRender(WebuiApplication app, WebuiRequestContext context)
			throws Exception {
		log.debug("Entering GroundWorkUIAdminToolbarPortlet processRender...");
		// Allow dashboard/siteeditor link if the user is a superuser or the
		// page is dashboard
		if (isDashboard() || isSuperUser()) {
			super.processRender(app, context);
		}
		log.debug("Exiting GroundWorkUIAdminToolbarPortlet processRender..."
				+ isDashboard() + "=====" + isSuperUser());
	}

	/**
	 * Helper to get the login identity
	 */
	private Identity getIdentity() {
		ConversationState conv = ConversationState.getCurrent();
		if (conv == null) {
			return guest;
		}

		Identity id = conv.getIdentity();
		if (id == null) {
			return guest;
		}

		return id;
	}

	/**
	 * Checks if the page is a dashboardpage
	 */
	private boolean isDashboard() throws Exception {
		UserNavigation selectedNavigation = getSelectedNavigation();
		String pageName = selectedNavigation.getKey().getTypeName();
		if (pageName != null && pageName.equalsIgnoreCase("user"))
			return true;
		else
			return false;
	}

	/**
	 * Checks user is a superuser.User name is case sensitive here
	 */
	private boolean isSuperUser() throws Exception {
		UIPortalApplication portalApp = Util.getUIPortalApplication();
		UserACL userACL = portalApp.getApplicationComponent(UserACL.class);
		Identity identity = getIdentity();
		if (userACL.getSuperUser() != null
				&& userACL.getSuperUser()
						.equalsIgnoreCase(identity.getUserId()))
			return true;
		else
			return false;
	}

}
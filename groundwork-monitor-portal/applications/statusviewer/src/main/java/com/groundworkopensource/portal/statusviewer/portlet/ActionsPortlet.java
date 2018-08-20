package com.groundworkopensource.portal.statusviewer.portlet;

import static com.groundworkopensource.portal.statusviewer.common.Constant.ACTIONS_VIEW_PATH;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.groundworkopensource.portal.common.BasePortlet;

/**
 * This portlet performs the selected actions depending on the context selected
 * (Host,HostGroup,Service,ServiceGroup)
 * 
 * @author shivangi_walvekar
 * 
 */
public class ActionsPortlet extends BasePortlet {

    /**
     * Constant String for portlet title
     */
    private static final String ACTIONS_PORTLET_TITLE = "Actions";

    /**
     * (non-Javadoc)
     * 
     * @see javax.portlet.GenericPortlet#processAction(javax.portlet.ActionRequest,
     *      javax.portlet.ActionResponse)
     */
    @Override
    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse) throws PortletException, IOException {

    }

    /**
     * (non-Javadoc)
     * 
     * @see javax.portlet.GenericPortlet#doView(javax.portlet.RenderRequest,
     *      javax.portlet.RenderResponse)
     */
    @Override
    protected void doView(RenderRequest renderRequest,
            RenderResponse renderResponse) throws PortletException, IOException {
        // Set the path of the jsp file to be displayed in VIEW mode.
        super.setViewPath(ACTIONS_VIEW_PATH);
        // Set the portlet title.
        renderResponse.setTitle(ACTIONS_PORTLET_TITLE);
        super.doView(renderRequest, renderResponse);

    }

}

/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package com.groundworkopensource.portal.statusviewer.handler;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.PreferenceConstants;
import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.common.eventbroker.ClientSocket;
import com.groundworkopensource.portal.common.eventbroker.NagiosCommandProcessor;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.GWPortalGenericException;
import com.groundworkopensource.portal.common.exception.PreferencesException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;
import com.groundworkopensource.portal.statusviewer.bean.CommentsBean;
import com.groundworkopensource.portal.statusviewer.bean.ServerPush;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.NodeType;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;
import com.groundworkopensource.portal.statusviewer.common.actions.CommandDescriptionConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.impl.WSClientConfiguration;
import org.groundwork.rs.client.CommentClient;
import org.groundwork.rs.client.HostClient;
import org.groundwork.rs.client.ServiceClient;
import org.groundwork.rs.dto.DtoComment;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoService;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * This class handles comments for hosts and services.
 * 
 * @author mridu_narang
 * 
 */

public class CommentsHandler extends ServerPush implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -8590178476236568926L;

    /**
     * Maximum allowable length of comment.
     */
    private static final int COMMENT_MAX_ALLOWABLE_LENGTH = 500;

    /**
     * boolean property for the attribute ' required' used for UI validation.
     */
    private boolean required;

    /**
     * 
     * @return required
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * 
     * @param required
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * Boolean variable indicating if the event broker server is listening for
     * nagios commands or not.
     */
    private boolean nagiosDown = false;

    /**
     * 
     * @return nagiosDown
     */
    public boolean isNagiosDown() {
        return nagiosDown;
    }

    /**
     * 
     * @param nagiosDown
     */
    public void setNagiosDown(boolean nagiosDown) {
        this.nagiosDown = nagiosDown;
    }

    /**
     * Logger
     */
    private static final Logger LOGGER = Logger.getLogger(CommentsHandler.class
            .getName());

    // Node Identifier Fields
    /**
     * Node Type of current sub-page
     */
    private NodeType nodeType;

    /**
     * Name of node that the current sub-page identifies
     */
    private String nodeName;

    /**
     * Node ID that the current sub-page identifies
     */
    private int nodeId;

    /**
     * Flag to identify if portlet is placed in StatusViewer sub-pages apart
     * from Network View.
     */
    private boolean inStatusViewer;

    /**
     * User Name of currently logged user
     */
    private String userName = "";

    // Exception Handling Fields
    /**
     * Error boolean to set if error occurred
     */
    private boolean error = false;

    /**
     * Error message to show on UI
     */
    private String errorMessage = "";

    // UI Related Fields
    /**
     * Backing bean field for data-table for comments
     */
    private List<CommentsBean> commentsList = new ArrayList<CommentsBean>();

    /**
     * Boolean field used to render add pop-up window.
     */
    private boolean addPopupVisible = false;

    /**
     * Boolean field used to render delete pop-up window.
     */
    private boolean deletePopupVisible = false;

    /**
     * Name of Host associated with host or service comment
     */
    private String hostName;

    /**
     * Field indicates whether the comment has to be persistent or not
     */
    private boolean persistent;

    /**
     * Comment to be added
     */
    private String inputComment;

    /**
     * Field indicates whether comments are for a host
     */
    private boolean hostComment;

    /**
     * Command Description
     */
    private String commandDescription;

    /**
     * Indicates if an empty comments list is returned
     */
    private boolean emptyList = false;

    /**
     * Comment ID selected for deletion
     */
    private String selectedCommentId;

    // Event-Broker Command Fields
    /**
     * Client socket to communicate with event broker
     */
    private static final ClientSocket SOCKET = new ClientSocket();

    // Constants in template to replace
    /**
     * Parameter for Byte-size of command
     */
    private static final String BYTE_SIZE = "<bytesize_cmd>";

    /**
     * Parameter for User name
     */
    private static final String USER_NAME = "<user_name>";

    /**
     * Parameter for Host Name associated with comment
     */
    private static final String HOST_NAME = "<host_name>";

    /**
     * Parameter for Persistent status of comment
     */
    private static final String PERSISTENT = "<persistent>";

    /**
     * Parameter for author
     */
    private static final String AUTHOR = "<author>";

    /**
     * Parameter for comment
     */
    private static final String COMMENT = "<comment>";

    /**
     * Parameter for service description
     */
    private static final String SERVICE_DESCRIPTION = "<service_description>";

    /**
     * Parameter for comment ID
     */
    private static final String COMMENT_ID = "<comment_id>";

    /**
     * Delimiter between comments
     */
    private static final String COMMENT_LEVEL_DELIMITER = "#!#";

    /**
     * Delimiter for comment fields
     */
    private static final String FIELD_LEVEL_DELIMITER = ";::;";

    /**
     * Allowable comment parameters
     */
    private static final int NO_OF_COMMENT_PARAMETERS = 4;

    /**
     * Comment ID Parameter
     */
    private static final String PARAM_COMMENT_ID = "commentID";

    /**
     * Regular Expression for detecting valid URL
     */
    private static final String URL_REGULAR_EXPRESSION = "(?:https?|ftps?)://[\\w/%.-]+";

    /**
     * foundationWSFacade Object to call web services.
     */
    private final IWSFacade foundationWSFacade = new WebServiceFactory()
            .getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);

    /**
     * Purposely added for debugging purpose. TODO Remove afterwards.
     */
    private String myName = null;

    /**
     * facesContext
     */
    private FacesContext facesContext;

    /**
     * String which sets the style to the DIV tag on commentsView.jspx
     */
    private String divTagStyle = Constant.DIV_TAG_STYLE_COMMENTS_HOST;

    /**
     * popup style
     */
    private String popupStyle;

    /**
     * SERVICE_COMMENT_ADD_POPUP_STYLE
     */
    private static final String SERVICE_COMMENT_ADD_POPUP_STYLE = "z-index:999; top: 25%; width: 500px; height: 200px; background: #FFFFFF;";

    /**
     * SERVICE_COMMENT_DELETE_POPUP_STYLE
     */
    private static final String SERVICE_COMMENT_DELETE_POPUP_STYLE = "z-index:999; top: 25%; width: 385px; height: 100px; background: #FFFFFF;";

    /**
     * HOST_COMMENT_ADD_POPUP_STYLE
     */
    private static final String HOST_COMMENT_ADD_POPUP_STYLE = "z-index:999; bottom: 25%; width: 500px; height: 200px; background: #FFFFFF;";

    /**
     * HOST_COMMENT_DELETE_POPUP_STYLE
     */
    private static final String HOST_COMMENT_DELETE_POPUP_STYLE = "z-index:999; bottom: 25%; width: 385px; height: 100px; background: #FFFFFF;";

    /**
     * Add Comment
     */
    private static final String ADD_COMMENT = "Add Comment";

    /**
     * Delete Comment
     */
    private static final String DELETE_COMMENT = "Delete Comment";
    /**
     * SubpageIntegrator
     */
    private SubpageIntegrator subpageIntegrator;

    /**
     * This title is used for the modal pop-up in case nagios/event broker is
     * down.
     */
    private String title;

    /**
     * 
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * 
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return divTagStyle
     */
    public String getDivTagStyle() {
        return divTagStyle;
    }

    /**
     * @param divTagStyle
     */
    public void setDivTagStyle(String divTagStyle) {
        this.divTagStyle = divTagStyle;
    }

    /**
     * This method sets the style of the DIV tag for the comments data table.The
     * style to be set depends on the current context (Host or Service). The
     * comments portlet in the Host view is wider than the one in Service page.
     */
    public void setDivTagStyle() {
        // Set the DIV tag style.
        if (nodeType == NodeType.SERVICE) {
            setDivTagStyle(Constant.DIV_TAG_STYLE_COMMENTS_SERVICE);
        } else if (nodeType == NodeType.HOST) {
            setDivTagStyle(Constant.DIV_TAG_STYLE_COMMENTS_HOST);
        }
    }

    /**
     * preferences Keys Map to be used for reading preferences.
     */
    private static final Map<String, NodeType> PREFERENCE_KEYS_MAP = new LinkedHashMap<String, NodeType>();
    static {
        PREFERENCE_KEYS_MAP.put(PreferenceConstants.DEFAULT_SERVICE_PREF,
                NodeType.SERVICE);
        PREFERENCE_KEYS_MAP.put(PreferenceConstants.DEFAULT_HOST_PREF,
                NodeType.HOST);
    }

    /**
     * List of deletedCommentIds.
     */
    // TODO Need to clear this at some time. Not necessary - but it will be
    // better if we do so.
    private List<String> deletedCommentIds = new ArrayList<String>();

    /**
     * 
     */
    private String hiddenField = Constant.HIDDEN;

    /**
     * Gets the hidden field.
     * 
     * @return the hidden field
     */
    public String getHiddenField() {
        if (subpageIntegrator.isInStatusViewer() && !isIntervalRender()) {
            // fetch the latest nav params
            subpageIntegrator.setNavigationParameters();
            // check for node type and node Id
            int nodeID = subpageIntegrator.getNodeID();
            NodeType newNodeType = subpageIntegrator.getNodeType();
            if (nodeID != nodeId || !newNodeType.equals(nodeType)) {
                // update node type vals
                nodeType = newNodeType;
                nodeName = subpageIntegrator.getNodeName();
                nodeId = nodeID;
                // subpage - update node type vals
                setIntervalRender(true);
            }
        }

        if (isIntervalRender()) {
            // Set the DIV tag style.
            setDivTagStyle();

            // Retrieve currently logged in user
            setUserName(FacesUtils.getLoggedInUser());

            // Retrieve comment list based on node state
            try {
                selectCommentsList();
            } catch (WSDataUnavailableException e) {
                handleError(
                        "com_groundwork_portal_statusviewer_commentsPortlet_error",
                        "CommentsHandler(): selectCommentsList() failed to initialize comments list. Web services data unavailable");
            } catch (GWPortalException e) {
                handleError(
                        "com_groundwork_portal_statusviewer_commentsPortlet_error",
                        "CommentsHandler(): selectCommentsList() failed to initialize comments list. Web services data unavailable");
            }
        }
        setIntervalRender(false);
        return hiddenField;
    }

    /**
     * @param hiddenField
     */
    public void setHiddenField(String hiddenField) {
        this.hiddenField = hiddenField;
    }

    /**
     * Default Constructor
     */
    public CommentsHandler() {
        // initialize the faces context to be used in JMS thread
        facesContext = FacesContext.getCurrentInstance();
        subpageIntegrator = new SubpageIntegrator();
        /*
         * Use state controller to retrieve and set node parameters - TYPE, ID,
         * NAME. Handle sub-page integration over here.
         */
        handleSubpageIntegration();

        // Set the DIV tag style.
        // setDivTagStyle();
        //
        // // Retrieve currently logged in user
        // setUserName(FacesUtils.getLoggedInUser());
        //
        // // Retrieve comment list based on node state
        // try {
        // selectCommentsList();
        // } catch (WSDataUnavailableException e) {
        // handleError(
        // "com_groundwork_portal_statusviewer_commentsPortlet_error",
        // "CommentsHandler(): selectCommentsList() failed to initialize comments list. Web services data unavailable"
        // );
        // } catch (GWPortalException e) {
        // handleError(
        // "com_groundwork_portal_statusviewer_commentsPortlet_error",
        // "CommentsHandler(): selectCommentsList() failed to initialize comments list. Web services data unavailable"
        // );
        // }
    }

    /**
     * Handles the sub-page integration: Reads parameters from request in case
     * of Status Viewer. If portlet is in dashboard, reads preferences.
     */
    private void handleSubpageIntegration() {

        boolean isPrefSet = subpageIntegrator
                .doSubpageIntegration(PREFERENCE_KEYS_MAP);
        if (!isPrefSet) {
            /*
             * As this portlet is not applicable for "Network View", show the
             * error message to user. If it was in the "Network View", then we
             * would have to assign Node Type as NETWORK with NodeId as 0.
             */
            String message = new PreferencesException().getMessage();
            setError(true);
            setErrorMessage(message);
            LOGGER.error(message);
            return;
        }
        // get the required data from SubpageIntegrator
        this.nodeType = subpageIntegrator.getNodeType();
        this.nodeId = subpageIntegrator.getNodeID();
        this.nodeName = subpageIntegrator.getNodeName();
        this.inStatusViewer = subpageIntegrator.isInStatusViewer();

        // for debugging purpose - to know which instance of comments handler is
        // in use.
        Random random = new Random();
        myName = "Comments_" + String.valueOf(random.nextInt()) + "_"
                + this.nodeType;

        // for debugging purpose
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(new StringBuilder("[Comments Portlet ").append(myName)
                    .append("] # Node Type [").append(this.nodeType).append(
                            "] # Node Name [").append(this.nodeName).append(
                            "] # Node ID [").append(this.nodeId).append(
                            "] # In Status Viewer [").append(
                            this.inStatusViewer).append("]"));
        }
    }

    /**
     * Method to decide for which sub-page the portlet is displayed
     * 
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    private void selectCommentsList() throws WSDataUnavailableException,
            GWPortalException {
        this.error = false;
        if (this.nodeType == null) {
            /*
             * Portlet cannot decide which sub-page its state is in - Cannot
             * recover
             */
            handleError(
                    "com_groundwork_portal_statusviewer_commentsPortlet_error",
                    "selectCommentsList(): Cannot instantiate comments portlet handler. Subpage node-type is null.");
            return;
        }

        if (this.nodeType == NodeType.HOST) {
            // Set host name parameter
            setHostName(this.nodeName);
            // Indicates this is a host comments configuration
            setHostComment(true);
            // Relay call to web service to retrieve host comments
            retrieveHostComments();

        } else if (this.nodeType == NodeType.SERVICE) {
            // Indicates this is a service comments configuration
            setHostComment(false);

            // Relay call to web service to retrieve service comments
            retrieveServicesComments();

        } else {
            // Portlet placed in wrong sub-page. Cannot recover.
            handleError(
                    "com_groundwork_portal_statusviewer_commentsPortlet_error",
                    "selectCommentsList(): Portlet received invalid sub-page node type. Only Host and Service entities are associated with comments portlet");
        }
    }

    // Web Service Retrieval Methods
    /**
     * Method to retrieve Host Comments for given Host
     * 
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    private void retrieveHostComments() {
        String deploymentUrl = WSClientConfiguration.getProperty(WSClientConfiguration.FOUNDATION_REST_ENDPOINT);
        HostClient hostClient = new HostClient(deploymentUrl);

        // Deep required to get comments
        DtoHost host = hostClient.lookup(this.nodeName, DtoDepthType.Deep);
        if (host == null) {
            // Host with given ID does not exist - Portlet cannot recover
            handleError(
                    "com_groundwork_portal_statusviewer_commentsPortlet_error",
                    "retrieveHostComments(): Host with subpage entity ID does not exist.");
            return;
        }

        // Set node id explicitly for dashboard
        this.nodeId = host.getId();

        this.commentsList.clear();
        List<DtoComment> comments = host.getComments();
        if (comments == null || comments.size() == 0) return;
        for (DtoComment dto : comments) {
            CommentsBean bean = new CommentsBean(dto.getNotes(), dto.getId().toString(), dto.getCreatedOn().toString(), dto.getAuthor(), false);
            this.commentsList.add(bean);
        }
    }

    /**
     * Method to retrieve Service Comments for given Service
     */
    private void retrieveServicesComments() {
        String deploymentUrl = WSClientConfiguration.getProperty(WSClientConfiguration.FOUNDATION_REST_ENDPOINT);
        ServiceClient serviceClient = new ServiceClient(deploymentUrl);

        try {
            DtoService service;
            if (this.inStatusViewer) {
                // retrieve service by using id
                service = serviceClient.lookup(nodeId);
            } else {
                if (facesContext != null) FacesUtils.setFacesContext(facesContext);
                // use preferences - host name and service name to get the service
                Map<String, String> servicePortletPreferences = PortletUtils
                        .getServicePortletPreferences();
                service = serviceClient.lookup(
                        servicePortletPreferences.get(PreferenceConstants.SERVICE_NAME),
                        servicePortletPreferences.get(PreferenceConstants.HOST_NAME), DtoDepthType.Deep);
            }

            if (service == null) {
                // Service with given ID does not exist - Portlet cannot recover
                handleError(
                        "com_groundwork_portal_statusviewer_commentsPortlet_error",
                        "retrieveServicesComments(): Service with subpage entity ID [received from state controller] does not exist.");
                return;
            }

            // Set node id explicitly for dashboard
            nodeId = service.getId();

            // Setting host associated with service
            setHostName(service.getHostName());

            this.commentsList.clear();
            List<DtoComment> comments = service.getComments();
            if (comments == null || comments.size() == 0) return;
            this.commentsList.clear();
            for (DtoComment dto : comments) {
                CommentsBean bean = new CommentsBean(dto.getNotes(), dto.getId().toString(), dto.getCreatedOn().toString(), dto.getAuthor(), false);
                this.commentsList.add(bean);
            }
        } catch (GWPortalGenericException e) {
            handleError(
                    "com_groundwork_portal_statusviewer_commentsPortlet_error",
                    "retrieveServicesComments(): Failed to retrieve service comments list : " + e.getMessage());
        }

    }

    /**
     * Method to process adding of a comment.
     * 
     * @param event
     */
    public void addComment(ActionEvent event) {
        if (!validate(facesContext, event)) {
            return;
        }

        String deploymentUrl = WSClientConfiguration.getProperty(WSClientConfiguration.FOUNDATION_REST_ENDPOINT);
        CommentClient commentClient = new CommentClient(deploymentUrl);

        // Relay call to addHostComment or addServiceComment
        if (isHostComment()) {
            HostClient hostClient = new HostClient(deploymentUrl);
            DtoHost dtoHost = hostClient.lookup(getHostName());
            if (StringUtils.equalsIgnoreCase(dtoHost.getAppType(), "nagios")) {
                NagiosCommandProcessor nagiosCommandProcessor = new NagiosCommandProcessor();
                nagiosCommandProcessor.addHostComment(getInputComment(), getHostName(), getUserName());
            } else {
                commentClient.addHostComment(nodeId, inputComment, userName);
            }
        } else {
            ServiceClient serviceClient = new ServiceClient(deploymentUrl);
            DtoService dtoService = serviceClient.lookup(nodeId);
            if (StringUtils.equalsIgnoreCase(dtoService.getAppType(), "nagios")) {
                NagiosCommandProcessor nagiosCommandProcessor = new NagiosCommandProcessor();
                nagiosCommandProcessor.addServiceComment(getInputComment(), getHostName(), getUserName(), dtoService.getDescription());
            } else {
                commentClient.addServiceComment(nodeId, inputComment, userName);
            }
        }
        // Close the pop up
        setAddPopupVisible(false);

        // set the title
        setTitle(ADD_COMMENT);

        // Reset the fields
        resetFields();

        reloadPage(event);
    }

    /**
     * Method to process deleting a comment.
     * 
     * @param event
     */
    public void deleteComment(ActionEvent event) {
        String commentID = getSelectedCommentId();
        if (commentID == null) {
            LOGGER.error("DeleteComment: Invalid comment ID parameter. Cannot delete comment");
            return;
        }

        String deploymentUrl = WSClientConfiguration.getProperty(WSClientConfiguration.FOUNDATION_REST_ENDPOINT);
        CommentClient commentClient = new CommentClient(deploymentUrl);
        if (isHostComment()) {
            HostClient hostClient = new HostClient(deploymentUrl);
            DtoHost dtoHost = hostClient.lookup(getHostName());
            if (StringUtils.equalsIgnoreCase(dtoHost.getAppType(), "nagios")) {
                NagiosCommandProcessor nagiosCommandProcessor = new NagiosCommandProcessor();
                nagiosCommandProcessor.deleteHostComment(getUserName(), commentID);
            } else {
                commentClient.deleteHostComment(nodeId, Integer.valueOf(commentID));
            }
        } else {
            ServiceClient serviceClient = new ServiceClient(deploymentUrl);
            DtoService dtoService = serviceClient.lookup(nodeId);
            if (StringUtils.equalsIgnoreCase(dtoService.getAppType(), "nagios")) {
                NagiosCommandProcessor nagiosCommandProcessor = new NagiosCommandProcessor();
                nagiosCommandProcessor.deleteServiceComment(getUserName(), commentID);
            } else {
                commentClient.deleteServiceComment(nodeId, Integer.valueOf(commentID));
            }
        }

        // set the title
        setTitle(DELETE_COMMENT);

        // close window
        setDeletePopupVisible(false);

        reloadPage(event);
    }

    // JMS PUSH

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.statusviewer.bean.ServerPush#refresh(java.lang.String)
     */
    @Override
    public void refresh(String xmlTopic) {
        // try {
        // if (xmlTopic == null) {
        // LOGGER.debug("onMessage(): Received null XML Message.");
        // return;
        // }
        //
        // /*
        // * Get the JMS updates for xmlMessage & particular nodeType [For
        // * comments-portlet will be only HOST or SERVICE].
        // *
        // * Update messages each indicating - action, id , node-type.
        // */
        // List<JMSUpdate> jmsUpdates = JMSUtils.getJMSUpdatesListFromXML(
        // xmlTopic, this.nodeType);
        //
        // if (jmsUpdates == null) {
        // LOGGER
        // .debug(
        // "onMessage(): Received null JMS Updates using JMSUtils.getJMSUpdatesListFromXML() utility method"
        // );
        // return;
        // }
        //
        // for (JMSUpdate update : jmsUpdates) {
        // if (update != null) {
        // /*
        // * If the nodeId matches with the enitiyID from jmsUpdates
        // * list,then only reload the data.
        // */
        // if (update.getId() == this.nodeId) {
        // /*
        // * Fetch the list of comments again using web service
        // * calls.
        // */
        // if (LOGGER.isDebugEnabled()) {
        // LOGGER
        // .debug(myName
        // + " ===> onMessage(): Performing update with JMS Push for Id ["
        // + this.nodeId + "]");
        // }
        // // Set the DIV tag style
        // setDivTagStyle();
        //
        // selectCommentsList();
        //
        // /* Initiate server side rendering to update portlet. */
        // SessionRenderer.render(groupRenderName);
        // }
        // }
        // }
        //
        // } catch (WSDataUnavailableException e) {
        // // Portlet must recover - For usability show last updated data
        // LOGGER
        // .warn(
        // "onMessage(): Error retrieving data from web services while performing JMS Push for node ("
        // + nodeId
        // + "). Maintaining latest state."
        // + e.getMessage());
        // } catch (GWPortalException e) {
        // LOGGER
        // .warn("onMessage(): Error retrieving data while JMS Push for node ("
        // + nodeId
        // + "). Maintaining latest state."
        // + e.getMessage());
        // }

    }

    // POPUP RENDERING METHODS
    /**
     * Method called when add comment button on UI is called.
     * 
     * @param event
     */
    public void showAddPopup(ActionEvent event) {
        resetFields();
        setRequired(false);
        if (isHostComment()) {
            setCommandDescription(CommandDescriptionConstants.ADD_HOST_COMMENT);
            // TODO set style for add comment pop-up here
            setPopupStyle(HOST_COMMENT_ADD_POPUP_STYLE);
        } else {
            setCommandDescription(CommandDescriptionConstants.ADD_SVC_COMMENT);
            setPopupStyle(SERVICE_COMMENT_ADD_POPUP_STYLE);
        }
        setRequired(true);
        setAddPopupVisible(true);
    }

    /**
     * Method called when close button on UI is called.
     * 
     * @param event
     */
    public void showDeletePopup(ActionEvent event) {
        if (isHostComment()) {
            setCommandDescription(CommandDescriptionConstants.DEL_HOST_COMMENT);
            setPopupStyle(HOST_COMMENT_DELETE_POPUP_STYLE);
        } else {
            setCommandDescription(CommandDescriptionConstants.DEL_SVC_COMMENT);
            setPopupStyle(SERVICE_COMMENT_DELETE_POPUP_STYLE);
        }
        // Retrieve parameters
        CommentsBean deletedComment = (CommentsBean) event.getComponent().getAttributes().get(PARAM_COMMENT_ID);
        setSelectedCommentId(deletedComment.getCommentId());
        // Enable pop up
        setDeletePopupVisible(true);
    }

    /**
     * Method called when close button on UI is called. Sets visibility of both
     * add & delete pop-ups to false.
     * 
     * @param event
     */
    public void closePopup(ActionEvent event) {
        setRequired(false);
        setAddPopupVisible(false);
        setDeletePopupVisible(false);
        resetFields();
    }

    /**
     * Resets pop-up fields.
     */
    public void resetFields() {
        setPersistent(true);
        setInputComment(Constant.EMPTY_STRING);
    }

    // ERROR HANDLING METHODS
    /**
     * Method sets the error flag to true which enables error page to be
     * displayed on UI, sets the error message to be displayed to the user and
     * logs the error. Ideally each catch block should call this method.
     * 
     * @param resourceKey
     *            - key for the localized message to be displayed on the UI.
     * @param logMessage
     *            - message to be logged.
     * 
     */
    public void handleError(String resourceKey, String logMessage) {
        setError(true);
        setErrorMessage(ResourceUtils.getLocalizedMessage(resourceKey));
        LOGGER.error(logMessage);
    }

    /**
     * Method that will be called on click of "Retry now" button on error page.
     * 
     * @param event
     */
    public void reloadPage(ActionEvent event) {

        try {
            // Set the DIV tag style
            setDivTagStyle();
            selectCommentsList();
        } catch (WSDataUnavailableException e) {
            handleError(
                    "com_groundwork_portal_statusviewer_commentsPortlet_error",
                    "reloadPage(): selectCommentsList() failed to re-initialize comments list. Web services data unavailable");
        } catch (GWPortalException e) {
            handleError(
                    "com_groundwork_portal_statusviewer_commentsPortlet_error",
                    "reloadPage(): selectCommentsList() failed to re-initialize comments list.");
        }
    }

    // GETTERS & SETTERS
    /**
     * Sets the nodeName.
     * 
     * @param nodeName
     *            the nodeName to set
     */
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * Returns the nodeName.
     * 
     * @return the nodeName
     */
    public String getNodeName() {
        return this.nodeName;
    }

    /**
     * Sets the nodeId.
     * 
     * @param nodeId
     *            the nodeId to set
     */
    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * Returns the nodeId.
     * 
     * @return the nodeId
     */
    public int getNodeId() {
        return this.nodeId;
    }

    /**
     * Sets the nodeType.
     * 
     * @param nodeType
     *            the nodeType to set
     */
    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    /**
     * Returns the nodeType.
     * 
     * @return the nodeType
     */
    public NodeType getNodeType() {
        return this.nodeType;
    }

    /**
     * Sets the errorMessage.
     * 
     * @param errorMessage
     *            the errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Returns the errorMessage.
     * 
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /**
     * Sets the error.
     * 
     * @param error
     *            the error to set
     */
    public void setError(boolean error) {
        this.error = error;
    }

    /**
     * Returns the error.
     * 
     * @return the error
     */
    public boolean isError() {
        return this.error;
    }

    /**
     * Sets the hostName.
     * 
     * @param hostName
     *            the hostName to set
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * Returns the hostName.
     * 
     * @return the hostName
     */
    public String getHostName() {
        return this.hostName;
    }

    /**
     * Sets the persistent.
     * 
     * @param persistent
     *            the persistent to set
     */
    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    /**
     * Returns the persistent.
     * 
     * @return the persistent
     */
    public boolean isPersistent() {
        return this.persistent;
    }

    /**
     * Sets the inputComment.
     * 
     * @param inputComment
     *            the inputComment to set
     */
    public void setInputComment(String inputComment) {
        this.inputComment = inputComment;
    }

    /**
     * Returns the inputComment.
     * 
     * @return the inputComment
     */
    public String getInputComment() {
        return this.inputComment;
    }

    /**
     * Sets the hostComment.
     * 
     * @param hostComment
     *            the hostComment to set
     */
    public void setHostComment(boolean hostComment) {
        this.hostComment = hostComment;
    }

    /**
     * Returns the hostComment.
     * 
     * @return the hostComment
     */
    public boolean isHostComment() {
        return this.hostComment;
    }

    /**
     * Sets the commandDescription.
     * 
     * @param commandDescription
     *            the commandDescription to set
     */
    public void setCommandDescription(String commandDescription) {
        this.commandDescription = commandDescription;
    }

    /**
     * Returns the commandDescription.
     * 
     * @return the commandDescription
     */
    public String getCommandDescription() {
        return this.commandDescription;
    }

    /**
     * Returns the commentsList.
     * 
     * @return commentsList
     */
    public List<CommentsBean> getCommentsList() {
        return this.commentsList;
    }

    /**
     * Sets the commentsList.
     * 
     * @param commentsList
     */
    public void setCommentsList(List<CommentsBean> commentsList) {
        this.commentsList = commentsList;
    }

    /**
     * Sets the addPopupVisible.
     * 
     * @param addPopupVisible
     *            the addPopupVisible to set
     */
    public void setAddPopupVisible(boolean addPopupVisible) {
        this.addPopupVisible = addPopupVisible;
    }

    /**
     * Returns the addPopupVisible.
     * 
     * @return the addPopupVisible
     */
    public boolean isAddPopupVisible() {
        return this.addPopupVisible;
    }

    /**
     * Sets the deletePopupVisible.
     * 
     * @param deletePopupVisible
     *            the deletePopupVisible to set
     */
    public void setDeletePopupVisible(boolean deletePopupVisible) {
        this.deletePopupVisible = deletePopupVisible;
    }

    /**
     * Returns the deletePopupVisible.
     * 
     * @return the deletePopupVisible
     */
    public boolean isDeletePopupVisible() {
        return this.deletePopupVisible;
    }

    /**
     * Sets the selectedCommentId.
     * 
     * @param selectedCommentId
     *            the selectedCommentId to set
     */
    public void setSelectedCommentId(String selectedCommentId) {
        this.selectedCommentId = selectedCommentId;
    }

    /**
     * Returns the selectedCommentId.
     * 
     * @return the selectedCommentId
     */
    public String getSelectedCommentId() {
        return this.selectedCommentId;
    }

    /**
     * Sets the userName.
     * 
     * @param userName
     *            the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Returns the userName.
     * 
     * @return the userName
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * Sets the emptyList.
     * 
     * @param emptyList
     *            the emptyList to set
     */
    public void setEmptyList(boolean emptyList) {
        this.emptyList = emptyList;
    }

    /**
     * Returns the emptyList.
     * 
     * @return the emptyList
     */
    public boolean isEmptyList() {
        // Set empty list boolean property
        if (this.commentsList.size() == 0) {
            this.setEmptyList(true);
        } else {
            this.setEmptyList(false);
        }
        return this.emptyList;
    }

    /**
     * Sets the popupStyle.
     * 
     * @param popupStyle
     *            the popupStyle to set
     */
    public void setPopupStyle(String popupStyle) {
        this.popupStyle = popupStyle;
    }

    /**
     * Returns the popupStyle.
     * 
     * @return the popupStyle
     */
    public String getPopupStyle() {
        return popupStyle;
    }

    /**
     * Validate.
     * 
     * @param context
     *            the context
     * @param event
     * 
     * @return false if validation fails
     */
    public boolean validate(FacesContext context, ActionEvent event) {
        UIComponent base = event.getComponent();
        if (null == context || null == base) {
            return false;
        }
        UIComponent parentForm = base.getParent();
        if (null == parentForm) {
            return false;
        }

        UIComponent commentsComponent = parentForm
                .findComponent("CPtxtCommentArea");
        String comment = getInputComment();

        if (null != commentsComponent) {
            // Validate length - REQUIRED
            if (comment == null || comment.trim().length() == 0
                    || comment == null) {
                ((UIInput) commentsComponent).setValid(false);
                showMessage("Comment is mandatory field.",
                        "Length of comment cannot be zero.", facesContext,
                        commentsComponent);
                return false;
            }

            // Validate length - MAX LENGTH = 500 chars
            if (comment.length() > COMMENT_MAX_ALLOWABLE_LENGTH) {
                ((UIInput) commentsComponent).setValid(false);
                setInputComment(comment);
                showMessage(
                        ResourceUtils
                                .getLocalizedMessage("com_groundwork_portal_statusviewer_commentsPortlet_lenghtExceeds_500"),
                        ResourceUtils
                                .getLocalizedMessage("com_groundwork_portal_statusviewer_commentsPortlet_lenghtExceeds_500"),
                        facesContext, commentsComponent);
                return false;
            }

            // Validate the characters in comment. # and , are not allowed.
            // if (!ValidationUtils
            // .isValidText(comment, Constant.COMMENTS_PATTERN)) {
            // ((UIInput) commentsComponent).setValid(false);
            // setInputComment(comment);
            // showMessage(
            // ResourceUtils
            // .getLocalizedMessage("com_groundwork_portal_statusviewer_commentsPortlet_comments_value"),
            // ResourceUtils
            // .getLocalizedMessage("com_groundwork_portal_statusviewer_commentsPortlet_comments_value"),
            // facesContext, commentsComponent);
            // return false;
            // }
        }
        return true;
    }

    /**
     * 
     * Method to set detail & summary message, severity fields for Faces
     * Message.
     * 
     * @param detailMessage
     * @param summaryMessage
     */
    private void showMessage(String detailMessage, String summaryMessage,
            FacesContext facesContext, UIComponent component) {
        // Custom faces message
        FacesMessage message = new FacesMessage();
        // Set message details
        message.setDetail(detailMessage);
        message.setSummary(summaryMessage);
        // Set severity
        message.setSeverity(FacesMessage.SEVERITY_ERROR);
        facesContext.addMessage(component.getClientId(facesContext), message);
    }

    /**
     * Method to close pop up window for info message to be displayed when
     * nagios is down.
     */
    public void closeNagiosDownPopup() {
        setNagiosDown(false);
    }

}

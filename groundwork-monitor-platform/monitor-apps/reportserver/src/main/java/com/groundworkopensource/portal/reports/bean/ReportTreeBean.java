/*
 * Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork) All rights
 * reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.portal.reports.bean;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.faces.event.ActionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.groundworkopensource.portal.common.ApplicationType;
import com.groundworkopensource.portal.common.PropertyUtils;
import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.reports.common.IPCUtils;
import com.groundworkopensource.portal.reports.common.ReportConstants;
import com.icesoft.faces.async.render.SessionRenderer;

/**
 * The back-end bean for the Generation of report Tree.
 * 
 * @author manish_kjain
 */
public class ReportTreeBean {

    /**
     * PUBLISH_REPORT
     */
    private static final String PUBLISH_REPORT = "publishReport";

    /**
     * VIEW_REPORT
     */
    private static final String VIEW_REPORT = "viewReport";

    /**
     * REPORTS_ERROR_TREE_ERROR
     */
    private static final String REPORTS_ERROR_TREE_ERROR = "reports.error.treeError";

    /**
     * PUBLISH_REPORT_REPORT_TREE_NAME
     */
    private static final String PUBLISH_REPORT_REPORT_TREE_NAME = "publish_report.report_tree_name";

    /**
	 * 
	 */
    private static final String ZERO = "0";

    /**
     * logger
     */
    private static Logger logger = Logger.getLogger(ReportTreeBean.class
            .getName());

    /**
     * Name of renderGroup to be rendered, for AJAX PUSH.
     */

    private String groupName;

    /**
     * holds which page currently selected.
     */
    private String view;

    /**
     * The session name of variable.
     */

    private static final String CURRENT_MODEL_KEY = "org.iceface.current.model";

    /**
     * Error boolean to set if error occurred
     */
    private boolean error = false;

    /**
     * Error message to show on UI
     */
    private String errorMessage;

    /**
     * create tree.
     */
    private final DefaultMutableTreeNode rootTreeNode = createTreeNode(
            ResourceUtils.getLocalizedMessage(PUBLISH_REPORT_REPORT_TREE_NAME),
            ReportConstants.BRANCH_CONTRACTED_ICON,
            ReportConstants.BRANCH_EXPANDED_ICON,
            ReportConstants.BRANCH_LEAF_ICON, ZERO, false);

    /**
     * Constructor.
     */
    public ReportTreeBean() {

        try {
            // create root node with its children expanded
            createTree(rootTreeNode, true);
            // model is accessed by by the ice:tree component
            setModel(new DefaultTreeModel(rootTreeNode));
            // set tree as expanded
            ((UrlNodeUserObject) rootTreeNode.getUserObject())
                    .setExpanded(true);
        } catch (GWPortalException e) {
            error = true;
            errorMessage = ResourceUtils
                    .getLocalizedMessage(REPORTS_ERROR_TREE_ERROR);
        }
    }

    /**
     * createTree method used to get the directory and file name from the XML
     * file and create the nodes.
     * 
     * @param rootTreeNode
     * @param leafEnabled
     */
    public static void createTree(final DefaultMutableTreeNode rootTreeNode,
            final boolean leafEnabled) {
        List<DefaultMutableTreeNode> treeNodeList = Collections
                .synchronizedList(new ArrayList<DefaultMutableTreeNode>());

        try {

            // get the context parameter value from web.XML
            String reportFileName = PropertyUtils.getProperty(
                    ApplicationType.REPORT_VIEWER,
                    ReportConstants.REPORTS_NAME_XML);
            File file = new File(reportFileName + ReportConstants.REPORT_EN_XML);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            NodeList nodeLst = doc
                    .getElementsByTagName(ReportConstants.REPORT_DIR);
            // create the directory nodes i.e. directory name
            for (int i = 0; i < nodeLst.getLength(); i++) {
                Node fstNode = nodeLst.item(i);
                String dirName = fstNode.getAttributes().item(0).getNodeValue();
                String displayDirName = fstNode.getAttributes().item(1)
                        .getNodeValue();

                DefaultMutableTreeNode branchNode = createTreeNode(
                        displayDirName, ReportConstants.BRANCH_CONTRACTED_ICON,
                        ReportConstants.BRANCH_EXPANDED_ICON,
                        ReportConstants.BRANCH_LEAF_ICON, null, false);

                treeNodeList.add(branchNode);
                // rootTreeNode.add(branchNode);

                if (leafEnabled) {
                    /*
                     * if the screen is View Report, that is leaves (Files) are
                     * enabled on directory nodes
                     */
                    Element fileElement = (Element) fstNode;
                    NodeList fileElementList = fileElement
                            .getElementsByTagName(ReportConstants.REPORT_FILE);
                    List<DefaultMutableTreeNode> leafTreeNodeList = Collections
                            .synchronizedList(new ArrayList<DefaultMutableTreeNode>());
                    // create the leaf nodes i.e. file name
                    for (int j = 0; j < fileElementList.getLength(); j++) {
                        Node fileNode = fileElementList.item(j);
                        if (fileNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element element = (Element) fileNode;
                            NodeList nodeList = element
                                    .getElementsByTagName(ReportConstants.DISPLAY_NAME);
                            Element firstNmElement = (Element) nodeList.item(0);
                            NodeList firstNm = firstNmElement.getChildNodes();
                            NodeList lastNmElementList = element
                                    .getElementsByTagName(ReportConstants.FILE_OBJECT_ID);
                            Element lastNmElement = (Element) lastNmElementList
                                    .item(0);
                            NodeList childList = lastNmElement.getChildNodes();
                            String displayFileName = (firstNm.item(0))
                                    .getNodeValue();
                            String fileobjectId = dirName
                                    + ReportConstants.SLASH
                                    + (childList.item(0)).getNodeValue();
                            DefaultMutableTreeNode subbranchNode = createTreeNode(
                                    displayFileName,
                                    ReportConstants.BRANCH_CONTRACTED_ICON,
                                    ReportConstants.BRANCH_EXPANDED_ICON,
                                    ReportConstants.BRANCH_LEAF_ICON,
                                    fileobjectId, true);
                            leafTreeNodeList.add(subbranchNode);
                            // branchNode.add(subbranchNode);
                        }
                    }
                    // Sort leaf tree node
                    leafTreeNodeList = getSortNodeList(leafTreeNodeList);
                    for (DefaultMutableTreeNode defaultMutableTreeNode : leafTreeNodeList) {
                        branchNode.add(defaultMutableTreeNode);
                    }

                }
            }
            // Sort tree node
            treeNodeList = getSortNodeList(treeNodeList);
            for (DefaultMutableTreeNode defaultMutableTreeNode : treeNodeList) {
                rootTreeNode.add(defaultMutableTreeNode);
            }

        } catch (ParserConfigurationException pce) {
            logger.warn("Error Parsing file.");
            pce.printStackTrace();
        } catch (IOException ioe) {
            logger.warn("Error writing file.");
            ioe.printStackTrace();
        } catch (SAXException sxe) {
            logger.warn("SAX ecxeption. probably bad file structure.");
            sxe.printStackTrace();
        }
    }

    /**
     * @param treeNodeList
     */
    private static List<DefaultMutableTreeNode> getSortNodeList(
            List<DefaultMutableTreeNode> treeNodeList) {
        Comparator<DefaultMutableTreeNode> comparator = new Comparator<DefaultMutableTreeNode>() {
            public int compare(DefaultMutableTreeNode entity1,
                    DefaultMutableTreeNode entity2) {
                String name1 = ((UrlNodeUserObject) entity1.getUserObject())
                        .getText();
                String name2 = ((UrlNodeUserObject) entity2.getUserObject())
                        .getText();
                // For sort order ascending -

                return name1.compareTo(name2);

            }
        };
        // sort the group List
        Collections.sort(treeNodeList, comparator);
        return treeNodeList;
    }

    /**
     * Method create the Tree Nodes.
     * 
     * @param nodeText
     * @param contractedIconPath
     * @param expandendIconPath
     * @param leafIconPath
     * @param nodeObjectId
     * @param isLeaf
     * @return DefaultMutableTreeNode
     */
    public static DefaultMutableTreeNode createTreeNode(final String nodeText,
            final String contractedIconPath, final String expandendIconPath,
            final String leafIconPath, final String nodeObjectId,
            final boolean isLeaf) {
        DefaultMutableTreeNode branchNode = new DefaultMutableTreeNode();
        UrlNodeUserObject branchObject = new UrlNodeUserObject(branchNode);
        branchObject.setText(nodeText);

        // set Icons
        branchObject
                .setBranchContractedIcon(ReportConstants.BRANCH_CONTRACTED_ICON);
        branchObject
                .setBranchExpandedIcon(ReportConstants.BRANCH_EXPANDED_ICON);
        branchObject.setLeafIcon(ReportConstants.BRANCH_LEAF_ICON);
        branchObject.setObjectId(nodeObjectId);
        branchNode.setUserObject(branchObject);
        branchObject.setLeaf(isLeaf);
        return branchNode;
    }

    /**
     * Gets the tree's default model.
     * 
     * @return tree model.
     * @throws GWPortalException
     */
    public DefaultTreeModel getModel() throws GWPortalException {
        return (DefaultTreeModel) IPCUtils
                .getApplicationAttribute(CURRENT_MODEL_KEY);
    }

    /**
     * @param view
     *            rerenders tree when switch
     * @throws GWPortalException
     */
    public void refreshTree(final String view) throws GWPortalException {
        this.setView(view);

        if (view.equalsIgnoreCase(VIEW_REPORT)) {
            createTreeForViewOperation();
        } else if (view.equalsIgnoreCase(PUBLISH_REPORT)) {
            createTreeForPublishOperation();
        }

        setModel(new DefaultTreeModel(rootTreeNode));
    }

    /**
     * creates Tree For View Operation, with children (files).
     */
    public void createTreeForViewOperation() {
        // before creating tree, remove all children nodes if any
        rootTreeNode.removeAllChildren();

        // set tree expanded
        createTree(rootTreeNode, true);
        ((UrlNodeUserObject) rootTreeNode.getUserObject()).setExpanded(true);
    }

    /**
     * creates Tree For publish Operation, without children (files).
     */

    public void createTreeForPublishOperation() {
        // before creating tree, remove all children nodes if any
        rootTreeNode.removeAllChildren();

        createTree(rootTreeNode, false);
        // set tree expanded
        ((UrlNodeUserObject) rootTreeNode.getUserObject()).setExpanded(true);
    }

    /**
     * @param model
     * @throws GWPortalException
     */
    public void setModel(final DefaultTreeModel model) throws GWPortalException {
        if (groupName == null) {
            groupName = IPCUtils.getSessionID();
            SessionRenderer.addCurrentSession(groupName);
        }
        IPCUtils.setApplicationAttribute(CURRENT_MODEL_KEY, model);
        SessionRenderer.render(groupName);
    }

    /**
     * @param view
     *            the view to set
     */
    public void setView(final String view) {
        this.view = view;
    }

    /**
     * @return the view
     */
    public String getView() {
        return view;
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
        return error;
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
        return errorMessage;
    }

    /**
     * Method that will be called on click of "Retry now" button on error page.
     * 
     * @param event
     */
    public void reloadPage(ActionEvent event) {

        error = false;
        try {
            // create root node with its children expanded
            createTree(rootTreeNode, true);
            // set tree expanded
            ((UrlNodeUserObject) rootTreeNode.getUserObject())
                    .setExpanded(true);
            // model is accessed by by the ice:tree component
            setModel(new DefaultTreeModel(rootTreeNode));
        } catch (GWPortalException e) {
            error = true;
            errorMessage = ResourceUtils
                    .getLocalizedMessage(REPORTS_ERROR_TREE_ERROR);
        }
    }

}

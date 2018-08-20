/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.portal.migration.xml.mop;

import org.codehaus.staxmate.SMOutputFactory;
import org.codehaus.staxmate.out.SMOutputDocument;
import org.codehaus.staxmate.out.SMOutputElement;
import org.jboss.portal.migration.xml.mop.MSite;
import javax.xml.stream.XMLOutputFactory;
import java.io.OutputStream;
import java.util.Map;

/**
 * This class is taking care of converting the import/export object model data (Obtained from EPP 4.3 live instance) into XML to an OutputStream.
 *
 * @author  theute
 */
public class SiteExporter {
    private SMOutputDocument doc;
    private SMOutputElement  sites;

    /**
     * @param   os
     *
     * @throws  Exception
     */
    public void startExport(OutputStream os) throws Exception {
        SMOutputFactory outf = new SMOutputFactory(XMLOutputFactory.newInstance());

        doc = outf.createOutputDocument(os);
        doc.setIndentation("\n  ", 1, 1);
        doc.addComment(" generated: " + new java.util.Date().toString());
        sites = doc.addElement("sites");
    }

    /** @throws  Exception */
    public void endExport() throws Exception {
        if (doc != null) {
            doc.closeRoot();
        }
    }

    /**
     * Get the data from the MSite (a portal) model and starts producing XML for data transfer &lt;site name="siteName"&gt; &lt;pages&gt;
     * &lt;/pages&gt; &lt;/site&gt;
     *
     * @param   site
     *
     * @throws  Exception
     */
    public void exportSite(MSite site) throws Exception {
        if (site == null) {
            throw new IllegalStateException("Document root element not created");
        }

        // <site>
        SMOutputElement u = sites.addElement("site");

        u.addAttribute("name", site.getName());

        SMOutputElement pagesE = u.addElement("pages");

        for (MPage page : site.getPages()) {
            exportPage(page, pagesE);
        }
    }

    /**
     * Get the data from the MPage (a page) model and produce XML for data transfer &lt;page name="pageName"&gt; &lt;properties&gt; &lt;property&gt;
     * &lt;key&gt;Key&lt;/key&gt; &lt;value&gt;Key&lt;/value&gt; &lt;/property&gt; &lt;/properties&gt; &lt;pages&gt; &lt;/pages&gt; &lt;windows&gt;
     * &lt;/windows&gt; &lt;/page&gt;
     *
     * @param   page
     * @param   pagesE
     *
     * @throws  Exception
     */
    private void exportPage(MPage page, SMOutputElement pagesE) throws Exception {
        SMOutputElement pageE = pagesE.addElement("page");

        pageE.addAttribute("name", page.getName());

        SMOutputElement propertiesE = pageE.addElement("properties");

        if (page.getProperties() != null) {
            for (Map.Entry<String, String> entry : page.getProperties().entrySet()) {
                SMOutputElement propertyE = propertiesE.addElement("property");

                propertyE.addElement("key").addCData(entry.getKey());
                propertyE.addElement("value").addCData(entry.getValue());
            }
        }

        SMOutputElement displayNames = pageE.addElement("displayNames");

        for (Map.Entry<String, String> entry : page.getDisplayNameIntl().entrySet()) {
            SMOutputElement propertyE = displayNames.addElement("displayName");

            propertyE.addElement("locale").addCData(entry.getKey());
            propertyE.addElement("value").addCData(entry.getValue());
        }

        SMOutputElement pagesE2 = pageE.addElement("pages");

        for (MPage page2 : page.getPages()) {
            exportPage(page2, pagesE2);
        }

        SMOutputElement windowsE = pageE.addElement("windows");

        for (MWindow window : page.getWindows()) {
            exportWindow(window, windowsE);
        }
    }

    /**
     * Get the data from the MWindow (a window) model and produce XML for data transfer pages elements. &lt;window name="windowName"
     * contentType="myContentType" uri="myURI"&gt; &lt;properties&gt; &lt;property&gt; &lt;key&gt;Key&lt;/key&gt; &lt;value&gt;Key&lt;/value&gt;
     * &lt;/property&gt; &lt;/properties&gt; &lt;/page&gt;
     *
     * @param   window
     * @param   windowsE
     *
     * @throws  Exception
     */
    private void exportWindow(MWindow window, SMOutputElement windowsE) throws Exception {
        SMOutputElement windowE = windowsE.addElement("window");

        windowE.addAttribute("name", window.getName());
        windowE.addAttribute("contentType", window.getContentType());
        windowE.addAttribute("uri", window.getUri());

        SMOutputElement propertiesE = windowE.addElement("properties");

        for (Map.Entry<String, String> entry : window.getProperties().entrySet()) {
            SMOutputElement propertyE = propertiesE.addElement("property");

            propertyE.addElement("key").addCData(entry.getKey());
            propertyE.addElement("value").addCData(entry.getValue());
        }

        exportContent(window.getContent(), window.getContentType(), windowE);
    }

    /**
     * Get the data from the MContent (any content but only portlet content type is currently supported) model and produce XML for data transfer
     * &lt;content&gt; &lt;/content&gt;
     *
     * @param   content
     * @param   contentType
     * @param   windowE
     *
     * @throws  Exception
     */
    private void exportContent(MContent content, String contentType, SMOutputElement windowE) throws Exception {
        SMOutputElement contentE = windowE.addElement("content");

        if (contentType.equals("portlet")) {
            exportPortletContent((MPortlet) content, contentE);
        }
    }

    /**
     * Get the data from the MPortlet (a portlet content) model and produce XML for data transfer &lt;portlet&gt;
     * &lt;applicationName&gt;MyApplicationName&lt;/applicationName&gt; &lt;portletName&gt;MyPortletName&lt;/portletName&gt;
     * &lt;title&gt;MyTitle&lt;/title&gt; &lt;displayName&gt;MyDisplayName&lt;/displayName&gt; &lt;/portlet&gt;
     *
     * @param   portlet
     * @param   contentE
     *
     * @throws  Exception
     */
    private void exportPortletContent(MPortlet portlet, SMOutputElement contentE) throws Exception {
        SMOutputElement portletE = contentE.addElement("portlet");

        portletE.addElement("applicationName").addCData(portlet.getPortletApplication());
        portletE.addElement("portletName").addCData(portlet.getPortletName());
        portletE.addElement("title").addCData(portlet.getTitle());

        String displayName = ((portlet.getDisplayName()) != null) ? (portlet.getDisplayName())
                                                                  : portlet.getTitle();

        portletE.addElement("displayName").addCData(displayName);

        SMOutputElement propertiesE = portletE.addElement("preferences");

        for (Map.Entry<String, String> entry : portlet.getPreferences().entrySet()) {
            SMOutputElement propertyE = propertiesE.addElement("preference");

            propertyE.addElement("key").addCData(entry.getKey());
            propertyE.addElement("value").addCData(entry.getValue());
        }
    }
}

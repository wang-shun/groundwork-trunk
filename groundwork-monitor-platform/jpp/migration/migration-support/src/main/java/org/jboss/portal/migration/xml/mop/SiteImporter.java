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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;

/**
 * This class is taking care of taking an XML InputStream and transform its content into an object model that can later be imported into EPP 5.
 *
 * @author  theute
 */
public class SiteImporter {
    private SMHierarchicCursor rootC;
    private SMInputCursor      siteC;

    /**
     * Should be called to start import from file.
     *
     * @param   is
     *
     * @throws  Exception
     */
    public void startImport(InputStream is) throws Exception {
        SMInputFactory inf = new SMInputFactory(XMLInputFactory.newInstance());

        rootC = inf.rootElementCursor(is);
        rootC.advance();
        siteC = rootC.childElementCursor("site");
    }

    /** @throws  Exception */
    public void endImport() throws Exception {
        if (rootC != null) {
            rootC.getStreamReader().closeCompletely();
        }
    }

    /**
     * Will return MSite from piece of XML that StAX cursor is currently pointing at.
     *
     * @return
     *
     * @throws  Exception
     */
    public MSite getMSite() throws Exception {
        // Check if there is still something to read
        siteC.advance();

        if (siteC.asEvent() == null) {
            return null;
        }

        // <site>
        String name = siteC.getAttrValue("name");
        MSite  site = new MSite(name);

        // <pages>
        SMInputCursor pagesC = siteC.childElementCursor("pages").advance();

        fillPages(pagesC, site);

        return site;
    }

    private void fillPages(SMInputCursor pagesC, MSite site) throws XMLStreamException {
        // <page>
        SMInputCursor pageC = pagesC.childElementCursor("page").advance();

        while (pageC.asEvent() != null) {
            String pageName = pageC.getAttrValue("name");

            System.out.println("page name: " + pageName);

            MPage page = new MPage(pageName);

            // properties
            SMInputCursor       propertiesC = pageC.childElementCursor().advance();                  // properties
            SMInputCursor       propertyC   = propertiesC.childElementCursor("property").advance();  // property
            Map<String, String> properties  = new HashMap<String, String>();

            while (propertyC.asEvent() != null) {
                SMInputCursor keyC   = propertyC.childElementCursor().advance();                     // key
                String        key    = keyC.collectDescendantText();
                SMInputCursor valueC = keyC.advance();                                               // value
                String        value  = valueC.collectDescendantText();

                properties.put(key, value);
                propertyC.advance();
            }

            page.setProperties(properties);

            SMInputCursor       displayNamesC = propertiesC.advance();
            SMInputCursor       displayNameC  = displayNamesC.childElementCursor("displayName").advance();
            Map<String, String> displayNames  = new HashMap<String, String>();

            while (displayNameC.asEvent() != null) {
                SMInputCursor keyC   = displayNameC.childElementCursor().advance();
                String        key    = keyC.collectDescendantText();
                SMInputCursor valueC = keyC.advance();
                String        value  = valueC.collectDescendantText();

                displayNames.put(key, value);
                displayNameC.advance();
            }

            page.setDisplayNameIntl(displayNames);

            SMInputCursor pagesC2 = propertiesC.advance();

            fillPages(pagesC2, page);

            // <windows>
            SMInputCursor windowsC = propertiesC.advance();

            fillWindows(windowsC, page);
            site.addPage(page);
            pageC.advance();
        }
    }

    private void fillWindows(SMInputCursor windowsC, MPage mpage) throws XMLStreamException {
        // <window>
        SMInputCursor windowC = windowsC.childElementCursor("window").advance();

        while (windowC.asEvent() != null) {
            System.out.println("window");

            MWindow window = new MWindow(windowC.getAttrValue("name"));

            window.setContentType(windowC.getAttrValue("contentType"));
            window.setURI(windowC.getAttrValue("uri"));

            // properties
            SMInputCursor       propertiesC = windowC.childElementCursor().advance();
            SMInputCursor       propertyC   = propertiesC.childElementCursor("property").advance();
            Map<String, String> properties  = new HashMap<String, String>();

            while (propertyC.asEvent() != null) {
                SMInputCursor keyC   = propertyC.childElementCursor().advance();
                String        key    = keyC.collectDescendantText();
                SMInputCursor valueC = keyC.advance();
                String        value  = valueC.collectDescendantText();

                properties.put(key, value);
                propertyC.advance();
            }

            System.out.println("win: " + properties.keySet());
            window.setProperties(properties);

            // content
            SMInputCursor contentC = propertiesC.advance();

            if (window.getContentType().equals("portlet")) {
                SMInputCursor portletC = contentC.childElementCursor("portlet").advance();
                MPortlet      content  = new MPortlet(window.getUri());
                SMInputCursor appNameC = portletC.childElementCursor().advance();

                content.setPortletApplication(appNameC.collectDescendantText());

                SMInputCursor portletNameC = appNameC.advance();

                content.setPortletName(portletNameC.collectDescendantText());

                SMInputCursor portletTitleC = portletNameC.advance();

                content.setTitle(portletTitleC.collectDescendantText());

                SMInputCursor displayNameC = portletNameC.advance();

                content.setDisplayName(displayNameC.collectDescendantText());

                SMInputCursor preferencesC = portletNameC.advance();
                SMInputCursor preferenceC  = preferencesC.childElementCursor("preference").advance();

                while (preferenceC.asEvent() != null) {
                    SMInputCursor keyC   = preferenceC.childElementCursor().advance();
                    String        key    = keyC.collectDescendantText();
                    SMInputCursor valueC = keyC.advance();
                    String        value  = valueC.collectDescendantText();

                    content.addPreference(key, value);
                    preferenceC.advance();
                }

                System.out.println(content.getPortletApplication() + "/" + content.getPortletName());
                window.setContent(content);
            }

            mpage.addWindow(window);
            windowC.advance();
        }
    }
}

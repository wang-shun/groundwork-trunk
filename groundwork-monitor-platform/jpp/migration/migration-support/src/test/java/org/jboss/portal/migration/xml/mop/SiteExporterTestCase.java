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

import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;

public class SiteExporterTestCase {
    public void testExport() throws Exception {
        MSite        site     = new MSite("test");
        SiteExporter exporter = new SiteExporter();

        exporter.startExport(System.out);
        exporter.exportSite(site);
        exporter.endExport();
    }

    public void testExport2() throws Exception {
        // MSite site = new MSite("test2");
        // MPage page1 = new MPage("page1");
        // Map<String, String> properties = new HashMap<String, String>();
        //
        // properties.put("prop1", "value1");
        // properties.put("prop2", "value2");
        // page1.setProperties(properties);
        // page1.setDisplayNameIntl(new HashMap<String, String>());
        //
        // MPage page11 = new MPage("page11");
        //
        // page1.addPage(page11);
        // site.addPage(page1);
        //
        // Map<String, String> properties11 = new HashMap<String, String>();
        //
        // properties11.put("prop1", "value1");
        // properties11.put("prop2", "value2");
        // page11.setProperties(properties11);
        // page11.setDisplayNameIntl(new HashMap<String, String>());
        //
        // SiteExporter exporter = new SiteExporter();
        //
        // exporter.startExport(System.out);
        // exporter.exportSite(site);
        // exporter.endExport();
    }

    public void testExport3() throws Exception {
        // MSite site = new MSite("test2");
        // MPage page1 = new MPage("page1");
        // Map<String, String> properties = new HashMap<String, String>();
        //
        // properties.put("prop1", "value1");
        // properties.put("prop2", "value2");
        // page1.setProperties(properties);
        //
        // MPage page11 = new MPage("page11");
        //
        // page1.addPage(page11);
        // site.addPage(page1);
        //
        // Map<String, String> properties11 = new HashMap<String, String>();
        //
        // properties11.put("prop1", "value1");
        // properties11.put("prop2", "value2");
        // page11.setProperties(properties11);
        //
        // SiteExporter exporter = new SiteExporter();
        //
        // exporter.startExport(System.out);
        // exporter.exportSite(site);
        // exporter.endExport();
    }
}

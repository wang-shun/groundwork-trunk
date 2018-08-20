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

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import junit.framework.TestCase;

public class SiteImporterTestCase extends TestCase {
    public void test01() throws Exception {
        String XML = "<sites>"
                       + "<site name=\"test2\">"
                       + "<pages>"
                       + "<page name=\"page1\">"
                       + "<properties><property>"
                       + "  <key><![CDATA[prop2]]></key>"
                       + "  <value><![CDATA[value2]]></value>"
                       + ""
                       + "  </property>"
                       + "  <property>"
                       + "  <key><![CDATA[prop1]]></key>"
                       + "  <value><![CDATA[value1]]></value>"
                       + "  </property>"
                       + "  </properties>"
                       + "  <displayNames>" + "   <displayName>"
                       + "     <locale><![CDATA[en]]></locale>"
                       + "     <value><![CDATA[Administration]]></value>"
                       + "    </displayName>" + "  </displayNames>" + "  <pages>"
                       + "  <pages>"
                       + "  <page name=\"page11\">"
                       + "  <properties>"
                       + "  <property>"
                       + "  <key><![CDATA[prop2]]></key>"
                       + "  <value><![CDATA[value2]]></value>"
                       + "  </property>"
                       + "  <property>"
                       + "  <key><![CDATA[prop1]]></key>"
                       + "  <value><![CDATA[value1]]></value>"
                       + "  </property>"
                       + "  </properties>"
                       + "  <displayNames>" + "   <displayName>"
                       + "     <locale><![CDATA[en]]></locale>"
                       + "     <value><![CDATA[Administration]]></value>"
                       + "    </displayName>" + "  </displayNames>" + "  <pages>"
                       + "  <pages/>"
                       + "  <windows/>"
                       + "  </page>"
                       + "  </pages>"
                       + "  <windows/>"
                       + "  </page>"
                       + "  </pages>" + " </site>" + "</sites>";

        // SiteImporter importer = new SiteImporter();
        // StringReader reader = new StringReader(XML);
        //
        // importer.startImport(new ByteArrayInputStream(XML.getBytes()));
        //
        // MSite site = importer.getMSite();
        //
        // assertEquals("page1", site.getPages().get(0).getName());
        // assertEquals("page11",
        // site.getPages().get(0).getPages().get(0).getName());
        // assertEquals(2, site.getPages().get(0).getProperties().size());
    }
}

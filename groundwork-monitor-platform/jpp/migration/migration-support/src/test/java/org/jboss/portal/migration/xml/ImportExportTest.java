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
package org.jboss.portal.migration.xml;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jboss.portal.migration.xml.identity.MProperty;
import org.jboss.portal.migration.xml.identity.MRole;
import org.jboss.portal.migration.xml.identity.MUser;
import org.jboss.portal.migration.xml.identity.RoleExporter;
import org.jboss.portal.migration.xml.identity.UserExporter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

/** Unit test for simple App. */
public class ImportExportTest extends TestCase {
    /**
     * Create the test case.
     *
     * @param  testName  name of the test case
     */
    public ImportExportTest(String testName) {
        super(testName);
    }

    /** @return  the suite of tests being tested */
    public static Test suite() {
        return new TestSuite(ImportExportTest.class);
    }

    /** Rigourous Test :-). */
    public void testUserExport() throws Exception {
        UserExporter uex = new UserExporter();

        // Prepare user set
        List<MProperty> props = new LinkedList<MProperty>();

        for (int i = 0; i < 5; i++) {
            MProperty mprop = new MProperty("someTestName_" + i, "someStrangeTestType", "xxxxxxxeValue");

            props.add(mprop);
        }

        List<MUser> users = new LinkedList<MUser>();

        for (int i = 0; i < 10000; i++) {
            MUser muser = new MUser("someSampleTestUserName_" + i, props);

            users.add(muser);
        }

        System.out.println("Exporting users: " + users.size());

        FileOutputStream fos       = new FileOutputStream("target/testUsers.xml");
        long             starttime = System.currentTimeMillis();

        uex.startExport(fos);

        for (MUser user : users) {
            uex.exportUser(user);
        }

        uex.endExport();
        fos.close();
        System.out.println("#### users export completed in " + (System.currentTimeMillis() - starttime) + " ms");
    }

    public void testRoles() throws Exception {
        RoleExporter rex = new RoleExporter();

        // Prepare user set
        Set<String> members = new HashSet<String>();

        for (int i = 0; i < 100; i++) {
            members.add("someMemberUserName_" + i);
        }

        List<MRole> roles = new LinkedList<MRole>();

        for (int i = 0; i < 4000; i++) {
            MRole mrole = new MRole("someSampleTestRoleName_" + i, "someTestDisplayName_" + i, members);

            roles.add(mrole);
        }

        FileOutputStream fos       = new FileOutputStream("target/testRoles.xml");
        long             starttime = System.currentTimeMillis();

        rex.startExport(fos);

        for (MRole role : roles) {
            rex.exportRole(role);
        }

        rex.endExport();
        fos.close();
        System.out.println("#### roles export completed in " + (System.currentTimeMillis() - starttime) + " ms");

        FileInputStream fis = new FileInputStream("target/testRoles.xml");
    }
}

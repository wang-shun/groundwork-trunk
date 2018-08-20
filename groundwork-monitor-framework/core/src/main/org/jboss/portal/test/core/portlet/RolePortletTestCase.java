/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2006, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.jboss.portal.test.core.portlet;

import net.sourceforge.jwebunit.WebTestCase;
import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.jboss.portal.test.core.Utils;

/** @author <a href="theute@jboss.org">Thomas Heute </a> $Revision: 8786 $ */
public class RolePortletTestCase extends WebTestCase
{

   protected void setUp() throws Exception
   {
      super.setUp();
      // initialize your database connection here
      IDatabaseConnection connection = Utils.getConnection();

      // initialize your dataset here
      IDataSet dataSet = Utils.getDataSet("resources/test/datarole.xml");

      try
      {
         Utils.resetAutoIncrement();
         DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);

      }
      finally
      {
         connection.close();
      }
   }

   public RolePortletTestCase(String name)
   {
      super(name);
      getTestContext().setBaseUrl("http://localhost.localdomain:8080/portal");
//        getTestContext().setUserAgent("Mozilla");
   }

   public void loginAdmin()
   {
      beginAt("/index.html?_id=page.default.admin");
      clickLink("standardlogin");
      setFormElement("j_username", "admin");
      setFormElement("j_password", "admin");
      submit();
   }

   public void testPresence()
   {
      loginAdmin();
      assertLinkPresent("createRole");
      assertLinkPresent("editRole");
   }

   public void testCreateRole()
   {
      loginAdmin();
      clickLink("createRole");
      assertFormPresent("createRole");
      assertFormElementPresent("rolename");
      assertFormElementPresent("roledisplayname");
      setFormElement("rolename", "myRole");
      setFormElement("roledisplayname", "myDisplayRole");
      submit();
      assertLinkPresent("createRole");
      assertLinkPresent("editRole");

      try
      {
         Assertion.assertEquals(Utils.getDataSet("resources/test/datarolecreated.xml").getTable("jbp_roles"), Utils.getConnection().createDataSet().getTable("jbp_roles"));
      }
      catch (DatabaseUnitException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   public void testEditRole()
   {
      loginAdmin();
      clickLink("editRole");
      assertFormPresent("editRole");
      assertFormElementPresent("roleid");
      assertFormElementPresent("roledisplayname");
      setWorkingForm("editRole");
      setFormElement("roleid", "2");
      setFormElement("roledisplayname", "myNewDisplayRole");
      submit();
      assertLinkPresent("createRole");
      assertLinkPresent("editRole");
      try
      {
         Assertion.assertEquals(Utils.getDataSet("resources/test/dataroleedited.xml").getTable("jbp_roles"), Utils.getConnection().createDataSet().getTable("jbp_roles"));
      }
      catch (DatabaseUnitException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   public void testDeleteRole()
   {
      loginAdmin();
      clickLink("editRole");
      assertFormPresent("deleteRole");
      assertFormElementPresent("roleid");
      setWorkingForm("deleteRole");
      setFormElement("roleid", "2");
      submit();
      assertTextPresent("The role has been deleted");
      try
      {
         Assertion.assertEquals(Utils.getDataSet("resources/test/dataroledeleted.xml").getTable("jbp_roles"), Utils.getConnection().createDataSet().getTable("jbp_roles"));
      }
      catch (DatabaseUnitException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
}

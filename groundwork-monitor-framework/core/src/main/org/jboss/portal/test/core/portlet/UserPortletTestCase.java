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
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.jboss.portal.test.core.Utils;

/** @author <a href="theute@jboss.org">Thomas Heute </a> $Revision: 8786 $ */
public class UserPortletTestCase extends WebTestCase
{

   public UserPortletTestCase(String name)
   {
      super(name);
      getTestContext().setBaseUrl("http://localhost.localdomain:8080/portal");
//        getTestContext().setUserAgent("Mozilla");
   }

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

   public void testPresence()
   {
      beginAt("/index.html");
      assertLinkPresent("standardlogin");
      assertLinkPresent("register");
   }

   public void testClickRegister()
   {
      beginAt("/index.html");
      clickLink("register");
      assertFormPresent("register");
      assertFormElementPresent("uname");
      assertFormElementPresent("pass1");
      assertFormElementPresent("pass2");
      assertFormElementPresent("realemail");
      assertFormElementPresent("fakeemail");
      assertFormElementPresent("question");
      assertFormElementPresent("answer");
      assertSubmitButtonPresent("register");
      assertLinkPresent("login");
   }

   public void testClickLogin()
   {
      beginAt("/index.html");
      clickLink("standardlogin");
      assertFormPresent("loginform");
      assertFormElementPresent("j_username");
      assertFormElementPresent("j_password");
      assertSubmitButtonPresent("login");
   }


   public void testRegister()
   {
      beginAt("/index.html");
      clickLink("register");
      setFormElement("uname", "testingDude");
      setFormElement("pass1", "testingPassword");
      setFormElement("pass2", "testingPassword");
      setFormElement("realemail", "email@exemple.com");
      submit();
   }

   public void testUserLogin()
   {
      beginAt("/index.html");
      clickLink("standardlogin");
      setFormElement("j_username", "user");
      setFormElement("j_password", "user");
      submit();
      assertLinkPresent("editprofile");
      assertLinkNotPresent("listusers");
      assertLinkPresent("logout");
   }

   public void testAdminLogin()
   {
      beginAt("/index.html");
      clickLink("standardlogin");
      setFormElement("j_username", "admin");
      setFormElement("j_password", "admin");
      submit();
      assertLinkPresent("editprofile");
      assertLinkPresent("listusers");
      assertLinkPresent("logout");
   }

   public void testLogout()
   {
      beginAt("/index.html");
      clickLink("standardlogin");
      setFormElement("j_username", "user");
      setFormElement("j_password", "user");
      submit();
      clickLink("logout");
      assertLinkPresent("standardlogin");
   }
}

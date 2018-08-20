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
package org.jboss.portal.test.core.theme;

import net.sourceforge.jwebunit.WebTestCase;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.jboss.portal.test.core.Utils;

/**
 * @author <a href="mailto:mageshbk@jboss.com">Magesh Kumar Bojan</a>
 * @version $Revision: 8786 $
 */
public class UserThemeTestCase extends WebTestCase
{
   public UserThemeTestCase(String key)
   {
      super(key);
      getTestContext().setBaseUrl("http://localhost:8080/portal");
   }

   protected void setUp() throws Exception
   {
      super.setUp();
   }

   protected void assertString(String str)
   {
      boolean isFound = getTester().getDialog().getResponseText().indexOf(str) >= 0 ? true : false;
      assertTrue("Unable to locate [" + str + "] ", isFound);
   }

   public void testChangeTheme()
   {
      beginAt("/index.html");
      clickLink("standardlogin");
      setFormElement("j_username", "user");
      setFormElement("j_password", "user");
      submit();
      clickLink("editprofile");
      setFormElement("theme", "portal.industrial");
      submit();
      assertString("/portal-core/themes/industrial/portal_style.css");
      clickLink("logout");
      assertLinkPresent("standardlogin");
      assertString("/portal-core/themes/phalanx/portal_style.css");
      clickLink("standardlogin");
      setFormElement("j_username", "user");
      setFormElement("j_password", "user");
      submit();
      assertString("/portal-core/themes/industrial/portal_style.css");
      clickLink("logout");
   }

   public void testDefaultTheme()
   {
      beginAt("/index.html");
      clickLink("standardlogin");
      setFormElement("j_username", "admin");
      setFormElement("j_password", "admin");
      submit();
      assertString("/portal-core/themes/phalanx/portal_style.css");
      clickLink("logout");
      assertLinkPresent("standardlogin");
   }

   public void testDeletedTheme()
   {
      // initialize your database connection here
      IDatabaseConnection connection = null;

      try
      {
         connection = Utils.getConnection();
         // initialize your dataset here
         IDataSet dataSet = Utils.getDataSet("resources/test/theme.xml");
         DatabaseOperation.UPDATE.execute(connection, dataSet);

      }
      catch (Exception dbe)
      {
         dbe.printStackTrace();
      }
      finally
      {
         if (connection != null)
         {
            try
            {
               connection.close();
            }
            catch (Exception sqle)
            {
               sqle.printStackTrace();
            }
         }
      }
      beginAt("/index.html");
      clickLink("standardlogin");
      setFormElement("j_username", "user");
      setFormElement("j_password", "user");
      submit();
      assertLinkPresent("editprofile");
      assertString("/portal-core/themes/phalanx/portal_style.css");
      clickLink("editprofile");
      assertFormElementEquals("theme", ""); //Site Default
      submit();
      clickLink("logout");
      assertLinkPresent("standardlogin");
   }

   public void testResetTheme()
   {
      beginAt("/index.html");
      clickLink("standardlogin");
      setFormElement("j_username", "user");
      setFormElement("j_password", "user");
      submit();
      assertLinkPresent("editprofile");
      clickLink("editprofile");
      setFormElement("theme", "");
      assertString("/portal-core/themes/phalanx/portal_style.css");
      submit();
      clickLink("logout");
      assertLinkPresent("standardlogin");
   }

}

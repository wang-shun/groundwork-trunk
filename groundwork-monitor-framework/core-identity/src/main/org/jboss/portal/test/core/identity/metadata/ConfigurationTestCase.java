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
package org.jboss.portal.test.core.identity.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.jboss.portal.core.identity.services.metadata.IdentityUIConfiguration;
import org.jboss.portal.core.identity.services.metadata.IdentityUIConfigurationServiceImpl;
import org.jboss.portal.core.identity.services.metadata.UIComponentConfiguration;
import org.jboss.portal.test.core.identity.mock.MockPropertyInfo;
import org.jboss.portal.test.core.identity.mock.MockUserProfileModule;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class ConfigurationTestCase extends TestCase
{
   /** The (mock) userProfileModule */
   MockUserProfileModule userProfileModule;
   
   protected void setUp()
   {
      this.userProfileModule = new MockUserProfileModule();
      
      // Creating PropertyInfo Objects 
      MockPropertyInfo mpi1 = new MockPropertyInfo("user.name.given");
      mpi1.setAccessMode("read-only");
      MockPropertyInfo mpi2 = new MockPropertyInfo("user.name.family");
      mpi2.setUsage("mandatory");
      MockPropertyInfo mpi3 = new MockPropertyInfo("user.business-info.online.email");
      MockPropertyInfo mpi4 = new MockPropertyInfo("portal.user.locale");
      MockPropertyInfo mpi5 = new MockPropertyInfo("portal.user.interests");
      
      // Feed the (mock) userProfileModule with information 
      this.userProfileModule.setPropertyInfo(mpi1.getName(), mpi1);
      this.userProfileModule.setPropertyInfo(mpi2.getName(), mpi2);
      this.userProfileModule.setPropertyInfo(mpi3.getName(), mpi3);
      this.userProfileModule.setPropertyInfo(mpi4.getName(), mpi4);
      this.userProfileModule.setPropertyInfo(mpi5.getName(), mpi5);
   }

   public void testBasicConfiguration()
   {
      // Create configuration service with conf/configuration1.xml
      IdentityUIConfigurationServiceImpl cfs = new IdentityUIConfigurationServiceImpl(){
         protected InputStream getResource(String path) throws IOException
         {
            path = "conf/configuration1.xml";
            return super.getResource(path);
         }
      };
      assertNotNull(cfs);
      
      // Set MockUserProfileModule
      cfs.setUserProfileModule(this.userProfileModule);
      
      try
      {
         cfs.startService();
      }
      catch(Exception e)
      {
         e.printStackTrace();
         fail("could not start configuration service ... ");
      }
      assertNotNull(cfs);
      
      // Check basic configuration values
      IdentityUIConfiguration cf = cfs.getConfiguration();
      assertNotNull(cf);
      assertEquals("automatic", cf.getSubscriptionMode());
      // admin subscription mode 
      assertEquals("automatic", cf.getAdminSubscriptionMode());
      assertEquals(false, cf.isOverwriteWorkflow());
      assertEquals("jboss.org", cf.getEmailDomain());
      assertEquals("do-no-reply@jboss.com", cf.getEmailFrom());
      assertEquals("testCharacters", cf.getPasswordGenerationCharacters());
      
      List defaultRoles = cf.getDefaultRoles();
      assertNotNull(defaultRoles);
      assertEquals(2, defaultRoles.size());
      assertEquals("User", defaultRoles.get(0));
      assertEquals("Admin", defaultRoles.get(1));      
      
      // Check givenname
      UIComponentConfiguration givenname = (UIComponentConfiguration)cf.getUiComponents().get("givenname");
      assertNotNull(givenname);
      assertEquals("givenname", givenname.getName());
      assertEquals(false, givenname.isRequired());
      // Must be overwritten from the mock property info
      assertEquals(true, givenname.isReadOnly());
      try
      {
         assertEquals(String.class, givenname.getPropertyClass());
      }
      catch (ClassNotFoundException e)
      {
         e.printStackTrace();
         fail();
      }
      
      // Check familyname - converter
      UIComponentConfiguration familyname = (UIComponentConfiguration)cf.getUiComponents().get("familyname");
      assertNotNull(familyname);
      assertEquals("familyname", familyname.getName());
      assertEquals("FamilyNameConverter", familyname.getConverter());
      // Value from the xml must be overwritten from the mock property info
      assertEquals(true, familyname.isRequired());
      assertEquals(false, familyname.isReadOnly());
      
      // Check locale and predefined map values 
      UIComponentConfiguration locale = (UIComponentConfiguration)cf.getUiComponents().get("locale");
      assertNotNull(locale);
      assertEquals("locale", locale.getName());
      assertEquals("org.jboss.portal.core.identity.locale", locale.getPredefinedMapValues());
      assertEquals(false, locale.isRequired());
      
      // Check email - required - validator
      UIComponentConfiguration email = (UIComponentConfiguration)cf.getUiComponents().get("email");
      assertNotNull(email);
      assertEquals("email", email.getName());
      assertEquals("EmailValidator", email.getValidator());
      assertEquals("EmailValidator", email.getValidators().get(0));
      assertEquals(true, email.isRequired());
      
      UIComponentConfiguration interests = (UIComponentConfiguration)cf.getUiComponents().get("interests");
      assertNotNull(interests);
      Map values = interests.getValues();
      assertEquals("snowboarding", (String) values.get("board"));
      assertEquals("skiing", (String) values.get("ski"));
      assertEquals("sledging", (String) values.get("sledge"));
      
   }

}

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
package org.jboss.portal.test.core;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class PageTestCase extends TestCase
{

   public PageTestCase(String name)
   {
      super(name);
   }

//   private PageDescriptorRepository descriptorRepository;
//
//   protected void setUp() throws Exception
//   {
//      descriptorRepository = new PageDescriptorRepositoryImpl(null); // fixme
//   }
//
//   protected void tearDown() throws Exception
//   {
//      descriptorRepository = null;
//   }
//
//   public void testDefaultPage()
//   {
//      assertNotNull(descriptorRepository.getDefault());
//   }
//
//   public void testSinglePage()
//   {
//      try
//      {
//         PageDescriptor pageDescriptor = descriptorRepository.create("page");
//         assertEquals("page", pageDescriptor.getKey());
//         assertEquals(pageDescriptor, descriptorRepository.getByName("page"));
//         try
//         {
//            descriptorRepository.create("page");
//            fail("Should not be capable to create two page with the same name");
//         }
//         catch (DuplicatePageNameException expected)
//         {
//         }
//         descriptorRepository.destroy("page");
//         assertNull(descriptorRepository.getByName("page"));
//      }
//      catch (DuplicatePageNameException e)
//      {
//         fail("Cannot create page");
//      }
//   }
//
//   public void testPageWithWindows() throws DuplicatePageNameException
//   {
//      PageDescriptor pageDescriptor = descriptorRepository.create("page");
//      Window window1 = new Window("window1")
//      {
//      };
//      Window window2 = new Window("window2")
//      {
//      };
//      try
//      {
//         pageDescriptor.addWindowID(window1);
//         pageDescriptor.addWindowID(window2);
//      }
//      catch (DuplicateWindowException unexpected)
//      {
//         fail();
//      }
//      try
//      {
//         pageDescriptor.addWindowID(window1);
//         fail();
//      }
//      catch (DuplicateWindowException e)
//      {
//      }
//      assertEquals(window1, pageDescriptor.getWindow(window1.getKey()));
//      assertEquals(window2, pageDescriptor.getWindow(window2.getKey()));
//      pageDescriptor.setDefaultWindow(window1.getKey());
//      assertEquals(window1, pageDescriptor.getDefaultWindow());
//      descriptorRepository.destroy("page");
//   }


}

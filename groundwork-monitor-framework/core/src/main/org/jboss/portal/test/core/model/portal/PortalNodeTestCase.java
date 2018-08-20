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
package org.jboss.portal.test.core.model.portal;

import junit.framework.TestSuite;
import org.jboss.portal.api.node.PortalNode;
import org.jboss.portal.common.junit.TransactionAssert;
import org.jboss.portal.core.impl.api.node.PortalNodeImpl;
import org.jboss.portal.core.model.portal.Context;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.security.RoleSecurityBinding;
import org.jboss.portal.security.spi.provider.AuthorizationDomain;
import org.jboss.portal.security.spi.provider.DomainConfigurator;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8883 $
 */
public class PortalNodeTestCase extends AbstractPortalObjectContainerTestCase
{

   public static TestSuite suite() throws Exception
   {
      return AbstractPortalObjectContainerTestCase.suite(PortalNodeTestCase.class);
   }


   public void setUp() throws Exception
   {
      super.setUp();

      //
      TransactionAssert.beginTransaction();
      Context root = container.getContext("");
      Portal p_1 = root.createPortal("1");
      Page p_1_1 = p_1.createPage("1");
      Page p_1_2 = p_1.createPage("2");
      p_1_1.createPage("1");
      p_1_1.createPage("2");
      p_1_1.createPage("3");
      p_1_1.createPage("4");
      TransactionAssert.commitTransaction();
   }

   public void testGetChildrenWithViewPermission() throws Exception
   {
      TransactionAssert.beginTransaction();
      AuthorizationDomain auth = container.getAuthorizationDomain();
      DomainConfigurator cfg = auth.getConfigurator();
      Portal p_1 = (Portal)container.getObject(PortalObjectId.parse("/1", PortalObjectPath.CANONICAL_FORMAT));
      cfg.setSecurityBindings(p_1.getId().toString(PortalObjectPath.CANONICAL_FORMAT), Collections.singleton(new RoleSecurityBinding("view", "admin")));
      TransactionAssert.commitTransaction();

      //
      TransactionAssert.beginTransaction();
      p_1 = (Portal)container.getObject(PortalObjectId.parse("/1", PortalObjectPath.CANONICAL_FORMAT));
      PortalNode node = new PortalNodeImpl(getAuthorizationManager(), p_1);
      assertEquals(0, node.getChildren().size());
      TransactionAssert.commitTransaction();

      //
      setUpSubjectForRole("blah", new String[]{"admin"});

      //
      TransactionAssert.beginTransaction();
      p_1 = (Portal)container.getObject(PortalObjectId.parse("/1", PortalObjectPath.CANONICAL_FORMAT));
      node = new PortalNodeImpl(getAuthorizationManager(), p_1);
      assertEquals(0, node.getChildren().size());
      TransactionAssert.commitTransaction();
   }

   public void testGetChildrenWithViewRecursivePermission() throws Exception
   {
      TransactionAssert.beginTransaction();
      AuthorizationDomain auth = container.getAuthorizationDomain();
      DomainConfigurator cfg = auth.getConfigurator();
      Portal tmp = (Portal)container.getObject(PortalObjectId.parse("/1", PortalObjectPath.CANONICAL_FORMAT));
      cfg.setSecurityBindings(tmp.getId().toString(PortalObjectPath.CANONICAL_FORMAT), Collections.singleton(new RoleSecurityBinding("viewrecursive", "admin")));
      TransactionAssert.commitTransaction();

      //
      TransactionAssert.beginTransaction();
      tmp = (Portal)container.getObject(PortalObjectId.parse("/1", PortalObjectPath.CANONICAL_FORMAT));
      PortalNode node_1 = new PortalNodeImpl(getAuthorizationManager(), tmp);
      assertEquals(0, node_1.getChildren().size());
      TransactionAssert.commitTransaction();

      //
      setUpSubjectForRole("blah", new String[]{"admin"});

      //
      TransactionAssert.beginTransaction();
      tmp = (Portal)container.getObject(PortalObjectId.parse("/1", PortalObjectPath.CANONICAL_FORMAT));
      node_1 = new PortalNodeImpl(getAuthorizationManager(), tmp);
      Map node_1_children = getChildrenMap(node_1);
      assertEquals(2, node_1_children.size());
      PortalNode node_1_1 = (PortalNode)node_1_children.get("1");
      PortalNode node_1_2 = (PortalNode)node_1_children.get("2");
      assertNotNull(node_1_1);
      assertNotNull(node_1_2);
      Map node_1_1_children = getChildrenMap(node_1_1);
      assertEquals(4, node_1_1_children.size());
      assertNotNull(node_1_1_children.get("1"));
      assertNotNull(node_1_1_children.get("2"));
      assertNotNull(node_1_1_children.get("3"));
      assertNotNull(node_1_1_children.get("4"));
      Map node_1_2_children = getChildrenMap(node_1_2);
      assertEquals(0, node_1_2_children.size());
      TransactionAssert.commitTransaction();
   }

   private Map getChildrenMap(PortalNode node)
   {
      Map p_1_children = new TreeMap();
      for (Iterator i = node.getChildren().iterator(); i.hasNext();)
      {
         PortalNode child = (PortalNode)i.next();
         p_1_children.put(child.getName(), child);
      }
      return p_1_children;
   }
}

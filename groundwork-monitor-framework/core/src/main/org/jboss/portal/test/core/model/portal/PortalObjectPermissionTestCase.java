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

import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.core.model.portal.PortalObjectPermission;
import org.jboss.portal.identity.auth.UserPrincipal;
import org.jboss.portal.security.PortalPermission;
import org.jboss.portal.security.PortalPermissionCollection;
import org.jboss.portal.security.PortalSecurityException;
import org.jboss.portal.security.RoleSecurityBinding;
import org.jboss.portal.security.impl.jacc.JACCPortalPermissionCollection;
import org.jboss.portal.security.spi.provider.AuthorizationDomain;
import org.jboss.portal.security.spi.provider.PermissionFactory;
import org.jboss.portal.test.security.BaseAuthorizationDomain;
import org.jboss.portal.test.security.PortalPermissionTestCase;
import org.jboss.portal.test.security.Server;
import org.jboss.security.SimplePrincipal;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class PortalObjectPermissionTestCase extends PortalPermissionTestCase
{

   public PortalObjectPermissionTestCase(String name)
   {
      super(name);
   }

   public void testImplies1()
   {
      PortalObjectPermission v = new PortalObjectPermission(new PortalObjectId("", new PortalObjectPath(new String[]{"abc"})), "view");
      PortalObjectPermission vr = new PortalObjectPermission(new PortalObjectId("", new PortalObjectPath(new String[]{"abc"})), "viewrecursive");
      assertTrue(v.implies(v));
      assertFalse(v.implies(vr));
      assertTrue(vr.implies(v));
      assertTrue(vr.implies(vr));
   }

   public void testImplies2()
   {
      PortalObjectPermission v = new PortalObjectPermission(new PortalObjectId("", new PortalObjectPath(new String[]{"abc"})), "view");
      PortalObjectPermission vr = new PortalObjectPermission(new PortalObjectId("", new PortalObjectPath(new String[]{"abc", "def"})), "viewrecursive");
      assertTrue(v.implies(v));
      assertFalse(v.implies(vr));
      assertFalse(vr.implies(v));
      assertTrue(vr.implies(vr));
   }

   public void testImplies3()
   {
      PortalObjectPermission v = new PortalObjectPermission(new PortalObjectId("", new PortalObjectPath(new String[]{"abc"})), "viewrecursive");
      PortalObjectPermission vr = new PortalObjectPermission(new PortalObjectId("", new PortalObjectPath(new String[]{"abc", "def"})), "view");
      assertTrue(v.implies(v));
      assertTrue(v.implies(vr));
      assertFalse(vr.implies(v));
      assertTrue(vr.implies(vr));
   }

   public void testDashboard1() throws Exception
   {
      PortalObjectPermission v = new PortalObjectPermission(new PortalObjectId("", new PortalObjectPath(new String[0])), "dashboard");
      PortalObjectPermission v1 = new PortalObjectPermission(new PortalObjectId("", new PortalObjectPath(new String[]{"abc"})), "view");
      PortalObjectPermission v2 = new PortalObjectPermission(new PortalObjectId("", new PortalObjectPath(new String[]{"abc", "def"})), "view");
      PortalObjectPermission v3 = new PortalObjectPermission(new PortalObjectId("", new PortalObjectPath(new String[]{"def"})), "view");
      PortalObjectPermission v4 = new PortalObjectPermission(new PortalObjectId("", new PortalObjectPath(new String[]{"def", "ghi"})), "view");

      Subject abc = new Subject();
      abc.getPrincipals().add(new UserPrincipal("abc"));

      Subject foo = new Subject();
      foo.getPrincipals().add(new UserPrincipal("foo"));

      assertTrue(v.implies(v1, abc));
      assertFalse(v1.implies(v, abc));
      assertTrue(v.implies(v2, abc));
      assertFalse(v2.implies(v, abc));
      assertFalse(v.implies(v3, abc));
      assertFalse(v3.implies(v, abc));
      assertFalse(v.implies(v4, abc));
      assertFalse(v4.implies(v, abc));

      assertFalse(v.implies(v1));
      assertFalse(v1.implies(v));
      assertFalse(v.implies(v2));
      assertFalse(v2.implies(v));
      assertFalse(v.implies(v3));
      assertFalse(v3.implies(v));
      assertFalse(v.implies(v4));
      assertFalse(v4.implies(v));

      assertFalse(v.implies(v1, foo));
      assertFalse(v1.implies(v, foo));
      assertFalse(v.implies(v2, foo));
      assertFalse(v2.implies(v, foo));
      assertFalse(v.implies(v3, foo));
      assertFalse(v3.implies(v, foo));
      assertFalse(v.implies(v4, foo));
      assertFalse(v4.implies(v, foo));
   }

   public void testDashboard2() throws Exception
   {
      PortalObjectPermission v = new PortalObjectPermission(new PortalObjectId("", new PortalObjectPath(new String[]{"abc"})), "dashboard");
      PortalObjectPermission v1 = new PortalObjectPermission(new PortalObjectId("", new PortalObjectPath(new String[]{"abc", "def"})), "view");
      PortalObjectPermission v2 = new PortalObjectPermission(new PortalObjectId("", new PortalObjectPath(new String[]{"abc", "def", "ghi"})), "view");
      PortalObjectPermission v3 = new PortalObjectPermission(new PortalObjectId("", new PortalObjectPath(new String[]{"jkl"})), "view");
      PortalObjectPermission v4 = new PortalObjectPermission(new PortalObjectId("", new PortalObjectPath(new String[]{"jkl", "mno"})), "view");

      Subject def = new Subject();
      def.getPrincipals().add(new UserPrincipal("def"));

      Subject foo = new Subject();
      foo.getPrincipals().add(new UserPrincipal("foo"));

      assertTrue(v.implies(v1, def));
      assertFalse(v1.implies(v, def));
      assertTrue(v.implies(v2, def));
      assertFalse(v2.implies(v, def));
      assertFalse(v.implies(v3, def));
      assertFalse(v3.implies(v, def));
      assertFalse(v.implies(v4, def));
      assertFalse(v4.implies(v, def));

      assertFalse(v.implies(v1));
      assertFalse(v1.implies(v));
      assertFalse(v.implies(v2));
      assertFalse(v2.implies(v));
      assertFalse(v.implies(v3));
      assertFalse(v3.implies(v));
      assertFalse(v.implies(v4));
      assertFalse(v4.implies(v));

      assertFalse(v.implies(v1, foo));
      assertFalse(v1.implies(v, foo));
      assertFalse(v.implies(v2, foo));
      assertFalse(v2.implies(v, foo));
      assertFalse(v.implies(v3, foo));
      assertFalse(v3.implies(v, foo));
      assertFalse(v.implies(v4, foo));
      assertFalse(v4.implies(v, foo));
   }

   /** . */
   private AuthorizationDomain domain;

   public void setUp() throws Exception
   {
      super.setUp();

      domain = new BaseAuthorizationDomain()
      {
         public String getType()
         {
            return "portalobject";
         }

         public PermissionFactory getPermissionFactory()
         {
            return new PermissionFactory()
            {
               public PortalPermission createPermissionContainer(PortalPermissionCollection collection) throws PortalSecurityException
               {
                  return new PortalObjectPermission(collection);
               }

               public PortalPermission createPermission(String uri, String action) throws PortalSecurityException
               {
                  PortalObjectId id = PortalObjectId.parse(uri, PortalObjectPath.CANONICAL_FORMAT);
                  return new PortalObjectPermission(id, action);
               }

               public PortalPermission createPermission(String uri, Collection actions) throws PortalSecurityException
               {
                  PortalObjectId id = PortalObjectId.parse(uri, PortalObjectPath.CANONICAL_FORMAT);
                  return new PortalObjectPermission(id, actions);
               }
            };
         }
      };

      // Add a container for rhe admin role
      PortalPermissionCollection collection = new JACCPortalPermissionCollection("admin", domain);
      PortalObjectPermission container = new PortalObjectPermission(collection);
      addContainerToRole("admin", container);
   }

   public void testDomainImplies1() throws Exception
   {
      //
      domain.getConfigurator().setSecurityBindings("/", Collections.singleton(new RoleSecurityBinding("viewrecursive", "admin")));

      //
      server.execute(new Server.Task()
      {
         public void execute() throws Exception
         {
            Principal[] principals = new Principal[]{new SimplePrincipal("admin")};
            assertTrue(implies(new PortalObjectPermission(new PortalObjectId("", new PortalObjectPath(new String[0])), "view"), principals));
            assertTrue(implies(new PortalObjectPermission(new PortalObjectId("", new PortalObjectPath(new String[]{"default"})), "view"), principals));
            assertTrue(implies(new PortalObjectPermission(new PortalObjectId("", new PortalObjectPath(new String[]{"default", "default"})), "view"), principals));
         }
      });
   }

   public void testDomainImplies2() throws Exception
   {
      //
      domain.getConfigurator().setSecurityBindings("/", Collections.singleton(new RoleSecurityBinding("view", "admin")));

      //
      server.execute(new Server.Task()
      {
         public void execute() throws Exception
         {
            Principal[] principals = new Principal[]{new SimplePrincipal("admin")};
            assertTrue(implies(new PortalObjectPermission(new PortalObjectId("", new PortalObjectPath(new String[0])), "view"), principals));
            assertFalse(implies(new PortalObjectPermission(new PortalObjectId("", new PortalObjectPath(new String[]{"default"})), "view"), principals));
            assertFalse(implies(new PortalObjectPermission(new PortalObjectId("", new PortalObjectPath(new String[]{"default", "default"})), "view"), principals));
         }
      });
   }
}

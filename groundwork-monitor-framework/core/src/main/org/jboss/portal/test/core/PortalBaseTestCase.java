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

/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.portal.test.core;

import org.jboss.portal.security.RoleSecurityBinding;
import org.jboss.portal.security.SecurityConstants;
import org.jboss.portal.test.framework.AbstractPortalTestCase;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.jacc.DelegatingPolicy;

import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import java.security.Policy;
import java.security.Principal;
import java.security.acl.Group;
import java.util.HashSet;
import java.util.Set;

/**
 * Base Test Case for Portal Core Contains common methods used by the test framework
 *
 * @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 * @version $Revision: 8786 $
 * @since Apr 7, 2006
 */
public abstract class PortalBaseTestCase extends AbstractPortalTestCase
{

   protected void setUp() throws Exception
   {
      super.setUp();

      // Security
      PolicyContext.setContextID("ctxid");
   }

   protected void tearDown() throws Exception
   {
      /**
       * Need to delete the context information set in the policy provider
       * for this test as it will affect the next test that is being run.
       * The alternative is that each test sets its own context id.
       */
      DelegatingPolicy p = (DelegatingPolicy)Policy.getPolicy();
      p.delete("ctxid");
      p.refresh();
      unsetSubjectForRole();
      super.tearDown();
   }

   protected Set getDefaultSecurityConstraints()
   {
      Set constraints = new HashSet();
      constraints.add(new RoleSecurityBinding("view", SecurityConstants.UNCHECKED_ROLE_NAME));
      return constraints;
   }

   protected Set getSecurityConstraints(String perm, String role)
   {
      Set constraints = new HashSet();
      constraints.add(new RoleSecurityBinding(perm, role));
      return constraints;
   }

   protected void installDefaultPolicyProvider()
   {
      // Setup custom policy
   }

   protected void setUpSubjectForRole(String username, String[] roleNames) throws Exception
   {
      Group roleGroup = new SimpleGroup("Roles");
      for (int i = 0; i < roleNames.length; i++)
      {
         String roleName = roleNames[i];
         Principal rolePrincipal = new SimplePrincipal(roleName);
         roleGroup.addMember(rolePrincipal);
      }
      Subject subject = new Subject();
      subject.getPrincipals().add(roleGroup);
      SecurityAssociation.setSubject(subject);
   }

   protected void unsetSubjectForRole() throws Exception
   {
      SecurityAssociation.setSubject(null);
   }

}

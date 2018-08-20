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
package org.jboss.portal.core.impl.portlet.state;

import org.jboss.portal.portlet.state.StateManagementPolicy;
import org.jboss.portal.registration.Registration;
import org.jboss.portal.registration.RegistrationLocal;

/**
 * An implementation that rely on thread local context to make up its decision. State is locally persisted only for the
 * local portal or for wsrp invocations in the context of a registration.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class StateManagementPolicyImpl implements StateManagementPolicy
{
   public boolean persistLocally()
   {
      // Otherwise the optional registration from the wsrp layer
      Registration registration = RegistrationLocal.getRegistration();

      // Persist locally if we are in the scope of an existing registration
      if (registration != null)
      {
         return true;
      }

      // Otherwise if this is a local call we persist locally
      return LocalPortletInvoker.isLocal();
   }
}

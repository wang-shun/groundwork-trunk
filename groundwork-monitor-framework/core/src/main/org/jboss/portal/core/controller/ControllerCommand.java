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
package org.jboss.portal.core.controller;

import org.jboss.logging.Logger;
import org.jboss.portal.common.invocation.Invocation;
import org.jboss.portal.common.invocation.InvocationContext;
import org.jboss.portal.common.invocation.InvocationException;
import org.jboss.portal.common.invocation.InvocationHandler;
import org.jboss.portal.common.invocation.Scope;
import org.jboss.portal.core.controller.command.info.CommandInfo;
import org.jboss.portal.security.PortalSecurityException;
import org.jboss.portal.security.spi.auth.PortalAuthorizationManager;

/**
 * A controller command.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public abstract class ControllerCommand extends Invocation
{

   /** . */
   public static final Scope PRINCIPAL_SCOPE = Scope.PRINCIPAL_SCOPE;

   /** . */
   public static final Scope SESSION_SCOPE = Scope.SESSION_SCOPE;

   /** . */
   public static final Scope REQUEST_SCOPE = Scope.REQUEST_SCOPE;

   /** . */
   public static final Scope NAVIGATIONAL_STATE_SCOPE = new Scope("navigationalstate");

   /** . */
   protected static Logger log = Logger.getLogger(ControllerCommand.class);

   /** The context of the command. */
   protected ControllerContext context;

   /** Execute command when the end of the stack is reached. */
   private static final InvocationHandler handler = new InvocationHandler()
   {
      public Object invoke(Invocation invocation) throws Exception, InvocationException
      {
         ControllerCommand cmd = (ControllerCommand)invocation;
         return cmd.execute();
      }
   };

   protected ControllerCommand()
   {
      setHandler(handler);
   }

   /** Return the meta data of this command. */
   public abstract CommandInfo getInfo();

   public final InvocationContext getContext()
   {
      if (context == null)
      {
         throw new IllegalStateException();
      }
      return context;
   }

   public final ControllerContext getControllerContext()
   {
      return context;
   }

   /**
    * Enforce the security on this command.
    *
    * @throws PortalSecurityException
    * @throws org.jboss.portal.core.controller.SecurityException
    *
    */
   public void enforceSecurity(PortalAuthorizationManager pam) throws SecurityException
   {
   }

   public void acquireResources() throws NoSuchResourceException
   {
   }

   public void releaseResources()
   {
   }

   /** Contextualize the command. */
   public final void createContext(ControllerContext context) throws ControllerException
   {
      this.context = context;

      //
      create();
   }

   /** Destroy state after invocation. */
   public final void destroyContext()
   {
      try
      {
         destroy();
      }
      finally
      {
         this.context = null;
      }
   }

   protected void create()
   {
   }

   protected void destroy()
   {
   }

   /** Execute the command. */
   public abstract ControllerResponse execute() throws ControllerException;

   public static void rethrow(Exception e) throws ControllerException, InvocationException, RuntimeException
   {
      if (e instanceof InvocationException)
      {
         throw (InvocationException)e;
      }
      if (e instanceof ControllerException)
      {
         throw (ControllerException)e;
      }
      if (e instanceof RuntimeException)
      {
         throw (RuntimeException)e;
      }
      throw new ControllerException(e);
   }
}

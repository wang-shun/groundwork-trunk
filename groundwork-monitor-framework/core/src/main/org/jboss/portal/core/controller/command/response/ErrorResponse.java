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
package org.jboss.portal.core.controller.command.response;

import org.jboss.portal.core.controller.ControllerResponse;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class ErrorResponse extends ControllerResponse
{

   /** The optional cause of the error. */
   private final Throwable cause;

   /** The optional error message. */
   private final String message;

   /** True if the error is considered as an internal error. */
   private final boolean internal;

   public ErrorResponse(Throwable cause, boolean internal)
   {
      this.cause = cause;
      this.message = cause != null ? cause.getMessage() : null;
      this.internal = internal;
   }

   public ErrorResponse(String message, boolean internal)
   {
      this.cause = null;
      this.message = message;
      this.internal = internal;
   }

   public ErrorResponse(boolean internal)
   {
      this.cause = null;
      this.message = null;
      this.internal = internal;
   }

   /**
    * Returns the optional error.
    *
    * @return the error
    */
   public Throwable getCause()
   {
      return cause;
   }

   /**
    * Returns the optional error message.
    *
    * @return the message
    */
   public String getMessage()
   {
      return message;
   }

   /**
    * Returns true if the error is considered as internal.
    *
    * @return true if the error is internal
    */
   public boolean isInternal()
   {
      return internal;
   }

   public String toString()
   {
      return "ErrorResponse[internal=" + internal + "]";
   }
}

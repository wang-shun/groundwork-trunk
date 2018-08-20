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
package org.jboss.portal.core.model.portal.control;

/**
 * Defines the constant for the control framework.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class ControlConstants
{

   private ControlConstants()
   {
   }

   /** . */
   public static final String PAGE_ACCESS_DENIED_CONTROL_KEY = "control.page.access_denied";

   /** . */
   public static final String PAGE_UNAVAILABLE_CONTROL_KEY = "control.page.unavailable";

   /** . */
   public static final String PAGE_ERROR_CONTROL_KEY = "control.page.error";

   /** . */
   public static final String PAGE_INTERNAL_ERROR_CONTROL_KEY = "control.page.internal_error";

   /** . */
   public static final String PAGE_NOT_FOUND_CONTROL_KEY = "control.page.not_found";

   /** . */
   public static final String PAGE_RESOURCE_URI_CONTROL_KEY = "control.page.resource_uri";

   /** . */
   public static final String PORTAL_ACCESS_DENIED_CONTROL_KEY = "control.portal.access_denied";

   /** . */
   public static final String PORTAL_UNAVAILABLE_CONTROL_KEY = "control.portal.unavailable";

   /** . */
   public static final String PORTAL_ERROR_CONTROL_KEY = "control.portal.error";

   /** . */
   public static final String PORTAL_INTERNAL_ERROR_CONTROL_KEY = "control.portal.internal_error";

   /** . */
   public static final String PORTAL_NOT_FOUND_CONTROL_KEY = "control.portal.not_found";

   /** . */
   public static final String PORTAL_RESOURCE_URI_CONTROL_KEY = "control.portal.resource_uri";

   /** . */
   public static final String HIDE_CONTROL_VALUE = "hide";

   /** . */
   public static final String IGNORE_CONTROL_VALUE = "ignore";

   /** . */
   public static final String JSP_CONTROL_VALUE = "jsp";

   /** . */
   public static final String ERROR_TYPE_ATTRIBUTE = "org.jboss.portal.control.ERROR_TYPE";

   /** . */
   public static final String CAUSE_ATTRIBUTE = "org.jboss.portal.control.CAUSE";

   /** . */
   public static final String MESSAGE_ATTRIBUTE = "org.jboss.portal.control.MESSAGE";

   /** . */
   public static final String ACCESS_DENIED_ERROR_TYPE = "ACCESS_DENIED";

   /** . */
   public static final String UNAVAILABLE_ERROR_TYPE = "UNAVAILABLE";

   /** . */
   public static final String ERROR_ERROR_TYPE = "ERROR";

   /** . */
   public static final String INTERNAL_ERROR_ERROR_TYPE = "INTERNAL_ERROR";

   /** . */
   public static final String NOT_FOUND_ERROR_TYPE = "NOT_FOUND";

}

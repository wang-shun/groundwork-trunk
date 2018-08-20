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
package org.jboss.portal.core;

import javax.xml.namespace.QName;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11745 $
 */
public final class CoreConstants
{

   /** Servlet constants. */
   public static final class Servlet
   {

      /** Servlet session constants. */
      public static final class Session
      {
         /** . */
         public static final String USER_FINALIZER = "jboss.portal.user_finalizer";
      }

      /** Servlet request constants. */
      public static final class Request
      {

         /** . */
         public static final String PORTAL_CONTEXT_PATH = "jboss.portal.context_path";

         /** . */
         public static final String PORTAL_SERVLET_PATH = "jboss.portal.servlet_path";
      }
   }

   public final static String JBOSS_PORTAL_NAMESPACE = "urn:jboss:portal";
   
   /**
    * SignOut event
    */
   public static final QName JBOSS_PORTAL_SIGN_OUT = new QName(JBOSS_PORTAL_NAMESPACE, "signOut");

   /**
    * The namespace for JBoss Portal content integration framework.
    */
   public static final String JBOSS_PORTAL_CONTENT_NAMESPACE = JBOSS_PORTAL_NAMESPACE + ":content";

   public static final QName JBOSS_PORTAL_CONTENT_URI = new QName(JBOSS_PORTAL_CONTENT_NAMESPACE, "uri");

   public static final QName JBOSS_PORTAL_CONTENT_PARAMETERS = new QName(JBOSS_PORTAL_CONTENT_NAMESPACE, "parameters");

   /**
    *  The namespace for the page integration.
    */
   public static final String JBOSS_PORTAL_PAGE_NAMESPACE = JBOSS_PORTAL_NAMESPACE + ":page";

   /**
    * This name can be used as a page parameter. It denotes a special parameter which is the page title.
    *
    * @todo implement it
    */
   public static final QName JBOSS_PORTAL_PAGE_TITLE = new QName(JBOSS_PORTAL_PAGE_NAMESPACE, "title");


}

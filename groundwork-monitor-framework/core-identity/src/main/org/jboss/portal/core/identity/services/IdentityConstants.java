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
package org.jboss.portal.core.identity.services;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class IdentityConstants
{
   /** The command factory url */
   public static final String VALIDATE_EMAIL = "validateEmail";
   
   /** Salt */
   public static final String HASH_SALT = "fuDrupRunEP2BRuspADr";
   
   /** Automatic subscription mode */
   public static final String SUBSCRIPTION_MODE_AUTOMATIC = "automatic";

   /** jBPM validate email process */
   public final static String jbp_identity_validate_email_process_name = "jbp_identity_validate_email";
   
   /** jBPM nodes */
   public final static String JBPM_NODE_EMAIL_VALIDATION = "validate_email";
   public final static String JBPM_NODE_APPROVAL = "admin_approval";
   public final static String JBPM_TRANSITION_VALIDATED = "validated";
   public final static String JBPM_TRANSITION_REJECTED ="rejected";
   public final static String JBPM_TRANSITION_APPROVED = "approved";

   /** Static predefined component values */
   public static final String COMPONENT_VALUE_LOCALE = "org.jboss.portal.core.identity.locale";
   public static final String COMPONENT_VALUE_THEME = "org.jboss.portal.core.identity.theme";
   public static final String COMPONENT_VALUE_TIMEZONE = "org.jboss.portal.core.identity.timezone";
   
   /** Available actions */
   public static final String ACTION = "action";
   public static final String ACTION_REGISTER_USER = "register";
   public static final String ACTION_CHANGE_EMAIL = "changeEmail";
   public static final String ACTION_LOST_PASSWORD = "lostPassword";
   
   /** Registration and validation status */
   public static final String REGISTRATION_REGISTERED = "registered";
   public static final String REGISTRATION_PENDING = "registration_pending";
   public static final String REGISTRATION_FAILED = "registration_failed";
   public static final String VALIDATION_FAILED = "validation_failed";
   public static final String VALIDATION_VALIDATED = "validated";
   public static final String VALIDATION_ERROR = "validation_error";
   
   
   /** jBPM process variables */
   public static final String PORTAL_URL = "portalURL"; 
   public static final String VALIDATION_HASH = "validationHash";
   public static final String VARIABLE_LOCALE = "locale";
   public static final String VARIABLE_EMAIL = "email";
   public static final String VARIABLE_USER = "user";
   
   /** Email constants */
   public static final String EMAIL_TO = "to";
   public static final String EMAIL_FROM = "from";
   public static final String EMAIL_DOMAIN = "emailDomain";
   public static final String EMAIL_TEXT = "emailText";
   
   /** Default language */
   public static final String DEFAULT_LOCALE = "en";
   
   /** Default role */
   public static final String DEFAULT_ROLE = "User";
   
   /** Bundle prefix for dynamic value localization */
   public static final String DYNAMIC_VALUE_PREFIX = "IDENTITY_DYNAMIC_VALUE_";
   
}


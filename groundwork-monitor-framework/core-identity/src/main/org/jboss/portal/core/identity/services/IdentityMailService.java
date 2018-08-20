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

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import freemarker.template.TemplateException;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public interface IdentityMailService
{
   /**
    * Generates and sends an email based on a template.
    * 
    * @param the template location (directory)
    * @param a map required for the template
    * @param the requested locale
    * @throws IOException
    * @throws TemplateException
    */
   void sendMail(String templateLocation, Map<String, String> mailData, Locale locale) throws IOException, TemplateException;
   
   /**
    * Sends an email
    * 
    * @param email from
    * @param email to
    * @param subject
    * @param email body
    * @throws IllegalArgumentException
    */
   public void send(String emailFrom, String emailTo, String subject, String emailText);
}


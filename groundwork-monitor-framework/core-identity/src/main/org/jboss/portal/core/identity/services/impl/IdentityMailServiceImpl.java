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
package org.jboss.portal.core.identity.services.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.jboss.logging.Logger;
import org.jboss.portal.common.io.IOTools;
import org.jboss.portal.core.identity.services.IdentityConstants;
import org.jboss.portal.core.identity.services.IdentityMailService;
import org.jboss.portal.core.identity.services.metadata.IdentityUIConfiguration;
import org.jboss.portal.core.identity.services.metadata.IdentityUIConfigurationService;
import org.jboss.portal.core.modules.MailModule;
import org.jboss.portal.jems.as.JNDI;
import org.jboss.portal.jems.as.system.AbstractJBossService;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class IdentityMailServiceImpl extends AbstractJBossService implements IdentityMailService
{
   
   /** The e-mail domain. */
   private String emailDomain = null;

   /** E-Mail from. */
   private String emailFrom = null;
   
   /** The mail module. */
   private MailModule mailModule;
   
   /** The template path. */
   private static final String TEMPLATE_PATH = "conf/templates/";
   
   /** The template prefix. */
   private static final String TEMPLATE_PREFIX = "/emailTemplate";
   
   /** The core-identity configuration service. */
   private IdentityUIConfigurationService identityUIConfigurationService;
   
   /** The Bundle prefix for an unknown action. */
   public static final String UNKOWN_ACTION_PREFIX = "IDENTITY_MAIL_SUBJECT_CUSTOM_";

   /** The logger */
   private static final Logger log = Logger.getLogger(IdentityMailServiceImpl.class);
   
   /** The JNDI binding */
   private JNDI.Binding jndiBinding;

   /** The JNDI name */
   private String jndiName = null;

   public void startService() throws Exception
   {
      super.startService();
      
      IdentityUIConfiguration cf = this.identityUIConfigurationService.getConfiguration();
      // Set mail attributes
      this.setEmailDomain(cf.getEmailDomain());
      this.setEmailFrom(cf.getEmailFrom());
      
      if (this.jndiName != null)
      {
         jndiBinding = new JNDI.Binding(jndiName, this);
         jndiBinding.bind();
      }
   }
   
   public void stopService() throws Exception
   {
      super.stopService();

      if (jndiBinding != null)
      {
         jndiBinding.unbind();
         jndiBinding = null;
      }
   }
   
   public String getEmailDomain()
   {
      return emailDomain;
   }

   public void setEmailDomain(String emailDomain)
   {
      if(emailDomain == null)
         throw new IllegalArgumentException("email domain may not be null.");
      
      this.emailDomain = emailDomain;
   }

   public String getEmailFrom()
   {
      return emailFrom;
   }

   public void setEmailFrom(String emailFrom)
   {
      if(emailFrom == null)
         throw new IllegalArgumentException("email from may not be null.");
      
      this.emailFrom = emailFrom;
   }

   public MailModule getMailModule()
   {
      return mailModule;
   }

   public void setMailModule(MailModule mailModule)
   {
      this.mailModule = mailModule;
   }

   public IdentityUIConfigurationService getIdentityUIConfigurationService()
   {
      return identityUIConfigurationService;
   }

   public void setIdentityUIConfigurationService(IdentityUIConfigurationService identityUIConfigurationService)
   {
      this.identityUIConfigurationService = identityUIConfigurationService;
   }

   public String getJNDIName()
   {
      return this.jndiName;
   }

   public void setJNDIName(String jndiName)
   {
      this.jndiName = jndiName;
   }
   
   public void sendMail(String templateLocation, Map<String, String> mailData, Locale locale) throws IOException, TemplateException
   {
      if(templateLocation == null)
         throw new IllegalArgumentException("template location may not be null.");
      if(mailData == null)
         throw new IllegalArgumentException("mail data may not be null.");
      if(locale == null)
         throw new IllegalArgumentException("locale may not be null.");
      
      mailData.put(IdentityConstants.EMAIL_DOMAIN, emailDomain);

      ResourceBundle bundle = ResourceBundle.getBundle("conf.bundles.Identity", locale);
      
      String subject = null;
      String to = (String) mailData.get(IdentityConstants.EMAIL_TO);
      
      if (IdentityConstants.ACTION_REGISTER_USER.equals(templateLocation))
      {
         subject = bundle.getString("IDENTITY_MAIL_SUBJECT_REGISTER");
      }
      else if (IdentityConstants.ACTION_CHANGE_EMAIL.equals(templateLocation))
      {
         subject = bundle.getString("IDENTITY_MAIL_SUBJECT_CHANGE_EMAIL");         
      }
      else if (IdentityConstants.ACTION_LOST_PASSWORD.equals(templateLocation))
      {
         subject = bundle.getString("IDENTITY_MAIL_SUBJECT_LOST_PASSWORD");
      }
      else
      {
         // In the case it's an unknown action we try to load a custom subject from the Bundle
         // where the templateLocation is the path to the folder containing the email templates
         // IDENTITY_MAIL_SUBJECT_CUSTOM_templateLocation 
         subject = bundle.getString(UNKOWN_ACTION_PREFIX + templateLocation);
         
         // No null subject allowed
         if(subject == null)
            throw new IllegalArgumentException("No custom mail subject found (Unknown Template). ");
      }

      // Generating message
      String emailText = generateEmailText(templateLocation, mailData, locale);
      // log.debug(this.emailText);
      
      // Sending mail
      send(emailFrom, to , subject, emailText);

   }

   public void send(String emailFrom, String emailTo, String subject, String emailText)
   {
      if(emailFrom == null)
         throw new IllegalArgumentException("email-from may not be null.");
      if(emailTo == null)
         throw new IllegalArgumentException("email-to may not be null.");
      if(subject == null)
         throw new IllegalArgumentException("email-subject may not be null.");
      
      this.getMailModule().send(emailFrom, emailTo, subject, emailText);
   }
   
   private String generateEmailText(String templateLocation, Map<String, String> mailData, Locale locale) throws IOException, TemplateException
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      URL config = tcl.getResource(TEMPLATE_PATH + templateLocation + TEMPLATE_PREFIX + "_"  + locale.getLanguage() + "_" + locale.getCountry() + ".tpl");
      if (config == null)
      {
         config = tcl.getResource(TEMPLATE_PATH + templateLocation + TEMPLATE_PREFIX + "_"  + locale.getLanguage() + ".tpl");
      }
      if (config == null)
      {
         config = tcl.getResource(TEMPLATE_PATH + templateLocation + TEMPLATE_PREFIX + ".tpl");
      }
      if (config == null)
      {
         throw new FileNotFoundException("Cannot load a suitable email template in: " + TEMPLATE_PATH);
      }
      
      InputStream in = config.openStream();
      Template tpl = new Template("emailTemplate", new InputStreamReader(in), new Configuration());
      StringWriter out = new StringWriter();
      // Process
      tpl.process(mailData, out);
      
      IOTools.safeClose(out);
      
      return out.toString();
   }
}

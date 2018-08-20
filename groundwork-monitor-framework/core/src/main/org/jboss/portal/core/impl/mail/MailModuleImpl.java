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
package org.jboss.portal.core.impl.mail;

import EDU.oswego.cs.dl.util.concurrent.BoundedLinkedQueue;
import EDU.oswego.cs.dl.util.concurrent.Channel;
import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;
import EDU.oswego.cs.dl.util.concurrent.QueuedExecutor;
import EDU.oswego.cs.dl.util.concurrent.SynchronizedLong;
import org.apache.log4j.Logger;
import org.jboss.logging.util.LoggerStream;
import org.jboss.portal.core.modules.AbstractModule;
import org.jboss.portal.core.modules.MailModule;

import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.NoSuchProviderException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:julien@jboss.com">Julien Viet</a>
 * @author <a href="mailto:theute@jboss.com">Thomas Heute</a>
 */
public class MailModuleImpl
   extends AbstractModule
   implements MailModule
{
   private final Logger log = Logger.getLogger(getClass());

   /** Creates a new {@link MailModuleImpl} object. */
   public MailModuleImpl()
   {
   }

   /** Javamail properties. */
   private Properties properties = new Properties();

   /** Queue max capacity or -1 if unbounded. */
   private int queueCapacity = -1;

   /** The thread that will send the mail. */
   private QueuedExecutor executor;

   /** The queue that will held all the messages. */
   private Channel queue;

   /** The SMTP gateway through which mail will be delivered. */
   public String gateway;

   /** The username for authenticating to the smtp gateway. */
   private String smtpUser;

   /** The password for authenticating to the smtp gateway. */
   private String smtpPassword;

   /** The Authenticator implementation used when stmp auth is needed. */
   private MailAuthenticator smtpAuth;

   /** True if javamail debug is enabled. */
   private boolean javaMailDebugEnabled = false;

   /** SMTP connection timeout. */
   private int SMTPConnectionTimeout = 10000;

   /** SMTP timeout. */
   private int SMTPTimeout = 10000;

   /** The PrintStream java mail debug output is sent to. */
   private LoggerStream logs;

   /** A serial id used to track messages locally. */
   private final SynchronizedLong currentSerialId = new SynchronizedLong(0);

   /** Default Content Type for Mail */
   public static final String DEFAULT_CONTENT_MIME_TYPE = "text/plain";

   public int getSMTPConnectionTimeout()
   {
      return SMTPConnectionTimeout;
   }

   public void setSMTPConnectionTimeout(int SMTPConnectionTimeout)
   {
      this.SMTPConnectionTimeout = SMTPConnectionTimeout;
   }

   public int getSMTPTimeout()
   {
      return SMTPTimeout;
   }

   public void setSMTPTimeout(int SMTPTimeout)
   {
      this.SMTPTimeout = SMTPTimeout;
   }

   public long getCurrentSerialId()
   {
      return currentSerialId.get();
   }

   public String getGateway()
   {
      return gateway;
   }

   public void setGateway(String gateway)
   {
      this.gateway = gateway;
   }

   public String getSmtpUser()
   {
      return smtpUser;
   }

   public void setSmtpUser(String smtpUser)
   {
      this.smtpUser = smtpUser;
   }

   public String getSmtpPassword()
   {
      return smtpPassword;
   }

   public void setSmtpPassword(String smtpPassword)
   {
      this.smtpPassword = smtpPassword;
   }

   public int getQueueSize()
   {
      if (queue == null)
      {
         return -1;
      }
      else if (queue instanceof BoundedLinkedQueue)
      {
         return ((BoundedLinkedQueue)queue).capacity();
      }
      else
      {
         return 0;
      }
   }

   public String listProperties()
   {
      StringBuffer buffer = new StringBuffer("[");
      for (Iterator i = properties.entrySet().iterator(); i.hasNext(); buffer.append(i.hasNext() ? "," : "]"))
      {
         Map.Entry entry = (Map.Entry)i.next();
         buffer.append(entry.getKey()).append("=").append(entry.getValue());
      }

      return buffer.toString();
   }

   public int flushQueue()
   {
      try
      {
         int size = 0;
         for (MyMessage r = (MyMessage)queue.poll(0); r != null; r = (MyMessage)queue.poll(0))
         {
            log.debug("Removed serialId=" + r.serialId + " from the queue");
            size++;
         }

         return size;
      }
      catch (InterruptedException ignore)
      {
         return -1;
      }
   }

   public int getQueueCapacity()
   {
      return queueCapacity;
   }

   public void setQueueCapacity(int queueCapacity)
   {
      this.queueCapacity = queueCapacity;
   }

   public boolean getJavaMailDebugEnabled()
   {
      return javaMailDebugEnabled;
   }

   public void setJavaMailDebugEnabled(boolean javaMailDebugEnabled)
   {
      this.javaMailDebugEnabled = javaMailDebugEnabled;
   }

   public void send(String from,
                    String to,
                    String subject,
                    String body,
                    String contentType)
   {
      try
      {
         MyMessage runnable = new MyMessage(from, to, subject, body, contentType);
         log.debug("Enqueuing serialId=" + runnable.serialId);
         executor.execute(runnable);
         log.debug("Enqueued serialId=" + runnable.serialId);
      }
      catch (InterruptedException ignore)
      {
         log.debug("Interrupted during deliver attempt");
      }
   }
   
   public void send(String from,
                    String to,
                    String subject,
                    String body)
   {
      send(from, to, subject, body, DEFAULT_CONTENT_MIME_TYPE);
   }

   protected void startService()
      throws Exception
   {
      // Create the thread used to deliver messages
      if (queueCapacity > 0)
      {
         queue = new BoundedLinkedQueue(queueCapacity);
      }
      else
      {
         queue = new LinkedQueue();
      }

      executor = new QueuedExecutor(queue);

      if ((gateway != null) && (gateway.length() > 0))
      {
         properties.setProperty("mail.smtp.host", gateway);
      }
      else
      {
         log.warn("You did not set up any SMTP gateway, cannot send any email");
      }

      if (smtpUser != null)
      {
         properties.setProperty("mail.smtp.auth", "true");
         smtpAuth = new MailAuthenticator(smtpUser, smtpPassword);
      }
      else
      {
         properties.setProperty("mail.smtp.auth", "false");
         smtpAuth = null;
      }

      // Set timeouts, default is infinite, we want to avoid it
      properties.setProperty("mail.smtp.connectiontimeout", "" + SMTPConnectionTimeout);
      properties.setProperty("mail.smtp.timeout", "" + SMTPTimeout);

      //
      super.startService();
   }

   protected void stopService() throws Exception
   {
      try
      {
         super.stopService();
      }
      finally
      {
         properties.clear();
         executor.shutdownAfterProcessingCurrentTask();
         executor = null;
         queue = null;
      }
   }

   public boolean deliver(long serialId,
                          String from,
                          String to,
                          String subject,
                          String body)
   {      
      return deliver(serialId, from, to, subject, body, DEFAULT_CONTENT_MIME_TYPE);
   }
   
   public boolean deliver(long serialId,
                          String from,
                          String to,
                          String subject,
                          String body,
                          String contentType)
   {
      boolean delivered = false;
      try
      {
         if ((gateway != null) && (gateway.length() > 0))
         {
            delivered = deliver(serialId, gateway, from, to, subject, body, contentType);
         }
         else
         {
            log.warn("You did not specify any gateway, the email cannot be sent");
         }
      }
      catch (Throwable t)
      {
         log.error("Problem while delivering serialId=" + serialId, t);
      }

      return delivered;
   }

   private boolean deliver(long serialId,
                           String host,
                           String from,
                           String to,
                           String subject,
                           String body,
                           String contentType)
      throws AddressException,
      NoSuchProviderException,
      MessagingException
   {
      Transport transport = null;
      try
      {
         InternetAddress toAddress = new InternetAddress(to);
         Session session = Session.getDefaultInstance(properties, smtpAuth);
         session.setDebug(javaMailDebugEnabled);
         session.setDebugOut(logs);

         // Get transport
         URLName urlname = new URLName("smtp://" + host);
         transport = session.getTransport(urlname);

         // Connect
         log.debug("Connecting to " + host + " with serialId=" + serialId);
         transport.connect();
         log.debug("Connected to " + host + " with serialId=" + serialId);

         // Prepare message
         MimeMessage message = new MimeMessage(session);
         message.setFrom(new InternetAddress(from));
         
         // Replaced message.setText(body); for setContent(...) to allow
         // a MIME type be set.  Now MailModule can support text/html messages
         message.setContent(body, contentType);
         message.setSubject(subject);         
         message.setSentDate(new Date());
         message.addRecipient(javax.mail.Message.RecipientType.TO, toAddress);

         // Send message
         log.debug("Sending message serialId=" + serialId);
         transport.sendMessage(message,
            new InternetAddress[]
               {
                  toAddress
               });
         log.debug("Sent msg, subject=" + subject + ", serialId=" + serialId);
         return true;
      }
      finally
      {
         if (transport != null)
         {
            try
            {
               transport.close();
            }
            catch (MessagingException ignore)
            {
            }
         }
      }
   }

   /** Used for sending through a gateway needing authentication */
   private static class MailAuthenticator
      extends javax.mail.Authenticator
   {
      private String username = null;
      private String password = null;

      public MailAuthenticator(String username,
                               String password)
      {
         this.username = username;
         this.password = password;
      }

      public PasswordAuthentication getPasswordAuthentication()
      {
         return new PasswordAuthentication(username, password);
      }
   }

   /** Encapsulate a message in this class with a serial id version to keep track. */
   private class MyMessage
      implements Runnable
   {
      public final long serialId;

      public final String from;

      public final String to;

      public final String subject;

      public final String body;
      
      public final String contentType;

      public MyMessage(String from,
                       String to,
                       String subject,
                       String body,
                       String contentType)
      {
         this.serialId = currentSerialId.increment();
         this.from = from;
         this.to = to;
         this.subject = subject;
         this.body = body;
         this.contentType = contentType;
      }

      public void run()
      {
         try
         {
            log.debug("Dequeued serialId=" + serialId + " and delivering it");
            boolean delivered = deliver(serialId, from, to, subject, body, contentType);
            log.debug(delivered + " on delivery for serialId=" + serialId);
         }
         catch (Throwable t)
         {
            log.error("Caught throwable while delivering serialId=" + serialId, t);
         }
      }
   }
}
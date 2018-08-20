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
package org.jboss.portal.core.servlet.jsp.taglib;

import org.jboss.portal.core.servlet.jsp.PortalJsp;

import javax.portlet.PortletConfig;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.io.Writer;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Error tag. Used to include an error message.
 *
 * @author <a href="theute@jboss.org">Thomas Heute</a> $Revision: 8786 $
 */
public class ErrorTag
   extends TagSupport
{

   /** The serialVersionUID */
   private static final long serialVersionUID = 5521318012046201299L;
   /** key attribute to the tag */
   private String key;


   /**
    * Set the key attribute.
    *
    * @param key
    */
   public void setKey(String key)
   {
      this.key = key;
   }

   public int doStartTag() throws JspException
   {
      String value = pageContext.getRequest().getParameter(key);
      if (value != null)
      {

         Locale locale = pageContext.getRequest().getLocale();
         PortletConfig portletConfig = (PortletConfig)pageContext.getRequest().getAttribute("javax.portlet.config");
         ResourceBundle resourceBundle = portletConfig.getResourceBundle(locale);
         String translatedValue = "";
         try
         {
            translatedValue = resourceBundle.getString(value);
         }
         catch (MissingResourceException e)
         {
            PortalJsp.logger.error("No such resource key in resource file: " + key);
         }

         try
         {
            Writer writer = pageContext.getOut();
            writer.write("<span class=\"portlet-msg-error\">" + translatedValue + "</span>");
         }
         catch (IOException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
      return SKIP_BODY;
   }

}

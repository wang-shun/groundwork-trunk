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
package org.jboss.portal.core.metadata.portlet;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 10228 $
 */
public class JBossPortletMetaData extends org.jboss.portal.portlet.deployment.jboss.metadata.JBossPortletMetaData
{

   /** . */
   private HeaderContentMetaData headerContent;

   /** . */
   private AjaxMetaData ajax;

   /** . */
   private PortletInfoMetaData portletInfo;

   public HeaderContentMetaData getHeaderContent()
   {
      return headerContent;
   }

   public void setHeaderContent(HeaderContentMetaData headerContent)
   {
      this.headerContent = headerContent;
   }

   public AjaxMetaData getAjax()
   {
      return ajax;
   }

   public void setAjax(AjaxMetaData ajax)
   {
      this.ajax = ajax;
   }

   public PortletInfoMetaData getPortletInfo()
   {
      return portletInfo;
   }

   public void setPortletInfo(PortletInfoMetaData portletInfo)
   {
      this.portletInfo = portletInfo;
   }

   public void merge(org.jboss.portal.portlet.deployment.jboss.metadata.JBossPortletMetaData portlet)
   {
      super.merge(portlet);

      // We handle the core extension here
      if (portlet instanceof JBossPortletMetaData)
      {
         JBossPortletMetaData portletExt = (JBossPortletMetaData)portlet;
         if (ajax == null)
         {
            // If not defined we use the default ajax configuration
            ajax = portletExt.getAjax();
         }
         else
         {
            // If no partialRefresh value provided use the one provided by the default
            if (ajax.getPartialRefresh() == null)
            {
               ajax.setPartialRefresh(portletExt.getAjax().getPartialRefresh());
            }
         }
      }
   }
}

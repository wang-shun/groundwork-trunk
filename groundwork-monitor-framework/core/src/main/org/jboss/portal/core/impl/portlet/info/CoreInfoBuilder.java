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
package org.jboss.portal.core.impl.portlet.info;

import org.jboss.portal.common.transaction.Transactions;
import org.jboss.portal.core.metadata.portlet.AjaxMetaData;
import org.jboss.portal.core.metadata.portlet.HeaderContentMetaData;
import org.jboss.portal.core.metadata.portlet.JBossPortletMetaData;
import org.jboss.portal.core.metadata.portlet.PortletInfoMetaData;
import org.jboss.portal.core.portlet.info.AjaxInfo;
import org.jboss.portal.core.portlet.info.MarkupHeaderInfo;
import org.jboss.portal.core.portlet.info.PortletInfoInfo;
import org.jboss.portal.core.portlet.info.TransactionInfo;
import org.jboss.portal.core.portlet.info.WSRPInfo;
import org.jboss.portal.portlet.deployment.jboss.InfoBuilder;
import org.jboss.portal.portlet.deployment.jboss.PortletApplicationContextImpl;
import org.jboss.portal.portlet.deployment.jboss.info.SessionInfo;
import org.jboss.portal.portlet.deployment.jboss.info.impl.SessionInfoImpl;
import org.jboss.portal.portlet.deployment.jboss.metadata.JBossApplicationMetaData;
import org.jboss.portal.portlet.impl.info.ContainerInfoBuilder;
import org.jboss.portal.portlet.impl.info.ContainerInfoBuilderContext;
import org.jboss.portal.portlet.impl.info.ContainerPortletApplicationInfo;
import org.jboss.portal.portlet.impl.info.ContainerPortletInfo;
import org.jboss.portal.portlet.impl.jsr168.ContainerInfoBuilderContextImpl;
import org.jboss.portal.portlet.impl.metadata.PortletApplication10MetaData;
import org.jboss.portal.portlet.info.PortletInfo;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * @author <a href="mailto:theute@jboss.org">Thomas Heute</a>
 * @version $Revision$
 */
public class CoreInfoBuilder implements InfoBuilder
{

   /** . */
   private static final Logger log = Logger.getLogger(CoreInfoBuilder.class);

   /** . */
   private JBossApplicationMetaData jbossApplicationMetaData;

   /** . */
   private PortletApplication10MetaData portletApplicationMD;

   /** . */
   private CoreInfoBuilderContext builderContext;

   /** . */
   private LinkedHashMap<String, PortletInfo> portlets;

   /** . */
   private ContainerPortletApplicationInfo application;

   public CoreInfoBuilder(
      JBossApplicationMetaData jbossApplicationMetaData,
      PortletApplication10MetaData portletApplicationMD,
      CoreInfoBuilderContext builderContext)
   {
      this.portletApplicationMD = portletApplicationMD;
      this.jbossApplicationMetaData = jbossApplicationMetaData;
      this.builderContext = builderContext;
      this.portlets = new LinkedHashMap<String, PortletInfo>();
   }

   public Collection<PortletInfo> getPortlets()
   {
      return portlets.values();
   }

   public ContainerPortletApplicationInfo getApplication()
   {
      return application;
   }

   public void build()
   {

      //
      String contextPath = builderContext.getWebApp().getContextPath();
      String id = jbossApplicationMetaData.getId();

      //
      log.debug("Going to build portlet application metadata for application with context path '" + contextPath + "' with id '" + id + "'");

      //
      ContainerInfoBuilderContext containerBuilderContext = new ContainerInfoBuilderContextImpl(portletApplicationMD, builderContext.getWebApp());
      ContainerInfoBuilder builder = new ContainerInfoBuilder(id, portletApplicationMD, containerBuilderContext);
      builder.build();

      //
      this.application = builder.getApplication();

      //
      for (ContainerPortletInfo containerInfo : builder.getPortlets())
      {
         String name = containerInfo.getName();
         org.jboss.portal.portlet.deployment.jboss.metadata.JBossPortletMetaData jbPortletMD = jbossApplicationMetaData.getPortlets().get(name);
         if (jbPortletMD instanceof JBossPortletMetaData)
         {
            JBossPortletMetaData jbossPortletMD = (JBossPortletMetaData)jbPortletMD;

            AjaxMetaData ajaxMD = jbossPortletMD.getAjax();
            if (ajaxMD != null)
            {
               containerInfo.setAttachment(AjaxInfo.class, new AjaxInfoImpl(ajaxMD));
            }

            HeaderContentMetaData headerContentMD = jbossPortletMD.getHeaderContent();
            if (headerContentMD != null)
            {
               containerInfo.setAttachment(MarkupHeaderInfo.class, new MarkupHeaderInfoImpl(headerContentMD));
            }

            PortletInfoMetaData portletInfoMD = jbossPortletMD.getPortletInfo();
            if (portletInfoMD != null)
            {
               containerInfo.setAttachment(PortletInfoInfo.class, new PortletInfoInfoImpl(new PortletApplicationContextImpl(builderContext.getWebApp()), portletInfoMD));
            }

            Transactions.Type txType = jbossPortletMD.getTxType();
            if (txType != null)
            {
               containerInfo.setAttachment(TransactionInfo.class, new TransactionInfoImpl(jbossPortletMD.getTxType()));
            }

            Boolean distributed = jbossPortletMD.getDistributed();
            if (distributed != null)
            {
               containerInfo.setAttachment(SessionInfo.class, new SessionInfoImpl(distributed));
            }
            
            Boolean remotable = jbossPortletMD.getRemotable();
            if (remotable != null)
            {
               containerInfo.setAttachment(WSRPInfo.class, new WSRPInfoImpl(remotable));
            }
         }

         portlets.put(name, containerInfo);
      }
   }

}
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

import org.jboss.portal.portlet.deployment.jboss.InfoBuilder;
import org.jboss.portal.portlet.deployment.jboss.InfoBuilderFactory;
import org.jboss.portal.portlet.deployment.jboss.metadata.JBossApplicationMetaData;
import org.jboss.portal.portlet.impl.metadata.PortletApplication10MetaData;
import org.jboss.portal.server.deployment.PortalWebApp;

/**
 * @author <a href="mailto:theute@jboss.org">Thomas Heute</a>
 * @version $Revision$
 */
public class CoreInfoBuilderFactory implements InfoBuilderFactory
{

   public InfoBuilder createInfoBuilder(PortalWebApp webApp, JBossApplicationMetaData jbossApplicationMetaData, PortletApplication10MetaData portletApplicationMD)
   {
      CoreInfoBuilderContext cibc = new CoreInfoBuilderContext(webApp);
      CoreInfoBuilder cib = new CoreInfoBuilder(jbossApplicationMetaData, portletApplicationMD, cibc);
      return cib;
   }

}


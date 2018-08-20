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
package org.jboss.portal.core.identity.ui.common;

import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.jboss.portal.core.identity.services.metadata.IdentityUIConfiguration;
import org.jboss.portal.core.identity.services.metadata.UIComponentConfiguration;
import org.jboss.portal.faces.el.PropertyValue;
import org.jboss.portal.faces.el.dynamic.DynamicBean;
import org.jboss.portal.server.ServerInvocationContext;
import org.jboss.portlet.JBossRenderRequest;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class MetaDataServiceBean implements DynamicBean
{
   /** The map */
   Map<String, UIComponentConfiguration> map = null;

   public MetaDataServiceBean()
   { 
      FacesContext ctx = FacesContext.getCurrentInstance();
      ConfigurationBean cfb = (ConfigurationBean) ctx.getApplication().createValueBinding(("#{configurationbean}")).getValue(ctx);
      IdentityUIConfiguration cf = cfb.getConfiguration(); 
      this.map = cf.getUiComponents();
   }
   
   public Class getType(Object propertyName) throws IllegalArgumentException
   {
         return UIComponentConfiguration.class;
   }

   public PropertyValue getValue(Object propertyName) throws IllegalArgumentException
   {
      UIComponentConfiguration uiComponent = map.get((String) propertyName);
      return uiComponent != null ? new PropertyValue(uiComponent) : null;
   }

   public boolean setValue(Object propertyName, Object value) throws IllegalArgumentException
   {
      return true;
   }
  
   /** used for building the url after validating email */
   public String getPortalContextPath()
   {
      ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();
      JBossRenderRequest request = (JBossRenderRequest) ectx.getRequest();
      ServerInvocationContext invocationContext = request.getControllerContext().getServerInvocation().getServerContext();
      return invocationContext.getPortalContextPath();      
   }
}

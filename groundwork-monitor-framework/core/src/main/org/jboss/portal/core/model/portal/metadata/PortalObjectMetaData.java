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
package org.jboss.portal.core.model.portal.metadata;

import org.jboss.logging.Logger;
import org.jboss.portal.common.i18n.LocaleFormat;
import org.jboss.portal.common.i18n.LocalizedString;
import org.jboss.portal.common.i18n.ResourceBundleManager;
import org.jboss.portal.common.util.ConversionException;
import org.jboss.portal.common.util.Tools;
import org.jboss.portal.common.xml.XMLTools;
import org.jboss.portal.core.model.MetaDataResourceBundleFactory;
import org.jboss.portal.core.model.content.spi.ContentProviderRegistry;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.PortalObjectContainer;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.core.model.portal.PortalObjectPermission;
import org.jboss.portal.core.model.portal.metadata.coordination.CoordinationMetaData;
import org.jboss.portal.security.RoleSecurityBinding;
import org.jboss.portal.security.SecurityConstants;
import org.jboss.portal.security.metadata.SecurityConstraintsMetaData;
import org.jboss.portal.security.spi.provider.DomainConfigurator;
import org.jboss.portal.security.spi.provider.SecurityConfigurationException;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @author <a href="mailto:boleslaw dot dawidowicz at redhat anotherdot com">Boleslaw Dawidowicz</a>
 * @version $Revision: 12550 $
 */
public abstract class PortalObjectMetaData
{

   private static Logger log = Logger.getLogger(PortalObjectMetaData.class);

   private String name;
   private String listener;
   private Map<String,String> properties;
   private Map<String,PortalObjectMetaData> children;
   private SecurityConstraintsMetaData securityConstraints;
   private LocalizedString displayName;
   private String resourceBundle;
   private List supportedLocales;

   public PortalObjectMetaData()
   {
      properties = new HashMap<String, String>();
      children = new HashMap<String, PortalObjectMetaData>();
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getListener()
   {
      return listener;
   }

   public void setListener(String listener)
   {
      this.listener = listener;
   }

   public Map<String,String> getProperties()
   {
      return properties;
   }

   public void setProperties(Map properties)
   {
      this.properties = properties;
   }

   public Map<String, PortalObjectMetaData> getChildren()
   {
      return children;
   }

   public void setChildren(Map children)
   {
      this.children = children;
   }
   
   public void setDisplayName(LocalizedString displayName)
   {
      this.displayName = displayName;
   }
   
   public LocalizedString getDisplayName()
   {
      return displayName;
   }

   public SecurityConstraintsMetaData getSecurityConstraints()
   {
      return securityConstraints;
   }

   public void setSecurityConstraints(SecurityConstraintsMetaData securityConstraints)
   {
      this.securityConstraints = securityConstraints;
   }

   /**
    * Create an instance of the corresponding portal object.
    *
    * @param buildContext the context
    * @param parent       the parent object
    * @return the corresponding portal object
    * @throws Exception any exception
    */
   public final PortalObject create(BuildContext buildContext, PortalObject parent) throws Exception
   {
      // Build instance
      PortalObject object = newInstance(buildContext, parent);
      
      // Configure common properties
      configure(buildContext, object);

      // Build children recursively
      for (PortalObjectMetaData portalObjectMD : getChildren().values())
      {
         portalObjectMD.create(buildContext, object);
      }

      // Coordination - must be applied after children (windows) were created 
      CoordinationMetaData coordinationMD = null;
      if (this instanceof PortalMetaData)
      {
         coordinationMD = ((PortalMetaData)this).getCoordinationMetaData();
      }
      else if (this instanceof PageMetaData)
      {
         coordinationMD = ((PageMetaData)this).getCoordinationMetaData();
      }

      if (coordinationMD != null)
      {
         coordinationMD.configure(buildContext, object);
      }

      //
      return object;
   }

   protected abstract PortalObject newInstance(BuildContext buildContext, PortalObject parent) throws Exception;

   /**
    * Configure common state.
    *
    * @param buildContext
    * @param object
    * @throws SecurityConfigurationException
    */
   private void configure(BuildContext buildContext, PortalObject object) throws SecurityConfigurationException
   {
      // Configure properties
      for (Map.Entry<String,String> entry : properties.entrySet())
      {
         object.setDeclaredProperty(entry.getKey(), entry.getValue());
      }

      // Configure listener
      object.setListener(listener);
  
      if (resourceBundle != null)
      {
         ClassLoader classloader = null;
         if (buildContext.getPortalWebApp() != null)
         {
            classloader = buildContext.getPortalWebApp().getClassLoader();
         }
         else
         {
            classloader = Thread.currentThread().getContextClassLoader();
         }
         
         ResourceBundleManager bundleMgr = MetaDataResourceBundleFactory.createResourceBundleManager(classloader, supportedLocales, resourceBundle);
         object.setDisplayName(bundleMgr.getLocalizedValue("org.jboss.portal.object.name." + object.getId().getPath().toString(PortalObjectPath.LEGACY_FORMAT), object.getName()));
      }
      
      // Configure display name
      if (displayName != null)
      {
         object.setDisplayName(displayName);
      }

      // Configure security
      SecurityConstraintsMetaData securityConstraints = getSecurityConstraints();
      if (securityConstraints == null)
      {
         if (this instanceof PortalMetaData)
         {
            // Default is view recursive
            securityConstraints = new SecurityConstraintsMetaData();
            RoleSecurityBinding binding = new RoleSecurityBinding(PortalObjectPermission.VIEW_RECURSIVE_ACTION, SecurityConstants.UNCHECKED_ROLE_NAME);
            securityConstraints.getConstraints().add(binding);
         }
         else if (this instanceof ContextMetaData)
         {
            // Default is view
            securityConstraints = new SecurityConstraintsMetaData();
            RoleSecurityBinding binding = new RoleSecurityBinding(PortalObjectPermission.VIEW_ACTION, SecurityConstants.UNCHECKED_ROLE_NAME);
            securityConstraints.getConstraints().add(binding);
         }
      }
      else
      {
         // Skip window configuration
         if (this instanceof WindowMetaData)
         {
            securityConstraints = null;
            log.warn("Window " + getName() + " has security a " +
               "configuration but it is not taken in account, portlet instance configuration should be done rather");
         }
      }

      //
      if (securityConstraints != null)
      {
         // Apply the constraint
         PortalObjectContainer poc = buildContext.getContainer();
         DomainConfigurator domainConfigurator = poc.getAuthorizationDomain().getConfigurator();
         domainConfigurator.setSecurityBindings(object.getId().toString(PortalObjectPath.CANONICAL_FORMAT), securityConstraints.getConstraints());
      }
   }

   public static PortalObjectMetaData buildMetaData(ContentProviderRegistry contentProviderRegistry, Element portalObjectElt) throws Exception
   {
      String type = portalObjectElt.getTagName();
      PortalObjectMetaData portalObjectMD = null;
      if ("portal".equals(type))
      {
         portalObjectMD = PortalMetaData.buildPortalMetaData(contentProviderRegistry, portalObjectElt);
      }
      else if ("page".equals(type))
      {
         portalObjectMD = PageMetaData.buildPageMetaData(contentProviderRegistry, portalObjectElt);
      }
      else if ("window".equals(type))
      {
         portalObjectMD = WindowMetaData.buildPortletWindowMetaData(contentProviderRegistry, portalObjectElt);
      }
      else if ("context".equals(type))
      {
         portalObjectMD = ContextMetaData.buildContextMetaData(contentProviderRegistry, portalObjectElt);
      }

      // Parse common XML stuff
      if (portalObjectMD != null)
      {
         // Add the security constraints
         Element securityConstraintElt = XMLTools.getUniqueChild(portalObjectElt, "security-constraint", false);
         if (securityConstraintElt != null)
         {
            SecurityConstraintsMetaData securityConstraintsMD = SecurityConstraintsMetaData.buildSecurityConstraintMetaData(securityConstraintElt);
            portalObjectMD.setSecurityConstraints(securityConstraintsMD);
         }

         // Configure properties
         Element propertiesElt = XMLTools.getUniqueChild(portalObjectElt, "properties", false);
         if (propertiesElt != null)
         {
            buildPropertiesMetaData(portalObjectMD, propertiesElt);
         }

         // Configure listener
         Element listenerElt = XMLTools.getUniqueChild(portalObjectElt, "listener", false);
         if (listenerElt != null)
         {
            buildListenerMetaData(portalObjectMD, listenerElt);
         }

         // Configure resource-bundle
         Element resourceBundleElt = XMLTools.getUniqueChild(portalObjectElt, "resource-bundle", false);
         if (resourceBundleElt != null)
         {
            buildResourceBundleMetaData(portalObjectMD, resourceBundleElt);
            buildSupportedLocalesMetaData(portalObjectMD, portalObjectElt);
         }
         else
         {
            buildDisplayNameMetaData(portalObjectMD, portalObjectElt);
         }
      }
      return portalObjectMD;
   }

   public static void buildPropertiesMetaData(PortalObjectMetaData portalObjectMD, Element propertiesElt)
   {
      for (Element propertyElt : XMLTools.getChildren(propertiesElt, "property"))
      {
         Element nameElt = XMLTools.getUniqueChild(propertyElt, "name", true);
         Element valueElt = XMLTools.getUniqueChild(propertyElt, "value", true);
         String name = XMLTools.asString(nameElt);
         String value = XMLTools.asString(valueElt);

         // log.debug("Found property " + name + " = " + value);
         portalObjectMD.getProperties().put(name, value);
      }
   }
   
   public static void buildDisplayNameMetaData(PortalObjectMetaData portalObjectMD, Element portalObjectElt)
   {
      Iterator displayNamesIt = XMLTools.getChildrenIterator(portalObjectElt, "display-name");
      
      // Configure localized display-name
      Map localizedStringValues = new HashMap();
      while (displayNamesIt.hasNext())
      {
         Element element = (Element)displayNamesIt.next();
         String lang = element.getAttribute("xml:lang");
         Locale locale;
         try
         {
            locale = LocaleFormat.DEFAULT.getLocale(lang);
            localizedStringValues.put(locale, element.getTextContent());
         }
         catch (ConversionException e)
         {
            log.error("Cannot set localized display-name, for language: " + lang, e);
         }
      }
      if (localizedStringValues.size() != 0)
      {
         portalObjectMD.setDisplayName(new LocalizedString(localizedStringValues, Locale.ENGLISH));
      }
   }

   public static void buildSupportedLocalesMetaData(PortalObjectMetaData portalObjectMD, Element portalObjectElt)
   {
      Iterator supportedLocalesIt = XMLTools.getChildrenIterator(portalObjectElt, "supported-locale");
      
      List supportedLocales = new ArrayList();
      while (supportedLocalesIt.hasNext())
      {
         Element element = (Element)supportedLocalesIt.next();
         supportedLocales.add(new Locale(element.getTextContent()));
      }
      portalObjectMD.setSupportedLocales(supportedLocales);
   }


   public static void buildListenerMetaData(PortalObjectMetaData portalObjectMD, Element listenerElt)
   {
      String listener = XMLTools.asString(listenerElt);
      portalObjectMD.setListener(listener);
   }

   public static void buildResourceBundleMetaData(PortalObjectMetaData portalObjectMD, Element resourceBundleElt)
   {
      String resourceBundle = XMLTools.asString(resourceBundleElt);
      portalObjectMD.setResourceBundle(resourceBundle);
   }

   public String toString()
   {
      String name = getClass().getName();
      return Tools.getShortNameOf(getClass()) + "[" + name + "]";
   }

   public String getResourceBundle()
   {
      return resourceBundle;
   }

   public void setResourceBundle(String resourceBundle)
   {
      this.resourceBundle = resourceBundle;
   }

   public List getSupportedLocales()
   {
      return supportedLocales;
   }

   public void setSupportedLocales(List supportedLocales)
   {
      this.supportedLocales = supportedLocales;
   }
}

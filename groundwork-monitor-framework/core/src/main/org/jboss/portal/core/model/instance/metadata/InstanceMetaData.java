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
package org.jboss.portal.core.model.instance.metadata;

import org.apache.log4j.Logger;
import org.jboss.portal.common.i18n.LocaleFormat;
import org.jboss.portal.common.i18n.LocalizedString;
import org.jboss.portal.common.i18n.ResourceBundleManager;
import org.jboss.portal.common.util.ConversionException;
import org.jboss.portal.common.xml.XMLTools;
import org.jboss.portal.core.model.MetaDataResourceBundleFactory;
import org.jboss.portal.portlet.deployment.LocalizedStringBuilder;
import org.jboss.portal.portlet.impl.metadata.common.LocalizedDescriptionMetaData;
import org.jboss.portal.portlet.impl.metadata.portlet.PortletPreferenceMetaData;
import org.jboss.portal.portlet.impl.metadata.portlet.PortletPreferencesMetaData;
import org.jboss.portal.security.metadata.SecurityConstraintsMetaData;
import org.jboss.portal.server.deployment.PortalWebApp;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Represent metadata of an instance.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 10228 $
 */
public class InstanceMetaData
{

   /** The logger. */
   private static final Logger log = Logger.getLogger(InstanceMetaData.class);

   /** The instance id. */
   private String id;

   /** The specific preferences of this instance. */
   private PortletPreferencesMetaData preferences;

   /** The portlet referenced by the instance. */
   private String portletRef;

   /** The security constraints. */
   private SecurityConstraintsMetaData securityConstraints;

   /** Localized display name */
   private LocalizedString displayName;
   
   /** Resource bundle name */
   private String resourceBundle;
   
   /** Supported locales */
   private List supportedLocales;

   public InstanceMetaData()
   {
   }

   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   public PortletPreferencesMetaData getPreferences()
   {
      return preferences;
   }

   public void setPreferences(PortletPreferencesMetaData preferences)
   {
      this.preferences = preferences;
   }

   public String getPortletRef()
   {
      return portletRef;
   }

   public void setPortletRef(String portletRef)
   {
      this.portletRef = portletRef;
   }

   public LocalizedString getDisplayName()
   {
      return displayName;
   }

   public void setDisplayName(LocalizedString displayName)
   {
      this.displayName = displayName;
   }

   public SecurityConstraintsMetaData getSecurityConstraints()
   {
      return securityConstraints;
   }

   public void setSecurityConstraints(SecurityConstraintsMetaData securityConstraints)
   {
      this.securityConstraints = securityConstraints;
   }

   public static InstanceMetaData buildMetaData(Element instanceElt, PortalWebApp pwa)
   {
      String instanceName = XMLTools.asString(XMLTools.getUniqueChild(instanceElt, "instance-id", true));
      String componentRef = XMLTools.asString(XMLTools.getUniqueChild(instanceElt, "portlet-ref", true));
      Element resourceBundleElement = XMLTools.getUniqueChild(instanceElt, "resource-bundle", false);
      String resourceBundle = null;
      if (resourceBundleElement != null)
      {
         resourceBundle = XMLTools.asString(resourceBundleElement);
      }
      Iterator displayNamesIt = XMLTools.getChildrenIterator(instanceElt, "display-name");

      // Configure preferences override
      org.jboss.portal.portlet.impl.metadata.portlet.PortletPreferencesMetaData preferencesMD = new PortletPreferencesMetaData();
      Element preferencesElt = XMLTools.getUniqueChild(instanceElt, "preferences", false);
      if (preferencesElt != null)
      {
         for (Iterator i = buildPreferencesMetaData(preferencesElt).values().iterator(); i.hasNext();)
         {
            PortletPreferenceMetaData preferenceMD = (PortletPreferenceMetaData)i.next();
            preferencesMD.addPortletPreference(preferenceMD);
         }
      }

      // Configure localized display-name
      LocalizedStringBuilder localizedStringMD = new LocalizedStringBuilder();
      Object dummy = new String("Dummy");
      while (displayNamesIt.hasNext())
      {
         Element element = (Element)displayNamesIt.next();
         LocalizedDescriptionMetaData displayNameMD = new LocalizedDescriptionMetaData();
         String lang = element.getAttribute("xml:lang");
         Locale locale;
         try
         {
            locale = LocaleFormat.DEFAULT.getLocale(lang);
            displayNameMD.setLang(locale.toString());
            displayNameMD.setDescription(element.getTextContent());
            localizedStringMD.put(dummy, displayNameMD);
         }
         catch (ConversionException e)
         {
            log.error("Cannot set localized display-name, for language: " + lang, e);
         }
      }

      Iterator supportedLocalesIt = XMLTools.getChildrenIterator(instanceElt, "supported-locale");
      List supportedLocales = new ArrayList();
      while (supportedLocalesIt.hasNext())
      {
         String localeName = ((Element)supportedLocalesIt.next()).getTextContent();
         supportedLocales.add(new Locale(localeName));
      }
      
      /*
      // Set display name
      if (localizedStringMD.getLocalizedString(dummy).getValues().isEmpty() && (pwa != null))
      {
         ResourceBundleManager bundleMgr = MetaDataResourceBundleFactory.createResourceBundleManager(pwa.getClassLoader(), supportedLocales, resourceBundle);
         LocalizedString localizedString = bundleMgr.getLocalizedValue("org.jboss.portal.instance.name." + instanceName, instanceName);
         Map map = localizedString.getValues();
         Iterator localeIt = map.values().iterator();
         while (localeIt.hasNext())
         {
            LocalizedString.Value value = (LocalizedString.Value)localeIt.next();
            LocalizedDescriptionMetaData displayNameMD = new LocalizedDescriptionMetaData();
            displayNameMD.setLang(value.getLocale().toString());
            displayNameMD.setDescription(value.getString());
            localizedStringMD.put(dummy, displayNameMD);
         }
      }
      */
      
      // Create the meta data
      InstanceMetaData instanceMD = new InstanceMetaData();
      instanceMD.setId(instanceName);
      instanceMD.setPortletRef(componentRef);
      instanceMD.setPreferences(preferencesMD);
      instanceMD.setDisplayName(localizedStringMD.getLocalizedString(dummy));
      instanceMD.setResourceBundle(resourceBundle);
      instanceMD.setSupportedLocales(supportedLocales);
      
      // Add the security constraints
      Element securityConstraintElt = XMLTools.getUniqueChild(instanceElt, "security-constraint", false);
      if (securityConstraintElt != null)
      {
         SecurityConstraintsMetaData securityConstraintsMD = SecurityConstraintsMetaData.buildSecurityConstraintMetaData(securityConstraintElt);
         instanceMD.setSecurityConstraints(securityConstraintsMD);
      }

      //
      return instanceMD;
   }

   /**
    * @param instanceElt the instance xml element
    * @param pwaId       the portlet application id
    */
   public static InstanceMetaData buildLegacyMetaData(Element instanceElt, String pwaId)
   {
      Element instanceNameElt = XMLTools.getUniqueChild(instanceElt, "instance-name", true);
      Element componentRefElt = XMLTools.getUniqueChild(instanceElt, "component-ref", true);

      //
      String instanceName = XMLTools.asString(instanceNameElt);
      String componentRef = XMLTools.asString(componentRefElt);

      //
      int dotIndex = componentRef.indexOf('.');

      //
      if (dotIndex == -1)
      {
         log.warn("Bad component ref " + componentRef);
      }
      else
      {
         String appId = componentRef.substring(0, dotIndex);
         if (appId.equals(pwaId) == false)
         {
            log.warn("The instance " + instanceName + " will not be created because the component referenced is outside of the same web app " + componentRef);
         }
         else
         {
            String portletRef = componentRef.substring(dotIndex + 1);

            if (portletRef.length() == 0)
            {
               log.warn("Zero portlet ref length are not considered " + componentRef);
            }
            else
            {
               log.debug("Adding legacy instance " + instanceName);

               //
               InstanceMetaData instanceMD = new InstanceMetaData();
               instanceMD.setId(instanceName);
               instanceMD.setPortletRef(portletRef);

               // Configure preferences
               PortletPreferencesMetaData preferencesMD = new PortletPreferencesMetaData();
               Element preferencesElt = XMLTools.getUniqueChild(instanceElt, "preferences", false);
               if (preferencesElt != null)
               {
                  for (Iterator j = InstanceMetaData.buildPreferencesMetaData(preferencesElt).values().iterator(); j.hasNext();)
                  {
                     PortletPreferenceMetaData preferenceMD = (PortletPreferenceMetaData)j.next();
                     preferencesMD.addPortletPreference(preferenceMD);
                  }
               }

               // Add the security constraints
               Element securityConstraintElt = XMLTools.getUniqueChild(instanceElt, "security-constraint", false);
               if (securityConstraintElt != null)
               {
                  SecurityConstraintsMetaData securityConstraintsMD = SecurityConstraintsMetaData.buildSecurityConstraintMetaData(securityConstraintElt);
                  instanceMD.setSecurityConstraints(securityConstraintsMD);
               }

               //
               return instanceMD;
            }
         }
      }

      //
      return null;
   }

   public static Map buildPreferencesMetaData(Element portletPreferencesElt)
   {
      Map preferences = new HashMap();
      for (Iterator i = XMLTools.getChildrenIterator(portletPreferencesElt, "preference"); i.hasNext();)
      {
         Element preferenceElt = (Element)i.next();
         PortletPreferenceMetaData preference = buildPreferenceMetaData(preferenceElt);
         preferences.put(preference.getName(), preference);
      }
      return preferences;
   }

   public static PortletPreferenceMetaData buildPreferenceMetaData(Element preferenceElt)
   {
      PortletPreferenceMetaData preferenceMD = new PortletPreferenceMetaData();
      List valuesElt = XMLTools.getChildren(preferenceElt, "value");
      String[] values = new String[valuesElt.size()];
      for (int i = 0; i < valuesElt.size(); i++)
      {
         values[i] = XMLTools.asString((Element)valuesElt.get(i));
      }
      Element readOnlyElt = XMLTools.getUniqueChild(preferenceElt, "read-only", false);
      boolean readOnly = false;
      if (readOnlyElt != null)
      {
         String value = XMLTools.asString(readOnlyElt);
         if ("true".equals(value))
         {
            readOnly = true;
         }
         else if ("false".equals(value))
         {
            readOnly = false;
         }
         else
         {
            // log.debug("Unrecognized value for read only " + value);
            readOnly = false;
         }
      }
      else
      {
         readOnly = false;
      }
      preferenceMD.setName(XMLTools.asString(XMLTools.getUniqueChild(preferenceElt, "name", true)));
      preferenceMD.setReadOnly(readOnly);
      preferenceMD.setValue(Arrays.asList(values));
      return preferenceMD;
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

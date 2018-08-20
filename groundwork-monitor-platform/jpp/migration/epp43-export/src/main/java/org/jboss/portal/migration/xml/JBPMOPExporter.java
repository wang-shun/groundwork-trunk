/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.portal.migration.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.naming.InitialContext;
import javax.transaction.TransactionManager;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.jboss.portal.common.i18n.LocalizedString;
import org.jboss.portal.common.transaction.Transactions;
import org.jboss.portal.core.model.content.Content;
import org.jboss.portal.core.model.content.ContentType;
import org.jboss.portal.core.model.instance.InstanceContainer;
import org.jboss.portal.core.model.instance.InstanceDefinition;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.PortalObjectContainer;
import org.jboss.portal.core.model.portal.Window;
import org.jboss.portal.migration.xml.mop.MContent;
import org.jboss.portal.migration.xml.mop.MPage;
import org.jboss.portal.migration.xml.mop.MPortlet;
import org.jboss.portal.migration.xml.mop.MSite;
import org.jboss.portal.migration.xml.mop.MWindow;
import org.jboss.portal.migration.xml.mop.SiteExporter;
import org.jboss.portal.portlet.Portlet;
import org.jboss.portal.portlet.PortletInvokerException;
import org.jboss.portal.portlet.info.MetaInfo;
import org.jboss.portal.portlet.info.PreferencesInfo;
import org.jboss.system.ServiceMBeanSupport;

/**
 * This MBean reads data from a live EPP 4.3 instance to create M* objects then convert those to XML.
 *
 * @author  theute
 */
public class JBPMOPExporter extends ServiceMBeanSupport implements JBPMOPExporterMBean {
    private PortalObjectContainer portalObjectContainer;
    private InstanceContainer     instanceContainer;

    @Override
    public void exportSites(final String fileName) throws Exception {
        try {
            TransactionManager tm = (TransactionManager) new InitialContext().lookup("java:/TransactionManager");

            Transactions.required(tm, new Transactions.Runnable() {
                                      public Object run() throws Exception {
                                          Collection<PortalObject> sites      = getSites();
                                          File                     outputFile = new File(fileName);

                                          if (!outputFile.exists()) {
                                              outputFile.createNewFile();
                                          }

                                          FileOutputStream fos = new FileOutputStream(outputFile, false);
                                          SiteExporter     se  = new SiteExporter();

                                          se.startExport(fos);

                                          try {
                                              for (PortalObject site : sites) {
                                                  se.exportSite(convert(site));
                                              }
                                          } catch (Exception e) {
                                              log.info("Error during user export: ", e);
                                          } finally {
                                              se.endExport();
                                              fos.close();
                                          }

                                          return null;
                                      }
                                  });
        } catch (Exception e) {
            log.info(e);
        }
    }

    private MSite convert(PortalObject site) {
        MSite msite = new MSite(site.getName());

        log.info("site: " + ReflectionToStringBuilder.toString(site));

        Collection<PortalObject> children = site.getChildren();

        for (PortalObject object : children) {
            System.out.println(object.getType() + " : " + ReflectionToStringBuilder.toString(object));
            log.info("obj: " + ReflectionToStringBuilder.toString(object));

            if (object.getType() == PortalObject.TYPE_PAGE) {
                msite.addPage(convertPage(object));
            } else {
                log.error("Unexpected child of " + site + " of type " + object.getType());
            }
        }

        return msite;
    }

    private MPage convertPage(PortalObject page) {
        MPage mpage = new MPage(page.getName());

        log.info("Portal Page: " + ReflectionToStringBuilder.toString(page));
        mpage.setProperties(page.getDeclaredProperties());

        Map<String, String> displayNames = new HashMap<String, String>();

        if (page.getDisplayName().hasValues()) {
            for (LocalizedString.Value v : page.getDisplayName().getValues().values()) {
                displayNames.put(v.getLocale().getLanguage(), v.getString());
            }

            log.info("Local values: " + displayNames);
        } else {
            log.info("No display name");
        }

        mpage.setDisplayNameIntl(displayNames);

        for (PortalObject object : page.getChildren()) {
            System.out.println(object);

            if (object.getType() == PortalObject.TYPE_PAGE) {
                mpage.addPage(convertPage(object));
            } else if (object.getType() == PortalObject.TYPE_WINDOW) {
                mpage.addWindow(convertWindow(object));
            } else {
                log.error("Unexpected child of " + page + " of type " + object.getType());
            }
        }

        return mpage;
    }

    private MWindow convertWindow(PortalObject object) {
        Window window = (Window) object;

        log.info("Window: " + ReflectionToStringBuilder.toString(object));

        MWindow mwindow = new MWindow(window.getName());

        mwindow.setContentType(window.getContentType().toString());
        mwindow.setProperties(window.getDeclaredProperties());
        mwindow.setURI(window.getContent().getURI());

        if (window.getContentType() == ContentType.PORTLET) {
            mwindow.setContent(convertPortletContent(window.getContent()));
        } else {
            log.info("Ignoring content of: " + window.getName());
        }

        return mwindow;
    }

    private MContent convertPortletContent(Content content) {
        MPortlet            mPortlet   = new MPortlet(content.getURI());
        Map<String, String> parameters = new HashMap<String, String>();

        log.info("Portlet: " + ReflectionToStringBuilder.toString(content));

        while (content.getParameterNames().hasNext()) {
            String key = content.getParameterNames().next();

            parameters.put(key, content.getParameter(key));
        }

        InstanceDefinition instanceDef = instanceContainer.getDefinition(content.getURI());

        try {
            Portlet         portlet = instanceDef.getPortlet();
            PreferencesInfo pi      = portlet.getInfo().getPreferences();

            for (String key : pi.getKeys()) {
                List<String>  prefValues    = pi.getPreference(key).getDefaultValue();
                StringBuilder finalPrefList = new StringBuilder("");

                for (String prefValue : prefValues) {
                    finalPrefList.append(prefValue);
                    finalPrefList.append(", ");
                }

                mPortlet.addPreference(key, StringUtils.substringBeforeLast(finalPrefList.toString(), ", "));
            }

            mPortlet.setPortletApplication(portlet.getInfo().getApplicationName());
            mPortlet.setPortletName(portlet.getInfo().getName());
            System.out.println("Default: " + portlet.getInfo().getMeta().getMetaValue(MetaInfo.TITLE).getDefaultString());
            mPortlet.setTitle(portlet.getInfo().getMeta().getMetaValue(MetaInfo.TITLE).getDefaultString());
        } catch (PortletInvokerException e) {
            e.printStackTrace();
        }

        mPortlet.setParameters(parameters);

        return mPortlet;
    }

    private Collection<PortalObject> getSites() {
        Collection<PortalObject> sites = portalObjectContainer.getContext().getChildren();

        return sites;
    }

    @Override
    public PortalObjectContainer getPortalObjectContainer() {
        return portalObjectContainer;
    }

    @Override
    public void setPortalObjectContainer(PortalObjectContainer portalObjectContainer) {
        this.portalObjectContainer = portalObjectContainer;
    }

    @Override
    public InstanceContainer getInstanceContainer() {
        return instanceContainer;
    }

    @Override
    public void setInstanceContainer(InstanceContainer instanceContainer) {
        this.instanceContainer = instanceContainer;
    }
}

package org.gatein.migration.jbp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jboss.portal.migration.xml.mop.MPage;
import org.jboss.portal.migration.xml.mop.MPortlet;
import org.jboss.portal.migration.xml.mop.MSite;
import org.jboss.portal.migration.xml.mop.MWindow;
import org.jboss.portal.migration.xml.mop.SiteImporter;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

public class GWImporter {
    private static Set<String>  layouts     = new HashSet<String>();
    private static Set<String>  regions     = new HashSet<String>();
    private static final String PORTAL_NAME = "newPortal";

    // for pages.xml
    private static List<EppPage> pageList = new ArrayList<EppPage>();

    /**
     * This exports the navigation.xml and pages.xml. These pages are imported directly by EPP. This should probably be incorporated into the Export
     * project and this project should be removed Users and Roles are handled by EPP without an intermediary
     *
     * @param  args
     */
    public static void main(String[] args) {
        // Test
        try {
            importSite("C:\\jboss\\sites-p.xml", "groundwork-monitor", "C:\\jboss\\");
            System.out.println("Layouts: " + layouts);
            System.out.println("Regions: " + regions);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("End");
    }

    public static void importSite(String inputFileName, String site, String pageAndNavXmlDir) throws Exception {
        if (inputFileName == null) {
            throw new IllegalArgumentException("Filename is null");
        }

        File inputFile = new File(inputFileName);

        if (!inputFile.exists()) {
            throw new IllegalArgumentException("File doesn't exist:" + inputFileName);
        }

        // Create input stream
        FileInputStream fis = new FileInputStream(inputFile);
        SiteImporter    si  = new SiteImporter();

        // Parse the XML document to obtain objects that can be manipulated.
        si.startImport(fis);

        // Get the very first portal
        MSite importedSite = si.getMSite();
        int   size         = 0;
        long  starttime    = System.currentTimeMillis();

        while (importedSite != null) {
            System.out.println("Importing site: " + importedSite.getName());

            if (!importedSite.getName().equals(site)) {
                System.out.println("Skipping import of site: " + importedSite.getName());
            } else {
                System.out.println("Importing of site: " + importedSite.getName());

                EppPage       eppMainPage = new EppPage();
                List<EppPage> pages       = new ArrayList<EppPage>();

                for (MPage page : importedSite.getPages()) {
                    EppPage ePage = getPageData(page, null);

                    pageList.add(ePage);
                    eppMainPage.addPage(ePage);

                    if ((page.getPages() != null) && (page.getPages().size() > 0)) {
                        for (MPage subPage : page.getPages()) {
                            EppPage eSubPage = getPageData(subPage, ePage.getNode().getPageReference());  // and so on

                            ePage.addPage(eSubPage);
                            pageList.add(eSubPage);
                        }
                    }
                }

                Configuration cfg = new Configuration();

                cfg.setTemplateLoader(new ClassTemplateLoader());

                Template            template = cfg.getTemplate("/pages.ftl");
                Map<String, Object> data     = new HashMap<String, Object>();

                pages.addAll(eppMainPage.getEppPageSet());
                data.put("pages", pageList);

                Writer file = new FileWriter(new File(pageAndNavXmlDir + "pages.xml"));

                template.process(data, file);
                file.flush();
                file.close();
                template = cfg.getTemplate("/navigation.ftl");
                data     = new HashMap<String, Object>();
                pages.addAll(eppMainPage.getEppPageSet());
                data.put("pages", eppMainPage.getEppPageSet());
                file = new FileWriter(new File(pageAndNavXmlDir + "navigation.xml"));
                template.process(data, file);
                file.flush();
                file.close();
            }

            importedSite = si.getMSite();
        }
    }

    /**
     * Will create an EPP 5 page metadata, this is also where layouts will be translated into UIContainers.
     *
     * @param   mpage
     * @param   portalKey
     * @param   portalName
     *
     * @return
     */
    private static EppPage getPageData(MPage mpage, String parentUri) {
        String layoutId = (mpage.getProperties().get("layout.id"));

        if (layoutId == null) {
            System.out.println("Page: " + mpage.getName() + " has no layout ");
        }

        layouts.add(mpage.getProperties().get("layout.id"));

        EppPage ePage = new EppPage();

        ePage.setLayout(mpage.getProperties().get("layout.id"));

        if (ePage.getLayout() == null) {
            ePage.setLayout("");
        }

        String orderNum = mpage.getProperties().get("order");

        if (orderNum != null) {
            ePage.setNodeOrder(Integer.valueOf(orderNum));
        }

        EppNode node = new EppNode();

        node.setPortalName(PORTAL_NAME);
        node.setParentUri("portal::" + PORTAL_NAME);

        // if (parentUri == null) {
        // node.setParentUri("portal::newPortal");
        // } else {
        // node.setParentUri(parentUri);
        // }
        // TODO these three are the same at the moment
        node.setName(mpage.getName());
        System.out.println("LABELS: " + mpage.getDisplayNameIntl());

        for (Map.Entry<String, String> entry : mpage.getDisplayNameIntl().entrySet()) {
            node.addLabel(entry.getKey(), entry.getValue().trim());
        }

        node.setUri(mpage.getName());
        node.setVisibility("DISPLAYED");
        ePage.setNode(node);
        ePage.setUrlName(mpage.getName());
        ePage.setDisplayName(mpage.getName());
        ePage.setTitle(mpage.getName());
        ePage.addAccess("*:/platform/users");
        ePage.setEditPermissions("*:/platform/administrators");
        ePage.setShowMaxWindow(false);

        // This section involves positioning of portlets on the page. It is
        // pretty basic right now. Needs to be improved
        List<EppPortlet> eppPortletList = new ArrayList<EppPortlet>();

        for (MWindow window : mpage.getWindows()) {
            if ((window != null) && window.getContentType().equals("portlet")) {
                eppPortletList.add(getWindowPortlet(window));
            } else {
                System.out.println("Ignoring: " + window.getName());
            }
        }

        int portletPriority = 1;

        for (EppPortlet leftPortlet : eppPortletList) {
            // leftContainerXmlPage += leftPortlet;
            leftPortlet.setPriority(portletPriority++);
            leftPortlet.setLocation("left");
            ePage.addPortlet(leftPortlet);
        }

        System.out.println("Portlets for: " + ePage.getDisplayName() + " " + ePage.getPortlets().size());

        return ePage;
    }

    private static EppPortlet getWindowPortlet(MWindow window) {
        MPortlet   portlet  = (MPortlet) window.getContent();
        EppPortlet ePortlet = new EppPortlet();

        ePortlet.setTitle(portlet.getDisplayName());

        PortletMapper p = PortletMapper.find(portlet.getPortletName());

        ePortlet.setApplicationRef(p.getApplicationRegRef());
        ePortlet.setPortletRef(p.getEppName());
        ePortlet.addPermission("*:/platform/users");
        ePortlet.setShowInfoBar(true);
        ePortlet.setShowApplicationMode(true);
        ePortlet.setShowApplicationState(true);
        ePortlet.setIcon("PortletIcon");
        ePortlet.setDescription("Desc");
        ePortlet.setRegion(window.getProperties().get("theme.region"));

        for (Map.Entry<String, String> entry : portlet.getPreferences().entrySet()) {
            ePortlet.addPreference(entry.getKey(), entry.getValue());
        }

        System.out.println("preferences: " + ePortlet.getPreferences());

        return ePortlet;
    }
}

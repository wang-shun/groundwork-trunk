package org.gatein.migration.jbp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class EppPage {
    private int              nodeOrder         = -1;
    private String           displayName;
    private String           title;
    private String           urlName;  // just one part of it
    private String           eppNode;
    private List<String>     accessPermissions = new ArrayList<String>();
    private String           editPermissions;
    private boolean          showMaxWindow;
    private Set<EppPage>     eppPageSet;
    private EppNode          node;
    private String           layout;
    private List<EppPortlet> portlets          = new ArrayList<EppPortlet>();

    public void addPage(EppPage page) {
        if (eppPageSet == null) {
            eppPageSet = new TreeSet<EppPage>(new Comparator<EppPage>() {
                        public int compare(EppPage thisOne, EppPage thatOne) {
                            System.out.println(thisOne.getUrlName() + " " + thatOne.getUrlName());

                            if (thisOne.getNodeOrder() == thatOne.getNodeOrder()) {
                                return thisOne.getUrlName().compareTo(thatOne.getUrlName());
                            }

                            return thisOne.getNodeOrder() - thatOne.getNodeOrder();
                        }
                    });
        }

        eppPageSet.add(page);
    }

    public List<EppPortlet> getPorltetsForTopRegion() {
        return getPorltetsForRegion("top");
    }

    public List<EppPortlet> getPorltetsForBottomRegion() {
        return getPorltetsForRegion("bottom");
    }

    public List<EppPortlet> getPorltetsForLeftRegion() {
        return getPorltetsForRegion("left");
    }

    public List<EppPortlet> getPorltetsForCenterCol1Region() {
        return getPorltetsForRegion("centerCol1");
    }

    public List<EppPortlet> getPorltetsForCenterCol2Region() {
        return getPorltetsForRegion("centerCol2");
    }

    public List<EppPortlet> getPorltetsForDashBottomRegion() {
        return getPorltetsForRegion("dash-bottom");
    }

    public List<EppPortlet> getPorltetsForRegion(String region) {
        List<EppPortlet> regionPortlets = new ArrayList<EppPortlet>();

        for (EppPortlet portlet : portlets) {
            if ((portlet.getRegion() != null) && portlet.getRegion().equals(region)) {
                regionPortlets.add(portlet);
            }
        }

        return regionPortlets;
    }

    public void addPortlet(EppPortlet portlet) {
        if (portlets == null) {
            portlets = new ArrayList<EppPortlet>();
        }

        portlets.add(portlet);
    }

    public void addAccess(String permissionTo) {
        if (accessPermissions == null) {
            accessPermissions = new ArrayList<String>();
        }

        accessPermissions.add(permissionTo);
    }

    public int getNodeOrder() {
        return nodeOrder;
    }

    public void setNodeOrder(int nodeOrder) {
        this.nodeOrder = nodeOrder;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrlName() {
        return urlName;
    }

    public void setUrlName(String urlName) {
        this.urlName = urlName;
    }

    public String getEppNode() {
        return eppNode;
    }

    public void setEppNode(String eppNode) {
        this.eppNode = eppNode;
    }

    public List<String> getAccessPermissions() {
        return accessPermissions;
    }

    public void setAccessPermissions(List<String> accessPermissions) {
        this.accessPermissions = accessPermissions;
    }

    public String getEditPermissions() {
        return editPermissions;
    }

    public void setEditPermissions(String editPermissions) {
        this.editPermissions = editPermissions;
    }

    public boolean isShowMaxWindow() {
        return showMaxWindow;
    }

    public void setShowMaxWindow(boolean showMaxWindow) {
        this.showMaxWindow = showMaxWindow;
    }

    public Set<EppPage> getEppPageSet() {
        return eppPageSet;
    }

    public void setEppPageSet(Set<EppPage> eppPageSet) {
        this.eppPageSet = eppPageSet;
    }

    public List<EppPage> getEppPageList() {
        if (eppPageSet == null) {
            return new ArrayList<EppPage>();
        }

        return new ArrayList<EppPage>(eppPageSet);
    }

    public EppNode getNode() {
        return node;
    }

    public void setNode(EppNode node) {
        this.node = node;
    }

    public List<EppPortlet> getPortlets() {
        return portlets;
    }

    public void setPortlets(List<EppPortlet> portlets) {
        this.portlets = portlets;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }
}

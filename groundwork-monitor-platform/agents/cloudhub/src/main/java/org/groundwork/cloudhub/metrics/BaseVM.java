package org.groundwork.cloudhub.metrics;

import org.apache.log4j.Logger;
import org.groundwork.cloudhub.connectors.ConnectorConstants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BaseVM extends BaseProperties implements MetricProvider {
    private static Logger log = Logger.getLogger(BaseVM.class);
    private static SimpleDateFormat sdf = new SimpleDateFormat(ConnectorConstants.gwosDateFormat);
    private static final Date nullDate = new Date(); // was (0L);

    private String vmName = null;
    private String systemName = null;
    private String hypervisor = null;
    private String vmGroup = null;
    private String macAddress = null;
    private String ipAddress = null;
    private String guestState = null;
    private Date bootDate = null;
    private Date lastDate = null;   // datetime of last update
    private long upTime = 0;      // in milliseconds
    private long biasTime = 0;      // to correct for bad server time
    private String currRunState = null;   // synthetic 'up/down/warning/critical' // stuff.
    // 2017-02-07
    // removing pending default state for backward compatibility
    // when cloudhub is restarted, the state shouldn't go to PENDING
    private String prevRunState = ""; // "PENDING";
    private String currRunStateExtra = null;   // receiving extra State guidance
    private ConcurrentHashMap<String, BaseMetric> metricPool = new ConcurrentHashMap<String, BaseMetric>();
    private ConcurrentHashMap<String, BaseMetric> configPool = new ConcurrentHashMap<String, BaseMetric>();
    private int mergeSkipped = 0;      // counts how many merges missed (seq.)
    private int mergeCount = 0;      // just counts number of merges.
    private String nextCheckTime = null;    // Next time the Hypervisor will be checked for status
    private String hostGroup = null;    // VM belongs to a Host Group with the name of the hypervisor

    private boolean ownedByAgent = true;
    private String gwosHostName = null;

    // -------------------------------------------------------------------
    // constructors
    // -------------------------------------------------------------------
    protected BaseVM() {}

    public BaseVM(String name) {
        vmName = name;
    }

    // -------------------------------------------------------------------
    // getters
    // -------------------------------------------------------------------
    public String getHypervisor() {
        return hypervisor;
    }

    public String getVMName() {
        return vmName;
    }
    public String getName() {
        return vmName;
    }

    public String getSystemName() {
        return systemName;
    }

    public String getVmGroup() {
        return vmGroup;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getRunState() {
        return currRunState;
    }

    public String getRunExtra() {
        return currRunStateExtra;
    }

    public String getPrevRunState() {
        return prevRunState;
    }

    public void setPrevRunState(String prevRunState) {
        this.prevRunState = prevRunState;
    }

    public String getGuestState() {
        return guestState;
    }

    public int getMergeCount() {
        return mergeCount;
    }

    public int incrementMergeCount() {
        mergeCount++;
        return mergeCount;
    }

    public int getSkipCount() {
        return mergeSkipped;
    }

    public String getHostGroup() {
        return hostGroup;
    }

    /* Setter for Hostgroup */
    public void setHostGroup(String group) {
        hostGroup = group;
    }

    /**
     * This method has a side-effect of mutating lastDate state on a getter
     *
     * @return
     */
    public String getLastUpdate() {

        if (bootDate != null) {
            if (biasTime == 0)
                biasTime = System.currentTimeMillis() - (bootDate.getTime() + upTime);

            Date d = new Date(bootDate.getTime() + upTime + biasTime);
            lastDate = d;
            return sdf.format(lastDate).toString();
        } else {
            return sdf.format(nullDate).toString();
        }
    }

    public ConcurrentHashMap<String, BaseMetric> getMetricPool() {
        return metricPool;
    }

    public ConcurrentHashMap<String, BaseMetric> getConfigPool() {
        return configPool;
    }

    public BaseMetric getMetric(String key) {
        return metricPool.get(key);
    }

    public BaseMetric getConfig(String key) {
        return configPool.get(key);
    }

    public String getNextCheckTime() {
        return nextCheckTime;
    }

    public void setNextCheckTime(String nextCheckTime) {
        this.nextCheckTime = nextCheckTime;
    }

    public String getValueByKey(String key) {
        BaseMetric vbmo;
        if ((vbmo = getMetric(key)) == null
                && (vbmo = getConfig(key)) == null) {
            return null;
        }
        return vbmo.getCurrValue();
    }

    // -------------------------------------------------------------------
    // setters
    // -------------------------------------------------------------------
    public void setRunExtra(String extra) {
        currRunStateExtra = extra;
    }

    /**
     * This method should be used when migrating connectors, as it has no side effect on previous State
     *
     * @param state
     */
    public void setRunningState(String state) {
        currRunState = (state == null) ? "" : state;
    }

    @Deprecated  // this has the side effect of only allowing runState to be set once, overriding prevRunState
    public void setRunState(String state) {
        if (currRunState != null) {
            prevRunState = currRunState;
        }
        currRunState = (state == null) ? "" : state;
    }

    public boolean isStateChange() {
        if (currRunState != null) {
            if (prevRunState != null) {
                return !currRunState.equalsIgnoreCase(prevRunState);
            }
            else {
                return true;
            }
        }
        else if (prevRunState != null) {
            return true;
        }
        else {
            return false;
        }
    }

    @Deprecated // this method is only used by broken RHEV connector
    public boolean isStale(int minSkipped, int minTime) {
        int nowTime = (int) ((new Date()).getTime() / 1000);    // seconds
        int lasTime = (lastDate != null)
                ? (int) (lastDate.getTime() / 1000)
                : nowTime;
        int deltaT = nowTime - lasTime;

        if (deltaT < 0)
            deltaT = 0;

        return (mergeSkipped > minSkipped && deltaT > minTime);
    }

    @Deprecated
    public void incSkipped() {
        this.mergeSkipped++;
    }

    public void setHypervisor(String host) {
        hypervisor = host;
    }

    public void setVMName(String vm) {
        vmName = vm;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public void setVmGroup(String group) {
        vmGroup = group;
    }

    public void setMacAddress(String mac) {
        macAddress = mac;
    }

    public void setIpAddress(String ip) {
        ipAddress = ip;
    }

    public void setGuestState(String state) {
        guestState = state;
    }

    public void setBootDate(Calendar date) {
        if (date != null) {
            bootDate = date.getTime();
        }
    }

    @Deprecated
    public void setBootDate(String textDate, SimpleDateFormat sdf) {
        setBootDate(textDate, sdf, null);
    }

    @Deprecated
    public void setBootDate(String textDate, SimpleDateFormat sdf, SimpleDateFormat sdf2) {
        if (textDate == null || textDate.isEmpty()) {
            return;
        }
        try {
            bootDate = sdf.parse(textDate);
        } catch (Exception e) {
            if (sdf2 == null) {
                log.error( "date '" + textDate + "' didn't parse with '" + sdf.toPattern() + "' (e=" + e + ")" );
            } else {
                try {
                    bootDate = sdf2.parse(textDate);
                } catch (Exception e2) {
                    log.error("date '" + textDate + "' didn't parse with '" + sdf.toPattern() + "' (e=" + e + ") or '" +
                            sdf2.toPattern() + "' (e=" + e2 + ")");
                }
            }
        }
    }

    @Deprecated
    public void setBootDate(Date date) {
        bootDate = date;
    }

    // -------------------------------------------------------------------
    // converts 'seconds' to milliseconds (which needs to be a long)
    // then using that, converts the basetime to the last-update-time
    // and assigns it to the holder object for later use.
    // -------------------------------------------------------------------
    public void setLastUpdate() {
        if (bootDate == null)
            bootDate = new Date(System.currentTimeMillis());

        upTime = System.currentTimeMillis() - bootDate.getTime();
    }

    public void setLastUpdate(String uptimeSeconds) {
        if (uptimeSeconds == null)
            upTime = 0;
        else if (uptimeSeconds.isEmpty())
            upTime = 0;
        else
            upTime = 1000 * Integer.decode(uptimeSeconds).longValue();
    }

    public void putMetric(String key, BaseMetric value) {
        if (key.startsWith("-")) metricPool.remove(key.substring(1));
        else metricPool.put(key, value);
    }

    public void putConfig(String key, BaseMetric value) {
        if (key.startsWith("-")) configPool.remove(key.substring(1));
        else configPool.put(key, value);
    }

    @Deprecated // used by old VMWare and Redhat connectors only
    public void mergeInNew(BaseVM update) {
        if (update == null)
            return;

        if (this.vmName == null || update.vmName != null)
            this.vmName = update.vmName;

        this.hypervisor = update.hypervisor;
        this.vmGroup = update.vmGroup;
        this.guestState = update.guestState;
        this.ipAddress = update.ipAddress;
        this.macAddress = update.macAddress;
        this.bootDate = update.bootDate;
        this.lastDate = update.lastDate;
        this.upTime = update.upTime;
        this.prevRunState = this.currRunState == null
                ? ""
                : this.currRunState;
        this.currRunState = update.currRunState;
        this.currRunStateExtra = update.currRunStateExtra;
        this.mergeSkipped = 0;
        this.mergeCount++;

        for (String upMetric : update.metricPool.keySet()) {
            BaseMetric upMetricObj = update.metricPool.get(upMetric);
            if (upMetricObj == null)
                continue;

            BaseMetric myMetricObj = this.metricPool.get(upMetric);
            if (myMetricObj == null)
                this.metricPool.put(
                        upMetric,
                        myMetricObj = new BaseMetric(
                                upMetric,
                                upMetricObj.getThresholdWarning(),
                                upMetricObj.getThresholdCritical(),
                                upMetricObj.isGraphed(),
                                upMetricObj.isMonitored(),
                                upMetricObj.getCustomName()));

            myMetricObj.mergeInNew(upMetricObj);
        }

        for (String upConfig : update.configPool.keySet()) {
            BaseMetric upConfigObj = update.configPool.get(upConfig);
            if (upConfigObj == null)
                continue;

            BaseMetric myConfigObj = this.configPool.get(upConfig);
            if (myConfigObj == null) {
                this.configPool.put(
                        upConfig,
                        myConfigObj = new BaseMetric(
                                upConfig,
                                upConfigObj.getThresholdWarning(),
                                upConfigObj.getThresholdCritical(),
                                upConfigObj.isGraphed(),
                                upConfigObj.isMonitored(),
                                upConfigObj.getCustomName()));
            }
            myConfigObj.mergeInNew(upConfigObj);
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        s.append(String.format("%-40s: %s\n", "vmName", this.vmName));
        s.append(String.format("%-40s: %s\n", "hypervisor", this.hypervisor));
        s.append(String.format("%-40s: %s\n", "vmGroup", this.vmGroup));
        s.append(String.format("%-40s: %s\n", "macAddress", this.macAddress));
        s.append(String.format("%-40s: %s\n", "ipAddress", this.ipAddress));
        s.append(String.format("%-40s: %s\n", "guestState", this.guestState));
        s.append(String.format("%-40s: %s\n", "bootDate", this.bootDate == null
                ? "" : this.bootDate.toString()));
        s.append(String.format("%-40s: %s\n", "lastDate", this.lastDate == null
                ? "" : this.lastDate.toString()));
        s.append(String.format("%-40s: %d\n", "upTime", this.upTime));
        s.append(String.format("%-40s: %d\n", "biasTime", this.biasTime));
        s.append(String.format("%-40s: %s\n", "currRunState", this.currRunState));
        s.append(String.format("%-40s: %s\n", "prevRunState", this.prevRunState));
        s.append(String.format("%-40s: %s\n", "currRunStateExtra", this.currRunStateExtra));
        s.append(String.format("%-40s: %d\n", "mergeSkipped", this.mergeSkipped));
        s.append(String.format("%-40s: %d\n", "mergeCount", this.mergeCount));
        s.append(String.format("%-40s: %s\n", "nextCheckTime", this.nextCheckTime));
        s.append(String.format("%-40s: %s\n", "hostGroup", this.hostGroup));

        if (this.metricPool != null)
            for (String key : metricPool.keySet())
                s.append(String.format("\n%-40s: (metricpool (vm))\n%s", key, metricPool.get(key).toString()));

        if (this.configPool != null)
            for (String key : configPool.keySet())
                s.append(String.format("\n%-40s: (configpool (vm))\n%s", key, configPool.get(key).toString()));

        return s.toString();
    }

    public boolean isOwnedByAgent() {
        return ownedByAgent;
    }

    public void setOwnedByAgent(boolean ownedByAgent) {
        this.ownedByAgent = ownedByAgent;
    }

    public String getGwosHostName() {
        return gwosHostName;
    }

    public void setGwosHostName(String gwosHostName) {
        this.gwosHostName = gwosHostName;
    }

    public Map<String,Object> createMetricMap() {
        return BaseMetricsUtil.createMetricMap(getMetricPool());
    }

    public boolean isRunning(MetricCollectionState collector) {
        return true;
    }

}

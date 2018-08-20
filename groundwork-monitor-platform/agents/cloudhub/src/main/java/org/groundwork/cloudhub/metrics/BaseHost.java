package org.groundwork.cloudhub.metrics;

import org.apache.log4j.Logger;
import org.groundwork.cloudhub.connectors.ConnectorConstants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BaseHost extends BaseProperties implements MetricProvider {
    private static Logger log = Logger.getLogger(BaseHost.class);
    private static SimpleDateFormat sdf = new SimpleDateFormat(ConnectorConstants.gwosDateFormat);

    private static final Date nullDate = new Date();
    private String hostName = null;
    private String systemName = null;
    private String hostGroup = null;
    private String ipAddress = null;
    private String macAddress = null;
    private Date bootDate = null;
    private Date lastDate = null;    // datetime of last update
    private long upTime = 0;       // in milliseconds
    private long biasTime = 0;       // in milliseconds (clock bias)
    private String hostDescription = null;
    private String currRunState = null;    // synthetic 'up/down/warning/critical' stuff.
    // 2017-02-07
    // removing pending default state for backward compatibility
    // when cloudhub is restarted, the state shouldn't go to PENDING
    private String prevRunState = ""; // "PENDING";
    private String currRunStateExtra = null;    // receiving extra State guidance
    private int mergeSkipped = 0;       // counts how many merges missed (seq.)
    private int mergeCount = 0;       // just counts number of merges.

    private ConcurrentHashMap<String, BaseVM> vmPool = new ConcurrentHashMap<String, BaseVM>();

    private ConcurrentHashMap<String, BaseMetric> metricPool = new ConcurrentHashMap<String, BaseMetric>();

    private ConcurrentHashMap<String, BaseMetric> configPool = new ConcurrentHashMap<String, BaseMetric>();

    private boolean ownedByAgent = true;
    private String gwosHostName = null;
    private boolean isTransient = false;

    public BaseHost() {
    }

    public BaseHost(String host) {
        hostName = host;
    }

    public String getHostName() {
        return hostName;
    }
    public String getName() {
        return hostName;
    }
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getSystemName() {
        return systemName;
    }
    
    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getHostGroup() {
        return hostGroup;
    }

    public String getDescription() {
        return hostDescription;
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

    public int getMergeCount() {
        return mergeCount;
    }

    public int incrementMergeCount() {
        mergeCount++;
        return mergeCount;
    }

    public BaseMetric getMetric(String lu) {
        return metricPool.get(lu);
    }

    public BaseMetric getConfig(String lu) {
        return configPool.get(lu);
    }

    public BaseVM getVM(String lu) {
        return vmPool.get(lu);
    }

    public String getValueByKey(String key) {
        BaseMetric vbmo = null;
        if ((vbmo = getMetric(key)) == null
                && (vbmo = getConfig(key)) == null) {
            return null;
        }
        return vbmo.getCurrValue();
    }

    public ConcurrentHashMap<String, BaseVM> getVMPool() {
        return vmPool;
    }

    public ConcurrentHashMap<String, BaseMetric> getMetricPool() {
        return metricPool;
    }

    public ConcurrentHashMap<String, BaseMetric> getConfigPool() {
        return configPool;
    }

    public long getBootDateMillisec() {
        return bootDate == null ? nullDate.getTime() : bootDate.getTime();
    }

    public String getBootDate() {
        return (bootDate == null)
                ? sdf.format(nullDate).toString()
                : sdf.format(bootDate).toString();
    }

    public long getLastUpdateMillisec() {
        if (lastDate != null)
            return lastDate.getTime();
        return 0;
    }

    /**
     * This method has a side-effect of mutating lastDate state on a getter
     *
     * @return
     */
    public String getLastUpdate() {

        if (bootDate != null) {
            // --------------------------------------------------------------------
            // This is to compute a "bias time" for each host, based on the host's
            // feeling of a "boot date".  Which might be way off.  Doing this once
            // should retain the granularity of a host's self-sample period, which
            // doing the bias computation EVERY time would remove.
            // --------------------------------------------------------------------
            if (biasTime == 0)
                biasTime = System.currentTimeMillis() - (bootDate.getTime() + upTime);

            Date date = new Date(bootDate.getTime() + upTime + biasTime);
            lastDate = date;
            return sdf.format(lastDate).toString();
        } else {
            return sdf.format(nullDate).toString();
        }
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

    public void setRunExtra(String extra) {
        currRunStateExtra = extra;
    }

    public boolean isStateChange() {
        if (currRunState != null) {
            if (prevRunState != null) {
                return !currRunState.equalsIgnoreCase(prevRunState);
            } else {
                return true;
            }
        }
        else {
            if (prevRunState != null) {
                return true;
            }
            else {
                return false;
            }
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

    public void setDescription(String description) {
        hostDescription = description;
    }

    public void setHostGroup(String group) {
        hostGroup = group;
    }

    public void setIpAddress(String address) {
        ipAddress = address;
    }

    public void setMacAddress(String address) {
        macAddress = address;
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
                    log.error("date '" + textDate + "' didn't parse with '" + sdf.toPattern() + "' or '" +
                            sdf2.toPattern() + "' (e=" + e2 + ")");
                }
            }
        }
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

    public void clearVM() {
        vmPool.clear();
    }

    public void clearMetric() {
        metricPool.clear();
    }

    public void clearConfig() {
        configPool.clear();
    }

    public void putVM(String name, BaseVM vm) {
        if (name.startsWith("-")) vmPool.remove(name.substring(1));
        else vmPool.put(name, vm);
    }

    public void putMetric(String name, BaseMetric value) {
        if (name.startsWith("-")) metricPool.remove(name.substring(1));
        else metricPool.put(name, value);
    }

    public void putConfig(String name, BaseMetric value) {
        if (name.startsWith("-")) configPool.remove(name.substring(1));
        else configPool.put(name, value);
    }

    public void renameVM(String oldname, String newname) {
        BaseVM vmo = this.vmPool.get(oldname);
        if (vmo != null) {
            this.vmPool.remove(oldname);
            this.vmPool.put(newname, vmo);
        }
        return;
    }

    @Deprecated // used by old VMWare and Redhat connectors only
    public void mergeInNew(BaseHost update) {
        if (update == null)    // nothing to do, for now
            return;

        this.hostName = update.hostName;
        this.hostGroup = update.hostGroup;
        this.ipAddress = update.ipAddress;
        this.macAddress = update.macAddress;
        this.bootDate = update.bootDate;
        this.lastDate = update.lastDate;
        this.upTime = update.upTime;
        this.hostDescription = update.hostDescription;
        this.prevRunState = this.currRunState == null
                ? ""
                : this.currRunState;
        this.currRunState = update.currRunState;
        this.currRunStateExtra = update.currRunStateExtra;
        this.mergeSkipped = 0;
        this.mergeCount++;

        for (String upvm : update.vmPool.keySet()) {
            // log.info( "update vm: '" + upvm + "'" );
            BaseVM upvmObj = update.vmPool.get(upvm);
            if (upvmObj == null)
                continue;

            BaseVM myvmObj = this.vmPool.get(upvm);
            if (myvmObj == null)   // have to create a receiving one?
                continue;           // but can't!  This is a superclass.

            // log.info( "update vm2 '" + upvm + "'" );
            myvmObj.mergeInNew(upvmObj);
        }

        for (String upMetric : update.metricPool.keySet()) {
            BaseMetric upMetricObj = update.metricPool.get(upMetric);
            if (upMetricObj == null)
                continue;

            BaseMetric myMetricObj = this.metricPool.get(upMetric);
            if (myMetricObj == null)
                this.metricPool.put(upMetric, myMetricObj =
                        new BaseMetric(
                                upMetric,
                                upMetricObj.getThresholdWarning(),
                                upMetricObj.getThresholdCritical(),
                                upMetricObj.isGraphed(),
                                upMetricObj.isMonitored(),
                                upMetricObj.getCustomName()));

            myMetricObj.mergeInNew(upMetricObj);
        }

        for (String upConfig : update.configPool.keySet()) {
            // log.info( "update config: '" + upConfig + "'" );
            BaseMetric upConfigObj = update.configPool.get(upConfig);
            if (upConfigObj == null)
                continue;

            BaseMetric myConfigObj = this.configPool.get(upConfig);
            if (myConfigObj == null)
                this.configPool.put(upConfig, myConfigObj =
                        new BaseMetric(
                                upConfig,
                                upConfigObj.getThresholdWarning(),
                                upConfigObj.getThresholdCritical(),
                                upConfigObj.isGraphed(),
                                upConfigObj.isMonitored(),
                                upConfigObj.getCustomName()));

            myConfigObj.mergeInNew(upConfigObj);
        }
        // log.info( "update done:" );
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        s.append(String.format("%-40s: '%s'\n", "hostName", this.hostName));
        s.append(String.format("%-40s: '%s'\n", "hostGroup", this.hostGroup));
        s.append(String.format("%-40s: '%s'\n", "ipAddress", this.ipAddress));
        s.append(String.format("%-40s: '%s'\n", "macAddress", this.macAddress));
        s.append(String.format("%-40s: '%s'\n", "bootDate", this.bootDate == null
                ? "" : this.bootDate.toString()));
        s.append(String.format("%-40s: '%s'\n", "lastDate", this.lastDate == null
                ? "" : this.lastDate.toString()));
        s.append(String.format("%-40s: '%d'\n", "upTime", this.upTime));
        s.append(String.format("%-40s: '%d'\n", "biasTime", this.biasTime));
        s.append(String.format("%-40s: '%s'\n", "hostDescription", this.hostDescription));
        s.append(String.format("%-40s: '%s'\n", "currRunState", this.currRunState));
        s.append(String.format("%-40s: '%s'\n", "prevRunState", this.prevRunState));
        s.append(String.format("%-40s: '%s'\n", "currRunStateExtra", this.currRunStateExtra));
        s.append(String.format("%-40s: '%d'\n", "mergeSkipped", this.mergeSkipped));
        s.append(String.format("%-40s: '%s'\n", "mergeCount", this.mergeCount));
    
        if (this.vmPool != null)
            for (String key : this.vmPool.keySet())
                s.append(String.format("\n%-40s: (vmpool (host))\n%s",
                        key, this.vmPool.get(key).toString()));

        if (this.metricPool != null)
            for (String key : this.metricPool.keySet())
                s.append(String.format("\n%-40s: (metricpool (host))\n%s",
                        key, this.metricPool.get(key).toString()));

        if (this.configPool != null)
            for (String key : this.configPool.keySet())
                s.append(String.format("\n%-40s: (configpool (host))\n%s",
                        key, this.configPool.get(key).toString()));

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

    public boolean isTransient() {
        return isTransient;
    }

    public void setTransient(boolean isTransient) {
        this.isTransient = isTransient;
    }

    public Map<String,Object> createMetricMap() {
        return BaseMetricsUtil.createMetricMap(getMetricPool());
    }

    public boolean isRunning(MetricCollectionState collector) {
        return true;
    }

}

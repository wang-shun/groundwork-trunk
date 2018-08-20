package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "perfData")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoPerfData {

    @XmlAttribute
    private String appType;
    @XmlAttribute
    private String serverName;
    @XmlAttribute
    private Long serverTime;
    @XmlAttribute
    private String serviceName;
    @XmlAttribute
    private String label;
    @XmlAttribute
    private String value;
    @XmlAttribute
    private String warning;
    @XmlAttribute
    private String critical;

    @XmlElementWrapper(name="tagNames")
    @XmlElement(name="tagName")
    private List<String> tagNames;
    @XmlElementWrapper(name="tagValues")
    @XmlElement(name="tagValue")
    private List<String> tagValues;

    @XmlAttribute
    private String componentTag;
    @XmlAttribute
    private String segmentTag;
    @XmlAttribute
    private String elementTag;
    @XmlAttribute
    private String portTag;
    @XmlAttribute
    private String vlanTag;
    @XmlAttribute
    private String cpuTag;
    @XmlAttribute
    private String interfaceTag;
    @XmlAttribute
    private String subinterfaceTag;
    @XmlAttribute
    private String httpMethodTag;
    @XmlAttribute
    private String httpCodeTag;
    @XmlAttribute
    private String deviceTag;
    @XmlAttribute
    private String whatTag;
    @XmlAttribute
    private String typeTag;
    @XmlAttribute
    private String resultTag;
    @XmlAttribute
    private String binMaxTag;
    @XmlAttribute
    private String directionTag;
    @XmlAttribute
    private String mTypeTag;
    @XmlAttribute
    private String unitTag;
    @XmlAttribute
    private String fileTag;
    @XmlAttribute
    private String lineTag;
    @XmlAttribute
    private String envTag;

    public DtoPerfData() {}

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public Long getServerTime() {
        return serverTime;
    }

    public void setServerTime(Long serverTime) {
        this.serverTime = serverTime;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    public String getCritical() {
        return critical;
    }

    public void setCritical(String critical) {
        this.critical = critical;
    }

    public List<String> getTagNames() {
        return tagNames;
    }

    public void setTagNames(List<String> tagNames) {
        this.tagNames = tagNames;
    }

    public List<String> getTagValues() {
        return tagValues;
    }

    public void setTagValues(List<String> tagValues) {
        this.tagValues = tagValues;
    }

    public String getComponentTag() {
        return componentTag;
    }

    public void setComponentTag(String componentTag) {
        this.componentTag = componentTag;
    }

    public String getSegmentTag() {
        return segmentTag;
    }

    public void setSegmentTag(String segmentTag) {
        this.segmentTag = segmentTag;
    }

    public String getElementTag() {
        return elementTag;
    }

    public void setElementTag(String elementTag) {
        this.elementTag = elementTag;
    }

    public String getPortTag() {
        return portTag;
    }

    public void setPortTag(String portTag) {
        this.portTag = portTag;
    }

    public String getVlanTag() {
        return vlanTag;
    }

    public void setVlanTag(String vlanTag) {
        this.vlanTag = vlanTag;
    }

    public String getCpuTag() {
        return cpuTag;
    }

    public void setCpuTag(String cpuTag) {
        this.cpuTag = cpuTag;
    }

    public String getInterfaceTag() {
        return interfaceTag;
    }

    public void setInterfaceTag(String interfaceTag) {
        this.interfaceTag = interfaceTag;
    }

    public String getSubinterfaceTag() {
        return subinterfaceTag;
    }

    public void setSubinterfaceTag(String subinterfaceTag) {
        this.subinterfaceTag = subinterfaceTag;
    }

    public String getHttpMethodTag() {
        return httpMethodTag;
    }

    public void setHttpMethodTag(String httpMethodTag) {
        this.httpMethodTag = httpMethodTag;
    }

    public String getHttpCodeTag() {
        return httpCodeTag;
    }

    public void setHttpCodeTag(String httpCodeTag) {
        this.httpCodeTag = httpCodeTag;
    }

    public String getDeviceTag() {
        return deviceTag;
    }

    public void setDeviceTag(String deviceTag) {
        this.deviceTag = deviceTag;
    }

    public String getWhatTag() {
        return whatTag;
    }

    public void setWhatTag(String whatTag) {
        this.whatTag = whatTag;
    }

    public String getTypeTag() {
        return typeTag;
    }

    public void setTypeTag(String typeTag) {
        this.typeTag = typeTag;
    }

    public String getResultTag() {
        return resultTag;
    }

    public void setResultTag(String resultTag) {
        this.resultTag = resultTag;
    }

    public String getBinMaxTag() {
        return binMaxTag;
    }

    public void setBinMaxTag(String binMaxTag) {
        this.binMaxTag = binMaxTag;
    }

    public String getDirectionTag() {
        return directionTag;
    }

    public void setDirectionTag(String directionTag) {
        this.directionTag = directionTag;
    }

    public String getMTypeTag() {
        return mTypeTag;
    }

    public void setMTypeTag(String mTypeTag) {
        this.mTypeTag = mTypeTag;
    }

    public String getUnitTag() {
        return unitTag;
    }

    public void setUnitTag(String unitTag) {
        this.unitTag = unitTag;
    }

    public String getFileTag() {
        return fileTag;
    }

    public void setFileTag(String fileTag) {
        this.fileTag = fileTag;
    }

    public String getLineTag() {
        return lineTag;
    }

    public void setLineTag(String lineTag) {
        this.lineTag = lineTag;
    }

    public String getEnvTag() {
        return envTag;
    }

    public void setEnvTag(String envTag) {
        this.envTag = envTag;
    }
}

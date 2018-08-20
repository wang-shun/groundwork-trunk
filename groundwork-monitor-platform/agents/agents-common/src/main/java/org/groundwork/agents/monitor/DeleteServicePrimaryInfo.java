package org.groundwork.agents.monitor;

/**
 * Created by dtaylor on 8/8/17.
 */
public class DeleteServicePrimaryInfo {

    private String hostName;
    private String serviceName;
    private Integer id;

    public DeleteServicePrimaryInfo(String hostName, String serviceName, Integer id) {
        this.serviceName = serviceName;
        this.hostName = hostName;
        this.id = id;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(hostName);
        buffer.append(":");
        buffer.append(serviceName);
        if (id != null) {
            buffer.append("-");
            buffer.append(id);
        }
        return buffer.toString();
    }
}

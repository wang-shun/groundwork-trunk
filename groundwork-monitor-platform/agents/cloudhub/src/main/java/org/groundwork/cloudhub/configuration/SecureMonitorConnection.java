package org.groundwork.cloudhub.configuration;

public interface SecureMonitorConnection extends MonitorConnection {

    String getUsername();

    void setUsername(String username);

    String getPassword();

    void setPassword(String password);

    boolean isSslEnabled();

    void setSslEnabled(boolean sslEnabled);

}

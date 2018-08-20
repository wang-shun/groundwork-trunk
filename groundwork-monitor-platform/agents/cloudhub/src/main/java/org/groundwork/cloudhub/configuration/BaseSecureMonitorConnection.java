package org.groundwork.cloudhub.configuration;

import org.hibernate.validator.constraints.NotBlank;

import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
public abstract class BaseSecureMonitorConnection extends BaseMonitorConnection implements SecureMonitorConnection {

    @NotBlank(message="User name cannot be empty.")
    protected String username;

    @NotBlank (message="Password cannot be empty.")
    protected String password;

    protected boolean sslEnabled = true;

    public BaseSecureMonitorConnection() {
        super();
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}

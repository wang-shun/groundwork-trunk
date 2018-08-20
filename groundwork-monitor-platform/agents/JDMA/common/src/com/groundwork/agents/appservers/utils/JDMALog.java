package com.groundwork.agents.appservers.utils;

/**
 * Created by dtaylor on 5/19/15.
 */
public class JDMALog {

    public JDMALog() {}

    public void debug(String message) {
        if (isDebugEnabled()) {
            System.out.println(message);
        }
    }

    public void info(String message) {
        if (isInfoEnabled()) {
            System.out.println(message);
        }
    }

    public void error(String message) {
        System.out.println(message);
    }

    public void error(String message, Exception e) {
        System.out.println(message);
        e.printStackTrace();
    }

    public boolean isDebugEnabled() {
        return false;
    }

    public boolean isErrorEnabled() {
        return true;
    }

    public boolean isInfoEnabled() {
        return true;
    }


}

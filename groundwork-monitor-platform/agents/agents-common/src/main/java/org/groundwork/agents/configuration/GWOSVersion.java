package org.groundwork.agents.configuration;

public enum GWOSVersion {
    version_67,
    version_70,
    version_71,
    unknown;

    public static GWOSVersion determineVersion(String version) {
        if (version == null)
            return unknown;
        if (version.startsWith("6"))
            return version_67;
        if (version.startsWith("7.0"))
            return version_70;
        if (version.startsWith("7.1"))
            return version_71;
        if (version.startsWith("7.2"))
            return version_71;
        return unknown;
    }

}





package org.groundwork.rs.it;

import org.groundwork.rs.dto.DtoHost;

import java.util.Comparator;

public abstract class IntegrationTestGenerator {

    public static final String FORMAT_NUMBER_SUFFIX = "%05d";

    static class HostComparator implements Comparator<DtoHost> {
        @Override
        public int compare(DtoHost d1, DtoHost d2) {
            return d1.getHostName().compareTo(d2.getHostName());
        }
    }

}

package org.groundwork.rs.dto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum DtoSortType {
    /**
     * No sort order specified
     */
    None,
    /**
     * Sort the result set in ascending order
     */
    Ascending,
    /**
     * Sort the result set in descending order
     */
    Descending;

    // Jax-RS annotations require a constant for default values
    public final static String DEFAULT = "None";

    public static class DtoSortWrapper {
        private static final Map<String, DtoSortType> MAPPER = Collections
                .unmodifiableMap(new HashMap<String, DtoSortType>() {
                    {
                        put(None.name().toLowerCase(), DtoSortType.None);
                        put(Ascending.name().toLowerCase(), DtoSortType.Ascending);
                        put(Descending.name().toLowerCase(), DtoSortType.Descending);
                    }
                });

        private DtoSortType type;

        public static DtoSortWrapper valueOf(String value) {
            DtoSortType type = DtoSortWrapper.MAPPER.get(value.toLowerCase());
            if (type == null) {
                // if nothing found just set the desired default value
                type = DtoSortType.None;
            }
            return new DtoSortWrapper(type);
        }

        private DtoSortWrapper(DtoSortType type) {
            this.type = type;
        }

        public DtoSortType getType() {
            return this.type;
        }
    }

}

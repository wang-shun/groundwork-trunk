package org.groundwork.rs.dto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines depth of requests for Collage entities
 */
public enum DtoDepthType {
    /**
     * Returns primitive data members, names of 1:1 associated entities
     */
    Simple,
    /**
     * Returns Simple attributes plus dynamic properties
     */
    Shallow,
    /**
     * Returns Shallow plus statistics properties,
     * plus collections shallow, 1:1 associated entities shallow
     */
    Deep,
    /**
     * Returns Deep attributes, plus collections deep, 1:1 associations deep
     */
    Full,
    /**
     * Returns selected data members intended to be used in the synchronization
     * process for entities managed externally. Typically returns Simple data
     * members for other entities.
     */
    Sync;

    // Jax-RS annotations require a constant for default values
    public final static String DEFAULT = "Shallow";

    public static class DtoDepthWrapper {
        private static final Map<String, DtoDepthType> MAPPER = Collections
                .unmodifiableMap(new HashMap<String, DtoDepthType>() {
                    {
                        put(Simple.name().toLowerCase(), DtoDepthType.Simple);
                        put(Shallow.name().toLowerCase(), DtoDepthType.Shallow);
                        put(Deep.name().toLowerCase(), DtoDepthType.Deep);
                        put(Full.name().toLowerCase(), DtoDepthType.Full);
                        put(Sync.name().toLowerCase(), DtoDepthType.Sync);
                    }
                });

        private DtoDepthType type;

        public static DtoDepthWrapper valueOf(String value) {
            DtoDepthType type = DtoDepthWrapper.MAPPER.get(value.toLowerCase());
            if (type == null) {
                // if nothing found just set the desired default value
                type = DtoDepthType.Shallow;
            }
            return new DtoDepthWrapper(type);
        }

        private DtoDepthWrapper(DtoDepthType type) {
            this.type = type;
        }

        public DtoDepthType getType() {
            return this.type;
        }
    }

}


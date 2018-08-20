package org.groundwork.rs.dto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines depth of requests for Collage entities
 */
public enum DtoPropertyDataType {
    DATE,
    BOOLEAN,
    STRING,
    INTEGER,
    LONG,
    DOUBLE;

    // Jax-RS annotations require a constant for default values
    public final static String DEFAULT = "STRING";

    public static class DtoPropertyDataTypeWrapper {
        private static final Map<String, DtoPropertyDataType> MAPPER = Collections
                .unmodifiableMap(new HashMap<String, DtoPropertyDataType>() {
                    {
                        put(DATE.name().toUpperCase(), DtoPropertyDataType.DATE);
                        put(BOOLEAN.name().toUpperCase(), DtoPropertyDataType.BOOLEAN);
                        put(STRING.name().toUpperCase(), DtoPropertyDataType.STRING);
                        put(INTEGER.name().toUpperCase(), DtoPropertyDataType.INTEGER);
                        put(LONG.name().toUpperCase(), DtoPropertyDataType.LONG);
                        put(DOUBLE.name().toUpperCase(), DtoPropertyDataType.DOUBLE);
                    }
                });

        private DtoPropertyDataType type;

        public static DtoPropertyDataTypeWrapper valueOf(String value) {
            DtoPropertyDataType type = DtoPropertyDataTypeWrapper.MAPPER.get(value.toUpperCase());
            if (type == null) {
                // if nothing found just set the desired default value
                type = DtoPropertyDataType.STRING;
            }
            return new DtoPropertyDataTypeWrapper(type);
        }

        private DtoPropertyDataTypeWrapper(DtoPropertyDataType type) {
            this.type = type;
        }

        public DtoPropertyDataType getType() {
            return this.type;
        }
    }

}


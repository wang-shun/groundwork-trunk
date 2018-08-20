package com.groundwork.collage.metrics;

import com.codahale.metrics.Gauge;
import com.groundwork.collage.CollageFactory;
import org.apache.commons.lang.StringUtils;

import java.util.concurrent.ConcurrentHashMap;

public class CollageGauge implements Gauge<Long>  {

    private long value = 0;

    void setValue(Long value) {
        this.value = value;
    }

    public Long getValue() {
        return value;
    }
}

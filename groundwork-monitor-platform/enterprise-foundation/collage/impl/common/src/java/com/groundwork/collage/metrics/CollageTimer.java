package com.groundwork.collage.metrics;

import com.codahale.metrics.Timer;

public class CollageTimer {

    private Timer.Context context;

    CollageTimer(final Timer.Context context) {
        this.context = context;
    }

    void stop() {
        context.stop();
    }
}

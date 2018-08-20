package com.groundwork.collage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;

public class FoundationLifecycleManager {

    protected static Log log = LogFactory.getLog(FoundationLifecycleManager.class);

    private ArrayList<Closeable> closeables = new ArrayList<>();

    public void register(Closeable closeable) {
        closeables.add(closeable);
    }
    public void close() {
        for (Closeable closeable: closeables) {
            try {
                closeable.close();
            } catch (IOException e) {
                log.error(e);
            }
        }
    }

}

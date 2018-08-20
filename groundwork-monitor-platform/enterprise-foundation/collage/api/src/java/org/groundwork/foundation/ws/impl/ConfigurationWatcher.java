package org.groundwork.foundation.ws.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.*;

public class ConfigurationWatcher implements Runnable {

    private static Log log = LogFactory.getLog(ConfigurationWatcher.class);

    private static int FOLLOW_SYMLINKS_LIMIT = 16;
    private static long MAX_SHUTDOWN_WAIT = 2500;

    private WatchService watchService = null;
    private Map<WatchKey,List<ConfigurationWatcherListenerContext>> listeners = null;
    private ExecutorService executor = null;

    private static ConfigurationWatcher singleton = null;

    public static synchronized void registerListener(ConfigurationWatcherNotificationListener listener, String fileToWatch) {
        if (singleton == null) {
            singleton = new ConfigurationWatcher();
        }
        singleton.registerNotificationListener(listener, fileToWatch);
    }

    public static void shutDown() {
        if (singleton != null) {
            singleton.shutDownWatcher();
        }
    }

    private ConfigurationWatcher() {
        try {
            listeners = new HashMap<>();
            watchService = FileSystems.getDefault().newWatchService();
            executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setDaemon(true);
                    return t;
                }
            });
            executor.submit(this);
            if (log.isInfoEnabled()) {
                log.info("Foundation Configuration Watcher successfully initialized");
            }
        }
        catch (Throwable  e) {
            log.error("Failed to initialize fail system for ConfigurationWatcher. Disabling.", e);
        }
    }

    private void registerNotificationListener(ConfigurationWatcherNotificationListener listener, String fileToWatch) {
        try {
            Path filePath = FileSystems.getDefault().getPath(fileToWatch);
            Path notifyPath = filePath.getFileName();
            for (int iteration = 0; Files.isSymbolicLink(filePath); iteration++) {
                if (iteration > FOLLOW_SYMLINKS_LIMIT) {
                    throw new RuntimeException("Symlink limit reached following: "+fileToWatch);
                }
                filePath = filePath.getParent().resolve(Files.readSymbolicLink(filePath));
            }
            Path watchPath = filePath.getParent();
            WatchKey key = watchPath.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            if (!listeners.containsKey(key)) {
                listeners.put(key, new ArrayList<ConfigurationWatcherListenerContext>());
            }
            listeners.get(key).add(new ConfigurationWatcherListenerContext(listener, notifyPath, filePath));
            if (log.isInfoEnabled()) {
                log.info("Registered configuration watcher listener for file " + fileToWatch);
            }
        }
        catch (IOException | InvalidPathException e) {
            log.error("Failed resolve or register file " + fileToWatch, e);
        }

    }

    @Override
    public void run() {
        if (log.isInfoEnabled()) {
            log.info("Starting ConfigurationWatcher Thread...");
        }
        WatchKey key = null;
        while (true) {
            try {
                key = watchService.take();
                List<ConfigurationWatcherListenerContext> contexts = listeners.get(key);
                if (contexts == null) {
                    log.error("Failed to find listener for key " + key);
                    key.reset();
                    continue;
                }
                for (WatchEvent event : key.pollEvents()) {
                    WatchEvent.Kind kind = event.kind();
                    Path path = (Path)event.context();
                    for (ConfigurationWatcherListenerContext context: contexts) {
                        if (context.filePath.getFileName().equals(path.getFileName())) {
                            if (log.isInfoEnabled()) {
                                log.info(String.format("Notifying change for file %s and kind %s", path, kind.name()));
                            }
                            context.listener.notifyChange(context.notifyPath);
                        }
                    }
                }
            } catch (InterruptedException e) {
                log.error("InterruptedException: " + e.getMessage());
                break;
            } catch (ClosedWatchServiceException cwse) {
                break;
            }
            key.reset();
        }
        if (log.isInfoEnabled()) {
            log.info("Exiting ConfigurationWatcher Thread...");
        }
    }

    public void shutDownWatcher() {
        if ((executor !=  null) && !executor.isShutdown()) {
            executor.shutdown();
            try {
                watchService.close();
            } catch (IOException ioe) {
            }
            try {
                executor.awaitTermination(MAX_SHUTDOWN_WAIT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ie) {
            }
        }
    }

    private class ConfigurationWatcherListenerContext {

        private ConfigurationWatcherNotificationListener listener;
        private Path notifyPath;
        private Path filePath;

        private ConfigurationWatcherListenerContext(ConfigurationWatcherNotificationListener listener,
                                                    Path notifyPath, Path filePath) {
            this.listener = listener;
            this.notifyPath = notifyPath;
            this.filePath = filePath;
        }

    }
}

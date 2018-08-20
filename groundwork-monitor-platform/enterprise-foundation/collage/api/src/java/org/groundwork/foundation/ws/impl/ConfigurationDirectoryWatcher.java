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

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class ConfigurationDirectoryWatcher implements Runnable {

    private static Log log = LogFactory.getLog(ConfigurationDirectoryWatcher.class);

    private static int FOLLOW_SYMLINKS_LIMIT = 16;
    private static long MAX_SHUTDOWN_WAIT = 2500;

    private WatchService watchService = null;
    private Map<WatchKey,List<DirectoryWatcherNotificationListenerContext>> listeners = null;
    private ExecutorService executor = null;

    private static ConfigurationDirectoryWatcher singleton = null;

    public static synchronized void registerListener(DirectoryWatcherNotificationListener listener, String fileToWatch) {
        if (singleton == null) {
            singleton = new ConfigurationDirectoryWatcher();
        }
        singleton.registerNotificationListener(listener, fileToWatch);
    }

    public static synchronized void unregisterListener(DirectoryWatcherNotificationListener listener, String path) {
        if (singleton == null) {
            singleton = new ConfigurationDirectoryWatcher();
        } else {
            singleton.unRegisterNotificationListener(listener, path);
        }
    }
    public static void shutDown() {
        if (singleton != null) {
            singleton.shutDownWatcher();
        }
    }

    private ConfigurationDirectoryWatcher() {
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
                log.info("Foundation Configuration Directory Watcher successfully initialized");
            }
        }
        catch (Throwable  e) {
            log.error("Failed to initialize fail system for Directory ConfigurationWatcher. Disabling.", e);
        }
    }

    private void registerNotificationListener(DirectoryWatcherNotificationListener listener, String dirToWatch) {
        try {
            Path filePath = FileSystems.getDefault().getPath(dirToWatch);
            Path notifyPath = filePath.getFileName();
            for (int iteration = 0; Files.isSymbolicLink(filePath); iteration++) {
                if (iteration > FOLLOW_SYMLINKS_LIMIT) {
                    throw new RuntimeException("Symlink limit reached following: "+ dirToWatch);
                }
                filePath = filePath.getParent().resolve(Files.readSymbolicLink(filePath));
            }
            Path watchPath = filePath;
            WatchKey key = watchPath.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            if (!listeners.containsKey(key)) {
                listeners.put(key, new ArrayList<DirectoryWatcherNotificationListenerContext>());
            }
            listeners.get(key).add(new DirectoryWatcherNotificationListenerContext(listener, notifyPath, filePath));
            if (log.isInfoEnabled()) {
                log.info("Registered directory configuration watcher listener for file " + dirToWatch);
            }
        }
        catch (IOException | InvalidPathException e) {
            log.error("Failed resolve or register file " + dirToWatch, e);
        }
    }

    private void unRegisterNotificationListener(DirectoryWatcherNotificationListener listener, String path) {
        try {
            for (List<DirectoryWatcherNotificationListenerContext> listenerContexts : listeners.values()) {
                List<DirectoryWatcherNotificationListenerContext> deletes = new ArrayList<>();
                for (DirectoryWatcherNotificationListenerContext context : listenerContexts) {
                    if (context.listener == listener) {
                        deletes.add(context);
                    }
                }
                for (DirectoryWatcherNotificationListenerContext delete : deletes) {
                    listenerContexts.remove(delete);
                    if (log.isInfoEnabled()) {
                        log.info("UN-Registered directory configuration watcher listener for file " + path);
                    }
                }
            }
        }
        catch (Exception e) {
            log.error("Failed resolve or register file " + path, e);
        }
    }

    @Override
    public void run() {
        if (log.isInfoEnabled()) {
            log.info("Starting Directory ConfigurationWatcher Thread...");
        }
        WatchKey key = null;
        while (true) {
            try {
                key = watchService.take();
                List<DirectoryWatcherNotificationListenerContext> contexts = listeners.get(key);
                if (contexts == null) {
                    log.error("Failed to find listener for key " + key);
                    key.reset();
                    continue;
                }
                for (WatchEvent event : key.pollEvents()) {
                    WatchEvent.Kind kind = event.kind();
                    Path path = (Path)event.context();
                    for (DirectoryWatcherNotificationListenerContext context: contexts) {
                        if (log.isDebugEnabled()) {
                            log.debug(String.format("Notifying change for file %s and kind %s", path, kind.name()));
                        }
                        context.listener.notifyChange(path, kind);
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

    private class DirectoryWatcherNotificationListenerContext {

        private DirectoryWatcherNotificationListener listener;
        private Path notifyPath;
        private Path filePath;

        private DirectoryWatcherNotificationListenerContext(DirectoryWatcherNotificationListener listener,
                                                     Path notifyPath, Path filePath) {
            this.listener = listener;
            this.notifyPath = notifyPath;
            this.filePath = filePath;
        }
    }
}

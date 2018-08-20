package org.groundwork.foundation.ws.impl;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

public interface DirectoryWatcherNotificationListener {
    public void notifyChange(Path path,  WatchEvent.Kind<Path> kind);
}

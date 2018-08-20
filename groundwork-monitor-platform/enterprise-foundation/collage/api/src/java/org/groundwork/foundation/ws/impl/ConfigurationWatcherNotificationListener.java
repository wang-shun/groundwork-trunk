package org.groundwork.foundation.ws.impl;

import java.nio.file.Path;

public interface ConfigurationWatcherNotificationListener {
    public void notifyChange(Path path);
}

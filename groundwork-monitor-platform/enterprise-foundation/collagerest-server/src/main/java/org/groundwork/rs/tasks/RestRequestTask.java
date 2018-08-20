package org.groundwork.rs.tasks;

import java.util.concurrent.Callable;

public interface RestRequestTask extends Callable<RestRequestResult> {
    String getName();
    String getTaskId();
    String getUriTemplate();
}

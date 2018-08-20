package org.groundwork.rs.client;

import org.groundwork.rs.dto.DtoAsyncSettings;
import org.groundwork.rs.dto.DtoOperationResults;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;

public class SettingsClientTest  extends AbstractClientTest {

    @Test
    public void testAsyncSettings() throws Exception {
        if (serverDown) return;
        SettingsClient client = new SettingsClient(getDeploymentURL());
        DtoAsyncSettings settings = client.getAsyncSettings();
        assertNotNull(settings);
        assert settings.getThreadPoolSize() == 15;
        assert settings.getQueueSize() == 1000;
        assert settings.getThrottleThreshold() == 500;
        assert settings.getThrottleWaitMs() == 500;

        DtoAsyncSettings newSettings = new DtoAsyncSettings(30, 2000, 1000, 1000);
        DtoOperationResults results = client.setAsyncSettings(newSettings);
        assert results.getSuccessful() == 1;

        DtoAsyncSettings settings2 = client.getAsyncSettings();
        assertNotNull(settings2);
        assert settings2.getThreadPoolSize() == 30;
        assert settings2.getQueueSize() == 2000;
        assert settings2.getThrottleThreshold() == 1000;
        assert settings2.getThrottleWaitMs() == 1000;

        results = client.setAsyncSettings(settings);
        assert results.getSuccessful() == 1;

        settings = client.getAsyncSettings();
        assertNotNull(settings);
        assert settings.getThreadPoolSize() == 15;
        assert settings.getQueueSize() == 1000;
        assert settings.getThrottleThreshold() == 500;
        assert settings.getThrottleWaitMs() == 500;
    }

}

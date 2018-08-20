package org.groundwork.rs.client;

import org.groundwork.rs.dto.DtoLicenseCheck;
import org.junit.Test;

import javax.ws.rs.core.MediaType;

/**
 * Created by dtaylor on 2/12/15.
 */
public class LicenseClientTest  extends AbstractClientTest {


    @Test
    public void testLicenseCheck() throws Exception {
        if (serverDown) return;
        LicenseClient client = new LicenseClient(getDeploymentURL());

        // test client using XML
        client.setMediaType(MediaType.APPLICATION_XML_TYPE);
        doTestLicenseCheck(client);

        // test client using JSON
        client.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        doTestLicenseCheck(client);
    }

    private void doTestLicenseCheck(LicenseClient client) throws Exception {
        DtoLicenseCheck check = client.check(5);
        assert check != null;
        assert check.getDevicesRequested() == 5;
        assert check.getDevices() > 1;
        assert check.getMessage() != null;
        assert check.isSuccess() == true;

        check = client.check(15000000);
        assert check != null;
        assert check.getDevicesRequested() == 15000000;
        assert check.getDevices() > 1;
        assert check.getMessage() != null;
        assert check.isSuccess() == false;

    }

}

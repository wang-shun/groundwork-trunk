package org.groundwork.rs.resources;

import org.groundwork.rs.utils.LicenseInfo;
import org.groundwork.rs.utils.PadlockReader;
import org.junit.Test;

/**
 * Created by dtaylor on 2/12/15.
 */
public class PadlockTest {

    private static final String LICENSE_KEY_PATH = "/usr/local/groundwork/config/groundwork.lic";

    @Test
    public void testPadlockLicense() throws Exception {
        PadlockReader licenseReader = new PadlockReader();
        LicenseInfo license = licenseReader.readLicense(LICENSE_KEY_PATH);
        assert license != null;
    }
}

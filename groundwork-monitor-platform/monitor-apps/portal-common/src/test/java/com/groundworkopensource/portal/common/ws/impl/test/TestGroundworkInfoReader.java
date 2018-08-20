package com.groundworkopensource.portal.common.ws.impl.test;

import com.groundworkopensource.portal.common.GroundworkInfoReader;
import org.junit.Test;

import java.util.Properties;

/**
 * Created by dtaylor on 3/16/15.
 */
public class TestGroundworkInfoReader {

    @Test
    public void testInfoReader() throws Exception {
        Properties info = GroundworkInfoReader.readInfoProperties();
        assert info.getProperty("name").equals("enterprise");
        assert info.getProperty("Monitor").startsWith("7.");
        assert info.getProperty("PostgreSQL").startsWith("9.");
        assert info.getProperty("version").startsWith("7.");
        assert info.getProperty("PatchLevel") != null;
        assert info.getProperty("Product") == null;
    }
}

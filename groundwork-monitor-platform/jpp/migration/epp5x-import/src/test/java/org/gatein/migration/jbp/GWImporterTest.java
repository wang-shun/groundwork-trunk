package org.gatein.migration.jbp;

import junit.framework.TestCase;

public class GWImporterTest extends TestCase {
    public void testImport() throws Exception {
        // Test
        try {
            String userDir   = System.getProperty("user.dir");
            String site      = userDir + "/src/test/resources/sites-p.xml";
            String siteName  = "groundwork-monitor";
            String outputDir = userDir + "/target/";

            GWImporter.importSite(site, siteName, outputDir);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // System.getProperties().list(System.out);
        System.out.println("End");
    }
}

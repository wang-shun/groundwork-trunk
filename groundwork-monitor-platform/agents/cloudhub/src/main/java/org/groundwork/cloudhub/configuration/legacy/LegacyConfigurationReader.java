package org.groundwork.cloudhub.configuration.legacy;

import com.wutka.jox.JOXBeanInputStream;
import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.agents.utils.SharedSecretProtector;
import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.rs.profile.VemaMonitoring;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * This class get the Configuration Information from the Config files for GWOS
 * and Vema These methods returns Java Beans from the XML Configuration.
 *
 * @author rvardhineedi
 */

public class LegacyConfigurationReader {
    private static Logger log = Logger.getLogger(LegacyConfigurationReader.class);

    /**
     * Generates VEMAGwosConfiguration Bean from the XML configuration
     *
     * @return VEMAGwosConfiguration object or null if no xml file is found
     */
    public static LegacyGwosConfiguration readLegacyConfiguration(VirtualSystem virtualSystem) {

        String vemaFileName = findLegacyConfigFile(virtualSystem);
        if (vemaFileName == null)
            return null;

        LegacyGwosConfiguration config = null;
        FileInputStream in = null;

        if (log.isDebugEnabled()) {
            log.debug("Legacy Config File (name='" + vemaFileName + "')" );
        }
        try {
            in = new FileInputStream(vemaFileName);
            JOXBeanInputStream joxIn = new JOXBeanInputStream(in);
            config = (LegacyGwosConfiguration) joxIn.readObject(LegacyGwosConfiguration.class);
            String encryptedPassword = config.getVirtualEnvPassword();
            config.setVirtualEnvPassword(SharedSecretProtector.decrypt(encryptedPassword));
        } catch (FileNotFoundException e) {
            log.error("File Not Found " + e.getMessage());
        } catch (IOException e) {
            log.error("IO Exception " + e.getMessage());
        } catch (GeneralSecurityException e) {
            log.error("General Security Exception " + e.getMessage());
        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        }
        finally {
            if (in != null)
                try { in.close(); } catch (Exception e) {}
        }
        return config;
    }

    /**
     * Converts vema XML to bean
     *
     * @return
     */
    public static VemaMonitoring vemaXMLToBean(String filename) {
        String vemaFileName = ConnectorConstants.CONFIG_FILE_PATH + filename
                + ConnectorConstants.CONFIG_FILE_EXTN;
        VemaMonitoring vemaBean = null;
        FileInputStream in = null;
        try {
            in = new FileInputStream(vemaFileName);
            JOXBeanInputStream joxIn = new JOXBeanInputStream(in);
            vemaBean = (VemaMonitoring) joxIn.readObject(VemaMonitoring.class);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage() + "\n" + e.getStackTrace()[0].toString()
                    + "\n" + e.getStackTrace()[1].toString() + "\n"
                    + e.getStackTrace()[2].toString() + "\n"
                    + e.getStackTrace()[3].toString() + "\n"
                    + e.getStackTrace()[4].toString() + "\n"
                    + e.getStackTrace()[5].toString() + "\n"
                    + e.getStackTrace()[6].toString() + "\n"
                    + e.getStackTrace()[7].toString());
        } catch (IOException e) {
            log.error(e.getMessage() + "\n" + e.getStackTrace()[0].toString()
                    + "\n" + e.getStackTrace()[1].toString() + "\n"
                    + e.getStackTrace()[2].toString() + "\n"
                    + e.getStackTrace()[3].toString() + "\n"
                    + e.getStackTrace()[4].toString() + "\n"
                    + e.getStackTrace()[5].toString() + "\n"
                    + e.getStackTrace()[6].toString() + "\n"
                    + e.getStackTrace()[7].toString());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error(e.getMessage() + "\n"
                            + e.getStackTrace()[0].toString() + "\n"
                            + e.getStackTrace()[1].toString() + "\n"
                            + e.getStackTrace()[2].toString() + "\n"
                            + e.getStackTrace()[3].toString() + "\n"
                            + e.getStackTrace()[4].toString() + "\n"
                            + e.getStackTrace()[5].toString() + "\n"
                            + e.getStackTrace()[6].toString() + "\n"
                            + e.getStackTrace()[7].toString());
                } // end try/catch
            } // end if
        }
        return vemaBean;
    }

    private static String findLegacyConfigFile(VirtualSystem virtualSystem) {
        try {
            switch (virtualSystem) {
                case VMWARE: {
                    String vemaFileName = ConnectorConstants.CONFIG_FILE_PATH
                            + ConnectorConstants.VEMA_CONFIG_FILE
                            + ConnectorConstants.CONFIG_FILE_EXTN;
                    File file = new File(vemaFileName);
                    if (file.exists())
                        return vemaFileName;
                    // try older filename
                    vemaFileName = ConnectorConstants.CONFIG_FILE_PATH
                            + ConnectorConstants.LEGACY_VMWARE_CONFIG_FILE
                            + ConnectorConstants.CONFIG_FILE_EXTN;
                    if (file.exists())
                        return vemaFileName;
                    break;
                }
                case REDHAT: {
                    String vemaFileName = ConnectorConstants.CONFIG_FILE_PATH
                            + ConnectorConstants.LEGACY_RHEV_CONFIG_FILE
                            + ConnectorConstants.CONFIG_FILE_EXTN;
                    File file = new File(vemaFileName);
                    if (file.exists())
                        return vemaFileName;
                    break;
                }
                case OPENSTACK:
                    // not supported
                    break;
                default:
                    break;
            }
        }
        catch (Exception e) {
            log.error("Failed to access VEMA Legacy configuration for " + virtualSystem.toString(), e);
        }
        return null;
    }

    public static void deleteLegacyConfiguration(VirtualSystem virtualSystem) {
        String vemaFileName = findLegacyConfigFile(virtualSystem);
        if (vemaFileName != null) {
            File file = new File(vemaFileName);
            file.delete();
        }
    }
}

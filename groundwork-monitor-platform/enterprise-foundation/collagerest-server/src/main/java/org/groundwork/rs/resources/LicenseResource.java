package org.groundwork.rs.resources;

import com.groundwork.collage.CollageFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.device.DeviceService;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.rs.dto.DtoLicenseCheck;
import org.groundwork.rs.utils.LicenseInfo;
import org.groundwork.rs.utils.PadlockReader;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

@Path("/license")
public class LicenseResource {

    public static final String RESOURCE_PREFIX = "/license/";
    protected static Log log = LogFactory.getLog(LicenseResource.class);

    /** The Constant LICENSE_KEY_PATH. */
    private static final String LICENSE_KEY_PATH = "/usr/local/groundwork/config/groundwork.lic";

    @GET
    @Path("/check")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoLicenseCheck checkAllocationOfDevices(@QueryParam("allocate") @DefaultValue("0") Integer devicesToAllocate) {

        if (devicesToAllocate == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Host name is mandatory").build());
        }

        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /license/check with allocate: %d", devicesToAllocate));
            }
            DeviceService deviceService =  CollageFactory.getInstance().getDeviceService();
            FoundationQueryList devices = deviceService.getDevices(null, null, -1, 1);
            int currentDeviceCount = devices.getTotalCount();
            PadlockReader padlockReader = new PadlockReader();
            LicenseInfo license = padlockReader.readLicense(LICENSE_KEY_PATH);
            // Enforce soft limit license settings; note: that no validation rule
            // or soft limit expiration date implies no soft limit tests, (See
            // com.groundworkopensource.portal.licensing.LicenseValidator.isSoftLimitExceeded()).
            String validationRules = license.getValidationRules();
            int softLimit = -1;
            boolean softLimitExpired = false;
            String softLimitExpiration = license.getSoftLimitExpirationDate();
            if ((validationRules != null) && (softLimitExpiration != null)) {
                // check validation rules for soft limit devices tests
                if (validationRules.contains(PadlockReader.SOFT_LIMIT_DEVICES_PARAM_NAME)) {
                    softLimit = license.getSoftLimit();
                }
                // check validation rules for soft limit expiration tests
                if (validationRules.contains(PadlockReader.SOFT_LIMIT_EXPIRATION_DATE_PARAM_NAME)) {
                    Date now = Calendar.getInstance().getTime();
                    Date softLimitExpirationDate = DateFormat.getDateTimeInstance().parse(softLimitExpiration);
                    softLimitExpired = now.after(softLimitExpirationDate);
                }
            }
            // return check result
            DtoLicenseCheck check = new DtoLicenseCheck();
            check.setDevicesRequested(devicesToAllocate);
            check.setDevices(currentDeviceCount);
            if (softLimitExpired) {
                check.setSuccess(false);
                check.setMessage("Device Allocation limit expired");
                return check;
            } else if ((softLimit >= 0) && ((currentDeviceCount + devicesToAllocate) > softLimit)) {
                check.setSuccess(false);
                check.setMessage("Device Allocation over limit");
                return check;
            }
            check.setSuccess(true);
            check.setMessage("OK to allocate " + devicesToAllocate + " devices");
            return check;
        }
        catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(String.format("An error occurred processing request for license check")).build());
        }
        finally {
        }
    }

}

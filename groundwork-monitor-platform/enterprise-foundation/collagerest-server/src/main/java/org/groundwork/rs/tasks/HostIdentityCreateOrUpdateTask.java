/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2014  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.groundwork.rs.tasks;

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostIdentity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.host.HostService;
import org.groundwork.foundation.bs.hostidentity.HostIdentityService;
import org.groundwork.rs.async.RestTransaction;
import org.groundwork.rs.dto.DtoHostIdentity;
import org.groundwork.rs.dto.DtoHostIdentityList;
import org.groundwork.rs.dto.DtoOperationResults;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * HostIdentityCreateTask
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class HostIdentityCreateOrUpdateTask extends AbstractRestTask implements RestRequestTask {

    private static Log log = LogFactory.getLog(HostIdentityCreateOrUpdateTask.class);

    /** HostIdentities to create */
    private final DtoHostIdentityList dtoHostIdentities;

    /**
     * Create HostIdentities task constructor.
     *
     * @param name task name
     * @param dtoHostIdentities HostIdentities to create
     * @param uriTemplate HostIdentity URI template
     */
    public HostIdentityCreateOrUpdateTask(String name, DtoHostIdentityList dtoHostIdentities, String uriTemplate) {
        super(name, uriTemplate);
        this.dtoHostIdentities = dtoHostIdentities;
    }

    @Override
    public RestRequestResult call() throws Exception {
        RestTransaction session = new RestTransaction();
        session.startTransaction();
        DtoOperationResults results = createOrUpdateHostIdentities();
        session.releaseSession();
        return new RestRequestResult(results, this, true, 0, false);
    }

    /**
     * Create task HostIdentities.
     *
     * @return operation results
     */
    public DtoOperationResults createOrUpdateHostIdentities() {
        if (dtoHostIdentities.size() == 0) {
            return new DtoOperationResults(HostIdentity.ENTITY_TYPE_CODE, DtoOperationResults.INSERT);
        }
        // optimize create all in one transaction
        HostIdentityService hostIdentityService = CollageFactory.getInstance().getHostIdentityService();
        HostService hostService = CollageFactory.getInstance().getHostService();
        boolean createAll = true;
        Map<DtoHostIdentity,HostIdentity> hostIdentities = new HashMap<DtoHostIdentity,HostIdentity>();
        for (DtoHostIdentity dtoHostIdentity : dtoHostIdentities.getHostIdentities()) {
            HostIdentity hostIdentity = null;
            if (dtoHostIdentity.getHostIdentityId() != null) {
                hostIdentity = hostIdentityService.getHostIdentityById(dtoHostIdentity.getHostIdentityId());
            } else {
                hostIdentity = hostIdentityService.getHostIdentityByHostName(dtoHostIdentity.getHostName());
            }
            hostIdentities.put(dtoHostIdentity, hostIdentity);
            createAll = (createAll && (hostIdentity == null));
        }
        // attempt to convert and create all host identities in one transaction
        if (createAll) {
            DtoOperationResults results = new DtoOperationResults(HostIdentity.ENTITY_TYPE_CODE, DtoOperationResults.INSERT);
            try {
                // convert host identities
                List<HostIdentity> createHostIdentities = new ArrayList<HostIdentity>(dtoHostIdentities.size());
                for (DtoHostIdentity dtoHostIdentity : dtoHostIdentities.getHostIdentities()) {
                    createHostIdentities.add(convertToHostIdentity(hostIdentityService, hostService, dtoHostIdentity));
                }
                // save host identities
                hostIdentityService.saveHostIdentities(createHostIdentities);
                // add successes to results
                for (HostIdentity hostIdentity : createHostIdentities) {
                    results.success(hostIdentity.getHostName(), buildResourceLocator(hostIdentity.getHostName()));
                }
                return results;
            } catch (Exception e) {
                // if there is only one host identity that has failed to convert or
                // save, emit that result
                if (dtoHostIdentities.size() == 1) {
                    // add failure to results
                    String message = "Failed to create HostIdentity: " + e.getMessage();
                    results.fail(dtoHostIdentities.getHostIdentities().get(0).getHostName(), message);
                    log.error(message, e);
                    return results;
                }
            }
        }
        // process host identities one at a time in order to ensure that
        // the results are returned in order and that individual host
        // identities may be saved even if others fail
        DtoOperationResults results = new DtoOperationResults(HostIdentity.ENTITY_TYPE_CODE, (createAll ? DtoOperationResults.INSERT : DtoOperationResults.UPDATE));
        for (DtoHostIdentity dtoHostIdentity : dtoHostIdentities.getHostIdentities()) {
            HostIdentity hostIdentity = hostIdentities.get(dtoHostIdentity);
            if (hostIdentity == null) {
                // create host identity
                try {
                    // convert host identity
                    hostIdentity = convertToHostIdentity(hostIdentityService, hostService, dtoHostIdentity);
                    // save host identity
                    hostIdentityService.saveHostIdentity(hostIdentity);
                    // add success to results
                    results.success(hostIdentity.getHostName(), buildResourceLocator(hostIdentity.getHostName()));
                } catch (Exception e) {
                    // add failure to results
                    String message = "Failed to create HostIdentity: " + e.getMessage();
                    results.fail(dtoHostIdentity.getHostName(), message);
                    log.error(message, e);
                }
            } else {
                // update host identity
                String idOrHostName = dtoHostIdentity.getHostName();
                if (dtoHostIdentity.getHostIdentityId() != null) {
                    idOrHostName = dtoHostIdentity.getHostIdentityId().toString();
                }
                try {
                    boolean updated = true;
                    // rename host identity and host host name
                    if (!dtoHostIdentity.getHostName().equalsIgnoreCase(hostIdentity.getHostName())) {
                        if (hostIdentity.getHost() != null) {
                            // use admin to ensure update events are sent when host is renamed, (note
                            // that HostIdentityService.renameHostIdentity() is invoked via the
                            // CollageAdminInfrastructure.renameHost() admin api)
                            CollageAdminInfrastructure admin = getAdminInfrastructureService();
                            updated = (admin.renameHost(hostIdentity.getHostName(), dtoHostIdentity.getHostName(), null, null) != null);
                        } else {
                            // no host identity host, HostIdentityService.renameHostIdentity() is invoked
                            updated = hostIdentityService.renameHostIdentity(hostIdentity.getHostName(), dtoHostIdentity.getHostName());
                        }
                    }
                    // add host names to host identity
                    if (updated && dtoHostIdentity.getHostNames() != null) {
                        for (String hostName : dtoHostIdentity.getHostNames()) {
                            if (!hostIdentity.getHostName().equalsIgnoreCase(hostName)) {
                                updated = updated && hostIdentityService.addHostNameToHostIdentity(hostIdentity.getHostName(), hostName);
                            }
                        }
                    }
                    // add updated success/failure to results
                    if (updated) {
                        results.success(idOrHostName, buildResourceLocator(hostIdentity.getHostName()));
                    } else {
                        results.fail(idOrHostName, "HostIdentity not updated");
                    }
                } catch (Exception e) {
                    // add failure to results
                    String message = "Failed to update HostIdentity: " + e.getMessage();
                    results.fail(idOrHostName, message);
                    log.error(message, e);
                }
            }
        }
        return results;
    }

    private static HostIdentity convertToHostIdentity(HostIdentityService hostIdentityService, HostService hostService, DtoHostIdentity dtoHostIdentity) {
        UUID hostIdentityId = dtoHostIdentity.getHostIdentityId();
        String hostName = dtoHostIdentity.getHostName();
        Collection<String> hostNames = dtoHostIdentity.getHostNames();
        // lookup host by host name
        Host host = hostService.getHostByHostName(hostName);
        // return HostIdentity using Host if available
        HostIdentity hostIdentity;
        if (host != null) {
            hostIdentity = hostIdentityService.createHostIdentity(hostIdentityId, host, hostNames);
        } else {
            hostIdentity = hostIdentityService.createHostIdentity(hostIdentityId, hostName, hostNames);
        }
        return hostIdentity;
    }
}

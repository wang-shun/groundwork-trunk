package com.groundworkopensource.portal.extension.rest;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.io.StringWriter;
import javax.ws.rs.QueryParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.WebApplicationException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.io.StringWriter;
import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;

import java.util.List;

import com.groundworkopensource.portal.identity.extendedui.HibernateExtendedRolePermission;
import com.groundworkopensource.portal.identity.extendedui.HibernateResource;
import com.groundworkopensource.portal.model.*;
import org.apache.log4j.Logger;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.ExoContainerContext;
import com.groundworkopensource.portal.identity.extendedui.HibernateExtendedRole;
import com.groundworkopensource.portal.identity.extendedui.ExtendedRoleModuleImpl;

import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.organization.Membership;

import org.exoplatform.services.rest.resource.ResourceContainer;

@Path("/extendedrole/")
@Produces("application/xml")
public class ExtendedRoleService implements ResourceContainer {

    /**
     * Logger
     */
    private static final Logger LOGGER = Logger
            .getLogger(ExtendedRoleService.class);

    /**
     * Returns list of associated roles for the given user. Format of return Response
     *
     * @param userName
     */
    @Path("/findrolesbyuser")
    @GET
    @Produces("application/xml")
    public Response findRolesByUser(@QueryParam("userName") String userName) {
        try {
            if (userName != null) {
                OrganizationService orgService = (OrganizationService) ExoContainerContext
                        .getCurrentContainer().getComponentInstanceOfType(
                                OrganizationService.class);
                Collection<Membership> membershipCol = orgService
                        .getMembershipHandler().findMembershipsByUser(userName);
                List<ExtendedUIRole> extendedUIRoles = this
                        .populateExtendedUIRoles(membershipCol);
                if (extendedUIRoles != null) {
                    // Now wrap the list for marshalling
                    ExtendedRoleList wrapperList = new ExtendedRoleList(
                            extendedUIRoles);
                    StringWriter xmlWriter = new StringWriter();
                    JAXBContext context = JAXBContext
                            .newInstance(ExtendedRoleList.class);
                    Marshaller m = context.createMarshaller();
                    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                            Boolean.TRUE);
                    m.marshal(wrapperList, xmlWriter);
                    String roleXml = xmlWriter.toString();
                    LOGGER.debug("Extended role info : " + roleXml);
                    return Response.ok(roleXml).build();
                } else {
                    throw new WebApplicationException(Response.Status.NOT_FOUND);
                }
            } else {
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
        } catch (Exception he) {
            LOGGER.error("Error while retriving roles for user : " + userName);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    /**
     * Populates the extendedUIRoles. Role is equaivalent to membership in
     * gatein
     *
     * @return
     */
    private List<ExtendedUIRole> populateExtendedUIRoles(
            Collection<Membership> membershipCol) {
        List<ExtendedUIRole> list = new ArrayList<ExtendedUIRole>();
        try {
            for (Membership membership : membershipCol) {
                String memName = membership.getMembershipType();
                if (memName != null) {
                    ExtendedRoleModuleImpl extRoleModule = new ExtendedRoleModuleImpl();
                    HibernateExtendedRole hibRole = extRoleModule
                            .findRoleByName(memName);
                    if (hibRole != null) {
                        ExtendedUIRole uiRole = new ExtendedUIRole();
                        uiRole.setId(hibRole.getId());
                        uiRole.setRoleName(memName);
                        uiRole.setDashboardLinksDisabled(hibRole
                                .isDashboardLinksDisabled());
                        uiRole.setHgList(hibRole.getHgList());
                        uiRole.setSgList(hibRole.getSgList());
                        uiRole.setDefaultHostGroup(hibRole
                                .getDefaultHostGroup());
                        uiRole.setDefaultServiceGroup(hibRole
                                .getDefaultServiceGroup());
                        uiRole.setRestrictionType(hibRole.getRestrictionType());
                        uiRole.setActionsEnabled(hibRole.isActionsEnabled());
                        Collection<ExtendedUIRolePermission> rolePermissions = new ArrayList<ExtendedUIRolePermission>();
                        Collection<HibernateExtendedRolePermission> permissions = hibRole.getRolePermissions();
                        for (HibernateExtendedRolePermission permission : permissions) {
                            String resource = permission.getResource().getName();
                            String action = permission.getPermission().getAction();
                            ExtendedUIRolePermission rolePermission = new ExtendedUIRolePermission();
                            rolePermission.setResource(resource);
                            rolePermission.setAction(action);
                            rolePermissions.add(rolePermission);

                        }
                        ExtendedUIRolePermissionList permList = new ExtendedUIRolePermissionList();
                        permList.setRolePermissions(rolePermissions);
                        uiRole.setRolePermissions(permList);
                        list.add(uiRole);

                    } // end if
                } // end if
            } // end for
        } catch (Exception ie) {
            LOGGER.error(ie.getMessage());
        } // end try/catch
        return list;
    }

    /**
     * Returns extended UI role for the give rolename. Format of return Response
     *
     * @param roleName
     */
    @Path("/findrolebyname")
    @GET
    @Produces("application/xml")
    public Response findRoleByName(@QueryParam("roleName") String roleName) {
        try {
            ExtendedRoleModuleImpl impl = new ExtendedRoleModuleImpl();
            HibernateExtendedRole hibRole = impl.findRoleByName(roleName);
            ExtendedUIRole role = new ExtendedUIRole();
            role.setId(hibRole.getId());
            role.setRoleName(hibRole.getName());
            role.setDashboardLinksDisabled(hibRole.isDashboardLinksDisabled());
            role.setHgList(hibRole.getHgList());
            role.setSgList(hibRole.getSgList());
            role.setRestrictionType(hibRole.getRestrictionType());
            role.setDefaultHostGroup(hibRole.getDefaultHostGroup());
            role.setDefaultServiceGroup(hibRole.getDefaultServiceGroup());
            role.setActionsEnabled(hibRole.isActionsEnabled());
            Collection<ExtendedUIRolePermission> rolePermissions = new ArrayList<ExtendedUIRolePermission>();
            Collection<HibernateExtendedRolePermission> permissions = hibRole.getRolePermissions();
            for (HibernateExtendedRolePermission permission : permissions) {
                String resource = permission.getResource().getName();
                String action = permission.getPermission().getAction();
                ExtendedUIRolePermission rolePermission = new ExtendedUIRolePermission();
                rolePermission.setResource(resource);
                rolePermission.setAction(action);
                rolePermissions.add(rolePermission);

            }
            ExtendedUIRolePermissionList permList = new ExtendedUIRolePermissionList();
            permList.setRolePermissions(rolePermissions);
            role.setRolePermissions(permList);
            StringWriter responseWriter = new StringWriter();
            JAXBContext context = JAXBContext.newInstance(ExtendedUIRole.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(role, responseWriter);
            String response = responseWriter.toString();
            return Response.ok(response).build();
        } catch (Exception he) {
            LOGGER.error("Error while retriving records for roleName : "
                    + roleName);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    /**
     * Returns all resources
     */
    @Path("/getResources")
    @GET
    @Produces("application/xml")
    public Response getResources() {
        try {
            ExtendedRoleModuleImpl impl = new ExtendedRoleModuleImpl();
            List<HibernateResource> resources = impl.getResources();
            ArrayList<ExtendedUIResource> arrayList = new ArrayList<>();
            for (HibernateResource resource : resources) {
                ExtendedUIResource uiResource = new ExtendedUIResource();
                uiResource.setResourceId(resource.getResourceId());
                uiResource.setResourceName(resource.getName());
                arrayList.add(uiResource);
            }
            ExtendedUIResourceList uiResourceList = new ExtendedUIResourceList();
            uiResourceList.setList(arrayList);
            StringWriter responseWriter = new StringWriter();
            JAXBContext context = JAXBContext.newInstance(ExtendedUIResourceList.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(uiResourceList, responseWriter);
            String response = responseWriter.toString();
            return Response.ok(response).build();
        } catch (Exception he) {
            LOGGER.error("Error while retrieving extended ui resources)");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    /**
     * Creates new extended role
     */
    @Path("/create")
    @POST
    @Produces("application/xml")
    public Response createRole(@Wrapped(element="ExtendedRole") ExtendedUIRole uiRole) {
        try {
            ExtendedRoleModuleImpl impl = new ExtendedRoleModuleImpl();
            HibernateExtendedRole hibRole = impl.createRole(uiRole.getRoleName(),
                    uiRole.isDashboardLinksDisabled(), uiRole.getHgList(),
                    uiRole.getSgList(), uiRole.getRestrictionType(), uiRole.getDefaultHostGroup(), uiRole.getDefaultServiceGroup(),
                    uiRole.isActionsEnabled(), uiRole.getRolePermissions());
            if (hibRole.getId() > 0) {
                return Response.status(Response.Status.OK).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .build();
            }

        } catch (Exception he) {
            LOGGER.error("Error while creating records for roleName : " + uiRole.getRoleName());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }


    /**
     * Updates extended role
     */
    @Path("/update")
    @POST
    @Produces("application/xml")
    public Response updateRole(@Wrapped(element="ExtendedRole") ExtendedUIRole uiRole) {
        try {
            ExtendedRoleModuleImpl impl = new ExtendedRoleModuleImpl();
            HibernateExtendedRole hibRole = impl.updateRole(uiRole.getId(),uiRole.getRoleName(),
                    uiRole.isDashboardLinksDisabled(), uiRole.getHgList(),
                    uiRole.getSgList(), uiRole.getRestrictionType(), uiRole.getDefaultHostGroup(), uiRole.getDefaultServiceGroup(),
                    uiRole.isActionsEnabled(), uiRole.getRolePermissions());
            if (hibRole.getId() > 0) {
                return Response.status(Response.Status.OK).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .build();
            } // end if

        } catch (Exception he) {
            LOGGER.error("Error while retriving records for roleName : " + uiRole.getRoleName());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    /**
     * Deletes extended role
     */
    @Path("/delete")
    @POST
    @Produces("application/xml")
    public Response deleteRole(@FormParam("roleName") String roleName) {
        try {
            ExtendedRoleModuleImpl impl = new ExtendedRoleModuleImpl();
            HibernateExtendedRole role = impl.findRoleByName(roleName);
            if (role != null) {
                Long roleId = role.getId();
                impl.removeRole(roleId);
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .build();
            }
            return Response.status(Response.Status.OK).build();
        } catch (Exception he) {
            LOGGER.error("Error while deleting records for roleName : "
                    + roleName);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    /**
     * Updates extended role
     */
    @Path("/updateactions")
    @POST
    @Produces("application/xml")
    public Response updateActionsEnabled(@FormParam("name") String name,
                                         @QueryParam("isActionsEnabled") boolean isActionsEnabled) {
        try {
            ExtendedRoleModuleImpl impl = new ExtendedRoleModuleImpl();
            HibernateExtendedRole hibRole = impl.updateActionsEnabled(name,
                    isActionsEnabled);
            if (hibRole.getId() > 0) {
                return Response.status(Response.Status.OK).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .build();
            } // end if
        } catch (Exception he) {
            LOGGER.error("Error while retriving records for roleName : " + name);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}
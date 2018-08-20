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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.database.HibernateService;
import com.groundworkopensource.portal.identity.extendedui.HibernateCustomGroup;
import com.groundworkopensource.portal.identity.extendedui.HibernateCustomGroupCollection;
import com.groundworkopensource.portal.identity.extendedui.HibernateCustomGroupElement;
import com.groundworkopensource.portal.model.CustomGroup;
import com.groundworkopensource.portal.model.CustomGroupElement;
import com.groundworkopensource.portal.identity.extendedui.HibernateEntityType;
import com.groundworkopensource.portal.model.EntityType;
import com.groundworkopensource.portal.model.EntityTypeList;
import com.groundworkopensource.portal.model.CustomGroupList;
import com.groundworkopensource.portal.identity.extendedui.CustomGroupModule;
import com.groundworkopensource.portal.identity.extendedui.CustomGroupModuleImpl;

import org.exoplatform.services.rest.resource.ResourceContainer;

@Path("/customgroup/")
@Produces("application/xml")
public class CustomGroupService implements ResourceContainer {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger
			.getLogger(CustomGroupService.class);

	private static final String DATE_FORMAT = "MM/dd/yyyy hh:mm:ss a";

	private static CustomGroupModule dao = new CustomGroupModuleImpl();

	/**
	 * Returns all custom groups
	 * 
	 * @param roleName
	 */
	@Path("/findcustomgroups")
	@GET
	@Produces("application/xml")
	public Response findCustomGroups() {
		try {
			List<CustomGroup> customGroupsLocal = new ArrayList<CustomGroup>();
			List<HibernateCustomGroup> hibCustomGroups = dao.findCustomGroups();
			for (HibernateCustomGroup hibCustomGroup : hibCustomGroups) {
				customGroupsLocal.add(this
						.convertHibObject2UIObject(hibCustomGroup));
			} // end if
			CustomGroupList groupList = new CustomGroupList(customGroupsLocal);
			return buildResponse(CustomGroupList.class, groupList);
		} catch (Exception he) {
			LOGGER.error("Error while retriving records for customgroups : ");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.build();
		}
	}

	/**
	 * Helper to convert hibernate object to UI object
	 */
	private CustomGroup convertHibObject2UIObject(
			HibernateCustomGroup hibCustomGroup) {
		EntityType uiType = new EntityType();
		uiType.setEntityTypeId(hibCustomGroup.getEntityType().getEntityTypeId());
		uiType.setEntityType(hibCustomGroup.getEntityType().getEntityType());

		// Populate customgroup here
		CustomGroup uiCustomGroup = new CustomGroup();
		uiCustomGroup.setGroupId(hibCustomGroup.getGroupId());
		uiCustomGroup.setEntityType(uiType);
		uiCustomGroup.setGroupName(hibCustomGroup.getGroupName());

		// Now populate children
		Collection<HibernateCustomGroupElement> hibCustomGroupElements = hibCustomGroup
				.getElements();
		List<CustomGroupElement> uiElements = new ArrayList<CustomGroupElement>();
		for (HibernateCustomGroupElement hibCustomGroupElement : hibCustomGroupElements) {
			CustomGroupElement uiElement = new CustomGroupElement();
			uiElement.setElementId(hibCustomGroupElement.getElementId());
			uiElement.setEntityTypeId(hibCustomGroupElement.getEntityType().getEntityTypeId());
			uiElements.add(uiElement);
		}

		uiCustomGroup.setElements(uiElements);

		// Now populate parents
		Collection<HibernateCustomGroup> hibParents = hibCustomGroup
				.getParents();
		List<CustomGroup> uiParents = new ArrayList<CustomGroup>();
		for (HibernateCustomGroup hibParent : hibParents) {
			CustomGroup uiParent = new CustomGroup();
			List<CustomGroup> uiParents_level_2 = new ArrayList<CustomGroup>();
			for (HibernateCustomGroup hibParent_level_2 : hibParent
					.getParents()) {
				CustomGroup uiParent_level_2 = new CustomGroup();
				uiParent_level_2.setGroupName(hibParent_level_2.getGroupName());
				uiParents_level_2.add(uiParent_level_2);
			}
			uiParent.setParents(uiParents_level_2);
			uiParent.setGroupName(hibParent.getGroupName());
			uiParent.setGroupId(hibParent.getGroupId());
			uiParents.add(uiParent);
		}

		uiCustomGroup.setParents(uiParents);
		uiCustomGroup.setCreatedBy(hibCustomGroup.getCreatedBy());
		uiCustomGroup.setGroupState(hibCustomGroup.getGroupState());

		String createdTimeStamp = new SimpleDateFormat(DATE_FORMAT)
				.format(hibCustomGroup.getCreatedTimeStamp());

		uiCustomGroup.setCreatedTimeStamp(createdTimeStamp);

		if (hibCustomGroup.getLastModifiedTimeStamp() != null) {
			String lastModifiedTimeStamp = new SimpleDateFormat(DATE_FORMAT)
					.format(hibCustomGroup.getLastModifiedTimeStamp());
			uiCustomGroup.setLastModifiedTimeStamp(lastModifiedTimeStamp);
		}

		return uiCustomGroup;
	}

	/**
	 * Returns all custom group based on the supplied customgroup id
	 * 
	 * @param groupId
	 */
	@Path("/findCustomGroupById")
	@GET
	@Produces("application/xml")
	public Response findCustomGroupById(@QueryParam("groupId") Long groupId) {
		try {
			HibernateCustomGroup hibCustomGroup = dao
					.findCustomGroupById(groupId);
			CustomGroup customGroup = this
					.convertHibObject2UIObject(hibCustomGroup);
			return buildResponse(CustomGroup.class, customGroup);
		} catch (Exception he) {
			LOGGER.error("Error while retriving records for customgroup : "
					+ he.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.build();
		} // end try/catch
	}

	/**
	 * Returns custom group based on the supplied customgroup id
	 * 
	 * @param customgroup
	 *            name
	 */
	@Path("/findCustomGroupByName")
	@GET
	@Produces("application/xml")
	public Response findCustomGroupByName(@QueryParam("name") String name) {
		try {
			List<CustomGroup> customGroupsLocal = new ArrayList<CustomGroup>();
			List<HibernateCustomGroup> hibCustomGroups = dao
					.findCustomGroupByName(name);
			for (HibernateCustomGroup hibCustomGroup : hibCustomGroups) {
				customGroupsLocal.add(this
						.convertHibObject2UIObject(hibCustomGroup));
			} // end if
			CustomGroupList groupList = new CustomGroupList(customGroupsLocal);
			return buildResponse(CustomGroupList.class, groupList);
		} catch (Exception he) {
			LOGGER.error("Error while retriving records for customgroup : "
					+ he.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.build();
		} // end try/catch
	}

	/**
	 * Returns all predefined EntityTypes.
	 * 
	 * @param none
	 */
	@Path("/findEntityTypes")
	@GET
	@Produces("application/xml")
	public Response findEntityTypes() {
		List<EntityType> list = new ArrayList<EntityType>();
		try {
			List<HibernateEntityType> entityTypeList = dao.findEntityTypes();
			for (HibernateEntityType hibType : entityTypeList) {
				EntityType entType = new EntityType();
				entType.setEntityTypeId(hibType.getEntityTypeId());
				entType.setEntityType(hibType.getEntityType());
				list.add(entType);
			} // end for
			EntityTypeList entList = new EntityTypeList(list);
			return buildResponse(EntityTypeList.class, entList);
		} catch (Exception he) {
			LOGGER.error("Error while retriving records for entitytypes : "
					+ he.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.build();
		} // end try/catch
	}

	/**
	 * Helper for Response Builder
	 */
	private Response buildResponse(Class clazz, Object marshalObj)
			throws JAXBException, PropertyException {
		StringWriter responseWriter = new StringWriter();
		JAXBContext context = JAXBContext.newInstance(clazz);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.marshal(marshalObj, responseWriter);
		String response = responseWriter.toString();
		return Response.ok(response).build();
	}

	/**
	 * Creates the custom group based on the supplied params.
	 * 
	 * @param groupName
	 * @param entityTypeId
	 * @param parents
	 *            - not used at this time. Pass null value.
	 * @param groupState
	 * @param createdBy
	 * @param children
	 *            - comma seperated ids of children
	 */
	@Path("/createCustomGroup")
	@POST
	@Produces("application/xml")
	public Response createCustomGroup(@FormParam("groupName") String groupName,
			@FormParam("entityTypeId") int entityTypeId,
			@FormParam("parents") String parents,
			@FormParam("groupState") String groupState,
			@FormParam("createdBy") String createdBy,
			@FormParam("children") String children) {
		try {
			Collection<Long> parentList = new ArrayList(); // not supported at
															// this time
			Collection<Long> childrenList = new ArrayList();
			if (children != null) {
				StringTokenizer stkn = new StringTokenizer(children, ",");
				while (stkn.hasMoreTokens()) {
					childrenList.add(Long.parseLong(stkn.nextToken()));
				}
			}
			HibernateCustomGroup customgroup = dao.createCustomGroup(groupName,
					(byte) entityTypeId, parentList, groupState, createdBy,
					childrenList);
			return Response.status(Response.Status.OK).build();
		} catch (Exception he) {
			LOGGER.error("Error while creating customgroup : "
					+ he.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.build();
		} // end try/catch
	}

	/**
	 * Updates the custom group based on the supplied params.
	 * 
	 * @param groupName
	 * @param entityTypeId
	 * @param parents
	 *            - not used at this time. Pass null value.
	 * @param groupState
	 * @param createdBy
	 * @param children
	 *            - comma seperated ids of children.
	 */
	@Path("/updateCustomGroup")
	@POST
	@Produces("application/xml")
	public Response updateCustomGroup(@FormParam("groupName") String groupName,
			@FormParam("entityTypeId") int entityTypeId,
			@FormParam("parents") String parents,
			@FormParam("groupState") String groupState,
			@FormParam("createdBy") String createdBy,
			@FormParam("children") String children) {
		try {
			Collection<Long> parentList = new ArrayList(); // not supported at
															// this time
			Collection<Long> childrenList = new ArrayList();
			if (children != null) {
				StringTokenizer stkn = new StringTokenizer(children, ",");
				while (stkn.hasMoreTokens()) {
					childrenList.add(Long.parseLong(stkn.nextToken()));
				}
			}
			HibernateCustomGroup customgroup = dao.updateCustomGroup(groupName,
					(byte) entityTypeId, parentList, groupState, createdBy,
					childrenList);
			return Response.status(Response.Status.OK).build();
		} catch (Exception he) {
			LOGGER.error("Error while updating customgroups : "
					+ he.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.build();
		} // end try/catch

	}

	/**
	 * Deletes custom group based on the supplied groupId.
	 * 
	 * @param groupId
	 */
	@Path("/removeCustomGroup")
	@POST
	@Produces("application/xml")
	public Response removeCustomGroup(@FormParam("groupId") Long groupId) {
		try {
			dao.removeCustomGroup(groupId);
			return Response.status(Response.Status.OK).build();
		} catch (Exception he) {
			LOGGER.error("Error while deleting customgroups : "
					+ he.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.build();
		} // end try/catch
	}

	/**
	 * Deletes Orphan chldren. Can be useful incase if hostgroup or servicegroup
	 * is deleted from Monarch
	 * 
	 * @param elementId
	 * @param entityTypeId
	 */
	@Path("/removeOrphanedChildren")
	@POST
	@Produces("application/xml")
	public Response removeOrphanedChildren(
			@FormParam("elementId") Long elementId,
			@FormParam("entityTypeId") int entityTypeId) {
		try {
			dao.removeOrphanedChildren(elementId, (byte)entityTypeId);
			return Response.status(Response.Status.OK).build();
		} catch (Exception he) {
			LOGGER.error("Error while deleting orphaned children : "
					+ he.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.build();
		} // end try/catch
	}

}
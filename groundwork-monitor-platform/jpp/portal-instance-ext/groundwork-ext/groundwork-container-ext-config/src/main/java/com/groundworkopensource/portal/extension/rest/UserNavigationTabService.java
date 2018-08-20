package com.groundworkopensource.portal.extension.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.StringWriter;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.database.HibernateService;
import com.groundworkopensource.portal.model.UserNavigation;

import org.exoplatform.services.rest.resource.ResourceContainer;

@Path("/usertabpersistance/")
public class UserNavigationTabService implements ResourceContainer {

	/**
	 * one constant
	 */
	public static final int ONE = 1;

	/**
	 * One constant.
	 */
	public static final int ZERO = 0;

	/**
	 * constant
	 */
	public static final int TEN = 10;
	/**
	 * constant
	 */
	public static final int NINE = 9;
	/**
	 * constant
	 */
	public static final int EIGHT = 8;
	/**
	 * constant
	 */
	public static final int SEVEN = 7;
	/**
	 * constant
	 */
	public static final int SIX = 6;
	/**
	 * constant
	 */
	public static final int FIVE = 5;
	/**
	 * constant
	 */
	public static final int FOUR = 4;
	/**
	 * constant
	 */
	public static final int THREE = 3;
	/**
	 * constant
	 */
	public static final int TWO = 2;
	/**
	 * /** Logger
	 */
	private static final Logger LOGGER = Logger
			.getLogger(UserNavigationTabService.class);

	/**
	 * Returns all Navigation History records for provided user Id.
	 * 
	 * Format of return list: navigationlist
	 * 
	 * @param userId
	 * @param app_type
	 * @return List
	 * @throws IOException
	 */
	@Path("/gethistory")
	@GET
	@Produces("application/xml")
	public Response getHistoryRecords(@QueryParam("userId") String userId,
			@QueryParam("app_type") String app_type) {
		Session session = getSessionFactory().openSession();
		try {
			// check if application type is null or empty String then use
			// default "statusviewer" application type.
			if (null == app_type || app_type.equals("")) {
				app_type = "statusviewer";
			}
			Query query = session
					.createQuery("from UserNavigation n where n.userId = :userId and n.appType = :appType");
			query.setParameter("userId", userId);
			query.setParameter("appType", app_type);

			List<UserNavigation> result = query.list();
			com.groundworkopensource.portal.model.NavigationList naviList = new com.groundworkopensource.portal.model.NavigationList(
					result);
			StringWriter responseWriter = new StringWriter();
			JAXBContext context = JAXBContext
					.newInstance(com.groundworkopensource.portal.model.NavigationList.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(naviList, responseWriter);
			String response = responseWriter.toString();
			return Response.ok(response).build();
		} catch (Exception he) {
			LOGGER.error("Error while retriving records for user Id : "
					+ userId);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.build();
		} finally {
			session.flush();
			session.close();
		}
	}

	/**
	 * This method is used to add single record to Navigation History database.<br>
	 * Note: record Id is not provided. Its auto generated by database.
	 * 
	 * @param userId
	 * @param nodeId
	 * @param nodeName
	 * @param nodeType
	 * @param parentInfo
	 * @param toolTip
	 * @param app_type
	 * @throws IOException
	 */
	@Path("/addwithoutlabel")
	@GET
	@Produces("application/xml")
	public Response addHistoryRecord(@QueryParam("userId") String userId,
			@QueryParam("nodeId") int nodeId,
			@QueryParam("nodeName") String nodeName,
			@QueryParam("nodeType") String nodeType,
			@QueryParam("parentInfo") String parentInfo,
			@QueryParam("toolTip") String toolTip,
			@QueryParam("app_type") String app_type) {
		Session session = getSessionFactory().openSession();

		try {
			Transaction transaction = session.beginTransaction();
			Query query = session
					.createSQLQuery("insert into USER_NAVIGATION (id,USER_ID,NODE_ID, NODE_NAME, NODE_TYPE, PARENT_INFO, TOOLTIP,APP_TYPE) values (nextval('hibernate_sequence'),?,?,?,?,?,?,?)");
			query.setParameter(ZERO, userId);
			query.setParameter(ONE, nodeId);
			query.setParameter(TWO, nodeName);
			query.setParameter(THREE, nodeType);
			query.setParameter(FOUR, parentInfo);
			query.setParameter(FIVE, toolTip);
			// check if application type is null or empty String then use
			// default "statusviewer" application type.
			if (null == app_type || app_type.equals("")) {
				app_type = "statusviewer";
			}
			query.setParameter(6, app_type);

			// execute update
			query.executeUpdate();
			// commit
			transaction.commit();
			return Response.status(Response.Status.OK).build();
		} catch (HibernateException he) {
			LOGGER.error("Error while inserting record for user [" + userId
					+ "]");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.build();

		} finally {
			// session.getTransaction().commit();
			session.flush();
			session.close();
		}
	}

	/**
	 * This method is used to add single record to Navigation History database.<br>
	 * Note: record Id is not provided. Its auto generated by database.
	 * 
	 * @param userId
	 * @param nodeId
	 * @param nodeName
	 * @param nodeType
	 * @param parentInfo
	 * @param toolTip
	 * @param app_type
	 * @param nodeLabel
	 * @throws IOException
	 */
	@Path("/addwithlabel")
	@GET
	@Produces("application/xml")
	public Response addHistoryRecord(@QueryParam("userId") String userId,
			@QueryParam("nodeId") int nodeId,
			@QueryParam("nodeName") String nodeName,
			@QueryParam("nodeType") String nodeType,
			@QueryParam("parentInfo") String parentInfo,
			@QueryParam("toolTip") String toolTip,
			@QueryParam("app_type") String app_type,
			@QueryParam("nodeLabel") String nodeLabel) {
		Session session = getSessionFactory().openSession();

		try {
			Transaction transaction = session.beginTransaction();
			Query query = session
					.createSQLQuery("insert into USER_NAVIGATION (id,USER_ID,NODE_ID, NODE_NAME, NODE_TYPE, PARENT_INFO, TOOLTIP,APP_TYPE,NODE_LABEL) values (nextval('hibernate_sequence'),?,?,?,?,?,?,?,?)");
			query.setParameter(ZERO, userId);
			query.setParameter(ONE, nodeId);
			query.setParameter(TWO, nodeName);
			query.setParameter(THREE, nodeType);
			query.setParameter(FOUR, parentInfo);
			query.setParameter(FIVE, toolTip);
			// check if application type is null or empty String then use
			// default "statusviewer" application type.
			if (null == app_type || app_type.equals("")) {
				app_type = "statusviewer";
			}
			query.setParameter(SIX, app_type);
			query.setParameter(SEVEN, nodeLabel);
			// execute update
			query.executeUpdate();
			// commit
			transaction.commit();
			return Response.status(Response.Status.OK).build();
		} catch (HibernateException he) {
			LOGGER.error("Error while inserting record for user [" + userId
					+ "]");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.build();

		} finally {
			// session.getTransaction().commit();
			session.flush();
			session.close();
		}
	}

	/**
	 * This method is used to delete a single record of a user identified by
	 * "userId" from Navigation History database.
	 * 
	 * @param userId
	 * @param nodeId
	 * @param nodeType
	 * @param app_type
	 * @throws IOException
	 */
	@Path("/deletenodewithtype")
	@GET
	@Produces("application/xml")
	public Response deleteHistoryRecord(@QueryParam("userId") String userId,
			@QueryParam("nodeId") int nodeId,
			@QueryParam("nodeType") String nodeType,
			@QueryParam("app_type") String app_type) {
		Session session = getSessionFactory().openSession();

		try {
			Transaction transaction = session.beginTransaction();
			Query query = session
					.createSQLQuery("delete from USER_NAVIGATION where USER_ID=? and NODE_ID=? and NODE_TYPE=? and APP_TYPE=?");
			query.setParameter(ZERO, userId);
			query.setParameter(ONE, nodeId);
			query.setParameter(TWO, nodeType);
			// check if application type is null or empty String then use
			// default "statusviewer" application type.
			if (null == app_type || app_type.equals("")) {
				app_type = "statusviewer";
			}
			query.setParameter(THREE, app_type);
			query.executeUpdate();

			transaction.commit();
			return Response.status(Response.Status.OK).build();
		} catch (HibernateException he) {
			LOGGER.error("Error while deleting record for user" + userId);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.build();

		} finally {
			session.flush();
			session.close();
		}
	}

	/**
	 * This method is used to delete all records of a user identified by
	 * "userId" from Navigation History database.
	 * 
	 * @param userId
	 * @param app_type
	 * @throws IOException
	 */
	@Path("/deleteall")
	@GET
	@Produces("application/xml")
	public Response deleteAllHistoryRecords(
			@QueryParam("userId") String userId,
			@QueryParam("app_type") String app_type) {
		Session session = getSessionFactory().openSession();
		session.beginTransaction();

		try {
			Query query = session
					.createSQLQuery("delete from USER_NAVIGATION where USER_ID=? and APP_TYPE=?");
			query.setString(ZERO, userId);
			// check if application type is null or empty String then use
			// default "statusviewer" application type.
			if (null == app_type || app_type.equals("")) {
				app_type = "statusviewer";
			}
			query.setParameter(ONE, app_type);
			query.executeUpdate();
			return Response.status(Response.Status.OK).build();
		} catch (HibernateException he) {
			LOGGER.error("Error while deleting all records for user Id: "
					+ userId);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.build();

		} finally {
			session.getTransaction().commit();
			session.flush();
			session.close();
		}
	}

	/**
	 * Get Session factory instance
	 * 
	 * @return SessionFactory
	 * @throws IOException
	 */
	private SessionFactory getSessionFactory() {
		PortalContainer manager = PortalContainer.getInstance();
		HibernateService service_ = (HibernateService) manager
				.getComponentInstanceOfType(HibernateService.class);
		SessionFactory sessionFactory = service_.getSessionFactory();
		return sessionFactory;
	}

	/**
	 * 
	 * @param userId
	 * @param nodeId
	 * @param nodeName
	 * @param nodeType
	 * @param app_type
	 * @return boolean
	 * @throws IOException
	 */
	@Path("/updatewithoutlabel")
	@GET
	@Produces("application/xml")
	public Response updateHistoryRecord(@QueryParam("userId") String userId,
			@QueryParam("nodeId") int nodeId,
			@QueryParam("nodeName") String nodeName,
			@QueryParam("nodeType") String nodeType,
			@QueryParam("app_type") String app_type) {
		Session session = getSessionFactory().openSession();
		session.beginTransaction();

		try {
			Query query = session
					.createSQLQuery("update USER_NAVIGATION set NODE_NAME=?,NODE_TYPE=? where USER_ID=? and NODE_ID=? and APP_TYPE=?");
			query.setParameter(0, nodeName);
			query.setParameter(1, nodeType);
			query.setParameter(2, userId);
			query.setParameter(3, nodeId);
			// check if application type is null or empty String then use
			// default "statusviewer" application type.
			if (null == app_type || app_type.equals("")) {
				app_type = "statusviewer";
			}
			query.setParameter(4, app_type);

			query.executeUpdate();
			return Response.status(Response.Status.OK).build();

		} catch (HibernateException he) {
			LOGGER.error("Error while retriving user Id for user: " + userId);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.build();
		} finally {
			session.getTransaction().commit();
			session.close();
		}
	}

	/**
	 * Update Node_Name and Node_type to Navigation History database.
	 * 
	 * @param userId
	 * @param nodeId
	 * @param nodeName
	 * @param nodeType
	 * @param app_type
	 * @param tabHistory
	 * @param nodeLabel
	 * @return boolean
	 * @throws IOException
	 */
	@Path("updatewithlabel")
	@GET
	@Produces("application/xml")
	public Response updateHistoryRecord(@QueryParam("userId") String userId,
			@QueryParam("nodeId") int nodeId,
			@QueryParam("nodeName") String nodeName,
			@QueryParam("nodeType") String nodeType,
			@QueryParam("app_type") String app_type,
			@QueryParam("tabHistory") String tabHistory,
			@QueryParam("nodeLabel") String nodeLabel) {
		Session session = getSessionFactory().openSession();
		session.beginTransaction();

		try {
			Query query = session
					.createSQLQuery("update USER_NAVIGATION set NODE_NAME=?,NODE_TYPE=?,TAB_HISTORY=?,NODE_LABEL=? where USER_ID=? and NODE_ID=? and APP_TYPE=?");
			query.setParameter(0, nodeName);
			query.setParameter(1, nodeType);
			query.setParameter(2, tabHistory);
			query.setParameter(3, nodeLabel);
			query.setParameter(4, userId);
			query.setParameter(5, nodeId);
			// check if application type is null or empty String then use
			// default "statusviewer" application type.
			if (null == app_type || app_type.equals("")) {
				app_type = "statusviewer";
			}
			query.setParameter(6, app_type);

			query.executeUpdate();
			return Response.status(Response.Status.OK).build();

		} catch (HibernateException he) {
			LOGGER.error("Error while retriving user Id for user: " + userId);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.build();
		} finally {
			session.getTransaction().commit();
			session.close();
		}
	}

	/**
	 * Update tab history column
	 * 
	 * @param userId
	 * @param nodeId
	 * @param app_type
	 * @param tabHistory
	 * @return boolean
	 * @throws IOException
	 */
	@Path("/updatetabhistoryfield")
	@GET
	@Produces("application/xml")
	public Response updateTabHistoryRecord(@QueryParam("userId") String userId,
			@QueryParam("nodeId") int nodeId,
			@QueryParam("app_type") String app_type,
			@QueryParam("tabHistory") String tabHistory) {
		Session session = getSessionFactory().openSession();
		session.beginTransaction();

		try {
			Query query = session
					.createSQLQuery("update USER_NAVIGATION set TAB_HISTORY=? where USER_ID=? and NODE_ID=? and APP_TYPE=?");
			query.setParameter(0, tabHistory);
			query.setParameter(1, userId);
			query.setParameter(2, nodeId);
			// check if application type is null or empty String then use
			// default "statusviewer" application type.
			if (null == app_type || app_type.equals("")) {
				app_type = "statusviewer";
			}
			query.setParameter(3, app_type);

			query.executeUpdate();
			return Response.status(Response.Status.OK).build();

		} catch (HibernateException he) {
			LOGGER.error("Error while retriving user Id for user: " + userId);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.build();
		} finally {
			session.getTransaction().commit();
			session.close();
		}

	}

	/**
	 * Update Node label column
	 * 
	 * @param userId
	 * @param nodeId
	 * @param app_type
	 * @param nodeLabel
	 * @return boolean
	 * @throws IOException
	 */
	@Path("/updatenodelabel")
	@GET
	@Produces("application/xml")
	public Response updateNodeLabelRecord(@QueryParam("userId") String userId,
			@QueryParam("nodeId") int nodeId,
			@QueryParam("app_type") String app_type,
			@QueryParam("nodeLabel") String nodeLabel) {
		Session session = getSessionFactory().openSession();
		session.beginTransaction();

		try {
			Query query = session
					.createSQLQuery("update USER_NAVIGATION set NODE_LABEL=? where USER_ID=? and NODE_ID=? and APP_TYPE=?");
			query.setParameter(0, nodeLabel);
			query.setParameter(1, userId);
			query.setParameter(2, nodeId);
			// check if application type is null or empty String then use
			// default "statusviewer" application type.
			if (null == app_type || app_type.equals("")) {
				app_type = "statusviewer";
			}
			query.setParameter(3, app_type);

			query.executeUpdate();
			return Response.status(Response.Status.OK).build();

		} catch (HibernateException he) {
			LOGGER.error("Error while retriving user Id for user: " + userId);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.build();
		} finally {
			session.getTransaction().commit();
			session.close();
		}
	}

	/**
	 * get max node id
	 * 
	 * @param userId
	 * @param app_type
	 * @return max node id
	 * @throws IOException
	 */
	@Path("/getmaxnodeid")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getMaxNodeID(@QueryParam("userId") String userId,
			@QueryParam("app_type") String app_type) {
		Session session = getSessionFactory().openSession();
		Integer result = 0;
		try {
			Query query = session
					.createSQLQuery("select max(NODE_ID) from USER_NAVIGATION where USER_ID=? and APP_TYPE=?");
			query.setParameter(0, userId);
			// check if application type is null or empty String then use
			// default "statusviewer" application type.
			if (null == app_type || app_type.equals("")) {
				app_type = "statusviewer";
			}
			query.setParameter(1, app_type);
			List list = query.list();

			Object output = list.get(0);
			if (output != null) {
				result = (Integer) output;
			} else {
				result = 0;
			}

		} catch (HibernateException he) {
			LOGGER.error("Error while retriving records for user Id : "
					+ userId);

		} finally {
			session.flush();
			session.close();
		}
		return String.valueOf(result.intValue());
	}

	/**
	 * This method is used to delete a single record of a user identified by
	 * "userId" from Navigation History database.
	 * 
	 * @param userId
	 * @param nodeId
	 * @param app_type
	 * @throws IOException
	 */
	@Path("/deletenode")
	@GET
	@Produces("application/xml")
	public Response deleteHistoryRecord(@QueryParam("userId") String userId,
			@QueryParam("nodeId") int nodeId,
			@QueryParam("app_type") String app_type) {
		Session session = getSessionFactory().openSession();

		try {
			Transaction transaction = session.beginTransaction();
			Query query = session
					.createSQLQuery("delete from USER_NAVIGATION where USER_ID=? and NODE_ID=?  and APP_TYPE=?");
			query.setParameter(ZERO, userId);
			query.setParameter(ONE, nodeId);

			// check if application type is null or empty String then use
			// default "statusviewer" application type.
			if (null == app_type || app_type.equals("")) {
				app_type = "statusviewer";
			}
			query.setParameter(TWO, app_type);
			query.executeUpdate();

			transaction.commit();
			return Response.status(Response.Status.OK).build();
		} catch (HibernateException he) {
			LOGGER.error("Error while deleting record for user" + userId);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.build();
		} finally {
			session.flush();
			session.close();
		}
	}

}
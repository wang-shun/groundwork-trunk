package org.groundwork.rs.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.dto.DtoCategory;
import org.groundwork.rs.dto.DtoCategoryList;
import org.groundwork.rs.dto.DtoCategoryMemberUpdate;
import org.groundwork.rs.dto.DtoCategoryUpdateList;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoOperationResults;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.GenericType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class CategoryClient extends BaseRestClient {

    protected static Log log = LogFactory.getLog(CategoryClient.class);

    private static final String API_ROOT_SINGLE = "/categories";
    private static final String API_ROOT = API_ROOT_SINGLE + "/";

    public static final String DELETE_LEAF_ONLY = "LEAF_ONLY";
    public static final String DELETE_CASCADE = "CASCADE";
    public static final String DELETE_CASCADE_ALL = "CASCADE_ALL";
    public static final String DELETE_ORPHAN_CHILDREN_AS_ROOTS = "ORPHAN_CHILDREN_AS_ROOTS";
    public static final String DELETE_ADD_CHILDREN_TO_PARENTS = "ADD_CHILDREN_TO_PARENTS";

    public static final String CREATE_AS_ROOT = "AS_ROOT";
    public static final String CREATE_AS_CHILD = "AS_CHILD";
    public static final String CREATE_AS_CHILD_WITH_PARENT_CHILDREN = "AS_CHILD_WITH_PARENT_CHILDREN";

    public static final String CLONE_AS_ROOT = "AS_ROOT";
    public static final String CLONE_AS_ROOT_WITH_CHILDREN = "AS_ROOT_WITH_CHILDREN";
    public static final String CLONE_AS_LEAF_WITH_PARENTS = "AS_LEAF_WITH_PARENTS";
    public static final String CLONE_WITH_PARENTS_AND_CHILDREN = "WITH_PARENTS_AND_CHILDREN";

    public static final String MODIFY_ROOT = "ROOT";
    public static final String MODIFY_ROOT_REMOVE_PARENTS = "ROOT_REMOVE_PARENTS";
    public static final String MODIFY_UNROOT = "UNROOT";
    public static final String MODIFY_ADD_PARENTS = "ADD_PARENTS";
    public static final String MODIFY_ADD_PARENTS_UNROOT = "ADD_PARENTS_UNROOT";
    public static final String MODIFY_ADD_CHILDREN = "ADD_CHILDREN";
    public static final String MODIFY_ADD_CHILDREN_UNROOT = "ADD_CHILDREN_UNROOT";
    public static final String MODIFY_REMOVE_PARENTS = "REMOVE_PARENTS";
    public static final String MODIFY_REMOVE_CHILDREN = "REMOVE_CHILDREN";
    public static final String MODIFY_MOVE_CHILD = "MOVE_CHILD";
    public static final String MODIFY_ADD_CHILD = "ADD_CHILD";
    public static final String MODIFY_ADD_CHILD_UNROOT = "ADD_CHILD_UNROOT";
    public static final String MODIFY_MOVE_PARENT = "MOVE_PARENT";
    public static final String MODIFY_ADD_PARENT = "ADD_PARENT";
    public static final String MODIFY_ADD_PARENT_UNROOT = "ADD_PARENT_UNROOT";
    public static final String MODIFY_SWAP_PARENTS = "SWAP_PARENTS";

    public static final String ENTITY_TYPE_CODE_SERVICEGROUP = "SERVICE_GROUP";
    public static final String ENTITY_TYPE_CODE_SERVICESTATUS = "SERVICE_STATUS";
    public static final String ENTITY_TYPE_CODE_SERVICECATEGORY = "SERVICE_CATEGORY";
    public static final String ENTITY_TYPE_CODE_HOST = "HOST";
    public static final String ENTITY_TYPE_CODE_HOSTCATEGORY = "HOST_CATEGORY";
    public static final String ENTITY_TYPE_CODE_CUSTOMGROUP = "CUSTOM_GROUP";
    public static final String ENTITY_TYPE_CODE_HOSTGROUP = "HOSTGROUP";

    public CategoryClient(String deploymentUrl) {
        super(deploymentUrl);
    }

    public CategoryClient(String deploymentUrl, MediaType mediaType) {
        this(deploymentUrl);
        this.mediaType = mediaType;
    }

    public DtoCategory lookup(String categoryName, String entityType) throws CollageRestException {
        return lookup(categoryName, entityType, DtoDepthType.Shallow);
    }
    
    public DtoCategory lookup(String categoryName, String entityType, DtoDepthType depthType) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoCategory> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildLookupWithDepth(API_ROOT, categoryName, entityType, depthType);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<DtoCategory>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    DtoCategory category = response.getEntity(new GenericType<DtoCategory>() {});
                    return category;
                }
                else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    return null;
                }
                else if (response.getResponseStatus() == Response.Status.UNAUTHORIZED && retry < 1) {
                    log.info(RETRY_AUTH);
                    status = response.getResponseStatus();
                    response.releaseConnection();
                    tokenSessionManager.removeToken(deploymentUrl);
                    continue;
                }
                status = response.getResponseStatus();
                break;
            }
        }
        catch (Exception e) {
            throw new CollageRestException(e);
        }
        finally {
            if (response != null)
                response.releaseConnection();
        }
        if (status == null)
            status = Response.Status.SERVICE_UNAVAILABLE;
        throw new CollageRestException(String.format("Exception executing lookup category (%s) with status code of %d, reason: %s",
                categoryName, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    public List<DtoCategory> query(String query) throws CollageRestException {
        return query(query, DtoDepthType.Shallow);
    }

    public List<DtoCategory> query(String query, DtoDepthType depthType) throws CollageRestException {
        return query(query, depthType, -1, -1);
    }

    public List<DtoCategory> query(String query, int first, int count) throws CollageRestException {
        return query(query, DtoDepthType.Shallow, first, count);
    }

    public List<DtoCategory> query(String query, DtoDepthType depthType, int first, int count) throws CollageRestException {
        Response.Status status;
        try {
            String url = buildEncodedQuery(API_ROOT, query, depthType, first, count);
            List<DtoCategory> [] categories = new List[1];
            status = categoriesRequest(url, categories);
            if (categories[0] != null) {
                return categories[0];
            }
        }
        catch (Exception e) {
            throw new CollageRestException(e);
        }
        throw new CollageRestException(String.format("Exception executing query categories (%s) with status code of %d, reason: %s",
                query, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    public List<DtoCategory> list(DtoDepthType depthType, int first, int count) throws CollageRestException {
        return query(null, depthType, first, count);
    }

    public List<DtoCategory> list(DtoDepthType depth) throws CollageRestException {
        return query(null, depth, -1, -1);
    }

    public List<DtoCategory> list() throws CollageRestException {
        return query(null, DtoDepthType.Shallow, -1, -1);
    }

    /**
     * Return list of all categories of a specified entity type, optionally
     * returning root categories only. Returns categories with the default
     * shallow depth.
     *
     * @param entityType category entity type
     * @param roots roots only flag
     * @return categories list
     */
    public List<DtoCategory> entityType(String entityType, boolean roots) {
        return entityType(entityType, roots, DtoDepthType.Shallow);
    }

    /**
     * Return list of all categories of a specified entity type, optionally
     * returning root categories only.
     *
     * @param entityType category entity type
     * @param roots roots only flag
     * @param depth category field depth
     * @return categories list
     */
    public List<DtoCategory> entityType(String entityType, boolean roots, DtoDepthType depth) {
        Response.Status status;
        try {
            String params = buildEncodedQueryParamsWithDepth(new String[]{"entityTypeName", "roots"},
                    new String[]{entityType, Boolean.toString(roots)}, depth);
            String url = buildUrlWithQueryParams(API_ROOT, params);
            List<DtoCategory> [] categories = new List[1];
            status = categoriesRequest(url, categories);
            if (categories[0] != null) {
                return categories[0];
            }
        }
        catch (Exception e) {
            throw new CollageRestException(e);
        }
        throw new CollageRestException(String.format("Exception executing entityType (%s) with status code of %d, reason: %s",
                entityType, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Return category hierarchy for a specific category. Returns categories
     * with the default shallow depth.
     *
     * @param categoryName category name
     * @param entityType category entity type
     * @return hierarchy categories list
     */
    public List<DtoCategory> hierarchy(String categoryName, String entityType) {
        return hierarchy(categoryName, entityType, DtoDepthType.Shallow);
    }

    /**
     * Return category hierarchy for a specific category.
     *
     * @param categoryName category name
     * @param entityType category entity type
     * @param depth category field depth
     * @return hierarchy categories list
     */
    public List<DtoCategory> hierarchy(String categoryName, String entityType, DtoDepthType depth) {
        Response.Status status;
        try {
            String params = buildEncodedQueryParamsWithDepth(new String[]{"name", "entityTypeName"},
                    new String[]{categoryName, entityType}, depth);
            String url = buildUrlWithQueryParams(API_ROOT, params);
            List<DtoCategory> [] categories = new List[1];
            status = categoriesRequest(url, categories);
            if (categories[0] != null) {
                return categories[0];
            }
        }
        catch (Exception e) {
            throw new CollageRestException(e);
        }
        throw new CollageRestException(String.format("Exception executing hierarchy (%s:%s) with status code of %d, reason: %s",
                categoryName, entityType, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Return list of category roots that have a specific entity in
     * their hierarchy of category/category entities. Returns categories
     * with the default shallow depth.
     *
     * @param entityType category entity type
     * @param entityObjectId entity object id
     * @param entityEntityType entity entity type
     * @return root entity categories list
     */
    public List<DtoCategory> entityRoots(String entityType, int entityObjectId, String entityEntityType) {
        return entityRoots(entityType, entityObjectId, entityEntityType, DtoDepthType.Shallow);
    }

    /**
     * Return list of category roots that have a specific entity in
     * their hierarchy of category/category entities.
     *
     * @param entityType category entity type
     * @param entityObjectId entity object id
     * @param entityEntityType entity entity type
     * @param depth category field depth
     * @return root entity categories list
     */
    public List<DtoCategory> entityRoots(String entityType, int entityObjectId, String entityEntityType, DtoDepthType depth) {
        Response.Status status;
        try {
            String params = buildEncodedQueryParamsWithDepth(
                    new String[]{"entityTypeName", "entityObjectId", "entityEntityTypeName", "entityRoots"},
                    new String[]{entityType, Integer.toString(entityObjectId), entityEntityType, "true"}, depth);
            String url = buildUrlWithQueryParams(API_ROOT, params);
            List<DtoCategory> [] categories = new List[1];
            status = categoriesRequest(url, categories);
            if (categories[0] != null) {
                return categories[0];
            }
        }
        catch (Exception e) {
            throw new CollageRestException(e);
        }
        throw new CollageRestException(String.format("Exception executing entity roots (%s, %d:%s) with status code of %d, reason: %s",
                entityType, entityObjectId, entityEntityType, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Return list of categories that have a specific entity in their
     * category entities. Returns categories with the default shallow depth.
     *
     * @param entityType category entity type
     * @param entityObjectId entity object id
     * @param entityEntityType entity entity type
     * @return entity categories list
     */
    public List<DtoCategory> entity(String entityType, int entityObjectId, String entityEntityType) {
        return entity(entityType, entityObjectId, entityEntityType, DtoDepthType.Shallow);
    }

    /**
     * Return list of categories that have a specific entity in their
     * category entities.
     *
     * @param entityType category entity type
     * @param entityObjectId entity object id
     * @param entityEntityType entity entity type
     * @param depth category field depth
     * @return entity categories list
     */
    public List<DtoCategory> entity(String entityType, int entityObjectId, String entityEntityType, DtoDepthType depth) {
        Response.Status status;
        try {
            String params = buildEncodedQueryParamsWithDepth(
                    new String[]{"entityTypeName", "entityObjectId", "entityEntityTypeName", "entityRoots"},
                    new String[]{entityType, Integer.toString(entityObjectId), entityEntityType, "false"}, depth);
            String url = buildUrlWithQueryParams(API_ROOT, params);
            List<DtoCategory> [] categories = new List[1];
            status = categoriesRequest(url, categories);
            if (categories[0] != null) {
                return categories[0];
            }
        }
        catch (Exception e) {
            throw new CollageRestException(e);
        }
        throw new CollageRestException(String.format("Exception executing entity (%s, %d:%s) with status code of %d, reason: %s",
                entityType, entityObjectId, entityEntityType, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Perform general categories request.
     *
     * @param url categories request url
     * @param returnedCategories categories returned by request
     * @return request status
     */
    private Response.Status categoriesRequest(String url, List<DtoCategory> [] returnedCategories) {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoCategoryList> response = null;
        try {
            for (int retry = 0; (retry < RETRIES); retry++) {
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<DtoCategoryList>(){});
                status = response.getResponseStatus();
                if (status == Response.Status.OK) {
                    DtoCategoryList categories = response.getEntity(new GenericType<DtoCategoryList>(){});
                    returnedCategories[0] = categories.getCategories();
                    return status;
                } else if (status == Response.Status.NOT_FOUND) {
                    // return an empty list for not found exception
                    returnedCategories[0] = new ArrayList<DtoCategory>();
                    return status;
                } else if ((status == Response.Status.UNAUTHORIZED) && (retry < 1)) {
                    log.info(RETRY_AUTH);
                    response.releaseConnection();
                    tokenSessionManager.removeToken(deploymentUrl);
                    continue;
                }
                break;
            }
        } catch (Exception e) {
            throw new CollageRestException(e);
        } finally {
            if (response != null) {
                response.releaseConnection();
            }
        }
        if (status == null) {
            status = Response.Status.SERVICE_UNAVAILABLE;
        }
        return status;
    }

    public DtoOperationResults post(DtoCategoryList updates) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoOperationResults> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                ClientRequest request = createClientRequest(build(API_ROOT_SINGLE));
                request.accept(mediaType);
                request.body(mediaType, updates);
                response = request.post();
                if (response.getResponseStatus() == Response.Status.OK) {
                    DtoOperationResults results = response.getEntity(DtoOperationResults.class);
                    return results;
                }
                else if (response.getResponseStatus() == Response.Status.UNAUTHORIZED && retry < 1) {
                    log.info(RETRY_AUTH);
                    status = response.getResponseStatus();
                    response.releaseConnection();
                    tokenSessionManager.removeToken(deploymentUrl);
                    continue;
                }
                status = response.getResponseStatus();
                break;
            }
        }
        catch (Exception e) {
            throw new CollageRestException(e);
        }
        finally {
            if (response != null)
                response.releaseConnection();
        }
        if (status == null)
            status = Response.Status.SERVICE_UNAVAILABLE;
        throw new CollageRestException(String.format("Exception executing post to categories with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Update categories in a single transaction.
     *
     * @param updates category updates list
     * @return operation results
     * @throws CollageRestException
     */
    public DtoOperationResults update(DtoCategoryUpdateList updates) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoOperationResults> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                ClientRequest request = createClientRequest(build(API_ROOT_SINGLE));
                request.accept(mediaType);
                request.body(mediaType, updates);
                response = request.put();
                status = response.getResponseStatus();
                if (status == Response.Status.OK) {
                    DtoOperationResults results = response.getEntity(DtoOperationResults.class);
                    return results;
                } else if (status == Response.Status.UNAUTHORIZED && retry < 1) {
                    log.info(RETRY_AUTH);
                    response.releaseConnection();
                    tokenSessionManager.removeToken(deploymentUrl);
                    continue;
                }
                break;
            }
        } catch (Exception e) {
            throw new CollageRestException(e);
        } finally {
            if (response != null) {
                response.releaseConnection();
            }
        }
        if (status == null) {
            status = Response.Status.SERVICE_UNAVAILABLE;
        }
        throw new CollageRestException(String.format("Exception executing put to categories with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Add category members.
     *
     * @param memberUpdate category member updates
     * @return operation results
     * @throws CollageRestException
     */
    public DtoOperationResults addMembers(DtoCategoryMemberUpdate memberUpdate) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoOperationResults> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                ClientRequest request = createClientRequest(buildUrlWithPath(API_ROOT, "addmembers"));
                request.accept(mediaType);
                request.body(mediaType, memberUpdate);
                response = request.put();
                status = response.getResponseStatus();
                if (status == Response.Status.OK) {
                    DtoOperationResults results = response.getEntity(DtoOperationResults.class);
                    return results;
                } else if (status == Response.Status.UNAUTHORIZED && retry < 1) {
                    log.info(RETRY_AUTH);
                    response.releaseConnection();
                    tokenSessionManager.removeToken(deploymentUrl);
                    continue;
                }
                break;
            }
        } catch (Exception e) {
            throw new CollageRestException(e);
        } finally {
            if (response != null) {
                response.releaseConnection();
            }
        }
        if (status == null) {
            status = Response.Status.SERVICE_UNAVAILABLE;
        }
        throw new CollageRestException(String.format("Exception executing put to categories add members with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Delete category members.
     *
     * @param memberUpdate category member updates
     * @return operation results
     * @throws CollageRestException
     */
    public DtoOperationResults deleteMembers(DtoCategoryMemberUpdate memberUpdate) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoOperationResults> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                ClientRequest request = createClientRequest(buildUrlWithPath(API_ROOT, "deletemembers"));
                request.accept(mediaType);
                request.body(mediaType, memberUpdate);
                response = request.put();
                status = response.getResponseStatus();
                if (status == Response.Status.OK) {
                    DtoOperationResults results = response.getEntity(DtoOperationResults.class);
                    return results;
                } else if (status == Response.Status.UNAUTHORIZED && retry < 1) {
                    log.info(RETRY_AUTH);
                    response.releaseConnection();
                    tokenSessionManager.removeToken(deploymentUrl);
                    continue;
                }
                break;
            }
        } catch (Exception e) {
            throw new CollageRestException(e);
        } finally {
            if (response != null) {
                response.releaseConnection();
            }
        }
        if (status == null) {
            status = Response.Status.SERVICE_UNAVAILABLE;
        }
        throw new CollageRestException(String.format("Exception executing put to categories delete members with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Delete single category.
     *
     * @param categoryName category name
     * @param entityType category entity type
     * @return operation results
     * @throws CollageRestException
     */
    public DtoOperationResults delete(String categoryName, String entityType) throws CollageRestException {
        DtoCategoryList deletes = new DtoCategoryList();
        DtoCategory dtoCategory = new DtoCategory();
        dtoCategory.setName(categoryName);
        dtoCategory.setEntityTypeName(entityType);
        deletes.add(dtoCategory);
        return delete(deletes);
    }

    public DtoOperationResults delete(DtoCategoryList deletes) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoOperationResults> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                ClientRequest request = createClientRequest(build(API_ROOT_SINGLE));
                request.accept(mediaType);
                request.body(mediaType, deletes);
                response = request.delete();
                if (response.getResponseStatus() == Response.Status.OK) {
                    DtoOperationResults results = response.getEntity(DtoOperationResults.class);
                    return results;
                }
                else if (response.getResponseStatus() == Response.Status.UNAUTHORIZED && retry < 1) {
                    log.info(RETRY_AUTH);
                    status = response.getResponseStatus();
                    response.releaseConnection();
                    tokenSessionManager.removeToken(deploymentUrl);
                    continue;
                }
                status = response.getResponseStatus();
                break;
            }
        }
        catch (Exception e) {
            throw new CollageRestException(e);
        }
        finally {
            if (response != null)
                response.releaseConnection();
        }
        if (status == null)
            status = Response.Status.SERVICE_UNAVAILABLE;
        throw new CollageRestException(String.format("Exception executing delete to categories with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }
}

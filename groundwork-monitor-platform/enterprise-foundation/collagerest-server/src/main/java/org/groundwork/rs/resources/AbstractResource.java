package org.groundwork.rs.resources;

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.metrics.CollageMetrics;
import com.groundwork.collage.metrics.CollageTimer;
import com.groundwork.collage.model.Category;
import com.groundwork.collage.model.CategoryEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.category.CategoryService;
import org.groundwork.foundation.bs.logmessage.ConsolidationService;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.SortCriteria;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoPropertiesBase;
import org.groundwork.rs.dto.DtoSortType;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

public abstract class AbstractResource {

    public static final String RETRIEVAL_METHOD_NOT_IMPLEMENTED_OR_UNKNOWN = "Retrieval method not implemented or unknown.";
    protected static Log log = LogFactory.getLog(AbstractResource.class);
    public static final int TOO_MANY_REQUESTS = 429;

    @Context
    protected HttpServletRequest request;
    @Context
    protected ServletContext context;
    @Context
    protected UriInfo uriInfo;
    @Context
    protected HttpHeaders httpHeaders;
    @Context
    protected SecurityContext securityContext;

    private CollageMetrics collageMetrics = null;

    protected Response badRequest(String comment /*WebError error*/) {
        return Response.status(Response.Status.BAD_REQUEST)./*header(HttpHeader.X_ML_ERROR.NAME, error.toString()). */ entity(comment).build();
    }

    protected Response notFound(String comment /*WebError error*/) {
        return Response.status(Response.Status.NOT_FOUND). /* header(HttpHeader.X_ML_ERROR.NAME, error.toString()).*/ entity(comment).build();
    }

    protected Response unauthorized(String comment /* WebError error */) {
        return Response.status(Response.Status.UNAUTHORIZED). /*header(HttpHeader.X_ML_ERROR.NAME, error.toString()). */ entity(comment).build();
    }

    protected URI buildResourceLocator(UriInfo info, String resourcePrefix, String entity) {
        return uriInfo.getBaseUriBuilder().path(resourcePrefix + entity).build();
    }

    protected String buildResourceLocatorTemplate(UriInfo info, String resourcePrefix) {
        return uriInfo.getBaseUriBuilder().path(resourcePrefix).build().toString();
    }

    protected URI buildResourceLocatorWithQueryParam(UriInfo info, String resourcePrefix, String entity,
                                                     String queryParamName, String queryParamValue) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(resourcePrefix);
        buffer.append(entity);
        buffer.append("?");
        buffer.append(queryParamName);
        buffer.append("=");
        buffer.append(queryParamValue);
        return uriInfo.getBaseUriBuilder().path(buffer.toString()).build();
    }

    public CollageAdminInfrastructure getAdminInfrastructureService() {
        CollageAdminInfrastructure admin = (CollageAdminInfrastructure) CollageFactory.getInstance()
                .getAPIObject("com.groundwork.collage.CollageAdmin");
        return admin;
    }

    public ConsolidationService getConsolidationService() {
        ConsolidationService consolidationService = (ConsolidationService) CollageFactory.getInstance()
                .getConsolidationService();
        return consolidationService;
    }

    protected void addToNotFoundList(List<String> notFound, DtoOperationResults results, String entityType) {
        if (notFound.size() > 0) {
            StringBuffer message = new StringBuffer();
            boolean first = true;
            for (String name : notFound) {
                if (!first) {
                    message.append(",");
                }
                message.append(name);
                first = false;
            }
            results.warn(message.toString(), entityType + "s did not exist and were not processed");
        }
    }

    protected SortCriteria createSortCriteria(String sortField, DtoSortType sortType) {
        SortCriteria sortCriteria = null;
        if (sortField != null) {
            if (sortType == DtoSortType.Descending || sortType == DtoSortType.None)
                sortCriteria  = SortCriteria.desc(sortField);
            else
                sortCriteria  = SortCriteria.asc(sortField);
        }
        return sortCriteria;
    }

    protected String formatDate(Date date) {
        if (date != null) {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            return formatter.format(date);
        }
        return null;
    }

    protected CategoryService getCategoryService() {
        return CollageFactory.getInstance().getCategoryService();
    }
    protected String buildCategoryList(String categoryName, String entityTypeName) {
        String name = categoryName.replace("'", "");
        Category category = getCategoryService().getCategoryByName(name, entityTypeName);
        if (category == null) {
            return null;
        }
        Collection<CategoryEntity> categoryEntities = category.getCategoryEntities();
        StringBuffer ids = new StringBuffer();
        if (categoryEntities != null) {
            int count = 0;
            ids.append("(");
            for (CategoryEntity entity : categoryEntities) {
                if (count > 0) ids.append(",");
                ids.append(entity.getObjectID());
                count++;
            } // end while
            ids.append(")");
        } // end if
        return ids.toString();
    }

    protected FilterCriteria createInFilterCriteria(String propertyName, String value) {
        FilterCriteria filterCriteria = null;
        if (propertyName != null && value != null) {
            StringTokenizer tokenizer = new StringTokenizer(value, ",");
            Object[] objArray = new Object[tokenizer.countTokens()];
            int i = 0;
            while (tokenizer.hasMoreTokens()) {
                String tokenValue = tokenizer.nextToken();
                tokenValue = tokenValue.replace("'", "");
                objArray[i] = tokenValue;
                i++;
            }
            filterCriteria = FilterCriteria.in(propertyName, objArray);
//            filter.and(FilterCriteria.eq(PROP_ENTITYTYPENAME, entityTypeName));
        }
        return filterCriteria;
    }

    protected List<String> parseNames(String commaSeparated) {
        String names[] = commaSeparated.split(",");
        List<String> ids = new ArrayList<String>(names.length);
        for (String name : names) {
            ids.add(name.trim());
        }
        return ids;
    }

    protected String[] parseNamesArray(String commaSeparated) {
        String names[] = commaSeparated.split(",");
        int index = 0;
        for (String name : names) {
            names[index] = name.trim();
            index++;
        }
        return names;
    }

    protected List<Integer> parseIds(String commaSeparated) throws NumberFormatException {
        String stringIds[] = commaSeparated.split(",");
        List<Integer> ids = new ArrayList<Integer>(stringIds.length);
        for (String stringId : stringIds) {
            ids.add(new Integer(stringId.trim()));
        }
        return ids;
    }

    protected Integer[] parseIdArray(String commaSeparated) throws NumberFormatException {
        String stringIds[] = commaSeparated.split(",");
        Integer ids[] = new Integer[stringIds.length];
        int ix = 0;
        for (String id : stringIds) {
            ids[ix] = new Integer(id.trim());
            ix++;
        }
        return ids;
    }

    protected String decode(String string) {
        try {
            return URLDecoder.decode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("failed to decode " + string, e);
        }
        return string;
    }

    protected Properties putAllProperties(DtoPropertiesBase dtoProperties, Properties properties) {
        if (properties != null && dtoProperties != null) {
            for (String key : dtoProperties.getProperties().keySet()) {
                String value = dtoProperties.getProperties().get(key);
                if (value == null) {
                    dtoProperties.putProperty(key, "");
                }
            }
            properties.putAll(dtoProperties.getProperties());
        }
        return properties;
    }

    protected boolean isEmpty(String s) {
        if (s == null) return true;
        if (s.trim().equals("")) return true;
        return false;
    }

    private CollageMetrics getCollageMetrics() {
        if (collageMetrics == null) {
            collageMetrics = CollageFactory.getInstance().getCollageMetrics();
        }
        return collageMetrics;
    }

    public CollageTimer startMetricsTimer() {
        StackTraceElement element = Thread.currentThread().getStackTrace()[2];
        String className = element.getClassName().substring(element.getClassName().lastIndexOf('.') + 1);
        CollageMetrics collageMetrics = getCollageMetrics();
        return (collageMetrics == null ? null : collageMetrics.startTimer(className, element.getMethodName()));
    }

    public void stopMetricsTimer(CollageTimer timer) {
        CollageMetrics collageMetrics = getCollageMetrics();
        if (collageMetrics != null) getCollageMetrics().stopTimer(timer);
    }

}


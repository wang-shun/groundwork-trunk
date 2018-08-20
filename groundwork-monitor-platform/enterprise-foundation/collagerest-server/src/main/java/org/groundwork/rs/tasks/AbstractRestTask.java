package org.groundwork.rs.tasks;

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.CollageFactory;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AbstractRestTask {

    private final String taskId;
    private final String name;
    private final String uriTemplate;
    private static final String ENCODING = "UTF-8";


    protected AbstractRestTask(String name, String uriTemplate) {
//        this.taskId = UUID.randomUUID().toString();
        this.taskId = Long.toString(System.currentTimeMillis());
        this.name = name;
        this.uriTemplate = uriTemplate;
    }

    protected String buildResourceLocator(String entity) {
        try {
            return uriTemplate + URLEncoder.encode(entity, ENCODING);
        }
        catch (Exception e) {
            return uriTemplate + entity;
        }
    }

    protected String buildResourceLocatorWithQueryParam(String entity,
                                                     String queryParamName, String queryParamValue) {
        try {
            StringBuffer buffer = new StringBuffer();
            buffer.append(buildResourceLocator(entity));
            buffer.append("?");
            buffer.append(URLEncoder.encode(queryParamName, ENCODING));
            buffer.append("=");
            buffer.append(URLEncoder.encode(queryParamValue, ENCODING));
            return buffer.toString();
        }
        catch (Exception e) {
            return uriTemplate + entity;
        }
    }

    protected CollageAdminInfrastructure getAdminInfrastructureService() {
        CollageAdminInfrastructure admin = (CollageAdminInfrastructure) CollageFactory.getInstance()
                .getAPIObject("com.groundwork.collage.CollageAdmin");
        return admin;
    }

    protected String formatDate(Date date) {
        if (date != null) {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return formatter.format(date);
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getUriTemplate() {
        return uriTemplate;
    }
}

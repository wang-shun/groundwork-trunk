package com.groundwork.collage.impl.aop;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.model.impl.Category;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.ServiceNotify;
import org.groundwork.foundation.bs.ServiceNotifyAction;
import org.groundwork.foundation.bs.ServiceNotifyEntityType;
import org.groundwork.foundation.bs.category.CategoryService;
import org.groundwork.foundation.bs.events.EntityPublisher;
import org.groundwork.foundation.bs.statistics.StatisticsService;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CategoryAfterAdvice implements AfterReturningAdvice {

    private Log log = LogFactory.getLog(this.getClass());

    private static final String METHOD_ADD_ENTITY_CATEGORY = "addCategoryEntity";
    private static final String METHOD_REMOVE_ENTITY_CATEGORY = "removeCategoryEntity";
    private static final String METHOD_REMOVE_CATEGORY = "removeCategory";
    private static final String METHOD_PROPAGATE_SERVICE_CATEGORIES = "propagateServiceChangesToServiceGroup";
    private static final String METHOD_SAVE_CATEGORY = "saveCategory";
    private static final String METHOD_UPDATE_CATEGORY = "updateCategory";
    private static final String METHOD_PROPAGATE_CREATED_CATEGORIES = "propagateCreatedCategories";
    private static final String METHOD_PROPAGATE_DELETED_CATEGORIES = "propagateDeletedCategories";
    private static final String METHOD_PROPAGATE_MODIFIED_CATEGORIES = "propagateModifiedCategories";

    private StatisticsService statisticsService = null;

    public CategoryAfterAdvice(StatisticsService statService) {
        statisticsService = statService;
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {

        String methodName = method.getName();
        log.info("Method name :" + methodName);
        if (methodName.equalsIgnoreCase(METHOD_ADD_ENTITY_CATEGORY)) {
            // get method return value
            Category category = getReturnValue(returnValue, Category.class);
            // notify/publish category changes
            notifyAndPublish(ServiceNotifyAction.CREATE, category);
        } else if (methodName.equalsIgnoreCase(METHOD_REMOVE_ENTITY_CATEGORY)) {
            if ((args != null) && (args.length > 2)) {
                // get method return value
                Category category = getReturnValue(returnValue, Category.class);
                // notify/publish category changes
                notifyAndPublish(ServiceNotifyAction.UPDATE, category);
            } else {
                // get method return value
                Collection<Category> categories = (Collection<Category>) getReturnValue(returnValue, Collection.class);
                if ((categories != null) && !categories.isEmpty()) {
                    for (Category category : categories) {
                        // notify/publish category changes
                        notifyAndPublish(ServiceNotifyAction.UPDATE, category);
                    }
                }
            }
        } else if (methodName.equalsIgnoreCase(METHOD_REMOVE_CATEGORY)) {
            // get method return value
            Category category = getReturnValue(returnValue, Category.class);
            // notify/publish category changes
            notifyAndPublish(ServiceNotifyAction.DELETE, category);
        } else if (methodName.equalsIgnoreCase(METHOD_PROPAGATE_SERVICE_CATEGORIES)) {
            // get method return value
            Collection<Category> categories = (Collection<Category>) getReturnValue(returnValue, Collection.class);
            if ((categories != null) && !categories.isEmpty()) {
                for (Category category : categories) {
                    // notify/publish category changes
                    notifyAndPublish(ServiceNotifyAction.UPDATE, category);
                }
            }
        } else if (methodName.equalsIgnoreCase(METHOD_SAVE_CATEGORY)) {
            // get method argument
            Category category = getArg(args, 0, Category.class);
            // notify/publish category changes
            notifyAndPublish(ServiceNotifyAction.UPDATE, category);
        } else if (methodName.equalsIgnoreCase(METHOD_UPDATE_CATEGORY)) {
            // get method return value
            Category category = getReturnValue(returnValue, Category.class);
            // notify/publish category changes
            notifyAndPublish(ServiceNotifyAction.UPDATE, category);
        } else if (methodName.equalsIgnoreCase(METHOD_PROPAGATE_CREATED_CATEGORIES)) {
            // get method argument
            Collection<Category> categories = (Collection<Category>) getArg(args, 0, Collection.class);
            if ((categories != null) && !categories.isEmpty()) {
                for (Category category : categories) {
                    // notify/publish category changes
                    notifyAndPublish(ServiceNotifyAction.CREATE, category);
                }
            }
        } else if (methodName.equalsIgnoreCase(METHOD_PROPAGATE_DELETED_CATEGORIES)) {
            // get method argument
            Collection<Category> categories = (Collection<Category>) getArg(args, 0, Collection.class);
            if ((categories != null) && !categories.isEmpty()) {
                for (Category category : categories) {
                    // notify/publish category changes
                    notifyAndPublish(ServiceNotifyAction.DELETE, category);
                }
            }
        } else if (methodName.equalsIgnoreCase(METHOD_PROPAGATE_MODIFIED_CATEGORIES)) {
            // get method argument
            Collection<Category> categories = (Collection<Category>) getArg(args, 0, Collection.class);
            if ((categories != null) && !categories.isEmpty()) {
                for (Category category : categories) {
                    // notify/publish category changes
                    notifyAndPublish(ServiceNotifyAction.UPDATE, category);
                }
            }
        }
    }

    /**
     * Extract typed method return value.
     *
     * @param value method return value
     * @param valueClass value class
     * @return cast value or null
     */
    private static <T> T getReturnValue(Object value, Class<T> valueClass) {
        if ((value == null) || !valueClass.isAssignableFrom(value.getClass())) {
            return null;
        }
        return (T)value;
    }

    /**
     * Extract typed method argument.
     *
     * @param args method arguments
     * @param index argument index
     * @param argClass argument class
     * @return cast argument or null
     */
    private static <T> T getArg(Object [] args, int index, Class<T> argClass) {
        if ((args == null) || (index >= args.length) || !argClass.isAssignableFrom(args[index].getClass())) {
            return null;
        }
        return (T)args[index];
    }

    /**
     * Notify and publish category change.
     *
     * @param action notification action
     * @param category notification category
     */
    private void notifyAndPublish(ServiceNotifyAction action, Category category) {
        if (category == null) {
            return;
        }
        // notify/publish ServiceGroup and CustomGroup changes
        if (category.getEntityType().getName().equals(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP)) {
            Map<String, Object> notifyAtts = new HashMap<String, Object>(1);
            notifyAtts.put(EntityPublisher.NOTIFY_ATTR_SERVICEGROUP_ID, category.getCategoryId().intValue());
            notifyAtts.put(StatisticsService.NOTIFY_ATTR_ENTITY_NAME, category.getName());
            notifyAndPublish(ServiceNotifyEntityType.SERVICEGROUP, action, notifyAtts);
        } else if (category.getEntityType().getName().equals(CategoryService.ENTITY_TYPE_CODE_CUSTOMGROUP)) {
            Map<String, Object> notifyAtts = new HashMap<String, Object>(1);
            notifyAtts.put(EntityPublisher.NOTIFY_ATTR_CUSTOMGROUP_ID, category.getCategoryId().intValue());
            notifyAtts.put(StatisticsService.NOTIFY_ATTR_ENTITY_NAME, category.getName());
            notifyAndPublish(ServiceNotifyEntityType.CUSTOMGROUP, action, notifyAtts);
        }
    }

    /**
     * Notify and publish entity change.
     *
     * @param entityType notification entity type
     * @param action notification action
     * @param attributes notification attributes
     */
    private void notifyAndPublish(ServiceNotifyEntityType entityType, ServiceNotifyAction action,
                                  Map<String, Object> attributes) {
        ServiceNotify notification = new ServiceNotify(entityType, action, attributes);
        statisticsService.notify(notification);
        publishEntity(notification);
    }


    /**
     * Publishes the Category notifications only
     * 
     * @param notify
     */
    private void publishEntity(ServiceNotify notify) {
        ServiceNotifyEntityType entityType = notify.getEntityType();
        log.info("Publishing Category....");
        if (notify == null)
            return;
        CollageFactory beanFactory = CollageFactory.getInstance();
        ConcurrentHashMap<String, String> distMap = beanFactory
                .getEntityPublisher().getDistinctEntityMap();
        if (distMap != null) {
            int entityId = -1;
            Object obj = null;
            if (entityType == ServiceNotifyEntityType.SERVICEGROUP) {
                obj = notify.getAttribute(EntityPublisher.NOTIFY_ATTR_SERVICEGROUP_ID);
            } else if (entityType == ServiceNotifyEntityType.CUSTOMGROUP) {
                obj = notify.getAttribute(EntityPublisher.NOTIFY_ATTR_CUSTOMGROUP_ID);
            }
            if (obj != null) {
                entityId = ((Integer) obj).intValue();
            } // end if
            StringBuffer sb = new StringBuffer();
            sb.append(notify.getAction());
            sb.append(":");
            sb.append(entityId);
            sb.append(";");
            String existingValue = null;
            if (distMap.get(entityType.getValue()) != null) {
                existingValue = distMap.get(entityType.getValue());
            } // end if
            String currentValue = sb.toString();
            StringBuilder builder = new StringBuilder();
            // If the value is already in the list, don't add a duplicate
            // one
            if (existingValue == null) {
                builder.append(currentValue);
            } else {
                if (existingValue.indexOf(currentValue) == -1) {
                    builder.append(existingValue);
                    builder.append(currentValue);
                } else {
                    builder.append(existingValue);
                } // end if
            }
            distMap.put(entityType.getValue(),
                    builder.toString());
        } // end if

    }

}

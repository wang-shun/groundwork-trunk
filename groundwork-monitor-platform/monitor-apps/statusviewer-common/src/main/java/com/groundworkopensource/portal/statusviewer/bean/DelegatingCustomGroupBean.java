/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2015  GroundWork Open Source Solutions info@groundworkopensource.com

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

package com.groundworkopensource.portal.statusviewer.bean;

import com.groundworkopensource.portal.common.ApplicationType;
import com.groundworkopensource.portal.common.PropertyUtils;
import com.groundworkopensource.portal.model.CustomGroup;

import javax.faces.model.SelectItem;
import java.util.List;

/**
 * DelegatingCustomGroupBean
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class DelegatingCustomGroupBean implements DelegateCustomGroupBean {

    private static final String DELEGATE_CLASS_PROP_NAME = "portal.custom.groups.bean.delegate";
    private static final String DELEGATE_CLASS_DEFAULT = "com.groundworkopensource.portal.statusviewer.bean.PortalCustomGroupBean";

    private DelegateCustomGroupBean delegateCustomGroupBean;

    public DelegatingCustomGroupBean() {
        // instantiate delegate class instance
        String delegateClassName = PropertyUtils.getProperty(ApplicationType.STATUS_VIEWER, DELEGATE_CLASS_PROP_NAME);
        if (delegateClassName == null) {
            delegateClassName = DELEGATE_CLASS_DEFAULT;
        }
        try {
            delegateCustomGroupBean = (DelegateCustomGroupBean) Class.forName(delegateClassName).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create delegate custom group bean: "+e, e);
        }
    }

    @Override
    public String cancel() {
        return delegateCustomGroupBean.cancel();
    }

    @Override
    public String deleteCustomGroup() {
        return delegateCustomGroupBean.deleteCustomGroup();
    }

    @Override
    public String editCustomGroup() {
        return delegateCustomGroupBean.editCustomGroup();
    }

    @Override
    public String publish() {
        return delegateCustomGroupBean.publish();
    }

    @Override
    public String save() {
        return delegateCustomGroupBean.save();
    }

    @Override
    public String updateAndPublish() {
        return delegateCustomGroupBean.updateAndPublish();
    }

    @Override
    public String updateAndSave() {
        return delegateCustomGroupBean.updateAndSave();
    }

    @Override
    public List<CustomGroup> getCustomGroups() {
        return delegateCustomGroupBean.getCustomGroups();
    }

    @Override
    public DualList getCustomGroupSelectItems() {
        return delegateCustomGroupBean.getCustomGroupSelectItems();
    }

    @Override
    public List<SelectItem> getEntityTypes() {
        return delegateCustomGroupBean.getEntityTypes();
    }

    @Override
    public DualList getHostGroups() {
        return delegateCustomGroupBean.getHostGroups();
    }

    @Override
    public String getMessage() {
        return delegateCustomGroupBean.getMessage();
    }

    @Override
    public DualList getServiceGroups() {
        return delegateCustomGroupBean.getServiceGroups();
    }
}

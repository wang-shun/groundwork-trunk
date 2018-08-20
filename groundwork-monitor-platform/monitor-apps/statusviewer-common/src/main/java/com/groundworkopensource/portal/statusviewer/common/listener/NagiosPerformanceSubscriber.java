/*
 * StatusViewer - The ultimate gwportal framework. Copyright (C) 2004-2009
 * GroundWork Open Source Solutions info@groundworkopensource.com
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */
package com.groundworkopensource.portal.statusviewer.common.listener;

import com.groundworkopensource.portal.common.CommonConstants;
import com.groundworkopensource.portal.statusviewer.bean.OnDemandServerPush;

/**
 * JMS Subscriber subscribes to the nagios_performance topic.
 * 
 * @author mridu_narang
 */
public abstract class NagiosPerformanceSubscriber extends OnDemandServerPush {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default Constructor
     */
    public NagiosPerformanceSubscriber() {
        super(CommonConstants.PROP_TOPIC_NAME);
    }

}

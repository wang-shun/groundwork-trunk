/*
 * Copyright (C) 2010 GroundWork Open Source, Inc. (GroundWork) All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.groundwork.dashboard.portlets;

import javax.portlet.PortletRequest;
import java.io.NotSerializableException;
import java.io.Serializable;

public class PortletMessaging {
    public PortletMessaging() {
    }

    public static final void publish(PortletRequest request, String portletTopic, String messageName, Object message) throws NotSerializableException {
        String key = portletTopic + ":" + messageName;
        if(message instanceof Serializable) {
            request.getPortletSession().setAttribute(key, message, 1);
        } else {
            throw new NotSerializableException("Message not serializable for " + key);
        }
    }

    public static final Object consume(PortletRequest request, String portletTopic, String messageName) {
        String key = portletTopic + ":" + messageName;
        Object object = request.getPortletSession().getAttribute(key, 1);
        request.getPortletSession().removeAttribute(key, 1);
        return object;
    }

    public static final Object receive(PortletRequest request, String portletTopic, String messageName) {
        String key = portletTopic + ":" + messageName;
        Object object = request.getPortletSession().getAttribute(key, 1);
        return object;
    }

    public static final void cancel(PortletRequest request, String portletTopic, String messageName) {
        String key = portletTopic + ":" + messageName;
        request.getPortletSession().removeAttribute(key, 1);
    }

    public static final void publish(PortletRequest request, String messageName, Object message) throws NotSerializableException {
        if(message instanceof Serializable) {
            request.getPortletSession().setAttribute(messageName, message, 2);
        } else {
            throw new NotSerializableException("Message not serializable for " + messageName);
        }
    }

    public static final Object consume(PortletRequest request, String messageName) {
        Object object = request.getPortletSession().getAttribute(messageName, 2);
        request.getPortletSession().removeAttribute(messageName, 2);
        return object;
    }

    public static final Object receive(PortletRequest request, String messageName) {
        Object object = request.getPortletSession().getAttribute(messageName, 2);
        return object;
    }

    public static final void cancel(PortletRequest request, String messageName) {
        request.getPortletSession().removeAttribute(messageName, 2);
    }
}

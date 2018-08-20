/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.groundwork.portal.organization;

import java.util.Map;

/** Helper POJO class to keep information about user and properties. */
public class ImportUser {
    private final String              userName;
    private final Map<String, String> properties;

    /**
     * @param  userName
     * @param  properties
     */
    public ImportUser(String userName, Map<String, String> properties) {
        if (userName == null) {
            throw new IllegalArgumentException("User name is null");
        }

        if (properties == null) {
            throw new IllegalArgumentException("Properties set is null");
        }

        this.userName   = userName;
        this.properties = properties;
    }

    /** @return */
    public String getUserName() {
        return userName;
    }

    /** @return */
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return "ImportUser[" + userName + "]";
    }
}

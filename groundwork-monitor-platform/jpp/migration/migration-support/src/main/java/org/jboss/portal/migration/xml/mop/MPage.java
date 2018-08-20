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
package org.jboss.portal.migration.xml.mop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MPage extends MSite {
    private List<MWindow>       windows;
    private Map<String, String> properties;
    private Map<String, String> displayNameIntl;

    public MPage(String name) {
        super(name);
        windows = new ArrayList<MWindow>();
    }

    public void addWindow(MWindow convertWindow) {
        windows.add(convertWindow);
    }

    public List<MWindow> getWindows() {
        return windows;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public Map<String, String> getDisplayNameIntl() {
        return displayNameIntl;
    }

    public void setDisplayNameIntl(Map<String, String> displayNameIntl) {
        this.displayNameIntl = displayNameIntl;
    }

}

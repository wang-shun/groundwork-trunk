/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

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

package org.groundwork.foundation.ws.api;

/**
 * @author glee
 *
 */
public interface GWService extends javax.xml.rpc.Service
{
    public void setEndpointAddress(String port, String endPoint) throws javax.xml.rpc.ServiceException;
    public java.rmi.Remote getService() throws javax.xml.rpc.ServiceException;
    public java.rmi.Remote getService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}

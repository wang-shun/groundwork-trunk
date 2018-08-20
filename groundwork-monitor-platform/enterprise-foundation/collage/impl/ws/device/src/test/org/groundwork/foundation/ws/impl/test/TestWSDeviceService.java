/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

 *	 This program is free software; you can redistribute it and/or modify
 *	 it under the terms of version 2 of the GNU General Public License
 *	 as published by the Free Software Foundation.

 *	 This program is distributed in the hope that it will be useful,
 *	 but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	 GNU General Public License for more details.

 *	 You should have received a copy of the GNU General Public License
 *	 along with this program; if not, write to the Free Software
 *	 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.groundwork.foundation.ws.impl.test;

import org.groundwork.foundation.ws.impl.DeviceSoapBindingStub;
import org.groundwork.foundation.ws.impl.WSDeviceServiceLocator;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;
import org.groundwork.foundation.ws.model.impl.DeviceQueryType;
import org.groundwork.foundation.ws.model.impl.SortCriteria;
import org.groundwork.foundation.ws.model.impl.WSFoundationException;

public class TestWSDeviceService extends junit.framework.TestCase {
    public TestWSDeviceService(java.lang.String name) {
        super(name);
    }

    public void testwsdeviceWSDL() throws Exception {
        javax.xml.rpc.ServiceFactory serviceFactory = javax.xml.rpc.ServiceFactory.newInstance();
        java.net.URL url = new java.net.URL(new WSDeviceServiceLocator().getdeviceAddress() + "?WSDL");
        javax.xml.rpc.Service service = serviceFactory.createService(url, new WSDeviceServiceLocator().getServiceName());
        assertTrue(service != null);
    }

    public void test1wsdeviceGetDevice() throws Exception {
        DeviceSoapBindingStub binding;
        try {
            binding = (DeviceSoapBindingStub)new WSDeviceServiceLocator().getdevice();
        }
        catch (javax.xml.rpc.ServiceException jre) {
            if(jre.getLinkedCause()!=null)
                jre.getLinkedCause().printStackTrace();
            throw new junit.framework.AssertionFailedError("JAX-RPC ServiceException caught: " + jre);
        }
        assertNotNull("binding is null", binding);

        // Time out after a minute
        binding.setTimeout(60000);

        // Test operation
        try {
            WSFoundationCollection value = null;
            value = binding.getDevice(DeviceQueryType.ALL, new java.lang.String(), 0, 0, new SortCriteria("ASC", ""));
        }
        catch (WSFoundationException e1) {
            throw new junit.framework.AssertionFailedError("WSFoundationException Exception caught: " + e1);
        }
            // TBD - validate results
    }
}

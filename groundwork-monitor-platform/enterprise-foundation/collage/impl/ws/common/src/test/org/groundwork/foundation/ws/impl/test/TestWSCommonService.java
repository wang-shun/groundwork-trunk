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

import org.groundwork.foundation.ws.impl.CommonSoapBindingStub;
import org.groundwork.foundation.ws.impl.WSCommonServiceLocator;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;
import org.groundwork.foundation.ws.model.impl.AttributeQueryType;
import org.groundwork.foundation.ws.model.impl.WSFoundationException;

public class TestWSCommonService extends junit.framework.TestCase {
    public TestWSCommonService(java.lang.String name) {
        super(name);
    }

    public void testwscommonWSDL() throws Exception {
        javax.xml.rpc.ServiceFactory serviceFactory = javax.xml.rpc.ServiceFactory.newInstance();
        java.net.URL url = new java.net.URL(new org.groundwork.foundation.ws.impl.WSCommonServiceLocator().getcommonAddress() + "?WSDL");
        javax.xml.rpc.Service service = serviceFactory.createService(url, new org.groundwork.foundation.ws.impl.WSCommonServiceLocator().getServiceName());
        assertTrue(service != null);
    }

    public void test1wscommonGetAttributeData() throws Exception {
       CommonSoapBindingStub binding;
        try {
            binding = (CommonSoapBindingStub)new WSCommonServiceLocator().getcommon();
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
            value = binding.getAttributeData(AttributeQueryType.APPLICATION_TYPES); 
        }
        catch (WSFoundationException e1) {
            throw new junit.framework.AssertionFailedError("WSFoundationException Exception caught: " + e1);
        }
            // TBD - validate results
    }
}

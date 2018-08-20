package org.groundwork.foundation.ws.impl;

import org.groundwork.foundation.ws.api.GWService;
import org.groundwork.foundation.ws.api.WSRRD;

public interface WSRRDService extends GWService {
	
	public java.lang.String getrrdAddress();

    public WSRRD getrrd() throws javax.xml.rpc.ServiceException;

    public WSRRD getrrd(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;

}

package org.groundwork.foundation.ws.impl;

import org.groundwork.foundation.ws.api.GWService;
import org.groundwork.foundation.ws.api.WSCategory;

public interface WSCategoryService extends GWService {
	
	public java.lang.String getwscategoryAddress();

    public WSCategory getwscategory() throws javax.xml.rpc.ServiceException;

    public WSCategory getwscategory(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;

}

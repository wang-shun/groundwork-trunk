package org.groundwork.foundation.ws.impl;

import java.rmi.RemoteException;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.ws.api.WSFoundationException;
import org.groundwork.foundation.ws.api.WSRRD;
import org.groundwork.foundation.ws.model.impl.RRDGraph;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

public class WSRRDImpl extends WebServiceImpl implements WSRRD {
	/* Enable logging */
	protected static Log log = LogFactory.getLog(WSRRDImpl.class);

	public WSFoundationCollection getGraph(String hostName, String serviceName,
			long startDate, long endDate, String applicationType,  int graphWidth)
			throws WSFoundationException, RemoteException {
		/* Needs to be overwritten by WS input */
		if (graphWidth <= 0)
		graphWidth = 620;

		Collection<RRDGraph> rrdCol = this.getRRDService().generateGraph(
				applicationType, hostName, serviceName, startDate, endDate, graphWidth);
		WSFoundationCollection foundCol = null;
		if (rrdCol != null) {
			RRDGraph[] array = (RRDGraph[]) rrdCol.toArray(new RRDGraph[rrdCol
					.size()]);
			foundCol = new WSFoundationCollection(rrdCol.size(), array);
		} // end if
		return foundCol;

	}

}

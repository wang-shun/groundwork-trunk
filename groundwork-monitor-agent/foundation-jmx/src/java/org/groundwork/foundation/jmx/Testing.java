package org.groundwork.foundation.jmx;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Testing {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		MessagePublisher messagePublisher;
//		String xmlMessage = "<Adapter Session=\"1\" AdapterType=\"SystemAdmin\">" + 
//		"<Command Action='ADD' AplicationType='JMX'>" +
//		"<Host Host='localhost' JMXPort='8004' User='root' Password='opensource' Description='localhost' Device='localhost' DisplayName='localhost' LastStateChange='2008-05-08 00:12:28' />" +
//		"</Command>" +
//		"</Adapter>";
//		
//		String xmlService = "<Adapter Session=\"2\" AdapterType=\"SystemAdmin\">" + 
//		"<Command Action='ADD' ApplicationType='JMX'>" +
//		"<Service Host='localhost' ServiceDescription='JMVCPU:comitted' CheckType='Active' MonitorStatus='PENDING' LastStateChange='2008-05-08 00:12:28' thresoldWarn='120' thresoldCritical='550' MBEAN='java.lang:type=Threading' Attribute='CurrentThreadCpuTime'/>" +
//		"<Service Host='localhost' ServiceDescription='JMVMemory:comitted' CheckType='Active' MonitorStatus='PENDING' LastStateChange='2008-05-08 00:12:28' thresoldWarn='120' thresoldCritical='550' MBEAN='java.lang:type=Memory' Attribute='HeapMemoryUsage' Key='used'/>" +
//		"</Command>" +
//		"</Adapter>";
//		String message = "";
//		XMLProcessing.addHosts(xmlMessage);
//		XMLProcessing.addServices(xmlService);
//		String fMessage = XMLProcessing.filterAndAddSyncToMessage(xmlMessage);
//		System.out.println("syncMessage:" + fMessage);
//		try{
//			JMXConnectionManager jcm = new JMXConnectionManager();
//			jcm.createConnections(XMLProcessing.listHosts);
//			QueryMBEAN qm = new QueryMBEAN();
//			ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
//			Future<String> f = threadExecutor.submit(qm);
//			
//			message = f.get();
//			threadExecutor.shutdown();
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//		
//		System.out.println(message);
	}

}

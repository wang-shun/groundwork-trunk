package org.groundwork.foundation.profiling;

import java.util.*;
import java.io.*;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.Category;
import org.apache.log4j.helpers.*;
import org.apache.log4j.PropertyConfigurator;
import java.text.SimpleDateFormat;

public class PassiveChecks {
	public static String filePath = "/usr/local/groundwork/nagios/etc/";
	//public static String templatePath = "/home/cpora/jdv/prc/logging/l1/t1/";
	//public static String log4jPath = "/home/cpora/j2ee/jettylogs/";
	//public static String log4jNSCAPath = "/home/cpora/eclipse2/workspace/profiler-java/profiler-console/src/webapp/";

	private static String templatePath = "";
	private static String log4jPath = "";
	private static String log4jNSCAPath = "";
	private static String latencyOutput = "";
	private static final String TEXTMESSAGE = "%1$s, HostName=%2$s, ServiceName=%3$s, Date (ms)=%4$s, Date=%5$s, Idx=%6$d";
	private static String NAME_FORMAT = "%1$s%2$d";
	private static final String MSG_TEXT_MESSAGE = "Profiler NSCALogMessage, H=%1$s, S=%2$s, WId=%3$d, B=%4$d, M=%5$d, LIDate=%6$s, MS=%7$s";
	private static SimpleDateFormat SQL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSSS");
    
	public static Logger log = Logger.getLogger(org.groundwork.foundation.profiling.PassiveChecks.class);
	private static final Category trace = Category.getInstance(PassiveChecks.class.getName());
	
	public static void setLog4jPath (String path)
	{
		log4jPath = path;
		log4jNSCAPath = path;
	}
	
	public static void setTemplatePath(String path)
	{
		templatePath = path;
		setLog4jPath (path);
	}
	/*
	public static void main(String args[]) throws IOException {

	    String path = args[6];

	    templatePath = path;
	    log4jPath = path;
	    log4jNSCAPath = path;
	    
		PropertyConfigurator.configure(log4jPath + "log4j.properties");
		log.debug("---------------passive checks   in----------------");
		log.debug("L====================================================================begin");
		log.debug(" ====                                                                 ====");
		log.debug(" ====                                                                 ====");
		log.debug(" ====                                                                 ====");
		log.debug(" ====                                                                 ====1");
		log.debug(" ====                                                                 ====2");
		// 1. copyDir ("/usr/local/groundwork/nagios/etc/" ,
		// "/usr/local/groundwork/nagios/etc/backup/");
		// 2. use hostsTemplate.cfg to append the new hosts and copy over
		// hosts.cfg
		// copyFile(new File("hostsTemplate.cfg"), new File("hosts.cfg"))
		// 3. use servicesTemplate.cfg to append the new services and copy over
		// services.cfg
		// copyFile(new File("servicesTemplate.cfg"), new File("services.cfg"))
		// 4. turn off/on nagios
		// executeCommand("/usr/local/groundwork/monarch/bin/nagios_reload")
		// 5.
		// 6. createConfiguration(numberOfHosts, numberOfServices, hostName,
		// ipAddress);
		int H = Integer.parseInt(args[0]);
		int S = Integer.parseInt(args[1]);
		String hostName = args[2];
		String ipAddress = args[3];
		String serviceName = args[4];
		String withConfig = args[5];
		String textMessage = args[6];
		String date1 = args[7];
		String date2 = args[8];
		log.debug("withConfig="+withConfig);
		log.debug("passive checks--1");
		if (withConfig.equals("y")) {
			log.debug("1");
			log.debug("passive checks--2");
			createConfigurations(H, S, hostName, ipAddress, serviceName);
			log.debug("2");
			log.debug("passive checks--3");
			executeCommand("/usr/local/groundwork/monarch/bin/nagios_reload");
			log.debug("3");
			log.debug("passive checks--4");
		}
		log.debug("4");
		log.debug("passive checks--5");

		log.debug("5");
		log.debug("passive checks--6");
		// PropertyConfigurator.configureAndWatch("log"+args[1]+".properties");

		log.debug("passive checks--7");
		long startTime = System.currentTimeMillis();
		log.debug("passive checks--8");
		PropertyConfigurator.configure(log4jNSCAPath+"log4jNSCA.properties");
		createMessages(H, S, hostName, serviceName, textMessage, date1, date2);
		PropertyConfigurator.configure(log4jPath+"log4j.properties");
		long endTime = System.currentTimeMillis();
		log.debug("passive checks--9");
		long latency = endTime - startTime;
		log.debug("passive checks--10");
		latencyOutput = latencyOutput + 
		"<table>"+
		"<tr><td>Start time:</td><td>"+SQL_DATE_FORMAT.format(new Date(startTime))+"</td></tr>"+
		"<tr><td>End time:</td><td>"+SQL_DATE_FORMAT.format(new Date(endTime))+"</td></tr>"+
		"</table>";
		log.debug("passive checks--11");
		
	}
	
	*/
	public static String mainCall(String args[], Object tg) throws IOException {
		if (log.isDebugEnabled()) log.debug("PassiveChecks.mainCall");
	    configureProperties("log4j.properties", tg);
		// 1. copyDir ("/usr/local/groundwork/nagios/etc/" ,
		// "/usr/local/groundwork/nagios/etc/backup/");
		// 2. use hostsTemplate.cfg to append the new hosts and copy over
		// hosts.cfg
		// copyFile(new File("hostsTemplate.cfg"), new File("hosts.cfg"))
		// 3. use servicesTemplate.cfg to append the new services and copy over
		// services.cfg
		// copyFile(new File("servicesTemplate.cfg"), new File("services.cfg"))
		// 4. turn off/on nagios
		// executeCommand("/usr/local/groundwork/monarch/bin/nagios_reload")
		// 5.
		// 6. createConfiguration(numberOfHosts, numberOfServices, hostName,
		// ipAddress);
		int H = Integer.parseInt(args[0]);
		int S = Integer.parseInt(args[1]);
		String hostName 	   = args[2];
		String ipAddress 	   = args[3];
		String serviceName 	   = args[4];
		String withConfig 	   = args[5];
		String textMessage 	   = args[6];
		String date1 		   = args[7];
		String date2 		   = args[8];
		if (withConfig.equals("y")) {
			createConfigurations(tg, H, S, hostName, ipAddress, serviceName);
			executeCommand("/usr/local/groundwork/monarch/bin/nagios_reload");
		}
		// PropertyConfigurator.configureAndWatch("log"+args[1]+".properties");
		long startTime = System.currentTimeMillis();
		configureProperties("log4jNSCA.properties", tg);
		createMessages(H, S, hostName, serviceName, textMessage, date1, SQL_DATE_FORMAT.format(new Date(startTime)));
		configureProperties("log4j.properties", tg);
		return getLatencyOutput(startTime);
	}
	
	public static String getLatencyOutput(long startTime)
	{
		long endTime = System.currentTimeMillis();
		long latency = endTime - startTime;
		latencyOutput = latencyOutput + 
		"<table>"+
		"<tr><td>Start time:</td><td>"+SQL_DATE_FORMAT.format(new Date(startTime))+"</td></tr>"+
		"<tr><td>End time:</td><td>"+SQL_DATE_FORMAT.format(new Date(endTime))+"</td></tr>"+
		"</table>";
		return latencyOutput;
	}
	
	public static void configureProperties(String fName, Object tg)
	{
	    InputStream propsFile = tg.getClass().
	    						   getClassLoader().
	    						   getResourceAsStream(fName);

        Properties tempProp = new Properties();
        try {
        tempProp.load(propsFile);

		PropertyConfigurator.configure(tempProp);
        } catch (IOException ioe) {
            log.error("configureProperties (fName, tg) I/O Exception.");
            log.debug("configureProperties (fName, tg) I/O Exception.");
            ioe.printStackTrace();
            System.exit(0);
        }
	}
	
	public static void configureProperties(String fName)
	{
		InputStream propsFile;
		/*
	    InputStream propsFile = tg.getClass().
	    						   getClassLoader().
	    						   getResourceAsStream(fName);
		*/
        Properties tempProp = new Properties();
        try {
        	propsFile = new FileInputStream (new File(log4jPath+"/"+fName));
        	tempProp.load(propsFile);

        	PropertyConfigurator.configure(tempProp);
        } catch (IOException ioe) {
            log.error(" (fName) I/O Exception.");
            log.debug(" (fName) I/O Exception.");
            ioe.printStackTrace();
            System.exit(0);
        }
	}
	
	
	
	public static void createMessages(int H, int S, String hostName,
			String serviceName, String txtmsg, String date1, String date2) {
		//Logger nscaLog = Logger.getLogger("root.nsca");
		if (log.isDebugEnabled()) log.debug("createMessages----begin");
		//=========================================
		MDC.put("nagios_canonical_hostname", hostName + "1");
		MDC.put("nagios_service_name", serviceName +  "1");
		String textMessage1 = String.format(TEXTMESSAGE, txtmsg,  hostName + "1", serviceName + "1", date1, date2, 1000);
		trace.info(textMessage1);
		MDC.remove("nagios_service_name");
		MDC.remove("nagios_canonical_hostname");
		//=========================================
		/*
		for (int i = 0; i < H; i++) {
			String l = new Integer(i).toString();
			MDC.put("nagios_canonical_hostname", hostName + l);
			
			for (int j = 0; j < S; j++) {
				String k = new Integer(j).toString();
				String textMessage1 = String.format(TEXTMESSAGE, txtmsg,  hostName + l, serviceName + "_" + k, date1, date2, 1000);
				String textMessage2 = String.format(TEXTMESSAGE, txtmsg,  hostName + l, serviceName + "_" + k, date1, date2, 1001);
				String textMessage3 = String.format(TEXTMESSAGE, txtmsg,  hostName + l, serviceName + "_" + k, date1, date2, 10011);
				String textMessage4 = String.format(TEXTMESSAGE, txtmsg,  hostName + l, serviceName + "_" + k, date1, date2, 1002);
				String textMessage5 = String.format(TEXTMESSAGE, txtmsg,  hostName + l, serviceName + "_" + k, date1, date2, 1003);
				MDC.put("nagios_service_name", serviceName + "_" + k);
				//log.debug(textMessage1);
				//trace.debug(textMessage1);
				log.debug(textMessage2);
				trace.info(textMessage2);
				//log.debug(textMessage3);
				//trace.info(textMessage3);
				log.debug(textMessage4);
				trace.error(textMessage4);
				//log.debug(textMessage5);
				//trace.fatal(textMessage5);

				
				 
				//trace.debug("1000 Debug On host " + hostName + l + " thise is service " + serviceName + "_" + k);
				//trace.info  ("1001 Info On host " + hostName + l + " thise is service " + serviceName + "_" + k);
				//trace.info("1001-1 Info On host " + hostName + l + " thise is service " + serviceName + "_" + k);
				//trace.error("1002 Error On host " + hostName + l + " thise is service " + serviceName + "_" + k);
				//trace.fatal("1003 Fatal On host " + hostName + l + " thise is service " + serviceName + "_" + k);
				 
				MDC.remove("nagios_service_name");
			}
			
			
			
			MDC.remove("nagios_canonical_hostname");
		} */
		if (log.isDebugEnabled()) log.debug("createMessages-----end");
	}


	public static void createMessages(String hostName, String serviceName, 
			String txtmsg, String msgType, Object tg) {
		//Logger nscaLog = Logger.getLogger("root.nsca");
		if (log.isDebugEnabled()) log.debug("createMessages----begin msgType [" + msgType+"]");
		//=========================================
		configureProperties("log4jNSCA.properties", tg);
		MDC.put("nagios_canonical_hostname", hostName);
		MDC.put("nagios_service_name", serviceName );
		if (msgType.equals("OK")) trace.info(txtmsg);
		if (msgType.equals("CRITICAL")) trace.error(txtmsg);
		if (msgType.equals("UNKNOWN")) trace.debug(txtmsg);
		if (msgType.equals("WARN")) trace.warn(txtmsg);
		MDC.remove("nagios_service_name");
		MDC.remove("nagios_canonical_hostname");
		configureProperties("log4j.properties", tg);
		//=========================================
		if (log.isDebugEnabled()) log.debug("createMessages-----end");
	}
	

	public static void sendNSCAServiceStatus(String hostName, String serviceName, 
			String txtmsg, String monitorStatus) {
		PropertyConfigurator.configure(log4jNSCAPath+"/log4jNSCA.properties");
		MDC.put("nagios_canonical_hostname", hostName);
		MDC.put("nagios_service_name", serviceName );
		if (monitorStatus.equals("OK")) trace.info(txtmsg);
		if (monitorStatus.equals("CRITICAL")) trace.error(txtmsg);
		if (monitorStatus.equals("UNKNOWN")) trace.debug(txtmsg);
		if (monitorStatus.equals("WARN")) trace.warn(txtmsg);
		MDC.remove("nagios_service_name");
		MDC.remove("nagios_canonical_hostname");
		PropertyConfigurator.configure(log4jPath+"/log4j.properties");
	}


/*
	public static void sendNSCAServiceStatus(String hostName, String serviceName, 
			String txtmsg, String monitorStatus) {
		PropertyConfigurator.configure(log4jNSCAPath+"/log4jNSCA.properties");
		MDC.put("nagios_canonical_hostname", hostName);
		MDC.put("nagios_service_name", serviceName );
		if (monitorStatus.equals("OK")) trace.info(txtmsg);
		if (monitorStatus.equals("CRITICAL")) trace.error(txtmsg);
		if (monitorStatus.equals("UNKNOWN")) trace.debug(txtmsg);
		if (monitorStatus.equals("WARN")) trace.warn(txtmsg);
		MDC.remove("nagios_service_name");
		MDC.remove("nagios_canonical_hostname");
		PropertyConfigurator.configure(log4jPath+"/log4j.properties");
	}
*/
	
	public static void sendNSCAServiceStatus(int H, int S, String hName, String sName,  int workloadId, int batchCount, int msgCount,  String lastInsertDate, String monitorStatus) {
		PropertyConfigurator.configure(log4jNSCAPath+"/log4jNSCA.properties");
		for (int j = 1; j <= H; j++)
		{
			String hostName = String.format(NAME_FORMAT,hName, j);
			for (int i = 1; i <= S; i++)
			{
				String serviceName = String.format(NAME_FORMAT,sName, i);;
				String txtmsg = buildTextMessage(hostName, serviceName, workloadId, batchCount, msgCount, lastInsertDate, monitorStatus);
				MDC.put("nagios_canonical_hostname", hostName);
				MDC.put("nagios_service_name", serviceName );
				if (monitorStatus.equals("OK")) trace.info(txtmsg);
				if (monitorStatus.equals("CRITICAL")) trace.error(txtmsg);
				if (monitorStatus.equals("UNKNOWN")) trace.debug(txtmsg);
				if (monitorStatus.equals("WARN")) trace.warn(txtmsg);
				MDC.remove("nagios_service_name");
				MDC.remove("nagios_canonical_hostname");
			}
		}
		PropertyConfigurator.configure(log4jPath+"/log4j.properties");
	}	
	

	public static void createConfigurations(Object tg, int H, int S, String hostName,
			String ipAddress, String serviceName) throws IOException {
		// 1. overwrite hosts.cfg with hostsTemplate.cfg
		// 2. overwrite services.cfg with servicesTemplate.cfg
		copyFile(tg, "hostsTemplate.cfg", filePath + "hosts.cfg");
		copyFile(tg, "servicesTemplate.cfg", filePath + "services.cfg");
		// 3. do the code below:
		try {
			String p = "";
			for (int i = 1; i <= H; i++) {
				String s = textFromFile(tg, "host_patterns")
						.replaceAll("#host.name#",
								hostName + new Integer(i).toString());
				s = s.replaceAll("#host.ipaddr#", ipAddress
						+ new Integer(i).toString());
				addMessage(s, filePath + "hosts.cfg");
				for (int j = 1; j <= S; j++) {
					s = textFromFile(tg, "service_patterns")
							.replaceAll("#host.name#",
									hostName + new Integer(i).toString());
					s = s.replaceAll("#service#", serviceName);
					s = s.replaceAll("#number#", new Integer(j).toString());
					addMessage(s, filePath + "services.cfg");
				}
				p = p + s;
			}

		} catch (Exception e) {
			log.error("Error: copy directory",e);
		}

	}

	
	public static void createConfigurations(int H, int S, String hostName,
			String ipAddress, String serviceName) throws IOException {
		// 1. overwrite hosts.cfg with hostsTemplate.cfg
		// 2. overwrite services.cfg with servicesTemplate.cfg
		copyFile(new File(templatePath + "/hostsTemplate.cfg"), new File(
				filePath + "hosts.cfg"));
		copyFile(new File(templatePath + "/servicesTemplate.cfg"), new File(
				filePath + "services.cfg"));
		// 3. do the code below:
		try {
			for (int i = 1; i <= H; i++) {
				String s = textFromFile(templatePath + "/host_patterns")
						.replaceAll("#host.name#",
								hostName + new Integer(i).toString());
				s = s.replaceAll("#host.ipaddr#", ipAddress
						+ new Integer(i).toString());
				addMessage(s, filePath + "hosts.cfg");
				for (int j = 1; j <= S; j++) {
					s = textFromFile(templatePath + "/service_patterns")
							.replaceAll("#host.name#",
									hostName + new Integer(i).toString());
					s = s.replaceAll("#service#", serviceName);
					s = s.replaceAll("#number#", new Integer(j).toString());
					addMessage(s, filePath + "services.cfg");
				}
			}

		} catch (Exception e) {
			System.err.println("Error: copy directory" + e);
		}

	}
	

	public static String textFromFile(Object tg, String fpath) {
		String s = " ";
		try {
			InputStream f = tg.getClass().getClassLoader().getResourceAsStream(fpath);
			int len = f.available();
			for (int i = 1; i <= len; i++)
				s = s + (char) f.read();
			f.close();
		} catch (Exception e) {
			log.debug("Error textFromFile(tg["+tg+"], fpath["+fpath+"]): " + e.getMessage());
		}
		return s;
	}
	

	public static String textFromFile(String fpath) {
		String s = " ";
		try {
			FileInputStream f = new FileInputStream(fpath);
			int len = f.available();
			for (int i = 1; i <= len; i++)
				s = s + (char) f.read();
			f.close();
		} catch (Exception e) {
			log.debug("Error textFromFile(fpath): " + e.getMessage());
		}
		return s;
	}

	public static void addMessage(String s, String fpath) {
		try {
			FileWriter fstream = new FileWriter(fpath, true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(s);
			out.close();
		} catch (Exception e) {
			log.debug("Error addMessage(s, fpath): " + e.getMessage());
		}
	}

	public static void copyFile(Object tg, String file1, String file2) {
		try {
			FileWriter fstream = new FileWriter(file2, false);
			BufferedWriter out = new BufferedWriter(fstream);
			log.debug("copyFile(tg, file1=["+file1+"], file2["+file2+"]----1 tg="+tg);
			out.write(textFromFile(tg, file1));
			log.debug("copyFile(tg, file1, file2----2");
			out.close();
		} catch (Exception e) {
			log.debug("Error copyFile(tg, file1, file2): " + e.getMessage());
		}
	}


	public static void copyFile(Object tg, String file1, String file2, String ngServer) {
		try {
			FileWriter fstream = new FileWriter(file2, false);
			BufferedWriter out = new BufferedWriter(fstream);
			log.debug("copyFile(tg, file1=["+file1+"], file2["+file2+"]----1 tg="+tg);
			out.write(textFromFile(tg, file1).replaceAll("#ngserver#", ngServer));
			log.debug("copyFile(tg, file1, file2----2");
			out.close();
		} catch (Exception e) {
			log.debug("Error copyFile(tg, file1, file2): " + e.getMessage());
		}
	}

	
	
	// Copies all files under srcDir to dstDir.
	// If dstDir does not exist, it will be created.
	public static void copyDirectory(File srcDir, File dstDir)
			throws IOException {
		if (srcDir.isDirectory()) {
			if (!dstDir.exists()) {
				dstDir.mkdir();
			}

			String[] children = srcDir.list();
			for (int i = 0; i < children.length; i++) {
				copyDirectory(new File(srcDir, children[i]), new File(dstDir,
						children[i]));
			}
		} else {
			// This method is implemented in e1071 Copying a File
			copyFile(srcDir, dstDir);
		}
	}

	// Copies src file to dst file.
	// If the dst file does not exist, it is created
	public static void copyFile(File src, File dst) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	public static boolean executeCommand(String command) {
		boolean success = true;
		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			BufferedReader in = new BufferedReader(new InputStreamReader(p
					.getInputStream()));

			int i = p.waitFor();
			if (i != 0)
				success = false;
		} catch (Exception err) {
			log.debug("PassiveChecks.executeCommand: "+err.getMessage());
			success = false;
		}
		return success;
	}
	
	private static String buildTextMessage(String hostName, String serviceName, int workloadId, int batchCount, int msgCount,  String lastInsertDate, String monitorStatus) {
		// MSG_TEXT_MESSAGE = "Profiler NSCAInit Message, Host=%1$s, Workload Id=%2$d, BatchCount=%3$d, MsgCount=%4$d";
		return String.format(MSG_TEXT_MESSAGE, hostName, serviceName, workloadId, batchCount, msgCount, lastInsertDate, monitorStatus);
	}
}

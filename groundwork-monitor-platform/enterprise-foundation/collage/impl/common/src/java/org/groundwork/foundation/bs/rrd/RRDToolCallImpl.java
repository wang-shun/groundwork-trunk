/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2009  GroundWork Open Source Solutions info@groundworkopensource.com

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
package org.groundwork.foundation.bs.rrd;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.ws.model.impl.RRDGraph;

/**
 * Class that calls the RRD tool and generates a graph. The thread is run in an
 * executor. If the thread doesn't return in a well specified time (property
 * rrdtool.thread.timeout) it will be interrupted.
 * 
 * @author rruttimann@gwos.com
 * 
 */

public class RRDToolCallImpl implements RRDToolCall {

	/* Local variables holding execution parameters */
	private String customCommand = null;
	private String rrdLabel = null;
	private String rrdPath = null;
	private String serviceDescription = null;

	private long endDate = System.currentTimeMillis() / 1000;
	private long startDate = endDate - 7200; /* 2 hours */
	private int graphWidth = 650; /* 650 pixels */

	/** Enable Logging **/
	protected static Log log = LogFactory.getLog(RRDToolCallImpl.class);

	private String rrdToolPath = null;

	private static final String SPACE = " ";

	public RRDToolCallImpl(String rrdToolPath, String rrdPath,
			String customCommand, String serviceDescription, String rrdLabel,
			long startDate, long endDate, int graphWidth) {
		if (rrdLabel != null)
			this.rrdLabel = rrdLabel;
		else
			this.rrdLabel = serviceDescription;
		this.rrdPath = rrdPath;
		this.serviceDescription = serviceDescription;
		this.customCommand = customCommand;
		this.rrdToolPath = rrdToolPath;
		/* If dates are 0 then use default */
		if (startDate != 0)
			this.startDate = startDate;
		if (endDate != 0)
			this.endDate = endDate;

		/* Graph width */
		this.graphWidth = graphWidth;
	}

	public void initialize(long startDate, long endDate) {
		/* If dates are 0 then use default */
		if (startDate != 0)
			this.startDate = startDate;
		if (endDate != 0)
			this.endDate = endDate;
	}

	/*
	 * IN the Foundation database the RRDCommand, The RRDLabel and the RRDPath
	 * are stored as dynamic properties for the service. With this information
	 * the rrdtool graph command can be called avoiding calling rrdtool info
	 * prior to invoking the graph command.
	 * 
	 * All what it needs to be done is appending start and end date to the RRD
	 * command if it is not already defined
	 */

	public RRDGraph call() throws Exception {
		DataInputStream is = null;

		/*
		 * Check if custom command was defined. If it was not defined it means
		 * that the graph should not be rendered but the object with the label
		 * should be returned
		 */

		if (this.customCommand == null) {
			return new RRDGraph(this.rrdLabel, new byte[0]);
		}

		Process childProcess = null;

		StringBuilder commandToExecute = new StringBuilder();

		String rrdCommandForDisplay = null;

		try {
			byte[] pngOut = null;

			Runtime runtime = Runtime.getRuntime();

			/*
			 * Build the command. 1) In some cases the full path to the RRDTool
			 * is defined so don't append it
			 */

			if (this.customCommand.indexOf(this.rrdToolPath) == -1) {
				// Append full path to RRD tool as defined in the properties
				commandToExecute.append(this.rrdToolPath).append("/");
			}

			/* Command includes already full path to RRDTool */
			commandToExecute.append(this.customCommand).append(SPACE);

			// Append start and end time if not defined in the command
			if ((commandToExecute.indexOf(RRD_START_TOKEN_SHORT) == -1)
					&& (commandToExecute.indexOf(RRD_START_TOKEN_LONG) == -1)) {
				commandToExecute.append(SPACE).append(RRD_START_TOKEN_LONG)
						.append(SPACE).append(startDate);
			}

			if ((commandToExecute.indexOf(RRD_END_TOKEN_SHORT) == -1)
					&& (commandToExecute.indexOf(RRD_END_TOKEN_LONG) == -1)) {
				commandToExecute.append(SPACE).append(RRD_END_TOKEN_LONG)
						.append(SPACE).append(endDate);
			}

			// Image format
			if ((commandToExecute.indexOf(RRD_IMAGE_FORMAT_SHORT) == -1)
					&& (commandToExecute.indexOf(RRD_IMAGE_FORMAT_LONG) == -1)) {
				commandToExecute.append(SPACE).append(RRD_IMAGE_FORMAT_LONG)
						.append(SPACE).append(RRD_IMAGE_FORMAT);
			}

			// Image width
			if (commandToExecute.indexOf(RRD_IMAGE_WIDTH) == -1) {
				commandToExecute.append(SPACE).append(RRD_IMAGE_WIDTH).append(
						SPACE).append(this.graphWidth);
			}

			/*
			 * Check if the command uses any quotes. If thats the case it needs
			 * special treatment since the Java runtime strips double quotes
			 * from the command line arguments.
			 * 
			 * The wrapper script (exec_rrdgraph.pl will take the command (that
			 * has to be one strings and therefore no spaces and replaces the
			 * &space; with a space and &quot; with a "
			 */

			StringBuilder command = new StringBuilder();
			int pos = commandToExecute.indexOf("\"");
			int start = 0;

			if (pos != -1) { // Substitute quotes
				while (pos != -1) {
					command.append(commandToExecute.substring(start, pos))
							.append("&quot;");
					start = ++pos;

					pos = commandToExecute.indexOf("\"", start);
				}
				// Append reminder of the command. If the String has no quotes
				// copy entire command
				command.append(commandToExecute.substring(start));

				/* re-initialize buffer */
				commandToExecute = new StringBuilder(
						"/usr/local/groundwork/common/bin/exec_rrdgraph.pl ");

				// Substitute spaces
				rrdCommandForDisplay = command.toString();

				pos = command.indexOf(" ");
				start = 0;
				while (pos != -1) {
					commandToExecute.append(command.substring(start, pos))
							.append("&space;");
					start = ++pos;

					pos = command.indexOf(" ", start);
				}
				// Append reminder of the command. If the String has no quotes
				// copy entire command
				commandToExecute.append(command.substring(start));

				if (log.isInfoEnabled())
					log.info("RRDCommand: " + commandToExecute.toString());
				childProcess = runtime.exec(commandToExecute.toString(), null);
			} else {
				// No substitutions -- just pass the argument to the command
				// line as it is
				rrdCommandForDisplay = commandToExecute.toString();
				if (log.isInfoEnabled())
					log.info("RRDCommand: " + rrdCommandForDisplay);
				childProcess = runtime.exec(commandToExecute.toString(), null);
			}

			// Read result while the child is still running (so it doesn't block trying to write).
			is = new DataInputStream(childProcess.getInputStream());
			pngOut = this.getByteArray(is);

			// Wait for the child to finish (it should essentially already be done, since it
			// doesn't do much after it writes the graph bits to its standard output stream).
			// This may be an issue if we block a long time.
			int exitCode = childProcess.waitFor();

			// Don't try to destroy() the child process in the finally{} block below, as that
			// would be possibly dangerous now that we know the process is already gone.  (It
			// might impolitely send a signal to some random other process.)  There is still a
			// window of vulnerability (race condition) here if this thread gets interrupted
			// between the childProcess.waitFor() above and this setting to null (GWMON-7418).
			// But we can at least close the window for the common case where the script is
			// known to terminate before we get interrupted.
			childProcess = null;

			// Return exit code and output from script
			return new RRDGraph(this.rrdLabel, pngOut);
		}

		catch (InterruptedException ie) {
			log.error("Error executing command - " + rrdCommandForDisplay
					+ " -  " + ie.toString());
			String host = "localhost";
			String logMessage = "<GENERICLOG ApplicationType='SYSTEM' MonitorServerName='localhost' Device='127.0.0.1' Severity='CRITICAL' MonitorStatus='CRITICAL' TextMessage='Following RRD command has been interrupted. Command: "
					+ rrdCommandForDisplay + "' />";
			postLogMessage(host, logMessage);

			return new RRDGraph(this.rrdLabel, new byte[0]);
		} catch (Exception e) {
			log.error("Error executing command - " + rrdCommandForDisplay
					+ " -  " + e.toString());
			String host = "localhost";
			String logMessage = "<GENERICLOG ApplicationType='SYSTEM' MonitorServerName='localhost' Device='127.0.0.1' Severity='CRITICAL' MonitorStatus='CRITICAL' TextMessage='Following RRD command has been interrupted. Command: "
					+ rrdCommandForDisplay + "' />";
			postLogMessage(host, logMessage);

			return new RRDGraph(this.rrdLabel, new byte[0]);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
				}
			}
			if (childProcess != null) {
				childProcess.destroy();
			}
		}
	}

	/**
	 * convert input stream to byte array
	 * 
	 * @param inputStream
	 * @return byte
	 * @throws IOException
	 */
	private byte[] getByteArray(InputStream inputStream) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		int iRead = 0;
		byte[] bytes = new byte[4096];
		try {
			while ((iRead = inputStream.read(bytes)) > 0) {
				byteArrayOutputStream.write(bytes, 0, iRead);
			}
		} catch (IOException e) {
			log.error("exception while getting byte array");
			throw new IOException("exception while getting byte array");
		}
		byte[] baResult = byteArrayOutputStream.toByteArray();
		return baResult;
	}

	/*
	 * Log message to foundation that indicates any problems with executing the
	 * rrd commands
	 */
	public void postLogMessage(String host, String logMessage) {
		// Create a socket with a timeout
		Socket socket = null;
		try {
			InetAddress addr = InetAddress.getByName(host);
			int port = 4913;
			SocketAddress sockaddr = new InetSocketAddress(addr, port);

			// Create an unbound socket
			socket = new Socket();

			// This method will block no more than timeoutMs.
			// If the timeout occurs, SocketTimeoutException is thrown.
			int timeoutMs = 2000; // 2 seconds
			socket.connect(sockaddr, timeoutMs);

			BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));
			wr.write(logMessage);
			wr.flush();
		} catch (UnknownHostException e) {
			log.error("Error: UnkownHost " + e.toString());
		} catch (SocketTimeoutException e) {
			log.error("Error: SocketTimeout " + e.toString());
		} catch (IOException e) {
			log.error("Error: IOException " + e.toString());
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (Exception exc) {
					log.error(exc.getMessage());
				} // end if
			}
		}
	}
}

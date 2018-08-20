/*$Id: $
*   
* All rights reserved. This program is free software; you can redistribute it
* and/or modify it under the terms of the GNU General Public License version 2
* as published by the Free Software Foundation.
*
* This program is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
* FOR A PARTICULAR PURPOSE.  See the GNU General Public License for 
* more details.
*
* You should have received a copy of the GNU General Public License along with
* this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
* Street, Fifth Floor, Boston, MA 02110-1301, USA.
*
*/


package org.groundwork.foundation.jmx;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

/*
 * @author Vong Tran (vong.tran@gmail.com)
 * 
 */
public class LogMessageWriter extends IncommingMessageListenerThread{
	
	private String msg;
//	private Log log = LogFactory.getLog(this.getClass());
	private Logger log = Logger.getLogger(this.getClass());
	
	public LogMessageWriter(String msg){
		this.msg = msg;
	}
	
	public void run(){
		String fileName = XMLProcessing.NAGIOS_LOG_FILE_PATH;
		File f = new File(fileName);
		try{
			if(!f.exists()){
				f.createNewFile();
			}
			FileWriter wrt = new FileWriter(f, true);
			wrt.write(msg);
			wrt.flush();
			wrt.close();
		}catch(IOException ioe){
			log.error("Could not write to log message " + ioe);
			System.out.println("Could not write to log message " + ioe);
		}
	}
	
	public void unInitialize(){
		
	}

}

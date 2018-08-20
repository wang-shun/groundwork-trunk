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

package com.groundwork.feeder.service;

/**
 * 
 * DataFeederService
 * 
 * @author <a href="mailto:rruttimann@itgroundwork.com"> Roger Ruttimann </a>
 * @version $Id: DataFeederService.java 6397 2007-04-02 21:27:40Z glee $
 * 
 * Service that listens for incoming feeder messages in XML format and
 * dispatches it to the corresponding collage feeder classes.
 */
public class DataFeederService {

    /**
     * Entry point for feeder service. The Service is configured to read a port
     * for incoming messages in XML format and forward them to the adapters.
     * 
     * Listener runs on a separate thread. Another thread distributes messages to
     * adapters. The threads are implemented in processFeederData class.
     * 
     * Message of the following format stops the service: <SERVICE-MAINTENANCE
     * command="stop">
     *  
     */
	
    
    public static void main(String[] args) {

       ProcessFeederData service = new ProcessFeederData(/*port default is 4913*/);

        // Start listening and processing thread
        service.startProcessing();

        // Keep the main thread alive, till the listener and process threads
        // exits.
        for (int i = 0; i < service.backgroundThreads.length; i++) {
            try {
                service.backgroundThreads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        // Clean shutdown
        service.unInitializeSystem();
    }
}
/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package com.groundworkopensource.webapp.console;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * @author manish_kjain
 * 
 */
public class GetMP3File extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = -3920426101546973899L;

    /**
     * logger.
     */
    private static Logger logger = Logger.getLogger(GetMP3File.class.getName());

    /**
     * (non-Javadoc)
     * 
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init() throws ServletException {
    }

    /**
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            String filePath = request.getParameter("filePath");
            // logger
            // .error("################### In doPost() of GetMP3File ################## : "
            // + filePath);
            // filePath = "/usr/local/groundwork/config/media/test-1.mp3";
            if (filePath != null
                    && !ConsoleConstants.EMPTY_STRING.equals(filePath)) {
                File file = new File(filePath);
                if (file.exists() && file.isFile()) {
                    FileInputStream inputStream = new FileInputStream(file);
                    byte[] bs = new byte[(int) file.length()];
                    inputStream.read(bs);
                    inputStream.close();
                    response.setContentType("audio/mpeg"); // supports only mp3
                    // files
                    response.setContentLength(bs.length);
                    ServletOutputStream outputStream = response
                            .getOutputStream();
                    outputStream.write(bs);
                    outputStream.flush();
                    outputStream.close();
                }
            }
        } catch (Exception e) {
            logger
                    .debug("Exception in getMP3File servlet while processing request. Actual exception : "
                            + e);
        }
    }

    /**
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        // logger
        // .error("################### In doGet() of GetMP3File ##################");
        doPost(request, response);
    }

    /**
     * (non-Javadoc)
     * 
     * @see javax.servlet.GenericServlet#destroy()
     */
    @Override
    public void destroy() {

    }

}

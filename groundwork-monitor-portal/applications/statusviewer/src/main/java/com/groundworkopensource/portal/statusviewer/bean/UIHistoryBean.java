/*
* 
* Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")  
* All rights reserved. This program is free software; you can redistribute it
* and/or modify it under the terms of the GNU General Public License version 2
* as published by the Free Software Foundation.
*
* This program is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
* FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
* more details.
*
* You should have received a copy of the GNU General Public License along with
* this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
* Street, Fifth Floor, Boston, MA 02110-1301, USA.
*
*/

package com.groundworkopensource.portal.statusviewer.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * This Class is used for storing UI settings per user, in static MAPs. 
 * 
 * @author nitin_jadhav
 *
 */
public class UIHistoryBean {
    private static final int DEFAULT_WIDTH = 235;
    /**
     * Map to store User name=Tree Portlet Width values
     */
    private static final Map<String, Integer> treePortletWidth;
    
    static{
        treePortletWidth=new HashMap<String, Integer>();
    }
    
    /**
     * Returns tree portlet width for provided user name
     * 
     * @param userName
     * @return width
     */
    public static int getTreePortletWidth(String userName){
        Integer width =treePortletWidth.get(userName);
        if(width==null){
            //RETURN DEFAULT WIDTH
            return DEFAULT_WIDTH;
        } else {
            return width;
        }
    }
    
    /**
     * Add width value to map
     * 
     * @param userName
     * @param width
     */
    public static void addTreePortletWidth(String userName, int width){
        treePortletWidth.put(userName, width);
    }
    
}

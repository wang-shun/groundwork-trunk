/*
 * Copyright 2009 GroundWork Open Source, Inc. ("GroundWork") All rights
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
package com.groundworkopensource.portal.migration;

/**
 * General exception for Guava migration issues.
 * 
 * @author Paul Burry
 * @version $Revision: 1792 $
 * @since GWMON 6.0
 */
public class GuavaMigrationException extends Exception
{
    public GuavaMigrationException(String message) {
        super(message);
    }
    
    public GuavaMigrationException(String message, Throwable cause) {
        super(message, cause);
    }
}

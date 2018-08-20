/**
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2006  GroundWork Open Source Solutions info@itgroundwork.com

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
package org.groundwork.foundation.profiling;

import java.sql.Connection;
import java.sql.DriverManager;

import org.groundwork.foundation.profiling.exceptions.ProfilerException;

/**
 * Simple database connection class used to store connection criteria and create JDBC connections.
 * @author glee
 *
 */
public class DBConnectionInfo
{
	// JDBC Driver class name to use
	private String _driver = null;
	
	// URL of database
	private String _url = null;
	
	// User name to use to login
	private String _login = null;
	
	// Password for database
	private String _password = null;
	
	private boolean _isDriverRegistered = false;
	
	public DBConnectionInfo (String driver, String url, String login, String password)
	{
		setDriver(driver);
		setURL(url);
		setLogin(login);
		setPassword(password);
	}	
	
	public String getDriver() 
	{
		return _driver;
	}
	
	public void setDriver(String driver)
	{
		if (driver == null || driver.length() == 0)
		{
			throw new IllegalArgumentException("Invalid null / empty db driver parameter.");
		}
		
		this._driver = driver;		
	}
	
	public String getLogin() {
		return _login;
	}
	
	public void setLogin(String login) 
	{
		if (login == null || login.length() == 0)
		{
			throw new IllegalArgumentException("Invalid null / empty db login parameter.");
		}
		
		this._login = login;
	}
	
	public String getPassword()
	{
		return _password;
	}
	
	public void setPassword(String password) 
	{
		this._password = password;
	}
	
	public String getURL() 
	{
		return _url;
	}
	
	public void setURL(String url)
	{
		if (url == null || url.length() == 0)
		{
			throw new IllegalArgumentException("Invalid null / empty db url parameter.");
		}
		
		this._url = url;
	}
	
	/*
	 * Extracts Server name from URL
	 * NOTE:  THIS IMPLEMENTATION SHOULD BE FIXED TO HANDLE EXCEPTIONAL CASES
	 */
	public String getServer()
	{
		if (_url == null || _url.length() == 0)
		{
			return null;
		}
		
		int firstPos  = _url.indexOf("//");
		int lastPos = _url.lastIndexOf("/");
		
		if (firstPos < 0 || lastPos < 0) {			
			return null;
		}
			
		return _url.substring(firstPos + 1, lastPos);
	}
	
	public Connection createConnection () throws ProfilerException
	{
		try {
			// Register driver, if necessary
			if (_isDriverRegistered == false) 
			{
				// Load and Register Driver - The driver will register itself
				Class.forName(_driver);
			}
				
			// TODO:  Pool Connections
			return DriverManager.getConnection(_url, _login, _password);
		}
		catch (Exception e)
		{
			throw new ProfilerException("Error occurred create profiler db connection -" + this, e);
		}
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder(32);
		sb.append("Driver: ");
		sb.append(_driver);
		sb.append(", ");
		
		sb.append("Driver: ");
		sb.append(_url);
		sb.append(", ");

		sb.append("Driver: ");
		sb.append(_login);
		sb.append(", ");

		sb.append("Driver Registered: ");
		sb.append(_isDriverRegistered);
		
		return sb.toString();
	}
}

/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2006, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.jboss.portal.test.core;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.jboss.portal.common.io.IOTools;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** @author <a href="theute@jboss.org">Thomas Heute </a> $Revision: 8786 $ */
public class Utils
{

   private static String url = "jdbc:mysql://localhost:3306/jbossportal?useServerPrepStmts=false";
   private static String username = "portal";
   private static String password = "portalpassword";

   public static IDataSet getDataSet(String file) throws Exception
   {
      return new FlatXmlDataSet(IOTools.safeBufferedWrapper(new FileInputStream(file)));
   }

   public static IDatabaseConnection getConnection() throws Exception
   {
      Class driverClass = Class.forName("org.gjt.mm.mysql.Driver");
      Connection jdbcConnection = DriverManager.getConnection(
         url, username, password);
      return new DatabaseConnection(jdbcConnection);
   }

   public static void resetAutoIncrement() throws SQLException
   {
      Connection jdbcConnection = DriverManager.getConnection(
         url, username, password);
      Statement stmt = jdbcConnection.createStatement();
      stmt.execute("alter table jbp_roles auto_increment=1");
   }
}

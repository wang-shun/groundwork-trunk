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
package org.jboss.portal.core.controller.command.mapper;

/**
 * A command mapper that delegates to other mappers based on the prefix of the request path.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public interface DelegatingCommandFactory extends CommandFactory
{
   /**
    * Register a command mapper with the specified path
    *
    * @param path    the path to associate with
    * @param factory the mapper
    * @throws IllegalArgumentException if any argument is null or another mapper is already registered with the path
    */
   void register(String path, CommandFactory factory) throws IllegalArgumentException;

   /**
    * Unregister a mapper for a given path.
    *
    * @param path the path
    * @throws IllegalArgumentException if the argument is null or no mapper is registered under that path
    */
   void unregister(String path) throws IllegalArgumentException;
}

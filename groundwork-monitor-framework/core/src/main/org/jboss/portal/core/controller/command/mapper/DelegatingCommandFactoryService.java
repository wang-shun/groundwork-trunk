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

import org.jboss.portal.core.controller.ControllerCommand;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.server.ServerInvocation;
import org.jboss.portal.server.servlet.PathMapping;
import org.jboss.portal.server.servlet.PathMappingResult;
import org.jboss.portal.server.servlet.PathParser;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class DelegatingCommandFactoryService extends AbstractCommandFactory implements DelegatingCommandFactory
{

   private PathParser parser;
   private CommandFactory nextFactory;
   private Map factories;
   private PathMapping mapping;

   public DelegatingCommandFactoryService()
   {
      factories = new HashMap();
      parser = new PathParser();
      mapping = new PathMapping()
      {
         public Object getRoot()
         {
            return this;
         }

         public Object getChild(Object parent, String name)
         {
            if (parent == this)
            {
               CommandFactory factory = (CommandFactory)factories.get(name);
               if (factory != null)
               {
                  return factory;
               }
               return factories.get("");
            }
            return null;
         }
      };
   }

   public CommandFactory getNextFactory()
   {
      return nextFactory;
   }

   public void setNextFactory(CommandFactory nextFactory)
   {
      this.nextFactory = nextFactory;
   }

   public void register(String path, CommandFactory factory)
   {
      if (path == null)
      {
         throw new IllegalArgumentException("no path");
      }
      if (factory == null)
      {
         throw new IllegalArgumentException("no mapper");
      }
      synchronized (this)
      {
         path = path.substring(1);
         if (factories.containsKey(path))
         {
            throw new IllegalArgumentException("path already registered");
         }
         Map copy = new HashMap(factories);
         copy.put(path, factory);
         factories = copy;
      }
   }

   public void unregister(String path)
   {
      if (path == null)
      {
         throw new IllegalArgumentException("no path");
      }
      synchronized (this)
      {
         path = path.substring(1);
         if (!factories.containsKey(path))
         {
            throw new IllegalArgumentException("path not registered");
         }
         Map copy = new HashMap(factories);
         copy.remove(path);
         factories = copy;
      }
   }

   public ControllerCommand doMapping(ControllerContext controllerContext, ServerInvocation invocation, String host, String contextPath, String requestPath)
   {
      //
      if (requestPath.length() == 0)
      {
         return null;
      }

      //
      PathMappingResult result = parser.map(mapping, requestPath);
      Object target = result.getTarget();
      ControllerCommand cmd = null;
      if (target instanceof CommandFactory)
      {
         CommandFactory delegate = (CommandFactory)target;
         String remainingPath = result.getRemainingPath();
         cmd = delegate.doMapping(controllerContext, invocation, host, contextPath + result.getMatchedPath(), remainingPath);
      }

      //
      if (cmd == null && nextFactory != null)
      {
         cmd = nextFactory.doMapping(controllerContext, invocation, host, contextPath, requestPath);
      }

      return cmd;
   }
}

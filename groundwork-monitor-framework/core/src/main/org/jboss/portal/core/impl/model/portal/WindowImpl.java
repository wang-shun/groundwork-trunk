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
package org.jboss.portal.core.impl.model.portal;

import org.jboss.portal.Mode;
import org.jboss.portal.WindowState;
import org.jboss.portal.core.model.content.Content;
import org.jboss.portal.core.model.content.ContentType;
import org.jboss.portal.core.model.content.spi.handler.ContentHandler;
import org.jboss.portal.core.model.content.spi.handler.ContentState;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.Window;
import org.jboss.portal.jems.hibernate.ContextObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11168 $
 */
public class WindowImpl extends PortalObjectImpl implements Window, ContextObject
{

   public static final String PORTAL_PROP_WINDOW_CONTENT_TYPE = "portal.windowContentType";

   public static final String PORTAL_INITIAL_WINDOW_STATE = "portal.windowInitialState";

   public static final String PORTAL_INITIAL_MODE = "portal.windowInitialMode";

   // Persistent state
   protected String uri;

   // Runtime fields
   protected AbstractPortalObjectContainer.ContainerContext containerContext;
   protected ContentType contentType;
   protected ContentStateImpl contentState;

   public WindowImpl()
   {
      super(true);

      //
      this.contentType = null;
      this.uri = null;
   }

   /**
    * Creates a new window.
    *
    * @param contentType the window content type
    * @throws IllegalArgumentException if the content type is null or the content URI is null
    */
   public WindowImpl(ContentType contentType, String contentURI) throws IllegalArgumentException
   {
      super(false);

      //
      if (contentType == null)
      {
         throw new IllegalArgumentException("No null content type accepted");
      }
      if (contentURI == null)
      {
         throw new IllegalArgumentException("No null content URI accepted");
      }

      //
      this.contentType = contentType;
      this.uri = contentURI;
   }

   private ContentStateImpl getContentState()
   {
      if (contentState == null)
      {
         contentState = new ContentStateImpl();
      }
      return contentState;
   }

   public void setContext(Object context)
   {
      this.containerContext = (AbstractPortalObjectContainer.ContainerContext)context;
   }

   public String getURI()
   {
      return uri;
   }

   public void setURI(String uri)
   {
      this.uri = uri;
   }

   public Page getPage()
   {
      return (Page)getParent();
   }

   public Content getContent()
   {
      return getContentState().getContent();
   }

   public ContentType getContentType()
   {
      if (contentType == null)
      {
         String value = getDeclaredProperty(PORTAL_PROP_WINDOW_CONTENT_TYPE);
         if (value == null)
         {
            // If nothing is provided then we use the default content type
            contentType = containerContext.getDefaultContentType();
         }
         else
         {
            contentType = ContentType.create(value);
         }
      }
      return contentType;
   }

   public int getType()
   {
      return PortalObject.TYPE_WINDOW;
   }

   protected PortalObjectImpl cloneObject()
   {
      WindowImpl clone = new WindowImpl();
      clone.setURI(uri);
      clone.setDeclaredPropertyMap(new HashMap(getDeclaredPropertyMap()));
      clone.setListener(getListener());
      clone.setDisplayName(getDisplayName());
      return clone;
   }

   protected void destroy()
   {
      // Destroy the associated content if it is necessary/possible
      getContentState().destroy();
   }

   /** Encapsulate content behavior for a window. */
   private class ContentStateImpl implements ContentState
   {

      /** . */
      private final String contextId = getId().toString();

      /** . */
      private Content content;

      public String getURI()
      {
         return WindowImpl.this.uri;
      }

      public void setURI(String uri)
      {
         WindowImpl.this.uri = uri;
      }

      private void destroy()
      {
         ContentHandler handler = getContentHandler();

         //
         if (handler != null)
         {
            handler.contentDestroyed(contextId, this);
         }
      }

      private Content getContent()
      {
         if (content == null)
         {
            ContentHandler handler = getContentHandler();

            //
            if (handler != null)
            {
               String contextId = getId().toString();
               content = handler.newContent(contextId, this);
            }
         }
         return content;
      }

      private ContentHandler getContentHandler()
      {
         ContentType contentType = getContentType();

         //
         return containerContext.getContentHandler(contentType);
      }

      public Iterator getParameterNames()
      {
         return new Iterator()
         {
            Iterator i = getDeclaredProperties().keySet().iterator();
            String next;

            {
               findNext();
            }

            public boolean hasNext()
            {
               return next != null;
            }

            public Object next()
            {
               String tmp = next;
               findNext();
               return tmp;
            }

            public void remove()
            {
               throw new UnsupportedOperationException();
            }

            void findNext()
            {
               next = null;
               while (i.hasNext() && next == null)
               {
                  String propertyName = (String)i.next();
                  if (propertyName.startsWith("content."))
                  {
                     next = propertyName.substring(8);
                  }
               }
            }
         };
      }

      public void setParameter(String name, String value) throws IllegalArgumentException
      {
         setDeclaredProperty("content." + name, value);
      }

      public void setParameters(Map<String, String> parameters) throws IllegalArgumentException
      {
         clearParameters();
         for (Entry<String, String> entry: parameters.entrySet())
         {
            setParameter(entry.getKey(), entry.getValue());
         }
      }

      public String getParameter(String name) throws IllegalArgumentException
      {
         return getDeclaredProperty("content." + name);
      }

      public void clearParameters()
      {
         for (Iterator<String> i = getDeclaredProperties().keySet().iterator(); i.hasNext();)
         {
            String propertyName = (String)i.next();
            if (propertyName.startsWith("content."))
            {
               i.remove();
            }
         }
      }
   }

   public WindowState getInitialWindowState()
   {
      String value = getDeclaredProperty(PORTAL_INITIAL_WINDOW_STATE);
      if (value != null)
      {
         return WindowState.create(value);
      }
      else
      {
         return WindowState.NORMAL;
      }
   }

   public Mode getInitialMode()
   {
      String value = getDeclaredProperty(PORTAL_INITIAL_MODE);
      if (value != null)
      {
         return Mode.create(value);
      }
      else
      {
         return Mode.VIEW;
      }
   }
}

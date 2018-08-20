/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2008, Red Hat Middleware, LLC, and individual                    *
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

package org.jboss.portal.core.metadata.portlet;

import org.jboss.portal.common.io.UndeclaredIOException;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

/**
 * A markup element.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 7228 $
 */
public class MarkupElement
{

   /** . */
   final String name;

   /** . */
   final String bodyContent;

   /**
    * If true and the body content is null then output the start tag and the end tag instead of an empty tag. The use
    * case is for the script element which when it is empty raise issues on IE, so it start element and end element have
    * to be used.
    */
   final boolean neverEmpty;

   /** . */
   final MarkupAttribute[] attributes;

   /**
    * @param name        the element name
    * @param bodyContent the optional body content
    * @param neverEmpty
    * @param attributes
    */
   public MarkupElement(String name, String bodyContent, boolean neverEmpty, MarkupAttribute[] attributes)
   {
      if (name == null)
      {
         throw new IllegalArgumentException("No null name accepted");
      }
      if (attributes == null)
      {
         throw new IllegalArgumentException("No null attributes accepted");
      }

      //
      Set<String> tmp = new HashSet<String>(attributes.length);
      for (MarkupAttribute attribute : attributes)
      {
         if (attribute == null)
         {
            throw new IllegalArgumentException("Cannot have a null attribute");
         }
         if (!tmp.add(attribute.getName()))
         {
            throw new IllegalArgumentException("Cannot have two identical attributes " + attribute.getName());
         }
      }

      this.name = name;
      this.bodyContent = bodyContent;
      this.neverEmpty = neverEmpty;
      this.attributes = attributes.clone();
   }

   public String getName()
   {
      return name;
   }

   public String getBodyContent()
   {
      return bodyContent;
   }

   public boolean isNeverEmpty()
   {
      return neverEmpty;
   }

   public int getAttributeSize()
   {
      return attributes.length;
   }

   public MarkupAttribute getAttribute(int index)
   {
      return attributes[index];
   }

   public MarkupAttribute getAttribute(String name)
   {
      for (MarkupAttribute attribute : attributes)
      {
         if (attribute.getName().equals(name))
         {
            return attribute;
         }
      }
      return null;
   }

   public void write(String urlPrefix, Writer writer) throws UndeclaredIOException
   {
      if (urlPrefix == null)
      {
         throw new IllegalArgumentException("No context path provided");
      }
      if (writer == null)
      {
         throw new IllegalArgumentException("No writer provided");
      }
      try
      {
         writer.write("<");
         writer.write(name);

         // attributes
         for (MarkupAttribute attribute : attributes)
         {
            writer.write(" ");
            attribute.write(urlPrefix, writer);
         }

         // content
         if (bodyContent != null && bodyContent.length() > 0)
         {
            writer.write(">");
            writer.write(bodyContent);
            writer.write("</");
            writer.write(name);
            writer.write(">\n");
         }
         else if (neverEmpty)
         {
            writer.write(">");
            writer.write("</");
            writer.write(name);
            writer.write(">\n");
         }
         else
         {
            writer.write("/>\n");
         }
      }
      catch (IOException e)
      {
         throw new UndeclaredIOException(e);
      }
   }

   public String toString(String contextPath)
   {
      StringWriter buffer = new StringWriter(128);
      write(contextPath, buffer);
      return buffer.toString();
   }

   @Override
   public String toString()
   {
      return toString("");
   }

   public Element toElement(String contextPath)
   {
      return new SimpleElement(contextPath);
   }

   private class SimpleElement implements Element
   {
      private String contextPath;

      private SimpleElement(String contextPath)
      {
         this.contextPath = contextPath;
      }

      @Override
      public String toString()
      {
         return MarkupElement.this.toString(contextPath);
      }

      public String getTagName()
      {
         return name;
      }

      public String getAttribute(String name)
      {
         return MarkupElement.this.getAttribute(name).getEncodedValue(contextPath);
      }

      public void setAttribute(String name, String value) throws DOMException
      {
         throw new UnsupportedOperationException();
      }

      public void removeAttribute(String name) throws DOMException
      {
         throw new UnsupportedOperationException();
      }

      public Attr getAttributeNode(String name)
      {
         throw new UnsupportedOperationException();
      }

      public Attr setAttributeNode(Attr newAttr) throws DOMException
      {
         throw new UnsupportedOperationException();
      }

      public Attr removeAttributeNode(Attr oldAttr) throws DOMException
      {
         throw new UnsupportedOperationException();
      }

      public NodeList getElementsByTagName(String name)
      {
         throw new UnsupportedOperationException();
      }

      public String getAttributeNS(String namespaceURI, String localName) throws DOMException
      {
         throw new UnsupportedOperationException();
      }

      public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException
      {
         throw new UnsupportedOperationException();
      }

      public void removeAttributeNS(String namespaceURI, String localName) throws DOMException
      {
         throw new UnsupportedOperationException();
      }

      public Attr getAttributeNodeNS(String namespaceURI, String localName) throws DOMException
      {
         throw new UnsupportedOperationException();
      }

      public Attr setAttributeNodeNS(Attr newAttr) throws DOMException
      {
         throw new UnsupportedOperationException();
      }

      public NodeList getElementsByTagNameNS(String namespaceURI, String localName) throws DOMException
      {
         throw new UnsupportedOperationException();
      }

      public boolean hasAttribute(String name)
      {
         throw new UnsupportedOperationException();
      }

      public boolean hasAttributeNS(String namespaceURI, String localName) throws DOMException
      {
         throw new UnsupportedOperationException();
      }

      public TypeInfo getSchemaTypeInfo()
      {
         throw new UnsupportedOperationException();
      }

      public void setIdAttribute(String name, boolean isId) throws DOMException
      {
         throw new UnsupportedOperationException();
      }

      public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException
      {
         throw new UnsupportedOperationException();
      }

      public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException
      {
         throw new UnsupportedOperationException();
      }

      public String getNodeName()
      {
         return getTagName();
      }

      public String getNodeValue() throws DOMException
      {
         return null;
      }

      public void setNodeValue(String nodeValue) throws DOMException
      {
         throw new UnsupportedOperationException();
      }

      public short getNodeType()
      {
         return ELEMENT_NODE;
      }

      public Node getParentNode()
      {
         throw new UnsupportedOperationException();
      }

      public NodeList getChildNodes()
      {
         throw new UnsupportedOperationException();
      }

      public Node getFirstChild()
      {
         throw new UnsupportedOperationException();
      }

      public Node getLastChild()
      {
         throw new UnsupportedOperationException();
      }

      public Node getPreviousSibling()
      {
         throw new UnsupportedOperationException();
      }

      public Node getNextSibling()
      {
         throw new UnsupportedOperationException();
      }

      public NamedNodeMap getAttributes()
      {
         throw new UnsupportedOperationException();
      }

      public Document getOwnerDocument()
      {
         throw new UnsupportedOperationException();
      }

      public Node insertBefore(Node newChild, Node refChild) throws DOMException
      {
         throw new UnsupportedOperationException();
      }

      public Node replaceChild(Node newChild, Node oldChild) throws DOMException
      {
         throw new UnsupportedOperationException();
      }

      public Node removeChild(Node oldChild) throws DOMException
      {
         throw new UnsupportedOperationException();
      }

      public Node appendChild(Node newChild) throws DOMException
      {
         throw new UnsupportedOperationException();
      }

      public boolean hasChildNodes()
      {
         throw new UnsupportedOperationException();
      }

      public Node cloneNode(boolean deep)
      {
         throw new UnsupportedOperationException();
      }

      public void normalize()
      {
         throw new UnsupportedOperationException();
      }

      public boolean isSupported(String feature, String version)
      {
         throw new UnsupportedOperationException();
      }

      public String getNamespaceURI()
      {
         throw new UnsupportedOperationException();
      }

      public String getPrefix()
      {
         throw new UnsupportedOperationException();
      }

      public void setPrefix(String prefix) throws DOMException
      {
         throw new UnsupportedOperationException();
      }

      public String getLocalName()
      {
         return getTagName();
      }

      public boolean hasAttributes()
      {
         return MarkupElement.this.getAttributeSize() != 0;
      }

      public String getBaseURI()
      {
         throw new UnsupportedOperationException();
      }

      public short compareDocumentPosition(Node other) throws DOMException
      {
         throw new UnsupportedOperationException();
      }

      public String getTextContent() throws DOMException
      {
         return MarkupElement.this.getBodyContent();
      }

      public void setTextContent(String textContent) throws DOMException
      {
         throw new UnsupportedOperationException();
      }

      public boolean isSameNode(Node other)
      {
         throw new UnsupportedOperationException();
      }

      public String lookupPrefix(String namespaceURI)
      {
         throw new UnsupportedOperationException();
      }

      public boolean isDefaultNamespace(String namespaceURI)
      {
         throw new UnsupportedOperationException();
      }

      public String lookupNamespaceURI(String prefix)
      {
         throw new UnsupportedOperationException();
      }

      public boolean isEqualNode(Node arg)
      {
         throw new UnsupportedOperationException();
      }

      public Object getFeature(String feature, String version)
      {
         throw new UnsupportedOperationException();
      }

      public Object setUserData(String key, Object data, UserDataHandler handler)
      {
         throw new UnsupportedOperationException();
      }

      public Object getUserData(String key)
      {
         throw new UnsupportedOperationException();
      }
   }
}

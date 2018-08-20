/*
* JBoss, a division of Red Hat
* Copyright 2008, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/

package org.jboss.portal.core.model.portal.metadata.coordination;

import org.w3c.dom.Element;
import org.jboss.portal.common.xml.XMLTools;

import javax.xml.namespace.QName;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class CoordinationWindowSimpleQNameMetaData extends CoordinationWindowMetaData
{
   private QName qname;

   private CoordinationWindowSimpleQNameMetaData(String windowName, QName qname)
   {
      super(windowName);
      this.qname = qname;
   }

   public QName getQname()
   {
      return qname;
   }

   public void setQname(QName qname)
   {
      this.qname = qname;
   }

   public static CoordinationWindowSimpleQNameMetaData buildMetaData(Element element)
   {
      Element name = XMLTools.getUniqueChild(element, "window-name", true);
      Element qname = XMLTools.getUniqueChild(element, "qname", true);

      return new CoordinationWindowSimpleQNameMetaData(XMLTools.asString(name), QName.valueOf(XMLTools.asString(qname)));
   }
}

/*
* JBoss, a division of Red Hat
* Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
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
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

/**
 * @author <a href="mailto:boleslaw dot dawidowicz at redhat anotherdot com">Boleslaw Dawidowicz</a>
 * @version : 0.1 $
 */
public class CoordinationAliasBindingMetaData
{
   private String name;

   private Set<QName> qnames = new HashSet<QName>();

   private CoordinationAliasBindingMetaData(String name)
   {
      this.name = name;
   }

   public Set<QName> getQnames()
   {
      return qnames;
   }

   public void setQnames(Set<QName> qnames)
   {
      this.qnames = qnames;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   private void addQName(String qname)
   {
      qnames.add(QName.valueOf(qname));
   }

   public static CoordinationAliasBindingMetaData buildMetaData(Element bindingElement)
   {
      Element nameElt = XMLTools.getUniqueChild(bindingElement, "id", true);
      CoordinationAliasBindingMetaData aliasMetaData = new CoordinationAliasBindingMetaData(XMLTools.asString(nameElt));

      Iterator qnameIter = XMLTools.getChildrenIterator(bindingElement, "qname");

      while (qnameIter.hasNext())
      {
         Element element = (Element)qnameIter.next();

         aliasMetaData.addQName(XMLTools.asString(element));
      }

      return aliasMetaData;
   }
}
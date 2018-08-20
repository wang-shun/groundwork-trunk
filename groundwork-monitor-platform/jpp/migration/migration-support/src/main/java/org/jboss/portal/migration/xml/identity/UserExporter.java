/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.portal.migration.xml.identity;

import org.codehaus.staxmate.SMOutputFactory;
import org.codehaus.staxmate.out.SMOutputDocument;
import org.codehaus.staxmate.out.SMOutputElement;

import javax.xml.stream.XMLOutputFactory;
import java.io.OutputStream;


/**
 * UserExporter exports MUSer objects into XML file using StaX API. Export process should be wrapped within
 * startExport() and endExport() method calls. Produced XML looks like this:
 * <users>
 *    <user name="x">
 *       <properties>
 *          <property name="z1" type="foo"/>
 *          <property name="z2"/>
 *       </properties>
 *    </user>
 * </users>
 *
 */
public class UserExporter
{

   private SMOutputDocument doc;

   private SMOutputElement users;

   /**
    *
    * @param os
    * @throws Exception
    */
   public void startExport(OutputStream os) throws Exception
   {
      SMOutputFactory outf = new SMOutputFactory(XMLOutputFactory.newInstance());
      doc = outf.createOutputDocument(os);
      doc.setIndentation("\n  ", 1, 1);

      doc.addComment(" generated: "+new java.util.Date().toString());

      // <users>
      users = doc.addElement("users");
   }

   /**
    *
    * @throws Exception
    */
   public void endExport() throws Exception
   {
      if (doc != null)
      {
         doc.closeRoot();
      }
   }

   /**
    * 
    * @param muser
    * @throws Exception
    */
   public void exportUser(MUser muser) throws Exception
   {
      if (users == null)
      {
         throw new IllegalStateException("Document root element not created");
      }

      // <user>
      SMOutputElement u = users.addElement("user");
      u.addAttribute("name", muser.getUserName());

      // <properties>
      SMOutputElement props = u.addElement("properties");

      for (MProperty mprop : muser.getProperties())
      {
         // <property>
         SMOutputElement p = props.addElement("property");
         p.addAttribute("name", mprop.getName());

         if (mprop.getType() != null)
         {
            p.addAttribute("type", mprop.getType());

         }

         p.addCData(mprop.getValue());



      }
   }


}

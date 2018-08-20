/*
 * Copyright (c) 2008, Your Corporation. All Rights Reserved.
 */
package org.jboss.portal.test.core;

import junit.framework.TestCase;
import org.jboss.portal.core.metadata.portlet.MarkupAttribute;
import org.jboss.portal.core.metadata.portlet.MarkupElement;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public class MarkupTestCase extends TestCase
{

   public void testAttributeConstructor()
   {
      try
      {
         new MarkupAttribute(null, "value", MarkupAttribute.Type.CDATA);
         fail("Was expecting an IAE");
      }
      catch (IllegalArgumentException expected)
      {
      }
      try
      {
         new MarkupAttribute("name", null, MarkupAttribute.Type.CDATA);
         fail("Was expecting an IAE");
      }
      catch (IllegalArgumentException expected)
      {
      }
      try
      {
         new MarkupAttribute("name", "value", null);
         fail("Was expecting an IAE");
      }
      catch (IllegalArgumentException expected)
      {
      }
      MarkupAttribute attribute = new MarkupAttribute("name", "value", MarkupAttribute.Type.CDATA);
      assertEquals("name", attribute.getName());
      assertEquals("value", attribute.getValue());
      assertEquals(MarkupAttribute.Type.CDATA, attribute.getType());
   }

   public void testElementContructor()
   {
      try
      {
         new MarkupElement(null, "body", false, new MarkupAttribute[0]);
         fail("Was expecting an IAE");
      }
      catch (IllegalArgumentException expected)
      {
      }
      try
      {
         new MarkupElement("name", "body", false, null);
         fail("Was expecting an IAE");
      }
      catch (IllegalArgumentException expected)
      {
      }
      try
      {
         new MarkupElement("name", "body", false, new MarkupAttribute[]{null});
         fail("Was expecting an IAE");
      }
      catch (IllegalArgumentException expected)
      {
      }
      MarkupElement element = new MarkupElement("elementname", "body", false, new MarkupAttribute[]{new MarkupAttribute("attributename", "attributevalue", MarkupAttribute.Type.CDATA)});
      assertEquals("elementname", element.getName());
      assertEquals("body", element.getBodyContent());
      assertEquals(false, element.isNeverEmpty());
      assertEquals(1, element.getAttributeSize());
      MarkupAttribute attribute = element.getAttribute(0);
      assertEquals("attributename", attribute.getName());
      assertEquals("attributevalue", attribute.getValue());
      assertEquals(MarkupAttribute.Type.CDATA, attribute.getType());
      attribute = element.getAttribute("attributename");
      assertEquals("attributename", attribute.getName());
      assertEquals("attributevalue", attribute.getValue());
      assertEquals(MarkupAttribute.Type.CDATA, attribute.getType());
      assertEquals(null, element.getAttribute("someotherattributename"));
   }

   public void testAttributeURIEncoding()
   {
      MarkupAttribute attribute = new MarkupAttribute("name", "/-_.*ABCabc012# ", MarkupAttribute.Type.URI);
      assertEquals("/-_.*ABCabc012%23+", attribute.getEncodedValue());
   }

   public void testToString()
   {
      MarkupElement element = new MarkupElement("elementname", "body", false,
         new MarkupAttribute[]{new MarkupAttribute("attributename", "attributevalue", MarkupAttribute.Type.CDATA)});

      assertEquals("<elementname attributename=\"attributevalue\">body</elementname>", element.toString().trim());

      element = new MarkupElement("elementname", null, false,
         new MarkupAttribute[]{new MarkupAttribute("attributename", "attributevalue", MarkupAttribute.Type.CDATA)});

      assertEquals("<elementname attributename=\"attributevalue\"/>", element.toString().trim());
   }
}

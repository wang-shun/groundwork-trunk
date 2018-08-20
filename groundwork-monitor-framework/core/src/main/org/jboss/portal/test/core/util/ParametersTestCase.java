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
package org.jboss.portal.test.core.util;

import junit.framework.TestCase;
import org.jboss.portlet.util.Parameters;

import java.util.HashMap;

/** @author <a href="theute@jboss.org">Thomas Heute </a> $Revision: 8786 $ */
public class ParametersTestCase
   extends TestCase
{

   private Parameters params;

   String booltrue01 = "true";

   String booltrue02 = "trUe";

   String booltrue03 = "TRUE";

   String boolfalse01 = "false";

   String boolfalse02 = "FALse";

   String boolfalse03 = "FALSE";

   String int01 = "10";

   String int02 = " 10";

   String int03 = " 10 ";

   String float01 = "3.14";

   String float02 = " 3.14";

   String float03 = " 3.14 ";

   String float04 = "3";

   String double01 = "" + Math.PI;

   String double02 = " " + Math.PI;

   String double03 = "3.14";

   String double04 = "3";

   String string01 = "foo";

   String string02 = " bar";

   String stringEmpty = "";

   String stringNull = null;

   String long01 = "" + (Integer.MAX_VALUE + 10);

   public void setUp()
   {
      HashMap map = new HashMap();
      map.put("booltrue01", new String[]{booltrue01});
      map.put("booltrue02", new String[]{booltrue02});
      map.put("booltrue03", new String[]{booltrue03});
      map.put("boolfalse01", new String[]{boolfalse01});
      map.put("boolfalse02", new String[]{boolfalse02});
      map.put("boolfalse03", new String[]{boolfalse03});
      map.put("int01", new String[]{int01});
      map.put("int02", new String[]{int02});
      map.put("int03", new String[]{int03});
      map.put("float01", new String[]{float01});
      map.put("float02", new String[]{float02});
      map.put("float03", new String[]{float03});
      map.put("float04", new String[]{float04});
      map.put("double01", new String[]{double01});
      map.put("double02", new String[]{double02});
      map.put("double03", new String[]{double03});
      map.put("double04", new String[]{double04});
      map.put("string01", new String[]{string01});
      map.put("string02", new String[]{string02});
      map.put("long01", new String[]{long01});
      map.put("stringEmpty", new String[]{stringEmpty});
      map.put("stringNull", new String[]{stringNull});

      params = new Parameters(map);
   }

   public void testGet01()
   {
      String string = params.get("string01", "toto");
      assertEquals(string01, string);
   }

   public void testGet02()
   {
      String string = params.get("string02", "toto");
      assertEquals(string02, string);
   }

   // Null value
   public void testGet03()
   {
      String string = params.get("StringNull", "toto");
      assertEquals("toto", string);
   }

   public void testGet04()
   {
      String string = params.get("stringEmpty", "toto");
      assertEquals(stringEmpty, string);
   }

   // Null key
   public void testGet05()
   {
      String string = params.get(null, "toto");
      assertEquals("toto", string);
   }

   public void testGetBoolean01()
   {
      boolean bool = params.getBoolean("booltrue01", false);
      assertEquals(true, bool);
   }

   public void testGetBoolean02()
   {
      boolean bool = params.getBoolean("booltrue02", false);
      assertEquals(true, bool);
   }

   public void testGetBoolean03()
   {
      boolean bool = params.getBoolean("booltrue03", false);
      assertEquals(true, bool);
   }

   public void testGetBoolean04()
   {
      boolean bool = params.getBoolean("boolfalse01", true);
      assertEquals(false, bool);
   }

   public void testGetBoolean05()
   {
      boolean bool = params.getBoolean("boolfalse02", true);
      assertEquals(false, bool);
   }

   public void testGetBoolean06()
   {
      boolean bool = params.getBoolean("boolfalse03", true);
      assertEquals(false, bool);
   }

   public void testGetBoolean07()
   {
      boolean bool = params.getBoolean(null, true);
      assertEquals(true, bool);
   }

   public void testGetBoolean08()
   {
      boolean bool = params.getBoolean(null, false);
      assertEquals(false, bool);
   }

   public void testGetBoolean09()
   {
      boolean bool = params.getBoolean("stringEmpty", true);
      assertEquals(true, bool);
   }

   public void testGetBoolean10()
   {
      boolean bool = params.getBoolean("stringEmpty", false);
      assertEquals(false, bool);
   }

   public void testGetBoolean11()
   {
      boolean bool = params.getBoolean("stringNull", true);
      assertEquals(true, bool);
   }

   public void testGetBoolean12()
   {
      boolean bool = params.getBoolean("stringNull", false);
      assertEquals(false, bool);
   }

   public void testGetBooleanObject01()
   {
      Boolean bool = params.getBooleanObject("booltrue01", false);
      assertEquals(Boolean.TRUE, bool);
   }

   public void testGetBooleanObject02()
   {
      Boolean bool = params.getBooleanObject("booltrue02", false);
      assertEquals(Boolean.TRUE, bool);
   }

   public void testGetBooleanObject03()
   {
      Boolean bool = params.getBooleanObject("booltrue03", false);
      assertEquals(Boolean.TRUE, bool);
   }

   public void testGetBooleanObject04()
   {
      Boolean bool = params.getBooleanObject("boolfalse01", true);
      assertEquals(Boolean.FALSE, bool);
   }

   public void testGetBooleanObject05()
   {
      Boolean bool = params.getBooleanObject("boolfalse02", true);
      assertEquals(Boolean.FALSE, bool);
   }

   public void testGetBooleanObject06()
   {
      Boolean bool = params.getBooleanObject("boolfalse03", true);
      assertEquals(Boolean.FALSE, bool);
   }

   public void testGetBooleanObject07()
   {
      Boolean bool = params.getBooleanObject(null, true);
      assertEquals(Boolean.TRUE, bool);
   }

   public void testGetBooleanObject08()
   {
      Boolean bool = params.getBooleanObject(null, false);
      assertEquals(Boolean.FALSE, bool);
   }

   public void testGetBooleanObject09()
   {
      Boolean bool = params.getBooleanObject("stringEmpty", true);
      assertEquals(Boolean.TRUE, bool);
   }

   public void testGetBooleanObject10()
   {
      Boolean bool = params.getBooleanObject("stringEmpty", false);
      assertEquals(Boolean.FALSE, bool);
   }

   public void testGetBooleanObject11()
   {
      Boolean bool = params.getBooleanObject("stringNull", true);
      assertEquals(Boolean.TRUE, bool);
   }

   public void testGetBooleanObject12()
   {
      Boolean bool = params.getBooleanObject("stringNull", false);
      assertEquals(Boolean.FALSE, bool);
   }

   public void testGetByteArray01()
   {
      byte[] bytes = params.getByteArray("string01", "toto".getBytes());
      assertEquals(string01, new String(bytes));
   }

   public void testGetByteArray02()
   {
      byte[] bytes = params.getByteArray("string02", "toto".getBytes());
      assertEquals(string02, new String(bytes));
   }

   public void testGetByteArray03()
   {
      byte[] bytes = params.getByteArray(null, "toto".getBytes());
      assertEquals("toto", new String(bytes));
   }

   public void testGetByteArray04()
   {
      byte[] bytes = params.getByteArray(stringEmpty, "toto".getBytes());
      assertEquals("toto", new String(bytes));
   }

   public void testGetByteArray05()
   {
      byte[] bytes = params.getByteArray(stringNull, "toto".getBytes());
      assertEquals("toto", new String(bytes));
   }

   public void testGetDouble01()
   {
      double value = params.getDouble("double01", 2.2);
      assertEquals(Math.PI, value, 0);
   }

   public void testGetDouble02()
   {
      double value = params.getDouble("double02", 2.2);
      assertEquals(Math.PI, value, 0);
   }

   public void testGetDouble03()
   {
      double value = params.getDouble("double03", 2.2);
      assertEquals(3.14, value, 0);
   }

   public void testGetDouble04()
   {
      double value = params.getDouble("double04", 2.2);
      assertEquals(3, value, 0);
   }

   public void testGetDouble05()
   {
      double value = params.getDouble(null, 2.2);
      assertEquals(2.2, value, 0);
   }

   public void testGetDouble06()
   {
      double value = params.getDouble("stringEmpty", 2.2);
      assertEquals(2.2, value, 0);
   }

   public void testGetDouble07()
   {
      double value = params.getDouble("stringNull", 2.2);
      assertEquals(2.2, value, 0);
   }

   public void testGetDoubleObject01()
   {
      Double value = params.getDoubleObject("double01", 2.2);
      assertEquals(new Double(Math.PI), value);
   }

   public void testGetDoubleObject02()
   {
      Double value = params.getDoubleObject("double02", 2.2);
      assertEquals(new Double(Math.PI), value);
   }

   public void testGetDoubleObject03()
   {
      Double value = params.getDoubleObject("double03", 2.2);
      assertEquals(new Double(3.14), value);
   }

   public void testGetDoubleObject04()
   {
      Double value = params.getDoubleObject("double04", 2.2);
      assertEquals(new Double(3), value);
   }

   public void testGetDoubleObject05()
   {
      Double value = params.getDoubleObject(null, 2.2);
      assertEquals(new Double(2.2), value);
   }

   public void testGetDoubleObject06()
   {
      Double value = params.getDoubleObject("stringEmpty", 2.2);
      assertEquals(new Double(2.2), value);
   }

   public void testGetDoubleObject07()
   {
      Double value = params.getDoubleObject("stringNull", 2.2);
      assertEquals(new Double(2.2), value);
   }

   public void testGetFloat01()
   {
      float value = params.getFloat("float01", 2.2f);
      assertEquals(3.14f, value, 0);
   }

   public void testGetFloat02()
   {
      float value = params.getFloat("float02", 2.2f);
      assertEquals(3.14f, value, 0);
   }

   public void testGetFloat03()
   {
      float value = params.getFloat("float03", 2.2f);
      assertEquals(3.14f, value, 0);
   }

   public void testGetFloat04()
   {
      float value = params.getFloat("float04", 2.2f);
      assertEquals(3f, value, 0);
   }

   public void testGetFloat05()
   {
      float value = params.getFloat("double01", 2.2f);
      assertEquals(3.1415927f, value, 0);
   }

   public void testGetFloat06()
   {
      float value = params.getFloat(null, 2.2f);
      assertEquals(2.2f, value, 0);
   }

   public void testGetFloat07()
   {
      float value = params.getFloat(stringEmpty, 2.2f);
      assertEquals(2.2f, value, 0);
   }

   public void testGetFloat08()
   {
      float value = params.getFloat(stringNull, 2.2f);
      assertEquals(2.2f, value, 0);
   }

   public void testGetFloatObject01()
   {
      Float value = params.getFloatObject("float01", 2.2f);
      assertEquals(new Float(3.14f), value);
   }

   public void testGetFloatObject02()
   {
      Float value = params.getFloatObject("float02", 2.2f);
      assertEquals(new Float(3.14f), value);
   }

   public void testGetFloatObject03()
   {
      Float value = params.getFloatObject("float03", 2.2f);
      assertEquals(new Float(3.14f), value);
   }

   public void testGetFloatObject04()
   {
      Float value = params.getFloatObject("float04", 2.2f);
      assertEquals(new Float(3f), value);
   }

   public void testGetFloatObject05()
   {
      Float value = params.getFloatObject("double01", 2.2f);
      assertEquals(new Float(3.1415927f), value);
   }

   public void testGetFloatObject06()
   {
      Float value = params.getFloatObject(null, 2.2f);
      assertEquals(new Float(2.2f), value);
   }

   public void testGetFloatObject07()
   {
      Float value = params.getFloatObject(stringEmpty, 2.2f);
      assertEquals(new Float(2.2f), value);
   }

   public void testGetFloatObject08()
   {
      Float value = params.getFloatObject(stringNull, 2.2f);
      assertEquals(new Float(2.2f), value);
   }

   public void testGetInt01()
   {
      int value = params.getInt("int01", 9);
      assertEquals(10, value);
   }

   public void testGetInt02()
   {
      int value = params.getInt("int01", 9);
      assertEquals(10, value);
   }

   public void testGetInt03()
   {
      int value = params.getInt("int01", 9);
      assertEquals(10, value);
   }

   public void testGetInt04()
   {
      int value = params.getInt("float01", 9);
      assertEquals(9, value);
   }

   public void testGetInt05()
   {
      int value = params.getInt(null, 9);
      assertEquals(9, value);
   }

   public void testGetInt06()
   {
      int value = params.getInt("stringEmpty", 9);
      assertEquals(9, value);
   }

   public void testGetInt07()
   {
      int value = params.getInt("stringNull", 9);
      assertEquals(9, value);
   }

   public void testGetIntObject01()
   {
      Integer value = params.getIntObject("int01", 9);
      assertEquals(new Integer(10), value);
   }

   public void testGetIntObject02()
   {
      Integer value = params.getIntObject("int01", 9);
      assertEquals(new Integer(10), value);
   }

   public void testGetIntObject03()
   {
      Integer value = params.getIntObject("int01", 9);
      assertEquals(new Integer(10), value);
   }

   public void testGetIntObject04()
   {
      Integer value = params.getIntObject("float01", 9);
      assertEquals(new Integer(9), value);
   }

   public void testGetIntObject05()
   {
      Integer value = params.getIntObject(null, 9);
      assertEquals(new Integer(9), value);
   }

   public void testGetIntObject06()
   {
      Integer value = params.getIntObject("stringEmpty", 9);
      assertEquals(new Integer(9), value);
   }

   public void testGetIntObject07()
   {
      Integer value = params.getIntObject("stringNull", 9);
      assertEquals(new Integer(9), value);
   }

   public void testGetShort01()
   {
      short value = params.getShort("int01", (short)9);
      assertEquals(10, value);
   }

   public void testGetShort02()
   {
      short value = params.getShort("int01", (short)9);
      assertEquals(10, value);
   }

   public void testGetShort03()
   {
      short value = params.getShort("int01", (short)9);
      assertEquals(10, value);
   }

   public void testGetShort04()
   {
      short value = params.getShort("float01", (short)9);
      assertEquals(9, value);
   }

   public void testGetShort05()
   {
      short value = params.getShort(null, (short)9);
      assertEquals(9, value);
   }

   public void testGetShort06()
   {
      short value = params.getShort("stringEmpty", (short)9);
      assertEquals(9, value);
   }

   public void testGetShort07()
   {
      short value = params.getShort("stringNull", (short)9);
      assertEquals(9, value);
   }

   public void testGetShortObject01()
   {
      Short value = params.getShortObject("int01", (short)9);
      assertEquals(new Short((short)10), value);
   }

   public void testGetShortObject02()
   {
      Short value = params.getShortObject("int01", (short)9);
      assertEquals(new Short((short)10), value);
   }

   public void testGetShortObject03()
   {
      Short value = params.getShortObject("int01", (short)9);
      assertEquals(new Short((short)10), value);
   }

   public void testGetShortObject04()
   {
      Short value = params.getShortObject("float01", (short)9);
      assertEquals(new Short((short)9), value);
   }

   public void testGetShortObject05()
   {
      Short value = params.getShortObject(null, (short)9);
      assertEquals(new Short((short)9), value);
   }

   public void testGetShortObject06()
   {
      Short value = params.getShortObject("stringEmpty", (short)9);
      assertEquals(new Short((short)9), value);
   }

   public void testGetShortObject07()
   {
      Short value = params.getShortObject("stringNull", (short)9);
      assertEquals(new Short((short)9), value);
   }

   public void testGetLong01()
   {
      long value = params.getLong("long01", 9);
      assertEquals((new Long(Integer.MAX_VALUE + 10)).longValue(), value);
   }

   public void testGetLong02()
   {
      long value = params.getLong(null, 9);
      assertEquals(9, value);
   }

   public void testGetLong03()
   {
      long value = params.getLong("stringEmpty", 9);
      assertEquals(9, value);
   }

   public void testGetLong04()
   {
      long value = params.getLong("stringNull", 9);
      assertEquals(9, value);
   }

   public void testGetLongObject01()
   {
      Long value = params.getLongObject("long01", 9);
      assertEquals((new Long(Integer.MAX_VALUE + 10)), value);
   }

   public void testGetLongObject02()
   {
      Long value = params.getLongObject(null, 9);
      assertEquals(new Long(9), value);
   }

   public void testGetLongObject03()
   {
      Long value = params.getLongObject("stringEmpty", 9);
      assertEquals(new Long(9), value);
   }

   public void testGetLongObject04()
   {
      Long value = params.getLongObject("stringNull", 9);
      assertEquals(new Long(9), value);
   }
}


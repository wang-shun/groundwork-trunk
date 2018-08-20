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

import junit.framework.TestCase;
//import org.jboss.portlet.JBossPortletPreferences;

import javax.portlet.ReadOnlyException;
import java.util.Arrays;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 10230 $
 */
public class CorePortletPreferencesTestCase extends TestCase
{

   public CorePortletPreferencesTestCase(String key)
   {
      super(key);
   }

//   private JBossPortletPreferences prefs;
   private TestProxy proxy;

   protected void setUp() throws Exception
   {
//      Class proxyClass = Proxy.getProxyClass(Thread.currentThread().getContextClassLoader(), new Class[]{TestProxy.class});
//      ProxyInfo info = new ProxyInfo(proxyClass);
//      PreferenceStore store = new AbstractPreferenceStore();
//      PreferenceSet set = store.get(new FQN("ns"));
//      prefs = new JBossPortletPreferences(
//            new PreferenceSet[]{set},
//            null,
//            PortletPreferencesImpl.ACTION,
//            info
//      );
//      proxy = (TestProxy)prefs.getProxy();
   }

   protected void tearDown() throws Exception
   {
      proxy = null;
//      prefs = null;
   }
/*
   public void testSetNonNullString()
   {
      proxy.setString("value");
      assertEquals("value", prefs.getValue("String", null));
   }

   public void testSetNullString() throws ReadOnlyException
   {
      prefs.setValue("String", "value1");
      proxy.setString(null);
      assertEquals("value2", prefs.getValue("String", "value2"));
   }

   public void testGetExistingString() throws ReadOnlyException
   {
      prefs.setValue("String", "value");
      assertEquals("value", proxy.getString(null));
   }

   public void testGetNonExistingString() throws ReadOnlyException
   {
      assertEquals("value", proxy.getString("value"));
      assertEquals(null, proxy.getString(null));
   }

   public void testGetExistingStringArray() throws ReadOnlyException
   {
      prefs.setValues("StringArray", new String[]{"value"});
      assertTrue(Arrays.equals(new String[]{"value"}, proxy.getStringArray(null)));
   }

   public void testGetNonExistingStringArray() throws ReadOnlyException
   {
      assertTrue(Arrays.equals(new String[]{"value"}, proxy.getStringArray(new String[]{"value"})));
   }

   public void testSetNonNullStringArray()
   {
      proxy.setStringArray(new String[]{"value"});
      assertTrue(Arrays.equals(new String[]{"value"}, prefs.getValues("StringArray", null)));
   }

   public void testSetNullStringArray() throws ReadOnlyException
   {
      prefs.setValues("StringArray", new String[]{"value1"});
      proxy.setStringArray(null);
      assertTrue(Arrays.equals(new String[]{"value2"}, prefs.getValues("StringArray", new String[]{"value2"})));
   }

   public void testSetInt()
   {
      proxy.setInt(1);
      assertEquals("1", prefs.getValue("Int", null));
   }

   public void testGetExistingInt() throws ReadOnlyException
   {
      prefs.setValue("Int", "1");
      assertEquals(1, proxy.getInt(0));

      prefs.setValue("Int", "not a number");
      assertEquals(2, proxy.getInt(2));
   }

   public void testGetNonExistingInt() throws ReadOnlyException
   {
      assertEquals(1, proxy.getInt(1));
   }

   public void testSetIntArray()
   {
      proxy.setIntArray(new int[]{0, 1, 2});
      assertTrue(Arrays.equals(new String[]{"0", "1", "2"}, prefs.getValues("IntArray", null)));
   }

   public void testGetExistingIntArray() throws ReadOnlyException
   {
      prefs.setValues("IntArray", new String[]{"0", "1", "2"});
      assertTrue(Arrays.equals(new int[]{0, 1, 2}, proxy.getIntArray(new int[]{-1})));

      prefs.setValues("IntArray", new String[]{"not a number"});
      assertTrue(Arrays.equals(new int[]{-1}, proxy.getIntArray(new int[]{-1})));

      prefs.setValues("IntArray", new String[]{null});
      assertTrue(Arrays.equals(new int[]{-1}, proxy.getIntArray(new int[]{-1})));
   }

   public void testGetNonExistingIntArray() throws ReadOnlyException
   {
      assertTrue(Arrays.equals(new int[]{-1}, proxy.getIntArray(new int[]{-1})));
   }

   public void testSetBoolean()
   {
      proxy.setBoolean(true);
      assertEquals("true", prefs.getValue("Boolean", null));
   }

   public void testGetExistingBoolean() throws ReadOnlyException
   {
      prefs.setValue("Boolean", "true");
      assertEquals(true, proxy.getBoolean(false));

      prefs.setValue("Boolean", "not a boolean");
      assertEquals(true, proxy.getBoolean(true));
   }

   public void testGetNonExistingBoolean() throws ReadOnlyException
   {
      assertEquals(true, proxy.getBoolean(true));
   }

   public void testSetBooleanArray()
   {
      proxy.setBooleanArray(new boolean[]{true, false});
      assertTrue(Arrays.equals(new String[]{"true", "false"}, prefs.getValues("BooleanArray", null)));
   }

   public void testGetExistingBooleanArray() throws ReadOnlyException
   {
      prefs.setValues("BooleanArray", new String[]{"true", "false"});
      assertTrue(Arrays.equals(new boolean[]{true, false}, proxy.getBooleanArray(new boolean[]{true})));

      prefs.setValues("BooleanArray", new String[]{"not a boolean"});
      assertTrue(Arrays.equals(new boolean[]{true}, proxy.getBooleanArray(new boolean[]{true})));

      prefs.setValues("BooleanArray", new String[]{null});
      assertTrue(Arrays.equals(new boolean[]{true}, proxy.getBooleanArray(new boolean[]{true})));
   }
*/
   public void testGetNonExistingBooleanArray() throws ReadOnlyException
   {
      assertTrue(Arrays.equals(new boolean[]{true}, proxy.getBooleanArray(new boolean[]{true})));
   }

   public interface TestProxy
   {
      int getInt(int dflt);

      String getString(String dflt);

      boolean getBoolean(boolean dflt);

      int[] getIntArray(int[] dflt);

      String[] getStringArray(String[] dflt);

      boolean[] getBooleanArray(boolean[] dflt);

      void setString(String value);

      void setStringArray(String[] value);

      void setInt(int value);

      void setIntArray(int[] value);

      void setBoolean(boolean value);

      void setBooleanArray(boolean[] value);
   }
}

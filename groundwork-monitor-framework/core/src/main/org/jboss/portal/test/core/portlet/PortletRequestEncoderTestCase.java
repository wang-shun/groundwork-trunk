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
package org.jboss.portal.test.core.portlet;

import org.jboss.portal.core.portlet.PortletRequestEncoder;
import org.jboss.portal.core.portlet.PortletRequestDecoder;
import org.jboss.portal.common.util.ParameterMap;
import org.jboss.portal.common.util.Tools;
import org.jboss.portal.portlet.ParametersStateString;
import org.jboss.portal.portlet.cache.CacheLevel;
import org.jboss.portal.Mode;
import org.jboss.portal.WindowState;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public class PortletRequestEncoderTestCase extends TestCase
{

   /** . */
   private ParameterMap params;

   /** . */
   private PortletRequestEncoder encoder;

   public void setUp() throws Exception
   {
      params = new ParameterMap();
      encoder = new PortletRequestEncoder(params);
   }

   public void tearDown() throws Exception
   {
      params = null;
      encoder = null;
   }


   public void testEncodeRender()
   {
      Blah blah = new Blah<Mode, WindowState>(
         Mode.VIEW,
         WindowState.NORMAL,
         PortletRequestDecoder.RENDER_PHASE,
         PortletRequestDecoder.MODE_PARAMETER,
         PortletRequestDecoder.WINDOW_STATE_PARAMETER,
         PortletRequestDecoder.MODE_MASK,
         PortletRequestDecoder.WINDOW_STATE_MASK)
      {
         protected void encodeBlah(ParametersStateString params, Mode view, WindowState normal)
         {
            encodeRender(params, view, normal);
         }
      };

      //
      blah.test();
   }


   public void testEncodeAction()
   {
      Blah blah = new Blah<Mode, WindowState>(
         Mode.VIEW,
         WindowState.NORMAL,
         PortletRequestDecoder.ACTION_PHASE,
         PortletRequestDecoder.MODE_PARAMETER,
         PortletRequestDecoder.WINDOW_STATE_PARAMETER,
         PortletRequestDecoder.MODE_MASK,
         PortletRequestDecoder.WINDOW_STATE_MASK)
      {
         protected void encodeBlah(ParametersStateString params, Mode view, WindowState normal)
         {
            encodeAction(params, view, normal);
         }
      };

      //
      blah.test();
   }

   public void testEncodeResource()
   {
      Blah blah = new Blah<String, CacheLevel>(
         "resource_id",
         CacheLevel.PAGE,
         PortletRequestDecoder.RESOURCE_PHASE,
         PortletRequestDecoder.RESOURCE_ID_PARAMETER,
         PortletRequestDecoder.CACHEABILITY_PARAMETER,
         PortletRequestDecoder.RESOURCE_ID_MASK,
         PortletRequestDecoder.CACHEABILITY_MASK)
      {
         protected void encodeBlah(ParametersStateString params, String view, CacheLevel normal)
         {
            encodeResource(params, view, normal);
         }
      };

      //
      blah.test();
   }

   private abstract class Blah<A,B>
   {

      /** . */
      protected final A a;

      /** . */
      protected final B b;

      /** . */
      protected final int lifecycleValue;

      /** . */
      protected final String aParamName;

      /** . */
      protected final String bParamName;

      /** . */
      protected final int aParamMask;

      /** . */
      protected final int bParamMask;

      protected Blah(A a, B b, int lifecycleValue, String aParamName, String bParamName, int aParamMask, int bParamMask)
      {
         this.a = a;
         this.b = b;
         this.lifecycleValue = lifecycleValue;
         this.aParamName = aParamName;
         this.bParamName = bParamName;
         this.aParamMask = aParamMask;
         this.bParamMask = bParamMask;
      }

      protected abstract void encodeBlah(ParametersStateString params, A view, B normal);

      public void test()
      {
         ParametersStateString pp = ParametersStateString.create();
         encodeBlah(pp, null, null);
         assertEquals(1, params.size());
         _assertEquals(lifecycleValue, params.getValues(PortletRequestDecoder.META_PARAMETER));

         //
         pp = ParametersStateString.create();
         pp.setValue(PortletRequestDecoder.META_PARAMETER, "foo");
         encodeBlah(pp, null, null);
         assertEquals(1, params.size());
         _assertEquals(new String[]{Integer.toHexString(lifecycleValue),"foo"}, params.getValues(PortletRequestDecoder.META_PARAMETER));

         //
         pp = ParametersStateString.create();
         pp.setValue("foo", "bar");
         encodeBlah(pp, null, null);
         assertEquals(2, params.size());
         _assertEquals(lifecycleValue, params.getValues(PortletRequestDecoder.META_PARAMETER));
         _assertEquals("bar", params.getValues("foo"));

         //
         pp = ParametersStateString.create();
         encodeBlah(pp, a, null);
         assertEquals(2, params.size());
         _assertEquals(lifecycleValue | aParamMask, params.getValues(PortletRequestDecoder.META_PARAMETER));
         _assertEquals(a, params.getValues(aParamName));

         //
         pp = ParametersStateString.create();
         pp.setValue(aParamName, "foo");
         encodeBlah(pp, a, null);
         assertEquals(2, params.size());
         _assertEquals(lifecycleValue | aParamMask, params.getValues(PortletRequestDecoder.META_PARAMETER));
         _assertEquals(new String[]{a.toString(),"foo"}, params.getValues(aParamName));

         //
         pp = ParametersStateString.create();
         pp.setValue("foo", "bar");
         encodeBlah(pp, a, null);
         assertEquals(3, params.size());
         _assertEquals(lifecycleValue | aParamMask, params.getValues(PortletRequestDecoder.META_PARAMETER));
         _assertEquals(a, params.getValues(aParamName));
         _assertEquals("bar", params.getValues("foo"));

         //
         pp = ParametersStateString.create();
         encodeBlah(pp, null, b);
         assertEquals(2, params.size());
         _assertEquals(lifecycleValue | bParamMask, params.getValues(PortletRequestDecoder.META_PARAMETER));
         _assertEquals(b, params.getValues(bParamName));

         //
         pp = ParametersStateString.create();
         pp.setValue(bParamName, "foo");
         encodeBlah(pp, null, b);
         assertEquals(2, params.size());
         _assertEquals(lifecycleValue | bParamMask, params.getValues(PortletRequestDecoder.META_PARAMETER));
         _assertEquals(new String[]{b.toString(),"foo"}, params.getValues(bParamName));

         //
         pp = ParametersStateString.create();
         pp.setValue("foo", "bar");
         encodeBlah(pp, null, b);
         assertEquals(3, params.size());
         _assertEquals(lifecycleValue | bParamMask, params.getValues(PortletRequestDecoder.META_PARAMETER));
         _assertEquals(b, params.getValues(bParamName));
         _assertEquals("bar", params.getValues("foo"));

         //
         pp = ParametersStateString.create();
         encodeBlah(pp, a, b);
         assertEquals(3, params.size());
         _assertEquals(lifecycleValue | aParamMask | bParamMask, params.getValues(PortletRequestDecoder.META_PARAMETER));
         _assertEquals(a, params.getValues(aParamName));
         _assertEquals(b, params.getValues(bParamName));

         //
         pp = ParametersStateString.create();
         pp.setValue("foo", "bar");
         encodeBlah(pp, a, b);
         assertEquals(4, params.size());
         _assertEquals(lifecycleValue | aParamMask | bParamMask, params.getValues(PortletRequestDecoder.META_PARAMETER));
         _assertEquals(a, params.getValues(aParamName));
         _assertEquals(b, params.getValues(bParamName));
         _assertEquals("bar", params.getValues("foo"));
      }
   }

   public void testEncodeNav()
   {
      encodeRender(null, null, null);
      assertEquals(0, params.size());

      //
      encodeRender(null, Mode.VIEW, null);
      assertEquals(1, params.size());
      _assertEquals(Mode.VIEW, params.getValues(PortletRequestDecoder.MODE_PARAMETER));

      //
      encodeRender(null, null, WindowState.NORMAL);
      assertEquals(1, params.size());
      _assertEquals(WindowState.NORMAL, params.getValues(PortletRequestDecoder.WINDOW_STATE_PARAMETER));

      //
      encodeRender(null, Mode.VIEW, WindowState.NORMAL);
      assertEquals(2, params.size());
      _assertEquals(Mode.VIEW, params.getValues(PortletRequestDecoder.MODE_PARAMETER));
      _assertEquals(WindowState.NORMAL, params.getValues(PortletRequestDecoder.WINDOW_STATE_PARAMETER));
   }

   private void encodeRender(ParametersStateString params, Mode view, WindowState normal)
   {
      encoder.encodeRender(params, view, normal);
   }

   private void encodeAction(ParametersStateString params, Mode view, WindowState normal)
   {
      encoder.encodeAction(null, params, view, normal);
   }

   private void encodeResource(ParametersStateString params, String resourceId, CacheLevel cacheability)
   {
      encoder.encodeResource(cacheability, resourceId, params);
   }

   void _assertEquals(int expected, String[] actual)
   {
      _assertEquals(new String[]{Integer.toHexString(expected)}, actual);
   }

   void _assertEquals(Object expected, String[] actual)
   {
      _assertEquals(new String[]{"" + expected}, actual);
   }

   void _assertEquals(String[] expected, String[] actual)
   {
      if (expected == null)
      {
         assertNull(actual);
      }
      else
      {
         assertNotNull((actual));
         assertEquals(Tools.toList(expected), Tools.toList(actual));
      }
   }
}
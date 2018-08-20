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

import org.jboss.portal.Mode;
import org.jboss.portal.WindowState;
import org.jboss.portal.core.portlet.PortletRequestDecoder;
import org.jboss.portal.common.util.ParameterMap;
import org.jboss.portal.portlet.OpaqueStateString;
import org.jboss.portal.portlet.ParametersStateString;
import org.jboss.portal.portlet.StateString;
import org.jboss.portal.portlet.cache.CacheLevel;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 6549 $
 */
public class PortletRequestDecoderTestCase extends TestCase
{

   public PortletRequestDecoderTestCase()
   {
   }

   private String[] asStringArray(Object s)
   {
      return new String[]{s.toString()};
   }

   public void testCorruped()
   {
      Map queryParams = new HashMap();
      Map bodyParams = new HashMap();
      PortletRequestDecoder o = new PortletRequestDecoder();

      // Action + Mode
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.ACTION_PHASE | PortletRequestDecoder.MODE_MASK)));
      assertFail(o, queryParams, null);
      queryParams.clear();

      //
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.ACTION_PHASE | PortletRequestDecoder.MODE_MASK)));
      bodyParams.put(PortletRequestDecoder.MODE_PARAMETER, asStringArray(Mode.VIEW.toString()));
      assertFail(o, queryParams, bodyParams);
      queryParams.clear();
      bodyParams.clear();

      // Action + WindowState
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.ACTION_PHASE | PortletRequestDecoder.WINDOW_STATE_MASK)));
      assertFail(o, queryParams, null);
      queryParams.clear();

      //
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.ACTION_PHASE | PortletRequestDecoder.WINDOW_STATE_MASK)));
      bodyParams.put(PortletRequestDecoder.WINDOW_STATE_PARAMETER, asStringArray(WindowState.NORMAL.toString()));
      assertFail(o, queryParams, bodyParams);
      queryParams.clear();
      bodyParams.clear();

      // Render + Mode
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.RENDER_PHASE | PortletRequestDecoder.MODE_MASK)));
      assertFail(o, queryParams, null);
      queryParams.clear();

      //
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.RENDER_PHASE | PortletRequestDecoder.MODE_MASK)));
      bodyParams.put(PortletRequestDecoder.MODE_PARAMETER, asStringArray(Mode.VIEW.toString()));
      assertFail(o, queryParams, bodyParams);
      queryParams.clear();
      bodyParams.clear();

      // Render + WindowState
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.RENDER_PHASE | PortletRequestDecoder.WINDOW_STATE_MASK)));
      assertFail(o, queryParams, bodyParams);
      queryParams.clear();

      //
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.RENDER_PHASE | PortletRequestDecoder.WINDOW_STATE_MASK)));
      bodyParams.put(PortletRequestDecoder.WINDOW_STATE_PARAMETER, asStringArray(WindowState.NORMAL.toString()));
      assertFail(o, queryParams, bodyParams);
      queryParams.clear();
      bodyParams.clear();
   }


   public void testNav()
   {
      Map queryParams = new HashMap();
      Map bodyParams = new HashMap();
      PortletRequestDecoder o = new PortletRequestDecoder();

      // Empty
      o.decode(queryParams, null);
      assertNav(o, null, null);
      queryParams.clear();

      // Query mode
      queryParams.put(PortletRequestDecoder.MODE_PARAMETER, asStringArray(Mode.VIEW.toString()));
      o.decode(queryParams, null);
      assertNav(o, Mode.VIEW, null);
      queryParams.clear();

      // Query mode two values
      queryParams.put(PortletRequestDecoder.MODE_PARAMETER, new String[]{Mode.VIEW.toString(), Mode.EDIT.toString()});
      o.decode(queryParams, null);
      assertNav(o, Mode.VIEW, null);
      queryParams.clear();

      // Body mode
      bodyParams.put(PortletRequestDecoder.MODE_PARAMETER, asStringArray(Mode.VIEW.toString()));
      o.decode(queryParams, bodyParams);
      assertNav(o, null, null);
      bodyParams.clear();

      // Query mode + Body mode
      queryParams.put(PortletRequestDecoder.MODE_PARAMETER, new String[]{Mode.VIEW.toString()});
      bodyParams.put(PortletRequestDecoder.MODE_PARAMETER, new String[]{Mode.EDIT.toString()});
      o.decode(queryParams, bodyParams);
      assertNav(o, Mode.VIEW, null);
      queryParams.clear();
      bodyParams.clear();

      // Query window state
      queryParams.put(PortletRequestDecoder.WINDOW_STATE_PARAMETER, asStringArray(WindowState.NORMAL.toString()));
      o.decode(queryParams, null);
      assertNav(o, null, WindowState.NORMAL);
      queryParams.clear();

      // Body window state
      bodyParams.put(PortletRequestDecoder.WINDOW_STATE_PARAMETER, asStringArray(WindowState.NORMAL.toString()));
      o.decode(queryParams, bodyParams);
      assertNav(o, null, null);
      bodyParams.clear();
   }

   public void testRender()
   {
      Map queryParams = new HashMap();
      PortletRequestDecoder o = new PortletRequestDecoder();

      // Empty
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.RENDER_PHASE)));
      o.decode(queryParams, null);
      assertRender(o, ParametersStateString.create(), null, null);
      queryParams.clear();

      // Query mode
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.RENDER_PHASE | PortletRequestDecoder.MODE_MASK)));
      queryParams.put(PortletRequestDecoder.MODE_PARAMETER, asStringArray(Mode.VIEW.toString()));
      o.decode(queryParams, null);
      assertRender(o, ParametersStateString.create(), Mode.VIEW, null);
      queryParams.clear();

      // Query window state
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.RENDER_PHASE | PortletRequestDecoder.WINDOW_STATE_MASK)));
      queryParams.put(PortletRequestDecoder.WINDOW_STATE_PARAMETER, asStringArray(WindowState.NORMAL.toString()));
      o.decode(queryParams, null);
      assertRender(o, ParametersStateString.create(), null, WindowState.NORMAL);
      queryParams.clear();
   }

   public void testRenderNonOpaque()
   {
      Map queryParams = new HashMap();
      Map bodyParams = new HashMap();
      PortletRequestDecoder o = new PortletRequestDecoder();
      ParametersStateString navState = ParametersStateString.create();

      // Query parameter
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.RENDER_PHASE)));
      queryParams.put("foo", asStringArray("bar"));
      o.decode(queryParams, null);
      navState.setValue("foo", "bar");
      assertRender(o, navState, null, null);
      navState.clear();
      queryParams.clear();

      // Query meta parameter
      queryParams.put(PortletRequestDecoder.META_PARAMETER, new String[]{Integer.toHexString(PortletRequestDecoder.RENDER_PHASE), "bar"});
      o.decode(queryParams, null);
      navState.setValue(PortletRequestDecoder.META_PARAMETER, "bar");
      assertRender(o, navState, null, null);
      navState.clear();
      queryParams.clear();

      // Query window state parameter + window state meta parameter
      queryParams.put(PortletRequestDecoder.META_PARAMETER, new String[]{Integer.toHexString(PortletRequestDecoder.RENDER_PHASE | PortletRequestDecoder.WINDOW_STATE_MASK)});
      queryParams.put(PortletRequestDecoder.WINDOW_STATE_PARAMETER, new String[]{WindowState.NORMAL.toString(), "bar"});
      o.decode(queryParams, null);
      navState.setValue(PortletRequestDecoder.WINDOW_STATE_PARAMETER, "bar");
      assertRender(o, navState, null, WindowState.NORMAL);
      navState.clear();
      queryParams.clear();

      // Query window state parameter
      queryParams.put(PortletRequestDecoder.META_PARAMETER, new String[]{Integer.toHexString(PortletRequestDecoder.RENDER_PHASE)});
      queryParams.put(PortletRequestDecoder.WINDOW_STATE_PARAMETER, new String[]{"bar"});
      o.decode(queryParams, null);
      navState.setValue(PortletRequestDecoder.WINDOW_STATE_PARAMETER, "bar");
      assertRender(o, navState, null, null);
      navState.clear();
      queryParams.clear();

      // Query mode parameter + mode meta parameter
      queryParams.put(PortletRequestDecoder.META_PARAMETER, new String[]{Integer.toHexString(PortletRequestDecoder.RENDER_PHASE | PortletRequestDecoder.MODE_MASK)});
      queryParams.put(PortletRequestDecoder.MODE_PARAMETER, new String[]{Mode.VIEW.toString(), "bar"});
      o.decode(queryParams, null);
      navState.setValue(PortletRequestDecoder.MODE_PARAMETER, "bar");
      assertRender(o, navState, Mode.VIEW, null);
      navState.clear();
      queryParams.clear();

      // Query mode parameter
      queryParams.put(PortletRequestDecoder.META_PARAMETER, new String[]{Integer.toHexString(PortletRequestDecoder.RENDER_PHASE)});
      queryParams.put(PortletRequestDecoder.MODE_PARAMETER, new String[]{"bar"});
      o.decode(queryParams, null);
      navState.setValue(PortletRequestDecoder.MODE_PARAMETER, "bar");
      assertRender(o, navState, null, null);
      navState.clear();
      queryParams.clear();

      // Body parameter
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.RENDER_PHASE)));
      bodyParams.put("foo", asStringArray("bar2"));
      o.decode(queryParams, bodyParams);
      assertRender(o, navState, null, null);
      navState.clear();
      queryParams.clear();
      bodyParams.clear();

      // Query multivalued parameter
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.RENDER_PHASE)));
      queryParams.put("foo", new String[]{"bar1", "bar2"});
      o.decode(queryParams, null);
      navState.setValues("foo", new String[]{"bar1", "bar2"});
      assertRender(o, navState, null, null);
      navState.clear();
      queryParams.clear();

      // Query + Body parameter
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.RENDER_PHASE)));
      queryParams.put("foo", new String[]{"bar1"});
      bodyParams.put("foo", new String[]{"bar2"});
      o.decode(queryParams, bodyParams);
      navState.setValue("foo", "bar1");
      assertRender(o, navState, null, null);
      navState.clear();
      queryParams.clear();
      bodyParams.clear();
   }

   public void testRenderOpaque()
   {
      Map queryParams = new HashMap();
      Map bodyParams = new HashMap();
      PortletRequestDecoder o = new PortletRequestDecoder();

      // Empty
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.RENDER_PHASE | PortletRequestDecoder.OPAQUE_MASK)));
      o.decode(queryParams, null);
      assertRender(o, null, null, null);
      queryParams.clear();

      // Query nav state
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.RENDER_PHASE | PortletRequestDecoder.OPAQUE_MASK)));
      queryParams.put(PortletRequestDecoder.NAVIGATIONAL_STATE_PARAMETER, asStringArray("navstatevalue"));
      o.decode(queryParams, bodyParams);
      assertRender(o, new OpaqueStateString("navstatevalue"), null, null);
      queryParams.clear();

      // Body nav state
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.RENDER_PHASE | PortletRequestDecoder.OPAQUE_MASK)));
      bodyParams.put(PortletRequestDecoder.NAVIGATIONAL_STATE_PARAMETER, asStringArray("navstatevalue"));
      o.decode(queryParams, bodyParams);
      assertRender(o, null, null, null);
      queryParams.clear();
      bodyParams.clear();

      // Query int state is ignored
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.RENDER_PHASE | PortletRequestDecoder.OPAQUE_MASK)));
      queryParams.put(PortletRequestDecoder.INTERACTION_STATE_PARAMETER, asStringArray("intstatevalue"));
      o.decode(queryParams, null);
      assertRender(o, null, null, null);
      queryParams.clear();

      // Body int state is ignored
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.RENDER_PHASE | PortletRequestDecoder.OPAQUE_MASK)));
      bodyParams.put(PortletRequestDecoder.INTERACTION_STATE_PARAMETER, asStringArray("intstatevalue"));
      o.decode(queryParams, bodyParams);
      assertRender(o, null, null, null);
      queryParams.clear();
      bodyParams.clear();
   }

   public void testAction()
   {
      Map queryParams = new HashMap();
      PortletRequestDecoder o = new PortletRequestDecoder();

      // Empty
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.ACTION_PHASE)));
      o.decode(queryParams, null);
      assertAction(o, null, ParametersStateString.create(), new ParameterMap(), null, null);
      queryParams.clear();

      // Query mode
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.ACTION_PHASE | PortletRequestDecoder.MODE_MASK)));
      queryParams.put(PortletRequestDecoder.MODE_PARAMETER, asStringArray(Mode.VIEW.toString()));
      o.decode(queryParams, null);
      assertAction(o, null, ParametersStateString.create(), new ParameterMap(), Mode.VIEW, null);
      queryParams.clear();

      // Query window state
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.ACTION_PHASE | PortletRequestDecoder.WINDOW_STATE_MASK)));
      queryParams.put(PortletRequestDecoder.WINDOW_STATE_PARAMETER, asStringArray(WindowState.NORMAL.toString()));
      o.decode(queryParams, null);
      assertAction(o, null, ParametersStateString.create(), new ParameterMap(), null, WindowState.NORMAL);
      queryParams.clear();
   }

   public void testActionNonOpaque()
   {
      Map queryParams = new HashMap();
      Map bodyParams = new HashMap();
      PortletRequestDecoder o = new PortletRequestDecoder();
      ParametersStateString intState = ParametersStateString.create();
      ParameterMap form = new ParameterMap();

      // Query parameter
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.ACTION_PHASE)));
      queryParams.put("foo", asStringArray("bar"));
      o.decode(queryParams, null);
      intState.setValue("foo", "bar");
      assertAction(o, null, intState, form, null, null);
      intState.clear();
      queryParams.clear();
      form.clear();

      // Query multivalued parameter
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.ACTION_PHASE)));
      queryParams.put("foo", new String[]{"bar1", "bar2"});
      o.decode(queryParams, null);
      intState.setValues("foo", new String[]{"bar1", "bar2"});
      assertAction(o, null, intState, form, null, null);
      intState.clear();
      queryParams.clear();
      form.clear();

      // Body parameter
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.ACTION_PHASE)));
      bodyParams.put("foo", asStringArray("bar"));
      o.decode(queryParams, bodyParams);
      form.setValue("foo", "bar");
      assertAction(o, null, intState, form, null, null);
      form.clear();
      queryParams.clear();
      bodyParams.clear();
      form.clear();

      // Body multivalued parameter
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.ACTION_PHASE)));
      bodyParams.put("foo", new String[]{"bar1", "bar2"});
      o.decode(queryParams, bodyParams);
      form.setValues("foo", new String[]{"bar1", "bar2"});
      assertAction(o, null, intState, form, null, null);
      form.clear();
      queryParams.clear();
      bodyParams.clear();
      form.clear();
   }

   public void testActionOpaque()
   {
      Map queryParams = new HashMap();
      Map bodyParams = new HashMap();
      PortletRequestDecoder o = new PortletRequestDecoder();

      // Empty
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.ACTION_PHASE | PortletRequestDecoder.OPAQUE_MASK)));
      o.decode(queryParams, null);
      assertAction(o, null, null, new ParameterMap(), null, null);
      queryParams.clear();

      // Query nav state
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.ACTION_PHASE | PortletRequestDecoder.OPAQUE_MASK)));
      queryParams.put(PortletRequestDecoder.NAVIGATIONAL_STATE_PARAMETER, asStringArray("navstatevalue"));
      o.decode(queryParams, null);
      assertAction(o, new OpaqueStateString("navstatevalue"), null, new ParameterMap(), null, null);
      queryParams.clear();

      // Query int state
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.ACTION_PHASE | PortletRequestDecoder.OPAQUE_MASK)));
      queryParams.put(PortletRequestDecoder.INTERACTION_STATE_PARAMETER, asStringArray("intstatevalue"));
      o.decode(queryParams, null);
      assertAction(o, null, new OpaqueStateString("intstatevalue"), new ParameterMap(), null, null);
      queryParams.clear();

      // Body parameters
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.ACTION_PHASE | PortletRequestDecoder.OPAQUE_MASK)));
      bodyParams.put("foo1", asStringArray("bar1"));
      bodyParams.put("foo2", new String[]{"bar2", "bar3"});
      queryParams.put("foo3", new String[]{"bar4"});
      bodyParams.put("foo3", new String[]{"bar5"});
      o.decode(queryParams, bodyParams);
      ParameterMap form = new ParameterMap();
      form.setValue("foo1", "bar1");
      form.setValues("foo2", new String[]{"bar2", "bar3"});
      form.setValues("foo3", new String[]{"bar5"});
      assertAction(o, null, null, form, null, null);
      queryParams.clear();
      bodyParams.clear();
   }

   public void testResource()
   {
      Map queryParams = new HashMap();
      PortletRequestDecoder o = new PortletRequestDecoder();

      // Empty
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.RESOURCE_TYPE)));
      o.decode(queryParams, null);
      assertResource(o, null, ParametersStateString.create(), new ParameterMap(), null);
      queryParams.clear();

      // Query mode
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.RESOURCE_TYPE | PortletRequestDecoder.RESOURCE_ID_MASK)));
      queryParams.put(PortletRequestDecoder.RESOURCE_ID_PARAMETER, asStringArray("resource_id"));
      o.decode(queryParams, null);
      assertResource(o, "resource_id", ParametersStateString.create(), new ParameterMap(), null);
      queryParams.clear();

      // Query window state
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.RESOURCE_TYPE | PortletRequestDecoder.CACHEABILITY_MASK)));
      queryParams.put(PortletRequestDecoder.CACHEABILITY_PARAMETER, asStringArray(CacheLevel.PAGE));
      o.decode(queryParams, null);
      assertResource(o, null, ParametersStateString.create(), new ParameterMap(), CacheLevel.PAGE);
      queryParams.clear();
   }

   public void testResourceNonOpaque()
   {
      Map queryParams = new HashMap();
      Map bodyParams = new HashMap();
      PortletRequestDecoder o = new PortletRequestDecoder();
      ParametersStateString resState = ParametersStateString.create();
      ParameterMap form = new ParameterMap();

      // Query parameter
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.RESOURCE_PHASE)));
      queryParams.put("foo", asStringArray("bar"));
      o.decode(queryParams, null);
      resState.setValue("foo", "bar");
      assertResource(o, null, resState, form, null);
      resState.clear();
      queryParams.clear();
      form.clear();

      // Query multivalued parameter
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.RESOURCE_PHASE)));
      queryParams.put("foo", new String[]{"bar1", "bar2"});
      o.decode(queryParams, null);
      resState.setValues("foo", new String[]{"bar1", "bar2"});
      assertResource(o, null, resState, form, null);
      resState.clear();
      queryParams.clear();
      form.clear();

      // Body parameter
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.RESOURCE_PHASE)));
      bodyParams.put("foo", asStringArray("bar"));
      o.decode(queryParams, bodyParams);
      form.setValue("foo", "bar");
      assertResource(o, null, resState, form, null);
      form.clear();
      queryParams.clear();
      bodyParams.clear();
      form.clear();

      // Body multivalued parameter
      queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.RESOURCE_PHASE)));
      bodyParams.put("foo", new String[]{"bar1", "bar2"});
      o.decode(queryParams, bodyParams);
      form.setValues("foo", new String[]{"bar1", "bar2"});
      assertResource(o, null, resState, form, null);
      form.clear();
      queryParams.clear();
      bodyParams.clear();
      form.clear();
   }

   private void assertResource(
      PortletRequestDecoder decoder,
      String expectedResourceId,
      StateString expectedResourceState,
      ParameterMap expectedForm,
      CacheLevel expectedCacheability
   )
   {
      assertEquals(PortletRequestDecoder.RESOURCE_TYPE, decoder.getType());
      assertEquals(expectedResourceId, decoder.getResourceId());
      assertEquals(expectedResourceState, decoder.getResourceState());
      assertEquals(expectedForm, decoder.getForm());
      assertEquals(expectedCacheability, decoder.getCacheability());

      //
      assertNull(decoder.getNavigationalState());
      assertNull( decoder.getInteractionState());
      assertNull(decoder.getMode());
      assertNull(decoder.getWindowState());
   }

   private void assertAction(
      PortletRequestDecoder decoder,
      StateString expectedNavigationalState,
      StateString expectedInteractionState,
      ParameterMap expectedForm,
      Mode expectedMode,
      WindowState expectedWindowState
   )
   {
      assertEquals(PortletRequestDecoder.ACTION_TYPE, decoder.getType());
      assertEquals(expectedNavigationalState, decoder.getNavigationalState());
      assertEquals(expectedInteractionState, decoder.getInteractionState());
      assertEquals(expectedForm, decoder.getForm());
      assertEquals(expectedMode, decoder.getMode());
      assertEquals(expectedWindowState, decoder.getWindowState());

      //
      assertNull(decoder.getResourceId());
      assertNull(decoder.getResourceState());
      assertNull(decoder.getCacheability());
   }

   private void assertRender(
      PortletRequestDecoder decoder,
      StateString expectedNavigationalState,
      Mode expectedMode,
      WindowState expectedWindowState
   )
   {
      assertEquals(PortletRequestDecoder.RENDER_TYPE, decoder.getType());
      assertEquals(expectedNavigationalState, decoder.getNavigationalState());
      assertEquals(expectedMode, decoder.getMode());
      assertEquals(expectedWindowState, decoder.getWindowState());

      //
      assertNull(decoder.getForm());
      assertNull(decoder.getInteractionState());
      assertNull(decoder.getResourceId());
      assertNull(decoder.getResourceState());
      assertNull(decoder.getCacheability());
   }

   private void assertNav(
      PortletRequestDecoder decoder,
      Mode expectedMode,
      WindowState expectedWindowState
   )
   {
      assertEquals(PortletRequestDecoder.NAV_TYPE, decoder.getType());
      assertEquals(expectedMode, decoder.getMode());
      assertEquals(expectedWindowState, decoder.getWindowState());

      //
      assertNull(decoder.getNavigationalState());
      assertNull(decoder.getForm());
      assertNull(decoder.getInteractionState());
      assertNull(decoder.getResourceId());
      assertNull(decoder.getResourceState());
      assertNull(decoder.getCacheability());
   }

   private void assertFail(
      PortletRequestDecoder decoder,
      Map queryParams,
      Map bodyParams)
   {
      try
      {
         queryParams.put(PortletRequestDecoder.META_PARAMETER, asStringArray(Integer.toHexString(PortletRequestDecoder.ACTION_PHASE | PortletRequestDecoder.MODE_MASK)));
         decoder.decode(queryParams, bodyParams);
         fail();
      }
      catch (IllegalArgumentException expected)
      {
      }
   }
}
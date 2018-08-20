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

package org.jboss.portal.test.core.model.portal.coordination;

import junit.framework.TestSuite;
import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.jboss.portal.common.junit.TransactionAssert;
import org.jboss.portal.core.controller.coordination.AliasBindingInfo;
import org.jboss.portal.core.controller.coordination.EventWiringInfo;
import org.jboss.portal.core.controller.coordination.IllegalCoordinationException;
import org.jboss.portal.core.controller.coordination.ParameterBindingInfo;
import org.jboss.portal.core.impl.coordination.CoordinationService;
import org.jboss.portal.core.model.content.ContentType;
import org.jboss.portal.core.model.portal.Context;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.core.model.portal.Window;
import org.jboss.portal.test.core.model.portal.AbstractPortalObjectContainerTestCase;

import javax.xml.namespace.QName;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:boleslaw dot dawidowicz at redhat anotherdot com">Boleslaw Dawidowicz</a>
 * @version : 0.1 $
 */
public class CoordinationServiceTestCase extends AbstractPortalObjectContainerTestCase
{

   static
   {
      Appender appender = new ConsoleAppender(new SimpleLayout());
      Logger.getRoot().addAppender(appender);
      Logger.getRoot().setLevel(Level.ERROR);
      Logger.getLogger("org.hibernate").setLevel(Level.ERROR);
   }

   protected CoordinationService cos;

   protected String getConfigLocationPrefix()
   {
      return "org/jboss/portal/test/core/model/portal/coordination/";
   }

   public static TestSuite suite() throws Exception
   {
      return AbstractPortalObjectContainerTestCase.suite(CoordinationServiceTestCase.class);
   }


   public void setUp() throws Exception
   {
      super.setUp();

      //
      TransactionAssert.beginTransaction();
      Context root = container.getContext("");
      Portal p_1 = root.createPortal("Portal_1");
      Page p_1_1 = p_1.createPage("Page_1");
      Page p_1_2 = p_1.createPage("Page_2");

      p_1_1.createPage("Page_1");

      p_1_1.createWindow("Window_1", ContentType.PORTLET, "");
      p_1_1.createWindow("Window_2", ContentType.PORTLET, "");
      p_1_1.createWindow("Window_3", ContentType.PORTLET, "");
      p_1_1.createWindow("Window_4", ContentType.PORTLET, "");


      p_1_2.createWindow("Window_1", ContentType.PORTLET, "");
      p_1_2.createWindow("Window_2", ContentType.PORTLET, "");
      p_1_2.createWindow("Window_3", ContentType.PORTLET, "");
      p_1_2.createWindow("Window_4", ContentType.PORTLET, "");


      TransactionAssert.commitTransaction();
   }

   public void testImplicitMode() throws Exception
   {
      TransactionAssert.beginTransaction();

      Portal portal_1 = container.getContext("").getPortal("Portal_1");
      Page page_1 = portal_1.getPage("Page_1");
      Page page_1_1 = page_1.getPage("Page_1");


      assertNull(cos.isEventWiringImplicitModeEnabled(portal_1));
      assertNull(cos.isEventWiringImplicitModeEnabled(page_1));
      assertNull(cos.isEventWiringImplicitModeEnabled(page_1_1));

      assertNull(cos.isParameterBindingImplicitModeEnabled(portal_1));
      assertNull(cos.isParameterBindingImplicitModeEnabled(page_1));
      assertNull(cos.isParameterBindingImplicitModeEnabled(page_1_1));

      // Check hardcoded default

      assertEquals(CoordinationService.DEFAULT_IMPLICIT_MODE, cos.resolveEventWiringImplicitModeEnabled(portal_1));
      assertEquals(CoordinationService.DEFAULT_IMPLICIT_MODE, cos.resolveEventWiringImplicitModeEnabled(page_1));
      assertEquals(CoordinationService.DEFAULT_IMPLICIT_MODE, cos.resolveEventWiringImplicitModeEnabled(page_1_1));

      assertEquals(CoordinationService.DEFAULT_IMPLICIT_MODE, cos.resolveParameterBindingImplicitModeEnabled(portal_1));
      assertEquals(CoordinationService.DEFAULT_IMPLICIT_MODE, cos.resolveParameterBindingImplicitModeEnabled(page_1));
      assertEquals(CoordinationService.DEFAULT_IMPLICIT_MODE, cos.resolveParameterBindingImplicitModeEnabled(page_1_1));

      // Check set / remove

      cos.setEventWiringImplicitMode(page_1, false);

      assertEquals(FALSE, cos.isEventWiringImplicitModeEnabled(page_1));

      cos.setEventWiringImplicitMode(page_1, true);

      assertEquals(TRUE, cos.isEventWiringImplicitModeEnabled(page_1));

      cos.removeEventWiringImplicitMode(page_1);

      assertNull(cos.isEventWiringImplicitModeEnabled(page_1));

      //

      cos.setParameterBindingImplicitMode(page_1, false);

      assertEquals(FALSE, cos.isParameterBindingImplicitModeEnabled(page_1));

      cos.setParameterBindingImplicitMode(page_1, true);

      assertEquals(TRUE, cos.isParameterBindingImplicitModeEnabled(page_1));

      cos.removeParameterBindingImplicitMode(page_1);

      assertNull(cos.isParameterBindingImplicitModeEnabled(page_1));

      // Check inheritance and resolve

      cos.setEventWiringImplicitMode(portal_1, false);
      cos.setEventWiringImplicitMode(page_1, true);

      assertEquals(FALSE, cos.resolveEventWiringImplicitModeEnabled(portal_1));
      assertEquals(TRUE, cos.resolveEventWiringImplicitModeEnabled(page_1));
      assertEquals(TRUE, cos.resolveEventWiringImplicitModeEnabled(page_1_1));

      cos.setEventWiringImplicitMode(portal_1, true);
      cos.setEventWiringImplicitMode(page_1, false);

      assertEquals(TRUE, cos.resolveEventWiringImplicitModeEnabled(portal_1));
      assertEquals(FALSE, cos.resolveEventWiringImplicitModeEnabled(page_1));
      assertEquals(FALSE, cos.resolveEventWiringImplicitModeEnabled(page_1_1));

      //

      cos.setParameterBindingImplicitMode(portal_1, false);
      cos.setParameterBindingImplicitMode(page_1, true);

      assertEquals(FALSE, cos.resolveParameterBindingImplicitModeEnabled(portal_1));
      assertEquals(TRUE, cos.resolveParameterBindingImplicitModeEnabled(page_1));
      assertEquals(TRUE, cos.resolveParameterBindingImplicitModeEnabled(page_1_1));

      cos.setParameterBindingImplicitMode(portal_1, true);
      cos.setParameterBindingImplicitMode(page_1, false);

      assertEquals(TRUE, cos.resolveParameterBindingImplicitModeEnabled(portal_1));
      assertEquals(FALSE, cos.resolveParameterBindingImplicitModeEnabled(page_1));
      assertEquals(FALSE, cos.resolveParameterBindingImplicitModeEnabled(page_1_1));

      TransactionAssert.commitTransaction();
   }

   public void testEventWiring() throws Exception
   {
      TransactionAssert.beginTransaction();

      Page page1 = getPage1();

      Window w1 = page1.getWindow("Window_1");
      Window w2 = page1.getWindow("Window_2");
      Window w3 = page1.getWindow("Window_3");
      Window w4 = page1.getWindow("Window_4");

      Map<Window, QName> s1 = new HashMap<Window, QName>();
      Map<Window, QName> d1 = new HashMap<Window, QName>();
      s1.put(w1, new QName("juju", "foo"));
      s1.put(w2, new QName("juju", "foo1"));
      s1.put(w3, new QName("juju", "foo3"));

      d1.put(w4, new QName("bobo", "bar"));

      cos.setEventWiring(s1, d1, "event1");

      Collection<EventWiringInfo> events = cos.getEventWirings(page1);

      assertEquals(1, events.size());

      EventWiringInfo originalInfo = events.iterator().next();
      assertEquals("event1", originalInfo.getName());

      events = cos.getEventSourceWirings(w1);

      assertNotNull(events);
      assertEquals(1, events.size());

      EventWiringInfo info = events.iterator().next();

      assertTrue(info.getSources().keySet().size() == 3);
      assertTrue(info.getSources().keySet().contains(w1));
      assertTrue(info.getSources().keySet().contains(w2));
      assertTrue(info.getSources().keySet().contains(w3));

      events = cos.getEventDestinationWirings(w4);

      assertNotNull(events);
      assertEquals(1, events.size());

      info = events.iterator().next();

      assertEquals(1, info.getDestinations().keySet().size());
      assertTrue(info.getDestinations().keySet().contains(w4));


      // remove
      cos.removeEventWiring(originalInfo);
      events = cos.getEventWirings(page1);
      assertNotNull(events);
      assertTrue(events.isEmpty());

      events = cos.getEventDestinationWirings(w4);
      assertNotNull(events);
      assertTrue(events.isEmpty());

      TransactionAssert.commitTransaction();
   }

   public void testParameterBinding() throws Exception
   {
      TransactionAssert.beginTransaction();

      Page page1 = getPage1();

      Window w1 = page1.getWindow("Window_1");
      Window w2 = page1.getWindow("Window_2");
      Window w4 = page1.getWindow("Window_4");


      Map<Window, Set<QName>> ws = new HashMap<Window, Set<QName>>();
      ws.put(w1, Collections.singleton(new QName("juju", "foo")));
      ws.put(w2, Collections.singleton(new QName("juju", "foo1")));
      ws.put(w4, Collections.singleton(new QName("juju", "foo3")));

      cos.setParameterBinding("binding1", ws);

      Collection<? extends ParameterBindingInfo> bindings = cos.getParameterBindings(page1);

      assertEquals(1, bindings.size());

      Map<Window, Set<QName>> mappings = bindings.iterator().next().getMappings();

      assertEquals(3, mappings.size());

      assertTrue(mappings.containsKey(w1));
      assertTrue(mappings.containsKey(w2));
      assertTrue(mappings.containsKey(w4));

      cos.removeParameterBinding(bindings.iterator().next());

      bindings = cos.getParameterBindings(w1);

      assertEquals(0, bindings.size());


      TransactionAssert.commitTransaction();
   }

   public void testGetParameterBinding() throws Exception
   {
      TransactionAssert.beginTransaction();

      Page page1 = getPage1();

      Window w1 = page1.getWindow("Window_1");
      Window w2 = page1.getWindow("Window_2");
      Window w4 = page1.getWindow("Window_4");


      Map<Window, Set<QName>> ws = new HashMap<Window, Set<QName>>();
      QName qName11 = new QName("ns1", "1");
      QName qName12 = new QName("ns1", "2");
      ws.put(w1, Collections.singleton(qName11));
      ws.put(w2, Collections.singleton(qName12));
      cos.setParameterBinding("binding1", ws);

      QName qName21 = new QName("ns2", "foo3");
      ws.clear();
      ws.put(w4, Collections.singleton(qName21));
      cos.setParameterBinding("binding2", ws);

      ParameterBindingInfo info = cos.getParameterBinding(page1, "binding1");
      assertNotNull(info);
      assertEquals("binding1", info.getName());
      assertEquals(page1, info.getPage());
      Map<Window, Set<QName>> windows = info.getMappings();
      assertNotNull(windows);
      assertEquals(2, windows.size());
      assertTrue(windows.containsKey(w1));
      assertTrue(windows.containsKey(w2));
      assertEquals(Collections.singleton(qName11), windows.get(w1));
      assertEquals(Collections.singleton(qName12), windows.get(w2));

      info = cos.getParameterBinding(page1, "binding2");
      assertNotNull(info);
      assertEquals("binding2", info.getName());
      assertEquals(page1, info.getPage());
      windows = info.getMappings();
      assertNotNull(windows);
      assertEquals(1, windows.size());
      assertTrue(windows.containsKey(w4));
      assertEquals(Collections.singleton(qName21), windows.get(w4));

      TransactionAssert.commitTransaction();
   }

   public void testGetParameterBindings() throws IllegalCoordinationException
   {
      TransactionAssert.beginTransaction();

      Page page1 = getPage1();

      Window w1 = page1.getWindow("Window_1");
      Window w2 = page1.getWindow("Window_2");
      Window w4 = page1.getWindow("Window_4");


      Map<Window, Set<QName>> ws = new HashMap<Window, Set<QName>>();
      QName qName11 = new QName("ns1", "1");
      QName qName12 = new QName("ns1", "2");
      ws.put(w1, Collections.singleton(qName11));
      ws.put(w2, Collections.singleton(qName12));
      cos.setParameterBinding("binding1", ws);

      QName qName21 = new QName("ns2", "foo3");
      ws.clear();
      ws.put(w4, Collections.singleton(qName21));
      cos.setParameterBinding("binding2", ws);

      // check bindings on page
      Collection<? extends ParameterBindingInfo> infos = cos.getParameterBindings(page1);
      assertNotNull(infos);
      assertEquals(2, infos.size());
      int count = 0;
      for (ParameterBindingInfo info : infos)
      {
         String name = info.getName();
         if ("binding1".equals(name) || "binding2".equals(name))
         {
            count++;
         }
      }
      assertEquals(2, count);

      infos = cos.getParameterBindings(w1);
      assertNotNull(infos);
      assertEquals(1, infos.size());
      for (ParameterBindingInfo info : infos)
      {
         assertEquals("binding1", info.getName());
      }

      ws = new HashMap<Window, Set<QName>>();
      ws.put(w2, Collections.singleton(qName11));
      cos.setParameterBinding("binding3", ws);

      infos = cos.getParameterBindings(page1, qName21);
      assertNotNull(infos);
      assertEquals(1, infos.size());
      for (ParameterBindingInfo info : infos)
      {
         assertEquals("binding2", info.getName());
      }

      infos = cos.getParameterBindings(page1, qName11);
      assertEquals(2, infos.size());
      count = 0;
      for (ParameterBindingInfo info : infos)
      {
         String name = info.getName();
         if ("binding1".equals(name) || "binding3".equals(name))
         {
            count++;
         }
      }
      assertEquals(2, count);

      TransactionAssert.commitTransaction();
   }

   public void testRemoveParameterBinding() throws Exception
   {
      TransactionAssert.beginTransaction();

      Page page1 = getPage1();

      Window w1 = page1.getWindow("Window_1");
      Window w2 = page1.getWindow("Window_2");
      Window w4 = page1.getWindow("Window_4");

      Map<Window, Set<QName>> ws = new HashMap<Window, Set<QName>>();
      QName qName11 = new QName("ns1", "1");
      QName qName12 = new QName("ns1", "2");
      ws.put(w1, Collections.singleton(qName11));
      ws.put(w2, Collections.singleton(qName12));
      cos.setParameterBinding("binding1", ws);

      QName qName21 = new QName("ns2", "foo3");
      ws.clear();
      ws.put(w4, Collections.singleton(qName21));
      cos.setParameterBinding("binding2", ws);

      Collection<? extends ParameterBindingInfo> infos = cos.getParameterBindings(page1);
      assertNotNull(infos);
      assertEquals(2, infos.size());

      cos.removeParameterBinding(page1, "binding2");
      infos = cos.getParameterBindings(page1);
      assertNotNull(infos);
      assertEquals(1, infos.size());
      assertNotNull(cos.getParameterBinding(page1, "binding1"));
      assertNull(cos.getParameterBinding(page1, "binding2"));

      TransactionAssert.commitTransaction();
   }

   public void testAliasBinding() throws Exception
   {
      TransactionAssert.beginTransaction();

      Portal portal1 = container.getContext("").getPortal("Portal_1");
      Page page1 = portal1.getPage("Page_1");
      Page page2 = portal1.getPage("Page_2");

      QName qname1 = new QName("nm1", "local1");
      QName qname2 = new QName("nm2", "local2");
      QName qname3 = new QName("nm3", "local3");
      QName qname4 = new QName("nm4", "local4");
      QName qname5 = new QName("nm5", "local5");
      QName qname6 = new QName("nm6", "local6");
      QName qname7 = new QName("nm7", "local7");
      QName qname8 = new QName("nm8", "local8");
      QName qname9 = new QName("nm9", "local9");

      HashSet<QName> qnames1 = new HashSet<QName>();
      qnames1.add(qname1);
      qnames1.add(qname2);
      qnames1.add(qname3);

      HashSet<QName> qnames2 = new HashSet<QName>();
      qnames2.add(qname4);
      qnames2.add(qname5);
      qnames2.add(qname6);

      HashSet<QName> qnames3 = new HashSet<QName>();
      qnames3.add(qname7);
      qnames3.add(qname8);
      qnames3.add(qname9);

      cos.setAliasBinding(page1, "alias1", qnames1);
      cos.setAliasBinding(page2, "alias2", qnames2);
      cos.setAliasBinding(page2, "alias3", qnames3);

      Collection<AliasBindingInfo> bindings1 = cos.getAliasBindings(page1);

      assertEquals(1, bindings1.size());
      AliasBindingInfo info = bindings1.iterator().next();

      assertTrue(info.getName().equals("alias1"));
      assertEquals(3, info.getParameterNames().size());
      assertTrue(info.getParameterNames().contains(qname1));
      assertTrue(info.getParameterNames().contains(qname2));
      assertTrue(info.getParameterNames().contains(qname3));

      cos.removeAliasBinding(info);
      assertTrue(cos.getAliasBindings(page1).isEmpty());

      AliasBindingInfo info2 = null;
      AliasBindingInfo info3 = null;

      Collection<AliasBindingInfo> bindings2 = cos.getAliasBindings(page2);

      assertEquals(2, bindings2.size());

      for (AliasBindingInfo aliasBindingInfo : bindings2)
      {
         if (aliasBindingInfo.getName().equals("alias2"))
         {
            info2 = aliasBindingInfo;
         }
         if (aliasBindingInfo.getName().equals("alias3"))
         {
            info3 = aliasBindingInfo;
         }

      }

      assertNotNull(info2);
      assertNotNull(info3);

      assertTrue(info2.getName().equals("alias2"));
      assertEquals(3, info2.getParameterNames().size());
      assertTrue(info2.getParameterNames().contains(qname4));
      assertTrue(info2.getParameterNames().contains(qname5));
      assertTrue(info2.getParameterNames().contains(qname6));

      assertTrue(info3.getName().equals("alias3"));
      assertEquals(3, info3.getParameterNames().size());
      assertTrue(info3.getParameterNames().contains(qname7));
      assertTrue(info3.getParameterNames().contains(qname8));
      assertTrue(info3.getParameterNames().contains(qname9));


      TransactionAssert.commitTransaction();
   }

   public void testGetAliasBinding() throws Exception
   {
      TransactionAssert.beginTransaction();

      Page page1 = getPage1();

      QName qname1 = new QName("nm1", "local1");
      QName qname2 = new QName("nm2", "local2");
      QName qname3 = new QName("nm3", "local3");
      QName qname4 = new QName("nm4", "local4");
      QName qname5 = new QName("nm5", "local5");
      QName qname6 = new QName("nm6", "local6");

      HashSet<QName> qnames1 = new HashSet<QName>();
      qnames1.add(qname1);
      qnames1.add(qname2);
      qnames1.add(qname3);

      HashSet<QName> qnames2 = new HashSet<QName>();
      qnames2.add(qname4);
      qnames2.add(qname5);
      qnames2.add(qname6);

      cos.setAliasBinding(page1, "alias1", qnames1);
      cos.setAliasBinding(page1, "alias2", qnames2);

      AliasBindingInfo info = cos.getAliasBinding(page1, "alias1");
      assertNotNull(info);
      assertEquals("alias1", info.getName());
      assertEquals(page1, info.getPage());
      Set<QName> names = info.getParameterNames();
      assertNotNull(names);
      assertEquals(3, names.size());
      assertTrue(names.contains(qname1));
      assertTrue(names.contains(qname2));
      assertTrue(names.contains(qname3));

      info = cos.getAliasBinding(page1, "alias2");
      assertNotNull(info);
      assertEquals("alias2", info.getName());
      assertEquals(page1, info.getPage());
      names = info.getParameterNames();
      assertNotNull(names);
      assertEquals(3, names.size());
      assertTrue(names.contains(qname4));
      assertTrue(names.contains(qname5));
      assertTrue(names.contains(qname6));

      TransactionAssert.commitTransaction();
   }

   public void testRemoveAliasBinding() throws Exception
   {
      TransactionAssert.beginTransaction();

      Page page1 = getPage1();

      QName qname1 = new QName("nm1", "local1");
      QName qname2 = new QName("nm2", "local2");
      QName qname3 = new QName("nm3", "local3");
      QName qname4 = new QName("nm4", "local4");
      QName qname5 = new QName("nm5", "local5");
      QName qname6 = new QName("nm6", "local6");

      HashSet<QName> qnames1 = new HashSet<QName>();
      qnames1.add(qname1);
      qnames1.add(qname2);
      qnames1.add(qname3);

      HashSet<QName> qnames2 = new HashSet<QName>();
      qnames2.add(qname4);
      qnames2.add(qname5);
      qnames2.add(qname6);

      cos.setAliasBinding(page1, "alias1", qnames1);
      cos.setAliasBinding(page1, "alias2", qnames2);

      Collection<AliasBindingInfo> bindings = cos.getAliasBindings(page1);
      assertEquals(2, bindings.size());

      cos.removeAliasBinding(page1, "alias1");
      bindings = cos.getAliasBindings(page1);
      assertEquals(1, bindings.size());
      assertNull(cos.getAliasBinding(page1, "alias1"));
      assertNotNull(cos.getAliasBinding(page1, "alias2"));

      TransactionAssert.commitTransaction();
   }


   public void testRenameEventWiring() throws IllegalCoordinationException
   {
      TransactionAssert.beginTransaction();

      Page page1 = getPage1();

      Window w1 = page1.getWindow("Window_1");
      Window w2 = page1.getWindow("Window_2");

      Map<Window, QName> s1 = new HashMap<Window, QName>();
      s1.put(w1, new QName("juju", "foo"));

      Map<Window, QName> d1 = new HashMap<Window, QName>();
      d1.put(w2, new QName("bobo", "bar"));

      cos.setEventWiring(s1, d1, "event1");

      Collection<EventWiringInfo> events = cos.getEventWirings(page1);
      assertNotNull(events);
      assertEquals(1, events.size());

      EventWiringInfo info = events.iterator().next();
      assertEquals("event1", info.getName());

      cos.renameEventWiring(info, "new");

      events = cos.getEventWirings(page1);
      assertEquals(1, events.size());
      info = events.iterator().next();
      assertEquals("new", info.getName());

      cos.renameEventWiring(page1, "new", "newer");

      info = cos.getEventWiring(page1, "newer");
      assertNotNull(info);

      TransactionAssert.commitTransaction();
   }

   public void testEventWiringSameWindowInBothSourcesAndDestinations() throws IllegalCoordinationException
   {
      TransactionAssert.beginTransaction();

      Page page1 = getPage1();

      Window w1 = page1.getWindow("Window_1");

      Map<Window, QName> s1 = new HashMap<Window, QName>();
      s1.put(w1, new QName("juju", "foo"));

      Map<Window, QName> d1 = new HashMap<Window, QName>();
      d1.put(page1.getWindow("Window_1"), new QName("bobo", "bar"));

      try
      {
         cos.setEventWiring(s1, d1, "event1");
         fail("Should have detected that Window_1 is in both sources and destinations!");
      }
      catch (IllegalCoordinationException e)
      {
         // expected
      }

      TransactionAssert.commitTransaction();
   }

   public void testRemoveEventWiring() throws IllegalCoordinationException
   {
      TransactionAssert.beginTransaction();

      Page page1 = getPage1();

      Window w1 = page1.getWindow("Window_1");
      Window w2 = page1.getWindow("Window_2");

      Map<Window, QName> s1 = new HashMap<Window, QName>();
      s1.put(w1, new QName("juju", "foo"));

      Map<Window, QName> d1 = new HashMap<Window, QName>();
      d1.put(w2, new QName("bobo", "bar"));

      cos.setEventWiring(s1, d1, "event1");
      cos.setEventWiring(s1, d1, "event2");

      Collection<EventWiringInfo> events = cos.getEventWirings(page1);
      assertNotNull(events);
      assertEquals(2, events.size());

      cos.removeEventWiring(page1, "event1");
      events = cos.getEventWirings(page1);
      assertEquals(1, events.size());
      assertNull(cos.getEventWiring(page1, "event1"));
      assertNotNull(cos.getEventWiring(page1, "event2"));

      TransactionAssert.commitTransaction();
   }

   public void testGetEventWiring() throws IllegalCoordinationException
   {
      TransactionAssert.beginTransaction();

      Page page1 = getPage1();

      Window w1 = page1.getWindow("Window_1");
      Window w2 = page1.getWindow("Window_2");
      Window w3 = page1.getWindow("Window_3");
      Window w4 = page1.getWindow("Window_4");

      Map<Window, QName> s1 = new HashMap<Window, QName>();
      Map<Window, QName> d1 = new HashMap<Window, QName>();
      QName source11 = new QName("source", "1");
      QName source12 = new QName("source", "2");
      s1.put(w1, source11);
      s1.put(w2, source12);
      QName dest11 = new QName("dest", "3");
      QName dest12 = new QName("dest", "4");
      d1.put(w3, dest11);
      d1.put(w4, dest12);

      cos.setEventWiring(s1, d1, "event1");

      s1 = new HashMap<Window, QName>();
      d1 = new HashMap<Window, QName>();
      QName source21 = new QName("source2", "1");
      s1.put(w1, source21);
      QName dest21 = new QName("dest2", "3");
      d1.put(w3, dest21);

      cos.setEventWiring(s1, d1, "event2");

      EventWiringInfo info = cos.getEventWiring(page1, "event1");
      assertNotNull(info);
      assertEquals("event1", info.getName());

      Map<Window, QName> sources = info.getSources();
      assertNotNull(sources);
      assertEquals(2, sources.size());
      assertEquals(source11, sources.get(w1));
      assertEquals(source12, sources.get(w2));
      Map<Window, QName> destinations = info.getDestinations();
      assertEquals(2, destinations.size());
      assertEquals(dest11, destinations.get(w3));
      assertEquals(dest12, destinations.get(w4));

      info = cos.getEventWiring(page1, "event2");
      assertNotNull(info);
      assertEquals("event2", info.getName());

      sources = info.getSources();
      assertNotNull(sources);
      assertEquals(1, sources.size());
      assertEquals(source21, sources.get(w1));
      destinations = info.getDestinations();
      assertEquals(1, destinations.size());
      assertEquals(dest21, destinations.get(w3));

      TransactionAssert.commitTransaction();
   }

   public void testRenameParameterBinding() throws Exception
   {
      TransactionAssert.beginTransaction();

      Page page1 = getPage1();

      Window w1 = page1.getWindow("Window_1");
      Window w2 = page1.getWindow("Window_2");

      Map<Window, Set<QName>> ws = new HashMap<Window, Set<QName>>();
      ws.put(w1, Collections.singleton(new QName("juju", "foo")));
      ws.put(w2, Collections.singleton(new QName("juju", "foo1")));

      cos.setParameterBinding("binding1", ws);

      Collection<? extends ParameterBindingInfo> bindings = cos.getParameterBindings(page1);
      assertEquals(1, bindings.size());

      ParameterBindingInfo info = bindings.iterator().next();
      assertEquals("binding1", info.getName());

      cos.renameParameterBinding(info, "new");

      bindings = cos.getParameterBindings(w1);
      assertEquals(1, bindings.size());
      info = bindings.iterator().next();
      assertEquals("new", info.getName());

      cos.renameParameterBinding(page1, "new", "newer");

      info = cos.getParameterBinding(page1, "newer");
      assertNotNull(info);

      TransactionAssert.commitTransaction();
   }

   public void testRenameAliasBinding() throws Exception
   {
      TransactionAssert.beginTransaction();

      Page page1 = getPage1();

      QName qname1 = new QName("nm1", "local1");
      QName qname2 = new QName("nm2", "local2");
      QName qname3 = new QName("nm3", "local3");

      HashSet<QName> qnames1 = new HashSet<QName>();
      qnames1.add(qname1);
      qnames1.add(qname2);
      qnames1.add(qname3);

      cos.setAliasBinding(page1, "alias1", qnames1);

      Collection<AliasBindingInfo> bindings1 = cos.getAliasBindings(page1);

      assertTrue(bindings1.size() == 1);
      AliasBindingInfo info = bindings1.iterator().next();

      assertEquals("alias1", info.getName());

      cos.renameAliasBinding(info, "new");
      bindings1 = cos.getAliasBindings(page1);
      assertEquals(1, bindings1.size());

      info = bindings1.iterator().next();
      assertEquals("new", info.getName());

      cos.renameAliasBinding(page1, "new", "newer");

      info = cos.getAliasBinding(page1, "newer");
      assertNotNull(info);

      TransactionAssert.commitTransaction();
   }

   public CoordinationService getCos()
   {
      return cos;
   }

   public void setCos(CoordinationService cos)
   {
      this.cos = cos;
   }

   private Page getPage1()
   {
      Portal portal1 = container.getContext("").getPortal("Portal_1");
      return portal1.getPage("Page_1");
   }
}

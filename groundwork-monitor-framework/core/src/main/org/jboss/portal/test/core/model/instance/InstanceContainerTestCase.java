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

/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.portal.test.core.model.instance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import junit.framework.TestSuite;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.jboss.portal.common.junit.TransactionAssert;
import org.jboss.portal.common.util.CollectionBuilder;
import org.jboss.portal.core.impl.model.instance.AbstractInstanceCustomization;
import org.jboss.portal.core.impl.model.instance.AbstractInstanceDefinition;
import org.jboss.portal.core.impl.model.instance.InstanceContainerImpl;
import org.jboss.portal.core.impl.portlet.state.PersistentPortletStatePersistenceManager;
import org.jboss.portal.core.model.instance.DuplicateInstanceException;
import org.jboss.portal.core.model.instance.Instance;
import org.jboss.portal.core.model.instance.InstanceDefinition;
import org.jboss.portal.portlet.NoSuchPortletException;
import org.jboss.portal.portlet.Portlet;
import org.jboss.portal.portlet.PortletContext;
import org.jboss.portal.portlet.PortletInvoker;
import org.jboss.portal.portlet.PortletInvokerException;
import org.jboss.portal.portlet.impl.spi.AbstractUserContext;
import org.jboss.portal.portlet.info.MetaInfo;
import org.jboss.portal.portlet.invocation.ActionInvocation;
import org.jboss.portal.portlet.invocation.PortletInvocation;
import org.jboss.portal.portlet.invocation.response.PortletInvocationResponse;
import org.jboss.portal.portlet.state.AbstractPropertyContext;
import org.jboss.portal.portlet.state.PropertyChange;
import org.jboss.portal.portlet.state.PropertyContext;
import org.jboss.portal.portlet.state.PropertyMap;
import org.jboss.portal.portlet.state.SimplePropertyMap;
import org.jboss.portal.portlet.state.producer.ProducerPortletInvoker;
import org.jboss.portal.security.RoleSecurityBinding;
import org.jboss.portal.security.spi.provider.DomainConfigurator;
import org.jboss.portal.test.framework.AbstractPortalTestCase;
import org.jboss.portal.test.framework.embedded.HibernateSupport;
import org.jboss.portal.test.framework.mc.TestRuntimeContext;

/**
 * Test Case that tests the authorization for instances via the instance container
 * <p/>
 * todo : configure instance with POP todo : configure instance with CCP todo : duplicate instance name todo : clone
 * before write without creating instance todo : destroy instance and all its children !!!!
 *
 * @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8872 $
 * @since Apr 4, 2006
 */
public class InstanceContainerTestCase extends AbstractPortalTestCase
{

   static
   {
      Appender appender = new ConsoleAppender(new SimpleLayout());
      Logger.getRoot().addAppender(appender);
      Logger.getRoot().setLevel(Level.ERROR);
      Logger.getLogger("org.hibernate").setLevel(Level.ERROR);
   }

   public static TestSuite suite() throws Exception
   {
      return AbstractPortalTestCase.suite(InstanceContainerTestCase.class);
   }

   private class TestPortletSupport extends PortletSupport
   {

      private PortletInvocation invocation;

      public TestPortletSupport()
      {
         super("Foo");
      }

      public PortletInvocationResponse invoke(PortletInvocation invocation)
      {
         this.invocation = invocation;
         try
         {
            return execute();
         }
         finally
         {
            this.invocation = null;
         }
      }

      public PortletInvocationResponse execute()
      {
         return null;
      }

      public void setProperty(String key, String value) throws IllegalStateException
      {
         AbstractPropertyContext props = (AbstractPropertyContext)invocation.getAttribute( PropertyContext.PREFERENCES_ATTRIBUTE);
         props.update(new PropertyChange[]{PropertyChange.newUpdate(key, value)});
      }
   }

   /** . */
   private boolean persistLocally;

   /** . */
   private boolean cloneOnCreate;

   /** . */
   private boolean cacheNaturalId;

   /** . */
   private HibernateSupport instanceHibernateSupport;

   /** . */
   private HibernateSupport portletHibernateSupport;

   /** . */
   private InstanceContainerImpl instanceContainer;

   /** . */
   private PersistentPortletStatePersistenceManager persistenceManager;

   /** . */
   private ProducerPortletInvoker producer;

   /** . */
   private PortletInvokerSupport portletContainer;

   /** . */
   private String config;

   public String getName()
   {
      return super.getName() + ",persistLocally=" + persistLocally + ",cacheNaturalId=" + cacheNaturalId + ",cloneOnCreate=" + cloneOnCreate;
   }

   public String getPersistLocallyParameter()
   {
      return Boolean.toString(persistLocally);
   }

   public void setPersistLocallyParameter(String persistLocallyParameter)
   {
      this.persistLocally = Boolean.valueOf(persistLocallyParameter).booleanValue();
   }

   public String getCloneOnCreateParameter()
   {
      return Boolean.toString(cloneOnCreate);
   }

   public void setCloneOnCreateParameter(String cloneOnCreateParameter)
   {
      this.cloneOnCreate = Boolean.valueOf(cloneOnCreateParameter).booleanValue();
   }

   public String getCacheNaturalIdParameter()
   {
      return Boolean.toString(cacheNaturalId);
   }

   public void setCacheNaturalIdParameter(String cacheNaturalIdParameter)
   {
      this.cacheNaturalId = Boolean.valueOf(cacheNaturalIdParameter).booleanValue();
   }

   public String getConfigParameter()
   {
      return config;
   }

   public void setConfigParameter(String configParameter)
   {
      this.config = configParameter;
   }

   public boolean isPersistLocally()
   {
      return persistLocally;
   }

   public boolean isCloneOnCreate()
   {
      return cloneOnCreate;
   }

   public boolean isCacheNaturalId()
   {
      return cacheNaturalId;
   }

   public HibernateSupport getInstanceHibernateSupport()
   {
      return instanceHibernateSupport;
   }

   public void setInstanceHibernateSupport(HibernateSupport instanceHibernateSupport)
   {
      this.instanceHibernateSupport = instanceHibernateSupport;
   }

   public HibernateSupport getPortletHibernateSupport()
   {
      return portletHibernateSupport;
   }

   public void setPortletHibernateSupport(HibernateSupport portletHibernateSupport)
   {
      this.portletHibernateSupport = portletHibernateSupport;
   }

   public InstanceContainerImpl getInstanceContainer()
   {
      return instanceContainer;
   }

   public void setInstanceContainer(InstanceContainerImpl instanceContainer)
   {
      this.instanceContainer = instanceContainer;
   }

   public PersistentPortletStatePersistenceManager getPersistenceManager()
   {
      return persistenceManager;
   }

   public void setPersistenceManager(PersistentPortletStatePersistenceManager persistenceManager)
   {
      this.persistenceManager = persistenceManager;
   }

   public ProducerPortletInvoker getProducer()
   {
      return producer;
   }

   public void setProducer(ProducerPortletInvoker portletInvoker)
   {
      this.producer = portletInvoker;
   }

   public PortletInvokerSupport getPortletContainer()
   {
      return portletContainer;
   }

   public void setPortletContainer(PortletInvokerSupport portletContainer)
   {
      this.portletContainer = portletContainer;
   }

   protected void configureRuntimeContext(TestRuntimeContext runtimeContext)
   {
      runtimeContext.addBean("TestCaseConfig", this);
   }

   protected String getConfigLocationPrefix()
   {
      return "org/jboss/portal/test/core/model/instance/";
   }

   protected void setUp() throws Exception
   {
      super.setUp();
   }

   protected void tearDown() throws Exception
   {
      super.tearDown();
   }

   public void testConfigureInstance() throws Exception
   {
      PortletInvokerSupport.InternalPortlet internalPortlet = portletContainer.addInternalPortlet("MyPortlet", new PortletSupport());
      java.util.List<String> list = new ArrayList<String>();
      list.add("def");
      internalPortlet.addPreference("abc", list);

      //
      TransactionAssert.beginTransaction();
      Instance instance = instanceContainer.createDefinition("MyInstance", "MyPortlet", true);
      assertNotNull(instance);
      TransactionAssert.commitTransaction();

      //
      TransactionAssert.beginTransaction();
//      Session session = instanceHibernateSupport.getCurrentSession();
//      List instances = session.createQuery("from InstanceDefinitionImpl").list();
//      assertEquals(1, instances.size());
//      InstanceDefinitionImpl instanceImpl = (InstanceDefinitionImpl)instances.get(0);
//      assertEquals(true, instanceImpl.isModifiable());
      assertEquals(1, instanceContainer.getDefinitions().size());
      AbstractInstanceDefinition instanceImpl = (AbstractInstanceDefinition)instanceContainer.getDefinition("MyInstance");
      assertNotNull(instanceImpl);
      assertEquals(true, instanceImpl.isModifiable());
      TransactionAssert.commitTransaction();

      //
      TransactionAssert.beginTransaction();
      instance = instanceContainer.getDefinition("MyInstance");
      instance.setProperties(new PropertyChange[]{PropertyChange.newUpdate("abc", "_def"), PropertyChange.newUpdate("ghi", "_jkl")});
      TransactionAssert.commitTransaction();

      //
      TransactionAssert.beginTransaction();
      instance = instanceContainer.getDefinition("MyInstance");
      PropertyMap props = instance.getProperties((Set)CollectionBuilder.hashSet().add("abc").add("ghi").get());
      PropertyMap expectedProps = new SimplePropertyMap();
      list = new ArrayList<String>();
      list.add("_def");
      expectedProps.setProperty("abc", list);
      list = new ArrayList<String>();
      list.add("_jkl");
      expectedProps.setProperty("ghi", list);
      ValueMapAssert.assertEquals(expectedProps, props);
      TransactionAssert.commitTransaction();

      //
      TransactionAssert.beginTransaction();
//      session = instanceHibernateSupport.getCurrentSession();
//      instances = session.createQuery("from AbstractInstanceDefinition").list();
//      assertEquals(1, instances.size());
//      instanceImpl = (AbstractInstanceDefinition)instances.get(0);
//      assertEquals(true, instanceImpl.isModifiable());
      assertEquals(1, instanceContainer.getDefinitions().size());
      instanceImpl = (AbstractInstanceDefinition)instanceContainer.getDefinition("MyInstance");
      assertNotNull(instanceImpl);
      assertEquals(true, instanceImpl.isModifiable());
      TransactionAssert.commitTransaction();
   }

   public void testConfigureInstanceWithoutPortlet() throws Exception
   {
      try
      {
         TransactionAssert.beginTransaction();
         instanceContainer.createDefinition("MyInstance", "UnknownPortlet");
         fail("Was expecting NoSuchPortletException");
      }
      catch (NoSuchPortletException expected)
      {
         // We expect that the transaction is marked for rollback
         TransactionAssert.rollbackTransaction();
      }

      //
      TransactionAssert.beginTransaction();
      assertEquals(0, instanceContainer.getDefinitions().size());
//      Session session = instanceHibernateSupport.getCurrentSession();
//      assertEquals(0, session.createQuery("from AbstractInstanceDefinition").list().size());
//      assertEquals(0, portletHibernateSupport.getCurrentSession().createQuery("from PersistentPortletState").list().size());
      TransactionAssert.commitTransaction();
   }

   public Portlet getSinglePOP() throws PortletInvokerException
   {
      assertNotNull(instanceContainer);
      PortletInvoker portletInvoker = instanceContainer.getPortletInvoker();
      assertNotNull(portletInvoker);
      Set portlets = portletInvoker.getPortlets();
      assertNotNull(portlets);
      assertEquals(1, portlets.size());
      Portlet p = (Portlet)portlets.iterator().next();
      assertNotNull(p);
      return p;
   }

   private abstract class TestCloneBeforeWrite
   {

      /** . */
      private final String identity;

      /** . */
      private final boolean cloneInstance;

      /** . */
      private final boolean expectException;

      /** . */
      private final TransactionAssert.Terminator terminator;

      public TestCloneBeforeWrite(String identity, boolean cloneInstance, boolean expectException, TransactionAssert.Terminator terminator)
      {
         this.identity = identity;
         this.cloneInstance = cloneInstance;
         this.expectException = expectException;
         this.terminator = terminator;
      }

      public abstract PortletInvocationResponse execute(TestPortletSupport portlet);

      public void execute() throws Exception
      {
         PortletInvokerSupport.InternalPortlet internalPortlet = portletContainer.addInternalPortlet("MyPortlet", new TestPortletSupport()
         {
            public PortletInvocationResponse execute()
            {
               return TestCloneBeforeWrite.this.execute(this);
            }
         });
         java.util.List<String> list = new ArrayList<String>();
         list.add("_def");
         internalPortlet.addPreference("_abc", list);
         String popId = getSinglePOP().getContext().getId();

         //
         TransactionAssert.beginTransaction();
         instanceContainer.createDefinition("MyInstance", popId, cloneInstance);
         TransactionAssert.commitTransaction();

         //
         TransactionAssert.beginTransaction();
         InstanceDefinition instanceDef = instanceContainer.getDefinition("MyInstance");
         Instance instance = instanceDef;
         if (identity != null)
         {
            instance = instanceDef.getCustomization(identity);
         }

         PortletInvocation action = new ActionInvocation(new PortletInvocationContextImpl());

         action.setUserContext(identity == null ? new AbstractUserContext() : new AbstractUserContext(identity));
         try
         {
            instance.invoke(action);
            if (expectException)
            {
               fail("Was expecting runtime exception");
            }
         }
         catch (RuntimeException e)
         {
            if (expectException)
            {
               assertEquals("custom_message", e.getMessage());
            }
            else
            {
               e.printStackTrace();
               fail("Was not expecting a runtime exception");
            }
         }
         TransactionAssert.endTransaction(terminator);
      }
   }

   /** . */
   public void testInvokePOPReadOnly() throws Exception
   {
      TestCloneBeforeWrite test = new TestCloneBeforeWrite(null, false, false, TransactionAssert.MUST_COMMIT)
      {
         public PortletInvocationResponse execute(TestPortletSupport portlet)
         {
            try
            {
               portlet.setProperty("abc", "def");
               fail("Was expecting an IllegalStateException");
            }
            catch (IllegalStateException expected)
            {
            }
            return null;
         }
      };
      test.execute();

      // Check state
      TransactionAssert.beginTransaction();
      AbstractInstanceDefinition instanceImpl = (AbstractInstanceDefinition)instanceContainer.getDefinition("MyInstance");
      assertNotNull(instanceImpl);
      Collection userInstances = instanceImpl.getCustomizations();
      assertNotNull(userInstances);
      assertEquals(0, userInstances.size());
      TransactionAssert.commitTransaction();
   }

   /** . */
   public void testInvokeCCPReadOnly() throws Exception
   {
      TestCloneBeforeWrite test = new TestCloneBeforeWrite(null, true, false, TransactionAssert.MUST_COMMIT)
      {
         public PortletInvocationResponse execute(TestPortletSupport portlet)
         {
            try
            {
               portlet.setProperty("abc", "def");
               fail("Was expecting an IllegalStateException");
            }
            catch (IllegalStateException expected)
            {
            }
            return null;
         }
      };
      test.execute();

      // Check state
      TransactionAssert.beginTransaction();
      AbstractInstanceDefinition instanceImpl = (AbstractInstanceDefinition)instanceContainer.getDefinition("MyInstance");
      assertNotNull(instanceImpl);
      Collection userInstances = instanceImpl.getCustomizations();
      assertNotNull(userInstances);
      assertEquals(0, userInstances.size());
      TransactionAssert.commitTransaction();
   }

   /** . */
   public void testInvokePOPCloneBeforeWrite() throws Exception
   {
      TestCloneBeforeWrite test = new TestCloneBeforeWrite("julien", false, false, TransactionAssert.MUST_COMMIT)
      {
         public PortletInvocationResponse execute(TestPortletSupport portlet)
         {
            portlet.setProperty("abc", "def");
            return null;
         }
      };
      test.execute();

      // Check state
      TransactionAssert.beginTransaction();
      AbstractInstanceDefinition instanceImpl = (AbstractInstanceDefinition)instanceContainer.getDefinition("MyInstance");
      assertNotNull(instanceImpl);
      Collection userInstances = instanceImpl.getCustomizations();
      assertNotNull(userInstances);
      assertEquals(1, userInstances.size());
      AbstractInstanceCustomization userInstance = (AbstractInstanceCustomization)userInstances.iterator().next();
      PortletContext userPortletContext = userInstance.getPortletContext();
      assertNotNull(userPortletContext);
      PropertyMap userProps = instanceContainer.getPortletInvoker().getProperties(userPortletContext);
      assertNotNull(userProps);
      PropertyMap expectedProps = new SimplePropertyMap();
      java.util.List<String> list = new ArrayList<String>();
      list.add("def");
      expectedProps.setProperty("abc", list);
      list = new ArrayList<String>();
      list.add("_def");
      expectedProps.setProperty("_abc", list);
      ValueMapAssert.assertEquals(expectedProps, userProps);
      Portlet userPortlet = instanceContainer.getPortletInvoker().getPortlet(userPortletContext);
      assertNotNull(userPortlet);
      assertEquals("Foo", userPortlet.getInfo().getMeta().getMetaValue(MetaInfo.DISPLAY_NAME).getDefaultString());
      TransactionAssert.commitTransaction();

      // Erase state
      TransactionAssert.beginTransaction();
      instanceImpl = (AbstractInstanceDefinition)instanceContainer.getDefinition("MyInstance");
      instanceImpl.destroyCustomization("julien");
      TransactionAssert.commitTransaction();

      // Check state has been destroyed on consumer and producer
      TransactionAssert.beginTransaction();
      instanceImpl = (AbstractInstanceDefinition)instanceContainer.getDefinition("MyInstance");
      assertNotNull(instanceImpl);
      userInstances = instanceImpl.getCustomizations();
      assertNotNull(userInstances);
      assertEquals(0, userInstances.size());
      if (persistLocally)
      {
         try
         {
            instanceContainer.getPortletInvoker().getProperties(userPortletContext);
            fail("Was expecting a NoSuchPortletException to be thrown");
         }
         catch (NoSuchPortletException expected)
         {
         }
      }
      else
      {
         instanceContainer.getPortletInvoker().getProperties(userPortletContext);
      }
      TransactionAssert.commitTransaction();
   }

   /** . */
   public void testInvokeCCPCloneBeforeWrite() throws Exception
   {
      TestCloneBeforeWrite test = new TestCloneBeforeWrite("julien", true, false, TransactionAssert.MUST_COMMIT)
      {
         public PortletInvocationResponse execute(TestPortletSupport portlet)
         {
            portlet.setProperty("abc", "def");
            return null;
         }
      };
      test.execute();

      // Check state
      TransactionAssert.beginTransaction();
      AbstractInstanceDefinition instanceImpl = (AbstractInstanceDefinition)instanceContainer.getDefinition("MyInstance");
      assertNotNull(instanceImpl);
      Collection userInstances = instanceImpl.getCustomizations();
      assertNotNull(userInstances);
      assertEquals(1, userInstances.size());
      AbstractInstanceCustomization userInstance = (AbstractInstanceCustomization)userInstances.iterator().next();
      PortletContext userPortletContext = userInstance.getPortletContext();
      assertNotNull(userPortletContext);
      PropertyMap userProps = instanceContainer.getPortletInvoker().getProperties(userPortletContext);
      assertNotNull(userProps);
      PropertyMap expectedProps = new SimplePropertyMap();
      java.util.List<String> list = new ArrayList<String>();
      list.add("def");
      expectedProps.setProperty("abc", list);
      list = new ArrayList<String>();
      list.add("_def");
      expectedProps.setProperty("_abc", list);
      ValueMapAssert.assertEquals(expectedProps, userProps);
      Portlet userPortlet = instanceContainer.getPortletInvoker().getPortlet(userPortletContext);
      assertNotNull(userPortlet);
      assertEquals("Foo", userPortlet.getInfo().getMeta().getMetaValue(MetaInfo.DISPLAY_NAME).getDefaultString());
      TransactionAssert.commitTransaction();

      // Erase state
      TransactionAssert.beginTransaction();
      instanceImpl = (AbstractInstanceDefinition)instanceContainer.getDefinition("MyInstance");
      instanceImpl.destroyCustomization("julien");
      TransactionAssert.commitTransaction();

      // Check state has been destroyed on consumer and producer
      TransactionAssert.beginTransaction();
      instanceImpl = (AbstractInstanceDefinition)instanceContainer.getDefinition("MyInstance");
      assertNotNull(instanceImpl);
      userInstances = instanceImpl.getCustomizations();
      assertNotNull(userInstances);
      assertEquals(0, userInstances.size());
      if (persistLocally)
      {
         try
         {
            instanceContainer.getPortletInvoker().getProperties(userPortletContext);
            fail("Was expecting a NoSuchPortletException to be thrown");
         }
         catch (NoSuchPortletException expected)
         {
         }
      }
      else
      {
         instanceContainer.getPortletInvoker().getProperties(userPortletContext);
      }
      TransactionAssert.commitTransaction();
   }

   /** . */
   public void testInvokePOPCloneBeforeWriteRollback() throws Exception
   {
      TestCloneBeforeWrite test = new TestCloneBeforeWrite("julien", false, false, TransactionAssert.MUST_ROLLBACK)
      {
         public PortletInvocationResponse execute(TestPortletSupport portlet)
         {
            portlet.setProperty("abc", "def");
            return null;
         }
      };
      test.execute();

      // Check state
      TransactionAssert.beginTransaction();
      AbstractInstanceDefinition instanceImpl = (AbstractInstanceDefinition)instanceContainer.getDefinition("MyInstance");
      Collection userInstances = instanceImpl.getCustomizations();
      assertNotNull(userInstances);
      assertEquals(0, userInstances.size());
      TransactionAssert.commitTransaction();
   }

   /** . */
   public void testInvokeCCPCloneBeforeWriteRollback() throws Exception
   {
      TestCloneBeforeWrite test = new TestCloneBeforeWrite("julien", true, false, TransactionAssert.MUST_ROLLBACK)
      {
         public PortletInvocationResponse execute(TestPortletSupport portlet)
         {
            portlet.setProperty("abc", "def");
            return null;
         }
      };
      test.execute();

      // Check state
      TransactionAssert.beginTransaction();
      AbstractInstanceDefinition instanceImpl = (AbstractInstanceDefinition)instanceContainer.getDefinition("MyInstance");
      Collection userInstances = instanceImpl.getCustomizations();
      assertNotNull(userInstances);
      assertEquals(0, userInstances.size());
      TransactionAssert.commitTransaction();
   }

   /** . */
   public void testInvokePOPCloneBeforeWritePortletThrowsRuntimeException() throws Exception
   {
      TestCloneBeforeWrite test = new TestCloneBeforeWrite("julien", false, true, TransactionAssert.MARKED_AS_ROLLBACK)
      {
         public PortletInvocationResponse execute(TestPortletSupport portlet)
         {
            portlet.setProperty("abc", "def");
            throw new RuntimeException("custom_message");
         }
      };
      test.execute();

      // Check state
      TransactionAssert.beginTransaction();
      AbstractInstanceDefinition instanceImpl = (AbstractInstanceDefinition)instanceContainer.getDefinition("MyInstance");
      Collection userInstances = instanceImpl.getCustomizations();
      assertNotNull(userInstances);
      assertEquals(0, userInstances.size());
      TransactionAssert.commitTransaction();
   }

   /** . */
   public void testInvokeCCPCloneBeforeWritePortletThrowsRuntimeException() throws Exception
   {
      TestCloneBeforeWrite test = new TestCloneBeforeWrite("julien", true, true, TransactionAssert.MARKED_AS_ROLLBACK)
      {
         public PortletInvocationResponse execute(TestPortletSupport portlet)
         {
            portlet.setProperty("abc", "def");
            throw new RuntimeException("custom_message");
         }
      };
      test.execute();

      // Check state
      TransactionAssert.beginTransaction();
      AbstractInstanceDefinition instanceImpl = (AbstractInstanceDefinition)instanceContainer.getDefinition("MyInstance");
      Collection userInstances = instanceImpl.getCustomizations();
      assertNotNull(userInstances);
      assertEquals(0, userInstances.size());
      TransactionAssert.commitTransaction();
   }

   public void testDestroyCCPInstance() throws Exception
   {
      PortletInvokerSupport.InternalPortlet internalPortlet = portletContainer.addInternalPortlet("MyPortlet", new TestPortletSupport()
      {
         public PortletInvocationResponse execute()
         {
            setProperty("abc", "def");
            return null;
         }
      });
      java.util.List<String> list = new ArrayList<String>();
      list.add("_def");
      internalPortlet.addPreference("_abc", list);

      // Create the instance
      TransactionAssert.beginTransaction();
      instanceContainer.createDefinition("MyInstance", "MyPortlet", true);
      TransactionAssert.commitTransaction();

      // Create a clone for a user
      TransactionAssert.beginTransaction();
      Instance instance = instanceContainer.getDefinition("MyInstance").getCustomization("julien");
      PortletInvocation action = new ActionInvocation(new PortletInvocationContextImpl());
      action.setUserContext(new AbstractUserContext("julien"));
      instance.invoke(action);
      TransactionAssert.commitTransaction();

      //
      TransactionAssert.beginTransaction();
      AbstractInstanceDefinition instanceImpl = (AbstractInstanceDefinition)instanceContainer.getDefinition("MyInstance");
      PortletContext sharedPortletContext = instanceImpl.getPortletContext();
      assertNotNull(producer.getPortlet(sharedPortletContext));
      assertNotNull(instance);
      Collection children = instanceImpl.getCustomizations();
      assertNotNull(children);
      assertEquals(1, children.size());
      AbstractInstanceCustomization userInstance = (AbstractInstanceCustomization)children.iterator().next();
      PortletContext userPortletContext = userInstance.getPortletContext();
      assertNotNull(producer.getPortlet(userPortletContext));
      assertNotNull(userInstance);
      TransactionAssert.commitTransaction();

      //
      TransactionAssert.beginTransaction();
      instanceContainer.destroyDefinition("MyInstance");
      TransactionAssert.commitTransaction();

      //
      if (persistLocally)
      {
         TransactionAssert.beginTransaction();
         try
         {
            producer.getPortlet(userPortletContext);
            fail("Was expecting a NoSuchPortletException");
         }
         catch (NoSuchPortletException expected)
         {
         }
         try
         {
            producer.getPortlet(sharedPortletContext);
            fail("Was expecting a NoSuchPortletException");
         }
         catch (NoSuchPortletException expected)
         {
         }
         TransactionAssert.commitTransaction();
      }
   }

   public void testRecreate() throws Exception
   {
      PortletInvokerSupport.InternalPortlet internalPortlet = portletContainer.addInternalPortlet("MyPortlet", new TestPortletSupport());
      java.util.List<String> list = new ArrayList<String>();
      list.add("_def");
      internalPortlet.addPreference("_abc", list);

      //
      TransactionAssert.beginTransaction();
      instanceContainer.createDefinition("MyInstance", "MyPortlet");
      TransactionAssert.commitTransaction();

      //
      TransactionAssert.beginTransaction();
      Instance instance = instanceContainer.getDefinition("MyInstance");
      assertNotNull(instance);
      TransactionAssert.commitTransaction();

      //
      TransactionAssert.beginTransaction();
      instanceContainer.destroyDefinition("MyInstance");
      instanceContainer.createDefinition("MyInstance", "MyPortlet");
      TransactionAssert.commitTransaction();

      //
      TransactionAssert.beginTransaction();
      instance = instanceContainer.getDefinition("MyInstance");
      assertNotNull(instance);
      TransactionAssert.commitTransaction();
   }

   public void testCreateDefinitionThrowsDuplicateInstanceException() throws PortletInvokerException, DuplicateInstanceException
   {
      PortletInvokerSupport.InternalPortlet internalPortlet = portletContainer.addInternalPortlet("MyPortlet", new TestPortletSupport());
      java.util.List<String> list = new ArrayList<String>();
      list.add("_def");
      internalPortlet.addPreference("_abc", list);

      //
      TransactionAssert.beginTransaction();
      instanceContainer.createDefinition("MyInstance", "MyPortlet");
      TransactionAssert.commitTransaction();

      TransactionAssert.beginTransaction();
      try
      {
         instanceContainer.createDefinition("MyInstance", "MyPortlet");
         fail("Was expecting a DuplicateInstanceException");
      }
      catch (DuplicateInstanceException expected)
      {
         // Duplicate instance exception does not mark the transaction as rollback
         TransactionAssert.commitTransaction();
      }
   }

   /** . */
   private final Set securityBindings1 = Collections.unmodifiableSet((Set)CollectionBuilder.hashSet().add(new RoleSecurityBinding("a,b", "admin")).add(new RoleSecurityBinding("a", "user")).get());

   /** . */
   private final Set securityBindings2 = Collections.unmodifiableSet((Set)CollectionBuilder.hashSet().add(new RoleSecurityBinding("a,b", "user")).get());

   public void testSecurityConfiguration() throws Exception
   {
      portletContainer.addInternalPortlet("MyPortlet", new TestPortletSupport());

      //
      TransactionAssert.beginTransaction();
      String instanceId = instanceContainer.createDefinition("MyInstance", "MyPortlet").getId();
      TransactionAssert.commitTransaction();

      //
      DomainConfigurator configurator = instanceContainer.getConfigurator();
      assertNotNull(configurator);

      //
      TransactionAssert.beginTransaction();
      configurator.setSecurityBindings(instanceId, securityBindings1);
      TransactionAssert.commitTransaction();

      //
      TransactionAssert.beginTransaction();
      Set bindings = configurator.getSecurityBindings(instanceId);
      assertNotNull(bindings);
      assertEquals(securityBindings1, bindings);
      TransactionAssert.commitTransaction();

      //
      TransactionAssert.beginTransaction();
      configurator.setSecurityBindings(instanceId, securityBindings2);
      TransactionAssert.commitTransaction();

      //
      TransactionAssert.beginTransaction();
      bindings = configurator.getSecurityBindings(instanceId);
      assertNotNull(bindings);
      assertEquals(securityBindings2, bindings);
      TransactionAssert.commitTransaction();

      //
      TransactionAssert.beginTransaction();
      configurator.removeSecurityBindings(instanceId);
      TransactionAssert.commitTransaction();

      //
      TransactionAssert.beginTransaction();
      bindings = configurator.getSecurityBindings(instanceId);
      assertNotNull(bindings);
      assertEquals(0, bindings.size());
      TransactionAssert.commitTransaction();
   }

   public void testDestroyInstanceWithSecurityConfiguration() throws Exception
   {
      portletContainer.addInternalPortlet("MyPortlet", new TestPortletSupport());

      //
      TransactionAssert.beginTransaction();
      String instanceId = instanceContainer.createDefinition("MyInstance", "MyPortlet").getId();
      TransactionAssert.commitTransaction();

      //
      DomainConfigurator configurator = instanceContainer.getConfigurator();
      assertNotNull(configurator);

      //
      TransactionAssert.beginTransaction();
      configurator.setSecurityBindings(instanceId, securityBindings1);
      TransactionAssert.commitTransaction();

      //
      TransactionAssert.beginTransaction();
      instanceContainer.destroyDefinition(instanceId);
      TransactionAssert.commitTransaction();
   }




//
////   /**Tests the authorization of portal objects */
////   public void testInstanceAuthorization() throws Exception
////   {
////      container.start();
////
////      //Populate the container with constraints
////      hibernate.beginTransaction();
////      constructInstances();
////      setUpSecurity("portletInstanceA","view", SecurityConstants.UNCHECKED_ROLE_NAME);
////      setUpSecurity("portletInstanceB","view", SecurityConstants.UNCHECKED_ROLE_NAME);
////      setUpSecurity("portletInstanceC","view", SecurityConstants.UNCHECKED_ROLE_NAME);
////      hibernate.commitTransaction();
////
////      hibernate.beginTransaction();
////      SecurityAssociation.setSubject(new Subject());
////      PortalAuthorizationManager manager = container.getPortalAuthorizationManagerFactory().getManager();
////      assertTrue("view perm on portletInstanceA",  manager.hasPermission("portletInstanceA", "view",type));
////      assertTrue("view perm on portletInstanceB",  manager.hasPermission("portletInstanceB", "view",type));
////      assertTrue("view perm on portletInstanceC",  manager.hasPermission("portletInstanceC", "view",type));
////      hibernate.commitTransaction();
////      container.stop();
////   }
////
////   /**Tests the authorization of portal objects */
////   public void testInstanceAuthorizationForAdmin() throws Exception
////   {
////      container.start();
////
////      //Populate the container with constraints
////      hibernate.beginTransaction();
////      constructInstances();
////      setUpSecurity("portletInstanceA","view", "admin");
////      setUpSecurity("portletInstanceB","view", "admin");
////      setUpSecurity("portletInstanceC","view", "admin");
////      hibernate.commitTransaction();
////
////      hibernate.beginTransaction();
////      setUpSubjectForRole("admin",new String[]{"admin"});
////
////      PortalAuthorizationManager manager = container.getPortalAuthorizationManagerFactory().getManager();
////      assertTrue("view perm on portletInstanceA",
////            manager.hasPermission("portletInstanceA", "view",type));
////      assertTrue("view perm on portletInstanceB",
////            manager.hasPermission("portletInstanceB", "view",type));
////      assertTrue("view perm on portletInstanceC",
////            manager.hasPermission("portletInstanceC", "view",type));
////      hibernate.commitTransaction();
////      container.stop();
////   }
////
//
//
////
////
////   /**
////    * Should be automated to construct from a xxx-yyy.xml
////    * Example : portal-object.xml
////    */
////   private void constructInstances() throws Exception
////   {
////      assertNotNull("portletInstanceA is not null", constructInstance("portletInstanceA", null));
////      assertNotNull("portletInstanceB is not null", constructInstance("portletInstanceB", null));
////      assertNotNull("portletInstanceC is not null", constructInstance("portletInstanceC", null));
////   }
////
////
////   private InstanceImpl constructInstance(String instanceA, String portletId) throws DuplicateInstanceException, NoSuchPortletException
////   {
////      InstanceImpl instanceImpl = (InstanceImpl)container.getInstance(instanceA);
////      if(instanceImpl == null)
////      {
////         instanceImpl = (InstanceImpl)container.createInstances(instanceA, portletId);
////      }
////      return instanceImpl;
////   }
////
////
////   private void setUpSecurity(String uri,String perm, String role) throws  Exception
////   {
////      PolicyContext.setContextID(CONTEXT_ID);
////      AuthorizationDomain authDomain = container.getAuthorizationDomain();
////      assertNotNull("AuthorizationDomain != null", authDomain);
////      DomainConfigurator dc = authDomain.getConfigurator();
////      assertNotNull("DomainConfigurator != null", dc);
////      JACCPortalAuthorizationManagerFactory factory = new JACCPortalAuthorizationManagerFactory();
////      JBossAuthorizationDomainRegistryImpl registry = new JBossAuthorizationDomainRegistryImpl();
////      registry.addDomain(container);
////      factory.setAuthorizationDomainRegistry(registry);
////      container.setAuthorizationDomainRegistry(registry);
////      container.setPortalAuthorizationManagerFactory(factory);
////      dc.setConstraints(uri, this.getSecurityConstraints(perm,role));
////   }
}

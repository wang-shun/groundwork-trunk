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
package org.jboss.portal.test.core.state;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.jboss.portal.Mode;
import org.jboss.portal.common.junit.TransactionAssert;
import org.jboss.portal.core.impl.portlet.state.PersistentPortletState;
import org.jboss.portal.core.impl.portlet.state.PersistentPortletStatePersistenceManager;
import org.jboss.portal.core.impl.portlet.state.PersistentRegistration;
import org.jboss.portal.core.impl.portlet.state.ProducerPortletInvoker;
import org.jboss.portal.portlet.NoSuchPortletException;
import org.jboss.portal.portlet.Portlet;
import org.jboss.portal.portlet.PortletContext;
import org.jboss.portal.portlet.PortletInvoker;
import org.jboss.portal.portlet.impl.spi.AbstractInstanceContext;
import org.jboss.portal.portlet.impl.spi.AbstractUserContext;
import org.jboss.portal.portlet.info.MetaInfo;
import org.jboss.portal.portlet.invocation.ActionInvocation;
import org.jboss.portal.portlet.invocation.PortletInvocation;
import org.jboss.portal.portlet.invocation.response.PortletInvocationResponse;
import org.jboss.portal.portlet.state.AbstractPropertyContext;
import org.jboss.portal.portlet.state.AccessMode;
import org.jboss.portal.portlet.state.DestroyCloneFailure;
import org.jboss.portal.portlet.state.PropertyChange;
import org.jboss.portal.portlet.state.PropertyContext;
import org.jboss.portal.portlet.state.PropertyMap;
import org.jboss.portal.portlet.state.SimplePropertyMap;
import org.jboss.portal.portlet.state.producer.PortletState;
import org.jboss.portal.portlet.state.producer.PortletStateContext;
import org.jboss.portal.registration.Consumer;
import org.jboss.portal.registration.ConsumerGroup;
import org.jboss.portal.registration.Registration;
import org.jboss.portal.registration.RegistrationLocal;
import org.jboss.portal.test.core.model.instance.PortletInvocationContextImpl;
import org.jboss.portal.test.core.model.instance.PortletInvokerSupport;
import org.jboss.portal.test.core.model.instance.PortletSupport;
import org.jboss.portal.test.core.model.instance.ValueMapAssert;
import org.jboss.portal.test.framework.TestParametrization;
import org.jboss.portal.test.framework.embedded.DataSourceSupport;
import org.jboss.portal.test.framework.embedded.HibernateSupport;
import org.jboss.portal.test.framework.junit.JUnitAdapter;
import org.jboss.portal.test.framework.junit.POJOJUnitTest;
import org.jboss.portal.test.framework.mc.TestRuntimeContext;

import javax.xml.namespace.QName;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class ProducerTestCase extends TestCase
{

   static
   {
      Appender appender = new ConsoleAppender(new SimpleLayout());
      Logger.getRoot().addAppender(appender);
      Logger.getRoot().setLevel(Level.DEBUG);
      Logger.getLogger("org.hibernate").setLevel(Level.ERROR);
   }

   public static TestSuite suite() throws Exception
   {
      TestParametrization parametrization = JUnitAdapter.getParametrization();
      URL configsURL = Thread.currentThread().getContextClassLoader().getResource("datasources.xml");
      parametrization.setParameterValue("DataSourceConfig", DataSourceSupport.Config.fromXML2(configsURL));
      POJOJUnitTest abc = new POJOJUnitTest(ProducerTestCase.class);
      JUnitAdapter adapter = new JUnitAdapter(abc, parametrization);
      TestSuite suite = new TestSuite();
      suite.addTest(adapter);
      return suite;
   }

   /** Test parameter whether we test registration or not. */
   private boolean useRegistration;

   /** . */
   private TestRuntimeContext runtimeContext;

   /** . */
   private DataSourceSupport.Config dataSourceConfigParameter;

   /** . */
   private HibernateSupport hibernateSupport;

   /** The persistence manager of the producer. */
   private PersistentPortletStatePersistenceManager persistenceManager;

   /** The producer. */
   private ProducerPortletInvoker producer;

   /** The portlet container. */
   private PortletInvokerSupport portletContainer;

   /** The consumer. */
   private PortletInvoker consumer;

   /** The registration id created during the setup. */
   private String registrationId;

   public String getName()
   {
      return super.getName() + ",ds=" + dataSourceConfigParameter.getName();
   }

   public String getUseRegistrationParameter()
   {
      return Boolean.toString(useRegistration);
   }

   public void setUseRegistrationParameter(String useRegistrationParameter)
   {
      this.useRegistration = Boolean.valueOf(useRegistrationParameter).booleanValue();
   }

   public HibernateSupport getHibernateSupport()
   {
      return hibernateSupport;
   }

   public void setHibernateSupport(HibernateSupport hibernateSupport)
   {
      this.hibernateSupport = hibernateSupport;
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

   public void setProducer(ProducerPortletInvoker producer)
   {
      this.producer = producer;
   }

   public PortletInvokerSupport getPortletContainer()
   {
      return portletContainer;
   }

   public void setPortletContainer(PortletInvokerSupport portletContainer)
   {
      this.portletContainer = portletContainer;
   }

   public DataSourceSupport.Config getDataSourceConfigParameter()
   {
      return dataSourceConfigParameter;
   }

   public void setDataSourceConfigParameter(DataSourceSupport.Config dataSourceConfigParameter)
   {
      this.dataSourceConfigParameter = dataSourceConfigParameter;
   }

   public PortletInvoker getConsumer()
   {
      return consumer;
   }

   public void setConsumer(PortletInvoker consumer)
   {
      this.consumer = consumer;
   }

   public void setUp() throws Exception
   {
      runtimeContext = new TestRuntimeContext("org/jboss/portal/test/core/state/jboss-beans.xml");
      runtimeContext.addBean("TestBean", this);
      runtimeContext.addBean("DataSourceConfig", dataSourceConfigParameter);
      runtimeContext.addBean("HibernateConfig", HibernateSupport.getConfig(dataSourceConfigParameter.getName()));
      runtimeContext.start();

      PortletInvokerSupport.InternalPortlet internalSimplePortlet = portletContainer.addInternalPortlet("SimplePortlet", new PortletSupport("SimplePortlet")
      {
         public PortletInvocationResponse invoke(PortletInvocation invocation)
         {
            AbstractPropertyContext props = (AbstractPropertyContext)invocation.getAttribute(PropertyContext.PREFERENCES_ATTRIBUTE);
            List<String> list = new ArrayList<String>();
            list.add("_def");
            props.update(new PropertyChange[]{PropertyChange.newUpdate("_abc", list)});
            return null;
         }
      });
      List<String> list = new ArrayList<String>();
      list.add("def");
      internalSimplePortlet.addPreference("abc", list);

      PortletInvokerSupport.InternalPortlet internalCloningPortlet = portletContainer.addInternalPortlet("CloningPortlet", new PortletSupport()
      {
         public PortletInvocationResponse invoke(PortletInvocation invocation)
         {
            AbstractPropertyContext props = (AbstractPropertyContext)invocation.getAttribute(PropertyContext.PREFERENCES_ATTRIBUTE);
            List<String> list = new ArrayList<String>();
            list.add("_def");
            props.update(new PropertyChange[]{PropertyChange.newUpdate("_abc", list)});
            return null;
         }
      });
      list = new ArrayList<String>();
      list.add("def");
      internalCloningPortlet.addPreference("abc", list);

      PortletInvokerSupport.InternalPortlet internalCloneFailedCloningPortlet = portletContainer.addInternalPortlet("CloneFailedCloningPortlet", new PortletSupport()
      {
         public PortletInvocationResponse invoke(PortletInvocation invocation)
         {
            try
            {
               AbstractPropertyContext props = (AbstractPropertyContext)invocation.getAttribute(PropertyContext.PREFERENCES_ATTRIBUTE);
               List<String> list = new ArrayList<String>();
               list.add("_def");
               props.update(new PropertyChange[]{PropertyChange.newUpdate("_abc", list)});
               fail("Was expecting an IllegalStateException");
            }
            catch (IllegalStateException expected)
            {
            }
            return null;
         }
      });
      list = new ArrayList<String>();
      list.add("def");
      internalCloneFailedCloningPortlet.addPreference("abc", list);

      PortletInvokerSupport.InternalPortlet internalCloningPortletThrowingRuntimeException = portletContainer.addInternalPortlet("CloningPortletThrowingRuntimeException", new PortletSupport()
      {
         public PortletInvocationResponse invoke(PortletInvocation invocation)
         {
            AbstractPropertyContext props = (AbstractPropertyContext)invocation.getAttribute(PropertyContext.PREFERENCES_ATTRIBUTE);
            List<String> list = new ArrayList<String>();
            list.add("_def");
            props.update(new PropertyChange[]{PropertyChange.newUpdate("_abc", list)});
            throw new RuntimeException("custom_message");
         }
      });
      list = new ArrayList<String>();
      list.add("def");
      internalCloningPortletThrowingRuntimeException.addPreference("abc", list);

      // Create registration
      if (useRegistration)
      {
         beginTX();
         ConsumerGroup cg = persistenceManager.createConsumerGroup("CG");
         Consumer consumer = persistenceManager.createConsumer("fooConsumer", "fooConsumer");
         cg.addConsumer(consumer);
         Map registrationProperties = new HashMap();
         registrationProperties.put(new QName("prop1"), "value1");
         registrationProperties.put(new QName("prop2"), "value2");
         Registration reg = persistenceManager.addRegistrationFor("fooConsumer", registrationProperties);
         registrationId = reg.getId();
         commitTX();
      }
   }

   public void tearDown() throws Exception
   {
      // Cleanup any pending transaction
      TransactionAssert.endTransaction();

      //
      runtimeContext.stop();
   }

   public void beginTX()
   {
      TransactionAssert.beginTransaction();
   }

   public void beginRegistrationScopedTX()
   {
      TransactionAssert.beginTransaction();

      //
      if (useRegistration)
      {
         Registration reg = persistenceManager.getRegistration(registrationId);
         RegistrationLocal.setRegistration(reg);
      }
   }

   private void rollbackTX()
   {
      TransactionAssert.rollbackTransaction(true);

      //
      if (useRegistration)
      {
         RegistrationLocal.setRegistration(null);
      }
   }

   public void commitTX()
   {
      TransactionAssert.commitTransaction();

      //
      if (useRegistration)
      {
         RegistrationLocal.setRegistration(null);
      }
   }

   public void testCloneExistingPOPWithinTx() throws Exception
   {
      // Clone a POP
      beginRegistrationScopedTX();
      PortletContext cloneCtx = consumer.createClone(PortletContext.createPortletContext("SimplePortlet"));
      commitTX();

      // Check the clone state
      beginTX();
      assertTrue(cloneCtx.getId().startsWith("_"));
      PersistentPortletState cloneState = (PersistentPortletState)persistenceManager.loadState(cloneCtx.getId().substring(1));
      assertNotNull(cloneState);
      assertEquals(cloneCtx.getId(), "_" + cloneState.getId());
      assertEquals("SimplePortlet", cloneState.getState().getPortletId());
      PropertyMap cloneValues = cloneState.getState().getProperties();
      assertNotNull(cloneValues);
      assertNotNull(cloneValues.keySet());
      assertEquals(1, cloneValues.keySet().size());
      List<String> list = new ArrayList<String>();
      list.add("def");
      assertEquals(list, cloneValues.getProperty("abc"));
      if (useRegistration)
      {
         PersistentRegistration registration = cloneState.getRelatedRegistration();
         assertNotNull(registration);
         assertEquals(registrationId, registration.getId());
      }
      commitTX();
   }

   public void testCloneExistingCCPWithinTx() throws Exception
   {
      // Clone a POP
      beginTX();
      PortletContext cloneCtx = consumer.createClone(PortletContext.createPortletContext("SimplePortlet"));
      commitTX();

      // Update CCP state directly
      beginTX();
      PropertyMap cloneValues = consumer.getProperties(cloneCtx);
      PropertyMap newCloneValues = new SimplePropertyMap(cloneValues);
      List<String> list = new ArrayList<String>();
      list.add("fed");
      newCloneValues.setProperty("abc", list);
      persistenceManager.updateState(cloneCtx.getId().substring(1), newCloneValues);
      commitTX();

      // Clone the modified CCP
      beginRegistrationScopedTX();
      PortletContext cloneCloneCtx = consumer.createClone(cloneCtx);
      commitTX();

      // Check the clone clone state
      beginTX();
      assertTrue(cloneCloneCtx.getId().startsWith("_"));
      PersistentPortletState cloneCloneState = (PersistentPortletState)persistenceManager.loadState(cloneCloneCtx.getId().substring(1));
      assertNotNull(cloneCloneState);
      assertEquals(cloneCloneCtx.getId(), "_" + cloneCloneState.getId());
      assertEquals("SimplePortlet", cloneCloneState.getState().getPortletId());
      PropertyMap cloneCloneValues = cloneCloneState.getState().getProperties();
      assertNotNull(cloneCloneValues);
      assertNotNull(cloneCloneValues.keySet());
      assertEquals(1, cloneCloneValues.keySet().size());
      list = new ArrayList<String>();
      list.add("fed");
      assertEquals(list, cloneCloneValues.getProperty("abc"));
      if (useRegistration)
      {
         PersistentRegistration registration = cloneCloneState.getRelatedRegistration();
         assertNotNull(registration);
         assertEquals(registrationId, registration.getId());
      }
      commitTX();
   }

   public void testCloneNullPortletWithinTx() throws Exception
   {
      try
      {
         beginRegistrationScopedTX();
         consumer.createClone(null);
         fail("Was expecting an IllegalArgumentException");
      }
      catch (IllegalArgumentException expected)
      {
         rollbackTX();
      }
   }

   public void testDestroyNonExistingPortletWithinTx() throws Exception
   {
      beginRegistrationScopedTX();
      List failures = consumer.destroyClones(Collections.singletonList(PortletContext.createPortletContext("_1")));
      assertEquals(Collections.singletonList(new DestroyCloneFailure("_1")), failures);
      commitTX();
   }

   public void testDestroyNullPortletWithinTx() throws Exception
   {
      try
      {
         beginRegistrationScopedTX();
         consumer.destroyClones(null);
         fail("Was expecting an IllegalArgumentException");
      }
      catch (IllegalArgumentException expected)
      {
         rollbackTX();
      }
   }

/*
   public void _testDestroyInvalidPortletWithinTx() throws Exception
   {
      try
      {
         TransactionAssert.beginTransaction();
         statefulPortletInvoker.destroyClone("_invalid");
         fail("Was expecting an InvalidPortletIdException");
      }
      catch (InvalidPortletIdException expected)
      {
         TransactionAssert.rollbackTransaction(true);
      }
   }
*/

   public void testDestroyCCPWithinTx() throws Exception
   {
      // Clone a POP 2 times
      beginTX();
      PortletContext clone1 = consumer.createClone(PortletContext.createPortletContext("SimplePortlet"));
      PortletContext clone2 = consumer.createClone(PortletContext.createPortletContext("SimplePortlet"));
      commitTX();

      // Clone the modified CCP 2 times
      beginTX();
      PortletContext cloneOfClone1 = consumer.createClone(clone1);
      PortletContext cloneOfClone2 = consumer.createClone(clone1);
      commitTX();

      // Destroy the clone 2
      beginTX();
      List failures = consumer.destroyClones(Collections.singletonList(clone2));
      assertEquals(Collections.EMPTY_LIST, failures);
      commitTX();

      // Destroy the clone of the clone 2
      beginTX();
      failures = consumer.destroyClones(Collections.singletonList(cloneOfClone2));
      assertEquals(Collections.EMPTY_LIST, failures);
      commitTX();

      // Destroy the clone 1
      beginTX();
      failures = consumer.destroyClones(Collections.singletonList(clone1));
      assertEquals(Collections.EMPTY_LIST, failures);
      commitTX();

      // Destroy the clone of the clone 1
      beginTX();
      failures = consumer.destroyClones(Collections.singletonList(cloneOfClone1));
      assertEquals(Collections.EMPTY_LIST, failures);
      commitTX();
   }

   public void testInvokeCloneBeforeWritePOPWithinTx() throws Exception
   {
      beginRegistrationScopedTX();
      PortletInvocation action = new ActionInvocation(new PortletInvocationContextImpl());
      action.setTarget(PortletContext.createPortletContext("CloningPortlet"));
      action.setUserContext(new AbstractUserContext("julien"));
      AbstractInstanceContext instanceContext = new AbstractInstanceContext("whatever", AccessMode.CLONE_BEFORE_WRITE);
      action.setInstanceContext(instanceContext);
      consumer.invoke(action);
      commitTX();

      // Check state
      beginTX();
      PortletContext clone = instanceContext.getClonedContext();
      assertNotNull(clone);
      PersistentPortletState cloneState = (PersistentPortletState)persistenceManager.loadState(clone.getId().substring(1));
      assertNotNull(cloneState);
      assertEquals(clone.getId().substring(1), cloneState.getId());
      assertEquals("CloningPortlet", cloneState.getState().getPortletId());
      SimplePropertyMap expectedValue = new SimplePropertyMap();
      List<String> list = new ArrayList<String>();
      list.add("def");
      expectedValue.setProperty("abc", list);
      list = new ArrayList<String>();
      list.add("_def");
      expectedValue.setProperty("_abc", list);
      ValueMapAssert.assertEquals(expectedValue, cloneState.getState().getProperties());
      if (useRegistration)
      {
         PersistentRegistration registration = cloneState.getRelatedRegistration();
         assertNotNull(registration);
         assertEquals(registrationId, registration.getId());
      }
      commitTX();
   }

   public void testInvokeReadWritePOPWithinTx() throws Exception
   {
      beginRegistrationScopedTX();
      PortletInvocation action = new ActionInvocation(new PortletInvocationContextImpl());
      action.setTarget(PortletContext.createPortletContext("CloneFailedCloningPortlet"));
      action.setUserContext(new AbstractUserContext("julien"));
      AbstractInstanceContext instanceContext = new AbstractInstanceContext("whatever", AccessMode.READ_WRITE);
      action.setInstanceContext(instanceContext);
      consumer.invoke(action);
      commitTX();

      // Check state
      beginTX();
      assertNull(instanceContext.getClonedContext());
      assertNull(instanceContext.getModifiedContext());
      commitTX();
   }

   public void testInvokeReadOnlyPOPWithinTx() throws Exception
   {
      beginRegistrationScopedTX();
      PortletInvocation action = new ActionInvocation(new PortletInvocationContextImpl());
      action.setTarget(PortletContext.createPortletContext("CloneFailedCloningPortlet"));
      action.setUserContext(new AbstractUserContext("julien"));
      AbstractInstanceContext instanceContext = new AbstractInstanceContext("whatever", AccessMode.READ_ONLY);
      action.setInstanceContext(instanceContext);
      consumer.invoke(action);
      commitTX();

      // Check state
      beginTX();
      assertNull(instanceContext.getClonedContext());
      assertNull(instanceContext.getModifiedContext());
      commitTX();
   }

   public void testInvokeCloneBeforeWriteCCPWithinTx() throws Exception
   {
      beginTX();
      PortletContext cloningPortletId = consumer.createClone(PortletContext.createPortletContext("CloningPortlet"));
      commitTX();

      // Modify the state of the CCP
      beginTX();
      PortletState cloningPortletState = persistenceManager.loadState(cloningPortletId.getId().substring(1)).getState();
      SimplePropertyMap newCloningPortletStateValue = new SimplePropertyMap(cloningPortletState.getProperties());
      List<String> list = new ArrayList<String>();
      list.add("deff");
      newCloningPortletStateValue.setProperty("abc", list);
      persistenceManager.updateState(cloningPortletId.getId().substring(1), newCloningPortletStateValue);
      commitTX();

      //
      beginRegistrationScopedTX();
      PortletInvocation action = new ActionInvocation(new PortletInvocationContextImpl());
      action.setTarget(cloningPortletId);
      action.setUserContext(new AbstractUserContext("julien"));
      AbstractInstanceContext instanceContext = new AbstractInstanceContext("whatever", AccessMode.CLONE_BEFORE_WRITE);
      action.setInstanceContext(instanceContext);
      consumer.invoke(action);
      commitTX();

      // Check state
      beginTX();
      PortletContext clone = instanceContext.getClonedContext();
      assertNotNull(clone);
      PersistentPortletState cloneState = (PersistentPortletState)persistenceManager.loadState(clone.getId().substring(1));
      assertNotNull(cloneState);
      assertEquals(clone.getId().substring(1), cloneState.getId());
      assertEquals("CloningPortlet", cloneState.getState().getPortletId());
      SimplePropertyMap expectedValue = new SimplePropertyMap();
      list = new ArrayList<String>();
      list.add("deff");
      expectedValue.setProperty("abc", list);
      list = new ArrayList<String>();
      list.add("_def");
      expectedValue.setProperty("_abc", list);
      ValueMapAssert.assertEquals(expectedValue, cloneState.getState().getProperties());
      if (useRegistration)
      {
         PersistentRegistration registration = cloneState.getRelatedRegistration();
         assertNotNull(registration);
         assertEquals(registrationId, registration.getId());
      }
      commitTX();
   }

   public void testInvokeReadWriteCCPWithinTx() throws Exception
   {
      beginTX();
      PortletContext cloningPortletId = consumer.createClone(PortletContext.createPortletContext("CloningPortlet"));
      commitTX();

      //
      beginRegistrationScopedTX();
      PortletInvocation action = new ActionInvocation(new PortletInvocationContextImpl());
      action.setTarget(cloningPortletId);
      action.setUserContext(new AbstractUserContext("julien"));
      AbstractInstanceContext instanceContext = new AbstractInstanceContext("whatever", AccessMode.READ_WRITE);
      action.setInstanceContext(instanceContext);
      consumer.invoke(action);
      commitTX();

      // Check state
      beginTX();
      assertNull(instanceContext.getClonedContext());
      PortletStateContext state = persistenceManager.loadState(cloningPortletId.getId().substring(1));
      assertNotNull(state);
      assertEquals("CloningPortlet", state.getState().getPortletId());
      assertEquals(cloningPortletId.getId().substring(1), state.getId());
      SimplePropertyMap expectedValue = new SimplePropertyMap();
      List<String> list = new ArrayList<String>();
      list.add("def");
      expectedValue.setProperty("abc", list);
      list = new ArrayList<String>();
      list.add("_def");
      expectedValue.setProperty("_abc", list);
      ValueMapAssert.assertEquals(expectedValue, state.getState().getProperties());
      commitTX();
   }

   public void testInvokeReadOnlyCCPWithinTx() throws Exception
   {
      beginTX();
      PortletContext cloneFailedCloningPortletId = consumer.createClone(PortletContext.createPortletContext("CloneFailedCloningPortlet"));
      commitTX();

      //
      beginRegistrationScopedTX();
      PortletInvocation action = new ActionInvocation(new PortletInvocationContextImpl());
      action.setTarget(cloneFailedCloningPortletId);
      action.setUserContext(new AbstractUserContext("julien"));
      AbstractInstanceContext instanceContext = new AbstractInstanceContext("whatever", AccessMode.READ_ONLY);
      action.setInstanceContext(instanceContext);
      consumer.invoke(action);
      commitTX();

      // Check state
      beginTX();
      assertNull(instanceContext.getClonedContext());
      commitTX();
   }

   public void testInvokeCloneBeforeWritePOPWithinTxThrowsException() throws Exception
   {
      beginRegistrationScopedTX();
      PortletInvocation action = new ActionInvocation(new PortletInvocationContextImpl());
      action.setTarget(PortletContext.createPortletContext("CloningPortletThrowingRuntimeException"));
      action.setUserContext(new AbstractUserContext("julien"));
      AbstractInstanceContext instanceContext = new AbstractInstanceContext("whatever", AccessMode.CLONE_BEFORE_WRITE);
      action.setInstanceContext(instanceContext);
      try
      {
         consumer.invoke(action);
         fail("Was expecting RuntimeException");
      }
      catch (RuntimeException expected)
      {
         assertEquals("custom_message", expected.getMessage());
         rollbackTX();
      }

      // Check state
      beginTX();
      assertNull(instanceContext.getClonedContext());
      commitTX();
   }

   public void _testGetProperties()
   {

   }

   /** todo : should check the portlet metadata as well */
   public void testGetCCP() throws Exception
   {
      // Clone a POP
      beginTX();
      PortletContext cloneContext = consumer.createClone(PortletContext.createPortletContext("SimplePortlet"));
      commitTX();

      //
      beginTX();
      Portlet portlet = consumer.getPortlet(cloneContext);
      assertEquals(cloneContext, portlet.getContext());
      assertEquals("SimplePortlet", portlet.getInfo().getMeta().getMetaValue(MetaInfo.DISPLAY_NAME).getDefaultString());
      commitTX();

      // Clone the modified CCP
      beginTX();
      PortletContext cloneCloneContext = consumer.createClone(cloneContext);
      commitTX();

      //
      beginTX();
      portlet = consumer.getPortlet(cloneCloneContext);
      assertEquals(cloneCloneContext, portlet.getContext());
      assertEquals("SimplePortlet", portlet.getInfo().getMeta().getMetaValue(MetaInfo.DISPLAY_NAME).getDefaultString());
      commitTX();
   }

   public void _testCloneNonExistingPortletWithinTx() throws Exception
   {
      try
      {
         beginTX();
         consumer.createClone(PortletContext.createPortletContext("UnknownPortlet"));
         fail("Was expecting no such portlet exception");
      }
      catch (NoSuchPortletException e)
      {
         rollbackTX();
      }

      // todo check state

//      try
//      {
//         TransactionAssert.beginTransaction();
//         statefulPortletInvoker.createClone("_1");
//         fail("Was expecting no such portlet exception");
//      }
//      catch (NoSuchPortletException e)
//      {
//         TransactionAssert.rollbackTransaction(true);
//      }

      // todo check state
   }
}

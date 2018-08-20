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

import junit.framework.TestSuite;
import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.jboss.portal.common.junit.TransactionAssert;
import org.jboss.portal.core.impl.portlet.state.PersistentPortletStatePersistenceManager;
import org.jboss.portal.registration.RegistrationPersistenceManager;
import org.jboss.portal.test.framework.TestParametrization;
import org.jboss.portal.test.framework.embedded.DataSourceSupport;
import org.jboss.portal.test.framework.embedded.HibernateSupport;
import org.jboss.portal.test.framework.junit.JUnitAdapter;
import org.jboss.portal.test.framework.junit.POJOJUnitTest;
import org.jboss.portal.test.framework.mc.TestRuntimeContext;
import org.jboss.portal.test.registration.AbstractRegistrationPersistenceManagerTestCase;

import java.net.URL;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class RegistrationPersistenceManagerTestCase extends AbstractRegistrationPersistenceManagerTestCase
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
      POJOJUnitTest abc = new POJOJUnitTest(RegistrationPersistenceManagerTestCase.class);
      JUnitAdapter adapter = new JUnitAdapter(abc, parametrization);
      TestSuite suite = new TestSuite();
      suite.addTest(adapter);
      return suite;
   }

   /** . */
   private TestRuntimeContext runtimeContext;

   /** . */
   private DataSourceSupport.Config dataSourceConfigParameter;

   /** . */
   private HibernateSupport hibernateSupport;

   /** . */
   private PersistentPortletStatePersistenceManager persistenceManager;

   public String getName()
   {
      return super.getName() + ",ds=" + dataSourceConfigParameter.getName();
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

   public DataSourceSupport.Config getDataSourceConfigParameter()
   {
      return dataSourceConfigParameter;
   }

   public void setDataSourceConfigParameter(DataSourceSupport.Config dataSourceConfigParameter)
   {
      this.dataSourceConfigParameter = dataSourceConfigParameter;
   }

   public void setUp() throws Exception
   {
      runtimeContext = new TestRuntimeContext("org/jboss/portal/test/core/state/registration-persistence-manager-beans.xml");
      runtimeContext.addBean("TestBean", this);
      runtimeContext.addBean("DataSourceConfig", dataSourceConfigParameter);
      runtimeContext.addBean("HibernateConfig", HibernateSupport.getConfig(dataSourceConfigParameter.getName()));
      runtimeContext.start();

      //
      super.setUp();
   }

   public void tearDown() throws Exception
   {
      super.tearDown();

      // Cleanup any pending transaction
      TransactionAssert.endTransaction();

      //
      runtimeContext.stop();
   }

   public RegistrationPersistenceManager getManager()
   {
      return persistenceManager;
   }

   public void startInteraction()
   {
      TransactionAssert.beginTransaction();
   }

   public void stopInteraction()
   {
      TransactionAssert.commitTransaction();
   }
}

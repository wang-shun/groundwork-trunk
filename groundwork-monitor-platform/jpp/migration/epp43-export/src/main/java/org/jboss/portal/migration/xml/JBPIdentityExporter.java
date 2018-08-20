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
package org.jboss.portal.migration.xml;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jboss.portal.common.transaction.Transactions;
import org.jboss.portal.identity.IdentityContext;
import org.jboss.portal.identity.IdentityServiceController;
import org.jboss.portal.identity.MembershipModule;
import org.jboss.portal.identity.Role;
import org.jboss.portal.identity.RoleModule;
import org.jboss.portal.identity.User;
import org.jboss.portal.identity.UserModule;
import org.jboss.portal.identity.UserProfileModule;
import org.jboss.portal.identity.db.HibernateRoleImpl;
import org.jboss.portal.identity.db.HibernateUserImpl;
import org.jboss.portal.identity.info.ProfileInfo;
import org.jboss.portal.identity.info.PropertyInfo;
import org.jboss.portal.migration.xml.identity.MProperty;
import org.jboss.portal.migration.xml.identity.MRole;
import org.jboss.portal.migration.xml.identity.MUser;
import org.jboss.portal.migration.xml.identity.RoleExporter;
import org.jboss.portal.migration.xml.identity.UserExporter;
import org.jboss.system.ServiceMBeanSupport;

import javax.naming.InitialContext;
import javax.transaction.TransactionManager;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 
 */
public class JBPIdentityExporter extends ServiceMBeanSupport implements JBPIdentityExporterMBean
{
   private static final org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(JBPIdentityExporter.class);

   private IdentityServiceController identityServiceController;

   private String sessionFactoryJNDIName = "java:/portal/IdentitySessionFactory";

   private SessionFactory sessionFactory;

   public String getSessionFactoryJNDIName()
   {
      return sessionFactoryJNDIName;
   }

   public void setSessionFactoryJNDIName(String sessionFactoryJNDIName)
   {
      this.sessionFactoryJNDIName = sessionFactoryJNDIName;
   }

   public void exportUsers(final String fileName) throws Exception
   {

      final Set<User> allUsers = new HashSet<User>();

      try {
         TransactionManager tm = (TransactionManager) new InitialContext()
            .lookup("java:/TransactionManager");
         Transactions.required(tm, new Transactions.Runnable()
         {
            public Object run() throws Exception
            {
               
               allUsers.addAll(getUsers());

               if (allUsers.size() > 0)
               {


                  File outputFile = new File(fileName);

                  if (!outputFile.exists())
                  {
                     outputFile.createNewFile();
                  }

                  FileOutputStream fos = new FileOutputStream(outputFile, false);

                  UserExporter ue = new UserExporter();

                  ue.startExport(fos);

                  try
                  {
                     for (User user : allUsers)
                     {
                        ue.exportUser(convertUser(user));
                     }
                  }
                  catch (Exception e)
                  {
                     log.info("Error during user export: ", e);
                  }
                  finally
                  {
                     ue.endExport();
                     fos.close();
                  }


               }

               return null;

            }
         });
      } catch (Exception e) {
         log.info(e);
      }

   }

   private Set<User> getUsers() throws Exception
   {
      UserModule userModule = getUserModule();

      Set<User> allUsers = new HashSet <User>();
      try
      {
         allUsers = userModule.findUsers(0, 0);
      }
      catch (Exception e)
      {
         log.info("Exception while getting users: ", e);

      }

      return allUsers;

   }

   private Set<Role> getRoles() throws Exception
   {
      RoleModule roleModule = getRoleModule();

      Set<Role> allRoles = new HashSet <Role>();
      try
      {
         allRoles = roleModule.findRoles();
      }
      catch (Exception e)
      {
         log.info("Exception while getting roles: ", e);

      }

      return allRoles;

   }

   private Set<String> getMembers(Role role) throws Exception
   {
      MembershipModule mm = getMembershipModule();

      Set<String> members = new HashSet<String>();

      Set<User> users = new HashSet<User>();

      try
      {
         users = mm.getUsers(role);
      }
      catch (Exception e)
      {
         log.info("Error while getting role members: ", e);
      }

      for (User user : users)
      {
         members.add(user.getUserName());
      }

      return members;

   }


   private MUser convertUser(User user, Map properties) throws Exception
   {
      UserProfileModule upm = getUserProfileModule();
      List<MProperty> mprops = new LinkedList<MProperty>();
      ProfileInfo profileInfo = upm.getProfileInfo();

      for (String propName : (Set<String>)properties.keySet())
      {
         PropertyInfo pi = profileInfo.getPropertyInfo(propName);

         String type = "java.lang.String";
         if (pi != null)
         {
            type = pi.getType();
         }

         String value = "";
         if (properties.get(propName) != null)
         {
            value = properties.get(propName).toString();
         }

         MProperty mprop = new MProperty(propName, type, value);
         mprops.add(mprop);
      }

      MUser muser = new MUser(user.getUserName(), mprops);
      return muser;
   }

   private MUser convertUser(User user) throws Exception
   {
      UserProfileModule upm = getUserProfileModule();

      Map<String, Object> properties = new HashMap<String, Object>();
      try
      {
         properties = upm.getProperties(user);
      }
      catch (Exception e)
      {
         log.info("cannot obtain user profile", e);
      }

      return convertUser(user, properties);
   }

   private MRole convertRole(Role role)
   {
      MRole mrole = new MRole(role.getName(), role.getDisplayName());

      return mrole;
   }

   private MRole convertRole(Role role, Set<String> members)
   {
      MRole mrole = new MRole(role.getName(), role.getDisplayName(), members);

      return mrole;
   }

   public void exportRoles(final String fileName) throws Exception
   {

      final Set<Role> allRoles = new HashSet<Role>();

      try {
         TransactionManager tm = (TransactionManager) new InitialContext()
            .lookup("java:/TransactionManager");
         Transactions.required(tm, new Transactions.Runnable()
         {
            public Object run() throws Exception
            {

               allRoles.addAll(getRoles());



               if (allRoles.size() > 0)
               {


                  File outputFile = new File(fileName);

                  if (!outputFile.exists())
                  {
                     outputFile.createNewFile();
                  }

                  FileOutputStream fos = new FileOutputStream(outputFile, false);

                  RoleExporter re = new RoleExporter();

                  re.startExport(fos);

                  try
                  {
                     for (final Role role : allRoles)
                     {

                        final Set<String> members = new HashSet<String>();
                        try {
                           TransactionManager tm = (TransactionManager) new InitialContext()
                              .lookup("java:/TransactionManager");
                           Transactions.required(tm, new Transactions.Runnable()
                           {
                              public Object run() throws Exception
                              {

                                 members.addAll(getMembers(role));

                                 return null;
                              }
                           });
                        } catch (Exception e) {
                           log.info(e);
                        }


                        re.exportRole(convertRole(role, members));
                     }
                  }
                  catch (Exception e)
                  {
                     log.info("Error during role export: ", e);
                  }
                  finally
                  {
                     re.endExport();
                     fos.close();
                  }


               }
               return null;
            }
         });
      } catch (Exception e) {
         log.info(e);
      }

   }

   public void exportHibernateUsers(final String fileName, final int batchSize) throws Exception
   {
      if (batchSize <= 0)
      {
         throw new IllegalArgumentException("Batch size must be number >0");
      }

      File outputFile = new File(fileName);

      if (!outputFile.exists())
      {
         outputFile.createNewFile();
      }

      FileOutputStream fos = new FileOutputStream(outputFile, false);

      final UserExporter ue = new UserExporter();

      ue.startExport(fos);

      try {
         TransactionManager tm = (TransactionManager) new InitialContext()
            .lookup("java:/TransactionManager");
         Transactions.required(tm, new Transactions.Runnable()
         {
            public Object run() throws Exception
            {

               int userCount = getUserModule().getUserCount();

               int offset = 0;

               for (; offset <= userCount; offset = offset + batchSize)
               {
                  Session session = getCurrentSession();
                  Query query = session.createQuery("from HibernateUserImpl as u order by u.userName");
                  query.setFirstResult(offset);

                  if (offset + batchSize > userCount)
                  {
                     // Then get from offset to userCount
                     query.setMaxResults(userCount);
                  }
                  else
                  {
                     query.setMaxResults(batchSize);
                  }

                  Iterator<HibernateUserImpl> iter = query.iterate();

                  while (iter.hasNext())
                  {
                     HibernateUserImpl user = (HibernateUserImpl)iter.next();

                     Map properties = user.getProfileMap();

                     try
                     {
                        ue.exportUser(convertUser(user, properties));
                     }
                     catch (Exception e)
                     {
                        log.info("Error during user export: ", e);
                     }
                  }
               }


               return null;
            }
         });
      }
      catch (Exception e)
      {
         throw e;

      }
      finally
      {
         ue.endExport();
         fos.close();
      }


   }

   public void exportHibernateRoles(final String fileName, final int batchSize) throws Exception
   {
      if (batchSize <= 0)
      {
         throw new IllegalArgumentException("Batch size must be number >0");
      }

      File outputFile = new File(fileName);

      if (!outputFile.exists())
      {
         outputFile.createNewFile();
      }

      FileOutputStream fos = new FileOutputStream(outputFile, false);

      final RoleExporter re = new RoleExporter();

      re.startExport(fos);

      try
      {
         TransactionManager tm = (TransactionManager) new InitialContext()
            .lookup("java:/TransactionManager");
         Transactions.required(tm, new Transactions.Runnable()
         {
            public Object run() throws Exception
            {

               int roleCount = getRoleModule().getRolesCount();

               int offset = 0;

               for (; offset <= roleCount; offset = offset + batchSize)
               {
                  Session session = getCurrentSession();
                  Query query = session.createQuery("from HibernateRoleImpl as r order by r.name");
                  query.setFirstResult(offset);

                  if (offset + batchSize > roleCount)
                  {
                     // Then get from offset to roleCount
                     query.setMaxResults(roleCount);
                  }
                  else
                  {
                     query.setMaxResults(batchSize);
                  }

                  Iterator<HibernateRoleImpl> iter = query.iterate();

                  while (iter.hasNext())
                  {
                     HibernateRoleImpl role = (HibernateRoleImpl)iter.next();

                     Set<String> members = new HashSet<String>();

                     for (User user : (Set<User>)role.getUsers())
                     {
                        members.add(user.getUserName());
                     }

                     try
                     {
                        re.exportRole(convertRole(role, members));
                     }
                     catch (Exception e)
                     {
                        log.info("Error during user export: ", e);
                     }
                  }
               }

               return null;
            }
         });
      }
      catch (Exception e)
      {
         throw e;
      }
      finally
      {
         re.endExport();
         fos.close();
      }
   }

   public IdentityServiceController getIdentityServiceController()
   {
      return identityServiceController;
   }

   public void setIdentityServiceController(IdentityServiceController identityServiceController)
   {
      this.identityServiceController = identityServiceController;
   }

   public UserModule getUserModule() throws Exception
   {
      return (UserModule)getIdentityServiceController().getIdentityContext().getObject(IdentityContext.TYPE_USER_MODULE);
   }

   public RoleModule getRoleModule() throws Exception
   {
      return (RoleModule)getIdentityServiceController().getIdentityContext().getObject(IdentityContext.TYPE_ROLE_MODULE);
   }

   public MembershipModule getMembershipModule() throws Exception
   {
      return (MembershipModule)getIdentityServiceController().getIdentityContext().getObject(IdentityContext.TYPE_MEMBERSHIP_MODULE);
   }

   public UserProfileModule getUserProfileModule() throws Exception
   {
      return (UserProfileModule)getIdentityServiceController().getIdentityContext().getObject(IdentityContext.TYPE_USER_PROFILE_MODULE);
   }

   public SessionFactory getSessionFactory() throws Exception
   {
      if (sessionFactory == null)
      {
         sessionFactory = (SessionFactory)new InitialContext().lookup(sessionFactoryJNDIName);
      }

      return sessionFactory;
      
   }

   public Session getCurrentSession() throws Exception
   {
      SessionFactory sf = getSessionFactory();

      if (sf == null)
      {
         throw new IllegalStateException("Failed to obtain SessionFactory");
      }

      return sf.getCurrentSession();
   }
}

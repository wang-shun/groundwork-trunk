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

package org.jboss.portal.core.identity;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.jboss.logging.Logger;
import org.jboss.portal.jems.as.system.AbstractJBossService;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author <a href="mailto:boleslaw dot dawidowicz at redhat anotherdot
 *         com">Boleslaw Dawidowicz</a>
 * @author <a href="mailto:jedim@vige.it">Luca Stancapiano</a>
 * @version $Revision: 12036 $
 */
public class UsersActivityStatsServiceImpl extends AbstractJBossService
      implements
         UsersActivityStatsService,
         NotificationListener
{
   /** Our logger. */
   private static final Logger log = Logger.getLogger(UsersActivityStatsServiceImpl.class);

   // TODO: some value just to begin - find some good default
   private int userTrackerThreadsNumber = 10;

   private int updaterThreadsNumber = 1;

   private int updaterInterval = 1000;

   private int activityQueueLimit = 1000;

   private long activityTimeout = 1800000;

   private Executor userTrackerExecutor;

   private ScheduledExecutorService updaterExecutor;

   private Queue activityQueue;

   private volatile Set activityResults = new HashSet();

   private String activityBroadcasterName;

   public UsersActivityStatsServiceImpl()
   {
   }

   protected void startService() throws Exception
   {
      super.startService();

      activityQueue = new LinkedBlockingQueue(getActivityQueueLimit());

      userTrackerExecutor = Executors.newFixedThreadPool(getUserTrackerThreadsNumber());

      updaterExecutor = Executors.newScheduledThreadPool(getUpdaterThreadsNumber());

      updaterExecutor.scheduleWithFixedDelay(new Updater(activityQueue), getUpdaterInterval(), getUpdaterInterval(),
            TimeUnit.MILLISECONDS);

      if (activityBroadcasterName != null)
      {
         server.addNotificationListener(new ObjectName(activityBroadcasterName), this, null, null);
      }
      else
      {
         addNotificationListener(this, null, null);
      }

   }

   protected void stopService() throws Exception
   {
      super.stopService();

      // /TODO: stop all the threads
   }

   public Set getActiveUsersIds(long period)
   {
      long currentTime = System.currentTimeMillis();

      Set results = new HashSet();
      for (Iterator iterator = activityResults.iterator(); iterator.hasNext();)
      {
         UserActivity ua = (UserActivity) iterator.next();
         if (currentTime - ua.getTimestamp() < period && !ua.getId().equals(UserActivity.GUEST))
         {
            results.add(ua.getSessionId());
         }
      }
      return results;
   }

   public int getActiveSessionCount(long period)
   {
      long currentTime = System.currentTimeMillis();

      int results = 0;
      for (Iterator iterator = activityResults.iterator(); iterator.hasNext();)
      {
         UserActivity ua = (UserActivity) iterator.next();
         if (currentTime - ua.getTimestamp() < period)
         {
            results++;
         }
      }
      return results;
   }

   public Set getActiveUsersNames(long period)
   {
      long currentTime = System.currentTimeMillis();
      Set results = new HashSet();
      for (Iterator iterator = activityResults.iterator(); iterator.hasNext();)
      {
         UserActivity ua = (UserActivity) iterator.next();
         if (currentTime - ua.getTimestamp() < period && !ua.getId().equals(UserActivity.GUEST))
         {
            results.add(ua.getId());
         }
      }
      return results;
   }

   public Set getUsersActivities(long period)
   {
      long currentTime = System.currentTimeMillis();
      Set results = new HashSet();
      for (Iterator iterator = activityResults.iterator(); iterator.hasNext();)
      {
         UserActivity ua = (UserActivity) iterator.next();
         if (currentTime - ua.getTimestamp() < period)
         {
            results.add(ua);
         }
      }
      return results;
   }

   public void registerActivity(final UserActivity userActivity)
   {
      try
      {
         Notification notification = new Notification(Integer.toString(userActivity.getType()), this.getServiceName(),
               userActivity.getTimestamp(), userActivity.getTimestamp(), userActivity.getId() + "_"
                     + userActivity.getSessionId());

         if (activityBroadcasterName != null)
         {
            log.debug("Broadcasting user activity notification ");

            server.invoke(new ObjectName(activityBroadcasterName), "sendNotification", new Object[]
            {notification}, new String[]
            {Notification.class.getName()});
         }
         else
         {
            log.debug("Sending local user activity notification ");
            sendNotification(notification);
         }

      }
      catch (Exception e)
      {
         log.error("Failed to send user activity notification: ", e);
      }

   }

   public void handleNotification(Notification notification, Object object)
   {
      log.debug("Handling  user activity notification ");
      final UserActivity ac = new UserActivity(notification);

      FutureTask task = new FutureTask(new Callable()
      {
         public Object call() throws Exception
         {

            boolean success = activityQueue.offer(ac);
            if (log.isTraceEnabled())
            {
               if (!success)
               {
                  log.trace("Failed track user activity - activityQueue is full ");
               }
            }
            return null;
         }
      });

      userTrackerExecutor.execute(task);
   }

   public int getUserTrackerThreadsNumber()
   {
      return userTrackerThreadsNumber;
   }

   public void setUserTrackerThreadsNumber(int userTrackerThreadsNumber)
   {
      this.userTrackerThreadsNumber = userTrackerThreadsNumber;
   }

   public int getUpdaterThreadsNumber()
   {
      return updaterThreadsNumber;
   }

   public void setUpdaterThreadsNumber(int updaterThreadsNumber)
   {
      this.updaterThreadsNumber = updaterThreadsNumber;
   }

   public int getUpdaterInterval()
   {
      return updaterInterval;
   }

   public void setUpdaterInterval(int updaterInterval)
   {
      this.updaterInterval = updaterInterval;
   }

   public int getActivityQueueLimit()
   {
      return activityQueueLimit;
   }

   public void setActivityQueueLimit(int activityQueueLimit)
   {
      this.activityQueueLimit = activityQueueLimit;
   }

   public long getActivityTimeout()
   {
      return activityTimeout;
   }

   public void setActivityTimeout(long activityTimeout)
   {
      this.activityTimeout = activityTimeout;
   }

   public String getActivityBroadcasterName()
   {
      return activityBroadcasterName;
   }

   public void setActivityBroadcasterName(String activityBroadcasterName)
   {
      this.activityBroadcasterName = activityBroadcasterName;
   }

   private class Updater implements Runnable
   {
      private final Queue activityQueue;

      public Updater(Queue activityQueue)
      {
         this.activityQueue = activityQueue;
      }

      // never run
      private Updater()
      {
         this.activityQueue = null;
      }

      public void run()
      {
         long currentTime = System.currentTimeMillis();

         Set stillActive = getUsersActivities(activityTimeout);

         while (!activityQueue.isEmpty())
         {
            UserActivity activity = (UserActivity) activityQueue.poll();
            if (activity != null && ((currentTime - activity.getTimestamp()) < activityTimeout))
            {
               if (activity.getType() != UserActivity.EXIT)
               {
                  stillActive.add(activity);
               }
               else
               {
                  stillActive.remove(activity);
               }
            }
         }

         activityResults = Collections.unmodifiableSet(stillActive);

      }
   }

}

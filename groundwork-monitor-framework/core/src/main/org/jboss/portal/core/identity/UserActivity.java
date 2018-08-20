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

import javax.management.Notification;

/**
 * @author <a href="mailto:boleslaw dot dawidowicz at redhat anotherdot com">Boleslaw Dawidowicz</a>
 * @author <a href="mailto:jedim@vige.it">Luca Stancapiano</a>
 * @version $Revision: 10807 $
 */
public class UserActivity
{

   public static final int NAVIGATION = 0;

   public static final int EXIT = 1;

   private String id;

   private String sessionId;

   private long timestamp;

   private final int type;

   public final static String GUEST = "guest";

   private UserActivity()
   {
      this.type = NAVIGATION;
   }

   public UserActivity(String id, String sessionId, long timestamp, int type)
   {
      if (id == null)
      {
         throw new IllegalArgumentException("Id cannot be null");
      }

      this.id = id;
      this.sessionId = sessionId;
      this.timestamp = timestamp;
      this.type = type;
   }

   public UserActivity(Notification notification)
   {
      if (notification.getMessage() == null)
      {
         throw new IllegalArgumentException("Id (notification message) cannot be null");
      }
      this.id = notification.getMessage().substring(0, notification.getMessage().indexOf("_"));
      this.sessionId = notification.getMessage().substring(notification.getMessage().indexOf("_"));
      this.timestamp = notification.getTimeStamp();
      this.type = Integer.parseInt(notification.getType());
   }

   public String getId()
   {
      return id;
   }

   public String getSessionId()
   {
      return sessionId;
   }

   public long getTimestamp()
   {
      return timestamp;
   }

   public int getType()
   {
      return type;
   }

   public boolean equals(Object o)
   {
      if (this == o)
      {
         return true;
      }
      if (o == null || getClass() != o.getClass())
      {
         return false;
      }

      UserActivity that = (UserActivity) o;

      if (!id.equals(that.id) || !sessionId.equals(that.sessionId))
      {
         return false;
      }

      return true;
   }

   public int hashCode()
   {
      int result;
      result = id.hashCode() + sessionId.hashCode();
      return result;
   }

}

/*
* JBoss, a division of Red Hat
* Copyright 2008, Red Hat Middleware, LLC, and individual contributors as indicated
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

package org.jboss.portal.core.util;

import org.jboss.portal.common.util.ParameterValidation;

import java.util.regex.Pattern;

/**
 * TODO
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 * @deprecated Should use {@link org.jboss.portal.common.util.ParameterValidation#sanitize} instead starting with 2.7.2
 */
public class ParameterSanitizer
{
   public final static Pattern CSS_DISTANCE = Pattern.compile("\\d+\\W*(em|ex|px|in|cm|mm|pt|pc|%)?");
   
   public static String sanitize(String value, Pattern regex, String defaultValue)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(regex, "expected value format");

      if(value == null || !regex.matcher(value).matches())
      {
         return defaultValue;
      }
      else
      {
         return value;
      }
   }
}

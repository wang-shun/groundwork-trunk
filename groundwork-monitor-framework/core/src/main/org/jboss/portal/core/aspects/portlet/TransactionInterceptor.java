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
package org.jboss.portal.core.aspects.portlet;

import org.jboss.portal.common.invocation.InvocationException;
import org.jboss.portal.common.transaction.Transactions;
import org.jboss.portal.core.portlet.info.TransactionInfo;
import org.jboss.portal.portlet.PortletInvokerException;
import org.jboss.portal.portlet.info.PortletInfo;
import org.jboss.portal.portlet.invocation.PortletInvocation;
import org.jboss.portal.portlet.invocation.response.ErrorResponse;
import org.jboss.portal.portlet.invocation.response.PortletInvocationResponse;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11068 $
 */
public class TransactionInterceptor extends CorePortletInterceptor
{

   public PortletInvocationResponse invoke(PortletInvocation invocation) throws IllegalArgumentException, PortletInvokerException
   {
      // Override tx type if found
      PortletInfo portletInfo = getPortletInfo(invocation);
      if (portletInfo != null)
      {
         TransactionInfo transactionInfo = portletInfo.getAttachment(TransactionInfo.class);

         if (transactionInfo != null)
         {
            Transactions.Type txType = transactionInfo.getTransactionType();
            
            //
            try
            {
               if (txType == Transactions.TYPE_NOT_SUPPORTED)
               {
                  return invokeNotSupported(invocation);
               }
               else if (txType == Transactions.TYPE_NEVER)
               {
                  return invokeNever(invocation);
               }
               else if (txType == Transactions.TYPE_MANDATORY)
               {
                  return invokeMandatory(invocation);
               }
               else if (txType == Transactions.TYPE_SUPPORTS)
               {
                  return invokeSupports(invocation);
               }
               else if (txType == Transactions.TYPE_REQUIRED)
               {
                  return invokeRequired(invocation);
               }
               else if (txType == Transactions.TYPE_REQUIRES_NEW)
               {
                  return invokeRequiresNew(invocation);
               }
               else
               {
                  throw new InvocationException("Should not happen");
               }
            }
            catch (PortletInvokerException e)
            {
               return new ErrorResponse(e);
            }
         }
      }

      return super.invoke(invocation);
   }

   protected PortletInvocationResponse invokeNotSupported(PortletInvocation invocation) throws PortletInvokerException
   {
      return super.invoke(invocation);
   }

   protected PortletInvocationResponse invokeNever(PortletInvocation invocation) throws PortletInvokerException
   {
      return super.invoke(invocation);
   }

   protected PortletInvocationResponse invokeMandatory(PortletInvocation invocation) throws PortletInvokerException
   {
      return super.invoke(invocation);
   }

   protected PortletInvocationResponse invokeSupports(PortletInvocation invocation) throws PortletInvokerException
   {
      return super.invoke(invocation);
   }

   protected PortletInvocationResponse invokeRequired(PortletInvocation invocation) throws PortletInvokerException
   {
      return super.invoke(invocation);
   }

   protected PortletInvocationResponse invokeRequiresNew(PortletInvocation invocation) throws PortletInvokerException
   {
      return super.invoke(invocation);
   }
}

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
package org.jboss.portal.test.core;

import junit.framework.TestCase;
import org.jboss.portal.core.util.ProxyValidator;

import java.util.List;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class ProxyValidatorTestCase extends TestCase
{
   public ProxyValidatorTestCase(String key)
   {
      super(key);
   }

   public void testInvalidMethod()
   {
      ProxyValidator.Error[] errors = ProxyValidator.validate(InvalidMethod.class);
      assertEquals(1, errors.length);
      assertEquals(ProxyValidator.METHOD_NOT_ACCESSOR, errors[0].getCode());
      assertNotNull(errors[0].getMethod());
   }

   public interface InvalidMethod
   {
      void notASetterNorAGetter();
   }

   public void testGetterDuplicateName()
   {
      ProxyValidator.Error[] errors = ProxyValidator.validate(GetterDuplicateName.class);
      assertEquals(1, errors.length);
      assertEquals(ProxyValidator.GETTER_DUPLICATE_NAME, errors[0].getCode());
      assertNotNull(errors[0].getMethod());
   }

   public interface GetterDuplicateName
   {
      int getA(int a);

      boolean getA(boolean a);
   }

   public void testGetterInvalidName()
   {
      ProxyValidator.Error[] errors = ProxyValidator.validate(GetterInvalidName.class);
      assertEquals(1, errors.length);
      assertEquals(ProxyValidator.GETTER_INVALID_NAME, errors[0].getCode());
      assertNotNull(errors[0].getMethod());
   }

   public interface GetterInvalidName
   {
      int get(int a);
   }

   public void testGetterInvalidReturnType()
   {
      ProxyValidator.Error[] errors = ProxyValidator.validate(GetterInvalidReturnType.class);
      assertEquals(1, errors.length);
      assertEquals(ProxyValidator.GETTER_INVALID_RETURN_TYPE, errors[0].getCode());
      assertNotNull(errors[0].getMethod());
   }

   public interface GetterInvalidReturnType
   {
      List getA(List a);
   }

   public void testGetterNoArgument()
   {
      ProxyValidator.Error[] errors = ProxyValidator.validate(GetterNoArgument.class);
      assertEquals(1, errors.length);
      assertEquals(ProxyValidator.GETTER_NO_ARGUMENT, errors[0].getCode());
      assertNotNull(errors[0].getMethod());
   }

   public interface GetterNoArgument
   {
      int getA();
   }

   public void testGetterReturnTypeDoesNotMatchArgumentType()
   {
      ProxyValidator.Error[] errors = ProxyValidator.validate(GetterReturnTypeDoesNotMatchArgumentType.class);
      assertEquals(1, errors.length);
      assertEquals(ProxyValidator.GETTER_RETURN_TYPE_DOES_NOT_MATCH_ARGUMENT_TYPE, errors[0].getCode());
      assertNotNull(errors[0].getMethod());
   }

   public interface GetterReturnTypeDoesNotMatchArgumentType
   {
      int getA(boolean a);
   }

   public void testGetterTooManyArgument()
   {
      ProxyValidator.Error[] errors = ProxyValidator.validate(GetterTooManyArgument.class);
      assertEquals(1, errors.length);
      assertEquals(ProxyValidator.GETTER_TOO_MANY_ARGUMENTS, errors[0].getCode());
      assertNotNull(errors[0].getMethod());
   }

   public interface GetterTooManyArgument
   {
      int getA(int a, int b);
   }

   public void testSetterDuplicateName()
   {
      ProxyValidator.Error[] errors = ProxyValidator.validate(SetterDuplicateName.class);
      assertEquals(1, errors.length);
      assertEquals(ProxyValidator.SETTER_DUPLICATE_NAME, errors[0].getCode());
      assertNotNull(errors[0].getMethod());
   }

   public interface SetterDuplicateName
   {
      void setA(int a);

      void setA(boolean a);
   }

   public void testSetterInvalidName()
   {
      ProxyValidator.Error[] errors = ProxyValidator.validate(SetterInvalidName.class);
      assertEquals(1, errors.length);
      assertEquals(ProxyValidator.SETTER_INVALID_NAME, errors[0].getCode());
      assertNotNull(errors[0].getMethod());
   }

   public interface SetterInvalidName
   {
      void set(int a);
   }

   public void testSetterInvalidType()
   {
      ProxyValidator.Error[] errors = ProxyValidator.validate(SetterInvalidType.class);
      assertEquals(1, errors.length);
      assertEquals(ProxyValidator.SETTER_INVALID_ARGUMENT_TYPE, errors[0].getCode());
      assertNotNull(errors[0].getMethod());
   }

   public interface SetterInvalidType
   {
      void setA(List a);
   }

   public void testSetterNoArgument()
   {
      ProxyValidator.Error[] errors = ProxyValidator.validate(SetterNoArgument.class);
      assertEquals(1, errors.length);
      assertEquals(ProxyValidator.SETTER_NO_ARGUMENT, errors[0].getCode());
      assertNotNull(errors[0].getMethod());
   }

   public interface SetterNoArgument
   {
      void setA();
   }

   public void testSetterReturnTypeIsNotVoid()
   {
      ProxyValidator.Error[] errors = ProxyValidator.validate(SetterReturnTypeIsNotVoid.class);
      assertEquals(1, errors.length);
      assertEquals(ProxyValidator.SETTER_RETURN_TYPE_IS_NOT_VOID, errors[0].getCode());
      assertNotNull(errors[0].getMethod());
   }

   public interface SetterReturnTypeIsNotVoid
   {
      int setA(int a);
   }

   public void testSetterTooManyArguments()
   {
      ProxyValidator.Error[] errors = ProxyValidator.validate(SetterTooManyArguments.class);
      assertEquals(1, errors.length);
      assertEquals(ProxyValidator.SETTER_TOO_MANY_ARGUMENTS, errors[0].getCode());
      assertNotNull(errors[0].getMethod());
   }

   public interface SetterTooManyArguments
   {
      void setA(int a, int b);
   }
}

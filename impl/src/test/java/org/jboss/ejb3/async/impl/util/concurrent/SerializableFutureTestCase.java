/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.async.impl.util.concurrent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.ejb.AsyncResult;

import org.jboss.ejb3.async.impl.AsyncInvocationIdUUIDImpl;
import org.jboss.ejb3.async.impl.ClientExecutorService;
import org.jboss.ejb3.async.spi.AsyncEndpoint;
import org.jboss.ejb3.async.spi.AsyncInvocationId;
import org.jboss.ejb3.async.spi.CurrentAsyncInvocation;
import org.junit.Assert;
import org.junit.Test;

/**
 * Ensures our return values are {@link Serializable} (for @Remote pass-by-value)
 * semantics
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public class SerializableFutureTestCase
{

   /**
    * Ensures that {@link LocalJvmSerializableFutureWrapper} instances
    * may be serialized
    * @throws Throwable
    */
   @SuppressWarnings("unchecked")
   @Test
   public void resultUnwrappingFutureMayBeSerialized() throws Exception
   {

      // Roundtrip
      final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
      final ObjectOutputStream out = new ObjectOutputStream(byteOut);
      final String expectedValue = "Expected Value";
      final ExecutorService es = new ResultUnwrappingExecutorService(ClientExecutorService.INSTANCE,
            new AsyncEndpoint()
            {

               @Override
               public boolean cancel(final AsyncInvocationId id) throws IllegalArgumentException
               {
                  //NOOP for this test
                  return false;
               }

               @Override
               public Object invokeAsync(Serializable session, Class<?> invokedBusinessInterface, Method method,
                     Object[] args) throws Throwable
               {
                  //NOOP for this test
                  return null;
               }
            });
      final AsyncInvocationId id = new AsyncInvocationIdUUIDImpl();
      CurrentAsyncInvocation.markCurrentInvocationOnThread(id);
      final Future<AsyncResult<String>> serializableFuture;
      try
      {
         serializableFuture = es.submit(new Callable<AsyncResult<String>>()
         {
            public AsyncResult<String> call()
            {
               return new AsyncResult<String>(expectedValue);
            }
         });
      }
      finally
      {
         CurrentAsyncInvocation.unmarkCurrentInvocationFromThread();
      }
      out.writeObject(serializableFuture);
      out.flush();
      out.close();
      final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(byteOut.toByteArray()));
      final Future<String> roundtrip = (Future<String>) in.readObject();
      in.close();

      // Test
      Assert.assertEquals("Value was not as expected after serialization roundtrip", expectedValue, roundtrip.get());
   }

}

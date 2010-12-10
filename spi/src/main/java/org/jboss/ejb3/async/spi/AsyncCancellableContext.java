package org.jboss.ejb3.async.spi;

/**
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public interface AsyncCancellableContext
{
   /**
    * Submits a cancel request to the container for the invocation with specified ID
    * @param id
    * @return
    * @throws IllegalArgumentException If the ID is not supplied
    */
   boolean cancel(AsyncInvocationId id) throws IllegalArgumentException;
}

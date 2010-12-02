package org.jboss.ejb3.async.impl.interceptor;

import org.jboss.aop.Advisor;
import org.jboss.aop.InstanceAdvisor;
import org.jboss.aop.advice.AspectFactory;
import org.jboss.aop.joinpoint.Joinpoint;

/**
 * Factory to create interceptor instances.
 * Only to be used in scope PER_CLASS
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public abstract class CreatePerClassAspectFactory implements AspectFactory
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Exception raised if used in any scope other than PER_CLASS
    */
   private static RuntimeException RTE = new RuntimeException("Only to be used PER_CLASS");

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * {@inheritDoc}
    * @see org.jboss.aop.advice.AspectFactory#createPerVM()
    */
   @Override
   public Object createPerVM()
   {
      throw RTE;
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.aop.advice.AspectFactory#createPerInstance(org.jboss.aop.Advisor, org.jboss.aop.InstanceAdvisor)
    */
   @Override
   public Object createPerInstance(final Advisor advisor, final InstanceAdvisor instanceAdvisor)
   {
      throw RTE;
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.aop.advice.AspectFactory#createPerJoinpoint(org.jboss.aop.Advisor, org.jboss.aop.joinpoint.Joinpoint)
    */
   @Override
   public Object createPerJoinpoint(final Advisor advisor, final Joinpoint jp)
   {
      throw RTE;
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.aop.advice.AspectFactory#createPerJoinpoint(org.jboss.aop.Advisor, org.jboss.aop.InstanceAdvisor, org.jboss.aop.joinpoint.Joinpoint)
    */
   @Override
   public Object createPerJoinpoint(final Advisor advisor, final InstanceAdvisor instanceAdvisor, final Joinpoint jp)
   {
      throw RTE;
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.aop.advice.AspectFactory#getName()
    */
   @Override
   public String getName()
   {
      return this.getClass().getName();
   }

}

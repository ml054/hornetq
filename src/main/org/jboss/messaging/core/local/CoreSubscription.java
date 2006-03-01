/*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
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
package org.jboss.messaging.core.local;

import javax.jms.JMSException;

import org.jboss.logging.Logger;
import org.jboss.messaging.core.plugin.contract.MessageStore;
import org.jboss.messaging.core.plugin.contract.PersistenceManager;

/**
 * Represents a subscription to a destination (topic or queue).
 * 
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 *
 * $Id$
 */
public class CoreSubscription extends Pipe
{
   // Constants -----------------------------------------------------
   
   private static final Logger log = Logger.getLogger(CoreSubscription.class);

   // Static --------------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   protected Topic topic;
   protected String selector;
   protected boolean noLocal;
   
   // Constructors --------------------------------------------------

   public CoreSubscription(long id, Topic topic, String selector, boolean noLocal, MessageStore ms)
   {
      this(id, topic, selector, noLocal, ms, null);
   }
   
   protected CoreSubscription(long id, Topic topic, String selector, boolean noLocal,
                              MessageStore ms, PersistenceManager tl)
   {
      // A CoreSubscription must accept reliable messages, even if itself is non-recoverable
      super(id, ms, tl, true);
      this.topic = topic;
      this.selector = selector;
      this.noLocal = noLocal;
   }
   
   // Channel implementation ----------------------------------------

   // Public --------------------------------------------------------
   
   public void subscribe()
   {
      topic.add(this);
   }
   
   public void unsubscribe() throws JMSException
   {
      topic.remove(this);
   }
   
   public void closeConsumer() throws JMSException
   {
      unsubscribe();
      try
      {
         if (pm != null)
         {
            pm.removeAllMessageData(this.channelID);
         }
      }
      catch (Exception e)
      {
         final String msg = "Failed to remove message data for subscription";
         log.error(msg, e);
         throw new IllegalStateException(msg);
      }
   }
   
   public Topic getTopic()
   {
      return topic;
   }
   
   public String getSelector()
   {
      return selector;
   }
   
   public boolean isNoLocal()
   {
      return noLocal;
   }

   public String toString()
   {
      return "CoreSubscription[" + getChannelID() + ", " + topic + "]";
   }

   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------   
}

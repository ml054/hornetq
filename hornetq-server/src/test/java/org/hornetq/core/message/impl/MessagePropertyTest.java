/*
 * Copyright 2005-2014 Red Hat, Inc.
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.hornetq.core.message.impl;

import org.hornetq.api.core.SimpleString;
import org.hornetq.api.core.client.ClientConsumer;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.ClientSessionFactory;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.tests.util.ServiceTestBase;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Francisco Borges
 */
public class MessagePropertyTest extends ServiceTestBase
{
   private HornetQServer server;
   private ServerLocator locator;
   private ClientSessionFactory sf;
   private final int numMessages = 20;

   private static final String ADDRESS = "anAddress123";
   private static final SimpleString SIMPLE_STRING_KEY = new SimpleString("StringToSimpleString");

   @Override
   @Before
   public void setUp() throws Exception
   {
      super.setUp();
      server = createServer(true);
      server.start();
      locator = createInVMNonHALocator();
      sf = createSessionFactory(locator);
   }

   private void sendMessages() throws Exception
   {
      ClientSession session = sf.createSession(true, true);
      session.createQueue(ADDRESS, ADDRESS, null, true);
      ClientProducer producer = session.createProducer(ADDRESS);

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = session.createMessage(true);
         setBody(i, message);
         message.putIntProperty("int", i);
         message.putShortProperty("short", (short) i);
         message.putByteProperty("byte", (byte) i);
         message.putFloatProperty("float", floatValue(i));
         message.putStringProperty(SIMPLE_STRING_KEY, new SimpleString(Integer.toString(i)));
         message.putBytesProperty("byte[]", byteArray(i));
         message.putObjectProperty("null-value", null);
         producer.send(message);
      }
      session.commit();
   }

   private float floatValue(int i)
   {
      return (float) (i * 1.3);
   }

   private byte[] byteArray(int i)
   {
      return new byte[]{(byte) i, (byte) (i / 2)};
   }

   @Test
   public void testProperties() throws Exception
   {
      sendMessages();
      receiveMessages();
   }


   private void receiveMessages() throws Exception
   {
      ClientSession session = sf.createSession(true, true);
      session.start();
      ClientConsumer consumer = session.createConsumer(ADDRESS);
      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer.receive(100);
         assertNotNull("Expecting a message " + i, message);
         assertMessageBody(i, message);
         assertEquals(i, message.getIntProperty("int").intValue());
         assertEquals((short) i, message.getShortProperty("short").shortValue());
         assertEquals((byte) i, message.getByteProperty("byte").byteValue());
         assertEquals(floatValue(i), message.getFloatProperty("float").floatValue(), 0.001);
         assertEquals(new SimpleString(Integer.toString(i)),
                      message.getSimpleStringProperty(SIMPLE_STRING_KEY.toString()));
         assertEqualsByteArrays(byteArray(i), message.getBytesProperty("byte[]"));

         assertTrue(message.containsProperty("null-value"));
         assertEquals(message.getObjectProperty("null-value"), null);

         message.acknowledge();
      }
      assertNull("no more messages", consumer.receive(50));
      consumer.close();
      session.commit();
   }

}

/**
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.test.messaging.tools.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.jboss.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.StringReader;
import java.io.Reader;
import java.io.InputStreamReader;
import java.net.URL;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:ovidiu@jboss.org">Ovidiu Feodorov</a>
 * @version <tt>$Revision$</tt>
 * $Id$
 */
public class XMLUtil
{
   // Constants -----------------------------------------------------

   private static final Logger log = Logger.getLogger(XMLUtil.class);

   // Static --------------------------------------------------------

   public static Element stringToElement(String s) throws Exception
   {
      return readerToElement(new StringReader(s));
   }

   public static Element urlToElement(URL url) throws Exception
   {
      return readerToElement(new InputStreamReader(url.openStream()));
   }

   public static Element readerToElement(Reader r) throws Exception
   {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder parser = factory.newDocumentBuilder();
      Document doc = parser.parse(new InputSource(r));
      return doc.getDocumentElement();
   }

   public static String elementToString(Node n) throws Exception
   {

      String name = n.getNodeName();
      if (name.startsWith("#"))
      {
         return "";
      }

      StringBuffer sb = new StringBuffer();
      sb.append('<').append(name);

      NamedNodeMap attrs = n.getAttributes();
      if (attrs != null)
      {
         for(int i = 0; i < attrs.getLength(); i++)
         {
            Node attr = attrs.item(i);
            sb.append(' ').append(attr.getNodeName() + "=\"" + attr.getNodeValue() + "\"");
         }
      }

      NodeList children = n.getChildNodes();

      if (children.getLength() == 0)
      {
         sb.append("/>").append('\n');
      }
      else
      {
         sb.append('>').append('\n');
         for(int i = 0; i < children.getLength(); i++)
         {
            sb.append(elementToString(children.item(i)));

         }
         sb.append("</").append(name).append('>');
      }
      return sb.toString();
   }


   private static final Object[] EMPTY_ARRAY = new Object[0];

   /**
    * This metod is here because Node.getTextContent() is not available in JDK 1.4 and I would like
    * to have an uniform access to this functionality.
    *
    * Note: if the content is another element or set of elements, it returns a string representation
    *       of the hierarchy.
    */
   public static String getTextContent(Node n) throws Exception
   {
      if (n.hasChildNodes())
      {
         StringBuffer sb = new StringBuffer();
         NodeList nl = n.getChildNodes();
         for(int i = 0; i < nl.getLength(); i++)
         {
            sb.append(XMLUtil.elementToString(nl.item(i)));
            if (i < nl.getLength() - 1)
            {
               sb.append('\n');
            }
         }

         String s = sb.toString();
         if (s.length() != 0)
         {
            return s;
         }
      }

      Method[] methods = Node.class.getMethods();

      for(int i = 0; i < methods.length; i++)
      {
         if("getTextContent".equals(methods[i].getName()))
         {
            Method getTextContext = methods[i];
            try
            {
               return (String)getTextContext.invoke(n, EMPTY_ARRAY);
            }
            catch(Exception e)
            {
               log.error("Failed to invoke getTextContent() on node " + n, e);
               return null;
            }
         }
      }

      // JDK 1.4

      String s = n.toString();
      int i = s.indexOf('>');
      int i2 = s.indexOf("</");
      if (i == -1 || i2 == -1)
      {
         log.error("Invalid string expression: " + s);
         return null;
      }
      return s.substring(i + 1, i2);

   }

   public static void assertEquivalent(Node node, Node node2)
   {
      if (node == null || node2 == null)
      {
         throw new XMLRuntimeException("at least on of the node is null");
      }

      if (!node.getNodeName().equals(node2.getNodeName()))
      {
         throw new XMLRuntimeException("nodes have different node names");
      }

      boolean hasAttributes = node.hasAttributes();

      if (hasAttributes != node2.hasAttributes())
      {
         throw new XMLRuntimeException("one node has attributes and the other doesn't");
      }

      if (hasAttributes)
      {
         NamedNodeMap attrs = node.getAttributes();
         int length = attrs.getLength();

         NamedNodeMap attrs2 = node2.getAttributes();
         int length2 = attrs2.getLength();

         if (length != length2)
         {
            throw new XMLRuntimeException("nodes hava a different number of attributes");
         }

         outer: for(int i = 0; i < length; i++)
         {
            Node n = attrs.item(i);
            String name = n.getNodeName();
            String value = n.getNodeValue();

            for(int j = 0; j < length; j++)
            {
               Node n2 = attrs2.item(i);
               String name2 = n2.getNodeName();
               String value2 = n2.getNodeValue();

               if (name.equals(name2) && value.equals(value2))
               {
                  continue outer;
               }
            }
            throw new XMLRuntimeException("attribute " + name + "=" + value + " doesn't match");
         }
      }

      boolean hasChildren = node.hasChildNodes();

      if (hasChildren != node2.hasChildNodes())
      {
         throw new XMLRuntimeException("one node has children and the other doesn't");
      }

      if (hasChildren)
      {
         NodeList nl = node.getChildNodes();
         NodeList nl2 = node2.getChildNodes();

         int length = nl.getLength();

         if (length != nl2.getLength())
         {
            throw new XMLRuntimeException("nodes hava a different number of children");
         }

         for(int i = 0; i < length; i++)
         {
            Node n = nl.item(i);
            Node n2 = nl2.item(i);
            assertEquivalent(n, n2);
         }
      }
   }

   // Attributes ----------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}

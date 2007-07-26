/* 
 * ========================================================================
 * 
 * Copyright 2005-2007 Vincent Massol.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ========================================================================
 */
package org.codehaus.cargo.module.webapp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.codehaus.cargo.util.CargoException;
import org.jdom.JDOMException;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX Handler for working out what the type of a web descriptor is.
 *
 * @version $Id: $
 */
public class WebXmlTypeAwareParser extends DefaultHandler 
{
    protected WebXmlVersion version;
    
    BufferedInputStream bufferedStream;
    
    EntityResolver      theEntityResolver;
    
    WebXml              webXml;
    
    /**
     * @param theInput
     * @param theEntityResolver 
     * @throws IOException 
     */
    public WebXmlTypeAwareParser(InputStream theInput, EntityResolver theEntityResolver)
    {
      this.bufferedStream = new BufferedInputStream(theInput);
      this.theEntityResolver = theEntityResolver;
    }
    
    public WebXml parse() throws IOException, JDOMException
    {
      bufferedStream.mark(1024*1024);
      
      // Trying to find out what the DOCTYPE declaration from SAX seems to be
      // unbelievably difficult unless you rely on implementation specifics.
      // Do something cheap instead - sniff the first few lines for decls until we
      // see the web-app definition. 
      
      BufferedReader reader = new BufferedReader( new InputStreamReader(bufferedStream) );
      String line;
      
      while( (line = reader.readLine()) != null && this.version == null )
      {
        if( line.contains(WebXmlVersion.V2_2.getPublicId()) )
          version = WebXmlVersion.V2_2;
        
        if( line.contains(WebXmlVersion.V2_3.getPublicId()) )
          version = WebXmlVersion.V2_3;
        
        if( line.contains("<web-app") )
          break;
      }
      
      if( this.version != null )
      {
        generateWebXml();
      }
      else
      {
        try
        {        
          bufferedStream.reset();
          bufferedStream.mark(1024*1024);
          SAXParser parser = SAXParserFactory.newInstance().newSAXParser();               
          
          parser.parse( new InputSource(bufferedStream), this);
        }
        catch (SAXException e)
        {
           // This exception is expected - the handler aborts the reading when it has worked
           // out what the type is.
        }
        catch(Exception ex)
        {
          // Something went wrong - just try normal generation
          throw new CargoException("Problem in parsing", ex);
        }
      }
      
      return this.webXml;      
    }

    private void generateWebXml() throws IOException, JDOMException
    {
      bufferedStream.reset();      
      
      // Default to 2.3 if nothing else specified
      WebXmlType descriptorType = WebXml23Type.getInstance();
      
      if( WebXmlVersion.V2_2.equals(getVersion()) )
      {
         descriptorType = WebXml22Type.getInstance();
      }
      else if ( WebXmlVersion.V2_4.equals(getVersion()) )
      {
        descriptorType = WebXml24Type.getInstance();
      }        
      
      webXml =  (WebXml) descriptorType.getDescriptorIo().parseXml(
          bufferedStream, theEntityResolver);        
    }
    
    public void notationDecl(String namespaceURI, String sName, String qName) throws SAXException
    {
      System.out.println(namespaceURI);

    }
    
    public void unparsedEntityDecl(java.lang.String arg0, java.lang.String arg1, java.lang.String arg2, java.lang.String arg3) throws org.xml.sax.SAXException
    {
      System.out.println(arg0);
      
    }
    
    public void startElement(String namespaceURI, String sName, String qName, Attributes attrs)
        throws org.xml.sax.SAXException
    {
      try
      {
      
        if( "2.4".equals( attrs.getValue("version") ) )
        {
          version = WebXmlVersion.V2_4;
        }
        
        generateWebXml();
      }
      catch(Exception ex)
      {
        throw new CargoException("Problem in parsing web xml file", ex);
      }
      throw new SAXException("Finished examining file - stop the parser");
    }
  
    
    
    /**
     * @return the version
     */
    public WebXmlVersion getVersion()
    {
      return this.version;
    }

    
}

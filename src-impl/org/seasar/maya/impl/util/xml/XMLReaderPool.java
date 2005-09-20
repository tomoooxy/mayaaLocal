/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which 
 * accompanies this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package org.seasar.maya.impl.util.xml;

import org.apache.xerces.parsers.SAXParser;
import org.seasar.maya.impl.util.collection.AbstractSoftReferencePool;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class XMLReaderPool extends AbstractSoftReferencePool {

	private static final long serialVersionUID = 1736077679163143852L;

	private static XMLReaderPool _xmlReaderPool;
    
    public static XMLReaderPool getPool() {
        if(_xmlReaderPool == null) {
            _xmlReaderPool = new XMLReaderPool();
        }
        return _xmlReaderPool;
    }
    
    protected XMLReaderPool() {
    }
    
    protected Object createObject() {
        XMLReader xmlReader = new SAXParser();
        return xmlReader;
    }
    
    protected boolean validateObject(Object object) {
        return object instanceof XMLReader;
    }

    protected void setFeature(
    		XMLReader xmlReader, String name, boolean value) {
        try {
            xmlReader.setFeature(name, value);
        } catch (SAXNotRecognizedException e) {
        } catch (SAXNotSupportedException e) {
        }
    }
    
    protected void setProperty(
    		XMLReader xmlReader, String name, Object value) {
        try {
            xmlReader.setProperty(name, value);
        } catch (SAXNotRecognizedException e) {
        } catch (SAXNotSupportedException e) {
        }
    }
    
	public XMLReader borrowXMLReader(ContentHandler handler, 
	        boolean namespaces, boolean validation, boolean xmlSchema) {
	    XMLReader xmlReader = (XMLReader)borrowObject();
        setFeature(xmlReader,
        	"http://xml.org/sax/features/namespaces", namespaces);
        setFeature(xmlReader,
           	"http://xml.org/sax/features/validation", validation);
        setFeature(xmlReader,
            "http://apache.org/xml/features/validation/schema", xmlSchema);
        xmlReader.setContentHandler(handler);
        if(handler instanceof EntityResolver) {
            xmlReader.setEntityResolver((EntityResolver)handler);
        }
        if(handler instanceof ErrorHandler) {
            xmlReader.setErrorHandler((ErrorHandler)handler);
        }
        if(handler instanceof DTDHandler) {
            xmlReader.setDTDHandler((DTDHandler)handler);
        }
        if(handler instanceof LexicalHandler) {
			setProperty(xmlReader,
				"http://xml.org/sax/properties/lexical-handler", handler);
        }
	    return xmlReader;
	}
	
	public void returnXMLReader(XMLReader xmlReader) {
	    returnObject(xmlReader);
	}
	
}
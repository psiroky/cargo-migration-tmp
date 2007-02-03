/* 
 * ========================================================================
 * 
 * Copyright 2005-2006 Vincent Massol.
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
package org.codehaus.cargo.module.webapp.orion;

import org.codehaus.cargo.module.AbstractDescriptor;
import org.codehaus.cargo.module.DescriptorTag;
import org.codehaus.cargo.module.Dtd;
import org.codehaus.cargo.module.webapp.VendorWebAppDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Encapsulates the DOM representation of a oracle web deployment descriptor 
 * <code>orion-web.xml</code> to provide convenience methods for easy access and manipulation.
 *
 * @version $Id$
 */
public class OrionWebXml extends AbstractDescriptor implements VendorWebAppDescriptor
{
    /**
     * File name of this descriptor.
     */
    private static final String FILE_NAME = "orion-web.xml";
    
    /**
     * Constructor.
     * 
     * @param document The DOM document representing the parsed deployment descriptor
     */
    public OrionWebXml(Document document)
    {
        super(document, new Dtd("http://www.oracle.com/technology/ias/dtds/orion-web-9_04.dtd"));
    }
    
    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.Descriptor#getFileName()
     */
    public final String getFileName()
    {
        return FILE_NAME;
    }
    
    /**
     * Adds a ejb reference description to orion-web.xml.
     * @param name name of the reference
     * @param jndiName jndi name to map
     */
    public final void addEjbReference(String name, String jndiName)
    {
        Element ejbRefElement = getDocument().createElement("ejb-ref-mapping");
        ejbRefElement.setAttribute("name", name);
        ejbRefElement.setAttribute("location", jndiName);
        addElement(
            new DescriptorTag("ejb-ref-mapping", true), ejbRefElement, getRootElement());
    }
}

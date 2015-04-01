/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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
package org.codehaus.cargo.container.jetty.internal;

import org.codehaus.cargo.container.jetty.JettyPropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;

/**
 * Configuration capability for a Jetty 6.x Embedded container.
 * 
 */
public class Jetty6xEmbeddedStandaloneLocalConfigurationCapability extends
    Jetty4xEmbeddedStandaloneLocalConfigurationCapability
{
    /**
     * Constructor.
     */
    public Jetty6xEmbeddedStandaloneLocalConfigurationCapability()
    {
        super();

        this.supportsMap.put(GeneralPropertySet.LOGGING, Boolean.FALSE);

        this.supportsMap.put(JettyPropertySet.SESSION_PATH, Boolean.TRUE);
        this.supportsMap.put(JettyPropertySet.USE_FILE_MAPPED_BUFFER, Boolean.TRUE);
        this.supportsMap.put(JettyPropertySet.CREATE_CONTEXT_XML, Boolean.TRUE);
    }
}

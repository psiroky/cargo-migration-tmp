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
package org.codehaus.cargo.container.jo.internal;

import java.util.Map;

import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfigurationCapability;

/**
 * Capabilities of Jo's standalone local configuration.
 *
 * @version $Id$
 */
public class Jo1xStandaloneLocalConfigurationCapability
    extends AbstractStandaloneLocalConfigurationCapability
{

    /**
     * Initialize the configuration-specific supports Map.
     */
    public Jo1xStandaloneLocalConfigurationCapability()
    {
        super();

        this.defaultSupportsMap.put(ServletPropertySet.USERS, Boolean.FALSE);
        this.defaultSupportsMap.put(GeneralPropertySet.PROTOCOL, Boolean.FALSE);

        this.defaultSupportsMap.put(GeneralPropertySet.RMI_PORT, Boolean.TRUE);
    }

    /**
     * {@inheritDoc}
     * @see AbstractStandaloneLocalConfigurationCapability#getPropertySupportMap()
     */
    protected Map getPropertySupportMap()
    {
        return this.defaultSupportsMap;
    }
}

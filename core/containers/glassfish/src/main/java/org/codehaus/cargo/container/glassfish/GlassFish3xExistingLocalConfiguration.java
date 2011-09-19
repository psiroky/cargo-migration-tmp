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
package org.codehaus.cargo.container.glassfish;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;

/**
 * GlassFish 3.x existing local configuration.
 * 
 * @version $Id$
 */
public class GlassFish3xExistingLocalConfiguration extends GlassFish2xExistingLocalConfiguration
{

    /**
     * Container capability instance.
     */
    private static final ConfigurationCapability CAPABILITY =
        new GlassFish3xExistingLocalConfigurationCapability();

    /**
     * Creates the local configuration object.
     * 
     * @param home The work directory where files needed to run Glassfish will be created.
     */
    public GlassFish3xExistingLocalConfiguration(String home)
    {
        super(home);
    }

    /**
     * {@inheritDoc}
     */
    public ConfigurationCapability getCapability()
    {
        return CAPABILITY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        super.doConfigure(container);

        InstalledLocalContainer installedContainer = (InstalledLocalContainer) container;
        String[] classPath = installedContainer.getExtraClasspath();
        if (classPath != null)
        {
            String toDir = getFileHandler().append(this.getHome(),
                this.getPropertyValue(GlassFishPropertySet.DOMAIN_NAME) + "/lib");

            for (String path : classPath)
            {
                String dest = getFileHandler().append(toDir, getFileHandler().getName(path));
                getFileHandler().copyFile(path, dest);
            }
        }
    }

}

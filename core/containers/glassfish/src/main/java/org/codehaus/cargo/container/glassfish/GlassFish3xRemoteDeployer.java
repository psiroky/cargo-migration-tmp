/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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

import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;

import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.spi.deployer.AbstractJsr88Deployer;
import org.codehaus.cargo.util.CargoException;
import org.glassfish.deployapi.SunDeploymentFactory;

/**
 * GlassFish remote deployer, which uses the JSR-88 to deploy and undeploy applications.
 *
 * @version $Id$
 */
public class GlassFish3xRemoteDeployer extends AbstractJsr88Deployer
{

    /**
     * Constructor.
     *
     * @param container the remote container
     */
    public GlassFish3xRemoteDeployer(RemoteContainer container)
    {
        super(container);
    }

    /**
     * @return The JSR-88 deployment manager for the target server.
     * @throws CargoException If some parameters are incorrect.
     * @throws DeploymentManagerCreationException If deployment manager creation fails.
     */
    protected DeploymentManager getDeploymentManager() throws CargoException,
        DeploymentManagerCreationException
    {
        DeploymentFactoryManager dfm = DeploymentFactoryManager.getInstance();
        dfm.registerDeploymentFactory(new SunDeploymentFactory());

        String hostname = this.getRuntimeConfiguration().getPropertyValue(
            GeneralPropertySet.HOSTNAME);
        String port = this.getRuntimeConfiguration().getPropertyValue(
            GlassFishPropertySet.ADMIN_PORT);
        String username = this.getRuntimeConfiguration().getPropertyValue(
            RemotePropertySet.USERNAME);
        String password = this.getRuntimeConfiguration().getPropertyValue(
            RemotePropertySet.PASSWORD);

        return dfm.getDeploymentManager("deployer:Sun:AppServer::" + hostname + ":" + port,
            username, password);
    }
}

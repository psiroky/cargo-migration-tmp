/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
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
package org.codehaus.cargo.container.jonas;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer;
import org.codehaus.cargo.util.FileHandler;

/**
 * Static deployer that deploys WAR, EAR, EJB, RAR, File and Bundle to JOnAS.
 * 
 * @version $Id$
 */
public class Jonas5xInstalledLocalDeployer extends AbstractCopyingInstalledLocalDeployer
{
    /**
     * {@inheritDoc}
     * 
     * @see AbstractCopyingInstalledLocalDeployer#AbstractCopyingInstalledLocalDeployer(InstalledLocalContainer)
     */
    public Jonas5xInstalledLocalDeployer(InstalledLocalContainer container)
    {
        this(container, null);
    }

    /**
     * Creation of a local deployer with a given file handler.
     * 
     * @param container the container to be used
     * @param fileHandler the file handler to use, can be null to use the default file handler
     * implementation
     */
    public Jonas5xInstalledLocalDeployer(InstalledLocalContainer container, FileHandler fileHandler)
    {
        super(container);
        if (fileHandler != null)
        {
            super.setFileHandler(fileHandler);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see AbstractCopyingInstalledLocalDeployer#undeploy(Deployable)
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        throw new ContainerException(
            "The jonas5x local container does not support undeploy or redeploy operations. "
            + "Please use the jonas5x remote container instead.");
    }

    /**
     * {@inheritDoc}
     *
     * @see AbstractCopyingInstalledLocalDeployer#redeploy(Deployable)
     */
    @Override
    public void redeploy(Deployable deployable)
    {
        throw new ContainerException(
            "The jonas5x local container does not support undeploy or redeploy operations. "
            + "Please use the jonas5x remote container instead.");
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractCopyingInstalledLocalDeployer#getDeployableDir()
     */
    @Override
    public String getDeployableDir()
    {
        return getContainer().getConfiguration().getHome() + "/deploy";
    }
}

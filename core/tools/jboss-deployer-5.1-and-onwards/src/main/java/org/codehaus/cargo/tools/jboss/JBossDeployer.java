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
package org.codehaus.cargo.tools.jboss;

import java.io.File;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.jboss.JBossPropertySet;
import org.codehaus.cargo.container.jboss.internal.IJBossProfileManagerDeployer;
import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.deployers.spi.management.deploy.DeploymentProgress;
import org.jboss.deployers.spi.management.deploy.DeploymentStatus;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;

/**
 * JBoss deployer implementation.
 * 
 * @version $Id$
 */
public class JBossDeployer implements IJBossProfileManagerDeployer
{

    /**
     * RMI provider URL, for example <code>jnp://localhost:1099</code>.
     */
    private final String providerURL;
    
    /**
     * Container configuration.
     */
    private Configuration configuration;

    /**
     * @param providerURL Provider URL to use.
     * @param configuration Configuration of the container.
     */
    public JBossDeployer(final String providerURL, Configuration configuration)
    {
        this.providerURL = providerURL;
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     * @see IJBossProfileManagerDeployer#deploy(File, String)
     */
    public void deploy(File deploymentFile, String deploymentName) throws Exception
    {
        DeploymentManager deployMgr = getDeploymentManager();

        deployMgr.loadProfile(getProfile());
        DeploymentProgress distribute = deployMgr.distribute(deploymentName,
            deploymentFile.toURI().toURL(), true);
        distribute.run();
        checkFailed(distribute);

        String[] repositoryNames = distribute.getDeploymentID().getRepositoryNames();
        DeploymentProgress start = deployMgr.start(repositoryNames);
        start.run();
        checkFailed(start);
    }

    /**
     * {@inheritDoc}
     * @see IJBossProfileManagerDeployer#undeploy(String)
     */
    public void undeploy(final String deploymentName) throws Exception
    {
        DeploymentManager deployMgr = getDeploymentManager();

        String[] deploymentNameArray = new String[1];
        deploymentNameArray[0] = deploymentName;
        deployMgr.loadProfile(getProfile());
        String[] repositoryNames = deployMgr.getRepositoryNames(deploymentNameArray);
        DeploymentProgress stop = deployMgr.stop(repositoryNames);
        stop.run();
        checkFailed(stop);
        DeploymentProgress remove = deployMgr.remove(repositoryNames);
        remove.run();
        checkFailed(remove);
    }

    /**
     * @return JBoss profile for the {@link Configuration}.
     */
    private ProfileKey getProfile()
    {
        String server = this.configuration.getPropertyValue(JBossPropertySet.CONFIGURATION);
        if (server == null || server.trim().length() == 0)
        {
            server = ProfileKey.DEFAULT;
        }

        Boolean isClustered = Boolean.valueOf(this.configuration.getPropertyValue(
            JBossPropertySet.CLUSTERED));
        String name = isClustered ? "farm" : ProfileKey.DEFAULT;

        return new ProfileKey(ProfileKey.DEFAULT, server, name);
    }

    /**
     * @param progress DP to check for failure.
     * @throws Exception If progress has failed.
     */
    private void checkFailed(DeploymentProgress progress) throws Exception
    {
        final int timeout = 30;
        DeploymentStatus status = progress.getDeploymentStatus();
        for (int i = 0; i < 30; i++)
        {
            Thread.sleep(1000);
            if (status.isCompleted() || status.isFailed())
            {
                break;
            }
            if (i == timeout - 1)
            {
                throw new Exception("Operation timed out");
            }
        }
        if (status.isFailed())
        {
            Exception cause = status.getFailure();
            throw new Exception("Remote action failed: " + status.getMessage()
                + " (" + cause.getMessage() + ")", cause);
        }
    }

    /**
     * @return The JBoss deployment manager.
     * @throws Exception If anything fails.
     */
    private DeploymentManager getDeploymentManager() throws Exception
    {
        Properties props = new Properties();
        props.setProperty(
            Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
        props.put(Context.PROVIDER_URL, this.providerURL);
        props.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
        Context ctx = new InitialContext(props);

        ProfileService ps = (ProfileService) ctx.lookup("ProfileService");

        return ps.getDeploymentManager();
    }

}

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
package org.codehaus.cargo.maven2;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.maven2.configuration.Deployable;
import org.codehaus.cargo.tools.daemon.DaemonClient;
import org.codehaus.cargo.tools.daemon.DaemonPropertySet;

/**
 * Common mojo for all daemon actions (start deployable, stop deployable).
 * 
 * @version $Id: $
 */
public abstract class AbstractDaemonMojo extends AbstractCargoMojo
{
    /**
     * The daemon client instance.
     */
    protected DaemonClient daemonClient = null;

    /**
     * The daemon handle identifier to use.
     */
    protected String daemonHandleId = null;

    /**
     * The deployables to deploy.
     */
    protected final List<org.codehaus.cargo.container.deployable.Deployable> daemonDeployables =
        new ArrayList<org.codehaus.cargo.container.deployable.Deployable>();
    
    /**
     * The container that should be started by the daemon.
     */
    protected InstalledLocalContainer daemonContainer;
    
        

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.maven2.AbstractCargoMojo#doExecute()
     */ 
    @Override
    public void doExecute() throws MojoExecutionException
    {
        getCargoProject().setDaemonRun(true);
        
        if (getCargoProject().getPackaging() == null || !getCargoProject().isJ2EEPackaging())
        {
            if (getDeployablesElement() == null || getDeployablesElement().length == 0)
            {
                getLog().info("There's nothing to deploy or undeploy");
                return;
            }
        }
        
        
        org.codehaus.cargo.container.Container container = createContainer();
        
        if (!(container instanceof InstalledLocalContainer))
        {
            throw new MojoExecutionException("Container must be of INSTALLED type.");
        }

        String daemonURL = getDaemonProperty(DaemonPropertySet.URL);
        String daemonHandleId = getDaemonProperty(DaemonPropertySet.HANDLE);

        if (daemonURL == null || daemonURL.length() == 0)
        {
            throw new MojoExecutionException("Missing daemon URL property.");
        }

        if (daemonHandleId == null || daemonHandleId.length() == 0)
        {
            throw new MojoExecutionException("Missing daemon handle id property.");
        }

        try
        {
            this.daemonClient = new DaemonClient(new URL(daemonURL));
        }
        catch (MalformedURLException e)
        {
            throw new MojoExecutionException("Malformed daemon URL: " + e);
        }
        this.daemonHandleId = daemonHandleId;
        this.daemonContainer = (InstalledLocalContainer) container;

        createDeployables(container);
        
        performAction();
    }
    
    /**
     * Performs the actual action.
     * 
     * @throws MojoExecutionException If an error happens
     */
    protected abstract void performAction() throws MojoExecutionException;

    /**
     * Perform deployment action on all deployables (defined in the deployer configuration element
     * and on the autodeployable).
     * 
     * @param container the container to deploy to the daemon
     * @throws MojoExecutionException in case of a deployment error
     */
    private void createDeployables(org.codehaus.cargo.container.Container container)
        throws MojoExecutionException
    {
        List<Deployable> deployableElements = new ArrayList<Deployable>();

        if (getDeployablesElement() != null)
        {
            for (Deployable deployableElement : getDeployablesElement())
            {
                if (!deployableElements.contains(deployableElement))
                {
                    deployableElements.add(deployableElement);
                }
            }
        }

        for (Deployable deployableElement : deployableElements)
        {
            org.codehaus.cargo.container.deployable.Deployable deployable =
                deployableElement.createDeployable(container.getId(), getCargoProject());
            URL pingURL = deployableElement.getPingURL();
            Long pingTimeout = deployableElement.getPingTimeout();
            addDeployable(deployable, pingURL, pingTimeout);
        }

        // Perform deployment action on the autodeployable (if any).
        if (getCargoProject().getPackaging() != null && getCargoProject().isJ2EEPackaging())
        {
            Deployable[] deployableElementsArray = new Deployable[deployableElements.size()];
            deployableElements.toArray(deployableElementsArray);

            if (!containsAutoDeployable(deployableElementsArray))
            {
                // The ping URL is always null here because if the user has specified a ping URL
                // then the auto deployable has already been deployed as it's been explicitely
                // specified by the user...
                addDeployable(createAutoDeployDeployable(container), null, null);
            }
        }
    }

    /**
     * Adds a deployable to the list.
     * 
     * @param deployable The deployable
     * @param pingURL The pingURL
     * @param pingTimeout The pingTimeout
     */
    private void addDeployable(org.codehaus.cargo.container.deployable.Deployable deployable,
        URL pingURL, Long pingTimeout)
    {
        daemonDeployables.add(deployable);
    }



    // /**
    // * Create a deployable monitor.
    // * @param pingURL Ping URL.
    // * @param pingTimeout Ping timeout (milliseconds).
    // * @param deployable {@link Deployable} to monitor.
    // * @return Deployable monitor with specified arguments.
    // */
    // protected DeployableMonitor createDeployableMonitor(URL pingURL, Long pingTimeout,
    // org.codehaus.cargo.container.deployable.Deployable deployable)
    // {
    // DeployableMonitor monitor;
    // if (pingTimeout == null)
    // {
    // monitor = new URLDeployableMonitor(pingURL);
    // }
    // else
    // {
    // monitor = new URLDeployableMonitor(pingURL, pingTimeout.longValue());
    // }
    // DeployerListener listener = new DeployerListener(deployable);
    // monitor.registerListener(listener);
    // return monitor;
    // }
}

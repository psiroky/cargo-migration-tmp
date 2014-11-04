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
package org.codehaus.cargo.container.tomcat;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer;
import org.codehaus.cargo.container.tomcat.internal.TomcatUtils;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.Dom4JUtil;
import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Static deployer that deploys WARs to the Tomcat <code>webapps</code> directory.
 * 
 * @version $Id$
 */
public class TomcatCopyingInstalledLocalDeployer extends AbstractCopyingInstalledLocalDeployer
{
    /**
     * @see #setShouldCopyWars(boolean)
     */
    private boolean shouldCopyWars = true;

    /**
     * {@inheritDoc}
     * @see AbstractCopyingInstalledLocalDeployer#AbstractCopyingInstalledLocalDeployer(InstalledLocalContainer)
     */
    public TomcatCopyingInstalledLocalDeployer(InstalledLocalContainer container)
    {
        super(container);
    }

    /**
     * {@inheritDoc}. For Tomcat this is the <code>webapps</code> directory.
     */
    @Override
    public String getDeployableDir(Deployable deployable)
    {
        return getFileHandler().append(getContainer().getConfiguration().getHome(),
            getContainer().getConfiguration().getPropertyValue(
                TomcatPropertySet.WEBAPPS_DIRECTORY));
    }

    /**
     * Whether the local deployer should copy the wars to the Tomcat webapps directory. This is
     * because Tomcat standalone configuration may not want to copy wars and instead configure
     * server.xml to point to where the wars are located instead of copying them.
     * 
     * @param shouldCopyWars true if the wars should be copied
     */
    public void setShouldCopyWars(boolean shouldCopyWars)
    {
        this.shouldCopyWars = shouldCopyWars;
    }

    /**
     * {@inheritDoc}. We override the base implementation in order to handle the special Tomcat
     * scenarios: if the deployable is a {@link TomcatWAR} instance and it contains a
     * <code>context.xml</code> file that we need to manually copy.
     */
    @Override
    protected void doDeploy(String deployableDir, Deployable deployable)
    {
        if (DeployableType.WAR.equals(deployable.getType()))
        {
            WAR war = (WAR) deployable;
            if (deployable.isExpanded())
            {
                if (TomcatUtils.containsContextFile(war))
                {
                    // If the WAR contains a META-INF/context.xml then it means the user is
                    // defining how to deploy it.
                    String contextDir = getFileHandler().createDirectory(
                        getContainer().getConfiguration().getHome(),
                            "conf/Catalina/" + getContainer().getConfiguration().getPropertyValue(
                                GeneralPropertySet.HOSTNAME));

                    getLogger().info("Deploying WAR by creating Tomcat context XML file in ["
                        + contextDir + "]...", getClass().getName());

                    // Copy only the context.xml to <config>/Catalina/<hostname>/<context-path>.xml
                    // and set docBase to point at the expanded WAR
                    Dom4JUtil xmlUtil = new Dom4JUtil(getFileHandler());
                    Document doc =
                        xmlUtil.loadXmlFromFile(getFileHandler().append(war.getFile(),
                            "META-INF/context.xml"));
                    Element context = doc.getRootElement();
                    if (context.attributeValue("docBase", "").length() <= 0)
                    {
                        context.addAttribute("docBase", war.getFile());
                    }
                    configureExtraClasspath(war, context);
                    xmlUtil.saveXml(doc,
                        getFileHandler().append(contextDir, war.getContext() + ".xml"));
                }
                else if (this.shouldCopyWars)
                {
                    super.doDeploy(deployableDir, war);
                }
                else
                {
                    // Else, do nothing since the context.xml will reference the existing file
                }
            }
            else
            {
                if (TomcatUtils.containsContextFile(war))
                {
                    // Drop WAR file into the webapps dir, Tomcat will read META-INF/context.xml
                    super.doDeploy(deployableDir, war);
                }
                else if (this.shouldCopyWars)
                {
                    super.doDeploy(deployableDir, war);
                }
                else
                {
                    // Else, do nothing since the context.xml will reference the existing file
                }
            }
        }
        else
        {
            super.doDeploy(deployableDir, deployable);
        }
    }

    /**
     * Configures the specified context element with the extra classpath (if any) of the given WAR.
     * 
     * @param war The WAR whose extra classpath should be configured, must not be {@code null}.
     * @param context The context element to configure, must not be {@code null}.
     */
    private void configureExtraClasspath(WAR war, Element context)
    {
        String extraClasspath = TomcatUtils.getExtraClasspath(war, true);
        if (extraClasspath != null)
        {
            Element loader = context.element("Loader");
            if (loader == null)
            {
                loader = context.addElement("Loader");
            }

            String className =
                loader.attributeValue("className", "org.apache.catalina.loader.WebappLoader");
            if (!"org.apache.catalina.loader.WebappLoader".equals(className)
                && !"org.apache.catalina.loader.VirtualWebappLoader".equals(className))
            {
                throw new CargoException("Extra classpath is not supported"
                    + " for WARs using custom loader: " + className);
            }
            loader.addAttribute("className", "org.apache.catalina.loader.VirtualWebappLoader");

            String virtualClasspath = loader.attributeValue("virtualClasspath", "");
            if (virtualClasspath.length() <= 0)
            {
                virtualClasspath = extraClasspath;
            }
            else
            {
                virtualClasspath = extraClasspath + ";" + virtualClasspath;
            }
            loader.addAttribute("virtualClasspath", virtualClasspath);
        }
    }

    /**
     * Undeploy WAR deployables by deleting the local file from the Tomcat webapps directory.
     * 
     * {@inheritDoc}
     * @see AbstractCopyingInstalledLocalDeployer#undeploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        // Check that the container supports the deployable type to undeploy
        if (!getContainer().getCapability().supportsDeployableType(deployable.getType()))
        {
            throw new ContainerException(getContainer().getName() + " doesn't support ["
                + deployable.getType().getType().toUpperCase() + "] archives. Got ["
                + deployable.getFile() + "]");
        }

        String deployableDir = getDeployableDir(deployable);
        try
        {
            if (deployable.getType() == DeployableType.WAR)
            {
                WAR war = (WAR) deployable;
                String context = war.getContext();
                getLogger().info("Undeploying context [" + context + "] from [" + deployableDir
                    + "]...", this.getClass().getName());

                // Delete either the WAR file or the expanded WAR directory.
                String warLocation;
                if (war.isExpanded())
                {
                    warLocation = getFileHandler().append(deployableDir, context);
                }
                else
                {
                    warLocation = getFileHandler().append(deployableDir, context + ".war");
                }

                if (getFileHandler().exists(warLocation))
                {
                    getLogger().info("Trying to delete WAR from [" + warLocation + "]...",
                        this.getClass().getName());
                    getFileHandler().delete(warLocation);
                }
                else
                {
                    throw new ContainerException("Failed to undeploy as there is no WAR at ["
                        + warLocation + "]");
                }
            }
            else
            {
                throw new ContainerException("Only WAR undeployment is currently supported");
            }
        }
        catch (Exception e)
        {
            throw new ContainerException("Failed to undeploy [" + deployable.getFile()
                + "] from [" + deployableDir + "]", e);
        }
    }

    /**
     * Replace the slashes with <code>#</code> in the deployable name (see: CARGO-1041).
     * {@inheritDoc}
     */
    @Override
    protected String getDeployableName(Deployable deployable)
    {
        return super.getDeployableName(deployable).replace('/', '#');
    }
}

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
package org.codehaus.cargo.container.jboss;

import java.io.File;
import java.util.jar.JarFile;

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.jboss.internal.JBoss7xContainerCapability;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;

/**
 * JBoss 7.x series container implementation.
 * 
 * @version $Id$
 */
public class JBoss7xInstalledLocalContainer extends AbstractInstalledLocalContainer
{
    /**
     * JBoss 7.x series unique id.
     */
    public static final String ID = "jboss7x";

    /**
     * Capability of the JBoss container.
     */
    private static final ContainerCapability CAPABILITY = new JBoss7xContainerCapability();

    /**
     * JBoss version.
     */
    protected String version;

    /**
     * {@inheritDoc}
     * @see AbstractInstalledLocalContainer#AbstractInstalledLocalContainer(LocalConfiguration)
     */
    public JBoss7xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getId()
     */
    public String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getName()
     */
    public String getName()
    {
        return "JBoss " + getVersion("7x");
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getCapability()
     */
    public ContainerCapability getCapability()
    {
        return CAPABILITY;
    }

    /**
     * Parse installed JBoss version.
     * 
     * @return the JBoss version, or <code>defaultVersion</code> if the version number could not be
     * determined
     * @param defaultVersion the default version used if the exact JBoss version can't be determined
     */
    protected String getVersion(String defaultVersion)
    {
        String version = this.version;

        if (version == null)
        {
            try
            {
                File configAdminFile;

                File configAdminDirectory = new File(getHome(),
                    "bundles/org/jboss/as/osgi/configadmin/main");

                if (configAdminDirectory.isDirectory())
                {
                    File[] contents = configAdminDirectory.listFiles();
                    if (contents.length != 1)
                    {
                        throw new IllegalStateException("The directory " + configAdminDirectory
                            + " does not contain exactly one file.");
                    }
                    configAdminFile = contents[0];
                }
                else
                {
                    throw new IllegalArgumentException(configAdminDirectory
                        + " is not a directory.");
                }

                JarFile jarFile = new JarFile(configAdminFile);
                version = jarFile.getManifest().getMainAttributes().getValue("Bundle-Version");

                if (version == null)
                {
                    version = defaultVersion;
                    getLogger().debug("Couldn't find Bundle-Version in the MANIFEST of "
                        + configAdminFile, this.getClass().getName());
                }
            }
            catch (Exception e)
            {
                version = defaultVersion;
                getLogger().debug(
                    "Failed to find JBoss version, base error [" + e.getMessage() + "]",
                    this.getClass().getName());
            }

            getLogger().info("Parsed JBoss version = [" + version + "]",
                this.getClass().getName());

            this.version = version;
        }

        return version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStart(JvmLauncher java) throws Exception
    {
        java.setWorkingDirectory(new File(getConfiguration().getHome()));

        java.addJvmArguments(
            "-Dorg.jboss.boot.log.file=" + getConfiguration().getHome() + "/log/boot.log",
            "-Dlogging.configuration="
                + new File(getConfiguration().getHome()
                    + "/configuration/logging.properties").toURI().toURL(),
            "-Djboss.home.dir=" + getHome(),
            "-Djboss.server.base.dir=" + getConfiguration().getHome());

        java.setJarFile(new File(getHome(), "jboss-modules.jar"));

        java.addAppArguments(
            "-mp", getHome() + "/modules",
            "-logmodule", "org.jboss.logmanager",
            "-jaxpmodule", "javax.xml.jaxp-provider",
            "org.jboss.as.standalone");

        java.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStop(JvmLauncher java) throws Exception
    {
        String port = getConfiguration().getPropertyValue(JBossPropertySet.JBOSS_MANAGEMENT_PORT);

        java.setWorkingDirectory(new File(getConfiguration().getHome()));

        java.setJarFile(new File(getHome(), "jboss-modules.jar"));

        java.addAppArguments(
            "-mp", getHome() + "/modules",
            "-logmodule", "org.jboss.logmanager",
            "org.jboss.as.cli",
            "--connect", "--controller=localhost:" + port,
            "command=:shutdown");

        java.start();
    }
}
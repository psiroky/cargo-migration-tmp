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

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.RuntimeConfiguration;
import org.codehaus.cargo.maven2.configuration.Configuration;
import org.codehaus.cargo.maven2.configuration.Container;
import org.codehaus.cargo.maven2.configuration.Deployable;
import org.codehaus.cargo.maven2.configuration.Deployer;
import org.codehaus.cargo.maven2.jetty.JettyArtifactResolver;
import org.codehaus.cargo.maven2.log.MavenLogger;
import org.codehaus.cargo.maven2.util.CargoProject;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.log.FileLogger;
import org.codehaus.cargo.util.log.LogLevel;
import org.codehaus.cargo.util.log.Logger;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Common code used by Cargo MOJOs requiring <code>&lt;container&gt;</code> and
 * <code>&lt;configuration&gt;</code> elements and supporting the notion of Auto-deployable.
 * 
 * @version $Id$
 */
public abstract class AbstractCargoMojo extends AbstractCommonMojo
{
    /**
     * The key under which the container instance is stored in the plugin context. We store it so
     * that it's possible to get back the same container instance even if this mojo is called in a
     * different Maven execution context. This is required for stopping embedded containers for
     * example as we need to use the same instance that was started in order to stop them.
     */
    public static final String CONTEXT_KEY_CONTAINER =
        AbstractCargoMojo.class.getName() + "-Container";

    /**
     * File utility class.
     */
    private FileHandler fileHandler = new DefaultFileHandler();

    /**
     * Configures a Cargo {@link org.codehaus.cargo.container.configuration.Configuration}. See the
     * <a href="http://cargo.codehaus.org/Maven2+Plugin+Reference+Guide">Cargo Maven2 plugin
     * reference guide</a> for more details.
     * 
     * @parameter
     * @see #getConfigurationElement()
     */
    private Configuration configuration;

    /**
     * Configures a Cargo {@link org.codehaus.cargo.container.Container}. See the <a
     * href="http://cargo.codehaus.org/Maven2+Plugin+Reference+Guide">Cargo Maven2 plugin reference
     * guide</a> for more details.
     * 
     * @parameter
     */
    private Container container;

    /**
     * Configures a Cargo {@link org.codehaus.cargo.container.deployer.Deployer}. See the <a
     * href="http://cargo.codehaus.org/Maven2+Plugin+Reference+Guide">Cargo Maven2 plugin reference
     * guide</a> for more details.
     * 
     * @parameter
     * @see #getDeployerElement()
     */
    private Deployer deployer;

    /**
     * The artifact resolver is used to dynamically resolve JARs that have to be in the embedded
     * container's classpaths. Another solution would have been to statically define them a
     * dependencies in the plugin's POM. Resolving them in a dynamic manner is much better as only
     * the required JARs for the defined embedded container are downloaded.
     * 
     * @component
     */
    private ArtifactResolver artifactResolver;

    /**
     * The local Maven repository. This is used by the artifact resolver to download resolved
     * artifacts and put them in the local repository so that they won't have to be fetched again
     * next time the plugin is executed.
     * 
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * The remote Maven repositories used by the artifact resolver to look for artifacts.
     * 
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @required
     * @readonly
     */
    private List<ArtifactRepository> repositories;

    /**
     * Set this to 'true' to bypass cargo execution.
     * 
     * @parameter expression="${cargo.maven.skip}" default-value="false"
     * @since 1.0.3
     */
    private boolean skip;

    /**
     * The artifact factory is used to create valid Maven {@link org.apache.maven.artifact.Artifact}
     * objects. This is used to pass Maven artifacts to the artifact resolver so that it can
     * download the required JARs to put in the embedded container's classpaths.
     * 
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * @see org.codehaus.cargo.maven2.util.CargoProject
     */
    private CargoProject cargoProject;

    /**
     * Maven settings, injected automatically.
     * 
     * @parameter expression="${settings}"
     * @required
     * @readonly
     */
    private Settings settings;

    /**
     * Calculates the container artifact ID for a given container ID. Note that all containers
     * identifier are in the form <code>containerArtifactId + the version number + x</code>; for
     * example <code>jboss42x</code> is from container artifact ID
     * <code>cargo-core-container-jboss</code>.
     * @param containerId Container ID, for example <code>jboss42x</code>.
     * @return Container artifact ID, for example <code>cargo-core-container-jboss</code>.
     */
    public static String calculateContainerArtifactId(String containerId)
    {
        return "cargo-core-container-" + containerId.replaceAll("\\d+x", "");
    }

    /**
     * @return the Cargo file utility class
     */
    protected FileHandler getFileHandler()
    {
        return this.fileHandler;
    }

    /**
     * @param fileHandler the Cargo file utility class to use. This method is useful for unit
     * testing with Mock objects as it can be passed a test file handler that doesn't perform any
     * real file action.
     */
    protected void setFileHandler(FileHandler fileHandler)
    {
        this.fileHandler = fileHandler;
    }

    /**
     * @return the user configuration of a Cargo
     * {@link org.codehaus.cargo.container.deployer.Deployer}. See the <a
     * href="http://cargo.codehaus.org/Maven2+Plugin+Reference+Guide">Cargo Maven2 plugin reference
     * guide</a> and {@link org.codehaus.cargo.maven2.configuration.Deployer} for more details.
     */
    protected Deployer getDeployerElement()
    {
        return this.deployer;
    }

    /**
     * @param deployerElement the {@link org.codehaus.cargo.container.deployer.Deployer}
     * configuration defined by the user
     * @see #getDeployerElement()
     */
    protected void setDeployerElement(Deployer deployerElement)
    {
        this.deployer = deployerElement;
    }

    /**
     * @return the user configuration of a Cargo
     * {@link org.codehaus.cargo.container.configuration.Configuration}. See the <a
     * href="http://cargo.codehaus.org/Maven2+Plugin+Reference+Guide">Cargo Maven2 plugin reference
     * guide</a> and {@link org.codehaus.cargo.maven2.configuration.Configuration} for more details.
     */
    protected Configuration getConfigurationElement()
    {
        return this.configuration;
    }

    /**
     * @param configurationElement the
     * {@link org.codehaus.cargo.container.configuration.Configuration} configuration defined by the
     * user
     * @see #getConfigurationElement()
     */
    protected void setConfigurationElement(Configuration configurationElement)
    {
        this.configuration = configurationElement;
    }

    /**
     * @return the user configuration of a Cargo {@link org.codehaus.cargo.container.Container}. See
     * the <a href="http://cargo.codehaus.org/Maven2+Plugin+Reference+Guide">Cargo Maven2 plugin
     * reference guide</a> and {@link org.codehaus.cargo.maven2.configuration.Container} for more
     * details.
     */
    protected Container getContainerElement()
    {
        return this.container;
    }

    /**
     * @param containerElement the {@link org.codehaus.cargo.container.Container} configuration
     * defined by the user
     * @see #getContainerElement()
     */
    protected void setContainerElement(Container containerElement)
    {
        this.container = containerElement;
    }

    /**
     * @param cargoProject Cargo project
     */
    protected void setCargoProject(CargoProject cargoProject)
    {
        this.cargoProject = cargoProject;
    }

    /**
     * @return Cargo project
     */
    protected CargoProject getCargoProject()
    {
        return this.cargoProject;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Note: This method is final so that extending classes cannot extend it. Instead they should
     * implement the {@link #doExecute()} method.
     * </p>
     * 
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public final void execute() throws MojoExecutionException
    {
        if (this.skip)
        {
            getLog().info("Skipping cargo execution");
            return;
        }
        if (this.cargoProject == null)
        {
            this.cargoProject = new CargoProject(getProject(), getLog());
        }
        doExecute();
    }

    /**
     * Executes the plugin.
     * 
     * <p>
     * This method must be implemented by all Mojos extending this class. The reason for this
     * pattern is because we want the {@link #execute()} method to always be called so that
     * necessary plugin initialization can be performed. Without this pattern Mojos extending this
     * class could "forget" to call <code>super.execute()</code> thus leading to unpredictible
     * results.
     * </p>
     * 
     * @throws MojoExecutionException in case of error
     */
    protected abstract void doExecute() throws MojoExecutionException;

    /**
     * Creates a {@link org.codehaus.cargo.container.configuration.Configuration} instance. If the
     * user has not specified a configuration element in the POM file then automatically create a
     * standalone configuration if the container's type is local or otherwise create a runtime
     * configuration.
     * 
     * @return a valid {@link org.codehaus.cargo.container.configuration.Configuration} instance
     * @throws MojoExecutionException in case of error
     */
    protected org.codehaus.cargo.container.configuration.Configuration createConfiguration()
        throws MojoExecutionException
    {
        String containerId = getContainerElement().getContainerId();

        if (containerId != null && artifactFactory != null)
        {
            String containerArtifactId =
                AbstractCargoMojo.calculateContainerArtifactId(containerId);
            String pluginVersion = this.getClass().getPackage().getImplementationVersion();
            Artifact containerArtifact = artifactFactory.createArtifact("org.codehaus.cargo",
                containerArtifactId, pluginVersion, null, "jar");
            try
            {
                artifactResolver.resolve(containerArtifact, repositories, localRepository);

                ClassLoader classLoader = (ClassLoader) this.getClass().getClassLoader();
                Method method;
                if (classLoader.getClass().getName().equals(
                    "org.codehaus.classworlds.RealmClassLoader"))
                {
                    method = classLoader.getClass().getMethod("addConstituent",
                        new Class[]{URL.class});
                }
                else
                {
                    method = classLoader.getClass().getMethod("addURL", new Class[]{URL.class});
                }
                method.setAccessible(true);
                method.invoke(classLoader, containerArtifact.getFile().toURI().toURL());

                createLogger().info("Resolved container artifact " + containerArtifact
                    + " for container " + containerId, this.getClass().getName());
            }
            catch (Exception e)
            {
                createLogger().warn("Cannot resolve container artifact " + containerArtifact
                    + " for container " + containerId + ": " + e.toString(),
                    this.getClass().getName());
            }
        }

        org.codehaus.cargo.container.configuration.Configuration configuration;

        // If no configuration element has been specified create one with default values.
        if (getConfigurationElement() == null)
        {
            Configuration configurationElement = new Configuration();

            if (getContainerElement().getType().isLocal())
            {
                File home = new File(getCargoProject().getBuildDirectory(), "cargo/configurations/"
                    + getContainerElement().getContainerId());

                configurationElement.setType(ConfigurationType.STANDALONE);
                configurationElement.setHome(home.getAbsolutePath());
            }
            else
            {
                configurationElement.setType(ConfigurationType.RUNTIME);
            }

            setConfigurationElement(configurationElement);
        }

        configuration = getConfigurationElement().createConfiguration(
            getContainerElement().getContainerId(), getContainerElement().getType(),
            getCargoProject());

        // Find the cargo.server.settings for the current configuration. When found, iterate in
        // the list of servers in Maven's settings.xml file in order to find out which server id
        // corresponds to that identifier, and copy all non-set settings (cargo.remote.uri, ...).
        //
        // This feature helps people out in centralising their configurations.
        Map<String, String> properties = configuration.getProperties();
        for (Map.Entry<String, String> property : properties.entrySet())
        {
            String propertyKey = (String) property.getKey();
            if ("cargo.server.settings".equals(propertyKey))
            {
                String serverId = (String) property.getValue();
                getLog()
                    .debug(
                        "Found cargo.server.settings: key is " + propertyKey + ", value is "
                            + serverId);
                for (Object serverObject : settings.getServers())
                {
                    Server server = (Server) serverObject;
                    if (serverId.equals(server.getId()))
                    {
                        getLog().debug(
                            "The Maven settings.xml file contains a reference for the "
                                + "server with cargo.server.settings " + serverId
                                + ", starting property injection");

                        Xpp3Dom[] globalConfigurationOptions = ((Xpp3Dom) server.getConfiguration())
                            .getChildren();
                        for (Xpp3Dom option : globalConfigurationOptions)
                        {
                            if (properties.get(option.getName()) == null)
                            {
                                properties.put(option.getName(), option.getValue());
                                getLog().debug(
                                    "\tInjected property: " + option.getName() + '='
                                        + option.getValue());
                            }
                        }
                        break;
                    }
                }
                break;
            }
        }

        return configuration;
    }

    /**
     * @return a {@link org.codehaus.cargo.container.Container} instance if no container object was
     * stored in the Maven Plugin Context or returns the saved instance otherwise. If a new
     * container instance is created it's also saved in the Maven Plugin Context for later
     * retrieval.
     * @throws MojoExecutionException in case of error
     */
    protected org.codehaus.cargo.container.Container createContainer()
        throws MojoExecutionException
    {
        org.codehaus.cargo.container.Container container = null;

        // Try to find the container in the Maven Plugin Context first.
        Map<Object, Object> context = getPluginContext();

        String containerKey = CONTEXT_KEY_CONTAINER;
        if (getContainerElement() != null)
        {
            containerKey += "." + getContainerElement().getType()
                + "." + getContainerElement().getHome();
        }
        if (getConfigurationElement() != null)
        {
            containerKey += "." + getConfigurationElement().getHome();
        }

        if (context != null)
        {
            container = (org.codehaus.cargo.container.Container) context.get(containerKey);
        }

        if (container == null)
        {
            container = createNewContainer();
        }
        else if (getConfigurationElement() != null)
        {
            createDefaultContainerElementIfNecessary();
            org.codehaus.cargo.container.configuration.Configuration configuration =
                createConfiguration();

            // CARGO-1053: Update the container's configuration, since different executions might
            //             have defined different configurations but the "put the container in the
            //             Maven2 context" (for handling multiple containers and also for handling
            //             embedded containers) mechanism will reuse existing container along with
            //             its configuration.
            if (container instanceof RemoteContainer)
            {
                if (!(configuration instanceof RuntimeConfiguration))
                {
                    throw new MojoExecutionException("Expected a "
                        + RuntimeConfiguration.class.getName()
                        + " but got a " + configuration.getClass().getName());
                }

                ((RemoteContainer) container).setConfiguration(
                    (RuntimeConfiguration) configuration);
            }
            else if (container instanceof LocalContainer)
            {
                if (!(configuration instanceof LocalConfiguration))
                {
                    throw new MojoExecutionException("Expected a "
                        + LocalConfiguration.class.getName()
                        + " but got a " + configuration.getClass().getName());
                }

                ((LocalContainer) container).setConfiguration(
                    (LocalConfiguration) createConfiguration());
            }
            else
            {
                throw new MojoExecutionException("Unknown container type "
                    + container.getClass().getName());
            }
        }

        if (context != null)
        {
            context.put(containerKey, container);
        }

        return container;
    }

    /**
     * Creates a brand new {@link org.codehaus.cargo.container.Container} instance. If the user has
     * not specified a container element in the POM file or if the user has not specified the
     * container id then automatically create a default container (as defined in
     * {@link #computeContainerId}) if the project calling this plugin has a WAR packaging. If the
     * packaging is different then an exception is raised.
     * 
     * @return a valid {@link org.codehaus.cargo.container.Container} instance
     * @throws MojoExecutionException in case of error or if a default container could not be
     * created
     */
    protected org.codehaus.cargo.container.Container createNewContainer()
        throws MojoExecutionException
    {
        org.codehaus.cargo.container.Container container;

        createDefaultContainerElementIfNecessary();

        if (getContainerElement().getType() == ContainerType.EMBEDDED)
        {
            loadEmbeddedContainerDependencies();
        }

        container = getContainerElement().createContainer(createConfiguration(),
            createLogger(), getCargoProject(), artifactFactory, artifactResolver, localRepository,
            repositories, settings);

        return container;
    }

    /**
     * Creates a container element if required.
     *
     * @throws MojoExecutionException in case of error or if a default container could not be
     * created
     */
    protected void createDefaultContainerElementIfNecessary() throws MojoExecutionException
    {
        if (getContainerElement() == null)
        {
            // Only accept default configuration if the packaging is not of type EAR as Cargo
            // currently doesn't have an embedded container that supports EAR (we need to add
            // openEJB support!).
            if (getCargoProject().getPackaging() != null
                && !getCargoProject().getPackaging().equalsIgnoreCase("war"))
            {
                throw new MojoExecutionException("For all packaging other than war you need to "
                    + "configure the container you wishes to use.");
            }

            Container containerElement = new Container();
            setContainerElement(containerElement);
        }

        // If no container id is specified, default to Jetty
        if (getContainerElement().getContainerId() == null)
        {
            getContainerElement().setContainerId("jetty6x");
            getContainerElement().setType(ContainerType.EMBEDDED);

            getLog().info("No container defined, using a default ["
                + getContainerElement().getContainerId() + ", "
                + getContainerElement().getType().getType() + "] container");
        }
    }

    /**
     * Loads an embedded container.
     * @throws MojoExecutionException If dependency resolve failed.
     */
    protected void loadEmbeddedContainerDependencies() throws MojoExecutionException
    {
        if (getContainerElement().getContainerId().startsWith("jetty"))
        {
            JettyArtifactResolver resolver = new JettyArtifactResolver(this.artifactResolver,
                this.localRepository, this.repositories, this.artifactFactory);
            ClassLoader classLoader = resolver.resolveDependencies(
                getContainerElement().getContainerId(), getCargoProject().getEmbeddedClassLoader());
            getCargoProject().setEmbeddedClassLoader(classLoader);
        }
    }

    /**
     * Create the autodeploy deployable (if the current project is a Java EE deployable)
     * @param container Container.
     * @return The autodeploy deployable.
     * @throws MojoExecutionException If deployable creation fails.
     */
    protected org.codehaus.cargo.container.deployable.Deployable createAutoDeployDeployable(
        org.codehaus.cargo.container.Container container) throws MojoExecutionException
    {
        Deployable deployableElement = new Deployable();
        return deployableElement.createDeployable(container.getId(), getCargoProject());
    }

    /**
     * Checks if the autodeployable is part of deployables.
     * @param deployableElements Deployable elements.
     * @return <code>true</code> if autodeployable is in <code>deployableElements</code>,
     * <code>false</code> otherwise.
     */
    protected boolean containsAutoDeployable(Deployable[] deployableElements)
    {
        boolean found = false;

        for (Deployable deployableElement : deployableElements)
        {
            if (deployableElement.getGroupId().equals(getCargoProject().getGroupId())
                && deployableElement.getArtifactId().equals(getCargoProject().getArtifactId()))
            {
                found = true;
                break;
            }
        }

        return found;
    }

    /**
     * Create a logger. If a <code>&lt;log&gt;</code> configuration element has been specified by
     * the user then use it. If none is specified then log to the Maven 2 logging subsystem.
     * 
     * @return the logger to use for logging this plugin's activity
     */
    protected Logger createLogger()
    {
        Logger logger;
        if (getContainerElement() != null && getContainerElement().getLog() != null)
        {
            // Ensure that the directories where the log will go are created
            getContainerElement().getLog().getParentFile().mkdirs();

            logger = new FileLogger(getContainerElement().getLog(), true);
        }
        else
        {
            logger = new MavenLogger(getLog());
        }

        if (getContainerElement() != null && getContainerElement().getLogLevel() != null)
        {
            logger.setLevel(getContainerElement().getLogLevel());
        }
        else
        {
            if (getLog().isDebugEnabled())
            {
                logger.setLevel(LogLevel.DEBUG);
            }
            else if (getLog().isInfoEnabled())
            {
                logger.setLevel(LogLevel.INFO);
            }
            else
            {
                logger.setLevel(LogLevel.WARN);
            }
        }

        return logger;
    }
}

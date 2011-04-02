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
package org.codehaus.cargo.documentation;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.RuntimeConfiguration;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.geronimo.GeronimoPropertySet;
import org.codehaus.cargo.container.glassfish.GlassFishPropertySet;
import org.codehaus.cargo.container.jboss.JBossPropertySet;
import org.codehaus.cargo.container.jetty.JettyPropertySet;
import org.codehaus.cargo.container.jonas.JonasPropertySet;
import org.codehaus.cargo.container.jrun.JRun4xPropertySet;
import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.property.ResourcePropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.tomcat.TomcatPropertySet;
import org.codehaus.cargo.container.weblogic.WebLogicPropertySet;
import org.codehaus.cargo.generic.ContainerCapabilityFactory;
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.DefaultContainerCapabilityFactory;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationCapabilityFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationCapabilityFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.codehaus.cargo.generic.deployer.DefaultDeployerFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;

/**
 * Generate container documentation using Confluence markup language. The generated text is meant to
 * be copied on the Cargo Confluence web site.
 * 
 * @version $Id: ConfluenceContainerDocumentationGenerator.java 2615 2011-01-16 16:44:06Z alitokmen
 * $
 */
public class ConfluenceContainerDocumentationGenerator
{
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static final List<String> JAVA4_CONTAINERS = Arrays.asList(new String[]
        {
            "geronimo1x",
            "jboss3x",
            "jboss4x",
            "jetty4x",
            "jetty5x",
            "jo1x",
            "jonas4x",
            "oc4j9x",
            "resin2x",
            "tomcat4x",
            "tomcat5x",
            "weblogic8x"
        });

    private static final List<String> JAVA5_CONTAINERS = Arrays.asList(new String[]
        {
            "geronimo2x",
            "glassfish2x",
            "jboss42x",
            "jboss5x",
            "jboss51x",
            "jetty6x",
            "jetty7x",
            "jonas5x",
            "jrun4x",
            "oc4j10x",
            "resin3x",
            "tomcat6x",
            "weblogic9x",
            "weblogic10x"
        });

    private ContainerFactory containerFactory = new DefaultContainerFactory();
    private ConfigurationFactory configurationFactory = new DefaultConfigurationFactory();
    private DeployerFactory deployerFactory = new DefaultDeployerFactory();
    private ContainerCapabilityFactory containerCapabilityFactory =
        new DefaultContainerCapabilityFactory();
    private ConfigurationCapabilityFactory configurationCapabilityFactory =
        new DefaultConfigurationCapabilityFactory();

    public String generateDocumentation(String containerId) throws Exception
    {
        StringBuilder output = new StringBuilder();

        output.append("{note}This page has been automatically generated by Cargo's build. "
            + "Do not edit it directly as it'll be overwritten next time it's generated again."
            + "{note}");
        output.append(LINE_SEPARATOR);
        output.append(LINE_SEPARATOR);

        if (containerId.equals("geronimo1x"))
        {
            output.append("{note}The Geronimo 1.x CARGO container REQUIRES Geronimo 1.1.1.{note}");
            output.append(LINE_SEPARATOR);
            output.append(LINE_SEPARATOR);
        }

        output.append(generateContainerFeaturesText(containerId));
        output.append(LINE_SEPARATOR);
        output.append(generateConfigurationFeaturesText(containerId));
        output.append(LINE_SEPARATOR);
        output.append(generateDeployerFeaturesText(containerId));
        output.append(LINE_SEPARATOR);
        output.append(generateOtherFeaturesText(containerId));
        output.append(LINE_SEPARATOR);
        output.append(generateConfigurationPropertiesText(containerId));
        output.append(LINE_SEPARATOR);

        return output.toString();
    }

    protected String generateContainerFeaturesText(String containerId)
    {
        StringBuilder output = new StringBuilder();

        output.append("h3.Container Features");
        output.append(LINE_SEPARATOR);
        output.append(LINE_SEPARATOR);
        output.append("|| Feature name || Java || Ant || Maven2 || Comment ||");
        output.append(LINE_SEPARATOR);

        output.append("| [Container Instantiation]               | ");
        output.append("(/) {{ContainerFactory.createContainer(\"" + containerId + "\"...)}} | ");
        output.append("(/) {{<cargo containerId=\"" + containerId + "\".../>}} |");
        output.append("(/) {{<containerId>" + containerId + "</containerId>}} | |");
        output.append(LINE_SEPARATOR);

        if (this.containerFactory.isContainerRegistered(containerId, ContainerType.INSTALLED)
            || this.containerFactory.isContainerRegistered(containerId, ContainerType.EMBEDDED))
        {
            output.append("| [Local Container]                       | (/) | (/) | (/) | |");
            output.append(LINE_SEPARATOR);
            if (containerId.startsWith("geronimo"))
            {
                output
                    .append("| &nbsp; [Container Classpath]            | (x) | (x) | (x) "
                        + "| Changing the the container classpath is not supported on Apache Geronimo |");
            }
            else if (containerId.equals("glassfish2x"))
            {
                output.append("| &nbsp; [Container Classpath]            | (x) | (x) | (x) "
                    + "| Changing the the container classpath is not supported on GlassFish 2.x |");
            }
            else if (containerId.equals("glassfish3x") || containerId.equals("jonas5x"))
            {
                output
                    .append("| &nbsp; [Container Classpath]            | (x) | (x) | (x) "
                        + "| OSGi application servers do not support changing the container classpath |");
            }
            else
            {
                output
                    .append("| &nbsp; [Container Classpath]            | (/) | (/) | (/) | |");
            }
            output.append(LINE_SEPARATOR);
            output.append("| &nbsp; [Container Start]                | (/) | (/) | (/) | |");
            output.append(LINE_SEPARATOR);
            output.append("| &nbsp; [Container Stop]                 | (/) | (/) | (/) | |");
            output.append(LINE_SEPARATOR);
            output.append("| &nbsp; [Container Timeout]              | (/) | (/) | (/) | |");
            output.append(LINE_SEPARATOR);

            if (this.containerFactory.isContainerRegistered(containerId, ContainerType.EMBEDDED))
            {
                output.append("| &nbsp; [Embedded Container]             | ");
                output.append("(/) {{" + computedFQCN(this.containerFactory.getContainerClass(
                    containerId, ContainerType.EMBEDDED).getName()) + "}} | (/) | (/) | |");
            }
            else
            {
                output.append(
                    "| &nbsp; [Embedded Container]             | (x) | (x) | (x) | |");
            }
            output.append(LINE_SEPARATOR);

            if (this.containerFactory.isContainerRegistered(containerId, ContainerType.INSTALLED))
            {
                output.append("| &nbsp; [Installed Container]            | ");
                output.append("(/) {{" + computedFQCN(this.containerFactory.getContainerClass(
                    containerId, ContainerType.INSTALLED).getName()) + "}} | (/) | (/) | |");
                output.append(LINE_SEPARATOR);
                output.append(
                    "| &nbsp;&nbsp; [Passing system properties]| (/) | (/) | (/) | |");
                output.append(LINE_SEPARATOR);
                output.append(
                    "| &nbsp;&nbsp; [Installer]                | (/) | (/) | (/) | |");
            }
            else
            {
                output.append(
                    "| &nbsp; [Installed Container]            | (x) | (x) | (x) | |");
                output.append(LINE_SEPARATOR);
                output.append(
                    "| &nbsp;&nbsp; [Passing system properties]| (x) | (x) | (x) | |");
                output.append(LINE_SEPARATOR);
                output.append(
                    "| &nbsp;&nbsp; [Installer]                | (x) | (x) | (x) | |");
            }
            output.append(LINE_SEPARATOR);
        }
        else
        {
            output.append("| [Local Container]                       | (x) | (x) | (x) | |");
            output.append(LINE_SEPARATOR);
            output.append("| &nbsp; [Container Classpath]            | (x) | (x) | (x) | |");
            output.append(LINE_SEPARATOR);
            output.append("| &nbsp; [Container Start]                | (x) | (x) | (x) | |");
            output.append(LINE_SEPARATOR);
            output.append("| &nbsp; [Container Stop]                 | (x) | (x) | (x) | |");
            output.append(LINE_SEPARATOR);
            output.append("| &nbsp; [Container Timeout]              | (x) | (x) | (x) | |");
            output.append(LINE_SEPARATOR);
            output.append("| &nbsp; [Embedded Container]             | (x) | (x) | (x) | |");
            output.append(LINE_SEPARATOR);
            output.append("| &nbsp; [Installed Container]            | (x) | (x) | (x) | |");
            output.append(LINE_SEPARATOR);
            output.append("| &nbsp;&nbsp; [Passing system properties]| (x) | (x) | (x) | |");
            output.append(LINE_SEPARATOR);
            output.append("| &nbsp;&nbsp; [Installer]                | (x) | (x) | (x) | |");
            output.append(LINE_SEPARATOR);
        }

        if (this.containerFactory.isContainerRegistered(containerId, ContainerType.REMOTE))
        {
            output.append("| [Remote Container]                      | ");
            output.append("(/) {{" + computedFQCN(this.containerFactory.getContainerClass(
                containerId, ContainerType.REMOTE).getName()) + "}} | (/) | (/) | |");
        }
        else
        {
            output.append("| [Remote Container]                      | (x) | (x) | (x) | |");
        }
        output.append(LINE_SEPARATOR);

        return output.toString();
    }

    protected String generateStandaloneConfigurationText(String containerId,
        ContainerType type)
    {
        StringBuilder output = new StringBuilder();

        if (this.configurationFactory.isConfigurationRegistered(containerId, type,
            ConfigurationType.STANDALONE))
        {
            output.append("| [Standalone Local Configuration for " + type.getType()
                + " container|Standalone Local Configuration]        | ");
            output.append("(/) {{" + computedFQCN(this.configurationFactory.getConfigurationClass(
                containerId, type, ConfigurationType.STANDALONE).getName())
                + "}} | (/) | (/) | |");
        }
        else
        {
            output.append("| [Standalone Local Configuration for " + type.getType()
                + " container|Standalone Local Configuration]        | (x) | (x) | (x) | |");
        }
        return output.toString();
    }

    protected String generateExistingConfigurationText(String containerId,
        ContainerType type)
    {
        StringBuilder output = new StringBuilder();

        if (this.configurationFactory.isConfigurationRegistered(containerId, type,
            ConfigurationType.EXISTING))
        {
            output.append("| [Existing Local Configuration for " + type.getType()
                + " container|Existing Local Configuration]          | ");
            output.append("(/) {{" + computedFQCN(this.configurationFactory.getConfigurationClass(
                containerId, type, ConfigurationType.EXISTING).getName())
                + "}} | (/) | (/) | |");
        }
        else
        {
            output.append("| [Existing Local Configuration for " + type.getType()
                + " container|Existing Local Configuration]          | (x) | (x) | (x) | |");
        }
        return output.toString();
    }

    protected String generateConfigurationFeaturesText(String containerId)
    {
        StringBuilder output = new StringBuilder();

        output.append("h3.Configuration Features");
        output.append(LINE_SEPARATOR);
        output.append(LINE_SEPARATOR);
        output.append("|| Feature name || Java || Ant || Maven2 || Comment ||");
        output.append(LINE_SEPARATOR);

        output.append(generateStandaloneConfigurationText(containerId, ContainerType.INSTALLED));
        output.append(LINE_SEPARATOR);
        output.append(generateStandaloneConfigurationText(containerId, ContainerType.EMBEDDED));
        output.append(LINE_SEPARATOR);

        output.append(generateExistingConfigurationText(containerId, ContainerType.INSTALLED));
        output.append(LINE_SEPARATOR);
        output.append(generateExistingConfigurationText(containerId, ContainerType.EMBEDDED));
        output.append(LINE_SEPARATOR);

        if (this.configurationFactory.isConfigurationRegistered(containerId, ContainerType.REMOTE,
            ConfigurationType.RUNTIME))
        {
            output.append("| [Runtime Configuration]                 | ");
            output.append("(/) {{" + computedFQCN(this.configurationFactory.getConfigurationClass(
                containerId, ContainerType.REMOTE, ConfigurationType.RUNTIME).getName())
                + "}} | (/) | (/) | |");
        }
        else
        {
            output.append("| [Runtime Configuration]                 | (x) | (x) | (x) | |");
        }
        output.append(LINE_SEPARATOR);

        if (this.containerCapabilityFactory.createContainerCapability(
            containerId).supportsDeployableType(DeployableType.WAR))
        {
            output.append("| [Static deployment of WAR]              | (/) | (/) | (/) | ");
            if (containerId.equals("tomcat4x"))
            {
                output.append("Does not support {{META-INF/context.xml}} files yet ");
            }
            output.append("|");
            output.append(LINE_SEPARATOR);

            // TODO: Need to introduce expanded WAR as a proper deployable type
            if (containerId.startsWith("geronimo"))
            {
                output.append(
                    "| [Static deployment of expanded WAR]     | (x) | (x) | (x) | "
                        + "The Apache Geronimo container does not support expanded WARs |");
            }
            else
            {
                output.append(
                    "| [Static deployment of expanded WAR]     | (/) | (/) | (/) | |");
            }
        }
        else
        {
            output.append("| [Static deployment of WAR]              | (x) | (x) | (x) | |");
        }
        output.append(LINE_SEPARATOR);

        if (this.containerCapabilityFactory.createContainerCapability(
            containerId).supportsDeployableType(DeployableType.EJB))
        {
            output.append("| [Static deployment of EJB]              | (/) | (/) | (/) | |");
        }
        else
        {
            output.append("| [Static deployment of EJB]              | (x) | (x) | (x) | |");
        }
        output.append(LINE_SEPARATOR);

        if (this.containerCapabilityFactory.createContainerCapability(
            containerId).supportsDeployableType(DeployableType.EAR))
        {
            output.append("| [Static deployment of EAR]              | (/) | (/) | (/) | |");
        }
        else
        {
            output.append("| [Static deployment of EAR]              | (x) | (x) | (x) | |");
        }
        output.append(LINE_SEPARATOR);

        if (this.containerCapabilityFactory.createContainerCapability(
            containerId).supportsDeployableType(DeployableType.RAR))
        {
            output.append("| [Static deployment of RAR]              | (/) | (/) | (/) | |");
        }
        else
        {
            output.append("| [Static deployment of RAR]              | (x) | (x) | (x) | |");
        }
        output.append(LINE_SEPARATOR);

        if (this.containerCapabilityFactory.createContainerCapability(
            containerId).supportsDeployableType(DeployableType.HAR))
        {
            output.append("| [Static deployment of HAR]              | (/) | (/) | (/) | |");
        }
        else
        {
            output.append("| [Static deployment of HAR]              | (x) | (x) | (x) | |");
        }
        output.append(LINE_SEPARATOR);

        if (this.containerCapabilityFactory.createContainerCapability(
            containerId).supportsDeployableType(DeployableType.SAR))
        {
            output.append("| [Static deployment of SAR]              | (/) | (/) | (/) | |");
        }
        else
        {
            output.append("| [Static deployment of SAR]              | (x) | (x) | (x) | |");
        }
        output.append(LINE_SEPARATOR);

        if (this.containerCapabilityFactory.createContainerCapability(
            containerId).supportsDeployableType(DeployableType.AOP))
        {
            output.append("| [Static deployment of (JBoss) AOP]      | (/) | (/) | (/) | |");
        }
        else
        {
            output.append("| [Static deployment of (JBoss) AOP]      | (x) | (x) | (x) | |");
        }
        output.append(LINE_SEPARATOR);

        if (this.containerCapabilityFactory.createContainerCapability(
            containerId).supportsDeployableType(DeployableType.FILE))
        {
            output.append(
                "| [Static deployment of files]              | (/) | (/) | (/) | |");
        }
        else
        {
            output.append(
                "| [Static deployment of files]              | (x) | (x) | (x) | |");
        }
        output.append(LINE_SEPARATOR);

        if (this.containerCapabilityFactory.createContainerCapability(
            containerId).supportsDeployableType(DeployableType.BUNDLE))
        {
            output.append(
                "| [Static deployment of OSGi Bundles]              | (/) | (/) | (/) | |");
        }
        else
        {
            output.append(
                "| [Static deployment of OSGi Bundles]              | (x) | (x) | (x) | |");
        }
        output.append(LINE_SEPARATOR);

        return output.toString();
    }

    protected String generateDeployerFeaturesText(String containerId)
    {
        StringBuilder output = new StringBuilder();

        output.append("h3.Deployer Features");
        output.append(LINE_SEPARATOR);
        output.append(LINE_SEPARATOR);
        output.append("|| Feature name || Java || Ant || Maven2 || Comment ||");
        output.append(LINE_SEPARATOR);

        if (this.deployerFactory.isDeployerRegistered(containerId, DeployerType.INSTALLED))
        {
            output.append("| [Installed Deployer]                    | ");
            output.append("(/) {{" + computedFQCN(this.deployerFactory.getDeployerClass(
                containerId, DeployerType.INSTALLED).getName()) + "}} | (x) | (/) | |");
        }
        else
        {
            output.append("| [Installed Deployer]                    | (x) | (x) | (x) | |");
        }
        output.append(LINE_SEPARATOR);

        if (this.deployerFactory.isDeployerRegistered(containerId, DeployerType.EMBEDDED))
        {
            output.append("| [Embedded Deployer]                     | ");
            output.append("(/) {{" + computedFQCN(this.deployerFactory.getDeployerClass(
                containerId, DeployerType.EMBEDDED).getName()) + "}} | (x) | (/) | |");
        }
        else
        {
            output.append("| [Embedded Deployer]                     | (x) | (x) | (x) | |");
        }
        output.append(LINE_SEPARATOR);

        if (this.deployerFactory.isDeployerRegistered(containerId, DeployerType.REMOTE))
        {
            output.append("| [Remote Deployer]                       | ");
            output.append("(/) {{" + computedFQCN(this.deployerFactory.getDeployerClass(
                containerId, DeployerType.REMOTE).getName()) + "}} | (x) | (/) | |");
        }
        else
        {
            output.append("| [Remote Deployer]                       | (x) | (x) | (x) | |");
        }
        output.append(LINE_SEPARATOR);

        return output.toString();
    }

    protected String generateOtherFeaturesText(String containerId)
    {
        StringBuilder output = new StringBuilder();

        output.append("h3.Other Features");
        output.append(LINE_SEPARATOR);
        output.append(LINE_SEPARATOR);
        output.append("|| Feature name || Java || Ant || Maven2 || Comment ||");
        output.append(LINE_SEPARATOR);

        output.append("| [Debugging]                             | (/) | (/) | (/) | |");
        output.append(LINE_SEPARATOR);

        return output.toString();
    }

    protected String generateConfigurationPropertiesText(String containerId)
        throws Exception
    {
        StringBuilder output = new StringBuilder();

        output.append("h3.Supported Configuration properties");
        output.append(LINE_SEPARATOR);
        output.append(LINE_SEPARATOR);
        output.append("The tables below list both the [general configuration "
            + "properties|Configuration properties] as well as the container-specific ones.");
        output.append(LINE_SEPARATOR);
        output.append(LINE_SEPARATOR);

        if (this.configurationFactory.isConfigurationRegistered(containerId,
            ContainerType.INSTALLED, ConfigurationType.STANDALONE)
                || this.configurationFactory.isConfigurationRegistered(containerId,
                    ContainerType.EMBEDDED, ConfigurationType.STANDALONE))
        {
            output.append("h4.Standalone Local Configuration Properties");
            output.append(LINE_SEPARATOR);
            output.append(LINE_SEPARATOR);
            if (this.configurationFactory.isConfigurationRegistered(containerId,
                ContainerType.INSTALLED, ConfigurationType.STANDALONE))
            {
                output.append(generateConfigurationPropertiesForConfigurationTypeForContainerType(
                    "Standalone Local", ConfigurationType.STANDALONE, containerId,
                    ContainerType.INSTALLED));
                output.append(LINE_SEPARATOR);
            }
            if (this.configurationFactory.isConfigurationRegistered(containerId,
                ContainerType.EMBEDDED, ConfigurationType.STANDALONE))
            {
                output.append(generateConfigurationPropertiesForConfigurationTypeForContainerType(
                    "Standalone Local", ConfigurationType.STANDALONE, containerId,
                    ContainerType.EMBEDDED));
                output.append(LINE_SEPARATOR);
            }

            if (containerId.startsWith("jboss"))
            {
                output.append(LINE_SEPARATOR);
                output.append(LINE_SEPARATOR);
                output.append("{info}A side note on the JBoss ports:");
                output.append(LINE_SEPARATOR);
                output.append("* The {{cargo.rmi.port}} corresponds to the {{Port}} parameter of");
                output.append(" the {{jboss:service=Naming}} bean.");
                output.append(LINE_SEPARATOR);
                output.append("* The {{cargo.jboss.naming.port}} corresponds to the {{RmiPort}}");
                output.append(" parameter of the {{jboss:service=Naming}} bean.");
                output.append(LINE_SEPARATOR);
                output.append("{info}");
                output.append(LINE_SEPARATOR);
            }
        }

        if (this.configurationFactory.isConfigurationRegistered(containerId,
            ContainerType.INSTALLED, ConfigurationType.EXISTING)
                || this.configurationFactory.isConfigurationRegistered(containerId,
                    ContainerType.EMBEDDED, ConfigurationType.EXISTING))
        {
            output.append("h4.Existing Local Configuration Properties");
            output.append(LINE_SEPARATOR);
            output.append(LINE_SEPARATOR);
            if (this.configurationFactory.isConfigurationRegistered(containerId,
                ContainerType.INSTALLED, ConfigurationType.EXISTING))
            {
                output.append(generateConfigurationPropertiesForConfigurationTypeForContainerType(
                    "Existing Local", ConfigurationType.EXISTING, containerId,
                    ContainerType.INSTALLED));
                output.append(LINE_SEPARATOR);
            }
            if (this.configurationFactory.isConfigurationRegistered(containerId,
                ContainerType.EMBEDDED, ConfigurationType.EXISTING))
            {
                output.append(generateConfigurationPropertiesForConfigurationTypeForContainerType(
                    "Existing Local", ConfigurationType.EXISTING, containerId,
                    ContainerType.EMBEDDED));
                output.append(LINE_SEPARATOR);
            }
        }

        if (this.configurationFactory.isConfigurationRegistered(containerId,
            ContainerType.REMOTE, ConfigurationType.RUNTIME))
        {
            output.append("h4.Runtime Configuration Properties");
            if (containerId.startsWith("jboss"))
            {
                output.append(LINE_SEPARATOR);
                output.append(LINE_SEPARATOR);
                output.append("{info}Before using the JBoss remote deployer, ");
                output.append("please read: [JBoss Remote Deployer]{info}");
            }
            if (containerId.startsWith("jetty"))
            {
                output.append(LINE_SEPARATOR);
                output.append(LINE_SEPARATOR);
                output.append("{info}Before using the Jetty remote deployer, ");
                output.append("please read: [Jetty Remote Deployer]{info}");
            }
            else if (containerId.equals("glassfish3x"))
            {
                output.append(LINE_SEPARATOR);
                output.append(LINE_SEPARATOR);
                output.append("{info}Before using the GlassFish remote deployer, ");
                output.append("please read: [JSR88]{info}");
            }
            output.append(LINE_SEPARATOR);
            output.append(LINE_SEPARATOR);
            output.append(generateConfigurationPropertiesForConfigurationTypeForContainerType(
                "Runtime", ConfigurationType.RUNTIME, containerId, ContainerType.REMOTE));
            output.append(LINE_SEPARATOR);
        }

        return output.toString();
    }

    protected String generateConfigurationPropertiesForConfigurationTypeForContainerType(
        String typeAsName, ConfigurationType type, String containerId, ContainerType containerType)
        throws Exception
    {
        StringBuilder output = new StringBuilder();

        output.append("h5. For " + containerType + " container " + computedFQCN(
            this.containerFactory.getContainerClass(containerId, containerType).getName()));
        output.append(LINE_SEPARATOR);
        output.append(LINE_SEPARATOR);

        output.append(
            "|| Property name || Java Property || Supported? || Default value || Comment ||");
        output.append(LINE_SEPARATOR);

        Class configurationClass = Class.forName(
            this.configurationFactory.getConfigurationClass(containerId, containerType,
                type).getName());

        Configuration slc;
        if (type != ConfigurationType.RUNTIME)
        {
            slc = (LocalConfiguration) configurationClass.getConstructor(
                new Class[] {String.class}).newInstance(new Object[] {"whatever"});
        }
        else
        {
            slc = (RuntimeConfiguration) configurationClass.newInstance();
        }

        Map properties = this.configurationCapabilityFactory.createConfigurationCapability(
            containerId, containerType, type).getProperties();
        Iterator keys = properties.keySet().iterator();
        while (keys.hasNext())
        {
            String property = (String) keys.next();
            output.append("| [" + property + "|Configuration properties] | ");
            output.append(
                "[" + findPropertySetFieldName(property) + "|Configuration properties] | ");
            boolean supported = ((Boolean) properties.get(property)).booleanValue();
            output.append(supported ? "(/)" : "(x)");
            if (GeneralPropertySet.JAVA_HOME.equals(property))
            {
                String javaVersion;
                if (JAVA4_CONTAINERS.contains(containerId))
                {
                    javaVersion = "1.4";
                }
                else if (JAVA5_CONTAINERS.contains(containerId))
                {
                    javaVersion = "1.5";
                }
                else
                {
                    javaVersion = "1.6";
                }

                output.append(" | {_}JAVA_HOME version " + javaVersion + " or newer{_} | |");
            }
            else
            {
                output.append(" | " + (slc.getPropertyValue(property) == null ? "N/A"
                    : slc.getPropertyValue(property)) + " | |");
            }
            output.append(LINE_SEPARATOR);
        }

        return output.toString();
    }

    protected String computedFQCN(String className)
    {
        return "o.c.c.c" + className.substring(
            className.substring(0, className.lastIndexOf(".")).lastIndexOf("."));
    }

    protected String findPropertySetFieldName(String propertyValue) throws Exception
    {
        String result = null;
        Class[] propertySetClasses = {
            GeneralPropertySet.class,
            ServletPropertySet.class,
            RemotePropertySet.class,
            TomcatPropertySet.class,
            GeronimoPropertySet.class,
            WebLogicPropertySet.class,
            JBossPropertySet.class,
            JettyPropertySet.class,
            JonasPropertySet.class,
            GlassFishPropertySet.class,
            JRun4xPropertySet.class,
            DatasourcePropertySet.class,
            ResourcePropertySet.class
        };

        for (Class propertySetClasse : propertySetClasses)
        {
            result = findPropertySetFieldName(propertyValue, propertySetClasse);
            if (result != null)
            {
                break;
            }
        }

        return result;
    }

    protected String findPropertySetFieldName(String propertyValue, Class propertySetClass)
        throws Exception
    {
        String result = null;

        Field[] fields = propertySetClass.getFields();
        for (Field field : fields)
        {
            String value = (String) field.get(null);
            if (value.equals(propertyValue))
            {
                result = propertySetClass.getName().substring(
                    propertySetClass.getName().lastIndexOf(".") + 1) + "."
                        + field.getName();
                break;
            }
        }

        return result;
    }
}

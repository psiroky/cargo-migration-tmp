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
package org.codehaus.cargo.maven2.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.cargo.container.EmbeddedLocalContainer;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.installer.ZipURLInstaller;
import org.codehaus.cargo.container.stub.EmbeddedLocalContainerStub;
import org.codehaus.cargo.container.stub.InstalledLocalContainerStub;
import org.codehaus.cargo.container.stub.StandaloneLocalConfigurationStub;
import org.codehaus.cargo.maven2.util.CargoProject;
import org.codehaus.cargo.util.log.NullLogger;
import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

/**
 * Unit tests for the {@link org.codehaus.cargo.maven2.configuration.Container} class.
 * 
 * @version $Id$
 */
public class ContainerTest extends MockObjectTestCase
{
    public void testCreateEmbeddedContainerWithSystemPropertiesSet() throws Exception
    {
        org.codehaus.cargo.maven2.configuration.Container containerElement = setUpContainerElement(new EmbeddedLocalContainerStub());

        Map props = new HashMap();
        props.put("id1", "value1");
        props.put("id2", "value2");

        containerElement.setSystemProperties(props);

        containerElement.createContainer(
            new StandaloneLocalConfigurationStub("configuration/home"),
            new NullLogger(), createTestCargoProject("whatever"));

        // For embedded containers, system properties get put into our own vm
        for (Iterator it = props.entrySet().iterator(); it.hasNext();)
        {
            Map.Entry entry = (Map.Entry) it.next();
            assertEquals((String) entry.getValue(), System.getProperty((String) entry.getKey()));
        }
    }

    public void testCreateInstalledLocalContainerWithSystemPropertiesSet() throws Exception
    {
        org.codehaus.cargo.maven2.configuration.Container containerElement = setUpContainerElement(new InstalledLocalContainerStub());

        Map props = new HashMap();
        props.put("id1", "value1");
        props.put("id2", "value2");

        containerElement.setSystemProperties(props);

        org.codehaus.cargo.container.InstalledLocalContainer container =
            (InstalledLocalContainer) containerElement.createContainer(
                new StandaloneLocalConfigurationStub("configuration/home"), new NullLogger(),
                createTestCargoProject("whatever"));
        assertEquals(props, container.getSystemProperties());
    }

    public void testCreateEmbeddedContainerWithExtraClasspathDependency() throws Exception
    {
        // 1) Create a JAR file acting as an extra dependency
        String resourceValue = "file in zip in dependency";
        String resourceName = "maven-test-my-dependency.txt";

        File zipFile = File.createTempFile("maven2-plugin-test-dependency", ".zip");
        zipFile.deleteOnExit();

        ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFile));
        zip.putNextEntry(new ZipEntry(resourceName));
        zip.write(resourceValue.getBytes("UTF-8"));
        zip.close();

        // 2) Create a Maven2 Artifact linked to the JAR file just created
        DefaultArtifact artifact = new DefaultArtifact("customGroupId", "customArtifactId",
            VersionRange.createFromVersion("0.1"), "compile", "jar", null,
            new DefaultArtifactHandler());
        artifact.setFile(zipFile);
        Set artifacts = new HashSet();
        artifacts.add(artifact);

        // 3) Set up the container element and add the extra Dependency to it
        Dependency dependencyElement = new Dependency();
        dependencyElement.setGroupId("customGroupId");
        dependencyElement.setArtifactId("customArtifactId");
        dependencyElement.setType("jar");

        org.codehaus.cargo.maven2.configuration.Container containerElement = setUpContainerElement(new EmbeddedLocalContainerStub());
        containerElement.setDependencies(new Dependency[] {dependencyElement});

        org.codehaus.cargo.container.EmbeddedLocalContainer container =
            (EmbeddedLocalContainer) containerElement.createContainer(
                new StandaloneLocalConfigurationStub("configuration/home"), new NullLogger(),
                createTestCargoProject("whatever", artifacts));

        // 4) Verify that we can load data from our JAR file when using the classloader from the
        // container and when using the context class loader as we set it to be the embedded
        // container classloader.
        assertEquals(resourceValue, getResource(container.getClassLoader(), resourceName));
        assertEquals(resourceValue, getResource(Thread.currentThread().getContextClassLoader(),
            resourceName));
    }

    public void testCreateEmbeddedContainerWithExtraClasspathLocation() throws Exception
    {
        // Create a jar file
        String resourceValue = "file in extra classpath";
        File resourceFile = File.createTempFile("maven2-plugin-test-embedded-extra", ".txt");
        resourceFile.deleteOnExit();
        String resourceName = resourceFile.getName();
        String resourceLocation = resourceFile.getParent();

        FileOutputStream os = new FileOutputStream(resourceFile);
        os.write(resourceValue.getBytes("UTF-8"));
        os.close();

        Dependency dependencyElement = new Dependency();
        dependencyElement.setLocation(resourceLocation);

        org.codehaus.cargo.maven2.configuration.Container containerElement = setUpContainerElement(new EmbeddedLocalContainerStub());
        containerElement.setDependencies(new Dependency[] {dependencyElement});

        org.codehaus.cargo.container.EmbeddedLocalContainer container =
            (EmbeddedLocalContainer) containerElement.createContainer(
                new StandaloneLocalConfigurationStub("configuration/home"), new NullLogger(),
                createTestCargoProject("whatever"));

        assertEquals(resourceValue, getResource(container.getClassLoader(), resourceName));
        assertEquals(resourceValue, getResource(Thread.currentThread().getContextClassLoader(),
            resourceName));
    }

    public void testCreateInstalledLocalContainerWithInstallerAndHome() throws Exception
    {
        org.codehaus.cargo.maven2.configuration.Container containerElement = setUpContainerElement(new InstalledLocalContainerStub());
        final String containerHome = "container/overriding_home";
        containerElement.setHome(containerHome);
        final Mock mockInstaller = mock(ZipURLInstaller.class, new Class[] {URL.class},
            new Object[] {new URL("http://whatever")});
        mockInstaller.expects(once()).method("install"); // install method should be called
        mockInstaller.stubs().method("getHome").will(returnValue("container/incorrect_home")); // home
                                                                                               // provided
                                                                                               // by
                                                                                               // installer
                                                                                               // should
                                                                                               // not
                                                                                               // be
                                                                                               // used
        containerElement
            .setZipUrlInstaller(new org.codehaus.cargo.maven2.configuration.ZipUrlInstaller()
            {
                @Override
                public ZipURLInstaller createInstaller()
               {
                   return (ZipURLInstaller) mockInstaller.proxy();
               }
            });

        org.codehaus.cargo.container.InstalledLocalContainer container =
            (InstalledLocalContainer) containerElement.createContainer(
                new StandaloneLocalConfigurationStub("configuration/home"), new NullLogger(),
                createTestCargoProject("whatever"));
        assertEquals("Specified home didn't override home defined by installer", containerHome,
            container.getHome());
    }

    public void testCreateInstalledLocalContainerWithHome() throws Exception
    {
        org.codehaus.cargo.maven2.configuration.Container containerElement = setUpContainerElement(new InstalledLocalContainerStub());
        final String containerHome = "container/home";
        containerElement.setHome(containerHome);

        org.codehaus.cargo.container.InstalledLocalContainer container =
            (InstalledLocalContainer) containerElement.createContainer(
                new StandaloneLocalConfigurationStub("configuration/home"), new NullLogger(),
                createTestCargoProject("whatever"));
        assertEquals("Specified home not used", containerHome, container.getHome());
    }

    public void testCreateInstalledLocalContainerWithInstaller() throws Exception
    {
        org.codehaus.cargo.maven2.configuration.Container containerElement = setUpContainerElement(new InstalledLocalContainerStub());
        containerElement.setHome(null);
        final Mock mockInstaller = mock(ZipURLInstaller.class, new Class[] {URL.class},
            new Object[] {new URL("http://whatever")});
        mockInstaller.expects(once()).method("install"); // install method should be called
        final String containerHome = "container/installer_home";
        mockInstaller.stubs().method("getHome").will(returnValue(containerHome));
        containerElement
            .setZipUrlInstaller(new org.codehaus.cargo.maven2.configuration.ZipUrlInstaller()
            {
                @Override
                public ZipURLInstaller createInstaller()
               {
                   return (ZipURLInstaller) mockInstaller.proxy();
               }
            });

        org.codehaus.cargo.container.InstalledLocalContainer container =
            (InstalledLocalContainer) containerElement.createContainer(
                new StandaloneLocalConfigurationStub("configuration/home"), new NullLogger(),
                createTestCargoProject("whatever"));
        assertEquals("Home specified by installer not used", containerHome, container.getHome());
    }

    protected org.codehaus.cargo.maven2.configuration.Container setUpContainerElement(
        org.codehaus.cargo.container.Container container)
    {
        org.codehaus.cargo.maven2.configuration.Container containerElement = new org.codehaus.cargo.maven2.configuration.Container();
        containerElement.setContainerId(container.getId());
        containerElement.setImplementation(container.getClass().getName());
        containerElement.setHome("container/home");
        containerElement.setType(container.getType());

        return containerElement;
    }

    /**
     * Provide a test CargoProject in lieu of the one that is normally generated from the
     * MavenProject at runtime.
     */
    protected CargoProject createTestCargoProject(String packaging, Set artifacts)
    {
        Mock mockLog = mock(Log.class);
        mockLog.stubs().method("debug");

        return new CargoProject(packaging, "projectGroupId", "projectArtifactId",
            "target", "projectFinalName", artifacts, (Log) mockLog.proxy());
    }

    protected CargoProject createTestCargoProject(String packaging)
    {
        return createTestCargoProject(packaging, new HashSet());
    }

    /**
     * Get the first line of a resource from a specific classloader.
     */
    public String getResource(ClassLoader classLoader, String resourceName) throws IOException
    {
        InputStreamReader reader =
            new InputStreamReader(classLoader.getResourceAsStream(resourceName), "UTF-8");
        return new BufferedReader(reader).readLine();
    }

    /*
     * private class ZipURLInstallerStub extends ZipUrlInstaller { public boolean
     * installMethodCalled = false;
     * 
     * public ZipURLInstallerStub(URL remoteLocation) { super(remoteLocation); }
     * 
     * @Override public void install() { installMethodCalled = true; } }
     */
}

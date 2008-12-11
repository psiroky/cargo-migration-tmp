/* 
 * ========================================================================
 * 
 * Copyright 2007-2008 OW2.
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
package org.codehaus.cargo.container.weblogic;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;
import org.custommonkey.xmlunit.XMLAssert;

/**
 * Unit tests for {@link WebLogicStandaloneLocalConfiguration}.
 */
public class WebLogic8xStandaloneLocalConfigurationTest extends TestCase
{
    private static final String BEA_HOME = "ram:/bea";

    private static final String DOMAIN_HOME = BEA_HOME + "/mydomain";

    private static final String WL_HOME = BEA_HOME + "/weblogic8";

    private static final String HOSTNAME = "127.0.0.1";

    private static final String PORT = "8001";
    
    private static final String DS_JNDI = "jdbc/CrowdDS";

    private static final String DS_TYPE_NONTX = "javax.sql.DataSource";

    private static final String DS_PASSWORD = "";

    private static final String DS_USER = "sa";

    private static final String DS_DRIVER = "org.hsqldb.jdbcDriver";

    private static final String DS_URL = "jdbc:hsqldb:mem:crowd_cargo";
    
    private String dataSourceProperty;
    
    private WebLogic8xInstalledLocalContainer container;

    private WebLogicStandaloneLocalConfiguration configuration;

    private StandardFileSystemManager fsManager;

    private FileHandler fileHandler;

    /**
     * {@inheritDoc}
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        StringBuffer dataSource = new StringBuffer();
        dataSource.append("cargo.datasource.url="+DS_URL+"|\n");
        dataSource.append("cargo.datasource.driver="+DS_DRIVER+"|\n");
        dataSource.append("cargo.datasource.username="+DS_USER+"|\n");
        dataSource.append("cargo.datasource.password="+DS_PASSWORD+"|\n");
        dataSource.append("cargo.datasource.type="+DS_TYPE_NONTX+"|\n");
        dataSource.append("cargo.datasource.jndi="+DS_JNDI);
        this.dataSourceProperty=dataSource.toString();
        
        this.fsManager = new StandardFileSystemManager();
        this.fsManager.init();
        this.fileHandler = new VFSFileHandler(this.fsManager);
        fileHandler.mkdirs(DOMAIN_HOME);
        fileHandler.mkdirs(WL_HOME);
        this.configuration = new WebLogicStandaloneLocalConfiguration(DOMAIN_HOME);
        this.configuration.setFileHandler(this.fileHandler);

        this.container = new WebLogic8xInstalledLocalContainer(configuration);
        this.container.setHome(WL_HOME);
        this.container.setFileHandler(this.fileHandler);

    }

    public void testDoConfigure() throws Exception
    {
        configuration.doConfigure(container);

        assertTrue(fileHandler.exists(DOMAIN_HOME + "/config.xml"));
        assertTrue(fileHandler.exists(DOMAIN_HOME + "/DefaultAuthenticatorInit.ldift"));
        assertTrue(fileHandler.exists(DOMAIN_HOME + "/applications/cargocpc.war"));

    }

    public void testDoConfigureSetsDefaultPort() throws Exception
    {
        configuration.doConfigure(container);
        String config = slurp(DOMAIN_HOME + "/config.xml");
        XMLAssert.assertXpathEvaluatesTo(configuration.getPropertyValue(ServletPropertySet.PORT),
            "//Server/@ListenPort", config);

    }

    public void testDoConfigureSetsPort() throws Exception
    {
        configuration.setProperty(ServletPropertySet.PORT, PORT);
        configuration.doConfigure(container);
        String config = slurp(DOMAIN_HOME + "/config.xml");
        XMLAssert.assertXpathEvaluatesTo(PORT, "//Server/@ListenPort", config);

    }
    
    public void testDoConfigureCreatesWar() throws Exception
    {
        configuration.addDeployable(new WAR("my.war"));
        configuration.doConfigure(container);
        String config = slurp(DOMAIN_HOME + "/config.xml");
        XMLAssert.assertXpathEvaluatesTo("my.war", "//WebAppComponent/@URI", config);        
    }
    
    public void testDoConfigureSetsDefaultAddress() throws Exception
    {
        configuration.doConfigure(container);
        String config = slurp(DOMAIN_HOME + "/config.xml");
        XMLAssert.assertXpathEvaluatesTo(configuration
            .getPropertyValue(GeneralPropertySet.HOSTNAME), "//Server/@ListenAddress", config);

    }

    public void testDoConfigureSetsAddress() throws Exception
    {
        configuration.setProperty(GeneralPropertySet.HOSTNAME, HOSTNAME);
        configuration.doConfigure(container);
        String config = slurp(DOMAIN_HOME + "/config.xml");
        XMLAssert.assertXpathEvaluatesTo(HOSTNAME, "//Server/@ListenAddress", config);

    }
    
    public void testDoConfigureCreatesDataSource() throws Exception
    {
        configuration.setProperty(DatasourcePropertySet.DATASOURCE, this.dataSourceProperty);
        configuration.doConfigure(container);
        String config = slurp(DOMAIN_HOME + "/config.xml");
        XMLAssert.assertXpathEvaluatesTo(DS_URL, "//JDBCConnectionPool/@URL", config);
        XMLAssert.assertXpathEvaluatesTo(DS_DRIVER, "//JDBCConnectionPool/@DriverName", config);
        XMLAssert.assertXpathEvaluatesTo("user="+DS_USER, "//JDBCConnectionPool/@Properties", config);
        XMLAssert.assertXpathEvaluatesTo(DS_PASSWORD, "//JDBCConnectionPool/@Password", config);
        XMLAssert.assertXpathEvaluatesTo("server", "//JDBCConnectionPool/@Targets", config);
        XMLAssert.assertXpathEvaluatesTo(DS_JNDI, "//JDBCConnectionPool/@Name", config);
        XMLAssert.assertXpathEvaluatesTo(DS_JNDI, "//JDBCDataSource/@Name", config);
        XMLAssert.assertXpathEvaluatesTo(DS_JNDI, "//JDBCDataSource/@JNDIName", config);
        XMLAssert.assertXpathEvaluatesTo(DS_JNDI, "//JDBCDataSource/@PoolName", config);
        XMLAssert.assertXpathEvaluatesTo("server", "//JDBCDataSource/@Targets", config);
    }
    /**
     * reads a file into a String
     * 
     * @param in - what to read
     * @return String contents of the file
     * @throws IOException
     */
    public String slurp(String file) throws IOException
    {
        InputStream in = this.fsManager.resolveFile(file).getContent().getInputStream();
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1;)
        {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }
}

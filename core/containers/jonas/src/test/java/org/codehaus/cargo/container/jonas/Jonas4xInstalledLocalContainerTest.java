/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
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
package org.codehaus.cargo.container.jonas;

import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Environment.Variable;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;
import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.Constraint;

/**
 * Unit tests for {@link Jonas4xInstalledLocalContainer}.
 * 
 * @version $Id$
 */
public class Jonas4xInstalledLocalContainerTest extends MockObjectTestCase
{
    /**
     * JONAS_ROOT folder for tests.
     */
    private static final String JONAS_ROOT = "ram:///jonasroot";

    /**
     * JONAS_BASE folder for tests.
     */
    private static final String JONAS_BASE = "ram:///jonasbase";

    /**
     * Container.
     */
    private Jonas4xInstalledLocalContainer container;

    /**
     * File system manager.
     */
    private StandardFileSystemManager fsManager;

    /**
     * File handler.
     */
    private FileHandler fileHandler;

    /**
     * Creates the test file system manager and the container. {@inheritdoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        this.fsManager = new StandardFileSystemManager();
        this.fsManager.init();
        this.fileHandler = new VFSFileHandler(this.fsManager);

        this.fileHandler.createDirectory(null, JONAS_ROOT);
        this.fileHandler.createDirectory(null, JONAS_BASE);

        LocalConfiguration configuration = new Jonas4xStandaloneLocalConfiguration(JONAS_BASE);

        this.container = new Jonas4xInstalledLocalContainer(configuration);
        this.container.setFileHandler(this.fileHandler);
        this.container.setHome(JONAS_ROOT);
    }

    /**
     * Closes the test file system manager. {@inheritdoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void tearDown() throws Exception
    {
        if (fsManager != null)
        {
            fsManager.close();
        }

        super.tearDown();
    }

    /**
     * Test system properties.
     */
    public void testSetupSysProps()
    {
        Mock mockJava = mock(Java.class);

        DoActionConstraint constaint = new DoActionConstraint();
        mockJava.expects(exactly(16)).method("addSysproperty").with(constaint);

        container.setupSysProps((Java) mockJava.proxy());

        mockJava.verify();
        assertEquals(0, constaint.remaingChecks);
    }

    /**
     * {@link Constraint} that checks how many times 
     */
    private class DoActionConstraint implements Constraint
    {
        /**
         * How many times more methods should be called.
         */
        private int remaingChecks = 5;

        /**
         * Current setting pair.
         */
        private String settingPair = null;

        /**
         * Evaluate. {@inheritdoc}
         * @param arg {@link Variable} to be evaluated.
         * @return <code>true</code> if evaluation positive, <code>false</code> otherwise.
         */
        public boolean eval(Object arg)
        {
            Variable var = (Variable) arg;
            settingPair = var.getKey() + "=" + var.getValue();
            if (var.getKey().equals("install.root"))
            {
                remaingChecks--;
                return var.getValue().endsWith("ram:/jonasroot");
            }
            else if (var.getKey().equals("jonas.base"))
            {
                remaingChecks--;
                return var.getValue().endsWith("ram:/jonasbase");
            }
            else if (var.getKey().equals("java.endorsed.dirs"))
            {
                remaingChecks--;
                return var.getValue()
                    .endsWith(fileHandler.append("ram:/jonasroot", "lib/endorsed"));
            }
            else if (var.getKey().equals("java.security.policy"))
            {
                remaingChecks--;
                return var.getValue().endsWith(
                    fileHandler.append("ram:/jonasbase", "conf/java.policy"));
            }
            else if (var.getKey().equals("java.security.auth.login.config"))
            {
                remaingChecks--;
                return var.getValue().endsWith(
                    fileHandler.append("ram:/jonasbase", "conf/jaas.config"));
            }
            return true;
        }

        /**
         * Append the unexpected settings. {@inheritdoc}
         * @param buffer Buffer to append to.
         * @return Appended <code>buffer</code>.
         */
        public StringBuffer describeTo(StringBuffer buffer)
        {
            buffer.append("unexpected settings " + settingPair);
            return buffer;
        }

    }
}

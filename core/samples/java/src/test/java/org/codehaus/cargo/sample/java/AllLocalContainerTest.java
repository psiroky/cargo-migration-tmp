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
package org.codehaus.cargo.sample.java;

import junit.framework.Test;
import org.codehaus.cargo.container.ContainerType;

import org.codehaus.cargo.container.State;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.IsLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.Validator;

/**
 * Test for local containers.
 * 
 * @version $Id$
 */
public class AllLocalContainerTest extends AbstractCargoTestCase
{
    /**
     * Initializes the test case.
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public AllLocalContainerTest(String testName, EnvironmentTestData testData)
        throws Exception
    {
        super(testName, testData);
    }

    /**
     * Creates the test suite, using the {@link Validator}s.
     * @return Test suite.
     * @throws Exception If anything goes wrong.
     */
    public static Test suite() throws Exception
    {
        CargoTestSuite suite = new CargoTestSuite("Tests that can run on all local containers");
        suite.addTestSuite(AllLocalContainerTest.class, new Validator[] {
            new IsLocalContainerValidator(),
            new HasStandaloneConfigurationValidator()});
        return suite;
    }

    /**
     * Smoke test: startup with no deployable.
     * @throws Exception If anything goes wrong.
     */
    public void testStartWithNoDeployable() throws Exception
    {
        setContainer(createContainer(createConfiguration(ConfigurationType.STANDALONE)));

        getLocalContainer().start();
        assertEquals(State.STARTED, getContainer().getState());

        getLocalContainer().stop();
        assertEquals(State.STOPPED, getContainer().getState());
    }

    /**
     * Smoke test: startup with no deployable.
     * @throws Exception If anything goes wrong.
     */
    public void testRestartWithNoDeployable() throws Exception
    {
        if ("jonas4x".equals(getTestData().containerId))
        {
            // JOnAS 4.x has trouble restarting too quickly, skip
            return;
        }

        setContainer(createContainer(createConfiguration(ConfigurationType.STANDALONE)));

        if (ContainerType.EMBEDDED.equals(getContainer().getType()))
        {
            // Do not restart embedded containers
            return;
        }

        getLocalContainer().start();
        assertEquals(State.STARTED, getContainer().getState());

        getLocalContainer().restart();
        assertEquals(State.STARTED, getContainer().getState());

        getLocalContainer().stop();
        assertEquals(State.STOPPED, getContainer().getState());

        getLocalContainer().restart();
        assertEquals(State.STARTED, getContainer().getState());

        getLocalContainer().stop();
        assertEquals(State.STOPPED, getContainer().getState());
    }
}

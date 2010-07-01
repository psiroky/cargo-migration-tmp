/* 
 * ========================================================================
 * 
 * Copyright 2007-2008 OW2. Code from this file was originally imported
 * from the OW2 JOnAS project.
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
package org.codehaus.cargo.container.jonas.internal;

import org.apache.tools.ant.taskdefs.Java;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.jonas.Jonas4xInstalledLocalContainer;
import org.codehaus.cargo.util.AntUtils;

/**
 * JOnAS 4X admin command line utils class.
 *
 * @version $Id$
 */
public class Jonas4xAdminImpl implements Jonas4xAdmin
{
    /**
     * Target JOnAS container, used for admin command line invocation setup.
     */
    private Jonas4xInstalledLocalContainer targetContainer;

    /**
     * @param targetContainer the JOnAS target container
     */
    public Jonas4xAdminImpl(final Jonas4xInstalledLocalContainer targetContainer)
    {
        this.targetContainer = targetContainer;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isServerRunning(String command, int expectedReturnCode)
    {
        Java ping = (Java) new AntUtils().createAntTask("java");
        ping.setFork(true);

        targetContainer.doAction(ping);
        ping.createArg().setValue("org.objectweb.jonas.adm.JonasAdmin");
        targetContainer.doServerAndDomainNameParam(ping);
        ping.createArg().setValue("-" + command);
        // IMPORTANT: impose timeout since default is 100 seconds
        //            the argument is in seconds in JOnAS 4
        ping.createArg().setValue("-timeout");
        ping.createArg().setValue("1");
        ping.reconfigure();

        int returnCode = ping.executeJava();
        if (returnCode != -1 && returnCode != 0 && returnCode != 1 && returnCode != 2)
        {
            throw new ContainerException("JonasAdmin ping returned " + returnCode
                    + ", the only values allowed are -1, 0, 1 and 2");
        }
        return returnCode == expectedReturnCode;
    }

    /**
     * {@inheritDoc}
     */
    public boolean unDeploy(final String beanFileName)
    {
        boolean undeployed = genericDeployment(beanFileName, "-r");
        if (!undeployed)
        {
            // file deployed trough autoload directory are not undeployed it the autoload
            // directory is not specified in the path
            undeployed = genericDeployment("autoload/" + beanFileName, "-r");
        }
        return undeployed;
    }

    /**
     * {@inheritDoc}
     */
    public boolean deploy(final String beanFileName)
    {
        return genericDeployment(beanFileName, "-a");
    }

    /**
     *
     * @param beanFileName bean File Name
     * @param deploymentParam deployment parameter
     * @return true if the deployment command(deploy or undeploy...)bean has been correctly executed
     */
    private boolean genericDeployment(final String beanFileName, final String deploymentParam)
    {
        Java java = (Java) new AntUtils().createAntTask("java");
        java.setFork(true);

        targetContainer.doAction(java);
        java.createArg().setValue("org.objectweb.jonas.adm.JonasAdmin");
        targetContainer.doServerAndDomainNameParam(java);
        java.createArg().setValue(deploymentParam);
        java.createArg().setValue(beanFileName);

        int returnCode = java.executeJava();
        return returnCode == 0;
    }
}

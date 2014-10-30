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

import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.tomcat.internal.Tomcat8xConfigurationBuilder;

/**
 * Catalina standalone {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration}
 * implementation.
 * 
 * @version $Id$
 */
public class Tomcat8xStandaloneLocalConfiguration extends Tomcat7xStandaloneLocalConfiguration
{
    /**
     * {@inheritDoc}
     * @see Tomcat7xStandaloneLocalConfiguration#Tomcat7xStandaloneLocalConfiguration(String)
     */
    public Tomcat8xStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        configurationBuilder = new Tomcat8xConfigurationBuilder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getExtraClasspathToken(WAR deployable)
    {
        String[] extraClasspath = deployable.getExtraClasspath();
        if (extraClasspath == null || extraClasspath.length <= 0)
        {
            return "";
        }
        else
        {
            StringBuilder sb = new StringBuilder();
            sb.append("<Resources>");
            for (String path : extraClasspath)
            {
                sb.append("<PostResources ");
                sb.append("className=\"org.apache.catalina.webresources.DirResourceSet\" base=\"");
                if (getFileHandler().isDirectory(path))
                {
                    sb.append(path.replace("&", "&amp;"));
                }
                else
                {
                    sb.append(getFileHandler().getParent(path).replace("&", "&amp;"));
                    sb.append("\" internalPath=\"");
                    sb.append(getFileHandler().getName(path).replace("&", "&amp;"));
                }
                sb.append("\" webAppMount=\"/WEB-INF/");
                if (getFileHandler().isDirectory(path))
                {
                    sb.append("classes");
                }
                else
                {
                    sb.append("lib");
                }
                sb.append("\" />");
            }
            sb.append("</Resources>");
            return sb.toString();
        }
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "Tomcat 8.x Standalone Configuration";
    }
}

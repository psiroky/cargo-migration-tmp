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
package org.codehaus.cargo.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.types.FilterChain;

/**
 * File operations that are performed in Cargo. All file operations must use this interface. This
 * interface is also useful for unit testing as it's possible to create a mock implementation of it
 * to prevent actual file operations from happening.
 * 
 * @version $Id$
 */
public interface FileHandler
{
    /**
     * Copy a file from a source to a destination. If destination file already exists, it is not
     * overwritten.
     * 
     * @param source the file to copy from. Must not be <code>null</code>.
     * @param target the file to copy to. Must not be <code>null</code>.
     */
    void copyFile(String source, String target);

    /**
     * Copy a file from a source to a destination specifying if source files may overwrite newer
     * destination files.
     * 
     * @param source the file to copy from. Must not be <code>null</code>.
     * @param target the file to copy to. Must not be <code>null</code>.
     * @param overwrite to overwrite the file if it already exists.
     */
    void copyFile(String source, String target, boolean overwrite);

    /**
     * Copy a file from a source to a destination using a filterchain to specify token replacement.
     * 
     * @param source the file to copy from. Must not be <code>null</code>
     * @param target the file to copy to. Must not be <code>null</code>
     * @param filterChain the filterChain to use. Must not be <code>null</code>
     * @deprecated Use {@link #copyFile(String, String, FilterChain, String)} instead.
     */
    @Deprecated
    void copyFile(String source, String target, FilterChain filterChain);

    /**
     * Copy a file from a source to a destination using a filterchain to specify token replacement.
     * 
     * @param source the file to copy from. Must not be <code>null</code>
     * @param target the file to copy to. Must not be <code>null</code>
     * @param filterChain the filterChain to use. Must not be <code>null</code>
     * @param encoding The character encoding to use, may be {@code null} or empty to use the
     *            platform's default encoding.
     */
    void copyFile(String source, String target, FilterChain filterChain, String encoding);

    /**
     * Copy a directory from a source to a destination.
     * 
     * @param source the directory to copy from. Must not be <code>null</code>.
     * @param target the directory to copy to. Must not be <code>null</code>.
     */
    void copyDirectory(String source, String target);

    /**
     * Copy a directory from a source to a destination specifying files to be excluded.
     * 
     * @param source the directory to copy from. Must not be <code>null</code>.
     * @param target the directory to copy to. Must not be <code>null</code>.
     * @param excludes the list of files to be excluded. Each entry in the list is an <a
     * href="http://ant.apache.org/manual/dirtasks.html#patterns">Ant file pattern</a>.
     */
    void copyDirectory(String source, String target, List<String> excludes);

    /**
     * Copy a directory from a source to a destination using a filterchain to specify token
     * replacement.
     * 
     * @param source the directory to copy from. Must not be <code>null</code>
     * @param target the directory to copy to. Must not be <code>null</code>
     * @param filterChain the filterChain to use. Must not be <code>null</code>
     * @param encoding The character encoding to use, may be {@code null} or empty to use the
     *            platform's default encoding.
     */
    void copyDirectory(String source, String target, FilterChain filterChain, String encoding);

    /**
     * Convenience method for creating a new directory inside another one. If the directory already
     * exists, it will return the already created directory.
     * 
     * @param parentDir The directory in which the new directory should be created
     * @param name The name of the directory to create
     * @return The new directory
     */
    String createDirectory(String parentDir, String name);

    /**
     * Copies data from an InputStream to an OutputStream.
     * 
     * @param in InputStream to copy data from
     * @param out OutputStream to copy data to
     * @param bufSize size of the copy buffer
     */
    void copy(InputStream in, OutputStream out, int bufSize);

    /**
     * Copies data from an InputStream to an OutputStream using a default buffer size.
     * 
     * @param in InputStream to copy data from
     * @param out OutputStream to copy data to
     */
    void copy(InputStream in, OutputStream out);

    /**
     * Replaces using a map of replacements in a given file.
     * 
     * @param file File to replace in.
     * @param replacements Map containing replacements.
     * @throws CargoException If anything fails, most notably if one of the replacements does not
     * exist in the file.
     * @deprecated Use {@link #replaceInFile(String, Map, String)} instead.
     */
    @Deprecated
    void replaceInFile(String file, Map<String, String> replacements) throws CargoException;

    /**
     * Replaces using a map of replacements in a given file.
     * 
     * @param file File to replace in.
     * @param replacements Map containing replacements.
     * @param encoding The character encoding to use, may be {@code null} or empty to use the
     *            platform's default encoding.
     * @throws CargoException If anything fails, most notably if one of the replacements does not
     * exist in the file.
     */
    void replaceInFile(String file, Map<String, String> replacements, String encoding)
        throws CargoException;

    /**
     * Compute the location of a temporary directory.
     * 
     * @param name The name of the directory to compute inside the tmp directory
     * @return the location of the computed temporary directory
     */
    String getTmpPath(String name);

    /**
     * Creates a unique temporary directory.
     * 
     * @return the newly created temporary directory
     */
    String createUniqueTmpDirectory();

    /**
     * Deletes a file or directory, removing any children as appropriate.
     * 
     * @param path the path to the file or directory to remove
     */
    void delete(String path);

    /**
     * @param file the file for which to get the size
     * @return long Size of the file, in bytes
     */
    long getSize(String file);

    /**
     * @param file the file for which to get an InputStream for
     * @return an InputStream pointing to the file
     */
    InputStream getInputStream(String file);

    /**
     * @param file the file for which to get an OutputStream for
     * @return an OutputStream pointing to the file
     */
    OutputStream getOutputStream(String file);

    /**
     * Append a suffix path to an existing path.
     * 
     * @param path the path to append to
     * @param suffixToAppend the suffix to append to the path
     * @return the new full path
     */
    String append(String path, String suffixToAppend);

    /**
     * Create directories for the passed path.
     * 
     * @param path the path for which to create all directories leading to it
     */
    void mkdirs(String path);

    /**
     * @param path the path for which to return its parent
     * @return the parent path of the passed path
     */
    String getParent(String path);

    /**
     * @param path the path to check the existence for
     * @return true if the path exists or false otherwise
     */
    boolean exists(String path);

    /**
     * Create a file.
     * 
     * @param file the file to create
     */
    void createFile(String file);

    /**
     * @param dir the directory to check
     * @return true if the passed directory is empty
     */
    boolean isDirectoryEmpty(String dir);

    /**
     * @param path the path to verify
     * @return true if the path passed is a directory or false otherwise
     */
    boolean isDirectory(String path);

    /**
     * @param file the file name for which to return the file name
     * @return the file name (without path)
     */
    String getName(String file);

    /**
     * @param path the path for which to return the URL
     * @return the URL for the passed path
     */
    String getURL(String path);

    /**
     * @param directory the directory for which to return all children
     * @return the children of the passed directory
     */
    String[] getChildren(String directory);

    /**
     * gets the absolute path from a file that may be relative to the current directory.
     * 
     * @param path - what to extract the file path from
     * @return - absolute path to the file
     */
    String getAbsolutePath(String path);

    /**
     * @param file the file for which to load into a String object.
     * @return a String with the file's contents.
     * @deprecated Use {@link #readTextFile(String, String)} instead.
     */
    @Deprecated
    String readTextFile(String file);

    /**
     * @param file the file for which to load into a String object.
     * @param encoding The character encoding to use, may be {@code null} or empty to use the
     *            platform's default encoding.
     * @return a String with the file's contents.
     */
    String readTextFile(String file, String encoding);
}

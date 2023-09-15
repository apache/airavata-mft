/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.airavata.mft.agent.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TransportClassLoader extends URLClassLoader {

    private static final Logger logger = LoggerFactory.getLogger(TransportClassLoader.class);

    private final ClassLoader systemClassLoader;

    private final Map<String, byte[]> content;

    public Map<String, byte[]> loadClassesFromFile(final Path path) throws IOException {
        try {
            final byte[] bytes = Files.readAllBytes(path);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(bytes);
            return unzipRecursively(baos);
        } catch (NoSuchFileException nsf) {
            logger.error("Failed to find the file {}. Current working directory {}",
                    path.toAbsolutePath().toString(), System.getProperty("user.dir"));
            throw nsf;
        }
    }

    public static final Map<String, byte[]> unzipRecursively(final ByteArrayOutputStream baos) {
        final Map<String, byte[]> result = new HashMap<String, byte[]>();
        try(final ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                final ByteArrayOutputStream os = new ByteArrayOutputStream();
                if (!entry.isDirectory()) {
                    os.write(in.readAllBytes());
                    if (entry.getName().toLowerCase().endsWith(".jar")) {
                        result.putAll(unzipRecursively(os));
                    } else if (entry.getName().toLowerCase().endsWith(".class")) {
                        result.put(entry.getName().replaceAll("/", ".").substring(0, entry.getName().length() - 6), os.toByteArray());
                    } else {
                        result.put(entry.getName(), os.toByteArray());
                    }
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public TransportClassLoader(URL[] urls, ClassLoader parent, Path connectorPath) throws IOException {
        super(urls, parent);
        systemClassLoader = getSystemClassLoader();
        content = loadClassesFromFile(connectorPath);
    }

    @Override
    public final Class<?> findClass(String name) throws ClassNotFoundException {
        final byte[] bytes = content.get(name);
        if(bytes != null) {
            logger.debug("Found class {}", name);
            return defineClass(name, bytes, 0, bytes.length);
        }
        logger.debug("Not found class {}", name);
        return super.findClass(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass == null) {
            try {
                if (systemClassLoader != null) {
                    loadedClass = systemClassLoader.loadClass(name);
                }
            } catch (ClassNotFoundException ex) {
                // class not found in system class loader... silently skipping
            }

            try {
                // find the class from given jar urls as in first constructor parameter.
                if (loadedClass == null) {
                    loadedClass = findClass(name);
                }
            } catch (ClassNotFoundException e) {
                // class is not found in the given urls.
                // Let's try it in parent classloader.
                // If class is still not found, then this method will throw class not found ex.
                loadedClass = super.loadClass(name, resolve);
            }
        }

        if (resolve) {      // marked to resolve
            resolveClass(loadedClass);
        }
        return loadedClass;
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        if (content.containsKey(name)) {
            return new ByteArrayInputStream(content.get(name));
        } else {
            return super.getResourceAsStream(name);
        }
    }
}

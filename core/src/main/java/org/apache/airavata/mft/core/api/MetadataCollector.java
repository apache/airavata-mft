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

package org.apache.airavata.mft.core.api;

import org.apache.airavata.mft.core.DirectoryResourceMetadata;
import org.apache.airavata.mft.core.FileResourceMetadata;

public interface MetadataCollector {

    /**
     * Initializes the {@link MetadataCollector}
     *
     * @param resourceServiceHost hostname of the resource service
     * @param resourceServicePort port of the resource service
     * @param secretServiceHost hostname of the secret service
     * @param secretServicePort port of the secret service
     */
    public void init(String resourceServiceHost, int resourceServicePort, String secretServiceHost, int secretServicePort);

    /**
     * Fetches a metadata of given File Resource
     *
     * @param resourceId id of the resource
     * @param credentialToken credential token for the resource
     * @return an object of {@link FileResourceMetadata}
     * @throws Exception if the resource id is not a File Resource type or the resource can't be fetched from the resource service
     */
    public FileResourceMetadata getFileResourceMetadata(String resourceId, String credentialToken) throws Exception;

    /**
     * Fetches a metadata of given File Resource
     *
     * @param storageId id of the storage resource
     * @param resourcePath resource path
     * @param credentialToken credential token for the resource
     * @return an object of {@link FileResourceMetadata}
     * @throws Exception if the resource id is not a File Resource type or the resource can't be fetched from the resource service
     */
    public FileResourceMetadata getFileResourceMetadata(String storageId, String resourcePath, String credentialToken) throws Exception;

    /**
     * Fetches a metadata of given Directory Resource
     *
     * @param resourceId id of the resource
     * @param credentialToken credential token for the resource
     * @return an object of {@link DirectoryResourceMetadata}
     * @throws Exception if the resource id is not a Directory Resource type or the resource can't be fetched from the resource service
     */
    public DirectoryResourceMetadata getDirectoryResourceMetadata(String resourceId, String credentialToken) throws Exception;

    /**
     * Fetches a metadata of given Directory Resource
     *
     * @param storageId id of the storage resource
     * @param resourcePath resource path
     * @return an object of {@link DirectoryResourceMetadata}
     * @throws Exception if the resource id is not a Directory Resource type or the resource can't be fetched from the resource service
     */
    public DirectoryResourceMetadata getDirectoryResourceMetadata(String storageId, String resourcePath, String credentialToken) throws Exception;

    /**
     * Check whether the resource is available in the actual storage
     *
     * @param resourceId id of the resource
     * @param credentialToken credential token for the resource
     * @return true of the resource is available false otherwise
     * @throws Exception if the resource details can not be fetched from the resource service
     */
    public Boolean isAvailable(String resourceId, String credentialToken) throws Exception;

    /**
     * Check whether the resource is available in the actual storage
     *
     * @param storageId id of the storage
     * @param resourcePath resource path
     * @param credentialToken credential token for the resource
     * @return true of the resource is available false otherwise
     * @throws Exception if the resource details can not be fetched from the resource service
     */
    public Boolean isAvailable(String storageId, String resourcePath, String credentialToken) throws Exception;
}

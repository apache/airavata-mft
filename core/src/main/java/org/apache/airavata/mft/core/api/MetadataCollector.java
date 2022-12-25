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

import org.apache.airavata.mft.agent.stub.ResourceMetadata;
import org.apache.airavata.mft.agent.stub.SecretWrapper;
import org.apache.airavata.mft.agent.stub.StorageWrapper;
import org.apache.airavata.mft.core.FileResourceMetadata;

public interface MetadataCollector {

    /**
     * Initializes the {@link MetadataCollector}
     *
     * @param storage
     * @param secret
     */
    public void init(StorageWrapper storage, SecretWrapper secret);

    /**
     * Fetches a metadata of given File Resource
     *
     * @param resourcePath    path of the resource
     * @param recursiveSearch
     * @return an object of {@link FileResourceMetadata}
     * @throws Exception if the resource id is not a File Resource type or the resource can't be fetched from the resource service
     */
    public ResourceMetadata getResourceMetadata(String resourcePath, boolean recursiveSearch) throws Exception;

    /**
     * Check whether the resource is available in the actual storage
     *
     * @param resourcePath path of the resource
     * @return true of the resource is available false otherwise
     * @throws Exception if the resource details can not be fetched from the resource service
     */
    public Boolean isAvailable(String resourcePath) throws Exception;

}

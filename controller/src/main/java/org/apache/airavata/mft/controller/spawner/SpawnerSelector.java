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

package org.apache.airavata.mft.controller.spawner;

import org.apache.airavata.mft.agent.stub.SecretWrapper;
import org.apache.airavata.mft.agent.stub.StorageWrapper;

import java.util.Optional;

public class SpawnerSelector {

    public static Optional<CloudAgentSpawner> selectSpawner(StorageWrapper storageWrapper, SecretWrapper secretWrapper) {
        switch (storageWrapper.getStorageCase()) {
            case S3:
                if (storageWrapper.getS3().getEndpoint().endsWith("amazonaws.com")) {
                    return Optional.of(new EC2AgentSpawner(storageWrapper, secretWrapper));
                }
                break;
        }

        return Optional.empty();
    }
}

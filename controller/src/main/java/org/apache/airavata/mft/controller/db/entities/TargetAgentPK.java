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

package org.apache.airavata.mft.controller.db.entities;

import java.io.Serializable;
import java.util.Objects;

public class TargetAgentPK implements Serializable {
    private String transferId;
    private String agentId;

    public TargetAgentPK() {
    }

    public TargetAgentPK(String transferId, String agentId) {
        this.transferId = transferId;
        this.agentId = agentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TargetAgentPK agentId1 = (TargetAgentPK) o;
        if (!transferId.equals(agentId1.transferId)) return false;
        return agentId.equals(agentId1.agentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transferId, agentId);
    }
}

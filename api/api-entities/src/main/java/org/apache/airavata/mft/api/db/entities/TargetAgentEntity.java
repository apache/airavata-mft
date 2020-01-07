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

package org.apache.airavata.mft.api.db.entities;

import javax.persistence.*;

@Entity
@IdClass(TargetAgentPK.class)
public class TargetAgentEntity {

    @Id
    @Column(name = "TRANSFER_ID")
    private String transferId;

    @ManyToOne()
    @PrimaryKeyJoinColumn(name="TRANSFER_ID", referencedColumnName="TRANSFER_ID")
    private TransferEntity transfer;

    @Id
    @Column(name = "AGENT_ID")
    private String agentId;

    @Column(name = "PRIORITY")
    private int priority;

    public TransferEntity getTransfer() {
        return transfer;
    }

    public void setTransfer(TransferEntity transfer) {
        this.transfer = transfer;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getTransferId() {
        return transferId;
    }

    public TargetAgentEntity setTransferId(String transferId) {
        this.transferId = transferId;
        return this;
    }
}

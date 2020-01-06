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

import javax.persistence.*;

@Entity
public class TransferStatusEntity {
    @Id
    @Column(name = "ID")
    @GeneratedValue
    private int id;

    @ManyToOne()
    @JoinColumn(name = "TRANSFER_ID", referencedColumnName = "TRANSFER_ID")
    private TransferEntity transfer;

    @Column(name = "STATE")
    private String status;

    @Column(name = "UPDATE_TIME")
    private long updateTimeMils;

    @Column(name = "PERCENTAGE")
    private double percentage;

    public int getId() {
        return id;
    }

    public TransferStatusEntity setId(int id) {
        this.id = id;
        return this;
    }

    public TransferEntity getTransfer() {
        return transfer;
    }

    public TransferStatusEntity setTransfer(TransferEntity transfer) {
        this.transfer = transfer;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public TransferStatusEntity setStatus(String status) {
        this.status = status;
        return this;
    }

    public long getUpdateTimeMils() {
        return updateTimeMils;
    }

    public TransferStatusEntity setUpdateTimeMils(long updateTimeMils) {
        this.updateTimeMils = updateTimeMils;
        return this;
    }

    public double getPercentage() {
        return percentage;
    }

    public TransferStatusEntity setPercentage(double percentage) {
        this.percentage = percentage;
        return this;
    }
}

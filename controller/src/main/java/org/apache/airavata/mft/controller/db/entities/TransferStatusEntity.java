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

    @Column(name = "STATUS")
    private String status;

    @Column(name = "TIME_OF_CHANGE")
    private long timeOfChange;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TransferEntity getTransfer() {
        return transfer;
    }

    public void setTransfer(TransferEntity transfer) {
        this.transfer = transfer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimeOfChange() {
        return timeOfChange;
    }

    public void setTimeOfChange(long timeOfChange) {
        this.timeOfChange = timeOfChange;
    }
}

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

package org.apache.airavata.mft.admin.models;

public class TransferState {
    private String state;
    private String publisher;
    private long updateTimeMils;
    private double percentage;
    private String description;

    public String getState() {
        return state;
    }

    public TransferState setState(String state) {
        this.state = state;
        return this;
    }

    public long getUpdateTimeMils() {
        return updateTimeMils;
    }

    public TransferState setUpdateTimeMils(long updateTimeMils) {
        this.updateTimeMils = updateTimeMils;
        return this;
    }

    public double getPercentage() {
        return percentage;
    }

    public TransferState setPercentage(double percentage) {
        this.percentage = percentage;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public TransferState setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getPublisher() {
        return publisher;
    }

    public TransferState setPublisher(String publisher) {
        this.publisher = publisher;
        return this;
    }
}

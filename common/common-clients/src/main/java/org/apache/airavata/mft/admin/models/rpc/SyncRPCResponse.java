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

 package org.apache.airavata.mft.admin.models.rpc;

public class SyncRPCResponse {
    public enum ResponseStatus {
        SUCCESS, FAIL
    }

    private String messageId;
    private String responseAsStr;
    private ResponseStatus responseStatus;
    private String errorAsStr;

    public String getMessageId() {
        return messageId;
    }

    public SyncRPCResponse setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    public String getResponseAsStr() {
        return responseAsStr;
    }

    public SyncRPCResponse setResponseAsStr(String responseAsStr) {
        this.responseAsStr = responseAsStr;
        return this;
    }

    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public SyncRPCResponse setResponseStatus(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
        return this;
    }

    public String getErrorAsStr() {
        return errorAsStr;
    }

    public SyncRPCResponse setErrorAsStr(String errorAsStr) {
        this.errorAsStr = errorAsStr;
        return this;
    }
}

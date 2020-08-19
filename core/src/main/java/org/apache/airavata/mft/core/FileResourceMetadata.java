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

package org.apache.airavata.mft.core;

public class FileResourceMetadata {

    private String friendlyName;
    private long resourceSize;
    private long createdTime;
    private long updateTime;
    private String md5sum;
    private String resourcePath;
    private String parentResourceId;
    private String parentResourceType;

    public String getFriendlyName() {
        return friendlyName;
    }

    public FileResourceMetadata setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
        return this;
    }

    public long getResourceSize() {
        return resourceSize;
    }

    public FileResourceMetadata setResourceSize(long resourceSize) {
        this.resourceSize = resourceSize;
        return this;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public FileResourceMetadata setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
        return this;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public FileResourceMetadata setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public String getMd5sum() {
        return md5sum;
    }

    public FileResourceMetadata setMd5sum(String md5sum) {
        this.md5sum = md5sum;
        return this;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public FileResourceMetadata setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
        return this;
    }

    public String getParentResourceId() {
        return parentResourceId;
    }

    public FileResourceMetadata setParentResourceId(String parentResourceId) {
        this.parentResourceId = parentResourceId;
        return this;
    }

    public String getParentResourceType() {
        return parentResourceType;
    }

    public FileResourceMetadata setParentResourceType(String parentResourceType) {
        this.parentResourceType = parentResourceType;
        return this;
    }

    public static final class Builder {
        private String friendlyName;
        private long resourceSize;
        private long createdTime;
        private long updateTime;
        private String md5sum;
        private String resourcePath;
        private String parentResourceId;
        private String parentResourceType;

        private Builder() {
        }

        public static Builder getBuilder() {
            return new Builder();
        }

        public Builder withFriendlyName(String friendlyName) {
            this.friendlyName = friendlyName;
            return this;
        }

        public Builder withResourceSize(long resourceSize) {
            this.resourceSize = resourceSize;
            return this;
        }

        public Builder withCreatedTime(long createdTime) {
            this.createdTime = createdTime;
            return this;
        }

        public Builder withUpdateTime(long updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public Builder withMd5sum(String md5sum) {
            this.md5sum = md5sum;
            return this;
        }

        public Builder withResourcePath(String resourcePath) {
            this.resourcePath = resourcePath;
            return this;
        }

        public Builder withParentResourceId(String parentResourceId) {
            this.parentResourceId = parentResourceId;
            return this;
        }

        public Builder withParentResourceType(String parentResourceType) {
            this.parentResourceType = parentResourceType;
            return this;
        }

        public FileResourceMetadata build() {
            FileResourceMetadata fileResourceMetadata = new FileResourceMetadata();
            fileResourceMetadata.setFriendlyName(friendlyName);
            fileResourceMetadata.setResourceSize(resourceSize);
            fileResourceMetadata.setCreatedTime(createdTime);
            fileResourceMetadata.setUpdateTime(updateTime);
            fileResourceMetadata.setMd5sum(md5sum);
            fileResourceMetadata.setResourcePath(resourcePath);
            fileResourceMetadata.setParentResourceId(parentResourceId);
            fileResourceMetadata.setParentResourceType(parentResourceType);
            return fileResourceMetadata;
        }
    }
}

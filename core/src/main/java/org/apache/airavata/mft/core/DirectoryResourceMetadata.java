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

import java.util.ArrayList;
import java.util.List;

public class DirectoryResourceMetadata {

    private String friendlyName;
    private long createdTime;
    private long updateTime;
    private String resourcePath;
    private String parentResourceId;
    private String parentResourceType;
    private List<DirectoryResourceMetadata> directories = new ArrayList<>();
    private List<FileResourceMetadata> files = new ArrayList<>();
    private boolean lazyInitialized = true;

    public String getFriendlyName() {
        return friendlyName;
    }

    public DirectoryResourceMetadata setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
        return this;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public DirectoryResourceMetadata setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
        return this;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public DirectoryResourceMetadata setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public DirectoryResourceMetadata setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
        return this;
    }

    public String getParentResourceId() {
        return parentResourceId;
    }

    public DirectoryResourceMetadata setParentResourceId(String parentResourceId) {
        this.parentResourceId = parentResourceId;
        return this;
    }

    public String getParentResourceType() {
        return parentResourceType;
    }

    public DirectoryResourceMetadata setParentResourceType(String parentResourceType) {
        this.parentResourceType = parentResourceType;
        return this;
    }

    public List<DirectoryResourceMetadata> getDirectories() {
        return directories;
    }

    public DirectoryResourceMetadata setDirectories(List<DirectoryResourceMetadata> directories) {
        this.directories = directories;
        return this;
    }

    public List<FileResourceMetadata> getFiles() {
        return files;
    }

    public DirectoryResourceMetadata setFiles(List<FileResourceMetadata> files) {
        this.files = files;
        return this;
    }

    public boolean isLazyInitialized() {
        return lazyInitialized;
    }

    public DirectoryResourceMetadata setLazyInitialized(boolean lazyInitialized) {
        this.lazyInitialized = lazyInitialized;
        return this;
    }


    public static final class Builder {
        private String friendlyName;
        private long createdTime;
        private long updateTime;
        private String resourcePath;
        private String parentResourceId;
        private String parentResourceType;
        private List<DirectoryResourceMetadata> directories = new ArrayList<>();
        private List<FileResourceMetadata> files = new ArrayList<>();
        private boolean lazyInitialized = true;

        private Builder() {
        }

        public static Builder getBuilder() {
            return new Builder();
        }

        public Builder withFriendlyName(String friendlyName) {
            this.friendlyName = friendlyName;
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

        public Builder withDirectories(List<DirectoryResourceMetadata> directories) {
            this.directories = directories;
            return this;
        }

        public Builder withDirectory(DirectoryResourceMetadata directory) {
            this.directories.add(directory);
            return this;
        }

        public Builder withFiles(List<FileResourceMetadata> files) {
            this.files = files;
            return this;
        }

        public Builder withFile(FileResourceMetadata file) {
            this.files.add(file);
            return this;
        }

        public Builder withLazyInitialized(boolean lazyInitialized) {
            this.lazyInitialized = lazyInitialized;
            return this;
        }

        public DirectoryResourceMetadata build() {
            DirectoryResourceMetadata directoryResourceMetadata = new DirectoryResourceMetadata();
            directoryResourceMetadata.setFriendlyName(friendlyName);
            directoryResourceMetadata.setCreatedTime(createdTime);
            directoryResourceMetadata.setUpdateTime(updateTime);
            directoryResourceMetadata.setResourcePath(resourcePath);
            directoryResourceMetadata.setParentResourceId(parentResourceId);
            directoryResourceMetadata.setParentResourceType(parentResourceType);
            directoryResourceMetadata.setDirectories(directories);
            directoryResourceMetadata.setFiles(files);
            directoryResourceMetadata.setLazyInitialized(lazyInitialized);
            return directoryResourceMetadata;
        }
    }
}

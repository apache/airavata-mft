/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.mft.transport.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.airavata.mft.core.api.TransportOperator;

public class S3TransportOperator implements TransportOperator {

    @Override
    public long getResourceSize(String resourceId) throws Exception {
        S3ResourceIdentifier resourceIdentifier = S3TransportUtil.getS3ResourceIdentifier(resourceId);
        AmazonS3 s3client = S3TransportUtil.getS3Client(resourceIdentifier.getAccessKey(),
                resourceIdentifier.getSecretKey(), resourceIdentifier.getRegion());
        S3Object s3object = s3client.getObject(resourceIdentifier.getBucket(), resourceIdentifier.getRemoteFile());
        return s3object.getObjectMetadata().getContentLength();
    }
}

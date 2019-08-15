/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.apache.airavata.mft.transport.s3;

/**
 * Includes constants related to S3 SDK
 */
public interface S3Constants {

    String ACCESS_KEY="ACCESS_KEY";
    String SECRET_KEY="SECRET_KEY";
    String BUCKET="BUCKET";
    String REGION="REGION";
    String REMOTE_FILE="REMOTE_FILE";
    int CONNECTION_EXPIRE_TIME = 1000 * 60 * 60;
    String HTTP_CONNECTION = "HTTP_CONNECTION";
    int HTTP_SUCCESS_RESPONSE_CODE = 200;
}

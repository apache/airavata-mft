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
public class S3Constants {

    public static String ACCESS_KEY = "ACCESS_KEY";
    public static String SECRET_KEY = "SECRET_KEY";
    public static String BUCKET = "BUCKET";
    public static String REGION = "REGION";
    public static String REMOTE_FILE = "REMOTE_FILE";
    public static int CONNECTION_EXPIRE_TIME = 1000 * 60 * 60;
    public static String HTTP_CONNECTION = "HTTP_CONNECTION";
    public static int HTTP_SUCCESS_RESPONSE_CODE = 200;
}

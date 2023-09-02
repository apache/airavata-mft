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

package org.apache.airavata.mft.transport.s3;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.apache.airavata.mft.credential.stubs.s3.S3Secret;
import org.apache.airavata.mft.credential.stubs.swift.SwiftSecret;
import org.apache.airavata.mft.credential.stubs.swift.SwiftV2AuthSecret;
import org.apache.airavata.mft.credential.stubs.swift.SwiftV3AuthSecret;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3Storage;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class S3Util {

    private ThreadLocal<Map<String, AmazonS3>> s3ClientCache = ThreadLocal.withInitial(() -> {
        Map<String, AmazonS3> map = new HashMap<>();
        return map;
    });

    private static S3Util instance;

    private S3Util(){}

    public static synchronized S3Util getInstance() {
        if (instance == null) {
            synchronized (S3Util.class) {
                if (instance == null) {
                    instance = new S3Util();
                }
            }
        }
        return instance;
    }

    public void releaseSwiftApi(SwiftSecret swiftSecret) {

    }

    private String getSecretKey(S3Secret s3Secret, S3Storage s3Storage) throws NoSuchAlgorithmException {
        String longSt =  s3Secret.getAccessKey()
                + s3Secret.getSecretKey()
                + s3Secret.getSessionToken()
                + s3Storage.getEndpoint() + s3Storage.getRegion();

        /*MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(longSt.getBytes());
        byte[] digest = md.digest();
        return new String(digest);*/

        return longSt;
    }

    public AmazonS3 leaseS3Client(S3Secret s3Secret, S3Storage s3Storage) throws Exception {

        String secretKey = getSecretKey(s3Secret, s3Storage);

        if (s3ClientCache.get().containsKey(secretKey)) {
            return s3ClientCache.get().get(secretKey);
        }

        AWSCredentials awsCreds;
        if (s3Secret.getSessionToken() == null || s3Secret.getSessionToken().equals("")) {
            awsCreds = new BasicAWSCredentials(s3Secret.getAccessKey(), s3Secret.getSecretKey());
        } else {
            awsCreds = new BasicSessionCredentials(s3Secret.getAccessKey(),
                    s3Secret.getSecretKey(),
                    s3Secret.getSessionToken());
        }

        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setUseTcpKeepAlive(true);

        AmazonS3ClientBuilder amazonS3ClientBuilder = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                        s3Storage.getEndpoint(), s3Storage.getRegion()))
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withClientConfiguration(clientConfiguration)
                .enablePathStyleAccess()
                .disableChunkedEncoding();

        if (s3Storage.getEnablePathStyleAccess()) {
            amazonS3ClientBuilder = amazonS3ClientBuilder.enablePathStyleAccess();
        }

        AmazonS3 s3Client = amazonS3ClientBuilder.build();

        s3ClientCache.get().put(secretKey, s3Client);
        return s3Client;
    }
}

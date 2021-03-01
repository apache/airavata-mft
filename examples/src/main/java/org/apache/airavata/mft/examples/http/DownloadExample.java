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

package org.apache.airavata.mft.examples.http;

import org.apache.airavata.mft.api.client.MFTApiClient;
import org.apache.airavata.mft.api.service.HttpDownloadApiRequest;
import org.apache.airavata.mft.api.service.HttpDownloadApiResponse;
import org.apache.airavata.mft.api.service.MFTApiServiceGrpc;
import org.apache.airavata.mft.common.AuthToken;
import org.apache.airavata.mft.common.UserTokenAuth;

public class DownloadExample {
    public static void main(String args[]) {
        AuthToken mftAuthorizationToken = AuthToken.newBuilder().setUserTokenAuth(UserTokenAuth.newBuilder().setToken("43ff79ac-e4f2-473c-9ea1-04eee9509a53").build()).build();

        MFTApiServiceGrpc.MFTApiServiceBlockingStub client = MFTApiClient.buildClient("localhost", 7004);
        HttpDownloadApiResponse httpDownloadApiResponse = client.submitHttpDownload(HttpDownloadApiRequest.newBuilder()
                .setTargetAgent("agent0")
                .setSourceResourceId("remote-ssh-resource")
                .setSourceToken("local-ssh-cred")
                .setSourceType("SCP")
                .setMftAuthorizationToken(mftAuthorizationToken)
                .build());

        System.out.println(httpDownloadApiResponse);
    }
}

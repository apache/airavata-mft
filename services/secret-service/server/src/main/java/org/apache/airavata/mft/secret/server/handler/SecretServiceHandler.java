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

package org.apache.airavata.mft.secret.server.handler;

import io.grpc.stub.StreamObserver;
import org.apache.airavata.mft.secret.service.SCPSecret;
import org.apache.airavata.mft.secret.service.SCPSecretRequest;
import org.apache.airavata.mft.secret.service.SecretServiceGrpc;
import org.lognet.springboot.grpc.GRpcService;

@GRpcService
public class SecretServiceHandler extends SecretServiceGrpc.SecretServiceImplBase {
    @Override
    public void getSCPSecret(SCPSecretRequest request, StreamObserver<SCPSecret> responseObserver) {
        SCPSecret.Builder builder = SCPSecret.newBuilder()
                .setPrivateKey("private")
                .setPrivateKey("pubKey")
                .setPassphrase("pass")
                .setSecretId("sec1");
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }
}

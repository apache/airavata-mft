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

import org.apache.airavata.mft.core.api.ConnectorChannel;
import org.apache.airavata.mft.core.bufferedImpl.channel.AbstractConnector;
import org.apache.airavata.mft.core.bufferedImpl.mediation.PassthroughMediator;

/**
 * For Testing
 */
public class Main {


    public static void main(String[] args) {

        Main main = new Main();
        main.execute();

    }

    public void execute() {

        S3ResourceIdentifier src = new S3ResourceIdentifier();
        S3ResourceIdentifier dst = new S3ResourceIdentifier();

        src.setAccessKey("XXX");
        src.setSecretKey("YYY");
        src.setRegion("us-east-2");
        src.setBucket("test");
        src.setRemoteFile("test.pdf");

        dst.setAccessKey("XXX");
        dst.setSecretKey("YYY");
        dst.setRegion("us-east-2");
        dst.setBucket("test");
        dst.setRemoteFile("testA.pdf");


        try {


            AbstractConnector s3SourceConnector = new S3SourceConnector(src);
            ConnectorChannel srcChannel = s3SourceConnector.openChannel();

            AbstractConnector s3SinkConnector = new S3SinkConnector(dst);
            ConnectorChannel dstChannel = s3SinkConnector.openChannel();

            PassthroughMediator passthroughMediator = new PassthroughMediator();


            //   Runnable r = () -> {
            passthroughMediator.mediate(srcChannel, dstChannel, (message, error) -> {
                try {
                    System.out.println(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            //  };


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

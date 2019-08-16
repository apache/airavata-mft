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
import org.apache.airavata.mft.core.bufferedImpl.AbstractConnector;
import org.apache.airavata.mft.core.bufferedImpl.ConnectorConfig;
import org.apache.airavata.mft.core.bufferedImpl.PassthroughMediator;

import java.util.Properties;

/**
 * For Testing
 */
public class Main {



    public static void main(String[] args) {

        Main main = new Main();
        main.execute();

    }

    public void execute() {
        Properties connectorConfProp = new Properties();
        connectorConfProp.put(S3Constants.ACCESS_KEY, "AKIA2SKSIFUH7QDAROSR");
        connectorConfProp.put(S3Constants.SECRET_KEY, "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");
        connectorConfProp.put(S3Constants.REGION, "us-east-2");

        Properties srcProp = new Properties();
        srcProp.put(S3Constants.BUCKET, "blimpit-test");
        srcProp.put(S3Constants.REMOTE_FILE, "test.pdf");

        Properties dstProp = new Properties();
        dstProp.put(S3Constants.BUCKET, "blimpit-test");
        dstProp.put(S3Constants.REMOTE_FILE, "teemure.pdf");

        try {

            ConnectorConfig connectorConfig = new ConnectorConfig(connectorConfProp);

            AbstractConnector s3SourceConnector = new S3SourceConnector();
            s3SourceConnector.initiate(connectorConfig);
            ConnectorChannel srcChannel = s3SourceConnector.openChannel(srcProp);

            AbstractConnector s3SinkConnector = new S3SinkConnector();
            s3SinkConnector.initiate(connectorConfig);
            ConnectorChannel dstChannel = s3SinkConnector.openChannel(dstProp);

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

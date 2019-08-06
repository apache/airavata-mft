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
package org.apache.airavata.mft.transport.scp;

import org.apache.airavata.mft.core.streaming.TransportMetadata;
import org.apache.airavata.mft.core.streaming.TransportStream;
import org.apache.airavata.mft.transport.s3.S3Receiver;
import org.apache.airavata.mft.transport.s3.S3TransportOperator;

import java.io.IOException;

public class Main {
    public static void main(final String[] arg) throws Exception {

        TransportMetadata metadata = new TransportMetadata();
        //SCPTransportOperator operator = new SCPTransportOperator();
        S3TransportOperator operator = new S3TransportOperator();
        metadata.setLength(operator.getResourceSize("3"));
        final TransportStream stream = new TransportStream("3", "2", metadata);

        Runnable r1 = new Runnable() {
            @Override
            public void run() {
                int asInt;
                try {
                    long read = 0;
                    while (true) {
                        if (stream.getInputStream().available() > 0) {
                            asInt = stream.getInputStream().read();
                            read++;
                            //char c = (char)asInt;
                            //System.out.print(c);
                        } else {
                            if (stream.isStreamCompleted()) {
                                break;
                            } else {
                                try {
                                    Thread.sleep(100);
                                    System.out.println("Waiting " + read);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        Runnable receiverRun = new Runnable() {
            @Override
            public void run() {
                //SCPReceiver receiver = new SCPReceiver();
                S3Receiver receiver = new S3Receiver();
                try {
                    receiver.receive(stream);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        Runnable senderRun = new Runnable() {
            @Override
            public void run() {
                SCPSender sender = new SCPSender();
                try {
                    sender.send(stream);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        //Thread t1 = new Thread(r1);
        //t1.start();

        Thread senderThread = new Thread(senderRun);
        senderThread.start();

        Thread receiverThread = new Thread(receiverRun);
        receiverThread.start();
        System.out.println("Done");
    }
}

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

import org.apache.airavata.mft.core.api.Connector;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public final class ConnectorResolver {
    public static Optional<Connector> resolveConnector(String type, String direction) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        String className = null;
        switch (type) {
            case "SCP":
                switch (direction) {
                    case "IN":
                        className = "org.apache.airavata.mft.transport.scp.SCPReceiver";
                        break;
                    case "OUT":
                        className = "org.apache.airavata.mft.transport.scp.SCPSender";
                        break;
                }
                break;
            case "LOCAL":
                switch (direction) {
                    case "IN":
                        className = "org.apache.airavata.mft.transport.local.LocalReceiver";
                        break;
                    case "OUT":
                        className = "org.apache.airavata.mft.transport.local.LocalSender";
                        break;
                }
                break;
            case "S3":
                switch (direction) {
                    case "IN":
                        className = "org.apache.airavata.mft.transport.s3.S3Receiver";
                        break;
                    case "OUT":
                        className = "org.apache.airavata.mft.transport.s3.S3Sender";
                        break;
                }
                break;
            case "BOX":
                switch (direction) {
                    case "IN":
                        className = "org.apache.airavata.mft.transport.box.BoxReceiver";
                        break;
                    case "OUT":
                        className = "org.apache.airavata.mft.transport.box.BoxSender";
                        break;
                }
                break;
            case "AZURE":
                switch (direction) {
                    case "IN":
                        className = "org.apache.airavata.mft.transport.azure.AzureReceiver";
                        break;
                    case "OUT":
                        className = "org.apache.airavata.mft.transport.azure.AzureSender";
                        break;
                }
                break;
            case "GCS":
                switch (direction) {
                    case "IN":
                        className = "org.apache.airavata.mft.transport.gcp.GCSReceiver";
                        break;
                    case "OUT":
                        className = "org.apache.airavata.mft.transport.gcp.GCSSender";
                        break;
                }
                break;
            case "DROPBOX":
                switch (direction) {
                    case "IN":
                        className = "org.apache.airavata.mft.transport.dropbox.DropboxReceiver";
                        break;
                    case "OUT":
                        className = "org.apache.airavata.mft.transport.dropbox.DropboxSender";
                        break;
                }
                break;
        }

        if (className != null) {
            Class<?> aClass = Class.forName(className);
            return Optional.of((Connector) aClass.getDeclaredConstructor().newInstance());
        } else {
            return Optional.empty();
        }
    }
}

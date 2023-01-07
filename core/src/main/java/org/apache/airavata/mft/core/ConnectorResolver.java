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

import org.apache.airavata.mft.core.api.IncomingChunkedConnector;
import org.apache.airavata.mft.core.api.IncomingStreamingConnector;
import org.apache.airavata.mft.core.api.OutgoingChunkedConnector;
import org.apache.airavata.mft.core.api.OutgoingStreamingConnector;

import java.util.Optional;

public final class ConnectorResolver {

    public static Optional<IncomingStreamingConnector> resolveIncomingStreamingConnector(String type) throws Exception {

        String className = null;
        switch (type) {
            case "SCP":
                className = "org.apache.airavata.mft.transport.scp.SCPIncomingConnector";
                break;
            case "S3":
                className = "org.apache.airavata.mft.transport.s3.S3IncomingConnector";
                break;
            case "ODATA":
                className = "org.apache.airavata.mft.transport.odata.ODataIncomingConnector";
                break;
            case "GCS":
                className = "org.apache.airavata.mft.transport.gcp.GCSIncomingStreamingConnector";
                break;
        }

        if (className != null) {
            Class<?> aClass = Class.forName(className);
            return Optional.of((IncomingStreamingConnector) aClass.getDeclaredConstructor().newInstance());
        } else {
            return Optional.empty();
        }
    }

    public static Optional<OutgoingStreamingConnector> resolveOutgoingStreamingConnector(String type) throws Exception {

        String className = null;
        switch (type) {
            case "SCP":
                className = "org.apache.airavata.mft.transport.scp.SCPOutgoingConnector";
                break;
            case "S3":
                className = "org.apache.airavata.mft.transport.s3.S3OutgoingStreamingConnector";
                break;
            case "GCS":
                className = "org.apache.airavata.mft.transport.gcp.GCSOutgoingStreamingConnector";
                break;

        }

        if (className != null) {
            Class<?> aClass = Class.forName(className);
            return Optional.of((OutgoingStreamingConnector) aClass.getDeclaredConstructor().newInstance());
        } else {
            return Optional.empty();
        }
    }

    public static Optional<IncomingChunkedConnector> resolveIncomingChunkedConnector(String type) throws Exception {

        String className = null;
        switch (type) {
            case "S3":
                className = "org.apache.airavata.mft.transport.s3.S3IncomingConnector";
                break;
            case "SWIFT":
                className = "org.apache.airavata.mft.transport.swift.SwiftIncomingConnector";
                break;
            case "GCS":
                className = "org.apache.airavata.mft.transport.gcp.GCSIncomingChunkedConnector";
                break;
        }

        if (className != null) {
            Class<?> aClass = Class.forName(className);
            return Optional.of((IncomingChunkedConnector) aClass.getDeclaredConstructor().newInstance());
        } else {
            return Optional.empty();
        }
    }

    public static Optional<OutgoingChunkedConnector> resolveOutgoingChunkedConnector(String type) throws Exception {

        String className = null;
        switch (type) {
            case "S3":
                className = "org.apache.airavata.mft.transport.s3.S3OutgoingConnector";
                break;
            case "SWIFT":
                className = "org.apache.airavata.mft.transport.swift.SwiftOutgoingConnector";
                break;
        }

        if (className != null) {
            Class<?> aClass = Class.forName(className);
            return Optional.of((OutgoingChunkedConnector) aClass.getDeclaredConstructor().newInstance());
        } else {
            return Optional.empty();
        }
    }

}

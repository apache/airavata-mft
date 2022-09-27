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

package org.apache.airavata.mft.command.line.sub.odata;

import org.apache.airavata.mft.api.client.MFTApiClient;
import org.apache.airavata.mft.command.line.CommandLineUtil;
import org.apache.airavata.mft.resource.stubs.odata.storage.ODataStorage;
import org.apache.airavata.mft.resource.stubs.odata.storage.ODataStorageListRequest;
import org.apache.airavata.mft.resource.stubs.odata.storage.ODataStorageListResponse;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(name = "remote", subcommands = {ODataRemoteAddSubCommand.class})
public class ODataRemoteSubCommand {

    @CommandLine.Command(name = "list")
    void listS3Resource() {
        System.out.println("Listing S3 Resource");
        MFTApiClient mftApiClient = MFTApiClient.MFTApiClientBuilder.newBuilder().build();

        ODataStorageListResponse oDataStorageListResponse = mftApiClient.getStorageServiceClient().odata()
                .listODataStorage(ODataStorageListRequest.newBuilder().setOffset(0).setLimit(10).build());

        List<ODataStorage> storagesList = oDataStorageListResponse.getStoragesList();

        int[] columnWidth = {40, 15, 55,};
        String[][] content = new String[storagesList.size() + 1][3];
        String[] headers = {"STORAGE ID", "NAME", "BASE URL"};
        content[0] = headers;


        for (int i = 1; i <= storagesList.size(); i ++) {
            ODataStorage storage = storagesList.get(i - 1);
            content[i][0] = storage.getStorageId();
            content[i][1] = storage.getName();
            content[i][2] = storage.getBaseUrl();
        }

        CommandLineUtil.printTable(columnWidth, content);
    }
}

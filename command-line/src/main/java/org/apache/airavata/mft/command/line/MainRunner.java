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

package org.apache.airavata.mft.command.line;

import org.apache.airavata.mft.command.line.sub.gcs.GCSSubCommand;
import org.apache.airavata.mft.command.line.sub.odata.ODataSubCommand;
import org.apache.airavata.mft.command.line.sub.s3.S3SubCommand;
import org.apache.airavata.mft.command.line.sub.swift.SwiftSubCommand;
import org.apache.airavata.mft.command.line.sub.transfer.TransferSubCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "checksum", mixinStandardHelpOptions = true, version = "checksum 4.0",
        description = "Prints the checksum (SHA-256 by default) of a file to STDOUT.",
        subcommands = {S3SubCommand.class, TransferSubCommand.class, SwiftSubCommand.class, ODataSubCommand.class, GCSSubCommand.class})
class MainRunner {

    public static void main(String... args) {
        int exitCode = new CommandLine(new MainRunner())
                .execute(args);
        System.exit(exitCode);
    }
}

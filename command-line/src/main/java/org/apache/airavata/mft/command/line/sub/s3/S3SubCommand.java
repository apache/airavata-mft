package org.apache.airavata.mft.command.line.sub.s3;

import org.apache.airavata.mft.command.line.sub.s3.secret.S3SecretSubCommand;
import org.apache.airavata.mft.command.line.sub.s3.storage.S3StorageSubCommand;
import picocli.CommandLine;

@CommandLine.Command(name = "s3", description = "Manage S3 resources and credentials",
        subcommands = {S3StorageSubCommand.class, S3SecretSubCommand.class})
public class S3SubCommand {


}

package org.apache.airavata.mft.command.line.sub.s3;

import picocli.CommandLine;

@CommandLine.Command(name = "s3", description = "Manage S3 resources and credentials",
        subcommands = {S3ResourceSubCommand.class, S3SecretSubCommand.class})
public class S3SubCommand {


}

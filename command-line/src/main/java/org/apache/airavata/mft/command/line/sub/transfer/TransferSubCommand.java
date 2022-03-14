package org.apache.airavata.mft.command.line.sub.transfer;

import picocli.CommandLine;

@CommandLine.Command(name = "transfer", description = "Data transfer operations",
        subcommands = {SubmitTransferSubCommand.class})
public class TransferSubCommand {
}

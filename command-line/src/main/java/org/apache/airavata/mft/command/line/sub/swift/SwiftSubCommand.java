package org.apache.airavata.mft.command.line.sub.swift;

import picocli.CommandLine;

@CommandLine.Command(name = "swift", description = "Manage Swift resources and credentials",
        subcommands = {SwiftRemoteSubCommand.class})
public class SwiftSubCommand {
}

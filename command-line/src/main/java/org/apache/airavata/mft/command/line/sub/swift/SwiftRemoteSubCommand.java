package org.apache.airavata.mft.command.line.sub.swift;

import picocli.CommandLine;

@CommandLine.Command(name = "remote", subcommands = {SwiftAddSubCommand.class})
public class SwiftRemoteSubCommand {
}

package org.apache.airavata.mft.command.line.sub.s3.secret;

import picocli.CommandLine;

@CommandLine.Command(name = "secret")
public class S3SecretSubCommand {
    @CommandLine.Command(name = "add")
    void addS3Secret() {
        System.out.println("Adding S3 Secret");
    }

    @CommandLine.Command(name = "delete")
    void deleteS3Secret(@CommandLine.Parameters(index = "0") String secretId) {
        System.out.println("Deleting S3 Secret " + secretId);
    }

    @CommandLine.Command(name = "list")
    void listS3Secret() {
        System.out.println("Listing S3 Resource");
    }

    @CommandLine.Command(name = "get")
    void getS3Secret(@CommandLine.Parameters(index = "0") String secretId) {
        System.out.println("Getting S3 Secret " + secretId);
    }
}

